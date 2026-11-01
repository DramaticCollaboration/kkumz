<template>
  <div class="clearfix">
    <a-upload
      :listType="listType"
      accept="image/*"
      :multiple="multiple"
      :action="uploadUrl"
      :headers="headers"
      :data="{ biz: bizPath }"
      v-model:fileList="uploadFileList"
      :beforeUpload="beforeUpload"
      :disabled="disabled"
      @change="handleChange"
      @preview="handlePreview"
    >
      <div v-if="uploadVisible">
        <div v-if="listType == 'picture-card'">
          <LoadingOutlined v-if="loading" />
          <UploadOutlined v-else />
          <div class="ant-upload-text">{{ text }}</div>
        </div>
        <a-button v-if="listType == 'picture'" :disabled="disabled">
          <UploadOutlined></UploadOutlined>
          {{ text }}
        </a-button>
      </div>
    </a-upload>
    <a-modal :open="previewVisible" :footer="null" @cancel="handleCancel()">
      <img alt="example" style="width: 100%" :src="previewImage" />
    </a-modal>
  </div>
</template>
<script lang="ts">
  import { defineComponent, PropType, ref, reactive, watchEffect, computed, unref, watch, onMounted, nextTick } from 'vue';
  import { LoadingOutlined, UploadOutlined } from '@ant-design/icons-vue';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getFileAccessHttpUrl, getHeaders, getRandom } from '/@/utils/common/compUtils';
  import { uploadUrl } from '/@/api/common/api';
  import { getToken } from '/@/utils/auth';

  const { createMessage, createErrorModal } = useMessage();
  export default defineComponent({
    name: 'JImageUpload',
    components: { LoadingOutlined, UploadOutlined },
    inheritAttrs: false,
    props: {
      //바인딩 값
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      //버튼 텍스트
      listType: {
        type: String,
        required: false,
        default: 'picture-card',
      },
      //버튼 텍스트
      text: {
        type: String,
        required: false,
        default: '업로드',
      },
      //이 속성은 파일 업로드의 비즈니스 경로를 제어하는 ​​데 사용됩니다.
      bizPath: {
        type: String,
        required: false,
        default: 'temp',
      },
      //비활성화 여부
      disabled: {
        type: Boolean,
        required: false,
        default: false,
      },
      //업로드 수량
      fileMax: {
        type: Number,
        required: false,
        default: 1,
      },
    },
    emits: ['options-change', 'change', 'update:value'],
    setup(props, { emit, refs }) {
      const emitData = ref<any[]>([]);
      const attrs = useAttrs();
      const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      //파일 이름 가져오기
      const getFileName = (path) => {
        if (path.lastIndexOf('\\') >= 0) {
          let reg = new RegExp('\\\\', 'g');
          path = path.replace(reg, '/');
        }
        return path.substring(path.lastIndexOf('/') + 1);
      };
      //token
      const headers = getHeaders();
      //업로드 상태
      const loading = ref<boolean>(false);
      //초기 로드인가요?
      const initTag = ref<boolean>(true);
      //파일 목록
      let uploadFileList = ref<any[]>([]);
      //미리 보기
      const previewImage = ref<string | undefined>('');
      //미리보기 상자 상태
      const previewVisible = ref<boolean>(false);

      //여러 이미지 업로드를 활성화할지 여부 계산
      const multiple = computed(() => {
        return props['fileMax'] > 1 || props['fileMax'] === 0;
      });

      //업로드를 계속할 수 있는지 계산
      const uploadVisible = computed(() => {
        if (props['fileMax'] === 0) {
          return true;
        }
        return uploadFileList.value.length < props['fileMax'];
      });

      /**
       * 모니터 값 변경
       */
      watch(
        () => props.value,
        (val, prevCount) => {
            if (val && val instanceof Array) {
            val = val.join(',');
          }
          if (initTag.value == true) {
            initFileList(val);
          }
        },
        { immediate: true }
      );

      /**
       * 초기화 파일 목록
       */
      function initFileList(paths) {
        if (!paths || paths.length == 0) {
          uploadFileList.value = [];
          return;
        }
        let files = [];
        let arr = paths.split(',');
        arr.forEach((value) => {
          let url = getFileAccessHttpUrl(value);
          files.push({
            uid: getRandom(10),
            name: getFileName(value),
            status: 'done',
            url: url,
            response: {
              status: 'history',
              message: value,
            },
          });
        });
        uploadFileList.value = files;
      }

      /**
       * 업로드하기 전에 확인하세요
       */
      function beforeUpload(file) {
        let fileType = file.type;
        if (fileType.indexOf('image') < 0) {
          createMessage.info('사진을 올려주세요');
          return false;
        }
      }
      /**
       * 파일 업로드 결과 콜백
       */
      function handleChange({ file, fileList, event }) {
        initTag.value = false;
        // uploadFileList.value = fileList;
        if (file.status === 'error') {
          createMessage.error(`${file.name} 업로드에 실패했습니다.`);
        }
        let fileUrls = [];
        let noUploadingFileCount = 0;
        if (file.status != 'uploading') {
          fileList.forEach((file) => {
            if (file.status === 'done') {
              fileUrls.push(file.response.message);
            }
            if (file.status != 'uploading') {
              noUploadingFileCount++;
            }
          });
          if (file.status === 'removed') {
            handleDelete(file);
          }
          if (noUploadingFileCount == fileList.length) {
            state.value = fileUrls.join(',');
            emit('update:value', fileUrls.join(','));
            nextTick(() => {
              initTag.value = true;
            });
          }
        }
      }

      /**
       * 사진 삭제
       */
      function handleDelete(file) {
        //필요한 경우 삭제 로직을 추가하세요.
        console.log(file);
      }

      /**
       * 미리보기 사진
       */
      function handlePreview(file) {
        previewImage.value = file.url || file.thumbUrl;
        previewVisible.value = true;
      }

      function getAvatarView() {
        if (uploadFileList.length > 0) {
          let url = uploadFileList[0].url;
          return getFileAccessHttpUrl(url, null);
        }
      }

      function handleCancel() {
        previewVisible.value = false;
      }

      return {
        state,
        attrs,
        previewImage,
        previewVisible,
        uploadFileList,
        multiple,
        headers,
        loading,
        uploadUrl,
        beforeUpload,
        uploadVisible,
        handlePreview,
        handleCancel,
        handleChange,
      };
    },
  });
</script>
<style scoped>
  .ant-upload-select-picture-card i {
    font-size: 32px;
    color: #999;
  }

  .ant-upload-select-picture-card .ant-upload-text {
    margin-top: 8px;
    color: #666;
  }
</style>
