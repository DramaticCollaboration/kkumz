<template>
  <div :class="prefixCls">
    <a-select
      v-if="query"
      v-model:value="state"
      :options="selectOptions"
      :disabled="disabled"
      style="width: 100%"
      v-bind="attrs"
      @change="onSelectChange"
    />
    <a-switch v-else v-model:checked="checked" :disabled="disabled" v-bind="attrs" @change="onSwitchChange" />
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  const { prefixCls } = useDesign('j-switch');
  const props = defineProps({
    // v-model:value
    value: propTypes.oneOfType([propTypes.string, propTypes.number]),
    // 가치 옵션
    options: propTypes.array.def(() => ['Y', 'N']),
    // 텍스트 옵션
    labelOptions: propTypes.array.def(() => ['예', '아니요']),
    // 드롭다운 사용 여부
    query: propTypes.bool.def(false),
    // 비활성화 여부
    disabled: propTypes.bool.def(false),
  });
  const attrs = useAttrs();
  const emit = defineEmits(['change', 'update:value']);

  const checked = ref<boolean>(false);
  const [state] = useRuleFormItem(props, 'value', 'change');
  watch(
    () => props.value,
    (val) => {
      if (!props.query) {
        if (!val && !props.options.includes(val)) {
          checked.value = false;
          emitValue(props.options[1]);
        } else {
          checked.value = props.options[0] == val;
        }
      }
    },
    { immediate: true }
  );

  const selectOptions = computed(() => {
    let options: any[] = [];
    options.push({ value: props.options[0], label: props.labelOptions[0] });
    options.push({ value: props.options[1], label: props.labelOptions[1] });
    return options;
  });

  function onSwitchChange(checked) {
    let flag = checked === false ? props.options[1] : props.options[0];
    emitValue(flag);
  }

  function onSelectChange(value) {
    emitValue(value);
  }

  function emitValue(value) {
    emit('change', value);
    emit('update:value', value);
  }
</script>

<style lang="less">
  //noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-j-switch';

  .@{prefix-cls} {
  }
</style>
