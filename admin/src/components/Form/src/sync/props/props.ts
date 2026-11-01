//드롭다운 선택 상자 컴포넌트 공개 소품
import { propTypes } from '/@/utils/propTypes';

export const selectProps = {
  //여러개 선택 여부
  isRadioSelection: {
    type: Boolean,
    default: false,
  },
  //반환 값 필드 이름
  rowKey: {
    type: String,
    default: 'id',
  },
  //텍스트 필드 이름 반환
  labelKey: {
    type: String,
    default: 'name',
  },
  //쿼리 매개변수
  params: {
    type: Object,
    default: () => {},
  },
  //선택 버튼 표시 여부
  showButton: propTypes.bool.def(true),
  //선택 목록을 오른쪽에 표시할지 여부
  showSelected: propTypes.bool.def(false),
  //최대 선택 수
  maxSelectCount: {
    type: Number,
    default: 0,
  },
};

//트리 선택 구성요소 공개 소품
export const treeProps = {
  //반환 값 필드 이름
  rowKey: {
    type: String,
    default: 'key',
  },
  //텍스트 필드 이름 반환
  labelKey: {
    type: String,
    default: 'title',
  },
  //초기 확장 수준
  defaultExpandLevel: {
    type: [Number],
    default: 0,
  },
  //루트 PID 값
  startPid: {
    type: [Number, String],
    default: '',
  },
  //기본 키 필드
  primaryKey: {
    type: [String],
    default: 'id',
  },
  //상위 ID 필드
  parentKey: {
    type: [String],
    default: 'parentId',
  },
  //제목 필드
  titleKey: {
    type: [String],
    default: 'title',
  },
  //트리 데이터 구조의 서버측 변환 활성화 여부
  serverTreeData: propTypes.bool.def(true),
  //데이터의 비동기 로드 활성화 여부
  sync: propTypes.bool.def(true),
  //선택 버튼 표시 여부
  showButton: propTypes.bool.def(true),
  //체크박스 표시 여부
  checkable: propTypes.bool.def(true),
  //checkable 노드 선택 가능 여부（상위 및 하위 노드의 선택된 상태는 더 이상 연결되지 않습니다.）
  checkStrictly: propTypes.bool.def(false),
  // 다중 선택 허용 여부, 기본값은 true
  multiple: propTypes.bool.def(true),
};
