<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="isUpdate ? t('appUi.k83184cf1') : t('appUi.kc4b296ab')"
    :width="720"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { saveAdminPrompt } from '/@/api/kkumz';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(false);
  const currentId = ref('');

  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 120,
    showActionButtonGroup: false,
    schemas: [
      {
        field: 'templateCode',
        label: t('appUi.kc071d7b0'),
        component: 'Input',
        required: true,
        colProps: { span: 12 },
      },
      {
        field: 'stageType',
        label: t('appUi.k5e5da1b7'),
        component: 'JDictSelectTag',
        required: true,
        defaultValue: 'NAMING',
        componentProps: {
          dictCode: 'kkumz_stage_type',
          showChooseOption: false,
        },
        colProps: { span: 12 },
      },
      {
        field: 'templateName',
        label: t('appUi.k513fec17'),
        component: 'Input',
        required: true,
        colProps: { span: 16 },
      },
      {
        field: 'modelName',
        label: t('appUi.k46697c5a'),
        component: 'Input',
        required: true,
        colProps: { span: 8 },
      },
      {
        field: 'systemPrompt',
        label: t('appUi.k950730e5'),
        component: 'InputTextArea',
        required: true,
        componentProps: { rows: 4 },
        colProps: { span: 24 },
      },
      {
        field: 'userPromptTemplate',
        label: t('appUi.k9f6d3868'),
        component: 'InputTextArea',
        required: true,
        componentProps: { rows: 3 },
        colProps: { span: 24 },
      },
      {
        field: 'forbiddenKeywordsText',
        label: t('appUi.k8ea3e128'),
        component: 'Input',
        helpMessage: t('appUi.k4c90f571'),
        colProps: { span: 16 },
      },
      {
        field: 'status',
        label: t('appUi.k2926977b'),
        component: 'JDictSelectTag',
        defaultValue: 'ACTIVE',
        componentProps: {
          dictCode: 'kkumz_active_status',
          showChooseOption: false,
        },
        colProps: { span: 8 },
      },
    ],
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate) && data?.record) {
      currentId.value = data.record.id;
      setFieldsValue({
        ...data.record,
        forbiddenKeywordsText: (data.record.forbiddenKeywords || []).join(', '),
      });
    } else {
      currentId.value = '';
    }
  });

  async function handleSubmit() {
    const values = await validate();
    setModalProps({ confirmLoading: true });
    try {
      const forbiddenKeywords = (values.forbiddenKeywordsText || '')
        .split(',')
        .map((s: string) => s.trim())
        .filter(Boolean);
      await saveAdminPrompt(
        {
          ...values,
          id: currentId.value || undefined,
          forbiddenKeywords,
        },
        unref(isUpdate),
      );
      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
