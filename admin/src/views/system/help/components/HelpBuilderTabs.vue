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

    <div v-if="activeTab === 'features'" class="space-y-2">
      <div
        v-for="(item, idx) in formState.features"
        :key="'f-' + idx"
        class="flex items-center gap-2"
      >
        <a-input
          v-model:value="item.name"
          :placeholder="isSystem ? t('appUi.k9e4dc8a3') : t('appUi.k46ea34d7')"
          class="w-1/4"
        />
        <a-input v-model:value="item.badge" :placeholder="t('appUi.ka2a9b53f')" class="w-1/6" />
        <a-input
          v-model:value="item.description"
          :placeholder="isSystem ? t('appUi.k3df5b001') : t('appUi.kd5ca775f')"
          class="flex-1"
        />
        <a-button danger type="text" @click="formState.features.splice(idx, 1)">{{ t('appUi.kfc81e222') }}</a-button>
      </div>
      <a-button
        type="dashed"
        block
        @click="formState.features.push({ name: '', description: '', badge: '' })"
      >
        {{ isSystem ? t('appUi.k45eb9a31') : t('appUi.kb001b610') }}
      </a-button>
    </div>

    <div v-else-if="activeTab === 'steps'" class="space-y-2">
      <div
        v-for="(step, idx) in formState.steps"
        :key="'st-' + idx"
        class="flex items-center gap-2"
      >
        <a-input
          v-model:value="step.title"
          :placeholder="isSystem ? t('appUi.kc3520add') : t('appUi.k90f34a7f')"
          class="w-1/4"
        />
        <a-input
          v-model:value="step.description"
          :placeholder="isSystem ? t('appUi.kdd8c0936') : t('appUi.k71c3a901')"
          class="flex-1"
        />
        <a-input
          v-if="isSystem"
          v-model:value="step.path"
          :placeholder="t('appUi.k5635a701')"
          class="w-1/5"
        />
        <a-button danger type="text" @click="formState.steps.splice(idx, 1)">{{ t('appUi.kfc81e222') }}</a-button>
      </div>
      <a-button
        type="dashed"
        block
        @click="formState.steps.push(isSystem ? { title: '', description: '', path: '' } : { title: '', description: '' })"
      >
        {{ isSystem ? t('appUi.k4934772b') : t('appUi.k2a4ed7dd') }}
      </a-button>
    </div>

    <div v-else-if="activeTab === 'lineage'" class="space-y-4">
      <div v-if="Array.isArray(formState.relatedScreens)">
        <div class="font-semibold text-xs text-gray-700 mb-2">
          {{ isSystem ? t('appUi.kbec9633e') : t('appUi.k6e6d4fc9') }}
        </div>
        <div
          v-for="(rel, idx) in formState.relatedScreens"
          :key="'r-' + idx"
          class="flex items-center gap-2 mb-2"
        >
          <a-input
            v-model:value="rel.type"
            :placeholder="isSystem ? t('appUi.kad6d8397') : t('appUi.ke7ed13fc')"
            class="w-1/5"
          />
          <a-input v-model:value="rel.title" :placeholder="t('appUi.k7c5c420f')" class="w-1/4" />
          <a-input v-model:value="rel.path" :placeholder="t('appUi.k5227cc3c')" class="w-1/5" />
          <a-input
            v-model:value="rel.description"
            :placeholder="isSystem ? t('appUi.kd67664e6') : t('appUi.k4dd57d87')"
            class="flex-1"
          />
          <a-button danger type="text" @click="formState.relatedScreens.splice(idx, 1)">
            {{ t('appUi.kfc81e222') }}
          </a-button>
        </div>
        <a-button
          type="dashed"
          size="small"
          @click="
            formState.relatedScreens.push({
              type: isSystem ? t('appUi.ka1037c16') : t('appUi.k6c837656'),
              title: '',
              path: '',
              description: '',
            })
          "
        >
          {{ isSystem ? t('appUi.k79845a42') : t('appUi.kc9dccb5b') }}
        </a-button>
      </div>

      <div>
        <div class="font-semibold text-xs text-gray-700 mb-2">
          {{ isSystem ? t('appUi.kd58536eb') : t('appUi.k6c418325') }}
        </div>
        <div
          v-for="(src, idx) in formState.lineageSources"
          :key="'s-' + idx"
          class="flex items-center gap-2 mb-2"
        >
          <a-input v-model:value="src.origin" :placeholder="t('appUi.k1868cee0')" class="w-1/4" />
          <a-input v-model:value="src.cycle" :placeholder="t('appUi.kb74816d8')" class="w-1/5" />
          <a-input v-model:value="src.description" :placeholder="t('appUi.k4c074603')" class="flex-1" />
          <a-button danger type="text" @click="formState.lineageSources.splice(idx, 1)">
            {{ t('appUi.kfc81e222') }}
          </a-button>
        </div>
        <a-button
          type="dashed"
          size="small"
          @click="formState.lineageSources.push({ origin: '', cycle: t('appUi.kc2712f0f'), description: '' })"
        >
          {{ t('appUi.kb1dcec43') }}
        </a-button>
      </div>

      <div>
        <div class="font-semibold text-xs text-gray-700 mb-2">
          {{ isSystem ? t('appUi.k48d522ce') : t('appUi.k5d1583ee') }}
        </div>
        <div
          v-for="(dst, idx) in formState.lineageDestinations"
          :key="'d-' + idx"
          class="flex items-center gap-2 mb-2"
        >
          <a-input v-model:value="dst.target" :placeholder="t('appUi.k33bd1b0a')" class="w-1/4" />
          <a-input v-model:value="dst.timing" :placeholder="t('appUi.k0cd07c0c')" class="w-1/5" />
          <a-input v-model:value="dst.description" :placeholder="t('appUi.k6f40aeae')" class="flex-1" />
          <a-button danger type="text" @click="formState.lineageDestinations.splice(idx, 1)">
            {{ t('appUi.kfc81e222') }}
          </a-button>
        </div>
        <a-button
          type="dashed"
          size="small"
          @click="
            formState.lineageDestinations.push({
              target: '',
              timing: t('appUi.k8afd2698'),
              description: '',
            })
          "
        >
          {{ t('appUi.k8e0e778b') }}
        </a-button>
      </div>

      <div v-if="!isSystem">
        <div class="font-semibold text-xs text-gray-700 mb-2">{{ t('appUi.k99b9eaa8') }}</div>
        <div
          v-for="(fm, idx) in formState.formulas"
          :key="'fm-' + idx"
          class="flex items-center gap-2 mb-2"
        >
          <a-input v-model:value="fm.name" :placeholder="t('appUi.k3c1f5d85')" class="w-1/4" />
          <a-input v-model:value="fm.formula" :placeholder="t('appUi.kd4a441e5')" class="w-1/3" />
          <a-input v-model:value="fm.description" :placeholder="t('appUi.k84196436')" class="flex-1" />
          <a-button danger type="text" @click="formState.formulas.splice(idx, 1)">{{ t('appUi.kfc81e222') }}</a-button>
        </div>
        <a-button
          type="dashed"
          size="small"
          @click="formState.formulas.push({ name: '', formula: '', description: '' })"
        >
          {{ t('appUi.ke3d92bb1') }}
        </a-button>
      </div>
    </div>

    <div v-else-if="activeTab === 'tips'" class="space-y-4">
      <div>
        <div class="font-semibold text-xs text-blue-700 mb-2">
          {{ isSystem ? t('appUi.k610a0246') : t('appUi.k44876234') }}
        </div>
        <div
          v-for="(_, idx) in formState.tips"
          :key="'t-' + idx"
          class="flex items-center gap-2 mb-2"
        >
          <a-input v-model:value="formState.tips[idx]" :placeholder="t('appUi.k827757ea')" class="flex-1" />
          <a-button danger type="text" @click="formState.tips.splice(idx, 1)">{{ t('appUi.kfc81e222') }}</a-button>
        </div>
        <a-button type="dashed" size="small" @click="formState.tips.push('')">
          {{ t('appUi.k6aaea8af') }}
        </a-button>
      </div>

      <div>
        <div class="font-semibold text-xs text-red-600 mb-2">
          {{ isSystem ? t('appUi.k28feece3') : t('appUi.k4b5cd029') }}
        </div>
        <div
          v-for="(_, idx) in formState.warnings"
          :key="'w-' + idx"
          class="flex items-center gap-2 mb-2"
        >
          <a-input
            v-model:value="formState.warnings[idx]"
            :placeholder="t('appUi.k32afe8ea')"
            class="flex-1"
          />
          <a-button danger type="text" @click="formState.warnings.splice(idx, 1)">{{ t('appUi.kfc81e222') }}</a-button>
        </div>
        <a-button type="dashed" size="small" @click="formState.warnings.push('')">
          {{ t('appUi.k0faedc11') }}
        </a-button>
      </div>

      <div>
        <div class="font-semibold text-xs text-gray-700 mb-2">{{ t('appUi.kae2ce921') }}</div>
        <div
          v-for="(fq, idx) in formState.faq"
          :key="'q-' + idx"
          class="flex items-center gap-2 mb-2"
        >
          <a-input v-model:value="fq.q" :placeholder="t('appUi.ke1aca374')" class="w-2/5" />
          <a-input v-model:value="fq.a" :placeholder="t('appUi.k3655d2fc')" class="flex-1" />
          <a-button danger type="text" @click="formState.faq.splice(idx, 1)">{{ t('appUi.kfc81e222') }}</a-button>
        </div>
        <a-button type="dashed" size="small" @click="formState.faq.push({ q: '', a: '' })">
          {{ t('appUi.k3d353fcc') }}
        </a-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const props = defineProps<{
    formState: Record<string, any>;
    helpType?: string;
  }>();

  const activeTab = ref<'features' | 'steps' | 'lineage' | 'tips'>('features');
  const isSystem = computed(() => String(props.helpType || props.formState.helpType || '').toUpperCase() === 'SYSTEM');

  const builderTabs = computed(() => [
    {
      key: 'features' as const,
      label: `${isSystem.value ? t('appUi.kf64aaffd') : t('appUi.k5c1f3414')} (${props.formState.features?.length || 0})`,
    },
    {
      key: 'steps' as const,
      label: `${isSystem.value ? t('appUi.k3d39ef19') : t('appUi.k0fc371eb')} (${props.formState.steps?.length || 0})`,
    },
    {
      key: 'lineage' as const,
      label: isSystem.value ? t('appUi.kd5027593') : t('appUi.k07570959'),
    },
    {
      key: 'tips' as const,
      label: isSystem.value ? t('appUi.kf54b19a6') : t('appUi.kcf9c5a47'),
    },
  ]);
</script>
