import type { VNodeChild } from 'vue';
import type { PaginationProps } from './pagination';
import type { FormProps } from '/@/components/Form';
import type { TableRowSelection as ITableRowSelection } from 'ant-design-vue/lib/table/interface';
import type { ColumnProps } from 'ant-design-vue/lib/table';

import { ComponentType } from './componentType';
import { VueNode } from '/@/utils/propTypes';
import { RoleEnum } from '/@/enums/roleEnum';

export declare type SortOrder = 'ascend' | 'descend';

export interface TableCurrentDataSource<T = Recordable> {
  currentDataSource: T[];
}

export interface TableRowSelection<T = any> extends ITableRowSelection {
  /**
   * Callback executed when selected rows change
   * @type Function
   */
  onChange?: (selectedRowKeys: string[] | number[], selectedRows: T[]) => any;

  /**
   * Callback executed when select/deselect one row
   * @type Function
   */
  onSelect?: (record: T, selected: boolean, selectedRows: Object[], nativeEvent: Event) => any;

  /**
   * Callback executed when select/deselect all rows
   * @type Function
   */
  onSelectAll?: (selected: boolean, selectedRows: T[], changeRows: T[]) => any;

  /**
   * Callback executed when row selection is inverted
   * @type Function
   */
  onSelectInvert?: (selectedRows: string[] | number[]) => any;
}

export interface TableCustomRecord<T> {
  record?: T;
  index?: number;
}

export interface ExpandedRowRenderRecord<T> extends TableCustomRecord<T> {
  indent?: number;
  expanded?: boolean;
}

export interface ColumnFilterItem {
  text?: string;
  value?: string;
  children?: any;
}

export interface TableCustomRecord<T = Recordable> {
  record?: T;
  index?: number;
}

export interface SorterResult {
  column: ColumnProps;
  order: SortOrder;
  field: string;
  columnKey: string;
}

export interface FetchParams {
  searchInfo?: Recordable;
  page?: number;
  sortInfo?: Recordable;
  filterInfo?: Recordable;
}

export interface GetColumnsParams {
  ignoreIndex?: boolean;
  ignoreAction?: boolean;
  sort?: boolean;
}

export type SizeType = 'middle' | 'small' | 'large';

export interface TableActionType {
  reload: (opt?: FetchParams) => Promise<void>;
  getSelectRows: <T = Recordable>() => T[];
  clearSelectedRowKeys: () => void;
  expandAll: () => void;
  collapseAll: () => void;
  getSelectRowKeys: () => string[];
  deleteSelectRowByKey: (key: string) => void;
  setPagination: (info: Partial<PaginationProps>) => void;
  setTableData: <T = Recordable>(values: T[]) => void;
  updateTableDataRecord: (rowKey: string | number, record: Recordable) => Recordable | void;
  deleteTableDataRecord: (rowKey: string | number | string[] | number[]) => void;
  insertTableDataRecord: (record: Recordable, index?: number) => Recordable | void;
  findTableDataRecord: (rowKey: string | number) => Recordable | void;
  getColumns: (opt?: GetColumnsParams) => BasicColumn[];
  setColumns: (columns: BasicColumn[] | string[]) => void;
  getDataSource: <T = Recordable>() => T[];
  getRawDataSource: <T = Recordable>() => T;
  setLoading: (loading: boolean) => void;
  setProps: (props: Partial<BasicTableProps>) => void;
  redoHeight: () => void;
  setSelectedRowKeys: (rowKeys: string[] | number[]) => void;
  getPaginationRef: () => PaginationProps | boolean;
  getSize: () => SizeType;
  getRowSelection: () => TableRowSelection<Recordable>;
  getCacheColumns: () => BasicColumn[];
  emit?: EmitType;
  updateTableData: (index: number, key: string, value: any) => Recordable;
  setShowPagination: (show: boolean) => Promise<void>;
  getShowPagination: () => boolean;
  setCacheColumnsByField?: (dataIndex: string | undefined, value: BasicColumn) => void;
}

export interface FetchSetting {
  // 인터페이스의 현재 페이지 번호를 요청합니다.
  pageField: string;
  // 페이지당 표시할 항목 수
  sizeField: string;
  // 요청 결과 목록 필드는 a.b.c를 지원합니다.
  listField: string;
  // 요청 결과 총계 필드는 a.b.c를 지원합니다.
  totalField: string;
}

export interface TableSetting {
  // 새로고침 버튼 표시 여부
  redo?: boolean;
  // 크기 조정 버튼 표시 여부
  size?: boolean;
  // 필드 조정 버튼 표시 여부
  setting?: boolean;
  // 페이지에 구별해야 하는 테이블이 여러 개 있을 때 사용되는 "필드 조정" 구성 키를 캐시합니다.
  cacheKey?: string;
  // 전체 화면 버튼 표시 여부
  fullScreen?: boolean;
}

