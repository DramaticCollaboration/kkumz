import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '메시지 제목',
    dataIndex: 'esTitle',
    width: 140,
  },
  {
    title: '콘텐츠 보내기',
    dataIndex: 'esContent',
    width: 200,
    // slots: { customRender: 'esContent' },
  },
  {
    title: '받는 사람',
    dataIndex: 'esReceiver',
    width: 140,
  },
  {
    title: '시간 보내기',
    dataIndex: 'esSendNum',
    width: 120,
  },
  {
    title: '상태 보내기',
    dataIndex: 'esSendStatus_dictText',
    width: 120,
  },
  {
    title: '보내는 시간',
    dataIndex: 'esSendTime',
    width: 140,
  },
  {
    title: '전송 방법',
    dataIndex: 'esType_dictText',
    width: 120,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: '메시지 제목',
    field: 'esTitle',
    component: 'Input',
  },
  {
    label: '상태 보내기',
    field: 'esSendStatus',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'msgSendStatus',
    },
  },
  {
    label: '전송 방법',
    field: 'esType',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'messageType',
    },
  },
];

export const formSchemas: FormSchema[] = [
  {
    label: 'ID',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: '메시지 제목',
    field: 'esTitle',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: '콘텐츠 보내기',
    field: 'esContent',
    component: 'InputTextArea',
    componentProps: { readOnly: true },
  },
  {
    label: '매개변수 보내기',
    field: 'esParam',
    component: 'Input',
    componentProps: { readOnly: true },
  },

  {
    label: '받는 사람',
    field: 'esReceiver',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: '전송 방법',
    field: 'esType',
    component: 'JDictSelectTag',
    componentProps: { disabled: true, dictCode: 'messageType' },
  },
  {
    label: '보내는 시간',
    field: 'esSendTime',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: '상태 보내기',
    field: 'esSendStatus',
    component: 'JDictSelectTag',
    componentProps: { disabled: true, dictCode: 'msgSendStatus' },
  },
  {
    label: '시간 보내기',
    field: 'esSendNum',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: '발송실패 이유',
    field: 'esResult',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: '비고',
    field: 'remark',
    component: 'InputTextArea',
    componentProps: { readOnly: true },
  },
];
