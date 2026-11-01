<template>
  <div>
    <!--참고표-->
    <BasicTable
      @register="registerTable"
      :rowSelection="rowSelection"
      :expandedRowKeys="expandedRowKeys"
      @expand="handleExpand"
      @fetch-success="onFetchSuccess"
    >
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
    <CategoryModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="system-category" setup>
  //TS 구문
  import { ref, computed, unref, toRaw, nextTick } from 'vue';
  import { BasicTable, useTable, TableAction } from '/src/components/Table';
  import { useDrawer } from '/src/components/Drawer';
  import CategoryModal from './components/CategoryModal.vue';
  import { useModal } from '/src/components/Modal';
  import { useMethods } from '/src/hooks/system/useMethods';
  import { columns, searchFormSchema } from './category.data';
  import { list, deleteCategory, batchDeleteCategory, getExportUrl, getImportUrl, getChildList, getChildListBatch } from './category.api';
  import { useListPage } from '/@/hooks/system/useListPage';

  const expandedRowKeys = ref([]);
  const { handleExportXls, handleImportXls } = useMethods();
  //사전 모델
  const [registerModal, { openModal }] = useModal();
  // 목록 페이지 공개 매개변수 및 메소드
  const { prefixCls, onExportXls, onImportXls, tableContext } = useListPage({
    designScope: 'category-template',
    tableProps: {
      title: '분류사전',
      api: list,
      columns: columns,
      actionColumn: {
        width: 180,
      },
      formConfig: {
        schemas: searchFormSchema,
      },
      isTreeTable: true,
    },
    exportConfig: {
      name: '분류 목록',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  //테이블 데이터 등록
  const [registerTable, { reload, collapseAll, updateTableDataRecord, findTableDataRecord, getDataSource }, { rowSelection, selectedRowKeys }] =
    tableContext;

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
  async function handleEdit(record) {
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
      hideFooter: true,
    });
  }

  /**
   * 이벤트 삭제
   */
  async function handleDelete(record) {
    await deleteCategory({ id: record.id }, importSuccess);
  }

  /**
   * 일괄 삭제 이벤트
   */
  async function batchHandleDelete() {
    const ids = selectedRowKeys.value.filter((item) => !item.includes('loading'));
    await batchDeleteCategory({ ids: ids }, importSuccess);
  }
  /**
   * 수입
   */
  function importSuccess() {
    (selectedRowKeys.value = []) && reload();
  }
  /**
   * 하위 추가
   */
  function handleAddSub(record) {
    openModal(true, {
      record,
      isUpdate: false,
    });
  }
  /**
   * 성공적인 콜백
   */
  async function handleSuccess({ isUpdate,isSubAdd, values, expandedArr }) {
    if (isUpdate) {
      //콜백 수정
      updateTableDataRecord(values.id, values);
    } else {
      if (!values['pid']) {
        //루트 노드 추가
        reload();
      } else {
        //새 하위 집합 추가
        if(isSubAdd){
          await expandTreeNode(values.pid);
        }else{
          //expandedRowKeys.value = [];
          for (let key of unref(expandedArr)) {
            await expandTreeNode(key);
          }
        }
      }
    }
  }

  /**
   * 성공적인 인터페이스 요청 후 콜백
   */
  function onFetchSuccess(result) {
    getDataByResult(result.items) && loadDataByExpandedRows();
  }
  /**
   * 확장된 행을 기반으로 데이터 쿼리（저장 후 새로 고칠 때 하위 데이터를 비동기적으로 로드하는 데 사용됩니다.）
   */
  async function loadDataByExpandedRows() {
    if (unref(expandedRowKeys).length > 0) {
      const res = await getChildListBatch({ parentIds: unref(expandedRowKeys).join(',') });
      if (res.success && res.result.records.length > 0) {
        //확장된 데이터 일괄 하위 노드
        let records = res.result.records;
        const listMap = new Map();
        for (let item of records) {
          let pid = item['pid'];
          if (unref(expandedRowKeys).includes(pid)) {
            let mapList = listMap.get(pid);
            if (mapList == null) {
              mapList = [];
            }
            mapList.push(item);
            listMap.set(pid, mapList);
          }
        }
        let childrenMap = listMap;
        let fn = (list) => {
          if (list) {
            list.forEach((data) => {
              if (unref(expandedRowKeys).includes(data.id)) {
                data.children = getDataByResult(childrenMap.get(data.id));
                fn(data.children);
              }
            });
          }
        };
        fn(getDataSource());
      }
    }
  }
  /**
   * 데이터 세트 처리
   */
  function getDataByResult(result) {
    if (result && result.length > 0) {
      return result.map((item) => {
        //하위 노드가 있는 노드가 표시되어 있는지 확인
        if (item['hasChild'] == '1') {
          let loadChild = { id: item.id + '_loadChild', name: 'loading...', isLoading: true };
          item.children = [loadChild];
        }
        return item;
      });
    }
  }
  /**
   *트리 노드 확장 및 병합
   * */
  async function handleExpand(expanded, record) {
    // 펼쳐진 상태인지, 펼쳐진 상태인지 판단(expanded)그리고 하위 집합이 있습니다(children)로드되지 않았습니다.(isLoading)그런 다음 하위 노드 데이터를 쿼리합니다.
    if (expanded) {
      expandedRowKeys.value.push(record.id);
      if (record.children.length > 0 && !!record.children[0].isLoading) {
        let result = await getChildList({ pid: record.id });
        if (result && result.length > 0) {
          record.children = getDataByResult(result);
        } else {
          record.children = null;
          record.hasChild = '0';
        }
      }
    } else {
      let keyIndex = expandedRowKeys.value.indexOf(record.id);
      if (keyIndex >= 0) {
        expandedRowKeys.value.splice(keyIndex, 1);
      }
    }
  }
  /**
   *테이블 조작 후 트리 노드를 처리하여 확장 및 병합
   * */
  async function expandTreeNode(key) {
    let record:any = findTableDataRecord(key);
    if(!expandedRowKeys.value.includes(key)){
      expandedRowKeys.value.push(key);
    }
    let result = await getChildList({ pid: key });
    if (result && result.length > 0) {
      record.children = getDataByResult(result);
    } else {
      record.children = null;
      record.hasChild = '0';
    }
    updateTableDataRecord(key, record);
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
        label: '삭제',
        popConfirm: {
          title: '삭제하시겠습니까?',
          confirm: handleDelete.bind(null, record),
        },
      },
      {
        label: '하위 추가',
        onClick: handleAddSub.bind(null, { pid: record.id }),
      },
    ];
  }
</script>

<style scoped></style>
