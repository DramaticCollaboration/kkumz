import { BasicColumn, FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';
import { SysPromptVariableItem } from './prompt.api';

export const CATEGORY_OPTIONS = [
  { label: '에이전트 페르소나', value: 'AGENT' },
  { label: '도메인 분석 및 생성', value: 'ANALYSIS' },
  { label: '고객 응대 및 요약', value: 'CS_AUTOMATION' },
  { label: '자율 도구 라우팅', value: 'MCP_REACT' },
  { label: '콘텐츠 및 카피라이팅', value: 'CONTENT' },
];

const CATEGORY_COLOR_MAP: Record<string, string> = {
  AGENT: 'purple',
  ANALYSIS: 'blue',
  CS_AUTOMATION: 'cyan',
  MCP_REACT: 'geekblue',
  CONTENT: 'green',
};

const CATEGORY_LABEL_MAP: Record<string, string> = {
  AGENT: '에이전트 페르소나',
  ANALYSIS: '도메인 분석 및 생성',
  CS_AUTOMATION: '고객 응대 및 요약',
  MCP_REACT: '자율 도구 라우팅',
  CONTENT: '콘텐츠 및 카피라이팅',
};

export const columns: BasicColumn[] = [
  {
    title: '프롬프트 코드',
    dataIndex: 'promptCode',
    width: 230,
    align: 'left',
    customRender: ({ text }) =>
      h('span', { class: 'font-mono text-xs font-semibold text-blue-700' }, text),
  },
  {
    title: '프롬프트 명칭',
    dataIndex: 'promptName',
    width: 220,
    align: 'left',
  },
  {
    title: '분류',
    dataIndex: 'category',
    width: 140,
    customRender: ({ text }) => {
      const color = CATEGORY_COLOR_MAP[text] || 'default';
      const label = CATEGORY_LABEL_MAP[text] || text || '일반';
      return h(Tag, { color }, () => label);
    },
  },
  {
    title: '호출 클래스',
    dataIndex: 'callerClass',
    width: 220,
    align: 'left',
    customRender: ({ text }) =>
      h('span', { class: 'font-mono text-xs text-gray-600' }, text || '-'),
  },
  {
    title: '바인딩 변수',
    dataIndex: 'variablesSchema',
    width: 190,
    align: 'left',
    customRender: ({ record }) => {
      const vars = parseVariablesSchema(record.variablesSchema || record.variables);
      if (!vars.length) {
        return h('span', { class: 'text-xs text-gray-400' }, '변수 없음');
      }
      return h(
        'div',
        { class: 'flex flex-wrap gap-1' },
        vars.slice(0, 4).map((v) =>
          h(
            Tag,
            { color: v.required ? 'processing' : 'default', class: 'font-mono text-xs m-0' },
            () => `{{${v.name}}}`,
          ),
        ),
      );
    },
  },
  {
    title: '버전',
    dataIndex: 'version',
    width: 80,
    customRender: ({ text }) => h(Tag, { color: 'blue' }, () => `v${text || 1}`),
  },
  {
    title: '온도값',
    dataIndex: 'temperature',
    width: 85,
    customRender: ({ text }) => (text !== undefined && text !== null ? Number(text).toFixed(2) : '0.20'),
  },
  {
    title: '상태',
    dataIndex: 'status',
    width: 90,
    customRender: ({ text }) => {
      const active = text === 'ACTIVE';
      return h(Tag, { color: active ? 'success' : 'default' }, () => (active ? '활성' : '비활성'));
    },
  },
  {
    title: '최종 수정일시',
    dataIndex: 'updateTime',
    width: 165,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'keyword',
    label: '통합 검색',
    component: 'Input',
    componentProps: {
      placeholder: '프롬프트 코드, 명칭, 호출 클래스 검색',
    },
    colProps: { span: 8 },
  },
  {
    field: 'category',
    label: '분류',
    component: 'Select',
    componentProps: {
      placeholder: '전체 분류',
      allowClear: true,
      options: CATEGORY_OPTIONS,
    },
    colProps: { span: 6 },
  },
  {
    field: 'status',
    label: '상태',
    component: 'Select',
    componentProps: {
      placeholder: '전체 상태',
      allowClear: true,
      options: [
        { label: '활성', value: 'ACTIVE' },
        { label: '비활성', value: 'INACTIVE' },
      ],
    },
    colProps: { span: 5 },
  },
];

export function parseVariablesSchema(raw: any): SysPromptVariableItem[] {
  if (!raw) return [];
  if (Array.isArray(raw)) {
    return raw.map((item) => ({
      name: String(item?.name || ''),
      label: item?.label ? String(item.label) : '',
      required: item?.required !== false,
      sampleValue: item?.sampleValue !== undefined ? String(item.sampleValue) : '',
      description: item?.description ? String(item.description) : '',
    }));
  }
  if (typeof raw === 'string') {
    try {
      const parsed = JSON.parse(raw);
      return Array.isArray(parsed) ? parseVariablesSchema(parsed) : [];
    } catch {
      return [];
    }
  }
  return [];
}
