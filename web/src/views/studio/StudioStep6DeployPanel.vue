<template>
  <div
    class="canvas-panel"
    :class="{ 'is-busy-locked': busy }"
    :inert="busy || undefined"
  >
    <div class="panel-header">
      <div>
        <span class="step-kicker">STEP 06 · HYBRID BUILD &amp; LIVE RELEASE</span>
        <h2>빌드 &amp; 라이브 사이트 무중단 배포</h2>
        <p>배포 전 최종 구성을 점검하고 공개 범위·커스텀 도메인·릴리즈 노트를 설정해 라이브 사이트로 즉시 발행하세요.</p>
      </div>
      <div class="header-btns" v-if="siteRuntime">
        <button
          type="button"
          class="primary-btn"
          @click="emit('open-live-site', siteRuntime.siteSlug)"
        >
          {{
            siteRuntime.isPublished
              ? `라이브 사이트 바로가기 /s/${siteRuntime.siteSlug} ↗`
              : `내 사이트 미리보기 /s/${siteRuntime.siteSlug} ↗`
          }}
        </button>
      </div>
    </div>

    <div v-if="siteRuntime" class="preflight-summary-bar">
      <div class="preflight-left">
        <div
          v-if="siteRuntime.logoSvg"
          class="preflight-logo"
          v-html="renderLogoWithBrand(siteRuntime.logoSvg, siteRuntime.siteName, previewBrandName)"
        ></div>
        <div class="preflight-meta">
          <strong>
            {{ siteRuntime.siteName }} <code>/s/{{ siteRuntime.siteSlug }}</code>
            <span
              class="step6-status-pill"
              :class="siteRuntime.isPublished ? 'published' : 'draft'"
              data-testid="step6-publish-status-badge"
            >
              {{
                siteRuntime.isPublished
                  ? `전체 공개 배포됨 v${siteRuntime.publishedVersion || siteRuntime.activeVersion}`
                  : '비공개 초안'
              }}
            </span>
          </strong>
          <span>
            {{ formatSiteTypeLabel(siteRuntime.siteType) }}
            · 상품 {{ previewProductsCount }}종
            · 매거진 {{ previewArticlesCount }}건
            · 갤러리 {{ previewGalleryCount }}장
            · 첨부문서 {{ previewDocsCount }}건
          </span>
        </div>
      </div>
      <div class="preflight-actions">
        <button type="button" class="secondary-btn" @click="emit('go-step', 2)">2단계 브랜드·로고 수정</button>
        <button type="button" class="secondary-btn" @click="emit('go-step', 3)">3단계 기획서·화면 수정</button>
        <button type="button" class="secondary-btn" @click="emit('go-step', 4)">4단계 비주얼·상품 수정</button>
      </div>
    </div>

    <div class="deploy-grid" v-if="siteRuntime">
      <div class="deploy-box">
        <h3>배포 방식 및 운영 설정</h3>

        <div class="step6-lifecycle-card" :class="siteRuntime.isPublished ? 'published' : 'draft'">
          <div class="lifecycle-card-main">
            <strong>
              {{
                siteRuntime.isPublished
                  ? `현재 상태: 전체 공개 배포됨 · 배포 버전 v${siteRuntime.publishedVersion || siteRuntime.activeVersion}`
                  : `현재 상태: 비공개 초안 · 작업 버전 v${siteRuntime.activeVersion}`
              }}
            </strong>
            <p v-if="!siteRuntime.isPublished">
              나만 볼 수 있는 사이트 초안이 생성되었습니다. 아래 새 릴리즈 버전 빌드 및 라이브 배포 실행을 누르면 쇼케이스 피드와 다른 사용자에게 공개됩니다.
            </p>
            <p v-else-if="siteRuntime.hasUnpublishedChanges">
              현재 타 사용자에게는 배포 버전 v{{ siteRuntime.publishedVersion }}이 노출되며, 작업 버전 v{{ siteRuntime.activeVersion }}에 미배포 수정본이 있습니다. 재배포를 실행하면 최신 작업본이 공개 반영됩니다.
            </p>
            <p v-else>
              최신 작업 버전 v{{ siteRuntime.activeVersion }}이 쇼케이스 피드 및 라이브 주소 /s/{{ siteRuntime.siteSlug }}에 정상 공개되어 있습니다.
            </p>
          </div>
          <button
            v-if="siteRuntime.isPublished || siteRuntime.visibility === 'PUBLIC'"
            type="button"
            class="secondary-btn"
            data-testid="unpublish-site-btn"
            :disabled="busy"
            @click="emit('unpublish')"
          >
            비공개로 전환
          </button>
        </div>

        <div class="track-cards">
          <div
            class="track-card"
            :class="{ selected: deployForm.deployMode === 'DYNAMIC_SDUI' }"
            @click="deployForm.deployMode = 'DYNAMIC_SDUI'"
          >
            <strong>1초 즉시 오픈</strong>
            <p>별도 빌드 대기 없이 컬러 토큰, 배너 문구, 상품·매거진·예약폼 구성을 1초 만에 라이브 사이트에 즉시 반영합니다.</p>
          </div>
          <div
            class="track-card"
            :class="{ selected: deployForm.deployMode === 'CUSTOM_BUNDLE' }"
            @click="deployForm.deployMode = 'CUSTOM_BUNDLE'"
          >
            <strong>개발자 코드 맞춤 배포</strong>
            <p>5단계 WebIDE에서 직접 수정한 Vue 컴포넌트 코드를 번들로 빌드하여 배포합니다.</p>
          </div>
        </div>

        <div class="step1-meta-grid">
          <div class="field-group">
            <label>쇼케이스 피드 공개 범위</label>
            <select v-model="deployForm.visibility" class="step1-select">
              <option value="PUBLIC">전체 공개 · 쇼케이스 피드 노출</option>
              <option value="UNLISTED">일부 공개 · 링크 보유자만 접속</option>
              <option value="PRIVATE">비공개 · 소유자 전용 초안</option>
            </select>
          </div>
          <div class="field-group">
            <label>연결할 커스텀 도메인</label>
            <input
              v-model="deployForm.customDomain"
              type="text"
              placeholder="예: www.naetga-stay.kr"
            />
          </div>
        </div>

        <div class="field-group">
          <label>릴리즈 노트</label>
          <input
            v-model="deployForm.releaseNote"
            type="text"
            placeholder="예: 2026 시즌 메인 배너 카피 및 시그니처 상품 카탈로그 업데이트"
          />
        </div>

        <button
          type="button"
          class="primary-btn full"
          data-testid="step6-deploy-submit-btn"
          :disabled="busy"
          @click="emit('deploy')"
        >
          {{
            busy
              ? '배포 실행 중...'
              : siteRuntime.isPublished && siteRuntime.hasUnpublishedChanges
                ? `미배포 수정본 v${siteRuntime.activeVersion} 라이브 재배포 실행`
                : '새 릴리즈 버전 빌드 및 라이브 배포 실행'
          }}
        </button>
      </div>

      <div class="release-history-box">
        <h3>배포 버전 히스토리 &amp; 원클릭 롤백</h3>
        <div class="rel-list">
          <div v-for="rel in siteRuntime.releases || []" :key="rel.id" class="rel-item">
            <div class="rel-top">
              <strong>v{{ rel.versionNo }} · {{ formatDeployModeLabel(rel.deployMode) }}</strong>
              <div class="rel-badges">
                <span
                  v-if="siteRuntime.isPublished && rel.versionNo === siteRuntime.publishedVersion"
                  class="active-rel-badge"
                >
                  배포 공개본
                </span>
                <span
                  v-if="rel.versionNo === siteRuntime.activeVersion"
                  class="active-rel-badge working"
                >
                  현재 작업본
                </span>
                <button
                  v-if="rel.versionNo !== siteRuntime.activeVersion"
                  type="button"
                  class="rollback-btn"
                  @click="emit('rollback-release', rel.versionNo)"
                >
                  이 버전으로 롤백
                </button>
              </div>
            </div>
            <p>{{ rel.releaseNote }}</p>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="empty-step-box">
      <strong>오픈할 내 사이트 화면 정보가 아직 준비되지 않았습니다.</strong>
      <p>먼저 내 사이트 화면 자동 제작을 실행하면 즉시 오픈 및 버전 롤백 기능을 사용할 수 있습니다.</p>
      <button type="button" class="primary-btn" :disabled="busy" @click="emit('provision-step4')">
        {{ busy ? '사이트 화면 제작 중...' : '내 사이트 화면 자동 제작 실행' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { formatDeployModeLabel } from '@/api/kkumzApi';
import { formatSiteTypeLabel, renderLogoWithBrand } from './studioFormatters';

defineProps<{
  busy: boolean;
  siteRuntime: any | null;
  previewBrandName: string;
  previewProductsCount: number;
  previewArticlesCount: number;
  previewGalleryCount: number;
  previewDocsCount: number;
  deployForm: {
    deployMode: string;
    releaseNote: string;
    visibility: string;
    customDomain: string;
  };
}>();

const emit = defineEmits<{
  (e: 'open-live-site', siteSlug: string): void;
  (e: 'go-step', stepNo: number): void;
  (e: 'unpublish'): void;
  (e: 'deploy'): void;
  (e: 'rollback-release', versionNo: number): void;
  (e: 'provision-step4'): void;
}>();
</script>

<style scoped src="./studio-view.css"></style>
