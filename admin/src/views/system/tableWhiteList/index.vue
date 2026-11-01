<template>
  <div :class="containerClasses" role="region" :aria-label="pageTitle">
    <SysTableWhiteListList />
  </div>
</template>

<script lang="ts" setup name="system-table-whitelist">
  import { computed } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import SysTableWhiteListList from './SysTableWhiteListList.vue';

  const props = defineProps({
    pageTitle: {
      type: String,
      default: '테이블 화이트리스트 관리',
    },
  });

  const { prefixCls } = useDesign('system-table-whitelist-view');
  const containerClasses = computed(() => [prefixCls, 'system-table-whitelist-page']);
  const pageTitle = computed(() => props.pageTitle);
</script>

<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-system-table-whitelist-view';

  .@{prefix-cls} {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
  }
</style>

