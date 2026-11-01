import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '이름',
    dataIndex: 'name',
    width: 350,
    align: 'left',
  },
  {
    title: '분류 코드',
    dataIndex: 'code',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: '이름',
    field: 'name',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '코드',
    field: 'code',
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
    label: '상위 노드',
    field: 'pid',
    component: 'TreeSelect',
    componentProps: {
      fieldNames: {
        value: 'key',
      },
      dropdownStyle: {
        maxHeight: '50vh',
      },
      getPopupContainer: () => document.body,
    },
    show: ({ values }) => {
      return values.pid !== '0';
    },
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  {
    label: '이름',
    field: 'name',
    required: true,
    component: 'Input',
  },
];
