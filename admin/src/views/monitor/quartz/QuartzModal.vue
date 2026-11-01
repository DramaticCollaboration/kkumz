<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="40%">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './quartz.data';
  import { saveOrUpdateQuartz, getQuartzById } from './quartz.api';
  import { isJsonObjectString } from '/@/utils/is';
  // 방출문
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  //Form 구성
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    // labelWidth: 150,
    schemas: formSchema,
    showActionButtonGroup: false,
    labelWidth: 100,
  });
  //Form 할당
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Form 재설정
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      //세부정보 보기
      //data.record = await getQuartzById({id: data.record.id});
      try {
        data.record.paramterType = isJsonObjectString(data?.record?.parameter) ? 'json' : 'string';
      } catch (e) {
        console.log(e);
      }
      //Form 할당
      await setFieldsValue({
        ...data.record,
      });
    }
  });
  //제목 설정
  const title = computed(() => (!unref(isUpdate) ? '새 작업 추가' : '작업 수정'));
  //Form 제출 이벤트
  async function handleSubmit(v) {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Form 제출
      await saveOrUpdateQuartz(values, isUpdate.value);
      //팝업 창 닫기
      closeModal();
      //목록 새로 고침
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
