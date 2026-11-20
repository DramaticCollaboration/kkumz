<template>
  <div
    v-if="open"
    class="fixed inset-0 z-50 flex justify-end bg-black/60 backdrop-blur-sm"
    data-testid="sf-slide-cart-drawer"
    @click.self="emit('close')"
  >
    <div
      class="w-full max-w-md h-full overflow-y-auto shadow-2xl flex flex-col justify-between border-l"
      :style="{
        backgroundColor: 'var(--sf-card)',
        borderColor: 'var(--sf-border)',
        color: 'var(--sf-ink)',
      }"
    >
      <div>
        <div
          class="sticky top-0 z-10 px-6 py-4 flex items-center justify-between gap-4 border-b backdrop-blur-md"
          :style="{ backgroundColor: 'var(--sf-header-bg)', borderColor: 'var(--sf-border)' }"
        >
          <div>
            <span
              class="sf-kicker block text-[10px] font-bold uppercase tracking-widest"
              :style="{ color: theme.isDark ? theme.accentColor : theme.secondaryColor }"
            >
              SHOPPING BAG
            </span>
            <strong class="sf-heading text-base font-bold">
              장바구니 ({{ totalItemCount }})
            </strong>
          </div>
          <button
            type="button"
            class="px-3 py-1.5 text-xs font-bold sf-btn-outline"
            @click="emit('close')"
          >
            닫기
          </button>
        </div>

        <div class="p-6 space-y-6">
          <div v-if="items.length === 0" class="py-16 text-center space-y-3">
            <p class="text-sm font-semibold" :style="{ color: 'var(--sf-ink-soft)' }">
              장바구니에 담긴 항목이 없습니다.
            </p>
            <button
              type="button"
              class="px-5 py-2.5 text-xs font-bold sf-btn-primary"
              @click="emit('close')"
            >
              컬렉션 계속 둘러보기
            </button>
          </div>

          <template v-else>
            <div class="space-y-3">
              <div
                v-for="(item, idx) in items"
                :key="`${item.productId}-${item.selectedOption || idx}`"
                class="sf-card-alt p-3.5 flex gap-3.5 items-start"
              >
                <div class="w-16 h-16 shrink-0 overflow-hidden border" :style="{ borderRadius: theme.radius, borderColor: 'var(--sf-border)' }">
                  <BrandImage
                    :src="item.pic"
                    :alt="item.name"
                    :brand-name="brandName"
                    :primary-color="theme.primaryColor"
                    :secondary-color="theme.secondaryColor"
                    :is-dark="theme.isDark"
                  />
                </div>
                <div class="flex-1 min-w-0 space-y-1.5">
                  <div class="flex items-start justify-between gap-2">
                    <strong class="text-xs sm:text-sm font-bold leading-snug line-clamp-2">
                      {{ item.name }}
                    </strong>
                    <button
                      type="button"
                      class="text-[11px] font-semibold shrink-0 hover:underline"
                      :style="{ color: 'var(--sf-ink-muted)' }"
                      @click="emit('remove-item', idx)"
                    >
                      삭제
                    </button>
                  </div>
                  <div v-if="item.selectedOption" class="text-[11px]" :style="{ color: 'var(--sf-ink-muted)' }">
                    옵션: {{ item.selectedOption }}
                  </div>
                  <div class="flex items-center justify-between pt-1">
                    <div class="flex items-center gap-2">
                      <button
                        type="button"
                        class="w-6 h-6 sf-btn-outline text-xs font-bold flex items-center justify-center"
                        @click="emit('update-qty', idx, Math.max(1, item.quantity - 1))"
                      >
                        -
                      </button>
                      <span class="sf-mono text-xs font-bold w-5 text-center">{{ item.quantity }}</span>
                      <button
                        type="button"
                        class="w-6 h-6 sf-btn-outline text-xs font-bold flex items-center justify-center"
                        @click="emit('update-qty', idx, item.quantity + 1)"
                      >
                        +
                      </button>
                    </div>
                    <strong class="text-sm font-extrabold" :class="theme.isDark ? 'sf-mono' : ''">
                      {{ formatKrw(item.unitPrice * item.quantity) }}
                    </strong>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="coupons.length > 0" class="sf-card-alt p-4 space-y-2.5">
              <div class="flex items-center justify-between">
                <span class="text-xs font-bold">적용 가능한 브랜드 쿠폰</span>
                <span class="text-[11px] font-bold" :style="{ color: theme.primaryColor }">
                  {{ selectedCoupon ? `-${formatKrw(discountAmount)}` : '미적용' }}
                </span>
              </div>
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="cp in coupons"
                  :key="cp.id || cp.code"
                  type="button"
                  class="px-3 py-1.5 text-[11px] font-bold border transition"
                  :style="couponBtnStyle(selectedCouponCode === String(cp.code || cp.id))"
                  @click="toggleCoupon(cp)"
                >
                  {{ cp.name }} ({{ formatKrw(cp.amount) }})
                </button>
              </div>
            </div>

            <div class="space-y-3 pt-3 border-t" :style="{ borderColor: 'var(--sf-border)' }">
              <strong class="block text-xs font-bold uppercase tracking-wider" :style="{ color: 'var(--sf-ink-soft)' }">
                주문자 · 수령 정보
              </strong>
              <div>
                <label class="block text-xs font-bold mb-1">수령인 성함 *</label>
                <input
                  v-model="receiverName"
                  type="text"
                  placeholder="성함을 입력해 주세요"
                  class="w-full px-3.5 py-2.5 text-xs border focus:outline-none"
                  :style="inputStyle"
                />
              </div>
              <div>
                <label class="block text-xs font-bold mb-1">연락처 *</label>
                <input
                  v-model="receiverPhone"
                  type="text"
                  placeholder="연락처를 입력해 주세요"
                  class="w-full px-3.5 py-2.5 text-xs border focus:outline-none"
                  :style="inputStyle"
                />
              </div>
              <div>
                <label class="block text-xs font-bold mb-1">배송지 주소 또는 이용 메모</label>
                <input
                  v-model="receiverAddress"
                  type="text"
                  placeholder="배송지 주소 또는 희망 일정을 입력해 주세요"
                  class="w-full px-3.5 py-2.5 text-xs border focus:outline-none"
                  :style="inputStyle"
                />
              </div>
            </div>
          </template>
        </div>
      </div>

      <div
        v-if="items.length > 0"
        class="sticky bottom-0 p-5 border-t space-y-3 backdrop-blur-md"
        :style="{ backgroundColor: 'var(--sf-header-bg)', borderColor: 'var(--sf-border)' }"
      >
        <div class="space-y-1.5 text-xs">
          <div class="flex justify-between" :style="{ color: 'var(--sf-ink-soft)' }">
            <span>상품 합계</span>
            <span class="sf-mono">{{ formatKrw(subtotal) }}</span>
          </div>
          <div v-if="discountAmount > 0" class="flex justify-between font-semibold" :style="{ color: theme.primaryColor }">
            <span>쿠폰 할인</span>
            <span class="sf-mono">-{{ formatKrw(discountAmount) }}</span>
          </div>
          <div class="flex justify-between items-baseline pt-2 border-t" :style="{ borderColor: 'var(--sf-border)' }">
            <span class="font-bold">최종 결제 금액</span>
            <strong class="text-xl font-extrabold" :class="theme.isDark ? 'sf-mono' : ''">
              {{ formatKrw(finalTotal) }}
            </strong>
          </div>
        </div>

        <button
          type="button"
          class="w-full py-3.5 text-xs sm:text-sm font-bold sf-btn-primary"
          :disabled="placingOrder"
          data-testid="sf-cart-checkout-btn"
          @click="submitCheckout"
        >
          {{ placingOrder ? '주문 처리 중...' : `${formatKrw(finalTotal)} 주문 확정하기` }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import BrandImage from '../BrandImage.vue';
import {
  formatKrw,
  type ResolvedThemeSpec,
  type StorefrontCartItem,
} from '../storefrontTypes';

const props = defineProps<{
  open: boolean;
  brandName: string;
  items: StorefrontCartItem[];
  coupons: Array<Record<string, any>>;
  placingOrder: boolean;
  theme: ResolvedThemeSpec;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'update-qty', index: number, quantity: number): void;
  (e: 'remove-item', index: number): void;
  (e: 'checkout', payload: {
    items: StorefrontCartItem[];
    couponCode?: string;
    discountAmount: number;
    receiverName: string;
    receiverPhone: string;
    receiverAddress: string;
  }): void;
}>();

