<!--부서별 사용자 선택-->
<template>
  <BasicModal v-bind="$attrs" @register="register" :title="modalTitle" width="1200px" @ok="handleOk" destroyOnClose @visible-change="visibleChange">
    <a-row :gutter="10">
      <a-col :md="7" :sm="24">
        <a-card :style="{ minHeight: '613px', overflow: 'auto' }">
          <!--조직 구조-->
          <BasicTree
            ref="treeRef"
            :style="{ minWidth: '250px' }"
            selectable
            @select="onDepSelect"
            :load-data="loadChildrenTreeData"
            :treeData="departTree"
            :selectedKeys="selectedDepIds"
            :expandedKeys="expandedKeys"
            :clickRowToExpand="false"
          ></BasicTree>
        </a-card>
      </a-col>
      <a-col :md="17" :sm="24">
        <a-card :style="{ minHeight: '613px', overflow: 'auto' }">
          <!--사용자 목록-->
          <BasicTable ref="tableRef" v-bind="getBindValue" :searchInfo="searchInfo" :api="getTableList" :rowSelection="rowSelection"></BasicTable>
        </a-card>
      </a-col>
    </a-row>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, unref, ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTree } from '/@/components/Tree/index';
  import { queryTreeList, getTableList } from '/@/api/common/api';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useSelectBiz } from '/@/components/Form/src/sync/hooks/useSelectBiz';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { queryDepartTreeSync } from '/@/views/system/depart/depart.api';
  import { selectProps } from '/@/components/Form/src/sync/props/props';
  export default defineComponent({
    name: 'UserSelectByDepModal',
    components: {
      //BasicTable은 여기에서 비동기식으로 로드되어야 합니다.
      BasicModal,
      BasicTree,
      BasicTable: createAsyncComponent(() => import('/@/components/Table/src/BasicTable.vue'), {
        loading: true,
      }),
    },
    props: {
      ...selectProps,
      //상자 제목 선택
      modalTitle: {
        type: String,
        default: '부서 사용자 선택',
      },
    },
    emits: ['register', 'getSelectResult'],
    setup(props, { emit, refs }) {
      const tableRef = ref();
      const treeRef = ref();
      //등록 팝업 상자
      const [register, { closeModal }] = useModalInner(async (data) => {
        await queryDepartTree();
      });
      const attrs = useAttrs();
      const departTree = ref([]);
      const selectedDepIds = ref([]);
      const expandedKeys = ref([]);
      const searchInfo = {};
      /**
       *테이블 구성
       */
      const tableProps = {
        columns: [
          {
            title: '아이디',
            dataIndex: 'username',
            width: 180,
          },
          {
            title: '사용자 이름',
            dataIndex: 'realname',
            width: 180,
          },
          {
            title: '성별',
            dataIndex: 'sex_dictText',
            width: 80,
          },
          {
            title: '전화 번호',
            dataIndex: 'phone',
            // width: 50,
          },
        ],
        useSearchForm: true,
        canResize: false,
        showIndexColumn: false,
        striped: true,
        bordered: true,
        size: 'small',
        formConfig: {
          //labelWidth: 200,
          baseColProps: {
            xs: 24,
            sm: 8,
            md: 6,
            lg: 8,
            xl: 6,
            xxl: 10,
          },
          actionColOptions: {
              xs: 24,
              sm: 12,
              md: 12,
              lg: 12,
              xl: 8,
              xxl: 8,
          },
          schemas: [
            {
              label: '계정',
              field: 'username',
              component: 'Input',
            },
          ],
          resetFunc: customResetFunc,
        },
      };
      const getBindValue = Object.assign({}, unref(props), unref(attrs), tableProps);
      const [{ rowSelection, visibleChange, indexColumnProps, getSelectResult, reset }] = useSelectBiz(getTableList, getBindValue);
      /**
       * 트리 데이터 로드
       */
      function queryDepartTree() {
        queryDepartTreeSync().then((res) => {
          if (res) {
            departTree.value = res;
            // 기본적으로 상위 노드 확장
            //expandedKeys.value = unref(departTree).map(item => item.id)
          }
        });
      }
      /**
       * 하위 부서 로드
       */
      async function loadChildrenTreeData(treeNode) {
        try {
          const result = await queryDepartTreeSync({
            pid: treeNode.eventKey,
          });
          const asyncTreeAction = unref(treeRef);
          if (asyncTreeAction) {
            asyncTreeAction.updateNodeByKey(treeNode.eventKey, { children: result });
            asyncTreeAction.setExpandedKeys([treeNode.eventKey, ...asyncTreeAction.getExpandedKeys()]);
          }
        } catch (e) {
          console.error(e);
        }
        return Promise.resolve();
      }
      /**
       * 트리 노드를 클릭하면 해당 사용자를 필터링할 수 있습니다.
       */
      function onDepSelect(keys) {
        if (keys[0] != null) {
          if (unref(selectedDepIds)[0] !== keys[0]) {
            selectedDepIds.value = [keys[0]];
          }
          searchInfo['departId'] = unref(selectedDepIds).join(',');
          tableRef.value.reload();
        }
      }
      /**
       * 사용자 정의 재설정 방법
       * */
      async function customResetFunc() {
        console.log('맞춤 쿼리');
        //트리 노드 지우기
        selectedDepIds.value = [];
        //쿼리 조건 지우기
        searchInfo['departId'] = '';
        //선택 취소
        reset();
      }
      /**
       * 선택 확인
       */
      function handleOk() {
        getSelectResult((options, values) => {
          //반환 옵션 및 선택한 값
          emit('getSelectResult', options, values);
          //팝업 창 닫기
          closeModal();
        });
      }

      return {
        //config,
        handleOk,
        searchInfo,
        register,
        indexColumnProps,
        visibleChange,
        getBindValue,
        rowSelection,

        departTree,
        selectedDepIds,
        expandedKeys,
        treeRef,
        tableRef,
        getTableList,
        onDepSelect,
        loadChildrenTreeData,
      };
    },
  });
</script>

<style scoped lang="less"></style>
