<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" width="550px" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';
  import { BasicForm, useForm } from '/src/components/Form';
  import { formSchema } from '../dict.data';
  import { saveOrUpdateDict } from '../dict.api';
  // 방출 선언
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  const rowId = ref('');
  //Form 구성
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
  });
  //Form 할당
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Form 재설정
    await resetFields();
    setModalProps({ confirmLoading: false, minHeight: 80 });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      rowId.value = data.record.id;
      //Form 할당
      await setFieldsValue({
        ...data.record,
      });
    }
  });
  //제목 설정
  const getTitle = computed(() => (!unref(isUpdate) ? '사전 추가' : '사전 편집'));
  //Form 제출 이벤트
  async function handleSubmit() {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Form 제출
      await saveOrUpdateDict(values, isUpdate.value);
      //팝업 창 닫기
      closeModal();
      //목록 새로 고침
      emit('success', { isUpdate: unref(isUpdate), values: { ...values, id: rowId.value } });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
