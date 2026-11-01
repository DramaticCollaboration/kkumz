import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/api/v1/kkumz/sys/position/list',
  save = '/api/v1/kkumz/sys/position/add',
  edit = '/api/v1/kkumz/sys/position/edit',
  get = '/api/v1/kkumz/sys/position/queryById',
  delete = '/api/v1/kkumz/sys/position/delete',
  importExcel = '/api/v1/kkumz/sys/position/importExcel',
  exportXls = '/api/v1/kkumz/sys/position/exportXls',
  deleteBatch = '/api/v1/kkumz/sys/position/deleteBatch',
}
/**
 * API 내보내기
 */
export const getExportUrl = Api.exportXls;

export const getImportUrl = Api.importExcel;
/**
 * 쿼리목록
 * @param params
 */
export const getPositionList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 저장 또는 업데이트
 * @param params
 */
export const saveOrUpdatePosition = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};

/**
 * 쿼리상세
 * @param params
 */
export const getPositionById = (params) => {
  return defHttp.get({ url: Api.get, params });
};

/**
 * 단일 항목 삭제
 * @param params
 */
export const deletePosition = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * 일괄 삭제
 * @param params
 */
export const batchDeletePosition = (params, handleSuccess) => {
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
 * 맞춤 업로드
 * @param customUpload
 */
export const customUpload = (params) => {
  defHttp.uploadFile({ url: Api.importExcel }, params);
};
