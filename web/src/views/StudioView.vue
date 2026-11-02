<template>
  <div class="studio-shell">
    <!-- 슬림 스튜디오 상단 헤더 -->
    <header class="studio-topbar">
      <div class="topbar-left">
        <button type="button" class="back-btn" @click="router.push('/feed')">
          ← 쇼케이스로 나가기
        </button>
        <div class="divider"></div>
        <div class="project-selector">
          <span class="selector-label">작업 프로젝트</span>
          <select v-model="selectedIdeaId" @change="onSelectIdea(selectedIdeaId)">
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
            :class="{ active: activeStep === step.no, done: (currentIdea?.currentStep || 1) > step.no }"
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
        <div v-if="toastMessage" class="studio-toast">{{ toastMessage }}</div>

        <!-- ================================================================ -->
        <!-- 1단계: 아이디어 정의 -->
        <!-- ================================================================ -->
        <div v-if="activeStep === 1" class="canvas-panel">
          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 01 · IDEA & SITE ARCHITECTURE</span>
              <h2>아이디어 정의 및 사이트 유형 선택</h2>
              <p>만들고 싶은 브랜드의 핵심 아이디어와 웹사이트 성격을 선택해 주세요. 업종별 예시 템플릿을 클릭하면 1초 만에 자동 입력됩니다.</p>
            </div>
            <button type="button" class="primary-btn" :disabled="busy" @click="saveCurrentIdeaAndGoStep2">
              {{ busy ? '저장 중...' : '현재 프로젝트 저장 후 2단계 이동 →' }}
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

          <div class="type-card-grid">
            <div
              v-for="t in siteTypes"
              :key="t.code"
              class="type-card"
              :class="{ selected: formStep1.siteType === t.code }"
              @click="formStep1.siteType = t.code"
            >
              <span class="type-badge">{{ t.badge }}</span>
              <h3>{{ t.title }}</h3>
              <p>{{ t.desc }}</p>
            </div>
          </div>

          <div class="field-group">
            <label>프로젝트 제목</label>
            <input
              v-model="formStep1.title"
              type="text"
              placeholder="예: 스페셜티 커피 로스터리 아카이브 매거진 & 싱글오리진 원두 스토어 복합 사이트"
            />
          </div>

          <div class="field-group">
            <label>아이디어 상세 설명 (AI 프롬프트)</label>
            <textarea
              v-model="formStep1.promptText"
              rows="4"
              placeholder="타겟 고객, 판매할 상품이나 발행할 매거진 주제, 원하는 브랜드 분위기를 자유롭게 적어주세요."
            ></textarea>
          </div>

          <div class="panel-actions-split">
            <button type="button" class="primary-btn" :disabled="busy" @click="saveCurrentIdeaAndGoStep2">
              {{ busy ? '저장 중...' : '현재 프로젝트에 반영하고 2단계(네이밍·로고)로 이동 →' }}
            </button>
            <button type="button" class="secondary-btn" :disabled="busy" @click="createNewIdeaFromStep1">
              + 완전히 새로운 브랜드 프로젝트로 생성하기
            </button>
          </div>
        </div>

        <!-- ================================================================ -->
        <!-- 2단계: 네이밍 & 로고 -->
        <!-- ================================================================ -->
        <div v-else-if="activeStep === 2" class="canvas-panel">
          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 02 · AI BRAND NAMING & VECTOR LOGO</span>
              <h2>AI 브랜드 네이밍 8종 & 벡터 SVG 로고 6종</h2>
              <p>아이디어 프롬프트에서 도출된 브랜드명(실시간 주소 중복 체크 완료)과 벡터 로고·컬러 팔레트를 선택하세요.</p>
            </div>
            <div class="header-btns">
              <input
                v-model="keywordHint"
                type="text"
                class="hint-input"
                placeholder="네이밍 키워드 힌트 (예: 로스터리, 아뜰리에)"
              />
              <button type="button" class="secondary-btn" :disabled="busy" @click="regenerateNames">
                네이밍 8종 재생성
              </button>
              <button type="button" class="secondary-btn" :disabled="busy" @click="regenerateLogos">
                SVG 로고 6종 재생성
              </button>
              <button type="button" class="primary-btn" :disabled="busy" @click="confirmBrandAndNext">
                브랜드 확정 및 3단계 이동 →
              </button>
            </div>
          </div>

          <!-- 실시간 브랜드 키트 조합 미리보기 바 -->
          <div
            class="brand-kit-preview"
            :style="{
              background: selectedLogoObj?.colorPalette?.surface || '#f8fafc',
              borderColor: selectedLogoObj?.colorPalette?.primary || '#cbd5e1',
            }"
          >
            <div class="kit-left">
              <span class="kit-tag">선택된 브랜드 아이덴티티 미리보기</span>
              <div class="kit-brand-row">
                <div v-if="previewLogoSvg" class="kit-logo" v-html="previewLogoSvg"></div>
                <div class="kit-info">
                  <strong>{{ selectedNameObj?.brandName || previewBrandName }}</strong>
                  <span>{{ selectedNameObj?.pronunciation || 'Brand Identity' }} · 라이브 주소: <code>/s/{{ selectedNameObj?.siteSlug || activeSiteSlug }}</code></span>
                </div>
              </div>
            </div>
            <div class="kit-right">
              <div class="kit-swatches" v-if="selectedLogoObj?.colorPalette">
                <span
                  v-for="(hex, key) in selectedLogoObj.colorPalette"
                  :key="key"
                  class="kit-swatch"
                >
                  <i :style="{ background: String(hex) }"></i>
                  <small>{{ key }}: {{ hex }}</small>
                </span>
              </div>
              <span
                class="kit-sample-btn"
                :style="{ background: selectedLogoObj?.colorPalette?.primary || '#4f46e5' }"
              >
                공식 스토어 탐색하기 →
              </span>
            </div>
          </div>

          <div class="section-title-row">
            <h3 class="sub-heading">AI 추천 브랜드 네이밍 후보 ({{ nameSuggestions.length }}종)</h3>
            <span class="helper-caption">원하는 브랜드명 카드를 클릭하면 상단 브랜드 키트와 사이트 주소가 즉시 변경됩니다.</span>
          </div>
          <div class="name-grid">
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
              <p>{{ name.brandStory }}</p>
            </div>
          </div>

          <div class="section-title-row">
            <h3 class="sub-heading">AI 벡터 SVG 로고 & 브랜드 컬러 팔레트 후보 ({{ logoSuggestions.length }}종)</h3>
            <span class="helper-caption">선택한 로고의 컬러 팔레트가 4단계 사이트의 메인 테마 컬러로 자동 적용됩니다.</span>
          </div>
          <div class="logo-grid">
            <div
              v-for="logo in logoSuggestions"
              :key="logo.id"
              class="logo-card"
              :class="{ selected: selectedLogoId === logo.id }"
              @click="selectLogoCandidate(logo)"
            >
              <div class="svg-stage" :style="{ background: logo.colorPalette?.surface || '#f8fafc' }">
                <div class="svg-inner" v-html="logo.svgCode"></div>
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
        <div v-else-if="activeStep === 3" class="canvas-panel">
          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 03 · FUNCTIONAL SPEC & SCREEN BLUEPRINT</span>
              <h2>서비스 기능명세서 및 화면 설계 블루프린트</h2>
              <p>AI가 설계한 사이트 화면 구성과 핵심 기능을 확인하고, 바로 4단계 실시간 사이트 프로비저닝으로 이동하세요.</p>
            </div>
            <div class="header-btns">
              <button type="button" class="secondary-btn" @click="specEditMode = !specEditMode">
                {{ specEditMode ? '시각적 기획서 카드 보기' : '마크다운 원문 직접 편집' }}
              </button>
              <button type="button" class="secondary-btn" :disabled="busy" @click="saveSpecVersion">
                현재 기획서 스냅샷 저장
              </button>
              <button type="button" class="primary-btn" :disabled="busy" @click="provisionAndGoStep4">
                {{ busy ? '사이트 생성 중...' : '사이트 프로비저닝 및 4단계 프리뷰 →' }}
              </button>
            </div>
          </div>

          <div class="spec-layout">
            <div class="spec-main">
              <!-- 기본: 일반 사용자 친화적 시각 기획 요약 뷰 -->
              <div v-if="!specEditMode" class="visual-spec-cards">
                <div class="visual-spec-summary">
                  <div class="summary-badge-row">
                    <span class="summary-type-pill">{{ formatSiteTypeLabel(currentIdea?.siteType) }}</span>
                    <span class="summary-slug-pill">예정 주소: /s/{{ activeSiteSlug || 'auto-slug' }}</span>
                  </div>
                  <h3>{{ previewBrandName }} — AI 자동 설계 요약</h3>
                  <p class="summary-prompt">{{ currentIdea?.promptText }}</p>
                  <div class="parsed-spec-lines">
                    <div v-for="(line, idx) in parsedSpecHighlights" :key="idx" class="spec-bullet-item">
                      <span class="bullet-dot">✓</span>
                      <span>{{ line }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 선택: 마크다운 원문 편집기 -->
              <div v-else class="markdown-editor-wrap">
                <label class="block-label">마크다운 서비스 기능명세서 원문 에디터</label>
                <textarea v-model="editableSpecMarkdown" rows="11" class="spec-textarea"></textarea>
              </div>

              <!-- 화면 설계 블루프린트 (한글 친화적 레이아웃 + 세로 줄바꿈 버그 해결) -->
              <div class="blueprint-box">
                <div class="blueprint-head">
                  <div>
                    <strong>화면 설계 블루프린트 ({{ screenSpecs.length }}개 핵심 화면 섹션)</strong>
                    <p class="blueprint-sub">사이트 생성 시 아래 화면 섹션과 기능 모듈이 자동으로 배치됩니다.</p>
                  </div>
                </div>
                <div class="screen-list">
                  <div v-for="(scr, idx) in screenSpecs" :key="scr.id || scr.screenKey" class="screen-item">
                    <div class="screen-item-header">
                      <span class="screen-step-num">섹션 0{{ Number(idx) + 1 }}</span>
                      <span class="layout-tag">{{ formatLayoutTypeLabel(scr.layoutType || scr.layout) }}</span>
                    </div>
                    <strong class="screen-title-text">{{ scr.screenTitle || scr.title }}</strong>
                    <p v-if="scr.description" class="screen-desc-text">{{ scr.description }}</p>
                    <div class="comp-tags">
                      <span
                        v-for="c in scr.components || []"
                        :key="c"
                        class="comp-chip"
                        :title="String(c)"
                      >
                        {{ formatComponentLabel(c) }}
                      </span>
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
        <div v-else-if="activeStep === 4" class="canvas-panel">
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
                  AI 대화 수정
                </button>
                <button
                  type="button"
                  :class="{ active: step4Tab === 'VISUAL_THEME' }"
                  @click="step4Tab = 'VISUAL_THEME'"
                >
                  배너·컬러 편집
                </button>
                <button
                  type="button"
                  :class="{ active: step4Tab === 'CATALOG_EDIT' }"
                  @click="step4Tab = 'CATALOG_EDIT'"
                >
                  상품·매거진
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

              <!-- 탭 2: 배너·컬러·노출 섹션 직접 편집 -->
              <div v-else-if="step4Tab === 'VISUAL_THEME'" class="tab-pane">
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
                    <label>배경 서피스 컬러</label>
                    <div class="color-input-wrap">
                      <input v-model="visualForm.surfaceColor" type="color" />
                      <span>{{ visualForm.surfaceColor }}</span>
                    </div>
                  </div>
                </div>

                <div class="toggle-group">
                  <label class="toggle-row">
                    <input v-model="visualForm.showFlashDeal" type="checkbox" />
                    <span>한정 수량/웰컴 쿠폰 배너 표시</span>
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
                  {{ busy ? '적용 중...' : '배너·컬러·섹션 설정 즉시 적용' }}
                </button>
              </div>

              <!-- 탭 3: 진열 상품 & 매거진 아티클 직접 편집 -->
              <div v-else-if="step4Tab === 'CATALOG_EDIT'" class="tab-pane catalog-edit-pane">
                <div class="catalog-edit-header">
                  <strong>진열 상품 직접 편집 ({{ editableProducts.length }}종)</strong>
                  <button type="button" class="tiny-add-btn" @click="addEditableProduct">+ 상품 추가</button>
                </div>
                <div class="editable-item-list">
                  <div v-for="(prod, pIdx) in editableProducts" :key="prod.id || pIdx" class="editable-item-card">
                    <div class="editable-card-top">
                      <span>상품 #{{ pIdx + 1 }}</span>
                      <button type="button" class="tiny-del-btn" @click="editableProducts.splice(pIdx, 1)">삭제</button>
                    </div>
                    <input v-model="prod.name" type="text" placeholder="상품명" />
                    <div class="editable-price-row">
                      <input v-model.number="prod.promotionPrice" type="number" placeholder="판매가(원)" />
                      <input v-model="prod.productCategoryName" type="text" placeholder="카테고리명" />
                    </div>
                    <input v-model="prod.subTitle" type="text" placeholder="상품 한줄 매력 포인트" />
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
                      <button type="button" class="tiny-del-btn" @click="editableArticles.splice(aIdx, 1)">삭제</button>
                    </div>
                    <input v-model="art.title" type="text" placeholder="매거진 제목" />
                    <input v-model="art.description" type="text" placeholder="요약 설명" />
                  </div>
                </div>

                <button type="button" class="primary-btn full" :disabled="busy" @click="saveCatalogAndArticlesOverrides">
                  {{ busy ? '저장 중...' : '상품·매거진 변경사항 즉시 반영' }}
                </button>
              </div>
            </div>

            <!-- 우측 실제 스토어프론트 수준 WYSIWYG 라이브 프리뷰 -->
            <div class="preview-stage">
              <div
                class="preview-frame"
                :class="viewportMode.toLowerCase()"
                :style="{ background: siteRuntime.designTokens?.surface || '#f8fafc' }"
              >
                <!-- 프리뷰 상단 브랜드 GNB -->
                <div class="preview-gnb">
                  <div class="preview-gnb-left">
                    <div class="frame-logo" v-html="siteRuntime.logoSvg"></div>
                    <strong class="gnb-brand-title">{{ siteRuntime.siteName }}</strong>
                  </div>
                  <div class="preview-gnb-links">
                    <span v-if="previewProducts.length">스토어 ({{ previewProducts.length }})</span>
                    <span v-if="siteRuntime.layoutSchema?.showCmsArticles !== false && previewArticles.length">매거진 ({{ previewArticles.length }})</span>
                    <span v-if="siteRuntime.layoutSchema?.showCmsForm !== false">예약/신청</span>
                    <span class="gnb-live-pill">/s/{{ siteRuntime.siteSlug }}</span>
                  </div>
                </div>

                <!-- 프리뷰 메인 히어로 배너 -->
                <div
                  class="frame-hero"
                  :style="{
                    background: `linear-gradient(135deg, ${siteRuntime.designTokens?.primary || '#14532d'} 0%, ${siteRuntime.designTokens?.secondary || siteRuntime.designTokens?.primary || '#1e293b'} 100%)`,
                    color: '#ffffff',
                  }"
                >
                  <div class="frame-top">
                    <span class="frame-badge">{{ siteRuntime.layoutSchema?.customBadge || '공식 브랜드 쇼케이스' }}</span>
                    <span v-if="siteRuntime.layoutSchema?.showFlashDeal !== false" class="flash-pill">첫 구매 웰컴 혜택 적용 중</span>
                  </div>
                  <h3>{{ siteRuntime.layoutSchema?.heroTitle || siteRuntime.siteName }}</h3>
                  <p>{{ siteRuntime.layoutSchema?.heroSubtitle || siteRuntime.brandSlogan }}</p>
                  <div class="hero-cta-row">
                    <span class="hero-cta-btn">
                      {{ siteRuntime.layoutSchema?.ctaText || '시그니처 컬렉션 살펴보기' }} →
                    </span>
                    <span v-if="siteRuntime.layoutSchema?.showAiConcierge !== false" class="hero-concierge-pill">
                      AI 브랜드 컨시어지 상시 대기
                    </span>
                  </div>
                </div>

                <!-- 프리뷰 시그니처 상품 카탈로그 섹션 -->
                <div v-if="previewProducts.length" class="frame-section">
                  <div class="frame-sec-head">
                    <div>
                      <span class="sec-kicker">SIGNATURE SHOP CATALOG</span>
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
                    <div v-for="p in previewProducts.slice(0, 4)" :key="p.id" class="mini-card">
                      <div class="mini-card-img-wrap">
                        <img v-if="p.pic" :src="p.pic" :alt="p.name" />
                        <span v-if="p.productCategoryName" class="prod-cat-tag">{{ p.productCategoryName }}</span>
                      </div>
                      <div class="mini-card-body">
                        <strong>{{ p.name }}</strong>
                        <p class="prod-sub">{{ p.subTitle }}</p>
                        <div class="prod-price-row">
                          <span class="prod-price" :style="{ color: siteRuntime.designTokens?.primary || '#0f172a' }">
                            {{ Number(p.promotionPrice || p.price || 0).toLocaleString() }}원
                          </span>
                          <span class="prod-buy-chip">바로 구매</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 프리뷰 브랜드 매거진 & 커뮤니티 아티클 섹션 -->
                <div
                  v-if="siteRuntime.layoutSchema?.showCmsArticles !== false && previewArticles.length"
                  class="frame-section"
                >
                  <div class="frame-sec-head">
                    <div>
                      <span class="sec-kicker">EDITORIAL ARCHIVE & JOURNAL</span>
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

                <!-- 프리뷰 온라인 클래스/세션 신청 폼 & AI 컨시어지 바 -->
                <div
                  v-if="siteRuntime.layoutSchema?.showCmsForm !== false && siteRuntime.cmsForm"
                  class="frame-section frame-form-preview"
                >
                  <div class="form-preview-bar">
                    <div>
                      <span class="sec-kicker">ONLINE BOOKING & INQUIRY</span>
                      <strong>{{ siteRuntime.cmsForm.title || siteRuntime.cmsForm.formName }}</strong>
                      <p>{{ siteRuntime.cmsForm.description }}</p>
                    </div>
                    <span
                      class="form-preview-cta"
                      :style="{ background: siteRuntime.designTokens?.primary || '#0f172a' }"
                    >
                      온라인 신청 접수 활성 ✓
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ================================================================ -->
        <!-- 5단계: WebIDE 코드 편집 (고급 선택 과정) -->
        <!-- ================================================================ -->
        <div v-else-if="activeStep === 5" class="canvas-panel">
          <div class="non-dev-banner">
            <div>
              <strong>일반 창업자·마케터 안내: 코드 편집 없이 바로 6단계로 이동하실 수 있습니다.</strong>
              <p>4단계에서 설정한 디자인·상품·매거진 구성만으로도 완성도 높은 라이브 사이트가 배포됩니다. 이 단계는 프론트엔드 개발자가 Vue 3 코드를 직접 커스텀하거나 AI 코드 자가치유를 실행할 때만 사용하세요.</p>
            </div>
            <button type="button" class="primary-btn" @click="goToStep(6)">
              코드 수정 건너뛰고 6단계(빌드·배포)로 이동 →
            </button>
          </div>

          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 05 · WEBIDE CODE STUDIO & AI SELF-HEALING (OPTIONAL)</span>
              <h2>고급 모드 WebIDE 코드 편집기 & AI 빌드 오류 자동 패치</h2>
              <p>Vue 3 컴포넌트와 테마 JSON 코드를 직접 커스텀하고, 빌드 오류 발생 시 AI로 즉시 복구합니다.</p>
            </div>
            <div class="header-btns">
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

          <div class="webide-grid" v-if="webIdeData">
            <div class="file-explorer">
              <div class="explorer-title">PROJECT FILES</div>
              <button
                v-for="(file, idx) in webIdeFiles"
                :key="file.path"
                type="button"
                class="file-btn"
                :class="{ active: activeFileIndex === idx }"
                @click="activeFileIndex = idx"
              >
                {{ file.path }}
              </button>
            </div>

            <div class="editor-area">
              <div class="editor-head" v-if="activeWebIdeFile">
                <span>{{ activeWebIdeFile.path }}</span>
                <span class="lang-tag">{{ activeWebIdeFile.language }}</span>
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
        </div>

        <!-- ================================================================ -->
        <!-- 6단계: 빌드 & 배포 -->
        <!-- ================================================================ -->
        <div v-else-if="activeStep === 6" class="canvas-panel">
          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 06 · HYBRID BUILD & LIVE RELEASE</span>
              <h2>빌드 & 라이브 사이트 무중단 배포</h2>
              <p>배포 방식을 선택해 라이브 주소로 즉시 발행하고, 쇼케이스 피드에서 초기 고객과 팀원을 만나보세요.</p>
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

          <div class="deploy-grid" v-if="siteRuntime">
            <div class="deploy-box">
              <h3>배포 방식 선택</h3>
              <div class="track-cards">
                <div
                  class="track-card"
                  :class="{ selected: deployForm.deployMode === 'DYNAMIC_SDUI' }"
                  @click="deployForm.deployMode = 'DYNAMIC_SDUI'"
                >
                  <strong>즉시 디자인 배포 (권장 · 무중단 스키마)</strong>
                  <p>별도 빌드 대기 없이 컬러 토큰, 배너 문구, 상품·매거진 구성을 1초 만에 라이브 사이트에 즉시 반영합니다.</p>
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

              <div class="field-group">
                <label>릴리즈 노트 (변경 요약)</label>
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
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
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

