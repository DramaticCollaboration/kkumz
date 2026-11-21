<template>
  <PageWrapper>
    <div class="help-viewer-layout">
      <HelpSidebarNav
        :guides="guideList"
        :selected-page-key="selectedGuide?.pageKey"
        :loading="loading"
        :title="t('appUi.k6f40311a')"
        @select="selectGuide"
      />

      <main class="help-content-panel" v-loading="loading">
          <template v-if="selectedGuide">
            <div class="viewer-toolbar">
              <div class="viewer-toolbar-meta">
                <a-tag :color="isSystemGuide(selectedGuide) ? 'cyan' : 'blue'">
                  {{ isSystemGuide(selectedGuide) ? t('appUi.k368c6bad') : t('appUi.ka3a0501d') }}
                </a-tag>
                <span class="viewer-toolbar-title">{{ selectedGuide.title }}</span>
              </div>
              <div class="viewer-toolbar-actions">
                <a-button size="small" @click="handleCopyFullGuide">
                  {{ t('appUi.k5ced26da') }}
                </a-button>
                <a-button
                  v-if="isNavigableScreen"
                  size="small"
                  @click="handleNavigateToScreen(selectedGuide.pageKey)"
                >
                  {{ t('appUi.k10d821dc') }}
                </a-button>
                <template v-if="isAdminUser">
                  <a-button size="small" @click="handleOpenEditModal(selectedGuide)">
                    {{ t('appUi.k637fac84') }}
                  </a-button>
                  <a-button type="primary" size="small" @click="handleGoManagePage">
                    {{ t('appUi.k0e985fbe') }}
                  </a-button>
                </template>
              </div>
            </div>

            <HelpSystemContent
              v-if="isSystemGuide(selectedGuide)"
              :guide="selectedGuide"
              :default-app-code="defaultAppCode"
              @navigate-path="handleNavigatePath"
              @navigate-related="handleNavigateRelated"
              @copy-prompt="handleCopyPrompt"
            />
            <HelpScreenContent
              v-else
              :guide="selectedGuide"
              :default-app-code="defaultAppCode"
              @navigate-related="handleNavigateRelated"
              @copy-prompt="handleCopyPrompt"
            />
          </template>

          <a-empty
            v-else-if="!loading"
            :description="t('appUi.kc4ff46cc')"
            class="my-16"
          >
            <a-button v-if="isAdminUser" type="primary" @click="handleGoManagePage">
              {{ t('appUi.k0e985fbe') }}
            </a-button>
          </a-empty>
      </main>
    </div>

    <HelpEditModal @register="registerEditModal" @success="loadAllGuides" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { PageWrapper } from '/@/components/Page';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import {
    DEFAULT_APP_CODE,
    getHelpList,
    HELP_MANAGE_PATH,
    RelatedScreenItem,
    SysPageHelpModel,
  } from './help.api';
  import {
    extractRelatedScreenPath,
    formatFullGuideClipboardText,
    isSystemGuide,
  } from './help.data';
  import HelpEditModal from './components/HelpEditModal.vue';
  import HelpSidebarNav from './components/HelpSidebarNav.vue';
  import HelpSystemContent from './components/HelpSystemContent.vue';
  import HelpScreenContent from './components/HelpScreenContent.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const emit = defineEmits(['switch-manage']);
  const route = useRoute();
  const router = useRouter();
  const userStore = useUserStore();
  const { createMessage } = useMessage();
  const defaultAppCode = DEFAULT_APP_CODE;

  const loading = ref(false);
  const guideList = ref<SysPageHelpModel[]>([]);
  const selectedGuide = ref<SysPageHelpModel | null>(null);

  const [registerEditModal, { openModal: openEditModal }] = useModal();

  const isAdminUser = computed(() => {
    const info = (userStore.getUserInfo || {}) as Record<string, unknown>;
    const username = String(info.username || '').toLowerCase();
    const roles: Array<string | { value?: string; roleCode?: string }> = Array.isArray(userStore.getRoleList) ? (userStore.getRoleList as Array<string | { value?: string; roleCode?: string }>) : [];
    const roleCodes = roles.map((r) => String(typeof r === 'object' ? r?.value || r?.roleCode : r).toLowerCase());
    if (!username && roleCodes.length === 0) return true;
    return username === 'admin' || roleCodes.includes('admin') || roleCodes.includes('role_admin');
  });

  const isNavigableScreen = computed(() => {
    if (!selectedGuide.value || isSystemGuide(selectedGuide.value)) return false;
    const pk = selectedGuide.value.pageKey || '';
    return pk.startsWith('/') && pk !== route.path;
  });

  async function loadAllGuides() {
    loading.value = true;
    try {
      const res = (await getHelpList()) as SysPageHelpModel[] | { records?: SysPageHelpModel[] };
      const records: SysPageHelpModel[] = Array.isArray(res)
        ? res
        : !Array.isArray(res) && Array.isArray(res?.records)
        ? res.records
        : [];
      const active = records.filter((i) => i.status === 'ACTIVE' || !i.status);
      active.sort((a, b) => {
        const typeDiff = (isSystemGuide(a) ? 0 : 1) - (isSystemGuide(b) ? 0 : 1);
        if (typeDiff !== 0) return typeDiff;
        return (a.sortOrder ?? 0) - (b.sortOrder ?? 0);
      });
      guideList.value = active;

      const queryRoute = (route.query.route as string) || (route.query.page as string) || '';
      if (queryRoute) {
        const matched = active.find(
          (item) => item.pageKey === queryRoute || queryRoute.startsWith(item.pageKey + '/'),
        );
        if (matched) {
          selectedGuide.value = matched;
          return;
        }
      }

      if (selectedGuide.value?.pageKey) {
        const refreshed = active.find((item) => item.pageKey === selectedGuide.value?.pageKey);
        if (refreshed) {
          selectedGuide.value = refreshed;
          return;
        }
      }

      // 기본 진입 시 시스템 개요(SYSTEM)를 표지로 우선 노출
      const defaultSystem = active.find((item) => isSystemGuide(item));
      selectedGuide.value = defaultSystem || active[0] || null;
    } finally {
      loading.value = false;
    }
  }

  function selectGuide(item: SysPageHelpModel) {
    selectedGuide.value = item;
  }

  function handleNavigateToScreen(pageKey: string) {
    router.push(pageKey);
  }

  function handleNavigatePath(targetPath: string) {
    if (targetPath && targetPath.startsWith('/')) {
      router.push(targetPath);
    }
  }

  function handleNavigateRelated(rel: RelatedScreenItem) {
    const targetPath = extractRelatedScreenPath(rel);
    if (targetPath) {
      router.push(targetPath);
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

  function handleOpenEditModal(guide: SysPageHelpModel) {
    openEditModal(true, {
      isUpdate: true,
      record: guide,
    });
  }

  function handleGoManagePage() {
    emit('switch-manage');
    router.replace({ path: HELP_MANAGE_PATH, query: { mode: 'manage' } });
  }

  watch(
    () => [route.query.route, route.query.page],
    ([newRoute, newPage]) => {
      const target = (newRoute as string) || (newPage as string);
      if (target) {
        const matched = guideList.value.find((item) => item.pageKey === target);
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
    align-items: start;
  }

  .help-content-panel {
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    padding: 20px 24px;
  }

  .viewer-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    padding-bottom: 12px;
    margin-bottom: 16px;
    border-bottom: 1px solid #f1f5f9;

    .viewer-toolbar-meta {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .viewer-toolbar-title {
      font-size: 13px;
      font-weight: 700;
      color: #0f172a;
    }

    .viewer-toolbar-actions {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }
</style>
