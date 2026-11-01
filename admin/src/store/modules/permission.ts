/**
 * @fileoverview RBAC 권한 기반 동적 라우팅 및 메뉴 필터링 스토어 (usePermissionStore)
 * @description
 *  - 백엔드에서 반환된 사용자 권한 코드(Roles, Permissions)에 따라 접근 가능한 라우트 트리 동적 구축
 *  - 403 인가 실패 화면 분기 및 사이드바 내비게이션 메뉴 렌더링 소스 제공
 */
import type { AppRouteRecordRaw, Menu } from '/@/router/types';

import { defineStore } from 'pinia';
import { store } from '/@/store';
import { useUserStore } from './user';
import { useAppStoreWithOut } from './app';
import { transformObjToRoute, flatMultiLevelRoutes, addSlashToRouteComponent } from '/@/router/helper/routeHelper';
import { transformRouteToMenu } from '/@/router/helper/menuHelper';

import projectSetting from '/@/settings/projectSetting';

import { PermissionModeEnum } from '/@/enums/appEnum';

import { ERROR_LOG_ROUTE, PAGE_NOT_FOUND_ROUTE } from '/@/router/routes/basic';

import { filter } from '/@/utils/helper/treeHelper';

import { getBackMenuAndPerms } from '/@/api/sys/menu';

import { PageEnum } from '/@/enums/pageEnum';

// 시스템 권한
interface AuthItem {
  // 메뉴 권한 인코딩(예:："sys:schedule:list,sys:schedule:info", 여러 개의 쉼표로 구분됨
  action: string;
  // 권한 정책 1은 2가 비활성화되었음을 표시합니다.
  type: string | number;
  // 허가 상태(0 유효하지 않음 1 유효함)
  status: string | number;
  // 권한 이름
  describe?: string;
  isAuth?: boolean;
}