const siteTypes = [
  {
    code: 'COMMERCE',
    badge: '쇼핑몰 스토어',
    title: '프리미엄 D2C 브랜드 쇼핑몰',
    desc: '상품 카탈로그, 카테고리 필터, 쿠폰, 장바구니 및 원클릭 주문 결제가 즉시 제공됩니다.',
  },
  {
    code: 'CMS_SITE',
    badge: '브랜드·커뮤니티 웹',
    title: '브랜드 매거진 & 커뮤니티 웹사이트',
    desc: '브랜드 스토리, 에디토리얼 아티클, 주간 일정 공지 및 온라인 참가·문의 신청 폼을 제공합니다.',
  },
  {
    code: 'HYBRID',
    badge: '매거진+스토어 복합',
    title: '매거진 아카이브 + 쇼핑몰 복합 사이트',
    desc: '깊이 있는 매거진 아티클과 상품 구매, 오프라인 클래스 예약을 하나의 사이트에서 동시에 운영합니다.',
  },
];

const studioPresets = ref<any[]>([]);
const layoutLabels = ref<Record<string, string>>({});
const componentLabels = ref<Record<string, string>>({});

const ideas = ref<any[]>([]);
const selectedIdeaId = ref('');
const currentIdea = ref<any | null>(null);
const activeStep = ref(1);
const busy = ref(false);
const toastMessage = ref('');
const specEditMode = ref(false);
const step4Tab = ref<'AI_CHAT' | 'VISUAL_THEME' | 'CATALOG_EDIT'>('AI_CHAT');

