<template>
  <div
    class="brand-img-shell relative overflow-hidden w-full h-full select-none"
    :class="containerClass"
    :style="shellStyle"
  >
    <div
      v-if="src && !hasError && !loaded"
      class="absolute inset-0 animate-pulse"
      :style="{ backgroundColor: isDark ? 'rgba(255,255,255,0.06)' : 'rgba(15,23,42,0.06)' }"
    ></div>

    <img
      v-if="src && !hasError"
      :src="src"
      :alt="alt || brandName || ''"
      :loading="eager ? 'eager' : 'lazy'"
      decoding="async"
      class="w-full h-full object-cover transition duration-500"
      :class="[imgClass, loaded ? 'opacity-100' : 'opacity-0']"
      @load="onLoad"
      @error="onError"
    />

    <div
      v-else
      class="w-full h-full flex flex-col items-center justify-center p-6 text-center relative overflow-hidden"
      :style="fallbackStyle"
    >
      <div
        class="w-12 h-12 rounded-full flex items-center justify-center font-extrabold text-lg tracking-tight mb-2.5 border"
        :style="{
          backgroundColor: 'rgba(255,255,255,0.14)',
          borderColor: 'rgba(255,255,255,0.28)',
          color: '#ffffff',
        }"
      >
        {{ monogram }}
      </div>
      <span class="text-xs font-bold text-white/90 tracking-wide line-clamp-2 max-w-[16rem]">
        {{ fallbackCaption }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';

const props = withDefaults(
  defineProps<{
    src?: string | null;
    alt?: string;
    brandName?: string;
    primaryColor?: string;
    secondaryColor?: string;
    isDark?: boolean;
    eager?: boolean;
    containerClass?: string;
    imgClass?: string;
  }>(),
  {
    src: '',
    alt: '',
    brandName: 'BRAND',
    primaryColor: '#14532d',
    secondaryColor: '#0f172a',
    isDark: false,
    eager: false,
    containerClass: '',
    imgClass: '',
  },
);

const hasError = ref(false);
const loaded = ref(false);

watch(
  () => props.src,
  () => {
    hasError.value = false;
    loaded.value = false;
  },
);

function onLoad() {
  loaded.value = true;
}

function onError() {
  hasError.value = true;
  loaded.value = true;
}

const monogram = computed(() => {
  const source = (props.brandName || props.alt || 'K').trim();
  return source.slice(0, 2).toUpperCase();
});

const fallbackCaption = computed(() => {
  return props.alt || props.brandName || 'Official Brand Visual';
});

const shellStyle = computed(() => ({
  backgroundColor: props.isDark ? '#18181b' : '#e2e8f0',
}));

const fallbackStyle = computed(() => ({
  background: `linear-gradient(135deg, ${props.primaryColor || '#14532d'} 0%, ${props.secondaryColor || '#0f172a'} 100%)`,
}));
</script>
