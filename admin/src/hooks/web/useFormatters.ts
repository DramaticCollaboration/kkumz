import { computed, unref } from 'vue';
import dayjs from 'dayjs';
import { useLocale } from '/@/locales/useLocale';

/**
 * 활성 로케일(ko-KR / en-US) 기반 날짜·숫자·통화·백분율 표준 포매터 훅
 */
export function useFormatters() {
  const { getLocale } = useLocale();
  const activeLocale = computed(() => unref(getLocale) || 'ko-KR');

  function formatDate(value?: string | number | Date | dayjs.Dayjs | null, pattern?: string): string {
    if (!value) return '-';
    const d = dayjs(value);
    if (!d.isValid()) return String(value);
    if (pattern) {
      return d.format(pattern);
    }
    return activeLocale.value === 'en-US' ? d.format('MMM D, YYYY') : d.format('YYYY-MM-DD');
  }

  function formatDateTime(value?: string | number | Date | dayjs.Dayjs | null, pattern?: string): string {
    if (!value) return '-';
    const d = dayjs(value);
    if (!d.isValid()) return String(value);
    if (pattern) {
      return d.format(pattern);
    }
    return activeLocale.value === 'en-US' ? d.format('MMM D, YYYY HH:mm:ss') : d.format('YYYY-MM-DD HH:mm:ss');
  }

  function formatNumber(value?: number | string | null, maximumFractionDigits = 2): string {
    if (value === null || value === undefined || value === '') return '-';
    const num = Number(value);
    if (Number.isNaN(num)) return String(value);
    return new Intl.NumberFormat(activeLocale.value, { maximumFractionDigits }).format(num);
  }

  function formatCurrency(value?: number | string | null, currency?: string): string {
    if (value === null || value === undefined || value === '') return '-';
    const num = Number(value);
    if (Number.isNaN(num)) return String(value);
    const resolvedCurrency = currency || (activeLocale.value === 'en-US' ? 'USD' : 'KRW');
    return new Intl.NumberFormat(activeLocale.value, {
      style: 'currency',
      currency: resolvedCurrency,
      maximumFractionDigits: resolvedCurrency === 'KRW' ? 0 : 2,
    }).format(num);
  }

  function formatPercent(value?: number | string | null, fractionDigits = 1): string {
    if (value === null || value === undefined || value === '') return '-';
    const num = Number(value);
    if (Number.isNaN(num)) return String(value);
    return `${new Intl.NumberFormat(activeLocale.value, {
      minimumFractionDigits: fractionDigits,
      maximumFractionDigits: fractionDigits,
    }).format(num)}%`;
  }

  return {
    activeLocale,
    formatDate,
    formatDateTime,
    formatNumber,
    formatCurrency,
    formatPercent,
  };
}
