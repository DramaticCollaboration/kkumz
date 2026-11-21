import { $t } from '/@/locales';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { dictItemCheck } from './dict.api';
import { rules } from '/@/utils/helper/validator';
import { h } from "vue";

export const columns: BasicColumn[] = [
  {
    title: $t('appUi.ka2617c7d'),
    dataIndex: 'dictName',
    width: 240,
  },
  {
    title: $t('appUi.k1de2c832'),
    dataIndex: 'dictCode',
    width: 240,
  },
  {
    title: $t('appUi.k84196436'),
    dataIndex: 'description',
    // width: 120
  },
];

export const recycleBincolumns: BasicColumn[] = [
  {
    title: $t('appUi.ka2617c7d'),
    dataIndex: 'dictName',
    width: 120,
  },
  {
    title: $t('appUi.k1de2c832'),
    dataIndex: 'dictCode',
    width: 120,
  },
  {
    title: $t('appUi.k84196436'),
    dataIndex: 'description',
    width: 120,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: $t('appUi.ka2617c7d'),
    field: 'dictName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: $t('appUi.k1de2c832'),
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
    label: $t('appUi.ka2617c7d'),
    field: 'dictName',
    required: true,
    component: 'Input',
  },
  {
    field: 'dictNameI18n',
    label: $t('common.i18nName'),
    component: 'I18nInput',
  },
  {
    label: $t('appUi.k1de2c832'),
    field: 'dictCode',
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model, schema }) => rules.duplicateCheckRule('sys_dict', 'dict_code', model, schema, true),
  },
  {
    label: $t('appUi.k84196436'),
    field: 'description',
    component: 'Input',
  },
];

export const dictItemColumns: BasicColumn[] = [
  {
    title: $t('appUi.k9aa18e50'),
    dataIndex: 'itemText',
    width: 80,
  },
  {
    title: $t('appUi.kdc125b35'),
    dataIndex: 'itemValue',
    width: 80,
  },
  {
    title: $t('appUi.k2425c571'),
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
    label: $t('appUi.k9aa18e50'),
    field: 'itemText',
    component: 'Input',
  },
  {
    field: 'itemTextI18n',
    label: $t('common.i18nName'),
    component: 'I18nInput',
  },
  {
    label: $t('appUi.k2926977b'),
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
    label: $t('appUi.k9aa18e50'),
    field: 'itemText',
    required: true,
    component: 'Input',
  },
  {
    label: $t('appUi.kdc125b35'),
    field: 'itemValue',
    component: 'Input',
    dynamicRules: ({ values, model }) => {
      return [
        {
          required: true,
          validator: (_, value) => {
            if (!value) {
              return Promise.reject($t('appUi.k89d36224'));
            }
            if (new RegExp("[`~!@#$^&*()=|{}'.<>《》/?！₩（）—【】‘；：”“。，、？]").test(value)) {
              return Promise.reject($t('appUi.k6864a5bd'));
            }
            return new Promise<void>((resolve, reject) => {
              let params = {
                dictId: values.dictId,
                id: model.id,
                itemValue: value,
              };
              dictItemCheck(params)
                .then((res) => {
                  res.success ? resolve() : reject(res.message || $t('appUi.kae6c001b'));
                })
                .catch((err) => {
                  reject(err.message || $t('appUi.k7790958f'));
                });
            });
          },
        },
      ];
    },
  },
  {
    label: $t('appUi.kcb12bde0'),
    field: 'itemColor',
    component: 'Input',
    slot:'itemColor'
  },
  {
    label: $t('appUi.k84196436'),
    field: 'description',
    component: 'Input',
  },
  {
    field: 'sortOrder',
    label: $t('appUi.k7745cc5a'),
    component: 'InputNumber',
    defaultValue: 1,
  },
  {
    field: 'status',
    label: $t('appUi.ke91e52ad'),
    defaultValue: 1,
    component: 'JDictSelectTag',
    componentProps: {
      type: 'radioButton',
      dictCode: 'dict_item_status',
      stringToNumber: true,
    },
  },
];
