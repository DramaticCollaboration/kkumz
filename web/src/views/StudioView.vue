<template>
  <div class="studio-shell">
    <!-- 슬림 스튜디오 상단 헤더 (복잡한 다중 탭 제거) -->
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
          <div v-if="currentIdea.selectedLogoSvg" class="mini-logo" v-html="currentIdea.selectedLogoSvg"></div>
          <div class="mini-meta">
            <strong>{{ currentIdea.selectedBrandName || currentIdea.title }}</strong>
            <span>{{ formatSiteTypeLabel(currentIdea.siteType) }}</span>
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
              <strong>{{ step.title }}</strong>
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

        <!-- 1단계: 아이디어 정의 -->
        <div v-if="activeStep === 1" class="canvas-panel">
          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 01 · IDEA & SITE ARCHITECTURE</span>
              <h2>아이디어 정의 및 사이트 유형 선택</h2>
              <p>만들고 싶은 브랜드의 핵심 아이디어와 웹사이트 성격을 선택해 주세요.</p>
            </div>
            <button type="button" class="primary-btn" :disabled="busy" @click="goToStep(2)">
              다음: 브랜드 네이밍 & 로고 →
            </button>
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
              placeholder="예: 제주 서귀포 첫물 유기농 차광 말차 & 도예가 수제 다기 브랜드 스토어"
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

          <div class="panel-actions">
            <button type="button" class="primary-btn" :disabled="busy" @click="createNewIdeaFromStep1">
              {{ busy ? '아이디어 생성 중...' : '신규 아이디어로 저장하고 AI 브랜딩 시작' }}
            </button>
          </div>
        </div>

        <!-- 2단계: 네이밍 & 로고 -->
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
                placeholder="네이밍 키워드 힌트 (선택)"
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

          <h3 class="sub-heading">AI 추천 브랜드 네이밍 후보 (8종)</h3>
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
                <span class="slug-ok">주소 사용 가능</span>
              </div>
              <h4>{{ name.brandName }}</h4>
              <div class="pronun">{{ name.pronunciation }} · /s/{{ name.siteSlug }}</div>
              <p>{{ name.brandStory }}</p>
            </div>
          </div>

          <h3 class="sub-heading">AI 벡터 SVG 로고 & 브랜드 컬러 팔레트 후보 (6종)</h3>
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
                <strong>{{ logo.styleName }}</strong>
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

        <!-- 3단계: 기능명세서 & 화면설계 -->
        <div v-else-if="activeStep === 3" class="canvas-panel">
          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 03 · FUNCTIONAL SPEC & SCREEN BLUEPRINT</span>
              <h2>서비스 기능명세서 및 화면 설계 블루프린트</h2>
              <p>AI가 작성한 마크다운 기획서와 화면 구성을 편집하고 버전 스냅샷으로 안전하게 관리합니다.</p>
            </div>
            <div class="header-btns">
              <button type="button" class="secondary-btn" :disabled="busy" @click="saveSpecVersion">
                현재 기획서 스냅샷 저장
              </button>
              <button type="button" class="primary-btn" :disabled="busy" @click="provisionAndGoStep4">
                사이트 프로비저닝 및 4단계 프리뷰 →
              </button>
            </div>
          </div>

          <div class="spec-layout">
            <div class="spec-main">
              <label class="block-label">마크다운 서비스 기능명세서 에디터</label>
              <textarea v-model="editableSpecMarkdown" rows="13" class="spec-textarea"></textarea>

              <div class="blueprint-box">
                <div class="blueprint-head">
                  <strong>화면 설계 블루프린트 ({{ screenSpecs.length }}개 화면 섹션)</strong>
                </div>
                <div class="screen-list">
                  <div v-for="scr in screenSpecs" :key="scr.id || scr.screenKey" class="screen-item">
                    <div class="screen-item-top">
                      <strong>{{ scr.screenTitle || scr.title }}</strong>
                      <span class="layout-tag">{{ scr.layoutType || scr.layout }}</span>
                    </div>
                    <div class="comp-tags">
                      <span v-for="c in scr.components || []" :key="c" class="comp-chip">{{ c }}</span>
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

        <!-- 4단계: 실시간 프리뷰 편집 -->
        <div v-else-if="activeStep === 4" class="canvas-panel">
          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 04 · CONVERSATIONAL LIVE PREVIEW</span>
              <h2>대화형 디자인·문구 수정 및 실시간 프리뷰</h2>
              <p>좌측 채팅창에 원하는 테마나 배너 문구를 입력하면 우측 프리뷰 화면에 즉시 반영됩니다.</p>
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
                5단계: WebIDE 코드 편집
              </button>
              <button type="button" class="primary-btn" @click="goToStep(6)">
                6단계: 빌드 & 배포 →
              </button>
            </div>
          </div>

          <div class="preview-split" v-if="siteRuntime">
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

              <label class="block-label">자연어 디자인·레이아웃 수정 지시</label>
              <textarea
                v-model="chatInstruction"
                rows="3"
                placeholder="예: 다크 인디고 테마로 컬러를 바꾸고 히어로 배너 문구를 더 강렬하게 변경해줘"
              ></textarea>
              <button type="button" class="primary-btn full" :disabled="busy" @click="applyChatRefine">
                {{ busy ? '실시간 반영 중...' : '대화형 수정 즉시 반영 (Hot-Reload)' }}
              </button>

              <div class="preset-group">
                <span class="preset-title">원클릭 컬러 테마 프리셋</span>
                <button type="button" @click="applyQuickTheme('보태니컬 포레스트 그린 테마로 변경해줘')">
                  🌿 보태니컬 포레스트 그린 테마
                </button>
                <button type="button" @click="applyQuickTheme('다크 인디고 테크 테마로 변경해줘')">
                  🌌 다크 인디고 테크 테마
                </button>
                <button type="button" @click="applyQuickTheme('웜 앰버 크래프트 테마로 변경해줘')">
                  ☕ 웜 앰버 크래프트 테마
                </button>
              </div>
            </div>

            <div class="preview-stage">
              <div class="preview-frame" :class="viewportMode.toLowerCase()">
                <div
                  class="frame-hero"
                  :style="{
                    background: siteRuntime.designTokens?.primary || '#14532d',
                    color: '#ffffff',
                  }"
                >
                  <div class="frame-top">
                    <div class="frame-logo" v-html="siteRuntime.logoSvg"></div>
                    <span class="frame-badge">{{ siteRuntime.layoutSchema?.customBadge || '공식 브랜드 스토어' }}</span>
                  </div>
                  <h3>{{ siteRuntime.layoutSchema?.heroTitle || siteRuntime.siteName }}</h3>
                  <p>{{ siteRuntime.layoutSchema?.heroSubtitle || siteRuntime.brandSlogan }}</p>
                </div>

                <div v-if="siteRuntime.commerce?.products?.length" class="frame-section">
                  <h4>시그니처 상품 카탈로그 ({{ siteRuntime.commerce.products.length }}종)</h4>
                  <div class="mini-cards">
                    <div v-for="p in siteRuntime.commerce.products.slice(0, 3)" :key="p.id" class="mini-card">
                      <img :src="p.pic" :alt="p.name" />
                      <div class="mini-card-body">
                        <strong>{{ p.name }}</strong>
                        <span>{{ Number(p.promotionPrice || p.price).toLocaleString() }}원</span>
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="siteRuntime.cms?.articles?.length" class="frame-section">
                  <h4>브랜드 매거진 & 커뮤니티 소식 ({{ siteRuntime.cms.articles.length }}건)</h4>
                  <div class="mini-articles">
                    <div v-for="a in siteRuntime.cms.articles.slice(0, 2)" :key="a.id" class="mini-article">
                      <strong>{{ a.title }}</strong>
                      <p>{{ a.description }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 5단계: WebIDE 코드 편집 -->
        <div v-else-if="activeStep === 5" class="canvas-panel">
          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 05 · WEBIDE CODE STUDIO & AI SELF-HEALING</span>
              <h2>고급 모드 WebIDE 코드 편집기 & AI 빌드 오류 자동 패치</h2>
              <p>Vue 3 컴포넌트와 테마 JSON 코드를 직접 커스텀하고, 빌드 오류 발생 시 AI로 즉시 복구합니다.</p>
            </div>
            <div class="header-btns">
              <button type="button" class="secondary-btn" :disabled="busy" @click="saveAndBuildWebIde(false)">
                코드 저장 & 번들 빌드
              </button>
              <button type="button" class="warn-btn" :disabled="busy" @click="saveAndBuildWebIde(true)">
                문법 오류 시뮬레이션
              </button>
              <button
                v-if="webIdeData?.buildStatus === 'FAILED'"
                type="button"
                class="heal-btn"
                :disabled="busy"
                @click="runAiFixBuild"
              >
                ✨ AI 빌드 오류 자동 패치
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
                📄 {{ file.path }}
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
                  {{ webIdeData.buildStatus === 'FAILED' ? '빌드 실패' : '빌드 정상' }}
                </span>
              </div>
              <pre class="console-log">{{ webIdeData.buildLog }}</pre>
            </div>
          </div>
        </div>

        <!-- 6단계: 빌드 & 배포 -->
        <div v-else-if="activeStep === 6" class="canvas-panel">
          <div class="panel-header">
            <div>
              <span class="step-kicker">STEP 06 · HYBRID BUILD & LIVE RELEASE</span>
              <h2>빌드 & 라이브 사이트 무중단 배포</h2>
              <p>배포 방식을 선택해 라이브 주소로 즉시 발행하고, 쇼케이스 피드에서 팀원을 모집하세요.</p>
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
                  <strong>즉시 디자인 배포 (무중단 스키마)</strong>
                  <p>별도 빌드 대기 없이 컬러 토큰과 레이아웃 구성을 1초 만에 라이브 사이트에 즉시 반영합니다.</p>
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
                <label>릴리즈 노트</label>
                <input
                  v-model="deployForm.releaseNote"
                  type="text"
                  placeholder="예: 2026 시즌 메인 배너 카피 및 기프트 패키지 카탈로그 업데이트"
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
  { no: 1, title: '1. 아이디어 정의', desc: '사이트 유형 & 프롬프트' },
  { no: 2, title: '2. 네이밍 & 로고', desc: '브랜드명 8종 & SVG 로고' },
  { no: 3, title: '3. 기능명세서 & 화면설계', desc: '기획서 편집 & 버전 관리' },
  { no: 4, title: '4. 실시간 프리뷰 편집', desc: '대화형 테마·문구 수정' },
  { no: 5, title: '5. WebIDE 코드 편집', desc: 'Vue 코드 & AI 오류 복구' },
  { no: 6, title: '6. 빌드 & 배포', desc: '라이브 발행 & 버전 롤백' },
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

const ideas = ref<any[]>([]);
const selectedIdeaId = ref('');
const currentIdea = ref<any | null>(null);
const activeStep = ref(1);
const busy = ref(false);
const toastMessage = ref('');

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
const activeSiteSlug = computed(
  () => siteRuntime.value?.siteSlug || currentIdea.value?.site?.siteSlug || currentIdea.value?.selectedSiteSlug || '',
);
const activeWebIdeFile = computed(() => webIdeFiles.value[activeFileIndex.value] || null);

function showToast(msg: string) {
  toastMessage.value = msg;
  setTimeout(() => {
    if (toastMessage.value === msg) toastMessage.value = '';
  }, 3200);
}

async function initStudio() {
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
    } catch {
      siteRuntime.value = null;
    }
  }
}

