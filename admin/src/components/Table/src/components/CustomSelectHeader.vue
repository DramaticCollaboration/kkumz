<!-- 사용자 정의 선택 열, 테이블 헤더 구현 부분 -->
<template>
  <template v-if="isRadio">
    <!-- 모두 선택할 수 있는 라디오가 존재하지 않으므로 빈 라벨을 넣으세요. -->
    <span></span>
  </template>
  <template v-else>
    <template v-if="hideSelectAll">
      <span></span>
    </template>
    <a-checkbox :disabled="disabled" v-else :checked="checked" :indeterminate="isHalf" @update:checked="onChange" />
  </template>
</template>
<script setup lang="ts">
  import { computed } from 'vue';

  const props = defineProps({
    isRadio: {
      type: Boolean,
      required: true,
    },
    selectedLength: {
      type: Number,
      required: true,
    },
    // 현재 페이지의 항목 수
    pageSize: {
      type: Number,
      required: true,
    },
    hideSelectAll: {
      type: Boolean,
      default: false,
    },
    disabled: {
      type: Boolean,
      required: true,
    },
  });
  const emit = defineEmits(['select-all']);

  // 모두 선택할 것인지
  const checked = computed(() => {
    if (props.isRadio) {
      return false;
    }
    return props.selectedLength > 0 && props.selectedLength >= props.pageSize;
  });

  // 반선택인가요?
  const isHalf = computed(() => {
    if (props.isRadio) {
      return false;
    }
    return props.selectedLength > 0 && props.selectedLength < props.pageSize;
  });

  function onChange(checked: boolean) {
    emit('select-all', checked);
  }
</script>

<style scoped lang="less"></style>
