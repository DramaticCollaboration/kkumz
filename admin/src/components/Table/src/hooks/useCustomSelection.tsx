import type { BasicColumn } from '/@/components/Table';
import type { Ref, ComputedRef } from 'vue';
import type { BasicTableProps, PaginationProps, TableRowSelection } from '/@/components/Table';
import { computed, nextTick, onUnmounted, ref, toRaw, unref, watch, watchEffect } from 'vue';
import { omit, isEqual } from 'lodash-es';
import { throttle } from 'lodash-es';
import { Checkbox, Radio } from 'ant-design-vue';
import { isFunction } from '/@/utils/is';
import { findNodeAll } from '/@/utils/helper/treeHelper';
import { ROW_KEY } from '/@/components/Table/src/const';
import { onMountedOrActivated } from '/@/hooks/core/onMountedOrActivated';
import { useMessage } from '/@/hooks/web/useMessage';
import { ModalFunc } from 'ant-design-vue/lib/modal/Modal';

// 맞춤 선택 열~의key
export const CUS_SEL_COLUMN_KEY = 'j-custom-selected-column';

/**
 * 맞춤 선택 열
 */
export function useCustomSelection(
  propsRef: ComputedRef<BasicTableProps>,
  emit: EmitType,
  wrapRef: Ref<null | HTMLDivElement>,
  getPaginationRef: ComputedRef<boolean | PaginationProps>,
  tableData: Ref<Recordable[]>,
  childrenColumnName: ComputedRef<string>
) {
  const { createConfirm } = useMessage();
  const bodyEl = ref<HTMLDivElement>();
  const bodyHeight = ref<number>(0);
  // 시트trHigh
  const rowHeight = ref<number>(0);
  const scrollTop = ref(0);
  // 선택~의key
  const selectedKeys = ref<string[]>([]);
  // 선택한 행
  const selectedRows = ref<Recordable[]>([]);
  let changeRows: Recordable[] = [];
  let allSelected: boolean = false;

  const flattedData = computed(() => {
    const data = flattenData(tableData.value, childrenColumnName.value);
    const rowSelection = propsRef.value.rowSelection;
    if (rowSelection?.type === 'checkbox' && rowSelection.getCheckboxProps) {
      for (let i = 0, len = data.length; i < len; i++) {
        const record = data[i];
        const result = rowSelection.getCheckboxProps(record);
        if (result.disabled) {
          data.splice(i, 1);
          i--;
          len--;
        }
      }
    }
    return data;
  });

  const getRowSelectionRef = computed((): TableRowSelection | null => {
    const { rowSelection } = unref(propsRef);
    if (!rowSelection) {
      return null;
    }

    return {
      preserveSelectedRowKeys: true,
      // selectedRowKeys: unref(selectedKeys),
      // onChange: (selectedRowKeys: string[]) => {
      //   setSelectedRowKeys(selectedRowKeys);
      // },
      ...omit(rowSelection, ['onChange', 'selectedRowKeys']),
    };
  });

  // ~이든예단일 선택
  const isRadio = computed(() => {
    return getRowSelectionRef.value?.type === 'radio';
  });

  const getAutoCreateKey = computed(() => {
    return unref(propsRef).autoCreateKey && !unref(propsRef).rowKey;
  });

  const getRowKey = computed(() => {
    const { rowKey } = unref(propsRef);
    return unref(getAutoCreateKey) ? ROW_KEY : rowKey;
  });
  // 얻다라인~의key필드데이터
  const getRecordKey = (record) => {
    if (!getRowKey.value) {
      return record[ROW_KEY];
    } else if (isFunction(getRowKey.value)) {
      return getRowKey.value(record);
    } else {
      return record[getRowKey.value];
    }
  };

  // 페이징 구성
  const getPagination = computed<PaginationProps>(() => {
    return typeof getPaginationRef.value === 'boolean' ? {} : getPaginationRef.value;
  });
  const currentPageSize = computed(() => {
    const { pageSize = 10, total = flattedData.value.length } = getPagination.value;
    return pageSize > total ? total : pageSize;
  });

  // 열 선택헤더props
  const selectHeaderProps = computed(() => {
    return {
      onSelectAll,
      isRadio: isRadio.value,
      selectedLength: flattedData.value.filter((data) => selectedKeys.value.includes(getRecordKey(data))).length,
      pageSize: tableData.value?.length ?? 0,
      // 【QQYUN-6774】체크박스가 비활성화된 후에도 체크박스가 계속 선택될 수 있는 문제 해결
      disabled: flattedData.value.length == 0,
      hideSelectAll: unref(propsRef)?.rowSelection?.hideSelectAll,
    };
  });

  watch(
    () => unref(propsRef)?.rowSelection?.selectedRowKeys,
    (val: string[]) => {
      const value = unref(val);
      if (Array.isArray(value) && !sameArray(value, selectedKeys.value)) {
        setSelectedRowKeys(value);
      }
    },
    {
      immediate: true,
      deep: true
    }
  );

  
  function sameArray(a, b) {
    if (a === b) {
      if (a.length === b.length) {
        return a.toString() === b.toString();
      } else {
        return false;
      }
    } else {
      if (isEqual(a, b)) {
        return true;
      }
      return false;
    }
  }

  watch([selectedKeys, selectedRows], () => {
    nextTick(() => {
      syncSelectedRows();
    });
  });

  const onScrollTopChange = throttle((e) => (scrollTop.value = e?.target?.scrollTop), 150);

  let bodyResizeObserver: Nullable<ResizeObserver> = null;
  watchEffect(() => {
    if (bodyEl.value) {
      bodyResizeObserver = new ResizeObserver((entries) => {
        for (let entry of entries) {
          if (entry.target === bodyEl.value && entry.contentRect) {
            const { height } = entry.contentRect;
            bodyHeight.value = Math.ceil(height);
          }
        }
      });
      bodyResizeObserver.observe(bodyEl.value);
      const el = bodyEl.value?.querySelector('tbody.ant-table-tbody tr.ant-table-row') as HTMLDivElement;
      if (el) {
        rowHeight.value = el.offsetHeight;
        return;
      }
    }
    rowHeight.value = 50;
    propsRef.value.size && void 0;
  });

  onMountedOrActivated(async () => {
    bodyEl.value = await getTableBody(wrapRef.value!);
    bodyEl.value.addEventListener('scroll', onScrollTopChange);
  });
  onUnmounted(() => {
    if (bodyEl.value) {
      bodyEl.value?.removeEventListener('scroll', onScrollTopChange);
    }
    if (bodyResizeObserver != null) {
      bodyResizeObserver.disconnect();
    }
  });

  // 모두 선택
  function onSelectAll(checked: boolean) {
    if (unref(propsRef)?.rowSelection?.onSelectAll) {
      allSelected = checked;
      changeRows = getInvertRows(selectedRows.value);
    }
    // 모두 선택 취소
    if (!checked) {
      // selectedKeys.value = [];
      // selectedRows.value = [];
      // emitChange('all');
      flattedData.value.forEach((item) => {
        updateSelected(item, false);
      });
      return;
    }
    let modal: Nullable<ReturnType<ModalFunc>> = null;
    // 모두 선택
    const checkAll = () => {
      if (modal != null) {
        modal.update({
          content: '전체 선택을 처리 중입니다. 잠시만 기다려 주세요.',
          cancelButtonProps: { disabled: true },
        });
      }
      let showCount = 0;
      let minSelect = 100;
      const hidden: Recordable[] = [];
      flattedData.value.forEach((item, index, array) => {
        if (array.length > 120) {
          if (showCount <= minSelect && recordIsShow(index, Math.max((minSelect - 10) / 2, 3))) {
            showCount++;
            updateSelected(item, checked);
          } else {
            hidden.push(item);
          }
        } else {
          updateSelected(item, checked);
        }
      });
      if (hidden.length > 0) {
        return batchesSelectAll(hidden, checked, minSelect);
      } else {
        emitChange('all');
      }
    };

    if (flattedData.value.length > 120) {
      modal = createConfirm({
        title: '모두 선택',
        content: '데이터 양이 많아 전체 선택 시 화면이 느려질 수 있습니다. 계속하시겠습니까?',
        iconType: 'warning',
        onOk: () => checkAll(),
      });
    } else {
      checkAll();
    }
  }

  function batchesSelectAll(hidden: Recordable[], checked: boolean, minSelect: number) {
    return new Promise<void>((resolve) => {
      (function call() {
        setTimeout(() => {
          const list = hidden.splice(0, minSelect);
          if (list.length > 0) {
            list.forEach((item) => {
              updateSelected(item, checked);
            });
            call();
          } else {
            setTimeout(() => {
              emitChange('all');
              setTimeout(() =>resolve(), 0);
            }, 500);
          }
        }, 300);
      })();
    });
  }

  function onSelect(record, checked) {
    updateSelected(record, checked);
    emitChange();
  }

  function updateSelected(record, checked) {
    const recordKey = getRecordKey(record);
    if (isRadio.value) {
      selectedKeys.value = [recordKey];
      selectedRows.value = [record];
      return;
    }
    const index = selectedKeys.value.findIndex((key) => key === recordKey);
    if (checked) {
      if (index === -1) {
        selectedKeys.value.push(recordKey);
        selectedRows.value.push(record);
      }
    } else {
      if (index !== -1) {
        selectedKeys.value.splice(index, 1);
        selectedRows.value.splice(index, 1);
      }
    }
  }

  // 코드사용자사용자 정의~의onChange이벤트
  function emitChange(mode = 'single') {
    const { rowSelection } = unref(propsRef);
    if (rowSelection) {
      const { onChange } = rowSelection;
      if (onChange && isFunction(onChange)) {
        setTimeout(() => {
          onChange(selectedKeys.value, selectedRows.value);
        }, 0);
      }
    }
    emit('selection-change', {
      keys: getSelectRowKeys(),
      rows: getSelectRows(),
    });
    if (mode == 'all') {
      const rowSelection = unref(propsRef)?.rowSelection;
      if (rowSelection?.onSelectAll) {
        rowSelection.onSelectAll(allSelected, toRaw(getSelectRows()), toRaw(changeRows));
      }
    }
  }

  function isCustomSelection(column: BasicColumn) {
    return column.key === CUS_SEL_COLUMN_KEY;
  }

  
  function recordIsShow(index: number, threshold = 3) {
    const isVirtual = flattedData.value.length > 50;
    if (isVirtual) {
      const flag1 = scrollTop.value - rowHeight.value * threshold < index * rowHeight.value;
      const flag2 = index * rowHeight.value < scrollTop.value + bodyHeight.value + rowHeight.value * threshold;
      return flag1 && flag2;
    }
    return true;
  }

  // 맞춤 렌더링Body
  function bodyCustomRender(params) {
    const { index } = params;
    if (propsRef.value.canResize && !recordIsShow(index)) {
      return '';
    }
    if (isRadio.value) {
      return renderRadioComponent(params);
    } else {
      return renderCheckboxComponent(params);
    }
  }

  /**
   * 표현checkbox컴포넌트
   */
  function renderCheckboxComponent({ record }) {
    const recordKey = getRecordKey(record);
    // 얻다사용자사용자 정의checkboxProps
    const checkboxProps = ((getCheckboxProps) => {
      if (typeof getCheckboxProps === 'function') {
        try {
          return getCheckboxProps(record) ?? {};
        } catch (error) {
          console.error(error);
        }
      }
      return {};
    })(propsRef.value.rowSelection?.getCheckboxProps);
    return (
      <Checkbox
        {...checkboxProps}
        key={'j-select__' + recordKey}
        checked={selectedKeys.value.includes(recordKey)}
        onUpdate:checked={(checked) => onSelect(record, checked)}
        onClick={(e) => e.stopPropagation()}
      />
    );
  }

  /**
   * 표현radio컴포넌트
   */
  function renderRadioComponent({ record }) {
    const recordKey = getRecordKey(record);
    // 얻다사용자사용자 정의radioProps
    const checkboxProps = (() => {
      const rowSelection = propsRef.value.rowSelection;
      if (rowSelection?.getCheckboxProps) {
        return rowSelection.getCheckboxProps(record);
      }
      return {};
    })();
    return (
      <Radio
        {...checkboxProps}
        key={'j-select__' + recordKey}
        checked={selectedKeys.value.includes(recordKey)}
        onUpdate:checked={(checked) => onSelect(record, checked)}
        onClick={(e) => e.stopPropagation()}
      />
    );
  }

  // 만들다열 선택
  function handleCustomSelectColumn(columns: BasicColumn[]) {
    const rowSelection = propsRef.value.rowSelection;
    if (!rowSelection) {
      return;
    }
    const isFixedLeft = rowSelection.fixed || columns.some((item) => item.fixed === 'left');
    columns.unshift({
      title: '열 선택',
      flag: 'CHECKBOX',
      key: CUS_SEL_COLUMN_KEY,
      width: 50,
      minWidth: 50,
      maxWidth: 50,
      align: 'center',
      ...(isFixedLeft ? { fixed: 'left' } : {}),
      customRender: bodyCustomRender,
    });
  }

  // 초기화모두선택
  function clearSelectedRowKeys() {
    onSelectAll(false);
  }

  // 통과하다 selectedKeys 동기화 selectedRows
  function syncSelectedRows() {
    if (selectedKeys.value.length !== selectedRows.value.length) {
      setSelectedRowKeys(selectedKeys.value);
    }
  }

  // 설정선택~의key
  function setSelectedRowKeys(rowKeys: string[]) {
    const isSomeRowKeys = selectedKeys.value === rowKeys;
    selectedKeys.value = rowKeys;
    const allSelectedRows = findNodeAll(
      toRaw(unref(flattedData)).concat(toRaw(unref(selectedRows))),
      (item) => rowKeys.includes(getRecordKey(item)),
      {
        children: propsRef.value.childrenColumnName ?? 'children',
      }
    );
    const trueSelectedRows: any[] = [];
    rowKeys.forEach((key: string) => {
      const found = allSelectedRows.find((item) => getRecordKey(item) === key);
      found && trueSelectedRows.push(found);
    });
    if (!(isSomeRowKeys && equal(selectedRows.value, trueSelectedRows))) {
      selectedRows.value = trueSelectedRows;
      emitChange();
    }
  }
  
  function equal(oldVal, newVal) {
    let oldKeys = [],
      newKeys = [];
    if (oldVal.length === newVal.length) {
      oldKeys = oldVal.map((item) => getRecordKey(item));
      newKeys = newVal.map((item) => getRecordKey(item));
      for (let i = 0, len = oldKeys.length; i < len; i++) {
        const findItem = newKeys.find((item) => item === oldKeys[i]);
        if (!findItem) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
  
  function getInvertRows(rows: any): any {
    const allRows = findNodeAll(toRaw(unref(flattedData)), () => true, {
      children: propsRef.value.childrenColumnName ?? 'children',
    });
    if (rows.length === 0 || rows.length === allRows.length) {
      return allRows;
    } else {
      const allRowsKey = allRows.map((item) => getRecordKey(item));
      rows.forEach((rItem) => {
        const rItemKey = getRecordKey(rItem);
        const findIndex = allRowsKey.findIndex((item) => rItemKey === item);
        if (findIndex != -1) {
          allRowsKey.splice(findIndex, 1);
          allRows.splice(findIndex, 1);
        }
      });
    }
    return allRows;
  }
  function getSelectRows<T = Recordable>() {
    return unref(selectedRows) as T[];
  }

  function getSelectRowKeys() {
    return unref(selectedKeys);
  }

  function getRowSelection() {
    return unref(getRowSelectionRef)!;
  }

  function deleteSelectRowByKey(key: string) {
    const index = selectedKeys.value.findIndex((item) => item === key);
    if (index !== -1) {
      selectedKeys.value.splice(index, 1);
      selectedRows.value.splice(index, 1);
    }
  }

  // 【QQYUN-5837】ExpandIconColumnIndex를 동적으로 계산
  const getExpandIconColumnIndex = computed(() => {
    const { expandIconColumnIndex } = unref(propsRef);
    if (getRowSelectionRef.value == null) {
      return expandIconColumnIndex;
    }
    if (expandIconColumnIndex == null) {
      return 1;
    }
    return expandIconColumnIndex;
  });

  return {
    getRowSelection,
    getRowSelectionRef,
    getSelectRows,
    getSelectRowKeys,
    setSelectedRowKeys,
    deleteSelectRowByKey,
    selectHeaderProps,
    isCustomSelection,
    handleCustomSelectColumn,
    clearSelectedRowKeys,
    getExpandIconColumnIndex,
  };
}

function getTableBody(wrap: HTMLDivElement) {
  return new Promise<HTMLDivElement>((resolve) => {
    (function fn() {
      const bodyEl = wrap.querySelector('.ant-table-wrapper .ant-table-body') as HTMLDivElement;
      if (bodyEl) {
        resolve(bodyEl);
      } else {
        setTimeout(fn, 100);
      }
    })();
  });
}

function flattenData<RecordType>(data: RecordType[] | undefined, childrenColumnName: string): RecordType[] {
  let list: RecordType[] = [];
  (data || []).forEach((record) => {
    list.push(record);

    if (record && typeof record === 'object' && childrenColumnName in record) {
      list = [...list, ...flattenData<RecordType>((record as any)[childrenColumnName], childrenColumnName)];
    }
  });

  return list;
}
