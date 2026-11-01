import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { getAllRolesList } from './user.api';
import { rules } from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
export const columns: BasicColumn[] = [
  {
    title: '아이디',
    dataIndex: 'username',
    width: 120,
  },
  {
    title: '사용자 이름',
    dataIndex: 'realname',
    width: 100,
  },
  {
    title: '아바타',
    dataIndex: 'avatar',
    width: 120,
    customRender: render.renderAvatar,
  },
  {
    title: '성별',
    dataIndex: 'sex',
    width: 80,
    sorter: true,
    customRender: ({ text }) => {
      return render.renderDict(text, 'sex');
    },
  },
  {
    title: '생일',
    dataIndex: 'birthday',
    width: 100,
  },
  {
    title: '전화 번호',
    dataIndex: 'phone',
    width: 100,
  },
  {
    title: '부서',
    width: 150,
    dataIndex: 'orgCodeTxt',
  },
  {
    title: '담당부서',
    width: 150,
    dataIndex: 'departIds_dictText',
  },
  {
    title: '상태',
    dataIndex: 'status_dictText',
    width: 80,
  },
];

export const recycleColumns: BasicColumn[] = [
  {
    title: '아이디',
    dataIndex: 'username',
    width: 100,
  },
  {
    title: '사용자 이름',
    dataIndex: 'realname',
    width: 100,
  },
  {
    title: '아바타',
    dataIndex: 'avatar',
    width: 80,
    customRender: render.renderAvatar,
  },
  {
    title: '성별',
    dataIndex: 'sex',
    width: 80,
    sorter: true,
    customRender: ({ text }) => {
      return render.renderDict(text, 'sex');
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: '계정',
    field: 'username',
    component: 'JInput',
    //colProps: { span: 6 },
  },
  {
    label: '이름',
    field: 'realname',
    component: 'JInput',
   //colProps: { span: 6 },
  },
  {
    label: '성별',
    field: 'sex',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: '성별을 선택해주세요',
      stringToNumber: true,
    },
    //colProps: { span: 6 },
  },
  {
    label: '전화 번호',
    field: 'phone',
    component: 'Input',
    //colProps: { span: 6 },
  },
  {
    label: '사용자 상태',
    field: 'status',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'user_status',
      placeholder: '상태를 선택해주세요',
      stringToNumber: true,
    },
   //colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: '아이디',
    field: 'username',
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model, schema }) => rules.duplicateCheckRule('sys_user', 'username', model, schema, true),
  },
  {
    label: '비밀번호',
    field: 'password',
    component: 'StrengthMeter',
    componentProps:{
      autocomplete: 'new-password',
    },
    rules: [
      {
        required: true,
        message: '비밀번호를 입력해주세요',
      },
      {
        pattern: /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[~!@#$%^&*()_+`\-={}:";'<>?,./]).{8,}$/,
        message: '비밀번호는 8자리, 대문자, 소문자, 특수기호로 구성됩니다!',
      },
    ],
  },
  {
    label: '비밀번호 확인',
    field: 'confirmPassword',
    component: 'InputPassword',
    dynamicRules: ({ values }) => rules.confirmPassword(values, true),
  },
  {
    label: '사용자 이름',
    field: 'realname',
    required: true,
    component: 'Input',
  },
  {
    label: '역할',
    field: 'selectedroles',
    component: 'ApiSelect',
    componentProps: {
      mode: 'multiple',
      api: getAllRolesList,
      labelField: 'roleName',
      valueField: 'id',
      immediate: false,
    },
  },
  {
    label: '부서',
    field: 'selecteddeparts',
    component: 'JSelectDept',
    componentProps: ({ formActionType, formModel }) => {
      return {
        sync: false,
        checkStrictly: true,
        defaultExpandLevel: 2,

        onSelect: (options, values) => {
          const { updateSchema } = formActionType;
          //해당 부서를 수정한 후 해당 부서의 드롭다운 박스에서 데이터를 업데이트합니다.
          updateSchema([
            {
              field: 'departIds',
              componentProps: { options },
            },
          ]);
          if(!values){
            formModel.departIds = [];
            return;
          }
          //소속부서의 데이터를 수정 후 담당부서의 데이터를 업데이트 합니다.
          formModel.departIds && (formModel.departIds = formModel.departIds.filter((item) => values.value.indexOf(item) > -1));
        },
      };
    },
  },
  {
    label: '등급',
    field: 'userIdentity',
    component: 'RadioGroup',
    defaultValue: 1,
    componentProps: ({ formModel }) => {
      return {
        options: [
          { label: '사용자', value: 1, key: '1' },
          { label: '관리자', value: 2, key: '2' },
        ],
        onChange: () => {
          formModel.userIdentity == 1 && (formModel.departIds = []);
        },
      };
    },
  },
  {
    label: '담당부서',
    field: 'departIds',
    component: 'Select',
    componentProps: {
      mode: 'multiple',
    },
    ifShow: ({ values }) => values.userIdentity == 2,
  },
  {
    label: '생일',
    field: 'birthday',
    component: 'DatePicker',
  },
  {
    label: '성별',
    field: 'sex',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: '성별을 선택해주세요',
      stringToNumber: true,
    },
  },
  {
    label: '이메일',
    field: 'email',
    component: 'Input',
    required: true,
    dynamicRules: ({ model, schema }) => {
      return [
        { ...rules.duplicateCheckRule('sys_user', 'email', model, schema, true)[0], trigger: 'blur' },
        { ...rules.rule('email', false)[0], trigger: 'blur' },
      ];
    },
  },
  {
    label: '전화 번호',
    field: 'phone',
    component: 'Input',
    required: true,
    dynamicRules: ({ model, schema }) => {
      return [
        { ...rules.duplicateCheckRule('sys_user', 'phone', model, schema, true)[0], trigger: 'blur' },
        { pattern: /^010([-\s]?)([0-9]{4})([-\s]?)([0-9]{4})$/, message: '휴대폰 번호 형식이 잘못되었습니다.', trigger: 'blur' },
      ];
    },
  },
  {
    label: '유선전화',
    field: 'telephone',
    component: 'Input',
    rules: [{ pattern: /^0\d{2,3}-[1-9]\d{6,7}$/, message: '정확한 유선전화번호를 입력해주세요' }],
  },
];

