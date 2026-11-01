<template>
  <BasicDrawer
    title="부서 역할 권한 구성"
    :width="650"
    :loading="loading"
    showFooter
    okText="저장하고 닫기"
    @ok="onSubmit(true)"
    @close="onClose"
    @register="registerDrawer"
  >
    <div>
      <a-spin :spinning="loading">
        <template v-if="treeData.length > 0">
          <BasicTree
            title="소유한 부서 권한"
            toolbar
            checkable
            :treeData="treeData"
            :checkedKeys="checkedKeys"
            :selectedKeys="selectedKeys"
            :expandedKeys="expandedKeys"
            :checkStrictly="checkStrictly"
            :clickRowToExpand="false"
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
      </a-spin>
    </div>

    <template #centerFooter>
      <a-button type="primary" :loading="loading" ghost @click="onSubmit(false)"> 저장</a-button>
    </template>
  </BasicDrawer>
  <DepartRoleDataRuleDrawer @register="registerDataRuleDrawer" />
</template>

<script lang="ts" setup>
  import { ref } from 'vue';

  import { BasicTree } from '/@/components/Tree/index';
  import { BasicDrawer, useDrawer, useDrawerInner } from '/@/components/Drawer';
  import { useMessage } from '/@/hooks/web/useMessage';

  import DepartRoleDataRuleDrawer from './DepartRoleDataRuleDrawer.vue';
  import { queryTreeListForDeptRole, queryDeptRolePermission, saveDeptRolePermission } from '../depart.user.api';
  import { translateTitle } from "@/utils/common/compUtils";

  defineEmits(['register']);
  const { createMessage } = useMessage();
  const loading = ref(false);
  const departId = ref('');
  const roleId = ref('');
  const treeData = ref<Array<any>>([]);
  const checkedKeys = ref<Array<any>>([]);
  const lastCheckedKeys = ref<Array<any>>([]);
  const expandedKeys = ref<Array<any>>([]);
  const selectedKeys = ref<Array<any>>([]);
  const allTreeKeys = ref<Array<any>>([]);
  const checkStrictly = ref(true);

  // 서랍 컴포넌트 등록
  const [registerDrawer, { closeDrawer }] = useDrawerInner((data) => {
    roleId.value = data.record.id;
    departId.value = data.record.departId;
    loadData();
  });
  // 등록 데이터 규칙 인증 Drawer
  const [registerDataRuleDrawer, dataRuleDrawer] = useDrawer();

  async function loadData() {
    try {
      loading.value = true;
      // 사용자 역할 권한 부여 기능, 쿼리 메뉴 권한 트리
      const { ids, treeList } = await queryTreeListForDeptRole({ departId: departId.value });
      if (ids.length > 0) {
        allTreeKeys.value = ids;
        expandedKeys.value = ids;
        treeData.value = translateTitle(treeList);
        // 쿼리 역할 승인
        checkedKeys.value = await queryDeptRolePermission({ roleId: roleId.value });
        lastCheckedKeys.value = [checkedKeys.value];
      } else {
        reset();
      }
    } finally {
      loading.value = false;
    }
  }

  // 페이지 재설정
  function reset() {
    treeData.value = [];
    expandedKeys.value = [];
    checkedKeys.value = [];
    lastCheckedKeys.value = [];
    loading.value = false;
  }

  // 트리 체크박스 이벤트
  function onCheck(event) {
    if (checkStrictly.value) {
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
      dataRuleDrawer.openDrawer(true, { roleId, departId, functionId });
    }
    selectedKeys.value = [];
  }

  function doClose() {
    reset();
    closeDrawer();
  }

  function onClose() {
    reset();
  }

  async function onSubmit(exit) {
    try {
      loading.value = true;
      let params = {
        roleId: roleId.value,
        permissionIds: checkedKeys.value.join(','),
        lastpermissionIds: lastCheckedKeys.value.join(','),
      };
      await saveDeptRolePermission(params);
      if (exit) {
        doClose();
      }
    } finally {
      loading.value = false;
      if (!exit) {
        loadData();
      }
    }
  }
</script>
