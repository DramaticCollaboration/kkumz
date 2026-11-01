<script lang="tsx">
  import { NamePath, ValidateOptions } from 'ant-design-vue/lib/form/interface';
  import type { PropType, Ref } from 'vue';
  import type { FormActionType, FormProps } from '../types/form';
  import type { FormSchema } from '../types/form';
  import type { ValidationRule } from 'ant-design-vue/lib/form/Form';
  import type { TableActionType } from '/@/components/Table';
  import { defineComponent, computed, unref, toRefs } from 'vue';
  import { Form, Col, Divider } from 'ant-design-vue';
  import { componentMap } from '../componentMap';
  import { BasicHelp } from '/@/components/Basic';
  import { isBoolean, isFunction, isNull } from '/@/utils/is';
  import { getSlot } from '/@/utils/helper/tsxHelper';
  import { createPlaceholderMessage, setComponentRuleType } from '../helper';
  import { upperFirst, cloneDeep } from 'lodash-es';
  import { useItemLabelWidth } from '../hooks/useLabelWidth';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useAppInject } from '/@/hooks/web/useAppInject';
  import { usePermission } from '/@/hooks/web/usePermission';
  import Middleware from './Middleware.vue';
  import { useLocaleStore } from '@/store/modules/locale';
  export default defineComponent({
    name: 'BasicFormItem',
    inheritAttrs: false,
    props: {
      schema: {
        type: Object as PropType<FormSchema>,
        default: () => ({}),
      },
      formProps: {
        type: Object as PropType<FormProps>,
        default: () => ({}),
      },
      allDefaultValues: {
        type: Object as PropType<Recordable>,
        default: () => ({}),
      },
      formModel: {
        type: Object as PropType<Recordable>,
        default: () => ({}),
      },
      setFormModel: {
        type: Function as PropType<(key: string, value: any) => void>,
        default: null,
      },
      validateFields: {
        type: Function as PropType<(nameList?: NamePath[] | undefined, options?: ValidateOptions) => Promise<any>>,
        default: null,
      },
      tableAction: {
        type: Object as PropType<TableActionType>,
      },
      formActionType: {
        type: Object as PropType<FormActionType>,
      },
      clearValidate: {
        type: Function,
        default: null,
      },
    },
    setup(props, { slots }) {
      const { t } = useI18n();
      const localeStore = useLocaleStore();

      const { schema, formProps } = toRefs(props) as {
        schema: Ref<FormSchema>;
        formProps: Ref<FormProps>;
      };

      const itemLabelWidthProp = useItemLabelWidth(schema, formProps);

      const getValues = computed(() => {
        const { allDefaultValues, formModel, schema } = props;
        const { mergeDynamicData } = props.formProps;
        return {
          field: schema.field,
          model: formModel,
          values: {
            ...mergeDynamicData,
            ...allDefaultValues,
            ...formModel,
          } as Recordable,
          schema: schema,
        };
      });

      const getComponentsProps = computed(() => {
        const { schema, tableAction, formModel, formActionType } = props;
        let { componentProps = {} } = schema;
        if (isFunction(componentProps)) {
          componentProps = componentProps({ schema, tableAction, formModel, formActionType }) ?? {};
        }
        if (schema.component === 'Divider') {
          componentProps = Object.assign({ type: 'horizontal', orientation: 'left', plain: true }, componentProps);
        }
        return componentProps as Recordable;
      });

      const getDisable = computed(() => {
        const { disabled: globDisabled } = props.formProps;
        if (!!globDisabled) {
          return globDisabled;
        }
        const { dynamicDisabled } = props.schema;
        const { disabled: itemDisabled = false } = unref(getComponentsProps);
        let disabled = !!globDisabled || itemDisabled;
        if (isBoolean(dynamicDisabled)) {
          disabled = dynamicDisabled;
        }
        if (isFunction(dynamicDisabled)) {
          disabled = dynamicDisabled(unref(getValues));
        }
        return disabled;
      });

      const getDynamicPropsValue = computed(() => {
        const { dynamicPropsVal, dynamicPropskey } = props.schema;
        if (dynamicPropskey == null) {
          return null;
        } else {
          const { [dynamicPropskey]: itemValue } = unref(getComponentsProps);
          let value = itemValue;
          if (isFunction(dynamicPropsVal)) {
            value = dynamicPropsVal(unref(getValues));
            return value;
          }
        }
      });

      function getShow(): { isShow: boolean; isIfShow: boolean } {
        const { show, ifShow } = props.schema;
        const { showAdvancedButton } = props.formProps;
        const itemIsAdvanced = showAdvancedButton ? (isBoolean(props.schema.isAdvanced) ? props.schema.isAdvanced : true) : true;

        let isShow = true;
        let isIfShow = true;

        if (isBoolean(show)) {
          isShow = show;
        }
        if (isBoolean(ifShow)) {
          isIfShow = ifShow;
        }
        if (isFunction(show)) {
          isShow = show(unref(getValues));
        }
        if (isFunction(ifShow)) {
          isIfShow = ifShow(unref(getValues));
        }
        isShow = isShow && itemIsAdvanced;
        return { isShow, isIfShow };
      }
      let vSwitchArr: any = [],
        prevValidatorArr: any = [];
      const hijackValidator = (rules) => {
        vSwitchArr = [];
        prevValidatorArr = [];
        rules.forEach((item, index) => {
          const fn = item.validator;
          vSwitchArr.push(true);
          prevValidatorArr.push(null);
          if (isFunction(fn)) {
            item.validator = (rule, value, callback) => {
              if (vSwitchArr[index]) {
                vSwitchArr[index] = false;
                setTimeout(() => {
                  vSwitchArr[index] = true;
                }, 100);
                const result = fn(rule, value, callback);
                prevValidatorArr[index] = result;
                return result;
              } else {
                return prevValidatorArr[index];
              }
            };
          }
        });
      };
      function handleRules(): ValidationRule[] {
        const { rules: defRules = [], component, rulesMessageJoinLabel, label, dynamicRules, required, auth, field } = props.schema;
        const { disabled: globDisabled } = props.formProps;
        const { disabled: itemDisabled = false } = unref(getComponentsProps);
        if (!!globDisabled || !!itemDisabled) {
          props.clearValidate(field);
          return [];
        }
        const { hasPermission } = usePermission();
        const { isShow } = getShow();
        if ((auth && !hasPermission(auth)) || !isShow) {
          return [];
        }
        if (isFunction(dynamicRules)) {
          const ruleArr = dynamicRules(unref(getValues)) as ValidationRule[];
          if (required) {
            ruleArr.unshift({ required: true });
          }
          hijackValidator(ruleArr);
          return ruleArr;
        }

        let rules: ValidationRule[] = cloneDeep(defRules) as ValidationRule[];
        const { rulesMessageJoinLabel: globalRulesMessageJoinLabel } = props.formProps;

        const joinLabel = Reflect.has(props.schema, 'rulesMessageJoinLabel') ? rulesMessageJoinLabel : globalRulesMessageJoinLabel;
        const defaultMsg =
          localeStore.getLocale == 'ko_KR'
            ? `${joinLabel ? label : ''} ` + createPlaceholderMessage(component)
            : createPlaceholderMessage(component) + `${joinLabel ? label : ''}`;

        function validator(rule: any, value: any) {
          const msg = rule.message || defaultMsg;
          if (value === undefined || isNull(value)) {
            // 널값
            return Promise.reject(msg);
          } else if (Array.isArray(value) && value.length === 0) {
            // 배열 유형
            return Promise.reject(msg);
          } else if (typeof value === 'string' && value.trim() === '') {
            // 빈 문자열
            return Promise.reject(msg);
          } else if (
            typeof value === 'object' &&
            Reflect.has(value, 'checked') &&
            Reflect.has(value, 'halfChecked') &&
            Array.isArray(value.checked) &&
            Array.isArray(value.halfChecked) &&
            value.checked.length === 0 &&
            value.halfChecked.length === 0
          ) {
            // 비연관 선택을 위한 트리 구성요소
            return Promise.reject(msg);
          }
          return Promise.resolve();
        }

        const getRequired = isFunction(required) ? required(unref(getValues)) : required;

        if ((!rules || rules.length === 0) && getRequired) {
          rules = [{ required: getRequired, validator }];
        }

        const requiredRuleIndex: number = rules.findIndex((rule) => Reflect.has(rule, 'required') && !Reflect.has(rule, 'validator'));

        if (requiredRuleIndex !== -1) {
          const rule = rules[requiredRuleIndex];
          const { isShow } = getShow();
          if (!isShow) {
            rule.required = false;
          }
          if (component) {
            //https://github.com/vbenjs/vue-vben-admin/pull/3082 github 복구 원본 텍스트
            /*if (!Reflect.has(rule, 'type')) {
              rule.type = component === 'InputNumber' ? 'number' : 'string';
            }*/

            rule.message = rule.message || defaultMsg;

            if (component.includes('Input') || component.includes('Textarea')) {
              rule.whitespace = true;
            }
            const valueFormat = unref(getComponentsProps)?.valueFormat;
            setComponentRuleType(rule, component, valueFormat);
          }
        }

        // Maximum input length rule check
        const characterInx = rules.findIndex((val) => val.max);
        if (characterInx !== -1 && !rules[characterInx].validator) {
          rules[characterInx].message = rules[characterInx].message || t('component.form.maxTip', [rules[characterInx].max] as Recordable);
        }
        rules.forEach((item) => {
          if (typeof item.pattern === 'string') {
            try {
              const reg = new Function('item', `return ${item.pattern}`)(item);
              if (Object.prototype.toString.call(reg) === '[object RegExp]') {
                item.pattern = reg;
              } else {
                item.pattern = new RegExp(item.pattern);
              }
            } catch (error) {
              item.pattern = new RegExp(item.pattern);
            }
          }
        });
        hijackValidator(rules);
        return rules;
      }

      function renderComponent() {
        const { renderComponentContent, component, field, changeEvent = 'change', valueField, componentProps, dynamicRules } = props.schema;

        const isCheck = component && ['Switch', 'Checkbox'].includes(component);
        let isTrim = false;
        if (component === 'Input' && componentProps && componentProps.trim) {
          isTrim = true;
        }
        const eventKey = `on${upperFirst(changeEvent)}`;
        const on = {
          [eventKey]: (...args: Nullable<Recordable>[]) => {
            const [e] = args;
            if (propsData[eventKey]) {
              propsData[eventKey](...args);
            }
            const target = e ? e.target : null;
            let value;
            if (target) {
              if (isCheck) {
                value = target.checked;
              } else {
                value = isTrim ? target.value.trim() : target.value;
              }
            } else {
              value = e;
            }
            props.setFormModel(field, value);
            props.validateFields([field]).catch((_) => {});
          },
          // onBlur: () => {
          //   props.validateFields([field], { triggerName: 'blur' }).catch((_) => {});
          // },
        };
        const Comp = componentMap.get(component) as ReturnType<typeof defineComponent>;

        const { autoSetPlaceHolder, size } = props.formProps;
        const propsData: Recordable = {
          allowClear: true,
          getPopupContainer: (trigger: Element) => {
            return trigger?.parentNode;
          },
          size,
          ...unref(getComponentsProps),
          disabled: unref(getDisable),
        };
        const dynamicPropskey = props.schema.dynamicPropskey;
        if (dynamicPropskey) {
          propsData[dynamicPropskey] = unref(getDynamicPropsValue);
        }

        const isCreatePlaceholder = !propsData.disabled && autoSetPlaceHolder;
        // RangePicker 장소는 배열입니다.
        if (isCreatePlaceholder && component !== 'RangePicker' && component) {
          //자동으로 자리 표시자 설정
          propsData.placeholder =
            unref(getComponentsProps)?.placeholder ||
            (localeStore.getLocale == 'ko_KR'
              ? props.schema.label + createPlaceholderMessage(component)
              : createPlaceholderMessage(component) + props.schema.label);
        }
        propsData.codeField = field;
        propsData.formValues = unref(getValues);

        const bindValue: Recordable = {
          [valueField || (isCheck ? 'checked' : 'value')]: props.formModel[field],
        };

        const compAttr: Recordable = {
          ...propsData,
          ...on,
          ...bindValue,
        };

        if (!renderComponentContent) {
          return <Comp {...compAttr} />;
        }
        const compSlot = isFunction(renderComponentContent)
          ? { ...renderComponentContent(unref(getValues)) }
          : {
              default: () => renderComponentContent,
            };
        return <Comp {...compAttr}>{compSlot}</Comp>;
      }

      /**
       *렌더 라벨
       * @updateBy:zyf
       */
      function renderLabelHelpMessage() {
        //라벨 너비는 사용자 정의를 지원합니다
        const { label, helpMessage, helpComponentProps, subLabel, labelLength } = props.schema;
        let showLabel: string = label + '';
        if (labelLength) {
          showLabel = showLabel.substr(0, labelLength);
        }
        const titleObj = { title: label };
        const renderLabel = subLabel ? (
          <span>
            {label} <span class="text-secondary">{subLabel}</span>
          </span>
        ) : labelLength ? (
          <label {...titleObj}>{showLabel}</label>
        ) : (
          label
        );
        const getHelpMessage = isFunction(helpMessage) ? helpMessage(unref(getValues)) : helpMessage;
        if (!getHelpMessage || (Array.isArray(getHelpMessage) && getHelpMessage.length === 0)) {
          return renderLabel;
        }
        return (
          <span>
            {renderLabel}
            <BasicHelp placement="top" class="mx-1" text={getHelpMessage} {...helpComponentProps} />
          </span>
        );
      }

      function renderItem() {
        const { itemProps, slot, render, field, suffix, component } = props.schema;
        const { labelCol, wrapperCol } = unref(itemLabelWidthProp);
        const { colon } = props.formProps;

        if (component === 'Divider') {
          return (
            <Col span={24}>
              <Divider {...unref(getComponentsProps)}>{renderLabelHelpMessage()}</Divider>
            </Col>
          );
        } else {
          const getContent = () => {
            return slot ? getSlot(slots, slot, unref(getValues)) : render ? render(unref(getValues)) : renderComponent();
          };

          const showSuffix = !!suffix;
          const getSuffix = isFunction(suffix) ? suffix(unref(getValues)) : suffix;

          return (
            <Form.Item
              name={field}
              colon={colon}
              class={{ 'suffix-item': showSuffix }}
              {...(itemProps as Recordable)}
              label={renderLabelHelpMessage()}
              rules={handleRules()}
              validateFirst={true}
              labelCol={labelCol}
              wrapperCol={wrapperCol}
            >
              <div style="display:flex">
                {/* author: sunjianlei for: 【VUEN-744】여기에 너비를 추가하세요.: 100%; 컴포넌트 너비가 FormItem을 초과하는 것을 방지하고 싶기 때문입니다. */}
                {/* update-begin--author:liaozhiyang---date:20240510---for：【TV360X-719】Form 유효성 검사에 실패한 항목은 표시 영역으로 스크롤됩니다. */}
                <Middleware>{getContent()}</Middleware>
                {/* update-end--author:liaozhiyang---date:20240510---for：【TV360X-719】Form 유효성 검사에 실패한 항목은 표시 영역으로 스크롤됩니다. */}
                {showSuffix && <span class="suffix">{getSuffix}</span>}
              </div>
            </Form.Item>
          );
        }
      }

      return () => {
        const { colProps = {}, colSlot, renderColContent, component, slot, render } = props.schema;
        if (!componentMap.has(component) && !slot && !colSlot && !render && !renderColContent) {
          return null;
        }

        const { baseColProps = {} } = props.formProps;
        const { getIsMobile } = useAppInject();
        let realColProps: Recordable = { span: 24, ...baseColProps, ...colProps };
        if (unref(getIsMobile)) {
          realColProps = { ...realColProps, span: 24, xs: 24, sm: 24, md: 24 };
        } else if (colProps['span']) {
          ['xs', 'sm', 'md', 'lg', 'xl', 'xxl'].forEach((name) => delete realColProps[name]);
        }
        const { isIfShow, isShow } = getShow();
        const values = unref(getValues);

        const getContent = () => {
          return colSlot ? getSlot(slots, colSlot, values) : renderColContent ? renderColContent(values) : renderItem();
        };

        return (
          isIfShow && (
            <Col {...realColProps} v-show={isShow}>
              {getContent()}
            </Col>
          )
        );
      };
    },
  });
</script>
