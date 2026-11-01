import { h } from 'vue';
import { Tag, Tooltip } from 'ant-design-vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';
import { JCronValidator } from '/@/components/Form';

export const columns: BasicColumn[] = [
  {
    title: '작업 설명',
    dataIndex: 'description',
    width: 250,
    align: 'left',
    customRender: ({ record }) => {
      return h('div', { class: 'flex flex-col' }, [
        h('span', { class: 'font-semibold text-gray-900 text-sm' }, record.description || '-'),
        record.botCode ? h('span', { class: 'text-[11px] text-blue-600 font-mono mt-0.5' }, record.botCode) : null,
      ]);
    },
  },
  {
    title: '실행 작업 클래스',
    dataIndex: 'jobClassName',
    width: 280,
    align: 'left',
    customRender: ({ text }) => {
      const simpleName = text ? text.substring(text.lastIndexOf('.') + 1) : '-';
      return h(Tooltip, { title: text }, () =>
        h('span', { class: 'font-mono text-xs text-gray-600 hover:text-blue-600 cursor-pointer' }, simpleName)
      );
    },
  },
  {
    title: '실행 주기',
    dataIndex: 'cronExpression',
    width: 130,
    align: 'center',
    customRender: ({ text }) => {
      return h('span', { class: 'font-mono text-xs px-2 py-0.5 bg-slate-100 text-slate-700 rounded border border-slate-200' }, text);
    },
  },
  {
    title: '최근 실행 시각',
    dataIndex: 'lastRunTime',
    width: 150,
    align: 'center',
    customRender: ({ text }) => {
      if (!text) return h('span', { class: 'text-xs text-gray-400' }, '-');
      return h('span', { class: 'text-xs font-mono text-gray-700' }, text);
    },
  },
  {
    title: '최근 실행 결과',
    dataIndex: 'lastStatus',
    width: 160,
    align: 'center',
    customRender: ({ record }) => {
      const status = record.lastStatus;
      if (!status) {
        return h(Tag, { color: 'default', class: 'm-0 text-xs' }, () => '미실행');
      }
      if (status === 'SUCCESS') {
        const summary = record.lastResultSummary || '정상 완료';
        return h(Tag, { color: 'success', class: 'm-0 text-xs font-medium' }, () => summary);
      }
      if (status === 'WARNING') {
        const summary = record.lastResultSummary || '일부 조정';
        return h(Tag, { color: 'warning', class: 'm-0 text-xs font-medium' }, () => summary);
      }
      if (status === 'RUNNING') {
        return h(Tag, { color: 'processing', class: 'm-0 text-xs font-medium' }, () => '실행 중');
      }
      const errorMsg = record.lastErrorMessage || record.lastResultSummary || '오류 발생';
      return h(Tooltip, { title: errorMsg }, () =>
        h(Tag, { color: 'error', class: 'm-0 text-xs font-medium cursor-pointer' }, () => '오류 발생')
      );
    },
  },
  {
    title: '소요 시간',
    dataIndex: 'lastDurationMs',
    width: 90,
    align: 'right',
    customRender: ({ text }) => {
      if (text == null) return h('span', { class: 'text-xs text-gray-400' }, '-');
      return h('span', { class: 'font-mono text-xs tabular-nums text-gray-600' }, `${text}ms`);
    },
  },
  {
    title: '스케줄 상태',
    dataIndex: 'status',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      const isRunning = text == '0';
      const color = isRunning ? 'success' : 'error';
      const label = isRunning ? '정상 활성' : '정지';
      return render.renderTag(label, color);
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'description',
    label: '작업 설명',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'jobClassName',
    label: '작업 클래스',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'status',
    label: '스케줄 상태',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'quartz_status',
      stringToNumber: true,
    },
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
    field: 'description',
    label: '작업 설명',
    component: 'Input',
    required: true,
  },
  {
    field: 'jobClassName',
    label: '작업 클래스',
    component: 'Input',
    required: true,
  },
  {
    field: 'cronExpression',
    label: '실행 주기',
    component: 'JEasyCron',
    defaultValue: '* * * * * ? *',
    rules: [{ required: true, message: 'Cron 표현식을 입력하세요.' }, { validator: JCronValidator }],
  },
  {
    field: 'paramterType',
    label: '파라미터 유형',
    component: 'Select',
    defaultValue: 'string',
    componentProps: {
      options: [
        { label: '문자열', value: 'string' },
        { label: 'JSON 객체', value: 'json' },
      ],
    },
  },
  {
    field: 'parameter',
    label: '매개변수',
    component: 'InputTextArea',
    ifShow: ({ values }) => {
      return values.paramterType == 'string';
    },
  },
  {
    field: 'parameter',
    label: '매개변수',
    component: 'JAddInput',
    helpMessage: '키-값 쌍 Form을 작성하세요.',
    ifShow: ({ values }) => {
      return values.paramterType == 'json';
    },
  },
  {
    field: 'status',
    label: '상태',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'quartz_status',
      type: 'radioButton',
      stringToNumber: true,
      dropdownStyle: {
        maxHeight: '6vh',
      },
    },
  },
  {
    field: 'description',
    label: '설명',
    component: 'InputTextArea',
  },
];
