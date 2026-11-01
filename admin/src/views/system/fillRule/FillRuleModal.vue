<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" :width="800" destroyOnClose>
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './fill.rule.data';
  import { saveFillRule, updateFillRule } from './fill.rule.api';

  //제목 설정
  const title = computed(() => (!unref(isUpdate) ? '등록' : '편집'));

  // 방출 선언
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);

  //Form 구성
  const [registerForm, { resetFields, setFieldsValue, validate, getFieldsValue }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 12 },
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

  //Form 제출 이벤트
  async function handleSubmit() {
    try {
      let formValue = await validate();
      setModalProps({ confirmLoading: true });
      if (isUpdate.value) {
        let allFieldsValue = getFieldsValue();
        // 편집 페이지 Form에 상위 드롭다운 상자가 없는 경우 유효성 검사 메서드는 제출 시 이 값을 반환하지 않으므로 수동으로 설정해야 합니다.
        if (!formValue.parentId && allFieldsValue.parentId) {
          formValue.parentId = allFieldsValue.parentId;
        }
        await updateFillRule(formValue);
      } else {
        await saveFillRule(formValue);
      }
      //팝업 창 닫기
      closeModal();
      //목록 새로 고침
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
