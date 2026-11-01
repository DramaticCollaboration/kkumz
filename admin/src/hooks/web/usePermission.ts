import type { RouteRecordRaw } from 'vue-router';

import { useAppStore } from '/@/store/modules/app';
import { usePermissionStore } from '/@/store/modules/permission';
import { useUserStore } from '/@/store/modules/user';

import { useTabs } from './useTabs';

import { router, resetRouter } from '/@/router';
// import { RootRoute } from '/@/router/routes';

import projectSetting from '/@/settings/projectSetting';
import { PermissionModeEnum } from '/@/enums/appEnum';
import { RoleEnum } from '/@/enums/roleEnum';

import { intersection } from 'lodash-es';
import { isArray } from '/@/utils/is';
import { useMultipleTabStore } from '/@/store/modules/multipleTab';

// User permissions related operations
export function usePermission() {
  const userStore = useUserStore();
  const appStore = useAppStore();
  const permissionStore = usePermissionStore();
  //프로세스 노드 Form 권한을 동적으로 로드
  let formData: any = {};
  function initBpmFormData(_bpmFormData) {
    formData = _bpmFormData;
  }
  const { closeAll } = useTabs(router);

  //==================================작업흐름 허가 판단-시작=========================================
  function hasBpmPermission(code, type) {
    // 비활성화 유형=2
    // 쇼 유형=1
    let codeList: string[] = [];
    let permissionList = formData.permissionList;
    if (permissionList && permissionList.length > 0) {
      for (let item of permissionList) {
        if (item.type == type) {
          codeList.push(item.action);
        }
      }
    }
    return codeList.indexOf(code) >= 0;
  }
  //==================================워크플로우 허가 판단 종료=========================================

  /**
   * Change permission mode
   */
  async function togglePermissionMode() {
    appStore.setProjectConfig({
      permissionMode: projectSetting.permissionMode === PermissionModeEnum.BACK ? PermissionModeEnum.ROUTE_MAPPING : PermissionModeEnum.BACK,
    });
    location.reload();
  }

  /**
   * Reset and regain authority resource information
   * @param id
   */
  async function resume() {
    const tabStore = useMultipleTabStore();
    tabStore.clearCacheTabs();
    resetRouter();
    const routes = await permissionStore.buildRoutesAction();
    routes.forEach((route) => {
      router.addRoute(route as unknown as RouteRecordRaw);
    });
    permissionStore.setLastBuildMenuTime();
    closeAll();
  }

  /**
   * 권한이 존재하는지 확인
   */
  function hasPermission(value?: RoleEnum | RoleEnum[] | string | string[], def = true): boolean {
    // Visible by default
    if (!value) {
      return def;
    }

    const permMode = projectSetting.permissionMode;

    if ([PermissionModeEnum.ROUTE_MAPPING, PermissionModeEnum.ROLE].includes(permMode)) {
      if (!isArray(value)) {
        return userStore.getRoleList?.includes(value as RoleEnum);
      }
      return (intersection(value, userStore.getRoleList) as RoleEnum[]).length > 0;
    }

    if (PermissionModeEnum.BACK === permMode) {
      const allCodeList = permissionStore.getPermCodeList as string[];
      if (!isArray(value) && allCodeList && allCodeList.length > 0) {
        //=============================작업흐름 허가 판단-표시-시작==============================================
        if (formData) {
          let code = value as string;
          if (hasBpmPermission(code, '1') === true) {
            return true;
          }
        }
        //=============================작업흐름 허가 판단-표시-종료==============================================
        return allCodeList.includes(value);
      }
      return (intersection(value, allCodeList) as string[]).length > 0;
    }
    return true;
  }
  /**
   * 구성요소 비활성화 여부
   */
  function isDisabledAuth(value?: RoleEnum | RoleEnum[] | string | string[], def = true): boolean {
    //=============================워크플로 허가 판단-비활성화-시작==============================================
    if (formData) {
      let code = value as string;
      if (hasBpmPermission(code, '2') === true) {
        return true;
      }
      if (isCodingButNoConfig(code) == true) {
        return false;
      }
    }
    //=============================작업흐름 허가 판단-비활성화-종료==============================================
    return !hasPermission(value);
  }

  /**
   * Change roles
   * @param roles
   */
  async function changeRole(roles: RoleEnum | RoleEnum[]): Promise<void> {
    if (projectSetting.permissionMode !== PermissionModeEnum.ROUTE_MAPPING) {
      throw new Error('Please switch PermissionModeEnum to ROUTE_MAPPING mode in the configuration to operate!');
    }

    if (!isArray(roles)) {
      roles = [roles];
    }
    userStore.setRoleList(roles);
    await resume();
  }

  /**
   * refresh menu data
   */
  async function refreshMenu() {
    resume();
  }

  /**
   * 코드에 로직이 작성되었지만 구성 권한이 없는지 확인합니다.
   */
  function isCodingButNoConfig(code) {
    let all = permissionStore.allAuthList;
    if (all && all instanceof Array) {
      let temp = all.filter((item) => item.action == code);
      if (temp && temp.length > 0) {
        if (temp[0].status == '0') {
          return true;
        }
      } else {
        return true;
      }
    }
    return false;
  }

  return { changeRole, hasPermission, togglePermissionMode, refreshMenu, isDisabledAuth, initBpmFormData };
}
