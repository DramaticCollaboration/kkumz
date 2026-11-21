<template>
  <div class="min-h-screen w-full flex flex-col">
    <div
      v-if="loading"
      class="min-h-screen flex flex-col items-center justify-center gap-3 bg-white text-slate-400"
    >
      <div class="w-9 h-9 rounded-full border-3 border-slate-200 border-t-slate-800 animate-spin"></div>
      <p class="text-sm font-bold text-slate-500">공식 브랜드 스토어를 불러오는 중입니다...</p>
    </div>

    <div
      v-else-if="accessBlocked"
      class="min-h-screen flex items-center justify-center p-6 bg-slate-950 text-white"
      data-testid="live-site-access-blocked"
    >
      <div
        class="max-w-lg w-full rounded-3xl bg-slate-900 border border-slate-800 p-8 md:p-10 text-center space-y-5 shadow-2xl"
      >
        <span
          class="inline-block px-3.5 py-1 rounded-full bg-amber-500/15 text-amber-300 border border-amber-400/30 text-xs font-extrabold tracking-wide"
        >
          PRIVATE DRAFT ISOLATION · HTTP 403
        </span>
        <h1 class="text-2xl font-extrabold text-white">
          아직 배포되지 않았거나 비공개 상태인 사이트입니다
        </h1>
        <p class="text-sm text-slate-300 leading-relaxed">
          요청하신 <code class="px-2 py-0.5 rounded bg-slate-800 text-amber-200 font-mono">/s/{{ currentSlug }}</code>
          사이트는 현재 소유자가 제작 중인 초안이거나 비공개 상태로 전환되어 있습니다.<br />
          소유자가 스튜디오에서 사이트 배포를 완료한 이후에만 외부 방문자가 열람할 수 있습니다.
        </p>
        <div class="pt-2 flex flex-wrap items-center justify-center gap-3">
          <button
            type="button"
            class="px-5 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-extrabold transition"
            data-testid="blocked-go-feed-btn"
            @click="router.push('/feed')"
          >
            쇼케이스 피드로 이동
          </button>
          <button
            type="button"
            class="px-5 py-3 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-100 text-xs font-extrabold transition"
            data-testid="blocked-login-owner-btn"
            @click="openAuthModal('login', () => loadRuntime(), '이 사이트의 소유자 또는 협업 팀원 계정으로 로그인해 주세요.')"
          >
            소유자 계정으로 로그인
          </button>
        </div>
      </div>
    </div>

    <template v-else-if="runtime">
      <div
        v-if="!runtime.isPublished || runtime.hasUnpublishedChanges"
        class="w-full py-2.5 px-4 bg-amber-950 text-amber-100 border-b border-amber-700/60 text-xs"
        data-testid="owner-preview-banner"
      >
        <div class="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-3">
          <div class="flex flex-wrap items-center gap-2">
            <span class="px-2.5 py-0.5 rounded-full bg-amber-400 text-slate-950 font-extrabold text-[11px]">
              {{
                !runtime.isPublished
                  ? '비공개 초안 (나만 보기)'
                  : `미배포 수정본 프리뷰 (작업 v${runtime.activeVersion})`
              }}
            </span>
            <span class="font-semibold">
              현재 나만 볼 수 있는 미리보기 상태입니다. 다른 사용자에게 공개하려면 스튜디오에서 배포를 완료해 주세요.
            </span>
          </div>
          <button
            type="button"
            class="px-3.5 py-1.5 rounded-lg bg-amber-400 hover:bg-amber-300 text-slate-950 font-extrabold text-xs transition shrink-0"
            data-testid="owner-preview-deploy-btn"
            @click="goStudioDeploy"
          >
            스튜디오에서 배포하기
          </button>
        </div>
      </div>

      <StorefrontRenderer
        :runtime="runtime"
        :preview-mode="false"
        :placing-order="placingOrder"
        :order-result="orderResult"
        :submitting-form="submittingForm"
        :form-submit-result="formSubmitResult"
        :concierge-messages="conciergeMessages"
        :asking-concierge="askingConcierge"
        @checkout="handleCheckout"
        @dismiss-order-result="orderResult = null"
        @submit-form="handleSubmitForm"
        @ask-concierge="handleAskConcierge"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { extractErrorMessage, type KkumzEntity, type KkumzRecord } from '@/types/kkumz';
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { kkumzApi, kkumzAuth, openAuthModal } from '@/api/kkumzApi';
import StorefrontRenderer from '@/components/storefront/StorefrontRenderer.vue';
import type { StorefrontCartItem } from '@/components/storefront/storefrontTypes';
import type { ConciergeMessage } from '@/components/storefront/modals/FloatingConciergeWidget.vue';

const route = useRoute();
const router = useRouter();

const currentSlug = computed(() => (route.params.siteSlug as string) || '');
const loading = ref(false);
const runtime = ref<Record<string, any> | null>(null);
const accessBlocked = ref(false);

const placingOrder = ref(false);
const orderResult = ref<Record<string, any> | null>(null);

const submittingForm = ref(false);
const formSubmitResult = ref<Record<string, any> | null>(null);

const conciergeMessages = ref<ConciergeMessage[]>([]);
const askingConcierge = ref(false);

