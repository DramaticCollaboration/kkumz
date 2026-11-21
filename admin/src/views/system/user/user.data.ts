import { $t } from '/@/locales';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { getAllRolesList } from './user.api';
import { rules } from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
export const columns: BasicColumn[] = [
  {
    title: $t('appUi.k8dd085d9'),
    dataIndex: 'username',
    width: 120,
  },
  {
    title: $t('appUi.k2cc04d4c'),
    dataIndex: 'realname',
    width: 100,
  },
  {
    title: $t('appUi.k27eeb981'),
    dataIndex: 'avatar',
    width: 120,
    customRender: render.renderAvatar,
  },
  {
    title: $t('appUi.k77737feb'),
    dataIndex: 'sex',
    width: 80,
    sorter: true,
    customRender: ({ text }) => {
      return render.renderDict(text, 'sex');
    },
  },
  {
    title: $t('appUi.k10f776d3'),
    dataIndex: 'birthday',
    width: 100,
  },
  {
    title: $t('appUi.kfbf7b5bb'),
    dataIndex: 'phone',
    width: 100,
  },
  {
    title: $t('appUi.ke5b530d6'),
    width: 150,
    dataIndex: 'orgCodeTxt',
  },
  {
    title: $t('appUi.k2eed4a6d'),
    width: 150,
    dataIndex: 'departIds_dictText',
  },
  {
    title: $t('appUi.k2926977b'),
    dataIndex: 'status_dictText',
    width: 80,
  },
];

export const recycleColumns: BasicColumn[] = [
  {
    title: $t('appUi.k8dd085d9'),
    dataIndex: 'username',
    width: 100,
  },
  {
    title: $t('appUi.k2cc04d4c'),
    dataIndex: 'realname',
    width: 100,
  },
  {
    title: $t('appUi.k27eeb981'),
    dataIndex: 'avatar',
    width: 80,
    customRender: render.renderAvatar,
  },
  {
    title: $t('appUi.k77737feb'),
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
    label: $t('appUi.k615212b2'),
    field: 'username',
    component: 'JInput',
    alwaysShow: true,
    //colProps: { span: 6 },
  },
  {
    label: $t('appUi.k9aa18e50'),
    field: 'realname',
    component: 'JInput',
    alwaysShow: true,
   //colProps: { span: 6 },
  },
  {
    label: $t('appUi.k77737feb'),
    field: 'sex',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: $t('appUi.k59337943'),
      stringToNumber: true,
    },
    //colProps: { span: 6 },
  },
  {
    label: $t('appUi.kfbf7b5bb'),
    field: 'phone',
    component: 'Input',
    //colProps: { span: 6 },
  },
  {
    label: $t('appUi.k45cd44a3'),
    field: 'status',
    component: 'JDictSelectTag',
    alwaysShow: true,
    componentProps: {
      dictCode: 'user_status',
      placeholder: $t('appUi.k2cb67eda'),
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
    label: $t('appUi.k8dd085d9'),
    field: 'username',
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model, schema }) => rules.duplicateCheckRule('sys_user', 'username', model, schema, true),
  },
  {
    label: $t('appUi.k81973897'),
    field: 'password',
    component: 'StrengthMeter',
    componentProps:{
      autocomplete: 'new-password',
    },
    rules: [
      {
        required: true,
        message: $t('appUi.kf893b7b0'),
      },
      {
        pattern: /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[~!@#$%^&*()_+`\-={}:";'<>?,./]).{8,}$/,
        message: $t('appUi.k1e15995d'),
      },
    ],
  },
  {
    label: $t('appUi.k768b58d6'),
    field: 'confirmPassword',
    component: 'InputPassword',
    dynamicRules: ({ values }) => rules.confirmPassword(values, true),
  },
  {
    label: $t('appUi.k2cc04d4c'),
    field: 'realname',
    required: true,
    component: 'Input',
  },
  {
    label: $t('appUi.kf3f4087e'),
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
    label: $t('appUi.ke5b530d6'),
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
    label: $t('appUi.k89dbf513'),
    field: 'userIdentity',
    component: 'RadioGroup',
    defaultValue: 1,
    componentProps: ({ formModel }) => {
      return {
        options: [
          { label: $t('appUi.k5c50d9e5'), value: 1, key: '1' },
          { label: $t('appUi.kec873cb1'), value: 2, key: '2' },
        ],
        onChange: () => {
          formModel.userIdentity == 1 && (formModel.departIds = []);
        },
      };
    },
  },
  {
    label: $t('appUi.k2eed4a6d'),
    field: 'departIds',
    component: 'Select',
    componentProps: {
      mode: 'multiple',
    },
    ifShow: ({ values }) => values.userIdentity == 2,
  },
  {
    label: $t('appUi.k10f776d3'),
    field: 'birthday',
    component: 'DatePicker',
  },
  {
    label: $t('appUi.k77737feb'),
    field: 'sex',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: $t('appUi.k59337943'),
      stringToNumber: true,
    },
  },
  {
    label: $t('appUi.k3c37764a'),
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
    label: $t('appUi.kfbf7b5bb'),
    field: 'phone',
    component: 'Input',
    required: true,
    dynamicRules: ({ model, schema }) => {
      return [
        { ...rules.duplicateCheckRule('sys_user', 'phone', model, schema, true)[0], trigger: 'blur' },
        { pattern: /^010([-\s]?)([0-9]{4})([-\s]?)([0-9]{4})$/, message: $t('appUi.ked5ae196'), trigger: 'blur' },
      ];
    },
  },
  {
    label: $t('appUi.ke38e65af'),
    field: 'telephone',
    component: 'Input',
    rules: [{ pattern: /^0\d{2,3}-[1-9]\d{6,7}$/, message: $t('appUi.kdf787538') }],
  },
];

