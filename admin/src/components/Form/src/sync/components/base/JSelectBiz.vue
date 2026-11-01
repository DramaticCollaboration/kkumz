<template>
  <div>
    <a-row class="j-select-row" type="flex" :gutter="8">
      <a-col class="left" :class="{ full: !showButton }">
        <!-- 로딩 효과 표시 -->
        <a-input v-if="loading" readOnly placeholder="로드 중…">
          <template #prefix>
            <LoadingOutlined />
          </template>
        </a-input>
        <a-select
          v-else
          ref="select"
          v-model:value="selectValues.value"
          :placeholder="placeholder"
          :mode="multiple"
          :open="false"
          :disabled="disabled"
          :options="options"
          :maxTagCount="maxTagCount"
          @change="handleChange"
          style="width: 100%"
          @click="!disabled && openModal(false)"
          v-bind="attrs"
        ></a-select>
      </a-col>
      <a-col v-if="showButton" class="right">
        <a-button v-if="buttonIcon" :preIcon="buttonIcon" type="primary" @click="openModal(true)" :disabled="disabled">선택</a-button>
        <a-button v-else type="primary" @click="openModal(true)" :disabled="disabled">선택</a-button>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, inject, reactive } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { LoadingOutlined } from '@ant-design/icons-vue';

  export default defineComponent({
    name: 'JSelectBiz',
    components: { LoadingOutlined },
    inheritAttrs: false,
    props: {
      showButton: propTypes.bool.def(true),
      disabled: propTypes.bool.def(false),
      placeholder: {
        type: String,
        default: '선택하세요',
      },
      // 다중 선택 지원 여부, 기본값은 true
      multiple: {
        type: String,
        default: 'multiple',
      },
      // 로드 중
      loading: propTypes.bool.def(false),
      // 최대 몇 개의 태그를 표시할 수 있나요?
      maxTagCount: propTypes.number,
      // buttonIcon
      buttonIcon: propTypes.string.def(''),
    },
    emits: ['handleOpen', 'change'],
    setup(props, { emit, refs }) {
      //드롭다운 상자 옵션 수신
      const options = inject('selectOptions') || ref([]);
      //선택한 값 받기
      const selectValues = inject('selectValues') || ref({});
      const attrs = useAttrs();

      /**
       * 팝업 상자 열기
       */
      function openModal(isButton) {
        if (props.showButton && isButton) {
          emit('handleOpen');
        }
        if (!props.showButton && !isButton) {
          emit('handleOpen');
        }
      }

      /**
       * 드롭다운 박스 값 변경 이벤트
       */
      function handleChange(value) {
        selectValues.value = value;
        selectValues.change = true;
        emit('change', value);
      }

      return {
        attrs,
        selectValues,
        options,
        handleChange,
        openModal,
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
