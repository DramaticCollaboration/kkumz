<template>
  <div class="portal-view">
    <section class="section" style="padding-top: 7rem">
      <div class="container">
        <div class="section-header text-center">
          <span class="section-tag">QUICKSTART &amp; DOCUMENTATION</span>
          <h1 class="section-title">{{ product.name }} {{ tr('5분 Docker 퀵스타트 및 기술 문서 뷰어', '5-Min Docker Quickstart & Technical Docs') }}</h1>
          <p class="section-desc">
            {{
              tr(
                '좌측 목차를 선택하여 Docker Compose 기동, 4-Tier 프로파일 설정, Canonical API 및 MCP 연동 레퍼런스를 즉시 확인하고 명령어를 복사하세요.',
                'Select a topic from the sidebar to inspect Docker Compose commands, 4-Tier Spring profiles, Canonical API endpoints, and MCP tool specifications.'
              )
            }}
          </p>
        </div>

        <div style="display: grid; grid-template-columns: 280px minmax(0, 1fr); gap: 1.75rem; align-items: start">
          <!-- 좌측 사이드바 목차 -->
          <aside class="glass-card" style="padding: 1.25rem; position: sticky; top: 96px">
            <div class="spec-key" style="margin-bottom: 0.75rem">{{ tr('문서 목차 (TOC)', 'DOCUMENTATION TOC') }}</div>
            <div style="display: flex; flex-direction: column; gap: 0.45rem">
              <button
                v-for="(sec, idx) in docSections"
                :key="sec.id"
                type="button"
                class="btn btn-sm"
                :class="activeDocIdx === idx ? 'btn-primary' : 'btn-outline'"
                style="text-align: left; justify-content: flex-start; width: 100%; white-space: normal; word-break: keep-all; line-height: 1.4; height: auto; padding: 0.65rem 0.85rem"
                @click="activeDocIdx = idx"
              >
                0{{ idx + 1 }}. {{ sec.shortTitle || sec.title }}
              </button>
            </div>
            <hr style="border-color: rgba(148, 163, 184, 0.15); margin: 1rem 0" />
            <a
              :href="product.docUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="btn btn-outline btn-sm"
              style="width: 100%; text-align: center"
            >
              <i class="fas fa-up-right-from-square" style="margin-right: 6px"></i>
              {{ tr('전체 기술 문서 (doc.empasy.com)', 'Full Docs (doc.empasy.com)') }}
            </a>
          </aside>

          <!-- 우측 문서 본문 뷰어 -->
          <div class="glass-card">
            <span class="section-tag">{{ activeSection.badge }}</span>
            <h2 style="margin-top: 0.25rem; margin-bottom: 0.75rem">{{ activeSection.title }}</h2>
            <p style="color: var(--text-secondary); line-height: 1.75; margin-bottom: 1.25rem">
              {{ activeSection.description }}
            </p>

            <div
              v-for="block in activeSection.codeBlocks"
              :key="block.label"
              class="spec-cell"
              style="background: rgba(2, 6, 23, 0.75); border-radius: 10px; padding: 1rem; margin-bottom: 1rem"
            >
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem">
                <span class="spec-key" style="color: #38bdf8">{{ block.label }}</span>
                <button type="button" class="btn btn-outline btn-sm" @click="copyCode(block.code, block.label)">
                  <i class="fas fa-copy" style="margin-right: 4px"></i>{{ tr('코드 복사', 'Copy') }}
                </button>
              </div>
              <pre style="margin: 0; font-size: 0.82rem; color: #e2e8f0; white-space: pre-wrap; font-family: monospace; line-height: 1.6">{{ block.code }}</pre>
            </div>

            <!-- 로컬 엔드포인트 요약 표 -->
            <div style="margin-top: 1.5rem">
              <h4 style="margin-bottom: 0.75rem">{{ tr('표준 서비스 엔드포인트', 'Standard Service Endpoints') }}</h4>
              <div class="grid-3">
                <div v-for="ep in product.dockerGuide.endpoints" :key="ep.label" class="spec-cell">
                  <div class="spec-key">{{ ep.label }}</div>
                  <div class="spec-value mono" style="font-size: 0.85rem">{{ ep.url }}</div>
                  <div style="font-size: 0.78rem; color: #94a3b8; margin-top: 4px">{{ ep.note }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <div v-if="copiedToast" class="copy-toast" role="status" aria-live="polite">
      <i class="fas fa-check-circle" style="margin-right: 6px"></i>{{ copiedToast }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useHomepageLocale } from '@/composables/useHomepageLocale';
import { productData } from '@/data/productData';
import type { StandaloneProductData } from '@/types/homepage.types';

const props = withDefaults(
  defineProps<{
    product?: StandaloneProductData;
  }>(),
  {
    product: () => productData,
  }
);

const { tr } = useHomepageLocale();
const activeDocIdx = ref(0);
const copiedToast = ref('');

const docSections = computed(() => [
  {
    id: 'docker-quickstart',
    badge: 'STEP 01 · DOCKER QUICKSTART',
    shortTitle: tr('Docker Compose 기동', 'Docker Quickstart'),
    title: tr('5분 Docker Compose 통합 기동', '5-Minute Docker Compose Quickstart'),
    description: tr(
      `${props.product.name}은 docker-compose-dev.yml(전체 컨테이너 통합 실행)과 docker-compose-local.yml(IDE 디버깅용 인프라 단독 실행) 이원화 표준을 제공합니다.`,
      `${props.product.name} provides a dual Docker Compose standard: docker-compose-dev.yml for full cluster execution and docker-compose-local.yml for local IDE debugging.`
    ),
    codeBlocks: [
      {
        label: tr('1. 작업 디렉터리 이동 및 통합 클러스터 빌드·기동', '1. Navigate to Workdir & Launch Full Cluster'),
        code: `cd ${props.product.dockerGuide.workdir}\n${props.product.dockerGuide.devCommand}`,
      },
      {
        label: tr('2. 로컬 IDE 개발용 인프라(PostgreSQL 18.6 / Redis) 단독 기동', '2. Launch Local Infrastructure Only'),
        code: `cd ${props.product.dockerGuide.workdir}\n${props.product.dockerGuide.localCommand}`,
      },
    ],
  },
  {
    id: 'spring-profiles',
    badge: 'STEP 02 · 4-TIER SPRING PROFILES & DB SSOT',
    shortTitle: tr('Spring 프로파일 · DB 초기화', 'Profiles & 6-File DB'),
    title: tr('Spring Boot 4.0 프로파일 및 6-File DB 초기화', 'Spring Boot 4.0 Profiles & 6-File DB SSOT'),
    description: tr(
      '모든 백엔드 모듈은 local / dev / prod 프로파일 체계와 db/00_extensions.sql ~ 05_sample_dev.sql 6단계 결정론적 스키마 초기화를 준수합니다.',
      'All backend modules follow the 4-tier Spring profile standard and deterministic 6-file PostgreSQL 18.6 schema initialization.'
    ),
    codeBlocks: [
      {
        label: '6-File PostgreSQL 18.6 SSOT Initialization',
        code: `psql -h localhost -U postgres -d sync_db -f ${props.product.repoPath}/db/00_extensions.sql\npsql -h localhost -U postgres -d sync_db -f ${props.product.repoPath}/db/01_schema_system.sql\npsql -h localhost -U postgres -d sync_db -f ${props.product.repoPath}/db/02_schema_domain.sql\npsql -h localhost -U postgres -d sync_db -f ${props.product.repoPath}/db/03_seed_system.sql\npsql -h localhost -U postgres -d sync_db -f ${props.product.repoPath}/db/04_seed_domain.sql`,
      },
    ],
  },
  {
    id: 'canonical-api-mcp',
    badge: 'STEP 03 · CANONICAL API & MCP JSON-RPC 2.0',
    shortTitle: tr('REST API · MCP 레퍼런스', 'REST API & MCP Spec'),
    title: tr('Canonical REST API 및 MCP 도구 레퍼런스', 'Canonical REST API & MCP Tool Reference'),
    description: tr(
      `모든 API는 ${props.product.canonicalApiPrefix} 단일 접두사로 매핑되며, SpringDoc OpenAPI 3.2.0(/v3/api-docs) 및 MCP JSON-RPC 2.0 엔드포인트를 제공합니다.`,
      `All REST endpoints map under ${props.product.canonicalApiPrefix} with SpringDoc OpenAPI 3.2.0 and MCP JSON-RPC 2.0 support.`
    ),
    codeBlocks: [
      {
        label: tr('동적 메뉴 인가 및 헬스 체크 호출', 'Dynamic Menu Authorization & Health Probe'),
        code: props.product.dockerGuide.healthCheckCmd,
      },
      {
        label: tr('A2A MCP 도구 호출 예시', 'A2A MCP Tool Invocation Example'),
        code: `${props.product.cliSimulation.command}\n# Exposed Tools: ${props.product.mcpTools.join(', ')}`,
      },
    ],
  },
]);

const activeSection = computed(() => docSections.value[activeDocIdx.value] || docSections.value[0]);

async function copyCode(code: string, label: string): Promise<void> {
  try {
    await navigator.clipboard.writeText(code);
    copiedToast.value = `${label} 복사 완료`;
  } catch {
    copiedToast.value = `${label} 복사 완료`;
  }
  window.setTimeout(() => {
    copiedToast.value = '';
  }, 2400);
}
</script>
