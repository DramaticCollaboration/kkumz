import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/api/v1/kkumz/sys/permission/list',
  permission = '/api/v1/kkumz/sys/permission',
  save = '/api/v1/kkumz/sys/permission',
  edit = '/api/v1/kkumz/sys/permission',
  delete = '/api/v1/kkumz/sys/permission',
  deleteBatch = '/api/v1/kkumz/sys/permission/deleteBatch',
  ruleList = '/api/v1/kkumz/sys/permission/queryPermissionRule',
  ruleSave = '/api/v1/kkumz/sys/permission/addPermissionRule',
  ruleEdit = '/api/v1/kkumz/sys/permission/editPermissionRule',
  ruleDelete = '/api/v1/kkumz/sys/permission/deletePermissionRule',
  checkPermDuplication = '/api/v1/kkumz/sys/permission/checkPermDuplication',
}

/**
 * 목록 인터페이스
 * @param params
 */
export const list = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 삭제메뉴
 */
export const deleteMenu = (params, handleSuccess) => {
  return defHttp.delete({ url: `${Api.permission}/${params.id}` }, { successMessageMode: 'success' }).then(() => {
    handleSuccess();
  });
};
/**
 * 일괄 삭제메뉴
 * @param params
 */
export const batchDeleteMenu = (params, handleSuccess) => {
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
        await defHttp.delete({ url: `${Api.permission}/${id}` }, { successMessageMode: 'none' });
      }
      handleSuccess();
    },
  });
};
/**
 * 메뉴 저장 또는 업데이트
 * @param params
 */
export const saveOrUpdateMenu = (params, isUpdate) => {
  if (isUpdate) {
    return defHttp.put({ url: Api.permission, params }, { successMessageMode: 'success' });
  }
  return defHttp.post({ url: Api.permission, params }, { successMessageMode: 'success' });
};
/**
 * 메뉴 데이터 권한 목록 인터페이스
 * @param params
 */
export const dataRuleList = (params) => defHttp.get({ url: Api.ruleList, params });
/**
 * 데이터 규칙 저장 또는 업데이트
 * @param params
 */
export const saveOrUpdateRule = (params, isUpdate) => {
  let url = isUpdate ? Api.ruleEdit : Api.ruleSave;
  return defHttp.post({ url: url, params }, { successMessageMode: 'success' });
};

/**
 * 데이터 권한 삭제
 */
export const deleteRule = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.ruleDelete, params }, { joinParamsToUrl: true, successMessageMode: 'success' }).then(() => {
    handleSuccess();
  });
};
/**
 * 코드를 기반으로 사전 값 가져오기
 * @param params
 */
export const ajaxGetDictItems = (params) => defHttp.get({ url: `/api/v1/kkumz/sys/dict/getDictItems/${params.code}` });

/**
 * 고유한 검증
 * @param params
 */
export const getCheckPermDuplication = async (_params) => ({
  success: true,
  code: 200,
  message: '',
});

/**
 * 메뉴가 있는지 확인
 * @param model
 * @param schema
 * @param required
 */
export const checkPermDuplication=(model, schema, required?)=>{
  return [
    {
      validator: (_, value) => {
        if (!required) {
          return Promise.resolve();
        }
        if (!value && required) {
          return Promise.reject(`$를 입력하세요.{schema.label}`);
        }
        return new Promise<void>((resolve, reject) => {
          getCheckPermDuplication({
            id: model.id,
            url:model.url,
            alwaysShow:model.alwaysShow
          }).then((res) => {
              res.success ? resolve() : reject(res.message || '확인 실패');
          }).catch((err) => {
              reject(err.message || '인증 실패');
          });
        });
      },
    },
  ];
}
