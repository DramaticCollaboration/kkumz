<template>
  <BasicModal
    @register="register"
    :getContainer="getContainer"
    :canFullscreen="false"
    title="사용자 선택"
    :width="600"
    wrapClassName="j-user-select-modal2"
  >
    <!-- 부서 드롭다운 상자 -->
    <a-select v-model:value="selectedDepart" style="width: 100%" class="depart-select" @change="onDepartChange">
      <a-select-option v-for="item in departOptions" :value="item.value">{{ item.label }}</a-select-option>
    </a-select>

    <div style="position: relative; min-height: 350px">
      <!-- 사용자 검색창 -->
      <div :class="searchInputStatus ? 'my-search all-width' : 'my-search'">
        <span :class="searchInputStatus ? 'hidden' : ''" style="margin-left: 10px"
          ><SearchOutlined style="color: #c0c0c0" @click="showSearchInput"
        /></span>
        <div style="width: 100%" :class="searchInputStatus ? '' : 'hidden'">
          <a-input v-model:value="searchText" @pressEnter="onSearchUser" style="width: 100%" placeholder="사용자 이름을 입력하고 Enter를 눌러 검색하세요.">
            <template #prefix>
              <SearchOutlined style="color: #c0c0c0" />
            </template>
            <template #suffix>
              <CloseOutlined title="검색 종료" @click="clearSearch" />
            </template>
          </a-input>
        </div>
      </div>

      <!-- tabs -->
      <div class="my-tabs">
        <a-tabs v-model:activeKey="myActiveKey" :centered="true" @change="onChangeTab">
          <!-- 모든 사용자 -->
          <a-tab-pane key="1" tab="모두" forceRender>
            <user-list :multi="multi" :excludeUserIdList="excludeUserIdList" :dataList="userDataList" :selectedIdList="selectedIdList" depart @selected="onSelectUser" @unSelect="unSelectUser" />
          </a-tab-pane>

          <!-- 부서 사용자 -->
          <a-tab-pane key="2" tab="부서별" forceRender>
            <depart-user-list
              :searchText="searchText"
              :selectedIdList="selectedIdList"
              :excludeUserIdList="excludeUserIdList"
              @loaded="initDepartOptions"
              @selected="onSelectUser"
              @unSelect="unSelectUser"
            />
          </a-tab-pane>

          <!-- 역할 사용자 -->
          <a-tab-pane key="3" tab="역할별" forceRender>
            <role-user-list :excludeUserIdList="excludeUserIdList" :searchText="searchText" :selectedIdList="selectedIdList" @selected="onSelectUser" @unSelect="unSelectUser" />
          </a-tab-pane>
        </a-tabs>
      </div>

      <!-- 사용자 선택 -->
      <div class="selected-users" style="width: 100%; overflow-x: hidden">
        <SelectedUserItem v-for="item in selectedUserList" :info="item" @unSelect="unSelectUser" />
      </div>
    </div>

    <template #footer>
      <div style="display: flex; justify-content: space-between; width: 100%">
        <div class="select-user-page-info">
          <a-pagination
            v-if="myActiveKey == '1'"
            v-model:current="pageNo"
            size="small"
            :total="totalRecord"
            show-quick-jumper
            @change="onPageChange"
          />
        </div>
        <a-button type="primary" @click="handleOk">확인</a-button>
      </div>
    </template>
  </BasicModal>
</template>

