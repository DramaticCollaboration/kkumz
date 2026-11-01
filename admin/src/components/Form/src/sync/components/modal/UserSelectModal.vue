<!--사용자 선택 상자-->
<template>
  <div>
    <BasicModal
      v-bind="$attrs"
      @register="register"
      :title="modalTitle"
      :width="showSelected ? '1200px' : '900px'"
      wrapClassName="j-user-select-modal"
      @ok="handleOk"
      @cancel="handleCancel"
      :maxHeight="maxHeight"
      :centered="true"
      destroyOnClose
      @visible-change="visibleChange"

    >
      <a-row>
        <a-col :span="showSelected ? 18 : 24">
          <BasicTable
            ref="tableRef"
            :columns="columns"
            :scroll="tableScroll"
            v-bind="getBindValue"
            :useSearchForm="true"
            :formConfig="formConfig"
            :api="getUserList"
            :searchInfo="searchInfo"
            :rowSelection="rowSelection"
            :indexColumnProps="indexColumnProps"
            :afterFetch="afterFetch"
          >
            <template #tableTitle></template>
          </BasicTable>
        </a-col>
        <a-col :span="showSelected ? 6 : 0">
          <BasicTable
            v-bind="selectedTable"
            :dataSource="selectRows"
            :useSearchForm="true"
            :formConfig="{ showActionButtonGroup: false, baseRowStyle: { minHeight: '40px' } }"
          >
            <!--액션바-->
            <template #action="{ record }">
              <a href="javascript:void(0)" @click="handleDeleteSelected(record)"><Icon icon="ant-design:delete-outlined"></Icon></a>
            </template>
          </BasicTable>
        </a-col>
      </a-row>
    </BasicModal>
  </div>
</template>
<script lang="ts">
  import { defineComponent, unref, ref, watch } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getUserList } from '/@/api/common/api';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useSelectBiz } from '/@/components/Form/src/sync/hooks/useSelectBiz';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { selectProps } from '/@/components/Form/src/sync/props/props';

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
        default: '사용자 선택',
      },
      //사용자 ID 모음 제외
      excludeUserIdList: {
        type: Array,
        default: [],
      },
    },
    emits: ['register', 'getSelectResult', 'close'],
    setup(props, { emit, refs }) {
      const tableScroll = ref<any>({ x: false });
      const tableRef = ref();
      const maxHeight = ref(600);

      //등록 팝업 상자
      const [register, { closeModal }] = useModalInner(() => {
        if (window.innerWidth < 900) {
          tableScroll.value = { x: 900 };
        } else {
          tableScroll.value = { x: false };
        }
        setTimeout(() => {
          if (tableRef.value) {
            tableRef.value.setSelectedRowKeys(selectValues['value'] || []);
          }
        }, 800);
      });
      const attrs = useAttrs();
      //테이블 구성
      const config = {
        canResize: false,
        bordered: true,
        size: 'small',
      };
      const getBindValue = Object.assign({}, unref(props), unref(attrs), config);
      const [{ rowSelection, visibleChange, selectValues, indexColumnProps, getSelectResult, handleDeleteSelected, selectRows }] = useSelectBiz(
        getUserList,
        getBindValue,
        emit
      );
      const searchInfo = ref(props.params);
      watch(rowSelection.selectedRowKeys, (newVal) => {
        if(tableRef.value){
          tableRef.value.setSelectedRowKeys(newVal);
        }
      });
      //쿼리 Form
      const formConfig = {
        baseColProps: {
          xs: 24,
          sm: 8,
          md: 6,
          lg: 8,
          xl: 6,
          xxl: 6,
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
            label: '계정',
            field: 'username',
            component: 'JInput',
          },
          {
            label: '이름',
            field: 'realname',
            component: 'JInput',
          },
        ],
      };
      //테이블 열 정의
      const columns = [
        {
          title: '아이디',
          dataIndex: 'username',
          width: 120,
          align: 'left',
        },
        {
          title: '사용자 이름',
          dataIndex: 'realname',
          width: 120,
        },
        {
          title: '성별',
          dataIndex: 'sex_dictText',
          width: 50,
        },
        {
          title: '전화 번호',
          dataIndex: 'phone',
          width: 120,
        },
        {
          title: '이메일',
          dataIndex: 'email',
          // width: 40,
        },
        {
          title: '상태',
          dataIndex: 'status_dictText',
          width: 80,
        },
      ];
      //선택한 테이블 정보
      const selectedTable = {
        pagination: false,
        showIndexColumn: false,
        scroll: { y: 390 },
        size: 'small',
        canResize: false,
        bordered: true,
        rowKey: 'id',
        columns: [
          {
            title: '사용자 이름',
            dataIndex: 'realname',
            width: 40,
          },
          {
            title: '작업',
            dataIndex: 'action',
            align: 'center',
            width: 40,
            slots: { customRender: 'action' },
          },
        ],
      };
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

      /**
       * 사용자 반환 결과 논리 쿼리
       */
      function afterFetch(record) {
        let excludeList = props.excludeUserIdList;
        if(!excludeList){
          return record;
        }
        let arr:any[] = [];
        //필터링된 사용자 ID 수집이 있고 백그라운드에서 반환된 데이터가 비어 있지 않은 경우
        if(excludeList.length>0 && record && record.length>0){
          for(let item of record){
            if(excludeList.indexOf(item.id)<0){
              arr.push({...item})
            }
          }
          return arr;
        }
        return record;
      }
      const handleCancel = () => {
        emit('close');
      };

      const clientHeight = document.documentElement.clientHeight * 200;
      maxHeight.value = clientHeight > 600 ? 600 : clientHeight;

      return {
        //config,
        handleOk,
        searchInfo,
        register,
        indexColumnProps,
        visibleChange,
        getBindValue,
        getUserList,
        formConfig,
        columns,
        rowSelection,
        selectRows,
        selectedTable,
        handleDeleteSelected,
        tableScroll,
        tableRef,
        afterFetch,
        handleCancel,
        maxHeight,
      };
    },
  });
</script>
