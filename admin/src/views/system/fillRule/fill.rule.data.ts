import { BasicColumn, FormSchema } from '/@/components/Table';
import { duplicateCheckDelay } from '/@/views/system/user/user.api';

export const columns: BasicColumn[] = [
  {
    title: '규칙 이름',
    dataIndex: 'ruleName',
    width: 200,
    align: 'center',
  },
  {
    title: '규칙 인코딩',
    dataIndex: 'ruleCode',
    width: 200,
    align: 'center',
  },
  {
    title: '규칙 구현 클래스',
    dataIndex: 'ruleClass',
    width: 300,
    align: 'center',
  },
  {
    title: '규칙 매개변수',
    dataIndex: 'ruleParams',
    width: 200,
    align: 'center',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'ruleName',
    label: '규칙 이름',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'ruleCode',
    label: '규칙 인코딩',
    component: 'Input',
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'ruleName',
    label: '규칙 이름',
    component: 'Input',
    required: true,
    colProps: { span: 24 },
  },
  {
    field: 'ruleCode',
    label: '규칙 인코딩',
    component: 'Input',
    colProps: { span: 24 },
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model }) => {
      return [
        {
          required: true,
          validator: (_, value) => {
            return new Promise((resolve, reject) => {
              if (!value) {
                return reject('규칙코드를 입력해주세요!');
              }
              let params = {
                tableName: 'sys_fill_rule',
                fieldName: 'rule_code',
                fieldVal: value,
                dataId: model.id,
              };
              duplicateCheckDelay(params)
                .then((res) => {
                  res.success ? resolve() : reject('규칙 인코딩이 이미 존재합니다!');
                })
                .catch((err) => {
                  reject(err.message || '확인 실패');
                });
            });
          },
        },
      ];
    },
  },
  {
    field: 'ruleClass',
    label: '규칙 구현 클래스',
    component: 'Input',
    required: true,
    colProps: { span: 24 },
  },
  {
    field: 'ruleParams',
    label: '규칙 매개변수',
    colProps: { span: 24 },
    component: 'JAddInput',
    componentProps: {
      min: 0,
    },
  },
];
