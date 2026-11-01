<template>
  <div :class="prefixCls" role="presentation" aria-hidden="true">
    <slot></slot>
  </div>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';

  export default defineComponent({
    name: 'FrameBlank',
    setup() {
      const { prefixCls } = useDesign('frame-blank');

      return {
        prefixCls,
      };
    },
  });
</script>

<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-frame-blank';

  .@{prefix-cls} {
    width: 100%;
    height: 100%;
    min-height: 1px;
  }
</style>
