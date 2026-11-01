import { computed, nextTick, ref, unref, watch } from 'vue';
import { propTypes } from '/@/utils/propTypes';
import { useDesign } from '/@/hooks/web/useDesign';
import { getEnhanced, replaceProps } from '../utils/enhancedUtils';
import { vModel } from '/@/components/sync/JVxeTable/utils';
import { JVxeRenderType } from '../types/JVxeTypes';
import { isBoolean, isFunction, isObject, isPromise } from '/@/utils/is';
import { JVxeComponent } from '../types/JVxeComponent';
import { filterDictText } from '/@/utils/dict/JDictSelectUtil';

export function useJVxeCompProps() {
  return {
    // 컴포넌트 유형
    type: propTypes.string,
    // 렌더링 유형
    renderType: propTypes.string.def('default'),
    // 렌더링 매개변수
    params: propTypes.object,
    // 렌더링 사용자 정의 옵션
    renderOptions: propTypes.object,
  };
}

export function useJVxeComponent(props: JVxeComponent.Props) {
  const value = computed(() => {
    const val = props.params.row[props.params.column.property];
    if (props.type === 'date' && typeof val === 'string') {
      return val.split(' ').shift();
    } else {
      return val;
    }
  });
  const innerValue = ref(value.value);
  const row = computed(() => props.params.row);
  const rows = computed(() => props.params.data);
  const column = computed(() => props.params.column);
  // 사용자가 구성한 원래 열
  const originColumn = computed(() => column.value.params);
  const rowIndex = computed(() => props.params._rowIndex);
  const columnIndex = computed(() => props.params._columnIndex);
  // 테이블 데이터 길이
  const fullDataLength = computed(() => props.params.$table.internalData.tableFullData.length);
  // 스크롤 되나요?
  const scrolling = computed(() => !!props.renderOptions.scrolling);
  const cellProps = computed(() => {
    let renderOptions = props.renderOptions;
    let col = originColumn.value;

    let cellProps = {};

    // 입력 자리 표시자
    cellProps['placeholder'] = replaceProps(col, col.placeholder);

    // 구문 분석 소품
    if (isObject(col.props)) {
      Object.keys(col.props).forEach((key) => {
        cellProps[key] = replaceProps(col, col.props[key]);
      });
    }

    // 비활성화된 열인지 확인
    cellProps['disabled'] = isBoolean(col['disabled']) ? col['disabled'] : cellProps['disabled'];
    // 행이 비활성화되었는지 확인
    if (renderOptions.isDisabledRow(row.value, rowIndex.value)) {
      cellProps['disabled'] = true;
    }
    if (col.props && col.props.isDisabledCell) {
      if (col.props.isDisabledCell({ row: row.value, rowIndex: rowIndex.value, column: col, columnIndex: columnIndex.value })) {
        cellProps['disabled'] = true;
      }
    }
    // 모든 컴포넌트를 비활성화할지 여부 결정
    if (renderOptions.disabled === true) {
      cellProps['disabled'] = true;
      cellProps['disabledTable'] = true;
    }
    if (col.checkStrictly === true) {
      cellProps['checkStrictly'] = true;
    }

    if (col.isRadioSelection === true) {
      cellProps['isRadioSelection'] = true;
    } else if (col.isRadioSelection === false) {
      cellProps['isRadioSelection'] = false;
    }

    return cellProps;
  });

  const listeners = computed(() => {
    let listeners = Object.assign({}, props.renderOptions.listeners || {});
    // 기본 변경 이벤트
    if (!listeners.change) {
      listeners.change = async (event) => {
        vModel(event.value, row, column);
        await nextTick();
        // 변경 이벤트 관련 로직 처리（예를 들어 다음을 확인하세요.）
        props.params.$table.updateStatus(props.params);
      };
    }
    return listeners;
  });
  const context = {
    innerValue,
    row,
    rows,
    rowIndex,
    column,
    columnIndex,
    originColumn,
    fullDataLength,
    cellProps,
    scrolling,
    handleChangeCommon,
    handleBlurCommon,
  };
  const ctx = { props, context };

  // 컴포넌트 개선 사항 받기
  const enhanced = getEnhanced(props.type);

  watch(
    value,
    (newValue) => {
      // 값 형식 확인
      let getValue = enhanced.getValue(newValue, ctx);
      if (newValue !== getValue) {
        // 값 형식이 잘못되었습니다. 값을 다시 할당하세요.
        newValue = getValue;
        vModel(newValue, row, column);
      }
      innerValue.value = enhanced.setValue(newValue, ctx);
      if (props.type === 'date' && props.renderType === JVxeRenderType.spaner && enhanced.translate.enabled === true) {
        if (isFunction(enhanced.translate.handler)) {
          innerValue.value = enhanced.translate.handler(newValue, ctx);
        }
        return;
      }
      // 번역 활성화 여부 결정
      if (props.renderType === JVxeRenderType.spaner && enhanced.translate.enabled === true) {
        if (isFunction(enhanced.translate.handler)) {
          let res = enhanced.translate.handler(newValue, ctx);
          // 비동기 번역은 느린 사전 쿼리 문제를 해결할 수 있습니다
          if (isPromise(res)) {
            res.then((v) => (innerValue.value = v));
          } else {
            innerValue.value = res;
          }
        }
      }
    },
    { immediate: true }
  );

  /** 변경 이벤트의 일반적인 처리 */
  function handleChangeCommon($value, force = false) {
    const newValue = enhanced.getValue($value, ctx);
    const oldValue = value.value;
    const execute = force ? true : newValue !== oldValue;
    if (execute) {
      trigger('change', { value: newValue });
      // 트리거 값변경 이벤트
      parentTrigger('valueChange', {
        type: props.type,
        value: newValue,
        oldValue: oldValue,
        col: originColumn.value,
        rowIndex: rowIndex.value,
        columnIndex: columnIndex.value,
      });
    }
  }

  /** 흐림 이벤트의 범용 처리 */
  function handleBlurCommon($value) {
    const newValue = enhanced.getValue($value, ctx);
    const oldValue = value.value;
    //trigger('blur', { value });
    // 흐림 이벤트 트리거
    parentTrigger('blur', {
      type: props.type,
      value: newValue,
      oldValue: oldValue,
      col: originColumn.value,
      rowIndex: rowIndex.value,
      columnIndex: columnIndex.value,
    });
  }

  /**
   * 이벤트가 존재하면 트리거
   * @param 이름 이벤트 이름
   * @param event 이벤트 매개변수
   * @param args 기타 부수적 매개변수
   */
  function trigger(name, event?, args: any[] = []) {
    let listener = listeners.value[name];
    if (isFunction(listener)) {
      if (isObject(event)) {
        event = packageEvent(name, event);
      }
      listener(event, ...args);
    }
  }

  function parentTrigger(name, event, args: any[] = []) {
    args.unshift(packageEvent(name, event));
    trigger('trigger', name, args);
  }

  function packageEvent(name, event: any = {}) {
    event.row = row.value;
    event.column = column.value;
    // 온라인으로 향상된 매개변수 호환성
    event.column['key'] = column.value['property'];
    // event.cellTarget = this
    if (!event.type) {
      event.type = name;
    }
    if (!event.cellType) {
      event.cellType = props.type;
    }
    // Form 확인 여부, 기본값은 true입니다.
    if (isBoolean(event.validate)) {
      event.validate = true;
    }
    return event;
  }

  /**
   * 스타일 충돌 방지 클래스 이름 생성기
   * @param scope
   */
  function useCellDesign(scope: string) {
    return useDesign(`vxe-cell-${scope}`);
  }

  return {
    ...context,
    enhanced,
    trigger,
    useCellDesign,
  };
}

/**
 * 컴포넌트 기본 개선 사항 가져오기
 */
export function useDefaultEnhanced(): JVxeComponent.EnhancedPartial {
  return {
    installOptions: {
      autofocus: '',
    },
    interceptor: {
      'event.clearActived': () => true,
      'event.clearActived.className': () => true,
    },
    switches: {
      editRender: true,
      visible: false,
    },
    aopEvents: {
      editActived() {},
      editClosed() {},
      activeMethod: () => true,
    },
    translate: {
      enabled: false,
      handler(value, ctx) {
        // 기본 번역 방법
        if (ctx) {
          return filterDictText(unref(ctx.context.column).params.options, value);
        } else {
          return value;
        }
      },
    },
    getValue: (value) => value,
    setValue: (value) => value,
    createValue: (defaultValue) => defaultValue,
  } as JVxeComponent.Enhanced;
}
