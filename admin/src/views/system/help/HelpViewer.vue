<template>
  <PageWrapper title="시스템 도움말 및 운영 가이드">
    <div class="help-viewer-layout">
      <!-- 좌측: 카테고리 및 화면별 가이드 네비게이션 -->
      <aside class="help-sidebar">
        <div class="sidebar-header">
          <div class="sidebar-title">화면별 매뉴얼 탐색기</div>
          <a-input-search
            v-model:value="searchKeyword"
            placeholder="화면명, 경로, 키워드 검색"
            allow-clear
          />
        </div>

        <div class="sidebar-menu-list">
          <div
            v-for="item in filteredGuides"
            :key="item.id || item.pageKey"
            class="guide-nav-item"
            :class="{ active: selectedGuide?.pageKey === item.pageKey }"
            @click="selectGuide(item)"
          >
            <div class="flex justify-between items-center gap-1">
              <span class="guide-nav-name">{{ item.title }}</span>
              <a-tag color="blue" class="m-0 text-xs">{{ item.category || '일반' }}</a-tag>
            </div>
            <div class="guide-nav-route">{{ item.pageKey }}</div>
          </div>

          <a-empty
            v-if="filteredGuides.length === 0 && !loading"
            description="등록된 도움말 가이드가 없습니다."
            class="my-8"
          />
        </div>
      </aside>

      <!-- 우측: 선택된 화면 도움말 본문 -->
      <main class="help-content-panel">
        <a-spin :spinning="loading">
          <template v-if="selectedGuide">
            <!-- 상단 헤더 & 액션 바 -->
            <div class="content-hero">
              <div class="hero-left">
                <div class="hero-badges">
                  <a-tag color="purple">{{ selectedGuide.appCode || defaultAppCode }}</a-tag>
                  <a-tag color="blue">{{ selectedGuide.category || '일반' }}</a-tag>
                  <code class="px-2 py-0.5 bg-gray-100 rounded text-xs font-mono text-blue-600">
                    {{ selectedGuide.pageKey }}
                  </code>
                </div>
                <h2 class="hero-title">{{ selectedGuide.title }}</h2>
                <p class="hero-summary">{{ selectedGuide.summary }}</p>
              </div>
              <div class="hero-actions">
                <a-button
                  v-if="isNavigableRoute(selectedGuide.pageKey)"
                  @click="handleNavigateToScreen(selectedGuide.pageKey)"
                >
                  해당 화면으로 이동
                </a-button>
                <a-button @click="handleOpenEditModal(selectedGuide)">
                  현재 가이드 편집
                </a-button>
                <a-button type="primary" @click="handleGoManagePage">
                  도움말 관리 마스터
                </a-button>
              </div>
            </div>

            <!-- 상세 개요 -->
            <div v-if="selectedGuide.overview" class="doc-overview">
              {{ selectedGuide.overview }}
            </div>

            <!-- 실무 운영 팁 -->
            <a-alert
              v-if="parsed.tips.length > 0"
              type="info"
              show-icon
              class="mb-5"
            >
              <template #message>
                <span class="font-semibold">실무 운영 팁</span>
              </template>
              <template #description>
                <ul class="list-disc pl-5 m-0 space-y-1">
                  <li v-for="(tip, idx) in parsed.tips" :key="idx">{{ tip }}</li>
                </ul>
              </template>
            </a-alert>

            <!-- 주요 기능 & 업무 수행 절차 -->
            <a-row :gutter="16">
              <a-col :xs="24" :lg="12" v-if="parsed.features.length > 0">
                <section class="doc-section">
                  <h3 class="doc-section-title">주요 제공 기능</h3>
                  <div class="feature-list">
                    <div v-for="(feat, idx) in parsed.features" :key="idx" class="feature-card">
                      <div class="flex justify-between items-center mb-1">
                        <span class="feature-card-title">{{ idx + 1 }}. {{ feat.name }}</span>
                        <a-tag v-if="feat.badge" color="cyan" class="m-0">{{ feat.badge }}</a-tag>
                      </div>
                      <div class="feature-card-desc">{{ feat.description }}</div>
                    </div>
                  </div>
                </section>
              </a-col>

              <a-col :xs="24" :lg="12" v-if="parsed.steps.length > 0">
                <section class="doc-section">
                  <h3 class="doc-section-title">표준 업무 수행 절차</h3>
                  <div class="feature-list">
                    <div v-for="(step, idx) in parsed.steps" :key="idx" class="feature-card">
                      <div class="feature-card-title">{{ step.title }}</div>
                      <div class="feature-card-desc">{{ step.description }}</div>
                    </div>
                  </div>
                </section>
              </a-col>
            </a-row>

            <!-- 데이터 계보 -->
            <section
              v-if="parsed.lineage.sources.length > 0 || parsed.lineage.destinations.length > 0"
              class="doc-section"
            >
              <h3 class="doc-section-title">데이터 계보 및 연계 흐름</h3>
              <a-row :gutter="16">
                <a-col :xs="24" :md="12">
                  <div class="lineage-card">
                    <div class="lineage-card-header text-blue-700">데이터 입력 원천</div>
                    <div
                      v-for="(src, idx) in parsed.lineage.sources"
                      :key="idx"
                      class="lineage-entry"
                    >
                      <div class="font-semibold text-gray-900 text-xs">
                        {{ src.origin }}
                        <span v-if="src.cycle" class="text-gray-500 font-normal">({{ src.cycle }})</span>
                      </div>
                      <div class="text-gray-600 text-xs mt-0.5">{{ src.description }}</div>
                    </div>
                    <div v-if="parsed.lineage.sources.length === 0" class="text-xs text-gray-400">
                      등록된 원천 정보 없음
                    </div>
                  </div>
                </a-col>
                <a-col :xs="24" :md="12">
                  <div class="lineage-card">
                    <div class="lineage-card-header text-indigo-700">후속 반영 및 연계 대상</div>
                    <div
                      v-for="(dst, idx) in parsed.lineage.destinations"
                      :key="idx"
                      class="lineage-entry"
                    >
                      <div class="font-semibold text-gray-900 text-xs">
                        {{ dst.target }}
                        <span v-if="dst.timing" class="text-gray-500 font-normal">({{ dst.timing }})</span>
                      </div>
                      <div class="text-gray-600 text-xs mt-0.5">{{ dst.description }}</div>
                    </div>
                    <div v-if="parsed.lineage.destinations.length === 0" class="text-xs text-gray-400">
                      등록된 연계 정보 없음
                    </div>
                  </div>
                </a-col>
              </a-row>
            </section>

            <!-- 산출 공식 -->
            <section v-if="parsed.formulas.length > 0" class="doc-section">
              <h3 class="doc-section-title">핵심 지표 산출 공식</h3>
              <div class="space-y-2">
                <div v-for="(fm, idx) in parsed.formulas" :key="idx" class="feature-card">
                  <div class="feature-card-title">{{ fm.name }}</div>
                  <code class="block my-1 px-2 py-1 bg-white border border-gray-200 rounded text-xs font-mono text-purple-700">
                    {{ fm.formula }}
                  </code>
                  <div class="feature-card-desc">{{ fm.description }}</div>
                </div>
              </div>
            </section>

            <!-- 주의사항 -->
            <a-alert
              v-if="parsed.warnings.length > 0"
              type="warning"
              show-icon
              class="mb-5"
            >
              <template #message>
                <span class="font-semibold">업무 처리 시 주의사항 및 제약 조건</span>
              </template>
              <template #description>
                <ul class="list-disc pl-5 m-0 space-y-1">
                  <li v-for="(warn, idx) in parsed.warnings" :key="idx">{{ warn }}</li>
                </ul>
              </template>
            </a-alert>

            <!-- FAQ -->
            <section v-if="parsed.faq.length > 0" class="doc-section">
              <h3 class="doc-section-title">자주 묻는 질문</h3>
              <div class="space-y-2">
                <div v-for="(item, idx) in parsed.faq" :key="idx" class="feature-card">
                  <div class="feature-card-title">Q. {{ item.q }}</div>
                  <div class="feature-card-desc mt-1">A. {{ item.a }}</div>
                </div>
              </div>
            </section>
          </template>

          <a-empty
            v-else-if="!loading"
            description="좌측 목록에서 열람할 화면 가이드를 선택하거나, 도움말 관리 화면에서 신규 가이드를 등록해 주세요."
            class="my-16"
          >
            <a-button type="primary" @click="handleGoManagePage">도움말 관리로 이동</a-button>
          </a-empty>
        </a-spin>
      </main>
    </div>

    <HelpEditModal @register="registerEditModal" @success="loadAllGuides" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, computed, onMounted, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { getHelpList, SysPageHelpModel, DEFAULT_APP_CODE } from './help.api';
  import { parseHelpRecord } from './help.data';
  import HelpEditModal from './components/HelpEditModal.vue';

  const route = useRoute();
  const router = useRouter();
  const defaultAppCode = DEFAULT_APP_CODE;

  const loading = ref(false);
  const searchKeyword = ref('');
  const guideList = ref<SysPageHelpModel[]>([]);
  const selectedGuide = ref<SysPageHelpModel | null>(null);

  const [registerEditModal, { openModal: openEditModal }] = useModal();

  const filteredGuides = computed(() => {
    const kw = searchKeyword.value.trim().toLowerCase();
    if (!kw) return guideList.value;
    return guideList.value.filter(
      (item) =>
        item.title?.toLowerCase().includes(kw) ||
        item.pageKey?.toLowerCase().includes(kw) ||
        item.category?.toLowerCase().includes(kw) ||
        item.summary?.toLowerCase().includes(kw),
    );
  });

  const parsed = computed(() => parseHelpRecord(selectedGuide.value));

  async function loadAllGuides() {
    loading.value = true;
    try {
      const res: any = await getHelpList();
      const records: SysPageHelpModel[] = Array.isArray(res)
        ? res
        : Array.isArray(res?.records)
        ? res.records
        : [];
      guideList.value = records.filter((i) => i.status === 'ACTIVE' || !i.status);

      const queryRoute = (route.query.route as string) || '';
      if (queryRoute) {
        const matched = guideList.value.find(
          (item) => item.pageKey === queryRoute || queryRoute.startsWith(item.pageKey + '/'),
        );
        if (matched) {
          selectedGuide.value = matched;
          return;
        }
      }

      if (selectedGuide.value?.pageKey) {
        const refreshed = guideList.value.find((item) => item.pageKey === selectedGuide.value?.pageKey);
        if (refreshed) {
          selectedGuide.value = refreshed;
          return;
        }
      }

      selectedGuide.value = guideList.value[0] || null;
    } finally {
      loading.value = false;
    }
  }

  function selectGuide(item: SysPageHelpModel) {
    selectedGuide.value = item;
  }

  function isNavigableRoute(pageKey?: string) {
    if (!pageKey) return false;
    const nonRouteKeys = ['/overview', '/api-guide', '/error-codes'];
    return pageKey.startsWith('/') && !nonRouteKeys.includes(pageKey) && pageKey !== route.path;
  }

  function handleNavigateToScreen(pageKey: string) {
    router.push(pageKey);
  }

  function handleOpenEditModal(guide: SysPageHelpModel) {
    openEditModal(true, {
      isUpdate: true,
      record: guide,
    });
  }

  function handleGoManagePage() {
    router.push('/system/help-guide');
  }

  watch(
    () => route.query.route,
    (newRoute) => {
      if (newRoute && typeof newRoute === 'string') {
        const matched = guideList.value.find((item) => item.pageKey === newRoute);
        if (matched) selectedGuide.value = matched;
      }
    },
  );

  onMounted(() => {
    loadAllGuides();
  });
