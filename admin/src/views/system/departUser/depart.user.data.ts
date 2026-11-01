import { Ref } from 'vue';
import { duplicateCheckDelay } from '/@/views/system/user/user.api';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { DescItem } from '/@/components/Description';
import { findTree } from '/@/utils/common/compUtils';

// 사용자 정보 열
export const userInfoColumns: BasicColumn[] = [
  {
    title: '아이디',
    dataIndex: 'username',
    width: 150,
  },
  {
    title: '사용자 이름',
    dataIndex: 'realname',
    width: 180,
  },
  {
    title: '부서',
    dataIndex: 'orgCode',
    width: 200,
  },
  {
    title: '성별',
    dataIndex: 'sex_dictText',
    width: 80,
  },
  {
    title: '전화',
    dataIndex: 'phone',
    width: 120,
  },
];

// 사용자 정보 조회 조건 Form
export const userInfoSearchFormSchema: FormSchema[] = [
  {
    field: 'username',
    label: '아이디',
    component: 'Input',
  },
];

// 부서 역할 열
export const departRoleColumns: BasicColumn[] = [
  {
    title: '부서 역할 이름',
    dataIndex: 'roleName',
    width: 100,
  },
  {
    title: '부서 역할 코드',
    dataIndex: 'roleCode',
    width: 100,
  },
  {
    title: '부서',
    dataIndex: 'departId_dictText',
    width: 100,
  },
  {
    title: '비고',
    dataIndex: 'description',
    width: 100,
  },
];

// 부서 역할 문의 조건 Form
export const departRoleSearchFormSchema: FormSchema[] = [
  {
    field: 'roleName',
    label: '부서 역할 이름',
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
    label: '부서 역할 이름',
    component: 'Input',
    rules: [
      { required: true, message: '부서 역할 이름은 필수입니다!' },
      { min: 2, max: 30, message: '2~30자 길이', trigger: 'blur' },
    ],
  },
  {
    field: 'roleCode',
    label: '부서 역할 코드',
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model }) => {
      return [
        { required: true, message: '부서 역할 코드는 필수입니다!' },
        { min: 0, max: 64, message: '길이는 64자를 초과할 수 없습니다.', trigger: 'blur' },
        {
          validator: (_, value) => {
            if (/[\u4E00-\u9FA5]/g.test(value)) {
              return Promise.reject('부서 역할 코드에는 한자를 입력할 수 없습니다!');
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
                  res.success ? resolve() : reject(res.message || '확인 실패');
                })
                .catch((err) => {
                  reject(err.message || '인증 실패');
                });
            });
          },
        },
      ];
    },
  },
  {
    field: 'description',
    label: '설명',
    component: 'Input',
    rules: [{ min: 0, max: 126, message: '길이는 126자를 초과할 수 없습니다.', trigger: 'blur' }],
  },
];

// 기본 정보 Form
export function useBaseInfoForm(treeData: Ref<any[]>) {
  const descItems: DescItem[] = [
    {
      field: 'departName',
      label: '조직 이름',
    },
    {
      field: 'parentId',
      label: '상급부서',
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
      label: '기관 코드',
    },
    {
      field: 'orgCategory',
      label: '기관 유형',
      render(val) {
        if (val === '1') {
          return '회사';
        } else if (val === '2') {
          return '부서';
        } else if (val === '3') {
          return '이메일';
        }
        return val;
      },
    },
    {
      field: 'departOrder',
      label: '정렬',
    },

    {
      field: 'mobile',
      label: '전화 번호',
    },
    {
      field: 'address',
      label: '주소',
    },
    {
      field: 'memo',
      label: '비고',
    },
  ];

  return { descItems };
}
