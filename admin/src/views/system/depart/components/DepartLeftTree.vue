<template>
  <a-card :bordered="false" style="height: 100%">
    <div class="j-table-operator" style="width: 100%">
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAddDepart">등록</a-button>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAddChildDepart()">하위 추가</a-button>
      <BasicUpload :maxSize="20" :maxNumber="1" :emptyHidePreview="true" :api="onImportXls" class="inline-block" />
      <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls">Export</a-button>
      <template v-if="checkedKeys.length > 0">
        <a-dropdown>
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onDeleteBatch">
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
    </div>
    <a-alert type="info" show-icon class="alert" style="margin-bottom: 8px">
      <template #message>
        <template v-if="checkedKeys.length > 0">
          <span>선택된 {{ checkedKeys.length }} 기록</span>
          <a-divider type="vertical" />
          <a @click="checkedKeys = []">초기화</a>
        </template>
        <template v-else>
          <span>선택한 데이터가 없습니다.</span>
        </template>
      </template>
    </a-alert>
    <a-spin :spinning="loading">
      <a-input-search placeholder="부서명으로 검색…" style="margin-bottom: 10px" @search="onSearch" />
      <!--조직도-->
      <template v-if="treeData.length > 0">
        <BasicTree
          v-if="!treeReloading"
          checkable
          :clickRowToExpand="false"
          :treeData="treeData"
          :selectedKeys="selectedKeys"
          :checkStrictly="checkStrictly"
          :load-data="loadChildrenTreeData"
          :checkedKeys="checkedKeys"
          v-model:expandedKeys="expandedKeys"
          @check="onCheck"
          @select="onSelect"
        >
          <template #title="{ key: treeKey, title, dataRef }">
            <a-dropdown :trigger="['contextmenu']">
              <Popconfirm
                :open="visibleTreeKey === treeKey"
                title="삭제하시겠습니까?"
                ok-text="확인"
                cancel-text="취소"
                placement="rightTop"
                @confirm="onDelete(dataRef)"
                @openChange="onVisibleChange"
              >
                <span>{{ title }}</span>
              </Popconfirm>

              <template #overlay>
                <a-menu @click="">
                  <a-menu-item key="1" @click="onAddChildDepart(dataRef)">자녀 추가</a-menu-item>
                  <a-menu-item key="2" @click="visibleTreeKey = treeKey">
                    <span style="color: red">삭제</span>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </template>
        </BasicTree>
      </template>
      <a-empty v-else description="아직 데이터가 없습니다" />
    </a-spin>
    <DepartFormModal :rootTreeData="treeData" @register="registerModal" @success="loadRootTreeData" />
  </a-card>
</template>

