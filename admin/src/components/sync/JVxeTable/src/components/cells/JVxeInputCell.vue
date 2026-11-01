<template>
  <a-input ref="input" :value="innerValue" v-bind="cellProps" @blur="handleBlur" @change="handleChange" />
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import { isString } from '/@/utils/is';
  import { JVxeComponent, JVxeTypes } from '/@/components/sync/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/sync/JVxeTable/hooks';

  const NumberRegExp = /^-?\d+\.?\d*$/;
  export default defineComponent({
    name: 'JVxeInputCell',
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, handleChangeCommon, handleBlurCommon } = useJVxeComponent(props);

      /** 변경 이벤트 처리 */
      function handleChange(event) {
        let { target } = event;
        let { value, selectionStart } = target;
        let change = true;
        if (props.type === JVxeTypes.inputNumber) {
          // 입력값이 숫자 정규식과 일치하는지 확인하고, 일치하지 않으면 복원합니다.
          if (!NumberRegExp.test(value) && value !== '' && value !== '-') {
            change = false;
            value = innerValue.value;
            target.value = value || '';
            if (typeof selectionStart === 'number') {
              target.selectionStart = selectionStart - 1;
              target.selectionEnd = selectionStart - 1;
            }
          } else {
            // 예를 들어：41.1 -> 41.10, 100.1 -> 100.10 handlerChangeCommon 함수가 실행되지 않습니다.
            if (value.indexOf('.') != -1) {
              const result = value.split('.').pop();
              if (result && result.length >= 2 && result.substr(-1) === '0') {
                change = false;
                innerValue.value = value;
              }
            }
          }
        }
        // 이벤트를 트리거하고 입력 값을 저장합니다.
        if (change) {
          handleChangeCommon(value);
        }
      }

      /** 흐림 초점 상실 이벤트 처리 */
      function handleBlur(event) {
        let { target } = event;
        // 입력 값이 숫자 정규식과 일치하는지 확인합니다. 일치하지 않으면 공백으로 둡니다.
        if (props.type === JVxeTypes.inputNumber) {
          if (!NumberRegExp.test(target.value)) {
            target.value = '';
          } else {
            target.value = Number.parseFloat(target.value);
          }
        }
        handleChangeCommon(target.value, true);
        handleBlurCommon(target.value);
      }

      return {
        innerValue,
        cellProps,
        handleChange,
        handleBlur,
      };
    },
    enhanced: {
      installOptions: {
        autofocus: '.ant-input',
      },
      getValue(value, ctx) {
        if (ctx?.props?.type === JVxeTypes.inputNumber && isString(value)) {
          if (NumberRegExp.test(value)) {
            // 【issues/I5IHN7】소수점 입력이 안되는 버그 수정
            if (/\.0*$/.test(value)) {
              return value;
            }
            return Number.parseFloat(value);
          }
        }
        return value;
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
