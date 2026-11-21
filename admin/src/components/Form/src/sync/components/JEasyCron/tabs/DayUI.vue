<template>
  <div :class="`${prefixCls}-config-list`">
    <a-radio-group v-model:value="type">
      <div class="item">
        <a-radio :value="TypeEnum.unset" v-bind="beforeRadioAttrs">{{ t('appUi.k8437fcce') }}</a-radio>
        <span class="tip-info">{{ t('appUi.k1f200ef5') }}</span>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.every" v-bind="beforeRadioAttrs">{{ t('appUi.k51941908') }}</a-radio>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.range" v-bind="beforeRadioAttrs">{{ t('appUi.k38ed2036') }}</a-radio>
        <span> {{ t('appUi.k915f36de') }} </span>
        <InputNumber v-model:value="valueRange.start" v-bind="typeRangeAttrs" />
        <span> {{ t('appUi.kf23bf857') }} </span>
        <InputNumber v-model:value="valueRange.end" v-bind="typeRangeAttrs" />
        <span> {{ t('appUi.k582cb057') }} </span>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.loop" v-bind="beforeRadioAttrs">{{ t('appUi.k1cd385d7') }}</a-radio>
        <span> {{ t('appUi.k915f36de') }} </span>
        <InputNumber v-model:value="valueLoop.start" v-bind="typeLoopAttrs" />
        <span> {{ t('appUi.k11b32aee') }} </span>
        <InputNumber v-model:value="valueLoop.interval" v-bind="typeLoopAttrs" />
        <span> {{ t('appUi.k582cb057') }} </span>
      </div>
<!--       근무일에는 지원되지 않으며, 오류가 발생할 경우 먼저 숨기도록 하겠습니다. -->



<!--        <InputNumber v-model:value="valueWork" v-bind="typeWorkAttrs" />-->

<!--      </div>-->
      <div class="item">
        <a-radio :value="TypeEnum.last" v-bind="beforeRadioAttrs">{{ t('appUi.ka5287c22') }}</a-radio>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.specify" v-bind="beforeRadioAttrs">{{ t('appUi.k7d4473c2') }}</a-radio>
        <div class="list">
          <a-checkbox-group v-model:value="valueList">
            <template v-for="i in specifyRange" :key="i">
              <a-checkbox :value="i" v-bind="typeSpecifyAttrs">{{ i }}</a-checkbox>
            </template>
          </a-checkbox-group>
        </div>
      </div>
    </a-radio-group>
  </div>
</template>

<script lang="ts">
  import { useI18n } from '/@/hooks/web/useI18n';
  import { computed, defineComponent, watch } from 'vue';
  import { InputNumber } from 'ant-design-vue';
  import { TypeEnum, useTabEmits, useTabProps, useTabSetup } from './useTabMixin';

  export default defineComponent({
    name: 'DayUI',
    components: { InputNumber },
    props: useTabProps({
      defaultValue: '*',
      props: {
        week: { type: String, default: '?' },
      },
    }),
    emits: useTabEmits(),
    setup(props, context) {
      const { t } = useI18n();
      const disabledChoice = computed(() => {
        return (props.week && props.week !== '?') || props.disabled;
      });
      const setup = useTabSetup(props, context, {
        defaultValue: '*',
        valueWork: 1,
        minValue: 1,
        maxValue: 31,
        valueRange: { start: 1, end: 31 },
        valueLoop: { start: 1, interval: 1 },
        disabled: disabledChoice,
      });
      const typeWorkAttrs = computed(() => ({
        disabled: setup.type.value !== TypeEnum.work || props.disabled || disabledChoice.value,
        ...setup.inputNumberAttrs.value,
      }));

      watch(
        () => props.week,
        () => {
          setup.updateValue(disabledChoice.value ? '?' : setup.computeValue.value);
        }
      );

      return {
        t, ...setup, typeWorkAttrs };
    },
  });
</script>
