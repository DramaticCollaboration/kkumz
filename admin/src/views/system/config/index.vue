<template>
  <PageWrapper>
    <a-card :bordered="false" class="shadow-sm">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange" type="card">
        <template #rightExtra>
          <div class="flex items-center gap-2 flex-wrap">
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="설정 키·명칭·설명 검색"
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
              외부 연동 연결 진단
            </a-button>
            <a-button type="default" @click="handleOpenAddModal">
              <template #icon><Icon icon="ant-design:plus-outlined" /></template>
              설정 항목 추가
            </a-button>
            <a-button type="default" @click="handleRefreshCache" :loading="refreshing">
              <template #icon><Icon icon="ant-design:sync-outlined" /></template>
              캐시 즉시 동기화
            </a-button>
            <a-button type="primary" @click="handleSaveCurrentCategory" :loading="saving">
              <template #icon><Icon icon="ant-design:save-outlined" /></template>
              현재 탭 일괄 저장
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
            description="조건에 일치하는 환경설정 항목이 없습니다."
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
                                편집
                              </a-button>
                              <a-button
                                type="link"
                                size="small"
                                class="p-0 h-auto text-xs"
                                @click="handleResetSingle(item)"
                              >
                                기본값 복원
                              </a-button>
                            </div>
                          </div>
                        </template>

                        <a-input-password
                          v-if="item.isSecret || item.valueType === 'SECRET'"
                          v-model:value="formModel[item.configKey]"
                          placeholder="민감 설정값을 입력하세요"
                        />
                        <a-input-number
                          v-else-if="item.valueType === 'NUMBER'"
                          v-model:value="formModel[item.configKey]"
                          class="w-full"
                        />
                        <a-switch
                          v-else-if="item.valueType === 'BOOLEAN'"
                          v-model:checked="boolModel[item.configKey]"
                          checked-children="활성"
                          un-checked-children="비활성"
                          @change="(val) => (formModel[item.configKey] = String(val))"
                        />
                        <a-textarea
                          v-else-if="item.valueType === 'JSON'"
                          v-model:value="formModel[item.configKey]"
                          :rows="3"
                          class="font-mono text-xs"
                          placeholder="JSON 설정값을 입력하세요"
                        />
                        <a-input
                          v-else
                          v-model:value="formModel[item.configKey]"
                          placeholder="설정값을 입력하세요"
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

const activeTab = ref<string>('INTEGRATION');
const searchKeyword = ref<string>('');
const configList = ref<SysConfigModel[]>([]);
const categoryLabelMap = reactive<Record<string, string>>({});
const formModel = reactive<Record<string, any>>({});
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
  } catch (e: any) {
    message.error('환경설정 목록 조회에 실패했습니다: ' + (e.message || '알 수 없는 오류'));
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
    message.success('현재 탭의 설정이 성공적으로 저장 및 핫 리로드되었습니다.');
    await loadConfigs();
  } catch (e: any) {
    message.error('설정 저장 중 오류가 발생했습니다: ' + (e.message || '알 수 없는 오류'));
  } finally {
    saving.value = false;
  }
}

async function handleResetSingle(item: SysConfigModel) {
  try {
    await resetConfig(item.configKey);
    message.success(`[${item.configName}] 기본값으로 복원되었습니다.`);
    await loadConfigs();
  } catch (e: any) {
    message.error('기본값 복원 실패: ' + (e.message || '알 수 없는 오류'));
  }
}

async function handleRefreshCache() {
  refreshing.value = true;
  try {
    await refreshConfigCache();
    message.success('시스템 환경설정 캐시가 성공적으로 갱신되었습니다.');
    await loadConfigs();
  } catch (e: any) {
    message.error('캐시 동기화 실패: ' + (e.message || '알 수 없는 오류'));
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
  } catch (e: any) {
    message.error('연결 진단 중 예외 발생: ' + (e.message || '서버 통신 오류'));
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
