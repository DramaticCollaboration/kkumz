import { defHttp } from '/@/utils/http/axios';
import { LoginParams, LoginResultModel, GetUserInfoModel } from './model/userModel';

import { ErrorMessageMode } from '/#/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { useUserStoreWithOut } from '/@/store/modules/user';
import { setAuthCache } from '/@/utils/auth';
import { TOKEN_KEY } from '/@/enums/cacheEnum';
import { router } from '/@/router';
import { PageEnum } from '/@/enums/pageEnum';
import { ExceptionEnum } from "@/enums/exceptionEnum";

const { createErrorModal } = useMessage();
enum Api {
  Login = '/api/v1/kkumz/sys/login',
  phoneLogin = '/api/v1/kkumz/sys/phoneLogin',
  Logout = '/api/v1/kkumz/sys/logout',
  GetUserInfo = '/api/v1/kkumz/sys/user/getUserInfo',
  // 시스템 권한 얻기
  // 1、사용자가 소유한 버튼/폼 접근권한을 조회합니다.
  // 2. 모든 권한
  // 3. 시스템 안전 모드
  GetPermCode = '/api/v1/kkumz/sys/permission/getPermCode',
  //그래픽 인증 코드를 받기 위한 새로운 인터페이스
  getInputCode = '/api/v1/kkumz/sys/randomImage',
  //SMS 인증 코드를 얻기 위한 인터페이스
  getCaptcha = '/api/v1/kkumz/sys/sms',
  //등록 인터페이스
  registerApi = '/api/v1/kkumz/sys/user/register',
  //사용자 인터페이스 확인
  checkOnlyUser = '/api/v1/kkumz/sys/user/checkOnlyUser',
  //SSO 로그인 확인
  validateCasLogin = '/api/v1/kkumz/sys/cas/client/validateLogin',
  //휴대폰번호 확인
  phoneVerify = '/api/v1/kkumz/sys/user/phoneVerification',
  //비밀번호 변경
  passwordChange = '/api/v1/kkumz/sys/user/passwordChange',
  //제3자 로그인
  thirdLogin = '/api/v1/kkumz/sys/thirdLogin/getLoginUser',
  //제3자 로그인
  getThirdCaptcha = '/api/v1/kkumz/sys/thirdSms',
  //QR 코드 정보 얻기
  getLoginQrcode = '/api/v1/kkumz/sys/getLoginQrcode',
  //QR 코드 스캔 상태 모니터링
  getQrcodeToken = '/api/v1/kkumz/sys/getQrcodeToken',
}

/**
 * @description: user login api
 */
export function loginApi(params: LoginParams, mode: ErrorMessageMode = 'modal') {
  return defHttp.post<LoginResultModel>(
    {
      url: Api.Login,
      params,
    },
    {
      errorMessageMode: mode,
    }
  );
}

/**
 * @description: user phoneLogin api
 */
export function phoneLoginApi(params: LoginParams, mode: ErrorMessageMode = 'modal') {
  return defHttp.post<LoginResultModel>(
    {
      url: Api.phoneLogin,
      params,
    },
    {
      errorMessageMode: mode,
    }
  );
}

/**
 * @description: getUserInfo
 */
export function getUserInfo() {
  return defHttp.get<GetUserInfoModel>({ url: Api.GetUserInfo }, {}).catch((e) => {
    if (e && (e.message.includes('timeout') || e.message.includes('401'))) {
      //인터페이스를 사용할 수 없을 때 로그인 인터페이스로 점프
      const userStore = useUserStoreWithOut();
      userStore.setToken('');
      setAuthCache(TOKEN_KEY, null);

      router.push({
        path: PageEnum.BASE_LOGIN,
        query: {
          // 현재 경로를 패스하고 로그인 성공 후 현재 경로로 점프합니다.
          redirect: router.currentRoute.value.fullPath,
        }
      });

    }
  });
}

export function getPermCode() {
  return defHttp.get({ url: Api.GetPermCode });
}

export function doLogout() {
  return defHttp.get({ url: Api.Logout });
}

export function getCodeInfo(currdatetime) {
  let url = Api.getInputCode + `/${currdatetime}`;
  return defHttp.get({ url: url });
}
/**
 * @description: SMS 인증 코드 받기
 */
export function getCaptcha(params) {
  return new Promise((resolve, reject) => {
    defHttp.post({ url: Api.getCaptcha, params }, { isTransformResponse: false }).then((res) => {
      console.log(res);
      if (res.success) {
        resolve(true);
      } else {
        if(res.code != ExceptionEnum.PHONE_SMS_FAIL_CODE){
          createErrorModal({ title: '오류 메시지', content: res.message || '알 수 없는 문제' });
          reject();
        }
        reject(res);
      }
    }).catch((res)=>{
      createErrorModal({ title: '오류 메시지', content: res.message || '알 수 없는 문제' });
      reject();
    });
  });
}

/**
 * @description: 등록 인터페이스
 */
export function register(params) {
  return defHttp.post({ url: Api.registerApi, params }, { isReturnNativeResponse: true });
}

/**
 *사용자가 존재하는지 확인
 * @param params
 */
export const checkOnlyUser = (params) => defHttp.get({ url: Api.checkOnlyUser, params }, { isTransformResponse: false });
/**
 *휴대폰번호 인증
 * @param params
 */
export const phoneVerify = (params) => defHttp.post({ url: Api.phoneVerify, params }, { isTransformResponse: false });
/**
 *비밀번호 변경
 * @param params
 */
export const passwordChange = (params) => defHttp.get({ url: Api.passwordChange, params }, { isTransformResponse: false });
/**
 * @description: 제3자 로그인
 */
export function thirdLogin(params, mode: ErrorMessageMode = 'modal') {
  return defHttp.get<LoginResultModel>(
    {
      url: `${Api.thirdLogin}/${params.token}/${params.thirdType}`,
    },
    {
      errorMessageMode: mode,
    }
  );
}
/**
 * @description: 제3자 SMS 인증 코드 받기
 */
export function setThirdCaptcha(params) {
  return new Promise((resolve, reject) => {
    defHttp.post({ url: Api.getThirdCaptcha, params }, { isTransformResponse: false }).then((res) => {
      console.log(res);
      if (res.success) {
        resolve(true);
      } else {
        createErrorModal({ title: '오류 메시지', content: res.message || '알 수 없는 문제' });
        reject();
      }
    });
  });
}

/**
 * 로그인 QR 코드 정보 받기
 */
export function getLoginQrcode() {
  let url = Api.getLoginQrcode;
  return defHttp.get({ url: url });
}

/**
 * 코드 스캔 상태 모니터링
 */
export function getQrcodeToken(params) {
  let url = Api.getQrcodeToken;
  return defHttp.get({ url: url, params });
}

/**
 * SSO 로그인 확인
 */
export async function validateCasLogin(params) {
  let url = Api.validateCasLogin;
  return defHttp.get({ url: url, params });
}
