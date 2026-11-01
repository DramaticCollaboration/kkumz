import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/api/v1/kkumz/sys/online/list',
  forceLogout = '/api/v1/kkumz/sys/online/forceLogout'
}

/**
 * 목록
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * 일괄 삭제역할
 * @param params
 */
export const forceLogout = (params) => {
  return defHttp.post({url:Api.forceLogout,params},{isTransformResponse:false})
};
