<template>
  <div
    v-if="product"
    class="fixed inset-0 z-50 flex justify-end bg-black/60 backdrop-blur-sm"
    data-testid="sf-product-detail-drawer"
    @click.self="emit('close')"
  >
    <div
      class="w-full max-w-xl h-full overflow-y-auto shadow-2xl flex flex-col justify-between border-l"
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
          <div class="min-w-0">
            <span
              class="sf-kicker block text-[10px] font-bold uppercase tracking-widest"
              :style="{ color: theme.isDark ? theme.accentColor : theme.secondaryColor }"
            >
              {{ product.productCategoryName || brandName }}
            </span>
            <strong class="sf-heading text-sm sm:text-base font-bold truncate block">
              {{ product.name }}
            </strong>
          </div>
          <button
            type="button"
            class="px-3 py-1.5 text-xs font-bold sf-btn-outline shrink-0"
            @click="emit('close')"
          >
            닫기
          </button>
        </div>

        <div class="p-6 space-y-6">
          <div class="space-y-3">
            <div class="aspect-[4/3] overflow-hidden border" :style="{ borderRadius: 'var(--sf-radius-lg)', borderColor: 'var(--sf-border)' }">
              <BrandImage
                :src="activeImage"
                :alt="product.name"
                :brand-name="brandName"
                :primary-color="theme.primaryColor"
                :secondary-color="theme.secondaryColor"
                :is-dark="theme.isDark"
              />
            </div>

            <div v-if="galleryList.length > 1" class="flex gap-2 overflow-x-auto pb-1">
              <button
                v-for="(imgUrl, idx) in galleryList"
                :key="`${imgUrl}-${idx}`"
                type="button"
                class="w-16 h-16 shrink-0 overflow-hidden border-2 transition"
                :style="{
                  borderRadius: theme.radius,
                  borderColor: activeImage === imgUrl ? theme.primaryColor : 'var(--sf-border)',
                }"
                @click="activeImage = imgUrl"
              >
                <BrandImage
                  :src="imgUrl"
                  :alt="`${product.name} ${idx + 1}`"
                  :brand-name="brandName"
                  :primary-color="theme.primaryColor"
                  :secondary-color="theme.secondaryColor"
                  :is-dark="theme.isDark"
                />
              </button>
            </div>
          </div>

          <div class="space-y-3">
            <div class="flex flex-wrap items-center gap-2">
              <span
                v-if="product.note || product.badge"
                class="px-2.5 py-0.5 text-[11px] font-bold text-white"
                :style="{ backgroundColor: theme.primaryColor, borderRadius: theme.radius }"
              >
                {{ product.note || product.badge }}
              </span>
              <span
                v-if="product.productSn"
                class="sf-mono text-[11px]"
                :style="{ color: 'var(--sf-ink-muted)' }"
              >
                SKU {{ product.productSn }}
              </span>
              <span
                v-if="product.stock !== undefined && product.stock !== null"
                class="text-xs font-semibold"
                :style="{ color: 'var(--sf-ink-soft)' }"
              >
                · 잔여 {{ activeStock }}{{ product.unit || '개' }}
              </span>
            </div>

            <h2 class="sf-heading text-2xl font-bold leading-snug">
              {{ product.name }}
            </h2>

            <p v-if="product.subTitle || product.description" class="text-sm leading-relaxed" :style="{ color: 'var(--sf-ink-soft)' }">
              {{ product.subTitle || product.description }}
            </p>

            <div class="pt-3 border-t flex items-baseline justify-between" :style="{ borderColor: 'var(--sf-border)' }">
              <span class="text-xs font-semibold" :style="{ color: 'var(--sf-ink-muted)' }">
                공식 판매가
              </span>
              <div class="text-right">
                <span
                  v-if="Number(product.originalPrice) > activePrice"
                  class="text-xs line-through mr-2"
                  :style="{ color: 'var(--sf-ink-muted)' }"
                >
                  {{ formatKrw(product.originalPrice) }}
                </span>
                <span class="text-2xl font-extrabold" :class="theme.isDark ? 'sf-mono' : ''">
                  {{ formatKrw(activePrice) }}
                </span>
                <small v-if="product.unit" class="text-xs ml-1" :style="{ color: 'var(--sf-ink-muted)' }">
                  / {{ product.unit }}
                </small>
              </div>
            </div>
          </div>

          <div v-if="selectableOptions.length" class="space-y-2.5">
            <label class="block text-xs font-bold uppercase tracking-wider" :style="{ color: 'var(--sf-ink-soft)' }">
              옵션 선택
            </label>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="opt in selectableOptions"
                :key="opt.label"
                type="button"
                class="px-3.5 py-2 text-xs font-bold border transition"
                :style="optionChipStyle(selectedOption === opt.label)"
                @click="selectOption(opt)"
              >
                {{ opt.label }}
                <span v-if="opt.price > 0 && opt.price !== basePrice" class="ml-1 opacity-75">
                  ({{ formatKrw(opt.price) }})
                </span>
              </button>
            </div>
          </div>

          <div class="sf-card-alt p-4 flex items-center justify-between gap-4">
            <span class="text-xs font-bold">수량</span>
            <div class="flex items-center gap-3">
              <button
                type="button"
                class="w-8 h-8 sf-btn-outline text-sm font-bold flex items-center justify-center"
                @click="quantity = Math.max(1, quantity - 1)"
              >
                -
              </button>
              <span class="sf-mono text-sm font-bold w-8 text-center">{{ quantity }}</span>
              <button
                type="button"
                class="w-8 h-8 sf-btn-outline text-sm font-bold flex items-center justify-center"
                @click="quantity = Math.min(maxQty, quantity + 1)"
              >
                +
              </button>
            </div>
          </div>

          <div
            v-if="product.detailHtml || product.detailDesc"
            class="pt-4 border-t space-y-3"
            :style="{ borderColor: 'var(--sf-border)' }"
          >
            <h3 class="sf-heading text-base font-bold">
              {{ product.detailTitle || '상세 안내' }}
            </h3>
            <div
              v-if="product.detailHtml"
              class="sf-prose text-xs sm:text-sm leading-relaxed space-y-2"
              :style="{ color: 'var(--sf-ink-soft)' }"
              v-html="product.detailHtml"
            ></div>
            <p v-else class="text-xs sm:text-sm leading-relaxed" :style="{ color: 'var(--sf-ink-soft)' }">
              {{ product.detailDesc }}
            </p>
          </div>
        </div>
      </div>

      <div
        class="sticky bottom-0 p-5 border-t space-y-3 backdrop-blur-md"
        :style="{ backgroundColor: 'var(--sf-header-bg)', borderColor: 'var(--sf-border)' }"
      >
        <div class="flex items-baseline justify-between text-xs">
          <span :style="{ color: 'var(--sf-ink-muted)' }">합계 금액 ({{ quantity }}건)</span>
          <strong class="text-xl font-extrabold" :class="theme.isDark ? 'sf-mono' : ''">
            {{ formatKrw(activePrice * quantity) }}
          </strong>
        </div>
        <div class="grid grid-cols-2 gap-3">
          <button
            type="button"
            class="py-3 text-xs sm:text-sm font-bold sf-btn-outline"
            data-testid="sf-detail-add-cart-btn"
            @click="handleAddToCart(false)"
          >
            장바구니 담기
          </button>
          <button
            type="button"
            class="py-3 text-xs sm:text-sm font-bold sf-btn-primary"
            data-testid="sf-detail-buy-now-btn"
            @click="handleAddToCart(true)"
          >
            바로 주문하기
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import BrandImage from '../BrandImage.vue';
import {
  displayProductPrice,
  formatKrw,
  type ResolvedThemeSpec,
  type StorefrontCartItem,
} from '../storefrontTypes';

