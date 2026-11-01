import type { AppRouteModule, AppRouteRecordRaw } from '/@/router/types';
import type { Router, RouteRecordNormalized } from 'vue-router';

import { getParentLayout, LAYOUT, EXCEPTION_COMPONENT } from '/@/router/constant';
import { cloneDeep, omit } from 'lodash-es';
import { warn } from '/@/utils/log';
import { createRouter, createWebHashHistory } from 'vue-router';
import { getToken } from "/@/utils/auth";
import { URL_HASH_TAB, _eval } from '/@/utils';
//온라인 lib 라우팅 소개
import { packageViews } from '/@/utils/monorepo/dynamicRouter';
import {useI18n} from "/@/hooks/web/useI18n";

export type LayoutMapKey = 'LAYOUT';
const IFRAME = () => import('/@/views/sys/iframe/FrameBlank.vue');
const LayoutContent = () => import('/@/layouts/default/content/index.vue');

const LayoutMap = new Map<string, () => Promise<typeof import('*.vue')>>();

LayoutMap.set('LAYOUT', LAYOUT);
LayoutMap.set('IFRAME', IFRAME);
//마이크로 프론트엔드 qiankun
LayoutMap.set('LayoutsContent', LayoutContent);

let dynamicViewsModules: Record<string, () => Promise<Recordable>>;

// Dynamic introduction
function asyncImportRoute(routes: AppRouteRecordRaw[] | undefined) {
  if (!dynamicViewsModules) {
    dynamicViewsModules = import.meta.glob('../../views/**/*.{vue,tsx}');
    //온라인 lib 라우팅 병합
    dynamicViewsModules = Object.assign({}, dynamicViewsModules, packageViews);
  }
  if (!routes) return;
  routes.forEach((item) => {
    //메뉴 다국어
    if (item?.meta?.title) {
      const { t } = useI18n();
      if(item.meta.title.includes('t(\'') && t){
        item.meta.title = new Function('t', `return ${item.meta.title}`)(t);
      }
    }
    // @ts-ignore 숨겨진 라우팅 조정
    if (item?.hidden) {
      item.meta.hideMenu = true;
      //탐색경로를 숨길지 여부
      item.meta.hideBreadcrumb = true;
    }
    // @ts-ignore 라우팅 구성 무시 추가
    if (item?.route == 0) {
      item.meta.ignoreRoute = true;
    }
    // @ts-ignore 라우팅 구성 캐시 여부 추가
    item.meta.ignoreKeepAlive = !item?.meta.keepAlive;
    let token = getToken();
    item.component = (item.component || '').replace(/{{([^}}]+)?}}/g, (s1, s2) => _eval(s2)).replace('${token}', token);
    // iframe 적응
    if (/^\/?http(s)?/.test(item.component as string)) {
      item.component = item.component.substring(1, item.component.length);
    }
    if (/^http(s)?/.test(item.component as string)) {
      if (item.meta?.internalOrExternal) {
        // @ts-ignore 외부에서 열기
        item.path = item.component;
        item.path = item.path.replace('#', URL_HASH_TAB);
      } else {
        // @ts-ignore 내부적으로 열기
        item.meta.frameSrc = item.component;
      }
      delete item.component;
    }
    if (!item.component && item.meta?.frameSrc) {
      item.component = 'IFRAME';
    }
    let { component, name } = item;
    const { children } = item;
    if (component) {
      const layoutFound = LayoutMap.get(component.toUpperCase());
      if (layoutFound) {
        item.component = layoutFound;
      } else {
        let matched = dynamicImport(dynamicViewsModules, component as string);
        if (!matched && component.indexOf('dashboard/') > -1 && component.indexOf('/index') < 0) {
          matched = dynamicImport(dynamicViewsModules, component + '/index');
        }
        if (!matched && (component.indexOf('dashboard') > -1 || (item.path && item.path.indexOf('/dashboard') > -1))) {
          matched = dynamicViewsModules['../../views/ecommerce/dashboard/index.vue'];
        }
        item.component = matched;
      }
    } else if (name) {
      item.component = getParentLayout();
    }
    children && asyncImportRoute(children);
  });
}

