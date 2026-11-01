import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/api/v1/kkumz/sys/fillRule/list',
  test = '/api/v1/kkumz/sys/fillRule/testFillRule',
  save = '/api/v1/kkumz/sys/fillRule/add',
  edit = '/api/v1/kkumz/sys/fillRule/edit',
  delete = '/api/v1/kkumz/sys/fillRule/delete',
  deleteBatch = '/api/v1/kkumz/sys/fillRule/deleteBatch',
  exportXls = '/api/v1/kkumz/sys/fillRule/exportXls',
  importExcel = '/api/v1/kkumz/sys/fillRule/importExcel',
}

/**
 * 수출 주소
 */
export const exportUrl = Api.exportXls;
/**
 * 수입주소
 */
export const importUrl = Api.importExcel;

/**
 * 목록 쿼리
 * @param params
 */
export const getFillRuleList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 삭제
 * @param params
 * @param handleSuccess
 */
export const deleteFillRule = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * 일괄 삭제
 * @param params
 */
export const batchDeleteFillRule = (params, handleSuccess) => {
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
 * 규칙 기능 테스트
 * @param params
 */
export const handleTest = (params) => {
  return defHttp.get({ url: Api.test, params }, { isTransformResponse: false });
};

/**
 * 저장
 * @param params
 */
export const saveFillRule = (params) => {
  return defHttp.post({ url: Api.save, params });
};

/**
 * 고쳐 쓰다
 * @param params
 */
export const updateFillRule = (params) => {
  return defHttp.put({ url: Api.edit, params });
};
