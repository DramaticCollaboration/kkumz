<template>
  <div class="kkumz-live-shell">
    <header class="brand-hero">
      <h1>{{ layout.heroTitle }}</h1>
      <p>{{ layout.heroSubtitle }}</p>
    </header>
    <section class="screen-block" data-screen="SCR_HOME">
      <h2>메인 홈 &amp; 시그니처 쇼케이스</h2>
    </section>
    <section class="screen-block" data-screen="SCR_GALLERY">
      <h2>공간 투어 &amp; 현장 포토 아카이브</h2>
    </section>
    <section class="screen-block" data-screen="SCR_GUIDE_AI">
      <h2>고객 후기 · FAQ · 오시는 길 &amp; AI 컨시어지</h2>
    </section>
  </div>
</template>
<script setup lang="ts">
defineProps<{ tokens: Record<string, any>; layout: Record<string, any> }>();
</script>