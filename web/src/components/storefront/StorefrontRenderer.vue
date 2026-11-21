<template>
  <div
    id="top"
    class="min-h-screen w-full flex flex-col relative"
    :class="theme.rootClasses"
    :style="theme.cssVars"
    :data-theme-archetype="theme.archetype"
    :data-surface-mode="theme.surfaceMode"
    data-testid="storefront-renderer-root"
  >
    <component :is="'style'" v-if="sanitizedCustomCss">
      {{ sanitizedCustomCss }}
    </component>

    <StorefrontHeader
      :brand-name="brandName"
      :logo-svg="runtime.logoSvg"
      :custom-badge="showFlashDeal ? customBadge : ''"
      :primary-coupon="showFlashDeal ? primaryCoupon : null"
      :nav-links="navLinks"
      :has-commerce="products.length > 0"
      :cart-count="cartTotalCount"
      :concierge-btn-label="conciergeBtnLabel"
      :theme="theme"
      :preview-mode="previewMode"
      @open-cart="cartOpen = true"
      @open-concierge="conciergeOpen = true"
      @focus-section="onPreviewFocus('VISUAL_THEME')"
    />

    <div
      :class="previewMode ? 'cursor-pointer' : ''"
      @click="previewMode && onPreviewFocus('VISUAL_THEME')"
    >
      <component
        :is="heroComponent"
        :brand-name="brandName"
        :category-label="categoryLabel"
        :custom-badge="customBadge"
        :hero-title="heroTitle"
        :hero-subtitle="heroSubtitle"
        :hero-banner-image="heroBannerImage"
        :hero-banner-caption="heroBannerCaption"
        :primary-cta-text="primaryCtaText"
        :primary-cta-href="primaryCtaHref"
        :secondary-cta-text="secondaryCtaText"
        :secondary-cta-href="secondaryCtaHref"
        :trust-metrics="trustMetrics"
        :theme="theme"
      />
    </div>

    <main class="flex-1 w-full">
      <div class="max-w-7xl mx-auto px-5 md:px-10 py-14 md:py-20 space-y-20 md:space-y-24">
        <template v-for="sectionKey in orderedSections" :key="sectionKey">
          <div
            v-if="sectionKey === 'CATALOG_GRID'"
            :class="previewMode ? 'cursor-pointer' : ''"
            @click="previewMode && onPreviewFocus('CATALOG_EDIT')"
          >
            <CatalogGridSection
              :brand-name="brandName"
              :products="products"
              :categories="commerceCategories"
              :order-result="orderResult"
              :copy-overrides="copyOverrides"
              :theme="theme"
              @inspect-product="handleInspectProduct"
              @add-to-cart="handleQuickAddToCart"
              @dismiss-order-result="emit('dismiss-order-result')"
            />
          </div>

          <div
            v-else-if="sectionKey === 'SPEC_COMPARISON' && specComparison"
            :class="previewMode ? 'cursor-pointer' : ''"
            @click="previewMode && onPreviewFocus('CATALOG_EDIT')"
          >
            <SpecComparisonSection
              :spec-comparison="specComparison"
              :copy-overrides="copyOverrides"
              :theme="theme"
            />
          </div>

          <div
            v-else-if="sectionKey === 'SUBSCRIPTION_TIERS'"
            :class="previewMode ? 'cursor-pointer' : ''"
            @click="previewMode && onPreviewFocus('CATALOG_EDIT')"
          >
            <SubscriptionTiersSection
              :tiers="subscriptionTiers"
              :copy-overrides="copyOverrides"
              :theme="theme"
              @select-plan="handleSelectPlan"
            />
          </div>

          <div
            v-else-if="sectionKey === 'SESSION_TIMETABLE'"
            :class="previewMode ? 'cursor-pointer' : ''"
            @click="previewMode && onPreviewFocus('CATALOG_EDIT')"
          >
            <SessionTimetableSection
              :sessions="sessionTimetable"
              :copy-overrides="copyOverrides"
              :theme="theme"
              @select-session="handleSelectSession"
            />
          </div>

          <div
            v-else-if="sectionKey === 'PRICING_MATRIX' && pricingMatrix"
            :class="previewMode ? 'cursor-pointer' : ''"
            @click="previewMode && onPreviewFocus('CATALOG_EDIT')"
          >
            <PricingMatrixSection
              :pricing-matrix="pricingMatrix"
              :copy-overrides="copyOverrides"
              :theme="theme"
            />
          </div>

          <div
            v-else-if="sectionKey === 'SPACE_TOUR_GALLERY'"
            :class="previewMode ? 'cursor-pointer' : ''"
            @click="previewMode && onPreviewFocus('CATALOG_EDIT')"
          >
            <SpaceTourGallerySection
              :brand-name="brandName"
              :space-tour="spaceTour"
              :gallery-images="galleryImages"
              :show-photo-gallery="showPhotoGallery"
              :theme="theme"
              @open-lightbox="(img) => (selectedLightboxImage = img)"
            />
          </div>

          <div
            v-else-if="sectionKey === 'STEP_GUIDE'"
            :class="previewMode ? 'cursor-pointer' : ''"
            @click="previewMode && onPreviewFocus('CATALOG_EDIT')"
          >
            <StepGuideSection
              :brand-name="brandName"
              :step-guide="stepGuide"
              :copy-overrides="copyOverrides"
              :theme="theme"
            />
          </div>

          <div
            v-else-if="sectionKey === 'EDITORIAL_FEED'"
            :class="previewMode ? 'cursor-pointer' : ''"
            @click="previewMode && onPreviewFocus('CATALOG_EDIT')"
          >
            <EditorialFeedSection
              :brand-name="brandName"
              :articles="articles"
              :copy-overrides="copyOverrides"
              :theme="theme"
              @read-article="(art) => (selectedArticle = art)"
            />
          </div>

          <div
            v-else-if="sectionKey === 'REVIEWS_POLL'"
            :class="previewMode ? 'cursor-pointer' : ''"
            @click="previewMode && onPreviewFocus('CATALOG_EDIT')"
          >
            <ReviewsPollSection
              :reviews="reviews"
              :poll-widget="pollWidget"
              :copy-overrides="copyOverrides"
              :theme="theme"
            />
          </div>

          <div
            v-else-if="sectionKey === 'FAQ_LOCATION'"
            :class="previewMode ? 'cursor-pointer' : ''"
            @click="previewMode && onPreviewFocus('FORM_EDIT')"
          >
            <FaqLocationPolicySection
              :faq-items="faqItems"
              :location-guide="locationGuide"
              :policy-rules="policyRules"
              :downloadable-docs="downloadableDocs"
              :copy-overrides="copyOverrides"
              :theme="theme"
            />
          </div>

          <div
            v-else-if="sectionKey === 'DYNAMIC_FORM'"
            :class="previewMode ? 'cursor-pointer' : ''"
            @click="previewMode && onPreviewFocus('FORM_EDIT')"
          >
            <DynamicFormSection
              :cms-form="cmsForm"
              :recruits="recruits"
              :prefill-message="formPrefillMessage"
              :submitting="Boolean(submittingForm)"
              :form-submit-result="formSubmitResult"
              :copy-overrides="copyOverrides"
              :theme="theme"
              @submit-form="(payload) => emit('submit-form', payload)"
            />
          </div>
        </template>

        <FloatingConciergeWidget
          v-if="showAiConcierge && previewMode"
          :open="true"
          :preview-mode="true"
          :brand-name="brandName"
          :greeting="conciergeGreeting"
          :messages="conciergeMessages || []"
          :asking="Boolean(askingConcierge)"
          :copy-overrides="copyOverrides"
          :theme="theme"
          @toggle="() => {}"
          @ask="(q) => emit('ask-concierge', q)"
        />
      </div>
    </main>

    <footer
      class="w-full border-t py-12 px-5 md:px-10"
      :style="{
        backgroundColor: theme.isDark ? '#050507' : '#0f172a',
        borderColor: 'rgba(255,255,255,0.1)',
        color: '#e2e8f0',
      }"
    >
      <div class="max-w-7xl mx-auto flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
        <div class="space-y-2">
          <div class="flex items-center gap-3">
            <strong class="sf-heading text-lg font-bold text-white">{{ brandName }}</strong>
            <span class="px-2 py-0.5 rounded bg-white/10 text-[10px] sf-mono uppercase text-white/75">
              {{ categoryLabel }}
            </span>
          </div>
          <p v-if="tagline" class="text-xs text-slate-400 max-w-xl leading-relaxed">
            {{ tagline }}
          </p>
          <p v-if="footerAddressText" class="text-[11px] text-slate-500">
            {{ footerAddressText }}
          </p>
        </div>

        <div class="text-left md:text-right space-y-1 text-xs text-slate-400">
          <div class="sf-mono text-[11px] text-slate-300">
            {{ runtime.customDomain || `/s/${runtime.siteSlug || ''}` }}
          </div>
          <div>© {{ currentYear }} {{ brandName }}. All rights reserved.</div>
        </div>
      </div>
    </footer>

    <FloatingConciergeWidget
      v-if="showAiConcierge && !previewMode"
      :open="conciergeOpen"
      :preview-mode="false"
      :brand-name="brandName"
      :greeting="conciergeGreeting"
      :messages="conciergeMessages || []"
      :asking="Boolean(askingConcierge)"
      :copy-overrides="copyOverrides"
      :theme="theme"
      @toggle="(v) => (conciergeOpen = v)"
      @ask="(q) => emit('ask-concierge', q)"
    />

    <ProductDetailDrawer
      v-if="!previewMode"
      :product="inspectedProduct"
      :brand-name="brandName"
      :theme="theme"
      @close="inspectedProduct = null"
      @add-to-cart="handleAddCartItem"
    />

    <SlideCartDrawer
      v-if="!previewMode"
      :open="cartOpen"
      :brand-name="brandName"
      :items="cartItems"
      :coupons="coupons"
      :placing-order="Boolean(placingOrder)"
      :theme="theme"
      @close="cartOpen = false"
      @update-qty="handleUpdateCartQty"
      @remove-item="handleRemoveCartItem"
      @checkout="handleCheckout"
    />

    <ArticleReaderModal
      v-if="!previewMode"
      :article="selectedArticle"
      :brand-name="brandName"
      :products="products"
      :theme="theme"
      @close="selectedArticle = null"
      @inspect-product="(prod) => (inspectedProduct = prod)"
    />

    <div
      v-if="selectedLightboxImage && !previewMode"
      class="fixed inset-0 z-50 bg-black/85 backdrop-blur-sm flex items-center justify-center p-4"
      @click.self="selectedLightboxImage = null"
    >
      <div class="bg-zinc-950 rounded-2xl max-w-4xl w-full overflow-hidden border border-white/15 shadow-2xl">
        <div class="flex items-center justify-between px-6 py-4 border-b border-white/10 text-white">
          <strong class="text-sm font-bold truncate">
            {{ selectedLightboxImage.caption || selectedLightboxImage.originalName }}
          </strong>
          <button
            type="button"
            class="px-3 py-1 rounded-lg bg-white/10 hover:bg-white/20 text-xs font-bold"
            @click="selectedLightboxImage = null"
          >
            닫기
          </button>
        </div>
        <div class="p-4 flex items-center justify-center bg-black">
          <img
            :src="selectedLightboxImage.fileUrl"
            :alt="selectedLightboxImage.caption || selectedLightboxImage.originalName"
            class="max-h-[75vh] w-auto object-contain rounded-xl"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { KkumzEntity } from '@/types/kkumz';
