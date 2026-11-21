<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="t('appUi.k9716062a')"
    width="760px"
    showFooter
    :okText="t('appUi.k615f1816')"
    @ok="handleRunTest"
  >
    <div class="space-y-4">
      <Description
        :schema="promptDescSchema"
        :data="currentRecord"
        :column="{ xs: 1, sm: 1, md: 2 }"
        size="small"
        bordered
      />

      <div class="p-3 bg-gray-50 rounded border border-gray-200">
        <div class="flex justify-between items-center mb-2">
          <span class="font-semibold text-xs text-gray-700">{{ t('appUi.kb6c68b27') }}</span>
          <a-button size="small" @click="handlePreview">{{ t('appUi.kb99869c7') }}</a-button>
        </div>
        <div v-if="variableInputs.length === 0" class="text-xs text-gray-500">
          {{ t('appUi.k182498dc') }}
        </div>
        <a-row v-else :gutter="12">
          <a-col
            v-for="item in variableInputs"
            :key="item.name"
            :xs="24" :md="12"
            class="mb-2"
          >
            <div class="text-xs text-gray-600 mb-1">
              <span class="font-mono font-semibold">{{ item.name }}</span>
              <span v-if="item.label" class="ml-1 text-gray-500">({{ item.label }})</span>
            </div>
            <a-input
              v-model:value="variableValues[item.name]"
              :placeholder="item.sampleValue || t('appUi.k4f4fcb58')"
              size="small"
            />
          </a-col>
        </a-row>
      </div>

      <div v-if="renderedSystem || renderedUser" class="space-y-3">
        <div>
          <div class="font-semibold text-xs text-gray-700 mb-1">{{ t('appUi.k5c6f2407') }}</div>
          <pre class="rendered-box">{{ renderedSystem }}</pre>
        </div>
        <div>
          <div class="font-semibold text-xs text-gray-700 mb-1">{{ t('appUi.k222e5cf6') }}</div>
          <pre class="rendered-box">{{ renderedUser }}</pre>
        </div>
      </div>

      <div v-if="testResult" class="space-y-2">
        <div class="flex justify-between items-center">
          <span class="font-semibold text-xs text-gray-700">{{ t('appUi.k21826f66') }}</span>
          <a-space>
            <a-tag color="blue">{{ testResult.durationMs }} ms</a-tag>
            <a-tag :color="testResult.jsonValid ? 'success' : 'warning'">
              {{ testResult.validationMessage || (testResult.jsonValid ? t('appUi.kbe5c9041') : t('appUi.k87c554df')) }}
            </a-tag>
          </a-space>
        </div>
        <pre class="response-box">{{ testResult.llmResponse }}</pre>
      </div>
    </div>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { h, ref, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { Description, DescItem } from '/@/components/Description';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    previewPromptRender,
    testRunPrompt,
    SysPromptModel,
    SysPromptTestRunResult,
    SysPromptVariableItem,
  } from '../prompt.api';
  import { parseVariablesSchema } from '../prompt.data';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const { createMessage } = useMessage();

  const currentRecord = ref<Partial<SysPromptModel>>({});
  const variableInputs = ref<SysPromptVariableItem[]>([]);
  const variableValues = reactive<Record<string, string>>({});
  const renderedSystem = ref('');
  const renderedUser = ref('');
  const testResult = ref<SysPromptTestRunResult | null>(null);

  const promptDescSchema: DescItem[] = [
    {
      field: 'promptCode',
      label: t('appUi.k5bef10a7'),
      render: (val) => h('span', { class: 'font-mono text-xs font-semibold' }, val || ''),
    },
    {
      field: 'promptName',
      label: t('appUi.kef80204d'),
    },
    {
      field: 'callerClass',
      label: t('appUi.k97b2edea'),
      span: 2,
      render: (val) => h('span', { class: 'font-mono text-xs' }, val || '-'),
    },
  ];

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
      createMessage.success(t('appUi.k7444c855'));
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
