import { computed, nextTick, unref, watchEffect } from 'vue';
import { router } from '/@/router';
import { useRoute } from 'vue-router';
import { createLocalStorage } from '/@/utils/cache';
import { useTableContext } from './useTableContext';
import { useMessage } from '/@/hooks/web/useMessage';

/**
 * 목록 구성 캐시
 */
export function useColumnsCache(opt, setColumns, handleColumnFixed) {
  let isInit = false;
  const table = useTableContext();
  const $ls = createLocalStorage();
  const { createMessage: $message } = useMessage();
  const route = useRoute();
  // 구성 캐시 키 나열
  const cacheKey = computed(() => {
    const path = route.path;
    let key = path.replace(/[\/\\]/g, '_');
    let cacheKey = table.getBindValues.value.tableSetting?.cacheKey;
    if (cacheKey) {
      key += ':' + cacheKey;
    }
    return 'columnCache:' + key;
  });

  watchEffect(() => {
    const columns = table.getColumns();
    if (columns.length) {
      init();
    }
  });

  async function init() {
    if (isInit) {
      return;
    }
    isInit = true;
    let columnCache = $ls.get(cacheKey.value);
    if (columnCache && columnCache.checkedList) {
      const { checkedList, sortedList, sortableOrder, checkIndex } = columnCache;
      await nextTick();
      // 체크박스 정렬 캐시
      opt.sortableOrder.value = sortableOrder;
      // 체크박스 선택 캐시
      opt.state.checkedList = checkedList;
      // tableColumn 정렬 캐시
      opt.plainSortOptions.value.sort((prev, next) => {
        return sortedList.indexOf(prev.value) - sortedList.indexOf(next.value);
      });
      // 테이블 열 재정렬
      checkedList.sort((prev, next) => sortedList.indexOf(prev) - sortedList.indexOf(next));
      // 줄 번호 열 표시 여부
      if (checkIndex) {
        table.setProps({ showIndexColumn: true });
      }
      setColumns(checkedList);
      // 고정 열 설정
      setColumnFixed(columnCache);
    }
  }

  /** 고정 열 설정 */
  async function setColumnFixed(columnCache) {
    const { fixedColumns } = columnCache;
    const columns = opt.plainOptions.value;
    for (const column of columns) {
      let fixedCol = fixedColumns.find((fc) => fc.key === (column.key || column.dataIndex));
      if (fixedCol) {
        await nextTick();
        handleColumnFixed(column, fixedCol.fixed);
      }
    }
  }

  // 열 고정 상태 확인
  const fixedReg = /^(true|left|right)$/;

  /** 고정 열 가져오기 */
  function getFixedColumns() {
    let fixedColumns: any[] = [];
    const columns = opt.plainOptions.value;
    for (const column of columns) {
      if (fixedReg.test((column.fixed ?? '').toString())) {
        fixedColumns.push({
          key: column.key || column.dataIndex,
          fixed: column.fixed === true ? 'left' : column.fixed,
        });
      }
    }
    return fixedColumns;
  }

  /** 열 구성 저장 */
  function saveSetting() {
    const { checkedList } = opt.state;
    const sortedList = unref(opt.plainSortOptions).map((item) => item.value);
    $ls.set(cacheKey.value, {
      // 저장된 열
      checkedList,
      // 정렬된 열
      sortedList,
      // 줄 번호 열 표시 여부
      checkIndex: unref(opt.checkIndex),
      // 확인란원본정렬
      sortableOrder: unref(opt.sortableOrder),
      // 고정 컬럼
      fixedColumns: getFixedColumns(),
    });
    $message.success('성공적으로 저장되었습니다');
    // 저장 후 바로 닫기
    opt.popoverVisible.value = false;
  }

  /** 재설정（삭제）컬럼 구성 */
  async function resetSetting() {
    // 고정 열 재설정
    await resetFixedColumn();
    $ls.remove(cacheKey.value);
    $message.success('재설정 성공');
  }

  async function resetFixedColumn() {
    const columns = opt.plainOptions.value;
    for (const column of columns) {
      column.fixed;
      if (fixedReg.test((column.fixed ?? '').toString())) {
        await nextTick();
        handleColumnFixed(column, null);
      }
    }
  }

  return {
    saveSetting,
    resetSetting,
  };
}
