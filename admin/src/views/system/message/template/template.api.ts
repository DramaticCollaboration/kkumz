import { unref } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

export enum Api {
  list = '/api/v1/kkumz/sys/message/sysMessageTemplate/list',
  delete = '/api/v1/kkumz/sys/message/sysMessageTemplate/delete',
  deleteBatch = '/api/v1/kkumz/sys/message/sysMessageTemplate/deleteBatch',
  exportXls = 'sys/message/sysMessageTemplate/exportXls',
  importXls = 'sys/message/sysMessageTemplate/importExcel',
  save = '/api/v1/kkumz/sys/message/sysMessageTemplate/add',
  edit = '/api/v1/kkumz/sys/message/sysMessageTemplate/edit',
  // 테스트 보내기
  send = '/api/v1/kkumz/sys/message/sysMessageTemplate/sendMsg',
}

export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * 일괄 삭제
 * @param params
 * @param confirm
 */
export const deleteBatch = (params, confirm = false) => {
  return new Promise((resolve, reject) => {
    const doDelete = () => {
      resolve(defHttp.delete({ url: Api.deleteBatch, params }, { joinParamsToUrl: true }));
    };
    if (confirm) {
      createConfirm({
        iconType: 'warning',
        title: '삭제',
        content: '삭제하시겠습니까?',
        onOk: () => doDelete(),
        onCancel: () => reject(),
      });
    } else {
      doDelete();
    }
  });
};

/**
 * 메시지 템플릿 저장 또는 변경
 */
export const saveOrUpdate = (params, isUpdate) => {
  if (unref(isUpdate)) {
    return defHttp.put({ url: Api.edit, params });
  } else {
    return defHttp.post({ url: Api.save, params });
  }
};

/**
 * 메시지 보내기 테스트
 * @param params
 */
export const sendMessageTest = (params) => defHttp.post({ url: Api.send, params });
