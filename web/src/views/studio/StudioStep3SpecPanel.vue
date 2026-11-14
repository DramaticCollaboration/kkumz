<template>
  <div
    class="canvas-panel"
    :class="{ 'is-busy-locked': busy }"
    :inert="busy || undefined"
  >
    <div class="panel-header">
      <div>
        <span class="step-kicker">STEP 03 · 기능명세서 &amp; 화면 설계 블루프린트</span>
        <h2>서비스 기능명세서 및 화면 설계 블루프린트</h2>
        <p>AI가 설계한 핵심 기능 항목과 화면 섹션 구성을 직접 추가·수정·순서 변경하고 내 사이트 화면 자동 제작으로 이동하세요.</p>
      </div>
      <div class="header-btns">
        <button type="button" class="secondary-btn" @click="emit('update:specEditMode', !specEditMode)">
          {{ specEditMode ? '시각적 기획서 카드 보기' : '마크다운 원문 직접 편집' }}
        </button>
        <button type="button" class="secondary-btn" :disabled="busy" @click="emit('save-spec')">
          현재 기획서·블루프린트 저장
        </button>
        <button type="button" class="primary-btn" :disabled="busy" @click="emit('provision-step4')">
          {{ busy ? '사이트 화면 제작 중...' : '내 사이트 화면 자동 제작 및 4단계 프리뷰 →' }}
        </button>
      </div>
    </div>

    <div class="spec-layout">
      <div class="spec-main">
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
              <button type="button" class="tiny-add-btn" @click="emit('add-highlight')">
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
                  @change="emit('sync-highlights-to-markdown')"
                />
                <button
                  type="button"
                  class="tiny-del-btn"
                  title="항목 삭제"
                  @click="emit('remove-highlight', idx)"
                >
                  삭제
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="markdown-editor-wrap">
          <label class="block-label">마크다운 서비스 기능명세서 원문 에디터</label>
          <textarea
            :value="editableSpecMarkdown"
            rows="12"
            class="spec-textarea"
            @input="(e: any) => emit('update:editableSpecMarkdown', e.target.value)"
            @change="emit('sync-markdown-to-highlights')"
          ></textarea>
        </div>

        <div class="blueprint-box">
          <div class="blueprint-head">
            <div>
              <strong>화면 설계 블루프린트 직접 편집 · {{ editableScreens.length }}개 핵심 화면 섹션</strong>
              <p class="blueprint-sub">각 섹션의 제목, 라우트 경로, 레이아웃 유형, 포함 기능 모듈을 직접 커스텀할 수 있습니다.</p>
            </div>
            <div class="header-btns">
              <button type="button" class="tiny-add-btn" @click="emit('add-screen')">
                + 새 화면 섹션 추가
              </button>
              <button
                type="button"
                class="secondary-btn"
                :disabled="busy"
                @click="emit('regenerate-spec')"
              >
                {{ busy ? '기획서 생성 중...' : 'AI 기획서·화면설계 재생성' }}
              </button>
            </div>
          </div>
          <div v-if="editableScreens.length === 0" class="empty-step-box" style="margin-top: 14px">
            <strong>아직 생성된 화면 설계 블루프린트가 없습니다.</strong>
            <p>우측 상단 또는 아래 버튼을 눌러 AI 서비스 기능명세서와 화면 설계 블루프린트를 생성하거나 직접 섹션을 추가해 주세요.</p>
            <div class="header-btns" style="justify-content: center; margin-top: 8px">
              <button type="button" class="primary-btn" :disabled="busy" @click="emit('regenerate-spec')">
                {{ busy ? '생성 중...' : 'AI 기능명세서 및 화면설계 생성' }}
              </button>
              <button type="button" class="secondary-btn" @click="emit('add-screen')">
                + 직접 첫 화면 섹션 추가
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
                    @click="emit('move-screen', idx, -1)"
                  >
                    ↑
                  </button>
                  <button
                    type="button"
                    class="order-mini-btn"
                    :disabled="idx === editableScreens.length - 1"
                    title="아래로 이동"
                    @click="emit('move-screen', idx, 1)"
                  >
                    ↓
                  </button>
                  <button
                    type="button"
                    class="tiny-del-btn"
                    title="섹션 삭제"
                    @click="emit('remove-screen', idx)"
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
                  placeholder="화면 섹션 제목 입력"
                />
                <input
                  v-model="scr.routePath"
                  type="text"
                  class="screen-route-input"
                  placeholder="경로 입력"
                />
              </div>

              <div class="screen-edit-row">
                <select v-model="scr.layoutType" class="screen-layout-select">
                  <option value="HERO_BANNER_GRID">메인 히어로 배너 &amp; 시그니처 그리드</option>
                  <option value="PRODUCT_CATALOG_GRID">상품 · 객실 · 메뉴 카탈로그 그리드</option>
                  <option value="GALLERY_SHOWCASE">현장 포토 갤러리 &amp; 공간 아카이브</option>
                  <option value="MAGAZINE_LIST">브랜드 매거진 &amp; 스토리 저널</option>
                  <option value="RESERVATION_FORM">온라인 예약 · 문의 · 신청 폼</option>
                  <option value="FAQ_CONCIERGE">AI 브랜드 컨시어지 &amp; 오시는 길 안내</option>
                  <option v-if="scr.layoutType && !isStandardLayoutType(scr.layoutType)" :value="scr.layoutType">
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
                  @change="(e: any) => onAddComponent(scr, e)"
                >
                  <option value="">+ 기능 모듈 추가</option>
                  <option value="HeroBanner">히어로 배너</option>
                  <option value="ProductGrid">상품/객실 진열</option>
                  <option value="PhotoGallery">포토 갤러리</option>
                  <option value="MagazineSection">매거진 저널</option>
                  <option value="BookingForm">온라인 예약/신청폼</option>
                  <option value="BrochureDownload">안내문/요금표 자료실</option>
                  <option value="AiConciergeWidget">AI 컨시어지 챗봇</option>
                  <option value="WelcomeCouponBar">웰컴 혜택 쿠폰바</option>
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
            <button type="button" class="rollback-btn" @click="emit('rollback-version', ver.versionNo)">
              v{{ ver.versionNo }} 버전으로 복원
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { formatSiteTypeLabel, isStandardLayoutType } from './studioFormatters';

