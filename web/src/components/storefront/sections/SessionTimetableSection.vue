<template>
  <section id="section-timetable" class="space-y-8 scroll-mt-24" data-testid="sdui-session-timetable">
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-3 pb-5 sf-section-divider">
      <div class="space-y-1.5">
        <span
          class="sf-kicker text-xs font-bold uppercase tracking-[0.16em]"
          :style="{ color: theme.primaryColor }"
        >
          {{ copyOverrides.timetableKicker || 'WEEKLY SESSION TIMETABLE' }}
        </span>
        <h2 class="sf-heading text-2xl md:text-3xl font-extrabold" :style="{ color: 'var(--sf-ink)' }">
          {{ copyOverrides.timetableTitle || '주간 정규 세션 & 프로그램 일정표' }}
        </h2>
      </div>
      <p class="text-xs sm:text-sm" :style="{ color: 'var(--sf-ink-muted)' }">
        {{ copyOverrides.timetableSubtitle || '참여하고 싶은 세션을 선택하면 하단 참가 신청 폼에 자동 입력됩니다.' }}
      </p>
    </div>

    <div class="grid gap-5" :class="gridClass">
      <div
        v-for="(session, idx) in sessions"
        :key="idx"
        class="sf-card p-6 flex flex-col justify-between space-y-5 hover:shadow-lg transition"
      >
        <div class="space-y-3.5">
          <div class="flex items-center justify-between gap-2">
            <span
              class="px-3 py-1 text-xs font-extrabold text-white"
              :style="{ backgroundColor: theme.primaryColor, borderRadius: theme.radius }"
            >
              {{ session.day }} · {{ session.time }}
            </span>
            <span
              v-if="session.badge"
              class="px-2.5 py-0.5 text-[11px] font-bold border"
              :style="{
                backgroundColor: 'var(--sf-card-alt)',
                borderColor: 'var(--sf-border)',
                color: 'var(--sf-ink-soft)',
                borderRadius: '999px',
              }"
            >
              {{ session.badge }}
            </span>
          </div>

          <h3 class="sf-heading text-lg font-extrabold leading-snug" :style="{ color: 'var(--sf-ink)' }">
            {{ session.title }}
          </h3>

          <div class="space-y-1.5 text-xs" :style="{ color: 'var(--sf-ink-soft)' }">
            <div v-if="session.location">
              <strong :style="{ color: 'var(--sf-ink)' }">집결지:</strong> {{ session.location }}
            </div>
            <div v-if="session.pace">
              <strong :style="{ color: 'var(--sf-ink)' }">페이스 그룹:</strong> {{ session.pace }}
            </div>
            <div v-if="session.capacity">
              <strong :style="{ color: 'var(--sf-ink)' }">모집 정원:</strong> {{ session.capacity }}
            </div>
          </div>
        </div>

        <button
          type="button"
          class="w-full py-2.5 text-xs font-extrabold sf-btn-outline"
          @click="emit('select-session', session)"
        >
          이 세션 참가 신청하기 -&gt;
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
  sessions: Array<Record<string, any>>;
  copyOverrides: StorefrontCopyOverrides;
  theme: ResolvedThemeSpec;
}>();

const emit = defineEmits<{
  (e: 'select-session', session: Record<string, any>): void;
}>();

const gridClass = computed(() => resolveGridColumnsClass(props.sessions.length, 3));
</script>
