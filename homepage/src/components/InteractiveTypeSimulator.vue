<template>
  <section class="section" style="padding-top: 1.5rem; padding-bottom: 2.5rem">
    <div class="container">
      <div
        class="glass-card"
        :style="{
          border: `1px solid ${product.accentColor || '#38bdf8'}55`,
          background: 'linear-gradient(145deg, rgba(15, 23, 42, 0.92), rgba(30, 41, 59, 0.78))',
        }"
      >
        <div style="display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 1rem; margin-bottom: 1.25rem">
          <div>
            <span
              class="section-tag"
              :style="{ color: product.accentColor || '#38bdf8', marginBottom: '0.35rem', display: 'inline-block' }"
            >
              {{ isEn ? pack.showcaseBadgeEn : pack.showcaseBadgeKo }}
            </span>
            <h3 style="margin: 0; font-size: 1.35rem">
              {{ product.name }} — {{ isEn ? pack.showcaseTitleEn : product.subtitle }}
            </h3>
          </div>
          <span
            class="hero-impact-pill"
            :style="{ background: `${product.accentColor || '#0ea5e9'}22`, color: product.accentColor || '#38bdf8', border: `1px solid ${product.accentColor || '#38bdf8'}44` }"
          >
            {{ isEn ? activeScenario.metricBadgeEn : activeScenario.metricBadgeKo }}
          </span>
        </div>

        <!-- 시나리오 선택 탭 + 제품군 특화 파라미터 컨트롤 -->
        <div style="display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 0.75rem; margin-bottom: 1.25rem">
          <div style="display: flex; flex-wrap: wrap; gap: 0.5rem">
            <button
              v-for="(sc, idx) in pack.scenarios"
              :key="sc.key"
              type="button"
              class="btn btn-sm"
              :class="selectedIdx === idx ? 'btn-primary' : 'btn-outline'"
              @click="selectScenario(idx)"
            >
              {{ isEn ? sc.labelEn : sc.labelKo }}
            </button>
          </div>

          <div style="display: flex; align-items: center; gap: 0.75rem; font-size: 0.82rem; color: #cbd5e1">
            <span>{{ tr('시뮬레이션 부하 배율', 'Load Multiplier') }}: <strong>{{ loadFactor }}x</strong></span>
            <input
              v-model.number="loadFactor"
              type="range"
              min="1"
              max="10"
              style="width: 110px; accent-color: #38bdf8; cursor: pointer"
            />
            <button type="button" class="btn btn-outline btn-sm" @click="runLiveStep">
              <i class="fas fa-bolt" style="margin-right: 4px"></i>{{ tr('즉시 실행', 'Run Trace') }}
            </button>
          </div>
        </div>

        <!-- 3-Stage Interactive Pipeline Grid -->
        <div class="grid-3" style="gap: 1rem">
          <!-- Stage 1: Input / Trigger -->
          <div class="spec-cell" style="background: rgba(2, 6, 23, 0.65); border-radius: 10px; padding: 1rem">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem">
              <span class="spec-key" style="color: #94a3b8">01 · {{ isEn ? activeScenario.inputTitleEn : activeScenario.inputTitleKo }}</span>
              <span class="code-chip">{{ product.canonicalApiPrefix }}</span>
            </div>
            <pre style="margin: 0; font-size: 0.78rem; color: #e2e8f0; white-space: pre-wrap; font-family: monospace; line-height: 1.55">{{ activeScenario.inputPayload }}</pre>
          </div>

          <!-- Stage 2: Autonomous Engine & Guardrail -->
          <div class="spec-cell" style="background: rgba(2, 6, 23, 0.65); border-radius: 10px; padding: 1rem">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem">
              <span class="spec-key" style="color: #38bdf8">02 · {{ tr('에이전트 추론 & 가드레일 파이프라인', 'Agent Reasoning & Guardrail') }}</span>
              <span class="code-chip accent">Run #{{ executionCounter }}</span>
            </div>
            <p style="font-size: 0.86rem; color: #f1f5f9; line-height: 1.6; margin: 0 0 0.75rem 0">
              {{ isEn ? activeScenario.engineStageEn : activeScenario.engineStageKo }}
            </p>
            <div class="chip-list">
              <span v-for="tool in product.mcpTools.slice(0, 3)" :key="tool" class="code-chip accent">
                {{ tool }}
              </span>
              <span v-for="tb in product.dbTables.slice(0, 2)" :key="tb" class="code-chip data">
                {{ tb }}
              </span>
            </div>
          </div>

          <!-- Stage 3: Output / Verification -->
          <div class="spec-cell" style="background: rgba(2, 6, 23, 0.65); border-radius: 10px; padding: 1rem">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem">
              <span class="spec-key" style="color: #34d399">03 · {{ isEn ? activeScenario.outputTitleEn : activeScenario.outputTitleKo }}</span>
              <span class="code-chip data">{{ computedThroughput }} req/s</span>
            </div>
            <pre style="margin: 0; font-size: 0.78rem; color: #a7f3d0; white-space: pre-wrap; font-family: monospace; line-height: 1.55">{{ activeScenario.outputPayload }}</pre>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useHomepageLocale } from '@/composables/useHomepageLocale';
import { getProductEnhancementPack } from '@/data/productI18n';
import type { StandaloneProductData } from '@/types/homepage.types';

const props = defineProps<{
  product: StandaloneProductData;
}>();

const { isEn, tr } = useHomepageLocale();

const selectedIdx = ref(0);
const loadFactor = ref(4);
const executionCounter = ref(1);

const pack = computed(() =>
  getProductEnhancementPack(
    props.product.id,
    props.product.name,
    props.product.canonicalApiPrefix,
    props.product.mcpTools,
    props.product.dbTables
  )
);

const activeScenario = computed(() => pack.value.scenarios[selectedIdx.value] || pack.value.scenarios[0]);

const computedThroughput = computed(() => 120 * loadFactor.value);

function selectScenario(idx: number): void {
  selectedIdx.value = idx;
  executionCounter.value += 1;
}

function runLiveStep(): void {
  selectedIdx.value = (selectedIdx.value + 1) % pack.value.scenarios.length;
  executionCounter.value += 1;
}
</script>