<script lang="ts" setup>
  import { inject, nextTick, ref, unref } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { BasicTree } from '/@/components/Tree';
  import { BasicUpload } from '/@/components/Upload';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useMethods } from '/@/hooks/system/useMethods';
  import { Api, deleteBatchDepart, queryDepartTreeSync } from '../depart.api';
  import { searchByKeywords } from '/@/views/system/departUser/depart.user.api';
  import DepartFormModal from '/@/views/system/depart/components/DepartFormModal.vue';
  import { Popconfirm } from 'ant-design-vue';

  const prefixCls = inject('prefixCls');
  const emit = defineEmits(['select', 'rootTreeData']);
  const { createMessage } = useMessage();
  const { handleImportXls, handleExportXls } = useMethods();

  const loading = ref<boolean>(false);
  // 부서 트리 목록 데이터
  const treeData = ref<any[]>([]);
  // 현재 선택된 항목
  const checkedKeys = ref<any[]>([]);
  // 현재 확장된 항목
  const expandedKeys = ref<any[]>([]);
  // 현재 선택된 항목
  const selectedKeys = ref<any[]>([]);
  // 트리 컴포넌트 다시 로드
  const treeReloading = ref<boolean>(false);
  // 아버지와 아들 나무는 서로 관련이 있나요?
  const checkStrictly = ref<boolean>(true);
  // 현재 선택학과
  const currentDepart = ref<any>(null);
  // 삭제 확인 프롬프트 상자 표시 여부 제어
  const visibleTreeKey = ref<any>(null);
  // 키워드 검색
  const searchKeyword = ref('');

  // 모달 등록
  const [registerModal, { openModal }] = useModal();

  // 최상위 부서 정보 로드
  async function loadRootTreeData() {
    try {
      loading.value = true;
      treeData.value = [];
      const result = await queryDepartTreeSync();
      if (Array.isArray(result)) {
        treeData.value = result;
      }
      if (expandedKeys.value.length === 0) {
        autoExpandParentNode();
      } else {
        if (selectedKeys.value.length === 0) {
          let item = treeData.value[0];
          if (item) {
            // 기본적으로 첫 번째 항목이 선택됩니다.
            setSelectedKey(item.id, item);
          }
        } else {
          emit('select', currentDepart.value);
        }
      }
      emit('rootTreeData', treeData.value);
    } finally {
      loading.value = false;
    }
  }

  loadRootTreeData();

  // 하위 부서 정보 불러오기
  async function loadChildrenTreeData(treeNode) {
    try {
      const result = await queryDepartTreeSync({
        pid: treeNode.dataRef.id,
      });
      if (result.length == 0) {
        treeNode.dataRef.isLeaf = true;
      } else {
        treeNode.dataRef.children = result;
        if (expandedKeys.value.length > 0) {
          // 획득한 하위 항목이 현재 확장된 항목을 가지고 있는지 확인
          let subKeys: any[] = [];
          for (let key of expandedKeys.value) {
            if (result.findIndex((item) => item.id === key) !== -1) {
              subKeys.push(key);
            }
          }
          if (subKeys.length > 0) {
            expandedKeys.value = [...expandedKeys.value];
          }
        }
      }
      treeData.value = [...treeData.value];
      emit('rootTreeData', treeData.value);
    } catch (e) {
      console.error(e);
    }
    return Promise.resolve();
  }

  // 상위 노드를 자동으로 확장하고 한 수준만 확장합니다.
  function autoExpandParentNode() {
    let item = treeData.value[0];
    if (item) {
      if (!item.isLeaf) {
        expandedKeys.value = [item.key];
      }
      // 기본적으로 첫 번째 항목이 선택됩니다.
      setSelectedKey(item.id, item);
      reloadTree();
    } else {
      emit('select', null);
    }
  }

  // 기본적으로 데이터를 확장할 수 없는 것을 방지하려면 트리 컴포넌트를 다시 로드하세요.
  async function reloadTree() {
    await nextTick();
    treeReloading.value = true;
    await nextTick();
    treeReloading.value = false;
  }

  /**
   * 현재 선택된 행 설정
   */
  function setSelectedKey(key: string, data?: object) {
    selectedKeys.value = [key];
    if (data) {
      currentDepart.value = data;
      emit('select', data);
    }
  }

  // 첫 번째 수준 부서 추가
  function onAddDepart() {
    openModal(true, { isUpdate: false, isChild: false });
  }

  // 하위 부서 추가
  function onAddChildDepart(data = currentDepart.value) {
    if (data == null) {
      createMessage.warning('먼저 부서를 선택해주세요');
      return;
    }
    const record = { parentId: data.id };
    openModal(true, { isUpdate: false, isChild: true, record });
  }

  // 이벤트 검색
  async function onSearch(value: string) {
    if (value) {
      try {
        loading.value = true;
        treeData.value = [];
        let result = await searchByKeywords({ keyWord: value });
        if (Array.isArray(result)) {
          treeData.value = result;
        }
        autoExpandParentNode();
      } finally {
        loading.value = false;
      }
    } else {
      loadRootTreeData();
    }
    searchKeyword.value = value;
  }

  // 트리 체크박스 선택 이벤트
  function onCheck(e) {
    if (Array.isArray(e)) {
      checkedKeys.value = e;
    } else {
      checkedKeys.value = e.checked;
    }
  }

  // 나무 선택 이벤트
  function onSelect(selKeys, event) {
    console.log('select: ', selKeys, event);
    if (selKeys.length > 0 && selectedKeys.value[0] !== selKeys[0]) {
      setSelectedKey(selKeys[0], event.selectedNodes[0]);
    } else {
      // 이렇게 하면 사용자가 선택을 취소할 수 없습니다.
      setSelectedKey(selectedKeys.value[0]);
    }
  }

  /**
   * ID를 기준으로 부서 삭제
   * @param idListRef array
   * @param 확인 확인 프롬프트 상자 표시 여부
   */
  async function doDeleteDepart(idListRef, confirm = true) {
    const idList = unref(idListRef);
    if (idList.length > 0) {
      try {
        loading.value = true;
        await deleteBatchDepart({ ids: idList.join(',') }, confirm);
        await loadRootTreeData();
      } finally {
        loading.value = false;
      }
    }
  }

  // 단일 부서 삭제
  async function onDelete(data) {
    if (data) {
      onVisibleChange(false);
      doDeleteDepart([data.id], false);
    }
  }

  // 부서를 일괄 삭제
  async function onDeleteBatch() {
    try {
      await doDeleteDepart(checkedKeys);
      checkedKeys.value = [];
    } finally {
    }
  }

  function onVisibleChange(visible) {
    if (!visible) {
      visibleTreeKey.value = null;
    }
  }

  function onImportXls(d) {
    handleImportXls(d, Api.importExcelUrl, () => {
      loadRootTreeData();
    });
  }

  function onExportXls() {
    handleExportXls('학과정보', Api.exportXlsUrl);
  }

  defineExpose({
    loadRootTreeData,
  });
</script>
