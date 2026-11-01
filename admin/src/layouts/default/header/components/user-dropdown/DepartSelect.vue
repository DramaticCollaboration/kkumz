<template>
  <BasicModal v-bind="config" :maxHeight="500" :title="currTitle" v-model:visible="visible" wrapClassName="loginSelectModal">
    <a-form ref="formRef" v-bind="layout" :colon="false" class="loginSelectForm">
      <a-form-item v-if="isMultiDepart" :validate-status="validate_status1">
        <!--라벨 내용-->
        <template #label>
          <a-tooltip placement="topLeft">
            <template #title>
              <span>귀하는 여러 부서에 속해 있습니다. 현재 부서를 선택하세요.</span>
            </template>
            <a-avatar style="background-color: rgb(104, 208, 203)" :size="30"> 부서 </a-avatar>
          </a-tooltip>
        </template>
        <!--부서 드롭다운 콘텐츠-->
        <a-select v-model:value="departSelected" placeholder="로그인 부서를 선택해주세요" :class="{ 'valid-error': validate_status1 == 'error' }">
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
      <a-button @click="close">취소</a-button>
      <a-button @click="handleSubmit" type="primary">확인</a-button>
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

  const userStore = useUserStore();
  const { createMessage, notification } = useMessage();
  const props = defineProps({
    title: { type: String, default: '학과선택' },
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
        unref(currentDepartName) && unref(currentDepartName).length > 0 ? `부서 변경（현재 부서:${unref(currentDepartName)}）` : props.title;
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
        createMessage.success('전환 성공');
        
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
      message: '로그인 실패',
      description: ((err.response || {}).data || {}).message || err.message || '요청에 오류가 발생했습니다. 나중에 다시 시도해 주세요.',
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
