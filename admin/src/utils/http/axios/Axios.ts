/**
 * @fileoverview Axios 라이브러리 래퍼 클래스 (VAxios)
 * @description
 *  - HTTP 메서드(GET, POST, PUT, DELETE, UPLOAD) 편의 함수 제공
 *  - 요청/응답 변환기(AxiosTransform) 체인을 통한 일관된 직렬화 및 역직렬화 보장
 */
import type { AxiosRequestConfig, AxiosInstance, AxiosResponse, AxiosError } from 'axios';
import type { RequestOptions, Result, UploadFileParams, UploadFileCallBack } from '/#/axios';
import type { CreateAxiosOptions } from './axiosTransform';
import axios from 'axios';
import qs from 'qs';
import { AxiosCanceler } from './axiosCancel';
import { isFunction } from '/@/utils/is';
import { cloneDeep } from 'lodash-es';
import { ContentTypeEnum } from '/@/enums/httpEnum';
import { RequestEnum } from '/@/enums/httpEnum';
import { useGlobSetting } from '/@/hooks/setting';
import { useMessage } from '/@/hooks/web/useMessage';

const { createMessage } = useMessage();
export * from './axiosTransform';

/**
 * @description:  axios module
 */
export class VAxios {
  private axiosInstance: AxiosInstance;
  private readonly options: CreateAxiosOptions;

  constructor(options: CreateAxiosOptions) {
    this.options = options;
    this.axiosInstance = axios.create(options);
    this.setupInterceptors();
  }

  /**
   * @description:  Create axios instance
   */
  private createAxios(config: CreateAxiosOptions): void {
    this.axiosInstance = axios.create(config);
  }

  private getTransform() {
    const { transform } = this.options;
    return transform;
  }

  getAxios(): AxiosInstance {
    return this.axiosInstance;
  }

  /**
   * @description: Reconfigure axios
   */
  configAxios(config: CreateAxiosOptions) {
    if (!this.axiosInstance) {
      return;
    }
    this.createAxios(config);
  }

  /**
   * @description: Set general header
   */
  setHeader(headers: any): void {
    if (!this.axiosInstance) {
      return;
    }
    Object.assign(this.axiosInstance.defaults.headers, headers);
  }

  /**
   * @description: Interceptor configuration
   */
  private setupInterceptors() {
    const transform = this.getTransform();
    if (!transform) {
      return;
    }
    const { requestInterceptors, requestInterceptorsCatch, responseInterceptors, responseInterceptorsCatch } = transform;

    const axiosCanceler = new AxiosCanceler();

    // 요청 리스너 구성 처리
    this.axiosInstance.interceptors.request.use((config: AxiosRequestConfig) => {
      // If cancel repeat request is turned on, then cancel repeat request is prohibited
      // @ts-ignore
      const { ignoreCancelToken } = config.requestOptions;

      const ignoreCancel = ignoreCancelToken !== undefined ? ignoreCancelToken : this.options.requestOptions?.ignoreCancelToken;

      !ignoreCancel && axiosCanceler.addPending(config);
      if (requestInterceptors && isFunction(requestInterceptors)) {
        config = requestInterceptors(config, this.options);
      }
      return config;
    }, undefined);

    // 요청 인터셉터 오류 잡기
    requestInterceptorsCatch &&
      isFunction(requestInterceptorsCatch) &&
      this.axiosInstance.interceptors.request.use(undefined, requestInterceptorsCatch);

    // 응답 결과 인터셉터 처리
    this.axiosInstance.interceptors.response.use((res: AxiosResponse<any>) => {
      res && axiosCanceler.removePending(res.config);
      if (responseInterceptors && isFunction(responseInterceptors)) {
        res = responseInterceptors(res);
      }
      return res;
    }, undefined);

    // 응답 결과 인터셉터 오류 잡기
    responseInterceptorsCatch &&
      isFunction(responseInterceptorsCatch) &&
      this.axiosInstance.interceptors.response.use(undefined, responseInterceptorsCatch);
  }

