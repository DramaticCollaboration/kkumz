/*
 *
 * 여기에 사용자 정의 표현식을 입력하세요.
 * 온라인 Form의 기본값 표현에 사용할 수 있습니다.
 * 외부에서 사용해야 하는 변수나 메소드는 내보내야 합니다. 그렇지 않으면 인식되지 않습니다.
 * 예：
 *   export const name = '홍길동'; // const는 상수이다
 *   export let age = 17; // 상황에 따라 다르며, const를 내보내거나 let을 내보내면 둘 다 정상적으로 사용할 수 있습니다.
 *   export function content(arg) { // 내보내기 메소드는 매개변수를 전달할 수 있으며, 괄호를 추가하여 Promise를 반환할 수 있습니다.
 *     return 'content' + arg;
 *   }
 *   export const address = (arg) => content(arg) + ' 서울 |'; // 내보내기 화살표 기능도 사용할 수 있습니다
 *
 */

/** 필드 기본값의 공식 예：주소 얻기 */
export function demoFieldDefVal_getAddress(arg) {
  if (!arg) {
    arg = '조양구';
  }
  반환`서울 ${arg}`;
}

/** 사용자 정의 JS 함수 예 */
export function sayHi(name) {
  if (!name) {
    name = '홍길동';
  }
  return `안녕하세요, 내 이름은： ${name}`;
}
