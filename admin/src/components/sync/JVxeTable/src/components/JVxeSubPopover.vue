<template>
  <a-popover :open="visible" :placement="placement" overlayClassName="j-vxe-popover-overlay" :overlayStyle="overlayStyle">
    <template #title>
      <div class="j-vxe-popover-title">
        <div>{{ t('appUi.kee1aef39') }}</div>
        <div class="j-vxe-popover-title-close" @click="close">
          <a-icon type="close" />
        </div>
      </div>
    </template>

    <template #content>
      <transition name="fade">
        <slot v-if="visible" name="subForm" :row="row" :column="column" />
      </transition>
    </template>

    <div ref="divRef" class="j-vxe-popover-div"></div>
  </a-popover>
</template>
<script lang="ts">
  import { useI18n } from '/@/hooks/web/useI18n';
  import { ref, reactive, nextTick, defineComponent } from 'vue';
  import domAlign from 'dom-align';
  import { getParentNodeByTagName } from '../utils/vxeUtils';
  import { triggerWindowResizeEvent } from '/@/utils/common/compUtils';
  import { cloneDeep } from 'lodash-es';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { isString } from '/@/utils/is';

  export default defineComponent({
    name: 'JVxeSubPopover',
    setup() {
      const { t } = useI18n();
      const visible = ref(false);
      const row = ref<any>(null);
      const column = ref<any>(null);
      const overlayStyle = reactive<{
        width?: number | string;
        maxWidth?: number | string;
        zIndex: number;
      }>({
        zIndex: 100,
      });
      const placement = ref('bottom');
      const divRef = ref<HTMLElement>();
      const { createMessage } = useMessage();

      function toggle(event) {
        if (document.body.clientHeight - event.$event.clientY > 350) {
          placement.value = 'bottom';
        } else {
          placement.value = 'top';
        }
        if (row.value == null) {
          open(event);
        } else {
          row.value.id === event.row.id ? close() : reopen(event);
        }
      }

      function open(event, level = 0) {
        if (level > 3) {
          createMessage.error(t('appUi.kce7b539a'));
          console.warn('[JVxeSubPopover] Failed to open sub-table.');
          return;
        }

        let {
          row: $row,
          column: $column,
          $table,
          $event: { target },
        } = event;
        row.value = cloneDeep($row);
        column.value = $column;

        let className = target.className || '';
        className = isString(className) ? className : className.toString();

        // td 부모 가져오기
        let td = getParentNodeByTagName(target, 'td');
        // 클릭된 것은 드래그 정렬 컬럼이므로 처리되지 않습니다.
        if (td && td.querySelector('.j-vxe-drag-box')) {
          return;
        }
        // 클릭이 확장되어 처리되지 않습니다.
        if (className.includes('vxe-table--expand-btn')) {
          return;
        }
        // 클릭된 것은 체크박스이며 처리되지 않습니다.
        if (className.includes('vxe-checkbox--icon') || className.includes('vxe-cell--checkbox')) {
          return;
        }
        // 클릭은 라디오이며 처리가 수행되지 않습니다.
        if (className.includes('vxe-radio--icon') || className.includes('vxe-cell--radio')) {
          return;
        }
        let parentElem = $table.getParentElem();
        let tr = getParentNodeByTagName(target, 'tr');
        if (parentElem && tr) {
          let clientWidth = parentElem.clientWidth;
          let clientHeight = tr.clientHeight;
          divRef.value!.style.width = clientWidth + 'px';
          divRef.value!.style.height = clientHeight + 'px';
          overlayStyle.width = Number.parseInt(`${clientWidth - clientWidth * 0.04}`) + 'px';
          overlayStyle.maxWidth = overlayStyle.width;
          //let realTable = getParentNodeByTagName(tr, 'table')
          //let left = realTable.parentNode.scrollLeft
          let h = event.$event.clientY;
          if (h) {
            h = h - 140;
          }
          let toolbar = divRef.value!.nextElementSibling;
          domAlign(divRef.value, toolbar, {
            points: ['tl', 'tl'],
            offset: [0, h],
            overflow: {
              alwaysByViewport: true,
            },
          });
          nextTick(() => {
            visible.value = true;
            nextTick(() => {
              triggerWindowResizeEvent();
            });
          });
        } else {
          let num = ++level;
          console.warn(t('appUi.k049d53e4') + num + t('appUi.k548fe005'), {
            event,
            table: parentElem,
            tr,
          });
          window.setTimeout(() => open(event, num), 100);
        }
      }

      function close() {
        if (visible.value) {
          row.value = null;
          visible.value = false;
        }
      }

      function reopen(event) {
        open(event);
      }

      return {
        t,
        divRef,
        row,
        column,
        visible,
        placement,
        overlayStyle,
        close,
        toggle,
      };
    },
  });
</script>
<style scoped lang="less">
  .j-vxe-popover-title {
    .j-vxe-popover-title-close {
      position: absolute;
      right: 0;
      top: 0;
      width: 31px;
      height: 31px;
      text-align: center;
      line-height: 31px;
      color: rgba(0, 0, 0, 0.45);
      cursor: pointer;
      transition: color 300ms;

      &:hover {
        color: rgba(0, 0, 0, 0.8);
      }
    }
  }

  .j-vxe-popover-div {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 31px;
    z-index: -1;
  }
</style>
<style lang="less">
  .j-vxe-popover-overlay.ant-popover {
    .ant-popover-title {
      position: relative;
    }
  }

  .fade-enter-active,
  .fade-leave-active {
    opacity: 1;
    transition: opacity 0.5s;
  }

  .fade-enter,
  .fade-leave-to {
    opacity: 0;
  }
</style>
