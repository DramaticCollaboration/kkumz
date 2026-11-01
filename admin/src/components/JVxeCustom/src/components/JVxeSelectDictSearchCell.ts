import { computed, ref, watch, defineComponent, h } from 'vue';
import { cloneDeep, debounce } from 'lodash-es';
import { defHttp } from '/@/utils/http/axios';
import { filterDictText } from '/@/utils/dict/JDictSelectUtil';
import { ajaxGetDictItems, getDictItemsByCode } from '/@/utils/dict';
import { JVxeComponent } from '/@/components/sync/JVxeTable/types';
import { dispatchEvent } from '/@/components/sync/JVxeTable/utils';
import { useResolveComponent as rc } from '/@/components/sync/JVxeTable/hooks';
import { useJVxeComponent, useJVxeCompProps } from '/@/components/sync/JVxeTable/hooks';
import { useMessage } from '/@/hooks/web/useMessage';

/** 값 - 반복 쿼리를 방지하기 위한 레이블 맵（캐시 지우기 새로 고침） */
const LabelMap = new Map<string, any>();
// 요청 ID
let requestId = 0;

/** 번역 기능이 내장된 디스플레이 구성요소 */
export const DictSearchSpanCell = defineComponent({
  name: 'JVxeSelectSearchSpanCell',
  props: useJVxeCompProps(),
  setup(props: JVxeComponent.Props) {
    const { innerOptions, innerSelectValue, innerValue } = useSelectDictSearch(props);
    return () => {
      return h('span', {}, [filterDictText(innerOptions.value, innerSelectValue.value || innerValue.value)]);
    };
  },
});

// 입력 선택 컴포넌트
export const DictSearchInputCell = defineComponent({
  name: 'JVxeSelectSearchInputCell',
  props: useJVxeCompProps(),
  setup(props: JVxeComponent.Props) {
    const { createMessage } = useMessage();
    const { dict, loading, isAsync, options, innerOptions, originColumn, cellProps, innerSelectValue, handleChangeCommon } =
      useSelectDictSearch(props);
    const hasRequest = ref(false);
    // 프롬프트 메시지
    const tipsContent = computed(() => {
      return originColumn.value.tipsContent || '검색 내용을 입력해주세요';
    });
    // 필터 기능
    const filterOption = computed(() => {
      if (isAsync.value) {
        //【sync-boot-console/issues/I5QRT8】JVxeTypes.selectDictSearch 동기화 문제
        return ()=>true;
      }
      return (input, option) => option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0;
    });

    /** 데이터 로드 */
    const loadData = debounce((value) => {
      const currentRequestId = ++requestId;
      loading.value = true;
      innerOptions.value = [];
      if (value == null || value.trim() === '') {
        loading.value = false;
        hasRequest.value = false;
        return;
      }
      // 사전 코드 형식：table,text,code
      hasRequest.value = true;
      loadDictByKeyword(dict.value, value)
        .then((res) => {
          if (currentRequestId !== requestId) {
            return;
          }
          let { success, result, message } = res;
          if (success) {
            innerOptions.value = result;
            result.forEach((item) => {
              LabelMap.set(item.value, [item]);
            });
          } else {
            createMessage.warning(message || '쿼리 실패');
          }
        })
        .finally(() => {
          loading.value = false;
        });
    }, 300);

    function handleChange(selectedValue) {
      innerSelectValue.value = selectedValue;
      handleChangeCommon(innerSelectValue.value);
    }

    function handleSearch(value) {
      if (isAsync.value) {
        // loadData에 디바운싱이 추가되었기 때문에 타이핑 시에도 로딩을 켜야 하므로 사용자가 주관적으로 고려하는 800ms의 지연 시간이 있을 것입니다.
        loading.value = true;
        if (innerOptions.value.length > 0) {
          innerOptions.value = [];
        }
        loadData(value);
      }
    }

    function renderOptionItem() {
      let optionItems: any[] = [];
      options.value.forEach(({ value, text, label, title, disabled }) => {
        optionItems.push(
          h(
            rc('a-select-option'),
            {
              key: value,
              value: value,
              disabled: disabled,
            },
            {
              default: () => text || label || title,
            }
          )
        );
      });
      return optionItems;
    }

    return () => {
      return h(
        rc('a-select'),
        {
          ...cellProps.value,
          value: innerSelectValue.value,
          filterOption: filterOption.value,
          showSearch: true,
          allowClear: true,
          autofocus: true,
          defaultOpen: true,
          style: 'width: 100%',
          onSearch: handleSearch,
          onChange: handleChange,
        },
        {
          default: () => renderOptionItem(),
          notFoundContent: () => {
            if (loading.value) {
              return h(rc('a-spin'), { size: 'small' });
            } else if (hasRequest.value) {
              return h('div', '데이터가 없습니다');
            } else {
              return h('div', [tipsContent.value]);
            }
          },
        }
      );
    };
  },
  // 【구성요소 강화】자세한 내용은 참고 사항을 참조하세요.：JVxeComponent.Enhanced
  enhanced: {
    aopEvents: {
      editActived({ $event }) {
        dispatchEvent({
          $event,
          props: this.props,
          className: '.ant-select .ant-select-selection-search-input',
          isClick: false,
          handler: (el) => el.focus(),
        });
      },
    },
  } as JVxeComponent.EnhancedPartial,
});

