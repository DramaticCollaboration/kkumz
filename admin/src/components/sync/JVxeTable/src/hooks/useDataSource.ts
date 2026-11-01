import { nextTick, watch } from 'vue';
import { JVxeDataProps, JVxeRefs, JVxeTableMethods } from '../types';
import { cloneDeep } from 'lodash-es';

export function useDataSource(props, data: JVxeDataProps, methods: JVxeTableMethods, refs: JVxeRefs) {
  watch(
    () => props.dataSource,
    async () => {
      data.disabledRowIds = [];
      data.vxeDataSource.value = cloneDeep(props.dataSource);
      data.vxeDataSource.value.forEach((row, rowIndex) => {
        // 회선이 비활성화되었는지 확인
        if (methods.isDisabledRow(row, rowIndex)) {
          data.disabledRowIds.push(row.id);
        }
        // 연계 에코 데이터 처리
        methods.handleLinkageBackData(row);
      });
      await waitRef(refs.gridRef);
      methods.recalcSortNumber();
    },
    { immediate: true }
  );
}

function waitRef($ref) {
  return new Promise<any>((resolve) => {
    (function next() {
      if ($ref.value) {
        resolve($ref);
      } else {
        nextTick(() => next());
      }
    })();
  });
}
