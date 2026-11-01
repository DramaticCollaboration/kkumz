import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/api/v1/kkumz/sys/user/list',
  save = '/api/v1/kkumz/sys/user/add',
  edit = '/api/v1/kkumz/sys/user/edit',
  agentSave = '/api/v1/kkumz/sys/sysUserAgent/add',
  agentEdit = '/api/v1/kkumz/sys/sysUserAgent/edit',
  getUserRole = '/api/v1/kkumz/sys/user/queryUserRole',
  duplicateCheck = '/api/v1/kkumz/sys/duplicate/check',
  deleteUser = '/api/v1/kkumz/sys/user/delete',
  deleteBatch = '/api/v1/kkumz/sys/user/deleteBatch',
  importExcel = '/api/v1/kkumz/sys/user/importExcel',
  exportXls = '/api/v1/kkumz/sys/user/exportXls',
  recycleBinList = '/api/v1/kkumz/sys/user/recycleBin',
  putRecycleBin = '/api/v1/kkumz/sys/user/putRecycleBin',
  deleteRecycleBin = '/api/v1/kkumz/sys/user/deleteRecycleBin',
  allRolesList = '/api/v1/kkumz/sys/role/queryall',
  allPostList = '/api/v1/kkumz/sys/position/list',
  userDepartList = '/api/v1/kkumz/sys/user/userDepartList',
  changePassword = '/api/v1/kkumz/sys/user/changePassword',
  frozenBatch = '/api/v1/kkumz/sys/user/frozenBatch',
  getUserAgent = '/api/v1/kkumz/sys/sysUserAgent/queryByUserName',
  userQuitAgent = '/api/v1/kkumz/sys/user/userQuitAgent',
  getQuitList = '/api/v1/kkumz/sys/user/getQuitList',
  putCancelQuit = '/api/v1/kkumz/sys/user/putCancelQuit',
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
 * 목록 인터페이스(사용자를 쿼리합니다.)
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * 사용자 역할 인터페이스
 * @param params
 */
export const getUserRoles = (params) => defHttp.get({ url: Api.getUserRole, params }, { errorMessageMode: 'none' });

/**
 * 삭제사용자
 */
export const deleteUser = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteUser, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * 일괄 삭제사용자
 * @param params
 */
export const batchDeleteUser = (params, handleSuccess) => {
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
 * 사용자 저장 또는 업데이트
 * @param params
 */
export const saveOrUpdateUser = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};
/**
 * 고유한 검증
 * @param params
 */
export const duplicateCheck = (params) => defHttp.get({ url: Api.duplicateCheck, params }, { isTransformResponse: false });

/**
 * 20231215
 * liaozhiyang
 * 고유한 검증（ 지연【흔들림 방지】）
 * @param params
 */
let timer;
export const duplicateCheckDelay = (params) => {
  return new Promise((resove, rejected) => {
    clearTimeout(timer);
    timer = setTimeout(() => {
      defHttp
        .get({ url: Api.duplicateCheck, params }, { isTransformResponse: false })
        .then((res: any) => {
          resove(res as any);
        })
        .catch((error) => {
          rejected(error);
        });
    }, 500);
  });
};
/**
 * 모든 역할 가져오기
 * @param params
 */
export const getAllRolesList = (params) => defHttp.get({ url: Api.allRolesList, params });
/**
 * 지정된 사용자 담당부서 확보
 */
export const getUserDepartList = (params) => defHttp.get({ url: Api.userDepartList, params }, { successMessageMode: 'none' });
/**
 * 모든 직업을 얻으세요
 */
export const getAllPostList = (params) => {
  return new Promise((resolve) => {
    defHttp.get({ url: Api.allPostList, params }).then((res) => {
      resolve(res.records);
    });
  });
};
/**
 * 삭제목록
 * @param params
 */
export const getRecycleBinList = (params) => defHttp.get({ url: Api.recycleBinList, params });
/**
 * 삭제복원
 * @param params
 */
export const putRecycleBin = (params, handleSuccess) => {
  return defHttp.put({ url: Api.putRecycleBin, params }).then(() => {
    handleSuccess();
  });
};
/**
 * 삭제삭제
 * @param params
 */
export const deleteRecycleBin = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteRecycleBin, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * 비밀번호 변경
 * @param params
 */
export const changePassword = (params) => {
  return defHttp.put({ url: Api.changePassword, params });
};
/**
 * 냉동 및 해동
 * @param params
 */
export const frozenBatch = (params, handleSuccess) => {
  return defHttp.put({ url: Api.frozenBatch, params }).then(() => {
    handleSuccess();
  });
};
/**
 * 사용자 에이전트 가져오기
 * @param params
 */
export const getUserAgent = (params) => defHttp.get({ url: Api.getUserAgent, params }, { isTransformResponse: false });
/**
 * 사용자 에이전트 저장 또는 업데이트
 * @param params
 */
export const saveOrUpdateAgent = (params) => {
  let url = params.id ? Api.agentEdit : Api.agentSave;
  return defHttp.post({ url: url, params });
};

/**
 * 사용자 종료(에이전트 및 사용자 상태 변경 작업이 추가되었습니다.)
 * @param params
 */
export const userQuitAgent = (params) => {
  return defHttp.put({ url: Api.userQuitAgent, params });
};

/**
 * 사용자 탈퇴 목록
 * @param params
 */
export const getQuitList = (params) => {
  return defHttp.get({ url: Api.getQuitList, params });
};

/**
 * 취소사직하다
 * @param params
 */
export const putCancelQuit = (params, handleSuccess) => {
  return defHttp.put({ url: Api.putCancelQuit, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
