<template>
  <div>
    <BasicTable @register="registerTable">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'logoSvg'">
          <div class="w-24 h-9 rounded bg-slate-50 border border-slate-200 flex items-center justify-center p-1 overflow-hidden mx-auto" v-html="record.logoSvg"></div>
        </template>
        <template v-else-if="column.key === 'siteSlug'">
          <span class="font-mono text-xs font-bold text-indigo-600">/s/{{ record.siteSlug }}</span>
        </template>
        <template v-else-if="column.key === 'siteType'">
          <Tag :color="record.siteType === 'COMMERCE' ? 'blue' : record.siteType === 'HYBRID' ? 'purple' : 'green'">
            {{ record.siteType === 'COMMERCE' ? '쇼핑몰 스토어' : record.siteType === 'HYBRID' ? '매거진+스토어 복합' : '브랜드·커뮤니티 웹' }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'deployMode'">
          <Tag :color="record.deployMode === 'CUSTOM_BUNDLE' ? 'purple' : 'cyan'">
            {{ record.deployMode === 'CUSTOM_BUNDLE' ? '코드 번들 배포' : '즉시 디자인 배포' }} (v{{ record.activeVersionNo }})
          </Tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <Tag :color="record.status === 'ACTIVE' ? 'success' : 'error'">
            {{ record.status === 'ACTIVE' ? '정상 가동' : '일시 정지' }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                label: record.status === 'ACTIVE' ? '사이트 정지' : '사이트 재개',
                icon: record.status === 'ACTIVE' ? 'ant-design:stop-outlined' : 'ant-design:check-circle-outlined',
                color: record.status === 'ACTIVE' ? 'warning' : 'success',
                popConfirm: {
                  title: record.status === 'ACTIVE' ? '이 멀티사이트를 일시 정지하시겠습니까?' : '이 멀티사이트를 다시 활성화하시겠습니까?',
                  confirm: handleToggleStatus.bind(null, record),
                },
              },
              {
                label: 'v1 롤백',
                icon: 'ant-design:rollback-outlined',
                ifShow: () => record.activeVersionNo > 1,
                popConfirm: {
                  title: '이 사이트의 릴리즈를 v1으로 즉시 롤백하시겠습니까?',
                  confirm: handleRollback.bind(null, record),
                },
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" setup>
  import { Tag } from 'ant-design-vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getAdminSiteList, updateAdminSiteStatus, rollbackAdminSiteRelease } from '/@/api/kkumz';

  const { createMessage } = useMessage();

  const [registerTable, { reload }] = useTable({
    title: '생성 사이트 목록',
    api: getAdminSiteList,
    rowKey: 'id',
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    formConfig: {
      labelWidth: 90,
      schemas: [
        {
          field: 'keyword',
          label: '검색어',
          component: 'Input',
          componentProps: { placeholder: '브랜드명, 슬러그 검색' },
          colProps: { span: 8 },
        },
        {
          field: 'status',
          label: '운영 상태',
          component: 'Select',
          componentProps: {
            options: [
              { label: '전체', value: '' },
              { label: '정상 가동', value: 'ACTIVE' },
              { label: '일시 정지', value: 'SUSPENDED' },
            ],
          },
          colProps: { span: 8 },
        },
      ],
    },
    columns: [
      { title: '로고', dataIndex: 'logoSvg', key: 'logoSvg', width: 120 },
      { title: '브랜드명', dataIndex: 'brandName', width: 170 },
      { title: '멀티사이트 경로', dataIndex: 'siteSlug', key: 'siteSlug', width: 150 },
      { title: '사이트 유형', dataIndex: 'siteType', key: 'siteType', width: 150 },
      { title: '배포 방식 / 버전', dataIndex: 'deployMode', key: 'deployMode', width: 170 },
      { title: '오너 계정', dataIndex: 'ownerUsername', width: 120 },
      { title: '조회수', dataIndex: 'viewCount', width: 90 },
      { title: '좋아요', dataIndex: 'likeCount', width: 90 },
      { title: '상태', dataIndex: 'status', key: 'status', width: 150 },
    ],
    actionColumn: {
      width: 190,
      title: '관리',
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  });

  async function handleToggleStatus(record: Recordable) {
    const nextStatus = record.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    await updateAdminSiteStatus(record.id, { status: nextStatus });
    createMessage.success(`사이트 상태가 ${nextStatus}(으)로 변경되었습니다.`);
    reload();
  }

  async function handleRollback(record: Recordable) {
    await rollbackAdminSiteRelease(record.siteSlug, 1);
    createMessage.success(`${record.brandName} 사이트가 v1 릴리즈로 롤백되었습니다.`);
    reload();
  }
</script>
