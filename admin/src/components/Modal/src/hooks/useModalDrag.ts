import { Ref, unref, watchEffect } from 'vue';
import { useTimeoutFn } from '/@/hooks/core/useTimeout';

export interface UseModalDragMoveContext {
  draggable: Ref<boolean>;
  destroyOnClose: Ref<boolean | undefined> | undefined;
  visible: Ref<boolean>;
}

export function useModalDragMove(context: UseModalDragMoveContext) {
  const getStyle = (dom: any, attr: any) => {
    return getComputedStyle(dom)[attr];
  };
  const drag = (wrap: any) => {
    if (!wrap) return;
    wrap.setAttribute('data-drag', unref(context.draggable));
    const dialogHeaderEl = wrap.querySelector('.ant-modal-header');
    const dragDom = wrap.querySelector('.ant-modal');

    if (!dialogHeaderEl || !dragDom || !unref(context.draggable)) return;

    dialogHeaderEl.style.cursor = 'move';

    dialogHeaderEl.onmousedown = (e: any) => {
      if (!e) return;
      // 마우스를 누르면 현재 요소와 보이는 영역 사이의 거리가 계산됩니다.
      const disX = e.clientX;
      const disY = e.clientY;
      const screenWidth = document.body.clientWidth; // 신체 현재 폭
      const screenHeight = document.documentElement.clientHeight; // 가시 영역 높이(신체의 높이여야 하는데 상황에 따라 구할 수 없는 경우도 있습니다.)

      const dragDomWidth = dragDom.offsetWidth; // 대화상자 너비
      const dragDomheight = dragDom.offsetHeight; // 대화상자 높이

      const minDragDomLeft = dragDom.offsetLeft;

      const maxDragDomLeft = screenWidth - dragDom.offsetLeft - dragDomWidth;
      const minDragDomTop = dragDom.offsetTop;
      let maxDragDomTop = screenHeight - dragDom.offsetTop - dragDomheight;
      if(maxDragDomTop<0){
        maxDragDomTop = screenHeight - dragDom.offsetTop
      }
      // 얻은 값에는 px 정규 매칭 및 교체가 있습니다.
      const domLeft = getStyle(dragDom, 'left');
      const domTop = getStyle(dragDom, 'top');
      let styL = +domLeft;
      let styT = +domTop;

      // IE에서는 처음 얻은 값이 컴포넌트의 50%이므로 이동 후에는 그 값이 px에 할당됩니다.
      if (domLeft.includes('%')) {
        styL = +document.body.clientWidth * (+domLeft.replace(/%/g, '') / 100);
        styT = +document.body.clientHeight * (+domTop.replace(/%/g, '') / 100);
      } else {
        styL = +domLeft.replace(/px/g, '');
        styT = +domTop.replace(/px/g, '');
      }

      document.onmousemove = function (e) {
        // 이벤트 위임을 통해 이동한 거리를 계산합니다.
        let left = e.clientX - disX;
        let top = e.clientY - disY;

        // 경계처리
        if (-left > minDragDomLeft) {
          left = -minDragDomLeft;
        } else if (left > maxDragDomLeft) {
          left = maxDragDomLeft;
        }

        if (-top > minDragDomTop) {
          top = -minDragDomTop;
        } else if (top > maxDragDomTop) {
          top = maxDragDomTop;
        }

        // 현재 요소 이동
        dragDom.style.cssText += `;left:${left + styL}px;top:${top + styT}px;`;
      };

      document.onmouseup = () => {
        document.onmousemove = null;
        document.onmouseup = null;
      };
    };
  };

  const handleDrag = () => {
    const dragWraps = document.querySelectorAll('.ant-modal-wrap');
    for (const wrap of Array.from(dragWraps)) {
      if (!wrap) continue;
      const display = getStyle(wrap, 'display');
      const draggable = wrap.getAttribute('data-drag');
      if (display !== 'none') {
        // 드래그 위치
        if (draggable === null || unref(context.destroyOnClose)) {
          drag(wrap);
        }
      }
    }
  };

  watchEffect(() => {
    if (!unref(context.visible) || !unref(context.draggable)) {
      return;
    }
    useTimeoutFn(() => {
      handleDrag();
    }, 30);
  });
}
