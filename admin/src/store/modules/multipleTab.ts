import type { RouteLocationNormalized, RouteLocationRaw, Router } from 'vue-router';

import { toRaw, unref } from 'vue';
import { defineStore } from 'pinia';
import { store } from '/@/store';

import { useGo, useRedo } from '/@/hooks/web/usePage';
import { Persistent } from '/@/utils/cache/persistent';

import { PageEnum } from '/@/enums/pageEnum';
import { PAGE_NOT_FOUND_ROUTE, REDIRECT_ROUTE } from '/@/router/routes/basic';
import { PAGE_NOT_FOUND_NAME, REDIRECT_NAME } from '/@/router/constant';
import { getRawRoute } from '/@/utils';
import { MULTIPLE_TABS_KEY } from '/@/enums/cacheEnum';

import projectSetting from '/@/settings/projectSetting';
import { useUserStore } from '/@/store/modules/user';
import type { LocationQueryRaw, RouteParamsRaw } from 'vue-router';

export interface MultipleTabState {
  cacheTabList: Set<string>;
  tabList: RouteLocationNormalized[];
  lastDragEndIndex: number;
  redirectPageParam: null | redirectPageParamType;
}

interface redirectPageParamType {
  redirect_type: string;
  name?: string;
  path?: string;
  query: LocationQueryRaw;
  params?: RouteParamsRaw;
}

function handleGotoPage(router: Router, path?) {
  const go = useGo(router);
  go(path || unref(router.currentRoute).path, true);
}
const getToTarget = (tabItem: RouteLocationNormalized) => {
  const { params, path, query } = tabItem;
  return {
    params: params || {},
    path,
    query: query || {},
  };
};

/**
 * 2024-06-05
 * liaozhiyang
 * 닫힌 탭에 현재 페이지가 포함되어 있는지 여부
 */
const closeTabContainCurrentRoute = (router, pathList) => {
  const { currentRoute } = router;
  const getCurrentTab = () => {
    const route = unref(currentRoute);
    const tabStore = useMultipleTabStore();
    return tabStore.getTabList.find((item) => item.path === route.path)!;
  };
  const currentTab = getCurrentTab();
  if (currentTab) {
    return pathList.includes(currentTab.path);
  }
  return false;
};

const cacheTab = projectSetting.multiTabsSetting.cache;

