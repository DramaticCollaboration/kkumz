<template>
  <LoginFormTitle v-show="getShow" class="enter-x" />

  <div v-show="getShow" class="enter-x">
    <!-- 일반 아이디/비밀번호 로그인 폼 -->
    <Form :model="formData" :rules="getFormRules" ref="formRef" @keypress.enter="handleLogin">
      <FormItem name="account" class="mb-3">
        <Input
          size="large"
          v-model:value="formData.account"
          :placeholder="t('sys.login.userName')"
          class="fix-auto-fill !rounded-lg"
        >
          <template #prefix>
            <span class="i-ant-design:user-outlined text-slate-400 mr-1"></span>
          </template>
        </Input>
      </FormItem>

      <FormItem name="password" class="mb-3">
        <InputPassword
          size="large"
          visibilityToggle
          v-model:value="formData.password"
          :placeholder="t('sys.login.password')"
          class="!rounded-lg"
        >
          <template #prefix>
            <span class="i-ant-design:lock-outlined text-slate-400 mr-1"></span>
          </template>
        </InputPassword>
      </FormItem>

      <!-- 점진적 보안 캡차 (로그인 실패 시 노출) -->
      <div v-if="showCaptcha" class="mb-3 p-2.5 rounded-lg bg-slate-50 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 transition-all">
        <div class="text-[11px] text-slate-600 dark:text-slate-300 font-medium mb-1.5 flex items-center justify-between">
          <span>보안 인증코드 입력</span>
          <span class="text-slate-400 dark:text-slate-400 cursor-pointer hover:underline" @click="handleChangeCheckCode">새로고침</span>
        </div>
        <ARow :gutter="8">
          <ACol :span="14">
            <FormItem name="inputCode" class="!mb-0">
              <Input
                size="large"
                v-model:value="formData.inputCode"
                :placeholder="t('sys.login.inputCode')"
                class="!rounded-lg"
              />
            </FormItem>
          </ACol>
          <ACol :span="10">
            <div
              class="h-10 flex items-center justify-center bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-700 cursor-pointer overflow-hidden shadow-2xs"
              @click="handleChangeCheckCode"
              title="클릭하여 새로고침"
            >
              <img
                v-if="randCodeData.requestCodeSuccess"
                :src="randCodeData.randCodeImage"
                class="h-full object-contain"
              />
              <span v-else class="text-xs text-slate-400">인증코드 불러오는 중</span>
            </div>
          </ACol>
        </ARow>
      </div>

      <div class="flex items-center justify-between mb-4">
        <Checkbox v-model:checked="rememberMe" size="small">
          <span class="text-xs text-slate-600 dark:text-slate-300">{{ t('sys.login.rememberMe') }}</span>
        </Checkbox>
        <Button type="link" size="small" class="!p-0 !text-xs !text-slate-500 dark:!text-slate-400 hover:!text-blue-600 dark:hover:!text-blue-400" @click="setLoginState(LoginStateEnum.RESET_PASSWORD)">
          {{ t('sys.login.forgetPassword') }}
        </Button>
      </div>

      <FormItem class="!mb-3">
        <Button
          type="primary"
          size="large"
          block
          @click="handleLogin"
          :loading="loading"
          class="!rounded-lg !h-11 !font-bold !bg-blue-600 hover:!bg-blue-500 shadow-md shadow-blue-500/20"
        >
          {{ t('sys.login.loginButton') }}
        </Button>
      </FormItem>

      <!-- 간편 로그인 섹션 (슬림 3버튼) -->
      <div class="mb-3">
        <div class="relative my-3">
          <div class="absolute inset-0 flex items-center">
            <div class="w-full border-t border-slate-200 dark:border-slate-700/80"></div>
          </div>
          <div class="relative flex justify-center text-xs">
            <span class="bg-white dark:bg-[#1e293b] px-2 text-slate-400 dark:text-slate-400">또는 간편 로그인</span>
          </div>
        </div>

        <div class="grid grid-cols-3 gap-2">
          <button
            type="button"
            @click="handleKakaoLogin"
            class="flex items-center justify-center gap-1.5 h-9 px-2 rounded-lg bg-[#FEE500] hover:bg-[#fbd800] text-[#191919] font-medium text-xs shadow-xs transition cursor-pointer border border-[#FEE500]"
            title="카카오 로그인"
          >
            <svg class="w-3.5 h-3.5 fill-current shrink-0" viewBox="0 0 24 24">
              <path d="M12 3c-5.523 0-10 3.582-10 8 0 2.868 1.867 5.39 4.685 6.726l-.95 3.518c-.1.37.28.69.6.49l4.19-2.617c.484.055.975.083 1.475.083 5.523 0 10-3.582 10-8s-4.477-8-10-8z"/>
            </svg>
            카카오
          </button>

          <button
            type="button"
            @click="handleSocialLogin('naver')"
            class="flex items-center justify-center gap-1.5 h-9 px-2 rounded-lg bg-[#03C75A] hover:bg-[#02b350] text-white font-medium text-xs shadow-xs transition border border-[#03C75A] cursor-pointer"
            title="네이버 로그인"
          >
            <span class="font-black text-xs leading-none">N</span> 네이버
          </button>

          <button
            type="button"
            @click="handleSocialLogin('google')"
            class="flex items-center justify-center gap-1.5 h-9 px-2 rounded-lg bg-white hover:bg-slate-50 text-slate-700 dark:bg-slate-800 dark:hover:bg-slate-700 dark:text-slate-200 dark:border-slate-700 font-medium text-xs shadow-xs transition border border-slate-200 cursor-pointer"
            title="구글 로그인"
          >
            <svg class="w-3.5 h-3.5 shrink-0" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.8-2.4 3.66v3.05h3.9c2.28-2.1 3.645-5.2 3.645-9.15z"/>
              <path fill="#34A853" d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.9-3.05c-1.07.72-2.45 1.16-4.03 1.16-3.1 0-5.74-2.1-6.68-4.92H1.28v3.15C3.26 21.36 7.33 24 12 24z"/>
              <path fill="#FBBC05" d="M5.32 14.28c-.24-.72-.38-1.49-.38-2.28s.14-1.56.38-2.28V6.57H1.28C.46 8.2 0 10.04 0 12s.46 3.8 1.28 5.43l4.04-3.15z"/>
              <path fill="#EA4335" d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.33 0 3.26 2.64 1.28 6.57l4.04 3.15c.94-2.82 3.58-4.97 6.68-4.97z"/>
            </svg>
            구글
          </button>
        </div>
      </div>

      <!-- 체험 계정 빠른 선택 -->
      <div class="mt-3 pt-3 border-t border-slate-100 dark:border-slate-800">
        <div class="flex items-center justify-between mb-2">
          <button
            type="button"
            class="text-xs font-semibold text-slate-700 dark:text-slate-300 hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer flex items-center gap-1.5"
            @click="showDemoAccounts = !showDemoAccounts"
          >
            <span>체험 계정 빠른 선택</span>
            <span class="text-[10px] text-slate-400 bg-slate-100 dark:bg-slate-800 px-1.5 py-0.5 rounded">{{ showDemoAccounts ? '접기' : '펼치기' }}</span>
          </button>
          <span v-if="showDemoAccounts" class="text-[11px] text-slate-500 dark:text-slate-400 bg-slate-100 dark:bg-slate-800/80 px-2 py-0.5 rounded font-medium">
            공통 비밀번호 123456
          </span>
        </div>

        <div v-show="showDemoAccounts" class="space-y-1.5 text-xs transition-all">
          <div class="grid grid-cols-2 gap-2">
            <button
              type="button"
              class="flex flex-col items-start px-2.5 py-2 rounded-lg bg-slate-50/90 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 hover:border-blue-500 dark:hover:border-blue-400 hover:bg-blue-50/50 dark:hover:bg-slate-700/60 transition text-left cursor-pointer group"
              @click="quickLogin('admin', '123456')"
            >
              <div class="flex items-center justify-between w-full">
                <span class="font-bold text-slate-800 dark:text-slate-100 group-hover:text-blue-600 dark:group-hover:text-blue-400 text-xs">총괄 관리자</span>
                <span class="text-[10px] px-1.5 py-0.2 rounded bg-purple-50 text-purple-700 dark:bg-purple-950/70 dark:text-purple-300 font-mono">admin</span>
              </div>
              <span class="text-[10px] text-slate-500 dark:text-slate-400 mt-0.5">꿈즈 통합 관제 마스터</span>
            </button>

            <button
              type="button"
              class="flex flex-col items-start px-2.5 py-2 rounded-lg bg-slate-50/90 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 hover:border-blue-500 dark:hover:border-blue-400 hover:bg-blue-50/50 dark:hover:bg-slate-700/60 transition text-left cursor-pointer group"
              @click="quickLogin('moderator', '123456')"
            >
              <div class="flex items-center justify-between w-full">
                <span class="font-bold text-slate-800 dark:text-slate-100 group-hover:text-blue-600 dark:group-hover:text-blue-400 text-xs">모더레이터</span>
                <span class="text-[10px] px-1.5 py-0.2 rounded bg-blue-50 text-blue-700 dark:bg-blue-950/70 dark:text-blue-300 font-mono">moderator</span>
              </div>
              <span class="text-[10px] text-slate-500 dark:text-slate-400 mt-0.5">커뮤니티 · 신고 심사</span>
            </button>
          </div>
        </div>
      </div>
    </Form>
  </div>
