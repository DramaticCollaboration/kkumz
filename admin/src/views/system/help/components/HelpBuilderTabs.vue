<template>
  <div class="pt-1">
    <div class="flex flex-wrap gap-1.5 mb-3 border-b border-slate-200 pb-2">
      <a-button
        v-for="tab in builderTabs"
        :key="tab.key"
        size="small"
        :type="activeTab === tab.key ? 'primary' : 'default'"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </a-button>
    </div>

    <!-- 탭 1: 주요 기능 -->
    <div v-if="activeTab === 'features'" class="space-y-2">
      <div
        v-for="(item, idx) in formState.features"
        :key="'f-' + idx"
        class="flex items-center gap-2"
      >
        <a-input v-model:value="item.name" placeholder="기능명" class="w-1/4" />
        <a-input v-model:value="item.badge" placeholder="배지" class="w-1/6" />
        <a-input v-model:value="item.description" placeholder="상세 기능 설명" class="flex-1" />
        <a-button danger type="text" @click="formState.features.splice(idx, 1)">삭제</a-button>
      </div>
      <a-button
        type="dashed"
        block
        @click="formState.features.push({ name: '', description: '', badge: '' })"
      >
        + 주요 기능 항목 추가
      </a-button>
    </div>

    <!-- 탭 2: 단계별 조작 절차 -->
    <div v-else-if="activeTab === 'steps'" class="space-y-2">
      <div
        v-for="(step, idx) in formState.steps"
        :key="'st-' + idx"
        class="flex items-center gap-2"
      >
        <a-input v-model:value="step.title" placeholder="단계 제목" class="w-1/3" />
        <a-input
          v-model:value="step.description"
          placeholder="단계별 상세 수행 방법"
          class="flex-1"
        />
        <a-button danger type="text" @click="formState.steps.splice(idx, 1)">삭제</a-button>
      </div>
      <a-button type="dashed" block @click="formState.steps.push({ title: '', description: '' })">
        + 업무 수행 단계 추가
      </a-button>
    </div>

    <!-- 탭 3: 연관 화면 및 데이터 계보 -->
    <div v-else-if="activeTab === 'lineage'" class="space-y-4">
      <div v-if="Array.isArray(formState.relatedScreens)">
        <div class="font-semibold text-xs text-gray-700 mb-2">연관 화면 및 후속 업무 이동</div>
        <div
          v-for="(rel, idx) in formState.relatedScreens"
          :key="'r-' + idx"
          class="flex items-center gap-2 mb-2"
        >
          <a-input v-model:value="rel.type" placeholder="유형 (예: 선행 화면)" class="w-1/5" />
          <a-input v-model:value="rel.title" placeholder="화면명" class="w-1/4" />
          <a-input v-model:value="rel.path" placeholder="라우트 경로" class="w-1/5" />
          <a-input v-model:value="rel.description" placeholder="연관 업무 설명" class="flex-1" />
          <a-button danger type="text" @click="formState.relatedScreens.splice(idx, 1)">
            삭제
          </a-button>
        </div>
        <a-button
          type="dashed"
          size="small"
          @click="
            formState.relatedScreens.push({
              type: '연관 화면',
              title: '',
              path: '',
              description: '',
            })
          "
        >
          + 연관 화면 추가
        </a-button>
      </div>

      <div>
        <div class="font-semibold text-xs text-gray-700 mb-2">데이터 입력 원천</div>
        <div
          v-for="(src, idx) in formState.lineageSources"
          :key="'s-' + idx"
          class="flex items-center gap-2 mb-2"
        >
          <a-input v-model:value="src.origin" placeholder="원천 테이블/시스템" class="w-1/4" />
          <a-input v-model:value="src.cycle" placeholder="수집/반영 주기" class="w-1/5" />
          <a-input v-model:value="src.description" placeholder="원천 데이터 설명" class="flex-1" />
          <a-button danger type="text" @click="formState.lineageSources.splice(idx, 1)">
            삭제
          </a-button>
        </div>
        <a-button
          type="dashed"
          size="small"
          @click="formState.lineageSources.push({ origin: '', cycle: '실시간', description: '' })"
        >
          + 데이터 원천 추가
        </a-button>
      </div>

      <div>
        <div class="font-semibold text-xs text-gray-700 mb-2">데이터 반영 및 연계 대상</div>
        <div
          v-for="(dst, idx) in formState.lineageDestinations"
          :key="'d-' + idx"
          class="flex items-center gap-2 mb-2"
        >
          <a-input v-model:value="dst.target" placeholder="반영 대상 시스템/모듈" class="w-1/4" />
          <a-input v-model:value="dst.timing" placeholder="반영 시점" class="w-1/5" />
          <a-input v-model:value="dst.description" placeholder="연계 처리 설명" class="flex-1" />
          <a-button danger type="text" @click="formState.lineageDestinations.splice(idx, 1)">
            삭제
          </a-button>
        </div>
        <a-button
          type="dashed"
          size="small"
          @click="
            formState.lineageDestinations.push({
              target: '',
              timing: '즉시 반영',
              description: '',
            })
          "
        >
          + 연계 대상 추가
        </a-button>
      </div>

      <div>
        <div class="font-semibold text-xs text-gray-700 mb-2">핵심 지표 산출 공식</div>
        <div
          v-for="(fm, idx) in formState.formulas"
          :key="'fm-' + idx"
          class="flex items-center gap-2 mb-2"
        >
          <a-input v-model:value="fm.name" placeholder="지표/공식명" class="w-1/4" />
          <a-input v-model:value="fm.formula" placeholder="산출 수식" class="w-1/3" />
          <a-input v-model:value="fm.description" placeholder="설명" class="flex-1" />
          <a-button danger type="text" @click="formState.formulas.splice(idx, 1)">삭제</a-button>
        </div>
        <a-button
          type="dashed"
          size="small"
          @click="formState.formulas.push({ name: '', formula: '', description: '' })"
        >
          + 산출 공식 추가
        </a-button>
      </div>
    </div>

    <!-- 탭 4: 운영 팁, 주의사항, FAQ -->
    <div v-else-if="activeTab === 'tips'" class="space-y-4">
      <div>
        <div class="font-semibold text-xs text-blue-700 mb-2">실무 운영 팁</div>
        <div
          v-for="(_, idx) in formState.tips"
          :key="'t-' + idx"
          class="flex items-center gap-2 mb-2"
        >
          <a-input v-model:value="formState.tips[idx]" placeholder="운영 팁 문구 입력" class="flex-1" />
          <a-button danger type="text" @click="formState.tips.splice(idx, 1)">삭제</a-button>
        </div>
        <a-button type="dashed" size="small" @click="formState.tips.push('')">
          + 운영 팁 추가
        </a-button>
      </div>

      <div>
        <div class="font-semibold text-xs text-red-600 mb-2">업무 주의사항</div>
        <div
          v-for="(_, idx) in formState.warnings"
          :key="'w-' + idx"
          class="flex items-center gap-2 mb-2"
        >
          <a-input
            v-model:value="formState.warnings[idx]"
            placeholder="주의사항 및 제약 조건 입력"
            class="flex-1"
          />
          <a-button danger type="text" @click="formState.warnings.splice(idx, 1)">삭제</a-button>
        </div>
        <a-button type="dashed" size="small" @click="formState.warnings.push('')">
          + 주의사항 추가
        </a-button>
      </div>

      <div>
        <div class="font-semibold text-xs text-gray-700 mb-2">자주 묻는 질문</div>
        <div
          v-for="(fq, idx) in formState.faq"
          :key="'q-' + idx"
          class="flex items-center gap-2 mb-2"
        >
          <a-input v-model:value="fq.q" placeholder="질문 내용" class="w-2/5" />
          <a-input v-model:value="fq.a" placeholder="답변 내용" class="flex-1" />
          <a-button danger type="text" @click="formState.faq.splice(idx, 1)">삭제</a-button>
        </div>
        <a-button type="dashed" size="small" @click="formState.faq.push({ q: '', a: '' })">
          + 질문 항목 추가
        </a-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';

  const props = defineProps<{
    formState: Record<string, any>;
  }>();

  const activeTab = ref<'features' | 'steps' | 'lineage' | 'tips'>('features');

  const builderTabs = computed(() => [
    { key: 'features' as const, label: `주요 제공 기능 (${props.formState.features?.length || 0})` },
    { key: 'steps' as const, label: `업무 수행 절차 (${props.formState.steps?.length || 0})` },
    { key: 'lineage' as const, label: '연관 화면 / 데이터 계보 / 공식' },
    { key: 'tips' as const, label: '운영 팁 / 주의사항 / 자주 묻는 질문' },
  ]);
</script>
