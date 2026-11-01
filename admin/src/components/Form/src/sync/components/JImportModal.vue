<template>
  <div>
    <BasicModal v-bind="$attrs" @register="register" title="엑셀 가져오기" :width="600" @cancel="handleClose" :confirmLoading="uploading" destroyOnClose>
      <!--검증 여부-->
      <div style="margin: 0 5px 5px" v-if="online">
        <span style="display: inline-block; height: 32px; line-height: 32px; vertical-align: middle">확인 활성화 여부:</span>
        <span style="margin-left: 6px">
          <a-switch :checked="validateStatus == 1" @change="handleChangeValidateStatus" checked-children="예" un-checked-children="아니요" />
        </span>
      </div>
      <!--업로드-->
      <a-upload name="file" accept=".xls,.xlsx" :multiple="true" :fileList="fileList" @remove="handleRemove" :beforeUpload="beforeUpload">
        <a-button preIcon="ant-design:upload-outlined">가져오기 파일 선택</a-button>
      </a-upload>
      <!--보행인-->
      <template #footer>
        <a-button @click="handleClose">취소</a-button>
        <a-button type="primary" @click="handleImport" :disabled="uploadDisabled" :loading="uploading">{{
          uploading ? '업로드 중...' : '업로드 시작'
        }}</a-button>
      </template>
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, unref, watchEffect, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { defHttp } from '/@/utils/http/axios';
  import { useGlobSetting } from '/@/hooks/setting';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { isObject } from '/@/utils/is';

  export default defineComponent({
    name: 'JImportModal',
    components: {
      BasicModal,
    },
    props: {
      url: {
        type: String,
        default: '',
        required: false,
      },
      biz: {
        type: String,
        default: '',
        required: false,
      },
      //온라인으로 수입할지 여부
      online: {
        type: Boolean,
        default: false,
        required: false,
      },
    },
    emits: ['ok', 'register'],
    setup(props, { emit, refs }) {
      const { createMessage, createWarningModal } = useMessage();
      //등록 팝업 상자
      const [register, { closeModal }] = useModalInner((data) => {
        reset(data);
      });
      const glob = useGlobSetting();
      const attrs = useAttrs();
      const uploading = ref(false);
      //파일 수집
      const fileList = ref([]);
      //업로드 URL
      const uploadAction = ref('');
      const foreignKeys = ref('');
      //상태 확인
      const validateStatus = ref(0);
      const getBindValue = Object.assign({}, unref(props), unref(attrs));
      //청취 URL
      watchEffect(() => {
        props.url && (uploadAction.value = `${glob.uploadUrl}${props.url}`);
      });
      //버튼 비활성화 상태
      const uploadDisabled = computed(() => !(unref(fileList).length > 0));

      //닫기 방법
      function handleClose() {
        closeModal();
        reset();
      }

      //상태 스위치 확인
      function handleChangeValidateStatus(checked) {
        validateStatus.value = !!checked ? 1 : 0;
      }

      //업로드된 파일 제거
      function handleRemove(file) {
        const index = unref(fileList).indexOf(file);
        const newFileList = unref(fileList).slice();
        newFileList.splice(index, 1);
        fileList.value = newFileList;
      }

      //업로드 전 처리 중
      function beforeUpload(file) {
        fileList.value = [...unref(fileList), file];
        return false;
      }

      //파일 업로드
      function handleImport() {
        let { biz, online } = props;
        const formData = new FormData();
        if (biz) {
          formData.append('isSingleTableImport', biz);
        }
        if (unref(foreignKeys) && unref(foreignKeys).length > 0) {
          formData.append('foreignKeys', unref(foreignKeys));
        }
        if (isObject(foreignKeys.value)) {
          formData.append('foreignKeys', JSON.stringify(foreignKeys.value));
        }
        if (!!online) {
          formData.append('validateStatus', unref(validateStatus));
        }
        unref(fileList).forEach((file) => {
          formData.append('files[]', file);
        });
        uploading.value = true;

        // 멀티파트 폼 데이터 요청 헤더 구성 및 파일 업로드 실행
        let headers = {
          'Content-Type': 'multipart/form-data;boundary = ' + new Date().getTime(),
        };
        defHttp.post({ url: props.url, params: formData, headers }, { isTransformResponse: false }).then((res) => {
          uploading.value = false;
          if (res.success) {
            if (res.code == 201) {
              errorTip(res.message, res.result);
            } else {
              createMessage.success(res.message);
            }
            handleClose();
            reset();
            emit('ok');
          } else {
            createMessage.warning(res.message);
          }
        }).catch(() => {
          uploading.value = false;
        });
      }

      //오류 메시지 프롬프트
      function errorTip(tipMessage, fileUrl) {
        let href = glob.uploadUrl + fileUrl;
        createWarningModal({
          title: '가져오기에 성공했지만 잘못된 데이터가 있습니다!',
          centered: false,
          content: `<div>
                        <span>${tipMessage}</span><br/>
                        <span>구체적인 내용은 부탁드립니다<a href = ${href} target="_blank"> 다운로드하려면 클릭하세요 </a> </span>
                      </div>`,
        });
      }

      //새로고침
      function reset(arg?) {
        fileList.value = [];
        uploading.value = false;
        foreignKeys.value = arg;
        validateStatus.value = 0;
      }

      return {
        register,
        getBindValue,
        uploadDisabled,
        fileList,
        uploading,
        validateStatus,
        handleClose,
        handleChangeValidateStatus,
        handleRemove,
        beforeUpload,
        handleImport,
      };
    },
  });
</script>
