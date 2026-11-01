<template>
  <div :class="[prefixCls]">
    <JSelectUser v-bind="getProps" @change="handleChange" />
  </div>
</template>

<script lang="ts">
  import { computed, defineComponent } from 'vue';
  //import { JSelectUser } from '/@/components/Form';
  import JSelectUser from '/@/components/Form/src/sync/components/JSelectUser.vue';
  import { JVxeComponent } from '/@/components/sync/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/sync/JVxeTable/hooks';
  import { dispatchEvent } from '/@/components/sync/JVxeTable/utils';
  import { isArray, isEmpty, isString } from '/@/utils/is';

  export default defineComponent({
    name: 'JVxeUserSelectCell',
    components: { JSelectUser },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, handleChangeCommon, useCellDesign } = useJVxeComponent(props);
      const { prefixCls } = useCellDesign('user-select');

      const selectedValue = computed(() => {
        let val: any = innerValue.value;
        if (val == null) {
          return val;
        }
        if (isEmpty(val)) {
          return [];
        }
        if (isArray(val)) {
          return val;
        }
        if (isString(val)) {
          // @ts-ignore
          return val.split(',');
        }
        return [val];
      });
      // @ts-ignore
      const multiple = computed(() => cellProps.value.multi != false);

      const getProps = computed(() => {
        return {
          ...cellProps.value,
          value: selectedValue.value,
          showButton: false,
          // 검색이 허용되지 않습니다
          showSearch: false,
          // 최대 디스플레이 수 설정
          maxTagCount: 1,
          // 프롬프트 재작성 표시, 줄임표 제거
          maxTagPlaceholder: ({ length }) => '+' + length,
        };
      });

      function handleChange(values) {
        handleChangeCommon(values.join(','));
      }

      return {
        prefixCls,
        selectedValue,
        multiple,
        cellProps,
        getProps,
        handleChange,
      };
    },
    enhanced: {
      switches: {
        visible: true,
      },
      translate: {
        enabled: false,
      },
      aopEvents: {
        editActived({ $event }) {
          dispatchEvent({
            $event,
            props: this.props,
            className: '.ant-select .ant-select-selection-search-input',
            isClick: true,
          });
        },
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>

<style lang="less">
  // noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-vxe-cell-user-select';

  .@{prefix-cls} {
    // 텍스트가 너무 많은 옵션을 선택할 때 줄 바꿈을 방지하려면 최대 태그 길이를 100px로 제한하세요.
    .ant-select .ant-select-selection-overflow-item {
      max-width: 80px;
    }
  }
</style>
