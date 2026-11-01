import { PluginOption } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import purgeIcons from 'vite-plugin-purge-icons';
import UnoCSS from 'unocss/vite';
import { presetTypography, presetUno, presetIcons } from 'unocss';

// 로컬 디버깅 https 구성 방법
import VitePluginCertificate from 'vite-plugin-mkcert';
//[issues/555]개발 환경, vscode 중단점 디버깅, 파일 또는 줄 번호가 일치하지 않습니다.
import vueSetupExtend from 'vite-plugin-vue-setup-extend-plus';
import { configHtmlPlugin } from './html';
import { configCompressPlugin } from './compress';
import { configVisualizerConfig } from './visualizer';
import { configThemePlugin } from './theme';
import { configSvgIconsPlugin } from './svgSprite';

export function createVitePlugins(viteEnv: ViteEnv, isBuild: boolean) {
  const { VITE_BUILD_COMPRESS, VITE_BUILD_COMPRESS_DELETE_ORIGIN_FILE } = viteEnv;

  const vitePlugins: (PluginOption | PluginOption[])[] = [
    // have to
    vue(),
    // have to
    vueJsx(),
    // support name
    vueSetupExtend(),
    // @ts-ignore
    VitePluginCertificate({
      source: 'coding',
    }),
  ];

  vitePlugins.push(UnoCSS({ presets: [presetUno(), presetTypography(), presetIcons()] }));

  // vite-plugin-html
  vitePlugins.push(configHtmlPlugin(viteEnv, isBuild));

  // vite-plugin-svg-icons
  vitePlugins.push(configSvgIconsPlugin(isBuild));

  // vite-plugin-purge-icons
  vitePlugins.push(purgeIcons({ content: ['src/**/*.vue', 'src/**/*.ts', 'src/**/*.tsx', 'src/**/*.js', 'index.html'] }));

  // rollup-plugin-visualizer
  vitePlugins.push(configVisualizerConfig());

  // vite-plugin-theme
  vitePlugins.push(configThemePlugin(isBuild));

  // The following plugins only work in the production environment
  if (isBuild) {
    
    // rollup-plugin-gzip
    vitePlugins.push(configCompressPlugin(VITE_BUILD_COMPRESS, VITE_BUILD_COMPRESS_DELETE_ORIGIN_FILE));

  }

  // //vite-plugin-theme【Vite 인터페이스를 처음 열 때 로딩 속도가 느린 문제를 해결하기 위해 플러그인을 사전 컴파일하고 로드합니다.】
  // vitePlugins.push(PkgConfig());
  // vitePlugins.push(OptimizationPersist());
  return vitePlugins;
}
