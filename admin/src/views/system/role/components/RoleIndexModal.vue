<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="홈페이지 구성" @ok="handleSubmit" width="40%">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { roleIndexFormSchema } from '../role.data';
  import { saveOrUpdateRoleIndex, queryIndexByCode } from '../role.api';
  // 방출문
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  //Form 구성
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 150,
    schemas: roleIndexFormSchema,
    showActionButtonGroup: false,
  });
  //Form 할당
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Form 재설정
    await resetFields();
    setModalProps({ confirmLoading: false });
    setFieldsValue({ roleCode: data.roleCode });
    let res = await queryIndexByCode({ roleCode: data.roleCode });
    isUpdate.value = !!res.result?.id;
    if (unref(isUpdate)) {
      //Form 할당
      await setFieldsValue({
        ...res.result,
      });
    }
  });

  //Form 제출 이벤트
  async function handleSubmit(v) {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Form 제출
      await saveOrUpdateRoleIndex(values, isUpdate.value);
      //팝업 창 닫기
      closeModal();
      //목록 새로 고침
      emit('success', { isUpdate: isUpdate.value, values });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped></style>
