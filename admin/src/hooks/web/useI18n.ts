import { i18n } from '/@/locales/setupI18n';

type I18nGlobalTranslation = {
  (key: string): string;
  (key: string, locale: string): string;
  (key: string, locale: string, list: unknown[]): string;
  (key: string, locale: string, named: Record<string, unknown>): string;
  (key: string, list: unknown[]): string;
  (key: string, named: Record<string, unknown>): string;
};

type I18nTranslationRestParameters = [string, any];

function getKey(namespace: string | undefined, key: string) {
  if (!namespace) {
    return key;
  }
  if (key.startsWith(namespace)) {
    return key;
  }
  return `${namespace}.${key}`;
}

/**
 * 문자열이 유효한 등록 i18n 키인 경우 번역 값을 반환하고, 일반 텍스트인 경우 원문을 그대로 반환합니다.
 * BasicTable 컬럼 제목, BasicForm 라벨/플레이스홀더, Description 라벨 자동 반응형 번역에 사용됩니다.
 */
export function translateIfKey(value: unknown): string {
  if (typeof value !== 'string' || !value) {
    return '';
  }
  if (!i18n || !value.includes('.')) {
    return value;
  }
  try {
    if (i18n.global.te(value)) {
      return (i18n.global.t as any)(value);
    }
  } catch {
    // 일반 문자열 폴백
  }
  return value;
}

export function useI18n(namespace?: string): {
  t: I18nGlobalTranslation;
  te: (key: string) => boolean;
  translateIfKey: (value: unknown) => string;
} {
  const normalFn = {
    t: (key: string) => getKey(namespace, key),
    te: (_key: string) => false,
    translateIfKey,
  };

  if (!i18n) {
    return normalFn;
  }

  const { t, te, ...methods } = i18n.global;

  const tFn: I18nGlobalTranslation = (key: string, ...arg: any[]) => {
    if (!key) return '';
    if (!key.includes('.') && !namespace) return key;
    const fullKey = getKey(namespace, key);
    if (!te(fullKey)) {
      return key;
    }
    return t(fullKey, ...(arg as I18nTranslationRestParameters));
  };

  const teFn = (key: string) => {
    if (!key) return false;
    return te(getKey(namespace, key));
  };

  return {
    ...methods,
    t: tFn,
    te: teFn,
    translateIfKey,
  };
}

// 라우팅/메뉴 정적 분석 호환용 식별자
export const t = (key: string) => key;
