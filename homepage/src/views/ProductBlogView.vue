<template>
  <div class="portal-view">
    <section class="section" style="padding-top: 7rem">
      <div class="container">
        <div class="section-header text-center">
          <span class="section-tag">TECH INSIGHTS &amp; RELEASE NOTES</span>
          <h1 class="section-title">{{ product.name }} {{ tr('엔지니어링 블로그 및 버전별 릴리스 노트', 'Engineering Blog & Release Notes') }}</h1>
          <p class="section-desc">
            {{
              tr(
                `${product.name}의 Spring Boot 4.0 · AgentScope Java · PostgreSQL 18.6 아키텍처 심층 분석 아티클과 최신 릴리스 변경 내역을 확인하세요.`,
                `Deep-dive engineering articles and version release notes for ${product.name} on Spring Boot 4.0, AgentScope Java, and PostgreSQL 18.6.`
              )
            }}
          </p>
        </div>

        <!-- 아티클 선택 카드 목록 -->
        <div class="grid-3" style="margin-bottom: 2rem">
          <article
            v-for="(art, idx) in pack.articles"
            :key="art.id"
            class="feature-card"
            :style="{
              cursor: 'pointer',
              border: selectedArticleIdx === idx ? `2px solid ${product.accentColor || '#38bdf8'}` : undefined,
            }"
            @click="selectedArticleIdx = idx"
          >
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; font-size: 0.78rem; color: #38bdf8">
              <span>{{ art.category }}</span>
              <span>{{ art.date }} · {{ art.readTime }}</span>
            </div>
            <h3 style="font-size: 1.08rem; margin-bottom: 0.75rem">
              {{ isEn ? art.titleEn : art.titleKo }}
            </h3>
            <p style="color: var(--text-secondary); font-size: 0.88rem; line-height: 1.65">
              {{ isEn ? art.summaryEn : art.summaryKo }}
            </p>
          </article>
        </div>

        <!-- 선택된 기술 아티클 본문 리더 뷰 -->
        <div v-if="activeArticle" class="glass-card" style="margin-bottom: 3rem">
          <div style="display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 0.5rem; margin-bottom: 1rem">
            <span class="section-tag" style="margin-bottom: 0">{{ activeArticle.category }} · {{ activeArticle.date }}</span>
            <span class="code-chip accent">{{ activeArticle.readTime }}</span>
          </div>
          <h2 style="margin-top: 0; margin-bottom: 1rem">
            {{ isEn ? activeArticle.titleEn : activeArticle.titleKo }}
          </h2>
          <p style="color: var(--text-secondary); line-height: 1.8; margin-bottom: 1.5rem">
            {{ isEn ? activeArticle.summaryEn : activeArticle.summaryKo }}
          </p>

          <div
            v-for="sec in (isEn ? activeArticle.sectionsEn : activeArticle.sectionsKo)"
            :key="sec.heading"
            style="margin-bottom: 1.5rem"
          >
            <h3 style="font-size: 1.1rem; color: #f8fafc; margin-bottom: 0.5rem">{{ sec.heading }}</h3>
            <p style="color: #cbd5e1; line-height: 1.75; margin-bottom: 0.75rem">{{ sec.body }}</p>
            <pre
              v-if="sec.code"
              style="background: rgba(2, 6, 23, 0.8); padding: 1rem; border-radius: 8px; color: #a7f3d0; font-size: 0.8rem; overflow-x: auto"
            >{{ sec.code }}</pre>
          </div>
        </div>

        <!-- 버전별 Release Notes -->
        <div class="glass-card">
          <span class="section-tag">CHANGELOG &amp; RELEASE NOTES</span>
          <h3 style="margin-bottom: 1.25rem">{{ product.name }} {{ tr('주요 버전 업데이트 내역', 'Version Release History') }}</h3>
          <div class="workflow-grid-3">
            <div class="workflow-step-card">
              <div class="workflow-step-top">
                <span class="workflow-step-badge">v4.0.9 (Latest · 2026.11)</span>
              </div>
              <h4>Spring Boot 4.0.9 &amp; Vue 3.5.35</h4>
              <p>{{ tr('Spring Framework 7.0, Jakarta EE 11, Jackson 3(tools.jackson.*), SpringDoc 3.2.0, Vite 8.1.1 및 36개 클린 도메인 체계 전면 적용.', 'Upgraded to Spring Boot 4.0.9, Jackson 3 (tools.jackson.*), SpringDoc 3.2.0, Vue 3.5.35, Vite 8.1.1, and 36 Clean Domains.') }}</p>
            </div>
            <div class="workflow-step-card">
              <div class="workflow-step-top">
                <span class="workflow-step-badge">v3.5.16 (2026.08)</span>
              </div>
              <h4>AgentScope Java 2.0 &amp; A2A MCP</h4>
              <p>{{ tr('전사 AgentScope Java 표준화 및 @SyncMcpTool 기반 JSON-RPC 2.0 에이전트 간 자율 협업 파이프라인 구축.', 'Standardized on AgentScope Java 2.0 and @SyncMcpTool JSON-RPC 2.0 multi-agent orchestration.') }}</p>
            </div>
            <div class="workflow-step-card">
              <div class="workflow-step-top">
                <span class="workflow-step-badge">v3.2.0 (2026.05)</span>
              </div>
              <h4>PostgreSQL 18.6 6-File SSOT</h4>
              <p>{{ tr('6단계 표준 DB 초기화 스크립트(00~05_*.sql) 및 PermissionModeEnum.BACK 동적 메뉴 인가 표준 확립.', 'Established the 6-File PostgreSQL 18.6 SSOT schema and PermissionModeEnum.BACK dynamic menu authorization.') }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useHomepageLocale } from '@/composables/useHomepageLocale';
import { productData } from '@/data/productData';
import { getProductEnhancementPack } from '@/data/productI18n';
import type { StandaloneProductData } from '@/types/homepage.types';

const props = withDefaults(
  defineProps<{
    product?: StandaloneProductData;
  }>(),
  {
    product: () => productData,
  }
);

const { isEn, tr } = useHomepageLocale();
const selectedArticleIdx = ref(0);

const pack = computed(() =>
  getProductEnhancementPack(
    props.product.id,
    props.product.name,
    props.product.canonicalApiPrefix,
    props.product.mcpTools,
    props.product.dbTables
  )
);

const activeArticle = computed(() => pack.value.articles[selectedArticleIdx.value] || pack.value.articles[0]);
</script>
