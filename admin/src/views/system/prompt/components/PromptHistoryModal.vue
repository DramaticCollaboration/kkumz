<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="프롬프트 버전 변경 이력 및 롤백"
    width="920px"
    :showOkBtn="false"
    cancelText="닫기"
  >
    <div class="space-y-4">
      <a-descriptions bordered size="small" :column="3">
        <a-descriptions-item label="프롬프트 코드">
          <span class="font-mono text-xs font-semibold">{{ currentRecord.promptCode }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="프롬프트 명칭">
          {{ currentRecord.promptName }}
        </a-descriptions-item>
        <a-descriptions-item label="현재 적용 버전">
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
                내용 보기
              </a-button>
              <a-popconfirm
                v-if="record.version !== currentRecord.version"
                :title="`v${record.version} 버전으로 복원하시겠습니까?`"
                @confirm="handleRollback(record)"
              >
                <a-button type="link" size="small">이 버전으로 복원</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <div v-if="selectedSnapshot" class="p-3 bg-gray-50 rounded border border-gray-200 space-y-2">
        <div class="font-semibold text-xs text-gray-700">
          선택 버전 상세 내용 (v{{ selectedSnapshot.version }} - {{ selectedSnapshot.changeReason || '변경 이력' }})
        </div>
        <div>
          <div class="text-xs text-gray-500 mb-1">시스템 페르소나 및 지침</div>
          <pre class="snapshot-box">{{ selectedSnapshot.systemPrompt }}</pre>
        </div>
        <div>
          <div class="text-xs text-gray-500 mb-1">사용자 입력 템플릿</div>
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

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();

  const currentRecord = ref<Partial<SysPromptModel>>({});
  const historyList = ref<SysPromptHistoryModel[]>([]);
  const selectedSnapshot = ref<SysPromptHistoryModel | null>(null);

  const historyColumns = [
    { title: '버전', dataIndex: 'version', width: 80, align: 'center' as const },
    { title: '변경 사유', dataIndex: 'changeReason', align: 'left' as const },
    { title: '온도값', dataIndex: 'temperature', width: 85, align: 'center' as const },
    { title: '작업자', dataIndex: 'createdBy', width: 100, align: 'center' as const },
    { title: '기록 일시', dataIndex: 'createdAt', width: 170, align: 'center' as const },
    { title: '관리', dataIndex: 'action', width: 180, align: 'center' as const },
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
    createMessage.success(`v${snapshot.version} 버전으로 복원되었습니다.`);
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
