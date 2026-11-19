import { generateAntColors } from '../../../build/config/themeConfig';
import { useAppStore } from '/@/store/modules/app';

/**
 * Apply a new primary theme color using native CSS custom properties.
 *
 * Sets `--primary-color` plus `--primary-1` … `--primary-10` (light palette)
 * and `--primary-dark-1` … `--primary-dark-10` (dark palette) on the document
 * root element.  Ant Design Vue 4.x ConfigProvider handles component-level
 * theming via its `theme` prop in App.vue.
 */
export async function changeTheme(color: string) {
  const appStore = useAppStore();
  appStore.setProjectConfig({ themeColor: color });

  const style = document.documentElement.style;
  style.setProperty('--primary-color', color);

  // Light palette (indices 0-9 → --primary-1 … --primary-10)
  const lightColors = generateAntColors(color, 'default');
  lightColors.forEach((c, i) => {
    style.setProperty(`--primary-${i + 1}`, c);
  });

  // Dark palette
  const darkColors = generateAntColors(color, 'dark');
  darkColors.forEach((c, i) => {
    style.setProperty(`--primary-dark-${i + 1}`, c);
  });
}
