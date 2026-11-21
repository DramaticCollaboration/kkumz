import { $t } from '/@/locales';
import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: $t('appUi.ke59c9016'),
    dataIndex: 'logContent',
    width: 100,
    align: 'left',
  },
  {
    title: $t('appUi.k600c1043'),
    dataIndex: 'userid',
    width: 80,
  },
  {
    title: $t('appUi.k52164040'),
    dataIndex: 'username',
    width: 80,
  },
  {
    title: 'IP',
    dataIndex: 'ip',
    width: 80,
  },
  {
    title: $t('appUi.k6bb035cf'),
    dataIndex: 'costTime',
    width: 80,
  },
  {
    title: $t('appUi.k6f80446e'),
    dataIndex: 'createTime',
    sorter: true,
    width: 80,
  },
  {
    title: $t('appUi.k26771db3'),
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
    title: $t('appUi.k3abc74fc'),
    dataIndex: 'operateType_dictText',
    width: 40,
  },
];

export const exceptionColumns: BasicColumn[] = [
  {
    title: $t('appUi.ke8ad2fbb'),
    dataIndex: 'logContent',
    width: 100,
    align: 'left',
  },
  {
    title: $t('appUi.k05dd5ce6'),
    dataIndex: 'requestUrl',
    width: 100,
  },
  {
    title: $t('appUi.k8f5bae3c'),
    dataIndex: 'method',
    width: 60,
  },
  {
    title: $t('appUi.k52164040'),
    dataIndex: 'username',
    width: 60,
    customRender: ({ record }) => {
      let pname = record.username;
      let pid = record.userid;
      if(!pname && !pid){
        return "";
      }
      return pname + $t('appUi.kdadaafa0')+ pid + " )";
    },
  },
  {
    title: 'IP',
    dataIndex: 'ip',
    width: 60,
  },
  {
    title: $t('appUi.k6f80446e'),
    dataIndex: 'createTime',
    sorter: true,
    width: 60,
  },
  {
    title: $t('appUi.k26771db3'),
    dataIndex: 'clientType_dictText',
    width: 60,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'keyWord',
    label: $t('appUi.k7e9c2b7b'),
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'fieldTime',
    component: 'RangePicker',
    label: $t('appUi.k6f80446e'),
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
    label: $t('appUi.k3abc74fc'),
    component: 'JDictSelectTag',
    colProps: { span: 4 },
    componentProps: {
      dictCode: 'operate_type',
    },
  },
];
