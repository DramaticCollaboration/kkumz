<template>
  <Progress :class="clazz" :percent="innerValue" size="small" v-bind="cellProps" />
</template>

<script lang="ts">
  import { computed, defineComponent } from 'vue';
  import { Progress } from 'ant-design-vue';
  import { JVxeComponent } from '/@/components/sync/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/sync/JVxeTable/hooks';

  export default defineComponent({
    name: 'JVxeCheckboxCell',
    components: { Progress },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, scrolling } = useJVxeComponent(props);
      const clazz = computed(() => {
        return {
          'j-vxe-progress': true,
          'no-animation': scrolling.value,
        };
      });
      return { innerValue, cellProps, clazz };
    },
    // 【구성요소 강화】자세한 내용은 참고 사항을 참조하세요.：：JVxeComponent.Enhanced
    enhanced: {
      switches: {
        editRender: false,
      },
      setValue(value) {
        try {
          if (typeof value !== 'number') {
            return Number.parseFloat(value);
          } else {
            return value;
          }
        } catch {
          return 0;
        }
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>

<style scoped lang="less">
  // 스크롤 중 동적 할당 문제를 방지하기 위해 진행률 표시줄의 애니메이션을 끕니다.
  .j-vxe-progress.no-animation {
    :deep(.ant-progress-bg) {
      transition: none !important;
    }
  }
</style>
