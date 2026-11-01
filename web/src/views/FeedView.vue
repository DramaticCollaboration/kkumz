<template>
  <div class="feed-page">
    <!-- 히어로: 한 줄 아이디어 입력 및 스튜디오 즉시 시작 -->
    <section class="hero-card">
      <div class="hero-content">
        <div class="hero-badges">
          <span class="pill-primary">AI 브랜드·멀티사이트 스튜디오</span>
          <span class="pill-outline">네이밍 · 벡터 로고 · 기획서 · 라이브 배포 · 팀빌딩</span>
        </div>
        <h1>
          아이디어 한 줄로 나만의 브랜드 사이트를 만들고,<br />
          함께 성장할 초기 팀원까지 만나는 곳
        </h1>
        <p class="hero-desc">
          복잡한 설정 없이 어떤 브랜드나 서비스를 만들고 싶은지 적어보세요. AI가 브랜드 네이밍 8종, 고해상도 벡터 로고 6종,
          서비스 기획서, 그리고 실제 주문과 참가 신청이 동작하는 라이브 웹사이트까지 즉시 완성합니다.
        </p>

        <div class="quick-prompt-box">
          <div class="site-type-selector">
            <button
              v-for="opt in siteTypeOptions"
              :key="opt.value"
              type="button"
              :class="{ active: quickSiteType === opt.value }"
              @click="quickSiteType = opt.value"
            >
              {{ opt.label }}
            </button>
          </div>
          <div class="prompt-input-row">
            <input
              v-model="quickPrompt"
              type="text"
              placeholder="어떤 브랜드나 서비스를 만들고 싶으신가요? (예: 제주 첫물 유기농 말차와 수제 백자 다완 스토어)"
              @keyup.enter="startStudioWithPrompt"
            />
            <button class="launch-btn" type="button" :disabled="creating" @click="startStudioWithPrompt">
              {{ creating ? '스튜디오 준비 중...' : 'AI 스튜디오에서 만들기 →' }}
            </button>
          </div>
          <div class="sample-prompts">
            <span class="sample-label">추천 아이디어:</span>
            <button
              type="button"
              @click="applySample('COMMERCE', '제주 서귀포 유기농 다원의 첫물 말차와 수제 백자 다완을 판매하는 프리미엄 다도 브랜드 스토어')"
            >
              제주 유기농 말차 & 다기 스토어
            </button>
            <button
              type="button"
              @click="applySample('CMS_SITE', '매주 화·목 한강에서 달리는 나이트 러닝 크루 커뮤니티 및 코스 가이드·게스트 런 신청 웹사이트')"
            >
              서울 나이트 러닝 크루 웹사이트
            </button>
            <button
              type="button"
              @click="applySample('HYBRID', '스페셜티 커피 산지 직교역 인터뷰 매거진과 싱글오리진 원두 구매·시음 클래스 예약 복합 사이트')"
            >
              스페셜티 커피 매거진 & 스토어
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 필터 바 -->
    <section class="filter-bar">
      <div class="filter-tabs">
        <button
          v-for="tab in filterTabs"
          :key="tab.value"
          type="button"
          :class="{ active: selectedSiteType === tab.value }"
          @click="selectSiteTypeFilter(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="filter-controls">
        <select v-model="selectedRecruitRole" @change="loadFeed">
          <option value="ALL">전체 모집 포지션</option>
          <option value="FRONTEND">프론트엔드 개발</option>
          <option value="BACKEND">백엔드 개발</option>
          <option value="DESIGNER">브랜드·프로덕트 디자이너</option>
          <option value="MARKETER">그로스 마케터</option>
          <option value="MD">커머스 상품 MD</option>
          <option value="PLANNER">서비스 기획·PM</option>
          <option value="CO_FOUNDER">공동창업자(Co-Founder)</option>
        </select>

        <div class="search-group">
          <input
            v-model="keyword"
            type="text"
            placeholder="브랜드명, 슬로건 검색..."
            @keyup.enter="loadFeed"
          />
          <button type="button" @click="loadFeed">검색</button>
        </div>

        <div class="sort-group">
          <button type="button" :class="{ active: sortBy === 'POPULAR' }" @click="changeSort('POPULAR')">
            인기순
          </button>
          <button type="button" :class="{ active: sortBy === 'LATEST' }" @click="changeSort('LATEST')">
            최신순
          </button>
        </div>
      </div>
    </section>

    <!-- 쇼케이스 그리드 (다이어트된 깔끔한 카드) -->
    <section v-if="loading" class="loading-box">창업가들의 브랜드 쇼케이스를 불러오는 중입니다...</section>

    <section v-else class="showcase-grid">
      <article v-for="item in feedItems" :key="item.id" class="showcase-card">
        <div
          class="card-banner"
          :style="{
            background: item.designTokens?.surface || '#f8fafc',
            borderBottomColor: (item.designTokens?.primary || '#1e293b') + '18',
          }"
        >
          <div class="banner-top">
            <div class="badge-row">
              <span class="type-pill" :class="item.siteType?.toLowerCase()">
                {{ formatSiteTypeLabel(item.siteType) }}
              </span>
              <span v-if="item.isFeatured" class="featured-pill">★ 추천 쇼케이스</span>
            </div>
            <span class="deploy-pill">
              {{ formatDeployModeLabel(item.deployMode) }} · v{{ item.activeVersion }}
            </span>
          </div>
          <div class="logo-preview" v-html="item.logoSvg"></div>
        </div>

        <div class="card-body">
          <div class="title-row">
            <h2>{{ item.siteName }}</h2>
            <span class="slug-badge">/s/{{ item.siteSlug }}</span>
          </div>
          <p class="slogan">{{ item.brandSlogan }}</p>

          <div class="tag-row">
            <span v-for="tag in (item.tags || []).slice(0, 4)" :key="tag" class="tag-chip">#{{ tag }}</span>
          </div>

          <!-- 모집 중인 포지션 요약 -->
          <div v-if="item.openRecruits && item.openRecruits.length > 0" class="recruit-summary" @click="openLounge(item)">
            <div class="recruit-summary-head">
              <span class="recruit-dot"></span>
              <strong>합류 가능 포지션 ({{ item.openRecruits.length }})</strong>
              <span class="recruit-more">팀빌딩 라운지 →</span>
            </div>
            <div class="recruit-chips">
              <span v-for="rec in item.openRecruits.slice(0, 3)" :key="rec.id" class="role-chip">
                {{ formatPositionRoleLabel(rec.positionRole) }} ({{ rec.recruitedCount }}/{{ rec.headcount }})
              </span>
            </div>
          </div>
        </div>

        <div class="card-footer">
          <div class="metrics">
            <button type="button" class="like-btn" @click="handleLike(item)">
              ♥ {{ item.likeCount }}
            </button>
            <button type="button" class="comment-btn" @click="openCommentModal(item)">
              💬 댓글 {{ item.commentCount }}
            </button>
            <span class="view-stat">조회 {{ item.viewCount }}</span>
          </div>

          <div class="card-actions">
            <button type="button" class="btn-outline" @click="openStudio(item)">
              스튜디오 열기
            </button>
            <button type="button" class="btn-solid" @click="openLiveSite(item)">
              라이브 사이트
            </button>
          </div>
        </div>
      </article>
    </section>

    <!-- 댓글 & 피드백 모달 -->
    <div v-if="activeSiteForComments" class="modal-backdrop" @click.self="activeSiteForComments = null">
      <div class="modal-panel">
        <div class="modal-header">
          <div>
            <h3>{{ activeSiteForComments.siteName }} — 커뮤니티 응원 & 피드백</h3>
            <p class="modal-sub">{{ activeSiteForComments.brandSlogan }}</p>
          </div>
          <button type="button" class="close-btn" @click="activeSiteForComments = null">✕</button>
        </div>

        <div class="comment-list">
          <div v-for="cmt in comments" :key="cmt.id" class="comment-item">
            <div class="comment-meta">
              <strong>{{ cmt.displayName }}</strong>
              <span class="role-badge">{{ cmt.authorRoleBadge }}</span>
            </div>
            <p>{{ cmt.content }}</p>
          </div>
          <div v-if="comments.length === 0" class="empty-comment">첫 응원 댓글을 남겨보세요!</div>
        </div>

        <div class="comment-form">
          <input
            v-model="newCommentText"
            type="text"
            placeholder="브랜드 피드백이나 응원 메시지를 남겨주세요..."
            @keyup.enter="submitComment"
          />
          <button type="button" @click="submitComment">등록</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import {
  kkumzApi,
  formatSiteTypeLabel,
  formatDeployModeLabel,
  formatPositionRoleLabel,
} from '@/api/kkumzApi';

