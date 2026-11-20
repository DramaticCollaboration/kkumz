<template>
  <section
    class="w-full border-b"
    :style="{ backgroundColor: 'var(--sf-bg)', borderColor: 'var(--sf-border-strong)' }"
    data-hero-archetype="SPLIT_VISUAL"
  >
    <div class="max-w-7xl mx-auto px-6 md:px-10 py-10 md:py-16">
      <div
        class="flex flex-wrap items-center justify-between gap-2 pb-4 mb-10 border-b text-[11px] font-bold uppercase tracking-[0.16em]"
        :style="{ borderColor: 'var(--sf-border-strong)', color: 'var(--sf-ink-muted)' }"
      >
        <span>{{ brandName }} JOURNAL &amp; SELECT SHOP</span>
        <span>{{ categoryLabel || 'ARCHIVE EDITION' }}</span>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-12 items-stretch">
        <div class="lg:col-span-5 flex flex-col justify-between space-y-8">
          <div class="space-y-5">
            <span
              class="inline-block px-3 py-1 text-[11px] font-bold uppercase tracking-wider text-white"
              :style="{ backgroundColor: theme.primaryColor, borderRadius: '4px' }"
            >
              {{ heroBannerCaption?.title || customBadge || 'FEATURED ARCHIVE' }}
            </span>

            <h1
              class="sf-heading text-3xl sm:text-4xl lg:text-[2.65rem] font-bold leading-[1.24] tracking-tight"
              :style="{ color: 'var(--sf-ink)' }"
            >
              {{ heroTitle }}
            </h1>

            <p
              class="text-sm sm:text-base leading-[1.8] pl-4 border-l-2"
              :style="{ color: 'var(--sf-ink-soft)', borderColor: theme.accentColor }"
            >
              {{ heroSubtitle }}
            </p>
          </div>

          <div class="space-y-4">
            <div class="flex flex-wrap gap-3">
              <a
                :href="primaryCtaHref"
                class="px-6 py-3.5 text-xs sm:text-sm font-bold sf-btn-primary inline-flex items-center gap-2"
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
        </div>

        <div class="lg:col-span-7 flex flex-col">
          <div
            class="flex-1 overflow-hidden border"
            :style="{ borderColor: 'var(--sf-border-strong)', borderRadius: 'var(--sf-radius)' }"
          >
            <div class="aspect-[16/10] w-full h-full min-h-[280px] overflow-hidden">
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
          </div>
          <div
            v-if="heroBannerCaption"
            class="pt-3 flex items-center justify-between text-xs"
            :style="{ color: 'var(--sf-ink-muted)' }"
          >
            <span class="font-semibold" :style="{ color: 'var(--sf-ink)' }">{{ heroBannerCaption.title }}</span>
            <span>{{ heroBannerCaption.sub }}</span>
          </div>
        </div>
      </div>

      <div
        v-if="trustMetrics.length"
        class="mt-12 pt-6 border-t-2 grid gap-6"
        :class="metricsGridClass"
        :style="{ borderColor: 'var(--sf-ink)' }"
      >
        <div v-for="(tm, idx) in trustMetrics" :key="idx" class="space-y-0.5">
          <div class="text-[11px] font-bold uppercase tracking-wider" :style="{ color: 'var(--sf-ink-muted)' }">
            {{ tm.label }}
          </div>
          <div class="sf-heading text-lg font-bold" :style="{ color: 'var(--sf-ink)' }">
            {{ tm.value }}
          </div>
          <p v-if="tm.sub" class="text-xs" :style="{ color: 'var(--sf-ink-soft)' }">{{ tm.sub }}</p>
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
  customBadge?: string;
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