import { ref, computed, watch } from 'vue';
import './storefront-themes.css';
import {
  displayProductPrice,
  resolveOrderedSections,
  resolveThemeSpec,
  type SectionKey,
  type StorefrontCartItem,
  type StorefrontCopyOverrides,
} from './storefrontTypes';
import StorefrontHeader, { type StorefrontNavLink } from './StorefrontHeader.vue';
import HeroEditorialStory from './heroes/HeroEditorialStory.vue';
import HeroPromoCommerce from './heroes/HeroPromoCommerce.vue';
import HeroSplitVisual from './heroes/HeroSplitVisual.vue';
import HeroFullScenic from './heroes/HeroFullScenic.vue';
import CatalogGridSection from './sections/CatalogGridSection.vue';
import SpecComparisonSection from './sections/SpecComparisonSection.vue';
import SubscriptionTiersSection from './sections/SubscriptionTiersSection.vue';
import SessionTimetableSection from './sections/SessionTimetableSection.vue';
import PricingMatrixSection from './sections/PricingMatrixSection.vue';
import SpaceTourGallerySection from './sections/SpaceTourGallerySection.vue';
import StepGuideSection from './sections/StepGuideSection.vue';
import EditorialFeedSection from './sections/EditorialFeedSection.vue';
import ReviewsPollSection from './sections/ReviewsPollSection.vue';
import FaqLocationPolicySection from './sections/FaqLocationPolicySection.vue';
import DynamicFormSection from './sections/DynamicFormSection.vue';
import ProductDetailDrawer from './modals/ProductDetailDrawer.vue';
import SlideCartDrawer from './modals/SlideCartDrawer.vue';
import ArticleReaderModal from './modals/ArticleReaderModal.vue';
import FloatingConciergeWidget, { type ConciergeMessage } from './modals/FloatingConciergeWidget.vue';

