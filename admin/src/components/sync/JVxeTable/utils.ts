import type { Ref, ComponentInternalInstance } from 'vue';
import { unref, isRef } from 'vue';
import { isFunction } from '/@/utils/is';

type dispatchEventOptions = {
  // JVxeTable의 소품
  props;
  // 트리거된 이벤트 이벤트 객체
  $event;
  // 행, 열
  row?;
  column?;
  // JVxeTable의 vue3 인스턴스
  instance?: ComponentInternalInstance | any;
  // 찾을 className
  className: string;
  // dom을 찾은 후 처리 방법을 다시 작성하십시오.
  handler?: Fn;
  // 클릭 이벤트를 시뮬레이션하는 대신 클릭 메소드를 직접 실행할지 여부
  isClick?: boolean;
};

/** 트리거 이벤트 시뮬레이션 */
export function dispatchEvent(options: dispatchEventOptions) {
  const { props, $event, row, column, instance, className, handler, isClick } = options;
  if ((!$event || !$event.path) && !instance) {
    return;
  }
  // 트리거 이벤트는 AlwaysEdit에서 시뮬레이션되지 않습니다. 그렇지 않으면 두 번 트리거됩니다.
  if (props && props.alwaysEdit) {
    return;
  }
  let getCell = () => {
    let paths: HTMLElement[] = [...($event?.path ?? [])];
    // 인스턴스를 통해 셀 돔 객체 가져오기
    if (row && column) {
      let selector = `table.vxe-table--body tbody tr[rowid='${row.id}'] td[colid='${column.id}']`;
      let cellDom = instance!.vnode?.el?.querySelector(selector);
      if (!cellDom) {
        cellDom = instance!.$el?.querySelector(selector);
      }
      if (cellDom) {
        paths.unshift(cellDom);
      }
    }
    for (const el of paths) {
      if (el.classList?.contains('vxe-body--column')) {
        return el;
      }
    }
    return null;
  };
  let cell = getCell();
  if (cell) {
    window.setTimeout(() => {
      let getElement = () => {
        let classList = className.split(' ');
        if (classList.length > 0) {
          const getClassName = (cls: string) => {
            if (cls.startsWith('.')) {
              return cls.substring(1, cls.length);
            }
            return cls;
          };
          let get = (target, className, idx = 0) => {
            let elements = target.getElementsByClassName(getClassName(className));
            if (elements && elements.length > 0) {
              return elements[idx];
            }
            return null;
          };
          let element: HTMLElement = get(cell, classList[0]);
          for (let i = 1; i < classList.length; i++) {
            if (!element) {
              break;
            }
            element = get(element, classList[i]);
          }
          return element;
        }
        return null;
      };
      let element = getElement();
      if (element) {
        if (isFunction(handler)) {
          handler(element);
        } else {
          // 클릭 이벤트 트리거 시뮬레이션
          if (isClick) {
            element.click();
          } else {
            element.dispatchEvent($event);
          }
        }
      }
    }, 10);
  } else {
    console.warn('【JVxeTable】DispatchEvent가 셀을 가져오지 못했습니다.');
  }
}

/** VxeTable 데이터 바인딩 */
export function vModel(value, row, column: Ref<any> | string) {
  // @ts-ignore
  let property = isRef(column) ? column.value.property : column;
  unref(row)[property] = value;
}
