<template>
  <span class="edit-table-header-icon">
    <slot></slot>
    <span v-if="hasTitle" class="edit-table-header-title">{{ title }}</span>
    <FormOutlined class="edit-table-header-mark" />
  </span>
</template>

<script lang="ts">
  import { defineComponent, computed } from 'vue';
  import { FormOutlined } from '@ant-design/icons-vue';

  export default defineComponent({
    name: 'EditTableHeaderIcon',
    components: { FormOutlined },
    props: {
      title: {
        type: String,
        default: '',
      },
    },
    setup(props) {
      const hasTitle = computed(() => Boolean(props.title && props.title.trim().length > 0));

      return {
        hasTitle,
      };
    },
  });
</script>

<style lang="less" scoped>
  .edit-table-header-icon {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }
</style>
