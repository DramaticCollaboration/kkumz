<!--
  @component SystemRoleManagement
  @description RBAC system role master and permission matrix view
  @features
    - Role registration and RoleCode uniqueness validation
    - Role menu tree and button permission assignment drawer
    - View users assigned per role
-->
<template>
  <BasicTable @register="registerTable">
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> {{ t('appUi.k1ac11e9b') }}</a-button>
      <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Export</a-button>
      <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">Import</j-upload-button>
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
          <Icon icon="mdi:chevron-down"></Icon>
        </a-button>
      </a-dropdown>
    </template>
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
    </template>
  </BasicTable>
  <!--역할 사용자 Form-->
  <RoleUserTable @register="roleUserDrawer" />
  <!--역할 편집 서랍-->
  <RoleDrawer @register="registerDrawer" @success="reload" :showFooter="showFooter" />
  <!--역할 세부정보-->
  <RoleDesc @register="registerDesc"></RoleDesc>
  <!--역할 메뉴 권한 서랍-->
  <RolePermissionDrawer @register="rolePermissionDrawer" />
  <!--역할 홈 페이지 구성-->
  <RoleIndexModal @register="registerIndexModal" />
</template>
<script lang="ts" name="system-role" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useDrawer } from '/@/components/Drawer';
  import { useModal } from '/@/components/Modal';
  import RoleDrawer from './components/RoleDrawer.vue';
  import RoleDesc from './components/RoleDesc.vue';
  import RolePermissionDrawer from './components/RolePermissionDrawer.vue';
  import RoleIndexModal from './components/RoleIndexModal.vue';
  import RoleUserTable from './components/RoleUserTable.vue';
  import { columns, searchFormSchema } from './role.data';
  import { list, deleteRole, batchDeleteRole, getExportUrl, getImportUrl } from './role.api';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  const showFooter = ref(true);
  const [roleUserDrawer, { openDrawer: openRoleUserDrawer }] = useDrawer();
  const [registerDrawer, { openDrawer }] = useDrawer();
  const [registerIndexModal, { openModal: openIndexModal }] = useModal();
  const [rolePermissionDrawer, { openDrawer: openRolePermissionDrawer }] = useDrawer();
  const [registerDesc, { openDrawer: openRoleDesc }] = useDrawer();

  // 목록 페이지 공개 매개변수 및 메소드
  const { prefixCls, tableContext, onImportXls, onExportXls } = useListPage({
    designScope: 'role-template',
    tableProps: {
      title: t('appUi.k09ea2a45'),
      api: list,
      columns: columns,
      formConfig: {
        labelWidth:65,
        rowProps: { gutter: 24 },
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 120,
      },
      rowSelection: null,
      //기본 정렬 사용자 정의
      defSort: {
        column: 'id',
        order: 'desc',
      },
    },
    exportConfig: {
      name: t('appUi.kaa93c339'),
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * 새로운
   */
  function handleCreate() {
    showFooter.value = true;
    openDrawer(true, {
      isUpdate: false,
    });
  }
  /**
   * 편집
   */
  function handleEdit(record: Recordable) {
    showFooter.value = true;
    openDrawer(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   *세부
   */
  function handleDetail(record) {
    showFooter.value = false;
    openRoleDesc(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * 이벤트 삭제
   */
  async function handleDelete(record) {
    await deleteRole({ id: record.id }, reload);
  }
  /**
   * 일괄 삭제 이벤트
   */
  async function batchHandleDelete() {
    await batchDeleteRole({ ids: selectedRowKeys.value }, reload);
  }
  /**
   * 역할 승인 팝업창
   */
  function handlePerssion(record) {
    openRolePermissionDrawer(true, { roleId: record.id });
  }
  /**
   * 홈페이지 구성 팝업창
   */
  function handleIndexConfig(roleCode) {
    openIndexModal(true, { roleCode });
  }
  /**
   * 역할 사용자
   */
  function handleUser(record) {
    //onSelectChange(selectedRowKeys)
    openRoleUserDrawer(true, record);
  }
  /**
   * 액션바
   */
  function getTableAction(record) {
    return [
      {
        label: t('appUi.k5c50d9e5'),
        onClick: handleUser.bind(null, record),
      },
      {
        label: t('appUi.k4d02bde7'),
        onClick: handlePerssion.bind(null, record),
      },
    ];
  }

  /**
   * 드롭다운 작업 표시줄
   */
  function getDropDownAction(record) {
    return [
      {
        label: t('appUi.kd482e14b'),
        onClick: handleEdit.bind(null, record),
      },
      {
        label: t('appUi.kbb446431'),
        onClick: handleDetail.bind(null, record),
      },
      {
        label: t('appUi.kfc81e222'),
        popConfirm: {
          title: t('appUi.ka6e94ce1'),
          confirm: handleDelete.bind(null, record),
        },
      },
      {
        label: t('appUi.kddd89b73'),
        onClick: handleIndexConfig.bind(null, record.roleCode),
      },
    ];
  }
</script>
