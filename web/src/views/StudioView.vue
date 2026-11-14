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
              <h2>어떤 사이트를 만들고 싶으신가요?</h2>
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
                  ✓ 내 사이트에 포함될 기능
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
                  ✓ {{ item.title }}
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
                    @change="(e: any) => onPickHeroAsset(e.target.value)"
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
                          @change="(e: any) => { if (e.target.value) prod.pic = e.target.value; }"
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
                          @change="(e: any) => { if (e.target.value) art.cover = e.target.value; }"
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
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  kkumzApi,
  kkumzAuth,
  isAuthenticated,
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
import {
  easySteps,
  steps,
  sanitizeBrandStoryText,
  renderLogoWithBrand as renderLogoWithBrandHelper,
  formatHeroStyleLabel,
  parseHighlightsFromMarkdown,
  isImageAsset,
  formatApiError,
  normalizeRuntime,
} from './studio/studioFormatters';

const route = useRoute();
const router = useRouter();

const easyMode = ref(true);
const showFeatureCatalogPanel = ref(false);
const showAssetUploadPanel = ref(false);
const showStep1MetaPanel = ref(false);
const liveOpenCelebration = ref(false);

const displayedSteps = computed(() => (easyMode.value ? easySteps : steps));

function setStudioMode(isEasy: boolean) {
  easyMode.value = isEasy;
  if (!isEasy) {
    showFeatureCatalogPanel.value = true;
    showStep1MetaPanel.value = true;
  } else {
    if (activeStep.value === 3 || activeStep.value === 5 || activeStep.value === 6) {
      activeStep.value = siteRuntime.value ? 4 : 2;
    }
  }
}

function openLiveSiteBySlug(siteSlug: string) {
  if (!siteSlug) return;
  router.push({ name: 'LiveSiteRendererView', params: { siteSlug } });
}

function onStep1PromptInput() {
  if (!formStep1.value.title.trim()) {
    const firstLine = formStep1.value.promptText
      .split(/\r?\n/)[0]
      .trim()
      .slice(0, 48);
    formStep1.value.title = firstLine;
  }
}

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

const step4HeroInputRef = ref<HTMLInputElement | null>(null);
const step4CatalogImageInputRef = ref<HTMLInputElement | null>(null);
const step4ExtraAssetInputRef = ref<HTMLInputElement | null>(null);

const uploadAssetType = ref('AUTO');
const assetUploading = ref(false);
const catalogUploadTarget = ref<{ kind: 'PRODUCT' | 'ARTICLE'; index: number } | null>(null);

const ideas = ref<any[]>([]);
const selectedIdeaId = ref('');
const currentIdea = ref<any | null>(null);
const accessDenied = ref(false);
const accessDeniedOwner = ref('');
const accessDeniedBrand = ref('');
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
  submitText: '온라인 신청 접수',
  conciergeGreeting: '',
});

const webIdeData = ref<any | null>(null);
const webIdeFiles = ref<Array<{ path: string; language: string; content: string }>>([]);
const activeFileIndex = ref(0);
const webIdeCommitMsg = ref('');

const showGitConfigForm = ref(false);
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

