<template>
  <span
    :class="triggerClasses"
    role="button"
    tabindex="0"
    @click="toggleCollapsed"
    @keydown.enter="toggleCollapsed"
  >
    <MenuUnfoldOutlined v-if="getCollapsed" />
    <MenuFoldOutlined v-else />
  </span>
</template>

<script lang="ts">
  import { defineComponent, computed } from 'vue';
  import { MenuUnfoldOutlined, MenuFoldOutlined } from '@ant-design/icons-vue';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { propTypes } from '/@/utils/propTypes';

  export default defineComponent({
    name: 'HeaderTrigger',
    components: { MenuUnfoldOutlined, MenuFoldOutlined },
    props: {
      theme: propTypes.oneOf(['light', 'dark']),
    },
    setup(props) {
      const { getCollapsed, toggleCollapsed } = useMenuSetting();
      const { prefixCls } = useDesign('layout-header-trigger');

      const triggerClasses = computed(() => [prefixCls, props.theme]);

      return {
        getCollapsed,
        toggleCollapsed,
        prefixCls,
        triggerClasses,
      };
    },
  });
</script>

<style lang="less" scoped>
  .@{namespace}-layout-header-trigger {
    cursor: pointer;
    user-select: none;
  }
</style>
