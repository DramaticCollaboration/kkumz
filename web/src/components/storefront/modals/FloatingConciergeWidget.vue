<template>
  <div
    class="z-40"
    :class="previewMode ? 'relative mt-10' : 'fixed bottom-5 right-5 sm:bottom-7 sm:right-7'"
    data-testid="sf-floating-concierge"
  >
    <div
      v-if="open || previewMode"
      class="w-full shadow-2xl border overflow-hidden flex flex-col"
      :class="previewMode ? 'max-w-2xl mx-auto' : 'w-[calc(100vw-2.5rem)] sm:w-[390px] mb-3'"
      :style="{
        backgroundColor: theme.isDark ? '#111113' : '#0f172a',
        borderColor: 'rgba(255,255,255,0.14)',
        borderRadius: 'var(--sf-radius-lg)',
        color: '#ffffff',
      }"
    >
      <div class="px-5 py-4 flex items-center justify-between gap-3 border-b border-white/10">
        <div class="space-y-0.5 min-w-0">
          <div class="flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-emerald-400"></span>
            <span class="sf-mono text-[10px] font-bold uppercase tracking-widest text-white/60">
              AI BRAND CONCIERGE
            </span>
          </div>
          <strong class="block text-sm font-bold truncate">
            {{ copyOverrides.conciergeTitle || `${brandName} AI 컨시어지` }}
          </strong>
        </div>
        <button
          v-if="!previewMode"
          type="button"
          class="px-2.5 py-1 rounded-lg bg-white/10 hover:bg-white/20 text-xs font-bold text-white shrink-0"
          @click="emit('toggle', false)"
        >
          닫기
        </button>
      </div>

      <div class="p-4 space-y-3 max-h-72 overflow-y-auto text-xs leading-relaxed">
        <div class="p-3.5 rounded-xl bg-white/10 border border-white/10 text-white/90">
          {{ greetingText }}
        </div>

        <div
          v-for="(msg, mIdx) in messages"
          :key="mIdx"
          class="flex"
          :class="msg.role === 'user' ? 'justify-end' : 'justify-start'"
        >
          <div
            class="max-w-[85%] px-3.5 py-2.5 rounded-xl whitespace-pre-wrap"
            :style="
              msg.role === 'user'
                ? { backgroundColor: theme.primaryColor, color: '#ffffff' }
                : { backgroundColor: 'rgba(255,255,255,0.08)', color: 'rgba(255,255,255,0.92)' }
            "
          >
            {{ msg.text }}
          </div>
        </div>

        <div v-if="asking" class="text-[11px] text-white/60 px-1">
          답변을 생성하고 있습니다...
        </div>
      </div>

      <div v-if="presetQuestions.length > 0" class="px-4 pb-2 flex flex-wrap gap-1.5">
        <button
          v-for="(preset, pIdx) in presetQuestions"
          :key="pIdx"
          type="button"
          class="px-2.5 py-1 rounded-full bg-white/10 hover:bg-white/20 text-[11px] font-semibold text-white/90 transition text-left"
          :disabled="asking"
          @click="sendQuestion(preset)"
        >
          {{ preset }}
        </button>
      </div>

      <form
        class="p-3.5 border-t border-white/10 flex gap-2"
        @submit.prevent="sendQuestion(draft)"
      >
        <input
          v-model="draft"
          type="text"
          placeholder="궁금한 점을 질문해 주세요..."
          class="flex-1 px-3.5 py-2 rounded-lg bg-white/10 border border-white/15 text-xs text-white placeholder:text-white/45 focus:outline-none"
        />
        <button
          type="submit"
          class="px-4 py-2 text-xs font-bold text-white shrink-0 transition hover:opacity-90"
          :style="{ backgroundColor: theme.primaryColor, borderRadius: theme.radius }"
          :disabled="asking || !draft.trim()"
        >
          전송
        </button>
      </form>
    </div>

    <button
      v-if="!previewMode"
      type="button"
      class="ml-auto px-5 py-3.5 rounded-full shadow-2xl text-xs font-extrabold text-white flex items-center gap-2.5 border border-white/20 transition hover:scale-[1.02]"
      :style="{ backgroundColor: theme.primaryColor }"
      data-testid="sf-concierge-launcher-btn"
      @click="emit('toggle', !open)"
    >
      <span class="w-2 h-2 rounded-full bg-emerald-300"></span>
      <span>{{ open ? '상담창 접기' : 'AI 컨시어지 상담' }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import type { ResolvedThemeSpec, StorefrontCopyOverrides } from '../storefrontTypes';

export interface ConciergeMessage {
  role: 'user' | 'assistant';
  text: string;
}

const props = defineProps<{
  open: boolean;
  brandName: string;
  greeting?: string;
  messages: ConciergeMessage[];
  asking: boolean;
  copyOverrides: StorefrontCopyOverrides;
  theme: ResolvedThemeSpec;
  previewMode?: boolean;
}>();

const emit = defineEmits<{
  (e: 'toggle', value: boolean): void;
  (e: 'ask', question: string): void;
}>();

const draft = ref('');

const greetingText = computed(
  () =>
    props.greeting ||
    props.copyOverrides.conciergeSubtitle ||
    `안녕하세요. ${props.brandName} AI 컨시어지입니다. 궁금하신 점을 편하게 질문해 주세요.`,
);

const presetQuestions = computed<string[]>(() => {
  const list = props.copyOverrides.conciergePresets;
  if (Array.isArray(list) && list.length > 0) {
    return list.slice(0, 3);
  }
  return [];
});

function sendQuestion(text: string) {
  const clean = text.trim();
  if (!clean || props.asking) return;
  draft.value = '';
  emit('ask', clean);
}
</script>
