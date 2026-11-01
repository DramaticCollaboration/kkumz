<template>
  <BasicModal
    @register="registerModal"
    :title="title"
    :width="width"
    @ok="onOk"
    @cancel="onCancel"
  >
    <div class="easy-cron-modal-content">
      <EasyCron v-bind="attrs" />
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import EasyCron from './EasyCronInner.vue';

  export default defineComponent({
    name: 'EasyCronModal',
    inheritAttrs: false,
    components: { BasicModal, EasyCron },
    props: {
      title: propTypes.string.def('Cron'),
      width: propTypes.oneOfType([propTypes.string, propTypes.number]).def('800px'),
    },
    setup() {
      const attrs = useAttrs();
      const [registerModal, { closeModal }] = useModalInner();

      function onOk() {
        closeModal();
      }

      function onCancel() {
        closeModal();
      }

      return { attrs, registerModal, onOk, onCancel };
    },
  });
</script>

<style lang="less" scoped>
  .easy-cron-modal-content {
    padding: 4px 0;
  }
</style>
