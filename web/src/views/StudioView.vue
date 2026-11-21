<template>
  <div class="studio-shell">
    <StudioTopbar
      :busy="busy"
      :selected-idea-id="selectedIdeaId"
      :ideas="ideas"
      :easy-mode="easyMode"
      :active-site-slug="activeSiteSlug"
      :site-runtime="siteRuntime"
      @go-feed="router.push('/feed')"
      @select-idea="onSelectIdea"
      @set-mode="setStudioMode"
      @open-live-site="openLiveSiteBySlug"
    />

    <StudioAccessDeniedSection
      v-if="accessDenied"
      :access-denied-brand="accessDeniedBrand"
      :access-denied-owner="accessDeniedOwner"
      @go-workspace="router.push('/workspace')"
      @go-feed="router.push('/feed')"
      @switch-account="openAuthModal('login', () => initStudio(), '이 사이트의 소유자 또는 협업 팀원 계정으로 로그인해 주세요.')"
    />

    <div v-else class="studio-body">
      <StudioSidebar
        :current-idea="currentIdea"
        :preview-logo-svg="previewLogoSvg"
        :preview-brand-name="previewBrandName"
        :site-type="formStep1.siteType"
        :easy-mode="easyMode"
        :displayed-steps="displayedSteps"
        :active-step="activeStep"
        :busy="busy"
        @go-step="goToStep"
        @set-mode="setStudioMode"
      />

      <section class="studio-canvas">
        <StudioStatusBanners
          :toast-message="toastMessage"
          :toast-type="toastType"
          :busy="busy"
          :busy-message="busyMessage"
          :busy-steps="busySteps"
          :error-banner="errorBanner"
          :active-step="activeStep"
          @clear-toast="toastMessage = ''"
          @retry-error="runErrorRetry"
          @force-step2="forceMoveToStep2"
          @dismiss-error="errorBanner = null"
        />

        <!-- 1단계: 아이디어 정의 -->
        <div
          v-if="activeStep === 1"
          class="canvas-panel"
          :class="{ 'is-busy-locked': busy }"
          :inert="busy || undefined"
        >
          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 01 · 만들고 싶은 사이트 알려주기</span>
              <h1 class="studio-main-heading">어떤 사이트를 만들고 싶으신가요?</h1>
              <p>만들고 싶은 사이트를 편하게 한 줄로 적어주시면, AI가 어울리는 이름·로고부터 꼭 필요한 기능까지 알아서 챙겨드립니다.</p>
            </div>
            <div class="header-btns">
              <button
                type="button"
                class="fasttrack-btn"
                data-testid="step1-fasttrack-btn"
                :disabled="busy"
                @click="runOneClickAutoComplete"
              >
                {{ busy ? '자동 완성 중...' : '1분 자동 완성: 완성된 사이트 바로 보기' }}
              </button>
              <button
                type="button"
                class="primary-btn"
                data-testid="step1-next-btn"
                :disabled="busy"
                @click="saveCurrentIdeaAndGoStep2"
              >
                {{ busy ? (busyMessage || '저장 및 AI 생성 중...') : '다음: 어울리는 이름과 로고 추천받기 →' }}
              </button>
            </div>
          </div>

          <div v-if="studioPresets.length > 0" class="starter-preset-bar">
            <span class="starter-label">어떻게 적을지 고민되신다면 예시를 눌러보세요</span>
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

          <div class="field-group prompt-hero-group">
            <div class="field-label-split">
              <label>만들고 싶은 사이트 설명</label>
              <div class="intent-action-btns">
                <button
                  type="button"
                  class="ai-intent-btn"
                  :disabled="busy || analyzingIntent"
                  @click="runIntentAnalysis(false)"
                >
                  {{ analyzingIntent ? 'AI 맞춤 기능 분석 중...' : 'AI로 내 설명에 맞는 기능 미리 추천받기' }}
                </button>
                <button
                  v-if="!easyMode || showStep1MetaPanel"
                  type="button"
                  class="ai-intent-sub-btn"
                  :disabled="busy || analyzingIntent"
                  @click="runIntentAnalysis(true)"
                >
                  설명 문구 더 풍성하게 자동 확장
                </button>
              </div>
            </div>
            <textarea
              v-model="formStep1.promptText"
              rows="3"
              data-testid="step1-prompt-textarea"
              placeholder="예: 엠파시 회사 홈페이지 만들고 싶어 / 충남 공주 냇가 옆 평상 백숙 민박 예약 사이트 만들고 싶어"
              @input="onStep1PromptInput"
            ></textarea>
          </div>

          <div class="easy-step1-cta-bar">
            <div class="easy-cta-left">
              <strong>한 줄만 적으셨다면 바로 다음으로 넘어가셔도 됩니다!</strong>
              <span>복잡한 설정과 필요한 기능 구성은 AI가 뒤에서 자동으로 완성해 드립니다.</span>
            </div>
            <div class="easy-cta-btns">
              <button
                type="button"
                class="fasttrack-btn"
                :disabled="busy"
                @click="runOneClickAutoComplete"
              >
                1분 자동 완성: 완성된 사이트 바로 보기
              </button>
              <button
                type="button"
                class="primary-btn"
                :disabled="busy"
                @click="saveCurrentIdeaAndGoStep2"
              >
                다음: 어울리는 이름과 로고 추천받기 →
              </button>
            </div>
          </div>

          <div class="ai-intent-banner" data-testid="ai-intent-summary-card">
            <div class="ai-intent-head">
              <div class="ai-intent-badges">
                <span class="intent-kicker-pill">AI 맞춤 사이트 추천</span>
                <span class="intent-arch-badge">
                  추천 사이트 형태: {{ formatSiteTypeLabel(formStep1.siteType) }}
                </span>
                <span v-if="aiIntentAnalysis.heroStyle" class="intent-hero-badge">
                  추천 첫 화면 분위기: {{ formatHeroStyleLabel(aiIntentAnalysis.heroStyle, heroStyles) }}
                </span>
              </div>
              <div v-if="!easyMode || showStep1MetaPanel" class="arch-override-group">
                <span class="override-label">사이트 형태 직접 변경:</span>
                <button
                  v-for="t in siteTypes"
                  :key="t.code"
                  type="button"
                  class="arch-mini-chip"
                  :class="{ active: formStep1.siteType === t.code }"
                  @click="formStep1.siteType = t.code"
                >
                  {{ formatSiteTypeLabel(t.code) }}
                </button>
              </div>
            </div>

            <p v-if="aiIntentAnalysis.siteTypeReason" class="ai-intent-reason">
              <strong>AI 추천 안내:</strong> {{ aiIntentAnalysis.siteTypeReason }}
            </p>
            <p v-else class="ai-intent-reason muted">
              위 입력창에 만들고 싶은 사이트를 적고 <strong>[다음: 어울리는 이름과 로고 추천받기 →]</strong>를 누르면, AI가 가장 잘 맞는 사이트 형태와 필수 기능을 알아서 세팅합니다.
            </p>

            <div class="easy-feature-summary-box" data-testid="easy-feature-summary">
              <div class="easy-feat-summary-top">
                <strong>
                  내 사이트에 포함될 기능
                  <span class="easy-feat-count">
                    · {{ enabledFeatureSummaries.length > 0 ? `${enabledFeatureSummaries.length}가지 선택됨` : '다음 단계 이동 시 AI가 자동 구성' }}
                  </span>
                </strong>
                <button
                  type="button"
                  class="easy-link-btn"
                  @click="showFeatureCatalogPanel = !showFeatureCatalogPanel"
                >
                  {{ showFeatureCatalogPanel ? '기능 목록 접기 ▲' : '+ 다른 기능 더 찾아보기 ▼' }}
                </button>
              </div>
              <div v-if="enabledFeatureSummaries.length > 0" class="easy-feat-pill-row">
                <span
                  v-for="item in enabledFeatureSummaries.slice(0, 8)"
                  :key="item.code"
                  class="easy-feat-pill"
                >
                  {{ item.title }}
                </span>
                <span v-if="enabledFeatureSummaries.length > 8" class="easy-feat-more-pill">
                  외 {{ enabledFeatureSummaries.length - 8 }}가지
                </span>
              </div>
              <p v-else class="easy-feat-empty-hint">
                일일이 고르지 않으셔도 괜찮습니다. 다음 버튼을 누르면 소개 배너, 주요 안내, 자주 묻는 질문, 문의·신청창 등 꼭 필요한 기능을 AI가 자동으로 담아드립니다.
              </p>
            </div>

            <div
              v-if="aiIntentAnalysis.expandedPrompt && aiIntentAnalysis.expandedPrompt !== formStep1.promptText"
              class="expanded-prompt-box"
            >
              <div class="expanded-prompt-top">
                <strong>AI가 더 자세하게 다듬은 사이트 설명 제안</strong>
                <button type="button" class="apply-expanded-btn" @click="applyExpandedPrompt">
                  이 설명으로 적용 →
                </button>
              </div>
              <p>{{ aiIntentAnalysis.expandedPrompt }}</p>
            </div>
          </div>

          <div class="progressive-disclosure-bar">
            <button
              type="button"
              class="disclosure-toggle-btn"
              data-testid="toggle-feature-catalog-btn"
              :class="{ open: showFeatureCatalogPanel || !easyMode }"
              @click="showFeatureCatalogPanel = !showFeatureCatalogPanel"
            >
              <span>{{ showFeatureCatalogPanel ? '원하는 기능 직접 골라 담기 닫기 ▲' : '+ 원하는 기능 직접 골라 담기 ▼' }}</span>
              <small>선택 {{ enabledFeatures.length }}가지</small>
            </button>

            <button
              type="button"
              class="disclosure-toggle-btn"
              data-testid="toggle-asset-upload-btn"
              :class="{ open: showAssetUploadPanel || !easyMode || ideaAssets.length > 0 }"
              @click="showAssetUploadPanel = !showAssetUploadPanel"
            >
              <span>{{ showAssetUploadPanel ? '내 사진·로고·소개자료 올리기 닫기 ▲' : '+ 내 사진·로고·소개자료 올리기 ▼' }}</span>
              <small>{{ ideaAssets.length }}건 등록됨</small>
            </button>

            <button
              type="button"
              class="disclosure-toggle-btn"
              data-testid="toggle-step1-meta-btn"
              :class="{ open: showStep1MetaPanel || !easyMode }"
              @click="showStep1MetaPanel = !showStep1MetaPanel"
            >
              <span>{{ showStep1MetaPanel ? '업종·키워드 세부 설정 닫기 ▲' : '+ 업종·키워드 세부 설정 ▼' }}</span>
              <small>AI 자동 판단 중</small>
            </button>
          </div>

          <div v-show="showStep1MetaPanel || !easyMode" class="step1-collapsible-section" data-testid="step1-meta-panel">
            <div class="field-group" style="margin-bottom: 14px">
              <label>프로젝트 관리용 제목</label>
              <input
                v-model="formStep1.title"
                type="text"
                placeholder="예: 엠파시 공식 브랜드 홈페이지 / 공주 맑은물 민박"
              />
            </div>

            <div class="step1-meta-grid">
              <div class="field-group">
                <label>업종 · 분야 선택</label>
                <select v-model="formStep1.category" class="step1-select">
                  <option value="AUTO">AI 자동 파악</option>
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
                    AI 자동 파악 업종: {{ formStep1.category }}
                  </option>
                  <option value="ACCOMMODATION">숙박 · 민박 · 감성 스테이 · 펜션</option>
                  <option value="FOOD_BEVERAGE">식음료 · 식당 · 로스터리 카페 · 베이커리</option>
                  <option value="RETAIL_CRAFT">공예 · 리빙 · 패션 · 라이프스타일 편집숍</option>
                  <option value="CULTURE_CLASS">원데이 클래스 · 전시 · 살롱 · 공간 대관</option>
                  <option value="LOCAL_BRAND">로컬 특산물 · 농수산 직거래 · 팜투테이블</option>
                  <option value="TECH_SERVICE">회사 소개 · IT · 크리에이터 · 일반 서비스</option>
                </select>
              </div>

              <div class="field-group">
                <label>핵심 키워드 태그</label>
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
                      placeholder="키워드 입력 후 Enter"
                      @keydown.enter.prevent="addStep1Tag"
                    />
                    <button type="button" class="tiny-add-btn" @click="addStep1Tag">+ 태그 추가</button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <StudioStep1AssetUploadPanel
            v-show="showAssetUploadPanel || !easyMode"
            :idea-assets="ideaAssets"
            :asset-types="assetTypes"
            :selectable-asset-types="selectableAssetTypes"
            :asset-type-labels="assetTypeLabels"
            :upload-asset-type="uploadAssetType"
            :busy="busy"
            :asset-uploading="assetUploading"
            @update:upload-asset-type="(val) => (uploadAssetType = val)"
            @upload-files="(files) => handleUploadIdeaFiles(files, uploadAssetType)"
            @asset-meta-change="onAssetMetaChange"
            @apply-asset-text="applyAssetTextToPrompt"
            @remove-asset="removeIdeaAsset"
          />

          <StudioStep1FeatureCatalogPanel
            v-show="showFeatureCatalogPanel || !easyMode"
            :feature-catalog="featureCatalog"
            :feature-categories="featureCategories"
            :filtered-feature-catalog="filteredFeatureCatalog"
            :recommended-feature-codes="recommendedFeatureCodes"
            :enabled-features="enabledFeatures"
            :ai-feature-reasons="aiFeatureReasons"
            :feature-search-query="featureSearchQuery"
            :active-feature-category-tab="activeFeatureCategoryTab"
            :active-engine-filter="activeEngineFilter"
            :busy="busy"
            :selected-idea-id="selectedIdeaId"
            @update:feature-search-query="(val) => (featureSearchQuery = val)"
            @update:active-feature-category-tab="(val) => (activeFeatureCategoryTab = val)"
            @update:active-engine-filter="(val) => (activeEngineFilter = val)"
            @select-recommended="selectRecommendedFeatures"
            @save-features="saveEnabledFeaturesToServer(true)"
            @toggle-feature="toggleFeatureCard"
          />

          <div class="panel-actions-split">
            <button type="button" class="primary-btn" :disabled="busy" @click="saveCurrentIdeaAndGoStep2">
              {{ busy ? (busyMessage || '저장 및 AI 생성 중...') : '다음: 어울리는 이름과 로고 추천받기 →' }}
            </button>
            <button type="button" class="fasttrack-btn" :disabled="busy" @click="runOneClickAutoComplete">
              {{ busy ? '자동 완성 중...' : '1분 자동 완성: 완성된 사이트 바로 보기' }}
            </button>
            <button type="button" class="secondary-btn" :disabled="busy" @click="createNewIdeaFromStep1">
              {{ busy ? '처리 중...' : '+ 완전히 새로운 사이트 프로젝트로 시작' }}
            </button>
          </div>
        </div>

        <!-- 2단계: 네이밍 & 로고 -->
        <StudioStep2BrandPanel
          v-else-if="activeStep === 2"
          :busy="busy"
          :asset-uploading="assetUploading"
          :easy-mode="easyMode"
          :naming-tone-preference="namingTonePreference"
          :keyword-hint="keywordHint"
          :custom-brand-form="customBrandForm"
          :selected-name-obj="selectedNameObj"
          :selected-logo-obj="selectedLogoObj"
          :preview-logo-svg="previewLogoSvg"
          :preview-brand-name="previewBrandName"
          :active-site-slug="activeSiteSlug"
          :generating-names="generatingNames"
          :generating-logos="generatingLogos"
          :name-suggestions="nameSuggestions"
          :logo-suggestions="logoSuggestions"
          :selected-name-id="selectedNameId"
          :selected-logo-id="selectedLogoId"
          @update:naming-tone-preference="(val) => (namingTonePreference = val)"
          @update:keyword-hint="(val) => (keywordHint = val)"
          @regenerate-names="regenerateNames"
          @regenerate-logos="regenerateLogos"
          @upload-logo="handleStep2LogoUpload"
          @confirm-brand="confirmBrandAndNext"
          @select-name="selectNameCandidate"
          @select-logo="selectLogoCandidate"
          @custom-palette-change="onCustomPaletteChange"
          @custom-brand-name-input="onCustomBrandNameInput"
          @custom-slug-input="onCustomSlugInput"
        />

        <!-- 3단계: 기능명세서 & 화면설계 -->
        <StudioStep3SpecPanel
          v-else-if="activeStep === 3"
          :busy="busy"
          :spec-edit-mode="specEditMode"
          :current-idea="currentIdea"
          :active-site-slug="activeSiteSlug"
          :preview-brand-name="previewBrandName"
          :editable-highlights="editableHighlights"
          :editable-spec-markdown="editableSpecMarkdown"
          :editable-screens="editableScreens"
          :idea-versions="ideaVersions"
          :layout-labels="layoutLabels"
          :component-labels="componentLabels"
          @update:spec-edit-mode="(val) => (specEditMode = val)"
          @update:editable-spec-markdown="(val) => (editableSpecMarkdown = val)"
          @save-spec="saveSpecVersion"
          @provision-step4="provisionAndGoStep4"
          @add-highlight="addSpecHighlightItem"
          @remove-highlight="removeSpecHighlightItem"
          @sync-highlights-to-markdown="syncHighlightsToMarkdown"
          @sync-markdown-to-highlights="syncMarkdownToHighlights"
          @add-screen="addEditableScreen"
          @remove-screen="removeEditableScreen"
          @move-screen="moveEditableScreen"
          @regenerate-spec="regenerateSpec"
          @rollback-version="rollbackSpecVersion"
        />

        <!-- 4단계: 실시간 프리뷰 편집 -->
        <div
          v-else-if="activeStep === 4"
          class="canvas-panel"
          :class="{ 'is-busy-locked': busy }"
          :inert="busy || undefined"
        >
          <div class="panel-header">
            <div>
              <span class="step-kicker">
                {{
                  easyMode
                    ? 'STEP 03 · 내 사이트 꾸미기 & 바로 오픈'
                    : 'STEP 04 · 실시간 사이트 미리보기 & 디자인·상품 편집'
                }}
              </span>
              <h2>
                {{
                  easyMode
                    ? '실시간으로 내 사이트 화면을 보면서 문구·사진·상품을 고치고 바로 오픈하세요'
                    : '실시간 디자인·문구·상품 편집 및 스토어 프리뷰'
                }}
              </h2>
              <p>우측 화면에서 바꾸고 싶은 곳을 클릭하거나, 좌측에서 AI에게 말로 지시해 보세요.</p>
            </div>
            <div class="header-btns">
              <div class="viewport-btns">
                <button type="button" :class="{ active: viewportMode === 'DESKTOP' }" @click="viewportMode = 'DESKTOP'">
                  PC · 100%
                </button>
                <button type="button" :class="{ active: viewportMode === 'TABLET' }" @click="viewportMode = 'TABLET'">
                  태블릿 · 768px
                </button>
                <button type="button" :class="{ active: viewportMode === 'MOBILE' }" @click="viewportMode = 'MOBILE'">
                  모바일 · 390px
                </button>
              </div>
              <button
                type="button"
                class="instant-open-btn"
                data-testid="step4-instant-open-btn"
                :disabled="busy || !siteRuntime"
                @click="executeQuickLiveOpen"
              >
                {{ busy ? '오픈 반영 중...' : '내 사이트 지금 바로 오픈' }}
              </button>
              <button v-if="!easyMode" type="button" class="secondary-btn" @click="goToStep(5)">
                5단계: 코드 편집
              </button>
              <button v-if="!easyMode" type="button" class="primary-btn" @click="goToStep(6)">
                6단계: 배포 세부 설정 →
              </button>
            </div>
          </div>

          <div
            v-if="liveOpenCelebration && siteRuntime"
            class="live-open-celebration-banner"
            data-testid="live-open-celebration"
          >
            <div class="celebration-left">
              <span class="celebration-badge">사이트 오픈 완료</span>
              <div class="celebration-text">
                <strong>축하합니다! '{{ visualForm.siteName || siteRuntime.siteName }}' 사이트가 인터넷에 바로 오픈되었습니다.</strong>
                <p>
                  내 사이트 인터넷 주소:
                  <code data-testid="live-open-slug-code">/s/{{ siteRuntime.siteSlug }}</code>
                  · v{{ siteRuntime.activeVersion }} · {{ formatDeployModeLabel(siteRuntime.deployMode) }}
                </p>
              </div>
            </div>
            <div class="celebration-actions">
              <button
                type="button"
                class="primary-btn"
                data-testid="open-live-site-btn"
                @click="openLiveSiteBySlug(siteRuntime.siteSlug)"
              >
                오픈된 내 사이트 바로가기 /s/{{ siteRuntime.siteSlug }} ↗
              </button>
              <button
                type="button"
                class="secondary-btn"
                data-testid="copy-live-url-btn"
                @click="copyLiveSiteUrl"
              >
                사이트 주소 복사
              </button>
              <button
                type="button"
                class="secondary-btn"
                @click="liveOpenCelebration = false"
              >
                닫기
              </button>
            </div>
          </div>

          <div
            v-if="siteRuntime && !siteRuntime.isPublished && !liveOpenCelebration"
            class="studio-draft-notice-bar"
            data-testid="step4-draft-notice"
          >
            <div class="draft-notice-left">
              <span class="draft-notice-chip">비공개 초안</span>
              <p>
                나만 볼 수 있는 사이트 초안이 생성되었습니다. 배포 단계에서 라이브 오픈을 실행하면 다른 사용자에게 공개됩니다.
              </p>
            </div>
            <button
              type="button"
              class="primary-btn"
              :disabled="busy"
              @click="executeQuickLiveOpen"
            >
              라이브 오픈 실행
            </button>
          </div>
          <div
            v-else-if="siteRuntime && siteRuntime.isPublished && siteRuntime.hasUnpublishedChanges && !liveOpenCelebration"
            class="studio-draft-notice-bar diff"
            data-testid="step4-unpublished-diff-notice"
          >
            <div class="draft-notice-left">
              <span class="draft-notice-chip diff">미배포 수정본 있음 (작업 v{{ siteRuntime.activeVersion }})</span>
              <p>
                현재 방문자에게는 배포 버전 v{{ siteRuntime.publishedVersion }}이 보이며, 편집 중인 작업본 v{{ siteRuntime.activeVersion }}은 나만 미리보기로 볼 수 있습니다.
              </p>
            </div>
            <button
              type="button"
              class="primary-btn"
              :disabled="busy"
              @click="executeQuickLiveOpen"
            >
              수정본 라이브 재배포
            </button>
          </div>

          <div class="preview-split" v-if="siteRuntime">
            <div class="chat-panel">
              <div class="status-box">
                <div class="status-row">
                  <span>내 사이트 인터넷 주소</span>
                  <code>/s/{{ siteRuntime.siteSlug }}</code>
                </div>
                <div class="status-row">
                  <span>사이트 유형</span>
                  <strong>{{ formatSiteTypeLabel(siteRuntime.siteType) }}</strong>
                </div>
                <div class="status-row">
                  <span>외부 공개 상태</span>
                  <strong v-if="siteRuntime.isPublished">
                    배포 완료 (v{{ siteRuntime.publishedVersion || siteRuntime.activeVersion }})
                  </strong>
                  <strong v-else>비공개 초안</strong>
                </div>
                <div class="status-row">
                  <span>현재 작업 버전</span>
                  <strong>v{{ siteRuntime.activeVersion }} · {{ formatDeployModeLabel(siteRuntime.deployMode) }}</strong>
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
                  {{ busy ? '실시간 반영 중...' : '대화형 수정 즉시 반영' }}
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
                  <span class="preset-title">원클릭 배너 카피 &amp; 레이아웃 지시</span>
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
                <div v-if="themeArchetypes.length" class="mini-field">
                  <label>비주얼 테마 아키타입</label>
                  <select v-model="visualForm.themeArchetype">
                    <option v-for="arch in themeArchetypes" :key="arch.code" :value="arch.code">
                      {{ arch.label }}
                    </option>
                  </select>
                </div>
                <div v-if="heroStyles.length" class="mini-field">
                  <label>히어로 레이아웃 스타일</label>
                  <select v-model="visualForm.heroStyle">
                    <option v-for="hs in heroStyles" :key="hs.code" :value="hs.code">
                      {{ hs.label }}
                    </option>
                  </select>
                </div>
                <div class="color-picker-row">
                  <div class="mini-field">
                    <label>서피스 무드</label>
                    <select v-model="visualForm.surfaceMode">
                      <option value="LIGHT">라이트 (화이트)</option>
                      <option value="WARM">웜 (아이보리/페이퍼)</option>
                      <option value="DARK">다크 (스튜디오 블랙)</option>
                    </select>
                  </div>
                  <div v-if="fontPresets.length" class="mini-field">
                    <label>타이포그래피 프리셋</label>
                    <select v-model="visualForm.fontPreset">
                      <option v-for="fp in fontPresets" :key="fp.code" :value="fp.code">
                        {{ fp.label }}
                      </option>
                    </select>
                  </div>
                </div>
                <div class="mini-field">
                  <label>상단 GNB 사이트 브랜드명</label>
                  <input v-model="visualForm.siteName" type="text" placeholder="예: 냇가식당, 청류민박" />
                </div>
                <div class="mini-field">
                  <label>브랜드 한줄 슬로건</label>
                  <input v-model="visualForm.brandSlogan" type="text" placeholder="브랜드 핵심 슬로건" />
                </div>

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
                      placeholder="배너 사진 캡션"
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
                    @change="(e: Event) => onPickHeroAsset((e.target as HTMLSelectElement).value)"
                  >
                    <option value="">-- 업로드된 사진 보관함에서 배너 사진 선택 · {{ imageAssets.length }}장 --</option>
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
                  <label>메인 행동 유도 버튼 문구</label>
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
                  <label>카드·버튼 모서리 곡률</label>
                  <select v-model="visualForm.radius">
                    <option value="6px">샤프 모던 · 6px</option>
                    <option value="12px">스탠다드 라운드 · 12px</option>
                    <option value="16px">소프트 감성 라운드 · 16px</option>
                    <option value="24px">풀 라운드 · 24px</option>
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
                    <span>브랜드 매거진 &amp; 아티클 섹션 표시</span>
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
                  <strong>진열 상품·객실·메뉴 직접 편집 · {{ editableProducts.length }}종</strong>
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
                          @change="(e: Event) => { const v = (e.target as HTMLSelectElement).value; if (v) prod.pic = v; }"
                        >
                          <option value="">보관함 사진에서 선택 · {{ imageAssets.length }}</option>
                          <option v-for="img in imageAssets" :key="img.id" :value="img.fileUrl">
                            {{ img.caption || img.originalName }}
                          </option>
                        </select>
                      </div>
                    </div>
                    <input v-model="prod.pic" type="text" placeholder="상품 이미지 URL 직접 입력" />
                    <div class="editable-price-row">
                      <input v-model="prod.name" type="text" placeholder="상품/객실/메뉴명" />
                      <input v-model="prod.productCategoryName" type="text" placeholder="카테고리" />
                    </div>
                    <div class="editable-price-row">
                      <input v-model.number="prod.promotionPrice" type="number" placeholder="판매가(원)" title="실제 판매가(원)" />
                      <input v-model.number="prod.price" type="number" placeholder="정상가(원)" title="정상가(원)" />
                    </div>
                    <div class="editable-price-row">
                      <input v-model="prod.unit" type="text" placeholder="단위" />
                      <input v-model.number="prod.stock" type="number" placeholder="일일 재고/객실수" />
                    </div>
                    <input v-model="prod.subTitle" type="text" placeholder="상품 한줄 매력 포인트 및 포함 혜택 설명" />
                  </div>
                </div>

                <div class="catalog-edit-header" style="margin-top: 10px">
                  <strong>매거진 아티클 편집 · {{ editableArticles.length }}건</strong>
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
                          @change="(e: Event) => { const v = (e.target as HTMLSelectElement).value; if (v) art.cover = v; }"
                        >
                          <option value="">보관함 사진에서 선택 · {{ imageAssets.length }}</option>
                          <option v-for="img in imageAssets" :key="img.id" :value="img.fileUrl">
                            {{ img.caption || img.originalName }}
                          </option>
                        </select>
                      </div>
                    </div>
                    <input v-model="art.cover" type="text" placeholder="커버 이미지 URL 직접 입력" />
                    <input v-model="art.title" type="text" placeholder="매거진 아티클 제목" />
                    <div class="editable-price-row">
                      <input v-model="art.categoryName" type="text" placeholder="카테고리" />
                      <input v-model="art.author" type="text" placeholder="작성자" />
                    </div>
                    <input v-model="art.description" type="text" placeholder="목록 노출용 한줄 요약 설명" />
                    <textarea v-model="art.bodyHtml" rows="2" placeholder="아티클 상세 본문 내용"></textarea>
                  </div>
                </div>

                <div class="catalog-edit-header" style="margin-top: 10px">
                  <strong>포토 갤러리 {{ previewGalleryImages.length }}장 &amp; 참고문서 {{ previewDownloadableDocs.length }}건</strong>
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
                    placeholder="예: 온라인 예약·문의 접수"
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

              <div class="step4-left-open-box">
                <button
                  type="button"
                  class="instant-open-btn full"
                  data-testid="step4-left-instant-open-btn"
                  :disabled="busy"
                  @click="executeQuickLiveOpen"
                >
                  {{ busy ? '오픈 반영 중...' : '내 사이트 지금 바로 오픈' }}
                </button>
                <div class="step4-left-sub-links">
                  <button
                    type="button"
                    class="text-link-btn"
                    @click="openLiveSiteBySlug(siteRuntime.siteSlug)"
                  >
                    오픈된 내 사이트 화면 바로가기 /s/{{ siteRuntime.siteSlug }} ↗
                  </button>
                  <button
                    type="button"
                    class="text-link-btn"
                    @click="goToStep(6)"
                  >
                    도메인·공개범위 고급 설정 →
                  </button>
                </div>
              </div>
            </div>

            <StudioStep4LivePreviewStage
              :viewport-mode="viewportMode"
              :visual-form="visualForm"
              :site-runtime="siteRuntime"
              :preview-brand-name="previewBrandName"
              :preview-hero-style="previewHeroStyle"
              :preview-products="previewProducts"
              :preview-categories="previewCategories"
              :preview-gallery-images="previewGalleryImages"
              :preview-articles="previewArticles"
              :preview-downloadable-docs="previewDownloadableDocs"
              :preview-feature-blocks="previewFeatureBlocks"
              :editable-cms-form="editableCmsForm"
              @focus-tab="focusStep4Editor"
            />
          </div>
          <div v-else class="empty-step-box">
            <strong>아직 제작된 내 사이트 화면이 없습니다.</strong>
            <p>아래 버튼을 눌러 AI가 설계한 구성으로 내 사이트 화면 자동 제작을 실행해 주세요.</p>
            <button type="button" class="primary-btn" :disabled="busy" @click="provisionAndGoStep4">
              {{ busy ? '사이트 화면 제작 중...' : '내 사이트 화면 자동 제작 실행 →' }}
            </button>
          </div>
        </div>

        <!-- 5단계: WebIDE 코드 편집 -->
        <StudioStep5WebIdePanel
          v-else-if="activeStep === 5"
          :busy="busy"
          :active-site-slug="activeSiteSlug"
          :web-ide-data="webIdeData"
          :web-ide-files="webIdeFiles"
          :active-file-index="activeFileIndex"
          :active-web-ide-file="activeWebIdeFile"
          :web-ide-commit-msg="webIdeCommitMsg"
          :git-repo-info="gitRepoInfo"
          :show-git-config-form="showGitConfigForm"
          :git-form="gitForm"
          :ssh-key-download-url="activeSiteSlug ? kkumzApi.getSshKeyDownloadUrl(activeSiteSlug) : ''"
          :git-archive-download-url="activeSiteSlug ? kkumzApi.getGitArchiveDownloadUrl(activeSiteSlug) : ''"
          @go-step="goToStep"
          @sync-step4-to-webide="syncStep4ToWebIde"
          @update:web-ide-commit-msg="(val) => (webIdeCommitMsg = val)"
          @save-and-build="saveAndBuildWebIde"
          @ai-fix-build="runAiFixBuild"
          @update:show-git-config-form="(val) => (showGitConfigForm = val)"
          @provision-git-repo="provisionGitRepository"
          @push-webide-to-git="pushWebIdeToGit"
          @rotate-git-keys="rotateGitDeveloperKeys"
          @copy-text="copyTextToClipboard"
          @add-file="addNewWebIdeFile"
          @remove-file="removeWebIdeFile"
          @update:active-file-index="(idx) => (activeFileIndex = idx)"
          @format-json="formatActiveJsonFile"
        />

        <!-- 6단계: 빌드 & 배포 -->
        <StudioStep6DeployPanel
          v-else-if="activeStep === 6"
          :busy="busy"
          :site-runtime="siteRuntime"
          :preview-brand-name="previewBrandName"
          :preview-products-count="previewProducts.length"
          :preview-articles-count="previewArticles.length"
          :preview-gallery-count="previewGalleryImages.length"
          :preview-docs-count="previewDownloadableDocs.length"
          :deploy-form="deployForm"
          @open-live-site="openLiveSiteBySlug"
          @go-step="goToStep"
          @unpublish="executeUnpublish"
          @deploy="executeDeploy"
          @rollback-release="rollbackRelease"
          @provision-step4="provisionAndGoStep4"
        />
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
  kkumzApi,
  kkumzAuth,
  openAuthModal,
  formatSiteTypeLabel,
  formatDeployModeLabel,
} from '@/api/kkumzApi';
import StudioTopbar from './studio/StudioTopbar.vue';
import StudioAccessDeniedSection from './studio/StudioAccessDeniedSection.vue';
import StudioSidebar from './studio/StudioSidebar.vue';
import StudioStatusBanners from './studio/StudioStatusBanners.vue';
import StudioStep1AssetUploadPanel from './studio/StudioStep1AssetUploadPanel.vue';
import StudioStep1FeatureCatalogPanel from './studio/StudioStep1FeatureCatalogPanel.vue';
import StudioStep2BrandPanel from './studio/StudioStep2BrandPanel.vue';
import StudioStep3SpecPanel from './studio/StudioStep3SpecPanel.vue';
import StudioStep4LivePreviewStage from './studio/StudioStep4LivePreviewStage.vue';
import StudioStep5WebIdePanel from './studio/StudioStep5WebIdePanel.vue';
import StudioStep6DeployPanel from './studio/StudioStep6DeployPanel.vue';
import { formatHeroStyleLabel } from './studio/studioFormatters';
import { useStudioState } from './studio/useStudioState';
import { useStudioIdeAndDeploy } from './studio/useStudioIdeAndDeploy';
import { useStudioRefineAndAssets } from './studio/useStudioRefineAndAssets';
import { useStudioWorkflow } from './studio/useStudioWorkflow';

