<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('appUi.k10768082')"
    width="920px"
    :showOkBtn="false"
    :cancelText="t('appUi.k94b7dba1')"
  >
    <div class="p-2 space-y-4">
      <Description
        :schema="promptDescSchema"
        :data="currentRecord"
        :column="{ xs: 1, sm: 1, md: 2, lg: 3 }"
        size="small"
        bordered
      />

      <BasicTable @register="registerHistoryTable">
        <template #action="{ record }">
          <TableAction
            :actions="[
              {
                label: t('appUi.k2d2bf613'),
                onClick: handleSelectSnapshot.bind(null, record),
              },
              {
                label: t('appUi.k4eeaf478'),
                ifShow: record.version !== currentRecord.version,
                popConfirm: {
                  title: t('appUi.k27fe7499', { p0: record.version }),
                  confirm: handleRollback.bind(null, record),
                },
              },
            ]"
          />
        </template>
      </BasicTable>

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
  import { h, ref } from 'vue';
  import { Tag } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { Description, DescItem } from '/@/components/Description';
  import { BasicTable, useTable, TableAction, BasicColumn } from '/@/components/Table';
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

  const promptDescSchema: DescItem[] = [
    {
      field: 'promptCode',
      label: t('appUi.k5bef10a7'),
      render: (val) => h('span', { class: 'font-mono text-xs font-semibold' }, val || ''),
    },
    {
      field: 'promptName',
      label: t('appUi.kef80204d'),
    },
    {
      field: 'version',
      label: t('appUi.kdfc80c3b'),
      render: (val) => h(Tag, { color: 'blue' }, () => `v${val || 1}`),
    },
  ];

  const historyColumns: BasicColumn[] = [
    {
      title: t('appUi.k593f8a81'),
      dataIndex: 'version',
      width: 80,
      align: 'center',
      customRender: ({ record }) =>
        h(
          Tag,
          { color: record.version === currentRecord.value.version ? 'green' : 'default' },
          () => `v${record.version}`,
        ),
    },
    { title: t('appUi.k4ac852d6'), dataIndex: 'changeReason', align: 'left' },
    { title: t('appUi.k83b47117'), dataIndex: 'temperature', width: 85, align: 'center' },
    { title: t('appUi.k52164040'), dataIndex: 'createdBy', width: 100, align: 'center' },
    { title: t('appUi.kc4090341'), dataIndex: 'createdAt', width: 170, align: 'center' },
  ];

  const [registerHistoryTable, { setTableData }] = useTable({
    columns: historyColumns,
    dataSource: historyList,
    rowKey: 'id',
    size: 'small',
    bordered: true,
    pagination: { pageSize: 8 },
    showIndexColumn: false,
    showTableSetting: false,
    canResize: false,
    actionColumn: {
      title: t('appUi.kc29fba5a'),
      dataIndex: 'action',
      width: 180,
      align: 'center',
      slots: { customRender: 'action' },
    },
  });

  function handleSelectSnapshot(record: SysPromptHistoryModel) {
    selectedSnapshot.value = record;
  }

  const [registerModal, { closeModal }] = useModalInner(async (data) => {
    const record: SysPromptModel = data?.record || {};
    currentRecord.value = record;
    selectedSnapshot.value = null;
    if (record.id) {
      const list = await getPromptHistory(record.id);
      historyList.value = Array.isArray(list) ? list : [];
      setTableData(historyList.value);
      if (historyList.value.length > 0) {
        selectedSnapshot.value = historyList.value[0];
      }
    } else {
      historyList.value = [];
      setTableData([]);
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
