<template>
  <BasicTable :ellipsis="true" @register="registerTable" :searchInfo="searchInfo" :columns="logColumns" :expand-column-width="16">
    <template #tableTitle>
      <a-tabs defaultActiveKey="4" @change="tabChange" size="small">
        <a-tab-pane tab="예외 로그" key="4"></a-tab-pane>
        <a-tab-pane tab="로그인 로그" key="1"></a-tab-pane>
        <a-tab-pane tab="작업 로그" key="2"></a-tab-pane>
      </a-tabs>
    </template>
    <template #expandedRowRender="{ record }">
      <div v-if="searchInfo.logType == 2">
        <div style="margin-bottom: 5px">
          <a-badge status="success" style="vertical-align: middle" />
          <span style="vertical-align: middle">요청 방법:{{ record.method }}</span></div
        >
        <div>
          <a-badge status="processing" style="vertical-align: middle" />
          <span style="vertical-align: middle">요청 매개변수:{{ record.requestParam }}</span></div
        >
      </div>
      <div v-if="searchInfo.logType == 4">
        <div style="margin-bottom: 5px">
          <a-badge status="success" style="vertical-align: middle" />
          <span class="error-box" style="vertical-align: middle">예외 스택:{{ record.requestParam }}</span>
        </div>
      </div>
    </template>
  </BasicTable>
</template>
<script lang="ts" name="monitor-log" setup>
  import { ref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { getLogList } from './log.api';
  import {
    columns,
    searchFormSchema,
    operationLogColumn,
    operationSearchFormSchema,
    exceptionColumns
  } from './log.data';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  const { createMessage } = useMessage();
  const checkedKeys = ref<Array<string | number>>([]);

  const logColumns = ref<any>(columns);
  const searchSchema = ref<any>(searchFormSchema);
  const searchInfo = { logType: '4' };
  // 목록 페이지 공개 매개변수 및 메소드
  const { prefixCls, tableContext } = useListPage({
    designScope: 'user-list',
    tableProps: {
      title: '로그 목록',
      api: getLogList,
      expandRowByClick: true,
      showActionColumn: false,
      rowSelection: {
        columnWidth: 20,
      },
      formConfig: {
        schemas: searchSchema,
        fieldMapToTime: [['fieldTime', ['createTime_begin', 'createTime_end'], 'YYYY-MM-DD']],
      },
    },
  });

  const [registerTable, { reload }] = tableContext;

  // 로그 유형
  function tabChange(key) {
    searchInfo.logType = key;
    if (key == '2') {
      logColumns.value = operationLogColumn;
      searchSchema.value = operationSearchFormSchema;
    }else if(key == '4'){
      searchSchema.value = searchFormSchema;
      logColumns.value = exceptionColumns;
    } else {
      searchSchema.value = searchFormSchema;
      logColumns.value = columns;
    }
    reload();
  }

  /**
   * 이벤트 선택
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }
</script>
<style lang="less" scoped>
  .error-box {
    white-space: break-spaces;
  }
</style>
