/**
 * JInput 컴포넌트 유형
 */
export enum JInputTypeEnum {
  //Like
  JINPUT_QUERY_LIKE = 'like',
  //아니요
  JINPUT_QUERY_NE = 'ne',
  //크거나 같음
  JINPUT_QUERY_GE = 'ge',
  //작거나 같음
  JINPUT_QUERY_LE = 'le',
}

/**
 * 패널 설계자가 요구하는 상수 정의
 */
export enum JDragConfigEnum {
  //baseURL
  DRAG_BASE_URL = 'drag-base-url',
  //캐시 접두사 드래그
  DRAG_CACHE_PREFIX = 'drag-cache:',
}
