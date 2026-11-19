import type { BasicTableProps, TableRowSelection, BasicColumn } from '../types/table';
import type { Ref, ComputedRef } from 'vue';
import { computed, unref, ref, nextTick, watch, onBeforeUnmount } from 'vue';
import { isBoolean } from '/@/utils/is';
import { useWindowSizeFn } from '/@/hooks/event/useWindowSizeFn';
import { useModalContext } from '/@/components/Modal';
import { onMountedOrActivated } from '/@/hooks/core/onMountedOrActivated';
import { useDebounceFn } from '@vueuse/core';
import componentSetting from '/@/settings/componentSetting';

export function useTableScroll(
  propsRef: ComputedRef<BasicTableProps>,
  tableElRef: Ref<ComponentRef>,
  columnsRef: ComputedRef<BasicColumn[]>,
  rowSelectionRef: ComputedRef<TableRowSelection<any> | null>,
  getDataSourceRef: ComputedRef<Recordable[]>
) {
  const tableHeightRef: Ref<Nullable<number>> = ref(null);

  const modalFn = useModalContext();

  const debounceRedoHeight = useDebounceFn(redoHeight, 80);
  let followupTimer: ReturnType<typeof setTimeout> | null = null;
  let headResizeObserver: ResizeObserver | null = null;
  let observedHeadEl: HTMLElement | null = null;
  let lastHeadWidth = 0;

  const getCanResize = computed(() => {
    const { canResize, scroll } = unref(propsRef);
    return canResize && !(scroll || {}).y;
  });

  watch(
    () => [unref(getCanResize), unref(getDataSourceRef)?.length, unref(propsRef).pagination],
    () => {
      debounceRedoHeight();
      if (followupTimer) clearTimeout(followupTimer);
      followupTimer = setTimeout(() => {
        calcTableHeight();
      }, 160);
    },
    {
      flush: 'post',
    }
  );

  onBeforeUnmount(() => {
    if (followupTimer) {
      clearTimeout(followupTimer);
      followupTimer = null;
    }
    if (headResizeObserver) {
      headResizeObserver.disconnect();
      headResizeObserver = null;
      observedHeadEl = null;
    }
  });

  function ensureHeadObserver(headEl: HTMLElement) {
    if (typeof ResizeObserver === 'undefined' || observedHeadEl === headEl) return;
    if (headResizeObserver) {
      headResizeObserver.disconnect();
    }
    observedHeadEl = headEl;
    lastHeadWidth = Math.round(headEl.getBoundingClientRect().width);
    headResizeObserver = new ResizeObserver((entries) => {
      const entry = entries[0];
      if (!entry) return;
      const w = Math.round(entry.contentRect.width);
      if (w !== lastHeadWidth) {
        lastHeadWidth = w;
        if (w > 0) {
          debounceRedoHeight();
        }
      }
    });
    headResizeObserver.observe(headEl);
  }

  function redoHeight() {
    nextTick(() => {
      calcTableHeight();
    });
  }

  function setHeight(height: Nullable<number>) {
    if (tableHeightRef.value !== height) {
      tableHeightRef.value = height;
      modalFn?.redoModalHeight?.();
    }
  }

  async function calcTableHeight() {
    const { resizeHeightOffset, pagination, maxHeight } = unref(propsRef);
    const tableData = unref(getDataSourceRef);

    const table = unref(tableElRef);
    if (!table) return;

    const tableEl = table.$el as HTMLElement | null;
    if (!tableEl) return;

    const tbodyEl = tableEl.querySelector('.ant-table-tbody') as HTMLElement | null;
    if (!tbodyEl) return;

    if (tbodyEl.style.height && tbodyEl.style.height !== 'unset') {
      tbodyEl.style.height = 'unset';
    }

    const headEl = tableEl.querySelector('.ant-table-thead') as HTMLElement | null;
    if (headEl) {
      ensureHeadObserver(headEl);
    }

    const scrollBodyEl = (tableEl.querySelector('.ant-table-body') ||
      tableEl.querySelector('.ant-table-content') ||
      tbodyEl) as HTMLElement;

    const hasScrollBarX = scrollBodyEl.scrollWidth > scrollBodyEl.clientWidth + 1;
    if (hasScrollBarX) {
      tableEl.classList.contains('hide-scrollbar-x') && tableEl.classList.remove('hide-scrollbar-x');
    } else {
      !tableEl.classList.contains('hide-scrollbar-x') && tableEl.classList.add('hide-scrollbar-x');
    }

    if (!unref(getCanResize) || !tableData || tableData.length === 0) {
      setHeight(null);
      !tableEl.classList.contains('hide-scrollbar-y') && tableEl.classList.add('hide-scrollbar-y');
      return;
    }

    const isInModalOrDrawer = Boolean(
      tableEl.closest('.ant-modal-body, .ant-modal-content, .ant-drawer-body, .ant-drawer-content')
    );
    if (isInModalOrDrawer) {
      setHeight(null);
      !tableEl.classList.contains('hide-scrollbar-y') && tableEl.classList.add('hide-scrollbar-y');
      return;
    }

    await nextTick();

    if (!headEl) return;

    const headRect = headEl.getBoundingClientRect();
    if (headRect.width === 0 && headRect.height === 0) return;

    const layoutContentEl = tableEl.closest('.jeecg-layout-content, .ant-layout-content') as HTMLElement | null;
    const containerScrollTop =
      (layoutContentEl?.scrollTop || 0) +
      (document.body.scrollTop || document.documentElement.scrollTop || window.pageYOffset || 0);

    const headTopUnscrolled = headRect.top + containerScrollTop;
    const viewportHeight = document.documentElement.clientHeight;
    const bottomIncludeBody = viewportHeight - headTopUnscrolled;

    let ancestorBottomSpace = 0;
    let hasBottomSibling = false;
    let curr: HTMLElement | null = tableEl;
    while (curr && curr !== document.body && curr !== document.documentElement) {
      const cStyle = window.getComputedStyle(curr);
      ancestorBottomSpace +=
        (Number.parseFloat(cStyle.paddingBottom) || 0) +
        (Number.parseFloat(cStyle.marginBottom) || 0) +
        (Number.parseFloat(cStyle.borderBottomWidth) || 0);

      const currRect = curr.getBoundingClientRect();
      let sib = curr.nextElementSibling as HTMLElement | null;
      while (sib) {
        const sStyle = window.getComputedStyle(sib);
        const tag = sib.tagName.toLowerCase();
        if (
          sStyle.display !== 'none' &&
          sStyle.visibility !== 'hidden' &&
          sStyle.position !== 'fixed' &&
          sStyle.position !== 'absolute' &&
          sib.offsetHeight > 4 &&
          !sib.classList.contains('ant-drawer') &&
          !sib.classList.contains('ant-modal-root') &&
          tag !== 'script' &&
          tag !== 'style'
        ) {
          const sibRect = sib.getBoundingClientRect();
          if (sibRect.top >= currRect.bottom - 4) {
            hasBottomSibling = true;
            break;
          }
        }
        sib = sib.nextElementSibling as HTMLElement | null;
      }

      if (
        curr.classList.contains('jeecg-layout-content') ||
        curr.classList.contains('ant-layout-content')
      ) {
        break;
      }
      curr = curr.parentElement;
    }

    if (hasBottomSibling) {
      setHeight(null);
      !tableEl.classList.contains('hide-scrollbar-y') && tableEl.classList.add('hide-scrollbar-y');
      return;
    }

    let paginationHeight = 0;
    if (!isBoolean(pagination) || pagination) {
      const paginationEl = tableEl.querySelector('.ant-pagination') as HTMLElement | null;
      if (paginationEl && paginationEl.offsetHeight > 0) {
        const pgStyle = window.getComputedStyle(paginationEl);
        const pgMt = Number.parseFloat(pgStyle.marginTop) || 0;
        const pgMb = Number.parseFloat(pgStyle.marginBottom) || 0;
        paginationHeight = paginationEl.offsetHeight + pgMt + pgMb;
      } else {
        paginationHeight = 42;
      }
    }

    const footerEl = tableEl.querySelector('.ant-table-footer') as HTMLElement | null;
    const footerHeight = footerEl ? footerEl.offsetHeight : 0;

    const summaryEl = tableEl.querySelector('.ant-table-container > .ant-table-summary') as HTMLElement | null;
    const summaryHeight = summaryEl ? summaryEl.offsetHeight : 0;

    const layoutFooterEl = document.querySelector('.jeecg-layout-footer, .ant-layout-footer') as HTMLElement | null;
    const layoutFooterHeight = layoutFooterEl ? layoutFooterEl.offsetHeight : 0;

    const headerHeight = Math.ceil(headRect.height);
    const safetyBuffer = 8;

    let availableHeight = Math.floor(
      bottomIncludeBody -
        (resizeHeightOffset || 0) -
        headerHeight -
        summaryHeight -
        footerHeight -
        paginationHeight -
        ancestorBottomSpace -
        layoutFooterHeight -
        safetyBuffer
    );

    if (availableHeight < 200) {
      setHeight(null);
      !tableEl.classList.contains('hide-scrollbar-y') && tableEl.classList.add('hide-scrollbar-y');
      return;
    }

    if (maxHeight && availableHeight > maxHeight) {
      availableHeight = maxHeight;
    }

    const liveTbodyEl = tableEl.querySelector('.ant-table-tbody') as HTMLElement | null;
    const naturalBodyHeight = liveTbodyEl ? liveTbodyEl.offsetHeight : 0;
    const horizBarAllowance = hasScrollBarX ? 10 : 0;

    if (naturalBodyHeight > 0 && naturalBodyHeight + horizBarAllowance <= availableHeight) {
      setHeight(null);
      !tableEl.classList.contains('hide-scrollbar-y') && tableEl.classList.add('hide-scrollbar-y');
    } else {
      setHeight(availableHeight);
      tableEl.classList.contains('hide-scrollbar-y') && tableEl.classList.remove('hide-scrollbar-y');

      if (document.body.scrollTop > 0) document.body.scrollTop = 0;
      if (document.documentElement.scrollTop > 0) document.documentElement.scrollTop = 0;
      if (layoutContentEl && layoutContentEl.scrollTop > 0) layoutContentEl.scrollTop = 0;
    }
  }

  useWindowSizeFn(calcTableHeight, 200);
  onMountedOrActivated(() => {
    calcTableHeight();
    nextTick(() => {
      debounceRedoHeight();
    });
  });

  const getScrollX = computed(() => {
    let width = 0;
    const { maxColumnWidth } = unref(propsRef);
    const NORMAL_WIDTH = maxColumnWidth ?? 150;

    const columns = unref(columnsRef).filter((item) => !item.defaultHidden);
    columns.forEach((item) => {
      width += Number.parseInt(((item.width || (item as any).minWidth) as string)) || 0;
    });
    const unsetWidthColumns = columns.filter(
      (item) => !Reflect.has(item, 'width') && !Reflect.has(item, 'minWidth')
    );

    const len = unsetWidthColumns.length;
    if (len !== 0) {
      width += len * NORMAL_WIDTH;
    }

    const table = unref(tableElRef);
    const tableWidth = table?.$el?.offsetWidth ?? 0;
    return tableWidth > width ? '100%' : width;
  });

  const getScrollRef = computed(() => {
    const tableHeight = unref(tableHeightRef);
    const { canResize, scroll } = unref(propsRef);
    const { table } = componentSetting;
    return {
      x: unref(getScrollX),
      y: canResize && tableHeight ? tableHeight : undefined,
      scrollToFirstRowOnChange: table.scrollToFirstRowOnChange,
      ...scroll,
    };
  });

  return { getScrollRef, redoHeight };
}