interface PermissionState {
  // Permission code list
  permCodeList: string[] | number[];
  // Whether the route has been dynamically added
  isDynamicAddedRoute: boolean;
  // To trigger a menu update
  lastBuildMenuTime: number;
  // Backstage menu list
  backMenuList: Menu[];
  frontMenuList: Menu[];
  // 사용자가 가지고 있는 권한
  authList: AuthItem[];
  // 모든 권한 구성
  allAuthList: AuthItem[];
  // 시스템 안전 모드
  sysSafeMode: boolean;
  // 온라인 하위 테이블 버튼 권한
  onlineSubTableAuthMap: object;
}
export const usePermissionStore = defineStore({
  id: 'app-permission',
  state: (): PermissionState => ({
    permCodeList: [],
    // Whether the route has been dynamically added
    isDynamicAddedRoute: false,
    // To trigger a menu update
    lastBuildMenuTime: 0,
    // Backstage menu list
    backMenuList: [],
    // menu List
    frontMenuList: [],
    authList: [],
    allAuthList: [],
    sysSafeMode: false,
    onlineSubTableAuthMap: {},
  }),
  getters: {
    getPermCodeList(): string[] | number[] {
      return this.permCodeList;
    },
    getBackMenuList(): Menu[] {
      return this.backMenuList;
    },
    getFrontMenuList(): Menu[] {
      return this.frontMenuList;
    },
    getLastBuildMenuTime(): number {
      return this.lastBuildMenuTime;
    },
    getIsDynamicAddedRoute(): boolean {
      return this.isDynamicAddedRoute;
    },

    getOnlineSubTableAuth: (state) => {
      return (code) => state.onlineSubTableAuthMap[code];
    },
  },
  actions: {
    setPermCodeList(codeList: string[]) {
      this.permCodeList = codeList;
    },

    setBackMenuList(list: Menu[]) {
      this.backMenuList = list;
      list?.length > 0 && this.setLastBuildMenuTime();
    },

    setFrontMenuList(list: Menu[]) {
      this.frontMenuList = list;
    },

    setLastBuildMenuTime() {
      this.lastBuildMenuTime = new Date().getTime();
    },

    setDynamicAddedRoute(added: boolean) {
      this.isDynamicAddedRoute = added;
    },
    resetState(): void {
      this.isDynamicAddedRoute = false;
      this.permCodeList = [];
      this.backMenuList = [];
      this.lastBuildMenuTime = 0;
    },
    async changePermissionCode() {
      const systemPermission = await getBackMenuAndPerms();
      if (!systemPermission) {
        throw new Error('인증 및 메뉴 권한 정보를 백엔드로부터 가져오지 못했습니다.');
      }
      const codeList = systemPermission.codeList || [];
      this.setPermCodeList(codeList);
      this.setAuthData(systemPermission);

      // 메뉴 라우팅
      const routeList = systemPermission.menu;
      if (!routeList || routeList.length === 0) {
        throw new Error('백엔드로부터 수신된 메뉴 목록이 비어있습니다.');
      }
      return routeList;
    },
    async buildRoutesAction(): Promise<AppRouteRecordRaw[]> {
      const userStore = useUserStore();
      const appStore = useAppStoreWithOut();

      let routes: AppRouteRecordRaw[] = [];
      const { permissionMode = projectSetting.permissionMode } = appStore.getProjectConfig;

      const routeRemoveIgnoreFilter = (route: AppRouteRecordRaw) => {
        const { meta } = route;
        const { ignoreRoute } = meta || {};
        return !ignoreRoute;
      };

      /**
       * @description 설정된 홈페이지 경로에 따라 경로의 첨부태그 수정（고정된 홈페이지）
       * */
      const patchHomeAffix = (routes: AppRouteRecordRaw[]) => {
        if (!routes || routes.length === 0) return;
        let homePath: string = userStore.getUserInfo.homePath || PageEnum.BASE_HOME;
        function patcher(routes: AppRouteRecordRaw[], parentPath = '') {
          if (parentPath) parentPath = parentPath + '/';
          routes.forEach((route: AppRouteRecordRaw) => {
            const { path, children, redirect } = route;
            const currentPath = path.startsWith('/') ? path : parentPath + path;
            if (currentPath === homePath) {
              if (redirect) {
                homePath = route.redirect! as string;
              } else {
                route.meta = Object.assign({}, route.meta, { affix: true });
                throw new Error('end');
              }
            }
            children && children.length > 0 && patcher(children, currentPath);
          });
        }
        try {
          patcher(routes);
        } catch (e) {
          // 루프를 처리하고 종료했습니다.
        }
        return;
      };

      if (permissionMode !== PermissionModeEnum.BACK) {
        throw new Error(`Unsupported permissionMode: ${permissionMode}. Only PermissionModeEnum.BACK is allowed.`);
      }

      console.log(' --- 백그라운드 라우팅 메뉴 구축 --- ');

      // 백그라운드에서 권한 코드 조회 (실패 시 Fail-Fast 예외 전파)
      let routeList: AppRouteRecordRaw[] = await this.changePermissionCode();
      routeList = addSlashToRouteComponent(routeList);
      // 컴포넌트를 동적으로 도입
      routeList = transformObjToRoute(routeList);

      // 백그라운드 라우팅 메뉴 구축
      const backMenuList = transformRouteToMenu(routeList);
      this.setBackMenuList(backMenuList);

      // Meta.ignoreRoute 항목 제거
      routeList = filter(routeList, routeRemoveIgnoreFilter);
      routeList = routeList.filter(routeRemoveIgnoreFilter);

      routeList = flatMultiLevelRoutes(routeList);
      routes = [PAGE_NOT_FOUND_ROUTE, ...routeList];

      routes.push(ERROR_LOG_ROUTE);
      patchHomeAffix(routes);
      return routes;
    },
    setAuthData(systemPermission) {
      if (!systemPermission) return;
      this.authList = systemPermission.auth || [];
      this.allAuthList = systemPermission.allAuth || [];
      this.sysSafeMode = systemPermission.sysSafeMode;
    },
    setAuthList(authList: AuthItem[]) {
      this.authList = authList;
    },
    setAllAuthList(authList: AuthItem[]) {
      this.allAuthList = authList;
    },

    setOnlineSubTableAuth(code, hideBtnList) {
      this.onlineSubTableAuthMap[code] = hideBtnList;
    },
  },
});

// 설정 외부에서 사용해야 함
export function usePermissionStoreWithOut() {
  return usePermissionStore(store);
}
