import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '테이블 이름',
    dataIndex: 'dataTable',
    width: 150,
    align: 'left',
  },
  {
    title: '데이터 ID',
    dataIndex: 'dataId',
    width: 350,
  },
  {
    title: '버전 번호',
    dataIndex: 'dataVersion',
    width: 100,
  },
  {
    title: '데이터 내용',
    dataIndex: 'dataContent',
  },
  {
    title: '생성자',
    dataIndex: 'createBy',
    sorter: true,
    width: 200,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'dataTable',
    label: '테이블 이름',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'dataId',
    label: '데이터 ID',
    component: 'Input',
    colProps: { span: 8 },
  },
];
