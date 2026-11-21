<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="isUpdate ? t('appUi.ka07ab67e') : t('appUi.k3257be71')"
    width="980px"
    @ok="handleSubmit"
  >
    <div class="prompt-edit-container">
      <BasicForm layout="vertical" :showActionButtonGroup="false">
        <template #formHeader>
          <div class="w-full">
            <a-row :gutter="16">
              <a-col :xs="24" :md="9">
                <a-form-item :label="t('appUi.k5bef10a7')" required>
                  <a-input
                    v-model:value="formState.promptCode"
                    :disabled="isUpdate"
                    :placeholder="t('appUi.kd91342a8')"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="9">
                <a-form-item :label="t('appUi.kef80204d')" required>
                  <a-input
                    v-model:value="formState.promptName"
                    :placeholder="t('appUi.k8b7d4af0')"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="6">
                <a-form-item :label="t('appUi.k8b1d74f0')" required>
                  <a-select v-model:value="formState.category" :options="CATEGORY_OPTIONS" />
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <a-col :xs="24" :md="9">
                <a-form-item :label="t('appUi.kb52cee49')">
                  <a-input
                    v-model:value="formState.callerClass"
                    :placeholder="t('appUi.k416a5633')"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="6">
                <a-form-item :label="t('appUi.kfcd1b545')">
                  <a-input
                    v-model:value="formState.modelOverride"
                    :placeholder="t('appUi.kbeeb044a')"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="4">
                <a-form-item :label="t('appUi.k2f90eb14')">
                  <a-input-number
                    v-model:value="formState.temperature"
                    :min="0"
                    :max="2"
                    :step="0.05"
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="5">
                <a-form-item :label="t('appUi.k2926977b')">
                  <a-select v-model:value="formState.status">
                    <a-select-option value="ACTIVE">{{ t('appUi.k6a2123a3') }}</a-select-option>
                    <a-select-option value="INACTIVE">{{ t('appUi.kfae2014b') }}</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item :label="t('appUi.k3e794577')">
              <a-input
                v-model:value="formState.description"
                :placeholder="t('appUi.k14764bee')"
              />
            </a-form-item>

            <a-form-item :label="t('appUi.k58301d13')" required>
              <a-textarea
                v-model:value="formState.systemPrompt"
                :rows="5"
                class="font-mono text-xs"
                :placeholder="t('appUi.ke9f35cec')"
              />
            </a-form-item>

            <a-form-item :label="t('appUi.kea6cdc93')" required>
              <a-textarea
                v-model:value="formState.userPromptTemplate"
                :rows="6"
                class="font-mono text-xs"
                :placeholder="t('appUi.k8543df8b')"
              />
            </a-form-item>

            <a-row :gutter="16">
              <a-col :xs="24" :md="14">
                <div class="flex justify-between items-center mb-2">
                  <span class="font-semibold text-xs text-gray-700">
                    {{ t('appUi.k282d17c8') }}{{ formState.variables.length }})
                  </span>
                  <a-space>
                    <a-button size="small" @click="handleExtractVariables">
                      {{ t('appUi.k5f019c74') }}
                    </a-button>
                    <a-button
                      size="small"
                      type="dashed"
                      @click="
                        formState.variables.push({
                          name: '',
                          label: '',
                          required: true,
                          sampleValue: '',
                        })
                      "
                    >
                      {{ t('appUi.k7fff032a') }}
                    </a-button>
                  </a-space>
                </div>
                <div class="variable-list">
                  <div
                    v-for="(v, idx) in formState.variables"
                    :key="idx"
                    class="variable-row"
                  >
                    <a-input
                      v-model:value="v.name"
                      :placeholder="t('appUi.k9fbc906f')"
                      class="font-mono text-xs"
                      style="width: 26%"
                    />
                    <a-input
                      v-model:value="v.label"
                      :placeholder="t('appUi.kd382d62a')"
                      style="width: 24%"
                    />
                    <a-input
                      v-model:value="v.sampleValue"
                      :placeholder="t('appUi.k0b99e62e')"
                      style="flex: 1"
                    />
                    <a-checkbox v-model:checked="v.required">{{ t('appUi.k5b4a45c2') }}</a-checkbox>
                    <a-button danger type="text" size="small" @click="formState.variables.splice(idx, 1)">
                      {{ t('appUi.kfc81e222') }}
                    </a-button>
                  </div>
                </div>
              </a-col>
              <a-col :xs="24" :md="10">
                <a-form-item :label="t('appUi.k05e3e2b3')">
                  <a-textarea
                    v-model:value="formState.outputSchema"
                    :rows="5"
                    class="font-mono text-xs"
                    :placeholder="t('appUi.k5a1d8d54')"
                  />
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item v-if="isUpdate" :label="t('appUi.kb4f8f196')" class="mt-2">
              <a-input
                v-model:value="formState.changeReason"
                :placeholder="t('appUi.k4f2f2264')"
              />
            </a-form-item>
          </div>
        </template>
      </BasicForm>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm } from '/@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    DEFAULT_APP_CODE,
    saveOrUpdatePrompt,
    SysPromptModel,
    SysPromptVariableItem,
  } from '../prompt.api';
  import { CATEGORY_OPTIONS, parseVariablesSchema } from '../prompt.data';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();

  const isUpdate = ref(false);

  const formState = reactive({
    id: undefined as string | undefined,
    appCode: DEFAULT_APP_CODE,
    promptCode: '',
    promptName: '',
    category: 'AGENT',
    callerClass: '',
    systemPrompt: '',
    userPromptTemplate: '',
    outputSchema: '',
    modelOverride: '',
    temperature: 0.2,
    status: 'ACTIVE',
    description: '',
    changeReason: '',
    variables: [] as SysPromptVariableItem[],
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    const record: Partial<SysPromptModel> = data?.record || {};

    formState.id = record.id;
    formState.appCode = record.appCode || DEFAULT_APP_CODE;
    formState.promptCode = record.promptCode || '';
    formState.promptName = record.promptName || '';
    formState.category = record.category || 'AGENT';
    formState.callerClass = record.callerClass || '';
    formState.systemPrompt = record.systemPrompt || '';
    formState.userPromptTemplate = record.userPromptTemplate || '';
    formState.outputSchema = record.outputSchema || '';
    formState.modelOverride = record.modelOverride || '';
    formState.temperature = record.temperature ?? 0.2;
    formState.status = record.status || 'ACTIVE';
    formState.description = record.description || '';
    formState.changeReason = '';
    formState.variables = parseVariablesSchema(record.variablesSchema || record.variables);
  });

  function handleExtractVariables() {
    const combined = `${formState.systemPrompt}\n${formState.userPromptTemplate}`;
    const regex = /\{\{\s*([a-zA-Z0-9_.-]+)\s*\}\}/g;
    const existingNames = new Set(formState.variables.map((v) => v.name));
    let match: RegExpExecArray | null;
    let addedCount = 0;
    while ((match = regex.exec(combined)) !== null) {
      const varName = match[1];
      if (varName && !existingNames.has(varName)) {
        existingNames.add(varName);
        formState.variables.push({
          name: varName,
          label: varName,
          required: true,
          sampleValue: '',
        });
        addedCount++;
      }
    }
    if (addedCount > 0) {
      createMessage.success(t('appUi.k04a09241', { p0: addedCount }));
    } else {
      createMessage.info('새로 추가할 변수가 없습니다.');
    }
  }

  async function handleSubmit() {
    if (
      !formState.promptCode.trim() ||
      !formState.promptName.trim() ||
      !formState.systemPrompt.trim() ||
      !formState.userPromptTemplate.trim()
    ) {
      createMessage.warning(
        t('appUi.kee2f9efd'),
      );
      return;
    }

    try {
      setModalProps({ confirmLoading: true });
      const cleanVars = formState.variables.filter((v) => v.name && v.name.trim());
      const payload: SysPromptModel = {
        id: formState.id,
        appCode: formState.appCode || DEFAULT_APP_CODE,
        promptCode: formState.promptCode.trim(),
        promptName: formState.promptName.trim(),
        category: formState.category,
        callerClass: formState.callerClass.trim() || undefined,
        systemPrompt: formState.systemPrompt,
        userPromptTemplate: formState.userPromptTemplate,
        variablesSchema: JSON.stringify(cleanVars),
        variables: cleanVars,
        outputSchema: formState.outputSchema.trim() || undefined,
        modelOverride: formState.modelOverride.trim() || undefined,
        temperature: formState.temperature,
        status: formState.status,
        description: formState.description.trim() || undefined,
        changeReason: formState.changeReason.trim() || undefined,
      };

      await saveOrUpdatePrompt(payload);
      createMessage.success(t('appUi.k9bfc6b5b'));
      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  .prompt-edit-container {
    padding: 4px 8px;
  }

  .variable-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    max-height: 165px;
    overflow-y: auto;
    padding: 8px;
    border: 1px solid #e5e7eb;
    border-radius: 4px;
    background: #fafafa;
  }

  .variable-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }
</style>
