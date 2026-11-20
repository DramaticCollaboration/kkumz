<template>
  <div class="w-full">
    <div
      v-if="announcementText"
      class="w-full py-2 px-4 text-center text-xs font-semibold tracking-wide text-white"
      :style="{ backgroundColor: theme.isDark ? '#18181b' : theme.primaryColor, borderBottom: theme.isDark ? '1px solid rgba(255,255,255,0.08)' : 'none' }"
    >
      <div class="max-w-7xl mx-auto flex flex-wrap items-center justify-center gap-2">
        <span v-if="customBadge" :class="theme.isDark ? 'sf-mono uppercase text-[11px] tracking-widest' : ''">
          {{ customBadge }}
        </span>
        <span v-if="customBadge && primaryCoupon" class="opacity-45">|</span>
        <span v-if="primaryCoupon">
          {{ primaryCoupon.name }} — <strong>{{ Number(primaryCoupon.amount).toLocaleString() }}원 즉시 할인</strong>
          <code class="ml-1.5 px-1.5 py-0.5 rounded bg-white/15 sf-mono text-[11px]">{{ primaryCoupon.code }}</code>
        </span>
      </div>
    </div>

    <header
      class="sticky top-0 z-40 w-full backdrop-blur-md transition-colors"
      :style="headerStyle"
      :class="theme.archetype === 'COMMUNITY_KINETIC' ? 'border-b-2' : 'border-b'"
    >
      <div class="max-w-7xl mx-auto px-5 md:px-10 h-18 md:h-20 flex items-center justify-between gap-4">
        <a
          href="#top"
          class="flex items-center gap-3 shrink-0 group"
          @click="handleLogoClick"
        >
          <div
            v-if="logoSvg"
            class="sf-header-logo flex items-center overflow-hidden"
            :class="{ 'sf-logo-dark-invert': theme.isDark }"
            v-html="logoSvg"
          ></div>
          <span
            v-else
            class="sf-heading text-lg md:text-xl font-extrabold tracking-tight"
            :style="{ color: 'var(--sf-ink)' }"
          >
            {{ brandName }}
          </span>
        </a>

        <nav class="hidden lg:flex items-center gap-1 xl:gap-2">
          <a
            v-for="link in navLinks"
            :key="link.href"
            :href="link.href"
            class="px-3 py-2 text-xs xl:text-sm font-semibold transition rounded-lg"
            :class="theme.isDark ? 'sf-mono uppercase tracking-wider text-zinc-300 hover:text-white hover:bg-white/10' : 'hover:opacity-75'"
            :style="theme.isDark ? {} : { color: 'var(--sf-ink-soft)' }"
          >
            {{ link.label }}
          </a>
        </nav>

        <div class="flex items-center gap-2.5 shrink-0">
          <button
            v-if="hasCommerce"
            type="button"
            class="relative px-3.5 py-2 text-xs font-bold sf-btn-outline flex items-center gap-2"
            data-testid="sf-header-cart-btn"
            @click="emit('open-cart')"
          >
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
              <line x1="3" y1="6" x2="21" y2="6" />
              <path d="M16 10a4 4 0 0 1-8 0" />
            </svg>
            <span class="hidden sm:inline">장바구니</span>
            <span
              v-if="cartCount > 0"
              class="min-w-[18px] h-[18px] px-1 rounded-full text-[10px] font-extrabold text-white flex items-center justify-center"
              :style="{ backgroundColor: theme.primaryColor }"
            >
              {{ cartCount }}
            </span>
          </button>

          <button
            type="button"
            class="px-4 py-2 text-xs font-bold sf-btn-primary shadow-sm"
            data-testid="sf-header-concierge-btn"
            @click="emit('open-concierge')"
          >
            {{ conciergeBtnLabel }}
          </button>
        </div>
      </div>

      <div
        v-if="navLinks.length > 0"
        class="lg:hidden overflow-x-auto border-t px-5 py-2 flex items-center gap-4 whitespace-nowrap"
        :style="{ borderColor: 'var(--sf-border)' }"
      >
        <a
          v-for="link in navLinks"
          :key="`m-${link.href}`"
          :href="link.href"
          class="text-xs font-semibold shrink-0"
          :style="{ color: 'var(--sf-ink-soft)' }"
        >
          {{ link.label }}
        </a>
      </div>
    </header>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ResolvedThemeSpec } from './storefrontTypes';

export interface StorefrontNavLink {
  href: string;
  label: string;
}

const props = defineProps<{
  brandName: string;
  logoSvg?: string;
  customBadge?: string;
  primaryCoupon?: Record<string, any> | null;
  navLinks: StorefrontNavLink[];
  hasCommerce: boolean;
  cartCount: number;
  conciergeBtnLabel: string;
  theme: ResolvedThemeSpec;
  previewMode?: boolean;
}>();

const emit = defineEmits<{
  (e: 'open-cart'): void;
  (e: 'open-concierge'): void;
  (e: 'focus-section', tab: string): void;
}>();

function handleLogoClick(event: MouseEvent) {
  if (props.previewMode) {
    event.preventDefault();
    emit('focus-section', 'VISUAL_THEME');
  }
}

const announcementText = computed(() => Boolean(props.customBadge || props.primaryCoupon));

const headerStyle = computed(() => ({
  backgroundColor: 'var(--sf-header-bg)',
  borderColor: props.theme.archetype === 'COMMUNITY_KINETIC' ? 'var(--sf-ink)' : 'var(--sf-border)',
}));
</script>

<style scoped>
.sf-header-logo {
  height: 38px;
  max-width: 210px;
}
.sf-header-logo :deep(svg) {
  height: 100%;
  width: auto;
  max-width: 100%;
  object-fit: contain;
  display: block;
}
.sf-logo-dark-invert :deep(text) {
  fill: #f4f4f5 !important;
}
</style>
