import { BasicColumn, FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';

export const columns: BasicColumn[] = [
  {
    title: '제목',
    dataIndex: 'titile',
    width: 100,
    align: 'left',
  },
  {
    title: '메시지 유형',
    dataIndex: 'msgCategory',
    width: 80,
    customRender: ({ text }) => {
      return render.renderDictNative(
        text,
        [
          { label: '공지사항', value: '1', color: 'blue' },
          { label: '시스템 메시지', value: '2' },
        ],
        true
      );
    },
  },
  {
    title: '등록자',
    dataIndex: 'sender',
    width: 80,
  },
  {
    title: '출시 시간',
    dataIndex: 'sendTime',
    width: 80,
  },
  {
    title: '우선순위',
    dataIndex: 'priority',
    width: 80,
    customRender: ({ text }) => {
      const color = text == 'L' ? 'blue' : text == 'M' ? 'yellow' : 'red';
      return render.renderTag(render.renderDict(text, 'priority'), color);
    },
  },
  {
    title: '읽음 여부',
    dataIndex: 'readFlag',
    width: 80,
    customRender: ({ text }) => {
      return render.renderDictNative(
        text,
        [
          { label: '읽히지 않는', value: '0', color: 'red' },
          { label: '읽음', value: '1' },
        ],
        true
      );
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'titile',
    label: '제목',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'sender',
    label: '등록자',
    component: 'Input',
    colProps: { span: 8 },
  },
];
