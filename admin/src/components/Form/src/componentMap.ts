/**
 * 현재 비동기 로딩을 구현하는 컴포넌트 목록 ：
 * JAreaLinkage
 * JEditor
 * JMarkdownEditor
 * JCodeEditor
 * JEasyCron
 */
import type { Component } from 'vue';
import type { ComponentType } from './types/index';
import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
/**
 * Component list, register here to setting it in the form
 */
import {
  Input,
  Select,
  Radio,
  Checkbox,
  AutoComplete,
  Cascader,
  DatePicker,
  InputNumber,
  Switch,
  TimePicker,
  TreeSelect,
  Slider,
  Rate,
  Divider,
} from 'ant-design-vue';
import ApiRadioGroup from './components/ApiRadioGroup.vue';
import RadioButtonGroup from './components/RadioButtonGroup.vue';
import ApiSelect from './components/ApiSelect.vue';
import ApiTreeSelect from './components/ApiTreeSelect.vue';
import { BasicUpload } from '/@/components/Upload';
import { StrengthMeter } from '/@/components/StrengthMeter';
import { IconPicker } from '/@/components/Icon';
import { CountdownInput } from '/@/components/CountDown';
//맞춤 구성요소
// import JAreaLinkage from './sync/components/JAreaLinkage.vue';
import JSelectUser from './sync/components/JSelectUser.vue';
import JSelectPosition from './sync/components/JSelectPosition.vue';
import JSelectRole from './sync/components/JSelectRole.vue';
import JImageUpload from './sync/components/JImageUpload.vue';
import JDictSelectTag from './sync/components/JDictSelectTag.vue';
import JSelectDept from './sync/components/JSelectDept.vue';
import JAreaSelect from './sync/components/JAreaSelect.vue';
import JEditor from './sync/components/JEditor.vue';
// import JMarkdownEditor from './sync/components/JMarkdownEditor.vue';
import JSelectInput from './sync/components/JSelectInput.vue';
// import JCodeEditor from './sync/components/JCodeEditor.vue';
import JCategorySelect from './sync/components/JCategorySelect.vue';
import JSelectMultiple from './sync/components/JSelectMultiple.vue';
import JPopup from './sync/components/JPopup.vue';
import JPopupDict from './sync/components/JPopupDict.vue';
import JSwitch from './sync/components/JSwitch.vue';
import JTreeDict from './sync/components/JTreeDict.vue';
import JInputPop from './sync/components/JInputPop.vue';
// import { JEasyCron } from './sync/components/JEasyCron';
import JCheckbox from './sync/components/JCheckbox.vue';
import JInput from './sync/components/JInput.vue';
import JTreeSelect from './sync/components/JTreeSelect.vue';
import JEllipsis from './sync/components/JEllipsis.vue';
import JSelectUserByDept from './sync/components/JSelectUserByDept.vue';
import JUpload from './sync/components/JUpload/JUpload.vue';
import JSearchSelect from './sync/components/JSearchSelect.vue';
import JAddInput from './sync/components/JAddInput.vue';
import { Time } from '/@/components/Time';
import JRangeNumber from './sync/components/JRangeNumber.vue';
import UserSelect from './sync/components/userSelect/index.vue';
import JRangeDate from './sync/components/JRangeDate.vue'
import JRangeTime from './sync/components/JRangeTime.vue'
import JInputSelect from './sync/components/JInputSelect.vue'
import RoleSelectInput from './sync/components/roleSelect/RoleSelectInput.vue';
import {DatePickerInFilter, CascaderPcaInFilter} from "@/components/InFilter";

const componentMap = new Map<ComponentType, Component>();

componentMap.set('Time', Time);
componentMap.set('Input', Input);
componentMap.set('InputGroup', Input.Group);
componentMap.set('InputPassword', Input.Password);
componentMap.set('InputSearch', Input.Search);
componentMap.set('InputTextArea', Input.TextArea);
componentMap.set('InputNumber', InputNumber);
componentMap.set('AutoComplete', AutoComplete);

