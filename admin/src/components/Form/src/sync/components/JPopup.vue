<!--팝업 컴포넌트-->
<template>
  <div class="JPopup components-input-demo-presuffix" v-if="avalid">
    <!--입력 상자-->
    <a-input @click="handleOpen" v-model:value="showText" :placeholder="placeholder" readOnly v-bind="attrs">
      <template #prefix>
        <Icon icon="ant-design:cluster-outlined"></Icon>
      </template>
      
    </a-input>
    <a-form-item>
      <!--팝업 팝업창-->
      <JPopupOnlReportModal
        @register="regModal"
        :code="code"
        :multi="multi"
        :sorter="sorter"
        :groupId="uniqGroupId"
        :param="param"
        :showAdvancedButton="showAdvancedButton"
        @ok="callBack"
        :getContainer="getContainer"
      ></JPopupOnlReportModal>
    </a-form-item>
  </div>
</template>
<script lang="ts">
  import JPopupOnlReportModal from './modal/JPopupOnlReportModal.vue';
  import { defineComponent, ref, reactive, onMounted, watchEffect, watch, computed, unref } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { useMessage } from '/@/hooks/web/useMessage';

  export default defineComponent({
    name: 'JPopup',
    components: {
      JPopupOnlReportModal,
    },
    inheritAttrs: false,
    props: {
      code: propTypes.string.def(''),
      value: propTypes.string.def(''),
      sorter: propTypes.string.def(''),
      width: propTypes.number.def(1200),
      placeholder: propTypes.string.def('선택하세요'),
      multi: propTypes.bool.def(false),
      param: propTypes.object.def({}),
      spliter: propTypes.string.def(','),
      groupId: propTypes.string.def(''),
      formElRef: propTypes.object,
      setFieldsValue: propTypes.func,
      getContainer: propTypes.func,
      fieldConfig: {
        type: Array,
        default: () => [],
      },
      showAdvancedButton: propTypes.bool.def(true),
    },
    emits: ['update:value', 'register', 'popUpChange', 'focus'],
    setup(props, { emit, refs }) {
      const { createMessage } = useMessage();
      const attrs = useAttrs();
      //팝 표시 여부
      const avalid = ref(true);
      const showText = ref('');
      //모델등록
      const [regModal, { openModal }] = useModal();
      //형태 가치
      let {code, fieldConfig } = props;
      //고유 그룹 그룹 ID
      const uniqGroupId = computed(() => (props.groupId ? `${props.groupId}_${code}_${fieldConfig[0]['source']}_${fieldConfig[0]['target']}` : ''));
      /**
       * 팝업 구성 항목이 올바른지 확인
       */
      onMounted(() => {
        if (props.fieldConfig.length == 0) {
          createMessage.error('팝업 매개변수가 올바르게 구성되지 않았습니다!');
          avalid.value = false;
        }
      });
      /**
       * 모니터 값 값
       */
      watch(
        () => props.value,
        (val) => {
          showText.value = val && val.length > 0 ? val.split(props.spliter).join(',') : '';
        },
        { immediate: true }
      );

      /**
       * 팝업 상자 열기
       */
      function handleOpen() {
        emit('focus');
        !attrs.value.disabled && openModal(true);
      }

      /**
       * TODO 지우기
       */
      function handleEmpty() {
        showText.value = '';
      }

      /**
       * 패스 값 콜백
       */
      function callBack(rows) {
        let { fieldConfig } = props;
        //팝업에서 설정한 콜백 값과 일치
        let values = {};
        for (let item of fieldConfig) {
          let val = rows.map((row) => row[item.source]);
          val = val.length == 1 ? val[0] : val.join(',');
          item.target.split(',').forEach((target) => {
            values[target] = val;
          });
        }
        //수신 Form 예시 메소드 할당
        props.formElRef && props.formElRef.setFieldsValue(values);
        //할당 방법 방법 할당에 전달
        props.setFieldsValue && props.setFieldsValue(values);
        emit('popUpChange', values);
      }

      return {
        showText,
        avalid,
        uniqGroupId,
        attrs,
        regModal,
        handleOpen,
        handleEmpty,
        callBack,
      };
    },
  });
</script>
<style lang="less" scoped>
  .JPopup {
    > .ant-form-item {
      display: none;
    }
  }
  .components-input-demo-presuffix .anticon-close-circle {
    cursor: pointer;
    color: #ccc;
    transition: color 0.3s;
    font-size: 12px;
  }

  .components-input-demo-presuffix .anticon-close-circle:hover {
    color: #f5222d;
  }

  .components-input-demo-presuffix .anticon-close-circle:active {
    color: #666;
  }
</style>
