import { PluginOption } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
// import purgeIcons from 'vite-plugin-purge-icons';
import UnoCSS from 'unocss/vite';
import { presetTypography } from 'unocss';
import { presetWind3 } from '@unocss/preset-wind3';
import { resolve, relative, isAbsolute } from 'path';
import fs from 'fs-extra';

//[issues/555]개발 환경, vscode 중단점 디버깅, 파일 또는 줄 번호가 일치하지 않습니다.
import vueSetupExtend from 'vite-plugin-vue-setup-extend-plus';
import { configHtmlPlugin } from './html';
import { configCompressPlugin } from './compress';
import { configVisualizerConfig } from './visualizer';
import { configThemePlugin } from './theme';
import { configSvgIconsPlugin } from './svgSprite';
// //사전 컴파일된 로딩 플러그인(vite3 무효화를 지원하지 않습니다)
// import OptimizationPersist from 'vite-plugin-optimize-persist';
// import PkgConfig from 'vite-plugin-package-config';

export async function createVitePlugins(viteEnv: ViteEnv, isBuild: boolean) {
  const { VITE_BUILD_COMPRESS, VITE_BUILD_COMPRESS_DELETE_ORIGIN_FILE, VITE_USE_MKCERT } = viteEnv;

  const vitePlugins: (PluginOption | PluginOption[])[] = [
    // have to
    vue({
      template: {
        compilerOptions: {
          isCustomElement: (tag) => tag === 'iconify-icon',
        },
      },
    }),
    // have to
    vueJsx(),
    // support name
    vueSetupExtend(),
  ];

  if (!isBuild && VITE_USE_MKCERT === true) {
    const [majorVersion = '0'] = process.versions.node.split('.');
    if (Number(majorVersion) < 22) {
      console.warn('[vite] mkcert plugin disabled: node 22+ runtime is required.');
      vitePlugins.push({
        name: 'mkcert-unavailable',
      });
    } else {
      try {
        const { default: VitePluginCertificate } = await import('vite-plugin-mkcert');
        vitePlugins.push(
          // @ts-ignore
          VitePluginCertificate({
            source: 'coding',
          }),
        );
      } catch (error) {
        console.warn('[vite] mkcert plugin disabled:', error);
        vitePlugins.push({
          name: 'mkcert-unavailable',
        });
      }
    }
  }

  // UnoCSS v66+: preset-uno is deprecated, use preset-wind3 instead
  vitePlugins.push(UnoCSS({ presets: [presetWind3(), presetTypography()] }));

  // index.html title 주입
  vitePlugins.push(configHtmlPlugin(viteEnv, isBuild));

  // vite-plugin-svg-icons
  vitePlugins.push(configSvgIconsPlugin(isBuild));

// vite-plugin-purge-icons removed

  // rollup-plugin-visualizer
  vitePlugins.push(await configVisualizerConfig());

  // vite-plugin-theme
  vitePlugins.push(configThemePlugin(isBuild));

  // The following plugins only work in the production environment
  if (isBuild) {

    // rollup-plugin-gzip
    vitePlugins.push(configCompressPlugin(VITE_BUILD_COMPRESS, VITE_BUILD_COMPRESS_DELETE_ORIGIN_FILE));

    // Air-gapped: copy vditor assets from node_modules to build output
    vitePlugins.push({
      name: 'copy-vditor-assets',
      closeBundle: async () => {
        const { OUTPUT_DIR } = await import('../../constant');
        const src = resolve(process.cwd(), 'node_modules/vditor/dist');
        const dest = resolve(process.cwd(), OUTPUT_DIR, 'vditor/dist');
        if (fs.existsSync(src)) {
          fs.copySync(src, dest);
        }
      },
    } as PluginOption);
  } else {
    // Air-gapped dev: serve vditor assets from node_modules via middleware
    vitePlugins.push({
      name: 'serve-vditor-dev',
      configureServer(server) {
        const vditorRoot = resolve(process.cwd(), 'node_modules/vditor');
        server.middlewares.use('/vditor', (req, res, next) => {
          let requestPath = '';
          try {
            requestPath = decodeURIComponent((req.url || '').split('?')[0]).replace(/^\/+/, '');
          } catch {
            return next();
          }
          if (!requestPath.startsWith('dist/')) {
            return next();
          }
          const filePath = resolve(vditorRoot, requestPath);
          const rel = relative(vditorRoot, filePath);
          if (rel.startsWith('..') || isAbsolute(rel)) {
            return next();
          }
          if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
            res.end(fs.readFileSync(filePath));
          } else {
            next();
          }
        });
      },
    } as PluginOption);
  }

  // //vite-plugin-theme【Vite 인터페이스를 처음 열 때 로딩 속도가 느린 문제를 해결하기 위해 플러그인을 사전 컴파일하고 로드합니다.】
  // vitePlugins.push(PkgConfig());
  // vitePlugins.push(OptimizationPersist());
  return vitePlugins;
}
