<!--
  @component SystemRoleManagement
  @description RBAC 기반 시스템 역할(Role) 마스터 및 권한 매트릭스 설정 화면
  @features
    - 역할(Role) 등록 및 코드(RoleCode) 중복 검증
    - 역할별 허용 메뉴 트리 및 기능 버튼 권한(Permission ID) 인가 매핑 드로어 연동
    - 역할별 사용자 배정 현황 열람
-->
<template>
  <BasicTable @register="registerTable">
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> 등록</a-button>
      <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Export</a-button>
      <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">Import</j-upload-button>
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
      title: '시스템 역할 목록',
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
      name: '역할 목록',
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
        label: '사용자',
        onClick: handleUser.bind(null, record),
      },
      {
        label: '권한',
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
        label: '편집',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: '상세',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: '삭제',
        popConfirm: {
          title: '삭제 하시겠습니까?',
          confirm: handleDelete.bind(null, record),
        },
      },
      {
        label: '홈페이지 구성',
        onClick: handleIndexConfig.bind(null, record.roleCode),
      },
    ];
  }
</script>
