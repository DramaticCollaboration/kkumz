<template>
  <div class="cli-window">
    <div class="cli-bar">
      <div class="cli-dot" style="background: #ef4444"></div>
      <div class="cli-dot" style="background: #f59e0b"></div>
      <div class="cli-dot" style="background: #22c55e"></div>
      <span class="cli-title">{{ simulation.roleTag }} — {{ simulation.roleSummary }}</span>
      <button
        type="button"
        class="copy-chip"
        @click="emit('copyText', simulation.command, 'CLI 명령어')"
      >
        명령어 복사
      </button>
    </div>
    <div class="cli-body">
      <div class="cli-line-cmd">
        <span class="cli-prompt">$</span>
        <span>{{ simulation.command }}</span>
      </div>
      <div v-for="(line, index) in simulation.outputLines" :key="index">
        <span class="cli-out-tag">&gt;</span> {{ line }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { CliSimulation } from '@/types/homepage.types';

defineProps<{
  simulation: CliSimulation;
}>();

const emit = defineEmits<{
  (e: 'copyText', value: string, label: string): void;
}>();
</script>
