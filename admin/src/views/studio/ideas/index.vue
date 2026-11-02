<template>
  <div>
    <BasicTable @register="registerTable">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'logoSvg'">
          <div class="w-24 h-9 rounded bg-slate-50 border border-slate-200 flex items-center justify-center p-1 overflow-hidden mx-auto" v-html="record.logoSvg"></div>
        </template>
        <template v-else-if="column.key === 'siteType'">
          <Tag :color="record.siteType === 'COMMERCE' ? 'blue' : record.siteType === 'HYBRID' ? 'purple' : 'green'">
            {{ record.siteType === 'COMMERCE' ? '쇼핑몰 스토어' : record.siteType === 'HYBRID' ? '매거진+스토어 복합' : '브랜드·커뮤니티 웹' }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <Tag :color="record.status === 'DEPLOYED' ? 'success' : 'processing'">
            {{ record.status }} (v{{ record.currentVersionNo }})
          </Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                label: '기획서 보기',
                icon: 'ant-design:eye-outlined',
                onClick: handleDetail.bind(null, record),
              },
              {
                label: '삭제',
                icon: 'ant-design:delete-outlined',
                color: 'error',
                popConfirm: {
                  title: '해당 아이디어 프로젝트를 삭제하시겠습니까?',
                  confirm: handleDelete.bind(null, record),
                },
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>
    <IdeaDetailModal @register="registerDetailModal" />
  </div>
</template>

<script lang="ts" setup>
  import { Tag } from 'ant-design-vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getAdminIdeaList, deleteAdminIdea } from '/@/api/kkumz';
  import IdeaDetailModal from './components/IdeaDetailModal.vue';

  const { createMessage } = useMessage();
  const [registerDetailModal, { openModal: openDetailModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: '아이디어·기획서 목록',
    api: getAdminIdeaList,
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
          componentProps: { placeholder: '브랜드명, 제목, 프롬프트 검색' },
          colProps: { span: 8 },
        },
        {
          field: 'siteType',
          label: '사이트 유형',
          component: 'Select',
          componentProps: {
            options: [
              { label: '전체', value: '' },
              { label: '쇼핑몰 스토어', value: 'COMMERCE' },
              { label: '브랜드·커뮤니티 웹', value: 'CMS_SITE' },
              { label: '매거진+스토어 복합', value: 'HYBRID' },
            ],
          },
          colProps: { span: 8 },
        },
      ],
    },
    columns: [
      { title: '로고', dataIndex: 'logoSvg', key: 'logoSvg', width: 120 },
      { title: '브랜드명', dataIndex: 'selectedBrandName', width: 160 },
      { title: '슬러그', dataIndex: 'siteSlug', width: 140 },
      { title: '아이디어 제목', dataIndex: 'title', width: 260 },
      { title: '사이트 유형', dataIndex: 'siteType', key: 'siteType', width: 150 },
      { title: '작성자', dataIndex: 'authorDisplayName', width: 120 },
      { title: '상태/버전', dataIndex: 'status', key: 'status', width: 130 },
      { title: '생성일시', dataIndex: 'createdAt', width: 160 },
    ],
    actionColumn: {
      width: 170,
      title: '관리',
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  });

  function handleDetail(record: Recordable) {
    openDetailModal(true, { record });
  }

  async function handleDelete(record: Recordable) {
    await deleteAdminIdea(record.id);
    createMessage.success('아이디어 프로젝트가 삭제되었습니다.');
    reload();
  }
</script>
