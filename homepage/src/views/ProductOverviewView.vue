<template>
  <div id="top" class="portal-view">
    <header class="page-header">
      <div class="container">
        <div>
          <span class="hero-badge" style="margin-bottom: 1rem">
            {{ isEn ? product.englishRoleBadge : (visualGuide?.roleEasyLabel ?? product.tierBadge) }}
          </span>
        </div>

        <h1 class="page-title">{{ product.name }}</h1>
        <p class="page-subtitle">{{ isEn ? pack.subtitleEn : product.subtitle }}</p>

        <div class="hero-actions" style="display: flex; flex-wrap: wrap; justify-content: center; gap: 0.75rem">
          <a
            v-for="entry in product.entryLinks"
            :key="entry.id"
            :href="entry.cloudUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="btn btn-lg"
            :class="entry.isPrimary ? 'btn-primary' : 'btn-outline'"
          >
            <i class="fas" :class="entry.isPrimary ? 'fa-rocket' : 'fa-sliders'" style="margin-right: 8px"></i>
            {{ entry.label }} · {{ entry.badgeText }}
          </a>
          <RouterLink :to="localePath('docs')" class="btn btn-outline btn-lg">
            <i class="fas fa-book-open" style="margin-right: 8px"></i> {{ tr('5분 Quickstart 가이드', '5-Min Quickstart Docs') }}
          </RouterLink>
        </div>

        <div class="hero-meta-strip">
          <div class="hero-impact-inline">
            <span class="hero-impact-pill">{{ isEn ? pack.impactValueEn : product.impactValue }}</span>
            <span class="hero-impact-text">{{ isEn ? pack.impactTitleEn : product.impactTitle }}</span>
          </div>
          <span class="hero-meta-divider" aria-hidden="true"></span>
          <div class="hero-resource-links">
            <RouterLink :to="localePath('features')" class="hero-inline-link">
              <i class="fas fa-cubes"></i> {{ tr('아키텍처 · MCP 명세', 'Architecture & MCP Spec') }}
            </RouterLink>
            <RouterLink :to="localePath('download')" class="hero-inline-link">
              <i class="fas fa-key"></i> {{ tr('데모 계정 · 다운로드', 'Demo Accounts & Downloads') }}
            </RouterLink>
          </div>
        </div>
      </div>
    </header>

    <!-- 4-Type 제품군 특화 인터랙티브 시뮬레이터 -->
    <InteractiveTypeSimulator :product="product" />

    <!-- 실제 작동 화면 쇼케이스 -->
    <section v-if="visualGuide" class="section" style="padding-top: 1rem">
      <div class="container">
        <ProductScreenshotShowcase
          :product-name="product.name"
          :primary-domain="product.subdomain"
          :primary-demo-url="primaryEntry?.cloudUrl"
          :guide="visualGuide"
        />
      </div>
    </section>

    <!-- 도입 전 vs 도입 후 비교 -->
    <section id="overview" class="section" style="background: rgba(15, 23, 42, 0.4)">
      <div class="container">
        <div class="section-header text-center">
          <span class="section-tag">WHY {{ product.name.toUpperCase() }}</span>
          <h2 class="section-title">{{ tr('무엇이 달라지나요? (도입 전 vs 도입 후)', 'Before vs After Deployment') }}</h2>
          <p class="section-desc">
            {{ isEn ? pack.summaryEn : product.summary }}
          </p>
        </div>

        <div class="vs-container" style="margin-top: 0">
          <div class="vs-box legacy">
            <h4>{{ product.vsLegacyTitle }}</h4>
            <ul style="margin-top: 1.5rem">
              <li v-for="pt in product.vsLegacyPoints" :key="pt">
                <i class="fas fa-xmark" style="color: #ef4444; margin-top: 4px"></i>
                <div>{{ pt }}</div>
              </li>
            </ul>
          </div>
          <div class="vs-box sync">
            <span class="badge-recommend">EMPASY AI SOLUTION</span>
            <h4>{{ product.vsAgentTitle }}</h4>
            <ul style="margin-top: 1.5rem">
              <li v-for="pt in product.vsAgentPoints" :key="pt">
                <i class="fas fa-check" style="color: #38bdf8; margin-top: 4px"></i>
                <div>{{ pt }}</div>
              </li>
            </ul>
          </div>
        </div>

        <div class="grid-3" style="margin-top: 2.5rem">
          <RouterLink :to="localePath('features')" class="feature-card" style="text-decoration: none">
            <div class="feature-icon-wrapper">01</div>
            <h3>{{ tr('심층 기능 & MCP 명세', 'Deep-Dive Features & MCP') }}</h3>
            <p style="color: var(--text-secondary)">{{ tr('핵심 기능, 3단계 파이프라인, @SyncMcpTool 및 PostgreSQL 18.6 스키마를 확인하세요.', 'Explore core capabilities, 3-step pipeline, @SyncMcpTool specs, and PostgreSQL 18.6 schema.') }}</p>
          </RouterLink>
          <RouterLink :to="localePath('pricing')" class="feature-card" style="text-decoration: none">
            <div class="feature-icon-wrapper">02</div>
            <h3>{{ tr('에디션 비교 & 도입 FAQ', 'Editions, Licensing & FAQ') }}</h3>
            <p style="color: var(--text-secondary)">{{ tr('Evaluation 무료 평가판, Standalone Pro 단독 구축형, Enterprise Swarm 에디션을 비교하세요.', 'Compare Evaluation Trial, Standalone Pro, and Enterprise Swarm editions.') }}</p>
          </RouterLink>
          <RouterLink :to="localePath('docs')" class="feature-card" style="text-decoration: none">
            <div class="feature-icon-wrapper">03</div>
            <h3>{{ tr('5분 Docker 퀵스타트 문서', '5-Min Docker Quickstart') }}</h3>
            <p style="color: var(--text-secondary)">{{ tr('docker-compose-dev.yml 원클릭 실행과 Canonical API 레퍼런스를 즉시 확인하세요.', 'Run with docker-compose-dev.yml and inspect Canonical API references.') }}</p>
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- 함께 연동할 수 있는 자매 제품군 -->
    <section class="section">
      <div class="container">
        <div class="section-header text-center">
          <span class="section-tag">SYNCSERIES LINEUP</span>
          <h2 class="section-title">{{ tr('함께 연동할 수 있는 SyncSeries 제품군', 'Connected SyncSeries Product Lineup') }}</h2>
        </div>

        <div class="sibling-nav">
          <a
            v-for="item in product.siblings"
            :key="item.id"
            :href="item.portalUrl"
            class="sibling-link"
          >
            <div class="agent-icon-box" :class="item.iconBoxClass">
              <i :class="item.faIconClass"></i>
            </div>
            <div>
              <div style="font-weight: 700; color: var(--text-primary)">{{ item.name }}</div>
              <div class="subdomain-text">{{ item.subdomain }}</div>
            </div>
          </a>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import InteractiveTypeSimulator from '@/components/InteractiveTypeSimulator.vue';
import ProductScreenshotShowcase from '@/components/ProductScreenshotShowcase.vue';
import { useHomepageLocale } from '@/composables/useHomepageLocale';
import { productData } from '@/data/productData';
import { getProductEnhancementPack } from '@/data/productI18n';
import { getProductVisualGuide } from '@/data/productVisuals';
import type { ProductEntryLink, StandaloneProductData } from '@/types/homepage.types';

const props = withDefaults(
  defineProps<{
    product?: StandaloneProductData;
  }>(),
  {
    product: () => productData,
  }
);

const { isEn, localePath, tr } = useHomepageLocale();

const visualGuide = computed(() => getProductVisualGuide(props.product.id));
const pack = computed(() =>
  getProductEnhancementPack(
    props.product.id,
    props.product.name,
    props.product.canonicalApiPrefix,
    props.product.mcpTools,
    props.product.dbTables
  )
);

const primaryEntry = computed<ProductEntryLink | undefined>(() => {
  return props.product.entryLinks.find((item) => item.isPrimary) ?? props.product.entryLinks[0];
});
</script>
