import { defHttp } from '/@/utils/http/axios';
import { useUserStore } from '/@/store/modules/user';
import { getAuthCache } from '/@/utils/auth';
import { DB_DICT_DATA_KEY } from '/@/enums/cacheEnum';

/**
 * 캐시에서 사전 구성 가져오기
 * @param code
 */
export const getDictItemsByCode = (code) => {
  const userStore = useUserStore();
  const dictItems = userStore.getAllDictItems;
  if (null != dictItems && typeof dictItems === 'object' && dictItems[code]) {
    return dictItems[code];
  }
  //기존 쓰기 방식과 호환 가능
  if (getAuthCache(DB_DICT_DATA_KEY) && getAuthCache(DB_DICT_DATA_KEY)[code]) {
    return getAuthCache(DB_DICT_DATA_KEY)[code];
  }


};
/**
 * 사전 배열 가져오기
 * @param dict코드 사전코드
 * @return List<Map>
 */
export const initDictOptions = (code) => {
  //1.캐시에서 사전 구성 읽기 우선순위 지정
  if (getDictItemsByCode(code)) {
    return new Promise((resolve, reject) => {
      resolve(getDictItemsByCode(code));
    });
  }
  //2.사전 배열 가져오기
  if (code.indexOf(',') > 0 && code.indexOf(' ') > 0) {
    // 인코딩 후에는 sys_user%20where%20username%20like%20xxx와 유사합니다.' 공백이 포함되어 있지 않습니다. 공백과 쉼표가 있으면 인코딩 처리가 필요하다는 의미로 판단됩니다.
    code = encodeURI(code);
  }
  return defHttp.get({ url: `/api/v1/kkumz/sys/dict/getDictItems/${code}` });
};
/**
 * 사전 배열 가져오기
 * @param 코드 사전코드
 * @param params 쿼리 매개변수
 * @param 옵션 쿼리 구성
 * @return List<Map>
 */
export const ajaxGetDictItems = (code, params, options?) => defHttp.get({ url: `/api/v1/kkumz/sys/dict/getDictItems/${code}`, params }, options);
