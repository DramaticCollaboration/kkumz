<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd">등록</a-button>


        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                삭제
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >일괄 작업
            <Icon style="fontsize: 12px" icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <NoticeModal @register="registerModal" @success="reload" />
    <DetailModal @register="register" :frameSrc="iframeUrl" />
  </div>
</template>
<script lang="ts" name="system-notice" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import NoticeModal from './NoticeModal.vue';
  import DetailModal from './DetailModal.vue';
  import { useMethods } from '/@/hooks/system/useMethods';
  import { useGlobSetting } from '/@/hooks/setting';
  import { getToken } from '/@/utils/auth';
  import { columns, searchFormSchema } from './notice.data';
  import { getList, deleteNotice, batchDeleteNotice, getExportUrl, getImportUrl, doReleaseData, doReovkeData } from './notice.api';
  import { useListPage } from '/@/hooks/system/useListPage';
  const glob = useGlobSetting();
  const [registerModal, { openModal }] = useModal();
  const [register, { openModal: openDetail }] = useModal();
  const iframeUrl = ref('');

  // 목록 페이지 공개 매개변수 및 메소드
  const { prefixCls, onExportXls, onImportXls, tableContext, doRequest } = useListPage({
    designScope: 'notice-template',
    tableProps: {
      title: '메시지 알림',
      api: getList,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
    },
    exportConfig: {
      name: '메시지 알림 목록',
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
    });
  }

  /**
   * 이벤트 편집
   */
  function handleEdit(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * 이벤트 삭제
   */
  async function handleDelete(record) {
    await deleteNotice({ id: record.id }, reload);
  }

  /**
   * 일괄 삭제 이벤트
   */
  async function batchHandleDelete() {
    doRequest(() => batchDeleteNotice({ ids: selectedRowKeys.value }));
  }
  /**
   * 게시
   */
  async function handleRelease(id) {
    await doReleaseData({ id });
    reload();
  }
  /**
   * 취소
   */
  async function handleReovke(id) {
    await doReovkeData({ id });
    reload();
  }
  /**
   * 확인
   */
  function handleDetail(record) {
    iframeUrl.value = `${glob.uploadUrl}/api/v1/kkumz/sys/annountCement/show/${record.id}?token=${getToken()}`;
    openDetail(true);
  }
  /**
   * 연산 열 정의
   * @param record
   */
  function getActions(record) {
    return [
      {
        label: '편집',
        onClick: handleEdit.bind(null, record),
        ifShow: record.sendStatus == 0,
      },
    ];
  }
  /**
   * 드롭다운 작업 표시줄
   */
  function getDropDownAction(record) {
    return [
      {
        label: '삭제',
        ifShow: record.sendStatus != 1,
        popConfirm: {
          title: '삭제 하시겠습니까?',
          confirm: handleDelete.bind(null, record),
        },
      },
      {
        label: '풀어 주다',
        ifShow: record.sendStatus == 0,
        onClick: handleRelease.bind(null, record.id),
      },
      {
        label: '취소',
        ifShow: record.sendStatus == 1,
        popConfirm: {
          title: '취소하시겠습니까?',
          confirm: handleReovke.bind(null, record.id),
        },
      },
      {
        label: '확인',
        onClick: handleDetail.bind(null, record),
      },
    ];
  }
</script>