export const formPasswordSchema: FormSchema[] = [
  {
    label: '아이디',
    field: 'username',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: '비밀번호',
    field: 'password',
    component: 'StrengthMeter',
    componentProps: {
      placeholder: '비밀번호를 입력해주세요',
    },
    rules: [
      {
        required: true,
        message: '비밀번호를 입력해주세요',
      },
      {
        pattern: /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[~!@#$%^&*()_+`\-={}:";'<>?,./]).{8,}$/,
        message: '비밀번호는 8자리, 대문자, 소문자, 특수기호로 구성됩니다!',
      },
    ],
  },
  {
    label: '비밀번호 확인',
    field: 'confirmPassword',
    component: 'InputPassword',
    dynamicRules: ({ values }) => rules.confirmPassword(values, true),
  },
];

export const formAgentSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'userName',
    label: '사용자 이름',
    component: 'Input',
    componentProps: {
      readOnly: true,
      allowClear: false,
    },
  },
  {
    field: 'agentUserName',
    label: '상담원 사용자 이름',
    required: true,
    component: 'JSelectUser',
    componentProps: {
      rowKey: 'username',
      labelKey: 'realname',
      maxSelectCount: 10,
    },
  },
  {
    field: 'startTime',
    label: '에이전트 시작 시간',
    component: 'DatePicker',
    required: true,
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      placeholder: '상담원 시작 시간을 선택하세요.',
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'endTime',
    label: '에이전트 종료 시간',
    component: 'DatePicker',
    required: true,
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      placeholder: '상담원 종료 시간을 선택하세요.',
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'status',
    label: '상태',
    component: 'JDictSelectTag',
    defaultValue: '1',
    componentProps: {
      dictCode: 'valid_status',
      type: 'radioButton',
    },
  },
];

export const formQuitAgentSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'userName',
    label: '사용자 이름',
    component: 'Input',
    componentProps: {
      readOnly: true,
      allowClear: false,
    },
  },
  {
    field: 'agentUserName',
    label: '인계인력',
    //required: true,
    component: 'JSelectUser',
    componentProps: {
      rowKey: 'username',
      labelKey: 'realname',
      maxSelectCount: 1,
    },
  },
  {
    field: 'startTime',
    label: '핸드오버 시작 시간',
    component: 'DatePicker',
    //required: true,
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      placeholder: '핸드오버 시작 시간을 선택하세요.',
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'endTime',
    label: '핸드오버 종료 시간',
    component: 'DatePicker',
    //required: true,
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      placeholder: '인계 종료 시간을 선택해주세요.',
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'status',
    label: '상태',
    component: 'JDictSelectTag',
    defaultValue: '1',
    componentProps: {
      dictCode: 'valid_status',
      type: 'radioButton',
    },
  },
];
