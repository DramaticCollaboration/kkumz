<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" width="700px" destroyOnClose>
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { dataRuleFormSchema } from './menu.data';
  import { saveOrUpdateRule } from './menu.api';
  // 방출 선언
  const emit = defineEmits(['success', 'register']);
  const props = defineProps({ permissionId: String });
  const isUpdate = ref(true);
  //Form 구성
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: dataRuleFormSchema,
    showActionButtonGroup: false,
  });
  //Form 할당
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Form 재설정
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      //Form 할당
      await setFieldsValue({
        ...data.record,
      });
    }
  });

  //제목 설정
  const getTitle = computed(() => (!unref(isUpdate) ? '새 규칙 추가' : '규칙 편집'));

  //Form 제출 이벤트
  async function handleSubmit() {
    try {
      const values = await validate();
      values.permissionId = props.permissionId;
      setModalProps({ confirmLoading: true });
      //Form 제출
      await saveOrUpdateRule(values, isUpdate.value);
      //팝업 창 닫기
      closeModal();
      //목록 새로 고침
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
