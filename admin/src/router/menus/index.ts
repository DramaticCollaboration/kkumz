import type { Menu } from '/@/router/types';
import type { RouteRecordNormalized } from 'vue-router';

import { useAppStoreWithOut } from '/@/store/modules/app';
import { usePermissionStore } from '/@/store/modules/permission';
import { getAllParentPath, flatSingleChildMenus } from '/@/router/helper/menuHelper';
import { filter } from '/@/utils/helper/treeHelper';
import { isUrl } from '/@/utils/is';
import { router } from '/@/router';
import { PermissionModeEnum } from '/@/enums/appEnum';
import { pathToRegexp } from 'path-to-regexp';

// ===========================
// ==========Helper===========
// ===========================

const getPermissionMode = () => {
  const appStore = useAppStoreWithOut();
  return appStore.getProjectConfig.permissionMode;
};

const isRoleMode = () => {
  return getPermissionMode() === PermissionModeEnum.ROLE;
};

async function getAsyncMenus(): Promise<Menu[]> {
  const permissionStore = usePermissionStore();
  return permissionStore.getBackMenuList.filter((item) => !item.meta?.hideMenu && !item.hideMenu);
}

export const getMenus = async (): Promise<Menu[]> => {
  const menus = await getAsyncMenus();
  const flattedMenus = flatSingleChildMenus(menus);
  if (isRoleMode()) {
    const routes = router.getRoutes();
    return filter(flattedMenus, basicFilter(routes));
  }
  return flattedMenus;
};

export async function getCurrentParentPath(currentPath: string) {
  const menus = await getAsyncMenus();
  const allParentPath = await getAllParentPath(menus, currentPath);
  return allParentPath?.[0];
}

// Get the level 1 menu, delete children
export async function getShallowMenus(): Promise<Menu[]> {
  const menus = await getAsyncMenus();
  const shallowMenuList = menus.map((item) => ({ ...item, children: undefined }));
  if (isRoleMode()) {
    const routes = router.getRoutes();
    return shallowMenuList.filter(basicFilter(routes));
  }
  return shallowMenuList;
}

// Get the children of the menu
export async function getChildrenMenus(parentPath: string) {
  const menus = await getMenus();
  const parent = menus.find((item) => item.path === parentPath);
  if (!parent || !parent.children || !!parent?.meta?.hideChildrenInMenu) {
    return [] as Menu[];
  }
  if (isRoleMode()) {
    const routes = router.getRoutes();
    return filter(parent.children, basicFilter(routes));
  }
  return parent.children;
}

function basicFilter(routes: RouteRecordNormalized[]) {
  return (menu: Menu) => {
    const matchRoute = routes.find((route) => {
      if (isUrl(menu.path)) return true;

      if (route.meta?.carryParam) {
        return pathToRegexp(route.path).test(menu.path);
      }
      const isSame = route.path === menu.path;
      if (!isSame) return false;

      if (route.meta?.ignoreAuth) return true;

      return isSame || pathToRegexp(route.path).test(menu.path);
    });

    if (!matchRoute) return false;
    menu.icon = (menu.icon || matchRoute.meta.icon) as string;
    menu.meta = matchRoute.meta;
    return true;
  };
}
