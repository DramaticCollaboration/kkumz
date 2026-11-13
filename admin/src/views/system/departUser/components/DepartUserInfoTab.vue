<template>
  <!--참고표-->
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <!--슬롯: 테이블 제목-->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="selectAddUser">기존 사용자 추가</a-button>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="createUser">사용자 등록</a-button>
      <template v-if="selectedRowKeys.length > 0">
        <a-dropdown>
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onUnlinkDepartUserBatch">
                <icon icon="bx:bx-unlink" />
                <span>연결 해제</span>
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            <span>일괄 작업 </span>
            <icon icon="akar-icons:chevron-down" />
          </a-button>
        </a-dropdown>
      </template>
    </template>
    <!-- 슬롯：인라인 작업 버튼 -->
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
    </template>
  </BasicTable>
  <UserDrawer @register="registerDrawer" @success="onUserDrawerSuccess" />
  <DepartRoleUserAuthDrawer @register="registerUserAuthDrawer" />
  <UserSelectModal rowKey="id" @register="registerSelUserModal" @getSelectResult="onSelectUserOk" />
</template>

<script lang="ts" setup>
  import { computed, inject, ref, unref, watch } from 'vue';
  import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';

  import UserDrawer from '/@/views/system/user/UserDrawer.vue';
  import UserSelectModal from '/@/components/Form/src/sync/components/modal/UserSelectModal.vue';
  import DepartRoleUserAuthDrawer from './DepartRoleUserAuthDrawer.vue';
  import { departUserList, linkDepartUserBatch, unlinkDepartUserBatch } from '../depart.user.api';
  import { userInfoColumns, userInfoSearchFormSchema } from '../depart.user.data';
  import { ColEx } from '/@/components/Form/src/types';

  const prefixCls = inject('prefixCls');
  const props = defineProps({
    data: { require: true, type: Object },
  });
  // 현재 선택된 부서 ID가 비어 있을 수 있습니다. 이는 선택된 부서가 없음을 나타냅니다.
  const departId = computed(() => props.data?.id);

  // 적응형 열 구성
  const adaptiveColProps: Partial<ColEx> = {
    xs: 24, // <576px
    sm: 24, // ≥576px
    md: 24, // ≥768px
    lg: 12, // ≥992px
    xl: 12, // ≥1200px
    xxl: 8, // ≥1600px
  };
  // 목록 페이지 공개 매개변수 및 메소드
  const { tableContext, createMessage } = useListPage({
    tableProps: {
      api: departUserList,
      columns: userInfoColumns,
      canResize: false,
      formConfig: {
        schemas: userInfoSearchFormSchema,
        baseColProps: adaptiveColProps,
        labelAlign: 'left',
        labelCol: {
          xs: 24,
          sm: 24,
          md: 24,
          lg: 9,
          xl: 7,
          xxl: 5,
        },
        wrapperCol: {},
        // 액션 버튼 구성
        actionColOptions: {
          ...adaptiveColProps,
          style: { textAlign: 'left' },
        },
      },
      // 【issues/1064】열 설정을 위한 캐시키
      tableSetting: { cacheKey: 'depart_user_userInfo' },
      // 요청하기 전에 매개변수를 처리하세요.
      beforeFetch(params) {
        params.depId = departId.value;
      },
    },
  });

  // ListTable 등록
  const [registerTable, { reload, setProps, setLoading, updateTableDataRecord }, { rowSelection, selectedRowKeys }] = tableContext;

  watch(
    () => props.data,
    () => reload()
  );
  //서랍 등록
  const [registerDrawer, { openDrawer, setDrawerProps }] = useDrawer();
  const [registerUserAuthDrawer, userAuthDrawer] = useDrawer();
  // 등록된 사용자는 모달을 선택합니다
  const [registerSelUserModal, selUserModal] = useModal();

  // 선택한 행 지우기
  function clearSelection() {
    selectedRowKeys.value = [];
  }

  // 부서 역할 보기
  function showDepartRole(record) {
    userAuthDrawer.openDrawer(true, { userId: record.id, departId });
  }

  // 사용자 생성
  function createUser() {
    if (!departId.value) {
      createMessage.warning('먼저 부서를 선택해주세요');
    } else {
      openDrawer(true, {
        isUpdate: false,
        departDisabled: true,
        // 초기화 담당부서
        nextDepartOptions: { value: props.data?.key, label: props.data?.title },
        record: {
          userIdentity: 1,
          selecteddeparts: departId.value,
        },
      });
    }
  }

  // 사용자 세부정보 보기
  function showUserDetail(record) {
    openDrawer(true, {
      record,
      isUpdate: true,
      departDisabled: true,
      showFooter: false,
    });
  }

  // 사용자 정보 편집
  function editUserInfo(record) {
    openDrawer(true, { isUpdate: true, record, departDisabled: true });
  }

  // 기존 사용자를 추가하려면 선택하세요.
  function selectAddUser() {
    selUserModal.openModal();
  }

  // 부서와 사용자의 관계를 일괄적으로 해제
  async function unlinkDepartUser(idList, confirm) {
    if (!departId.value) {
      createMessage.warning('먼저 부서를 선택해주세요');
    } else {
      setLoading(true);
      let userIds = unref(idList).join(',');
      try {
        await unlinkDepartUserBatch({ depId: departId.value, userIds }, confirm);
        return reload();
      } finally {
        setLoading(false);
      }
    }
    return Promise.reject();
  }

  // 일괄적으로 이벤트 연결 해제
  async function onUnlinkDepartUserBatch() {
    try {
      await unlinkDepartUser(selectedRowKeys, true);
      // 일괄 삭제 성공 후 선택 지우기
      clearSelection();
    } catch (e) {}
  }

  // 사용자를 성공적으로 선택했습니다
  async function onSelectUserOk(options, userIdList) {
    if (userIdList.length > 0) {
      try {
        setLoading(true);
        await linkDepartUserBatch(departId.value, userIdList);
        reload();
      } finally {
        setLoading(false);
      }
    }
  }

  /**
   * 사용자 서랍 Form의 성공적인 콜백
   */
  function onUserDrawerSuccess({ isUpdate, values }) {
    isUpdate ? updateTableDataRecord(values.id, values) : reload();
  }

  /**
   * 액션바
   */
  function getTableAction(record): ActionItem[] {
    return [{ label: '편집', onClick: editUserInfo.bind(null, record) }];
  }

  /**
   * 드롭다운 작업 표시줄
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      { label: '부서 역할', onClick: showDepartRole.bind(null, record) },
      { label: '사용자 세부정보', onClick: showUserDetail.bind(null, record) },
      {
        label: '연결 해제',
        color: 'error',
        popConfirm: {
          title: '선택한 사용자의 부서 연결을 해제하시겠습니까?',
          confirm: unlinkDepartUser.bind(null, [record.id], false),
        },
      },
    ];
  }
</script>
