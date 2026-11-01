<template>
    <div class="app-loading">
        <div class="app-loading-wrap">
            <img src="/resource/img/logo.png" class="app-loading-logo" alt="Logo">
            <div class="app-loading-dots">
                <span class="dot dot-spin"><i></i><i></i><i></i><i></i></span>
            </div>
            <div class="app-loading-title">Empasy 엔터프라이즈 AI 플랫폼</div>
        </div>
    </div>
</template>


<script lang="ts">
  /**
   * 주소에 토큰이 포함된 경우 로그인 작업을 위해 이 페이지로 이동하세요.
   */
  import { useRoute, useRouter } from 'vue-router';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';

  export default {
    name: "TokenLogin",
    setup(){
      const route = useRoute();
      let router = useRouter();
      const {createMessage, notification} = useMessage()
      const {t} = useI18n();
      const routeQuery:any = route.query;
      if(!routeQuery){
        createMessage.warning('잘못된 매개변수')
      }

      const token = routeQuery['loginToken'];
      if(!token){
        createMessage.warning('토큰이 유효하지 않습니다')
      }
      const userStore = useUserStore();
      userStore.ThirdLogin({ token, thirdType:'email', goHome: false }).then(res => {
        console.log("res====>doThirdLogin",res)
        if(res && res.userInfo){
          requestSuccess(res)
        }else{
          requestFailed(res)
        }
      });

      function requestFailed (err) {
        notification.error({
          message: '로그인 실패',
          description: ((err.response || {}).data || {}).message || err.message || "요청에 오류가 발생했습니다. 나중에 다시 시도해 주세요.",
          duration: 4,
        });
      }

      function requestSuccess(res){
        let info = routeQuery.info;
        if(info){
          let query = JSON.parse(info);
          let path = '';
          if(query.isLowApp === 1){
            path = '/myapps/personalOffice/myTodo'
          }else{
            let taskId = query.taskId;
            path = '/task/handle/' + taskId
          }

          router.replace({ path, query });
          notification.success({
            message: t('sys.login.loginSuccessTitle'),
            description: `${t('sys.login.loginSuccessDesc')}: ${res.userInfo.realname}`,
            duration: 3,
          });
        }else{
          notification.error({
            message: '매개변수가 잘못되었습니다.',
            description: "페이지 점프 매개변수가 손실되었습니다. 로그를 확인하세요.",
            duration: 4,
          });
        }
      }
    }
  }
</script>

<style scoped>

    html[data-theme='dark'] .app-loading {
        background-color: #2c344a;
    }

    html[data-theme='dark'] .app-loading .app-loading-title {
        color: rgba(255, 255, 255, 0.85);
    }

    .app-loading {
        display: flex;
        width: 100%;
        height: 100%;
        justify-content: center;
        align-items: center;
        flex-direction: column;
        background-color: #f4f7f9;
    }

    .app-loading .app-loading-wrap {
        position: absolute;
        top: 50%;
        left: 50%;
        display: flex;
        -webkit-transform: translate3d(-50%, -50%, 0);
        transform: translate3d(-50%, -50%, 0);
        justify-content: center;
        align-items: center;
        flex-direction: column;
    }

    .app-loading .dots {
        display: flex;
        padding: 98px;
        justify-content: center;
        align-items: center;
    }

    .app-loading .app-loading-title {
        display: flex;
        margin-top: 30px;
        font-size: 30px;
        color: rgba(0, 0, 0, 0.85);
        justify-content: center;
        align-items: center;
    }

    .app-loading .app-loading-logo {
        display: block;
        width: 90px;
        margin: 0 auto;
        margin-bottom: 20px;
    }

    .dot {
        position: relative;
        display: inline-block;
        width: 48px;
        height: 48px;
        margin-top: 30px;
        font-size: 32px;
        transform: rotate(45deg);
        box-sizing: border-box;
        animation: antRotate 1.2s infinite linear;
    }

    .dot i {
        position: absolute;
        display: block;
        width: 20px;
        height: 20px;
        background-color: #0065cc;
        border-radius: 100%;
        opacity: 0.3;
        transform: scale(0.75);
        animation: antSpinMove 1s infinite linear alternate;
        transform-origin: 50% 50%;
    }

    .dot i:nth-child(1) {
        top: 0;
        left: 0;
    }

    .dot i:nth-child(2) {
        top: 0;
        right: 0;
        -webkit-animation-delay: 0.4s;
        animation-delay: 0.4s;
    }

    .dot i:nth-child(3) {
        right: 0;
        bottom: 0;
        -webkit-animation-delay: 0.8s;
        animation-delay: 0.8s;
    }

    .dot i:nth-child(4) {
        bottom: 0;
        left: 0;
        -webkit-animation-delay: 1.2s;
        animation-delay: 1.2s;
    }
    @keyframes antRotate {
        to {
            -webkit-transform: rotate(405deg);
            transform: rotate(405deg);
        }
    }
    @-webkit-keyframes antRotate {
        to {
            -webkit-transform: rotate(405deg);
            transform: rotate(405deg);
        }
    }
    @keyframes antSpinMove {
        to {
            opacity: 1;
        }
    }
    @-webkit-keyframes antSpinMove {
        to {
            opacity: 1;
        }
    }

</style>
