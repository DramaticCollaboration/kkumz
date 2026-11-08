<template>
  <SvgIcon :size="size" :name="getSvgIcon" v-if="isSvgIcon" :class="[$attrs.class, 'anticon']" :spin="spin" />
  <span v-else ref="elRef" :class="[$attrs.class, 'app-iconify anticon', spin && 'app-iconify-spin']" :style="getWrapStyle"></span>
</template>
<script lang="ts">
  import type { PropType } from 'vue';
  import { defineComponent, ref, watch, onMounted, nextTick, unref, computed, CSSProperties } from 'vue';
  import SvgIcon from './SvgIcon.vue';
  import Iconify from '@purge-icons/generated';
  import { isString } from '/@/utils/is';
  import { propTypes } from '/@/utils/propTypes';

  const SVG_END_WITH_FLAG = '|svg';
  const ICON_ALIAS_MAP: Record<string, string> = {
    'ant-design:shield-outlined': 'ant-design:safety-outlined',
    'ant-design:shield-filled': 'ant-design:safety-certificate-filled',
    'ant-design:gold-outline': 'ant-design:gold-outlined',
    'ant-design:interation-outlined': 'ant-design:interaction-outlined',
  };

  function normalizeIconName(rawIcon: string): string {
    if (!rawIcon) return '';
    const trimmed = rawIcon.trim();
    if (ICON_ALIAS_MAP[trimmed]) {
      return ICON_ALIAS_MAP[trimmed];
    }
    if (trimmed.startsWith('ant-design:')) {
      const suffix = trimmed.slice('ant-design:'.length);
      if (suffix !== suffix.toLowerCase()) {
        const kebab = suffix
          .replace(/([a-z0-9])([A-Z])/g, '$1-$2')
          .replace(/([A-Z]+)([A-Z][a-z])/g, '$1-$2')
          .toLowerCase();
        const normalized = `ant-design:${kebab}`;
        return ICON_ALIAS_MAP[normalized] || normalized;
      }
    }
    return trimmed;
  }

  export default defineComponent({
    name: 'Icon',
    components: { SvgIcon },
    props: {
      // icon name
      icon: propTypes.string,
      // icon color
      color: propTypes.string,
      // icon size
      size: {
        type: [String, Number] as PropType<string | number>,
        default: 16,
      },
      spin: propTypes.bool.def(false),
      prefix: propTypes.string.def(''),
    },
    setup(props) {
      const elRef = ref<ElRef>(null);

      const isSvgIcon = computed(() => props.icon?.endsWith(SVG_END_WITH_FLAG));
      const getSvgIcon = computed(() => props.icon.replace(SVG_END_WITH_FLAG, ''));
      const getIconRef = computed(() => normalizeIconName(`${props.prefix ? props.prefix + ':' : ''}${props.icon}`));

      const update = async () => {
        if (unref(isSvgIcon)) return;

        const el = unref(elRef);
        if (!el) return;

        await nextTick();
        const icon = unref(getIconRef);
        if (!icon) return;

        let svg = Iconify.renderSVG(icon, {});
        if (!svg && icon.startsWith('ant-design:') && !/-(outlined|filled|twotone)$/.test(icon)) {
          svg = Iconify.renderSVG(`${icon}-outlined`, {});
        }
        if (!svg && icon.startsWith('ant-design:')) {
          svg = Iconify.renderSVG('ant-design:appstore-outlined', {});
        }
        if (svg) {
          el.textContent = '';
          el.appendChild(svg);
        } else {
          const span = document.createElement('span');
          span.className = 'iconify';
          span.dataset.icon = icon;
          el.textContent = '';
          el.appendChild(span);
        }
      };

      const getWrapStyle = computed((): CSSProperties => {
        const { size, color } = props;
        let fs = size;
        if (isString(size)) {
          fs = parseInt(size, 10);
        }

        return {
          fontSize: `${fs}px`,
          color: color,
          display: 'inline-flex',
        };
      });

      watch(() => props.icon, update, { flush: 'post' });

      onMounted(update);

      return { elRef, getWrapStyle, isSvgIcon, getSvgIcon };
    },
  });
</script>
<style lang="less">
  .app-iconify {
    display: inline-block;
    // vertical-align: middle;

    &-spin {
      svg {
        animation: loadingCircle 1s infinite linear;
      }
    }
  }

  span.iconify {
    display: block;
    min-width: 1em;
    min-height: 1em;
    background-color: @iconify-bg-color;
    border-radius: 100%;
  }
</style>
