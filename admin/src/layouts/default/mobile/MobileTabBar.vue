<template>
  <nav
    v-if="isVisible"
    class="sync-mobile-tabbar fixed bottom-0 left-0 right-0 z-[499] bg-white/95 dark:bg-slate-900/95 backdrop-blur-md border-t border-slate-200 dark:border-slate-800 md:hidden"
    :aria-label="t('appUi.kcb651ef4')"
  >
    <div class="grid grid-cols-4 h-14 items-center">
      <button
        v-for="item in quickTabs"
        :key="item.path"
        type="button"
        class="sync-mobile-tabbar-item flex flex-col items-center justify-center gap-0.5 h-full px-1 border-0 bg-transparent cursor-pointer transition-colors"
        :class="isActive(item.path) ? 'text-sky-600 dark:text-sky-400 font-semibold' : 'text-slate-500 dark:text-slate-400'"
        @click="navigateTo(item.path)"
      >
        <Icon :icon="item.icon || 'ant-design:appstore-outlined'" :size="19" />
        <span class="text-[11px] leading-tight truncate max-w-[72px]">{{ t(item.label) }}</span>
      </button>

      <button
        type="button"
        class="sync-mobile-tabbar-item flex flex-col items-center justify-center gap-0.5 h-full px-1 border-0 bg-transparent cursor-pointer text-slate-500 dark:text-slate-400 transition-colors"
        @click="openMenuDrawer"
      >
        <Icon icon="ant-design:menu-outlined" :size="19" />
        <span class="text-[11px] leading-tight">{{ t('appUi.kf022be31') }}</span>
      </button>
    </div>
  </nav>
</template>

<script lang="ts">
  import { $t } from '/@/locales';
  import { defineComponent, computed, unref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Icon } from '/@/components/Icon';
  import { usePermissionStore } from '/@/store/modules/permission';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { useAppInject } from '/@/hooks/web/useAppInject';
  import { useI18n } from '/@/hooks/web/useI18n';

  interface QuickTabItem {
    path: string;
    label: string;
    icon?: string;
  }

  function extractFirstLeaf(node: any): QuickTabItem | null {
    if (!node || node.hideMenu || node.meta?.hideMenu) return null;
    if (Array.isArray(node.children) && node.children.length > 0) {
      for (const child of node.children) {
        const leaf = extractFirstLeaf(child);
        if (leaf) {
          return {
            path: leaf.path,
            label: node.name || node.meta?.title || leaf.label,
            icon: node.icon || node.meta?.icon || leaf.icon,
          };
        }
      }
    }
    if (node.path && !String(node.path).startsWith('http')) {
      return {
        path: node.path,
        label: node.name || node.meta?.title || $t('appUi.k076925c5'),
        icon: node.icon || node.meta?.icon,
      };
    }
    return null;
  }

  export default defineComponent({
    name: 'MobileTabBar',
    components: { Icon },
    setup() {
      const route = useRoute();
      const router = useRouter();
      const permissionStore = usePermissionStore();
      const { setMenuSetting } = useMenuSetting();
      const { getIsMobile } = useAppInject();
      const { t } = useI18n();

      const quickTabs = computed<QuickTabItem[]>(() => {
        const menus = permissionStore.getBackMenuList || [];
        const items: QuickTabItem[] = [];
        const seen = new Set<string>();
        for (const menu of menus) {
          if (items.length >= 3) break;
          const resolved = extractFirstLeaf(menu);
          if (resolved && !seen.has(resolved.path)) {
            seen.add(resolved.path);
            items.push(resolved);
          }
        }
        return items;
      });

      const isVisible = computed(() => unref(getIsMobile) && quickTabs.value.length > 0);

      function isActive(path: string): boolean {
        return route.path === path || route.path.startsWith(`${path}/`);
      }

      function navigateTo(path: string) {
        if (route.path !== path) {
          router.push(path).catch(() => {});
        }
      }

      function openMenuDrawer() {
        setMenuSetting({ collapsed: false });
      }

      return {
        quickTabs,
        isVisible,
        isActive,
        navigateTo,
        openMenuDrawer,
        t,
      };
    },
  });
</script>

<style lang="less" scoped>
  .sync-mobile-tabbar {
    padding-bottom: env(safe-area-inset-bottom);
    box-shadow: 0 -2px 10px rgba(15, 23, 42, 0.06);
  }
</style>
