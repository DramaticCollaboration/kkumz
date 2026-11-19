<template>
  <div class="portal-shell">
    <a href="#portal-main-content" class="skip-nav-link">본문 바로가기</a>
    <ProductHeader :product="product" />
    <main id="portal-main-content" class="portal-main" tabindex="-1">
      <RouterView v-slot="{ Component }">
        <component :is="Component" :product="product" />
      </RouterView>
    </main>
    <ProductFooter :product="product" />
    <button
      v-show="showScrollTop"
      type="button"
      class="scroll-top-btn"
      aria-label="페이지 상단으로 이동"
      @click="scrollToTop"
    >
      상단으로
    </button>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';
import ProductFooter from '@/components/ProductFooter.vue';
import ProductHeader from '@/components/ProductHeader.vue';
import { productData } from '@/data/productData';

const product = productData;
const showScrollTop = ref(false);

function handleWindowScroll() {
  showScrollTop.value = window.scrollY > 320;
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

onMounted(() => {
  window.addEventListener('scroll', handleWindowScroll, { passive: true });
});

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleWindowScroll);
});
</script>

<style scoped>
.skip-nav-link {
  position: absolute;
  top: -48px;
  left: 16px;
  z-index: 9999;
  padding: 8px 16px;
  background: #1e293b;
  color: #f8fafc;
  border-radius: 6px;
  text-decoration: none;
  font-size: 13px;
  transition: top 0.2s ease;
}

.skip-nav-link:focus {
  top: 12px;
}

.fade-page-enter-active,
.fade-page-leave-active {
  transition: opacity 0.18s ease;
}

.fade-page-enter-from,
.fade-page-leave-to {
  opacity: 0;
}

.scroll-top-btn {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 100;
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  background: rgba(15, 23, 42, 0.88);
  color: #f8fafc;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  backdrop-filter: blur(8px);
}
</style>
