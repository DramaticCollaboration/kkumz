import type { Router, RouteRecordRaw } from 'vue-router';

import { usePermissionStoreWithOut } from '/@/store/modules/permission';

import { PageEnum } from '/@/enums/pageEnum';
import { useUserStoreWithOut } from '/@/store/modules/user';

import { PAGE_NOT_FOUND_ROUTE } from '/@/router/routes/basic';
import { PAGE_NOT_FOUND_NAME } from '/@/router/constant';

import { RootRoute } from '/@/router/routes';

import { isOAuth2AppEnv } from '/@/views/sys/login/useLogin';
import { setAuthCache } from "/@/utils/auth";

const LOGIN_PATH = PageEnum.BASE_LOGIN;
//auth2 로그인 경로
const OAUTH2_LOGIN_PAGE_PATH = PageEnum.OAUTH2_LOGIN_PAGE_PATH;

//로그인이 필요 없는 라우팅 공유
const SYS_FILES_PATH = PageEnum.SYS_FILES_PATH;

// 이메일에 있는 점프 주소가 이 경로와 일치합니다. 토큰을 지참하고 로그인 없이 바로 신청 페이지로 이동하세요.
const TOKEN_LOGIN = PageEnum.TOKEN_LOGIN;

const ROOT_PATH = RootRoute.path;

const whitePathList: PageEnum[] = [LOGIN_PATH, OAUTH2_LOGIN_PAGE_PATH,SYS_FILES_PATH, TOKEN_LOGIN ];

