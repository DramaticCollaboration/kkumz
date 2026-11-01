import { unref } from 'vue';
import { dateUtil } from '/@/utils/dateUtil';

/**
 * Form 간격 시간 숫자 필드 변환
 * @param props
 * @param values
 */
export function handleRangeValue(props, values) {
  //fieldMapToTime 구성 및 처리 여부 결정
  const fieldMapToTime = unref(props)?.fieldMapToTime;
  fieldMapToTime && (values = handleRangeTimeValue(props, values));
  //fieldMapToNumber 구성 및 처리 여부 결정
  const fieldMapToNumber = unref(props)?.fieldMapToNumber;
  fieldMapToNumber && (values = handleRangeNumberValue(props, values));
  return values;
}
/**
 * 처리 시간을 2개 필드로 변환
 * @param props
 * @param values
 */
export function handleRangeTimeValue(props, values) {
  const fieldMapToTime = unref(props).fieldMapToTime;
  if (!fieldMapToTime || !Array.isArray(fieldMapToTime)) {
    return values;
  }
  for (const [field, [startTimeKey, endTimeKey], format = 'YYYY-MM-DD'] of fieldMapToTime) {
    if (!field || !startTimeKey || !endTimeKey || !values[field]) {
      continue;
    }

    // 【issues/I53G9Y】 날짜 간격 구성요소는 문자열일 수 있습니다.
    let timeValue = values[field];
    if (!Array.isArray(timeValue)) {
      timeValue = timeValue.split(',');
    }
    const [startTime, endTime]: string[] = timeValue;
    values[startTimeKey] = dateUtil(startTime).format(format);
    values[endTimeKey] = dateUtil(endTime).format(format);
    Reflect.deleteProperty(values, field);
  }
  return values;
}
/**
 * 숫자를 2개의 필드로 처리
 * @param props
 * @param values
 * @updateby liusq
 * @updateDate:2021-09-16
 */
export function handleRangeNumberValue(props, values) {
  const fieldMapToNumber = unref(props).fieldMapToNumber;
  if (!fieldMapToNumber || !Array.isArray(fieldMapToNumber)) {
    return values;
  }
  for (const [field, [startNumberKey, endNumberKey]] of fieldMapToNumber) {
    if (!field || !startNumberKey || !endNumberKey || !values[field]) {
      continue;
    }
    let temp = values[field];
    if (typeof temp === 'string') {
      temp = temp.split(',');
    }
    const [startNumber, endNumber]: number[] = temp;
    values[startNumberKey] = startNumber;
    values[endNumberKey] = endNumber;
    Reflect.deleteProperty(values, field);
  }
  return values;
}
