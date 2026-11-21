<template>
  <div class="system-guide-content">
    <!-- 시스템 정체성 히어로 -->
    <div class="sys-hero">
      <div class="sys-hero-tags">
        <a-tag color="default">{{ guide.appCode || defaultAppCode }}</a-tag>
        <a-tag color="cyan">{{ t('appUi.k368c6bad') }}</a-tag>
        <a-tag v-if="guide.category && guide.category !== t('appUi.k368c6bad')" color="blue">
          {{ guide.category }}
        </a-tag>
      </div>
      <div class="sys-hero-title">{{ guide.title }}</div>
      <p class="sys-hero-summary">{{ guide.summary }}</p>
      <div v-if="guide.overview" class="sys-hero-overview">
        {{ guide.overview }}
      </div>
    </div>

    <!-- 전체 업무 파이프라인 (가로 플로우) -->
    <section v-if="parsed.steps.length > 0" class="sys-section">
      <h4 class="sys-section-heading">{{ t('appUi.k3d39ef19') }}</h4>
      <div class="workflow-grid">
        <div
          v-for="(step, idx) in parsed.steps"
          :key="idx"
          class="workflow-card"
          :class="{ 'is-clickable': interactive && !!step.path }"
          @click="handleStepClick(step.path)"
        >
          <div class="workflow-card-top">
            <span class="workflow-step-num">{{ String(idx + 1).padStart(2, '0') }}</span>
            <span v-if="interactive && step.path" class="workflow-jump">{{ t('appUi.k81b07839') }}</span>
          </div>
          <div class="workflow-card-title">{{ step.title }}</div>
          <div class="workflow-card-desc">{{ step.description }}</div>
        </div>
      </div>
    </section>

    <!-- 핵심 제공 역량 -->
    <section v-if="parsed.features.length > 0" class="sys-section">
      <h4 class="sys-section-heading">{{ t('appUi.kf64aaffd') }}</h4>
      <a-row :gutter="12">
        <a-col
          v-for="(feat, idx) in parsed.features"
          :key="idx"
          :xs="24"
          :md="12"
          class="mb-3"
        >
          <div class="capability-card">
            <div class="flex justify-between items-center gap-2 mb-1">
              <span class="capability-title">{{ feat.name }}</span>
              <a-tag v-if="feat.badge" color="blue" class="m-0 text-xs">{{ feat.badge }}</a-tag>
            </div>
            <div class="capability-desc">{{ feat.description }}</div>
          </div>
        </a-col>
      </a-row>
    </section>

    <!-- SyncSeries 생태계 연동 경계 -->
    <section
      v-if="parsed.lineage.sources.length > 0 || parsed.lineage.destinations.length > 0"
      class="sys-section"
    >
      <h4 class="sys-section-heading">{{ t('appUi.k5bb8d2f3') }}</h4>
      <a-row :gutter="12">
        <a-col :xs="24" :md="12" class="mb-2">
          <div class="boundary-box">
            <div class="boundary-label">{{ t('appUi.kd58536eb') }}</div>
            <div v-for="(src, idx) in parsed.lineage.sources" :key="idx" class="boundary-entry">
              <div class="boundary-name">
                {{ src.origin }}
                <span v-if="src.cycle" class="boundary-meta">[{{ src.cycle }}]</span>
              </div>
              <div class="boundary-desc">{{ src.description }}</div>
            </div>
          </div>
        </a-col>
        <a-col :xs="24" :md="12" class="mb-2">
          <div class="boundary-box boundary-box--dest">
            <div class="boundary-label">{{ t('appUi.k48d522ce') }}</div>
            <div v-for="(dst, idx) in parsed.lineage.destinations" :key="idx" class="boundary-entry">
              <div class="boundary-name">
                {{ dst.target }}
                <span v-if="dst.timing" class="boundary-meta">[{{ dst.timing }}]</span>
              </div>
              <div class="boundary-desc">{{ dst.description }}</div>
            </div>
          </div>
        </a-col>
      </a-row>
    </section>

    <!-- 역할별 주요 시작 화면 -->
    <section v-if="parsed.relatedScreens.length > 0" class="sys-section">
      <h4 class="sys-section-heading">{{ t('appUi.kbec9633e') }}</h4>
      <a-row :gutter="12">
        <a-col
          v-for="(rel, idx) in parsed.relatedScreens"
          :key="idx"
          :xs="24"
          :md="12"
          class="mb-2"
        >
          <div
            class="role-card"
            :class="{ 'is-clickable': interactive && !!extractRelatedScreenPath(rel) }"
            @click="handleRelatedClick(rel)"
          >
            <div class="flex justify-between items-center mb-1">
              <a-tag color="geekblue" class="m-0 text-xs">{{ rel.type || t('appUi.ka1037c16') }}</a-tag>
              <span v-if="interactive && extractRelatedScreenPath(rel)" class="role-jump">
                {{ t('appUi.kf24a6b0d') }}
              </span>
            </div>
            <div class="capability-title">{{ rel.title }}</div>
            <div class="capability-desc">{{ rel.description }}</div>
          </div>
        </a-col>
      </a-row>
    </section>

    <!-- 운영 원칙 -->
    <a-alert v-if="parsed.tips.length > 0" type="info" show-icon class="mb-4">
      <template #message>
        <span class="font-semibold">{{ t('appUi.k610a0246') }}</span>
      </template>
      <template #description>
        <ul class="list-disc pl-5 m-0 space-y-1">
          <li v-for="(tip, idx) in parsed.tips" :key="idx">{{ tip }}</li>
        </ul>
      </template>
    </a-alert>

    <!-- 책임 범위 및 유의사항 -->
    <a-alert v-if="parsed.warnings.length > 0" type="warning" show-icon class="mb-4">
      <template #message>
        <span class="font-semibold">{{ t('appUi.k28feece3') }}</span>
      </template>
      <template #description>
        <ul class="list-disc pl-5 m-0 space-y-1">
          <li v-for="(warn, idx) in parsed.warnings" :key="idx">{{ warn }}</li>
        </ul>
      </template>
    </a-alert>

    <!-- 자주 묻는 질문 -->
    <section v-if="parsed.faq.length > 0" class="sys-section">
      <h4 class="sys-section-heading">{{ t('appUi.kae2ce921') }}</h4>
      <div class="faq-stack">
        <div v-for="(item, idx) in parsed.faq" :key="idx" class="faq-card">
          <div class="faq-q">Q. {{ item.q }}</div>
          <div class="faq-a">A. {{ item.a }}</div>
        </div>
      </div>
    </section>

    <!-- AI 추천 프롬프트 -->
    <div v-if="guide.aiPromptHint" class="ai-prompt-box">
      <div class="ai-prompt-header">
        <span class="ai-prompt-title">{{ t('appUi.kf86b7280') }}</span>
        <a-button v-if="interactive" size="small" type="link" @click="emit('copy-prompt', guide.aiPromptHint)">
          {{ t('appUi.k04399087') }}
        </a-button>
      </div>
      <div class="ai-prompt-body">{{ guide.aiPromptHint }}</div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { RelatedScreenItem, SysPageHelpModel } from '../help.api';
  import { extractRelatedScreenPath, parseHelpRecord } from '../help.data';
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
    (e: 'navigate-path', path: string): void;
    (e: 'navigate-related', item: RelatedScreenItem): void;
    (e: 'copy-prompt', text: string): void;
  }>();

  const parsed = computed(() => parseHelpRecord(props.guide));

  function handleStepClick(path?: string) {
    if (!props.interactive || !path) return;
    emit('navigate-path', path);
  }

  function handleRelatedClick(rel: RelatedScreenItem) {
    if (!props.interactive) return;
    emit('navigate-related', rel);
  }
