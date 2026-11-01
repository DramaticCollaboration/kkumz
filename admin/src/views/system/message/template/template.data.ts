import { BasicColumn, FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';
import { filterDictTextByCache } from '/@/utils/dict/JDictSelectUtil';

export const columns: BasicColumn[] = [
  {
    title: '템플릿 제목',
    dataIndex: 'templateName',
    width: 80,
  },
  {
    title: '템플릿 인코딩',
    dataIndex: 'templateCode',
    width: 100,
  },
  {
    title: '알림 템플릿',
    dataIndex: 'templateContent',
    width: 150,
  },
  {
    title: '템플릿 유형',
    dataIndex: 'templateType',
    width: 100,
    customRender: ({ text }) => filterDictTextByCache('msgType', text),
  },
  {
    title: '신청 여부',
    dataIndex: 'useStatus',
    width: 90,
    customRender: function ({ text }) {
      if (text == '1') {
        return '예';
      } else {
        return '아니요';
      }
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: '템플릿 제목',
    field: 'templateName',
    component: 'Input',
  },
  {
    label: '템플릿 인코딩',
    field: 'templateCode',
    component: 'Input',
  },
  {
    label: '템플릿 유형',
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
    label: '템플릿 제목',
    field: 'templateName',
    component: 'Input',
    required: true,
  },
  {
    label: '템플릿 인코딩',
    field: 'templateCode',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      return [ ...rules.duplicateCheckRule('sys_sms_template', 'template_code', model, schema, true)];
    },
    // 편집 모드에서는 인코딩을 수정할 수 없습니다.
    dynamicDisabled: (params) => !!params.values.id,
  },
  {
    label: '템플릿 유형',
    field: 'templateType',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'msgType',
      placeholder: '템플릿 유형을 선택하세요.',
    },
    required: true,
  },
  {
    label: '신청 여부',
    field: 'useStatus',
    component: 'JSwitch',
    componentProps: {
      options: ['1', '0'],
    },
  },
  {
    label: '템플릿 내용',
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
    label: '템플릿 내용',
    field: 'templateContent',
    component: 'JEditor',
    ifShow: ({ values }) => {
      return ['2', '4'].includes(values.templateType);
    },
  },
  {
    label: '템플릿 내용',
    field: 'templateContent',
    component: 'JMarkdownEditor',
    ifShow: ({ values }) => {
      return ['5'].includes(values.templateType);
    },
  },
];

export const sendTestFormSchemas: FormSchema[] = [
  {
    label: '템플릿 인코딩',
    field: 'templateCode',
    component: 'Input',
    show: false,
  },
  {
    label: '템플릿 제목',
    field: 'templateName',
    component: 'Input',
    componentProps: { disabled: true },
  },
  {
    label: '템플릿 내용',
    field: 'templateContent',
    component: 'InputTextArea',
    componentProps: { disabled: true, rows: 5 },
  },
  {
    label: '테스트 데이터',
    field: 'testData',
    component: 'InputTextArea',
    required: true,
    helpMessage: 'JSON 데이터',
    defaultValue: '{}',
    componentProps: {
      placeholder: '테스트 데이터를 JSON 형식으로 입력하세요.',
      rows: 5,
    },
  },
  {
    label: '메시지 유형',
    field: 'msgType',
    component: 'JDictSelectTag',
    required: true,
    defaultValue:'system',
    componentProps: { dictCode: 'messageType',type:'radio' },
  },
  {
    label: '메시지 수신자',
    field: 'receiver',
    required: true,
    component: 'JSelectUser',
    componentProps: {
      labelKey: 'username',
      rowKey: 'username',
    },
  },
];
