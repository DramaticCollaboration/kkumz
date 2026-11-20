<template>
  <section
    class="w-full relative overflow-hidden min-h-[520px] md:min-h-[600px] flex flex-col justify-between text-white"
    data-hero-archetype="FULL_SCENIC"
  >
    <div class="absolute inset-0 z-0">
      <BrandImage
        :src="heroBannerImage"
        :alt="heroBannerCaption?.title || brandName"
        :brand-name="brandName"
        :primary-color="theme.primaryColor"
        :secondary-color="theme.secondaryColor"
        is-dark
        eager
      />
      <div
        class="absolute inset-0"
        :style="{
          background: `linear-gradient(180deg, rgba(9,9,11,0.45) 0%, rgba(9,9,11,0.58) 52%, rgba(9,9,11,0.88) 100%)`,
        }"
      ></div>
    </div>

    <div class="max-w-7xl w-full mx-auto px-6 md:px-10 pt-20 md:pt-28 pb-12 relative z-10">
      <div class="max-w-3xl space-y-6">
        <div class="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white/15 backdrop-blur-md border border-white/25 text-xs font-bold">
          <span
            class="w-2 h-2 rounded-full"
            :style="{ backgroundColor: theme.accentColor }"
          ></span>
          <span>{{ heroBannerCaption?.title || categoryLabel || brandName }}</span>
        </div>

        <h1
          class="sf-heading text-3xl sm:text-5xl md:text-[3.4rem] leading-[1.16] tracking-tight text-white"
          :class="theme.archetype === 'COMMUNITY_KINETIC' ? 'font-black' : 'font-bold'"
        >
          {{ heroTitle }}
        </h1>

        <p class="text-sm sm:text-lg text-white/88 leading-relaxed max-w-2xl">
          {{ heroSubtitle }}
        </p>

        <div class="pt-2 flex flex-wrap gap-3">
          <a
            :href="primaryCtaHref"
            class="px-7 py-3.5 text-xs sm:text-sm font-extrabold text-white shadow-xl transition hover:opacity-95 inline-flex items-center gap-2"
            :style="{ backgroundColor: theme.primaryColor, borderRadius: theme.radius }"
          >
            <span>{{ primaryCtaText }}</span>
            <span aria-hidden="true">-&gt;</span>
          </a>
          <a
            v-if="secondaryCtaHref && secondaryCtaText"
            :href="secondaryCtaHref"
            class="px-7 py-3.5 text-xs sm:text-sm font-bold text-white bg-white/15 hover:bg-white/25 backdrop-blur-md border border-white/30 transition inline-flex items-center"
            :style="{ borderRadius: theme.radius }"
          >
            {{ secondaryCtaText }}
          </a>
        </div>
      </div>
    </div>

    <div
      v-if="trustMetrics.length"
      class="relative z-10 w-full border-t border-white/15 bg-black/45 backdrop-blur-md"
    >
      <div
        class="max-w-7xl mx-auto px-6 md:px-10 py-6 grid gap-6"
        :class="metricsGridClass"
      >
        <div v-for="(tm, idx) in trustMetrics" :key="idx" class="space-y-0.5">
          <div class="text-[11px] font-semibold text-white/70 uppercase tracking-wider">
            {{ tm.label }}
          </div>
          <div class="sf-heading text-xl md:text-2xl font-extrabold text-white">
            {{ tm.value }}
          </div>
          <p v-if="tm.sub" class="text-xs text-white/70">{{ tm.sub }}</p>
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
