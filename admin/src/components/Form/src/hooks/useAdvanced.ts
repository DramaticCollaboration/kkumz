import type { ColEx } from '../types';
import type { AdvanceState } from '../types/hooks';
import type { ComputedRef, Ref } from 'vue';
import type { FormProps, FormSchema } from '../types/form';
import { computed, unref, watch } from 'vue';
import { isBoolean, isFunction, isNumber, isObject } from '/@/utils/is';
import { useBreakpoint } from '/@/hooks/event/useBreakpoint';
import { useDebounceFn } from '@vueuse/core';

const BASIC_COL_LEN = 24;
const ACTION_MIN_SPAN = 6;
const MAX_SEARCH_COL_SPAN = 6;

interface UseAdvancedContext {
  advanceState: AdvanceState;
  emit: EmitType;
  getProps: ComputedRef<FormProps>;
  getSchema: ComputedRef<FormSchema[]>;
  formModel: Recordable;
  defaultValueRef: Ref<Recordable>;
}

/**
 * 검색 폼(showAdvancedButton) 열 너비를 정규화하여 1줄 인라인(최대 3개 필드 + 우측 액션 영역) 배치를 보장한다.
 */
export function normalizeSearchColProps(
  baseColProps: Partial<ColEx> = {},
  colProps: Partial<ColEx> = {},
  showAdvancedButton = false,
  isMobile = false
): Partial<ColEx> {
  const realColProps: Recordable = { span: BASIC_COL_LEN, ...baseColProps, ...colProps };
  if (isMobile) {
    return { ...realColProps, span: BASIC_COL_LEN, xs: BASIC_COL_LEN, sm: BASIC_COL_LEN, md: BASIC_COL_LEN };
  }
  if (colProps.span !== undefined) {
    ['xs', 'sm', 'md', 'lg', 'xl', 'xxl'].forEach((name) => delete realColProps[name]);
  }
  if (showAdvancedButton) {
    ['span', 'lg', 'xl', 'xxl'].forEach((key) => {
      const val = Number(realColProps[key]);
      if (!Number.isNaN(val) && val > MAX_SEARCH_COL_SPAN && val < BASIC_COL_LEN) {
        realColProps[key] = MAX_SEARCH_COL_SPAN;
      }
    });
  }
  return realColProps;
}

/**
 * 접힘 상태에서 숨겨진 상세 검색 필드에 유효한 필터 값이 입력되어 있는지 판별한다.
 */
export function isFilterValueActive(val: any, defaultVal?: any): boolean {
  if (val === undefined || val === null || val === '') {
    return false;
  }
  if (Array.isArray(val)) {
    if (val.length === 0 || val.every((item) => item === undefined || item === null || item === '')) {
      return false;
    }
  }
  if (defaultVal !== undefined && defaultVal !== null && defaultVal !== '') {
    try {
      if (JSON.stringify(val) === JSON.stringify(defaultVal)) {
        return false;
      }
    } catch {
      if (val === defaultVal) {
        return false;
      }
    }
  }
  return true;
}

/**
 * 접힘/펼침 시 필드 노출 여부를 결정한다.
 * - 닫힘: alwaysShowLines(기본 1줄) 안쪽 필드는 항상 노출
 * - 닫힘: autoAdvancedCol을 넘는 필드는 숨김
 * - 열림: 전체 노출
 */
export function resolveFieldAdvancedVisibility(params: {
  itemColSumAfter: number;
  index: number;
  isAdvanced: boolean;
  alwaysShowLines?: number;
  autoAdvancedCol?: number;
  alwaysShow?: boolean;
  maxCollapsedSpan?: number;
}): boolean {
  if (params.isAdvanced) {
    return true;
  }
  const alwaysShowLines = params.alwaysShowLines ?? 1;
  const autoAdvancedCol = params.autoAdvancedCol ?? 3;
  const maxSpan = params.maxCollapsedSpan ?? BASIC_COL_LEN * alwaysShowLines;

  if (params.itemColSumAfter > maxSpan) {
    return params.index === 0;
  }
  if (params.index + 1 > autoAdvancedCol && !params.alwaysShow) {
    return false;
  }
  return true;
}