function dynamicImport(dynamicViewsModules: Record<string, () => Promise<Recordable>>, component: string) {
  const keys = Object.keys(dynamicViewsModules);
  const matchKeys = keys.filter((key) => {
    const k = key.replace('../../views', '');
    const startFlag = component.startsWith('/');
    const endFlag = component.endsWith('.vue') || component.endsWith('.tsx');
    const startIndex = startFlag ? 0 : 1;
    const lastIndex = endFlag ? k.length : k.lastIndexOf('.');
    return k.substring(startIndex, lastIndex).toLowerCase() === component.toLowerCase();
  });
  if (matchKeys?.length === 1) {
    const matchKey = matchKeys[0];
    return dynamicViewsModules[matchKey];
  } else if (matchKeys?.length > 1) {
    warn(
      'Please do not create `.vue` and `.TSX` files with the same file name in the same hierarchical directory under the views folder. This will cause dynamic introduction failure'
    );
    return;
  }
}

// Turn background objects into routing objects
export function transformObjToRoute<T = AppRouteModule>(routeList: AppRouteModule[]): T[] {
  if (!routeList) {
    return [] as T[];
  }
  routeList.forEach((route) => {
    const component = route.component as string;
    if (component) {
      if (component.toUpperCase() === 'LAYOUT') {
        route.component = LayoutMap.get(component.toUpperCase());
      } else {
        route.children = [cloneDeep(route)];
        route.component = LAYOUT;
        route.name = `${route.name}Parent`;
        route.path = '';
        const meta = route.meta || {};
        meta.single = true;
        meta.affix = false;
        route.meta = meta;
      }
    } else {
      warn(route?.name + ' 컴포넌트가 설정되어 있지 않습니다');
    }
    route.children && asyncImportRoute(route.children);
  });
  return routeList as unknown as T[];
}

/**
 *  다중 레벨 라우팅을 레벨 2로 변환
 */
export function flatMultiLevelRoutes(routeModules: AppRouteModule[]) {
  if (!routeModules) {
    return [];
  }
  const modules: AppRouteModule[] = cloneDeep(routeModules);
  for (let index = 0; index < modules.length; index++) {
    const routeModule = modules[index];
    if (!isMultipleRoute(routeModule)) {
      continue;
    }
    promoteRouteLevel(routeModule);
  }
  return modules;
}

//라우팅 수준 개선
function promoteRouteLevel(routeModule: AppRouteModule) {
  // Use vue-router to splice menus
  let router: Router | null = createRouter({
    routes: [routeModule as unknown as RouteRecordNormalized],
    history: createWebHashHistory(),
  });

  const routes = router.getRoutes();
  addToChildren(routes, routeModule.children || [], routeModule);
  router = null;

  routeModule.children = routeModule.children?.map((item) => omit(item, 'children'));
}

// Add all sub-routes to the secondary route
function addToChildren(routes: RouteRecordNormalized[], children: AppRouteRecordRaw[], routeModule: AppRouteModule) {
  for (let index = 0; index < children.length; index++) {
    const child = children[index];
    const route = routes.find((item) => item.name === child.name);
    if (!route) {
      continue;
    }
    routeModule.children = routeModule.children || [];
    if (!routeModule.children.find((item) => item.name === route.name)) {
      routeModule.children?.push(route as unknown as AppRouteModule);
    }
    if (child.children?.length) {
      addToChildren(routes, child.children, routeModule);
    }
  }
}

// Determine whether the level exceeds 2 levels
function isMultipleRoute(routeModule: AppRouteModule) {
  if (!routeModule || !Reflect.has(routeModule, 'children') || !routeModule.children?.length) {
    return false;
  }

  const children = routeModule.children;

  let flag = false;
  for (let index = 0; index < children.length; index++) {
    const child = children[index];
    if (child.children?.length) {
      flag = true;
      break;
    }
  }
  return flag;
}
/**
 * 컴포넌트 주소 앞에 슬래시를 추가하세요.
 * @updateBy:lsq
 * @updateDate:2021-09-08
 */
export function addSlashToRouteComponent(routeList: AppRouteRecordRaw[]) {
  if (!routeList) {
    return [];
  }
  routeList.forEach((route) => {
    let component = route.component as string;
    if (component) {
      const layoutFound = LayoutMap.get(component);
      if (!layoutFound) {
        route.component = component.startsWith('/') ? component : `/${component}`;
      }
    }
    route.children && addSlashToRouteComponent(route.children);
  });
  return routeList;
}
