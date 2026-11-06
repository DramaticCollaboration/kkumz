import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/api/v1/kkumz/sys/user/list',
  user = '/api/v1/kkumz/sys/user',
  save = '/api/v1/kkumz/sys/user',
  edit = '/api/v1/kkumz/sys/user',
  agentSave = '/api/v1/kkumz/sys/sysUserAgent/add',
  agentEdit = '/api/v1/kkumz/sys/sysUserAgent/edit',
  getUserRole = '/api/v1/kkumz/sys/user/queryUserRole',
  duplicateCheck = '/api/v1/kkumz/sys/duplicate/check',
  deleteUser = '/api/v1/kkumz/sys/user',
  deleteBatch = '/api/v1/kkumz/sys/user/deleteBatch',
  importExcel = '/api/v1/kkumz/sys/user/importExcel',
  exportXls = '/api/v1/kkumz/sys/user/exportXls',
  recycleBinList = '/api/v1/kkumz/sys/user/recycleBin',
  putRecycleBin = '/api/v1/kkumz/sys/user/putRecycleBin',
  deleteRecycleBin = '/api/v1/kkumz/sys/user/deleteRecycleBin',
  allRolesList = '/api/v1/kkumz/sys/role/list',
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
export const getUserRoles = async (params) => {
  try {
    const res = await defHttp.get({ url: Api.list, params: { pageNo: 1, pageSize: 200 } }, { errorMessageMode: 'none' });
    const records = Array.isArray(res) ? res : res?.records || [];
    const matched = records.find((u: any) => String(u.id) === String(params?.userid));
    if (matched && Array.isArray(matched.roles)) {
      return matched.roles.map((r: any) => r.id || r);
    }
    return [];
  } catch {
    return [];
  }
};

/**
 * 삭제사용자
 */
export const deleteUser = (params, handleSuccess) => {
  return defHttp.delete({ url: `${Api.user}/${params.id}` }, { successMessageMode: 'success' }).then(() => {
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
    onOk: async () => {
      const ids = String(params?.ids || '')
        .split(',')
        .map((id) => id.trim())
        .filter(Boolean);
      for (const id of ids) {
        await defHttp.delete({ url: `${Api.user}/${id}` }, { successMessageMode: 'none' });
      }
      handleSuccess();
    },
  });
};
/**
 * 사용자 저장 또는 업데이트
 * @param params
 */
export const saveOrUpdateUser = (params, isUpdate) => {
  const payload = {
    ...params,
    roleIds: Array.isArray(params?.selectedroles)
      ? params.selectedroles
      : typeof params?.selectedroles === 'string' && params.selectedroles
      ? params.selectedroles.split(',').filter(Boolean)
      : params?.roleIds,
  };
  if (isUpdate) {
    return defHttp.put({ url: Api.user, params: payload }, { successMessageMode: 'success' });
  }
  return defHttp.post({ url: Api.user, params: payload }, { successMessageMode: 'success' });
};
/**
 * 고유한 검증
 * @param params
 */
export const duplicateCheck = async (params) => {
  try {
    const res = await defHttp.get({ url: Api.list, params: { pageNo: 1, pageSize: 200 } }, { errorMessageMode: 'none' });
    const records = Array.isArray(res) ? res : res?.records || [];
    const fieldName = params?.fieldName || 'username';
    const fieldVal = String(params?.fieldVal || '').trim();
    const dataId = params?.dataId ? String(params.dataId) : '';
    const exists = records.some(
      (item: any) => String(item?.[fieldName] || '').toLowerCase() === fieldVal.toLowerCase() && String(item?.id || '') !== dataId
    );
    return {
      success: !exists,
      code: exists ? 500 : 200,
      message: exists ? '이미 사용 중인 값입니다.' : '',
    };
  } catch {
    return { success: true, code: 200, message: '' };
  }
};

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
      duplicateCheck(params)
        .then((res: any) => {
          resove(res as any);
        })
        .catch((error) => {
          rejected(error);
        });
    }, 300);
  });
};
/**
 * 모든 역할 가져오기
 * @param params
 */
export const getAllRolesList = async (params) => {
  const res = await defHttp.get({ url: Api.allRolesList, params: { pageNo: 1, pageSize: 200, ...params } });
  return Array.isArray(res) ? res : res?.records || [];
};
/**
 * 지정된 사용자 담당부서 확보
 */
export const getUserDepartList = async (_params) => [];
/**
 * 모든 직업을 얻으세요
 */
export const getAllPostList = async (_params) => [];
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
