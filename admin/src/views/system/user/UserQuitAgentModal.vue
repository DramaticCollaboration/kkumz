<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :width="800" title="이양" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup name="user-quit-agent-modal">
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formQuitAgentSchema } from './user.data';
  import { getUserAgent, userQuitAgent } from './user.api';
  import dayjs from 'dayjs';
  // 방출 선언
  const emit = defineEmits(['success', 'register']);
  //Form 구성
  const [registerForm, { resetFields, setFieldsValue, validate, clearValidate, updateSchema }] = useForm({
    labelWidth: 140, schemas: formQuitAgentSchema,
    showActionButtonGroup: false,
  });
  //Form 할당
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Form 재설정
    await resetFields();
    setModalProps({ confirmLoading: true });
    
    let userId = data.userId;
    //Form 데이터를 얻기 위한 쿼리
    const res = await getUserAgent({ userName: data.userName });
    data = res.result ? res.result : data;
    let date = new Date();
    if (!data.startTime) {
      data.startTime = dayjs(date).format('YYYY-MM-DD HH:mm:ss');
    }
    if (!data.endTime) {
      data.endTime = getYear(date);
    }

    await updateSchema(
      [{
        field:'agentUserName',
        componentProps:{
          excludeUserIdList:[userId]
        }
      }]
    )
    //Form 할당
    await setFieldsValue({ ...data });
    setModalProps({ confirmLoading: false });
  });
  //Form 제출 이벤트
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      //Form 제출
      await userQuitAgent(values);
      //팝업 창 닫기
      closeModal();
      //목록 새로 고침
      emit('success',values.userName);
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  /**
   *인수 후 30년
   */
  function getYear(date) {
    //이는 숫자 값입니다.
    let y = date.getFullYear() + 30;
    let m = dayjs(date).format('MM');
    let d = dayjs(date).format('DD');
    let hour = dayjs(date).format('HH:mm:ss');
    console.log('년 월 일', y + '-' + m + '-' + d);
    return dayjs(y + '-' + m + '-' + d + ' ' + hour).format('YYYY-MM-DD HH:mm:ss');
  }
</script>
