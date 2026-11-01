<template>
  <!--팝업 선택 상자-->
  <div>
    <BasicModal
      v-bind="$attrs"
      @register="register"
      :title="title"
      :width="1200"
      @ok="handleSubmit"
      @cancel="handleCancel"
      cancelText="취소"
      wrapClassName="j-popup-modal"
      @visible-change="visibleChange"
    >
      <div class="sync-basic-table-form-container" v-if="showSearchFlag">
        <a-form ref="formRef" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol" @keyup.enter.native="searchQuery">
          <a-row :gutter="24">
            <template v-for="(item, index) in queryInfo">
              <template v-if="item.hidden === '1'">
                <a-col :md="8" :sm="24" :key="'query' + index" v-show="toggleSearchStatus">
                  <SearchFormItem :formElRef="formRef" :queryParam="queryParam" :item="item" :dictOptions="dictOptions"></SearchFormItem>
                </a-col>
              </template>
              <template v-else>
                <a-col :md="8" :sm="24" :key="'query' + index">
                  <SearchFormItem :formElRef="formRef" :queryParam="queryParam" :item="item" :dictOptions="dictOptions"></SearchFormItem>
                </a-col>
              </template>
            </template>

            <a-col :md="8" :sm="8" v-if="showAdvancedButton">
              <span style="float: left; overflow: hidden" class="table-page-search-submitButtons">
                <a-col :lg="6">
                  <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset">새로고침</a-button>
                  <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery" style="margin-left: 8px">검색</a-button>
                  <a @click="handleToggleSearch" style="margin-left: 8px">
                    {{ toggleSearchStatus ? '닫기' : '확장' }}
                    <Icon :icon="toggleSearchStatus ? 'ant-design:up-outlined' : 'ant-design:down-outlined'" />
                  </a>
                </a-col>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <BasicTable
        ref="tableRef"
        :canResize="false"
        :bordered="true"
        :loading="loading"
        :rowKey="combineRowKey"
        :columns="columns"
        :showIndexColumn="false"
        :dataSource="dataSource"
        :pagination="pagination"
        :rowSelection="rowSelection"
        @row-click="clickThenCheck"
        @change="handleChangeInTable"
      >
        <template #tableTitle></template>
      </BasicTable>
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { defineComponent, unref, ref, watch, watchEffect, reactive, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { usePopBiz } from '/@/components/sync/OnLine/hooks/usePopBiz';
  import { useMessage } from '/@/hooks/web/useMessage';

  export default defineComponent({
    name: 'JPopupOnlReportModal',
    components: {
      //BasicTable은 여기에서 비동기식으로 로드되어야 합니다.
      BasicModal,
      SearchFormItem: createAsyncComponent(() => import('/@/components/sync/OnLine/SearchFormItem.vue'), { loading: false }),
      BasicTable: createAsyncComponent(() => import('/@/components/Table/src/BasicTable.vue'), {
        loading: true,
      }),
    },
    props: ['multi', 'code', 'sorter', 'groupId', 'param','showAdvancedButton'],
    emits: ['ok', 'register'],
    setup(props, { emit, refs }) {
      const { createMessage } = useMessage();
      const labelCol = reactive({
        xs: { span: 24 },
        sm: { span: 6 },
      });
      const wrapperCol = reactive({
        xs: { span: 24 },
        sm: { span: 18 },
      });
      //등록 팝업 상자
      const [register, { closeModal }] = useModalInner();
      const formRef = ref();
      const tableRef = ref();
      const toggleSearchStatus = ref(false);
      const attrs = useAttrs();
      const tableScroll = ref({ x: true });
      const getBindValue = computed(() => {
        return Object.assign({}, unref(props), unref(attrs));
      });
      const [
        {
          visibleChange,
          loadColumnsInfo,
          dynamicParamHandler,
          loadData,
          handleChangeInTable,
          combineRowKey,
          clickThenCheck,
          filterUnuseSelect,
          getOkSelectRows,
        },
        {
          visible,
          rowSelection,
          checkedKeys,
          selectRows,
          pagination,
          dataSource,
          columns,
          loading,
          title,
          iSorter,
          queryInfo,
          queryParam,
          dictOptions,
        },
      ] = usePopBiz(getBindValue,tableRef);

      const showSearchFlag = computed(() => unref(queryInfo) && unref(queryInfo).length > 0);
      /**
       *코드 듣기
       */
      watch(
        () => props.code,
        () => {
          loadColumnsInfo();
        }
      );
      /**
       *팝업 동적 매개변수 모니터링 및 시스템 변수 구문 지원
       */
      watch(
        () => props.param,
        () => {
          if (visible.value) {
            dynamicParamHandler();
            loadData();
          }
        }
      );
      /**
       *분류기 정렬 필드 듣기
       */
      watchEffect(() => {
        if (props.sorter) {
          let arr = props.sorter.split('=');
          if (arr.length === 2 && ['asc', 'desc'].includes(arr[1].toLowerCase())) {
            iSorter.value = { column: arr[0], order: arr[1].toLowerCase() };
            // 정렬 필드 제어
            unref(columns).forEach((col) => {
              if (col.dataIndex === unref(iSorter).column) {
                col['sortOrder'] = unref(iSorter).order === 'asc' ? 'ascend' : 'descend';
              } else {
                col['sortOrder'] = false;
              }
            });
          } else {
            console.warn('【JPopup】분류기 매개변수가 잘못되었습니다.');
          }
        }
      });

      watch(
        () => pagination.current,
        (current) => {
          if (current) {
            tableRef.value.setPagination({
              current: current,
            });
          }
        }
      );

      function handleToggleSearch() {
        toggleSearchStatus.value = !unref(toggleSearchStatus);
      }
      /**
       *  취소/닫기
       */
      function handleCancel() {
        closeModal();
        checkedKeys.value = [];
        selectRows.value = [];
        tableRef.value.clearSelectedRowKeys();
      }

      /**
       *제출 확인
       */
      function handleSubmit() {
        filterUnuseSelect();
        if (!props.multi && unref(selectRows) && unref(selectRows).length > 1) {
          createMessage.warning('하나의 레코드만 선택할 수 있습니다.');
          return false;
        }
        if (!unref(selectRows) || unref(selectRows).length == 0) {
          createMessage.warning('레코드를 하나 이상 선택하세요.');
          return false;
        }
        let rows = getOkSelectRows!();
        emit('ok', rows);
        handleCancel();
      }

      /**
       * 쿼리
       */
      function searchQuery() {
        loadData(1);
      }
      /**
       * 재설정
       */
      function searchReset() {
        queryParam.value = {};
        loadData(1);
      }
      return {
        attrs,
        register,
        tableScroll,
        dataSource,
        pagination,
        columns,
        rowSelection,
        checkedKeys,
        loading,
        title,
        handleCancel,
        handleSubmit,
        clickThenCheck,
        loadData,
        combineRowKey,
        handleChangeInTable,
        visibleChange,
        queryInfo,
        queryParam,
        tableRef,
        formRef,
        labelCol,
        wrapperCol,
        dictOptions,
        showSearchFlag,
        toggleSearchStatus,
        handleToggleSearch,
        searchQuery,
        searchReset,
      };
    },
  });
</script>

<style lang="less" scoped>
  .sync-basic-table-form-container {
    padding: 5px;

    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 0;
      white-space: nowrap;
    }
  }
  :deep(.sync-basic-table .ant-table-wrapper .ant-table-title){
    min-height: 0;
  }
</style>
