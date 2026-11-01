<template>
  <Tinymce v-bind="bindProps" @change="onChange" />
</template>

<script lang="ts">
  import { computed, defineComponent, nextTick } from 'vue';

  import { Tinymce } from '/@/components/Tinymce';
  import { propTypes } from '/@/utils/propTypes';
  import { Form } from 'ant-design-vue';

  export default defineComponent({
    name: 'JEditor',
    // attrs 속성을 html 태그에 바인딩하지 마세요.
    inheritAttrs: false,
    components: { Tinymce },
    props: {
      value: propTypes.string.def(''),
      disabled: propTypes.bool.def(false),
    },
    emits: ['change', 'update:value'],
    setup(props, { emit, attrs }) {
      // props와 attrs 병합
      const bindProps = computed(() => Object.assign({}, props, attrs));
      const formItemContext = Form.useInjectFormItemContext();
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
      };
    },
  });
</script>

<style lang="less" scoped></style>
