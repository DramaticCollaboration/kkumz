<template>
  <div class="kkumz-container space-y-6">
    <!-- 상단 프로젝트 전환 바 -->
    <div class="kkumz-card p-4 flex flex-wrap items-center justify-between gap-3">
      <div class="flex flex-wrap items-center gap-2">
        <span class="text-xs font-extrabold text-slate-500 mr-1">팀빌딩 라운지 선택</span>
        <button
          v-for="p in activeProjects"
          :key="p.slug"
          class="px-3.5 py-1.5 rounded-full text-xs font-bold border transition"
          :class="
            currentSlug === p.slug
              ? 'bg-indigo-600 text-white border-indigo-600 shadow-sm'
              : 'bg-slate-50 text-slate-700 border-slate-200 hover:border-indigo-300'
          "
          @click="router.push(`/lounge/${p.slug}`)"
        >
          {{ p.brandName }}
        </button>
      </div>
      <div v-if="runtime" class="flex items-center gap-2">
        <button
          class="kkumz-btn-secondary text-xs py-1.5"
          @click="router.push(`/studio/${runtime.ideaId}`)"
        >
          AI 스튜디오 열기
        </button>
        <button
          class="kkumz-btn-primary text-xs py-1.5"
          @click="openLiveSiteInNewTab(runtime.siteSlug)"
        >
          라이브 사이트 방문
        </button>
      </div>
    </div>

    <div v-if="loading" class="py-16 text-center text-sm font-bold text-slate-400">
      팀빌딩 라운지 데이터를 불러오는 중입니다...
    </div>

    <template v-else-if="runtime">
      <!-- 프로젝트 헤더 카드 -->
      <div class="kkumz-card p-6 md:p-8 flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div class="flex items-start gap-5">
          <div
            class="w-20 h-20 rounded-2xl bg-slate-50 border border-slate-200 flex items-center justify-center p-2 shrink-0 overflow-hidden"
            v-html="runtime.logoSvg"
          ></div>
          <div>
            <div class="flex flex-wrap items-center gap-2 mb-2">
              <span class="kkumz-badge bg-indigo-50 text-indigo-700 border border-indigo-200">
                {{ formatSiteTypeLabel(runtime.siteType) }}
              </span>
              <span class="kkumz-badge bg-emerald-50 text-emerald-700 border border-emerald-200">
                {{ formatDeployModeLabel(runtime.deployMode) }} · v{{ runtime.activeVersionNo || runtime.activeVersion || 1 }}
              </span>
              <span class="text-xs font-mono text-slate-400">/s/{{ runtime.siteSlug }}</span>
            </div>
            <h1 class="text-2xl font-extrabold text-slate-900">{{ runtime.brandName || runtime.siteName }}</h1>
            <p class="text-sm text-slate-600 mt-1">{{ runtime.tagline || runtime.brandSlogan }}</p>
          </div>
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <button
            class="px-4 py-2 rounded-xl bg-rose-50 hover:bg-rose-100 text-rose-700 text-xs font-extrabold border border-rose-200 transition"
            @click="showReportModal = true"
          >
            프로젝트 신고
          </button>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
        <!-- 좌측: 팀원 모집 공고 & 팀원 RBAC/초대/승인 관리 (7col) -->
        <div class="lg:col-span-7 space-y-6">
          <!-- 모집 중인 포지션 -->
          <div class="kkumz-card p-6 space-y-4">
            <div class="flex items-center justify-between">
              <div>
                <h2 class="text-lg font-extrabold text-slate-900">오픈된 팀원 모집 포지션</h2>
                <p class="text-xs text-slate-500">
                  함께 브랜드를 성장시킬 디자이너·개발자·마케터·공동창업자를 모집합니다.
                </p>
              </div>
              <button
                class="px-3.5 py-1.5 rounded-xl bg-indigo-50 text-indigo-700 text-xs font-extrabold border border-indigo-200 hover:bg-indigo-100"
                @click="showNewRecruitForm = !showNewRecruitForm"
              >
                + 신규 모집 공고 등록
              </button>
            </div>

            <!-- 신규 모집 공고 등록 폼 -->
            <div
              v-if="showNewRecruitForm"
              class="p-4 rounded-2xl bg-indigo-50/60 border border-indigo-200 space-y-3"
            >
              <div class="grid grid-cols-1 md:grid-cols-3 gap-2.5">
                <select v-model="newRecruit.positionRole" class="kkumz-input text-xs py-2 bg-white">
                  <option value="FRONTEND">프론트엔드 개발</option>
                  <option value="BACKEND">백엔드 개발</option>
                  <option value="DESIGNER">브랜드·프로덕트 디자이너</option>
                  <option value="MARKETER">그로스 마케터</option>
                  <option value="MD">커머스 상품 MD</option>
                  <option value="PLANNER">서비스 기획·PM</option>
                  <option value="CO_FOUNDER">공동창업자</option>
                </select>
                <input
                  v-model="newRecruit.title"
                  type="text"
                  placeholder="공고 제목 입력"
                  class="kkumz-input text-xs py-2 bg-white md:col-span-2"
                />
              </div>
              <textarea
                v-model="newRecruit.description"
                rows="2"
                placeholder="담당 역할 및 협업 방식 설명"
                class="kkumz-input text-xs py-2 bg-white"
              ></textarea>
              <div class="flex items-center justify-between gap-2">
                <input
                  v-model="newRecruit.skillsText"
                  type="text"
                  placeholder="요구 스킬 쉼표 구분 입력"
                  class="kkumz-input text-xs py-1.5 bg-white flex-1"
                />
                <button class="kkumz-btn-primary text-xs py-2 shrink-0" @click="handleCreateRecruit">
                  공고 게시
                </button>
              </div>
            </div>

            <div class="space-y-3">
              <div
                v-for="rec in recruits"
                :key="rec.id"
                class="p-4 rounded-2xl border border-slate-200 hover:border-indigo-300 transition bg-slate-50/50 space-y-2.5"
              >
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <span class="kkumz-badge bg-indigo-600 text-white">
                      {{ formatPositionRoleLabel(rec.positionRole) }}
                    </span>
                    <span class="text-sm font-extrabold text-slate-900">{{ rec.title }}</span>
                  </div>
                  <span
                    class="kkumz-badge"
                    :class="
                      rec.status === 'OPEN'
                        ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                        : 'bg-slate-200 text-slate-600'
                    "
                  >
                    {{ rec.status === 'OPEN' ? '모집중' : '마감' }} ({{ rec.recruitedCount ?? rec.currentCount ?? 0 }}/{{ rec.headcount }}명)
                  </span>
                </div>
                <p class="text-xs text-slate-600 leading-relaxed">{{ rec.description }}</p>
                <div class="flex flex-wrap items-center justify-between gap-2 pt-1">
                  <div class="flex flex-wrap gap-1.5">
                    <span
                      v-for="sk in rec.requiredSkills || []"
                      :key="sk"
                      class="px-2 py-0.5 rounded-md bg-white border border-slate-200 text-[11px] font-semibold text-slate-600"
                    >
                      {{ sk }}
                    </span>
                  </div>
                  <button
                    class="px-3.5 py-1.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-extrabold transition"
                    @click="openApplyModal(rec)"
                  >
                    이 포지션 지원하기
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 팀원 목록 및 지원자 승인 / 멤버 초대 관리 -->
          <div class="kkumz-card p-6 space-y-4">
            <div class="flex items-center justify-between">
              <div>
                <h2 class="text-lg font-extrabold text-slate-900">팀 멤버 & 지원자 승인 관리</h2>
                <p class="text-xs text-slate-500">
                  합류한 팀원의 권한(오너 / 에디터 / 뷰어)을 설정하거나 지원자를 승인합니다.
                </p>
              </div>
              <button
                class="px-3.5 py-1.5 rounded-xl bg-slate-900 text-white text-xs font-extrabold"
                @click="showInviteForm = !showInviteForm"
              >
                + 팀원 직접 초대
              </button>
            </div>

            <!-- 팀원 직접 초대 폼 -->
            <div
              v-if="showInviteForm"
              class="p-4 rounded-2xl bg-slate-50 border border-slate-200 grid grid-cols-1 md:grid-cols-4 gap-2"
            >
              <input
                v-model="inviteForm.username"
                type="text"
                placeholder="초대할 아이디"
                class="kkumz-input text-xs py-2 bg-white"
              />
              <input
                v-model="inviteForm.displayName"
                type="text"
                placeholder="이름 / 닉네임"
                class="kkumz-input text-xs py-2 bg-white"
              />
              <select v-model="inviteForm.memberRole" class="kkumz-input text-xs py-2 bg-white">
                <option value="EDITOR">에디터 (공동 편집)</option>
                <option value="VIEWER">뷰어 (열람 전용)</option>
              </select>
              <button class="kkumz-btn-primary text-xs py-2" @click="handleInviteMember">초대 발송</button>
            </div>

            <div class="divide-y divide-slate-100">
              <div
                v-for="m in members"
                :key="m.id"
                class="py-3.5 flex flex-wrap items-center justify-between gap-3"
              >
                <div class="flex items-center gap-3">
                  <div
                    class="w-9 h-9 rounded-full bg-indigo-100 text-indigo-700 font-extrabold text-xs flex items-center justify-center"
                  >
                    {{ (m.displayName || m.username || 'U').slice(0, 1) }}
                  </div>
                  <div>
                    <div class="flex items-center gap-2">
                      <span class="text-sm font-extrabold text-slate-900">{{ m.displayName }}</span>
                      <span class="text-xs text-slate-400">@{{ m.username }}</span>
                      <span class="kkumz-badge bg-slate-100 text-slate-700">
                        {{ formatMemberRoleLabel(m.memberRole) }}
                      </span>
                      <span class="kkumz-badge bg-indigo-50 text-indigo-700">
                        {{ formatPositionRoleLabel(m.positionRole) }}
                      </span>
                    </div>
                    <p v-if="m.applicationMessage" class="text-xs text-slate-500 mt-0.5">
                      "{{ m.applicationMessage }}"
                    </p>
                  </div>
                </div>

                <div class="flex items-center gap-2">
                  <span
                    class="kkumz-badge"
                    :class="{
                      'bg-emerald-50 text-emerald-700 border border-emerald-200': m.status === 'ACTIVE',
                      'bg-amber-50 text-amber-700 border border-amber-200':
                        m.status === 'APPLIED' || m.status === 'INVITED' || m.status === 'PENDING',
                      'bg-rose-50 text-rose-700': m.status === 'REJECTED',
                    }"
                  >
                    {{ formatMemberStatusLabel(m.status) }}
                  </span>

                  <template v-if="m.status === 'APPLIED' || m.status === 'INVITED' || m.status === 'PENDING'">
                    <button
                      class="px-3 py-1 rounded-lg bg-emerald-600 text-white text-xs font-bold hover:bg-emerald-500"
                      @click="handleMemberStatus(m.id, 'ACTIVE', 'EDITOR')"
                    >
                      승인(에디터)
                    </button>
                    <button
                      class="px-2.5 py-1 rounded-lg bg-slate-200 text-slate-700 text-xs font-bold hover:bg-slate-300"
                      @click="handleMemberStatus(m.id, 'REJECTED')"
                    >
                      거절
                    </button>
                  </template>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 우측: 브랜드 기획 요약 & 커뮤니티 피드백 (5col) -->
        <div class="lg:col-span-5 space-y-6">
          <div class="kkumz-card p-6 space-y-4">
            <h2 class="text-lg font-extrabold text-slate-900">서비스 기획 요약</h2>
            <pre
              class="p-4 rounded-2xl bg-slate-50 border border-slate-200 text-xs text-slate-700 whitespace-pre-wrap font-sans leading-relaxed max-h-60 overflow-y-auto"
            >{{ runtime.specMarkdown }}</pre>
          </div>

          <!-- 커뮤니티 피드백 & 응원 댓글 -->
          <div class="kkumz-card p-6 space-y-4">
            <h2 class="text-lg font-extrabold text-slate-900">
              커뮤니티 피드백 & 응원 ({{ comments.length }})
            </h2>

            <div class="space-y-2.5">
              <div class="grid grid-cols-2 gap-2">
                <input
                  v-model="newComment.displayName"
                  type="text"
                  placeholder="작성자 닉네임"
                  class="kkumz-input text-xs py-2"
                />
                <select v-model="newComment.authorRoleBadge" class="kkumz-input text-xs py-2 bg-white">
                  <option value="프로덕트 디자이너">프로덕트 디자이너</option>
                  <option value="풀스택 개발자">풀스택 개발자</option>
                  <option value="브랜드 마케터">브랜드 마케터</option>
                  <option value="예비 창업가">예비 창업가</option>
                </select>
              </div>
              <div class="flex gap-2">
                <input
                  v-model="newComment.content"
                  type="text"
                  placeholder="아이디어와 라이브 사이트에 대한 피드백을 남겨주세요."
                  class="kkumz-input text-xs py-2 flex-1"
                  @keyup.enter="handleAddComment"
                />
                <button class="kkumz-btn-primary text-xs py-2 shrink-0" @click="handleAddComment">
                  등록
                </button>
              </div>
            </div>

            <div class="space-y-3 max-h-96 overflow-y-auto pr-1">
              <div
                v-for="c in comments"
                :key="c.id"
                class="p-3.5 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-1.5"
              >
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <span class="text-xs font-extrabold text-slate-900">{{ c.displayName }}</span>
                    <span
                      v-if="c.authorRoleBadge"
                      class="px-2 py-0.5 rounded-full bg-indigo-50 text-indigo-700 text-[10px] font-bold"
                    >
                      {{ c.authorRoleBadge }}
                    </span>
                  </div>
                  <span class="text-[11px] text-slate-400">{{
                    (c.createdAt || '').slice(0, 16).replace('T', ' ')
                  }}</span>
                </div>
                <p class="text-xs text-slate-700 leading-relaxed">{{ c.content }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 포지션 지원 모달 -->
    <div
      v-if="applyingRecruit"
      class="fixed inset-0 z-50 bg-slate-950/60 backdrop-blur-sm flex items-center justify-center p-4"
    >
      <div class="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl space-y-4">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-extrabold text-slate-900">
            {{ formatPositionRoleLabel(applyingRecruit.positionRole) }} 포지션 지원
          </h3>
          <button class="text-slate-400 hover:text-slate-700 font-bold" @click="applyingRecruit = null">
            ✕
          </button>
        </div>
        <p class="text-xs text-slate-500">{{ applyingRecruit.title }}</p>
        <div class="space-y-3">
          <input
            v-model="applyForm.username"
            type="text"
            placeholder="지원자 아이디"
            class="kkumz-input text-xs"
          />
          <input
            v-model="applyForm.displayName"
            type="text"
            placeholder="이름 / 닉네임"
            class="kkumz-input text-xs"
          />
          <textarea
            v-model="applyForm.applicationMessage"
            rows="3"
            placeholder="간단한 자기소개 및 포트폴리오/협업 포부를 남겨주세요."
            class="kkumz-input text-xs"
          ></textarea>
        </div>
        <div class="flex justify-end gap-2 pt-2">
          <button class="kkumz-btn-secondary text-xs" @click="applyingRecruit = null">취소</button>
          <button class="kkumz-btn-primary text-xs" @click="submitTeamApplication">지원서 제출</button>
        </div>
      </div>
    </div>

    <!-- 모더레이션 신고 모달 -->
    <div
      v-if="showReportModal"
      class="fixed inset-0 z-50 bg-slate-950/60 backdrop-blur-sm flex items-center justify-center p-4"
    >
      <div class="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl space-y-4">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-extrabold text-slate-900">프로젝트 모더레이션 신고</h3>
          <button class="text-slate-400 hover:text-slate-700 font-bold" @click="showReportModal = false">
            ✕
          </button>
        </div>
        <div class="space-y-3">
          <input
            v-model="reportForm.reporterUsername"
            type="text"
            placeholder="신고자 아이디 입력"
            class="kkumz-input text-xs bg-white"
          />
          <select v-model="reportForm.reasonCategory" class="kkumz-input text-xs bg-white">
            <option value="SPAM">스팸 / 도배성 사이트</option>
            <option value="PHISHING">피싱 / 사기 의심 커머스</option>
            <option value="COPYRIGHT">상표권 / 디자인 도용</option>
            <option value="INAPPROPRIATE">부적절한 콘텐츠</option>
          </select>
          <textarea
            v-model="reportForm.reasonDetail"
            rows="3"
            placeholder="상세 신고 사유를 입력해주세요. 운영팀 모더레이션 큐로 즉시 접수됩니다."
            class="kkumz-input text-xs"
          ></textarea>
        </div>
        <div class="flex justify-end gap-2">
          <button class="kkumz-btn-secondary text-xs" @click="showReportModal = false">취소</button>
          <button
            class="px-4 py-2 rounded-xl bg-rose-600 text-white text-xs font-extrabold hover:bg-rose-500"
            @click="submitReport"
          >
            신고 접수
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  kkumzApi,
  formatSiteTypeLabel,
  formatDeployModeLabel,
  formatPositionRoleLabel,
} from '@/api/kkumzApi';

