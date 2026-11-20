<template>
  <section id="section-pricing" class="space-y-7 scroll-mt-24" data-testid="sdui-pricing-matrix">
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-3 pb-5 sf-section-divider">
      <div class="space-y-1.5">
        <span
          class="sf-kicker text-xs font-bold uppercase tracking-[0.16em]"
          :style="{ color: theme.isDark ? theme.accentColor : theme.secondaryColor }"
        >
          {{ copyOverrides.pricingKicker || 'SEASON & GUEST PRICING MATRIX' }}
        </span>
        <h2 class="sf-heading text-2xl md:text-3xl font-bold" :style="{ color: 'var(--sf-ink)' }">
          {{ copyOverrides.pricingTitle || pricingMatrix.title || '상세 요금 안내' }}
        </h2>
      </div>
      <p class="text-xs sm:text-sm" :style="{ color: 'var(--sf-ink-muted)' }">
        {{ copyOverrides.pricingSubtitle || '시즌·요일 및 기준 인원별 공식 요금 안내입니다.' }}
      </p>
    </div>

    <div class="overflow-x-auto sf-card shadow-sm">
      <table class="w-full text-left border-collapse text-xs md:text-sm">
        <thead v-if="Array.isArray(pricingMatrix.columns) && pricingMatrix.columns.length">
          <tr :style="{ backgroundColor: theme.primaryColor, color: '#ffffff' }">
            <th
              v-for="col in pricingMatrix.columns"
              :key="col"
              class="py-4 px-5 font-bold whitespace-nowrap"
            >
              {{ col }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(row, rIdx) in pricingMatrix.rows || []"
            :key="rIdx"
            class="border-t transition"
            :style="{ borderColor: 'var(--sf-border)' }"
          >
            <td class="py-4 px-5 font-bold whitespace-nowrap" :style="{ color: 'var(--sf-ink)' }">
              {{ row.tier }}
            </td>
            <td class="py-4 px-5 whitespace-nowrap" :style="{ color: 'var(--sf-ink-soft)' }">
              {{ row.capacity || row.target }}
            </td>
            <td class="py-4 px-5 font-semibold whitespace-nowrap" :style="{ color: 'var(--sf-ink)' }">
              {{ row.offPeakWeekday }}
            </td>
            <td class="py-4 px-5 font-bold whitespace-nowrap" :style="{ color: theme.secondaryColor }">
              {{ row.offPeakWeekend }}
            </td>
            <td class="py-4 px-5 font-extrabold whitespace-nowrap" :style="{ color: theme.accentColor }">
              {{ row.peakSeason }}
            </td>
            <td class="py-4 px-5" :style="{ color: 'var(--sf-ink-soft)' }">
              {{ row.note || row.includes }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { ResolvedThemeSpec, StorefrontCopyOverrides } from '../storefrontTypes';

defineProps<{
  pricingMatrix: Record<string, any>;
  copyOverrides: StorefrontCopyOverrides;
  theme: ResolvedThemeSpec;
}>();
</script>
