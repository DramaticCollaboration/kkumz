<template>
  <div ref="wrapRef" :class="getWrapperClass">
    <BasicForm
      :class="{ 'table-search-area-hidden': !getBindValues.formConfig?.schemas?.length }"
      submitOnReset
      v-bind="getFormProps"
      v-if="getBindValues.useSearchForm"
      :tableAction="tableAction"
      @register="registerForm"
      @submit="handleSearchInfoChange"
      @advanced-change="redoHeight"
    >
      <template #[replaceFormSlotKey(item)]="data" v-for="item in getFormSlotKeys">
        <slot :name="item" v-bind="data || {}"></slot>
      </template>
    </BasicForm>

    <!-- antd v3 업그레이드와 호환되어 데이터 수집 방지 및 콘솔 오류 방지 -->
    <!-- https://antdv.com/docs/vue/migration-v3-cn -->
    <a-form-item-rest>
      <!-- 【TV360X-377】관련 레코드가 필요하며, 이는 테이블의 입력 상자 및 페이지 번호 스타일에 영향을 미칩니다. -->
      <a-form-item>
        <Table ref="tableElRef" v-bind="getBindValues" :rowClassName="getRowClassName" v-show="getEmptyDataIsShowTable" @resizeColumn="handleResizeColumn" @change="handleTableChange">
          <!-- antd의 기본 슬롯은 직접 전달됩니다. -->
          <template #[item]="data" v-for="item in slotNamesGroup.native" :key="item">
            <template v-if="item === 'headerCell'">
              <CustomSelectHeader v-if="isCustomSelection(data.column)" v-bind="selectHeaderProps" />
              <slot v-else :name="item" v-bind="data || {}"></slot>
            </template>
            <slot v-else :name="item" v-bind="data || {}"></slot>
          </template>
          <template #headerCell="{ column }">
            <CustomSelectHeader v-if="isCustomSelection(column)" v-bind="selectHeaderProps"/>
            <HeaderCell v-else :column="column" />
          </template>
          <!-- antdv3About 지원을 추가합니다.x 호환 -->
          <template #bodyCell="data">
            <template v-if="data.column?.slotsBak?.customRender">
              <slot :name="data.column.slotsBak.customRender" v-bind="data || {}"></slot>
            </template>
            <template v-else>
              <slot name="bodyCell" v-bind="data || {}"></slot>
            </template>
          </template>
          <template v-if="showSummaryRef && !getBindValues.showSummary" #summary="data">
            <slot name="summary" v-bind="data || {}">
              <TableSummary :data="data || {}" v-bind="getSummaryProps" />
            </slot>
          </template>
        </Table>
      </a-form-item>
    </a-form-item-rest>
  </div>
