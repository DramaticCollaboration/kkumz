<template>
  <div>
    <template v-if="hasFile" v-for="(file, fileKey) of [innerFile || {}]" :key="fileKey">
      <div style="position: relative">
        <a-tooltip v-if="file.status === 'uploading'" :title="t('appUi.k5d4f78c0', { p0: Math.floor(file.percent) })">
          <LoadingOutlined />
          <span style="margin-left: 5px">{{ t('appUi.k7cc8ef1f') }}</span>
        </a-tooltip>

        <a-tooltip v-else-if="file.status === 'done'" :title="file.name">
          <Icon icon="ant-design:paper-clip" />
          <span style="margin-left: 5px">{{ ellipsisFileName }}</span>
        </a-tooltip>

        <a-tooltip v-else :title="file.message || t('appUi.ka40a0075')">
          <Icon icon="ant-design:exclamation-circle" style="color: red" />
          <span style="margin-left: 5px">{{ ellipsisFileName }}</span>
        </a-tooltip>

        <Dropdown :trigger="['click']" placement="bottomRight" style="margin-left: 10px">
          <a-tooltip :title="t('appUi.k9d9bf438')">
            <Icon v-if="file.status !== 'uploading'" icon="ant-design:setting" style="cursor: pointer" />
          </a-tooltip>
          <template #overlay>
            <a-menu>
              <a-menu-item v-if="originColumn.allowDownload !== false" @click="handleClickDownloadFile">
                <span><Icon icon="ant-design:download" />{{ t('appUi.k9c9d1b79') }}</span>
              </a-menu-item>
              <a-menu-item :disabled="cellProps.disabled" v-if="originColumn.allowRemove !== false" @click="handleClickDeleteFile">
                <span><Icon icon="ant-design:delete" />{{ t('appUi.kfff2bc8f') }}</span>
              </a-menu-item>
              <a-menu-item :disabled="cellProps.disabled" @click="handleMoreOperation">
                <span><Icon icon="ant-design:bars" />{{ t('appUi.kcc38553e') }}</span>
              </a-menu-item>
            </a-menu>
          </template>
        </Dropdown>
      </div>
    </template>

    <a-upload
      v-if="!cellProps.disabledTable"
      v-show="!hasFile"
      name="file"
      :data="{ isup: 1 }"
      :multiple="false"
      :action="uploadAction"
      :headers="uploadHeaders"
      :showUploadList="false"
      v-bind="cellProps"
      @change="handleChangeUpload"
    >
      <a-button preIcon="ant-design:upload">{{ originColumn.btnText || t('appUi.kf4d4465f') }}</a-button>
    </a-upload>
    <JUploadModal :value="modalValue" @register="registerModel" @change="onModalChange" />
  </div>
</template>

<script lang="ts">
  import { useI18n } from '/@/hooks/web/useI18n';
  import { defineComponent } from 'vue';
  import { UploadTypeEnum } from '/@/components/Form/src/sync/components/JUpload';
  import { JVxeComponent } from '/@/components/sync/JVxeTable/types';
  import { useJVxeCompProps } from '/@/components/sync/JVxeTable/hooks';
  import { useFileCell, enhanced, components } from '../hooks/useFileCell';

  export default defineComponent({
    name: 'JVxeFileCell',
    components: components,
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { t } = useI18n();
      return { t, ...useFileCell(props, UploadTypeEnum.file) };
    },
    // 【구성요소 강화】자세한 내용은 참고 사항을 참조하세요.：JVxeComponent.Enhanced
    enhanced: enhanced,
  });
</script>

<style scoped lang="less"></style>
