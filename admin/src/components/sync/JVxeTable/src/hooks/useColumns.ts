import type { JVxeColumn, JVxeDataProps, JVxeTableProps } from '../types';
import { computed, nextTick } from 'vue';
import { isArray, isEmpty, isPromise } from '/@/utils/is';
import { cloneDeep } from 'lodash-es';
import { JVxeTypePrefix, JVxeTypes } from '../types/JVxeTypes';
import { initDictOptions } from '/@/utils/dict';
import { pushIfNotExist } from '/@/utils/common/compUtils';
import { getEnhanced } from '../utils/enhancedUtils';
import { isRegistered } from '../utils/registerUtils';
import { JVxeComponent } from '../types/JVxeComponent';
import { useValidateRules } from './useValidateRules';
import { JVxeTableMethods } from '../types';

// 핸들 메소드 매개변수
export interface HandleArgs {
  props: JVxeTableProps;
  slots: any;
  data: JVxeDataProps;
  methods: JVxeTableMethods;
  col?: JVxeColumn;
  columns: JVxeColumn[];
  renderOptions?: any;
  enhanced?: JVxeComponent.Enhanced;
}

export function useColumns(props: JVxeTableProps, data: JVxeDataProps, methods: JVxeTableMethods, slots) {
  data.vxeColumns = computed(() => {
    let columns: JVxeColumn[] = [];
    if (isArray(props.columns)) {
      // 핸들 메소드 매개변수
      const args: HandleArgs = { props, slots, data, methods, columns };
      let seqColumn, selectionColumn, expandColumn, dragSortColumn;
      props.columns.forEach((column: JVxeColumn) => {
        // 승인되지 않은 열 1 제외 = 2 표시/숨기기 = 에러
        let auth = methods.getColAuth(column.key);
        if (auth?.type == '1' && !auth.isAuth) {
          return;
        } else if (auth?.type == '2' && !auth.isAuth) {
          column.disabled = true;
        }
        // 유형은 비어 있으며 기본값은 일반입니다.
        if (column.type == null || isEmpty(column.type)) {
          column.type = JVxeTypes.normal;
        }
        let col: JVxeColumn = cloneDeep(column);
        // 숨겨진 열 처리
        if (col.type === JVxeTypes.hidden) {
          return handleInnerColumn(args, col, handleHiddenColumn);
        }
        // 구성요소가 등록되지 않고 자동으로 일반으로 설정됩니다.
        if (!isRegistered(col.type)) {
          col.type = JVxeTypes.normal;
        }
        args.enhanced = getEnhanced(col.type);
        args.col = col;
        args.renderOptions = {
          bordered: props.bordered,
          disabled: props.disabled,
          scrolling: data.scrolling,
          isDisabledRow: methods.isDisabledRow,
          listeners: {
            trigger: (name, event) => methods.trigger(name, event),
            valueChange: (event) => methods.trigger('valueChange', event),
            /** 행 재정렬 */
            rowResort: (event) => {
              methods.doSort(event.oldIndex, event.newIndex);
              methods.trigger('dragged', event);
            },
            /** 현재 행 아래에 행 삽입 */
            rowInsertDown: (rowIndex) => methods.insertRows({}, rowIndex + 1),
          },
        };
        if (col.type === JVxeTypes.rowNumber) {
          seqColumn = col;
          columns.push(col);
        } else if (col.type === JVxeTypes.rowRadio || col.type === JVxeTypes.rowCheckbox) {
          selectionColumn = col;
          columns.push(col);
        } else if (col.type === JVxeTypes.rowExpand) {
          expandColumn = col;
          columns.push(col);
        } else if (col.type === JVxeTypes.rowDragSort) {
          dragSortColumn = col;
          columns.push(col);
        } else {
          col.params = column;
          handlerCol(args);
        }
      });
      handleInnerColumn(args, seqColumn, handleSeqColumn);
      handleInnerColumn(args, selectionColumn, handleSelectionColumn);
      handleInnerColumn(args, expandColumn, handleExpandColumn);
      handleInnerColumn(args, dragSortColumn, handleDragSortColumn, true);
      customComponentAddStar(columns);
    }
    return columns;
  });
}

/**
 * 2024-05-30
 * liaozhiyang
 * 편집할 수 없는 컴포넌트는 필수이며 제목 전체에 *를 수동으로 표시해야 합니다.
 */
