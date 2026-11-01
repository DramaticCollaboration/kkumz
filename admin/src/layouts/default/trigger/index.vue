<template>
  <SiderTrigger v-if="isSiderTrigger" class="layout-trigger-sider" />
  <HeaderTrigger v-else :theme="currentTheme" class="layout-trigger-header" />
</template>

<script lang="ts">
  import { defineComponent, computed } from 'vue';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { propTypes } from '/@/utils/propTypes';
  import HeaderTrigger from './HeaderTrigger.vue';

  export default defineComponent({
    name: 'LayoutTrigger',
    components: {
      SiderTrigger: createAsyncComponent(() => import('./SiderTrigger.vue')),
      HeaderTrigger: HeaderTrigger,
    },
    props: {
      sider: propTypes.bool.def(true),
      theme: propTypes.oneOf(['light', 'dark']),
    },
    setup(props) {
      const isSiderTrigger = computed(() => Boolean(props.sider));
      const currentTheme = computed(() => props.theme);

      return {
        isSiderTrigger,
        currentTheme,
      };
    },
  });
</script>

<style lang="less" scoped>
  .layout-trigger-sider,
  .layout-trigger-header {
    cursor: pointer;
  }
</style>
