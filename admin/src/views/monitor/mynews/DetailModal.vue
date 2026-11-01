<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="세부 사항을 확인하세요" :minHeight="600" :showCancelBtn="false" :showOkBtn="false" :height="88" :destroyOnClose="true">
    <a-card class="daily-article">
      <a-card-meta :title="content.titile" :description="'등록자：' + content.sender + ' 출시 시간： ' + content.sendTime"> </a-card-meta>
      <a-divider />
      <div v-html="content.msgContent" class="article-content"></div>
      <div>
        <a-button v-if="hasHref" @click="jumpToHandlePage">신청하러 가기<ArrowRightOutlined /></a-button>
      </div>
    </a-card>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { ArrowRightOutlined } from '@ant-design/icons-vue';
  import { useRouter } from 'vue-router'
  import xss from 'xss'
  import { options } from './XssWhiteList'

  const router = useRouter()

  import { ref, unref } from 'vue';
  const isUpdate = ref(true);
  const content = ref({});
  //Form 할당
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      //data.record.msgContent = '<p>2323</p><input onmouseover=alert(1)>xss test';
      if(data.record.msgContent){
        data.record.msgContent = xss(data.record.msgContent,options);
      }
      content.value = data.record;
      showHrefButton();
    }
  });

  const hasHref = ref(false)
  //메시지 세부정보를 보고 다음으로 이동하세요.
  function showHrefButton(){
    if(content.value.busId){
      hasHref.value = true;
    }
  }
  //처리 페이지로 이동
  function jumpToHandlePage(){
    let temp:any = content.value
    if(temp.busId){
      //이 버스 ID는 작업 ID입니다.
      let jsonStr = temp.msgAbstract;
      let query = {};
      try {
        if(jsonStr){
          let temp = JSON.parse(jsonStr)
          if(temp){
            Object.keys(temp).map(k=>{
              query[k] = temp[k]
            });
          }
        }
      }catch(e){
        console.log('매개변수 구문 분석 예외', e)
      }

      console.log('query', query, jsonStr)
      console.log('busId', temp.busId)

      if(Object.keys(query).length>0){
        // taskId taskDefKey procInsId
        router.push({ path: '/task/handle/' + temp.busId, query: query })
      }else{
        router.push({ path: '/task/handle/' + temp.busId })
      }
    }
    closeModal();
  }

</script>

<style scoped lang="less">
  .detail-iframe {
    border: 0;
    width: 100%;
    height: 100%;
    min-height: 600px;
  }
</style>
