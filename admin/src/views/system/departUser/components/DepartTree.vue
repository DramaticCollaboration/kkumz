<template>
  <div class="bg-white m-4 mr-0 overflow-hidden">
    <a-spin :spinning="loading">
      <template v-if="userIdentity === '2'">
        <!--조직도-->
        <BasicTree
          v-if="!treeReloading"
          title="부서 목록"
          toolbar
          search
          showLine
          :checkStrictly="true"
          :clickRowToExpand="false"
          :treeData="treeData"
          :selectedKeys="selectedKeys"
          :expandedKeys="expandedKeys"
          :autoExpandParent="autoExpandParent"
          @select="onSelect"
          @expand="onExpand"
          @search="onSearch"
        />
      </template>
      <a-empty v-else description="일반 직원에게는 이러한 권한이 없습니다." />
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
  import { inject, nextTick, ref } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTree } from '/@/components/Tree';
  import { queryMyDepartTreeList, searchByKeywords } from '../depart.user.api';

  const prefixCls = inject('prefixCls');
  const emit = defineEmits(['select']);
  const { createMessage } = useMessage();

  let loading = ref<boolean>(false);
  // 부서 트리 목록 데이터
  let treeData = ref<any[]>([]);
  // 현재 확장된 항목
  let expandedKeys = ref<any[]>([]);
  // 현재 선택된 항목
  let selectedKeys = ref<any[]>([]);
  // 상위 항목을 자동으로 확장할지 여부
  let autoExpandParent = ref<boolean>(true);
  // 사용자 ID
  let userIdentity = ref<string>('2');
  // 트리 컴포넌트 다시 로드
  let treeReloading = ref<boolean>(false);

  // 부서정보 불러오기
  function loadDepartTreeData() {
    loading.value = true;
    treeData.value = [];
    queryMyDepartTreeList()
      .then((res) => {
        if (res.success) {
          if (Array.isArray(res.result)) {
            treeData.value = res.result;
            userIdentity.value = res.message;
            autoExpandParentNode();
          }
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => (loading.value = false));
  }

  loadDepartTreeData();

  // 상위 노드를 자동으로 확장하고 한 수준만 확장합니다.
  function autoExpandParentNode() {
    let keys: Array<any> = [];
    treeData.value.forEach((item, index) => {
      if (item.children && item.children.length > 0) {
        keys.push(item.key);
      }
      if (index === 0) {
        // 기본적으로 첫 번째 항목이 선택됩니다.
        setSelectedKey(item.id, item);
      }
    });
    if (keys.length > 0) {
      reloadTree();
      expandedKeys.value = keys;
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
      emit('select', data);
    }
  }

  // 이벤트 검색
  function onSearch(value: string) {
    if (value) {
      loading.value = true;
      searchByKeywords({ keyWord: value, myDeptSearch: '1' })
        .then((result) => {
          if (Array.isArray(result)) {
            treeData.value = result;
          } else {
            createMessage.warning('부서 정보를 찾을 수 없습니다');
            treeData.value = [];
          }
        })
        .finally(() => (loading.value = false));
    } else {
      loadDepartTreeData();
    }
  }

  // 나무 선택 이벤트
  function onSelect(selKeys, event) {
    if (selKeys.length > 0 && selectedKeys.value[0] !== selKeys[0]) {
      setSelectedKey(selKeys[0], event.selectedNodes[0]);
    } else {
      // 이렇게 하면 사용자가 선택을 취소할 수 없습니다.
      setSelectedKey(selectedKeys.value[0]);
    }
  }

  // 트리 확장 이벤트
  function onExpand(keys) {
    expandedKeys.value = keys;
    autoExpandParent.value = false;
  }
</script>
<style lang="less" scoped>
  /*antd3을 업그레이드한 후 쿼리 상자가 트리에 너무 가까워지고 스타일이 최적화되었습니다.*/
  :deep(.sync-tree-header) {
    margin-bottom: 6px;
  }
</style>
