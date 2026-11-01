import { defHttp } from '/@/utils/http/axios';
import { getMenuListResultModel } from './model/menuModel';

enum Api {
  GetMenuList = '/api/v1/kkumz/sys/permission/getUserPermissionByToken',
}

/**
 * @description: Get user menu based on id
 */

export const getMenuList = () => {
  return new Promise((resolve) => {
    defHttp.get<getMenuListResultModel>({ url: Api.GetMenuList }).then((res) => {
      if (Array.isArray(res)) {
        resolve(res);
      } else {
        resolve(res['menu']);
      }
    });
  });
};

/**
 * @description: 배경 메뉴 권한 및 버튼 권한 얻기
 */
export function getBackMenuAndPerms() {
  return defHttp.get({ url: Api.GetMenuList });
}
