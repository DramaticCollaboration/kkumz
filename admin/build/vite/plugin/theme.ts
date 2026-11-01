/**
 * Vite plugin for website theme color switching
 * https://github.com/anncwb/vite-plugin-theme
 */
import type { PluginOption } from 'vite';
import path from 'path';
import { viteThemePlugin, antdDarkThemePlugin, mixLighten, mixDarken, tinycolor } from '@rys-fe/vite-plugin-theme';
import { getThemeColors, generateColors } from '../../config/themeConfig';
import { generateModifyVars } from '../../generate/generateModifyVars';

export function configThemePlugin(isBuild: boolean): PluginOption[] {
  const colors = generateColors({
    mixDarken,
    mixLighten,
    tinycolor,
  });

  // 업데이트 시작-테마 색상 전환이 적용되지 않고 컴파일 후 검은 화면이 나타나는 문제를 수정했습니다.-----------------------
  // https://github.com/vbenjs/vue-vben-admin/issues/1445
  // viteThemePlugin 플러그인을 추출하면 아래의 다양한 환경에 따라 적용이 설정됩니다.
  const vite_theme_plugin = viteThemePlugin({
    resolveSelector: (s) => {
      s = s.trim();
      switch (s) {
        case '.ant-steps-item-process .ant-steps-item-icon > .ant-steps-icon':
          return '.ant-steps-item-icon > .ant-steps-icon';
        case '.ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled)':
        case '.ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled):hover':
        case '.ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled):active':
          return s;
        case '.ant-steps-item-icon > .ant-steps-icon':
          return s;
        case '.ant-select-item-option-selected:not(.ant-select-item-option-disabled)':
          return s;
        default:
          if (s.indexOf('.ant-btn') >= -1) {
            // 버튼이 다시 맞춤설정되었으며 덮어쓰기를 방지하기 위해 클래스를 필터링해야 합니다.
            return s;
          }
      }
      return s.startsWith('[data-theme') ? s : `[data-theme] ${s}`;
    },
    colorVariables: [...getThemeColors(), ...colors],
  });
  vite_theme_plugin.forEach(function (item) {
    //vite:theme 플러그인을 위한 특별 구성
    if ('vite:theme' === item.name) {
      // 패키징 시 강제 제거: "post"，vite 2.6.x에 맞게 조정됨. 그렇지 않으면 생성된 앱 테마 스타일이 비동기 변환으로 인해 비어 있게 됩니다.(code, id) {코드가 올바르게 획득되지 않았습니다.
      if (isBuild) {
        delete item.enforce;
      }
    }
  });

  const plugin = [
    vite_theme_plugin,
    antdDarkThemePlugin({
      preloadFiles: [
        // path.resolve(process.cwd(), 'node_modules/ant-design-vue/dist/reset.css'),
        //path.resolve(process.cwd(), 'node_modules/ant-design-vue/dist/antd.dark.less'),
        path.resolve(process.cwd(), 'src/design/index.less'),
      ],
      filter: (id) => (isBuild ? !id.endsWith('antd.less') : true),
      // extractCss: false,
      darkModifyVars: {
        ...generateModifyVars(true),
        'text-color': '#c9d1d9',
        'primary-1': 'rgb(255 255 255 / 8%)',
        'text-color-base': '#c9d1d9',
        'component-background': '#151515',
        'heading-color': 'rgb(255 255 255 / 65%)',
        // black: '#0e1117',
        // #8b949e
        'text-color-secondary': '#8b949e',
        'border-color-base': '#303030',
        'header-light-bottom-border-color': '#303030',
        // 'border-color-split': '#30363d',
        'item-active-bg': '#111b26',
        'app-content-background': '#1e1e1e',
        'tree-node-selected-bg': '#11263c',

        'alert-success-border-color': '#274916',
        'alert-success-bg-color': '#162312',
        'alert-success-icon-color': '#49aa19',
        'alert-info-border-color': '#153450',
        'alert-info-bg-color': '#111b26',
        'alert-info-icon-color': '#177ddc',
        'alert-warning-border-color': '#594214',
        'alert-warning-bg-color': '#2b2111',
        'alert-warning-icon-color': '#d89614',
        'alert-error-border-color': '#58181c',
        'alert-error-bg-color': '#2a1215',
        'alert-error-icon-color': '#a61d24',
      },
    }),
  ];

  return plugin as unknown as PluginOption[];
}
