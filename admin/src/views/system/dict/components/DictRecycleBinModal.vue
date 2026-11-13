<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="사전 삭제" :showOkBtn="false" width="1000px" destroyOnClose>
    <BasicTable @register="registerTable">
      <!--액션바-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, toRaw } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';
  import { BasicTable, useTable, TableAction } from '/src/components/Table';
  import { recycleBincolumns } from '../dict.data';
  import { getRecycleBinList, putRecycleBin, deleteRecycleBin } from '../dict.api';
  // 방출 선언
  const emit = defineEmits(['success', 'register']);
  const checkedKeys = ref<Array<string | number>>([]);
  const [registerModal, { setModalProps, closeModal }] = useModalInner();
  //테이블 데이터 등록
  const [registerTable, { reload }] = useTable({
    api: getRecycleBinList,
    columns: recycleBincolumns,
    striped: true,
    useSearchForm: false,
    showTableSetting: false,
    clickToRowSelect: false,
    bordered: true,
    showIndexColumn: false,
    pagination: false,
    tableSetting: { fullScreen: true },
    canResize: false,
    actionColumn: {
      width: 100,
      title: '작업',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: undefined,
    },
  });

  /**
   *복원 이벤트
   */
  async function handleRevert(record) {
    await putRecycleBin(record.id, reload);
    emit('success');
  }
  /**
   * 이벤트 삭제
   */
  async function handleDelete(record) {
    await deleteRecycleBin(record.id, reload);
  }
  /**
   * 일괄 복원 이벤트
   */
  function batchHandleRevert() {
    handleRevert({ id: toRaw(checkedKeys.value).join(',') });
  }
  /**
   * 일괄 삭제 이벤트
   */
  function batchHandleDelete() {
    handleDelete({ id: toRaw(checkedKeys.value).join(',') });
  }
  //액션바 이벤트 받기
  function getTableAction(record) {
    return [
      {
        label: '복원',
        icon: 'ant-design:redo-outlined',
        popConfirm: {
          title: '선택한 항목을 복원하시겠습니까?',
          confirm: handleRevert.bind(null, record),
        },
      },
      {
        label: '영구 삭제',
        icon: 'ant-design:scissor-outlined',
        color: 'error',
        popConfirm: {
          title: '삭제 하시겠습니까?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