</script>

<style lang="less" scoped>
  .help-viewer-layout {
    display: grid;
    grid-template-columns: 300px 1fr;
    gap: 16px;
    min-height: calc(100vh - 190px);
  }

  .help-sidebar {
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .sidebar-header {
      padding: 16px;
      border-bottom: 1px solid #f1f5f9;

      .sidebar-title {
        font-size: 14px;
        font-weight: 700;
        color: #0f172a;
        margin-bottom: 10px;
      }
    }

    .sidebar-menu-list {
      flex: 1;
      overflow-y: auto;
      padding: 10px;
    }

    .guide-nav-item {
      padding: 10px 12px;
      border-radius: 8px;
      cursor: pointer;
      margin-bottom: 6px;
      border: 1px solid transparent;
      transition: all 0.15s ease;

      &:hover {
        background: #f8fafc;
        border-color: #e2e8f0;
      }

      &.active {
        background: #eff6ff;
        border-color: #bfdbfe;

        .guide-nav-name {
          color: #1d4ed8;
          font-weight: 700;
        }
      }

      .guide-nav-name {
        font-size: 13px;
        font-weight: 600;
        color: #1e293b;
      }

      .guide-nav-route {
        font-size: 11px;
        color: #64748b;
        font-family: monospace;
        margin-top: 2px;
      }
    }
  }

  .help-content-panel {
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    padding: 24px 28px;
  }

  .content-hero {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 16px;
    border-bottom: 1px solid #f1f5f9;
    padding-bottom: 18px;
    margin-bottom: 18px;

    .hero-badges {
      display: flex;
      align-items: center;
      gap: 6px;
      flex-wrap: wrap;
      margin-bottom: 8px;
    }

    .hero-title {
      font-size: 22px;
      font-weight: 700;
      color: #0f172a;
      margin: 0 0 6px 0;
    }

    .hero-summary {
      font-size: 14px;
      color: #475569;
      margin: 0;
      line-height: 1.6;
    }

    .hero-actions {
      display: flex;
      gap: 8px;
      flex-shrink: 0;
    }
  }

  .doc-overview {
    padding: 14px 16px;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    font-size: 13px;
    color: #334155;
    line-height: 1.65;
    white-space: pre-wrap;
    margin-bottom: 20px;
  }

  .doc-section {
    margin-bottom: 22px;

    .doc-section-title {
      font-size: 15px;
      font-weight: 700;
      color: #0f172a;
      margin-bottom: 12px;
      padding-left: 10px;
      border-left: 3px solid #2563eb;
    }
  }

  .feature-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .feature-card {
    padding: 12px 14px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #fafafa;

    .feature-card-title {
      font-size: 13px;
      font-weight: 700;
      color: #0f172a;
    }

    .feature-card-desc {
      font-size: 13px;
      color: #475569;
      line-height: 1.55;
    }
  }

  .lineage-card {
    padding: 12px 14px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #f8fafc;
    height: 100%;

    .lineage-card-header {
      font-size: 12px;
      font-weight: 700;
      margin-bottom: 8px;
    }

    .lineage-entry {
      padding: 6px 0;
      border-bottom: 1px dashed #e2e8f0;

      &:last-child {
        border-bottom: none;
      }
    }
  }
</style>
