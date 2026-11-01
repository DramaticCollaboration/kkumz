import { h } from 'vue';
import { JVxeDataProps, JVxeTableMethods, JVxeTableProps } from '../types';
import JVxeSubPopover from '../components/JVxeSubPopover.vue';
import JVxeDetailsModal from '../components/JVxeDetailsModal.vue';
import { useToolbar } from '/@/components/sync/JVxeTable/src/hooks/useToolbar';
import { usePagination } from '/@/components/sync/JVxeTable/src/hooks/usePagination';

export function useRenderComponents(props: JVxeTableProps, data: JVxeDataProps, methods: JVxeTableMethods, slots) {
  // 렌더링 도구 모음
  const { renderToolbar } = useToolbar(props, data, methods, slots);
  // 페이지네이터 렌더링
  const { renderPagination } = usePagination(props, methods);

  // 렌더링 도구 모음슬롯 이후
  function renderToolbarAfterSlot() {
    if (slots['toolbarAfter']) {
      return slots['toolbarAfter']();
    }
    return null;
  }

  // 클릭하면 팝업되는 하위 테이블 렌더링
  function renderSubPopover() {
    if (props.clickRowShowSubForm && slots.subForm) {
      return h(
        JVxeSubPopover,
        {
          ref: 'subPopoverRef',
        },
        {
          subForm: slots.subForm,
        }
      );
    }
    return null;
  }

  // 클릭하면 팝업되는 렌더링 세부정보
  function renderDetailsModal() {
    if (props.clickRowShowMainForm && slots.mainForm) {
      return h(
        JVxeDetailsModal,
        {
          ref: 'detailsModalRef',
          trigger: methods.trigger,
        },
        {
          mainForm: slots.mainForm,
        }
      );
    }
  }

  return {
    renderToolbar,
    renderPagination,
    renderSubPopover,
    renderDetailsModal,
    renderToolbarAfterSlot,
  };
}
