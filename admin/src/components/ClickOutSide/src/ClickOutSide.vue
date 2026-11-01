<template>
  <div ref="wrap" class="click-outside-wrap">
    <slot></slot>
  </div>
</template>

<script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  import { onClickOutside } from '@vueuse/core';

  const props = defineProps({
    disabled: {
      type: Boolean,
      default: false,
    },
  });

  const emit = defineEmits(['mounted', 'clickOutside']);
  const wrap = ref<ElRef>(null);

  function handleOutsideEvent() {
    if (!props.disabled) {
      emit('clickOutside');
    }
  }

  onClickOutside(wrap, handleOutsideEvent);

  onMounted(() => {
    emit('mounted');
  });
</script>

<style lang="less" scoped>
  .click-outside-wrap {
    width: 100%;
    height: 100%;
  }
</style>
