import type { RouteLocationNormalized, RouteRecordNormalized } from 'vue-router';
import type { App, Plugin } from 'vue';

import { unref } from 'vue';
import { isObject } from '/@/utils/is';

export const URL_HASH_TAB = `__AGWE4H__HASH__TAG__PWHRG__`;

export const noop = () => {};

/**
 * @description:  Set ui mount node
 */
export function getPopupContainer(node?: HTMLElement): HTMLElement {
  return (node?.parentNode as HTMLElement) ?? document.body;
}

/**
 * Add the object as a parameter to the URL
 * @param baseUrl url
 * @param obj
 * @returns {string}
 * eg:
 *  let obj = {a: '3', b: '4'}
 *  setObjToUrlParams('www.empasy.com', obj)
 *  ==>www.empasy.com?a=3&b=4
 */
export function setObjToUrlParams(baseUrl: string, obj: any): string {
  let parameters = '';
  for (const key in obj) {
    parameters += key + '=' + encodeURIComponent(obj[key]) + '&';
  }
  parameters = parameters.replace(/&$/, '');
  return /\?$/.test(baseUrl) ? baseUrl + parameters : baseUrl.replace(/\/?$/, '?') + parameters;
}

export function deepMerge<T = any>(src: any = {}, target: any = {}): T {
  let key: string;
  for (key in target) {
    src[key] = isObject(src[key]) && isObject(target[key]) ? deepMerge(src[key], target[key]) : (src[key] = target[key]);
  }
  return src;
}

export function openWindow(url: string, opt?: { target?: TargetContext | string; noopener?: boolean; noreferrer?: boolean }) {
  const { target = '__blank', noopener = true, noreferrer = true } = opt || {};
  const feature: string[] = [];

  noopener && feature.push('noopener=yes');
  noreferrer && feature.push('noreferrer=yes');

  window.open(url, target, feature.join(','));
}

// dynamic use hook props
export function getDynamicProps<T, U>(props: T): Partial<U> {
  const ret: Recordable = {};

  Object.keys(props).map((key) => {
    ret[key] = unref((props as Recordable)[key]);
  });

  return ret as Partial<U>;
}

/**
 * Form 필드 값 데이터 유형 가져오기
 * @param props
 * @param field
 * @updateBy:zyf
 */
export function getValueType(props, field) {
  let formSchema = unref(unref(props)?.schemas);
  let valueType = 'string';
  if (formSchema) {
    let schema = formSchema.filter((item) => item.field === field)[0];
    valueType = schema.componentProps && schema.componentProps.valueType ? schema.componentProps.valueType : valueType;
  }
  return valueType;
}
export function getRawRoute(route: RouteLocationNormalized): RouteLocationNormalized {
  if (!route) return route;
  const { matched, ...opt } = route;
  return {
    ...opt,
    matched: (matched
      ? matched.map((item) => ({
          meta: item.meta,
          name: item.name,
          path: item.path,
        }))
      : undefined) as RouteRecordNormalized[],
  };
}
/**
 * 딥 클론 개체 및 배열
 * @param obj 복제되는 객체
 * @return 복제된 객체
 */
export function cloneObject(obj) {
  return JSON.parse(JSON.stringify(obj));
}

export const withInstall = <T>(component: T, alias?: string) => {
  //console.log("---초기화---", component)
  
  const comp = component as any;
  comp.install = (app: App) => {
    app.component(comp.name || comp.displayName, component);
    if (alias) {
      app.config.globalProperties[alias] = component;
    }
  };
  return component as T & Plugin;
};

/**
 * URL 주소 매개변수 가져오기
 * @param paraName
 */
export function getUrlParam(paraName) {
  let url = document.location.toString();
  let arrObj = url.split('?');

  if (arrObj.length > 1) {
    let arrPara = arrObj[1].split('&');
    let arr;

    for (let i = 0; i < arrPara.length; i++) {
      arr = arrPara[i].split('=');

      if (arr != null && arr[0] == paraName) {
        return arr[1];
      }
    }
    return '';
  } else {
    return '';
  }
}

