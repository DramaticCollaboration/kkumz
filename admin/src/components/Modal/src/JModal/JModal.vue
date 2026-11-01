<template>
  <a-modal v-bind="getBindValue" @cancel="handleCancel" :body-style="!fullScreenRef ? bodyStyle : {}">
    <slot></slot>
    <template #closeIcon v-if="!$slots.closeIcon">
      <div class="sync-basic-modal-close" v-if="canFullscreen">
        <Tooltip :title="t('component.modal.restore')" placement="bottom" v-if="fullScreenRef">
          <FullscreenExitOutlined role="full" @click="handleFullScreen" />
        </Tooltip>
        <Tooltip :title="t('component.modal.maximize')" placement="bottom" v-else>
          <FullscreenOutlined role="close" @click="handleFullScreen" />
        </Tooltip>
        <Tooltip :title="t('component.modal.close')" placement="bottom">
          <CloseOutlined @click="handleCancel" />
        </Tooltip>
      </div>
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

    <template #[item]="data" v-for="item in Object.keys(omit($slots, 'default'))">
      <slot :name="item" v-bind="data || {}"></slot>
    </template>
  </a-modal>
</template>

<script lang="ts" name="j-modal">
  import { computed, defineComponent, getCurrentInstance, ref, toRef, unref, watch, watchEffect } from 'vue';
  import ModalFooter from '@/components/Modal/src/components/ModalFooter.vue';
  import ModalClose from '@/components/Modal/src/components/ModalClose.vue';
  import ModalHeader from '@/components/Modal/src/components/ModalHeader.vue';
  import { omit } from 'lodash-es';
  import { useAppInject } from '@/hooks/web/useAppInject';
  import type { ModalMethods, ModalProps } from '@/components/Modal';
  import { CloseOutlined, FullscreenExitOutlined, FullscreenOutlined } from '@ant-design/icons-vue';
  import { Tooltip } from 'ant-design-vue';
  import { useI18n } from '@/hooks/web/useI18n';
  import { deepMerge } from '@/utils';
  import { basicProps } from '@/components/Modal/src/props';
  import Modal from '/@/components/Modal/src/components/Modal';
  import { isFunction } from '@/utils/is';
  export default defineComponent({
    name: 'JModal',
    methods: { omit },
    components: {
      CloseOutlined,
      Tooltip,
      FullscreenExitOutlined,
      FullscreenOutlined,
      ModalHeader,
      ModalClose,
      ModalFooter,
      Modal,
    },
    props: {
      //전체 화면 여부
      fullscreen: {
        type: Boolean,
        default: false,
      },
      ...basicProps,
    },
    emits: ['visible-change', 'open-change', 'height-change', 'cancel', 'ok', 'register', 'update:visible', 'update:open', 'fullScreen'],
    setup(props, { emit, attrs, slots }) {
      const { getIsMobile } = useAppInject();
      const visibleRef = ref(false);
      const propsRef = ref<Partial<ModalProps> | null>(null);
      const fullScreenRef = ref<any>(props.fullscreen);
      const fullScreen = ref<boolean>(false);
      const { t } = useI18n();
      const bodyStyle = ref<any>({
        height: props.maxHeight ? props.maxHeight : '600px',
        'overflow-y': 'auto',
      });
      const modalMethods: ModalMethods = {
        setModalProps,
        emitVisible: undefined,
      };

      const getMergeProps = computed((): Recordable => {
        const result = {
          ...props,
          ...(unref(propsRef) as any),
        };
        if (getIsMobile.value) {
          result.fullscreen = false;
        }
        return result;
      });

      const getBindValue = computed((): Recordable => {
        const attr = {
          ...attrs,
          ...unref(getMergeProps),
          open: unref(visibleRef),
          wrapClassName: unref(getWrapClassName),
        };
        if (unref(fullScreenRef)) {
          return omit(attr, ['height', 'visible']);
        }
        return omit(attr, ['visible']);
      });

      //WarpClassName 통합
      const getWrapClassName = computed(() => {
        const clsName = toRef(getMergeProps.value, 'wrapClassName').value || '';
        return unref(fullScreenRef) ? `sync-full-screen-modal-code-generate ${clsName} ` : unref(clsName);
      });

      //소품 받기
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

      //여러 모델 충돌을 방지하려면 모델, instance.uid를 등록하세요.
      const instance = getCurrentInstance();
      if (instance) {
        emit('register', modalMethods, instance.uid);
      }

      /**
       * 제목 포함 여부
       */
      function isNoTitle() {
        //제목이 비어 있고 제목 슬롯이 없습니다.
        return !unref(getMergeProps).title && !slots.title;
      }

      /**
       * 확대 및 축소 이벤트
       * @param e
       */
      function handleFullScreen(e: Event) {
        e?.stopPropagation();
        e?.preventDefault();
        fullScreenRef.value = !unref(fullScreenRef);
      }

      // 이벤트 취소
      async function handleCancel(e: Event) {
        e?.stopPropagation();
        // 맞춤 닫기 버튼을 위한 공백 필터링
        if (props.closeFunc && isFunction(props.closeFunc)) {
          const isClose: boolean = await props.closeFunc();
          visibleRef.value = !isClose;
          return;
        }
        visibleRef.value = false;
        emit('cancel', e);
      }

      /**
       * 이벤트 결정
       * @param e
       */
      function handleOk(e: Event) {
        emit('ok', e);
      }

      function handleTitleDbClick(e) {
        if (!props.fullscreen) return;
        e.stopPropagation();
        handleFullScreen(e);
      }
      /**
       * 모달 매개변수 설정
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
            fullScreenRef.value = true;
          }
        }
      }

      /**
       * 모니터 확대 및 축소
       */
      watchEffect(() => {
        fullScreenRef.value = props.fullscreen;
        if (getIsMobile.value) {
          fullScreenRef.value = true;
        }
      });

      /**
       * 모델의 표시 및 숨김을 모니터링합니다.
       */
      watchEffect(() => {
        visibleRef.value = !!props.visible;
      });

      /**
       * 모델의 표시 및 숨김을 모니터링합니다.
       */
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
        },
        {
          immediate: false,
        }
      );

      return {
        isNoTitle,
        getBindValue,
        fullScreenRef,
        handleFullScreen,
        fullScreen,
        t,
        handleCancel,
        handleOk,
        getProps,
        getMergeProps,
        handleTitleDbClick,
        bodyStyle,
      };
    },
  });
