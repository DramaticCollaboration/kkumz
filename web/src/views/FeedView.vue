<template>
  <div class="feed-page">
    <!-- 히어로: 깨끗하고 세련된 모던 AI 스튜디오 랜딩 (v0 / Perplexity 스타일) -->
    <section class="hero-section">
      <!-- 배경 소프트 바이올렛 글로우 -->
      <div class="hero-glow"></div>

      <div class="hero-inner">
        <!-- 1. 미니 뱃지 -->
        <div class="hero-badge">
          <span class="badge-dot"></span>
          <span class="badge-title">AI 브랜드·멀티사이트 스튜디오</span>
          <span class="badge-divider">·</span>
          <span class="badge-sub">네이밍 · 로고 · 기획서 · 라이브 웹</span>
        </div>

        <!-- 2. 명확하고 날카로운 가치 제안 헤드라인 -->
        <h1 class="hero-heading">
          아이디어 한 줄로 완성하는<br />
          <span class="gradient-text">나만의 라이브 브랜드 사이트</span>
        </h1>
        <p class="hero-desc">
          브랜드 네이밍부터 고해상도 로고, 서비스 기획서, 실제 동작하는 커머스·웹사이트까지 AI가 단 1분 만에 완성합니다.
        </p>

        <!-- 3. 고도화된 프롬프트 카드 (인풋 + 타입 칩 + CTA 일체형) -->
        <div class="prompt-card">
          <textarea
            v-model="quickPrompt"
            class="prompt-textarea"
            rows="2"
            placeholder="어떤 브랜드나 서비스를 만들고 싶으신가요? (예: 제주 서귀포 첫물 유기농 말차와 수제 백자 다완을 판매하는 프리미엄 다도 스토어)"
            @keydown.enter.exact.prevent="startStudioWithPrompt"
          ></textarea>

          <div class="prompt-bottom-bar">
            <!-- 사이트 타입 칩 셀렉터 -->
            <div class="type-chips">
              <button
                v-for="opt in siteTypeOptions"
                :key="opt.value"
                type="button"
                class="type-chip-btn"
                :class="{ active: quickSiteType === opt.value }"
                @click="quickSiteType = opt.value"
              >
                {{ opt.label }}
              </button>
            </div>

            <!-- 메인 CTA 버튼 -->
            <button
              class="create-cta-btn"
              type="button"
              :disabled="creating"
              @click="startStudioWithPrompt"
            >
              <span v-if="creating" class="btn-spinner"></span>
              <span>{{ creating ? '스튜디오 준비 중...' : 'AI 스튜디오에서 만들기' }}</span>
              <span class="arrow">→</span>
            </button>
          </div>
        </div>

        <!-- 4. 정제된 추천 키워드 태그 -->
        <div v-if="presetKeywords.length > 0" class="sample-keywords">
          <span class="sample-label">추천 키워드:</span>
          <div class="sample-list">
            <button
              v-for="preset in presetKeywords"
              :key="preset.code"
              type="button"
              class="sample-pill"
              @click="applySample(preset.siteType, preset.prompt)"
            >
              # {{ preset.title || preset.label }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 쇼케이스 탐색 툴바 (2단 분리 및 반응형 정렬 개선) -->
    <section class="filter-section">
      <div class="filter-top-row">
        <!-- 카테고리 탭 -->
        <div class="filter-tabs">
          <button
            v-for="tab in filterTabs"
            :key="tab.value"
            type="button"
            class="tab-btn"
            :class="{ active: selectedSiteType === tab.value }"
            @click="selectSiteTypeFilter(tab.value)"
          >
            {{ tab.label }}
          </button>
        </div>

        <!-- 우측 컨트롤 그룹 -->
        <div class="filter-controls-group">
          <!-- 포지션 필터 -->
          <div class="select-wrapper">
            <select v-model="selectedRecruitRole" class="custom-select" @change="loadFeed">
              <option value="ALL">전체 모집 포지션</option>
              <option value="FRONTEND">프론트엔드 개발</option>
              <option value="BACKEND">백엔드 개발</option>
              <option value="DESIGNER">브랜드·프로덕트 디자이너</option>
              <option value="MARKETER">그로스 마케터</option>
              <option value="MD">커머스 상품 MD</option>
              <option value="PLANNER">서비스 기획·PM</option>
              <option value="CO_FOUNDER">공동창업자</option>
            </select>
          </div>

          <!-- 정렬 세그먼트 -->
          <div class="sort-segment">
            <button
              type="button"
              class="sort-btn"
              :class="{ active: sortBy === 'POPULAR' }"
              @click="changeSort('POPULAR')"
            >
              인기순
            </button>
            <button
              type="button"
              class="sort-btn"
              :class="{ active: sortBy === 'LATEST' }"
              @click="changeSort('LATEST')"
            >
              최신순
            </button>
          </div>

          <!-- 검색창 -->
          <div class="search-box">
            <svg class="search-icon" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M9 3.5a5.5 5.5 0 100 11 5.5 5.5 0 000-11zM2 9a7 7 0 1112.452 4.391l3.328 3.329a.75.75 0 11-1.06 1.06l-3.329-3.328A7 7 0 012 9z" clip-rule="evenodd" />
            </svg>
            <input
              v-model="keyword"
              type="text"
              class="search-input"
              placeholder="브랜드명, 슬로건 검색..."
              @keyup.enter="loadFeed"
            />
            <button v-if="keyword" type="button" class="search-clear-btn" @click="clearKeyword">✕</button>
          </div>
        </div>
      </div>
    </section>

    <!-- 쇼케이스 그리드 (다이어트된 깔끔한 카드) -->
    <section v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>창업가들의 생생한 브랜드 쇼케이스를 불러오는 중입니다...</p>
    </section>

    <section v-else-if="feedItems.length === 0" class="empty-state">
      <h3>발견된 쇼케이스가 없습니다</h3>
      <p>새로운 아이디어로 첫 번째 브랜드 쇼케이스의 주인공이 되어보세요.</p>
      <button class="empty-cta" type="button" @click="router.push('/studio')">
        새 아이디어 만들기
      </button>
    </section>

    <section v-else class="showcase-grid">
      <article v-for="item in feedItems" :key="item.id" class="showcase-card">
        <div
          class="card-banner"
          :style="{
            background: item.designTokens?.surface || '#f8fafc',
            borderBottomColor: (item.designTokens?.primary || '#1e293b') + '15',
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

          <!-- 모집 중인 포지션 요약 (팀빌딩 연동 강화) -->
          <div v-if="item.openRecruits && item.openRecruits.length > 0" class="recruit-summary" @click="openLounge(item)">
            <div class="recruit-summary-head">
              <span class="recruit-dot"></span>
              <strong>팀원 모집 중 ({{ item.openRecruits.length }})</strong>
              <span class="recruit-more">지원하기 →</span>
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
              <span class="like-icon">♥</span> {{ item.likeCount }}
            </button>
            <button type="button" class="comment-btn" @click="openCommentModal(item)">
              댓글 {{ item.commentCount }}
            </button>
            <span class="view-stat">조회 {{ item.viewCount }}</span>
          </div>

          <div class="card-actions">
            <button type="button" class="btn-outline" @click="openStudio(item)">
              스튜디오
            </button>
            <button type="button" class="btn-solid" @click="openLiveSite(item)">
              라이브 사이트 →
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
            v-model="newCommentAuthor"
            type="text"
            placeholder="작성자 닉네임"
            style="max-width: 140px"
          />
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
const presetKeywords = ref<any[]>([]);
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
const newCommentAuthor = ref('');
const newCommentText = ref('');