async function goToStep(stepNo: number) {
  activeStep.value = stepNo;
  if (!selectedIdeaId.value) return;

  if (stepNo === 2) {
    if (nameSuggestions.value.length === 0) {
      await regenerateNames();
    }
    if (logoSuggestions.value.length === 0) {
      await regenerateLogos();
    }
  } else if (stepNo === 4 || stepNo === 6) {
    if (!siteRuntime.value) {
      siteRuntime.value = await kkumzApi.scaffoldSite(selectedIdeaId.value);
    }
  } else if (stepNo === 5) {
    if (!siteRuntime.value) {
      siteRuntime.value = await kkumzApi.scaffoldSite(selectedIdeaId.value);
    }
    if (activeSiteSlug.value) {
      await loadWebIde(activeSiteSlug.value);
    }
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
    showToast('새 아이디어가 생성되었습니다. 브랜드 네이밍과 로고를 선택해 보세요!');
  } finally {
    busy.value = false;
  }
}

async function regenerateNames() {
  if (!selectedIdeaId.value) return;
  busy.value = true;
  try {
    currentIdea.value = await kkumzApi.generateNames(selectedIdeaId.value, {
      keywordHint: keywordHint.value || '스튜디오',
    });
    selectedNameId.value = currentIdea.value.nameSuggestions?.[0]?.id || '';
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
      stylePreference: 'MODERN',
    });
    selectedLogoId.value = currentIdea.value.logoSuggestions?.[0]?.id || '';
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

