<template>
  <section
    id="section-faq-location"
    class="space-y-10 scroll-mt-24"
    data-testid="sdui-faq-location"
  >
    <div v-if="downloadableDocs.length" class="space-y-5">
      <div class="pb-4 sf-section-divider space-y-1">
        <span class="sf-kicker text-xs font-bold uppercase tracking-[0.16em]" :style="{ color: theme.secondaryColor }">
          OFFICIAL BROCHURE &amp; DOCUMENTS
        </span>
        <h2 class="sf-heading text-2xl font-bold" :style="{ color: 'var(--sf-ink)' }">
          공식 안내자료 &amp; 첨부파일
        </h2>
      </div>
      <div class="grid gap-4" :class="docsGridClass">
        <div
          v-for="(doc, dIdx) in downloadableDocs"
          :key="doc.id || dIdx"
          class="sf-card p-5 flex items-center justify-between gap-4"
        >
          <div class="flex items-center gap-3.5 min-w-0">
            <span
              class="sf-mono px-2.5 py-1.5 text-xs font-bold text-white shrink-0"
              :style="{ backgroundColor: theme.primaryColor, borderRadius: theme.radius }"
            >
              {{ (doc.fileExt || 'DOC').toUpperCase() }}
            </span>
            <div class="min-w-0">
              <strong class="block text-sm font-bold truncate" :style="{ color: 'var(--sf-ink)' }">
                {{ doc.caption || doc.originalName }}
              </strong>
              <span class="text-xs" :style="{ color: 'var(--sf-ink-muted)' }">
                {{ doc.originalName }} · {{ formatSize(doc.fileSize) }}
              </span>
            </div>
          </div>
          <a
            :href="doc.downloadUrl || `${doc.fileUrl}?download=true`"
            target="_blank"
            rel="noopener noreferrer"
            class="px-3.5 py-2 text-xs font-bold sf-btn-primary shrink-0"
          >
            다운로드
          </a>
        </div>
      </div>
    </div>

    <div
      v-if="faqItems.length || locationGuide || policyRules.length"
      class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start"
    >
      <div
        v-if="faqItems.length"
        :class="hasRightColumn ? 'lg:col-span-6' : 'lg:col-span-12'"
        class="space-y-5"
      >
        <div class="pb-4 sf-section-divider space-y-1">
          <span
            class="sf-kicker text-xs font-bold uppercase tracking-[0.16em]"
            :style="{ color: theme.isDark ? theme.accentColor : theme.secondaryColor }"
          >
            {{ copyOverrides.faqKicker || 'FREQUENTLY ASKED QUESTIONS' }}
          </span>
          <h2 class="sf-heading text-2xl font-bold" :style="{ color: 'var(--sf-ink)' }">
            {{ copyOverrides.faqTitle || '자주 묻는 질문 (FAQ)' }}
          </h2>
        </div>

        <div class="space-y-3">
          <div
            v-for="(item, fIdx) in faqItems"
            :key="item.id || fIdx"
            class="sf-card overflow-hidden"
          >
            <button
              type="button"
              class="w-full px-5 py-4 text-left flex items-center justify-between gap-3 font-bold text-xs sm:text-sm transition"
              :style="{ color: 'var(--sf-ink)' }"
              @click="openIdx = openIdx === fIdx ? -1 : fIdx"
            >
              <span>Q. {{ item.q }}</span>
              <span class="sf-mono text-xs shrink-0" :style="{ color: 'var(--sf-ink-muted)' }">
                {{ openIdx === fIdx ? '-' : '+' }}
              </span>
            </button>
            <div
              v-if="openIdx === fIdx"
              class="px-5 py-4 text-xs sm:text-sm leading-relaxed border-t"
              :style="{
                borderColor: 'var(--sf-border)',
                backgroundColor: 'var(--sf-card-alt)',
                color: 'var(--sf-ink-soft)',
              }"
            >
              {{ item.a }}
            </div>
          </div>
        </div>
      </div>

      <div
        v-if="hasRightColumn"
        :class="faqItems.length ? 'lg:col-span-6' : 'lg:col-span-12'"
        class="space-y-5"
      >
        <div v-if="locationGuide" class="sf-card p-6 space-y-4">
          <div class="space-y-1">
            <span
              class="sf-kicker text-[11px] font-bold uppercase tracking-wider"
              :style="{ color: theme.secondaryColor }"
            >
              LOCATION &amp; ACCESS GUIDE
            </span>
            <h3 class="sf-heading text-xl font-bold" :style="{ color: 'var(--sf-ink)' }">
              오시는 길 · 교통 및 주변 안내
            </h3>
          </div>

          <div class="space-y-2 text-xs sm:text-sm" :style="{ color: 'var(--sf-ink-soft)' }">
            <div><strong :style="{ color: 'var(--sf-ink)' }">주소:</strong> {{ locationGuide.address }}</div>
            <div v-if="locationGuide.parking">
              <strong :style="{ color: 'var(--sf-ink)' }">주차 안내:</strong> {{ locationGuide.parking }}
            </div>
            <div v-if="locationGuide.publicTransit">
              <strong :style="{ color: 'var(--sf-ink)' }">대중교통:</strong> {{ locationGuide.publicTransit }}
            </div>
          </div>

          <div
            v-if="nearbySpots.length"
            class="pt-4 border-t space-y-2.5"
            :style="{ borderColor: 'var(--sf-border)' }"
          >
            <strong class="block text-xs font-bold" :style="{ color: 'var(--sf-ink)' }">
              함께 둘러보기 좋은 주변 코스
            </strong>
            <div class="grid gap-2.5" :class="spotsGridClass">
              <div
                v-for="(spot, spIdx) in nearbySpots"
                :key="spIdx"
                class="p-3 border text-[11px] space-y-1"
                :style="{
                  backgroundColor: 'var(--sf-card-alt)',
                  borderColor: 'var(--sf-border)',
                  borderRadius: theme.radius,
                }"
              >
                <strong class="block" :style="{ color: 'var(--sf-ink)' }">{{ spot.name }}</strong>
                <span class="font-bold block" :style="{ color: theme.primaryColor }">{{ spot.distance }}</span>
                <p :style="{ color: 'var(--sf-ink-muted)' }">{{ spot.desc }}</p>
              </div>
            </div>
          </div>
        </div>

        <div v-if="policyRules.length" class="sf-card-alt p-6 space-y-3">
          <strong class="block sf-heading text-sm font-bold" :style="{ color: 'var(--sf-ink)' }">
            이용 수칙 및 배송 · 교환 · 환불 안내
          </strong>
          <ul class="space-y-2 text-xs list-disc pl-4" :style="{ color: 'var(--sf-ink-soft)' }">
            <li v-for="(rule, rIdx) in policyRules" :key="rIdx">{{ rule }}</li>
          </ul>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import {
  resolveGridColumnsClass,
  type ResolvedThemeSpec,
  type StorefrontCopyOverrides,
} from '../storefrontTypes';

const props = defineProps<{
  faqItems: Array<Record<string, any>>;
  locationGuide?: Record<string, any> | null;
  policyRules: string[];
  downloadableDocs: Array<Record<string, any>>;
  copyOverrides: StorefrontCopyOverrides;
  theme: ResolvedThemeSpec;
}>();

const openIdx = ref<number>(0);

const hasRightColumn = computed(() => Boolean(props.locationGuide) || props.policyRules.length > 0);
const nearbySpots = computed<Array<Record<string, any>>>(() => {
  const list = props.locationGuide?.nearbySpots || props.locationGuide?.nearbyCourses || [];
  return Array.isArray(list) ? list : [];
});
const spotsGridClass = computed(() => resolveGridColumnsClass(nearbySpots.value.length, 3));
const docsGridClass = computed(() => resolveGridColumnsClass(props.downloadableDocs.length, 2));

function formatSize(bytes: any): string {
  const num = Number(bytes || 0);
  if (!num || num <= 0) return '0 KB';
  if (num < 1024) return `${num} B`;
  if (num < 1024 * 1024) return `${(num / 1024).toFixed(1)} KB`;
  return `${(num / (1024 * 1024)).toFixed(2)} MB`;
}
</script>
