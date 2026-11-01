import type { JVxeVueComponent } from './types';
import { JVxeTypes } from './types/JVxeTypes';

import JVxeSlotCell from './components/cells/JVxeSlotCell';
import JVxeNormalCell from './components/cells/JVxeNormalCell.vue';
import JVxeDragSortCell from './components/cells/JVxeDragSortCell.vue';

import JVxeInputCell from './components/cells/JVxeInputCell.vue';
import JVxeDateCell from './components/cells/JVxeDateCell.vue';
import JVxeTimeCell from './components/cells/JVxeTimeCell.vue';
import JVxeSelectCell from './components/cells/JVxeSelectCell.vue';
import JVxeRadioCell from './components/cells/JVxeRadioCell.vue';
import JVxeCheckboxCell from './components/cells/JVxeCheckboxCell.vue';
import JVxeUploadCell from './components/cells/JVxeUploadCell.vue';
// import { TagsInputCell, TagsSpanCell } from './components/cells/JVxeTagsCell.vue'
import JVxeProgressCell from './components/cells/JVxeProgressCell.vue';
import JVxeTextareaCell from './components/cells/JVxeTextareaCell.vue';
// import JVxeDepartSelectCell from './components/cells/JVxeDepartSelectCell.vue'
// import JVxeUserSelectCell from './components/cells/JVxeUserSelectCell.vue'

let componentMap = new Map<JVxeTypes | string, JVxeVueComponent>();
const JVxeComponents = 'JVxeComponents__';
if (import.meta.env.DEV && componentMap.size === 0 && window[JVxeComponents] && window[JVxeComponents].size > 0) {
  componentMap = window[JVxeComponents];
}
/** 스팬 컴포넌트 끝 */
export const spanEnds: string = ':span';

/** 등록에 사용할 수 없는 키워드를 정의하세요. */
export const excludeKeywords: Array<JVxeTypes> = [
  JVxeTypes.hidden,
  JVxeTypes.rowNumber,
  JVxeTypes.rowCheckbox,
  JVxeTypes.rowRadio,
  JVxeTypes.rowExpand,
];

/**
 * 컴포넌트
 *
 * @param 유형 컴포넌트 유형
 * @param 컴포넌트Vue 컴포넌트
 * @param spanComponent는 비어 있을 수 있는 컴포넌트를 표시합니다. 기본값은 JVxeNormalCell 컴포넌트입니다.
 */
export function addComponent(type: JVxeTypes, component: JVxeVueComponent, spanComponent?: JVxeVueComponent) {
  if (excludeKeywords.includes(type)) {
    throw new Error(`【addComponent】사용할 수 없습니다"${type}"컴포넌트의 이름으로, 이것이 키워드이기 때문입니다.`);
  }
  if (componentMap.has(type)) {
    throw new Error(`【addComponent】컴포넌트"${type}"이미 존재합니다`);
  }
  componentMap.set(type, component);
  if (spanComponent) {
    componentMap.set(type + spanEnds, spanComponent);
  }
  import.meta.env.DEV && (window[JVxeComponents] = componentMap);
}

export function deleteComponent(type: JVxeTypes) {
  componentMap.delete(type);
  componentMap.delete(type + spanEnds);
  import.meta.env.DEV && (window[JVxeComponents] = componentMap);
}

/** 내장된 사용자 정의 컴포넌트 정의 */
export function definedComponent() {
  addComponent(JVxeTypes.slot, JVxeSlotCell);
  addComponent(JVxeTypes.normal, JVxeNormalCell);
  addComponent(JVxeTypes.rowDragSort, JVxeDragSortCell);

  addComponent(JVxeTypes.input, JVxeInputCell);
  addComponent(JVxeTypes.inputNumber, JVxeInputCell);
  addComponent(JVxeTypes.radio, JVxeRadioCell);
  addComponent(JVxeTypes.checkbox, JVxeCheckboxCell);
  addComponent(JVxeTypes.select, JVxeSelectCell);
  addComponent(JVxeTypes.selectSearch, JVxeSelectCell); // 드롭다운 검색
  addComponent(JVxeTypes.selectMultiple, JVxeSelectCell); // 드롭다운 다중 선택
  addComponent(JVxeTypes.date, JVxeDateCell);
  addComponent(JVxeTypes.datetime, JVxeDateCell);
  addComponent(JVxeTypes.time, JVxeTimeCell);
  addComponent(JVxeTypes.upload, JVxeUploadCell);
  addComponent(JVxeTypes.textarea, JVxeTextareaCell);

  // addComponent(JVxeTypes.tags, TagsInputCell, TagsSpanCell)
  addComponent(JVxeTypes.progress, JVxeProgressCell);

  // addComponent(JVxeTypes.departSelect, JVxeDepartSelectCell)
  // addComponent(JVxeTypes.userSelect, JVxeUserSelectCell)
}

export { componentMap };
