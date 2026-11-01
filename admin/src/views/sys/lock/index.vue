<template>
  <transition name="fade-bottom" mode="out-in">
    <div
      v-if="getIsLock"
      class="sys-lock-page-container"
      role="dialog"
      aria-modal="true"
    >
      <LockPage />
    </div>
  </transition>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import LockPage from './LockPage.vue';
  import { useLockStore } from '/@/store/modules/lock';

  const lockStore = useLockStore();

  const lockInfo = computed(() => lockStore?.getLockInfo);
  const getIsLock = computed(() => Boolean(lockInfo.value?.isLock));
</script>

<style lang="less" scoped>
  .sys-lock-page-container {
    position: fixed;
    inset: 0;
    z-index: 9999;
    width: 100%;
    height: 100%;
    overflow: hidden;
  }
</style>

