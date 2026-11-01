import { FormSchema } from '/@/components/Table';
import { isRoleExist } from './role.api';
export const columns = [
  {
    title: '역할 이름',
    dataIndex: 'roleName',
    width: 100,
  },
  {
    title: '역할 코드',
    dataIndex: 'roleCode',
    width: 100,
  },
  {
    title: '등록일',
    dataIndex: 'createTime',
    width: 100,
  },
];
/**
 * 역할 사용자 열
 */
export const userColumns = [
  {
    title: '아이디',
    dataIndex: 'username',
  },
  {
    title: '사용자 이름',
    dataIndex: 'realname',
  },
  {
    title: '상태',
    dataIndex: 'status_dictText',
    width: 80,
  },
];
export const searchFormSchema: FormSchema[] = [
  {
    field: 'roleName',
    label: '역할 이름',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'roleCode',
    label: '역할 코드',
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
    label: '아이디',
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
    label: '역할 이름',
    required: true,
    component: 'Input',
  },
  {
    field: 'roleCode',
    label: '역할 코드',
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
              return Promise.reject('역할코드를 입력해주세요');
            }
            if (values) {
              return new Promise((resolve, reject) => {
                isRoleExist({ id: model.id, roleCode: value })
                  .then((res) => {
                    res.success ? resolve() : reject(res.message || '확인 실패');
                  })
                  .catch((err) => {
                    reject(err.message || '인증 실패');
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
    label: '비고',
    field: 'description',
    component: 'InputTextArea',
  },
];

export const formDescSchema = [
  {
    field: 'roleName',
    label: '역할 이름',
  },
  {
    field: 'roleCode',
    label: '역할 코드',
  },
  {
    label: '비고',
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
    label: '역할 코드',
    field: 'roleCode',
    component: 'Input',
    dynamicDisabled: true,
  },
  {
    label: '홈페이지 라우팅',
    field: 'url',
    component: 'Input',
    required: true,
    helpMessage: '홈페이지 라우팅 접속 주소',
  },
  {
    label: '구성요소 주소',
    field: 'component',
    component: 'Input',
    helpMessage: '홈페이지 라우팅의 구성요소 주소',
    componentProps: {
      placeholder: '프런트엔드 구성요소를 입력하세요.',
    },
    required: true,
  },
  {
    field: 'route',
    label: '라우팅',
    helpMessage: '비라우팅 메뉴는 홈페이지에 설정되어 있으며 활성화가 필요합니다',
    component: 'Switch',
    defaultValue: true
  },
  {
    label: '우선순위',
    field: 'priority',
    component: 'InputNumber',
  },
  {
    label: '활성화 여부',
    field: 'status',
    component: 'JSwitch',
    componentProps: {
      options: ['1', '0'],
    },
  },
];
