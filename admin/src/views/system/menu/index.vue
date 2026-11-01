<!--
  @component SystemMenuManagement
  @description 시스템 메뉴 계층 트리 및 기능 권한 관리 화면
  @features
    - 디렉토리(1단계), 메뉴(2단계), 버튼 액션(3단계) 3차원 계층 구조 트리 관리
    - Vue 라우트 컴포넌트 경로, 리다이렉트, 아이콘, 캐싱 유지 설정
    - 데이터 권한 규칙(Data Rule) 연동을 통한 부서/개인별 행 단위 데이터 격리
-->
<template>
  <div class="p-4">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> 메뉴 추가</a-button>
        <a-button type="primary" preIcon="ic:round-expand" @click="expandAll">모두 펼치기</a-button>
        <a-button type="primary" preIcon="ic:round-compress" @click="collapseAll">모두 접기</a-button>

        <a-dropdown v-if="checkedKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined" />
                삭제
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >일괄 작업
            <Icon icon="ant-design:down-outlined" />
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <MenuDrawer @register="registerDrawer" @success="handleSuccess" :showFooter="showFooter" />
    <DataRuleList @register="registerDrawer1" />
  </div>
</template>
<script lang="ts" name="system-menu" setup>
  import { nextTick, ref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useDrawer } from '/@/components/Drawer';
  import MenuDrawer from './MenuDrawer.vue';
  import DataRuleList from './DataRuleList.vue';
  import { columns,searchFormSchema } from './menu.data';
  import { list, deleteMenu, batchDeleteMenu } from './menu.api';
  import { useDefIndexStore } from "@/store/modules/defIndex";
  import { useI18n } from "/@/hooks/web/useI18n";

  const checkedKeys = ref<Array<string | number>>([]);
  const showFooter = ref(true);
  const [registerDrawer, { openDrawer }] = useDrawer();
  const [registerDrawer1, { openDrawer: openDataRule }] = useDrawer();
  const { t } = useI18n();

  // 사용자 정의 메뉴 이름 열 렌더링
  columns[0].customRender = function ({text, record}) {
    const isDefIndex = checkDefIndex(record)
    if (isDefIndex) {
      text += '（기본 홈페이지）'
    }
    if (text.includes("t('") && t) {
      return new Function('t', `return ${text}`)(t);
    }
    return text
  }

  // 목록 페이지 공개 매개변수 및 메소드
  const { prefixCls, tableContext } = useListPage({
    tableProps: {
      title: '메뉴 목록',
      api: list,
      columns: columns,
      size: 'small',
      pagination: false,
      isTreeTable: true,
      striped: true,
      useSearchForm: true,
      showTableSetting: true,
      bordered: true,
      showIndexColumn: false,
      tableSetting: { fullScreen: true },
      formConfig: {
        labelWidth:60,
        owProps: { gutter: 24 },
        schemas: searchFormSchema,
        autoAdvancedCol: 4,
        baseColProps: { xs: 24, sm: 12, md: 6, lg: 6, xl: 6, xxl: 6 },
        actionColOptions: { xs: 24, sm: 12, md: 6, lg: 6, xl: 6, xxl: 6 },
      },
      actionColumn: {
        width: 120,
      },
    },
  });
  //테이블 데이터 등록
  const [registerTable, { reload, expandAll, collapseAll }] = tableContext;

  /**
   * 컬럼 구성 선택
   */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 30,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };

  /**
   * 이벤트 선택
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }

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
  function handleEdit(record) {
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
    openDrawer(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * 하위 추가
   */
  function handleAddSub(record) {
    openDrawer(true, {
      record: { parentId: record.id, menuType: 1 },
      isUpdate: false,
    });
  }
  /**
   * 데이터 권한 팝업창
   */
  function handleDataRule(record) {
    openDataRule(true, { id: record.id });
  }

  /**
   * 삭제
   */
  async function handleDelete(record) {
    await deleteMenu({ id: record.id }, reload);
  }
  /**
   * 일괄 삭제 이벤트
   */
  async function batchHandleDelete() {
    await batchDeleteMenu({ ids: checkedKeys.value }, reload);
  }
  /**
   * 성공적인 콜백
   */
  function handleSuccess() {
    reload();
    reloadDefIndex();
  }

  function onFetchSuccess() {
    // 데모에서는 기본적으로 모든 테이블 항목을 확장합니다.
    nextTick(expandAll);
  }

  // --------------- 기본 홈 페이지 구성 시작 ------------

  const defIndexStore = useDefIndexStore()

  // 기본 홈페이지 설정
  async function handleSetDefIndex(record: Recordable) {
    defIndexStore.update(record.url, record.component, record.route)
  }

  /**
   * 기본 홈페이지인지 확인하세요
   * @param record
   */
  function checkDefIndex(record: Recordable) {
    return defIndexStore.check(record.url)
  }

  // 기본 홈 페이지 구성 다시 로드
  function reloadDefIndex() {
    try {
      defIndexStore.query();
    } catch (e) {
      console.error(e)
    }
  }

  reloadDefIndex()

  // --------------- end 기본 홈 페이지 구성 종료 ------------

  /**
   * 액션바
   */
  function getTableAction(record) {
    return [
      {
        label: '편집',
        onClick: handleEdit.bind(null, record),
      },
    ];
  }

  /**
   * 드롭다운 작업 표시줄
   */
  function getDropDownAction(record) {
    return [
      // {
      //   label: '상세',
      //   onClick: handleDetail.bind(null, record),
      // },
      {
        label: '하위 추가',
        onClick: handleAddSub.bind(null, record),
      },
      // {
      //   label: '기본 홈페이지로 설정',
      //   onClick: handleSetDefIndex.bind(null, record),
      //   ifShow: () => !record.internalOrExternal && record.component && !checkDefIndex(record),
      // },
      {
        label: '삭제',
        color: 'error',
        popConfirm: {
          title: '삭제 하시겠습니까?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
