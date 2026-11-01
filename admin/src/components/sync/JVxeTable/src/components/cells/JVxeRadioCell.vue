<template>
  <a-radio-group :class="clazz" :value="innerValue" v-bind="cellProps" @change="(e) => handleChangeCommon(e.target.value)">
    <a-radio v-for="item of originColumn.options" :key="item.value" :value="item.value" @click="(e) => handleRadioClick(item, e)"
      >{{ item.text || item.label || item.title || item.value }}
    </a-radio>
  </a-radio-group>
</template>

<script lang="ts">
  import { computed, defineComponent } from 'vue';
  import { JVxeComponent } from '/@/components/sync/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/sync/JVxeTable/hooks';

  export default defineComponent({
    name: 'JVxeRadioCell',
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, originColumn, handleChangeCommon } = useJVxeComponent(props);
      const scrolling = computed(() => !!props.renderOptions.scrolling);
      const clazz = computed(() => {
        return {
          'j-vxe-radio': true,
          'no-animation': scrolling.value,
        };
      });

      function handleRadioClick(item) {
        if (originColumn.value.allowClear === true) {
          // 선택 취소
          if (item.value === innerValue.value) {
            handleChangeCommon(null);
          }
        }
      }

      return {
        clazz,
        innerValue,
        originColumn,
        cellProps,
        handleRadioClick,
        handleChangeCommon,
      };
    },
    // 【구성요소 강화】자세한 내용은 참고 사항을 참조하세요.：JVxeComponent.Enhanced
    enhanced: {
      switches: { visible: true },
    } as JVxeComponent.EnhancedPartial,
  });
</script>

<style lang="less">
  // 스크롤 중 동적 할당 문제를 방지하려면 애니메이션을 끄세요.
  .j-vxe-radio.no-animation {
    .ant-radio-inner,
    .ant-radio-inner::after {
      transition: none !important;
    }
  }
</style>
