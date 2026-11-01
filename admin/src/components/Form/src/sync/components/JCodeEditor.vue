<template>
  <div ref="containerRef" v-bind="boxBindProps">
    <!-- 전체 화면 버튼 -->
    <a-icon v-if="fullScreen" class="full-screen-icon" :type="fullScreenIcon" @click="onToggleFullScreen" />
    <textarea ref="textarea" v-bind="getBindValue"></textarea>
  </div>
</template>

<script lang="ts">
  import { defineComponent, onMounted, reactive, ref, watch, unref, computed } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  // 글로벌 인스턴스 소개
  import _CodeMirror, { EditorFromTextArea } from 'codemirror';
  // 핵심 스타일
  import 'codemirror/lib/codemirror.css';
  // 테마를 도입한 후 옵션에서 테마를 지정해야 적용됩니다.
  import 'codemirror/theme/idea.css';
  // 해당 구문 강조 효과를 얻으려면 특정 구문 강조 라이브러리를 도입해야 합니다.
  import 'codemirror/mode/javascript/javascript.js';
  import 'codemirror/mode/css/css.js';
  import 'codemirror/mode/xml/xml.js';
  import 'codemirror/mode/clike/clike.js';
  import 'codemirror/mode/markdown/markdown.js';
  import 'codemirror/mode/python/python.js';
  import 'codemirror/mode/r/r.js';
  import 'codemirror/mode/shell/shell.js';
  import 'codemirror/mode/sql/sql.js';
  import 'codemirror/mode/swift/swift.js';
  import 'codemirror/mode/vue/vue.js';
  // 접는 자원 소개: 시작
  import 'codemirror/addon/fold/foldgutter.css';
  import 'codemirror/addon/fold/foldcode.js';
  import 'codemirror/addon/fold/brace-fold.js';
  import 'codemirror/addon/fold/comment-fold.js';
  import 'codemirror/addon/fold/indent-fold.js';
  import 'codemirror/addon/fold/foldgutter.js';
  // 접는 리소스 소개: 끝
  //커서 라인 배경이 강조 표시되고 구성에서 styleActiveLine도 true로 설정되어야 합니다.
  import 'codemirror/addon/selection/active-line.js';
  // 지원 코드 자동 완성
  import 'codemirror/addon/hint/show-hint.css';
  import 'codemirror/addon/hint/show-hint.js';
  import 'codemirror/addon/hint/anyword-hint.js';
  // 일치 괄호
  import 'codemirror/addon/edit/matchbrackets';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { isJsonObjectString } from '/@/utils/is.ts';
  // 코드 팁
  import { useCodeHinting } from '../hooks/useCodeHinting';

  import { useRootSetting } from '/@/hooks/setting/useRootSetting';
  import { ThemeEnum } from '/@/enums/appEnum';
  export default defineComponent({
    name: 'JCodeEditor',
    // attrs 속성을 html 태그에 바인딩하지 마세요.
    inheritAttrs: false,
    components: {},
    props: {
      value: propTypes.string.def(''),
      height: propTypes.string.def('auto'),
      disabled: propTypes.bool.def(false),
      // 전체 화면 버튼 표시 여부
      fullScreen: propTypes.bool.def(false),
      // 전체 화면 이후의 Z-색인
      zIndex: propTypes.any.def(1500),
      theme: propTypes.string.def('idea'),
      language: propTypes.string.def(''),
      // 코드 팁
      keywords: propTypes.array.def([]),
    },
    emits: ['change', 'update:value'],
    setup(props, { emit }) {
      const { getDarkMode } = useRootSetting();
      const containerRef = ref(null);
      const { prefixCls } = useDesign('code-editer');
      const CodeMirror = window.CodeMirror || _CodeMirror;
      const emitData = ref<object>();
      //형태 가치
      const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      const textarea = ref<HTMLTextAreaElement>();
      let coder: Nullable<EditorFromTextArea> = null;
      const attrs = useAttrs();
      const height = ref(props.height);
      const options = reactive({
        // 들여쓰기 형식
        tabSize: 2,
        // 테마, 해당 테마 라이브러리 JS를 사전에 도입해야 합니다.
        theme: getDarkMode.value == ThemeEnum.DARK ? 'monokai' : props.theme,
        smartIndent: true, // 스마트 들여쓰기 사용 여부
        // 줄 번호 표시
        lineNumbers: true,
        line: true,
        // 코드 접기 관련 기능 활성화: 시작하기
        foldGutter: true,
        lineWrapping: true,
        gutters: ['CodeMirror-linenumbers', 'CodeMirror-foldgutter', 'CodeMirror-lint-markers'],
        // 코드 접기 관련 기능 활성화: 끝
        // 커서 라인 강조
        styleActiveLine: true,
        mode: props.language,
        readOnly: props.disabled,
        // 일치 괄호
        matchBrackets: true,
        extraKeys: {
          // Tab: function autoFormat(editor) {
          //   //var totalLines = editor.lineCount();
          //   //editor.autoFormatRange({line:0, ch:0}, {line:totalLines});
          //   setValue(innerValue, false);
          // },
          'Cmd-/': (cm) => comment(cm),
          'Ctrl-/': (cm) => comment(cm),
        },
      });
      // 내부에 저장된 값, 초기에는 props.value
      let innerValue = props.value ?? '';
      // 전체 화면 상태
      const isFullScreen = ref(false);
      const fullScreenIcon = computed(() => (isFullScreen.value ? 'fullscreen-exit' : 'fullscreen'));
      // 외부 상자 매개변수
      const boxBindProps = computed(() => {
        let _props = {
          class: [
            prefixCls,
            'full-screen-parent',
            'auto-height',
            {
              'full-screen': isFullScreen.value,
            },
          ],
          style: {},
        };
        if (isFullScreen.value) {
          _props.style['z-index'] = props.zIndex;
        }
        return _props;
      });
      const { codeHintingMount, codeHintingRegistry } = useCodeHinting(CodeMirror, props.keywords, props.language);
      codeHintingRegistry();
      /**
       * 컴포넌트 값 듣기
       */
      watch(
        () => props.value,
        () => {
          if (innerValue != props.value) {
            setValue(props.value, false);
          }
        }
      );
      onMounted(() => {
        initialize();
        setTimeout(() => {
          refresh();
        }, 150);
      });

      /**
       * 컴포넌트 할당
       * @param value
       * @param 변경 이벤트를 트리거할지 여부를 트리거합니다.
       */
      function setValue(value: string, trigger = true) {
        if (value && isJsonObjectString(value)) {
          value = JSON.stringify(JSON.parse(value), null, 2);
        }
        coder?.setValue(value ?? '');
        innerValue = value;
        trigger && emitChange(innerValue);
        setTimeout(() => {
          refresh();
          // 작은 확률로 교합 문제를 방지하려면 다시 브러시를 사용하세요.
          setTimeout(() => {
            refresh();
          }, 600);
        }, 400);
      }

      //에디터 값 수정 이벤트
      function onChange(obj) {
        let value = obj.getValue();
        innerValue = value || '';
        if (props.value != innerValue) {
          emitChange(innerValue);
        }
      }

      function emitChange(value) {
        emit('change', value);
        emit('update:value', value);
      }

      //구성요소 초기화
      function initialize() {
        coder = CodeMirror.fromTextArea(textarea.value!, options);
        //바인드 값 수정 이벤트
        coder.on('change', onChange);
        // 초기화에 성공하면 값을 한 번만 지정하세요.
        setValue(innerValue, false);
        codeHintingMount(coder);
      }

      // 전체 화면 상태 전환
      function onToggleFullScreen() {
        isFullScreen.value = !isFullScreen.value;
      }

      watch(
        () => props.disabled,
        (val) => {
          if (coder) {
            coder.setOption('readOnly', val);
          }
        }
      );
      
      // 동적으로 언어 설정 지원
      watch(()=>props.language, (val)=>{
        if(val && coder){
          coder.setOption('mode', val);
        }
      });

      const getBindValue = Object.assign({}, unref(props), unref(attrs));

      function refresh(){
        if(coder){
          coder.refresh();
        }
      }

      /**
       * 2024-04-01
       * liaozhiyang
       * 코드 배치 주석
       */
      function comment(cm) {
        var selection = cm.getSelection();
        var start = cm.getCursor('start');
        var end = cm.getCursor('end');
        var isCommented = selection.startsWith('//');
        if (isCommented) {
          // 이미 댓글이 달린 경우 댓글을 삭제하세요.
          cm.replaceRange(selection.replace(/\n\/\/\s/g, '\n').replace(/^\/\/\s/, ''), start, end);
        } else {
          // 댓글 추가
          cm.replaceRange('// ' + selection.replace(/\n(?=.)/g, '\n// '), start, end);
        }
      }

      return {
        state,
        textarea,
        boxBindProps,
        getBindValue,
        setValue,
        isFullScreen,
        fullScreenIcon,
        onToggleFullScreen,
        refresh,
        containerRef,
      };
    },
  });