const props = defineProps<{
  runtime: KkumzEntity;
  previewMode?: boolean;
  placingOrder?: boolean;
  orderResult?: KkumzEntity | null;
  submittingForm?: boolean;
  formSubmitResult?: KkumzEntity | null;
  conciergeMessages?: ConciergeMessage[];
  askingConcierge?: boolean;
}>();

const emit = defineEmits<{
  (e: 'checkout', payload: {
    items: StorefrontCartItem[];
    couponCode?: string;
    discountAmount?: number;
    receiverName: string;
    receiverPhone: string;
    receiverAddress: string;
  }): void;
  (e: 'dismiss-order-result'): void;
  (e: 'submit-form', payload: {
    formId?: string | number;
    applicantName: string;
    phone: string;
    optionValue: string;
    message: string;
    extraFields: KkumzEntity;
  }): void;
  (e: 'ask-concierge', question: string): void;
  (e: 'focus-tab', tab: 'AI_CHAT' | 'VISUAL_THEME' | 'CATALOG_EDIT' | 'FORM_EDIT'): void;
}>();

const currentYear = new Date().getFullYear();

const theme = computed(() => resolveThemeSpec(props.runtime));

const heroComponent = computed(() => {
  const style = theme.value.heroStyle;
  if (style === 'PROMO_COMMERCE') return HeroPromoCommerce;
  if (style === 'SPLIT_VISUAL') return HeroSplitVisual;
  if (style === 'FULL_SCENIC') return HeroFullScenic;
  return HeroEditorialStory;
});

