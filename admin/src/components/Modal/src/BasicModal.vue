<template>
  <Modal v-bind="getBindValue" @cancel="handleCancel">
    <template #closeIcon v-if="!$slots.closeIcon">
      <ModalClose :canFullscreen="getProps.canFullscreen" :fullScreen="fullScreenRef" :commentSpan="commentSpan" :enableComment="getProps.enableComment" @comment="handleComment" @cancel="handleCancel" @fullscreen="handleFullScreen" />
    </template>

    <template #title v-if="!isNoTitle">
      <ModalHeader :helpMessage="getProps.helpMessage" :title="getMergeProps.title" @dblclick="handleTitleDbClick" />
    </template>

    <template #footer v-if="!$slots.footer">
      <ModalFooter v-bind="getBindValue" @ok="handleOk" @cancel="handleCancel">
        <template #[item]="data" v-for="item in Object.keys($slots)">
          <slot :name="item" v-bind="data || {}"></slot>
        </template>
      </ModalFooter>
    </template>

    <a-row class="sync-modal-wrapper">
      <a-col :span="24-commentSpan" class="sync-modal-content">
        <ModalWrapper
          :useWrapper="getProps.useWrapper"
          :footerOffset="wrapperFooterOffset"
          :fullScreen="fullScreenRef"
          ref="modalWrapperRef"
          :loading="getProps.loading"
          :loading-tip="getProps.loadingTip"
          :minHeight="getProps.minHeight"
          :maxHeight="getProps.maxHeight"
          :height="getWrapperHeight"
          :visible="visibleRef"
          :modalFooterHeight="footer !== undefined && !footer ? 0 : undefined"
          v-bind="omit(getProps.wrapperProps, 'visible', 'height', 'modalFooterHeight')"
          @ext-height="handleExtHeight"
          @height-change="handleHeightChange">
          <slot></slot>
        </ModalWrapper>
      </a-col>

      <a-col :span="commentSpan" class="sync-comment-outer">
        <slot name="comment"></slot>
      </a-col>

    </a-row>

    <template #[item]="data" v-for="item in Object.keys(omit($slots, 'default'))">
      <slot :name="item" v-bind="data || {}"></slot>
    </template>
  </Modal>
