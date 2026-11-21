<template>
  <div>
    <BasicTable @register="registerTable">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'logoSvg'">
          <div class="w-24 h-9 rounded bg-slate-50 border border-slate-200 flex items-center justify-center p-1 overflow-hidden mx-auto" v-html="record.logoSvg"></div>
        </template>
        <template v-else-if="column.key === 'siteType'">
          <Tag :color="record.siteType === 'COMMERCE' ? 'blue' : record.siteType === 'HYBRID' ? 'default' : 'green'">
            {{ record.siteType === 'COMMERCE' ? t('appUi.kfe990b2f') : record.siteType === 'HYBRID' ? t('appUi.k55479f2f') : t('appUi.k59d1dd90') }}
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
                label: t('appUi.k1a7a1ad2'),
                icon: 'ant-design:eye-outlined',
                onClick: handleDetail.bind(null, record),
              },
              {
                label: t('appUi.kfc81e222'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                popConfirm: {
                  title: t('appUi.kc2172bf9'),
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
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const { createMessage } = useMessage();
  const [registerDetailModal, { openModal: openDetailModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: t('appUi.k9140c8ef'),
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
          label: t('appUi.kd64280cb'),
          component: 'Input',
          componentProps: { placeholder: t('appUi.kf69498b7') },
          colProps: { span: 8 },
        },
        {
          field: 'siteType',
          label: t('appUi.k73456d62'),
          component: 'JDictSelectTag',
          componentProps: {
            dictCode: 'kkumz_site_type',
            placeholder: t('appUi.k934dd25e'),
          },
          colProps: { span: 8 },
        },
      ],
    },
    columns: [
      { title: t('appUi.kd2ba3ac7'), dataIndex: 'logoSvg', key: 'logoSvg', width: 120 },
      { title: t('appUi.ke4171b02'), dataIndex: 'selectedBrandName', width: 160 },
      { title: t('appUi.k40c5662e'), dataIndex: 'siteSlug', width: 140 },
      { title: t('appUi.ke4057e06'), dataIndex: 'title', width: 260 },
      { title: t('appUi.k73456d62'), dataIndex: 'siteType', key: 'siteType', width: 150 },
      { title: t('appUi.k0be45e6f'), dataIndex: 'authorDisplayName', width: 120 },
      { title: t('appUi.kf34625a7'), dataIndex: 'status', key: 'status', width: 130 },
      { title: t('appUi.k92436d1b'), dataIndex: 'createdAt', width: 160 },
    ],
    actionColumn: {
      width: 170,
      title: t('appUi.kc29fba5a'),
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  });

  function handleDetail(record: Recordable) {
    openDetailModal(true, { record });
  }

  async function handleDelete(record: Recordable) {
    await deleteAdminIdea(record.id);
    createMessage.success(t('appUi.k62234d68'));
    reload();
  }
</script>
