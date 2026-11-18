<template>
  <BasicModal
    v-bind="$attrs"
    :title="isUpdate ? '환경설정 항목 편집' : '신규 환경설정 항목 등록'"
    :width="640"
    @register="registerModal"
    @ok="handleSubmit"
  >
    <div class="p-4">
      <BasicForm @register="registerForm" />
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, unref } from 'vue';
import { message } from 'ant-design-vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, FormSchema, useForm } from '/@/components/Form';
import { saveOrUpdateConfig } from '../config.api';

const emit = defineEmits(['success', 'register']);
const isUpdate = ref<boolean>(false);
const recordId = ref<string | undefined>(undefined);

const VALUE_TYPE_CODES = ['STRING', 'NUMBER', 'BOOLEAN', 'JSON', 'SECRET'];

const formSchemas: FormSchema[] = [
  {
    field: 'configCategory',
    label: '설정 카테고리 코드',
    component: 'AutoComplete',
    required: true,
    colProps: { span: 12 },
    componentProps: {
      placeholder: '예: INTEGRATION, AI_LLM, SECURITY, STORAGE, BIZ_POLICY',
      options: [],
    },
  },
  {
    field: 'valueType',
    label: '데이터 타입',
    component: 'Select',
    required: true,
    defaultValue: 'STRING',
    colProps: { span: 12 },
    componentProps: {
      options: VALUE_TYPE_CODES.map((code) => ({ label: code, value: code })),
    },
  },
  {
    field: 'configKey',
    label: '설정 고유 키 (Key)',
    component: 'Input',
    required: true,
    colProps: { span: 24 },
    componentProps: {
      placeholder: '예: kkumz.integration.cms.base-url',
    },
  },
  {
    field: 'configName',
    label: '설정 표시 명칭',
    component: 'Input',
    required: true,
    colProps: { span: 24 },
    componentProps: {
      placeholder: '관리화면에 표시할 한글 명칭',
    },
  },
  {
    field: 'configValue',
    label: '현재 설정값',
    component: 'InputTextArea',
    required: true,
    colProps: { span: 12 },
    componentProps: {
      rows: 2,
      placeholder: '적용할 설정값 입력',
    },
  },
  {
    field: 'defaultValue',
    label: '초기 기본값 (Default)',
    component: 'InputTextArea',
    required: true,
    colProps: { span: 12 },
    componentProps: {
      rows: 2,
      placeholder: '기본값 복원 시 사용될 값',
    },
  },
  {
    field: 'sortOrder',
    label: '정렬 순서',
    component: 'InputNumber',
    defaultValue: 10,
    colProps: { span: 8 },
    componentProps: {
      min: 0,
      max: 9999,
      class: 'w-full',
    },
  },
  {
    field: 'isSecret',
    label: '민감정보 암호화 (AES-GCM)',
    component: 'Switch',
    defaultValue: false,
    colProps: { span: 8 },
  },
  {
    field: 'isDynamic',
    label: '런타임 핫 리로드',
    component: 'Switch',
    defaultValue: true,
    colProps: { span: 8 },
  },
  {
    field: 'description',
    label: '상세 설명 및 운영 가이드',
    component: 'InputTextArea',
    colProps: { span: 24 },
    componentProps: {
      rows: 2,
      placeholder: '해당 설정이 제어하는 비즈니스 로직 및 단위 설명',
    },
  },
];

const [registerForm, { resetFields, setFieldsValue, updateSchema, validate }] = useForm({
  labelWidth: 140,
  baseColProps: { span: 24 },
  schemas: formSchemas,
  showActionButtonGroup: false,
});

const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  await resetFields();
  setModalProps({ confirmLoading: false });
  isUpdate.value = !!data?.isUpdate;
  recordId.value = data?.record?.id;

  const categoryList: string[] = Array.isArray(data?.categories) ? data.categories : [];
  await updateSchema([
    {
      field: 'configCategory',
      componentProps: {
        options: categoryList.map((c) => ({ value: c, label: c })),
      },
    },
    {
      field: 'configKey',
      dynamicDisabled: unref(isUpdate),
    },
  ]);

  if (unref(isUpdate) && data?.record) {
    await setFieldsValue({
      ...data.record,
    });
  } else if (data?.defaultCategory) {
    await setFieldsValue({
      configCategory: data.defaultCategory,
      valueType: 'STRING',
      isSecret: false,
      isDynamic: true,
      sortOrder: 10,
    });
  }
});

async function handleSubmit() {
  try {
    const values = await validate();
    setModalProps({ confirmLoading: true });
    await saveOrUpdateConfig({
      ...values,
      id: unref(recordId),
      status: 1,
    });
    message.success(unref(isUpdate) ? '환경설정 항목이 수정되었습니다.' : '신규 환경설정 항목이 등록되었습니다.');
    closeModal();
    emit('success');
  } catch (e: any) {
    if (e?.message) {
      message.error('설정 저장 실패: ' + e.message);
    }
  } finally {
    setModalProps({ confirmLoading: false });
  }
}
</script>
