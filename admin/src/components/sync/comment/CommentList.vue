<template>
  <div :style="{ position: 'relative', height: allHeight + 'px' }">
    <a-list ref="listRef" class="sync-comment-list" header="" item-layout="horizontal" :data-source="dataList" :style="{ height: commentHeight + 'px' }">
      <template #renderItem="{ item }">
        <a-list-item style="padding-left: 10px; flex-direction: column" @click="handleClickItem">
          <a-comment>
            <template #avatar>
              <a-avatar class="tx" :src="getAvatar(item)" :alt="getAvatarText(item)">{{ getAvatarText(item) }}</a-avatar>
            </template>

            <template #author>
              <div class="comment-author">
                <span>{{ item.fromUserId_dictText }}</span>

                <template v-if="item.toUserId">
                  <span>회신하다</span>
                  <span>{{ item.toUserId_dictText }}</span>
                  <Tooltip class="comment-last-content" @openChange="(v)=>visibleChange(v, item)">
                    <template #title>
                      <div v-html="getHtml(item.commentId_dictText)"></div>
                    </template>
                    <message-outlined />
                  </Tooltip>
                </template>
              </div>
            </template>

            <template #datetime>
              <div>
                <Tooltip :title="item.createTime">
                  <span>{{ getDateDiff(item) }}</span>
                </Tooltip>
              </div>
            </template>

            <template #actions>
              <span @click="showReply(item)">회신하다</span>

              <Popconfirm title="삭제하시겠습니까?" @confirm="deleteComment(item)">
                <span>삭제</span>
              </Popconfirm>
            </template>

            <template #content>
              <div class="content" v-html="getHtml(item.commentContent)" style="font-size: 15px">
              </div>

              <div v-if="item.fileList && item.fileList.length > 0">
                <!-- 역사적 문서 -->
                <history-file-list :dataList="item.fileList" isComment></history-file-list>
              </div>
            </template>
          </a-comment>
          <div v-if="item.commentStatus" class="inner-comment">
            <my-comment inner @cancel="item.commentStatus = false" @comment="(content, fileList) => replyComment(item, content, fileList)" :inputFocus="focusStatus"></my-comment>
          </div>
        </a-list-item>
      </template>
    </a-list>

    <div class="comment-area">
      <a-comment style="margin: 0 10px">
        <template #avatar>
          <a-avatar class="tx" :src="getMyAvatar()" :alt="getMyname()">{{ getMyname() }}</a-avatar>
        </template>
        <template #content>
          <my-comment ref="bottomCommentRef" @comment="sendComment" :inputFocus="focusStatus"></my-comment>
        </template>
      </a-comment>
    </div>
  </div>
</template>

