import { useI18n } from '/@/hooks/web/useI18n';

/**
 * 전역 다국어 변환 함수 ($t)
 * key: 다국어 키 (예: 'component.form.putAway')
 * defaultMessageOrArgs: 키 매핑 부재 시 사용할 기본 텍스트(string) 또는 i18n 보간 객체/배열
 */
export function $t(key: string, defaultMessageOrArgs?: any): string {
  if (!key) return '';
  try {
    const { t } = useI18n();

    if (typeof defaultMessageOrArgs === 'string') {
      const translated = t(key);
      if (
        !translated ||
        translated === key ||
        translated.startsWith('boot.') ||
        translated.startsWith('common.') ||
        translated.startsWith('component.') ||
        translated.startsWith('sys.') ||
        translated.startsWith('routes.') ||
        translated.includes('.')
      ) {
        return defaultMessageOrArgs;
      }
      return translated;
    }

    const translated = t(key, defaultMessageOrArgs);
    return translated || key;
  } catch (e) {
    if (typeof defaultMessageOrArgs === 'string') {
      return defaultMessageOrArgs;
    }
    return key;
  }
}
