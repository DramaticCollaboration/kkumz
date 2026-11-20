<template>
  <section id="section-articles" class="space-y-8 scroll-mt-24" data-testid="sdui-editorial-feed">
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-3 pb-5 sf-section-divider">
      <div class="space-y-1.5">
        <span
          class="sf-kicker text-xs font-bold uppercase tracking-[0.16em]"
          :style="{ color: theme.isDark ? theme.accentColor : theme.secondaryColor }"
        >
          {{ copyOverrides.editorialKicker || 'EDITORIAL & BRAND JOURNAL' }}
        </span>
        <h2 class="sf-heading text-2xl md:text-3xl font-bold" :style="{ color: 'var(--sf-ink)' }">
          {{ copyOverrides.editorialTitle || `${brandName} 매거진 & 아카이브` }}
        </h2>
      </div>
      <p class="text-xs sm:text-sm" :style="{ color: 'var(--sf-ink-muted)' }">
        {{ copyOverrides.editorialSubtitle || '아티클을 클릭하면 전문과 연관 추천 구성을 함께 열람할 수 있습니다.' }}
      </p>
    </div>

    <div
      v-if="useLeadSplitLayout && leadArticle"
      class="grid grid-cols-1 lg:grid-cols-12 gap-7 items-stretch"
    >
      <article
        class="lg:col-span-7 sf-card overflow-hidden flex flex-col cursor-pointer group hover:shadow-xl transition"
        @click="emit('read-article', leadArticle)"
      >
        <div class="aspect-[16/9] overflow-hidden relative">
          <BrandImage
            :src="leadArticle.cover || leadArticle.thumbnailUrl"
            :alt="leadArticle.title"
            :brand-name="brandName"
            :primary-color="theme.primaryColor"
            :secondary-color="theme.secondaryColor"
            :is-dark="theme.isDark"
            img-class="group-hover:scale-105"
          />
          <span
            class="absolute top-4 left-4 px-3 py-1 text-[11px] font-bold text-white"
            :style="{ backgroundColor: theme.primaryColor, borderRadius: theme.radius }"
          >
            {{ leadArticle.categoryName || 'FEATURE STORY' }}
          </span>
        </div>
        <div class="p-6 md:p-8 flex-1 flex flex-col justify-between space-y-4">
          <div class="space-y-3">
            <div class="flex items-center justify-between text-xs" :style="{ color: 'var(--sf-ink-muted)' }">
              <span class="font-semibold">{{ leadArticle.author || brandName }}</span>
              <span>조회 {{ leadArticle.clicks ?? leadArticle.viewCount ?? 0 }}</span>
            </div>
            <h3 class="sf-heading text-xl md:text-2xl font-bold leading-snug" :style="{ color: 'var(--sf-ink)' }">
              {{ leadArticle.title }}
            </h3>
            <p class="text-sm leading-relaxed" :style="{ color: 'var(--sf-ink-soft)' }">
              {{ leadArticle.description || leadArticle.summary }}
            </p>
          </div>
          <div class="pt-3 border-t flex items-center justify-between text-xs font-bold" :style="{ borderColor: 'var(--sf-border)', color: theme.primaryColor }">
            <span>아티클 전문 읽기</span>
            <span aria-hidden="true">-&gt;</span>
          </div>
        </div>
      </article>

      <div class="lg:col-span-5 flex flex-col gap-6">
        <article
          v-for="art in secondaryArticles"
          :key="art.id || art.title"
          class="sf-card overflow-hidden flex-1 flex flex-col sm:flex-row lg:flex-col cursor-pointer group hover:shadow-lg transition"
          @click="emit('read-article', art)"
        >
          <div class="sm:w-44 lg:w-full aspect-[16/9] shrink-0 overflow-hidden">
            <BrandImage
              :src="art.cover || art.thumbnailUrl"
              :alt="art.title"
              :brand-name="brandName"
              :primary-color="theme.primaryColor"
              :secondary-color="theme.secondaryColor"
              :is-dark="theme.isDark"
              img-class="group-hover:scale-105"
            />
          </div>
          <div class="p-5 flex-1 flex flex-col justify-between space-y-3">
            <div class="space-y-2">
              <div class="flex items-center justify-between text-[11px]" :style="{ color: 'var(--sf-ink-muted)' }">
                <span class="font-bold" :style="{ color: theme.isDark ? theme.accentColor : theme.secondaryColor }">
                  {{ art.categoryName || 'JOURNAL' }}
                </span>
                <span>조회 {{ art.clicks ?? art.viewCount ?? 0 }}</span>
              </div>
              <h3 class="sf-heading text-base font-bold leading-snug line-clamp-2" :style="{ color: 'var(--sf-ink)' }">
                {{ art.title }}
              </h3>
              <p class="text-xs leading-relaxed line-clamp-2" :style="{ color: 'var(--sf-ink-soft)' }">
                {{ art.description || art.summary }}
              </p>
            </div>
            <span class="text-xs font-bold" :style="{ color: theme.primaryColor }">
              자세히 읽기 -&gt;
            </span>
          </div>
        </article>
      </div>
    </div>

    <div v-else class="grid gap-7" :class="standardGridClass">
      <article
        v-for="art in articles"
        :key="art.id || art.title"
        class="sf-card overflow-hidden flex flex-col cursor-pointer group hover:shadow-xl transition"
        @click="emit('read-article', art)"
      >
        <div class="aspect-[16/10] overflow-hidden relative">
          <BrandImage
            :src="art.cover || art.thumbnailUrl"
            :alt="art.title"
            :brand-name="brandName"
            :primary-color="theme.primaryColor"
            :secondary-color="theme.secondaryColor"
            :is-dark="theme.isDark"
            img-class="group-hover:scale-105"
          />
          <span
            v-if="art.categoryName"
            class="absolute top-3 left-3 px-2.5 py-1 text-[11px] font-bold text-white"
            :style="{ backgroundColor: theme.primaryColor, borderRadius: theme.radius }"
          >
            {{ art.categoryName }}
          </span>
        </div>
        <div class="p-6 flex-1 flex flex-col justify-between space-y-4">
          <div class="space-y-2.5">
            <div class="flex items-center justify-between text-xs" :style="{ color: 'var(--sf-ink-muted)' }">
              <span>{{ art.author || brandName }}</span>
              <span>조회 {{ art.clicks ?? art.viewCount ?? 0 }}</span>
            </div>
            <h3 class="sf-heading text-lg font-bold leading-snug line-clamp-2" :style="{ color: 'var(--sf-ink)' }">
              {{ art.title }}
            </h3>
            <p class="text-xs sm:text-sm leading-relaxed line-clamp-3" :style="{ color: 'var(--sf-ink-soft)' }">
              {{ art.description || art.summary }}
            </p>
          </div>
          <div class="pt-3 border-t flex items-center justify-between text-xs font-bold" :style="{ borderColor: 'var(--sf-border)', color: theme.primaryColor }">
            <span>전문 열람</span>
            <span aria-hidden="true">-&gt;</span>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import BrandImage from '../BrandImage.vue';
import {
  resolveGridColumnsClass,
  type ResolvedThemeSpec,
  type StorefrontCopyOverrides,
} from '../storefrontTypes';

const props = defineProps<{
  brandName: string;
  articles: Array<Record<string, any>>;
  copyOverrides: StorefrontCopyOverrides;
  theme: ResolvedThemeSpec;
}>();

const emit = defineEmits<{
  (e: 'read-article', article: Record<string, any>): void;
}>();

const useLeadSplitLayout = computed(
  () => props.theme.archetype === 'MAGAZINE_HYBRID' && props.articles.length >= 2,
);

const leadArticle = computed(() => props.articles[0] || null);
const secondaryArticles = computed(() => props.articles.slice(1));
const standardGridClass = computed(() => resolveGridColumnsClass(props.articles.length, 3));
</script>
