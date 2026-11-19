import type { PluginOption } from 'vite';

const HTML_ESCAPE_LOOKUP: Record<string, string> = {
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&#39;',
};

/** index.html 템플릿에 주입될 title 값을 HTML 본문 기준으로 이스케이프한다. */
export function escapeHtmlContent(value: string): string {
  return value.replace(/[&<>"']/g, (char) => HTML_ESCAPE_LOOKUP[char] ?? char);
}

/** Vite index.html의 기존 EJS title placeholder를 안전한 문자열로 치환한다. */
export function renderHtmlTitleTemplate(html: string, title: string): string {
  return html.replace(/<%=\s*title\s*%>/g, escapeHtmlContent(title));
}

/** 외부 EJS 플러그인 없이 title placeholder만 처리하는 Vite HTML 플러그인을 구성한다. */
export function configHtmlPlugin(env: ViteEnv, isBuild: boolean) {
  const { VITE_GLOB_APP_TITLE } = env;

  const htmlPlugin: PluginOption[] = [
    {
      name: 'sync-crawl-html-title',
      apply: isBuild ? 'build' : 'serve',
      transformIndexHtml: {
        order: 'pre',
        handler(html) {
          return renderHtmlTitleTemplate(html, VITE_GLOB_APP_TITLE);
        },
      },
    },
  ];
  return htmlPlugin;
}
