<template>
  <PageWrapper>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-space>
          <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate">
            {{ t('appUi.k30866a6b') }}
          </a-button>
          <a-button
            v-if="selectedRowKeys.length > 0"
            danger
            preIcon="ant-design:delete-outlined"
            @click="handleBatchDelete"
          >
            {{ t('appUi.k6dac28ac') }}{{ selectedRowKeys.length }})
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
              label: t('appUi.kdd6c8185'),
              onClick: handleTest.bind(null, record),
            },
            {
              label: t('appUi.k5c8631b6'),
              onClick: handleHistory.bind(null, record),
            },
            {
              label: t('appUi.kfc81e222'),
              color: 'error',
              popConfirm: {
                title: t('appUi.k651f172f'),
                confirm: handleDelete.bind(null, record),
              },
            },
          ]"
        />
      </template>
    </BasicTable>

    <PromptEditModal @register="registerEditModal" @success="handleSuccess" />
    <PromptTestDrawer @register="registerTestDrawer" />
    <PromptHistoryModal @register="registerHistoryModal" @success="handleSuccess" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useDrawer } from '/@/components/Drawer';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { columns, searchFormSchema } from './prompt.data';
  import {
    getPromptList,
    deletePromptById,
    deleteBatchPrompt,
    SysPromptModel,
  } from './prompt.api';
  import PromptEditModal from './components/PromptEditModal.vue';
  import PromptTestDrawer from './components/PromptTestDrawer.vue';
  import PromptHistoryModal from './components/PromptHistoryModal.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const { createMessage, createConfirm } = useMessage();
  const selectedRowKeys = ref<string[]>([]);

  const [registerEditModal, { openModal: openEditModal }] = useModal();
  const [registerTestDrawer, { openDrawer: openTestDrawer }] = useDrawer();
  const [registerHistoryModal, { openModal: openHistoryModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: t('appUi.k87cacb6b'),
    api: async (params) => {
      const res: any = await getPromptList(params);
      const list: SysPromptModel[] = Array.isArray(res)
        ? res
        : Array.isArray(res?.records)
        ? res.records
        : [];
      return list;
    },
    rowKey: 'id',
    columns,
    formConfig: {
      labelWidth: 90,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: false,
    actionColumn: {
      width: 220,
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

  function handleCreate() {
    openEditModal(true, { isUpdate: false });
  }

  function handleEdit(record: SysPromptModel) {
    openEditModal(true, { isUpdate: true, record });
  }

  function handleTest(record: SysPromptModel) {
    openTestDrawer(true, { record });
  }

  function handleHistory(record: SysPromptModel) {
    openHistoryModal(true, { record });
  }

  async function handleDelete(record: SysPromptModel) {
    if (!record.id) return;
    await deletePromptById(record.id);
    createMessage.success(t('appUi.k203b7989'));
    reload();
  }

  function handleBatchDelete() {
    if (selectedRowKeys.value.length === 0) return;
    createConfirm({
      iconType: 'warning',
      title: t('appUi.k28b1f14e'),
      content: t('appUi.kd59e7829', { p0: selectedRowKeys.value.length }),
      onOk: async () => {
        await deleteBatchPrompt(selectedRowKeys.value);
        createMessage.success(t('appUi.kd0b117e1'));
        selectedRowKeys.value = [];
        reload();
      },
    });
  }

  function handleSuccess() {
    reload();
  }
</script>
