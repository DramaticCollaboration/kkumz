<script setup lang="ts">
import { IconifyIcon } from '@vben/icons';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { $t } from '/@/locales';
  const { t } = useI18n();

withDefaults(
  defineProps<{
    showEdit?: boolean;
    showDelete?: boolean;
    editText?: string;
    deleteText?: string;
    editIcon?: string;
    deleteIcon?: string;
  }>(),
  {
    showEdit: true,
    showDelete: true,
    editText: (() => $t('appUi.ke1407b51')) as unknown as string,
    deleteText: (() => $t('appUi.kfc81e222')) as unknown as string,
  },
);

const emit = defineEmits<{
  (e: 'edit'): void;
  (e: 'delete'): void;
}>();
</script>

<template>
  <div class="inline-flex items-center gap-1.5 text-xs">
    <slot name="before"></slot>

    <button
      v-if="showEdit"
      type="button"
      class="btn-row-action edit"
      @click="emit('edit')"
    >
      <IconifyIcon v-if="editIcon" :icon="editIcon" class="size-3.5" />
      <span>{{ editText }}</span>
    </button>

    <span v-if="showEdit && showDelete" class="text-slate-300 select-none">|</span>

    <slot name="middle"></slot>

    <button
      v-if="showDelete"
      type="button"
      class="btn-row-action delete"
      @click="emit('delete')"
    >
      <IconifyIcon v-if="deleteIcon" :icon="deleteIcon" class="size-3.5" />
      <span>{{ deleteText }}</span>
    </button>

    <slot name="after"></slot>
  </div>
</template>
