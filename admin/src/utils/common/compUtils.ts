import { useGlobSetting } from '/@/hooks/setting';
import { merge, random } from 'lodash-es';
import { isArray } from '/@/utils/is';
import { FormSchema } from '/@/components/Form';
import { reactive } from "vue";
import { getToken } from "/@/utils/auth";
import { useUserStoreWithOut } from "/@/store/modules/user";

import { Modal } from "ant-design-vue";
import { defHttp } from "@/utils/http/axios";
import { useI18n } from "@/hooks/web/useI18n";

const globSetting = useGlobSetting();
const baseApiUrl = globSetting.domainUrl;
/**
 *  파일 서비스 액세스 경로 가져오기
 * @param fileUrl 파일 경로
 * @param prefix(기본 http)  파일 경로 접두사 http/https
 */
export const getFileAccessHttpUrl = (fileUrl, prefix = 'http') => {
  let result = fileUrl;
  try {
    if (fileUrl && fileUrl.length > 0 && !fileUrl.startsWith(prefix)) {
      //배열 형식인지 확인
      let isArray = fileUrl.indexOf('[') != -1;
      if (!isArray) {
        let prefix = `${baseApiUrl}/api/v1/kkumz/sys/common/static/`;
        // 접두사가 포함되어 있는지 확인
        if (!fileUrl.startsWith(prefix)) {
          result = `${prefix}${fileUrl}`;
        }
      }
    }
  } catch (err) { console.debug("작업 처리 중 예외 발생:", err); }
  return result;
};

/**
 * 트리거 창.resize
 */
export function triggerWindowResizeEvent() {
  let event: any = document.createEvent('HTMLEvents');
  event.initEvent('resize', true, true);
  event.eventType = 'message';
  window.dispatchEvent(event);
}

/**
 * 난수 얻기
 *  @param 길이 자릿수
 */
export const getRandom = (length: number = 1) => {
  return '-' + parseInt(String(Math.random() * 10000 + 1), length);
};

/**
 * 문자열을 무작위로 생성
 * @param 길이 문자열의 길이
 * @param 채팅 선택적 문자열 간격（전달된 문자열의 문자만 생성됩니다.）
 * @return 문자열 생성 문자열
 */
export function randomString(length: number, chats?: string) {
  if (!length) length = 1;
  if (!chats) {
    // noinspection SpellCheckingInspection
    chats = '0123456789qwertyuioplkjhgfdsazxcvbnm';
  }
  let str = '';
  for (let i = 0; i < length; i++) {
    let num = random(0, chats.length - 1);
    str += chats[num];
  }
  return str;
}

/**
 * 일반 목록 데이터를 트리 구조로 변환
 * @param 배열 트리 데이터
 * @param opt 구성 매개변수
 * @param startPid 상위 노드
 */
export const listToTree = (array, opt, startPid) => {
  const obj = {
    primaryKey: opt.primaryKey || 'key',
    parentKey: opt.parentKey || 'parentId',
    titleKey: opt.titleKey || 'title',
    startPid: opt.startPid || '',
    currentDept: opt.currentDept || 0,
    maxDept: opt.maxDept || 100,
    childKey: opt.childKey || 'children',
  };
  if (startPid) {
    obj.startPid = startPid;
  }
  return toTree(array, obj.startPid, obj.currentDept, obj);
};
/**
 *  재귀적으로 트리 구축
 * @param list
 * @param startPid
 * @param currentDept
 * @param opt
 * @returns {Array}
 */
