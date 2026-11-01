<template>
  <a-tree-select
    v-if="show"
    allowClear
    labelInValue
    style="width: 100%"
    :getPopupContainer="(node) => node?.parentNode"
    :dropdownStyle="{ maxHeight: '400px', overflow: 'auto' }"
    :placeholder="placeholder"
    :loadData="asyncLoadTreeData"
    :value="treeValue"
    :treeData="treeData"
    :multiple="multiple"
    v-bind="attrs"
    @change="onChange"
    @search="onSearch"
    :tree-checkable="treeCheckAble"
  >
  </a-tree-select>
</template>
<script lang="ts" setup>
  /*
   * 비동기 트리 로딩 구성요소는 필드를 표시하고, 필드를 저장하고, 테이블 이름을 전달하여 트리 컨트롤을 로드합니다.
   * <j-tree-select dict="aa_tree_test,aad,id" pid-field="pid" ></j-tree-select>
   * */
  import { ref, watch, unref, nextTick } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { TreeSelect } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';

  enum Api {
    url = '/api/v1/kkumz/sys/dict/loadTreeData',
    view = '/api/v1/kkumz/sys/dict/loadDictItem/',
  }

  const props = defineProps({
    value: propTypes.string.def(''),
    placeholder: propTypes.string.def('선택하세요'),
    dict: propTypes.string.def('id'),
    parentCode: propTypes.string.def(''),
    pidField: propTypes.string.def('pid'),
    pidValue: propTypes.string.def(''),
    hasChildField: propTypes.string.def(''),
    converIsLeafVal: propTypes.integer.def(1),
    condition: propTypes.string.def(''),
    multiple: propTypes.bool.def(false),
    loadTriggleChange: propTypes.bool.def(false),
    reload: propTypes.number.def(1),
    url: propTypes.string.def(''),
    params: propTypes.object.def({}),
    //기본적으로 선택 상자가 없습니다.
    treeCheckAble: propTypes.bool.def(false),
    hiddenNodeKey: propTypes.string.def(''),
  });
  const attrs = useAttrs();
  const emit = defineEmits(['change', 'update:value']);
  const { createMessage } = useMessage();
  //트리 드롭다운 데이터
  const treeData = ref<any[]>([]);
  //데이터 선택
  const treeValue = ref<any>(null);
  const tableName = ref<any>('');
  const text = ref<any>('');
  const code = ref<any>('');
  const show = ref<boolean>(true);
  /**
   * 값 데이터 모니터링 및 초기화
   */
  watch(
    () => props.value,
    () => loadItemByCode(),
    { deep: true, immediate: true }
  );
  /**
   * 사전 변경 사항 모니터링
   */
  watch(
    () => props.dict,
    () => {
      initDictInfo();
      loadRoot();
    },
    { deep: true, immediate: true }
  );
  watch(
    () => props.hiddenNodeKey,
    () => {
      if (treeData.value?.length && props.hiddenNodeKey) {
        handleHiddenNode(treeData.value);
        treeData.value = [...treeData.value];
      }
    }
  );

  watch(
    () => props.reload,
    async () => {
      treeData.value = [];
      show.value = false;
      nextTick(() => {
        show.value = true;
      });
      await loadRoot();
    },
    {
      immediate: false,
    }
  );

  /**
   * 코드에 따라 드롭다운 데이터를 가져와서 에코합니다.
   */
  async function loadItemByCode() {
    if (!props.value || props.value == '0') {
      if(props.multiple){
        treeValue.value = [];
      }else{
        treeValue.value = { label: null, value: null };
      }
    } else {
      if(props.url){
        getItemFromTreeData();
      }else{
        let params = { key: props.value };
        let result = await defHttp.get({ url: `${Api.view}${props.dict}`, params }, { isTransformResponse: false });
        if (result.success) {
          if(props.multiple){
            let values = props.value.split(',');
            treeValue.value = result.result.map((item, index) => ({
              key: values[index],
              value: values[index],
              label: item,
            }));
          }else{
            treeValue.value = { key: props.value, value: props.value, label: result.result[0] };
          }
          onLoadTriggleChange(result.result[0]);
        }
      }
    }
  }

  function onLoadTriggleChange(text) {
    //단일 선택만 트리거됩니다.
    if (!props.multiple && props.loadTriggleChange) {
      emit('change', props.value, text);
    }
  }

  /**
   * 초기화 데이터
   */
  function initDictInfo() {
    let arr = props.dict?.split(',');
    tableName.value = arr[0];
    text.value = arr[1];
    code.value = arr[2];
  }

  /**
   * 드롭다운 트리 데이터 로드
   */
  async function loadRoot() {
    let params = {
      pid: props.pidValue,
      pidField: props.pidField,
      hasChildField: props.hasChildField,
      converIsLeafVal: props.converIsLeafVal,
      condition: props.condition,
      tableName: unref(tableName),
      text: unref(text),
      code: unref(code),
    };
    let res = await defHttp.get({ url: Api.url, params }, { isTransformResponse: false });
    if (res.success && res.result) {
      for (let i of res.result) {
        i.value = i.key;
        i.isLeaf = !!i.leaf;
      }
      handleHiddenNode(res.result);
      treeData.value = [...res.result];
    } else {
      console.log('여러 루트 노드About 비정상적인 쿼리 결과', res);
    }
  }

  /**
   * 비동기적으로 데이터 로드
   */
  async function asyncLoadTreeData(treeNode) {
    if (treeNode.dataRef.children) {
      return Promise.resolve();
    }
    if(props.url){
      return Promise.resolve();
    }
    let pid = treeNode.dataRef.key;
    let params = {
      pid: pid,
      pidField: props.pidField,
      hasChildField: props.hasChildField,
      converIsLeafVal: props.converIsLeafVal,
      condition: props.condition,
      tableName: unref(tableName),
      text: unref(text),
      code: unref(code),
    };
    let res = await defHttp.get({ url: Api.url, params }, { isTransformResponse: false });
    if (res.success) {
      for (let i of res.result) {
        i.value = i.key;
        i.isLeaf = !!i.leaf;
      }
      handleHiddenNode(res.result);
      //하위 노드 추가
      addChildren(pid, res.result, treeData.value);
      treeData.value = [...treeData.value];
    }
    return Promise.resolve();
  }

  /**
   * 자식 노드 로드
   */
  function addChildren(pid, children, treeArray) {
    if (treeArray && treeArray.length > 0) {
      for (let item of treeArray) {
        if (item.key == pid) {
          if (!children || children.length == 0) {
            item.isLeaf = true;
          } else {
            item.children = children;
          }
          break;
        } else {
          addChildren(pid, children, item.children);
        }
      }
    }
  }

  /**
   * 트리 노드 이벤트 선택
   */
  function onChange(value) {
    if (!value) {
      emitValue('');
    } else if (value instanceof Array) {
      emitValue(value.map((item) => item.value).join(','));
    } else {
      emitValue(value.value);
    }
    treeValue.value = value;
  }

  function emitValue(value) {
    emit('change', value);
    emit('update:value', value);
  }

  /**
   * 텍스트 상자 값 변경
   */
  function onSearch(value) {
    console.log(value);
  }

  /**
   * 검증 조건 구성이 올바른지 확인
   */
  function validateProp() {
    let mycondition = props.condition;
    return new Promise((resolve, reject) => {
      if (!mycondition) {
        resolve();
      } else {
        try {
          let test = JSON.parse(mycondition);
          if (typeof test == 'object' && test) {
            resolve();
          } else {
            createMessage.error('JTreeSelect-condition 컴포넌트가 잘못된 값을 전달하고 json 문자열이 필요합니다!');
            reject();
          }
        } catch (e) {
          createMessage.error('JTreeSelect-condition 컴포넌트가 잘못된 값을 전달하고 json 문자열이 필요합니다!');
          reject();
        }
      }
    });
  }

  watch(()=>props.url, async (val)=>{
    if(val){
      await loadRootByUrl();
    }
  });

  /**
   * 맞춤 요청 주소에 따라 데이터 로드
   */
  async function loadRootByUrl(){
    let url = props.url;
    let params = props.params;
    let res = await defHttp.get({ url, params }, { isTransformResponse: false });
    if (res.success && res.result) {
      for (let i of res.result) {
        i.key = i.value;
        i.isLeaf = !!i.leaf;
      }
      handleHiddenNode(res.result);
      treeData.value = [...res.result];
    } else {
      console.log('여러 루트 노드About 비정상적인 쿼리 결과', res);
    }
  }

  /**
   * 기존 트리 데이터를 기반으로 한 번역 옵션
   */
  function getItemFromTreeData(){
    let data = treeData.value;
    let arr = []
    findChildrenNode(data, arr);
    if(arr.length>0){
      treeValue.value = arr
      onLoadTriggleChange(arr[0]);
    }
  }

  /**
   * 재귀적으로 자식 노드 찾기
   * @param data
   * @param arr
   */
  function findChildrenNode(data, arr){
    let val = props.value;
    if(data && data.length){
      for(let item of data){
        if(val===item.value){
          arr.push({
            key: item.key,
            value: item.value,
            label: item.label||item.title
          })
        }else{
          findChildrenNode(item.children, arr)
        }
      }
    }
  }

  /**
   * 2024-05-23
   * liaozhiyang
   *지정된 노드 필터링(하위 노드를 포함합니다.)
   */
  function handleHiddenNode(data) {
    if (props.hiddenNodeKey && data?.length) {
      for (let i = 0, len = data.length; i < len; i++) {
        const item = data[i];
        if (item.key == props.hiddenNodeKey) {
          data.splice(i, 1);
          i--;
          len--;
          return;
        }
      }
    }
  }
  // onCreated
  validateProp().then(() => {
    initDictInfo();
    loadRoot();
    loadItemByCode();
  });
</script>

<style lang="less"></style>