</template>
<script lang="ts">
  import type { BasicTableProps, TableActionType, SizeType, ColumnChangeParam, BasicColumn } from './types/table';

  import { defineComponent, ref, computed, unref, toRaw, inject, watchEffect, watch, onUnmounted, onMounted, nextTick } from 'vue';
  import { Table } from 'ant-design-vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { PageWrapperFixedHeightKey } from '/@/components/Page/injectionKey';
  import CustomSelectHeader from './components/CustomSelectHeader.vue'
  import expandIcon from './components/ExpandIcon';
  import HeaderCell from './components/HeaderCell.vue';
  import TableSummary from './components/TableSummary';
  import { InnerHandlers } from './types/table';
  import { usePagination } from './hooks/usePagination';
  import { useColumns } from './hooks/useColumns';
  import { useDataSource } from './hooks/useDataSource';
  import { useLoading } from './hooks/useLoading';
  import { useRowSelection } from './hooks/useRowSelection';
  import { useTableScroll } from './hooks/useTableScroll';
  import { useCustomRow } from './hooks/useCustomRow';
  import { useTableStyle } from './hooks/useTableStyle';
  import { useTableHeader } from './hooks/useTableHeader';
  import { useTableExpand } from './hooks/useTableExpand';
  import { createTableContext } from './hooks/useTableContext';
  import { useTableFooter } from './hooks/useTableFooter';
  import { useTableForm } from './hooks/useTableForm';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useCustomSelection } from "./hooks/useCustomSelection";

  import { omit, pick } from 'lodash-es';
  import { basicProps } from './props';
  import { isFunction } from '/@/utils/is';
  import { warn } from '/@/utils/log';

  export default defineComponent({
    components: {
      Table,
      BasicForm,
      HeaderCell,
      TableSummary,
      CustomSelectHeader,
    },
    props: basicProps,
    emits: [
      'fetch-success',
      'fetch-error',
      'selection-change',
      'register',
      'row-click',
      'row-dbClick',
      'row-contextmenu',
      'row-mouseenter',
      'row-mouseleave',
      'edit-end',
      'edit-cancel',
      'edit-row-end',
      'edit-change',
      'expanded-rows-change',
      'change',
      'columns-change',
      'table-redo',
    ],
    setup(props, { attrs, emit, slots, expose }) {
      const tableElRef = ref(null);
      const tableData = ref<Recordable[]>([]);

      const wrapRef = ref(null);
      const innerPropsRef = ref<Partial<BasicTableProps>>();

      const { prefixCls } = useDesign('basic-table');
      const [registerForm, formActions] = useForm();

      const getProps = computed(() => {
        return { ...props, ...unref(innerPropsRef) } as BasicTableProps;
      });

      const isFixedHeightPage = inject(PageWrapperFixedHeightKey, false);
      watchEffect(() => {
        unref(isFixedHeightPage) &&
          props.canResize &&
          warn("'canResize' of BasicTable may not work in PageWrapper with 'fixedHeight' (especially in hot updates)");
      });

      const { getLoading, setLoading } = useLoading(getProps);
      const { getPaginationInfo, getPagination, setPagination, setShowPagination, getShowPagination } = usePagination(getProps);


      // const { getRowSelection, getRowSelectionRef, getSelectRows, clearSelectedRowKeys, getSelectRowKeys, deleteSelectRowByKey, setSelectedRowKeys } =
      //   useRowSelection(getProps, tableData, emit);

      // 하위 열 이름
      const childrenColumnName = computed(() => getProps.value.childrenColumnName || 'children');

      // 맞춤 선택 열
      const {
        getRowSelection,
        getSelectRows,
        getSelectRowKeys,
        setSelectedRowKeys,
        getRowSelectionRef,
        selectHeaderProps,
        isCustomSelection,
        handleCustomSelectColumn,
        clearSelectedRowKeys,
        deleteSelectRowByKey,
        getExpandIconColumnIndex,
      } = useCustomSelection(
        getProps,
        emit,
        wrapRef,
        getPaginationInfo,
        tableData,
        childrenColumnName
      )

      const {
        handleTableChange: onTableChange,
        getDataSourceRef,
        getDataSource,
        getRawDataSource,
        setTableData,
        updateTableDataRecord,
        deleteTableDataRecord,
        insertTableDataRecord,
        findTableDataRecord,
        fetch,
        getRowKey,
        reload,
        getAutoCreateKey,
        updateTableData,
      } = useDataSource(
        getProps,
        {
          tableData,
          getPaginationInfo,
          setLoading,
          setPagination,
          validate: formActions.validate,
          clearSelectedRowKeys,
        },
        emit
      );

      function handleTableChange(...args) {
        onTableChange.call(undefined, ...args);
        emit('change', ...args);
        // useTable을 통해 등록 시 onChange가 동작하지 않는 문제 해결
        const { onChange } = unref(getProps);
        onChange && isFunction(onChange) && onChange.call(undefined, ...args);
      }

      const { getViewColumns, getColumns, setCacheColumnsByField, setColumns, getColumnsRef, getCacheColumns } = useColumns(
        getProps,
        getPaginationInfo,
        handleCustomSelectColumn,
      );

      const { getScrollRef, redoHeight } = useTableScroll(getProps, tableElRef, getColumnsRef, getRowSelectionRef, getDataSourceRef);

      const { customRow } = useCustomRow(getProps, {
        setSelectedRowKeys,
        getSelectRowKeys,
        clearSelectedRowKeys,
        getAutoCreateKey,
        emit,
      });

      const { getRowClassName } = useTableStyle(getProps, prefixCls);

      const { getExpandOption, expandAll, collapseAll } = useTableExpand(getProps, tableData, emit);

      const handlers: InnerHandlers = {
        onColumnsChange: (data: ColumnChangeParam[]) => {
          emit('columns-change', data);
          // support useTable
          unref(getProps).onColumnsChange?.(data);
        },
      };

      const { getHeaderProps } = useTableHeader(getProps, slots, handlers);
      const getSummaryProps = computed(() => {
        return pick(unref(getProps), ['summaryFunc', 'summaryData', 'hasExpandedRow', 'rowKey']);
      });
      const getIsEmptyData = computed(() => {
        return (unref(getDataSourceRef) || []).length === 0;
      });
      const showSummaryRef = computed(() => {
        const summaryProps = unref(getSummaryProps);
        return (summaryProps.summaryFunc || summaryProps.summaryData) && !unref(getIsEmptyData);
      });

      const { getFooterProps } = useTableFooter(getProps, slots, getScrollRef, tableElRef, getDataSourceRef);

      const { getFormProps, replaceFormSlotKey, getFormSlotKeys, handleSearchInfoChange } = useTableForm(getProps, slots, fetch, getLoading);

      const getBindValues = computed(() => {
        const dataSource = unref(getDataSourceRef);
        let propsData: Recordable = {
          // ...(dataSource.length === 0 ? { getPopupContainer: () => document.body } : {}),
          ...attrs,
          customRow,
          //트리 목록 확장은 AntDesignVue의 기본 플러스 및 마이너스 아이콘을 사용합니다. 작성자:scott 날짜:20210914
          //expandIcon: slots.expandIcon ? null : expandIcon(),
          ...unref(getProps),
          ...unref(getHeaderProps),
          scroll: unref(getScrollRef),
          loading: unref(getLoading),
          tableLayout: 'fixed',
          rowSelection: unref(getRowSelectionRef),
          rowKey: unref(getRowKey),
          columns: toRaw(unref(getViewColumns)),
          pagination: toRaw(unref(getPaginationInfo)),
          dataSource,
          footer: unref(getFooterProps),
          ...unref(getExpandOption),
          // 【QQYUN-5837】ExpandIconColumnIndex를 동적으로 계산
          expandIconColumnIndex: getExpandIconColumnIndex.value,
        };

        //슬롯에 추가 확장 줄이 있으면 스크롤이 제거되고 주석 처리됩니다.
        /*if (slots.expandedRowRender) {
          propsData = omit(propsData, 'scroll');
        }*/

        // 선택 열을 사용자 정의하려면 기본 열을 제거해야 합니다.
        delete propsData.rowSelection

        !propsData.isTreeTable && delete propsData.expandIconColumnIndex;
        propsData.expandedRowKeys === null && delete propsData.expandedRowKeys;
        propsData = omit(propsData, ['class', 'onChange']);
        return propsData;
      });

      // 테이블 열 너비를 균일하게 설정
      const getMaxColumnWidth = computed(() => {
        const values = unref(getBindValues);
        return values.maxColumnWidth > 0 ? values.maxColumnWidth + 'px' : null;
      });

      const getWrapperClass = computed(() => {
        const values = unref(getBindValues);
        return [
          prefixCls,
          attrs.class,
          {
            [`${prefixCls}-form-container`]: values.useSearchForm,
            [`${prefixCls}--inset`]: values.inset,
            [`${prefixCls}-col-max-width`]: getMaxColumnWidth.value != null,
            // 테이블 끝에 합계를 표시할지 여부
            [`${prefixCls}--show-summary`]: values.showSummary,
          },
        ];
      });

      const getEmptyDataIsShowTable = computed(() => {
        const { emptyDataIsShowTable, useSearchForm } = unref(getProps);
        if (emptyDataIsShowTable || !useSearchForm) {
          return true;
        }
        return !!unref(getDataSourceRef).length;
      });

      function setProps(props: Partial<BasicTableProps>) {
        innerPropsRef.value = { ...unref(innerPropsRef), ...props };
      }

      const tableAction: TableActionType = {
        reload,
        getSelectRows,
        clearSelectedRowKeys,
        getSelectRowKeys,
        deleteSelectRowByKey,
        setPagination,
        setTableData,
        updateTableDataRecord,
        deleteTableDataRecord,
        insertTableDataRecord,
        findTableDataRecord,
        redoHeight,
        setSelectedRowKeys,
        setColumns,
        setLoading,
        getDataSource,
        getRawDataSource,
        setProps,
        getRowSelection,
        getPaginationRef: getPagination,
        getColumns,
        getCacheColumns,
        emit,
        updateTableData,
        setShowPagination,
        getShowPagination,
        setCacheColumnsByField,
        expandAll,
        collapseAll,
        getSize: () => {
          return unref(getBindValues).size as SizeType;
        },
      };
      createTableContext({ ...tableAction, wrapRef, getBindValues });

      // 그룹화 후 슬롯 이름 가져오기
      const slotNamesGroup = computed<{
        // AntTable 기본 슬롯
        native: string[];
        // 열 맞춤 슬롯
        custom: string[];
      }>(() => {
        const native: string[] = [];
        const custom: string[] = [];
        const columns = unref<Recordable[]>(getViewColumns) as BasicColumn[];
        const allCustomRender = columns.map<string>((column) => column.slotsBak?.customRender);
        for (const name of Object.keys(slots)) {
          // 특수 슬롯 필터링
          if (['bodyCell'].includes(name)) {
            continue;
          }
          if (allCustomRender.includes(name)) {
            custom.push(name);
          } else {
            native.push(name);
          }
        }
        return { native, custom };
      });
      nextTick(() => {
        getProps.value.defaultExpandAllRows && expandAll();
      })
      expose(tableAction);

      emit('register', tableAction, formActions);


      return {
        tableElRef,
        getBindValues,
        getLoading,
        registerForm,
        handleSearchInfoChange,
        getEmptyDataIsShowTable,
        handleTableChange,
        getRowClassName,
        wrapRef,
        tableAction,
        redoHeight,
        handleResizeColumn: (w, col) => {
        console.log('col',col);
          col.width = w;
        },
        getFormProps: getFormProps as any,
        replaceFormSlotKey,
        getFormSlotKeys,
        getWrapperClass,
        getMaxColumnWidth,
        columns: getViewColumns,

        selectHeaderProps,
        isCustomSelection,
        slotNamesGroup,
        getSummaryProps,
        showSummaryRef,
      };
    },
  });
