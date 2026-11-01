<template>
  <a-button type="primary" v-if="hasExportAuth() && config.export" preIcon="ant-design:export-outlined" @click="onExportXls()"> Export</a-button>
  <a-upload name="file" :showUploadList="false" v-if="hasImportAuth() && config.import" :customRequest="(file) => onImportXls(file)">
    <a-button type="primary" preIcon="ant-design:import-outlined">Import</a-button>
  </a-upload>
</template>

<script lang="ts" setup name="ExcelButton">
  import { PropType } from 'vue';
  import { usePermission } from '/@/hooks/web/usePermission';
  import { useMethods } from '/@/hooks/system/useMethods';
  import { useMessage } from '/@/hooks/web/useMessage';

  // Excel 방법에 필요한 매개변수를 정의합니다.
  interface ExcelConfig {
    // 구성 내보내기
    exportConfig: {
      url: string;
      // 내보내기 파일 이름
      name?: string | (() => string);
      //버튼 권한
      auth?: string | string[];
    };
    // 구성 가져오기
    importConfig: {
      url: string;
      // 내보내기 성공 후 콜백
      success?: (fileInfo?: any) => void;
      //버튼 권한
      auth?: string | string[];
    };
  }
  /**
   * 구성요소 매개변수 정의
   */
  const props = defineProps({
    config: {
      type: Object as PropType<ExcelConfig>,
      default: null,
    },
  });
  //버튼 권한 문제
  const { hasPermission } = usePermission();
  //가져오기 및 내보내기 방법
  const { handleExportXls, handleImportXls } = useMethods();

  const $message = useMessage();
  // 엑셀 내보내기
  function onExportXls() {
    let { url, name } = props.config?.export ?? {};
    if (url) {
      let title = typeof name === 'function' ? name() : name;
      return handleExportXls(title as string, url);
    } else {
      $message.createMessage.warn('내보내기.url 매개변수가 전달되지 않았습니다.');
      return Promise.reject();
    }
  }

  // 엑셀 가져오기
  function onImportXls(file) {
    let { url, success } = props.config?.import ?? {};
    if (url) {
      return handleImportXls(file, url, success);
    } else {
      $message.createMessage.warn('import.url 매개변수가 전달되지 않았습니다.');
      return Promise.reject();
    }
  }

  // 가져오기 버튼 권한
  function hasImportAuth() {
    let auth = props.config?.import?.auth;
    return auth && auth.length > 0 ? hasPermission(auth) : true;
  }

  // 내보내기 버튼 권한
  function hasExportAuth() {
    let auth = props.config?.export?.auth;
    return auth && auth.length > 0 ? hasPermission(auth) : true;
  }
</script>

<style scoped lang="less"></style>
