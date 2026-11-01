import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/api/v1/kkumz/sys/log/list',
}

/**
 * 쿼리 로그 목록
 * @param params
 */
export const getLogList = (params) => {
  return defHttp.get({ url: Api.list, params });
};
