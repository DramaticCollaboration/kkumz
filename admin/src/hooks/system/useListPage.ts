import { reactive, ref, Ref, unref } from 'vue';
import { merge } from 'lodash-es';
import { DynamicProps } from '/#/utils';
import { BasicTableProps, TableActionType, useTable } from '/@/components/Table';
import { ColEx } from '/@/components/Form/src/types';
import { FormActionType } from '/@/components/Form';
import { useMessage } from '/@/hooks/web/useMessage';
import { useMethods } from '/@/hooks/system/useMethods';
import { useDesign } from '/@/hooks/web/useDesign';
import { filterObj } from '/@/utils/common/compUtils';
const { handleExportXls, handleImportXls } = useMethods();

// useListPage 메소드에 필요한 매개변수를 정의합니다.
interface ListPageOptions {
  // 스타일 범위
  designScope?: string;
  // 【필수의】테이블 매개변수 구성
  tableProps: TableProps;
  // 페이지 매김 여부
  pagination?: boolean;
  // 구성 내보내기
  exportConfig?: {
    url: string | (() => string);
    // 내보내기 파일 이름
    name?: string | (() => string);
    //매개변수 내보내기
    params?: object;
  };
  // 구성 가져오기
  importConfig?: {
    url: string | (() => string);
    // 내보내기 성공 후 콜백
    success?: (fileInfo?: any) => void;
  };
}

interface IDoRequestOptions {
  // 확인 대화 상자를 표시할지 여부, 기본값은 true
  confirm?: boolean;
  // 테이블을 자동으로 새로 고칠지 여부, 기본값은 true
  reload?: boolean;
  // 선택 항목을 자동으로 지울지 여부, 기본값은 true
  clearSelection?: boolean;
}

/**
 * listPage 페이지 공개 메소드
 *
 * @param options
 */
export function useListPage(options: ListPageOptions) {
  const $message = useMessage();
  let $design = {} as ReturnType<typeof useDesign>;
  if (options.designScope) {
    $design = useDesign(options.designScope);
  }

  const tableContext = useListTable(options.tableProps);

  const [, { getForm, reload, setLoading }, { selectedRowKeys }] = tableContext;

  // 엑셀 내보내기
  async function onExportXls() {
    let { url, name, params } = options?.exportConfig ?? {};
    let realUrl = typeof url === 'function' ? url() : url;
    if (realUrl) {
      let title = typeof name === 'function' ? name() : name;
      let paramsForm:any = {};
      try {
        paramsForm = await getForm().validate();
      } catch (e) {
        console.error(e);
      }

      if(!paramsForm?.column){
         Object.assign(paramsForm,{column:'createTime',order:'desc'});
      }

      //매개변수가 비어 있지 않으면 함께 통합됩니다.
      if (params) {
        Object.keys(params).map((k) => {
          let temp = (params as object)[k];
          if (temp) {
            paramsForm[k] = unref(temp);
          }
        });
      }
      if (selectedRowKeys.value && selectedRowKeys.value.length > 0) {
        paramsForm['selections'] = selectedRowKeys.value.join(',');
      }
      console.log()
      return handleExportXls(title as string, realUrl, filterObj(paramsForm));
    } else {
      $message.createMessage.warn('내보내기Config.url 매개변수가 전달되지 않았습니다.');
      return Promise.reject();
    }
  }

  // 엑셀 가져오기
  function onImportXls(file) {
    let { url, success } = options?.importConfig ?? {};
    let realUrl = typeof url === 'function' ? url() : url;
    if (realUrl) {
      return handleImportXls(file, realUrl, success || reload);
    } else {
      $message.createMessage.warn('importConfig.url 매개변수가 전달되지 않았습니다.');
      return Promise.reject();
    }
  }

  /**
   * 테이블을 자동으로 새로 고치고 선택 항목을 자동으로 지울 수 있는 범용 요청 처리 방법
   * @param API 요청 API
   * @param 확인 상자 표시 여부 옵션
   */
  function doRequest(api: () => Promise<any>, options?: IDoRequestOptions) {
    return new Promise((resolve, reject) => {
      const execute = async () => {
        try {
          setLoading(true);
          const res = await api();
          if (options?.reload ?? true) {
            reload();
          }
          if (options?.clearSelection ?? true) {
            selectedRowKeys.value = [];
          }
          resolve(res);
        } catch (e) {
          reject(e);
        } finally {
          setLoading(false);
        }
      };
      if (options?.confirm ?? true) {
        $message.createConfirm({
          iconType: 'warning',
          title: '삭제',
          content: '삭제하시겠습니까?',
          onOk: () => execute(),
          onCancel: () => reject(),
        });
      } else {
        execute();
      }
    });
  }

  /** 단일 삭제 작업 수행 */
  function doDeleteRecord(api: () => Promise<any>) {
    return doRequest(api, { confirm: false, clearSelection: false });
  }

  return {
    ...$design,
    ...$message,
    onExportXls,
    onImportXls,
    doRequest,
    doDeleteRecord,
    tableContext,
  };
}

