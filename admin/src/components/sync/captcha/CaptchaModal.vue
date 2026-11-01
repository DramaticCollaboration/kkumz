<template>
  <BasicModal @register="registerModal" width="450px" :minHeight="100" :title="title" @ok="handleSubmit" destroyOnClose :canFullscreen="false">
    <BasicForm @register="registerForm">
      <template #captchaSlot="{ model, field }">
        <div style="width: 100%; display: flex">
          <a-input style="width: 200px" v-model:value="model[field]" placeholder="이미지 인증코드를 입력해주세요" />
          <div class="margin-left10">
            <img
              class="pointer"
              v-if="randCodeData.requestCodeSuccess"
              style="margin-top: 2px; max-width: initial; height: 30px"
              :src="randCodeData.randCodeImage"
              @click="getCaptchaCode"
            />
            <img v-else class="pointer" style="margin-top: 2px; max-width: initial; height: 30px" :src="codeImage" @click="getCaptchaCode" />
          </div>
        </div>
      </template>
    </BasicForm>
  </BasicModal>
</template>

<script lang="ts" name="CaptchaModal">
  import { defineComponent, reactive, ref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/Modal';
  import { BasicForm, FormSchema, useForm } from '@/components/Form';
  import codeImage from '@/assets/images/checkcode.png';
  import { getCodeInfo } from '@/api/sys/user';
  import { defHttp } from '@/utils/http/axios';
  import {useMessage} from "@/hooks/web/useMessage";

  export default defineComponent({
    name: 'CaptchaModal',
    components: { BasicModal, BasicForm },
    emits: ['ok','register'],
    setup(props, { emit }) {
      const title = ref<string>('인증코드');
      const schemas: FormSchema[] = [
        {
          field: 'captcha',
          component: 'Input',
          label: '이미지 인증 코드',
          rules: [{ required: true }],
          slot: 'captchaSlot',
        },
      ];
      //Form 구성
      const [registerForm, { resetFields, validate }] = useForm({
        schemas: schemas,
        showActionButtonGroup: false,
        baseRowStyle: { "justify-content": 'center', "display": "grid", "margin-top": "10px" },
        rowProps: { "justify": "center" },
        labelCol: { span: 24 },
        wrapperCol: { span: 24 },
      });

      //Form 할당
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: true });
        //Form 재설정
        await resetFields();
        await getCaptchaCode();
        setModalProps({ confirmLoading: false });
      });

      //QR 코드 데이터 저장
      const randCodeData = reactive({
        randCodeImage: '',
        requestCodeSuccess: false,
        checkKey: -1,
      });
      const { createMessage } = useMessage();
      
      /**
       * 인증 코드 받기
       */
      async function getCaptchaCode() {
        await resetFields();
        randCodeData.checkKey = new Date().getTime();
        getCodeInfo(randCodeData.checkKey).then((res) => {
          randCodeData.randCodeImage = res;
          randCodeData.requestCodeSuccess = true;
        });
      }

      /**
       * 써드파티 구성 클릭 이벤트
       */
      async function handleSubmit() {
        let values = await validate();
        defHttp.post({ url: '/api/v1/kkumz/sys/smsCheckCaptcha', params: { captcha: values.captcha, checkKey: randCodeData.checkKey } }, { isTransformResponse: false }).then((res)=>{
          if(res.success){
            emit('ok');
            closeModal();
          }else{
            createMessage.warning(res.message);
            getCaptchaCode();
          }
        }).catch((res) =>{
          createMessage.warning(res.message);
          getCaptchaCode();
        });
      }

      /**
       * 팝업창 닫기
       */
      function handleCancel() {
        closeModal();
      }

      return {
        title,
        registerForm,
        registerModal,
        handleSubmit,
        handleCancel,
        randCodeData,
        codeImage,
        getCaptchaCode,
      };
    },
  });
</script>

<style scoped>
  .margin-left10 {
    margin-left: 10px;
  }
  :deep(.ant-row){
    display: block;
  }
</style>