const sanitizedCustomCss = computed(() => {
  const raw = String(props.runtime?.customCss || '').trim();
  if (!raw) return '';
  return raw.replace(/<\/?style[^>]*>/gi, '').replace(/<\/?script[^>]*>/gi, '');
});

const brandName = computed(
  () => String(props.runtime?.brandName || props.runtime?.siteName || '공식 브랜드 스토어'),
);
const tagline = computed(
  () => String(props.runtime?.tagline || props.runtime?.brandSlogan || ''),
);
const categoryLabel = computed(
  () => String(props.runtime?.categoryLabel || props.runtime?.siteName || 'Official Brand'),
);
const layoutSchema = computed<Record<string, any>>(() => props.runtime?.layoutSchema || {});
const copyOverrides = computed<StorefrontCopyOverrides>(
  () => (layoutSchema.value?.copyOverrides || {}) as StorefrontCopyOverrides,
);

const customBadge = computed(() =>
  String(layoutSchema.value?.customBadge || layoutSchema.value?.announcement || ''),
);
const heroTitle = computed(() => String(layoutSchema.value?.heroTitle || brandName.value));
const heroSubtitle = computed(() => String(layoutSchema.value?.heroSubtitle || tagline.value));

const showFlashDeal = computed(() => layoutSchema.value?.showFlashDeal !== false);
const showPhotoGallery = computed(() => layoutSchema.value?.showPhotoGallery !== false);
const showCmsArticles = computed(() => layoutSchema.value?.showCmsArticles !== false);
const showCmsForm = computed(() => layoutSchema.value?.showCmsForm !== false);
const showAiConcierge = computed(() => layoutSchema.value?.showAiConcierge !== false);

