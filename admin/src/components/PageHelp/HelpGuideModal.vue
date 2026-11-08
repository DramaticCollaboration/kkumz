<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="화면 도움말 및 운영 가이드"
    width="1120px"
    :showOkBtn="false"
    cancelText="닫기"
  >
    <div class="help-guide-modal-container">
      <!-- 상단 현재 화면 컨텍스트 배너 및 바로가기 툴바 -->
      <div class="context-bar">
        <div class="context-bar-left">
          <a-tag color="blue">현재 접속 화면</a-tag>
          <span class="context-route-title">{{ currentRouteTitle || '현재 화면' }}</span>
          <code class="context-route-path">{{ currentRoutePath }}</code>
          <a-tag v-if="!hasCurrentRouteGuide && !loading" color="warning">미등록 화면</a-tag>
        </div>
        <div class="context-bar-actions">
          <a-button v-if="!hasCurrentRouteGuide" type="primary" size="small" @click="handleCreateForCurrentRoute">
            현재 화면 가이드 등록
          </a-button>
          <a-button v-else size="small" @click="handleEditSelectedGuide">
            현재 가이드 편집
          </a-button>
          <a-button size="small" @click="handleGoManagePage">
            도움말 관리
          </a-button>
          <a-button type="primary" ghost size="small" @click="handleOpenFullPageViewer">
            전체화면 도움말 보기
          </a-button>
        </div>
      </div>

      <!-- 2-Pane 본문 -->
      <div class="modal-two-pane">
        <!-- 좌측: 등록된 화면 가이드 탐색 사이드바 -->
        <aside class="modal-sidebar">
          <div class="sidebar-search">
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="화면명 또는 경로 검색"
              allow-clear
              size="small"
            />
          </div>
          <div class="sidebar-list">
            <div
              v-for="item in filteredGuides"
              :key="item.id || item.pageKey"
              class="sidebar-item"
              :class="{
                active: selectedGuide?.pageKey === item.pageKey,
                'is-current-route': item.pageKey === currentRoutePath,
              }"
              @click="selectGuide(item)"
            >
              <div class="sidebar-item-top">
                <span class="sidebar-item-title">{{ item.title }}</span>
                <a-tag v-if="item.pageKey === currentRoutePath" color="processing" class="m-0 text-xs">
                  현재 화면
                </a-tag>
              </div>
              <div class="sidebar-item-route">{{ item.pageKey }}</div>
            </div>
            <a-empty
              v-if="filteredGuides.length === 0 && !loading"
              description="검색 결과가 없습니다."
              class="my-6"
            />
          </div>
        </aside>

        <!-- 우측: 선택된 가이드 상세 본문 -->
        <main class="modal-content">
          <a-spin :spinning="loading">
            <template v-if="selectedGuide">
              <div class="content-header">
                <div class="content-header-tags">
                  <a-tag color="default">{{ selectedGuide.appCode || defaultAppCode }}</a-tag>
                  <a-tag color="blue">{{ selectedGuide.category || '일반' }}</a-tag>
                  <code class="px-2 py-0.5 bg-gray-100 rounded text-xs font-mono text-blue-600">
                    {{ selectedGuide.pageKey }}
                  </code>
                </div>
                <h2 class="content-title">{{ selectedGuide.title }}</h2>
                <p class="content-summary">{{ selectedGuide.summary }}</p>
                <div v-if="selectedGuide.overview" class="content-overview">
                  {{ selectedGuide.overview }}
                </div>
              </div>

              <!-- 실무 운영 팁 -->
              <a-alert
                v-if="parsed.tips.length > 0"
                type="info"
                show-icon
                class="mb-4"
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

              <!-- 주요 제공 기능 및 조작 절차 -->
              <a-row :gutter="16">
                <a-col :xs="24" :md="12" v-if="parsed.features.length > 0">
                  <div class="section-block">
                    <h4 class="section-heading">주요 제공 기능</h4>
                    <div class="card-stack">
                      <div v-for="(feat, idx) in parsed.features" :key="idx" class="info-card">
                        <div class="flex justify-between items-center mb-1">
                          <span class="info-card-title">{{ idx + 1 }}. {{ feat.name }}</span>
                          <a-tag v-if="feat.badge" color="default" class="m-0">{{ feat.badge }}</a-tag>
                        </div>
                        <div class="info-card-desc">{{ feat.description }}</div>
                      </div>
                    </div>
                  </div>
                </a-col>
                <a-col :xs="24" :md="12" v-if="parsed.steps.length > 0">
                  <div class="section-block">
                    <h4 class="section-heading">표준 업무 수행 절차</h4>
                    <div class="card-stack">
                      <div v-for="(step, idx) in parsed.steps" :key="idx" class="info-card">
                        <div class="info-card-title">{{ step.title }}</div>
                        <div class="info-card-desc">{{ step.description }}</div>
                      </div>
                    </div>
                  </div>
                </a-col>
              </a-row>

              <!-- 데이터 계보 -->
              <div
                v-if="parsed.lineage.sources.length > 0 || parsed.lineage.destinations.length > 0"
                class="section-block"
              >
                <h4 class="section-heading">데이터 계보 및 연계 흐름</h4>
                <a-row :gutter="12">
                  <a-col :xs="24" :md="12">
                    <div class="lineage-box">
                      <div class="lineage-label">데이터 입력 원천</div>
                      <div v-for="(src, idx) in parsed.lineage.sources" :key="idx" class="mb-2 last:mb-0">
                        <div class="lineage-val">
                          {{ src.origin }}
                          <span v-if="src.cycle" class="text-gray-500 font-normal">({{ src.cycle }})</span>
                        </div>
                        <div class="text-xs text-gray-600">{{ src.description }}</div>
                      </div>
                    </div>
                  </a-col>
                  <a-col :xs="24" :md="12">
                    <div class="lineage-box active">
                      <div class="lineage-label">후속 반영 및 연계 대상</div>
                      <div v-for="(dst, idx) in parsed.lineage.destinations" :key="idx" class="mb-2 last:mb-0">
                        <div class="lineage-val">
                          {{ dst.target }}
                          <span v-if="dst.timing" class="text-gray-500 font-normal">({{ dst.timing }})</span>
                        </div>
                        <div class="text-xs text-gray-600">{{ dst.description }}</div>
                      </div>
                    </div>
                  </a-col>
                </a-row>
              </div>

              <!-- 산출 공식 -->
              <div v-if="parsed.formulas.length > 0" class="section-block">
                <h4 class="section-heading">핵심 지표 산출 공식</h4>
                <div class="card-stack">
                  <div v-for="(fm, idx) in parsed.formulas" :key="idx" class="info-card">
                    <div class="info-card-title">{{ fm.name }}</div>
                    <code class="block my-1 px-2 py-1 bg-white border border-gray-200 rounded text-xs font-mono text-slate-700">
                      {{ fm.formula }}
                    </code>
                    <div class="info-card-desc">{{ fm.description }}</div>
                  </div>
                </div>
              </div>

              <!-- 주의사항 -->
              <a-alert
                v-if="parsed.warnings.length > 0"
                type="warning"
                show-icon
                class="mb-4"
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
              <div v-if="parsed.faq.length > 0" class="section-block">
                <h4 class="section-heading">자주 묻는 질문</h4>
                <div class="card-stack">
                  <div v-for="(item, idx) in parsed.faq" :key="idx" class="info-card">
                    <div class="info-card-title">Q. {{ item.q }}</div>
                    <div class="info-card-desc mt-1">A. {{ item.a }}</div>
                  </div>
                </div>
              </div>
            </template>

            <a-empty
              v-else-if="!loading"
              description="현재 화면에 등록된 도움말이 없습니다. 상단 '현재 화면 가이드 등록' 버튼을 눌러 바로 작성할 수 있습니다."
              class="my-12"
            />
          </a-spin>
        </main>
      </div>
    </div>

    <HelpEditModal @register="registerEditModal" @success="handleEditSuccess" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { BasicModal, useModalInner, useModal } from '/@/components/Modal';
  import {
    getHelpList,
    getHelpGuideByPath,
    SysPageHelpModel,
    DEFAULT_APP_CODE,
    HELP_VIEWER_PATH,
  } from '/@/views/system/help/help.api';
  import { parseHelpRecord } from '/@/views/system/help/help.data';
  import HelpEditModal from '/@/views/system/help/components/HelpEditModal.vue';

  const route = useRoute();
  const router = useRouter();
  const defaultAppCode = DEFAULT_APP_CODE;

  const loading = ref(false);
  const currentRoutePath = ref('');
  const currentRouteTitle = ref('');
  const searchKeyword = ref('');
  const guideList = ref<SysPageHelpModel[]>([]);
  const selectedGuide = ref<SysPageHelpModel | null>(null);

  const [registerEditModal, { openModal: openEditModal }] = useModal();

  const [registerModal, { closeModal }] = useModalInner(async (data) => {
    const targetPath = data?.path || data?.currentPath || route.path || '';
    currentRoutePath.value = targetPath;
    currentRouteTitle.value = data?.title || (route.meta?.title as string) || '';
    searchKeyword.value = '';
    await loadHelpData(targetPath);
  });

  const hasCurrentRouteGuide = computed(() => {
    if (!currentRoutePath.value) return false;
    return guideList.value.some((item) => item.pageKey === currentRoutePath.value);
  });

  const filteredGuides = computed(() => {
    const kw = searchKeyword.value.trim().toLowerCase();
    if (!kw) return guideList.value;
    return guideList.value.filter(
      (item) =>
        (item.title && item.title.toLowerCase().includes(kw)) ||
        (item.pageKey && item.pageKey.toLowerCase().includes(kw)) ||
        (item.category && item.category.toLowerCase().includes(kw)) ||
        (item.summary && item.summary.toLowerCase().includes(kw)),
    );
  });

  const parsed = computed(() => parseHelpRecord(selectedGuide.value));

  async function loadHelpData(targetPath: string) {
    loading.value = true;
    try {
      const [listRes, directGuide] = await Promise.all([
        getHelpList().catch(() => []),
        targetPath ? getHelpGuideByPath({ path: targetPath }).catch(() => null) : Promise.resolve(null),
      ]);

      const records: SysPageHelpModel[] = Array.isArray(listRes)
        ? listRes
        : Array.isArray((listRes as any)?.records)
        ? (listRes as any).records
        : [];

      const activeRecords = records.filter((i) => i.status === 'ACTIVE' || !i.status);

      if (directGuide && directGuide.pageKey && !activeRecords.some((item) => item.pageKey === directGuide.pageKey)) {
        activeRecords.unshift(directGuide);
      }

      guideList.value = activeRecords;

      if (directGuide && directGuide.pageKey) {
        selectedGuide.value = directGuide;
      } else {
        const matched = activeRecords.find(
          (item) =>
            item.pageKey === targetPath ||
            (targetPath && item.pageKey && targetPath.startsWith(item.pageKey + '/')),
        );
        selectedGuide.value = matched || activeRecords[0] || null;
      }
    } finally {
      loading.value = false;
    }
  }

  function selectGuide(item: SysPageHelpModel) {
    selectedGuide.value = item;
  }

  function handleCreateForCurrentRoute() {
    openEditModal(true, {
      isUpdate: false,
      record: {
        appCode: DEFAULT_APP_CODE,
        pageKey: currentRoutePath.value,
        title: currentRouteTitle.value || currentRoutePath.value,
        category: '일반',
        status: 'ACTIVE',
        sortOrder: 10,
      },
    });
  }

  function handleEditSelectedGuide() {
    if (!selectedGuide.value) return;
    openEditModal(true, {
      isUpdate: true,
      record: selectedGuide.value,
    });
  }

  async function handleEditSuccess() {
    await loadHelpData(currentRoutePath.value);
  }

  function handleGoManagePage() {
    closeModal();
    router.push('/system/help-guide');
  }

  function handleOpenFullPageViewer() {
    const targetRoute = selectedGuide.value?.pageKey || currentRoutePath.value;
    closeModal();
    router.push({
      path: HELP_VIEWER_PATH,
      query: targetRoute ? { route: targetRoute } : undefined,
    });
  }
