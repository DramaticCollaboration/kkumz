import type { UserConfig, ConfigEnv } from 'vite';
import pkg from './package.json';
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

export default async ({ command, mode }: ConfigEnv): Promise<UserConfig> => {
  const root = process.cwd();

  const env = loadEnv(mode, root);
  const appInfo = {
    pkg: { dependencies, devDependencies, name, version },
    lastBuildTime: env.VITE_APP_BUILD_TIME || process.env.VITE_APP_BUILD_TIME || version,
  };

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
          replacement: 'vue-i18n/dist/vue-i18n.esm-bundler.js',
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
          find: /@api\//,
          replacement: pathResolve('src/api') + '/',
        },
        {
          find: /@generated\//,
          replacement: pathResolve('src/api/generated') + '/',
        },
        {
          find: /@\//,
          replacement: pathResolve('src') + '/',
        },
        // /#/xxxx => types/xxxx
        {
          find: /#\//,
          replacement: pathResolve('types') + '/',
        },
      ],
    },
    server: {
      host: true,
      port: VITE_PORT,
      proxy: createProxy(VITE_PROXY),
    },
    build: {
      minify: 'esbuild',
      target: 'es2015',
      cssTarget: 'chrome80',
      outDir: OUTPUT_DIR,
      rolldownOptions: {
        checks: {
          pluginTimings: false,
        },
        // 중요한 코드가 삭제되어 패키징 후 기능 이상이 발생하는 것을 방지하려면 칩 제거 최적화를 끄십시오.
        treeshake: false,
        output: {
          chunkFileNames: 'js/[name]-[hash].js', // 파일 이름의 이름을 소개합니다
          entryFileNames: 'js/[name]-[hash].js', // 패키지 항목 파일 이름
          codeSplitting: {
            groups: [
              {
                name: 'vue-vendor',
                test: /[\\/]node_modules[\\/](vue|vue-router|pinia|vue-i18n)[\\/]/,
                priority: 50
              },
              {
                name: 'ant-vendor',
                test: /[\\/]node_modules[\\/](ant-design-vue|@ant-design)[\\/]/,
                priority: 40
              },
              {
                name: 'emoji-mart-vue-fast',
                test: /[\\/]node_modules[\\/]emoji-mart-vue-fast[\\/]/,
                priority: 30
              },
              {
                name: 'echarts-vendor',
                test: /[\\/]node_modules[\\/]echarts[\\/]/,
                priority: 20
              },
              {
                name: 'vditor-vendor',
                test: /[\\/]node_modules[\\/]vditor[\\/]/,
                priority: 10
              }
            ]
          }
        },
      },
      // brotliSize 디스플레이를 끄면 패키징 시간이 약간 단축될 수 있습니다.
      reportCompressedSize: false,
      chunkSizeWarningLimit: 3200,
    },
    // esbuild: {
    //   //전역 console.log 지우기 및 디버그
    //   drop: isBuild ? ['console', 'debugger'] : [],
    // },
    define: {
      // setting vue-i18-next
      // Suppress warning
      __INTLIFY_PROD_DEVTOOLS__: false,
      __APP_INFO__: JSON.stringify(appInfo),
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
    plugins: await createVitePlugins(viteEnv, isBuild),
    // 빌드 구성 미리 로드（스크롤 없이 볼 수 있는 성능)
    optimizeDeps: {
      exclude: [
        //vite4를 업그레이드한 후 온라인 종속성을 제외해야 합니다.
        '@sync/online',
        // iconify를 dev dependency optimization 대상에서 제외합니다.
        '@iconify/iconify',
      ],
    },
  };
};