export interface BasicTableProps<T = any> {
  // 클릭 행 선택.
  clickToRowSelect?: boolean;
  isTreeTable?: boolean;
  // 사용자 정의 정렬 방법
  sortFn?: (sortInfo: SorterResult) => any;
  // 정렬 방법
  filterFn?: (data: Partial<Recordable<string[]>>) => any;
  // 테이블의 기본 패딩 취소
  inset?: boolean;
  // 테이블 설정 표시
  showTableSetting?: boolean;
  // 테이블 위의 조작 버튼 설정
  tableSetting?: TableSetting;
  // 얼룩말 인쇄
  striped?: boolean;
  // 키 자동 생성 여부
  autoCreateKey?: boolean;
  // 총 행을 계산하는 방법
  summaryFunc?: (...arg: any) => Recordable[];
  // 전체 표 내용 사용자 정의
  summaryData?: Recordable[];
  // 총 행 표시 여부
  showSummary?: boolean;
  // 열을 끌어서 놓을 수 있는지 여부
  canColDrag?: boolean;
  // 인터페이스 요청 객체
  api?: (...arg: any) => Promise<any>;
  // 요청하기 전에 매개변수를 처리하세요.
  beforeFetch?: Fn;
  // 사용자 정의 처리 인터페이스 반환 매개변수
  afterFetch?: Fn;
  // 쿼리 조건은 요청하기 전에 처리됩니다.
  handleSearchInfoFn?: Fn;
  // 인터페이스 구성 요청
  fetchSetting?: Partial<FetchSetting>;
  // 지금 인터페이스 요청
  immediate?: boolean;
  // 검색폼 오픈 시, 데이터가 없을 경우 테이블을 표시할지 여부
  emptyDataIsShowTable?: boolean;
  // 추가 요청 매개변수
  searchInfo?: Recordable;
  // 기본 정렬 매개변수
  defSort?: Recordable;
  // 검색 Form을 사용하세요
  useSearchForm?: boolean;
  // Form 구성
  formConfig?: Partial<FormProps>;
  // 컬럼 구성
  columns: BasicColumn[];
  // 열 최대 너비를 균일하게 설정
  maxColumnWidth?: number;
  // 일련번호 열 표시 여부
  showIndexColumn?: boolean;
  // 일련번호 열 구성
  indexColumnProps?: BasicColumn;
  // 작업 열 표시 여부
  showActionColumn?: boolean;
  // 작업 열 구성
  actionColumn?: BasicColumn;
  // 텍스트가 너비를 초과하여 표시되는지 여부입니다.。。
  ellipsis?: boolean;
  // 높이 조절이 가능한가요?
  canResize?: boolean;
  // 적응형 높이 오프셋, 계산 결과 - 오프셋
  resizeHeightOffset?: number;
  // 페이지 매김이 변경되면 옵션 지우기
  clearSelectOnPageChange?: boolean;
  //
  rowKey?: string | ((record: Recordable) => string);
  // 데이터
  dataSource?: Recordable[];
  // 제목 오른쪽에 팁 표시
  titleHelpMessage?: string | string[];
  // 테이블 최소 높이
  minHeight?: number;
  // 테이블 스크롤 최대 높이
  maxHeight?: number;
  // 테두리 표시 여부
  bordered?: boolean;
  // 페이징 구성
  pagination?: PaginationProps | boolean;
  // 로드로드 중
  loading?: boolean;

  /**
   * The column contains children to display
   * @default 'children'
   * @type string | string[]
   */
  childrenColumnName?: string;

  /**
   * Override default table elements
   * @type object
   */
  components?: object;

  /**
   * Expand all rows initially
   * @default false
   * @type boolean
   */
  defaultExpandAllRows?: boolean;

  /**
   * Initial expanded row keys
   * @type string[]
   */
  defaultExpandedRowKeys?: string[];

  /**
   * Current expanded row keys
   * @type string[]
   */
  expandedRowKeys?: string[];

  /**
   * Expanded container render for each row
   * @type Function
   */
  expandedRowRender?: (record?: ExpandedRowRenderRecord<T>) => VNodeChild | JSX.Element;

  /**
   * Customize row expand Icon.
   * @type Function | VNodeChild
   */
  expandIcon?: Function | VNodeChild | JSX.Element;

  /**
   * Whether to expand row by clicking anywhere in the whole row
   * @default false
   * @type boolean
   */
  expandRowByClick?: boolean;

  /**
   * The index of `expandIcon` which column will be inserted when `expandIconAsCell` is false. default 0
   */
  expandIconColumnIndex?: number;

  /**
   * Table footer renderer
   * @type Function | VNodeChild
   */
  footer?: Function | VNodeChild | JSX.Element;

  /**
   * Indent size in pixels of tree data
   * @default 15
   * @type number
   */
  indentSize?: number;

  /**
   * i18n text including filter, sort, empty text, etc
   * @default { filterConfirm: 'Ok', filterReset: 'Reset', emptyText: 'No Data' }
   * @type object
   */
  locale?: object;