export function createPermissionGuard(router: Router) {
  const userStore = useUserStoreWithOut();
  const permissionStore = usePermissionStoreWithOut();
  router.beforeEach(async (to, from, next) => {
    if (
      from.path === ROOT_PATH &&
      to.path === PageEnum.BASE_HOME &&
      userStore.getUserInfo.homePath &&
      userStore.getUserInfo.homePath !== PageEnum.BASE_HOME
    ) {
      next(userStore.getUserInfo.homePath);
      return;
    }

    const token = userStore.getToken;

    // Whitelist can be directly entered
    if (whitePathList.includes(to.path as PageEnum)) {
      if (to.path === LOGIN_PATH && token) {
        const isSessionTimeout = userStore.getSessionTimeout;
        
        //TODO vben은 기본 방식으로 작성되었습니다. 현재로서는 목적을 모르겠습니다. 문제가 있으면 먼저 주석을 달아주세요.
        //await userStore.afterLoginAction();
        
        try {
          if (!isSessionTimeout) {
            next((to.query?.redirect as string) || '/');
            return;
          }
        } catch (e) {
          console.warn('[PermissionGuard] 리디렉션 처리 예외:', e);
        }
      } else if (to.path === LOGIN_PATH && isOAuth2AppEnv() && !token) {
        //로그아웃하고 이 논리를 입력하세요.
        //입력한 페이지가 로그인 페이지이고 현재 환경이 OAuth2app이고 토큰이 비어 있으면 OAuth2 로그인 페이지로 들어갑니다.
        next({ path: OAUTH2_LOGIN_PAGE_PATH });
        return;
      }
      next();
      return;
    }

    // token does not exist
    if (!token) {
      // You can access without permission. You need to set the routing meta.ignoreAuth to true
      if (to.meta.ignoreAuth) {
        next();
        return;
      }

      let path = LOGIN_PATH;
      if (whitePathList.includes(to.path as PageEnum)) {
        // 로그인 프리 화이트리스트에 입력한 페이지가 로그인 페이지이고, 현재 OAuth2app 환경이라면 OAuth2 로그인 페이지를 입력합니다.
        if (to.path === LOGIN_PATH && isOAuth2AppEnv()) {
          next({ path: OAUTH2_LOGIN_PAGE_PATH });
        } else {
          //로그인 없이 화이트리스트에 직접 입력
          next();
        }
      } else {
        // 현재 OAuth2APP 환경에 있다면 OAuth2 로그인 페이지로 이동하고, 그렇지 않으면 로그인 페이지로 이동하세요.
        path = isOAuth2AppEnv() ? OAUTH2_LOGIN_PAGE_PATH : LOGIN_PATH;
      }
      // redirect login page
      const redirectData: { path: string; replace: boolean; query?: Recordable<string> } = {
        path: path,
        replace: true,
      };

      if (to.fullPath) {
        console.log("to.fullPath 1",to.fullPath)
        console.log("to.path 2",to.path)
        
        let getFullPath = to.fullPath;
        if(getFullPath=='/' || getFullPath=='/500' || getFullPath=='/400' || getFullPath=='/login?redirect=/' || getFullPath=='/login?redirect=/login?redirect=/'){
          return;
        }
        
        redirectData.query = {
          ...redirectData.query,
          redirect: to.fullPath,

        };
      }
      next(redirectData);
      return;
    }

    
    const isNotFound = [PAGE_NOT_FOUND_ROUTE.name, PAGE_NOT_FOUND_NAME, `${PAGE_NOT_FOUND_NAME}Page`].includes(to.name as string);
    // Jump to the 404 page after processing the login
    if (from.path === LOGIN_PATH && isNotFound && to.fullPath !== (userStore.getUserInfo.homePath || PageEnum.BASE_HOME)) {
      next(userStore.getUserInfo.homePath || PageEnum.BASE_HOME);
      return;
    }

    // // get userinfo while last fetch time is empty
    // if (userStore.getLastUpdateTime === 0) {
    //   try {
    //     console.log("--LastUpdateTime---getUserInfoAction-----")
    //     await userStore.getUserInfoAction();
    //   } catch (err) {
    //     console.info(err);
    //     next();
    //   }
    // }
    if (userStore.getLastUpdateTime === 0) {
      userStore.setAllDictItemsByLocal();
    }
    if (permissionStore.getIsDynamicAddedRoute) {
      const isRouteNotFound = [PAGE_NOT_FOUND_ROUTE.name, PAGE_NOT_FOUND_NAME, `${PAGE_NOT_FOUND_NAME}Page`].includes(to.name as string);
      if (isRouteNotFound && token) {
        const homePath = userStore.getUserInfo.homePath || PageEnum.BASE_HOME;
        if (to.path !== homePath) {
          next({ path: homePath, replace: true });
          return;
        }
      }
      next();
      return;
    }

    // 배경 메뉴 라우팅 구축
    try {
      const routes = await permissionStore.buildRoutesAction();
      routes.forEach((route) => {
        router.addRoute(route as unknown as RouteRecordRaw);
      });
    } catch (e) {
      userStore.setToken(undefined);
      await userStore.logout(true);
      return;
    }

    router.addRoute(PAGE_NOT_FOUND_ROUTE as unknown as RouteRecordRaw);
    permissionStore.setDynamicAddedRoute(true);

    if (isNotFound) {
      // 경로를 동적으로 추가한 후, 대상 경로가 실제로 존재하는지 확인
      const resolved = router.resolve(to.fullPath);
      const isStillNotFound = !resolved || [PAGE_NOT_FOUND_ROUTE.name, PAGE_NOT_FOUND_NAME, `${PAGE_NOT_FOUND_NAME}Page`].includes(resolved.name as string);
      if (isStillNotFound && token) {
        // 권한이 없거나 존재하지 않는 경로인 경우 사용자의 홈페이지로 안전하게 리디렉션
        const homePath = userStore.getUserInfo.homePath || PageEnum.BASE_HOME;
        next({ path: homePath, replace: true });
        return;
      }
      // 경로를 동적으로 추가한 후에는 fullPath로 리디렉션되어야 하며, 그렇지 않으면 404 페이지 콘텐츠가 로드됩니다.
      next({ path: to.fullPath, replace: true, query: to.query });
    } else {
      const redirectPath = (from.query.redirect || to.path) as string;
      const redirect = decodeURIComponent(redirectPath);
      const resolved = router.resolve(redirect);
      const isRedirectNotFound = !resolved || [PAGE_NOT_FOUND_ROUTE.name, PAGE_NOT_FOUND_NAME, `${PAGE_NOT_FOUND_NAME}Page`].includes(resolved.name as string);
      if (isRedirectNotFound && token) {
        const homePath = userStore.getUserInfo.homePath || PageEnum.BASE_HOME;
        next({ path: homePath, replace: true });
        return;
      }
      const nextData = to.path === redirect ? { ...to, replace: true } : { path: redirect };
      next(nextData);
    }
  });
}
