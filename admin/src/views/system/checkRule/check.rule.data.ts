import { BasicColumn, FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';
import { duplicateCheckDelay } from '/@/views/system/user/user.api';
import { validateCheckRule } from '/@/views/system/checkRule/check.rule.api';
import { array } from 'vue-types';

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
    title: '규칙 설명',
    dataIndex: 'ruleDescription',
    width: 300,
    align: 'center',
    customRender: function ({ text }) {
      return render.renderTip(text, 30);
    },
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
                tableName: 'sys_check_rule',
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
    field: 'ruleDescription',
    label: '규칙 설명',
    colProps: { span: 24 },
    component: 'InputTextArea',
    componentProps: {
      placeholder: '규칙 설명을 입력하세요.',
      rows: 2,
    },
  },
];

export const checkRuleInput: FormSchema[] = [
  {
    label: '123',
    field: 'ruleCode',
    component: 'Input',
    show: false,
  },
  {
    field: 'testValue',
    label: '테스트할 값:',
    component: 'Input',
    componentProps: ({ formModel }) => {
      return {
        onChange: (e) => {
          formModel.testValue = e.target.value;
        },
      };
    },
    dynamicRules: ({ model }) => {
      const { ruleCode } = model;
      return [
        {
          required: false,
          validator: (_, value) => {
            return new Promise((resolve, reject) => {
              if (ruleCode && value) {
                /*console.log({ruleCode,value})*/
                validateCheckRule(ruleCode, value)
                  .then((res) => {
                    //console.log(1233, res)
                    res['success'] ? resolve() : reject(res['message']);
                  })
                  .catch((err) => {
                    reject(err.message || err);
                  });
              } else {
                resolve();
              }
            });
          },
        },
      ];
    },
  },
];
