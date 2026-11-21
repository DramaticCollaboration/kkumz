<template>
  <div>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate">{{ t('appUi.kceb6d3c6') }}</a-button>
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
                label: t('appUi.kd482e14b'),
                icon: 'ant-design:edit-outlined',
                onClick: handleEdit.bind(null, record),
              },
              {
                label: t('appUi.kfc81e222'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                popConfirm: {
                  title: t('appUi.kab691740'),
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
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: t('appUi.k55dd4d33'),
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
          label: t('appUi.k5e5da1b7'),
          component: 'JDictSelectTag',
          componentProps: {
            dictCode: 'kkumz_stage_type',
            placeholder: t('appUi.k934dd25e'),
          },
          colProps: { span: 8 },
        },
      ],
    },
    columns: [
      { title: t('appUi.kc071d7b0'), dataIndex: 'templateCode', width: 170 },
      { title: t('appUi.k0e685c7c'), dataIndex: 'stageType', key: 'stageType', width: 130 },
      { title: t('appUi.k513fec17'), dataIndex: 'templateName', width: 220 },
      { title: t('appUi.k6866b6f6'), dataIndex: 'modelName', width: 160 },
      { title: t('appUi.k8ea3e128'), dataIndex: 'forbiddenKeywords', key: 'forbiddenKeywords', width: 240 },
      { title: t('appUi.k8687560a'), dataIndex: 'totalCalls', width: 110 },
      { title: t('appUi.k2926977b'), dataIndex: 'status', key: 'status', width: 100 },
    ],
    actionColumn: {
      width: 150,
      title: t('appUi.kc29fba5a'),
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
    createMessage.success(t('appUi.k12515c11'));
    reload();
  }
</script>
