<template>
  <div class="workspace-page">
    <header class="ws-header">
      <div>
        <span class="ws-kicker">MY CREATOR WORKSPACE</span>
        <h1>
          {{ isAuthenticated ? `${kkumzAuth.user?.realname || kkumzAuth.user?.username}님의 브랜드 워크스페이스` : '내 브랜드 & 프로젝트 워크스페이스' }}
        </h1>
        <p>본인이 생성했거나 협업 팀원으로 합류한 AI 브랜드 아이디어, 배포된 라이브 사이트, 팀빌딩 지원 현황을 관리합니다.</p>
      </div>
      <button type="button" class="primary-btn" @click="goNewStudio">
        + 새 브랜드 스튜디오 열기
      </button>
    </header>

    <section v-if="!isAuthenticated" class="auth-gate-card">
      <div class="auth-gate-badge">OWNERSHIP WORKSPACE</div>
      <h2>로그인이 필요한 개인 창작 워크스페이스입니다</h2>
      <p>
        꿈즈(Kkumz)에서는 본인이 만든 브랜드 아이디어와 사이트만 안전하게 수정·배포할 수 있습니다.
        로그인하거나 새 계정을 가입하여 나만의 프로젝트를 관리해 보세요.
      </p>
      <div class="auth-gate-actions">
        <button
          type="button"
          class="primary-btn"
          @click="openAuthModal('login', () => loadWorkspace(), '내 워크스페이스를 열려면 로그인해 주세요.')"
        >
          로그인하기
        </button>
        <button
          type="button"
          class="secondary-btn"
          @click="openAuthModal('register', () => loadWorkspace(), '새 메이커 계정을 가입하고 첫 사이트를 만들어 보세요.')"
        >
          회원가입
        </button>
      </div>
    </section>

    <template v-else>
      <section class="kpi-row">
        <div class="kpi-card">
          <span class="kpi-label">내 아이디어·협업 프로젝트</span>
          <strong class="kpi-value">{{ ideas.length }}건</strong>
        </div>
        <div class="kpi-card">
          <span class="kpi-label">내 라이브 배포 사이트</span>
          <strong class="kpi-value">{{ publishedSitesCount }}개</strong>
        </div>
        <div class="kpi-card">
          <span class="kpi-label">내 사이트 누적 응원</span>
          <strong class="kpi-value">{{ totalLikesCount }}회</strong>
        </div>
        <div class="kpi-card">
          <span class="kpi-label">대기 중인 팀 지원자</span>
          <strong class="kpi-value">{{ pendingApplicants.length }}명</strong>
        </div>
      </section>

      <section class="ws-section">
        <div class="section-head">
          <h2>내 아이디어 & 브랜드 프로젝트 ({{ ideas.length }})</h2>
        </div>

        <div v-if="ideas.length === 0" class="empty-ws-box">
          <h3>아직 생성한 브랜드 아이디어가 없습니다</h3>
          <p>아이디어 한 줄로 브랜드 네이밍, 로고, 기획서, 라이브 사이트를 즉시 만들어 보세요.</p>
          <button type="button" class="primary-btn" @click="router.push('/studio')">
            + 첫 브랜드 아이디어 만들기
          </button>
        </div>

        <div v-else class="idea-grid">
          <article v-for="idea in ideas" :key="idea.id" class="idea-card">
            <div class="idea-top">
              <div class="idea-badges">
                <span class="type-tag">{{ formatSiteTypeLabel(idea.siteType) }}</span>
                <span class="owner-tag">{{ idea.isOwner ? '소유자' : '협업 팀원' }}</span>
              </div>
              <span class="step-tag">진행 단계: {{ idea.currentStep }} / 6단계</span>
            </div>
            <div v-if="idea.selectedLogoSvg" class="logo-box" v-html="idea.selectedLogoSvg"></div>
            <h3>{{ idea.selectedBrandName || idea.title }}</h3>
            <p class="prompt-preview">{{ idea.promptText }}</p>

            <div class="idea-meta">
              <span>소유자: {{ idea.authorDisplayName }}</span>
              <span v-if="idea.deployedUrl" class="live-url">{{ idea.deployedUrl }}</span>
            </div>

            <div class="idea-actions">
              <button
                type="button"
                class="btn-studio"
                @click="router.push({ name: 'StudioView', params: { ideaId: idea.id } })"
              >
                AI 스튜디오 편집
              </button>
              <button
                v-if="idea.selectedSiteSlug"
                type="button"
                class="btn-live"
                @click="openLiveSiteInNewTab(idea.selectedSiteSlug)"
              >
                라이브 사이트 ↗
              </button>
            </div>
          </article>
        </div>
      </section>

      <section v-if="pendingApplicants.length > 0" class="ws-section">
        <div class="section-head">
          <h2>내 프로젝트 합류 대기 지원자 ({{ pendingApplicants.length }})</h2>
        </div>
        <div class="applicant-list">
          <div v-for="app in pendingApplicants" :key="app.id" class="applicant-card">
            <div class="applicant-info">
              <div class="applicant-head">
                <strong>{{ app.displayName }}</strong>
                <span class="role-pill">{{ app.specialty }}</span>
                <span class="site-pill">{{ app.siteName }}</span>
              </div>
              <p class="applicant-pitch">{{ app.pitchMessage }}</p>
            </div>
            <div class="applicant-actions">
              <button
                type="button"
                class="btn-live"
                @click="router.push({ name: 'ProjectLoungeView', params: { siteSlug: app.siteSlug } })"
              >
                라운지에서 심사하기 →
              </button>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
  kkumzApi,
  kkumzAuth,
  isAuthenticated,
  openAuthModal,
  formatSiteTypeLabel,
} from '@/api/kkumzApi';

