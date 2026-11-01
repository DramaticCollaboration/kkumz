export default {
  api: {
    operationFailed: '작업에 실패했습니다.',
    errorTip: '오류 안내',
    errorMessage: '작업 중 오류가 발생했습니다. 시스템 상태를 확인해주세요.',
    timeoutMessage: '로그인 세션이 만료되었습니다. 다시 로그인해주세요.',
    apiTimeoutMessage: '요청 시간이 초과되었습니다. 페이지를 새로고침한 후 다시 시도해주세요.',
    apiRequestFailed: '네트워크 요청에 실패했습니다. 잠시 후 다시 시도해주세요.',
    networkException: '네트워크 연결 오류',
    networkExceptionMsg: '네트워크 연결 상태를 확인해주세요.',

    errMsg401: '인증 권한이 없습니다 (토큰, 아이디, 비밀번호 확인 필요).',
    errMsg403: '접근이 거부되었습니다.',
    errMsg404: '요청하신 리소스를 찾을 수 없습니다.',
    errMsg405: '허용되지 않은 요청 방식입니다.',
    errMsg408: '요청 시간이 초과되었습니다.',
    errMsg500: '서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.',
    errMsg501: '지원되지 않는 요청입니다.',
    errMsg502: '네트워크 게이트웨이 오류입니다.',
    errMsg503: '서비스를 일시적으로 사용할 수 없습니다. 서버 점검 중일 수 있습니다.',
    errMsg504: '네트워크 게이트웨이 시간 초과입니다.',
    errMsg505: '지원되지 않는 HTTP 버전입니다.',
  },
  app: {
    logoutTip: '알림',
    logoutMessage: '시스템에서 로그아웃하시겠습니까?',
    menuLoading: '메뉴 로딩 중...',
  },
  errorLog: {
    tableTitle: '오류 로그 목록',
    tableColumnType: '유형',
    tableColumnDate: '일시',
    tableColumnFile: '파일',
    tableColumnMsg: '오류 메시지',
    tableColumnStackMsg: '스택 정보',

    tableActionDesc: '상세보기',

    modalTitle: '오류 상세 정보',

    fireVueError: 'Vue 오류 발생',
    fireResourceError: '리소스 오류 발생',
    fireAjaxError: 'Ajax 오류 발생',

    enableMessage: '`/src/settings/projectSetting.ts`에서 useErrorHandle=true 일 때만 활성화됩니다.',
  },
  exception: {
    backLogin: '로그인 화면으로',
    backHome: '홈으로 이동',
    subTitle403: '죄송합니다. 이 페이지에 접근할 권한이 없습니다.',
    subTitle404: '죄송합니다. 요청하신 페이지를 찾을 수 없습니다.',
    subTitle500: '죄송합니다. 서버에서 오류가 발생했습니다.',
    noDataTitle: '표시할 데이터가 없습니다.',
    networkErrorTitle: '네트워크 오류',
    networkErrorSubTitle: '네트워크 연결이 끊어졌습니다. 연결 상태를 확인해주세요.',
  },
  lock: {
    unlock: '잠금 해제',
    alert: '잠금 화면 비밀번호가 올바르지 않습니다.',
    backToLogin: '로그인 화면으로',
    entry: '시스템 진입',
    placeholder: '잠금 화면 비밀번호 입력',
  },
  login: {
    backSignIn: '로그인으로 돌아가기',
    mobileSignInFormTitle: '휴대폰 번호 로그인',
    qrSignInFormTitle: 'QR 코드 로그인',
    signInFormTitle: 'Kkumz 셀러 콘솔 로그인',
    signUpFormTitle: '위탁 셀러 입점 신청',
    forgetFormTitle: '비밀번호 재설정',

    signInTitle: '전자동 이커머스 위탁 셀러 플랫폼',
    signInDesc: '계정 정보를 입력하고 자율운영 콘솔에 접속하세요.',
    policy: '이용약관 및 개인정보 처리방침에 동의합니다.',
    scanSign: 'QR 코드를 스캔하여 로그인을 완료하세요.',

    loginButton: '로그인',
    registerButton: '회원가입',
    rememberMe: '로그인 상태 유지',
    forgetPassword: '비밀번호 찾기',
    otherSignIn: '간편 로그인',

    // notify
    loginSuccessTitle: '로그인 성공',
    loginSuccessDesc: '환영합니다',

    // placeholder
    accountPlaceholder: '아이디 또는 이메일을 입력하세요',
    passwordPlaceholder: '비밀번호를 입력하세요',
    smsPlaceholder: '인증번호를 입력하세요',
    mobilePlaceholder: '휴대폰 번호를 입력하세요',
    mobileCorrectPlaceholder: '올바른 휴대폰 번호를 입력하세요',
    policyPlaceholder: '약관 동의 후 가입 가능합니다',
    diffPwd: '비밀번호가 일치하지 않습니다',

    userName: '아이디 또는 이메일',
    password: '비밀번호',
    inputCode: '보안 인증코드',
    confirmPassword: '비밀번호 확인',
    email: '이메일 주소',
    smsCode: 'SMS 인증번호',
    mobile: '휴대폰 번호',

    // 비밀번호 재설정 페이지
    authentication: '본인 인증',
    resetLoginPassword: '로그인 비밀번호 재설정',
    resetSuccess: '비밀번호가 성공적으로 변경되었습니다',
    nextStep: '다음 단계',
    goToLogin: '로그인하기',
  },
};
