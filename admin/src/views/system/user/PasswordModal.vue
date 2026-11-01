<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="비밀번호 변경" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" name="PassWordModal" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formPasswordSchema } from './user.data';
  import { changePassword } from './user.api';
  // 방출 선언
  const emit = defineEmits(['success', 'register']);
  //Form 구성
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: formPasswordSchema,
    showActionButtonGroup: false,
  });
  //Form 할당
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Form 재설정
    await resetFields();
    setModalProps({ confirmLoading: false });
    //Form 할당
    await setFieldsValue({ ...data });
  });
  //Form 제출 이벤트
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      //Form 제출
      await changePassword(values);
      //팝업 창 닫기
      closeModal();
      //목록 새로 고침
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
