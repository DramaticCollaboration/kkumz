<!--
 * @Author: Empasy Kkumz Architecture Team
 * @Description: Empasy Vertical Service AppLogo component for Kkumz Admin
-->
<template>
  <div class="anticon flex items-center cursor-pointer select-none py-1.5 px-2.5" :class="getAppLogoClass" @click="goHome">
    <img
      src="../../../assets/images/logo.png"
      alt="Kkumz"
      class="w-8 h-8 rounded-lg shadow-sm object-contain mr-2.5 shrink-0 transition-transform hover:scale-105"
    />

    <div class="flex items-center text-lg font-bold tracking-tight whitespace-nowrap" :class="getTitleClass" v-show="showTitle">
      <span
        class="font-extrabold tracking-tight shrink-0"
        :class="theme === 'light' ? 'text-slate-900' : 'text-white'"
      >
        Awesome<span class="text-[#3B82F6]">Fit</span>
      </span>
      <span
        class="ml-2 px-1.5 py-0.5 text-[10px] leading-none font-semibold rounded uppercase tracking-wider whitespace-nowrap shrink-0 hidden sm:inline-block"
        :class="
          theme === 'light'
            ? 'bg-blue-50 text-blue-700 border border-blue-300'
            : 'bg-blue-950/70 text-blue-300 border border-blue-500/40'
        "
      >
        B2B
      </span>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, unref } from 'vue';
  import { useGlobSetting } from '/@/hooks/setting';
  import { useGo } from '/@/hooks/web/usePage';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { PageEnum } from '/@/enums/pageEnum';
  import { useUserStore } from '/@/store/modules/user';

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
    go(userStore.getUserInfo.homePath || PageEnum.BASE_HOME);
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
