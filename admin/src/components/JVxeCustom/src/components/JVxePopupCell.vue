<template>
  <JPopup v-bind="popupProps" @focus="handleFocus" />
</template>
<script lang="ts">
  import { computed, defineComponent, ref } from 'vue';
  //import { JPopup } from '/@/components/Form';
  import JPopup from '/@/components/Form/src/sync/components/JPopup.vue';
  import { JVxeComponent } from '/@/components/sync/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/sync/JVxeTable/hooks';
  import { dispatchEvent, vModel } from '/@/components/sync/JVxeTable/utils';
  import { isEmpty } from '/@/utils/is';

  export default defineComponent({
    name: 'JVxePopupCell',
    components: { JPopup },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, row, originColumn, cellProps, handleChangeCommon } = useJVxeComponent(props);
      const groupId = ref<string>('j-vxe-popup');
      const popupProps = computed(() => {
        return {
          ...cellProps.value,
          value: innerValue.value,
          field: originColumn.value.field || originColumn.value.key,
          code: originColumn.value.popupCode,
          fieldConfig: originColumn.value.fieldConfig,
          // orgFields: originColumn.value.orgFields,
          // destFields: originColumn.value.destFields,
          groupId: groupId.value,
          param: originColumn.value.params,
          sorter: originColumn.value.sorter,
          setFieldsValue: (values) => {
            if (!isEmpty(values)) {
              let popupValue = '';
              Object.keys(values).forEach((key) => {
                let currentValue = values[key];
                // 현재 열은 직접 할당되고, 다른 열은 vModel을 통해 할당됩니다.
                if (key === originColumn.value.key) {
                  popupValue = currentValue;
                } else {
                  vModel(currentValue, row, key);
                }
              });
              handleChangeCommon(popupValue);
            }
          },
        };
      });
      const handleFocus = () => {
        groupId.value = '';
      };
      return {
        handleFocus,
        popupProps,
      };
    },
    // 【구성요소 강화】자세한 내용은 참고 사항을 참조하세요.：JVxeComponent.Enhanced
    enhanced: {
      aopEvents: {
        editActived({ $event }) {
          dispatchEvent({
            $event,
            props: this.props,
            className: '.ant-input',
            isClick: true,
          });
        },
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
