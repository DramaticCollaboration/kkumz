<template>
  <BasicModal
    @register="registerModal"
    :title="title"
    width="40%"
    v-bind="$attrs"
    @ok="handleSubmit"
  >
    <div class="content"> 
      <BasicForm  @register="registerForm"/>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
import {computed, ref, unref} from 'vue';
import {BasicModal, useModalInner} from '/@/components/Modal';
import {BasicForm, useForm} from '/@/components/Form/index';
import {formSchema} from '../SysTableWhiteList.data';
import {saveOrUpdate} from '../SysTableWhiteList.api';
// 방출문
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
//Form 구성
const [registerForm, {resetFields, setFieldsValue, validate}] = useForm({
  labelWidth: 120,
  wrapperCol: null,
  schemas: formSchema,
  showActionButtonGroup: false,
});
//Form 할당
const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
  //Form 재설정
  await resetFields();
  setModalProps({
    confirmLoading: false,
    showCancelBtn: data?.showFooter,
    showOkBtn: data?.showFooter
  });
  isUpdate.value = !!data?.isUpdate;
  if (unref(isUpdate)) {
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
    setModalProps({confirmLoading: true});
    //Form 제출
    await saveOrUpdate(values, isUpdate.value);
    //팝업 창 닫기
    closeModal();
    //목록 새로 고침
    emit('success', {isUpdate: isUpdate.value, values});
  } finally {
    setModalProps({confirmLoading: false});
  }
}
</script>

<style lang="less" scoped>
  .content {
    padding: 20px 8% 0 4%;
  }
</style>
