<template>
  <MenuItem :key="itemKey" :title="itemTitle" class="basic-menu-item">
    <MenuItemContent v-bind="$props" :item="item" />
  </MenuItem>
</template>

<script lang="ts">
  import { defineComponent, computed } from 'vue';
  import { Menu } from 'ant-design-vue';
  import { itemProps } from '../props';
  import MenuItemContent from './MenuItemContent.vue';

  export default defineComponent({
    name: 'BasicMenuItem',
    components: { MenuItem: Menu.Item, MenuItemContent },
    props: itemProps,
    setup(props) {
      const itemKey = computed(() => props.item?.path || '');
      const itemTitle = computed(() => props.item?.title || '');

      return {
        itemKey,
        itemTitle,
      };
    },
  });
</script>

<style lang="less" scoped>
  .basic-menu-item {
    position: relative;
  }
</style>