const products = computed<Array<Record<string, any>>>(() => {
  if (Array.isArray(props.runtime?.products) && props.runtime.products.length > 0) {
    return props.runtime.products;
  }
  if (Array.isArray(props.runtime?.commerce?.products)) {
    return props.runtime.commerce.products;
  }
  return [];
});

const commerceCategories = computed<Array<Record<string, any>>>(() => {
  if (Array.isArray(props.runtime?.categories) && props.runtime.categories.length > 0) {
    return props.runtime.categories;
  }
  if (Array.isArray(props.runtime?.commerce?.categories)) {
    return props.runtime.commerce.categories;
  }
  return [];
});

const coupons = computed<Array<Record<string, any>>>(() => {
  const list = props.runtime?.coupons || props.runtime?.commerce?.coupons || [];
  return Array.isArray(list) ? list : [];
});
const primaryCoupon = computed(() => (coupons.value.length > 0 ? coupons.value[0] : null));

const articles = computed<Array<Record<string, any>>>(() => {
  if (Array.isArray(props.runtime?.articles) && props.runtime.articles.length > 0) {
    return props.runtime.articles;
  }
  if (Array.isArray(props.runtime?.cms?.articles)) {
    return props.runtime.cms.articles;
  }
  return [];
});

const cmsForm = computed<Record<string, any> | null>(() => {
  if (layoutSchema.value?.customCmsForm) return layoutSchema.value.customCmsForm;
  if (props.runtime?.cmsForm) return props.runtime.cmsForm;
  const forms = props.runtime?.cms?.forms;
  return Array.isArray(forms) && forms.length > 0 ? forms[0] : null;
});

const recruits = computed<Array<Record<string, any>>>(() => {
  const list = props.runtime?.recruits || [];
  return Array.isArray(list) ? list : [];
});
const openRecruits = computed(() =>
  recruits.value.filter((r) => String(r.status || 'OPEN').toUpperCase() === 'OPEN'),
);

const galleryImages = computed<Array<Record<string, any>>>(() => {
  const list = props.runtime?.galleryImages || layoutSchema.value?.galleryImages || [];
  return Array.isArray(list) ? list : [];
});

const downloadableDocs = computed<Array<Record<string, any>>>(() => {
  const list = props.runtime?.downloadableDocs || layoutSchema.value?.downloadableDocs || [];
  return Array.isArray(list) ? list : [];
});

const featureBlocks = computed<Record<string, any>>(
  () => props.runtime?.featureBlocks || layoutSchema.value?.featureBlocks || {},
);

