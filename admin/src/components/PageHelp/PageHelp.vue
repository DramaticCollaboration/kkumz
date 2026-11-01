<template>
  <div>
    <a-tooltip title="화면 도움말 및 운영 가이드" placement="bottom">
      <div
        class="header-help-btn cursor-pointer px-2 flex items-center h-full hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
        @click="handleOpenHelp"
      >
        <QuestionCircleOutlined class="text-lg" />
        <span v-if="showText" class="ml-1 text-sm font-medium">도움말</span>
      </div>
    </a-tooltip>

    <HelpGuideModal @register="registerModal" />
  </div>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router';
  import { QuestionCircleOutlined } from '@ant-design/icons-vue';
  import { useModal } from '/@/components/Modal';
  import HelpGuideModal from './HelpGuideModal.vue';

  defineProps({
    showText: {
      type: Boolean,
      default: false,
    },
  });

  const router = useRouter();
  const [registerModal, { openModal }] = useModal();

  const handleOpenHelp = () => {
    const currentRoute = router.currentRoute.value;
    const path = currentRoute.path;
    const name = (currentRoute.name as string) || '';
    const title = (currentRoute.meta?.title as string) || '';

    openModal(true, {
      path,
      currentPath: path,
      name,
      title,
    });
  };
</script>

<style scoped lang="less">
  .header-help-btn {
    display: flex;
    align-items: center;
    justify-content: center;
  }
</style>
