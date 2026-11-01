<template>
  <a-input v-bind="getBindValue" v-model:value="showText" @input="backValue"></a-input>
</template>

<script lang="ts">
  import { defineComponent, PropType, ref, watchEffect, unref, watch, computed } from 'vue';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { propTypes } from '/@/utils/propTypes';
  import { JInputTypeEnum } from '/@/enums/syncEnum.ts';
  import { omit } from 'lodash-es';

  export default defineComponent({
    name: 'JInput',
    inheritAttrs: false,
    props: {
      value: propTypes.string.def(''),
      type: propTypes.string.def(JInputTypeEnum.JINPUT_QUERY_LIKE),
      placeholder: propTypes.string.def(''),
      trim: propTypes.bool.def(false),
    },
    emits: ['change', 'update:value'],
    setup(props, { emit }) {
      const attrs = useAttrs();
      //형태 가치
      const showText = ref('');
      //바인딩 속성
      const getBindValue = computed(() => {
        return omit(Object.assign({}, unref(props), unref(attrs)), ['value']);
      });
      //듣기 유형 변경
      watch(
        () => props.type,
        (val) => {
          val && backValue({ target: { value: unref(showText) } });
        }
      );
      //값 변화 모니터링
      watch(
        () => props.value,
        () => {
          initVal();
        },
        { immediate: true }
      );

      /**
       * 초기화 값
       */
      function initVal() {
        if (!props.value) {
          showText.value = '';
        } else {
          let text = props.value;
          switch (props.type) {
            case JInputTypeEnum.JINPUT_QUERY_LIKE:
              //진풋박스로 전송된 라우팅 파라미터 값이 앞뒤로 1비트씩 가로채는 현상 수정 #1336
              if (text.indexOf('*') != -1) {
                text = text.substring(1, text.length - 1);
              }
              break;
            case JInputTypeEnum.JINPUT_QUERY_NE:
              text = text.substring(1);
              break;
            case JInputTypeEnum.JINPUT_QUERY_GE:
              text = text.substring(2);
              break;
            case JInputTypeEnum.JINPUT_QUERY_LE:
              text = text.substring(2);
              break;
            default:
          }
          showText.value = text;
        }
      }

      /**
       * 반환 값
       */
      function backValue(e) {
        let text = e?.target?.value ?? '';
        if (text && !!props.trim) {
          text = text.trim();
        }
        switch (props.type) {
          case JInputTypeEnum.JINPUT_QUERY_LIKE:
            text = '*' + text + '*';
            break;
          case JInputTypeEnum.JINPUT_QUERY_NE:
            text = '!' + text;
            break;
          case JInputTypeEnum.JINPUT_QUERY_GE:
            text = '>=' + text;
            break;
          case JInputTypeEnum.JINPUT_QUERY_LE:
            text = '<=' + text;
            break;
          default:
        }
        emit('change', text);
        emit('update:value', text);
      }

      return { showText, attrs, getBindValue, backValue };
    },
  });
</script>

<style scoped></style>
