<template>
  <section
    class="w-full relative overflow-hidden border-b"
    :style="{
      backgroundColor: '#09090b',
      borderColor: 'rgba(255,255,255,0.1)',
      color: '#f4f4f5',
    }"
    data-hero-archetype="PROMO_COMMERCE"
  >
    <div
      class="pointer-events-none absolute -top-24 right-[-10%] w-[520px] h-[520px] rounded-full blur-3xl opacity-25"
      :style="{ background: `radial-gradient(circle, ${theme.secondaryColor} 0%, transparent 70%)` }"
    ></div>

    <div class="max-w-7xl mx-auto px-6 md:px-10 py-16 md:py-24 relative z-10">
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-10 items-center">
        <div class="lg:col-span-6 space-y-6">
          <div class="inline-flex items-center gap-2.5 px-3 py-1.5 rounded-md border border-white/15 bg-white/5">
            <span
              class="w-2 h-2 rounded-full"
              :style="{ backgroundColor: theme.accentColor }"
            ></span>
            <span class="sf-mono text-[11px] uppercase tracking-[0.16em] text-zinc-200">
              {{ customBadge || categoryLabel || brandName }}
            </span>
          </div>

          <h1 class="sf-heading text-3xl sm:text-5xl font-black tracking-tight leading-[1.14] text-white">
            {{ heroTitle }}
          </h1>

          <p class="text-sm sm:text-base text-zinc-400 leading-relaxed max-w-xl">
            {{ heroSubtitle }}
          </p>

          <div class="pt-2 flex flex-wrap gap-3">
            <a
              :href="primaryCtaHref"
              class="px-7 py-3.5 text-xs sm:text-sm font-bold text-white inline-flex items-center gap-2 shadow-lg transition hover:opacity-90"
              :style="{ backgroundColor: theme.primaryColor, borderRadius: theme.radius }"
            >
              <span>{{ primaryCtaText }}</span>
              <span class="sf-mono">-&gt;</span>
            </a>
            <a
              v-if="secondaryCtaHref && secondaryCtaText"
              :href="secondaryCtaHref"
              class="px-6 py-3.5 text-xs sm:text-sm font-bold text-zinc-200 border border-white/20 hover:bg-white/10 transition inline-flex items-center"
              :style="{ borderRadius: theme.radius }"
            >
              {{ secondaryCtaText }}
            </a>
          </div>
        </div>

        <div class="lg:col-span-6">
          <div class="rounded-2xl overflow-hidden border border-white/12 bg-zinc-900/90 shadow-2xl">
            <div class="flex items-center justify-between px-4 py-2.5 border-b border-white/10 bg-zinc-950/80">
              <span class="sf-mono text-[10px] uppercase tracking-widest text-zinc-400">
                {{ brandName }} // FLAGSHIP SETUP
              </span>
              <span
                class="sf-mono text-[10px] uppercase px-2 py-0.5 rounded bg-white/10"
                :style="{ color: theme.accentColor }"
              >
                LIVE SPEC
              </span>
            </div>
            <div class="aspect-[16/10] w-full overflow-hidden">
              <BrandImage
                :src="heroBannerImage"
                :alt="heroBannerCaption?.title || brandName"
                :brand-name="brandName"
                :primary-color="theme.primaryColor"
                :secondary-color="theme.secondaryColor"
                is-dark
                eager
              />
            </div>
            <div
              v-if="heroBannerCaption?.title"
              class="px-5 py-3.5 border-t border-white/10 bg-zinc-950/90 flex flex-wrap items-center justify-between gap-2"
            >
              <strong class="text-xs font-bold text-white">{{ heroBannerCaption.title }}</strong>
              <span class="sf-mono text-[11px] text-zinc-400">{{ heroBannerCaption.sub }}</span>
            </div>
          </div>
        </div>
      </div>

      <div
        v-if="trustMetrics.length"
        class="mt-14 grid gap-4"
        :class="metricsGridClass"
      >
        <div
          v-for="(tm, idx) in trustMetrics"
          :key="idx"
          class="p-5 rounded-xl border border-white/10 bg-zinc-900/70 space-y-1.5"
        >
          <div class="sf-mono text-[10px] uppercase tracking-widest text-zinc-400">
            {{ tm.label }}
          </div>
          <div class="sf-mono text-xl font-bold" :style="{ color: theme.accentColor }">
            {{ tm.value }}
          </div>
          <p v-if="tm.sub" class="text-xs text-zinc-400 leading-relaxed">{{ tm.sub }}</p>
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
