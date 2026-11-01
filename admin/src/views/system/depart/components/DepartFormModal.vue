<template>
  <BasicModal :title="title" :width="800" v-bind="$attrs" @ok="handleOk" @register="registerModal">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { watch, computed, inject, ref, unref, onMounted } from 'vue';

  import { BasicForm, useForm } from '/@/components/Form/index';
  import { BasicModal, useModalInner } from '/@/components/Modal';

  import { saveOrUpdateDepart } from '../depart.api';
  import { useBasicFormSchema, orgCategoryOptions } from '../depart.data';

  const emit = defineEmits(['success', 'register']);
  const props = defineProps({
    rootTreeData: { type: Array, default: () => [] },
  });
  const prefixCls = inject('prefixCls');
  // 현재 업데이트 모드인지 여부
  const isUpdate = ref<boolean>(false);
  // 현재 팝업 데이터
  const model = ref<object>({});
  const title = computed(() => (isUpdate.value ? '편집' : '등록'));

  //등록 Form
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    schemas: useBasicFormSchema().basicFormSchema,
    showActionButtonGroup: false,
  });

  // 등록팝업창
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    await resetFields();
    isUpdate.value = unref(data?.isUpdate);
    // 현재 자녀 추가 여부
    let isChild = unref(data?.isChild);
    let categoryOptions = isChild ? orgCategoryOptions.child : orgCategoryOptions.root;
    // 표시할 필요가 없는 필드 숨기기
    updateSchema([
      {
        field: 'parentId',
        show: isChild,
        componentProps: {
          // 하위 부서를 추가하는 경우 이 필드를 비활성화하세요.
          disabled: isChild,
          treeData: props.rootTreeData,
        },
      },
      {
        field: 'orgCode',
        show: false,
      },
      {
        field: 'orgCategory',
        componentProps: { options: categoryOptions },
      },
    ]);

    let record = unref(data?.record);
    if (typeof record !== 'object') {
      record = {};
    }
    // 기본값 할당
    record = Object.assign(
      {
        departOrder: 0,
        orgCategory: categoryOptions[0].value,
      },
      record
    );
    model.value = record;
    await setFieldsValue({ ...record });
  });

  // 이벤트 제출
  async function handleOk() {
    try {
      setModalProps({ confirmLoading: true });
      let values = await validate();
      //Form 제출
      await saveOrUpdateDepart(values, isUpdate.value);
      //팝업 창 닫기
      closeModal();
      //목록 새로 고침
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
