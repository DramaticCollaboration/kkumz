import { CSSProperties, VNodeChild } from 'vue';
import { createTypes, VueTypeValidableDef, VueTypesInterface, toValidableType } from 'vue-types';

export type VueNode = VNodeChild | JSX.Element;

type PropTypes = VueTypesInterface & {
  readonly style: VueTypeValidableDef<CSSProperties>;
  readonly VNodeChild: VueTypeValidableDef<VueNode>;
  // readonly trueBool: VueTypeValidableDef<boolean>;
};
const newPropTypes = createTypes({
  func: undefined,
  bool: undefined,
  string: undefined,
  number: undefined,
  object: undefined,
  integer: undefined,
}) as PropTypes;

// vue-types v5.0부터 확장()이 방법은 폐기되었으며 공식적으로 권장되는 ES6+ 방법 https:으로 변경되었습니다.//dwightjack.github.io/vue-types/advanced/extending-vue-types.html#the-extend-method
class propTypes extends newPropTypes {
  // a native-like validator that supports the `.validable` method
  static get style() {
    return toValidableType('style', {
      type: [String, Object],
    });
  }

  static get VNodeChild() {
    return toValidableType('VNodeChild', {
      type: undefined,
    });
  }
}
export { propTypes };
