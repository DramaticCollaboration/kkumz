<template>
  <div class="mb-4 sm:mb-6 enter-x text-center xl:text-left">
    <div class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-blue-50 dark:bg-blue-950/60 text-blue-600 dark:text-blue-400 text-[11px] font-semibold mb-2">
      <span>KKUMZ ADMIN</span>
      <span class="text-blue-300 dark:text-blue-700">·</span>
      <span>{{ t('appUi.kd661d424') }}</span>
    </div>
    <h2 class="text-lg sm:text-2xl font-bold text-slate-800 dark:text-white xl:text-[26px] tracking-tight break-keep">
      {{ t('appUi.k6c0ba582') }}
    </h2>
    <p v-if="isLoginState" class="mt-1.5 text-xs text-slate-500 dark:text-slate-400 break-keep leading-relaxed">
      {{ t('appUi.ke2adae13') }}
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
