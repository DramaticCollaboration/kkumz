<template>
  <section id="section-gallery" class="space-y-12 scroll-mt-24" data-testid="sdui-space-tour">
    <div v-if="spaceTour.length" class="space-y-7">
      <div class="flex flex-col md:flex-row md:items-end justify-between gap-3 pb-5 sf-section-divider">
        <div class="space-y-1.5">
          <span
            class="sf-kicker text-xs font-bold uppercase tracking-[0.16em]"
            :style="{ color: theme.isDark ? theme.accentColor : theme.secondaryColor }"
          >
            SIGNATURE SPACE &amp; ZONE ARCHIVE
          </span>
          <h2 class="sf-heading text-2xl md:text-3xl font-bold" :style="{ color: 'var(--sf-ink)' }">
            공간 구역별 스페이스 투어 ({{ spaceTour.length }}개 존)
          </h2>
        </div>
        <p class="text-xs sm:text-sm" :style="{ color: 'var(--sf-ink-muted)' }">
          머무는 동안 경험할 핵심 공간과 시설 구역을 미리 살펴보세요.
        </p>
      </div>

      <div
        v-if="spaceTour.length === 1"
        class="sf-card overflow-hidden grid grid-cols-1 md:grid-cols-12 items-stretch"
      >
        <div class="md:col-span-7 aspect-[16/10] overflow-hidden relative">
          <BrandImage
            :src="spaceTour[0].imageUrl"
            :alt="spaceTour[0].title"
            :brand-name="brandName"
            :primary-color="theme.primaryColor"
            :secondary-color="theme.secondaryColor"
            :is-dark="theme.isDark"
          />
          <span
            class="absolute top-4 left-4 px-3 py-1 text-xs font-bold text-white"
            :style="{ backgroundColor: theme.primaryColor, borderRadius: theme.radius }"
          >
            {{ spaceTour[0].zone }}
          </span>
        </div>
        <div class="md:col-span-5 p-7 md:p-9 flex flex-col justify-center space-y-3">
          <span class="sf-mono text-[11px] font-bold uppercase tracking-wider" :style="{ color: theme.secondaryColor }">
            {{ spaceTour[0].zone }}
          </span>
          <h3 class="sf-heading text-2xl font-bold" :style="{ color: 'var(--sf-ink)' }">
            {{ spaceTour[0].title }}
          </h3>
          <p class="text-sm leading-relaxed" :style="{ color: 'var(--sf-ink-soft)' }">
            {{ spaceTour[0].desc || spaceTour[0].description }}
          </p>
        </div>
      </div>

      <div v-else class="grid gap-6" :class="spaceGridClass">
        <div
          v-for="(zone, zIdx) in spaceTour"
          :key="zIdx"
          class="sf-card overflow-hidden flex flex-col hover:shadow-lg transition"
        >
          <div class="aspect-[4/3] overflow-hidden relative">
            <BrandImage
              :src="zone.imageUrl"
              :alt="zone.title"
              :brand-name="brandName"
              :primary-color="theme.primaryColor"
              :secondary-color="theme.secondaryColor"
              :is-dark="theme.isDark"
            />
            <span
              class="absolute top-3 left-3 px-2.5 py-1 text-[11px] font-bold text-white"
              :style="{ backgroundColor: theme.primaryColor, borderRadius: theme.radius }"
            >
              {{ zone.zone }}
            </span>
          </div>
          <div class="p-5 space-y-2 flex-1">
            <h3 class="sf-heading text-base font-bold" :style="{ color: 'var(--sf-ink)' }">
              {{ zone.title }}
            </h3>
            <p class="text-xs leading-relaxed" :style="{ color: 'var(--sf-ink-soft)' }">
              {{ zone.desc || zone.description }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showPhotoGallery && galleryImages.length" class="space-y-7">
      <div class="flex flex-col md:flex-row md:items-end justify-between gap-3 pb-5 sf-section-divider">
        <div class="space-y-1.5">
          <span
            class="sf-kicker text-xs font-bold uppercase tracking-[0.16em]"
            :style="{ color: theme.isDark ? theme.accentColor : theme.secondaryColor }"
          >
            PHOTO GALLERY ARCHIVE
          </span>
          <h2 class="sf-heading text-2xl md:text-3xl font-bold" :style="{ color: 'var(--sf-ink)' }">
            현장 포토 갤러리 ({{ galleryImages.length }}장)
          </h2>
        </div>
        <span class="text-xs" :style="{ color: 'var(--sf-ink-muted)' }">
          사진을 클릭하면 고해상도 원본으로 확대해 볼 수 있습니다.
        </span>
      </div>

      <div class="grid gap-5" :class="galleryGridClass">
        <div
          v-for="(gImg, gIdx) in galleryImages"
          :key="gImg.id || gIdx"
          class="group sf-card overflow-hidden cursor-pointer hover:shadow-xl transition"
          @click="emit('open-lightbox', gImg)"
        >
          <div class="aspect-[16/10] overflow-hidden">
            <BrandImage
              :src="gImg.fileUrl || gImg.url"
              :alt="gImg.caption || gImg.originalName"
              :brand-name="brandName"
              :primary-color="theme.primaryColor"
              :secondary-color="theme.secondaryColor"
              :is-dark="theme.isDark"
              img-class="group-hover:scale-105"
            />
          </div>
          <div class="p-4 flex items-center justify-between gap-2">
            <strong class="text-xs sm:text-sm font-bold truncate" :style="{ color: 'var(--sf-ink)' }">
              {{ gImg.caption || gImg.originalName }}
            </strong>
            <span class="text-xs font-bold shrink-0" :style="{ color: theme.primaryColor }">
              확대
            </span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import BrandImage from '../BrandImage.vue';
import { resolveGridColumnsClass, type ResolvedThemeSpec } from '../storefrontTypes';

const props = defineProps<{
  brandName: string;
  spaceTour: Array<Record<string, any>>;
  galleryImages: Array<Record<string, any>>;
  showPhotoGallery: boolean;
  theme: ResolvedThemeSpec;
}>();

const emit = defineEmits<{
  (e: 'open-lightbox', img: Record<string, any>): void;
}>();

const spaceGridClass = computed(() => resolveGridColumnsClass(props.spaceTour.length, 4));
const galleryGridClass = computed(() => resolveGridColumnsClass(props.galleryImages.length, 3));
</script>
