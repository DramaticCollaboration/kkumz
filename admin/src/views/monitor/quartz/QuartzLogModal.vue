<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="modalTitle"
    :width="1020"
    :canFullscreen="true"
    :footer="null"
  >
    <div class="p-2 space-y-3">
      <!-- 상단 필터 및 작업 정보 바 -->
      <div class="flex flex-wrap items-center justify-between gap-3 bg-gray-50/80 p-3 rounded-lg border border-gray-100">
        <div class="flex items-center gap-2">
          <span class="text-xs font-semibold text-gray-500">조회 대상:</span>
          <a-tag v-if="targetJob" color="blue" class="font-medium text-xs m-0">
            {{ targetJob.description || targetJob.jobClassName }}
          </a-tag>
          <a-tag v-else color="purple" class="font-medium text-xs m-0">
            전체 스케줄러 작업
          </a-tag>
          <span class="text-xs text-gray-400 font-mono ml-1">총 {{ pagination.total }}건</span>
        </div>

        <div class="flex items-center gap-2">
          <a-select
            v-model:value="filterStatus"
            placeholder="상태 선택"
            style="width: 120px"
            size="small"
            allowClear
            @change="handleSearch"
          >
            <a-select-option value="">전체 상태</a-select-option>
            <a-select-option value="SUCCESS">정상 완료</a-select-option>
            <a-select-option value="WARNING">일부 조정</a-select-option>
            <a-select-option value="FAIL">오류 발생</a-select-option>
          </a-select>

          <a-input-search
            v-model:value="searchKeyword"
            placeholder="작업명 또는 에러 검색"
            style="width: 200px"
            size="small"
            allowClear
            @search="handleSearch"
          />
        </div>
      </div>

      <!-- 실행 로그 테이블 -->
      <BasicTable
        @register="registerLogTable"
        :dataSource="logList"
        :columns="tableColumns"
        :loading="loading"
        :pagination="pagination"
        size="small"
        rowKey="id"
        :showIndexColumn="false"
        :canResize="false"
        :scroll="{ x: 880 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'botName'">
            <div class="flex flex-col">
              <span class="font-medium text-gray-900 text-xs">{{ record.botName || record.botCode }}</span>
              <span class="text-[11px] text-gray-400 font-mono">{{ record.botCode }}</span>
            </div>
          </template>

          <template v-else-if="column.key === 'executionType'">
            <a-tag v-if="record.executionType === 'SCHEDULED'" color="cyan" class="text-[11px]">정기 스케줄</a-tag>
            <a-tag v-else color="orange" class="text-[11px]">수동 실행</a-tag>
          </template>

          <template v-else-if="column.key === 'startTime'">
            <span class="text-xs font-mono text-gray-700">{{ record.startTime }}</span>
          </template>

          <template v-else-if="column.key === 'durationMs'">
            <span class="text-xs font-mono text-gray-600 tabular-nums">
              {{ record.durationMs != null ? `${record.durationMs}ms` : '-' }}
            </span>
          </template>

          <template v-else-if="column.key === 'counts'">
            <span class="text-xs font-semibold text-gray-800 tabular-nums">
              성공 {{ record.successCount }} / 총 {{ record.totalCount }}
            </span>
            <span v-if="record.failCount > 0" class="text-xs text-rose-600 ml-1 font-semibold tabular-nums">
              (경고 {{ record.failCount }})
            </span>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-badge v-if="record.status === 'SUCCESS'" status="success" text="정상 완료" />
            <a-badge v-else-if="record.status === 'WARNING'" status="warning" text="일부 조정" />
            <a-badge v-else-if="record.status === 'RUNNING'" status="processing" text="실행 중" />
            <a-badge v-else status="error" text="오류" />
          </template>

          <template v-else-if="column.key === 'errorMessage'">
            <a-tooltip v-if="record.errorMessage" :title="record.errorMessage">
              <span class="text-xs text-rose-600 font-mono line-clamp-1 cursor-pointer">
                {{ record.errorMessage }}
              </span>
            </a-tooltip>
            <span v-else class="text-xs text-gray-400">정상 완료</span>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-button
              v-if="record.status === 'FAIL' || record.status === 'FAILURE'"
              size="small"
              type="link"
              danger
              :loading="record.retrying"
              @click="handleRetry(record)"
            >
              재시도
            </a-button>
            <span v-else class="text-xs text-gray-300">-</span>
          </template>
        </template>
      </BasicTable>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTable, useTable } from '/@/components/Table';
  import { message } from 'ant-design-vue';
  import { getQuartzJobLogs, retryQuartzJob } from './quartz.api';

  const [registerLogTable] = useTable({
    showIndexColumn: false,
    canResize: false,
  });

  const modalTitle = ref('스케줄러 실행 이력 로그');
  const targetJob = ref<any>(null);
  const loading = ref(false);
  const logList = ref<any[]>([]);
  const filterStatus = ref('');
  const searchKeyword = ref('');

  const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    pageSizeOptions: ['10', '20', '50'],
    showTotal: (total: number) => `총 ${total}건`,
  });

  const tableColumns = [
    { title: '작업명', key: 'botName', width: 200, align: 'left' },
    { title: '실행 유형', key: 'executionType', width: 95, align: 'center' },
    { title: '시작 일시', key: 'startTime', width: 140, align: 'center' },
    { title: '소요 시간', key: 'durationMs', width: 90, align: 'right' },
    { title: '처리 실적', key: 'counts', width: 140, align: 'center' },
    { title: '상태', key: 'status', width: 95, align: 'center' },
    { title: '결과 메시지 / 오류', key: 'errorMessage', align: 'left' },
    { title: '관리', key: 'action', width: 80, align: 'center' },
  ];

  const [registerModal] = useModalInner((data) => {
    targetJob.value = data?.record || null;
    filterStatus.value = '';
    searchKeyword.value = '';
    pagination.current = 1;

    if (targetJob.value) {
      modalTitle.value = `[${targetJob.value.description || targetJob.value.jobClassName}] 실행 이력 로그`;
    } else {
      modalTitle.value = '전체 스케줄러 실행 이력 로그';
    }

    fetchLogs();
  });

  async function fetchLogs() {
    loading.value = true;
    try {
      const params: any = {
        pageNo: pagination.current,
        pageSize: pagination.pageSize,
      };

      if (targetJob.value) {
        if (targetJob.value.jobClassName) {
          params.jobClassName = targetJob.value.jobClassName;
        }
        if (targetJob.value.botCode) {
          params.botCode = targetJob.value.botCode;
        }
      }

      if (filterStatus.value) {
        params.status = filterStatus.value;
      }

      if (searchKeyword.value) {
        params.keyword = searchKeyword.value;
      }

      const res = await getQuartzJobLogs(params);
      if (res && res.records) {
        logList.value = res.records;
        pagination.total = res.total || 0;
      } else if (res && Array.isArray(res)) {
        logList.value = res;
        pagination.total = res.length;
      } else {
        logList.value = [];
        pagination.total = 0;
      }
    } catch (err: any) {
      message.error(err?.message || '실행 로그를 조회하지 못했습니다.');
    } finally {
      loading.value = false;
    }
  }

  function handleSearch() {
    pagination.current = 1;
    fetchLogs();
  }

  function handleTableChange(p: any) {
    pagination.current = p.current;
    pagination.pageSize = p.pageSize;
    fetchLogs();
  }

  async function handleRetry(record: any) {
    record.retrying = true;
    try {
      await retryQuartzJob(record.id);
      message.success(`[${record.botName || record.botCode}] 작업이 재시도되었습니다.`);
      setTimeout(() => {
        fetchLogs();
      }, 500);
    } catch (err: any) {
      message.error(err?.message || '재시도 요청에 실패했습니다.');
    } finally {
      record.retrying = false;
    }
  }
</script>
