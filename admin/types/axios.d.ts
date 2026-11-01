export type ErrorMessageMode = 'none' | 'modal' | 'message' | undefined;
export type SuccessMessageMode = 'none' | 'success' | 'error' | undefined;

export interface RequestOptions {
  // 요청 매개변수를 URL에 연결
  joinParamsToUrl?: boolean;
  // 형식 요청 매개변수 시간
  formatDate?: boolean;
  // 요청 결과 처리 여부
  isTransformResponse?: boolean;
  // 로컬 응답 헤더를 반환할지 여부. 응답 헤더를 가져와야 할 때 이 속성을 사용하세요.
  isReturnNativeResponse?: boolean;
  // 기본적으로 URL에 접두사가 추가됩니다.
  joinPrefix?: boolean;
  // 인터페이스 주소, 비어 있는 경우 기본값이 사용됩니다.
  apiUrl?: string;
  // 요청 접합 경로
  urlPrefix?: string;
  // 오류 메시지 프롬프트 유형
  errorMessageMode?: ErrorMessageMode;
  // 성공 메시지 프롬프트 유형
  successMessageMode?: SuccessMessageMode;
  // 타임스탬프 추가 여부
  joinTime?: boolean;
  ignoreCancelToken?: boolean;
  //헤더에 토큰을 보낼지 여부
  withToken?: boolean;
  // 백그라운드 Polling 요청 여부 (에러 모달/메시지 노출 방지 등 분기 처리용)
  isPolling?: boolean;
}

export interface Result<T = any> {
  code: number;
  type: 'success' | 'error' | 'warning';
  message: string;
  result: T;
}

//파일 업로드 매개변수
export interface UploadFileParams {
  // 기타 매개변수
  data?: Recordable;
  // 파일 매개변수 인터페이스 필드 이름
  name?: string;
  // 문서
  file: File | Blob;
  // 파일 이름
  filename?: string;
  [key: string]: any;
}
//파일 반환 매개변수
export interface UploadFileCallBack {
  // 성공 콜백 방법
  success?: any;
  // 응답 헤더를 반환할지 여부. 응답 헤더를 가져와야 할 때 이 속성을 사용하세요.
  isReturnResponse?: boolean;
}