</script>

<style lang="less" scoped>
  /*시작 확대 닫기 버튼 스타일*/
  .sync-basic-modal-close {
    display: flex;
    height: 95%;
    align-items: center;

    > span {
      margin-left: 10px;
      font-size: 16px;
    }

    &--can-full {
      > span {
        margin-left: 12px;
      }
    }

    &:not(&--can-full) {
      > span:nth-child(1) {
        &:hover {
          font-weight: 700;
        }
      }
    }

    & span:nth-child(1) {
      display: inline-block;
      padding: 10px;

      &:hover {
        color: @primary-color;
      }
    }

    & span:last-child {
      padding: 10px 10px 10px 0;
      &:hover {
        color: @error-color;
      }
    }
  }
  /*끝 확대 닫기 버튼 스타일*/
</style>

<style lang="less">
  /*전체 화면 팝업 모달 스타일 시작*/
  .sync-full-screen-modal-code-generate {
    .ant-modal {
      max-width: 100%;
      top: 0 !important;
      padding-bottom: 0 !important;
      margin: 0 !important;
      width: 100% !important;
      overflow-y: auto;
    }
    .ant-modal-content {
      display: flex;
      flex-direction: column;
      height: calc(100vh);
    }
    .ant-modal-body {
      flex: 1;
      overflow-y: auto;
    }
  }
  /*전체 화면 팝업 모달 스타일 종료*/
</style>
