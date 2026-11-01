import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/api/v1/kkumz/sys/category/rootList',
  save = '/api/v1/kkumz/sys/category/add',
  edit = '/api/v1/kkumz/sys/category/edit',
  deleteCategory = '/api/v1/kkumz/sys/category/delete',
  deleteBatch = '/api/v1/kkumz/sys/category/deleteBatch',
  importExcel = '/api/v1/kkumz/sys/category/importExcel',
  exportXls = '/api/v1/kkumz/sys/category/exportXls',
  loadTreeData = '/api/v1/kkumz/sys/category/loadTreeRoot',
  getChildList = '/api/v1/kkumz/sys/category/childList',
  getChildListBatch = '/api/v1/kkumz/sys/category/getChildListBatch',
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
 * 목록 인터페이스
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });
/**
 * 삭제
 */
export const deleteCategory = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteCategory, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * 일괄 삭제
 * @param params
 */
export const batchDeleteCategory = (params, handleSuccess) => {
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
 * 저장 또는 업데이트
 * @param params
 */
export const saveOrUpdateDict = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};
/**
 * 모든 트리 노드 데이터 쿼리
 * @param params
 */
export const loadTreeData = (params) => defHttp.get({ url: Api.loadTreeData, params });
/**
 * 하위 노드 데이터 쿼리
 * @param params
 */
export const getChildList = (params) => defHttp.get({ url: Api.getChildList, params });
/**
 * 하위 노드 데이터를 일괄 쿼리
 * @param params
 */
export const getChildListBatch = (params) => defHttp.get({ url: Api.getChildListBatch, params }, { isTransformResponse: false });
