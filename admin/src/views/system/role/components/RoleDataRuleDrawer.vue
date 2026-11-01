<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="데이터 규칙 구성" width="450px" destroyOnClose>
    <a-tabs defaultActiveKey="1">
      <a-tab-pane tab="데이터 규칙" key="1">
        <a-checkbox-group v-model:value="dataRuleChecked" v-if="dataRuleList.length > 0">
          <a-row>
            <a-col :span="24" v-for="(item, index) in dataRuleList" :key="'dr' + index">
              <a-checkbox :value="item.id">{{ item.ruleName }}</a-checkbox>
            </a-col>

            <a-col :span="24">
              <div style="width: 100%; margin-top: 15px">
                <a-button @click="saveDataRuleForRole" type="primary" size="small"> <Icon icon="ant-design:save-outlined"></Icon>저장을 클릭하세요</a-button>
              </div>
            </a-col>
          </a-row>
        </a-checkbox-group>
        <div v-else><h3>구성 정보가 없습니다!</h3></div>
      </a-tab-pane>
    </a-tabs>
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/src/components/Drawer';
  import { useMessage } from '/src/hooks/web/useMessage';
  import { queryDataRule, saveDataRule } from '../role.api';
  // 방출 선언
  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();
  // 데이터 선언
  const functionId = ref('');
  const roleId = ref('');
  const dataRuleList = ref([]);
  const dataRuleChecked = ref([]);

  /**
   * 데이터
   */
  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await reset();
    setDrawerProps({ confirmLoading: false });
    //권한 ID
    functionId.value = data.functionId;
    //역할 ID
    roleId.value = data.roleId;
    //데이터 쿼리
    const res = await queryDataRule({ functionId: unref(functionId), roleId: unref(roleId) });
    if (res.success) {
      dataRuleList.value = res.result.datarule;
      if (res.result.drChecked) {
        dataRuleChecked.value = res.result.drChecked.split(',');
      }
    }
  });

  /**
   * 재설정
   */
  function reset() {
    functionId.value = '';
    roleId.value = '';
    dataRuleList.value = [];
    dataRuleChecked.value = [];
  }

  /**
   * 제출하다
   */
  async function saveDataRuleForRole() {
    if (!unref(dataRuleChecked) || unref(dataRuleChecked).length == 0) {
      createMessage.warning('현재 데이터 권한이 확인되어 있지 않습니다.');
    }
    let params = {
      permissionId: unref(functionId),
      roleId: unref(roleId),
      dataRuleIds: unref(dataRuleChecked).join(','),
    };
    await saveDataRule(params);
    //팝업 창 닫기
    closeDrawer();
    //목록 새로 고침
    emit('success');
  }
</script>
