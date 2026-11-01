<template>
  <div class="mb-3 sm:mb-5 enter-x text-center xl:text-left">
    <h2 class="text-lg sm:text-2xl font-bold text-slate-800 dark:text-white xl:text-3xl tracking-tight break-keep">
      Kkumz 자율운영 포털 로그인
    </h2>
    <p v-if="isLoginState" class="mt-1 text-xs text-slate-500 dark:text-slate-400 break-keep">
      도도매 무인 발주부터 1:1 순익 대사까지 전자동으로 관리하세요
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