componentMap.set('Select', Select);
componentMap.set('ApiSelect', ApiSelect);
componentMap.set('TreeSelect', TreeSelect);
componentMap.set('ApiTreeSelect', ApiTreeSelect);
componentMap.set('ApiRadioGroup', ApiRadioGroup);
componentMap.set('Switch', Switch);
componentMap.set('RadioButtonGroup', RadioButtonGroup);
componentMap.set('RadioGroup', Radio.Group);
componentMap.set('Checkbox', Checkbox);
componentMap.set('CheckboxGroup', Checkbox.Group);
componentMap.set('Cascader', Cascader);
componentMap.set('Slider', Slider);
componentMap.set('Rate', Rate);

componentMap.set('DatePicker', DatePicker);
componentMap.set('MonthPicker', DatePicker.MonthPicker);
componentMap.set('RangePicker', DatePicker.RangePicker);
componentMap.set('WeekPicker', DatePicker.WeekPicker);
componentMap.set('TimePicker', TimePicker);
componentMap.set('DatePickerInFilter', DatePickerInFilter);
componentMap.set('StrengthMeter', StrengthMeter);
componentMap.set('IconPicker', IconPicker);
componentMap.set('InputCountDown', CountdownInput);

componentMap.set('Upload', BasicUpload);
componentMap.set('Divider', Divider);

//사용자 정의 컴포넌트 등록

componentMap.set(
  'JAreaLinkage',
  createAsyncComponent(() => import('./sync/components/JAreaLinkage.vue'))
);
componentMap.set('JSelectPosition', JSelectPosition);
componentMap.set('JSelectUser', JSelectUser);
componentMap.set('JSelectRole', JSelectRole);
componentMap.set('JImageUpload', JImageUpload);
componentMap.set('JDictSelectTag', JDictSelectTag);
componentMap.set('JSelectDept', JSelectDept);
componentMap.set('JAreaSelect', JAreaSelect);
// componentMap.set(
//   'JEditor',
//   createAsyncComponent(() => import('./sync/components/JEditor.vue'))
// );
componentMap.set('JEditor', JEditor);
componentMap.set(
  'JMarkdownEditor',
  createAsyncComponent(() => import('./sync/components/JMarkdownEditor.vue'))
);
componentMap.set('JSelectInput', JSelectInput);
componentMap.set(
  'JCodeEditor',
  createAsyncComponent(() => import('./sync/components/JCodeEditor.vue'))
);
componentMap.set('JCategorySelect', JCategorySelect);
componentMap.set('JSelectMultiple', JSelectMultiple);
componentMap.set('JPopup', JPopup);
componentMap.set('JPopupDict', JPopupDict);
componentMap.set('JSwitch', JSwitch);
componentMap.set('JTreeDict', JTreeDict);
componentMap.set('JInputPop', JInputPop);
componentMap.set(
  'JEasyCron',
  createAsyncComponent(() => import('./sync/components/JEasyCron/EasyCronInput.vue'))
);
componentMap.set('JCheckbox', JCheckbox);
componentMap.set('JInput', JInput);
componentMap.set('JTreeSelect', JTreeSelect);
componentMap.set('JEllipsis', JEllipsis);
componentMap.set('JSelectUserByDept', JSelectUserByDept);
componentMap.set('JUpload', JUpload);
componentMap.set('JSearchSelect', JSearchSelect);
componentMap.set('JAddInput', JAddInput);
componentMap.set('JRangeNumber', JRangeNumber);
componentMap.set('CascaderPcaInFilter', CascaderPcaInFilter);
componentMap.set('UserSelect', UserSelect);
componentMap.set('RangeDate', JRangeDate);
componentMap.set('RangeTime', JRangeTime);
componentMap.set('RoleSelect', RoleSelectInput);
componentMap.set('JInputSelect', JInputSelect);



export function add(compName: ComponentType, component: Component) {
  componentMap.set(compName, component);
}

export function del(compName: ComponentType) {
  componentMap.delete(compName);
}

export { componentMap };
