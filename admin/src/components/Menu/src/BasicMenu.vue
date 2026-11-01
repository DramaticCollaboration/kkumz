<template>
  <Menu
    :selectedKeys="selectedKeys"
    :defaultSelectedKeys="defaultSelectedKeys"
    :mode="mode"
    :openKeys="getOpenKeys"
    :inlineIndent="inlineIndent"
    :theme="theme"
    @openChange="handleOpenChange"
    :class="getMenuClass"
    @click="handleMenuClick"
    :subMenuOpenDelay="0.2"
    v-bind="getInlineCollapseOptions"
  >
    <template v-for="item in items" :key="item.path">
      <BasicSubMenuItem :item="item" :theme="theme" :isHorizontal="isHorizontal" />
    </template>
  </Menu>
</template>
<script lang="ts">
  import type { MenuState } from './types';
  import { computed, defineComponent, unref, reactive, watch, toRefs, ref } from 'vue';
  import { Menu } from 'ant-design-vue';
  import BasicSubMenuItem from './components/BasicSubMenuItem.vue';
  import { MenuModeEnum, MenuTypeEnum } from '/@/enums/menuEnum';
  import { useOpenKeys } from './useOpenKeys';
  import { RouteLocationNormalizedLoaded, useRouter } from 'vue-router';
  import { isFunction, isUrl } from '/@/utils/is';
  import { basicProps } from './props';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { REDIRECT_NAME } from '/@/router/constant';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { getCurrentParentPath } from '/@/router/menus';
  import { listenerRouteChange } from '/@/logics/mitt/routeChange';
  import { getAllParentPath } from '/@/router/helper/menuHelper';
  import { createBasicRootMenuContext } from './useBasicMenuContext';
  import { URL_HASH_TAB } from '/@/utils';
  import { getMenus } from '/@/router/menus';

  export default defineComponent({
    name: 'BasicMenu',
    components: {
      Menu,
      BasicSubMenuItem,
    },
    props: basicProps,
    emits: ['menuClick'],
    setup(props, { emit }) {
      const isClickGo = ref(false);
      const currentActiveMenu = ref('');

      const menuState = reactive<MenuState>({
        defaultSelectedKeys: [],
        openKeys: [],
        selectedKeys: [],
        collapsedOpenKeys: [],
      });
      createBasicRootMenuContext({ menuState: menuState });
      const { prefixCls } = useDesign('basic-menu');
      const { items, mode, accordion } = toRefs(props);

      const { getCollapsed, getTopMenuAlign, getSplit } = useMenuSetting();

      const { currentRoute } = useRouter();

      const { handleOpenChange, setOpenKeys, getOpenKeys } = useOpenKeys(menuState, items, mode as any, accordion);

      const getIsTopMenu = computed(() => {
        const { type, mode } = props;

        return (type === MenuTypeEnum.TOP_MENU && mode === MenuModeEnum.HORIZONTAL) || (props.isHorizontal && unref(getSplit));
      });

      const getMenuClass = computed(() => {
        const align = props.isHorizontal && unref(getSplit) ? 'start' : unref(getTopMenuAlign);
        return [
          prefixCls,
          `justify-${align}`,
          {
            [`${prefixCls}__second`]: !props.isHorizontal && unref(getSplit),
            [`${prefixCls}__sidebar-hor`]: unref(getIsTopMenu),
          },
        ];
      });

      const getInlineCollapseOptions = computed(() => {
        const isInline = props.mode === MenuModeEnum.INLINE;

        const inlineCollapseOptions: { inlineCollapsed?: boolean } = {};
        if (isInline) {
          inlineCollapseOptions.inlineCollapsed = props.mixSider ? false : unref(getCollapsed);
        }
        return inlineCollapseOptions;
      });

      listenerRouteChange((route) => {
        if (route.name === REDIRECT_NAME) return;
        handleMenuChange(route);
        currentActiveMenu.value = route.meta?.currentActiveMenu as string;

        if (unref(currentActiveMenu)) {
          menuState.selectedKeys = [unref(currentActiveMenu)];
          setOpenKeys(unref(currentActiveMenu));
        }
      });

      !props.mixSider &&
        watch(
          () => props.items,
          () => {
            handleMenuChange();
          }
        );

      async function handleMenuClick({ item, key }: { item: any; key: string; keyPath: string[] }) {
        const { beforeClickFn } = props;
        if (isUrl(key)) {
          key = key.replace(URL_HASH_TAB, '#');
          window.open(key);
          return;
        }
        if (beforeClickFn && isFunction(beforeClickFn)) {
          const flag = await beforeClickFn(key);
          if (!flag) return;
        }
        if (props.type === MenuTypeEnum.MIX) {
          const menus = await getMenus();
          const menuItem = getMatchingPath(menus, key);
          if (menuItem && !menuItem.redirect && menuItem.children?.length) {
            const subMenuItem = getSubMenu(menuItem.children);
            if (subMenuItem?.path) {
              const path = subMenuItem.redirect ?? subMenuItem.path;
              let _key = path;
              if (isUrl(path)) {
                window.open(path);
                // 외부에서 내보내기를 열 때 사용되는 키는 URL이 될 수 없습니다. 그렇지 않으면 왼쪽 메뉴가 표시되지 않습니다.
                _key = key;
              }
              emit('menuClick', _key, { title: subMenuItem.title });
            } else {
              emit('menuClick', key, item);
            }
          } else {
            emit('menuClick', key, item);
          }
        } else {
          emit('menuClick', key, item);
        }
        // emit('menuClick', key, item);

        isClickGo.value = true;
        // const parentPath = await getCurrentParentPath(key);

        // menuState.openKeys = [parentPath];
        menuState.selectedKeys = [key];
      }

      async function handleMenuChange(route?: RouteLocationNormalizedLoaded) {
        if (unref(isClickGo)) {
          isClickGo.value = false;
          return;
        }
        const path = (route || unref(currentRoute)).meta?.currentActiveMenu || (route || unref(currentRoute)).path;
        setOpenKeys(path);
        if (unref(currentActiveMenu)) return;
        if (props.isHorizontal && unref(getSplit)) {
          const parentPath = await getCurrentParentPath(path);
          menuState.selectedKeys = [parentPath];
        } else {
          const parentPaths = await getAllParentPath(props.items, path);
          menuState.selectedKeys = parentPaths;
        }
      }
      /**
       * liaozhiyang
       * 2024-05-18
       * 지정된 메뉴 아래의 첫 번째 메뉴 가져오기
       */
      function getSubMenu(menus) {
        for (let i = 0, len = menus.length; i < len; i++) {
          const item = menus[i];
          if (item.path && !item.children?.length) {
            return item;
          } else if (item.children?.length) {
            const result = getSubMenu(item.children);
            if (result) {
              return result;
            }
          }
        }
        return null;
      }

      /**
       * liaozhiyang
       * 2024-05-18
       * 메뉴 일치 경로 가져오기
       */
      function getMatchingPath(menus, path) {
        for (let i = 0, len = menus.length; i < len; i++) {
          const item = menus[i];
          if (item.path === path) {
            return item;
          } else if (item.children?.length) {
            const result = getMatchingPath(item.children, path);
            if (result) {
              return result;
            }
          }
        }
        return null;
      }

      return {
        handleMenuClick,
        getInlineCollapseOptions,
        getMenuClass,
        handleOpenChange,
        getOpenKeys,
        ...toRefs(menuState),
      };
    },
  });
</script>
<style lang="less">
  @import './index.less';
</style>