const previewHeroStyle = computed<Record<string, string>>(() => {
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

function renderLogoWithBrand(svgCode: any, overrideBrandName?: string): string {
  return renderLogoWithBrandHelper(svgCode, overrideBrandName, previewBrandName.value);
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

const enabledFeatureSummaries = computed<Array<{ code: string; title: string }>>(() => {
  const codes =
    enabledFeatures.value.length > 0
      ? enabledFeatures.value
      : recommendedFeatureCodes.value;
  return codes.map((code) => {
    const found = featureCatalog.value.find((f: any) => String(f.featureCode) === String(code));
    return {
      code: String(code),
      title: found?.title || String(code),
    };
  });
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
    showToast('먼저 AI로 내 설명에 맞는 기능 미리 추천받기를 실행해 주세요.', 'info');
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
    showToast('먼저 아이디어 상세 설명을 입력해 주세요.', 'error');
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
        visibility: 'PRIVATE',
      });
      ideas.value = (await kkumzApi.listIdeas({ mineOnly: true })) || [];
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
    ideas.value = (await kkumzApi.listIdeas({ mineOnly: true })) || [];
    showToast(
      `AI 분석 완료! 추천 아키텍처 ${formatSiteTypeLabel(formStep1.value.siteType)}와 맞춤 기능 ${enabledFeatures.value.length}종이 자동 구성되었습니다.`,
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

function formatAssetTypeLabel(code: string) {
  if (!code) return '';
  return assetTypeLabels.value[code] || code;
}

function showToast(msg: string, type: 'info' | 'success' | 'error' = 'info') {
  toastMessage.value = msg;
  toastType.value = type;
  const duration = type === 'error' ? 6800 : 3800;
  setTimeout(() => {
    if (toastMessage.value === msg) toastMessage.value = '';
  }, duration);
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
      // saveSpecVersion 시 동기화
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
    ctaText: layout.ctaText || layout.primaryCta || '시그니처 컬렉션 탐색',
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
    submitText: formObj.submitText || '온라인 신청 접수',
    conciergeGreeting:
      layout.conciergeGreeting ||
      `안녕하세요! ${normalized.siteName || '브랜드'} AI 컨시어지입니다. 상품·객실·요금·예약 관련 궁금한 점을 물어보세요.`,
  };

  deployForm.value.deployMode = normalized.deployMode || 'DYNAMIC_SDUI';
  deployForm.value.visibility = (normalized.isPublished ? normalized.visibility : 'PUBLIC') || 'PUBLIC';
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
  const routeIdeaId = route.params.ideaId ? String(route.params.ideaId) : '';
  if (!isAuthenticated.value && !routeIdeaId) {
    ideas.value = [];
    selectedIdeaId.value = '';
    currentIdea.value = null;
    openAuthModal(
      'login',
      () => initStudio(),
      'AI 브랜드 스튜디오에서 나만의 사이트를 생성·편집하려면 먼저 로그인 또는 회원가입해 주세요.',
    );
    return;
  }
  try {
    ideas.value = isAuthenticated.value ? (await kkumzApi.listIdeas({ mineOnly: true })) || [] : [];
    const targetId = routeIdeaId || (ideas.value[0]?.id ?? '');
    if (targetId) {
      await onSelectIdea(targetId);
    } else {
      selectedIdeaId.value = '';
      currentIdea.value = null;
      siteRuntime.value = null;
      accessDenied.value = false;
      activeStep.value = 1;
    }
    if (String(route.query.fastTrack || '') === '1' && formStep1.value.promptText.trim()) {
      router
        .replace({
          name: 'StudioView',
          params: selectedIdeaId.value ? { ideaId: selectedIdeaId.value } : undefined,
        })
        .catch(() => {});
      await runOneClickAutoComplete();
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
  if (busy.value && !allowWhenBusy) return;
  if (!ideaId) {
    selectedIdeaId.value = '';
    currentIdea.value = null;
    siteRuntime.value = null;
    accessDenied.value = false;
    activeStep.value = 1;
    formStep1.value = {
      siteType: 'COMMERCE',
      title: '',
      promptText: '',
      category: 'AUTO',
      tags: [],
    };
    enabledFeatures.value = [];
    if (route.params.ideaId) {
      router.replace({ name: 'StudioView' }).catch(() => {});
    }
    return;
  }
  selectedIdeaId.value = ideaId;
  errorBanner.value = null;
  accessDenied.value = false;
  if (String(route.params.ideaId || '') !== ideaId) {
    router.replace({ name: 'StudioView', params: { ideaId }, query: route.query }).catch(() => {});
  }
  try {
    const detail = await kkumzApi.getIdeaDetail(ideaId);
    if (selectedIdeaId.value !== ideaId) return;
    if (!detail.canEdit) {
      accessDenied.value = true;
      accessDeniedOwner.value = detail.authorDisplayName || detail.authorUsername || '원작자';
      accessDeniedBrand.value = detail.selectedBrandName || detail.title || '해당 브랜드';
      currentIdea.value = null;
      siteRuntime.value = null;
      return;
    }
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
  } catch (err: any) {
    if (selectedIdeaId.value !== ideaId) return;
    const status = err?.response?.status;
    if (status === 403 || status === 401) {
      accessDenied.value = true;
      accessDeniedOwner.value = '소유자';
      accessDeniedBrand.value = '비공개 프로젝트';
      currentIdea.value = null;
      siteRuntime.value = null;
      return;
    }
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
      currentIdea.value = {
        ...currentIdea.value,
        category: updated.category || currentIdea.value.category,
        selectedBrandName: updated.selectedBrandName || currentIdea.value.selectedBrandName,
        selectedSiteSlug: updated.selectedSiteSlug || currentIdea.value.selectedSiteSlug,
        nameSuggestions: updated.nameSuggestions || [],
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
        '네이밍 후보를 불러오지 못했습니다. 상단 다른 이름 8가지 추천받기 버튼으로 다시 시도해 주세요.',
        () => regenerateNames(),
      );
      return;
    }
    if (logoErr) {
      reportError(
        logoErr,
        'AI 벡터 SVG 로고 자동 생성에 실패했습니다',
        'SVG 로고 후보를 불러오지 못했습니다. 상단 다른 로고 6가지 추천받기 버튼으로 다시 시도해 주세요.',
        () => regenerateLogos(),
      );
    }
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function executeScaffoldWithRecovery(targetIdeaId: string): Promise<any> {
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
      // scaffoldSite 자체의 브랜드·로고 보완 로직으로 계속 진행
    }
  }

  try {
    return await kkumzApi.scaffoldSite(targetIdeaId);
  } catch (err) {
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
      // 복구 조회 실패 시 원본 에러 재전파
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
        // 기존 상태 유지
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
    showToast('만들고 싶은 사이트에 대한 설명을 한 줄 이상 입력해 주세요.', 'error');
    return;
  }
  if (!formStep1.value.title.trim()) {
    formStep1.value.title = formStep1.value.promptText.split(/\r?\n/)[0].trim().slice(0, 48);
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
    { label: '1. 아이디어 설정 확인 · 맞춤 기능 자동 반영', status: 'active' },
    { label: '2. AI 추천 브랜드 이름 8가지 생성', status: willGenNames ? 'pending' : 'done' },
    { label: '3. 고화질 브랜드 로고 6가지 생성', status: willGenLogos ? 'pending' : 'done' },
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
        ideas.value = (await kkumzApi.listIdeas({ mineOnly: true })) || [];
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
        ideas.value = (await kkumzApi.listIdeas({ mineOnly: true })) || [];
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
          'AI 브랜드 이름 생성 중 오류가 발생했습니다',
          '1단계 아이디어는 정상 저장되어 2단계로 이동했으나, AI 브랜드 이름 생성 요청이 실패했습니다. 다시 시도 또는 상단 다른 이름 8가지 추천받기를 눌러주세요.',
          () => regenerateNames(),
        );
        return;
      }
      if (logoErr) {
        reportError(
          logoErr,
          'AI 고화질 브랜드 로고 생성 중 오류가 발생했습니다',
          '아이디어 저장 및 브랜드 이름 8가지는 완료되었으나, 로고 생성 요청이 실패했습니다. 다시 시도 또는 상단 다른 로고 6가지 추천받기를 눌러주세요.',
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
    showToast('만들고 싶은 사이트에 대한 설명을 입력해 주세요.', 'error');
    return;
  }
  if (!formStep1.value.title.trim()) {
    formStep1.value.title = formStep1.value.promptText.split(/\r?\n/)[0].trim().slice(0, 48);
  }
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = '새 브랜드 프로젝트를 생성하고 있습니다...';
  busySteps.value = [
    { label: '1. 신규 프로젝트 생성 & 맞춤 기능 분석', status: 'active' },
    { label: '2. AI 추천 브랜드 이름 8가지 병렬 생성', status: 'pending' },
    { label: '3. 고화질 브랜드 로고 6가지 병렬 생성', status: 'pending' },
  ];
  try {
    const created = await kkumzApi.createIdea({
      title: formStep1.value.title,
      promptText: formStep1.value.promptText,
      siteType: formStep1.value.siteType,
      category: formStep1.value.category,
      tags: formStep1.value.tags,
      enabledFeatures: enabledFeatures.value,
      visibility: 'PRIVATE',
    });
    ideas.value = (await kkumzApi.listIdeas({ mineOnly: true })) || [];
    await onSelectIdea(created.id, true);
    if (busySteps.value[0]) busySteps.value[0].status = 'done';

    activeStep.value = 2;

    const { nameErr, logoErr } = await runStep2GenerationsParallel(true, true, true, 1, 2);
    if (nameErr) {
      reportError(
        nameErr,
        '새 프로젝트는 생성되었으나 AI 이름 생성에 실패했습니다',
        'AI 브랜드 이름 생성 중 서버 오류가 발생했습니다. 다시 시도를 눌러 이름을 생성해 주세요.',
        () => regenerateNames(),
      );
      return;
    }
    if (logoErr) {
      reportError(
        logoErr,
        'AI 고화질 브랜드 로고 생성 중 오류가 발생했습니다',
        '프로젝트 및 이름 8가지는 준비되었으나 로고 생성 중 서버 오류가 발생했습니다. 다시 시도를 눌러 로고를 생성해 주세요.',
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

async function runOneClickAutoComplete() {
  if (busy.value) return;
  if (!formStep1.value.promptText.trim()) {
    showToast('만들고 싶은 사이트에 대한 설명을 한 줄 이상 입력해 주세요.', 'error');
    return;
  }
  if (!formStep1.value.title.trim()) {
    formStep1.value.title = formStep1.value.promptText.split(/\r?\n/)[0].trim().slice(0, 48);
  }
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = 'AI가 아이디어를 분석해 이름·로고·기능을 자동 구성하고 완성된 사이트 화면을 만들고 있습니다...';
  busySteps.value = [
    { label: '1. 아이디어 분석 & 필수 기능 자동 구성', status: 'active' },
    { label: '2. AI 대표 브랜드 이름 & 고화질 로고 자동 생성', status: 'pending' },
    { label: '3. 내 사이트 화면 자동 제작 및 미리보기 오픈', status: 'pending' },
  ];
  try {
    let targetId = selectedIdeaId.value;
    const prevPrompt = currentIdea.value?.promptText || '';
    const promptChanged = !targetId || prevPrompt.trim() !== formStep1.value.promptText.trim();

    if (!targetId) {
      const created = await kkumzApi.createIdea({
        title: formStep1.value.title,
        promptText: formStep1.value.promptText,
        siteType: formStep1.value.siteType,
        category: formStep1.value.category,
        tags: formStep1.value.tags,
        enabledFeatures: enabledFeatures.value,
        visibility: 'PRIVATE',
      });
      ideas.value = (await kkumzApi.listIdeas({ mineOnly: true })) || [];
      await onSelectIdea(created.id, true);
      targetId = created.id;
    }

    const analyzed = await kkumzApi.analyzeIdeaIntent(targetId, {
      title: formStep1.value.title,
      promptText: formStep1.value.promptText,
      category: formStep1.value.category,
      tags: formStep1.value.tags,
      applyExpandedPrompt: false,
      applyRecommendedFeatures: enabledFeatures.value.length === 0,
    });
    currentIdea.value = analyzed;
    if (Array.isArray(analyzed?.enabledFeatures)) {
      enabledFeatures.value = [...analyzed.enabledFeatures];
    }
    if (analyzed?.siteType) {
      formStep1.value.siteType = analyzed.siteType;
    }
    if (analyzed?.aiIntentAnalysis) {
      aiIntentAnalysis.value = { ...analyzed.aiIntentAnalysis };
    }
    if (busySteps.value[0]) busySteps.value[0].status = 'done';

    const needNames = promptChanged || nameSuggestions.value.length === 0;
    const needLogos = promptChanged || logoSuggestions.value.length === 0;
    if (needNames || needLogos) {
      await runStep2GenerationsParallel(needNames, needLogos, promptChanged, 1, 1);
    } else if (busySteps.value[1]) {
      busySteps.value[1].status = 'done';
    }
    if (busySteps.value[1]) busySteps.value[1].status = 'done';
    if (busySteps.value[2]) busySteps.value[2].status = 'active';
    busyMessage.value = '선택된 브랜드 이름과 고화질 로고로 내 사이트 화면을 자동 제작하고 있습니다...';

    const rt = await executeScaffoldWithRecovery(targetId);
    if (busySteps.value[2]) busySteps.value[2].status = 'done';

    siteRuntime.value = rt;
    syncVisualFormFromRuntime(siteRuntime.value);
    currentIdea.value = await kkumzApi.getIdeaDetail(targetId);
    syncStep3FromIdea(currentIdea.value);
    ideas.value = (await kkumzApi.listIdeas({ mineOnly: true })) || [];
    activeStep.value = 4;
    showToast(
      '나만 볼 수 있는 사이트 초안이 생성되었습니다. 배포 단계에서 라이브 오픈을 실행하면 다른 사용자에게 공개됩니다.',
      'success',
    );
  } catch (err) {
    reportError(
      err,
      '1분 자동 완성 처리 중 오류가 발생했습니다',
      '사이트를 자동 구성하는 중 오류가 발생했습니다. 다시 시도를 눌러주세요.',
      () => runOneClickAutoComplete(),
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
  const targetIdeaId = selectedIdeaId.value;
  const finalBrandName = customBrandForm.value.brandName.trim() || selectedNameObj.value?.brandName || '';
  if (!finalBrandName) {
    showToast('브랜드 이름을 선택하거나 직접 입력해 주세요.', 'error');
    return;
  }
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = easyMode.value
    ? `'${finalBrandName}' 이름과 로고로 내 사이트 화면을 자동 제작하고 있습니다...`
    : '선택 및 커스텀한 브랜드 아이덴티티를 확정하고 3단계 기획서를 준비하고 있습니다...';
  busySteps.value = easyMode.value
    ? [
        { label: '1. 브랜드 이름 & 고화질 로고 확정', status: 'active' },
        { label: '2. AI 맞춤 화면 구성 자동 설계', status: 'pending' },
        { label: '3. 내 사이트 화면 자동 제작 및 미리보기 오픈', status: 'pending' },
      ]
    : [
        { label: '1. 브랜드 아이덴티티 확정', status: 'active' },
        { label: '2. 3단계 AI 기능명세서 · 화면설계 생성', status: 'pending' },
      ];
  try {
    const finalSvg = renderLogoWithBrand(
      customBrandForm.value.svgCode || selectedLogoObj.value?.svgCode || '',
      finalBrandName,
    );
    currentIdea.value = await kkumzApi.selectBrand(targetIdeaId, {
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
    busyMessage.value = `'${finalBrandName}' 브랜드에 맞춰 화면 구성을 자동 설계하고 있습니다...`;
    currentIdea.value = await kkumzApi.generateSpec(targetIdeaId, false);
    if (busySteps.value[1]) busySteps.value[1].status = 'done';
    syncStep3FromIdea(currentIdea.value);

    if (easyMode.value) {
      if (busySteps.value[2]) busySteps.value[2].status = 'active';
      busyMessage.value = `'${finalBrandName}' 사이트의 상품·배너·예약창 화면을 자동 제작하고 있습니다...`;
      const rt = await executeScaffoldWithRecovery(targetIdeaId);
      if (busySteps.value[2]) busySteps.value[2].status = 'done';
      siteRuntime.value = rt;
      syncVisualFormFromRuntime(siteRuntime.value);
      currentIdea.value = await kkumzApi.getIdeaDetail(targetIdeaId);
      ideas.value = (await kkumzApi.listIdeas({ mineOnly: true })) || [];
      activeStep.value = 4;
      showToast(
        '나만 볼 수 있는 사이트 초안이 생성되었습니다. 배포 단계에서 라이브 오픈을 실행하면 다른 사용자에게 공개됩니다.',
        'success',
      );
    } else {
      activeStep.value = 3;
      ideas.value = (await kkumzApi.listIdeas({ mineOnly: true })) || [];
    }
  } catch (err) {
    reportError(
      err,
      '브랜드 확정 및 사이트 화면 제작에 실패했습니다',
      '선택한 브랜드 이름·로고를 반영하는 중 오류가 발생했습니다.',
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
    ideas.value = (await kkumzApi.listIdeas({ mineOnly: true })) || [];
    activeStep.value = 4;
    showToast(
      '나만 볼 수 있는 사이트 초안이 생성되었습니다. 배포 단계에서 라이브 오픈을 실행하면 다른 사용자에게 공개됩니다.',
      'success',
    );
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
    visibility: 'PRIVATE',
  });
  ideas.value = (await kkumzApi.listIdeas({ mineOnly: true })) || [];
  selectedIdeaId.value = created.id;
  currentIdea.value = created;
  return created.id;
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
        // 프로비저닝 전 상태 무시
      }
    }
    showToast(
      `파일 ${files.length}건이 업로드되어 프로젝트 자산 보관함 및 AI 프롬프트에 자동 반영되었습니다.`,
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
    showToast('자료 설명 및 추출 텍스트가 AI 프롬프트에 추가되었습니다.', 'success');
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

async function handleStep2LogoUpload(file: File) {
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
    showToast('보유 로고가 업로드되어 현재 브랜드 로고로 즉시 선택되었습니다.', 'success');
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
    showToast('사진이 업로드되어 항목에 적용되었습니다. 하단 상품·매거진 상세 변경사항 즉시 반영을 눌러 저장하세요.', 'success');
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
    showToast('배너·브랜드·컬러·섹션 노출 설정이 라이브 프리뷰에 즉시 반영되었습니다.', 'success');
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
    showToast('온라인 예약·신청 폼 및 컨시어지 안내 문구가 즉시 반영되었습니다.', 'success');
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
  busyMessage.value = 'Git 리포지토리 스캐폴딩 및 사용자 전용 배포 키를 생성하고 있습니다...';
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
      res.notice || 'Git 리포지토리 생성 및 전용 개발 키 발급이 완료되었습니다.',
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
  busyMessage.value = '새 릴리즈 버전을 빌드하고 라이브 사이트에 오픈 반영하고 있습니다...';
  try {
    siteRuntime.value = await kkumzApi.deploySite(siteRuntime.value.siteSlug, {
      deployMode: deployForm.value.deployMode,
      releaseNote: deployForm.value.releaseNote,
      visibility: deployForm.value.visibility || 'PUBLIC',
      customDomain: deployForm.value.customDomain.trim() || undefined,
    });
    syncVisualFormFromRuntime(siteRuntime.value);
    if (selectedIdeaId.value) {
      currentIdea.value = await kkumzApi.getIdeaDetail(selectedIdeaId.value);
      ideas.value = (await kkumzApi.listIdeas({ mineOnly: true })) || [];
    }
    deployForm.value.releaseNote = '';
    showToast(
      siteRuntime.value.isPublished
        ? `v${siteRuntime.value.publishedVersion || siteRuntime.value.activeVersion} 라이브 사이트 배포가 완료되어 전체 공개되었습니다.`
        : `v${siteRuntime.value.activeVersion} 버전이 저장되었으며 비공개 상태로 설정되었습니다.`,
      'success',
    );
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

async function executeUnpublish() {
  if (!siteRuntime.value || busy.value) return;
  errorBanner.value = null;
  busy.value = true;
  busyMessage.value = '사이트를 비공개 초안 상태로 전환하고 있습니다...';
  try {
    siteRuntime.value = await kkumzApi.unpublishSite(siteRuntime.value.siteSlug);
    syncVisualFormFromRuntime(siteRuntime.value);
    liveOpenCelebration.value = false;
    if (selectedIdeaId.value) {
      currentIdea.value = await kkumzApi.getIdeaDetail(selectedIdeaId.value);
      ideas.value = (await kkumzApi.listIdeas({ mineOnly: true })) || [];
    }
    showToast('사이트가 비공개 초안 상태로 전환되어 외부 노출이 즉시 중단되었습니다.', 'info');
  } catch (err) {
    reportError(
      err,
      '비공개 전환에 실패했습니다',
      '사이트 공개 상태를 변경하는 중 오류가 발생했습니다.',
      () => executeUnpublish(),
    );
  } finally {
    busy.value = false;
    busyMessage.value = '';
  }
}

async function executeQuickLiveOpen() {
  if (!siteRuntime.value || busy.value) return;
  deployForm.value.deployMode = 'DYNAMIC_SDUI';
  if (!deployForm.value.releaseNote.trim()) {
    deployForm.value.releaseNote = '1초 즉시 오픈 · 최신 문구·사진·상품 반영';
  }
  await executeDeploy();
  if (!errorBanner.value) {
    liveOpenCelebration.value = true;
  }
}

async function copyLiveSiteUrl() {
  if (!siteRuntime.value?.siteSlug) return;
  const origin = typeof window !== 'undefined' ? window.location.origin : '';
  const fullUrl = `${origin}/s/${siteRuntime.value.siteSlug}`;
  await copyTextToClipboard(fullUrl, '내 사이트 인터넷 주소');
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
