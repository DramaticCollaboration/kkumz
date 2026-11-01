import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/api/v1/kkumz/sys/annountCement/list',
  save = '/api/v1/kkumz/sys/annountCement/add',
  edit = '/api/v1/kkumz/sys/annountCement/edit',
  delete = '/api/v1/kkumz/sys/annountCement/delete',
  deleteBatch = '/api/v1/kkumz/sys/annountCement/deleteBatch',
  exportXls = '/api/v1/kkumz/sys/annountCement/exportXls',
  importExcel = '/api/v1/kkumz/sys/annountCement/importExcel',
  releaseData = '/api/v1/kkumz/sys/annountCement/doReleaseData',
  reovkeData = '/api/v1/kkumz/sys/annountCement/doReovkeData',
}

/**
 * 내보내기 URL
 */
export const getExportUrl = Api.exportXls;
/**
 * 가져오기 URL
 */
export const getImportUrl = Api.importExcel;
/**
 * 테넌트 목록 쿼리
 * @param params
 */
export const getList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 공지사항 저장 또는 업데이트
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};

/**
 * 공지 삭제
 * @param params
 */
export const deleteNotice = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * 대량 메시지 공지
 * @param params
 */
export const batchDeleteNotice = (params) => defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true });

/**
 * 게시
 * @param id
 */
export const doReleaseData = (params) => defHttp.get({ url: Api.releaseData, params });
/**
 * 취소
 * @param id
 */
export const doReovkeData = (params) => defHttp.get({ url: Api.reovkeData, params });
