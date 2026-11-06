/**
 * @fileoverview Axios 인스턴스 팩토리 및 인터셉터 파이프라인 구성
 * @description
 *  - 요청 인터셉터: JWT Bearer 토큰(X-Access-Token) 헤더 자동 주입, 타임스탬프 쿼리 결합, 중복 요청 취소(CancelToken)
 *  - 응답 인터셉터: 백엔드 Result<T> 표준 응답 언래핑, 비즈니스 에러 코드(401/403/500) 감지 및 모달 토스트 알림
 *  - 인증 만료 처리: 401 Unauthorized 수신 시 자동 로그아웃 및 로그인 페이지 리다이렉트
 */
// Axios 구성은 프로젝트에 따라 변경될 수 있으며, 이 파일만 변경하면 되며, 다른 파일은 변경하지 않고 그대로 놔두셔도 됩니다.
// The axios configuration can be changed according to the project, just change the file, other files can be left unchanged

import type { AxiosResponse } from 'axios';
import type { RequestOptions, Result } from '/#/axios';
import type { AxiosTransform, CreateAxiosOptions } from './axiosTransform';
import { VAxios } from './Axios';
import { checkStatus } from './checkStatus';
import { router } from '/@/router';
import { useGlobSetting } from '/@/hooks/setting';
import { useMessage } from '/@/hooks/web/useMessage';
import { RequestEnum, ResultEnum, ContentTypeEnum, ConfigEnum } from '/@/enums/httpEnum';
import { isString } from '/@/utils/is';
import { getToken } from '/@/utils/auth';
import { setObjToUrlParams, deepMerge } from '/@/utils';
import signMd5Utils from '/@/utils/encryption/signMd5Utils';
import { useErrorLogStoreWithOut } from '/@/store/modules/errorLog';
import { useI18n } from '/@/hooks/web/useI18n';
import { joinTimestamp, formatRequestDate } from './helper';
import { useUserStoreWithOut } from '/@/store/modules/user';
import { cloneDeep } from "lodash-es";

const globSetting = useGlobSetting();
const urlPrefix = globSetting.urlPrefix;
const { createMessage, createErrorModal } = useMessage();

/**
 * @description: 데이터 처리, 여러 처리 방법을 쉽게 구별
 */
