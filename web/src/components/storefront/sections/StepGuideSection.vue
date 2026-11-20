<template>
  <section id="section-step-guide" class="space-y-8 scroll-mt-24" data-testid="sdui-step-guide">
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-3 pb-5 sf-section-divider">
      <div class="space-y-1.5">
        <span
          class="sf-kicker text-xs font-bold uppercase tracking-[0.16em]"
          :style="{ color: theme.isDark ? theme.accentColor : theme.secondaryColor }"
        >
          {{ copyOverrides.stepKicker || 'STEP-BY-STEP RITUAL & GUIDE' }}
        </span>
        <h2 class="sf-heading text-2xl md:text-3xl font-bold" :style="{ color: 'var(--sf-ink)' }">
          {{ copyOverrides.stepTitle || `${brandName} 단계별 이용 가이드` }}
        </h2>
      </div>
      <p class="text-xs sm:text-sm" :style="{ color: 'var(--sf-ink-muted)' }">
        {{ copyOverrides.stepSubtitle || '가장 깊이 있는 브랜드 경험을 위한 추천 순서입니다.' }}
      </p>
    </div>

    <div class="grid gap-6" :class="gridClass">
      <div
        v-for="(st, sIdx) in stepGuide"
        :key="sIdx"
        class="sf-card p-6 space-y-4 flex flex-col justify-between relative overflow-hidden"
      >
        <div class="space-y-3">
          <div class="flex items-center justify-between gap-2">
            <span
              class="sf-mono px-3 py-1 text-[11px] font-bold text-white"
              :style="{ backgroundColor: theme.primaryColor, borderRadius: theme.radius }"
            >
              {{ st.step || `0${sIdx + 1}` }}
            </span>
            <span
              v-if="st.time"
              class="text-[11px] font-semibold"
              :style="{ color: 'var(--sf-ink-muted)' }"
            >
              {{ st.time }}
            </span>
          </div>

          <h3 class="sf-heading text-lg font-bold leading-snug" :style="{ color: 'var(--sf-ink)' }">
            {{ st.title }}
          </h3>

          <p class="text-xs sm:text-sm leading-relaxed" :style="{ color: 'var(--sf-ink-soft)' }">
            {{ st.desc || st.description }}
          </p>
        </div>

        <div
          class="h-[2px] w-10 mt-2 opacity-60"
          :style="{ backgroundColor: theme.accentColor }"
        ></div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import {
  resolveGridColumnsClass,
  type ResolvedThemeSpec,
  type StorefrontCopyOverrides,
} from '../storefrontTypes';

const props = defineProps<{
  brandName: string;
  stepGuide: Array<Record<string, any>>;
  copyOverrides: StorefrontCopyOverrides;
  theme: ResolvedThemeSpec;
}>();

const gridClass = computed(() => resolveGridColumnsClass(props.stepGuide.length, 4));
</script>