function customComponentAddStar(columns) {
  columns.forEach((column) => {
    const { params } = column;
    if (params) {
      const { validateRules, type } = params;
      if (
        validateRules?.length &&
        [
          JVxeTypes.checkbox,
          JVxeTypes.radio,
          JVxeTypes.upload,
          JVxeTypes.progress,
          JVxeTypes.departSelect,
          JVxeTypes.userSelect,
          JVxeTypes.image,
          JVxeTypes.file,
        ].includes(type)
      ) {
        if (validateRules.find((item) => item.required)) {
          column.title = ` * ${column.title}`;
        }
      }
    }
  });
}

/** 내장 열 처리 */
function handleInnerColumn(args: HandleArgs, col: JVxeColumn, handler: (args: HandleArgs) => void, assign?: boolean) {
  let renderOptions = col?.editRender || col?.cellRender;
  return handler({
    ...args,
    col: col,
    renderOptions: assign ? Object.assign({}, args.renderOptions, renderOptions) : renderOptions,
  });
}

/**
 * 숨겨진 열 처리
 */
function handleHiddenColumn({ col, columns }: HandleArgs) {
  col!.params = cloneDeep(col);
  delete col!.type;
  col!.field = col!.key;
  col!.visible = false;
  columns.push(col!);
}

/**
 * 프로세스 라인 번호 열
 */
function handleSeqColumn({ props, col, columns }: HandleArgs) {
  // 줄 번호 열이 활성화되어 있는지 확인
  if (props.rowNumber) {
    let column = {
      type: 'seq',
      title: '#',
      width: 60,
      // 【QQYUN-8405】
      fixed: props.rowNumberFixed,
      align: 'center',
    };
    if (props.rowNumberFixed === 'none') {
      delete column.fixed;
    }
    if (col) {
      Object.assign(col, column);
    } else {
      columns.unshift(column as any);
    }
  }
}

/**
 * 선택 가능한 열 처리
 */
function handleSelectionColumn({ props, data, col, columns }: HandleArgs) {
  // 선택 가능한 행이 활성화되어 있는지 확인
  if (props.rowSelection) {
    let width = 45;
    if (data.statistics.has && !props.rowExpand && !props.dragSort) {
      width = 60;
    }
    let column: any = {
      type: props.rowSelectionType,
      width: width,
      fixed: 'left',
      align: 'center',
    };
    if (props.rowSelectionFixed === 'none') {
      delete column.fixed;
    }
    if (col) {
      Object.assign(col, column);
    } else {
      columns.unshift(column as any);
    }
  }
}

/**
 * 확장 가능한 행 처리
 */
function handleExpandColumn({ props, data, col, columns }: HandleArgs) {
  // 행 확장 가능 여부
  if (props.rowExpand) {
    let width = 40;
    if (data.statistics.has && !props.dragSort) {
      width = 60;
    }
    let column = {
      type: 'expand',
      title: '',
      width: width,
      fixed: 'left',
      align: 'center',
      slots: { content: 'expandContent' },
    };
    if (col) {
      Object.assign(col, column);
    } else {
      columns.unshift(column as any);
    }
  }
}

/** 정렬 가능한 열 처리 */
function handleDragSortColumn({ props, data, col, columns, renderOptions }: HandleArgs) {
  // 드래그 가능한 정렬이 가능한가요?
  if (props.dragSort) {
    let width = 40;
    if (data.statistics.has) {
      width = 60;
    }
    let column: any = {
      title: '',
      width: width,
      fixed: 'left',
      align: 'center',
      params: {
        notAllowDrag: props.notAllowDrag,
        ...col?.params,
      },
    };
    if (props.dragSortFixed === 'none') {
      delete column.fixed;
    }
    let cellRender = {
      name: JVxeTypePrefix + JVxeTypes.rowDragSort,
      sortKey: props.sortKey,
    };
    if (renderOptions) {
      column.cellRender = Object.assign(renderOptions, cellRender);
    } else {
      column.cellRender = cellRender;
    }
    if (col) {
      Object.assign(col, column);
    } else {
      columns.unshift(column);
    }
  }
}

