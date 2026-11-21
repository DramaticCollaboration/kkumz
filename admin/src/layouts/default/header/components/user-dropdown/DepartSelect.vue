<template>
  <BasicModal v-bind="config" :maxHeight="500" :title="currTitle" v-model:visible="visible" wrapClassName="loginSelectModal">
    <a-form ref="formRef" v-bind="layout" :colon="false" class="loginSelectForm">
      <a-form-item v-if="isMultiDepart" :validate-status="validate_status1">
        <!--라벨 내용-->
        <template #label>
          <a-tooltip placement="topLeft">
            <template #title>
              <span>{{ t('appUi.ke3cfd95a') }}</span>
            </template>
            <a-avatar style="background-color: rgb(104, 208, 203)" :size="30"> {{ t('appUi.ke5b530d6') }} </a-avatar>
          </a-tooltip>
        </template>
        <!--부서 드롭다운 콘텐츠-->
        <a-select v-model:value="departSelected" :placeholder="t('appUi.k21eedb85')" :class="{ 'valid-error': validate_status1 == 'error' }">
          <template #suffixIcon>
            <Icon icon="ant-design:gold-outline" />
          </template>
          <template v-for="depart in departList" :key="depart.orgCode">
            <a-select-option :value="depart.orgCode">{{ depart.departName }}</a-select-option>
          </template>
        </a-select>
      </a-form-item>
    </a-form>

    <template #footer>
      <a-button @click="close">{{ t('appUi.k19b2d19b') }}</a-button>
      <a-button @click="handleSubmit" type="primary">{{ t('appUi.k468266d6') }}</a-button>
    </template>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, watch, unref } from 'vue';
  import { Avatar } from 'ant-design-vue';
  import { BasicModal } from '/@/components/Modal';
  import { getUserDeparts, selectDepart } from '/@/views/system/depart/depart.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { $t } from '/@/locales';
  const { t } = useI18n();

  const userStore = useUserStore();
  const { createMessage, notification } = useMessage();
  const props = defineProps({
    title: { type: String, default: () => $t('appUi.k66d71b07') },
    closable: { type: Boolean, default: false },
    username: { type: String, default: '' },
  });

  const layout = {
    labelCol: { span: 4 },
    wrapperCol: { span: 18 },
  };

  const config = {
    maskClosable: false,
    closable: false,
    canFullscreen: false,
    width: '500px',
    minHeight: 20,
    maxHeight: 20,
  };
  const currTitle = ref('');

  const isMultiDepart = ref(false);
  const currentDepartName = ref('');
  const departSelected = ref('');
  const departList = ref([]);
  const validate_status1 = ref('');
  //팝업 창 표시 또는 숨기기
  const visible = ref(false);
  /**
   * 팝업창 열기 전 처리
   */
  async function show() {
    //부하 부서
    await loadDepartList();
    //타이틀 구성
    if (unref(isMultiDepart)) {
      currTitle.value =
        unref(currentDepartName) && unref(currentDepartName).length > 0 ? t('appUi.k61c07078', { p0: unref(currentDepartName) }) : props.title;
    }
    //모델 표시 및 숨김
    if (unref(isMultiDepart)) {
      visible.value = true;
    }
  }
  /**
   *부서정보 불러오기
   */
  async function loadDepartList() {
    const result = await getUserDeparts();
    if (!result.list || result.list.length == 0) {
      return;
    }
    let currentDepart = result.list.filter((item) => item.orgCode == result.orgCode);
    departList.value = result.list;
    departSelected.value = currentDepart && currentDepart.length > 0 ? result.orgCode : '';
    currentDepartName.value = currentDepart && currentDepart.length > 0 ? currentDepart[0].departName : '';
    isMultiDepart.value = true;
  }

  /**
   * 데이터 제출
   */
  async function handleSubmit() {
    if (unref(isMultiDepart) && !unref(departSelected)) {
      validate_status1.value = 'error';
      return false;
    }
    departResolve()
      .then(() => {
        createMessage.success(t('appUi.k76c08714'));
        
        //테넌트 전환 후 홈페이지 새로 고침
        window.location.reload();
      })
      .catch((e) => {
        console.log('로그인 선택 문제', e);
      })
      .finally(() => {
        close();
      });
  }
  /**
   *부서선택으로 전환
   */
  function departResolve() {
    return new Promise(async (resolve, reject) => {
      if (!unref(isMultiDepart)) {
        resolve();
      } else {
        const result = await selectDepart({
          username: userStore.getUserInfo.username,
          orgCode: unref(departSelected),
        });
        if (result.userInfo) {
          const userInfo = result.userInfo;
          userStore.setUserInfo(userInfo);
          resolve();
        } else {
          requestFailed(result);
          userStore.logout();
          reject();
        }
      }
    });
  }
  /**
   * 요청 실패 처리
   */
  function requestFailed(err) {
    notification.error({
      message: t('appUi.k56efcfd5'),
      description: ((err.response || {}).data || {}).message || err.message || t('appUi.kdeee49cc'),
      duration: 4,
    });
  }
  /**
   * 모델 닫기
   */
  function close() {
    departClear();
  }

  /**
   *초기화 데이터
   */
  function departClear() {
    currTitle.value = '';

    isMultiDepart.value = false;
    currentDepartName.value = '';
    departSelected.value = '';
    departList.value = [];
    validate_status1.value = '';

    visible.value = false;
  }

  /**
   * 모니터 사용자 이름
   */
  watch(
    () => props.username,
    (value) => {
      value && loadDepartList();
    }
  );

  defineExpose({
    show,
  });
</script>
<style lang="less" scoped>
  .loginSelectForm {
    margin-bottom: -20px;
  }

  .loginSelectModal {
    top: 20px;
  }

  .valid-error .ant-select-selection__placeholder {
    color: #f5222d;
  }
</style>
