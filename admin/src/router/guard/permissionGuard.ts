import type { Router, RouteRecordRaw } from 'vue-router';

import { usePermissionStore } from '/@/store/modules/permission';

import { PageEnum } from '/@/enums/pageEnum';
import { useUserStore } from '/@/store/modules/user';

import { PAGE_NOT_FOUND_ROUTE } from '/@/router/routes/basic';

import { RootRoute } from '/@/router/routes';

import { isOAuth2AppEnv } from '/@/views/sys/login/useLogin';
import { OAUTH2_THIRD_LOGIN_TENANT_ID } from '/@/enums/cacheEnum';
import { setAuthCache } from '/@/utils/auth';

import { resolveInternalRedirect } from './safeRedirect';

const LOGIN_PATH = PageEnum.BASE_LOGIN;
//auth2 로그인 경로
const OAUTH2_LOGIN_PAGE_PATH = PageEnum.OAUTH2_LOGIN_PAGE_PATH;

//로그인이 필요 없는 라우팅 공유
const SYS_FILES_PATH = PageEnum.SYS_FILES_PATH;

// 이메일에 있는 점프 주소가 이 경로와 일치합니다. 토큰을 지참하고 로그인 없이 바로 신청 페이지로 이동하세요.
const TOKEN_LOGIN = PageEnum.TOKEN_LOGIN;

const ROOT_PATH = RootRoute.path;

//update-begin---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3은 auth2 로그인을 지원하지 않습니다------------
//update-begin---author:wangshuai ---date:20221111  for: [VUEN-2472]로그인 없이 공유------------
const whitePathList: PageEnum[] = [LOGIN_PATH, OAUTH2_LOGIN_PAGE_PATH, SYS_FILES_PATH, TOKEN_LOGIN];
//update-end---author:wangshuai ---date:20221111  for: [VUEN-2472]로그인 없이 공유------------
//update-end---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3은 auth2 로그인을 지원하지 않습니다------------

/**
 * Create permission guard.
 */
