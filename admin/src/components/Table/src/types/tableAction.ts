import { ButtonProps } from 'ant-design-vue/es/button/buttonTypes';
import { TooltipProps } from 'ant-design-vue/es/tooltip/Tooltip';
import { RoleEnum } from '/@/enums/roleEnum';
export interface ActionItem extends ButtonProps {
  onClick?: Fn;
  label?: string;
  color?: 'success' | 'error' | 'warning';
  icon?: string;
  popConfirm?: PopConfirm;
  disabled?: boolean;
  divider?: boolean;
  // 권한 인코딩은 표시 여부를 제어합니다.
  auth?: RoleEnum | RoleEnum[] | string | string[];
  // 비즈니스 제어 표시 여부
  ifShow?: boolean | ((action: ActionItem) => boolean);
  tooltip?: string | TooltipProps;
  // 맞춤 클래스 이름
  class?: string | Record<string, boolean> | any[];
  // 맞춤 아이콘 색상
  iconColor?: string;
}

export interface PopConfirm {
  title: string;
  okText?: string;
  cancelText?: string;
  confirm: Fn;
  cancel?: Fn;
  icon?: string;
  placement?: string;
  overlayClassName?: string;
}