<script lang="ts">
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { SearchOutlined, CloseOutlined } from '@ant-design/icons-vue';
  import UserList from './UserList.vue';
  import SelectedUserItem from './SelectedUserItem.vue';
  import DepartUserList from './UserListAndDepart.vue';
  import RoleUserList from './UserListAndRole.vue';
  import { Pagination } from 'ant-design-vue';
  const APagination = Pagination;
  import { defHttp } from '/@/utils/http/axios';

  import {computed, ref, toRaw, unref} from 'vue';
  import { useUserStore } from '/@/store/modules/user';
  import { mySelfData } from './useUserSelect'

  export default {
    name: 'UserSelectModal',
    components: {
      BasicModal,
      SearchOutlined,
      CloseOutlined,
      SelectedUserItem,
      UserList,
      DepartUserList,
      RoleUserList,
      APagination,
    },
    props: {
      multi: {
        type: Boolean,
        default: false,
      },
      getContainer: {
        type: Function,
        default: null,
      },
      //나 자신을 제외합니까?
      izExcludeMy: {
        type: Boolean,
        default: false,
      },
      //현재 사용자 표현식을 고급 쿼리의 조건으로 사용할지 여부입니다.
      inSuperQuery:{
        type: Boolean,
        default: false,
      }
    },
    emits: ['selected', 'register'],
    setup(props, { emit }) {
      const myActiveKey = ref('1');
      const selectedUserList = ref<any[]>([]);
      const userStore = useUserStore();
      const selectedIdList = computed(() => {
        let arr = selectedUserList.value;
        if (!arr || arr.length == 0) {
          return [];
        } else {
          return arr.map((k) => k.id);
        }
      });
      // QQYUN-4152【애플리케이션】이미 존재하는 사용자를 추가 시 다시 선택할 수 있습니다.
      const excludeUserIdList = ref<any[]>([]);

      // 팝업 이벤트
      const [register] = useModalInner((data) => {
        let list = data.list;
        if (list && list.length > 0) {
          selectedUserList.value = [...list];
        } else {
          selectedUserList.value = [];
        }
        if(data.excludeUserIdList){
          excludeUserIdList.value = data.excludeUserIdList;
        }else{
          excludeUserIdList.value = [];
        }
        //나 자신을 제외하는 경우에는excludeUserIdList.push를 사용하여 제외하면 됩니다.
        if (props.izExcludeMy) {
          excludeUserIdList.value.push(userStore.getUserInfo.id);
        }
        //사용자 목록 로드
        loadUserList();
      });

      // 이벤트 결정
      function handleOk() {
        let arr = toRaw(selectedUserList.value);
        emit('selected', arr);
      }

      /*--------------사용자 필터링을 위한 부서 드롭다운 상자---------------*/
      const selectedDepart = ref('');
      const departOptions = ref<any[]>([]);
      function initDepartOptions(options) {
        departOptions.value = [{ value: '', label: '모든 사용자' }, ...options];
        selectedDepart.value = '';
      }
      function onDepartChange() {
        loadUserList();
      }
      /*--------------사용자 필터링을 위한 부서 드롭다운 상자---------------*/

      /*--------------대시보드 검색창---------------*/
      const searchInputStatus = ref(false);
      const searchText = ref('');
      function showSearchInput(e) {
        e && prevent(e);
        searchInputStatus.value = true;
      }

      // 이벤트 트리거 쿼리 입력
      function onSearchUser() {
        pageNo.value = 1;
        loadUserList();
      }

      // 이름으로 필터 지우기
      function clearSearch(e) {
        e && prevent(e);
        pageNo.value = 1;
        searchText.value = '';
        searchInputStatus.value = false;
        loadUserList();
      }
      /*--------------대시보드 검색창---------------*/

      /*--------------데이터 로드---------------*/
      const pageNo = ref(1);
      const totalRecord = ref(0);
      const userDataList = ref<any[]>([]);
      async function onPageChange() {
        console.log('onPageChange', pageNo.value);
        await loadUserList();
      }
      async function loadUserList() {
        const url = '/api/v1/kkumz/sys/user/selectUserList';
        let params = {
          pageNo: pageNo.value,
          pageSize: 10,
        };
        if (searchText.value) {
          params['keyword'] = searchText.value;
        }
        if (selectedDepart.value) {
          params['departId'] = selectedDepart.value;
        }

        if(unref(excludeUserIdList) && unref(excludeUserIdList).length>0){
          params['excludeUserIdList'] = excludeUserIdList.value.join(",");
        }

        const data = await defHttp.get({ url, params }, { isTransformResponse: false });
        if (data.success) {
          let { records, total } = data.result;
          totalRecord.value = total;
          initCurrentUserData(records);
          userDataList.value = records;
        } else {
          console.error(data.message);
        }
        console.log('loadUserList', data);
      }

      // 사용자 목록에 현재 사용자 옵션을 추가합니다.
      function initCurrentUserData(records) {
        if(pageNo.value==1 && props.inSuperQuery === true){
          records.unshift({...mySelfData})
        }
      }
      /*--------------데이터 로드---------------*/

      /*--------------선택/선택 취소---------------*/
      function onSelectUser(info) {
        if (props.multi === true) {
          let arr = selectedUserList.value;
          let idList = selectedIdList.value;
          if (idList.indexOf(info.id) < 0) {
            arr.push({ ...info });
            selectedUserList.value = arr;
          }
        } else {
          selectedUserList.value = [{ ...info }];
        }
      }
      function unSelectUser(id) {
        let arr = selectedUserList.value;
        let index = -1;
        for (let i = 0; i < arr.length; i++) {
          if (arr[i].id === id) {
            index = i;
            break;
          }
        }
        if (index >= 0) {
          arr.splice(index, 1);
          selectedUserList.value = arr;
        }
      }
      /*--------------선택/선택 취소---------------*/

      function onChangeTab(tab) {
        myActiveKey.value = tab;
      }

      function prevent(e) {
        e.preventDefault();
        e.stopPropagation();
      }

      //데이터의 첫 번째 페이지 로드
      loadUserList();

      return {
        selectedDepart,
        departOptions,
        initDepartOptions,
        onDepartChange,

        register,
        handleOk,

        searchText,
        searchInputStatus,
        showSearchInput,
        onSearchUser,
        clearSearch,

        myActiveKey,
        onChangeTab,

        pageNo,
        totalRecord,
        onPageChange,
        userDataList,
        selectedUserList,
        selectedIdList,
        onSelectUser,
        unSelectUser,
        excludeUserIdList
      };
    },
  };
</script>

<style lang="less">
  .j-user-select-modal2 {
    .depart-select {
      .ant-select-selector {
        color: #fff !important;
        background-color: #409eff !important;
        border-radius: 5px !important;
      }
      .ant-select-selection-item,
      .ant-select-arrow {
        color: #fff !important;
      }
    }
    .my-search {
      position: absolute;
      top: 14px;
      z-index: 1;
      &.all-width {
        width: 100%;
      }

      .anticon {
        cursor: pointer;
        &:hover {
          color: #0a8fe9 !important;
        }
      }
      .hidden {
        display: none;
      }
    }

    .my-tabs {
    }

    .selected-users {
      display: flex;
      flex-wrap: wrap;
      flex-direction: row;
      padding-top: 15px;
    }

    .scroll-container {
      padding-bottom: 0 !important;
    }
  }
</style>
