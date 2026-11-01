import { defHttp } from '/@/utils/http/axios';

enum Api {
  queryDiskInfo = '/api/v1/kkumz/sys/actuator/redis/queryDiskInfo',
}

/**
 * 상세
 */
export const queryDiskInfo = () => {
  return defHttp.get({ url: Api.queryDiskInfo }, { successMessageMode: 'none' });
};
