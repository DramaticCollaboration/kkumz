<template>
  <div>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate">+ 신규 프롬프트 템플릿 등록</a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'stageType'">
          <Tag color="blue">{{ record.stageType }}</Tag>
        </template>
        <template v-else-if="column.key === 'forbiddenKeywords'">
          <div class="flex flex-wrap gap-1">
            <Tag v-for="kw in record.forbiddenKeywords || []" :key="kw" color="red">{{ kw }}</Tag>
          </div>
        </template>
        <template v-else-if="column.key === 'status'">
          <Tag :color="record.status === 'ACTIVE' ? 'success' : 'default'">
            {{ record.status }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                label: '편집',
                icon: 'ant-design:edit-outlined',
                onClick: handleEdit.bind(null, record),
              },
              {
                label: '삭제',
                icon: 'ant-design:delete-outlined',
                color: 'error',
                popConfirm: {
                  title: '해당 AI 프롬프트 템플릿을 삭제하시겠습니까?',
                  confirm: handleDelete.bind(null, record),
                },
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>
    <PromptModal @register="registerModal" @success="reload" />
  </div>
</template>

<script lang="ts" setup>
  import { Tag } from 'ant-design-vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getAdminPromptList, deleteAdminPrompt } from '/@/api/kkumz';
  import PromptModal from './components/PromptModal.vue';

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: '프롬프트·금지어 설정',
    api: getAdminPromptList,
    rowKey: 'id',
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    formConfig: {
      labelWidth: 95,
      schemas: [
        {
          field: 'stageType',
          label: '스튜디오 단계',
          component: 'Select',
          componentProps: {
            options: [
              { label: '전체', value: '' },
              { label: '브랜드 네이밍 (NAMING)', value: 'NAMING' },
              { label: '벡터 SVG 로고 (LOGO_SVG)', value: 'LOGO_SVG' },
              { label: '기능명세서 (FRS_SPEC)', value: 'FRS_SPEC' },
              { label: '프리뷰 수정 (CHAT_REFINE)', value: 'CHAT_REFINE' },
              { label: '빌드 오류 패치 (WEBIDE_FIX)', value: 'WEBIDE_FIX' },
            ],
          },
          colProps: { span: 8 },
        },
      ],
    },
    columns: [
      { title: '템플릿 코드', dataIndex: 'templateCode', width: 170 },
      { title: '단계', dataIndex: 'stageType', key: 'stageType', width: 130 },
      { title: '템플릿 명칭', dataIndex: 'templateName', width: 220 },
      { title: 'AI 모델', dataIndex: 'modelName', width: 160 },
      { title: '브랜드 금지어 필터', dataIndex: 'forbiddenKeywords', key: 'forbiddenKeywords', width: 240 },
      { title: '누적 호출수', dataIndex: 'totalCalls', width: 110 },
      { title: '상태', dataIndex: 'status', key: 'status', width: 100 },
    ],
    actionColumn: {
      width: 150,
      title: '관리',
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  });

  function handleCreate() {
    openModal(true, { isUpdate: false });
  }

  function handleEdit(record: Recordable) {
    openModal(true, { isUpdate: true, record });
  }

  async function handleDelete(record: Recordable) {
    await deleteAdminPrompt(record.id);
    createMessage.success('프롬프트 템플릿이 삭제되었습니다.');
    reload();
  }
</script>
