<template>
  <a-col v-bind="actionColOpt" v-if="showActionButtonGroup">
    <div style="width: 100%" :style="{ textAlign: actionColOpt.style.textAlign }">
      <FormItem :wrapper-col="{ span: 24 }">
        <div class="inline-flex items-center justify-end flex-nowrap whitespace-nowrap">
          <slot name="submitBefore"></slot>
          <Button type="primary" class="mr-2" v-bind="getSubmitBtnOptions" @click="submitAction" v-if="showSubmitButton">
            {{ getSubmitBtnOptions.text }}
          </Button>

          <slot name="resetBefore"></slot>
          <Button type="default" class="mr-2" v-bind="getResetBtnOptions" @click="resetAction" v-if="showResetButton">
            {{ getResetBtnOptions.text }}
          </Button>

          <slot name="advanceBefore"></slot>
          <Button type="link" size="small" class="!px-1" @click="toggleAdvanced" v-if="showAdvancedButton && !hideAdvanceBtn">
            {{ toggleButtonText }}
            <BasicArrow class="ml-1" :expand="!isAdvanced" up />
          </Button>
          <slot name="advanceAfter"></slot>
        </div>
      </FormItem>
    </div>
  </a-col>
</template>
<script lang="ts">
  import type { ColEx } from '../types/index';
  import { defineComponent, computed, unref, PropType } from 'vue';
  import { Form, Col } from 'ant-design-vue';
  import { Button, ButtonProps } from '/@/components/Button';
  import { BasicArrow } from '/@/components/Basic';
  import { useFormContext } from '../hooks/useFormContext';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useAppInject } from '/@/hooks/web/useAppInject';
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
      hiddenFilterCount: propTypes.number.def(0),
      isAdvanced: propTypes.bool,
      hideAdvanceBtn: propTypes.bool,
      layout: propTypes.oneOf(['horizontal', 'vertical', 'inline']).def('horizontal'),
    },
    emits: ['toggle-advanced'],
    setup(props, { emit }) {
      const { t } = useI18n();
      const { getIsMobile } = useAppInject();

      function resolveText(key: string, fallback: string): string {
        const translated = t(key);
        if (!translated || translated === key || translated.includes('.')) {
          return fallback;
        }
        return translated;
      }

      const actionColOpt = computed(() => {
        const { showAdvancedButton, actionSpan: span, actionColOptions } = props;
        const actionSpan = 24 - span;
        const resolvedSpan = actionSpan < 6 ? 24 : actionSpan;
        if (showAdvancedButton) {
          const {
            xs: _xs,
            sm: _sm,
            md: _md,
            lg: _lg,
            xl: _xl,
            xxl: _xxl,
            span: _ignoredSpan,
            style: customStyle,
            ...restActionCol
          } = (actionColOptions || {}) as Recordable;
          return {
            ...restActionCol,
            ...(props.layout === 'inline' ? {} : { span: resolvedSpan }),
            style: { ...(customStyle || {}), textAlign: 'right' },
            ...(unref(getIsMobile) ? { span: 24, xs: 24, sm: 24, md: 24 } : {}),
          } as Partial<ColEx>;
        }
        const defaultSpan = props.layout == 'inline' ? {} : { span: 4 };
        const actionColOpt: Partial<ColEx> = {
          style: { textAlign: 'right' },
          ...defaultSpan,
          ...actionColOptions,
          ...(unref(getIsMobile) ? { span: 24, xs: 24, sm: 24, md: 24 } : {}),
        };

        return actionColOpt;
      });

      const getResetBtnOptions = computed((): ButtonOptions => {
        return Object.assign(
          {
            text: resolveText('common.resetText', '초기화'),
            preIcon: 'ic:baseline-restart-alt',
          },
          props.resetButtonOptions
        );
      });

      const getSubmitBtnOptions = computed(() => {
        return Object.assign(
          {},
          {
            text: resolveText('common.queryText', '검색'),
            preIcon: 'ant-design:search-outlined',
          },
          props.submitButtonOptions
        );
      });

      const toggleButtonText = computed(() => {
        if (props.isAdvanced) {
          return resolveText('component.form.putAway', '접기');
        }
        const baseText = resolveText('component.form.unfold', '펼치기');
        const count = Number(props.hiddenFilterCount || 0);
        return count > 0 ? `${baseText} (${count})` : baseText;
      });

      function toggleAdvanced() {
        emit('toggle-advanced');
      }

      return {
        t,
        actionColOpt,
        getResetBtnOptions,
        getSubmitBtnOptions,
        toggleButtonText,
        toggleAdvanced,
        ...useFormContext(),
      };
    },
  });
</script>
