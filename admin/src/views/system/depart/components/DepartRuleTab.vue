<template>
  <a-spin :spinning="loading">
    <template v-if="treeData.length > 0">
      <BasicTree
        ref="basicTree"
        class="depart-rule-tree"
        checkable
        :treeData="treeData"
        :checkedKeys="checkedKeys"
        :selectedKeys="selectedKeys"
        :expandedKeys="expandedKeys"
        :checkStrictly="checkStrictly"
        style="height: 500px; overflow: auto"
        @check="onCheck"
        @expand="onExpand"
        @select="onSelect"
      >
        <template #title="{ slotTitle, ruleFlag }">
          <span>{{ slotTitle }}</span>
          <Icon v-if="ruleFlag" icon="ant-design:align-left-outlined" style="margin-left: 5px; color: red" />
        </template>
      </BasicTree>
    </template>
    <a-empty v-else description="부서 권한을 구성할 수 없습니다." />

    <div class="j-box-bottom-button offset-20" style="margin-top: 30px">
      <div class="j-box-bottom-button-float" :class="[`${prefixCls}`]">
        <a-dropdown :trigger="['click']" placement="top">
          <template #overlay>
            <a-menu>
              <a-menu-item key="3" @click="toggleCheckALL(true)">모두 확인</a-menu-item>
              <a-menu-item key="4" @click="toggleCheckALL(false)">모두 선택 취소</a-menu-item>
              <a-menu-item key="5" @click="toggleExpandAll(true)">모두 펼치기</a-menu-item>
              <a-menu-item key="6" @click="toggleExpandAll(false)">모두 숨기기</a-menu-item>
            </a-menu>
          </template>
          <a-button style="float: left">
            트리 작업
            <Icon icon="ant-design:up-outlined" />
          </a-button>
        </a-dropdown>
        <a-button type="primary" preIcon="ant-design:save-filled" @click="onSubmit">저장</a-button>
      </div>
    </div>
  </a-spin>
  <DepartDataRuleDrawer @register="registerDataRuleDrawer" />
</template>

<script lang="ts" setup>
  import { watch, computed, inject, ref, nextTick } from 'vue';
  import { useDrawer } from '/@/components/Drawer';
  import { BasicTree } from '/@/components/Tree/index';
  import DepartDataRuleDrawer from './DepartDataRuleDrawer.vue';
  import { queryRoleTreeList, queryDepartPermission, saveDepartPermission } from '../depart.api';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { translateTitle } from '/@/utils/common/compUtils';

  const { prefixCls } = useDesign('j-depart-form-content');
  const props = defineProps({
    data: { type: Object, default: () => ({}) },
  });
  // 현재 선택된 부서 ID가 비어 있을 수 있습니다. 이는 선택된 부서가 없음을 나타냅니다.
  const departId = computed(() => props.data?.id);

  const basicTree = ref();
  const loading = ref<boolean>(false);
  const treeData = ref<any[]>([]);
  const expandedKeys = ref<Array<any>>([]);
  const selectedKeys = ref<Array<any>>([]);
  const checkedKeys = ref<Array<any>>([]);
  const lastCheckedKeys = ref<Array<any>>([]);
  const checkStrictly = ref(true);

  // 등록 데이터 규칙 인증 Drawer
  const [registerDataRuleDrawer, dataRuleDrawer] = useDrawer();

  // onCreated
  loadData();
  watch(departId, () => loadDepartPermission(), { immediate: true });

  async function loadData() {
    try {
      loading.value = true;
      let { treeList } = await queryRoleTreeList();
      treeData.value = translateTitle(treeList) || [];
      await nextTick();
      toggleExpandAll(true);
    } finally {
      loading.value = false;
    }
  }

  async function loadDepartPermission() {
    if (departId.value) {
      try {
        loading.value = true;
        let keys = await queryDepartPermission({ departId: departId.value });
        checkedKeys.value = keys;
        lastCheckedKeys.value = [...keys];
      } finally {
        loading.value = false;
      }
    }
  }

  async function onSubmit() {
    try {
      loading.value = true;
      await saveDepartPermission({
        departId: departId.value,
        permissionIds: checkedKeys.value.join(','),
        lastpermissionIds: lastCheckedKeys.value.join(','),
      });
      await loadData();
      await loadDepartPermission();
    } finally {
      loading.value = false;
    }
  }

  // 트리 체크박스 이벤트
  function onCheck(event) {
    if (!Array.isArray(event)) {
      checkedKeys.value = event.checked;
    } else {
      checkedKeys.value = event;
    }
  }

  // 트리 확장 이벤트
  function onExpand($expandedKeys) {
    expandedKeys.value = $expandedKeys;
  }

  // 트리 선택 이벤트
  function onSelect($selectedKeys, { selectedNodes }) {
    if (selectedNodes[0]?.ruleFlag) {
      let functionId = $selectedKeys[0];
      dataRuleDrawer.openDrawer(true, { departId, functionId });
    }
    selectedKeys.value = [];
  }

  // 상위-하위 연결 전환
  async function toggleCheckStrictly(flag) {
    checkStrictly.value = flag;
    await nextTick();
    checkedKeys.value = basicTree.value.getCheckedKeys();
  }

  // 펼치기 접기 전환
  async function toggleExpandAll(flag) {
    basicTree.value.expandAll(flag);
    await nextTick();
    expandedKeys.value = basicTree.value.getExpandedKeys();
  }

  // 모두 선택 전환
  async function toggleCheckALL(flag) {
    basicTree.value.checkAll(flag);
    await nextTick();
    checkedKeys.value = basicTree.value.getCheckedKeys();
  }
</script>

<style lang="less" scoped>
  // 【VUEN-188】민감하지 않은 스크롤 막대 문제 해결
  .depart-rule-tree :deep(.scrollbar__bar) {
    pointer-events: none;
  }
</style>
