<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose width="550px" :title="getTitle" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';
  import { BasicForm, useForm } from '/src/components/Form';
  import { formSchema } from '../category.data';
  import { loadTreeData, saveOrUpdateDict } from '../category.api';
  // 방출 받기
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  const expandedRowKeys = ref([]);
  const treeData = ref([]);
  const isSubAdd = ref(false);
  //Form 구성
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
    labelCol: {
      xs: { span: 24 },
      sm: { span: 4 },
    },
    wrapperCol: {
      xs: { span: 24 },
      sm: { span: 18 },
    },
  });
  //Form 할당
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Form 재설정
    await resetFields();
    expandedRowKeys.value = [];
    setModalProps({ confirmLoading: false, minHeight: 80 });
    isUpdate.value = !!data?.isUpdate;
    isSubAdd.value = !data?.isUpdate && data.record && data.record.id;
    if (data?.record) {
      //Form 할당
      await setFieldsValue({
        ...data.record,
      });
    }
    //상위 노드 트리 정보
    treeData.value = await loadTreeData({ async: false, pcode: '' });
    updateSchema({
      field: 'pid',
      componentProps: { treeData },
    });
  });
  //제목 설정
  const getTitle = computed(() => (!unref(isUpdate) ? '분류 추가' : '분류 편집'));

  /**
   * pid를 기반으로 확장된 노드 조회.
   * @param pid
   * @param arr
   */
  function getExpandKeysByPid(pid, arr) {
    if (pid && arr && arr.length > 0) {
      for (let i = 0; i < arr.length; i++) {
        if (arr[i].key == pid && unref(expandedRowKeys).indexOf(pid) < 0) {
          //동일한 레벨.
          getSameLevelExpandKeysByPid(arr[i]);
          expandedRowKeys.value.push(arr[i].key);
          getExpandKeysByPid(arr[i]['parentId'], unref(treeData));
        } else {
          getExpandKeysByPid(pid, arr[i].children);
        }
      }
    }
  }
  //Form 제출 이벤트
  async function handleSubmit() {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Form 제출
      await saveOrUpdateDict(values, isUpdate.value);
      //팝업 창 닫기
      closeModal();
      //확장된 노드 정보
      await getExpandKeysByPid(values['pid'], unref(treeData));
      //목록 새로 고침(isUpdate: 편집 여부; 값: Form 정보; ExpandedArr: 확장된 노드 정보)
      emit('success', { isUpdate: unref(isUpdate), isSubAdd:unref(isSubAdd), values: { ...values }, expandedArr: unref(expandedRowKeys).reverse() });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  /**
   * 같은 레벨의 ID와 같은 레벨의 하위 ID를 가져옵니다.
   */
  function getSameLevelExpandKeysByPid(arr) {
    if (arr.children && arr.children.length > 0) {
      for (const children of arr.children) {
        if (unref(expandedRowKeys).indexOf(children.key) < 0 && children.children && children.children.length > 0) {
          getSameLevelExpandKeysByPid(children);
          expandedRowKeys.value.push(children.key);
        }
      }
    }
  }
</script>
