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
        <template v-else-if="column.key === 'recruits'">
          <div class="flex flex-wrap gap-1">
            <Tag v-for="rec in record.openRecruits || []" :key="rec.id" color="indigo">
              {{ rec.positionRole }} ({{ rec.currentCount }}/{{ rec.headcount }})
            </Tag>
            <span v-if="!record.openRecruits?.length" class="text-xs text-slate-400">{{ t('appUi.k006b2b95') }}</span>
          </div>
        </template>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" setup>
  import { Tag } from 'ant-design-vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { getAdminProjectList } from '/@/api/kkumz';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const [registerTable] = useTable({
    title: t('appUi.k24455e49'),
    api: getAdminProjectList,
    rowKey: 'siteId',
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
          componentProps: { placeholder: t('appUi.kdf8a83ae') },
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
      { title: t('appUi.ke4171b02'), dataIndex: 'brandName', width: 160 },
      { title: t('appUi.ke5fbbf15'), dataIndex: 'tagline', width: 260 },
      { title: t('appUi.k73456d62'), dataIndex: 'siteType', key: 'siteType', width: 140 },
      { title: t('appUi.k2d08c7fe'), dataIndex: 'openRecruits', key: 'recruits', width: 220 },
      { title: t('appUi.k224a2886'), dataIndex: 'likeCount', width: 85 },
      { title: t('appUi.kbf9193c0'), dataIndex: 'commentCount', width: 85 },
      { title: t('appUi.k274b7b80'), dataIndex: 'viewCount', width: 90 },
      { title: t('appUi.kd987beed'), dataIndex: 'authorDisplayName', width: 110 },
    ],
  });
</script>
