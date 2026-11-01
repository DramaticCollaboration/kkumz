import { useDefaultEnhanced } from '../hooks/useJVxeComponent';
import { isFunction, isObject, isString } from '/@/utils/is';
import { JVxeTypes } from '../types';
import { JVxeComponent } from '../types/JVxeComponent';
import { componentMap } from '../componentMap';

// 등록된 컴포넌트 개선 사항
const enhancedMap = new Map<JVxeTypes, JVxeComponent.Enhanced>();

/**
 * 구성요소 개선 사항 받기
 * @param type JVxeTypes
 */
export function getEnhanced(type: JVxeTypes | string): JVxeComponent.Enhanced {
  let $type: JVxeTypes = <JVxeTypes>type;
  if (!enhancedMap.has($type)) {
    let defaultEnhanced = useDefaultEnhanced();
    if (componentMap.has($type)) {
      let enhanced = componentMap.get($type)?.enhanced ?? {};
      if (isObject(enhanced)) {
        Object.keys(defaultEnhanced).forEach((key) => {
          let def = defaultEnhanced[key];
          if (enhanced.hasOwnProperty(key)) {
            // 메소드가 존재하는 경우 덮어쓰지 않습니다.
            if (!isFunction(def) && !isString(def)) {
              enhanced[key] = Object.assign({}, def, enhanced[key]);
            }
          } else {
            enhanced[key] = def;
          }
        });
        enhancedMap.set($type, <JVxeComponent.Enhanced>enhanced);
        return <JVxeComponent.Enhanced>enhanced;
      }
    } else {
      throw new Error(`[JVxeTable] ${$type} 컴포넌트가 등록되지 않았으며 향상된 기능을 얻지 못했습니다.`);
    }
    enhancedMap.set($type, <JVxeComponent.Enhanced>defaultEnhanced);
  }
  return <JVxeComponent.Enhanced>enhancedMap.get($type);
}

/** Helper Method：바꾸기${...}변하기 쉬운 */
export function replaceProps(col, value) {
  if (value && typeof value === 'string') {
    let text = value;
    text = text.replace(/\${title}/g, col.title);
    text = text.replace(/\${key}/g, col.key);
    text = text.replace(/\${defaultValue}/g, col.defaultValue);
    return text;
  }
  return value;
}

