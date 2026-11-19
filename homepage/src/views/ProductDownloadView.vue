<template>
  <div class="portal-view">
    <section class="section" style="padding-top: 7rem">
      <div class="container">
        <div class="section-header text-center">
          <span class="section-tag">LIVE DEMO LAUNCHER &amp; DOWNLOADS</span>
          <h1 class="section-title">{{ product.name }} {{ tr('라이브 데모 런처 · 역할별 계정 · 다운로드 센터', 'Live Demo Launcher, Role Accounts & Downloads') }}</h1>
          <p class="section-desc">
            {{ product.resetScheduleNotice }}
          </p>
        </div>

        <!-- 클라우드 라이브 엔드포인트 즉시 실행 카드 -->
        <div class="grid-3" style="margin-bottom: 2.5rem">
          <div
            v-for="entry in product.entryLinks"
            :key="entry.id"
            class="glass-card"
            style="display: flex; flex-direction: column; justify-content: space-between"
          >
            <div>
              <span class="section-tag">{{ entry.badgeText }}</span>
              <h3 style="margin-bottom: 0.5rem">{{ entry.label }}</h3>
              <p style="color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 1.25rem">
                {{ entry.cloudUrl }}
              </p>
            </div>
            <a
              :href="entry.cloudUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="btn"
              :class="entry.isPrimary ? 'btn-primary' : 'btn-outline'"
              style="width: 100%; text-align: center"
            >
              <i class="fas fa-up-right-from-square" style="margin-right: 6px"></i>
              {{ tr('새 탭에서 바로 열기', 'Open in New Tab') }}
            </a>
          </div>

          <!-- 제안서 및 아키텍처 패키지 다운로드 카드 -->
          <div class="glass-card" style="display: flex; flex-direction: column; justify-content: space-between">
            <div>
              <span class="section-tag">PROPOSAL &amp; BUNDLE</span>
              <h3 style="margin-bottom: 0.5rem">{{ product.name }} {{ tr('공식 제안서 & 배포 번들', 'Proposal PDF & Deployment Bundle') }}</h3>
              <p style="color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 1.25rem">
                {{ tr('제품 아키텍처 제안서 PDF 및 Docker Compose 설정 가이드를 확인합니다.', 'Download the official architecture proposal PDF and Docker Compose bundle guide.') }}
              </p>
            </div>
            <div style="display: flex; gap: 0.5rem">
              <a
                :href="'https://sync.empasy.io' + product.proposalPdfUrl"
                target="_blank"
                rel="noopener noreferrer"
                class="btn btn-outline"
                style="flex: 1; text-align: center"
              >
                <i class="fas fa-file-pdf" style="margin-right: 4px"></i>PDF
              </a>
              <RouterLink :to="localePath('docs')" class="btn btn-outline" style="flex: 1; text-align: center">
                <i class="fas fa-terminal" style="margin-right: 4px"></i>Docker
              </RouterLink>
            </div>
          </div>
        </div>

        <!-- 역할별 데모 계정 원클릭 복사 및 Docker Quickstart 섹션 -->
        <QuickstartDemoDockerSection :product="product" @copy-text="handleCopy" />
      </div>
    </section>

    <div v-if="toastMessage" class="copy-toast" role="status" aria-live="polite">
      <i class="fas fa-check-circle" style="margin-right: 6px"></i>{{ toastMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import QuickstartDemoDockerSection from '@/components/QuickstartDemoDockerSection.vue';
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
const toastMessage = ref('');

async function handleCopy(value: string, label: string): Promise<void> {
  try {
    await navigator.clipboard.writeText(value);
    toastMessage.value = `${label} 복사 완료: ${value}`;
  } catch {
    toastMessage.value = `${label} 복사 완료`;
  }
  window.setTimeout(() => {
    toastMessage.value = '';
  }, 2400);
}
</script>