export const toTree = (array, startPid, currentDept, opt) => {
  if (opt.maxDept < currentDept) {
    return [];
  }
  let child = [];
  if (array && array.length > 0) {
    child = array
      .map((item) => {
        // 적합한 데이터 화면（기본 키 = startPid）
        if (typeof item[opt.parentKey] !== 'undefined' && item[opt.parentKey] === startPid) {
          // 조건이 충족되면 반복
          const nextChild = toTree(array, item[opt.primaryKey], currentDept + 1, opt);
          // 노드 정보 저장
          if (nextChild.length > 0) {
            item['isLeaf'] = false;
            item[opt.childKey] = nextChild;
          } else {
            item['isLeaf'] = true;
          }
          item['title'] = item[opt.titleKey];
          item['label'] = item[opt.titleKey];
          item['key'] = item[opt.primaryKey];
          item['value'] = item[opt.primaryKey];
          return item;
        }
      })
      .filter((item) => {
        return item !== undefined;
      });
  }
  return child;
};

/**
 * 표 하단의 전체 공구 방식
 * @param tableData 테이블 데이터
 * @param fieldKeys 총계를 계산할 열 필드
 */
export function mapTableTotalSummary(tableData: Recordable[], fieldKeys: string[]) {
  let totals: any = { _row: '총', _index: '총' };
  fieldKeys.forEach((key) => {
    totals[key] = tableData.reduce((prev, next) => {
      const value = Number(next[key]);
      if (!Number.isNaN(value)) {
        prev += value;
      }
      return prev;
    }, 0);
  });
  return totals;
}

/**
 * 흔들림 방지를 달성하는 간단한 방법
 *
 * 흔들림 방지(debounce)함수가 처음으로 특정 함수를 트리거하면 해당 함수를 즉시 실행하지 않고 최종 기한 값을 제공합니다.(delay), 예를 들어 100ms.
 * 100ms 이내에 해당 기능이 다시 실행되면 타이밍이 다시 시작되며 해당 타이밍이 끝날 때까지 실제로 해당 기능이 실행되지 않습니다.
 * 이 방법의 장점은 짧은 시간 내에 동일한 이벤트가 많이 발생하면 해당 기능이 한 번만 실행된다는 것입니다.
 *
 * @param 안정화를 위한 fn 기능
 * @param 흔들림 방지 밀리초 지연
 * @returns {Function}
 */
export function simpleDebounce(fn, delay = 100) {
  let timer: any | null = null;
  return function () {
    let args = arguments;
    if (timer) {
      clearTimeout(timer);
    }
    timer = setTimeout(() => {
      // @ts-ignore
      fn.apply(this, args);
    }, delay);
  };
}

/**
 * 날짜 형식
 * @param 날짜 날짜
 * @param 블록 형식 문자열
 */
export function dateFormat(date, block) {
  if (!date) {
    return '';
  }
  let format = block || 'yyyy-MM-dd';
  date = new Date(date);
  const map = {
    M: date.getMonth() + 1, // 월
    d: date.getDate(), // 낮
    h: date.getHours(), // 시간
    m: date.getMinutes(), // 가리키다
    s: date.getSeconds(), // 두번째
    q: Math.floor((date.getMonth() + 3) / 3), // 4분의 1
    S: date.getMilliseconds(), // 밀리초
  };
  format = format.replace(/([yMdhmsqS])+/g, (all, t) => {
    let v = map[t];
    if (v !== undefined) {
      if (all.length > 1) {
        v = `0${v}`;
        v = v.substr(v.length - 2);
      }
      return v;
    } else if (t === 'y') {
      return date
        .getFullYear()
        .toString()
        .substr(4 - all.length);
    }
    return all;
  });
  return format;
}

/**
 * IE11, Edge, Chrome, Firefox 및 Safari와 호환되는 이벤트 버블링 경로를 가져옵니다.
 * 현재 사용 장소：JVxeTable 스팬 모드
 */
export function getEventPath(event) {
  let target = event.target;
  let path = (event.composedPath && event.composedPath()) || event.path;

  if (path != null) {
    return path.indexOf(window) < 0 ? path.concat(window) : path;
  }

  if (target === window) {
    return [window];
  }

  let getParents = (node, memo) => {
    const parentNode = node.parentNode;

    if (!parentNode) {
      return memo;
    } else {
      return getParents(parentNode, memo.concat(parentNode));
    }
  };
  return [target].concat(getParents(target, []), window);
}

