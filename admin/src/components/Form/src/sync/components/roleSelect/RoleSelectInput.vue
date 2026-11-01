<template>
  <div>
    <div @click="showModal" :class="disabled ? 'select-input disabled-select' : 'select-input'">
      <template v-if="selectedList.length > 0">
        <template v-for="(item, index) in selectedList">
          <SelectedUserItem v-if="index < maxCount" :info="item" @unSelect="unSelect" query />
        </template>
      </template>
      <span v-else style="height: 30px; line-height: 30px; display: inline-block; margin-left: 7px; color: #bfbfbf">선택하세요</span>
      <div v-if="ellipsisInfo.status" class="user-selected-item">
        <div class="user-select-ellipsis">
          <span style="color: red">+{{ ellipsisInfo.count }}...</span>
        </div>
      </div>
    </div>
    <RoleSelectModal :appId="currentAppId" :multi="multi" :getContainer="getContainer" title="조직 역할 선택" @register="registerRoleModal" @selected="onSelected" />
  </div>
</template>

<script lang="ts">
  import { useModal } from '/@/components/Modal';
  import { defHttp } from '/@/utils/http/axios';
  import { computed, ref, watch, watchEffect, defineComponent } from 'vue';
  import RoleSelectModal from './RoleSelectModal.vue';
  import SelectedUserItem from '../userSelect/SelectedUserItem.vue';
  import { Form } from 'ant-design-vue';
  import { useUserStore } from '/@/store/modules/user';

  const maxCount = 2;

  export default defineComponent({
    name: 'RoleSelectInput',
    components: {
      RoleSelectModal,
      SelectedUserItem,
    },
    props: {
      disabled: {
        type: Boolean,
        default: false,
      },
      store: {
        type: String,
        default: 'id',
      },
      value: {
        type: String,
        default: '',
      },
      multi: {
        type: Boolean,
        default: false,
      },
      getContainer: {
        type: Function,
        default: null,
      },
      appId: {
        type: String,
        default: '',
      },
    },
    emits: ['update:value', 'change'],
    setup(props, { emit }) {
      const formItemContext = Form.useInjectFormItemContext();
      const selectedList = ref<any[]>([]);
      const loading = ref(true);

      const [registerRoleModal, { openModal: openRoleModal, closeModal: closeRoleModal }] = useModal();
      function showModal(e) {
        e.preventDefault();
        e.stopPropagation();
        let list = selectedList.value.map((item) => item.id);
        openRoleModal(true, {
          list,
        });
      }

      const ellipsisInfo = computed(() => {
        let max = maxCount;
        let len = selectedList.value.length;
        if (len > max) {
          return { status: true, count: len - max };
        } else {
          return { status: false };
        }
      });

      function unSelect(id) {
        console.log('unSelectUser', id);
        loading.value = false;
        let arr = selectedList.value;
        let index = -1;
        for (let i = 0; i < arr.length; i++) {
          if (arr[i].id == id) {
            index = i;
            break;
          }
        }
        if (index >= 0) {
          arr.splice(index, 1);
          selectedList.value = arr;
          onSelectedChange();
        }
      }

      function onSelectedChange() {
        let temp: any[] = [];
        let arr = selectedList.value;
        if (arr && arr.length > 0) {
          temp = arr.map((k) => {
            return k[props.store];
          });
        }
        let str = temp.join(',');
        emit('update:value', str);
        emit('change', str);
        formItemContext.onFieldChange();
        console.log('데이터 선택', str);
      }

      function onSelected(_v, values) {
        console.log('역할 선택 완료：', values);
        loading.value = false;
        if (values && values.length > 0) {
          selectedList.value = values;
        } else {
          selectedList.value = [];
        }
        onSelectedChange();
        closeRoleModal();
      }

      // 현재는 데이터 다시 로드에 사용되는 상태만
      const currentAppId = ref('');
      const userStore = useUserStore();
      watchEffect(() => {
        let appId = props.appId;
        if (appId) {
          currentAppId.value = appId;
        } else {
          currentAppId.value = new Date().getTime() + '-0';
        }
      });

      watch(
        () => props.value,
        async (val) => {
          if (val) {
            if (loading.value === true) {
              await getRoleList(val);
            }
          } else {
            selectedList.value = [];
          }
          loading.value = true;
        },
        { immediate: true }
      );

      /**
       * 역할 목록 가져오기
       * @param ids
       */
      async function getRoleList(ids) {
        const url = '/api/v1/kkumz/sys/role/list';
        let params = {
          [props.store]: ids,
          pageSize: 200
        };
        // 특수조건 처리（백그라운드 엔터티가 roleCode이기 때문에 손상이 있고 직접 변경할 수 없으므로 문제가 발생합니다.）
        if (props.store === 'code') {
          params.roleCode = ids;
        }
        selectedList.value = [];
        const data = await defHttp.get({ url, params }, { isTransformResponse: false });
        console.log('getRoleList>>', data);
        if (data.success) {
          const { records } = data.result;
          let arr: any[] = [];
          if (records && records.length > 0) {
            for (let item of records) {
              arr.push({
                id: item.id,
                name: item.name || item.roleName,
                code: item.roleCode,
                checked: true,
                selectType: 'sys_role',
              });
            }
          }
          selectedList.value = arr;
        } else {
          console.error(data.message);
        }
      }

      return {
        selectedList,
        ellipsisInfo,
        maxCount,
        registerRoleModal,
        closeRoleModal,
        showModal,
        onSelected,
        unSelect,
        currentAppId,
      };
    },
  });
</script>

<style scoped lang="less">
  .select-input {
    padding: 0 5px;
    background-color: #fff;
    border: 1px solid #ccc;
    border-radius: 3px;
    box-sizing: border-box;
    display: flex;
    color: #9e9e9e;
    font-size: 14px;
    flex-wrap: nowrap;
    min-height: 32px;
    overflow-x: hidden;
    &.disabled-select {
      cursor: not-allowed;
      background-color: #f5f5f5 !important;
    }
  }
</style>
