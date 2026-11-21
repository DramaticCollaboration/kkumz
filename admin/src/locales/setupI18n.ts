import type { App } from 'vue';
import type { I18n, I18nOptions } from 'vue-i18n';

import dayjs from 'dayjs';
import 'dayjs/locale/ko';
import 'dayjs/locale/en';
import { createI18n } from 'vue-i18n';
import { setHtmlPageLang, setLoadLocalePool } from './helper';
import { LOCALE, localeSetting, normalizeLocale } from '/@/settings/localeSetting';
import { useLocaleStoreWithOut } from '/@/store/modules/locale';

const { fallback, availableLocales } = localeSetting;

export let i18n: ReturnType<typeof createI18n>;

function toDayjsLocale(locale: string): string {
  return locale === LOCALE.EN_US ? 'en' : 'ko';
}

async function loadLocaleMessage(locale: string) {
  const normalized = normalizeLocale(locale);
  const mod =
    normalized === LOCALE.EN_US
      ? await import('./lang/en-US')
      : await import('./lang/ko-KR');
  return mod.default?.message ?? {};
}

async function createI18nOptions(): Promise<I18nOptions> {
  const localeStore = useLocaleStoreWithOut();
  const locale = normalizeLocale(localeStore.getLocale);

  // 폴백 언어(ko-KR)를 항상 함께 선적재하여 누락 키 발생 시 안전하게 폴백
  const messages: Record<string, any> = {};
  const fallbackMessage = await loadLocaleMessage(fallback);
  messages[fallback] = fallbackMessage;

  setLoadLocalePool((loadLocalePool) => {
    if (!loadLocalePool.includes(fallback)) {
      loadLocalePool.push(fallback);
    }
  });

  if (locale !== fallback) {
    messages[locale] = await loadLocaleMessage(locale);
    setLoadLocalePool((loadLocalePool) => {
      if (!loadLocalePool.includes(locale)) {
        loadLocalePool.push(locale);
      }
    });
  }

  setHtmlPageLang(locale);
  dayjs.locale(toDayjsLocale(locale));

  return {
    legacy: false,
    locale,
    fallbackLocale: fallback,
    messages,
    availableLocales,
    sync: true,
    silentTranslationWarn: true,
    missingWarn: false,
    silentFallbackWarn: true,
  };
}

export async function setupI18n(app: App) {
  const options = await createI18nOptions();
  i18n = createI18n(options) as I18n;
  app.use(i18n);
}
