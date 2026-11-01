import { reactive, ref, unref, defineAsyncComponent, toRaw, markRaw, isRef, watch, onUnmounted } from 'vue';
import { httpGroupRequest } from '/@/components/Form/src/utils/GroupRequest';
import { defHttp } from '/@/utils/http/axios';
import { filterMultiDictText } from '/@/utils/dict/JDictSelectUtil.js';
import { useMessage } from '/@/hooks/web/useMessage';
import { OnlineColumn } from '/@/components/sync/OnLine/types/onlineConfig';
import { h } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useMethods } from '/@/hooks/system/useMethods';
import { importViewsFile, _eval } from '/@/utils';
import {getToken} from "@/utils/auth";

export function usePopBiz(ob, tableRef?) {
  let props: any;
  if (isRef(ob)) {
    props = ob.value;
    const stopWatch = watch(ob, (newVal) => {
      props = newVal;
    });
    onUnmounted(() => stopWatch());
  } else {
    props = ob;
  }
  const { createMessage } = useMessage();
  //팝업창 표시 상태
  const visible = ref(false);
  //테이블 로딩
  const loading = ref(false);
  //cgRpConfigId
  const cgRpConfigId = ref('');
  //제목
  const title = ref('목록');
  // 정렬 필드, 기본적으로 정렬 없음
  const iSorter = ref<any>('');
  // 쿼리 객체
  const queryInfo = ref([]);
  // 쿼리 매개변수
  const queryParam = ref<any>({});
  // 동적 매개변수
  const dynamicParam = ref<any>({});
  //사전 구성 항목
  const dictOptions = ref({});
  //데이터세트
  const dataSource = ref<Array<object>>([]);
  //테이블 정보 정의
  const columns = ref<Array<object>>([]);
  // 현재 경로
  const route = useRoute();
  //요청 URL 정보 정의
  const configUrl = reactive({
    //목록 페이지에서 열과 데이터를 로드합니다.
    getColumnsAndData: '/online/cgreport/api/getColumnsAndData/',
    getColumns: '/online/cgreport/api/getRpColumns/',
    getData: '/online/cgreport/api/getData/',
    getQueryInfo: '/online/cgreport/api/getQueryInfo/',
    export: '/online/cgreport/api/exportManySheetXls/',
  });
  //선택한 값
  const checkedKeys = ref<Array<string | number>>([]);
  //선택한 행 레코드
  const selectRows = ref<Array<any>>([]);
  // 팝업을 위해서는 셀을 클릭하여 행을 선택해야 하지만 보고서 미리보기에서는 필요하지 않습니다.
  let clickThenCheckFlag = true;
  if (props.clickToRowSelect === false) {
    clickThenCheckFlag = false;
  }

  /**
   * 컬럼 구성 선택
   */
  const rowSelection = {
    fixed: true,
    type: props.multi ? 'checkbox' : 'radio',
    selectedRowKeys: checkedKeys,
    selectionRows: selectRows,
    onChange: onSelectChange,
  };

  /**
   * 일련번호 열 구성
   */
  const indexColumnProps = {
    dataIndex: 'index',
    width: '15px',
  };
  /**
   * 페이징 구성
   */
  const pagination = reactive({
    current: 1,
    pageSize: 10,
    pageSizeOptions: ['10', '20', '30'],
    // showTotal: (total, range) => {
    //     return range[0] + '-' + range[1] + ' 총 ' + total + '건'
    // },
    showQuickJumper: true,
    showSizeChanger: true,
    total: 0,
    // 집계 논리 [3.0으로 최적화 예정]
    showTotal: (total) => onShowTotal(total),
    realPageSize: 10,
    realTotal: 0,
    // 총계 컬럼 유무, 기본값은"", 처음으로 데이터를 얻은 후 참 또는 거짓으로 설계됩니다.
    isTotal: <string | boolean>'',
    onShowSizeChange: (current, pageSize) => onSizeChange(current, pageSize),
  });

  /**
   * 테이블 선택 이벤트
   * @param selectedRowKeys
   * @param selectRow
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    if (!props.multi) {
      selectRows.value = [];
      checkedKeys.value = [];
      selectedRowKeys = [selectedRowKeys[selectedRowKeys.length - 1]];
    }
    if (!selectedRowKeys || selectedRowKeys.length == 0) {
      selectRows.value = [];
      checkedKeys.value = [];
    } else {
      if (selectRows.value.length > selectedRowKeys.length) {
        // 취소
        selectRows.value.forEach((item, index) => {
          const rowKey = combineRowKey(item);
          if (!selectedRowKeys.find((key) => key === rowKey)) {
            selectRows.value.splice(index, 1);
          }
        });
      } else {
        // 등록
        const append: any = [];
        const beforeRowKeys = selectRows.value.map((item) => combineRowKey(item));
        selectedRowKeys.forEach((key) => {
          if (!beforeRowKeys.find((item) => item === key)) {
            // 즉, 선택한 행을 추가하는 것입니다.
            const row = getRowByKey(key);
            row && append.push(row);
          }
        });
        selectRows.value = [...selectRows.value, ...append];
      }
      checkedKeys.value = [...selectedRowKeys];
    }
  }
  /**
   * 쓸모없는 옵션 필터링
   * @param selectedRowKeys
   */
  function filterUnuseSelect() {
    selectRows.value = unref(selectRows).filter((item) => {
      let combineKey = combineRowKey(item);
      return unref(checkedKeys).indexOf(combineKey) >= 0;
    });
  }

  /**
   * 키를 기반으로 행 정보 가져오기
   * @param key
   */
  function getRowByKey(key) {
    let row = unref(dataSource).filter((record) => combineRowKey(record) === key);
    return row && row.length > 0 ? row[0] : '';
  }

  /**
   * rowKey 로드
   */
  function combineRowKey(record) {
    let res = record?.id || '';
    Object.keys(record).forEach((key) => {
      res = key == 'rowIndex' ? record[key] + res : res + record[key];
    });
    res = res.length > 50 ? res.substring(0, 50) : res;
    return res;
  }

  /**
   * 열 정보 로드
   */
  function loadColumnsInfo() {
    let url = `${configUrl.getColumns}${props.code}`;
    //캐시 키
    let groupIdKey = props.groupId ? `${props.groupId}${url}` : '';
    httpGroupRequest(() => defHttp.get({ url }, { isTransformResponse: false, successMessageMode: 'none' }), groupIdKey).then((res) => {
      if (res.success) {
        initDictOptionData(res.result.dictOptions);
        cgRpConfigId.value = res.result.cgRpConfigId;
        title.value = res.result.cgRpConfigName;
        let currColumns = res.result.columns;
        for (let a = 0; a < currColumns.length; a++) {
          if (currColumns[a].customRender) {
            let dictCode = currColumns[a].customRender;
            currColumns[a].customRender = ({ text }) => {
              return filterMultiDictText(unref(dictOptions)[dictCode], text + '');
            };
          }
          // 정렬 필드 제어
          if (unref(iSorter) && currColumns[a].dataIndex === unref(iSorter).column) {
            currColumns[a].sortOrder = unref(iSorter).order === 'asc' ? 'ascend' : 'descend';
          }
        }
        if (currColumns[0].key !== 'rowIndex') {
          currColumns.unshift({
            title: '일련번호',
            dataIndex: 'rowIndex',
            key: 'rowIndex',
            width: 60,
            align: 'center',
            customRender: function ({ text }) {
              if (text == undefined) {
                return '';
              } else {
                return parseInt(text) + 1;
              }
            },
          });
        }
        columns.value = [...currColumns];
        initQueryInfo(null);
      }
    });
  }

  /**
   * 열 및 데이터 로드[목록만]
   */
  function loadColumnsAndData() {
    // 처음 로드할 때 isTotal을 공백으로 두고 여기에서 호출하여 이 메서드가 페이지에 들어간 후 한 번만 로드되도록 합니다. 이 메서드는 다른 쿼리에 사용되지 않습니다.
    pagination.isTotal = '';
    let url = `${configUrl.getColumnsAndData}${props.id}`;
    //캐시 키
    let groupIdKey = props.groupId ? `${props.groupId}${url}` : '';
    httpGroupRequest(() => defHttp.get({ url }, { isTransformResponse: false, successMessageMode: 'none' }), groupIdKey).then((res) => {
      if (res.success) {
        initDictOptionData(res.result.dictOptions);
        cgRpConfigId.value = props.id;
        let { columns: metaColumnList, cgreportHeadName, fieldHrefSlots, isGroupTitle } = res.result;
        title.value = cgreportHeadName;
        // href 점프
        const fieldHrefSlotKeysMap = {};
        fieldHrefSlots.forEach((item) => (fieldHrefSlotKeysMap[item.slotName] = item));
        let currColumns = handleColumnHrefAndDict(metaColumnList, fieldHrefSlotKeysMap);

        // 팝업에는 일련 번호가 필요하지만 일반 목록에는 그렇지 않습니다.
        if (clickThenCheckFlag === true) {
          currColumns.unshift({
            title: '일련번호',
            dataIndex: 'rowIndex',
            key: 'rowIndex',
            width: 60,
            align: 'center',
            customRender: function ({ text }) {
              return parseInt(text) + 1;
            },
          });
        }

        // 헤더 병합
        if (isGroupTitle === true) {
          currColumns = handleGroupTitle(currColumns);
        }
        columns.value = [...currColumns];
        initQueryInfo(res.result.data);
      } else {
        createMessage.warning(res.message);
      }
    });
  }

  /**
   * 열 처리 합계 집계 논리 [3.0으로 최적화 예정]
   */
  function handleSumColumn(metaColumnList: OnlineColumn[], dataTotal: number): void {
    // 합산이 필요한 열의 dataIndex를 가져옵니다.
    let sumColumnList = getNeedSumColumns(metaColumnList);
    // 데이터를 처음으로 얻는 것인지 확인합니다. 그렇다면 pageSize를 재설정해야 합니다.
    if (pagination.isTotal == '') {
      if (sumColumnList.length > 0) {
        pagination.isTotal = true;
        // 전체 필드가 ​​있는 경우 매번 최대 원본 pageSize-1 레코드를 쿼리할 수 있으며, 쿼리된 레코드 10개 중 마지막 레코드를 처음 삭제해야 합니다.
        // 마지막 데이터를 삭제합니다. 처음 얻은 데이터의 길이가 pageSize와 같으면 마지막 데이터를 삭제합니다.
        if (dataSource.value.length == pagination.pageSize) {
          let remove_data = dataSource.value.pop();
        }
        pagination.realPageSize = pagination.pageSize - 1;
      } else {
        pagination.isTotal = false;
      }
    }
    // 전체 필드를 추가해야 합니다.
    if (pagination.isTotal) {
      let totalRow = {};
      sumColumnList.forEach((dataIndex) => {
        let count = 0;
        dataSource.value.forEach((row) => {
          // 통계는 null 및 빈 데이터를 제거합니다.
          if (row[dataIndex] != null && row[dataIndex] != '') {
            count += parseFloat(row[dataIndex]);
          }
        });
        totalRow[dataIndex] = isNaN(count) ? '디지털이 아닌 콘텐츠가 포함되어 있습니다.' : count.toFixed(2);

        // 긴 모양을 사용하는 경우 합계에 .00 접미사가 표시되지 않습니다.
        let v = metaColumnList.find((v) => v.dataIndex == dataIndex);
        if (v && v.fieldType == 'Long') {
          totalRow[dataIndex] = parseInt(totalRow[dataIndex]);
        }
      });
      dataSource.value.push(totalRow);
      pagination.realTotal = dataTotal;
      pagination.total = Number(dataTotal) + Number(Math.floor(dataTotal / pagination.realPageSize));
    }
  }

  /**
   * 합산해야 하는 열 dataIndex를 가져옵니다.
   * @param columns
   */
  function getNeedSumColumns(columns: OnlineColumn[]): string[] {
    let arr: string[] = [];
    for (let column of columns) {
      if (column.isTotal === '1') {
        arr.push(column.dataIndex!);
      }
        // 【VUEN-1569】【온라인 보고서】잘못된 합계
      if (column.children && column.children.length > 0) {
        let subArray = getNeedSumColumns(column.children);
        if (subArray.length > 0) {
          arr.push(...subArray);
        }
      }
    }
    return arr;
  }

  /**
   * 열 href 및 사전 번역 처리
   */
  function handleColumnHrefAndDict(columns: OnlineColumn[], fieldHrefSlotKeysMap: {}): OnlineColumn[] {
    for (let column of columns) {
      let { customRender, hrefSlotName, fieldType } = column;
      // 온라인 보고서의 유형은 날짜로 구성됩니다.（yyyy-MM-dd ），그러나 실제 표시는 날짜 및 시간 형식입니다.(yyyy-MM-dd HH:mm:ss) issues/3042
      if (fieldType == 'Date') {
        column.customRender = ({ text }) => {
          if (!text) {
            return '';
          }
          if (text.length > 10) {
            return text.substring(0, 10);
          }
          return text;
        };
      } else {
        if (!hrefSlotName && column.scopedSlots && column.scopedSlots.customRender) {
          //【온라인 보고서】Dictionary와 href는 상호 배타적입니다. 여기서 먼저 fieldHrefSlotKeysMap을 통해 href인 열을 찾습니다.
          if (fieldHrefSlotKeysMap.hasOwnProperty(column.scopedSlots.customRender)) {
            hrefSlotName = column.scopedSlots.customRender;
          }
        }
        // customRender에 값이 있으면 사전이 사용된다는 의미입니다.
        // hrefSlotName에 값이 있으면 href 점프가 사용된다는 의미입니다.
        // 둘 다 호환됩니다.호환성About 구체적인 아이디어는：먼저 사전으로 대체된 값을 얻은 다음 href 링크 점프를 추가하세요.
        if (customRender || hrefSlotName) {
          let dictCode = customRender as string;
          let replaceFlag = '_replace_text_';
          column.customRender = ({ text, record }) => {
            let value = text;
            // dictCode에 값이 있으면 사전 변환을 수행합니다.
            if (dictCode) {
              if (dictCode.startsWith(replaceFlag)) {
                let textFieldName = dictCode.replace(replaceFlag, '');
                value = record[textFieldName];
              } else {
                value = filterMultiDictText(unref(dictOptions)[dictCode], text + '');
              }
            }
            // 확장 매개변수는 열의 내용 길이를 설정합니다.
            if (column.showLength) {
              if (value && value.length > column.showLength) {
                value = value.substr(0, column.showLength) + '...';
              }
            }
            // hrefSlotName에 값이 있으면 사전을 래핑하고 교체하기 위한 태그가 생성됩니다.（또는 원주민）값
            if (hrefSlotName) {
              let field = fieldHrefSlotKeysMap[hrefSlotName];
              if (field) {
                return h(
                  'a',
                  {
                    onClick: () => handleClickFieldHref(field, record),
                  },
                  value
                );
              }
            }
            return value;
          };
        }
      }
    }
    return columns;
  }

  /**
   * 병합 헤더 처리
   * @param columns
   */
  function handleGroupTitle(columns: OnlineColumn[]): OnlineColumn[] {
    let newColumns: OnlineColumn[] = [];
    for (let column of columns) {
      //정렬 필드 제어  ---- 본 로직은 새로 추가될 로직입니다.
      if (unref(iSorter) && column.dataIndex === unref(iSorter).column) {
        column.sortOrder = unref(iSorter).order === 'asc' ? 'ascend' : 'descend';
      }
      //필드를 헤더와 병합해야 하는지 여부 결정
      if (column.groupTitle) {
        let clIndex = newColumns.findIndex((im) => im.title === column.groupTitle);
        if (clIndex !== -1) {
          //테이블 헤더가 이미 존재하며 하위 요소를 직접 푸시합니다.
          newColumns[clIndex].children!.push(column);
        } else {
          //헤더에 어셈블리 헤더 정보가 없습니다.
          let clGroup: OnlineColumn = {},
            child: OnlineColumn[] = [];
          child.push(column);
          clGroup.title = column.groupTitle;
          clGroup.align = 'center';
          clGroup.children = child;
          newColumns.push(clGroup);
        }
      } else {
        newColumns.push(column);
      }
    }
    return newColumns;
  }

  // 라우터 객체 href 점프를 사용하여 가져오기
  let router = useRouter();
  /**
   * href 클릭 이벤트
   * @param field
   * @param record
   */
  function handleClickFieldHref(field, record) {
    let href = field.href;
    let urlPattern = /(ht|f)tp(s?)\:\/\/[0-9a-zA-Z]([-.\w]*[0-9a-zA-Z])*(:(0-9)*)*(\/?)([a-zA-Z0-9\-\.\?\,\'\/\\\+&amp;%\$#_]*)?/;
    let compPattern = /\.vue(\?.*)?$/;
    let jsPattern = /{{([^}]+)}}/g; // {{ xxx }}
    if (typeof href === 'string') {
      href = href.trim().replace(/\${([^}]+)?}/g, (s1, s2) => record[s2]);
      // 구현하다 {{...}} JS 향상된 문
      if (jsPattern.test(href)) {
        href = href.replace(jsPattern, function (text, s0) {
          try {
            // 지원하다 {{ ACCESS_TOKEN }} 자리 표시자
            if (s0.trim() === 'ACCESS_TOKEN') {
              return getToken()
            }

            return _eval(s0);
          } catch (e) {
            console.error(e);
            return text;
          }
        });
      }
      if (urlPattern.test(href)) {
        window.open(href, '_blank');
      } else if (compPattern.test(href)) {
        // 팝업 처리
        openHrefCompModal(href);
      } else {
        router.push(href);
      }
    }
  }

  /**
   * Export
   */
  function handleExport() {
    const { handleExportXls } = useMethods();
    let url = `${configUrl.export}${cgRpConfigId.value}`;
    let params = getQueryParams(); //쿼리 조건
    // 【VUEN-1568】일부 행을 선택하면 선택한 행만 내보내집니다.
    let keys = unref(checkedKeys);
    if (keys.length > 0) {
      params['force_id'] = keys
        .map((i) => selectRows.value.find((item) => combineRowKey(item) === i)?.id)
        .filter((i) => i != null && i !== '')
        .join(',');
    }
    handleExportXls(title.value, url, params);
  }

  /**
   * 집계 논리 [3.0으로 최적화 예정]
   * 페이지네이션 크기 변경 이벤트
   * @param _current
   * @param size
   */
  function onSizeChange(_current, size) {
    pagination.isTotal = '';
    pagination.pageSize = size;
    if (pagination.isTotal) {
      pagination.realPageSize = size - 1;
    } else {
      pagination.realPageSize = size;
    }
    pagination.current = 1;
  }

  /**
   *  집계 논리 [3.0으로 최적화 예정]
   * 총 항목 수 표시
   * @param total
   */
  function onShowTotal(total) {
    // 합계가 있는지 여부에 따라 각 페이지에 표시되는 데이터를 다시 계산합니다.
    let start = (pagination.current - 1) * pagination.realPageSize + 1;
    let end = start + (pagination.isTotal ? dataSource.value.length - 1 : dataSource.value.length) - 1;
    let realTotal = pagination.isTotal ? pagination.realTotal : total;
    return start + '-' + end + ' 총 ' + realTotal + '건';
  }

  /**
   * 팝업 상자에 숨겨진 트리거 이벤트가 표시됩니다.
   */
  async function visibleChange($event) {
    visible.value = $event;
    $event && loadColumnsInfo();
  }

  /**
   * 쿼리 조건 초기화
   * @param 데이터 데이터 결과 세트
   */
  function initQueryInfo(data) {
    let url = `${configUrl.getQueryInfo}${unref(cgRpConfigId)}`;
    //캐시 키
    let groupIdKey = props.groupId ? `${props.groupId}${url}` : '';
    httpGroupRequest(() => defHttp.get({ url }, { isTransformResponse: false, successMessageMode: 'none' }), groupIdKey).then((res) => {
      // console.log("쿼리 조건 가져오기", res);
      if (res.success) {
        dynamicParamHandler(res.result);
        queryInfo.value = res.result;
        console.log('queryInfo==>', queryInfo.value);
        //쿼리 조건이 로드된 후 데이터 요청
        if (data) {
          setDataSource(data);
          //라우팅 매개변수와 동적 매개변수 전달은 적용되지 않습니다.
          loadData(1);
        } else {
          //데이터가 전달되지 않은 경우 데이터 쿼리
          loadData(1);
        }
      } else {
        createMessage.warning(res.message);
      }
    });
  }

  /**
   * 테이블 데이터 로드
   * @param arg
   */
  function loadData(arg?) {
    if (arg == 1) {
      pagination.current = 1;
    }
    let params = getQueryParams(); //쿼리 조건
    params['onlRepUrlParamStr'] = getUrlParamString();
    console.log('params', params);
    loading.value = true;
    let url = `${configUrl.getColumnsAndData}${unref(cgRpConfigId)}`;
    //캐시 키
    let groupIdKey = props.groupId ? `${props.groupId}${url}${JSON.stringify(params)}` : '';
    httpGroupRequest(() => defHttp.get({ url, params }, { isTransformResponse: false, successMessageMode: 'none' }), groupIdKey).then((res) => {
      res.result.dictOptions && initDictOptionData(res.result.dictOptions);
      loading.value = false;
      let data = res.result.data;
      console.log('Form 정보:', data);
      setDataSource(data);
    });
  }

  /**
   * 주소 표시줄의 매개변수 가져오기
   */
  function getUrlParamString() {
   let query = route.query;
   let arr:any[] = []
   if(query && Object.keys(query).length>0){
     Object.keys(query).map(k=>{
       arr.push(`${k}=${query[k]}`)
     })
   }
   return arr.join('&')
  }

  /**
   * 데이터 소스 설정
   */
  function setDataSource(data) {
    if (data) {
      pagination.total = Number(data.total);
      let currentPage = pagination?.current ?? 1;
      for (let a = 0; a < data.records.length; a++) {
        if (!data.records[a].rowIndex) {
          data.records[a].rowIndex = a + (currentPage - 1) * 10;
        }
      }
      dataSource.value = data.records;
      tableRef?.value && tableRef?.value?.setPagination({
        total: Number(data.total)
      })
    } else {
      pagination.total = 0;
      dataSource.value = [];
    }
    // 집계 논리 [3.0으로 최적화 예정]
    handleSumColumn(columns.value, pagination.total);
  }

  /**
   * 쿼리 매개변수 가져오기
   */
  function getQueryParams() {
    let paramTarget = {};
    if (unref(dynamicParam)) {
      //맞춤 매개변수 처리
      Object.keys(unref(dynamicParam)).map((key) => {
        paramTarget['self_' + key] = unref(dynamicParam)[key];
      });
    }
    let param = Object.assign(paramTarget, unref(queryParam), unref(iSorter));
    param.pageNo = pagination.current;
    // 집계 논리 [3.0으로 최적화 예정]
    //  실제 쿼리에서는 테이블 컴포넌트의 pageSize가 사용되지 않지만 사용자 정의 realPageSize가 처음으로 데이터를 얻은 후에 변경됩니다.
    param.pageSize = pagination.realPageSize;
    return filterObj(param);
  }

  /**
   * 동적 매개변수 처리
   */
  function dynamicParamHandler(arr?) {
    if (arr && arr.length > 0) {
      //쿼리 조건을 처음 로드하기 전에 queryParam을 빈 개체로 초기화합니다.
      let queryTemp = {};
      for (let item of arr) {
        if (item.mode === 'single') {
          queryTemp[item.field] = '';
        }
      }
      queryParam.value = { ...queryTemp };
    }
    // 라우팅 매개변수 병합
    if (props.routeQuery) {
      queryParam.value = Object.assign(queryParam.value, props.routeQuery);
    }

    let dynamicTemp = {};
    if (props.param) {
      Object.keys(props.param).map((key) => {
        let str = props.param[key];
        if (key in queryParam) {
          if (str && str.startsWith("'") && str.endsWith("'")) {
            str = str.substring(1, str.length - 1);
          }
          //쿼리 조건에 매개변수가 포함된 경우 값을 설정하세요.
          unref(queryParam)[key] = str;
        }
        dynamicTemp[key] = props.param[key];
      });
    }
    dynamicParam.value = { ...dynamicTemp };
  }

  /**
   * 쪽수 매기기
   * @param page
   * @param filters
   * @param sorter
   */
  function handleChangeInTable(page, filters, sorter) {
    console.log(page, filters, sorter);
    //페이징, 정렬, 필터링 변경 시 트리거됨
    if (Object.keys(sorter).length > 0) {
      iSorter.value = {
        column: sorter.field,
        order: 'ascend' === sorter.order ? 'asc' : 'desc',
      };
      // 정렬 필드 제어
      unref(columns).forEach((col) => {
        if (col['dataIndex'] === sorter.field) {
          col['sortOrder'] = sorter.order;
        }
      });
    }
    pagination.current = page.current;
    pagination.pageSize = page.pageSize;
    loadData();
  }

  /**
   * 행 클릭 이벤트
   * @param record
   */
  function clickThenCheck(record) {
    if (clickThenCheckFlag === true) {
      if (!props.multi) {
        selectRows.value = [];
        checkedKeys.value = [];
      }
      let rowKey = combineRowKey(record);
      if (!unref(checkedKeys) || unref(checkedKeys).length == 0) {
        let arr1: any[] = [],
          arr2: any[] = [];
        arr1.push(record);
        arr2.push(rowKey);
        checkedKeys.value = arr2;
        //selectRows.value = arr1;
      } else {
        if (unref(checkedKeys).indexOf(rowKey) < 0) {
          //존재하지 않는 경우 선택
          checkedKeys.value.push(rowKey);
          //selectRows.value.push(record);
        } else {
          //선택한 경우 취소
          let rowKey_index = unref(checkedKeys).indexOf(rowKey);
          checkedKeys.value.splice(rowKey_index, 1);
          //selectRows.value.splice(rowKey_index, 1);
        }
      }
      tableRef.value.setSelectedRowKeys([...checkedKeys.value]);
    }
  }

  //사전의 정크 데이터 방지
  function initDictOptionData(arr) {
    let obj = {};
    Object.keys(arr).map((k) => {
      obj[k] = arr[k].filter((item) => {
        return item != null;
      });
    });
    dictOptions.value = obj;
  }

  /**
   * 객체의 빈 속성 필터링
   * @param obj
   * @returns {*}
   */
  function filterObj(obj) {
    if (!(typeof obj == 'object')) {
      return;
    }

    for (let key in obj) {
      if (obj.hasOwnProperty(key) && (obj[key] == null || obj[key] == undefined || obj[key] === '')) {
        delete obj[key];
      }
    }
    return obj;
  }

  // 스타일
  const dialogStyle = {
    top: 0,
    left: 0,
    height: '100%',
    margin: 0,
    padding: 0,
  };

  // 팝업창 속성 설정
  const hrefComponent = ref({
    model: {
      title: '',
      okText: '취소',
      width: '100%',
      open: false,
      destroyOnClose: true,
      style: dialogStyle,
      // dialogStyle: dialogStyle,
      bodyStyle: {
        padding: '8px',
        height: 'calc(100vh - 108px)',
        overflow: 'auto',
        overflowX: 'hidden',
      },
      // 취소 버튼 숨기기
      cancelButtonProps: { style: { display: 'none' } },
    },
    on: {
      ok: () => (hrefComponent.value.model.open = false),
      cancel: () => (hrefComponent.value.model.open = false),
    },
    is: <any>null,
    params: {},
  });

  // 하이퍼링크 클릭 이벤트--> 모달 창 열기
  function openHrefCompModal(href) {
    // href 매개변수 구문 분석
    let index = href.indexOf('?');
    let path = href;
    if (index !== -1) {
      path = href.substring(0, index);
      let paramString = href.substring(index + 1, href.length);
      let paramArray = paramString.split('&');
      let params = {};
      paramArray.forEach((paramObject) => {
        let paramItem = paramObject.split('=');
        params[paramItem[0]] = paramItem[1];
      });
      hrefComponent.value.params = params;
    } else {
      hrefComponent.value.params = {};
    }
    hrefComponent.value.model.open = true;
    hrefComponent.value.model.title = '작업';
    hrefComponent.value.is = markRaw(defineAsyncComponent(() => importViewsFile(path)));
  }

  /**
   * 방출 이벤트는 선택된 행 데이터를 가져옵니다.
   */
  function getOkSelectRows(): any[] {
    let arr = unref(selectRows);
    let selectedRowKeys = checkedKeys.value;
    console.log('arr', arr);
    if (!selectedRowKeys || selectedRowKeys.length <= 0) {
      return [];
    }
    if (!arr || arr.length <= 0) {
      return [];
    }
    let rows: any = [];
    for (let key of selectedRowKeys) {
      for (let i = 0; i < arr.length; i++) {
        let combineKey = combineRowKey(arr[i]);
        if (key === combineKey) {
          rows.push(toRaw(arr[i]));
          break;
        }
      }
    }
    return rows;
  }

  return [
    {
      visibleChange,
      loadColumnsInfo,
      loadColumnsAndData,
      dynamicParamHandler,
      loadData,
      handleChangeInTable,
      combineRowKey,
      clickThenCheck,
      filterUnuseSelect,
      handleExport,
      getOkSelectRows,
    },
    {
      hrefComponent,
      visible,
      rowSelection,
      checkedKeys,
      selectRows,
      pagination,
      dataSource,
      columns,
      indexColumnProps,
      loading,
      title,
      iSorter,
      queryInfo,
      queryParam,
      dictOptions,
    },
  ];
}
