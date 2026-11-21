<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="isUpdate ? t('appUi.k18e893d1') : t('appUi.ke14ec9fd')"
    width="1040px"
    @ok="handleSubmit"
  >
    <div class="p-2">
      <div class="flex flex-wrap justify-between items-center gap-2 mb-3 p-2.5 bg-gray-50 rounded border border-gray-200">
        <div class="flex items-center gap-3 flex-wrap">
          <span class="text-xs font-semibold text-gray-700">{{ t('appUi.ke94d1e75') }}</span>
          <a-radio-group v-model:value="formState.helpType" button-style="solid" size="small" @change="handleHelpTypeChange">
            <a-radio-button value="SCREEN">{{ t('appUi.ka3a0501d') }}</a-radio-button>
            <a-radio-button value="SYSTEM">{{ t('appUi.k368c6bad') }}</a-radio-button>
          </a-radio-group>
          <span class="text-xs text-gray-500">
            {{
              formState.helpType === 'SYSTEM'
                ? t('appUi.kac2792dc')
                : t('appUi.k764d4070')
            }}
          </span>
        </div>
        <a-radio-group v-model:value="editMode" button-style="solid" size="small">
          <a-radio-button value="builder">{{ t('appUi.kdb7864d1') }}</a-radio-button>
          <a-radio-button value="json">{{ t('appUi.kc22288e3') }}</a-radio-button>
        </a-radio-group>
      </div>

      <BasicForm @register="registerBaseForm" />

      <HelpBuilderTabs
        v-if="editMode === 'builder'"
        :form-state="formState"
        :help-type="formState.helpType"
      />
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
    HelpGuideType,
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
  import { parseHelpRecord, resolveHelpType } from '../help.data';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();

  const isUpdate = ref(false);
  const editMode = ref<'builder' | 'json'>('builder');
  const menuRouteOptions = ref<Array<{ label: string; value: string; title: string; category?: string }>>([]);

  const formState = reactive({
    id: undefined as string | undefined,
    appCode: DEFAULT_APP_CODE,
    helpType: 'SCREEN' as HelpGuideType,
    pageKey: '',
    routeName: '',
    title: '',
    category: t('appUi.kaef1a1e7'),
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
      label: t('appUi.k59b1513e'),
      component: 'Select',
      colProps: { span: 10 },
      ifShow: () => formState.helpType !== 'SYSTEM',
      componentProps: {
        showSearch: true,
        allowClear: true,
        placeholder: t('appUi.k7ffdebbb'),
        options: [],
        filterOption: filterMenuOption,
        onChange: (val: string) => handleMenuRouteSelect(val),
      },
    },
    {
      field: 'pageKey',
      label: t('appUi.kfa5e96d1'),
      component: 'Input',
      required: true,
      colProps: { span: 8 },
      componentProps: { placeholder: t('appUi.k37881802') },
    },
    {
      field: 'category',
      label: t('appUi.k8b1d74f0'),
      component: 'Input',
      required: true,
      colProps: { span: 6 },
      componentProps: { placeholder: t('appUi.k52f5d7a7') },
    },
    {
      field: 'title',
      label: t('appUi.k8cfe4107'),
      component: 'Input',
      required: true,
      colProps: { span: 10 },
      componentProps: { placeholder: t('appUi.kd4a743f5') },
    },
    {
      field: 'appCode',
      label: t('appUi.k33084c64'),
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      field: 'sortOrder',
      label: t('appUi.k11c67410'),
      component: 'InputNumber',
      colProps: { span: 4 },
      componentProps: { min: 0, style: { width: '100%' } },
    },
    {
      field: 'status',
      label: t('appUi.k2926977b'),
      component: 'Select',
      colProps: { span: 4 },
      componentProps: {
        options: [
          { label: t('appUi.k6a2123a3'), value: 'ACTIVE' },
          { label: t('appUi.kfae2014b'), value: 'INACTIVE' },
        ],
      },
    },
    {
      field: 'summary',
      label: t('appUi.k3d9cd24e'),
      component: 'Input',
      required: true,
      colProps: { span: 24 },
      componentProps: { placeholder: t('appUi.k3ead9978') },
    },
    {
      field: 'overview',
      label: t('appUi.ke026b4a7'),
      component: 'InputTextArea',
      required: true,
      colProps: { span: 24 },
      componentProps: { rows: 3, placeholder: t('appUi.k4e831f92') },
    },
    {
      field: 'aiPromptHint',
      label: t('appUi.k61e65e4f'),
      component: 'Input',
      colProps: { span: 24 },
      componentProps: { placeholder: t('appUi.k1d5583a0') },
    },
  ];

  const jsonFormSchemas: FormSchema[] = [
    {
      field: 'featuresJson',
      label: t('appUi.kef063204'),
      component: 'InputTextArea',
      colProps: { span: 12 },
      componentProps: { rows: 4, class: 'font-mono text-xs' },
    },
    {
      field: 'stepsJson',
      label: t('appUi.kfdd76612'),
      component: 'InputTextArea',
      colProps: { span: 12 },
      componentProps: { rows: 4, class: 'font-mono text-xs' },
    },
    {
      field: 'lineageJson',
      label: t('appUi.k93c4f9a2'),
      component: 'InputTextArea',
      colProps: { span: 8 },
      componentProps: { rows: 4, class: 'font-mono text-xs' },
    },
    {
      field: 'formulasJson',
      label: t('appUi.kaf209a8f'),
      component: 'InputTextArea',
      colProps: { span: 8 },
      componentProps: { rows: 4, class: 'font-mono text-xs' },
    },
    {
      field: 'relatedScreensJson',
      label: t('appUi.k8657cda3'),
      component: 'InputTextArea',
      colProps: { span: 8 },
      componentProps: { rows: 4, class: 'font-mono text-xs' },
    },
    {
      field: 'tipsJson',
      label: t('appUi.k1ee6d600'),
      component: 'InputTextArea',
      colProps: { span: 8 },
      componentProps: { rows: 4, class: 'font-mono text-xs' },
    },
    {
      field: 'warningsJson',
      label: t('appUi.k992e7b01'),
      component: 'InputTextArea',
      colProps: { span: 8 },
      componentProps: { rows: 4, class: 'font-mono text-xs' },
    },
    {
      field: 'faqJson',
      label: t('appUi.k47ee20c6'),
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

  function handleHelpTypeChange() {
    const current = getBaseFieldsValue();
    if (formState.helpType === 'SYSTEM') {
      const updates: Record<string, any> = {};
      if (!current.category || current.category === '일반') {
        updates.category = t('appUi.k368c6bad');
      }
      if (!current.pageKey) {
        updates.pageKey = '/overview';
      }
      if (Object.keys(updates).length > 0) {
        setBaseFieldsValue(updates);
      }
    }
  }

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
        placeholder: t('appUi.k7ffdebbb'),
        options: menuRouteOptions.value,
        filterOption: filterMenuOption,
        onChange: (val: string) => handleMenuRouteSelect(val),
      },
    });

    const record: Partial<SysPageHelpModel> = data?.record || {};
    const parsed = parseHelpRecord(record);

    formState.id = record.id;
    formState.appCode = record.appCode || DEFAULT_APP_CODE;
    formState.helpType = resolveHelpType(record);
    formState.pageKey = record.pageKey || '';
    formState.routeName = record.routeName || '';
    formState.title = record.title || '';
    formState.category = record.category || (formState.helpType === 'SYSTEM' ? t('appUi.k368c6bad') : t('appUi.kaef1a1e7'));
    formState.icon = record.icon || 'ant-design:question-circle-outlined';
    formState.summary = record.summary || '';
    formState.overview = record.overview || '';
    formState.aiPromptHint = record.aiPromptHint || '';
    formState.status = record.status || 'ACTIVE';
    formState.sortOrder = record.sortOrder ?? (formState.helpType === 'SYSTEM' ? 0 : 10);

    formState.features = parsed.features.length ? [...parsed.features] : [{ name: '', description: '', badge: t('appUi.kf5cc9c1e') }];
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
    const nextCategory = !current.category || current.category === '일반' ? found?.category || t('appUi.kaef1a1e7') : current.category;
    setBaseFieldsValue({
      pageKey: val,
      title: nextTitle,
      category: nextCategory,
    });
  }

  async function handleSubmit() {
    Object.assign(formState, getBaseFieldsValue());
    if (!String(formState.pageKey || '').trim() || !String(formState.title || '').trim() || !String(formState.summary || '').trim()) {
      createMessage.warning(t('appUi.kfc94f29f'));
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
        helpType: formState.helpType,
        pageKey: formState.pageKey.trim(),
        routeName: formState.routeName.trim() || undefined,
        title: formState.title.trim(),
        category: formState.category.trim() || (formState.helpType === 'SYSTEM' ? t('appUi.k368c6bad') : t('appUi.kaef1a1e7')),
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
        formulas: formState.helpType === 'SYSTEM' ? [] : formState.formulas.filter((i) => i.name.trim()),
        tips: formState.tips.map((s) => s.trim()).filter(Boolean),
        warnings: formState.warnings.map((s) => s.trim()).filter(Boolean),
        faq: formState.faq.filter((i) => i.q.trim()),
        featuresJson: rawJson.featuresJson,
        stepsJson: rawJson.stepsJson,
        relatedScreensJson: rawJson.relatedScreensJson,
        lineageJson: rawJson.lineageJson,
        formulasJson: formState.helpType === 'SYSTEM' ? '[]' : rawJson.formulasJson,
        tipsJson: rawJson.tipsJson,
        warningsJson: rawJson.warningsJson,
        faqJson: rawJson.faqJson,
      };

      await saveOrUpdateHelp(payload);
      createMessage.success(t('appUi.kc725c7d2'));
      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
