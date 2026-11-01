import { isRef, unref, watch, Ref, ComputedRef } from 'vue';
import Clipboard from 'clipboard';
import { ModalOptionsEx, useMessage } from '/@/hooks/web/useMessage';

/** 복사 버튼이 있는 팝업 창 */
interface IOptions extends ModalOptionsEx {
  // 복사할 텍스트는 동적으로 업데이트되는 참조 객체일 수 있습니다.
  copyText: string | Ref<string> | ComputedRef<string>;
}

const COPY_CLASS = 'copy-this-text';
const CLIPBOARD_TEXT = 'data-clipboard-text';

export function useCopyModal() {
  return { createCopyModal };
}

const { createMessage, createConfirm } = useMessage();

/** 복사 팝업 만들기 */
function createCopyModal(options: Partial<IOptions>) {
  let modal = createConfirm({
    ...options,
    iconType: options.iconType ?? 'info',
    width: options.width ?? 500,
    title: options.title ?? '복사',
    maskClosable: options.maskClosable ?? true,
    okText: options.okText ?? '복사',
    okButtonProps: {
      ...options.okButtonProps,
      class: COPY_CLASS,
      [CLIPBOARD_TEXT]: unref(options.copyText),
    } as any,
    onOk() {
      return new Promise((resolve: any) => {
        const clipboard = new Clipboard('.' + COPY_CLASS);
        clipboard.on('success', () => {
          clipboard.destroy();
          createMessage.success('성공적으로 복사되었습니다');
          resolve();
        });
        clipboard.on('error', () => {
          createMessage.error('이 브라우저는 자동 복사를 지원하지 않습니다');
          clipboard.destroy();
          resolve();
        });
      });
    },
  });

  // copyText를 동적으로 업데이트
  if (isRef(options.copyText)) {
    watch(options.copyText, (copyText) => {
      modal.update({
        okButtonProps: {
          ...options.okButtonProps,
          class: COPY_CLASS,
          [CLIPBOARD_TEXT]: copyText,
        } as any,
      });
    });
  }
  return modal;
}