  /**
   * Row's className
   * @type Function
   */
  rowClassName?: (record: TableCustomRecord<T>, index: number) => string;

  /**
   * Row selection config
   * @type object
   */
  rowSelection?: TableRowSelection;

  /**
   * Set horizontal or vertical scrolling, can also be used to specify the width and height of the scroll area.
   * It is recommended to set a number for x, if you want to set it to true,
   * you need to add style .ant-table td { white-space: nowrap; }.
   * @type object
   */
  scroll?: { x?: number | true | 'max-content'; y?: number; scrollToFirstRowOnChange?: boolean };

  /**
   * Whether to show table header
   * @default true
   * @type boolean
   */
  showHeader?: boolean;

  /**
   * Size of table
   * @default 'default'
   * @type string
   */
  size?: SizeType;

  /**
   * Table title renderer
   * @type Function | ScopedSlot
   */
  title?: VNodeChild | JSX.Element | string | ((data: Recordable) => string);

  /**
   * Set props on per header row
   * @type Function
   */
  customHeaderRow?: (column: ColumnProps, index: number) => object;

  /**
   * Set props on per row
   * @type Function
   */
  customRow?: (record: T, index: number) => object;

  /**
   * `table-layout` attribute of table element
   * `fixed` when header/columns are fixed, or using `column.ellipsis`
   *
   * @see https://developer.mozilla.org/en-US/docs/Web/CSS/table-layout
   * @version 1.5.0
   */
  tableLayout?: 'auto' | 'fixed' | string;

  /**
   * the render container of dropdowns in table
   * @param triggerNode
   * @version 1.5.0
   */
  getPopupContainer?: (triggerNode?: HTMLElement) => HTMLElement;

  /**
   * Data can be changed again before rendering.
   * The default configuration of general user empty data.
   * You can configured globally through [ConfigProvider](https://antdv.com/components/config-provider-cn/)
   *
   * @version 1.5.4
   */
  transformCellText?: Function;

  /**
   * Callback executed before editable cell submit value, not for row-editor
   *
   * The cell will not submit data while callback return false
   */
  beforeEditSubmit?: (data: { record: Recordable; index: number; key: string | number; value: any }) => Promise<any>;

  /**
   * Callback executed when pagination, filters or sorter is changed
   * @param pagination
   * @param filters
   * @param sorter
   * @param currentDataSource
   */
  onChange?: (pagination: any, filters: any, sorter: any, extra: any) => void;

  /**
   * Callback executed when the row expand icon is clicked
   *
   * @param expanded
   * @param record
   */
  onExpand?: (expande: boolean, record: T) => void;

  /**
   * Callback executed when the expanded rows change
   * @param expandedRows
   */
  onExpandedRowsChange?: (expandedRows: string[] | number[]) => void;

  onColumnsChange?: (data: ColumnChangeParam[]) => void;
}

export type CellFormat = string | ((text: string, record: Recordable, index: number) => string | number) | Map<string | number, any>;

// @ts-ignore
export interface BasicColumn extends ColumnProps<Recordable> {
  children?: BasicColumn[];
  filters?: {
    text: string;
    value: string;
    children?: unknown[] | (((props: Record<string, unknown>) => unknown[]) & (() => unknown[]) & (() => unknown[]));
  }[];

  //
  flag?: 'INDEX' | 'DEFAULT' | 'CHECKBOX' | 'RADIO' | 'ACTION';
  customTitle?: VueNode;

  slots?: Recordable;
  // 슬롯 백업은 기존 쓰기 방식과 호환되며, 콘솔 경고를 피하기 위해 새로운 쓰기 방식으로 변환할 수 있습니다.
  slotsBak?: Recordable;

  // Whether to hide the column by default, it can be displayed in the column configuration
  defaultHidden?: boolean;

  // Help text for table column header
  helpMessage?: string | string[];

  format?: CellFormat;

  // Editable
  edit?: boolean;
  editRow?: boolean;
  editable?: boolean;
  editComponent?: ComponentType;
  editComponentProps?: Recordable;
  editRule?: boolean | ((text: string, record: Recordable) => Promise<string>);
  editValueMap?: (value: any) => string;
  onEditRow?: () => void;
  // 권한 인코딩은 표시 여부를 제어합니다.
  auth?: RoleEnum | RoleEnum[] | string | string[];
  // 비즈니스 제어 표시 여부
  ifShow?: boolean | ((column: BasicColumn) => boolean);
  //compType - 레코드 유형용
  compType?: string;
  customSummaryRender?: (opt: {
    value: any;
    text: any;
    record: Recordable;
    index: number;
    renderIndex?: number;
    column: BasicColumn;
  }) => any | VNodeChild | JSX.Element;
}

export type ColumnChangeParam = {
  dataIndex: string;
  fixed: boolean | 'left' | 'right' | undefined;
  visible: boolean;
};

export interface InnerHandlers {
  onColumnsChange: (data: ColumnChangeParam[]) => void;
}
