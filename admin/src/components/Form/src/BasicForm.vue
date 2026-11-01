<template>
  <Form v-bind="getBindValue" :class="getFormClass" ref="formElRef" :model="formModel" @keypress.enter="handleEnterPress">
    <Row v-bind="getRow">
      <slot name="formHeader"></slot>
      <template v-for="schema in getSchema" :key="schema.field">
        <FormItem
          :tableAction="tableAction"
          :formActionType="formActionType"
          :schema="schema"
          :formProps="getProps"
          :allDefaultValues="defaultValueRef"
          :formModel="formModel"
          :setFormModel="setFormModel"
          :validateFields="validateFields"
          :clearValidate="clearValidate"
          v-auth="schema.auth"
        >
          <template #[item]="data" v-for="item in Object.keys($slots)">
            <slot :name="item" v-bind="data || {}"></slot>
          </template>
        </FormItem>
      </template>

      <FormAction v-bind="getFormActionBindProps" @toggle-advanced="handleToggleAdvanced">
        <template #[item]="data" v-for="item in ['resetBefore', 'submitBefore', 'advanceBefore', 'advanceAfter']">
          <slot :name="item" v-bind="data || {}"></slot>
        </template>
      </FormAction>
      <slot name="formFooter"></slot>
    </Row>
  </Form>
