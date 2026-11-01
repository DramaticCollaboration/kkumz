<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="900px" destroyOnClose>
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './notice.data';
  import { saveOrUpdate } from './notice.api';
  // 방출 선언
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  //Form 구성
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
  });
  //Form 할당
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Form 재설정
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      if (data.record.userIds) {
        data.record.userIds = data.record.userIds.substring(0, data.record.userIds.length - 1);
      }
      //Form 할당
      await setFieldsValue({
        ...data.record,
      });
    }
  });
  //제목 설정
  const title = computed(() => (!unref(isUpdate) ? '등록' : '편집'));
  //Form 제출 이벤트
  async function handleSubmit(v) {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Form 제출
      if(values.msgType==='ALL'){
        values.userIds = '';
      }else{
        values.userIds += ',';
      }
      await saveOrUpdate(values, isUpdate.value);
      //팝업 창 닫기
      closeModal();
      //목록 새로 고침
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
