import { AppRouteModule } from '/@/router/types';
import type { MenuModule, Menu, AppRouteRecordRaw } from '/@/router/types';
import { findPath, treeMap } from '/@/utils/helper/treeHelper';
import { cloneDeep } from 'lodash-es';
import { isUrl } from '/@/utils/is';
import { RouteParams } from 'vue-router';
import { toRaw } from 'vue';

export function getAllParentPath<T = Recordable>(treeData: T[], path: string) {
  // 원본 코드
  // const menuList = findPath(treeData, (n) => n.path === path) as Menu[];
  // 숨겨진 메뉴를 포함하지 않는 경로를 먼저 일치시킵니다.
  let menuList = findMenuPath(treeData, path, false);
  // 일치하는 항목이 없으면 숨겨진 메뉴가 포함된 경로를 일치시킵니다.
  if(!(menuList?.length)) {
    menuList = findMenuPath(treeData, path, true)
  }
  return (menuList || []).map((item) => item.path);
}

/**
 * 메뉴 경로 찾기
 *
 * @param treeData
 * @param path
 * @param matchHide 숨겨진 메뉴와 일치할지 여부
 */
function findMenuPath<T = Recordable>(treeData: T[], path: string, matchHide: boolean) {
  return findPath(treeData, (n) => {
    // 숨겨진 메뉴는 매칭에 참여하지 않습니다.
    if(!matchHide && n.hideMenu) {
      return false;
    }
    return n.path === path
  }) as Menu[];
}

// 경로 처리
function joinParentPath(menus: Menu[], parentPath = '') {
  for (let index = 0; index < menus.length; index++) {
    const menu = menus[index];
    // https://next.router.vuejs.org/guide/essentials/nested-routes.html
    // Note that nested paths that start with / will be treated as a root path.
    // /로 시작하는 중첩 경로는 루트 경로로 처리됩니다.
    // This allows you to leverage the component nesting without having to use a nested URL.
    // 이를 통해 중첩된 URL을 사용하지 않고도 컴포넌트 중첩을 활용할 수 있습니다.
    if (!(menu.path.startsWith('/') || isUrl(menu.path))) {
      // path doesn't start with /, nor is it a url, join parent path
      // 경로가 / 또는 url로 시작하지 않습니다. 상위 경로를 추가하세요.
      menu.path = `${parentPath}/${menu.path}`;
    }
    if (menu?.children?.length) {
      joinParentPath(menu.children, menu.meta?.hidePathForChildren ? parentPath : menu.path);
    }
  }
}

// Parsing the menu module
export function transformMenuModule(menuModule: MenuModule): Menu {
  const { menu } = menuModule;

  const menuList = [menu];

  joinParentPath(menuList);
  return flatSingleChildMenus(menuList)[0];
}

// 경로를 메뉴로 변환
export function transformRouteToMenu(routeModList: AppRouteModule[], routerMapping = false) {
  if (!routeModList) {
    return [];
  }
  // lodash 딥카피 사용하기
  const cloneRouteModList = cloneDeep(routeModList);
  const routeList: AppRouteRecordRaw[] = [];

  // 라우팅 항목 수정
  cloneRouteModList.forEach((item) => {
    if (routerMapping && item.meta.hideChildrenInMenu && typeof item.redirect === 'string') {
      item.path = item.redirect;
    }

    if (item.meta?.single) {
      const realItem = item?.children?.[0];
      realItem && routeList.push(realItem);
    } else {
      routeList.push(item);
    }
  });
  // 트리의 지정된 구조를 추출합니다.
  const list = treeMap(routeList, {
    conversion: (node: AppRouteRecordRaw) => {
      const { meta: { title, hideMenu = false } = {} } = node;

      return {
        ...(node.meta || {}),
        meta: node.meta,
        name: title,
        hideMenu,
        alwaysShow:node.alwaysShow||false,
        path: node.path,
        ...(node.redirect ? { redirect: node.redirect } : {}),
      };
    },
  });
  // 경로 처리
  joinParentPath(list);
  return flatSingleChildMenus(cloneDeep(list));
}

/**
 * 하위 메뉴가 하나인 경우 상위 메뉴와 하위 메뉴를 하나로 합침
 * @param menus 
 */
export function flatSingleChildMenus(menus: Menu[]): Menu[] {
  if (!menus || !menus.length) {
    return [];
  }
  const result: Menu[] = [];

  for (const item of menus) {
    // 1. 하위 메뉴가 있는 경우 먼저 재귀적으로 처리
    if (item.children && item.children.length > 0) {
      item.children = flatSingleChildMenus(item.children);
    }

    // 2. 숨겨지지 않은(표시되는) 하위 메뉴 목록
    const visibleChildren = (item.children || []).filter((child) => !child.hideMenu);

    // 3. alwaysShow가 true가 아니고 표시되는 하위 메뉴가 정확히 1개인 경우 합치기
    const isAlwaysShow = item.alwaysShow || item.meta?.alwaysShow;
    if (!isAlwaysShow && visibleChildren.length === 1) {
      const singleChild = visibleChildren[0];
      const mergedItem: Menu = {
        ...item,
        ...singleChild,
        name: singleChild.name || singleChild.meta?.title || item.name,
        icon: singleChild.icon || singleChild.meta?.icon || item.icon || item.meta?.icon,
        meta: {
          ...(item.meta || {}),
          ...(singleChild.meta || {}),
          title: singleChild.name || singleChild.meta?.title || item.name,
          icon: singleChild.icon || singleChild.meta?.icon || item.icon || item.meta?.icon,
        },
        children: singleChild.children && singleChild.children.length > 0 ? singleChild.children : undefined,
      };
      result.push(mergedItem);
    } else {
      result.push(item);
    }
  }

  return result;
}

/**
 * config menu with given params
 */
const menuParamRegex = /(?::)([\s\S]+?)((?=\/)|$)/g;

export function configureDynamicParamsMenu(menu: Menu, params: RouteParams) {
  const { path, paramPath } = toRaw(menu);
  let realPath = paramPath ? paramPath : path;
  const matchArr = realPath.match(menuParamRegex);

  matchArr?.forEach((it) => {
    const realIt = it.substr(1);
    if (params[realIt]) {
      realPath = realPath.replace(`:${realIt}`, params[realIt] as string);
    }
  });
  // save original param path.
  if (!paramPath && matchArr && matchArr.length > 0) {
    menu.paramPath = path;
  }
  menu.path = realPath;
  // children
  menu.children?.forEach((item) => configureDynamicParamsMenu(item, params));
}
