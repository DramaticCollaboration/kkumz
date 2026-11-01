<template>
  <div :class="containerClasses" role="region" :aria-label="pageTitle">
    <MessageTemplate />
  </div>
</template>

<script lang="ts" setup name="system-message">
  import { computed } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import MessageTemplate from './template/index.vue';

  const props = defineProps({
    pageTitle: {
      type: String,
      default: '시스템 메시지 템플릿 관리',
    },
  });

  const { prefixCls } = useDesign('system-message-view');
  const containerClasses = computed(() => [prefixCls, 'system-message-page']);
  const pageTitle = computed(() => props.pageTitle);
</script>

<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-system-message-view';

  .@{prefix-cls} {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
  }
</style>

