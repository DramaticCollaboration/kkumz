/**
 * Multi-language related operations (BCP 47: ko-KR / en-US)
 */
import type { LocaleType } from '/#/config';

import dayjs from 'dayjs';
import 'dayjs/locale/ko';
import 'dayjs/locale/en';
import { i18n } from './setupI18n';
import { useLocaleStoreWithOut } from '/@/store/modules/locale';
import { unref, computed } from 'vue';
import { loadLocalePool, setHtmlPageLang } from './helper';
import { LOCALE, normalizeLocale } from '/@/settings/localeSetting';
import { getToken, setAuthCache } from '/@/utils/auth';
import { DB_DICT_DATA_KEY } from '/@/enums/cacheEnum';
import { updateUserLocaleApi } from '/@/api/sys/user';

interface LangModule {
  message: Recordable;
  dateLocale?: Recordable | null;
  dateLocaleName?: string;
}

function toDayjsLocale(locale: LocaleType): string {
  return locale === LOCALE.EN_US ? 'en' : 'ko';
}

function setI18nLanguage(locale: LocaleType) {
  const localeStore = useLocaleStoreWithOut();
  const normalized = normalizeLocale(locale);

  if (i18n.mode === 'legacy') {
    i18n.global.locale = normalized;
  } else {
    (i18n.global.locale as any).value = normalized;
  }
  localeStore.setLocaleInfo({ locale: normalized });
  localeStore.clearPathTitles();
  setHtmlPageLang(normalized);
  dayjs.locale(toDayjsLocale(normalized));
}

export function useLocale() {
  const localeStore = useLocaleStoreWithOut();
  const getLocale = computed(() => normalizeLocale(localeStore.getLocale));
  const getShowLocalePicker = computed(() => localeStore.getShowPicker);

  const getAntdLocale = computed((): any => {
    return i18n.global.getLocaleMessage(unref(getLocale))?.antdLocale ?? {};
  });

  async function changeLocale(locale: LocaleType | string) {
    const normalized = normalizeLocale(locale);
    const globalI18n = i18n.global;
    const currentLocale = normalizeLocale(unref(globalI18n.locale) as string);
    if (currentLocale === normalized) {
      return normalized;
    }

    if (!loadLocalePool.includes(normalized)) {
      const imported =
        normalized === LOCALE.EN_US
          ? await import('./lang/en-US')
          : await import('./lang/ko-KR');
      const langModule = imported.default as LangModule;
      if (!langModule) return currentLocale;

      globalI18n.setLocaleMessage(normalized, langModule.message);
      loadLocalePool.push(normalized);
    }

    // 로케일 변경 시 기존 언어 기준 사전 캐시 무효화
    setAuthCache(DB_DICT_DATA_KEY, null);
    setI18nLanguage(normalized);

    // 로그인 세션이 활성화된 경우 사용자 선호 언어를 서버 DB에 비동기 동기화
    if (getToken()) {
      try {
        await updateUserLocaleApi(normalized);
      } catch {
        // 선호 언어 서버 동기화 실패가 UI 언어 전환을 차단하지 않도록 무시
      }
    }

    return normalized;
  }

  return {
    getLocale,
    getShowLocalePicker,
    changeLocale,
    getAntdLocale,
  };
}
