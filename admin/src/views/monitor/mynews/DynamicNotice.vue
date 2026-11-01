<template>
  <component :is="currentModal" :formData="formData" v-model:visible="modalVisible"></component>
</template>
<script setup lang="ts" name="dynamic-notice">
  import { ref, shallowRef, ComponentOptions, nextTick, defineAsyncComponent } from 'vue';
  const props = defineProps({
    path: { type: String, default: '' },
    formData: { type: Object, default: {} },
  });
  const modalVisible = ref<Boolean>(false);
  const currentModal = shallowRef<Nullable<ComponentOptions>>(null);
  const formData = ref<any>(props.formData);

  const componentType = {
  };

  /**
   * 후속 부품 교체 및 값 전달 이벤트
   */
  function detail() {
    setTimeout(() => {
      if (props.path) {
        nextTick(() => {
          currentModal.value = componentType[props.path];
          formData.value = props.formData;
          modalVisible.value = true;
        });
      }
    }, 200);
  }

  defineExpose({
    detail,
  });
</script>
