<template>
  <div class="portal-view">
    <section class="section" style="padding-top: 7rem">
      <div class="container">
        <div class="section-header text-center">
          <span class="section-tag">EDITIONS &amp; LICENSING</span>
          <h1 class="section-title">{{ product.name }} {{ tr('에디션 비교 및 도입 안내', 'Editions, Licensing & Deployment Tiers') }}</h1>
          <p class="section-desc">
            {{
              tr(
                '무료 라이브 데모 평가판부터 단일 제품 독립 구축(Standalone Pro), 전사 A2A 에코시스템 연합(Enterprise Swarm)까지 도입 규모에 맞춰 선택하세요.',
                'Choose from our Free Evaluation Trial, Standalone Pro single-product package, or Enterprise Swarm A2A ecosystem edition.'
              )
            }}
          </p>
        </div>

        <div style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 1.5rem; align-items: stretch">
          <!-- Tier 1: Evaluation / Trial -->
          <div class="glass-card" style="display: flex; flex-direction: column; justify-content: space-between; padding: 1.75rem; border-radius: 16px; background: rgba(15, 23, 42, 0.78); border: 1px solid rgba(148, 163, 184, 0.22)">
            <div>
              <span class="section-tag">EVALUATION / TRIAL</span>
              <h3 style="margin-bottom: 0.5rem">{{ tr('무료 라이브 데모 & Docker 평가판', 'Free Live Demo & Docker Trial') }}</h3>
              <div style="font-size: 1.75rem; font-weight: 800; color: #38bdf8; margin-bottom: 1rem">
                {{ tr('무료 체험 (Free)', 'Free Evaluation') }}
              </div>
              <ul class="capability-list" style="margin-bottom: 1.5rem">
                <li>{{ tr('클라우드 라이브 데모 즉시 접속 및 역할별 계정 체험', 'Instant cloud live demo access with role-based accounts') }}</li>
                <li>{{ tr('docker-compose-dev.yml 기반 로컬 평가 환경 기동', 'Local evaluation stack via docker-compose-dev.yml') }}</li>
                <li>{{ tr('매일 04:00 KST 데모 데이터 자동 초기화 보장', 'Daily automated data reset at 04:00 KST') }}</li>
              </ul>
            </div>
            <RouterLink :to="localePath('download')" class="btn btn-outline" style="width: 100%; text-align: center">
              {{ tr('데모 계정으로 바로 체험', 'Launch Free Live Demo') }}
            </RouterLink>
          </div>

          <!-- Tier 2: Standalone Pro -->
          <div
            class="glass-card"
            :style="{
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
              border: `2px solid ${product.accentColor || '#38bdf8'}`, padding: '1.75rem', borderRadius: '16px', background: 'rgba(15, 23, 42, 0.88)',
            }"
          >
            <div>
              <span class="section-tag" :style="{ color: product.accentColor || '#38bdf8' }">STANDALONE PRO (RECOMMENDED)</span>
              <h3 style="margin-bottom: 0.5rem">{{ product.name }} {{ tr('단독 패키지 구축형', 'Standalone Pro Edition') }}</h3>
              <div style="font-size: 1.75rem; font-weight: 800; color: #f8fafc; margin-bottom: 1rem">
                {{ tr('단일 제품 영구/연간 라이선스', 'Per-Product Commercial License') }}
              </div>
              <ul class="capability-list" style="margin-bottom: 1.5rem">
                <li>{{ product.standaloneArch.title }}</li>
                <li v-for="hl in product.standaloneArch.highlights" :key="hl">{{ hl }}</li>
                <li>{{ tr('Spring Boot 4.0 + Vue 3.5 + PostgreSQL 18.6 표준 풀스택 제공', 'Full Spring Boot 4.0 + Vue 3.5 + PostgreSQL 18.6 package') }}</li>
              </ul>
            </div>
            <a
              href="https://empasy.io/ko/pages/contact.html"
              target="_blank"
              rel="noopener noreferrer"
              class="btn btn-primary"
              style="width: 100%; text-align: center"
            >
              {{ tr('단독 도입 견적 문의', 'Request Standalone Quote') }}
            </a>
          </div>

          <!-- Tier 3: Enterprise Swarm -->
          <div class="glass-card" style="display: flex; flex-direction: column; justify-content: space-between; padding: 1.75rem; border-radius: 16px; background: rgba(15, 23, 42, 0.78); border: 1px solid rgba(148, 163, 184, 0.22)">
            <div>
              <span class="section-tag">ENTERPRISE SWARM</span>
              <h3 style="margin-bottom: 0.5rem">{{ tr('전사 A2A 에코시스템 & 폐쇄망 구축', 'Enterprise A2A Swarm & Air-Gapped') }}</h3>
              <div style="font-size: 1.75rem; font-weight: 800; color: #34d399; margin-bottom: 1rem">
                {{ tr('엔터프라이즈 맞춤형', 'Custom Enterprise SLA') }}
              </div>
              <ul class="capability-list" style="margin-bottom: 1.5rem">
                <li>{{ product.ecosystemArch.title }}</li>
                <li v-for="hl in product.ecosystemArch.highlights" :key="hl">{{ hl }}</li>
                <li>{{ tr('폐쇄망(Air-Gapped) K8s 매니페스트 및 전담 아키텍트 기술지원', 'Air-gapped K8s manifests & dedicated architect support') }}</li>
              </ul>
            </div>
            <a
              href="https://empasy.io/ko/pages/contact.html"
              target="_blank"
              rel="noopener noreferrer"
              class="btn btn-outline"
              style="width: 100%; text-align: center"
            >
              {{ tr('엔터프라이즈 아키텍처 상담', 'Contact Enterprise Architect') }}
            </a>
          </div>
        </div>

        <!-- 상세 비교 매트릭스 -->
        <div class="glass-card" style="margin-top: 2.5rem; overflow-x: auto">
          <h3 style="margin-bottom: 1rem">{{ tr('에디션별 제공 범위 상세 비교', 'Edition Feature Comparison Matrix') }}</h3>
          <table style="width: 100%; border-collapse: collapse; font-size: 0.9rem; text-align: left">
            <thead>
              <tr style="border-bottom: 1px solid rgba(148, 163, 184, 0.25); color: #94a3b8">
                <th style="padding: 0.75rem">{{ tr('항목', 'Capability') }}</th>
                <th style="padding: 0.75rem">Evaluation / Trial</th>
                <th style="padding: 0.75rem; color: #38bdf8">Standalone Pro</th>
                <th style="padding: 0.75rem; color: #34d399">Enterprise Swarm</th>
              </tr>
            </thead>
            <tbody>
              <tr style="border-bottom: 1px solid rgba(148, 163, 184, 0.12)">
                <td style="padding: 0.75rem; font-weight: 600">{{ tr('핵심 도메인 기능 및 DB 동적 메뉴(PermissionModeEnum.BACK)', 'Core Domain Features & Dynamic DB Menus') }}</td>
                <td style="padding: 0.75rem">{{ tr('데모 클러스터 제공', 'Cloud Demo Cluster') }}</td>
                <td style="padding: 0.75rem">{{ tr('100% 전체 제공', '100% Full Access') }}</td>
                <td style="padding: 0.75rem">{{ tr('100% 전체 제공', '100% Full Access') }}</td>
              </tr>
              <tr style="border-bottom: 1px solid rgba(148, 163, 184, 0.12)">
                <td style="padding: 0.75rem; font-weight: 600">Canonical API ({{ product.canonicalApiPrefix }}) &amp; SpringDoc 3.2</td>
                <td style="padding: 0.75rem">{{ tr('읽기·체험 허용', 'Read & Sandbox') }}</td>
                <td style="padding: 0.75rem">{{ tr('무제한 호출', 'Unlimited') }}</td>
                <td style="padding: 0.75rem">{{ tr('무제한 + 게이트웨이 보안 관제', 'Unlimited + Inline WAF Guard') }}</td>
              </tr>
              <tr style="border-bottom: 1px solid rgba(148, 163, 184, 0.12)">
                <td style="padding: 0.75rem; font-weight: 600">SyncVerse A2A MCP ({{ product.mcpTools.length }} Tools)</td>
                <td style="padding: 0.75rem">{{ tr('시뮬레이터 프리뷰', 'Simulator Preview') }}</td>
                <td style="padding: 0.75rem">{{ tr('단독 MCP 서버 내장', 'Built-in MCP Server') }}</td>
                <td style="padding: 0.75rem">{{ tr('전사 멀티에이전트 자동 오케스트레이션', 'Full Multi-Agent Swarm Orchestration') }}</td>
              </tr>
              <tr>
                <td style="padding: 0.75rem; font-weight: 600">{{ tr('배포 환경 및 기술지원 SLA', 'Deployment & Support SLA') }}</td>
                <td style="padding: 0.75rem">Docker Compose</td>
                <td style="padding: 0.75rem">Docker / On-Premise / Cloud</td>
                <td style="padding: 0.75rem">Air-Gapped K8s + 24/7 Enterprise SLA</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- FAQ -->
        <div class="faq-section" style="margin: 3rem auto 0 auto">
          <div class="section-header text-center" style="margin-bottom: 2rem">
            <span class="section-tag">FAQ</span>
            <h2 class="section-title">{{ product.name }} {{ tr('자주 묻는 질문', 'Frequently Asked Questions') }}</h2>
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
  </div>
</template>

<script setup lang="ts">
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

const { localePath, tr } = useHomepageLocale();
</script>