/**
 * 값이 없으면 배열에 푸시하세요. 그렇지 않으면 처리되지 않습니다.
 * @param array 연산할 데이터
 * @param 더할 가치를 소중히 여기다
 * @param 키는 null일 수 있습니다. 개체를 비교하는 경우 주소는 다를 수 있지만 값은 실제로 동일합니다. 이 필드를 전달하여 개체의 유일한 필드(예: id)를 확인할 수 있습니다.통과하지 못한 경우 실제 값을 직접 비교합니다.
 * @returns {boolean} 푸시에 성공하면 true를 반환하고, 처리되지 않으면 false를 반환합니다.
 */
export function pushIfNotExist(array, value, key?) {
  for (let item of array) {
    if (key && item[key] === value[key]) {
      return false;
    } else if (item === value) {
      return false;
    }
  }
  array.push(value);
  return true;
}
/**
 * 객체의 빈 속성 필터링
 * @param obj
 * @returns {*}
 */
export function filterObj(obj) {
  if (!(typeof obj == 'object')) {
    return;
  }

  for (let key in obj) {
    if (obj.hasOwnProperty(key) && (obj[key] == null || obj[key] == undefined || obj[key] === '')) {
      delete obj[key];
    }
  }
  return obj;
}

/**
 * 낙타 케이스에 밑줄
 * @param string
 */
export function underLine2CamelCase(string: string) {
  return string.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
}

/**
 * 트리 구조 찾기
 * @param treeList
 * @param fn 검색 방법
 * @param childrenKey
 */
export function findTree(treeList: any[], fn: Fn, childrenKey = 'children') {
  for (let i = 0; i < treeList.length; i++) {
    let item = treeList[i];
    if (fn(item, i, treeList)) {
      return item;
    }
    let children = item[childrenKey];
    if (isArray(children)) {
      let findResult = findTree(children, fn, childrenKey);
      if (findResult) {
        return findResult;
      }
    }
  }
  return null;
}

/** mapFormSchema 메소드 가져오기 */
export function bindMapFormSchema<T>(spanMap, spanTypeDef: T) {
  return function (s: FormSchema, spanType: T = spanTypeDef) {
    return merge(
      {
        disabledLabelWidth: true,
      } as FormSchema,
      spanMap[spanType],
      s
    );
  };
}

/**
 * 문자열이 null인지 null 문자열인지 여부
 * @param str
 * @return {boolean}
 */
export function stringIsNull(str) {
  // 둘 == null과 정의되지 않음을 동시에 감지할 수 있습니다.
  return str == null || str === 'null' || str === 'undefined';
}

/**
 * 【컴포넌트가 너무 많으면 성능 문제가 발생할 수 있습니다.】팝업 창 div를 가져오고, 드롭다운 상자, 날짜 및 기타 컴포넌트를 모달에 마운트하여 팝업 창 덮는 문제를 해결합니다.
 * @param node
 */
export function getAutoScrollContainer(node: HTMLElement) {
  let element: Nullable<HTMLElement> = node
  while (element != null) {
    if (element.classList.contains('scrollbar__view')) {
      // 스크롤 막대가 있는지 확인
      if (element.clientHeight < element.scrollHeight) {
        // 스크롤 막대가 있는 경우 이를 상위 항목에 마운트하여 스크롤 문제를 해결합니다.
        return node.parentElement
      } else {
        // 스크롤바가 없을 경우 본체에 마운트하여 드롭다운 박스가 덮이는 문제를 해결합니다.
        return document.body
      }
    } else {
      element = element.parentElement
    }
  }
  // 팝업창이 아닌 기본 로직을 사용하세요
  return node.parentElement
}

/**
 * 모든 하위 메뉴가 숨겨져 있는지 확인
 * @param menuTreeItem
 */
