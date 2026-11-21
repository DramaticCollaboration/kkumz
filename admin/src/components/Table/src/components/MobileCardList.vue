<template>
  <div class="sync-mobile-card-list">
    <div v-if="typeof headerRender === 'function'" class="sync-mobile-card-header mb-2.5 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-700 p-2.5">
      <MobileHeaderRenderer :renderFn="headerRender" />
    </div>
    <a-spin :spinning="Boolean(loading)">
      <div v-if="!dataSource || dataSource.length === 0" class="py-10 text-center">
        <a-empty :description="t('appUi.kc2f5b1c9')" />
      </div>

      <div v-else class="flex flex-col gap-2.5">
        <div
          v-for="(record, rowIndex) in dataSource"
          :key="resolveRowKey(record, rowIndex)"
          class="sync-mobile-card rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 p-3.5 shadow-xs transition-colors"
          @click="$emit('row-click', record, rowIndex)"
        >
          <!-- Card Header: Title Column + Status/Tag Column -->
          <div class="flex items-start justify-between gap-2 pb-2.5 mb-2.5 border-b border-slate-100 dark:border-slate-800">
            <div class="min-w-0 flex-1">
              <div v-if="titleColumn" class="text-[14px] font-bold text-slate-900 dark:text-slate-100 break-keep leading-snug">
                <MobileCellRenderer
                  :column="titleColumn"
                  :record="record"
                  :index="rowIndex"
                  :bodyCellSlot="$slots.bodyCell"
                  :parentSlots="$slots"
                />
              </div>
              <div v-else class="text-[14px] font-bold text-slate-900 dark:text-slate-100">
                #{{ rowIndex + 1 }}
              </div>
            </div>
            <div v-if="badgeColumn" class="shrink-0">
              <MobileCellRenderer
                :column="badgeColumn"
                :record="record"
                :index="rowIndex"
                :bodyCellSlot="$slots.bodyCell"
                :parentSlots="$slots"
              />
            </div>
          </div>

          <!-- Card Body: Key-Value Metadata Pairs -->
          <div class="grid grid-cols-1 gap-1.5 text-[13px]">
            <div
              v-for="col in detailColumns"
              :key="String(col.key || col.dataIndex || col.customTitle || col.title)"
              class="flex items-start justify-between gap-3 py-0.5"
            >
              <span class="text-slate-500 dark:text-slate-400 text-[12px] shrink-0 max-w-[42%] break-keep">
                {{ col.customTitle || col.title || col.dataIndex }}
              </span>
              <div class="text-right text-slate-800 dark:text-slate-200 min-w-0 flex-1 break-words">
                <MobileCellRenderer
                  :column="col"
                  :record="record"
                  :index="rowIndex"
                  :bodyCellSlot="$slots.bodyCell"
                  :parentSlots="$slots"
                />
              </div>
            </div>
          </div>

          <!-- Card Footer: Action Column -->
          <div
            v-if="actionColumn"
            class="mt-3 pt-2.5 border-t border-slate-100 dark:border-slate-800 flex items-center justify-end gap-1.5 flex-wrap"
            @click.stop
          >
            <MobileCellRenderer
              :column="actionColumn"
              :record="record"
              :index="rowIndex"
              :bodyCellSlot="$slots.bodyCell"
              :parentSlots="$slots"
            />
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="paginationObj" class="mt-3 flex justify-center">
        <a-pagination
          :current="paginationObj.current"
          :page-size="paginationObj.pageSize"
          :total="paginationObj.total"
          size="small"
          simple
          @change="handlePageChange"
        />
      </div>
    </a-spin>
  </div>
</template>

