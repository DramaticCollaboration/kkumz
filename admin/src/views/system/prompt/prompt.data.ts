import { $t } from '/@/locales';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';
import { SysPromptVariableItem } from './prompt.api';

export const CATEGORY_OPTIONS = [
  { label: $t('appUi.k9f778842'), value: 'AGENT' },
  { label: $t('appUi.kb6c8656e'), value: 'ANALYSIS' },
  { label: $t('appUi.ke9300105'), value: 'CS_AUTOMATION' },
  { label: $t('appUi.k7c7881d5'), value: 'MCP_REACT' },
  { label: $t('appUi.k5de84edf'), value: 'CONTENT' },
];

const CATEGORY_COLOR_MAP: Record<string, string> = {
  AGENT: 'purple',
  ANALYSIS: 'blue',
  CS_AUTOMATION: 'cyan',
  MCP_REACT: 'geekblue',
  CONTENT: 'green',
};

const CATEGORY_LABEL_MAP: Record<string, string> = {
  AGENT: $t('appUi.k9f778842'),
  ANALYSIS: $t('appUi.kb6c8656e'),
  CS_AUTOMATION: $t('appUi.ke9300105'),
  MCP_REACT: $t('appUi.k7c7881d5'),
  CONTENT: $t('appUi.k5de84edf'),
};

export const columns: BasicColumn[] = [
  {
    title: $t('appUi.k5bef10a7'),
    dataIndex: 'promptCode',
    width: 230,
    align: 'left',
    customRender: ({ text }) =>
      h('span', { class: 'font-mono text-xs font-semibold text-blue-700' }, text),
  },
  {
    title: $t('appUi.kef80204d'),
    dataIndex: 'promptName',
    width: 220,
    align: 'left',
  },
  {
    title: $t('appUi.k8b1d74f0'),
    dataIndex: 'category',
    width: 140,
    customRender: ({ text }) => {
      const color = CATEGORY_COLOR_MAP[text] || 'default';
      const label = CATEGORY_LABEL_MAP[text] || text || $t('appUi.kaef1a1e7');
      return h(Tag, { color }, () => label);
    },
  },
  {
    title: $t('appUi.k97b2edea'),
    dataIndex: 'callerClass',
    width: 220,
    align: 'left',
    customRender: ({ text }) =>
      h('span', { class: 'font-mono text-xs text-gray-600' }, text || '-'),
  },
  {
    title: $t('appUi.kdc3bdceb'),
    dataIndex: 'variablesSchema',
    width: 190,
    align: 'left',
    customRender: ({ record }) => {
      const vars = parseVariablesSchema(record.variablesSchema || record.variables);
      if (!vars.length) {
        return h('span', { class: 'text-xs text-gray-400' }, $t('appUi.kfa7a7b1b'));
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
    title: $t('appUi.k593f8a81'),
    dataIndex: 'version',
    width: 80,
    customRender: ({ text }) => h(Tag, { color: 'blue' }, () => `v${text || 1}`),
  },
  {
    title: $t('appUi.k83b47117'),
    dataIndex: 'temperature',
    width: 85,
    customRender: ({ text }) => (text !== undefined && text !== null ? Number(text).toFixed(2) : '0.20'),
  },
  {
    title: $t('appUi.k2926977b'),
    dataIndex: 'status',
    width: 90,
    customRender: ({ text }) => {
      const active = text === 'ACTIVE';
      return h(Tag, { color: active ? 'success' : 'default' }, () => (active ? $t('appUi.k6a2123a3') : $t('appUi.kfae2014b')));
    },
  },
  {
    title: $t('appUi.kdf300f61'),
    dataIndex: 'updateTime',
    width: 165,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'keyword',
    label: $t('appUi.kbbf7693c'),
    component: 'Input',
    componentProps: {
      placeholder: $t('appUi.k02aad434'),
    },
    colProps: { span: 8 },
  },
  {
    field: 'category',
    label: $t('appUi.k8b1d74f0'),
    component: 'Select',
    componentProps: {
      placeholder: $t('appUi.k0bfe6f2c'),
      allowClear: true,
      options: CATEGORY_OPTIONS,
    },
    colProps: { span: 6 },
  },
  {
    field: 'status',
    label: $t('appUi.k2926977b'),
    component: 'Select',
    componentProps: {
      placeholder: $t('appUi.k21caa442'),
      allowClear: true,
      options: [
        { label: $t('appUi.k6a2123a3'), value: 'ACTIVE' },
        { label: $t('appUi.kfae2014b'), value: 'INACTIVE' },
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
