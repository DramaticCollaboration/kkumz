import type { ValidationRule } from 'ant-design-vue/lib/form/Form';
import type { ComponentType } from './types/index';
import { useI18n } from '/@/hooks/web/useI18n';
import { dateUtil } from '/@/utils/dateUtil';
import { isNumber, isObject } from '/@/utils/is';

const { t } = useI18n();

/**
 * @description:
 */
export function createPlaceholderMessage(component: ComponentType) {
  if (component.includes('Input') || component.includes('Complete')) {
    return t('common.inputText');
  }
  if (component.includes('Picker')) {
    return t('common.chooseText');
  }
  if (/Select|Cascader|Checkbox|Radio|Switch|Picker/.test(component)) {
    // return `$을 선택하세요.{label}`;
    return t('common.chooseText');
  }
  return '';
}

const DATE_TYPE = ['DatePicker', 'MonthPicker', 'WeekPicker', 'TimePicker'];

function genType() {
  return [...DATE_TYPE, 'RangePicker'];
}

export function setComponentRuleType(rule: ValidationRule, component: ComponentType, valueFormat: string) {
  //https://github.com/vbenjs/vue-vben-admin/pull/3082 github 복구 원본 텍스트
  if (Reflect.has(rule, 'type')) {
    return;
  }
  if (DATE_TYPE.includes(component)) {
    rule.type = valueFormat ? 'string' : 'object';
  } else if (/^(RangePicker|Upload|CheckboxGroup|TimePicker)$/.test(component)) {
    rule.type = 'array';
  } else if (['InputNumber'].includes(component)) {
    rule.type = 'number';
  }
}

export function processDateValue(attr: Recordable, component: string) {
  const { valueFormat, value } = attr;
  if (valueFormat) {
    attr.value = isObject(value) ? dateUtil(value).format(valueFormat) : value;
  } else if (DATE_TYPE.includes(component) && value) {
    attr.value = dateUtil(attr.value);
  }
}

export function handleInputNumberValue(component?: ComponentType, val?: any) {
  if (!component) return val;
  if (/^Input(Password|Search|TextArea)?$/.test(component)) {
    return val && isNumber(val) ? `${val}` : val;
  }
  return val;
}
/**
*liaozhiyang
*2023-12-26
*일부 컴포넌트의 값 전송에는 문자열 유형을 숫자 유형으로 변환해야 합니다.
*/
export function handleInputStringValue(component?: ComponentType, val?: any) {
  if (!component) return val;
  if (['InputNumber'].includes(component) && typeof val === 'string' && val != '') {
    return Number(val);
  }
  return val;
}

/**
 * 시간 필드
 */
export const dateItemType = genType();
