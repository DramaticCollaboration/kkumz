import type { Component } from 'vue';
import { h } from 'vue';
import VXETable from 'vxe-table';
import { definedComponent, addComponent, componentMap, spanEnds, excludeKeywords } from '../componentMap';
import { JVxeRenderType, JVxeTypePrefix, JVxeTypes } from '../types/JVxeTypes';
import { getEnhanced } from './enhancedUtils';
import { isFunction } from '/@/utils/is';

/**
 * 컴포넌트가 등록되었는지 확인
 * @param type
 */
export function isRegistered(type: JVxeTypes | string) {
  if (excludeKeywords.includes(<JVxeTypes>type)) {
    return true;
  }
  return componentMap.has(type);
}

/**
 * vxe 사용자 정의 컴포넌트 등록
 *
 * @param type
 * @param 구성요소 편집 상태 표시 구성요소
 * @param spanComponent는 편집되지 않은 상태로 표시되는 컴포넌트이며 비어 있을 수 있습니다.
 */
export function registerComponent(type: JVxeTypes, component: Component, spanComponent?: Component) {
  addComponent(type, component, spanComponent);
  registerOneComponent(type);
}

/**
 * vxe 사용자 정의 컴포넌트를 비동기식으로 등록
 *
 * @param type
 * @param promise
 */
export async function registerAsyncComponent(type: JVxeTypes, promise: Promise<any>) {
  const result = await promise;
  if (isFunction(result.installJVxe)) {
    result.install((component: Component, spanComponent?: Component) => {
      addComponent(type, component, spanComponent);
      registerOneComponent(type);
    });
  } else {
    addComponent(type, result.default);
    registerOneComponent(type);
  }
}

/**
 * 2024-03-08
 * liaozhiyang
 * vxe 사용자 정의 컴포넌트를 비동기식으로 등록
 * 【QQYUN-8241】
 * @param type
 * @param promise
 */
export function registerASyncComponentReal(type: JVxeTypes, component) {
  addComponent(type, component);
  registerOneComponent(type);
}

/**
 * 모든 vxe 컴포넌트 설치
 */
export function registerAllComponent() {
  definedComponent();
  // 모든 컴포넌트 일괄 등록
  const components = [...componentMap.keys()];
  components.forEach((type) => {
    if (!type.endsWith(spanEnds)) {
      registerOneComponent(<JVxeTypes>type);
    }
  });
}

/**
 * 단일 vxe 컴포넌트 등록
 *
 * @param 유형 컴포넌트 유형
 */
export function registerOneComponent(type: JVxeTypes) {
  const component = componentMap.get(type);
  if (component) {
    const switches = getEnhanced(type).switches;
    if (switches.editRender && !switches.visible) {
      createEditRender(type, component);
    } else {
      createCellRender(type, component);
    }
  } else {
    throw new Error(`【registerOneComponent】"${type}"컴포넌트맵에 존재하지 않습니다`);
  }
}

/** 편집 가능한 컴포넌트 등록 */
function createEditRender(type: JVxeTypes, component: Component, spanComponent?: Component) {
  // 현재 컴포넌트About 개선 사항 얻기
  const enhanced = getEnhanced(type);
  if (!spanComponent) {
    if (componentMap.has(type + spanEnds)) {
      spanComponent = componentMap.get(type + spanEnds);
    } else {
      // 기본 스팬 컴포넌트는 정상입니다.
      spanComponent = componentMap.get(JVxeTypes.normal);
    }
  }
  // 렌더링 추가
  VXETable.renderer.add(JVxeTypePrefix + type, {
    // 편집 가능한 템플릿
    renderEdit: createRender(type, component, JVxeRenderType.editer),
    // 템플릿 표시
    renderCell: createRender(type, spanComponent, JVxeRenderType.spaner),
    // 향상된 등록
    ...enhanced.installOptions,
  });
}

/** 공통 컴포넌트 등록 */
function createCellRender(type: JVxeTypes, component: Component = <Component>componentMap.get(JVxeTypes.normal)) {
  // 현재 컴포넌트About 개선 사항 얻기
  const enhanced = getEnhanced(type);
  VXETable.renderer.add(JVxeTypePrefix + type, {
    // 기본 표시 템플릿
    renderDefault: createRender(type, component, JVxeRenderType.default),
    // 향상된 등록
    ...enhanced.installOptions,
  });
}

function createRender(type, component, renderType) {
  return function (renderOptions, params) {
    return [
      h(component, {
        type: type,
        params: params,
        renderOptions: renderOptions,
        renderType: renderType,
      }),
    ];
  };
}
