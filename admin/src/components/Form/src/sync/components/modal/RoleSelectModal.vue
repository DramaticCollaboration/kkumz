<!--역할 선택 상자-->
<template>
  <div>
    <BasicModal v-bind="$attrs" @register="register" :title="modalTitle" width="800px" @ok="handleOk" destroyOnClose @visible-change="visibleChange">
      <BasicTable
        :columns="columns"
        v-bind="config"
        :useSearchForm="true"
        :formConfig="formConfig"
        :api="getRoleList"
        :searchInfo="searchInfo"
        :rowSelection="rowSelection"
        :indexColumnProps="indexColumnProps"
      ></BasicTable>
    </BasicModal>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getRoleList } from '/@/api/common/api';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useSelectBiz } from '/@/components/Form/src/sync/hooks/useSelectBiz';
  import { selectProps } from '/@/components/Form/src/sync/props/props';
  import { useAttrs } from '/@/hooks/core/useAttrs';

  export default defineComponent({
    name: 'UserSelectModal',
    components: {
      //BasicTable은 여기에서 비동기식으로 로드되어야 합니다.
      BasicModal,
      BasicTable: createAsyncComponent(() => import('/@/components/Table/src/BasicTable.vue'), {
        loading: true,
      }),
    },
    props: {
      ...selectProps,
      //상자 제목 선택
      modalTitle: {
        type: String,
        default: '역할 선택',
      },
    },
    emits: ['register', 'getSelectResult'],
    setup(props, { emit, refs }) {
      //등록 팝업 상자
      const [register, { closeModal }] = useModalInner();
      const attrs = useAttrs();
      //테이블 구성
      const config = {
        canResize: false,
        bordered: true,
        size: 'small',
        rowKey: unref(props).rowKey,
      };
      const getBindValue = Object.assign({}, unref(props), unref(attrs), config);
      const [{ rowSelection, indexColumnProps, visibleChange, getSelectResult }] = useSelectBiz(getRoleList, getBindValue);
      const searchInfo = ref(props.params);
      //쿼리 Form
      const formConfig = {
        //labelWidth: 220,
        baseColProps: {
          xs: 24,
          sm: 24,
          md: 24,
          lg: 14,
          xl: 14,
          xxl: 14,
        },
        actionColOptions: {
            xs: 24,
            sm: 8,
            md: 8,
            lg: 8,
            xl: 8,
            xxl: 8,
        },
        schemas: [
          {
            label: '역할 이름',
            field: 'roleName',
            component: 'Input',
          },
        ],
      };
      //테이블 열 정의
      const columns = [
        {
          title: '역할 이름',
          dataIndex: 'roleName',
          width: 240,
          align: 'left',
        },
        {
          title: '역할 코드',
          dataIndex: 'roleCode',
          // width: 40,
        },
      ];

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
        config,
        handleOk,
        searchInfo,
        register,
        indexColumnProps,
        visibleChange,
        getRoleList,
        formConfig,
        getBindValue,
        columns,
        rowSelection,
      };
    },
  });
</script>
