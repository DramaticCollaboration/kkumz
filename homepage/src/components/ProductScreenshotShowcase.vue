<template>
  <div v-if="guide && guide.screenshots.length" class="screen-showcase-card">
    <div class="screen-showcase-head">
      <div class="screen-showcase-title-group">
        <span class="screen-live-pill">
          <i class="fas fa-desktop"></i> 실제 작동 화면
        </span>
        <h3 class="screen-showcase-title">{{ activeSlide.title }}</h3>
        <p class="screen-showcase-summary">{{ activeSlide.summary }}</p>
      </div>
    </div>

    <div class="screen-tabs" role="tablist">
      <button
        v-for="(slide, idx) in guide.screenshots"
        :key="slide.idx"
        type="button"
        role="tab"
        class="screen-tab-btn"
        :class="{ active: activeIdx === idx }"
        :aria-selected="activeIdx === idx"
        @click="activeIdx = idx"
      >
        <span class="screen-tab-num">0{{ slide.idx }}</span>
        <span class="screen-tab-label">{{ slide.tabLabel || slide.title }}</span>
      </button>
    </div>

    <div class="screen-browser-frame">
      <div class="screen-browser-bar">
        <div class="screen-browser-dots">
          <span style="background: #ef4444"></span>
          <span style="background: #f59e0b"></span>
          <span style="background: #22c55e"></span>
        </div>
        <div class="screen-browser-address mono">
          <i class="fas fa-lock" style="color: #34d399; margin-right: 6px"></i>
          <span>{{ primaryDomain }}{{ activeSlide.route }}</span>
        </div>
        <div class="screen-browser-actions">
          <a
            v-if="activeDemoDeepLink"
            :href="activeDemoDeepLink"
            target="_blank"
            rel="noopener noreferrer"
            class="screen-open-live-link"
          >
            이 화면 라이브 데모로 직접 조작하기 <i class="fas fa-arrow-up-right-from-square"></i>
          </a>
        </div>
      </div>

      <div class="screen-viewport">
        <img
          :src="activeSlide.rawUrl"
          :alt="`${productName} - ${activeSlide.title}`"
          class="screen-image"
          loading="eager"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import type { ProductVisualGuide } from '@/data/productVisuals';

const props = defineProps<{
  productName: string;
  primaryDomain: string;
  primaryDemoUrl?: string;
  guide?: ProductVisualGuide;
}>();

const activeIdx = ref<number>(0);

watch(
  () => props.guide?.productId,
  () => {
    activeIdx.value = 0;
  }
);

const activeSlide = computed(() => {
  const slides = props.guide?.screenshots ?? [];
  return (
    slides[activeIdx.value] ??
    slides[0] ?? {
      idx: 1,
      tabLabel: '',
      title: '',
      route: '/',
      summary: '',
      rawUrl: '',
      annotatedUrl: '',
    }
  );
});

const activeDemoDeepLink = computed<string | undefined>(() => {
  if (!props.primaryDemoUrl) {
    return undefined;
  }
  const base = props.primaryDemoUrl.replace(/\/$/, '');
  const routePath = activeSlide.value.route?.startsWith('/')
    ? activeSlide.value.route
    : `/${activeSlide.value.route ?? ''}`;
  return `${base}${routePath}`;
});
</script>