async function loadStudioPresets() {
  try {
    const meta = await kkumzApi.getStudioMetadata();
    presetKeywords.value = meta?.starterPresets || [];
  } catch {
    presetKeywords.value = [];
  }
}

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

function clearKeyword() {
  keyword.value = '';
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
  const res = await kkumzApi.toggleSiteLike(item.id, item.ownerUsername || item.siteSlug);
  item.likeCount = res.likeCount;
}

async function openCommentModal(item: any) {
  activeSiteForComments.value = item;
  comments.value = (await kkumzApi.listSiteComments(item.id)) || [];
}

async function submitComment() {
  if (!activeSiteForComments.value || !newCommentText.value.trim()) return;
  if (!newCommentAuthor.value.trim()) {
    alert('작성자 닉네임을 입력해 주세요.');
    return;
  }
  comments.value = await kkumzApi.addSiteComment(activeSiteForComments.value.id, {
    username: newCommentAuthor.value.trim(),
    displayName: newCommentAuthor.value.trim(),
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
  const url = router.resolve({ name: 'LiveSiteRendererView', params: { siteSlug: item.siteSlug } }).href;
  window.open(url, '_blank', 'noopener,noreferrer');
}

onMounted(() => {
  loadStudioPresets();
  loadFeed();
});
</script>

<style scoped>
.feed-page {
  display: flex;
  flex-direction: column;
  gap: 36px;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px 64px;
}

/* ============================================================
   1. HERO SECTION (모던 라이트 & 바이올렛 글로우)
   ============================================================ */
.hero-section {
  position: relative;
  padding: 56px 20px 48px;
  text-align: center;
  overflow: hidden;
  border-radius: 28px;
  background: linear-gradient(180deg, #ffffff 0%, #fcfaff 100%);
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 10px 30px -10px rgba(99, 102, 241, 0.07);
}

.hero-glow {
  position: absolute;
  top: -80px;
  left: 50%;
  transform: translateX(-50%);
  width: 680px;
  height: 320px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.15) 0%, rgba(168, 85, 247, 0.08) 50%, transparent 75%);
  filter: blur(48px);
  pointer-events: none;
  z-index: 0;
}

.hero-inner {
  position: relative;
  z-index: 1;
  max-width: 820px;
  margin: 0 auto;
}

/* 미니 뱃지 */
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 9999px;
  background: #f5f3ff;
  border: 1px solid #ede9fe;
  font-size: 12px;
  margin-bottom: 20px;
}
.badge-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #6366f1;
  animation: pulseDot 2s infinite ease-in-out;
}
@keyframes pulseDot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(0.85); }
}
.badge-title {
  color: #4f46e5;
  font-weight: 700;
}
.badge-divider {
  color: #cbd5e1;
}
.badge-sub {
  color: #64748b;
  font-weight: 500;
}

