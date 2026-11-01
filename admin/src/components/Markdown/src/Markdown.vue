<template>
  <div ref="wrapRef"></div>
</template>
<script lang="ts">
  import type { Ref } from 'vue';
  import { defineComponent, ref, unref, nextTick, computed, watch, onBeforeUnmount, onDeactivated } from 'vue';
  import Vditor from 'vditor';
  import 'vditor/dist/index.css';
  import { useLocale } from '/@/locales/useLocale';
  import { useModalContext } from '../../Modal';
  import { useRootSetting } from '/@/hooks/setting/useRootSetting';
  import { onMountedOrActivated } from '/@/hooks/core/onMountedOrActivated';
  import { getToken } from '/@/utils/auth';
  import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';

  type Lang = 'zh_CN' | 'en_US' | 'ja_JP' | 'ko_KR' | undefined;

  export default defineComponent({
    inheritAttrs: false,
    props: {
      height: { type: Number, default: 360 },
      value: { type: String, default: '' },
    },
    emits: ['change', 'get', 'update:value'],
    setup(props, { attrs, emit }) {
      console.log("---Markdown---초기화---")
      const wrapRef = ref<ElRef>(null);
      const vditorRef = ref(null) as Ref<Nullable<Vditor>>;
      const initedRef = ref(false);

      const modalFn = useModalContext();

      const { getLocale } = useLocale();
      const { getDarkMode } = useRootSetting();
      const valueRef = ref(props.value || '');

      watch(
        [() => getDarkMode.value, () => initedRef.value],
        ([val, inited]) => {
          if (!inited) {
            return;
          }
          const theme = val === 'dark' ? 'dark' : 'classic';
          instance.getVditor()?.setTheme(theme);
        },
        {
          immediate: true,
          flush: 'post',
        }
      );

      watch(
        () => props.value,
        (v) => {
          if (v !== valueRef.value) {
            instance.getVditor()?.setValue(v);
          }
          valueRef.value = v;
        }
      );

      const getCurrentLang = computed((): 'zh_CN' | 'en_US' | 'ja_JP' | 'ko_KR' => {
        let lang: Lang;
        switch (unref(getLocale)) {
          case 'en':
            lang = 'en_US';
            break;
          case 'ja':
            lang = 'ja_JP';
            break;
          default:
            lang = 'ko_KR';
        }
        return lang;
      });
      const uploadUrl = `${window._CONFIG['domianURL']}/api/v1/kkumz/sys/common/upload`;
      const token = getToken();
      function formatResult(files, responseText): string {
        let data: any = JSON.parse(responseText);
        // {"success":true,"message":"markdown/aa_1653391146501.png","code":0,"result":null,"timestamp":1653391146501}'
        let filename = files[0].name as string;
        let result = {
          msg: '',
          code: 0,
          data: {
            errFiles: [''],
            succMap: {},
          },
        };
        if (data.success) {
          result.data.errFiles = [];
          result.data.succMap = {
            [data.message]: getFileAccessHttpUrl(data.message),
          };
        } else {
          result.code = 1;
          result.msg = data.message;
          result.data.errFiles = [filename];
        }
        return JSON.stringify(result);
      }
      function init() {
        const wrapEl = unref(wrapRef) as HTMLElement;
        if (!wrapEl) return;
        const bindValue = { ...attrs, ...props };
        const insEditor = new Vditor(wrapEl, {
          theme: getDarkMode.value === 'dark' ? 'dark' : 'classic',
          lang: unref(getCurrentLang),
          toolbar: [
            'emoji',
            'headings',
            'bold',
            'italic',
            'strike',
            'link',
            '|',
            'list',
            'ordered-list',
            'check',
            'outdent',
            'indent',
            '|',
            'quote',
            'line',
            'code',
            'inline-code',
            'insert-before',
            'insert-after',
            '|',
            'upload',
            // 'record',
            'table',
            '|',
            'undo',
            'redo',
            '|',
            'fullscreen',
            'edit-mode',
            {
              name: 'more',
              toolbar: ['both', 'code-theme', 'content-theme', 'export', 'outline', 'preview', 'devtools', 'info', 'help'],
            },
          ],
          mode: 'sv',
          // cdn: 'https://cdn.jsdelivr.net/npm/vditor@3.9.6',
          cdn: 'https://unpkg.com/vditor@3.10.1',
          fullscreen: {
            index: 520,
          },
          preview: {
            actions: [],
          },
          upload: {
            accept: 'image/*',
            url: uploadUrl,
            fieldName: 'file',
            extraData: { biz: 'markdown' },
            setHeaders() {
              return {
                'X-Access-Token': token as string,
              };
            },
            format(files, response) {
              return formatResult(files, response);
            },
          },
          input: (v) => {
            valueRef.value = v;
            emit('update:value', v);
            emit('change', v);
          },
          after: () => {
            nextTick(() => {
              modalFn?.redoModalHeight?.();
              insEditor.setValue(valueRef.value);
              vditorRef.value = insEditor;
              initedRef.value = true;
              emit('get', instance);
            });
          },
          blur: () => {
            //unref(vditorRef)?.setValue(props.value);
          },
          ...bindValue,
          cache: {
            enable: false,
          },
        });
      }

      const instance = {
        getVditor: (): Vditor => vditorRef.value!,
      };

      function destroy() {
        const vditorInstance = unref(vditorRef);
        if (!vditorInstance) return;
        try {
          vditorInstance?.destroy?.();
        } catch (error) { console.debug("작업 처리 중 예외 발생:", error); }
        vditorRef.value = null;
        initedRef.value = false;
      }

      onMountedOrActivated(init);

      onBeforeUnmount(destroy);
      onDeactivated(destroy);
      return {
        wrapRef,
        ...instance,
      };
    },
  });
</script>
<style lang="less" scoped>
  :deep(.vditor-menu--disabled) {
    pointer-events: none;
  }
</style>
