import type { Component, Ref, ComputedRef, ExtractPropTypes } from 'vue';
import type { VxeColumnProps } from 'vxe-table/types/column';
import type { JVxeComponent } from './JVxeComponent';
import type { VxeGridInstance, VxeTablePropTypes } from 'vxe-table';
import { JVxeTypes } from './JVxeTypes';
import { vxeProps } from '../vxe.data';
import { useMethods } from '../hooks/useMethods';
import { getJVxeAuths } from '../utils/authUtils';

export type JVxeTableProps = Partial<ExtractPropTypes<ReturnType<typeof vxeProps>>>;
export type JVxeTableMethods = ReturnType<typeof useMethods>['methods'];

export type JVxeVueComponent = {
  enhanced?: JVxeComponent.EnhancedPartial;
} & Component;

type statisticsTypes = 'sum' | 'average';

export type JVxeColumn = IJVxeColumn & Recordable;

/**
 * JVxe 열 구성 항목
 */
export interface IJVxeColumn extends VxeColumnProps {
  type?: any;
  // 행 고유 식별자
  key: string;
  // Form의 예상 값About 프롬프트 정보를 보려면 $를 사용할 수 있습니다.{...}변수 대체 텍스트
  placeholder?: string;
  // 기본값
  defaultValue?: any;
  // 현재 열을 비활성화할지 여부, 기본값은 false
  disabled?: boolean;
  // 정의할 검증 규칙 TODO 유형
  validateRules?: any;
  // 다음 레벨 필드 키 연결
  linkageKey?: string;
  // 컴포넌트에 전달된 다른 속성을 사용자 정의합니다.
  props?: Recordable;
  allowClear?: boolean; // 삭제를 허용하다
  // 【inputNumber】통계 열인지 여부는 inputNumber만 통계 열을 설정할 수 있습니다.통계 열：합계 합계; 평균 평균값
  statistics?: boolean | [statisticsTypes, statisticsTypes?];
  // 【select】
  dictCode?: string; // 사전 코드
  options?: { title?: string; label?: string; text?: string; value: any; disabled?: boolean }[]; // 옵션 드롭다운 목록
  allowInput?: boolean; // 입력 허용
  allowSearch?: boolean; // 검색 허용
  // 【slot】
  slotName?: string; // 슬롯 이름
  // 【checkbox】
  customValue?: [any, any]; // 맞춤 값
  defaultChecked?: boolean; // 기본적으로 선택됨
  // 【upload】 upload
  btnText?: string; // 업로드 버튼 텍스트
  token?: boolean; // 토큰 전달 여부
  responseName?: string; // 반환값 이름
  action?: string; // 주소 업로드
  allowRemove?: boolean; // 삭제 허용 여부
  allowDownload?: boolean; // 다운로드 허용 여부
  // 【드롭다운 사전 검색】
  dict?: string; // 사전 테이블 구성 정보：데이터베이스 테이블 이름, 표시 필드 이름, 저장 필드 이름
  async?: boolean; // 모드 동기화 여부
  tipsContent?: string;
  // 【popup】
  popupCode?: string;
  field?: string;
  orgFields?: string;
  destFields?: string;
}

export interface JVxeRefs {
  gridRef: Ref<VxeGridInstance | undefined>;
  subPopoverRef: Ref<any>;
  detailsModalRef: Ref<any>;
}

export interface JVxeDataProps {
  prefixCls: string;
  // vxe 인스턴스 ID
  caseId: string;
  // vxe 최종 열
  vxeColumns?: ComputedRef;
  // vxe 최종 데이터 소스
  vxeDataSource: Ref<Recordable[]>;
  // 스크롤 막대 위치 기록
  scroll: { top: number; left: number };
  // 현재 스크롤 중인지 여부
  scrolling: Ref<boolean>;
  // vxe 기본 구성
  defaultVxeProps: object;
  // 왼쪽 선택 상자 바인딩
  selectedRows: Ref<any[]>;
  // 왼쪽 선택 상자의 선택된 ID를 바인딩합니다.
  selectedRowIds: Ref<string[]>;
  disabledRowIds: string[];
  // 통계 열 구성
  statistics: {
    has: boolean;
    sum: string[];
    average: string[];
  };
  // 현재 Form과 관련된 모든 승인 정보
  authsMap: Ref<Nullable<ReturnType<typeof getJVxeAuths>>>;
  // 내장 EditRules
  innerEditRules: Recordable<VxeTablePropTypes.EditRules[]>;
  // 연결된 드롭다운 옵션（다양한 드롭다운 옵션을 분리하는 데 사용됩니다.）
  // 내부 연계 구성, 맵
  innerLinkageConfig: Map<string, any>;
  // 데이터 새로 고침 효과가 켜진 행
  reloadEffectRowKeysMap: Recordable;
}

export interface JVxeLinkageConfig {
  // 첫 번째 연결 수준의 핵심
  key: string;
  // 데이터를 얻는 방법
  requestData: (parent: string) => Promise<any>;
}

export { JVxeTypes };