// 테이블에 필요한 매개변수 정의
type TableProps = Partial<DynamicProps<BasicTableProps>>;
type UseTableMethod = TableActionType & {
  getForm: () => FormActionType;
};

/**
 * useListTable 목록 페이지 표준 테이블 매개변수
 *
 * @param tableProps 테이블 매개변수
 */
export function useListTable(tableProps: TableProps): [
  (instance: TableActionType, formInstance: UseTableMethod) => void,
  TableActionType & {
    getForm: () => FormActionType;
  },
  {
    rowSelection: any;
    selectedRows: Ref<Recordable[]>;
    selectedRowKeys: Ref<any[]>;
  }
] {
  // 적응형 열 구성
  const adaptiveColProps: Partial<ColEx> = {
    xs: 24, // <576px
    sm: 12, // ≥576px
    md: 8, // ≥768px
    lg: 6, // ≥992px
    xl: 6, // ≥1200px
    xxl: 6, // ≥1600px
  };
  const defaultTableProps: TableProps = {
    rowKey: 'id',
    // 검색 기준 영역 사용
    useSearchForm: true,
    // 쿼리 조건 영역 구성
    formConfig: {
      // 컴팩트 모드
      compact: true,
      // 라벨 기본 너비
      // labelWidth: 120,
      // Enter 키를 누르면 자동 제출
      autoSubmitOnEnter: true,
      // 기본 행 구성
      rowProps: { gutter: 8 },
      // 기본 열 구성
      baseColProps: {
        ...adaptiveColProps,
      },
      labelCol: {
        xs: 24,
        sm: 8,
        md: 6,
        lg: 8,
        xl: 6,
        xxl: 6,
      },
      wrapperCol: {},
      // 확장/축소 버튼 표시 여부
      showAdvancedButton: true,
      // 지정된 열 수를 초과하면 기본 축소
      autoAdvancedCol: 3,
      // 액션 버튼 구성
      actionColOptions: {
        style: { textAlign: 'right' },
      },
    },
    // 얼룩말 인쇄
    striped: false,
    // 높이 조절이 가능한가요?
    canResize: true,
    // 테이블 최소 높이
    minHeight: 300,
    // 선택하려면 행을 클릭하세요.
    clickToRowSelect: false,
    // 테두리 표시 여부
    bordered: true,
    // 일련번호 열 표시 여부
    showIndexColumn: false,
    // 테이블 설정 표시
    showTableSetting: true,
    // 테이블 전체 화면 설정
    tableSetting: {
      fullScreen: false,
    },
    // 작업 열 표시 여부
    showActionColumn: true,
    // 작업란
    actionColumn: {
      width: 120,
      title: '작업',
      //작업 열 값을 잠글지 여부 right, left, false
      fixed: false,
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  };
  // 사용자 개인화 구성 병합
  if (tableProps) {
    if(tableProps.formConfig){
      setTableProps(tableProps.formConfig);
    }
    // 병합 방법은 객체를 깊게 병합할 수 있습니다.
    merge(defaultTableProps, tableProps);
  }

  // 요청을 보내기 전에 호출되는 메서드
  function beforeFetch(params) {
    // 기본적으로 createTime을 기준으로 내림차순 정렬
    return Object.assign({ column: 'createTime', order: 'desc' }, params);
  }

  // 병합 방법
  Object.assign(defaultTableProps, { beforeFetch });
  if (typeof tableProps.beforeFetch === 'function') {
    defaultTableProps.beforeFetch = function (params) {
      params = beforeFetch(params);
      // @ts-ignore
      tableProps.beforeFetch(params);
      return params;
    };
  }

  // 현재 선택된 행
  const selectedRowKeys = ref<any[]>([]);
  // 선택한 행 레코드
  const selectedRows = ref<Recordable[]>([]);

  // 테이블 선택 열 구성
  const rowSelection: any = tableProps?.rowSelection ?? {};
  const defaultRowSelection = reactive({
    ...rowSelection,
    type: rowSelection.type ?? 'checkbox',
    // 열 너비를 선택하세요. 기본값은 50입니다.
    columnWidth: rowSelection.columnWidth ?? 50,
    selectedRows: selectedRows,
    selectedRowKeys: selectedRowKeys,
    onChange(...args) {
      selectedRowKeys.value = args[0];
      selectedRows.value = args[1];
      if (typeof rowSelection.onChange === 'function') {
        rowSelection.onChange(...args);
      }
    },
  });
  delete defaultTableProps.rowSelection;

  /**
   * 테이블 매개변수 설정
   *
   * @param formConfig
   */
  function setTableProps(formConfig: any) {
    const replaceAttributeArray: string[] = ['baseColProps','labelCol'];
    for (let item of replaceAttributeArray) {
      if(formConfig && formConfig[item]){
        if(defaultTableProps.formConfig){
          let defaultFormConfig:any = defaultTableProps.formConfig;
          defaultFormConfig[item] = formConfig[item];
        }
        formConfig[item] = {};
      }
    }
  }

  return [
    ...useTable(defaultTableProps),
    {
      selectedRows,
      selectedRowKeys,
      rowSelection: defaultRowSelection,
    },
  ];
}
