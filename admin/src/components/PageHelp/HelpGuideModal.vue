<template>
  <div>
    <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('appUi.kcda5fef2')"
    width="1160px"
    :showOkBtn="false"
    :cancelText="t('appUi.k94b7dba1')"
    :loading="loading"
  >
    <div class="help-guide-modal-container">
      <!-- 상단 현재 화면 컨텍스트 배너 및 빠른 전환 툴바 -->
      <div class="context-bar">
        <div class="context-bar-left">
          <a-tag color="blue">{{ t('appUi.k669bdf37') }}</a-tag>
          <span class="context-route-title">{{ currentRouteTitle || t('appUi.k0d2e1c24') }}</span>
          <code class="context-route-path">{{ currentRoutePath }}</code>
          <a-tag v-if="!hasCurrentRouteGuide && !loading" color="warning">{{ t('appUi.kb5b365aa') }}</a-tag>
        </div>
        <div class="context-bar-actions">
          <a-button
            v-if="primarySystemGuide && !isViewingSystem"
            size="small"
            type="default"
            @click="handleSelectSystemOverview"
          >
            {{ t('appUi.ka38eefb5') }}
          </a-button>
          <a-button
            v-if="currentRouteGuide && selectedGuide?.pageKey !== currentRouteGuide.pageKey"
            size="small"
            type="default"
            @click="selectGuide(currentRouteGuide)"
          >
            {{ t('appUi.k0d2e1c24') }}
          </a-button>
          <a-button size="small" @click="handleCopyFullGuide">
            {{ t('appUi.k5ced26da') }}
          </a-button>
          <template v-if="isAdminUser">
            <a-button v-if="!hasCurrentRouteGuide" type="primary" size="small" @click="handleCreateForCurrentRoute">
              {{ t('appUi.k4b507cd4') }}
            </a-button>
            <a-button v-else size="small" @click="handleEditSelectedGuide">
              {{ t('appUi.k637fac84') }}
            </a-button>
            <a-button size="small" @click="handleGoManagePage">
              {{ t('appUi.k0e985fbe') }}
            </a-button>
          </template>
          <a-button type="primary" ghost size="small" @click="handleOpenFullPageViewer">
            {{ t('appUi.k1f9029eb') }}
          </a-button>
        </div>
      </div>

      <!-- 2-Pane 본문 -->
      <div class="modal-two-pane">
        <HelpSidebarNav
          ref="sidebarRef"
          :guides="guideList"
          :selected-page-key="selectedGuide?.pageKey"
          :current-route-path="currentRoutePath"
          :loading="loading"
          @select="selectGuide"
        />

        <main class="modal-content">
            <template v-if="selectedGuide">
              <HelpSystemContent
                v-if="isSystemGuide(selectedGuide)"
                :guide="selectedGuide"
                :default-app-code="defaultAppCode"
                @navigate-path="handleNavigatePath"
                @navigate-related="handleNavigateRelatedScreen"
                @copy-prompt="handleCopyPrompt"
              />
              <HelpScreenContent
                v-else
                :guide="selectedGuide"
                :default-app-code="defaultAppCode"
                @navigate-related="handleNavigateRelatedScreen"
                @copy-prompt="handleCopyPrompt"
              />
            </template>

            <a-empty
              v-else-if="!loading"
              :description="t('appUi.k0e712a4f')"
              class="my-12"
            />
        </main>
      </div>
    </div>

    </BasicModal>

    <HelpEditModal @register="registerEditModal" @success="handleEditSuccess" />
  </div>
</template>

