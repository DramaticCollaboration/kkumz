import { $t } from '/@/locales';
import { defHttp } from '/@/utils/http/axios';

export const HELP_API_PREFIX = '/api/v1/kkumz';
export const DEFAULT_APP_CODE = 'KKUMZ';
export const HELP_VIEWER_PATH = '/help';
export const HELP_MANAGE_PATH = '/system/help-guide';

export type HelpGuideType = 'SYSTEM' | 'SCREEN';

const Api = {
  GuideByPath: `${HELP_API_PREFIX}/sys/help/guide`,
  List: `${HELP_API_PREFIX}/sys/help/list`,
  Detail: `${HELP_API_PREFIX}/sys/help`,
  Save: `${HELP_API_PREFIX}/sys/help/save`,
  Delete: `${HELP_API_PREFIX}/sys/help`,
  DeleteBatch: `${HELP_API_PREFIX}/sys/help/batch`,
  UserPermissions: `${HELP_API_PREFIX}/sys/permission/getUserPermissionByToken`,
} as const;

export interface PageFeatureItem {
  name: string;
  description: string;
  badge?: string;
}

export interface LineageSourceItem {
  origin: string;
  cycle?: string;
  description?: string;
}

export interface LineageDestinationItem {
  target: string;
  timing?: string;
  description?: string;
}

export interface DataLineageModel {
  sources?: LineageSourceItem[];
  destinations?: LineageDestinationItem[];
}

export interface MetricFormulaItem {
  name: string;
  formula: string;
  description?: string;
}

export interface PageStepItem {
  title: string;
  description: string;
  path?: string;
}

export interface RelatedScreenItem {
  type: string;
  title: string;
  path?: string;
  description?: string;
}

export interface PageFaqItem {
  q: string;
  a: string;
}

export interface HelpFeatureItem {
  name?: string;
  description?: string;
  badge?: string;
  title?: string;
  desc?: string;
}

export interface HelpLineageNode {
  type: 'TABLE' | 'API' | 'EVENT' | 'AGENT' | 'CACHE' | string;
  name: string;
  desc: string;
}

export interface HelpLineageInfo {
  sources?: HelpLineageNode[];
  transforms?: string[];
  destinations?: HelpLineageNode[];
  sourceTables?: string[];
  apiEndpoint?: string;
}

export interface HelpFormulaItem {
  metric: string;
  expression: string;
  explanation: string;
}

export interface HelpStepItem {
  step: number;
  title: string;
  detail: string;
  path?: string;
}

export interface HelpRelatedScreen {
  title: string;
  path: string;
  relation: string;
}

export interface HelpFaqItem {
  q: string;
  a: string;
}

export interface SysPageHelpModel {
  id?: string;
  appCode?: string;
  helpType?: HelpGuideType | string;
  pageKey: string;
  routeName?: string;
  title: string;
  category: string;
  icon?: string;
  summary: string;
  overview: string;
  features?: PageFeatureItem[];
  lineage?: DataLineageModel;
  formulas?: MetricFormulaItem[];
  steps?: PageStepItem[];
  relatedScreens?: RelatedScreenItem[];
  tips?: string[];
  warnings?: string[];
  faq?: PageFaqItem[];
  featuresJson?: string;
  lineageJson?: string;
  formulasJson?: string;
  stepsJson?: string;
  relatedScreensJson?: string;
  tipsJson?: string;
  warningsJson?: string;
  faqJson?: string;
  aiPromptHint?: string;
  status?: string;
  sortOrder?: number;
  updateTime?: string;
}


export const getHelpGuideByPath = (params: { path: string; appCode?: string; routeName?: string }) =>
  defHttp.get<SysPageHelpModel>(
    {
      url: Api.GuideByPath,
      params: {
        appCode: params.appCode || DEFAULT_APP_CODE,
        path: params.path,
        routeName: params.routeName,
      },
    },
    { errorMessageMode: 'none' },
  );

export const getHelpList = (params?: Record<string, any>) =>
  defHttp.get<SysPageHelpModel[]>({
    url: Api.List,
    params: { appCode: DEFAULT_APP_CODE, ...params },
  });


export const saveOrUpdateHelp = (params: SysPageHelpModel) =>
  defHttp.post({
    url: Api.Save,
    params: {
      appCode: DEFAULT_APP_CODE,
      ...params,
    },
  });


export const deleteHelpById = (id: string) =>
  defHttp.delete({ url: `${Api.Delete}/${id}` });


export const deleteBatchHelp = (ids: string[]) =>
  defHttp.delete({ url: Api.DeleteBatch, params: { ids: ids.join(',') } }, { joinParamsToUrl: true });


export const getRegisteredMenuRoutes = async (): Promise<
  Array<{ label: string; value: string; title: string; category?: string }>
> => {
  try {
    const res = await defHttp.get({ url: Api.UserPermissions }, { errorMessageMode: 'none' });
    const menuList = res?.menu || (Array.isArray(res) ? res : []);
    const options: Array<{ label: string; value: string; title: string; category?: string }> = [];
    const seen = new Set<string>();

    const traverse = (nodes: Array<Record<string, unknown>>, parentTitle = '') => {
      if (!Array.isArray(nodes)) return;
      for (const node of nodes) {
        const path = node.path || node.url;
        const meta = node.meta as { title?: string } | undefined;
        const title = meta?.title || node.name || path;
        if (path && typeof path === 'string' && path.startsWith('/') && !seen.has(path)) {
          seen.add(path);
          options.push({
            value: path,
            title: String(title),
            category: parentTitle || $t('appUi.kaef1a1e7'),
            label: parentTitle ? `[${parentTitle}] ${title} - ${path}` : `${title} - ${path}`,
          });
        }
        if (Array.isArray(node.children) && node.children.length > 0) {
          traverse(node.children as Array<Record<string, unknown>>, String(title));
        }
      }
    };
    traverse(menuList);
    return options;
  } catch {
    return [];
  }
};
