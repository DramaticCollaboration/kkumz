<template>
  <div
    v-if="article"
    class="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4"
    data-testid="sf-article-reader-modal"
    @click.self="emit('close')"
  >
    <article
      class="w-full max-w-3xl max-h-[88vh] overflow-y-auto shadow-2xl border"
      :style="{
        backgroundColor: 'var(--sf-card)',
        borderColor: 'var(--sf-border)',
        color: 'var(--sf-ink)',
        borderRadius: 'var(--sf-radius-lg)',
      }"
    >
      <div
        class="sticky top-0 z-10 px-6 py-4 flex items-center justify-between gap-4 border-b backdrop-blur-md"
        :style="{ backgroundColor: 'var(--sf-header-bg)', borderColor: 'var(--sf-border)' }"
      >
        <div class="flex items-center gap-2 min-w-0">
          <span
            v-if="article.categoryName"
            class="px-2.5 py-0.5 text-[11px] font-bold text-white shrink-0"
            :style="{ backgroundColor: theme.primaryColor, borderRadius: theme.radius }"
          >
            {{ article.categoryName }}
          </span>
          <span class="text-xs truncate" :style="{ color: 'var(--sf-ink-muted)' }">
            {{ article.author || brandName }} · 조회 {{ article.clicks ?? article.viewCount ?? 0 }}
          </span>
        </div>
        <button
          type="button"
          class="px-3 py-1.5 text-xs font-bold sf-btn-outline shrink-0"
          @click="emit('close')"
        >
          닫기
        </button>
      </div>

      <div v-if="article.cover || article.thumbnailUrl" class="aspect-[16/9] w-full overflow-hidden">
        <BrandImage
          :src="article.cover || article.thumbnailUrl"
          :alt="article.title"
          :brand-name="brandName"
          :primary-color="theme.primaryColor"
          :secondary-color="theme.secondaryColor"
          :is-dark="theme.isDark"
        />
      </div>

      <div class="p-6 md:p-10 space-y-6">
        <div class="space-y-3">
          <h2 class="sf-heading text-2xl md:text-3xl font-bold leading-snug">
            {{ article.title }}
          </h2>
          <p
            v-if="article.description || article.summary"
            class="text-sm sm:text-base leading-relaxed font-medium"
            :style="{ color: 'var(--sf-ink-soft)' }"
          >
            {{ article.description || article.summary }}
          </p>
        </div>

        <div
          v-if="article.bodyHtml || article.contentHtml"
          class="sf-prose pt-6 border-t text-sm sm:text-base leading-loose space-y-4"
          :style="{ borderColor: 'var(--sf-border)', color: 'var(--sf-ink-soft)' }"
          v-html="article.bodyHtml || article.contentHtml"
        ></div>

        <div
          v-if="relatedProducts.length > 0"
          class="pt-8 border-t space-y-4"
          :style="{ borderColor: 'var(--sf-border)' }"
        >
          <div class="flex items-center justify-between">
            <strong class="sf-heading text-base font-bold">
              기사 속 추천 컬렉션
            </strong>
            <span class="text-xs" :style="{ color: 'var(--sf-ink-muted)' }">
              클릭하여 바로 주문할 수 있습니다
            </span>
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
            <button
              v-for="prod in relatedProducts"
              :key="prod.id"
              type="button"
              class="sf-card-alt p-3 flex items-center gap-3 text-left hover:opacity-90 transition"
              @click="handleInspectRelated(prod)"
            >
              <div class="w-14 h-14 shrink-0 overflow-hidden border" :style="{ borderRadius: theme.radius, borderColor: 'var(--sf-border)' }">
                <BrandImage
                  :src="prod.pic"
                  :alt="prod.name"
                  :brand-name="brandName"
                  :primary-color="theme.primaryColor"
                  :secondary-color="theme.secondaryColor"
                  :is-dark="theme.isDark"
                />
              </div>
              <div class="min-w-0 flex-1">
                <strong class="block text-xs font-bold truncate">{{ prod.name }}</strong>
                <span class="text-xs font-extrabold" :style="{ color: theme.primaryColor }">
                  {{ formatKrw(displayProductPrice(prod)) }}
                </span>
              </div>
            </button>
          </div>
        </div>
      </div>
    </article>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import BrandImage from '../BrandImage.vue';
import {
  displayProductPrice,
  formatKrw,
  type ResolvedThemeSpec,
} from '../storefrontTypes';

const props = defineProps<{
  article: Record<string, any> | null;
  brandName: string;
  products: Array<Record<string, any>>;
  theme: ResolvedThemeSpec;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'inspect-product', product: Record<string, any>): void;
}>();

const relatedProducts = computed(() => (props.products || []).slice(0, 2));

function handleInspectRelated(prod: Record<string, any>) {
  emit('close');
  emit('inspect-product', prod);
}
</script>
