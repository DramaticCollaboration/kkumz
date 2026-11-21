<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="t('appUi.k8a38b786')"
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
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

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