</template>
<script lang="ts" setup>
  import { reactive, ref, toRaw, unref, computed, onMounted, nextTick } from 'vue';

  import { Checkbox, Form, Input, Row, Col, Button, Modal } from 'ant-design-vue';
  import LoginFormTitle from './LoginFormTitle.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';

  import { useUserStore } from '/@/store/modules/user';
  import { LoginStateEnum, useLoginState, useFormRules, useFormValid } from './useLogin';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { getCodeInfo } from '/@/api/sys/user';
  import { useRouter } from 'vue-router';
  import { PageEnum } from '/@/enums/pageEnum';

  const emit = defineEmits(['open-apply']);

  const ACol = Col;
  const ARow = Row;
  const FormItem = Form.Item;
  const InputPassword = Input.Password;
  const { t } = useI18n();
  const { notification } = useMessage();
  const { prefixCls } = useDesign('login');
  const userStore = useUserStore();

  const { setLoginState, getLoginState } = useLoginState();
  const { getFormRules } = useFormRules();

  const formRef = ref();
  const loading = ref(false);
  const rememberMe = ref(true);
  const showCaptcha = ref(false);
  const showDemoAccounts = ref(true);
  const failedCount = ref(0);

  const formData = reactive({
    account: '',
    password: '',
    inputCode: '',
  });

  const randCodeData = reactive({
    randCodeImage: '',
    requestCodeSuccess: false,
    checkKey: null,
  });

  const { validForm } = useFormValid(formRef);

  const getShow = computed(() => unref(getLoginState) === LoginStateEnum.LOGIN);

  async function handleLogin() {
    const data = await validForm();
    if (!data) return;
    try {
      loading.value = true;
      const { userInfo } = await userStore.login(
        toRaw({
          password: data.password,
          username: data.account,
          captcha: data.inputCode || '',
          checkKey: randCodeData.checkKey,
          mode: 'none',
        })
      );
      if (userInfo) {
        failedCount.value = 0;
        notification.success({
          message: t('sys.login.loginSuccessTitle'),
          description: `${t('sys.login.loginSuccessDesc')}: ${userInfo.realname || userInfo.username}`,
          duration: 3,
        });
      }
    } catch (error: any) {
      notification.error({
        message: t('sys.api.errorTip'),
        description: error.message || t('sys.api.networkExceptionMsg'),
        duration: 3,
      });
      loading.value = false;
      failedCount.value++;
      // 2회 이상 연속 실패 시 캡차 입력 영역 노출 및 새로고침
      if (failedCount.value >= 2) {
        showCaptcha.value = true;
        handleChangeCheckCode();
      }
    }
  }

  const router = useRouter();

  function quickLogin(account: string, pass: string) {
    formData.account = account;
    formData.password = pass;
    formData.inputCode = '';
    // 셀러 계정으로 빠른 로그인 시 관리자 전용 리디렉션 파라미터가 남아있다면 제거
    if (account.startsWith('seller')) {
      const currentRedirect = router.currentRoute.value?.query?.redirect as string;
      if (currentRedirect && /(automation|seller-apply|inquiry|supplier|channel|pricing-rules|dashboard\/)/.test(currentRedirect)) {
        router.replace({ path: PageEnum.BASE_LOGIN });
      }
    }
    nextTick(() => {
      handleLogin();
    });
  }

  function handleKakaoLogin() {
    emit('open-apply');
  }

  function handleSocialLogin(provider: string) {
    emit('open-apply');
  }

  function handleChangeCheckCode() {
    formData.inputCode = '';
    randCodeData.checkKey = new Date().getTime();
    getCodeInfo(randCodeData.checkKey).then((res) => {
      randCodeData.randCodeImage = res;
      randCodeData.requestCodeSuccess = true;
    }).catch(() => {
      randCodeData.requestCodeSuccess = false;
    });
  }

  onMounted(() => {
    if (showCaptcha.value) {
      handleChangeCheckCode();
    }
  });
</script>
