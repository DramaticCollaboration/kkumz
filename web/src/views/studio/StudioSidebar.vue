<template>
  <aside class="studio-sidebar">
    <div class="sidebar-top-stack">
      <div class="sidebar-brand" v-if="currentIdea">
        <div v-if="previewLogoSvg" class="mini-logo" v-html="previewLogoSvg"></div>
        <div class="mini-meta">
          <strong>{{ previewBrandName }}</strong>
          <span>{{ formatSiteTypeLabel(siteType || currentIdea.siteType) }}</span>
        </div>
      </div>

      <div class="sidebar-mode-badge-box">
        <span class="sidebar-mode-pill" :class="{ pro: !easyMode }">
          {{ easyMode ? '초보자 맞춤 3단계 이지 모드' : '전체 6단계 전문가 모드' }}
        </span>
      </div>

      <nav class="vertical-stepper">
        <button
          v-for="step in displayedSteps"
          :key="step.no"
          type="button"
          class="step-nav-btn"
          :data-testid="`step-nav-btn-${step.no}`"
          :disabled="busy"
          :class="{
            active: isStepNavActive(step.no),
            done: (currentIdea?.currentStep || 1) > step.no,
            'is-disabled': busy,
          }"
          @click="emit('go-step', step.no)"
        >
          <span class="step-index">{{ step.displayNo }}</span>
          <div class="step-labels">
            <div class="step-title-row">
              <strong>{{ step.title }}</strong>
              <span v-if="step.optional" class="opt-badge">선택</span>
            </div>
            <small>{{ step.desc }}</small>
          </div>
        </button>
      </nav>
    </div>

    <div class="sidebar-footer">
      <button
        type="button"
        class="sidebar-mode-toggle-btn"
        @click="emit('set-mode', !easyMode)"
      >
        {{ easyMode ? '기획서 · 코드 · 고급 배포 보기' : '3단계 이지 모드로 돌아가기' }}
      </button>
      <p class="sidebar-tip">
        모든 변경 내역은 자동 저장되며 언제든 이전 상태로 되돌릴 수 있습니다.
      </p>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { formatSiteTypeLabel, type StudioStepMeta } from './studioFormatters';

const props = defineProps<{
  currentIdea: any | null;
  previewLogoSvg: string;
  previewBrandName: string;
  siteType: string;
  easyMode: boolean;
  displayedSteps: StudioStepMeta[];
  activeStep: number;
  busy: boolean;
}>();

const emit = defineEmits<{
  (e: 'go-step', stepNo: number): void;
  (e: 'set-mode', isEasy: boolean): void;
}>();

function isStepNavActive(stepNo: number): boolean {
  if (!props.easyMode) return props.activeStep === stepNo;
  if (
    stepNo === 4 &&
    (props.activeStep === 3 || props.activeStep === 4 || props.activeStep === 5 || props.activeStep === 6)
  ) {
    return true;
  }
  return props.activeStep === stepNo;
}
</script>

<style scoped src="./studio-view.css"></style>
