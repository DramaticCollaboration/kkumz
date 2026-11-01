<template>
  <JInputPop
    :value="innerValue"
    :width="300"
    :height="210"
    :pop-container="getPopupContainer"
    v-bind="cellProps"
    style="width: 100%"
    @blur="handleBlurCommon"
    @change="handleChangeCommon"
  />
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import JInputPop from '/@/components/Form/src/sync/components/JInputPop.vue';
  import { dispatchEvent } from '/@/components/sync/JVxeTable/utils';
  import { JVxeComponent } from '/@/components/sync/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/sync/JVxeTable/hooks';

  export default defineComponent({
    name: 'JVxeTextareaCell',
    components: { JInputPop },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, handleChangeCommon, handleBlurCommon } = useJVxeComponent(props);

      function getPopupContainer() {
        return document.body;
      }

      return { innerValue, cellProps, handleChangeCommon, handleBlurCommon, getPopupContainer };
    },
    // 【구성요소 강화】자세한 내용은 참고 사항을 참조하세요.：JVxeComponent.Enhanced
    enhanced: {
      installOptions: {
        autofocus: '.ant-input',
      },
      aopEvents: {
        editActived({ $event, row, column }) {
          // 기본적으로 오른쪽 팝업창을 열지 여부
          if (column.params.defaultOpen ?? false) {
            dispatchEvent({
              $event,
              row,
              column,
              props: this.props,
              instance: this,
              className: '.ant-input-suffix .app-iconify',
              isClick: true,
            });
          }
        },
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