const trustMetrics = computed<Array<Record<string, any>>>(() =>
  Array.isArray(featureBlocks.value?.trustMetrics) ? featureBlocks.value.trustMetrics : [],
);
const specComparison = computed<Record<string, any> | null>(
  () => featureBlocks.value?.specComparison || null,
);
const subscriptionTiers = computed<Array<Record<string, any>>>(() =>
  Array.isArray(featureBlocks.value?.subscriptionTiers) ? featureBlocks.value.subscriptionTiers : [],
);
const sessionTimetable = computed<Array<Record<string, any>>>(() =>
  Array.isArray(featureBlocks.value?.sessionTimetable) ? featureBlocks.value.sessionTimetable : [],
);
const pricingMatrix = computed<Record<string, any> | null>(
  () => featureBlocks.value?.pricingMatrix || null,
);
const spaceTour = computed<Array<Record<string, any>>>(() =>
  Array.isArray(featureBlocks.value?.spaceTour) ? featureBlocks.value.spaceTour : [],
);
const stepGuide = computed<Array<Record<string, any>>>(() =>
  Array.isArray(featureBlocks.value?.stepGuide) ? featureBlocks.value.stepGuide : [],
);
const reviews = computed<Array<Record<string, any>>>(() =>
  Array.isArray(featureBlocks.value?.reviews) ? featureBlocks.value.reviews : [],
);
const pollWidget = computed<Record<string, any> | null>(
  () => featureBlocks.value?.pollWidget || null,
);
const faqItems = computed<Array<Record<string, any>>>(() =>
  Array.isArray(featureBlocks.value?.faqItems) ? featureBlocks.value.faqItems : [],
);
const locationGuide = computed<Record<string, any> | null>(
  () => featureBlocks.value?.locationGuide || null,
);
const policyRules = computed<string[]>(() =>
  Array.isArray(featureBlocks.value?.policyRules) ? featureBlocks.value.policyRules : [],
);

const heroBannerImage = computed<string>(() => {
  if (layoutSchema.value?.heroBannerImage) return String(layoutSchema.value.heroBannerImage);
  const shopBanners = props.runtime?.commerce?.banners;
  if (Array.isArray(shopBanners) && shopBanners.length > 0 && shopBanners[0].pic) {
    return String(shopBanners[0].pic);
  }
  const cmsBanners = props.runtime?.cms?.banners;
  if (Array.isArray(cmsBanners) && cmsBanners.length > 0 && cmsBanners[0].imageUrl) {
    return String(cmsBanners[0].imageUrl);
  }
  return '';
});

const heroBannerCaption = computed<{ title: string; sub: string } | null>(() => {
  const rawCap = layoutSchema.value?.heroBannerCaption;
  if (rawCap) {
    if (typeof rawCap === 'object') {
      return {
        title: String(rawCap.title || ''),
        sub: String(rawCap.sub || brandName.value),
      };
    }
    return { title: String(rawCap), sub: brandName.value };
  }
  const shopBanners = props.runtime?.commerce?.banners;
  if (Array.isArray(shopBanners) && shopBanners.length > 0) {
    return { title: String(shopBanners[0].name || ''), sub: String(shopBanners[0].note || '') };
  }
  const cmsBanners = props.runtime?.cms?.banners;
  if (Array.isArray(cmsBanners) && cmsBanners.length > 0) {
    return {
      title: String(cmsBanners[0].title || ''),
      sub: String(cmsBanners[0].subTitle || ''),
    };
  }
  return null;
});

const sectionAvailability = computed<Record<SectionKey, boolean>>(() => ({
  CATALOG_GRID: products.value.length > 0,
  SPEC_COMPARISON: Boolean(Array.isArray(specComparison.value?.rows) && specComparison.value.rows.length > 0),
  SUBSCRIPTION_TIERS: subscriptionTiers.value.length > 0,
  SESSION_TIMETABLE: sessionTimetable.value.length > 0,
  PRICING_MATRIX: Boolean(Array.isArray(pricingMatrix.value?.rows) && pricingMatrix.value.rows.length > 0),
  SPACE_TOUR_GALLERY:
    spaceTour.value.length > 0 || (showPhotoGallery.value && galleryImages.value.length > 0),
  STEP_GUIDE: stepGuide.value.length > 0,
  EDITORIAL_FEED: showCmsArticles.value && articles.value.length > 0,
  REVIEWS_POLL: reviews.value.length > 0 || Boolean(pollWidget.value),
  FAQ_LOCATION:
    faqItems.value.length > 0 ||
    Boolean(locationGuide.value) ||
    policyRules.value.length > 0 ||
    downloadableDocs.value.length > 0,
  DYNAMIC_FORM: showCmsForm.value && (Boolean(cmsForm.value) || openRecruits.value.length > 0),
}));

