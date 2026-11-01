<template>
  <div
    :id="formItemId"
    :class="['form-item-middleware', { 'has-form-id': hasFormItemId }]"
  >
    <slot></slot>
  </div>
</template>

<script lang="ts" setup>
  import { Form } from 'ant-design-vue';
  import { computed } from 'vue';

  const formItemContext = Form.useInjectFormItemContext();

  const formItemId = computed(() => {
    return formItemContext?.id?.value || undefined;
  });

  const hasFormItemId = computed(() => {
    return Boolean(formItemId.value && String(formItemId.value).trim().length > 0);
  });
</script>

<style lang="less" scoped>
  .form-item-middleware {
    flex: 1;
    width: 100%;
    min-width: 0;
    box-sizing: border-box;
    position: relative;
  }
</style>
