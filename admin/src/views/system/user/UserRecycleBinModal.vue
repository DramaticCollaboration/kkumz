<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="사용자 삭제" :showOkBtn="false" width="1000px" destroyOnClose>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--슬롯: 테이블 제목-->
      <template #tableTitle>
        <a-dropdown v-if="checkedKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                일괄 삭제
              </a-menu-item>
              <a-menu-item key="1" @click="batchHandleRevert">
                <Icon icon="ant-design:redo-outlined"></Icon>
                일괄 복원
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >일괄 작업
            <Icon icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--액션바-->
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <TableAction :actions="getTableAction(record)" />
        </template>
      </template>
    </BasicTable>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, toRaw, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { recycleColumns } from './user.data';
  import { getRecycleBinList, putRecycleBin, deleteRecycleBin } from './user.api';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createConfirm } = useMessage();
  // 방출 선언
  const emit = defineEmits(['success', 'register']);
  const checkedKeys = ref<Array<string | number>>([]);
  const [registerModal] = useModalInner(() => {
    checkedKeys.value = [];
  });
  //테이블 데이터 등록
  const [registerTable, { reload }] = useTable({
    api: getRecycleBinList,
    columns: recycleColumns,
    rowKey: 'id',
    striped: true,
    useSearchForm: false,
    showTableSetting: false,
    clickToRowSelect: false,
    bordered: true,
    showIndexColumn: false,
    pagination: true,
    tableSetting: { fullScreen: true },
    canResize: false,
    actionColumn: {
      width: 150,
      title: '작업',
      dataIndex: 'action',
      // slots: { customRender: 'action' },
      fixed: undefined,
    },
  });
  /**
   * 컬럼 구성 선택
   */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 50,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };
  /**
   * 이벤트 선택
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }
  /**
   *복원 이벤트
   */
  async function handleRevert(record) {
    await putRecycleBin({ userIds: record.id }, reload);
    emit('success');
  }
  /**
   * 일괄 복원 이벤트
   */
  function batchHandleRevert() {
    handleRevert({ id: toRaw(unref(checkedKeys)).join(',') });
  }
  /**
   * 이벤트 삭제
   */
  async function handleDelete(record) {
    await deleteRecycleBin({ userIds: record.id }, reload);
  }
  /**
   * 일괄 삭제 이벤트
   */
  function batchHandleDelete() {
    createConfirm({
      iconType: 'warning',
      title: '삭제',
      content: '영구적으로 삭제하시겠습니까?한번 삭제하면 복구할 수 없습니다!',
      onOk: () => handleDelete({ id: toRaw(unref(checkedKeys)).join(',') }),
      onCancel() {},
    });
  }
  //액션바 이벤트 받기
  function getTableAction(record) {
    return [
      {
        label: '검색',
        icon: 'ant-design:redo-outlined',
        popConfirm: {
          title: '복원 확인',
          confirm: handleRevert.bind(null, record),
        },
      },
      {
        label: '영구 삭제',
        icon: 'ant-design:scissor-outlined',
        popConfirm: {
          title: '삭제 하시겠습니까?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
