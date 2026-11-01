<template>
  <div class="kkumz-container space-y-6">
    <!-- 꿈즈 라이브 사이트 상단 네비게이션 바 -->
    <div
      class="px-5 py-3 rounded-2xl bg-slate-900 text-white flex flex-wrap items-center justify-between gap-3 shadow-lg"
    >
      <div class="flex items-center gap-3">
        <button
          class="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-bold text-slate-300 transition"
          @click="router.push('/feed')"
        >
          ← 쇼케이스 피드
        </button>
        <span class="px-2.5 py-1 rounded-full bg-indigo-500/20 text-indigo-300 text-xs font-extrabold border border-indigo-400/30">
          꿈즈 라이브 사이트
        </span>
        <span class="text-xs font-mono text-slate-300">/s/{{ currentSlug }}</span>
        <template v-if="runtime">
          <span class="text-xs text-slate-400">·</span>
          <span class="text-xs font-bold text-emerald-400">
            {{ formatSiteTypeLabel(runtime.siteType) }} · {{ formatDeployModeLabel(runtime.deployMode) }} (v{{ runtime.activeVersionNo }})
          </span>
        </template>
      </div>

      <div class="flex flex-wrap items-center gap-2">
        <button
          v-for="s in quickSlugs"
          :key="s.slug"
          class="px-2.5 py-1 rounded-lg text-xs font-bold transition"
          :class="
            currentSlug === s.slug
              ? 'bg-indigo-600 text-white'
              : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
          "
          @click="router.push(`/s/${s.slug}`)"
        >
          {{ s.label }}
        </button>
        <button
          v-if="runtime"
          class="px-3 py-1 rounded-lg bg-white text-slate-900 text-xs font-extrabold hover:bg-slate-100"
          @click="router.push(`/studio/${runtime.ideaId}`)"
        >
          스튜디오에서 편집
        </button>
        <button
          v-if="runtime"
          class="px-3 py-1 rounded-lg bg-indigo-500 text-white text-xs font-extrabold hover:bg-indigo-400"
          @click="router.push(`/lounge/${runtime.siteSlug}`)"
        >
          팀빌딩 라운지
        </button>
      </div>
    </div>

    <div v-if="loading" class="py-20 text-center text-sm font-bold text-slate-400">
      브랜드 라이브 사이트를 렌더링하는 중입니다...
    </div>

    <!-- 실제 렌더링되는 독립 브랜드 사이트 캔버스 -->
    <div
      v-else-if="runtime"
      class="rounded-3xl overflow-hidden border border-slate-200 shadow-xl transition-all"
      :style="{
        backgroundColor: runtime.designTokens?.surfaceColor || '#ffffff',
        fontFamily: runtime.designTokens?.fontFamily || 'Pretendard, sans-serif',
      }"
    >
      <!-- 브랜드 사이트 자체 헤더 -->
      <header class="px-6 md:px-10 py-5 border-b border-slate-200/70 bg-white/90 backdrop-blur flex flex-wrap items-center justify-between gap-4">
        <div class="flex items-center gap-3">
          <div class="site-header-logo flex items-center overflow-hidden" v-html="runtime.logoSvg"></div>
          <span
            v-if="runtime.layoutSchema?.announcement"
            class="hidden sm:inline-block px-3 py-1 rounded-full text-xs font-extrabold text-white"
            :style="{ backgroundColor: runtime.designTokens?.primaryColor || '#4f46e5' }"
          >
            {{ runtime.layoutSchema.announcement }}
          </span>
        </div>

        <div class="flex items-center gap-3 text-xs font-bold text-slate-600">
          <a
            v-if="runtime.products?.length"
            href="#section-commerce"
            class="px-3 py-1.5 rounded-lg hover:bg-slate-100"
          >
            스토어 상품 ({{ runtime.products.length }})
          </a>
          <a
            v-if="runtime.articles?.length"
            href="#section-articles"
            class="px-3 py-1.5 rounded-lg hover:bg-slate-100"
          >
            브랜드 매거진 ({{ runtime.articles.length }})
          </a>
          <a
            v-if="runtime.cmsForm"
            href="#section-form"
            class="px-3 py-1.5 rounded-lg hover:bg-slate-100"
          >
            온라인 신청
          </a>
          <a href="#section-concierge" class="px-3 py-1.5 rounded-lg bg-indigo-50 text-indigo-700 font-extrabold">
            🤖 AI 브랜드 컨시어지
          </a>
        </div>
      </header>

      <!-- 히어로 배너 섹션 -->
      <section
        class="px-6 md:px-12 py-14 md:py-20 text-white relative overflow-hidden"
        :style="{
          background: `linear-gradient(135deg, ${runtime.designTokens?.primaryColor || '#1e1b4b'}, #0f172a)`,
        }"
      >
        <div class="max-w-3xl space-y-5 relative z-10">
          <div class="inline-flex items-center gap-2 px-3.5 py-1 rounded-full bg-white/15 text-xs font-bold backdrop-blur-sm">
            <span>{{ runtime.brandName }}</span>
            <span>·</span>
            <span>Official Brand Site</span>
          </div>
          <h1 class="text-3xl md:text-5xl font-extrabold leading-tight tracking-tight">
            {{ runtime.layoutSchema?.heroTitle || runtime.brandName }}
          </h1>
          <p class="text-base md:text-lg text-white/85 leading-relaxed">
            {{ runtime.layoutSchema?.heroSubtitle || runtime.tagline }}
          </p>
          <div class="pt-2 flex flex-wrap gap-3">
            <a
              v-if="runtime.products?.length"
              href="#section-commerce"
              class="px-6 py-3 rounded-xl bg-white text-slate-900 font-extrabold text-sm shadow-lg hover:bg-slate-100 transition"
            >
              {{ runtime.layoutSchema?.primaryCta || '스토어 컬렉션 살펴보기' }}
            </a>
            <a
              v-if="runtime.cmsForm"
              href="#section-form"
              class="px-6 py-3 rounded-xl bg-white/15 hover:bg-white/25 text-white font-extrabold text-sm border border-white/25 transition"
            >
              {{ runtime.cmsForm.formName }}
            </a>
          </div>
        </div>
      </section>

      <div class="p-6 md:p-12 space-y-14 bg-white">
        <!-- 1. 브랜드 스토어 상품 카탈로그 & 주문 -->
        <section v-if="runtime.products?.length" id="section-commerce" class="space-y-6">
          <div class="flex flex-col sm:flex-row sm:items-end justify-between gap-2 border-b border-slate-100 pb-4">
            <div>
              <span class="text-xs font-extrabold uppercase tracking-wider text-indigo-600">
                OFFICIAL BRAND STORE
              </span>
              <h2 class="text-2xl font-extrabold text-slate-900 mt-0.5">
                시그니처 상품 컬렉션 & 즉시 주문
              </h2>
            </div>
            <span class="text-xs text-slate-500">
              실시간 재고 확인 및 바로 주문이 가능합니다
            </span>
          </div>

          <!-- 주문 완료 영수증 배너 -->
          <div
            v-if="orderResult"
            class="p-5 rounded-2xl bg-emerald-50 border border-emerald-300 flex flex-col md:flex-row md:items-center justify-between gap-4"
          >
            <div class="space-y-1">
              <div class="flex items-center gap-2">
                <span class="px-2.5 py-0.5 rounded-full bg-emerald-600 text-white text-xs font-extrabold">
                  주문 및 결제 완료
                </span>
                <span class="text-sm font-extrabold text-emerald-950">
                  주문번호: {{ orderResult.orderSn }}
                </span>
              </div>
              <p class="text-xs text-emerald-800">
                상품: <strong>{{ orderResult.productName }}</strong> ({{ orderResult.quantity }}개) · 결제금액:
                <strong>{{ Number(orderResult.payAmount).toLocaleString() }}원</strong> · 잔여 재고:
                <strong>{{ orderResult.remainingStock }}개</strong>
              </p>
            </div>
            <button
              class="px-3 py-1.5 rounded-lg bg-white text-emerald-800 text-xs font-bold border border-emerald-200 self-start md:self-center"
              @click="orderResult = null"
            >
              확인 닫기
            </button>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
            <div
              v-for="prod in runtime.products"
              :key="prod.id"
              class="rounded-2xl border border-slate-200 overflow-hidden bg-white hover:shadow-lg transition flex flex-col justify-between"
            >
              <div>
                <div class="h-48 bg-slate-100 overflow-hidden relative">
                  <img :src="prod.pic" :alt="prod.name" class="w-full h-full object-cover" />
                  <span
                    class="absolute top-3 right-3 px-2.5 py-1 rounded-full bg-slate-900/80 text-white text-[11px] font-bold backdrop-blur-sm"
                  >
                    재고 {{ prod.stock }}개
                  </span>
                </div>
                <div class="p-4 space-y-1.5">
                  <div class="text-[11px] font-bold text-slate-400">{{ prod.productSn }}</div>
                  <h3 class="text-sm font-extrabold text-slate-900 line-clamp-2">{{ prod.name }}</h3>
                  <p class="text-xs text-slate-500 line-clamp-2">{{ prod.subTitle }}</p>
                </div>
              </div>

              <div class="p-4 pt-0 space-y-3">
                <div class="flex items-baseline justify-between border-t border-slate-100 pt-3">
                  <span class="text-xs text-slate-400">판매가</span>
                  <span class="text-lg font-extrabold text-slate-900">
                    {{ Number(prod.price).toLocaleString() }}원
                  </span>
                </div>
                <button
                  class="w-full py-2.5 rounded-xl text-white text-xs font-extrabold transition shadow-sm hover:opacity-95"
                  :style="{ backgroundColor: runtime.designTokens?.primaryColor || '#4f46e5' }"
                  @click="openOrderModal(prod)"
                >
                  즉시 구매하기
                </button>
              </div>
            </div>
          </div>
        </section>

        <!-- 2. 브랜드 스토리 & 에디토리얼 매거진 -->
        <section v-if="runtime.articles?.length" id="section-articles" class="space-y-6">
          <div class="flex flex-col sm:flex-row sm:items-end justify-between gap-2 border-b border-slate-100 pb-4">
            <div>
              <span class="text-xs font-extrabold uppercase tracking-wider text-emerald-600">
                EDITORIAL & BRAND STORY
              </span>
              <h2 class="text-2xl font-extrabold text-slate-900 mt-0.5">
                브랜드 매거진 & 커뮤니티 소식
              </h2>
            </div>
            <span class="text-xs text-slate-500">브랜드 철학과 새로운 소식을 만나보세요</span>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <article
              v-for="art in runtime.articles"
              :key="art.id"
              class="p-6 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-3 hover:border-indigo-300 transition"
            >
              <div class="flex items-center justify-between text-xs text-slate-400">
                <span class="font-bold text-indigo-600">Editor · {{ art.author }}</span>
                <span>조회 {{ art.viewCount }}</span>
              </div>
              <h3 class="text-lg font-extrabold text-slate-900">{{ art.title }}</h3>
              <p class="text-xs text-slate-600 leading-relaxed">{{ art.summary }}</p>
              <div
                class="pt-2 text-xs text-slate-700 prose max-w-none border-t border-slate-200/60"
                v-html="art.bodyHtml"
              ></div>
            </article>
          </div>
        </section>

        <!-- 3. 온라인 참가/예약 신청 & AI 브랜드 컨시어지 2단 배치 -->
        <section class="grid grid-cols-1 lg:grid-cols-12 gap-8 pt-2">
          <!-- 온라인 신청 폼 -->
          <div v-if="runtime.cmsForm" id="section-form" class="lg:col-span-6 p-6 md:p-8 rounded-3xl bg-slate-50 border border-slate-200 space-y-5">
            <div>
              <span class="px-2.5 py-1 rounded-full bg-indigo-100 text-indigo-800 text-xs font-extrabold">
                온라인 예약 · 신청 접수
              </span>
              <h3 class="text-xl font-extrabold text-slate-900 mt-2">
                {{ runtime.cmsForm.formName }}
              </h3>
              <p class="text-xs text-slate-500 mt-1">
                신청서를 작성하시면 브랜드 운영팀에게 즉시 전달됩니다.
              </p>
            </div>

            <div
              v-if="formSubmitResult"
              class="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-xs text-emerald-900 space-y-1"
            >
              <div class="font-extrabold">
                ✅ 신청이 정상 접수되었습니다! (접수번호 #{{ formSubmitResult.submissionId }})
              </div>
              <div>
                신청자: {{ formSubmitResult.applicantName }} · 총 누적 신청 {{ formSubmitResult.totalSubmissions }}건
              </div>
            </div>

            <div class="space-y-3">
              <div>
                <label class="block text-xs font-bold text-slate-700 mb-1">신청자 성함</label>
                <input
                  v-model="cmsFormInput.applicantName"
                  type="text"
                  placeholder="성함을 입력하세요"
                  class="kkumz-input text-xs bg-white"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-700 mb-1">연락처</label>
                <input
                  v-model="cmsFormInput.phone"
                  type="text"
                  placeholder="수신 가능한 휴대전화 번호 입력"
                  class="kkumz-input text-xs bg-white"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-700 mb-1">세션 / 프로그램 선택</label>
                <input
                  v-model="cmsFormInput.optionValue"
                  type="text"
                  placeholder="예: 주말 프라이빗 세션 / 5:30분 페이스 그룹"
                  class="kkumz-input text-xs bg-white"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-700 mb-1">요청 메시지</label>
                <textarea
                  v-model="cmsFormInput.message"
                  rows="2"
                  placeholder="문의사항이나 요청사항을 남겨주세요"
                  class="kkumz-input text-xs bg-white"
                ></textarea>
              </div>
              <button
                class="w-full py-3 rounded-xl text-white text-xs font-extrabold shadow-sm transition"
                :style="{ backgroundColor: runtime.designTokens?.primaryColor || '#4f46e5' }"
                :disabled="submittingForm"
                @click="handleSubmitCmsForm"
              >
                {{ submittingForm ? '신청서 접수 중...' : '온라인 신청서 제출하기' }}
              </button>
            </div>
          </div>

          <!-- AI 브랜드 컨시어지 챗봇 -->
          <div
            id="section-concierge"
            :class="runtime.cmsForm ? 'lg:col-span-6' : 'lg:col-span-12'"
            class="p-6 md:p-8 rounded-3xl bg-slate-900 text-white space-y-5 flex flex-col justify-between"
          >
            <div class="space-y-3">
              <div class="flex items-center justify-between">
                <span class="px-3 py-1 rounded-full bg-indigo-500/20 text-indigo-300 text-xs font-extrabold border border-indigo-400/30">
                  🤖 AI 브랜드 컨시어지
                </span>
                <span class="text-[11px] text-slate-400">24시간 실시간 응대</span>
              </div>
              <h3 class="text-xl font-extrabold">
                {{ runtime.brandName }}에 대해 무엇이든 물어보세요
              </h3>
              <p class="text-xs text-slate-300 leading-relaxed">
                브랜드 스토리, 판매 중인 시그니처 상품 및 재고, 커뮤니티 프로그램 안내까지 실시간으로 답변해 드립니다.
              </p>

              <div class="flex flex-wrap gap-1.5 pt-1">
                <button
                  class="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-[11px] text-slate-200 transition"
                  @click="askPreset('대표 추천 상품과 가격을 알려주세요')"
                >
                  💡 대표 추천 상품과 가격
                </button>
                <button
                  class="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-[11px] text-slate-200 transition"
                  @click="askPreset('브랜드 핵심 가치와 특징이 궁금해요')"
                >
                  ✨ 브랜드 핵심 가치
                </button>
                <button
                  class="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-[11px] text-slate-200 transition"
                  @click="askPreset('현재 참여 가능한 오프라인 클래스나 모임이 있나요?')"
                >
                  📅 클래스/모임 신청 안내
                </button>
              </div>
            </div>

            <div class="space-y-3 pt-4">
              <div
                v-if="conciergeAnswer"
                class="p-4 rounded-2xl bg-slate-800/90 border border-slate-700 text-xs text-slate-100 whitespace-pre-line leading-relaxed"
              >
                {{ conciergeAnswer }}
              </div>

              <div class="flex gap-2">
                <input
                  v-model="conciergeQuestion"
                  type="text"
                  placeholder="궁금한 내용을 입력하세요..."
                  class="flex-1 px-4 py-2.5 rounded-xl bg-slate-800 border border-slate-700 text-xs text-white placeholder-slate-400 focus:outline-none focus:border-indigo-400"
                  @keyup.enter="handleAskConcierge"
                />
                <button
                  class="px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-extrabold shrink-0"
                  :disabled="askingConcierge"
                  @click="handleAskConcierge"
                >
                  {{ askingConcierge ? '답변 중...' : '질문하기' }}
                </button>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>

    <!-- 즉시 구매 주문 모달 -->
    <div
      v-if="selectedProduct"
      class="fixed inset-0 z-50 bg-slate-950/60 backdrop-blur-sm flex items-center justify-center p-4"
    >
      <div class="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl space-y-4">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-extrabold text-slate-900">스토어 즉시 주문</h3>
          <button class="text-slate-400 hover:text-slate-700 font-bold" @click="selectedProduct = null">
            ✕
          </button>
        </div>

        <div class="p-3.5 rounded-2xl bg-slate-50 border border-slate-200 flex items-center gap-3">
          <img :src="selectedProduct.pic" class="w-14 h-14 rounded-xl object-cover" />
          <div>
            <div class="text-xs font-extrabold text-slate-900">{{ selectedProduct.name }}</div>
            <div class="text-xs text-indigo-600 font-bold mt-0.5">
              {{ Number(selectedProduct.price).toLocaleString() }}원 · 현재 재고 {{ selectedProduct.stock }}개
            </div>
          </div>
        </div>

        <div class="space-y-3">
          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="block text-xs font-bold text-slate-700 mb-1">수량</label>
              <input
                v-model.number="orderInput.quantity"
                type="number"
                min="1"
                max="10"
                class="kkumz-input text-xs py-2"
              />
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-700 mb-1">수령인 성함</label>
              <input v-model="orderInput.receiverName" type="text" class="kkumz-input text-xs py-2" />
            </div>
          </div>
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">연락처</label>
            <input v-model="orderInput.receiverPhone" type="text" class="kkumz-input text-xs py-2" />
          </div>
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">배송지 주소</label>
            <input v-model="orderInput.receiverAddress" type="text" class="kkumz-input text-xs py-2" />
          </div>
        </div>

        <div class="flex items-center justify-between pt-2 border-t border-slate-100">
          <div class="text-xs text-slate-500">
            총 결제금액:
            <strong class="text-base text-slate-900">
              {{ Number(selectedProduct.price * (orderInput.quantity || 1)).toLocaleString() }}원
            </strong>
          </div>
          <div class="flex gap-2">
            <button class="kkumz-btn-secondary text-xs" @click="selectedProduct = null">취소</button>
            <button
              class="kkumz-btn-primary text-xs"
              :disabled="placingOrder"
              @click="submitCommerceOrder"
            >
              {{ placingOrder ? '결제 처리 중...' : '결제 및 주문 확정' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { kkumzApi, formatSiteTypeLabel, formatDeployModeLabel } from '@/api/kkumzApi';

const route = useRoute();
const router = useRouter();

const quickSlugs = [
  { slug: 'matcha-atelier', label: '말차 아뜰리에' },
  { slug: 'seoul-run-club', label: '서울 나이트 런' },
  { slug: 'bean-chronicle', label: '빈 크로니클' },
  { slug: 'desk-orbit', label: '데스크 오빗' },
];

const currentSlug = computed(() => (route.params.siteSlug as string) || 'matcha-atelier');
const loading = ref(false);
const runtime = ref<any>(null);

// 주문 상태
const selectedProduct = ref<any>(null);
const placingOrder = ref(false);
const orderResult = ref<any>(null);
const orderInput = ref({
  quantity: 1,
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
});

// 온라인 신청 폼 상태
const submittingForm = ref(false);
const formSubmitResult = ref<any>(null);
const cmsFormInput = ref({
  applicantName: '',
  phone: '',
  optionValue: '',
  message: '',
});

// AI 브랜드 컨시어지 상태
const conciergeQuestion = ref('');
const conciergeAnswer = ref('');
const askingConcierge = ref(false);

async function loadRuntime() {
  loading.value = true;
  orderResult.value = null;
  formSubmitResult.value = null;
  conciergeAnswer.value = '';
  try {
    runtime.value = await kkumzApi.getSiteRuntime(currentSlug.value);
  } finally {
    loading.value = false;
  }
}

function openOrderModal(prod: any) {
  selectedProduct.value = prod;
  orderInput.value.quantity = 1;
}

async function submitCommerceOrder() {
  if (!selectedProduct.value) return;
  placingOrder.value = true;
  try {
    orderResult.value = await kkumzApi.createCommerceOrder(currentSlug.value, {
      productId: selectedProduct.value.id,
      quantity: orderInput.value.quantity || 1,
      receiverName: orderInput.value.receiverName,
      receiverPhone: orderInput.value.receiverPhone,
      receiverAddress: orderInput.value.receiverAddress,
    });
    selectedProduct.value = null;
    runtime.value = await kkumzApi.getSiteRuntime(currentSlug.value);
  } finally {
    placingOrder.value = false;
  }
}

async function handleSubmitCmsForm() {
  if (!cmsFormInput.value.applicantName.trim()) return;
  submittingForm.value = true;
  try {
    formSubmitResult.value = await kkumzApi.submitCmsForm(currentSlug.value, {
      formId: runtime.value?.cmsForm?.id,
      applicantName: cmsFormInput.value.applicantName,
      phone: cmsFormInput.value.phone,
      optionValue: cmsFormInput.value.optionValue,
      message: cmsFormInput.value.message,
    });
    runtime.value = await kkumzApi.getSiteRuntime(currentSlug.value);
  } finally {
    submittingForm.value = false;
  }
}

function askPreset(q: string) {
  conciergeQuestion.value = q;
  handleAskConcierge();
}

async function handleAskConcierge() {
  if (!conciergeQuestion.value.trim()) return;
  askingConcierge.value = true;
  try {
    const res = await kkumzApi.askSiteConcierge(currentSlug.value, conciergeQuestion.value);
    conciergeAnswer.value = res.answer;
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

onMounted(() => {
  loadRuntime();
});
</script>

<style scoped>
.site-header-logo {
  height: 44px;
  max-width: 220px;
}
.site-header-logo :deep(svg) {
  height: 100%;
  width: auto;
  max-width: 100%;
  object-fit: contain;
  display: block;
}
</style>
