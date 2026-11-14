<template>
  <PageWrapper>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-space>
          <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate">
            신규 프롬프트 등록
          </a-button>
          <a-button
            v-if="selectedRowKeys.length > 0"
            danger
            preIcon="ant-design:delete-outlined"
            @click="handleBatchDelete"
          >
            선택 일괄 삭제 ({{ selectedRowKeys.length }})
          </a-button>
        </a-space>
      </template>

      <template #action="{ record }">
        <TableAction
          :actions="[
            {
              label: '편집',
              onClick: handleEdit.bind(null, record),
            },
            {
              label: '테스트',
              onClick: handleTest.bind(null, record),
            },
            {
              label: '변경이력',
              onClick: handleHistory.bind(null, record),
            },
            {
              label: '삭제',
              color: 'error',
              popConfirm: {
                title: '해당 프롬프트 템플릿을 삭제하시겠습니까?',
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

  const { createMessage, createConfirm } = useMessage();
  const selectedRowKeys = ref<string[]>([]);

  const [registerEditModal, { openModal: openEditModal }] = useModal();
  const [registerTestDrawer, { openDrawer: openTestDrawer }] = useDrawer();
  const [registerHistoryModal, { openModal: openHistoryModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: '프롬프트 템플릿 목록',
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
      title: '관리',
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
    createMessage.success('프롬프트가 삭제되었습니다.');
    reload();
  }

  function handleBatchDelete() {
    if (selectedRowKeys.value.length === 0) return;
    createConfirm({
      iconType: 'warning',
      title: '일괄 삭제 확인',
      content: `선택한 ${selectedRowKeys.value.length}건의 프롬프트 템플릿을 삭제하시겠습니까?`,
      onOk: async () => {
        await deleteBatchPrompt(selectedRowKeys.value);
        createMessage.success('선택한 프롬프트가 일괄 삭제되었습니다.');
        selectedRowKeys.value = [];
        reload();
      },
    });
  }

  function handleSuccess() {
    reload();
  }
</script>