const orderedSections = computed<SectionKey[]>(() =>
  resolveOrderedSections(
    theme.value.archetype,
    layoutSchema.value?.sectionOrder,
    sectionAvailability.value,
  ),
);

function buildSectionNavLink(key: SectionKey): StorefrontNavLink | null {
  const c = copyOverrides.value;
  switch (key) {
    case 'CATALOG_GRID':
      return { href: '#section-commerce', label: c.navCatalogLabel || `컬렉션 (${products.value.length})` };
    case 'SPEC_COMPARISON':
      return { href: '#section-spec', label: c.navSpecLabel || '스펙 비교' };
    case 'SUBSCRIPTION_TIERS':
      return { href: '#section-subscription', label: c.navSubscriptionLabel || '정기 구독' };
    case 'SESSION_TIMETABLE':
      return { href: '#section-timetable', label: c.navTimetableLabel || '정기 일정' };
    case 'PRICING_MATRIX':
      return { href: '#section-pricing', label: c.navPricingLabel || '요금 안내' };
    case 'SPACE_TOUR_GALLERY':
      return { href: '#section-gallery', label: c.navGalleryLabel || '공간·갤러리' };
    case 'EDITORIAL_FEED':
      return { href: '#section-articles', label: c.navEditorialLabel || '저널' };
    case 'FAQ_LOCATION':
      return { href: '#section-faq-location', label: c.navFaqLabel || '안내·FAQ' };
    case 'DYNAMIC_FORM':
      return { href: '#section-form', label: c.navFormLabel || '예약·신청' };
    default:
      return null;
  }
}

const navLinks = computed<StorefrontNavLink[]>(() => {
  const links: StorefrontNavLink[] = [];
  for (const key of orderedSections.value) {
    const link = buildSectionNavLink(key);
    if (link) links.push(link);
  }
  return links.slice(0, 6);
});

const primaryCtaText = computed(() =>
  String(layoutSchema.value?.primaryCta || layoutSchema.value?.ctaText || '컬렉션 살펴보기'),
);

const primaryCtaHref = computed(() => {
  const first = orderedSections.value[0];
  const link = first ? buildSectionNavLink(first) : null;
  return link ? link.href : '#section-commerce';
});

const secondaryCtaMeta = computed<{ text: string; href: string }>(() => {
  if (sectionAvailability.value.DYNAMIC_FORM && primaryCtaHref.value !== '#section-form') {
    return {
      text: String(cmsForm.value?.title || copyOverrides.value.navFormLabel || '온라인 신청'),
      href: '#section-form',
    };
  }
  if (sectionAvailability.value.EDITORIAL_FEED && primaryCtaHref.value !== '#section-articles') {
    return {
      text: copyOverrides.value.navEditorialLabel || '브랜드 저널 읽기',
      href: '#section-articles',
    };
  }
  if (sectionAvailability.value.SPEC_COMPARISON && primaryCtaHref.value !== '#section-spec') {
    return {
      text: copyOverrides.value.navSpecLabel || '스펙 비교 보기',
      href: '#section-spec',
    };
  }
  return { text: '', href: '' };
});

const secondaryCtaText = computed(() => secondaryCtaMeta.value.text);
const secondaryCtaHref = computed(() => secondaryCtaMeta.value.href);

const conciergeGreeting = computed(() =>
  String(layoutSchema.value?.conciergeGreeting || copyOverrides.value.conciergeSubtitle || ''),
);
const conciergeBtnLabel = computed(() =>
  theme.value.isDark ? 'AI CONCIERGE' : 'AI 컨시어지',
);

