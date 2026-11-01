import type { ProjectConfig } from '/#/config';
import { MenuTypeEnum, MenuModeEnum, TriggerEnum, MixSidebarTriggerEnum } from '/@/enums/menuEnum';
import { CacheTypeEnum } from '/@/enums/cacheEnum';
import {
  ContentEnum,
  PermissionModeEnum,
  ThemeEnum,
  RouterTransitionEnum,
  SettingButtonPositionEnum,
  SessionTimeoutProcessingEnum,
  TabsThemeEnum,
} from '/@/enums/appEnum';
import { SIDE_BAR_BG_COLOR_LIST, HEADER_PRESET_BG_COLOR_LIST } from './designSetting';
import { primaryColor } from '../../build/config/themeConfig';

// ! 변경 후에는 브라우저 캐시를 지워야 합니다.
const setting: ProjectConfig = {
  // 설정버튼 표시 여부
  showSettingButton: true,

  // 테마 전환 버튼 표시 여부
  showDarkModeToggle: true,

  // 버튼 위치 설정 옵션
  settingButtonPosition: SettingButtonPositionEnum.HEADER,

  // 권한 모드, 기본 프런트엔드 역할 권한 모드
  // ROUTE_MAPPING: 프런트엔드 모드（메뉴는 라우팅에 의해 생성됩니다. 기본적으로）
  // ROLE：프런트엔드 모드（메뉴 라우팅 별도）
  // BACK：배경 모드
  permissionMode: PermissionModeEnum.BACK,

  // 권한 캐시 저장 위치입니다.기본적으로 localStorage에 저장됨
  permissionCacheType: CacheTypeEnum.LOCAL,

  // 세션 시간 초과 처리 솔루션
  // SessionTimeoutProcessingEnum.ROUTE_JUMP: 로그인 페이지로 이동
  // SessionTimeoutProcessingEnum.PAGE_COVERAGE: 현재 페이지를 덮는 로그인 팝업 창 생성
  sessionTimeoutProcessing: SessionTimeoutProcessingEnum.ROUTE_JUMP,

  // 프로젝트 테마 색상
  themeColor: primaryColor,

  // 웹사이트 회색 모드, 애도 날짜가 있을 경우 켜짐
  grayMode: false,

  // 색약 모드
  colorWeak: false,

  // 다른 시스템에 내장되어 있을 수 있는 메뉴, 상단, 멀티탭 표시 취소 여부
  fullContent: false,

  // 주제 내용 너비
  contentMode: ContentEnum.FULL,

  // 로고 표시 여부
  showLogo: true,

  // 하단 정보 저작권 표시 여부
  showFooter: false,

  // 헤드 구성
  headerSetting: {
    // 배경색
    bgColor: HEADER_PRESET_BG_COLOR_LIST[0],
    // 고정 머리
    fixed: true,
    // 상단 표시 여부
    show: true,
    // 주제
    theme: ThemeEnum.LIGHT,
    // 잠금 화면 기능 켜기
    useLockPage: true,
    // 전체 화면 버튼 표시
    showFullScreen: true,
    // 공식 웹사이트 버튼 표시
    showDoc: false,
    // 메시지 센터 버튼 표시
    showNotice: true,
    // 메뉴 검색 버튼 표시
    showSearch: true,
  },

  // 메뉴 구성
  menuSetting: {
    // 배경색
    bgColor: SIDE_BAR_BG_COLOR_LIST[0],
    // 왼쪽 메뉴 고정 여부
    fixed: true,
    // 메뉴 접기
    collapsed: false,
    // 메뉴 접을 때 메뉴 이름 표시 여부
    collapsedShowTitle: false,
    // 드래그 가능한지 여부
    // Only limited to the opening of the left menu, the mouse has a drag bar on the right side of the menu
    canDrag: true,
    // Whether to show no dom
    show: true,
    // Whether to show dom
    hidden: false,
    // 메뉴 너비
    menuWidth: 245,
    // 메뉴 모드
    mode: MenuModeEnum.INLINE,
    // 메뉴 종류
    type: MenuTypeEnum.SIDEBAR,
    // 메뉴 테마
    theme: ThemeEnum.DARK,
    // 분할 메뉴
    split: false,
    // 상단 메뉴 레이아웃
    topMenuAlign: 'center',
    // 트리거 위치 축소
    trigger: TriggerEnum.HEADER,
    // 하나의 메뉴만 보여주는 아코디언 모드
    accordion: true,
    // 경로 전환 시 왼쪽 하이브리드 메뉴 닫고 메뉴 펼치기
    closeMixSidebarOnChange: false,
    // 왼쪽 혼합 메뉴 모듈은 트리거 모드 '클릭'을 전환합니다 |'hover'
    mixSideTrigger: MixSidebarTriggerEnum.CLICK,
    // 왼쪽 믹스 메뉴 고정 여부
    mixSideFixed: false,
  },

  // 여러 태그
  multiTabsSetting: {
    // 새로 고친 후 탭을 열어 둘지 여부
    cache: false,
    // 켜다
    show: true,
    // 드래그 앤 드롭이 가능한가요?
    canDrag: true,
    // 빠른 작업 켜기
    showQuick: true,
    // 새로고침 버튼 표시 여부
    showRedo: true,
    // 축소 버튼 표시 여부
    showFold: true,
    // 탭 스타일
    theme: TabsThemeEnum.CARD,
  },

  // 애니메이션 구성
  transitionSetting: {
    //  전환 애니메이션 활성화 여부
    // The disabled state will also disable pageLoadinng
    enable: true,

    // 애니메이션 이름 경로 기본 전환 애니메이션
    basicTransition: RouterTransitionEnum.FADE_SIDE,

    // 페이지 전환 로딩을 열지 여부
    // Only open when enable=true
    openPageLoading: true,

    //페이지를 열고 상단 진행 표시줄을 전환할지 여부
    openNProgress: true,
  },

  // KeepAlive 캐시 활성화 여부 개발 중에는 끄는 것이 가장 좋습니다. 그렇지 않으면 매번 캐시를 지워야 합니다.
  openKeepAlive: true,

  // 자동 잠금 화면 시간, 0이면 화면이 잠기지 않습니다. 단위는 분, 기본값은 1시간
  lockTime: 0,

  // 탐색경로 표시
  showBreadCrumb: true,

  // 탐색경로 아이콘 표시
  showBreadCrumbIcon: true,

  // 전역 오류 트래핑 사용 여부
  useErrorHandle: true,

  // 켤지 여부 맨 위로 가기
  useOpenBackTop: true,

  // iframe 페이지를 삽입할 수 있나요?
  canEmbedIFramePage: true,

  // 인터페이스 전환 시 닫히지 않은 메시지 및 알림 삭제 여부
  closeMessageOnSwitch: true,

  // 인터페이스 전환 시 전송되었지만 응답하지 않은 HTTP 요청을 취소할지 여부입니다.
  // 활성화된 경우 개별 인터페이스를 포함하려고 합니다.별도의 인터페이스에서 설정할 수 있습니다.
  removeAllHttpPending: false,
};

export default setting;
