<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleCompare" style="margin-right: 5px">데이터 비교</a-button>
      </template>
    </BasicTable>
    <DataLogCompareModal @register="registerModal" @success="reload" />
  </div>
</template>
<script lang="ts" name="monitor-datalog" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import DataLogCompareModal from './DataLogCompareModal.vue';
  const [registerModal, { openModal }] = useModal();
  import { getDataLogList } from './datalog.api';
  import { columns, searchFormSchema } from './datalog.data';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  const { createMessage } = useMessage();
  const checkedRows = ref<Array<object | number>>([]);

  // 목록 페이지 공개 매개변수 및 메소드
  const { prefixCls, tableContext } = useListPage({
    designScope: 'datalog-template',
    tableProps: {
      title: '데이터 로그 목록',
      api: getDataLogList,
      columns: columns,
      formConfig: {
        labelWidth: 120,
        schemas: searchFormSchema,
      },
      actionColumn: false,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;

  function handleCompare() {
    let obj = selectedRows.value;
    console.info('sfsfsf', obj);
    if (!obj || obj.length != 2) {
      createMessage.warning('두 가지 데이터를 선택해주세요!');
      return false;
    }
    if (obj[0].dataId != obj[1].dataId) {
      createMessage.warning('비교할 동일한 데이터베이스 테이블과 데이터 ID를 선택하세요!');
      return false;
    }
    openModal(true, {
      selectedRows,
      isUpdate: true,
    });
  }
</script>
