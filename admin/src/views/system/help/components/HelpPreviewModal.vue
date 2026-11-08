<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="`도움말 미리보기: ${record?.title || ''}`"
    width="940px"
    :showOkBtn="false"
    cancelText="닫기"
  >
    <div v-if="record" class="help-preview-container">
      <div class="preview-header">
        <div class="mb-2 flex items-center gap-2 flex-wrap">
          <a-tag color="default">{{ record.appCode }}</a-tag>
          <a-tag color="blue">{{ record.category || '일반' }}</a-tag>
          <code class="px-2 py-0.5 bg-gray-100 rounded text-xs font-mono text-blue-600">{{ record.pageKey }}</code>
          <a-tag :color="record.status === 'ACTIVE' ? 'success' : 'default'">
            {{ record.status === 'ACTIVE' ? '활성' : '비활성' }}
          </a-tag>
        </div>
        <h3 class="text-lg font-bold text-gray-900 mb-1">{{ record.title }}</h3>
        <p class="text-sm text-gray-600 mb-2">{{ record.summary }}</p>
        <div v-if="record.overview" class="p-3 bg-gray-50 border border-gray-200 rounded text-xs text-gray-700 leading-relaxed whitespace-pre-wrap">
          {{ record.overview }}
        </div>
      </div>

      <!-- 실무 운영 팁 -->
      <a-alert v-if="parsed.tips.length > 0" type="info" show-icon class="mb-4">
        <template #message><span class="font-semibold">실무 운영 팁</span></template>
        <template #description>
          <ul class="list-disc pl-5 m-0 space-y-1">
            <li v-for="(tip, idx) in parsed.tips" :key="idx">{{ tip }}</li>
          </ul>
        </template>
      </a-alert>

      <!-- 주요 제공 기능 & 업무 수행 절차 -->
      <a-row :gutter="16">
        <a-col :xs="24" :md="12" v-if="parsed.features.length > 0">
          <div class="section-block">
            <h4 class="section-title">주요 제공 기능</h4>
            <div class="space-y-2">
              <div v-for="(feat, idx) in parsed.features" :key="idx" class="item-card">
                <div class="flex justify-between items-center mb-1">
                  <span class="font-bold text-xs text-gray-900">{{ idx + 1 }}. {{ feat.name }}</span>
                  <a-tag v-if="feat.badge" color="default" class="m-0">{{ feat.badge }}</a-tag>
                </div>
                <div class="text-xs text-gray-600">{{ feat.description }}</div>
              </div>
            </div>
          </div>
        </a-col>

        <a-col :xs="24" :md="12" v-if="parsed.steps.length > 0">
          <div class="section-block">
            <h4 class="section-title">표준 업무 수행 절차</h4>
            <div class="space-y-2">
              <div v-for="(step, idx) in parsed.steps" :key="idx" class="item-card">
                <div class="font-bold text-xs text-gray-900 mb-1">{{ step.title }}</div>
                <div class="text-xs text-gray-600">{{ step.description }}</div>
              </div>
            </div>
          </div>
        </a-col>
      </a-row>

      <!-- 데이터 흐름 계보 -->
      <div
        v-if="parsed.lineage.sources.length > 0 || parsed.lineage.destinations.length > 0"
        class="section-block"
      >
        <h4 class="section-title">데이터 계보 및 연계 흐름</h4>
        <a-row :gutter="16">
          <a-col :xs="24" :md="12">
            <div class="p-3 bg-gray-50 border border-gray-200 rounded">
              <div class="text-xs font-bold text-blue-700 mb-2">데이터 입력 원천</div>
              <div v-for="(src, idx) in parsed.lineage.sources" :key="idx" class="mb-2 last:mb-0">
                <div class="text-xs font-semibold text-gray-900">
                  {{ src.origin }}
                  <span v-if="src.cycle" class="text-gray-500 font-normal">({{ src.cycle }})</span>
                </div>
                <div class="text-xs text-gray-600">{{ src.description }}</div>
              </div>
            </div>
          </a-col>
          <a-col :xs="24" :md="12">
            <div class="p-3 bg-gray-50 border border-gray-200 rounded">
              <div class="text-xs font-bold text-slate-700 mb-2">후속 반영 및 연계 대상</div>
              <div v-for="(dst, idx) in parsed.lineage.destinations" :key="idx" class="mb-2 last:mb-0">
                <div class="text-xs font-semibold text-gray-900">
                  {{ dst.target }}
                  <span v-if="dst.timing" class="text-gray-500 font-normal">({{ dst.timing }})</span>
                </div>
                <div class="text-xs text-gray-600">{{ dst.description }}</div>
              </div>
            </div>
          </a-col>
        </a-row>
      </div>

      <!-- 산출 공식 -->
      <div v-if="parsed.formulas.length > 0" class="section-block">
        <h4 class="section-title">핵심 지표 산출 공식</h4>
        <div class="space-y-2">
          <div v-for="(fm, idx) in parsed.formulas" :key="idx" class="item-card">
            <div class="font-bold text-xs text-gray-900">{{ fm.name }}</div>
            <code class="block my-1 px-2 py-1 bg-white border border-gray-200 rounded text-xs font-mono text-slate-700">
              {{ fm.formula }}
            </code>
            <div class="text-xs text-gray-600">{{ fm.description }}</div>
          </div>
        </div>
      </div>

      <!-- 주의사항 -->
      <a-alert v-if="parsed.warnings.length > 0" type="warning" show-icon class="mb-4">
        <template #message><span class="font-semibold">업무 처리 시 주의사항</span></template>
        <template #description>
          <ul class="list-disc pl-5 m-0 space-y-1">
            <li v-for="(warn, idx) in parsed.warnings" :key="idx">{{ warn }}</li>
          </ul>
        </template>
      </a-alert>

      <!-- FAQ -->
      <div v-if="parsed.faq.length > 0" class="section-block">
        <h4 class="section-title">자주 묻는 질문</h4>
        <div class="space-y-2">
          <div v-for="(item, idx) in parsed.faq" :key="idx" class="item-card">
            <div class="font-bold text-xs text-gray-900">Q. {{ item.q }}</div>
            <div class="text-xs text-gray-600 mt-1">A. {{ item.a }}</div>
          </div>
        </div>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { SysPageHelpModel } from '../help.api';
  import { parseHelpRecord } from '../help.data';

  const record = ref<SysPageHelpModel | null>(null);

  const [registerModal] = useModalInner((data) => {
    record.value = data?.record || null;
  });

  const parsed = computed(() => parseHelpRecord(record.value));
</script>

<style lang="less" scoped>
  .help-preview-container {
    padding: 4px 8px;
  }

  .preview-header {
    border-bottom: 1px solid #e5e7eb;
    padding-bottom: 14px;
    margin-bottom: 16px;
  }

  .section-block {
    margin-bottom: 16px;

    .section-title {
      font-size: 14px;
      font-weight: 700;
      color: #1f2937;
      margin-bottom: 8px;
      padding-left: 8px;
      border-left: 3px solid #2563eb;
    }
  }

  .item-card {
    padding: 10px 12px;
    border: 1px solid #e5e7eb;
    border-radius: 6px;
    background: #fafafa;
  }
</style>