const footerAddressText = computed(() => {
  const addr = props.runtime?.commerce?.companyAddress;
  if (addr && (addr.province || addr.detailAddress)) {
    return [addr.addressName, addr.province, addr.city, addr.region, addr.detailAddress, addr.phone]
      .filter(Boolean)
      .join(' · ');
  }
  if (locationGuide.value?.address) {
    return String(locationGuide.value.address);
  }
  return '';
});

// Interactive State
const inspectedProduct = ref<Record<string, any> | null>(null);
const selectedArticle = ref<Record<string, any> | null>(null);
const selectedLightboxImage = ref<Record<string, any> | null>(null);
const cartOpen = ref(false);
const conciergeOpen = ref(false);
const cartItems = ref<StorefrontCartItem[]>([]);
const formPrefillMessage = ref('');

const cartTotalCount = computed(() =>
  cartItems.value.reduce((acc, item) => acc + Number(item.quantity || 1), 0),
);

function onPreviewFocus(tab: 'AI_CHAT' | 'VISUAL_THEME' | 'CATALOG_EDIT' | 'FORM_EDIT') {
  if (props.previewMode) {
    emit('focus-tab', tab);
  }
}

function handleInspectProduct(prod: KkumzEntity) {
  if (props.previewMode) {
    emit('focus-tab', 'CATALOG_EDIT');
    return;
  }
  inspectedProduct.value = prod;
}

function handleQuickAddToCart(prod: KkumzEntity) {
  if (props.previewMode) {
    emit('focus-tab', 'CATALOG_EDIT');
    return;
  }
  const firstOpt =
    Array.isArray(prod.options) && prod.options.length > 0 ? String(prod.options[0]) : undefined;
  handleAddCartItem(
    {
      productId: Number(prod.id || 0),
      name: String(prod.name || ''),
      pic: String(prod.pic || ''),
      unitPrice: displayProductPrice(prod),
      quantity: 1,
      selectedOption: firstOpt,
      unit: prod.unit ? String(prod.unit) : undefined,
      stock: Number(prod.stock ?? 99),
    },
    true,
  );
}

function handleAddCartItem(item: StorefrontCartItem, openCartAfter: boolean) {
  const existing = cartItems.value.find(
    (c) => c.productId === item.productId && (c.selectedOption || '') === (item.selectedOption || ''),
  );
  if (existing) {
    existing.quantity += item.quantity;
  } else {
    cartItems.value.push({ ...item });
  }
  if (openCartAfter) {
    cartOpen.value = true;
  }
}

function handleUpdateCartQty(index: number, quantity: number) {
  if (cartItems.value[index]) {
    cartItems.value[index].quantity = Math.max(1, quantity);
  }
}

function handleRemoveCartItem(index: number) {
  cartItems.value.splice(index, 1);
}

function handleCheckout(payload: {
  items: StorefrontCartItem[];
  couponCode?: string;
  discountAmount?: number;
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
}) {
  emit('checkout', payload);
}

function scrollToFormOrConcierge(messageText: string) {
  if (props.previewMode) return;
  if (sectionAvailability.value.DYNAMIC_FORM) {
    formPrefillMessage.value = messageText;
    const el = document.getElementById('section-form');
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  } else {
    conciergeOpen.value = true;
    emit('ask-concierge', messageText);
  }
}

function handleSelectSession(session: KkumzEntity) {
  const text = `[세션 참가 신청] ${session.day || ''} ${session.time || ''} ${session.title || ''} (${session.pace || ''})`.trim();
  scrollToFormOrConcierge(text);
}

function handleSelectPlan(plan: KkumzEntity) {
  const text = `[정기 구독 신청] ${plan.tier || ''} (${plan.price || ''}) 구독 안내를 요청합니다.`.trim();
  scrollToFormOrConcierge(text);
}

watch(
  () => props.orderResult,
  (res) => {
    if (res) {
      cartItems.value = [];
      cartOpen.value = false;
    }
  },
);
</script>
