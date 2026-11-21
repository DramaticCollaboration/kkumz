<template>
  <div>
    <BasicTable @register="registerTable">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'logoSvg'">
          <div class="w-24 h-9 rounded bg-slate-50 border border-slate-200 flex items-center justify-center p-1 overflow-hidden mx-auto" v-html="record.logoSvg"></div>
        </template>
        <template v-else-if="column.key === 'siteSlug'">
          <span class="font-mono text-xs font-bold text-slate-700">/s/{{ record.siteSlug }}</span>
        </template>
        <template v-else-if="column.key === 'siteType'">
          <Tag :color="record.siteType === 'COMMERCE' ? 'blue' : record.siteType === 'HYBRID' ? 'default' : 'green'">
            {{ record.siteType === 'COMMERCE' ? t('appUi.kfe990b2f') : record.siteType === 'HYBRID' ? t('appUi.k55479f2f') : t('appUi.k59d1dd90') }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'deployMode'">
          <Tag :color="record.deployMode === 'CUSTOM_BUNDLE' ? 'default' : 'default'">
            {{ record.deployMode === 'CUSTOM_BUNDLE' ? t('appUi.k8bb944fb') : t('appUi.k2f258cb1') }} (v{{ record.activeVersionNo }})
          </Tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <Tag :color="record.status === 'ACTIVE' ? 'success' : 'error'">
            {{ record.status === 'ACTIVE' ? t('appUi.kf567a95a') : t('appUi.k340d4e47') }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                label: record.status === 'ACTIVE' ? t('appUi.k825c8915') : t('appUi.k105ee254'),
                icon: record.status === 'ACTIVE' ? 'ant-design:stop-outlined' : 'ant-design:check-circle-outlined',
                color: record.status === 'ACTIVE' ? 'warning' : 'success',
                popConfirm: {
                  title: record.status === 'ACTIVE' ? t('appUi.k89d3814a') : t('appUi.k2e5ef435'),
                  confirm: handleToggleStatus.bind(null, record),
                },
              },
              {
                label: t('appUi.k31fa871d'),
                icon: 'ant-design:rollback-outlined',
                ifShow: () => record.activeVersionNo > 1,
                popConfirm: {
                  title: t('appUi.k9c88f568'),
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
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const { createMessage } = useMessage();

  const [registerTable, { reload }] = useTable({
    title: t('appUi.k21484471'),
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
          label: t('appUi.kd64280cb'),
          component: 'Input',
          componentProps: { placeholder: t('appUi.k1b8f7ac0') },
          colProps: { span: 8 },
        },
        {
          field: 'status',
          label: t('appUi.k68d30371'),
          component: 'JDictSelectTag',
          componentProps: {
            dictCode: 'kkumz_site_status',
            placeholder: t('appUi.k934dd25e'),
          },
          colProps: { span: 8 },
        },
      ],
    },
    columns: [
      { title: t('appUi.kd2ba3ac7'), dataIndex: 'logoSvg', key: 'logoSvg', width: 120 },
      { title: t('appUi.ke4171b02'), dataIndex: 'brandName', width: 170 },
      { title: t('appUi.k3b4a102e'), dataIndex: 'siteSlug', key: 'siteSlug', width: 150 },
      { title: t('appUi.k73456d62'), dataIndex: 'siteType', key: 'siteType', width: 150 },
      { title: t('appUi.ka39b046b'), dataIndex: 'deployMode', key: 'deployMode', width: 170 },
      { title: t('appUi.k27b3430a'), dataIndex: 'ownerUsername', width: 120 },
      { title: t('appUi.k274b7b80'), dataIndex: 'viewCount', width: 90 },
      { title: t('appUi.k224a2886'), dataIndex: 'likeCount', width: 90 },
      { title: t('appUi.k2926977b'), dataIndex: 'status', key: 'status', width: 150 },
    ],
    actionColumn: {
      width: 190,
      title: t('appUi.kc29fba5a'),
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  });

  async function handleToggleStatus(record: Recordable) {
    const nextStatus = record.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    await updateAdminSiteStatus(record.id, { status: nextStatus });
    createMessage.success(t('appUi.k6fd4fec1', { p0: nextStatus }));
    reload();
  }

  async function handleRollback(record: Recordable) {
    await rollbackAdminSiteRelease(record.siteSlug, 1);
    createMessage.success(t('appUi.kcf3d0419', { p0: record.brandName }));
    reload();
  }
</script>
