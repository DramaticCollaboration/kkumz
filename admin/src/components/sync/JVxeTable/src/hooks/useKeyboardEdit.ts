/*
 * JVxeTable 키보드 조작
 */
import type { VxeTablePropTypes } from 'vxe-table';
import type { JVxeTableProps } from '../types';
import { computed } from 'vue';

/**
 * JVxeTable 키보드 조작
 *
 * @param props
 */
export function useKeyboardEdit(props: JVxeTableProps) {
  // 키보드 조작 활성화 여부
  const enabledKeyboard = computed(() => props.keyboardEdit ?? false);
  // 키보드 구성 재정의
  const keyboardConfig: VxeTablePropTypes.KeyboardConfig = {
    editMethod({ row, column, $table }) {
      // 기본 재정의 유형을 재정의하고 추가 유형으로 변경합니다.
      $table.setActiveCell(row, column);
      return true;
    },
  };
  // 키보드 작동 구성
  const keyboardEditConfig = computed(() => {
    return {
      mouseConfig: {
        selected: enabledKeyboard.value,
      },
      keyboardConfig,
    };
  });

  return {
    keyboardEditConfig,
  };
}
