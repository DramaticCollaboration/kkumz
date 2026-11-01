import type { FieldMapToTime, FormSchema } from './types/form';
import type { CSSProperties, PropType } from 'vue';
import type { ColEx } from './types';
import type { TableActionType } from '/@/components/Table';
import type { ButtonProps } from 'ant-design-vue/es/button/buttonTypes';
import type { RowProps } from 'ant-design-vue/lib/grid/Row';
import dayjs from "dayjs";
import { propTypes } from '/@/utils/propTypes';
import componentSetting from '/@/settings/componentSetting';

const { form } = componentSetting;
export const basicProps = {
  model: {
    type: Object as PropType<Recordable>,
    default: {},
  },
  // 라벨 너비 고정 너비
  labelWidth: {
    type: [Number, String] as PropType<number | string>,
    default: 0,
  },
  fieldMapToTime: {
    type: Array as PropType<FieldMapToTime>,
    default: () => [],
  },
  fieldMapToNumber: {
    type: Array as PropType<FieldMapToTime>,
    default: () => [],
  },
  compact: propTypes.bool,
  // Form 구성 규칙
  schemas: {
    type: [Array] as PropType<FormSchema[]>,
    default: () => [],
  },
  mergeDynamicData: {
    type: Object as PropType<Recordable>,
    default: null,
  },
  baseRowStyle: {
    type: Object as PropType<CSSProperties>,
  },
  baseColProps: {
    type: Object as PropType<Partial<ColEx>>,
  },
  autoSetPlaceHolder: propTypes.bool.def(true),
  // INPUT 컴포넌트에서 Enter 클릭 시 자동 제출 여부
  autoSubmitOnEnter: propTypes.bool.def(false),
  submitOnReset: propTypes.bool,
  size: propTypes.oneOf(['default', 'small', 'large']).def('default'),
  // Form 비활성화
  disabled: propTypes.bool,
  emptySpan: {
    type: [Number, Object] as PropType<number>,
    default: 0,
  },
  // 축소 확장 버튼 표시 여부
  showAdvancedButton: propTypes.bool,
  // 전환 시간
  transformDateFunc: {
    type: Function as PropType<Fn>,
    default: (date: any) => {
      // dayjs 인스턴스인지 확인
      return dayjs.isDayjs(date) ? date?.format('YYYY-MM-DD HH:mm:ss') : date;
    },
  },
  rulesMessageJoinLabel: propTypes.bool.def(true),
  // 【empasy】3개 이상의 열을 자동으로 접습니다.
  autoAdvancedCol: propTypes.number.def(3),
  // 3줄 이상일 경우 자동으로 접기
  autoAdvancedLine: propTypes.number.def(3),
  // 접기에 영향을 받지 않는 행 수
  alwaysShowLines: propTypes.number.def(1),

  // 조작 버튼 표시 여부
  showActionButtonGroup: propTypes.bool.def(true),
  // 작업 열 Col 구성
  actionColOptions: Object as PropType<Partial<ColEx>>,
  // 재설정 버튼 표시
  showResetButton: propTypes.bool.def(true),
  // 첫 번째 입력 상자에 초점을 맞출지 여부는 첫 번째 Form 항목이 입력될 때만 작동합니다.
  autoFocusFirstItem: propTypes.bool,
  // 재설정 버튼 구성
  resetButtonOptions: Object as PropType<Partial<ButtonProps>>,

  // 확인 버튼 표시
  showSubmitButton: propTypes.bool.def(true),
  // 버튼 구성 확인
  submitButtonOptions: Object as PropType<Partial<ButtonProps>>,

  // 맞춤형 재설정 기능
  resetFunc: Function as PropType<() => Promise<void>>,
  submitFunc: Function as PropType<() => Promise<void>>,

  // 다음은 기본 소품입니다
  hideRequiredMark: propTypes.bool,

  labelCol: {
    type: Object as PropType<Partial<ColEx>>,
    default: form.labelCol,
  },

  layout: propTypes.oneOf(['horizontal', 'vertical', 'inline']).def('horizontal'),
  tableAction: {
    type: Object as PropType<TableActionType>,
  },

  wrapperCol: {
    type: Object as PropType<Partial<ColEx>>,
    default: form.wrapperCol,
  },

  colon: propTypes.bool.def(form.colon),

  labelAlign: propTypes.string,

  rowProps: Object as PropType<RowProps>,

  // Form이 조회 조건인 경우 Form 변경 후 자동으로 조회됩니다. 조회 버튼을 클릭할 필요가 없습니다.
  autoSearch: propTypes.bool.def(false),
};
