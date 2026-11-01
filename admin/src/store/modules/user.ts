/**
 * @fileoverview 사용자 인증 세션 및 프로필 관리 Pinia/Vuex 스토어 (useUserStore)
 * @description
 *  - 로그인/로그아웃 트랜잭션, JWT 토큰(AccessToken, RefreshToken) 로컬 스토리지 동기화
 *  - 사용자 기본 정보(사번, 이름, 소속 부서, 역할 목록) 캐싱 및 세션 복구
 */
import type { UserInfo, LoginInfo } from '/#/store';
import type { ErrorMessageMode } from '/#/axios';
import { defineStore } from 'pinia';
import { store } from '/@/store';
import { RoleEnum } from '/@/enums/roleEnum';
import { PageEnum } from '/@/enums/pageEnum';
import { ROLES_KEY, TOKEN_KEY, USER_INFO_KEY, LOGIN_INFO_KEY, DB_DICT_DATA_KEY } from '/@/enums/cacheEnum';
import { getAuthCache, setAuthCache, removeAuthCache } from '/@/utils/auth';
import { GetUserInfoModel, LoginParams, ThirdLoginParams } from '/@/api/sys/model/userModel';
import { doLogout, getUserInfo, loginApi, phoneLoginApi, thirdLogin } from '/@/api/sys/user';
import { useI18n } from '/@/hooks/web/useI18n';
import { useMessage } from '/@/hooks/web/useMessage';
import { router } from '/@/router';
import { usePermissionStore } from '/@/store/modules/permission';
import { RouteRecordRaw } from 'vue-router';
import { PAGE_NOT_FOUND_ROUTE } from '/@/router/routes/basic';
import { isArray } from '/@/utils/is';
import { useGlobSetting } from '/@/hooks/setting';
import { JDragConfigEnum } from '/@/enums/syncEnum';
import { useSso } from '/@/hooks/web/useSso';
import { isOAuth2AppEnv } from "/@/views/sys/login/useLogin";
import { getUrlParam } from "@/utils";

interface dictType {
  [key: string]: any;
}

interface UserState {
  userInfo: Nullable<UserInfo>;
  token?: string;
  roleList: RoleEnum[];
  dictItems?: dictType | null;
  sessionTimeout?: boolean;
  lastUpdateTime: number;
  loginInfo?: Nullable<LoginInfo>;
}

