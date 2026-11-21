<template>
  <BasicDrawer :title="t('appUi.k6648a00a')" :width="365" @close="onClose" @register="registerDrawer">
    <div v-loading="loading" class="relative">
      <template v-if="desformList.length > 0">
        <a-checkbox-group v-model:value="designNameValue">
          <a-row>
            <a-col :span="24" v-for="item of desformList">
              <a-checkbox :value="item.id">{{ item.roleName }}</a-checkbox>
            </a-col>
          </a-row>
        </a-checkbox-group>
        <div style="width: 100%; margin-top: 15px">
          <a-button type="primary" :loading="loading" :size="'small'" preIcon="ant-design:save-filled" @click="onSubmit">
            <span>{{ t('appUi.k656bc9de') }}</span>
          </a-button>
        </div>
      </template>
      <a-empty v-else :description="t('appUi.kc395f1f1')" />
    </div>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';

  import { queryDepartRoleByUserId, queryDepartRoleUserList, saveDepartRoleUser } from '../depart.user.api';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  defineEmits(['register']);
  const loading = ref<boolean>(false);
  const userId = ref('');
  const departId = ref('');
  const oldRoleId = ref('');
  const desformList = ref<Array<Record<string, unknown>>>([]);
  const designNameValue = ref<Array<string | number>>([]);

  // 서랍 컴포넌트 등록
  const [registerDrawer, { closeDrawer }] = useDrawerInner((data) => {
    userId.value = unref(data.userId);
    departId.value = unref(data.departId);
    loadData();
  });

  async function loadData() {
    try {
      loading.value = true;
      const params = {
        departId: departId.value,
        userId: userId.value,
      };
      // 출발 역할 쿼리
      const [$desformList, $departRoleList] = await Promise.all([queryDepartRoleUserList(params), queryDepartRoleByUserId(params)]);
      desformList.value = $desformList;
      designNameValue.value = $departRoleList.map((item) => item.droleId);
      oldRoleId.value = designNameValue.value.join(',');
    } finally {
      loading.value = false;
    }
  }

  async function onSubmit() {
    try {
      loading.value = true;
      await saveDepartRoleUser({
        userId: userId.value,
        newRoleId: designNameValue.value.join(','),
        oldRoleId: oldRoleId.value,
      });
      doClose();
    } finally {
      loading.value = false;
    }
  }

  function onClose() {
    doReset();
  }

  function doClose() {
    doReset();
    closeDrawer();
  }

  function doReset() {
    userId.value = '';
    departId.value = '';
    oldRoleId.value = '';
    desformList.value = [];
    designNameValue.value = [];
  }
</script>
