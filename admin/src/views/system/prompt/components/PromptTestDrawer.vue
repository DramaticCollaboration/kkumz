<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    title="프롬프트 변수 치환 및 실행 테스트"
    width="760px"
    showFooter
    okText="실시간 LLM 테스트 실행"
    @ok="handleRunTest"
  >
    <div class="space-y-4">
      <a-descriptions bordered size="small" :column="2">
        <a-descriptions-item label="프롬프트 코드">
          <span class="font-mono text-xs font-semibold">{{ currentRecord.promptCode }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="프롬프트 명칭">
          {{ currentRecord.promptName }}
        </a-descriptions-item>
        <a-descriptions-item label="호출 클래스" :span="2">
          <span class="font-mono text-xs">{{ currentRecord.callerClass || '-' }}</span>
        </a-descriptions-item>
      </a-descriptions>

      <div class="p-3 bg-gray-50 rounded border border-gray-200">
        <div class="flex justify-between items-center mb-2">
          <span class="font-semibold text-xs text-gray-700">테스트 변수 입력</span>
          <a-button size="small" @click="handlePreview">변수 치환 미리보기</a-button>
        </div>
        <div v-if="variableInputs.length === 0" class="text-xs text-gray-500">
          등록된 바인딩 변수가 없는 고정형 프롬프트입니다.
        </div>
        <a-row v-else :gutter="12">
          <a-col
            v-for="item in variableInputs"
            :key="item.name"
            :span="12"
            class="mb-2"
          >
            <div class="text-xs text-gray-600 mb-1">
              <span class="font-mono font-semibold">{{ item.name }}</span>
              <span v-if="item.label" class="ml-1 text-gray-500">({{ item.label }})</span>
            </div>
            <a-input
              v-model:value="variableValues[item.name]"
              :placeholder="item.sampleValue || '테스트 값 입력'"
              size="small"
            />
          </a-col>
        </a-row>
      </div>

      <div v-if="renderedSystem || renderedUser" class="space-y-3">
        <div>
          <div class="font-semibold text-xs text-gray-700 mb-1">렌더링된 시스템 프롬프트</div>
          <pre class="rendered-box">{{ renderedSystem }}</pre>
        </div>
        <div>
          <div class="font-semibold text-xs text-gray-700 mb-1">렌더링된 사용자 프롬프트</div>
          <pre class="rendered-box">{{ renderedUser }}</pre>
        </div>
      </div>

      <div v-if="testResult" class="space-y-2">
        <div class="flex justify-between items-center">
          <span class="font-semibold text-xs text-gray-700">LLM 실행 응답 결과</span>
          <a-space>
            <a-tag color="blue">{{ testResult.durationMs }} ms</a-tag>
            <a-tag :color="testResult.jsonValid ? 'success' : 'warning'">
              {{ testResult.validationMessage || (testResult.jsonValid ? '검증 통과' : '일반 텍스트') }}
            </a-tag>
          </a-space>
        </div>
        <pre class="response-box">{{ testResult.llmResponse }}</pre>
      </div>
    </div>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    previewPromptRender,
    testRunPrompt,
    SysPromptModel,
    SysPromptTestRunResult,
    SysPromptVariableItem,
  } from '../prompt.api';
  import { parseVariablesSchema } from '../prompt.data';

  const { createMessage } = useMessage();

  const currentRecord = ref<Partial<SysPromptModel>>({});
  const variableInputs = ref<SysPromptVariableItem[]>([]);
  const variableValues = reactive<Record<string, string>>({});
  const renderedSystem = ref('');
  const renderedUser = ref('');
  const testResult = ref<SysPromptTestRunResult | null>(null);

  const [registerDrawer, { setDrawerProps }] = useDrawerInner(async (data) => {
    setDrawerProps({ confirmLoading: false });
    const record: SysPromptModel = data?.record || {};
    currentRecord.value = record;
    renderedSystem.value = '';
    renderedUser.value = '';
    testResult.value = null;

    Object.keys(variableValues).forEach((k) => delete variableValues[k]);
    const vars = parseVariablesSchema(record.variablesSchema || record.variables);
    variableInputs.value = vars;
    vars.forEach((v) => {
      variableValues[v.name] = v.sampleValue || '';
    });

    await handlePreview();
  });

  async function handlePreview() {
    if (!currentRecord.value.promptCode) return;
    const res = await previewPromptRender({
      promptCode: currentRecord.value.promptCode,
      systemPrompt: currentRecord.value.systemPrompt,
      userPromptTemplate: currentRecord.value.userPromptTemplate,
      outputSchema: currentRecord.value.outputSchema,
      variables: { ...variableValues },
    });
    renderedSystem.value = res?.renderedSystemPrompt || '';
    renderedUser.value = res?.renderedUserPrompt || '';
  }

  async function handleRunTest() {
    if (!currentRecord.value.promptCode) return;
    try {
      setDrawerProps({ confirmLoading: true });
      const res = await testRunPrompt({
        promptCode: currentRecord.value.promptCode,
        systemPrompt: currentRecord.value.systemPrompt,
        userPromptTemplate: currentRecord.value.userPromptTemplate,
        outputSchema: currentRecord.value.outputSchema,
        variables: { ...variableValues },
      });
      testResult.value = res;
      if (res) {
        renderedSystem.value = res.renderedSystemPrompt || renderedSystem.value;
        renderedUser.value = res.renderedUserPrompt || renderedUser.value;
      }
      createMessage.success('프롬프트 실행 테스트가 완료되었습니다.');
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  .rendered-box {
    margin: 0;
    padding: 10px 12px;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 4px;
    font-size: 12px;
    line-height: 1.55;
    white-space: pre-wrap;
    word-break: break-word;
    max-height: 200px;
    overflow-y: auto;
  }

  .response-box {
    margin: 0;
    padding: 12px;
    background: #0f172a;
    color: #f8fafc;
    border-radius: 4px;
    font-size: 12px;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-word;
    max-height: 260px;
    overflow-y: auto;
  }
</style>
