<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="isUpdate ? '서비스 연동 API 액세스 키 설정 수정' : '신규 API 액세스 키 발급 / 연동 등록'"
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

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(false);
  const currentId = ref('');

  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 130,
    showActionButtonGroup: false,
    schemas: [
      {
        field: 'keyName',
        label: '액세스키 명칭',
        component: 'Input',
        required: true,
        componentProps: {
          placeholder: '예: SyncCMS 멀티사이트 연동 키 / 외부 파트너 API 키',
        },
        colProps: { span: 16 },
      },
      {
        field: 'direction',
        label: '연동 방향',
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
        label: '대상 서비스',
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
        label: '원격 Base URL',
        component: 'Input',
        componentProps: {
          placeholder: '예: https://cms.empasy.com 또는 https://shop.empasy.com',
        },
        helpMessage: 'OUTBOUND 연동 시 호출할 대상 서비스의 기본 URL',
        colProps: { span: 12 },
      },
      {
        field: 'accessKey',
        label: 'Access Key',
        component: 'Input',
        componentProps: {
          placeholder: '비워두면 표준 규격(ak_...)으로 자동 생성됩니다',
        },
        colProps: { span: 12 },
      },
      {
        field: 'secretKey',
        label: 'Secret Key',
        component: 'InputPassword',
        componentProps: {
          placeholder: '비워두면 고강도 시크릿(sk_...)이 자동 발급됩니다',
        },
        colProps: { span: 12 },
      },
      {
        field: 'scopesText',
        label: '허용 권한 스코프',
        component: 'Input',
        helpMessage: '쉼표(,) 구분 입력 (예: cms:site:read, cms:site:provision, cms:content:write, cms:form:submit)',
        componentProps: {
          placeholder: 'cms:site:read, cms:site:provision, cms:content:write, cms:form:submit',
        },
        colProps: { span: 16 },
      },
      {
        field: 'rateLimitPerMin',
        label: '분당 호출 제한(RPM)',
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
        label: '키 상태',
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
        label: '용도 및 비고',
        component: 'InputTextArea',
        componentProps: {
          rows: 3,
          placeholder: '해당 API 액세스 키의 연동 목적 및 호출 범위를 기록하세요.',
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
