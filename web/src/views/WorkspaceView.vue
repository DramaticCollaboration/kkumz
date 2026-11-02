<template>
  <div class="workspace-page">
    <header class="ws-header">
      <div>
        <span class="ws-kicker">MY CREATOR WORKSPACE</span>
        <h1>내 브랜드 & 프로젝트 워크스페이스</h1>
        <p>진행 중인 AI 브랜드 아이디어, 배포된 라이브 사이트, 팀빌딩 현황을 한눈에 관리하세요.</p>
      </div>
      <button type="button" class="primary-btn" @click="router.push('/studio')">
        + 새 브랜드 스튜디오 열기
      </button>
    </header>

    <section class="kpi-row">
      <div class="kpi-card">
        <span class="kpi-label">진행 중인 아이디어</span>
        <strong class="kpi-value">{{ ideas.length }}건</strong>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">라이브 배포 사이트</span>
        <strong class="kpi-value">{{ publishedSitesCount }}개</strong>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">누적 커뮤니티 응원</span>
        <strong class="kpi-value">{{ totalLikesCount }}회</strong>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">오픈 팀원 모집 공고</span>
        <strong class="kpi-value">{{ totalRecruitsCount }}건</strong>
      </div>
    </section>

    <section class="ws-section">
      <div class="section-head">
        <h2>내 아이디어 & 브랜드 프로젝트</h2>
      </div>
      <div class="idea-grid">
        <article v-for="idea in ideas" :key="idea.id" class="idea-card">
          <div class="idea-top">
            <span class="type-tag">{{ formatSiteTypeLabel(idea.siteType) }}</span>
            <span class="step-tag">진행 단계: {{ idea.currentStep }} / 6단계</span>
          </div>
          <div v-if="idea.selectedLogoSvg" class="logo-box" v-html="idea.selectedLogoSvg"></div>
          <h3>{{ idea.selectedBrandName || idea.title }}</h3>
          <p class="prompt-preview">{{ idea.promptText }}</p>

          <div class="idea-meta">
            <span>작성자: {{ idea.authorDisplayName }}</span>
            <span v-if="idea.deployedUrl" class="live-url">{{ idea.deployedUrl }}</span>
          </div>

          <div class="idea-actions">
            <button
              type="button"
              class="btn-studio"
              @click="router.push({ name: 'StudioView', params: { ideaId: idea.id } })"
            >
              AI 스튜디오 이어서 작업
            </button>
            <button
              v-if="idea.selectedSiteSlug"
              type="button"
              class="btn-live"
              @click="openLiveSiteInNewTab(idea.selectedSiteSlug)"
            >
              라이브 사이트 보기 ↗
            </button>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { kkumzApi, formatSiteTypeLabel } from '@/api/kkumzApi';

const router = useRouter();
const ideas = ref<any[]>([]);
const feed = ref<any[]>([]);

function openLiveSiteInNewTab(slug: string) {
  if (!slug) return;
  const url = router.resolve({ name: 'LiveSiteRendererView', params: { siteSlug: slug } }).href;
  window.open(url, '_blank', 'noopener,noreferrer');
}

const publishedSitesCount = computed(() => ideas.value.filter((i) => i.status === 'PUBLISHED').length);
const totalLikesCount = computed(() => feed.value.reduce((acc, s) => acc + (s.likeCount || 0), 0));
const totalRecruitsCount = computed(() =>
  feed.value.reduce((acc, s) => acc + ((s.openRecruits && s.openRecruits.length) || 0), 0),
);

async function loadWorkspace() {
  ideas.value = (await kkumzApi.listIdeas()) || [];
  feed.value = (await kkumzApi.getCommunityFeed()) || [];
}

onMounted(() => {
  loadWorkspace();
});
</script>

<style scoped>
.workspace-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.ws-header {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.ws-kicker {
  font-size: 11px;
  font-weight: 800;
  color: #4f46e5;
  letter-spacing: 0.06em;
}
.ws-header h1 {
  margin: 6px 0 8px;
  font-size: 24px;
}
.ws-header p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}
.primary-btn {
  padding: 12px 20px;
  border-radius: 12px;
  border: none;
  background: #4f46e5;
  color: #fff;
  font-weight: 800;
  cursor: pointer;
}
.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.kpi-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.kpi-label {
  font-size: 13px;
  color: #64748b;
}
.kpi-value {
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
}
.ws-section {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 24px;
}
.section-head h2 {
  margin: 0 0 18px;
  font-size: 18px;
}
.idea-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 18px;
}
.idea-card {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.idea-top {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}
.type-tag {
  background: #eef2ff;
  color: #4f46e5;
  padding: 4px 10px;
  border-radius: 999px;
  font-weight: 700;
}
.step-tag {
  color: #64748b;
  font-weight: 700;
}
.logo-box {
  height: 36px;
}
.logo-box :deep(svg) {
  height: 34px;
  width: auto;
}
.idea-card h3 {
  margin: 0;
  font-size: 18px;
}
.prompt-preview {
  margin: 0;
  font-size: 13px;
  color: #475569;
  line-height: 1.55;
}
.idea-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #64748b;
}
.live-url {
  font-family: monospace;
  color: #4f46e5;
  font-weight: 700;
}
.idea-actions {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}
.btn-studio {
  flex: 1;
  padding: 10px;
  border-radius: 10px;
  border: none;
  background: #4f46e5;
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
}
.btn-live {
  padding: 10px 14px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  background: #fff;
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
}
</style>
