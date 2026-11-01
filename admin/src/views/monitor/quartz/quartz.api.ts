import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/api/v1/kkumz/sys/quartzJob/list',
  save = '/api/v1/kkumz/sys/quartzJob/add',
  edit = '/api/v1/kkumz/sys/quartzJob/edit',
  get = '/api/v1/kkumz/sys/quartzJob/queryById',
  pause = '/api/v1/kkumz/sys/quartzJob/pause',
  resume = '/api/v1/kkumz/sys/quartzJob/resume',
  delete = '/api/v1/kkumz/sys/quartzJob/delete',
  exportXlsUrl = '/api/v1/kkumz/sys/quartzJob/exportXls',
  importExcelUrl = '/api/v1/kkumz/sys/quartzJob/importExcel',
  execute = '/api/v1/kkumz/sys/quartzJob/execute',
  deleteBatch = '/api/v1/kkumz/sys/quartzJob/deleteBatch',
  logs = '/api/v1/kkumz/sys/quartzJob/logs',
  retry = '/api/v1/kkumz/sys/quartzJob/retry',
}

/**
 * 스케줄러 실행 이력 로그 목록 조회 API
 */
export const getQuartzJobLogs = (params: any) => {
  return defHttp.get({ url: Api.logs, params });
};

/**
 * 실패한 스케줄러 작업 재시도 API
 */
export const retryQuartzJob = (logId: string) => {
  return defHttp.post({ url: Api.retry, data: { logId, id: logId } });
};

/**
 * API 내보내기
 */
export const getExportUrl = Api.exportXlsUrl;
/**
 * API 가져오기
 */
export const getImportUrl = Api.importExcelUrl;
/**
 * 쿼리 작업 목록
 * @param params
 */
export const getQuartzList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 작업 저장 또는 업데이트
 * @param params
 */
export const saveOrUpdateQuartz = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};

/**
 * 쿼리 작업 세부정보
 * @param params
 */
export const getQuartzById = (params) => {
  return defHttp.get({ url: Api.get, params });
};

/**
 * 삭제일
 * @param params
 */
export const deleteQuartz = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * 시작
 * @param params
 */
export const resumeJob = (params, handleSuccess) => {
  return defHttp.get({ url: Api.resume, params }).then(() => {
    handleSuccess();
  });
};

/**
 * 정지시키다
 * @param params
 */
export const pauseJob = (params, handleSuccess) => {
  return defHttp.get({ url: Api.pause, params }).then(() => {
    handleSuccess();
  });
};

/**
 * 즉시 실행
 * @param params
 */
export const executeImmediately = (params, handleSuccess) => {
  return defHttp.get({ url: Api.execute, params }).then(() => {
    handleSuccess();
  });
};

/**
 * 일괄 삭제일
 * @param params
 */
export const batchDeleteQuartz = (params, handleSuccess) => {
  Modal.confirm({
    title: '삭제 확인',
    content: '선택한 데이터 삭제 여부',
    okText: '확인',
    cancelText: '취소',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};
