<!--
 * @Author: Vben
 * @Description: Multi-language switching component
-->
<template>
  <Dropdown
    placement="bottom"
    :trigger="['click']"
    :dropMenuList="localeList"
    :selectedKeys="selectedKeys"
    @menuEvent="handleMenuEvent"
    overlayClassName="app-locale-picker-overlay"
  >
    <span class="cursor-pointer flex items-center">
      <svg
        class="anticon"
        viewBox="0 0 24 24"
        width="16"
        height="16"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <rect
          x="2.5"
          y="2.5"
          width="13"
          height="12"
          rx="2.5"
          stroke="currentColor"
          stroke-width="1.8"
        />
        <path
          d="M6.3 11.8L9 5.2L11.7 11.8"
          stroke="currentColor"
          stroke-width="1.8"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <line
          x1="7.1"
          y1="9.8"
          x2="10.9"
          y2="9.8"
          stroke="currentColor"
          stroke-width="1.8"
          stroke-linecap="round"
        />
        <path
          d="M15.5 9.5H19A2.5 2.5 0 0 1 21.5 12V19A2.5 2.5 0 0 1 19 21.5H12A2.5 2.5 0 0 1 9.5 19V14.5"
          stroke="currentColor"
          stroke-width="1.8"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M14 13.5H17.5M14 13.5V18.5M14 16H16.8M14 18.5H17.5"
          stroke="currentColor"
          stroke-width="1.6"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
      <span v-if="showText" class="ml-1">{{ getLocaleText }}</span>
    </span>
  </Dropdown>
</template>
<script lang="ts" setup>
  import type { LocaleType } from '/#/config';
  import type { DropMenu } from '/@/components/Dropdown';
  import { ref, watchEffect, unref, computed } from 'vue';
  import { Dropdown } from '/@/components/Dropdown';

  import { useLocale } from '/@/locales/useLocale';
  import { localeList } from '/@/settings/localeSetting';

  const props = defineProps({
    /**
     * Whether to display text
     */
    showText: { type: Boolean, default: true },
    /**
     * Whether to refresh the interface when changing
     */
    reload: { type: Boolean },
  });

  const selectedKeys = ref<string[]>([]);

  const { changeLocale, getLocale } = useLocale();

  const getLocaleText = computed(() => {
    const key = selectedKeys.value[0];
    if (!key) {
      return '';
    }
    return localeList.find((item) => item.event === key)?.text;
  });

  watchEffect(() => {
    selectedKeys.value = [unref(getLocale)];
  });

  async function toggleLocale(lang: LocaleType | string) {
    await changeLocale(lang as LocaleType);
    selectedKeys.value = [lang as string];
    props.reload && location.reload();
  }

  function handleMenuEvent(menu: DropMenu) {
    if (unref(getLocale) === menu.event) {
      return;
    }
    toggleLocale(menu.event as string);
  }
</script>

<style lang="less">
  .app-locale-picker-overlay {
    .ant-dropdown-menu-item {
      min-width: 160px;
    }
  }
</style>
