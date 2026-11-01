import { defineComponent, h, ref, watch } from 'vue';
import { randomString } from '/@/utils/common/compUtils';
import '../style/reload-effect.less';

// 데이터 효과 수정
export default defineComponent({
  props: {
    vNode: null,
    // 특수 효과 활성화 여부
    effect: Boolean,
  },
  emits: ['effectBegin', 'effectEnd'],
  setup(props, { emit }) {
    // vNode: null,
    const innerEffect = ref(props.effect);
    // 동시에 여러 특수 효과에 대처
    const effectIdx = ref(0);
    const effectList = ref<any[]>([]);

    watch(
      () => props.effect,
      () => (innerEffect.value = props.effect)
    );
    watch(
      () => props.vNode,
      (_vNode, old) => {
        if (props.effect && old != null) {
          let topLayer = renderSpan(old, 'top');
          effectList.value.push(topLayer);
        }
      },
      { deep: true, immediate: true }
    );

    // 조건부로 렌더링된 콘텐츠 범위
    function renderVNode() {
      if (props.vNode == null) {
        return null;
      }
      let bottom = renderSpan(props.vNode, 'bottom');
      // 특수 효과가 활성화되고 이전 데이터를 사용할 수 있는 경우 특수 효과의 최상위 레이어를 렌더링합니다.
      if (innerEffect.value && effectList.value.length > 0) {
        emit('effectBegin');
        // 1.4초 후에 특수 효과 끄기
        window.setTimeout(() => {
          let item = effectList.value[effectIdx.value];
          if (item && item.elm) {
            // 특수 효과가 끝난 후 요소를 직접 삭제하는 대신 먼저 표시를 없음으로 설정합니다.
            // 목적은 페이지가 다시 렌더링되어 애니메이션이 재설정되는 것을 방지하는 것입니다.
            item.elm.style.display = 'none';
          }
          // 모든 계층적 애니메이션이 종료되면 모든 요소를 ​​삭제합니다.
          if (++effectIdx.value === effectList.value.length) {
            innerEffect.value = false;
            effectIdx.value = 0;
            effectList.value = [];
            emit('effectEnd');
          }
        }, 1400);
        return [effectList.value, bottom];
      } else {
        return bottom;
      }
    }

    // 렌더링 콘텐츠 범위
    function renderSpan(vNode, layer) {
      let options = {
        key: layer + effectIdx.value + randomString(6),
        class: ['j-vxe-reload-effect-span', `layer-${layer}`],
        style: {},
        title: vNode,

      };
      if (layer === 'top') {
        // 최신 렌더링은 아래와 같습니다
        options.style['z-index'] = 9999 - effectIdx.value;
      }
      return h('span', options, [vNode]);
    }

    return () =>
      h(
        'div',
        {
          class: ['j-vxe-reload-effect-box'],
        },
        [renderVNode()]
      );
  },
});
