<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :width="800" title="사용자 에이전트" @ok="handleSubmit" destroyOnClose>
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formAgentSchema } from './user.data';
  import { getUserAgent, saveOrUpdateAgent } from './user.api';
  // 방출 선언
  const emit = defineEmits(['success', 'register']);
  //Form 구성
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 140, schemas: formAgentSchema,
    showActionButtonGroup: false,
  });
  //Form 할당
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Form 재설정
    await resetFields();
    setModalProps({ confirmLoading: false });
    //Form 데이터를 얻기 위한 쿼리
    const res = await getUserAgent({ userName: data.userName });
    data = res.result ? res.result : data;
    //Form 할당
    await setFieldsValue({ ...data });
  });
  //Form 제출 이벤트
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      //Form 제출
      await saveOrUpdateAgent(values);
      //팝업 창 닫기
      closeModal();
      //목록 새로 고침
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
