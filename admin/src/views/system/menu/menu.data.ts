import { $t } from '/@/locales';
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
    title: $t('appUi.k43f3ee13'),
    dataIndex: 'name',
    width: 200,
    align: 'left',
  },
  {
    title: $t('appUi.k1361e7d1'),
    dataIndex: 'menuType',
    width: 150,
    customRender: ({ text }) => {
      return render.renderDict(text, 'menu_type');
    },
  },
  {
    title: $t('appUi.kba29904c'),
    dataIndex: 'icon',
    width: 50,
    customRender: ({ record }) => {
      return h(Icon, { icon: record.icon });
    },
  },
  {
    title: $t('appUi.k7e806e3b'),
    dataIndex: 'component',
    align: 'left',
    width: 150,
  },
  {
    title: $t('appUi.kbae4002d'),
    dataIndex: 'url',
    align: 'left',
    width: 150,
  },
  {
    title: $t('appUi.k7745cc5a'),
    dataIndex: 'sortNo',
    width: 50,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: $t('appUi.k43f3ee13'),
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
    label: $t('appUi.k1361e7d1'),
    component: 'RadioButtonGroup',
    defaultValue: 0,
    componentProps: ({ formActionType, formModel }) => {
      return {
        options: [
          { label: $t('appUi.k076925c5'), value: 0 },
          { label: $t('appUi.k7bab3e7a'), value: 1 },
          { label: $t('appUi.k8b5208c5'), value: 2 },
        ],
        onChange: (e) => {
          const { updateSchema, clearValidate } = formActionType;
          const label = isButton(e) ? $t('appUi.k8b5208c5') : $t('appUi.k43f3ee13');
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
    label: $t('appUi.k43f3ee13'),
    component: 'Input',
    required: true,
  },
  {
    field: 'nameI18n',
    label: $t('common.i18nName'),
    component: 'I18nInput',
  },
  {
    field: 'parentId',
    label: $t('appUi.k0821aeff'),
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
    label: $t('appUi.kbae4002d'),
    component: 'Input',
    required: true,
    ifShow: ({ values }) => !(values.component === ComponentTypes.IFrame && values.internalOrExternal),
    dynamicRules: ({ model, schema, values }) => {
      return checkPermDuplication(model, schema, values.menuType !== 2 ? true : false);
    },
  },
  {
    field: 'component',
    label: $t('appUi.k933d33cf'),
    component: 'Input',
    componentProps: {
      placeholder: $t('appUi.k888b1cd4'),
    },
    defaultValue: 'layouts/default/index',
    required: true,
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'componentName',
    label: $t('appUi.kaef0e6a1'),
    component: 'Input',
    componentProps: {
      placeholder: $t('appUi.ke9065d29'),
    },
    helpMessage: $t('appUi.k3d5de949'),
    defaultValue: '',
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'frameSrc',
    label: $t('appUi.k6824a0d5'),
    component: 'Input',
    rules: [
      { required: true, message: $t('appUi.k56481df1') },
      { type: 'url', message: $t('appUi.k701f4996') },
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
    label: $t('appUi.kfda3a010'),
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
    label: $t('appUi.ked8e2347'),
    component: 'RadioGroup',
    defaultValue: '1',
    helpMessage: [$t('appUi.k4c4c2b48'), $t('appUi.k75592334')],
    componentProps: {
      options: [
        { label: $t('appUi.k998f15a1'), value: '1' },
        { label: $t('appUi.k40f71af6'), value: '2' },
      ],
    },
    ifShow: ({ values }) => isButton(values.menuType),
  },
  {
    field: 'status',
    label: $t('appUi.k2926977b'),
    component: 'RadioGroup',
    defaultValue: '1',
    componentProps: {
      options: [
        { label: $t('appUi.kbbf831ad'), value: '1' },
        { label: $t('appUi.k8ff58636'), value: '0' },
      ],
    },
    ifShow: ({ values }) => isButton(values.menuType),
  },
  {
    field: 'icon',
    label: $t('appUi.k3cca5135'),
    component: 'IconPicker',
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'sortNo',
    label: $t('appUi.k7745cc5a'),
    component: 'InputNumber',
    defaultValue: 1,
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'route',
    label: $t('appUi.k06b03319'),
    component: 'Switch',
    defaultValue: true,
    componentProps: {
      checkedChildren: $t('appUi.ka842629a'),
      unCheckedChildren: $t('appUi.kccf0c3a2'),
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'hidden',
    label: $t('appUi.k093ff103'),
    component: 'Switch',
    defaultValue: 0,
    componentProps: {
      checkedChildren: $t('appUi.ka842629a'),
      unCheckedChildren: $t('appUi.kccf0c3a2'),
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'hideTab',
    label: $t('appUi.k49249c72'),
    component: 'Switch',
    defaultValue: 0,
    componentProps: {
      checkedChildren: $t('appUi.ka842629a'),
      unCheckedChildren: $t('appUi.kccf0c3a2'),
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'keepAlive',
    label: 'Keep Alive',
    component: 'Switch',
    defaultValue: false,
    componentProps: {
      checkedChildren: $t('appUi.ka842629a'),
      unCheckedChildren: $t('appUi.kccf0c3a2'),
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'alwaysShow',
    label: $t('appUi.k60d6f146'),
    component: 'Switch',
    defaultValue: false,
    componentProps: {
      checkedChildren: $t('appUi.ka842629a'),
      unCheckedChildren: $t('appUi.kccf0c3a2'),
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'internalOrExternal',
    label: $t('appUi.ka87938c2'),
    component: 'Switch',
    defaultValue: false,
    componentProps: {
      checkedChildren: $t('appUi.k1e269063'),
      unCheckedChildren: $t('appUi.kb57e1c0e'),
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
];

export const dataRuleColumns: BasicColumn[] = [
  {
    title: $t('appUi.k6d2c1870'),
    dataIndex: 'ruleName',
    width: 150,
  },
  {
    title: $t('appUi.k179705a7'),
    dataIndex: 'ruleColumn',
    width: 100,
  },
  {
    title: $t('appUi.k7b58f09d'),
    dataIndex: 'ruleValue',
    width: 100,
  },
];

export const dataRuleSearchFormSchema: FormSchema[] = [
  {
    field: 'ruleName',
    label: $t('appUi.k6d2c1870'),
    component: 'Input',
    // colProps: { span: 6 },
  },
  {
    field: 'ruleValue',
    label: $t('appUi.k7b58f09d'),
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
    label: $t('appUi.k6d2c1870'),
    component: 'Input',
    required: true,
  },
  {
    field: 'ruleColumn',
    label: $t('appUi.k179705a7'),
    component: 'Input',
    ifShow: ({ values }) => {
      const ruleConditions = Array.isArray(values.ruleConditions) ? values.ruleConditions[0] : values.ruleConditions;
      return ruleConditions !== 'USE_SQL_RULES';
    },
  },
  {
    field: 'ruleConditions',
    label: $t('appUi.k29f59547'),
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
    label: $t('appUi.k7b58f09d'),
    component: 'Input',
    required: true,
  },
  {
    field: 'status',
    label: $t('appUi.k2926977b'),
    component: 'RadioButtonGroup',
    defaultValue: '1',
    componentProps: {
      options: [
        { label: $t('appUi.k8ff58636'), value: '0' },
        { label: $t('appUi.kbbf831ad'), value: '1' },
      ],
    },
  },
];
