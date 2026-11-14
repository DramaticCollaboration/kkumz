import { defHttp } from '/@/utils/http/axios';

export const PROMPT_API_PREFIX = '/api/v1/kkumz/sys/prompt';
export const DEFAULT_APP_CODE = 'KKUMZ';

export interface SysPromptVariableItem {
  name: string;
  label?: string;
  required?: boolean;
  sampleValue?: string;
  description?: string;
}

export interface SysPromptModel {
  id?: string;
  appCode?: string;
  promptCode: string;
  promptName: string;
  category: string;
  callerClass?: string;
  systemPrompt: string;
  userPromptTemplate: string;
  variablesSchema?: string;
  variables?: SysPromptVariableItem[];
  outputSchema?: string;
  modelOverride?: string;
  temperature?: number;
  version?: number;
  status?: string;
  description?: string;
  changeReason?: string;
  createBy?: string;
  createTime?: string;
  updateBy?: string;
  updateTime?: string;
}

export interface SysPromptHistoryModel {
  id: string;
  promptId: string;
  appCode: string;
  promptCode: string;
  version: number;
  systemPrompt: string;
  userPromptTemplate: string;
  variablesSchema?: string;
  outputSchema?: string;
  modelOverride?: string;
  temperature?: number;
  changeReason?: string;
  createdBy?: string;
  createdAt?: string;
}

export interface SysPromptRenderRequest {
  promptCode?: string;
  systemPrompt?: string;
  userPromptTemplate?: string;
  outputSchema?: string;
  variables?: Record<string, any>;
}

export interface SysPromptRenderResult {
  promptCode?: string;
  renderedSystemPrompt: string;
  renderedUserPrompt: string;
  detectedVariables: string[];
  missingVariables: string[];
  valid: boolean;
}

export interface SysPromptTestRunResult {
  promptCode?: string;
  renderedSystemPrompt: string;
  renderedUserPrompt: string;
  llmResponse: string;
  durationMs: number;
  jsonValid: boolean;
  validationMessage?: string;
}

export const getPromptList = (params?: Record<string, any>) =>
  defHttp.get<SysPromptModel[]>({
    url: `${PROMPT_API_PREFIX}/list`,
    params: { appCode: DEFAULT_APP_CODE, ...params },
  });

export const getPromptDetail = (id: string) =>
  defHttp.get<SysPromptModel>({
    url: `${PROMPT_API_PREFIX}/${id}`,
  });

export const getPromptByCode = (promptCode: string) =>
  defHttp.get<SysPromptModel>({
    url: `${PROMPT_API_PREFIX}/code/${promptCode}`,
  });

export const saveOrUpdatePrompt = (params: SysPromptModel) =>
  defHttp.post<SysPromptModel>({
    url: `${PROMPT_API_PREFIX}/save`,
    params: { appCode: DEFAULT_APP_CODE, ...params },
  });

export const deletePromptById = (id: string) =>
  defHttp.delete({
    url: `${PROMPT_API_PREFIX}/${id}`,
  });

export const deleteBatchPrompt = (ids: string[]) =>
  defHttp.delete({
    url: `${PROMPT_API_PREFIX}/batch`,
    params: { ids: ids.join(',') },
  });

export const getPromptHistory = (id: string) =>
  defHttp.get<SysPromptHistoryModel[]>({
    url: `${PROMPT_API_PREFIX}/${id}/history`,
  });

export const rollbackPromptVersion = (id: string, version: number) =>
  defHttp.post<SysPromptModel>({
    url: `${PROMPT_API_PREFIX}/${id}/rollback/${version}`,
  });

export const previewPromptRender = (params: SysPromptRenderRequest) =>
  defHttp.post<SysPromptRenderResult>({
    url: `${PROMPT_API_PREFIX}/preview`,
    params,
  });

export const testRunPrompt = (params: SysPromptRenderRequest) =>
  defHttp.post<SysPromptTestRunResult>({
    url: `${PROMPT_API_PREFIX}/test-run`,
    params,
  });
