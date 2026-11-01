<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :width="860"
    :title="$t('sys.errorLog.modalTitle', '오류 상세')"
    :canFullscreen="true"
    :draggable="true"
    :resizable="true"
    :showCancelBtn="false"
    :okText="$t('common.closeText', '닫기')"
    @ok="closeModal"
  >
    <div class="p-2 error-detail-modal-body">
      <Description :data="info" @register="register" />
    </div>
  </BasicModal>
</template>
<script lang="ts" setup>
  import type { PropType } from 'vue';
  import type { ErrorLogInfo } from '/#/store';
  import { BasicModal, useModalInner } from '/@/components/Modal/index';
  import { Description, useDescription } from '/@/components/Description/index';
  import { $t } from '/@/locales';
  import { getDescSchema } from './data';

  defineProps({
    info: {
      type: Object as PropType<ErrorLogInfo>,
      default: null,
    },
  });

  const [registerModal, { closeModal }] = useModalInner();

  const [register] = useDescription({
    column: 2,
    bordered: true,
    size: 'small',
    schema: getDescSchema(),
  });
</script>

<style scoped>
/* SyncSeries Enterprise Component Styling: DetailModal */
.error-detail-modal-body :deep(.ant-descriptions-item-label) {
  white-space: nowrap;
  word-break: keep-all;
  width: 130px;
  min-width: 110px;
  font-weight: 600;
}

.error-detail-modal-body :deep(.ant-descriptions-item-content) {
  word-break: break-word;
}
</style>
