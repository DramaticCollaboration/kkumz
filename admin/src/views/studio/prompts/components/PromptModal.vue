<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="isUpdate ? 'AI 프롬프트 템플릿 & 브랜드 필터 수정' : '신규 AI 프롬프트 템플릿 등록'"
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

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(false);
  const currentId = ref('');

  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 120,
    showActionButtonGroup: false,
    schemas: [
      {
        field: 'templateCode',
        label: '템플릿 코드',
        component: 'Input',
        required: true,
        colProps: { span: 12 },
      },
      {
        field: 'stageType',
        label: '스튜디오 단계',
        component: 'Select',
        required: true,
        defaultValue: 'NAMING',
        componentProps: {
          options: [
            { label: '브랜드 네이밍 (NAMING)', value: 'NAMING' },
            { label: '벡터 SVG 로고 (LOGO_SVG)', value: 'LOGO_SVG' },
            { label: '마크다운 기능명세서 (FRS_SPEC)', value: 'FRS_SPEC' },
            { label: '대화형 프리뷰 수정 (CHAT_REFINE)', value: 'CHAT_REFINE' },
            { label: 'WebIDE 빌드 패치 (WEBIDE_FIX)', value: 'WEBIDE_FIX' },
          ],
        },
        colProps: { span: 12 },
      },
      {
        field: 'templateName',
        label: '템플릿 명칭',
        component: 'Input',
        required: true,
        colProps: { span: 16 },
      },
      {
        field: 'modelName',
        label: 'AI 모델명',
        component: 'Input',
        required: true,
        defaultValue: 'sync-llm-sonnet-4.5',
        colProps: { span: 8 },
      },
      {
        field: 'systemPrompt',
        label: '시스템 프롬프트',
        component: 'InputTextArea',
        required: true,
        componentProps: { rows: 4 },
        colProps: { span: 24 },
      },
      {
        field: 'userPromptTemplate',
        label: '유저 프롬프트 템플릿',
        component: 'InputTextArea',
        required: true,
        componentProps: { rows: 3 },
        colProps: { span: 24 },
      },
      {
        field: 'forbiddenKeywordsText',
        label: '브랜드 금지어 필터',
        component: 'Input',
        helpMessage: '쉼표(,)로 구분하여 입력 (예: 도박,불법,사칭,피싱)',
        colProps: { span: 16 },
      },
      {
        field: 'status',
        label: '상태',
        component: 'Select',
        defaultValue: 'ACTIVE',
        componentProps: {
          options: [
            { label: '활성 (ACTIVE)', value: 'ACTIVE' },
            { label: '비활성 (INACTIVE)', value: 'INACTIVE' },
          ],
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
