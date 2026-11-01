import { inject, reactive, ref, watch, unref, Ref } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { isEmpty } from '@/utils/is';

export function useSelectBiz(getList, props, emit) {
  //드롭다운 상자 옵션 수신
  const selectOptions = inject('selectOptions', ref<Array<object>>([]));
  //선택한 값 받기
  const selectValues = <object>inject('selectValues', reactive({ value: [], change: false }));
  // 에코가 로드 중인지 여부
  const loadingEcho = inject<Ref<boolean>>('loadingEcho', ref(false));
  //데이터세트
  const dataSource = ref<Array<object>>([]);
  //선택한 값
  const checkedKeys = ref<Array<string | number>>([]);
  //선택한 행 레코드
  const selectRows = ref<Array<object>>([]);
  //프롬프트 팝업 창
  const $message = useMessage();
  // 에코를 처음 로드하는 경우에도 처음 로드할 때만 로딩이 표시됩니다.
  let isFirstLoadEcho = true;

  /**
   * selectValues ​​변경 사항 모니터링
   */
  watch(
    selectValues,
    () => {
      //if (selectValues['change'] == false && !isEmpty(selectValues['value'])) {
      if (selectValues['change'] == false && !isEmpty(selectValues['value'])) {
        let params = { isMultiTranslate: 'true' };
        params[props.rowKey] = selectValues['value'].join(',');
        loadingEcho.value = isFirstLoadEcho;
        isFirstLoadEcho = false;
        getDataSource(params, true)
          .then()
          .finally(() => {
            loadingEcho.value = isFirstLoadEcho;
          });
      }
      //기본적으로 선택되는 설정 목록
      checkedKeys['value'] = selectValues['value'];
    },
    { immediate: true }
  );

  async function onSelectChange(selectedRowKeys: (string | number)[], selectRow) {
    checkedKeys.value = selectedRowKeys;
    //모두 선택 문제를 판단하려면 selectedKeys와 selectRows가 일관성을 유지해야 합니다.
    if (props.showSelected && unref(checkedKeys).length !== unref(selectRow).length) {
      let { records } = await getList({
        code: unref(checkedKeys).join(','),
        pageSize: unref(checkedKeys).length,
      });
      selectRows.value = records;
    } else {
      selectRows.value = selectRow;
    }
  }

  /**
   * 컬럼 구성 선택
   */
  const rowSelection = {
    type: props.isRadioSelection ? 'radio' : 'checkbox',
    columnWidth: 20,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
    //표 4.4.0에서는 선택 후 이전 페이지와 다음 페이지의 데이터를 지울지 여부를 새로운 속성에 추가합니다. 기본값은 false입니다.
    preserveSelectedRowKeys:true,
  };

  /**
   * 일련번호 열 구성
   */
  const indexColumnProps = {
    dataIndex: 'index',
    width: 50,
  };

  /**
   * 로드 목록 데이터세트
   * @param params
   * @param 플래그는 기본 에코 모드로 로드됩니다.
   */
  async function getDataSource(params, flag) {
    let { records } = await getList(params);
    dataSource.value = records;
    if (flag) {
      let options = <any[]>[];
      records.forEach((item) => {
        options.push({ label: item[props.labelKey], value: item[props.rowKey] });
      });
      selectOptions.value = options;
    }
  }
  async function initSelectRows() {
    let { records } = await getList({
      code: selectValues['value'].join(','),
      pageSize: selectValues['value'].length,
    });
    checkedKeys['value'] = selectValues['value'];
    selectRows['value'] = records;
  }

  /**
   * 팝업 상자에 숨겨진 트리거 이벤트가 표시됩니다.
   */
  async function visibleChange(visible) {
    if (visible) {
      //기본적으로 선택되는 설정 목록
      props.showSelected && initSelectRows();
    } else {
      emit('close');
    }
  }

  /**
   * 선택 확인
   */
  function getSelectResult(success) {
    let options = <any[]>[];
    let values = <any[]>[];
    selectRows.value.forEach((item) => {
      options.push({ label: item[props.labelKey], value: item[props.rowKey] });
    });
    checkedKeys.value.forEach((item) => {
      values.push(item);
    });
    selectOptions.value = options;
    if (props.maxSelectCount && values.length > props.maxSelectCount) {
      $message.createMessage.warning(`최대 선택할 수 있습니다.${props.maxSelectCount}데이터`);
      return false;
    }
    success && success(options, values);
  }
  //선택한 정보 삭제
  function handleDeleteSelected(record) {
    checkedKeys.value = checkedKeys.value.filter((item) => item != record[props.rowKey]);
    selectRows.value = selectRows.value.filter((item) => item[props.rowKey] !== record[props.rowKey]);
  }
  //선택 항목 지우기
  function reset() {
    checkedKeys.value = [];
    selectRows.value = [];
  }
  return [
    {
      onSelectChange,
      getDataSource,
      visibleChange,
      selectOptions,
      selectValues,
      rowSelection,
      indexColumnProps,
      checkedKeys,
      selectRows,
      dataSource,
      getSelectResult,
      handleDeleteSelected,
      reset,
    },
  ];
}