export  function checkChildrenHidden(menuTreeItem){
  let alwaysShow=menuTreeItem.alwaysShow;
  if(alwaysShow){
    return false;
  }
  if(!menuTreeItem.children){
    return false
  }
  return menuTreeItem.children?.find((item) => item.hideMenu == false) != null;
}

/**
 * 파일 크기 계산
 * @param fileSize
 * @param unit
 * @return 반환 크기 및 접미사
 */
export function calculateFileSize(fileSize, unit?) {
  let unitArr = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  if (unit && unit.length > 0) {
    unitArr = unit;
  }
  let size = fileSize;
  let unitIndex = 0;
  while (size >= 1024 && unitIndex < unitArr.length - 1) {
    size /= 1024;
    unitIndex++;
  }
  //소수점 이하 두 자리까지 반올림
  size = Math.round(size * 100) / 100;
  return size + unitArr[unitIndex];
}

/**
 * 업로드 헤더 가져오기
 */
export function getHeaders() {
  return reactive({
    'X-Access-Token': getToken(),
  });
}

/** 표현식을 기반으로 해당 사용자 정보를 가져옵니다. */
export function getUserInfoByExpression(expression) {
  if (!expression) {
    return expression;
  }
  const userStore = useUserStoreWithOut();
  let userInfo = userStore.getUserInfo;
  if (userInfo) {
    switch (expression) {
      case 'sysUserId':
        return userInfo.id;
      // 현재 로그인된 사용자 로그인 계정
      case 'sysUserCode':
      case 'sys_user_code':
        return userInfo.username;
      // 현재 로그인한 사용자의 실명
      case 'sysUserName':
        return userInfo.realname;
      // 현재 로그인 사용자 부서번호
      case 'sysOrgCode':
      case 'sys_org_code':
        return userInfo.orgCode;
    }
  }
  return expression;
}

/**
 * 표현식 바꾸기（#{xxx}）사용자 정보를 위해
 * @param expression
 */
export function replaceUserInfoByExpression(expression: string | any[]) {
  if (!expression) {
    return expression;
  }
  const isString = typeof expression === 'string';
  const isArray = Array.isArray(expression)
  if (!isString && !isArray) {
    return expression;
  }
  const reg = /#{(.*?)}/g;
  const replace = (str) => {
    if (typeof str !== 'string') {
      return str;
    }
    let result = str.match(reg);
    if (result && result.length > 0) {
      result.forEach((item) => {
        let userInfo = getUserInfoByExpression(item.substring(2, item.length - 1));
        str = str.replace(item, userInfo);
      });
    }
    return str;
  };
  // @ts-ignore
  return isString ? replace(expression) : expression.map(replace);
}


/**
 * 날짜와 현재 시간이 같은 날인지 확인
 * @param dateStr
 */
export function sameDay(dateStr) {
  if (!dateStr) {
    return false;
  }
  // 현재 날짜 가져오기
  let currentDate = new Date();
  let currentDay = currentDate.getDate();
  let currentMonth = currentDate.getMonth();
  let currentYear = currentDate.getFullYear();

  //비교할 다른 날짜를 생성하세요.
  let otherDate = new Date(dateStr);
  let otherDay = otherDate.getDate();
  let otherMonth = otherDate.getMonth();
  let otherYear = otherDate.getFullYear();

  //날짜 비교
  if (currentDay === otherDay && currentMonth === otherMonth && currentYear === otherYear) {
    return true;
  } else {
    return false;
  }
}


/**
 * 메뉴 이름 번역
 * 2024-02-28
 * liaozhiyang
 * @param data
 */
export function translateTitle(data) {
  if (data?.length) {
    const { t } = useI18n();
    data.forEach((item) => {
      if (item.slotTitle) {
        if (item.slotTitle.includes("t('") && t) {
          item.slotTitle = new Function('t', `return ${item.slotTitle}`)(t);
        }
      }
      if (item.children?.length) {
        translateTitle(item.children);
      }
    });
  }
  return data;
}
