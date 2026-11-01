import { Tag } from 'ant-design-vue';
import { BasicColumn } from '/@/components/Table/index';
import { DescItem } from '/@/components/Description/index';
import { ErrorTypeEnum } from '/@/enums/exceptionEnum';
import { $t } from '/@/locales';

function getTypeColor(text: string): string {
  return text === ErrorTypeEnum.VUE
    ? 'green'
    : text === ErrorTypeEnum.RESOURCE
    ? 'cyan'
    : text === ErrorTypeEnum.PROMISE
    ? 'blue'
    : text === ErrorTypeEnum.AJAX
    ? 'red'
    : 'purple';
}

function formatStackText(stack?: string): string {
  if (!stack) return '-';
  return String(stack)
    .split('~')
    .map((line, idx) => (idx === 0 ? line.trim() : `  at ${line.trim()}`))
    .join('\n');
}

export function getColumns(): BasicColumn[] {
  return [
    {
      dataIndex: 'type',
      title: $t('sys.errorLog.tableColumnType', '오류 유형'),
      width: 90,
      customRender: ({ text }) => {
        const color = getTypeColor(text);
        return <Tag color={color}>{() => text}</Tag>;
      },
    },
    {
      dataIndex: 'url',
      title: $t('sys.errorLog.tableColumnUrl', 'URL'),
      width: 200,
    },
    {
      dataIndex: 'time',
      title: $t('sys.errorLog.tableColumnDate', '발생 시각'),
      width: 160,
    },
    {
      dataIndex: 'file',
      title: $t('sys.errorLog.tableColumnFile', '리소스/파일 경로'),
      width: 200,
    },
    {
      dataIndex: 'name',
      title: $t('sys.errorLog.tableColumnName', '오류명'),
      width: 160,
    },
    {
      dataIndex: 'message',
      title: $t('sys.errorLog.tableColumnMsg', '오류 내용'),
      width: 280,
    },
    {
      dataIndex: 'stack',
      title: $t('sys.errorLog.tableColumnStackMsg', '스택 상세'),
      customRender: ({ text }) => {
        if (!text) return '-';
        return String(text).replace(/~/g, ' → ');
      },
    },
  ];
}

export function getDescSchema(): DescItem[] {
  return [
    {
      field: 'type',
      label: $t('sys.errorLog.tableColumnType', '오류 유형'),
      span: 1,
      labelMinWidth: 110,
      render: (val) => (val ? <Tag color={getTypeColor(val)}>{() => val}</Tag> : '-'),
    },
    {
      field: 'time',
      label: $t('sys.errorLog.tableColumnDate', '발생 시각'),
      span: 1,
      labelMinWidth: 110,
      render: (val) => <span class="font-mono text-xs">{val || '-'}</span>,
    },
    {
      field: 'name',
      label: $t('sys.errorLog.tableColumnName', '오류명'),
      span: 1,
      labelMinWidth: 110,
      render: (val) => <span class="font-semibold text-slate-800 dark:text-zinc-100">{val || '-'}</span>,
    },
    {
      field: 'url',
      label: $t('sys.errorLog.tableColumnUrl', 'URL'),
      span: 1,
      labelMinWidth: 110,
      render: (val) => <span class="font-mono text-xs break-all text-blue-600 dark:text-blue-400">{val || '-'}</span>,
    },
    {
      field: 'file',
      label: $t('sys.errorLog.tableColumnFile', '리소스/파일 경로'),
      span: 2,
      labelMinWidth: 110,
      render: (val) => <span class="font-mono text-xs break-all text-slate-600 dark:text-zinc-300">{val || '-'}</span>,
    },
    {
      field: 'message',
      label: $t('sys.errorLog.tableColumnMsg', '오류 내용'),
      span: 2,
      labelMinWidth: 110,
      render: (val) => (
        <div class="bg-red-50 dark:bg-red-950/40 text-red-700 dark:text-red-300 border border-red-200 dark:border-red-800/60 rounded px-3 py-2 font-mono text-xs break-all">
          {val || '-'}
        </div>
      ),
    },
    {
      field: 'stack',
      label: $t('sys.errorLog.tableColumnStackMsg', '스택 상세'),
      span: 2,
      labelMinWidth: 110,
      render: (val) => (
        <pre class="bg-slate-900 text-slate-100 p-3 rounded-md text-xs font-mono max-h-64 overflow-y-auto whitespace-pre-wrap break-all leading-relaxed m-0">
          {formatStackText(val)}
        </pre>
      ),
    },
  ];
}
