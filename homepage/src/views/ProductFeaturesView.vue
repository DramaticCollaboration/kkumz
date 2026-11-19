<template>
  <div class="portal-view">
    <section class="section" style="padding-top: 7rem">
      <div class="container">
        <div class="section-header text-center">
          <span class="section-tag">DEEP-DIVE FEATURES &amp; ARCHITECTURE</span>
          <h1 class="section-title">{{ product.name }} {{ tr('핵심 기능 · 아키텍처 · MCP 도구 명세', 'Core Features, Architecture & MCP Spec') }}</h1>
          <p class="section-desc">
            {{ isEn ? product.englishRoleBadge : product.summary }}
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

        <div class="grid-2" style="margin-top: 2.5rem">
          <div class="glass-card">
            <span class="section-tag">CORE CAPABILITIES</span>
            <h3 style="margin-bottom: 1rem">{{ tr('5대 핵심 역량', '5 Core Capabilities') }}</h3>
            <ul class="capability-list">
              <li v-for="cap in product.coreCapabilities" :key="cap">{{ cap }}</li>
            </ul>
          </div>

          <div class="glass-card">
            <span class="section-tag">TECH STACK &amp; RUNTIME</span>
            <h3 style="margin-bottom: 1rem">{{ tr('엔터프라이즈 기술 규격', 'Enterprise Runtime Specifications') }}</h3>
            <div class="spec-grid" style="margin-bottom: 1rem">
              <div class="spec-cell">
                <div class="spec-key">{{ tr('공식 홈페이지 도메인', 'Official Domain') }}</div>
                <div class="spec-value mono">{{ product.subdomain }}</div>
              </div>
              <div class="spec-cell">
                <div class="spec-key">Canonical API Prefix</div>
                <div class="spec-value mono">{{ product.canonicalApiPrefix }}</div>
              </div>
              <div class="spec-cell">
                <div class="spec-key">{{ tr('백엔드 런타임', 'Backend Runtime') }}</div>
                <div class="spec-value">Spring Boot 4.0.9 · JDK 25</div>
              </div>
              <div class="spec-cell">
                <div class="spec-key">{{ tr('데이터베이스 표준', 'Database Standard') }}</div>
                <div class="spec-value">PostgreSQL 18.6 + pgvector</div>
              </div>
            </div>
            <div class="tag-row left">
              <span v-for="tag in product.techTags" :key="tag" class="tag">{{ tag }}</span>
            </div>
          </div>
        </div>

        <div class="workflow-pipeline-head" style="margin-top: 3rem">
          <span class="section-tag" style="margin-bottom: 0">3-STEP PIPELINE</span>
          <h3>{{ tr('3단계 자율 실행 파이프라인', '3-Step Autonomous Execution Pipeline') }}</h3>
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

    <StandaloneVsEcosystemSection :product="product" />

    <section class="section" style="padding-top: 0">
      <div class="container">
        <div class="glass-card">
          <span class="section-tag">MCP TOOLS &amp; DATABASE SCHEMA</span>
          <h3 style="margin-bottom: 1.25rem">{{ tr('A2A 연동 MCP 도구(@SyncMcpTool) 및 핵심 DB 스키마', 'Exposed @SyncMcpTool Registry & PostgreSQL 18.6 Schema') }}</h3>
          <div style="margin-bottom: 1.75rem">
            <AgentCliTerminal :simulation="product.cliSimulation" />
          </div>
          <div class="grid-2">
            <div class="spec-cell">
              <div class="spec-key">{{ tr('노출 MCP 도구 (@SyncMcpTool · JSON-RPC 2.0)', 'Exposed MCP Tools (@SyncMcpTool · JSON-RPC 2.0)') }}</div>
              <div class="chip-list" style="margin-top: 0.65rem">
                <span v-for="tool in product.mcpTools" :key="tool" class="code-chip accent">
                  {{ tool }}
                </span>
              </div>
            </div>
            <div class="spec-cell">
              <div class="spec-key">{{ tr('핵심 데이터베이스 테이블 (PostgreSQL 18.6)', 'Core Database Tables (PostgreSQL 18.6)') }}</div>
              <div class="chip-list" style="margin-top: 0.65rem">
                <span v-for="tb in product.dbTables" :key="tb" class="code-chip data">
                  {{ tb }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import AgentCliTerminal from '@/components/AgentCliTerminal.vue';
import StandaloneVsEcosystemSection from '@/components/StandaloneVsEcosystemSection.vue';
import { useHomepageLocale } from '@/composables/useHomepageLocale';
import { productData } from '@/data/productData';
import type { StandaloneProductData } from '@/types/homepage.types';

withDefaults(
  defineProps<{
    product?: StandaloneProductData;
  }>(),
  {
    product: () => productData,
  }
);

const { isEn, tr } = useHomepageLocale();
</script>
