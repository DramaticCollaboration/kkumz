<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="isUpdate ? '프롬프트 템플릿 수정' : '신규 프롬프트 템플릿 등록'"
    width="980px"
    @ok="handleSubmit"
  >
    <div class="prompt-edit-container">
      <BasicForm layout="vertical" :showActionButtonGroup="false">
        <template #formHeader>
          <div class="w-full">
            <a-row :gutter="16">
              <a-col :span="9">
                <a-form-item label="프롬프트 코드" required>
                  <a-input
                    v-model:value="formState.promptCode"
                    :disabled="isUpdate"
                    placeholder="예: TODAYCARE_DAILY_SAFETY_BRIEFING"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="9">
                <a-form-item label="프롬프트 명칭" required>
                  <a-input
                    v-model:value="formState.promptName"
                    placeholder="예: 일간 안심 돌봄 브리핑 생성"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item label="분류" required>
                  <a-select v-model:value="formState.category" :options="CATEGORY_OPTIONS" />
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <a-col :span="9">
                <a-form-item label="호출 클래스 및 메서드">
                  <a-input
                    v-model:value="formState.callerClass"
                    placeholder="예: TodayCareSeniorCareAgent.generateDailyCareBriefing"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item label="지정 모델">
                  <a-input
                    v-model:value="formState.modelOverride"
                    placeholder="기본 라우팅 모델 사용 시 비워둠"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="4">
                <a-form-item label="생성 온도값">
                  <a-input-number
                    v-model:value="formState.temperature"
                    :min="0"
                    :max="2"
                    :step="0.05"
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="5">
                <a-form-item label="상태">
                  <a-select v-model:value="formState.status">
                    <a-select-option value="ACTIVE">활성</a-select-option>
                    <a-select-option value="INACTIVE">비활성</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item label="용도 및 운영 설명">
              <a-input
                v-model:value="formState.description"
                placeholder="프롬프트의 호출 시점, 비즈니스 역할 및 주의사항을 입력하세요."
              />
            </a-form-item>

            <a-form-item label="시스템 페르소나 및 지침" required>
              <a-textarea
                v-model:value="formState.systemPrompt"
                :rows="5"
                class="font-mono text-xs"
                placeholder="AI 에이전트의 역할, 제약 조건, 응답 어조를 정의하세요."
              />
            </a-form-item>

            <a-form-item label="사용자 입력 템플릿" required>
              <a-textarea
                v-model:value="formState.userPromptTemplate"
                :rows="6"
                class="font-mono text-xs"
                placeholder="{{variableName}} 형식의 바인딩 변수를 포함하여 사용자 프롬프트 본문을 작성하세요."
              />
            </a-form-item>

            <a-row :gutter="16">
              <a-col :span="14">
                <div class="flex justify-between items-center mb-2">
                  <span class="font-semibold text-xs text-gray-700">
                    바인딩 변수 스키마 ({{ formState.variables.length }})
                  </span>
                  <a-space>
                    <a-button size="small" @click="handleExtractVariables">
                      본문에서 변수 자동 추출
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
                      + 변수 추가
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
                      placeholder="변수키"
                      class="font-mono text-xs"
                      style="width: 26%"
                    />
                    <a-input
                      v-model:value="v.label"
                      placeholder="표시명"
                      style="width: 24%"
                    />
                    <a-input
                      v-model:value="v.sampleValue"
                      placeholder="샘플값"
                      style="flex: 1"
                    />
                    <a-checkbox v-model:checked="v.required">필수</a-checkbox>
                    <a-button danger type="text" size="small" @click="formState.variables.splice(idx, 1)">
                      삭제
                    </a-button>
                  </div>
                </div>
              </a-col>
              <a-col :span="10">
                <a-form-item label="출력 JSON 스키마 규격">
                  <a-textarea
                    v-model:value="formState.outputSchema"
                    :rows="5"
                    class="font-mono text-xs"
                    placeholder='JSON 응답 강제 시 예시 규격 입력 (예: {"key": "value"})'
                  />
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item v-if="isUpdate" label="버전 변경 사유" class="mt-2">
              <a-input
                v-model:value="formState.changeReason"
                placeholder="이번 프롬프트 수정 사유를 기록하면 변경 이력에 함께 저장됩니다."
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
      createMessage.success(`${addedCount}개의 바인딩 변수가 추출되었습니다.`);
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
        '프롬프트 코드, 명칭, 시스템 페르소나 지침, 사용자 입력 템플릿은 필수 입력 항목입니다.',
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
      createMessage.success('프롬프트 템플릿이 저장되고 런타임 캐시에 반영되었습니다.');
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
