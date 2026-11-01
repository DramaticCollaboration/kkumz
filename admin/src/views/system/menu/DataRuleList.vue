<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="데이터 권한 규칙" :width="adaptiveWidth">
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <a-button type="primary" @click="handleCreate"> 등록</a-button>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </BasicDrawer>
  <DataRuleModal @register="registerModal" @success="reload" :permissionId="permissionId" />
</template>
<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import DataRuleModal from './DataRuleModal.vue';
  import { dataRuleColumns, dataRuleSearchFormSchema } from './menu.data';
  import { dataRuleList, deleteRule } from './menu.api';
  import { ColEx } from '/@/components/Form/src/types';
  import { useDrawerAdaptiveWidth } from '/@/hooks/sync/useAdaptiveWidth';
  const permissionId = ref('');
  const { adaptiveWidth } = useDrawerAdaptiveWidth();
  //권한 규칙 모델
  const [registerModal, { openModal }] = useModal();
  const [registerDrawer] = useDrawerInner(async (data) => {
    permissionId.value = data.id;
    setProps({ searchInfo: { permissionId: unref(permissionId) } });
    reload();
  });
  // 적응형 열 구성
  const adaptiveColProps: Partial<ColEx> = {
    xs: 24, // <576px
    sm: 24, // ≥576px
    md: 24, // ≥768px
    lg: 12, // ≥992px
    xl: 8, // ≥1200px
    xxl: 8, // ≥1600px
  };
  const [registerTable, { reload, setProps }] = useTable({
    api: dataRuleList,
    columns: dataRuleColumns,
    size: 'small',
    formConfig: {
      baseColProps: adaptiveColProps,
      labelAlign: 'right',
      labelCol: {
        offset: 1,
        xs: 24,
        sm: 24,
        md: 24,
        lg: 8,
        xl: 8,
        xxl: 8,
      },
      wrapperCol: {},
      schemas: dataRuleSearchFormSchema,
      autoSubmitOnEnter: true,
    },
    striped: true,
    useSearchForm: true,
    bordered: true,
    showIndexColumn: false,
    showTableSetting: false,
    canResize: false,
    immediate: false,
    actionColumn: {
      width: 100,
      title: '작업',
      dataIndex: 'action',
      slots: { customRender: 'action' },
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
    await deleteRule({ id: record.id }, reload);
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
</script>
