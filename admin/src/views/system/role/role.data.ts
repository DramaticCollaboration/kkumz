import { $t } from '/@/locales';
import { FormSchema } from '/@/components/Table';
import { isRoleExist } from './role.api';
export const columns = [
  {
    title: $t('appUi.k27cbee4e'),
    dataIndex: 'roleName',
    width: 100,
  },
  {
    title: $t('appUi.k859a72b1'),
    dataIndex: 'roleCode',
    width: 100,
  },
  {
    title: $t('appUi.k6f80446e'),
    dataIndex: 'createTime',
    width: 100,
  },
];
/**
 * 역할 사용자 열
 */
export const userColumns = [
  {
    title: $t('appUi.k8dd085d9'),
    dataIndex: 'username',
  },
  {
    title: $t('appUi.k2cc04d4c'),
    dataIndex: 'realname',
  },
  {
    title: $t('appUi.k2926977b'),
    dataIndex: 'status_dictText',
    width: 80,
  },
];
export const searchFormSchema: FormSchema[] = [
  {
    field: 'roleName',
    label: $t('appUi.k27cbee4e'),
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'roleCode',
    label: $t('appUi.k859a72b1'),
    component: 'Input',
    colProps: { span: 6 },
  },
];
/**
 * 역할 사용자 검색 Form
 */
export const searchUserFormSchema: FormSchema[] = [
  {
    field: 'username',
    label: $t('appUi.k8dd085d9'),
    component: 'Input',
    colProps: { span: 12 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: '',
    component: 'Input',
    show: false,
  },
  {
    field: 'roleName',
    label: $t('appUi.k27cbee4e'),
    required: true,
    component: 'Input',
  },
  {
    field: 'roleCode',
    label: $t('appUi.k859a72b1'),
    required: true,
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ values, model }) => {
      console.log('values:', values);
      return [
        {
          required: true,
          validator: (_, value) => {
            if (!value) {
              return Promise.reject($t('appUi.kf62ef49e'));
            }
            if (values) {
              return new Promise((resolve, reject) => {
                isRoleExist({ id: model.id, roleCode: value })
                  .then((res) => {
                    res.success ? resolve() : reject(res.message || $t('appUi.kae6c001b'));
                  })
                  .catch((err) => {
                    reject(err.message || $t('appUi.k7790958f'));
                  });
              });
            }
            return Promise.resolve();
          },
        },
      ];
    },
  },
  {
    label: $t('appUi.k75cffa41'),
    field: 'description',
    component: 'InputTextArea',
  },
];

export const formDescSchema = [
  {
    field: 'roleName',
    label: $t('appUi.k27cbee4e'),
  },
  {
    field: 'roleCode',
    label: $t('appUi.k859a72b1'),
  },
  {
    label: $t('appUi.k75cffa41'),
    field: 'description',
  },
];

export const roleIndexFormSchema: FormSchema[] = [
  {
    field: 'id',
    label: '',
    component: 'Input',
    show: false,
  },
  {
    label: $t('appUi.k859a72b1'),
    field: 'roleCode',
    component: 'Input',
    dynamicDisabled: true,
  },
  {
    label: $t('appUi.k48c2fd4c'),
    field: 'url',
    component: 'Input',
    required: true,
    helpMessage: $t('appUi.k1c98c731'),
  },
  {
    label: $t('appUi.k103b1b22'),
    field: 'component',
    component: 'Input',
    helpMessage: $t('appUi.k9ff84802'),
    componentProps: {
      placeholder: $t('appUi.k888b1cd4'),
    },
    required: true,
  },
  {
    field: 'route',
    label: $t('appUi.k06b03319'),
    helpMessage: $t('appUi.kd35ecb18'),
    component: 'Switch',
    defaultValue: true
  },
  {
    label: $t('appUi.ka0914b4d'),
    field: 'priority',
    component: 'InputNumber',
  },
  {
    label: $t('appUi.ke91e52ad'),
    field: 'status',
    component: 'JSwitch',
    componentProps: {
      options: ['1', '0'],
    },
  },
];
