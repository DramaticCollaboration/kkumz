<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('appUi.k7cd23eb8')"
    :showCancelBtn="false"
    :showOkBtn="false"
    :maxHeight="500"
  >
    <iframe v-if="hasFrameSrc" :src="frameSrc" class="detail-iframe" :title="t('appUi.kbf510bc6')" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { propTypes } from '/@/utils/propTypes';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const props = defineProps({
    frameSrc: propTypes.string.def(''),
  });

  const hasFrameSrc = computed(() => Boolean(props.frameSrc && props.frameSrc.trim().length > 0));

  const [registerModal] = useModalInner();
</script>

<style scoped lang="less">
  .detail-iframe {
    border: 0;
    width: 100%;
    height: 100%;
    min-height: 500px;
  }
</style>