async function loadWebIde(slug: string) {
  const data = await kkumzApi.getWebIdeFiles(slug);
  webIdeData.value = data;
  webIdeFiles.value = (data.files || []).map((f: any) => ({ ...f }));
  activeFileIndex.value = 0;
}

async function saveAndBuildWebIde(simulateSyntaxError: boolean) {
  if (!activeSiteSlug.value) return;
  busy.value = true;
  try {
    const res = await kkumzApi.updateWebIdeFiles(activeSiteSlug.value, {
      files: webIdeFiles.value,
      simulateSyntaxError,
    });
    webIdeData.value = res;
    webIdeFiles.value = (res.files || []).map((f: any) => ({ ...f }));
    showToast(simulateSyntaxError ? '문법 오류가 감지되었습니다. AI 자동 패치를 실행해 보세요.' : '번들 빌드가 성공적으로 완료되었습니다.');
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
    showToast('AI가 템플릿 문법 오류를 자동으로 수정하고 빌드를 완료했습니다!');
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
}
.step-labels strong {
  font-size: 13px;
  color: #0f172a;
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
}
.logo-card.selected {
  border: 2px solid #4f46e5;
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
  gap: 14px;
}
.spec-textarea {
  width: 100%;
  padding: 14px;
  border-radius: 12px;
  border: 1px solid #cbd5e1;
  font-family: monospace;
  font-size: 13px;
  line-height: 1.6;
}
.blueprint-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 16px;
}
.screen-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
  margin-top: 10px;
}
.screen-item {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px;
}
.screen-item-top {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 8px;
}
.layout-tag {
  font-size: 10px;
  background: #eef2ff;
  color: #4f46e5;
  padding: 2px 6px;
  border-radius: 6px;
}
.comp-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.comp-chip {
  font-size: 11px;
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 6px;
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
  grid-template-columns: 320px 1fr;
  gap: 20px;
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
.chat-panel textarea {
  padding: 10px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
}
.preset-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 6px;
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
  background: #f1f5f9;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  justify-content: center;
}
.preview-frame {
  width: 100%;
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.08);
}
.preview-frame.tablet {
  max-width: 720px;
}
.preview-frame.mobile {
  max-width: 390px;
}
.frame-hero {
  padding: 24px;
}
.frame-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.frame-logo :deep(svg) {
  height: 30px;
  width: auto;
  background: #fff;
  padding: 3px 8px;
  border-radius: 6px;
}
.frame-badge {
  font-size: 11px;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 10px;
  border-radius: 999px;
}
.frame-section {
  padding: 20px 24px;
}
.frame-section h4 {
  margin: 0 0 12px;
  font-size: 15px;
}
.mini-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.mini-card {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
}
.mini-card img {
  width: 100%;
  height: 100px;
  object-fit: cover;
}
.mini-card-body {
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
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
