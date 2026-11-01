<template>
  <a-checkbox-group v-bind="attrs" v-model:value="checkboxArray" :options="checkOptions" @change="handleChange">
    <template #label="{label, value}">
      <span :class="[useDicColor && getDicColor(value) ? 'colorText' : '']" :style="{ backgroundColor: `${getDicColor(value)}` }">{{ label }}</span>
    </template>
  </a-checkbox-group>
</template>

<script lang="ts">
  import { defineComponent, computed, watch, watchEffect, ref, unref } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { initDictOptions } from '/@/utils/dict/index';

  export default defineComponent({
    name: 'JCheckbox',
    props: {
      value:propTypes.oneOfType([propTypes.string, propTypes.number]),
      dictCode: propTypes.string,
      useDicColor: propTypes.bool.def(false),
      options: {
        type: Array,
        default: () => [],
      },
    },
    emits: ['change', 'update:value'],
    setup(props, { emit }) {
      const attrs = useAttrs();
      //체크박스 옵션
      const checkOptions = ref<any[]>([]);
      //체크박스 값
      const checkboxArray = ref<any[]>([]);
      /**
       * 가치를 들어라
       */
      watchEffect(() => {
        let temp = props.value;
        if(!temp && temp!==0){
          checkboxArray.value = []
        }else{
          temp = temp + '';
          checkboxArray.value = temp.split(',')
        }
        if (props.value === '' || props.value === undefined) {
          checkboxArray.value = [];
        }
      });
      /**
       * 모니터 사전 코드
       */
      watchEffect(() => {
        props && initOptions();
      });

      /**
       * 초기화 옵션
       */
      async function initOptions() {
        //옵션에 따른 초기화 옵션
        if (props.options && props.options.length > 0) {
          checkOptions.value = props.options;
          return;
        }
        //사전 코드에 따르면 초기화 옵션
        if (props.dictCode) {
          const dictData = await initDictOptions(props.dictCode);
          checkOptions.value = dictData.reduce((prev, next) => {
            if (next) {
              const value = next['value'];
              prev.push({
                label: next['text'],
                value: value,
                color: next['color'],
              });
            }
            return prev;
          }, []);
        }
      }

      /**
       * 이벤트 변경
       * @param $event
       */
      function handleChange($event) {
        emit('update:value', $event.join(','));
        emit('change', $event.join(','));
      }
      const getDicColor = (value) => {
        if (props.useDicColor) {
          const findItem = checkOptions.value.find((item) => item.value == value);
          if (findItem) {
            return findItem.color;
          }
        }
        return null;
      };
      return { checkboxArray, checkOptions, attrs, handleChange, getDicColor };
    },
  });
</script>
<style lang="less" scoped>
  .colorText {
    display: inline-block;
    height: 20px;
    line-height: 20px;
    padding: 0 6px;
    border-radius: 8px;
    background-color: red;
    color: #fff;
    font-size: 12px;
  }
</style>
