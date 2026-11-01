/**
 * 사전 유틸리티
 * author: scott
 * date: 20190109
 */

import { ajaxGetDictItems, getDictItemsByCode } from './index';

/**
 * 사전 배열 가져오기
 * 【현재는 Form 디자이너 페이지에서만 이 방법을 사용합니다.】
 * @param dict코드 사전코드
 * @param isTransformResponse 반환 결과를 변환할지 여부
 * @return List<Map>
 */
export async function initDictOptions(dictCode, isTransformResponse = true) {
  if (!dictCode) {
    return '사전 코드는 필수입니다!';
  }
  //캐시에서 사전 구성 읽기 우선순위 지정
  if (getDictItemsByCode(dictCode)) {
    let res = {};
    res.result = getDictItemsByCode(dictCode);
    res.success = true;
    if (isTransformResponse) {
      return res.result;
    } else {
      return res;
    }
  }
  //사전 배열 가져오기
  return await ajaxGetDictItems(dictCode, {}, { isTransformResponse });
}

/**
 * 텍스트를 사전 값으로 바꾸는 일반적인 방법
 * @param dictOptions 사전 배열
 * @param 텍스트 사전 값
 * @return String
 */
export function filterDictText(dictOptions, text) {
  if (text != null && Array.isArray(dictOptions)) {
    let result = [];
    // 여러 쉼표로 구분하고 배열 객체 전달을 허용합니다.
    let splitText;
    if (Array.isArray(text)) {
      splitText = text;
    } else {
      splitText = text.toString().trim().split(',');
    }
    for (let txt of splitText) {
      let dictText = txt;
      for (let dictItem of dictOptions) {
        if (dictItem == null) break;
        if (txt.toString() === dictItem.value.toString()) {
          dictText = dictItem.text || dictItem.title || dictItem.label;
          break;
        }
      }
      result.push(dictText);
    }
    return result.join(',');
  }
  return text;
}

/**
 * 텍스트를 사전 값으로 바꾸는 일반적인 방법(객관식)
 * @param dictOptions 사전 배열
 * @param 텍스트 사전 값
 * @return String
 */
export function filterMultiDictText(dictOptions, text) {
  //js “!text”는 0을 비어있는 것으로 간주하여 고급 처리를 수행합니다.
  if (text === 0 || text === '0') {
    if (dictOptions) {
      for (let dictItem of dictOptions) {
        if (text == dictItem.value) {
          return dictItem.text;
        }
      }
    }
  }

  if (!text || text == 'undefined' || text == 'null' || !dictOptions || dictOptions.length == 0) {
    return '';
  }
  let re = '';
  text = text.toString();
  let arr = text.split(',');
  dictOptions.forEach(function (option) {
    if (option) {
      for (let i = 0; i < arr.length; i++) {
        if (arr[i] === option.value) {
          re += option.text + ',';
          break;
        }
      }
    }
  });
  if (re == '') {
    return text;
  }
  return re.substring(0, re.length - 1);
}

/**
 * 필드 값에 해당하는 텍스트를 번역합니다.
 * @param children
 * @returns string
 */
export function filterDictTextByCache(dictCode, key) {
  if (key == null || key.length == 0) {
    return;
  }
  if (!dictCode) {
    return '사전 코드는 필수입니다!';
  }
  //캐시에서 사전 구성 읽기 우선순위 지정
  if (getDictItemsByCode(dictCode)) {
    let item = getDictItemsByCode(dictCode).filter((t) => t['value'] == key);
    if (item && item.length > 0) {
      return item[0]['text'];
    }
  }
}

/** 코드를 통해 사전 배열 가져오기 */
export async function getDictItems(dictCode, params) {
  //캐시에서 사전 구성 읽기 우선순위 지정
  if (getDictItemsByCode(dictCode)) {
    let desformDictItems = getDictItemsByCode(dictCode).map((item) => ({
      ...item,
      label: item.text,
    }));
    return Promise.resolve(desformDictItems);
  }

  //캐시에 없으면 배경을 요청하세요
  return await ajaxGetDictItems(dictCode, params)
    .then((result) => {
      if (result.length) {
        let res = result.map((item) => ({ ...item, label: item.text }));
        console.log('------- DB에서 사전을 얻어옴-------dictCode : ', dictCode, res);
        return Promise.resolve(res);
      } else {
        console.error('getDictItems error: : ', res);
        return Promise.resolve([]);
      }
    })
    .catch((res) => {
      console.error('getDictItems error: ', res);
      return Promise.resolve([]);
    });
}
