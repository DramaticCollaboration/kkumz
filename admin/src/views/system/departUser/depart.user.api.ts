import { unref } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  treeList = '/api/v1/kkumz/sys/sysDepart/queryMyDeptTreeList',
  queryIdTree = '/api/v1/kkumz/sys/sysDepart/queryIdTree',
  searchBy = '/api/v1/kkumz/sys/sysDepart/searchBy',
}

// 부서 사용자 API
enum DepartUserApi {
  list = '/api/v1/kkumz/sys/user/departUserList',
  link = '/api/v1/kkumz/sys/user/editSysDepartWithUser',
  unlink = '/api/v1/kkumz/sys/user/deleteUserInDepartBatch',
}

// 부서 역할 API
enum DepartRoleApi {
  list = '/api/v1/kkumz/sys/sysDepartRole/list',
  deleteBatch = '/api/v1/kkumz/sys/sysDepartRole/deleteBatch',
  save = '/api/v1/kkumz/sys/sysDepartRole/add',
  edit = '/api/v1/kkumz/sys/sysDepartRole/edit',
  queryTreeListForDeptRole = '/api/v1/kkumz/sys/sysDepartPermission/queryTreeListForDeptRole',
  queryDeptRolePermission = '/api/v1/kkumz/sys/sysDepartPermission/queryDeptRolePermission',
  saveDeptRolePermission = '/api/v1/kkumz/sys/sysDepartPermission/saveDeptRolePermission',
  dataRule = '/api/v1/kkumz/sys/sysDepartRole/datarule',
  getDeptRoleList = '/api/v1/kkumz/sys/sysDepartRole/getDeptRoleList',
  getDeptRoleByUserId = '/api/v1/kkumz/sys/sysDepartRole/getDeptRoleByUserId',
  saveDeptRoleUser = '/api/v1/kkumz/sys/sysDepartRole/deptRoleUserAdd',
}

/**
 * 부서 트리 목록 가져오기
 */
export const queryMyDepartTreeList = (params?) => defHttp.get({ url: Api.treeList, params }, { isTransformResponse: false });

/**
 * 데이터를 쿼리하고 트리 구조의 모든 부서 이름을 로드합니다.
 */
export const queryIdTree = (params?) => defHttp.get({ url: Api.queryIdTree, params });

/**
 * 키워드로 부서 검색
 */
export const searchByKeywords = (params) => defHttp.get({ url: Api.searchBy, params });

/**
 * 부서별 사용자 정보 조회
 */
export const departUserList = (params) => defHttp.get({ url: DepartUserApi.list, params });

/**
 * 부서와 사용자 간의 관계를 일괄 추가
 *
 * @param 출발 ID 부서 ID
 * @param userIdList 사용자 ID 목록
 */
export const linkDepartUserBatch = (departId: string, userIdList: string[]) =>
  defHttp.post({ url: DepartUserApi.link, params: { depId: departId, userIdList } });

/**
 * 부서와 사용자 간의 연결을 일괄 취소합니다.
 */
export const unlinkDepartUserBatch = (params, confirm = false) => {
  return new Promise((resolve, reject) => {
    const doDelete = () => {
      resolve(defHttp.delete({ url: DepartUserApi.unlink, params }, { joinParamsToUrl: true }));
    };
    if (confirm) {
      createConfirm({
        iconType: 'warning',
        title: '연결 해제',
        content: '연결을 취소하시겠습니까?',
        onOk: () => doDelete(),
        onCancel: () => reject(),
      });
    } else {
      doDelete();
    }
  });
};

/**
 * 부서 역할 정보 조회
 */
export const departRoleList = (params) => defHttp.get({ url: DepartRoleApi.list, params });

/**
 * 부서 역할 저장 또는 업데이트
 */
export const saveOrUpdateDepartRole = (params, isUpdate) => {
  if (isUpdate) {
    return defHttp.put({ url: DepartRoleApi.edit, params });
  } else {
    return defHttp.post({ url: DepartRoleApi.save, params });
  }
};

/**
 * 부서 역할을 일괄 삭제
 */
export const deleteBatchDepartRole = (params, confirm = false) => {
  return new Promise((resolve, reject) => {
    const doDelete = () => {
      resolve(defHttp.delete({ url: DepartRoleApi.deleteBatch, params }, { joinParamsToUrl: true }));
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
 * 사용자 역할 권한 부여 기능, 쿼리 메뉴 권한 트리
 */
export const queryTreeListForDeptRole = (params) => defHttp.get({ url: DepartRoleApi.queryTreeListForDeptRole, params });
/**
 * 쿼리 역할 승인
 */
export const queryDeptRolePermission = (params) => defHttp.get({ url: DepartRoleApi.queryDeptRolePermission, params });
/**
 * 역할 승인 저장
 */
export const saveDeptRolePermission = (params) => defHttp.post({ url: DepartRoleApi.saveDeptRolePermission, params });

/**
 *  부서 역할 데이터 권한 목록 쿼리
 */
export const queryDepartRoleDataRule = (functionId, departId, roleId, params?) => {
  let url = `${DepartRoleApi.dataRule}/${unref(functionId)}/${unref(departId)}/${unref(roleId)}`;
  return defHttp.get({ url, params });
};
/**
 * 부서 역할 데이터를 저장하기 위한 권한입니다.
 */
export const saveDepartRoleDataRule = (params) => defHttp.post({ url: DepartRoleApi.dataRule, params });
/**
 * 부서 역할 사용자 인증 쿼리
 */
export const queryDepartRoleUserList = (params) => defHttp.get({ url: DepartRoleApi.getDeptRoleList, params });
/**
 * userId를 기반으로 부서 역할 사용자 권한 쿼리
 */
export const queryDepartRoleByUserId = (params) => defHttp.get({ url: DepartRoleApi.getDeptRoleByUserId, params });
/**
 * 부서 역할 사용자 권한 저장
 */
export const saveDepartRoleUser = (params) => defHttp.post({ url: DepartRoleApi.saveDeptRoleUser, params });
