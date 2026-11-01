/**
 * @fileoverview Vite 4.x 프론트엔드 빌드 및 개발 서버 환경설정
 * @description
 *  - 로컬 개발 서버 프록시: VITE_PROXY 환경변수에 따라 /api 요청을 백엔드(포트 8080)로 역방향 라우팅
 *  - 번들 최적화: Rollup manualChunks를 통한 벤더 라이브러리(Ant Design Vue, ECharts, Lodash) 분할 청크 빌드
 *  - 플러그인 파이프라인: Vue 3 SFC 컴파일러, JSX/TSX 지원, WindiCSS/Tailwind, 압축(gzip/brotli), 모의 API 설정
 *  - 경로 별칭(Alias): /@/ -> src/, /#/ -> types/ 매핑을 통한 모듈 임포트 가독성 확보
 */
﻿import type { UserConfig, ConfigEnv } from 'vite';
import pkg from './package.json';
import dayjs from 'dayjs';
import { loadEnv } from 'vite';
import { resolve } from 'path';
import { generateModifyVars } from './build/generate/generateModifyVars';
import { createProxy } from './build/vite/proxy';
import { wrapperEnv } from './build/utils';
import { createVitePlugins } from './build/vite/plugin';
import { OUTPUT_DIR } from './build/constant';

function pathResolve(dir: string) {
  return resolve(process.cwd(), '.', dir);
}

const { dependencies, devDependencies, name, version } = pkg;
const __APP_INFO__ = {
  pkg: { dependencies, devDependencies, name, version },
  lastBuildTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
};

export default ({ command, mode }: ConfigEnv): UserConfig => {
  const root = process.cwd();

  const env = loadEnv(mode, root);

  // The boolean type read by loadEnv is a string. This function can be converted to boolean type
  const viteEnv = wrapperEnv(env);

  const { VITE_PORT, VITE_PUBLIC_PATH, VITE_PROXY } = viteEnv;

  const isBuild = command === 'build';

  return {
    base: VITE_PUBLIC_PATH,
    root,
    resolve: {
      alias: [
        {
          find: 'vue-i18n',
          replacement: 'vue-i18n/dist/vue-i18n.cjs.js',
        },
        // /@/xxxx => src/xxxx
        {
          find: /\/@\//,
          replacement: pathResolve('src') + '/',
        },
        // /#/xxxx => types/xxxx
        {
          find: /\/#\//,
          replacement: pathResolve('types') + '/',
        },
        {
          find: /@\//,
          replacement: pathResolve('src') + '/',
        },
        {
          find: /~@assets\//,
          replacement: pathResolve('src/assets') + '/',
        },
        // /#/xxxx => types/xxxx
        {
          find: /#\//,
          replacement: pathResolve('types') + '/',
        },
      ],
    },
    server: {
      // Listening on all local IPs
      host: true,
      https: false,
      port: VITE_PORT,
      // Load proxy configuration from .env
      proxy: createProxy(VITE_PROXY),
    },
    build: {
      minify: 'esbuild',
      target: 'es2015',
      cssTarget: 'chrome80',
      outDir: OUTPUT_DIR,
      rollupOptions: {
        // 중요한 코드가 삭제되어 패키징 후 기능 이상이 발생하는 것을 방지하려면 칩 제거 최적화를 끄십시오.
        treeshake: false,
        output: {
          chunkFileNames: 'js/[name]-[hash].js', // 파일 이름의 이름을 소개합니다
          entryFileNames: 'js/[name]-[hash].js', // 패키지 항목 파일 이름
          // 수동 청크 구성 (종속 패키지는 가장 큰 패키지에서 가장 작은 패키지로 정렬됩니다.)
          manualChunks: {
            // vue vue-router 병합 및 패키지
            'vue-vendor': ['vue', 'vue-router'],
            'antd-vue-vendor': ['ant-design-vue','@ant-design/icons-vue','@ant-design/colors'],
            'vxe-table-vendor': ['vxe-table','vxe-table-plugin-antd','xe-utils','@vxe-ui/core'],
            'emoji-mart-vue-fast': ['emoji-mart-vue-fast'],
            'china-area-data-vendor': ['china-area-data']
          },
        },
      },
      // brotliSize 디스플레이를 끄면 패키징 시간이 약간 단축될 수 있습니다.
      reportCompressedSize: false,
      // 크기가 너무 큰 정적 리소스About 경고 크기 늘리기
      chunkSizeWarningLimit: 3000,
    },
    esbuild: {
      //전역 console.log 지우기 및 디버그
      drop: isBuild ? ['console', 'debugger'] : [],
    },
    define: {
      // setting vue-i18-next
      // Suppress warning
      __INTLIFY_PROD_DEVTOOLS__: false,
      __APP_INFO__: JSON.stringify(__APP_INFO__),
    },
    css: {
      preprocessorMaxWorkers: 0,
      preprocessorOptions: {
        less: {
          modifyVars: generateModifyVars(),
          javascriptEnabled: true,
        },
      },
    },

    // The vite plugin used by the project. The quantity is large, so it is separately extracted and managed
    plugins: createVitePlugins(viteEnv, isBuild),
    // 빌드 구성 미리 로드（스크롤 없이 볼 수 있는 성능)
    optimizeDeps: {
      esbuildOptions: {
        target: 'es2020',
      },
      exclude: [
        //vite4를 업그레이드한 후 온라인 종속성을 제외해야 합니다.
        '@sync/online',
      ],
    },
  };
};
