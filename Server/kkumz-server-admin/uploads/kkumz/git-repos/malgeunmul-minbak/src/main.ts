import { createApp, h, reactive } from 'vue';
import App from './App.vue';
import localTheme from './theme.json';
import localLayout from './layout.json';

const state = reactive({
  tokens: (localTheme as any).designTokens || localTheme || {},
  layout: (localLayout as any).layoutSchema || localLayout || {},
  runtime: null as Record<string, any> | null,
});

async function bootstrap() {
  try {
    const res = await fetch('/api/v1/kkumz/sites/malgeunmul-minbak/runtime', {
      headers: {
        'X-Kkumz-Site-Key': import.meta.env.VITE_KKUMZ_SITE_ACCESS_KEY || '',
      },
    });
    if (res.ok) {
      const json = await res.json();
      state.runtime = json?.result || json?.data || null;
    }
  } catch (err) {
    console.warn('[Kkumz SDK] 로컬 정적 스키마(theme.json / layout.json)로 렌더링합니다.', err);
  }

  createApp({
    render: () => h(App, { tokens: state.tokens, layout: state.layout, runtime: state.runtime }),
  }).mount('#app');
}

bootstrap();