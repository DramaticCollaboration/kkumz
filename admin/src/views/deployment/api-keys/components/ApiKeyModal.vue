<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="isUpdate ? t('appUi.k21274bfd') : t('appUi.kc6549889')"
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
  import { saveAdminApiKey } from '/@/api/kkumz';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(false);
  const currentId = ref('');

  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 130,
    showActionButtonGroup: false,
    schemas: [
      {
        field: 'keyName',
        label: t('appUi.k6fb40084'),
        component: 'Input',
        required: true,
        componentProps: {
          placeholder: t('appUi.k695cbad2'),
        },
        colProps: { span: 16 },
      },
      {
        field: 'direction',
        label: t('appUi.kb553f730'),
        component: 'JDictSelectTag',
        required: true,
        defaultValue: 'OUTBOUND',
        componentProps: {
          dictCode: 'kkumz_api_direction',
          showChooseOption: false,
        },
        colProps: { span: 8 },
      },
      {
        field: 'serviceCode',
        label: t('appUi.k484829ad'),
        component: 'JDictSelectTag',
        required: true,
        defaultValue: 'SYNC_CMS',
        componentProps: {
          dictCode: 'kkumz_api_service_code',
          showChooseOption: false,
        },
        colProps: { span: 12 },
      },
      {
        field: 'baseUrl',
        label: t('appUi.kaaa0d53f'),
        component: 'Input',
        componentProps: {
          placeholder: t('appUi.k2fd49056'),
        },
        helpMessage: t('appUi.k1f599278'),
        colProps: { span: 12 },
      },
      {
        field: 'accessKey',
        label: 'Access Key',
        component: 'Input',
        componentProps: {
          placeholder: t('appUi.k228b1cd5'),
        },
        colProps: { span: 12 },
      },
      {
        field: 'secretKey',
        label: 'Secret Key',
        component: 'InputPassword',
        componentProps: {
          placeholder: t('appUi.kca7db537'),
        },
        colProps: { span: 12 },
      },
      {
        field: 'scopesText',
        label: t('appUi.kf1fb0ae6'),
        component: 'Input',
        helpMessage: t('appUi.k7b675ee3'),
        componentProps: {
          placeholder: 'cms:site:read, cms:site:provision, cms:content:write, cms:form:submit',
        },
        colProps: { span: 16 },
      },
      {
        field: 'rateLimitPerMin',
        label: t('appUi.kb263664e'),
        component: 'InputNumber',
        defaultValue: 1000,
        componentProps: {
          min: 10,
          max: 100000,
          step: 100,
          style: { width: '100%' },
        },
        colProps: { span: 8 },
      },
      {
        field: 'status',
        label: t('appUi.k26b4eee5'),
        component: 'JDictSelectTag',
        defaultValue: 'ACTIVE',
        componentProps: {
          dictCode: 'kkumz_api_key_status',
          showChooseOption: false,
        },
        colProps: { span: 8 },
      },
      {
        field: 'description',
        label: t('appUi.k55542f4b'),
        component: 'InputTextArea',
        componentProps: {
          rows: 3,
          placeholder: t('appUi.k0dbe9d3b'),
        },
        colProps: { span: 16 },
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
        scopesText: Array.isArray(data.record.scopes) ? data.record.scopes.join(', ') : '',
      });
    } else {
      currentId.value = '';
      if (data?.defaultDirection) {
        setFieldsValue({ direction: data.defaultDirection });
      }
    }
  });

  async function handleSubmit() {
    const values = await validate();
    setModalProps({ confirmLoading: true });
    try {
      const scopes = (values.scopesText || '')
        .split(',')
        .map((s: string) => s.trim())
        .filter(Boolean);
      await saveAdminApiKey(
        {
          ...values,
          id: currentId.value || undefined,
          scopes: scopes.length > 0 ? scopes : undefined,
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
