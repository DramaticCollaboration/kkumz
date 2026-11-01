/* JVxeTable 행 편집 권한 */
import { usePermissionStoreWithOut } from '/@/store/modules/permission';

const permissionStore = usePermissionStoreWithOut();

/**
 * JVxe 독점, 권한 획득
 * @param prefix
 */
export function getJVxeAuths(prefix) {
  prefix = getPrefix(prefix);
  let { authList, allAuthList } = permissionStore;
  let authsMap = new Map<string, typeof allAuthList[0]>();
  if (!prefix || prefix.length == 0) {
    return authsMap;
  }
  // vxe에서 사용하는 모든 권한을 제거합니다.
  for (let auth of allAuthList) {
    if (auth.status == '1' && (auth.action || '').startsWith(prefix)) {
      authsMap.set(auth.action, { ...auth, isAuth: false });
    }
  }
  // 설정 승인 여부를 설정합니다.
  for (let auth of authList) {
    let getAuth = authsMap.get(auth.action);
    if (getAuth != null) {
      getAuth.isAuth = true;
    }
  }
  let onlineButtonAuths = permissionStore.getOnlineSubTableAuth(prefix);
  if (onlineButtonAuths && onlineButtonAuths.length > 0) {
    for (let auth of onlineButtonAuths) {
      authsMap.set(prefix + 'btn:' + auth, { action: auth, type: 1, status: 1, isAuth: false });
    }
  }
  return authsMap;
}

/**
 * 접두사 가져오기
 * @param prefix
 */
export function getPrefix(prefix: string) {
  if (prefix && !prefix.endsWith(':')) {
    return prefix + ':';
  }
  return prefix;
}
