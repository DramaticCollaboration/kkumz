<template>
  <span>
    <Dropdown placement="bottomRight" :overlayClassName="`${prefixCls}-dropdown-overlay`">
      <span :class="[prefixCls, `${prefixCls}--${theme}`]" class="flex">
        <img :class="`${prefixCls}__header`" :src="getAvatarUrl" />
        <span :class="`${prefixCls}__info hidden md:block`">
          <span :class="`${prefixCls}__name  `" class="truncate">
            {{ getUserInfo.realname }}
          </span>
        </span>
      </span>

      <template #overlay>
        <Menu @click="handleMenuClick">
          <MenuItem key="password" :text="t('layout.header.dropdownItemSwitchPassword')" icon="ant-design:key-outlined" />
          <MenuItem key="cache" :text="t('layout.header.dropdownItemRefreshCache')" icon="ion:sync-outline" />
          <MenuItem v-if="getIsMobile" key="mobileTheme" :text="isDarkMode ? '라이트 모드' : '다크 모드'" icon="ant-design:bg-colors-outlined" />
          <MenuItem v-if="getIsMobile" key="mobileLocale" :text="currentLocale === 'en' ? '한국어 전환' : 'English'" icon="ant-design:global-outlined" />
          <MenuItem key="logout" :text="t('layout.header.dropdownItemLoginOut')" icon="ion:power-outline" />
        </Menu>
      </template>
    </Dropdown>
    <LockAction v-if="lockActionVisible" ref="lockActionRef" @register="register" />
    <DepartSelect ref="loginSelectRef" />
    <UpdatePassword v-if="passwordVisible" ref="updatePasswordRef" />
  </span>
