import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/api/v1/kkumz/sys/checkRule/list',
  delete = '/api/v1/kkumz/sys/checkRule/delete',
  deleteBatch = '/api/v1/kkumz/sys/checkRule/deleteBatch',
  exportXls = 'sys/checkRule/exportXls',
  importXls = 'sys/checkRule/importExcel',
  checkByCode = '/api/v1/kkumz/sys/checkRule/checkByCode',
  save = '/api/v1/kkumz/sys/checkRule/add',
  edit = '/api/v1/kkumz/sys/checkRule/edit',
}

/**
 * 수출 주소
 */
export const exportUrl = Api.exportXls;
/**
 * 수입주소
 */
export const importUrl = Api.importXls;

/**
 * 목록 쿼리
 * @param params
 */
export const getCheckRuleList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 삭제
 * @param params
 * @param handleSuccess
 */
export const deleteCheckRule = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * 일괄 삭제
 * @param params
 */
export const batchDeleteCheckRule = (params, handleSuccess) => {
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
 * 코드 검증 규칙 코드에 따라 들어오는 값이 합법적인지 확인합니다.
 * @param ruleCode
 * @param value
 */
export const validateCheckRule = (ruleCode, value) => {
  value = encodeURIComponent(value);
  let params = { ruleCode, value };
  return defHttp.get({ url: Api.checkByCode, params }, { isTransformResponse: false });
};

/**
 * 저장
 * @param params
 */
export const saveCheckRule = (params) => {
  return defHttp.post({ url: Api.save, params });
};

/**
 * 고쳐 쓰다
 * @param params
 */
export const updateCheckRule = (params) => {
  return defHttp.put({ url: Api.edit, params });
};
