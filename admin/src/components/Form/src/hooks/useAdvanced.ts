import type { ColEx } from '../types';
import type { AdvanceState } from '../types/hooks';
import type { ComputedRef, Ref } from 'vue';
import type { FormProps, FormSchema } from '../types/form';
import { computed, unref, watch } from 'vue';
import { isBoolean, isFunction, isNumber, isObject } from '/@/utils/is';
import { useBreakpoint } from '/@/hooks/event/useBreakpoint';
import { useDebounceFn } from '@vueuse/core';

const BASIC_COL_LEN = 24;

interface UseAdvancedContext {
  advanceState: AdvanceState;
  emit: EmitType;
  getProps: ComputedRef<FormProps>;
  getSchema: ComputedRef<FormSchema[]>;
  formModel: Recordable;
  defaultValueRef: Ref<Recordable>;
}

export default function ({ advanceState, emit, getProps, getSchema, formModel, defaultValueRef }: UseAdvancedContext) {
  const { realWidthRef, screenEnum, screenRef } = useBreakpoint();

  const getEmptySpan = computed((): number => {
    if (!advanceState.isAdvanced) {
      return 0;
    }
    // For some special cases, you need to manually specify additional blank lines
    const emptySpan = unref(getProps).emptySpan || 0;

    if (isNumber(emptySpan)) {
      return emptySpan;
    }
    if (isObject(emptySpan)) {
      const { span = 0 } = emptySpan;
      const screen = unref(screenRef) as string;

      const screenSpan = (emptySpan as any)[screen.toLowerCase()];
      return screenSpan || span || 0;
    }
    return 0;
  });

  const debounceUpdateAdvanced = useDebounceFn(updateAdvanced, 30);

  watch(
    [() => unref(getSchema), () => advanceState.isAdvanced, () => unref(realWidthRef)],
    () => {
      const { showAdvancedButton } = unref(getProps);
      if (showAdvancedButton) {
        debounceUpdateAdvanced();
      }
    },
    { immediate: true }
  );

  function getAdvanced(itemCol: Partial<ColEx>, itemColSum = 0, isLastAction = false, index = 0) {
    const width = unref(realWidthRef);

    const mdWidth =
      parseInt(itemCol.md as string) || parseInt(itemCol.xs as string) || parseInt(itemCol.sm as string) || (itemCol.span as number) || BASIC_COL_LEN;

    const lgWidth = parseInt(itemCol.lg as string) || mdWidth;
    const xlWidth = parseInt(itemCol.xl as string) || lgWidth;
    const xxlWidth = parseInt(itemCol.xxl as string) || xlWidth;
    if (width <= screenEnum.LG) {
      itemColSum += mdWidth;
    } else if (width < screenEnum.XL) {
      itemColSum += lgWidth;
    } else if (width < screenEnum.XXL) {
      itemColSum += xlWidth;
    } else {
      itemColSum += xxlWidth;
    }

    let autoAdvancedCol = unref(getProps).autoAdvancedCol ?? 3;

    if (isLastAction) {
      advanceState.hideAdvanceBtn = unref(getSchema).length <= autoAdvancedCol;
      /* if (itemColSum <= BASIC_COL_LEN * 2) {
        // 2줄 이하인 경우 축소 및 확장 버튼이 표시되지 않습니다.
        advanceState.hideAdvanceBtn = true;
        advanceState.isAdvanced = true;
      } else */
      if (itemColSum > BASIC_COL_LEN * 2 && itemColSum <= BASIC_COL_LEN * (unref(getProps).autoAdvancedLine || 3)) {
        advanceState.hideAdvanceBtn = false;

        // 기본적으로 3줄을 초과하면 접힙니다.
      } else if (!advanceState.isLoad) {
        advanceState.isLoad = true;
        advanceState.isAdvanced = !advanceState.isAdvanced;
        if (unref(getSchema).length > autoAdvancedCol) {
          advanceState.hideAdvanceBtn = false;
          advanceState.isAdvanced = false;
        }
      }
      return { isAdvanced: advanceState.isAdvanced, itemColSum };
    }
    if (itemColSum > BASIC_COL_LEN * (unref(getProps).alwaysShowLines || 1)) {
      return { isAdvanced: advanceState.isAdvanced, itemColSum };
    } else if (!advanceState.isAdvanced && index + 1 > autoAdvancedCol) {
      // 현재 접힌 상태이고 현재 열 아래 첨자가 있는 경우 > autoAdvancedCol, 그냥 숨기기
      return { isAdvanced: false, itemColSum };
    } else {
      // The first line is always displayed
      return { isAdvanced: true, itemColSum };
    }
  }

  function updateAdvanced() {
    let itemColSum = 0;
    let realItemColSum = 0;
    const { baseColProps = {} } = unref(getProps);

    const schemas = unref(getSchema);
    for (let i = 0; i < schemas.length; i++) {
      const schema = schemas[i];
      const { show, colProps } = schema;
      let isShow = true;

      if (isBoolean(show)) {
        isShow = show;
      }

      if (isFunction(show)) {
        isShow = show({
          schema: schema,
          model: formModel,
          field: schema.field,
          values: {
            ...unref(defaultValueRef),
            ...formModel,
          },
        });
      }

      if (isShow && (colProps || baseColProps)) {
        const { itemColSum: sum, isAdvanced } = getAdvanced({ ...baseColProps, ...colProps }, itemColSum, false, i);

        itemColSum = sum || 0;
        if (isAdvanced) {
          realItemColSum = itemColSum;
        }
        schema.isAdvanced = isAdvanced;
      }
    }

    advanceState.actionSpan = (realItemColSum % BASIC_COL_LEN) + unref(getEmptySpan);

    getAdvanced(unref(getProps).actionColOptions || { span: BASIC_COL_LEN }, itemColSum, true);

    emit('advanced-change');
  }

  function handleToggleAdvanced() {
    advanceState.isAdvanced = !advanceState.isAdvanced;
  }

  return { handleToggleAdvanced };
}
