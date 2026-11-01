<template>
  <PageWrapper title="꿈즈 Kkumz 운영 종합 대시보드" content="AI 브랜드 스튜디오 생성 현황, 멀티사이트 배포, WebIDE 빌드 성공률 및 커뮤니티 팀빌딩 지표를 실시간으로 관제합니다.">
    <!-- 1. 핵심 KPI 요약 카드 6종 -->
    <div class="grid grid-cols-1 md:grid-cols-3 xl:grid-cols-6 gap-4 mb-6">
      <Card size="small" :loading="loading" class="!rounded-xl shadow-xs">
        <div class="text-xs text-slate-500 font-medium">누적 아이디어 프로젝트</div>
        <div class="text-2xl font-extrabold text-indigo-600 mt-1">{{ summary.totalIdeas || 0 }}건</div>
        <div class="text-[11px] text-slate-400 mt-1">AI 스튜디오 기획서 기준</div>
      </Card>

      <Card size="small" :loading="loading" class="!rounded-xl shadow-xs">
        <div class="text-xs text-slate-500 font-medium">가동 중 멀티사이트</div>
        <div class="text-2xl font-extrabold text-emerald-600 mt-1">
          {{ summary.activeSites || 0 }} / {{ summary.totalSites || 0 }}개
        </div>
        <div class="text-[11px] text-slate-400 mt-1">쇼핑몰 · 브랜드웹 · 복합</div>
      </Card>

      <Card size="small" :loading="loading" class="!rounded-xl shadow-xs">
        <div class="text-xs text-slate-500 font-medium">WebIDE 빌드 성공률</div>
        <div class="text-2xl font-extrabold text-blue-600 mt-1">{{ summary.buildSuccessRate || 100 }}%</div>
        <div class="text-[11px] text-slate-400 mt-1">총 {{ summary.totalBuilds || 0 }}회 빌드 실행</div>
      </Card>

      <Card size="small" :loading="loading" class="!rounded-xl shadow-xs">
        <div class="text-xs text-slate-500 font-medium">오픈 팀빌딩 포지션</div>
        <div class="text-2xl font-extrabold text-violet-600 mt-1">{{ summary.openRecruits || 0 }}개</div>
        <div class="text-[11px] text-slate-400 mt-1">활성 합류 팀원 {{ summary.activeTeamMembers || 0 }}명</div>
      </Card>

      <Card size="small" :loading="loading" class="!rounded-xl shadow-xs">
        <div class="text-xs text-slate-500 font-medium">AI 프롬프트 누적 호출</div>
        <div class="text-2xl font-extrabold text-amber-600 mt-1">{{ (summary.totalAiPromptCalls || 0).toLocaleString() }}회</div>
        <div class="text-[11px] text-slate-400 mt-1">네이밍 · SVG 로고 · 기획서</div>
      </Card>

      <Card size="small" :loading="loading" class="!rounded-xl shadow-xs">
        <div class="text-xs text-slate-500 font-medium">미처리 모더레이션 신고</div>
        <div class="text-2xl font-extrabold mt-1" :class="summary.pendingModerationCount > 0 ? 'text-rose-600' : 'text-slate-700'">
          {{ summary.pendingModerationCount || 0 }}건
        </div>
        <div class="text-[11px] text-slate-400 mt-1">스팸 · 상표권 · 부적절 콘텐츠</div>
      </Card>
    </div>

    <!-- 2. 사이트 유형별 분포 & 최근 빌드 파이프라인 & 최근 모더레이션 현황 -->
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-4">
      <Card title="멀티사이트 유형별 분포" size="small" :loading="loading" class="lg:col-span-4 !rounded-xl shadow-xs">
        <div class="space-y-4 py-2">
          <div v-for="(cnt, key) in summary.siteTypeDistribution || {}" :key="key" class="space-y-1">
            <div class="flex items-center justify-between text-xs font-semibold">
              <span>{{ formatSiteType(String(key)) }}</span>
              <span class="text-indigo-600 font-bold">{{ cnt }}개 사이트</span>
            </div>
            <Progress :percent="calcPercent(Number(cnt), Number(summary.totalSites || 1))" :show-info="false" stroke-color="#4f46e5" />
          </div>
          <Divider class="!my-3" />
          <div class="flex flex-wrap gap-2">
            <a-button type="primary" size="small" @click="go('/studio/ideas')">아이디어 & 명세서 관제</a-button>
            <a-button size="small" @click="go('/deployment/sites')">멀티사이트 도메인 관제</a-button>
            <a-button size="small" @click="go('/community/moderation')">신고 모더레이션 심사</a-button>
          </div>
        </div>
      </Card>

      <Card title="최근 WebIDE 빌드 & 배포 파이프라인" size="small" :loading="loading" class="lg:col-span-4 !rounded-xl shadow-xs">
        <div class="divide-y divide-slate-100">
          <div v-for="job in summary.recentBuildJobs || []" :key="job.id" class="py-2.5 flex items-center justify-between gap-2">
            <div>
              <div class="flex items-center gap-1.5">
                <Tag :color="job.status === 'SUCCESS' ? 'green' : job.status === 'FAILED' ? 'red' : 'blue'">
                  {{ job.status }}
                </Tag>
                <span class="text-xs font-bold">{{ job.brandName || job.siteSlug }}</span>
                <span class="text-[11px] text-slate-400">v{{ job.versionNo }}</span>
              </div>
              <div class="text-[11px] text-slate-500 mt-0.5 truncate max-w-[260px]">
                {{ job.errorSummary || `${job.triggerType} · ${job.durationMs}ms` }}
              </div>
            </div>
            <span class="text-[11px] text-slate-400 shrink-0">{{ (job.createdAt || '').slice(5, 16).replace('T', ' ') }}</span>
          </div>
        </div>
      </Card>

      <Card title="최근 모더레이션 신고 접수 큐" size="small" :loading="loading" class="lg:col-span-4 !rounded-xl shadow-xs">
        <div class="divide-y divide-slate-100">
          <div v-for="rep in summary.recentReports || []" :key="rep.id" class="py-2.5 flex items-center justify-between gap-2">
            <div>
              <div class="flex items-center gap-1.5">
                <Tag :color="rep.status === 'PENDING' ? 'orange' : 'default'">
                  {{ rep.status === 'PENDING' ? '심사대기' : '처리완료' }}
                </Tag>
                <span class="text-xs font-bold">{{ rep.brandName || rep.targetId }}</span>
                <Tag color="purple">{{ rep.reasonCategory }}</Tag>
              </div>
              <div class="text-[11px] text-slate-500 mt-0.5 truncate max-w-[260px]">
                {{ rep.reasonDetail }}
              </div>
            </div>
            <a-button type="link" size="small" @click="go('/community/moderation')">심사 →</a-button>
          </div>
        </div>
      </Card>
    </div>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  import { Card, Tag, Progress, Divider } from 'ant-design-vue';
  import { PageWrapper } from '/@/components/Page';
  import { useGo } from '/@/hooks/web/usePage';
  import { getDashboardSummary } from '/@/api/kkumz';

  const go = useGo();
  const loading = ref(false);
  const summary = ref<Recordable>({});

  function formatSiteType(type: string) {
    switch (type) {
      case 'COMMERCE':
        return '쇼핑몰 스토어 (COMMERCE)';
      case 'CMS_SITE':
        return '브랜드·커뮤니티 웹 (CMS_SITE)';
      case 'HYBRID':
        return '매거진+스토어 복합 (HYBRID)';
      default:
        return type;
    }
  }

  function calcPercent(val: number, total: number) {
    if (!total) return 0;
    return Math.round((val * 100) / total);
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
