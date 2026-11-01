<template>
  <BasicDrawer @register="registerBaseDrawer" title="역할 사용자" width="800" destroyOnClose>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" @click="handleCreate" v-if="!disableUserEdit"> 사용자 추가</a-button>
        <a-button type="primary" @click="handleSelect"> 사용자 찾기</a-button>

        <a-dropdown v-if="checkedKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="bx:bx-unlink"></Icon>
                연결 해제
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >일괄 작업
            <Icon icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <!--사용자 작업 서랍-->
    <UserDrawer @register="registerDrawer" @success="handleSuccess" />
    <!--사용자 선택 팝업-->
    <UseSelectModal @register="registerModal" @select="selectOk" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, watch, unref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/src/components/Table';
  import { BasicDrawer, useDrawer, useDrawerInner } from '/src/components/Drawer';
  import { useModal } from '/src/components/Modal';
  import UserDrawer from '../../user/UserDrawer.vue';
  import UseSelectModal from './UseSelectModal.vue';
  import { userList, deleteUserRole, batchDeleteUserRole, addUserRole } from '../role.api';
  import { userColumns, searchUserFormSchema } from '../role.data';
  import { getUserRoles } from '../../user/user.api';

  const emit = defineEmits(['register', 'hideUserList']);
  const props = defineProps({
    disableUserEdit: {type:Boolean,default:false}
  })

  const checkedKeys = ref<Array<string | number>>([]);
  const roleId = ref('');
  const [registerBaseDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    roleId.value = data.id;
    setProps({ searchInfo: { roleId: data.id } });
    reload();
  });
  //서랍 등록
  const [registerDrawer, { openDrawer }] = useDrawer();
  //서랍 등록
  const [registerModal, { openModal }] = useModal();
  const [registerTable, { reload, updateTableDataRecord, setProps }] = useTable({
    title: '사용자 목록',
    api: userList,
    columns: userColumns,
    formConfig: {
      labelWidth: 60,
      schemas: searchUserFormSchema,
      autoSubmitOnEnter: true,
    },
    striped: true,
    useSearchForm: true,
    showTableSetting: true,
    clickToRowSelect: false,
    bordered: true,
    showIndexColumn: false,
    // 【issues/1064】열 설정을 위한 캐시키
    tableSetting: { fullScreen: true, cacheKey: 'role_user_table' },
    canResize: false,
    rowKey: 'id',
    actionColumn: {
      width: 180,
      title: '작업',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: undefined,
    },
  });

  /**
   * 컬럼 구성 선택
   */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 50,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };

  /**
   * 이벤트 선택
   */
  function onSelectChange(selectedRowKeys: (string | number)[], selectionRows) {
    checkedKeys.value = selectedRowKeys;
  }

  /**
   * 새로운
   */
  function handleCreate() {
    openDrawer(true, {
      isUpdate: false,
      selectedroles: [roleId.value],
      isRole: true,
    });
  }
  /**
   * 편집
   */
  async function handleEdit(record: Recordable) {
    try {
      const userRoles = await getUserRoles({ userid: record.id });
      if (userRoles && userRoles.length > 0) {
        record.selectedroles = userRoles;
      }
    } catch (e) {
      console.log(e);
    }
    openDrawer(true, {
      record,
      isUpdate: true,
      isRole: true,
    });
  }

  /**
   * 이벤트 삭제
   */
  async function handleDelete(record) {
    await deleteUserRole({ userId: record.id, roleId: roleId.value }, reload);
  }

  /**
   * 일괄 삭제 이벤트
   */
  async function batchHandleDelete() {
    await batchDeleteUserRole({ userIds: checkedKeys.value.join(','), roleId: roleId.value }, reload);
  }

  /**
   * 성공적인 콜백
   */
  function handleSuccess({ isUpdate, values }) {
    isUpdate ? updateTableDataRecord(values.id, values) : reload();
  }
  /**
   * 기존 사용자 선택
   */
  function handleSelect() {
    openModal(true);
  }
  /**
   * 기존 사용자 추가
   */
  async function selectOk(val) {
    await addUserRole({ roleId: roleId.value, userIdList: val }, reload);
  }
  /**
   * 액션바
   */
  function getTableAction(record) {
    return [
      {
        label: '편집',
        onClick: handleEdit.bind(null, record),
        ifShow: () => !props.disableUserEdit,
      },
      {
        label: '연결 해제',
        popConfirm: {
          title: '연결을 취소하시겠습니까?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>

<style scoped>
  :deep(.ant-form-item-control-input-content){
    text-align: left;
  }
</style>
