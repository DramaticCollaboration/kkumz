<template>
  <!--비동기 사전 드롭다운 검색 요소-->
  <a-select
    v-if="async"
    v-bind="attrs"
    v-model:value="selectedAsyncValue"
    showSearch
    labelInValue
    allowClear
    :getPopupContainer="getParentContainer"
    :placeholder="placeholder"
    :filterOption="isDictTable ? false : filterOption"
    :notFoundContent="loading ? undefined : null"
    @focus="handleAsyncFocus"
    @search="loadData"
    @change="handleAsyncChange"
  >
    <template #notFoundContent>
      <a-spin size="small" />
    </template>
    <a-select-option v-for="d in options" :key="d.value" :value="d.value">{{ d.text }}</a-select-option>
  </a-select>
  <!--사전 드롭다운 검색-->
  <a-select
    v-else
    v-model:value="selectedValue"
    v-bind="attrs"
    showSearch
    :getPopupContainer="getParentContainer"
    :placeholder="placeholder"
    :filterOption="filterOption"
    :notFoundContent="loading ? undefined : null"
    :dropdownAlign="{overflow: {adjustY: adjustY }}"
    @change="handleChange"
  >
    <template #notFoundContent>
      <a-spin v-if="loading" size="small" />
    </template>
    <a-select-option v-for="d in options" :key="d.value" :value="d.value">{{ d.text }}</a-select-option>
  </a-select>
</template>

