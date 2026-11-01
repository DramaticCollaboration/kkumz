<template>
  <div
    class="setting-button-trigger"
    role="button"
    tabindex="0"
    @click="handleOpenSetting"
    @keydown.enter="handleOpenSetting"
  >
    <Icon icon="ion:settings-outline" />
    <SettingDrawer @register="register" />
  </div>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import SettingDrawer from './SettingDrawer';
  import Icon from '/@/components/Icon';
  import { useDrawer } from '/@/components/Drawer';

  export default defineComponent({
    name: 'SettingButton',
    components: { SettingDrawer, Icon },
    setup() {
      const [register, { openDrawer }] = useDrawer();

      function handleOpenSetting() {
        openDrawer(true);
      }

      return {
        register,
        openDrawer,
        handleOpenSetting,
      };
    },
  });
</script>

<style lang="less" scoped>
  .setting-button-trigger {
    display: inline-flex;
    align-items: center;
    cursor: pointer;
  }
</style>