const router = useRouter();

const siteTypeOptions = [
  { value: 'COMMERCE', label: '쇼핑몰 스토어' },
  { value: 'CMS_SITE', label: '브랜드·커뮤니티 웹' },
  { value: 'HYBRID', label: '매거진+스토어 복합' },
];

const filterTabs = [
  { value: 'ALL', label: '전체 쇼케이스' },
  { value: 'COMMERCE', label: '쇼핑몰 스토어' },
  { value: 'CMS_SITE', label: '브랜드·커뮤니티 웹' },
  { value: 'HYBRID', label: '매거진+스토어 복합' },
];

const feedItems = ref<any[]>([]);
const loading = ref(false);
const creating = ref(false);

const quickSiteType = ref('COMMERCE');
const quickPrompt = ref('');

const selectedSiteType = ref('ALL');
const selectedRecruitRole = ref('ALL');
const sortBy = ref('POPULAR');
const keyword = ref('');

const activeSiteForComments = ref<any | null>(null);
const comments = ref<any[]>([]);
const newCommentText = ref('');

async function loadFeed() {
  loading.value = true;
  try {
    feedItems.value =
      (await kkumzApi.getCommunityFeed({
        siteType: selectedSiteType.value,
        recruitRole: selectedRecruitRole.value,
        sortBy: sortBy.value,
        keyword: keyword.value,
      })) || [];
  } finally {
    loading.value = false;
  }
}

