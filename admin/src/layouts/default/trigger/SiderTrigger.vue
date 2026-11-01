<template>
  <div
    class="sider-trigger-wrap"
    role="button"
    tabindex="0"
    @click.stop="handleToggle"
    @keydown.enter.stop="handleToggle"
  >
    <DoubleRightOutlined v-if="getCollapsed" />
    <DoubleLeftOutlined v-else />
  </div>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import { DoubleRightOutlined, DoubleLeftOutlined } from '@ant-design/icons-vue';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';

  export default defineComponent({
    name: 'SiderTrigger',
    components: { DoubleRightOutlined, DoubleLeftOutlined },
    setup() {
      const { getCollapsed, toggleCollapsed } = useMenuSetting();

      function handleToggle() {
        toggleCollapsed();
      }

      return {
        getCollapsed,
        toggleCollapsed,
        handleToggle,
      };
    },
  });
</script>

<style lang="less" scoped>
  .sider-trigger-wrap {
    cursor: pointer;
    user-select: none;
  }
</style>
