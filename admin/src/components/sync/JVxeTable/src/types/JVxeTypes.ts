/** 컴포넌트 유형 */
export enum JVxeTypes {
  // 줄 번호 열
  rowNumber = 'row-number',
  // 열 선택
  rowCheckbox = 'row-checkbox',
  // 라디오 선택 열
  rowRadio = 'row-radio',
  // 열 확장
  rowExpand = 'row-expand',
  // 위아래로 정렬
  rowDragSort = 'row-drag-sort',

  input = 'input',
  inputNumber = 'input-number',
  textarea = 'textarea',
  select = 'select',
  date = 'date',
  datetime = 'datetime',
  time = 'time',
  checkbox = 'checkbox',
  upload = 'upload',
  // 드롭다운 검색
  selectSearch = 'select-search',
  // 드롭다운 다중 선택
  selectMultiple = 'select-multiple',
  // 진행률 표시줄
  progress = 'progress',
  //학과선택
  departSelect = 'depart-select',
  //사용자 선택
  userSelect = 'user-select',

  // 예인선 태그（아직 유용하지 않음）
  tags = 'tags', // 구현될 TODO

  slot = 'slot',
  normal = 'normal',
  hidden = 'hidden',

  // 다음은 맞춤 구성요소입니다.
  popup = 'popup',
  selectDictSearch = 'selectDictSearch',
  radio = 'radio',
  image = 'image',
  file = 'file',
  // 지방 및 자치단체
  pca = 'pca',
}

// vxe의 내장 유형과의 충돌을 방지하기 위해 접두사가 추가됩니다.
// 접두사는 자동으로 추가되므로 코드에서 직접 사용하면 됩니다.（JVxeTypes.input）
export const JVxeTypePrefix = 'j-';

/** VxeTable 렌더링 유형 */
export enum JVxeRenderType {
  editer = 'editer',
  spaner = 'spaner',
  default = 'default',
}
