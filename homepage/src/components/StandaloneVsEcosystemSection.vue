<template>
  <section id="architecture" class="section">
    <div class="container">
      <div class="section-header text-center">
        <span class="section-tag">FLEXIBLE DEPLOYMENT &amp; EDITIONS</span>
        <h2 class="section-title">우리 회사 상황에 맞는 도입 방식 선택</h2>
        <p class="section-desc">
          <strong>{{ product.name }}</strong> 하나만 단독 패키지로 가볍게 먼저 도입할 수도 있고,
          추후 <strong>SyncVerse</strong> 통합 지휘소와 연결해 부서 간 업무 자동화로 확장할 수도 있습니다.
        </p>
      </div>

      <div class="arch-dual-grid">
        <!-- 1. 단독 도입 아키텍처 카드 -->
        <article class="glass-card arch-card">
          <div class="arch-card-top">
            <span class="arch-badge standalone">
              <i class="fas fa-box-open"></i> {{ product.standaloneArch.badge }}
            </span>
            <span class="arch-status-pill">독립 실행 지원</span>
          </div>

          <h3>{{ product.standaloneArch.title }}</h3>
          <p class="arch-summary">{{ product.standaloneArch.summary }}</p>

          <div class="arch-box">
            <div class="arch-box-title">
              <i class="fas fa-layer-group"></i> 독립 패키지 기본 포함 구성
            </div>
            <ul class="arch-comp-list">
              <li v-for="comp in product.standaloneArch.components" :key="comp">
                <i class="fas fa-check-circle"></i>
                <span>{{ comp }}</span>
              </li>
            </ul>
          </div>

          <div class="arch-highlights">
            <div class="arch-box-title">단독 도입 핵심 강점</div>
            <ul>
              <li v-for="hl in product.standaloneArch.highlights" :key="hl">
                {{ hl }}
              </li>
            </ul>
          </div>
        </article>

        <!-- 2. 에코시스템 연동 카드 -->
        <article class="glass-card arch-card ecosystem-card">
          <div class="arch-card-top">
            <span class="arch-badge ecosystem">
              <i class="fas fa-network-wired"></i> {{ product.ecosystemArch.badge }}
            </span>
            <span class="arch-status-pill accent">표준 MCP · A2A 연동</span>
          </div>

          <h3>{{ product.ecosystemArch.title }}</h3>
          <p class="arch-summary">{{ product.ecosystemArch.summary }}</p>

          <div class="arch-box accent-border">
            <div class="arch-box-title">
              <i class="fas fa-plug"></i> 무수정 자동 연동되는 자매 제품군
            </div>
            <div class="connected-agent-chips">
              <span
                v-for="agent in product.ecosystemArch.connectedAgents"
                :key="agent"
                class="agent-chip"
              >
                <i class="fas fa-link"></i> {{ agent }}
              </span>
            </div>
          </div>

          <div class="arch-highlights">
            <div class="arch-box-title">전사 에코시스템 확장 강점</div>
            <ul>
              <li v-for="hl in product.ecosystemArch.highlights" :key="hl">
                {{ hl }}
              </li>
            </ul>
          </div>
        </article>
      </div>

      <!-- 통합 도입 에디션 3종 요약 -->
      <div class="grid-3" style="margin-top: 2.2rem">
        <div class="glass-card edition-card">
          <span class="section-tag">STANDALONE EDITION</span>
          <h3>{{ product.name }} 단독 구축형</h3>
          <p class="edition-desc">
            타 제품 없이 <strong>{{ product.name }}</strong> 단일 솔루션만 사내 서버나 클라우드에 빠르게 도입하려는 기업에 적합합니다.
          </p>
          <ul class="capability-list" style="margin: 1.2rem 0 1.6rem">
            <li>웹 관리 콘솔 + 백엔드 + DB 올인원 패키지</li>
            <li>기존 사내 시스템과 표준 REST API 즉시 연동</li>
            <li>Docker Compose 기반 즉시 설치 지원</li>
          </ul>
          <a href="#quickstart" class="btn btn-outline" style="width: 100%; justify-content: center">
            데모 및 설치 방법 보기
          </a>
        </div>

        <div class="glass-card edition-card featured-edition">
          <span class="badge-recommend">POPULAR</span>
          <span class="section-tag">ECOSYSTEM CONNECTED</span>
          <h3>{{ product.name }} + SyncVerse 연동형</h3>
          <p class="edition-desc">
            단독 기능을 모두 사용하면서 상위 통합 지휘소(SyncVerse) 및 자매 제품과 부서 간 업무를 자동화합니다.
          </p>
          <ul class="capability-list" style="margin: 1.2rem 0 1.6rem">
            <li>코드 수정 없이 설정만으로 타 에이전트와 협업</li>
            <li>4단계 직무분리(HITL) 결재 및 스냅샷 롤백</li>
            <li>SyncLLM 토큰 비용 절감 및 보안 안전 규칙 통합</li>
          </ul>
          <a
            :href="product.portalProductUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="btn btn-primary"
            style="width: 100%; justify-content: center"
          >
            통합 데모 포털에서 연동 체험
          </a>
        </div>

        <div class="glass-card edition-card">
          <span class="section-tag">ON-PREMISE / AIR-GAPPED</span>
          <h3>폐쇄망 · 맞춤형 구축</h3>
          <p class="edition-desc">
            금융·공공·국방 등 외부 인터넷이 차단된 사내 폐쇄망 및 전용 GPU 인프라 환경에 맞춘 엔터프라이즈 구축입니다.
          </p>
          <ul class="capability-list" style="margin: 1.2rem 0 1.6rem">
            <li>사내 구축형 로컬 LLM (vLLM · Ollama) 호환</li>
            <li>오프라인 컨테이너 이미지 번들 및 기술지원 제공</li>
            <li>기존 레거시 ERP · 그룹웨어 SSO 맞춤 연동</li>
          </ul>
          <a
            href="https://empasy.io/ko/pages/contact.html"
            target="_blank"
            rel="noopener noreferrer"
            class="btn btn-outline"
            style="width: 100%; justify-content: center"
          >
            도입 및 견적 상담 신청
          </a>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { StandaloneProductData } from '@/types/homepage.types';

defineProps<{
  product: StandaloneProductData;
}>();
</script>
