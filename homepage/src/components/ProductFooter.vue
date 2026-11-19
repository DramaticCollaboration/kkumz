<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-grid">
        <div class="footer-brand">
          <div class="footer-brand-top">
            <RouterLink :to="localePath('')" class="footer-logo" aria-label="Empasy Home">
              <img :src="logoUrl" alt="Empasy" class="footer-logo-img" />
            </RouterLink>
            <span class="footer-product-pill">
              <i :class="product.faIconClass"></i>
              {{ product.name }}
            </span>
          </div>
          <p class="footer-desc">
            <strong>{{ product.name }}</strong> — {{ isEn ? product.englishRoleBadge : product.subtitle }}<br />
            {{
              tr(
                '단독 패키지 도입부터 SyncSeries 전사 에이전트 연합까지 유연하게 확장됩니다.',
                'Scales seamlessly from a standalone deployment to an enterprise SyncSeries A2A agent federation.'
              )
            }}
          </p>
          <div class="footer-cta-row">
            <RouterLink :to="localePath('download')" class="btn btn-primary btn-sm">
              <i class="fas fa-play-circle" style="margin-right: 6px"></i>
              {{ tr('데모 계정 · 다운로드', 'Live Demo & Download') }}
            </RouterLink>
            <a
              href="https://sync.empasy.io"
              target="_blank"
              rel="noopener noreferrer"
              class="btn btn-outline btn-sm"
            >
              {{ tr('통합 데모 포털', 'SyncSeries Portal') }}
            </a>
          </div>
        </div>

        <div class="footer-links-group">
          <h4>{{ product.name }} {{ tr('메뉴', 'Navigation') }}</h4>
          <ul>
            <li><RouterLink :to="localePath('')">{{ tr('제품 개요 & 인터랙티브 시뮬레이터', 'Overview & Simulator') }}</RouterLink></li>
            <li><RouterLink :to="localePath('features')">{{ tr('핵심 기능 & 아키텍처 명세', 'Features & Architecture') }}</RouterLink></li>
            <li><RouterLink :to="localePath('pricing')">{{ tr('에디션 비교 & 라이선스 FAQ', 'Editions & Pricing') }}</RouterLink></li>
            <li><RouterLink :to="localePath('docs')">{{ tr('5분 Docker 퀵스타트 & API 문서', 'Quickstart & Docs') }}</RouterLink></li>
            <li><RouterLink :to="localePath('download')">{{ tr('설치 패키지 & 데모 계정 런처', 'Downloads & Demo Accounts') }}</RouterLink></li>
            <li><RouterLink :to="localePath('blog')">{{ tr('엔지니어링 블로그 & 릴리스 노트', 'Tech Blog & Release Notes') }}</RouterLink></li>
          </ul>
        </div>

        <div class="footer-links-group">
          <h4>{{ tr('공식 문서 & 에코시스템', 'Resources & Ecosystem') }}</h4>
          <ul>
            <li>
              <a :href="product.docUrl" target="_blank" rel="noopener noreferrer">
                <i class="fas fa-book"></i> {{ product.name }} {{ tr('기술 문서', 'Technical Docs') }}
              </a>
            </li>
            <li>
              <a href="https://sync.empasy.io" target="_blank" rel="noopener noreferrer">
                <i class="fas fa-desktop"></i> {{ tr('SyncSeries 통합 데모 포털', 'SyncSeries Demo Portal') }}
              </a>
            </li>
            <li>
              <a
                :href="'https://sync.empasy.io' + product.proposalPdfUrl"
                target="_blank"
                rel="noopener noreferrer"
              >
                <i class="fas fa-file-pdf"></i> {{ product.name }} {{ tr('제안서 PDF', 'Proposal PDF') }}
              </a>
            </li>
          </ul>
        </div>
      </div>

      <div class="footer-bottom">
        <address class="footer-corp-info">
          <strong>{{ tr('주식회사 엠파시', 'Empasy IT Corp.') }}</strong>
          <span class="sep">|</span> {{ tr('대표이사 : 오병문', 'CEO : Byungmoon Oh') }}
          <span class="sep">|</span> {{ tr('사업자등록번호', 'Business Reg.') }} : {{ bizRegNo }}<br />
          {{ tr('본점 소재지 : 서울 금천구 가산디지털1로 205-27 2층 208호', 'HQ : 205-27 Gasan digital 1-ro, Geumcheon-gu, Seoul, Korea') }}
          <span class="sep">|</span> {{ tr('도입 문의', 'Contact') }} :
          <a href="mailto:poh@empasy.com" class="footer-contact-link">poh@empasy.com</a>
          <span class="sep">|</span> {{ tr('대표번호', 'Tel') }} :
          <a :href="'tel:' + corpTel" class="footer-tel-link">{{ corpTel }}</a>
        </address>
        <p class="footer-copyright">
          &copy; 2026 Empasy IT Corp. All rights reserved. | {{ product.name }} ({{ product.subdomain }})
        </p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { useHomepageLocale } from '@/composables/useHomepageLocale';
import type { StandaloneProductData } from '@/types/homepage.types';

defineProps<{
  product: StandaloneProductData;
}>();

const { isEn, localePath, tr } = useHomepageLocale();
const logoUrl = `${import.meta.env.BASE_URL}logo-dark.svg`;
const bizRegNo = ['547', '81', '03408'].join('\x2d');
const corpTel = ['0507', '1360', '8169'].join('\x2d');
</script>
