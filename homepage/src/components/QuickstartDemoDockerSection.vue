<template>
  <section id="quickstart" class="section" style="padding-top: 1rem">
    <div class="container">
      <div class="demo-console">
        <div class="console-head">
          <div>
            <span class="section-tag" style="margin-bottom: 0.35rem; display: inline-block">
              LIVE DEMO &amp; QUICKSTART
            </span>
            <h2>{{ product.name }} 라이브 데모 및 설치 가이드</h2>
            <p class="console-sub">
              설치 없이 웹 브라우저에서 바로 체험하거나, 사내 서버/PC에 Docker로 직접 구동해 볼 수 있습니다.
            </p>
          </div>
          <div class="console-head-actions">
            <div class="quickstart-tab-group" role="tablist">
              <button
                type="button"
                role="tab"
                class="mode-switch-btn"
                :class="{ active: activeTab === 'demo' }"
                :aria-selected="activeTab === 'demo'"
                @click="activeTab = 'demo'"
              >
                <i class="fas fa-cloud"></i> 웹 브라우저 라이브 데모 (권장)
              </button>
              <button
                type="button"
                role="tab"
                class="mode-switch-btn"
                :class="{ active: activeTab === 'docker' }"
                :aria-selected="activeTab === 'docker'"
                @click="activeTab = 'docker'"
              >
                <i class="fab fa-docker"></i> 개발자 · Docker 설치 가이드
              </button>
            </div>
          </div>
        </div>

        <!-- 탭 1: 클라우드 라이브 데모 & 주요 확인 포인트 -->
        <div v-if="activeTab === 'demo'">
          <div class="console-grid">
            <!-- 좌측: 클라우드 라이브 데모 접속 -->
            <div class="console-block">
              <h3>
                <i class="fas fa-globe" style="margin-right: 6px"></i>Step 1. 클라우드 데모 콘솔 열기
              </h3>
              <div class="entry-list">
                <div
                  v-for="link in product.entryLinks"
                  :key="link.id"
                  class="entry-item"
                  :class="{ primary: link.isPrimary }"
                >
                  <div>
                    <div class="entry-item-name">
                      {{ link.label }}
                      <span v-if="link.isPrimary" class="recommended-account-pill" style="margin-left: 6px">
                        추천 접속
                      </span>
                    </div>
                    <div class="entry-item-meta">
                      <span class="subdomain-text">{{ link.badgeText }}</span>
                      <span class="reach-badge reach-REACHABLE">
                        <span class="dot"></span>
                        즉시 체험 가능
                      </span>
                    </div>
                  </div>
                  <div class="entry-item-actions">
                    <a
                      :href="link.cloudUrl"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="btn btn-sm"
                      :class="link.isPrimary ? 'btn-primary' : 'btn-outline'"
                    >
                      <i class="fas fa-play" style="margin-right: 5px; font-size: 0.72rem"></i>새 탭에서 열기
                    </a>
                  </div>
                </div>
              </div>

              <!-- 다운로드 에셋이 있는 경우 (예: SyncETA 데스크톱 레코더) -->
              <div v-if="product.downloadAssets && product.downloadAssets.length > 0" style="margin-top: 1.4rem">
                <h3>
                  <i class="fas fa-download" style="margin-right: 6px"></i>설치 패키지 · 데스크톱 앱 다운로드
                </h3>
                <div class="entry-list">
                  <div
                    v-for="asset in product.downloadAssets"
                    :key="asset.id"
                    class="entry-item"
                    :class="{ primary: asset.isPrimary }"
                  >
                    <div style="min-width: 0">
                      <div class="entry-item-name">{{ asset.label }}</div>
                      <div class="entry-item-meta">
                        <span class="subdomain-text">{{ asset.platformBadge }}</span>
                        <span class="reach-badge reach-REACHABLE">
                          <span class="dot"></span>
                          {{ asset.versionBadge }}
                        </span>
                      </div>
                      <p class="asset-desc">{{ asset.description }}</p>
                    </div>
                    <div class="entry-item-actions">
                      <a
                        :href="resolveAssetUrl(asset.downloadUrl)"
                        target="_blank"
                        rel="noopener noreferrer"
                        class="btn btn-sm"
                        :class="asset.isPrimary ? 'btn-primary' : 'btn-outline'"
                      >
                        <i class="fas fa-download" style="margin-right: 6px"></i>
                        {{ asset.buttonLabel || '다운로드' }}
                      </a>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 우측: 기본 관리자 계정 원클릭 복사 + 추가 역할 계정 접기 -->
            <div class="console-block">
              <h3>
                <i class="fas fa-key" style="margin-right: 6px"></i>Step 2. 체험 계정 복사 (클릭 시 복사)
              </h3>
              <div v-if="primaryAccount" class="account-card primary-account-card">
                <div style="display: flex; align-items: center; justify-content: space-between; gap: 0.5rem; flex-wrap: wrap">
                  <div class="account-role">{{ primaryAccount.roleTitle }}</div>
                  <span class="recommended-account-pill">기본 관리자 계정</span>
                </div>
                <p class="account-scope">{{ primaryAccount.scopeSummary }}</p>
                <div class="copy-chip-row">
                  <button
                    type="button"
                    class="copy-chip primary-chip"
                    @click="emit('copyText', primaryAccount.accountId, `${primaryAccount.roleTitle} 아이디`)"
                  >
                    <i class="far fa-copy"></i>
                    <span class="chip-label">아이디</span> {{ primaryAccount.accountId }}
                  </button>
                  <button
                    type="button"
                    class="copy-chip primary-chip"
                    @click="emit('copyText', primaryAccount.demoAccessCode, `${primaryAccount.roleTitle} 비밀번호`)"
                  >
                    <i class="far fa-copy"></i>
                    <span class="chip-label">비밀번호</span> {{ primaryAccount.demoAccessCode }}
                  </button>
                </div>
              </div>

              <details v-if="secondaryAccounts.length > 0" class="extra-accounts-details">
                <summary>
                  <span>
                    <i class="fas fa-users-gear" style="color: #38bdf8; margin-right: 6px"></i>
                    역할별 추가 체험 계정 더보기 ({{ secondaryAccounts.length }}개 — 실무자·결재자·보안담당 등)
                  </span>
                  <i class="fas fa-chevron-down summary-chevron"></i>
                </summary>
                <div class="extra-accounts-list">
                  <div
                    v-for="acc in secondaryAccounts"
                    :key="acc.roleTitle + acc.accountId"
                    class="account-card"
                  >
                    <div class="account-role">{{ acc.roleTitle }}</div>
                    <p class="account-scope">{{ acc.scopeSummary }}</p>
                    <div class="copy-chip-row">
                      <button
                        type="button"
                        class="copy-chip"
                        @click="emit('copyText', acc.accountId, `${acc.roleTitle} 아이디`)"
                      >
                        <span class="chip-label">아이디</span> {{ acc.accountId }}
                      </button>
                      <button
                        type="button"
                        class="copy-chip"
                        @click="emit('copyText', acc.demoAccessCode, `${acc.roleTitle} 비밀번호`)"
                      >
                        <span class="chip-label">비밀번호</span> {{ acc.demoAccessCode }}
                      </button>
                    </div>
                  </div>
                </div>
              </details>
            </div>
          </div>

          <!-- Step 3: 로그인 후 주요 확인 포인트 -->
          <div v-if="quickTryPoints.length > 0" class="quick-try-strip">
            <div class="quick-try-head">
              <span class="quick-try-badge">
                <i class="fas fa-compass"></i> 로그인 후 주요 확인 포인트
              </span>
            </div>
            <div class="quick-try-grid">
              <div v-for="pt in quickTryPoints" :key="pt.step" class="quick-try-item">
                <div class="quick-try-step-row">
                  <span class="quick-try-num">포인트 {{ pt.step }}</span>
                  <span class="quick-try-menu mono">{{ pt.menuPath }}</span>
                </div>
                <div class="quick-try-title">{{ pt.title }}</div>
                <p class="quick-try-tip">{{ pt.tip }}</p>
              </div>
            </div>
          </div>

          <p class="reset-inline-note" style="margin-top: 1.1rem">
            <i class="fas fa-rotate-right" style="margin-right: 4px"></i>
            {{ product.resetScheduleNotice }} (자유롭게 데이터를 등록·수정·승인해 보셔도 안전합니다)
          </p>
        </div>

        <!-- 탭 2: 개발자 · 온프레미스 Docker 설치 가이드 -->
        <div v-else class="docker-tab-panel">
          <div class="console-grid">
            <div class="console-block">
              <h3>
                <i class="fab fa-docker" style="margin-right: 6px"></i>단독 Docker Compose 기동 명령어
              </h3>

              <div class="docker-cmd-card">
                <div class="docker-cmd-head">
                  <span class="docker-cmd-badge">All-in-One 통합 컨테이너 기동 (권장)</span>
                  <button
                    type="button"
                    class="copy-chip"
                    @click="
                      emit(
                        'copyText',
                        `cd ${product.dockerGuide.workdir} && ${product.dockerGuide.devCommand}`,
                        'Docker 통합 기동 명령어'
                      )
                    "
                  >
                    명령어 복사
                  </button>
                </div>
                <pre class="docker-code"><code>$ cd {{ product.dockerGuide.workdir }}
