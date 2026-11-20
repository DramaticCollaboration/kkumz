<template>
  <section id="section-form" class="grid grid-cols-1 lg:grid-cols-12 gap-8 scroll-mt-24" data-testid="sdui-dynamic-form">
    <div
      v-if="cmsForm"
      :class="openRecruits.length ? 'lg:col-span-7' : 'lg:col-span-12'"
      class="sf-card p-7 md:p-9 space-y-6"
    >
      <div class="space-y-2 pb-4 sf-section-divider">
        <span
          class="sf-kicker text-xs font-bold uppercase tracking-[0.16em]"
          :style="{ color: theme.isDark ? theme.accentColor : theme.secondaryColor }"
        >
          {{ copyOverrides.formKicker || 'ONLINE APPLICATION & RESERVATION' }}
        </span>
        <h2 class="sf-heading text-2xl font-bold" :style="{ color: 'var(--sf-ink)' }">
          {{ cmsForm.title || cmsForm.formName }}
        </h2>
        <p v-if="cmsForm.description" class="text-xs sm:text-sm leading-relaxed" :style="{ color: 'var(--sf-ink-soft)' }">
          {{ cmsForm.description }}
        </p>
      </div>

      <div
        v-if="formSubmitResult"
        class="p-4 border text-xs space-y-1"
        :style="{
          backgroundColor: theme.isDark ? 'rgba(16, 185, 129, 0.14)' : '#ecfdf5',
          borderColor: theme.isDark ? 'rgba(52, 211, 153, 0.35)' : '#6ee7b7',
          color: 'var(--sf-ink)',
          borderRadius: theme.radius,
        }"
      >
        <div class="font-bold">신청이 정상 접수되었습니다. (접수번호 #{{ formSubmitResult.submissionId }})</div>
        <div :style="{ color: 'var(--sf-ink-soft)' }">
          신청자: {{ formSubmitResult.applicantName }} · 누적 접수 {{ formSubmitResult.totalSubmissions }}건
        </div>
      </div>

      <div class="space-y-4">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
          <div>
            <label class="block text-xs font-bold mb-1.5" :style="{ color: 'var(--sf-ink)' }">신청자 성함 *</label>
            <input
              v-model="applicantName"
              type="text"
              placeholder="성함을 입력해 주세요"
              class="w-full px-4 py-2.5 text-xs border focus:outline-none"
              :style="inputStyle"
            />
          </div>
          <div>
            <label class="block text-xs font-bold mb-1.5" :style="{ color: 'var(--sf-ink)' }">연락처 *</label>
            <input
              v-model="phone"
              type="tel"
              placeholder="연락처를 입력해 주세요"
              class="w-full px-4 py-2.5 text-xs border focus:outline-none"
              :style="inputStyle"
            />
          </div>
        </div>

        <div v-if="customFields.length" class="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
          <div
            v-for="field in customFields"
            :key="field.resolvedKey"
            :class="field.type === 'checkbox' || field.type === 'textarea' ? 'sm:col-span-2' : ''"
          >
            <label class="block text-xs font-bold mb-1.5" :style="{ color: 'var(--sf-ink)' }">
              {{ field.label }}
            </label>
            <input
              v-if="field.type === 'date'"
              v-model="dynamicValues[field.resolvedKey]"
              type="date"
              class="w-full px-4 py-2.5 text-xs border focus:outline-none"
              :style="inputStyle"
            />
            <select
              v-else-if="field.type === 'select'"
              v-model="dynamicValues[field.resolvedKey]"
              class="w-full px-4 py-2.5 text-xs border focus:outline-none"
              :style="inputStyle"
            >
              <option value="">선택해 주세요</option>
              <option v-for="opt in field.options || []" :key="opt" :value="opt">{{ opt }}</option>
            </select>
            <input
              v-else-if="field.type === 'number'"
              v-model.number="dynamicValues[field.resolvedKey]"
              type="number"
              :min="field.min || 1"
              :max="field.max || 50"
              class="w-full px-4 py-2.5 text-xs border focus:outline-none"
              :style="inputStyle"
            />
            <div v-else-if="field.type === 'checkbox'" class="flex flex-wrap gap-2">
              <label
                v-for="opt in field.options || []"
                :key="opt"
                class="inline-flex items-center gap-1.5 px-3 py-1.5 border text-xs cursor-pointer"
                :style="inputStyle"
              >
                <input
                  type="checkbox"
                  :value="opt"
                  :checked="Array.isArray(dynamicValues[field.resolvedKey]) && dynamicValues[field.resolvedKey].includes(opt)"
                  @change="toggleCheckbox(field.resolvedKey, opt)"
                />
                <span>{{ opt }}</span>
              </label>
            </div>
            <input
              v-else
              v-model="dynamicValues[field.resolvedKey]"
              type="text"
              :placeholder="field.placeholder || `${field.label} 입력`"
              class="w-full px-4 py-2.5 text-xs border focus:outline-none"
              :style="inputStyle"
            />
          </div>
        </div>

        <div>
          <label class="block text-xs font-bold mb-1.5" :style="{ color: 'var(--sf-ink)' }">추가 문의 및 요청사항</label>
          <textarea
            v-model="message"
            rows="3"
            placeholder="희망 일정, 옵션, 문의하실 내용을 자유롭게 남겨주세요"
            class="w-full px-4 py-2.5 text-xs border focus:outline-none"
            :style="inputStyle"
          ></textarea>
        </div>

        <button
          type="button"
          class="w-full py-3.5 text-xs sm:text-sm font-bold sf-btn-primary"
          :disabled="submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '접수 처리 중...' : cmsForm.submitText || '온라인 신청서 제출하기' }}
        </button>
      </div>
    </div>

    <div
      v-if="openRecruits.length"
      :class="cmsForm ? 'lg:col-span-5' : 'lg:col-span-12'"
      class="sf-card-alt p-7 space-y-5"
    >
      <div class="space-y-1 pb-4 sf-section-divider">
        <span class="sf-kicker text-[11px] font-bold uppercase tracking-[0.16em]" :style="{ color: theme.primaryColor }">
          CREW &amp; PARTNER RECRUITMENT
        </span>
        <h3 class="sf-heading text-xl font-bold" :style="{ color: 'var(--sf-ink)' }">
          함께 만들어 갈 크루 · 파트너 모집 ({{ openRecruits.length }})
        </h3>
      </div>

      <div class="space-y-3.5">
        <div
          v-for="rec in openRecruits"
          :key="rec.id"
          class="sf-card p-5 space-y-2.5"
        >
          <div class="flex items-center justify-between gap-2">
            <span
              class="px-2.5 py-0.5 text-[11px] font-bold text-white"
              :style="{ backgroundColor: theme.primaryColor, borderRadius: theme.radius }"
            >
              {{ rec.positionRole }}
            </span>
            <span class="sf-mono text-[11px] font-semibold" :style="{ color: 'var(--sf-ink-muted)' }">
              모집 {{ rec.recruitedCount || 0 }} / {{ rec.headcount || 1 }}명
            </span>
          </div>
          <strong class="block text-sm font-bold leading-snug" :style="{ color: 'var(--sf-ink)' }">
            {{ rec.title }}
          </strong>
          <p class="text-xs leading-relaxed line-clamp-2" :style="{ color: 'var(--sf-ink-soft)' }">
            {{ rec.description }}
          </p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import type { ResolvedThemeSpec, StorefrontCopyOverrides } from '../storefrontTypes';

const props = defineProps<{
  cmsForm?: Record<string, any> | null;
  recruits: Array<Record<string, any>>;
  prefillMessage?: string;
  submitting: boolean;
  formSubmitResult?: Record<string, any> | null;
  copyOverrides: StorefrontCopyOverrides;
  theme: ResolvedThemeSpec;
}>();

const emit = defineEmits<{
  (e: 'submit-form', payload: {
    formId?: any;
    applicantName: string;
    phone: string;
    optionValue: string;
    message: string;
    extraFields: Record<string, any>;
  }): void;
}>();

const applicantName = ref('');
const phone = ref('');
const message = ref('');
const dynamicValues = ref<Record<string, any>>({});

watch(
  () => props.prefillMessage,
  (val) => {
    if (val && val.trim()) {
      message.value = val;
    }
  },
);

const openRecruits = computed(() =>
  (props.recruits || []).filter((r) => String(r.status || 'OPEN').toUpperCase() === 'OPEN'),
);

const customFields = computed(() => {
  const rawFields = props.cmsForm?.fields;
  if (!Array.isArray(rawFields)) return [];
  return rawFields
    .map((f: any) => ({
      ...f,
      resolvedKey: String(f?.key || f?.field || f?.name || ''),
    }))
    .filter(
      (f: any) =>
        f.resolvedKey &&
        !/^(applicantName|phone|message)$/i.test(f.resolvedKey),
    );
});

const inputStyle = computed(() => ({
  backgroundColor: 'var(--sf-card)',
  borderColor: 'var(--sf-border-strong)',
  color: 'var(--sf-ink)',
  borderRadius: props.theme.radius,
}));

function toggleCheckbox(key: string, opt: string) {
  const cur = Array.isArray(dynamicValues.value[key]) ? [...dynamicValues.value[key]] : [];
  const idx = cur.indexOf(opt);
  if (idx >= 0) cur.splice(idx, 1);
  else cur.push(opt);
  dynamicValues.value[key] = cur;
}

function handleSubmit() {
  if (!applicantName.value.trim()) return;
  const summary = Object.entries(dynamicValues.value)
    .filter(([, v]) => v !== undefined && v !== null && String(v).trim() !== '')
    .map(([k, v]) => `${k}:${Array.isArray(v) ? v.join(',') : v}`)
    .join(' | ');
  emit('submit-form', {
    formId: props.cmsForm?.id,
    applicantName: applicantName.value.trim(),
    phone: phone.value.trim(),
    optionValue: summary || '온라인 신청',
    message: message.value.trim(),
    extraFields: { ...dynamicValues.value },
  });
}
</script>
