<template>
  <BasicDrawer :title="t('appUi.k1590891e')" :width="365" @close="onClose" @register="registerDrawer">
    <div v-loading="loading" class="relative">
      <a-tabs defaultActiveKey="1">
        <a-tab-pane :tab="t('appUi.k6296eba0')" key="1">
          <a-checkbox-group v-model:value="dataRuleChecked" v-if="dataRuleList.length > 0">
            <a-row>
              <a-col :span="24" v-for="(item, index) in dataRuleList" :key="'dr' + index">
                <a-checkbox :value="item.id">{{ item.ruleName }}</a-checkbox>
              </a-col>
              <a-col :span="24">
                <div style="width: 100%; margin-top: 15px">
                  <a-button type="primary" :loading="loading" :size="'small'" preIcon="ant-design:save-filled" @click="saveDataRuleForRole">
                    <span>{{ t('appUi.k656bc9de') }}</span>
                  </a-button>
                </div>
              </a-col>
            </a-row>
          </a-checkbox-group>
          <a-empty v-else :description="t('appUi.kc395f1f1')" />
        </a-tab-pane>
      </a-tabs>
    </div>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';

  import { queryDepartDataRule, saveDepartDataRule } from '../depart.api';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  defineEmits(['register']);
  const loading = ref<boolean>(false);
  const departId = ref('');
  const functionId = ref('');
  const dataRuleList = ref<Array<Record<string, unknown>>>([]);
  const dataRuleChecked = ref<Array<string | number>>([]);

  // 서랍 컴포넌트 등록
  const [registerDrawer, { closeDrawer }] = useDrawerInner((data) => {
    departId.value = unref(data.departId);
    functionId.value = unref(data.functionId);
    loadData();
  });

  async function loadData() {
    try {
      loading.value = true;
      const { datarule, drChecked } = await queryDepartDataRule(functionId, departId);
      dataRuleList.value = datarule;
      if (drChecked) {
        dataRuleChecked.value = drChecked.split(',');
      }
    } finally {
      loading.value = false;
    }
  }

  function saveDataRuleForRole() {
    let params = {
      departId: departId.value,
      permissionId: functionId.value,
      dataRuleIds: dataRuleChecked.value.join(','),
    };
    saveDepartDataRule(params);
  }

  function onClose() {
    doReset();
  }

  function doReset() {
    functionId.value = '';
    dataRuleList.value = [];
    dataRuleChecked.value = [];
  }
</script>
