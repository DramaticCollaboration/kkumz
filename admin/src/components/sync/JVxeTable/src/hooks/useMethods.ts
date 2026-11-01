import { Ref, watch } from 'vue';
import XEUtils from 'xe-utils';
import { simpleDebounce } from '/@/utils/common/compUtils';
import { JVxeDataProps, JVxeRefs, JVxeTableProps, JVxeTypes } from '../types';
import { getEnhanced } from '../utils/enhancedUtils';
import { VxeTableInstance, VxeTablePrivateMethods } from 'vxe-table';
import { cloneDeep } from 'lodash-es';
import { isArray, isEmpty, isNull, isString } from '/@/utils/is';
import { useLinkage } from './useLinkage';
import { useWebSocket } from './useWebSocket';
import { getPrefix, getJVxeAuths } from '../utils/authUtils';
import { excludeKeywords } from '../componentMap';
import { useColumnsCache } from './useColumnsCache';

export function useMethods(props: JVxeTableProps, { emit }, data: JVxeDataProps, refs: JVxeRefs, instanceRef: Ref) {
  let xTableTemp: VxeTableInstance & VxeTablePrivateMethods;

  function getXTable() {
    if (!xTableTemp) {
      // !. TypeScriptAbout null이 아닌 어설션
      xTableTemp = refs.gridRef.value!.getRefMaps().refTable.value;
    }
    return xTableTemp;
  }

  // noinspection JSUnusedGlobalSymbols
  const hookMethods = {
    getXTable,
    addRows,
    pushRows,
    insertRows,
    addOrInsert,
    setValues,
    getValues,
    getTableData,
    getNewData,
    getNewDataWithId,
    getIfRowById,
    getNewRowById,
    getDeleteData,
    getSelectionData,
    getSelectedData,
    removeRows,
    removeRowsById,
    removeSelection,
    resetScrollTop,
    validateTable,
    fullValidateTable,
    clearSelection,
    filterNewRows,
    isDisabledRow,
    recalcDisableRows,
    rowResort,
  };

  // 다단계 연계
  const linkageMethods = useLinkage(props, data, hookMethods);
  // WebSocket 보이지 않는 새로 고침
  const socketMethods = useWebSocket(props, data, hookMethods);

  // 외부에서 명시적으로 호출할 수 있는 메서드
  const publicMethods = {
    ...hookMethods,
    ...linkageMethods,
    ...socketMethods,
  };

  /** vxe 스크롤 막대 위치 모니터링 */
  function handleVxeScroll(event) {
    let { scroll } = data;

    // 스크롤바 위치 기록
    scroll.top = event.scrollTop;
    scroll.left = event.scrollLeft;

    refs.subPopoverRef.value?.close();
    data.scrolling.value = true;
    closeScrolling();
  }

  // 라디오 옵션을 수동으로 선택하면 이벤트가 발생합니다.
  function handleVxeRadioChange(event) {
    let row = event.$table.getRadioRecord();
    data.selectedRows.value = row ? [row] : [];
    handleSelectChange('radio', data.selectedRows.value, event);
  }

  // 모두 수동으로 선택하면 이벤트가 발생합니다.
  function handleVxeCheckboxAll(event) {
    data.selectedRows.value = event.$table.getCheckboxRecords();
    handleSelectChange('checkbox-all', data.selectedRows.value, event);
  }

  // 수동으로 확인하고 값이 변경되면 이벤트가 발생합니다.
  function handleVxeCheckboxChange(event) {
    data.selectedRows.value = event.$table.getCheckboxRecords();
    handleSelectChange('checkbox', data.selectedRows.value, event);
  }

  // 행 선택 변경 이벤트
  function handleSelectChange(type, selectedRows, $event) {
    let action;
    if (type === 'radio') {
      action = 'selected';
    } else if (type === 'checkbox') {
      action = selectedRows.includes($event.row) ? 'selected' : 'unselected';
    } else {
      action = 'selected-all';
    }

    data.selectedRowIds.value = selectedRows.map((row) => row.id);
    trigger('selectRowChange', {
      type: type,
      action: action,
      $event: $event,
      row: $event.row,
      selectedRows: data.selectedRows.value,
      selectedRowIds: data.selectedRowIds.value,
    });
  }

  // 셀을 클릭하면 이벤트가 트리거됩니다.
  function handleCellClick(event) {
    let { row, column, $event, $table } = event;

    // 편집 가능을 클릭했습니다
    if (column.editRender) {
      refs.subPopoverRef.value?.close();
      return;
    }

    // 세부정보 표시
    if (column.params?.showDetails) {
      refs.detailsModalRef.value?.open(event);
    } else if (refs.subPopoverRef.value) {
      refs.subPopoverRef.value.toggle(event);
    } else if (props.clickSelectRow) {
      let className = $event.target.className || '';
      className = isString(className) ? className : className.toString();
      // 클릭이 확장되어 처리되지 않습니다.
      if (className.includes('vxe-table--expand-btn')) {
        return;
      }
      // 클릭된 것은 체크박스이며 처리되지 않습니다.
      if (className.includes('vxe-checkbox--icon') || className.includes('vxe-cell--checkbox')) {
        return;
      }
      // 클릭은 라디오이며 처리가 수행되지 않습니다.
      if (className.includes('vxe-radio--icon') || className.includes('vxe-cell--radio')) {
        return;
      }
      if (props.rowSelectionType === 'radio') {
        $table.setRadioRow(row);
        handleVxeRadioChange(event);
      } else {
        $table.toggleCheckboxRow(row);
        handleVxeCheckboxChange(event);
      }
    }
  }

  // 이 이벤트는 편집을 위해 셀이 활성화될 때 트리거됩니다.
  function handleEditActived({ column }) {
    // 실행 강화
    getEnhanced(column.params.type).aopEvents.editActived!.apply(instanceRef.value, arguments as any);
  }

  // 이 이벤트는 편집 상태에서 셀이 닫힐 때 트리거됩니다.
  function handleEditClosed({ column }) {
    // 실행 강화
    getEnhanced(column.params.type).aopEvents.editClosed!.apply(instanceRef.value, arguments as any);
  }

  // 반환 값은 행을 선택할 수 있는지 여부를 결정합니다.
  function handleCheckMethod({ row }) {
    if (props.disabled) {
      return false;
    }
    return !data.disabledRowIds.includes(row.id);
  }

  // 반환 값은 셀을 편집할 수 있는지 여부를 결정합니다.
  function handleActiveMethod({ row, column }) {
    let flag = (() => {
      if (props.disabled) {
        return false;
      }
      if (data.disabledRowIds.includes(row.id)) {
        return false;
      }
      if (column.params?.disabled) {
        return false;
      }
      // 실행 강화
      return getEnhanced(column.params.type).aopEvents.activeMethod!.apply(instanceRef.value, arguments as any) ?? true;
    })();
    if (!flag) {
      getXTable().clearActived();
    }
    return flag;
  }

  /**
   * 회선이 비활성화되었는지 확인
   * @param 행 행 데이터
   * @param rowIndex 행 번호
   * @param 강제로 판결을 내릴지 여부
   */
  function isDisabledRow(row, rowIndex: number | boolean = -1, force = true) {
    if(typeof rowIndex === 'boolean'){
      force = rowIndex;
      rowIndex = -1;
    }
    if (!force) {
      return !data.disabledRowIds.includes(row.id);
    }
    if (props.disabledRows == null || isEmpty(props.disabledRows)) {
      return false;
    }
    let disabled: boolean = false;
    let keys: string[] = Object.keys(props.disabledRows);
    for (const key of keys) {
      // 속성이 존재하는지 확인
      if (row.hasOwnProperty(key)) {
        let value = row[key];
        let temp: any = props.disabledRows![key];
        // 비활성화 규칙은 함수일 수 있습니다.
        if (typeof temp === 'function') {
          disabled = temp(value, row, rowIndex);
        } else if (isArray(temp)) {
          // 비활성화 규칙은 배열일 수 있습니다.
          disabled = temp.includes(value);
        } else {
          // 비활성화 규칙은 특정 값일 수 있습니다.
          disabled = temp === value;
        }
        if (disabled) {
          break;
        }
      }
    }
    return disabled;
  }

  // 비활성화된 행 다시 계산
  function recalcDisableRows() {
    let xTable = getXTable();
    data.disabledRowIds = [];
    const { tableFullData } = xTable.internalData;
    tableFullData.forEach((row, rowIndex) => {
      // 회선이 비활성화되었는지 확인
      if (isDisabledRow(row, rowIndex)) {
        data.disabledRowIds.push(row.id);
      }
    });
    xTable.updateData();
  }

  // 비활성화된 행을 수신하고 변경 시 비활성화된 행을 다시 계산합니다.
  watch(
    () => props.disabledRows,
    () => recalcDisableRows()
  );

  // 반환 값은 행의 확장 및 축소를 허용할지 여부를 결정합니다.
  function handleExpandToggleMethod({ expanded }) {
    return !(expanded && props.disabled);
  }

  // 데이터 스크롤 손떨림 방지 모드 설정
  const closeScrolling = simpleDebounce(function () {
    data.scrolling.value = false;
  }, 100);

  /** 통계 정보를 표시하기 위해 사용되는 테일 데이터 처리 방법 */
  function handleFooterMethod({ columns, data: $data }) {
    const { statistics } = data;
    let footers: any[] = [];
    if (statistics.has) {
      if (statistics.sum.length > 0) {
        footers.push(
          getFooterStatisticsMap({
            columns: columns,
            title: '총',
            checks: statistics.sum,
            method: (column) => XEUtils.sum($data, column.property),
          })
        );
      }
      if (statistics.average.length > 0) {
        footers.push(
          getFooterStatisticsMap({
            columns: columns,
            title: '평균',
            checks: statistics.average,
            method: (column) => XEUtils.mean($data, column.property),
          })
        );
      }
    }
    return footers;
  }

  /** 하단 통계 지도 가져오기 */
  function getFooterStatisticsMap({ columns, title, checks, method }) {
    return columns.map((column, columnIndex) => {
      if (columnIndex === 0) {
        return title;
      }
      if (checks.includes(column.property)) {
        return method(column, columnIndex);
      }
      return null;
    });
  }

  // 새 행을 생성하고 자동으로 기본값을 추가합니다.
  function createRow(record: Recordable = {}) {
    let xTable = getXTable();
    // 기본값 추가
    xTable.internalData.tableFullColumn.forEach((column) => {
      let col = column.params;
      // 등록할 수 없는 열은 개선되지 않습니다.
      if (col && !excludeKeywords.includes(col.type)) {
        if (col.key && (record[col.key] == null || record[col.key] === '')) {
          // 기본값 설정
          let createValue = getEnhanced(col.type).createValue;
          let defaultValue = col.defaultValue ?? '';
          let ctx = { context: { row: record, column, $table: xTable } };
          record[col.key] = createValue(defaultValue, ctx);
        }
        // 프로세스 연결 컬럼
        if (col.type === JVxeTypes.select && data.innerLinkageConfig.size > 0) {
          // 현재 열이 연결된 열인지 확인
          if (data.innerLinkageConfig.has(col.key)) {
            let configItem = data.innerLinkageConfig.get(col.key);
            linkageMethods.getLinkageOptionsAsync(configItem, '');
          }
        }
      } else if (col?.type === JVxeTypes.hidden) {
        record[col.key] = col.defaultValue ?? '';
      }
    });
    return record;
  }

  async function addOrInsert(rows: Recordable | Recordable[] = {}, index, triggerName, options?: IAddRowsOptions) {
    let xTable = getXTable();
    let records;
    if (isArray(rows)) {
      records = rows;
    } else {
      records = [rows];
    }
    // 기본값을 탐색하고 추가합니다.
    records.forEach((record) => createRow(record));
    let setActive = options?.setActive ?? props.addSetActive ?? true;
    let result = await pushRows(records, { index: index, setActive });
    // 삽입된 행에 대해 반복
    // 온라인 js가 향상되면 전달된 값이 우선하며 기본값은 할당되지 않습니다.
    if (!(options?.isOnlineJS ?? false)) {
      if (triggerName != null) {
        for (let i = 0; i < result.rows.length; i++) {
          let row = result.rows[i];
          trigger(triggerName, {
            row: row,
            rows: result.rows,
            insertIndex: index,
            $table: xTable,
            target: instanceRef.value,
            isModalData: options?.isModalData
          });
        }
      }
    }
    return result;
  }

  // 행 추가 또는 삽입 시 선택적 매개변수
  interface IAddRowsOptions {
    // onlineJS 향상으로 인해 발생합니까?
    isOnlineJS?: boolean;
    // 편집 상태 활성화 여부
    setActive?: boolean;
    //변경 이벤트를 트리거해야 합니까?
    emitChange?:boolean
    // 모달팝업창에서 추가한 데이터인가요?
    isModalData?:boolean
  }

  /**
   * 하나 이상의 행 추가
   *
   * @param rows
   * @param 옵션 매개변수
   * @return
   */
  async function addRows(rows: Recordable | Recordable[] = {}, options?: IAddRowsOptions) {
    let result = await addOrInsert(rows, -1, 'added', options);
    if(options && options!.emitChange==true){
      trigger('valueChange', {column: 'all', row: result.row})
    }
    let xTable = getXTable();
    setTimeout(() => {
      xTable.scrollToRow(result.row);
    }, 0);
    return result;
  }

  /**
   * 하나 이상의 임시 데이터 행을 추가합니다. 기본값은 채워지지 않습니다. 전달된 내용은 모두 추가됩니다.
   * @param rows
   * @param 옵션 옵션
   * @param options.setActive 마지막 줄의 편집 모드 활성화 여부
   */
  async function pushRows(rows: Recordable | Recordable[] = {}, options = { setActive: false, index: -1 }) {
    let xTable = getXTable();
    let { setActive, index } = options;
    index = index === -1 ? index : xTable.internalData.tableFullData[index];
    // 행 삽입
    let result = await xTable.insertAt(rows, index);
    if (setActive) {
      // 마지막 줄About 편집 모드 활성화
      xTable.setActiveRow(result.rows[result.rows.length - 1]);
    }
    await recalcSortNumber();
    return result;
  }

  /**
   * 하나 이상의 임시 데이터 행 삽입
   *
   * @param rows
   * @param 색인 아래 첨자 추가, 숫자, 필수
   * @param 옵션 매개변수
   * @return
   */
  function insertRows(rows: Recordable | Recordable[] = {}, index: number, options?: IAddRowsOptions) {
    if (index < 0) {
      console.warn(`【JVxeTable】insertRows：인덱스는 숫자를 전달해야 하며 -1보다 큽니다.`);
      return;
    }
    return addOrInsert(rows, index, 'inserted', options);
  }

  /** 테이블 형식으로 값 가져오기 */
  function getValues(callback, rowIds) {
    let tableData = getTableData({ rowIds: rowIds });
    callback('', tableData);
  }

  /** 테이블 데이터 가져오기 */
  function getTableData(options: any = {}) {
    let { rowIds } = options;
    let tableData;
    // 지정된 ID가 있는 행만 쿼리
    if (isArray(rowIds) && rowIds.length > 0) {
      tableData = [];
      rowIds.forEach((rowId) => {
        let { row } = getIfRowById(rowId);
        if (row) {
          tableData.push(row);
        }
      });
    } else {
      // 모든 행 쿼리
      tableData = getXTable().getTableData().fullData;
    }
    return filterNewRows(tableData, false);
  }

  /** 새로운 데이터만 가져오기 */
  function getNewData() {
    let newData = getNewDataWithId();
    newData.forEach((row) => delete row.id);
    return newData;
  }

  /** ID로 새로 추가된 데이터만 가져오기 */
  function getNewDataWithId() {
    let xTable = getXTable();
    return cloneDeep(xTable.getInsertRecords());
  }

  /** ID를 기준으로 행을 가져오고 새로 추가된 행도 찾을 수 있습니다. */
  function getIfRowById(id) {
    let xTable = getXTable();
    let row = xTable.getRowById(id),
      isNew = false;
    if (!row) {
      row = getNewRowById(id);
      if (!row) {
        console.warn(`JVxeTable.getIfRowById：ID를 찾을 수 없습니다."${id}"행`);
        return { row: null };
      }
      isNew = true;
    }
    return { row, isNew };
  }

  /** 임시 ID로 새 행 가져오기 */
  function getNewRowById(id) {
    let records = getXTable().getInsertRecords();
    for (let record of records) {
      if (record.id === id) {
        return record;
      }
    }
    return null;
  }

  /**
   * 추가된 행 필터링
   * @param 필터링할 행 행 데이터
   * @param remove true = 새 추가 삭제, false=아이디만 삭제
   * @param handler function
   */
  function filterNewRows(rows, remove = true, handler?: Fn) {
    let insertRecords = getXTable().getInsertRecords();
    let records: Recordable[] = [];
    for (let row of rows) {
      let item = cloneDeep(row);
      if (insertRecords.includes(row)) {
        handler ? handler({ item, row, insertRecords }) : null;
        if (remove) {
          continue;
        }
        delete item.id;
      }
      records.push(item);
    }
    return records;
  }

  /**
   * 스크롤 막대의 상단 위치 재설정
   * @param top 새로운 상단 위치. 공백으로 두면 마지막으로 기록된 위치로 스크롤됩니다. 탭 전환 시 스크롤 막대가 자동으로 위로 스크롤되는 현상과 흰색 화면 문제를 해결하는 데 사용됩니다.
   */
  function resetScrollTop(top?) {
    let xTable = getXTable();
    xTable.scrollTo(null, top == null || top === '' ? data.scroll.top : top);
  }

  /** 테이블을 확인하고, 실패하면 errMap을 반환하고, 성공하면 null을 반환합니다. */
  async function validateTable(rows?) {
    let xTable = getXTable();
    const errMap = await xTable.validate(rows ?? true).catch((errMap) => errMap);
    return errMap ? errMap : null;
  }

  /** 검증 완료 */
  async function fullValidateTable(rows?) {
    let xTable = getXTable();
    const errMap = await xTable.fullValidate(rows ?? true).catch((errMap) => errMap);
    return errMap ? errMap : null;
  }

  type setValuesParam = { rowKey: string; values: Recordable };

  /**
   * 행 또는 열의 값 설정
   *
   * @param values
   * @return 영향을 받은 셀의 수를 반환합니다.
   */
  function setValues(values: setValuesParam[]): number {
    if (!isArray(values)) {
      console.warn(`[JVxeTable] setValues는 배열을 전달해야 합니다`);
      return 0;
    }
    let xTable = getXTable();
    let count = 0;
    values.forEach((item) => {
      let { rowKey, values: record } = item;
      let { row } = getIfRowById(rowKey);
      if (!row) {
        return;
      }
      Object.keys(record).forEach((colKey) => {
        let column = xTable.getColumnByField(colKey);
        if (column) {
          let oldValue = row[colKey];
          let newValue = record[colKey];
          if (newValue !== oldValue) {
            row[colKey] = newValue;
            // 트리거 값변경 이벤트
            trigger('valueChange', {
              type: column.params.type,
              value: newValue,
              oldValue: oldValue,
              col: column.params,
              column: column,
              isSetValues: true,
              row: {...row}
            });
            count++;
          }
        } else {
          console.warn(`[JVxeTable] setValues에서 다음About 키를 찾지 못했습니다."${colKey}"칼럼`);
        }
      });
    });
    if (count > 0) {
      xTable.updateData();
    }
    return count;
  }

  /** 선택한 행 지우기 */
  async function clearSelection() {
    const xTable = getXTable();
    let event = { $table: xTable, target: instanceRef.value };
    if (props.rowSelectionType === JVxeTypes.rowRadio) {
      await xTable.clearRadioRow();
      handleVxeRadioChange(event);
    } else {
      await xTable.clearCheckboxRow();
      handleVxeCheckboxChange(event);
    }
  }

  /**
   * 선택한 데이터 가져오기
   * @param isFull인 경우 isFull=true인 경우 전체 테이블의 선택된 데이터를 가져옵니다.
   */
  function getSelectionData(isFull?: boolean) {
    const xTable = getXTable();
    if (props.rowSelectionType === JVxeTypes.rowRadio) {
      let row = xTable.getRadioRecord(isFull);
      if (isNull(row)) {
        return [];
      }
      return filterNewRows([row], false);
    } else {
      return filterNewRows(xTable.getCheckboxRecords(isFull), false);
    }
  }

  /** 삭제된 데이터만 가져오기（추가 및 삭제된 데이터는 복구되지 않습니다.） */
  function getDeleteData() {
    return filterNewRows(getXTable().getRemoveRecords(), false);
  }

  /** 하나 이상의 데이터 행 삭제 */
  async function removeRows(rows, asyncRemove = false) {
    const xTable = getXTable();
    const removeEvent: any = { deleteRows: rows, $table: xTable };
    if (asyncRemove) {
      const selectedRows = Array.isArray(rows) ? rows : [rows];
      const deleteOldRows = filterNewRows(selectedRows);
      if (deleteOldRows.length) {
        return new Promise((resolve) => {
          // 삭제를 확인하세요. 이 메서드를 호출해야만 삭제됩니다.
          removeEvent.confirmRemove = async () => {
            const insertRecords = xTable.getInsertRecords();
            selectedRows.forEach((item) => {
              // 새로 추가된 데이터 ID 삭제
              if (insertRecords.includes(item)) {
                delete item.id;
              }
            });
            const res = await xTable.remove(rows);
            await recalcSortNumber();
            resolve(res);
          };
          trigger('removed', removeEvent);
        });
      } else {
        // 새로운 행은 기다리지 않고 즉시 삭제됩니다.
        const res = await xTable.remove(rows);
        removeEvent.confirmRemove = () => {};
        trigger('removed', removeEvent);
        await recalcSortNumber();
        return res;
      }
    } else {
      const res = await xTable.remove(rows);
      trigger('removed', removeEvent);
      await recalcSortNumber();
      return res;
    }
  }

  /** ID를 기준으로 하나 이상의 행 삭제 */
  function removeRowsById(rowId) {
    let rowIds;
    if (isArray(rowId)) {
      rowIds = rowId;
    } else {
      rowIds = [rowId];
    }
    let rows = rowIds
      .map((id) => {
        let { row } = getIfRowById(id);
        if (!row) {
          return;
        }
        if (row) {
          return row;
        } else {
          console.warn(`【JVxeTable】removeRowsById：${id}존재하지 않는다`);
          return null;
        }
      })
      .filter((row) => row != null);
    return removeRows(rows);
  }

  // 선택한 데이터 삭제
  async function removeSelection() {
    let xTable = getXTable();
    let res;
    if (props.rowSelectionType === JVxeTypes.rowRadio) {
      res = await xTable.removeRadioRow();
    } else {
      res = await xTable.removeCheckboxRow();
    }
    await clearSelection();
    await recalcSortNumber();
    return res;
  }

  /** 정렬 필드의 숫자 값을 다시 계산합니다. */
  async function recalcSortNumber(force = false) {
    if (props.dragSort || force) {
      let xTable = getXTable();
      let sortKey = props.sortKey ?? 'orderNum';
      let sortBegin = props.sortBegin ?? 0;
      xTable.internalData.tableFullData.forEach((data) => (data[sortKey] = sortBegin++));
      // 4.1.0
      //await xTable.updateCache();
      // 4.1.1
      await xTable.cacheRowMap()
      return await xTable.updateData();
    }
  }

  /**
   * 테이블 정렬
   * @param oldIndex
   * @param newIndex
   * @param force 정렬
   */
  async function doSort(oldIndex: number, newIndex: number, force = false) {
    if (props.dragSort || force) {
      let xTable = getXTable();
      let sort = (array) => {
        // 오래된 데이터 저장 및 항목 삭제
        let row = array.splice(oldIndex, 1)[0];
        // newIndex에 이전 데이터 추가
        array.splice(newIndex, 0, row);
      };
      sort(xTable.internalData.tableFullData);
      if (xTable.keepSource) {
        sort(xTable.internalData.tableSourceData);
      }
      return await recalcSortNumber(force);
    }
  }

  /** 행 재정렬 */
  function rowResort(oldIndex: number, newIndex: number) {
    return doSort(oldIndex, newIndex, true);
  }

  // ---------------- 권한 제어 시작 ----------------
  // 로드 권한
  function loadAuthsMap() {
    if (!props.authPre || props.authPre.length == 0) {
      data.authsMap.value = null;
    } else {
      data.authsMap.value = getJVxeAuths(props.authPre);
    }
  }

  /**
   * 권한 코드에 따라 권한 얻기
   * @param authCode
   */
  function getAuth(authCode) {
    if (data.authsMap.value != null && props.authPre) {
      let prefix = getPrefix(props.authPre);
      return data.authsMap.value.get(prefix + authCode);
    }
    return null;
  }

  // 열 권한 얻기
  function getColAuth(key: string) {
    return getAuth(key);
  }

  // 버튼 권한 결정
  function hasBtnAuth(key: string) {
    return getAuth('btn:' + key)?.isAuth ?? true;
  }

  // ---------------- 권한 제어 종료 ----------------

  /* --- Helper Method ---*/

  function created() {
    loadAuthsMap();
  }

  // 트리거 이벤트
  function trigger(name, event: any = {}) {
    event.$target = instanceRef.value;
    event.$table = getXTable();
    //온라인으로 향상된 매개변수 호환성
    event.target = instanceRef.value;
    emit(name, event);
  }

  /**
   * 선택한 행 가져오기 - getSelectionData와의 차이점은 새 행에 대해서도 ID가 반환된다는 점입니다.
   * onlinePopForm용
   * @param isFull
   */
  function getSelectedData(isFull?: boolean) {
    const xTable = getXTable();
    let rows:any[] = []
    if (props.rowSelectionType === JVxeTypes.rowRadio) {
      let row = xTable.getRadioRecord(isFull);
      if (isNull(row)) {
        return [];
      }
      rows = [row]
    } else {
      rows = xTable.getCheckboxRecords(isFull)
    }
    let records: Recordable[] = [];
    for (let row of rows) {
      let item = cloneDeep(row);
      records.push(item);
    }
    return records;
  }
  /**
   *  2024-03-21
   *  liaozhiyang
   *  VXETable 열 설정은 캐시된 필드 이름을 저장합니다.
   * */
  function handleCustom({ type, $grid }) {
    const { saveSetting, resetSetting } = useColumnsCache({ cacheColumnsKey: props.cacheColumnsKey });
    if (type === 'confirm') {
      saveSetting($grid);
    } else if (type == 'reset') {
      resetSetting($grid);
    }
  }

  return {
    methods: {
      trigger,
      ...publicMethods,
      closeScrolling,
      doSort,
      recalcSortNumber,
      handleVxeScroll,
      handleVxeRadioChange,
      handleVxeCheckboxAll,
      handleVxeCheckboxChange,
      handleFooterMethod,
      handleCellClick,
      handleEditActived,
      handleEditClosed,
      handleCheckMethod,
      handleActiveMethod,
      handleExpandToggleMethod,
      getColAuth,
      hasBtnAuth,
      handleCustom,
    },
    publicMethods,
    created,
  };
}
