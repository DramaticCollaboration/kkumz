<template>
  
  
  <!--        @click="openSelect"-->
  <!--        placeholder="부서를 선택하려면 클릭하세요."-->
  <!--        v-model="departNames"-->
  <!--        readOnly-->
  <!--        :disabled="componentDisabled"-->
  <!--        class="jvxe-select-input">-->
  
  <!--    </a-input>-->
  
  <!--        :modal-width="modalWidth"-->
  <!--        :multi="multi"-->
  <!--        :rootOpened="rootOpened"-->
  <!--        :depart-id="departIds"-->
  <!--        @ok="handleOK"-->
  <!--        @initComp="initComp"/>-->
  
  
  
  <!--    </span>-->
  <!--  </div>-->
  <div :class="[prefixCls]">
    <JSelectDept v-bind="getProps" @change="handleChange" />
  </div>
</template>

<script lang="ts">
  import { computed, defineComponent } from 'vue';
  //import { JSelectDept } from '/@/components/Form';
  import JSelectDept from '/@/components/Form/src/sync/components/JSelectDept.vue';
  import { JVxeComponent } from '/@/components/sync/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/sync/JVxeTable/hooks';

  // import JSelectDepartModal from '@/components/empasybiz/modal/JSelectDepartModal'
  import { dispatchEvent } from '/@/components/sync/JVxeTable/utils';
  import { isArray, isEmpty, isString } from '/@/utils/is';

  export default defineComponent({
    name: 'JVxeDepartSelectCell',
    components: { JSelectDept },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, handleChangeCommon, useCellDesign } = useJVxeComponent(props);
      const { prefixCls } = useCellDesign('depart-select');

      const selectedValue = computed(() => {
        let val: any = innerValue.value;
        if (val == null) {
          return val;
        }
        if (isEmpty(val)) {
          return [];
        }
        if (isArray(val)) {
          return val;
        }
        if (isString(val)) {
          return (<string>val).split(',');
        }
        return [val];
      });

      const multiple = computed(() => cellProps.value['multi'] != false);

      const getProps = computed(() => {
        return {
          ...cellProps.value,
          value: selectedValue.value,
          showButton: false,
          // 검색이 허용되지 않습니다
          showSearch: false,
          // 최대 디스플레이 수 설정
          maxTagCount: 1,
          // 프롬프트 재작성 표시, 줄임표 제거
          maxTagPlaceholder: ({ length }) => '+' + length,
        };
      });

      function handleChange(values) {
        handleChangeCommon(values.join(','));
      }

      return {
        prefixCls,
        selectedValue,
        multiple,
        cellProps,
        getProps,
        handleChange,
      };
    },

    data() {
      return {
        // departNames: '',
        // departIds: '',
        // selectedOptions: [],
        // customReturnField: 'id',
      };
    },
    computed: {
      // custProps() {
      //   const { departIds, originColumn: col, caseId, cellProps } = this
      //   return {
      //     ...cellProps,
      //     value: departIds,
      //     field: col.field || col.key,
      //     groupId: caseId,
      //     class: 'jvxe-select',
      //   }
      // },
      // componentDisabled() {
      //   if (this.cellProps.disabled == true) {
      //     return true
      //   }
      //   return false
      // },
      // modalWidth() {
      //   if (this.cellProps.modalWidth) {
      //     return this.cellProps.modalWidth
      //   } else {
      //     return 500
      //   }
      // },
      // multi() {
      //   if (this.cellProps.multi == false) {
      //     return false
      //   } else {
      //     return true
      //   }
      // },
      // rootOpened() {
      //   if (this.cellProps.open == false) {
      //     return false
      //   } else {
      //     return true
      //   }
      // },
    },
    watch: {
      // innerValue: {
      //   immediate: true,
      //   handler(val) {
      //     if (val == null || val === '') {
      //       this.departIds = ''
      //     } else {
      //       this.departIds = val
      //     }
      //   },
      // },
    },
    methods: {
      // openSelect() {
      //   this.$refs.innerDepartSelectModal.show()
      // },
      // handleEmpty() {
      //   this.handleOK('')
      // },
      // handleOK(rows, idstr) {
      //   let value = ''
      //   if (!rows && rows.length <= 0) {
      //     this.departNames = ''
      //     this.departIds = ''
      //   } else {
      //     value = rows.map(row => row[this.customReturnField]).join(',')
      //     this.departNames = rows.map(row => row['departName']).join(',')
      //     this.departIds = idstr
      //   }
      //   this.handleChangeCommon(this.departIds)
      // },
      // initComp(departNames) {
      //   this.departNames = departNames
      // },
      // handleChange(value) {
      //   this.handleChangeCommon(value)
      // },
    },
    enhanced: {
      switches: {
        visible: true,
      },
      translate: {
        enabled: false,
      },
      aopEvents: {
        editActived({ $event }) {
          dispatchEvent({
            $event,
            props: this.props,
            className: '.ant-select .ant-select-selection-search-input',
            isClick: true,
          });
        },
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>

<style lang="less">
  // noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-vxe-cell-depart-select';

  .@{prefix-cls} {
    // 텍스트가 너무 많은 옵션을 선택할 때 줄 바꿈을 방지하려면 최대 태그 길이를 100px로 제한하세요.
    .ant-select .ant-select-selection-overflow-item {
      max-width: 80px;
    }
  }
</style>
