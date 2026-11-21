<template>
  <PageWrapper>
    <a-card :bordered="false" class="shadow-sm">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange" type="card">
        <template #rightExtra>
          <div class="flex items-center gap-2 flex-wrap">
            <a-input-search
              v-model:value="searchKeyword"
              :placeholder="t('appUi.kbace4ff5')"
              allow-clear
              style="width: 220px"
            />
            <a-button
              v-if="activeTab === 'INTEGRATION'"
              type="primary"
              ghost
              @click="handleTestIntegrationConnection"
              :loading="testing"
            >
              <template #icon><Icon icon="ant-design:cluster-outlined" /></template>
              {{ t('appUi.k7d5a032f') }}
            </a-button>
            <a-button type="default" @click="handleOpenAddModal">
              <template #icon><Icon icon="ant-design:plus-outlined" /></template>
              {{ t('appUi.k0b325bd0') }}
            </a-button>
            <a-button type="default" @click="handleRefreshCache" :loading="refreshing">
              <template #icon><Icon icon="ant-design:sync-outlined" /></template>
              {{ t('appUi.kb543d328') }}
            </a-button>
            <a-button type="primary" @click="handleSaveCurrentCategory" :loading="saving">
              <template #icon><Icon icon="ant-design:save-outlined" /></template>
              {{ t('appUi.k7e2095dc') }}
            </a-button>
          </div>
        </template>

        <a-tab-pane
          v-for="cat in categoryTabs"
          :key="cat.code"
          :tab="`${cat.label} (${cat.count})`"
        >
          <a-empty
            v-if="currentConfigs.length === 0"
            :description="t('appUi.k00203e77')"
            class="my-8"
          />
          <BasicForm v-else layout="vertical" :showActionButtonGroup="false" class="max-w-6xl">
            <template #formHeader>
              <div class="w-full">
                <a-row :gutter="16">
                  <template v-for="item in currentConfigs" :key="item.configKey">
                    <a-col :span="item.valueType === 'JSON' ? 24 : 12">
                      <a-form-item>
                        <template #label>
                          <div class="flex items-center gap-2">
                            <span class="font-medium">{{ item.configName }}</span>
                            <a-tag v-if="item.isSecret || item.valueType === 'SECRET'" color="red">
                              AES-GCM
                            </a-tag>
                            <a-tag v-else color="blue">{{ item.valueType }}</a-tag>
                          </div>
                        </template>
                        <template #extra>
                          <div class="flex justify-between items-center mt-1 text-xs text-gray-400 gap-2">
                            <span class="truncate" :title="`${item.configKey} - ${item.description || ''}`">
                              <code class="text-gray-500">{{ item.configKey }}</code>
                              <span v-if="item.description"> - {{ item.description }}</span>
                            </span>
                            <div class="flex items-center gap-2 shrink-0">
                              <a-button
                                type="link"
                                size="small"
                                class="p-0 h-auto text-xs"
                                @click="handleOpenEditModal(item)"
                              >
                                {{ t('appUi.kd482e14b') }}
                              </a-button>
                              <a-button
                                type="link"
                                size="small"
                                class="p-0 h-auto text-xs"
                                @click="handleResetSingle(item)"
                              >
                                {{ t('appUi.ka2d19eaa') }}
                              </a-button>
                            </div>
                          </div>
                        </template>

                        <a-input-password
                          v-if="item.isSecret || item.valueType === 'SECRET'"
                          v-model:value="formModel[item.configKey]"
                          :placeholder="t('appUi.kf23263a7')"
                        />
                        <a-input-number
                          v-else-if="item.valueType === 'NUMBER'"
                          v-model:value="formModel[item.configKey]"
                          class="w-full"
                        />
                        <a-switch
                          v-else-if="item.valueType === 'BOOLEAN'"
                          v-model:checked="boolModel[item.configKey]"
                          :checked-children="t('appUi.k6a2123a3')"
                          :un-checked-children="t('appUi.kfae2014b')"
                          @change="(val) => (formModel[item.configKey] = String(val))"
                        />
                        <a-textarea
                          v-else-if="item.valueType === 'JSON'"
                          v-model:value="formModel[item.configKey]"
                          :rows="3"
                          class="font-mono text-xs"
                          :placeholder="t('appUi.k251977de')"
                        />
                        <a-input
                          v-else
                          v-model:value="formModel[item.configKey]"
                          :placeholder="t('appUi.kd9567124')"
                        />
                      </a-form-item>
                    </a-col>
                  </template>
                </a-row>
              </div>
            </template>
          </BasicForm>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <ConfigDiagModal @register="registerDiagModal" />
    <ConfigFormModal @register="registerFormModal" @success="loadConfigs" />
  </PageWrapper>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { message } from 'ant-design-vue';
import Icon from '/@/components/Icon';
import { PageWrapper } from '/@/components/Page';
import { BasicForm } from '/@/components/Form';
import { useModal } from '/@/components/Modal';
import ConfigDiagModal from './components/ConfigDiagModal.vue';
import ConfigFormModal from './components/ConfigFormModal.vue';
import {
  queryConfigList,
  queryCategoryDictItems,
  updateBatchConfig,
  resetConfig,
  refreshConfigCache,
  testConnection,
  SysConfigModel,
} from './config.api';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