<script lang="ts" setup>
  defineOptions({ inheritAttrs: false });
  defineEmits(['register']);
  import { computed, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { BasicModal, useModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import {
    DEFAULT_APP_CODE,
    getHelpGuideByPath,
    getHelpList,
    HELP_MANAGE_PATH,
    HELP_VIEWER_PATH,
    RelatedScreenItem,
    SysPageHelpModel,
  } from '/@/views/system/help/help.api';
  import {
    extractRelatedScreenPath,
    formatFullGuideClipboardText,
    isSystemGuide,
  } from '/@/views/system/help/help.data';
  import HelpEditModal from '/@/views/system/help/components/HelpEditModal.vue';
  import HelpSidebarNav from '/@/views/system/help/components/HelpSidebarNav.vue';
  import HelpSystemContent from '/@/views/system/help/components/HelpSystemContent.vue';
  import HelpScreenContent from '/@/views/system/help/components/HelpScreenContent.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const route = useRoute();
  const router = useRouter();
  const userStore = useUserStore();
  const { createMessage } = useMessage();
  const defaultAppCode = DEFAULT_APP_CODE;

  const loading = ref(false);
  const currentRoutePath = ref('');
  const currentRouteTitle = ref('');
  const guideList = ref<SysPageHelpModel[]>([]);
  const selectedGuide = ref<SysPageHelpModel | null>(null);
  const sidebarRef = ref<{ clearSearch?: () => void } | null>(null);

  const [registerEditModal, { openModal: openEditModal }] = useModal();

  const [registerModal, { closeModal }] = useModalInner(async (data) => {
    const targetPath = data?.path || data?.currentPath || route.path || '';
    currentRoutePath.value = targetPath;
    currentRouteTitle.value = data?.title || (route.meta?.title as string) || '';
    sidebarRef.value?.clearSearch?.();
    await loadHelpData(targetPath);
  });

  const isAdminUser = computed(() => {
    const info = (userStore.getUserInfo || {}) as Record<string, unknown>;
    const username = String(info.username || '').toLowerCase();
    const roles: unknown[] = Array.isArray(userStore.getRoleList) ? userStore.getRoleList : [];
    const roleCodes = roles.map((r) => {
      if (r && typeof r === 'object') {
        const obj = r as { value?: string; roleCode?: string };
        return String(obj.value || obj.roleCode || '').toLowerCase();
      }
      return String(r || '').toLowerCase();
    });
    if (!username && roleCodes.length === 0) return true;
    return username === 'admin' || roleCodes.includes('admin') || roleCodes.includes('role_admin');
  });

  const primarySystemGuide = computed(() => guideList.value.find((item) => isSystemGuide(item)) || null);
  const isViewingSystem = computed(() => isSystemGuide(selectedGuide.value));

  const currentRouteGuide = computed(() => {
    if (!currentRoutePath.value) return null;
    return (
      guideList.value.find(
        (item) =>
          !isSystemGuide(item) &&
          (item.pageKey === currentRoutePath.value ||
            (item.pageKey && currentRoutePath.value.startsWith(item.pageKey + '/'))),
      ) || null
    );
  });

  const hasCurrentRouteGuide = computed(() => !!currentRouteGuide.value);

  async function loadHelpData(targetPath: string) {
    loading.value = true;
    try {
      const listRes = await getHelpList();
      let directGuide: SysPageHelpModel | null = null;
      if (targetPath) {
        try {
          directGuide = await getHelpGuideByPath({ path: targetPath });
        } catch {
          directGuide = null;
        }
      }

      const paged = listRes as unknown as { records?: SysPageHelpModel[] };
      const records: SysPageHelpModel[] = Array.isArray(listRes)
        ? listRes
        : Array.isArray(paged?.records)
        ? paged.records
        : [];

      const activeRecords = records.filter((i) => i.status === 'ACTIVE' || !i.status);

      if (directGuide && directGuide.pageKey && !activeRecords.some((item) => item.pageKey === directGuide.pageKey)) {
        activeRecords.push(directGuide);
      }

      activeRecords.sort((a, b) => {
        const typeDiff = (isSystemGuide(a) ? 0 : 1) - (isSystemGuide(b) ? 0 : 1);
        if (typeDiff !== 0) return typeDiff;
        return (a.sortOrder ?? 0) - (b.sortOrder ?? 0);
      });

      guideList.value = activeRecords;

      // 1순위: 현재 접속 화면 가이드 (실무 컨텍스트 유지)
      if (directGuide && directGuide.pageKey) {
        selectedGuide.value = directGuide;
      } else {
        const matched = activeRecords.find(
          (item) =>
            !isSystemGuide(item) &&
            (item.pageKey === targetPath ||
              (targetPath && item.pageKey && targetPath.startsWith(item.pageKey + '/'))),
        );
        // 2순위: 현재 화면 미등록 시 시스템 개요(SYSTEM) 우선 표시
        selectedGuide.value = matched || activeRecords.find((i) => isSystemGuide(i)) || activeRecords[0] || null;
      }
    } finally {
      loading.value = false;
    }
  }

  function selectGuide(item: SysPageHelpModel) {
    selectedGuide.value = item;
  }

  function handleSelectSystemOverview() {
    if (primarySystemGuide.value) {
      selectedGuide.value = primarySystemGuide.value;
    }
  }

  function handleCopyPrompt(text: string) {
    if (!text) return;
    navigator.clipboard?.writeText(text);
    createMessage.success(t('appUi.ke85ada34'));
  }

  function handleCopyFullGuide() {
    if (!selectedGuide.value) return;
    const text = formatFullGuideClipboardText(selectedGuide.value);
    navigator.clipboard?.writeText(text);
    createMessage.success(t('appUi.k5068e969'));
  }

  function handleNavigatePath(targetPath: string) {
    if (targetPath && targetPath.startsWith('/')) {
      closeModal();
      router.push(targetPath);
    }
  }

  function handleNavigateRelatedScreen(rel: RelatedScreenItem) {
    const targetPath = extractRelatedScreenPath(rel);
    if (targetPath) {
      closeModal();
      router.push(targetPath);
    }
  }

  function handleCreateForCurrentRoute() {
    openEditModal(true, {
      isUpdate: false,
      record: {
        appCode: DEFAULT_APP_CODE,
        helpType: 'SCREEN',
        pageKey: currentRoutePath.value,
        title: currentRouteTitle.value || currentRoutePath.value,
        category: t('appUi.kaef1a1e7'),
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
    router.push({
      path: HELP_MANAGE_PATH,
      query: { mode: 'manage' },
    });
  }

  function handleOpenFullPageViewer() {
    const targetRoute = selectedGuide.value?.pageKey || currentRoutePath.value;
    closeModal();
    router.push({
      path: HELP_VIEWER_PATH,
      query: targetRoute ? { route: targetRoute, mode: 'viewer' } : { mode: 'viewer' },
    });
  }
</script>

<style lang="less" scoped>
  .help-guide-modal-container {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 12px 16px;
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
      flex-wrap: wrap;
    }
  }

  .modal-two-pane {
    display: grid;
    grid-template-columns: 270px 1fr;
    gap: 14px;
    height: clamp(360px, 60vh, 600px);
    min-height: 0;

    @media (max-width: 767.98px) {
      grid-template-columns: 1fr;
      height: auto;
    }
  }

  .modal-content {
    min-height: 0;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #ffffff;
    padding: 18px 20px;
    overflow-y: auto;
  }
</style>
