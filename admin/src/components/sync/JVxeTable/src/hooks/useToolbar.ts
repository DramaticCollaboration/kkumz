import { h } from 'vue';
import JVxeToolbar from '../components/JVxeToolbar.vue';
import { JVxeDataProps, JVxeTableMethods, JVxeTableProps } from '../types';

export function useToolbar(props: JVxeTableProps, data: JVxeDataProps, methods: JVxeTableMethods, $slots) {
  /** 렌더링 도구 모음 */
  function renderToolbar() {
    if (props.toolbar) {
      return h(
        JVxeToolbar,
        {
          size: props.size,
          disabled: props.disabled,
          toolbarConfig: props.toolbarConfig,
          disabledRows: props.disabledRows,
          hasBtnAuth: methods.hasBtnAuth,
          selectedRowIds: data.selectedRowIds.value,
          custom: props.custom,
          // 이벤트 추가
          onAdd: () => {
            setTimeout(() => {
              methods.addRows();
            }, 0);
          },
          // 이벤트 저장
          onSave: () => methods.trigger('save'),
          onRemove() {
            const $table = methods.getXTable();
            // 삭제 이벤트 트리거
            if (data.selectedRows.value.length > 0) {
              const deleteOldRows = methods.filterNewRows(data.selectedRows.value);
              const removeEvent: any = { deleteRows: data.selectedRows.value, $table };
              const insertRecords = $table.getInsertRecords();
              if (props.asyncRemove && deleteOldRows.length) {
                data.selectedRows.value.forEach((item) => {
                  // 새로 추가된 데이터 ID 삭제
                  if (insertRecords.includes(item)) {
                    delete item.id;
                  }
                });
                // 삭제를 확인하세요. 이 메서드를 호출해야만 삭제됩니다.
                removeEvent.confirmRemove = () => methods.removeSelection();
              } else {
                if (props.asyncRemove) {
                  // asyncRemove가 새로 추가된 데이터만 삭제하면 verifyRemove 호출이 오류를 보고하는 것을 방지합니다.
                  removeEvent.confirmRemove = () => {};
                }
                methods.removeSelection();
              }
              methods.trigger('removed', removeEvent);
            } else {
              methods.removeSelection();
            }
          },
          // 선택 이벤트 지우기
          onClearSelection: () => methods.clearSelection(),
          onRegister: ({ xToolbarRef }) => methods.getXTable().connect(xToolbarRef.value),
        },
        {
          toolbarPrefix: $slots.toolbarPrefix,
          toolbarSuffix: $slots.toolbarSuffix,
        }
      );
    }
    return null;
  }

  return { renderToolbar };
}
