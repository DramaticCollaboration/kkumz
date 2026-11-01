import { propTypes } from '/@/utils/propTypes';

export const vxeProps = () => ({
  rowKey: propTypes.string.def('id'),
  // 컬럼정보
  columns: {
    type: Array,
    required: true,
  },
  // 데이터 소스
  dataSource: {
    type: Array,
    required: true,
  },
  authPre: {
    type: String,
    required: false,
    default: '',
  },
  // 도구 모음 표시 여부
  toolbar: propTypes.bool.def(false),
  // 툴바 구성
  toolbarConfig: propTypes.object.def(() => ({
    // 접두사 접미사;
    slots: ['prefix', 'suffix'],
    // 새 버튼 추가, 삭제 버튼 제거, 선택 취소 버튼 축소;
    btns: ['add', 'remove', 'clearSelection'],
  })),
  // 줄 번호 표시 여부
  rowNumber: propTypes.bool.def(false),
  // 줄 번호 위치 고정 또는 고정되지 않음 【QQYUN-8405】
  rowNumberFixed: propTypes.oneOf(['left', 'none']).def('left'),
  dragSortFixed: propTypes.oneOf(['left', 'none']).def('left'),
  rowSelectionFixed: propTypes.oneOf(['left', 'none']).def('left'),
  // 행을 선택할 수 있나요?
  rowSelection: propTypes.bool.def(false),
  // 행 유형 선택
  rowSelectionType: propTypes.oneOf(['checkbox', 'radio']).def('checkbox'),
  // 행 확장 가능 여부
  rowExpand: propTypes.bool.def(false),
  // 행 구성 펼치기
  expandConfig: propTypes.object.def(() => ({})),
  // 페이지가 로딩 중인가요?
  loading: propTypes.bool.def(false),
  // 테이블 높이
  height: propTypes.oneOfType([propTypes.number, propTypes.string]).def('auto'),
  // 최대 높이
  maxHeight: {
    type: Number,
    default: () => null,
  },
  // 비활성화할 라인
  disabledRows: propTypes.object.def(() => ({})),
  // 모든 컴포넌트를 비활성화할지 여부
  disabled: propTypes.bool.def(false),
  // 드래그 앤 드롭으로 정렬할 수 있는지 여부（고정 컬럼이 있는 경우 드래그 앤 드롭 정렬은 불가능하고, 상하 정렬만 가능합니다.）
  dragSort: propTypes.bool.def(false),
  // 정렬 필드에 저장된 키
  sortKey: propTypes.string.def('orderNum'),
  // 정렬 순서 번호의 시작 값, 기본값은 0입니다.
  sortBegin: propTypes.number.def(0),
  // 크기, 선택값은：medium（Medium）、small（작은）、mini（마이크로）
  size: propTypes.oneOf(['medium', 'small', 'mini']).def('medium'),
  // 경계선 표시 여부
  bordered: propTypes.bool.def(false),
  // 맞춤 열 구성은 기본적으로 설정을 상속합니다.toolbar.custom
  custom: propTypes.bool.def(false),
  // 페이지네이터 매개변수가 설정되면 페이지네이터가 표시됩니다.
  pagination: propTypes.object.def(() => ({})),
  // 행을 클릭할 때 하위 Form을 표시할지 여부
  clickRowShowSubForm: propTypes.bool.def(false),
  // 행을 클릭할 때 기본 Form을 표시할지 여부
  clickRowShowMainForm: propTypes.bool.def(false),
  // 클릭하여 선택할지 여부, 우선순위가 가장 낮음
  clickSelectRow: propTypes.bool.def(false),
  // 데이터 다시 로드 효과 활성화 여부
  reloadEffect: propTypes.bool.def(false),
  // 확인 규칙
  editRules: propTypes.object.def(() => ({})),
  // 행을 비동기식으로 삭제할지 여부입니다. 비동기식 삭제를 수행하려면 이 옵션을 활성화해야 합니다.
  // 제거 이벤트에서 verifyRemove 메소드가 호출될 때만 실제로 삭제됩니다.（모든 새 줄을 삭제하지 않는 한）
  asyncRemove: propTypes.bool.def(false),
  // 컴포넌트를 항상 표시할지 여부입니다. false인 경우 클릭할 때만 컴포넌트가 나타납니다.
  // 메모：이 매개변수는 동적으로 수정할 수 없습니다. 행과 열이 많으면 시스템 성능에 따라 다양한 정도의 지연이 발생합니다.
  // TODO vxe-table의 새 버전에서는 보이는 매개변수가 취소되어 이 기능을 구현할 수 없습니다.
  alwaysEdit: propTypes.bool.def(false),
  // 연결 구성, 어레이, 자세한 구성은 설명서를 참조하세요.
  linkageConfig: propTypes.array.def(() => []),
  // webSocket을 사용하여 보이지 않는 새로 고침을 활성화할지 여부
  socketReload: propTypes.bool.def(false),
  // 동일한 소켓 키가 변경되면 서로 새로 고쳐집니다.
  socketKey: propTypes.string.def('vxe-default'),
  // 새 행을 추가할 때 행 활성화 상태 전환
  addSetActive: propTypes.bool.def(true),
  // 키보드 편집 활성화 여부
  keyboardEdit: propTypes.bool.def(false),
  // 수평 가상 스크롤 구성（행 확장은 지원되지 않습니다.）
  // 【QQYUN-7676】x 스크롤 막대가 스크롤되면 사전은 ID가 됩니다.
  scrollX: propTypes.object.def(() => ({ enabled: false })),
  // 수직 가상 스크롤 구성（행 확장은 지원되지 않습니다.）
  scrollY: propTypes.object.def(() => ({ enabled: true })),
  //【QQYUN-8566】캐시 컬럼 설정의 키（라우팅 페이지 내에서 고유함）
  cacheColumnsKey: propTypes.string.def(''),
  rowClassName: {
    type: [String, Function],
    default: null,
  },
  // 드래그가 허용되지 않는 행 [{'key':field,'value':value}]
  notAllowDrag: propTypes.array.def(() => []),
});

export const vxeEmits = ['save', 'added', 'removed', 'inserted', 'dragged', 'selectRowChange', 'pageChange', 'valueChange', 'blur'];