function selectSiteTypeFilter(type: string) {
  selectedSiteType.value = type;
  loadFeed();
}

function changeSort(sort: string) {
  sortBy.value = sort;
  loadFeed();
}

function applySample(siteType: string, prompt: string) {
  quickSiteType.value = siteType;
  quickPrompt.value = prompt;
}

async function startStudioWithPrompt() {
  if (!quickPrompt.value.trim()) {
    router.push({ name: 'StudioView' });
    return;
  }
  creating.value = true;
  try {
    const created = await kkumzApi.createIdea({
      promptText: quickPrompt.value.trim(),
      siteType: quickSiteType.value,
      visibility: 'PUBLIC',
    });
    router.push({ name: 'StudioView', params: { ideaId: created.id } });
  } finally {
    creating.value = false;
  }
}

async function handleLike(item: any) {
  const res = await kkumzApi.toggleSiteLike(item.id);
  item.likeCount = res.likeCount;
}

async function openCommentModal(item: any) {
  activeSiteForComments.value = item;
  comments.value = (await kkumzApi.listSiteComments(item.id)) || [];
}

async function submitComment() {
  if (!activeSiteForComments.value || !newCommentText.value.trim()) return;
  comments.value = await kkumzApi.addSiteComment(activeSiteForComments.value.id, {
    displayName: '김도현 창업가',
    authorRoleBadge: '메이커 멤버',
    content: newCommentText.value.trim(),
  });
  activeSiteForComments.value.commentCount = comments.value.length;
  newCommentText.value = '';
}

function openStudio(item: any) {
  router.push({ name: 'StudioView', params: { ideaId: item.ideaId } });
}

function openLounge(item: any) {
  router.push({ name: 'ProjectLoungeView', params: { siteSlug: item.siteSlug } });
}

function openLiveSite(item: any) {
  router.push({ name: 'LiveSiteRendererView', params: { siteSlug: item.siteSlug } });
}

onMounted(() => {
  loadFeed();
});
</script>

