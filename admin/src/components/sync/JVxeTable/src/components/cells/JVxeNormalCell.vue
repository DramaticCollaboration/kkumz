<template>
  <JVxeReloadEffect :vNode="innerValue" :effect="isEffect" @effectEnd="handleEffectEnd" />
</template>

<script lang="ts">
  import { ref, defineComponent } from 'vue';
  import JVxeReloadEffect from '../JVxeReloadEffect';
  import { JVxeComponent } from '/@/components/sync/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/sync/JVxeTable/hooks';
  import { watch } from 'vue';

  export default defineComponent({
    name: 'JVxeNormalCell',
    components: { JVxeReloadEffect },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const setup = useJVxeComponent(props);
      const { innerValue, row } = setup;

      const reloadEffect = props.renderOptions.reloadEffect;
      const isEffect = ref<boolean>(false);

      watch(
        innerValue,
        () => {
          if (reloadEffect.enabled) {
            if (reloadEffect.isEffect(row.value.id)) {
              isEffect.value = true;
            }
          }
        },
        { immediate: true }
      );

      // 특수효과 종료
      function handleEffectEnd() {
        isEffect.value = false;
        reloadEffect.removeEffect(row.value.id);
      }

      return {
        innerValue,
        isEffect,
        handleEffectEnd,
      };
    },
    enhanced: {
      switches: { editRender: false },
    } as JVxeComponent.EnhancedPartial,
  });
</script>

<style scoped></style>