export function createPermissionGuard(router: Router) {
  router.beforeEach(async (to, from) => {
    const userStore = useUserStore();
    const permissionStore = usePermissionStore();
    if (
      from.path === ROOT_PATH &&
      to.path === PageEnum.BASE_HOME &&
      userStore.getUserInfo.homePath &&
      userStore.getUserInfo.homePath !== PageEnum.BASE_HOME
    ) {
      return PageEnum.BASE_HOME;
    }

    const token = userStore.getToken;

    // Whitelist can be directly entered
    if (whitePathList.includes(to.path as PageEnum)) {
      if (to.path === LOGIN_PATH && token) {
        const isSessionTimeout = userStore.getSessionTimeout;
        //update-begin---author:scott ---date:2023-04-24  for：【QQYUN-4713】로그인 코드 조정 논리에 문제가 있으며 변환은 아직 지켜봐야 합니다.
        //NOTE:vben은 기본 방식으로 작성되었습니다. 현재로서는 목적을 모르겠습니다. 문제가 있으면 먼저 주석을 달아주세요.
        //await userStore.afterLoginAction();
        //update-end---author:scott ---date::2023-04-24  for：【QQYUN-4713】로그인 코드 조정 논리에 문제가 있으며 변환은 아직 지켜봐야 합니다.
        try {
          if (!isSessionTimeout) {
            return resolveInternalRedirect(to.query?.redirect, '/');
          }
        } catch {}
        //update-begin---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3은 auth2 로그인을 지원하지 않습니다------------
      } else if (to.path === LOGIN_PATH && isOAuth2AppEnv() && !token) {
        //로그아웃하고 이 논리를 입력하세요.
        //입력한 페이지가 로그인 페이지이고 현재 환경이 OAuth2app이고 토큰이 비어 있으면 OAuth2 로그인 페이지로 들어갑니다.
        //update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]새로운 기업 라인 및 Kakao 구성 테이블을 생성하고 테넌트 모드를 통해 격리합니다.------------
        if (to.query.tenantId) {
          setAuthCache(OAUTH2_THIRD_LOGIN_TENANT_ID, to.query.tenantId);
        }
        return { path: OAUTH2_LOGIN_PAGE_PATH };
        //update-end---author:wangshuai ---date:20230224  for：[QQYUN-3440]새로운 기업 라인 및 Kakao 구성 테이블을 생성하고 테넌트 모드를 통해 격리합니다.------------
        //update-end---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3은 auth2 로그인을 지원하지 않습니다------------
      }
      return true;
    }

    // token does not exist
    if (!token) {
      // You can access without permission. You need to set the routing meta.ignoreAuth to true
      if (to.meta.ignoreAuth) {
        return true;
      }

      //update-begin---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3 Auth2가 구현되지 않았습니다.------------
      let path = LOGIN_PATH;
      if (whitePathList.includes(to.path as PageEnum)) {
        // 로그인 프리 화이트리스트에 입력한 페이지가 로그인 페이지이고, 현재 OAuth2app 환경이라면 OAuth2 로그인 페이지를 입력합니다.
        if (to.path === LOGIN_PATH && isOAuth2AppEnv()) {
          return { path: OAUTH2_LOGIN_PAGE_PATH };
        } else {
          //로그인 없이 화이트리스트에 직접 입력
          return true;
        }
      } else {
        //update-begin---author:wangshuai ---date:20230302  for：처음 로그인하고 라인 또는 Kakao를 사용할 때만 호출됩니다.------------
        //----------【처음 로그인하고 라인 또는 Kakao를 사용할 때만 호출됩니다.】-----------------------------------------------
        //처음 로그인하고 라인 또는 Kakao를 사용할 때만 호출됩니다.
        const href = window.location.href;
        //현재 페이지가 auth2이고 Kakao/Enterprise 라인이며 테넌트Id 매개변수를 포함하는지 확인합니다.
        if (isOAuth2AppEnv() && href.indexOf('/tenantId/') != -1) {
          const params = to.params;
          if (params && params.path && params.path.length > 0) {
            //매개변수의 마지막 숫자를 직접 가져옵니다.
            setAuthCache(OAUTH2_THIRD_LOGIN_TENANT_ID, params.path[params.path.length - 1]);
          }
        }
        //---------【처음 로그인하고 라인 또는 Kakao를 사용할 때만 호출됩니다.】------------------------------------------------
        //update-end---author:wangshuai ---date:20230302  for：처음 로그인하고 라인 또는 Kakao를 사용할 때만 호출됩니다.------------
        // 현재 OAuth2APP 환경에 있다면 OAuth2 로그인 페이지로 이동하고, 그렇지 않으면 로그인 페이지로 이동하세요.
        path = isOAuth2AppEnv() ? OAUTH2_LOGIN_PAGE_PATH : LOGIN_PATH;
      }
      //update-end---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3 Auth2가 구현되지 않았습니다.------------
      // redirect login page
      const redirectData: { path: string; replace: boolean; query?: Recordable<string> } = {
        //update-begin---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3 Auth2가 구현되지 않았습니다.------------
        path: path,
        //update-end---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3 Auth2가 구현되지 않았습니다.------------
        replace: true,
      };

      //update-begin---author:scott ---date:2023-04-24  for：【QQYUN-4713】로그인 코드 조정 논리에 문제가 있으며 변환은 아직 지켜봐야 합니다.
      if (to.fullPath) {
        const getFullPath = to.fullPath;
        if (
          getFullPath == '/' ||
          getFullPath == '/500' ||
          getFullPath == '/400' ||
          getFullPath == '/login?redirect=/' ||
          getFullPath == '/login?redirect=/login?redirect=/'
        ) {
          return true;
        }
        //update-end---author:scott ---date:2023-04-24  for：【QQYUN-4713】로그인 코드 조정 논리에 문제가 있으며 변환은 아직 지켜봐야 합니다.

        redirectData.query = {
          ...redirectData.query,
          // update-begin-author:sunjianlei date:20230306 for: 로그인 성공 후 올바르게 리디렉션되지 않는 문제가 수정되었습니다.
          redirect: to.fullPath,
          // update-end-author:sunjianlei date:20230306 for: 로그인 성공 후 올바르게 리디렉션되지 않는 문제가 수정되었습니다.
        };
      }
      return redirectData;
    }

    //==============================【처음 로그인하고 라인 또는 Kakao를 사용할 때만 호출됩니다.】==================
    //로그인이 필요 없는 페이지로 판단됩니다. 해당 페이지에 /tenantId/가 포함되어 있으면 홈페이지로 바로 이동합니다.
    if (isOAuth2AppEnv() && to.path.indexOf('/tenantId/') != -1) {
      return PageEnum.BASE_HOME;
    }
    //==============================【처음 로그인하고 라인 또는 Kakao를 사용할 때만 호출됩니다.】==================

    // Jump to the 404 page after processing the login
    const pageNotFoundChildName = PAGE_NOT_FOUND_ROUTE.children?.[0]?.name;
    const isNotFoundTarget =
      to.name === PAGE_NOT_FOUND_ROUTE.name ||
      (Boolean(pageNotFoundChildName) && to.name === pageNotFoundChildName) ||
      to.matched.some((record) => record.name === PAGE_NOT_FOUND_ROUTE.name);

    if (from.path === LOGIN_PATH && isNotFoundTarget && to.fullPath !== (userStore.getUserInfo.homePath || PageEnum.BASE_HOME)) {
      return PageEnum.BASE_HOME;
    }

    if (userStore.getLastUpdateTime === 0) {
      userStore.setAllDictItemsByLocal();
    }

    if (permissionStore.getIsDynamicAddedRoute) {
      return true;
    }

    // 배경 메뉴 라우팅 구축
    const routes = await permissionStore.buildRoutesAction();
    routes.forEach((route) => {
      router.addRoute(route as unknown as RouteRecordRaw);
    });

    router.addRoute(PAGE_NOT_FOUND_ROUTE as unknown as RouteRecordRaw);
    permissionStore.setDynamicAddedRoute(true);

    if (isNotFoundTarget) {
      // 경로를 동적으로 추가한 후에는 원본 경로로 재매칭해야 404 자식 라우트에 고정되지 않습니다.
      return { path: to.path, replace: true, query: to.query, hash: to.hash };
    } else {
      // fullPath 기반으로 통일하여 query string / hash 가 포함된 원본 navigation 의도를 보존한다.
      // 인코딩된 redirect 값을 굳이 decode 하지 않아 router 가 자체적으로 정규화하도록 둔다.
      const fallbackPath = to.fullPath || PageEnum.BASE_HOME;
      const redirect = resolveInternalRedirect(from.query.redirect, fallbackPath);
      const nextData = to.fullPath === redirect ? { ...to, replace: true } : redirect;
      return nextData;
    }
  });
}