const route = useRoute();
const router = useRouter();

const activeProjects = ref<Array<{ slug: string; brandName: string }>>([]);

const currentSlug = computed(
  () => (route.params.siteSlug as string) || activeProjects.value[0]?.slug || '',
);

const loading = ref(false);
const runtime = ref<any>(null);
const recruits = ref<any[]>([]);
const members = ref<any[]>([]);
const comments = ref<any[]>([]);

const showNewRecruitForm = ref(false);
const newRecruit = ref({
  positionRole: 'FRONTEND',
  title: '',
  description: '',
  skillsText: '',
});

const showInviteForm = ref(false);
const inviteForm = ref({
  username: '',
  displayName: '',
  memberRole: 'EDITOR',
});

const applyingRecruit = ref<any>(null);
const applyForm = ref({
  username: '',
  displayName: '',
  applicationMessage: '',
});

const newComment = ref({
  username: '',
  displayName: '',
  authorRoleBadge: '프로덕트 디자이너',
  content: '',
});

const showReportModal = ref(false);
const reportForm = ref({
  reporterUsername: '',
  reasonCategory: 'SPAM',
  reasonDetail: '',
});

function openLiveSiteInNewTab(slug: string) {
  if (!slug) return;
  const url = router.resolve({ name: 'LiveSiteRendererView', params: { siteSlug: slug } }).href;
  window.open(url, '_blank', 'noopener,noreferrer');
}

