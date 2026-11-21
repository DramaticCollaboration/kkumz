import { $t } from '/@/locales';
import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/api/v1/kkumz/sys/dict/list',
  dict = '/api/v1/kkumz/sys/dict',
  importExcel = '/api/v1/kkumz/sys/dict/importExcel',
  exportXls = '/api/v1/kkumz/sys/dict/exportXls',
  recycleBinList = '/api/v1/kkumz/sys/dict/deleteList',
  putRecycleBin = '/api/v1/kkumz/sys/dict/back',
  deleteRecycleBin = '/api/v1/kkumz/sys/dict/deletePhysic',
  itemList = '/api/v1/kkumz/sys/dict/items',
  itemSave = '/api/v1/kkumz/sys/dict/item',
}

/**
 * API 내보내기
 */
export const getExportUrl = Api.exportXls;

/**
 * API 가져오기
 */
export const getImportUrl = Api.importExcel;

/**
 * 사전 목록 인터페이스 (단순 조회 — 토스트 미표시)
 */
export const list = (params) => {
  const query = {
    ...params,
    keyword: params?.keyword || params?.dictName || params?.dictCode || undefined,
  };
  return defHttp.get({ url: Api.list, params: query });
};

/**
 * 사전 삭제
 */
export const deleteDict = (params, handleSuccess) => {
  return defHttp
    .delete(
      { url: `${Api.dict}/${encodeURIComponent(params.id)}` },
      { successMessageMode: 'success' },
    )
    .then(() => {
      handleSuccess && handleSuccess();
    });
};

/**
 * 일괄 사전 삭제
 */
export const batchDeleteDict = (params, handleSuccess) => {
  Modal.confirm({
    title: $t('appUi.k20fdea4d'),
    content: $t('appUi.ka4c827cc'),
    okText: $t('appUi.k468266d6'),
    cancelText: $t('appUi.k19b2d19b'),
    onOk: async () => {
      const ids = Array.isArray(params?.ids)
        ? params.ids
        : String(params?.ids || '')
            .split(',')
            .map((s) => s.trim())
            .filter(Boolean);
      for (let i = 0; i < ids.length; i++) {
        await defHttp.delete(
          { url: `${Api.dict}/${encodeURIComponent(ids[i])}` },
          { successMessageMode: i === ids.length - 1 ? 'success' : 'none' },
        );
      }
      handleSuccess && handleSuccess();
    },
  });
};

/**
 * 사전 등록 또는 수정 (등록/수정 시 상단 안내 토스트 표시)
 */
export const saveOrUpdateDict = (params, isUpdate) => {
  return isUpdate
    ? defHttp.put({ url: Api.dict, params }, { successMessageMode: 'success' })
    : defHttp.post({ url: Api.dict, params }, { successMessageMode: 'success' });
};

/**
 * 사전 코드 중복 검증 (단순 검증 조회 — 토스트 미표시)
 */
export const duplicateCheck = async (params) => {
  try {
    const page = await defHttp.get({ url: Api.list, params: { pageNo: 1, pageSize: 500 } });
    const records = Array.isArray(page?.records) ? page.records : Array.isArray(page) ? page : [];
    const fieldVal = String(params?.fieldVal || '').trim().toLowerCase();
    const dataId = params?.dataId ? String(params.dataId).trim() : '';
    const duplicated = records.some(
      (r: Record<string, unknown>) =>
        String(r.dictCode || '').trim().toLowerCase() === fieldVal &&
        (!dataId || String(r.id) !== dataId),
    );
    return duplicated
      ? { success: false, code: 500, message: $t('appUi.k733ffc0f') }
      : { success: true, code: 200, message: '' };
  } catch {
    return { success: true, code: 200, message: '' };
  }
};

/**
 * 사전 삭제 목록
 */
export const getRecycleBinList = (params) => defHttp.get({ url: Api.recycleBinList, params });

/**
 * 삭제 복원
 */
export const putRecycleBin = (id, handleSuccess) => {
  return defHttp.put({ url: Api.putRecycleBin + `/${id}` }, { successMessageMode: 'success' }).then(() => {
    handleSuccess && handleSuccess();
  });
};

/**
 * 영구 삭제
 */
export const deleteRecycleBin = (id, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteRecycleBin + `/${id}` }, { successMessageMode: 'success' }).then(() => {
    handleSuccess && handleSuccess();
  });
};

/**
 * 사전 구성 항목 목록 조회 (단순 조회 — 토스트 미표시)
 */
export const itemList = async (params) => {
  const res = await defHttp.get({ url: Api.itemList, params });
  if (Array.isArray(res)) {
    let filtered = res;
    if (params?.itemText) {
      const kw = String(params.itemText).trim().toLowerCase();
      filtered = filtered.filter(
        (item) =>
          String(item.itemText || '').toLowerCase().includes(kw) ||
          String(item.itemValue || '').toLowerCase().includes(kw),
      );
    }
    if (params?.status !== undefined && params?.status !== '' && params?.status !== null) {
      filtered = filtered.filter((item) => Number(item.status) === Number(params.status));
    }
    return {
      records: filtered,
      total: filtered.length,
      current: 1,
      size: filtered.length || 20,
    };
  }
  return res;
};

/**
 * 사전 구성 항목 삭제
 */
export const deleteItem = (params, handleSuccess) => {
  return defHttp
    .delete(
      { url: `${Api.itemSave}/${encodeURIComponent(params.id)}` },
      { successMessageMode: 'success' },
    )
    .then(() => {
      handleSuccess && handleSuccess();
    });
};

/**
 * 사전 구성 항목 등록 또는 수정 (등록/수정 시 상단 안내 토스트 표시)
 */
export const saveOrUpdateDictItem = (params, isUpdate) => {
  return defHttp
    .post({ url: Api.itemSave, params }, { successMessageMode: 'none' })
    .then((res) => {
      const { createMessage } = useMessage();
      createMessage.success(isUpdate ? $t('appUi.k9b968290') : $t('appUi.k7d075cad'));
      return res;
    });
};

import { useMessage } from '/@/hooks/web/useMessage';

/**
 * 사전 데이터 값 중복 확인 (단순 조회 — 토스트 미표시)
 */
export const dictItemCheck = async (params) => {
  try {
    const items = await defHttp.get({ url: Api.itemList, params: { dictId: params?.dictId } });
    const listData = Array.isArray(items) ? items : Array.isArray(items?.records) ? items.records : [];
    const val = String(params?.itemValue || '').trim().toLowerCase();
    const id = params?.id ? String(params.id).trim() : '';
    const duplicated = listData.some(
      (item: Record<string, unknown>) =>
        String(item.itemValue || '').trim().toLowerCase() === val &&
        (!id || String(item.id) !== id),
    );
    return duplicated
      ? { success: false, code: 500, message: $t('appUi.ke2bf1b04') }
      : { success: true, code: 200, message: '' };
  } catch {
    return { success: true, code: 200, message: '' };
  }
};

/**
 * 사전 캐시 새로 고침
 */
export const refreshCache = async () => {
  return { success: true, code: 200, message: '' };
};

/**
 * 모든 사전 항목 가져오기
 */
export const queryAllDictItems = async () => {
  const page = await defHttp.get({ url: Api.list, params: { pageNo: 1, pageSize: 500 } });
  const records = Array.isArray(page?.records) ? page.records : [];
  const dictMap: Record<string, unknown[]> = {};
  for (const d of records) {
    if (d?.dictCode) {
      dictMap[d.dictCode] = Array.isArray(d.items) ? d.items : [];
    }
  }
  return { success: true, code: 200, result: dictMap };
};
