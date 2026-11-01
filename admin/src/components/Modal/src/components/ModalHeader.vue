<template>
  <BasicTitle :helpMessage="helpMessage" class="basic-modal-header" @dblclick="handleDblClick">
    {{ title }}
  </BasicTitle>
</template>

<script lang="ts">
  import type { PropType } from 'vue';
  import { defineComponent } from 'vue';
  import { BasicTitle } from '/@/components/Basic';

  export default defineComponent({
    name: 'BasicModalHeader',
    components: { BasicTitle },
    props: {
      helpMessage: {
        type: [String, Array] as PropType<string | string[]>,
      },
      title: {
        type: String,
        default: '',
      },
    },
    emits: ['dblclick'],
    setup(_, { emit }) {
      function handleDblClick(e: MouseEvent) {
        emit('dblclick', e);
      }

      return {
        handleDblClick,
      };
    },
  });
</script>

<style lang="less" scoped>
  .basic-modal-header {
    user-select: none;
  }
</style>
