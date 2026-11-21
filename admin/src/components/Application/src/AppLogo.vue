<!--
 * @Author: Empasy Kkumz Architecture Team
 * @Description: Empasy Vertical Service AppLogo component for Kkumz Admin
-->
<template>
  <div class="anticon flex items-center cursor-pointer select-none py-1.5 px-2.5" :class="getAppLogoClass" @click="goHome">
    <img
      :src="kkumzLogo"
      :alt="t('appUi.k7681c3dc')"
      width="36"
      height="36"
      class="w-9 h-9 rounded-lg shadow-sm mr-2.5 shrink-0 transition-transform hover:scale-105"
    />

    <div class="flex items-center text-lg font-bold tracking-tight whitespace-nowrap" :class="getTitleClass" v-show="showTitle">
      <span
        class="font-extrabold tracking-tight shrink-0"
        :class="theme === 'light' ? 'text-slate-900' : 'text-white'"
      >
        {{ t('appUi.k9d77b845') }} <span class="text-[#818CF8]">Kkumz</span>
      </span>
      <span
        class="ml-2 px-1.5 py-0.5 text-[10px] leading-none font-semibold rounded uppercase tracking-wider whitespace-nowrap shrink-0 hidden sm:inline-block"
        :class="
          theme === 'light'
            ? 'bg-slate-50 text-slate-700 border border-slate-300'
            : 'bg-slate-950/70 text-slate-300 border border-slate-500/40'
        "
      >
        ADMIN
      </span>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, unref } from 'vue';
  import kkumzLogo from '/@/assets/svg/kkumz-logo.svg';
  import { useGlobSetting } from '/@/hooks/setting';
  import { useGo } from '/@/hooks/web/usePage';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { PageEnum } from '/@/enums/pageEnum';
  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const props = defineProps({
    theme: { type: String, validator: (v: string) => ['light', 'dark'].includes(v) },
    showTitle: { type: Boolean, default: true },
    alwaysShowTitle: { type: Boolean },
  });

  const { prefixCls } = useDesign('app-logo');
  const { getCollapsedShowTitle } = useMenuSetting();
  const userStore = useUserStore();
  const { title, shortTitle } = useGlobSetting();

  const go = useGo();

  const getAppLogoClass = computed(() => [
    prefixCls,
    props.theme,
    { 'collapsed-show-title': unref(getCollapsedShowTitle) },
  ]);

  const getTitleClass = computed(() => [
    `${prefixCls}__title`,
    {
      'xs:opacity-0': !props.alwaysShowTitle,
    },
  ]);

  function goHome() {
    const rawHome = userStore.getUserInfo.homePath;
    const targetHome = rawHome && !String(rawHome).startsWith('/ecommerce') ? rawHome : PageEnum.BASE_HOME;
    go(targetHome);
  }
</script>

<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-app-logo';

  .@{prefix-cls} {
    display: flex;
    align-items: center;
    padding-left: 10px;
    cursor: pointer;
    transition: all 0.2s ease;

    &.sync-layout-mix-sider-logo, &.sync-layout-menu-logo {
      background: @sider-logo-bg-color;
    }

    &.collapsed-show-title {
      padding-left: 16px;
    }

    &.light &__title {
      color: @primary-color;
    }

    &.dark &__title {
      color: @white;
    }

    &__title {
      font-size: 18px;
      font-weight: 700;
      transition: all 0.3s ease;
      line-height: normal;
    }
  }
</style>
