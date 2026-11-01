import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { dictItemCheck } from './dict.api';
import { rules } from '/@/utils/helper/validator';
import { h } from "vue";

export const columns: BasicColumn[] = [
  {
    title: '사전 이름',
    dataIndex: 'dictName',
    width: 240,
  },
  {
    title: '사전 코드',
    dataIndex: 'dictCode',
    width: 240,
  },
  {
    title: '설명',
    dataIndex: 'description',
    // width: 120
  },
];

export const recycleBincolumns: BasicColumn[] = [
  {
    title: '사전 이름',
    dataIndex: 'dictName',
    width: 120,
  },
  {
    title: '사전 코드',
    dataIndex: 'dictCode',
    width: 120,
  },
  {
    title: '설명',
    dataIndex: 'description',
    width: 120,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: '사전 이름',
    field: 'dictName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '사전 코드',
    field: 'dictCode',
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
    label: '사전 이름',
    field: 'dictName',
    required: true,
    component: 'Input',
  },
  {
    label: '사전 코드',
    field: 'dictCode',
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model, schema }) => rules.duplicateCheckRule('sys_dict', 'dict_code', model, schema, true),
  },
  {
    label: '설명',
    field: 'description',
    component: 'Input',
  },
];

export const dictItemColumns: BasicColumn[] = [
  {
    title: '이름',
    dataIndex: 'itemText',
    width: 80,
  },
  {
    title: '데이터 값',
    dataIndex: 'itemValue',
    width: 80,
  },
  {
    title: '사전 색상',
    dataIndex: 'itemColor',
    width: 80,
    align:'center',
    customRender:({ text }) => {
      return h('div', {
        style: {"background": text, "width":"18px","height":"18px","border-radius":"50%","margin":"0 auto"}
      })
    }
  },
];

export const dictItemSearchFormSchema: FormSchema[] = [
  {
    label: '이름',
    field: 'itemText',
    component: 'Input',
  },
  {
    label: '상태',
    field: 'status',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'dict_item_status',
      stringToNumber: true,
    },
  },
];

export const itemFormSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: '이름',
    field: 'itemText',
    required: true,
    component: 'Input',
  },
  {
    label: '데이터 값',
    field: 'itemValue',
    component: 'Input',
    dynamicRules: ({ values, model }) => {
      return [
        {
          required: true,
          validator: (_, value) => {
            if (!value) {
              return Promise.reject('데이터 값을 입력하세요');
            }
            if (new RegExp("[`~!@#$^&*()=|{}'.<>《》/?！₩（）—【】‘；：”“。，、？]").test(value)) {
              return Promise.reject('데이터 값에는 특수 문자가 포함될 수 없습니다!');
            }
            return new Promise<void>((resolve, reject) => {
              let params = {
                dictId: values.dictId,
                id: model.id,
                itemValue: value,
              };
              dictItemCheck(params)
                .then((res) => {
                  res.success ? resolve() : reject(res.message || '확인 실패');
                })
                .catch((err) => {
                  reject(err.message || '인증 실패');
                });
            });
          },
        },
      ];
    },
  },
  {
    label: '색상 값',
    field: 'itemColor',
    component: 'Input',
    slot:'itemColor'
  },
  {
    label: '설명',
    field: 'description',
    component: 'Input',
  },
  {
    field: 'sortOrder',
    label: '정렬',
    component: 'InputNumber',
    defaultValue: 1,
  },
  {
    field: 'status',
    label: '활성화 여부',
    defaultValue: 1,
    component: 'JDictSelectTag',
    componentProps: {
      type: 'radioButton',
      dictCode: 'dict_item_status',
      stringToNumber: true,
    },
  },
];
