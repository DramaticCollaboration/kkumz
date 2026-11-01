import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/api/v1/kkumz/sys/sysAnnouncementSend/getMyAnnouncementSend',
  editCementSend = '/api/v1/kkumz/sys/sysAnnouncementSend/editByAnntIdAndUserId',
  readAllMsg = '/api/v1/kkumz/sys/sysAnnouncementSend/readAll',
  syncNotic = '/api/v1/kkumz/sys/annountCement/syncNotic',
  getOne = '/api/v1/kkumz/sys/sysAnnouncementSend/getOne',
}

/**
 * 쿼리 메시지 목록
 * @param params
 */
export const getMyNewsList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 사용자 시스템 메시지 읽기 상태 업데이트
 * @param params
 */
export const editCementSend = (params) => {
  return defHttp.put({ url: Api.editCementSend, params });
};

/**
 * 한 번의 클릭으로 읽기
 * @param params
 */
export const readAllMsg = (params, handleSuccess) => {
  Modal.confirm({
    title: '조치 확인',
    content: '모두 읽음으로 표시되어 있습니까?',
    okText: '확인',
    cancelText: '취소',
    onOk: () => {
      return defHttp.put({ url: Api.readAllMsg, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};

/**
 * 메시지 동기화
 * @param params
 */
export const syncNotic = (params) => {
  return defHttp.get({ url: Api.syncNotic, params });
};

/**
 * 메시지 전송 기록 ID를 기반으로 메시지 내용 가져오기
 * @param sendId
 */
export const getOne = (sendId) => {
  return defHttp.get({ url: Api.getOne, params:{sendId} });
};
