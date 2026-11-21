<template>
  <BasicModal @register="registerModal" :title="t('appUi.kb0465814')" :width="800" v-bind="$attrs" @ok="onSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { sendTestFormSchemas } from './template.data';
  import { sendMessageTest } from './template.api';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  // 선언하다
  const emit = defineEmits(['register']);
  // 등록 Form
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    schemas: sendTestFormSchemas,
    showActionButtonGroup: false,
  });
  // 모달 등록
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    await resetFields();
    await setFieldsValue({ ...unref(data.record) });
  });

  //Form 제출 이벤트
  async function onSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      // Form 제출
      await sendMessageTest(values);
      //팝업 창 닫기
      closeModal();
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
