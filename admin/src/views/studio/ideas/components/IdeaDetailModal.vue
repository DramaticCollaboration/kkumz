<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('appUi.k2ff72fd1')"
    :width="820"
    :showOkBtn="false"
    :cancelText="t('appUi.k94b7dba1')"
  >
    <div v-if="detail" class="space-y-4">
      <div class="p-4 rounded-xl bg-slate-50 border border-slate-200 flex items-center justify-between gap-4">
        <div class="flex items-center gap-4">
          <div class="w-28 h-12 rounded-lg bg-white border border-slate-200 flex items-center justify-center p-1 overflow-hidden" v-html="detail.logoSvg"></div>
          <div>
            <div class="flex items-center gap-2">
              <Tag color="indigo">{{ detail.siteType }}</Tag>
              <Tag color="green">v{{ detail.currentVersionNo }}</Tag>
              <span class="text-xs font-mono text-slate-500">/s/{{ detail.siteSlug }}</span>
            </div>
            <div class="text-base font-bold text-slate-900 mt-1">{{ detail.selectedBrandName || detail.title }}</div>
            <div class="text-xs text-slate-500">{{ t('appUi.kfea38bc7') }} {{ detail.authorDisplayName }} (@{{ detail.authorUsername }})</div>
          </div>
        </div>
      </div>

      <div>
        <div class="text-xs font-bold text-slate-700 mb-1">{{ t('appUi.k3cc16ebe') }}</div>
        <div class="p-3 rounded-lg bg-slate-50 border border-slate-200 text-xs text-slate-700">
          {{ detail.promptText }}
        </div>
      </div>

      <div>
        <div class="text-xs font-bold text-slate-700 mb-1">{{ t('appUi.kd6cbaa9e') }}</div>
        <pre class="p-3 rounded-lg bg-slate-900 text-slate-100 text-xs whitespace-pre-wrap font-mono max-h-60 overflow-y-auto">{{ detail.specMarkdown }}</pre>
      </div>

      <div v-if="detail.screens?.length">
        <div class="text-xs font-bold text-slate-700 mb-2">{{ t('appUi.kc1b72bf1') }}{{ detail.screens.length }}{{ t('appUi.k3c593db6') }}</div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
          <div v-for="sc in detail.screens" :key="sc.id || sc.screenCode" class="p-2.5 rounded-lg border border-slate-200 bg-white text-xs">
            <div class="font-bold text-slate-800">{{ sc.screenName }} <span class="font-mono text-slate-700">({{ sc.routePath }})</span></div>
            <div class="text-slate-500 mt-0.5">{{ sc.description }}</div>
          </div>
        </div>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { Tag } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getAdminIdeaDetail } from '/@/api/kkumz';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const detail = ref<Recordable | null>(null);

  const [registerModal, { setModalProps }] = useModalInner(async (data) => {
    setModalProps({ loading: true });
    try {
      detail.value = await getAdminIdeaDetail(data.record.id);
    } finally {
      setModalProps({ loading: false });
    }
  });
</script>
