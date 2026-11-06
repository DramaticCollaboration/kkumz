<!--
  @component SystemDictManagement
  @description 전사 표준 공통 코드 및 시스템 사전(SysDict) 관리 화면
  @features
    - 사전 코드 및 사전 명칭 마스터 관리
    - 사전별 항목 명칭 및 값 세부 목록 그리드
    - 등록/수정/삭제 시 Redis 캐시 자동 무효화 연동
-->
<template>
  <!--참고표-->
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <!--슬롯: 테이블 제목-->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> 등록</a-button>
      <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Export</a-button>
      <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">Import</j-upload-button>
      <a-button type="primary" @click="handlerRefreshCache" preIcon="ant-design:sync-outlined"> 캐시 갱신</a-button>
      <a-button type="primary" @click="openRecycleModal(true)" preIcon="ant-design:hdd-outlined"> 삭제</a-button>

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
          <Icon icon="ant-design:down-outlined"></Icon>
        </a-button>
      </a-dropdown>
    </template>
    <!--액션바-->
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" />
    </template>
  </BasicTable>
  <!--사전 팝업-->
  <DictModal @register="registerModal" @success="handleSuccess" />
  <!--사전 구성 서랍-->
  <DictItemList @register="registerDrawer" />
  <!--삭제 팝업창-->
  <DictRecycleBinModal @register="registerModal1" @success="reload" />
</template>

<script lang="ts" name="system-dict" setup>
  //TS 구문
  import { ref, computed, unref } from 'vue';
  import { BasicTable, TableAction } from '/src/components/Table';
  import { useDrawer } from '/src/components/Drawer';
  import { useModal } from '/src/components/Modal';
  import DictItemList from './components/DictItemList.vue';
  import DictModal from './components/DictModal.vue';
  import DictRecycleBinModal from './components/DictRecycleBinModal.vue';
  import { useMessage } from '/src/hooks/web/useMessage';
  import { removeAuthCache, setAuthCache } from '/src/utils/auth';
  import { columns, searchFormSchema } from './dict.data';
  import { list, deleteDict, batchDeleteDict, getExportUrl, getImportUrl, refreshCache, queryAllDictItems } from './dict.api';
  import { DB_DICT_DATA_KEY } from '/src/enums/cacheEnum';
  import { useUserStore } from '/@/store/modules/user';

  const { createMessage } = useMessage();
  //사전 모델
  const [registerModal, { openModal }] = useModal();
  //사전 구성 서랍
  const [registerDrawer, { openDrawer }] = useDrawer();
  import { useListPage } from '/@/hooks/system/useListPage';

  //삭제 모델
  const [registerModal1, { openModal: openRecycleModal }] = useModal();

  // 목록 페이지 공개 매개변수 및 메소드
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    designScope: 'dict-template',
    tableProps: {
      title: '데이터 사전',
      api: list,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 240,
      },
    },
    exportConfig: {
      name: '데이터 사전 목록',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  //테이블 데이터 등록
  const [registerTable, { reload, updateTableDataRecord }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * 새로운 이벤트
   */
  function handleCreate() {
    openModal(true, {
      isUpdate: false,
    });
  }
  /**
   * 이벤트 편집
   */
  async function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   *세부
   */
  async function handleDetail(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * 이벤트 삭제
   */
  async function handleDelete(record) {
    await deleteDict({ id: record.id }, reload);
  }
  /**
   * 일괄 삭제 이벤트
   */
  async function batchHandleDelete() {
    await batchDeleteDict({ ids: selectedRowKeys.value }, reload);
  }
  /**
   * 성공적인 콜백
   */
  function handleSuccess({ isUpdate, values }) {
    if (isUpdate) {
      updateTableDataRecord(values.id, values);
    } else {
      reload();
    }
  }
  /**
   * 캐시 새로 고침
   */
  async function handlerRefreshCache() {
    const result = await refreshCache();
    if (result.success) {
      const res = await queryAllDictItems();
      removeAuthCache(DB_DICT_DATA_KEY);
      const userStore = useUserStore();
      userStore.setAllDictItems(res.result);
      createMessage.success('캐시 갱신이 완료되었습니다!');
    } else {
      createMessage.error('캐시를 새로 고치지 못했습니다!');
    }
  }
  /**
   * 사전 구성
   */
  function handleItem(record) {
    openDrawer(true, {
      id: record.id,
    });
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
        label: '사전 구성',
        onClick: handleItem.bind(null, record),
      },
      {
        label: '삭제',
        popConfirm: {
          title: '삭제하시겠습니까?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>

<style scoped></style>
