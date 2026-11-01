<template>
  <MarkDown v-bind="bindProps" @change="onChange" @get="onGetVditor" />
</template>

<script lang="ts">
  import { computed, defineComponent, watch, nextTick } from 'vue';
  import { MarkDown } from '/@/components/Markdown';
  import { propTypes } from '/@/utils/propTypes';
  import { Form } from 'ant-design-vue';
  export default defineComponent({
    name: 'JMarkdownEditor',
    // attrs 속성을 html 태그에 바인딩하지 마세요.
    inheritAttrs: false,
    components: { MarkDown },
    props: {
      value: propTypes.string.def(''),
      disabled: propTypes.bool.def(false),
    },
    emits: ['change', 'update:value'],
    setup(props, { emit, attrs }) {
      // 마크다운 컴포넌트 인스턴스
      let mdRef: any = null;
      // vditor 컴포넌트 인스턴스
      let vditorRef: any = null;
      // props와 attrs 병합
      const bindProps = computed(() => Object.assign({}, props, attrs));
      const formItemContext = Form.useInjectFormItemContext();
      // onMounted와 동일
      function onGetVditor(instance) {
        mdRef = instance;
        vditorRef = mdRef.getVditor();

        // 모니터링이 비활성화되고 편집기가 비활성화된 상태로 전환됩니다.
        watch(
          () => props.disabled,
          (disabled) => (disabled ? vditorRef.disabled() : vditorRef.enable()),
          { immediate: true }
        );
      }

      // 값 변경 이벤트
      function onChange(value) {
        emit('change', value);
        emit('update:value', value);
        nextTick(() => {
          formItemContext?.onFieldChange();
        });
      }

      return {
        bindProps,

        onChange,
        onGetVditor,
      };
    },
  });
</script>

<style lang="less" scoped></style>
