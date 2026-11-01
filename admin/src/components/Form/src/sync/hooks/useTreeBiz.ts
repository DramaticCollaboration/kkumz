import type { Ref } from 'vue';
import { inject, reactive, ref, computed, unref, watch, nextTick } from 'vue';
import { TreeActionType } from '/@/components/Tree';
import { listToTree } from '/@/utils/common/compUtils';

export function useTreeBiz(treeRef, getList, props, realProps, emit) {
  //드롭다운 상자 옵션 수신
  const selectOptions = inject('selectOptions', ref<Array<object>>([]));
  //선택한 값 받기
  const selectValues = <object>inject('selectValues', reactive({}));
  // 에코가 로드 중인지 여부
  const loadingEcho = inject<Ref<boolean>>('loadingEcho', ref(false));
  //데이터세트
  const treeData = ref<Array<object>>([]);
  //선택한 값
  const checkedKeys = ref<Array<string | number>>([]);
  //선택한 행 레코드
  const selectRows = ref<Array<object>>([]);
  //팝업 모드 열기 여부
  const openModal = ref(false);
  // 상위-하위 연결 활성화 여부입니다. 다중 선택이 불가능할 경우 상위-하위 연결이 항상 취소됩니다.
  const getCheckStrictly = computed(() => (realProps.multiple ? props.checkStrictly : true));
  // 에코를 처음 로드하는 경우에도 처음 로드할 때만 로딩이 표시됩니다.
  let isFirstLoadEcho = true;

  /**
   * selectValues ​​변경 사항 모니터링
   */
  watch(
    selectValues,
    ({ value: values }: Recordable) => {
      if(!values){
        return;
      }
      if (openModal.value == false && values.length > 0) {
        loadingEcho.value = isFirstLoadEcho;
        isFirstLoadEcho = false;
        onLoadData(null, values.join(',')).finally(() => {
          loadingEcho.value = false;
        });
      }
    },
    { immediate: true }
  );

  /**
   * 트리 인스턴스 가져오기
   */
  function getTree() {
    const tree = unref(treeRef);
    if (!tree) {
      throw new Error('tree is null!');
    }
    return tree;
  }

  /**
   * 트리 확장 수준 설정
   */
  function expandTree() {
    nextTick(() => {
      if (props.defaultExpandLevel && props.defaultExpandLevel > 0) {
        getTree().filterByLevel(props.defaultExpandLevel);
      }
      //기본적으로 선택되는 설정 목록
      checkedKeys.value = selectValues['value'];
    }).then();
  }

  /**
   * 트리 노드 선택
   */
  function onSelect(keys, info) {
    if (props.checkable == false) {
      checkedKeys.value = props.checkStrictly ? keys.checked : keys;
      const { selectedNodes } = info;
      let rows = <any[]>[];
      selectedNodes.forEach((item) => {
        rows.push(item);
      });
      selectRows.value = rows;
    }
  }

  /**
   * 트리 노드 선택
   */
  function onCheck(keys, info) {
    if (props.checkable == true) {
      // 다중 선택이 허용되지 않으면 마지막으로 선택한 항목만 유지됩니다.
      if (!realProps.multiple) {
        if (info.checked) {
          checkedKeys.value = [info.node.eventKey];
          let rowKey = props.rowKey;
          let temp = info.checkedNodes.find((n) => n[rowKey] === info.node.eventKey);
          selectRows.value = [temp];
        } else {
          checkedKeys.value = [];
          selectRows.value = [];
        }
        return;
      }
      checkedKeys.value = props.checkStrictly ? keys.checked : keys;
      const { checkedNodes } = info;
      let rows = <any[]>[];
      checkedNodes.forEach((item) => {
        rows.push(item);
      });
      selectRows.value = rows;
    }
  }

  /**
   * 모두 확인
   */
  async function checkALL(checkAll) {
    getTree().checkAll(checkAll);
    await nextTick();
    checkedKeys.value = getTree().getCheckedKeys();
    if(checkAll){
      getTreeRow();
    }else{
      selectRows.value = [];
    }
  }

  /**
   * 번호 목록 가져오기
   * @param res
   */
  function getTreeRow() {
    let ids = "";
    if(unref(checkedKeys).length>0){
      ids = checkedKeys.value.join(",");
    }
    getList({ids:ids}).then((res) =>{
      selectRows.value = res;
    })
  }

  /**
   * 모두 펼치기
   */
  function expandAll(expandAll) {
    getTree().expandAll(expandAll);
  }

  /**
   * 트리 데이터 로드
   */
  async function onLoadData(treeNode, ids) {
    let params = {};
    let startPid = '';
    if (treeNode) {
      startPid = treeNode.eventKey;
      params['pid'] = treeNode.value;
    }
    if (ids) {
      startPid = '';
      params['ids'] = ids;
    }
    let record = await getList(params);
    let optionData = record;
    if (!props.serverTreeData) {
      //프런트 엔드 처리 데이터는 트리 구조입니다.
      record = listToTree(record, props, startPid);
      if (record.length == 0 && treeNode) {
        checkHasChild(startPid, treeData.value);
      }
    }

    if (openModal.value == true) {
      //팝업 모드에서 모든 데이터 로드
      if (!treeNode) {
        treeData.value = record;
      } else {
        return new Promise((resolve: (value?: unknown) => void) => {
          if (!treeNode.children) {
            resolve();
            return;
          }
          const asyncTreeAction: TreeActionType | null = unref(treeRef);
          if (asyncTreeAction) {
            asyncTreeAction.updateNodeByKey(treeNode.eventKey, { children: record });
            asyncTreeAction.setExpandedKeys([treeNode.eventKey, ...asyncTreeAction.getExpandedKeys()]);
          }
          resolve();
          return;
        });
      }
      expandTree();
    } else {
      const options = <any[]>[];
      optionData.forEach((item) => {
        options.push({ label: item[props.labelKey], value: item[props.rowKey] });
      });
      selectOptions.value = options;
    }
  }

  /**
   * 비동기 로딩 중 하위 노드가 있는지 감지
   * @param pid 상위 노드
   * @param treeArray 트리데이터
   */
  function checkHasChild(pid, treeArray) {
    if (treeArray && treeArray.length > 0) {
      for (let item of treeArray) {
        if (item.key == pid) {
          if (!item.child) {
            item.isLeaf = true;
          }
          break;
        } else {
          checkHasChild(pid, item.children);
        }
      }
    }
  }

  /**
   * 선택한 데이터 가져오기
   */
  function getSelectTreeData(success) {
    const options = <any[]>[];
    const values = <any[]>[];
    selectRows.value.forEach((item) => {
      options.push({ label: item[props.labelKey], value: item[props.rowKey] });
    });
    checkedKeys.value.forEach((item) => {
      values.push(item);
    });
    selectOptions.value = options;
    success && success(options, values);
  }

  /**
   * 팝업 상자에 숨겨진 트리거 이벤트가 표시됩니다.
   */
  async function visibleChange(visible) {
    if (visible) {
      //팝업 상자가 열리면 모든 데이터를 로드합니다.
      openModal.value = true;
      await onLoadData(null, null);
    } else {
      openModal.value = false;
      emit?.('close');
    }
  }

  return [
    {
      visibleChange,
      selectOptions,
      selectValues,
      onLoadData,
      onCheck,
      onSelect,
      checkALL,
      expandAll,
      checkedKeys,
      selectRows,
      treeData,
      getCheckStrictly,
      getSelectTreeData,
    },
  ];
}
