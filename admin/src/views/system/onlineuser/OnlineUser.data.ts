import { FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';
import { getToken } from '/@/utils/auth';

// 목록 컬럼
export const columns = [
  {
    title: '아이디',
    align: 'center',
    dataIndex: 'username',
    customRender: ({ text, record }) => {
      let token = getToken();
      if (record && record.token === token) {
        return text + ' (현재 접속자)';
      }
      return text;
    },
  },
  {
    title: '사용자 이름',
    align: 'center',
    dataIndex: 'realname',
  },
  {
    title: '프로필 사진',
    align: 'center',
    width: 100,
    dataIndex: 'avatar',
    customRender: render.renderAvatar,
  },
  {
    title: '생년월일',
    align: 'center',
    dataIndex: 'birthday',
  },
  {
    title: '성별',
    align: 'center',
    dataIndex: 'sex',
    customRender: ({ text }) => {
      return render.renderDict(text, 'sex');
    },
  },
  {
    title: '휴대폰 번호',
    align: 'center',
    dataIndex: 'phone',
  },
];

// 검색 폼
export const searchFormSchema: FormSchema[] = [
  {
    field: 'username',
    label: '아이디',
    component: 'Input',
    colProps: { span: 6 },
  },
];