<script>
  /**
   *댓글 목록
   */
  import { defineComponent, ref, onMounted, watch, watchEffect ,inject, nextTick } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  // import dayjs from 'dayjs';
  // import relativeTime from 'dayjs/plugin/relativeTime';
  // import customParseFormat from 'dayjs/plugin/customParseFormat';
  // dayjs.locale('zh');
  // dayjs.extend(relativeTime);
  // dayjs.extend(customParseFormat);

  import { MessageOutlined } from '@ant-design/icons-vue';
  import { Comment, Tooltip } from 'ant-design-vue';
  import { useUserStore } from '/@/store/modules/user';
  import MyComment from './MyComment.vue';
  import { list, saveOne, deleteOne, useCommentWithFile, useEmojiHtml, queryById, getGloablEmojiIndex } from './useComment';
  import { useMessage } from '/@/hooks/web/useMessage';
  import HistoryFileList from './HistoryFileList.vue';
  import { Popconfirm } from 'ant-design-vue';
  import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';

  export default defineComponent({
    name: 'CommentList',
    components: {
      MessageOutlined,
      AComment: Comment,
      Tooltip,
      MyComment,
      Popconfirm,
      HistoryFileList,
    },
    props: {
      tableName: propTypes.string.def(''),
      dataId: propTypes.string.def(''),
      datetime:  propTypes.number.def(1),
      // 빼야 할 다른 높이
      otherHeight: propTypes.number.def(0),
    },
    setup(props) {
      const { createMessage } = useMessage();
      const dataList = ref([]);
      const { userInfo } = useUserStore();
      const dayjs = inject('$dayjs')
      const listRef = ref(null);
      /**
       * 현재 사용자 이름을 가져옵니다
       */
      function getMyname() {
        if (userInfo.realname) {
          return userInfo.realname.substr(0, 2);
        }
        return '';
      }

      function getMyAvatar(){
        return userInfo.avatar;
      }

      // 아바타 받기
      function getAvatar(item) {
        if (item.fromUserAvatar) {
          return getFileAccessHttpUrl(item.fromUserAvatar)
        }
        return '';
      }

      // 아바타는 획득되지 않습니다. 사용자 이름의 처음 두 자리는 획득되지 않습니다.
      function getAvatarText(item){
        if (item.fromUserId_dictText) {
          return item.fromUserId_dictText.substr(0, 2);
        }
        return '알려지지 않은';
      }

      function getAuthor(item) {
        if (item.toUser) {
          return item.fromUserId_dictText + ' 회신하다 ' + item.fromUserId_dictText;
        } else {
          return item.fromUserId_dictText;
        }
      }

      function getDateDiff(item) {
        if (item.createTime) {
          const temp = dayjs(item.createTime, 'YYYY-MM-DD hh:mm:ss');
          return temp.fromNow();
        }
        return '';
      }
      const commentHeight = ref(300);
      const allHeight = ref(300);
      onMounted(() => {
        let otherHeight = props.otherHeight || 0;
        commentHeight.value = window.innerHeight - 57 - 46 - 70 - 160 - otherHeight;
        allHeight.value = window.innerHeight - 57 - 46 - 53 -20 - otherHeight;
      });

      /**
       * 데이터 로드
       * @returns {Promise<void>}
       */
      async function loadData() {
        const params = {
          tableName: props.tableName,
          tableDataId: props.dataId,
          column: 'createTime',
          order: 'desc',
        };
        const data = await list(params);
        if (!data || !data.records || data.records.length == 0) {
          dataList.value = [];
        } else {
          let array = data.records;
          console.log(123, array);
          dataList.value = array;
          // Number.MAX_SAFE_INTEGER Firefox가 호환되지 않아 10e4로 변경되었습니다.
          nextTick(() => {
            listRef.value && listRef.value.$el && (listRef.value.$el.scrollTop = 10e5);
          });
        }
      }

      const { saveCommentAndFiles } = useCommentWithFile(props);
      // 회신하다
      async function replyComment(item, content, fileList) {
        console.log(content, item);
        let obj = {
          fromUserId: userInfo.id,
          toUserId: item.fromUserId,
          commentId: item.id,
          commentContent: content
        }
        await saveCommentAndFiles(obj, fileList)
        await loadData();
      }

      //커멘트
      async function sendComment(content, fileList) {
        let obj = {
          fromUserId: userInfo.id,
          commentContent: content
        }
        await saveCommentAndFiles(obj, fileList)
        await loadData();
        focusStatus.value = false;
        setTimeout(()=>{
          focusStatus.value = true;
        },100)
      }

      //삭제
      async function deleteComment(item) {
        const params = { id: item.id };
        await deleteOne(params);
        await loadData();
      }

      /**
       * 답글이 열릴 때 트리거됨
       * @type {Ref<UnwrapRef<boolean>>}
       */
      const focusStatus = ref(false);
      function showReply(item) {
        let arr = dataList.value;
        for (let temp of arr) {
          temp.commentStatus = false;
        }
        item.commentStatus = true;
        focusStatus.value = false;
        focusStatus.value = true;
      }

      // Form 변경 - 댓글 목록을 다시 로드합니다.
      watchEffect(() => {
        if(props.datetime){
          if (props.tableName && props.dataId) {
            loadData();
          }
        }
      });

      //const storageEmojiIndex = inject('$globalEmojiIndex')
      const storageEmojiIndex = getGloablEmojiIndex()
      const { getHtml } = useEmojiHtml(storageEmojiIndex);
      const bottomCommentRef = ref()
      function handleClickItem(){
        bottomCommentRef.value.changeActive()
      }


      /**
       * 아이디를 기준으로 댓글정보 조회
       */
      async function visibleChange(v, item){
        if(v==true){
          if(!item.commentId_dictText){
            const data = await queryById(item.commentId);
            if(data.success == true){
              item.commentId_dictText = data.result.commentContent
            }else{
              console.error(data.message)
              item.commentId_dictText='댓글이 삭제되었습니다';
            }
          }
        }
      }

      return {
        dataList,
        getAvatar,
        getAvatarText,
        getAuthor,
        getDateDiff,
        commentHeight,
        allHeight,
        replyComment,
        sendComment,
        getMyname,
        getMyAvatar,

        focusStatus,
        showReply,
        deleteComment,
        getHtml,
        handleClickItem,
        bottomCommentRef,
        visibleChange,
        listRef,
      };
    },
  });
</script>

<style lang="less" scoped>
  .sync-comment-list {
    overflow: auto;
    /* border-bottom: 1px solid #eee;*/
    .inner-comment {
      width: 100%;
      padding: 0 10px;
    }
    .ant-comment {
      width: 100%;
    }
  }
  .comment-author {
    span {
      margin: 3px;
    }
    .comment-last-content {
      margin-left: 5px;
      &:hover{
        color: @primary-color;
      }
    }
  }
  .ant-list-items{
    .ant-list-item:last-child{
      margin-bottom: 46px;
    }
  }
  .tx{
    margin-top: 4px;
  }
  .comment-area {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    border-top: 1px solid #eee;
    background-color: #fff;
  }
  html[data-theme='dark'] {
    .comment-area {
      border-color: rgba(253, 253, 253, 0.12);
      background-color: #1f1f1f;
    }
    .content {
      color:rgba(255, 255, 255, 0.85);
    }
  }
</style>