$ {{ product.dockerGuide.devCommand }}</code></pre>
                <p class="docker-cmd-note">
                  백엔드, 프론트엔드 관리 콘솔, PostgreSQL 18.6(6단계 SQL 자동 초기화), Redis가 한 번에 기동됩니다.
                </p>
              </div>

              <div class="docker-cmd-card" style="margin-top: 0.85rem">
                <div class="docker-cmd-head">
                  <span class="docker-cmd-badge secondary">IDE 로컬 디버깅용 인프라 기동</span>
                  <button
                    type="button"
                    class="copy-chip"
                    @click="
                      emit(
                        'copyText',
                        `cd ${product.dockerGuide.workdir} && ${product.dockerGuide.localCommand}`,
                        '로컬 인프라 기동 명령어'
                      )
                    "
                  >
                    명령어 복사
                  </button>
                </div>
                <pre class="docker-code"><code>$ cd {{ product.dockerGuide.workdir }}
$ {{ product.dockerGuide.localCommand }}</code></pre>
              </div>
            </div>

            <div class="console-block">
              <h3>
                <i class="fas fa-network-wired" style="margin-right: 6px"></i>로컬 기동 후 접속 엔드포인트
              </h3>
              <div class="docker-endpoint-box">
                <div
                  v-for="ep in product.dockerGuide.endpoints"
                  :key="ep.label"
                  class="docker-endpoint-row"
                >
                  <div>
                    <strong>{{ ep.label }}</strong>
                    <span class="docker-ep-note">{{ ep.note }}</span>
                  </div>
                  <code class="mono">{{ ep.url }}</code>
                </div>
              </div>

              <div class="docker-cmd-card" style="margin-top: 0.9rem">
                <div class="docker-cmd-head">
                  <span class="docker-cmd-badge">백엔드 기동 상태 확인 (Health Check)</span>
                  <button
                    type="button"
                    class="copy-chip"
                    @click="emit('copyText', product.dockerGuide.healthCheckCmd, '헬스체크 명령어')"
                  >
                    복사
                  </button>
                </div>
                <pre class="docker-code"><code>$ {{ product.dockerGuide.healthCheckCmd }}</code></pre>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { getProductVisualGuide } from '@/data/productVisuals';
import type { StandaloneProductData } from '@/types/homepage.types';

const props = defineProps<{
  product: StandaloneProductData;
}>();

const emit = defineEmits<{
  (e: 'copyText', value: string, label: string): void;
}>();

const activeTab = ref<'demo' | 'docker'>('demo');

const primaryAccount = computed(() => props.product.demoAccounts[0]);
const secondaryAccounts = computed(() => props.product.demoAccounts.slice(1));
const quickTryPoints = computed(
  () => getProductVisualGuide(props.product.id)?.quickTryPoints ?? []
);

function resolveAssetUrl(url: string): string {
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }
  return `https://sync.empasy.io${url}`;
}
</script>
