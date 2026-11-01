import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';
enum Api {
  list = '/api/v1/kkumz/sys/dict/list',
  save = '/api/v1/kkumz/sys/dict/add',
  edit = '/api/v1/kkumz/sys/dict/edit',
  duplicateCheck = '/api/v1/kkumz/sys/duplicate/check',
  deleteDict = '/api/v1/kkumz/sys/dict/delete',
  deleteBatch = '/api/v1/kkumz/sys/dict/deleteBatch',
  importExcel = '/api/v1/kkumz/sys/dict/importExcel',
  exportXls = '/api/v1/kkumz/sys/dict/exportXls',
  recycleBinList = '/api/v1/kkumz/sys/dict/deleteList',
  putRecycleBin = '/api/v1/kkumz/sys/dict/back',
  deleteRecycleBin = '/api/v1/kkumz/sys/dict/deletePhysic',
  itemList = '/api/v1/kkumz/sys/dictItem/list',
  deleteItem = '/api/v1/kkumz/sys/dictItem/delete',
  itemSave = '/api/v1/kkumz/sys/dictItem/add',
  itemEdit = '/api/v1/kkumz/sys/dictItem/edit',
  dictItemCheck = '/api/v1/kkumz/sys/dictItem/dictItemCheck',
  refreshCache = '/api/v1/kkumz/sys/dict/refleshCache',
  queryAllDictItems = '/api/v1/kkumz/sys/dict/queryAllDictItems',
}
/**
 * API 내보내기
 * @param params
 */
export const getExportUrl = Api.exportXls;
/**
 * API 가져오기
 * @param params
 */
export const getImportUrl = Api.importExcel;
/**
 * 사전 목록 인터페이스
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });
/**
 * 삭제사전
 */
export const deleteDict = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteDict, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * 일괄 삭제사전
 * @param params
 */
export const batchDeleteDict = (params, handleSuccess) => {
  Modal.confirm({
    title: '삭제 확인',
    content: '선택한 데이터 삭제 여부',
    okText: '확인',
    cancelText: '취소',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};
/**
 * 사전 저장 또는 업데이트
 * @param params
 */
export const saveOrUpdateDict = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};
/**
 * 고유한 검증
 * @param params
 */
export const duplicateCheck = (params) => defHttp.get({ url: Api.duplicateCheck, params }, { isTransformResponse: false });
/**
 * 사전 삭제 목록
 * @param params
 */
export const getRecycleBinList = (params) => defHttp.get({ url: Api.recycleBinList, params });
/**
 * 삭제복원
 * @param params
 */
export const putRecycleBin = (id, handleSuccess) => {
  return defHttp.put({ url: Api.putRecycleBin + `/${id}` }).then(() => {
    handleSuccess();
  });
};
/**
 * 삭제삭제
 * @param params
 */
export const deleteRecycleBin = (id, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteRecycleBin + `/${id}` }).then(() => {
    handleSuccess();
  });
};
/**
 * 사전 구성목록
 * @param params
 */
export const itemList = (params) => defHttp.get({ url: Api.itemList, params });
/**
 * 사전 구성삭제
 * @param params
 */
export const deleteItem = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteItem, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * 사전 구성 저장 또는 업데이트
 * @param params
 */
export const saveOrUpdateDictItem = (params, isUpdate) => {
  let url = isUpdate ? Api.itemEdit : Api.itemSave;
  return defHttp.post({ url: url, params });
};
/**
 * 사전 데이터 값 확인
 * @param params
 */
export const dictItemCheck = (params) => defHttp.get({ url: Api.dictItemCheck, params }, { isTransformResponse: false });
/**
 * 사전 새로 고침
 * @param params
 */
export const refreshCache = () => defHttp.get({ url: Api.refreshCache }, { isTransformResponse: false });
/**
 * 모든 사전 항목 가져오기
 * @param params
 */
export const queryAllDictItems = () => defHttp.get({ url: Api.queryAllDictItems }, { isTransformResponse: false });
