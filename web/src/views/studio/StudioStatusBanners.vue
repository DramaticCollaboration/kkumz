<template>
  <div
    v-if="toastMessage"
    class="studio-toast"
    :class="`toast-${toastType}`"
    role="status"
    aria-live="polite"
  >
    <span class="toast-icon">
      {{ toastType === 'error' ? '!' : toastType === 'success' ? '✓' : 'i' }}
    </span>
    <span class="toast-text">{{ toastMessage }}</span>
    <button type="button" class="toast-close" @click="emit('clear-toast')">✕</button>
  </div>

  <div v-if="busy" class="studio-progress-banner" role="status" aria-live="polite">
    <div class="progress-banner-main">
      <div class="progress-banner-left">
        <span class="progress-spinner"></span>
        <div class="progress-texts">
          <strong>{{ busyMessage || '요청하신 작업을 처리하고 있습니다...' }}</strong>
          <span>AI 생성 및 저장 작업 중에는 데이터 보호를 위해 화면 편집과 단계 이동이 일시 잠금됩니다.</span>
        </div>
      </div>
      <span class="busy-lock-pill">편집 잠금 중</span>
    </div>
    <div v-if="busySteps.length > 0" class="progress-step-pills">
      <div
        v-for="(bStep, bIdx) in busySteps"
        :key="bIdx"
        class="progress-step-pill"
        :class="`step-${bStep.status}`"
      >
        <span class="p-step-icon">
          {{ bStep.status === 'done' ? '✓' : bStep.status === 'active' ? '▶' : '•' }}
        </span>
        <span>{{ bStep.label }}</span>
      </div>
    </div>
  </div>

  <div v-if="errorBanner" class="studio-error-banner" role="alert">
    <div class="error-banner-left">
      <span class="error-badge-icon">!</span>
      <div class="error-banner-body">
        <div class="error-title-row">
          <strong>{{ errorBanner.title }}</strong>
          <code v-if="errorBanner.endpoint" class="error-endpoint-pill">{{ errorBanner.endpoint }}</code>
        </div>
        <p>{{ errorBanner.detail }}</p>
      </div>
    </div>
    <div class="error-banner-actions">
      <button
        v-if="errorBanner.retryAction"
        type="button"
        class="error-retry-btn"
        :disabled="busy"
        @click="emit('retry-error')"
      >
        다시 시도
      </button>
      <button
        v-if="errorBanner.canMoveToStep2 && activeStep === 1"
        type="button"
        class="error-step-btn"
        :disabled="busy"
        @click="emit('force-step2')"
      >
        2단계 화면으로 이동 →
      </button>
      <button type="button" class="error-dismiss-btn" @click="emit('dismiss-error')">
        닫기 ✕
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  toastMessage: string;
  toastType: 'info' | 'success' | 'error';
  busy: boolean;
  busyMessage: string;
  busySteps: Array<{ label: string; status: 'done' | 'active' | 'pending' }>;
  errorBanner: {
    title: string;
    detail: string;
    endpoint?: string;
    retryAction?: (() => void) | null;
    canMoveToStep2?: boolean;
  } | null;
  activeStep: number;
}>();

const emit = defineEmits<{
  (e: 'clear-toast'): void;
  (e: 'retry-error'): void;
  (e: 'force-step2'): void;
  (e: 'dismiss-error'): void;
}>();
</script>

<style scoped src="./studio-view.css"></style>
