// /**
//  * qiankun 구성
//  */
// import { registerMicroApps, setDefaultMountApp, start, runAfterFirstMounted, addGlobalUncaughtErrorHandler } from 'qiankun';
// import { apps } from './apps';
// import { getProps, initGlState } from './state';
//
// /**
//  * 앱 리팩터링
//  */
// function filterApps() {
//   apps.forEach((item) => {
//     //기본 애플리케이션이 마이크로 애플리케이션에 전달해야 하는 데이터입니다.
//     item.props = getProps();
//     //마이크로 애플리케이션에 의해 트리거되는 라우팅 규칙
//     // @ts-ignore
//     item.activeRule = genActiveRule('/' + item.activeRule);
//   });
//   return apps;
// }
//
// /**
//  * 경로 모니터링
//  * @param {*} 라우터 접두사 접두사
//  */
// function genActiveRule(routerPrefix) {
//   return (location) => location.pathname.startsWith(routerPrefix);
// }
//
// /**
//  * 마이크로 애플리케이션 등록
//  */
// function registerApps() {
//   const _apps = filterApps();
//   registerMicroApps(_apps, {
//     beforeLoad: [
//       // @ts-ignore
//       (loadApp) => {
//         console.log('before load', loadApp);
//       },
//     ],
//     beforeMount: [
//       // @ts-ignore
//       (mountApp) => {
//         console.log('before mount', mountApp);
//       },
//     ],
//     afterMount: [
//       // @ts-ignore
//       (mountApp) => {
//         console.log('before mount', mountApp);
//       },
//     ],
//     afterUnmount: [
//       // @ts-ignore
//       (unloadApp) => {
//         console.log('after unload', unloadApp);
//       },
//     ],
//   });
//   // genActiveRule의 매개변수와 일치하도록 기본 하위 애플리케이션을 설정합니다.
//   // setDefaultMountApp();
//   // 일부 모니터링을 활성화하거나 스크립트를 묻어두는 등 첫 번째 마이크로 애플리케이션이 마운트된 후 호출해야 하는 메서드입니다.
//   runAfterFirstMounted(() => console.log('모니터링 켜기'));
//   // 포착되지 않은 전역 예외 처리기를 추가합니다.
//   addGlobalUncaughtErrorHandler((event) => console.log(event));
//   // 전역 상태 정의
//   initGlState();
//   //qiankun 시작
//   start({});
// }
//
// export default registerApps;
