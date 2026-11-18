<template>
  <BasicModal
    v-bind="$attrs"
    title="원격 엔드포인트 연결 진단 결과"
    :width="520"
    :min-height="200"
    :show-ok-btn="false"
    cancel-text="닫기"
    @register="registerModal"
  >
    <div v-if="diagResult" class="p-4">
      <a-result
        :status="diagResult.success ? 'success' : 'warning'"
        :title="diagResult.success ? '원격 연결 정상' : '원격 서비스 응답 확인 필요'"
        :sub-title="diagResult.message"
      >
        <template #extra>
          <div class="bg-gray-100 dark:bg-gray-800 p-3 rounded text-left text-xs font-mono space-y-1">
            <div>지연시간: <span class="font-bold text-blue-600">{{ diagResult.latencyMs }}ms</span></div>
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

const diagResult = ref<ConnectionTestResponse | null>(null);

const [registerModal] = useModalInner((data) => {
  diagResult.value = data?.diagResult || null;
});
</script>