const formStep1 = ref({
  siteType: 'COMMERCE',
  title: '',
  promptText: '',
});

const keywordHint = ref('');
const selectedNameId = ref('');
const selectedLogoId = ref('');
const editableSpecMarkdown = ref('');

const siteRuntime = ref<any | null>(null);
const viewportMode = ref<'DESKTOP' | 'TABLET' | 'MOBILE'>('DESKTOP');
const chatInstruction = ref('');

const visualForm = ref({
  customBadge: '',
  heroTitle: '',
  heroSubtitle: '',
  ctaText: '',
  primaryColor: '#14532d',
  surfaceColor: '#f8fafc',
  showFlashDeal: true,
  showCmsArticles: true,
  showCmsForm: true,
  showAiConcierge: true,
});

const editableProducts = ref<any[]>([]);
const editableArticles = ref<any[]>([]);

const webIdeData = ref<any | null>(null);
const webIdeFiles = ref<Array<{ path: string; language: string; content: string }>>([]);
const activeFileIndex = ref(0);

const deployForm = ref({
  deployMode: 'DYNAMIC_SDUI',
  releaseNote: '',
});

const nameSuggestions = computed(() => currentIdea.value?.nameSuggestions || []);
const logoSuggestions = computed(() => currentIdea.value?.logoSuggestions || []);
const ideaVersions = computed(() => currentIdea.value?.versions || []);
const screenSpecs = computed(() => currentIdea.value?.screenSpecs || currentIdea.value?.screens || []);

