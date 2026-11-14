<template>
  <div
    class="feature-catalog-panel"
    data-testid="feature-catalog-panel"
  >
    <div class="feature-catalog-head">
      <div>
        <div class="feature-head-title-row">
          <span class="step-kicker">내 사이트에 추가할 수 있는 편리한 기능 모음 · 총 {{ featureCatalog.length || 52 }}가지</span>
          <span class="feature-stat-pill">전체 {{ featureCatalog.length }}가지</span>
          <span class="feature-stat-pill recommended">AI 추천 {{ recommendedFeatureCodes.length }}가지</span>
          <span class="feature-stat-pill active">현재 선택 {{ enabledFeatures.length }}가지</span>
        </div>
        <h3>내 사이트에 넣고 싶은 세부 기능 골라 담기</h3>
        <p>
          검색창에 원하는 기능을 검색하거나 클릭해서 자유롭게 켜고 끌 수 있습니다.
        </p>
      </div>
      <div class="feature-head-actions">
        <button
          type="button"
          class="secondary-btn"
          :disabled="busy"
          @click="emit('select-recommended')"
        >
          AI 추천 기능 {{ recommendedFeatureCodes.length }}가지 한 번에 켜기
        </button>
        <button
          type="button"
          class="secondary-btn"
          :disabled="busy || !selectedIdeaId"
          @click="emit('save-features')"
        >
          선택한 기능 저장 · {{ enabledFeatures.length }}가지
        </button>
      </div>
    </div>

    <div class="feature-search-bar">
      <div class="feature-search-input-wrap">
        <span class="search-icon-label">기능 검색</span>
        <input
          :value="featureSearchQuery"
          type="text"
          data-testid="feature-search-input"
          class="feature-search-input"
          placeholder="원하는 기능 검색: 요금표, 객실, 예약폼, 공간 소개, 자주 묻는 질문, 오시는 길, 쿠폰, 후기"
          @input="(e: any) => emit('update:featureSearchQuery', e.target.value)"
        />
        <button
          v-if="featureSearchQuery"
          type="button"
          class="clear-search-btn"
          @click="emit('update:featureSearchQuery', '')"
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
          @click="emit('update:activeEngineFilter', eng.code)"
        >
          {{ eng.label }}
        </button>
      </div>
    </div>

    <div class="feature-category-tabs">
      <button
        type="button"
        class="feat-cat-tab"
        :class="{ active: activeFeatureCategoryTab === 'ALL' }"
        @click="emit('update:activeFeatureCategoryTab', 'ALL')"
      >
        전체 기능 · {{ featureCatalog.length }}
      </button>
      <button
        type="button"
        class="feat-cat-tab recommended"
        :class="{ active: activeFeatureCategoryTab === 'RECOMMENDED' }"
        @click="emit('update:activeFeatureCategoryTab', 'RECOMMENDED')"
      >
        AI 맞춤 추천 · {{ recommendedFeatureCodes.length }}
      </button>
      <button
        type="button"
        class="feat-cat-tab selected"
        :class="{ active: activeFeatureCategoryTab === 'SELECTED' }"
        @click="emit('update:activeFeatureCategoryTab', 'SELECTED')"
      >
        선택됨 · {{ enabledFeatures.length }}
      </button>
      <button
        v-for="cat in featureCategories"
        :key="cat.code"
        type="button"
        class="feat-cat-tab"
        :class="{ active: activeFeatureCategoryTab === cat.code }"
        @click="emit('update:activeFeatureCategoryTab', cat.code)"
      >
        {{ cat.label }} · {{ countFeaturesByCategory(cat.code) }}
      </button>
    </div>

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
        @click="emit('toggle-feature', feat.featureCode)"
      >
        <div class="feat-card-top">
          <div class="feat-badges">
            <span class="feat-engine-badge" :class="(feat.engineCode || 'SYNC_CMS').toLowerCase()">
              {{ formatEngineBadge(feat.engineCode) }}
            </span>
            <span class="feat-cat-badge">{{ feat.badgeLabel || feat.categoryName }}</span>
            <span v-if="isFeatureRecommended(feat.featureCode)" class="feat-ai-rec-badge">
              AI 추천
            </span>
          </div>
          <span class="feat-toggle-pill" :class="{ on: isFeatureEnabled(feat.featureCode) }">
            {{ isFeatureEnabled(feat.featureCode) ? '✓ 포함됨' : '+ 담기' }}
          </span>
        </div>

        <h4 class="feat-card-title">{{ feat.title }}</h4>
        <p class="feat-card-sub">{{ feat.subtitle }}</p>
        <p class="feat-card-desc">{{ feat.description }}</p>

        <div v-if="aiFeatureReasons[feat.featureCode]" class="feat-ai-reason-box">
          <strong>추천 이유:</strong> {{ aiFeatureReasons[feat.featureCode] }}
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
      <strong>찾으시는 기능 카드가 없습니다.</strong>
      <p>검색어를 지우거나 전체 기능 탭을 눌러 모든 기능을 확인해 보세요.</p>
      <button
        type="button"
        class="secondary-btn"
        @click="resetFilters"
      >
        전체 기능 목록 보기
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { engineFilterOptions, formatEngineBadge } from './studioFormatters';

const props = defineProps<{
  featureCatalog: any[];
  featureCategories: Array<{ code: string; label: string; desc?: string }>;
  filteredFeatureCatalog: any[];
  recommendedFeatureCodes: string[];
  enabledFeatures: string[];
  aiFeatureReasons: Record<string, string>;
  featureSearchQuery: string;
  activeFeatureCategoryTab: string;
  activeEngineFilter: string;
  busy: boolean;
  selectedIdeaId: string;
}>();

const emit = defineEmits<{
  (e: 'update:featureSearchQuery', val: string): void;
  (e: 'update:activeFeatureCategoryTab', val: string): void;
  (e: 'update:activeEngineFilter', val: string): void;
  (e: 'select-recommended'): void;
  (e: 'save-features'): void;
  (e: 'toggle-feature', code: string): void;
}>();

function countFeaturesByCategory(catCode: string): number {
  return props.featureCatalog.filter((f: any) => f.categoryCode === catCode).length;
}

function isFeatureEnabled(code: string): boolean {
  return props.enabledFeatures.includes(code);
}

function isFeatureRecommended(code: string): boolean {
  return props.recommendedFeatureCodes.includes(code);
}

function resetFilters() {
  emit('update:featureSearchQuery', '');
  emit('update:activeFeatureCategoryTab', 'ALL');
  emit('update:activeEngineFilter', 'ALL');
}
</script>

<style scoped src="./studio-view.css"></style>
