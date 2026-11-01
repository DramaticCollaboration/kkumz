import { getAuthCache, setAuthCache } from '/@/utils/auth';
/**
 * 요청 그룹화
 *
 * @param getPromise는 Promise 객체를 얻을 수 있는 메서드를 전달합니다.
 * @param groupId 그룹 ID, 전달되지 않거나 비어 있으면 그룹화가 수행되지 않습니다.
 * @param 만료 만료 시간, 기본값 30분
 */
export function httpGroupRequest(getPromise, groupId, expire = 1000 * 30) {
  if (groupId == null || groupId === '') {
    console.log('--------popup----------getFrom  DB-------with---no--groupId ');
    return getPromise();
  }

  if (getAuthCache(groupId)) {
    console.log('---------popup--------getFrom  Cache--------groupId = ' + groupId);
    return Promise.resolve(getAuthCache(groupId));
  } else {
    console.log('--------popup----------getFrom  DB---------groupId = ' + groupId);
  }

  // 요청이 발행되기 전에 첫 번째 요청이 발행됩니다.
  return getPromise().then((res) => {
    setAuthCache(groupId, res);
    return Promise.resolve(res);
  });
}
