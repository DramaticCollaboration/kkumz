import { $t } from '/@/locales';
import { Ref } from 'vue';
import { duplicateCheckDelay } from '/@/views/system/user/user.api';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { DescItem } from '/@/components/Description';
import { findTree } from '/@/utils/common/compUtils';

// 사용자 정보 열
export const userInfoColumns: BasicColumn[] = [
  {
    title: $t('appUi.k8dd085d9'),
    dataIndex: 'username',
    width: 150,
  },
  {
    title: $t('appUi.k2cc04d4c'),
    dataIndex: 'realname',
    width: 180,
  },
  {
    title: $t('appUi.ke5b530d6'),
    dataIndex: 'orgCode',
    width: 200,
  },
  {
    title: $t('appUi.k77737feb'),
    dataIndex: 'sex_dictText',
    width: 80,
  },
  {
    title: $t('appUi.k91032991'),
    dataIndex: 'phone',
    width: 120,
  },
];

// 사용자 정보 조회 조건 Form
export const userInfoSearchFormSchema: FormSchema[] = [
  {
    field: 'username',
    label: $t('appUi.k8dd085d9'),
    component: 'Input',
  },
];

// 부서 역할 열
export const departRoleColumns: BasicColumn[] = [
  {
    title: $t('appUi.k1f3baa15'),
    dataIndex: 'roleName',
    width: 100,
  },
  {
    title: $t('appUi.k2fd47290'),
    dataIndex: 'roleCode',
    width: 100,
  },
  {
    title: $t('appUi.ke5b530d6'),
    dataIndex: 'departId_dictText',
    width: 100,
  },
  {
    title: $t('appUi.k75cffa41'),
    dataIndex: 'description',
    width: 100,
  },
];

// 부서 역할 문의 조건 Form
export const departRoleSearchFormSchema: FormSchema[] = [
  {
    field: 'roleName',
    label: $t('appUi.k1f3baa15'),
    component: 'Input',
  },
];

// 부서 역할 팝업 Form
export const departRoleModalFormSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'roleName',
    label: $t('appUi.k1f3baa15'),
    component: 'Input',
    rules: [
      { required: true, message: $t('appUi.k174ba08d') },
      { min: 2, max: 30, message: $t('appUi.k061d4331'), trigger: 'blur' },
    ],
  },
  {
    field: 'roleCode',
    label: $t('appUi.k2fd47290'),
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model }) => {
      return [
        { required: true, message: $t('appUi.ka16ebc3c') },
        { min: 0, max: 64, message: $t('appUi.k9a3b11ed'), trigger: 'blur' },
        {
          validator: (_, value) => {
            if (/[\u4E00-\u9FA5]/g.test(value)) {
              return Promise.reject($t('appUi.keb1bb74a'));
            }
            return new Promise((resolve, reject) => {
              let params = {
                tableName: 'sys_depart_role',
                fieldName: 'role_code',
                fieldVal: value,
                dataId: model.id,
              };
              duplicateCheckDelay(params)
                .then((res) => {
                  res.success ? resolve() : reject(res.message || $t('appUi.kae6c001b'));
                })
                .catch((err) => {
                  reject(err.message || $t('appUi.k7790958f'));
                });
            });
          },
        },
      ];
    },
  },
  {
    field: 'description',
    label: $t('appUi.k84196436'),
    component: 'Input',
    rules: [{ min: 0, max: 126, message: $t('appUi.kb472863d'), trigger: 'blur' }],
  },
];

// 기본 정보 Form
export function useBaseInfoForm(treeData: Ref<any[]>) {
  const descItems: DescItem[] = [
    {
      field: 'departName',
      label: $t('appUi.k5b860f35'),
    },
    {
      field: 'parentId',
      label: $t('appUi.k6e257e6e'),
      render(val) {
        if (val) {
          let data = findTree(treeData.value, (item) => item.key == val);
          return data?.title ?? val;
        }
        return val;
      },
    },
    {
      field: 'orgCode',
      label: $t('appUi.kb5856d92'),
    },
    {
      field: 'orgCategory',
      label: $t('appUi.k1db0901e'),
      render(val) {
        if (val === '1') {
          return $t('appUi.kea129229');
        } else if (val === '2') {
          return $t('appUi.ke5b530d6');
        } else if (val === '3') {
          return $t('appUi.k3c37764a');
        }
        return val;
      },
    },
    {
      field: 'departOrder',
      label: $t('appUi.k7745cc5a'),
    },

    {
      field: 'mobile',
      label: $t('appUi.kfbf7b5bb'),
    },
    {
      field: 'address',
      label: $t('appUi.k117bab80'),
    },
    {
      field: 'memo',
      label: $t('appUi.k75cffa41'),
    },
  ];

  return { descItems };
}
