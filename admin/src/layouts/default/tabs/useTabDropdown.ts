import type { TabContentProps } from './types';
import type { DropMenu } from '/@/components/Dropdown';
import type { ComputedRef } from 'vue';

import { computed, unref, reactive } from 'vue';
import { MenuEventEnum } from './types';
import { useMultipleTabStore } from '/@/store/modules/multipleTab';
import { RouteLocationNormalized, useRouter } from 'vue-router';
import { useTabs } from '/@/hooks/web/useTabs';
import { useI18n } from '/@/hooks/web/useI18n';

export function useTabDropdown(tabContentProps: TabContentProps, getIsTabs: ComputedRef<boolean>) {
  const state = reactive({
    current: null as Nullable<RouteLocationNormalized>,
    currentIndex: 0,
  });

  const { t } = useI18n();
  const tabStore = useMultipleTabStore();
  const { currentRoute } = useRouter();
  const { refreshPage, closeAll, close, closeLeft, closeOther, closeRight } = useTabs();

  const getTargetTab = computed((): RouteLocationNormalized => {
    return unref(getIsTabs) ? tabContentProps.tabItem : unref(currentRoute);
  });

  /**
   * @description: drop-down list
   */
  const getDropMenuList = computed(() => {
    if (!unref(getTargetTab)) {
      return;
    }
    const { meta } = unref(getTargetTab);
    const { path } = unref(currentRoute);

    // Refresh button
    const curItem = state.current;

    const isCurItem = curItem ? curItem.path === path : false;
    const index = state.currentIndex;
    const refreshDisabled = !isCurItem;
    // Close left
    const closeLeftDisabled = () => {
      if (index === 0) {
        return true;
      } else {
        // 【TV360X-1039】홈페이지와 다른 탭 페이지만 있는 경우 왼쪽 닫기 비활성화
        const validTabList = tabStore.getTabList.filter((item) => !item?.meta?.affix);
        return validTabList[0].path === state.current?.path;
      }
    };
    // Close other
    const closeOtherDisabled = () => {
      if (tabStore.getTabList.length === 1) {
        return true;
      } else {
        // 【TV360X-1039】홈 페이지와 다른 탭 페이지만 있는 경우 다른 비활성화를 끄십시오.
        const validTabList = tabStore.getTabList.filter((item) => !item?.meta?.affix);
        return validTabList.length == 1;
      }
    };

    // Close right
    const closeRightDisabled = index === tabStore.getTabList.length - 1 && tabStore.getLastDragEndIndex >= 0;
    const dropMenuList: DropMenu[] = [
      {
        icon: 'jam:refresh-reverse',
        event: MenuEventEnum.REFRESH_PAGE,
        text: t('layout.multipleTab.reload'),
        disabled: refreshDisabled,
        divider: true,
      },
      // {
      //   icon: 'ic:twotone-close',
      //   event: MenuEventEnum.CLOSE_CURRENT,
      //   text: t('layout.multipleTab.close'),
      //   disabled: !!meta?.affix || disabled,
      //   divider: true,
      // },
      {
        icon: 'mdi:arrow-left',
        event: MenuEventEnum.CLOSE_LEFT,
        text: t('layout.multipleTab.closeLeft'),
        disabled: closeLeftDisabled(),
        divider: false,
      },
      {
        icon: 'mdi:arrow-right',
        event: MenuEventEnum.CLOSE_RIGHT,
        text: t('layout.multipleTab.closeRight'),
        disabled: closeRightDisabled,
        divider: true,
      },
      {
        icon: 'material-symbols:arrows-outward',
        event: MenuEventEnum.CLOSE_OTHER,
        text: t('layout.multipleTab.closeOther'),
        disabled: closeOtherDisabled(),
      },
      // {
      //   icon: 'clarity:minus-line',
      //   event: MenuEventEnum.CLOSE_ALL,
      //   text: t('layout.multipleTab.closeAll'),
      //   disabled: disabled,
      // },
    ];

    return dropMenuList;
  });

  function handleContextMenu(tabItem: RouteLocationNormalized) {
    return (e: Event) => {
      if (!tabItem) {
        return;
      }
      e?.preventDefault();
      const index = tabStore.getTabList.findIndex((tab) => tab.path === tabItem.path);
      state.current = tabItem;
      state.currentIndex = index;
    };
  }

  // Handle right click event
  function handleMenuEvent(menu: DropMenu): void {
    const { event } = menu;
    switch (event) {
      case MenuEventEnum.REFRESH_PAGE:
        // refresh page
        refreshPage();
        break;
      // Close current
      case MenuEventEnum.CLOSE_CURRENT:
        close(tabContentProps.tabItem);
        break;
      // Close left
      case MenuEventEnum.CLOSE_LEFT:
        closeLeft(state.current);
        break;
      // Close right
      case MenuEventEnum.CLOSE_RIGHT:
        closeRight(state.current);
        break;
      // Close other
      case MenuEventEnum.CLOSE_OTHER:
        closeOther(state.current);
        break;
      // Close all
      case MenuEventEnum.CLOSE_ALL:
        closeAll(state.current);
        break;
    }
  }
  return { getDropMenuList, handleMenuEvent, handleContextMenu };
}
