<template>
  <div class="studio-shell">
    <!-- 슬림 스튜디오 상단 헤더 -->
    <header class="studio-topbar">
      <div class="topbar-left">
        <button type="button" class="back-btn" :disabled="busy" @click="router.push('/feed')">
          ← 쇼케이스로 나가기
        </button>
        <div class="divider"></div>
        <div class="project-selector">
          <span class="selector-label">작업 프로젝트</span>
          <select v-model="selectedIdeaId" :disabled="busy" @change="onSelectIdea(selectedIdeaId)">
            <option v-for="item in ideas" :key="item.id" :value="item.id">
              {{ item.selectedBrandName || item.title }} ({{ formatSiteTypeLabel(item.siteType) }})
            </option>
          </select>
        </div>
      </div>

      <div class="topbar-right">
        <span v-if="activeSiteSlug" class="slug-pill">라이브 경로: /s/{{ activeSiteSlug }}</span>
        <button
          v-if="activeSiteSlug"
          type="button"
          class="live-btn"
          :disabled="busy"
          @click="router.push({ name: 'LiveSiteRendererView', params: { siteSlug: activeSiteSlug } })"
        >
          라이브 사이트 열기 ↗
        </button>
      </div>
    </header>

    <!-- 본문: 좌측 수직 6단계 Stepper 사이드바 + 우측 집중형 작업 캔버스 -->
    <div class="studio-body">
      <aside class="studio-sidebar">
        <div class="sidebar-brand" v-if="currentIdea">
          <div v-if="previewLogoSvg" class="mini-logo" v-html="previewLogoSvg"></div>
          <div class="mini-meta">
            <strong>{{ previewBrandName }}</strong>
            <span>{{ formatSiteTypeLabel(formStep1.siteType || currentIdea.siteType) }}</span>
          </div>
        </div>

        <nav class="vertical-stepper">
          <button
            v-for="step in steps"
            :key="step.no"
            type="button"
            class="step-nav-btn"
            :disabled="busy"
            :class="{
              active: activeStep === step.no,
              done: (currentIdea?.currentStep || 1) > step.no,
              'is-disabled': busy,
            }"
            @click="goToStep(step.no)"
          >
            <span class="step-index">{{ step.no }}</span>
            <div class="step-labels">
              <div class="step-title-row">
                <strong>{{ step.title }}</strong>
                <span v-if="step.optional" class="opt-badge">선택</span>
              </div>
              <small>{{ step.desc }}</small>
            </div>
          </button>
        </nav>

        <div class="sidebar-footer">
          <p class="sidebar-tip">
            모든 변경 사항은 버전 스냅샷으로 안전하게 기록되며 언제든 이전 버전으로 되돌릴 수 있습니다.
          </p>
        </div>
      </aside>

      <section class="studio-canvas">
        <!-- 우측 상단 고정 플로팅 토스트 (스크롤 위치와 무관하게 항상 표시) -->
        <div
          v-if="toastMessage"
          class="studio-toast"
          :class="`toast-${toastType}`"
          role="status"
          aria-live="polite"
        >
          <span class="toast-icon">
            {{ toastType === 'error' ? '⚠' : toastType === 'success' ? '✓' : 'ℹ' }}
          </span>
          <span class="toast-text">{{ toastMessage }}</span>
          <button type="button" class="toast-close" @click="toastMessage = ''">✕</button>
        </div>

        <!-- 진행 상태 안내 배너 (AI 생성·저장·빌드 대기 중 안내 + 단계별 진행 표시 + 편집 잠금 안내) -->
        <div v-if="busy" class="studio-progress-banner" role="status" aria-live="polite">
          <div class="progress-banner-main">
            <div class="progress-banner-left">
              <span class="progress-spinner"></span>
              <div class="progress-texts">
                <strong>{{ busyMessage || '요청하신 작업을 처리하고 있습니다...' }}</strong>
                <span>AI 생성 및 저장 작업 중에는 데이터 보호를 위해 화면 편집과 단계 이동이 일시 잠금됩니다.</span>
              </div>
            </div>
            <span class="busy-lock-pill">편집 잠금 중</span>
          </div>
          <div v-if="busySteps.length > 0" class="progress-step-pills">
            <div
              v-for="(bStep, bIdx) in busySteps"
              :key="bIdx"
              class="progress-step-pill"
              :class="`step-${bStep.status}`"
            >
              <span class="p-step-icon">
                {{ bStep.status === 'done' ? '✓' : bStep.status === 'active' ? '⏳' : '•' }}
              </span>
              <span>{{ bStep.label }}</span>
            </div>
          </div>
        </div>

        <!-- 오류 발생 시 상단 고정 안내 배너 (원인 설명 + 재시도 / 단계 이동 제공) -->
        <div v-if="errorBanner" class="studio-error-banner" role="alert">
          <div class="error-banner-left">
            <span class="error-badge-icon">!</span>
            <div class="error-banner-body">
              <div class="error-title-row">
                <strong>{{ errorBanner.title }}</strong>
                <code v-if="errorBanner.endpoint" class="error-endpoint-pill">{{ errorBanner.endpoint }}</code>
              </div>
              <p>{{ errorBanner.detail }}</p>
            </div>
          </div>
          <div class="error-banner-actions">
            <button
              v-if="errorBanner.retryAction"
              type="button"
              class="error-retry-btn"
              :disabled="busy"
              @click="runErrorRetry"
            >
              다시 시도
            </button>
            <button
              v-if="errorBanner.canMoveToStep2 && activeStep === 1"
              type="button"
              class="error-step-btn"
              :disabled="busy"
              @click="forceMoveToStep2"
            >
              2단계 화면으로 이동 →
            </button>
            <button type="button" class="error-dismiss-btn" @click="errorBanner = null">
              닫기 ✕
            </button>
          </div>
        </div>

        <!-- ================================================================ -->
        <!-- 1단계: 아이디어 정의 -->
        <!-- ================================================================ -->
        <div
          v-if="activeStep === 1"
          class="canvas-panel"
          :class="{ 'is-busy-locked': busy }"
          :inert="busy || undefined"
        >
          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 01 · AI INTENT DIAGNOSIS & 52-FEATURE CATALOG</span>
              <h2>아이디어 프롬프트 입력 및 AI 맞춤 기능 카탈로그 구성</h2>
              <p>만들고 싶은 사이트의 아이디어를 적어주시면 AI 에이전트가 최적 아키텍처(커머스/브랜드웹/복합)를 자율 판단하고, DB 52종 전체 기능 중 꼭 필요한 기능을 추천합니다.</p>
            </div>
            <button type="button" class="primary-btn" :disabled="busy" @click="saveCurrentIdeaAndGoStep2">
              {{ busy ? (busyMessage || '저장 및 AI 생성 중...') : '현재 프로젝트 저장 후 2단계 이동 →' }}
            </button>
          </div>

          <div v-if="studioPresets.length > 0" class="starter-preset-bar">
            <span class="starter-label">원클릭 업종별 스타터 프롬프트</span>
            <div class="starter-chips">
              <button
                v-for="preset in studioPresets"
                :key="preset.code || preset.title"
                type="button"
                class="starter-chip"
                @click="applyStarterPreset(preset)"
              >
                {{ preset.title || preset.label }}
              </button>
            </div>
          </div>

          <div class="field-group">
            <label>프로젝트 제목</label>
            <input
              v-model="formStep1.title"
              type="text"
              placeholder="예: 충남 공주 냇가 옆 프라이빗 스테이 & 평상 백숙 민박 '맑은물 민박'"
            />
          </div>

          <div class="field-group">
            <div class="field-label-split">
              <label>아이디어 상세 설명 (AI 프롬프트)</label>
              <div class="intent-action-btns">
                <button
                  type="button"
                  class="ai-intent-btn"
                  :disabled="busy || analyzingIntent"
                  @click="runIntentAnalysis(false)"
                >
                  {{ analyzingIntent ? 'AI 의도·기능 정밀 분석 중...' : '✨ AI로 아이디어 분석 & 맞춤 기능 추천받기' }}
                </button>
                <button
                  type="button"
                  class="ai-intent-sub-btn"
                  :disabled="busy || analyzingIntent"
                  @click="runIntentAnalysis(true)"
                >
                  ✨ 기획 프롬프트 확장 + 사진 캡션 자동완성
                </button>
              </div>
            </div>
            <textarea
              v-model="formStep1.promptText"
              rows="4"
              placeholder="만들고 싶은 공간·브랜드·상품과 원하는 기능(예: 객실/평상 예약, 시즌 요금표, 오시는 길, FAQ, 포토갤러리 등)을 자유롭게 적어주세요."
            ></textarea>
          </div>

          <!-- AI 자율 아키텍처 & 의도 진단 리포트 배너 -->
          <div class="ai-intent-banner">
            <div class="ai-intent-head">
              <div class="ai-intent-badges">
                <span class="intent-kicker-pill">AI ARCHITECTURE ADVISOR</span>
                <span class="intent-arch-badge">
                  추천 아키텍처: {{ formatSiteTypeLabel(formStep1.siteType) }} ({{ formStep1.siteType }})
                </span>
                <span v-if="aiIntentAnalysis.heroStyle" class="intent-hero-badge">
                  히어로 무드: {{ formatHeroStyleLabel(aiIntentAnalysis.heroStyle) }}
                </span>
              </div>
              <div class="arch-override-group">
                <span class="override-label">아키텍처 수동 변경:</span>
                <button
                  v-for="t in siteTypes"
                  :key="t.code"
                  type="button"
                  class="arch-mini-chip"
                  :class="{ active: formStep1.siteType === t.code }"
                  @click="formStep1.siteType = t.code"
                >
                  {{ t.title }}
                </button>
              </div>
            </div>

            <p v-if="aiIntentAnalysis.siteTypeReason" class="ai-intent-reason">
              <strong>AI 아키텍처 진단:</strong> {{ aiIntentAnalysis.siteTypeReason }}
            </p>
            <p v-else class="ai-intent-reason muted">
              프롬프트를 입력한 뒤 <strong>[✨ AI로 아이디어 분석 & 맞춤 기능 추천받기]</strong>를 누르거나 2단계로 이동하면, AI가 최적 사이트 아키텍처와 필요 기능을 자동으로 진단합니다.
            </p>

            <div
              v-if="aiIntentAnalysis.expandedPrompt && aiIntentAnalysis.expandedPrompt !== formStep1.promptText"
              class="expanded-prompt-box"
            >
              <div class="expanded-prompt-top">
                <strong>💡 AI가 구체화한 상세 기획 프롬프트 제안</strong>
                <button type="button" class="apply-expanded-btn" @click="applyExpandedPrompt">
                  이 기획 프롬프트로 교체 적용 →
                </button>
              </div>
              <p>{{ aiIntentAnalysis.expandedPrompt }}</p>
            </div>
          </div>

          <!-- 1단계: 업종 카테고리 및 핵심 키워드 태그 직접 편집 -->
          <div class="step1-meta-grid">
            <div class="field-group">
              <label>업종 · 도메인 카테고리 (AI 네이밍·기획서 톤앤매너 반영)</label>
              <select v-model="formStep1.category" class="step1-select">
                <option value="AUTO">AI 에이전트 자율 추론 (프롬프트 기반 자동 판단)</option>
                <option
                  v-if="
                    formStep1.category &&
                    ![
                      'AUTO',
                      'ACCOMMODATION',
                      'FOOD_BEVERAGE',
                      'RETAIL_CRAFT',
                      'CULTURE_CLASS',
                      'LOCAL_BRAND',
                      'TECH_SERVICE',
                    ].includes(formStep1.category)
                  "
                  :value="formStep1.category"
                >
                  AI 자율 추론 업종: {{ formStep1.category }}
                </option>
                <option value="ACCOMMODATION">숙박 · 민박 · 감성 스테이 · 펜션</option>
                <option value="FOOD_BEVERAGE">식음료 · 식당 · 로스터리 카페 · 베이커리</option>
                <option value="RETAIL_CRAFT">공예 · 리빙 · 패션 · 라이프스타일 편집숍</option>
                <option value="CULTURE_CLASS">원데이 클래스 · 전시 · 살롱 · 공간 대관</option>
                <option value="LOCAL_BRAND">로컬 특산물 · 농수산 직거래 · 팜투테이블</option>
                <option value="TECH_SERVICE">IT · SaaS · 크리에이터 · 디지털 서비스</option>
              </select>
            </div>

            <div class="field-group">
              <label>핵심 브랜드 키워드 태그 (네이밍·SEO·컨시어지 핵심 어휘)</label>
              <div class="tag-editor-box">
                <div class="tag-chip-list">
                  <span v-for="(tag, tIdx) in formStep1.tags" :key="tag + tIdx" class="editable-tag-chip">
                    #{{ tag }}
                    <button type="button" class="tag-del-x" @click="formStep1.tags.splice(tIdx, 1)">×</button>
                  </span>
                  <input
                    v-model="newTagInput"
                    type="text"
                    class="tag-inline-input"
                    placeholder="키워드 입력 후 Enter (예: 냇가민박, 평상백숙, 독채스테이)"
                    @keydown.enter.prevent="addStep1Tag"
                  />
                  <button type="button" class="tiny-add-btn" @click="addStep1Tag">+ 태그 추가</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 1단계: 보유 사진·로고·참고문서 통합 업로드 & 자산 보관함 -->
          <div class="asset-upload-panel">
            <div class="asset-panel-head">
              <div>
                <div class="asset-title-row">
                  <strong>보유 사진 · 로고 · 참고자료 업로드</strong>
                  <span class="asset-count-pill">현재 {{ ideaAssets.length }}건 등록됨</span>
                </div>
                <p>
                  민박·매장 전경, 객실·상품 사진, 기존 로고(SVG/PNG), 요금표·안내문(PDF/DOCX/TXT/CSV)을 올리면 AI가 사진별 최적 용도(배너/객실·상품/갤러리/매거진)와 한국어 설명을 자동 부여합니다.
                </p>
              </div>
              <div class="asset-head-controls">
                <select v-model="uploadAssetType" class="asset-type-select" title="업로드 파일 기본 용도">
                  <option v-for="at in assetTypes" :key="at.code" :value="at.code">
                    {{ at.label }}
                  </option>
                </select>
                <input
                  ref="step1FileInputRef"
                  type="file"
                  multiple
                  accept="image/*,.svg,.pdf,.doc,.docx,.txt,.md,.csv,.json,.html"
                  class="hidden-file-input"
                  @change="onStep1FileInputChange"
                />
                <button
                  type="button"
                  class="secondary-btn"
                  :disabled="busy || assetUploading"
                  @click="triggerStep1FileSelect"
                >
                  {{ assetUploading ? '업로드 중...' : '+ 사진·파일 선택 업로드' }}
                </button>
              </div>
            </div>

            <div
              class="asset-dropzone"
              :class="{ dragging: isDraggingFiles }"
              @dragover.prevent="isDraggingFiles = true"
              @dragleave.prevent="isDraggingFiles = false"
              @drop.prevent="onStep1FileDrop"
              @click="triggerStep1FileSelect"
            >
              <div class="dropzone-inner">
                <span class="dropzone-badge">DRAG & DROP MULTI-UPLOAD</span>
                <strong>여기로 사진이나 문서 파일을 끌어다 놓거나 클릭하여 여러 파일을 한 번에 업로드하세요</strong>
                <span>지원 형식: JPG, PNG, WEBP, GIF, SVG(로고), PDF, DOCX, TXT, CSV, MD (파일당 최대 50MB · 문서 텍스트 자동 추출)</span>
              </div>
            </div>

            <div v-if="ideaAssets.length > 0" class="asset-card-grid">
              <div v-for="asset in ideaAssets" :key="asset.id" class="asset-item-card">
                <div class="asset-thumb-box">
                  <img
                    v-if="isImageAsset(asset)"
                    :src="asset.fileUrl"
                    :alt="asset.caption || asset.originalName"
                    class="asset-thumb-img"
                  />
                  <div v-else class="asset-doc-icon">
                    <span>{{ (asset.fileExt || 'DOC').toUpperCase() }}</span>
                    <small>참고문서</small>
                  </div>
                  <span class="asset-type-chip">{{ formatAssetTypeLabel(asset.assetType) }}</span>
                </div>

                <div class="asset-item-body">
                  <div class="asset-name-row">
                    <a :href="asset.fileUrl" target="_blank" rel="noopener noreferrer" class="asset-filename" :title="asset.originalName">
                      {{ asset.originalName }}
                    </a>
                    <span class="asset-filesize">{{ formatFileSize(asset.fileSize) }}</span>
                  </div>

                  <div class="asset-meta-controls">
                    <select
                      v-model="asset.assetType"
                      class="asset-mini-select"
                      @change="onAssetMetaChange(asset)"
                    >
                      <option
                        v-for="at in selectableAssetTypes"
                        :key="at.code"
                        :value="at.code"
                      >
                        {{ at.label }}
                      </option>
                    </select>
                    <input
                      v-model="asset.caption"
                      type="text"
                      class="asset-caption-input"
                      placeholder="사진·자료 설명 (예: 냇가 평상 전경, 황토방 객실, 2026 성수기 요금표)"
                      @change="onAssetMetaChange(asset)"
                    />
                  </div>

                  <div v-if="asset.extractedText" class="asset-extracted-box">
                    <p>{{ asset.extractedText }}</p>
                  </div>

                  <div class="asset-card-actions">
                    <button
                      type="button"
                      class="asset-action-btn"
                      :disabled="busy"
                      @click="applyAssetTextToPrompt(asset)"
                    >
                      AI 프롬프트에 내용 반영
                    </button>
                    <button
                      type="button"
                      class="asset-del-btn"
                      :disabled="busy"
                      @click="removeIdeaAsset(asset)"
                    >
                      삭제
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 1단계: DB 단일 원천 52종 전체 기능 카탈로그 실시간 검색·필터·선택 보드 -->
          <div class="feature-catalog-panel" data-testid="feature-catalog-panel">
            <div class="feature-catalog-head">
              <div>
                <div class="feature-head-title-row">
                  <span class="step-kicker">SYNC-SHOP · SYNC-CMS · AI CORE CAPABILITY MARKETPLACE</span>
                  <span class="feature-stat-pill">전체 {{ featureCatalog.length }}종</span>
                  <span class="feature-stat-pill recommended">✨ AI 추천 {{ recommendedFeatureCodes.length }}종</span>
                  <span class="feature-stat-pill active">✅ 현재 선택 {{ enabledFeatures.length }}종</span>
                </div>
                <h3>사이트에 탑재할 세부 기능 카드 검색 및 선택 (DB 단일 원천 {{ featureCatalog.length }}종)</h3>
                <p>
                  검색창에 원하는 기능 키워드(예: <strong>요금표, 객실, 평상, 쿠폰, 포토갤러리, FAQ, 오시는 길, 투표, 후기</strong>)를 입력하거나 카테고리별로 필터링하여 원하는 기능을 자유롭게 켜고 끌 수 있습니다.
                </p>
              </div>
              <div class="feature-head-actions">
                <button
                  type="button"
                  class="secondary-btn"
                  :disabled="busy"
                  @click="selectRecommendedFeatures"
                >
                  ✨ AI 추천 기능({{ recommendedFeatureCodes.length }}종) 일괄 적용
                </button>
                <button
                  type="button"
                  class="secondary-btn"
                  :disabled="busy || !selectedIdeaId"
                  @click="saveEnabledFeaturesToServer(true)"
                >
                  선택 기능 즉시 저장 ({{ enabledFeatures.length }}종)
                </button>
              </div>
            </div>

            <!-- 실시간 검색바 & 엔진 필터 -->
            <div class="feature-search-bar">
              <div class="feature-search-input-wrap">
                <span class="search-icon-label">🔍 기능 검색</span>
                <input
                  v-model="featureSearchQuery"
                  type="text"
                  data-testid="feature-search-input"
                  class="feature-search-input"
                  placeholder="기능명, 설명, 유의어 검색 (예: 요금표, 객실, 예약폼, 스페이스 투어, FAQ, 오시는 길, 쿠폰, 타임특가, 투표, 방명록...)"
                />
                <button
                  v-if="featureSearchQuery"
                  type="button"
                  class="clear-search-btn"
                  @click="featureSearchQuery = ''"
                >
                  검색 초기화 ×
                </button>
              </div>
              <div class="engine-filter-pills">
                <button
                  v-for="eng in engineFilterOptions"
                  :key="eng.code"
                  type="button"
                  class="engine-pill"
                  :class="{ active: activeEngineFilter === eng.code }"
                  @click="activeEngineFilter = eng.code"
                >
                  {{ eng.label }}
                </button>
              </div>
            </div>

            <!-- 카테고리 탭 바 -->
            <div class="feature-category-tabs">
              <button
                type="button"
                class="feat-cat-tab"
                :class="{ active: activeFeatureCategoryTab === 'ALL' }"
                @click="activeFeatureCategoryTab = 'ALL'"
              >
                전체 기능 ({{ featureCatalog.length }})
              </button>
              <button
                type="button"
                class="feat-cat-tab recommended"
                :class="{ active: activeFeatureCategoryTab === 'RECOMMENDED' }"
                @click="activeFeatureCategoryTab = 'RECOMMENDED'"
              >
                ✨ AI 맞춤 추천 ({{ recommendedFeatureCodes.length }})
              </button>
              <button
                type="button"
                class="feat-cat-tab selected"
                :class="{ active: activeFeatureCategoryTab === 'SELECTED' }"
                @click="activeFeatureCategoryTab = 'SELECTED'"
              >
                ✅ 선택됨 ({{ enabledFeatures.length }})
              </button>
              <button
                v-for="cat in featureCategories"
                :key="cat.code"
                type="button"
                class="feat-cat-tab"
                :class="{ active: activeFeatureCategoryTab === cat.code }"
                @click="activeFeatureCategoryTab = cat.code"
              >
                {{ cat.label }} ({{ countFeaturesByCategory(cat.code) }})
              </button>
            </div>

            <!-- 기능 카드 그리드 -->
            <div v-if="filteredFeatureCatalog.length > 0" class="feature-card-grid">
              <div
                v-for="feat in filteredFeatureCatalog"
                :key="feat.featureCode"
                class="feature-catalog-card"
                :data-feature-code="feat.featureCode"
                :class="{
                  selected: isFeatureEnabled(feat.featureCode),
                  recommended: isFeatureRecommended(feat.featureCode),
                }"
                @click="toggleFeatureCard(feat.featureCode)"
              >
                <div class="feat-card-top">
                  <div class="feat-badges">
                    <span class="feat-engine-badge" :class="(feat.engineCode || 'SYNC_CMS').toLowerCase()">
                      {{ formatEngineBadge(feat.engineCode) }}
                    </span>
                    <span class="feat-cat-badge">{{ feat.badgeLabel || feat.categoryName }}</span>
                    <span v-if="isFeatureRecommended(feat.featureCode)" class="feat-ai-rec-badge">
                      ✨ AI 추천
                    </span>
                  </div>
                  <span class="feat-toggle-pill" :class="{ on: isFeatureEnabled(feat.featureCode) }">
                    {{ isFeatureEnabled(feat.featureCode) ? '✓ 탑재됨' : '+ 선택' }}
                  </span>
                </div>

                <h4 class="feat-card-title">{{ feat.title }}</h4>
                <p class="feat-card-sub">{{ feat.subtitle }}</p>
                <p class="feat-card-desc">{{ feat.description }}</p>

                <div v-if="getFeatureReason(feat.featureCode)" class="feat-ai-reason-box">
                  <strong>AI 추천 사유:</strong> {{ getFeatureReason(feat.featureCode) }}
                </div>

                <div v-if="Array.isArray(feat.searchKeywords) && feat.searchKeywords.length" class="feat-keyword-row">
                  <span
                    v-for="kw in feat.searchKeywords.slice(0, 5)"
                    :key="kw"
                    class="feat-kw-chip"
                  >
                    #{{ kw }}
                  </span>
                </div>
              </div>
            </div>
            <div v-else class="feature-empty-box">
              <strong>조건에 맞는 기능 카드가 없습니다.</strong>
              <p>검색어를 지우거나 '전체 기능 ({{ featureCatalog.length }})' 탭을 선택해 모든 기능을 확인해 보세요.</p>
              <button
                type="button"
                class="secondary-btn"
                @click="
                  featureSearchQuery = '';
                  activeFeatureCategoryTab = 'ALL';
                  activeEngineFilter = 'ALL';
                "
              >
                전체 기능 목록 보기
              </button>
            </div>
          </div>

          <div class="panel-actions-split">
            <button type="button" class="primary-btn" :disabled="busy" @click="saveCurrentIdeaAndGoStep2">
              {{ busy ? (busyMessage || '저장 및 AI 생성 중...') : '현재 프로젝트에 반영하고 2단계(네이밍·로고)로 이동 →' }}
            </button>
            <button type="button" class="secondary-btn" :disabled="busy" @click="createNewIdeaFromStep1">
              {{ busy ? '처리 중...' : '+ 완전히 새로운 브랜드 프로젝트로 생성하기' }}
            </button>
          </div>
        </div>

        <!-- ================================================================ -->
        <!-- 2단계: 네이밍 & 로고 -->
        <!-- ================================================================ -->
        <div
          v-else-if="activeStep === 2"
          class="canvas-panel"
          :class="{ 'is-busy-locked': busy }"
          :inert="busy || undefined"
        >
          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 02 · AI BRAND NAMING & VECTOR LOGO</span>
              <h2>AI 브랜드 네이밍 8종 & 벡터 SVG 로고 6종</h2>
              <p>AI 후보를 선택하거나, 브랜드명·슬러그 주소·스토리·컬러 팔레트·SVG 로고를 직접 커스텀 편집할 수 있습니다.</p>
            </div>
            <div class="header-btns">
              <select v-model="namingTonePreference" class="tone-select" title="네이밍 톤앤매너 선택">
                <option value="ALL">전체 조화형 톤</option>
                <option value="PURE_KOREAN">순우리말 · 자연 감성</option>
                <option value="HANJA_CLASSIC">단아한 한자 · 전통미</option>
                <option value="MODERN_EDITORIAL">모던 에디토리얼 · 라이프스타일</option>
                <option value="GLOBAL_ENGLISH">글로벌 영문 · 미니멀</option>
              </select>
              <input
                v-model="keywordHint"
                type="text"
                class="hint-input"
                placeholder="네이밍 키워드 힌트 (예: 냇가, 평상, 로스터리)"
              />
              <button type="button" class="secondary-btn" :disabled="busy" @click="regenerateNames">
                {{ busy ? '생성 중...' : '네이밍 8종 재생성' }}
              </button>
              <button type="button" class="secondary-btn" :disabled="busy" @click="regenerateLogos">
                {{ busy ? '생성 중...' : 'SVG 로고 6종 재생성' }}
              </button>
              <input
                ref="step2LogoInputRef"
                type="file"
                accept="image/*,.svg"
                class="hidden-file-input"
                @change="onStep2LogoFileChange"
              />
              <button
                type="button"
                class="secondary-btn"
                :disabled="busy || assetUploading"
                @click="triggerStep2LogoUpload"
              >
                {{ assetUploading ? '로고 업로드 중...' : '+ 보유 로고 업로드 (SVG/PNG)' }}
              </button>
              <button type="button" class="primary-btn" :disabled="busy" @click="confirmBrandAndNext">
                브랜드 확정 및 3단계 이동 →
              </button>
            </div>
          </div>

          <!-- 실시간 브랜드 키트 조합 미리보기 & 직접 커스텀 편집 워크벤치 -->
          <div
            class="brand-kit-preview"
            :style="{
              background: customBrandForm.colorPalette.surface || selectedLogoObj?.colorPalette?.surface || '#f8fafc',
              borderColor: customBrandForm.colorPalette.primary || selectedLogoObj?.colorPalette?.primary || '#cbd5e1',
            }"
          >
            <div class="kit-main-row">
              <div class="kit-left">
                <div class="kit-tag-row">
                  <span class="kit-tag">선택된 브랜드 아이덴티티 키트 (실시간 동기화)</span>
                  <button
                    type="button"
                    class="kit-edit-toggle-btn"
                    @click="showBrandDirectEditor = !showBrandDirectEditor"
                  >
                    {{ showBrandDirectEditor ? '직접 편집 패널 접기 ▲' : '✎ 브랜드명 · 주소 · 컬러 · SVG 직접 편집 ▼' }}
                  </button>
                </div>
                <div class="kit-brand-row">
                  <div v-if="previewLogoSvg" class="kit-logo" v-html="previewLogoSvg"></div>
                  <div class="kit-info">
                    <strong>{{ previewBrandName }}</strong>
                    <span>
                      {{ customBrandForm.pronunciation || selectedNameObj?.pronunciation || 'Brand Identity' }}
                      · 라이브 주소:
                      <code>/s/{{ customBrandForm.siteSlug || selectedNameObj?.siteSlug || activeSiteSlug || 'auto-slug' }}</code>
                    </span>
                    <p v-if="customBrandForm.brandStory || selectedNameObj?.brandStory" class="kit-story-line">
                      {{ sanitizeBrandStoryText(customBrandForm.brandStory || selectedNameObj?.brandStory) }}
                    </p>
                  </div>
                </div>
              </div>
              <div class="kit-right">
                <div class="kit-swatches">
                  <label class="kit-swatch editable-swatch" title="메인 컬러 변경">
                    <input type="color" v-model="customBrandForm.colorPalette.primary" @input="onCustomPaletteChange" />
                    <i :style="{ background: customBrandForm.colorPalette.primary }"></i>
                    <small>primary: {{ customBrandForm.colorPalette.primary }}</small>
                  </label>
                  <label class="kit-swatch editable-swatch" title="보조 컬러 변경">
                    <input type="color" v-model="customBrandForm.colorPalette.secondary" @input="onCustomPaletteChange" />
                    <i :style="{ background: customBrandForm.colorPalette.secondary }"></i>
                    <small>secondary: {{ customBrandForm.colorPalette.secondary }}</small>
                  </label>
                  <label class="kit-swatch editable-swatch" title="포인트 컬러 변경">
                    <input type="color" v-model="customBrandForm.colorPalette.accent" @input="onCustomPaletteChange" />
                    <i :style="{ background: customBrandForm.colorPalette.accent }"></i>
                    <small>accent: {{ customBrandForm.colorPalette.accent }}</small>
                  </label>
                  <label class="kit-swatch editable-swatch" title="배경 서피스 컬러 변경">
                    <input type="color" v-model="customBrandForm.colorPalette.surface" @input="onCustomPaletteChange" />
                    <i :style="{ background: customBrandForm.colorPalette.surface }"></i>
                    <small>surface: {{ customBrandForm.colorPalette.surface }}</small>
                  </label>
                </div>
                <span
                  class="kit-sample-btn"
                  :style="{
                    background: customBrandForm.colorPalette.primary || '#4f46e5',
                    borderRadius: customBrandForm.colorPalette.radius || '12px',
                  }"
                >
                  {{ previewBrandName }} 탐색하기 →
                </span>
              </div>
            </div>

            <!-- 브랜드명·슬러그·스토리·컬러·SVG 코드 직접 편집 워크벤치 -->
            <div v-if="showBrandDirectEditor" class="brand-direct-editor">
              <div class="direct-editor-grid">
                <div class="mini-field">
                  <label>브랜드명 직접 입력 (입력 즉시 로고 텍스트 자동 동기화)</label>
                  <input
                    v-model="customBrandForm.brandName"
                    type="text"
                    placeholder="예: 냇가식당, 청류민박, 공주여울스테이"
                    @input="onCustomBrandNameInput"
                  />
                </div>
                <div class="mini-field">
                  <label>라이브 사이트 슬러그 주소 (/s/ 뒤에 붙는 영문·숫자 주소)</label>
                  <div class="slug-input-group">
                    <span class="slug-prefix">/s/</span>
                    <input
                      v-model="customBrandForm.siteSlug"
                      type="text"
                      placeholder="예: naet-ga-sik-dang"
                      @input="onCustomSlugInput"
                    />
                  </div>
                </div>
                <div class="mini-field">
                  <label>발음 표기 / 영문 서브타이틀</label>
                  <input
                    v-model="customBrandForm.pronunciation"
                    type="text"
                    placeholder="예: 냇가식당 / naet-ga-sik-dang"
                  />
                </div>
                <div class="mini-field">
                  <label>버튼·카드 모서리 곡률 (Radius)</label>
                  <select v-model="customBrandForm.colorPalette.radius">
                    <option value="6px">샤프 모던 (6px)</option>
                    <option value="12px">스탠다드 라운드 (12px)</option>
                    <option value="16px">소프트 감성 라운드 (16px)</option>
                    <option value="24px">풀 라운드 칩 (24px)</option>
                  </select>
                </div>
              </div>

              <div class="mini-field" style="margin-top: 8px">
                <label>브랜드 스토리 & 슬로건 직접 편집 (3단계 기획서 및 4단계 메인 카피에 반영)</label>
                <input
                  v-model="customBrandForm.brandStory"
                  type="text"
                  placeholder="브랜드의 철학과 공간의 매력을 한두 문장으로 입력해 주세요."
                />
              </div>

              <div class="svg-code-toggle-row">
                <button
                  type="button"
                  class="tiny-add-btn"
                  @click="showSvgCodeEditor = !showSvgCodeEditor"
                >
                  {{ showSvgCodeEditor ? 'SVG 벡터 코드 편집기 닫기 ▲' : '</> SVG 로고 벡터 코드 직접 편집 열기 ▼' }}
                </button>
                <span class="helper-caption">
                  상단 컬러 칩을 클릭해 색상을 바꾸거나, 직접 SVG 코드를 수정·붙여넣기 할 수 있습니다.
                </span>
              </div>

              <div v-if="showSvgCodeEditor" class="svg-code-editor-box">
                <textarea
                  v-model="customBrandForm.svgCode"
                  rows="4"
                  class="code-editor-mini"
                  spellcheck="false"
                  placeholder="<svg viewBox='0 0 320 90' xmlns='http://www.w3.org/2000/svg'>...</svg>"
                ></textarea>
              </div>
            </div>
          </div>

          <div class="section-title-row">
            <h3 class="sub-heading">
              AI 추천 브랜드 네이밍 후보 ({{ generatingNames ? '8종 생성 중...' : `${nameSuggestions.length}종` }})
            </h3>
            <span class="helper-caption">원하는 브랜드명 카드를 클릭하면 상단 브랜드 키트, 사이트 주소, 하단 모든 SVG 로고의 텍스트가 즉시 연동됩니다.</span>
          </div>
          <div v-if="generatingNames" class="name-grid skeleton-grid" aria-busy="true">
            <div v-for="n in 8" :key="'name-skel-' + n" class="name-card skeleton-card">
              <div class="skel-row">
                <span class="skel-bar skel-pill"></span>
                <span class="skel-bar skel-pill-sm"></span>
              </div>
              <div class="skel-bar skel-title"></div>
              <div class="skel-bar skel-sub"></div>
              <div class="skel-bar skel-line"></div>
              <div class="skel-bar skel-line short"></div>
            </div>
          </div>
          <div v-else-if="nameSuggestions.length === 0" class="empty-step-box">
            <strong>아직 생성된 AI 브랜드 네이밍 후보가 없습니다.</strong>
            <p>네이밍 생성 요청이 실패했거나 아직 실행되지 않았습니다. 아래 버튼을 눌러 AI 브랜드 네이밍 8종을 생성해 주세요.</p>
            <button type="button" class="primary-btn" :disabled="busy" @click="regenerateNames">
              {{ busy ? 'AI 네이밍 생성 중...' : 'AI 브랜드 네이밍 8종 생성하기' }}
            </button>
          </div>
          <div v-else class="name-grid">
            <div
              v-for="name in nameSuggestions"
              :key="name.id"
              class="name-card"
              :class="{ selected: selectedNameId === name.id }"
              @click="selectNameCandidate(name)"
            >
              <div class="name-top">
                <span class="score-pill">적합도 {{ name.score }}점</span>
                <span class="slug-ok">✓ 주소 사용 가능</span>
              </div>
              <h4>{{ name.brandName }}</h4>
              <div class="pronun">{{ name.pronunciation }} · /s/{{ name.siteSlug }}</div>
              <p>{{ sanitizeBrandStoryText(name.brandStory) }}</p>
            </div>
          </div>

          <div class="section-title-row">
            <h3 class="sub-heading">
              AI 벡터 SVG 로고 & 브랜드 컬러 팔레트 후보 ({{
                generatingLogos || (generatingNames && logoSuggestions.length === 0)
                  ? '6종 생성 중...'
                  : `${logoSuggestions.length}종`
              }})
            </h3>
            <span class="helper-caption">선택한 브랜드명({{ previewBrandName }})이 모든 로고 프리뷰에 실시간 반영되며, 컬러 팔레트는 4단계 사이트 테마로 자동 적용됩니다.</span>
          </div>
          <div
            v-if="generatingLogos || (generatingNames && logoSuggestions.length === 0)"
            class="logo-grid skeleton-grid"
            aria-busy="true"
          >
            <div v-for="n in 6" :key="'logo-skel-' + n" class="logo-card skeleton-card">
              <div class="svg-stage skel-stage">
                <span class="skel-stage-label">벡터 SVG 로고 렌더링 중...</span>
              </div>
              <div class="logo-meta">
                <div class="skel-bar skel-title"></div>
                <div class="skel-row">
                  <span class="skel-bar skel-swatch"></span>
                  <span class="skel-bar skel-swatch"></span>
                  <span class="skel-bar skel-swatch"></span>
                </div>
              </div>
            </div>
          </div>
          <div v-else-if="logoSuggestions.length === 0" class="empty-step-box">
            <strong>아직 생성된 벡터 SVG 로고 후보가 없습니다.</strong>
            <p>아래 버튼을 눌러 브랜드 컬러 팔레트와 벡터 SVG 로고 6종을 생성해 주세요.</p>
            <button type="button" class="primary-btn" :disabled="busy" @click="regenerateLogos">
              {{ busy ? 'SVG 로고 생성 중...' : '벡터 SVG 로고 6종 생성하기' }}
            </button>
          </div>
          <div v-else class="logo-grid">
            <div
              v-for="logo in logoSuggestions"
              :key="logo.id"
              class="logo-card"
              :class="{ selected: selectedLogoId === logo.id }"
              @click="selectLogoCandidate(logo)"
            >
              <div class="svg-stage" :style="{ background: logo.colorPalette?.surface || '#f8fafc' }">
                <div class="svg-inner" v-html="renderLogoWithBrand(logo.svgCode)"></div>
              </div>
              <div class="logo-meta">
                <div class="logo-meta-top">
                  <strong>{{ logo.styleName }}</strong>
                  <span v-if="selectedLogoId === logo.id" class="selected-pill">선택됨</span>
                </div>
                <div class="swatches" v-if="logo.colorPalette">
                  <span
                    v-for="(hex, key) in logo.colorPalette"
                    :key="key"
                    class="swatch-item"
                  >
                    <i :style="{ background: String(hex) }"></i>
                    {{ hex }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ================================================================ -->
        <!-- 3단계: 기능명세서 & 화면설계 -->
        <!-- ================================================================ -->
        <div
          v-else-if="activeStep === 3"
          class="canvas-panel"
          :class="{ 'is-busy-locked': busy }"
          :inert="busy || undefined"
        >
          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 03 · FUNCTIONAL SPEC & SCREEN BLUEPRINT</span>
              <h2>서비스 기능명세서 및 화면 설계 블루프린트</h2>
              <p>AI가 설계한 핵심 기능 항목과 화면 섹션 블루프린트를 직접 추가·수정·순서 변경하고 4단계 사이트 프로비저닝으로 이동하세요.</p>
            </div>
            <div class="header-btns">
              <button type="button" class="secondary-btn" @click="specEditMode = !specEditMode">
                {{ specEditMode ? '시각적 기획서 카드 보기' : '마크다운 원문 직접 편집' }}
              </button>
              <button type="button" class="secondary-btn" :disabled="busy" @click="saveSpecVersion">
                현재 기획서·블루프린트 저장
              </button>
              <button type="button" class="primary-btn" :disabled="busy" @click="provisionAndGoStep4">
                {{ busy ? '사이트 생성 중...' : '사이트 프로비저닝 및 4단계 프리뷰 →' }}
              </button>
            </div>
          </div>

          <div class="spec-layout">
            <div class="spec-main">
              <!-- 기본: 일반 사용자 친화적 시각 기획 요약 & 핵심 기능 인라인 편집 뷰 -->
              <div v-if="!specEditMode" class="visual-spec-cards">
                <div class="visual-spec-summary">
                  <div class="summary-badge-row">
                    <span class="summary-type-pill">{{ formatSiteTypeLabel(currentIdea?.siteType) }}</span>
                    <span class="summary-slug-pill">예정 주소: /s/{{ activeSiteSlug || 'auto-slug' }}</span>
                  </div>
                  <div class="summary-head-flex">
                    <div>
                      <h3>{{ previewBrandName }} — AI 설계 요약 및 핵심 기능 정의</h3>
                      <p class="summary-prompt">{{ currentIdea?.promptText }}</p>
                    </div>
                    <button type="button" class="tiny-add-btn" @click="addSpecHighlightItem">
                      + 핵심 기능 항목 추가
                    </button>
                  </div>
                  <div class="parsed-spec-lines">
                    <div v-for="(_, idx) in editableHighlights" :key="idx" class="spec-bullet-item editable-bullet-row">
                      <span class="bullet-dot">✓</span>
                      <input
                        v-model="editableHighlights[idx]"
                        type="text"
                        class="bullet-inline-input"
                        placeholder="핵심 서비스 기능 또는 공간 특징 입력"
                        @change="syncHighlightsToMarkdown"
                      />
                      <button
                        type="button"
                        class="tiny-del-btn"
                        title="항목 삭제"
                        @click="removeSpecHighlightItem(idx)"
                      >
                        삭제
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 선택: 마크다운 원문 편집기 -->
              <div v-else class="markdown-editor-wrap">
                <label class="block-label">마크다운 서비스 기능명세서 원문 에디터</label>
                <textarea
                  v-model="editableSpecMarkdown"
                  rows="12"
                  class="spec-textarea"
                  @change="syncMarkdownToHighlights"
                ></textarea>
              </div>

              <!-- 화면 설계 블루프린트 (직접 섹션 추가/삭제/순서변경/컴포넌트 편집 지원) -->
              <div class="blueprint-box">
                <div class="blueprint-head">
                  <div>
                    <strong>화면 설계 블루프린트 직접 편집 ({{ editableScreens.length }}개 핵심 화면 섹션)</strong>
                    <p class="blueprint-sub">각 섹션의 제목, 라우트 경로, 레이아웃 유형, 포함 기능 모듈을 직접 커스텀할 수 있습니다.</p>
                  </div>
                  <div class="header-btns">
                    <button type="button" class="tiny-add-btn" @click="addEditableScreen">
                      + 새 화면 섹션 추가
                    </button>
                    <button
                      type="button"
                      class="secondary-btn"
                      :disabled="busy"
                      @click="regenerateSpec"
                    >
                      {{ busy ? '기획서 생성 중...' : 'AI 기획서·화면설계 재생성' }}
                    </button>
                  </div>
                </div>
                <div v-if="editableScreens.length === 0" class="empty-step-box" style="margin-top: 14px">
                  <strong>아직 생성된 화면 설계 블루프린트가 없습니다.</strong>
                  <p>우측 상단 또는 아래 버튼을 눌러 AI 서비스 기능명세서와 화면 설계 블루프린트를 생성하거나 직접 섹션을 추가해 주세요.</p>
                  <div class="header-btns" style="justify-content: center; margin-top: 8px">
                    <button type="button" class="primary-btn" :disabled="busy" @click="regenerateSpec">
                      {{ busy ? '생성 중...' : 'AI 기능명세서 및 화면설계 생성하기' }}
                    </button>
                    <button type="button" class="secondary-btn" @click="addEditableScreen">
                      + 직접 첫 화면 섹션 추가하기
                    </button>
                  </div>
                </div>
                <div v-else class="screen-list">
                  <div v-for="(scr, idx) in editableScreens" :key="scr.id || scr.screenKey || idx" class="screen-item editable-screen-card">
                    <div class="screen-item-header">
                      <span class="screen-step-num">섹션 0{{ Number(idx) + 1 }}</span>
                      <div class="screen-head-actions">
                        <button
                          type="button"
                          class="order-mini-btn"
                          :disabled="idx === 0"
                          title="위로 이동"
                          @click="moveEditableScreen(idx, -1)"
                        >
                          ↑
                        </button>
                        <button
                          type="button"
                          class="order-mini-btn"
                          :disabled="idx === editableScreens.length - 1"
                          title="아래로 이동"
                          @click="moveEditableScreen(idx, 1)"
                        >
                          ↓
                        </button>
                        <button
                          type="button"
                          class="tiny-del-btn"
                          title="섹션 삭제"
                          @click="removeEditableScreen(idx)"
                        >
                          삭제
                        </button>
                      </div>
                    </div>

                    <div class="screen-edit-row">
                      <input
                        v-model="scr.screenTitle"
                        type="text"
                        class="screen-title-input"
                        placeholder="화면 섹션 제목 (예: 메인 히어로 & 냇가 평상 소개)"
                      />
                      <input
                        v-model="scr.routePath"
                        type="text"
                        class="screen-route-input"
                        placeholder="경로 (예: /main)"
                      />
                    </div>

                    <div class="screen-edit-row">
                      <select v-model="scr.layoutType" class="screen-layout-select">
                        <option value="HERO_BANNER_GRID">메인 히어로 배너 & 시그니처 그리드</option>
                        <option value="PRODUCT_CATALOG_GRID">상품 · 객실 · 메뉴 카탈로그 그리드</option>
                        <option value="GALLERY_SHOWCASE">현장 포토 갤러리 & 공간 아카이브</option>
                        <option value="MAGAZINE_LIST">브랜드 매거진 & 스토리 저널</option>
                        <option value="RESERVATION_FORM">온라인 예약 · 문의 · 신청 폼</option>
                        <option value="FAQ_CONCIERGE">AI 브랜드 컨시어지 & 오시는 길 안내</option>
                        <option v-if="scr.layoutType && !['HERO_BANNER_GRID','PRODUCT_CATALOG_GRID','GALLERY_SHOWCASE','MAGAZINE_LIST','RESERVATION_FORM','FAQ_CONCIERGE'].includes(scr.layoutType)" :value="scr.layoutType">
                          {{ formatLayoutTypeLabel(scr.layoutType) }}
                        </option>
                      </select>
                    </div>

                    <input
                      v-model="scr.description"
                      type="text"
                      class="screen-desc-input"
                      placeholder="이 화면 섹션의 역할과 구성을 입력하세요"
                    />

                    <div class="comp-tags">
                      <span
                        v-for="(c, cIdx) in scr.components || []"
                        :key="String(c) + cIdx"
                        class="comp-chip editable-comp-chip"
                        :title="String(c)"
                      >
                        {{ formatComponentLabel(String(c)) }}
                        <button type="button" class="tag-del-x" @click="scr.components.splice(cIdx, 1)">×</button>
                      </span>
                      <select
                        class="comp-add-select"
                        value=""
                        @change="(e: any) => { addComponentToScreen(scr, e.target.value); e.target.value = ''; }"
                      >
                        <option value="">+ 기능 모듈 추가</option>
                        <option value="HeroBanner">히어로 배너 (HeroBanner)</option>
                        <option value="ProductGrid">상품/객실 진열 (ProductGrid)</option>
                        <option value="PhotoGallery">포토 갤러리 (PhotoGallery)</option>
                        <option value="MagazineSection">매거진 저널 (MagazineSection)</option>
                        <option value="BookingForm">온라인 예약/신청폼 (BookingForm)</option>
                        <option value="BrochureDownload">안내문/요금표 자료실 (BrochureDownload)</option>
                        <option value="AiConciergeWidget">AI 컨시어지 챗봇 (AiConciergeWidget)</option>
                        <option value="WelcomeCouponBar">웰컴 혜택 쿠폰바 (WelcomeCouponBar)</option>
                      </select>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="version-sidebar">
              <label class="block-label">기획서 버전 스냅샷 이력</label>
              <div class="version-cards">
                <div v-for="ver in ideaVersions" :key="ver.id" class="version-card">
                  <div class="ver-top">
                    <span class="ver-no">v{{ ver.versionNo }}</span>
                    <span class="ver-user">{{ ver.createdBy }}</span>
                  </div>
                  <div class="ver-title">{{ ver.title }}</div>
                  <button type="button" class="rollback-btn" @click="rollbackSpecVersion(ver.versionNo)">
                    v{{ ver.versionNo }} 버전으로 복원
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ================================================================ -->
        <!-- 4단계: 실시간 프리뷰 편집 (AI 대화 + 비주얼 폼 편집 + 상품/매거진 관리) -->
        <!-- ================================================================ -->
        <div
          v-else-if="activeStep === 4"
          class="canvas-panel"
          :class="{ 'is-busy-locked': busy }"
          :inert="busy || undefined"
        >
          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 04 · CONVERSATIONAL & VISUAL LIVE STUDIO</span>
              <h2>실시간 디자인·문구·상품 편집 및 스토어 프리뷰</h2>
              <p>AI 채팅 지시 또는 직관적인 비주얼 편집기로 배너 문구, 컬러 테마, 진열 상품과 매거진을 즉시 수정하세요.</p>
            </div>
            <div class="header-btns">
              <div class="viewport-btns">
                <button type="button" :class="{ active: viewportMode === 'DESKTOP' }" @click="viewportMode = 'DESKTOP'">
                  PC (100%)
                </button>
                <button type="button" :class="{ active: viewportMode === 'TABLET' }" @click="viewportMode = 'TABLET'">
                  태블릿 (768px)
                </button>
                <button type="button" :class="{ active: viewportMode === 'MOBILE' }" @click="viewportMode = 'MOBILE'">
                  모바일 (390px)
                </button>
              </div>
              <button type="button" class="secondary-btn" @click="goToStep(5)">
                5단계: 코드 편집 (선택)
              </button>
              <button type="button" class="primary-btn" @click="goToStep(6)">
                6단계: 바로 빌드 & 배포 →
              </button>
            </div>
          </div>

          <div class="preview-split" v-if="siteRuntime">
            <!-- 좌측 멀티 모드 컨트롤러 (AI 대화 / 배너·컬러 직접 편집 / 상품·매거진 관리) -->
            <div class="chat-panel">
              <div class="status-box">
                <div class="status-row">
                  <span>라이브 주소</span>
                  <code>/s/{{ siteRuntime.siteSlug }}</code>
                </div>
                <div class="status-row">
                  <span>사이트 유형</span>
                  <strong>{{ formatSiteTypeLabel(siteRuntime.siteType) }}</strong>
                </div>
                <div class="status-row">
                  <span>현재 버전</span>
                  <strong>v{{ siteRuntime.activeVersion }} ({{ formatDeployModeLabel(siteRuntime.deployMode) }})</strong>
                </div>
              </div>

              <div class="editor-mode-tabs">
                <button
                  type="button"
                  :class="{ active: step4Tab === 'AI_CHAT' }"
                  @click="step4Tab = 'AI_CHAT'"
                >
                  AI 대화
                </button>
                <button
                  type="button"
                  :class="{ active: step4Tab === 'VISUAL_THEME' }"
                  @click="step4Tab = 'VISUAL_THEME'"
                >
                  배너·컬러
                </button>
                <button
                  type="button"
                  :class="{ active: step4Tab === 'CATALOG_EDIT' }"
                  @click="step4Tab = 'CATALOG_EDIT'"
                >
                  상품·매거진
                </button>
                <button
                  type="button"
                  :class="{ active: step4Tab === 'FORM_EDIT' }"
                  @click="step4Tab = 'FORM_EDIT'"
                >
                  예약·상담 폼
                </button>
              </div>

              <!-- 탭 1: AI 자연어 대화형 수정 -->
              <div v-if="step4Tab === 'AI_CHAT'" class="tab-pane">
                <label class="block-label">자연어 디자인·문구 수정 지시</label>
                <textarea
                  v-model="chatInstruction"
                  rows="3"
                  placeholder="예: 웜 앰버 크래프트 테마로 컬러를 바꾸고 히어로 배너 문구를 더 감성적으로 다듬어줘"
                ></textarea>
                <button type="button" class="primary-btn full" :disabled="busy" @click="applyChatRefine">
                  {{ busy ? '실시간 반영 중...' : '대화형 수정 즉시 반영 (Hot-Reload)' }}
                </button>

                <div class="preset-group">
                  <span class="preset-title">원클릭 컬러 테마 프리셋</span>
                  <button type="button" @click="applyQuickTheme('보태니컬 포레스트 그린 테마로 변경해줘')">
                    보태니컬 포레스트 그린 테마
                  </button>
                  <button type="button" @click="applyQuickTheme('다크 인디고 테크 테마로 변경해줘')">
                    다크 인디고 테크 테마
                  </button>
                  <button type="button" @click="applyQuickTheme('웜 앰버 크래프트 테마로 변경해줘')">
                    웜 앰버 크래프트 테마
                  </button>
                  <button type="button" @click="applyQuickTheme('코랄 로즈 부티크 테마로 변경해줘')">
                    코랄 로즈 부티크 테마
                  </button>
                </div>

                <div class="preset-group">
                  <span class="preset-title">원클릭 배너 카피 & 레이아웃 지시</span>
                  <button type="button" @click="applyQuickTheme('히어로 배너 문구를 더 감성적인 에디토리얼 톤으로 변경해줘')">
                    감성 에디토리얼 헤드라인으로 다듬기
                  </button>
                  <button type="button" @click="applyQuickTheme('그랜드 런칭 스페셜 프로모션 배너 문구로 변경해줘')">
                    런칭 프로모션 배너 문구로 변경
                  </button>
                </div>
              </div>

              <!-- 탭 2: 배너·브랜드·컬러·노출 섹션 직접 편집 -->
              <div v-else-if="step4Tab === 'VISUAL_THEME'" class="tab-pane">
                <div class="mini-field">
                  <label>상단 GNB 사이트 브랜드명 (변경 시 로고 텍스트도 자동 연동)</label>
                  <input v-model="visualForm.siteName" type="text" placeholder="예: 냇가식당, 청류민박" />
                </div>
                <div class="mini-field">
                  <label>브랜드 한줄 슬로건</label>
                  <input v-model="visualForm.brandSlogan" type="text" placeholder="브랜드 핵심 슬로건" />
                </div>

                <!-- 메인 히어로 배너 사진 업로드 & 보관함 선택 -->
                <div class="mini-field hero-media-box">
                  <div class="hero-media-head">
                    <label>메인 히어로 배너 배경 사진</label>
                    <div class="hero-media-btns">
                      <input
                        ref="step4HeroInputRef"
                        type="file"
                        accept="image/*"
                        class="hidden-file-input"
                        @change="onStep4HeroFileChange"
                      />
                      <button
                        type="button"
                        class="tiny-add-btn"
                        :disabled="busy || assetUploading"
                        @click="triggerStep4HeroUpload"
                      >
                        {{ assetUploading ? '업로드 중...' : '+ 배너 사진 업로드' }}
                      </button>
                      <button
                        v-if="visualForm.heroBannerImage"
                        type="button"
                        class="tiny-del-btn"
                        @click="visualForm.heroBannerImage = ''"
                      >
                        사진 제거
                      </button>
                    </div>
                  </div>
                  <div v-if="visualForm.heroBannerImage" class="hero-media-preview">
                    <img :src="visualForm.heroBannerImage" alt="Hero Banner Preview" />
                    <input
                      v-model="visualForm.heroBannerCaption"
                      type="text"
                      placeholder="배너 사진 캡션 (선택)"
                    />
                  </div>
                  <input
                    v-model="visualForm.heroBannerImage"
                    type="text"
                    placeholder="배너 이미지 URL 직접 입력 또는 아래 보관함에서 선택"
                  />
                  <select
                    v-if="imageAssets.length > 0"
                    class="asset-picker-select"
                    :value="visualForm.heroBannerImage"
                    @change="(e: any) => onPickHeroAsset(e.target.value)"
                  >
                    <option value="">-- 업로드된 사진 보관함에서 배너 사진 선택 ({{ imageAssets.length }}장) --</option>
                    <option v-for="img in imageAssets" :key="img.id" :value="img.fileUrl">
                      [{{ formatAssetTypeLabel(img.assetType) }}] {{ img.caption || img.originalName }}
                    </option>
                  </select>
                </div>

                <div class="mini-field">
                  <label>상단 포인트 배지 문구</label>
                  <input v-model="visualForm.customBadge" type="text" placeholder="예: 공식 브랜드 쇼케이스" />
                </div>
                <div class="mini-field">
                  <label>메인 히어로 헤드라인</label>
                  <input v-model="visualForm.heroTitle" type="text" />
                </div>
                <div class="mini-field">
                  <label>서브 슬로건 설명 문구</label>
                  <textarea v-model="visualForm.heroSubtitle" rows="2"></textarea>
                </div>
                <div class="mini-field">
                  <label>메인 행동 유도(CTA) 버튼 문구</label>
                  <input v-model="visualForm.ctaText" type="text" />
                </div>

                <div class="color-picker-row">
                  <div class="color-field">
                    <label>메인 브랜드 컬러</label>
                    <div class="color-input-wrap">
                      <input v-model="visualForm.primaryColor" type="color" />
                      <span>{{ visualForm.primaryColor }}</span>
                    </div>
                  </div>
                  <div class="color-field">
                    <label>보조 그라디언트 컬러</label>
                    <div class="color-input-wrap">
                      <input v-model="visualForm.secondaryColor" type="color" />
                      <span>{{ visualForm.secondaryColor }}</span>
                    </div>
                  </div>
                </div>

                <div class="color-picker-row">
                  <div class="color-field">
                    <label>포인트 액센트 컬러</label>
                    <div class="color-input-wrap">
                      <input v-model="visualForm.accentColor" type="color" />
                      <span>{{ visualForm.accentColor }}</span>
                    </div>
                  </div>
                  <div class="color-field">
                    <label>배경 서피스 컬러</label>
                    <div class="color-input-wrap">
                      <input v-model="visualForm.surfaceColor" type="color" />
                      <span>{{ visualForm.surfaceColor }}</span>
                    </div>
                  </div>
                </div>

                <div class="mini-field">
                  <label>카드·버튼 모서리 곡률 (Radius)</label>
                  <select v-model="visualForm.radius">
                    <option value="6px">샤프 모던 (6px)</option>
                    <option value="12px">스탠다드 라운드 (12px)</option>
                    <option value="16px">소프트 감성 라운드 (16px)</option>
                    <option value="24px">풀 라운드 (24px)</option>
                  </select>
                </div>

                <div class="toggle-group">
                  <label class="toggle-row">
                    <input v-model="visualForm.showFlashDeal" type="checkbox" />
                    <span>한정 수량/웰컴 쿠폰 배너 표시</span>
                  </label>
                  <label class="toggle-row">
                    <input v-model="visualForm.showPhotoGallery" type="checkbox" />
                    <span>현장 포토 갤러리 섹션 표시</span>
                  </label>
                  <label class="toggle-row">
                    <input v-model="visualForm.showCmsArticles" type="checkbox" />
                    <span>브랜드 매거진 & 아티클 섹션 표시</span>
                  </label>
                  <label class="toggle-row">
                    <input v-model="visualForm.showCmsForm" type="checkbox" />
                    <span>온라인 참가·예약 신청 폼 표시</span>
                  </label>
                  <label class="toggle-row">
                    <input v-model="visualForm.showAiConcierge" type="checkbox" />
                    <span>AI 브랜드 컨시어지 상담창 표시</span>
                  </label>
                </div>

                <button type="button" class="primary-btn full" :disabled="busy" @click="saveVisualThemeOverrides">
                  {{ busy ? '적용 중...' : '배너·브랜드·컬러·섹션 설정 즉시 적용' }}
                </button>
              </div>

              <!-- 탭 3: 진열 상품 & 매거진 아티클 & 갤러리/첨부문서 직접 편집 -->
              <div v-else-if="step4Tab === 'CATALOG_EDIT'" class="tab-pane catalog-edit-pane">
                <input
                  ref="step4CatalogImageInputRef"
                  type="file"
                  accept="image/*"
                  class="hidden-file-input"
                  @change="onStep4CatalogImageFileChange"
                />
                <input
                  ref="step4ExtraAssetInputRef"
                  type="file"
                  multiple
                  accept="image/*,.pdf,.doc,.docx,.txt,.md,.csv"
                  class="hidden-file-input"
                  @change="onStep4ExtraAssetFileChange"
                />

                <div class="catalog-edit-header">
                  <strong>진열 상품·객실·메뉴 직접 편집 ({{ editableProducts.length }}종)</strong>
                  <button type="button" class="tiny-add-btn" @click="addEditableProduct">+ 상품 추가</button>
                </div>
                <div class="editable-item-list">
                  <div v-for="(prod, pIdx) in editableProducts" :key="prod.id || pIdx" class="editable-item-card">
                    <div class="editable-card-top">
                      <span>상품 #{{ pIdx + 1 }}</span>
                      <div class="screen-head-actions">
                        <button
                          type="button"
                          class="order-mini-btn"
                          :disabled="pIdx === 0"
                          @click="moveEditableProduct(pIdx, -1)"
                        >
                          ↑
                        </button>
                        <button
                          type="button"
                          class="order-mini-btn"
                          :disabled="pIdx === editableProducts.length - 1"
                          @click="moveEditableProduct(pIdx, 1)"
                        >
                          ↓
                        </button>
                        <button type="button" class="tiny-del-btn" @click="editableProducts.splice(pIdx, 1)">삭제</button>
                      </div>
                    </div>
                    <div class="item-media-row">
                      <img v-if="prod.pic" :src="prod.pic" :alt="prod.name" class="item-mini-thumb" />
                      <div class="item-media-controls">
                        <button
                          type="button"
                          class="tiny-add-btn"
                          :disabled="assetUploading"
                          @click="triggerCatalogItemImageUpload('PRODUCT', pIdx)"
                        >
                          + 상품 사진 업로드
                        </button>
                        <select
                          v-if="imageAssets.length > 0"
                          class="asset-picker-select"
                          :value="prod.pic"
                          @change="(e: any) => { if (e.target.value) prod.pic = e.target.value; }"
                        >
                          <option value="">보관함 사진에서 선택 ({{ imageAssets.length }})</option>
                          <option v-for="img in imageAssets" :key="img.id" :value="img.fileUrl">
                            {{ img.caption || img.originalName }}
                          </option>
                        </select>
                      </div>
                    </div>
                    <input v-model="prod.pic" type="text" placeholder="상품 이미지 URL 직접 입력 (또는 위에서 업로드)" />
                    <div class="editable-price-row">
                      <input v-model="prod.name" type="text" placeholder="상품/객실/메뉴명" />
                      <input v-model="prod.productCategoryName" type="text" placeholder="카테고리 (예: 독채객실, 시그니처메뉴)" />
                    </div>
                    <div class="editable-price-row">
                      <input v-model.number="prod.promotionPrice" type="number" placeholder="판매가(원)" title="실제 판매가(원)" />
                      <input v-model.number="prod.price" type="number" placeholder="정상가(원)" title="정상가(원)" />
                    </div>
                    <div class="editable-price-row">
                      <input v-model="prod.unit" type="text" placeholder="단위 (예: 1박, 1인, 1세트)" />
                      <input v-model.number="prod.stock" type="number" placeholder="일일 재고/객실수" />
                    </div>
                    <input v-model="prod.subTitle" type="text" placeholder="상품 한줄 매력 포인트 및 포함 혜택 설명" />
                  </div>
                </div>

                <div class="catalog-edit-header" style="margin-top: 10px">
                  <strong>매거진 아티클 편집 ({{ editableArticles.length }}건)</strong>
                  <button type="button" class="tiny-add-btn" @click="addEditableArticle">+ 아티클 추가</button>
                </div>
                <div class="editable-item-list">
                  <div v-for="(art, aIdx) in editableArticles" :key="art.id || aIdx" class="editable-item-card">
                    <div class="editable-card-top">
                      <span>아티클 #{{ aIdx + 1 }}</span>
                      <div class="screen-head-actions">
                        <button
                          type="button"
                          class="order-mini-btn"
                          :disabled="aIdx === 0"
                          @click="moveEditableArticle(aIdx, -1)"
                        >
                          ↑
                        </button>
                        <button
                          type="button"
                          class="order-mini-btn"
                          :disabled="aIdx === editableArticles.length - 1"
                          @click="moveEditableArticle(aIdx, 1)"
                        >
                          ↓
                        </button>
                        <button type="button" class="tiny-del-btn" @click="editableArticles.splice(aIdx, 1)">삭제</button>
                      </div>
                    </div>
                    <div class="item-media-row">
                      <img v-if="art.cover" :src="art.cover" :alt="art.title" class="item-mini-thumb" />
                      <div class="item-media-controls">
                        <button
                          type="button"
                          class="tiny-add-btn"
                          :disabled="assetUploading"
                          @click="triggerCatalogItemImageUpload('ARTICLE', aIdx)"
                        >
                          + 커버 사진 업로드
                        </button>
                        <select
                          v-if="imageAssets.length > 0"
                          class="asset-picker-select"
                          :value="art.cover"
                          @change="(e: any) => { if (e.target.value) art.cover = e.target.value; }"
                        >
                          <option value="">보관함 사진에서 선택 ({{ imageAssets.length }})</option>
                          <option v-for="img in imageAssets" :key="img.id" :value="img.fileUrl">
                            {{ img.caption || img.originalName }}
                          </option>
                        </select>
                      </div>
                    </div>
                    <input v-model="art.cover" type="text" placeholder="커버 이미지 URL 직접 입력" />
                    <input v-model="art.title" type="text" placeholder="매거진 아티클 제목" />
                    <div class="editable-price-row">
                      <input v-model="art.categoryName" type="text" placeholder="카테고리 (예: 공간 이야기, 계절 소식)" />
                      <input v-model="art.author" type="text" placeholder="작성자 (예: 호스트, 에디터)" />
                    </div>
                    <input v-model="art.description" type="text" placeholder="목록 노출용 한줄 요약 설명" />
                    <textarea v-model="art.bodyHtml" rows="2" placeholder="아티클 상세 본문 내용"></textarea>
                  </div>
                </div>

                <div class="catalog-edit-header" style="margin-top: 10px">
                  <strong>포토 갤러리({{ previewGalleryImages.length }}장) & 참고문서({{ previewDownloadableDocs.length }}건)</strong>
                  <button
                    type="button"
                    class="tiny-add-btn"
                    :disabled="assetUploading"
                    @click="triggerStep4ExtraAssetUpload"
                  >
                    + 갤러리·자료 파일 추가
                  </button>
                </div>

                <button type="button" class="primary-btn full" :disabled="busy" @click="saveCatalogAndArticlesOverrides">
                  {{ busy ? '저장 중...' : '상품·매거진 상세 변경사항 즉시 반영' }}
                </button>
              </div>

              <!-- 탭 4: 온라인 예약·신청 폼 & AI 컨시어지 직접 편집 -->
              <div v-else-if="step4Tab === 'FORM_EDIT'" class="tab-pane">
                <div class="mini-field">
                  <label>온라인 예약·신청 폼 제목</label>
                  <input
                    v-model="editableCmsForm.title"
                    type="text"
                    placeholder="예: 냇가 평상 & 독채 민박 사전 예약 신청"
                  />
                </div>
                <div class="mini-field">
                  <label>예약·신청 폼 상세 안내 문구</label>
                  <textarea
                    v-model="editableCmsForm.description"
                    rows="3"
                    placeholder="예: 원하시는 방문 일자와 인원수를 남겨주시면 호스트가 확인 후 안내 문자를 드립니다."
                  ></textarea>
                </div>
                <div class="mini-field">
                  <label>신청 제출 버튼 문구</label>
                  <input
                    v-model="editableCmsForm.submitText"
                    type="text"
                    placeholder="예: 온라인 예약·문의 접수하기"
                  />
                </div>
                <div class="mini-field">
                  <label>AI 브랜드 컨시어지 환영 인사말</label>
                  <textarea
                    v-model="editableCmsForm.conciergeGreeting"
                    rows="2"
                    placeholder="예: 안녕하세요! 객실 요금, 평상 예약, 계절 메뉴, 오시는 길 등 궁금한 점을 물어보세요."
                  ></textarea>
                </div>

                <button type="button" class="primary-btn full" :disabled="busy" @click="saveFormOverrides">
                  {{ busy ? '저장 중...' : '예약 폼·컨시어지 설정 즉시 반영' }}
                </button>
              </div>
            </div>

            <!-- 우측 실제 스토어프론트 수준 WYSIWYG 라이브 프리뷰 (클릭 시 좌측 편집 탭 포커스) -->
            <div class="preview-stage">
              <div class="preview-click-tip">
                💡 프리뷰의 각 영역(배너, 상품, 매거진, 예약 폼)을 클릭하면 좌측 편집기가 해당 탭으로 자동 전환됩니다.
              </div>
              <div
                class="preview-frame"
                :class="viewportMode.toLowerCase()"
                :style="{ background: visualForm.surfaceColor || siteRuntime.designTokens?.surface || '#f8fafc' }"
              >
                <!-- 프리뷰 상단 브랜드 GNB -->
                <div class="preview-gnb clickable-preview-sec" @click="focusStep4Editor('VISUAL_THEME')">
                  <div class="preview-gnb-left">
                    <div
                      class="frame-logo"
                      v-html="renderLogoWithBrand(siteRuntime.logoSvg, visualForm.siteName || siteRuntime.siteName)"
                    ></div>
                    <strong class="gnb-brand-title">{{ visualForm.siteName || siteRuntime.siteName }}</strong>
                  </div>
                  <div class="preview-gnb-links">
                    <span v-if="previewProducts.length">스토어 ({{ previewProducts.length }})</span>
                    <span v-if="visualForm.showPhotoGallery && previewGalleryImages.length">포토갤러리 ({{ previewGalleryImages.length }})</span>
                    <span v-if="visualForm.showCmsArticles && previewArticles.length">매거진 ({{ previewArticles.length }})</span>
                    <span v-if="previewDownloadableDocs.length">안내자료 ({{ previewDownloadableDocs.length }})</span>
                    <span v-if="visualForm.showCmsForm">예약/신청</span>
                    <span class="gnb-live-pill">/s/{{ siteRuntime.siteSlug }}</span>
                  </div>
                </div>

                <!-- 프리뷰 메인 히어로 배너 -->
                <div
                  class="frame-hero clickable-preview-sec"
                  :style="previewHeroStyle"
                  @click="focusStep4Editor('VISUAL_THEME')"
                >
                  <div class="frame-top">
                    <span class="frame-badge">{{ visualForm.customBadge || siteRuntime.layoutSchema?.customBadge || '공식 브랜드 쇼케이스' }}</span>
                    <span v-if="visualForm.showFlashDeal" class="flash-pill">첫 구매·방문 웰컴 혜택 적용 중</span>
                  </div>
                  <h3>{{ visualForm.heroTitle || siteRuntime.layoutSchema?.heroTitle || siteRuntime.siteName }}</h3>
                  <p>{{ visualForm.heroSubtitle || siteRuntime.layoutSchema?.heroSubtitle || siteRuntime.brandSlogan }}</p>
                  <div class="hero-cta-row">
                    <span class="hero-cta-btn" :style="{ borderRadius: visualForm.radius || '12px' }">
                      {{ visualForm.ctaText || siteRuntime.layoutSchema?.ctaText || '시그니처 컬렉션 살펴보기' }} →
                    </span>
                    <span v-if="visualForm.showAiConcierge" class="hero-concierge-pill">
                      AI 브랜드 컨시어지 상시 대기
                    </span>
                  </div>
                </div>

                <!-- 프리뷰 시그니처 상품 카탈로그 섹션 -->
                <div
                  v-if="previewProducts.length"
                  class="frame-section clickable-preview-sec"
                  @click="focusStep4Editor('CATALOG_EDIT')"
                >
                  <div class="frame-sec-head">
                    <div>
                      <span class="sec-kicker">SIGNATURE SHOP CATALOG · 클릭하여 편집</span>
                      <h4>시그니처 상품 카탈로그 ({{ previewProducts.length }}종)</h4>
                    </div>
                    <div class="cat-pills">
                      <span class="cat-pill active">전체</span>
                      <span
                        v-for="cat in previewCategories.slice(0, 3)"
                        :key="cat.id || cat.name"
                        class="cat-pill"
                      >
                        {{ cat.name }}
                      </span>
                    </div>
                  </div>
                  <div class="mini-cards">
                    <div
                      v-for="p in previewProducts.slice(0, 4)"
                      :key="p.id"
                      class="mini-card"
                      :style="{ borderRadius: visualForm.radius || '12px' }"
                    >
                      <div class="mini-card-img-wrap">
                        <img v-if="p.pic" :src="p.pic" :alt="p.name" />
                        <span v-if="p.productCategoryName" class="prod-cat-tag">{{ p.productCategoryName }}</span>
                      </div>
                      <div class="mini-card-body">
                        <strong>{{ p.name }}</strong>
                        <p class="prod-sub">{{ p.subTitle }}</p>
                        <div class="prod-price-row">
                          <span class="prod-price" :style="{ color: visualForm.primaryColor || siteRuntime.designTokens?.primary || '#0f172a' }">
                            {{ Number(p.promotionPrice > 0 ? p.promotionPrice : (p.price > 0 ? p.price : 120000)).toLocaleString() }}원
                            <small v-if="p.unit" style="font-size: 10px; color: #64748b">/ {{ p.unit }}</small>
                          </span>
                          <span class="prod-buy-chip">바로 예약·구매</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 프리뷰 시즌·인원별 요금표 매트릭스 (SDUI MATRIX_TABLE) -->
                <div
                  v-if="previewFeatureBlocks.pricingMatrix?.rows?.length"
                  class="frame-section clickable-preview-sec"
                  @click="focusStep4Editor('CATALOG_EDIT')"
                >
                  <div class="frame-sec-head">
                    <div>
                      <span class="sec-kicker">SDUI MATRIX_TABLE · SHOP_PRICING_MATRIX</span>
                      <h4>{{ previewFeatureBlocks.pricingMatrix.title || '시즌 · 요일 · 인원별 상세 요금표' }}</h4>
                    </div>
                  </div>
                  <div class="mini-pricing-table-wrap">
                    <table class="mini-pricing-table">
                      <thead>
                        <tr>
                          <th v-for="col in previewFeatureBlocks.pricingMatrix.columns || []" :key="col">{{ col }}</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(row, rIdx) in previewFeatureBlocks.pricingMatrix.rows" :key="rIdx">
                          <td><strong>{{ row.tier }}</strong></td>
                          <td>{{ row.capacity }}</td>
                          <td>{{ row.offPeakWeekday }}</td>
                          <td>{{ row.offPeakWeekend }}</td>
                          <td>{{ row.peakSeason }}</td>
                          <td>{{ row.note }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>

                <!-- 프리뷰 공간 구역별 스페이스 투어 (SDUI SPACE_TOUR) -->
                <div
                  v-if="previewFeatureBlocks.spaceTour?.length"
                  class="frame-section clickable-preview-sec"
                  @click="focusStep4Editor('CATALOG_EDIT')"
                >
                  <div class="frame-sec-head">
                    <div>
                      <span class="sec-kicker">SDUI SPACE_TOUR · CMS_SPACE_TOUR</span>
                      <h4>공간 구역별 스페이스 투어 ({{ previewFeatureBlocks.spaceTour.length }}개 존)</h4>
                    </div>
                  </div>
                  <div class="mini-space-tour-grid">
                    <div
                      v-for="(zone, zIdx) in previewFeatureBlocks.spaceTour"
                      :key="zIdx"
                      class="mini-space-zone-card"
                    >
                      <span class="zone-badge">{{ zone.zone }}</span>
                      <strong>{{ zone.title }}</strong>
                      <p>{{ zone.desc }}</p>
                    </div>
                  </div>
                </div>

                <!-- 프리뷰 현장 포토 갤러리 아카이브 섹션 -->
                <div
                  v-if="visualForm.showPhotoGallery && previewGalleryImages.length"
                  class="frame-section clickable-preview-sec"
                  @click="focusStep4Editor('CATALOG_EDIT')"
                >
                  <div class="frame-sec-head">
                    <div>
                      <span class="sec-kicker">SPACE & PHOTO GALLERY ARCHIVE · 클릭하여 편집</span>
                      <h4>현장 포토 갤러리 ({{ previewGalleryImages.length }}장)</h4>
                    </div>
                  </div>
                  <div class="mini-gallery-grid">
                    <div
                      v-for="(gImg, gIdx) in previewGalleryImages.slice(0, 6)"
                      :key="gImg.id || gIdx"
                      class="mini-gallery-card"
                    >
                      <img :src="gImg.fileUrl" :alt="gImg.caption || gImg.originalName" />
                      <span>{{ gImg.caption || gImg.originalName }}</span>
                    </div>
                  </div>
                </div>

                <!-- 프리뷰 단계별 이용 코스 가이드 (SDUI TIMELINE_STEPS) -->
                <div
                  v-if="previewFeatureBlocks.stepGuide?.length"
                  class="frame-section clickable-preview-sec"
                  @click="focusStep4Editor('CATALOG_EDIT')"
                >
                  <div class="frame-sec-head">
                    <div>
                      <span class="sec-kicker">SDUI TIMELINE_STEPS · CMS_STEP_GUIDE</span>
                      <h4>단계별 이용 & 추천 코스 가이드</h4>
                    </div>
                  </div>
                  <div class="mini-step-guide-grid">
                    <div
                      v-for="(st, sIdx) in previewFeatureBlocks.stepGuide"
                      :key="sIdx"
                      class="mini-step-card"
                    >
                      <span class="step-num-pill">{{ st.step }}</span>
                      <strong>{{ st.title }}</strong>
                      <p>{{ st.desc }}</p>
                    </div>
                  </div>
                </div>

                <!-- 프리뷰 브랜드 매거진 & 커뮤니티 아티클 섹션 -->
                <div
                  v-if="visualForm.showCmsArticles && previewArticles.length"
                  class="frame-section clickable-preview-sec"
                  @click="focusStep4Editor('CATALOG_EDIT')"
                >
                  <div class="frame-sec-head">
                    <div>
                      <span class="sec-kicker">EDITORIAL ARCHIVE & JOURNAL · 클릭하여 편집</span>
                      <h4>브랜드 매거진 & 커뮤니티 소식 ({{ previewArticles.length }}건)</h4>
                    </div>
                  </div>
                  <div class="mini-articles">
                    <div v-for="a in previewArticles.slice(0, 2)" :key="a.id" class="mini-article">
                      <img v-if="a.cover" :src="a.cover" :alt="a.title" class="mini-art-thumb" />
                      <div class="mini-art-content">
                        <span class="art-badge">{{ a.categoryName || '브랜드 저널' }}</span>
                        <strong>{{ a.title }}</strong>
                        <p>{{ a.description || a.summary }}</p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 프리뷰 브랜드 안내자료 & 첨부 문서 다운로드 섹션 -->
                <div
                  v-if="previewDownloadableDocs.length"
                  class="frame-section clickable-preview-sec"
                  @click="focusStep4Editor('CATALOG_EDIT')"
                >
                  <div class="frame-sec-head">
                    <div>
                      <span class="sec-kicker">OFFICIAL BROCHURE & GUIDE DOCUMENTS</span>
                      <h4>안내자료 & 요금표 다운로드 ({{ previewDownloadableDocs.length }}건)</h4>
                    </div>
                  </div>
                  <div class="mini-doc-list">
                    <a
                      v-for="(doc, dIdx) in previewDownloadableDocs"
                      :key="doc.id || dIdx"
                      :href="doc.downloadUrl || doc.fileUrl"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="mini-doc-item"
                      @click.stop
                    >
                      <div class="mini-doc-left">
                        <span class="mini-doc-ext">{{ (doc.fileExt || 'DOC').toUpperCase() }}</span>
                        <div>
                          <strong>{{ doc.caption || doc.originalName }}</strong>
                          <small>{{ doc.originalName }} · {{ formatFileSize(doc.fileSize) }}</small>
                        </div>
                      </div>
                      <span class="mini-doc-dl">다운로드 ↓</span>
                    </a>
                  </div>
                </div>

                <!-- 프리뷰 FAQ & 오시는 길 요약 (SDUI FAQ_ACCORDION & LOCATION_COURSE) -->
                <div
                  v-if="previewFeatureBlocks.faqItems?.length || previewFeatureBlocks.locationGuide"
                  class="frame-section clickable-preview-sec"
                  @click="focusStep4Editor('FORM_EDIT')"
                >
                  <div class="frame-sec-head">
                    <div>
                      <span class="sec-kicker">SDUI FAQ_ACCORDION & LOCATION_COURSE</span>
                      <h4>자주 묻는 질문 ({{ (previewFeatureBlocks.faqItems || []).length }}건) & 오시는 길 · 주변 추천 코스</h4>
                    </div>
                  </div>
                  <div class="mini-faq-loc-grid">
                    <div v-if="previewFeatureBlocks.faqItems?.length" class="mini-faq-box">
                      <div
                        v-for="(fq, fIdx) in previewFeatureBlocks.faqItems.slice(0, 3)"
                        :key="fIdx"
                        class="mini-faq-item"
                      >
                        <strong>Q. {{ fq.q }}</strong>
                        <p>A. {{ fq.a }}</p>
                      </div>
                    </div>
                    <div v-if="previewFeatureBlocks.locationGuide" class="mini-loc-box">
                      <strong>📍 {{ previewFeatureBlocks.locationGuide.address }}</strong>
                      <p>{{ previewFeatureBlocks.locationGuide.parking }}</p>
                      <p>{{ previewFeatureBlocks.locationGuide.publicTransit }}</p>
                    </div>
                  </div>
                </div>

                <!-- 프리뷰 온라인 클래스/세션 신청 폼 & AI 컨시어지 바 -->
                <div
                  v-if="visualForm.showCmsForm && (editableCmsForm.title || siteRuntime.cmsForm)"
                  class="frame-section frame-form-preview clickable-preview-sec"
                  @click="focusStep4Editor('FORM_EDIT')"
                >
                  <div class="form-preview-bar">
                    <div>
                      <span class="sec-kicker">ONLINE BOOKING & INQUIRY · 클릭하여 폼 문구 편집</span>
                      <strong>{{ editableCmsForm.title || siteRuntime.cmsForm?.title || siteRuntime.cmsForm?.formName }}</strong>
                      <p>{{ editableCmsForm.description || siteRuntime.cmsForm?.description }}</p>
                    </div>
                    <span
                      class="form-preview-cta"
                      :style="{ background: visualForm.primaryColor || siteRuntime.designTokens?.primary || '#0f172a' }"
                    >
                      {{ editableCmsForm.submitText || '온라인 신청 접수 활성 ✓' }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-step-box">
            <strong>아직 프로비저닝된 라이브 사이트 런타임이 없습니다.</strong>
            <p>3단계에서 사이트 프로비저닝을 먼저 실행하거나, 아래 버튼을 눌러 라이브 사이트를 생성해 주세요.</p>
            <button type="button" class="primary-btn" :disabled="busy" @click="provisionAndGoStep4">
              {{ busy ? '프로비저닝 중...' : '라이브 멀티사이트 프로비저닝 실행 →' }}
            </button>
          </div>
        </div>

        <!-- ================================================================ -->
        <!-- 5단계: WebIDE 코드 편집 (고급 선택 과정) -->
        <!-- ================================================================ -->
        <div
          v-else-if="activeStep === 5"
          class="canvas-panel"
          :class="{ 'is-busy-locked': busy }"
          :inert="busy || undefined"
        >
          <div class="non-dev-banner">
            <div>
              <strong>일반 창업자·마케터 안내: 코드 편집 없이 바로 6단계로 이동하실 수 있습니다.</strong>
              <p>4단계에서 설정한 디자인·상품·매거진·예약폼 구성만으로도 완성도 높은 라이브 사이트가 배포됩니다. 이 단계는 프론트엔드 개발자가 Vue 3 코드를 직접 커스텀하거나 AI 코드 자가치유를 실행할 때만 사용하세요.</p>
            </div>
            <button type="button" class="primary-btn" @click="goToStep(6)">
              코드 수정 건너뛰고 6단계(빌드·배포)로 이동 →
            </button>
          </div>

          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 05 · WEBIDE CODE STUDIO & AI SELF-HEALING (OPTIONAL)</span>
              <h2>고급 모드 WebIDE 코드 편집기 & AI 빌드 오류 자동 패치</h2>
              <p>Vue 3 컴포넌트와 테마 JSON 코드를 직접 커스텀·파일 추가하고, 빌드 오류 발생 시 AI로 즉시 복구합니다.</p>
            </div>
            <div class="header-btns">
              <button type="button" class="secondary-btn" :disabled="busy" @click="syncStep4ToWebIde">
                4단계 비주얼 설정 → 코드로 재동기화
              </button>
              <input
                v-model="webIdeCommitMsg"
                type="text"
                class="hint-input"
                placeholder="빌드 커밋 메모 (선택)"
              />
              <button type="button" class="secondary-btn" :disabled="busy" @click="saveAndBuildWebIde()">
                코드 저장 & 번들 빌드
              </button>
              <button
                v-if="webIdeData?.buildStatus === 'FAILED'"
                type="button"
                class="heal-btn"
                :disabled="busy"
                @click="runAiFixBuild"
              >
                AI 빌드 오류 자동 패치 실행
              </button>
              <button type="button" class="primary-btn" @click="goToStep(6)">
                6단계: 빌드 & 배포 →
              </button>
            </div>
          </div>

          <!-- GitHub 리포지토리 프로비저닝 & 사용자 전용 개발 키(SSH Deploy Key + Site API Key) 워크벤치 -->
          <div class="git-repo-workbench" v-if="activeSiteSlug">
            <div class="git-wb-head">
              <div class="git-wb-title-col">
                <div class="git-wb-badge-row">
                  <span class="git-provider-pill">GITHUB GITOPS & DEVELOPER KEY VAULT</span>
                  <span v-if="gitRepoInfo" class="git-status-pill active">
                    {{ gitRepoInfo.githubRemoteSynced ? 'GitHub 원격 연동됨' : 'Managed Git 저장소 활성' }}
                    · {{ gitRepoInfo.visibility }}
                    · branch: {{ gitRepoInfo.defaultBranch }}
                    · commit: {{ gitRepoInfo.lastCommitSha?.slice(0, 7) }}
                  </span>
                  <span v-else class="git-status-pill pending">미생성 (원클릭 프로비저닝 가능)</span>
                </div>
                <h3>
                  {{
                    gitRepoInfo
                      ? `Git 리포지토리 (${gitRepoInfo.repoOwner}/${gitRepoInfo.repoName}) & 사용자 전용 액세스 키`
                      : 'GitHub 리포지토리 생성 & 현재 사용자 전용 개발 키 발급'
                  }}
                </h3>
                <p>
                  {{
                    gitRepoInfo
                      ? '발급된 단일 리포지토리 격리형 Ed25519 SSH Deploy Key와 사이트 런타임 API Key로 로컬 IDE(VS Code · Cursor)에서 즉시 클론·실행·푸시할 수 있습니다.'
                      : '현재 사이트 코드를 즉시 실행 가능한 Vue 3 + Vite 프로젝트(package.json, vite.config.ts, .env)로 구성하여 Git 리포지토리를 생성하고, 사용자 전용 SSH 배포 키와 사이트 API 키를 발급합니다.'
                  }}
                </p>
              </div>

              <div class="git-wb-actions">
                <template v-if="!gitRepoInfo">
                  <button
                    type="button"
                    class="secondary-btn"
                    :disabled="busy"
                    @click="showGitConfigForm = !showGitConfigForm"
                  >
                    {{ showGitConfigForm ? '저장소 옵션 접기 ▲' : '저장소 · GitHub 토큰 옵션 ▼' }}
                  </button>
                  <button
                    type="button"
                    class="primary-btn"
                    :disabled="busy"
                    @click="provisionGitRepository"
                  >
                    {{ busy ? '생성 및 키 발급 중...' : 'GitHub 리포지토리 생성 & 전용 키 발급' }}
                  </button>
                </template>
                <template v-else>
                  <button
                    type="button"
                    class="primary-btn"
                    :disabled="busy"
                    @click="pushWebIdeToGit"
                  >
                    {{ busy ? '푸시 동기화 중...' : '현재 WebIDE 코드 → Git Commit & Push' }}
                  </button>
                  <a
                    :href="kkumzApi.getSshKeyDownloadUrl(activeSiteSlug)"
                    class="secondary-btn link-btn"
                    download
                  >
                    SSH 개인키(.pem) 다운로드 ↓
                  </a>
                  <a
                    :href="kkumzApi.getGitArchiveDownloadUrl(activeSiteSlug)"
                    class="secondary-btn link-btn"
                    download
                  >
                    실행 가능 Git 번들(.zip) 다운로드 ↓
                  </a>
                  <button
                    type="button"
                    class="secondary-btn"
                    :disabled="busy"
                    @click="rotateGitDeveloperKeys"
                  >
                    전용 키 재발급 (Rotate)
                  </button>
                  <button
                    type="button"
                    class="secondary-btn"
                    :disabled="busy"
                    @click="showGitConfigForm = !showGitConfigForm"
                  >
                    {{ showGitConfigForm ? '설정 닫기 ▲' : '원격 설정 ▼' }}
                  </button>
                </template>
              </div>
            </div>

            <!-- 리포지토리 생성/연동 상세 설정 폼 -->
            <div v-if="showGitConfigForm || !gitRepoInfo" class="git-config-bar">
              <div class="git-config-grid">
                <div class="mini-field">
                  <label>GitHub Organization / Owner 계정명</label>
                  <input
                    v-model="gitForm.repoOwner"
                    type="text"
                    placeholder="예: kkumz-sites 또는 본인 GitHub ID"
                  />
                </div>
                <div class="mini-field">
                  <label>리포지토리 이름 (Repository Name)</label>
                  <input
                    v-model="gitForm.repoName"
                    type="text"
                    :placeholder="activeSiteSlug || 'my-brand-site'"
                  />
                </div>
                <div class="mini-field">
                  <label>기본 브랜치 / 공개 범위</label>
                  <div class="inline-pair">
                    <input v-model="gitForm.defaultBranch" type="text" placeholder="main" />
                    <select v-model="gitForm.visibility">
                      <option value="PRIVATE">PRIVATE (비공개 권장)</option>
                      <option value="PUBLIC">PUBLIC (공개)</option>
                    </select>
                  </div>
                </div>
                <div class="mini-field">
                  <label>GitHub Personal Access Token (선택 · 입력 시 실제 GitHub API 원격 생성)</label>
                  <input
                    v-model="gitForm.githubPatToken"
                    type="password"
                     autocomplete="off"
                    placeholder="ghp_... (미입력 시 Kkumz Managed Git + 전용 키 즉시 발급)"
                  />
                </div>
              </div>
              <div v-if="gitRepoInfo" class="git-config-foot">
                <button
                  type="button"
                  class="secondary-btn"
                  :disabled="busy"
                  @click="provisionGitRepository"
                >
                  변경된 리포지토리 설정으로 재프로비저닝 실행
                </button>
              </div>
            </div>

            <!-- 발급된 키 2종(SSH Deploy Key + Site Runtime API Key) 및 클론 명령어 패널 -->
            <div v-if="gitRepoInfo" class="git-credentials-grid">
              <div class="git-cred-card">
                <div class="cred-card-head">
                  <strong>1. Git 리포지토리 주소 & 전용 SSH Deploy Key (Ed25519 · Write 허용)</strong>
                  <span class="cred-scope-chip">단일 리포지토리 격리 (Single-Repo Scoped)</span>
                </div>

                <div class="cred-row">
                  <label>SSH Clone URL</label>
                  <div class="cred-copy-box">
                    <code>{{ gitRepoInfo.cloneSshUrl }}</code>
                    <button type="button" class="tiny-add-btn" @click="copyTextToClipboard(gitRepoInfo.cloneSshUrl, 'SSH Clone URL')">
                      복사
                    </button>
                  </div>
                </div>

                <div class="cred-row">
                  <label>원클릭 로컬 터미널 Git Clone 명령어 (발급 키 자동 지정)</label>
                  <div class="cred-copy-box">
                    <code>{{ gitRepoInfo.quickCloneCommand }}</code>
                    <button type="button" class="tiny-add-btn" @click="copyTextToClipboard(gitRepoInfo.quickCloneCommand, 'Git Clone 명령어')">
                      명령어 복사
                    </button>
                  </div>
                </div>

                <div class="cred-row">
                  <label>SSH Deploy Key 지문 (Fingerprint) & 공개키 (Public Key)</label>
                  <div class="cred-copy-box">
                    <code>{{ gitRepoInfo.sshKeyFingerprint }} · {{ gitRepoInfo.sshPublicKey }}</code>
                    <button type="button" class="tiny-add-btn" @click="copyTextToClipboard(gitRepoInfo.sshPublicKey, 'SSH 공개키')">
                      공개키 복사
                    </button>
                  </div>
                </div>

                <div class="cred-row">
                  <div class="cred-label-split">
                    <label>사용자 전용 SSH 개인키 (Private Key PEM — 로컬 ~/.ssh/{{ activeSiteSlug }}-deploy-key.pem 저장용)</label>
                    <div class="cred-mini-btns">
                      <button type="button" class="tiny-add-btn" @click="showSshPrivateKey = !showSshPrivateKey">
                        {{ showSshPrivateKey ? '개인키 숨기기' : '개인키 내용 보기' }}
                      </button>
                      <button type="button" class="tiny-add-btn" @click="copyTextToClipboard(gitRepoInfo.sshPrivateKeyPem, 'SSH 개인키(PEM)')">
                        개인키 복사
                      </button>
                    </div>
                  </div>
                  <pre v-if="showSshPrivateKey" class="ssh-pem-preview">{{ gitRepoInfo.sshPrivateKeyPem }}</pre>
                </div>
              </div>

              <div class="git-cred-card">
                <div class="cred-card-head">
                  <strong>2. 로컬 개발 연동용 Site Runtime API Key (.env 자동 주입)</strong>
                  <span class="cred-scope-chip">SyncShop · SyncCMS · 업로드 자산 연동</span>
                </div>
                <p class="cred-helper-desc">
                  클론한 Vue 3 프로젝트를 로컬(`pnpm dev`)에서 실행할 때 Kkumz 서버의 업로드 사진(`/api/v1/kkumz/assets/files/...`), 객실·상품, 매거진, 예약폼 데이터를 안전하게 호출하는 전용 키입니다.
                </p>

                <div class="cred-row">
                  <label>Site Access Key (VITE_KKUMZ_SITE_ACCESS_KEY / X-Kkumz-Site-Key)</label>
                  <div class="cred-copy-box">
                    <code>{{ gitRepoInfo.siteAccessKey }}</code>
                    <button type="button" class="tiny-add-btn" @click="copyTextToClipboard(gitRepoInfo.siteAccessKey, 'Site Access Key')">
                      복사
                    </button>
                  </div>
                </div>

                <div class="cred-row">
                  <label>Site Secret Key (VITE_KKUMZ_SITE_SECRET_KEY)</label>
                  <div class="cred-copy-box">
                    <code>
                      {{
                        showSiteSecretKey
                          ? gitRepoInfo.siteSecretKey
                          : (gitRepoInfo.siteSecretKey?.slice(0, 12) + '••••••••••••••••••••')
                      }}
                    </code>
                    <button type="button" class="tiny-add-btn" @click="showSiteSecretKey = !showSiteSecretKey">
                      {{ showSiteSecretKey ? '숨김' : '표시' }}
                    </button>
                    <button type="button" class="tiny-add-btn" @click="copyTextToClipboard(gitRepoInfo.siteSecretKey, 'Site Secret Key')">
                      복사
                    </button>
                  </div>
                </div>

                <div v-if="gitRepoInfo.commitHistory?.length" class="cred-row">
                  <label>최근 Git 커밋 동기화 이력 ({{ gitRepoInfo.commitHistory.length }}건)</label>
                  <div class="git-commit-list">
                    <div
                      v-for="commit in gitRepoInfo.commitHistory.slice(0, 3)"
                      :key="commit.sha"
                      class="git-commit-item"
                    >
                      <span class="commit-sha">{{ commit.shortSha || commit.sha?.slice(0, 7) }}</span>
                      <span class="commit-msg">{{ commit.message }}</span>
                      <span class="commit-meta">{{ commit.author }} · {{ commit.fileCount }} files</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="webide-grid" v-if="webIdeData">
            <div class="file-explorer">
              <div class="explorer-head-row">
                <div class="explorer-title">PROJECT FILES</div>
                <button type="button" class="tiny-add-btn" @click="addNewWebIdeFile">+ 파일 추가</button>
              </div>
              <div
                v-for="(file, idx) in webIdeFiles"
                :key="file.path + idx"
                class="file-btn-row"
                :class="{ active: activeFileIndex === idx }"
              >
                <button
                  type="button"
                  class="file-btn"
                  :class="{ active: activeFileIndex === idx }"
                  @click="activeFileIndex = idx"
                >
                  {{ file.path }}
                </button>
                <button
                  v-if="webIdeFiles.length > 1"
                  type="button"
                  class="file-del-btn"
                  title="파일 삭제"
                  @click.stop="removeWebIdeFile(idx)"
                >
                  ×
                </button>
              </div>
            </div>

            <div class="editor-area">
              <div class="editor-head" v-if="activeWebIdeFile">
                <div class="editor-path-wrap">
                  <input
                    v-model="activeWebIdeFile.path"
                    type="text"
                    class="file-path-input"
                    title="파일 경로 직접 편집"
                  />
                </div>
                <div class="editor-head-right">
                  <button
                    v-if="activeWebIdeFile.path.endsWith('.json') || activeWebIdeFile.language === 'json'"
                    type="button"
                    class="tiny-add-btn"
                    @click="formatActiveJsonFile"
                  >
                    JSON 포맷 정렬
                  </button>
                  <span class="lang-tag">{{ activeWebIdeFile.language }}</span>
                </div>
              </div>
              <textarea
                v-if="activeWebIdeFile"
                v-model="activeWebIdeFile.content"
                class="code-editor"
                spellcheck="false"
              ></textarea>
            </div>

            <div class="console-area">
              <div class="console-head">
                <span>BUILD CONSOLE</span>
                <span class="status-chip" :class="webIdeData.buildStatus?.toLowerCase()">
                  {{ webIdeData.buildStatus === 'FAILED' ? '빌드 실패 (AI 패치 대기)' : '빌드 정상' }}
                </span>
              </div>
              <pre class="console-log">{{ webIdeData.buildLog }}</pre>
            </div>
          </div>
          <div v-else class="empty-step-box">
            <strong>WebIDE 프로젝트 소스 파일을 아직 불러오지 못했습니다.</strong>
            <p>사이트 프로비저닝 및 WebIDE 파일 로드를 다시 시도하거나, 바로 6단계 배포로 이동할 수 있습니다.</p>
            <button type="button" class="primary-btn" :disabled="busy" @click="goToStep(5)">
              {{ busy ? '불러오는 중...' : 'WebIDE 파일 다시 불러오기' }}
            </button>
          </div>
        </div>

        <!-- ================================================================ -->
        <!-- 6단계: 빌드 & 배포 -->
        <!-- ================================================================ -->
        <div
          v-else-if="activeStep === 6"
          class="canvas-panel"
          :class="{ 'is-busy-locked': busy }"
          :inert="busy || undefined"
        >
          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 06 · HYBRID BUILD & LIVE RELEASE</span>
              <h2>빌드 & 라이브 사이트 무중단 배포</h2>
              <p>배포 전 최종 구성을 점검하고 공개 범위·커스텀 도메인·릴리즈 노트를 설정해 라이브 사이트로 즉시 발행하세요.</p>
            </div>
            <div class="header-btns" v-if="siteRuntime">
              <button
                type="button"
                class="primary-btn"
                @click="router.push({ name: 'LiveSiteRendererView', params: { siteSlug: siteRuntime.siteSlug } })"
              >
                라이브 사이트 바로가기 (/s/{{ siteRuntime.siteSlug }}) ↗
              </button>
            </div>
          </div>

          <!-- 배포 전 최종 점검 요약 바 (Pre-flight Checklist & Quick Jump) -->
          <div v-if="siteRuntime" class="preflight-summary-bar">
            <div class="preflight-left">
              <div v-if="siteRuntime.logoSvg" class="preflight-logo" v-html="renderLogoWithBrand(siteRuntime.logoSvg, siteRuntime.siteName)"></div>
              <div class="preflight-meta">
                <strong>{{ siteRuntime.siteName }} <code>/s/{{ siteRuntime.siteSlug }}</code></strong>
                <span>
                  {{ formatSiteTypeLabel(siteRuntime.siteType) }}
                  · 상품 {{ previewProducts.length }}종
                  · 매거진 {{ previewArticles.length }}건
                  · 갤러리 {{ previewGalleryImages.length }}장
                  · 첨부문서 {{ previewDownloadableDocs.length }}건
                </span>
              </div>
            </div>
            <div class="preflight-actions">
              <button type="button" class="secondary-btn" @click="goToStep(2)">2단계 브랜드·로고 수정</button>
              <button type="button" class="secondary-btn" @click="goToStep(3)">3단계 기획서·화면 수정</button>
              <button type="button" class="secondary-btn" @click="goToStep(4)">4단계 비주얼·상품 수정</button>
            </div>
          </div>

          <div class="deploy-grid" v-if="siteRuntime">
            <div class="deploy-box">
              <h3>배포 방식 및 운영 설정</h3>
              <div class="track-cards">
                <div
                  class="track-card"
                  :class="{ selected: deployForm.deployMode === 'DYNAMIC_SDUI' }"
                  @click="deployForm.deployMode = 'DYNAMIC_SDUI'"
                >
                  <strong>즉시 디자인 배포 (권장 · 무중단 스키마)</strong>
                  <p>별도 빌드 대기 없이 컬러 토큰, 배너 문구, 상품·매거진·예약폼 구성을 1초 만에 라이브 사이트에 즉시 반영합니다.</p>
                </div>
                <div
                  class="track-card"
                  :class="{ selected: deployForm.deployMode === 'CUSTOM_BUNDLE' }"
                  @click="deployForm.deployMode = 'CUSTOM_BUNDLE'"
                >
                  <strong>코드 번들 배포 (WebIDE 커스텀)</strong>
                  <p>5단계 WebIDE에서 직접 수정한 Vue 컴포넌트 코드를 번들로 빌드하여 배포합니다.</p>
                </div>
              </div>

              <div class="step1-meta-grid">
                <div class="field-group">
                  <label>쇼케이스 피드 공개 범위 (Visibility)</label>
                  <select v-model="deployForm.visibility" class="step1-select">
                    <option value="PUBLIC">전체 공개 (PUBLIC · 쇼케이스 피드 노출)</option>
                    <option value="UNLISTED">일부 공개 (UNLISTED · 링크 보유자만 접속)</option>
                    <option value="PRIVATE">비공개 (PRIVATE · 관리자 전용 점검 모드)</option>
                  </select>
                </div>
                <div class="field-group">
                  <label>연결할 커스텀 도메인 (선택)</label>
                  <input
                    v-model="deployForm.customDomain"
                    type="text"
                    placeholder="예: www.naetga-stay.kr"
                  />
                </div>
              </div>

              <div class="field-group">
                <label>릴리즈 노트 (변경 요약 · 입력 시 새 버전 스냅샷 생성)</label>
                <input
                  v-model="deployForm.releaseNote"
                  type="text"
                  placeholder="예: 2026 시즌 메인 배너 카피 및 시그니처 상품 카탈로그 업데이트"
                />
              </div>

              <button type="button" class="primary-btn full" :disabled="busy" @click="executeDeploy">
                {{ busy ? '배포 실행 중...' : '새 릴리즈 버전 빌드 및 라이브 배포 실행' }}
              </button>
            </div>

            <div class="release-history-box">
              <h3>배포 버전 히스토리 & 원클릭 롤백</h3>
              <div class="rel-list">
                <div v-for="rel in siteRuntime.releases || []" :key="rel.id" class="rel-item">
                  <div class="rel-top">
                    <strong>v{{ rel.versionNo }} · {{ formatDeployModeLabel(rel.deployMode) }}</strong>
                    <span
                      v-if="rel.versionNo === siteRuntime.activeVersion"
                      class="active-rel-badge"
                    >
                      현재 라이브
                    </span>
                    <button
                      v-else
                      type="button"
                      class="rollback-btn"
                      @click="rollbackRelease(rel.versionNo)"
                    >
                      이 버전으로 롤백
                    </button>
                  </div>
                  <p>{{ rel.releaseNote }}</p>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-step-box">
            <strong>배포할 사이트 런타임 정보가 아직 준비되지 않았습니다.</strong>
            <p>먼저 사이트를 프로비저닝하면 라이브 배포 및 버전 롤백 기능을 사용할 수 있습니다.</p>
            <button type="button" class="primary-btn" :disabled="busy" @click="provisionAndGoStep4">
              {{ busy ? '프로비저닝 중...' : '사이트 프로비저닝 실행하기' }}
            </button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  kkumzApi,
  formatSiteTypeLabel,
  formatDeployModeLabel,
} from '@/api/kkumzApi';

const route = useRoute();
const router = useRouter();

const steps = [
  { no: 1, title: '1. 아이디어 정의', desc: '사이트 유형 & 프롬프트', optional: false },
  { no: 2, title: '2. 네이밍 & 로고', desc: '브랜드명 8종 & SVG 로고', optional: false },
  { no: 3, title: '3. 기능명세서 & 화면설계', desc: '블루프린트 & 버전 관리', optional: false },
  { no: 4, title: '4. 실시간 프리뷰 편집', desc: 'AI 대화·비주얼·상품 편집', optional: false },
  { no: 5, title: '5. WebIDE 코드 편집', desc: '개발자용 코드 커스텀', optional: true },
  { no: 6, title: '6. 빌드 & 배포', desc: '라이브 발행 & 버전 롤백', optional: false },
];

const siteTypes = ref<Array<{ code: string; badge: string; title: string; desc: string }>>([]);
const assetTypes = ref<Array<{ code: string; label: string; desc?: string }>>([]);
const assetTypeLabels = ref<Record<string, string>>({});
const studioPresets = ref<any[]>([]);
const layoutLabels = ref<Record<string, string>>({});
const componentLabels = ref<Record<string, string>>({});
const featureCatalog = ref<any[]>([]);
const featureCategories = ref<Array<{ code: string; label: string; desc?: string }>>([]);
const heroStyles = ref<Array<{ code: string; label: string; desc?: string }>>([]);

const enabledFeatures = ref<string[]>([]);
const aiIntentAnalysis = ref<Record<string, any>>({});
const featureSearchQuery = ref('');
const activeFeatureCategoryTab = ref('ALL');
const activeEngineFilter = ref('ALL');
const analyzingIntent = ref(false);

const engineFilterOptions = [
  { code: 'ALL', label: '전체 엔진' },
  { code: 'SYNC_SHOP', label: 'SyncShop 커머스·예약' },
  { code: 'SYNC_CMS', label: 'SyncCMS 콘텐츠·폼·갤러리' },
  { code: 'AI_CORE', label: 'AI Core · 커뮤니티' },
];

const step1FileInputRef = ref<HTMLInputElement | null>(null);
const step2LogoInputRef = ref<HTMLInputElement | null>(null);
const step4HeroInputRef = ref<HTMLInputElement | null>(null);
const step4CatalogImageInputRef = ref<HTMLInputElement | null>(null);
const step4ExtraAssetInputRef = ref<HTMLInputElement | null>(null);

const uploadAssetType = ref('AUTO');
const assetUploading = ref(false);
const isDraggingFiles = ref(false);
const catalogUploadTarget = ref<{ kind: 'PRODUCT' | 'ARTICLE'; index: number } | null>(null);

const ideas = ref<any[]>([]);
const selectedIdeaId = ref('');
const currentIdea = ref<any | null>(null);
const activeStep = ref(1);
const busy = ref(false);
const busyMessage = ref('');
const busySteps = ref<Array<{ label: string; status: 'done' | 'active' | 'pending' }>>([]);
const generatingNames = ref(false);
const generatingLogos = ref(false);
const toastMessage = ref('');
const toastType = ref<'info' | 'success' | 'error'>('info');

watch(busy, (isBusy) => {
  if (isBusy && typeof document !== 'undefined') {
    (document.activeElement as HTMLElement | null)?.blur?.();
  }
  if (!isBusy) {
    busySteps.value = [];
    generatingNames.value = false;
    generatingLogos.value = false;
  }
});
const errorBanner = ref<{
  title: string;
  detail: string;
  endpoint?: string;
  retryAction?: (() => void) | null;
  canMoveToStep2?: boolean;
} | null>(null);
const specEditMode = ref(false);
const step4Tab = ref<'AI_CHAT' | 'VISUAL_THEME' | 'CATALOG_EDIT' | 'FORM_EDIT'>('AI_CHAT');

const formStep1 = ref({
  siteType: 'COMMERCE',
  title: '',
  promptText: '',
  category: 'AUTO',
  tags: [] as string[],
});
const newTagInput = ref('');

const namingTonePreference = ref('ALL');
const keywordHint = ref('');
const selectedNameId = ref('');
const selectedLogoId = ref('');
const showBrandDirectEditor = ref(false);
const showSvgCodeEditor = ref(false);

const customBrandForm = ref({
  brandName: '',
  siteSlug: '',
  pronunciation: '',
  brandStory: '',
  svgCode: '',
  colorPalette: {
    primary: '#14532d',
    secondary: '#166534',
    accent: '#d97706',
    surface: '#f8fafc',
    radius: '12px',
  } as Record<string, string>,
});

const editableSpecMarkdown = ref('');
const editableHighlights = ref<string[]>([]);
const editableScreens = ref<any[]>([]);

const siteRuntime = ref<any | null>(null);
const viewportMode = ref<'DESKTOP' | 'TABLET' | 'MOBILE'>('DESKTOP');
const chatInstruction = ref('');

const visualForm = ref({
  siteName: '',
  brandSlogan: '',
  customBadge: '',
  heroTitle: '',
  heroSubtitle: '',
  ctaText: '',
  heroBannerImage: '',
  heroBannerCaption: '',
  primaryColor: '#14532d',
  secondaryColor: '#166534',
  accentColor: '#d97706',
  surfaceColor: '#f8fafc',
  radius: '12px',
  showFlashDeal: true,
  showPhotoGallery: true,
  showCmsArticles: true,
  showCmsForm: true,
  showAiConcierge: true,
});

const editableProducts = ref<any[]>([]);
const editableArticles = ref<any[]>([]);
const editableCmsForm = ref({
  title: '',
  description: '',
  submitText: '온라인 신청 접수하기',
  conciergeGreeting: '',
});

const webIdeData = ref<any | null>(null);
const webIdeFiles = ref<Array<{ path: string; language: string; content: string }>>([]);
const activeFileIndex = ref(0);
const webIdeCommitMsg = ref('');

const showGitConfigForm = ref(false);
const showSshPrivateKey = ref(false);
const showSiteSecretKey = ref(false);
const gitForm = ref({
  repoOwner: 'kkumz-sites',
  repoName: '',
  defaultBranch: 'main',
  visibility: 'PRIVATE',
  githubPatToken: '',
});
const gitRepoInfo = computed(() => webIdeData.value?.gitRepo || null);

const deployForm = ref({
  deployMode: 'DYNAMIC_SDUI',
  releaseNote: '',
  visibility: 'PUBLIC',
  customDomain: '',
});

const nameSuggestions = computed(() => currentIdea.value?.nameSuggestions || []);
const logoSuggestions = computed(() => currentIdea.value?.logoSuggestions || []);
const ideaVersions = computed(() => currentIdea.value?.versions || []);
const ideaAssets = computed<any[]>(() => currentIdea.value?.assets || []);

const selectableAssetTypes = computed(() =>
  assetTypes.value.filter((at) => at.code !== 'AUTO'),
);

const imageAssets = computed(() =>
  ideaAssets.value.filter((a) => isImageAsset(a)),
);

const previewGalleryImages = computed<any[]>(() => {
  const fromRuntime =
    siteRuntime.value?.galleryImages ||
    siteRuntime.value?.layoutSchema?.galleryImages ||
    [];
  if (fromRuntime.length > 0) return fromRuntime;
  return imageAssets.value.filter((a) => a.assetType !== 'LOGO');
});

const previewDownloadableDocs = computed<any[]>(() => {
  const fromRuntime =
    siteRuntime.value?.downloadableDocs ||
    siteRuntime.value?.layoutSchema?.downloadableDocs ||
    [];
  if (fromRuntime.length > 0) return fromRuntime;
  return ideaAssets.value
    .filter((a) => a.assetType === 'DOC' || !isImageAsset(a))
    .map((a) => ({
      ...a,
      downloadUrl: `${a.fileUrl}?download=true`,
    }));
});

const previewHeroStyle = computed(() => {
  const primary =
    visualForm.value.primaryColor ||
    siteRuntime.value?.designTokens?.primary ||
    '#14532d';
  const secondary =
    visualForm.value.secondaryColor ||
    siteRuntime.value?.designTokens?.secondary ||
    primary;
  const bannerImg =
    visualForm.value.heroBannerImage ||
    siteRuntime.value?.layoutSchema?.heroBannerImage ||
    '';
  if (bannerImg) {
    return {
      backgroundImage: `linear-gradient(135deg, rgba(15, 23, 42, 0.72) 0%, rgba(15, 23, 42, 0.48) 100%), url("${bannerImg}")`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      color: '#ffffff',
    };
  }
  return {
    background: `linear-gradient(135deg, ${primary} 0%, ${secondary} 100%)`,
    color: '#ffffff',
  };
});

const selectedNameObj = computed(
  () => nameSuggestions.value.find((n: any) => n.id === selectedNameId.value) || nameSuggestions.value[0] || null,
);
const selectedLogoObj = computed(
  () => logoSuggestions.value.find((l: any) => l.id === selectedLogoId.value) || logoSuggestions.value[0] || null,
);

const previewBrandName = computed(() => {
  const custom = customBrandForm.value.brandName?.trim();
  if (custom) return custom;
  return (
    selectedNameObj.value?.brandName ||
    currentIdea.value?.selectedBrandName ||
    currentIdea.value?.title ||
    '신규 브랜드'
  );
});

function sanitizeBrandStoryText(raw: any): string {
  if (!raw) return '';
  return String(raw).trim();
}

function escapeXmlText(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

function renderLogoWithBrand(svgCode: any, overrideBrandName?: string): string {
  const rawSvg = String(svgCode || '');
  if (!rawSvg) return '';
  const targetBrand = (overrideBrandName || previewBrandName.value || '').trim();
  if (!targetBrand) return rawSvg;

  const escapedBrand = escapeXmlText(targetBrand);
  const firstChar = escapeXmlText(targetBrand.charAt(0));

  return rawSvg.replace(/(<text\b[^>]*>)([^<]+)(<\/text>)/gi, (full, openTag, innerText, closeTag) => {
    const trimmed = String(innerText || '').trim();
    if (!trimmed) return full;
    const upper = trimmed.toUpperCase();
    if (
      upper.includes('OFFICIAL') ||
      upper.includes('BRAND') ||
      upper.includes('ARCHIVE') ||
      upper.includes('STUDIO') ||
      upper.includes('COLLECTION') ||
      upper.includes('EST.') ||
      upper.includes('SINCE')
    ) {
      return full;
    }
    if (trimmed.length === 1) {
      return `${openTag}${firstChar}${closeTag}`;
    }
    return `${openTag}${escapedBrand}${closeTag}`;
  });
}

const previewLogoSvg = computed(() => {
  const raw =
    customBrandForm.value.svgCode ||
    selectedLogoObj.value?.svgCode ||
    currentIdea.value?.selectedLogoSvg ||
    '';
  return renderLogoWithBrand(raw, previewBrandName.value);
});

const activeSiteSlug = computed(
  () =>
    customBrandForm.value.siteSlug?.trim() ||
    siteRuntime.value?.siteSlug ||
    currentIdea.value?.site?.siteSlug ||
    currentIdea.value?.selectedSiteSlug ||
    '',
);
const activeWebIdeFile = computed(() => webIdeFiles.value[activeFileIndex.value] || null);

const previewProducts = computed(() => {
  if (editableProducts.value.length > 0) return editableProducts.value;
  return siteRuntime.value?.products || siteRuntime.value?.commerce?.products || [];
});
const previewCategories = computed(
  () => siteRuntime.value?.categories || siteRuntime.value?.commerce?.categories || [],
);
const previewArticles = computed(() => {
  if (editableArticles.value.length > 0) return editableArticles.value;
  return siteRuntime.value?.articles || siteRuntime.value?.cms?.articles || [];
});
const previewFeatureBlocks = computed<Record<string, any>>(
  () => siteRuntime.value?.featureBlocks || siteRuntime.value?.layoutSchema?.featureBlocks || {},
);

const recommendedFeatureCodes = computed<string[]>(() => {
  const fromAnalysis = aiIntentAnalysis.value?.recommendedFeatures;
  if (Array.isArray(fromAnalysis) && fromAnalysis.length > 0) {
    return fromAnalysis.map((c: any) => String(c));
  }
  return enabledFeatures.value;
});

const aiFeatureReasons = computed<Record<string, string>>(() => {
  const raw = aiIntentAnalysis.value?.featureReasons;
  if (raw && typeof raw === 'object') {
    return raw as Record<string, string>;
  }
  return {};
});

const filteredFeatureCatalog = computed<any[]>(() => {
  const q = featureSearchQuery.value.trim().toLowerCase();
  const catTab = activeFeatureCategoryTab.value;
  const eng = activeEngineFilter.value;

  return featureCatalog.value.filter((feat: any) => {
    const code = String(feat.featureCode || '');
    if (eng !== 'ALL' && String(feat.engineCode || '') !== eng) {
      return false;
    }
    if (catTab === 'RECOMMENDED' && !recommendedFeatureCodes.value.includes(code)) {
      return false;
    } else if (catTab === 'SELECTED' && !enabledFeatures.value.includes(code)) {
      return false;
    } else if (
      catTab !== 'ALL' &&
      catTab !== 'RECOMMENDED' &&
      catTab !== 'SELECTED' &&
      String(feat.categoryCode || '') !== catTab
    ) {
      return false;
    }
    if (!q) return true;
    const keywords = Array.isArray(feat.searchKeywords) ? feat.searchKeywords.join(' ') : '';
    const haystack = [
      feat.featureCode,
      feat.title,
      feat.subtitle,
      feat.description,
      feat.categoryName,
      feat.badgeLabel,
      feat.engineCode,
      keywords,
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase();
    return haystack.includes(q);
  });
});

function countFeaturesByCategory(catCode: string): number {
  return featureCatalog.value.filter((f: any) => f.categoryCode === catCode).length;
}

function isFeatureEnabled(code: string): boolean {
  return enabledFeatures.value.includes(code);
}

function isFeatureRecommended(code: string): boolean {
  return recommendedFeatureCodes.value.includes(code);
}

function getFeatureReason(code: string): string {
  return aiFeatureReasons.value[code] || '';
}

function formatEngineBadge(engineCode: string): string {
  if (engineCode === 'SYNC_SHOP') return 'SyncShop 커머스';
  if (engineCode === 'SYNC_CMS') return 'SyncCMS 콘텐츠';
  if (engineCode === 'AI_CORE') return 'AI Core 지능형';
  return engineCode || 'SyncSeries';
}

function formatHeroStyleLabel(code: string): string {
  const found = heroStyles.value.find((h) => h.code === code);
  if (found) return found.label;
  if (code === 'FULL_SCENIC') return '풀스크린 파노라마 배너';
  if (code === 'SPLIT_VISUAL') return '좌우 분할 시그니처 쇼케이스';
  if (code === 'EDITORIAL_STORY') return '매거진 에디토리얼 커버';
  if (code === 'PROMO_COMMERCE') return '커머스 프로모션 포커스';
  return code || '표준 히어로 배너';
}

function toggleFeatureCard(code: string) {
  if (!code) return;
  const idx = enabledFeatures.value.indexOf(code);
  if (idx >= 0) {
    enabledFeatures.value.splice(idx, 1);
  } else {
    enabledFeatures.value.push(code);
  }
}

function selectRecommendedFeatures() {
  const recs = recommendedFeatureCodes.value;
  if (recs.length === 0) {
    showToast('먼저 [✨ AI로 아이디어 분석 & 맞춤 기능 추천받기]를 실행해 주세요.', 'info');
    return;
  }
  const merged = new Set<string>([...recs]);
  enabledFeatures.value = Array.from(merged);
  showToast(`AI가 추천한 ${enabledFeatures.value.length}종 핵심 기능이 선택되었습니다.`, 'success');
}

function applyExpandedPrompt() {
  const exp = String(aiIntentAnalysis.value?.expandedPrompt || '').trim();
  if (!exp) return;
  formStep1.value.promptText = exp;
  showToast('AI가 구체화한 상세 기획 프롬프트가 적용되었습니다.', 'success');
}

async function runIntentAnalysis(applyExpandedAndAssets = false) {
  if (busy.value || analyzingIntent.value) return;
  if (!formStep1.value.promptText.trim()) {
    showToast('먼저 아이디어 상세 설명(프롬프트)을 입력해 주세요.', 'error');
    return;
  }
  analyzingIntent.value = true;
  errorBanner.value = null;
  try {
    let targetId = selectedIdeaId.value;
    if (!targetId) {
      const created = await kkumzApi.createIdea({
        title: formStep1.value.title || formStep1.value.promptText.slice(0, 40),
        promptText: formStep1.value.promptText,
        siteType: formStep1.value.siteType,
        category: formStep1.value.category,
        tags: formStep1.value.tags,
        enabledFeatures: enabledFeatures.value,
        visibility: 'PUBLIC',
      });
      ideas.value = (await kkumzApi.listIdeas()) || [];
      selectedIdeaId.value = created.id;
      targetId = created.id;
    }

    const updated = await kkumzApi.analyzeIdeaIntent(targetId, {
      title: formStep1.value.title,
      promptText: formStep1.value.promptText,
      category: formStep1.value.category,
      tags: formStep1.value.tags,
      applyExpandedPrompt: applyExpandedAndAssets,
      applyRecommendedFeatures: true,
    });

    currentIdea.value = updated;
    formStep1.value.siteType = updated.siteType || formStep1.value.siteType;
    formStep1.value.category = updated.category || formStep1.value.category;
    if (Array.isArray(updated.tags) && updated.tags.length > 0) {
      formStep1.value.tags = [...updated.tags];
    }
    if (applyExpandedAndAssets && updated.promptText) {
      formStep1.value.promptText = updated.promptText;
    }
    if (Array.isArray(updated.enabledFeatures)) {
      enabledFeatures.value = [...updated.enabledFeatures];
    }
    if (updated.aiIntentAnalysis && typeof updated.aiIntentAnalysis === 'object') {
      aiIntentAnalysis.value = { ...updated.aiIntentAnalysis };
    }
    syncStep3FromIdea(updated);
    ideas.value = (await kkumzApi.listIdeas()) || [];
    showToast(
      `AI 분석 완료! 추천 아키텍처(${formatSiteTypeLabel(formStep1.value.siteType)})와 맞춤 기능 ${enabledFeatures.value.length}종이 자동 구성되었습니다.`,
      'success',
    );
  } catch (err) {
    reportError(
      err,
      'AI 의도·기능 카탈로그 분석 중 오류가 발생했습니다',
      '프롬프트 분석 요청을 처리하는 중 오류가 발생했습니다.',
      () => runIntentAnalysis(applyExpandedAndAssets),
    );
  } finally {
    analyzingIntent.value = false;
  }
}

async function saveEnabledFeaturesToServer(showNotification = false) {
  if (!selectedIdeaId.value) return;
  try {
    const updated = await kkumzApi.updateIdeaFeatures(selectedIdeaId.value, {
      siteType: formStep1.value.siteType,
      category: formStep1.value.category,
      tags: formStep1.value.tags,
      enabledFeatures: enabledFeatures.value,
      aiIntentAnalysis: aiIntentAnalysis.value,
    });
    currentIdea.value = updated;
    syncStep3FromIdea(updated);
    if (showNotification) {
      showToast(`선택한 기능 카드 ${enabledFeatures.value.length}종이 프로젝트 및 블루프린트에 반영되었습니다.`, 'success');
    }
  } catch (err) {
    if (showNotification) {
      reportError(
        err,
        '선택 기능 저장에 실패했습니다',
        '기능 카탈로그 선택 목록을 저장하는 중 오류가 발생했습니다.',
        () => saveEnabledFeaturesToServer(true),
      );
    }
  }
}

function parseHighlightsFromMarkdown(raw: string): string[] {
  const lines = (raw || '')
    .split('\n')
    .map((line) => line.trim())
    .filter((line) => line.length > 0 && !line.startsWith('#'))
    .map((line) => line.replace(/^[\-*\d.\s✓]+/, '').replace(/\*\*/g, '').trim())
    .filter((line) => line.length > 4);
  return lines.length > 0
    ? lines.slice(0, 8)
    : [
        '브랜드 스토리 및 공간/상품 핵심 매력 소개 히어로 섹션',
        '시그니처 객실·메뉴·상품 카탈로그 및 실시간 예약·주문 연동',
        '현장 포토 갤러리 아카이브 및 안내문·요금표 다운로드 제공',
        '24시간 AI 브랜드 컨시어지 상담 및 온라인 예약 신청 폼',
      ];
}

function formatLayoutTypeLabel(code: string) {
  if (!code) return '반응형 표준 섹션';
  return layoutLabels.value[code] || code;
}

function formatComponentLabel(code: string) {
  if (!code) return '기능 모듈';
  return componentLabels.value[code] || code;
}

function formatAssetTypeLabel(code: string) {
  if (!code) return '';
  return assetTypeLabels.value[code] || code;
}

function isImageAsset(asset: any) {
  if (!asset) return false;
  const mime = String(asset.mimeType || '').toLowerCase();
  const ext = String(asset.fileExt || '').toLowerCase();
  return (
    mime.startsWith('image/') ||
    ['jpg', 'jpeg', 'png', 'webp', 'gif', 'bmp', 'avif', 'svg'].includes(ext)
  );
}

function formatFileSize(bytes: any) {
  const num = Number(bytes || 0);
  if (!num || num <= 0) return '0 KB';
  if (num < 1024) return `${num} B`;
  if (num < 1024 * 1024) return `${(num / 1024).toFixed(1)} KB`;
  return `${(num / (1024 * 1024)).toFixed(2)} MB`;
}

function showToast(msg: string, type: 'info' | 'success' | 'error' = 'info') {
  toastMessage.value = msg;
  toastType.value = type;
  const duration = type === 'error' ? 6800 : 3800;
  setTimeout(() => {
    if (toastMessage.value === msg) toastMessage.value = '';
  }, duration);
}

function formatApiError(err: any, fallbackDetail: string) {
  const status = err?.response?.status;
  const method = (err?.config?.method || 'POST').toUpperCase();
  const rawUrl = err?.config?.url || '';
  const fullPath = rawUrl
    ? rawUrl.startsWith('/api')
      ? rawUrl
      : `/api/v1/kkumz${rawUrl.startsWith('/') ? '' : '/'}${rawUrl}`
    : '';
  const endpoint = fullPath ? `${method} ${fullPath}${status ? ` (${status})` : ''}` : status ? `HTTP ${status}` : '';

  const serverData = err?.response?.data;
  const serverMsg =
    (typeof serverData === 'object' &&
      (serverData?.message || serverData?.msg || serverData?.error_description)) ||
    '';
  const cleanServerMsg =
    serverMsg && serverMsg !== 'Internal Server Error' && serverMsg !== 'No message available'
      ? String(serverMsg)
      : '';

  let detail = fallbackDetail;
  if (status === 500) {
    detail = cleanServerMsg
      ? `${fallbackDetail} (서버 내부 오류 500: ${cleanServerMsg})`
      : `${fallbackDetail} — 서버 내부 오류(HTTP 500)가 발생하여 요청을 완료하지 못했습니다. 잠시 후 [다시 시도]를 누르거나 서버 상태를 확인해 주세요.`;
  } else if (err?.code === 'ECONNABORTED') {
    detail = `${fallbackDetail} — AI 생성 응답 대기 시간이 초과되었습니다. 잠시 후 다시 시도해 주세요.`;
  } else if (!err?.response) {
    detail = `${fallbackDetail} — 백엔드 서버와 통신할 수 없습니다. 서버 실행 상태를 확인해 주세요.`;
  } else if (cleanServerMsg) {
    detail = `${fallbackDetail} (${cleanServerMsg})`;
  } else if (status) {
    detail = `${fallbackDetail} (HTTP ${status} 오류)`;
  }

  return { detail, endpoint };
}

function reportError(
  err: any,
  title: string,
  fallbackDetail: string,
  retryAction?: () => void,
  canMoveToStep2 = false,
) {
  const { detail, endpoint } = formatApiError(err, fallbackDetail);
  errorBanner.value = {
    title,
    detail,
    endpoint,
    retryAction: retryAction || null,
    canMoveToStep2,
  };
  showToast(`${title}: ${detail}`, 'error');
}

function runErrorRetry() {
  const fn = errorBanner.value?.retryAction;
  if (fn) {
    fn();
  }
}

function forceMoveToStep2() {
  errorBanner.value = null;
  activeStep.value = 2;
}

function applyStarterPreset(preset: any) {
  formStep1.value.siteType = preset.siteType || 'COMMERCE';
  formStep1.value.title = preset.title || preset.label || '';
  formStep1.value.promptText = preset.prompt || preset.promptText || '';
  if (preset.category) {
    formStep1.value.category = preset.category;
  }
}

function addStep1Tag() {
  const val = newTagInput.value.trim().replace(/^#/, '');
  if (!val) return;
  if (!formStep1.value.tags.includes(val)) {
    formStep1.value.tags.push(val);
  }
  newTagInput.value = '';
}

function syncCustomBrandFromSelection(detail?: any) {
  const idea = detail || currentIdea.value;
  const selName =
    nameSuggestions.value.find((n: any) => n.id === selectedNameId.value) ||
    nameSuggestions.value[0] ||
    null;
  const selLogo =
    logoSuggestions.value.find((l: any) => l.id === selectedLogoId.value) ||
    logoSuggestions.value[0] ||
    null;

  const brandName = selName?.brandName || idea?.selectedBrandName || idea?.title || '';
  const siteSlug = selName?.siteSlug || idea?.selectedSiteSlug || idea?.site?.siteSlug || '';
  const pronunciation = selName?.pronunciation || '';
  const brandStory = sanitizeBrandStoryText(selName?.brandStory || '');
  const palette = selLogo?.colorPalette || {};
  const rawSvg = selLogo?.svgCode || idea?.selectedLogoSvg || '';

  customBrandForm.value = {
    brandName,
    siteSlug,
    pronunciation,
    brandStory,
    svgCode: renderLogoWithBrand(rawSvg, brandName),
    colorPalette: {
      primary: String(palette.primary || '#14532d'),
      secondary: String(palette.secondary || '#166534'),
      accent: String(palette.accent || '#d97706'),
      surface: String(palette.surface || '#f8fafc'),
      radius: String(palette.radius || '12px'),
    },
  };
}

function onCustomBrandNameInput() {
  const name = customBrandForm.value.brandName.trim();
  if (name && customBrandForm.value.svgCode) {
    customBrandForm.value.svgCode = renderLogoWithBrand(customBrandForm.value.svgCode, name);
  }
}

function onCustomSlugInput() {
  customBrandForm.value.siteSlug = customBrandForm.value.siteSlug
    .toLowerCase()
    .replace(/[^a-z0-9-]/g, '-')
    .replace(/-+/g, '-');
}

function onCustomPaletteChange() {
  const baseLogo = selectedLogoObj.value;
  if (!baseLogo?.svgCode) return;
  let updatedSvg = renderLogoWithBrand(baseLogo.svgCode, previewBrandName.value);
  const oldPal = baseLogo.colorPalette || {};
  const newPal = customBrandForm.value.colorPalette;
  for (const key of ['primary', 'secondary', 'accent', 'surface']) {
    const oldHex = String(oldPal[key] || '').trim();
    const newHex = String(newPal[key] || '').trim();
    if (oldHex && newHex && oldHex.toLowerCase() !== newHex.toLowerCase()) {
      const escaped = oldHex.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
      updatedSvg = updatedSvg.replace(new RegExp(escaped, 'gi'), newHex);
    }
  }
  customBrandForm.value.svgCode = updatedSvg;
}

function syncStep3FromIdea(detail: any) {
  if (!detail) return;
  editableSpecMarkdown.value = detail.specMarkdown || '';
  editableHighlights.value = parseHighlightsFromMarkdown(editableSpecMarkdown.value);
  const rawScreens = detail.screenSpecs || detail.screens || [];
  editableScreens.value = rawScreens.map((s: any, idx: number) => ({
    id: s.id || '',
    screenKey: s.screenKey || `scr_${idx + 1}`,
    screenTitle: s.screenTitle || s.title || `화면 섹션 ${idx + 1}`,
    routePath: s.routePath || `/${s.screenKey || `section-${idx + 1}`}`,
    layoutType: s.layoutType || s.layout || 'HERO_BANNER_GRID',
    description: s.description || '',
    components: Array.isArray(s.components) ? [...s.components] : [],
    sortOrder: s.sortOrder ?? idx + 1,
  }));
}

function addSpecHighlightItem() {
  editableHighlights.value.push('새로운 핵심 서비스 기능 또는 공간 특징을 입력하세요');
  syncHighlightsToMarkdown();
}

function removeSpecHighlightItem(idx: number) {
  editableHighlights.value.splice(idx, 1);
  syncHighlightsToMarkdown();
}

function syncHighlightsToMarkdown() {
  const cleanLines = editableHighlights.value
    .map((l) => l.trim())
    .filter((l) => l.length > 0);
  const bulletBlock = cleanLines.map((l) => `- ${l}`).join('\n');
  const titleHeader = `# ${previewBrandName.value} 서비스 기능명세서\n\n## 핵심 제공 기능 및 공간 경험\n`;
  editableSpecMarkdown.value = `${titleHeader}${bulletBlock}\n`;
}

function syncMarkdownToHighlights() {
  editableHighlights.value = parseHighlightsFromMarkdown(editableSpecMarkdown.value);
}

function addEditableScreen() {
  const nextNo = editableScreens.value.length + 1;
  editableScreens.value.push({
    id: '',
    screenKey: `custom_sec_${Date.now()}`,
    screenTitle: `신규 커스텀 섹션 0${nextNo}`,
    routePath: `/section-${nextNo}`,
    layoutType: 'PRODUCT_CATALOG_GRID',
    description: '이 화면 섹션에서 제공할 콘텐츠와 핵심 기능을 설명해 주세요.',
    components: ['ProductGrid', 'AiConciergeWidget'],
    sortOrder: nextNo,
  });
}

async function removeEditableScreen(idx: number) {
  const target = editableScreens.value[idx];
  editableScreens.value.splice(idx, 1);
  if (selectedIdeaId.value && target?.id) {
    try {
      await kkumzApi.deleteIdeaScreen(selectedIdeaId.value, target.id);
    } catch {
      // will also be synced on saveSpecVersion
    }
  }
}

function moveEditableScreen(idx: number, delta: number) {
  const targetIdx = idx + delta;
  if (targetIdx < 0 || targetIdx >= editableScreens.value.length) return;
  const temp = editableScreens.value[idx];
  editableScreens.value[idx] = editableScreens.value[targetIdx];
  editableScreens.value[targetIdx] = temp;
  editableScreens.value.forEach((s, i) => {
    s.sortOrder = i + 1;
  });
}

function addComponentToScreen(scr: any, compCode: string) {
  if (!compCode) return;
  if (!Array.isArray(scr.components)) {
    scr.components = [];
  }
  if (!scr.components.includes(compCode)) {
    scr.components.push(compCode);
  }
}

function normalizeRuntime(raw: any) {
  if (!raw) return null;
  const site = raw.site || raw;
  const rel = raw.release || {};
  return {
    ...site,
    ...raw,
    layoutSchema: raw.layoutSchema || rel.layoutSchema || {},
    designTokens: raw.designTokens || rel.designTokens || {},
  };
}

function syncVisualFormFromRuntime(rt: any) {
  if (!rt) return;
  const normalized = normalizeRuntime(rt);
  siteRuntime.value = normalized;
  const layout = normalized.layoutSchema || {};
  const tokens = normalized.designTokens || {};
  const rawCap = layout.heroBannerCaption;
  const capStr =
    typeof rawCap === 'object' && rawCap !== null
      ? String(rawCap.title || '')
      : String(rawCap || '');

  visualForm.value = {
    siteName: normalized.siteName || '',
    brandSlogan: normalized.brandSlogan || '',
    customBadge: layout.customBadge || '공식 브랜드 쇼케이스',
    heroTitle: layout.heroTitle || normalized.siteName || '',
    heroSubtitle: layout.heroSubtitle || normalized.brandSlogan || '',
    ctaText: layout.ctaText || layout.primaryCta || '시그니처 컬렉션 탐색하기',
    heroBannerImage: layout.heroBannerImage || '',
    heroBannerCaption: capStr,
    primaryColor: tokens.primary || tokens.primaryColor || '#14532d',
    secondaryColor: tokens.secondary || tokens.secondaryColor || tokens.primary || '#166534',
    accentColor: tokens.accent || tokens.accentColor || '#d97706',
    surfaceColor: tokens.surface || tokens.surfaceColor || '#f8fafc',
    radius: tokens.radius || '12px',
    showFlashDeal: layout.showFlashDeal !== false,
    showPhotoGallery: layout.showPhotoGallery !== false,
    showCmsArticles: layout.showCmsArticles !== false,
    showCmsForm: layout.showCmsForm !== false,
    showAiConcierge: layout.showAiConcierge !== false,
  };
  const prods = normalized.products || normalized.commerce?.products || [];
  editableProducts.value = prods.map((p: any) => ({
    id: p.id,
    name: p.name,
    pic: p.pic || '',
    price: Number(p.price || p.promotionPrice || 0),
    promotionPrice: Number(p.promotionPrice || p.price || 0),
    subTitle: p.subTitle || '',
    stock: Number(p.stock ?? 0),
    unit: p.unit || '개',
    brandName: p.brandName || normalized.siteName,
    productCategoryName: p.productCategoryName || p.categoryName || '',
  }));
  const arts = normalized.articles || normalized.cms?.articles || [];
  editableArticles.value = arts.map((a: any) => ({
    id: a.id,
    title: a.title,
    author: a.author || a.authorName || '',
    description: a.description || a.summary || '',
    summary: a.summary || a.description || '',
    bodyHtml: a.bodyHtml || a.contentHtml || `<p>${a.description || a.summary || ''}</p>`,
    categoryName: a.categoryName || '',
    cover: a.cover || a.thumbnailUrl || '',
    clicks: a.clicks || a.viewCount || 0,
    viewCount: a.viewCount || a.clicks || 0,
  }));

  const formObj = layout.customCmsForm || normalized.cmsForm || normalized.cms?.forms?.[0] || {};
  editableCmsForm.value = {
    title: formObj.title || formObj.formName || `${normalized.siteName || '브랜드'} 온라인 예약 · 문의 신청`,
    description:
      formObj.description ||
      '원하시는 일정, 인원 또는 문의 사항을 남겨주시면 확인 후 신속히 안내해 드립니다.',
    submitText: formObj.submitText || '온라인 신청 접수하기',
    conciergeGreeting:
      layout.conciergeGreeting ||
      `안녕하세요! ${normalized.siteName || '브랜드'} AI 컨시어지입니다. 상품·객실·요금·예약 관련 궁금한 점을 물어보세요.`,
  };

  deployForm.value.deployMode = normalized.deployMode || 'DYNAMIC_SDUI';
  deployForm.value.visibility = currentIdea.value?.visibility || 'PUBLIC';
  deployForm.value.customDomain = normalized.customDomain || '';
}

async function initStudio() {
  try {
    const meta = await kkumzApi.getStudioMetadata();
    layoutLabels.value = meta?.layoutLabels || {};
    componentLabels.value = meta?.componentLabels || {};
    assetTypeLabels.value = meta?.assetTypeLabels || {};
    assetTypes.value = meta?.assetTypes || [];
    siteTypes.value = meta?.siteTypes || [];
    studioPresets.value = meta?.starterPresets || [];
    featureCatalog.value = meta?.featureCatalog || [];
    featureCategories.value = meta?.featureCategories || [];
    heroStyles.value = meta?.heroStyles || [];
  } catch {
    layoutLabels.value = {};
    componentLabels.value = {};
    assetTypeLabels.value = {};
    assetTypes.value = [];
    siteTypes.value = [];
    studioPresets.value = [];
    featureCatalog.value = [];
    featureCategories.value = [];
    heroStyles.value = [];
  }
  try {
    ideas.value = (await kkumzApi.listIdeas()) || [];
    const routeIdeaId = route.params.ideaId ? String(route.params.ideaId) : '';
    const targetId = routeIdeaId || (ideas.value[0]?.id ?? '');
    if (targetId) {
      await onSelectIdea(targetId);
    }
  } catch (err) {
    reportError(
      err,
      '프로젝트 목록을 불러오지 못했습니다',
      '스튜디오 초기 데이터를 조회하는 중 오류가 발생했습니다.',
      () => initStudio(),
    );
  }
}

async function onSelectIdea(ideaId: string, allowWhenBusy = false) {
  if (!ideaId) return;
  if (busy.value && !allowWhenBusy) return;
  selectedIdeaId.value = ideaId;
  errorBanner.value = null;
  if (String(route.params.ideaId || '') !== ideaId) {
    router.replace({ name: 'StudioView', params: { ideaId } }).catch(() => {});
  }
  try {
    const detail = await kkumzApi.getIdeaDetail(ideaId);
    if (selectedIdeaId.value !== ideaId) return;
    currentIdea.value = detail;

    formStep1.value = {
      siteType: detail.siteType || 'COMMERCE',
      title: detail.title || '',
      promptText: detail.promptText || '',
      category: detail.category || 'AUTO',
      tags: Array.isArray(detail.tags) ? [...detail.tags] : [],
    };
    enabledFeatures.value = Array.isArray(detail.enabledFeatures) ? [...detail.enabledFeatures] : [];
    aiIntentAnalysis.value =
      detail.aiIntentAnalysis && typeof detail.aiIntentAnalysis === 'object'
        ? { ...detail.aiIntentAnalysis }
        : {};
    syncStep3FromIdea(detail);

    const selName = (detail.nameSuggestions || []).find((n: any) => n.isSelected) || detail.nameSuggestions?.[0];
    selectedNameId.value = selName?.id || '';

    const selLogo = (detail.logoSuggestions || []).find((l: any) => l.isSelected) || detail.logoSuggestions?.[0];
    selectedLogoId.value = selLogo?.id || '';

    syncCustomBrandFromSelection(detail);

    const slug = detail.site?.siteSlug || detail.deployedSites?.[0]?.siteSlug || detail.selectedSiteSlug;
    if (slug) {
      try {
        siteRuntime.value = await kkumzApi.getSiteRuntime(slug);
        syncVisualFormFromRuntime(siteRuntime.value);
      } catch {
        siteRuntime.value = null;
      }
    } else {
      siteRuntime.value = null;
    }

    if (activeStep.value === 2 && !allowWhenBusy) {
      await ensureStep2Candidates();
    }
  } catch (err) {
    if (selectedIdeaId.value !== ideaId) return;
    reportError(
      err,
      '프로젝트 상세 정보를 불러오지 못했습니다',
      '선택한 아이디어 정보를 조회하는 중 오류가 발생했습니다.',
      () => onSelectIdea(ideaId),
    );
  }
}

async function performGenerateNames(forceRegenerate = false) {
  if (!selectedIdeaId.value) return;
  generatingNames.value = true;
  try {
    let updated: any;
    try {
      updated = await kkumzApi.generateNames(selectedIdeaId.value, {
        keywordHint: keywordHint.value.trim(),
        toneStyle: namingTonePreference.value !== 'ALL' ? namingTonePreference.value : undefined,
        forceRegenerate,
      });
    } catch (err) {
      // 네트워크 순단·타임아웃 발생 시 백엔드 DB에 이미 생성 완료되었는지 자동 복구 확인
      try {
        const recovered = await kkumzApi.getIdeaDetail(selectedIdeaId.value);
        if (!forceRegenerate && (recovered?.nameSuggestions?.length || 0) >= 8) {
          updated = recovered;
        } else {
          throw err;
        }
      } catch {
        throw err;
      }
    }

    if (currentIdea.value && updated) {
      // 병렬 실행 중인 로고 생성 결과를 덮어쓰지 않도록 네이밍 관련 필드만 병합
      currentIdea.value = {
        ...currentIdea.value,
        category: updated.category || currentIdea.value.category,
        selectedBrandName: updated.selectedBrandName || currentIdea.value.selectedBrandName,
        selectedSiteSlug: updated.selectedSiteSlug || currentIdea.value.selectedSiteSlug,
        nameSuggestions: updated.nameSuggestions || [],
        // 백엔드가 로고 워드마크까지 동기화해 반환한 경우 최신 로고 목록도 반영
        logoSuggestions:
          updated.logoSuggestions?.length >= (currentIdea.value.logoSuggestions?.length || 0)
            ? updated.logoSuggestions
            : currentIdea.value.logoSuggestions,
      };
    } else {
      currentIdea.value = updated;
    }

    if (currentIdea.value?.category) {
      formStep1.value.category = currentIdea.value.category;
    }
    const sel =
      (currentIdea.value?.nameSuggestions || []).find((n: any) => n.isSelected) ||
      currentIdea.value?.nameSuggestions?.[0];
    selectedNameId.value = sel?.id || '';
    syncCustomBrandFromSelection(currentIdea.value);
  } finally {
    generatingNames.value = false;
  }
}

async function performGenerateLogos(forceRegenerate = false) {
  if (!selectedIdeaId.value) return;
  generatingLogos.value = true;
  try {
    let updated: any;
    try {
      updated = await kkumzApi.generateLogos(selectedIdeaId.value, {
        brandName: previewBrandName.value,
        stylePreference: namingTonePreference.value !== 'ALL' ? namingTonePreference.value : undefined,
        forceRegenerate,
      });
    } catch (err) {
      // 네트워크 순단·타임아웃 발생 시 백엔드 DB에 이미 생성 완료되었는지 자동 복구 확인
      try {
        const recovered = await kkumzApi.getIdeaDetail(selectedIdeaId.value);
        if (!forceRegenerate && (recovered?.logoSuggestions?.length || 0) >= 6) {
          updated = recovered;
        } else {
          throw err;
        }
      } catch {
        throw err;
      }
    }

    if (currentIdea.value && updated) {
      // 병렬 실행 중인 네이밍 생성 결과를 덮어쓰지 않도록 로고 관련 필드만 병합
      currentIdea.value = {
        ...currentIdea.value,
        selectedLogoSvg: updated.selectedLogoSvg || currentIdea.value.selectedLogoSvg,
        designTokens: updated.designTokens || currentIdea.value.designTokens,
        logoSuggestions: updated.logoSuggestions || [],
        nameSuggestions:
          updated.nameSuggestions?.length >= (currentIdea.value.nameSuggestions?.length || 0)
            ? updated.nameSuggestions
            : currentIdea.value.nameSuggestions,
      };
    } else {
      currentIdea.value = updated;
    }

    const sel =
      (currentIdea.value?.logoSuggestions || []).find((l: any) => l.isSelected) ||
      currentIdea.value?.logoSuggestions?.[0];
    selectedLogoId.value = sel?.id || '';
    syncCustomBrandFromSelection(currentIdea.value);
  } finally {
    generatingLogos.value = false;
  }
}

async function runStep2GenerationsParallel(
  needNames: boolean,
  needLogos: boolean,
  forceRegenerate: boolean,
  nameStepIndex: number,
  logoStepIndex: number,
): Promise<{ nameErr: any; logoErr: any }> {
  let nameErr: any = null;
  let logoErr: any = null;

  const tasks: Promise<void>[] = [];

  if (needNames && needLogos) {
    if (busySteps.value[nameStepIndex]) busySteps.value[nameStepIndex].status = 'active';
    if (busySteps.value[logoStepIndex]) busySteps.value[logoStepIndex].status = 'active';
    busyMessage.value = 'AI가 브랜드 네이밍 8종과 벡터 SVG 로고 6종을 동시에 병렬 생성하고 있습니다...';
  } else if (needNames) {
    if (busySteps.value[nameStepIndex]) busySteps.value[nameStepIndex].status = 'active';
    busyMessage.value = 'AI가 브랜드 네이밍 후보 8종을 생성하고 있습니다...';
  } else if (needLogos) {
    if (busySteps.value[logoStepIndex]) busySteps.value[logoStepIndex].status = 'active';
    busyMessage.value = 'AI가 벡터 SVG 로고 후보 6종을 생성하고 있습니다...';
  }

  if (needNames) {
    tasks.push(
      performGenerateNames(forceRegenerate)
        .then(() => {
          if (busySteps.value[nameStepIndex]) busySteps.value[nameStepIndex].status = 'done';
          if (needLogos && generatingLogos.value) {
            busyMessage.value = '브랜드 네이밍 8종 완료! 벡터 SVG 로고 6종을 마무리하고 있습니다...';
          }
        })
        .catch((err) => {
          nameErr = err;
        }),
    );
  }

  if (needLogos) {
    tasks.push(
      performGenerateLogos(forceRegenerate)
        .then(() => {
          if (busySteps.value[logoStepIndex]) busySteps.value[logoStepIndex].status = 'done';
          if (needNames && generatingNames.value) {
            busyMessage.value = '벡터 SVG 로고 6종 완료! 브랜드 네이밍 8종을 마무리하고 있습니다...';
          }
        })
        .catch((err) => {
          logoErr = err;
        }),
    );
  }

  await Promise.allSettled(tasks);

  // 두 병렬 작업 완료 후 확정된 1순위 브랜드명을 SVG 로고 워드마크 및 폼에 최종 동기화
  if (currentIdea.value) {
    syncCustomBrandFromSelection(currentIdea.value);
  }

  return { nameErr, logoErr };
}

async function ensureStep2Candidates() {
  if (!selectedIdeaId.value) return;
  const needNames = nameSuggestions.value.length < 8;
  const needLogos = logoSuggestions.value.length < 6;
  if (!needNames && !needLogos) return;

  busy.value = true;
  busySteps.value = [
    { label: '1. AI 브랜드 네이밍 8종 생성', status: needNames ? 'active' : 'done' },
    { label: '2. 벡터 SVG 로고 6종 병렬 생성', status: needLogos ? 'active' : 'done' },
  ];
  try {
    const { nameErr, logoErr } = await runStep2GenerationsParallel(
      needNames,
      needLogos,
      false,
      0,
      1,
    );
    if (nameErr) {
      reportError(
        nameErr,
        'AI 브랜드 네이밍 자동 생성에 실패했습니다',
        '네이밍 후보를 불러오지 못했습니다. 상단 [네이밍 8종 재생성] 버튼으로 다시 시도해 주세요.',
        () => regenerateNames(),
      );
      return;
    }
    if (logoErr) {
      reportError(
        logoErr,
        'AI 벡터 SVG 로고 자동 생성에 실패했습니다',
        'SVG 로고 후보를 불러오지 못했습니다. 상단 [SVG 로고 6종 재생성] 버튼으로 다시 시도해 주세요.',
        () => regenerateLogos(),
      );
    }
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function executeScaffoldWithRecovery(targetIdeaId: string): Promise<any> {
  // 2단계를 거치지 않았거나 프리뷰 중인 브랜드/로고 변경분이 아직 반영되지 않은 경우 자동 동기화
  const needsBrandSync =
    !currentIdea.value?.selectedLogoSvg ||
    (customBrandForm.value.brandName.trim() &&
      customBrandForm.value.brandName.trim() !== currentIdea.value?.selectedBrandName);
  const candidateSvg =
    customBrandForm.value.svgCode ||
    selectedLogoObj.value?.svgCode ||
    currentIdea.value?.selectedLogoSvg ||
    '';
  const candidateBrand =
    customBrandForm.value.brandName.trim() ||
    selectedNameObj.value?.brandName ||
    currentIdea.value?.selectedBrandName ||
    '';

  if (needsBrandSync && (candidateSvg || candidateBrand)) {
    try {
      const syncedSvg = candidateSvg && candidateBrand
        ? renderLogoWithBrand(candidateSvg, candidateBrand)
        : candidateSvg;
      currentIdea.value = await kkumzApi.selectBrand(targetIdeaId, {
        nameSuggestionId: selectedNameId.value || undefined,
        logoSuggestionId: selectedLogoId.value || undefined,
        brandName: candidateBrand || undefined,
        siteSlug: customBrandForm.value.siteSlug.trim() || selectedNameObj.value?.siteSlug || undefined,
        pronunciation: customBrandForm.value.pronunciation.trim() || selectedNameObj.value?.pronunciation || undefined,
        brandStory:
          customBrandForm.value.brandStory.trim() ||
          sanitizeBrandStoryText(selectedNameObj.value?.brandStory || '') ||
          undefined,
        logoSvg: syncedSvg || undefined,
        designTokens: { ...customBrandForm.value.colorPalette },
      });
    } catch {
      // 백엔드 scaffoldSite 자체의 브랜드·로고 자동 보완(Self-Healing) 로직으로 계속 진행
    }
  }

  try {
    return await kkumzApi.scaffoldSite(targetIdeaId);
  } catch (err) {
    // 네트워크 순단·타임아웃 발생 시 서버에 이미 프로비저닝 완료되었는지 자동 복구 확인
    try {
      const recovered = await kkumzApi.getIdeaDetail(targetIdeaId);
      const slug =
        recovered?.site?.siteSlug ||
        recovered?.deployedSites?.[0]?.siteSlug ||
        recovered?.selectedSiteSlug;
      if (slug) {
        const rt = await kkumzApi.getSiteRuntime(slug);
        if (rt && rt.siteSlug) {
          currentIdea.value = recovered;
          return rt;
        }
      }
    } catch {
      // ignore recovery lookup error and rethrow original error
    }
    throw err;
  }
}

async function goToStep(stepNo: number) {
  if (busy.value) return;
  errorBanner.value = null;
  activeStep.value = stepNo;
  const targetIdeaId = selectedIdeaId.value;
  if (!targetIdeaId) return;

  if (stepNo === 2) {
    await ensureStep2Candidates();
  } else if (stepNo === 3) {
    const targetBrand = previewBrandName.value;
    const specMd = currentIdea.value?.specMarkdown || '';
    const needSpecSync =
      !currentIdea.value?.screenSpecs?.length ||
      (targetBrand && !specMd.includes(targetBrand));
    if (needSpecSync) {
      busy.value = true;
      busyMessage.value = `'${targetBrand}' 브랜드 기준으로 AI 3단계 기능명세서 및 화면 설계를 준비하고 있습니다...`;
      try {
        currentIdea.value = await kkumzApi.generateSpec(targetIdeaId, false);
      } catch {
        // ignore and fallback to existing idea state
      } finally {
        busy.value = false;
        busyMessage.value = '';
      }
    }
    if (currentIdea.value) {
      syncStep3FromIdea(currentIdea.value);
    }
  } else if (stepNo === 4 || stepNo === 6) {
    if (!siteRuntime.value) {
      busy.value = true;
      busyMessage.value = '라이브 멀티사이트를 프로비저닝하고 있습니다...';
      try {
        const rt = await executeScaffoldWithRecovery(targetIdeaId);
        if (selectedIdeaId.value !== targetIdeaId) return;
        siteRuntime.value = rt;
        syncVisualFormFromRuntime(siteRuntime.value);
      } catch (err) {
        if (selectedIdeaId.value !== targetIdeaId) return;
        reportError(
          err,
          '사이트 프로비저닝에 실패했습니다',
          `${stepNo}단계 사이트 런타임을 구성하는 중 오류가 발생했습니다.`,
          () => goToStep(stepNo),
        );
      } finally {
        busy.value = false;
        busyMessage.value = '';
      }
    } else {
      syncVisualFormFromRuntime(siteRuntime.value);
    }
  } else if (stepNo === 5) {
    busy.value = true;
    try {
      if (!siteRuntime.value) {
        busyMessage.value = '라이브 멀티사이트를 프로비저닝하고 있습니다...';
        const rt = await executeScaffoldWithRecovery(targetIdeaId);
        if (selectedIdeaId.value !== targetIdeaId) return;
        siteRuntime.value = rt;
        syncVisualFormFromRuntime(siteRuntime.value);
      }
      if (activeSiteSlug.value) {
        busyMessage.value = 'WebIDE 프로젝트 소스 파일을 불러오고 있습니다...';
        await loadWebIde(activeSiteSlug.value);
      }
    } catch (err) {
      if (selectedIdeaId.value !== targetIdeaId) return;
      reportError(
        err,
        'WebIDE 코드 편집기 준비에 실패했습니다',
        '프로젝트 소스 파일을 불러오는 중 오류가 발생했습니다.',
        () => goToStep(5),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }
}

async function saveCurrentIdeaAndGoStep2() {
  if (busy.value) return;
  if (!formStep1.value.promptText.trim()) {
    showToast('아이디어 상세 설명(프롬프트)을 입력해 주세요.', 'error');
    return;
  }
  if (!selectedIdeaId.value) {
    await createNewIdeaFromStep1();
    return;
  }
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = '1단계 아이디어 설정을 저장하고 있습니다...';
  const prevPrompt = currentIdea.value?.promptText || '';
  const promptChanged = prevPrompt.trim() !== formStep1.value.promptText.trim();
  const prevTags = JSON.stringify(currentIdea.value?.tags || []);
  const nextTags = JSON.stringify(formStep1.value.tags || []);
  const prevFeatures = JSON.stringify(currentIdea.value?.enabledFeatures || []);
  const nextFeatures = JSON.stringify(enabledFeatures.value || []);
  const step1Changed =
    promptChanged ||
    (currentIdea.value?.title || '').trim() !== formStep1.value.title.trim() ||
    (currentIdea.value?.siteType || '') !== formStep1.value.siteType ||
    (currentIdea.value?.category || 'AUTO') !== (formStep1.value.category || 'AUTO') ||
    prevTags !== nextTags ||
    prevFeatures !== nextFeatures;

  let willGenNames = promptChanged || nameSuggestions.value.length < 8;
  let willGenLogos = promptChanged || logoSuggestions.value.length < 6;

  busySteps.value = [
    { label: '1. 아이디어 설정 확인·저장', status: 'active' },
    { label: '2. AI 브랜드 네이밍 8종 생성', status: willGenNames ? 'pending' : 'done' },
    { label: '3. 벡터 SVG 로고 6종 생성', status: willGenLogos ? 'pending' : 'done' },
  ];

  try {
    try {
      if (enabledFeatures.value.length === 0) {
        currentIdea.value = await kkumzApi.analyzeIdeaIntent(selectedIdeaId.value, {
          title: formStep1.value.title,
          promptText: formStep1.value.promptText,
          category: formStep1.value.category,
          tags: formStep1.value.tags,
          applyExpandedPrompt: false,
          applyRecommendedFeatures: true,
        });
        if (Array.isArray(currentIdea.value?.enabledFeatures)) {
          enabledFeatures.value = [...currentIdea.value.enabledFeatures];
        }
        if (currentIdea.value?.siteType) {
          formStep1.value.siteType = currentIdea.value.siteType;
        }
        if (currentIdea.value?.aiIntentAnalysis) {
          aiIntentAnalysis.value = { ...currentIdea.value.aiIntentAnalysis };
        }
        ideas.value = (await kkumzApi.listIdeas()) || [];
      } else if (step1Changed) {
        currentIdea.value = await kkumzApi.updateSpec(selectedIdeaId.value, {
          title: formStep1.value.title,
          promptText: formStep1.value.promptText,
          siteType: formStep1.value.siteType,
          category: formStep1.value.category,
          tags: formStep1.value.tags,
          enabledFeatures: enabledFeatures.value,
          aiIntentAnalysis: aiIntentAnalysis.value,
          specMarkdown: editableSpecMarkdown.value,
        });
        ideas.value = (await kkumzApi.listIdeas()) || [];
      } else {
        currentIdea.value = await kkumzApi.getIdeaDetail(selectedIdeaId.value);
      }
      willGenNames = promptChanged || nameSuggestions.value.length < 8;
      willGenLogos = promptChanged || logoSuggestions.value.length < 6;
      if (busySteps.value[0]) busySteps.value[0].status = 'done';
      if (busySteps.value[1]) busySteps.value[1].status = willGenNames ? 'pending' : 'done';
      if (busySteps.value[2]) busySteps.value[2].status = willGenLogos ? 'pending' : 'done';
      syncCustomBrandFromSelection(currentIdea.value);
    } catch (err) {
      reportError(
        err,
        '아이디어 설정 저장에 실패했습니다',
        '1단계 프로젝트 정보를 저장하는 중 서버 오류가 발생했습니다.',
        () => saveCurrentIdeaAndGoStep2(),
      );
      return;
    }

    // 1단계 저장 완료 즉시 2단계 화면으로 전환하여 네이밍 8종 + 로고 6종 동시 병렬 생성 진행
    activeStep.value = 2;

    if (willGenNames || willGenLogos) {
      const { nameErr, logoErr } = await runStep2GenerationsParallel(
        willGenNames,
        willGenLogos,
        promptChanged,
        1,
        2,
      );
      if (nameErr) {
        reportError(
          nameErr,
          'AI 브랜드 네이밍 생성 중 오류가 발생했습니다',
          '1단계 아이디어는 정상 저장되어 2단계로 이동했으나, AI 브랜드 네이밍 생성 요청이 실패했습니다. [다시 시도] 또는 상단 [네이밍 8종 재생성]을 눌러주세요.',
          () => regenerateNames(),
        );
        return;
      }
      if (logoErr) {
        reportError(
          logoErr,
          'AI 벡터 SVG 로고 생성 중 오류가 발생했습니다',
          '아이디어 저장 및 브랜드 네이밍 8종은 완료되었으나, SVG 로고 생성 요청이 실패했습니다. [다시 시도] 또는 상단 [SVG 로고 6종 재생성]을 눌러주세요.',
          () => regenerateLogos(),
        );
        return;
      }
    }
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function createNewIdeaFromStep1() {
  if (busy.value) return;
  if (!formStep1.value.promptText.trim()) {
    showToast('아이디어 프롬프트를 입력해 주세요.', 'error');
    return;
  }
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = '새 브랜드 프로젝트를 생성하고 있습니다...';
  busySteps.value = [
    { label: '1. 신규 프로젝트 생성', status: 'active' },
    { label: '2. AI 브랜드 네이밍 8종 병렬 생성', status: 'pending' },
    { label: '3. 벡터 SVG 로고 6종 병렬 생성', status: 'pending' },
  ];
  try {
    const created = await kkumzApi.createIdea({
      title: formStep1.value.title,
      promptText: formStep1.value.promptText,
      siteType: formStep1.value.siteType,
      category: formStep1.value.category,
      tags: formStep1.value.tags,
      enabledFeatures: enabledFeatures.value,
      visibility: 'PUBLIC',
    });
    ideas.value = (await kkumzApi.listIdeas()) || [];
    await onSelectIdea(created.id, true);
    if (busySteps.value[0]) busySteps.value[0].status = 'done';

    // 프로젝트 생성 완료 즉시 2단계로 전환하여 네이밍 8종 + 로고 6종 동시 병렬 생성
    activeStep.value = 2;

    const { nameErr, logoErr } = await runStep2GenerationsParallel(true, true, true, 1, 2);
    if (nameErr) {
      reportError(
        nameErr,
        '새 프로젝트는 생성되었으나 AI 네이밍 생성에 실패했습니다',
        'AI 브랜드 네이밍 생성 중 서버 오류가 발생했습니다. [다시 시도]를 눌러 네이밍을 생성해 주세요.',
        () => regenerateNames(),
      );
      return;
    }
    if (logoErr) {
      reportError(
        logoErr,
        'AI 벡터 SVG 로고 생성 중 오류가 발생했습니다',
        '프로젝트 및 네이밍 8종은 준비되었으나 로고 생성 중 서버 오류가 발생했습니다. [다시 시도]를 눌러 로고를 생성해 주세요.',
        () => regenerateLogos(),
      );
      return;
    }
  } catch (err) {
    reportError(
      err,
      '새 브랜드 프로젝트 생성에 실패했습니다',
      '프로젝트를 생성하는 중 서버 오류가 발생했습니다.',
      () => createNewIdeaFromStep1(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function regenerateNames() {
  if (!selectedIdeaId.value || busy.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = 'AI가 브랜드 네이밍 후보 8종을 새로 생성하고 있습니다...';
  try {
    await performGenerateNames(true);
    showToast('AI 브랜드 네이밍 8종이 새로 생성되었습니다.', 'success');
  } catch (err) {
    reportError(
      err,
      'AI 브랜드 네이밍 생성에 실패했습니다',
      '브랜드 네이밍 8종을 생성하는 중 오류가 발생했습니다.',
      () => regenerateNames(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function regenerateLogos() {
  if (!selectedIdeaId.value || busy.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = 'AI가 벡터 SVG 로고 후보 6종을 새로 생성하고 있습니다...';
  try {
    await performGenerateLogos(true);
    showToast('벡터 SVG 로고 6종이 새로 생성되었습니다.', 'success');
  } catch (err) {
    reportError(
      err,
      'AI 벡터 SVG 로고 생성에 실패했습니다',
      '벡터 SVG 로고 6종을 생성하는 중 오류가 발생했습니다.',
      () => regenerateLogos(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

function selectNameCandidate(name: any) {
  if (busy.value) return;
  selectedNameId.value = name.id;
  customBrandForm.value.brandName = name.brandName || '';
  customBrandForm.value.siteSlug = name.siteSlug || '';
  customBrandForm.value.pronunciation = name.pronunciation || '';
  customBrandForm.value.brandStory = sanitizeBrandStoryText(name.brandStory || '');
  const baseSvg = selectedLogoObj.value?.svgCode || customBrandForm.value.svgCode || '';
  if (baseSvg) {
    customBrandForm.value.svgCode = renderLogoWithBrand(baseSvg, name.brandName);
  }
}

function selectLogoCandidate(logo: any) {
  if (busy.value) return;
  selectedLogoId.value = logo.id;
  const pal = logo.colorPalette || {};
  customBrandForm.value.colorPalette = {
    primary: String(pal.primary || '#14532d'),
    secondary: String(pal.secondary || '#166534'),
    accent: String(pal.accent || '#d97706'),
    surface: String(pal.surface || '#f8fafc'),
    radius: String(pal.radius || '12px'),
  };
  customBrandForm.value.svgCode = renderLogoWithBrand(logo.svgCode || '', previewBrandName.value);
}

async function confirmBrandAndNext() {
  if (!selectedIdeaId.value || busy.value) return;
  const finalBrandName = customBrandForm.value.brandName.trim() || selectedNameObj.value?.brandName || '';
  if (!finalBrandName) {
    showToast('브랜드명을 선택하거나 직접 입력해 주세요.', 'error');
    return;
  }
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = '선택 및 커스텀한 브랜드 아이덴티티를 확정하고 3단계 기획서를 준비하고 있습니다...';
  busySteps.value = [
    { label: '1. 브랜드 아이덴티티 확정', status: 'active' },
    { label: '2. 3단계 AI 기능명세서 · 화면설계 생성', status: 'pending' },
  ];
  try {
    const finalSvg = renderLogoWithBrand(
      customBrandForm.value.svgCode || selectedLogoObj.value?.svgCode || '',
      finalBrandName,
    );
    currentIdea.value = await kkumzApi.selectBrand(selectedIdeaId.value, {
      nameSuggestionId: selectedNameId.value,
      logoSuggestionId: selectedLogoId.value,
      brandName: finalBrandName,
      siteSlug: customBrandForm.value.siteSlug.trim() || selectedNameObj.value?.siteSlug || '',
      pronunciation: customBrandForm.value.pronunciation.trim() || selectedNameObj.value?.pronunciation || '',
      brandStory: customBrandForm.value.brandStory.trim() || sanitizeBrandStoryText(selectedNameObj.value?.brandStory || ''),
      logoSvg: finalSvg,
      designTokens: { ...customBrandForm.value.colorPalette },
    });
    if (busySteps.value[0]) busySteps.value[0].status = 'done';
    if (busySteps.value[1]) busySteps.value[1].status = 'active';
    activeStep.value = 3;
    busyMessage.value = `'${finalBrandName}' 브랜드에 맞춰 3단계 AI 기능명세서 및 화면 설계를 동기화하고 있습니다...`;
    currentIdea.value = await kkumzApi.generateSpec(selectedIdeaId.value, false);
    if (busySteps.value[1]) busySteps.value[1].status = 'done';
    ideas.value = (await kkumzApi.listIdeas()) || [];
    syncStep3FromIdea(currentIdea.value);
  } catch (err) {
    reportError(
      err,
      '브랜드 확정 처리에 실패했습니다',
      '선택한 브랜드 네이밍·로고를 저장하는 중 오류가 발생했습니다.',
      () => confirmBrandAndNext(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function regenerateSpec() {
  if (!selectedIdeaId.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = 'AI가 서비스 기능명세서 및 화면 설계 블루프린트를 새로 생성하고 있습니다...';
  try {
    currentIdea.value = await kkumzApi.generateSpec(selectedIdeaId.value, true);
    syncStep3FromIdea(currentIdea.value);
    showToast('AI 기능명세서 및 화면 설계 블루프린트가 새로 생성되었습니다.', 'success');
  } catch (err) {
    reportError(
      err,
      'AI 기획서 생성에 실패했습니다',
      '서비스 기능명세서 및 화면 설계를 생성하는 중 오류가 발생했습니다.',
      () => regenerateSpec(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function saveSpecVersion() {
  if (!selectedIdeaId.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = '기획서 및 화면 설계 블루프린트 스냅샷을 저장하고 있습니다...';
  try {
    if (!specEditMode.value && editableHighlights.value.length > 0) {
      syncHighlightsToMarkdown();
    }
    currentIdea.value = await kkumzApi.updateSpec(selectedIdeaId.value, {
      title: formStep1.value.title,
      promptText: formStep1.value.promptText,
      siteType: formStep1.value.siteType,
      category: formStep1.value.category,
      tags: formStep1.value.tags,
      specMarkdown: editableSpecMarkdown.value,
      screens: editableScreens.value,
    });
    syncStep3FromIdea(currentIdea.value);
    showToast('기획서 및 화면 설계 블루프린트 스냅샷이 저장되었습니다.', 'success');
  } catch (err) {
    reportError(
      err,
      '기획서 스냅샷 저장에 실패했습니다',
      '기획서 변경사항을 저장하는 중 오류가 발생했습니다.',
      () => saveSpecVersion(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function rollbackSpecVersion(versionNo: number) {
  if (!selectedIdeaId.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = `v${versionNo} 기획서 버전으로 복원하고 있습니다...`;
  try {
    currentIdea.value = await kkumzApi.saveOrRollbackIdeaVersion(selectedIdeaId.value, {
      rollbackToVersionNo: versionNo,
    });
    syncStep3FromIdea(currentIdea.value);
    showToast(`기획서 및 화면 블루프린트가 v${versionNo} 버전으로 복원되었습니다.`, 'success');
  } catch (err) {
    reportError(
      err,
      '기획서 버전 복원에 실패했습니다',
      `v${versionNo} 버전으로 되돌리는 중 오류가 발생했습니다.`,
      () => rollbackSpecVersion(versionNo),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function provisionAndGoStep4() {
  const targetIdeaId = selectedIdeaId.value;
  if (!targetIdeaId || busy.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = '기획서·화면설계를 반영하여 라이브 멀티사이트를 프로비저닝하고 있습니다...';
  busySteps.value = [
    { label: '1. 브랜드 아이덴티티 · 기획서 블루프린트 저장', status: 'active' },
    { label: '2. SyncShop · SyncCMS 멀티사이트 프로비저닝 및 초기 콘텐츠 구성', status: 'pending' },
  ];
  try {
    if (editableScreens.value.length > 0 || editableSpecMarkdown.value) {
      currentIdea.value = await kkumzApi.updateSpec(targetIdeaId, {
        title: formStep1.value.title,
        promptText: formStep1.value.promptText,
        siteType: formStep1.value.siteType,
        category: formStep1.value.category,
        tags: formStep1.value.tags,
        specMarkdown: editableSpecMarkdown.value,
        screens: editableScreens.value,
      });
    }
    if (busySteps.value[0]) busySteps.value[0].status = 'done';
    if (busySteps.value[1]) busySteps.value[1].status = 'active';
    busyMessage.value = '업종 맞춤 초기 콘텐츠와 SDUI 레이아웃 런타임을 생성하고 있습니다...';

    const rt = await executeScaffoldWithRecovery(targetIdeaId);
    if (selectedIdeaId.value !== targetIdeaId) return;
    if (busySteps.value[1]) busySteps.value[1].status = 'done';

    siteRuntime.value = rt;
    syncVisualFormFromRuntime(siteRuntime.value);
    currentIdea.value = await kkumzApi.getIdeaDetail(targetIdeaId);
    ideas.value = (await kkumzApi.listIdeas()) || [];
    activeStep.value = 4;
  } catch (err) {
    if (selectedIdeaId.value !== targetIdeaId) return;
    reportError(
      err,
      '라이브 멀티사이트 프로비저닝에 실패했습니다',
      '사이트를 생성하고 초기 데이터를 구성하는 중 오류가 발생했습니다.',
      () => provisionAndGoStep4(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

function focusStep4Editor(tab: 'AI_CHAT' | 'VISUAL_THEME' | 'CATALOG_EDIT' | 'FORM_EDIT') {
  step4Tab.value = tab;
}

function moveEditableProduct(idx: number, delta: number) {
  const target = idx + delta;
  if (target < 0 || target >= editableProducts.value.length) return;
  const tmp = editableProducts.value[idx];
  editableProducts.value[idx] = editableProducts.value[target];
  editableProducts.value[target] = tmp;
}

function moveEditableArticle(idx: number, delta: number) {
  const target = idx + delta;
  if (target < 0 || target >= editableArticles.value.length) return;
  const tmp = editableArticles.value[idx];
  editableArticles.value[idx] = editableArticles.value[target];
  editableArticles.value[target] = tmp;
}

async function applyChatRefine() {
  if (!siteRuntime.value || !chatInstruction.value.trim()) {
    if (!chatInstruction.value.trim()) {
      showToast('수정할 디자인·문구 지시 내용을 입력해 주세요.', 'error');
    }
    return;
  }
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = 'AI 대화형 수정 사항을 프리뷰에 실시간 반영하고 있습니다...';
  try {
    siteRuntime.value = await kkumzApi.chatRefineSite(siteRuntime.value.siteSlug, {
      instruction: chatInstruction.value.trim(),
    });
    syncVisualFormFromRuntime(siteRuntime.value);
    chatInstruction.value = '';
    showToast('대화형 디자인 수정 사항이 프리뷰에 즉시 반영되었습니다.', 'success');
  } catch (err) {
    reportError(
      err,
      '대화형 디자인 수정에 실패했습니다',
      'AI 수정 지시를 반영하는 중 오류가 발생했습니다.',
      () => applyChatRefine(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function applyQuickTheme(instruction: string) {
  chatInstruction.value = instruction;
  await applyChatRefine();
}

async function ensureIdeaForUpload(): Promise<string> {
  if (selectedIdeaId.value) {
    return selectedIdeaId.value;
  }
  const prompt = formStep1.value.promptText.trim() || formStep1.value.title.trim();
  if (!prompt) {
    throw new Error('먼저 프로젝트 제목 또는 아이디어 설명을 한 줄 이상 입력해 주세요.');
  }
  const created = await kkumzApi.createIdea({
    title: formStep1.value.title || prompt,
    promptText: prompt,
    siteType: formStep1.value.siteType,
    category: formStep1.value.category,
    tags: formStep1.value.tags,
    visibility: 'PUBLIC',
  });
  ideas.value = (await kkumzApi.listIdeas()) || [];
  selectedIdeaId.value = created.id;
  currentIdea.value = created;
  return created.id;
}

function triggerStep1FileSelect() {
  step1FileInputRef.value?.click();
}

async function onStep1FileInputChange(e: Event) {
  const input = e.target as HTMLInputElement;
  const files = input.files ? Array.from(input.files) : [];
  input.value = '';
  if (files.length > 0) {
    await handleUploadIdeaFiles(files, uploadAssetType.value);
  }
}

async function onStep1FileDrop(e: DragEvent) {
  isDraggingFiles.value = false;
  const files = e.dataTransfer?.files ? Array.from(e.dataTransfer.files) : [];
  if (files.length > 0) {
    await handleUploadIdeaFiles(files, uploadAssetType.value);
  }
}

async function handleUploadIdeaFiles(files: File[], targetAssetType = 'AUTO') {
  if (!files.length) return;
  errorBanner.value = null;
  assetUploading.value = true;
  busy.value = true;
  busyMessage.value = `${files.length}개의 보유 사진·문서 파일을 업로드하고 텍스트/메타데이터를 분석하고 있습니다...`;
  try {
    const ideaId = await ensureIdeaForUpload();
    await kkumzApi.uploadIdeaAssets(ideaId, files, targetAssetType, '', true);
    const detail = await kkumzApi.getIdeaDetail(ideaId);
    currentIdea.value = detail;
    if (detail.promptText) {
      formStep1.value.promptText = detail.promptText;
    }
    const selLogo =
      (detail.logoSuggestions || []).find((l: any) => l.isSelected) ||
      detail.logoSuggestions?.[0];
    if (selLogo) {
      selectedLogoId.value = selLogo.id;
    }
    syncCustomBrandFromSelection(detail);
    if (activeSiteSlug.value) {
      try {
        siteRuntime.value = await kkumzApi.getSiteRuntime(activeSiteSlug.value);
        syncVisualFormFromRuntime(siteRuntime.value);
      } catch {
        // ignore if not provisioned
      }
    }
    showToast(
      `파일 ${files.length}건이 업로드되어 프로젝트 자산 보관함 및 AI 프롬프트에 자동 반영되었습니다!`,
      'success',
    );
  } catch (err: any) {
    reportError(
      err,
      '파일 업로드에 실패했습니다',
      err?.message || '선택한 사진·문서 파일을 업로드하는 중 오류가 발생했습니다.',
    );
  } finally {
    assetUploading.value = false;
    busy.value = false;
    busyMessage.value = '';
  }
}

async function onAssetMetaChange(asset: any) {
  if (!selectedIdeaId.value || !asset?.id) return;
  try {
    await kkumzApi.updateIdeaAsset(selectedIdeaId.value, asset.id, {
      assetType: asset.assetType,
      caption: asset.caption || '',
      sortOrder: asset.sortOrder,
    });
    const detail = await kkumzApi.getIdeaDetail(selectedIdeaId.value);
    currentIdea.value = detail;
    if (activeSiteSlug.value) {
      siteRuntime.value = await kkumzApi.getSiteRuntime(activeSiteSlug.value);
      syncVisualFormFromRuntime(siteRuntime.value);
    }
    showToast('자산 용도 및 설명이 저장되었습니다.', 'success');
  } catch (err) {
    reportError(err, '자산 정보 수정에 실패했습니다', '파일 용도 및 설명을 변경하는 중 오류가 발생했습니다.');
  }
}

async function applyAssetTextToPrompt(asset: any) {
  if (!selectedIdeaId.value || !asset?.id) return;
  busy.value = true;
  busyMessage.value = '업로드 자료 내용을 AI 프롬프트에 반영하고 있습니다...';
  try {
    const detail = await kkumzApi.applyAssetToPrompt(selectedIdeaId.value, asset.id);
    currentIdea.value = detail;
    formStep1.value.promptText = detail.promptText || formStep1.value.promptText;
    showToast('자료 설명 및 추출 텍스트가 AI 프롬프트에 추가되었습니다!', 'success');
  } catch (err) {
    reportError(err, '프롬프트 반영에 실패했습니다', '자산 내용을 프롬프트에 반영하는 중 오류가 발생했습니다.');
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function removeIdeaAsset(asset: any) {
  if (!selectedIdeaId.value || !asset?.id) return;
  busy.value = true;
  busyMessage.value = '선택한 업로드 파일을 삭제하고 있습니다...';
  try {
    await kkumzApi.deleteIdeaAsset(selectedIdeaId.value, asset.id);
    const detail = await kkumzApi.getIdeaDetail(selectedIdeaId.value);
    currentIdea.value = detail;
    if (activeSiteSlug.value) {
      siteRuntime.value = await kkumzApi.getSiteRuntime(activeSiteSlug.value);
      syncVisualFormFromRuntime(siteRuntime.value);
    }
    showToast('업로드 파일이 삭제되었습니다.', 'info');
  } catch (err) {
    reportError(err, '파일 삭제에 실패했습니다', '업로드 자산을 삭제하는 중 오류가 발생했습니다.');
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

function triggerStep2LogoUpload() {
  step2LogoInputRef.value?.click();
}

async function onStep2LogoFileChange(e: Event) {
  const input = e.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = '';
  if (!file || !selectedIdeaId.value) return;
  errorBanner.value = null;
  assetUploading.value = true;
  busy.value = true;
  busyMessage.value = '보유 로고 파일을 업로드하고 브랜드 키트에 등록하고 있습니다...';
  try {
    const detail = await kkumzApi.uploadCustomBrandLogo(
      selectedIdeaId.value,
      file,
      previewBrandName.value,
    );
    currentIdea.value = detail;
    const selLogo =
      (detail.logoSuggestions || []).find((l: any) => l.isSelected) ||
      detail.logoSuggestions?.[0];
    if (selLogo) {
      selectedLogoId.value = selLogo.id;
    }
    syncCustomBrandFromSelection(detail);
    showToast('보유 로고가 업로드되어 현재 브랜드 로고로 즉시 선택되었습니다!', 'success');
  } catch (err) {
    reportError(err, '보유 로고 업로드에 실패했습니다', '로고 파일을 등록하는 중 오류가 발생했습니다.');
  } finally {
    assetUploading.value = false;
    busy.value = false;
    busyMessage.value = '';
  }
}

function triggerStep4HeroUpload() {
  step4HeroInputRef.value?.click();
}

async function onStep4HeroFileChange(e: Event) {
  const input = e.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = '';
  if (!file || !selectedIdeaId.value) return;
  assetUploading.value = true;
  busy.value = true;
  busyMessage.value = '메인 히어로 배너 사진을 업로드하고 프리뷰에 반영하고 있습니다...';
  try {
    const uploaded = await kkumzApi.uploadIdeaAssets(
      selectedIdeaId.value,
      [file],
      'HERO',
      visualForm.value.heroTitle || '',
      false,
    );
    const first = uploaded?.[0];
    if (first?.fileUrl) {
      visualForm.value.heroBannerImage = first.fileUrl;
      visualForm.value.heroBannerCaption = first.caption || first.originalName || '';
    }
    currentIdea.value = await kkumzApi.getIdeaDetail(selectedIdeaId.value);
    await saveVisualThemeOverrides();
  } catch (err) {
    reportError(err, '배너 사진 업로드에 실패했습니다', '히어로 배너 이미지를 업로드하는 중 오류가 발생했습니다.');
  } finally {
    assetUploading.value = false;
    busy.value = false;
    busyMessage.value = '';
  }
}

function onPickHeroAsset(fileUrl: string) {
  visualForm.value.heroBannerImage = fileUrl || '';
  const matched = imageAssets.value.find((a) => a.fileUrl === fileUrl);
  if (matched?.caption) {
    visualForm.value.heroBannerCaption = matched.caption;
  }
}

function triggerCatalogItemImageUpload(kind: 'PRODUCT' | 'ARTICLE', index: number) {
  catalogUploadTarget.value = { kind, index };
  step4CatalogImageInputRef.value?.click();
}

async function onStep4CatalogImageFileChange(e: Event) {
  const input = e.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = '';
  const target = catalogUploadTarget.value;
  catalogUploadTarget.value = null;
  if (!file || !target || !selectedIdeaId.value) return;

  assetUploading.value = true;
  busy.value = true;
  busyMessage.value = '상품·매거진 사진을 업로드하고 있습니다...';
  try {
    const itemCaption =
      target.kind === 'PRODUCT'
        ? editableProducts.value[target.index]?.name || ''
        : editableArticles.value[target.index]?.title || '';
    const uploaded = await kkumzApi.uploadIdeaAssets(
      selectedIdeaId.value,
      [file],
      target.kind,
      itemCaption,
      false,
    );
    const first = uploaded?.[0];
    if (first?.fileUrl) {
      if (target.kind === 'PRODUCT' && editableProducts.value[target.index]) {
        editableProducts.value[target.index].pic = first.fileUrl;
      } else if (target.kind === 'ARTICLE' && editableArticles.value[target.index]) {
        editableArticles.value[target.index].cover = first.fileUrl;
      }
    }
    currentIdea.value = await kkumzApi.getIdeaDetail(selectedIdeaId.value);
    showToast('사진이 업로드되어 항목에 적용되었습니다. 하단 [상품·매거진 상세 변경사항 즉시 반영]을 눌러 저장하세요.', 'success');
  } catch (err) {
    reportError(err, '항목 사진 업로드에 실패했습니다', '사진을 업로드하는 중 오류가 발생했습니다.');
  } finally {
    assetUploading.value = false;
    busy.value = false;
    busyMessage.value = '';
  }
}

function triggerStep4ExtraAssetUpload() {
  step4ExtraAssetInputRef.value?.click();
}

async function onStep4ExtraAssetFileChange(e: Event) {
  const input = e.target as HTMLInputElement;
  const files = input.files ? Array.from(input.files) : [];
  input.value = '';
  if (!files.length) return;
  await handleUploadIdeaFiles(files, 'AUTO');
}

async function saveVisualThemeOverrides() {
  if (!siteRuntime.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = '배너 문구·브랜드·사진·컬러·섹션 설정을 프리뷰에 적용하고 있습니다...';
  try {
    const updatedLogo = renderLogoWithBrand(
      siteRuntime.value.logoSvg || previewLogoSvg.value,
      visualForm.value.siteName || siteRuntime.value.siteName,
    );
    siteRuntime.value = await kkumzApi.chatRefineSite(siteRuntime.value.siteSlug, {
      instruction: '',
      siteName: visualForm.value.siteName,
      brandSlogan: visualForm.value.brandSlogan,
      logoSvg: updatedLogo,
      tokenOverrides: {
        primary: visualForm.value.primaryColor,
        primaryColor: visualForm.value.primaryColor,
        secondary: visualForm.value.secondaryColor,
        secondaryColor: visualForm.value.secondaryColor,
        accent: visualForm.value.accentColor,
        accentColor: visualForm.value.accentColor,
        surface: visualForm.value.surfaceColor,
        surfaceColor: visualForm.value.surfaceColor,
        radius: visualForm.value.radius,
      },
      layoutOverrides: {
        customBadge: visualForm.value.customBadge,
        heroTitle: visualForm.value.heroTitle,
        heroSubtitle: visualForm.value.heroSubtitle,
        ctaText: visualForm.value.ctaText,
        primaryCta: visualForm.value.ctaText,
        heroBannerImage: visualForm.value.heroBannerImage,
        heroBannerCaption: visualForm.value.heroBannerCaption,
        showFlashDeal: visualForm.value.showFlashDeal,
        showPhotoGallery: visualForm.value.showPhotoGallery,
        showCmsArticles: visualForm.value.showCmsArticles,
        showCmsForm: visualForm.value.showCmsForm,
        showAiConcierge: visualForm.value.showAiConcierge,
      },
    });
    syncVisualFormFromRuntime(siteRuntime.value);
    showToast('배너·브랜드·컬러·섹션 노출 설정이 라이브 프리뷰에 즉시 반영되었습니다!', 'success');
  } catch (err) {
    reportError(
      err,
      '배너·컬러 설정 적용에 실패했습니다',
      '비주얼 테마 설정을 저장하는 중 오류가 발생했습니다.',
      () => saveVisualThemeOverrides(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function saveFormOverrides() {
  if (!siteRuntime.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = '온라인 예약·신청 폼 및 AI 컨시어지 설정을 저장하고 있습니다...';
  try {
    const existingForm = siteRuntime.value.cmsForm || {};
    const customForm = {
      ...existingForm,
      title: editableCmsForm.value.title,
      formName: editableCmsForm.value.title,
      description: editableCmsForm.value.description,
      submitText: editableCmsForm.value.submitText,
    };
    siteRuntime.value = await kkumzApi.chatRefineSite(siteRuntime.value.siteSlug, {
      instruction: '',
      layoutOverrides: {
        customCmsForm: customForm,
        conciergeGreeting: editableCmsForm.value.conciergeGreeting,
      },
    });
    syncVisualFormFromRuntime(siteRuntime.value);
    showToast('온라인 예약·신청 폼 및 컨시어지 안내 문구가 즉시 반영되었습니다!', 'success');
  } catch (err) {
    reportError(
      err,
      '예약·신청 폼 저장에 실패했습니다',
      '온라인 신청 폼 정보를 저장하는 중 오류가 발생했습니다.',
      () => saveFormOverrides(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

function addEditableProduct() {
  editableProducts.value.push({
    id: Date.now(),
    name: '',
    pic: '',
    price: 0,
    promotionPrice: 0,
    subTitle: '',
    stock: 10,
    unit: '개',
    brandName: siteRuntime.value?.siteName || '',
    productCategoryName: '',
  });
}

function addEditableArticle() {
  editableArticles.value.push({
    id: Date.now(),
    title: '',
    author: siteRuntime.value?.siteName ? `${siteRuntime.value.siteName} 에디터` : '호스트',
    description: '',
    summary: '',
    bodyHtml: '',
    categoryName: '브랜드 저널',
    cover: '',
    clicks: 0,
    viewCount: 0,
  });
}

async function saveCatalogAndArticlesOverrides() {
  if (!siteRuntime.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = '진열 상품 및 매거진 아티클 구성을 업데이트하고 있습니다...';
  try {
    const normalizedProducts = editableProducts.value.map((p) => ({
      ...p,
      price: Number(p.price || p.promotionPrice || 0),
      promotionPrice: Number(p.promotionPrice || p.price || 0),
    }));
    const normalizedArticles = editableArticles.value.map((a) => ({
      ...a,
      summary: a.description || a.summary || '',
      bodyHtml: a.bodyHtml || `<p>${a.description || a.summary || ''}</p>`,
    }));
    siteRuntime.value = await kkumzApi.chatRefineSite(siteRuntime.value.siteSlug, {
      instruction: '',
      layoutOverrides: {
        customProducts: normalizedProducts,
        customArticles: normalizedArticles,
      },
    });
    syncVisualFormFromRuntime(siteRuntime.value);
    showToast('진열 상품 및 매거진 아티클 구성이 즉시 업데이트되었습니다.', 'success');
  } catch (err) {
    reportError(
      err,
      '상품·매거진 업데이트에 실패했습니다',
      '변경한 상품 및 아티클 정보를 반영하는 중 오류가 발생했습니다.',
      () => saveCatalogAndArticlesOverrides(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function loadWebIde(slug: string) {
  const data = await kkumzApi.getWebIdeFiles(slug);
  webIdeData.value = data;
  webIdeFiles.value = (data.files || []).map((f: any) => ({ ...f }));
  activeFileIndex.value = 0;
  if (data.gitRepo) {
    gitForm.value.repoOwner = data.gitRepo.repoOwner || 'kkumz-sites';
    gitForm.value.repoName = data.gitRepo.repoName || slug;
    gitForm.value.defaultBranch = data.gitRepo.defaultBranch || 'main';
    gitForm.value.visibility = data.gitRepo.visibility || 'PRIVATE';
  } else if (!gitForm.value.repoName) {
    gitForm.value.repoName = slug;
  }
}

async function provisionGitRepository() {
  if (!activeSiteSlug.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = 'Git 리포지토리 스캐폴딩 및 사용자 전용 배포 키(Ed25519 SSH + Site API Key)를 생성하고 있습니다...';
  try {
    const res = await kkumzApi.provisionSiteGitRepo(activeSiteSlug.value, {
      repoOwner: gitForm.value.repoOwner.trim() || 'kkumz-sites',
      repoName: gitForm.value.repoName.trim() || activeSiteSlug.value,
      defaultBranch: gitForm.value.defaultBranch.trim() || 'main',
      visibility: gitForm.value.visibility,
      githubPatToken: gitForm.value.githubPatToken.trim() || undefined,
      includeFullViteScaffold: true,
    });
    webIdeData.value = res;
    webIdeFiles.value = (res.files || []).map((f: any) => ({ ...f }));
    showGitConfigForm.value = false;
    showToast(
      res.notice || 'Git 리포지토리 생성 및 전용 개발 키(SSH Deploy Key + Site API Key) 발급이 완료되었습니다.',
      'success',
    );
  } catch (err) {
    reportError(
      err,
      'Git 리포지토리 및 전용 키 프로비저닝에 실패했습니다',
      '저장소 설정을 확인한 뒤 다시 시도해 주세요.',
      () => provisionGitRepository(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function pushWebIdeToGit() {
  if (!activeSiteSlug.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = '현재 WebIDE 코드를 검증하고 Git 리포지토리에 커밋·푸시하고 있습니다...';
  try {
    const res = await kkumzApi.pushSiteToGitRepo(activeSiteSlug.value, {
      files: webIdeFiles.value,
      commitMessage:
        webIdeCommitMsg.value.trim() ||
        `feat(studio): update ${activeSiteSlug.value} components & layout from WebIDE`,
      githubPatToken: gitForm.value.githubPatToken.trim() || undefined,
    });
    webIdeData.value = res;
    webIdeFiles.value = (res.files || []).map((f: any) => ({ ...f }));
    webIdeCommitMsg.value = '';
    showToast(res.notice || 'WebIDE 변경 사항이 Git 리포지토리 최신 커밋으로 푸시되었습니다.', 'success');
  } catch (err) {
    reportError(
      err,
      'Git 커밋 및 푸시 동기화에 실패했습니다',
      'WebIDE 코드 구문 또는 저장소 상태를 확인해 주세요.',
      () => pushWebIdeToGit(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function rotateGitDeveloperKeys() {
  if (!activeSiteSlug.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = '기존 키를 폐기하고 새로운 SSH Deploy Key 및 Site API Key를 재발급하고 있습니다...';
  try {
    const res = await kkumzApi.rotateSiteGitKeys(activeSiteSlug.value, {
      rotateSshKey: true,
      rotateSiteApiKey: true,
      githubPatToken: gitForm.value.githubPatToken.trim() || undefined,
    });
    webIdeData.value = res;
    webIdeFiles.value = (res.files || []).map((f: any) => ({ ...f }));
    showToast(
      res.notice || '사용자 전용 SSH Deploy Key 및 사이트 API Secret Key가 새로 발급되었습니다.',
      'success',
    );
  } catch (err) {
    reportError(
      err,
      '전용 개발 키 재발급에 실패했습니다',
      '잠시 후 다시 시도해 주세요.',
      () => rotateGitDeveloperKeys(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function copyTextToClipboard(text: string | undefined, label: string) {
  if (!text) return;
  try {
    await navigator.clipboard.writeText(text);
    showToast(`${label}가 클립보드에 복사되었습니다.`, 'success');
  } catch {
    showToast(`${label} 복사에 실패했습니다. 텍스트를 직접 선택해 복사해 주세요.`, 'error');
  }
}

function addNewWebIdeFile() {
  const nextIdx = webIdeFiles.value.length + 1;
  const openScript = '<' + 'script setup lang="ts">';
  const closeScript = '<' + '/script>';
  webIdeFiles.value.push({
    path: `src/components/CustomSection${nextIdx}.vue`,
    language: 'vue',
    content: `<template>\n  <section class="custom-section">\n    <h3>커스텀 섹션 ${nextIdx}</h3>\n  </section>\n</template>\n\n${openScript}\n${closeScript}\n`,
  });
  activeFileIndex.value = webIdeFiles.value.length - 1;
}

function removeWebIdeFile(idx: number) {
  if (webIdeFiles.value.length <= 1) return;
  webIdeFiles.value.splice(idx, 1);
  if (activeFileIndex.value >= webIdeFiles.value.length) {
    activeFileIndex.value = Math.max(0, webIdeFiles.value.length - 1);
  }
}

function formatActiveJsonFile() {
  const file = activeWebIdeFile.value;
  if (!file) return;
  try {
    const parsed = JSON.parse(file.content);
    file.content = JSON.stringify(parsed, null, 2);
    showToast('JSON 포맷이 깔끔하게 정렬되었습니다.', 'success');
  } catch {
    showToast('JSON 구문 오류가 있어 자동 정렬할 수 없습니다. 구문을 확인해 주세요.', 'error');
  }
}

async function syncStep4ToWebIde() {
  if (!activeSiteSlug.value) return;
  busy.value = true;
  busyMessage.value = '4단계 비주얼·상품 설정을 WebIDE 소스 파일에 동기화하고 있습니다...';
  try {
    await loadWebIde(activeSiteSlug.value);
    const themeIdx = webIdeFiles.value.findIndex((f) => f.path.endsWith('theme.json'));
    if (themeIdx >= 0 && siteRuntime.value) {
      webIdeFiles.value[themeIdx].content = JSON.stringify(
        {
          siteName: siteRuntime.value.siteName,
          siteSlug: siteRuntime.value.siteSlug,
          designTokens: siteRuntime.value.designTokens,
          layoutSchema: siteRuntime.value.layoutSchema,
        },
        null,
        2,
      );
      activeFileIndex.value = themeIdx;
    }
    showToast('4단계 최신 비주얼·테마 설정이 WebIDE 코드로 동기화되었습니다.', 'success');
  } catch (err) {
    reportError(err, 'WebIDE 동기화에 실패했습니다', '파일을 다시 불러오는 중 오류가 발생했습니다.');
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function saveAndBuildWebIde() {
  if (!activeSiteSlug.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = '코드를 저장하고 번들 빌드를 검증하고 있습니다...';
  try {
    const res = await kkumzApi.updateWebIdeFiles(activeSiteSlug.value, {
      files: webIdeFiles.value,
      commitMessage: webIdeCommitMsg.value.trim() || undefined,
    });
    webIdeData.value = res;
    webIdeFiles.value = (res.files || []).map((f: any) => ({ ...f }));
    webIdeCommitMsg.value = '';
    showToast(
      res.buildStatus === 'FAILED'
        ? '빌드 검증 중 구문 오류가 감지되었습니다. 우측 상단 AI 빌드 오류 자동 패치 실행 버튼으로 복구할 수 있습니다.'
        : '번들 빌드가 정상 완료되었습니다.',
      res.buildStatus === 'FAILED' ? 'error' : 'success',
    );
  } catch (err) {
    reportError(
      err,
      '코드 저장 및 번들 빌드에 실패했습니다',
      'WebIDE 파일 저장 및 빌드 요청 중 오류가 발생했습니다.',
      () => saveAndBuildWebIde(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function runAiFixBuild() {
  if (!activeSiteSlug.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = 'AI가 빌드 오류를 분석하고 자동 패치하고 있습니다...';
  try {
    const res = await kkumzApi.aiFixBuildError(activeSiteSlug.value);
    webIdeData.value = res;
    webIdeFiles.value = (res.files || []).map((f: any) => ({ ...f }));
    showToast('AI가 템플릿 구문 오류를 수정하고 빌드를 완료했습니다.', 'success');
  } catch (err) {
    reportError(
      err,
      'AI 빌드 오류 자동 패치에 실패했습니다',
      '구문 오류 자동 복구 요청 중 오류가 발생했습니다.',
      () => runAiFixBuild(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function executeDeploy() {
  if (!siteRuntime.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = '새 릴리즈 버전을 빌드하고 라이브 사이트에 배포하고 있습니다...';
  try {
    siteRuntime.value = await kkumzApi.deploySite(siteRuntime.value.siteSlug, {
      deployMode: deployForm.value.deployMode,
      releaseNote: deployForm.value.releaseNote,
      visibility: deployForm.value.visibility || 'PUBLIC',
      customDomain: deployForm.value.customDomain.trim() || undefined,
    });
    syncVisualFormFromRuntime(siteRuntime.value);
    deployForm.value.releaseNote = '';
    showToast(`v${siteRuntime.value.activeVersion} 라이브 릴리즈 배포가 완료되었습니다!`, 'success');
  } catch (err) {
    reportError(
      err,
      '라이브 배포 실행에 실패했습니다',
      '새 릴리즈 버전을 배포하는 중 오류가 발생했습니다.',
      () => executeDeploy(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function rollbackRelease(versionNo: number) {
  if (!siteRuntime.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = `라이브 사이트를 v${versionNo} 릴리즈로 롤백하고 있습니다...`;
  try {
    siteRuntime.value = await kkumzApi.rollbackSiteRelease(siteRuntime.value.siteSlug, versionNo);
    syncVisualFormFromRuntime(siteRuntime.value);
    showToast(`라이브 사이트가 v${versionNo} 릴리즈로 즉시 롤백되었습니다.`, 'success');
  } catch (err) {
    reportError(
      err,
      '릴리즈 롤백에 실패했습니다',
      `v${versionNo} 버전으로 롤백하는 중 오류가 발생했습니다.`,
      () => rollbackRelease(versionNo),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

onMounted(() => {
  initStudio();
});
</script>

<style scoped>
.studio-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f1f5f9;
}
.studio-topbar {
  height: 60px;
  background: #0f172a;
  color: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.topbar-left,
.topbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.back-btn {
  padding: 7px 12px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: #e2e8f0;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.divider {
  width: 1px;
  height: 22px;
  background: rgba(255, 255, 255, 0.16);
}
.project-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}
.selector-label {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 700;
}
.project-selector select {
  padding: 7px 12px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: #1e293b;
  color: #fff;
  font-size: 13px;
  font-weight: 700;
}
.slug-pill {
  font-family: monospace;
  font-size: 12px;
  background: rgba(99, 102, 241, 0.22);
  color: #c7d2fe;
  padding: 5px 12px;
  border-radius: 999px;
}
.live-btn {
  padding: 8px 16px;
  border-radius: 8px;
  border: none;
  background: #4f46e5;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
  cursor: pointer;
}
.studio-body {
  flex: 1;
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  min-height: calc(100vh - 60px);
}
.studio-sidebar {
  background: #fff;
  border-right: 1px solid #e2e8f0;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.sidebar-brand {
  padding: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  margin-bottom: 16px;
}
.mini-logo :deep(svg) {
  height: 30px;
  width: auto;
  margin-bottom: 8px;
}
.mini-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.mini-meta strong {
  font-size: 14px;
  color: #0f172a;
}
.mini-meta span {
  font-size: 11px;
  color: #4f46e5;
  font-weight: 700;
}
.vertical-stepper {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}
.step-nav-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid transparent;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: all 0.15s ease;
}
.step-nav-btn:hover {
  background: #f8fafc;
}
.step-nav-btn.active {
  background: #eef2ff;
  border-color: #6366f1;
}
.step-nav-btn:disabled,
.step-nav-btn.is-disabled {
  opacity: 0.52;
  cursor: not-allowed;
}
.back-btn:disabled,
.project-selector select:disabled,
.live-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
.step-index {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e2e8f0;
  color: #475569;
  font-size: 12px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.step-nav-btn.active .step-index {
  background: #4f46e5;
  color: #fff;
}
.step-nav-btn.done .step-index {
  background: #10b981;
  color: #fff;
}
.step-labels {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}
.step-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
}
.step-labels strong {
  font-size: 13px;
  color: #0f172a;
}
.opt-badge {
  font-size: 10px;
  font-weight: 700;
  background: #f1f5f9;
  color: #64748b;
  padding: 1px 6px;
  border-radius: 999px;
}
.step-labels small {
  font-size: 11px;
  color: #64748b;
}
.sidebar-footer {
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}
.sidebar-tip {
  margin: 0;
  font-size: 11px;
  color: #64748b;
  line-height: 1.5;
}
.studio-canvas {
  padding: 28px 32px;
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
  min-width: 0;
  box-sizing: border-box;
}
.studio-toast {
  position: fixed;
  top: 76px;
  right: 28px;
  z-index: 1200;
  max-width: 520px;
  display: flex;
  align-items: center;
  gap: 10px;
  background: #0f172a;
  color: #fff;
  padding: 13px 16px;
  border-radius: 12px;
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.24);
  font-size: 13px;
  font-weight: 700;
  line-height: 1.45;
}
.studio-toast.toast-success {
  background: #065f46;
  border: 1px solid #10b981;
}
.studio-toast.toast-error {
  background: #991b1b;
  border: 1px solid #f87171;
}
.toast-icon {
  font-size: 15px;
  flex-shrink: 0;
}
.toast-text {
  flex: 1;
}
.toast-close {
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
  cursor: pointer;
  padding: 2px 4px;
}
.studio-progress-banner {
  position: sticky;
  top: 0;
  z-index: 40;
  background: #eef2ff;
  border: 1.5px solid #6366f1;
  border-radius: 14px;
  padding: 14px 18px;
  margin-bottom: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.14);
}
.progress-banner-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
}
.progress-banner-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.busy-lock-pill {
  font-size: 11px;
  font-weight: 800;
  color: #3730a3;
  background: #e0e7ff;
  border: 1px solid #a5b4fc;
  padding: 4px 10px;
  border-radius: 999px;
  white-space: nowrap;
}
.progress-step-pills {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding-top: 10px;
  border-top: 1px dashed #c7d2fe;
}
.progress-step-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
  padding: 5px 12px;
  border-radius: 999px;
  background: #ffffff;
  color: #64748b;
  border: 1px solid #cbd5e1;
}
.progress-step-pill.step-active {
  background: #4f46e5;
  color: #ffffff;
  border-color: #4f46e5;
  box-shadow: 0 4px 10px rgba(79, 70, 229, 0.24);
}
.progress-step-pill.step-done {
  background: #ecfdf5;
  color: #047857;
  border-color: #6ee7b7;
}
.p-step-icon {
  font-weight: 900;
}
.progress-spinner {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2.5px solid #c7d2fe;
  border-top-color: #4f46e5;
  animation: studioSpin 0.8s linear infinite;
  flex-shrink: 0;
}
@keyframes studioSpin {
  to {
    transform: rotate(360deg);
  }
}
.progress-texts {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.progress-texts strong {
  font-size: 13px;
  color: #1e1b4b;
}
.progress-texts span {
  font-size: 12px;
  color: #4338ca;
}
.studio-error-banner {
  position: sticky;
  top: 0;
  z-index: 45;
  background: #fef2f2;
  border: 1.5px solid #f87171;
  border-radius: 16px;
  padding: 16px 20px;
  margin-bottom: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  box-shadow: 0 10px 26px rgba(185, 28, 28, 0.12);
}
.error-banner-left {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
  min-width: 280px;
}
.error-badge-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #dc2626;
  color: #fff;
  font-size: 13px;
  font-weight: 900;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}
.error-banner-body {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.error-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.error-title-row strong {
  font-size: 14px;
  color: #991b1b;
}
.error-endpoint-pill {
  font-family: monospace;
  font-size: 11px;
  background: #fee2e2;
  color: #b91c1c;
  padding: 2px 8px;
  border-radius: 6px;
  border: 1px solid #fecaca;
}
.error-banner-body p {
  margin: 0;
  font-size: 12.5px;
  color: #7f1d1d;
  line-height: 1.5;
}
.error-banner-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.error-retry-btn {
  padding: 8px 14px;
  border-radius: 8px;
  border: none;
  background: #dc2626;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
  cursor: pointer;
}
.error-step-btn {
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid #fca5a5;
  background: #fff;
  color: #991b1b;
  font-size: 12px;
  font-weight: 800;
  cursor: pointer;
}
.error-dismiss-btn {
  padding: 8px 10px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #991b1b;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.empty-step-box {
  background: #f8fafc;
  border: 1.5px dashed #cbd5e1;
  border-radius: 16px;
  padding: 26px 24px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}
.empty-step-box strong {
  font-size: 15px;
  color: #0f172a;
}
.empty-step-box p {
  margin: 0 0 6px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}
.canvas-panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 28px;
  display: flex;
  flex-direction: column;
  gap: 22px;
  min-width: 0;
  max-width: 100%;
  box-sizing: border-box;
  transition: opacity 0.2s ease;
}
.canvas-panel.is-busy-locked {
  pointer-events: none;
  user-select: none;
}
.canvas-panel.is-busy-locked > :not(.skeleton-grid) {
  opacity: 0.68;
}
.skeleton-card {
  pointer-events: none;
  background: #f8fafc !important;
  border-color: #e2e8f0 !important;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.skel-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.skel-bar {
  display: block;
  height: 12px;
  border-radius: 6px;
  background: linear-gradient(90deg, #e2e8f0 25%, #f1f5f9 50%, #e2e8f0 75%);
  background-size: 200% 100%;
  animation: studioShimmer 1.4s ease-in-out infinite;
}
.skel-pill {
  width: 72px;
  height: 18px;
  border-radius: 999px;
}
.skel-pill-sm {
  width: 58px;
  height: 16px;
  border-radius: 999px;
}
.skel-title {
  width: 65%;
  height: 20px;
  margin-top: 2px;
}
.skel-sub {
  width: 48%;
  height: 13px;
}
.skel-line {
  width: 100%;
  height: 12px;
}
.skel-line.short {
  width: 78%;
}
.skel-stage {
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%) !important;
  background-size: 200% 100% !important;
  animation: studioShimmer 1.4s ease-in-out infinite;
  display: flex;
  align-items: center;
  justify-content: center;
}
.skel-stage-label {
  font-size: 11px;
  font-weight: 700;
  color: #64748b;
}
.skel-swatch {
  width: 54px;
  height: 16px;
  border-radius: 6px;
}
@keyframes studioShimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
  padding-bottom: 18px;
  border-bottom: 1px solid #f1f5f9;
}
.step-kicker {
  font-size: 11px;
  font-weight: 800;
  color: #4f46e5;
  letter-spacing: 0.06em;
}
.panel-header h2 {
  margin: 4px 0 6px;
  font-size: 22px;
}
.panel-header p {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}
.header-btns {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.hint-input {
  padding: 9px 12px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  font-size: 12px;
  min-width: 210px;
}
.primary-btn {
  padding: 10px 18px;
  border-radius: 10px;
  border: none;
  background: #4f46e5;
  color: #fff;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
}
.primary-btn.full {
  width: 100%;
}
.secondary-btn {
  padding: 10px 14px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  background: #fff;
  color: #334155;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.warn-btn {
  padding: 10px 14px;
  border-radius: 10px;
  border: 1px solid #fca5a5;
  background: #fef2f2;
  color: #b91c1c;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.heal-btn {
  padding: 10px 16px;
  border-radius: 10px;
  border: none;
  background: #059669;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
  cursor: pointer;
}
.starter-preset-bar {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.starter-label {
  font-size: 12px;
  font-weight: 800;
  color: #4f46e5;
}
.starter-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.starter-chip {
  padding: 7px 13px;
  border-radius: 999px;
  border: 1px solid #cbd5e1;
  background: #fff;
  color: #1e293b;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.15s ease;
}
.starter-chip:hover {
  border-color: #4f46e5;
  background: #eef2ff;
  color: #4338ca;
}
.type-card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.type-card {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  background: #f8fafc;
}
.type-card.selected {
  border: 2px solid #4f46e5;
  background: #eef2ff;
}
.type-badge {
  font-size: 11px;
  font-weight: 800;
  color: #4f46e5;
}
.type-card h3 {
  margin: 8px 0;
  font-size: 16px;
}
.type-card p {
  margin: 0;
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
}
.field-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.field-group label,
.block-label {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
}
.field-group input,
.field-group textarea {
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  font-size: 14px;
}
.panel-actions-split {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.brand-kit-preview {
  border: 2px solid #cbd5e1;
  border-radius: 16px;
  padding: 18px 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
}
.kit-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.kit-tag {
  font-size: 11px;
  font-weight: 800;
  color: #4f46e5;
}
.kit-brand-row {
  display: flex;
  align-items: center;
  gap: 14px;
}
.kit-logo :deep(svg) {
  height: 38px;
  width: auto;
}
.kit-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.kit-info strong {
  font-size: 18px;
  color: #0f172a;
}
.kit-info span {
  font-size: 12px;
  color: #475569;
}
.kit-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.kit-swatches {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.kit-swatch {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: rgba(255, 255, 255, 0.85);
  padding: 4px 8px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  font-size: 11px;
}
.kit-swatch i {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  display: inline-block;
}
.kit-sample-btn {
  color: #fff;
  padding: 9px 16px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 800;
}
.section-title-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
  flex-wrap: wrap;
}
.helper-caption {
  font-size: 12px;
  color: #64748b;
}
.sub-heading {
  margin: 0;
  font-size: 16px;
}
.name-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.name-card {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.name-card:hover {
  border-color: #94a3b8;
}
.name-card.selected {
  border: 2px solid #4f46e5;
  background: #f5f7ff;
}
.name-top {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  font-weight: 700;
  margin-bottom: 8px;
}
.score-pill {
  color: #4f46e5;
}
.slug-ok {
  color: #15803d;
}
.name-card h4 {
  margin: 0 0 4px;
  font-size: 16px;
}
.pronun {
  font-size: 11px;
  color: #64748b;
  margin-bottom: 8px;
}
.name-card p {
  margin: 0;
  font-size: 12px;
  color: #475569;
  line-height: 1.45;
}
.logo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.logo-card {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.15s ease;
}
.logo-card.selected {
  border: 2px solid #4f46e5;
  box-shadow: 0 6px 18px rgba(79, 70, 229, 0.12);
}
.svg-stage {
  height: 86px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
}
.svg-inner :deep(svg) {
  height: 40px;
  width: auto;
}
.logo-meta {
  padding: 12px 14px;
}
.logo-meta-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.selected-pill {
  font-size: 10px;
  font-weight: 800;
  background: #eef2ff;
  color: #4f46e5;
  padding: 2px 8px;
  border-radius: 999px;
}
.swatches {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}
.swatch-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #475569;
}
.swatch-item i {
  width: 11px;
  height: 11px;
  border-radius: 3px;
  display: inline-block;
}
.spec-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 260px;
  gap: 20px;
  align-items: start;
  min-width: 0;
}
.spec-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}
.version-sidebar {
  min-width: 0;
  box-sizing: border-box;
}
.visual-spec-summary {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
  min-width: 0;
  box-sizing: border-box;
}
.summary-badge-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}
.summary-type-pill {
  font-size: 11px;
  font-weight: 800;
  background: #eef2ff;
  color: #4f46e5;
  padding: 4px 10px;
  border-radius: 999px;
}
.summary-slug-pill {
  font-size: 11px;
  font-family: monospace;
  background: #e2e8f0;
  color: #334155;
  padding: 4px 10px;
  border-radius: 999px;
}
.visual-spec-summary h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #0f172a;
}
.summary-prompt {
  margin: 0 0 14px;
  font-size: 13px;
  color: #475569;
  line-height: 1.6;
}
.parsed-spec-lines {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  min-width: 0;
}
.spec-bullet-item {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 12px;
  color: #334155;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  line-height: 1.45;
  min-width: 0;
  box-sizing: border-box;
}
.bullet-dot {
  color: #10b981;
  font-weight: 900;
}
.spec-textarea {
  width: 100%;
  padding: 14px;
  border-radius: 12px;
  border: 1px solid #cbd5e1;
  font-family: monospace;
  font-size: 13px;
  line-height: 1.6;
  margin-top: 8px;
  box-sizing: border-box;
}
.blueprint-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 18px;
  min-width: 0;
  box-sizing: border-box;
}
.blueprint-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.blueprint-sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: #64748b;
}
.screen-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 14px;
  min-width: 0;
}
.screen-item {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
  box-sizing: border-box;
}
.screen-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}
.screen-step-num {
  font-size: 11px;
  font-weight: 800;
  color: #64748b;
}
.layout-tag {
  font-size: 11px;
  font-weight: 700;
  background: #eef2ff;
  color: #4f46e5;
  padding: 3px 8px;
  border-radius: 999px;
}
.screen-title-text {
  font-size: 15px;
  color: #0f172a;
  line-height: 1.4;
  word-break: keep-all;
}
.screen-desc-text {
  margin: 0;
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
}
.comp-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 4px;
}
.comp-chip {
  font-size: 11px;
  font-weight: 600;
  background: #f1f5f9;
  color: #334155;
  padding: 4px 8px;
  border-radius: 8px;
}
.version-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 8px;
  min-width: 0;
}
.version-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px;
  background: #f8fafc;
  min-width: 0;
  box-sizing: border-box;
}
.ver-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  margin-bottom: 6px;
}
.ver-no {
  font-weight: 800;
  color: #4f46e5;
  flex-shrink: 0;
}
.ver-user {
  color: #64748b;
  font-size: 11px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ver-title {
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 8px;
  line-height: 1.45;
  word-break: break-word;
  overflow-wrap: anywhere;
}
.rollback-btn {
  width: 100%;
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid #c7d2fe;
  background: #eef2ff;
  color: #4338ca;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  box-sizing: border-box;
}
.preview-split {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 22px;
}
.chat-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.status-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
}
.status-row {
  display: flex;
  justify-content: space-between;
}
.editor-mode-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4px;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 10px;
}
.editor-mode-tabs button {
  padding: 8px 6px;
  border-radius: 8px;
  border: none;
  background: transparent;
  font-size: 11px;
  font-weight: 800;
  color: #475569;
  cursor: pointer;
}
.editor-mode-tabs button.active {
  background: #fff;
  color: #4f46e5;
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.08);
}
.tab-pane {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.chat-panel textarea,
.mini-field input,
.mini-field textarea {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  font-size: 13px;
  width: 100%;
  box-sizing: border-box;
}
.mini-field {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.mini-field label {
  font-size: 12px;
  font-weight: 700;
  color: #334155;
}
.color-picker-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.color-field {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.color-field label {
  font-size: 11px;
  font-weight: 700;
  color: #475569;
}
.color-input-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 5px 8px;
  font-size: 12px;
  font-family: monospace;
}
.color-input-wrap input[type='color'] {
  width: 26px;
  height: 26px;
  border: none;
  padding: 0;
  background: transparent;
  cursor: pointer;
}
.toggle-group {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.toggle-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #334155;
  cursor: pointer;
}
.catalog-edit-pane {
  max-height: 540px;
  overflow-y: auto;
  padding-right: 4px;
}
.catalog-edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}
.tiny-add-btn {
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid #c7d2fe;
  background: #eef2ff;
  color: #4338ca;
  font-size: 11px;
  font-weight: 800;
  cursor: pointer;
}
.tiny-del-btn {
  border: none;
  background: transparent;
  color: #ef4444;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
}
.editable-item-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.editable-item-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.editable-card-top {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  font-weight: 700;
  color: #64748b;
}
.editable-item-card input {
  padding: 7px 10px;
  border-radius: 7px;
  border: 1px solid #cbd5e1;
  font-size: 12px;
}
.editable-price-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
}
.preset-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 4px;
}
.preset-title {
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
}
.preset-group button {
  padding: 9px 12px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #fff;
  text-align: left;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.preset-group button:hover {
  border-color: #6366f1;
  background: #f5f7ff;
}
.viewport-btns {
  display: flex;
  gap: 4px;
}
.viewport-btns button {
  padding: 7px 10px;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  background: #fff;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
}
.viewport-btns button.active {
  background: #0f172a;
  color: #fff;
}
.preview-stage {
  background: #e2e8f0;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}
.preview-frame {
  width: 100%;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.12);
}
.preview-frame.tablet {
  max-width: 740px;
}
.preview-frame.mobile {
  max-width: 390px;
}
.preview-gnb {
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  padding: 12px 22px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.preview-gnb-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.gnb-brand-title {
  font-size: 14px;
  color: #0f172a;
}
.preview-gnb-links {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 12px;
  font-weight: 700;
  color: #475569;
}
.gnb-live-pill {
  font-family: monospace;
  font-size: 11px;
  background: #f1f5f9;
  color: #4f46e5;
  padding: 3px 8px;
  border-radius: 999px;
}
.frame-hero {
  padding: 28px 26px;
}
.frame-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.frame-logo :deep(svg) {
  height: 28px;
  width: auto;
}
.frame-badge {
  font-size: 11px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 10px;
  border-radius: 999px;
}
.flash-pill {
  font-size: 11px;
  font-weight: 700;
  background: rgba(250, 204, 21, 0.24);
  color: #fef08a;
  padding: 4px 10px;
  border-radius: 999px;
}
.frame-hero h3 {
  margin: 0 0 8px;
  font-size: 22px;
}
.frame-hero p {
  margin: 0 0 18px;
  font-size: 13px;
  opacity: 0.92;
  line-height: 1.55;
}
.hero-cta-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.hero-cta-btn {
  background: #ffffff;
  color: #0f172a;
  padding: 9px 16px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}
.hero-concierge-pill {
  font-size: 11px;
  background: rgba(15, 23, 42, 0.35);
  padding: 7px 12px;
  border-radius: 999px;
}
.frame-section {
  padding: 22px 24px;
  border-bottom: 1px solid #e2e8f0;
}
.frame-sec-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}
.sec-kicker {
  font-size: 10px;
  font-weight: 800;
  color: #64748b;
  letter-spacing: 0.06em;
}
.frame-section h4 {
  margin: 2px 0 0;
  font-size: 16px;
  color: #0f172a;
}
.cat-pills {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.cat-pill {
  font-size: 11px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid #cbd5e1;
  color: #475569;
}
.cat-pill.active {
  background: #0f172a;
  color: #fff;
  border-color: #0f172a;
}
.mini-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}
.mini-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.mini-card-img-wrap {
  position: relative;
  height: 130px;
  background: #f1f5f9;
}
.mini-card-img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.prod-cat-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(15, 23, 42, 0.78);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 999px;
}
.mini-card-body {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
}
.mini-card-body strong {
  font-size: 13px;
  color: #0f172a;
  line-height: 1.35;
}
.prod-sub {
  margin: 0;
  font-size: 11px;
  color: #64748b;
  line-height: 1.4;
}
.prod-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}
.prod-price {
  font-size: 14px;
  font-weight: 800;
}
.prod-buy-chip {
  font-size: 11px;
  font-weight: 700;
  background: #f1f5f9;
  color: #0f172a;
  padding: 4px 10px;
  border-radius: 6px;
}
.mini-articles {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.mini-article {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  gap: 12px;
  align-items: center;
}
.mini-art-thumb {
  width: 68px;
  height: 68px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}
.mini-art-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.art-badge {
  font-size: 10px;
  font-weight: 800;
  color: #4f46e5;
}
.mini-art-content strong {
  font-size: 13px;
  color: #0f172a;
  line-height: 1.35;
}
.mini-art-content p {
  margin: 0;
  font-size: 11px;
  color: #64748b;
  line-height: 1.4;
}
.frame-form-preview {
  background: #ffffff;
}
.form-preview-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.form-preview-bar strong {
  display: block;
  font-size: 14px;
  color: #0f172a;
  margin-top: 2px;
}
.form-preview-bar p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #64748b;
}
.form-preview-cta {
  color: #fff;
  font-size: 11px;
  font-weight: 800;
  padding: 8px 14px;
  border-radius: 999px;
}
.non-dev-banner {
  background: #eef2ff;
  border: 1px solid #c7d2fe;
  border-radius: 14px;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.non-dev-banner strong {
  font-size: 14px;
  color: #1e1b4b;
}
.non-dev-banner p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #4338ca;
}
.webide-grid {
  display: grid;
  grid-template-columns: 220px 1fr 320px;
  gap: 14px;
  min-height: 460px;
}
.file-explorer {
  background: #0f172a;
  color: #e2e8f0;
  border-radius: 12px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.explorer-title {
  font-size: 11px;
  font-weight: 800;
  color: #94a3b8;
  margin-bottom: 6px;
}
.file-btn {
  padding: 8px 10px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #cbd5e1;
  text-align: left;
  font-size: 12px;
  cursor: pointer;
}
.file-btn.active {
  background: #4f46e5;
  color: #fff;
  font-weight: 700;
}
.editor-area {
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.editor-head {
  background: #1e293b;
  color: #fff;
  padding: 10px 14px;
  font-size: 12px;
  display: flex;
  justify-content: space-between;
}
.code-editor {
  flex: 1;
  width: 100%;
  min-height: 400px;
  border: none;
  background: #0f172a;
  color: #f8fafc;
  padding: 14px;
  font-family: monospace;
  font-size: 13px;
  line-height: 1.55;
}
.console-area {
  background: #0f172a;
  color: #e2e8f0;
  border-radius: 12px;
  padding: 14px;
  display: flex;
  flex-direction: column;
}
.console-head {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  font-weight: 800;
  margin-bottom: 10px;
}
.status-chip {
  padding: 2px 8px;
  border-radius: 999px;
}
.status-chip.success {
  background: #065f46;
  color: #a7f3d0;
}
.status-chip.failed {
  background: #991b1b;
  color: #fecaca;
}
.console-log {
  margin: 0;
  font-size: 12px;
  white-space: pre-wrap;
  line-height: 1.5;
  color: #cbd5e1;
}
.deploy-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.deploy-box,
.release-history-box {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.track-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.track-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px;
  cursor: pointer;
}
.track-card.selected {
  border: 2px solid #4f46e5;
  background: #eef2ff;
}
.track-card p {
  margin: 6px 0 0;
  font-size: 12px;
  color: #475569;
}
.rel-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.rel-item {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px;
}
.rel-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}
.active-rel-badge {
  font-size: 11px;
  background: #dcfce7;
  color: #166534;
  padding: 3px 8px;
  border-radius: 999px;
  font-weight: 800;
}
.rel-item p {
  margin: 6px 0 0;
  font-size: 12px;
  color: #475569;
}

.hidden-file-input {
  display: none;
}

.asset-upload-panel {
  background: #f8fafc;
  border: 1.5px solid #cbd5e1;
  border-radius: 16px;
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.asset-panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.asset-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.asset-title-row strong {
  font-size: 15px;
  color: #0f172a;
}

.asset-count-pill {
  font-size: 11px;
  font-weight: 800;
  background: #e0e7ff;
  color: #3730a3;
  padding: 3px 10px;
  border-radius: 999px;
}

.asset-panel-head p {
  margin: 0;
  font-size: 12px;
  color: #475569;
  line-height: 1.5;
  max-width: 720px;
}

.asset-head-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.asset-type-select {
  padding: 9px 12px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  background: #fff;
  color: #0f172a;
  font-size: 12px;
  font-weight: 700;
}

.asset-dropzone {
  border: 2px dashed #94a3b8;
  border-radius: 14px;
  background: #ffffff;
  padding: 20px 18px;
  text-align: center;
  cursor: pointer;
  transition: all 0.15s ease;
}

.asset-dropzone:hover,
.asset-dropzone.dragging {
  border-color: #4f46e5;
  background: #eef2ff;
}

.dropzone-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.dropzone-badge {
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.06em;
  color: #4f46e5;
  background: #e0e7ff;
  padding: 2px 8px;
  border-radius: 999px;
}

.dropzone-inner strong {
  font-size: 13px;
  color: #0f172a;
}

.dropzone-inner span {
  font-size: 11px;
  color: #64748b;
}

.asset-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(290px, 1fr));
  gap: 12px;
}

.asset-item-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.asset-thumb-box {
  position: relative;
  width: 82px;
  height: 82px;
  border-radius: 10px;
  overflow: hidden;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.asset-thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.asset-doc-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  color: #334155;
}

.asset-doc-icon span {
  font-size: 14px;
  font-weight: 900;
  color: #4f46e5;
}

.asset-doc-icon small {
  font-size: 10px;
  color: #64748b;
}

.asset-type-chip {
  position: absolute;
  bottom: 4px;
  left: 4px;
  right: 4px;
  background: rgba(15, 23, 42, 0.82);
  color: #fff;
  font-size: 9px;
  font-weight: 700;
  text-align: center;
  padding: 2px 4px;
  border-radius: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.asset-item-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.asset-name-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 6px;
}

.asset-filename {
  font-size: 12px;
  font-weight: 800;
  color: #0f172a;
  text-decoration: none;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.asset-filename:hover {
  color: #4f46e5;
  text-decoration: underline;
}

.asset-filesize {
  font-size: 10px;
  color: #64748b;
  flex-shrink: 0;
}

.asset-meta-controls {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.asset-mini-select,
.asset-caption-input,
.asset-picker-select {
  width: 100%;
  padding: 6px 8px;
  border-radius: 7px;
  border: 1px solid #cbd5e1;
  font-size: 11px;
  background: #fff;
  color: #0f172a;
  box-sizing: border-box;
}

.asset-extracted-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 7px;
  padding: 6px 8px;
  max-height: 54px;
  overflow-y: auto;
}

.asset-extracted-box p {
  margin: 0;
  font-size: 10px;
  color: #475569;
  line-height: 1.4;
  white-space: pre-wrap;
}

.asset-card-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 6px;
}

.asset-action-btn {
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #c7d2fe;
  background: #eef2ff;
  color: #3730a3;
  font-size: 10px;
  font-weight: 800;
  cursor: pointer;
}

.asset-del-btn {
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #fecaca;
  background: #fef2f2;
  color: #b91c1c;
  font-size: 10px;
  font-weight: 800;
  cursor: pointer;
}

.hero-media-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hero-media-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.hero-media-btns {
  display: flex;
  align-items: center;
  gap: 6px;
}

.hero-media-preview {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.hero-media-preview img {
  width: 100%;
  height: 92px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
}

.item-media-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-mini-thumb {
  width: 48px;
  height: 48px;
  border-radius: 7px;
  object-fit: cover;
  border: 1px solid #cbd5e1;
  flex-shrink: 0;
}

.item-media-controls {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.mini-gallery-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.mini-gallery-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.mini-gallery-card img {
  width: 100%;
  height: 108px;
  object-fit: cover;
}

.mini-gallery-card span {
  padding: 6px 8px;
  font-size: 11px;
  font-weight: 700;
  color: #334155;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mini-doc-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mini-doc-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  text-decoration: none;
  color: #0f172a;
}

.mini-doc-item:hover {
  border-color: #6366f1;
  background: #f8fafc;
}

.mini-doc-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mini-doc-ext {
  font-size: 10px;
  font-weight: 900;
  background: #eef2ff;
  color: #4338ca;
  padding: 4px 8px;
  border-radius: 6px;
}

.mini-doc-left strong {
  display: block;
  font-size: 12px;
  color: #0f172a;
}

.mini-doc-left small {
  font-size: 10px;
  color: #64748b;
}

.mini-doc-dl {
  font-size: 11px;
  font-weight: 800;
  color: #4f46e5;
}

/* Direct Editing Enhancements (Step 1 ~ Step 6) */
.step1-meta-grid {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 14px;
}

.step1-select,
.tone-select {
  padding: 9px 12px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  background: #fff;
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
}

.tag-editor-box {
  background: #fff;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 6px 10px;
}

.tag-chip-list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.editable-tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 999px;
  background: #eef2ff;
  color: #4338ca;
  font-size: 12px;
  font-weight: 700;
}

.tag-del-x {
  border: none;
  background: transparent;
  color: #6366f1;
  font-size: 13px;
  font-weight: 900;
  cursor: pointer;
  padding: 0 2px;
  line-height: 1;
}

.tag-inline-input {
  flex: 1;
  min-width: 180px;
  border: none !important;
  outline: none;
  padding: 4px 6px !important;
  font-size: 12px !important;
}

.brand-kit-preview {
  flex-direction: column !important;
  align-items: stretch !important;
  gap: 14px !important;
}

.kit-main-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
}

.kit-tag-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.kit-edit-toggle-btn {
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid #6366f1;
  background: #eef2ff;
  color: #4338ca;
  font-size: 11px;
  font-weight: 800;
  cursor: pointer;
}

.kit-story-line {
  margin: 4px 0 0;
  font-size: 12px;
  color: #475569;
  max-width: 680px;
}

.editable-swatch {
  cursor: pointer;
  position: relative;
}

.editable-swatch input[type='color'] {
  position: absolute;
  opacity: 0;
  width: 100%;
  height: 100%;
  left: 0;
  top: 0;
  cursor: pointer;
}

.brand-direct-editor {
  border-top: 1px dashed #cbd5e1;
  padding-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.direct-editor-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.slug-input-group {
  display: flex;
  align-items: center;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  background: #fff;
  overflow: hidden;
}

.slug-prefix {
  padding: 0 8px;
  background: #f1f5f9;
  color: #475569;
  font-family: monospace;
  font-size: 12px;
  font-weight: 700;
  height: 100%;
  display: flex;
  align-items: center;
}

.slug-input-group input {
  border: none !important;
  flex: 1;
}

.svg-code-toggle-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

.code-editor-mini {
  width: 100%;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #334155;
  background: #0f172a;
  color: #e2e8f0;
  font-family: monospace;
  font-size: 12px;
}

.summary-head-flex {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.editable-bullet-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.bullet-inline-input {
  flex: 1;
  min-width: 0;
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  font-size: 13px;
  color: #1e293b;
  box-sizing: border-box;
}

.editable-screen-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
  box-sizing: border-box;
}

.screen-head-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.order-mini-btn {
  padding: 2px 7px;
  border-radius: 6px;
  border: 1px solid #cbd5e1;
  background: #f8fafc;
  color: #334155;
  font-size: 11px;
  font-weight: 800;
  cursor: pointer;
}

.order-mini-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.screen-edit-row {
  display: flex;
  gap: 8px;
  min-width: 0;
  width: 100%;
}

.screen-title-input {
  flex: 2;
  min-width: 0;
  width: 0;
  padding: 7px 10px;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  font-size: 13px;
  font-weight: 800;
  color: #0f172a;
  box-sizing: border-box;
}

.screen-route-input {
  flex: 1;
  min-width: 0;
  width: 0;
  padding: 7px 10px;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  font-family: monospace;
  font-size: 12px;
  color: #475569;
  box-sizing: border-box;
}

.screen-layout-select,
.screen-desc-input {
  width: 100%;
  min-width: 0;
  padding: 7px 10px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  font-size: 12px;
  color: #334155;
  box-sizing: border-box;
}

.editable-comp-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.comp-add-select {
  padding: 3px 8px;
  border-radius: 6px;
  border: 1px dashed #94a3b8;
  background: #f8fafc;
  font-size: 11px;
  font-weight: 700;
  color: #475569;
  cursor: pointer;
  max-width: 100%;
  box-sizing: border-box;
}

@media (max-width: 1440px) {
  .screen-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1200px) {
  .spec-layout {
    grid-template-columns: minmax(0, 1fr);
  }
  .parsed-spec-lines,
  .screen-list {
    grid-template-columns: minmax(0, 1fr);
  }
}

.preview-click-tip {
  font-size: 12px;
  font-weight: 700;
  color: #4338ca;
  background: #eef2ff;
  border: 1px solid #c7d2fe;
  padding: 7px 12px;
  border-radius: 8px;
  margin-bottom: 10px;
}

.clickable-preview-sec {
  cursor: pointer;
  transition: outline 0.15s ease, box-shadow 0.15s ease;
}

.clickable-preview-sec:hover {
  outline: 2px dashed #6366f1;
  outline-offset: 2px;
}

.explorer-head-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
}

.file-btn-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 6px;
}

.file-del-btn {
  border: none;
  background: transparent;
  color: #94a3b8;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
  padding: 2px 6px;
}

.file-del-btn:hover {
  color: #ef4444;
}

.editor-path-wrap {
  flex: 1;
}

.file-path-input {
  background: #1e293b;
  color: #f8fafc;
  border: 1px solid #334155;
  border-radius: 6px;
  padding: 4px 10px;
  font-family: monospace;
  font-size: 12px;
  width: 280px;
}

.editor-head-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.preflight-summary-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #cbd5e1;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.preflight-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.preflight-logo :deep(svg) {
  height: 40px;
  width: auto;
}

.preflight-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.preflight-meta strong {
  font-size: 15px;
  color: #0f172a;
}

.preflight-meta span {
  font-size: 12px;
  color: #475569;
}

.preflight-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.git-repo-workbench {
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #cbd5e1;
  border-radius: 14px;
  padding: 18px 20px;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.git-wb-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.git-wb-title-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 680px;
}

.git-wb-badge-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.git-provider-pill {
  background: #0f172a;
  color: #f8fafc;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.04em;
  padding: 3px 9px;
  border-radius: 6px;
}

.git-status-pill {
  font-size: 11px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 999px;
}

.git-status-pill.active {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #86efac;
}

.git-status-pill.pending {
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fde68a;
}

.git-wb-title-col h3 {
  margin: 2px 0 0;
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
}

.git-wb-title-col p {
  margin: 0;
  font-size: 12px;
  color: #475569;
  line-height: 1.45;
}

.git-wb-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.link-btn {
  text-decoration: none;
  display: inline-flex;
  align-items: center;
}

.git-config-bar {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 14px;
}

.git-config-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.inline-pair {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 6px;
}

.git-config-foot {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.git-credentials-grid {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 14px;
}

.git-cred-card {
  background: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.cred-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding-bottom: 8px;
  border-bottom: 1px solid #f1f5f9;
}

.cred-card-head strong {
  font-size: 13px;
  color: #0f172a;
}

.cred-scope-chip {
  font-size: 10px;
  font-weight: 700;
  background: #eff6ff;
  color: #1d4ed8;
  padding: 3px 8px;
  border-radius: 999px;
}

.cred-helper-desc {
  margin: 0;
  font-size: 11px;
  color: #64748b;
  line-height: 1.45;
}

.cred-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cred-row label {
  font-size: 11px;
  font-weight: 700;
  color: #334155;
}

.cred-label-split {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.cred-mini-btns {
  display: flex;
  gap: 6px;
}

.cred-copy-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  background: #0f172a;
  color: #e2e8f0;
  border-radius: 8px;
  padding: 7px 10px;
}

.cred-copy-box code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 11px;
  color: #38bdf8;
  word-break: break-all;
  flex: 1;
}

.ssh-pem-preview {
  margin: 4px 0 0;
  background: #020617;
  color: #a7f3d0;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 11px;
  padding: 10px 12px;
  border-radius: 8px;
  max-height: 140px;
  overflow: auto;
  white-space: pre;
}

.git-commit-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.git-commit-item {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 11px;
}

.commit-sha {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-weight: 800;
  color: #4f46e5;
  background: #eef2ff;
  padding: 2px 6px;
  border-radius: 4px;
}

.commit-msg {
  flex: 1;
  color: #0f172a;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.commit-meta {
  color: #64748b;
  font-size: 10px;
  white-space: nowrap;
}

@media (max-width: 1100px) {
  .git-config-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .git-credentials-grid {
    grid-template-columns: 1fr;
  }
}

/* ===================================================================== */
/* AI Intent Diagnosis & 52-Feature Catalog Marketplace Board            */
/* ===================================================================== */
.field-label-split {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.intent-action-btns {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.ai-intent-btn {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: #ffffff;
  border: none;
  border-radius: 999px;
  padding: 7px 14px;
  font-size: 12px;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.22);
  transition: transform 0.15s ease, opacity 0.15s ease;
}

.ai-intent-btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.ai-intent-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.ai-intent-sub-btn {
  background: #f5f3ff;
  color: #5b21b6;
  border: 1px solid #ddd6fe;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.ai-intent-banner {
  background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%);
  border: 1px solid #c7d2fe;
  border-radius: 14px;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ai-intent-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.ai-intent-badges {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.intent-kicker-pill {
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.06em;
  background: #312e81;
  color: #e0e7ff;
  padding: 3px 8px;
  border-radius: 999px;
}

.intent-arch-badge {
  font-size: 12px;
  font-weight: 800;
  background: #4f46e5;
  color: #ffffff;
  padding: 4px 10px;
  border-radius: 999px;
}

.intent-hero-badge {
  font-size: 11px;
  font-weight: 700;
  background: #ffffff;
  color: #334155;
  border: 1px solid #cbd5e1;
  padding: 3px 9px;
  border-radius: 999px;
}

.arch-override-group {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.override-label {
  font-size: 11px;
  font-weight: 700;
  color: #64748b;
}

.arch-mini-chip {
  font-size: 11px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 999px;
  background: #ffffff;
  color: #475569;
  border: 1px solid #cbd5e1;
  cursor: pointer;
}

.arch-mini-chip.active {
  background: #0f172a;
  color: #ffffff;
  border-color: #0f172a;
}

.ai-intent-reason {
  margin: 0;
  font-size: 13px;
  color: #1e293b;
  line-height: 1.5;
}

.ai-intent-reason.muted {
  color: #64748b;
}

.expanded-prompt-box {
  background: #ffffff;
  border: 1px solid #a5b4fc;
  border-radius: 10px;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.expanded-prompt-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.expanded-prompt-top strong {
  font-size: 12px;
  color: #3730a3;
}

.apply-expanded-btn {
  background: #4f46e5;
  color: #ffffff;
  border: none;
  border-radius: 6px;
  padding: 5px 10px;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
}

.expanded-prompt-box p {
  margin: 0;
  font-size: 12px;
  color: #334155;
  line-height: 1.55;
}

.feature-catalog-panel {
  background: #f8fafc;
  border: 1px solid #cbd5e1;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.feature-catalog-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
  flex-wrap: wrap;
}

.feature-head-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 4px;
}

.feature-stat-pill {
  font-size: 11px;
  font-weight: 800;
  padding: 3px 9px;
  border-radius: 999px;
  background: #e2e8f0;
  color: #334155;
}

.feature-stat-pill.recommended {
  background: #ede9fe;
  color: #5b21b6;
}

.feature-stat-pill.active {
  background: #dcfce7;
  color: #166534;
}

.feature-catalog-head h3 {
  margin: 0 0 4px;
  font-size: 17px;
  font-weight: 800;
  color: #0f172a;
}

.feature-catalog-head p {
  margin: 0;
  font-size: 12px;
  color: #475569;
}

.feature-head-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.feature-search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  background: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 10px 14px;
}

.feature-search-input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 260px;
}

.search-icon-label {
  font-size: 12px;
  font-weight: 800;
  color: #4f46e5;
  white-space: nowrap;
}

.feature-search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 13px;
  color: #0f172a;
  background: transparent;
}

.clear-search-btn {
  background: #f1f5f9;
  color: #475569;
  border: none;
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
}

.engine-filter-pills {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.engine-pill {
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  padding: 5px 11px;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
}

.engine-pill.active {
  background: #0f172a;
  color: #ffffff;
  border-color: #0f172a;
}

.feature-category-tabs {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.feat-cat-tab {
  background: #ffffff;
  color: #475569;
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.15s ease;
}

.feat-cat-tab.active {
  background: #4f46e5;
  color: #ffffff;
  border-color: #4f46e5;
}

.feat-cat-tab.recommended.active {
  background: #7c3aed;
  border-color: #7c3aed;
}

.feat-cat-tab.selected.active {
  background: #15803d;
  border-color: #15803d;
}

.feature-card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  max-height: 540px;
  overflow-y: auto;
  padding-right: 4px;
}

.feature-catalog-card {
  background: #ffffff;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  cursor: pointer;
  transition: border-color 0.15s ease, box-shadow 0.15s ease, transform 0.12s ease;
}

.feature-catalog-card:hover {
  border-color: #94a3b8;
  transform: translateY(-1px);
}

.feature-catalog-card.recommended {
  border-color: #c4b5fd;
  background: #fdfcff;
}

.feature-catalog-card.selected {
  border-color: #4f46e5;
  background: #f5f7ff;
  box-shadow: 0 4px 14px rgba(79, 70, 229, 0.12);
}

.feat-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 6px;
}

.feat-badges {
  display: flex;
  align-items: center;
  gap: 5px;
  flex-wrap: wrap;
}

.feat-engine-badge {
  font-size: 10px;
  font-weight: 800;
  padding: 2px 7px;
  border-radius: 999px;
  background: #e0f2fe;
  color: #0369a1;
}

.feat-engine-badge.sync_shop {
  background: #fef3c7;
  color: #b45309;
}

.feat-engine-badge.sync_cms {
  background: #e0e7ff;
  color: #3730a3;
}

.feat-engine-badge.ai_core {
  background: #fae8ff;
  color: #86198f;
}

.feat-cat-badge {
  font-size: 10px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 999px;
  background: #f1f5f9;
  color: #475569;
}

.feat-ai-rec-badge {
  font-size: 10px;
  font-weight: 800;
  padding: 2px 7px;
  border-radius: 999px;
  background: #ede9fe;
  color: #6d28d9;
}

.feat-toggle-pill {
  font-size: 11px;
  font-weight: 800;
  padding: 3px 9px;
  border-radius: 999px;
  background: #f1f5f9;
  color: #64748b;
  white-space: nowrap;
}

.feat-toggle-pill.on {
  background: #4f46e5;
  color: #ffffff;
}

.feat-card-title {
  margin: 2px 0 0;
  font-size: 14px;
  font-weight: 800;
  color: #0f172a;
}

.feat-card-sub {
  margin: 0;
  font-size: 11px;
  font-weight: 700;
  color: #4f46e5;
}

.feat-card-desc {
  margin: 0;
  font-size: 11.5px;
  color: #475569;
  line-height: 1.45;
}

.feat-ai-reason-box {
  background: #eef2ff;
  border-left: 3px solid #6366f1;
  border-radius: 6px;
  padding: 6px 8px;
  font-size: 11px;
  color: #312e81;
  line-height: 1.4;
}

.feat-keyword-row {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
  margin-top: auto;
  padding-top: 4px;
}

.feat-kw-chip {
  font-size: 10px;
  color: #64748b;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  padding: 1px 5px;
}

.feature-empty-box {
  text-align: center;
  padding: 28px;
  background: #ffffff;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
}

/* Step 4 SDUI Preview Blocks */
.mini-pricing-table-wrap {
  overflow-x: auto;
}

.mini-pricing-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 11px;
}

.mini-pricing-table th,
.mini-pricing-table td {
  border: 1px solid #e2e8f0;
  padding: 7px 9px;
  text-align: left;
}

.mini-pricing-table th {
  background: #f1f5f9;
  font-weight: 800;
  color: #0f172a;
}

.mini-space-tour-grid,
.mini-step-guide-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.mini-space-zone-card,
.mini-step-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.zone-badge,
.step-num-pill {
  align-self: flex-start;
  font-size: 10px;
  font-weight: 800;
  background: #e0e7ff;
  color: #3730a3;
  padding: 2px 7px;
  border-radius: 999px;
}

.mini-space-zone-card strong,
.mini-step-card strong {
  font-size: 12px;
  color: #0f172a;
}

.mini-space-zone-card p,
.mini-step-card p {
  margin: 0;
  font-size: 11px;
  color: #475569;
  line-height: 1.4;
}

.mini-faq-loc-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 12px;
}

.mini-faq-box,
.mini-loc-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mini-faq-item strong,
.mini-loc-box strong {
  font-size: 12px;
  color: #0f172a;
}

.mini-faq-item p,
.mini-loc-box p {
  margin: 2px 0 0;
  font-size: 11px;
  color: #475569;
  line-height: 1.4;
}

@media (max-width: 1100px) {
  .feature-card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .mini-faq-loc-grid {
    grid-template-columns: 1fr;
  }
}
</style>
