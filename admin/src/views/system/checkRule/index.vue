<template>
  <div :class="prefixCls">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--슬롯: 테이블 제목-->
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd">등록</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Export</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">Import</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
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

    <CheckRuleTestModal @register="testModal"></CheckRuleTestModal>
    <CheckRuleModal @register="registerModal" @success="reload"></CheckRuleModal>
  </div>
</template>

<script name="system-fillrule" lang="ts" setup>
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';

  import { useModal } from '/@/components/Modal';
  import {
    getCheckRuleList,
    exportUrl,
    importUrl,
    deleteCheckRule,
    batchDeleteCheckRule,
    validateCheckRule,
  } from '/@/views/system/checkRule/check.rule.api';
  import { columns, searchFormSchema } from '/@/views/system/checkRule/check.rule.data';
  import { ActionItem } from '/@/components/Table';

  import CheckRuleTestModal from '/@/views/system/checkRule/CheckRuleTestModal.vue';
  const [testModal, { openModal: openTestModal }] = useModal();

  import CheckRuleModal from '/@/views/system/checkRule/CheckRuleModal.vue';
  const [registerModal, { openModal }] = useModal();

  // 목록 페이지 공개 매개변수 및 메소드
  const { prefixCls, tableContext, createMessage, onExportXls, onImportXls, createSuccessModal } = useListPage({
    designScope: 'check-rule',
    tableProps: {
      title: '코드 검증 규칙 관리 페이지',
      api: getCheckRuleList,
      columns: columns,
      showIndexColumn: true,
      formConfig: {
        schemas: searchFormSchema,
      },
    },
    exportConfig: {
      url: exportUrl,
      name: '코드 검증 규칙 목록',
    },
    importConfig: {
      url: importUrl,
      success: () => reload(),
    },
  });
  // ListTable 등록
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * 새로운 이벤트
   */
  function handleAdd() {
    openModal(true, {
      isUpdate: false,
    });
  }

  /**
   * 이벤트 편집
   */
  function handleEdit(record) {
    console.log('record....', record);
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * 이벤트 삭제
   */
  async function handleDelete(record) {
    console.log(12345, record);
    await deleteCheckRule({ id: record.id }, reload);
  }

  /**
   * 일괄 삭제 이벤트
   */
  async function batchHandleDelete() {
    await batchDeleteCheckRule({ ids: selectedRowKeys.value }, () => {
      selectedRowKeys.value = [];
      reload();
    });
  }

  /**
   * 기능 테스트
   */
  function testRule(record) {
    openTestModal(true, { ruleCode: record.ruleCode });
  }

  /**
   * 편집
   */
  function getTableAction(record): ActionItem[] {
    return [{ label: '편집', onClick: handleEdit.bind(null, record) }];
  }

  /**
   * 드롭다운 작업 표시줄
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      { label: '기능 테스트', onClick: testRule.bind(null, record) },
      {
        label: '삭제',
        color: 'error',
        popConfirm: {
          title: '삭제하시겠습니까?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
