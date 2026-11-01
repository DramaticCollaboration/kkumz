/**
 * @fileoverview HTTP 요청 헬퍼 유틸리티
 * @description
 *  - 캐시 방지용 타임스탬프 파라미터 결합 (_t=timestamp)
 *  - 날짜/시간 객체의 ISO 표준 문자열 직렬화 포맷터
 */
import { isObject, isString } from '/@/utils/is';
import dayjs from "dayjs";
const DATE_TIME_FORMAT = 'YYYY-MM-DD HH:mm:ss';

export function joinTimestamp<T extends boolean>(join: boolean, restful: T): T extends true ? string : object;

export function joinTimestamp(join: boolean, restful = false): string | object {
  if (!join) {
    return restful ? '' : {};
  }
  const now = new Date().getTime();
  if (restful) {
    return `?_t=${now}`;
  }
  return { _t: now };
}

/**
 * @description: Format request parameter time
 */
export function formatRequestDate(params: Recordable) {
  if (Object.prototype.toString.call(params) !== '[object Object]') {
    return;
  }

  for (const key in params) {
    // dayjs 인스턴스인지 확인
    if (dayjs.isDayjs(params[key])) {
      params[key] = params[key].format(DATE_TIME_FORMAT);
    }
    if (isString(key)) {
      const value = params[key];
      if (value) {
        try {
          params[key] = isString(value) ? value.trim() : value;
        } catch (error) {
          throw new Error(error);
        }
      }
    }
    if (isObject(params[key])) {
      formatRequestDate(params[key]);
    }
  }
}
