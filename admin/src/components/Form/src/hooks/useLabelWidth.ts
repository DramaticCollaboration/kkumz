import type { Ref } from 'vue';
import type { FormProps, FormSchema } from '../types/form';

import { computed, unref } from 'vue';
import { isNumber } from '/@/utils/is';
import { useAppInject } from '/@/hooks/web/useAppInject';

export function useItemLabelWidth(schemaItemRef: Ref<FormSchema>, propsRef: Ref<FormProps>) {
  const { getIsMobile } = useAppInject();

  return computed(() => {
    const schemaItem = unref(schemaItemRef);
    const { labelCol = {}, wrapperCol = {} } = schemaItem.itemProps || {};
    const { labelWidth, disabledLabelWidth } = schemaItem;

    const { labelWidth: globalLabelWidth, labelCol: globalLabelCol, wrapperCol: globWrapperCol, layout } = unref(propsRef);

    if (unref(getIsMobile)) {
      return {
        labelCol: { span: 24, style: { width: '100%', textAlign: 'left' } },
        wrapperCol: { span: 24, style: { width: '100%', maxWidth: '100%' } },
      };
    }

    if (disabledLabelWidth || layout === 'vertical') {
      return { labelCol, wrapperCol };
    }

    // If labelWidth is set globally, all items setting
    if (!globalLabelWidth && !labelWidth && !globalLabelCol) {
      labelCol.style = {
        textAlign: 'left',
      };
      return { labelCol, wrapperCol };
    }
    let width = labelWidth || globalLabelWidth;
    const col = { ...globalLabelCol, ...labelCol };
    const wrapCol = { ...globWrapperCol, ...wrapperCol };

    if (width) {
      width = isNumber(width) ? `${width}px` : width;
    }

    return {
      labelCol: { style: { width: width ? width : '100%' }, ...col },
      wrapperCol: {
        style: { width: layout === 'vertical' ? '100%' : `calc(100% - ${width})` },
        ...wrapCol,
      },
    };
  });
}
