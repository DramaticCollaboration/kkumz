<template>
  <div>
    <BasicTable @register="registerTable">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <Tag :color="record.status === 'SUCCESS' ? 'success' : record.status === 'FAILED' ? 'error' : 'processing'">
            {{ record.status }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'triggerType'">
          <Tag color="blue">{{ record.triggerType }}</Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                label: '빌드 로그',
                icon: 'ant-design:code-outlined',
                onClick: handleOpenLog.bind(null, record),
              },
              {
                label: 'AI 빌드 패치',
                icon: 'ant-design:robot-outlined',
                ifShow: () => record.status === 'FAILED',
                onClick: handleAiFix.bind(null, record),
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>
    <BuildLogModal @register="registerLogModal" />
  </div>
</template>

<script lang="ts" setup>
  import { Tag } from 'ant-design-vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getAdminBuildList, triggerAdminAiFixBuild } from '/@/api/kkumz';
  import BuildLogModal from './components/BuildLogModal.vue';

  const { createMessage } = useMessage();
  const [registerLogModal, { openModal: openLogModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: '빌드·배포 로그',
    api: getAdminBuildList,
    rowKey: 'id',
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    formConfig: {
      labelWidth: 90,
      schemas: [
        {
          field: 'status',
          label: '빌드 상태',
          component: 'Select',
          componentProps: {
            options: [
              { label: '전체', value: '' },
              { label: '성공', value: 'SUCCESS' },
              { label: '실패', value: 'FAILED' },
            ],
          },
          colProps: { span: 8 },
        },
      ],
    },
    columns: [
      { title: '빌드 ID', dataIndex: 'id', width: 130 },
      { title: '브랜드명', dataIndex: 'brandName', width: 160 },
      { title: '사이트 슬러그', dataIndex: 'siteSlug', width: 140 },
      { title: '버전', dataIndex: 'versionNo', width: 70 },
      { title: '트리거 유형', dataIndex: 'triggerType', key: 'triggerType', width: 150 },
      { title: '상태', dataIndex: 'status', key: 'status', width: 100 },
      { title: '소요시간(ms)', dataIndex: 'durationMs', width: 110 },
      { title: '에러 요약', dataIndex: 'errorSummary', width: 240 },
      { title: '실행일시', dataIndex: 'createdAt', width: 160 },
    ],
    actionColumn: {
      width: 190,
      title: '관리',
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  });

  function handleOpenLog(record: Recordable) {
    openLogModal(true, { record });
  }

  async function handleAiFix(record: Recordable) {
    await triggerAdminAiFixBuild(record.siteSlug, record.errorSummary);
    createMessage.success('AI 자동 빌드 오류 패치 및 재빌드가 완료되었습니다.');
    reload();
  }
</script>
