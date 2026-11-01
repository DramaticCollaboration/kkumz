import type { PropType } from 'vue';
import type { PaginationProps } from './types/pagination';
import type { BasicColumn, FetchSetting, TableSetting, SorterResult, TableCustomRecord, TableRowSelection, SizeType } from './types/table';
import type { FormProps } from '/@/components/Form';
import { DEFAULT_FILTER_FN, DEFAULT_SORT_FN, FETCH_SETTING, DEFAULT_SIZE } from './const';
import { propTypes } from '/@/utils/propTypes';

export const basicProps = {
  clickToRowSelect: propTypes.bool.def(true),
  isTreeTable: propTypes.bool.def(false),
  tableSetting: propTypes.shape<TableSetting>({}),
  inset: propTypes.bool,
  sortFn: {
    type: Function as PropType<(sortInfo: SorterResult) => any>,
    default: DEFAULT_SORT_FN,
  },
  filterFn: {
    type: Function as PropType<(data: Partial<Recordable<string[]>>) => any>,
    default: DEFAULT_FILTER_FN,
  },
  showTableSetting: propTypes.bool,
  autoCreateKey: propTypes.bool.def(true),
  striped: propTypes.bool.def(false),
  showSummary: propTypes.bool,
  summaryFunc: {
    type: [Function, Array] as PropType<(...arg: any[]) => any[]>,
    default: null,
  },
  summaryData: {
    type: Array as PropType<Recordable[]>,
    default: null,
  },
  indentSize: propTypes.number.def(24),
  canColDrag: propTypes.bool.def(true),
  api: {
    type: Function as PropType<(...arg: any[]) => Promise<any>>,
    default: null,
  },
  beforeFetch: {
    type: Function as PropType<Fn>,
    default: null,
  },
  afterFetch: {
    type: Function as PropType<Fn>,
    default: null,
  },
  handleSearchInfoFn: {
    type: Function as PropType<Fn>,
    default: null,
  },
  fetchSetting: {
    type: Object as PropType<FetchSetting>,
    default: () => {
      return FETCH_SETTING;
    },
  },
  // 지금 인터페이스 요청
  immediate: propTypes.bool.def(true),
  emptyDataIsShowTable: propTypes.bool.def(true),
  // 추가 요청 매개변수
  searchInfo: {
    type: Object as PropType<Recordable>,
    default: null,
  },
  // 기본 정렬 매개변수
  defSort: {
    type: Object as PropType<Recordable>,
    default: null,
  },
  // 검색 Form을 사용하세요
  useSearchForm: propTypes.bool,
  // Form 구성
  formConfig: {
    type: Object as PropType<Partial<FormProps>>,
    default: null,
  },
  columns: {
    type: [Array] as PropType<BasicColumn[]>,
    default: () => [],
  },
  showIndexColumn: propTypes.bool.def(true),
  indexColumnProps: {
    type: Object as PropType<BasicColumn>,
    default: null,
  },
  showActionColumn: {
    type: Boolean,
    default: true,
  },
  actionColumn: {
    type: Object as PropType<BasicColumn>,
    default: null,
  },
  ellipsis: propTypes.bool.def(true),
  canResize: propTypes.bool.def(true),
  clearSelectOnPageChange: propTypes.bool,
  resizeHeightOffset: propTypes.number.def(0),
  rowSelection: {
    type: Object as PropType<TableRowSelection | null>,
    default: null,
  },
  title: {
    type: [String, Function] as PropType<string | ((data: Recordable) => string)>,
    default: null,
  },
  titleHelpMessage: {
    type: [String, Array] as PropType<string | string[]>,
  },
  minHeight: propTypes.number,
  maxHeight: propTypes.number,
  // 열 최대 너비를 균일하게 설정
  maxColumnWidth: propTypes.number,
  dataSource: {
    type: Array as PropType<Recordable[]>,
    default: null,
  },
  rowKey: {
    type: [String, Function] as PropType<string | ((record: Recordable) => string)>,
    default: '',
  },
  bordered: propTypes.bool,
  pagination: {
    type: [Object, Boolean] as PropType<PaginationProps | boolean>,
    default: null,
  },
  loading: propTypes.bool,
  rowClassName: {
    type: Function as PropType<(record: TableCustomRecord<any>, index: number) => string>,
  },
  scroll: {
    type: Object as PropType<{ x?: number | true; y?: number; scrollToFirstRowOnChange?: boolean }>,
    default: null,
  },
  beforeEditSubmit: {
    type: Function as PropType<(data: { record: Recordable; index: number; key: string | number; value: any }) => Promise<any>>,
  },
  size: {
    type: String as PropType<SizeType>,
    default: DEFAULT_SIZE,
  },
  expandedRowKeys: {
    type: Array,
    default: null,
  },
};
