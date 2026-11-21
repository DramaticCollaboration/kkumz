<!-- 필터링에 사용할 날짜 선택기（오늘, 이번주 등을 선택할 수 있습니다.） -->
<template>
  <a-space :id="formItemId" :class="[prefixCls]" direction="vertical">
    <a-space-compact block>
      <!-- 기간 드롭다운 -->
      <a-select v-if="isRange" v-model:value="innerValue">
        <a-select-option v-for="opt of RANGE_OPTIONS" :key="opt.key" :value="opt.key">
          {{ opt.label }}
        </a-select-option>
        <a-select-option key="custom" value="custom">
          {{ t('appUi.k3622ce61') }}
        </a-select-option>
      </a-select>
      <!-- 맞춤 날짜 선택기 -->
      <DatePicker v-else v-model:value="innerValue" v-model:open="datePickerIsOpen" v-bind="attrs"/>
      <!-- 오른쪽 드롭다운 메뉴 -->
      <a-dropdown v-if="allowSelectRange" :trigger="['click']">
        <a-button preIcon="ant-design:menu-unfold"/>
        <template #overlay>
          <a-menu @click="onMenuClick">
            <a-menu-item v-for="opt of RANGE_OPTIONS" :key="opt.key">
              {{ opt.label }}
            </a-menu-item>
            <a-menu-item key="custom">
              {{ t('appUi.k3622ce61') }}
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </a-space-compact>
  </a-space>
</template>

<script lang="ts">
import {defineComponent} from "vue";

export default defineComponent({
  name: 'DatePickerInFilter',
  inheritAttrs: false,
})
</script>

<script lang="ts" setup>
import {ref, watch, computed, nextTick, useAttrs, defineProps} from 'vue'
import {DatePicker} from 'ant-design-vue'
import {useDesign} from '/@/hooks/web/useDesign';
import { Form } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
const formItemContext = Form.useInjectFormItemContext();

// 기간 옵션
const RANGE_OPTIONS = [
  {key: 'TODAY', label: t('appUi.k2bdce5e8')},
  {key: 'YESTERDAY', label: t('appUi.k476b58b7')},
  {key: 'TOMORROW', label: t('appUi.kad95f2b6')},
  {key: 'THIS_WEEK', label: t('appUi.k89ea2d72')},
  {key: 'LAST_WEEK', label: t('appUi.k85289ac8')},
  {key: 'NEXT_WEEK', label: t('appUi.k3e92afe7')},
  {key: 'LAST_7_DAYS', label: t('appUi.kab9f8e81')},
  {key: 'THIS_MONTH', label: t('appUi.kfa72e1d8')},
  {key: 'LAST_MONTH', label: t('appUi.k996151eb')},
  {key: 'NEXT_MONTH', label: t('appUi.k4f828d56')},
];
const RANGE_OPTION_KEYS = RANGE_OPTIONS.map(item => item.key)

const {prefixCls} = useDesign('j-data-picker-in-filter');
const props = defineProps({
  value: {
    type: [String],
    default: ''
  },
  // 사전 설정된 범위 선택 허용 여부
  allowSelectRange: {
    type: Boolean,
    default: true,
  }
})
const emit = defineEmits(['change', 'update:value'])
const attrs = useAttrs()

// 컴포넌트 ID
const formItemId  = computed(() => formItemContext.id.value)

const innerValue = ref(props.value)
// 날짜 선택기를 열지 여부
const datePickerIsOpen = ref(false)

// 범위 옵션인지 확인
const isRange = computed(() => RANGE_OPTION_KEYS.includes(innerValue.value));

// 동기화 값
watch(() => props.value, (val) => {
  innerValue.value = val
})

// 변화를 방출하다
watch(innerValue, (val) => {
  if (val === 'custom') {
    val = ''
    openDatePicker()
  }
  emit('change', val)
  emit('update:value', val)
  formItemContext?.onFieldChange();
})

watch(() => props.allowSelectRange, (allow) => {
  // 범위 선택이 허용되지 않고 현재 값이 범위 옵션인 경우 값을 지웁니다.
  if (!allow && isRange.value) {
    innerValue.value = ''
  }
}, {immediate: true});

// 오른쪽의 드롭다운 메뉴를 클릭하세요.
function onMenuClick(event: Recordable) {
  if (event.key === 'custom') {
    if (isRange.value) {
      innerValue.value = '';
    }
    openDatePicker()
  } else {
    innerValue.value = event.key
  }
}

// 날짜 선택기 드롭다운 열기
async function openDatePicker() {
  await nextTick()
  datePickerIsOpen.value = true
}

</script>

<style lang="less">
//noinspection LessUnresolvedVariable
@prefix-cls: ~'@{namespace}-j-data-picker-in-filter';

.@{prefix-cls} {
  width: 100%;
}
</style>
