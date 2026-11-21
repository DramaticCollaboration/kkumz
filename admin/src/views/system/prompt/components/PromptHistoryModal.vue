<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('appUi.k10768082')"
    width="920px"
    :showOkBtn="false"
    :cancelText="t('appUi.k94b7dba1')"
  >
    <div class="space-y-4">
      <a-descriptions bordered size="small" :column="{ xs: 1, sm: 1, md: 2, lg: 3 }">
        <a-descriptions-item :label="t('appUi.k5bef10a7')">
          <span class="font-mono text-xs font-semibold">{{ currentRecord.promptCode }}</span>
        </a-descriptions-item>
        <a-descriptions-item :label="t('appUi.kef80204d')">
          {{ currentRecord.promptName }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('appUi.kdfc80c3b')">
          <a-tag color="blue">v{{ currentRecord.version || 1 }}</a-tag>
        </a-descriptions-item>
      </a-descriptions>

      <a-table
        :dataSource="historyList"
        :columns="historyColumns"
        rowKey="id"
        size="small"
        bordered
        :pagination="{ pageSize: 8 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'version'">
            <a-tag :color="record.version === currentRecord.version ? 'green' : 'default'">
              v{{ record.version }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="selectedSnapshot = record">
                {{ t('appUi.k2d2bf613') }}
              </a-button>
              <a-popconfirm
                v-if="record.version !== currentRecord.version"
                :title="t('appUi.k27fe7499', { p0: record.version })"
                @confirm="handleRollback(record)"
              >
                <a-button type="link" size="small">{{ t('appUi.k4eeaf478') }}</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <div v-if="selectedSnapshot" class="p-3 bg-gray-50 rounded border border-gray-200 space-y-2">
        <div class="font-semibold text-xs text-gray-700">
          {{ t('appUi.k1914aa94') }}{{ selectedSnapshot.version }} - {{ selectedSnapshot.changeReason || t('appUi.k14bf3e5b') }})
        </div>
        <div>
          <div class="text-xs text-gray-500 mb-1">{{ t('appUi.k58301d13') }}</div>
          <pre class="snapshot-box">{{ selectedSnapshot.systemPrompt }}</pre>
        </div>
        <div>
          <div class="text-xs text-gray-500 mb-1">{{ t('appUi.kea6cdc93') }}</div>
          <pre class="snapshot-box">{{ selectedSnapshot.userPromptTemplate }}</pre>
        </div>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    getPromptHistory,
    rollbackPromptVersion,
    SysPromptHistoryModel,
    SysPromptModel,
  } from '../prompt.api';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();

  const currentRecord = ref<Partial<SysPromptModel>>({});
  const historyList = ref<SysPromptHistoryModel[]>([]);
  const selectedSnapshot = ref<SysPromptHistoryModel | null>(null);

  const historyColumns = [
    { title: t('appUi.k593f8a81'), dataIndex: 'version', width: 80, align: 'center' as const },
    { title: t('appUi.k4ac852d6'), dataIndex: 'changeReason', align: 'left' as const },
    { title: t('appUi.k83b47117'), dataIndex: 'temperature', width: 85, align: 'center' as const },
    { title: t('appUi.k52164040'), dataIndex: 'createdBy', width: 100, align: 'center' as const },
    { title: t('appUi.kc4090341'), dataIndex: 'createdAt', width: 170, align: 'center' as const },
    { title: t('appUi.kc29fba5a'), dataIndex: 'action', width: 180, align: 'center' as const },
  ];

  const [registerModal, { closeModal }] = useModalInner(async (data) => {
    const record: SysPromptModel = data?.record || {};
    currentRecord.value = record;
    selectedSnapshot.value = null;
    if (record.id) {
      const list = await getPromptHistory(record.id);
      historyList.value = Array.isArray(list) ? list : [];
      if (historyList.value.length > 0) {
        selectedSnapshot.value = historyList.value[0];
      }
    } else {
      historyList.value = [];
    }
  });

  async function handleRollback(snapshot: SysPromptHistoryModel) {
    if (!currentRecord.value.id) return;
    await rollbackPromptVersion(currentRecord.value.id, snapshot.version);
    createMessage.success(t('appUi.k023e82f4', { p0: snapshot.version }));
    closeModal();
    emit('success');
  }
</script>

<style lang="less" scoped>
  .snapshot-box {
    margin: 0;
    padding: 8px 10px;
    background: #ffffff;
    border: 1px solid #e5e7eb;
    border-radius: 4px;
    font-size: 12px;
    line-height: 1.5;
    white-space: pre-wrap;
    word-break: break-word;
    max-height: 150px;
    overflow-y: auto;
  }
</style>
