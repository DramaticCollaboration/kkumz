<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="커뮤니티 모더레이션 신고 심사 처리"
    :width="620"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { resolveModerationReport } from '/@/api/kkumz';
  import { useUserStore } from '/@/store/modules/user';

  const emit = defineEmits(['success', 'register']);
  const userStore = useUserStore();
  const reportId = ref('');

  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 120,
    showActionButtonGroup: false,
    schemas: [
      {
        field: 'brandName',
        label: '대상 사이트',
        component: 'Input',
        dynamicDisabled: true,
        colProps: { span: 12 },
      },
      {
        field: 'reasonCategory',
        label: '신고 사유 유형',
        component: 'Input',
        dynamicDisabled: true,
        colProps: { span: 12 },
      },
      {
        field: 'reasonDetail',
        label: '상세 신고 내용',
        component: 'InputTextArea',
        dynamicDisabled: true,
        componentProps: { rows: 2 },
        colProps: { span: 24 },
      },
      {
        field: 'status',
        label: '심사 결정',
        component: 'Select',
        required: true,
        defaultValue: 'RESOLVED',
        componentProps: {
          options: [
            { label: '조치 완료', value: 'RESOLVED' },
            { label: '반려 / 이상 없음', value: 'DISMISSED' },
          ],
        },
        colProps: { span: 12 },
      },
      {
        field: 'suspendSite',
        label: '사이트 즉시 정지',
        component: 'Switch',
        defaultValue: false,
        colProps: { span: 12 },
      },
      {
        field: 'resolutionNote',
        label: '심사 의견 기록',
        component: 'InputTextArea',
        required: true,
        componentProps: { rows: 3, placeholder: '모더레이션 조치 사유 및 결과를 기록하세요.' },
        colProps: { span: 24 },
      },
    ],
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner((data) => {
    resetFields();
    if (data?.record) {
      reportId.value = data.record.id;
      setFieldsValue({
        brandName: data.record.brandName || data.record.siteSlug,
        reasonCategory: data.record.reasonCategory,
        reasonDetail: data.record.reasonDetail,
        status: data.record.status === 'PENDING' ? 'RESOLVED' : data.record.status,
        resolutionNote: data.record.resolutionNote || '',
        suspendSite: false,
      });
    }
  });

  async function handleSubmit() {
    const values = await validate();
    setModalProps({ confirmLoading: true });
    try {
      await resolveModerationReport(reportId.value, {
        status: values.status,
        resolutionNote: values.resolutionNote,
        suspendSite: !!values.suspendSite,
        resolvedBy: userStore.getUserInfo?.username || '',
      });
      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
