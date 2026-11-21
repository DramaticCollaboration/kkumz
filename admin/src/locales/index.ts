import { i18n } from '/@/locales/setupI18n';
import { useI18n } from '/@/hooks/web/useI18n';

/**
 * 전역 다국어 변환 함수 ($t)
 * - 등록된 i18n 키 여부를 i18n.global.te(key)로 정확히 판별하여
 *   마침표(.)가 포함된 번역 문장이 기본값으로 덮어쓰이는 문제를 방지합니다.
 */
export function $t(key: string, defaultMessageOrArgs?: any): string {
  if (!key) return '';
  try {
    const { t } = useI18n();

    if (typeof defaultMessageOrArgs === 'string') {
      if (i18n?.global?.te && i18n.global.te(key)) {
        const translated = t(key);
        return translated || defaultMessageOrArgs;
      }
      return defaultMessageOrArgs;
    }

    if (i18n?.global?.te && !i18n.global.te(key)) {
      return key;
    }

    const translated = t(key, defaultMessageOrArgs);
    return translated || key;
  } catch {
    if (typeof defaultMessageOrArgs === 'string') {
      return defaultMessageOrArgs;
    }
    return key;
  }
}
