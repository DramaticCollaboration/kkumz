<template>
  <a-select :value="innerValue" v-bind="selectProps">
    <template v-if="loading" #notFoundContent>
      <LoadingOutlined />
      <span>&nbsp;로드 중…</span>
    </template>
    <template v-for="option of selectOptions" :key="option.value">
      <a-select-option :value="option.value" :title="option.text || option.label || option.title" :disabled="option.disabled">
        <span>{{ option.text || option.label || option.title || option.value }}</span>
      </a-select-option>
    </template>
  </a-select>
</template>

<script lang="ts">
  import { ref, computed, defineComponent } from 'vue';
  import { LoadingOutlined } from '@ant-design/icons-vue';
  import { filterDictText } from '/@/utils/dict/JDictSelectUtil';
  import { JVxeComponent, JVxeTypes } from '/@/components/sync/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/sync/JVxeTable/hooks';
  import { dispatchEvent } from '/@/components/sync/JVxeTable/utils';
  import { isPromise } from '/@/utils/is';

  export default defineComponent({
    name: 'JVxeSelectCell',
    components: { LoadingOutlined },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, row, originColumn, scrolling, handleChangeCommon, handleBlurCommon } = useJVxeComponent(props);
      const loading = ref(false);
      // 비동기식으로 로드된 옵션（다단계 연결에 사용됩니다.）
      const asyncOptions = ref<any[] | null>(null);
      // 드롭다운 상자 소품
      const selectProps = computed(() => {
        let selProps = {
          ...cellProps.value,
          allowClear: true,
          autofocus: true,
          defaultOpen: !scrolling.value,
          style: { width: '100%' },
          filterOption: handleSelectFilterOption,
          onBlur: handleBlur,
          onChange: handleChange,
        };
        // 선택 항목이 입력을 허용하는지 확인
        let { allowSearch, allowInput } = originColumn.value;
        if (allowInput === true || allowSearch === true) {
          selProps['showSearch'] = true;
          selProps['onSearch'] = handleSearchSelect;
        }
        return selProps;
      });
      // 드롭다운 옵션
      const selectOptions = computed(() => {
        if (asyncOptions.value) {
          return asyncOptions.value;
        }
        let { linkage } = props.renderOptions;
        if (linkage) {
          let { getLinkageOptionsSibling, config } = linkage;
          let res = getLinkageOptionsSibling(row.value, originColumn.value, config, true);
          // Promise가 반환되면 다중 레벨 연결을 나타냅니다.
          if (res instanceof Promise) {
            loading.value = true;
            res
              .then((opt) => {
                asyncOptions.value = opt;
                loading.value = false;
              })
              .catch((e) => {
                console.error(e);
                loading.value = false;
              });
          } else {
            asyncOptions.value = null;
            return res;
          }
        }
        return originColumn.value.options;
      });

      // --------- created ---------

      // 다중선택, 검색형
      let multipleTypes = [JVxeTypes.selectMultiple, 'list_multi'];
      let searchTypes = [JVxeTypes.selectSearch, 'sel_search'];
      if (multipleTypes.includes(props.type)) {
        // 다중 선택 처리
        let props = originColumn.value.props || {};
        props['mode'] = 'multiple';
        props['maxTagCount'] = 1;
        originColumn.value.allowSearch = true;
        originColumn.value.props = props;
      } else if (searchTypes.includes(props.type)) {
        // 검색 처리
        originColumn.value.allowSearch = true;
      }

      /** 변경 이벤트 처리 */
      function handleChange(value) {
        // 하위 수준 연결 처리
        let linkage = props.renderOptions.linkage;
        if (linkage) {
          linkage.handleLinkageSelectChange(row.value, originColumn.value, linkage.config, value);
        }
        handleChangeCommon(value);
      }

      /** 흐림 초점 상실 이벤트 처리 */
      function handleBlur(value) {
        let { allowInput, options } = originColumn.value;
        if (allowInput === true) {
          // 쓸모없는 검색 제거（사용자 입력）그리고 생성된 아이템
          if (typeof value === 'string') {
            let indexes: number[] = [];
            options.forEach((option, index) => {
              if (option.value.toLocaleString() === value.toLocaleString()) {
                delete option.searchAdd;
              } else if (option.searchAdd === true) {
                indexes.push(index);
              }
            });
            // 뒤집어서 배열의 항목 삭제
            for (let index of indexes.reverse()) {
              options.splice(index, 1);
            }
          }
        }
        handleBlurCommon(value);
      }

      /** 드롭다운 상자의 내용을 검색하는 데 사용됩니다. */
      function handleSelectFilterOption(input, option) {

        let { allowSearch, allowInput } = originColumn.value;
        if (allowSearch === true || allowInput === true) {
          if (option.title == null) return false;
          return option.title.toLowerCase().indexOf(input.toLowerCase()) >= 0;
        }
        return true;
      }

      /** 검색 중 이벤트 선택, 동적으로 옵션을 추가하는 데 사용 */
      function handleSearchSelect(value) {
        let { allowSearch, allowInput, options } = originColumn.value;

        if (allowSearch !== true && allowInput === true) {
          // 해당 항목이 있는지 여부, 찾을 수 없으면 이 항목을 추가하세요.
          let flag = false;
          for (let option of options) {
            if (option.value.toLocaleString() === value.toLocaleString()) {
              flag = true;
              break;
            }
          }
          // !!value ：Null 값을 추가하지 마세요.
          if (!flag && !!value) {
            // searchAdd 검색을 통해 추가되나요?
            options.push({ title: value, value: value, searchAdd: true });
          }
        }
      }

      return {
        loading,
        innerValue,
        selectProps,
        selectOptions,
        handleChange,
        handleBlur,
      };
    },
    // 【구성요소 강화】자세한 내용은 참고 사항을 참조하세요.：JVxeComponent.Enhanced
    enhanced: {
      aopEvents: {
        editActived({ $event, row, column }) {
          dispatchEvent({
            $event,
            row,
            column,
            props: this.props,
            instance: this,
            className: '.ant-select .ant-select-selection-search-input',
            isClick: false,
            handler: (el) => el.focus(),
          });
        },
      },
      translate: {
        enabled: true,
        async handler(value, ctx) {
          let { props, context } = ctx!;
          let { row, originColumn } = context;
          let options;
          let linkage = props?.renderOptions.linkage;
          // 다단계 연계인지 확인하고, 그렇다면 인터페이스를 통해 비동기적으로 번역합니다.
          if (linkage) {
            let { getLinkageOptionsSibling, config } = linkage;
            let linkageOptions = getLinkageOptionsSibling(row.value, originColumn.value, config, true);
            options = isPromise(linkageOptions) ? await linkageOptions : linkageOptions;
          } else if (isPromise(originColumn.value.optionsPromise)) {
            options = await originColumn.value.optionsPromise;
          } else {
            options = originColumn.value.options;
          }
          return filterDictText(options, value);
        },
      },
      getValue(value) {
        if (Array.isArray(value)) {
          return value.join(',');
        } else {
          return value;
        }
      },
      setValue(value, ctx) {
        let { context } = ctx!;
        let { originColumn } = context;
        // 다중 선택인지 확인
        if ((originColumn.value.props || {})['mode'] === 'multiple') {
          originColumn.value.props['maxTagCount'] = 1;
        }
        if (value != null && value !== '') {
          if (typeof value === 'string') {
            return value === '' ? [] : value.split(',');
          }
          return value;
        } else {
          return undefined;
        }
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
