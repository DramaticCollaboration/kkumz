import { ref, reactive } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { getDictItemsByCode } from '/@/utils/dict/index';
import { useRouter, useRoute } from 'vue-router'
import { useAppStore } from '/@/store/modules/app';
import { useTabs } from '/@/hooks/web/useTabs';
import { useModal } from '/@/components/Modal';
import {useMessage} from "/@/hooks/web/useMessage";

/**
 * 목록 인터페이스
 * @param params
 */
const queryMessageList = (params) => {
  const url = '/api/v1/kkumz/sys/annountCement/vue3List';
  return defHttp.get({ url, params });
};

/**
 * 메시지 목록 데이터 가져오기
 */
export function useSysMessage() {
  const { createMessage } = useMessage();
  const rangeDateArray = getDictItemsByCode('rangeDate');
  console.log('+++++++++++++++++++++');
  console.log('rangeDateArray', rangeDateArray);
  console.log('+++++++++++++++++++++');

  const messageList = ref<any[]>([]);
  const pageNo = ref(1)
  let pageSize = 10;
  
  const searchParams = reactive({
    fromUser: '',
    rangeDateKey: '',
    rangeDate: [],
    starFlag: ''
  });


  function getQueryParams() {
    let { fromUser, rangeDateKey, rangeDate, starFlag } = searchParams;
    let params = {
      fromUser,
      starFlag,
      rangeDateKey,
      beginDate: '',
      endDate: '',
      pageNo: pageNo.value,
      pageSize
    };
    if (rangeDateKey == 'zdy') {
      params.beginDate = rangeDate[0]+' 00:00:00';
      params.endDate = rangeDate[1]+' 23:59:59';
    }
    return params;
  }

  // 데이터가 로드되었나요?
  const loadEndStatus = ref(false);
  
  //데이터 요청
  async function loadData() {
    if(loadEndStatus.value === true){
      return;
    }
    let params = getQueryParams();
    const data = await queryMessageList(params);
    console.log('결과 얻기', data);
    if(!data || data.length<=0){
      loadEndStatus.value = true;
      return;
    }
    if(data.length<pageSize){
      loadEndStatus.value = true;
    }
    pageNo.value = pageNo.value+1
    let temp:any[] = messageList.value;
    temp.push(...data);
    messageList.value = temp;
  }

  //새로고침
  function reset(){
    messageList.value = []
    pageNo.value = 1;
    loadEndStatus.value = false;
  }
  
  //별표 표시
  async function updateStarMessage(item){
    const url = '/api/v1/kkumz/sys/sysAnnouncementSend/edit';
    let starFlag = '1';
    if(item.starFlag==starFlag){
      starFlag = '0'
    }
    const params = {
      starFlag,
      id: item.sendId
    }
    const data:any = await defHttp.put({url, params}, {isTransformResponse: false});
    if(data.success === true){
    }else{
      createMessage.warning(data.message)
    }
  }


  const loadingMoreStatus = ref(false);
  async function onLoadMore() {
    loadingMoreStatus.value = true;
    await loadData();
    loadingMoreStatus.value = false;
  }

  function noRead(item) {
    if (item.readFlag === '1') {
      return false;
    }
    return true;
  }

  // 메시지 유형
  function getMsgCategory(item) {
    if(item.busType=='email'){
      return '이메일 알림:';
    } else if(item.busType=='bpm'){
      return '프로세스 알림:';
    } else if(item.busType=='bpm_cc'){
      return '프로세스 CC:';
    }else if(item.busType=='bpm_task'){
      return '프로세스 작업:';
    } else if (item.msgCategory == '2') {
      return '시스템 메시지:';
    } else if (item.msgCategory == '1') {
      return '공지사항:';
    }
    return '';
  }
  
  return {
    messageList,
    reset,
    loadData,
    loadEndStatus,
    searchParams,
    updateStarMessage,
    
    onLoadMore,
    noRead,
    getMsgCategory,
    
  };
}

/**
 * 메시지 점프에 사용됨
 */
export function useMessageHref(emit, props){
  const messageHrefArray: any[] = getDictItemsByCode('messageHref');
  const router = useRouter();
  const appStore = useAppStore();
  const rt = useRoute();
  const { close: closeTab, closeSameRoute } = useTabs();
 // const defaultPath = '/monitor/mynews';
  //const bpmPath = '/task/handle/'
  
  async function goPage(record, openModalFun?){
    if(!record.busType || record.busType == 'msg_node'){
      if(!openModalFun){
        // 홈 페이지의 메시지 알림에서 이동
        await goPageFromOuter(record);
      }else{
        // 메시지 페이지 목록에서 세부정보 페이지를 클릭하면 모달이 바로 열립니다.
        openModalFun()
      }
    }else{
      await goPageWithBusType(record)
    }
/*    busId: "1562035005173587970"
    busType: "email"
    openPage: "modules/eoa/email/modals/EoaEmailInForm"
    openType: "component"*/
  }

  /**
   * 다양한 버스 유형에 따라 다른 페이지로 이동
   * @param record
   */
  async function goPageWithBusType(record){
    const { busType, busId, msgAbstract } = record;
    let temp = messageHrefArray.filter(item=>item.value === busType);
    if(!temp || temp.length==0){
      console.error('현재 업종이 인식되지 않습니다.', busType);
      return;
    }
    let path = temp[0].text;
    path = path.replace('{DETAIL_ID}', busId)
    //고정 매개변수 DetailId는 Form 데이터를 쿼리하는 데 사용됩니다.
    let query:any = {
      detailId: busId
    };
    // 추가 매개변수 처리
    if(msgAbstract){
      try {
        let json = JSON.parse(msgAbstract);
        Object.keys(json).map(k=>{
          query[k] = json[k]
        });
      }catch (e) {
        console.error('msgAbstract 매개변수가 JSON 형식이 아닙니다.', msgAbstract)
      }
    }
    // 점프 루트
    appStore.setMessageHrefParams(query);
    if(rt.path.indexOf(path)>=0){
      await closeTab();
      await router.replace({ path: path, query:{ time: new Date().getTime() } });
    }else{
      closeSameRoute(path)
      await router.push({ path: path });
    }
  }

  /**
   * 홈페이지의 메시지 알림에서 모달을 열어 메시지 목록으로 이동하세요.
   * @param record
   */
  async function goPageFromOuter(record){
    //정의된 비즈니스 유형이 없습니다. 내 메시지 페이지로 바로 이동합니다.
    emit('detail', record)
  }
  
  return {
    goPage
  }
}
