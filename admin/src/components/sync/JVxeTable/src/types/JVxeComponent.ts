import { ComponentInternalInstance, ExtractPropTypes } from 'vue';
import { useJVxeCompProps } from '/@/components/sync/JVxeTable/hooks';

export namespace JVxeComponent {
  export type Props = ExtractPropTypes<ReturnType<typeof useJVxeCompProps>>;

  interface EnhancedCtx {
    props?: JVxeComponent.Props;
    context?: any;
  }

  /** 구성요소 강화 유형 */
  export interface Enhanced {
    // 등록 매개변수（세부정보 보기：https://xuliangzhan_admin.gitee.io/vxe-table/v4/table/renderer/edit）
    installOptions: {
      // 자동 집중 수업 수업명
      autofocus?: string;
    } & Recordable;
    // 이벤트 인터셉터（호환성을 위해）
    interceptor: {
      // 실현됨：event.clearActived
      // 설명：예를 들어, 컴포넌트의 팝업 패널을 클릭한 후 활성화된 셀은 이때 자동으로 닫혀서는 안 됩니다. false를 반환하면 기본 동작을 방지할 수 있습니다.
      'event.clearActived'?: (params, event, target, ctx?: EnhancedCtx) => boolean;
      // 사용자 정의：event.clearActived.className
      // 설명：원래 매개변수보다 하나 더 많은 매개변수：className, 클릭한 요소의 스타일 이름을 결정하는 데 사용됩니다.（최상위 수준으로 재귀）
      'event.clearActived.className'?: (params, event, target, ctx?: EnhancedCtx) => boolean;
    };
    // 【기능 스위치】
    switches: {
      // editRender 모드 사용 여부（전역이 아닌 현재 구성요소만）
      // true로 설정하면 편집 가능한 아이콘이 헤더 위에 나타납니다.
      editRender?: boolean;
      // false = 구성요소가 트리거된 후에 표시됩니다.）；true = 구성요소는 항상 표시됩니다.
      visible?: boolean;
    };
    // 【섹션 강화】Aspect 이벤트 처리, 일반적으로 특정 메소드가 실행된 후 동기적으로 실행됨
    aopEvents: {
      // 이 이벤트는 편집을 위해 셀이 활성화될 때 트리거됩니다.
      editActived?: (this: ComponentInternalInstance, ...args) => any;
      // 이 이벤트는 편집 상태에서 셀이 닫힐 때 트리거됩니다.
      editClosed?: (this: ComponentInternalInstance, ...args) => any;
      // 반환 값은 셀을 편집할 수 있는지 여부를 결정합니다.
      activeMethod?: (this: ComponentInternalInstance, ...args) => boolean;
    };
    // 【번역 향상】예를 들어, select 컴포넌트가 저장한 값을 구현할 수는 있지만, 스팬 모드에서는 텍스트로 표시해야 합니다.
    translate: {
      // 번역 활성화 여부
      enabled?: boolean;
      /**
       * 【번역 처리 방법】핸들러를 공백으로 두면 기본 번역 방법이 사용됩니다.
       *
       * @param value 번역해야 할 값
       * @returns{*} 번역된 데이터 반환
       */
      handler?: (value, ctx?: EnhancedCtx) => any;
    };
    /**
     * 【가치 향상을 얻으세요】구성요소가 던진 값
     *
     * @param value 데이터베이스에 저장된 값
     * @returns{*} 처리된 값을 반환
     */
    getValue: (value, ctx?: EnhancedCtx) => any;
    /**
     * 【설정값 강화】컴포넌트에 설정된 값
     *
     * @param value 컴포넌트에 의해 트리거되는 값입니다.
     * @returns{*} 처리된 값을 반환
     */
    setValue: (value, ctx?: EnhancedCtx) => any;
    /**
     * 【신규 라인 강화】사용자가 추가를 클릭하면 이벤트가 트리거되어 새 행의 기본값을 반환합니다.
     *
     * @param defaultValue 기본값
     * @param 행 행 데이터
     * @param 열 열 구성.params는 사용자가 구성한 매개변수입니다.
     * @param $table vxe 인스턴스
     * @param renderOptions 렌더링 옵션
     * @param 매개변수는 $table에서 얻을 수 있습니다.
     *
     * @returns는 새로운 값을 반환합니다.
     */
    createValue: (defaultValue: any, ctx?: EnhancedCtx) => any;
  }

  export type EnhancedPartial = Partial<Enhanced>;
}
