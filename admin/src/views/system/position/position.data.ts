import { BasicColumn, FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';

export const columns: BasicColumn[] = [
  // {
  //   title: '직업 코드',
  //   dataIndex: 'code',
  //   width: 200,
  //   align: 'left',
  // },
  {
    title: '직위',
    dataIndex: 'name',
    align: 'left'
    // width: 200,
  },
  // {
  //   title: '직업레벨',
  //   dataIndex: 'postRank_dictText',
  //   width: 100,
  // },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: '직위',
    component: 'Input',
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: '기본 키',
    field: 'id',
    component: 'Input',
    show: false,
  },
  // {
  //   label: '계급',
  //   field: 'postRank',
  //   component: 'JDictSelectTag',
  //   required: true,
  //   componentProps: {
  //     dictCode: 'position_rank',
  //     dropdownStyle: {
  //       maxHeight: '100vh',
  //     },
  //     getPopupContainer: () => document.body,
  //   },
  // },
  {
    field: 'name',
    label: '직위',
    component: 'Input',
    required: true,
  },
  // {
  //   field: 'code',
  //   label: '직업 코드',
  //   component: 'Input',
  //   required: true,
  //   dynamicDisabled: ({ values }) => {
  //     return !!values.id;
  //   },
  //   dynamicRules: ({ model, schema }) => {
  //     return rules.duplicateCheckRule('sys_position', 'code', model, schema, true);
  //   },
  // },
];
