<template>
  <a-input-group>
    <a-input-number v-bind="attrs" :value="beginValue" style="width: calc(50% - 15px)" placeholder="최소값" @change="handleChangeBegin" />
    <a-input style="width: 30px; border-left: 0; pointer-events: none; background-color: #fff" placeholder="~" disabled />
    <a-input-number v-bind="attrs" :value="endValue" style="width: calc(50% - 15px); border-left: 0" placeholder="최대값" @change="handleChangeEnd" />
  </a-input-group>
</template>

<script>
  /**
   * -숫자 범위 쿼리를 사용한 쿼리 조건
   */
  import { ref, watch } from 'vue';
  import { Form } from 'ant-design-vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  
  export default {
    name: 'JRangeNumber',
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
    },
    emits: ['change', 'update:value', 'blur'],
    setup(props, { emit }) {
      const beginValue = ref('');
      const endValue = ref('');
      const attrs = useAttrs();
      const formItemContext = Form.useInjectFormItemContext();
      
      function handleChangeBegin(e) {
        beginValue.value = e;
        emitArray();
      }

      function handleChangeEnd(e) {
        endValue.value = e;
        emitArray();
      }

      function emitArray() {
        let arr = [];
        let begin = beginValue.value || '';
        let end = endValue.value || '';
        arr.push(begin);
        arr.push(end);
        emit('change', arr);
        emit('update:value', arr);
        formItemContext.onFieldChange();
      }

      watch(
        () => props.value,
        (val) => {
          if (val && val.length == 2) {
            beginValue.value = val[0];
            endValue.value = val[1];
          } else {
            beginValue.value = '';
            endValue.value = '';
          }
        }, {immediate: true}
      );
      
      return {
        beginValue,
        endValue,
        handleChangeBegin,
        handleChangeEnd,
        attrs,
      };
    },
  };
</script>

<style lang="less" scoped>
  .ant-input-group {
    display: flex;
    .ant-input-number {
      &:first-child {
        border-top-right-radius: 0;
        border-bottom-right-radius: 0;
      }
      &:last-child {
        border-top-left-radius: 0;
        border-bottom-left-radius: 0;
      }
    }
  }
</style>
