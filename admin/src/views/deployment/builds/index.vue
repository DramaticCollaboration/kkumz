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
                label: t('appUi.kb1640e60'),
                icon: 'ant-design:code-outlined',
                onClick: handleOpenLog.bind(null, record),
              },
              {
                label: t('appUi.k83862f6d'),
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
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const { createMessage } = useMessage();
  const [registerLogModal, { openModal: openLogModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: t('appUi.ke7e9ecc9'),
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
          label: t('appUi.k2ed6100c'),
          component: 'JDictSelectTag',
          componentProps: {
            dictCode: 'kkumz_build_status',
            placeholder: t('appUi.k934dd25e'),
          },
          colProps: { span: 8 },
        },
      ],
    },
    columns: [
      { title: t('appUi.ka87af073'), dataIndex: 'id', width: 130 },
      { title: t('appUi.ke4171b02'), dataIndex: 'brandName', width: 160 },
      { title: t('appUi.k6b6d08bf'), dataIndex: 'siteSlug', width: 140 },
      { title: t('appUi.k593f8a81'), dataIndex: 'versionNo', width: 70 },
      { title: t('appUi.k05bb91f8'), dataIndex: 'triggerType', key: 'triggerType', width: 150 },
      { title: t('appUi.k2926977b'), dataIndex: 'status', key: 'status', width: 100 },
      { title: t('appUi.k6eceb048'), dataIndex: 'durationMs', width: 110 },
      { title: t('appUi.k87bc82e6'), dataIndex: 'errorSummary', width: 240 },
      { title: t('appUi.k87ff2218'), dataIndex: 'createdAt', width: 160 },
    ],
    actionColumn: {
      width: 190,
      title: t('appUi.kc29fba5a'),
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  });

  function handleOpenLog(record: Recordable) {
    openLogModal(true, { record });
  }

  async function handleAiFix(record: Recordable) {
    await triggerAdminAiFixBuild(record.siteSlug, record.errorSummary);
    createMessage.success(t('appUi.kb91d5b97'));
    reload();
  }
</script>
