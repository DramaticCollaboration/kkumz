<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="600px">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { rules } from '/@/utils/helper/validator';
  import { defHttp } from '/@/utils/http/axios';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import BasicForm from '/@/components/Form/src/BasicForm.vue';
  import { useForm } from '/@/components/Form/src/hooks/useForm';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useLocaleStore } from '/@/store/modules/locale';
  import { useI18n } from '/@/hooks/web/useI18n';
  const localeStore = useLocaleStore();
  const { t } = useI18n();
  // 방출 선언
  const emit = defineEmits(['register']);
  const $message = useMessage();
  const formRef = ref();
  const username = ref('');
  const title = ref(t('layout.changePassword.changePassword'));
  //Form 구성
  const [registerForm, { resetFields, validate, clearValidate }] = useForm({
    schemas: [
      {
        label: t('layout.changePassword.oldPassword'),
        field: 'oldpassword',
        component: 'InputPassword',
        required: true,
      },
      {
        label: t('layout.changePassword.newPassword'),
        field: 'password',
        component: 'StrengthMeter',
        componentProps: {
          placeholder: t('layout.changePassword.pleaseEnterNewPassword'),
        },
        rules: [
          {
            required: true,
            message: t('layout.changePassword.pleaseEnterNewPassword'),
          },
        ],
      },
      {
        label: t('layout.changePassword.confirmNewPassword'),
        field: 'confirmpassword',
        component: 'InputPassword',
        dynamicRules: ({ values }) => rules.confirmPassword(values, true),
      },
    ],
    showActionButtonGroup: false,
    wrapperCol: null,
    labelWidth: localeStore.getLocale == 'ko_KR' ? 100 : 160,
  });
  //Form 할당
  const [registerModal, { setModalProps, closeModal }] = useModalInner();

  //Form 제출 이벤트
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      //Form 제출
      let params = Object.assign({ username: unref(username) }, values);
      defHttp.put({ url: '/api/v1/kkumz/sys/user/updatePassword', params }, { isTransformResponse: false }).then((res) => {
        if (res.success) {
          $message.createMessage.success(res.message);
          //팝업 창 닫기
          closeModal();
        } else {
          $message.createMessage.warning(res.message);
        }
      });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  async function show(name) {
    if (!name) {
      $message.createMessage.warning('현재 시스템에 로그인한 사용자가 없습니다!');
      return;
    } else {
      username.value = name;
      await setModalProps({ visible: true });
      await resetFields();
      await clearValidate();
    }
  }

  defineExpose({
    title,
    show,
  });
</script>
