<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="isUpdate ? '화면 도움말 가이드 수정' : '신규 화면 도움말 가이드 등록'"
    width="1020px"
    @ok="handleSubmit"
  >
    <div class="help-edit-container">
      <!-- 상단 편집 모드 전환 바 -->
      <div class="flex justify-between items-center mb-4 p-3 bg-gray-50 rounded border border-gray-200">
        <div class="text-xs text-gray-600">
          DB 등록 메뉴에서 화면 경로를 선택하거나, 도메인 공통 가이드 경로(예: <code>/overview</code>, <code>/api-guide</code>)를 직접 입력할 수 있습니다.
        </div>
        <a-radio-group v-model:value="editMode" button-style="solid" size="small">
          <a-radio-button value="builder">항목별 폼 빌더</a-radio-button>
          <a-radio-button value="json">JSON 원본 편집</a-radio-button>
        </a-radio-group>
      </div>

      <BasicForm layout="vertical" :showActionButtonGroup="false">
        <template #formHeader>
          <div class="w-full">
        <!-- 1. 기본 식별 정보 -->
        <a-row :gutter="16">
          <a-col :span="10">
            <a-form-item label="등록된 메뉴에서 화면 선택">
              <a-select
                v-model:value="selectedMenuRoute"
                show-search
                allow-clear
                placeholder="메뉴를 선택하면 경로와 화면명이 자동 입력됩니다"
                :options="menuRouteOptions"
                :filter-option="filterMenuOption"
                @change="handleMenuRouteSelect"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="라우트 경로" required>
              <a-input v-model:value="formState.pageKey" placeholder="예: /llm/models 또는 /overview" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="분류" required>
              <a-input v-model:value="formState.category" placeholder="예: 게이트웨이 관리" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="10">
            <a-form-item label="가이드 / 화면명" required>
              <a-input v-model:value="formState.title" placeholder="예: AI 모델 라우팅 관리" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="앱 코드">
              <a-input v-model:value="formState.appCode" />
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <a-form-item label="정렬 순서">
              <a-input-number v-model:value="formState.sortOrder" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <a-form-item label="상태">
              <a-select v-model:value="formState.status">
                <a-select-option value="ACTIVE">활성</a-select-option>
                <a-select-option value="INACTIVE">비활성</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="한 줄 핵심 요약" required>
          <a-input
            v-model:value="formState.summary"
            placeholder="화면 상단 배너에 표시될 핵심 역할 요약을 입력하세요."
          />
        </a-form-item>

        <a-form-item label="화면 상세 개요" required>
          <a-textarea
            v-model:value="formState.overview"
            :rows="3"
            placeholder="화면의 비즈니스 목적, 아키텍처 역할, 주요 사용 시나리오를 서술하세요."
          />
        </a-form-item>

        <!-- 2-A. 항목별 동적 폼 빌더 모드 -->
        <template v-if="editMode === 'builder'">
          <a-tabs v-model:activeKey="activeBuilderTab" type="card" size="small">
            <!-- 탭 1: 주요 기능 -->
            <a-tab-pane key="features" :tab="`주요 제공 기능 (${formState.features.length})`">
              <div class="builder-list">
                <div v-for="(item, idx) in formState.features" :key="idx" class="builder-row">
                  <a-input v-model:value="item.name" placeholder="기능명" style="width: 24%" />
                  <a-input v-model:value="item.badge" placeholder="배지" style="width: 16%" />
                  <a-input v-model:value="item.description" placeholder="상세 기능 설명" style="flex: 1" />
                  <a-button danger type="text" @click="formState.features.splice(idx, 1)">삭제</a-button>
                </div>
                <a-button type="dashed" block @click="formState.features.push({ name: '', description: '', badge: '' })">
                  + 주요 기능 항목 추가
                </a-button>
              </div>
            </a-tab-pane>

            <!-- 탭 2: 단계별 조작 절차 -->
            <a-tab-pane key="steps" :tab="`업무 수행 절차 (${formState.steps.length})`">
              <div class="builder-list">
                <div v-for="(step, idx) in formState.steps" :key="idx" class="builder-row">
                  <a-input v-model:value="step.title" placeholder="단계 제목" style="width: 32%" />
                  <a-input v-model:value="step.description" placeholder="단계별 상세 수행 방법" style="flex: 1" />
                  <a-button danger type="text" @click="formState.steps.splice(idx, 1)">삭제</a-button>
                </div>
                <a-button type="dashed" block @click="formState.steps.push({ title: '', description: '' })">
                  + 업무 수행 단계 추가
                </a-button>
              </div>
            </a-tab-pane>

            <!-- 탭 3: 데이터 흐름 및 공식 -->
            <a-tab-pane key="lineage" tab="데이터 계보 및 산출 공식">
              <div class="space-y-4">
                <div>
                  <div class="font-semibold text-xs text-gray-700 mb-2">데이터 입력 원천</div>
                  <div v-for="(src, idx) in formState.lineageSources" :key="'s-' + idx" class="builder-row mb-2">
                    <a-input v-model:value="src.origin" placeholder="원천 테이블/시스템" style="width: 28%" />
                    <a-input v-model:value="src.cycle" placeholder="수집/반영 주기" style="width: 20%" />
                    <a-input v-model:value="src.description" placeholder="원천 데이터 설명" style="flex: 1" />
                    <a-button danger type="text" @click="formState.lineageSources.splice(idx, 1)">삭제</a-button>
                  </div>
                  <a-button type="dashed" size="small" @click="formState.lineageSources.push({ origin: '', cycle: '실시간', description: '' })">
                    + 데이터 원천 추가
                  </a-button>
                </div>

                <div>
                  <div class="font-semibold text-xs text-gray-700 mb-2">데이터 반영 및 연계 대상</div>
                  <div v-for="(dst, idx) in formState.lineageDestinations" :key="'d-' + idx" class="builder-row mb-2">
                    <a-input v-model:value="dst.target" placeholder="반영 대상 시스템/모듈" style="width: 28%" />
                    <a-input v-model:value="dst.timing" placeholder="반영 시점" style="width: 20%" />
                    <a-input v-model:value="dst.description" placeholder="연계 처리 설명" style="flex: 1" />
                    <a-button danger type="text" @click="formState.lineageDestinations.splice(idx, 1)">삭제</a-button>
                  </div>
                  <a-button type="dashed" size="small" @click="formState.lineageDestinations.push({ target: '', timing: '즉시 반영', description: '' })">
                    + 연계 대상 추가
                  </a-button>
                </div>

                <div>
                  <div class="font-semibold text-xs text-gray-700 mb-2">핵심 지표 산출 공식</div>
                  <div v-for="(fm, idx) in formState.formulas" :key="'f-' + idx" class="builder-row mb-2">
                    <a-input v-model:value="fm.name" placeholder="지표/공식명" style="width: 25%" />
                    <a-input v-model:value="fm.formula" placeholder="산출 수식" style="width: 35%" />
                    <a-input v-model:value="fm.description" placeholder="설명" style="flex: 1" />
                    <a-button danger type="text" @click="formState.formulas.splice(idx, 1)">삭제</a-button>
                  </div>
                  <a-button type="dashed" size="small" @click="formState.formulas.push({ name: '', formula: '', description: '' })">
                    + 산출 공식 추가
                  </a-button>
                </div>
              </div>
            </a-tab-pane>

            <!-- 탭 4: 운영 팁, 주의사항, FAQ -->
            <a-tab-pane key="tips" tab="운영 팁 / 주의사항 / 자주 묻는 질문">
              <div class="space-y-4">
                <div>
                  <div class="font-semibold text-xs text-blue-700 mb-2">실무 운영 팁</div>
                  <div v-for="(_, idx) in formState.tips" :key="'t-' + idx" class="builder-row mb-2">
                    <a-input v-model:value="formState.tips[idx]" placeholder="운영 팁 문구 입력" style="flex: 1" />
                    <a-button danger type="text" @click="formState.tips.splice(idx, 1)">삭제</a-button>
                  </div>
                  <a-button type="dashed" size="small" @click="formState.tips.push('')">+ 운영 팁 추가</a-button>
                </div>

                <div>
                  <div class="font-semibold text-xs text-red-600 mb-2">업무 주의사항</div>
                  <div v-for="(_, idx) in formState.warnings" :key="'w-' + idx" class="builder-row mb-2">
                    <a-input v-model:value="formState.warnings[idx]" placeholder="주의사항 및 제약 조건 입력" style="flex: 1" />
                    <a-button danger type="text" @click="formState.warnings.splice(idx, 1)">삭제</a-button>
                  </div>
                  <a-button type="dashed" size="small" @click="formState.warnings.push('')">+ 주의사항 추가</a-button>
                </div>

                <div>
                  <div class="font-semibold text-xs text-gray-700 mb-2">자주 묻는 질문</div>
                  <div v-for="(fq, idx) in formState.faq" :key="'q-' + idx" class="builder-row mb-2">
                    <a-input v-model:value="fq.q" placeholder="질문 내용" style="width: 38%" />
                    <a-input v-model:value="fq.a" placeholder="답변 내용" style="flex: 1" />
                    <a-button danger type="text" @click="formState.faq.splice(idx, 1)">삭제</a-button>
                  </div>
                  <a-button type="dashed" size="small" @click="formState.faq.push({ q: '', a: '' })">+ 질문 항목 추가</a-button>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </template>

        <!-- 2-B. JSON 원본 편집 모드 -->
        <template v-else>
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="주요 제공 기능 JSON">
                <a-textarea v-model:value="rawJson.featuresJson" :rows="5" class="font-mono text-xs" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="업무 수행 절차 JSON">
                <a-textarea v-model:value="rawJson.stepsJson" :rows="5" class="font-mono text-xs" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="데이터 계보 JSON">
                <a-textarea v-model:value="rawJson.lineageJson" :rows="5" class="font-mono text-xs" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="산출 공식 JSON">
                <a-textarea v-model:value="rawJson.formulasJson" :rows="5" class="font-mono text-xs" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="실무 팁 JSON">
                <a-textarea v-model:value="rawJson.tipsJson" :rows="4" class="font-mono text-xs" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="주의사항 JSON">
                <a-textarea v-model:value="rawJson.warningsJson" :rows="4" class="font-mono text-xs" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="자주 묻는 질문 JSON">
                <a-textarea v-model:value="rawJson.faqJson" :rows="4" class="font-mono text-xs" />
              </a-form-item>
            </a-col>
          </a-row>
        </template>
          </div>
        </template>
      </BasicForm>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive, watch } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm } from '/@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    DEFAULT_APP_CODE,
    getRegisteredMenuRoutes,
    LineageDestinationItem,
    LineageSourceItem,
    MetricFormulaItem,
    PageFaqItem,
    PageFeatureItem,
    PageStepItem,
    saveOrUpdateHelp,
    SysPageHelpModel,
  } from '../help.api';
  import { parseHelpRecord } from '../help.data';

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();

  const isUpdate = ref(false);
  const editMode = ref<'builder' | 'json'>('builder');
  const activeBuilderTab = ref('features');
  const selectedMenuRoute = ref<string | undefined>(undefined);
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
    status: 'ACTIVE',
    sortOrder: 10,
    features: [] as PageFeatureItem[],
    steps: [] as PageStepItem[],
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
    lineageJson: '{"sources":[],"destinations":[]}',
    formulasJson: '[]',
    tipsJson: '[]',
    warningsJson: '[]',
    faqJson: '[]',
  });

  function syncBuilderToRawJson() {
    rawJson.featuresJson = JSON.stringify(formState.features.filter((i) => i.name.trim()), null, 2);
    rawJson.stepsJson = JSON.stringify(formState.steps.filter((i) => i.title.trim()), null, 2);
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
      lineageJson: rawJson.lineageJson,
      formulasJson: rawJson.formulasJson,
      tipsJson: rawJson.tipsJson,
      warningsJson: rawJson.warningsJson,
      faqJson: rawJson.faqJson,
    });
    formState.features = parsed.features;
    formState.steps = parsed.steps;
    formState.lineageSources = parsed.lineage.sources;
    formState.lineageDestinations = parsed.lineage.destinations;
    formState.formulas = parsed.formulas;
    formState.tips = parsed.tips;
    formState.warnings = parsed.warnings;
    formState.faq = parsed.faq;
  }

  watch(editMode, (newMode) => {
    if (newMode === 'json') {
      syncBuilderToRawJson();
    } else {
      syncRawJsonToBuilder();
    }
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    editMode.value = 'builder';
    activeBuilderTab.value = 'features';
    selectedMenuRoute.value = undefined;

    if (menuRouteOptions.value.length === 0) {
      menuRouteOptions.value = await getRegisteredMenuRoutes();
    }

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
    formState.status = record.status || 'ACTIVE';
    formState.sortOrder = record.sortOrder ?? 10;

    formState.features = parsed.features.length ? [...parsed.features] : [{ name: '', description: '', badge: '핵심' }];
    formState.steps = parsed.steps.length ? [...parsed.steps] : [{ title: '', description: '' }];
    formState.lineageSources = parsed.lineage.sources.length ? [...parsed.lineage.sources] : [];
    formState.lineageDestinations = parsed.lineage.destinations.length ? [...parsed.lineage.destinations] : [];
    formState.formulas = parsed.formulas.length ? [...parsed.formulas] : [];
    formState.tips = parsed.tips.length ? [...parsed.tips] : [];
    formState.warnings = parsed.warnings.length ? [...parsed.warnings] : [];
    formState.faq = parsed.faq.length ? [...parsed.faq] : [];

    syncBuilderToRawJson();
  });

  function filterMenuOption(input: string, option: any) {
    return (option?.label || '').toLowerCase().includes(input.toLowerCase());
  }

  function handleMenuRouteSelect(val: string) {
    if (!val) return;
    formState.pageKey = val;
    const found = menuRouteOptions.value.find((item) => item.value === val);
    if (found) {
      if (!formState.title) formState.title = found.title;
      if (!formState.category || formState.category === '일반') {
        formState.category = found.category || '일반';
      }
    }
  }

  async function handleSubmit() {
    if (!formState.pageKey.trim() || !formState.title.trim() || !formState.summary.trim()) {
      createMessage.warning('라우트 경로, 화면명, 한 줄 핵심 요약은 필수 입력 항목입니다.');
      return;
    }

    if (editMode.value === 'builder') {
      syncBuilderToRawJson();
    } else {
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
        status: formState.status,
        sortOrder: formState.sortOrder,
        features: formState.features.filter((i) => i.name.trim()),
        steps: formState.steps.filter((i) => i.title.trim()),
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

<style lang="less" scoped>
  .help-edit-container {
    padding: 4px 8px;
  }

  .builder-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding-top: 6px;
  }

  .builder-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }
</style>
