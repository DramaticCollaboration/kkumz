import { addClass, hasClass, removeClass } from '/@/utils/domUtils';

/**
 * Toggle dark / light mode on the HTML root element.
 *
 * Sets `data-theme` attribute and toggles the `dark` CSS class.
 * Ant Design Vue 4.x handles dark component styling via ConfigProvider's
 * `theme.algorithm` (darkAlgorithm) in App.vue, so no runtime CSS
 * injection is needed.
 */
export async function updateDarkTheme(mode: string | null = 'light') {
  const htmlRoot = document.getElementById('htmlRoot');
  if (!htmlRoot) {
    return;
  }
  const hasDarkClass = hasClass(htmlRoot, 'dark');
  if (mode === 'dark') {
    htmlRoot.setAttribute('data-theme', 'dark');
    if (!hasDarkClass) {
      addClass(htmlRoot, 'dark');
    }
  } else {
    htmlRoot.setAttribute('data-theme', 'light');
    if (hasDarkClass) {
      removeClass(htmlRoot, 'dark');
    }
  }
}
