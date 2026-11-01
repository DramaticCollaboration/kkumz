import { defHttp } from '/@/utils/http/axios';

enum Api {
  keysSize = '/api/v1/kkumz/sys/actuator/redis/keysSize',
  memoryInfo = '/api/v1/kkumz/sys/actuator/redis/memoryInfo',
  info = '/api/v1/kkumz/sys/actuator/redis/info',
  metricsHistory = '/api/v1/kkumz/sys/actuator/redis/metrics/history',
}

/**
 * 키 수
 */
export const getKeysSize = () => {
  return defHttp.get({ url: Api.keysSize }, { isTransformResponse: false });
};

/**
 * 메모리 정보
 */
export const getMemoryInfo = () => {
  return defHttp.get({ url: Api.memoryInfo }, { isTransformResponse: false });
};

/**
 * 상세
 */
export const getInfo = () => {
  return defHttp.get({ url: Api.info });
};

/**
 * 이력 모니터링 기록
 */
export const getMetricsHistory = () => {
  return defHttp.get({ url: Api.metricsHistory });
};

export const getRedisInfo = () => {
  return Promise.all([getKeysSize(), getMemoryInfo()]);
};
