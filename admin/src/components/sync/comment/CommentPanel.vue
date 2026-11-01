<template>
  <div class="comment-tabs-warp" v-if="showStatus">
    <a-tabs v-if="show" @change="handleChange" :animated="false">
      <a-tab-pane v-if="showComment" tab="커멘트" key="comment" class="comment-list-tab">
        <comment-list :tableName="tableName" :dataId="dataId" :datetime="datetime1" :otherHeight="otherHeight"></comment-list>
      </a-tab-pane>
      <a-tab-pane v-if="showFiles" tab="문서" key="file">
        <comment-files :tableName="tableName" :dataId="dataId" :datetime="datetime2"></comment-files>
      </a-tab-pane>
      <a-tab-pane v-if="showDataLog" tab="통나무" key="log">
        <data-log-list :tableName="tableName" :dataId="dataId" :datetime="datetime3"></data-log-list>
      </a-tab-pane>
    </a-tabs>
  </div>
  <a-empty v-else description="새 페이지에서는 댓글을 지원하지 않습니다." />
</template>

<script>
  /**
   *댓글 영역
   */
  import { propTypes } from '/@/utils/propTypes';
  import { computed, ref, nextTick } from 'vue';
  import CommentList from './CommentList.vue';
  import CommentFiles from './CommentFiles.vue';
  import DataLogList from './DataLogList.vue';

  export default {
    name: 'CommentPanel',
    components: {
      CommentList,
      CommentFiles,
      DataLogList,
    },
    props: {
      tableName: propTypes.string.def(''),
      dataId: propTypes.string.def(''),
      // 댓글 표시
      showComment: propTypes.bool.def(true),
      // 파일 표시
      showFiles: propTypes.bool.def(true),
      // 로그 표시
      showDataLog: propTypes.bool.def(true),
      // 빼야 할 다른 높이
      otherHeight: propTypes.number.def(0),
    },
    setup(props) {
      const showStatus = computed(() => {
        if (props.dataId && props.tableName) {
          return true;
        }
        return false;
      });

      const datetime1 = ref(1);
      const datetime2 = ref(1);
      const datetime3 = ref(1);
      const show = ref(true);
      function handleChange(e) {
        let temp = new Date().getTime();
        if (e == 'comment') {
          datetime1.value = temp;
        } else if (e == 'file') {
          datetime2.value = temp;
        } else {
          datetime3.value = temp;
        }
      }

      // VUEN-1978【bug】20 온라인 관련 기록 및 기타 테이블 필드에 문제가 있습니다. 데이터 수정 후, 탭 전환 없이 다시 열면 수정 로그가 변경되지 않습니다.
      function reload() {
        let temp = new Date().getTime();
        datetime1.value = temp;
        datetime2.value = temp;
        datetime3.value = temp;
        // 컴포넌트 내 상태를 재설정하려면 다시 엽니다.
        show.value = false;
        nextTick(() => {
          show.value = true;
        });
      }

      return {
        showStatus,
        handleChange,
        datetime1,
        datetime2,
        datetime3,
        reload,
        show,
      };
    },
  };
</script>

<style lang="less" scoped>
  .comment-tabs-warp {
    height: 100%;
    overflow: visible;
    > .ant-tabs {
      overflow: visible;
    }
  }
  //antd3 업그레이드 후 Form 오른쪽에서 스타일 조정에 대해 논의합니다.
  :deep(.ant-tabs-top  .ant-tabs-nav, .ant-tabs-bottom  .ant-tabs-nav, .ant-tabs-top  div  .ant-tabs-nav, .ant-tabs-bottom  div  .ant-tabs-nav) {
    margin: 0 16px 0;
  }
</style>