/**
 * 동면하다（setTimeout의 Promise 버전）
 * @param ms 수면 시간, 단위：밀리초
 * @param fn 콜백, 선택 사항
 * @return Promise
 */
export function sleep(ms: number, fn?: Fn) {
  return new Promise<void>((resolve) =>
    setTimeout(() => {
      fn && fn();
      resolve();
    }, ms)
  );
}

/**
 * 정규식 없이 모든 값 바꾸기
 * @param text 대체할 문자열
 * @param 교체 전 체커 내용
 * @param replacement 교체된 콘텐츠
 * @returns {String} 교체된 문자열
 */
export function replaceAll(text, checker, replacer) {
  let lastText = text;
  text = text.replace(checker, replacer);
  if (lastText !== text) {
    return replaceAll(text, checker, replacer);
  }
  return text;
}

/**
 * URLAbout 매개변수 가져오기
 * @param url
 */
export function getQueryVariable(url) {
  if (!url) return;

  var t,
    n,
    r,
    i = url.split('?')[1],
    s = {};
  (t = i.split('&')), (r = null), (n = null);
  for (var o in t) {
    var u = t[o].indexOf('=');
    u !== -1 && ((r = t[o].substr(0, u)), (n = t[o].substr(u + 1)), (s[r] = n));
  }
  return s;
}
/**
 * 처리 버튼 표시 여부 결정
 * @param bpmStatus
 * @returns {*}
 */
export function showDealBtn(bpmStatus) {
  if (bpmStatus != '1' && bpmStatus != '3' && bpmStatus != '4') {
    return true;
  }
  return false;
}
/**
 * 숫자를 대문자로 변환
 * @param value
 * @returns {*}
 */
export function numToUpper(value) {
  if (value != '') {
    let unit = new Array('천', '백', '찾다', '', '천', '백', '찾다', '', '뿔', '가리키다');
    const toDx = (n) => {
      switch (n) {
        case '0':
          return '영';
        case '1':
          return '하나';
        case '2':
          return '둘';
        case '3':
          return '삼';
        case '4':
          return '4개';
        case '5':
          return '우';
        case '6':
          return '땅';
        case '7':
          return '일곱';
        case '8':
          return '여덟';
        case '9':
          return '지우';
      }
    };
    let lth = value.toString().length;
    value *= 100;
    value += '';
    let length = value.length;
    if (lth <= 8) {
      let result = '';
      for (let i = 0; i < length; i++) {
        if (i == 2) {
          result = '원' + result;
        } else if (i == 6) {
          result = '만' + result;
        }
        if (value.charAt(length - i - 1) == 0) {
          if (i != 0 && i != 1) {
            if (result.charAt(0) != '영' && result.charAt(0) != '원' && result.charAt(0) != '만') {
              result = '영' + result;
            }
          }
          continue;
        }
        result = toDx(value.charAt(length - i - 1)) + unit[unit.length - i - 1] + result;
      }
      result += result.charAt(result.length - 1) == '원' ? '모두' : '';
      return result;
    } else {
      return null;
    }
  }
  return null;
}

const allModules = import.meta.glob('../views/**/*.vue');
export function importViewsFile(path): Promise<any> {
  if (path.startsWith('/')) {
    path = path.substring(1);
  }
  let page = '';
  if (path.endsWith('.vue')) {
    page = `../views/${path}`;
  } else {
    page = `../views/${path}.vue`;
  }
  return new Promise((resolve, reject) => {
    let flag = true;
    for (const path in allModules) {
      if (path == page) {
        flag = false;
        allModules[path]().then((mod) => {
          console.log(path, mod);
          resolve(mod);
        });
      }
    }
    if (flag) {
      reject('파일이 존재하지 않습니다:' + page);
    }
  });
}


/**
 * 빌딩 블록 보고서의 미리보기 페이지로 이동
 * @param url
 * @param id
 * @param token
 */
