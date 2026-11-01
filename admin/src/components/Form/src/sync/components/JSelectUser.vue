<!--커스텀 컴포넌트-->
<template>
  <div class="JselectUser">
    <JSelectBiz @change="handleSelectChange" @handleOpen="handleOpen" :loading="loadingEcho" v-bind="attrs"></JSelectBiz>
    <a-form-item>
      <UserSelectModal
        :rowKey="rowKey"
        @register="regModal"
        @getSelectResult="setValue"
        v-bind="getBindValue"
        :excludeUserIdList="excludeUserIdList"
        @close="handleClose"
      />
    </a-form-item>
  </div>
</template>
<script lang="ts">
  import { unref } from 'vue';
  import UserSelectModal from './modal/UserSelectModal.vue';
  import JSelectBiz from './base/JSelectBiz.vue';
  import { defineComponent, ref, reactive, watchEffect, watch, provide } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { SelectValue } from 'ant-design-vue/es/select';
  import { cloneDeep } from 'lodash-es';
  export default defineComponent({
    name: 'JSelectUser',
    components: {
      UserSelectModal,
      JSelectBiz,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      labelKey: {
        type: String,
        default: 'realname',
      },
      rowKey: {
        type: String,
        default: 'username',
      },
      params: {
        type: Object,
        default: () => {},
      },
      //사용자 ID 모음 제외
      excludeUserIdList:{
        type: Array,
        default: () => [],
      }
    },
    emits: ['options-change', 'change', 'update:value'],
    setup(props, { emit }) {
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
        change: false,
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
        // 쿼리 조건이 재설정되면 인터페이스 표시가 지워지지 않습니다.
        if (!props.value) {
          selectValues.value = [];
        }
      });

      /**
       * selectValues ​​변경 사항 모니터링
       */
      // watch(selectValues, () => {
      //   if (selectValues) {
      //     state.value = selectValues.value;
      //   }
      // });

      const excludeUserIdList = ref<any>([]);
      
      /**
       *excludeUserIdList를 모니터링해야 합니다. 그렇지 않으면 모달이 이를 얻을 수 없습니다.
       */ 
      watch(()=>props.excludeUserIdList,(data)=>{
        excludeUserIdList.value = data;
      },{ immediate: true })
      
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
        let result = typeof props.value == "string" ? values.join(',') : values;
        emit('update:value', result);
        emit('change', result);
      };
      return {
        // state,
        attrs,
        selectOptions,
        getBindValue,
        selectValues,
        loadingEcho,
        tag,
        regModal,
        setValue,
        handleOpen,
        excludeUserIdList,
        handleClose,
        handleSelectChange,
      };
    },
  });
</script>
<style lang="less" scoped>
  .JselectUser {
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
