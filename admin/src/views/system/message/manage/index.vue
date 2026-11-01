<template>
  <div :class="prefixCls">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--슬롯: 테이블 제목-->
      <template #tableTitle>
        <a-dropdown v-if="showBatchBtn">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onDeleteBatch">
                <Icon icon="ant-design:delete-outlined"></Icon>
                <span>삭제</span>
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            <span>일괄 작업</span>
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>

      <!--액션바-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <ManageDrawer @register="registerDrawer" />
  </div>
</template>

<script lang="ts" setup name="message-manage">
  import { unref, computed } from 'vue';
  import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';
  import ManageDrawer from './ManageDrawer.vue';
  import { Api, list, deleteBatch } from './manage.api';
  import { columns, searchFormSchema } from './manage.data';

  // 목록 페이지 공개 매개변수 및 메소드
  const { prefixCls, tableContext } = useListPage({
    designScope: 'message-manage',
    tableProps: {
      title: '메시지 센터 템플릿 목록 데이터',
      api: list,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
    },
    exportConfig: {
      url: Api.exportXls,
      name: '메시지 센터 템플릿 목록',
    },
    importConfig: {
      url: Api.importXls,
      success: () => reload(),
    },
  });

  // ListTable 등록
  const [registerTable, { reload, setLoading }, { rowSelection, selectedRowKeys }] = tableContext;
  const showBatchBtn = computed(() => selectedRowKeys.value.length > 0);

  const [registerDrawer, { openDrawer }] = useDrawer();

  function onDetail(record) {
    openDrawer(true, { record: record });
  }

  function onDelete(record) {
    if (record) {
      doDeleteDepart([record.id], false);
    }
  }

  async function onDeleteBatch() {
    try {
      await doDeleteDepart(selectedRowKeys);
      selectedRowKeys.value = [];
    } finally {
    }
  }

  /**
   * ID 기반 일괄 삭제
   * @param idListRef array
   * @param 확인 확인 프롬프트 상자 표시 여부
   */
  async function doDeleteDepart(idListRef, confirm = true) {
    const idList = unref(idListRef);
    if (idList.length > 0) {
      try {
        setLoading(true);
        await deleteBatch({ ids: idList.join(',') }, confirm);
        await reload();
      } finally {
        setLoading(false);
      }
    }
  }

  /**
   * 액션바
   */
  function getTableAction(record): ActionItem[] {
    return [{ label: '상세', onClick: onDetail.bind(null, record) }];
  }

  /**
   * 드롭다운 작업 표시줄
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      {
        label: '삭제',
        color: 'error',
        popConfirm: {
          title: '삭제하시겠습니까?',
          confirm: onDelete.bind(null, record),
        },
      },
    ];
  }
</script>

<style lang="less">
  @import 'index';
</style>