export const useMultipleTabStore = defineStore({
  id: 'app-multiple-tab',
  state: (): MultipleTabState => ({
    // Tabs that need to be cached
    cacheTabList: new Set(),
    // multiple tab list
    tabList: (cacheTab ? Persistent.getLocal(MULTIPLE_TABS_KEY) || [] : []).filter(
      (tab: any) => !String(tab?.path || tab?.fullPath || '').startsWith('/ecommerce')
    ),
    // Index of the last moved tab
    lastDragEndIndex: 0,
    // 리디렉션 시 저장된 라우팅 매개변수
    redirectPageParam: null,
  }),
  getters: {
    getTabList(): RouteLocationNormalized[] {
      return this.tabList;
    },
    getCachedTabList(): string[] {
      return Array.from(this.cacheTabList);
    },
    getLastDragEndIndex(): number {
      return this.lastDragEndIndex;
    },
  },
  actions: {
    /**
     * Update the cache according to the currently opened tabs
     */
    async updateCacheTab() {
      const cacheMap: Set<string> = new Set();

      for (const tab of this.tabList) {
        const item = getRawRoute(tab);
        // Ignore the cache
        const needCache = !item.meta?.ignoreKeepAlive;
        if (!needCache) {
          continue;
        }
        const name = item.name as string;
        cacheMap.add(name);
      }
      this.cacheTabList = cacheMap;
    },

    /**
     * Refresh tabs
     */
    async refreshPage(router: Router) {
      const { currentRoute } = router;
      const route = unref(currentRoute);
      const name = route.name;

      const findTab = this.getCachedTabList.find((item) => item === name);
      if (findTab) {
        this.cacheTabList.delete(findTab);
      }
      const redo = useRedo(router);
      await redo();
    },
    clearCacheTabs(): void {
      this.cacheTabList = new Set();
    },
    resetState(): void {
      this.tabList = [];
      this.clearCacheTabs();
    },
    goToPage(router: Router) {
      const go = useGo(router);
      const len = this.tabList.length;
      const { path } = unref(router.currentRoute);

      let toPath: PageEnum | string = PageEnum.BASE_HOME;

      if (len > 0) {
        const page = this.tabList[len - 1];
        const p = page.fullPath || page.path;
        if (p) {
          toPath = p;
        }
      }
      // Jump to the current page and report an error
      path !== toPath && go(toPath as PageEnum, true);
    },

    async addTab(route: RouteLocationNormalized) {
      const { path, name, fullPath, params, query, meta } = getRawRoute(route);
      // 404  The page does not need to add a tab
      if (
        path === PageEnum.ERROR_PAGE ||
        path === PageEnum.BASE_LOGIN ||
        !name ||
        [REDIRECT_ROUTE.name, PAGE_NOT_FOUND_ROUTE.name, REDIRECT_NAME, PAGE_NOT_FOUND_NAME, `${PAGE_NOT_FOUND_NAME}Page`].includes(name as string)
      ) {
        return;
      }

      let updateIndex = -1;
      // Existing pages, do not add tabs repeatedly
      const tabHasExits = this.tabList.some((tab, index) => {
        updateIndex = index;
        return (tab.fullPath || tab.path) === (fullPath || path);
      });

      // If the tab already exists, perform the update operation
      if (tabHasExits) {
        const curTab = toRaw(this.tabList)[updateIndex];
        if (!curTab) {
          return;
        }
        curTab.params = params || curTab.params;
        curTab.query = query || curTab.query;
        curTab.fullPath = fullPath || curTab.fullPath;
        this.tabList.splice(updateIndex, 1, curTab);
      } else {
        // Add tab
        // 동적 경로 개구부 수를 가져옵니다. 0을 초과하면 개구부 수를 제어해야 함을 의미합니다.
        const dynamicLevel = meta?.dynamicLevel ?? -1;
        if (dynamicLevel > 0) {
          // 동적 라우팅 수준이 0보다 큰 경우 경로 열기 수를 제한해야 합니다.
          // 먼저, 실제 경로를 획득하고 구성 방법을 사용하여 계산 오버헤드를 줄입니다.
          // const realName: string = path.match(/(\S*)\//)![1];
          const realPath = meta?.realPath ?? '';
          // 열린 동적 경로의 수를 얻어 특정 값보다 큰지 확인
          if (this.tabList.filter((e) => e.meta?.realPath ?? '' === realPath).length >= dynamicLevel) {
            // 먼저 닫아라
            const index = this.tabList.findIndex((item) => item.meta.realPath === realPath);
            index !== -1 && this.tabList.splice(index, 1);
          }
        }
        this.tabList.push(route);
      }
      this.updateCacheTab();
      cacheTab && Persistent.setLocal(MULTIPLE_TABS_KEY, this.tabList);
    },

    async closeTab(tab: RouteLocationNormalized, router: Router) {
      const close = (route: RouteLocationNormalized) => {
        const { fullPath, meta: { affix } = {} } = route;
        if (affix) {
          return;
        }
        const index = this.tabList.findIndex((item) => item.fullPath === fullPath);
        index !== -1 && this.tabList.splice(index, 1);
      };

      const { currentRoute, replace } = router;

      const { path } = unref(currentRoute);
      if (path !== tab.path) {
        // Closed is not the activation tab
        close(tab);
        this.updateCacheTab();
        return;
      }

      // Closed is activated atb
      let toTarget: RouteLocationRaw = {};

      const index = this.tabList.findIndex((item) => item.path === path);

      // If the current is the leftmost tab
      if (index === 0) {
        // There is only one tab, then jump to the homepage, otherwise jump to the right tab
        if (this.tabList.length === 1) {
          const userStore = useUserStore();
          toTarget = userStore.getUserInfo.homePath || PageEnum.BASE_HOME;
        } else {
          //  Jump to the right tab
          const page = this.tabList[index + 1];
          toTarget = getToTarget(page);
        }
      } else {
        // Close the current tab
        const page = this.tabList[index - 1];
        toTarget = getToTarget(page);
      }
      close(currentRoute.value);
      await replace(toTarget);
    },

    // Close according to key
    async closeTabByKey(key: string, router: Router) {
      const index = this.tabList.findIndex((item) => (item.fullPath || item.path) === key);
      if (index !== -1) {
        await this.closeTab(this.tabList[index], router);
        const { currentRoute, replace } = router;
        // 현재 경로가 tabList에 있는지 확인하십시오.
        const isActivated = this.tabList.findIndex((item) => {
          return item.fullPath === currentRoute.value.fullPath;
        });
        // 현재 경로가 TabList에 없으면 다른 경로로 전환해 보세요.
        if (isActivated === -1) {
          let pageIndex;
          if (index > 0) {
            pageIndex = index - 1;
          } else if (index < this.tabList.length - 1) {
            pageIndex = index + 1;
          } else {
            pageIndex = -1;
          }
          if (pageIndex >= 0) {
            const page = this.tabList[index - 1];
            const toTarget = getToTarget(page);
            await replace(toTarget);
          }
        }
      }
    },

    // Sort the tabs
    async sortTabs(oldIndex: number, newIndex: number) {
      const currentTab = this.tabList[oldIndex];
      this.tabList.splice(oldIndex, 1);
      this.tabList.splice(newIndex, 0, currentTab);
      this.lastDragEndIndex = this.lastDragEndIndex + 1;
    },

    // Close the tab on the right and jump
    async closeLeftTabs(route: RouteLocationNormalized, router: Router) {
      const index = this.tabList.findIndex((item) => item.path === route.path);
      let isCloseCurrentTab = false;
      if (index > 0) {
        const leftTabs = this.tabList.slice(0, index);
        const pathList: string[] = [];
        for (const item of leftTabs) {
          const affix = item?.meta?.affix ?? false;
          if (!affix) {
            pathList.push(item.fullPath);
          }
        }
        isCloseCurrentTab = closeTabContainCurrentRoute(router, pathList);
        this.bulkCloseTabs(pathList);
      }
      this.updateCacheTab();
      if (isCloseCurrentTab) {
        handleGotoPage(router, route.path);
      } else {
        handleGotoPage(router);
      }
    },

    // Close the tab on the left and jump
    async closeRightTabs(route: RouteLocationNormalized, router: Router) {
      const index = this.tabList.findIndex((item) => item.fullPath === route.fullPath);
      let isCloseCurrentTab = false;
      if (index >= 0 && index < this.tabList.length - 1) {
        const rightTabs = this.tabList.slice(index + 1, this.tabList.length);

        const pathList: string[] = [];
        for (const item of rightTabs) {
          const affix = item?.meta?.affix ?? false;
          if (!affix) {
            pathList.push(item.fullPath);
          }
        }
        isCloseCurrentTab = closeTabContainCurrentRoute(router, pathList);
        this.bulkCloseTabs(pathList);
      }
      this.updateCacheTab();
      if (isCloseCurrentTab) {
        handleGotoPage(router, route.path);
      } else {
        handleGotoPage(router);
      }
    },

    async closeAllTab(router: Router) {
      this.tabList = this.tabList.filter((item) => item?.meta?.affix ?? false);
      this.clearCacheTabs();
      this.goToPage(router);
    },
    

    /**
     * Close other tabs
     */
    async closeOtherTabs(route: RouteLocationNormalized, router: Router) {
      const closePathList = this.tabList.map((item) => item.fullPath);
      let isCloseCurrentTab = false;
      const pathList: string[] = [];

      for (const path of closePathList) {
        if (path !== route.fullPath) {
          const closeItem = this.tabList.find((item) => item.path === path);
          if (!closeItem) {
            continue;
          }
          const affix = closeItem?.meta?.affix ?? false;
          if (!affix) {
            pathList.push(closeItem.fullPath);
          }
        }
      }
      isCloseCurrentTab = closeTabContainCurrentRoute(router, pathList);
      this.bulkCloseTabs(pathList);
      this.updateCacheTab();
      if (isCloseCurrentTab) {
        handleGotoPage(router, route.path);
      } else {
        handleGotoPage(router);
      }
    },

    /**
     * Close tabs in bulk
     */
    async bulkCloseTabs(pathList: string[]) {
      this.tabList = this.tabList.filter((item) => !pathList.includes(item.fullPath));
    },

    /**
     * Set tab's title
     */
    async setTabTitle(title: string, route: RouteLocationNormalized) {
      const findTab = this.getTabList.find((item) => item === route);
      if (findTab) {
        findTab.meta.title = title;
        await this.updateCacheTab();
      }
    },
    /**
     * replace tab's path
     * **/
    async updateTabPath(fullPath: string, route: RouteLocationNormalized) {
      const findTab = this.getTabList.find((item) => item === route);
      if (findTab) {
        findTab.fullPath = fullPath;
        findTab.path = fullPath;
        await this.updateCacheTab();
      }
    },
    setRedirectPageParam(data) {
      this.redirectPageParam = data;
    },
  },
});

// Need to be used outside the setup
export function useMultipleTabWithOutStore() {
  return useMultipleTabStore(store);
}
