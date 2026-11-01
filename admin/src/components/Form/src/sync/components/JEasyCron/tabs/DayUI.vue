<template>
  <div :class="`${prefixCls}-config-list`">
    <a-radio-group v-model:value="type">
      <div class="item">
        <a-radio :value="TypeEnum.unset" v-bind="beforeRadioAttrs">설정되지 않음</a-radio>
        <span class="tip-info">요일과 요일 중 하나만 설정할 수 있습니다.</span>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.every" v-bind="beforeRadioAttrs">일일</a-radio>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.range" v-bind="beforeRadioAttrs">간격</a-radio>
        <span> ~에서 </span>
        <InputNumber v-model:value="valueRange.start" v-bind="typeRangeAttrs" />
        <span> 하루에 </span>
        <InputNumber v-model:value="valueRange.end" v-bind="typeRangeAttrs" />
        <span> 낮 </span>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.loop" v-bind="beforeRadioAttrs">주기</a-radio>
        <span> ~에서 </span>
        <InputNumber v-model:value="valueLoop.start" v-bind="typeLoopAttrs" />
        <span> 하루 시작, 간격 </span>
        <InputNumber v-model:value="valueLoop.interval" v-bind="typeLoopAttrs" />
        <span> 낮 </span>
      </div>
<!--       근무일에는 지원되지 않으며, 오류가 발생할 경우 먼저 숨기도록 하겠습니다. -->



<!--        <InputNumber v-model:value="valueWork" v-bind="typeWorkAttrs" />-->

<!--      </div>-->
      <div class="item">
        <a-radio :value="TypeEnum.last" v-bind="beforeRadioAttrs">마지막 날</a-radio>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.specify" v-bind="beforeRadioAttrs">지정하다</a-radio>
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

      return { ...setup, typeWorkAttrs };
    },
  });
</script>