</script>

<style lang="less" scoped>
  .help-guide-modal-container {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .context-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    padding: 10px 14px;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 8px;

    .context-bar-left {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }

    .context-route-title {
      font-size: 13px;
      font-weight: 700;
      color: #0f172a;
    }

    .context-route-path {
      font-size: 12px;
      padding: 2px 6px;
      background: #e2e8f0;
      border-radius: 4px;
      color: #334155;
    }

    .context-bar-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .modal-two-pane {
    display: grid;
    grid-template-columns: 260px 1fr;
    gap: 14px;
    min-height: 520px;
    max-height: 640px;
  }

  .modal-sidebar {
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #ffffff;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .sidebar-search {
      padding: 10px;
      border-bottom: 1px solid #f1f5f9;
    }

    .sidebar-list {
      flex: 1;
      overflow-y: auto;
      padding: 8px;
    }

    .sidebar-item {
      padding: 8px 10px;
      border-radius: 6px;
      cursor: pointer;
      margin-bottom: 4px;
      border: 1px solid transparent;
      transition: all 0.15s;

      &:hover {
        background: #f8fafc;
      }

      &.active {
        background: #eff6ff;
        border-color: #bfdbfe;
      }

      .sidebar-item-top {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 4px;
      }

      .sidebar-item-title {
        font-size: 13px;
        font-weight: 600;
        color: #1e293b;
      }

      .sidebar-item-route {
        font-size: 11px;
        color: #64748b;
        font-family: monospace;
        margin-top: 2px;
      }
    }
  }

  .modal-content {
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #ffffff;
    padding: 18px 20px;
    overflow-y: auto;
  }

  .content-header {
    border-bottom: 1px solid #f1f5f9;
    padding-bottom: 14px;
    margin-bottom: 16px;

    .content-header-tags {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-bottom: 6px;
    }

    .content-title {
      font-size: 18px;
      font-weight: 700;
      color: #0f172a;
      margin: 0 0 6px 0;
    }

    .content-summary {
      font-size: 13px;
      color: #475569;
      margin: 0;
      line-height: 1.6;
    }

    .content-overview {
      margin-top: 10px;
      padding: 10px 12px;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      font-size: 12px;
      color: #334155;
      line-height: 1.6;
      white-space: pre-wrap;
    }
  }

  .section-block {
    margin-bottom: 16px;

    .section-heading {
      font-size: 14px;
      font-weight: 700;
      color: #1e293b;
      margin-bottom: 8px;
      padding-left: 8px;
      border-left: 3px solid #2563eb;
    }
  }

  .lineage-box {
    padding: 10px 12px;
    border-radius: 6px;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    height: 100%;

    &.active {
      background: #eff6ff;
      border-color: #93c5fd;
    }

    .lineage-label {
      font-size: 11px;
      font-weight: 700;
      color: #64748b;
      margin-bottom: 6px;
    }

    .lineage-val {
      font-size: 12px;
      font-weight: 600;
      color: #0f172a;
    }
  }

  .card-stack {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .info-card {
    padding: 10px 12px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    background: #fafafa;

    .info-card-title {
      font-size: 13px;
      font-weight: 700;
      color: #0f172a;
    }

    .info-card-desc {
      font-size: 12px;
      color: #475569;
      line-height: 1.5;
    }
  }
</style>
