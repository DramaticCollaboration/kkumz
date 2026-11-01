<template>
  <div :class="formDisabled ? 'sync-form-container-disabled sync-form-detail-effect' : ''">
    <fieldset :disabled="formDisabled">
      <slot name="detail"></slot>
    </fieldset>
    <slot name="edit"> </slot>
    <fieldset disabled>
      <slot></slot>
    </fieldset>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, watch } from 'vue';

  export default defineComponent({
    name: 'JForm',
    props: {
      disabled: {
        type: Boolean,
        default: false,
        required: false,
      },
    },
    setup(props, { emit }) {
      const formDisabled = ref<boolean>(props.disabled);

      watch(
        () => props.disabled,
        (value) => {
          formDisabled.value = value;
        }
      );

      return {
        formDisabled,
      };
    },
  });
</script>

<style scoped lang="less">
  .sync-form-container-disabled {
    cursor: not-allowed;
  }
  .sync-form-container-disabled fieldset[disabled] {
    -ms-pointer-events: none;
    pointer-events: none;
  }
  .sync-form-container-disabled :deep(.ant-select) {
    -ms-pointer-events: none;
    pointer-events: none;
  }
  // antdv 비활성화 스타일 시작
  // .sync-form-container-disabled :deep(.ant-input-number),
  // .sync-form-container-disabled :deep(.ant-input),
  // .sync-form-container-disabled :deep(.ant-input-password),
  // .sync-form-container-disabled :deep(.ant-select-single .ant-select-selector),
  // .sync-form-container-disabled :deep(.ant-radio-wrapper .ant-radio-checked .ant-radio-inner),
  // .sync-form-container-disabled :deep(.ant-switch),
  // .sync-form-container-disabled :deep(.ant-picker),
  // .sync-form-container-disabled :deep(.ant-select:not(.ant-select-customize-input) .ant-select-selector),
  // .sync-form-container-disabled :deep(.ant-input-affix-wrapper),
  // .sync-form-container-disabled :deep(.tox .tox-toolbar__group),
  // .sync-form-container-disabled :deep(.tox .tox-edit-area__iframe),
  // .sync-form-container-disabled :deep(.vditor-toolbar),
  // .sync-form-container-disabled :deep(.vditor-preview),
  // .sync-form-container-disabled :deep(.sync-tinymce-img-upload) {
  //   background: rgba(51, 51, 51, 0.04);
  // }

  // .sync-form-container-disabled :deep(.ant-radio-wrapper),
  // .sync-form-container-disabled :deep(.ant-checkbox-wrapper),
  // .sync-form-container-disabled :deep(.ant-btn) {
  //   color: rgba(0, 0, 0, 0.65);
  // }
  // .sync-form-container-disabled :deep(.ant-radio-wrapper .ant-radio-inner:after),
  // .sync-form-container-disabled :deep(.ant-checkbox-checked .ant-checkbox-inner) {
  //   background-color: rgba(51, 51, 51, 0.25);
  // }
  // .sync-form-container-disabled :deep(.ant-radio-inner),
  // .sync-form-container-disabled :deep(.ant-checkbox-inner) {
  //   border-color: rgba(51, 51, 51, 0.25) !important;
  // }
  // .sync-form-container-disabled :deep(.ant-input-password > .ant-input),
  // .sync-form-container-disabled :deep(.ant-input-affix-wrapper .ant-input) {
  //   background: none;
  // }
  html[data-theme='light'] {
    .sync-form-detail-effect {
      :deep(.ant-select-selector),
      :deep(.ant-btn),
      :deep(.ant-input),
      :deep(.ant-input-affix-wrapper),
      :deep(.ant-picker),
      :deep(.ant-input-number) {
        color: #606266 !important;
      }
      :deep(.ant-select) {
        color: #606266 !important;
      }
      :deep(.ant-select-selection-item-content),:deep(.ant-select-selection-item),:deep(input) {
        color: #606266 !important;
      }

      :deep(.ant-radio-wrapper),
      :deep(.ant-checkbox-wrapper),
      :deep(.ant-btn) {
        color: rgba(0, 0, 0, 0.65);
      }
      :deep(.ant-radio-wrapper .ant-radio-inner:after),
      :deep(.ant-checkbox-checked .ant-checkbox-inner) {
        color: #606266 !important;
      }
      :deep(.ant-radio-inner),
      :deep(.ant-checkbox-inner) {
        border-color: rgba(51, 51, 51, 0.25) !important;
        background-color: rgba(51, 51, 51, 0.04) !important;
      }
      :deep(.ant-checkbox-checked .ant-checkbox-inner::after), :deep(.ant-tree-checkbox-checked .ant-tree-checkbox-inner::after){
        border-color: rgba(51, 51, 51, 0.25) !important;
      }
      :deep(.ant-switch) {
        background-color: rgba(51, 51, 51, 0.25);
      }
      :deep(.tox .tox-toolbar__group),
      :deep(.tox .tox-edit-area__iframe),
      :deep(.vditor-toolbar),
      :deep(.vditor-preview),
      :deep(.sync-tinymce-img-upload) {
        background: rgba(51, 51, 51, 0.04);
      }
    }
  }

  html[data-theme='dark'] {
    .sync-form-detail-effect {
      :deep(.ant-select-selector),
      :deep(.ant-btn),
      :deep(.ant-input),
      :deep(.ant-input-affix-wrapper),
      :deep(.ant-picker),
      :deep(.ant-input-number) {
        color: rgba(255, 255, 255, 0.25) !important;
        //background-color: rgba(255, 255, 255, 0.08) !important;
      }
      :deep(.ant-select) {
        color: rgba(255, 255, 255, 0.25) !important;
      }
      :deep(.ant-select-selection-item-content),:deep(.ant-select-selection-item),:deep(input) {
        color: rgba(255, 255, 255, 0.25) !important;
      }

      :deep(.ant-radio-wrapper),
      :deep(.ant-checkbox-wrapper){
        color: rgba(255, 255, 255, 0.25);
      }
      :deep(.ant-radio-wrapper .ant-radio-inner:after),
      :deep(.ant-checkbox-checked .ant-checkbox-inner) {
        background-color: rgba(255, 255, 255, 0.08);
      }
      :deep(.ant-radio-inner),
      :deep(.ant-checkbox-inner) {
        border-color: #424242 !important;
        background-color: rgba(255, 255, 255, 0.08);
      }
      :deep(.ant-switch) {
        background-color: rgba(51, 51, 51, 0.25);
        opacity: 0.65;
      }
      :deep(.tox .tox-toolbar__group),
      :deep(.tox .tox-edit-area__iframe),
      :deep(.vditor-toolbar),
      :deep(.vditor-preview),
      :deep(.sync-tinymce-img-upload) {
        background: rgba(51, 51, 51, 0.04);
      }
    }
  }
  // antdv 비활성화 스타일 종료
  .sync-form-container-disabled :deep(.ant-upload-select) {
    cursor: grabbing;
  }
  .sync-form-container-disabled :deep(.ant-upload-list) {
    cursor: grabbing;
  }
  .sync-form-container-disabled fieldset[disabled] :deep(.ant-upload-list){
    // -ms-pointer-events: auto !important;
    // pointer-events: auto !important;
  }
  .sync-form-container-disabled fieldset[disabled] iframe {
    -ms-pointer-events: auto !important;
    pointer-events: auto !important;
  }
  .sync-form-container-disabled :deep(.ant-upload-list-item-actions .anticon-delete),
  .sync-form-container-disabled :deep(.ant-upload-list-item .anticon-close) {
    display: none;
  }
  .sync-form-container-disabled :deep(.vditor-sv) {
    display: none !important;
    background: rgba(51, 51, 51, 0.04);
  }
</style>
