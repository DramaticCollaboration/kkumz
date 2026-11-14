<template>
  <div
    class="canvas-panel"
    :class="{ 'is-busy-locked': busy }"
    :inert="busy || undefined"
  >
    <div class="panel-header">
      <div>
        <span class="step-kicker">STEP 02 · 이름 &amp; 로고 고르기</span>
        <h2>AI 추천 브랜드 이름 8가지 &amp; 고화질 브랜드 로고 6가지</h2>
        <p>
          마음에 드는 이름 카드 1개와 로고 카드 1개를 클릭한 뒤
          <strong>[{{ easyMode ? '이 이름과 로고로 내 사이트 화면 만들기 →' : '브랜드 확정 및 3단계 이동 →' }}]</strong>
          버튼만 누르세요.
        </p>
      </div>
      <div class="header-btns">
        <template v-if="!easyMode || showBrandDirectEditor">
          <select
            :value="namingTonePreference"
            class="tone-select"
            title="네이밍 톤앤매너 선택"
            @change="(e: any) => emit('update:namingTonePreference', e.target.value)"
          >
            <option value="ALL">전체 조화형 톤</option>
            <option value="PURE_KOREAN">순우리말 · 자연 감성</option>
            <option value="HANJA_CLASSIC">단아한 한자 · 전통미</option>
            <option value="MODERN_EDITORIAL">모던 에디토리얼 · 라이프스타일</option>
            <option value="GLOBAL_ENGLISH">글로벌 영문 · 미니멀</option>
          </select>
          <input
            :value="keywordHint"
            type="text"
            class="hint-input"
            placeholder="네이밍 키워드 힌트 입력"
            @input="(e: any) => emit('update:keywordHint', e.target.value)"
          />
        </template>
        <button type="button" class="secondary-btn" :disabled="busy" @click="emit('regenerate-names')">
          {{ busy ? '생성 중...' : '다른 이름 8가지 추천받기' }}
        </button>
        <button type="button" class="secondary-btn" :disabled="busy" @click="emit('regenerate-logos')">
          {{ busy ? '생성 중...' : '다른 로고 6가지 추천받기' }}
        </button>
        <input
          ref="logoInputRef"
          type="file"
          accept="image/*,.svg"
          class="hidden-file-input"
          @change="onLogoFileChange"
        />
        <button
          type="button"
          class="secondary-btn"
          :disabled="busy || assetUploading"
          @click="triggerLogoUpload"
        >
          {{ assetUploading ? '로고 업로드 중...' : '+ 내 로고 이미지 올리기' }}
        </button>
        <button
          type="button"
          class="primary-btn"
          data-testid="step2-confirm-brand-btn"
          :disabled="busy"
          @click="emit('confirm-brand')"
        >
          {{ easyMode ? '이 이름과 로고로 내 사이트 화면 만들기 →' : '브랜드 확정 및 3단계 이동 →' }}
        </button>
      </div>
    </div>

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
            <span class="kit-tag">내가 고른 브랜드 이름 &amp; 고화질 브랜드 로고 미리보기</span>
            <button
              type="button"
              class="kit-edit-toggle-btn"
              data-testid="toggle-brand-direct-editor-btn"
              @click="showBrandDirectEditor = !showBrandDirectEditor"
            >
              {{ showBrandDirectEditor ? '직접 편집 패널 접기 ▲' : '이름 · 내 사이트 인터넷 주소 · 컬러 · 로고 직접 수정 ▼' }}
            </button>
          </div>
          <div class="kit-brand-row">
            <div v-if="previewLogoSvg" class="kit-logo" v-html="previewLogoSvg"></div>
            <div class="kit-info">
              <strong>{{ previewBrandName }}</strong>
              <span>
                {{ customBrandForm.pronunciation || selectedNameObj?.pronunciation || 'Brand Identity' }}
                · 내 사이트 인터넷 주소:
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
              <input type="color" v-model="customBrandForm.colorPalette.primary" @input="emit('custom-palette-change')" />
              <i :style="{ background: customBrandForm.colorPalette.primary }"></i>
              <small>메인색: {{ customBrandForm.colorPalette.primary }}</small>
            </label>
            <label class="kit-swatch editable-swatch" title="보조 컬러 변경">
              <input type="color" v-model="customBrandForm.colorPalette.secondary" @input="emit('custom-palette-change')" />
              <i :style="{ background: customBrandForm.colorPalette.secondary }"></i>
              <small>보조색: {{ customBrandForm.colorPalette.secondary }}</small>
            </label>
            <label class="kit-swatch editable-swatch" title="포인트 컬러 변경">
              <input type="color" v-model="customBrandForm.colorPalette.accent" @input="emit('custom-palette-change')" />
              <i :style="{ background: customBrandForm.colorPalette.accent }"></i>
              <small>강조색: {{ customBrandForm.colorPalette.accent }}</small>
            </label>
            <label class="kit-swatch editable-swatch" title="배경 컬러 변경">
              <input type="color" v-model="customBrandForm.colorPalette.surface" @input="emit('custom-palette-change')" />
              <i :style="{ background: customBrandForm.colorPalette.surface }"></i>
              <small>배경색: {{ customBrandForm.colorPalette.surface }}</small>
            </label>
          </div>
          <span
            class="kit-sample-btn"
            :style="{
              background: customBrandForm.colorPalette.primary || '#4f46e5',
              borderRadius: customBrandForm.colorPalette.radius || '12px',
            }"
          >
            {{ previewBrandName }} 둘러보기 →
          </span>
        </div>
      </div>

      <div v-if="showBrandDirectEditor" class="brand-direct-editor">
        <div class="direct-editor-grid">
          <div class="mini-field">
            <label>브랜드 이름 직접 입력</label>
            <input
              v-model="customBrandForm.brandName"
              type="text"
              placeholder="예: 냇가식당, 청류민박, 공주여울스테이"
              @input="emit('custom-brand-name-input')"
            />
          </div>
          <div class="mini-field">
            <label>내 사이트 인터넷 주소</label>
            <div class="slug-input-group">
              <span class="slug-prefix">/s/</span>
              <input
                v-model="customBrandForm.siteSlug"
                type="text"
                placeholder="예: naet-ga-sik-dang"
                @input="emit('custom-slug-input')"
              />
            </div>
          </div>
          <div class="mini-field">
            <label>발음 표기 / 영문 부제목</label>
            <input
              v-model="customBrandForm.pronunciation"
              type="text"
              placeholder="예: 냇가식당 / naet-ga-sik-dang"
            />
          </div>
          <div class="mini-field">
            <label>버튼·카드 모서리 모양</label>
            <select v-model="customBrandForm.colorPalette.radius">
              <option value="6px">깔끔한 직각형 · 6px</option>
              <option value="12px">기본 라운드형 · 12px</option>
              <option value="16px">부드러운 라운드형 · 16px</option>
              <option value="24px">둥근 캡슐형 · 24px</option>
            </select>
          </div>
        </div>

        <div class="mini-field" style="margin-top: 8px">
          <label>브랜드 한 줄 소개 &amp; 슬로건</label>
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
            {{ showSvgCodeEditor ? '고화질 로고 코드 편집기 닫기 ▲' : '</> 고화질 브랜드 로고 코드 직접 편집 열기 ▼' }}
          </button>
          <span class="helper-caption">
            상단 색상 칩을 클릭해 색상을 바꾸거나, 직접 로고 코드를 수정할 수 있습니다.
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
        1. 마음에 드는 브랜드 이름을 골라보세요 · {{ generatingNames ? '8가지 생성 중...' : `${nameSuggestions.length}가지` }}
      </h3>
      <span class="helper-caption">이름 카드를 클릭하면 상단 미리보기와 아래 모든 고화질 로고의 글자가 즉시 바뀝니다.</span>
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
      <strong>아직 생성된 AI 브랜드 이름 후보가 없습니다.</strong>
      <p>아래 버튼을 눌러 AI가 추천하는 브랜드 이름 8가지를 받아보세요.</p>
      <button type="button" class="primary-btn" :disabled="busy" @click="emit('regenerate-names')">
        {{ busy ? 'AI 이름 생성 중...' : 'AI 브랜드 이름 8가지 추천받기' }}
      </button>
    </div>
    <div v-else class="name-grid">
      <div
        v-for="name in nameSuggestions"
        :key="name.id"
        class="name-card"
        :class="{ selected: selectedNameId === name.id }"
        @click="emit('select-name', name)"
      >
        <div class="name-top">
          <span class="score-pill">추천도 {{ name.score }}점</span>
          <span class="slug-ok">✓ 인터넷 주소 사용 가능</span>
        </div>
        <h4>{{ name.brandName }}</h4>
        <div class="pronun">{{ name.pronunciation }} · /s/{{ name.siteSlug }}</div>
        <p>{{ sanitizeBrandStoryText(name.brandStory) }}</p>
      </div>
    </div>

    <div class="section-title-row">
      <h3 class="sub-heading">
        2. 어울리는 고화질 브랜드 로고 &amp; 색상 테마를 골라보세요 · {{
          generatingLogos || (generatingNames && logoSuggestions.length === 0)
            ? '6가지 생성 중...'
            : `${logoSuggestions.length}가지`
        }}
      </h3>
      <span class="helper-caption">선택한 브랜드 이름이 모든 로고에 자동 적용되며, 색상 테마는 내 사이트 디자인에 바로 반영됩니다.</span>
    </div>
    <div
      v-if="generatingLogos || (generatingNames && logoSuggestions.length === 0)"
      class="logo-grid skeleton-grid"
      aria-busy="true"
    >
      <div v-for="n in 6" :key="'logo-skel-' + n" class="logo-card skeleton-card">
        <div class="svg-stage skel-stage">
          <span class="skel-stage-label">고화질 브랜드 로고 그리는 중...</span>
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
      <strong>아직 생성된 고화질 브랜드 로고 후보가 없습니다.</strong>
      <p>아래 버튼을 눌러 브랜드 색상 테마와 고화질 브랜드 로고 6가지를 만들어 보세요.</p>
      <button type="button" class="primary-btn" :disabled="busy" @click="emit('regenerate-logos')">
        {{ busy ? '고화질 로고 생성 중...' : '고화질 브랜드 로고 6가지 만들기' }}
      </button>
    </div>
    <div v-else class="logo-grid">
      <div
        v-for="logo in logoSuggestions"
        :key="logo.id"
        class="logo-card"
        :class="{ selected: selectedLogoId === logo.id }"
        @click="emit('select-logo', logo)"
      >
        <div class="svg-stage" :style="{ background: logo.colorPalette?.surface || '#f8fafc' }">
          <div class="svg-inner" v-html="renderLogoWithBrand(logo.svgCode, undefined, previewBrandName)"></div>
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

    <div class="step-footer-actions easy-step2-cta-bar">
      <button
        type="button"
        class="primary-btn large"
        data-testid="step2-bottom-confirm-btn"
        :disabled="busy"
        @click="emit('confirm-brand')"
      >
        {{
          easyMode
            ? '이 이름과 로고로 내 사이트 화면 만들기 →'
            : '브랜드 확정 및 3단계 이동 →'
        }}
      </button>
      <button
        type="button"
        class="secondary-btn large"
        :disabled="busy"
        @click="showBrandDirectEditor = !showBrandDirectEditor"
      >
        {{ showBrandDirectEditor ? '직접 이름·색상 수정 닫기 ▲' : '이름 · 인터넷 주소 · 색상 직접 바꾸기' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { sanitizeBrandStoryText, renderLogoWithBrand } from './studioFormatters';

defineProps<{
  busy: boolean;
  assetUploading: boolean;
  easyMode: boolean;
  namingTonePreference: string;
  keywordHint: string;
  customBrandForm: {
    brandName: string;
    siteSlug: string;
    pronunciation: string;
    brandStory: string;
    svgCode: string;
    colorPalette: Record<string, string>;
  };
  selectedNameObj: any | null;
  selectedLogoObj: any | null;
  previewLogoSvg: string;
  previewBrandName: string;
  activeSiteSlug: string;
  generatingNames: boolean;
  generatingLogos: boolean;
  nameSuggestions: any[];
  logoSuggestions: any[];
  selectedNameId: string;
  selectedLogoId: string;
}>();

const emit = defineEmits<{
  (e: 'update:namingTonePreference', val: string): void;
  (e: 'update:keywordHint', val: string): void;
  (e: 'regenerate-names'): void;
  (e: 'regenerate-logos'): void;
  (e: 'upload-logo', file: File): void;
  (e: 'confirm-brand'): void;
  (e: 'select-name', name: any): void;
  (e: 'select-logo', logo: any): void;
  (e: 'custom-palette-change'): void;
  (e: 'custom-brand-name-input'): void;
  (e: 'custom-slug-input'): void;
}>();

const showBrandDirectEditor = ref(false);
const showSvgCodeEditor = ref(false);
const logoInputRef = ref<HTMLInputElement | null>(null);

function triggerLogoUpload() {
  logoInputRef.value?.click();
}

function onLogoFileChange(e: Event) {
  const input = e.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = '';
  if (file) {
    emit('upload-logo', file);
  }
}
</script>

<style scoped src="./studio-view.css"></style>
