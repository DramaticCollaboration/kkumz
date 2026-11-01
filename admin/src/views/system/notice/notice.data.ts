import { BasicColumn, FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';

export const columns: BasicColumn[] = [
  {
    title: '제목',
    width: 150,
    dataIndex: 'titile',
  },
  {
    title: '메시지 유형',
    dataIndex: 'msgCategory',
    width: 100,
    customRender: ({ text }) => {
      return render.renderDict(text, 'msg_category');
    },
  },
  {
    title: '등록자',
    width: 100,
    dataIndex: 'sender',
  },
  {
    title: '우선순위',
    dataIndex: 'priority',
    width: 70,
    customRender: ({ text }) => {
      const color = text == 'L' ? 'blue' : text == 'M' ? 'yellow' : 'red';
      return render.renderTag(render.renderDict(text, 'priority'), color);
    },
  },
  {
    title: '알림 객체',
    dataIndex: 'msgType',
    width: 100,
    customRender: ({ text }) => {
      return render.renderDict(text, 'msg_type');
    },
  },
  {
    title: '게시 상태',
    dataIndex: 'sendStatus',
    width: 70,
    customRender: ({ text }) => {
      const color = text == '0' ? 'red' : text == '1' ? 'green' : 'gray';
      return render.renderTag(render.renderDict(text, 'send_status'), color);
    },
  },
  {
    title: '출시 시간',
    width: 100,
    dataIndex: 'sendTime',
  },
  {
    title: '실행 취소 시간',
    width: 100,
    dataIndex: 'cancelTime',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'titile',
    label: '제목',
    component: 'JInput',
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'msgCategory',
    label: '메시지 유형',
    required: true,
    component: 'JDictSelectTag',
    defaultValue: '1',
    componentProps: {
      type: 'radio',
      dictCode: 'msg_category',
      placeholder: '유형을 선택하세요',
    },
  },
  {
    field: 'titile',
    label: '제목',
    component: 'Input',
    required: true,
    componentProps: {
      placeholder: '제목을 입력하세요',
    },
  },
  {
    field: 'msgAbstract',
    label: '요약',
    component: 'InputTextArea',
    required: true,
  },
  // {
  //   field: 'endTime',
  //   label: '만료일',
  //   component: 'DatePicker',
  //   componentProps: {
  //     showTime: true,
  //     valueFormat: 'YYYY-MM-DD HH:mm:ss',
  //     placeholder: '종료일을 선택하세요.',
  //   },
  //   dynamicRules: ({ model }) => rules.endTime(model.startTime, true),
  // },
  {
    field: 'msgType',
    label: '사용자 수신',
    defaultValue: 'ALL',
    component: 'JDictSelectTag',
    required: true,
    componentProps: {
      type: 'radio',
      dictCode: 'msg_type',
      placeholder: '출판 범위를 선택해주세요',
    },
  },
  {
    field: 'userIds',
    label: '사용자 지정',
    component: 'JSelectUser',
    required: true,
    componentProps: {
      rowKey: 'id',
      labelKey: 'username',
    },
    ifShow: ({ values }) => values.msgType == 'USER',
  },
  {
    field: 'priority',
    label: '우선순위',
    defaultValue: 'H',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'priority',
      type: 'radio',
      placeholder: '우선순위를 선택해 주세요',
    },
  },
  {
    field: 'msgContent',
    label: '콘텐츠',
    component: 'Input',
    render: render.renderTinymce,
  },
];
