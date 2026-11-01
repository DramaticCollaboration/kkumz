import 'uno.css';
import '/@/design/index.less';
import 'ant-design-vue/dist/reset.css';
// 등록 아이콘
import 'virtual:svg-icons-register';

import App from './App.vue';
import { createApp } from 'vue';
import { initAppConfigStore } from '/@/logics/initAppConfig';
import { setupErrorHandle } from '/@/logics/error-handle';
import { router, setupRouter } from '/@/router';
import { setupRouterGuard } from '/@/router/guard';
import { setupStore } from '/@/store';
import { setupGlobDirectives } from '/@/directives';
import { setupI18n } from '/@/locales/setupI18n';
import { registerGlobComp } from '/@/components/registerGlobComp';
import { registerThirdComp } from '/@/settings/registerThirdComp';
import { useSso } from '/@/hooks/web/useSso';
// 온라인 모듈 lib 등록
import { registerPackages } from '/@/utils/monorepo/registerPackages';

async function bootstrap() {
  // 애플리케이션 인스턴스 생성
  const app = createApp(App);
  // 【QQYUN-6329】
  window.appRootInstance = app;
  // 다중 언어 구성, 비동기 상황: 서버 측에서 언어 파일을 얻을 수 있습니다.
  await setupI18n(app);

  // 구성 저장
  setupStore(app);

  // 내부 시스템 구성 초기화
  initAppConfigStore();

  // 외부 모듈 경로 등록(온라인 모듈 lib 등록)
  registerPackages(app);

  // 전역 컴포넌트 등록
  registerGlobComp(app);

  //CAS 싱글 사인온
  await useSso().ssoLogin();

  // 라우팅 구성
  setupRouter(app);

  // 경로 보호
  setupRouterGuard(router);

  // 글로벌 디렉티브 등록
  setupGlobDirectives(app);

  // 전역 오류 처리 구성
  setupErrorHandle(app);

  // 써드파티 컴포넌트 등록
  await registerThirdComp(app);

  // 경로가 준비되면 마운트를 실행합니다.( https://next.router.vuejs.org/api/#isready)
  await router.isReady();

  // 마운트 애플리케이션
  app.mount('#app', true);

  console.log(" vue3 앱이 로드되었습니다!")
}

bootstrap();
