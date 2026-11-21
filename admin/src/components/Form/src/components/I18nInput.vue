<template>
  <div class="sync-i18n-input flex flex-col gap-1.5 w-full">
    <div class="flex items-center gap-2">
      <span class="sync-i18n-badge">KO</span>
      <a-input
        :value="localeValues['ko-KR']"
        :placeholder="placeholderKo || t('component.i18n.koPlaceholder')"
        :disabled="disabled"
        allow-clear
        @update:value="(val) => handleUpdate('ko-KR', val)"
      />
    </div>
    <div class="flex items-center gap-2">
      <span class="sync-i18n-badge">EN</span>
      <a-input
        :value="localeValues['en-US']"
        :placeholder="placeholderEn || t('component.i18n.enPlaceholder')"
        :disabled="disabled"
        allow-clear
        @update:value="(val) => handleUpdate('en-US', val)"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { reactive, watch } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';

  defineOptions({ name: 'I18nInput' });

  const props = defineProps<{
    value?: string | Record<string, string>;
    disabled?: boolean;
    placeholderKo?: string;
    placeholderEn?: string;
  }>();

  const emit = defineEmits<{
    (e: 'update:value', val: string): void;
    (e: 'change', val: string): void;
  }>();

  const { t } = useI18n();

  const localeValues = reactive<Record<string, string>>({
    'ko-KR': '',
    'en-US': '',
  });

  function parseIncoming(raw?: string | Record<string, string>) {
    if (!raw) {
      localeValues['ko-KR'] = '';
      localeValues['en-US'] = '';
      return;
    }
    if (typeof raw === 'object') {
      localeValues['ko-KR'] = raw['ko-KR'] || raw['ko'] || '';
      localeValues['en-US'] = raw['en-US'] || raw['en'] || '';
      return;
    }
    try {
      const parsed = JSON.parse(raw);
      if (parsed && typeof parsed === 'object') {
        localeValues['ko-KR'] = parsed['ko-KR'] || parsed['ko'] || '';
        localeValues['en-US'] = parsed['en-US'] || parsed['en'] || '';
        return;
      }
    } catch {
      localeValues['ko-KR'] = raw;
      localeValues['en-US'] = '';
    }
  }

  watch(
    () => props.value,
    (val) => parseIncoming(val),
    { immediate: true }
  );

  function handleUpdate(locale: 'ko-KR' | 'en-US', val: string) {
    localeValues[locale] = val ?? '';
    const payload: Record<string, string> = {};
    if (localeValues['ko-KR'].trim()) {
      payload['ko-KR'] = localeValues['ko-KR'].trim();
    }
    if (localeValues['en-US'].trim()) {
      payload['en-US'] = localeValues['en-US'].trim();
    }
    const serialized = JSON.stringify(payload);
    emit('update:value', serialized);
    emit('change', serialized);
  }
</script>

<style scoped lang="less">
  .sync-i18n-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 24px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 600;
    background: var(--ant-color-fill-secondary, #f1f5f9);
    color: var(--ant-color-text-secondary, #475569);
    flex-shrink: 0;
  }
</style>
