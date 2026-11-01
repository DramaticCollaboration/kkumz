<template>
  <BasicModal :title="title" :width="800" v-bind="$attrs" @ok="handleOk" @register="registerModal">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { computed, inject, ref, unref } from 'vue';

  import { BasicForm, useForm } from '/@/components/Form/index';
  // noinspection ES6UnusedImports
  import { BasicModal, useModalInner } from '/@/components/Modal';

  import { saveOrUpdateDepartRole } from '../depart.user.api';
  import { departRoleModalFormSchema } from '../depart.user.data';

  const emit = defineEmits(['success', 'register']);
  const props = defineProps({
    // 현재 부서 ID
    departId: { require: true, type: String },
  });
  const prefixCls = inject('prefixCls');
  // 현재 업데이트 모드인지 여부
  const isUpdate = ref<boolean>(true);
  // 현재 팝업 데이터
  const model = ref<object>({});
  const title = computed(() => (isUpdate.value ? '편집' : '등록'));

  //등록 Form
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    schemas: departRoleModalFormSchema,
    showActionButtonGroup: false,
  });

  // 등록팝업창
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    await resetFields();
    isUpdate.value = unref(data?.isUpdate);
    // 추가 또는 편집 여부에 관계없이 Form 값을 설정할 수 있습니다
    let record = unref(data?.record);
    if (typeof record === 'object') {
      model.value = record;
      await setFieldsValue({ ...record });
    }
  });

  //이벤트 제출
  async function handleOk() {
    try {
      setModalProps({ confirmLoading: true });
      let values = await validate();
      values.departId = unref(props.departId);
      //Form 제출
      await saveOrUpdateDepartRole(values, isUpdate.value);
      //팝업 창 닫기
      closeModal();
      //목록 새로 고침
      emit('success', { isUpdate: unref(isUpdate), values });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
