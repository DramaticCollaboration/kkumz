import type { LocaleSetting, LocaleType } from '/#/config';

import { defineStore } from 'pinia';
import { store } from '/@/store';

import { LOCALE_KEY } from '/@/enums/cacheEnum';
import { createLocalStorage } from '/@/utils/cache';
import {
  LOCALE,
  localeSetting,
  normalizeLocale,
  getLocaleCookie,
  setLocaleCookie,
  detectBrowserLocale,
} from '/@/settings/localeSetting';

const ls = createLocalStorage();

function resolveInitialLocaleSetting(): LocaleSetting {
  const stored = ls.get(LOCALE_KEY) as Partial<LocaleSetting> | null;
  const cookieLocale = getLocaleCookie();
  const initialLocale: LocaleType = stored?.locale
    ? normalizeLocale(stored.locale)
    : cookieLocale || detectBrowserLocale() || localeSetting.locale;

  return {
    ...localeSetting,
    ...(stored || {}),
    showPicker: true,
    locale: initialLocale,
    fallback: LOCALE.KO_KR,
    availableLocales: [LOCALE.KO_KR, LOCALE.EN_US],
  };
}

const lsLocaleSetting = resolveInitialLocaleSetting();

interface LocaleState {
  localInfo: LocaleSetting;
  pathTitleMap: Record<string, string>;
  appIndexTheme: string;
  appMainPth: string;
}

export const useLocaleStore = defineStore('app-locale', {
  state: (): LocaleState => ({
    localInfo: lsLocaleSetting,
    pathTitleMap: {},
    appIndexTheme: '',
    appMainPth: '',
  }),
  getters: {
    getShowPicker(): boolean {
      return this.localInfo?.showPicker !== false;
    },
    getLocale(): LocaleType {
      return normalizeLocale(this.localInfo?.locale);
    },
    getPathTitle: (state) => {
      return (path: string) => state.pathTitleMap[path];
    },
    getAppIndexTheme(): string {
      return this.appIndexTheme;
    },
    getAppMainPth(): string {
      return this.appMainPth;
    },
  },
  actions: {
    setLocaleInfo(info: Partial<LocaleSetting>) {
      const nextLocale = info.locale ? normalizeLocale(info.locale) : normalizeLocale(this.localInfo.locale);
      this.localInfo = {
        ...this.localInfo,
        ...info,
        locale: nextLocale,
        fallback: LOCALE.KO_KR,
        availableLocales: [LOCALE.KO_KR, LOCALE.EN_US],
      };
      ls.set(LOCALE_KEY, this.localInfo);
      setLocaleCookie(nextLocale);
    },
    initLocale() {
      const normalized = resolveInitialLocaleSetting();
      this.setLocaleInfo(normalized);
    },
    setPathTitle(path: string, title: string) {
      this.pathTitleMap[path] = title;
    },
    clearPathTitles() {
      this.pathTitleMap = {};
    },
    setAppIndexTheme(theme: string) {
      this.appIndexTheme = theme;
    },
    setAppMainPth(path: string) {
      this.appMainPth = path;
    },
  },
});

export function useLocaleStoreWithOut() {
  return useLocaleStore(store);
}
