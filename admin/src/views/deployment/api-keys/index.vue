<template>
  <div>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate('INBOUND')">{{ t('appUi.k88d1fedc') }}</a-button>
        <a-button @click="handleCreate('OUTBOUND')">{{ t('appUi.kfc2775be') }}</a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'direction'">
          <Tag :color="record.direction === 'OUTBOUND' ? 'indigo' : 'default'">
            {{ record.direction === 'OUTBOUND' ? t('appUi.k10bffc65') : t('appUi.k903fdd13') }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'serviceCode'">
          <Tag :color="serviceColor(record.serviceCode)">
            {{ record.serviceCode }}
          </Tag>
          <div v-if="record.baseUrl" class="text-xs font-mono text-gray-500 mt-1">
            {{ record.baseUrl }}
          </div>
        </template>
        <template v-else-if="column.key === 'credentials'">
          <div class="space-y-1 text-xs font-mono">
            <div class="flex items-center gap-1">
              <span class="text-gray-400">AK:</span>
              <span class="font-semibold text-blue-600">{{ record.accessKey }}</span>
              <a-button type="link" size="small" @click="copyText(record.accessKey, 'Access Key')">{{ t('appUi.ka55b1ecb') }}</a-button>
            </div>
            <div class="flex items-center gap-1">
              <span class="text-gray-400">SK:</span>
              <span class="text-gray-600">
                {{ visibleSecrets[record.id] ? record.secretKey : maskSecret(record.secretKey) }}
              </span>
              <a-button type="link" size="small" @click="toggleSecret(record.id)">
                {{ visibleSecrets[record.id] ? t('appUi.k093ff103') : t('appUi.ka61d568e') }}
              </a-button>
              <a-button type="link" size="small" @click="copyText(record.secretKey, 'Secret Key')">{{ t('appUi.ka55b1ecb') }}</a-button>
            </div>
          </div>
        </template>
        <template v-else-if="column.key === 'scopes'">
          <div class="flex flex-wrap gap-1">
            <Tag v-for="scope in record.scopes || []" :key="scope" color="blue">
              {{ scope }}
            </Tag>
          </div>
        </template>
        <template v-else-if="column.key === 'status'">
          <Tag :color="record.status === 'ACTIVE' ? 'success' : 'error'">
            {{ record.status === 'ACTIVE' ? t('appUi.k7964be1a') : t('appUi.kd73ce22f') }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                label: t('appUi.kb7a09430'),
                icon: 'ant-design:api-outlined',
                onClick: handleVerify.bind(null, record),
              },
              {
                label: t('appUi.kb0678b3d'),
                icon: 'ant-design:sync-outlined',
                popConfirm: {
                  title: t('appUi.k97591ea5'),
                  confirm: handleRotate.bind(null, record),
                },
              },
              {
                label: t('appUi.kd482e14b'),
                icon: 'ant-design:edit-outlined',
                onClick: handleEdit.bind(null, record),
              },
              {
                label: record.status === 'ACTIVE' ? t('appUi.k9cb606b2') : t('appUi.kbbf831ad'),
                color: record.status === 'ACTIVE' ? 'warning' : 'success',
                popConfirm: {
                  title: record.status === 'ACTIVE' ? t('appUi.ke1afc124') : t('appUi.k4ad5c539'),
                  confirm: handleToggleStatus.bind(null, record),
                },
              },
              {
                label: t('appUi.kfc81e222'),
                icon: 'ant-design:delete-outlined',
                color: 'error',
                popConfirm: {
                  title: t('appUi.k5cd8a8b2'),
                  confirm: handleDelete.bind(null, record),
                },
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>
    <ApiKeyModal @register="registerModal" @success="reload" />
  </div>
</template>

<script lang="ts" setup>
  import { reactive } from 'vue';
  import { Tag } from 'ant-design-vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    getAdminApiKeyList,
    rotateAdminApiKey,
    updateAdminApiKeyStatus,
    verifyAdminApiKey,
    deleteAdminApiKey,
  } from '/@/api/kkumz';
  import ApiKeyModal from './components/ApiKeyModal.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const { createMessage, createSuccessModal, createErrorModal } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const visibleSecrets = reactive<Record<string, boolean>>({});

  const [registerTable, { reload }] = useTable({
    title: t('appUi.k219ee1e6'),
    api: getAdminApiKeyList,
    rowKey: 'id',
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    formConfig: {
      labelWidth: 95,
      schemas: [
        {
          field: 'direction',
          label: t('appUi.kb553f730'),
          component: 'JDictSelectTag',
          componentProps: {
            dictCode: 'kkumz_api_direction',
            placeholder: t('appUi.k934dd25e'),
          },
          colProps: { span: 6 },
        },
        {
          field: 'serviceCode',
          label: t('appUi.k484829ad'),
          component: 'JDictSelectTag',
          componentProps: {
            dictCode: 'kkumz_api_service_code',
            placeholder: t('appUi.k934dd25e'),
          },
          colProps: { span: 6 },
        },
        {
          field: 'status',
          label: t('appUi.k2926977b'),
          component: 'JDictSelectTag',
          componentProps: {
            dictCode: 'kkumz_api_key_status',
            placeholder: t('appUi.k934dd25e'),
          },
          colProps: { span: 5 },
        },
        {
          field: 'keyword',
          label: t('appUi.kd64280cb'),
          component: 'Input',
          componentProps: { placeholder: t('appUi.ke850cbd0') },
          colProps: { span: 7 },
        },
      ],
    },
    columns: [
      { title: t('appUi.kb553f730'), dataIndex: 'direction', key: 'direction', width: 170 },
      { title: t('appUi.k0a69e043'), dataIndex: 'serviceCode', key: 'serviceCode', width: 210 },
      { title: t('appUi.k6fb40084'), dataIndex: 'keyName', width: 220 },
      { title: 'Access Key / Secret Key', dataIndex: 'accessKey', key: 'credentials', width: 320 },
      { title: t('appUi.ke3a01397'), dataIndex: 'scopes', key: 'scopes', width: 240 },
      { title: t('appUi.kf2caa8d8'), dataIndex: 'callCount', width: 110, customRender: ({ record }) => t('appUi.kf93445e7', { p0: record.callCount || 0, p1: record.rateLimitPerMin }) },
      { title: t('appUi.k7147a142'), dataIndex: 'lastUsedAt', width: 155 },
      { title: t('appUi.k2926977b'), dataIndex: 'status', key: 'status', width: 105 },
    ],
    actionColumn: {
      width: 310,
      title: t('appUi.k7ed11b5a'),
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  });

  function serviceColor(code: string) {
    if (code === 'SYNC_CMS') return 'default';
    if (code === 'SYNC_SHOP') return 'blue';
    if (code === 'SYNC_VERSE') return 'default';
    return 'orange';
  }

  function maskSecret(secret?: string) {
    if (!secret) return '********';
    if (secret.length <= 10) return '********';
    return `${secret.slice(0, 7)}••••••••••••${secret.slice(-4)}`;
  }

  function toggleSecret(id: string) {
    visibleSecrets[id] = !visibleSecrets[id];
  }

  function copyText(text: string, label: string) {
    if (!text) return;
    navigator.clipboard?.writeText(text);
    createMessage.success(t('appUi.kb48740f6', { p0: label }));
  }

  function handleCreate(defaultDirection: string) {
    openModal(true, { isUpdate: false, defaultDirection });
  }

  function handleEdit(record: Recordable) {
    openModal(true, { isUpdate: true, record });
  }

  async function handleVerify(record: Recordable) {
    const res = await verifyAdminApiKey(record.id);
    reload();
    if (res?.verified) {
      createSuccessModal({
        title: t('appUi.k881c1cde', { p0: record.serviceCode }),
        content: t('appUi.k6f3c99c2', { p0: res.keyName, p1: res.message, p2: res.latencyMs ?? 1, p3: res.endpoint || 'INBOUND' }),
      });
    } else {
      createErrorModal({
        title: t('appUi.kab86ea27', { p0: record.serviceCode }),
        content: res?.message || t('appUi.kdee2b726'),
      });
    }
  }

  async function handleRotate(record: Recordable) {
    const updated = await rotateAdminApiKey(record.id);
    visibleSecrets[record.id] = true;
    createMessage.success(t('appUi.kf2d302a4', { p0: updated?.secretKey?.slice(0, 10) }));
    reload();
  }

  async function handleToggleStatus(record: Recordable) {
    const nextStatus = record.status === 'ACTIVE' ? 'REVOKED' : 'ACTIVE';
    await updateAdminApiKeyStatus(record.id, nextStatus);
    createMessage.success(t('appUi.k58d3c2ea', { p0: nextStatus }));
    reload();
  }

  async function handleDelete(record: Recordable) {
    await deleteAdminApiKey(record.id);
    createMessage.success(t('appUi.kfff77ba4'));
    reload();
  }
</script>