const activeTab = ref<string>('INTEGRATION');
const searchKeyword = ref<string>('');
const configList = ref<SysConfigModel[]>([]);
const categoryLabelMap = reactive<Record<string, string>>({});
const formModel = reactive<Record<string, string | number | boolean | undefined>>({});
const boolModel = reactive<Record<string, boolean>>({});
const saving = ref<boolean>(false);
const refreshing = ref<boolean>(false);
const testing = ref<boolean>(false);

const [registerDiagModal, { openModal: openDiagModal }] = useModal();
const [registerFormModal, { openModal: openFormModal }] = useModal();

function resolveCategoryLabel(code: string): string {
  if (!code) return 'GENERAL';
  return categoryLabelMap[code] || code;
}

const categoryTabs = computed(() => {
  const counts = new Map<string, number>();
  for (const item of configList.value) {
    const cat = item.configCategory || 'GENERAL';
    counts.set(cat, (counts.get(cat) || 0) + 1);
  }
  if (counts.size === 0) {
    return [{ code: 'INTEGRATION', label: resolveCategoryLabel('INTEGRATION'), count: 0 }];
  }
  return Array.from(counts.entries()).map(([code, count]) => ({
    code,
    label: resolveCategoryLabel(code),
    count,
  }));
});

const currentConfigs = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase();
  return configList.value.filter((item) => {
    const matchTab = item.configCategory === activeTab.value;
    if (!matchTab) return false;
    if (!kw) return true;
    return (
      (item.configKey && item.configKey.toLowerCase().includes(kw)) ||
      (item.configName && item.configName.toLowerCase().includes(kw)) ||
      (item.description && item.description.toLowerCase().includes(kw)) ||
      (item.configValue && String(item.configValue).toLowerCase().includes(kw))
    );
  });
});

async function loadCategoryDict() {
  try {
    const items = await queryCategoryDictItems();
    if (Array.isArray(items)) {
      items.forEach((item) => {
        const label = item.text || item.title || item.label || item.value;
        if (item.value && label) {
          categoryLabelMap[item.value] = label;
        }
      });
    }
  } catch {
    // 사전 미등록 시 카테고리 코드를 그대로 탭 제목으로 사용
  }
}

async function loadConfigs() {
  try {
    const res = await queryConfigList();
    if (Array.isArray(res)) {
      configList.value = res;
      const availableCategories = new Set(res.map((r) => r.configCategory));
      if (availableCategories.size > 0 && !availableCategories.has(activeTab.value)) {
        activeTab.value = res[0].configCategory;
      }
      res.forEach((item) => {
        formModel[item.configKey] = item.configValue;
        if (item.valueType === 'BOOLEAN') {
          boolModel[item.configKey] = item.configValue === 'true';
        }
      });
    }
  } catch (e: unknown) {
    message.error(t('appUi.k76b4fe13') + ((e instanceof Error ? e.message : (e as { message?: string })?.message) || t('appUi.k5e9f6b80')));
  }
}

function handleTabChange(tabKey: string) {
  activeTab.value = tabKey;
}

function handleOpenAddModal() {
  openFormModal(true, {
    isUpdate: false,
    defaultCategory: activeTab.value,
    categories: categoryTabs.value.map((c) => c.code),
  });
}

function handleOpenEditModal(record: SysConfigModel) {
  openFormModal(true, {
    isUpdate: true,
    record,
    categories: categoryTabs.value.map((c) => c.code),
  });
}

async function handleSaveCurrentCategory() {
  saving.value = true;
  try {
    const itemsToUpdate = currentConfigs.value.map((cfg) => ({
      configKey: cfg.configKey,
      configValue: String(formModel[cfg.configKey] ?? ''),
    }));

    await updateBatchConfig(itemsToUpdate);
    message.success(t('appUi.kb8cff7dc'));
    await loadConfigs();
  } catch (e: unknown) {
    message.error(t('appUi.kd763edfb') + ((e instanceof Error ? e.message : (e as { message?: string })?.message) || t('appUi.k5e9f6b80')));
  } finally {
    saving.value = false;
  }
}

async function handleResetSingle(item: SysConfigModel) {
  try {
    await resetConfig(item.configKey);
    message.success(t('appUi.kcf57fbd1', { p0: item.configName }));
    await loadConfigs();
  } catch (e: unknown) {
    message.error(t('appUi.kb66dac2e') + ((e instanceof Error ? e.message : (e as { message?: string })?.message) || t('appUi.k5e9f6b80')));
  }
}

async function handleRefreshCache() {
  refreshing.value = true;
  try {
    await refreshConfigCache();
    message.success(t('appUi.k8cc04e24'));
    await loadConfigs();
  } catch (e: unknown) {
    message.error(t('appUi.kcb87df22') + ((e instanceof Error ? e.message : (e as { message?: string })?.message) || t('appUi.k5e9f6b80')));
  } finally {
    refreshing.value = false;
  }
}

async function handleTestIntegrationConnection() {
  testing.value = true;
  try {
    const endpoint = formModel['kkumz.integration.cms.base-url'] || '';
    const res = await testConnection({
      testType: 'INTEGRATION',
      endpoint,
    });
    openDiagModal(true, { diagResult: res });
  } catch (e: unknown) {
    message.error(t('appUi.kf749a189') + ((e instanceof Error ? e.message : (e as { message?: string })?.message) || t('appUi.kf0e53658')));
  } finally {
    testing.value = false;
  }
}

onMounted(() => {
  loadCategoryDict();
  loadConfigs();
});
</script>

<style scoped>
:deep(.ant-tabs-card .ant-tabs-tab) {
  border-radius: 6px 6px 0 0;
}
</style>