  /**
   * 파일 업로드
   */
  //--@updateBy-begin----author:liusq---date:20211117------for:업로드 콜백 매개변수 콜백 추가------
  uploadFile<T = any>(config: AxiosRequestConfig, params: UploadFileParams, callback?: UploadFileCallBack) {
    //--@updateBy-end----author:liusq---date:20211117------for:업로드 콜백 매개변수 콜백 추가------
    const formData = new window.FormData();
    const customFilename = params.name || 'file';

    if (params.filename) {
      formData.append(customFilename, params.file, params.filename);
    } else {
      formData.append(customFilename, params.file);
    }
    const glob = useGlobSetting();
    config.baseURL = glob.uploadUrl;
    if (params.data) {
      Object.keys(params.data).forEach((key) => {
        const value = params.data![key];
        if (Array.isArray(value)) {
          value.forEach((item) => {
            formData.append(`${key}[]`, item);
          });
          return;
        }

        formData.append(key, params.data[key]);
      });
    }

    return this.axiosInstance
      .request<T>({
        ...config,
        method: 'POST',
        data: formData,
        headers: {
          'Content-type': ContentTypeEnum.FORM_DATA,
          ignoreCancelToken: true,
        },
      })
      .then((res: any) => {
        //--@updateBy-begin----author:liusq---date:20210914------for:업로드에 콜백 메소드가 포함되어 있는지 확인------
        if (callback?.success && isFunction(callback?.success)) {
          callback?.success(res?.data);
          //--@updateBy-end----author:liusq---date:20210914------for:업로드에 콜백 메소드가 포함되어 있는지 확인------
        } else if (callback?.isReturnResponse) {
          //--@updateBy-begin----author:liusq---date:20211117------for:업로드하여 해상도 정보 반환 여부 결정------
          return Promise.resolve(res?.data);
          //--@updateBy-end----author:liusq---date:20211117------for:업로드하여 해상도 정보 반환 여부 결정------
        } else {
          if (res.data.success == true && res.data.code == 200) {
            createMessage.success(res.data.message);
          } else {
            createMessage.error(res.data.message);
          }
        }
      });
  }

  // 지원 Form 데이터
  supportFormData(config: AxiosRequestConfig) {
    const headers = config.headers || this.options.headers;
    const contentType = headers?.['Content-Type'] || headers?.['content-type'];

    if (contentType !== ContentTypeEnum.FORM_URLENCODED || !Reflect.has(config, 'data') || config.method?.toUpperCase() === RequestEnum.GET) {
      return config;
    }

    return {
      ...config,
      data: qs.stringify(config.data, { arrayFormat: 'brackets' }),
    };
  }

  get<T = any>(config: AxiosRequestConfig, options?: RequestOptions): Promise<T> {
    return this.request({ ...config, method: 'GET' }, options);
  }

  post<T = any>(config: AxiosRequestConfig, options?: RequestOptions): Promise<T> {
    return this.request({ ...config, method: 'POST' }, options);
  }

  put<T = any>(config: AxiosRequestConfig, options?: RequestOptions): Promise<T> {
    return this.request({ ...config, method: 'PUT' }, options);
  }

  delete<T = any>(config: AxiosRequestConfig, options?: RequestOptions): Promise<T> {
    return this.request({ ...config, method: 'DELETE' }, options);
  }

  request<T = any>(config: AxiosRequestConfig, options?: RequestOptions): Promise<T> {
    let conf: CreateAxiosOptions = cloneDeep(config);
    const transform = this.getTransform();

    const { requestOptions } = this.options;

    const opt: RequestOptions = Object.assign({}, requestOptions, options);

    const { beforeRequestHook, requestCatchHook, transformRequestHook } = transform || {};
    if (beforeRequestHook && isFunction(beforeRequestHook)) {
      conf = beforeRequestHook(conf, opt);
    }
    conf.requestOptions = opt;

    conf = this.supportFormData(conf);

    return new Promise((resolve, reject) => {
      this.axiosInstance
        .request<any, AxiosResponse<Result>>(conf)
        .then((res: AxiosResponse<Result>) => {
          if (transformRequestHook && isFunction(transformRequestHook)) {
            try {
              const ret = transformRequestHook(res, opt);
              //zhangyafei---콜백 메소드 추가
              config.success && config.success(res.data);
              //zhangyafei---콜백 메소드 추가
              resolve(ret);
            } catch (err) {
              reject(err || new Error('request error!'));
            }
            return;
          }
          resolve(res as unknown as Promise<T>);
        })
        .catch((e: Error | AxiosError) => {
          if (requestCatchHook && isFunction(requestCatchHook)) {
            reject(requestCatchHook(e, opt));
            return;
          }
          if (axios.isAxiosError(e)) {
            // 여기에 axios의 오류 메시지를 다시 작성하세요.
          }
          reject(e);
        });
    });
  }


  /**
   * 【댓글 기능에 사용】사용자 정의 파일 업로드 요청
   * @param url
   * @param formData
   */
  uploadMyFile<T = any>(url, formData) {
    const glob = useGlobSetting();
    return this.axiosInstance
      .request<T>({
        url: url,
        baseURL: glob.uploadUrl,
        method: 'POST',
        data: formData,
        headers: {
          'Content-type': ContentTypeEnum.FORM_DATA,
          ignoreCancelToken: true,
        },
      });
  }
}
