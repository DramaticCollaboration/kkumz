<template>
  <!--참고표-->
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <!--슬롯: 테이블 제목-->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="addDepartRole">부서 역할 추가</a-button>
      <template v-if="selectedRowKeys.length > 0">
        <a-divider type="vertical" />
        <a-dropdown>
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onDeleteDepartRoleBatch">
                <icon icon="ant-design:delete-outlined" />
                <span>삭제</span>
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
  <!-- 부서 팝업창 추가 -->
  <DepartRoleModal :departId="departId" @register="registerFormModal" @success="onFormModalSuccess" />
  <DepartRoleAuthDrawer @register="registerAuthDrawer" />
</template>

<script lang="ts" setup>
  import { inject, ref, unref, watch, computed, onMounted } from 'vue';

  import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';

  import DepartRoleModal from './DepartRoleModal.vue';
  import DepartRoleAuthDrawer from './DepartRoleAuthDrawer.vue';
  import { deleteBatchDepartRole, departRoleList } from '../depart.user.api';
  import { departRoleColumns, departRoleSearchFormSchema } from '../depart.user.data';
  import { ColEx } from '/@/components/Form/src/types';

  const prefixCls = inject('prefixCls');
  const props = defineProps({
    data: { require: true, type: Object },
  });
  defineEmits(['register']);
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
      api: departRoleList,
      columns: departRoleColumns,
      canResize: false,
      formConfig: {
        labelWidth: 100,
        schemas: departRoleSearchFormSchema,
        baseColProps: adaptiveColProps,
        labelAlign: 'left',
        labelCol: {
          xs: 24,
          sm: 24,
          md: 24,
          lg: 9,
          xl: 7,
          xxl: 6,
        },
        wrapperCol: {},
        // 액션 버튼 구성
        actionColOptions: {
          ...adaptiveColProps,
          style: { textAlign: 'left' },
        },
      },
      // 【issues/1064】열 설정을 위한 캐시키
      tableSetting: { cacheKey: 'depart_user_departInfo' },
      // 요청하기 전에 매개변수를 처리하세요.
      beforeFetch(params) {
        params.deptId = departId.value;
      },
      immediate: !!departId.value,
    },
  });

  // ListTable 등록
  const [registerTable, { reload, setProps, setLoading, updateTableDataRecord }, { rowSelection, selectedRowKeys }] = tableContext;

  // 등록 Form 팝업 창
  const [registerFormModal, formModal] = useModal();
  // 인증 팝업창 등록
  const [registerAuthDrawer, authDrawer] = useDrawer();

  // 데이터 변경 사항을 수신하고 데이터를 다시 로드합니다.
  watch(
    () => props.data,
    () => reload()
  );
  onMounted(() => {
    // reload();
  });

  // 선택한 행 지우기
  function clearSelection() {
    selectedRowKeys.value = [];
  }

  // 부서 역할 추가
  function addDepartRole() {
    formModal.openModal(true, {
      isUpdate: false,
      record: {},
    });
  }

  // 편집부서의 역할
  function editDepartRole(record) {
    formModal.openModal(true, {
      isUpdate: true,
      record: record,
    });
  }

  // 승인된 부서 역할
  function permissionDepartRole(record) {
    authDrawer.openDrawer(true, { record });
  }

  // 부서 역할을 일괄 삭제
  async function deleteDepartRole(idList, confirm) {
    if (!departId.value) {
      createMessage.warning('먼저 부서를 선택해주세요');
    } else {
      setLoading(true);
      let ids = unref(idList).join(',');
      try {
        await deleteBatchDepartRole({ ids }, confirm);
        return reload();
      } finally {
        setLoading(false);
      }
    }
    return Promise.reject();
  }

  // 부서 역할 이벤트 일괄 삭제
  async function onDeleteDepartRoleBatch() {
    try {
      await deleteDepartRole(selectedRowKeys, true);
      // 일괄 삭제 성공 후 선택 지우기
      clearSelection();
    } catch (e) { console.debug("작업 처리 중 예외 발생:", e); }
  }

  // Form 팝업 창 성공 후 콜백
  function onFormModalSuccess({ isUpdate, values }) {
    isUpdate ? updateTableDataRecord(values.id, values) : reload();
  }

  /**
   * 액션바
   */
  function getTableAction(record): ActionItem[] {
    return [{ label: '편집', onClick: editDepartRole.bind(null, record) }];
  }

  /**
   * 드롭다운 작업 표시줄
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      { label: '권한', onClick: permissionDepartRole.bind(null, record) },
      {
        label: '삭제',
        color: 'error',
        popConfirm: {
          title: '삭제하시겠습니까?',
          confirm: deleteDepartRole.bind(null, [record.id], false),
        },
      },
    ];
  }
</script>