const props = defineProps<{
  busy: boolean;
  specEditMode: boolean;
  currentIdea: any | null;
  activeSiteSlug: string;
  previewBrandName: string;
  editableHighlights: string[];
  editableSpecMarkdown: string;
  editableScreens: any[];
  ideaVersions: any[];
  layoutLabels: Record<string, string>;
  componentLabels: Record<string, string>;
}>();

const emit = defineEmits<{
  (e: 'update:specEditMode', val: boolean): void;
  (e: 'update:editableSpecMarkdown', val: string): void;
  (e: 'save-spec'): void;
  (e: 'provision-step4'): void;
  (e: 'add-highlight'): void;
  (e: 'remove-highlight', idx: number): void;
  (e: 'sync-highlights-to-markdown'): void;
  (e: 'sync-markdown-to-highlights'): void;
  (e: 'add-screen'): void;
  (e: 'remove-screen', idx: number): void;
  (e: 'move-screen', idx: number, delta: number): void;
  (e: 'regenerate-spec'): void;
  (e: 'rollback-version', versionNo: number): void;
}>();

function formatLayoutTypeLabel(code: string) {
  if (!code) return '반응형 표준 섹션';
  return props.layoutLabels[code] || code;
}

function formatComponentLabel(code: string) {
  if (!code) return '기능 모듈';
  return props.componentLabels[code] || code;
}

function onAddComponent(scr: any, event: Event) {
  const select = event.target as HTMLSelectElement;
  const compCode = select.value;
  select.value = '';
  if (!compCode) return;
  if (!Array.isArray(scr.components)) {
    scr.components = [];
  }
  if (!scr.components.includes(compCode)) {
    scr.components.push(compCode);
  }
}
</script>

<style scoped src="./studio-view.css"></style>