</template>
<script lang="ts">
  import type { ModalProps, ModalMethods } from './typing';

  import { defineComponent, computed, ref, watch, unref, watchEffect, toRef, getCurrentInstance, nextTick } from 'vue';
  import Modal from './components/Modal';
  import ModalWrapper from './components/ModalWrapper.vue';
  import ModalClose from './components/ModalClose.vue';
  import ModalFooter from './components/ModalFooter.vue';
  import ModalHeader from './components/ModalHeader.vue';
  import { isFunction } from '/@/utils/is';
  import { deepMerge } from '/@/utils';
  import { basicProps } from './props';
  import { useFullScreen } from './hooks/useModalFullScreen';
  import { omit } from 'lodash-es';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useAppInject } from '/@/hooks/web/useAppInject';


  export default defineComponent({
    name: 'BasicModal',
    components: { Modal, ModalWrapper, ModalClose, ModalFooter, ModalHeader },
    inheritAttrs: false,
    props: basicProps,
    emits: ['visible-change', 'open-change', 'height-change', 'cancel', 'ok', 'register', 'update:visible', 'update:open', 'fullScreen','comment-open'],
    setup(props, { emit, attrs , slots}) {
      const visibleRef = ref(false);
      const propsRef = ref<Partial<ModalProps> | null>(null);
      const modalWrapperRef = ref<any>(null);
      const { prefixCls } = useDesign('basic-modal');
      // modal   Bottom and top height
      const extHeightRef = ref(0);
      const modalMethods: ModalMethods = {
        setModalProps,
        emitVisible: undefined,
        redoModalHeight: () => {
          nextTick(() => {
            if (unref(modalWrapperRef)) {
              (unref(modalWrapperRef) as any).setModalHeight();
            }
          });
        },
      };

      const instance = getCurrentInstance();
      if (instance) {
        emit('register', modalMethods, instance.uid);
      }
      const { getIsMobile } = useAppInject();

      // Custom title component: get title
      const getMergeProps = computed((): Recordable => {
        const result = {
          ...props,
          ...(unref(propsRef) as any),
        };
        if (getIsMobile.value) {
          result.canFullscreen = false;
          result.defaultFullscreen = true;
        }
        return result;
      });
        //제목이 설정되어 있지 않은지 여부
        const isNoTitle = computed(() => {
            //제목이 비어 있고 제목 슬롯이 없습니다.
            return !unref(getMergeProps).title && !slots.title;
        });

      const { handleFullScreen, getWrapClassName, fullScreenRef } = useFullScreen({
        modalWrapperRef,
        extHeightRef,
        wrapClassName: toRef(getMergeProps.value, 'wrapClassName'),
      });

      // modal component does not need title and origin buttons
      const getProps = computed((): Recordable => {
        const opt = {
          ...unref(getMergeProps),
          visible: unref(visibleRef),
          okButtonProps: undefined,
          cancelButtonProps: undefined,
          title: undefined,
        };
        return {
          ...opt,
          wrapClassName: unref(getWrapClassName),
        };
      });

      const getBindValue = computed((): Recordable => {
        const attr = {
          ...attrs,
          ...unref(getMergeProps),
          open: unref(visibleRef),
          wrapClassName: unref(getWrapClassName),
        };
        if (unref(fullScreenRef)) {
          return omit(attr, ['height', 'title', 'visible']);
        }
        return omit(attr, ['title', 'visible']);
      });

      const getWrapperHeight = computed(() => {
        if (unref(fullScreenRef)) return undefined;
        return unref(getProps).height;
      });

      watchEffect(() => {
        fullScreenRef.value = !!props.defaultFullscreen;
        if (getIsMobile.value) {
          fullScreenRef.value = true
        }
      });

      watchEffect(() => {
        visibleRef.value = !!props.visible;
      });

      watchEffect(() => {
        visibleRef.value = !!props.open;
      });

      watch(
        () => unref(visibleRef),
        (v) => {
          emit('visible-change', v);
          emit('open-change', v);
          emit('update:visible', v);
          emit('update:open', v);
          instance && modalMethods.emitVisible?.(v, instance.uid);
          nextTick(() => {
            if (props.scrollTop && v && unref(modalWrapperRef)) {
              (unref(modalWrapperRef) as any).scrollTop();
            }
          });
        },
        {
          immediate: false,
        }
      );

      // 이벤트 취소
      async function handleCancel(e: Event) {
        e?.stopPropagation();
        // 맞춤 닫기 버튼을 위한 공백 필터링
        if ((e.target as HTMLElement)?.classList?.contains(prefixCls + '-close--custom')) return;
        if (props.closeFunc && isFunction(props.closeFunc)) {
          const isClose: boolean = await props.closeFunc();
          visibleRef.value = !isClose;
          return;
        }

        visibleRef.value = false;
        emit('cancel', e);
      }

      /**
       * @description: 모달 매개변수 설정
       */
      function setModalProps(props: Partial<ModalProps>): void {
        // Keep the last setModalProps
        propsRef.value = deepMerge(unref(propsRef) || ({} as any), props);
        if (Reflect.has(props, 'visible')) {
          visibleRef.value = !!props.visible;
        }
        if (Reflect.has(props, 'open')) {
          visibleRef.value = !!props.open;
        }
        if (Reflect.has(props, 'defaultFullscreen')) {
          fullScreenRef.value = !!props.defaultFullscreen;
          if (getIsMobile.value) {
            fullScreenRef.value = true
          }
        }
      }

      function handleOk(e: Event) {
        emit('ok', e);
      }

      function handleHeightChange(height: string) {
        emit('height-change', height);
      }

      function handleExtHeight(height: number) {
        extHeightRef.value = height;
      }

      function handleTitleDbClick(e) {
        if (!props.canFullscreen) return;
        e.stopPropagation();
        handleFullScreen(e);
      }

      const commentSpan = ref(0);
      watch(()=>props.enableComment, (flag)=>{
        handleComment(flag)
      }, {immediate:true});
      function handleComment(flag){
        if(flag=== true){
          commentSpan.value = 6
        }else{
          commentSpan.value = 0
        }
        emit('comment-open', commentSpan.value === 0, commentSpan.value);
      }

      watch(fullScreenRef,(val)=>{
        emit('fullScreen',val);
      });

      return {
        handleCancel,
        getBindValue,
        getProps,
        handleFullScreen,
        fullScreenRef,
        getMergeProps,
        handleOk,
        visibleRef,
        omit,
        modalWrapperRef,
        handleExtHeight,
        handleHeightChange,
        handleTitleDbClick,
        getWrapperHeight,
        commentSpan,
        handleComment,
        isNoTitle
      };
    },
  });
</script>
<style lang="less">
  .sync-comment-outer {
    border-left: 1px solid #f0f0f0;
    .ant-tabs-nav-wrap{
    /*  text-align: center;*/
    }
  }
  .sync-modal-content{
    >.scroll-container{
      padding: 6px;
    }
  }

  // 래퍼를 100%로 설정합니다. 이는 이전에 작성한 팝업창 사용자 정의 스타일과 호환됩니다.
  .sync-modal-wrapper,
  .sync-modal-content {
    height: 100%;
  }
  html[data-theme='dark'] {
    .sync-comment-outer {
      border-left: 1px solid rgba(253, 253, 253, 0.12);
    }
  }
</style>
