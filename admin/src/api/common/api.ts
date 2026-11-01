import { defHttp } from '/@/utils/http/axios';
import { message } from 'ant-design-vue';
import { useGlobSetting } from '/@/hooks/setting';
const globSetting = useGlobSetting();
const baseUploadUrl = globSetting.uploadUrl;
enum Api {
  positionList = '/api/v1/kkumz/sys/position/list',
  userList = '/api/v1/kkumz/sys/user/list',
  roleList = '/api/v1/kkumz/sys/role/list',
  queryDepartTreeSync = '/api/v1/kkumz/sys/sysDepart/queryDepartTreeSync',
  queryTreeList = '/api/v1/kkumz/sys/sysDepart/queryTreeList',
  loadTreeData = '/api/v1/kkumz/sys/category/loadTreeData',
  loadDictItem = '/api/v1/kkumz/sys/category/loadDictItem',
  getDictItems = '/api/v1/kkumz/sys/dict/getDictItems',
  getTableList = '/api/v1/kkumz/sys/user/queryUserComponentData',
  getCategoryData = '/api/v1/kkumz/sys/category/loadAllData',
}

/**
 * 상위 경로 업로드
 */
export const uploadUrl = `${baseUploadUrl}/api/v1/kkumz/sys/common/upload`;

/**
 * 직업 목록
 * @param params
 */
export const getPositionList = (params) => {
  return defHttp.get({ url: Api.positionList, params });
};

/**
 * 사용자 목록
 * @param params
 */
export const getUserList = (params) => {
  return defHttp.get({ url: Api.userList, params });
};

/**
 * 역할 목록
 * @param params
 */
export const getRoleList = (params) => {
  return defHttp.get({ url: Api.roleList, params });
};

/**
 * 부서 트리 목록을 비동기식으로 가져오기
 */
export const queryDepartTreeSync = (params?) => {
  return defHttp.get({ url: Api.queryDepartTreeSync, params });
};
/**
 * 부서 트리 목록 가져오기
 */
export const queryTreeList = (params?) => {
  return defHttp.get({ url: Api.queryTreeList, params });
};

/**
 * 분류 트리 제어 로딩 노드
 */
export const loadTreeData = (params?) => {
  return defHttp.get({ url: Api.loadTreeData, params });
};

/**
 * 사전 코드에 따라 사전 텍스트 로드
 */
export const loadDictItem = (params?) => {
  return defHttp.get({ url: Api.loadDictItem, params });
};

/**
 * 사전 코드에 따라 사전 텍스트 로드
 */
export const getDictItems = (dictCode) => {
  return defHttp.get({ url: Api.getDictItems + dictCode }, { joinTime: false });
};
/**
 * 부서 사용자 모달 선택 목록 로딩 목록
 */
export const getTableList = (params) => {
  return defHttp.get({ url: Api.getTableList, params });
};
/**
 * 모든 분류 데이터 로드
 */
export const loadCategoryData = (params) => {
  return defHttp.get({ url: Api.getCategoryData, params });
};
/**
 * 파일 업로드
 */
export const uploadFile = (params, success) => {
  return defHttp.uploadFile({ url: uploadUrl }, params, { success });
};
/**
 * 파일 다운로드
 * @param URL 파일 경로
 * @param fileName 파일 이름
 * @param parameter
 * @returns {*}
 */
export const downloadFile = (url, fileName?, parameter?) => {
  return getFileblob(url, parameter).then((data) => {
    if (!data || data.size === 0) {
      message.warning('파일 다운로드 실패');
      return;
    }
    if (typeof window.navigator.msSaveBlob !== 'undefined') {
      window.navigator.msSaveBlob(new Blob([data]), fileName);
    } else {
      let url = window.URL.createObjectURL(new Blob([data]));
      let link = document.createElement('a');
      link.style.display = 'none';
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link); //전체 제거 요소 다운로드
      window.URL.revokeObjectURL(url); //블롭 객체 해제
    }
  });
};

/**
 * Excel 내보내기용 파일 다운로드
 * @param url
 * @param parameter
 * @returns {*}
 */
export const getFileblob = (url, parameter) => {
  return defHttp.get(
    {
      url: url,
      timeout: 60* 1000,
      params: parameter,
      responseType: 'blob',
    },
    { isTransformResponse: false }
  );
};

/**
 * 【댓글 기능에 사용】사용자 정의 파일 업로드 방법
 */
export const uploadMyFile = (url, data) => {
  return defHttp.uploadMyFile(url, data);
};
