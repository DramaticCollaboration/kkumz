import { ref, unref } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { useGlobSetting } from '/@/hooks/setting';
import { useMessage } from '/@/hooks/web/useMessage';
import { useUserStore } from '/@/store/modules/user';
import { setThirdCaptcha, getCaptcha } from '/@/api/sys/user';
import { useI18n } from '/@/hooks/web/useI18n';

export function useThirdLogin() {
  const { createMessage, notification } = useMessage();
  const { t } = useI18n();
  const glob = useGlobSetting();
  const userStore = useUserStore();
  //제3자 유형
  const thirdType = ref('');
  //제3자 로그인 관련 정보
  const thirdLoginInfo = ref<any>({});
  //상태
  const thirdLoginState = ref(false);
  //휴대폰번호 팝업창 바인딩
  const bindingPhoneModal = ref(false);
  //제3자 사용자 UUID
  const thirdUserUuid = ref('');
  //프롬프트 창
  const thirdConfirmShow = ref(false);
  //비밀번호 바인딩 팝업창
  const thirdPasswordShow = ref(false);
  //비밀번호 바인딩
  const thirdLoginPassword = ref('');
  //사용자 바인딩
  const thirdLoginUser = ref('');
  //로드 중
  const thirdCreateUserLoding = ref(false);
  //휴대폰 번호 바인딩
  const thirdPhone = ref('');
  //인증코드
  const thirdCaptcha = ref('');
  //제3자 로그인
  function onThirdLogin(source) {
    let url = `${glob.uploadUrl}/api/v1/kkumz/sys/thirdLogin/render/${source}`;
    window.open(
      url,
      `login ${source}`,
      'height=500, width=500, top=0, left=0, toolbar=no, menubar=no, scrollbars=no, resizable=no,location=n o, status=no'
    );
    thirdType.value = source;
    thirdLoginInfo.value = {};
    thirdLoginState.value = false;
    let receiveMessage = function (event) {
      let token = event.data;
      if (typeof token === 'string') {
        //문자열 형식인 경우 토큰 정보를 의미합니다.
        if (token === '로그인 실패') {
          createMessage.warning(token);
        } else if (token.includes('휴대폰 번호 바인딩')) {
          bindingPhoneModal.value = true;
          let strings = token.split(',');
          thirdUserUuid.value = strings[1];
        } else {
          doThirdLogin(token);
        }
      } else if (typeof token === 'object') {
        //개체 유형 설명 기존 계정을 바인딩할지 여부를 묻는 메시지가 표시되어야 함
        if (token['isObj'] === true) {
          thirdConfirmShow.value = true;
          thirdLoginInfo.value = { ...token };
        }
      } else {
        createMessage.warning('미확인 정보 전송');
      }
      window.removeEventListener('message', unref(receiveMessage),false);
    };
    window.addEventListener('message', receiveMessage, false);
  }
  // 토큰 기반 로그인
  function doThirdLogin(token) {
    if (unref(thirdLoginState) === false) {
      thirdLoginState.value = true;
      userStore.ThirdLogin({ token, thirdType: unref(thirdType) }).then((res) => {
        console.log('res====>doThirdLogin', res);
        if (res && res.userInfo) {
          notification.success({
            message: t('sys.login.loginSuccessTitle'),
            description: `${t('sys.login.loginSuccessDesc')}: ${res.userInfo.realname}`,
            duration: 3,
          });
        } else {
          requestFailed(res);
        }
      });
    }
  }

  function requestFailed(err) {
    notification.error({
      message: '로그인 실패',
      description: ((err.response || {}).data || {}).message || err.message || '요청에 오류가 발생했습니다. 나중에 다시 시도해 주세요.',
      duration: 4,
    });
  }
  // 기존 계정을 바인딩하면 비밀번호를 입력해야 합니다
  function thirdLoginUserBind() {
    thirdLoginPassword.value = '';
    thirdLoginUser.value = thirdLoginInfo.value.uuid;
    thirdConfirmShow.value = false;
    thirdPasswordShow.value = true;
  }
  //새 계정 만들기
  function thirdLoginUserCreate() {
    thirdCreateUserLoding.value = true;
    // 계정 이름 뒤에 임의의 숫자 두 개를 추가하세요.
    thirdLoginInfo.value.suffix = parseInt(Math.random() * 98 + 1);
    defHttp
      .post({ url: '/api/v1/kkumz/sys/third/user/create', params: { thirdLoginInfo: unref(thirdLoginInfo) } }, { isTransformResponse: false })
      .then((res) => {
        if (res.success) {
          let token = res.result;
          doThirdLogin(token);
          thirdConfirmShow.value = false;
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        thirdCreateUserLoding.value = false;
      });
  }
  // 비밀번호 확인
  function thirdLoginCheckPassword() {
    let params = Object.assign({}, unref(thirdLoginInfo), { password: unref(thirdLoginPassword) });
    defHttp.post({ url: '/api/v1/kkumz/sys/third/user/checkPassword', params }, { isTransformResponse: false }).then((res) => {
      if (res.success) {
        thirdLoginNoPassword();
        doThirdLogin(res.result);
      } else {
        createMessage.warning(res.message);
      }
    });
  }
  // 비밀번호 없음 작업 취소
  function thirdLoginNoPassword() {
    thirdPasswordShow.value = false;
    thirdLoginPassword.value = '';
    thirdLoginUser.value = '';
  }

  //실행 전 카운트다운 기능
  function sendCodeApi() {
    //return setThirdCaptcha({mobile:unref(thirdPhone)});
    return getCaptcha({ mobile: unref(thirdPhone), smsmode: '0' });
  }
  //휴대폰 번호를 바인딩하고 확인 버튼을 클릭하세요.
  function thirdHandleOk() {
    if (!unref(thirdPhone)) {
      cmsFailed('휴대폰번호를 입력해주세요');
    }
    if (!unref(thirdCaptcha)) {
      cmsFailed('인증코드를 입력해주세요');
    }
    let params = {
      mobile: unref(thirdPhone),
      captcha: unref(thirdCaptcha),
      thirdUserUuid: unref(thirdUserUuid),
    };
    defHttp.post({ url: '/api/v1/kkumz/sys/thirdLogin/bindingThirdPhone', params }, { isTransformResponse: false }).then((res) => {
      if (res.success) {
        bindingPhoneModal.value = false;
        doThirdLogin(res.result);
      } else {
        createMessage.warning(res.message);
      }
    });
  }
  function cmsFailed(err) {
    notification.error({
      message: '로그인 실패',
      description: err,
      duration: 4,
    });
    return;
  }
  //데이터 및 메서드 반환
  return {
    thirdPasswordShow,
    thirdLoginCheckPassword,
    thirdLoginNoPassword,
    thirdLoginPassword,
    thirdConfirmShow,
    thirdCreateUserLoding,
    thirdLoginUserCreate,
    thirdLoginUserBind,
    bindingPhoneModal,
    thirdHandleOk,
    thirdPhone,
    thirdCaptcha,
    onThirdLogin,
    sendCodeApi,
  };
}
