import { onMounted, onUnmounted, nextTick } from 'vue';
import { JVxeTableMethods, JVxeTableProps } from '/@/components/sync/JVxeTable/src/types';
import Sortable from 'sortablejs';

export function useDragSort(props: JVxeTableProps, methods: JVxeTableMethods) {
  if (props.dragSort) {
    let sortable2: Sortable;
    let initTime: any;

    onMounted(() => {
      // 로딩이 완료된 후 드래그 이벤트 바인딩
      initTime = setTimeout(createSortable, 300);
    });

    onUnmounted(() => {
      clearTimeout(initTime);
      if (sortable2) {
        sortable2.destroy();
      }
    });

    function createSortable() {
      let xTable = methods.getXTable();
      // let dom = xTable.$el.querySelector('.vxe-table--fixed-wrapper .vxe-table--body tbody')
      let dom = xTable.$el.querySelector('.body--wrapper>.vxe-table--body tbody');
      let startChildren = [];
      sortable2 = Sortable.create(dom as HTMLElement, {
        handle: '.drag-btn',
        filter: '.not-allow-drag',
        draggable: ".allow-drag",
        direction: 'vertical',
        animation: 300,
        onStart(e) {
          let from = e.from;
          // @ts-ignore
          startChildren = [...from.children];
        },
        onEnd(e) {
          let oldIndex = e.oldIndex as number;
          let newIndex = e.newIndex as number;
          if (oldIndex === newIndex) {
            return;
          }
          // 【VUEN-2505】현재 행 데이터 가져오기
          let rowNode = xTable.getRowNode(e.item);
          if (!rowNode) {
            return;
          }
          let from = e.from;
          let element = startChildren[oldIndex];
          let target = null;
          if (oldIndex > newIndex) {
            // 위로 이동
            if (oldIndex + 1 < startChildren.length) {
              target = startChildren[oldIndex + 1];
            }
          } else {
            // 아래로 이동
            target = startChildren[oldIndex + 1];
          }
          from.removeChild(element);
          from.insertBefore(element, target);
          nextTick(() => {
            // 【VUEN-2505】가상 스크롤로 인한 오프셋 계산
            let diffIndex = rowNode!.index - oldIndex;
            if (diffIndex > 0) {
              oldIndex = oldIndex + diffIndex;
              newIndex = newIndex + diffIndex;
            }
            methods.doSort(oldIndex, newIndex);
            methods.trigger('dragged', { oldIndex, newIndex });
          });
        },
      });
    }
  }
}
