<template>
  <div>
    <BasicTable @register="registerTable">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'reasonCategory'">
          <Tag color="purple">{{ record.reasonCategory }}</Tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <Tag :color="record.status === 'PENDING' ? 'warning' : record.status === 'RESOLVED' ? 'success' : 'default'">
            {{ record.status === 'PENDING' ? '심사 대기' : record.status === 'RESOLVED' ? '조치 완료' : '반려' }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                label: '심사/조치',
                icon: 'ant-design:audit-outlined',
                onClick: handleResolve.bind(null, record),
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>
    <ModerationResolveModal @register="registerModal" @success="reload" />
  </div>
</template>

<script lang="ts" setup>
  import { Tag } from 'ant-design-vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { getAdminModerationList } from '/@/api/kkumz';
  import ModerationResolveModal from './components/ModerationResolveModal.vue';

  const [registerModal, { openModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: '신고·제재 관리',
    api: getAdminModerationList,
    rowKey: 'id',
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    formConfig: {
      labelWidth: 90,
      schemas: [
        {
          field: 'status',
          label: '처리 상태',
          component: 'Select',
          componentProps: {
            options: [
              { label: '전체', value: '' },
              { label: '심사 대기', value: 'PENDING' },
              { label: '조치 완료', value: 'RESOLVED' },
              { label: '반려', value: 'DISMISSED' },
            ],
          },
          colProps: { span: 8 },
        },
      ],
    },
    columns: [
      { title: '신고 ID', dataIndex: 'id', width: 120 },
      { title: '대상 브랜드', dataIndex: 'brandName', width: 160 },
      { title: '슬러그', dataIndex: 'siteSlug', width: 130 },
      { title: '신고 유형', dataIndex: 'reasonCategory', key: 'reasonCategory', width: 130 },
      { title: '상세 신고 사유', dataIndex: 'reasonDetail', width: 260 },
      { title: '신고자', dataIndex: 'reporterUsername', width: 120 },
      { title: '상태', dataIndex: 'status', key: 'status', width: 140 },
      { title: '심사 의견', dataIndex: 'resolutionNote', width: 200 },
      { title: '접수일시', dataIndex: 'createdAt', width: 150 },
    ],
    actionColumn: {
      width: 130,
      title: '심사',
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  });

  function handleResolve(record: Recordable) {
    openModal(true, { record });
  }
</script>
