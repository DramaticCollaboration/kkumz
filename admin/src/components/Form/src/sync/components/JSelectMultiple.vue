<!--사전 드롭다운 다중 선택-->
<template>
  <a-select
    :value="arrayValue"
    @change="onChange"
    mode="multiple"
    :filter-option="filterOption"
    :disabled="disabled"
    :placeholder="placeholder"
    allowClear
    :getPopupContainer="getParentContainer"
  >
    <a-select-option v-for="(item, index) in dictOptions" :key="index" :getPopupContainer="getParentContainer" :value="item.value">
      <span :class="[useDicColor && item.color ? 'colorText' : '']" :style="{ backgroundColor: `${useDicColor && item.color}` }">{{ item.text || item.label }}</span>
    </a-select-option>
  </a-select>
</template>
<script lang="ts">
  import { computed, defineComponent, onMounted, ref, nextTick, watch } from 'vue';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { getDictItems } from '/@/api/common/api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { setPopContainer } from '/@/utils';

  const { createMessage, createErrorModal } = useMessage();
  export default defineComponent({
    name: 'JSelectMultiple',
    components: {},
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      placeholder: {
        type: String,
        default: '선택하세요',
        required: false,
      },
      readOnly: {
        type: Boolean,
        required: false,
        default: false,
      },
      options: {
        type: Array,
        default: () => [],
        required: false,
      },
      triggerChange: {
        type: Boolean,
        required: false,
        default: true,
      },
      spliter: {
        type: String,
        required: false,
        default: ',',
      },
      popContainer: {
        type: String,
        default: '',
        required: false,
      },
      dictCode: {
        type: String,
        required: false,
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      useDicColor: {
        type: Boolean,
        default: false,
      },
    },
    emits: ['options-change', 'change', 'input', 'update:value'],
    setup(props, { emit, refs }) {
      //console.info(props);
      const emitData = ref<any[]>([]);
      const arrayValue = ref<any[]>(!props.value ? [] : props.value.split(props.spliter));
      const dictOptions = ref<any[]>([]);
      const attrs = useAttrs();
      const [state, , , formItemContext] = useRuleFormItem(props, 'value', 'change', emitData);

      onMounted(() => {
        if (props.dictCode) {
          loadDictOptions();
        } else {
          dictOptions.value = props.options;
        }
      });

      watch(
        () => props.value,
        (val) => {
          if (!val) {
            arrayValue.value = [];
          } else {
            arrayValue.value = props.value.split(props.spliter);
          }
        }
      );

      //드롭다운 옵션을 동적으로 변경하는 작업에 적용 가능
      watch(()=>props.options, ()=>{
        if (props.dictCode) {
          // nothing to do
        } else {
          dictOptions.value = props.options;
        }
      });

      function onChange(selectedValue) {
        if (props.triggerChange) {
          emit('change', selectedValue.join(props.spliter));
          emit('update:value', selectedValue.join(props.spliter));
        } else {
          emit('input', selectedValue.join(props.spliter));
          emit('update:value', selectedValue.join(props.spliter));
        }
        nextTick(() => {
          formItemContext?.onFieldChange();
        });
      }

      function getParentContainer(node) {
        if (!props.popContainer) {
          return node?.parentNode;
        } else {
          return setPopContainer(node, props.popContainer);
        }
      }

      // 사전 코드를 기반으로 사전 항목 쿼리
      function loadDictOptions() {
        let temp = props.dictCode || '';
        if (temp.indexOf(',') > 0 && temp.indexOf(' ') > 0) {
          // 인코딩 후에는 공백이 포함되지 않습니다.
          temp = encodeURI(temp);
        }
        getDictItems(temp).then((res) => {
          if (res) {
            dictOptions.value = res.map((item) => ({ value: item.value, label: item.text, color:item.color }));
            //console.info('res', dictOptions.value);
          } else {
            console.error('getDictItems error: : ', res);
            dictOptions.value = [];
          }
        });
      }

      function filterOption(input, option) {
        return option.children()[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      }

      return {
        state,
        attrs,
        dictOptions,
        onChange,
        arrayValue,
        getParentContainer,
        filterOption,
      };
    },
  });
</script>
<style scoped lang='less'>
.colorText{
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
