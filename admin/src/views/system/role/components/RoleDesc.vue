<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    title="역할 세부정보"
    width="500px"
    destroyOnClose
  >
    <div class="role-desc-container">
      <Description :column="1" :data="roleData" :schema="formDescSchema" />
    </div>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { ref, useAttrs } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/src/components/Drawer';
  import { formDescSchema } from '../role.data';
  import { Description } from '/@/components/Description/index';

  defineEmits(['register']);
  const attrs = useAttrs();
  const roleData = ref<Record<string, any>>({});

  const [registerDrawer, { setDrawerProps }] = useDrawerInner(async (data) => {
    setDrawerProps({ confirmLoading: false });
    roleData.value = data?.record || {};
  });
</script>

<style lang="less" scoped>
  .role-desc-container {
    padding: 4px 0;
  }
</style>
