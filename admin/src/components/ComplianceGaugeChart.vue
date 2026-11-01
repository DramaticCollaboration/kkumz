<template>
  <div
    class="compliance-gauge-card p-4 bg-white rounded shadow cursor-pointer transition-colors hover:bg-gray-50"
    role="button"
    tabindex="0"
    :aria-label="`Compliance Rate ${formattedRate}%`"
    @click="handleDrillDown"
    @keydown.enter="handleDrillDown"
  >
    <h3 class="text-lg font-bold text-gray-800">{{ title }}</h3>
    <div class="text-4xl font-bold compliance-rate-value">{{ formattedRate }}%</div>
    <p v-if="hasUnencryptedItems" class="mt-2 font-semibold flex items-center gap-1.5 compliance-alert-text">
      <span class="inline-block w-2 h-2 rounded-full bg-red-500"></span>
      <span>{{ unencryptedCount }}건 승인 검토 필요</span>
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps({
  title: {
    type: String,
    default: 'Compliance Rate',
  },
  targetSystem: {
    type: String,
    default: 'CRM_PROD',
  },
});

const router = useRouter();
const complianceRate = ref<number>(92.5);
const unencryptedCount = ref<number>(320);

const formattedRate = computed(() => complianceRate.value.toFixed(1));
const hasUnencryptedItems = computed(() => unencryptedCount.value > 0);

function handleDrillDown() {
  if (hasUnencryptedItems.value) {
    router.push({
      path: '/approval/hitl',
      query: {
        system_id: props.targetSystem,
        status: 'unencrypted',
      },
    });
  }
}
</script>

<style scoped>
.compliance-gauge-card {
  user-select: none;
}

.compliance-rate-value {
  color: var(--color-primary, #2563eb);
}

.compliance-alert-text {
  color: var(--color-alert-danger, #dc2626);
}
</style>
