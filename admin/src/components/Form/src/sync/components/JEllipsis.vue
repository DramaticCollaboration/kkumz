<template>
  <a-tooltip placement="topLeft">
    <template #title>
      <span>{{ rawText }}</span>
    </template>
    <span class="j-ellipsis-text">{{ showText }}</span>
  </a-tooltip>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { propTypes } from '/@/utils/propTypes';

  const props = defineProps({
    value: propTypes.oneOfType([propTypes.string, propTypes.number, propTypes.array]),
    length: propTypes.number.def(25),
  });

  const rawText = computed(() => {
    if (props.value === null || props.value === undefined) {
      return '';
    }
    return Array.isArray(props.value) ? props.value.join(', ') : String(props.value);
  });

  const showText = computed(() => {
    const text = rawText.value;
    if (!text) {
      return '';
    }
    return text.length > props.length ? `${text.slice(0, props.length)}...` : text;
  });
</script>

<style lang="less" scoped>
  .j-ellipsis-text {
    display: inline-block;
    vertical-align: bottom;
    word-break: break-all;
  }
</style>