const selectedNameObj = computed(
  () => nameSuggestions.value.find((n: any) => n.id === selectedNameId.value) || nameSuggestions.value[0] || null,
);
const selectedLogoObj = computed(
  () => logoSuggestions.value.find((l: any) => l.id === selectedLogoId.value) || logoSuggestions.value[0] || null,
);
const previewBrandName = computed(
  () => selectedNameObj.value?.brandName || currentIdea.value?.selectedBrandName || currentIdea.value?.title || '신규 브랜드',
);
const previewLogoSvg = computed(
  () => selectedLogoObj.value?.svgCode || currentIdea.value?.selectedLogoSvg || '',
);

const activeSiteSlug = computed(
  () => siteRuntime.value?.siteSlug || currentIdea.value?.site?.siteSlug || currentIdea.value?.selectedSiteSlug || '',
);
const activeWebIdeFile = computed(() => webIdeFiles.value[activeFileIndex.value] || null);

const previewProducts = computed(
  () => siteRuntime.value?.products || siteRuntime.value?.commerce?.products || [],
);
const previewCategories = computed(
  () => siteRuntime.value?.categories || siteRuntime.value?.commerce?.categories || [],
);
const previewArticles = computed(
  () => siteRuntime.value?.articles || siteRuntime.value?.cms?.articles || [],
);

