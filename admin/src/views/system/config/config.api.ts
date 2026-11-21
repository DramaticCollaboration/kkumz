import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/api/v1/kkumz/sys/config/list',
  categories = '/api/v1/kkumz/sys/config/categories',
  categoryDict = '/api/v1/kkumz/sys/dict/getDictItems/sys_config_category',
  publicList = '/api/v1/kkumz/sys/config/public',
  save = '/api/v1/kkumz/sys/config/save',
  delete = '/api/v1/kkumz/sys/config/delete',
  update = '/api/v1/kkumz/sys/config/update',
  updateBatch = '/api/v1/kkumz/sys/config/updateBatch',
  reset = '/api/v1/kkumz/sys/config/reset',
  refreshCache = '/api/v1/kkumz/sys/config/refreshCache',
  testConnection = '/api/v1/kkumz/sys/config/test-connection',
}

export interface DictItemModel {
  value: string;
  text?: string;
  title?: string;
  label?: string;
}

export const queryCategoryDictItems = () => {
  return defHttp.get<DictItemModel[]>({
    url: Api.categoryDict,
  });
};

export interface SysConfigModel {
  id?: string;
  configCategory: string;
  configKey: string;
  configName: string;
  configValue: string;
  defaultValue: string;
  valueType: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON' | 'SECRET';
  isSecret: boolean;
  isDynamic: boolean;
  status: number;
  sortOrder: number;
  description: string;
}

export interface ConfigUpdateRequest {
  configKey: string;
  configValue: string;
}

export interface ConnectionTestRequest {
  testType: string;
  endpoint: string;
  credential?: string;
  extraParams?: Record<string, string>;
}

export interface ConnectionTestResponse {
  success: boolean;
  latencyMs: number;
  message: string;
  details: Record<string, any>;
}

export const queryConfigList = (category?: string) => {
  return defHttp.get<SysConfigModel[]>({
    url: Api.list,
    params: { category },
  });
};


export const saveOrUpdateConfig = (data: Partial<SysConfigModel>) => {
  return defHttp.post<SysConfigModel>({
    url: Api.save,
    data,
  });
};


export const updateBatchConfig = (data: ConfigUpdateRequest[]) => {
  return defHttp.put({
    url: Api.updateBatch,
    data,
  });
};

export const resetConfig = (configKey: string) => {
  return defHttp.post({
    url: Api.reset,
    params: { configKey },
  });
};

export const refreshConfigCache = () => {
  return defHttp.post({
    url: Api.refreshCache,
  });
};

export const testConnection = (data: ConnectionTestRequest) => {
  return defHttp.post<ConnectionTestResponse>({
    url: Api.testConnection,
    data,
  });
};
