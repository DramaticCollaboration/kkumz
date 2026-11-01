import { ModalOptionsPartial } from '/@/hooks/web/useMessage';
import { RenderCallbackParams, Rule } from '/@/components/Form';

export interface JPromptProps extends ModalOptionsPartial {
  // 입력란은 필수인가요?
  required?: boolean;
  // 확인
  rules?: Rule[];
  // 동적 검증
  dynamicRules?: (renderCallbackParams: RenderCallbackParams) => Rule[];
  // 자리 표시자 문자
  placeholder?: string;
  // 입력 상자 기본값
  defaultValue?: string;
}
