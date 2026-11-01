// 컴포넌트를 수정하지 않고 특정 컴포넌트를 구성하기 위한 일반 구성

import type { SorterResult } from '../components/Table';

export default {
  // 테이블 구성
  table: {
    // 테이블 인터페이스는 구성요소 소품에서 재정의될 수 있는 일반 구성을 요청합니다.
    // xxx.xxx.xxx 형식 지원
    fetchSetting: {
      // 현재 페이지 필드가 배경으로 전달되었습니다.
      pageField: 'pageNo',
      // 각 페이지에 표시하기 위해 백그라운드로 전달되는 필드 수는 몇 개입니까?
      sizeField: 'pageSize',
      // 인터페이스는 Form 데이터 필드를 반환합니다.
      listField: 'records',
      // 인터페이스는 총 테이블 수의 필드를 반환합니다.
      totalField: 'total',
    },
    // 선택적 페이징 옵션
    pageSizeOptions: ['10', '50', '80', '100'],
    // 테이블 기본 크기
    defaultSize: 'middle',
    //기본적으로 페이지당 표시되는 항목 수
    defaultPageSize: 10,
    // 기본 정렬 방법
    defaultSortFn: (sortInfo: SorterResult) => {
      if(sortInfo instanceof Array){
        let sortInfoArray:any[] = []
        for(let item of sortInfo){
          let info = getSort(item);
          if(info){
            sortInfoArray.push(info)
          }
        }
        return {
          sortInfoString: JSON.stringify(sortInfoArray)
        }
      }else{
        let info = getSort(sortInfo)
        return info || {}
      }
    },
    // 맞춤 필터링 방법
    defaultFilterFn: (data: Partial<Recordable<string[]>>) => {
      return data;
    },
    scrollToFirstRowOnChange: false,
  },
  // 스크롤 컴포넌트 구성
  scrollbar: {
    // 기본 스크롤 스타일 사용 여부
    // 활성화되면 메뉴, 팝업 창 및 서랍이 기본 스크롤 막대 컴포넌트를 사용합니다.
    native: false,
  },
  //Form 구성
  form: {
    labelCol: {
      xs: { span: 24 },
      sm: { span: 4 },
      xl: { span: 6 },
      xxl: { span: 4 },
    },
    wrapperCol: {
      xs: { span: 24 },
      sm: { span: 18 },
    },
    //기본 콜론 형식
    colon: true,
  },
};

/**
 * 정렬 정보 가져오기
 * @param item
 */
function getSort(item){
  const { field, order } = item;
  if (field && order) {
    let sortType = 'ascend' == order ? 'asc' : 'desc';
    return {
      // 필드 정렬
      column: field,
      // 오름차순/내림차순으로 정렬
      order: sortType,
    };
  }
  return ''
}