export const formPasswordSchema: FormSchema[] = [
  {
    label: $t('appUi.k8dd085d9'),
    field: 'username',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: $t('appUi.k81973897'),
    field: 'password',
    component: 'StrengthMeter',
    componentProps: {
      placeholder: $t('appUi.kf893b7b0'),
    },
    rules: [
      {
        required: true,
        message: $t('appUi.kf893b7b0'),
      },
      {
        pattern: /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[~!@#$%^&*()_+`\-={}:";'<>?,./]).{8,}$/,
        message: $t('appUi.k1e15995d'),
      },
    ],
  },
  {
    label: $t('appUi.k768b58d6'),
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
    label: $t('appUi.k2cc04d4c'),
    component: 'Input',
    componentProps: {
      readOnly: true,
      allowClear: false,
    },
  },
  {
    field: 'agentUserName',
    label: $t('appUi.k82a2e5df'),
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
    label: $t('appUi.kb4dd997c'),
    component: 'DatePicker',
    required: true,
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      placeholder: $t('appUi.k21113295'),
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'endTime',
    label: $t('appUi.kac8e3945'),
    component: 'DatePicker',
    required: true,
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      placeholder: $t('appUi.k3ddc481e'),
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'status',
    label: $t('appUi.k2926977b'),
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
    label: $t('appUi.k2cc04d4c'),
    component: 'Input',
    componentProps: {
      readOnly: true,
      allowClear: false,
    },
  },
  {
    field: 'agentUserName',
    label: $t('appUi.k39a8a1e1'),
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
    label: $t('appUi.k9718b125'),
    component: 'DatePicker',
    //required: true,
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      placeholder: $t('appUi.k40bf1a2e'),
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'endTime',
    label: $t('appUi.kbf78bf27'),
    component: 'DatePicker',
    //required: true,
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      placeholder: $t('appUi.k3317c34f'),
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'status',
    label: $t('appUi.k2926977b'),
    component: 'JDictSelectTag',
    defaultValue: '1',
    componentProps: {
      dictCode: 'valid_status',
      type: 'radioButton',
    },
  },
];
