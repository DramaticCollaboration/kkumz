<template>
  <div :class="`${prefixCls}-config-list`">
    <a-radio-group v-model:value="type">
      <div class="item">
        <a-radio :value="TypeEnum.unset" v-bind="beforeRadioAttrs">{{ t('appUi.k8437fcce') }}</a-radio>
        <span class="tip-info">{{ t('appUi.k1f200ef5') }}</span>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.range" v-bind="beforeRadioAttrs">{{ t('appUi.k38ed2036') }}</a-radio>
        <span> {{ t('appUi.k915f36de') }} </span>
        <a-select v-model:value="valueRange.start" :options="weekOptions" v-bind="typeRangeSelectAttrs" />
        <span> {{ t('appUi.kf45acf8f') }} </span>
        <a-select v-model:value="valueRange.end" :options="weekOptions" v-bind="typeRangeSelectAttrs" />
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.loop" v-bind="beforeRadioAttrs">{{ t('appUi.k1cd385d7') }}</a-radio>
        <span> {{ t('appUi.k915f36de') }} </span>
        <a-select v-model:value="valueLoop.start" :options="weekOptions" v-bind="typeLoopSelectAttrs" />
        <span> {{ t('appUi.k15040dd3') }} </span>
        <InputNumber v-model:value="valueLoop.interval" v-bind="typeLoopAttrs" />
        <span> {{ t('appUi.kc3d78839') }} </span>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.specify" v-bind="beforeRadioAttrs">{{ t('appUi.k7d4473c2') }}</a-radio>
        <div class="list list-cn">
          <a-checkbox-group v-model:value="valueList">
            <template v-for="opt in weekOptions" :key="i">
              <a-checkbox :value="opt.value" v-bind="typeSpecifyAttrs">{{ opt.label }}</a-checkbox>
            </template>
          </a-checkbox-group>
        </div>
      </div>
    </a-radio-group>
  </div>
</template>

<script lang="ts">
  import { useI18n } from '/@/hooks/web/useI18n';
  import { $t } from '/@/locales';
  import { computed, watch, defineComponent } from 'vue';
  import { InputNumber } from 'ant-design-vue';
  import { useTabProps, useTabEmits, useTabSetup, TypeEnum } from './useTabMixin';

  const WEEK_MAP_EN = {
    '1': 'SUN',
    '2': 'MON',
    '3': 'TUE',
    '4': 'WED',
    '5': 'THU',
    '6': 'FRI',
    '7': 'SAT',
  };

  const WEEK_MAP_CN = {
    '1': $t('appUi.k4a7e4be4'),
    '2': $t('appUi.k1e443e8c'),
    '3': $t('appUi.k85674ecb'),
    '4': $t('appUi.k66dadc9b'),
    '5': $t('appUi.k73259471'),
    '6': $t('appUi.k5054e8b8'),
    '7': $t('appUi.k618ad44e'),
  };

  export default defineComponent({
    name: 'WeekUI',
    components: { InputNumber },
    props: useTabProps({
      defaultValue: '?',
      props: {
        day: { type: String, default: '*' },
      },
    }),
    emits: useTabEmits(),
    setup(props, context) {
      const { t } = useI18n();
      const disabledChoice = computed(() => {
        return (props.day && props.day !== '?') || props.disabled;
      });
      const setup = useTabSetup(props, context, {
        defaultType: TypeEnum.unset,
        defaultValue: '?',
        minValue: 1,
        maxValue: 7,
        // 0,7은 일요일, 1은 월요일을 의미합니다.
        valueRange: { start: 1, end: 7 },
        valueLoop: { start: 2, interval: 1 },
        disabled: disabledChoice,
      });
      const weekOptions = computed(() => {
        let options: { label: string; value: number }[] = [];
        for (let weekKey of Object.keys(WEEK_MAP_CN)) {
          let weekName: string = WEEK_MAP_CN[weekKey];
          options.push({
            value: Number.parseInt(weekKey),
            label: weekName,
          });
        }
        return options;
      });

      const typeRangeSelectAttrs = computed(() => ({
        class: ['w80'],
        disabled: setup.typeRangeAttrs.value.disabled,
      }));

      const typeLoopSelectAttrs = computed(() => ({
        class: ['w80'],
        disabled: setup.typeLoopAttrs.value.disabled,
      }));

      watch(
        () => props.day,
        () => {
          setup.updateValue(disabledChoice.value ? '?' : setup.computeValue.value);
        }
      );

      return {
        t,
        ...setup,
        weekOptions,
        typeLoopSelectAttrs,
        typeRangeSelectAttrs,
        WEEK_MAP_CN,
        WEEK_MAP_EN,
      };
    },
  });
</script>
