<template>
  <PageWrapper dense contentFullHeight :contentBackground="false">
    <div class="space-y-4">
      <!-- 1. 핵심 KPI 요약 카드 6종 -->
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-6 gap-3.5">
        <Card
          size="small"
          :loading="loading"
          class="!rounded-xl shadow-xs hover:shadow-md transition-shadow cursor-pointer border-slate-200/80"
          @click="go('/studio/ideas')"
        >
          <div class="flex items-start justify-between">
            <div class="text-xs text-slate-500 font-semibold">누적 아이디어 프로젝트</div>
            <div class="w-7 h-7 rounded-lg bg-slate-100 text-slate-600 flex items-center justify-center">
              <Icon icon="ant-design:bulb-outlined" :size="16" />
            </div>
          </div>
          <div class="text-2xl font-extrabold text-slate-900 mt-1.5 font-mono tabular-nums">
            {{ summary.totalIdeas || 0 }}건
          </div>
          <div class="text-[11px] text-slate-500 mt-1 flex items-center justify-between">
            <span>배포 전환 {{ summary.publishedIdeas || 0 }}건</span>
            <span class="text-slate-400">기획서 {{ summary.specVersionCount || 0 }}버전</span>
          </div>
        </Card>

        <Card
          size="small"
          :loading="loading"
          class="!rounded-xl shadow-xs hover:shadow-md transition-shadow cursor-pointer border-slate-200/80"
          @click="go('/deployment/sites')"
        >
          <div class="flex items-start justify-between">
            <div class="text-xs text-slate-500 font-semibold">가동 중 멀티사이트</div>
            <div class="w-7 h-7 rounded-lg bg-slate-100 text-slate-600 flex items-center justify-center">
              <Icon icon="ant-design:global-outlined" :size="16" />
            </div>
          </div>
          <div class="text-2xl font-extrabold text-slate-900 mt-1.5 font-mono tabular-nums">
            {{ summary.activeSites || 0 }} / {{ summary.totalSites || 0 }}개
          </div>
          <div class="text-[11px] text-slate-500 mt-1 flex items-center justify-between">
            <span>조회 {{ (summary.totalViews || 0).toLocaleString() }}회</span>
            <span class="text-slate-400">좋아요 {{ (summary.totalLikes || 0).toLocaleString() }}개</span>
          </div>
        </Card>

        <Card
          size="small"
          :loading="loading"
          class="!rounded-xl shadow-xs hover:shadow-md transition-shadow cursor-pointer border-slate-200/80"
          @click="go('/deployment/builds')"
        >
          <div class="flex items-start justify-between">
            <div class="text-xs text-slate-500 font-semibold">샌드박스 빌드 성공률</div>
            <div class="w-7 h-7 rounded-lg bg-slate-100 text-slate-600 flex items-center justify-center">
              <Icon icon="ant-design:code-outlined" :size="16" />
            </div>
          </div>
          <div class="text-2xl font-extrabold text-slate-900 mt-1.5 font-mono tabular-nums">
            {{ summary.buildSuccessRate ?? 100 }}%
          </div>
          <div class="text-[11px] text-slate-500 mt-1 flex items-center justify-between">
            <span>성공 {{ summary.successBuilds || 0 }} / {{ summary.totalBuilds || 0 }}회</span>
            <span class="text-slate-400">자동 패치 {{ summary.aiPatchedBuilds || 0 }}건</span>
          </div>
        </Card>

        <Card
          size="small"
          :loading="loading"
          class="!rounded-xl shadow-xs hover:shadow-md transition-shadow cursor-pointer border-slate-200/80"
          @click="go('/community/projects')"
        >
          <div class="flex items-start justify-between">
            <div class="text-xs text-slate-500 font-semibold">오픈 팀빌딩 포지션</div>
            <div class="w-7 h-7 rounded-lg bg-slate-100 text-slate-600 flex items-center justify-center">
              <Icon icon="ant-design:team-outlined" :size="16" />
            </div>
          </div>
          <div class="text-2xl font-extrabold text-slate-900 mt-1.5 font-mono tabular-nums">
            {{ summary.openRecruits || 0 }}개
          </div>
          <div class="text-[11px] text-slate-500 mt-1 flex items-center justify-between">
            <span>합류 팀원 {{ summary.activeMembers || 0 }}명</span>
            <span class="text-slate-400">지원 대기 {{ summary.pendingApplicants || 0 }}명</span>
          </div>
        </Card>

        <Card
          size="small"
          :loading="loading"
          class="!rounded-xl shadow-xs hover:shadow-md transition-shadow cursor-pointer border-slate-200/80"
          @click="go('/studio/prompts')"
        >
          <div class="flex items-start justify-between">
            <div class="text-xs text-slate-500 font-semibold">스튜디오 생성 산출물</div>
            <div class="w-7 h-7 rounded-lg bg-slate-100 text-slate-600 flex items-center justify-center">
              <Icon icon="ant-design:thunderbolt-outlined" :size="16" />
            </div>
          </div>
          <div class="text-2xl font-extrabold text-slate-900 mt-1.5 font-mono tabular-nums">
            {{ (summary.totalAiPromptCalls || 0).toLocaleString() }}건
          </div>
          <div class="text-[11px] text-slate-500 mt-1 flex items-center justify-between">
            <span>네이밍 {{ summary.namingCount || 0 }} · 로고 {{ summary.logoCount || 0 }}</span>
            <span class="text-slate-400">화면설계 {{ summary.screenBlueprintCount || 0 }}</span>
          </div>
        </Card>

        <Card
          size="small"
          :loading="loading"
          class="!rounded-xl shadow-xs hover:shadow-md transition-shadow cursor-pointer border-slate-200/80"
          @click="go('/community/moderation')"
        >
          <div class="flex items-start justify-between">
            <div class="text-xs text-slate-500 font-semibold">미처리 모더레이션 신고</div>
            <div
              class="w-7 h-7 rounded-lg flex items-center justify-center"
              :class="(summary.pendingReports || 0) > 0 ? 'bg-rose-50 text-rose-600' : 'bg-slate-100 text-slate-500'"
            >
              <Icon icon="ant-design:safety-certificate-outlined" :size="16" />
            </div>
          </div>
          <div
            class="text-2xl font-extrabold mt-1.5 font-mono tabular-nums"
            :class="(summary.pendingReports || 0) > 0 ? 'text-rose-600' : 'text-slate-700'"
          >
            {{ summary.pendingReports || 0 }}건
          </div>
          <div class="text-[11px] text-slate-500 mt-1 flex items-center justify-between">
            <span>전체 접수 {{ summary.totalReports || 0 }}건</span>
            <span :class="(summary.pendingReports || 0) > 0 ? 'text-rose-500 font-semibold' : 'text-slate-400'">
              {{ (summary.pendingReports || 0) > 0 ? '즉시 심사 필요' : '심사 완료' }}
            </span>
          </div>
        </Card>
      </div>

      <!-- 2. 사이트 유형별 분포 & 최근 빌드 파이프라인 & 최근 모더레이션 신고 큐 -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-4">
        <Card title="멀티사이트 유형 및 배포 분포" size="small" :loading="loading" class="lg:col-span-4 !rounded-xl shadow-xs">
          <div class="space-y-3.5 py-1">
            <div v-for="item in siteTypeItems" :key="item.siteType" class="space-y-1">
              <div class="flex items-center justify-between text-xs">
                <div class="flex items-center gap-1.5">
                  <span class="w-2 h-2 rounded-full" :style="{ backgroundColor: getSiteTypeColor(item.siteType) }"></span>
                  <span class="font-semibold text-slate-700">{{ formatSiteType(item.siteType) }}</span>
                </div>
                <div class="flex items-center gap-2">
                  <span class="text-[11px] text-slate-400 font-mono">
                    조회 {{ (item.totalViews || 0).toLocaleString() }}
                  </span>
                  <span class="text-slate-700 font-bold font-mono tabular-nums">
                    {{ item.count }}개 ({{ calcPercent(item.count, Number(summary.totalSites || 1)) }}%)
                  </span>
                </div>
              </div>
              <Progress
                :percent="calcPercent(item.count, Number(summary.totalSites || 1))"
                :show-info="false"
                :stroke-color="getSiteTypeColor(item.siteType)"
                size="small"
              />
            </div>

            <div v-if="!siteTypeItems.length" class="py-6 text-center text-xs text-slate-400">
              배포된 멀티사이트가 없습니다.
            </div>

            <div v-if="deployModeItems.length" class="pt-2 border-t border-slate-100">
              <div class="text-[11px] font-semibold text-slate-500 mb-2">배포 아키텍처 트랙 현황</div>
              <div class="grid grid-cols-2 gap-2">
                <div
                  v-for="mode in deployModeItems"
                  :key="mode.deployMode"
                  class="px-2.5 py-2 rounded-lg bg-slate-50 border border-slate-200/70 flex items-center justify-between"
                >
                  <span class="text-xs font-medium text-slate-700">{{ formatDeployMode(mode.deployMode) }}</span>
                  <span class="text-xs font-bold font-mono text-slate-900">{{ mode.count }}개</span>
                </div>
              </div>
            </div>

            <Divider class="!my-2.5" />
            <div class="flex flex-wrap gap-1.5">
              <a-button type="primary" size="small" @click="go('/studio/ideas')">아이디어 관제</a-button>
              <a-button size="small" @click="go('/deployment/sites')">멀티사이트 관제</a-button>
              <a-button size="small" @click="go('/deployment/builds')">빌드 파이프라인</a-button>
              <a-button size="small" @click="go('/community/moderation')">모더레이션 심사</a-button>
            </div>
          </div>
        </Card>

        <Card title="최근 WebIDE 빌드 & 배포 파이프라인" size="small" :loading="loading" class="lg:col-span-4 !rounded-xl shadow-xs">
          <template #extra>
            <a-button type="link" size="small" class="!px-0" @click="go('/deployment/builds')">전체 이력</a-button>
          </template>
          <div v-if="buildItems.length" class="divide-y divide-slate-100">
            <div
              v-for="job in buildItems"
              :key="job.id"
              class="py-2.5 flex items-center justify-between gap-2 hover:bg-slate-50/70 px-1 rounded transition-colors"
            >
              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-1.5 flex-wrap">
                  <Tag :color="job.status === 'SUCCESS' ? 'green' : job.status === 'FAILED' ? 'red' : 'blue'" class="!m-0">
                    {{ formatBuildStatus(job.status) }}
                  </Tag>
                  <span class="text-xs font-bold text-slate-800 truncate">{{ job.brandName || job.siteName || job.siteSlug }}</span>
                  <span class="text-[11px] font-mono text-slate-700 font-semibold">v{{ job.versionNo || 1 }}</span>
                  <Tag v-if="job.aiPatchApplied" color="default" class="!m-0 !text-[10px]">자동 패치</Tag>
                </div>
                <div class="text-[11px] text-slate-500 mt-1 flex items-center gap-1.5 truncate">
                  <span>{{ formatJobType(job.jobType || job.triggerType) }}</span>
                  <span>·</span>
                  <span>{{ formatDeployMode(job.deployMode) }}</span>
                  <span>·</span>
                  <span class="font-mono">{{ (job.durationMs || 0).toLocaleString() }}ms</span>
                </div>
              </div>
              <div class="text-right shrink-0">
                <div class="text-[11px] font-mono text-slate-400">{{ formatShortTime(job.startedAt || job.createdAt) }}</div>
                <div class="text-[10px] text-slate-400 mt-0.5">@{{ job.triggeredBy || 'admin' }}</div>
              </div>
            </div>
          </div>
          <div v-else class="py-10 text-center text-xs text-slate-400">
            빌드 실행 이력이 없습니다.
          </div>
        </Card>

        <Card title="최근 모더레이션 신고 접수 큐" size="small" :loading="loading" class="lg:col-span-4 !rounded-xl shadow-xs">
          <template #extra>
            <a-button type="link" size="small" class="!px-0" @click="go('/community/moderation')">심사 큐 이동</a-button>
          </template>
          <div v-if="reportItems.length" class="divide-y divide-slate-100">
            <div
              v-for="rep in reportItems"
              :key="rep.id"
              class="py-2.5 flex items-center justify-between gap-2 hover:bg-slate-50/70 px-1 rounded transition-colors"
            >
              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-1.5 flex-wrap">
                  <Tag
                    :color="rep.status === 'PENDING' ? 'orange' : rep.status === 'RESOLVED' ? 'green' : 'default'"
                    class="!m-0"
                  >
                    {{ formatReportStatus(rep.status) }}
                  </Tag>
                  <span class="text-xs font-bold text-slate-800 truncate">{{ rep.brandName || rep.siteName || rep.targetId }}</span>
                  <Tag color="default" class="!m-0">{{ formatReasonCategory(rep.reasonCategory) }}</Tag>
                </div>
                <div class="text-[11px] text-slate-500 mt-1 truncate">
                  {{ rep.reasonDetail }}
                </div>
                <div class="text-[10px] text-slate-400 mt-0.5 flex items-center gap-2">
                  <span>신고 대상: {{ formatTargetType(rep.targetType) }}</span>
                  <span>·</span>
                  <span>신고자: @{{ rep.reporterUsername }}</span>
                </div>
              </div>
              <a-button type="link" size="small" class="shrink-0 !px-1" @click="go('/community/moderation')">
                심사
              </a-button>
            </div>
          </div>
          <div v-else class="py-10 text-center text-xs text-slate-400">
            접수된 신고 내역이 없습니다.
          </div>
        </Card>
      </div>
    </div>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, computed, onMounted } from 'vue';
  import { Card, Tag, Progress, Divider } from 'ant-design-vue';
  import { PageWrapper } from '/@/components/Page';
  import { Icon } from '/@/components/Icon';
  import { useGo } from '/@/hooks/web/usePage';
  import { getDashboardSummary } from '/@/api/kkumz';

  const go = useGo();
  const loading = ref(false);
  const summary = ref<Recordable>({});

  const siteTypeItems = computed(() => {
    const raw = summary.value?.siteTypeDistribution;
    if (Array.isArray(raw)) {
      return raw.map((item) => ({
        siteType: String(item.siteType || ''),
        count: Number(item.count || 0),
        totalViews: Number(item.totalViews || 0),
        totalLikes: Number(item.totalLikes || 0),
      }));
    }
    if (raw && typeof raw === 'object') {
      return Object.entries(raw).map(([key, val]) => ({
        siteType: String(key),
        count: Number(val || 0),
        totalViews: 0,
        totalLikes: 0,
      }));
    }
    return [];
  });

  const deployModeItems = computed(() => {
    const raw = summary.value?.deployModeDistribution;
    if (Array.isArray(raw)) {
      return raw.map((item) => ({
        deployMode: String(item.deployMode || ''),
        count: Number(item.count || 0),
      }));
    }
    return [];
  });

  const buildItems = computed(() => {
    const list = summary.value?.recentBuilds || summary.value?.recentBuildJobs;
    return Array.isArray(list) ? list : [];
  });

  const reportItems = computed(() => {
    const list = summary.value?.recentReports;
    return Array.isArray(list) ? list : [];
  });

  function formatSiteType(type: string) {
    switch (type) {
      case 'COMMERCE':
        return '쇼핑몰 스토어';
      case 'CMS_SITE':
        return '브랜드·커뮤니티 웹';
      case 'HYBRID':
        return '매거진·스토어 복합';
      default:
        return type;
    }
  }

  function getSiteTypeColor(type: string) {
    switch (type) {
      case 'COMMERCE':
        return '#0284c7';
      case 'CMS_SITE':
        return '#0d9488';
      case 'HYBRID':
        return '#475569';
      default:
        return '#64748b';
    }
  }

  function formatDeployMode(mode: string) {
    switch (mode) {
      case 'CUSTOM_BUNDLE':
        return '코드 번들 배포';
      case 'DYNAMIC_SDUI':
        return '즉시 디자인 배포';
      default:
        return mode || '즉시 디자인 배포';
    }
  }

  function formatBuildStatus(status: string) {
    switch (status) {
      case 'SUCCESS':
        return '성공';
      case 'FAILED':
        return '실패';
      default:
        return '진행중';
    }
  }

  function formatJobType(jobType: string) {
    switch (jobType) {
      case 'WEBIDE_BUNDLE_BUILD':
        return '웹 IDE 번들 빌드';
      case 'CHAT_HOT_RELOAD':
        return '대화형 즉시 반영';
      case 'INITIAL_PROVISION':
        return '초기 프로비저닝';
      default:
        return jobType || '배포 빌드';
    }
  }

  function formatReportStatus(status: string) {
    switch (status) {
      case 'PENDING':
        return '심사 대기';
      case 'RESOLVED':
        return '조치 완료';
      case 'DISMISSED':
        return '기각';
      default:
        return status;
    }
  }

  function formatReasonCategory(category: string) {
    switch (category) {
      case 'SPAM_PROMOTION':
        return '스팸 홍보';
      case 'INFO_VERIFICATION':
        return '정보 확인 요청';
      case 'COPYRIGHT_TRADEMARK':
        return '저작권·상표권';
      default:
        return category;
    }
  }

  function formatTargetType(targetType: string) {
    switch (targetType) {
      case 'COMMENT':
        return '피드 댓글';
      case 'RECRUIT':
        return '팀원 모집공고';
      case 'SITE':
        return '멀티사이트';
      default:
        return targetType;
    }
  }

  function formatShortTime(raw?: string) {
    if (!raw) return '-';
    return String(raw).slice(5, 16).replace('T', ' ');
  }

  function calcPercent(val: number, total: number) {
    if (!total) return 0;
    return Math.round((Number(val || 0) * 100) / total);
  }

  async function loadSummary() {
    loading.value = true;
    try {
      summary.value = (await getDashboardSummary()) || {};
    } finally {
      loading.value = false;
    }
  }

  onMounted(() => {
    loadSummary();
  });
</script>
