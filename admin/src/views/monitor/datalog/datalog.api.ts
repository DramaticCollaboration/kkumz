import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/api/v1/kkumz/sys/dataLog/list',
  queryDataVerList = '/api/v1/kkumz/sys/dataLog/queryDataVerList',
  queryCompareList = '/api/v1/kkumz/sys/dataLog/queryCompareList',
}

/**
 * 데이터 로그 목록 조회
 * @param params
 */
export const getDataLogList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 데이터 로그 목록 조회
 * @param params
 */
export const queryDataVerList = (params) => {
  return defHttp.get({ url: Api.queryDataVerList, params });
};

/**
 * 데이터 쿼리 및 비교
 * @param params
 */
export const queryCompareList = (params) => {
  return defHttp.get({ url: Api.queryCompareList, params });
};
