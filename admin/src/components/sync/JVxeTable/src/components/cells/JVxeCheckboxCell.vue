<template>
  <div :class="boxClass" :style="boxStyle" title="">
    <a-checkbox :checked="innerValue" v-bind="cellProps" @change="handleChange" />
  </div>
</template>

<script lang="ts">
  import { computed, defineComponent } from 'vue';
  import { JVxeComponent } from '/@/components/sync/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/sync/JVxeTable/hooks';
  import { isArray, isBoolean } from '/@/utils/is';

  export default defineComponent({
    name: 'JVxeCheckboxCell',
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, originColumn, scrolling, handleChangeCommon } = useJVxeComponent(props);
      // 테두리 활성화 여부
      const bordered = computed(() => !!props.renderOptions.bordered);
      // 박스 클래스 이름
      const boxClass = computed(() => {
        return {
          'j-vxe-checkbox': true,
          'no-animation': scrolling.value,
        };
      });
      // 박스 인라인 스타일
      const boxStyle = computed(() => {
        const style = {};
        // 테두리가 있고 정렬 속성이 설정되지 않은 경우 강제로 중앙에 배치됩니다.
        if (bordered.value && !originColumn.value.align) {
          style['text-align'] = 'center';
        }
        return style;
      });

      // onChange 이벤트
      function handleChange(event) {
        handleChangeCommon(event.target.checked);
      }

      return {
        cellProps,
        innerValue,
        boxClass,
        boxStyle,
        handleChange,
      };
    },

    // 【구성요소 강화】자세한 내용은 참고 사항을 참조하세요.：JVxeComponent.Enhanced
    enhanced: {
      switches: {
        visible: true,
      },
      getValue(value, ctx) {
        let { context } = ctx!;
        let { originColumn } = context;
        // customValue 처리
        if (isArray(originColumn.value.customValue)) {
          let customValue = getCustomValue(originColumn.value);
          if (isBoolean(value)) {
            return value ? customValue[0] : customValue[1];
          } else {
            return value;
          }
        } else {
          return value;
        }
      },
      setValue(value, ctx) {
        let { context } = ctx!;
        let { originColumn } = context;
        // customValue가 설정되었는지 확인（맞춤 값）
        if (isArray(originColumn.value.customValue)) {
          let customValue = getCustomValue(originColumn.value);
          return neverNull(value).toString() === customValue[0].toString();
        } else {
          return !!value;
        }
      },
      createValue(_defaultValue, ctx) {
        let { context } = ctx!;
        let {
          column: { params: col },
        } = context;
        if (isArray(col.customValue)) {
          let customValue = getCustomValue(col);
          return col.defaultChecked ? customValue[0] : customValue[1];
        } else {
          return !!col.defaultChecked;
        }
      },
    } as JVxeComponent.EnhancedPartial,
  });

  function neverNull(value, def?) {
    return value == null ? neverNull(def, '') : value;
  }

  function getCustomValue(col) {
    let customTrue = neverNull(col.customValue[0], true);
    let customFalse = neverNull(col.customValue[1], false);
    return [customTrue, customFalse];
  }
</script>

<style lang="less">
  // 스크롤 중 동적 할당 문제를 방지하려면 애니메이션을 끄세요.
  .j-vxe-checkbox.no-animation {
    .ant-checkbox-inner,
    .ant-checkbox-inner::after {
      transition: none !important;
    }
  }
</style>
