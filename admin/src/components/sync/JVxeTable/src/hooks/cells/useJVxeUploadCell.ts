import { ref, computed, watch } from 'vue';

import {getToken} from '/@/utils/auth';
import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
import { JVxeComponent } from '../../types/JVxeComponent';
import { useJVxeComponent } from '../useJVxeComponent';

/**
 * 공개 업로드 컴포넌트 사용
 * @param props
 * @param 옵션 컴포넌트 옵션, 토큰：기본적으로 토큰 및 작업 전달 여부：기본 업로드 경로, 다중：여러 파일을 허용할지 여부
 */
export function useJVxeUploadCell(props: JVxeComponent.Props, options?) {
  const setup = useJVxeComponent(props);
  const { innerValue, originColumn, handleChangeCommon } = setup;

  const innerFile = ref<any>(null);

  /** upload headers */
  const uploadHeaders = computed(() => {
    let headers = {};
    if ((originColumn.value.token ?? options?.token ?? false) === true) {
      headers['X-Access-Token'] = getToken();
    }
    return headers;
  });

  /** 업로드 요청 주소 */
  const uploadAction = computed(() => {
    if (!originColumn.value.action) {
      return options?.action ?? '';
    } else {
      return originColumn.value.action;
    }
  });
  const hasFile = computed(() => innerFile.value != null);
  const responseName = computed(() => originColumn.value.responseName ?? 'message');

  watch(
    innerValue,
    (val) => {
      if (val) {
        innerFile.value = val;
      } else {
        innerFile.value = null;
      }
    },
    { immediate: true }
  );

  function handleChangeUpload(info) {
    let { file } = info;
    let value = {
      name: file.name,
      type: file.type,
      size: file.size,
      status: file.status,
      percent: file.percent,
      path: innerFile.value?.path ?? '',
    };
    if (file.response) {
      value['responseName'] = file.response[responseName.value];
    }
    let paths: string[] = [];
    if (options?.multiple && innerFile.value && innerFile.value.path) {
      paths = innerFile.value.path.split(',');
    }
    if (file.status === 'done') {
      if (typeof file.response.success === 'boolean') {
        if (file.response.success) {
          paths.push(file.response[responseName.value]);
          value['path'] = paths.join(',');
          handleChangeCommon(value);
        } else {
          value['status'] = 'error';
          value['message'] = file.response.message || '알 수 없는 오류';
        }
      } else {
        // 액션 업로드 경로가 sync-boot가 아닌 배경으로 설정된 경우 성공 속성이 반환되지 않을 수 있다는 점을 고려하면 기본값은 성공입니다.
        paths.push(file.response[responseName.value]);
        value['path'] = paths.join(',');
        handleChangeCommon(value);
      }
    } else if (file.status === 'error') {
      value['message'] = file.response.message || '알 수 없는 오류';
    }
    innerFile.value = value;
  }

  function handleClickDownloadFile() {
    let { url, path } = innerFile.value || {};
    if (!url || url.length === 0) {
      if (path && path.length > 0) {
        url = getFileAccessHttpUrl(path.split(',')[0]);
      }
    }
    if (url) {
      window.open(url);
    }
  }

  function handleClickDeleteFile() {
    handleChangeCommon(null);
  }

  return {
    ...setup,
    innerFile,
    uploadAction,
    uploadHeaders,
    hasFile,
    responseName,
    handleChangeUpload,
    handleClickDownloadFile,
    handleClickDeleteFile,
  };
}

export function fileGetValue(value) {
  if (value && value.path) {
    return value.path;
  }
  return value;
}

export function fileSetValue(value) {
  if (value) {
    let first = value.split(',')[0];
    let name = first.substring(first.lastIndexOf('/') + 1);
    return {
      name: name,
      path: value,
      status: 'done',
    };
  }
  return value;
}
