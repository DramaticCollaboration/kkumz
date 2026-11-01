<template>
  <a-radio-group v-if="compType === CompTypeEnum.Radio" v-bind="attrs" v-model:value="state" @change="handleChangeRadio">
    <template v-for="item in dictOptions" :key="`${item.value}`">
      <a-radio :value="item.value">
        <span :class="[useDicColor && item.color ? 'colorText' : '']" :style="{ backgroundColor: `${useDicColor && item.color}` }">
          {{ item.label }}
        </span>
      </a-radio>
    </template>
  </a-radio-group>

  <a-radio-group
    v-else-if="compType === CompTypeEnum.RadioButton"
    v-bind="attrs"
    v-model:value="state"
    buttonStyle="solid"
    @change="handleChangeRadio"
  >
    <template v-for="item in dictOptions" :key="`${item.value}`">
      <a-radio-button :value="item.value">
        {{ item.label }}
      </a-radio-button>
    </template>
  </a-radio-group>

  <template v-else-if="compType === CompTypeEnum.Select">
    <!-- 로딩 효과 표시 -->
    <a-input v-if="loadingEcho" readOnly placeholder="로드 중…">
      <template #prefix>
        <LoadingOutlined />
      </template>
    </a-input>
    <a-select
      v-else
      :placeholder="placeholder"
      v-bind="attrs"
      v-model:value="state"
      :filterOption="handleFilterOption"
      :getPopupContainer="getPopupContainer"
      :style="style"
      @change="handleChange"
    >
      <a-select-option v-if="showChooseOption" :value="null">선택하세요…</a-select-option>
      <template v-for="item in dictOptions" :key="`${item.value}`">
        <a-select-option :value="item.value">
          <span
            :class="[useDicColor && item.color ? 'colorText' : '']"
            :style="{ backgroundColor: `${useDicColor && item.color}` }"
            :title="item.label"
          >
            {{ item.label }}
          </span>
        </a-select-option>
      </template>
    </a-select>
  </template>
</template>
<script lang="ts">
  import { defineComponent, PropType, ref, reactive, watchEffect, computed, unref, watch, onMounted, nextTick } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { initDictOptions } from '/@/utils/dict';
  import { get, omit } from 'lodash-es';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { CompTypeEnum } from '/@/enums/CompTypeEnum';
  import { LoadingOutlined } from '@ant-design/icons-vue';

  export default defineComponent({
    name: 'JDictSelectTag',
    inheritAttrs: false,
    components: { LoadingOutlined },
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.number, propTypes.array]),
      dictCode: propTypes.string,
      type: propTypes.string,
      placeholder: propTypes.string,
      stringToNumber: propTypes.bool,
      useDicColor: propTypes.bool.def(false),
      getPopupContainer: {
        type: Function,
        default: (node) => node?.parentNode,
      },
      // 표시 여부【선택하세요】옵션
      showChooseOption: propTypes.bool.def(true),
      // 드롭다운 항목 - 온라인 사용
      options: {
        type: Array,
        default: [],
        required: false,
      },
      style: propTypes.any,
    },
    emits: ['options-change', 'change','update:value'],
    setup(props, { emit, refs }) {
      const dictOptions = ref<any[]>([]);
      const attrs = useAttrs();
      const [state, , , formItemContext] = useRuleFormItem(props, 'value', 'change');
      const getBindValue = Object.assign({}, unref(props), unref(attrs));
      // 에코 데이터가 로드되고 있는지 여부
      const loadingEcho = ref<boolean>(false);
      // 에코를 처음 로드하는 경우에도 처음 로드할 때만 로딩이 표시됩니다.
      let isFirstLoadEcho = true;

      //컴포넌트 유형
      const compType = computed(() => {
        return !props.type || props.type === 'list' ? 'select' : props.type;
      });
      /**
       * 모니터 사전 코드
       */
      watchEffect(() => {
        if (props.dictCode) {
          loadingEcho.value = isFirstLoadEcho;
          isFirstLoadEcho = false;
          initDictData().finally(() => {
            loadingEcho.value = isFirstLoadEcho;
          });
        }
        if (!props.dictCode) {
          dictOptions.value = props.options;
        }
      });

      // 여기에 null 값이 있는 변경 이벤트를 추가합니다. 즉, 컴포넌트가 값을 설정하기 위해 코드를 호출하는 경우입니다.''변경 이벤트도 트리거될 수 있습니다.
      watch(
        () => props.value,
        () => {
          if (props.value === '') {
            emit('change', '');
            nextTick(() => formItemContext.onFieldChange());
          }
        }
      );

      async function initDictData() {
        let { dictCode, stringToNumber } = props;
        //사전 코드에 따라 사전 배열을 초기화합니다.
        const dictData = await initDictOptions(dictCode);
        dictOptions.value = dictData.reduce((prev, next) => {
          if (next) {
            const value = next['value'];
            prev.push({
              label: next['text'] || next['label'],
              value: stringToNumber ? +value : value,
              color: next['color'],
              ...omit(next, ['text', 'value', 'color']),
            });
          }
          return prev;
        }, []);
      }

      function handleChange(e) {
        const { mode } = unref<Recordable>(getBindValue);
        let changeValue:any;
        // 다중 선택 모드와 호환
        
        //하나의 값을 사용하십시오. 그렇지 않으면 상태 값 변경으로 인해 여러 변경이 트리거됩니다.
        if (mode === 'multiple') {
          changeValue = e?.target?.value ?? e;
          // Null 값 필터링
          if (changeValue == null || changeValue === '') {
            changeValue = [];
          }
          if (Array.isArray(changeValue)) {
            changeValue = changeValue.filter((item) => item != null && item !== '');
          }
        } else {
          changeValue = e?.target?.value ?? e;
        }
        state.value = changeValue;

        emit('update:value',changeValue)
        
        // nextTick(() => formItemContext.onFieldChange());
      }

      /** 라디오 값 변경 이벤트 */
      function handleChangeRadio(e) {
        state.value = e?.target?.value ?? e;
        emit('update:value',e?.target?.value ?? e)
      }

      /** 드롭다운 상자의 내용을 검색하는 데 사용됩니다. */
      function handleFilterOption(input, option) {
        if (typeof option.children === 'function') {
          // 라벨에서 검색
          let labelIf = option.children()[0]?.children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
          if (labelIf) {
            return true;
          }
        }
        // 값으로 검색
        return (option.value || '').toString().toLowerCase().indexOf(input.toLowerCase()) >= 0;
      }

      return {
        state,
        compType,
        attrs,
        loadingEcho,
        getBindValue,
        dictOptions,
        CompTypeEnum,
        handleChange,
        handleChangeRadio,
        handleFilterOption,
      };
    },
  });
</script>
<style scoped lang="less">
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
