<!--역할 선택 컴포넌트-->
<template>
  <div class="JSelectRole">
    <JSelectBiz @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs"></JSelectBiz>
    <a-form-item>
      <RoleSelectModal @register="regModal" @getSelectResult="setValue" v-bind="getBindValue"></RoleSelectModal>
    </a-form-item>
  </div>
</template>
<script lang="ts">
  import RoleSelectModal from './modal/RoleSelectModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { defineComponent, ref, unref, reactive, watchEffect, watch, provide } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';

  export default defineComponent({
    name: 'JSelectRole',
    components: {
      RoleSelectModal,
      JSelectBiz,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      labelKey: {
        type: String,
        default: 'roleName',
      },
      rowKey: {
        type: String,
        default: 'id',
      },
      params: {
        type: Object,
        default: () => {},
      },
    },
    emits: ['options-change', 'change'],
    setup(props, { emit, refs }) {
      const emitData = ref<any[]>();
      //모델등록
      const [regModal, { openModal }] = useModal();
      //형태 가치
      const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      //드롭다운 상자 옵션 값
      const selectOptions = ref<SelectValue>([]);
      //드롭다운 상자에서 선택한 값
      let selectValues = reactive<Recordable>({
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
        props.value && initValue();
        // 쿼리 조건이 재설정되면 인터페이스 표시가 지워집니다.
        if (!props.value) {
          selectValues.value = [];
        }
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
      }
      const getBindValue = Object.assign({}, unref(props), unref(attrs));
      return {
        state,
        attrs,
        getBindValue,
        selectOptions,
        selectValues,
        loadingEcho,
        tag,
        regModal,
        setValue,
        handleOpen,
      };
    },
  });
</script>
<style lang="less" scoped>
  .JSelectRole {
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
