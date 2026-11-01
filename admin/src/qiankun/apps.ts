// /**
//  *마이크로 애플리케이션 앱
//  * @name: 마이크로앱 이름 - 고유해야 합니다.
//  * @entry: 마이크로 애플리케이션 입구.필수 - 이 주소를 통해 마이크로앱을 로드합니다.
//  * @container: 마이크로 애플리케이션 탑재 노드 - 로딩이 완료된 후 마이크로 애플리케이션이 이 노드에 탑재됩니다.
//  * @activeRule: 마이크로앱에 의해 트리거된 라우팅 규칙 - 라우팅 규칙이 트리거된 후 마이크로앱이 로드됩니다.
//  */
// //하위 애플리케이션 목록
// const _apps: object[] = [];
// for (const key in import.meta.env) {
//   if (key.includes('VITE_APP_SUB_')) {
//     const name = key.split('VITE_APP_SUB_')[1];
//     const obj = {
//       name,
//       entry: import.meta.env[key],
//       container: '#content',
//       activeRule: name,
//     };
//     _apps.push(obj);
//   }
// }
// export const apps = _apps;
