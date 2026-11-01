<template>
  <BasicDrawer
    @register="registerModal"
    title="상세"
    :width="600"
    v-bind="$attrs"
    @ok="handleClose"
  >
    <div class="message-manage-drawer-body">
      <BasicForm @register="registerForm" />
    </div>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { formSchemas } from './manage.data';

  defineEmits(['register']);

  const [registerForm, { resetFields, setFieldsValue }] = useForm({
    schemas: formSchemas,
    showActionButtonGroup: false,
  });

  const [registerModal, { closeDrawer }] = useDrawerInner(async (data) => {
    await resetFields();
    if (data?.record) {
      await setFieldsValue({ ...data.record });
    }
  });

  function handleClose() {
    closeDrawer();
  }
</script>

<style lang="less" scoped>
  .message-manage-drawer-body {
    padding: 4px 0;
  }
</style>