function normalizeRuntimePayload(raw: KkumzEntity | null | undefined): KkumzEntity | null {
  if (!raw) return null;
  const site = (raw.site || raw) as KkumzEntity;
  const rel = (raw.release || {}) as KkumzEntity;
  return {
    ...site,
    ...raw,
    layoutSchema: raw.layoutSchema || rel.layoutSchema || {},
    designTokens: raw.designTokens || rel.designTokens || {},
  };
}

async function loadRuntime() {
  if (!currentSlug.value) return;
  loading.value = true;
  accessBlocked.value = false;
  orderResult.value = null;
  formSubmitResult.value = null;
  conciergeMessages.value = [];
  try {
    const raw = await kkumzApi.getSiteRuntime(currentSlug.value);
    runtime.value = normalizeRuntimePayload(raw);
    if (runtime.value) {
      const titleName = runtime.value.brandName || runtime.value.siteName || currentSlug.value;
      document.title = `${titleName} — Official Brand Site`;
    }
  } catch {
    runtime.value = null;
    accessBlocked.value = true;
  } finally {
    loading.value = false;
  }
}

function goStudioDeploy() {
  if (runtime.value?.ideaId) {
    router.push({ name: 'StudioView', params: { ideaId: runtime.value.ideaId } });
  } else {
    router.push('/studio');
  }
}

async function handleCheckout(payload: {
  items: StorefrontCartItem[];
  couponCode?: string;
  discountAmount?: number;
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
}) {
  if (!payload.items.length || placingOrder.value) return;
  placingOrder.value = true;
  try {
    let lastResult: KkumzEntity = null;
    let totalQty = 0;
    let totalAmount = 0;
    let remainingDiscount = Math.max(0, Number(payload.discountAmount || 0));
    const itemNames: string[] = [];
    for (const item of payload.items) {
      const qty = Number(item.quantity || 1);
      const itemGross = Number(item.unitPrice || 0) * qty;
      const appliedDiscount = Math.min(remainingDiscount, itemGross);
      remainingDiscount = Math.max(0, remainingDiscount - appliedDiscount);
      const noteParts: string[] = [];
      if (item.selectedOption) {
        noteParts.push(`[옵션: ${item.selectedOption}]`);
      }
      if (appliedDiscount > 0 && payload.couponCode) {
        noteParts.push(`[쿠폰: ${payload.couponCode} (-${appliedDiscount.toLocaleString()}원)]`);
      }
      const res = await kkumzApi.createCommerceOrder(currentSlug.value, {
        productId: Number(item.productId),
        quantity: qty,
        receiverName: payload.receiverName,
        receiverPhone: payload.receiverPhone,
        receiverAddress: payload.receiverAddress,
        discountAmount: appliedDiscount > 0 ? appliedDiscount : undefined,
        note: noteParts.length > 0 ? noteParts.join(' ') : undefined,
      });
      lastResult = res;
      totalQty += qty;
      totalAmount += Number(res?.payAmount ?? Math.max(0, itemGross - appliedDiscount));
      itemNames.push(item.selectedOption ? `${item.name} (${item.selectedOption})` : item.name);
    }
    if (lastResult) {
      orderResult.value = {
        ...lastResult,
        productName: itemNames.length > 1 ? `${itemNames[0]} 외 ${itemNames.length - 1}건` : itemNames[0],
        quantity: totalQty,
        payAmount: totalAmount,
      };
    }
    runtime.value = normalizeRuntimePayload(await kkumzApi.getSiteRuntime(currentSlug.value));
  } finally {
    placingOrder.value = false;
  }
}

async function handleSubmitForm(payload: {
  formId?: string | number;
  applicantName: string;
  phone: string;
  optionValue: string;
  message: string;
  extraFields: KkumzRecord;
}) {
  if (!payload.applicantName.trim() || submittingForm.value) return;
  submittingForm.value = true;
  try {
    formSubmitResult.value = await kkumzApi.submitCmsForm(currentSlug.value, {
      formId: payload.formId !== undefined ? Number(payload.formId) : undefined,
      applicantName: payload.applicantName,
      phone: payload.phone,
      optionValue: payload.optionValue,
      message: payload.message,
      extraFields: payload.extraFields,
    });
    runtime.value = normalizeRuntimePayload(await kkumzApi.getSiteRuntime(currentSlug.value));
  } finally {
    submittingForm.value = false;
  }
}

async function handleAskConcierge(question: string) {
  const clean = question.trim();
  if (!clean || askingConcierge.value) return;
  conciergeMessages.value.push({ role: 'user', text: clean });
  askingConcierge.value = true;
  try {
    const res = await kkumzApi.askSiteConcierge(currentSlug.value, clean);
    conciergeMessages.value.push({
      role: 'assistant',
      text: String(res?.answer || '응답을 받지 못했습니다.'),
    });
  } catch (err: unknown) {
    conciergeMessages.value.push({
      role: 'assistant',
      text: extractErrorMessage(err, '상담 응답 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.'),
    });
  } finally {
    askingConcierge.value = false;
  }
}

watch(
  () => route.params.siteSlug,
  () => {
    loadRuntime();
  },
);

watch(
  () => kkumzAuth.user?.username,
  () => {
    loadRuntime();
  },
);

onMounted(() => {
  loadRuntime();
});
</script>
