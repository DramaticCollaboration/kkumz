<template>
  <aside class="help-nav-sidebar">
    <div class="help-nav-search">
      <div v-if="title" class="help-nav-title">{{ title }}</div>
      <a-input-search
        v-model:value="searchKeyword"
        :placeholder="t('appUi.k4a3992ea')"
        allow-clear
        size="small"
      />
    </div>

    <div class="help-nav-list">
      <!-- 1. 시스템 안내 고정 섹션 -->
      <div v-if="grouped.systemGuides.length > 0" class="nav-section">
        <div class="nav-section-header">
          <span class="nav-section-label">{{ t('appUi.k368c6bad') }}</span>
          <span class="nav-section-count">{{ grouped.systemGuides.length }}</span>
        </div>
        <div
          v-for="item in grouped.systemGuides"
          :key="item.id || item.pageKey"
          class="nav-item nav-item--system"
          :class="{ active: selectedPageKey === item.pageKey }"
          @click="emit('select', item)"
        >
          <div class="nav-item-top">
            <span class="nav-item-title">{{ item.title }}</span>
            <a-tag color="cyan" class="m-0 text-xs">{{ t('appUi.k2af9c732') }}</a-tag>
          </div>
          <div class="nav-item-summary">{{ item.summary }}</div>
        </div>
      </div>

      <!-- 2. 화면별 가이드 (카테고리 그룹핑) -->
      <div v-if="grouped.screenGroups.length > 0" class="nav-section">
        <div class="nav-section-header">
          <span class="nav-section-label">{{ t('appUi.k873d7153') }}</span>
          <span class="nav-section-count">{{ grouped.screenGuides.length }}</span>
        </div>
        <div v-for="group in grouped.screenGroups" :key="group.category" class="nav-category-group">
          <div class="nav-category-label">
            <span>{{ group.category }}</span>
            <span class="nav-category-count">{{ group.items.length }}</span>
          </div>
          <div
            v-for="item in group.items"
            :key="item.id || item.pageKey"
            class="nav-item"
            :class="{
              active: selectedPageKey === item.pageKey,
              'is-current': currentRoutePath && item.pageKey === currentRoutePath,
            }"
            @click="emit('select', item)"
          >
            <div class="nav-item-top">
              <span class="nav-item-title">{{ item.title }}</span>
              <a-tag
                v-if="currentRoutePath && item.pageKey === currentRoutePath"
                color="processing"
                class="m-0 text-xs"
              >
                {{ t('appUi.k0d2e1c24') }}
              </a-tag>
            </div>
            <div class="nav-item-route">{{ item.pageKey }}</div>
          </div>
        </div>
      </div>

      <a-empty
        v-if="filteredGuides.length === 0 && !loading"
        :description="t('appUi.k300d3cac')"
        class="my-6"
      />
    </div>
  </aside>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { SysPageHelpModel } from '../help.api';
  import { splitGuidesByType } from '../help.data';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const props = defineProps<{
    guides: SysPageHelpModel[];
    selectedPageKey?: string;
    currentRoutePath?: string;
    loading?: boolean;
    title?: string;
  }>();

  const emit = defineEmits<{
    (e: 'select', item: SysPageHelpModel): void;
  }>();

  const searchKeyword = ref('');

  const filteredGuides = computed(() => {
    const kw = searchKeyword.value.trim().toLowerCase();
    if (!kw) return props.guides;
    return props.guides.filter(
      (item) =>
        (item.title && item.title.toLowerCase().includes(kw)) ||
        (item.pageKey && item.pageKey.toLowerCase().includes(kw)) ||
        (item.category && item.category.toLowerCase().includes(kw)) ||
        (item.summary && item.summary.toLowerCase().includes(kw)),
    );
  });

  const grouped = computed(() => splitGuidesByType(filteredGuides.value));

  function clearSearch() {
    searchKeyword.value = '';
  }

  defineExpose({ clearSearch });
</script>

<style lang="less" scoped>
  .help-nav-sidebar {
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #ffffff;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    height: 100%;
  }

  .help-nav-search {
    padding: 10px 12px;
    border-bottom: 1px solid #f1f5f9;

    .help-nav-title {
      font-size: 13px;
      font-weight: 700;
      color: #0f172a;
      margin-bottom: 8px;
    }
  }

  .help-nav-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px;
  }

  .nav-section {
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .nav-section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 4px 6px 6px;
    border-bottom: 1px solid #f1f5f9;
    margin-bottom: 6px;

    .nav-section-label {
      font-size: 11px;
      font-weight: 700;
      color: #475569;
      letter-spacing: 0.02em;
    }

    .nav-section-count {
      font-size: 11px;
      color: #64748b;
      background: #f1f5f9;
      border-radius: 10px;
      padding: 0 7px;
      line-height: 18px;
    }
  }

  .nav-category-group {
    margin-bottom: 8px;

    .nav-category-label {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 4px 8px;
      font-size: 11px;
      font-weight: 600;
      color: #64748b;

      .nav-category-count {
        font-size: 10px;
        color: #94a3b8;
      }
    }
  }

  .nav-item {
    padding: 8px 10px;
    border-radius: 6px;
    cursor: pointer;
    margin-bottom: 4px;
    border: 1px solid transparent;
    transition: all 0.15s ease;

    &:hover {
      background: #f8fafc;
      border-color: #e2e8f0;
    }

    &.active {
      background: #eff6ff;
      border-color: #93c5fd;

      .nav-item-title {
        color: #1d4ed8;
      }
    }

    &.nav-item--system {
      background: #f0fdfa;
      border-color: #ccfbf1;

      &:hover {
        background: #e6fffa;
        border-color: #99f6e4;
      }

      &.active {
        background: #e0f2fe;
        border-color: #38bdf8;

        .nav-item-title {
          color: #0369a1;
        }
      }
    }

    .nav-item-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 4px;
    }

    .nav-item-title {
      font-size: 13px;
      font-weight: 600;
      color: #1e293b;
      line-height: 1.35;
    }

    .nav-item-summary {
      font-size: 11px;
      color: #475569;
      margin-top: 3px;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .nav-item-route {
      font-size: 11px;
      color: #64748b;
      font-family: monospace;
      margin-top: 2px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
</style>
