<template>
  <a-popover
    trigger="contextmenu"
    v-model:open="visible"
    :overlayClassName="`${prefixCls}-popover`"
    :getPopupContainer="getPopupContainer"
    :placement="position"
  >
    <template #title>
      <span :class="title ? 'title' : 'emptyTitle'">{{ title }}</span>
      <span style="float: right" title="취소">
        <Icon icon="ant-design:close-outlined" @click="visible = false" />
      </span>
    </template>
    <template #content>
      <a-textarea ref="textareaRef" :value="innerValue" :disabled="disabled" :style="textareaStyle" v-bind="attrs" @input="onInputChange" />
    </template>
    <a-input :class="`${prefixCls}-input`" :value="innerValue" :disabled="disabled" v-bind="attrs" @change="onInputChange">
      <template #suffix>
        <Icon icon="ant-design:fullscreen-outlined" @click.stop="onShowPopup" />
      </template>
    </a-input>
  </a-popover>
</template>

<script lang="ts" setup>
  import { computed, nextTick, ref, watch } from 'vue';
  import Icon from '/@/components/Icon/src/Icon.vue';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { propTypes } from '/@/utils/propTypes';
  import { useDesign } from '/@/hooks/web/useDesign';

  const { prefixCls } = useDesign('j-input-popup');
  const props = defineProps({
    // v-model:value
    value: propTypes.string.def(''),
    title: propTypes.string.def(''),
    // 팝업 상자 표시 위치
    position: propTypes.string.def('right'),
    width: propTypes.number.def(300),
    height: propTypes.number.def(150),
    disabled: propTypes.bool.def(false),
    // 팝업 상자에 마운트된 요소 ID
    popContainer: propTypes.oneOfType([propTypes.string, propTypes.func]).def(''),
  });
  const attrs = useAttrs();
  const emit = defineEmits(['change', 'update:value']);

  const visible = ref<boolean>(false);
  const innerValue = ref<string>('');
  // 텍스트 영역 참조 객체
  const textareaRef = ref();
  // 텍스트 영역 스타일
  const textareaStyle = computed(() => ({
    height: `${props.height}px`,
    width: `${props.width}px`,
  }));

  watch(
    () => props.value,
    (value) => {
      if (value && value.length > 0) {
        innerValue.value = value;
      }
    },
    { immediate: true }
  );

  function onInputChange(event) {
    innerValue.value = event.target.value;
    emitValue(innerValue.value);
  }

  async function onShowPopup() {
    visible.value = true;
    await nextTick();
    textareaRef.value?.focus();
  }

  // 팝업 상자에 의해 마운트된 요소 가져오기
  function getPopupContainer(node) {
    if (!props.popContainer) {
      return node?.parentNode;
    } else if (typeof props.popContainer === 'function') {
      return props.popContainer(node);
    } else {
      return document.getElementById(props.popContainer);
    }
  }

  function emitValue(value) {
    emit('change', value);
    emit('update:value', value);
  }
</script>

<style lang="less">
  //noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-j-input-popup';

  .@{prefix-cls} {
    &-popover {
      .ant-popover-title:has(.emptyTitle) {
        border-bottom: none;
      }
    }

    &-input {
      .app-iconify {
        cursor: pointer;
        color: #666666;
        transition: color 0.3s;

        &:hover {
          color: black;
        }
      }
    }
  }
</style>
