<template>
  <section id="section-spec" class="space-y-7 scroll-mt-24" data-testid="sdui-spec-comparison">
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-3 pb-5 sf-section-divider">
      <div class="space-y-1.5">
        <span
          class="sf-kicker text-xs font-bold uppercase tracking-[0.16em]"
          :style="{ color: theme.isDark ? theme.accentColor : theme.secondaryColor }"
        >
          {{ copyOverrides.specKicker || 'DETAILED SPECIFICATION MATRIX' }}
        </span>
        <h2 class="sf-heading text-2xl md:text-3xl font-bold" :style="{ color: 'var(--sf-ink)' }">
          {{ copyOverrides.specTitle || specComparison.title || '상세 사양 & 호환성 비교표' }}
        </h2>
      </div>
      <p class="text-xs sm:text-sm" :style="{ color: 'var(--sf-ink-muted)' }">
        {{ copyOverrides.specSubtitle || '주요 모델별 소재, 규격, 호환성 제원을 한눈에 비교할 수 있습니다.' }}
      </p>
    </div>

    <div class="overflow-x-auto sf-card shadow-sm">
      <table class="w-full text-left border-collapse text-xs md:text-sm">
        <thead>
          <tr
            :style="{
              backgroundColor: theme.isDark ? 'rgba(255,255,255,0.06)' : 'var(--sf-ink)',
              color: '#ffffff',
            }"
          >
            <th
              v-for="col in resolvedColumns"
              :key="col"
              class="py-4 px-5 font-bold whitespace-nowrap"
              :class="theme.isDark ? 'sf-mono text-[11px] uppercase tracking-wider text-zinc-200' : ''"
            >
              {{ col }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(row, rIdx) in specComparison.rows || []"
            :key="rIdx"
            class="border-t transition"
            :style="{ borderColor: 'var(--sf-border)' }"
          >
            <td
              class="py-4 px-5 font-bold whitespace-nowrap"
              :style="{ color: 'var(--sf-ink)' }"
            >
              {{ row.tier || row.name }}
            </td>
            <td
              v-if="hasSpec1"
              class="py-4 px-5"
              :class="theme.isDark ? 'sf-mono text-xs text-zinc-300' : ''"
              :style="theme.isDark ? {} : { color: 'var(--sf-ink-soft)' }"
            >
              {{ row.spec1 }}
            </td>
            <td
              v-if="hasSpec2"
              class="py-4 px-5 font-semibold whitespace-nowrap"
              :class="theme.isDark ? 'sf-mono text-xs' : ''"
              :style="{ color: theme.isDark ? theme.accentColor : theme.primaryColor }"
            >
              {{ row.spec2 }}
            </td>
            <td
              v-if="hasSpec3"
              class="py-4 px-5"
              :style="{ color: 'var(--sf-ink-soft)' }"
            >
              {{ row.spec3 }}
            </td>
            <td class="py-4 px-5" :style="{ color: 'var(--sf-ink-muted)' }">
              {{ row.note || row.includes }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ResolvedThemeSpec, StorefrontCopyOverrides } from '../storefrontTypes';

const props = defineProps<{
  specComparison: Record<string, any>;
  copyOverrides: StorefrontCopyOverrides;
  theme: ResolvedThemeSpec;
}>();

const rows = computed<Array<Record<string, any>>>(() =>
  Array.isArray(props.specComparison?.rows) ? props.specComparison.rows : [],
);

const hasSpec1 = computed(() => rows.value.some((r) => Boolean(r.spec1)));
const hasSpec2 = computed(() => rows.value.some((r) => Boolean(r.spec2)));
const hasSpec3 = computed(() => rows.value.some((r) => Boolean(r.spec3)));

const resolvedColumns = computed<string[]>(() => {
  if (Array.isArray(props.specComparison?.columns) && props.specComparison.columns.length > 0) {
    return props.specComparison.columns;
  }
  const cols = ['모델 / 항목'];
  if (hasSpec1.value) cols.push('핵심 사양');
  if (hasSpec2.value) cols.push('규격 / 출력');
  if (hasSpec3.value) cols.push('호환성');
  cols.push('비고');
  return cols;
});
</script>
