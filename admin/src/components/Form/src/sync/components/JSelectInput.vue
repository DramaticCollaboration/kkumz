<template>
  <a-select v-bind="bindProps" @change="onChange" @search="onSearch" />
</template>

<script lang="ts">
  import { propTypes } from '/@/utils/propTypes';
  import { defineComponent, ref, watch, computed } from 'vue';

  // 입력 드롭다운 상자（이 구성요소는 현재 사용되지 않습니다.）
  export default defineComponent({
    name: 'JSelectInput',
    props: {
      options: propTypes.array.def(() => []),
    },
    emits: ['change', 'update:value'],
    setup(props, { emit, attrs }) {
      // 내부 옵션 옵션
      const options = ref<any[]>([]);
      // 외부 옵션의 변경 사항을 모니터링하고 내부 옵션을 덮어씁니다.
      watch(
        () => props.options,
        () => {
          options.value = [...props.options];
        },
        { deep: true, immediate: true }
      );
      // props와 attrs 병합
      const bindProps: any = computed(() =>
        Object.assign(
          {
            showSearch: true,
          },
          props,
          attrs,
          {
            options: options.value,
          }
        )
      );

      function onChange(...args: any[]) {
        deleteSearchAdd(args[0]);
        emit('change', ...args);
        emit('update:value', args[0]);
      }

      function onSearch(value) {
        // 해당 항목이 있는지 여부, 찾을 수 없으면 이 항목을 추가하세요.
        let foundIt =
          options.value.findIndex((option) => {
            return option.value.toString() === value.toString();
          }) !== -1;
        // !!value ：Null 값을 추가하지 마세요.
        if (!foundIt && !!value) {
          deleteSearchAdd(value);
          // searchAdd 검색을 통해 추가되나요?
          options.value.push({ value: value, searchAdd: true });
          //onChange(value,{ value })
        } else if (foundIt) {
          onChange(value);
        }
      }

      // 쓸모없는 검색 제거（사용자 입력）그리고 생성된 아이템
      function deleteSearchAdd(value = '') {
        let indexes: any[] = [];
        options.value.forEach((option, index) => {
          if (option.searchAdd) {
            if ((option.value ?? '').toString() !== value.toString()) {
              indexes.push(index);
            }
          }
        });
        // 뒤집어서 배열의 항목 삭제
        for (let index of indexes.reverse()) {
          options.value.splice(index, 1);
        }
      }

      return {
        bindProps,
        onChange,
        onSearch,
      };
    },
  });
</script>

<style scoped></style>
