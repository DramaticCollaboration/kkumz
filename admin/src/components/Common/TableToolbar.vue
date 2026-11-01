<script setup lang="ts">
import { computed } from 'vue';
import { IconifyIcon } from '@vben/icons';
import { Tooltip } from 'ant-design-vue';

const props = withDefaults(
  defineProps<{
    selectedCount?: number;
    showCreate?: boolean;
    showDelete?: boolean;
    createText?: string;
    deleteText?: string;
    createIcon?: string;
    deleteIcon?: string;
  }>(),
  {
    selectedCount: 0,
    showCreate: true,
    showDelete: true,
    createText: '등록',
    deleteText: '선택 삭제',
    createIcon: 'lucide:plus',
    deleteIcon: 'lucide:trash-2',
  },
);

const emit = defineEmits<{
  (e: 'create'): void;
  (e: 'delete'): void;
  (e: 'refresh'): void;
}>();

const hasSelected = computed(() => (props.selectedCount ?? 0) > 0);
</script>

<template>
  <div class="table-toolbar-wrapper">
    <div class="toolbar-left">
      <a-button v-if="showCreate" type="primary" @click="emit('create')">
        <IconifyIcon :icon="createIcon" class="size-4" />
        <span>{{ createText }}</span>
      </a-button>

      <slot name="extra-actions"></slot>

      <a-button
        v-if="showDelete"
        class="btn-soft-danger"
        :disabled="!hasSelected"
        @click="emit('delete')"
      >
        <IconifyIcon :icon="deleteIcon" class="size-4" />
        <span>{{ deleteText }}<template v-if="hasSelected"> ({{ selectedCount }})</template></span>
      </a-button>
    </div>

    <div class="toolbar-right">
      <slot name="extra-tools"></slot>
      <Tooltip title="새로고침">
        <button type="button" class="btn-ghost-icon" @click="emit('refresh')">
          <IconifyIcon icon="lucide:refresh-cw" class="size-4" />
        </button>
      </Tooltip>
    </div>
  </div>
</template>
