<template>
  <div>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate('INBOUND')">+ 인바운드 API 키 신규 발급</a-button>
        <a-button @click="handleCreate('OUTBOUND')">+ 외부 백엔드(SyncCMS/SyncShop) 연동 키 등록</a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'direction'">
          <Tag :color="record.direction === 'OUTBOUND' ? 'indigo' : 'cyan'">
            {{ record.direction === 'OUTBOUND' ? 'OUTBOUND (백엔드 호출)' : 'INBOUND (외부 유입)' }}
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
              <a-button type="link" size="small" @click="copyText(record.accessKey, 'Access Key')">복사</a-button>
            </div>
            <div class="flex items-center gap-1">
              <span class="text-gray-400">SK:</span>
              <span class="text-gray-600">
                {{ visibleSecrets[record.id] ? record.secretKey : maskSecret(record.secretKey) }}
              </span>
              <a-button type="link" size="small" @click="toggleSecret(record.id)">
                {{ visibleSecrets[record.id] ? '숨김' : '표시' }}
              </a-button>
              <a-button type="link" size="small" @click="copyText(record.secretKey, 'Secret Key')">복사</a-button>
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
            {{ record.status === 'ACTIVE' ? '정상 활성' : '폐기/중지' }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                label: '연동 검증',
                icon: 'ant-design:api-outlined',
                onClick: handleVerify.bind(null, record),
              },
              {
                label: 'Secret 재발급',
                icon: 'ant-design:sync-outlined',
                popConfirm: {
                  title: 'Secret Key를 즉시 회전(재발급)하시겠습니까? OUTBOUND 키는 대상 서비스에도 즉시 동기화됩니다.',
                  confirm: handleRotate.bind(null, record),
                },
              },
              {
                label: '편집',
                icon: 'ant-design:edit-outlined',
                onClick: handleEdit.bind(null, record),
              },
              {
                label: record.status === 'ACTIVE' ? '중지' : '활성화',
                color: record.status === 'ACTIVE' ? 'warning' : 'success',
                popConfirm: {
                  title: record.status === 'ACTIVE' ? '이 API 키를 일시 중지하시겠습니까?' : '이 API 키를 다시 활성화하시겠습니까?',
                  confirm: handleToggleStatus.bind(null, record),
                },
              },
              {
                label: '삭제',
                icon: 'ant-design:delete-outlined',
                color: 'error',
                popConfirm: {
                  title: '이 API 액세스 키를 영구 삭제하시겠습니까?',
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

  const { createMessage, createSuccessModal, createErrorModal } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const visibleSecrets = reactive<Record<string, boolean>>({});

  const [registerTable, { reload }] = useTable({
    title: '서비스 간 API 연동 · 액세스 키(Access Key / Secret Key) 관제',
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
          label: '연동 방향',
          component: 'Select',
          componentProps: {
            options: [
              { label: '전체', value: '' },
              { label: 'OUTBOUND (SyncCMS / SyncShop 호출)', value: 'OUTBOUND' },
              { label: 'INBOUND (외부 에이전트 유입)', value: 'INBOUND' },
            ],
          },
          colProps: { span: 6 },
        },
        {
          field: 'serviceCode',
          label: '대상 서비스',
          component: 'Select',
          componentProps: {
            options: [
              { label: '전체', value: '' },
              { label: 'SYNC_CMS', value: 'SYNC_CMS' },
              { label: 'SYNC_SHOP', value: 'SYNC_SHOP' },
              { label: 'SYNC_VERSE', value: 'SYNC_VERSE' },
              { label: 'EXTERNAL_PARTNER', value: 'EXTERNAL_PARTNER' },
            ],
          },
          colProps: { span: 6 },
        },
        {
          field: 'status',
          label: '상태',
          component: 'Select',
          componentProps: {
            options: [
              { label: '전체', value: '' },
              { label: '정상 활성', value: 'ACTIVE' },
              { label: '폐기/중지', value: 'REVOKED' },
            ],
          },
          colProps: { span: 5 },
        },
        {
          field: 'keyword',
          label: '검색어',
          component: 'Input',
          componentProps: { placeholder: '키 명칭, Access Key 검색' },
          colProps: { span: 7 },
        },
      ],
    },
    columns: [
      { title: '연동 방향', dataIndex: 'direction', key: 'direction', width: 170 },
      { title: '대상 서비스 / 엔드포인트', dataIndex: 'serviceCode', key: 'serviceCode', width: 210 },
      { title: '액세스키 명칭', dataIndex: 'keyName', width: 220 },
      { title: 'Access Key / Secret Key', dataIndex: 'accessKey', key: 'credentials', width: 320 },
      { title: '권한 스코프 (Scopes)', dataIndex: 'scopes', key: 'scopes', width: 240 },
      { title: '호출수 / RPM', dataIndex: 'callCount', width: 110, customRender: ({ record }) => `${record.callCount || 0}회 (${record.rateLimitPerMin}/m)` },
      { title: '최근 호출 시각', dataIndex: 'lastUsedAt', width: 155 },
      { title: '상태', dataIndex: 'status', key: 'status', width: 105 },
    ],
    actionColumn: {
      width: 310,
      title: '관리 및 연동 테스트',
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  });

  function serviceColor(code: string) {
    if (code === 'SYNC_CMS') return 'purple';
    if (code === 'SYNC_SHOP') return 'blue';
    if (code === 'SYNC_VERSE') return 'geekblue';
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
    createMessage.success(`${label}가 클립보드에 복사되었습니다.`);
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
        title: `${record.serviceCode} API 연동 인증 성공`,
        content: `[${res.keyName}] ${res.message} (응답 지연: ${res.latencyMs ?? 1}ms, 엔드포인트: ${res.endpoint || 'INBOUND'})`,
      });
    } else {
      createErrorModal({
        title: `${record.serviceCode} API 연동 인증 실패`,
        content: res?.message || '원격 서비스 연결 또는 Access Key 검증에 실패했습니다.',
      });
    }
  }

  async function handleRotate(record: Recordable) {
    const updated = await rotateAdminApiKey(record.id);
    visibleSecrets[record.id] = true;
    createMessage.success(`새 Secret Key(${updated?.secretKey?.slice(0, 10)}...)가 발급 및 동기화되었습니다.`);
    reload();
  }

  async function handleToggleStatus(record: Recordable) {
    const nextStatus = record.status === 'ACTIVE' ? 'REVOKED' : 'ACTIVE';
    await updateAdminApiKeyStatus(record.id, nextStatus);
    createMessage.success(`키 상태가 ${nextStatus}(으)로 변경되었습니다.`);
    reload();
  }

  async function handleDelete(record: Recordable) {
    await deleteAdminApiKey(record.id);
    createMessage.success('API 액세스 키가 삭제되었습니다.');
    reload();
  }
</script>
