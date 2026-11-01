<template>
  <div class="not-data-exception-page">
    <Exception :status="exceptionStatus" :title="title" :sub-title="subTitle" />
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { Exception } from '/@/views/sys/exception/index';
  import { ExceptionEnum } from '/@/enums/exceptionEnum';

  const props = defineProps({
    title: {
      type: String,
      default: undefined,
    },
    subTitle: {
      type: String,
      default: undefined,
    },
  });

  const status = ref(ExceptionEnum.PAGE_NOT_DATA);
  const exceptionStatus = computed(() => status.value);
</script>

<style lang="less" scoped>
  .not-data-exception-page {
    width: 100%;
    height: 100%;
  }
</style>