export default function ({ advanceState, emit, getProps, getSchema, formModel, defaultValueRef }: UseAdvancedContext) {
  const { realWidthRef, screenEnum, screenRef } = useBreakpoint();

  const getEmptySpan = computed((): number => {
    if (!advanceState.isAdvanced) {
      return 0;
    }
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
    [() => unref(getSchema), () => advanceState.isAdvanced, () => unref(realWidthRef), () => formModel],
    () => {
      const { showAdvancedButton } = unref(getProps);
      if (showAdvancedButton) {
        debounceUpdateAdvanced();
      }
    },
    { immediate: true, deep: true }
  );

  function getColSpan(itemCol: Partial<ColEx>): number {
    const width = unref(realWidthRef);

    const mdWidth =
      parseInt(itemCol.md as string) ||
      parseInt(itemCol.xs as string) ||
      parseInt(itemCol.sm as string) ||
      (itemCol.span as number) ||
      BASIC_COL_LEN;

    const lgWidth = parseInt(itemCol.lg as string) || (itemCol.span as number) || mdWidth;
    const xlWidth = parseInt(itemCol.xl as string) || (itemCol.span as number) || lgWidth;
    const xxlWidth = parseInt(itemCol.xxl as string) || (itemCol.span as number) || xlWidth;

    if (!width) {
      return xxlWidth;
    }
    if (width <= screenEnum.LG) {
      return mdWidth;
    } else if (width < screenEnum.XL) {
      return lgWidth;
    } else if (width < screenEnum.XXL) {
      return xlWidth;
    } else {
      return xxlWidth;
    }
  }

  function updateAdvanced() {
    let itemColSum = 0;
    let realItemColSum = 0;
    const {
      baseColProps = {},
      showAdvancedButton = false,
      alwaysShowLines = 1,
      autoAdvancedCol: rawAutoAdvancedCol = 3,
    } = unref(getProps);

    if (!advanceState.isLoad) {
      advanceState.isLoad = true;
      advanceState.isAdvanced = false;
    }

    const autoAdvancedCol = Math.min(rawAutoAdvancedCol, 3);
    const width = unref(realWidthRef);
    const isDesktop = !width || width > screenEnum.LG;
    const maxCollapsedSpan = isDesktop
      ? Math.max(BASIC_COL_LEN * alwaysShowLines - ACTION_MIN_SPAN, ACTION_MIN_SPAN)
      : BASIC_COL_LEN * alwaysShowLines;

    const schemas = unref(getSchema);
    const defaultValues = unref(defaultValueRef) || {};
    let visibleSchemaIndex = 0;
    let hiddenFieldTotal = 0;
    let hiddenActiveCount = 0;

    for (const schema of schemas) {
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
            ...defaultValues,
            ...formModel,
          },
        });
      }

      if (isShow && (colProps || baseColProps)) {
        const normalizedCol = normalizeSearchColProps(baseColProps, colProps, showAdvancedButton, false);
        const colSpan = getColSpan(normalizedCol);
        itemColSum += colSpan;

        const visibleWhenCollapsed = resolveFieldAdvancedVisibility({
          itemColSumAfter: itemColSum,
          index: visibleSchemaIndex,
          isAdvanced: false,
          alwaysShowLines,
          autoAdvancedCol,
          alwaysShow: schema.alwaysShow === true || Boolean(schema.required),
          maxCollapsedSpan,
        });

        if (!visibleWhenCollapsed) {
          hiddenFieldTotal += 1;
          const currentVal = formModel ? formModel[schema.field] : undefined;
          const defVal = defaultValues[schema.field] ?? schema.defaultValue;
          if (isFilterValueActive(currentVal, defVal)) {
            hiddenActiveCount += 1;
          }
        }

        const isFieldVisible = advanceState.isAdvanced ? true : visibleWhenCollapsed;
        if (isFieldVisible) {
          realItemColSum += colSpan;
        }
        if (schema.isAdvanced !== isFieldVisible) {
          schema.isAdvanced = isFieldVisible;
        }
        visibleSchemaIndex += 1;
      }
    }

    advanceState.actionSpan = (realItemColSum % BASIC_COL_LEN) + unref(getEmptySpan);
    advanceState.hideAdvanceBtn = showAdvancedButton ? hiddenFieldTotal === 0 : true;
    advanceState.hiddenFilterCount = showAdvancedButton && !advanceState.isAdvanced ? hiddenActiveCount : 0;

    emit('advanced-change');
  }

  function handleToggleAdvanced() {
    advanceState.isAdvanced = !advanceState.isAdvanced;
  }

  return { handleToggleAdvanced };
}
