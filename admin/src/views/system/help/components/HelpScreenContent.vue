<template>
  <div class="screen-guide-content">
    <div class="content-header">
      <div class="content-header-tags">
        <a-tag color="default">{{ guide.appCode || defaultAppCode }}</a-tag>
        <a-tag color="blue">{{ guide.category || t('appUi.kaef1a1e7') }}</a-tag>
        <code class="px-2 py-0.5 bg-gray-100 rounded text-xs font-mono text-blue-600">
          {{ guide.pageKey }}
        </code>
      </div>
      <div class="content-title">{{ guide.title }}</div>
      <p class="content-summary">{{ guide.summary }}</p>
      <div v-if="guide.overview" class="content-overview">
        {{ guide.overview }}
      </div>
    </div>

    <!-- AI 에이전트 추천 프롬프트 -->
    <div v-if="guide.aiPromptHint" class="ai-prompt-box">
      <div class="ai-prompt-header">
        <span class="ai-prompt-title">{{ t('appUi.k55a7da3f') }}</span>
        <a-button v-if="interactive" size="small" type="link" @click="emit('copy-prompt', guide.aiPromptHint)">
          {{ t('appUi.k04399087') }}
        </a-button>
      </div>
      <div class="ai-prompt-body">{{ guide.aiPromptHint }}</div>
    </div>

    <!-- 실제 화면 캡처 (CRUD 화면에 한해 콜아웃 범례 노출) -->
    <div v-if="screenshotUrl && !imageLoadError" class="section-block">
      <h4 class="section-heading">
        {{ showCalloutLegend ? t('appUi.k795967b4') : t('appUi.kd410b17d') }}
      </h4>
      <div class="screenshot-preview-card">
        <a-image
          :src="screenshotUrl"
          :alt="guide.title"
          class="screenshot-img"
          @error="imageLoadError = true"
        />
        <div v-if="showCalloutLegend" class="callout-legend">
          <span class="callout-badge"><b>1</b> {{ t('appUi.kdd726cdb') }}</span>
          <span class="callout-badge"><b>2</b> {{ t('appUi.k23317319') }}</span>
          <span class="callout-badge"><b>3</b> {{ t('appUi.k0df03aea') }}</span>
          <span class="callout-badge"><b>4</b> {{ t('appUi.k8fd75636') }}</span>
        </div>
      </div>
    </div>

    <!-- 실무 운영 팁 -->
    <a-alert v-if="parsed.tips.length > 0" type="info" show-icon class="mb-4">
      <template #message>
        <span class="font-semibold">{{ t('appUi.k44876234') }}</span>
      </template>
      <template #description>
        <ul class="list-disc pl-5 m-0 space-y-1">
          <li v-for="(tip, idx) in parsed.tips" :key="idx">{{ tip }}</li>
        </ul>
      </template>
    </a-alert>

    <!-- 주요 제공 기능 및 표준 업무 수행 절차 -->
    <a-row :gutter="16">
      <a-col :xs="24" :md="12" v-if="parsed.features.length > 0">
        <div class="section-block">
          <h4 class="section-heading">{{ t('appUi.k5c1f3414') }}</h4>
          <div class="card-stack">
            <div v-for="(feat, idx) in parsed.features" :key="idx" class="info-card">
              <div class="flex justify-between items-center mb-1">
                <span class="info-card-title">{{ idx + 1 }}. {{ feat.name }}</span>
                <a-tag v-if="feat.badge" color="default" class="m-0">{{ feat.badge }}</a-tag>
              </div>
              <div class="info-card-desc">{{ feat.description }}</div>
            </div>
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :md="12" v-if="parsed.steps.length > 0">
        <div class="section-block">
          <h4 class="section-heading">{{ t('appUi.kb18ae88d') }}</h4>
          <div class="card-stack">
            <div v-for="(step, idx) in parsed.steps" :key="idx" class="info-card">
              <div class="info-card-title">{{ step.title }}</div>
              <div class="info-card-desc">{{ step.description }}</div>
            </div>
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 연관 화면 및 후속 업무 이동 -->
    <div v-if="parsed.relatedScreens.length > 0" class="section-block">
      <h4 class="section-heading">
        {{ interactive ? t('appUi.k8d2ae103') : t('appUi.kd4156ba4') }}
      </h4>
      <a-row :gutter="12">
        <a-col v-for="(rel, idx) in parsed.relatedScreens" :key="idx" :xs="24" :md="12" class="mb-2">
          <div
            class="related-screen-card"
            :class="{ 'is-clickable': interactive && !!extractRelatedScreenPath(rel) }"
            @click="interactive && emit('navigate-related', rel)"
          >
            <div class="flex justify-between items-center mb-1">
              <a-tag color="default" class="m-0 text-xs">{{ rel.type || t('appUi.k6c837656') }}</a-tag>
              <span v-if="interactive && extractRelatedScreenPath(rel)" class="text-xs text-blue-600 font-semibold">
                {{ t('appUi.kf24a6b0d') }}
              </span>
            </div>
            <div class="info-card-title">{{ rel.title }}</div>
            <div class="info-card-desc">{{ rel.description }}</div>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 데이터 계보 -->
    <div v-if="parsed.lineage.sources.length > 0 || parsed.lineage.destinations.length > 0" class="section-block">
      <h4 class="section-heading">{{ t('appUi.kbc021060') }}</h4>
      <a-row :gutter="12">
        <a-col :xs="24" :md="12" class="mb-2">
          <div class="lineage-box">
            <div class="lineage-label">{{ t('appUi.k6c418325') }}</div>
            <div v-for="(src, idx) in parsed.lineage.sources" :key="idx" class="mb-2 last:mb-0">
              <div class="lineage-val">
                {{ src.origin }}
                <span v-if="src.cycle" class="text-gray-500 font-normal">[{{ src.cycle }}]</span>
              </div>
              <div class="text-xs text-gray-600">{{ src.description }}</div>
            </div>
          </div>
        </a-col>
        <a-col :xs="24" :md="12" class="mb-2">
          <div class="lineage-box active">
            <div class="lineage-label">{{ t('appUi.k665b44e3') }}</div>
            <div v-for="(dst, idx) in parsed.lineage.destinations" :key="idx" class="mb-2 last:mb-0">
              <div class="lineage-val">
                {{ dst.target }}
                <span v-if="dst.timing" class="text-gray-500 font-normal">[{{ dst.timing }}]</span>
              </div>
              <div class="text-xs text-gray-600">{{ dst.description }}</div>
            </div>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 핵심 지표 산출 공식 -->
    <div v-if="parsed.formulas.length > 0" class="section-block">
      <h4 class="section-heading">{{ t('appUi.k99b9eaa8') }}</h4>
      <div class="card-stack">
        <div v-for="(fm, idx) in parsed.formulas" :key="idx" class="info-card">
          <div class="info-card-title">{{ fm.name }}</div>
          <code class="block my-1 px-2 py-1 bg-white border border-gray-200 rounded text-xs font-mono text-slate-700">
            {{ fm.formula }}
          </code>
          <div class="info-card-desc">{{ fm.description }}</div>
        </div>
      </div>
    </div>

    <!-- 주의사항 -->
    <a-alert v-if="parsed.warnings.length > 0" type="warning" show-icon class="mb-4">
      <template #message>
        <span class="font-semibold">{{ t('appUi.k07ece1cf') }}</span>
      </template>
      <template #description>
        <ul class="list-disc pl-5 m-0 space-y-1">
          <li v-for="(warn, idx) in parsed.warnings" :key="idx">{{ warn }}</li>
        </ul>
      </template>
    </a-alert>

    <!-- FAQ -->
    <div v-if="parsed.faq.length > 0" class="section-block">
      <h4 class="section-heading">{{ t('appUi.kae2ce921') }}</h4>
      <div class="card-stack">
        <div v-for="(item, idx) in parsed.faq" :key="idx" class="info-card">
          <div class="info-card-title">Q. {{ item.q }}</div>
          <div class="info-card-desc mt-1">A. {{ item.a }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';
  import { Image as AImage } from 'ant-design-vue';
  import { RelatedScreenItem, SysPageHelpModel } from '../help.api';
  import {
    extractRelatedScreenPath,
    parseHelpRecord,
    resolveGuideScreenshotUrl,
    supportsCalloutLegend,
  } from '../help.data';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      guide: SysPageHelpModel;
      defaultAppCode: string;
      interactive?: boolean;
    }>(),
    {
      interactive: true,
    },
  );

  const emit = defineEmits<{
    (e: 'navigate-related', item: RelatedScreenItem): void;
    (e: 'copy-prompt', text: string): void;
  }>();

  const imageLoadError = ref(false);
  const parsed = computed(() => parseHelpRecord(props.guide));
  const showCalloutLegend = computed(() => supportsCalloutLegend(props.guide));
  const screenshotUrl = computed(() =>
    resolveGuideScreenshotUrl(
      props.guide.appCode || props.defaultAppCode,
      props.guide.pageKey,
      props.guide.helpType,
    ),
  );

  watch(
    () => props.guide.pageKey,
    () => {
      imageLoadError.value = false;
    },
  );