function formatMemberRoleLabel(role?: string) {
  switch ((role || '').toUpperCase()) {
    case 'OWNER':
      return '오너(창업자)';
    case 'EDITOR':
      return '에디터(공동편집)';
    case 'VIEWER':
      return '뷰어';
    default:
      return role || '멤버';
  }
}

function formatMemberStatusLabel(status?: string) {
  switch ((status || '').toUpperCase()) {
    case 'ACTIVE':
      return '합류 완료';
    case 'APPLIED':
    case 'PENDING':
      return '지원 대기';
    case 'INVITED':
      return '초대 대기';
    case 'REJECTED':
      return '거절됨';
    default:
      return status || '';
  }
}

async function loadProjectDirectory() {
  const feed = await kkumzApi.getCommunityFeed({ sortBy: 'POPULAR' });
  activeProjects.value = (feed || []).map((item: any) => ({
    slug: item.siteSlug,
    brandName: item.siteName || item.brandName || item.siteSlug,
  }));
}

async function loadLoungeData() {
  if (!currentSlug.value) {
    await loadProjectDirectory();
  }
  if (!currentSlug.value) return;
  loading.value = true;
  try {
    const data = await kkumzApi.getSiteRuntime(currentSlug.value);
    const siteInfo = data.site || data;
    runtime.value = {
      ...siteInfo,
      ...data,
      siteId: siteInfo.id || data.siteId || siteInfo.siteId,
      brandName: siteInfo.siteName || siteInfo.brandName,
      tagline: siteInfo.brandSlogan || siteInfo.tagline,
    };
    recruits.value = data.recruits || [];
    members.value = data.teamMembers || [];
    comments.value = data.comments || [];
  } finally {
    loading.value = false;
  }
}

