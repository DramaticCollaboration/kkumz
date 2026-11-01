<template>
  <div :class="prefixCls">
    <Badge :count="count" :overflowCount="9" :offset="[-4, 10]" :numberStyle="numberStyle" @click="clickBadge">
      <BellOutlined />
    </Badge>

    <DynamicNotice ref="dynamicNoticeRef" v-bind="dynamicNoticeProps" />
    <DetailModal @register="registerDetail" />

    <sys-message-modal @register="registerMessageModal" @refresh="reloadCount"></sys-message-modal>
  </div>
</template>
<script lang="ts">
  import { computed, defineComponent, ref, unref, reactive, onMounted, getCurrentInstance } from 'vue';
  import { Popover, Tabs, Badge } from 'ant-design-vue';
  import { BellOutlined } from '@ant-design/icons-vue';
  import { tabListData } from './data';
  import { listCementByUser, editCementSend } from './notify.api';
  import NoticeList from './NoticeList.vue';
  import DetailModal from '/@/views/monitor/mynews/DetailModal.vue';
  import DynamicNotice from '/@/views/monitor/mynews/DynamicNotice.vue';
  import { useModal } from '/@/components/Modal';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useGlobSetting } from '/@/hooks/setting';
  import { useUserStore } from '/@/store/modules/user';
  import { connectWebSocket, onWebSocket } from '/@/hooks/web/useWebSocket';
  import { readAllMsg } from '/@/views/monitor/mynews/mynews.api';
  import { getToken } from '/@/utils/auth';
  import md5 from 'crypto-js/md5';

  import SysMessageModal from '/@/views/system/message/components/SysMessageModal.vue'

  export default defineComponent({
    components: {
      Popover,
      BellOutlined,
      Tabs,
      TabPane: Tabs.TabPane,
      Badge,
      NoticeList,
      DetailModal,
      DynamicNotice,
      SysMessageModal,
    },
    setup() {
      const { prefixCls } = useDesign('header-notify');
      const instance: any = getCurrentInstance();
      const userStore = useUserStore();
      const glob = useGlobSetting();
      const dynamicNoticeProps = reactive({ path: '', formData: {} });
      const [registerDetail, detailModal] = useModal();
      const listData = ref(tabListData);
      const count = computed(() => {
        let count = 0;
        for (let i = 0; i < listData.value.length; i++) {
          count += listData.value[i].count;
        }
        return count;
      });

      const [registerMessageModal, { openModal: openMessageModal }] = useModal();
      function clickBadge(){
        //메시지 목록 팝업 창 앞 모서리 표시 제거
        for (let i = 0; i < listData.value.length; i++) {
          listData.value[i].count = 0;
        }
        openMessageModal(true, {})
      }

      const popoverVisible = ref<boolean>(false);
      onMounted(() => {
       initWebSocket();
      });

      function mapAnnouncement(item) {
        return {
          ...item,
          title: item.titile,
          description: item.msgAbstract,
          datetime: item.sendTime,
        };
      }

      // 시스템 메시지 받기
      async function loadData() {
        try {
          let { anntMsgList, sysMsgList, anntMsgTotal, sysMsgTotal } = await listCementByUser({
            pageSize: 5,
          });
          listData.value[0].list = anntMsgList.map(mapAnnouncement);
          listData.value[1].list = sysMsgList.map(mapAnnouncement);
          listData.value[0].count = anntMsgTotal;
          listData.value[1].count = sysMsgTotal;
        } catch (e) {
          console.warn('시스템 메시지 알림 예외：', e);
        }
      }

      loadData();

      function onNoticeClick(record) {
        try {
          editCementSend(record.id);
          loadData();
        } catch (e) {
          console.error(e);
        }
        if (record.openType === 'component') {
          dynamicNoticeProps.path = record.openPage;
          dynamicNoticeProps.formData = { id: record.busId };
          instance.refs.dynamicNoticeRef?.detail(record.openPage);
        } else {
          detailModal.openModal(true, {
            record,
            isUpdate: true,
          });
        }
        popoverVisible.value = false;
      }

      // WebSocket 초기화
      function initWebSocket() {
        let token = getToken();
        //로그인 토큰에서 짧은 식별자 생성
        let wsClientId = md5(token);
        let userId = unref(userStore.getUserInfo).id + "_" + wsClientId;
        // WebSocket에서 사용하는 프로토콜은 일반 요청의 프로토콜과 다릅니다. ws는 http와 동일하며 wss는 https와 동일합니다.
        const baseDomain = (glob.domainUrl || window.location.origin)?.replace(/\/api\/?$/, '').replace(/\/+$/, '');
        let url = baseDomain?.replace('https://', 'wss://').replace('http://', 'ws://') + '/websocket/' + userId;
        connectWebSocket(url);
        onWebSocket(onWebSocketMessage);
      }

      function onWebSocketMessage(data) {
        if (data.cmd === 'topic' || data.cmd === 'user') {
          //백그라운드에서 데이터를 저장하는 속도가 너무 느리고 프런트 엔드에서 메시지 새로 고침이 지연됩니다.
          setTimeout(()=>{
            loadData();
          }, 1000)
        }
      }

      // 메시지 지우기
      function onEmptyNotify() {
        popoverVisible.value = false;
        readAllMsg({}, loadData);
      }
      async function reloadCount(id){
        try {
          await editCementSend(id);
          await loadData();
        } catch (e) {
          console.error(e);
        }
      }

      return {
        prefixCls,
        listData,
        count,
        clickBadge,
        registerMessageModal,
        reloadCount,
        onNoticeClick,
        onEmptyNotify,
        numberStyle: {},
        popoverVisible,
        registerDetail,
        dynamicNoticeProps,
      };
    },
  });
</script>
<style lang="less">
  //noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-header-notify';

  .@{prefix-cls} {
    padding-top: 2px;

    &__overlay {
      max-width: 340px;

      .ant-popover-inner-content {
        padding: 0;
      }

      .ant-tabs-nav {
        margin-bottom: 12px;
      }

      .ant-list-item {
        padding: 12px 24px;
        transition: background-color 300ms;

      }

      .bottom-buttons {
        text-align: center;
        border-top: 1px solid #f0f0f0;
        height: 42px;

        .ant-btn {
          border: 0;
          height: 100%;

          &:first-child {
            border-right: 1px solid #f0f0f0;
          }
        }
      }
    }

    .ant-tabs-content {
      width: 300px;
    }

    .ant-badge {
      font-size: 18px;

      .ant-badge-count {
        @badget-size: 16px;
        width: @badget-size;
        height: @badget-size;
        min-width: @badget-size;
        line-height: @badget-size;
        padding: 0;

        .ant-scroll-number-only > p.ant-scroll-number-only-unit {
          font-size: 14px;
          height: @badget-size;
        }
      }

      .ant-badge-multiple-words {
        padding: 0 0 0 2px;
        font-size: 12px;
      }

      svg {
        width: 0.9em;
      }
    }
  }

  // 다크 모드 호환
  [data-theme='dark'] .@{prefix-cls} {
    &__overlay {
      .ant-list-item {
        &:hover {
          background-color: #111b26;
        }
      }

      .bottom-buttons {
        border-top: 1px solid #303030;

        .ant-btn {
          &:first-child {
            border-right: 1px solid #303030;
          }
        }
      }
    }
  }

</style>