interface OptionChoice {
  label: string;
  price: number;
  stock: number;
}

const props = defineProps<{
  product: Record<string, any> | null;
  brandName: string;
  theme: ResolvedThemeSpec;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'add-to-cart', item: StorefrontCartItem, openCartAfter: boolean): void;
}>();

const activeImage = ref('');
const selectedOption = ref('');
const activePriceOverride = ref<number | null>(null);
const activeStockOverride = ref<number | null>(null);
const quantity = ref(1);

const basePrice = computed(() => displayProductPrice(props.product));

const galleryList = computed<string[]>(() => {
  if (!props.product) return [];
  const urls: string[] = [];
  if (props.product.pic) urls.push(String(props.product.pic));
  const rawAlbum = props.product.albumPics;
  if (typeof rawAlbum === 'string' && rawAlbum.trim()) {
    rawAlbum
      .split(',')
      .map((s) => s.trim())
      .filter((s) => s && !urls.includes(s))
      .forEach((u) => urls.push(u));
  } else if (Array.isArray(rawAlbum)) {
    rawAlbum
      .map((s) => String(s || '').trim())
      .filter((s) => s && !urls.includes(s))
      .forEach((u) => urls.push(u));
  }
  return urls;
});

const selectableOptions = computed<OptionChoice[]>(() => {
  if (!props.product) return [];
  const skus = props.product.skus;
  if (Array.isArray(skus) && skus.length > 0) {
    return skus
      .map((sku: any) => {
        const label = formatSkuLabel(sku);
        return {
          label,
          price: Number(sku.promotionPrice || sku.price || basePrice.value),
          stock: Number(sku.stock ?? props.product?.stock ?? 99),
        };
      })
      .filter((o) => Boolean(o.label));
  }
  const opts = props.product.options;
  if (Array.isArray(opts) && opts.length > 0) {
    return opts.map((opt: any) => ({
      label: String(opt),
      price: basePrice.value,
      stock: Number(props.product?.stock ?? 99),
    }));
  }
  return [];
});