const parsedSpecHighlights = computed(() => {
  const raw = editableSpecMarkdown.value || '';
  return raw
    .split('\n')
    .map((line) => line.replace(/^[#\-*\d.\s]+/, '').replace(/\*\*/g, '').trim())
    .filter((line) => line.length > 6 && !line.startsWith('1. ') && !line.startsWith('2. '))
    .slice(0, 6);
});

function formatLayoutTypeLabel(code: string) {
  if (!code) return '반응형 표준 섹션';
  return layoutLabels.value[code] || code;
}

function formatComponentLabel(code: string) {
  if (!code) return '기능 모듈';
  return componentLabels.value[code] || code;
}

function showToast(msg: string) {
  toastMessage.value = msg;
  setTimeout(() => {
    if (toastMessage.value === msg) toastMessage.value = '';
  }, 3400);
}

function applyStarterPreset(preset: any) {
  formStep1.value.siteType = preset.siteType || 'COMMERCE';
  formStep1.value.title = preset.title || preset.label || '';
  formStep1.value.promptText = preset.prompt || preset.promptText || '';
  showToast('업종별 스타터 프롬프트가 입력되었습니다. 바로 2단계로 이동하거나 새 프로젝트로 생성해 보세요.');
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
  visualForm.value = {
    customBadge: layout.customBadge || '공식 브랜드 쇼케이스',
    heroTitle: layout.heroTitle || normalized.siteName || '',
    heroSubtitle: layout.heroSubtitle || normalized.brandSlogan || '',
    ctaText: layout.ctaText || layout.primaryCta || '시그니처 컬렉션 탐색하기',
    primaryColor: tokens.primary || tokens.primaryColor || '#14532d',
    surfaceColor: tokens.surface || tokens.surfaceColor || '#f8fafc',
    showFlashDeal: layout.showFlashDeal !== false,
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
}

async function initStudio() {
  try {
    const meta = await kkumzApi.getStudioMetadata();
    layoutLabels.value = meta?.layoutLabels || {};
    componentLabels.value = meta?.componentLabels || {};
    studioPresets.value = meta?.starterPresets || [];
  } catch {
    layoutLabels.value = {};
    componentLabels.value = {};
    studioPresets.value = [];
  }
  ideas.value = (await kkumzApi.listIdeas()) || [];
  const routeIdeaId = route.params.ideaId ? String(route.params.ideaId) : '';
  const targetId = routeIdeaId || (ideas.value[0]?.id ?? '');
  if (targetId) {
    await onSelectIdea(targetId);
  }
}

async function onSelectIdea(ideaId: string) {
  if (!ideaId) return;
  selectedIdeaId.value = ideaId;
  const detail = await kkumzApi.getIdeaDetail(ideaId);
  currentIdea.value = detail;

  formStep1.value = {
    siteType: detail.siteType || 'COMMERCE',
    title: detail.title || '',
    promptText: detail.promptText || '',
  };
  editableSpecMarkdown.value = detail.specMarkdown || '';

  const selName = (detail.nameSuggestions || []).find((n: any) => n.isSelected) || detail.nameSuggestions?.[0];
  selectedNameId.value = selName?.id || '';

  const selLogo = (detail.logoSuggestions || []).find((l: any) => l.isSelected) || detail.logoSuggestions?.[0];
  selectedLogoId.value = selLogo?.id || '';

  const slug = detail.site?.siteSlug || detail.selectedSiteSlug;
  if (slug) {
    try {
      siteRuntime.value = await kkumzApi.getSiteRuntime(slug);
      deployForm.value.deployMode = siteRuntime.value.deployMode || 'DYNAMIC_SDUI';
      syncVisualFormFromRuntime(siteRuntime.value);
    } catch {
      siteRuntime.value = null;
    }
  } else {
    siteRuntime.value = null;
  }

  if (activeStep.value === 2) {
    await ensureStep2Candidates();
  }
}

async function ensureStep2Candidates() {
  if (!selectedIdeaId.value) return;
  if (nameSuggestions.value.length < 8) {
    await regenerateNames();
  }
  if (logoSuggestions.value.length < 6) {
    await regenerateLogos();
  }
}

async function goToStep(stepNo: number) {
  activeStep.value = stepNo;
  if (!selectedIdeaId.value) return;

  if (stepNo === 2) {
    await ensureStep2Candidates();
  } else if (stepNo === 4 || stepNo === 6) {
    if (!siteRuntime.value) {
      siteRuntime.value = await kkumzApi.scaffoldSite(selectedIdeaId.value);
    }
    syncVisualFormFromRuntime(siteRuntime.value);
  } else if (stepNo === 5) {
    if (!siteRuntime.value) {
      siteRuntime.value = await kkumzApi.scaffoldSite(selectedIdeaId.value);
      syncVisualFormFromRuntime(siteRuntime.value);
    }
    if (activeSiteSlug.value) {
      await loadWebIde(activeSiteSlug.value);
    }
  }
}

async function saveCurrentIdeaAndGoStep2() {
  if (!formStep1.value.promptText.trim()) {
    showToast('아이디어 상세 설명(프롬프트)을 입력해 주세요.');
    return;
  }
  if (!selectedIdeaId.value) {
    await createNewIdeaFromStep1();
    return;
  }
  busy.value = true;
  try {
    const prevPrompt = currentIdea.value?.promptText || '';
    const promptChanged = prevPrompt.trim() !== formStep1.value.promptText.trim();
    currentIdea.value = await kkumzApi.updateSpec(selectedIdeaId.value, {
      title: formStep1.value.title,
      promptText: formStep1.value.promptText,
      siteType: formStep1.value.siteType,
      specMarkdown: editableSpecMarkdown.value,
    });
    ideas.value = (await kkumzApi.listIdeas()) || [];
    if (promptChanged || nameSuggestions.value.length < 8) {
      await regenerateNames();
    }
    if (promptChanged || logoSuggestions.value.length < 6) {
      await regenerateLogos();
    }
    activeStep.value = 2;
    showToast('아이디어 설정이 저장되었습니다. 브랜드 네이밍과 로고를 선택해 주세요!');
  } finally {
    busy.value = false;
  }
}

async function createNewIdeaFromStep1() {
  if (!formStep1.value.promptText.trim()) {
    showToast('아이디어 프롬프트를 입력해 주세요.');
    return;
  }
  busy.value = true;
  try {
    const created = await kkumzApi.createIdea({
      title: formStep1.value.title,
      promptText: formStep1.value.promptText,
      siteType: formStep1.value.siteType,
      visibility: 'PUBLIC',
    });
    ideas.value = (await kkumzApi.listIdeas()) || [];
    await onSelectIdea(created.id);
    await goToStep(2);
    showToast('새 브랜드 프로젝트가 생성되었습니다. AI 네이밍 8종과 로고 6종을 확인해 보세요!');
  } finally {
    busy.value = false;
  }
}

async function regenerateNames() {
  if (!selectedIdeaId.value) return;
  busy.value = true;
  try {
    currentIdea.value = await kkumzApi.generateNames(selectedIdeaId.value, {
      keywordHint: keywordHint.value.trim(),
    });
    const sel = (currentIdea.value.nameSuggestions || []).find((n: any) => n.isSelected) || currentIdea.value.nameSuggestions?.[0];
    selectedNameId.value = sel?.id || '';
    showToast('AI 브랜드 네이밍 8종이 생성되었습니다.');
  } finally {
    busy.value = false;
  }
}

async function regenerateLogos() {
  if (!selectedIdeaId.value) return;
  busy.value = true;
  try {
    currentIdea.value = await kkumzApi.generateLogos(selectedIdeaId.value, {
      brandName: selectedNameObj.value?.brandName || currentIdea.value?.selectedBrandName,
      stylePreference: 'MODERN',
    });
    const sel = (currentIdea.value.logoSuggestions || []).find((l: any) => l.isSelected) || currentIdea.value.logoSuggestions?.[0];
    selectedLogoId.value = sel?.id || '';
    showToast('벡터 SVG 로고 6종이 생성되었습니다.');
  } finally {
    busy.value = false;
  }
}

function selectNameCandidate(name: any) {
  selectedNameId.value = name.id;
}

function selectLogoCandidate(logo: any) {
  selectedLogoId.value = logo.id;
}

async function confirmBrandAndNext() {
  if (!selectedIdeaId.value) return;
  busy.value = true;
  try {
    currentIdea.value = await kkumzApi.selectBrand(selectedIdeaId.value, {
      nameSuggestionId: selectedNameId.value,
      logoSuggestionId: selectedLogoId.value,
    });
    ideas.value = (await kkumzApi.listIdeas()) || [];
    editableSpecMarkdown.value = currentIdea.value.specMarkdown || '';
    activeStep.value = 3;
    showToast('브랜드 네이밍과 로고가 확정되었습니다.');
  } finally {
    busy.value = false;
  }
}

async function saveSpecVersion() {
  if (!selectedIdeaId.value) return;
  busy.value = true;
  try {
    currentIdea.value = await kkumzApi.updateSpec(selectedIdeaId.value, {
      title: formStep1.value.title,
      promptText: formStep1.value.promptText,
      siteType: formStep1.value.siteType,
      specMarkdown: editableSpecMarkdown.value,
    });
    showToast('새 기획서 버전 스냅샷이 저장되었습니다.');
  } finally {
    busy.value = false;
  }
}

async function rollbackSpecVersion(versionNo: number) {
  if (!selectedIdeaId.value) return;
  busy.value = true;
  try {
    currentIdea.value = await kkumzApi.saveOrRollbackIdeaVersion(selectedIdeaId.value, {
      rollbackToVersionNo: versionNo,
    });
    editableSpecMarkdown.value = currentIdea.value.specMarkdown || '';
    showToast(`기획서가 v${versionNo} 버전으로 복원되었습니다.`);
  } finally {
    busy.value = false;
  }
}

async function provisionAndGoStep4() {
  if (!selectedIdeaId.value) return;
  busy.value = true;
  try {
    siteRuntime.value = await kkumzApi.scaffoldSite(selectedIdeaId.value);
    syncVisualFormFromRuntime(siteRuntime.value);
    activeStep.value = 4;
    showToast('라이브 멀티사이트 프로비저닝이 완료되었습니다.');
  } finally {
    busy.value = false;
  }
}

async function applyChatRefine() {
  if (!siteRuntime.value || !chatInstruction.value.trim()) return;
  busy.value = true;
  try {
    siteRuntime.value = await kkumzApi.chatRefineSite(siteRuntime.value.siteSlug, {
      instruction: chatInstruction.value.trim(),
    });
    syncVisualFormFromRuntime(siteRuntime.value);
    chatInstruction.value = '';
    showToast('대화형 디자인 수정 사항이 프리뷰에 즉시 반영되었습니다.');
  } finally {
    busy.value = false;
  }
}

async function applyQuickTheme(instruction: string) {
  chatInstruction.value = instruction;
  await applyChatRefine();
}

async function saveVisualThemeOverrides() {
  if (!siteRuntime.value) return;
  busy.value = true;
  try {
    siteRuntime.value = await kkumzApi.chatRefineSite(siteRuntime.value.siteSlug, {
      instruction: '',
      tokenOverrides: {
        primary: visualForm.value.primaryColor,
        primaryColor: visualForm.value.primaryColor,
        surface: visualForm.value.surfaceColor,
        surfaceColor: visualForm.value.surfaceColor,
      },
      layoutOverrides: {
        customBadge: visualForm.value.customBadge,
        heroTitle: visualForm.value.heroTitle,
        heroSubtitle: visualForm.value.heroSubtitle,
        ctaText: visualForm.value.ctaText,
        primaryCta: visualForm.value.ctaText,
        showFlashDeal: visualForm.value.showFlashDeal,
        showCmsArticles: visualForm.value.showCmsArticles,
        showCmsForm: visualForm.value.showCmsForm,
        showAiConcierge: visualForm.value.showAiConcierge,
      },
    });
    syncVisualFormFromRuntime(siteRuntime.value);
    showToast('배너 문구·컬러·섹션 노출 설정이 라이브 프리뷰에 즉시 반영되었습니다!');
  } finally {
    busy.value = false;
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
    stock: 0,
    unit: '개',
    brandName: siteRuntime.value?.siteName || '',
    productCategoryName: '',
  });
}

function addEditableArticle() {
  editableArticles.value.push({
    id: Date.now(),
    title: '',
    author: '',
    description: '',
    summary: '',
    bodyHtml: '',
    categoryName: '',
    cover: '',
    clicks: 0,
    viewCount: 0,
  });
}

async function saveCatalogAndArticlesOverrides() {
  if (!siteRuntime.value) return;
  busy.value = true;
  try {
    const normalizedProducts = editableProducts.value.map((p) => ({
      ...p,
      price: Number(p.promotionPrice || p.price || 0),
      promotionPrice: Number(p.promotionPrice || p.price || 0),
    }));
    const normalizedArticles = editableArticles.value.map((a) => ({
      ...a,
      summary: a.description || a.summary || '',
      bodyHtml: `<p>${a.description || a.summary || ''}</p>`,
    }));
    siteRuntime.value = await kkumzApi.chatRefineSite(siteRuntime.value.siteSlug, {
      instruction: '',
      layoutOverrides: {
        customProducts: normalizedProducts,
        customArticles: normalizedArticles,
      },
    });
    syncVisualFormFromRuntime(siteRuntime.value);
    showToast('진열 상품 및 매거진 아티클 구성이 즉시 업데이트되었습니다.');
  } finally {
    busy.value = false;
  }
}

async function loadWebIde(slug: string) {
  const data = await kkumzApi.getWebIdeFiles(slug);
  webIdeData.value = data;
  webIdeFiles.value = (data.files || []).map((f: any) => ({ ...f }));
  activeFileIndex.value = 0;
}

async function saveAndBuildWebIde() {
  if (!activeSiteSlug.value) return;
  busy.value = true;
  try {
    const res = await kkumzApi.updateWebIdeFiles(activeSiteSlug.value, {
      files: webIdeFiles.value,
    });
    webIdeData.value = res;
    webIdeFiles.value = (res.files || []).map((f: any) => ({ ...f }));
    showToast(
      res.buildStatus === 'FAILED'
        ? '빌드 검증 중 구문 오류가 감지되었습니다. 우측 상단 AI 빌드 오류 자동 패치 실행 버튼으로 복구할 수 있습니다.'
        : '번들 빌드가 정상 완료되었습니다.',
    );
  } finally {
    busy.value = false;
  }
}

async function runAiFixBuild() {
  if (!activeSiteSlug.value) return;
  busy.value = true;
  try {
    const res = await kkumzApi.aiFixBuildError(activeSiteSlug.value);
    webIdeData.value = res;
    webIdeFiles.value = (res.files || []).map((f: any) => ({ ...f }));
    showToast('AI가 템플릿 구문 오류를 수정하고 빌드를 완료했습니다.');
  } finally {
    busy.value = false;
  }
}

async function executeDeploy() {
  if (!siteRuntime.value) return;
  busy.value = true;
  try {
    siteRuntime.value = await kkumzApi.deploySite(siteRuntime.value.siteSlug, {
      deployMode: deployForm.value.deployMode,
      releaseNote: deployForm.value.releaseNote,
      visibility: 'PUBLIC',
    });
    deployForm.value.releaseNote = '';
    showToast(`v${siteRuntime.value.activeVersion} 라이브 릴리즈 배포가 완료되었습니다!`);
  } finally {
    busy.value = false;
  }
}

async function rollbackRelease(versionNo: number) {
  if (!siteRuntime.value) return;
  busy.value = true;
  try {
    siteRuntime.value = await kkumzApi.rollbackSiteRelease(siteRuntime.value.siteSlug, versionNo);
    syncVisualFormFromRuntime(siteRuntime.value);
    showToast(`라이브 사이트가 v${versionNo} 릴리즈로 즉시 롤백되었습니다.`);
  } finally {
    busy.value = false;
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
  grid-template-columns: 280px 1fr;
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
}
.studio-toast {
  background: #0f172a;
  color: #fff;
  padding: 12px 18px;
  border-radius: 12px;
  margin-bottom: 18px;
  font-size: 13px;
  font-weight: 700;
}
.canvas-panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 28px;
  display: flex;
  flex-direction: column;
  gap: 22px;
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
  grid-template-columns: 1fr 300px;
  gap: 20px;
}
.spec-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.visual-spec-summary {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
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
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
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
}
.blueprint-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 18px;
}
.blueprint-sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: #64748b;
}
.screen-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin-top: 14px;
}
.screen-item {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
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
}
.version-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px;
  background: #f8fafc;
}
.ver-top {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  margin-bottom: 6px;
}
.ver-no {
  font-weight: 800;
  color: #4f46e5;
}
.ver-title {
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 8px;
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
}
.preview-split {
  display: grid;
  grid-template-columns: 360px 1fr;
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
</style>
