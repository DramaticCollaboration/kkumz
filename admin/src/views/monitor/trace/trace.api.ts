import { defHttp } from '/@/utils/http/axios';

enum Api {
  actuatorList = '/api/v1/kkumz/actuator/httptrace-new/',
}

/**
 * 추적 정보
 */
export const getActuatorList = (query: String, order: String) => {
  const suffix = order ? `${query}/${order}` : `${query}`;
  return defHttp.get({ url: Api.actuatorList + suffix }, { isTransformResponse: false });
};
