<template>
  <section id="section-subscription" class="space-y-8 scroll-mt-24" data-testid="sdui-subscription-tiers">
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-3 pb-5 sf-section-divider">
      <div class="space-y-1.5">
        <span
          class="sf-kicker text-xs font-bold uppercase tracking-[0.16em]"
          :style="{ color: theme.isDark ? theme.accentColor : theme.secondaryColor }"
        >
          {{ copyOverrides.subscriptionKicker || 'SUBSCRIPTION & MEMBERSHIP' }}
        </span>
        <h2 class="sf-heading text-2xl md:text-3xl font-bold" :style="{ color: 'var(--sf-ink)' }">
          {{ copyOverrides.subscriptionTitle || '정기구독 & 멤버십 플랜' }}
        </h2>
      </div>
      <p class="text-xs sm:text-sm" :style="{ color: 'var(--sf-ink-muted)' }">
        {{ copyOverrides.subscriptionSubtitle || '취향과 주기에 맞춰 매월 정기적으로 받아보는 큐레이션 플랜입니다.' }}
      </p>
    </div>

    <div class="grid gap-6" :class="gridClass">
      <div
        v-for="(plan, idx) in tiers"
        :key="idx"
        class="p-7 flex flex-col justify-between space-y-6 transition relative"
        :class="plan.highlighted ? 'shadow-xl ring-2' : 'sf-card'"
        :style="planCardStyle(Boolean(plan.highlighted))"
      >
        <div class="space-y-4">
          <div class="flex items-center justify-between gap-2">
            <span
              class="sf-mono text-[11px] font-bold uppercase tracking-wider"
              :style="{ color: plan.highlighted ? '#ffffff' : 'var(--sf-ink-muted)' }"
            >
              {{ plan.tier }}
            </span>
            <span
              v-if="plan.badge"
              class="px-2.5 py-0.5 text-[11px] font-bold rounded-full"
              :style="{
                backgroundColor: plan.highlighted ? 'rgba(255,255,255,0.2)' : 'var(--sf-card-alt)',
                color: plan.highlighted ? '#ffffff' : theme.primaryColor,
              }"
            >
              {{ plan.badge }}
            </span>
          </div>

          <div>
            <div class="flex items-baseline gap-1.5">
              <strong
                class="sf-heading text-3xl font-extrabold"
                :style="{ color: plan.highlighted ? '#ffffff' : 'var(--sf-ink)' }"
              >
                {{ plan.price }}
              </strong>
              <span
                class="text-xs"
                :style="{ color: plan.highlighted ? 'rgba(255,255,255,0.78)' : 'var(--sf-ink-muted)' }"
              >
                {{ plan.period }}
              </span>
            </div>
            <p
              class="text-xs leading-relaxed mt-3"
              :style="{ color: plan.highlighted ? 'rgba(255,255,255,0.88)' : 'var(--sf-ink-soft)' }"
            >
              {{ plan.desc }}
            </p>
          </div>

          <ul class="space-y-2 pt-3 border-t text-xs" :style="{ borderColor: plan.highlighted ? 'rgba(255,255,255,0.2)' : 'var(--sf-border)' }">
            <li
              v-for="(feat, fIdx) in plan.features || []"
              :key="fIdx"
              class="flex items-start gap-2"
              :style="{ color: plan.highlighted ? '#ffffff' : 'var(--sf-ink-soft)' }"
            >
              <span class="font-bold shrink-0" :style="{ color: plan.highlighted ? theme.accentColor : theme.primaryColor }">
                +
              </span>
              <span>{{ feat }}</span>
            </li>
          </ul>
        </div>

        <button
          type="button"
          class="w-full py-3 text-xs font-bold transition"
          :style="planButtonStyle(Boolean(plan.highlighted))"
          @click="emit('select-plan', plan)"
        >
          {{ plan.tier }} 상담 · 구독 신청하기
        </button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import {
  resolveGridColumnsClass,
  type ResolvedThemeSpec,
  type StorefrontCopyOverrides,
} from '../storefrontTypes';

const props = defineProps<{
  tiers: Array<Record<string, any>>;
  copyOverrides: StorefrontCopyOverrides;
  theme: ResolvedThemeSpec;
}>();

const emit = defineEmits<{
  (e: 'select-plan', plan: Record<string, any>): void;
}>();

const gridClass = computed(() => resolveGridColumnsClass(props.tiers.length, 3));

function planCardStyle(highlighted: boolean) {
  if (highlighted) {
    return {
      backgroundColor: props.theme.primaryColor,
      color: '#ffffff',
      borderRadius: 'var(--sf-radius-lg)',
      '--tw-ring-color': props.theme.accentColor,
    } as Record<string, string>;
  }
  return {};
}

function planButtonStyle(highlighted: boolean) {
  if (highlighted) {
    return {
      backgroundColor: '#ffffff',
      color: props.theme.primaryColor,
      borderRadius: props.theme.radius,
    };
  }
  return {
    backgroundColor: props.theme.primaryColor,
    color: '#ffffff',
    borderRadius: props.theme.radius,
  };
}
</script>
