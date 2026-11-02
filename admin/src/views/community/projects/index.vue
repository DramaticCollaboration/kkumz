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
        <template v-else-if="column.key === 'recruits'">
          <div class="flex flex-wrap gap-1">
            <Tag v-for="rec in record.openRecruits || []" :key="rec.id" color="indigo">
              {{ rec.positionRole }} ({{ rec.currentCount }}/{{ rec.headcount }})
            </Tag>
            <span v-if="!record.openRecruits?.length" class="text-xs text-slate-400">모집 마감</span>
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

  const [registerTable] = useTable({
    title: '쇼케이스·팀빌딩 목록',
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
          label: '검색어',
          component: 'Input',
          componentProps: { placeholder: '브랜드명, 슬로건 검색' },
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
      { title: '브랜드명', dataIndex: 'brandName', width: 160 },
      { title: '슬로건', dataIndex: 'tagline', width: 260 },
      { title: '사이트 유형', dataIndex: 'siteType', key: 'siteType', width: 140 },
      { title: '모집 중 포지션', dataIndex: 'openRecruits', key: 'recruits', width: 220 },
      { title: '좋아요', dataIndex: 'likeCount', width: 85 },
      { title: '피드백수', dataIndex: 'commentCount', width: 85 },
      { title: '조회수', dataIndex: 'viewCount', width: 90 },
      { title: '오너', dataIndex: 'authorDisplayName', width: 110 },
    ],
  });
</script>
