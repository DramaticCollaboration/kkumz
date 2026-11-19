import type { Router } from 'vue-router';
import { configureDynamicParamsMenu } from '../helper/menuHelper';
import { Menu } from '../types';
import { PermissionModeEnum } from '/@/enums/appEnum';
import { useAppStore } from '/@/store/modules/app';

import { usePermissionStore } from '/@/store/modules/permission';

export function createParamMenuGuard(router: Router) {
  router.beforeEach(async (to, _) => {
    const permissionStore = usePermissionStore();
    // filter no name route
    if (!to.name) {
      return true;
    }

    // menu has been built.
    if (!permissionStore.getIsDynamicAddedRoute) {
      return true;
    }

    let menus: Menu[] = [];
    if (isBackMode()) {
      menus = permissionStore.getBackMenuList;
    } else if (isRouteMappingMode()) {
      menus = permissionStore.getFrontMenuList;
    }
    menus.forEach((item) => configureDynamicParamsMenu(item, to.params));

    return true;
  });
}

const getPermissionMode = () => {
  const appStore = useAppStore();
  return appStore.getProjectConfig.permissionMode;
};

const isBackMode = () => {
  return getPermissionMode() === PermissionModeEnum.BACK;
};

const isRouteMappingMode = () => {
  return getPermissionMode() === PermissionModeEnum.ROUTE_MAPPING;
};