async function handleCreateRecruit() {
  if (!runtime.value || !newRecruit.value.title.trim()) return;
  const skills = newRecruit.value.skillsText
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean);
  await kkumzApi.createSiteRecruit(runtime.value.siteId, {
    positionRole: newRecruit.value.positionRole,
    title: newRecruit.value.title,
    description: newRecruit.value.description,
    requiredSkills: skills,
    headcount: 1,
  });
  recruits.value = await kkumzApi.listSiteRecruits(runtime.value.siteId);
  newRecruit.value.title = '';
  newRecruit.value.description = '';
  showNewRecruitForm.value = false;
}

function openApplyModal(rec: any) {
  applyingRecruit.value = rec;
  applyForm.value.applicationMessage = `${rec.title} 포지션에 합류하여 함께 브랜드를 성장시키고 싶습니다.`;
}

async function submitTeamApplication() {
  if (!runtime.value || !applyingRecruit.value) return;
  if (!applyForm.value.username.trim() || !applyForm.value.displayName.trim()) {
    alert('지원자 아이디와 이름을 입력해 주세요.');
    return;
  }
  members.value = await kkumzApi.applyToTeam(runtime.value.siteId, {
    recruitId: applyingRecruit.value.id,
    username: applyForm.value.username.trim(),
    displayName: applyForm.value.displayName.trim(),
    positionRole: applyingRecruit.value.positionRole,
    applicationMessage: applyForm.value.applicationMessage,
  });
  applyingRecruit.value = null;
}

