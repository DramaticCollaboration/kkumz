<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" width="650px" destroyOnClose showFooter>
    <template #title>
      역할 권한 구성
      <a-dropdown>
        <Icon icon="ant-design:more-outlined" class="more-icon" />
        <template #overlay>
          <a-menu @click="treeMenuClick">
            <a-menu-item key="checkAll">모두 선택</a-menu-item>
            <a-menu-item key="cancelCheck">선택 취소</a-menu-item>
            <div class="line"></div>
            <a-menu-item key="openAll">모두 펼치기</a-menu-item>
            <a-menu-item key="closeAll">모두 접기</a-menu-item>
            <div class="line"></div>
            <a-menu-item key="relation">계층적 연관</a-menu-item>
            <a-menu-item key="standAlone">계층 독립적</a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </template>
    <BasicTree
      ref="treeRef"
      checkable
      :treeData="treeData"
      :checkedKeys="checkedKeys"
      :expandedKeys="expandedKeys"
      :selectedKeys="selectedKeys"
      :clickRowToExpand="false"
      :checkStrictly="true"
      @check="onCheck"
      @select="onTreeNodeSelect"
    >
      <template #title="{ slotTitle, ruleFlag }">
        {{ slotTitle }}
        <Icon v-if="ruleFlag" icon="ant-design:align-left-outlined" style="margin-left: 5px; color: red"></Icon>
      </template>
    </BasicTree>
    <!--오른쪽 하단 버튼-->
    <template #footer>
      <!-- <PopConfirmButton title="편집을 중단하시겠습니까?" @confirm="closeDrawer" okText="확인" cancelText="취소"></PopConfirmButton> -->
      <a-button @click="closeDrawer">취소</a-button>
      <a-button @click="handleSubmit(false)" type="primary" :loading="loading" ghost style="margin-right: 0.8rem"> 저장</a-button>
      <a-button @click="handleSubmit(true)" type="primary" :loading="loading">저장하고 닫기</a-button>
    </template>
    <RoleDataRuleDrawer @register="registerDrawer1" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, computed, unref, onMounted } from 'vue';
  import { BasicDrawer, useDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicTree, TreeItem } from '/@/components/Tree';
  import { PopConfirmButton } from '/@/components/Button';
  import RoleDataRuleDrawer from './RoleDataRuleDrawer.vue';
  import { queryTreeListForRole, queryRolePermission, saveRolePermission } from '../role.api';
  import { useI18n } from "/@/hooks/web/useI18n";
  import { ROLE_AUTH_CONFIG_KEY } from '/@/enums/cacheEnum';
  const emit = defineEmits(['register']);
  //나무 정보
  const treeData = ref<TreeItem[]>([]);
  //트리의 모든 노드 정보
  const allTreeKeys = ref([]);
  //트리 선택 노드 정보
  const checkedKeys = ref<any>([]);
  const defaultCheckedKeys = ref([]);
  //트리의 선택된 노드 정보
  const selectedKeys = ref([]);
  const roleId = ref('');
  //트리 인스턴스
  const treeRef = ref(null);
  const loading = ref(false);

  //접힌 키 펼치기
  const expandedKeys = ref<any>([]);
  //상위 노드와 하위 노드의 선택된 상태가 관련되어 있는지 여부를 True는 관련이 없고, False는 관련이 있습니다.
  const checkStrictly = ref<boolean>(false);
  const [registerDrawer1, { openDrawer: openDataRuleDrawer }] = useDrawer();
  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await reset();
    setDrawerProps({ confirmLoading: false, loading: true });
    roleId.value = data.roleId;
    //초기화 데이터
    const roleResult = await queryTreeListForRole();
    treeData.value = translateTitle(roleResult.treeList) || [];
    allTreeKeys.value = roleResult.ids;
    const localData = localStorage.getItem(ROLE_AUTH_CONFIG_KEY);
    if (localData) {
      const obj = JSON.parse(localData);
      obj.level && treeMenuClick({ key: obj.level });
      obj.expand && treeMenuClick({ key: obj.expand });
    } else {
      expandedKeys.value = roleResult.ids;
    }
    //역할 메뉴 데이터 초기화
    const permResult = await queryRolePermission({ roleId: unref(roleId) });
    checkedKeys.value = permResult;
    defaultCheckedKeys.value = permResult;
    setDrawerProps({ loading: false });
  });
  /**
  * 2024-02-28
  * liaozhiyang
  * 메뉴 이름 번역
   */
  function translateTitle(data) {
    if (data?.length) {
      data.forEach((item) => {
        if (item.slotTitle) {
          const { t } = useI18n();
          if (item.slotTitle.includes("t('") && t) {
            item.slotTitle = new Function('t', `return ${item.slotTitle}`)(t);
          }
        }
        if (item.children?.length) {
          translateTitle(item.children);
        }
      });
    }
    return data;
  }
  /**
   * 클릭해서 선택하세요
   * 2024-04-26
   * liaozhiyang
   */
  function onCheck(o, e) {
    // checkStrictly: true=>레벨 독립적, false=>계층적 연관.
    if (checkStrictly.value) {
      checkedKeys.value = o.checked ? o.checked : o;
    } else {
      const keys = getNodeAllKey(e.node, 'children', 'key');
      if (e.checked) {
        // 반복 작업 중에 중복 키가 있을 수 있습니다. 이를 복제하려면 새 설정을 사용해야 합니다.
        checkedKeys.value = [...new Set([...checkedKeys.value, ...keys])];
      } else {
        const result = removeMatchingItems(checkedKeys.value, keys);
        checkedKeys.value = result;
      }
    }
  }
  /**
   * 2024-04-26
   * liaozhiyang
   * 일치하는 배열 항목 삭제
   */
  function removeMatchingItems(arr1, arr2) {
    // 해시 테이블을 사용하여 arr2의 요소를 기록합니다.
    const hashTable = {};
    for (const item of arr2) {
      hashTable[item] = true;
    }
    // 필터 메소드를 사용하여 첫 번째 배열을 반복하고 해시 테이블에 존재하지 않는 항목을 필터링합니다.
    return arr1.filter((item) => !hashTable[item]);
  }
  /**
   * 2024-04-26
   * liaozhiyang
   * 현재 노드와 아래의 모든 자손의 키를 가져옵니다.
   */
  function getNodeAllKey(node: any, children: any, key: string) {
    const result: any = [];
    result.push(node[key]);
    const recursion = (data) => {
      data.forEach((item: any) => {
        result.push(item[key]);
        if (item[children]?.length) {
          recursion(item[children]);
        }
      });
    };
    node[children]?.length && recursion(node[children]);
    return result;
  }

  /**
   * 노드를 선택하고 데이터 권한 서랍을 엽니다.
   */
  function onTreeNodeSelect(key) {
    if (key && key.length > 0) {
      selectedKeys.value = key;
    }
    openDataRuleDrawer(true, { functionId: unref(selectedKeys)[0], roleId: unref(roleId) });
  }
  /**
   * 데이터 재설정
   */
  function reset() {
    treeData.value = [];
    allTreeKeys.value = [];
    checkedKeys.value = [];
    defaultCheckedKeys.value = [];
    selectedKeys.value = [];
    roleId.value = '';
  }
  /**
   * 트리 인스턴스 가져오기
   */
  function getTree() {
    const tree = unref(treeRef);
    if (!tree) {
      throw new Error('tree is null!');
    }
    return tree;
  }
  /**
   * 제출하다
   */
  async function handleSubmit(exit) {
    let params = {
      roleId: unref(roleId),
      permissionIds: unref(getTree().getCheckedKeys()).join(','),
      lastpermissionIds: unref(defaultCheckedKeys).join(','),
    };
    if(loading.value===false){
      await doSave(params)
    }else{
      console.log('마지막 실행이 완료될 때까지 기다려주세요!');
    }
    if(exit){
      // 닫혀 있으면
      closeDrawer();
    }else{
      // 선택한 데이터를 닫았다가 다시 가져올 필요가 없습니다.
      const permResult = await queryRolePermission({ roleId: unref(roleId) });
      defaultCheckedKeys.value = permResult;
    }
  }

  // VUE 역할 인증이 반복적으로 저장됩니다 #352
  async function doSave(params) {
    loading.value = true;
    await saveRolePermission(params);
    setTimeout(()=>{
      loading.value = false;
    }, 500)
  }

  /**
   * 트리 메뉴 선택
   * @param key
   */
  function treeMenuClick({ key }) {
    if (key === 'checkAll') {
      checkedKeys.value = allTreeKeys.value;
    } else if (key === 'cancelCheck') {
      checkedKeys.value = [];
    } else if (key === 'openAll') {
      expandedKeys.value = allTreeKeys.value;
      saveLocalOperation('expand', 'openAll');
    } else if (key === 'closeAll') {
      expandedKeys.value = [];
      saveLocalOperation('expand', 'closeAll');
    } else if (key === 'relation') {
      checkStrictly.value = false;
      saveLocalOperation('level', 'relation');
    } else {
      checkStrictly.value = true;
      saveLocalOperation('level', 'standAlone');
    }
  }
  /**
   * 2024-05-31
   * liaozhiyang
   * 【TV360X-590】역할 승인 팝업창 동작 캐시
   * */
  const saveLocalOperation = (key, value) => {
    const localData = localStorage.getItem(ROLE_AUTH_CONFIG_KEY);
    const obj = localData ? JSON.parse(localData) : {};
    obj[key] = value;
    localStorage.setItem(ROLE_AUTH_CONFIG_KEY, JSON.stringify(obj))
  };
</script>

<style lang="less" scoped>
  /** 고정 동작 버튼 */
  .sync-basic-tree {
    position: absolute;
    width: 618px;
  }
  .line {
    height: 1px;
    width: 100%;
    border-bottom: 1px solid #f0f0f0;
  }
  .more-icon {
    font-size: 20px !important;
    color: black;
    display: inline-flex;
    float: right;
    margin-right: 2px;
    cursor: pointer;
  }
  :deep(.sync-tree-header) {
    border-bottom: none;
  }
</style>
