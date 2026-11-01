<!--커스텀 컴포넌트-->
<template>
  <div>
    <JSelectBiz @change="handleChange" @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs"></JSelectBiz>
    <UserSelectByDepModal :rowKey="rowKey" @register="regModal" @getSelectResult="setValue" v-bind="getBindValue"></UserSelectByDepModal>
  </div>
</template>
<script lang="ts">
  import { unref } from 'vue';
  import UserSelectByDepModal from './modal/UserSelectByDepModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { defineComponent, ref, reactive, watchEffect, watch, provide } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';
  export default defineComponent({
    name: 'JSelectUserByDept',
    components: {
      UserSelectByDepModal,
      JSelectBiz,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      rowKey: {
        type: String,
        default: 'username',
      },
      labelKey: {
        type: String,
        default: 'realname',
      },
    },
    emits: ['options-change', 'change', 'update:value'],
    setup(props, { emit, refs }) {
      const emitData = ref<any[]>();
      //모델등록
      const [regModal, { openModal }] = useModal();
      //형태 가치
      const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      //드롭다운 상자 옵션 값
      const selectOptions = ref<SelectValue>([]);
      //드롭다운 상자에서 선택한 값
      let selectValues = reactive<object>({
        value: [],
        change: false,
      });
      // 에코 데이터가 로드되고 있는지 여부
      const loadingEcho = ref<boolean>(false);
      //selectOptions 보내기, xxxBiz 구성요소로 받기
      provide('selectOptions', selectOptions);
      //selectValues ​​보내기, xxxBiz 컴포넌트로 받기
      provide('selectValues', selectValues);
      //loadingEcho 보내기, xxxBiz 구성요소로 받기
      provide('loadingEcho', loadingEcho);

      const tag = ref(false);
      const attrs = useAttrs();

      /**
       * 컴포넌트 값 듣기
       */
      watchEffect(() => {
        initValue();
      });

      /**
       * selectValues ​​변경 사항 모니터링
       */
      watch(selectValues, () => {
        if (selectValues) {
          state.value = selectValues.value;
        }
      });

      /**
       * 체크인 팝업창
       */
      function handleOpen() {
        tag.value = true;
        openModal(true, {
          isUpdate: false,
        });
      }

      /**
       * 문자열 값을 배열로 변환
       */
      function initValue() {
        let value = props.value ? props.value : [];
        if (value && typeof value === 'string' && value != 'null' && value != 'undefined') {
          state.value = value.split(',');
          selectValues.value = value.split(',');
        } else {
          selectValues.value = value;
        }
      }

      /**
       * 드롭다운 상자의 값 설정
       */
      function setValue(options, values) {
        selectOptions.value = options;
        //emitData.value = values.join(",");
        state.value = values;
        selectValues.value = values;
        emit('update:value', values);
        emit('options-change', options);
      }

      function handleChange(values) {
        emit('update:value', values);
      }
      const getBindValue = Object.assign({}, unref(props), unref(attrs));
      return {
        state,
        attrs,
        selectOptions,
        getBindValue,
        selectValues,
        loadingEcho,
        tag,
        regModal,
        setValue,
        handleOpen,
        handleChange,
      };
    },
  });
</script>
<style lang="less" scoped>
  .j-select-row {
    @width: 82px;

    .left {
      width: calc(100% - @width - 8px);
    }

    .right {
      width: @width;
    }

    .full {
      width: 100%;
    }

    :deep(.ant-select-search__field) {
      display: none !important;
    }
  }
</style>