const router = useRouter();
const ideas = ref<any[]>([]);
const mySites = ref<any[]>([]);
const pendingApplicants = ref<any[]>([]);

function goNewStudio() {
  if (!isAuthenticated.value) {
    openAuthModal('login', () => router.push('/studio'), '새 브랜드 스튜디오를 시작하려면 먼저 로그인해 주세요.');
    return;
  }
  router.push('/studio');
}

function openLiveSiteInNewTab(slug: string) {
  if (!slug) return;
  const url = router.resolve({ name: 'LiveSiteRendererView', params: { siteSlug: slug } }).href;
  window.open(url, '_blank', 'noopener,noreferrer');
}

const publishedSitesCount = computed(() => mySites.value.length);
const totalLikesCount = computed(() => mySites.value.reduce((acc, s) => acc + (s.likeCount || 0), 0));

async function loadWorkspace() {
  if (!isAuthenticated.value) {
    ideas.value = [];
    mySites.value = [];
    pendingApplicants.value = [];
    return;
  }
  ideas.value = (await kkumzApi.listIdeas({ mineOnly: true })) || [];
  const allFeed = (await kkumzApi.getCommunityFeed()) || [];
  mySites.value = allFeed.filter((s: any) => s.canEdit === true);

  const applicants: any[] = [];
  for (const site of mySites.value) {
    try {
      const runtime = await kkumzApi.getSiteRuntime(site.siteSlug);
      const members = runtime?.teamMembers || [];
      for (const m of members) {
        if (m.status === 'PENDING') {
          applicants.push({
            ...m,
            siteName: site.siteName,
            siteSlug: site.siteSlug,
          });
        }
      }
    } catch {
      // ignore individual runtime error
    }
  }
  pendingApplicants.value = applicants;
}

watch(
  () => kkumzAuth.user?.username,
  () => {
    loadWorkspace();
  },
);

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
.auth-gate-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 48px 28px;
  text-align: center;
  max-width: 640px;
  margin: 16px auto;
}
.auth-gate-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  background: #eef2ff;
  color: #4f46e5;
  font-size: 11px;
  font-weight: 800;
  margin-bottom: 12px;
}
.auth-gate-card h2 {
  margin: 0 0 10px;
  font-size: 22px;
  color: #0f172a;
}
.auth-gate-card p {
  margin: 0 auto 24px;
  max-width: 480px;
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
}
.auth-gate-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}
.secondary-btn {
  padding: 12px 20px;
  border-radius: 12px;
  border: 1px solid #cbd5e1;
  background: #ffffff;
  color: #0f172a;
  font-weight: 800;
  cursor: pointer;
}
.idea-badges {
  display: flex;
  gap: 6px;
  align-items: center;
}
.owner-tag {
  background: #f1f5f9;
  color: #334155;
  padding: 4px 10px;
  border-radius: 999px;
  font-weight: 700;
}
.empty-ws-box {
  text-align: center;
  padding: 40px 20px;
  border: 1px dashed #cbd5e1;
  border-radius: 16px;
  background: #f8fafc;
}
.empty-ws-box h3 {
  margin: 0 0 8px;
  font-size: 17px;
}
.empty-ws-box p {
  margin: 0 0 18px;
  font-size: 13px;
  color: #64748b;
}
.applicant-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.applicant-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #f8fafc;
}
.applicant-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.role-pill {
  font-size: 11px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 6px;
  background: #eef2ff;
  color: #4f46e5;
}
.site-pill {
  font-size: 11px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 6px;
  background: #e2e8f0;
  color: #334155;
}
.applicant-pitch {
  margin: 0;
  font-size: 13px;
  color: #475569;
}
</style>
