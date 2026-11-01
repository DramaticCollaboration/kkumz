<template>
  <div ref="containerRef" :class="`${prefixCls}-container`">
    <a-upload
      :headers="headers"
      :multiple="multiple"
      :action="uploadUrl"
      :fileList="fileList"
      :disabled="disabled"
      v-bind="bindProps"
      @remove="onRemove"
      @change="onFileChange"
      @preview="onFilePreview"
    >
      <template v-if="isImageMode">
        <div v-if="!isMaxCount">
          <Icon icon="ant-design:plus-outlined" />
          <div class="ant-upload-text">{{ text }}</div>
        </div>
      </template>
      <a-button v-else-if="buttonVisible" :disabled="buttonDisabled">
        <Icon icon="ant-design:upload-outlined" />
        <span>{{ text }}</span>
      </a-button>
    </a-upload>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, watch, nextTick, createApp,unref } from 'vue';
  import { Icon } from '/@/components/Icon';
  import { getToken } from '/@/utils/auth';
  import { uploadUrl } from '/@/api/common/api';
  import { propTypes } from '/@/utils/propTypes';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { createImgPreview } from '/@/components/Preview/index';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { UploadTypeEnum } from './upload.data';
  import { getFileAccessHttpUrl, getHeaders } from '/@/utils/common/compUtils';
  import UploadItemActions from './components/UploadItemActions.vue';

  const { createMessage, createConfirm } = useMessage();
  const { prefixCls } = useDesign('j-upload');
  const attrs = useAttrs();
  const emit = defineEmits(['change', 'update:value']);
  const props = defineProps({
    value: propTypes.oneOfType([propTypes.string, propTypes.array]),
    text: propTypes.string.def('업로드'),
    fileType: propTypes.string.def(UploadTypeEnum.all),
    /*이 속성은 파일 업로드의 비즈니스 경로를 제어하는 ​​데 사용됩니다.*/
    bizPath: propTypes.string.def('temp'),
    /**
     * URL을 반환할지 여부,
     * true：URL만 반환
     * false：fileName filePath fileSize 반환
     */
    returnUrl: propTypes.bool.def(true),
    // 최대 업로드 수
    maxCount: propTypes.number.def(0),
    buttonVisible: propTypes.bool.def(true),
    multiple: propTypes.bool.def(true),
    // 좌우 이동 버튼 표시 여부
    mover: propTypes.bool.def(true),
    // 다운로드 버튼 표시 여부
    download: propTypes.bool.def(true),
    // 삭제시 확인창 표시 여부
    removeConfirm: propTypes.bool.def(false),
    beforeUpload: propTypes.func,
    disabled: propTypes.bool.def(false),
    // 최대 개수를 초과한 경우에도 업로드를 허용하려면 이전 파일을 교체하세요.
    replaceLastOne: propTypes.bool.def(false),
  });

  const headers = getHeaders();
  const fileList = ref<any[]>([]);
  const uploadGoOn = ref<boolean>(true);
  // refs
  const containerRef = ref();
  // 최대 업로드 수에 도달했습니까?
  const isMaxCount = computed(() => props.maxCount > 0 && fileList.value.length >= props.maxCount);
  // 현재 이미지 업로드 모드인지 여부
  const isImageMode = computed(() => props.fileType === UploadTypeEnum.image);
  // 업로드 버튼이 비활성화되어 있나요?
  const buttonDisabled = computed(()=>{
    if(props.disabled === true){
      return true;
    }
    if(isMaxCount.value === true){
      if(props.replaceLastOne === true){
        return false
      }else{
        return true;
      }
    }
    return false
  });
  // props와 attrs 병합
  const bindProps = computed(() => {
    const bind: any = Object.assign({}, props, unref(attrs));

    bind.name = 'file';
    bind.listType = isImageMode.value ? 'picture-card' : 'text';
    bind.class = [bind.class, { 'upload-disabled': props.disabled }];
    bind.data = { biz: props.bizPath, ...bind.data };
    if (!bind.beforeUpload) {
      bind.beforeUpload = onBeforeUpload;
    }
    // 현재 이미지 업로드 모드인 경우에는 이미지만 업로드할 수 있습니다.
    if (isImageMode.value && !bind.accept) {
      bind.accept = 'image/*';
    }
    return bind;
  });

  watch(
    () => props.value,
    (val) => {
      if (Array.isArray(val)) {
        if (props.returnUrl) {
          parsePathsValue(val.join(','));
        } else {
          parseArrayValue(val);
        }
      } else {
        if (props.returnUrl) {
          parsePathsValue(val);
        } else {
          val && parseArrayValue(JSON.parse(val));
        }
      }
    },
    { immediate: true }
  );

  watch(fileList, () => nextTick(() => addActionsListener()), { immediate: true });

  const antUploadItemCls = 'ant-upload-list-item';

  // Listener
  function addActionsListener() {
    if (!isImageMode.value) {
      return;
    }
    const uploadItems = containerRef.value ? containerRef.value.getElementsByClassName(antUploadItemCls) : null;
    if (!uploadItems || uploadItems.length === 0) {
      return;
    }
    for (const uploadItem of uploadItems) {
      let hasActions = uploadItem.getAttribute('data-has-actions') === 'true';
      if (!hasActions) {
        uploadItem.addEventListener('mouseover', onAddActionsButton);
      }
    }
  }

  // 좌우로 움직일 수 있는 버튼 추가
  function onAddActionsButton(event) {
    const getUploadItem = () => {
      for (const path of event.path) {
        if (path.classList.contains(antUploadItemCls)) {
          return path;
        } else if (path.classList.contains(`${prefixCls}-container`)) {
          return null;
        }
      }
      return null;
    };
    const uploadItem = getUploadItem();
    if (!uploadItem) {
      return;
    }
    const actions = uploadItem.getElementsByClassName('ant-upload-list-item-actions');
    if (!actions || actions.length === 0) {
      return;
    }
    // 작업 버튼 추가
    const div = document.createElement('div');
    div.className = 'upload-actions-container';
    createApp(UploadItemActions, {
      element: uploadItem,
      fileList: fileList,
      mover: props.mover,
      download: props.download,
      emitValue: emitValue,
    }).mount(div);
    actions[0].appendChild(div);
    uploadItem.setAttribute('data-has-actions', 'true');
    uploadItem.removeEventListener('mouseover', onAddActionsButton);
  }

  // 쉼표로 구분된 데이터베이스 저장소 구문 분석
  function parsePathsValue(paths) {
    if (!paths || paths.length == 0) {
      fileList.value = [];
      return;
    }
    let list: any[] = [];
    for (const item of paths.split(',')) {
      let url = getFileAccessHttpUrl(item);
      list.push({
        uid: uidGenerator(),
        name: getFileName(item),
        status: 'done',
        url: url,
        response: { status: 'history', message: item },
      });
    }
    fileList.value = list;
  }

  // 배열 값 구문 분석
  function parseArrayValue(array) {
    if (!array || array.length == 0) {
      fileList.value = [];
      return;
    }
    let list: any[] = [];
    for (const item of array) {
      let url = getFileAccessHttpUrl(item.filePath);
      list.push({
        uid: uidGenerator(),
        name: item.fileName,
        url: url,
        status: 'done',
        response: { status: 'history', message: item.filePath },
      });
    }
    fileList.value = list;
  }

  // 파일 업로드 전 작업
  function onBeforeUpload(file) {
    uploadGoOn.value = true;
    if (isImageMode.value) {
      if (file.type.indexOf('image') < 0) {
        createMessage.warning('사진을 올려주세요');
        uploadGoOn.value = false;
        return false;
      }
    }
    // Upload 검증 이전 확장
    if (typeof props.beforeUpload === 'function') {
      return props.beforeUpload(file);
    }
    return true;
  }

  // 처리 이벤트 삭제
  function onRemove() {
    if (props.removeConfirm) {
      return new Promise((resolve) => {
        createConfirm({
          title: '삭제',
          content: `정말로 이을 삭제하시겠습니까?${isImageMode.value ? '영화' : '파일'}?`,
          iconType: 'warning',
          onOk: () => resolve(true),
          onCancel: () => resolve(false),
        });
      });
    }
    return true;
  }

  // 구성요소 변경 이벤트 업로드
  function onFileChange(info) {
    if (!info.file.status && uploadGoOn.value === false) {
      info.fileList.pop();
    }
    let fileListTemp = info.fileList;
    // 최대 업로드 수를 제한하세요
    if (props.maxCount > 0) {
      let count = fileListTemp.length;
      if (count >= props.maxCount) {
        let diffNum = props.maxCount - fileListTemp.length;
        if (diffNum >= 0) {
          fileListTemp = fileListTemp.slice(-props.maxCount);
        } else {
          return;
        }
      }
    }
    if (info.file.status === 'done') {
      let successFileList = [];
      if (info.file.response.success) {
        successFileList = fileListTemp.map((file) => {
          if (file.response) {
            let reUrl = file.response.message;
            file.url = getFileAccessHttpUrl(reUrl);
          }
          return file;
        });
      }else{
        successFileList = fileListTemp.filter(item=>{
          return item.uid!=info.file.uid;
        });
        createMessage.error(`${info.file.name} 업로드에 실패했습니다.`);
      }
      fileListTemp = successFileList;
    } else if (info.file.status === 'error') {
      createMessage.error(`${info.file.name} 업로드에 실패했습니다.`);
    }
    fileList.value = fileListTemp;
    if (info.file.status === 'done' || info.file.status === 'removed') {
      //returnUrl이 true인 경우 파일 경로만 반환됩니다.
      if (props.returnUrl) {
        handlePathChange();
      } else {
        //returnUrl이 false인 경우 파일 이름, 파일 경로 및 파일 크기를 반환합니다.
        let newFileList: any[] = [];
        for (const item of fileListTemp) {
          if (item.status === 'done') {
            let fileJson = {
              fileName: item.name,
              filePath: item.response.message,
              fileSize: item.size,
            };
            newFileList.push(fileJson);
          }else{
            return;
          }
        }
        emitValue(JSON.stringify(newFileList));
      }
    }
  }

  function handlePathChange() {
    let uploadFiles = fileList.value;
    let path = '';
    if (!uploadFiles || uploadFiles.length == 0) {
      path = '';
    }
    let pathList: string[] = [];
    for (const item of uploadFiles) {
      if (item.status === 'done') {
        pathList.push(item.response.message);
      } else {
        return;
      }
    }
    if (pathList.length > 0) {
      path = pathList.join(',');
    }
    emitValue(path);
  }

  // 파일 및 사진 미리보기
  function onFilePreview(file) {
    if (isImageMode.value) {
      createImgPreview({ imageList: [file.url], maskClosable: true });
    } else {
      window.open(file.url);
    }
  }

  function emitValue(value) {
    emit('change', value);
    emit('update:value', value);
  }

  function uidGenerator() {
    return '-' + parseInt(Math.random() * 10000 + 1, 10);
  }

  function getFileName(path) {
    if (path.lastIndexOf('\\') >= 0) {
      let reg = new RegExp('\\\\', 'g');
      path = path.replace(reg, '/');
    }
    return path.substring(path.lastIndexOf('/') + 1);
  }

  defineExpose({
    addActionsListener,
  });
</script>

<style lang="less">
  //noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-j-upload';

  .@{prefix-cls} {
    &-container {
      position: relative;

      .upload-disabled {
        .ant-upload-list-item {
          .anticon-close {
            display: none;
          }

          .anticon-delete {
            display: none;
          }
        }

        .upload-download-handler {
          right: 6px !important;
        }
      }
      .ant-upload-text-icon {
        color: @primary-color;
      }
      .ant-upload-list-item {
        .upload-actions-container {
          position: absolute;
          top: -31px;
          left: -18px;
          z-index: 11;
          width: 84px;
          height: 84px;
          line-height: 28px;
          text-align: center;
          pointer-events: none;

          a {
            opacity: 0.9;
            margin: 0 5px;
            cursor: pointer;
            transition: opacity 0.3s;

            .anticon {
              color: #fff;
              font-size: 16px;
            }

            &:hover {
              opacity: 1;
            }
          }

          .upload-mover-handler,
          .upload-download-handler {
            position: absolute;
            pointer-events: auto;
          }

          .upload-mover-handler {
            width: 100%;
            bottom: 0;
          }

          .upload-download-handler {
            top: -4px;
            right: -4px;
          }
        }
      }
    }
  }
</style>
