<template>
  <a-spin :spinning="loading">
    <BasicForm @register="registerForm" />
    <div class="j-box-bottom-button offset-20" style="margin-top: 30px">
      <div class="j-box-bottom-button-float" :class="[`${prefixCls}`]">
        <a-button class="rounded-md" preIcon="ant-design:sync-outlined" @click="onReset">초기화</a-button>
        <a-button type="primary" class="rounded-md" preIcon="ant-design:save-filled" @click="onSubmit">저장</a-button>
      </div>
    </div>
  </a-spin>
</template>

<script lang="ts" setup>
  import { watch, computed, inject, ref, unref, onMounted } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { saveOrUpdateDepart } from '../depart.api';
  import { useBasicFormSchema, orgCategoryOptions } from '../depart.data';
  import { useDesign } from '/@/hooks/web/useDesign';

  const { prefixCls } = useDesign('j-depart-form-content');

  const emit = defineEmits(['success']);
  const props = defineProps({
    data: { type: Object, default: () => ({}) },
    rootTreeData: { type: Array, default: () => [] },
  });
  const loading = ref<boolean>(false);
  // 현재 업데이트 모드인지 여부
  const isUpdate = ref<boolean>(true);
  // 현재 팝업 데이터
  const model = ref<object>({});

  //등록 Form
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    schemas: useBasicFormSchema().basicFormSchema,
    showActionButtonGroup: false,
  });

  const categoryOptions = computed(() => {
    if (!!props?.data?.parentId) {
      return orgCategoryOptions.child;
    } else {
      return orgCategoryOptions.root;
    }
  });

  onMounted(() => {
    // 필드 비활성화
    updateSchema([
      { field: 'parentId', componentProps: { disabled: true } },
      { field: 'orgCode', componentProps: { disabled: true } },
    ]);
    // 데이터 변경, Form 다시 작성
    watch(
      () => props.data,
      async () => {
        let record = unref(props.data);
        if (typeof record !== 'object') {
          record = {};
        }
        model.value = record;
        await resetFields();
        await setFieldsValue({ ...record });
      },
      { deep: true, immediate: true }
    );
    // 상위 부서 옵션 업데이트
    watch(
      () => props.rootTreeData,
      async () => {
        updateSchema([
          {
            field: 'parentId',
            componentProps: { treeData: props.rootTreeData },
          },
        ]);
      },
      { deep: true, immediate: true }
    );
    // orgCategory 옵션 듣기 및 변경
    watch(
      categoryOptions,
      async () => {
        updateSchema([
          {
            field: 'orgCategory',
            componentProps: { options: categoryOptions.value },
          },
        ]);
      },
      { immediate: true }
    );
  });

  // Form 재설정
  async function onReset() {
    await resetFields();
    await setFieldsValue({ ...model.value });
  }

  // 이벤트 제출
  async function onSubmit() {
    try {
      loading.value = true;
      let values = await validate();
      values = Object.assign({}, model.value, values);
      //Form 제출
      await saveOrUpdateDepart(values, isUpdate.value);
      //목록 새로 고침
      emit('success');
      Object.assign(model.value, values);
    } finally {
      loading.value = false;
    }
  }
</script>
<style lang="less">

  @prefix-cls: ~'@{namespace}-j-depart-form-content';
  /*다크모드와 호환 가능*/
  .@{prefix-cls} {
    background: @component-background;
    border-top: 1px solid @border-color-base;
  }
  /*end 다크모드와 호환 가능*/
</style>
