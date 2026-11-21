<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="t('appUi.k296368a8')" :showOkBtn="false" width="1000px" destroyOnClose>
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
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
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
      title: t('appUi.k9d9bf438'),
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
        label: t('appUi.kcc598771'),
        icon: 'ant-design:redo-outlined',
        popConfirm: {
          title: t('appUi.ka41d694f'),
          confirm: handleRevert.bind(null, record),
        },
      },
      {
        label: t('appUi.k36ed4635'),
        icon: 'ant-design:scissor-outlined',
        color: 'error',
        popConfirm: {
          title: t('appUi.ka6e94ce1'),
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