<script lang="ts">
  import { KOREAN_INDEX_COLUMN_TITLE, STATUS_FIELD_NAME_RE } from '/@/locales/koPatterns';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { defineComponent, computed, h, Comment, Text, Fragment, PropType, VNode } from 'vue';
  import { Spin, Empty, Pagination } from 'ant-design-vue';
  import { ACTION_COLUMN_FLAG, INDEX_COLUMN_FLAG } from '../const';

  function getNestedValue(record: Record<string, any>, dataIndex: any): any {
    if (!record || dataIndex === undefined || dataIndex === null) return undefined;
    if (Array.isArray(dataIndex)) {
      return dataIndex.reduce((acc, k) => (acc != null ? acc[k] : undefined), record);
    }
    if (typeof dataIndex === 'string' && dataIndex.includes('.')) {
      return dataIndex.split('.').reduce((acc, k) => (acc != null ? acc[k] : undefined), record);
    }
    return record[dataIndex];
  }

  function hasMeaningfulVNodes(nodes: VNode[] | undefined): boolean {
    if (!nodes || nodes.length === 0) return false;
    return nodes.some((node) => {
      if (!node) return false;
      if (node.type === Comment) return false;
      if (node.type === Text) {
        return String(node.children ?? '').trim().length > 0;
      }
      if (node.type === Fragment) {
        return Array.isArray(node.children) && hasMeaningfulVNodes(node.children as VNode[]);
      }
      return true;
    });
  }

  const MobileHeaderRenderer = defineComponent({
    name: 'MobileHeaderRenderer',
    props: {
      renderFn: { type: Function as PropType<() => any>, required: true },
    },
    setup(props) {
      return () => props.renderFn();
    },
  });

  const MobileCellRenderer = defineComponent({
    name: 'MobileCellRenderer',
    props: {
      column: { type: Object as PropType<Record<string, any>>, required: true },
      record: { type: Object as PropType<Record<string, any>>, required: true },
      index: { type: Number, required: true },
      bodyCellSlot: { type: Function as PropType<any>, default: undefined },
      parentSlots: { type: Object as PropType<Record<string, any>>, default: () => ({}) },
    },
    setup(props) {
      return () => {
        const col = props.column;
        const record = props.record;
        const index = props.index;
        const text = getNestedValue(record, col.dataIndex);

        const namedSlotKey = col.slotsBak?.customRender;
        if (namedSlotKey && props.parentSlots?.[namedSlotKey]) {
          const out = props.parentSlots[namedSlotKey]({ text, value: text, record, index, column: col });
          if (hasMeaningfulVNodes(out)) return out;
        }

        if (props.bodyCellSlot) {
          const out = props.bodyCellSlot({ text, value: text, record, index, column: col });
          if (hasMeaningfulVNodes(out)) return out;
        }

        if (typeof col.customRender === 'function') {
          const rendered = col.customRender({ text, value: text, record, index, column: col });
          if (rendered !== undefined && rendered !== null && rendered !== '') {
            return rendered;
          }
        }

        if (text === undefined || text === null || text === '') {
          return h('span', { class: 'text-slate-400' }, '-');
        }
        return h('span', {}, String(text));
      };
    },
  });

  export default defineComponent({
    name: 'MobileCardList',
    components: {
      [Spin.name]: Spin,
      [Empty.name]: Empty,
      [Pagination.name]: Pagination,
      MobileCellRenderer,
      MobileHeaderRenderer,
    },
    props: {
      dataSource: { type: Array as PropType<Record<string, any>[]>, default: () => [] },
      columns: { type: Array as PropType<Record<string, any>[]>, default: () => [] },
      loading: { type: [Boolean, Object] as PropType<boolean | Record<string, any>>, default: false },
      pagination: { type: [Boolean, Object] as PropType<boolean | Record<string, any>>, default: false },
      rowKey: { type: [String, Function] as PropType<string | ((record: any) => string)>, default: 'id' },
      headerRender: { type: Function as PropType<(() => any) | undefined>, default: undefined },
    },
    emits: ['change', 'row-click'],
    setup(props, { emit }) {
      const { t } = useI18n();
      const actionColumn = computed(() =>
        props.columns.find((c) => c.flag === ACTION_COLUMN_FLAG || c.key === 'action' || c.dataIndex === 'action')
      );

      const dataColumns = computed(() =>
        props.columns.filter((c) => {
          if (c.defaultHidden) return false;
          if (c.flag === INDEX_COLUMN_FLAG || c.flag === ACTION_COLUMN_FLAG) return false;
          if (c.key === 'action' || c.dataIndex === 'action' || c.key === 'row-selection-column') return false;
          if (c.dataIndex === 'rowIndex' || c.title === '#' || c.customTitle === KOREAN_INDEX_COLUMN_TITLE || c.customTitle === 'Index') return false;
          return true;
        })
      );

      const STATUS_FIELD_RE = STATUS_FIELD_NAME_RE;
      const badgeColumn = computed(() =>
        dataColumns.value.find((c) => {
          if (c.mobileBadge === true) return true;
          const fieldMeta = `${String(c.dataIndex || c.key || '')} ${String(c.customTitle || c.title || '')}`;
          return STATUS_FIELD_RE.test(fieldMeta);
        })
      );

      const titleColumn = computed(() => {
        const cols = dataColumns.value;
        if (cols.length === 0) return null;
        return cols.find((c) => c !== badgeColumn.value) || cols[0];
      });

      const detailColumns = computed(() =>
        dataColumns.value.filter((c) => c !== titleColumn.value && c !== badgeColumn.value).slice(0, 6)
      );

      const paginationObj = computed(() => {
        if (!props.pagination || typeof props.pagination === 'boolean') return null;
        return props.pagination;
      });

      function resolveRowKey(record: Record<string, any>, idx: number): string | number {
        if (typeof props.rowKey === 'function') {
          return props.rowKey(record) ?? idx;
        }
        return record?.[props.rowKey] ?? record?.id ?? idx;
      }

      function handlePageChange(page: number, pageSize: number) {
        emit('change', { ...(paginationObj.value || {}), current: page, pageSize }, {}, {}, { action: 'paginate' });
      }

      return {
        t,
        actionColumn,
        badgeColumn,
        titleColumn,
        detailColumns,
        paginationObj,
        resolveRowKey,
        handlePageChange,
      };
    },
  });
</script>

<style lang="less" scoped>
  .sync-mobile-card-list {
    width: 100%;
    padding: 4px 0;
  }

  .sync-mobile-card:active {
    background-color: #f8fafc;
  }

  html[data-theme='dark'] .sync-mobile-card:active {
    background-color: #1e293b;
  }
</style>
