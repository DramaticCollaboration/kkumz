<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="사전 목록" width="800px">
    <BasicTable @register="registerTable" :rowClassName="getRowClassName">
      <template #tableTitle>
        <a-button type="primary" @click="handleCreate"> 등록</a-button>
      </template>
      <template v-slot:bodyCell="{column, record, index}">
        <template v-if="column.dataIndex ==='action'">
          <TableAction :actions="getTableAction(record)" />
        </template>
      </template>

    </BasicTable>
  </BasicDrawer>
  <DictItemModal @register="registerModal" @success="reload" :dictId="dictId" />
</template>
<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/src/components/Drawer';
  import { BasicTable, useTable, TableAction } from '/src/components/Table';
  import { useModal } from '/src/components/Modal';
  import { useDesign } from '/@/hooks/web/useDesign';
  import DictItemModal from './DictItemModal.vue';
  import { dictItemColumns, dictItemSearchFormSchema } from '../dict.data';
  import { itemList, deleteItem } from '../dict.api';
  import { ColEx } from '/@/components/Form/src/types';

  const { prefixCls } = useDesign('row-invalid');
  const dictId = ref('');
  //사전 구성 모델
  const [registerModal, { openModal }] = useModal();
  const [registerDrawer] = useDrawerInner(async (data) => {
    dictId.value = data.id;
    setProps({ searchInfo: { dictId: unref(dictId) } });
    reload();
  });
  // 적응형 열 구성
  const adaptiveColProps: Partial<ColEx> = {
    xs: 24, // <576px
    sm: 24, // ≥576px
    md: 24, // ≥768px
    lg: 12, // ≥992px
    xl: 12, // ≥1200px
    xxl: 8, // ≥1600px
  };
  const [registerTable, { reload, setProps }] = useTable({
    //RowKey를 구성해야 합니다. 그렇지 않으면 경고가 표시됩니다.
    rowKey: 'id',
    api: itemList,
    columns: dictItemColumns,
    formConfig: {
      baseColProps: adaptiveColProps,
      labelAlign: 'right',
      labelCol: {
        offset: 1,
        xs: 24,
        sm: 24,
        md: 24,
        lg: 9,
        xl: 7,
        xxl: 4,
      },
      wrapperCol: {},
      schemas: dictItemSearchFormSchema,
      autoSubmitOnEnter: true,
      actionColOptions: {
        span: 8
      }
    },
    striped: true,
    useSearchForm: true,
    bordered: true,
    showIndexColumn: false,
    canResize: false,
    immediate: false,
    actionColumn: {
      width: 100,
      title: '작업',
      dataIndex: 'action',
      //slots: { customRender: 'action' },
      fixed: undefined,
    },
  });

  /**
   * 새로운
   */
  function handleCreate() {
    openModal(true, {
      isUpdate: false,
    });
  }

  /**
   * 편집
   */
  function handleEdit(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * 삭제
   */
  async function handleDelete(record) {
    await deleteItem({ id: record.id }, reload);
  }

  /**
   * 액션바
   */
  function getTableAction(record) {
    return [
      {
        label: '편집',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: '삭제',
        popConfirm: {
          title: '삭제 하시겠습니까?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
  function getRowClassName(record) {
    return record.status == 0 ? prefixCls : '';
  }
</script>
<style scoped lang="less">
  @prefix-cls: ~'@{namespace}-row-invalid';

  :deep(.@{prefix-cls}) {
    background: #f4f4f4;
    color: #bababa;
  }
</style>
