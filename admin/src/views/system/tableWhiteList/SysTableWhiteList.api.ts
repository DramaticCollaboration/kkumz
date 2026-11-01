import {defHttp} from '/@/utils/http/axios';
import {Modal} from 'ant-design-vue';

enum Api {
  list = '/api/v1/kkumz/sys/tableWhiteList/list',
  save = '/api/v1/kkumz/sys/tableWhiteList/add',
  edit = '/api/v1/kkumz/sys/tableWhiteList/edit',
  deleteOne = '/api/v1/kkumz/sys/tableWhiteList/delete',
  deleteBatch = '/api/v1/kkumz/sys/tableWhiteList/deleteBatch',
  importExcel = '/api/v1/kkumz/sys/tableWhiteList/importExcel',
  exportXls = '/api/v1/kkumz/sys/tableWhiteList/exportXls',
}

/**
 * API 내보내기
 * @param params
 */
export const getExportUrl = Api.exportXls;
/**
 * API 가져오기
 */
export const getImportUrl = Api.importExcel;
/**
 * 목록 인터페이스
 * @param params
 */
export const list = (params) =>
  defHttp.get({url: Api.list, params});

/**
 * 삭제하나개인
 * @param params
 * @param handleSuccess
 */
export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}
/**
 * 일괄 삭제
 * @param params
 * @param handleSuccess
 */
export const batchDelete = (params, handleSuccess) => {
  Modal.confirm({
    title: '삭제 확인',
    content: '선택한 데이터 삭제 여부',
    okText: '확인',
    cancelText: '취소',
    onOk: () => {
      return defHttp.delete({
        url: Api.deleteBatch,
        data: params
      }, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}
/**
 * 저장 또는 업데이트
 * @param params
 * @param isUpdate 업데이트된 데이터인가요?
 */
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({url: url, params});
}
