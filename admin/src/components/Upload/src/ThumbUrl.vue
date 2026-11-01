<template>
  <span class="thumb">
    <Image v-if="hasFileUrl" :src="fileUrl" :alt="fileName" :width="width" />
  </span>
</template>

<script lang="ts">
  import { defineComponent, computed } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { Image } from 'ant-design-vue';

  export default defineComponent({
    name: 'ThumbUrl',
    components: { Image },
    props: {
      fileUrl: propTypes.string.def(''),
      fileName: propTypes.string.def(''),
      width: propTypes.number.def(104),
    },
    setup(props) {
      const hasFileUrl = computed(() => Boolean(props.fileUrl && props.fileUrl.trim().length > 0));

      return {
        hasFileUrl,
      };
    },
  });
</script>

<style lang="less">
  .thumb {
    display: inline-block;

    img {
      position: static;
      display: block;
      cursor: zoom-in;
      border-radius: 4px;
      object-fit: cover;
    }
  }
</style>
