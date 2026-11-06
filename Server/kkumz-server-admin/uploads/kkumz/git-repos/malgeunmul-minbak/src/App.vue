<template>
  <div class="kkumz-live-shell" :style="{ backgroundColor: tokens?.surface || '#F8FAFC', color: tokens?.text || '#0F172A' }">
    <header class="brand-hero">
      <div class="hero-inner">
        <span class="brand-badge">{{ layout?.category || 'OFFICIAL BRAND' }}</span>
        <h1>{{ layout?.heroTitle }}</h1>
        <p>{{ layout?.heroSubtitle }}</p>
      </div>
    </header>
    <main class="screen-container">
      <section class="screen-block" data-screen="SCR_HOME">
        <h2>메인 홈 &amp; 시그니처 쇼케이스</h2>
        <p class="screen-desc">브랜드 핵심 스토리와 대표 프로그램·상품을 한눈에 소개합니다.</p>
      </section>
      <section class="screen-block" data-screen="SCR_GALLERY">
        <h2>공간 투어 &amp; 현장 포토 아카이브</h2>
        <p class="screen-desc">생생한 현장 갤러리와 주요 공간 포인트를 확인하실 수 있습니다.</p>
      </section>
      <section class="screen-block" data-screen="SCR_GUIDE_AI">
        <h2>고객 후기 · FAQ · 오시는 길 &amp; AI 컨시어지</h2>
        <p class="screen-desc">이용 안내, 자주 묻는 질문 및 24시간 AI 컨시어지 상담을 제공합니다.</p>
      </section>
    </main>
    <footer class="brand-footer">
      <p>{{ layout?.heroTitle }} — Powered by Kkumz Runtime SDK</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  tokens: Record<string, any>;
  layout: Record<string, any>;
  runtime?: Record<string, any> | null;
}>();
</script>

<style scoped>
.kkumz-live-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.brand-hero {
  padding: 4rem 1.5rem;
  text-align: center;
}
.screen-container {
  max-width: 1120px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
  display: grid;
  gap: 1.5rem;
}
.screen-block {
  padding: 1.75rem;
  border-radius: 1rem;
  background: #ffffff;
  border: 1px solid #e2e8f0;
}
.brand-footer {
  margin-top: auto;
  padding: 2rem 1.5rem;
  text-align: center;
  font-size: 0.875rem;
  color: #64748b;
}
</style>