const router = useRouter();
const state = useStudioState();
const ideAndDeploy = useStudioIdeAndDeploy(state);
const refineAndAssets = useStudioRefineAndAssets(state);
const workflow = useStudioWorkflow(state, ideAndDeploy.loadWebIde);

const {
  easyMode,
  showFeatureCatalogPanel,
  showAssetUploadPanel,
  showStep1MetaPanel,
  liveOpenCelebration,
  displayedSteps,
  siteTypes,
  assetTypes,
  assetTypeLabels,
  studioPresets,
  layoutLabels,
  componentLabels,
  featureCatalog,
  featureCategories,
  heroStyles,
  themeArchetypes,
  fontPresets,
  enabledFeatures,
  aiIntentAnalysis,
  featureSearchQuery,
  activeFeatureCategoryTab,
  activeEngineFilter,
  analyzingIntent,
  step4HeroInputRef,
  step4CatalogImageInputRef,
  step4ExtraAssetInputRef,
  uploadAssetType,
  assetUploading,
  ideas,
  selectedIdeaId,
  currentIdea,
  accessDenied,
  accessDeniedOwner,
  accessDeniedBrand,
  activeStep,
  busy,
  busyMessage,
  busySteps,
  generatingNames,
  generatingLogos,
  toastMessage,
  toastType,
  errorBanner,
  specEditMode,
  step4Tab,
  formStep1,
  newTagInput,
  namingTonePreference,
  keywordHint,
  selectedNameId,
  selectedLogoId,
  customBrandForm,
  editableSpecMarkdown,
  editableHighlights,
  editableScreens,
  siteRuntime,
  viewportMode,
  chatInstruction,
  visualForm,
  editableProducts,
  editableArticles,
  editableCmsForm,
  webIdeData,
  webIdeFiles,
  activeFileIndex,
  webIdeCommitMsg,
  showGitConfigForm,
  gitForm,
  gitRepoInfo,
  deployForm,
  nameSuggestions,
  logoSuggestions,
  ideaVersions,
  ideaAssets,
  selectableAssetTypes,
  imageAssets,
  previewGalleryImages,
  previewDownloadableDocs,
  previewHeroStyle,
  selectedNameObj,
  selectedLogoObj,
  previewBrandName,
  previewLogoSvg,
  activeSiteSlug,
  activeWebIdeFile,
  previewProducts,
  previewCategories,
  previewArticles,
  previewFeatureBlocks,
  recommendedFeatureCodes,
  enabledFeatureSummaries,
  aiFeatureReasons,
  filteredFeatureCatalog,
  setStudioMode,
  openLiveSiteBySlug,
  onStep1PromptInput,
  runErrorRetry,
  forceMoveToStep2,
  formatAssetTypeLabel,
  toggleFeatureCard,
  selectRecommendedFeatures,
  applyExpandedPrompt,
  applyStarterPreset,
  addStep1Tag,
  renderLogoWithBrand,
  onCustomBrandNameInput,
  onCustomSlugInput,
  onCustomPaletteChange,
  addSpecHighlightItem,
  removeSpecHighlightItem,
  syncHighlightsToMarkdown,
  syncMarkdownToHighlights,
  addEditableScreen,
  moveEditableScreen,
} = state;

