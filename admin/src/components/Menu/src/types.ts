// import { ComputedRef } from 'vue';
// import { ThemeEnum } from '/@/enums/appEnum';
// import { MenuModeEnum } from '/@/enums/menuEnum';
export interface MenuState {
  // 기본 선택 목록
  defaultSelectedKeys: string[];

  // 모델
  // mode: MenuModeEnum;

  // // 주제
  // theme: ComputedRef<ThemeEnum> | ThemeEnum;

  // 톱니 모양
  inlineIndent?: number;

  // 배열 확장
  openKeys: string[];

  // 현재 선택된 메뉴 항목 키 배열
  selectedKeys: string[];

  // 축소된 상태에서 확장된 배열
  collapsedOpenKeys: string[];
}
