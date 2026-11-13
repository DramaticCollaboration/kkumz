<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="퇴직자 정보" :showOkBtn="false" width="1000px" destroyOnClose>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--슬롯: 테이블 제목-->
      <template #tableTitle>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleRevert">
                <Icon icon="ant-design:redo-outlined"></Icon>
                일괄 취소
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
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </BasicModal>
</template>

<script lang="ts" setup name="user-quit-modal">
  import { ref, toRaw, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { recycleColumns } from './user.data';
  import { getQuitList, putCancelQuit, deleteRecycleBin } from './user.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  
  const { createConfirm } = useMessage();
  // 방출 선언
  const emit = defineEmits(['success', 'register']);
  const checkedKeys = ref<Array<string | number>>([]);
  const [registerModal] = useModalInner(() => {
    checkedKeys.value = [];
  });
  //테이블 데이터 등록
  const { prefixCls, tableContext } = useListPage({
    tableProps: {
      api: getQuitList,
      columns: recycleColumns,
      rowKey: 'id',
      canResize: false,
      useSearchForm: false,
      actionColumn: {
        width: 120,
      },
    },
  });
  //테이블 데이터 등록
  const [registerTable, { reload }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;

  /**
   * 퇴사 이벤트 취소
   * @param record
   */
  async function handleCancelQuit(record) {
    await putCancelQuit({ userIds: record.id, usernames: record.username }, reload);
    emit('success');
  }
  /**
   * 퇴사 이벤트 일괄 취소
   */
  function batchHandleRevert() {
    createConfirm({
      title: '퇴사 취소',
      content: '선택한 사용자의 퇴사 처리를 취소하고 정상 계정으로 복원하시겠습니까?',
      okText: '확인',
      cancelText: '취소',
      onOk: () => {
        let rowValue = selectedRows.value;
        let rowData: any = [];
        for (const value of rowValue) {
          rowData.push(value.username);
        }
        handleCancelQuit({ id: toRaw(unref(selectedRowKeys)).join(','), username: rowData.join(',') });
      },
    });
  }

  //액션바 이벤트 받기
  function getTableAction(record) {
    return [
      {
        label: '퇴사 취소',
        icon: 'ant-design:redo-outlined',
        popConfirm: {
          title: '선택한 사용자의 퇴사 처리를 취소하고\n정상 계정으로 복원하시겠습니까?',
          confirm: handleCancelQuit.bind(null, record),
        },
      },
    ];
  }
</script>