const activePrice = computed(() =>
  activePriceOverride.value && activePriceOverride.value > 0
    ? activePriceOverride.value
    : basePrice.value,
);

const activeStock = computed(() =>
  activeStockOverride.value !== null
    ? activeStockOverride.value
    : Number(props.product?.stock ?? 99),
);

const maxQty = computed(() => Math.max(1, Math.min(99, activeStock.value || 99)));

function formatSkuLabel(sku: Record<string, any>): string {
  const rawSp = sku.spData;
  if (typeof rawSp === 'string' && rawSp.trim()) {
    try {
      const parsed = JSON.parse(rawSp);
      if (Array.isArray(parsed)) {
        const parts = parsed
          .map((item: any) => String(item?.value || item?.key || '').trim())
          .filter(Boolean);
        if (parts.length > 0) return parts.join(' / ');
      }
    } catch {
      return rawSp.trim();
    }
  }
  return String(sku.skuCode || '').trim();
}

function selectOption(opt: OptionChoice) {
  selectedOption.value = opt.label;
  activePriceOverride.value = opt.price;
  activeStockOverride.value = opt.stock;
}

function optionChipStyle(active: boolean) {
  if (active) {
    return {
      backgroundColor: props.theme.primaryColor,
      borderColor: props.theme.primaryColor,
      color: '#ffffff',
      borderRadius: props.theme.radius,
    };
  }
  return {
    backgroundColor: 'var(--sf-card-alt)',
    borderColor: 'var(--sf-border-strong)',
    color: 'var(--sf-ink)',
    borderRadius: props.theme.radius,
  };
}

function handleAddToCart(openCartAfter: boolean) {
  if (!props.product) return;
  emit(
    'add-to-cart',
    {
      productId: props.product.id,
      name: String(props.product.name || ''),
      pic: String(props.product.pic || activeImage.value || ''),
      unitPrice: activePrice.value,
      quantity: quantity.value,
      selectedOption: selectedOption.value || undefined,
      unit: props.product.unit ? String(props.product.unit) : undefined,
      stock: activeStock.value,
    },
    openCartAfter,
  );
  emit('close');
}

watch(
  () => props.product,
  (prod) => {
    if (!prod) return;
    activeImage.value = String(prod.pic || galleryList.value[0] || '');
    quantity.value = 1;
    const firstOpt = selectableOptions.value[0];
    if (firstOpt) {
      selectOption(firstOpt);
    } else {
      selectedOption.value = '';
      activePriceOverride.value = null;
      activeStockOverride.value = null;
    }
  },
  { immediate: true },
);
</script>
