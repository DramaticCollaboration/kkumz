<!--학과선택 구성요소-->
<template>
  <div class="JSelectDept">
    <JSelectBiz  @change="handleSelectChange" @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs"/>
    <a-form-item>
      <DeptSelectModal @register="regModal" @getSelectResult="setValue" v-bind="getBindValue" :multiple="multiple" @close="handleClose"/>
    </a-form-item>
  </div>
</template>
<script lang="ts">
  import DeptSelectModal from './modal/DeptSelectModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { defineComponent, ref, reactive, watchEffect, watch, provide, unref, toRaw } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';
  import { cloneDeep } from 'lodash-es';

  export default defineComponent({
    name: 'JSelectDept',
    components: {
      DeptSelectModal,
      JSelectBiz,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      // 다중 선택 허용 여부, 기본값은 true
      multiple: propTypes.bool.def(true),
    },
    emits: ['options-change', 'change', 'select', 'update:value'],
    setup(props, { emit, refs }) {
      const emitData = ref<any[]>();
      //모델등록
      const [regModal, { openModal }] = useModal();
      //형태 가치
      // const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      //드롭다운 상자 옵션 값
      const selectOptions = ref<SelectValue>([]);
      //드롭다운 상자에서 선택한 값
      let selectValues = reactive<Recordable>({
        value: [],
      });
      let tempSave: any = [];

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
        tempSave = [];
        props.value && initValue();
      });

      watch(
        () => props.value,
        () => {
          initValue();
        }
      );
      /**
       * selectValues ​​변경 사항 모니터링
       */
      // watch(selectValues, () => {
      //   if (selectValues) {
      //     state.value = selectValues.value;
      //   }
      // });
      /**
       * selectOptions 변경 사항 모니터링
       */
      watch(selectOptions, () => {
        if (selectOptions) {
          emit('select', toRaw(unref(selectOptions)), toRaw(unref(selectValues)));
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
        if (value && typeof value === 'string') {
          // state.value = value.split(',');
          selectValues.value = value.split(',');
          tempSave = value.split(',');
        } else {
          // 【VUEN-857】호환 가능한 어레이（라인 편집 사용 문제）
          selectValues.value = value;
          tempSave = cloneDeep(value);
        }
      }

      /**
       * 드롭다운 상자의 값 설정
       */
      function setValue(options, values) {
        selectOptions.value = options;
        //emitData.value = values.join(",");
        // state.value = values;
        selectValues.value = values;
        send(values);
      }
      const getBindValue = Object.assign({}, unref(props), unref(attrs));

      const handleClose = () => {
        if (tempSave.length) {
          selectValues.value = cloneDeep(tempSave);
        } else {
          send(tempSave);
        }
      };
      const handleSelectChange = (values) => {
        tempSave = cloneDeep(values);
        send(tempSave);
      };
      const send = (values) => {
        let result = typeof props.value == 'string' ? values.join(',') : values;
        emit('update:value', result);
        emit('change', result);
      };
      
      return {
        // state,
        attrs,
        selectOptions,
        selectValues,
        loadingEcho,
        getBindValue,
        tag,
        regModal,
        setValue,
        handleOpen,
        handleClose,
        handleSelectChange,
      };
    },
  });
</script>
<style lang="less" scoped>
  .JSelectDept {
    > .ant-form-item {
      display: none;
    }
  }
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
