<template>
  <PageWrapper>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-space>
          <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate">
            신규 가이드 등록
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
              label: '미리보기',
              onClick: handlePreview.bind(null, record),
            },
            {
              label: '편집',
              onClick: handleEdit.bind(null, record),
            },
            {
              label: '삭제',
              color: 'error',
              popConfirm: {
                title: '해당 화면 가이드를 삭제하시겠습니까?',
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
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { columns, searchFormSchema } from './help.data';
  import { getHelpList, deleteHelpById, deleteBatchHelp, SysPageHelpModel } from './help.api';
  import HelpEditModal from './components/HelpEditModal.vue';
  import HelpPreviewModal from './components/HelpPreviewModal.vue';

  const { createMessage, createConfirm } = useMessage();
  const selectedRowKeys = ref<string[]>([]);

  const [registerEditModal, { openModal: openEditModal }] = useModal();
  const [registerPreviewModal, { openModal: openPreviewModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: '화면별 도움말 가이드 목록',
    api: async (params) => {
      const res: any = await getHelpList(params);
      const list: SysPageHelpModel[] = Array.isArray(res) ? res : Array.isArray(res?.records) ? res.records : [];

      let filtered = list;
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

  function handleEdit(record: SysPageHelpModel) {
    openEditModal(true, { isUpdate: true, record });
  }

  function handlePreview(record: SysPageHelpModel) {
    openPreviewModal(true, { record });
  }

  async function handleDelete(record: SysPageHelpModel) {
    if (!record.id) return;
    await deleteHelpById(record.id);
    createMessage.success('가이드가 삭제되었습니다.');
    reload();
  }

  function handleBatchDelete() {
    if (selectedRowKeys.value.length === 0) return;
    createConfirm({
      iconType: 'warning',
      title: '일괄 삭제 확인',
      content: `선택한 ${selectedRowKeys.value.length}건의 도움말 가이드를 삭제하시겠습니까?`,
      onOk: async () => {
        await deleteBatchHelp(selectedRowKeys.value);
        createMessage.success('선택한 가이드가 일괄 삭제되었습니다.');
        selectedRowKeys.value = [];
        reload();
      },
    });
  }

  function handleSuccess() {
    reload();
  }
</script>
