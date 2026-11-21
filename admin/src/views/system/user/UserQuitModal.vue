<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="t('appUi.ke624aff7')" :showOkBtn="false" width="1000px" destroyOnClose>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--슬롯: 테이블 제목-->
      <template #tableTitle>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleRevert">
                <Icon icon="ant-design:redo-outlined"></Icon>
                {{ t('appUi.k27fb23da') }}
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >{{ t('appUi.k39eb4aed') }}
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
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  
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
      title: t('appUi.k1d1b5292'),
      content: t('appUi.k9955246a'),
      okText: t('appUi.k468266d6'),
      cancelText: t('appUi.k19b2d19b'),
      onOk: () => {
        let rowValue = selectedRows.value;
        let rowData: Array<Record<string, unknown>> = [];
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
        label: t('appUi.k1d1b5292'),
        icon: 'ant-design:redo-outlined',
        popConfirm: {
          title: t('appUi.kb57f63a3'),
          confirm: handleCancelQuit.bind(null, record),
        },
      },
    ];
  }
</script>