/* 헤드라인 & 서브카피 */
.hero-heading {
  font-size: 38px;
  line-height: 1.28;
  font-weight: 900;
  color: #0f172a;
  letter-spacing: -0.025em;
  margin: 0 0 14px;
}
.gradient-text {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 50%, #db2777 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.hero-desc {
  font-size: 15px;
  line-height: 1.65;
  color: #64748b;
  max-width: 640px;
  margin: 0 auto 32px;
}

/* 프롬프트 인풋 카드 */
.prompt-card {
  background: #ffffff;
  border-radius: 20px;
  border: 1.5px solid #e2e8f0;
  box-shadow: 0 16px 36px -8px rgba(15, 23, 42, 0.08), 0 0 0 1px rgba(99, 102, 241, 0.04);
  padding: 16px 18px 14px;
  text-align: left;
  transition: all 0.2s ease;
}
.prompt-card:focus-within {
  border-color: #6366f1;
  box-shadow: 0 20px 40px -10px rgba(99, 102, 241, 0.16), 0 0 0 4px rgba(99, 102, 241, 0.12);
}
.prompt-textarea {
  width: 100%;
  border: none;
  background: transparent;
  outline: none;
  font-size: 15px;
  line-height: 1.55;
  color: #0f172a;
  resize: none;
  font-family: inherit;
}
.prompt-textarea::placeholder {
  color: #94a3b8;
}

/* 프롬프트 카드 하단 바 */
.prompt-bottom-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 12px;
  margin-top: 8px;
  border-top: 1px solid #f1f5f9;
  flex-wrap: wrap;
}

