<template>
  <span :class="['app-search-key-item', $attrs.class]" role="presentation" aria-hidden="true">
    <Icon v-if="hasIcon" :icon="iconName" />
  </span>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { Icon } from '/@/components/Icon';

  const props = defineProps({
    icon: {
      type: String,
      default: '',
    },
  });

  const iconName = computed(() => props.icon || '');
  const hasIcon = computed(() => Boolean(props.icon && props.icon.trim().length > 0));
</script>

<style lang="less" scoped>
  .app-search-key-item {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
    vertical-align: middle;
  }
</style>
