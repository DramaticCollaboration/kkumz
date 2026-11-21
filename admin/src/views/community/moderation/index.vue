<template>
  <div>
    <BasicTable @register="registerTable">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'reasonCategory'">
          <Tag color="default">{{ record.reasonCategory }}</Tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <Tag :color="record.status === 'PENDING' ? 'warning' : record.status === 'RESOLVED' ? 'success' : 'default'">
            {{ record.status === 'PENDING' ? t('appUi.k5270f81b') : record.status === 'RESOLVED' ? t('appUi.k18dd1c90') : t('appUi.kd747407a') }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                label: t('appUi.k330fe5b5'),
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
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const [registerModal, { openModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: t('appUi.k6e0bbb84'),
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
          label: t('appUi.kf845e3fb'),
          component: 'JDictSelectTag',
          componentProps: {
            dictCode: 'kkumz_moderation_status',
            placeholder: t('appUi.k934dd25e'),
          },
          colProps: { span: 8 },
        },
      ],
    },
    columns: [
      { title: t('appUi.kb8bb19d8'), dataIndex: 'id', width: 120 },
      { title: t('appUi.k55e1d4c0'), dataIndex: 'brandName', width: 160 },
      { title: t('appUi.k40c5662e'), dataIndex: 'siteSlug', width: 130 },
      { title: t('appUi.k95baa4f6'), dataIndex: 'reasonCategory', key: 'reasonCategory', width: 130 },
      { title: t('appUi.kbcd6a5b7'), dataIndex: 'reasonDetail', width: 260 },
      { title: t('appUi.k4f5f63a0'), dataIndex: 'reporterUsername', width: 120 },
      { title: t('appUi.k2926977b'), dataIndex: 'status', key: 'status', width: 140 },
      { title: t('appUi.k9240fc60'), dataIndex: 'resolutionNote', width: 200 },
      { title: t('appUi.kefe20c17'), dataIndex: 'createdAt', width: 150 },
    ],
    actionColumn: {
      width: 130,
      title: t('appUi.k7e8360b9'),
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  });

  function handleResolve(record: Recordable) {
    openModal(true, { record });
  }
</script>
