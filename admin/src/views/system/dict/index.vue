<!--
  @component SystemDictManagement
  @description Enterprise standard common code and system dictionary (SysDict) management view
  @features
    - Dictionary code and name master management
    - Per-dictionary item label and value detail grid
    - Automatic Redis cache invalidation on create/update/delete
-->
<template>
  <!--참고표-->
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <!--슬롯: 테이블 제목-->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate">{{ t('appUi.k46135abd') }}</a-button>
      <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls">{{ t('appUi.kc3a6ac35') }}</a-button>
      <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">{{ t('appUi.k7cf2b30a') }}</j-upload-button>
      <a-button type="primary" @click="handlerRefreshCache" preIcon="ant-design:sync-outlined">{{ t('appUi.kd46cfdae') }}</a-button>
      <a-button type="primary" @click="openRecycleModal(true)" preIcon="ant-design:hdd-outlined">{{ t('appUi.k5b601a30') }}</a-button>

      <a-dropdown v-if="selectedRowKeys.length > 0">
        <template #overlay>
          <a-menu>
            <a-menu-item key="1" @click="batchHandleDelete">
              <Icon icon="ant-design:delete-outlined"></Icon>
              {{ t('appUi.kfc81e222') }}
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
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  //삭제 모델
  const [registerModal1, { openModal: openRecycleModal }] = useModal();

  // 목록 페이지 공개 매개변수 및 메소드
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    designScope: 'dict-template',
    tableProps: {
      title: t('appUi.kba99b30b'),
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
      name: t('appUi.k7aa65826'),
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
      createMessage.success(t('appUi.kd5b58e07'));
    } else {
      createMessage.error(t('appUi.k14f9e5fd'));
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
        label: t('appUi.kd482e14b'),
        onClick: handleEdit.bind(null, record),
      },
      {
        label: t('appUi.k3a4c63cd'),
        onClick: handleItem.bind(null, record),
      },
      {
        label: t('appUi.kfc81e222'),
        popConfirm: {
          title: t('appUi.k5da9b401'),
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>

<style scoped></style>
