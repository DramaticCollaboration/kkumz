<template>
  <div :class="`${prefixCls}-config-list`">
    <a-radio-group v-model:value="type">
      <div class="item">
        <a-radio :value="TypeEnum.every" v-bind="beforeRadioAttrs">{{ t('appUi.kb95bf8f2') }}</a-radio>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.range" v-bind="beforeRadioAttrs">{{ t('appUi.k38ed2036') }}</a-radio>
        <span> {{ t('appUi.k915f36de') }} </span>
        <InputNumber class="w80" v-model:value="valueRange.start" v-bind="typeRangeAttrs" />
        <span> {{ t('appUi.k2c945abe') }} </span>
        <InputNumber class="w80" v-model:value="valueRange.end" v-bind="typeRangeAttrs" />
        <span> {{ t('appUi.k9e494018') }} </span>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.loop" v-bind="beforeRadioAttrs">{{ t('appUi.k1cd385d7') }}</a-radio>
        <span> {{ t('appUi.k915f36de') }} </span>
        <InputNumber class="w80" v-model:value="valueLoop.start" v-bind="typeLoopAttrs" />
        <span> {{ t('appUi.kf16b516b') }} </span>
        <InputNumber class="w80" v-model:value="valueLoop.interval" v-bind="typeLoopAttrs" />
        <span> {{ t('appUi.k9e494018') }} </span>
      </div>
    </a-radio-group>
  </div>
</template>

<script lang="ts">
  import { useI18n } from '/@/hooks/web/useI18n';
  import { defineComponent } from 'vue';
  import { InputNumber } from 'ant-design-vue';
  import { useTabProps, useTabEmits, useTabSetup } from './useTabMixin';

  export default defineComponent({
    name: 'YearUI',
    components: { InputNumber },
    props: useTabProps({
      defaultValue: '*',
    }),
    emits: useTabEmits(),
    setup(props, context) {
      const { t } = useI18n();
      const nowYear = new Date().getFullYear();
      return { t, ...useTabSetup(props, context, {
        defaultValue: '*',
        minValue: 0,
        valueRange: { start: nowYear, end: nowYear + 100 },
        valueLoop: { start: nowYear, interval: 1 },
      }) };
    },
  });
</script>
