import { defHttp } from '/@/utils/http/axios';

enum Api {
  listCementByUser = '/api/v1/kkumz/sys/annountCement/listByUser',
  getUnreadMessageCount = '/api/v1/kkumz/sys/annountCement/getUnreadMessageCount',
  editCementSend = '/api/v1/kkumz/sys/sysAnnouncementSend/editByAnntIdAndUserId',
  clearAllUnReadMessage = '/api/v1/kkumz/sys/annountCement/clearAllUnReadMessage',
}

/**
 * 시스템 알림 메시지 목록 가져오기
 * @param params
 */
export const listCementByUser = (params?) => defHttp.get({ url: Api.listCementByUser, params });

/**
 * 지난 2개월 동안 사용자로부터 읽지 않은 메시지 수를 가져옵니다.
 * @param params
 */
export const getUnreadMessageCount = (params?) => defHttp.get({ url: Api.getUnreadMessageCount, params });

export const editCementSend = (anntId, params?) => defHttp.put({ url: Api.editCementSend, params: { anntId, ...params } });

/**
 * 읽지 않은 메시지 모두 지우기
 */
export const clearAllUnReadMessage = () => defHttp.post({ url: Api.clearAllUnReadMessage },{ isTransformResponse: false });
