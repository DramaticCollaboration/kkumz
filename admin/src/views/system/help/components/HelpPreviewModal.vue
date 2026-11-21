<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('appUi.k5b156c81', { p0: record?.title || '' })"
    width="960px"
    :showOkBtn="false"
    :cancelText="t('appUi.k94b7dba1')"
  >
    <div v-if="record" class="p-3">
      <HelpSystemContent
        v-if="isSystemGuide(record)"
        :guide="record"
        :default-app-code="defaultAppCode"
        :interactive="false"
      />
      <HelpScreenContent
        v-else
        :guide="record"
        :default-app-code="defaultAppCode"
        :interactive="false"
      />
    </div>
    <a-empty v-else :description="t('appUi.k89924323')" class="my-10" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { DEFAULT_APP_CODE, SysPageHelpModel } from '../help.api';
  import { isSystemGuide } from '../help.data';
  import HelpSystemContent from './HelpSystemContent.vue';
  import HelpScreenContent from './HelpScreenContent.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const defaultAppCode = DEFAULT_APP_CODE;
  const record = ref<SysPageHelpModel | null>(null);

  const [registerModal] = useModalInner((data) => {
    record.value = data?.record || null;
  });
</script>
