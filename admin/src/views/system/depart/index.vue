<!--
  @component SystemDepartManagement
  @description 전사 기업 조직도 및 부서 계층 트리 관리 화면
  @features
    - 본부 - 실 - 팀 다단계 조직 트리 시각화 및 드래그 앤 드롭 정렬
    - 부서 코드, 부서장 계정, 부서별 데이터 스코프(Data Scope) 설정
    - 부서 소속 임직원 명단 조회
-->
<template>
  <a-row :class="['p-4', `${prefixCls}--box`]" type="flex" :gutter="10">
    <a-col :xl="12" :lg="24" :md="24" style="margin-bottom: 10px">
      <DepartLeftTree ref="leftTree" @select="onTreeSelect" @rootTreeData="onRootTreeData" />
    </a-col>
    <a-col :xl="12" :lg="24" :md="24" style="margin-bottom: 10px">
      <div style="height: 100%;" :class="[`${prefixCls}`]">
        <a-tabs v-show="departData != null" defaultActiveKey="base-info">
          <a-tab-pane tab="기본정보" key="base-info" forceRender style="position: relative">
            <div style="padding: 20px">
              <DepartFormTab :data="departData" :rootTreeData="rootTreeData" @success="onSuccess" />
            </div>
          </a-tab-pane>
          <a-tab-pane tab="부서 권한" key="role-info">
            <div style="padding: 0 20px 20px">
              <DepartRuleTab :data="departData" />
            </div>
          </a-tab-pane>
        </a-tabs>
        <div v-show="departData == null" style="padding-top: 40px">
          <a-empty description="아직 선택된 부서가 없습니다." />
        </div>
      </div>
    </a-col>
  </a-row>
</template>

<script lang="ts" setup name="system-depart">
  import { provide, ref } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import DepartLeftTree from './components/DepartLeftTree.vue';
  import DepartFormTab from './components/DepartFormTab.vue';
  import DepartRuleTab from './components/DepartRuleTab.vue';

  const { prefixCls } = useDesign('depart-manage');
  provide('prefixCls', prefixCls);

  // 하위 컴포넌트About ref 변수를 정의합니다.
  const leftTree = ref();

  // 현재 선택학과 정보
  const departData = ref({});
  const rootTreeData = ref<any[]>([]);

  // 왼쪽 트리가 선택된 후 트리거됨
  function onTreeSelect(data) {
    console.log('onTreeSelect: ', data);
    departData.value = data;
  }

  // 왼쪽 트리 rootTreeData가 트리거됩니다.
  function onRootTreeData(data) {
    rootTreeData.value = data;
  }

  function onSuccess() {
    leftTree.value.loadRootTreeData();
  }
</script>

<style lang="less">
  @import './index.less';
</style>
