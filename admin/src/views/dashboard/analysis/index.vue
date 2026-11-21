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
            <div class="text-xs text-slate-500 font-semibold">{{ t('appUi.k81421d37') }}</div>
            <div class="w-7 h-7 rounded-lg bg-slate-100 text-slate-600 flex items-center justify-center">
              <Icon icon="ant-design:bulb-outlined" :size="16" />
            </div>
          </div>
          <div class="text-2xl font-extrabold text-slate-900 mt-1.5 font-mono tabular-nums">
            {{ summary.totalIdeas || 0 }}{{ t('appUi.kd202b4d7') }}
          </div>
          <div class="text-[11px] text-slate-500 mt-1 flex items-center justify-between">
            <span>{{ t('appUi.kf9344d9d') }} {{ summary.publishedIdeas || 0 }}{{ t('appUi.kd202b4d7') }}</span>
            <span class="text-slate-400">{{ t('appUi.kf57ae64f') }} {{ summary.specVersionCount || 0 }}{{ t('appUi.k593f8a81') }}</span>
          </div>
        </Card>

        <Card
          size="small"
          :loading="loading"
          class="!rounded-xl shadow-xs hover:shadow-md transition-shadow cursor-pointer border-slate-200/80"
          @click="go('/deployment/sites')"
        >
          <div class="flex items-start justify-between">
            <div class="text-xs text-slate-500 font-semibold">{{ t('appUi.k43010a10') }}</div>
            <div class="w-7 h-7 rounded-lg bg-slate-100 text-slate-600 flex items-center justify-center">
              <Icon icon="ant-design:global-outlined" :size="16" />
            </div>
          </div>
          <div class="text-2xl font-extrabold text-slate-900 mt-1.5 font-mono tabular-nums">
            {{ summary.activeSites || 0 }} / {{ summary.totalSites || 0 }}{{ t('appUi.k11600c9a') }}
          </div>
          <div class="text-[11px] text-slate-500 mt-1 flex items-center justify-between">
            <span>{{ t('appUi.k72d116b0') }} {{ (summary.totalViews || 0).toLocaleString() }}{{ t('appUi.k2fc05c02') }}</span>
            <span class="text-slate-400">{{ t('appUi.k224a2886') }} {{ (summary.totalLikes || 0).toLocaleString() }}{{ t('appUi.k11600c9a') }}</span>
          </div>
        </Card>

        <Card
          size="small"
          :loading="loading"
          class="!rounded-xl shadow-xs hover:shadow-md transition-shadow cursor-pointer border-slate-200/80"
          @click="go('/deployment/builds')"
        >
          <div class="flex items-start justify-between">
            <div class="text-xs text-slate-500 font-semibold">{{ t('appUi.k7f249cf1') }}</div>
            <div class="w-7 h-7 rounded-lg bg-slate-100 text-slate-600 flex items-center justify-center">
              <Icon icon="ant-design:code-outlined" :size="16" />
            </div>
          </div>
          <div class="text-2xl font-extrabold text-slate-900 mt-1.5 font-mono tabular-nums">
            {{ summary.buildSuccessRate ?? 100 }}%
          </div>
          <div class="text-[11px] text-slate-500 mt-1 flex items-center justify-between">
            <span>{{ t('appUi.k23b64411') }} {{ summary.successBuilds || 0 }} / {{ summary.totalBuilds || 0 }}{{ t('appUi.k2fc05c02') }}</span>
            <span class="text-slate-400">{{ t('appUi.k1380f27e') }} {{ summary.aiPatchedBuilds || 0 }}{{ t('appUi.kd202b4d7') }}</span>
          </div>
        </Card>

        <Card
          size="small"
          :loading="loading"
          class="!rounded-xl shadow-xs hover:shadow-md transition-shadow cursor-pointer border-slate-200/80"
          @click="go('/community/projects')"
        >
          <div class="flex items-start justify-between">
            <div class="text-xs text-slate-500 font-semibold">{{ t('appUi.ka2ca4690') }}</div>
            <div class="w-7 h-7 rounded-lg bg-slate-100 text-slate-600 flex items-center justify-center">
              <Icon icon="ant-design:team-outlined" :size="16" />
            </div>
          </div>
          <div class="text-2xl font-extrabold text-slate-900 mt-1.5 font-mono tabular-nums">
            {{ summary.openRecruits || 0 }}{{ t('appUi.k11600c9a') }}
          </div>
          <div class="text-[11px] text-slate-500 mt-1 flex items-center justify-between">
            <span>{{ t('appUi.kf1886487') }} {{ summary.activeMembers || 0 }}{{ t('appUi.k5a62fd50') }}</span>
            <span class="text-slate-400">{{ t('appUi.k8155dc46') }} {{ summary.pendingApplicants || 0 }}{{ t('appUi.k5a62fd50') }}</span>
          </div>
        </Card>

        <Card
          size="small"
          :loading="loading"
          class="!rounded-xl shadow-xs hover:shadow-md transition-shadow cursor-pointer border-slate-200/80"
          @click="go('/studio/prompts')"
        >
          <div class="flex items-start justify-between">
            <div class="text-xs text-slate-500 font-semibold">{{ t('appUi.kdcdfb154') }}</div>
            <div class="w-7 h-7 rounded-lg bg-slate-100 text-slate-600 flex items-center justify-center">
              <Icon icon="ant-design:thunderbolt-outlined" :size="16" />
            </div>
          </div>
          <div class="text-2xl font-extrabold text-slate-900 mt-1.5 font-mono tabular-nums">
            {{ (summary.totalAiPromptCalls || 0).toLocaleString() }}{{ t('appUi.kd202b4d7') }}
          </div>
          <div class="text-[11px] text-slate-500 mt-1 flex items-center justify-between">
            <span>{{ t('appUi.kc03dab82') }} {{ summary.namingCount || 0 }} {{ t('appUi.k02d9bd51') }} {{ summary.logoCount || 0 }}</span>
            <span class="text-slate-400">{{ t('appUi.k86f31e80') }} {{ summary.screenBlueprintCount || 0 }}</span>
          </div>
        </Card>

        <Card
          size="small"
          :loading="loading"
          class="!rounded-xl shadow-xs hover:shadow-md transition-shadow cursor-pointer border-slate-200/80"
          @click="go('/community/moderation')"
        >
          <div class="flex items-start justify-between">
            <div class="text-xs text-slate-500 font-semibold">{{ t('appUi.k8bc10015') }}</div>
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
            {{ summary.pendingReports || 0 }}{{ t('appUi.kd202b4d7') }}
          </div>
          <div class="text-[11px] text-slate-500 mt-1 flex items-center justify-between">
            <span>{{ t('appUi.k620e6a6f') }} {{ summary.totalReports || 0 }}{{ t('appUi.kd202b4d7') }}</span>
            <span :class="(summary.pendingReports || 0) > 0 ? 'text-rose-500 font-semibold' : 'text-slate-400'">
              {{ (summary.pendingReports || 0) > 0 ? t('appUi.k348477d0') : t('appUi.k98b3a353') }}
            </span>
          </div>
        </Card>
      </div>

      <!-- 2. 사이트 유형별 분포 & 최근 빌드 파이프라인 & 최근 모더레이션 신고 큐 -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-4">
        <Card :title="t('appUi.k5296b333')" size="small" :loading="loading" class="lg:col-span-4 !rounded-xl shadow-xs">
          <div class="space-y-3.5 py-1">
            <div v-for="item in siteTypeItems" :key="item.siteType" class="space-y-1">
              <div class="flex items-center justify-between text-xs">
                <div class="flex items-center gap-1.5">
                  <span class="w-2 h-2 rounded-full" :style="{ backgroundColor: getSiteTypeColor(item.siteType) }"></span>
                  <span class="font-semibold text-slate-700">{{ formatSiteType(item.siteType) }}</span>
                </div>
                <div class="flex items-center gap-2">
                  <span class="text-[11px] text-slate-400 font-mono">
                    {{ t('appUi.k72d116b0') }} {{ (item.totalViews || 0).toLocaleString() }}
                  </span>
                  <span class="text-slate-700 font-bold font-mono tabular-nums">
                    {{ item.count }}{{ t('appUi.k6b89e930') }}{{ calcPercent(item.count, Number(summary.totalSites || 1)) }}%)
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
              {{ t('appUi.kedeee123') }}
            </div>

            <div v-if="deployModeItems.length" class="pt-2 border-t border-slate-100">
              <div class="text-[11px] font-semibold text-slate-500 mb-2">{{ t('appUi.ke66f43af') }}</div>
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
                <div
                  v-for="mode in deployModeItems"
                  :key="mode.deployMode"
                  class="px-2.5 py-2 rounded-lg bg-slate-50 border border-slate-200/70 flex items-center justify-between"
                >
                  <span class="text-xs font-medium text-slate-700">{{ formatDeployMode(mode.deployMode) }}</span>
                  <span class="text-xs font-bold font-mono text-slate-900">{{ mode.count }}{{ t('appUi.k11600c9a') }}</span>
                </div>
              </div>
            </div>

            <Divider class="!my-2.5" />
            <div class="flex flex-wrap gap-1.5">
              <a-button type="primary" size="small" @click="go('/studio/ideas')">{{ t('appUi.kdf4dd5ce') }}</a-button>
              <a-button size="small" @click="go('/deployment/sites')">{{ t('appUi.k924f4f05') }}</a-button>
              <a-button size="small" @click="go('/deployment/builds')">{{ t('appUi.k811cbeae') }}</a-button>
              <a-button size="small" @click="go('/community/moderation')">{{ t('appUi.k552ec5a5') }}</a-button>
            </div>
          </div>
        </Card>

        <Card :title="t('appUi.k60692508')" size="small" :loading="loading" class="lg:col-span-4 !rounded-xl shadow-xs">
          <template #extra>
            <a-button type="link" size="small" class="!px-0" @click="go('/deployment/builds')">{{ t('appUi.k00d809f7') }}</a-button>
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
                  <Tag v-if="job.aiPatchApplied" color="default" class="!m-0 !text-[10px]">{{ t('appUi.k1380f27e') }}</Tag>
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
            {{ t('appUi.k90cc4e33') }}
          </div>
        </Card>

        <Card :title="t('appUi.ke10285f4')" size="small" :loading="loading" class="lg:col-span-4 !rounded-xl shadow-xs">
          <template #extra>
            <a-button type="link" size="small" class="!px-0" @click="go('/community/moderation')">{{ t('appUi.kdee3973d') }}</a-button>
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
                  <span>{{ t('appUi.k786f654d') }} {{ formatTargetType(rep.targetType) }}</span>
                  <span>·</span>
                  <span>{{ t('appUi.k0b47f7d6') }}{{ rep.reporterUsername }}</span>
                </div>
              </div>
              <a-button type="link" size="small" class="shrink-0 !px-1" @click="go('/community/moderation')">
                {{ t('appUi.k7e8360b9') }}
              </a-button>
            </div>
          </div>
          <div v-else class="py-10 text-center text-xs text-slate-400">
            {{ t('appUi.kafcf5f65') }}
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
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

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
        return t('appUi.kfe990b2f');
      case 'CMS_SITE':
        return t('appUi.k59d1dd90');
      case 'HYBRID':
        return t('appUi.k28fc2433');
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
        return t('appUi.k8bb944fb');
      case 'DYNAMIC_SDUI':
        return t('appUi.k2f258cb1');
      default:
        return mode || t('appUi.k2f258cb1');
    }
  }

  function formatBuildStatus(status: string) {
    switch (status) {
      case 'SUCCESS':
        return t('appUi.k23b64411');
      case 'FAILED':
        return t('appUi.k732fe33a');
      default:
        return t('appUi.k0dae9079');
    }
  }

  function formatJobType(jobType: string) {
    switch (jobType) {
      case 'WEBIDE_BUNDLE_BUILD':
        return t('appUi.k0778ffe0');
      case 'CHAT_HOT_RELOAD':
        return t('appUi.k1b709fa6');
      case 'INITIAL_PROVISION':
        return t('appUi.kb2ef704f');
      default:
        return jobType || t('appUi.kf56f8ff2');
    }
  }

  function formatReportStatus(status: string) {
    switch (status) {
      case 'PENDING':
        return t('appUi.k5270f81b');
      case 'RESOLVED':
        return t('appUi.k18dd1c90');
      case 'DISMISSED':
        return t('appUi.k804c263a');
      default:
        return status;
    }
  }

  function formatReasonCategory(category: string) {
    switch (category) {
      case 'SPAM_PROMOTION':
        return t('appUi.k412f8054');
      case 'INFO_VERIFICATION':
        return t('appUi.kf949e62a');
      case 'COPYRIGHT_TRADEMARK':
        return t('appUi.ka5de8df6');
      default:
        return category;
    }
  }

  function formatTargetType(targetType: string) {
    switch (targetType) {
      case 'COMMENT':
        return t('appUi.k1cd70086');
      case 'RECRUIT':
        return t('appUi.kf5dfb8d0');
      case 'SITE':
        return t('appUi.kd57149b9');
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
