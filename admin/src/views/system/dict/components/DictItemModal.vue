<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" width="800px">
    <BasicForm @register="registerForm" >
      <template #itemColor="{ model, field }">
        <div class="item-tool">
          <div
              v-for="(item,index) in Colors"
              :style="{ color: item[0] }"
              :class="model.itemColor===item[0]?'item-active':''"
              class="item-color"
              @click="itemColorClick(item)">
            <div class="item-color-border"></div>
            <div class="item-back" :style="{ background: item[0] }"></div>
          </div>
        </div>
      </template>
    </BasicForm>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { defineProps, ref, computed, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';
  import { BasicForm, useForm } from '/src/components/Form';
  import { itemFormSchema } from '../dict.data';
  import { saveOrUpdateDictItem } from '../dict.api';
  import { Colors } from '/@/utils/dict/DictColors.js'
  
  // 방출 선언
  const emit = defineEmits(['success', 'register']);
  const props = defineProps({ dictId: String });
  const isUpdate = ref(true);
  //Form 구성
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: itemFormSchema,
    showActionButtonGroup: false,
    mergeDynamicData: props,
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
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      //Form 할당
      await setFieldsValue({
        ...data.record,
      });
    }
  });

  //제목 설정
  const getTitle = computed(() => (!unref(isUpdate) ? '등록' : '편집'));

  //Form 제출 이벤트
  async function handleSubmit() {
    try {
      const values = await validate();
      values.dictId = props.dictId;
      setModalProps({ confirmLoading: true });
      //Form 제출
      await saveOrUpdateDictItem(values, isUpdate.value);
      //팝업 창 닫기
      closeModal();
      //목록 새로 고침
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
  
  /**
   * 사전 색상 클릭 이벤트
   * 
   * @param index
   * @param item
   * @param model
   */
  function itemColorClick(item) {
    console.log(item)
    setFieldsValue({ itemColor: item[0] })
  }
  
</script>
<style lang="less" scoped>
   /*사전 색상 구성 스타일 시작*/
  .item-tool{
    display: flex;
    flex-wrap: wrap;
    .item-color{
      width: 18px;
      display: flex;
      justify-content: center;
      cursor: pointer;
      align-items: center;
      margin-right: 10px;
    }
    .item-back{
      width: 18px;
      height: 18px;
      border-radius: 50%;
    }
  }
  .item-color-border{
    visibility: hidden;
  }
  .item-active .item-color-border{
    visibility: visible;
    position: absolute;
    border: 1px solid;
    width: 24px;
    height: 24px;
    border-radius: 50%;
  }
   /*최종 사전 색상 구성 스타일*/
</style>
