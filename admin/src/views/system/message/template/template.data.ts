import { $t } from '/@/locales';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';
import { filterDictTextByCache } from '/@/utils/dict/JDictSelectUtil';

export const columns: BasicColumn[] = [
  {
    title: $t('appUi.kd8066c4e'),
    dataIndex: 'templateName',
    width: 80,
  },
  {
    title: $t('appUi.kafcd4a88'),
    dataIndex: 'templateCode',
    width: 100,
  },
  {
    title: $t('appUi.k0e9d1a26'),
    dataIndex: 'templateContent',
    width: 150,
  },
  {
    title: $t('appUi.k2461cb85'),
    dataIndex: 'templateType',
    width: 100,
    customRender: ({ text }) => filterDictTextByCache('msgType', text),
  },
  {
    title: $t('appUi.k279076d8'),
    dataIndex: 'useStatus',
    width: 90,
    customRender: function ({ text }) {
      if (text == '1') {
        return $t('appUi.ka842629a');
      } else {
        return $t('appUi.kccf0c3a2');
      }
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: $t('appUi.kd8066c4e'),
    field: 'templateName',
    component: 'Input',
  },
  {
    label: $t('appUi.kafcd4a88'),
    field: 'templateCode',
    component: 'Input',
  },
  {
    label: $t('appUi.k2461cb85'),
    field: 'templateType',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'msgType',
    },
  },
];

export const formSchemas: FormSchema[] = [
  {
    label: 'ID',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: $t('appUi.kd8066c4e'),
    field: 'templateName',
    component: 'Input',
    required: true,
  },
  {
    label: $t('appUi.kafcd4a88'),
    field: 'templateCode',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      return [ ...rules.duplicateCheckRule('sys_sms_template', 'template_code', model, schema, true)];
    },
    // 편집 모드에서는 인코딩을 수정할 수 없습니다.
    dynamicDisabled: (params) => !!params.values.id,
  },
  {
    label: $t('appUi.k2461cb85'),
    field: 'templateType',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'msgType',
      placeholder: $t('appUi.k45085f3d'),
    },
    required: true,
  },
  {
    label: $t('appUi.k279076d8'),
    field: 'useStatus',
    component: 'JSwitch',
    componentProps: {
      options: ['1', '0'],
    },
  },
  {
    label: $t('appUi.kaea1d4a9'),
    field: 'templateContent',
    component: 'InputTextArea',
    componentProps: {
      autoSize: {
        minRows: 8,
        maxRows: 8,
      },
    },
    ifShow: ({ values }) => {
      return !/^[245]$/.test(String(values.templateType || ''));
    },
  },

  {
    label: $t('appUi.kaea1d4a9'),
    field: 'templateContent',
    component: 'JEditor',
    ifShow: ({ values }) => {
      return ['2', '4'].includes(values.templateType);
    },
  },
  {
    label: $t('appUi.kaea1d4a9'),
    field: 'templateContent',
    component: 'JMarkdownEditor',
    ifShow: ({ values }) => {
      return ['5'].includes(values.templateType);
    },
  },
];

export const sendTestFormSchemas: FormSchema[] = [
  {
    label: $t('appUi.kafcd4a88'),
    field: 'templateCode',
    component: 'Input',
    show: false,
  },
  {
    label: $t('appUi.kd8066c4e'),
    field: 'templateName',
    component: 'Input',
    componentProps: { disabled: true },
  },
  {
    label: $t('appUi.kaea1d4a9'),
    field: 'templateContent',
    component: 'InputTextArea',
    componentProps: { disabled: true, rows: 5 },
  },
  {
    label: $t('appUi.kbc37a64d'),
    field: 'testData',
    component: 'InputTextArea',
    required: true,
    helpMessage: $t('appUi.k605bd73a'),
    defaultValue: '{}',
    componentProps: {
      placeholder: $t('appUi.ke8cbf762'),
      rows: 5,
    },
  },
  {
    label: $t('appUi.kb2d237a7'),
    field: 'msgType',
    component: 'JDictSelectTag',
    required: true,
    defaultValue:'system',
    componentProps: { dictCode: 'messageType',type:'radio' },
  },
  {
    label: $t('appUi.kc9db07ee'),
    field: 'receiver',
    required: true,
    component: 'JSelectUser',
    componentProps: {
      labelKey: 'username',
      rowKey: 'username',
    },
  },
];
