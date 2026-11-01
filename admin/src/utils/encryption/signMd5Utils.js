import md5 from 'md5';
//서명 키 문자열(전면 및 후면 끝은 일관성이 있어야 합니다. 공식 출시를 위해서는 직접 수정하세요.)
const signatureSecret = (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.VITE_SIGNATURE_SECRET) || 'dd05f1c54d63749eda95f9fa6d49v442a';

export default class signMd5Utils {
  /**
   * json 매개변수 오름차순
   * @param jsonObj가 매개변수를 보냅니다.
   */

  static sortAsc(jsonObj) {
    let arr = new Array();
    let num = 0;
    for (let i in jsonObj) {
      arr[num] = i;
      num++;
    }
    let sortArr = arr.sort();
    let sortObj = {};
    for (let i in sortArr) {
      sortObj[sortArr[i]] = jsonObj[sortArr[i]];
    }
    return sortObj;
  }

  /**
   * @param URL 요청 URL, 요청 매개변수를 포함해야 함(URL?다음 매개변수)
   * @param requestParams 요청 매개변수(@RequestParam(get)JSON 매개변수)
   * @param requestBodyParams 요청 매개변수(@RequestBody(post)매개변수)
   * @returns {string} 서명 받기
   */
  static getSign(url, requestParams, requestBodyParams) {
    let urlParams = this.parseQueryString(url);
    let jsonObj = this.mergeObject(urlParams, requestParams);
    if(requestBodyParams){
      jsonObj = this.mergeObject(jsonObj, requestBodyParams)
    }
    let requestBody = this.sortAsc(jsonObj);
    delete requestBody._t;
    console.log('sign requestBody:', requestBody);
    return md5(JSON.stringify(requestBody) + signatureSecret).toUpperCase();
  }

  /**
   * @param URL이 요청한 URL
   * @returns {{}} URL의 요청 매개변수를 json 객체로 조합합니다.(URL?다음 매개변수)
   */
  static parseQueryString(url) {
    let urlReg = /^[^\?]+\?([\w\W]+)$/,
      paramReg = /([^&=]+)=([\w\W]*?)(&|$|#)/g,
      urlArray = urlReg.exec(url),
      result = {};

    // URL sys/dict/getDictItems/sys_user,realname,username에서 마지막 쉼표가 있는 매개변수 변수를 가져옵니다.
    //【여기의 조건은 인코딩되지 않았습니다.】조건부 매개변수의 예：/sys/dict/getDictItems/sys_user,realname,id,username!='admin'%20order%20by%20create_time
    let lastpathVariable = url.substring(url.lastIndexOf('/') + 1);
    if (lastpathVariable.includes(',')) {
      if (lastpathVariable.includes('?')) {
        lastpathVariable = lastpathVariable.substring(0, lastpathVariable.indexOf('?'));
      }
      //서명 확인 실패 해결 #2728
      //decodeURI에는 특수 문자를 인코딩하거나 디코딩하는 기능이 없으므로 decodeURIComponent를 사용해야 합니다.
      result['x-path-variable'] = decodeURIComponent(lastpathVariable);
    }
    if (urlArray && urlArray[1]) {
      let paramString = urlArray[1],
        paramResult;
      while ((paramResult = paramReg.exec(paramString)) != null) {
        //숫자 값은 문자열 유형으로 변환되며 프런트엔드 및 백엔드 암호화 규칙은 일관되게 유지됩니다.
        if (this.myIsNaN(paramResult[2])) {
          paramResult[2] = paramResult[2].toString();
        }
        result[paramResult[1]] = paramResult[2];
      }
    }
    return result;
  }

  /**
   * @returns {*} 두 개체를 하나로 병합
   */
  static mergeObject(objectOne, objectTwo) {
    if (objectTwo && Object.keys(objectTwo).length > 0) {
      for (let key in objectTwo) {
        if (objectTwo.hasOwnProperty(key) === true) {
          //숫자 값은 문자열 유형으로 변환되며 프런트엔드 및 백엔드 암호화 규칙은 일관되게 유지됩니다.
          if (this.myIsNaN(objectTwo[key])) {
            objectTwo[key] = objectTwo[key].toString();
          }
          //부울 유형은 문자열 유형으로 변환되며 프런트엔드 및 백엔드 암호화 규칙은 일관되게 유지됩니다.
          if (typeof objectTwo[key] === 'boolean') {
            objectTwo[key] = objectTwo[key].toString();
          }
          objectOne[key] = objectTwo[key];
        }
      }
    }
    return objectOne;
  }

  static urlEncode(param, key, encode) {
    if (param == null) return '';
    let paramStr = '';
    let t = typeof param;
    if (t == 'string' || t == 'number' || t == 'boolean') {
      paramStr += '&' + key + '=' + (encode == null || encode ? encodeURIComponent(param) : param);
    } else {
      for (let i in param) {
        let k = key == null ? i : key + (param instanceof Array ? '[' + i + ']' : '.' + i);
        paramStr += this.urlEncode(param[i], k, encode);
      }
    }
    return paramStr;
  }

  /**
   * 인터페이스 서명은 헤더에 타임스탬프를 생성하는 데 사용됩니다.
   * @returns {number}
   */
  static getTimestamp() {
    return new Date().getTime();
  }

  // static getDateTimeToString() {
  //   const date_ = new Date()
  //   const year = date_.getFullYear()
  //   let month = date_.getMonth() + 1
  //   let day = date_.getDate()
  //   if (month < 10) month = '0' + month
  //   if (day < 10) day = '0' + day
  //   let hours = date_.getHours()
  //   let mins = date_.getMinutes()
  //   let secs = date_.getSeconds()
  //   const msecs = date_.getMilliseconds()
  //   if (hours < 10) hours = '0' + hours
  //   if (mins < 10) mins = '0' + mins
  //   if (secs < 10) secs = '0' + secs
  //   if (msecs < 10) secs = '0' + msecs
  //   return year + '' + month + '' + day + '' + hours + '' + mins + '' + secs
  // }
  // 참: 숫자, 거짓：숫자가 아닌 유형
  static myIsNaN(value) {
    return typeof value === 'number' && !isNaN(value);
  }
}
