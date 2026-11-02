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
        <Button type="link" size="small" class="!p-0 !text-xs !text-slate-500 dark:!text-slate-400 hover:!text-blue-600 dark:hover:!text-blue-400" @click="handleForgotPassword">
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

      <!-- 체험 계정 빠른 선택 -->
      <div class="mt-4 pt-3.5 border-t border-slate-100 dark:border-slate-800">
        <div class="flex items-center justify-between mb-2">
          <button
            type="button"
            class="text-xs font-semibold text-slate-700 dark:text-slate-300 hover:text-blue-600 dark:hover:text-blue-400 cursor-pointer flex items-center gap-1.5"
            @click="showDemoAccounts = !showDemoAccounts"
          >
            <span>권한별 시연 계정 빠른 로그인</span>
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
              class="flex flex-col items-start px-3 py-2.5 rounded-lg bg-slate-50/90 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 hover:border-blue-500 dark:hover:border-blue-400 hover:bg-blue-50/50 dark:hover:bg-slate-700/60 transition text-left cursor-pointer group"
              @click="quickLogin('admin', '123456')"
            >
              <div class="flex items-center justify-between w-full">
                <span class="font-bold text-slate-800 dark:text-slate-100 group-hover:text-blue-600 dark:group-hover:text-blue-400 text-xs">총괄 관리자</span>
                <span class="text-[10px] px-1.5 py-0.2 rounded bg-purple-50 text-purple-700 dark:bg-purple-950/70 dark:text-purple-300 font-mono">admin</span>
              </div>
              <span class="text-[10px] text-slate-500 dark:text-slate-400 mt-1">AI 스튜디오 · 배포 · 시스템 전체</span>
            </button>

            <button
              type="button"
              class="flex flex-col items-start px-3 py-2.5 rounded-lg bg-slate-50/90 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 hover:border-blue-500 dark:hover:border-blue-400 hover:bg-blue-50/50 dark:hover:bg-slate-700/60 transition text-left cursor-pointer group"
              @click="quickLogin('moderator.kim', '123456')"
            >
              <div class="flex items-center justify-between w-full">
                <span class="font-bold text-slate-800 dark:text-slate-100 group-hover:text-blue-600 dark:group-hover:text-blue-400 text-xs">모더레이터</span>
                <span class="text-[10px] px-1.5 py-0.2 rounded bg-blue-50 text-blue-700 dark:bg-blue-950/70 dark:text-blue-300 font-mono">moderator.kim</span>
              </div>
              <span class="text-[10px] text-slate-500 dark:text-slate-400 mt-1">커뮤니티 큐레이션 · 신고 심사</span>
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

  const ACol = Col;
  const ARow = Row;
  const FormItem = Form.Item;
  const InputPassword = Input.Password;
  const { t } = useI18n();
  const { notification } = useMessage();
  const { prefixCls } = useDesign('login');
  const userStore = useUserStore();

  const { getLoginState } = useLoginState();
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
    // 모더레이터 계정으로 빠른 로그인 시 총괄관리자 전용 라우트(/studio, /deployment, /system) 리디렉션 파라미터가 남아있다면 제거
    if (account.startsWith('moderator')) {
      const currentRedirect = router.currentRoute.value?.query?.redirect as string;
      if (currentRedirect && /(\/studio|\/deployment|\/system)/.test(currentRedirect)) {
        router.replace({ path: PageEnum.BASE_LOGIN });
      }
    }
    nextTick(() => {
      handleLogin();
    });
  }

  function handleForgotPassword() {
    Modal.info({
      title: '관리자 계정 비밀번호 초기화 안내',
      content: '보안 정책상 백오피스 운영 계정의 비밀번호 초기화는 시스템 총괄 관리자(admin@kkumz.com) 권한으로만 가능합니다. 사내 보안 담당자에게 초기화를 요청해 주세요.',
      okText: '확인',
      centered: true,
    });
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
