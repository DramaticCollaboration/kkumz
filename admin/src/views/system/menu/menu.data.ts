import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Icon } from '/@/components/Icon';
import { ajaxGetDictItems, checkPermDuplication } from './menu.api';
import { render } from '/@/utils/common/renderUtils';

const isDir = (type) => type === 0;
const isMenu = (type) => type === 1;
const isButton = (type) => type === 2;

// 선택적 컴포넌트 유형 정의
export enum ComponentTypes {
  Default = 'layouts/default/index',
  IFrame = 'sys/iframe/FrameBlank',
}

export const columns: BasicColumn[] = [
  {
    title: '메뉴명',
    dataIndex: 'name',
    width: 200,
    align: 'left',
  },
  {
    title: '메뉴 종류',
    dataIndex: 'menuType',
    width: 150,
    customRender: ({ text }) => {
      return render.renderDict(text, 'menu_type');
    },
  },
  {
    title: '아이콘',
    dataIndex: 'icon',
    width: 50,
    customRender: ({ record }) => {
      return h(Icon, { icon: record.icon });
    },
  },
  {
    title: '컴포넌트',
    dataIndex: 'component',
    align: 'left',
    width: 150,
  },
  {
    title: '경로',
    dataIndex: 'url',
    align: 'left',
    width: 150,
  },
  {
    title: '정렬',
    dataIndex: 'sortNo',
    width: 50,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: '메뉴명',
    component: 'Input',
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'menuType',
    label: '메뉴 종류',
    component: 'RadioButtonGroup',
    defaultValue: 0,
    componentProps: ({ formActionType, formModel }) => {
      return {
        options: [
          { label: '메뉴', value: 0 },
          { label: '하위 메뉴', value: 1 },
          { label: '버튼/권한', value: 2 },
        ],
        onChange: (e) => {
          const { updateSchema, clearValidate } = formActionType;
          const label = isButton(e) ? '버튼/권한' : '메뉴명';
          //체크섬 지우기
          clearValidate();
          updateSchema([
            {
              field: 'name',
              label: label,
            },
            {
              field: 'url',
              required: !isButton(e),
            },
          ]);
          if (isMenu(e) && !formModel.id && (formModel.component == 'layouts/default/index' || formModel.component == 'layouts/RouteView')) {
            formModel.component = '';
          }
        },
      };
    },
  },
  {
    field: 'name',
    label: '메뉴명',
    component: 'Input',
    required: true,
  },
  {
    field: 'parentId',
    label: '상위 메뉴',
    component: 'TreeSelect',
    required: true,
    componentProps: {
      fieldNames: {
        label: 'name',
        key: 'id',
        value: 'id',
      },
      dropdownStyle: {
        maxHeight: '50vh',
      },
      getPopupContainer: (node) => node?.parentNode,
    },
    ifShow: ({ values }) => !isDir(values.menuType),
  },
  {
    field: 'url',
    label: '경로',
    component: 'Input',
    required: true,
    ifShow: ({ values }) => !(values.component === ComponentTypes.IFrame && values.internalOrExternal),
    dynamicRules: ({ model, schema, values }) => {
      return checkPermDuplication(model, schema, values.menuType !== 2 ? true : false);
    },
  },
  {
    field: 'component',
    label: '프런트엔드 컴포넌트',
    component: 'Input',
    componentProps: {
      placeholder: '프런트엔드 구성요소를 입력하세요.',
    },
    defaultValue: 'layouts/default/index',
    required: true,
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'componentName',
    label: '구성요소 이름',
    component: 'Input',
    componentProps: {
      placeholder: '구성요소 이름을 입력하세요.',
    },
    helpMessage: 'Vue 컴포넌트 name 속성과 일치해야 하며(경로 캐싱용), 비워두면 자동 생성됩니다.',
    defaultValue: '',
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'frameSrc',
    label: 'Iframe 주소',
    component: 'Input',
    rules: [
      { required: true, message: 'Iframe 주소를 입력하세요.' },
      { type: 'url', message: '올바른 URL 주소를 입력하세요.' },
    ],
    ifShow: ({ values }) => !isButton(values.menuType) && values.component === ComponentTypes.IFrame,
  },
  {
    field: 'redirect',
    label: 'Redirect',
    component: 'Input',
    ifShow: ({ values }) => isDir(values.menuType),
  },
  {
    field: 'perms',
    label: '권한 아이디',
    component: 'Input',
    ifShow: ({ values }) => isButton(values.menuType),
    // dynamicRules: ({ model }) => {
    //   return [
    //     {
    //       required: false,
    //       validator: (_, value) => {
    //         return new Promise((resolve, reject) => {
    //           let params = {
    //             tableName: 'sys_permission',
    //             fieldName: 'perms',
    //             fieldVal: value,
    //             dataId: model.id,
    //           };
    //           duplicateCheck(params)
    //             .then((res) => {
    //               res.success ? resolve() : reject(res.message || '확인 실패');
    //             })
    //             .catch((err) => {
    //               reject(err.message || '확인 실패');
    //             });
    //         });
    //       },
    //     },
    //   ];
    // },
  },
  {
    field: 'permsType',
    label: '권한 정책',
    component: 'RadioGroup',
    defaultValue: '1',
    helpMessage: ['보기/ 접근 가능 (인증 후 보기/ 접근 가능)', '편집 가능 (권한 부여되지 않은 경우 사용하지 않음)'],
    componentProps: {
      options: [
        { label: '조회 가능', value: '1' },
        { label: '편집 가능', value: '2' },
      ],
    },
    ifShow: ({ values }) => isButton(values.menuType),
  },
  {
    field: 'status',
    label: '상태',
    component: 'RadioGroup',
    defaultValue: '1',
    componentProps: {
      options: [
        { label: '활성화', value: '1' },
        { label: '비활성화', value: '0' },
      ],
    },
    ifShow: ({ values }) => isButton(values.menuType),
  },
  {
    field: 'icon',
    label: '메뉴 아이콘',
    component: 'IconPicker',
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'sortNo',
    label: '정렬',
    component: 'InputNumber',
    defaultValue: 1,
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'route',
    label: '라우팅',
    component: 'Switch',
    defaultValue: true,
    componentProps: {
      checkedChildren: '예',
      unCheckedChildren: '아니요',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'hidden',
    label: '숨김',
    component: 'Switch',
    defaultValue: 0,
    componentProps: {
      checkedChildren: '예',
      unCheckedChildren: '아니요',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'hideTab',
    label: '탭 숨기기',
    component: 'Switch',
    defaultValue: 0,
    componentProps: {
      checkedChildren: '예',
      unCheckedChildren: '아니요',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'keepAlive',
    label: 'Keep Alive',
    component: 'Switch',
    defaultValue: false,
    componentProps: {
      checkedChildren: '예',
      unCheckedChildren: '아니요',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'alwaysShow',
    label: '항상 보이기',
    component: 'Switch',
    defaultValue: false,
    componentProps: {
      checkedChildren: '예',
      unCheckedChildren: '아니요',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'internalOrExternal',
    label: '외부링크',
    component: 'Switch',
    defaultValue: false,
    componentProps: {
      checkedChildren: '링크',
      unCheckedChildren: '컨포넌트',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
];

export const dataRuleColumns: BasicColumn[] = [
  {
    title: '규칙 이름',
    dataIndex: 'ruleName',
    width: 150,
  },
  {
    title: '규칙 필드',
    dataIndex: 'ruleColumn',
    width: 100,
  },
  {
    title: '규칙 값',
    dataIndex: 'ruleValue',
    width: 100,
  },
];

export const dataRuleSearchFormSchema: FormSchema[] = [
  {
    field: 'ruleName',
    label: '규칙 이름',
    component: 'Input',
    // colProps: { span: 6 },
  },
  {
    field: 'ruleValue',
    label: '규칙 값',
    component: 'Input',
    // colProps: { span: 6 },
  },
];

export const dataRuleFormSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'ruleName',
    label: '규칙 이름',
    component: 'Input',
    required: true,
  },
  {
    field: 'ruleColumn',
    label: '규칙 필드',
    component: 'Input',
    ifShow: ({ values }) => {
      const ruleConditions = Array.isArray(values.ruleConditions) ? values.ruleConditions[0] : values.ruleConditions;
      return ruleConditions !== 'USE_SQL_RULES';
    },
  },
  {
    field: 'ruleConditions',
    label: '조건부 규칙',
    required: true,
    component: 'ApiSelect',
    componentProps: {
      api: ajaxGetDictItems,
      params: { code: 'rule_conditions' },
      labelField: 'text',
      valueField: 'value',
      getPopupContainer: (node) => document.body,
    },
  },
  {
    field: 'ruleValue',
    label: '규칙 값',
    component: 'Input',
    required: true,
  },
  {
    field: 'status',
    label: '상태',
    component: 'RadioButtonGroup',
    defaultValue: '1',
    componentProps: {
      options: [
        { label: '비활성화', value: '0' },
        { label: '활성화', value: '1' },
      ],
    },
  },
];
