<template>
  <a-col v-bind="actionColOpt" v-if="showActionButtonGroup">
    <div style="width: 100%" :style="{ textAlign: actionColOpt.style.textAlign }">
      <FormItem>
        <slot name="resetBefore"></slot>
        <Button type="default" class="mr-2" v-bind="getResetBtnOptions" @click="resetAction" v-if="showResetButton">
          {{ getResetBtnOptions.text }}
        </Button>

        <slot name="submitBefore"></slot>
        <Button type="primary" class="mr-2" v-bind="getSubmitBtnOptions" @click="submitAction" v-if="showSubmitButton">
          {{ getSubmitBtnOptions.text }}
        </Button>

        <slot name="advanceBefore"></slot>
        <Button type="link" size="small" @click="toggleAdvanced" v-if="showAdvancedButton && !hideAdvanceBtn">
          {{ isAdvanced ? $t('component.form.putAway', '닫기') : $t('component.form.unfold', '열기') }}
          <BasicArrow class="ml-1" :expand="!isAdvanced" up />
        </Button>
        <slot name="advanceAfter"></slot>
      </FormItem>
    </div>
  </a-col>
</template>
<script lang="ts">
  import type { ColEx } from '../types/index';
  //import type { ButtonProps } from 'ant-design-vue/es/button/buttonTypes';
  import { defineComponent, computed, unref, PropType } from 'vue';
  import { Form, Col } from 'ant-design-vue';
  import { Button, ButtonProps } from '/@/components/Button';
  import { BasicArrow } from '/@/components/Basic';
  import { useFormContext } from '../hooks/useFormContext';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useAppInject } from '/@/hooks/web/useAppInject';
  import { $t } from '/@/locales';
  import { propTypes } from '/@/utils/propTypes';

  type ButtonOptions = Partial<ButtonProps> & { text: string };

  export default defineComponent({
    name: 'BasicFormAction',
    components: {
      FormItem: Form.Item,
      Button,
      BasicArrow,
      [Col.name]: Col,
    },
    props: {
      showActionButtonGroup: propTypes.bool.def(true),
      showResetButton: propTypes.bool.def(true),
      showSubmitButton: propTypes.bool.def(true),
      showAdvancedButton: propTypes.bool.def(true),
      resetButtonOptions: {
        type: Object as PropType<ButtonOptions>,
        default: () => ({}),
      },
      submitButtonOptions: {
        type: Object as PropType<ButtonOptions>,
        default: () => ({}),
      },
      actionColOptions: {
        type: Object as PropType<Partial<ColEx>>,
        default: () => ({}),
      },
      actionSpan: propTypes.number.def(6),
      isAdvanced: propTypes.bool,
      hideAdvanceBtn: propTypes.bool,
      layout: propTypes.oneOf(['horizontal', 'vertical', 'inline']).def('horizontal'),
    },
    emits: ['toggle-advanced'],
    setup(props, { emit }) {
      const { t } = useI18n();
      const { getIsMobile } = useAppInject();

      const actionColOpt = computed(() => {
        const { showAdvancedButton, actionSpan: span, actionColOptions } = props;
        const actionSpan = 24 - span;
        const advancedSpanObj = showAdvancedButton ? { span: actionSpan < 6 ? 24 : actionSpan } : {};
        const defaultSpan = props.layout == 'inline' ? {} : { span: showAdvancedButton ? 6 : 4 };
        const actionColOpt: Partial<ColEx> = {
          style: { textAlign: 'right' },
          ...defaultSpan,
          ...advancedSpanObj,
          ...actionColOptions,
          ...(unref(getIsMobile) ? { span: 24, xs: 24, sm: 24, md: 24 } : {}),
        };

        return actionColOpt;
      });

      const getResetBtnOptions = computed((): ButtonOptions => {
        return Object.assign(
          {
            text: $t('common.resetText', '초기화'),
            preIcon: 'ic:baseline-restart-alt',
          },
          props.resetButtonOptions
        );
      });

      const getSubmitBtnOptions = computed(() => {
        return Object.assign(
          {},
          {
            text: $t('common.queryText', '검색'),
            preIcon: 'ant-design:search-outlined',
          },
          props.submitButtonOptions
        );
      });

      function toggleAdvanced() {
        emit('toggle-advanced');
      }

      return {
        t,
        $t,
        actionColOpt,
        getResetBtnOptions,
        getSubmitBtnOptions,
        toggleAdvanced,
        ...useFormContext(),
      };
    },
  });
</script>
