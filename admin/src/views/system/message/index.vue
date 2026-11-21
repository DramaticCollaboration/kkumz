<template>
  <div :class="containerClasses" role="region" :aria-label="pageTitle">
    <MessageTemplate />
  </div>
</template>

<script lang="ts" setup name="system-message">
  import { computed } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import MessageTemplate from './template/index.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { $t } from '/@/locales';
  const { t } = useI18n();

  const props = defineProps({
    pageTitle: {
      type: String,
      default: () => $t('appUi.kc1fd19d2'),
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