</script>
<style lang="less">
  @border-color: #cecece4d;

  @prefix-cls: ~'@{namespace}-basic-table';

  [data-theme='dark'] {
    .ant-table-tbody > tr:hover.ant-table-row-selected > td,
    .ant-table-tbody > tr.ant-table-row-selected td {
      background-color: #262626;
    }

    .@{prefix-cls} {
      //테이블 선택 도구 모음 스타일
      .alert {
        // background-color: #323232;
        // border-color: #424242;
      }
    }
  }

  .@{prefix-cls} {
    max-width: 100%;

    &-row__striped {
      td {
        background-color: @app-content-background;
      }
    }
    > .table-search-area-hidden {
      display: none;
    }
    &-form-container {
      padding: 10px;

      .ant-form {
        padding: 12px 10px 6px 10px;
        margin-bottom: 8px;
        background-color: @component-background;
        border-radius: 2px;
      }
    }

    .ant-tag {
      margin-right: 0;
    }

    .ant-picker-preset {
      .ant-tag {
        margin-right: 8px !important;
      }
    }

    .ant-table-wrapper {
      padding: 6px;
      background-color: @component-background;
      border-radius: 2px;

      .ant-table-title {
        min-height: 40px;
        padding: 0 0 8px 0 !important;
      }

      .ant-table.ant-table-bordered .ant-table-title {
        border: none !important;
      }
    }

    .ant-table {
      width: 100%;
      overflow-x: hidden;

      &-title {
        display: flex;
        padding: 8px 6px;
        border-bottom: none;
        justify-content: space-between;
        align-items: center;
      }
      //행 색상 정의
      .trcolor {
        background-color: rgba(255, 192, 203, 0.31);
        color: red;
      }

      //.ant-table-tbody > tr.ant-table-row-selected td {
      //background-color: fade(@primary-color, 8%) !important;
      //}
    }

    .ant-pagination {
      margin: 10px 0 0 0;
    }

    .ant-table-footer {
      padding: 0;

      .ant-table-wrapper {
        padding: 0;
      }

      table {
        border: none !important;
      }

      .ant-table-content {
        overflow-x: hidden !important;
        //  overflow-y: scroll !important;
      }

      td {
        padding: 12px 8px;
      }
    }
    //테이블 선택 도구 모음 스타일
    .alert {
      height: 38px;
      // background-color: #e6f7ff;
      // border-color: #91d5ff;
    }
    &--inset {
      .ant-table-wrapper {
        padding: 0;
      }
    }

    // ------ 테이블 열의 최대 너비를 균일하게 설정 ------
    &-col-max-width {
      .ant-table-thead tr th,
      .ant-table-tbody tr td {
        max-width: v-bind(getMaxColumnWidth);
      }
    }
    // ------ 테이블 열의 최대 너비를 균일하게 설정 ------

    &--show-summary {
      .ant-table > .ant-table-footer {
        padding: 12px 0 0;
      }

      .ant-table.ant-table-bordered > .ant-table-footer {
        border: 0;
      }
    }
    > .ant-form-item {
      margin-bottom: 0;
    }
  }
</style>
