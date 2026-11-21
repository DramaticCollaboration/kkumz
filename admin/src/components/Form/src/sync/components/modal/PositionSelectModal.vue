<!--직업 선택 상자-->
<template>
  <div>
    <BasicModal
      v-bind="$attrs"
      @register="register"
      :title="modalTitle"
      width="1100px"
      wrapClassName="j-user-select-modal"
      @ok="handleOk"
      destroyOnClose
      @visible-change="visibleChange"
    >
      <a-row>
        <a-col :span="showSelected ? 18 : 24">
          <BasicTable
            :columns="columns"
            :bordered="true"
            :useSearchForm="true"
            :formConfig="formConfig"
            :api="getPositionList"
            :searchInfo="searchInfo"
            :rowSelection="rowSelection"
            :indexColumnProps="indexColumnProps"
            v-bind="getBindValue"
          ></BasicTable>
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
  import { useI18n } from '/@/hooks/web/useI18n';
  import { $t } from '/@/locales';
  import { defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getPositionList } from '/@/api/common/api';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useSelectBiz } from '/@/components/Form/src/sync/hooks/useSelectBiz';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { selectProps } from '/@/components/Form/src/sync/props/props';

  export default defineComponent({
    name: 'PositionSelectModal',
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
        default: $t('appUi.k0828c751'),
      },
    },
    emits: ['register', 'getSelectResult'],
    setup(props, { emit, refs }) {
      const { t } = useI18n();
      //등록 팝업 상자
      const [register, { closeModal }] = useModalInner();
      const attrs = useAttrs();
      //테이블 구성
      const config = {
        canResize: false,
        bordered: true,
        size: 'small',
        //rowKey를 읽고 전달 매개변수를 맞춤설정하도록 변경
        rowKey: props.rowKey,
      };
      const getBindValue = Object.assign({}, unref(props), unref(attrs), config);
      const [{ rowSelection, visibleChange, indexColumnProps, getSelectResult, handleDeleteSelected, selectRows }] = useSelectBiz(
        getPositionList,
        getBindValue
      );
      const searchInfo = ref(props.params);
      //쿼리 Form
      const formConfig = {
        labelCol: {
          span: 4,
        },
        baseColProps: {
          xs: 24,
          sm: 10,
          md: 10,
          lg: 10,
          xl: 10,
          xxl: 10,
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
            label: t('appUi.k0923a240'),
            field: 'name',
            component: 'JInput',
            colProps: { span: 10 },
          },
        ],
      };
      //테이블 열 정의
      const columns = [
        {
          title: t('appUi.kc0a1d4a6'),
          dataIndex: 'code',
          width: 180,
          align: 'left',
        },
        {
          title: t('appUi.k0923a240'),
          dataIndex: 'name',
          // width: 180,
        },
        {
          title: t('appUi.k4ef6d9e4'),
          dataIndex: 'postRank_dictText',
          width: 180,
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
            title: t('appUi.k0923a240'),
            dataIndex: 'name',
            width: 40,
          },
          {
            title: t('appUi.k9d9bf438'),
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
      return {
        handleOk,
        getPositionList,
        register,
        visibleChange,
        getBindValue,
        formConfig,
        indexColumnProps,
        columns,
        rowSelection,

        selectedTable,
        selectRows,
        handleDeleteSelected,
        searchInfo,
      };
    },
  });
</script>
