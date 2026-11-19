<template>
  <header class="navbar" :class="{ scrolled: isScrolled }">
    <div class="container nav-container">
      <div class="brand-group">
        <a href="https://empasy.io" class="logo" aria-label="Empasy Official Website">
          <img :src="logoUrl" alt="Empasy" class="logo-img" />
        </a>
        <span class="brand-divider" aria-hidden="true">/</span>
        <RouterLink :to="localePath('')" class="product-brand-badge">
          <span class="product-brand-icon" :class="product.iconBoxClass">
            <i :class="product.faIconClass"></i>
          </span>
          <span class="product-brand-text">
            <strong>{{ product.name }}</strong>
            <small>{{ isEn ? product.englishRoleBadge : roleLabel }}</small>
          </span>
        </RouterLink>
      </div>

      <button
        type="button"
        class="mobile-menu-btn"
        :aria-expanded="mobileMenuOpen"
        aria-label="Toggle Menu"
        @click="mobileMenuOpen = !mobileMenuOpen"
      >
        <i class="fas" :class="mobileMenuOpen ? 'fa-times' : 'fa-bars'"></i>
      </button>

      <nav class="nav-links" :class="{ active: mobileMenuOpen }" style="gap: 1.15rem; font-size: 0.9rem; white-space: nowrap">
        <RouterLink :to="localePath('')" exact-active-class="active-nav-link" @click="closeMenu">
          {{ tr('개요', 'Overview') }}
        </RouterLink>
        <RouterLink :to="localePath('features')" active-class="active-nav-link" @click="closeMenu">
          {{ tr('기능 · 아키텍처', 'Features') }}
        </RouterLink>
        <RouterLink :to="localePath('pricing')" active-class="active-nav-link" @click="closeMenu">
          {{ tr('에디션 · 요금', 'Pricing') }}
        </RouterLink>
        <RouterLink :to="localePath('docs')" active-class="active-nav-link" @click="closeMenu">
          {{ tr('문서 · 가이드', 'Docs') }}
        </RouterLink>
        <RouterLink :to="localePath('download')" active-class="active-nav-link" @click="closeMenu">
          {{ tr('데모 · 다운로드', 'Download') }}
        </RouterLink>
        <RouterLink :to="localePath('blog')" active-class="active-nav-link" @click="closeMenu">
          {{ tr('기술 블로그', 'Blog') }}
        </RouterLink>
      </nav>

      <div class="nav-actions" :class="{ active: mobileMenuOpen }" style="display: flex; align-items: center; gap: 0.5rem">
        <button
          type="button"
          class="btn btn-outline btn-sm"
          data-testid="lang-switch-btn"
          style="min-width: 68px; font-weight: 700"
          @click="toggleLocale"
        >
          {{ locale === 'ko' ? 'EN' : 'KO' }}
        </button>
        <a
          v-if="primaryDemoUrl"
          :href="primaryDemoUrl"
          target="_blank"
          rel="noopener noreferrer"
          class="btn btn-primary btn-sm"
        >
          <i class="fas fa-play" style="margin-right: 6px; font-size: 0.75rem"></i>
          {{ tr('라이브 데모 실행', 'Launch App') }}
        </a>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useHomepageLocale } from '@/composables/useHomepageLocale';
import { getProductVisualGuide } from '@/data/productVisuals';
import type { StandaloneProductData } from '@/types/homepage.types';

const props = defineProps<{
  product: StandaloneProductData;
}>();

const { locale, isEn, localePath, toggleLocale, tr } = useHomepageLocale();

const isScrolled = ref(false);
const mobileMenuOpen = ref(false);
const logoUrl = `${import.meta.env.BASE_URL}logo-dark.svg`;

const roleLabel = computed(() => {
  return getProductVisualGuide(props.product.id)?.roleEasyLabel ?? props.product.tierBadge;
});

const primaryDemoUrl = computed(() => {
  const primary = props.product.entryLinks.find((item) => item.isPrimary) ?? props.product.entryLinks[0];
  return primary?.cloudUrl ?? '';
});

const secondaryAdminUrl = computed(() => {
  if (props.product.entryLinks.length < 2) {
    return '';
  }
  const adminLink = props.product.entryLinks.find((item) => !item.isPrimary && item.cloudUrl.includes('admin'));
  return adminLink?.cloudUrl ?? props.product.entryLinks[1]?.cloudUrl ?? '';
});

function handleScroll(): void {
  isScrolled.value = window.scrollY > 20;
}

function closeMenu(): void {
  mobileMenuOpen.value = false;
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true });
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});
</script>
