<template>
  <div
    class="asset-upload-panel"
    data-testid="asset-upload-panel"
  >
    <div class="asset-panel-head">
      <div>
        <div class="asset-title-row">
          <strong>보유 사진 · 로고 · 소개자료 올리기</strong>
          <span class="asset-count-pill">현재 {{ ideaAssets.length }}건 등록됨</span>
        </div>
        <p>
          매장·회사 전경, 상품 사진, 기존 로고 이미지, 안내문·요금표 문서를 올리면 AI가 사이트 곳곳에 알맞게 배치해 드립니다.
        </p>
      </div>
      <div class="asset-head-controls">
        <select
          :value="uploadAssetType"
          class="asset-type-select"
          title="업로드 파일 기본 용도"
          @change="(e: any) => emit('update:uploadAssetType', e.target.value)"
        >
          <option v-for="at in assetTypes" :key="at.code" :value="at.code">
            {{ at.label }}
          </option>
        </select>
        <input
          ref="fileInputRef"
          type="file"
          multiple
          accept="image/*,.svg,.pdf,.doc,.docx,.txt,.md,.csv,.json,.html"
          class="hidden-file-input"
          @change="onFileInputChange"
        />
        <button
          type="button"
          class="secondary-btn"
          :disabled="busy || assetUploading"
          @click="triggerFileSelect"
        >
          {{ assetUploading ? '업로드 중...' : '+ 사진·파일 선택 업로드' }}
        </button>
      </div>
    </div>

    <div
      class="asset-dropzone"
      :class="{ dragging: isDragging }"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="onFileDrop"
      @click="triggerFileSelect"
    >
      <div class="dropzone-inner">
        <span class="dropzone-badge">사진 · 문서 간편 업로드</span>
        <strong>여기로 사진이나 문서 파일을 끌어다 놓거나 클릭해서 올려주세요</strong>
        <span>지원 형식: JPG, PNG, WEBP, GIF, SVG, PDF, DOCX, TXT, CSV, MD · 최대 50MB</span>
      </div>
    </div>

    <div v-if="ideaAssets.length > 0" class="asset-card-grid">
      <div v-for="asset in ideaAssets" :key="asset.id" class="asset-item-card">
        <div class="asset-thumb-box">
          <img
            v-if="isImageAsset(asset)"
            :src="asset.fileUrl"
            :alt="asset.caption || asset.originalName"
            class="asset-thumb-img"
          />
          <div v-else class="asset-doc-icon">
            <span>{{ (asset.fileExt || 'DOC').toUpperCase() }}</span>
            <small>참고문서</small>
          </div>
          <span class="asset-type-chip">{{ formatAssetTypeLabel(asset.assetType) }}</span>
        </div>

        <div class="asset-item-body">
          <div class="asset-name-row">
            <a :href="asset.fileUrl" target="_blank" rel="noopener noreferrer" class="asset-filename" :title="asset.originalName">
              {{ asset.originalName }}
            </a>
            <span class="asset-filesize">{{ formatFileSize(asset.fileSize) }}</span>
          </div>

          <div class="asset-meta-controls">
            <select
              v-model="asset.assetType"
              class="asset-mini-select"
              @change="emit('asset-meta-change', asset)"
            >
              <option
                v-for="at in selectableAssetTypes"
                :key="at.code"
                :value="at.code"
              >
                {{ at.label }}
              </option>
            </select>
            <input
              v-model="asset.caption"
              type="text"
              class="asset-caption-input"
              placeholder="사진·자료 설명 입력"
              @change="emit('asset-meta-change', asset)"
            />
          </div>

          <div v-if="asset.extractedText" class="asset-extracted-box">
            <p>{{ asset.extractedText }}</p>
          </div>

          <div class="asset-card-actions">
            <button
              type="button"
              class="asset-action-btn"
              :disabled="busy"
              @click="emit('apply-asset-text', asset)"
            >
              사이트 설명에 내용 반영
            </button>
            <button
              type="button"
              class="asset-del-btn"
              :disabled="busy"
              @click="emit('remove-asset', asset)"
            >
              삭제
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { isImageAsset, formatFileSize } from './studioFormatters';

const props = defineProps<{
  ideaAssets: any[];
  assetTypes: Array<{ code: string; label: string; desc?: string }>;
  selectableAssetTypes: Array<{ code: string; label: string; desc?: string }>;
  assetTypeLabels: Record<string, string>;
  uploadAssetType: string;
  busy: boolean;
  assetUploading: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:uploadAssetType', val: string): void;
  (e: 'upload-files', files: File[]): void;
  (e: 'asset-meta-change', asset: any): void;
  (e: 'apply-asset-text', asset: any): void;
  (e: 'remove-asset', asset: any): void;
}>();

const fileInputRef = ref<HTMLInputElement | null>(null);
const isDragging = ref(false);

function formatAssetTypeLabel(code: string) {
  if (!code) return '';
  return props.assetTypeLabels[code] || code;
}

function triggerFileSelect() {
  fileInputRef.value?.click();
}

function onFileInputChange(e: Event) {
  const input = e.target as HTMLInputElement;
  const files = input.files ? Array.from(input.files) : [];
  input.value = '';
  if (files.length > 0) {
    emit('upload-files', files);
  }
}

function onFileDrop(e: DragEvent) {
  isDragging.value = false;
  const files = e.dataTransfer?.files ? Array.from(e.dataTransfer.files) : [];
  if (files.length > 0) {
    emit('upload-files', files);
  }
}
</script>

<style scoped src="./studio-view.css"></style>