</script>

<style lang="less" scoped>
  .content-header {
    border-bottom: 1px solid #f1f5f9;
    padding-bottom: 14px;
    margin-bottom: 16px;

    .content-header-tags {
      display: flex;
      align-items: center;
      gap: 6px;
      flex-wrap: wrap;
      margin-bottom: 6px;
    }

    .content-title {
      font-size: 18px;
      font-weight: 700;
      color: #0f172a;
      margin: 0 0 6px 0;
    }

    .content-summary {
      font-size: 13px;
      color: #475569;
      margin: 0;
      line-height: 1.6;
    }

    .content-overview {
      margin-top: 10px;
      padding: 10px 12px;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      font-size: 12px;
      color: #334155;
      line-height: 1.6;
      white-space: pre-wrap;
    }
  }

  .ai-prompt-box {
    margin-bottom: 16px;
    padding: 10px 14px;
    border-radius: 8px;
    border: 1px solid #cbd5e1;
    background: #f8fafc;

    .ai-prompt-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;
    }

    .ai-prompt-title {
      font-size: 12px;
      font-weight: 700;
      color: #334155;
    }

    .ai-prompt-body {
      font-size: 12px;
      color: #1e293b;
      background: #ffffff;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      padding: 8px 10px;
    }
  }

  .screenshot-preview-card {
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 10px;
    background: #f8fafc;

    .screenshot-img {
      width: 100%;
      max-height: 280px;
      object-fit: cover;
      border-radius: 6px;
      border: 1px solid #cbd5e1;
    }

    .callout-legend {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-top: 8px;
    }

    .callout-badge {
      font-size: 11px;
      color: #1e293b;
      background: #ffffff;
      border: 1px solid #cbd5e1;
      border-radius: 4px;
      padding: 3px 8px;

      b {
        display: inline-block;
        width: 16px;
        height: 16px;
        line-height: 16px;
        text-align: center;
        border-radius: 50%;
        background: #2563eb;
        color: #ffffff;
        margin-right: 4px;
        font-size: 10px;
      }
    }
  }

  .related-screen-card {
    padding: 10px 12px;
    border: 1px solid #dbeafe;
    border-radius: 6px;
    background: #f8fafc;
    transition: all 0.15s;
    height: 100%;

    &.is-clickable {
      cursor: pointer;

      &:hover {
        background: #eff6ff;
        border-color: #60a5fa;
      }
    }
  }

  .section-block {
    margin-bottom: 16px;

    .section-heading {
      font-size: 14px;
      font-weight: 700;
      color: #1e293b;
      margin-bottom: 8px;
      padding-left: 8px;
      border-left: 3px solid #2563eb;
    }
  }

  .lineage-box {
    padding: 10px 12px;
    border-radius: 6px;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    height: 100%;

    &.active {
      background: #eff6ff;
      border-color: #93c5fd;
    }

    .lineage-label {
      font-size: 11px;
      font-weight: 700;
      color: #64748b;
      margin-bottom: 6px;
    }

    .lineage-val {
      font-size: 12px;
      font-weight: 600;
      color: #0f172a;
    }
  }

  .card-stack {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .info-card {
    padding: 10px 12px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    background: #fafafa;

    .info-card-title {
      font-size: 13px;
      font-weight: 700;
      color: #0f172a;
    }

    .info-card-desc {
      font-size: 12px;
      color: #475569;
      line-height: 1.5;
    }
  }
</style>
