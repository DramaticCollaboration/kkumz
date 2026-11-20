<template>
  <section
    class="w-full relative overflow-hidden border-b"
    :style="{ backgroundColor: 'var(--sf-bg)', borderColor: 'var(--sf-border)' }"
    data-hero-archetype="EDITORIAL_STORY"
  >
    <div class="max-w-7xl mx-auto px-6 md:px-10 py-14 md:py-22">
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-14 items-center">
        <div class="lg:col-span-6 space-y-6">
          <div class="flex items-center gap-3">
            <span
              class="inline-block w-8 h-[1px]"
              :style="{ backgroundColor: theme.primaryColor }"
            ></span>
            <span
              class="text-xs font-bold uppercase tracking-[0.18em]"
              :style="{ color: theme.primaryColor }"
            >
              {{ categoryLabel || brandName }}
            </span>
          </div>

          <h1
            class="sf-heading text-3xl sm:text-4xl lg:text-[2.9rem] font-bold leading-[1.24] tracking-tight"
            :style="{ color: 'var(--sf-ink)' }"
          >
            {{ heroTitle }}
          </h1>

          <p
            class="text-sm sm:text-base leading-[1.85] max-w-xl"
            :style="{ color: 'var(--sf-ink-soft)' }"
          >
            {{ heroSubtitle }}
          </p>

          <div class="pt-3 flex flex-wrap items-center gap-3.5">
            <a
              :href="primaryCtaHref"
              class="px-7 py-3.5 text-xs sm:text-sm font-bold sf-btn-primary inline-flex items-center gap-2 shadow-sm"
            >
              <span>{{ primaryCtaText }}</span>
              <span aria-hidden="true">-&gt;</span>
            </a>
            <a
              v-if="secondaryCtaHref && secondaryCtaText"
              :href="secondaryCtaHref"
              class="px-6 py-3.5 text-xs sm:text-sm font-bold sf-btn-outline inline-flex items-center"
            >
              {{ secondaryCtaText }}
            </a>
          </div>
        </div>

        <div class="lg:col-span-6">
          <div
            class="p-3 sm:p-4 border transition"
            :style="{
              backgroundColor: 'var(--sf-card)',
              borderColor: 'var(--sf-border-strong)',
              borderRadius: 'var(--sf-radius-lg)',
            }"
          >
            <div class="aspect-[4/3] w-full overflow-hidden" :style="{ borderRadius: 'var(--sf-radius)' }">
              <BrandImage
                :src="heroBannerImage"
                :alt="heroBannerCaption?.title || brandName"
                :brand-name="brandName"
                :primary-color="theme.primaryColor"
                :secondary-color="theme.secondaryColor"
                :is-dark="theme.isDark"
                eager
              />
            </div>
            <div
              v-if="heroBannerCaption?.title"
              class="pt-3.5 px-1 flex flex-col sm:flex-row sm:items-center justify-between gap-1 text-xs"
            >
              <strong class="sf-heading font-semibold" :style="{ color: 'var(--sf-ink)' }">
                {{ heroBannerCaption.title }}
              </strong>
              <span :style="{ color: 'var(--sf-ink-muted)' }">
                {{ heroBannerCaption.sub }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div
        v-if="trustMetrics.length"
        class="mt-14 pt-8 border-t grid gap-6"
        :class="metricsGridClass"
        :style="{ borderColor: 'var(--sf-border)' }"
      >
        <div
          v-for="(tm, idx) in trustMetrics"
          :key="idx"
          class="space-y-1"
        >
          <div class="text-[11px] font-semibold uppercase tracking-wider" :style="{ color: 'var(--sf-ink-muted)' }">
            {{ tm.label }}
          </div>
          <div class="sf-heading text-lg sm:text-xl font-bold" :style="{ color: theme.primaryColor }">
            {{ tm.value }}
          </div>
          <p v-if="tm.sub" class="text-xs leading-relaxed" :style="{ color: 'var(--sf-ink-soft)' }">
            {{ tm.sub }}
          </p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import BrandImage from '../BrandImage.vue';
import { resolveGridColumnsClass, type ResolvedThemeSpec } from '../storefrontTypes';

const props = defineProps<{
  brandName: string;
  categoryLabel: string;
  heroTitle: string;
  heroSubtitle: string;
  heroBannerImage: string;
  heroBannerCaption: { title: string; sub: string } | null;
  primaryCtaText: string;
  primaryCtaHref: string;
  secondaryCtaText?: string;
  secondaryCtaHref?: string;
  trustMetrics: Array<Record<string, any>>;
  theme: ResolvedThemeSpec;
}>();

const metricsGridClass = computed(() => resolveGridColumnsClass(props.trustMetrics.length, 4));
</script>
