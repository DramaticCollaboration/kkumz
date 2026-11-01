<template>
  <div ref="editorRootRef" :class="[prefixCls, { 'editor-loading': !isEditorReady }]" :style="{ width: containerWidth }">
    <Teleport v-if="imgUploadShow" :to="targetElem">
      <ImgUpload :fullscreen="fullscreen" @uploading="handleImageUploading" @done="handleDone" v-show="editorRef" :disabled="disabled" />
    </Teleport>
    <Editor :id="tinymceId" ref="elRef" :disabled="disabled" :init="initOptions" v-if="!initOptions.inline" />
    <slot v-else></slot>
  </div>
</template>

<script lang="ts">
  import tinymce from 'tinymce/tinymce';
  import Editor from '@tinymce/tinymce-vue';
  import 'tinymce/themes/silver';
  import 'tinymce/icons/default/icons';
  import 'tinymce/models/dom';

  // Tinmce 플러그인은 필요에 따라 가져올 수 있습니다.
  // 더 많은 플러그인 참조：https://www.tiny.cloud/docs/plugins/
  import 'tinymce/plugins/fullscreen';
  import 'tinymce/plugins/link';
  import 'tinymce/plugins/lists';
  import 'tinymce/plugins/preview';
  import 'tinymce/plugins/image';
  import { defineComponent, computed, nextTick, ref, unref, watch, onDeactivated, onBeforeUnmount, onMounted } from 'vue';
  import ImgUpload from './ImgUpload.vue';
  import { simpleToolbar, menubar, simplePlugins } from './tinymce';
  import { buildShortUUID } from '/@/utils/uuid';
  import { bindHandlers } from './helper';
  import { onMountedOrActivated } from '/@/hooks/core/onMountedOrActivated';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { isNumber } from '/@/utils/is';
  import { useLocale } from '/@/locales/useLocale';
  import { useAppStore } from '/@/store/modules/app';
  import { uploadFile } from '/@/api/common/api';
  import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
  import { ThemeEnum } from '/@/enums/appEnum';
  const tinymceProps = {
    options: {
      type: Object as PropType<Partial<RawEditorSettings>>,
      default: {},
    },
    value: {
      type: String,
    },

    toolbar: {
      type: [Array as PropType<string[]>, String],
      default: simpleToolbar,
    },
    plugins: {
      type: [Array as PropType<string[]>, String],
      default: simplePlugins,
    },
    menubar: {
      type: [Object, String],
      default: menubar,
    },
    modelValue: {
      type: String,
    },
    height: {
      type: [Number, String] as PropType<string | number>,
      required: false,
      default: 400,
    },
    width: {
      type: [Number, String] as PropType<string | number>,
      required: false,
      default: 'auto',
    },
    showImageUpload: {
      type: Boolean,
      default: true,
    },
  };

  export default defineComponent({
    name: 'Tinymce',
    components: { ImgUpload, Editor },
    inheritAttrs: false,
    props: tinymceProps,
    emits: ['change', 'update:modelValue', 'inited', 'init-error'],
    setup(props, { emit, attrs }) {
      console.log('---Tinymce---초기화---');

      const editorRef = ref<Nullable<any>>(null);
      const fullscreen = ref(false);
      const tinymceId = ref<string>(buildShortUUID('tiny-vue'));
      const elRef = ref<Nullable<HTMLElement>>(null);
      const editorRootRef = ref<Nullable<HTMLElement>>(null);
      const imgUploadShow = ref(false);
      const targetElem = ref<null | HTMLDivElement>(null);
      const isEditorReady = ref(false);

      const { prefixCls } = useDesign('tinymce-container');

      const appStore = useAppStore();

      const tinymceContent = computed(() => props.modelValue);

      const containerWidth = computed(() => {
        const width = props.width;
        if (isNumber(width)) {
          return `${width}px`;
        }
        return width;
      });

      const skinName = computed(() => {
        return appStore.getDarkMode === 'light' ? 'sync' : 'oxide-dark';
      });

      const langName = computed(() => {
        const lang = useLocale().getLocale.value;
        return ['ko_KR', 'en'].includes(lang) ? lang : 'ko_KR';
      });

      const initOptions = computed(() => {
        const { height, options, toolbar, plugins, menubar } = props;
        let publicPath = import.meta.env.VITE_PUBLIC_PATH || '/';
        if (!publicPath.endsWith('/')) {
          publicPath += '/';
        }

        return {
          selector: `#${unref(tinymceId)}`,
          height,
          toolbar,
          menubar: false,
          plugins,
          language_url: publicPath + 'resource/tinymce/langs/' + langName.value + '.js',
          language: langName.value,
          branding: false,
          default_link_target: '_blank',
          link_title: false,
          object_resizing: true,
          toolbar_mode: 'sliding',
          auto_focus: true,
          // toolbar_groups: true,
          skin: skinName.value,
          skin_url: publicPath + 'resource/tinymce/skins/ui/' + skinName.value,
          images_upload_handler: (blobInfo, process) =>
            new Promise((resolve, reject) => {
              let params = {
                file: blobInfo.blob(),
                filename: blobInfo.filename(),
                data: { biz: 'jeditor', jeditor: '1' },
              };
              const uploadSuccess = (res) => {
                if (res.success) {
                  if (res.message == 'local') {
                    const img = 'data:image/jpeg;base64,' + blobInfo.base64();
                    resolve(img);
                  } else {
                    let img = getFileAccessHttpUrl(res.message);
                    resolve(img);
                  }
                } else {
                  reject('업로드 실패!');
                }
              };
              uploadFile(params, uploadSuccess);
            }),
          content_css: publicPath + 'resource/tinymce/skins/ui/' + skinName.value + '/content.min.css',
          ...options,
          setup: (editor: any) => {
            editorRef.value = editor;
            editor.on('init', (e) => {
              initSetup(e);

              // TinyMCE 컨테이너의 visibility 설정
              const editorContainer = editor.getContainer();
              if (editorContainer) {
                editorContainer.style.visibility = 'visible';
              }

              // iframe의 visibility도 설정
              const editorIframe = editor.iframeElement;
              if (editorIframe) {
                editorIframe.style.visibility = 'visible';
              }
            });
          },
        };
      });

      const disabled = computed(() => {
        const { options } = props;
        const getdDisabled = options && Reflect.get(options, 'readonly');
        const editor = unref(editorRef);
        if (editor && editor?.setMode) {
          editor.setMode(getdDisabled || attrs.disabled === true ? 'readonly' : 'design');
        }
        if (attrs.disabled === true) {
          return true;
        }
        return getdDisabled ?? false;
      });

      watch(
        () => attrs.disabled,
        () => {
          const editor = unref(editorRef);
          if (!editor) {
            return;
          }
          editor?.setMode && editor.setMode(attrs.disabled ? 'readonly' : 'design');
        }
      );

      onMountedOrActivated(() => {
        if (!initOptions.value.inline) {
          tinymceId.value = buildShortUUID('tiny-vue');
        }
        nextTick(() => {
          setTimeout(() => {
            initEditor();
          }, 30);
        });
      });

      onBeforeUnmount(() => {
        destory();
      });

      onDeactivated(() => {
        destory();
      });

      function destory() {
        if (tinymce !== null) {
          tinymce?.remove?.(unref(initOptions).selector!);
        }
      }

      function initEditor() {
        const el = unref(elRef);

        tinymce
          .init(unref(initOptions))
          .then((editor) => {
            changeColor();
            isEditorReady.value = true;
            emit('inited', editor);
          })
          .catch((err) => {
            emit('init-error', err);
          });
      }

      function initSetup(e) {
        const editor = unref(editorRef);
        if (!editor) {
          return;
        }
        const value = props.modelValue || '';

        editor.setContent(value);
        bindModelHandlers(editor);
        bindHandlers(e, attrs, unref(editorRef));
      }

      function setValue(editor: Recordable, val: string, prevVal?: string) {
        if (editor && typeof val === 'string' && val !== prevVal && val !== editor.getContent({ format: attrs.outputFormat })) {
          editor.setContent(val);
        }
      }

      function bindModelHandlers(editor: any) {
        const modelEvents = attrs.modelEvents ? attrs.modelEvents : null;
        const normalizedEvents = Array.isArray(modelEvents) ? modelEvents.join(' ') : modelEvents;

        watch(
          () => props.modelValue,
          (val: string, prevVal: string) => {
            setValue(editor, val, prevVal);
          }
        );

        watch(
          () => props.value,
          (val: string, prevVal: string) => {
            setValue(editor, val, prevVal);
          },
          {
            immediate: true,
          }
        );

        editor.on(normalizedEvents ? normalizedEvents : 'change keyup undo redo', () => {
          const content = editor.getContent({ format: attrs.outputFormat });
          emit('update:modelValue', content);
          emit('change', content);
        });

        editor.on('FullscreenStateChanged', (e) => {
          fullscreen.value = e.state;
        });
      }

      function handleImageUploading(name: string) {
        const editor = unref(editorRef);
        if (!editor) {
          return;
        }
        editor.execCommand('mceInsertContent', false, getUploadingImgName(name));
        const content = editor?.getContent() ?? '';
        setValue(editor, content);
      }

      function handleDone(name: string, url: string) {
        const editor = unref(editorRef);
        if (!editor) {
          return;
        }
        const content = editor?.getContent() ?? '';
        const val = content?.replace(getUploadingImgName(name), `<img src="${url}"/>`) ?? '';
        setValue(editor, val);
      }

      function getUploadingImgName(name: string) {
        return `[uploading:${name}]`;
      }
      let executeCount = 0;
      watch(
        () => props.showImageUpload,
        () => {
          mountElem();
        }
      );
      onMounted(() => {
        mountElem();
      });
      const mountElem = () => {
        if (executeCount > 20) return;
        setTimeout(() => {
          if (targetElem.value) {
            imgUploadShow.value = props.showImageUpload;
          } else {
            const toxToolbar = editorRootRef.value?.querySelector('.tox-toolbar__group');
            if (toxToolbar) {
              const divElem = document.createElement('div');
              divElem.setAttribute('style', `width:64px;height:39px;display:flex;align-items:center;`);
              toxToolbar!.appendChild(divElem);
              targetElem.value = divElem;
              imgUploadShow.value = props.showImageUpload;
              executeCount = 0;
            } else {
              mountElem();
            }
          }
          executeCount++;
        }, 100);
      };
      function changeColor() {
        setTimeout(() => {
          const iframe = editorRootRef.value?.querySelector('iframe');
          const body = iframe?.contentDocument?.querySelector('body');
          if (body) {
            if (appStore.getDarkMode == ThemeEnum.DARK) {
              body.style.color = '#fff';
            } else {
              body.style.color = '#000';
            }
          }
        }, 300);
      }
      watch(
        () => appStore.getDarkMode,
        () => {
          changeColor();
        }
      );
      return {
        prefixCls,
        containerWidth,
        initOptions,
        tinymceContent,
        elRef,
        tinymceId,
        handleImageUploading,
        handleDone,
        editorRef,
        fullscreen,
        disabled,
        editorRootRef,
        imgUploadShow,
        isEditorReady,
        targetElem,
      };
    },
  });
</script>

<style lang="less" scoped></style>

<style lang="less">
  @prefix-cls: ~'@{namespace}-tinymce-container';

  .@{prefix-cls} {
    position: relative;
    line-height: normal;

    textarea {
      z-index: -1;
      visibility: hidden;
    }
    .tox:not(.tox-tinymce-inline) .tox-editor-header {
      padding: 0;
    }
    .tox .tox-tbtn--disabled,
    .tox .tox-tbtn--disabled:hover,
    .tox .tox-tbtn:disabled,
    .tox .tox-tbtn:disabled:hover {
      background-image: url("data:image/svg+xml;charset=utf8,%3Csvg height='39px' viewBox='0 0 40 39px' width='40' xmlns='http://www.w3.org/2000/svg'%3E%3Crect x='0' y='38px' width='100' height='1' fill='%23d9d9d9'/%3E%3C/svg%3E");
      background-position: left 0;
    }
  }
  html[data-theme='dark'] {
    .@{prefix-cls} {
      .tox .tox-edit-area__iframe {
        background-color: #141414;
      }
    }
  }

  .editor-loading {
    .tox-tinymce {
      visibility: hidden;
    }
  }
</style>
