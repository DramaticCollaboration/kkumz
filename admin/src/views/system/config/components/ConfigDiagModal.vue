<template>
  <BasicModal
    v-bind="$attrs"
    :title="t('appUi.ka6d640bd')"
    :width="520"
    :min-height="200"
    :show-ok-btn="false"
    :cancel-text="t('appUi.k94b7dba1')"
    @register="registerModal"
  >
    <div v-if="diagResult" class="p-4">
      <a-result
        :status="diagResult.success ? 'success' : 'warning'"
        :title="diagResult.success ? t('appUi.kdd21c095') : t('appUi.k4dd37492')"
        :sub-title="diagResult.message"
      >
        <template #extra>
          <div class="bg-gray-100 dark:bg-gray-800 p-3 rounded text-left text-xs font-mono space-y-1">
            <div>{{ t('appUi.k64c7e6c6') }} <span class="font-bold text-blue-600">{{ diagResult.latencyMs }}ms</span></div>
            <div v-for="(v, k) in diagResult.details" :key="k">
              {{ k }}: {{ v }}
            </div>
          </div>
        </template>
      </a-result>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { ConnectionTestResponse } from '../config.api';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

const diagResult = ref<ConnectionTestResponse | null>(null);

const [registerModal] = useModalInner((data) => {
  diagResult.value = data?.diagResult || null;
});
</script>
