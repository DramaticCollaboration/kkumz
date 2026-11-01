<template>
  <BasicModal @register="registerModal" :title="title" :width="800" v-bind="$attrs" @ok="onSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { formSchemas } from './template.data';
  import { saveOrUpdate } from './template.api';

  // 선언하다
  const emit = defineEmits(['success', 'register']);
  const title = ref<string>('');
  const isUpdate = ref<boolean>(false);
  // 등록 Form
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema, setProps }] = useForm({
    schemas: formSchemas,
    showActionButtonGroup: false,
  });
  // 모달 등록
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({confirmLoading: false,showCancelBtn:!!data?.showFooter,showOkBtn:!!data?.showFooter});
    isUpdate.value = unref(data.isUpdate);
    title.value = unref(data.title);
    await resetFields();
    await setFieldsValue({ ...data.record });
    // 하단을 숨길 때 전체 Form 비활성화
    setProps({ disabled: !data?.showFooter })
  });

  //Form 제출 이벤트
  async function onSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      // Form 제출
      await saveOrUpdate(values, isUpdate);
      //팝업 창 닫기
      closeModal();
      //목록 새로 고침
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