const transform: AxiosTransform = {
  /**
   * @description: 요청 데이터를 처리합니다.데이터가 예상된 형식이 아닌 경우 오류가 직접 발생할 수 있습니다.
   */
  transformRequestHook: (res: AxiosResponse<Result>, options: RequestOptions) => {
    const { t } = useI18n();
    const { isTransformResponse, isReturnNativeResponse } = options;
    if (isReturnNativeResponse) {
      return res;
    }
    if (!isTransformResponse) {
      return res.data;
    }

    const { data } = res;
    if (!data) {
      throw new Error(t('sys.api.apiRequestFailed'));
    }
    const { code, result, message, success } = data;
    const hasSuccess = data && Reflect.has(data, 'code') && (code === ResultEnum.SUCCESS || code === 200);
    if (hasSuccess) {
      const method = (res.config?.method || 'GET').toUpperCase();
      const requestUrl = String(res.config?.url || '');
      const isGet = method === RequestEnum.GET;
      const isScreenTransitionOrSilentApi =
        /\/(login|phoneLogin|logout|thirdLogin|validateLogin|getUserInfo|getUserPermissionByToken|getPermCode|randomImage|sms|thirdSms|checkOnlyUser|duplicate\/check|dictItemCheck|checkRoleCode|checkPermDuplication|analyze-intent|generate\/|scaffold-site|concierge|verify)(\/|\?|$)/i.test(
          requestUrl
        );
      const rawMsg = typeof message === 'string' ? message.trim() : '';
      const isGenericOk = /^(ok|success|성공|작업 성공|작업성공|쿼리 성공)$/i.test(rawMsg);

      if (!isGet && !isScreenTransitionOrSilentApi && success && options.successMessageMode === 'success') {
        if (rawMsg && !isGenericOk) {
          createMessage.success(rawMsg);
        } else if (method === RequestEnum.DELETE || /\/delete/i.test(requestUrl)) {
          createMessage.success('삭제되었습니다.');
        } else if (method === RequestEnum.PUT || /\/(edit|update)(\/|\?|$)/i.test(requestUrl)) {
          createMessage.success('수정되었습니다.');
        } else if (method === RequestEnum.POST) {
          createMessage.success('등록되었습니다.');
        }
      }
      return result;
    }

    let timeoutMsg = '';
    switch (code) {
      case ResultEnum.TIMEOUT:
        timeoutMsg = t('sys.api.timeoutMessage');
        const userStore = useUserStoreWithOut();
        userStore.setToken(undefined);
        userStore.logout(true);
        break;
      default:
        if (message) {
          timeoutMsg = message;
        }
    }

    if (options.isPolling) {
      throw new Error(timeoutMsg || t('sys.api.apiRequestFailed'));
    }

    if (options.errorMessageMode === 'modal') {
      createErrorModal({ title: t('sys.api.errorTip'), content: timeoutMsg });
    } else if (options.errorMessageMode === 'message') {
      createMessage.error(timeoutMsg);
    }

    throw new Error(timeoutMsg || t('sys.api.apiRequestFailed'));
  },

  // 요청하기 전에 구성을 처리하세요.
  beforeRequestHook: (config, options) => {
    const { apiUrl, joinPrefix, joinParamsToUrl, formatDate, joinTime = true, urlPrefix } = options;

    let isStartWithHttp = false;
    const requestUrl = config.url;
    if(requestUrl!=null && (requestUrl.startsWith("http:") || requestUrl.startsWith("https:"))){
      isStartWithHttp = true;
    }
    if (!isStartWithHttp && joinPrefix) {
      config.url = `${urlPrefix}${config.url}`;
    }

    if (!isStartWithHttp && apiUrl && isString(apiUrl) && !config.url?.startsWith(apiUrl)) {
      config.url = `${apiUrl}${config.url}`;
    }
    
    const params = config.params || {};
    const data = config.data || false;
    formatDate && data && !isString(data) && formatRequestDate(data);
    if (config.method?.toUpperCase() === RequestEnum.GET) {
      if (!isString(params)) {
        config.params = Object.assign(params || {}, joinTimestamp(joinTime, false));
      } else {
        config.url = config.url + params + `${joinTimestamp(joinTime, true)}`;
        config.params = undefined;
      }
    } else {
      if (!isString(params)) {
        formatDate && formatRequestDate(params);
        if (Reflect.has(config, 'data') && config.data && Object.keys(config.data).length > 0) {
          config.data = data;
          config.params = params;
        } else {
          config.data = params;
          config.params = undefined;
        }
        if (joinParamsToUrl) {
          config.url = setObjToUrlParams(config.url as string, Object.assign({}, config.params, config.data));
          config.params = undefined;
        }
      } else {
        config.url = config.url + params;
        config.params = undefined;
      }
    }
    return config;
  },

  /**
   * @description: 요청 인터셉터 처리
   */
  requestInterceptors: (config: Recordable, options) => {
    const token = getToken();
    
    config.headers[ConfigEnum.TIMESTAMP] = signMd5Utils.getTimestamp();
    config.headers[ConfigEnum.Sign] = signMd5Utils.getSign(config.url, cloneDeep(config.params), cloneDeep(config.data));
    config.headers[ConfigEnum.VERSION] = 'v3';

    if (token && (config as Recordable)?.requestOptions?.withToken !== false) {
      config.headers.Authorization = options.authenticationScheme ? `${options.authenticationScheme} ${token}` : token;
      config.headers[ConfigEnum.TOKEN] = token;

      let routeParams = router.currentRoute.value.params;
      if (routeParams.appId) {
        config.headers[ConfigEnum.X_LOW_APP_ID] = routeParams.appId;
        if (routeParams.lowAppFilter) {
          config.params = { ...config.params, ...JSON.parse(routeParams.lowAppFilter as string) };
          delete routeParams.lowAppFilter;
        }
      }
    }
    return config;
  },

  /**
   * @description: 응답 인터셉터 처리
   */
  responseInterceptors: (res: AxiosResponse<any>) => {
    return res;
  },

  /**
   * @description: 응답 오류 처리
   */
  responseInterceptorsCatch: (error: any) => {
    const { t } = useI18n();
    const errorLogStore = useErrorLogStoreWithOut();
    errorLogStore.addAjaxErrorInfo(error);
    const { response, code, message, config } = error || {};
    const errorMessageMode = config?.requestOptions?.errorMessageMode || 'none';
    const msg: string = response?.data?.message ?? '';
    const err: string = error?.toString?.() ?? '';
    let errMessage = '';

    if (config?.requestOptions?.isPolling) {
      // Polling 요청인 경우 에러 모달/메시지를 띄우지 않고 조용히 reject만 수행
      return Promise.reject(error);
    }

    try {
      if (code === 'ECONNABORTED' && message.indexOf('timeout') !== -1) {
        errMessage = t('sys.api.apiTimeoutMessage');
      }
      if (err?.includes('Network Error')) {
        errMessage = t('sys.api.networkExceptionMsg');
      }

      if (errMessage) {
        if (errorMessageMode === 'modal') {
          createErrorModal({ title: t('sys.api.errorTip'), content: errMessage });
        } else if (errorMessageMode === 'message') {
          createMessage.error(errMessage);
        }
        return Promise.reject(error);
      }
    } catch (error) {
      throw new Error(error);
    }

    checkStatus(error?.response?.status, msg, errorMessageMode);
    return Promise.reject(error);
  },
};

function createAxios(opt?: Partial<CreateAxiosOptions>) {
  return new VAxios(
    deepMerge(
      {
        authenticationScheme: '',
        timeout: 10 * 1000,
        headers: { 'Content-Type': ContentTypeEnum.JSON },
        transform,
        requestOptions: {
          joinPrefix: true,
          isReturnNativeResponse: false,
          isTransformResponse: true,
          joinParamsToUrl: false,
          formatDate: true,
          errorMessageMode: 'message',
          successMessageMode: 'none',
          apiUrl: globSetting.apiUrl,
          urlPrefix: urlPrefix,
          joinTime: true,
          ignoreCancelToken: true,
          withToken: true,
        },
      },
      opt || {}
    )
  );
}
export const defHttp = createAxios();