export function goJmReportViewPage(url, id, token) {
  // URL 지원{{ window.xxx }}자리표시자 변수
  url = url.replace(/{{([^}]+)?}}/g, (_s1, s2) => _eval(s2))
  if (url.includes('?')) {
    url += '&'
  } else {
    url += '?'
  }
  url += `id=${id}`
  url += `&token=${token}`
  window.open(url)
}

/**
 * 임의의 색상을 얻으십시오
 */
export function getRandomColor(index?) {

  const colors = [
    'rgb(100, 181, 246)',
    'rgb(77, 182, 172)',
    'rgb(255, 183, 77)',
    'rgb(229, 115, 115)',
    'rgb(149, 117, 205)',
    'rgb(161, 136, 127)',
    'rgb(144, 164, 174)',
    'rgb(77, 208, 225)',
    'rgb(129, 199, 132)',
    'rgb(255, 138, 101)',
    'rgb(133, 202, 205)',
    'rgb(167, 214, 118)',
    'rgb(254, 225, 89)',
    'rgb(251, 199, 142)',
    'rgb(239, 145, 139)',
    'rgb(169, 181, 255)',
    'rgb(231, 218, 202)',
    'rgb(252, 128, 58)',
    'rgb(254, 161, 172)',
    'rgb(194, 163, 205)',
  ];
  return index && index < 19 ? colors[index] : colors[Math.floor((Math.random()*(colors.length-1)))];
}

export function getRefPromise(componentRef) {
  return new Promise((resolve) => {
    (function next() {
      const ref = componentRef.value;
      if (ref) {
        resolve(ref);
      } else {
        setTimeout(() => {
          next();
        }, 100);
      }
    })();
  });
}

/**
 * 2023-09-04
 * liaozhiyang
 * eval을 새 함수로 교체
 */
export function _eval(str: string) {
 return new Function(`return ${str}`)();
}

/**
 * 2024-04-30
 * liaozhiyang
 * 시간 또는 타임스탬프를 통해 antd에 해당하는 연도, 월, 주, 분기를 가져옵니다.
 */
export function getWeekMonthQuarterYear(date) {
  // ISO 주 번호를 얻는 기능
  const getISOWeek = (date) => {
    const jan4 = new Date(date.getFullYear(), 0, 4);
    const oneDay = 86400000; // 하루에 밀리초
    return Math.ceil(((date - jan4.getTime()) / oneDay + jan4.getDay() + 1) / 7);
  };
  // 타임스탬프를 날짜 객체로 변환
  const dateObj = new Date(date);
  // 계산 주간
  const week = getISOWeek(dateObj);
  // 월 계산
  const month = dateObj.getMonth() + 1; // 월은 0부터 시작하므로 1을 더합니다.
  // 분기 계산
  const quarter = Math.floor(dateObj.getMonth() / 3) + 1;
  // 계산연도
  const year = dateObj.getFullYear();
  return {
    year: `${year}`,
    month: `${year}-${month.toString().padStart(2, '0')}`,
    week: `${year}-${week}주`,
    quarter: `${year}-Q${quarter}`,
  };
}

/**
 * 2024-05-17
 * liaozhiyang
 * 마운트를 설정하는 데 여러 모달 요소가 있을 수 있으며 해당 요소를 찾아야 합니다.
 */
export const setPopContainer = (node, selector) => {
  if (typeof selector === 'string') {
    const targetEles = Array.from(document.querySelectorAll(selector));
    if (targetEles.length > 1) {
      const retrospect = (node, elems) => {
        let ele = node.parentNode;
        while (ele) {
          const findParentNode = elems.find(item => item === ele);
          if (findParentNode) {
            ele = null;
            return findParentNode;
          } else {
            ele = ele.parentNode;
          }
        }
        return null;
      };
      const elem = retrospect(node, targetEles);
      if (elem) {
        return elem;
      } else {
        return document.querySelector(selector);
      }
    } else {
      return document.querySelector(selector);
    }
  } else {
    return selector;
  }
};
