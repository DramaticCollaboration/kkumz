<template>
  <div :class="prefixCls" class="relative w-full min-h-screen px-3 sm:px-4 flex flex-col justify-start xl:justify-between overflow-x-hidden">
    <!-- 모바일 전용 상단 헤더 바 (로고 + 다크모드/언어) -->
    <header class="flex items-center justify-between w-full pt-3 pb-2 px-2 xl:hidden z-20 shrink-0 mb-1">
      <div class="mobile-logo-wrap">
        <AppLogo :alwaysShowTitle="true" />
      </div>
      <div class="flex items-center gap-2.5">
        <AppDarkModeToggle v-if="!sessionTimeout" class="enter-x" />
        <AppLocalePicker class="enter-x text-slate-300 hover:text-white" :showText="false" v-if="!sessionTimeout && showLocale" />
      </div>
    </header>

    <!-- 데스크톱 전용 상단 미니 유틸리티 (다크모드 & 언어) -->
    <div class="hidden xl:flex absolute top-5 right-8 z-20 items-center gap-3">
      <AppDarkModeToggle v-if="!sessionTimeout" class="enter-x" />
      <AppLocalePicker class="enter-x text-slate-400 hover:text-slate-600" :showText="false" v-if="!sessionTimeout && showLocale" />
    </div>

    <div class="container relative min-h-0 xl:min-h-full py-1 sm:py-4 mx-auto sm:px-10 flex-1 flex flex-col justify-start xl:justify-center">
      <div class="flex h-auto xl:h-full items-start xl:items-center w-full">
        <!-- 좌측 브랜딩 & 소셜 프루프 영역 (데스크톱 xl 이상만 노출) -->
        <div class="hidden min-h-full pl-4 mr-4 xl:flex xl:flex-col xl:w-6/12 justify-center">
          <AppLogo class="-enter-x mb-6" />
          <div class="pr-8">
            <div class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-slate-500/15 border border-slate-400/30 text-slate-300 text-xs font-medium mb-5 -enter-x">
              꿈즈 Kkumz AI 브랜드·멀티사이트 빌더 관제 백오피스
            </div>
            <h1 class="text-3xl font-bold text-white tracking-tight leading-snug -enter-x">
              AI 스튜디오 · 멀티사이트 배포 · 커뮤니티까지<br />단일 백오피스에서 통합 관제
            </h1>
            <p class="mt-4 text-slate-300 text-sm font-normal leading-relaxed -enter-x">
              AI 브랜드 네이밍·로고 생성 프롬프트 거버넌스부터 멀티사이트 릴리즈/롤백, WebIDE 빌드 파이프라인, 팀빌딩 라운지 및 모더레이션 심사까지 실시간으로 운영합니다.
            </p>
          </div>
        </div>

        <!-- 우측 로그인 폼 영역 -->
        <div class="flex w-full py-1 sm:py-5 xl:py-0 xl:my-0 xl:w-6/12">
          <div
            :class="`${prefixCls}-form`"
            class="relative w-full px-5 py-6 sm:px-8 sm:py-7 mx-auto my-0 xl:my-auto rounded-2xl bg-white dark:bg-slate-900 shadow-xl shadow-slate-200/70 dark:shadow-black/50 xl:ml-12 sm:w-3/4 lg:w-2/4 xl:w-[460px] enter-x border border-slate-200/80 dark:border-slate-800 mb-8 xl:mb-0"
          >
            <LoginForm />

            <!-- 하단 약관 & 보안 안내 푸터 -->
            <div class="mt-3 text-center text-[11px] text-slate-400 dark:text-slate-500 flex items-center justify-center gap-3">
              <span>꿈즈 Kkumz Admin · 256bit SSL 보안</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { computed } from 'vue';
  import { AppLogo } from '/@/components/Application';
  import { AppLocalePicker, AppDarkModeToggle } from '/@/components/Application';
  import LoginForm from './LoginForm.vue';
  import { useGlobSetting } from '/@/hooks/setting';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useLocaleStore } from '/@/store/modules/locale';
  import { useLoginState } from './useLogin';
  defineProps({
    sessionTimeout: {
      type: Boolean,
    },
  });

  const globSetting = useGlobSetting();
  const { prefixCls } = useDesign('login');
  const { t } = useI18n();
  const localeStore = useLocaleStore();
  const showLocale = localeStore.getShowPicker;
  const title = computed(() => globSetting?.title ?? '');
  const { handleBackLogin } = useLoginState();
  handleBackLogin();
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-login';
  @logo-prefix-cls: ~'@{namespace}-app-logo';
  @countdown-prefix-cls: ~'@{namespace}-countdown-input';
  @dark-bg: #293146;

  html[data-theme='dark'] {
    .@{prefix-cls} {
      background-color: #0f172a;

      &::before {
        background-image: url(/@/assets/svg/login-bg-dark.svg);
      }

      .ant-input,
      .ant-input-password,
      .ant-input-affix-wrapper {
        background-color: #0f172a !important;
        border-color: #334155 !important;
        color: #f8fafc !important;

        input {
          background-color: transparent !important;
          color: #f8fafc !important;
        }

        &::placeholder, input::placeholder {
          color: #64748b !important;
        }

        &:hover, &:focus, &-focused {
          border-color: #3b82f6 !important;
          box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2) !important;
        }
      }

      .ant-btn:not(.ant-btn-link):not(.ant-btn-primary) {
        border: 1px solid #334155;
        background-color: rgba(255, 255, 255, 0.05);
        color: #e2e8f0;
      }

      &-form {
        background-color: #1e293b !important;
        border: 1px solid rgba(255, 255, 255, 0.12) !important;
        box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.6) !important;
      }

      .app-iconify {
        color: #fff;
      }
    }

    input.fix-auto-fill,
    .fix-auto-fill input {
      -webkit-text-fill-color: #c9d1d9 !important;
      box-shadow: inherit !important;
    }
  }

  .@{prefix-cls} {
    min-height: 100vh;
    overflow-x: hidden;
    overflow-y: auto;

    @media (max-width: @screen-xl) {
      position: fixed !important;
      top: 0 !important;
      left: 0 !important;
      right: 0 !important;
      bottom: 0 !important;
      width: 100% !important;
      height: 100% !important;
      max-height: 100dvh !important;
      overflow-x: hidden !important;
      overflow-y: auto !important;
      -webkit-overflow-scrolling: touch !important;
      background-color: #293146;

      .@{prefix-cls}-form {
        background-color: #fff;
      }

      html[data-theme='dark'] & {
        background-color: #0f172a !important;

        .@{prefix-cls}-form {
          background-color: #1e293b !important;
          border-color: rgba(255, 255, 255, 0.12) !important;
        }
      }
    }

    @media (min-width: @screen-xl) {
      height: 100%;
      overflow: hidden;
    }

    &::before {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      margin-left: -48%;
      background-image: url(/@/assets/svg/login-bg.svg);
      background-position: 100%;
      background-repeat: no-repeat;
      background-size: auto 100%;
      content: '';
      @media (max-width: @screen-xl) {
        display: none;
      }
    }

    /* 모바일 전용 로고 래퍼 스타일 */
    .mobile-logo-wrap {
      .@{logo-prefix-cls} {
        position: relative !important;
        top: 0 !important;
        left: 0 !important;
        padding-left: 0 !important;
        height: auto !important;

        &__title {
          font-size: 16px !important;
          color: #fff !important;
        }

        img {
          width: 28px !important;
          height: 28px !important;
        }
      }
    }

    .@{logo-prefix-cls} {
      position: absolute;
      top: 12px;
      height: 30px;

      &__title {
        font-size: 16px;
        color: #fff;
      }

      img {
        width: 32px;
      }
    }

    .container {
      .@{logo-prefix-cls} {
        display: flex;
        width: 60%;
        height: 80px;

        &__title {
          font-size: 24px;
          color: #fff;
        }

        img {
          width: 48px;
        }
      }
    }

    &-sign-in-way {
      .anticon {
        font-size: 22px;
        color: #888;
        cursor: pointer;

        &:hover {
          color: @primary-color;
        }
      }
    }

    input:not([type='checkbox']) {
      width: 100%;
      min-width: 0;

      @media (min-width: @screen-sm) {
        min-width: 220px;
      }

      @media (min-width: @screen-md) {
        min-width: 240px;
      }

      @media (min-width: @screen-lg) {
        min-width: 260px;
      }

      @media (min-width: @screen-xl) {
        min-width: 320px;
      }
    }

    .@{countdown-prefix-cls} input {
      min-width: unset;
    }

    .ant-divider-inner-text {
      font-size: 12px;
      color: @text-color-secondary;
    }
  }
</style>
