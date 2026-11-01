import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '로그 내용',
    dataIndex: 'logContent',
    width: 100,
    align: 'left',
  },
  {
    title: '작업자 ID',
    dataIndex: 'userid',
    width: 80,
  },
  {
    title: '작업자',
    dataIndex: 'username',
    width: 80,
  },
  {
    title: 'IP',
    dataIndex: 'ip',
    width: 80,
  },
  {
    title: '소요 시간(밀리초)',
    dataIndex: 'costTime',
    width: 80,
  },
  {
    title: '등록일',
    dataIndex: 'createTime',
    sorter: true,
    width: 80,
  },
  {
    title: '클라이언트 유형',
    dataIndex: 'clientType_dictText',
    width: 60,
  },
];

/**
 * 작업 로그에는 작업 유형이 필요합니다.
 */
export const operationLogColumn: BasicColumn[] = [
  ...columns,
  {
    title: '작업 유형',
    dataIndex: 'operateType_dictText',
    width: 40,
  },
];

export const exceptionColumns: BasicColumn[] = [
  {
    title: '예외 제목',
    dataIndex: 'logContent',
    width: 100,
    align: 'left',
  },
  {
    title: '요청 주소',
    dataIndex: 'requestUrl',
    width: 100,
  },
  {
    title: '요청 매개변수',
    dataIndex: 'method',
    width: 60,
  },
  {
    title: '작업자',
    dataIndex: 'username',
    width: 60,
    customRender: ({ record }) => {
      let pname = record.username;
      let pid = record.userid;
      if(!pname && !pid){
        return "";
      }
      return pname + " (계정: "+ pid + " )";
    },
  },
  {
    title: 'IP',
    dataIndex: 'ip',
    width: 60,
  },
  {
    title: '등록일',
    dataIndex: 'createTime',
    sorter: true,
    width: 60,
  },
  {
    title: '클라이언트 유형',
    dataIndex: 'clientType_dictText',
    width: 60,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'keyWord',
    label: '검색 로그',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'fieldTime',
    component: 'RangePicker',
    label: '등록일',
    componentProps: {
      valueType: 'Date',
    },
    colProps: {
      span: 6,
    },
  },
];

export const operationSearchFormSchema: FormSchema[] = [
  ...searchFormSchema,
  {
    field: 'operateType',
    label: '작업 유형',
    component: 'JDictSelectTag',
    colProps: { span: 4 },
    componentProps: {
      dictCode: 'operate_type',
    },
  },
];