<style scoped>
.feed-page {
  display: flex;
  flex-direction: column;
  gap: 28px;
}
.hero-card {
  background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 58%, #311042 100%);
  border-radius: 24px;
  padding: 40px 36px;
  color: #fff;
  box-shadow: 0 20px 44px rgba(15, 23, 42, 0.16);
}
.hero-badges {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.pill-primary {
  background: #4f46e5;
  color: #fff;
  padding: 5px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}
.pill-outline {
  background: rgba(255, 255, 255, 0.1);
  color: #cbd5e1;
  padding: 5px 12px;
  border-radius: 999px;
  font-size: 12px;
}
.hero-content h1 {
  font-size: 30px;
  line-height: 1.35;
  margin: 0 0 12px;
  font-weight: 800;
}
.hero-desc {
  color: #cbd5e1;
  font-size: 14px;
  line-height: 1.65;
  max-width: 780px;
  margin: 0 0 24px;
}
.quick-prompt-box {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 18px;
  padding: 18px;
}
.site-type-selector {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.site-type-selector button {
  padding: 7px 14px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: #e2e8f0;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.site-type-selector button.active {
  background: #6366f1;
  border-color: #6366f1;
  color: #fff;
}
.prompt-input-row {
  display: flex;
  gap: 10px;
}
.prompt-input-row input {
  flex: 1;
  padding: 14px 18px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(15, 23, 42, 0.78);
  color: #fff;
  font-size: 14px;
}
.launch-btn {
  padding: 0 24px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-weight: 800;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
}
.sample-prompts {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}
.sample-label {
  font-size: 12px;
  color: #94a3b8;
}
.sample-prompts button {
  padding: 5px 11px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.06);
  color: #cbd5e1;
  font-size: 12px;
  cursor: pointer;
}
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  background: #fff;
  padding: 14px 18px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
}
.filter-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.filter-tabs button {
  padding: 8px 14px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  font-size: 13px;
  font-weight: 700;
  color: #475569;
  cursor: pointer;
}
.filter-tabs button.active {
  background: #0f172a;
  color: #fff;
  border-color: #0f172a;
}
.filter-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.filter-controls select,
.search-group input {
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  font-size: 13px;
}
.search-group {
  display: flex;
  gap: 6px;
}
.search-group button,
.sort-group button {
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.sort-group button.active {
  background: #eef2ff;
  color: #4f46e5;
  border-color: #c7d2fe;
}
.showcase-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(540px, 1fr));
  gap: 22px;
}
.showcase-card {
  background: #fff;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: transform 0.16s ease, box-shadow 0.16s ease;
}
.showcase-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.08);
}
.card-banner {
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}
.banner-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.badge-row {
  display: flex;
  gap: 6px;
  align-items: center;
}
.type-pill {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 800;
}
.type-pill.commerce {
  background: #dcfce7;
  color: #166534;
}
.type-pill.cms_site {
  background: #ffedd5;
  color: #9a3412;
}
.type-pill.hybrid {
  background: #ede9fe;
  color: #5b21b6;
}
.featured-pill {
  background: #fef3c7;
  color: #92400e;
  padding: 4px 9px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
}
.deploy-pill {
  font-size: 11px;
  font-weight: 600;
  color: #475569;
  background: rgba(255, 255, 255, 0.85);
  padding: 4px 10px;
  border-radius: 999px;
}
.logo-preview {
  height: 42px;
  display: flex;
  align-items: center;
}
.logo-preview :deep(svg) {
  height: 40px;
  width: auto;
}
.card-body {
  padding: 22px 24px;
}
.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.title-row h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
}
.slug-badge {
  font-family: monospace;
  font-size: 12px;
  background: #f1f5f9;
  color: #4f46e5;
  padding: 4px 10px;
  border-radius: 8px;
  font-weight: 700;
}
.slogan {
  color: #475569;
  font-size: 14px;
  line-height: 1.55;
  margin: 0 0 14px;
}
.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
}
.tag-chip {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: #f8fafc;
  color: #475569;
  border: 1px solid #e2e8f0;
}
.recruit-summary {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px 14px;
  cursor: pointer;
}
.recruit-summary:hover {
  border-color: #c7d2fe;
  background: #f5f7ff;
}
.recruit-summary-head {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  margin-bottom: 8px;
}
.recruit-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #10b981;
}
.recruit-more {
  margin-left: auto;
  color: #4f46e5;
  font-weight: 700;
}
.recruit-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.role-chip {
  font-size: 11px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 8px;
  background: #eef2ff;
  color: #4338ca;
}
.card-footer {
  padding: 14px 24px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.metrics {
  display: flex;
  align-items: center;
  gap: 10px;
}
.like-btn,
.comment-btn {
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #fff;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.view-stat {
  font-size: 12px;
  color: #64748b;
}
.card-actions {
  display: flex;
  gap: 8px;
}
.btn-outline {
  padding: 8px 14px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  background: #fff;
  color: #334155;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.btn-solid {
  padding: 8px 16px;
  border-radius: 10px;
  border: none;
  background: #0f172a;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal-panel {
  background: #fff;
  width: 540px;
  max-width: 92vw;
  border-radius: 20px;
  padding: 24px;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}
.modal-header h3 {
  margin: 0;
  font-size: 18px;
}
.modal-sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: #64748b;
}
.close-btn {
  border: none;
  background: #f1f5f9;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  cursor: pointer;
}
.comment-list {
  max-height: 280px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}
.comment-item {
  background: #f8fafc;
  padding: 12px;
  border-radius: 12px;
}
.comment-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 4px;
}
.role-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #e0e7ff;
  color: #3730a3;
}
.comment-form {
  display: flex;
  gap: 8px;
}
.comment-form input {
  flex: 1;
  padding: 10px 14px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
}
.comment-form button {
  padding: 10px 18px;
  border-radius: 10px;
  border: none;
  background: #4f46e5;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}
</style>
