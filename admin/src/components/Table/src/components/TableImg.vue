<template>
  <div :class="prefixCls" class="flex items-center mx-auto" v-if="imgList && imgList.length" :style="getWrapStyle">
    <Badge :count="!showBadge || imgList.length == 1 ? 0 : imgList.length" v-if="simpleShow">
      <div class="img-div">
        <PreviewGroup>
          <template v-for="(img, index) in imgList" :key="img">
            <Image
              :width="size"
              :style="{
                display: index === 0 ? '' : 'none !important',
              }"
              :src="srcPrefix + img"
            />
          </template>
        </PreviewGroup>
      </div>
    </Badge>
    <PreviewGroup v-else>
      <template v-for="(img, index) in imgList" :key="img">
        <Image :width="size" :style="{ marginLeft: index === 0 ? 0 : margin }" :src="srcPrefix + img" />
      </template>
    </PreviewGroup>
  </div>
</template>
<script lang="ts">
  import type { CSSProperties } from 'vue';
  import { defineComponent, computed } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { Image, Badge } from 'ant-design-vue';
  import { propTypes } from '/@/utils/propTypes';

  export default defineComponent({
    name: 'TableImage',
    components: { Image, PreviewGroup: Image.PreviewGroup, Badge },
    props: {
      imgList: propTypes.arrayOf(propTypes.string),
      size: propTypes.number.def(40),
      // 단순하게 표시할지 여부（첫 번째 이미지만 표시）
      simpleShow: propTypes.bool,
      // 단순 모드에서 사진 매수와 함께 배지를 표시할지 여부
      showBadge: propTypes.bool.def(true),
      // 그림 간격
      margin: propTypes.number.def(4),
      // imgList의 각 항목 앞에 src 접두사가 추가됩니다.
      srcPrefix: propTypes.string.def(''),
    },
    setup(props) {
      const getWrapStyle = computed((): CSSProperties => {
        const { size } = props;
        const s = `${size}px`;
        return { height: s, width: s };
      });

      const { prefixCls } = useDesign('basic-table-img');
      return { prefixCls, getWrapStyle };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-basic-table-img';

  .@{prefix-cls} {
    .ant-image {
      margin-right: 4px;
      cursor: zoom-in;

      img {
        border-radius: 2px;
      }
    }

    .img-div {
      display: inline-grid;
    }
  }
</style>