function useSelectDictSearch(props) {
  const setup = useJVxeComponent(props);
  const { innerValue, originColumn } = setup;

  // 로딩 상태
  const loading = ref(false);
  // 내부 선택 값
  const innerSelectValue = ref(null);
  // 내부 옵션
  const innerOptions = ref<any[]>([]);

  const dict = computed(() => originColumn.value.dict);
  // 비동기 모드인지 여부
  const isAsync = computed(() => {
    let isAsync = originColumn.value.async;
    return isAsync != null && isAsync !== '' ? !!isAsync : true;
  });
  const options = computed(() => {
    if (isAsync.value) {
      return innerOptions.value;
    } else {
      return originColumn.value.options || [];
    }
  });

  /** 공공재산 모니터링 */
  watch(
    innerValue,
    (value: string) => {
      if (value == null || value === '') {
        innerSelectValue.value = null;
      } else {
        loadDataByValue(value);
      }
    },
    { immediate: true }
  );
  watch(dict, () => loadDataByDict());

  // 에코 값을 기반으로 데이터 쿼리
  async function loadDataByValue(value) {
    if (isAsync.value) {
      if (innerSelectValue.value !== value) {
        if (LabelMap.has(value)) {
          innerOptions.value = cloneDeep(LabelMap.get(value));
        } else {
          let result = await loadDictItem(dict.value, value);
          if (result && result.length > 0) {
            innerOptions.value = [{ value: value, text: result[0] }];
            LabelMap.set(value, cloneDeep(innerOptions.value));
          }
        }
      }
    }
    innerSelectValue.value = (value || '').toString();
  }

  // 사전 초기화
  async function loadDataByDict() {
    if (!isAsync.value) {
      // 사전 항목 컬렉션에 데이터가 있는 경우
      if (!originColumn.value.options || originColumn.value.options.length === 0) {
        // 사전 코드에 따라 사전 배열을 초기화합니다.
        let dictStr = '';
        if (dict.value) {
          let arr = dict.value.split(',');
          if (arr[0].indexOf('where') > 0) {
            let tbInfo = arr[0].split('where');
            dictStr = tbInfo[0].trim() + ',' + arr[1] + ',' + arr[2] + ',' + encodeURIComponent(tbInfo[1]);
          } else {
            dictStr = dict.value;
          }
          if (dict.value.indexOf(',') === -1) {
            //캐시에서 사전 구성 읽기 우선순위 지정
            let cache = getDictItemsByCode(dict.value);
            if (cache) {
              innerOptions.value = cache;
              return;
            }
          }
          let { success, result } = await ajaxGetDictItems(dictStr, null);
          if (success) {
            innerOptions.value = result;
          }
        }
      }
    }
  }

  return {
    ...setup,
    loading,
    innerOptions,
    innerSelectValue,
    dict,
    isAsync,
    options,
  };
}

/** 사전 항목 가져오기 */
function loadDictItem(dict: string, key: string) {
  return defHttp.get({
    url: `/api/v1/kkumz/sys/dict/loadDictItem/${dict}`,
    params: {
      key: key,
    },
  });
}

/** 키워드를 기반으로 사전 항목 가져오기（찾다） */
function loadDictByKeyword(dict: string, keyword: string) {
  return defHttp.get(
    {
      url: `/api/v1/kkumz/sys/dict/loadDict/${dict}`,
      params: {
        keyword: keyword,
      },
    },
    {
      isTransformResponse: false,
    }
  );
}
