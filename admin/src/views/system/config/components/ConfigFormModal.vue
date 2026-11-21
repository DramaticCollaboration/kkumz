<template>
  <BasicModal
    v-bind="$attrs"
    :title="isUpdate ? t('appUi.k42bbaeaf') : t('appUi.ke06703c7')"
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
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

const emit = defineEmits(['success', 'register']);
const isUpdate = ref<boolean>(false);
const recordId = ref<string | undefined>(undefined);

const VALUE_TYPE_CODES = ['STRING', 'NUMBER', 'BOOLEAN', 'JSON', 'SECRET'];

const formSchemas: FormSchema[] = [
  {
    field: 'configCategory',
    label: t('appUi.kc88cf5e0'),
    component: 'AutoComplete',
    required: true,
    colProps: { span: 12 },
    componentProps: {
      placeholder: t('appUi.k9d1056ad'),
      options: [],
    },
  },
  {
    field: 'valueType',
    label: t('appUi.k9b7bb0ee'),
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
    label: t('appUi.kca77ff42'),
    component: 'Input',
    required: true,
    colProps: { span: 24 },
    componentProps: {
      placeholder: t('appUi.kd1338da1'),
    },
  },
  {
    field: 'configName',
    label: t('appUi.k60fd0c71'),
    component: 'Input',
    required: true,
    colProps: { span: 24 },
    componentProps: {
      placeholder: t('appUi.ka10b5ee9'),
    },
  },
  {
    field: 'configValue',
    label: t('appUi.k9e477027'),
    component: 'InputTextArea',
    required: true,
    colProps: { span: 12 },
    componentProps: {
      rows: 2,
      placeholder: t('appUi.ke43f2aff'),
    },
  },
  {
    field: 'defaultValue',
    label: t('appUi.kecfec860'),
    component: 'InputTextArea',
    required: true,
    colProps: { span: 12 },
    componentProps: {
      rows: 2,
      placeholder: t('appUi.keabc03a3'),
    },
  },
  {
    field: 'sortOrder',
    label: t('appUi.k11c67410'),
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
    label: t('appUi.k734e1a28'),
    component: 'Switch',
    defaultValue: false,
    colProps: { span: 8 },
  },
  {
    field: 'isDynamic',
    label: t('appUi.ka616a5c0'),
    component: 'Switch',
    defaultValue: true,
    colProps: { span: 8 },
  },
  {
    field: 'description',
    label: t('appUi.k3949eb2a'),
    component: 'InputTextArea',
    colProps: { span: 24 },
    componentProps: {
      rows: 2,
      placeholder: t('appUi.kfed074d4'),
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
    message.success(unref(isUpdate) ? t('appUi.k5dd0a384') : t('appUi.k1ab088cf'));
    closeModal();
    emit('success');
  } catch (e: any) {
    if (e?.message) {
      message.error(t('appUi.k05a6a93f') + e.message);
    }
  } finally {
    setModalProps({ confirmLoading: false });
  }
}
</script>
