import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const apiTarget = env.VITE_KKUMZ_API_TARGET || 'http://localhost:9580';
  const siteAccessKey = env.VITE_KKUMZ_SITE_ACCESS_KEY || 'ak_site_malgeunmul_e44c0342d91e';

  return {
    plugins: [vue()],
    server: {
      host: '0.0.0.0',
      port: 5173,
      proxy: {
        '/api/v1/kkumz': {
          target: apiTarget,
          changeOrigin: true,
          headers: {
            'X-Kkumz-Site-Key': siteAccessKey,
          },
        },
      },
    },
  };
});