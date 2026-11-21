import type { DropMenu } from '../components/Dropdown';
import type { LocaleSetting, LocaleType } from '/#/config';

export const LOCALE: { [key: string]: LocaleType } = {
  KO_KR: 'ko-KR',
  EN_US: 'en-US',
};

export const LOCALE_COOKIE_NAME = 'empasy_locale';

/**
 * 레거시 로케일 값(ko_KR, ko, en, en_US 등)을 BCP 47 표준(ko-KR, en-US)으로 정규화
 */
export function normalizeLocale(raw?: string | null): LocaleType {
  if (!raw || typeof raw !== 'string') {
    return LOCALE.KO_KR;
  }
  const canonical = raw.trim().replace('_', '-').toLowerCase();
  if (canonical === 'en' || canonical.startsWith('en-')) {
    return LOCALE.EN_US;
  }
  return LOCALE.KO_KR;
}

/**
 * SSO 공통 도메인 쿠키에서 로케일 조회
 */
export function getLocaleCookie(): LocaleType | null {
  if (typeof document === 'undefined') {
    return null;
  }
  const match = document.cookie.match(new RegExp(`(?:^|;\\s*)${LOCALE_COOKIE_NAME}=([^;]+)`));
  return match ? normalizeLocale(decodeURIComponent(match[1])) : null;
}

/**
 * SSO 공통 쿠키에 로케일 기록 (서브도메인 간 언어 유지)
 */
export function setLocaleCookie(locale: LocaleType): void {
  if (typeof document === 'undefined') {
    return;
  }
  const normalized = normalizeLocale(locale);
  const maxAge = 60 * 60 * 24 * 365;
  let cookie = `${LOCALE_COOKIE_NAME}=${encodeURIComponent(normalized)}; Path=/; Max-Age=${maxAge}; SameSite=Lax`;
  const host = typeof window !== 'undefined' ? window.location.hostname : '';
  if (host && host.endsWith('.empasy.io')) {
    cookie += '; Domain=.empasy.io';
  }
  document.cookie = cookie;
}

/**
 * 브라우저 기본 언어 감지
 */
export function detectBrowserLocale(): LocaleType {
  if (typeof navigator === 'undefined') {
    return LOCALE.KO_KR;
  }
  const lang = navigator.language || (Array.isArray(navigator.languages) ? navigator.languages[0] : '');
  return normalizeLocale(lang);
}

export const localeSetting: LocaleSetting = {
  // 언어 선택기 표시 여부
  showPicker: true,
  // 현재 언어
  locale: LOCALE.KO_KR,
  // 기본 폴백 언어
  fallback: LOCALE.KO_KR,
  // 지원 언어 목록
  availableLocales: [LOCALE.KO_KR, LOCALE.EN_US],
};

// 언어 선택 목록
export const localeList: DropMenu[] = [
  {
    text: '한국어',
    event: LOCALE.KO_KR,
  },
  {
    text: 'English',
    event: LOCALE.EN_US,
  },
];