/** 맞춤 구성요소 열 처리 */
function handlerCol(args: HandleArgs) {
  const { props, col, columns, enhanced } = args;
  if (!col) return;
  let { type } = col;
  col.field = col.key;
  delete col.type;
  let renderName = 'cellRender';
  // 렌더링 옵션
  let $renderOptions: any = { name: JVxeTypePrefix + type };
  if (enhanced?.switches.editRender) {
    if (!(enhanced.switches.visible || props.alwaysEdit)) {
      renderName = 'editRender';
    }
    // $renderOptions.type = (enhanced.switches.visible || props.alwaysEdit) ? 'visible' : 'default'
  }
  col[renderName] = $renderOptions;
  !col.params.customOptions && handleDict(args);
  handleRules(args);
  handleStatistics(args);
  handleSlots(args);
  handleLinkage(args);
  handleReloadEffect(args);

  if (col.editRender) {
    Object.assign(col.editRender, args.renderOptions);
  }
  if (col.cellRender) {
    Object.assign(col.cellRender, args.renderOptions);
  }

  columns.push(col);
}

/**
 * 사전 처리
 */
async function handleDict({ col, methods }: HandleArgs) {
  if (col && col.params.dictCode) {
    /** 데이터 사전을 로드하고 옵션으로 병합 */
    try {
      // 쿼리 사전
      if (!isPromise(col.params.optionsPromise)) {
        col.params.optionsPromise = new Promise(async (resolve) => {
          let dictCodeString = col.params.dictCode;
          if (dictCodeString) {
            dictCodeString = encodeURI(dictCodeString);
          }
          const dictOptions: any = await initDictOptions(dictCodeString);
          let options = col.params.options ?? [];
          dictOptions.forEach((dict) => {
            // 중복 데이터 필터링
            if (options.findIndex((o) => o.value === dict.value) === -1) {
              options.push(dict);
            }
          });
          resolve(options);
        });
      }
      col.params.options = await col.params.optionsPromise;
      await nextTick();
      await methods.getXTable().updateData();
    } catch (e) {
      console.group(`[JVxeTable] 쿼리 사전 "${col.params.dictCode}" 예외가 발생합니다!`);
      console.warn(e);
      console.groupEnd();
    }
  }
}

/**
 * 확인 처리
 */
function handleRules(args: HandleArgs) {
  if (isArray(args.col?.validateRules)) {
    useValidateRules(args);
  }
}

/**
 * 통계 열 처리
 */
function handleStatistics({ col, data }: HandleArgs) {
  // 합계 = 합계, 평균 = 평균
  if (col && isArray(col.statistics)) {
    data.statistics.has = true;
    col.statistics.forEach((item) => {
      if (!isEmpty(item)) {
        let arr = data.statistics[(item as string).toLowerCase()];
        if (isArray(arr)) {
          pushIfNotExist(arr, col.key);
        }
      }
    });
  }
}

/**
 * 핸들 슬롯
 */
function handleSlots({ slots, col, renderOptions }: HandleArgs) {
  // 슬롯 부품의 특수 처리
  if (col && col.params.type === JVxeTypes.slot) {
    if (!isEmpty(col.slotName) && slots.hasOwnProperty(col.slotName)) {
      renderOptions.slot = slots[col.slotName];
    }
  }
}

/** 프로세스 연결 컬럼 */
function handleLinkage({ data, col, renderOptions, methods }: HandleArgs) {
  // 프로세스 연결 열. 연결 열은 선택한 컴포넌트에만 사용할 수 있습니다.
  if (col && col.params.type === JVxeTypes.select && data.innerLinkageConfig != null) {
    // 현재 열이 연결된 열인지 확인
    if (data.innerLinkageConfig.has(col.key)) {
      renderOptions.linkage = {
        config: data.innerLinkageConfig.get(col.key),
        getLinkageOptionsAsync: methods.getLinkageOptionsAsync,
        getLinkageOptionsSibling: methods.getLinkageOptionsSibling,
        handleLinkageSelectChange: methods.handleLinkageSelectChange,
      };
    }
  }
}

function handleReloadEffect({ props, data, renderOptions }: HandleArgs) {
  renderOptions.reloadEffect = {
    enabled: props.reloadEffect,
    getMap() {
      return data.reloadEffectRowKeysMap;
    },
    isEffect(rowId) {
      return data.reloadEffectRowKeysMap[rowId] === true;
    },
    removeEffect(rowId) {
      return (data.reloadEffectRowKeysMap[rowId] = false);
    },
  };
}
