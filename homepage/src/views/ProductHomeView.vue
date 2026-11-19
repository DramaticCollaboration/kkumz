<template>
  <div id="top" class="portal-view">
    <!-- =========================================================
         HERO: 시선 분산 요소를 제거한 고집중 가치 제안 + 실제 작동 화면 쇼케이스
         ========================================================= -->
    <header class="page-header">
      <div class="container">
        <div>
          <span class="hero-badge" style="margin-bottom: 1rem">
            {{ visualGuide?.roleEasyLabel ?? product.tierBadge }}
          </span>
        </div>

        <h1 class="page-title">{{ product.name }}</h1>
        <p class="page-subtitle">{{ product.subtitle }}</p>

        <!-- 핵심 CTA 2개 집중 -->
        <div class="hero-actions">
          <a
            v-if="primaryEntry"
            :href="primaryEntry.cloudUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="btn btn-primary btn-lg"
          >
            <i class="fas fa-rocket"></i> 라이브 데모 바로 열기
          </a>
          <a href="#quickstart" class="btn btn-secondary btn-lg">
            <i class="fas fa-key"></i> 데모 계정 · 설치 가이드
          </a>
        </div>

        <!-- 인라인 핵심 성과 및 기술 문서 스트립 (박스 중첩 없이 시선 흐름 유지) -->
        <div class="hero-meta-strip">
          <div class="hero-impact-inline">
            <span class="hero-impact-pill">{{ product.impactValue }}</span>
            <span class="hero-impact-text">{{ product.impactTitle }}</span>
          </div>
          <span class="hero-meta-divider" aria-hidden="true"></span>
          <div class="hero-resource-links">
            <a
              :href="'https://sync.empasy.io' + product.proposalPdfUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="hero-inline-link"
            >
              <i class="fas fa-file-pdf"></i> 제안서 PDF
            </a>
            <a
              :href="product.docUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="hero-inline-link"
            >
              <i class="fas fa-book"></i> 아키텍처 · API 문서
            </a>
          </div>
        </div>

        <!-- 실제 작동 화면 4종 쇼케이스 -->
        <div v-if="visualGuide" style="margin-top: 2rem; text-align: left">
          <ProductScreenshotShowcase
            :product-name="product.name"
            :primary-domain="product.subdomain"
            :primary-demo-url="primaryEntry?.cloudUrl"
            :guide="visualGuide"
          />
        </div>
      </div>
    </header>

    <!-- =========================================================
         1. WHY IT MATTERS: 도입 전 vs 도입 후 비교 & 제품 개요
         ========================================================= -->
    <section id="overview" class="section" style="background: rgba(15, 23, 42, 0.4)">
      <div class="container">
        <div class="section-header text-center">
          <span class="section-tag">WHY {{ product.name.toUpperCase() }}</span>
          <h2 class="section-title">무엇이 달라지나요? (도입 전 vs 도입 후)</h2>
          <p class="section-desc">
            기존 수작업·파편화된 운영 방식의 한계를 {{ product.name }}이 어떻게 해결하는지 비교해 보세요.
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

        <div class="grid-2" style="margin-top: 2.5rem">
          <div class="glass-card">
            <span class="section-tag">OVERVIEW</span>
            <h3 style="margin-bottom: 1rem">제품 한눈에 보기</h3>
            <p style="color: var(--text-secondary); line-height: 1.85; margin-bottom: 1.25rem">
              {{ product.summary }}
            </p>
            <div class="tag-row left" style="margin-top: 0">
              <span v-for="tag in product.techTags" :key="tag" class="tag">{{ tag }}</span>
            </div>
          </div>

          <div class="glass-card">
            <span class="section-tag">CORE CAPABILITIES</span>
            <h3 style="margin-bottom: 1rem">5대 핵심 역량</h3>
            <ul class="capability-list">
              <li v-for="cap in product.coreCapabilities" :key="cap">{{ cap }}</li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <!-- =========================================================
         2. KEY FEATURES & 3-STEP WORKFLOW: 핵심 기능 및 작동 원리
         ========================================================= -->
    <section id="features" class="section">
      <div class="container">
        <div class="section-header text-center">
          <span class="section-tag">KEY FEATURES &amp; WORKFLOW</span>
          <h2 class="section-title">{{ product.name }} 핵심 기능과 3단계 작동 원리</h2>
          <p class="section-desc">
            복잡한 설정 없이 직관적인 3단계 프로세스로 업무를 자동화합니다.
          </p>
        </div>

        <div class="grid-3">
          <div
            v-for="feat in product.richFeatures"
            :key="feat.badgeNo + feat.title"
            class="feature-card"
          >
            <div class="feature-icon-wrapper">{{ feat.badgeNo }}</div>
            <h3>{{ feat.title }}</h3>
            <p style="color: var(--text-secondary)">{{ feat.description }}</p>
          </div>
        </div>

        <div class="workflow-pipeline-head">
          <span class="section-tag" style="margin-bottom: 0">3-STEP PROCESS</span>
          <h3>3단계 자동화 파이프라인</h3>
        </div>
        <div class="workflow-grid-3">
          <div v-for="step in product.processSteps" :key="step.stepNo" class="workflow-step-card">
            <div class="workflow-step-top">
              <span class="workflow-step-badge">STEP 0{{ step.stepNo }}</span>
            </div>
            <h4>{{ step.title }}</h4>
            <p>{{ step.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- =========================================================
         3. LIVE DEMO & DOCKER QUICKSTART
         ========================================================= -->
    <QuickstartDemoDockerSection :product="product" @copy-text="copyText" />

    <!-- =========================================================
         4. DEPLOYMENT ARCHITECTURE & EDITIONS + 개발자 접이식 기술 명세
         ========================================================= -->
    <StandaloneVsEcosystemSection :product="product" />

    <section class="section" style="padding-top: 0">
      <div class="container">
        <details class="dev-spec-drawer">
          <summary class="dev-spec-summary">
            <div class="dev-spec-summary-left">
              <span class="dev-spec-badge"><i class="fas fa-terminal"></i> DEVELOPER &amp; ARCHITECTURE SPEC</span>
              <strong>개발자 · 아키텍트를 위한 AI 에이전트 CLI 시뮬레이션 및 연동 기술 명세 열기</strong>
              <span class="dev-spec-sub">A2A 명령어 실행 예시 · 표준 API 경로({{ product.canonicalApiPrefix }}) · MCP 연동 도구 · DB 스키마</span>
            </div>
            <span class="dev-spec-toggle-label">
              상세 기술 명세 펼쳐보기 <i class="fas fa-chevron-down summary-chevron"></i>
            </span>
          </summary>

          <div class="dev-spec-body">
            <div style="margin-bottom: 2rem">
              <h4 style="margin-bottom: 0.8rem; color: #38bdf8">
                <i class="fas fa-robot"></i> AI 에이전트 자율 작업 CLI 시뮬레이션
              </h4>
              <AgentCliTerminal :simulation="product.cliSimulation" @copy-text="copyText" />
            </div>

            <div class="spec-grid" style="margin-bottom: 1.5rem">
              <div class="spec-cell">
                <div class="spec-key">제품 분류</div>
                <div class="spec-value">{{ product.tierBadge }}</div>
              </div>
              <div class="spec-cell">
                <div class="spec-key">공식 도메인</div>
                <div class="spec-value mono">{{ product.subdomain }}</div>
              </div>
              <div class="spec-cell">
                <div class="spec-key">표준 API Prefix</div>
                <div class="spec-value mono">{{ product.canonicalApiPrefix }}</div>
              </div>
              <div class="spec-cell">
                <div class="spec-key">저장소 경로</div>
                <div class="spec-value mono">{{ product.repoPath }}</div>
              </div>
            </div>

            <div class="grid-2">
              <div class="spec-cell">
                <div class="spec-key">노출 MCP 도구 (@SyncMcpTool · JSON-RPC 2.0)</div>
                <div class="chip-list" style="margin-top: 0.65rem">
                  <span v-for="tool in product.mcpTools" :key="tool" class="code-chip accent">
                    {{ tool }}
                  </span>
                </div>
              </div>
              <div class="spec-cell">
                <div class="spec-key">핵심 데이터베이스 스키마 (PostgreSQL 18.6)</div>
                <div class="chip-list" style="margin-top: 0.65rem">
                  <span v-for="tb in product.dbTables" :key="tb" class="code-chip data">
                    {{ tb }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </details>
      </div>
    </section>

    <!-- =========================================================
         5. FAQ: 자주 묻는 질문
         ========================================================= -->
    <section id="faq" class="section" style="background: rgba(15, 23, 42, 0.4)">
      <div class="container">
        <div class="faq-section" style="margin: 0 auto">
          <div class="section-header text-center" style="margin-bottom: 2rem">
            <span class="section-tag">FAQ</span>
            <h2 class="section-title">{{ product.name }} 자주 묻는 질문</h2>
          </div>
          <details
            v-for="(faq, idx) in product.faqs"
            :key="faq.question"
            class="faq-item"
            :open="idx === 0"
          >
            <summary>{{ faq.question }}</summary>
            <div class="faq-answer">{{ faq.answer }}</div>
          </details>
        </div>
      </div>
    </section>

    <!-- =========================================================
         6. SIBLING PRODUCTS: 함께 연동 가능한 자매 제품군
         ========================================================= -->
    <section class="section">
      <div class="container">
        <div class="section-header text-center">
          <span class="section-tag">SYNCSERIES LINEUP</span>
          <h2 class="section-title">함께 연동할 수 있는 SyncSeries 제품군</h2>
          <p class="section-desc">
            필요할 때 언제든 추가 결합하여 부서 간 업무를 이어주는 자율 에이전트 라인업입니다.
          </p>
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

    <div v-if="toastMessage" class="copy-toast" role="status" aria-live="polite">
      <i class="fas fa-check-circle" style="margin-right: 6px"></i>{{ toastMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import AgentCliTerminal from '@/components/AgentCliTerminal.vue';
import ProductScreenshotShowcase from '@/components/ProductScreenshotShowcase.vue';
import QuickstartDemoDockerSection from '@/components/QuickstartDemoDockerSection.vue';
import StandaloneVsEcosystemSection from '@/components/StandaloneVsEcosystemSection.vue';
import { productData } from '@/data/productData';
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

const toastMessage = ref('');
let toastTimer: number | undefined;

const visualGuide = computed(() => getProductVisualGuide(props.product.id));

const primaryEntry = computed<ProductEntryLink | undefined>(() => {
  return props.product.entryLinks.find((item) => item.isPrimary) ?? props.product.entryLinks[0];
});

async function copyText(value: string, label: string): Promise<void> {
  try {
    await navigator.clipboard.writeText(value);
    toastMessage.value = `${label} 복사 완료: ${value}`;
  } catch {
    const textarea = document.createElement('textarea');
    textarea.value = value;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('copy');
    document.body.removeChild(textarea);
    toastMessage.value = `${label} 복사 완료`;
  }

  if (toastTimer) {
    window.clearTimeout(toastTimer);
  }
  toastTimer = window.setTimeout(() => {
    toastMessage.value = '';
  }, 2600);
}
</script>
