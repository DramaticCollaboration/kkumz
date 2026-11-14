<template>
  <header class="studio-topbar">
    <div class="topbar-left">
      <button type="button" class="back-btn" :disabled="busy" @click="emit('go-feed')">
        ← 쇼케이스로 나가기
      </button>
      <div class="divider"></div>
      <div class="project-selector">
        <span class="selector-label">작업 프로젝트</span>
        <select :value="selectedIdeaId" :disabled="busy" @change="onSelectChange">
          <option value="">+ 새 브랜드 아이디어 만들기</option>
          <option v-for="item in ideas" :key="item.id" :value="item.id">
            {{ item.selectedBrandName || item.title }} · {{ formatSiteTypeLabel(item.siteType) }}
          </option>
        </select>
      </div>
    </div>

    <div class="topbar-right">
      <div class="studio-mode-switch" data-testid="studio-mode-toggle" role="group" aria-label="스튜디오 작업 모드 전환">
        <button
          type="button"
          class="mode-switch-btn"
          data-testid="mode-easy-btn"
          :class="{ active: easyMode }"
          @click="emit('set-mode', true)"
        >
          이지 모드
        </button>
        <button
          type="button"
          class="mode-switch-btn"
          data-testid="mode-pro-btn"
          :class="{ active: !easyMode }"
          @click="emit('set-mode', false)"
        >
          전문가 모드
        </button>
      </div>

      <span v-if="activeSiteSlug" class="slug-pill">내 사이트 주소: /s/{{ activeSiteSlug }}</span>
      <span
        v-if="siteRuntime && !siteRuntime.isPublished"
        class="publish-status-pill draft"
        data-testid="studio-status-draft-badge"
      >
        비공개 초안
      </span>
      <span
        v-else-if="siteRuntime && siteRuntime.isPublished"
        class="publish-status-pill published"
        data-testid="studio-status-published-badge"
      >
        배포 완료 (v{{ siteRuntime.publishedVersion || siteRuntime.activeVersion }})
      </span>
      <span
        v-if="siteRuntime && siteRuntime.isPublished && siteRuntime.hasUnpublishedChanges"
        class="publish-status-pill unpublished-diff"
        data-testid="studio-status-unpublished-diff-badge"
      >
        미배포 수정본 있음 (작업 v{{ siteRuntime.activeVersion }})
      </span>
      <button
        v-if="activeSiteSlug"
        type="button"
        class="live-btn"
        data-testid="studio-open-site-btn"
        :disabled="busy"
        @click="emit('open-live-site', activeSiteSlug)"
      >
        {{ siteRuntime && siteRuntime.isPublished ? '라이브 사이트 열기 ↗' : '내 사이트 미리보기 ↗' }}
      </button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { formatSiteTypeLabel } from './studioFormatters';

defineProps<{
  busy: boolean;
  selectedIdeaId: string;
  ideas: any[];
  easyMode: boolean;
  activeSiteSlug: string;
  siteRuntime: any | null;
}>();

const emit = defineEmits<{
  (e: 'go-feed'): void;
  (e: 'select-idea', ideaId: string): void;
  (e: 'set-mode', isEasy: boolean): void;
  (e: 'open-live-site', siteSlug: string): void;
}>();

function onSelectChange(event: Event) {
  const target = event.target as HTMLSelectElement;
  emit('select-idea', target.value);
}
</script>

<style scoped src="./studio-view.css"></style>
