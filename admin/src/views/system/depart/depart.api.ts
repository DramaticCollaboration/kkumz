import { unref } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

export enum Api {
  queryDepartTreeSync = '/api/v1/kkumz/sys/sysDepart/queryDepartTreeSync',
  save = '/api/v1/kkumz/sys/sysDepart/add',
  edit = '/api/v1/kkumz/sys/sysDepart/edit',
  delete = '/api/v1/kkumz/sys/sysDepart/delete',
  deleteBatch = '/api/v1/kkumz/sys/sysDepart/deleteBatch',
  exportXlsUrl = '/api/v1/kkumz/sys/sysDepart/exportXls',
  importExcelUrl = '/api/v1/kkumz/sys/sysDepart/importExcel',

  roleQueryTreeList = '/api/v1/kkumz/sys/permission/queryTreeList',
  queryDepartPermission = '/api/v1/kkumz/sys/permission/queryDepartPermission',
  saveDepartPermission = '/api/v1/kkumz/sys/permission/saveDepartPermission',

  dataRule = '/api/v1/kkumz/sys/sysDepartPermission/datarule',

  getCurrentUserDeparts = '/api/v1/kkumz/sys/user/getCurrentUserDeparts',
  selectDepart = '/api/v1/kkumz/sys/selectDepart',
  getUpdateDepartInfo = '/api/v1/kkumz/sys/user/getUpdateDepartInfo',
  doUpdateDepartInfo = '/api/v1/kkumz/sys/user/doUpdateDepartInfo',
  changeDepartChargePerson = '/api/v1/kkumz/sys/user/changeDepartChargePerson',
}

/**
 * 부서 트리 목록 가져오기
 */
export const queryDepartTreeSync = (params?) => defHttp.get({ url: Api.queryDepartTreeSync, params });

/**
 * 부서 역할 저장 또는 업데이트
 */
export const saveOrUpdateDepart = (params, isUpdate) => {
  if (isUpdate) {
    return defHttp.put({ url: Api.edit, params });
  } else {
    return defHttp.post({ url: Api.save, params });
  }
};

/**
 * 부서 역할을 일괄 삭제
 */
export const deleteBatchDepart = (params, confirm = false) => {
  return new Promise((resolve, reject) => {
    const doDelete = () => {
      resolve(defHttp.delete({ url: Api.deleteBatch, params }, { joinParamsToUrl: true }));
    };
    if (confirm) {
      createConfirm({
        iconType: 'warning',
        title: '삭제',
        content: '삭제하시겠습니까?',
        onOk: () => doDelete(),
        onCancel: () => reject(),
      });
    } else {
      doDelete();
    }
  });
};

/**
 * 권한 트리 목록 가져오기
 */
export const queryRoleTreeList = (params?) => defHttp.get({ url: Api.roleQueryTreeList, params });
/**
 * 부서 권한 쿼리
 */
export const queryDepartPermission = (params?) => defHttp.get({ url: Api.queryDepartPermission, params });
/**
 * 부서 권한 저장
 */
export const saveDepartPermission = (params) => defHttp.post({ url: Api.saveDepartPermission, params });

/**
 *  부서 데이터 권한 목록 쿼리
 */
export const queryDepartDataRule = (functionId, departId, params?) => {
  let url = `${Api.dataRule}/${unref(functionId)}/${unref(departId)}`;
  return defHttp.get({ url, params });
};
/**
 * 부서정보 저장을 위한 권한
 */
export const saveDepartDataRule = (params) => defHttp.post({ url: Api.dataRule, params });
/**
 * 로그인한 사용자 부서 정보 가져오기
 */
export const getUserDeparts = (params?) => defHttp.get({ url: Api.getCurrentUserDeparts, params });
/**
 * 부서선택으로 전환
 */
export const selectDepart = (params?) => defHttp.put({ url: Api.selectDepart, params });

/**
 * 부서를 편집하기 전에 부서 관련 정보를 얻으십시오.
 * @param id
 */
export const getUpdateDepartInfo = (id) => defHttp.get({ url: Api.getUpdateDepartInfo, params: {id} });

/**
 * 편집부서
 * @param params
 */
export const doUpdateDepartInfo = (params) => defHttp.put({ url: Api.doUpdateDepartInfo, params });

/**
 * 삭제부서
 * @param id
 */
export const deleteDepart = (id) => defHttp.delete({ url: Api.delete, params:{ id } }, { joinParamsToUrl: true });

/**
 * 담당자 설정 담당자 취소
 * @param params
 */
export const changeDepartChargePerson = (params) => defHttp.put({ url: Api.changeDepartChargePerson, params });
