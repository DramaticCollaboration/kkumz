<template>
  <Cascader v-bind="attrs" :value="cascaderValue" :options="getOptions" @change="handleChange" />
</template>
<script lang="ts">
  import { defineComponent, PropType, ref, reactive, watchEffect, computed, unref, watch, onMounted } from 'vue';
  import { Cascader } from 'ant-design-vue';
  import { provinceAndCityData, regionData, provinceAndCityDataPlus, regionDataPlus } from '../../utils/areaDataUtil';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { isArray } from '/@/utils/is';

  export default defineComponent({
    name: 'JAreaLinkage',
    components: {
      Cascader,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.object, propTypes.array, propTypes.string]),
      //구, 군 표시 여부
      showArea: propTypes.bool.def(true),
      //그게 다야?
      showAll: propTypes.bool.def(false),
      // 데이터 저장 
      saveCode: propTypes.oneOf(['province', 'city', 'region', 'all']).def('all'),
    },
    emits: ['options-change', 'change', 'update:value'],
    setup(props, { emit, refs }) {
      const emitData = ref<any[]>([]);
      const attrs = useAttrs();
      // const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      const cascaderValue = ref([]);
      const getOptions = computed(() => {
        if (props.showArea && props.showAll) {
          return regionDataPlus;
        }
        if (props.showArea && !props.showAll) {
          return regionData;
        }
        if (!props.showArea && !props.showAll) {
          return provinceAndCityData;
        }
        if (!props.showArea && props.showAll) {
          return provinceAndCityDataPlus;
        }
      });
      /**
       * 모니터 값 변경
       */
      watchEffect(() => {
        if (props.value) {
          initValue();
        } else {
          cascaderValue.value = [];
        }
      });

      /**
       * 문자열 값을 배열로 변환
       */
      function initValue() {
        let value = props.value ? props.value : [];
        if (value && typeof value === 'string' && value != 'null' && value != 'undefined') {
          const arr = value.split(',');
          cascaderValue.value = transform(arr);
        } else if (isArray(value)) {
          if (value.length) {
            cascaderValue.value = transform(value);
          } else {
            cascaderValue.value = [];
          }
        }
      }
      function transform(arr) {
        let result: any = [];
        if (props.saveCode === 'region') {
          // 81 홍콩, 82 마카오
          const regionCode = arr[0];
          if (['82', '81'].includes(regionCode.substring(0, 2))) {
            result = [`${regionCode.substring(0, 2)}0000`, regionCode];
          } else {
            result = [`${regionCode.substring(0, 2)}0000`, `${regionCode.substring(0, 2)}${regionCode.substring(2, 4)}00`, regionCode];
          }
        } else if (props.saveCode === 'city') {
          const cityCode = arr[0];
          result = [`${cityCode.substring(0, 2)}0000`, cityCode];
        } else if (props.saveCode === 'province') {
          const provinceCode = arr[0];
          result = [provinceCode];
        } else {
          result = arr;
        }
        return result;
      }
      function handleChange(arr, ...args) {
        if (arr?.length) {
          let result: any = [];
          if (props.saveCode === 'region') {
            // 어쩌면 두 명뿐일지도（홍콩 선택 시 성, 지역만 선택）
            result = [arr[arr.length - 1]];
          } else if (props.saveCode === 'city') {
            result = [arr[1]];
          } else if (props.saveCode === 'province') {
            result = [arr[0]];
          } else {
            result = arr;
          }
          emit('change', result);
          emit('update:value', result);
        } else {
          emit('change', arr);
          emit('update:value', arr);
        }
        // emitData.value = args;
        // 위에서 변경한 v-model:value로 인해 선택한 데이터가 표시되지 않습니다.
        // state.value = result;
      }
      return {
        cascaderValue,
        attrs,
        regionData,
        getOptions,
        handleChange,
      };
    },
  });
</script>
