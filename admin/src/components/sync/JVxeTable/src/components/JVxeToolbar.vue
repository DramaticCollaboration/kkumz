<template>
  <div :class="boxClass">
    <vxe-toolbar ref="xToolbarRef" :custom="custom">
      <!-- 도구 버튼 -->
      <template #buttons>
        <div :class="`${prefixCls}-button div`" :size="btnSize">
          <slot v-if="showPrefix" name="toolbarPrefix" :size="btnSize" />
          <a-button v-if="showAdd" type="primary" preIcon="ant-design:plus-outlined" :disabled="disabled" @click="trigger('add')">
            <span>등록</span>
          </a-button>
          <a-button v-if="showSave" preIcon="ant-design:save-outlined" :disabled="disabled" @click="trigger('save')">
            <span>저장</span>
          </a-button>
          <template v-if="selectedRowIds.length > 0">
            <Popconfirm v-if="showRemove" :title="`정말로 이 $을 삭제하시겠습니까?{selectedRowIds.length} 목?`" @confirm="trigger('remove')">
              <a-button preIcon="ant-design:minus-outlined" :disabled="disabled">삭제</a-button>
            </Popconfirm>
            <template v-if="showClearSelection">
              <a-button preIcon="ant-design:delete-outlined" @click="trigger('clearSelection')">선택 취소</a-button>
            </template>
          </template>
          <slot v-if="showSuffix" name="toolbarSuffix" :size="btnSize" />
          <a v-if="showCollapse" style="margin-left: 4px" @click="toggleCollapse">
            <span>{{ collapsed ? '확장' : '닫기' }}</span>
            <Icon :icon="collapsed ? 'ant-design:down-outlined' : 'ant-design:up-outlined'" />
          </a>
        </div>
      </template>
    </vxe-toolbar>
  </div>
</template>

<script lang="ts" setup>
  import { computed, inject, ref, onMounted } from 'vue';
  // noinspection ES6UnusedImports
  import { Popconfirm } from 'ant-design-vue';
  import { VxeToolbarInstance } from 'vxe-table';
  import { Icon } from '/@/components/Icon';
  import { propTypes } from '/@/utils/propTypes';

  const props = defineProps({
    size: propTypes.string,
    disabled: propTypes.bool.def(false),
    custom: propTypes.bool.def(false),
    toolbarConfig: propTypes.object,
    disabledRows: propTypes.object,
    hasBtnAuth: propTypes.func,
    selectedRowIds: propTypes.array.def(() => []),
  });
  const emit = defineEmits(['save', 'add', 'remove', 'clearSelection', 'register']);
  const xToolbarRef = ref({} as VxeToolbarInstance);
  const prefixCls = `${inject('prefixCls')}-toolbar`;
  const boxClass = computed(() => [
    prefixCls,
    {
      [`${prefixCls}-collapsed`]: collapsed.value,
    },
  ]);
  // 폐쇄 여부
  const collapsed = ref(true);
  // 구성된 버튼
  const btns = computed(() => {
    let { btn, btns } = props.toolbarConfig || {};
    btns = btn || btns || ['add', 'remove', 'clearSelection'];
    // 승인되지 않은 버튼 제외
    return btns.filter((btn) => {
      // 시스템의 기본 일괄 삭제 인코딩 구성은 Batch_delete입니다. 여기에는 호환성이 필요합니다.
      if (btn === 'remove') {
        return hasBtnAuth(btn) && hasBtnAuth('batch_delete');
      }
      return hasBtnAuth(btn);
    });
  });
  const showAdd = computed(() => btns.value.includes('add'));
  const showSave = computed(() => btns.value.includes('save'));
  const showRemove = computed(() => btns.value.includes('remove'));
  // 구성된 슬롯
  const slots = computed(() => props.toolbarConfig?.slot || ['prefix', 'suffix']);
  const showPrefix = computed(() => slots.value.includes('prefix'));
  const showSuffix = computed(() => slots.value.includes('suffix'));
  // 선택 취소 버튼 표시 여부
  const showClearSelection = computed(() => {
    if (btns.value.includes('clearSelection')) {
      // 선택 취소 버튼은 비활성화된 행이 있는 경우에만 표시됩니다.
      // 행을 비활성화하면 행 선택이 방지되므로 모두 선택을 취소할 수는 없습니다.
      // return Object.keys(props.disabledRows).length > 0
    }
    return false;
  });
  // 펼치기 접기 버튼 표시 여부
  const showCollapse = computed(() => btns.value.includes('collapse'));
  // 버튼 크기
  const btnSize = computed(() => (props.size === 'tiny' ? 'small' : null));

  onMounted(() => {
    // vxe-toolbar 등록
    emit('register', {
      xToolbarRef,
    });
  });

  // 버튼이 인증되었는지 확인
  function hasBtnAuth(key: string) {
    return props.hasBtnAuth ? props.hasBtnAuth(key) : true;
  }

  /** 트리거 이벤트 */
  function trigger(name) {
    emit(name);
  }

  // 펼치기 접기 전환
  function toggleCollapse() {
    collapsed.value = !collapsed.value;
  }
</script>