</template>
<script lang="ts">
  import type { FormActionType, FormProps, FormSchema } from './types/form';
  import type { AdvanceState } from './types/hooks';
  import type { Ref } from 'vue';

  import { defineComponent, reactive, ref, computed, unref, onMounted, watch, nextTick } from 'vue';
  import { Form, Row } from 'ant-design-vue';
  import FormItem from './components/FormItem.vue';
  import FormAction from './components/FormAction.vue';

  import { dateItemType } from './helper';
  import { dateUtil } from '/@/utils/dateUtil';

  // import { cloneDeep } from 'lodash-es';
  import { deepMerge } from '/@/utils';

  import { useFormValues } from './hooks/useFormValues';
  import useAdvanced from './hooks/useAdvanced';
  import { useFormEvents } from './hooks/useFormEvents';
  import { createFormContext } from './hooks/useFormContext';
  import { useAutoFocus } from './hooks/useAutoFocus';
  import { useModalContext } from '/@/components/Modal';

  import { basicProps } from './props';
  import componentSetting from '/@/settings/componentSetting';

  import { useDesign } from '/@/hooks/web/useDesign';
  import dayjs from 'dayjs';
  import { useDebounceFn } from '@vueuse/core';

  export default defineComponent({
    name: 'BasicForm',
    components: { FormItem, Form, Row, FormAction },
    props: basicProps,
    emits: ['advanced-change', 'reset', 'submit', 'register'],
    setup(props, { emit, attrs }) {
      const formModel = reactive<Recordable>({});
      const modalFn = useModalContext();

      const advanceState = reactive<AdvanceState>({
        // 기본값은 축소된 상태입니다.
        isAdvanced: false,
        hideAdvanceBtn: true,
        isLoad: false,
        actionSpan: 6,
      });

      const defaultValueRef = ref<Recordable>({});
      const isInitedDefaultRef = ref(false);
      const propsRef = ref<Partial<FormProps>>({});
      const schemaRef = ref<Nullable<FormSchema[]>>(null);
      const formElRef = ref<Nullable<FormActionType>>(null);

      const { prefixCls } = useDesign('basic-form');

      // Get the basic configuration of the form
      const getProps = computed((): FormProps => {
        let mergeProps = { ...props, ...unref(propsRef) } as FormProps;
        if (mergeProps.labelWidth) {
          mergeProps.labelCol = undefined;
        }
        if (mergeProps.layout === 'inline' || mergeProps.layout === 'vertical') {
          if (mergeProps.labelCol === componentSetting.form.labelCol) {
            mergeProps.labelCol = undefined;
          }
          if (mergeProps.wrapperCol === componentSetting.form.wrapperCol) {
            mergeProps.wrapperCol = undefined;
          }
        }
        return mergeProps;
      });

      const getFormClass = computed(() => {
        return [
          prefixCls,
          {
            [`${prefixCls}--compact`]: unref(getProps).compact,
            'sync-form-detail-effect': unref(getProps).disabled
          },
        ];
      });

      // Get uniform row style and Row configuration for the entire form
      const getRow = computed((): Recordable => {
        const { baseRowStyle = {}, rowProps } = unref(getProps);
        return {
          style: baseRowStyle,
          ...rowProps,
        };
      });

      const getBindValue = computed(() => ({ ...attrs, ...props, ...unref(getProps) } as Recordable));

      const getSchema = computed((): FormSchema[] => {
        const schemas: FormSchema[] = unref(schemaRef) || (unref(getProps).schemas as any);
        for (const schema of schemas) {
          const { defaultValue, component, componentProps } = schema;
          // handle date type
          if (defaultValue && dateItemType.includes(component)) {
            let valueFormat:string = "";
            if(componentProps){
              valueFormat = componentProps?.valueFormat;
            }
            if(!valueFormat){
              console.warn("ValueFormat이 구성되지 않아 형식 오류가 발생할 수 있습니다!");
            }
            if (!Array.isArray(defaultValue)) {
              if(valueFormat){
                // schema.defaultValue = dateUtil(defaultValue).format(valueFormat);
                schema.defaultValue = dateUtil(defaultValue, valueFormat).format(valueFormat);
              }else{
                schema.defaultValue = dateUtil(defaultValue);
              }
            } else {
              const def: dayjs.Dayjs[] = [];
              defaultValue.forEach((item) => {
                if(valueFormat){
                  def.push(dateUtil(item, valueFormat).format(valueFormat));
                }else{
                  def.push(dateUtil(item));
                }
              });
              def.forEach((item, index) => {
                defaultValue[index] = item;
              });
            }
          }
        }
        if (unref(getProps).showAdvancedButton) {
          return schemas.filter((schema) => schema.component !== 'Divider') as FormSchema[];
        } else {
          return schemas as FormSchema[];
        }
      });

      const { handleToggleAdvanced } = useAdvanced({
        advanceState,
        emit,
        getProps,
        getSchema,
        formModel,
        defaultValueRef,
      });

      const { handleFormValues, initDefault } = useFormValues({
        getProps,
        defaultValueRef,
        getSchema,
        formModel,
      });

      useAutoFocus({
        getSchema,
        getProps,
        isInitedDefault: isInitedDefaultRef,
        formElRef: formElRef as Ref<FormActionType>,
      });

      const {
        handleSubmit,
        setFieldsValue,
        clearValidate,
        validate,
        validateFields,
        getFieldsValue,
        updateSchema,
        resetSchema,
        appendSchemaByField,
        removeSchemaByFiled,
        resetFields,
        scrollToField,
      } = useFormEvents({
        emit,
        getProps,
        formModel,
        getSchema,
        defaultValueRef,
        formElRef: formElRef as Ref<FormActionType>,
        schemaRef: schemaRef as Ref<FormSchema[]>,
        handleFormValues,
      });

      createFormContext({
        resetAction: resetFields,
        submitAction: handleSubmit,
      });

      watch(
        () => unref(getProps).model,
        () => {
          const { model } = unref(getProps);
          if (!model) return;
          setFieldsValue(model);
        },
        {
          immediate: true,
        }
      );

      watch(
        () => unref(getProps).schemas,
        (schemas) => {
          resetSchema(schemas ?? []);
        }
      );

      watch(
        () => getSchema.value,
        (schema) => {
          nextTick(() => {
            //  Solve the problem of modal adaptive height calculation when the form is placed in the modal
            modalFn?.redoModalHeight?.();
          });
          if (unref(isInitedDefaultRef)) {
            return;
          }
          if (schema?.length) {
            initDefault();
            isInitedDefaultRef.value = true;
          }
        }
      );

      async function setProps(formProps: Partial<FormProps>): Promise<void> {
        propsRef.value = deepMerge(unref(propsRef) || {}, formProps);
      }

      const onFormSubmitWhenChange = useDebounceFn(handleSubmit, 300);
      function setFormModel(key: string, value: any) {
        formModel[key] = value;
        // const { validateTrigger } = unref(getBindValue);
        // if (!validateTrigger || validateTrigger === 'change') {
        //   validateFields([key]).catch((_) => {});
        // }
        if(props.autoSearch === true){
          onFormSubmitWhenChange();
        }
      }

      function handleEnterPress(e: KeyboardEvent) {
        const { autoSubmitOnEnter } = unref(getProps);
        if (!autoSubmitOnEnter) return;
        if (e.key === 'Enter' && e.target && e.target instanceof HTMLElement) {
          const target: HTMLElement = e.target as HTMLElement;
          if (target && target.tagName && target.tagName.toUpperCase() == 'INPUT') {
            handleSubmit();
          }
        }
      }

      const formActionType: Partial<FormActionType> = {
        getFieldsValue,
        setFieldsValue,
        resetFields,
        updateSchema,
        resetSchema,
        setProps,
        getProps,
        removeSchemaByFiled,
        appendSchemaByField,
        clearValidate,
        validateFields,
        validate,
        submit: handleSubmit,
        scrollToField: scrollToField,
      };

      onMounted(() => {
        initDefault();
        emit('register', formActionType);
      });

      return {
        getBindValue,
        handleToggleAdvanced,
        handleEnterPress,
        formModel,
        defaultValueRef,
        advanceState,
        getRow,
        getProps,
        formElRef,
        getSchema,
        formActionType: formActionType as any,
        setFormModel,
        getFormClass,
        getFormActionBindProps: computed((): Recordable => ({ ...getProps.value, ...advanceState })),
        ...formActionType,
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-basic-form';

  .@{prefix-cls} {
    .ant-form-item {
      &-label label::after {
        margin: 0 6px 0 2px;
      }

      &-with-help {
        margin-bottom: 0;
      }
      //&:not(.ant-form-item-with-help) {
      //  margin-bottom: 24px;
      //}
      &-has-error {
        margin-bottom: 24px;
      }
      &.suffix-item {
        .ant-form-item-children {
          display: flex;
        }

        .ant-form-item-control {
          margin-top: 4px;
        }

        .suffix {
          display: inline-flex;
          padding-left: 6px;
          margin-top: 1px;
          line-height: 1;
          align-items: center;
        }
      }
    }
    /*【형태를 아름답게】Form의 글꼴 크기를 더 작은 크기로 변경*/
/*    .ant-form-item-label > label{
      font-size: 13px;
    }
    .ant-form-item .ant-select {
      font-size: 13px;
    }
    .ant-select-item-option-selected {
      font-size: 13px;
    }
    .ant-select-item-option-content {
      font-size: 13px;
    }
    .ant-input {
      font-size: 13px;
    }*/
    /*【형태를 아름답게】Form의 글꼴 크기를 더 작은 크기로 변경*/

    .ant-form-explain {
      font-size: 14px;
    }

    &--compact {
      .ant-form-item {
        margin-bottom: 8px !important;
      }
    }
    &.ant-form-inline {
      & > .ant-row {
        .ant-col { width:auto !important; }
      }
    }
  }
</style>
