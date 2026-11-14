import { BasicColumn, FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';
import {
  DataLineageModel,
  MetricFormulaItem,
  PageFaqItem,
  PageFeatureItem,
  PageStepItem,
  RelatedScreenItem,
  SysPageHelpModel,
} from './help.api';

export const columns: BasicColumn[] = [
  {
    title: '정렬',
    dataIndex: 'sortOrder',
    width: 70,
    align: 'center',
    customRender: ({ record }) => record.sortOrder ?? 0,
  },
  {
    title: '분류',
    dataIndex: 'category',
    width: 130,
    align: 'center',
    customRender: ({ text }) => h(Tag, { color: 'blue' }, () => text || '일반'),
  },
  {
    title: '가이드 / 화면명',
    dataIndex: 'title',
    width: 220,
    align: 'left',
  },
  {
    title: '라우트 경로',
    dataIndex: 'pageKey',
    width: 210,
    align: 'left',
    customRender: ({ text }) => h('code', { class: 'px-1.5 py-0.5 bg-gray-100 rounded text-xs font-mono text-blue-600' }, text),
  },
  {
    title: '핵심 요약',
    dataIndex: 'summary',
    align: 'left',
  },
  {
    title: '앱 코드',
    dataIndex: 'appCode',
    width: 110,
    align: 'center',
    customRender: ({ text }) => h(Tag, { color: 'default' }, () => text || '-'),
  },
  {
    title: '상태',
    dataIndex: 'status',
    width: 90,
    align: 'center',
    customRender: ({ record }) => {
      const isActive = record.status === 'ACTIVE' || record.status === 1;
      return h(Tag, { color: isActive ? 'success' : 'default' }, () => (isActive ? '활성' : '비활성'));
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'title',
    label: '화면/가이드명',
    component: 'Input',
    alwaysShow: true,
    componentProps: {
      placeholder: '화면명 검색',
      allowClear: true,
    },
    colProps: { span: 6 },
  },
  {
    field: 'pageKey',
    label: '라우트 경로',
    component: 'Input',
    alwaysShow: true,
    componentProps: {
      placeholder: '예: /llm/models',
      allowClear: true,
    },
    colProps: { span: 6 },
  },
  {
    field: 'category',
    label: '분류',
    component: 'Input',
    componentProps: {
      placeholder: '분류명 검색',
      allowClear: true,
    },
    colProps: { span: 6 },
  },
  {
    field: 'status',
    label: '상태',
    component: 'Select',
    alwaysShow: true,
    componentProps: {
      placeholder: '전체 상태',
      allowClear: true,
      options: [
        { label: '활성', value: 'ACTIVE' },
        { label: '비활성', value: 'INACTIVE' },
      ],
    },
    colProps: { span: 6 },
  },
];

export function parseJsonArray<T = any>(raw: any): T[] {
  if (!raw) return [];
  if (Array.isArray(raw)) return raw;
  if (typeof raw === 'string') {
    try {
      const parsed = JSON.parse(raw);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }
  return [];
}

export function parseLineage(record?: Partial<SysPageHelpModel> | null): Required<DataLineageModel> {
  if (!record) return { sources: [], destinations: [] };
  let obj: any = record.lineage;
  if (!obj && record.lineageJson) {
    try {
      obj = typeof record.lineageJson === 'string' ? JSON.parse(record.lineageJson) : record.lineageJson;
    } catch {
      obj = {};
    }
  }
  return {
    sources: Array.isArray(obj?.sources) ? obj.sources : [],
    destinations: Array.isArray(obj?.destinations) ? obj.destinations : [],
  };
}

export function parseHelpRecord(record?: Partial<SysPageHelpModel> | null) {
  const r = record || {};
  return {
    features: parseJsonArray<PageFeatureItem>(r.features ?? r.featuresJson),
    steps: parseJsonArray<PageStepItem>(r.steps ?? r.stepsJson),
    formulas: parseJsonArray<MetricFormulaItem>(r.formulas ?? r.formulasJson),
    relatedScreens: parseJsonArray<RelatedScreenItem>(r.relatedScreens ?? r.relatedScreensJson),
    tips: parseJsonArray<string>(r.tips ?? r.tipsJson),
    warnings: parseJsonArray<string>(r.warnings ?? r.warningsJson),
    faq: parseJsonArray<PageFaqItem>(r.faq ?? r.faqJson),
    lineage: parseLineage(r),
  };
}