</template>
<script lang="ts">
  // components
  import { Dropdown, Menu } from 'ant-design-vue';

  import { defineComponent, computed, ref } from 'vue';

  import { SITE_URL } from '/@/settings/siteSetting';

  import { useUserStore } from '/@/store/modules/user';
  import { useHeaderSetting } from '/@/hooks/setting/useHeaderSetting';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useAppInject } from '/@/hooks/web/useAppInject';
  import { useRootSetting } from '/@/hooks/setting/useRootSetting';
  import { useLocale } from '/@/locales/useLocale';
  import { ThemeEnum } from '/@/enums/appEnum';
  import { updateDarkTheme } from '/@/logics/theme/dark';
  import { updateHeaderBgColor, updateSidebarBgColor } from '/@/logics/theme/updateBackground';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/src/hooks/web/useMessage';
  import { useGo } from '/@/hooks/web/usePage';
  import headerImg from '/@/assets/images/header.jpg';
  import { propTypes } from '/@/utils/propTypes';
  import { openWindow } from '/@/utils';

  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';

  import { refreshCache, queryAllDictItems } from '/@/views/system/dict/dict.api';
  import { DB_DICT_DATA_KEY } from '/src/enums/cacheEnum';
  import { removeAuthCache, setAuthCache } from '/src/utils/auth';
  import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
  import { getRefPromise } from '/@/utils/index';

  type MenuEvent = 'mobileTheme' | 'mobileLocale' | 'logout' | 'doc' | 'lock' | 'cache' | 'depart';
  const { createMessage } = useMessage();
  export default defineComponent({
    name: 'UserDropdown',
    components: {
      Dropdown,
      Menu,
      MenuItem: createAsyncComponent(() => import('./DropMenuItem.vue')),
      MenuDivider: Menu.Divider,
      LockAction: createAsyncComponent(() => import('../lock/LockModal.vue')),
      DepartSelect: createAsyncComponent(() => import('./DepartSelect.vue')),
      UpdatePassword: createAsyncComponent(() => import('./UpdatePassword.vue')),
    },
    props: {
      theme: propTypes.oneOf(['dark', 'light']),
    },
    setup() {
      const { prefixCls } = useDesign('header-user-dropdown');
      const { getIsMobile } = useAppInject();
      const { getDarkMode, setDarkMode } = useRootSetting();
      const { getLocale, changeLocale } = useLocale();
      const isDarkMode = computed(() => getDarkMode.value === ThemeEnum.DARK);
      const currentLocale = computed(() => (getLocale.value === 'en' ? 'en' : 'ko'));

      function toggleMobileTheme() {
        const nextMode = getDarkMode.value === ThemeEnum.DARK ? ThemeEnum.LIGHT : ThemeEnum.DARK;
        setDarkMode(nextMode);
        updateDarkTheme(nextMode);
        updateHeaderBgColor();
        updateSidebarBgColor();
      }

      async function toggleMobileLocale() {
        await changeLocale((currentLocale.value === 'en' ? 'ko_KR' : 'en') as any);
      }
      const { t } = useI18n();
      const { getShowDoc, getUseLockPage } = useHeaderSetting();
      const userStore = useUserStore();
      const go = useGo();
      const passwordVisible = ref(false);
      const lockActionVisible = ref(false);
      const lockActionRef = ref(null);

      const getUserInfo = computed(() => {
        const { realname = '', avatar, desc } = userStore.getUserInfo || {};
        const cleanName = (realname || '').replace(/\(.*?\)/g, '').trim();
        return { realname: cleanName || realname, avatar: avatar || headerImg, desc };
      });

      const getAvatarUrl = computed(() => {
        let { avatar } = getUserInfo.value;
        if (avatar == headerImg) {
          return avatar;
        } else {
          return getFileAccessHttpUrl(avatar);
        }
      });

      const [register, { openModal }] = useModal();
      /**
       * 여러 부서 팝업창 로직
       */
      const loginSelectRef = ref();
      async function handleLock() {
        await getRefPromise(lockActionRef);
        openModal(true);
      }
      //  login out
      function handleLoginOut() {
        userStore.confirmLoginOut();
      }

      // open doc
      function openDoc() {
        openWindow(SITE_URL);
      }

      // 캐시 지우기
      async function clearCache() {
        const result = await refreshCache();
        if (result.success) {
          const res = await queryAllDictItems();
          removeAuthCache(DB_DICT_DATA_KEY);
          setAuthCache(DB_DICT_DATA_KEY, res.result);
          createMessage.success(t('layout.header.refreshCacheComplete'));
        } else {
          createMessage.error(t('layout.header.refreshCacheFailure'));
        }
      }
      // 부서 전환
      function updateCurrentDepart() {
        loginSelectRef.value.show();
      }
      // 비밀번호 변경
      const updatePasswordRef = ref();
      async function updatePassword() {
        passwordVisible.value = true;
        await getRefPromise(updatePasswordRef);
        updatePasswordRef.value.show(userStore.getUserInfo.username);
      }
      function handleMenuClick(e: { key: MenuEvent }) {
        switch (e.key) {
          case 'mobileTheme':
            toggleMobileTheme();
            break;
          case 'mobileLocale':
            toggleMobileLocale();
            break;
          case 'logout':
            handleLoginOut();
            break;
          case 'doc':
            openDoc();
            break;
          case 'lock':
            handleLock();
            break;
          case 'cache':
            clearCache();
            break;
          case 'depart':
            updateCurrentDepart();
            break;
          case 'password':
            updatePassword();
            break;
          case 'account':
            go(`/system/usersetting`);
            break;
        }
      }

      return {
        prefixCls,
        getIsMobile,
        isDarkMode,
        currentLocale,
        t,
        getUserInfo,
        getAvatarUrl,
        handleMenuClick,
        getShowDoc,
        register,
        getUseLockPage,
        loginSelectRef,
        updatePasswordRef,
        passwordVisible,
        lockActionVisible,
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-header-user-dropdown';

  .@{prefix-cls} {
    height: @header-height;
    padding: 0 0 0 10px;
    padding-right: 10px;
    overflow: hidden;
    font-size: 12px;
    cursor: pointer;
    align-items: center;

    img {
      width: 24px;
      height: 24px;
      margin-right: 12px;
    }

    &__header {
      border-radius: 50%;
    }

    &__name {
      font-size: 14px;
    }

    &--dark {
      &:hover {
        background-color: @header-dark-bg-hover-color;
      }
    }

    &--light {
      &:hover {
        background-color: @header-light-bg-hover-color;
      }

      .@{prefix-cls}__name {
        color: @text-color-base;
      }

      .@{prefix-cls}__desc {
        color: @header-light-desc-color;
      }
    }

    &-dropdown-overlay {
      width: 160px;
      .ant-dropdown-menu-item {
        min-width: 160px;
      }
    }
  }
</style>