</script>

<style lang="less">
  //noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-code-editer';
  .@{prefix-cls} {
    &.auto-height {
      .CodeMirror {
        height: v-bind(height) !important;
        min-height: 100px;
      }
    }

    /* 전체 화면 스타일 */

    &.full-screen-parent {
      position: relative;

      .full-screen-icon {
        opacity: 0;
        color: black;
        width: 20px;
        height: 20px;
        line-height: 24px;
        background-color: white;
        position: absolute;
        top: 2px;
        right: 2px;
        z-index: 9;
        cursor: pointer;
        transition: opacity 0.3s;
        padding: 2px 0 0 1.5px;
      }

      &:hover {
        .full-screen-icon {
          opacity: 1;

          &:hover {
            background-color: rgba(255, 255, 255, 0.88);
          }
        }
      }

      &.full-screen {
        position: fixed;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        padding: 8px;
        background-color: #f5f5f5;

        .full-screen-icon {
          top: 12px;
          right: 12px;
        }

        .full-screen-child,
        .CodeMirror {
          height: 100%;
          max-height: 100%;
          min-height: 100%;
        }
      }

      .full-screen-child {
        height: 100%;
      }
    }
    
    /** VUEN-2344【vue3】이 스타일에 문제가 있습니다. 테두리를 추가해야 합니까? */
    .CodeMirror{
      border: 1px solid #ddd;
    }
  }
  .CodeMirror-hints.idea,
  .CodeMirror-hints.monokai {
    z-index: 1001;
    max-width: 600px;
    max-height: 300px;
  }
  html[data-theme='dark'] {
    .@{prefix-cls} {
      .CodeMirror {
        border: 1px solid #3a3a3a;
      }
    }
  }
</style>