export const useUserStore = defineStore({
  id: 'app-user',
  state: (): UserState => ({
    // 사용자 정보
    userInfo: null,
    // token
    token: undefined,
    // 역할 목록
    roleList: [],
    // 사전
    dictItems: null,
    // 세션 만료 시간
    sessionTimeout: false,
    // Last fetch time
    lastUpdateTime: 0,
    //로그인 반납정보
    loginInfo: null,
  }),
  getters: {
    getUserInfo(): UserInfo {
      if(this.userInfo == null){
        this.userInfo = getAuthCache<UserInfo>(USER_INFO_KEY)!=null ? getAuthCache<UserInfo>(USER_INFO_KEY) : null;
      }
      const info = this.userInfo || getAuthCache<UserInfo>(USER_INFO_KEY) || ({} as UserInfo);
      if (info && info.homePath && String(info.homePath).startsWith('/ecommerce')) {
        info.homePath = PageEnum.BASE_HOME;
      }
      return info;
    },
    getLoginInfo(): LoginInfo {
      return this.loginInfo || getAuthCache<LoginInfo>(LOGIN_INFO_KEY) || {};
    },
    getToken(): string {
      return this.token || getAuthCache<string>(TOKEN_KEY);
    },
    getAllDictItems(): [] {
      return this.dictItems || getAuthCache(DB_DICT_DATA_KEY);
    },
    getRoleList(): RoleEnum[] {
      return this.roleList.length > 0 ? this.roleList : getAuthCache<RoleEnum[]>(ROLES_KEY);
    },
    getSessionTimeout(): boolean {
      return !!this.sessionTimeout;
    },
    getLastUpdateTime(): number {
      return this.lastUpdateTime;
    },
  },
  actions: {
    setToken(info: string | undefined) {
      this.token = info ? info : ''; // for null or undefined value
      setAuthCache(TOKEN_KEY, info);
    },
    setRoleList(roleList: RoleEnum[]) {
      this.roleList = roleList;
      setAuthCache(ROLES_KEY, roleList);
    },
    setUserInfo(info: UserInfo | null) {
      if (info) {
        const roles = info.roles || [];
        if (roles.some((role) => role.value === RoleEnum.INSTALLER)) {
          info.homePath = PageEnum.INSTALLER_HOME;
        }
        if (!info.homePath || String(info.homePath).startsWith('/ecommerce')) {
          info.homePath = PageEnum.BASE_HOME;
        }
      }
      this.userInfo = info;
      this.lastUpdateTime = new Date().getTime();
      setAuthCache(USER_INFO_KEY, info);
    },
    setLoginInfo(info: LoginInfo | null) {
      this.loginInfo = info;
      setAuthCache(LOGIN_INFO_KEY, info);
    },
    setAllDictItems(dictItems) {
      this.dictItems = dictItems;
      setAuthCache(DB_DICT_DATA_KEY, dictItems);
    },
    setAllDictItemsByLocal() {
      if (!this.dictItems) {
        const allDictItems = getAuthCache(DB_DICT_DATA_KEY);
        if (allDictItems) {
          this.dictItems = allDictItems;
        }
      }
    },
    setSessionTimeout(flag: boolean) {
      this.sessionTimeout = flag;
    },
    resetState() {
      this.userInfo = null;
      this.dictItems = null;
      this.token = '';
      this.roleList = [];
      this.sessionTimeout = false;
    },
    async login(
      params: LoginParams & {
        goHome?: boolean;
        mode?: ErrorMessageMode;
      }
    ): Promise<GetUserInfoModel | null> {
      try {
        const { goHome = true, mode, ...loginParams } = params;
        const data = await loginApi(loginParams, mode);
        const { token } = data;
        this.setToken(token);
        return this.afterLoginAction(goHome, data);
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async qrCodeLogin(token): Promise<GetUserInfoModel | null> {
      try {
        this.setToken(token);
        return this.afterLoginAction(true, {});
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async afterLoginAction(goHome?: boolean, data?: any): Promise<any | null> {
      if (!this.getToken) return null;
      const userInfo = await this.getUserInfoAction();
      const sessionTimeout = this.sessionTimeout;
      if (sessionTimeout) {
        this.setSessionTimeout(false);
      } else {
        await this.setLoginInfo({ ...data, isLogin: true });
        localStorage.setItem(JDragConfigEnum.DRAG_BASE_URL, useGlobSetting().domainUrl);

        let redirect = router.currentRoute.value?.query?.redirect as string;
        if (redirect) {
          try {
            redirect = decodeURIComponent(redirect);
          } catch (e) {
            console.warn('[UserStore] URL 디코딩 실패(원본 유지):', e);
          }
        }

        const rawHome = userInfo && userInfo.homePath;
        const homePath = rawHome && !String(rawHome).startsWith('/ecommerce') ? rawHome : PageEnum.BASE_HOME;

        if (redirect && redirect.startsWith('/ecommerce')) {
          redirect = '';
        }

        if (redirect && goHome) {
          let publicPath = import.meta.env.VITE_PUBLIC_PATH;
          if (publicPath && publicPath != '/') {
            if (publicPath.endsWith('/')) {
              publicPath = publicPath.slice(0, -1);
            }
            redirect = publicPath + redirect;
          }
          window.open(redirect, '_self')
          return data;
        }

        let ticket = getUrlParam('ticket');
        if(ticket){
          goHome && (window.location.replace(homePath));
        }else{
          goHome && (await router.replace(homePath));
        }
      }
      return data;
    },
    async phoneLogin(
      params: LoginParams & {
        goHome?: boolean;
        mode?: ErrorMessageMode;
      }
    ): Promise<GetUserInfoModel | null> {
      try {
        const { goHome = true, mode, ...loginParams } = params;
        const data = await phoneLoginApi(loginParams, mode);
        const { token } = data;
        this.setToken(token);
        return this.afterLoginAction(goHome, data);
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async getUserInfoAction(): Promise<UserInfo | null> {
      if (!this.getToken) {
        return null;
      }
      const res: any = await getUserInfo();
      const userInfo = res?.userInfo || (res?.username ? res : null);
      const sysAllDictItems = res?.sysAllDictItems;
      if (userInfo) {
        const { roles = [] } = userInfo;
        if (isArray(roles)) {
          const roleList = roles.map((item) => item.value || item.roleCode || item) as RoleEnum[];
          this.setRoleList(roleList);
        } else {
          userInfo.roles = [];
          this.setRoleList([]);
        }
        this.setUserInfo(userInfo);
      }
      if (sysAllDictItems) {
        this.setAllDictItems(sysAllDictItems);
      }
      return userInfo;
    },
    async logout(goLogin = false) {
      if (this.getToken) {
        try {
          await doLogout();
        } catch (error) {
          console.warn('[UserStore] 토큰 로그아웃 실패:', error);
        }
      }

      this.setToken('');
      setAuthCache(TOKEN_KEY, null);
      this.setSessionTimeout(false);
      this.setUserInfo(null);
      this.setLoginInfo(null);
      setTimeout(() => {
        this.setAllDictItems(null);
      }, 1e3);
      localStorage.removeItem(JDragConfigEnum.DRAG_BASE_URL);

      const openSso = useGlobSetting().openSso;
      if (openSso == 'true') {
        await useSso().ssoLoginOut();
      }
      if(isOAuth2AppEnv()){
        goLogin && await router.push({ name:"Login" })
      }else{
        goLogin && (await router.push({
          path: PageEnum.BASE_LOGIN,
          query: {
            redirect: router.currentRoute.value.fullPath,
          }
        }));
      }
    },
    async ThirdLogin(
      params: ThirdLoginParams & {
        goHome?: boolean;
        mode?: ErrorMessageMode;
      }
    ): Promise<any | null> {
      try {
        const { goHome = true, mode, ...ThirdLoginParams } = params;
        const data = await thirdLogin(ThirdLoginParams, mode);
        const { token } = data;
        this.setToken(token);
        return this.afterLoginAction(goHome, data);
      } catch (error) {
        return Promise.reject(error);
      }
    },
    confirmLoginOut() {
      const { createConfirm } = useMessage();
      const { t } = useI18n();
      createConfirm({
        iconType: 'warning',
        title: t('sys.app.logoutTip'),
        content: t('sys.app.logoutMessage'),
        onOk: async () => {
          await this.logout(true);
        },
      });
    },
  },
});

export function useUserStoreWithOut() {
  return useUserStore(store);
}
