<template>
  <div class="j-vxe-drag-box">
     <span v-if="!isAllowDrag"><span class="not-drag-btn"> <Icon icon="mi:drag" /> </span
      ></span>
    <a-dropdown v-else :trigger="['click']" >
      <span
        ><span class="drag-btn"> <Icon icon="mi:drag" /> </span
      ></span>
      <template #overlay >
        <a-menu>
          <a-menu-item key="0" :disabled="disabledMoveUp" @click="handleRowMoveUp">위로 이동</a-menu-item>
          <a-menu-item key="1" :disabled="disabledMoveDown" @click="handleRowMoveDown">아래로 이동</a-menu-item>
          <a-menu-divider />
          <a-menu-item key="3" @click="handleRowInsertDown">행 삽입</a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
  </div>
</template>

<script lang="ts">
  import { computed, defineComponent } from 'vue';
  import { Icon } from '/@/components/Icon';
  import { JVxeComponent } from '/@/components/sync/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/sync/JVxeTable/hooks';

  export default defineComponent({
    name: 'JVxeDragSortCell',
    components: { Icon },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { rowIndex, originColumn, fullDataLength, trigger } = useJVxeComponent(props);
      const isAllowDrag = computed(() => {
        const notAllowDrag = originColumn.value.notAllowDrag;
        if (notAllowDrag.length) {
          const row = props.params.row;
          const find = notAllowDrag.find((item: any) => {
            const { key, value } = item;
            return row[key] == value;
          });
          return !find;
        } else {
          return true;
        }
      });
      const disabledMoveUp = computed(() => rowIndex.value === 0);
      const disabledMoveDown = computed(() => rowIndex.value === fullDataLength.value - 1);

      /** 위로 이동 */
      function handleRowMoveUp() {
        if (!disabledMoveUp.value) {
          trigger('rowResort', {
            oldIndex: rowIndex.value,
            newIndex: rowIndex.value - 1,
          });
        }
      }

      /** 아래로 이동 */
      function handleRowMoveDown() {
        if (!disabledMoveDown.value) {
          trigger('rowResort', {
            oldIndex: rowIndex.value,
            newIndex: rowIndex.value + 1,
          });
        }
      }

      /** 행 삽입 */
      function handleRowInsertDown() {
        trigger('rowInsertDown', rowIndex.value);
      }

      return {
        disabledMoveUp,
        disabledMoveDown,
        handleRowMoveUp,
        handleRowMoveDown,
        handleRowInsertDown,
        isAllowDrag
      };
    },
    // 【구성요소 강화】자세한 내용은 참고 사항을 참조하세요.：JVxeComponent.Enhanced
    enhanced: {
      // 【기능 스위치】
      switches: {
        editRender: false,
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>

<style lang="less">
  .j-vxe-drag-box {
    .app-iconify {
      cursor: move;
    }
  }

  .vxe-table--fixed-wrapper {
    .j-vxe-drag-box {
      .app-iconify {
        cursor: pointer;
      }
    }
  }
</style>
<style scoped>
  .not-drag-btn {
    opacity: 0.5;
     .app-iconify {
      cursor: not-allowed;
    }
  }
</style>