async function handleInviteMember() {
  if (!runtime.value || !inviteForm.value.username.trim()) return;
  members.value = await kkumzApi.inviteTeamMember(runtime.value.siteId, {
    username: inviteForm.value.username,
    displayName: inviteForm.value.displayName || inviteForm.value.username,
    memberRole: inviteForm.value.memberRole,
    positionRole: 'FRONTEND',
    message: '팀 오너 직접 초대',
  });
  inviteForm.value.username = '';
  inviteForm.value.displayName = '';
  showInviteForm.value = false;
}

async function handleMemberStatus(memberId: string, status: string, memberRole?: string) {
  if (!runtime.value) return;
  members.value = await kkumzApi.updateTeamMemberStatus(runtime.value.siteId, memberId, {
    status,
    memberRole,
  });
  recruits.value = await kkumzApi.listSiteRecruits(runtime.value.siteId);
}

async function handleAddComment() {
  if (!runtime.value || !newComment.value.content.trim()) return;
  if (!newComment.value.displayName.trim()) {
    alert('작성자 닉네임을 입력해 주세요.');
    return;
  }
  comments.value = await kkumzApi.addSiteComment(runtime.value.siteId, {
    username: newComment.value.username.trim() || newComment.value.displayName.trim(),
    displayName: newComment.value.displayName.trim(),
    authorRoleBadge: newComment.value.authorRoleBadge,
    content: newComment.value.content,
  });
  newComment.value.content = '';
}

async function submitReport() {
  if (!runtime.value) return;
  if (!reportForm.value.reporterUsername.trim() || !reportForm.value.reasonDetail.trim()) {
    alert('신고자 아이디와 상세 사유를 입력해 주세요.');
    return;
  }
  await kkumzApi.reportContent(runtime.value.siteId, {
    targetType: 'SITE',
    targetId: runtime.value.siteId,
    reporterUsername: reportForm.value.reporterUsername.trim(),
    reasonCategory: reportForm.value.reasonCategory,
    reasonDetail: reportForm.value.reasonDetail.trim(),
  });
  showReportModal.value = false;
  reportForm.value.reasonDetail = '';
  alert('신고가 접수되었습니다. 운영팀 모더레이션 심사 후 조치됩니다.');
}

watch(
  () => route.params.siteSlug,
  () => {
    loadLoungeData();
  },
);

onMounted(async () => {
  await loadProjectDirectory();
  await loadLoungeData();
});
</script>