.type-chips {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.type-chip-btn {
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #475569;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
}
.type-chip-btn:hover {
  background: #f1f5f9;
  color: #1e293b;
}
.type-chip-btn.active {
  background: #0f172a;
  border-color: #0f172a;
  color: #ffffff;
}

.create-cta-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 10px 20px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%);
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(79, 70, 229, 0.3);
  transition: all 0.18s ease;
}
.create-cta-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #4338ca 0%, #4f46e5 100%);
  box-shadow: 0 6px 18px rgba(79, 70, 229, 0.4);
  transform: translateY(-1px);
}
.create-cta-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}
.btn-icon {
  font-size: 13px;
}
.arrow {
  font-size: 14px;
  transition: transform 0.15s ease;
}
.create-cta-btn:hover:not(:disabled) .arrow {
  transform: translateX(2px);
}
.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 추천 키워드 태그 */
.sample-keywords {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 18px;
  flex-wrap: wrap;
}
.sample-label {
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
}
.sample-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.sample-pill {
  border: 1px solid #e2e8f0;
  background: #ffffff;
  color: #475569;
  font-size: 12px;
  padding: 5px 12px;
  border-radius: 9999px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.sample-pill:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
  color: #4f46e5;
}

/* ============================================================
   2. FILTER & TOOLBAR SECTION (정돈된 2단 반응형)
   ============================================================ */
.filter-section {
  background: #ffffff;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.03);
}

.filter-top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-tabs {
  display: flex;
  gap: 6px;
  overflow-x: auto;
  scrollbar-width: none;
}
.filter-tabs::-webkit-scrollbar {
  display: none;
}
.tab-btn {
  padding: 8px 14px;
  border-radius: 10px;
  border: 1px solid transparent;
  background: transparent;
  color: #64748b;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.15s ease;
}
.tab-btn:hover {
  background: #f8fafc;
  color: #1e293b;
}
.tab-btn.active {
  background: #f1f5f9;
  color: #0f172a;
  font-weight: 700;
}

