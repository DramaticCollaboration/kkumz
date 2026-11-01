// 주로 요일과 요일을 함께 사용하는 데 사용됩니다.
import { computed, inject, reactive, ref, unref, watch } from 'vue';
import { propTypes } from '/@/utils/propTypes';

export enum TypeEnum {
  unset = 'UNSET',
  every = 'EVERY',
  range = 'RANGE',
  loop = 'LOOP',
  work = 'WORK',
  last = 'LAST',
  specify = 'SPECIFY',
}

// 공공 소품을 사용하다
export function useTabProps(options) {
  const defaultValue = options?.defaultValue ?? '?';
  return {
    value: propTypes.string.def(defaultValue),
    disabled: propTypes.bool.def(false),
    ...options?.props,
  };
}

// 공개 방출 사용
export function useTabEmits() {
  return ['change', 'update:value'];
}

// 공개 설정 사용
export function useTabSetup(props, context, options) {
  const { emit } = context;
  const prefixCls = inject('prefixCls');
  const defaultValue = ref(options?.defaultValue ?? '?');
  // 유형
  const type = ref(options.defaultType ?? TypeEnum.every);
  const valueList = ref<any[]>([]);
  // 유형에 따라 정의된 값도 다릅니다.
  const valueRange = reactive(options.valueRange);
  const valueLoop = reactive(options.valueLoop);
  const valueWeek = reactive(options.valueWeek);
  const valueWork = ref(options.valueWork);
  const maxValue = ref(options.maxValue);
  const minValue = ref(options.minValue);

  // 다양한 유형을 기준으로 계산된 값
  const computeValue = computed(() => {
    let valueArray: any[] = [];
    switch (type.value) {
      case TypeEnum.unset:
        valueArray.push('?');
        break;
      case TypeEnum.every:
        valueArray.push('*');
        break;
      case TypeEnum.range:
        valueArray.push(`${valueRange.start}-${valueRange.end}`);
        break;
      case TypeEnum.loop:
        valueArray.push(`${valueLoop.start}/${valueLoop.interval}`);
        break;
      case TypeEnum.work:
        valueArray.push(`${valueWork.value}W`);
        break;
      case TypeEnum.last:
        valueArray.push('L');
        break;
      case TypeEnum.specify:
        if (valueList.value.length === 0) {
          valueList.value.push(minValue.value);
        }
        valueArray.push(valueList.value.join(','));
        break;
      default:
        valueArray.push(defaultValue.value);
        break;
    }
    return valueArray.length > 0 ? valueArray.join('') : defaultValue.value;
  });
  // 최소값과 최대값 사이의 값 범위를 지정합니다.
  const specifyRange = computed(() => {
    let range: number[] = [];
    if (maxValue.value != null) {
      for (let i = minValue.value; i <= maxValue.value; i++) {
        range.push(i);
      }
    }
    return range;
  });

  watch(
    () => props.value,
    (val) => {
      if (val !== computeValue.value) {
        parseValue(val);
      }
    },
    { immediate: true }
  );

  watch(computeValue, (v) => updateValue(v));

  function updateValue(value) {
    emit('change', value);
    emit('update:value', value);
  }

  /**
   * parseValue
   * @param value
   */
  function parseValue(value) {
    if (value === computeValue.value) {
      return;
    }
    try {
      if (!value || value === defaultValue.value) {
        type.value = TypeEnum.every;
      } else if (value.indexOf('?') >= 0) {
        type.value = TypeEnum.unset;
      } else if (value.indexOf('-') >= 0) {
        type.value = TypeEnum.range;
        const values = value.split('-');
        if (values.length >= 2) {
          valueRange.start = parseInt(values[0]);
          valueRange.end = parseInt(values[1]);
        }
      } else if (value.indexOf('/') >= 0) {
        type.value = TypeEnum.loop;
        const values = value.split('/');
        if (values.length >= 2) {
          valueLoop.start = value[0] === '*' ? 0 : parseInt(values[0]);
          valueLoop.interval = parseInt(values[1]);
        }
      } else if (value.indexOf('W') >= 0) {
        type.value = TypeEnum.work;
        const values = value.split('W');
        if (!values[0] && !isNaN(values[0])) {
          valueWork.value = parseInt(values[0]);
        }
      } else if (value.indexOf('L') >= 0) {
        type.value = TypeEnum.last;
      } else if (value.indexOf(',') >= 0 || !isNaN(value)) {
        type.value = TypeEnum.specify;
        valueList.value = value.split(',').map((item) => parseInt(item));
      } else {
        type.value = TypeEnum.every;
      }
    } catch (e) {
      type.value = TypeEnum.every;
    }
  }

  const beforeRadioAttrs = computed(() => ({
    class: ['choice'],
    disabled: props.disabled || unref(options.disabled),
  }));
  const inputNumberAttrs = computed(() => ({
    class: ['w60'],
    max: maxValue.value,
    min: minValue.value,
    precision: 0,
  }));
  const typeRangeAttrs = computed(() => ({
    disabled: type.value !== TypeEnum.range || props.disabled || unref(options.disabled),
    ...inputNumberAttrs.value,
  }));
  const typeLoopAttrs = computed(() => ({
    disabled: type.value !== TypeEnum.loop || props.disabled || unref(options.disabled),
    ...inputNumberAttrs.value,
  }));
  const typeSpecifyAttrs = computed(() => ({
    disabled: type.value !== TypeEnum.specify || props.disabled || unref(options.disabled),
    class: ['list-check-item'],
  }));

  return {
    type,
    TypeEnum,
    prefixCls,
    defaultValue,
    valueRange,
    valueLoop,
    valueWeek,
    valueList,
    valueWork,
    maxValue,
    minValue,
    computeValue,
    specifyRange,
    updateValue,
    parseValue,
    beforeRadioAttrs,
    inputNumberAttrs,
    typeRangeAttrs,
    typeLoopAttrs,
    typeSpecifyAttrs,
  };
}
