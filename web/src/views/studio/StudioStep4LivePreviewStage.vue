<template>
  <div class="preview-stage">
    <div class="preview-click-tip">
      실제 배포 사이트(/s/{{ siteRuntime.siteSlug }})와 동일한 100% WYSIWYG 프리뷰입니다. 각 영역을 클릭하면 좌측 편집기가 해당 탭으로 전환됩니다.
    </div>

    <div
      class="preview-frame unified-storefront-frame"
      :class="viewportMode.toLowerCase()"
    >
      <StorefrontRenderer
        :runtime="livePreviewRuntime"
        :preview-mode="true"
        @focus-tab="(tab) => emit('focus-tab', tab)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import StorefrontRenderer from '@/components/storefront/StorefrontRenderer.vue';
import { renderLogoWithBrand } from './studioFormatters';

const props = defineProps<{
  viewportMode: 'DESKTOP' | 'TABLET' | 'MOBILE';
  visualForm: Record<string, any>;
  siteRuntime: any;
  previewBrandName: string;
  previewHeroStyle: Record<string, string>;
  previewProducts: any[];
  previewCategories: any[];
  previewGalleryImages: any[];
  previewArticles: any[];
  previewDownloadableDocs: any[];
  previewFeatureBlocks: Record<string, any>;
  editableCmsForm: {
    title: string;
    description: string;
    submitText: string;
    conciergeGreeting: string;
  };
}>();

const emit = defineEmits<{
  (e: 'focus-tab', tab: 'AI_CHAT' | 'VISUAL_THEME' | 'CATALOG_EDIT' | 'FORM_EDIT'): void;
}>();

const livePreviewRuntime = computed<Record<string, any>>(() => {
  const base = props.siteRuntime || {};
  const vf = props.visualForm || {};
  const baseLayout = base.layoutSchema || {};
  const baseTokens = base.designTokens || {};
  const resolvedSiteName = vf.siteName || base.siteName || props.previewBrandName;
  const resolvedLogoSvg = renderLogoWithBrand(base.logoSvg, resolvedSiteName, props.previewBrandName);
  const existingForm = baseLayout.customCmsForm || base.cmsForm || base.cms?.forms?.[0] || null;
  const mergedForm = existingForm
    ? {
        ...existingForm,
        title: props.editableCmsForm.title || existingForm.title || existingForm.formName,
        formName: props.editableCmsForm.title || existingForm.formName || existingForm.title,
        description: props.editableCmsForm.description || existingForm.description,
        submitText: props.editableCmsForm.submitText || existingForm.submitText,
      }
    : null;

  return {
    ...base,
    siteName: resolvedSiteName,
    brandName: resolvedSiteName,
    brandSlogan: vf.brandSlogan || base.brandSlogan || base.tagline || '',
    tagline: vf.brandSlogan || base.tagline || base.brandSlogan || '',
    logoSvg: resolvedLogoSvg,
    themeArchetype: vf.themeArchetype || base.themeArchetype || baseLayout.themeArchetype,
    heroStyle: vf.heroStyle || base.heroStyle || baseLayout.heroStyle,
    surfaceMode: vf.surfaceMode || base.surfaceMode || baseTokens.surfaceMode,
    fontPreset: vf.fontPreset || base.fontPreset || baseTokens.fontPreset,
    products: props.previewProducts,
    categories: props.previewCategories,
    articles: props.previewArticles,
    galleryImages: props.previewGalleryImages,
    downloadableDocs: props.previewDownloadableDocs,
    featureBlocks: props.previewFeatureBlocks,
    cmsForm: mergedForm,
    designTokens: {
      ...baseTokens,
      primary: vf.primaryColor || baseTokens.primary,
      primaryColor: vf.primaryColor || baseTokens.primaryColor,
      secondary: vf.secondaryColor || baseTokens.secondary,
      secondaryColor: vf.secondaryColor || baseTokens.secondaryColor,
      accent: vf.accentColor || baseTokens.accent,
      accentColor: vf.accentColor || baseTokens.accentColor,
      surface: vf.surfaceColor || baseTokens.surface,
      surfaceColor: vf.surfaceColor || baseTokens.surfaceColor,
      radius: vf.radius || baseTokens.radius,
      surfaceMode: vf.surfaceMode || baseTokens.surfaceMode,
      fontPreset: vf.fontPreset || baseTokens.fontPreset,
    },
    layoutSchema: {
      ...baseLayout,
      themeArchetype: vf.themeArchetype || baseLayout.themeArchetype,
      heroStyle: vf.heroStyle || baseLayout.heroStyle,
      customBadge: vf.customBadge ?? baseLayout.customBadge,
      heroTitle: vf.heroTitle || baseLayout.heroTitle || resolvedSiteName,
      heroSubtitle: vf.heroSubtitle || baseLayout.heroSubtitle || vf.brandSlogan,
      ctaText: vf.ctaText || baseLayout.ctaText,
      primaryCta: vf.ctaText || baseLayout.primaryCta,
      heroBannerImage: vf.heroBannerImage ?? baseLayout.heroBannerImage,
      heroBannerCaption: vf.heroBannerCaption || baseLayout.heroBannerCaption,
      showFlashDeal: vf.showFlashDeal,
      showPhotoGallery: vf.showPhotoGallery,
      showCmsArticles: vf.showCmsArticles,
      showCmsForm: vf.showCmsForm,
      showAiConcierge: vf.showAiConcierge,
      conciergeGreeting: props.editableCmsForm.conciergeGreeting || baseLayout.conciergeGreeting,
      customCmsForm: mergedForm,
    },
  };
});
</script>

<style scoped src="./studio-view.css"></style>
<style scoped>
.unified-storefront-frame {
  padding: 0;
  overflow-x: hidden;
  overflow-y: auto;
  max-height: 78vh;
}
</style>
