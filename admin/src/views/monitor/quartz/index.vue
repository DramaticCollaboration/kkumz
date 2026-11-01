<template>
  <div class="p-2">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <div class="flex items-center gap-2">
          <a-button preIcon="ant-design:plus-outlined" type="primary" class="rounded-md" @click="handleAdd">
            등록
          </a-button>
          <a-button preIcon="ant-design:history-outlined" type="default" class="rounded-md" @click="handleOpenAllLogs">
            실행 로그
          </a-button>
          <a-button type="default" preIcon="ant-design:export-outlined" class="rounded-md" @click="onExportXls">
            내보내기
          </a-button>
          <j-upload-button type="default" preIcon="ant-design:import-outlined" class="rounded-md" @click="onImportXls">
            가져오기
          </j-upload-button>
          <a-dropdown v-if="selectedRowKeys.length > 0">
            <template #overlay>
              <a-menu>
                <a-menu-item key="1" @click="batchHandleDelete">
                  <Icon icon="ant-design:delete-outlined"></Icon>
                  선택 삭제
                </a-menu-item>
              </a-menu>
            </template>
            <a-button class="rounded-md">
              일괄 작업
              <Icon icon="mdi:chevron-down"></Icon>
            </a-button>
          </a-dropdown>
        </div>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <QuartzModal @register="registerModal" @success="reload" />
    <QuartzLogModal @register="registerLogModal" />
  </div>
</template>
<script lang="ts" name="monitor-quartz" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { getQuartzList, deleteQuartz, batchDeleteQuartz, executeImmediately, resumeJob, pauseJob, getExportUrl, getImportUrl } from './quartz.api';
  import { columns, searchFormSchema } from './quartz.data';
  import QuartzModal from './QuartzModal.vue';
  import QuartzLogModal from './QuartzLogModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const [registerLogModal, { openModal: openLogModal }] = useModal();

  // 목록 페이지 공개 매개변수 및 메소드
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    designScope: 'quartz-template',
    tableProps: {
      title: '작업 목록',
      api: getQuartzList,
      columns: columns,
      actionColumn: {
        width: 250,
        title: '관리',
      },
      formConfig: {
        labelWidth: 100,
        schemas: searchFormSchema,
        fieldMapToTime: [['fieldTime', ['beginDate', 'endDate'], 'YYYY-MM-DD HH:mm:ss']],
      },
    },
    exportConfig: {
      name: '스케줄러 작업 목록',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * 연산 열 정의
   * @param record
   */
  function getActions(record) {
    return [
      {
        label: '즉시 실행',
        color: 'primary',
        popConfirm: {
          title: `[${record.description || record.jobClassName}] 작업을 즉시 실행하시겠습니까?`,
          confirm: handlerExecute.bind(null, record),
        },
      },
      {
        label: '스케줄 시작',
        popConfirm: {
          title: `[${record.description || record.jobClassName}] 작업의 자동 스케줄을 시작(활성화)하시겠습니까?`,
          confirm: handlerResume.bind(null, record),
        },
        ifShow: (_action) => {
          return record.status == -1;
        },
      },
      {
        label: '스케줄 정지',
        popConfirm: {
          title: `[${record.description || record.jobClassName}] 작업의 자동 스케줄을 일시정지하시겠습니까?`,
          confirm: handlerPause.bind(null, record),
        },
        ifShow: (_action) => {
          return record.status == 0;
        },
      },
      {
        label: '실행 로그',
        onClick: handleOpenJobLogs.bind(null, record),
      },
    ];
  }

  /**
   * 드롭다운 작업 표시줄
   */
  function getDropDownAction(record) {
    return [
      {
        label: '수정',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: '삭제',
        popConfirm: {
          title: '선택한 작업을 삭제하시겠습니까?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }

  /**
   * 실행 이력 로그 모달 (단일 잡)
   */
  function handleOpenJobLogs(record) {
    openLogModal(true, { record });
  }

  /**
   * 실행 이력 로그 모달 (전체 잡 통합)
   */
  function handleOpenAllLogs() {
    openLogModal(true, { record: null });
  }

  /**
   * 새로운 이벤트
   */
  function handleAdd() {
    openModal(true, {
      isUpdate: false,
    });
  }

  /**
   * 이벤트 편집
   */
  function handleEdit(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * 이벤트 삭제
   */
  async function handleDelete(record) {
    await deleteQuartz({ id: record.id }, reload);
  }

  /**
   * 즉시 실행
   */
  async function handlerExecute(record) {
    await executeImmediately({ id: record.id }, reload);
  }

  /**
   * 정지시키다
   */
  async function handlerPause(record) {
    await pauseJob({ id: record.id }, reload);
  }

  /**
   * 시작
   */
  async function handlerResume(record) {
    await resumeJob({ id: record.id }, reload);
  }

  /**
   * 일괄 삭제 이벤트
   */
  async function batchHandleDelete() {
    await batchDeleteQuartz({ ids: selectedRowKeys.value }, reload);
  }
</script>
