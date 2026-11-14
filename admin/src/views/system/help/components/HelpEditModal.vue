<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="isUpdate ? '화면 도움말 가이드 수정' : '신규 화면 도움말 가이드 등록'"
    width="1040px"
    @ok="handleSubmit"
  >
    <div class="p-2">
      <div class="flex justify-between items-center mb-3 p-2.5 bg-gray-50 rounded border border-gray-200">
        <div class="text-xs text-gray-600">
          DB 등록 메뉴에서 화면 경로를 선택하거나, 도메인 공통 가이드 경로(예: <code>/overview</code>, <code>/api-guide</code>)를 직접 입력할 수 있습니다.
        </div>
        <a-radio-group v-model:value="editMode" button-style="solid" size="small">
          <a-radio-button value="builder">항목별 폼 빌더</a-radio-button>
          <a-radio-button value="json">JSON 원본 편집</a-radio-button>
        </a-radio-group>
      </div>

      <BasicForm @register="registerBaseForm" />

      <HelpBuilderTabs v-if="editMode === 'builder'" :form-state="formState" />
      <BasicForm v-else @register="registerJsonForm" />
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive, watch, nextTick } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, FormSchema, useForm } from '/@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';
  import HelpBuilderTabs from './HelpBuilderTabs.vue';
  import {
    DEFAULT_APP_CODE,
    getRegisteredMenuRoutes,
    LineageDestinationItem,
    LineageSourceItem,
    MetricFormulaItem,
    PageFaqItem,
    PageFeatureItem,
    PageStepItem,
    RelatedScreenItem,
    saveOrUpdateHelp,
    SysPageHelpModel,
  } from '../help.api';
  import { parseHelpRecord } from '../help.data';

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();

  const isUpdate = ref(false);
  const editMode = ref<'builder' | 'json'>('builder');
  const menuRouteOptions = ref<Array<{ label: string; value: string; title: string; category?: string }>>([]);

  const formState = reactive({
    id: undefined as string | undefined,
    appCode: DEFAULT_APP_CODE,
    pageKey: '',
    routeName: '',
    title: '',
    category: '일반',
    icon: 'ant-design:question-circle-outlined',
    summary: '',
    overview: '',
    aiPromptHint: '',
    status: 'ACTIVE',
    sortOrder: 10,
    features: [] as PageFeatureItem[],
    steps: [] as PageStepItem[],
    relatedScreens: [] as RelatedScreenItem[],
    lineageSources: [] as LineageSourceItem[],
    lineageDestinations: [] as LineageDestinationItem[],
    formulas: [] as MetricFormulaItem[],
    tips: [] as string[],
    warnings: [] as string[],
    faq: [] as PageFaqItem[],
  });

  const rawJson = reactive({
    featuresJson: '[]',
    stepsJson: '[]',
    relatedScreensJson: '[]',
    lineageJson: '{"sources":[],"destinations":[]}',
    formulasJson: '[]',
    tipsJson: '[]',
    warningsJson: '[]',
    faqJson: '[]',
  });

  function filterMenuOption(input: string, option: any) {
    return (option?.label || '').toLowerCase().includes(input.toLowerCase());
  }

  const baseFormSchemas: FormSchema[] = [
    {
      field: 'selectedMenuRoute',
      label: '등록 메뉴 선택',
      component: 'Select',
      colProps: { span: 10 },
      componentProps: {
        showSearch: true,
        allowClear: true,
        placeholder: '메뉴 선택 시 경로와 화면명 자동 입력',
        options: [],
        filterOption: filterMenuOption,
        onChange: (val: string) => handleMenuRouteSelect(val),
      },
    },
    {
      field: 'pageKey',
      label: '라우트 경로',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: { placeholder: '예: /llm/models 또는 /overview' },
    },
    {
      field: 'category',
      label: '분류',
      component: 'Input',
      required: true,
      colProps: { span: 6 },
      componentProps: { placeholder: '예: 게이트웨이 관리' },
    },
    {
      field: 'title',
      label: '화면명',
      component: 'Input',
      required: true,
      colProps: { span: 10 },
      componentProps: { placeholder: '예: AI 모델 라우팅 관리' },
    },
    {
      field: 'appCode',
      label: '앱 코드',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      field: 'sortOrder',
      label: '정렬 순서',
      component: 'InputNumber',
      colProps: { span: 4 },
      componentProps: { min: 0, style: { width: '100%' } },
    },
    {
      field: 'status',
      label: '상태',
      component: 'Select',
      colProps: { span: 4 },
      componentProps: {
        options: [
          { label: '활성', value: 'ACTIVE' },
          { label: '비활성', value: 'INACTIVE' },
        ],
      },
    },
    {
      field: 'summary',
      label: '핵심 요약',
      component: 'Input',
      required: true,
      colProps: { span: 24 },
      componentProps: { placeholder: '화면 상단 배너에 표시될 핵심 역할 요약 입력' },
    },
    {
      field: 'overview',
      label: '상세 개요',
      component: 'InputTextArea',
      required: true,
      colProps: { span: 24 },
      componentProps: { rows: 3, placeholder: '화면의 비즈니스 목적, 아키텍처 역할, 주요 사용 시나리오 서술' },
    },
    {
      field: 'aiPromptHint',
      label: '추천 프롬프트',
      component: 'Input',
      colProps: { span: 24 },
      componentProps: { placeholder: '예: 이 화면에서 승인 대기 중인 항목을 요약하고 표준 처리 절차를 알려줘.' },
    },
  ];

  const jsonFormSchemas: FormSchema[] = [
    {
      field: 'featuresJson',
      label: '주요 제공 기능 JSON',
      component: 'InputTextArea',
      colProps: { span: 12 },
      componentProps: { rows: 4, class: 'font-mono text-xs' },
    },
    {
      field: 'stepsJson',
      label: '업무 수행 절차 JSON',
      component: 'InputTextArea',
      colProps: { span: 12 },
      componentProps: { rows: 4, class: 'font-mono text-xs' },
    },
    {
      field: 'lineageJson',
      label: '데이터 계보 JSON',
      component: 'InputTextArea',
      colProps: { span: 8 },
      componentProps: { rows: 4, class: 'font-mono text-xs' },
    },
    {
      field: 'formulasJson',
      label: '산출 공식 JSON',
      component: 'InputTextArea',
      colProps: { span: 8 },
      componentProps: { rows: 4, class: 'font-mono text-xs' },
    },
    {
      field: 'relatedScreensJson',
      label: '연관 화면 JSON',
      component: 'InputTextArea',
      colProps: { span: 8 },
      componentProps: { rows: 4, class: 'font-mono text-xs' },
    },
    {
      field: 'tipsJson',
      label: '실무 팁 JSON',
      component: 'InputTextArea',
      colProps: { span: 8 },
      componentProps: { rows: 4, class: 'font-mono text-xs' },
    },
    {
      field: 'warningsJson',
      label: '주의사항 JSON',
      component: 'InputTextArea',
      colProps: { span: 8 },
      componentProps: { rows: 4, class: 'font-mono text-xs' },
    },
    {
      field: 'faqJson',
      label: '자주 묻는 질문 JSON',
      component: 'InputTextArea',
      colProps: { span: 8 },
      componentProps: { rows: 4, class: 'font-mono text-xs' },
    },
  ];

  const [
    registerBaseForm,
    {
      setFieldsValue: setBaseFieldsValue,
      getFieldsValue: getBaseFieldsValue,
      updateSchema: updateBaseSchema,
    },
  ] = useForm({
    layout: 'vertical',
    schemas: baseFormSchemas,
    showActionButtonGroup: false,
    baseRowStyle: { gutter: 16 },
  });

  const [
    registerJsonForm,
    { setFieldsValue: setJsonFieldsValue, getFieldsValue: getJsonFieldsValue },
  ] = useForm({
    layout: 'vertical',
    schemas: jsonFormSchemas,
    showActionButtonGroup: false,
    baseRowStyle: { gutter: 16 },
  });

  function syncBuilderToRawJson() {
    rawJson.featuresJson = JSON.stringify(formState.features.filter((i) => i.name.trim()), null, 2);
    rawJson.stepsJson = JSON.stringify(formState.steps.filter((i) => i.title.trim()), null, 2);
    rawJson.relatedScreensJson = JSON.stringify(formState.relatedScreens.filter((i) => i.title.trim()), null, 2);
    rawJson.lineageJson = JSON.stringify(
      {
        sources: formState.lineageSources.filter((i) => i.origin.trim()),
        destinations: formState.lineageDestinations.filter((i) => i.target.trim()),
      },
      null,
      2,
    );
    rawJson.formulasJson = JSON.stringify(formState.formulas.filter((i) => i.name.trim()), null, 2);
    rawJson.tipsJson = JSON.stringify(formState.tips.map((s) => s.trim()).filter(Boolean), null, 2);
    rawJson.warningsJson = JSON.stringify(formState.warnings.map((s) => s.trim()).filter(Boolean), null, 2);
    rawJson.faqJson = JSON.stringify(formState.faq.filter((i) => i.q.trim()), null, 2);
  }

  function syncRawJsonToBuilder() {
    const parsed = parseHelpRecord({
      featuresJson: rawJson.featuresJson,
      stepsJson: rawJson.stepsJson,
      relatedScreensJson: rawJson.relatedScreensJson,
      lineageJson: rawJson.lineageJson,
      formulasJson: rawJson.formulasJson,
      tipsJson: rawJson.tipsJson,
      warningsJson: rawJson.warningsJson,
      faqJson: rawJson.faqJson,
    });
    formState.features = parsed.features;
    formState.steps = parsed.steps;
    formState.relatedScreens = parsed.relatedScreens;
    formState.lineageSources = parsed.lineage.sources;
    formState.lineageDestinations = parsed.lineage.destinations;
    formState.formulas = parsed.formulas;
    formState.tips = parsed.tips;
    formState.warnings = parsed.warnings;
    formState.faq = parsed.faq;
  }

  watch(editMode, async (newMode) => {
    if (newMode === 'json') {
      syncBuilderToRawJson();
      await nextTick();
      await setJsonFieldsValue({ ...rawJson });
    } else {
      Object.assign(rawJson, getJsonFieldsValue());
      syncRawJsonToBuilder();
    }
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    editMode.value = 'builder';

    if (menuRouteOptions.value.length === 0) {
      menuRouteOptions.value = await getRegisteredMenuRoutes();
    }
    await updateBaseSchema({
      field: 'selectedMenuRoute',
      componentProps: {
        showSearch: true,
        allowClear: true,
        placeholder: '메뉴 선택 시 경로와 화면명 자동 입력',
        options: menuRouteOptions.value,
        filterOption: filterMenuOption,
        onChange: (val: string) => handleMenuRouteSelect(val),
      },
    });

    const record: Partial<SysPageHelpModel> = data?.record || {};
    const parsed = parseHelpRecord(record);

    formState.id = record.id;
    formState.appCode = record.appCode || DEFAULT_APP_CODE;
    formState.pageKey = record.pageKey || '';
    formState.routeName = record.routeName || '';
    formState.title = record.title || '';
    formState.category = record.category || '일반';
    formState.icon = record.icon || 'ant-design:question-circle-outlined';
    formState.summary = record.summary || '';
    formState.overview = record.overview || '';
    formState.aiPromptHint = record.aiPromptHint || '';
    formState.status = record.status || 'ACTIVE';
    formState.sortOrder = record.sortOrder ?? 10;

    formState.features = parsed.features.length ? [...parsed.features] : [{ name: '', description: '', badge: '핵심' }];
    formState.steps = parsed.steps.length ? [...parsed.steps] : [{ title: '', description: '' }];
    formState.relatedScreens = parsed.relatedScreens.length ? [...parsed.relatedScreens] : [];
    formState.lineageSources = parsed.lineage.sources.length ? [...parsed.lineage.sources] : [];
    formState.lineageDestinations = parsed.lineage.destinations.length ? [...parsed.lineage.destinations] : [];
    formState.formulas = parsed.formulas.length ? [...parsed.formulas] : [];
    formState.tips = parsed.tips.length ? [...parsed.tips] : [];
    formState.warnings = parsed.warnings.length ? [...parsed.warnings] : [];
    formState.faq = parsed.faq.length ? [...parsed.faq] : [];

    await setBaseFieldsValue({
      selectedMenuRoute: undefined,
      pageKey: formState.pageKey,
      category: formState.category,
      title: formState.title,
      appCode: formState.appCode,
      sortOrder: formState.sortOrder,
      status: formState.status,
      summary: formState.summary,
      overview: formState.overview,
      aiPromptHint: formState.aiPromptHint,
    });

    syncBuilderToRawJson();
  });

  function handleMenuRouteSelect(val: string) {
    if (!val) return;
    const current = getBaseFieldsValue();
    const found = menuRouteOptions.value.find((item) => item.value === val);
    const nextTitle = current.title || found?.title || '';
    const nextCategory = !current.category || current.category === '일반' ? found?.category || '일반' : current.category;
    setBaseFieldsValue({
      pageKey: val,
      title: nextTitle,
      category: nextCategory,
    });
  }

  async function handleSubmit() {
    Object.assign(formState, getBaseFieldsValue());
    if (!String(formState.pageKey || '').trim() || !String(formState.title || '').trim() || !String(formState.summary || '').trim()) {
      createMessage.warning('라우트 경로, 화면명, 핵심 요약은 필수 입력 항목입니다.');
      return;
    }

    if (editMode.value === 'builder') {
      syncBuilderToRawJson();
    } else {
      Object.assign(rawJson, getJsonFieldsValue());
      syncRawJsonToBuilder();
    }

    try {
      setModalProps({ confirmLoading: true });
      const payload: SysPageHelpModel = {
        id: formState.id,
        appCode: formState.appCode || DEFAULT_APP_CODE,
        pageKey: formState.pageKey.trim(),
        routeName: formState.routeName.trim() || undefined,
        title: formState.title.trim(),
        category: formState.category.trim() || '일반',
        icon: formState.icon,
        summary: formState.summary.trim(),
        overview: formState.overview.trim() || formState.summary.trim(),
        aiPromptHint: formState.aiPromptHint?.trim() || undefined,
        status: formState.status,
        sortOrder: formState.sortOrder,
        features: formState.features.filter((i) => i.name.trim()),
        steps: formState.steps.filter((i) => i.title.trim()),
        relatedScreens: formState.relatedScreens.filter((i) => i.title.trim()),
        lineage: {
          sources: formState.lineageSources.filter((i) => i.origin.trim()),
          destinations: formState.lineageDestinations.filter((i) => i.target.trim()),
        },
        formulas: formState.formulas.filter((i) => i.name.trim()),
        tips: formState.tips.map((s) => s.trim()).filter(Boolean),
        warnings: formState.warnings.map((s) => s.trim()).filter(Boolean),
        faq: formState.faq.filter((i) => i.q.trim()),
        featuresJson: rawJson.featuresJson,
        stepsJson: rawJson.stepsJson,
        relatedScreensJson: rawJson.relatedScreensJson,
        lineageJson: rawJson.lineageJson,
        formulasJson: rawJson.formulasJson,
        tipsJson: rawJson.tipsJson,
        warningsJson: rawJson.warningsJson,
        faqJson: rawJson.faqJson,
      };

      await saveOrUpdateHelp(payload);
      createMessage.success('화면 도움말 가이드가 저장되었습니다.');
      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
