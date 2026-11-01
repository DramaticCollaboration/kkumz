import { BasicColumn, FormSchema } from '/@/components/Table';

const statusOptions = [
  { label: '활성', value: '1' },
  { label: '비활성', value: '0' },
];

export const columns: BasicColumn[] = [
  {
    title: '허용 테이블명',
    dataIndex: 'tableName',
    align: 'left',
    width: 200,
  },
  {
    title: '허용 필드/컬럼명',
    dataIndex: 'fieldName',
    align: 'left',
  },
  {
    title: '상태',
    dataIndex: 'status',
    width: 100,
    align: 'center',
    customRender({ text }) {
      const find = statusOptions.find((opt) => opt.value === String(text));
      return find?.label || (text === '1' ? '활성' : '비활성');
    },
  },
  {
    title: '등록일시',
    dataIndex: 'createTime',
    width: 180,
    align: 'center',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: '허용 테이블명',
    field: 'tableName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '허용 필드명',
    field: 'fieldName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '상태',
    field: 'status',
    component: 'Select',
    colProps: { span: 6 },
    componentProps: {
      options: statusOptions,
    },
  },
];

export const formSchema: FormSchema[] = [
  { label: '', field: 'id', component: 'Input', show: false },
  {
    label: '허용 테이블명',
    field: 'tableName',
    component: 'Input',
    required: true,
  },
  {
    label: '허용 필드명',
    field: 'fieldName',
    component: 'Input',
    required: true,
    helpMessage: '복수 개 입력 시 쉼표(,)로 구분하십시오.',
  },
  {
    label: '상태',
    field: 'status',
    component: 'Select',
    defaultValue: '1',
    componentProps: {
      options: statusOptions,
    },
  },
];
