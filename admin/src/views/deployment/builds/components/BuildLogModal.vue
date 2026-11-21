<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('appUi.keea7e0cc')"
    :width="760"
    :showOkBtn="false"
    :cancelText="t('appUi.k94b7dba1')"
  >
    <div v-if="record" class="space-y-3">
      <div class="flex flex-wrap items-center justify-between gap-2 p-3 rounded-lg bg-slate-50 border border-slate-200">
        <div class="flex items-center gap-2">
          <Tag :color="record.status === 'SUCCESS' ? 'success' : 'error'">{{ record.status }}</Tag>
          <span class="font-bold text-sm">{{ record.brandName || record.siteSlug }}</span>
          <span class="text-xs text-slate-500">v{{ record.versionNo }} · {{ record.triggerType }}</span>
        </div>
        <span class="text-xs font-mono text-slate-500">{{ t('appUi.k2e86c9b1') }} {{ record.durationMs }}ms</span>
      </div>

      <div v-if="record.errorSummary" class="p-3 rounded-lg bg-rose-50 border border-rose-200 text-xs text-rose-800 font-bold">
        {{ t('appUi.k0e84f9bb') }} {{ record.errorSummary }}
      </div>

      <div v-if="record.aiFixSuggestion" class="p-3 rounded-lg bg-slate-50 border border-slate-200 text-xs text-slate-700">
        <strong>{{ t('appUi.k40f97194') }}</strong> {{ record.aiFixSuggestion }}
      </div>

      <div>
        <div class="text-xs font-bold text-slate-700 mb-1">{{ t('appUi.k15ef8dbd') }}</div>
        <pre class="p-4 rounded-xl bg-slate-950 text-emerald-300 text-xs font-mono whitespace-pre-wrap max-h-80 overflow-y-auto">{{ record.logOutput }}</pre>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { Tag } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const record = ref<Recordable | null>(null);

  const [registerModal] = useModalInner((data) => {
    record.value = data?.record || null;
  });
</script>
