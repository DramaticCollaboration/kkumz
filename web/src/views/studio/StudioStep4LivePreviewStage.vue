<template>
  <div class="preview-stage">
    <div class="preview-click-tip">
      프리뷰의 각 영역을 클릭하면 좌측 편집기가 해당 탭으로 자동 전환됩니다.
    </div>
    <div
      class="preview-frame"
      :class="viewportMode.toLowerCase()"
      :style="{ background: visualForm.surfaceColor || siteRuntime.designTokens?.surface || '#f8fafc' }"
    >
      <div class="preview-gnb clickable-preview-sec" @click="emit('focus-tab', 'VISUAL_THEME')">
        <div class="preview-gnb-left">
          <div
            class="frame-logo"
            v-html="renderLogoWithBrand(siteRuntime.logoSvg, visualForm.siteName || siteRuntime.siteName, previewBrandName)"
          ></div>
          <strong class="gnb-brand-title">{{ visualForm.siteName || siteRuntime.siteName }}</strong>
        </div>
        <div class="preview-gnb-links">
          <span v-if="previewProducts.length">스토어 · {{ previewProducts.length }}</span>
          <span v-if="visualForm.showPhotoGallery && previewGalleryImages.length">포토갤러리 · {{ previewGalleryImages.length }}</span>
          <span v-if="visualForm.showCmsArticles && previewArticles.length">매거진 · {{ previewArticles.length }}</span>
          <span v-if="previewDownloadableDocs.length">안내자료 · {{ previewDownloadableDocs.length }}</span>
          <span v-if="visualForm.showCmsForm">예약/신청</span>
          <span class="gnb-live-pill">/s/{{ siteRuntime.siteSlug }}</span>
        </div>
      </div>

      <div
        class="frame-hero clickable-preview-sec"
        :style="previewHeroStyle"
        @click="emit('focus-tab', 'VISUAL_THEME')"
      >
        <div class="frame-top">
          <span class="frame-badge">{{ visualForm.customBadge || siteRuntime.layoutSchema?.customBadge || '공식 브랜드 쇼케이스' }}</span>
          <span v-if="visualForm.showFlashDeal" class="flash-pill">첫 구매·방문 웰컴 혜택 적용 중</span>
        </div>
        <h3>{{ visualForm.heroTitle || siteRuntime.layoutSchema?.heroTitle || siteRuntime.siteName }}</h3>
        <p>{{ visualForm.heroSubtitle || siteRuntime.layoutSchema?.heroSubtitle || siteRuntime.brandSlogan }}</p>
        <div class="hero-cta-row">
          <span class="hero-cta-btn" :style="{ borderRadius: visualForm.radius || '12px' }">
            {{ visualForm.ctaText || siteRuntime.layoutSchema?.ctaText || '시그니처 컬렉션 살펴보기' }} →
          </span>
          <span v-if="visualForm.showAiConcierge" class="hero-concierge-pill">
            AI 브랜드 컨시어지 상시 대기
          </span>
        </div>
      </div>

      <div
        v-if="previewProducts.length"
        class="frame-section clickable-preview-sec"
        @click="emit('focus-tab', 'CATALOG_EDIT')"
      >
        <div class="frame-sec-head">
          <div>
            <span class="sec-kicker">SIGNATURE SHOP CATALOG · 클릭하여 편집</span>
            <h4>시그니처 상품 카탈로그 · {{ previewProducts.length }}종</h4>
          </div>
          <div class="cat-pills">
            <span class="cat-pill active">전체</span>
            <span
              v-for="cat in previewCategories.slice(0, 3)"
              :key="cat.id || cat.name"
              class="cat-pill"
            >
              {{ cat.name }}
            </span>
          </div>
        </div>
        <div class="mini-cards">
          <div
            v-for="p in previewProducts.slice(0, 4)"
            :key="p.id"
            class="mini-card"
            :style="{ borderRadius: visualForm.radius || '12px' }"
          >
            <div class="mini-card-img-wrap">
              <img v-if="p.pic" :src="p.pic" :alt="p.name" />
              <span v-if="p.productCategoryName" class="prod-cat-tag">{{ p.productCategoryName }}</span>
            </div>
            <div class="mini-card-body">
              <strong>{{ p.name }}</strong>
              <p class="prod-sub">{{ p.subTitle }}</p>
              <div class="prod-price-row">
                <span class="prod-price" :style="{ color: visualForm.primaryColor || siteRuntime.designTokens?.primary || '#0f172a' }">
                  {{ Number(p.promotionPrice > 0 ? p.promotionPrice : (p.price > 0 ? p.price : 120000)).toLocaleString() }}원
                  <small v-if="p.unit" style="font-size: 10px; color: #64748b">/ {{ p.unit }}</small>
                </span>
                <span class="prod-buy-chip">바로 예약·구매</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div
        v-if="previewFeatureBlocks.pricingMatrix?.rows?.length"
        class="frame-section clickable-preview-sec"
        @click="emit('focus-tab', 'CATALOG_EDIT')"
      >
        <div class="frame-sec-head">
          <div>
            <span class="sec-kicker">SDUI MATRIX_TABLE · SHOP_PRICING_MATRIX</span>
            <h4>{{ previewFeatureBlocks.pricingMatrix.title || '시즌 · 요일 · 인원별 상세 요금표' }}</h4>
          </div>
        </div>
        <div class="mini-pricing-table-wrap">
          <table class="mini-pricing-table">
            <thead>
              <tr>
                <th v-for="col in previewFeatureBlocks.pricingMatrix.columns || []" :key="col">{{ col }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, rIdx) in previewFeatureBlocks.pricingMatrix.rows" :key="rIdx">
                <td><strong>{{ row.tier }}</strong></td>
                <td>{{ row.capacity }}</td>
                <td>{{ row.offPeakWeekday }}</td>
                <td>{{ row.offPeakWeekend }}</td>
                <td>{{ row.peakSeason }}</td>
                <td>{{ row.note }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div
        v-if="previewFeatureBlocks.spaceTour?.length"
        class="frame-section clickable-preview-sec"
        @click="emit('focus-tab', 'CATALOG_EDIT')"
      >
        <div class="frame-sec-head">
          <div>
            <span class="sec-kicker">SDUI SPACE_TOUR · CMS_SPACE_TOUR</span>
            <h4>공간 구역별 스페이스 투어 · {{ previewFeatureBlocks.spaceTour.length }}개 존</h4>
          </div>
        </div>
        <div class="mini-space-tour-grid">
          <div
            v-for="(zone, zIdx) in previewFeatureBlocks.spaceTour"
            :key="zIdx"
            class="mini-space-zone-card"
          >
            <span class="zone-badge">{{ zone.zone }}</span>
            <strong>{{ zone.title }}</strong>
            <p>{{ zone.desc }}</p>
          </div>
        </div>
      </div>

      <div
        v-if="visualForm.showPhotoGallery && previewGalleryImages.length"
        class="frame-section clickable-preview-sec"
        @click="emit('focus-tab', 'CATALOG_EDIT')"
      >
        <div class="frame-sec-head">
          <div>
            <span class="sec-kicker">SPACE &amp; PHOTO GALLERY ARCHIVE · 클릭하여 편집</span>
            <h4>현장 포토 갤러리 · {{ previewGalleryImages.length }}장</h4>
          </div>
        </div>
        <div class="mini-gallery-grid">
          <div
            v-for="(gImg, gIdx) in previewGalleryImages.slice(0, 6)"
            :key="gImg.id || gIdx"
            class="mini-gallery-card"
          >
            <img :src="gImg.fileUrl" :alt="gImg.caption || gImg.originalName" />
            <span>{{ gImg.caption || gImg.originalName }}</span>
          </div>
        </div>
      </div>

      <div
        v-if="previewFeatureBlocks.stepGuide?.length"
        class="frame-section clickable-preview-sec"
        @click="emit('focus-tab', 'CATALOG_EDIT')"
      >
        <div class="frame-sec-head">
          <div>
            <span class="sec-kicker">SDUI TIMELINE_STEPS · CMS_STEP_GUIDE</span>
            <h4>단계별 이용 &amp; 추천 코스 가이드</h4>
          </div>
        </div>
        <div class="mini-step-guide-grid">
          <div
            v-for="(st, sIdx) in previewFeatureBlocks.stepGuide"
            :key="sIdx"
            class="mini-step-card"
          >
            <span class="step-num-pill">{{ st.step }}</span>
            <strong>{{ st.title }}</strong>
            <p>{{ st.desc }}</p>
          </div>
        </div>
      </div>

      <div
        v-if="visualForm.showCmsArticles && previewArticles.length"
        class="frame-section clickable-preview-sec"
        @click="emit('focus-tab', 'CATALOG_EDIT')"
      >
        <div class="frame-sec-head">
          <div>
            <span class="sec-kicker">EDITORIAL ARCHIVE &amp; JOURNAL · 클릭하여 편집</span>
            <h4>브랜드 매거진 &amp; 커뮤니티 소식 · {{ previewArticles.length }}건</h4>
          </div>
        </div>
        <div class="mini-articles">
          <div v-for="a in previewArticles.slice(0, 2)" :key="a.id" class="mini-article">
            <img v-if="a.cover" :src="a.cover" :alt="a.title" class="mini-art-thumb" />
            <div class="mini-art-content">
              <span class="art-badge">{{ a.categoryName || '브랜드 저널' }}</span>
              <strong>{{ a.title }}</strong>
              <p>{{ a.description || a.summary }}</p>
            </div>
          </div>
        </div>
      </div>

      <div
        v-if="previewDownloadableDocs.length"
        class="frame-section clickable-preview-sec"
        @click="emit('focus-tab', 'CATALOG_EDIT')"
      >
        <div class="frame-sec-head">
          <div>
            <span class="sec-kicker">OFFICIAL BROCHURE &amp; GUIDE DOCUMENTS</span>
            <h4>안내자료 &amp; 요금표 다운로드 · {{ previewDownloadableDocs.length }}건</h4>
          </div>
        </div>
        <div class="mini-doc-list">
          <a
            v-for="(doc, dIdx) in previewDownloadableDocs"
            :key="doc.id || dIdx"
            :href="doc.downloadUrl || doc.fileUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="mini-doc-item"
            @click.stop
          >
            <div class="mini-doc-left">
              <span class="mini-doc-ext">{{ (doc.fileExt || 'DOC').toUpperCase() }}</span>
              <div>
                <strong>{{ doc.caption || doc.originalName }}</strong>
                <small>{{ doc.originalName }} · {{ formatFileSize(doc.fileSize) }}</small>
              </div>
            </div>
            <span class="mini-doc-dl">다운로드 ↓</span>
          </a>
        </div>
      </div>

      <div
        v-if="previewFeatureBlocks.faqItems?.length || previewFeatureBlocks.locationGuide"
        class="frame-section clickable-preview-sec"
        @click="emit('focus-tab', 'FORM_EDIT')"
      >
        <div class="frame-sec-head">
          <div>
            <span class="sec-kicker">SDUI FAQ_ACCORDION &amp; LOCATION_COURSE</span>
            <h4>자주 묻는 질문 {{ (previewFeatureBlocks.faqItems || []).length }}건 &amp; 오시는 길 · 주변 추천 코스</h4>
          </div>
        </div>
        <div class="mini-faq-loc-grid">
          <div v-if="previewFeatureBlocks.faqItems?.length" class="mini-faq-box">
            <div
              v-for="(fq, fIdx) in previewFeatureBlocks.faqItems.slice(0, 3)"
              :key="fIdx"
              class="mini-faq-item"
            >
              <strong>Q. {{ fq.q }}</strong>
              <p>A. {{ fq.a }}</p>
            </div>
          </div>
          <div v-if="previewFeatureBlocks.locationGuide" class="mini-loc-box">
            <strong>위치: {{ previewFeatureBlocks.locationGuide.address }}</strong>
            <p>{{ previewFeatureBlocks.locationGuide.parking }}</p>
            <p>{{ previewFeatureBlocks.locationGuide.publicTransit }}</p>
          </div>
        </div>
      </div>

      <div
        v-if="visualForm.showCmsForm && (editableCmsForm.title || siteRuntime.cmsForm)"
        class="frame-section frame-form-preview clickable-preview-sec"
        @click="emit('focus-tab', 'FORM_EDIT')"
      >
        <div class="form-preview-bar">
          <div>
            <span class="sec-kicker">ONLINE BOOKING &amp; INQUIRY · 클릭하여 폼 문구 편집</span>
            <strong>{{ editableCmsForm.title || siteRuntime.cmsForm?.title || siteRuntime.cmsForm?.formName }}</strong>
            <p>{{ editableCmsForm.description || siteRuntime.cmsForm?.description }}</p>
          </div>
          <span
            class="form-preview-cta"
            :style="{ background: visualForm.primaryColor || siteRuntime.designTokens?.primary || '#0f172a' }"
          >
            {{ editableCmsForm.submitText || '온라인 신청 접수 활성 ✓' }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { renderLogoWithBrand, formatFileSize } from './studioFormatters';

defineProps<{
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
</script>

<style scoped src="./studio-view.css"></style>
