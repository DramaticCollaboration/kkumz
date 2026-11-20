<template>
  <section id="section-commerce" class="space-y-8 scroll-mt-24" data-testid="sdui-catalog-grid">
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-4 pb-5 sf-section-divider">
      <div class="space-y-1.5">
        <span
          class="sf-kicker text-xs font-bold uppercase tracking-[0.16em]"
          :style="{ color: theme.isDark ? theme.accentColor : theme.secondaryColor }"
        >
          {{ copyOverrides.catalogKicker || 'OFFICIAL COLLECTION' }}
        </span>
        <h2 class="sf-heading text-2xl md:text-3xl font-bold" :style="{ color: 'var(--sf-ink)' }">
          {{ copyOverrides.catalogTitle || `${brandName} 시그니처 컬렉션` }}
        </h2>
        <p v-if="copyOverrides.catalogSubtitle" class="text-xs sm:text-sm" :style="{ color: 'var(--sf-ink-soft)' }">
          {{ copyOverrides.catalogSubtitle }}
        </p>
      </div>

      <div v-if="categories.length" class="flex flex-wrap gap-2">
        <button
          type="button"
          class="px-3.5 py-1.5 text-xs font-bold border transition"
          :style="chipStyle(selectedCategoryId === null)"
          @click="selectedCategoryId = null"
        >
          전체 ({{ products.length }})
        </button>
        <button
          v-for="cat in categories"
          :key="cat.id"
          type="button"
          class="px-3.5 py-1.5 text-xs font-bold border transition"
          :style="chipStyle(selectedCategoryId === cat.id)"
          @click="selectedCategoryId = cat.id"
        >
          {{ cat.name }}
        </button>
      </div>
    </div>

    <div
      v-if="orderResult"
      class="p-5 rounded-2xl border flex flex-col md:flex-row md:items-center justify-between gap-4"
      :style="{
        backgroundColor: theme.isDark ? 'rgba(16, 185, 129, 0.12)' : '#ecfdf5',
        borderColor: theme.isDark ? 'rgba(52, 211, 153, 0.35)' : '#6ee7b7',
      }"
    >
      <div class="space-y-1">
        <div class="flex items-center gap-2">
          <span class="px-2.5 py-0.5 rounded-full bg-emerald-600 text-white text-[11px] font-bold">
            주문 · 예약 완료
          </span>
          <span class="text-sm font-bold" :style="{ color: 'var(--sf-ink)' }">
            주문번호 {{ orderResult.orderSn }}
          </span>
        </div>
        <p class="text-xs" :style="{ color: 'var(--sf-ink-soft)' }">
          항목: <strong>{{ orderResult.productName }}</strong> ({{ orderResult.quantity }}건) · 결제금액
          <strong>{{ formatKrw(orderResult.payAmount) }}</strong>
        </p>
      </div>
      <button
        type="button"
        class="px-3.5 py-1.5 text-xs font-bold sf-btn-outline self-start md:self-center"
        @click="emit('dismiss-order-result')"
      >
        확인 닫기
      </button>
    </div>

    <div class="grid gap-6" :class="gridClass">
      <article
        v-for="prod in filteredProducts"
        :key="prod.id"
        class="group sf-card overflow-hidden flex flex-col justify-between transition duration-200 hover:-translate-y-1 hover:shadow-xl"
      >
        <div>
          <div
            class="aspect-[4/3] relative overflow-hidden cursor-pointer"
            @click="emit('inspect-product', prod)"
          >
            <BrandImage
              :src="prod.pic"
              :alt="prod.name"
              :brand-name="brandName"
              :primary-color="theme.primaryColor"
              :secondary-color="theme.secondaryColor"
              :is-dark="theme.isDark"
              img-class="group-hover:scale-105"
            />
            <span
              v-if="prod.note || prod.badge"
              class="absolute top-3 left-3 px-2.5 py-1 text-[11px] font-bold text-white shadow-sm"
              :style="{ backgroundColor: theme.primaryColor, borderRadius: theme.radius }"
            >
              {{ prod.note || prod.badge }}
            </span>
            <span
              v-if="prod.stock !== undefined && prod.stock !== null"
              class="absolute top-3 right-3 px-2.5 py-1 rounded-full bg-black/70 text-white text-[11px] font-semibold backdrop-blur-sm"
            >
              잔여 {{ prod.stock }}{{ prod.unit || '개' }}
            </span>
          </div>

          <div class="p-5 space-y-2.5">
            <div class="flex items-center justify-between text-[11px] font-semibold" :style="{ color: 'var(--sf-ink-muted)' }">
              <span :class="theme.isDark ? 'sf-mono uppercase' : ''">
                {{ prod.productCategoryName || prod.productSn }}
              </span>
              <span v-if="prod.sale">누적 {{ prod.sale }}건</span>
            </div>

            <h3
              class="sf-heading text-base font-bold leading-snug line-clamp-2 cursor-pointer hover:opacity-80 transition"
              :style="{ color: 'var(--sf-ink)' }"
              @click="emit('inspect-product', prod)"
            >
              {{ prod.name }}
            </h3>

            <p class="text-xs leading-relaxed line-clamp-2" :style="{ color: 'var(--sf-ink-soft)' }">
              {{ prod.subTitle || prod.description }}
            </p>

            <div v-if="Array.isArray(prod.options) && prod.options.length" class="flex flex-wrap gap-1.5 pt-1">
              <span
                v-for="opt in prod.options.slice(0, 3)"
                :key="opt"
                class="px-2 py-0.5 text-[10px] font-semibold border"
                :style="{
                  backgroundColor: 'var(--sf-card-alt)',
                  borderColor: 'var(--sf-border)',
                  color: 'var(--sf-ink-soft)',
                  borderRadius: '4px',
                }"
              >
                {{ opt }}
              </span>
            </div>
          </div>
        </div>

        <div class="p-5 pt-0 space-y-3.5">
          <div class="flex items-baseline justify-between pt-3.5 border-t" :style="{ borderColor: 'var(--sf-border)' }">
            <span class="text-[11px] font-semibold" :style="{ color: 'var(--sf-ink-muted)' }">
              {{ copyOverrides.productPriceLabel || '공식 판매가' }}
            </span>
            <div class="text-right">
              <span
                v-if="Number(prod.originalPrice) > displayProductPrice(prod)"
                class="text-xs line-through mr-1.5"
                :style="{ color: 'var(--sf-ink-muted)' }"
              >
                {{ formatKrw(prod.originalPrice) }}
              </span>
              <span
                class="text-lg font-extrabold"
                :class="theme.isDark ? 'sf-mono' : ''"
                :style="{ color: 'var(--sf-ink)' }"
              >
                {{ formatKrw(displayProductPrice(prod)) }}
              </span>
              <small v-if="prod.unit" class="text-[11px] ml-0.5" :style="{ color: 'var(--sf-ink-muted)' }">
                / {{ prod.unit }}
              </small>
            </div>
          </div>

          <div class="grid grid-cols-5 gap-2">
            <button
              type="button"
              class="col-span-2 py-2.5 text-xs font-bold sf-btn-outline"
              @click="emit('add-to-cart', prod)"
            >
              담기
            </button>
            <button
              type="button"
              class="col-span-3 py-2.5 text-xs font-bold sf-btn-primary"
              @click="emit('inspect-product', prod)"
            >
              {{ copyOverrides.productCtaLabel || '상세 보기 · 주문' }}
            </button>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import BrandImage from '../BrandImage.vue';
