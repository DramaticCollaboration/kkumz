<template>
  <a-date-picker
    :value="innerDateValue"
    allowClear
    :format="picker ? null : dateFormat"
    :showTime="isDatetime"
    :valueFormat="picker ? dateFormat : null"
    popupClassName="j-vxe-date-picker"
    style="min-width: 0"
    v-model:open="openPicker"
    v-bind="cellProps"
    :picker="picker"
    @change="handleChange"
  />
</template>

<script lang="ts">
  import { ref, computed, watch, defineComponent } from 'vue';
  import dayjs from 'dayjs';
  import { JVxeComponent, JVxeTypes } from '/@/components/sync/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/sync/JVxeTable/hooks';
  import { isEmpty } from '/@/utils/is';
  import { getWeekMonthQuarterYear } from '/@/utils';

  export default defineComponent({
    name: 'JVxeDateCell',
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, originColumn, handleChangeCommon } = useJVxeComponent(props);
      const innerDateValue = ref<any>(null);
      const isDatetime = computed(() => props.type === JVxeTypes.datetime);
      const dateFormat = computed(() => {
        let format = originColumn.value.format;
        return format ? format : isDatetime.value ? 'YYYY-MM-DD HH:mm:ss' : 'YYYY-MM-DD';
      });
      const openPicker = ref(true);
      const picker = computed(() => {
        const picker = originColumn.value.picker;
        return picker ? picker : null;
      });
      watch(
        innerValue,
        (val) => {
          if (val == null || isEmpty(val)) {
            innerDateValue.value = null;
          } else {
            innerDateValue.value = dayjs(val, dateFormat.value);
          }
        },
        { immediate: true }
      );

      function handleChange(_mom, dateStr) {
        if (picker.value) {
          handleChangeCommon(_mom);
        } else {
          handleChangeCommon(dateStr);
        }
      }

      return {
        cellProps,
        isDatetime,
        dateFormat,
        innerDateValue,
        openPicker,
        handleChange,
        picker,
      };
    },
    // 【구성요소 강화】자세한 내용은 참고 사항을 참조하세요.：JVxeComponent.Enhanced
    enhanced: {
      aopEvents: {
      },
      translate: {
        enabled: true,
        handler(value, ctx) {
          let { props, context } = ctx!;
          let { row, originColumn } = context;
          if (originColumn.value.picker && value) {
            return getWeekMonthQuarterYear(value)[originColumn.value.picker];
          }
          return value;
        },
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
