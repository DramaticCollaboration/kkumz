// /**
//  *공개 데이터
//  */
// import { initGlobalState } from 'qiankun';
// import { store } from '/@/store';
// import { router } from '/@/router';
// import { getToken } from '/@/utils/auth';
// //하위 애플리케이션으로 전달되는 데이터 정의
// export function getProps() {
//   return {
//     data: {
//       publicPath: '/',
//       token: getToken(),
//       store,
//       router,
//     },
//   };
// }
//
// /**
//  * 전역 상태를 정의하고 메인 애플리케이션에서 사용하는 통신 방식을 반환합니다. 마이크로 애플리케이션은 props를 통해 통신 방식을 얻습니다.
//  * @param 기본 애플리케이션에 의해 전달된 상태 공개 데이터
//  */
// export function initGlState(info = { userName: 'admin' }) {
//   // 상태 초기화
//   const actions = initGlobalState(info);
//   // 새로운 값을 설정하다
//   actions.setGlobalState(info);
//   // 관찰자 함수 등록 - globalState 변경에 응답하고 globalState가 변경되면 관찰자 함수를 트리거합니다.
//   actions.onGlobalStateChange((newState, prev) => {
//     // state: 상태가 변경됨; 이전 변경 전 상태
//     console.info('newState', newState);
//     console.info('prev', prev);
//     for (const key in newState) {
//       console.info('onGlobalStateChange', key);
//     }
//   });
// }
