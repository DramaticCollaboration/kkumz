import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/api/v1/kkumz/sys/role/list',
  save = '/api/v1/kkumz/sys/role/add',
  edit = '/api/v1/kkumz/sys/role/edit',
  deleteRole = '/api/v1/kkumz/sys/role/delete',
  deleteBatch = '/api/v1/kkumz/sys/role/deleteBatch',
  exportXls = '/api/v1/kkumz/sys/role/exportXls',
  importExcel = '/api/v1/kkumz/sys/role/importExcel',
  isRoleExist = '/api/v1/kkumz/sys/role/checkRoleCode',
  queryTreeListForRole = '/api/v1/kkumz/sys/permission/queryTreeList',
  queryRolePermission = '/api/v1/kkumz/sys/permission/queryRolePermission',
  saveRolePermission = '/api/v1/kkumz/sys/permission/saveRolePermission',
  queryDataRule = '/api/v1/kkumz/sys/role/datarule',
  getParentDesignList = '/api/v1/kkumz/act/process/extActDesignFlowData/getDesFormFlows',
  getRoleDegisnList = '/api/v1/kkumz/joa/designform/designFormCommuse/getRoleDegisnList',
  saveRoleDesign = '/api/v1/kkumz/joa/designform/designFormCommuse/sysRoleDesignAdd',
  userList = '/api/v1/kkumz/sys/user/userRoleList',
  deleteUserRole = '/api/v1/kkumz/sys/user/deleteUserRole',
  batchDeleteUserRole = '/api/v1/kkumz/sys/user/deleteUserRoleBatch',
  addUserRole = '/api/v1/kkumz/sys/user/addSysUserRole',
  saveRoleIndex = '/api/v1/kkumz/sys/sysRoleIndex/add',
  editRoleIndex = '/api/v1/kkumz/sys/sysRoleIndex/edit',
  queryIndexByCode = '/api/v1/kkumz/sys/sysRoleIndex/queryByCode',
}
/**
 * API 내보내기
 */
export const getExportUrl = Api.exportXls;
/**
 * API 가져오기
 */
export const getImportUrl = Api.importExcel;
/**
 * 시스템 역할 목록
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * 삭제역할
 */
export const deleteRole = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteRole, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * 일괄 삭제역할
 * @param params
 */
export const batchDeleteRole = (params, handleSuccess) => {
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
 * 역할 저장 또는 업데이트
 * @param params
 */
export const saveOrUpdateRole = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};
/**
 * 코드 검증
 * @param params
 */
let timer;
export const isRoleExist = (params) => {
  return new Promise((resolve, rejected) => {
    clearTimeout(timer);
    timer = setTimeout(() => {
      defHttp
        .get({ url: Api.isRoleExist, params }, { isTransformResponse: false })
        .then((res) => {
          resolve(res);
        })
        .catch((error) => {
          rejected(error);
        });
    }, 500);
  });
};
/**
 * 역할에 따른 쿼리 트리 정보
 */
export const queryTreeListForRole = () => defHttp.get({ url: Api.queryTreeListForRole });
/**
 * 쿼리 역할 권한
 */
export const queryRolePermission = (params) => defHttp.get({ url: Api.queryRolePermission, params });
/**
 * 역할 권한 저장
 */
export const saveRolePermission = (params) => defHttp.post({ url: Api.saveRolePermission, params });
/**
 * 쿼리 역할 데이터 규칙
 */
export const queryDataRule = (params) =>
  defHttp.get({ url: `${Api.queryDataRule}/${params.functionId}/${params.roleId}` }, { isTransformResponse: false });
/**
 * 역할 데이터 규칙 저장
 */
export const saveDataRule = (params) => defHttp.post({ url: Api.queryDataRule, params });
/**
 * Form 데이터 가져오기
 * @return List<Map>
 */
export const getParentDesignList = () => defHttp.get({ url: Api.getParentDesignList });
/**
 * 역할 Form 데이터 가져오기
 * @return List<Map>
 */
export const getRoleDegisnList = (params) => defHttp.get({ url: Api.getRoleDegisnList, params });
/**
 * 역할 티켓 정보 제출
 */
export const saveRoleDesign = (params) => defHttp.post({ url: Api.saveRoleDesign, params });
/**
 * 역할 목록 인터페이스
 * @param params
 */
export const userList = (params) => defHttp.get({ url: Api.userList, params });
/**
 * 역할 사용자 삭제
 */
export const deleteUserRole = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteUserRole, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * 일괄적으로 역할 사용자 삭제
 * @param params
 */
export const batchDeleteUserRole = (params, handleSuccess) => {
  Modal.confirm({
    title: '삭제 확인',
    content: '선택한 데이터 삭제 여부',
    okText: '확인',
    cancelText: '취소',
    onOk: () => {
      return defHttp.delete({ url: Api.batchDeleteUserRole, params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};
/**
 * 기존 사용자 추가
 */
export const addUserRole = (params, handleSuccess) => {
  return defHttp.post({ url: Api.addUserRole, params }).then(() => {
    handleSuccess();
  });
};
/**
 * 저장 또는 업데이트
 * @param params
 * @param isUpdate 업데이트된 데이터인가요?
 */
export const saveOrUpdateRoleIndex = (params, isUpdate) => {
  let url = isUpdate ? Api.editRoleIndex : Api.saveRoleIndex;
  return defHttp.post({ url: url, params });
};
/**
 * 코드를 기반으로 홈 페이지 구성 쿼리
 * @param params
 */
export const queryIndexByCode = (params) => defHttp.get({ url: Api.queryIndexByCode, params }, { isTransformResponse: false });
