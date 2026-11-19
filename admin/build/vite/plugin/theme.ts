/**
 * Theme plugin stub.
 *
 * The original @rys-fe/vite-plugin-theme v0.8.6 is incompatible with Vite 6.x
 * (produces an empty app-theme-style CSS file).  Theme colors are now managed
 * via native CSS custom properties set at runtime (see src/logics/theme/index.ts)
 * and Ant Design Vue 4.x ConfigProvider (see App.vue).
 */
import type { PluginOption } from 'vite';

export function configThemePlugin(_isBuild: boolean): PluginOption[] {
  // No build-time plugins required — theming is handled at runtime.
  return [];
}
