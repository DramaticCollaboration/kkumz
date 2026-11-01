<template>
  <BasicModal v-bind="config" @register="registerModal" :title="currTitle" wrapClassName="loginSelectModal" v-model:visible="visible">
    <BasicForm ref="formRef" @register="registerForm" :model="formState" :rules="rules" v-bind="layout" :colon="false" class="loginSelectForm">
      <template #formHeader>
        <div class="w-full">
          <!--여러 부서 옵션-->
          <a-form-item v-if="isMultiDepart" :validate-status="validate_status1" :colon="false">
            <!--라벨 내용-->
            <template #label>
              <a-tooltip placement="topLeft">
                <template #title>
                  <span>귀하는 여러 부서에 속해 있습니다. 로그인 부서를 선택하세요.</span>
                </template>
                <a-avatar style="background-color: rgb(104, 208, 203)" :size="30"> 부서 </a-avatar>
              </a-tooltip>
            </template>
            <template #extra v-if="validate_status1 == 'error'">
              <span style="color: #ed6f6f">로그인 부서를 선택해주세요</span>
            </template>
            <!--부서 드롭다운 콘텐츠-->
            <a-select
              v-model:value="formState.orgCode"
              @change="handleDepartChange"
              placeholder="로그인 부서를 선택해주세요"
              :class="{ 'valid-error': validate_status1 == 'error' }"
            >
              <template v-for="depart in departList" :key="depart.orgCode">
                <a-select-option :value="depart.orgCode">{{ depart.departName }}</a-select-option>
              </template>
            </a-select>
          </a-form-item>
        </div>
      </template>
    </BasicForm>

    <template #footer>
      <a-button @click="handleSubmit" type="primary">확인</a-button>
    </template>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, computed, watch, unref, reactive, UnwrapRef } from 'vue';
  import { Avatar } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import { defHttp } from '/@/utils/http/axios';
  interface FormState {
    orgCode: string | undefined;
  }
  export default defineComponent({
    name: 'loginSelect',
    components: {
      Avatar,
      BasicModal,
      BasicForm,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const userStore = useUserStore();
      const { notification } = useMessage();
      const [registerForm] = useForm({
        showActionButtonGroup: false,
        schemas: [],
      });
      //부서 구성
      const isMultiDepart = ref(false);
      const departList = ref([]);
      const validate_status1 = ref('');
      //팝업 창 표시 또는 숨기기
      const visible = ref(false);
      //로그인 사용자
      const username = ref('');
      //형태
      const formRef = ref();
      //선정된 입주부서 정보
      const formState: UnwrapRef<FormState> = reactive({
        orgCode: undefined,
      });

      const config = {
        maskClosable: false,
        closable: false,
        canFullscreen: false,
        width: '500px',
        minHeight: 20,
        maxHeight: 20,
      };
      //팝업창 동작
      const [registerModal, { closeModal }] = useModalInner();

      //현재 제목
      const currTitle = computed(() => {
        if (unref(isMultiDepart)) {
          return '부서를 선택해주세요.';
        }
      });

      const rules = ref({
        orgCode: [{ required: unref(isMultiDepart), message: '부서를 선택해주세요.', trigger: 'change' }],
      });

      const layout = {
        labelCol: { span: 4 },
        wrapperCol: { span: 18 },
      };
      /**
       * 부서별 상황 처리
       */
      function bizDepart(loginResult) {
        //로그인 인터페이스가 사용자의 마지막 로그인의 테넌트 ID를 반환하는 경우 다시 선택할 필요가 없습니다.
        if(loginResult.userInfo?.orgCode && loginResult.userInfo?.orgCode!==''){
          isMultiDepart.value = false;
          return;
        }
        
        let multi_depart = loginResult.multi_depart;
        //0: 부서 없음 1: 한 부서 2: 여러 부서
        if (multi_depart == 0) {
          notification.warn({
            message: '힌트',
            description: `아직 해당 부서에 속하지 않으셨습니다. 계정정보를 확인해 주세요`,
            duration: 3,
          });
          isMultiDepart.value = false;
        } else if (multi_depart == 2) {
          isMultiDepart.value = true;
          departList.value = loginResult.departs;
        } else {
          isMultiDepart.value = false;
        }
      }


      /**
       * 선택한 테넌트 및 부서 정보를 확인하세요.
       */
      function handleSubmit() {
        if (unref(isMultiDepart) && !formState.orgCode) {
          validate_status1.value = 'error';
          return false;
        }
        formRef.value
          .validate()
          .then(() => {
            departResolve()
              .then(() => {
                emit('success');
              })
              .catch((e) => {
                console.log('로그인 선택 문제', e);
              })
              .finally(() => {
                close();
              });
          })
          .catch((err) => {
            console.log('Form 확인 실패 오류', err);
          });
      }
      /**
       *부서선택으로 전환
       */
      function departResolve() {
        return new Promise((resolve, reject) => {
          if (!unref(isMultiDepart)) {
            resolve();
          } else {
            let params = { orgCode: formState.orgCode, username: unref(username) };
            defHttp.put({ url: '/api/v1/kkumz/sys/selectDepart', params }).then((res) => {
              if (res.userInfo) {
                userStore.setUserInfo(res.userInfo);
                resolve();
              } else {
                requestFailed(res);
                userStore.logout();
                reject();
              }
            });
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
        closeModal();
        reset();
      }
      /**
       * 팝업창 열기 전 처리
       */
      async function show(loginResult) {
        if (loginResult) {
          username.value = userStore.username;
          await reset();
          await bizDepart(loginResult);
          if (!unref(isMultiDepart)) {
            emit('success', userStore.getUserInfo);
          } else {
            visible.value = true;
          }
        }
        //로그인 팝업창이 완료된 후 로그인 ID를 false로 설정하세요.
        loginResult.isLogin = false;
        userStore.setLoginInfo(loginResult);
      }

      /**
       *데이터 재설정
       */
      function reset() {
        departList.value = [];
        validate_status1.value = '';
      }

      function handleDepartChange(e) {
        validate_status1.value = '';
      }

      return {
        registerModal,
        registerForm,
        visible,
        isMultiDepart,
        departList,
        validate_status1,
        formState,
        rules,
        layout,
        formRef,
        currTitle,
        config,
        handleDepartChange,
        show,
        handleSubmit,
      };
    },
  });
</script>

<style lang="less" scoped>
  .loginSelectForm {
    margin-bottom: -20px;
  }

  .loginSelectModal {
    top: 10px;
  }

  .valid-error .ant-select-selection__placeholder {
    color: #f5222d;
  }
</style>