const {
  provisionGitRepository,
  pushWebIdeToGit,
  rotateGitDeveloperKeys,
  copyTextToClipboard,
  addNewWebIdeFile,
  removeWebIdeFile,
  formatActiveJsonFile,
  syncStep4ToWebIde,
  saveAndBuildWebIde,
  runAiFixBuild,
  executeDeploy,
  executeUnpublish,
  executeQuickLiveOpen,
  copyLiveSiteUrl,
  rollbackRelease,
} = ideAndDeploy;

const {
  focusStep4Editor,
  moveEditableProduct,
  moveEditableArticle,
  applyChatRefine,
  applyQuickTheme,
  handleUploadIdeaFiles,
  onAssetMetaChange,
  applyAssetTextToPrompt,
  removeIdeaAsset,
  handleStep2LogoUpload,
  triggerStep4HeroUpload,
  onStep4HeroFileChange,
  onPickHeroAsset,
  triggerCatalogItemImageUpload,
  onStep4CatalogImageFileChange,
  triggerStep4ExtraAssetUpload,
  onStep4ExtraAssetFileChange,
  saveVisualThemeOverrides,
  saveFormOverrides,
  addEditableProduct,
  addEditableArticle,
  saveCatalogAndArticlesOverrides,
} = refineAndAssets;

const {
  runIntentAnalysis,
  saveEnabledFeaturesToServer,
  removeEditableScreen,
  initStudio,
  onSelectIdea,
  goToStep,
  saveCurrentIdeaAndGoStep2,
  createNewIdeaFromStep1,
  runOneClickAutoComplete,
  regenerateNames,
  regenerateLogos,
  selectNameCandidate,
  selectLogoCandidate,
  confirmBrandAndNext,
  regenerateSpec,
  saveSpecVersion,
  rollbackSpecVersion,
  provisionAndGoStep4,
} = workflow;

watch(
  () => kkumzAuth.user?.username,
  () => {
    initStudio();
  },
);

onMounted(() => {
  initStudio();
});
</script>

<style scoped src="./studio/studio-view.css"></style>
