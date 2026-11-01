import type { DropMenu } from '../components/Dropdown';
import type { LocaleSetting, LocaleType } from '/#/config';

export const LOCALE: { [key: string]: LocaleType } = {
  KO_KR: 'ko_KR',
  EN_US: 'en',
};

export const localeSetting: LocaleSetting = {
  // 언어 선택기 표시 여부
  showPicker: true,
  // 현재 언어
  locale: LOCALE.KO_KR,
  // 기본 언어
  fallback: LOCALE.KO_KR,
  // 허용되는 언어
  availableLocales: [LOCALE.KO_KR, LOCALE.EN_US],
};

// 언어 목록
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
