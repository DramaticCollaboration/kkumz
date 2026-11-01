/**
 * @fileoverview Axios 라이프사이클 인터셉터 추상 변환기(AxiosTransform) 인터페이스
 * @description
 *  - beforeRequestHook: 요청 파라미터 날짜 포맷 변환 및 서명 암호화
 *  - requestInterceptors / responseInterceptors: 요청/응답 공통 헤더 및 토큰 처리
 *  - responseInterceptorsCatch: 네트워크 단절 및 서버 장애 시 글로벌 재시도 및 폴백
 */
/**
 * Data processing class, can be configured according to the project
 */
import type { AxiosRequestConfig, AxiosResponse } from 'axios';
import type { RequestOptions, Result } from '/#/axios';

export interface CreateAxiosOptions extends AxiosRequestConfig {
  authenticationScheme?: string;
  transform?: AxiosTransform;
  requestOptions?: RequestOptions;
}

export abstract class AxiosTransform {
  /**
   * @description: Process configuration before request
   * @description: Process configuration before request
   */
  beforeRequestHook?: (config: AxiosRequestConfig, options: RequestOptions) => AxiosRequestConfig;

  /**
   * @description: Request successfully processed
   */
  transformRequestHook?: (res: AxiosResponse<Result>, options: RequestOptions) => any;

  /**
   * @description: 요청 실패 처리
   */
  requestCatchHook?: (e: Error, options: RequestOptions) => Promise<any>;

  /**
   * @description: 요청 전 인터셉터
   */
  requestInterceptors?: (config: AxiosRequestConfig, options: CreateAxiosOptions) => AxiosRequestConfig;

  /**
   * @description: 요청 후 인터셉터
   */
  responseInterceptors?: (res: AxiosResponse<any>) => AxiosResponse<any>;

  /**
   * @description: 요청 전 인터셉터 오류 처리
   */
  requestInterceptorsCatch?: (error: Error) => void;

  /**
   * @description: 요청 후 인터셉터 오류 처리
   */
  responseInterceptorsCatch?: (error: Error) => void;
}
