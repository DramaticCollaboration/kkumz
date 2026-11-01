import { FormSchema } from '/@/components/Form';

// 학과기본Form
export function useBasicFormSchema() {
  const basicFormSchema: FormSchema[] = [
    {
      field: 'departName',
      label: '조직 이름',
      component: 'Input',
      componentProps: {
        placeholder: '소속/부서명을 입력해주세요',
      },
      rules: [{ required: true, message: '조직 이름은 필수입니다.' }],
    },
    {
      field: 'parentId',
      label: '상급부서',
      component: 'TreeSelect',
      componentProps: {
        treeData: [],
        placeholder: '없음',
        dropdownStyle: { maxHeight: '200px', overflow: 'auto' },
      },
    },
    {
      field: 'orgCode',
      label: '기관 코드',
      component: 'Input',
      componentProps: {
        placeholder: '조직코드를 입력해주세요',
      },
    },
    {
      field: 'orgCategory',
      label: '기관 유형',
      component: 'RadioButtonGroup',
      componentProps: { options: [] },
    },
    {
      field: 'departOrder',
      label: '정렬',
      component: 'InputNumber',
      componentProps: {},
    },
    {
      field: 'mobile',
      label: '전화',
      component: 'Input',
      componentProps: {
        placeholder: '전화번호를 입력해주세요',
      },
    },
    {
      field: 'fax',
      label: '팩스',
      component: 'Input',
      componentProps: {
        placeholder: '팩스를 입력해주세요',
      },
    },
    {
      field: 'address',
      label: '주소',
      component: 'Input',
      componentProps: {
        placeholder: '주소를 입력해주세요',
      },
    },
    {
      field: 'memo',
      label: '비고',
      component: 'InputTextArea',
      componentProps: {
        placeholder: '비고란을 입력해 주세요',
      },
    },
  ];
  return { basicFormSchema };
}

// 조직 유형 옵션
export const orgCategoryOptions = {
  // 1급 부서
  root: [{ value: '1', label: '회사' }],
  // 하위 부서
  child: [
    { value: '2', label: '부서' },
    { value: '3', label: '이메일' },
  ],
};
