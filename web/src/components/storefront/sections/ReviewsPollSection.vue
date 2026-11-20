<template>
  <section
    id="section-reviews"
    class="grid grid-cols-1 lg:grid-cols-12 gap-8 scroll-mt-24"
    data-testid="sdui-reviews-poll"
  >
    <div
      v-if="reviews.length"
      :class="pollWidget ? 'lg:col-span-8' : 'lg:col-span-12'"
      class="space-y-6"
    >
      <div class="pb-4 sf-section-divider space-y-1.5">
        <span
          class="sf-kicker text-xs font-bold uppercase tracking-[0.16em]"
          :style="{ color: theme.isDark ? theme.accentColor : theme.secondaryColor }"
        >
          {{ copyOverrides.reviewKicker || 'VERIFIED CUSTOMER REVIEWS' }}
        </span>
        <h2 class="sf-heading text-2xl md:text-3xl font-bold" :style="{ color: 'var(--sf-ink)' }">
          {{ copyOverrides.reviewTitle || `실구매자 · 방문객 후기 (${reviews.length}건)` }}
        </h2>
      </div>

      <div class="grid gap-5" :class="reviewsGridClass">
        <article
          v-for="(rv, rvIdx) in reviews"
          :key="rv.id || rvIdx"
          class="sf-card p-6 flex flex-col justify-between space-y-4"
        >
          <div class="space-y-3">
            <div class="flex items-center justify-between gap-2">
              <div class="flex items-center gap-1 text-xs font-extrabold" :style="{ color: theme.accentColor }">
                <span>{{ Number(rv.rating || 5).toFixed(1) }}</span>
                <span class="opacity-65">/ 5.0</span>
              </div>
              <span
                v-if="rv.tag"
                class="px-2.5 py-0.5 text-[10px] font-bold border truncate max-w-[11rem]"
                :style="{
                  backgroundColor: 'var(--sf-card-alt)',
                  borderColor: 'var(--sf-border)',
                  color: 'var(--sf-ink-soft)',
                  borderRadius: '999px',
                }"
              >
                {{ rv.tag }}
              </span>
            </div>

            <div
              v-if="rv.productName"
              class="text-[11px] font-semibold truncate"
              :style="{ color: theme.isDark ? theme.accentColor : theme.secondaryColor }"
            >
              {{ rv.productName }}
            </div>

            <p class="text-xs sm:text-sm leading-relaxed" :style="{ color: 'var(--sf-ink)' }">
              "{{ rv.comment || rv.content }}"
            </p>
          </div>

          <div
            class="pt-3 border-t flex items-center justify-between text-[11px] font-semibold"
            :style="{ borderColor: 'var(--sf-border)', color: 'var(--sf-ink-muted)' }"
          >
            <span>{{ rv.author }}</span>
            <span v-if="rv.date">{{ rv.date }}</span>
          </div>
        </article>
      </div>
    </div>

    <div
      v-if="pollWidget"
      :class="reviews.length ? 'lg:col-span-4' : 'lg:col-span-12'"
      class="p-7 flex flex-col justify-between space-y-5 text-white"
      :style="{
        backgroundColor: theme.isDark ? '#18181f' : theme.primaryColor,
        border: theme.isDark ? '1px solid rgba(255,255,255,0.12)' : 'none',
        borderRadius: 'var(--sf-radius-lg)',
      }"
    >
      <div class="space-y-2.5">
        <span class="sf-mono inline-block px-2.5 py-1 rounded-full bg-white/15 text-[11px] font-bold tracking-wider">
          LIVE VISITOR POLL
        </span>
        <h3 class="sf-heading text-lg font-bold leading-snug text-white">
          {{ pollWidget.question }}
        </h3>
      </div>

      <div class="space-y-2.5">
        <button
          v-for="(opt, oIdx) in pollOptions"
          :key="oIdx"
          type="button"
          class="w-full p-3.5 border text-left transition flex items-center justify-between gap-2 text-xs font-bold"
          :style="pollOptionStyle(oIdx)"
          @click="votedIdx = oIdx"
        >
          <span>{{ opt.label }}</span>
          <span class="sf-mono px-2 py-0.5 rounded-full bg-black/25 text-[11px] shrink-0">
            {{ Number(opt.votes || 0) + (votedIdx === oIdx ? 1 : 0) }}표
          </span>
        </button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import {
  resolveGridColumnsClass,
  type ResolvedThemeSpec,
  type StorefrontCopyOverrides,
} from '../storefrontTypes';

const props = defineProps<{
  reviews: Array<Record<string, any>>;
  pollWidget?: Record<string, any> | null;
  copyOverrides: StorefrontCopyOverrides;
  theme: ResolvedThemeSpec;
}>();

const votedIdx = ref<number | null>(null);

const pollOptions = computed<Array<Record<string, any>>>(() =>
  Array.isArray(props.pollWidget?.options) ? props.pollWidget.options : [],
);

const reviewsGridClass = computed(() => {
  const maxCols: 2 | 3 = props.pollWidget ? 2 : 3;
  return resolveGridColumnsClass(props.reviews.length, maxCols);
});

function pollOptionStyle(idx: number) {
  const active = votedIdx.value === idx;
  return {
    backgroundColor: active ? '#ffffff' : 'rgba(255,255,255,0.1)',
    borderColor: active ? '#ffffff' : 'rgba(255,255,255,0.2)',
    color: active ? props.theme.primaryColor : '#ffffff',
    borderRadius: props.theme.radius,
  };
}
</script>
