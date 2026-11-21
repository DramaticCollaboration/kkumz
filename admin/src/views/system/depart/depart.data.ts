import { $t } from '/@/locales';
import { FormSchema } from '/@/components/Form';

// 학과기본Form
export function useBasicFormSchema() {
  const basicFormSchema: FormSchema[] = [
    {
      field: 'departName',
      label: $t('appUi.k5b860f35'),
      component: 'Input',
      componentProps: {
        placeholder: $t('appUi.k7d6addec'),
      },
      rules: [{ required: true, message: $t('appUi.kbba5bf93') }],
    },
    {
      field: 'parentId',
      label: $t('appUi.k6e257e6e'),
      component: 'TreeSelect',
      componentProps: {
        treeData: [],
        placeholder: $t('appUi.kd58fa73a'),
        dropdownStyle: { maxHeight: '200px', overflow: 'auto' },
      },
    },
    {
      field: 'orgCode',
      label: $t('appUi.kb5856d92'),
      component: 'Input',
      componentProps: {
        placeholder: $t('appUi.k128fec3d'),
      },
    },
    {
      field: 'orgCategory',
      label: $t('appUi.k1db0901e'),
      component: 'RadioButtonGroup',
      componentProps: { options: [] },
    },
    {
      field: 'departOrder',
      label: $t('appUi.k7745cc5a'),
      component: 'InputNumber',
      componentProps: {},
    },
    {
      field: 'mobile',
      label: $t('appUi.k91032991'),
      component: 'Input',
      componentProps: {
        placeholder: $t('appUi.keecd9086'),
      },
    },
    {
      field: 'fax',
      label: $t('appUi.k30675920'),
      component: 'Input',
      componentProps: {
        placeholder: $t('appUi.k5bd21416'),
      },
    },
    {
      field: 'address',
      label: $t('appUi.k117bab80'),
      component: 'Input',
      componentProps: {
        placeholder: $t('appUi.k58bdd72b'),
      },
    },
    {
      field: 'memo',
      label: $t('appUi.k75cffa41'),
      component: 'InputTextArea',
      componentProps: {
        placeholder: $t('appUi.k711b5282'),
      },
    },
  ];
  return { basicFormSchema };
}

// 조직 유형 옵션
export const orgCategoryOptions = {
  // 1급 부서
  root: [{ value: '1', label: $t('appUi.kea129229') }],
  // 하위 부서
  child: [
    { value: '2', label: $t('appUi.ke5b530d6') },
    { value: '3', label: $t('appUi.k3c37764a') },
  ],
};
