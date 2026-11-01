interface ScopedSlots {
  customRender: string;
}

interface HrefSlots {
  // 링크 주소
  href: string;
  // fieldHref_필드 이름
  slotName: string;
}

interface OnlineColumn {
  dataIndex?: string;
  title?: string;
  key?: string;
  fieldType?: string;
  width?: number | string;
  align?: string;
  sorter?: string | boolean;
  isTotal?: string | number | boolean;
  groupTitle?: string;
  // 하이퍼링크할 때 HrefSlots의 SlotName과 일치합니다.
  scopedSlots?: ScopedSlots;
  // 일반적으로 사전에 사용됩니다. 사전에서 전달되는 것은 사전 코드된 문자열입니다.
  customRender?: string | Function;
  // 이 유형은 어떤 용도로 사용되는지 모르겠습니다.
  hrefSlotName?: string;
  showLength?: number | string;
  children?: OnlineColumn[];
  sortOrder?: string;
  // 슬롯 대응 제어 방식(목록)
  slots?: ScopedSlots;
  //너비를 초과하는 경우 자동으로 생략되며 정렬 및 필터링에 사용할 수 없습니다.
  ellipsis?: boolean;
  // 열 수정 여부
  fixed?: boolean | 'left' | 'right';
  //필드 유형 int/string 
  dbType?:string;
  //다른 테이블 필드에서 사용됨
  linkField?:string;
}

export { OnlineColumn, HrefSlots };
