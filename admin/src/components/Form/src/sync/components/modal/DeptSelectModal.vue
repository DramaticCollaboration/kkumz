<!--학과 선택 상자-->
<template>
  <div>
    <BasicModal v-bind="$attrs" @register="register" :title="modalTitle" width="500px" :maxHeight="maxHeight" @ok="handleOk" destroyOnClose @visible-change="visibleChange">
      <BasicTree
        ref="treeRef"
        :treeData="treeData"
        :load-data="sync == false ? null : onLoadData"
        v-bind="getBindValue"
        @select="onSelect"
        @check="onCheck"
        :fieldNames="fieldNames"
        :checkedKeys="checkedKeys"
        :multiple="multiple"
        :checkStrictly="getCheckStrictly"
      />
      <!--트리 연산 섹션-->
      <template #insertFooter>
        <a-dropdown placement="top">
          <template #overlay>
            <a-menu>
              <a-menu-item v-if="multiple" key="1" @click="checkALL(true)">모두 확인</a-menu-item>
              <a-menu-item v-if="multiple" key="2" @click="checkALL(false)">모두 선택 취소</a-menu-item>
              <a-menu-item key="3" @click="expandAll(true)">모두 펼치기</a-menu-item>
              <a-menu-item key="4" @click="expandAll(false)">모두 접기</a-menu-item>
            </a-menu>
          </template>
          <a-button style="float: left"> 트리 작업 <Icon icon="ant-design:up-outlined" /> </a-button>
        </a-dropdown>
      </template>
    </BasicModal>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { queryDepartTreeSync, queryTreeList } from '/@/api/common/api';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { treeProps } from '/@/components/Form/src/sync/props/props';
  import { BasicTree, TreeActionType } from '/@/components/Tree';
  import { useTreeBiz } from '/@/components/Form/src/sync/hooks/useTreeBiz';
  import {propTypes} from "/@/utils/propTypes";
  import { omit } from 'lodash-es';

  export default defineComponent({
    name: 'DeptSelectModal',
    components: {
      BasicModal,
      BasicTree,
    },
    props: {
      ...treeProps,
      //상자 제목 선택
      modalTitle: {
        type: String,
        default: '학과선택',
      },
      maxHeight: {
        type: Number,
        default: 500,
      },
      value: propTypes.oneOfType([propTypes.string, propTypes.array])
    },
    emits: ['register', 'getSelectResult', 'close'],
    setup(props, { emit, refs }) {
      //등록 팝업 상자
      const [register, { closeModal }] = useModalInner();
      const attrs = useAttrs();
      const treeRef = ref<Nullable<TreeActionType>>(null);
      
      let propValue = props.value === ''?[]:props.value;
      let temp = Object.assign({}, unref(props), unref(attrs), {value: propValue},{disabled: false});
      const getBindValue = omit(temp, 'multiple');
      
      const queryUrl = getQueryUrl();
      const [{ visibleChange, checkedKeys, getCheckStrictly, getSelectTreeData, onCheck, onLoadData, treeData, checkALL, expandAll, onSelect }] =
        useTreeBiz(treeRef, queryUrl, getBindValue, props);
      const searchInfo = ref(props.params);
      const tree = ref([]);
      //treeNode의 키 필드를 treeData의 해당 필드로 바꿉니다.
      const fieldNames = {
        key: props.rowKey,
      };
      // {children:'children', title:'title', key:'key' }
      /**
       * 선택 확인
       */
      function handleOk() {
        getSelectTreeData((options, values) => {
          //반환 옵션 및 선택한 값
          emit('getSelectResult', options, values);
          //팝업 창 닫기
          closeModal();
        });
      }

      /** 쿼리 데이터 가져오기 방법 */
      function getQueryUrl() {
        let queryFn = props.sync ? queryDepartTreeSync : queryTreeList;
        return (params) => queryFn(Object.assign({}, params, { primaryKey: props.rowKey }));
      }

      return {
        tree,
        handleOk,
        searchInfo,
        treeRef,
        treeData,
        onCheck,
        onSelect,
        checkALL,
        expandAll,
        fieldNames,
        checkedKeys,
        register,
        getBindValue,
        getCheckStrictly,
        visibleChange,
        onLoadData,
      };
    },
  });
</script>
