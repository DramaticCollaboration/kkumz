import type { App } from 'vue';
// vxe-table 소개
import 'xe-utils';
import VXETable /*Grid*/ from 'vxe-table';
import VXETablePluginAntd from 'vxe-table-plugin-antd';
import 'vxe-table/lib/style.css';

import JVxeTable from './JVxeTable';
import { getEventPath } from '/@/utils/common/compUtils';
import { registerAllComponent } from './utils/registerUtils';
import { getEnhanced } from './utils/enhancedUtils';

export function registerJVxeTable(app: App) {
  // VXETable 전역 구성
  const VXETableSettings = {
    // Z-색인 시작 값
    zIndex: 1000,
    table: {},
  };

  // 이벤트 인터셉터 추가 event.clearActived
  // 예를 들어, 컴포넌트의 팝업 패널을 클릭한 후 활성화된 셀은 이때 자동으로 닫혀서는 안 됩니다. false를 반환하면 기본 동작을 방지할 수 있습니다.
  VXETable.interceptor.add('event.clearActived', preventClosingPopUp);
  VXETable.interceptor.add('event.clearEdit', preventClosingPopUp);
  // 플러그인 등록
  VXETable.use(VXETablePluginAntd);
  // 사용자 정의 컴포넌트 등록
  registerAllComponent();
  // 등록 방법 실행
  app.use(VXETable, VXETableSettings);
  app.component('JVxeTable', JVxeTable);
}


/**
 * 라인 편집 중 팝업 닫기 방지
 * @param params
 */
function preventClosingPopUp(this: any, params) {
  // 컴포넌트 개선 사항 받기
  let col = params.column.params;
  let { $event } = params;
  const interceptor = getEnhanced(col.type).interceptor;
  // 실행 강화
  let flag = interceptor['event.clearActived']?.call(this, ...arguments);
  if (flag === false) {
    return false;
  }

  let path = getEventPath($event);
  for (let p of path) {
    let className: any = p.className || '';
    className = typeof className === 'string' ? className : className.toString();

    /* --- 다음 컴포넌트는 특별하게 취급되며 다음 레이블을 클릭해도 편집 상태가 지워지지 않습니다. --- */

    // 클릭한 라벨은 JInputPop입니다.
    if (className.includes('j-input-pop')) {
      return false;
    }
    // 클릭한 탭은 JPopup의 팝업 레이어, 부서 선택, 사용자 선택입니다.
    if (/j-(popup|depart-select|user-select)-modal/.test(className)) {
      return false;
    }
    // 날짜 선택기를 클릭하세요.
    if (className.includes('j-vxe-date-picker')) {
      return false;
    }
    // 실행 강화
    let flag = interceptor['event.clearActived.className']?.call(this, className, ...arguments);
    if (flag === false) {
      return false;
    }
  }
}