const receiverName = ref('');
const receiverPhone = ref('');
const receiverAddress = ref('');
const selectedCouponCode = ref('');

const totalItemCount = computed(() =>
  props.items.reduce((acc, item) => acc + Number(item.quantity || 1), 0),
);

const subtotal = computed(() =>
  props.items.reduce((acc, item) => acc + Number(item.unitPrice || 0) * Number(item.quantity || 1), 0),
);

const selectedCoupon = computed(() =>
  props.coupons.find((cp) => String(cp.code || cp.id) === selectedCouponCode.value) || null,
);

const discountAmount = computed(() => {
  if (!selectedCoupon.value) return 0;
  const minPoint = Number(selectedCoupon.value.minPoint || 0);
  if (minPoint > 0 && subtotal.value < minPoint) return 0;
  return Math.min(subtotal.value, Number(selectedCoupon.value.amount || 0));
});

const finalTotal = computed(() => Math.max(0, subtotal.value - discountAmount.value));

const inputStyle = computed(() => ({
  backgroundColor: 'var(--sf-card-alt)',
  borderColor: 'var(--sf-border-strong)',
  color: 'var(--sf-ink)',
  borderRadius: props.theme.radius,
}));

function toggleCoupon(cp: Record<string, any>) {
  const code = String(cp.code || cp.id || '');
  selectedCouponCode.value = selectedCouponCode.value === code ? '' : code;
}

function couponBtnStyle(active: boolean) {
  if (active) {
    return {
      backgroundColor: props.theme.primaryColor,
      borderColor: props.theme.primaryColor,
      color: '#ffffff',
      borderRadius: props.theme.radius,
    };
  }
  return {
    backgroundColor: 'var(--sf-card)',
    borderColor: 'var(--sf-border-strong)',
    color: 'var(--sf-ink)',
    borderRadius: props.theme.radius,
  };
}

function submitCheckout() {
  if (props.items.length === 0) return;
  emit('checkout', {
    items: [...props.items],
    couponCode: selectedCouponCode.value || undefined,
    discountAmount: discountAmount.value,
    receiverName: receiverName.value.trim(),
    receiverPhone: receiverPhone.value.trim(),
    receiverAddress: receiverAddress.value.trim(),
  });
}

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen && !selectedCouponCode.value && props.coupons.length > 0) {
      selectedCouponCode.value = String(props.coupons[0].code || props.coupons[0].id || '');
    }
  },
);
</script>
