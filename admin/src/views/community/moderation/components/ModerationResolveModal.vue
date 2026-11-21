<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('appUi.k96bef73f')"
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
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const emit = defineEmits(['success', 'register']);
  const userStore = useUserStore();
  const reportId = ref('');

  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 120,
    showActionButtonGroup: false,
    schemas: [
      {
        field: 'brandName',
        label: t('appUi.k32e52cff'),
        component: 'Input',
        dynamicDisabled: true,
        colProps: { span: 12 },
      },
      {
        field: 'reasonCategory',
        label: t('appUi.k02848934'),
        component: 'Input',
        dynamicDisabled: true,
        colProps: { span: 12 },
      },
      {
        field: 'reasonDetail',
        label: t('appUi.kfe5807da'),
        component: 'InputTextArea',
        dynamicDisabled: true,
        componentProps: { rows: 2 },
        colProps: { span: 24 },
      },
      {
        field: 'status',
        label: t('appUi.k2bb9a21b'),
        component: 'JDictSelectTag',
        required: true,
        defaultValue: 'RESOLVED',
        componentProps: {
          dictCode: 'kkumz_moderation_status',
          showChooseOption: false,
        },
        colProps: { span: 12 },
      },
      {
        field: 'suspendSite',
        label: t('appUi.k4baa960e'),
        component: 'Switch',
        defaultValue: false,
        colProps: { span: 12 },
      },
      {
        field: 'resolutionNote',
        label: t('appUi.k0bc9e520'),
        component: 'InputTextArea',
        required: true,
        componentProps: { rows: 3, placeholder: t('appUi.k13bc8746') },
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
