<template>
  <div :class="prefixCls">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--슬롯: 테이블 제목-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAdd">등록</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Export</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">Import</j-upload-button>
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
    <TemplateModal @register="registerModal" @success="reload" />
    <TemplateTestModal @register="registerTestModal" />
  </div>
</template>

<script lang="ts" setup name="message-template">
  import { unref, computed, toRaw } from 'vue';
  import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import TemplateModal from './TemplateModal.vue';
  import TemplateTestModal from './TemplateTestModal.vue';
  import { Api, saveOrUpdate, list, deleteBatch } from './template.api';
  import { columns, searchFormSchema } from './template.data';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage } = useMessage();

  // 목록 페이지 공개 매개변수 및 메소드
  const { prefixCls, onExportXls, onImportXls, tableContext } = useListPage({
    designScope: 'message-template',
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
  const [registerTable, { reload, setLoading }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;
  const [registerModal, { openModal }] = useModal();
  const [registerTestModal, testModal] = useModal();
  const showBatchBtn = computed(() => selectedRowKeys.value.length > 0);

  function onAdd() {
    openModal(true, {
      title: '메시지 템플릿 추가',
      isUpdate: false,
      showFooter: true,
      record: {},
    });
  }

  function onEdit(record) {
    if (record.useStatus === '1') {
      createMessage.warning('이 템플릿은 적용되었으며 편집이 금지되어 있습니다!');
      return;
    }
    openModal(true, {
      title: '메시지 템플릿 수정',
      isUpdate: true,
      record: record,
      showFooter: true,
    });
  }

  function onDelete(record) {
    if (record) {
      if(record.useStatus == '1'){
        createMessage.warning('이 템플릿은 애플리케이션에 의해 삭제가 금지되었습니다!');
        return;
      }
      doDeleteDepart([record.id], false);
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

  async function onDeleteBatch() {
    try {
      let arr = toRaw(selectedRows.value);
      let temp = arr.filter(item=>item.useStatus=='1')
      if(temp.length>0){
        createMessage.warning('선택한 템플릿은 적용되었으며 삭제가 금지됩니다!');
        return;
      }
      await doDeleteDepart(selectedRowKeys);
      selectedRowKeys.value = [];
    } finally {
    }
  }

  // 메시지 보내기 테스트
  function onSendTest(record) {
    testModal.openModal(true, { record });
  }

  /**
   * 액션바
   */
  function getTableAction(record): ActionItem[] {
    return [{ label: '확인', onClick: handleDetail.bind(null, record)}, { label: '편집', onClick: onEdit.bind(null, record) }];
  }

  /**
   * 드롭다운 작업 표시줄
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      { label: '애플리케이션', onClick: handleUse.bind(null, record) },
      { label: '비활성화', onClick: handleNotUse.bind(null, record) },
      { label: '테스트 보내기', onClick: onSendTest.bind(null, record) },
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

  /**
   * 애플리케이션
   */
  async function handleUse(record) {
    let param = { id: record.id, useStatus: '1' };
    await saveOrUpdate(param, true);
    await reload();
  }

  /**
   * 비활성화
   */
  async function handleNotUse(record) {
    let param = { id: record.id, useStatus: '0' };
    await saveOrUpdate(param, true);
    await reload();
  }

  /**
   * 확인
   * @param record
   */
  function handleDetail(record) {
    openModal(true,{
      title: "메시지 템플릿 세부정보",
      isUpdate: true,
      showFooter: false,
      record:record
    })
  }
</script>

<style lang="less">
  @import 'index';
</style>