import {
  displayProductPrice,
  formatKrw,
  resolveGridColumnsClass,
  type ResolvedThemeSpec,
  type StorefrontCopyOverrides,
} from '../storefrontTypes';

const props = defineProps<{
  brandName: string;
  products: Array<Record<string, any>>;
  categories: Array<Record<string, any>>;
  orderResult?: Record<string, any> | null;
  copyOverrides: StorefrontCopyOverrides;
  theme: ResolvedThemeSpec;
}>();

const emit = defineEmits<{
  (e: 'inspect-product', product: Record<string, any>): void;
  (e: 'add-to-cart', product: Record<string, any>): void;
  (e: 'dismiss-order-result'): void;
}>();

const selectedCategoryId = ref<number | string | null>(null);

const filteredProducts = computed(() => {
  if (selectedCategoryId.value === null) return props.products;
  const cat = props.categories.find((c) => c.id === selectedCategoryId.value);
  const matched = props.products.filter(
    (p) =>
      p.productCategoryId === selectedCategoryId.value ||
      (cat && p.productCategoryName === cat.name),
  );
  return matched.length > 0 ? matched : props.products;
});

const maxCols = computed<3 | 4>(() =>
  props.theme.archetype === 'EDITORIAL_BOTANICAL' || props.theme.archetype === 'SCENIC_HOSPITALITY' ? 3 : 4,
);

const gridClass = computed(() => resolveGridColumnsClass(filteredProducts.value.length, maxCols.value));

function chipStyle(active: boolean) {
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
    borderColor: 'var(--sf-border)',
    color: 'var(--sf-ink-soft)',
    borderRadius: props.theme.radius,
  };
}
</script>