</script>

<style lang="less" scoped>
  .sys-hero {
    border-bottom: 1px solid #e2e8f0;
    padding-bottom: 16px;
    margin-bottom: 18px;

    .sys-hero-tags {
      display: flex;
      align-items: center;
      gap: 6px;
      flex-wrap: wrap;
      margin-bottom: 8px;
    }

    .sys-hero-title {
      font-size: 20px;
      font-weight: 700;
      color: #0f172a;
      margin: 0 0 6px 0;
    }

    .sys-hero-summary {
      font-size: 13.5px;
      color: #334155;
      margin: 0;
      line-height: 1.6;
    }

    .sys-hero-overview {
      margin-top: 12px;
      padding: 12px 14px;
      background: linear-gradient(135deg, #f0fdfa 0%, #f8fafc 100%);
      border: 1px solid #cbd5e1;
      border-left: 3px solid #0284c7;
      border-radius: 6px;
      font-size: 12.5px;
      color: #1e293b;
      line-height: 1.65;
      white-space: pre-wrap;
    }
  }

  .sys-section {
    margin-bottom: 18px;

    .sys-section-heading {
      font-size: 14px;
      font-weight: 700;
      color: #0f172a;
      margin-bottom: 10px;
      padding-left: 8px;
      border-left: 3px solid #0284c7;
    }
  }

  .workflow-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(175px, 1fr));
    gap: 10px;
  }

  .workflow-card {
    padding: 12px;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
    background: #f8fafc;
    display: flex;
    flex-direction: column;
    gap: 6px;
    transition: all 0.15s ease;

    &.is-clickable {
      cursor: pointer;

      &:hover {
        background: #f0f9ff;
        border-color: #38bdf8;
      }
    }

    .workflow-card-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .workflow-step-num {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: 1px 8px;
      border-radius: 999px;
      background: #0284c7;
      color: #ffffff;
      font-size: 11px;
      font-weight: 700;
    }

    .workflow-jump {
      font-size: 11px;
      font-weight: 600;
      color: #0284c7;
    }

    .workflow-card-title {
      font-size: 13px;
      font-weight: 700;
      color: #0f172a;
    }

    .workflow-card-desc {
      font-size: 12px;
      color: #475569;
      line-height: 1.5;
    }
  }

  .capability-card {
    padding: 11px 13px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #ffffff;
    height: 100%;
  }

  .capability-title {
    font-size: 13px;
    font-weight: 700;
    color: #0f172a;
  }

  .capability-desc {
    font-size: 12px;
    color: #475569;
    line-height: 1.55;
    margin-top: 2px;
  }

  .boundary-box {
    padding: 12px 14px;
    border-radius: 8px;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    height: 100%;

    &.boundary-box--dest {
      background: #eff6ff;
      border-color: #bfdbfe;
    }

    .boundary-label {
      font-size: 11.5px;
      font-weight: 700;
      color: #334155;
      margin-bottom: 8px;
    }

    .boundary-entry {
      padding: 6px 0;
      border-bottom: 1px dashed #cbd5e1;

      &:last-child {
        border-bottom: none;
        padding-bottom: 0;
      }
    }

    .boundary-name {
      font-size: 12.5px;
      font-weight: 600;
      color: #0f172a;
    }

    .boundary-meta {
      font-size: 11px;
      font-weight: 400;
      color: #64748b;
      margin-left: 4px;
    }

    .boundary-desc {
      font-size: 12px;
      color: #475569;
      margin-top: 2px;
    }
  }

  .role-card {
    padding: 11px 13px;
    border: 1px solid #dbeafe;
    border-radius: 8px;
    background: #f8fafc;
    height: 100%;
    transition: all 0.15s ease;

    &.is-clickable {
      cursor: pointer;

      &:hover {
        background: #eff6ff;
        border-color: #60a5fa;
      }
    }

    .role-jump {
      font-size: 11px;
      color: #2563eb;
      font-weight: 600;
    }
  }

  .faq-stack {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .faq-card {
    padding: 10px 13px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    background: #fafafa;

    .faq-q {
      font-size: 13px;
      font-weight: 700;
      color: #0f172a;
    }

    .faq-a {
      font-size: 12px;
      color: #475569;
      margin-top: 4px;
      line-height: 1.55;
    }
  }

  .ai-prompt-box {
    margin-top: 8px;
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
</style>
