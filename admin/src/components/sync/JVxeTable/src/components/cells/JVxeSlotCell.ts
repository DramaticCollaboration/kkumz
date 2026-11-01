import { computed, defineComponent, h } from 'vue';
import { useJVxeComponent, useJVxeCompProps } from '/@/components/sync/JVxeTable/src/hooks/useJVxeComponent';
import { JVxeComponent } from '/@/components/sync/JVxeTable/src/types/JVxeComponent';

export default defineComponent({
  name: 'JVxeSlotCell',
  props: useJVxeCompProps(),
  setup(props: JVxeComponent.Props) {
    const data = useJVxeComponent(props);
    const slotProps = computed(() => {
      return {
        value: data.innerValue.value,
        row: data.row.value,
        column: data.originColumn.value,
        params: props.params,
        $table: props.params.$table,
        rowId: props.params.rowid,
        index: props.params.rowIndex,
        rowIndex: props.params.rowIndex,
        columnIndex: props.params.columnIndex,
        scrolling: props.renderOptions.scrolling,
        reloadEffect: props.renderOptions.reloadEffect.enabled,
        triggerChange: (v) => data.handleChangeCommon(v),
      };
    });
    return () => {
      let { slot } = props.renderOptions;
      if (slot) {
        return h('div', {}, slot(slotProps.value));
      } else {
        return h('div');
      }
    };
  },
  // 【구성요소 강화】자세한 내용은 참고 사항을 참조하세요.：JVxeComponent.Enhanced
  enhanced: {
    switches: {
      editRender: false,
    },
  } as JVxeComponent.EnhancedPartial,
});
