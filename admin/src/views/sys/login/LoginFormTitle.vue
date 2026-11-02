<template>
  <div class="mb-4 sm:mb-6 enter-x text-center xl:text-left">
    <div class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-blue-50 dark:bg-blue-950/60 text-blue-600 dark:text-blue-400 text-[11px] font-semibold mb-2">
      <span>KKUMZ BACKOFFICE</span>
      <span class="text-blue-300 dark:text-blue-700">·</span>
      <span>RBAC 통합 관제</span>
    </div>
    <h2 class="text-lg sm:text-2xl font-bold text-slate-800 dark:text-white xl:text-[26px] tracking-tight break-keep">
      Kkumz 통합 관제 포털 로그인
    </h2>
    <p v-if="isLoginState" class="mt-1.5 text-xs text-slate-500 dark:text-slate-400 break-keep leading-relaxed">
      AI 브랜드 스튜디오 · 멀티사이트 배포 · 커뮤니티 모더레이션을 관리하세요
    </p>
  </div>
</template>
<script lang="ts" setup>
  import { computed, unref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { LoginStateEnum, useLoginState } from './useLogin';

  const { t } = useI18n();

  const { getLoginState } = useLoginState();

  const isLoginState = computed(() => unref(getLoginState) === LoginStateEnum.LOGIN);

  const getFormTitle = computed(() => {
    const titleObj = {
      [LoginStateEnum.RESET_PASSWORD]: t('sys.login.forgetFormTitle'),
      [LoginStateEnum.LOGIN]: t('sys.login.signInFormTitle'),
      [LoginStateEnum.REGISTER]: t('sys.login.signUpFormTitle'),
      [LoginStateEnum.MOBILE]: t('sys.login.mobileSignInFormTitle'),
      [LoginStateEnum.QR_CODE]: t('sys.login.qrSignInFormTitle'),
    };
    return titleObj[unref(getLoginState)];
  });
</script>
