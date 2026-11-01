<template>
  <div>
    <!--참고표-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--슬롯: 테이블 제목-->
      <template #tableTitle>
        <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined">
          등록
        </a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                삭제
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            일괄 작업
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--액션바-->
      <template #action="{ record }">
        <TableAction
          :actions="getTableAction(record)"
          :dropDownActions="getDropDownAction(record)"
        />
      </template>
    </BasicTable>

    <!-- Form 영역 -->
    <SysTableWhiteListModal @register="registerModal" @success="handleSuccess"/>
  </div>
</template>

<script lang="ts" name="system-table-whitelist-view" setup>
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage';
import SysTableWhiteListModal from './modules/SysTableWhiteListModal.vue';
import { columns, searchFormSchema } from './SysTableWhiteList.data';
import { batchDelete, deleteOne, getExportUrl, getImportUrl, list } from './SysTableWhiteList.api';

//모델등록
const [registerModal, { openModal }] = useModal();
//테이블 데이터 등록
const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
  designScope: 'table-whitelist',
  tableProps: {
    title: '테이블 화이트리스트 목록',
    api: list,
    columns,
    canResize: false,
    formConfig: {
      labelWidth: 120,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
    },
    actionColumn: {
      width: 140,
      title: '작업',
    },
    showIndexColumn: true,
  },
  exportConfig: {
    name: '테이블 화이트리스트',
    url: getExportUrl,
  },
  importConfig: {
    url: getImportUrl,
  },
});

const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

/**
 * 새로운 이벤트
 */
function handleAdd() {
  openModal(true, {
    isUpdate: false,
    showFooter: true,
  });
}

/**
 * 이벤트 편집
 */
function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: true,
  });
}

/**
 * 세부
 */
function handleDetail(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: false,
  });
}

/**
 * 이벤트 삭제
 */
async function handleDelete(record) {
  await deleteOne({ id: record.id }, reload);
}

/**
 * 일괄 삭제 이벤트
 */
async function batchHandleDelete() {
  await batchDelete({ ids: selectedRowKeys.value }, reload);
}

/**
 * 성공 콜백
 */
function handleSuccess() {
  reload();
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
  ];
}

/**
 * 드롭다운 작업 표시줄
 */
function getDropDownAction(record) {
  return [
    {
      label: '상세',
      onClick: handleDetail.bind(null, record),
    },
    {
      label: '삭제',
      color: 'error',
      popConfirm: {
        title: '정말 삭제하시겠습니까?',
        confirm: handleDelete.bind(null, record),
      },
    },
  ];
}
</script>

<style scoped>
</style>
