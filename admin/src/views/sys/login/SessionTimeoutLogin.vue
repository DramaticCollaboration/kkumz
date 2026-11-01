<template>
  <transition>
    <div :class="prefixCls">
      <Login sessionTimeout />
    </div>
  </transition>
</template>
<script lang="ts" setup>
  import { onBeforeUnmount, onMounted, ref } from 'vue';
  import Login from './Login.vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useUserStore } from '/@/store/modules/user';
  import { usePermissionStore } from '/@/store/modules/permission';
  import { useAppStore } from '/@/store/modules/app';
  import { PermissionModeEnum } from '/@/enums/appEnum';

  const { prefixCls } = useDesign('st-login');
  const userStore = useUserStore();
  const permissionStore = usePermissionStore();
  const appStore = useAppStore();
  const userId = ref<Nullable<number | string>>(0);

  const isBackMode = () => {
    return appStore.getProjectConfig.permissionMode === PermissionModeEnum.BACK;
  };

  onMounted(() => {
    // 현재 사용자 ID를 기록하세요.
    userId.value = userStore.getUserInfo?.userId;
    console.log('Mounted', userStore.getUserInfo);
  });

  onBeforeUnmount(() => {
    if (userId.value && userId.value !== userStore.getUserInfo.userId) {
      // 사용자가 로그인하지 않은 경우 전체 페이지를 새로 고쳐 이전 사용자의 페이지 상태를 삭제합니다.
      document.location.reload();
    } else if (isBackMode() && permissionStore.getLastBuildMenuTime === 0) {
      // 백그라운드 권한 모드에서 메뉴가 성공적으로 로드되지 않으면 전체 페이지가 다시 로드됩니다.이는 일반적으로 전체 페이지를 새로 고치기 위해 F5 키를 눌러 세션이 만료된 후 모듈을 로드할 때 발생합니다.
      document.location.reload();
    }
  });
</script>
<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-st-login';

  .@{prefix-cls} {
    position: fixed;
    z-index: 9999999;
    width: 100%;
    height: 100%;
    background: @component-background;
  }
</style>