.filter-controls-group {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

/* 포지션 드롭다운 */
.select-wrapper {
  position: relative;
}
.custom-select {
  appearance: none;
  background: #f8fafc url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%2364748b'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E") no-repeat right 10px center / 14px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 8px 30px 8px 12px;
  font-size: 13px;
  font-weight: 500;
  color: #334155;
  cursor: pointer;
  outline: none;
}
.custom-select:focus {
  border-color: #6366f1;
}

/* 정렬 세그먼트 */
.sort-segment {
  display: flex;
  background: #f1f5f9;
  padding: 3px;
  border-radius: 10px;
}
.sort-btn {
  border: none;
  background: transparent;
  padding: 5px 12px;
  border-radius: 7px;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.12s ease;
}
.sort-btn.active {
  background: #ffffff;
  color: #0f172a;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
}

/* 인라인 검색창 */
.search-box {
  position: relative;
  display: flex;
  align-items: center;
}
.search-icon {
  position: absolute;
  left: 10px;
  width: 15px;
  height: 15px;
  color: #94a3b8;
  pointer-events: none;
}
.search-input {
  width: 190px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 7px 28px 7px 32px;
  font-size: 12px;
  color: #1e293b;
  outline: none;
  transition: all 0.2s ease;
}
.search-input:focus {
  width: 230px;
  background: #ffffff;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}
.search-clear-btn {
  position: absolute;
  right: 8px;
  border: none;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  font-size: 11px;
}

/* ============================================================
   3. SHOWCASE GRID & CARDS
   ============================================================ */
.loading-state,
.empty-state {
  text-align: center;
  padding: 64px 20px;
  background: #ffffff;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
}
.loading-spinner {
  width: 32px;
  height: 32px;
  margin: 0 auto 16px;
  border: 3px solid #e2e8f0;
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
.empty-icon {
  font-size: 40px;
  margin-bottom: 12px;
}
.empty-state h3 {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 6px;
}
.empty-state p {
  color: #64748b;
  font-size: 14px;
  margin: 0 0 20px;
}
.empty-cta {
  padding: 10px 22px;
  border-radius: 10px;
  border: none;
  background: #0f172a;
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.showcase-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(520px, 1fr));
  gap: 24px;
}

.showcase-card {
  background: #ffffff;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.showcase-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 16px 36px -6px rgba(15, 23, 42, 0.08), 0 4px 12px rgba(15, 23, 42, 0.03);
  border-color: #cbd5e1;
}

.card-banner {
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  position: relative;
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
  border-radius: 9999px;
  font-size: 11px;
  font-weight: 800;
}
.type-pill.commerce {
  background: #dcfce7;
  color: #15803d;
}
.type-pill.cms_site {
  background: #ffedd5;
  color: #c2410c;
}
.type-pill.hybrid {
  background: #ede9fe;
  color: #6d28d9;
}
.featured-pill {
  background: #fef3c7;
  color: #92400e;
  padding: 4px 9px;
  border-radius: 9999px;
  font-size: 11px;
  font-weight: 700;
}
.deploy-pill {
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
  background: rgba(255, 255, 255, 0.9);
  padding: 4px 10px;
  border-radius: 9999px;
  border: 1px solid rgba(226, 232, 240, 0.6);
}
.logo-preview {
  height: 44px;
  display: flex;
  align-items: center;
}
.logo-preview :deep(svg) {
  height: 42px;
  width: auto;
}

.card-body {
  padding: 22px 24px;
  flex: 1;
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
  letter-spacing: -0.01em;
}
.slug-badge {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 11px;
  background: #f1f5f9;
  color: #4f46e5;
  padding: 3px 8px;
  border-radius: 6px;
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
  font-size: 11px;
  padding: 3px 9px;
  border-radius: 9999px;
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.recruit-summary {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px 14px;
  cursor: pointer;
  transition: all 0.15s ease;
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
  padding: 3px 9px;
  border-radius: 6px;
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
  background: #fafafa;
}
.metrics {
  display: flex;
  align-items: center;
  gap: 8px;
}
.like-btn,
.comment-btn {
  padding: 6px 11px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  transition: all 0.12s ease;
}
.like-btn:hover,
.comment-btn:hover {
  background: #f8fafc;
  color: #0f172a;
}
.like-icon {
  color: #ef4444;
}
.view-stat {
  font-size: 11px;
  color: #94a3b8;
  margin-left: 2px;
}

.card-actions {
  display: flex;
  gap: 8px;
}
.btn-outline {
  padding: 8px 14px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  background: #ffffff;
  color: #334155;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.12s ease;
}
.btn-outline:hover {
  background: #f8fafc;
  border-color: #94a3b8;
}
.btn-solid {
  padding: 8px 16px;
  border-radius: 10px;
  border: none;
  background: #0f172a;
  color: #ffffff;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.12s ease;
}
.btn-solid:hover {
  background: #1e293b;
}

/* ============================================================
   4. COMMENT MODAL
   ============================================================ */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal-panel {
  background: #ffffff;
  width: 520px;
  max-width: 92vw;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 24px 48px -12px rgba(15, 23, 42, 0.25);
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
  font-weight: 800;
  color: #0f172a;
}
.modal-sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: #64748b;
}
.close-btn {
  border: none;
  background: #f1f5f9;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  cursor: pointer;
  color: #64748b;
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
  border-radius: 9999px;
  background: #e0e7ff;
  color: #3730a3;
  font-weight: 600;
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
  font-size: 13px;
  outline: none;
}
.comment-form input:focus {
  border-color: #6366f1;
}
.comment-form button {
  padding: 10px 18px;
  border-radius: 10px;
  border: none;
  background: #4f46e5;
  color: #ffffff;
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
}

/* ============================================================
   5. RESPONSIVE BREAKPOINTS
   ============================================================ */
@media (max-width: 768px) {
  .hero-section {
    padding: 36px 16px 32px;
  }
  .hero-heading {
    font-size: 26px;
  }
  .hero-desc {
    font-size: 13px;
  }
  .showcase-grid {
    grid-template-columns: 1fr;
  }
  .prompt-bottom-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .create-cta-btn {
    width: 100%;
    justify-content: center;
  }
  .filter-controls-group {
    width: 100%;
    justify-content: space-between;
  }
  .search-box {
    width: 100%;
  }
  .search-input {
    width: 100%;
  }
  .search-input:focus {
    width: 100%;
  }
}
</style>