<script lang="ts">
  import { useDebounceFn } from '@vueuse/core';
  import { defineComponent, PropType, ref, reactive, watchEffect, computed, unref, watch, onMounted } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { initDictOptions } from '/@/utils/dict/index';
  import { defHttp } from '/@/utils/http/axios';
  import { debounce } from 'lodash-es';
  import { setPopContainer } from '/@/utils';

  export default defineComponent({
    name: 'JSearchSelect',
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.number]),
      dict: propTypes.string,
      dictOptions: {
        type: Array,
        default: () => [],
      },
      async: propTypes.bool.def(false),
      placeholder: propTypes.string,
      popContainer: propTypes.string,
      pageSize: propTypes.number.def(10),
      getPopupContainer: {
        type: Function,
        default: (node) => node?.parentNode,
      },
      adjustY:propTypes.bool.def(true),
      immediateChange: propTypes.bool.def(false),
      //정렬 정보와 같은 쿼리 매개변수 전달 지원
      params:{
        type: Object,
        default: ()=>{}
      },
    },
    emits: ['change', 'update:value'],
    setup(props, { emit, refs }) {
      const options = ref<any[]>([]);
      const loading = ref(false);
      const attrs = useAttrs({'excludeDefaultKeys': false});
      const selectedValue = ref([]);
      const selectedAsyncValue = ref([]);
      const lastLoad = ref(0);
      // 값을 기준으로 텍스트를 로드할지 여부
      const loadSelectText = ref(true);

      // 사전 테이블인가요?
      const isDictTable = computed(() => {
        if (props.dict) {
          return props.dict.split(',').length >= 2
        }
        return false;
      })

      /**
       * 모니터 사전 코드
       */
      watch(() => props.dict, () => {
        if (!props.dict) {
          return
        }
        if (isDictTable.value) {
          initDictTableData();
        } else {
          initDictCodeData();
        }
      }, {immediate: true});

      /**
       * 가치를 들어라
       */
      watch(
        () => props.value,
        (val) => {
          if (val || val === 0) {
            initSelectValue();
          } else {
            selectedValue.value = [];
            selectedAsyncValue.value = [];
          }
        },
        { immediate: true }
      );
      /**
       * dictOptions 듣기
       */
      watch(
        () => props.dictOptions,
        (val) => {
          if (val && val.length >= 0) {
            options.value = [...val];
          }
        },
        { immediate: true }
      );
      /**
       * 비동기식으로 데이터 쿼리
       */
      const loadData = debounce(async function loadData(value) {
        if (!isDictTable.value) {
          return;
        }
        lastLoad.value += 1;
        const currentLoad = unref(lastLoad);
        options.value = [];
        loading.value = true;
        let keywordInfo = getKeywordParam(value);
        keywordInfo = keywordInfo.replaceAll("'", '');
        // 사전 코드 형식：table,text,code
        defHttp
          .get({
            url: `/api/v1/kkumz/sys/dict/loadDict/${props.dict}`,
            params: { keyword: keywordInfo, pageSize: props.pageSize },
          })
          .then((res) => {
            loading.value = false;
            if (res && res.length > 0) {
              if (currentLoad != unref(lastLoad)) {
                return;
              }
              options.value = res;
            }
          });
      }, 300);
      /**
       * 값 초기화
       */
      function initSelectValue() {
        if (loadSelectText.value === false) {
          loadSelectText.value = true;
          return;
        }
        let { async, value, dict } = props;
        if (async) {
          if (!selectedAsyncValue || !selectedAsyncValue.key || selectedAsyncValue.key !== value) {
            defHttp.get({ url: `/api/v1/kkumz/sys/dict/loadDictItem/${dict}`, params: { key: value } }).then((res) => {
              if (res && res.length > 0) {
                let obj = {
                  key: value,
                  label: res,
                };
                selectedAsyncValue.value = { ...obj };
                if(props.immediateChange == true){
                  emit('change', value);
                }
              }
            });
          }
        } else {
          selectedValue.value = value.toString();
          if(props.immediateChange == true){
            emit('change', value.toString());
          }
        }
      }

      /**
       * 사전 드롭다운 데이터 초기화
       */
      async function initDictTableData() {
        let { dict, async, dictOptions, pageSize } = props;
        if (!async) {
          //사전 항목 컬렉션에 데이터가 있는 경우
          if (dictOptions && dictOptions.length > 0) {
            options.value = dictOptions;
          } else {
            //사전 코드에 따라 사전 배열을 초기화합니다.
            let dictStr = '';
            if (dict) {
              let arr = dict.split(',');
              if (arr[0].indexOf('where') > 0) {
                let tbInfo = arr[0].split('where');
                dictStr = tbInfo[0].trim() + ',' + arr[1] + ',' + arr[2] + ',' + encodeURIComponent(tbInfo[1]);
              } else {
                dictStr = dict;
              }
              //사전 코드에 따라 사전 배열을 초기화합니다.
              const dictData = await initDictOptions(dictStr);
              options.value = dictData;
            }
          }
        } else {
          if (!dict) {
            console.error('검색 컴포넌트에 구성된 사전 항목이 없습니다.');
          } else {
            //또한 처음에 일부 데이터를 비동기식으로 로드합니다.
            loading.value = true;
            let keywordInfo = getKeywordParam('');
            defHttp
              .get({
                url: `/api/v1/kkumz/sys/dict/loadDict/${dict}`,
                params: { pageSize: pageSize, keyword: keywordInfo },
              })
              .then((res) => {
                loading.value = false;
                if (res && res.length > 0) {
                  options.value = res;
                }
              });
          }
        }
      }

      /**
       * 쿼리 데이터 사전
       */
      async function initDictCodeData() {
        options.value = await initDictOptions(props.dict);
      }

      /**
       * 동기 변경 이벤트
       * */
      function handleChange(value) {
        selectedValue.value = value;
        callback();
      }
      /**
       * 비동기 변경 이벤트
       * */
      function handleAsyncChange(selectedObj) {
        if (selectedObj) {
          selectedAsyncValue.value = selectedObj;
          selectedValue.value = selectedObj.key;
        } else {
          selectedAsyncValue.value = null;
          selectedValue.value = null;
          options.value = null;
          loadData('');
        }
        callback();
        // x를 클릭하여 지우려면 loadSelectText를 true로 설정해야 합니다.
        selectedObj ?? (loadSelectText.value = true);
      }
      /**
       *콜백 방법
       * */
      function callback() {
        loadSelectText.value = false;
        emit('change', unref(selectedValue));
        emit('update:value', unref(selectedValue));
      }
      /**
       * 필터 선택 옵션
       */
      function filterOption(input, option) {
        let value = '', label = '';
        try {
          value = option.value;
          label = option.children()[0].children;
        }catch (e) {
          console.log('드롭다운 항목을 가져오지 못했습니다.', e)
        }
        let str = input.toLowerCase();
        return value.toLowerCase().indexOf(str) >= 0 || label.toLowerCase().indexOf(str) >= 0;
      }

      function getParentContainer(node) {
        if (props.popContainer) {
          return setPopContainer(node, props.popContainer);
        } else {
          if (typeof props.getPopupContainer === 'function') {
            return props.getPopupContainer(node);
          } else {
            return node?.parentNode;
          }
        }
      }

      //키워드 매개변수 획득 및 정렬 정보 설정 지원
      function getKeywordParam(text){
        // 정렬 정보가 설정된 경우 정렬 정보를 작성하고 키워드 뒤에 추가해야 합니다. [orderby:create_time,desc]
        if(props.params && props.params.column && props.params.order){
          let temp = text||''

          temp = temp+'[orderby:'+props.params.column+','+props.params.order+']'
          return encodeURI(temp);

        }else{
          return text;
        }
      }
      const handleAsyncFocus = () => {
        options.value.length && initDictCodeData();
        attrs.onFocus?.();
      };
      return {
        attrs,
        options,
        loading,
        isDictTable,
        selectedValue,
        selectedAsyncValue,
        loadData: useDebounceFn(loadData, 800),
        getParentContainer,
        filterOption,
        handleChange,
        handleAsyncChange,
        handleAsyncFocus,
      };
    },
  });
</script>

<style scoped></style>
