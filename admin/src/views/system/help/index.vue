<template>
  <div>
    <HelpViewer v-if="viewMode === 'viewer'" @switch-manage="viewMode = 'manage'" />

    <PageWrapper v-else>
      <BasicTable @register="registerTable" :rowSelection="rowSelection">
        <template #tableTitle>
          <a-space wrap>
            <a-radio-group
              v-model:value="helpTypeFilter"
              button-style="solid"
              size="small"
              @change="handleTypeFilterChange"
            >
              <a-radio-button value="ALL">{{ t('appUi.k934dd25e') }}</a-radio-button>
              <a-radio-button value="SYSTEM">{{ t('appUi.k368c6bad') }}</a-radio-button>
              <a-radio-button value="SCREEN">{{ t('appUi.ka3a0501d') }}</a-radio-button>
            </a-radio-group>
            <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate">
              {{ t('appUi.k4b507cd4') }}
            </a-button>
            <a-button preIcon="ant-design:read-outlined" @click="handleSwitchToViewer">
              {{ t('appUi.k09facaca') }}
            </a-button>
            <a-button
              v-if="selectedRowKeys.length > 0"
              danger
              preIcon="ant-design:delete-outlined"
              @click="handleBatchDelete"
            >
              {{ t('appUi.k308cf507') }}{{ selectedRowKeys.length }})
            </a-button>
          </a-space>
        </template>

        <template #action="{ record }">
          <TableAction
            :actions="[
              {
                label: t('appUi.kd482e14b'),
                onClick: handleEdit.bind(null, record),
              },
              {
                label: t('appUi.k2f1c9d7b'),
                onClick: handlePreview.bind(null, record),
              },
            ]"
            :dropDownActions="[
              {
                label: t('appUi.kfc81e222'),
                color: 'error',
                popConfirm: {
                  title: t('appUi.kcd28522b'),
                  confirm: handleDelete.bind(null, record),
                },
              },
            ]"
          />
        </template>
      </BasicTable>

      <HelpEditModal @register="registerEditModal" @success="handleSuccess" />
      <HelpPreviewModal @register="registerPreviewModal" />
    </PageWrapper>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { PageWrapper } from '/@/components/Page';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { columns, isSystemGuide, searchFormSchema } from './help.data';
  import {
    deleteBatchHelp,
    deleteHelpById,
    getHelpList,
    HELP_MANAGE_PATH,
    SysPageHelpModel,
  } from './help.api';
  import HelpEditModal from './components/HelpEditModal.vue';
  import HelpPreviewModal from './components/HelpPreviewModal.vue';
  import HelpViewer from './HelpViewer.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const route = useRoute();
  const router = useRouter();
  const { createMessage, createConfirm } = useMessage();
  const selectedRowKeys = ref<string[]>([]);
  const helpTypeFilter = ref<'ALL' | 'SYSTEM' | 'SCREEN'>('ALL');

  const viewMode = ref<'viewer' | 'manage'>(
    route.query.route || route.query.mode === 'viewer' ? 'viewer' : 'manage',
  );

  watch(
    () => [route.query.route, route.query.mode],
    ([qRoute, qMode]) => {
      if (qMode === 'manage') {
        viewMode.value = 'manage';
      } else if (qRoute || qMode === 'viewer') {
        viewMode.value = 'viewer';
      }
    },
  );

  const [registerEditModal, { openModal: openEditModal }] = useModal();
  const [registerPreviewModal, { openModal: openPreviewModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: t('appUi.k4b62d0f9'),
    api: async (params) => {
      const res: any = await getHelpList(params);
      const list: SysPageHelpModel[] = Array.isArray(res) ? res : Array.isArray(res?.records) ? res.records : [];

      let filtered = list;
      if (helpTypeFilter.value === 'SYSTEM') {
        filtered = filtered.filter((i) => isSystemGuide(i));
      } else if (helpTypeFilter.value === 'SCREEN') {
        filtered = filtered.filter((i) => !isSystemGuide(i));
      }
      if (params?.title) {
        const kw = String(params.title).toLowerCase();
        filtered = filtered.filter(
          (i) => i.title?.toLowerCase().includes(kw) || i.summary?.toLowerCase().includes(kw),
        );
      }
      if (params?.pageKey) {
        const kw = String(params.pageKey).toLowerCase();
        filtered = filtered.filter((i) => i.pageKey?.toLowerCase().includes(kw));
      }
      if (params?.category) {
        const kw = String(params.category).toLowerCase();
        filtered = filtered.filter((i) => i.category?.toLowerCase().includes(kw));
      }
      if (params?.status) {
        filtered = filtered.filter((i) => i.status === params.status);
      }
      return filtered;
    },
    rowKey: 'id',
    columns,
    formConfig: {
      labelWidth: 100,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: false,
    actionColumn: {
      width: 180,
      title: t('appUi.kc29fba5a'),
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: 'right',
    },
  });

  const rowSelection = computed(() => ({
    type: 'checkbox' as const,
    selectedRowKeys: selectedRowKeys.value,
    onChange: (keys: (string | number)[]) => {
      selectedRowKeys.value = keys.map(String);
    },
  }));

  function handleTypeFilterChange() {
    reload();
  }

  function handleSwitchToViewer() {
    viewMode.value = 'viewer';
    router.replace({ path: HELP_MANAGE_PATH, query: { mode: 'viewer' } });
  }

  function handleCreate() {
    const defaultType = helpTypeFilter.value === 'SYSTEM' ? 'SYSTEM' : 'SCREEN';
    openEditModal(true, {
      isUpdate: false,
      record: { helpType: defaultType },
    });
  }

  function handleEdit(record: SysPageHelpModel) {
    openEditModal(true, { isUpdate: true, record });
  }

  function handlePreview(record: SysPageHelpModel) {
    openPreviewModal(true, { record });
  }

  async function handleDelete(record: SysPageHelpModel) {
    if (!record.id) return;
    await deleteHelpById(record.id);
    createMessage.success(t('appUi.kbcdb0e7c'));
    reload();
  }

  function handleBatchDelete() {
    if (selectedRowKeys.value.length === 0) return;
    createConfirm({
      iconType: 'warning',
      title: t('appUi.k28b1f14e'),
      content: t('appUi.k53bc8f40', { p0: selectedRowKeys.value.length }),
      onOk: async () => {
        await deleteBatchHelp(selectedRowKeys.value);
        createMessage.success(t('appUi.k7a491ebf'));
        selectedRowKeys.value = [];
        reload();
      },
    });
  }

  function handleSuccess() {
    reload();
  }
</script>
