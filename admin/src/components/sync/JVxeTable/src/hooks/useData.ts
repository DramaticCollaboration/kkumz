import { ref, reactive, provide, resolveComponent } from 'vue';
import { useDesign } from '/@/hooks/web/useDesign';
import { JVxeDataProps, JVxeRefs, JVxeTableProps } from '../types';
import { VxeGridInstance } from 'vxe-table';
import { randomString } from '/@/utils/common/compUtils';

export function useData(props: JVxeTableProps): JVxeDataProps {
  const { prefixCls } = useDesign('j-vxe-table');
  provide('prefixCls', prefixCls);
  return {
    prefixCls: prefixCls,
    caseId: `j-vxe-${randomString(8)}`,
    vxeDataSource: ref([]),
    scroll: reactive({ top: 0, left: 0 }),
    scrolling: ref(false),
    defaultVxeProps: reactive({
      // rowId: props.rowKey,
      rowConfig: {
        keyField: props.rowKey,
      },
      // 호버 행 강조 표시
      highlightHoverRow: true,

      // --- 【issues/209】내장된 툴팁이 잘못 배치되므로 기본 제목으로 바꾸십시오. ---
      // 오버플로 숨기기 및 표시 도구 설명
      showOverflow: "title",
      // 헤더 오버플로는 툴팁을 숨기고 표시합니다.
      showHeaderOverflow: "title",
      // --- 【issues/209】내장된 툴팁이 잘못 배치되므로 기본 제목으로 바꾸십시오. ---

      showFooterOverflow: true,
      // 편집 가능한 구성
      editConfig: {
        trigger: 'click',
        mode: 'cell',
        //activeMethod: () => !props.disabled,
        beforeEditMethod: () => !props.disabled,
      },
      expandConfig: {
        iconClose: 'ant-table-row-expand-icon ant-table-row-expand-icon-collapsed',
        iconOpen: 'ant-table-row-expand-icon ant-table-row-expand-icon-expanded',
      },
      // 가상 스크롤링 구성, y축이 xx개의 데이터보다 클 때 가상 스크롤이 활성화됩니다.
      scrollY: {
        gt: 30,
      },
      scrollX: {
        gt: 20,
        // 왼쪽 및 오른쪽 가상 스크롤을 일시적으로 끄기
        enabled: false,
      },
      radioConfig: { highlight: true },
      checkboxConfig: { highlight: true },
      mouseConfig: { selected: false },
      keyboardConfig: {
        // 키 기능 삭제
        isDel: false,
        // Esc 키를 누르면 편집 기능이 닫힙니다.
        isEsc: true,
        // 탭 키 기능
        isTab: true,
        // 편집에 들어가려면 아무 키나 누르세요（기능키 제외）
        isEdit: true,
        // 방향키 기능
        isArrow: true,
        // 키 기능 입력
        isEnter: true,
        // 기능이 지원되는 경우 column.type에 사용됩니다.=체크박스|라디오, 스페이스 키를 활성화하여 체크박스 또는 라디오 버튼 상태 기능 전환
        isChecked: true,
      },
    }),
    selectedRows: ref<any[]>([]),
    selectedRowIds: ref<string[]>([]),
    disabledRowIds: [],
    statistics: reactive({
      has: false,
      sum: [],
      average: [],
    }),
    authsMap: ref(null),
    innerEditRules: {},
    innerLinkageConfig: new Map<string, any>(),
    reloadEffectRowKeysMap: reactive({}),
  };
}

export function useRefs(): JVxeRefs {
  return {
    gridRef: ref<VxeGridInstance>(),
    subPopoverRef: ref<any>(),
    detailsModalRef: ref<any>(),
  };
}

export function useResolveComponent(...t: any[]): any {
  // @ts-ignore
  return resolveComponent(...t);
}
