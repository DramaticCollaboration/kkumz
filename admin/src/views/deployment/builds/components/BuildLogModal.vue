<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="WebIDE 빌드 파이프라인 상세 콘솔 로그"
    :width="760"
    :showOkBtn="false"
    cancelText="닫기"
  >
    <div v-if="record" class="space-y-3">
      <div class="flex flex-wrap items-center justify-between gap-2 p-3 rounded-lg bg-slate-50 border border-slate-200">
        <div class="flex items-center gap-2">
          <Tag :color="record.status === 'SUCCESS' ? 'success' : 'error'">{{ record.status }}</Tag>
          <span class="font-bold text-sm">{{ record.brandName || record.siteSlug }}</span>
          <span class="text-xs text-slate-500">v{{ record.versionNo }} · {{ record.triggerType }}</span>
        </div>
        <span class="text-xs font-mono text-slate-500">소요 시간: {{ record.durationMs }}ms</span>
      </div>

      <div v-if="record.errorSummary" class="p-3 rounded-lg bg-rose-50 border border-rose-200 text-xs text-rose-800 font-bold">
        오류 요약: {{ record.errorSummary }}
      </div>

      <div v-if="record.aiFixSuggestion" class="p-3 rounded-lg bg-indigo-50 border border-indigo-200 text-xs text-indigo-800">
        <strong>AI 자동 패치 내역:</strong> {{ record.aiFixSuggestion }}
      </div>

      <div>
        <div class="text-xs font-bold text-slate-700 mb-1">표준 출력 / 컴파일러 콘솔 로그</div>
        <pre class="p-4 rounded-xl bg-slate-950 text-emerald-300 text-xs font-mono whitespace-pre-wrap max-h-80 overflow-y-auto">{{ record.logOutput }}</pre>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { Tag } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';

  const record = ref<Recordable | null>(null);

  const [registerModal] = useModalInner((data) => {
    record.value = data?.record || null;
  });
</script>
