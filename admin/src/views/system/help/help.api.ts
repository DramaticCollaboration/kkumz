import { defHttp } from '/@/utils/http/axios';

export const HELP_API_PREFIX = '/api/v1/kkumz';
export const DEFAULT_APP_CODE = 'KKUMZ';
export const HELP_VIEWER_PATH = '/help';

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
}

export interface RelatedScreenItem {
  type: string;
  title: string;
  description?: string;
}

export interface PageFaqItem {
  q: string;
  a: string;
}

export interface SysPageHelpModel {
  id?: string;
  appCode?: string;
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

/**
 * ?꾩옱 ?붾㈃ 寃쎈줈(pageKey) 湲곗? ?꾩?留??④굔 議고쉶
 */
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

/**
 * ?꾩?留??꾩껜 紐⑸줉 議고쉶
 */
export const getHelpList = (params?: Record<string, any>) =>
  defHttp.get<SysPageHelpModel[]>({
    url: Api.List,
    params: { appCode: DEFAULT_APP_CODE, ...params },
  });

/**
 * ?꾩?留??④굔 ?곸꽭 議고쉶
 */
export const getHelpById = (id: string) =>
  defHttp.get<SysPageHelpModel>({ url: `${Api.Detail}/${id}` });

/**
 * ?꾩?留??좉퇋 ?깅줉 諛??섏젙 ??? */
export const saveOrUpdateHelp = (params: SysPageHelpModel) =>
  defHttp.post({
    url: Api.Save,
    params: {
      appCode: DEFAULT_APP_CODE,
      ...params,
    },
  });

/**
 * ?꾩?留??④굔 ??젣
 */
export const deleteHelpById = (id: string) =>
  defHttp.delete({ url: `${Api.Delete}/${id}` });

/**
 * ?꾩?留??쇨큵 ??젣
 */
export const deleteBatchHelp = (ids: string[]) =>
  defHttp.delete({ url: Api.DeleteBatch, params: { ids: ids.join(',') } });

/**
 * DB ?깅줉 硫붾돱 ?몃━ 議고쉶 (?쇱슦??寃쎈줈 ?좏깮湲곗슜)
 */
export const getRegisteredMenuRoutes = async (): Promise<
  Array<{ label: string; value: string; title: string; category?: string }>
> => {
  try {
    const res = await defHttp.get({ url: Api.UserPermissions }, { errorMessageMode: 'none' });
    const menuList = res?.menu || (Array.isArray(res) ? res : []);
    const options: Array<{ label: string; value: string; title: string; category?: string }> = [];
    const seen = new Set<string>();

    const traverse = (nodes: any[], parentTitle = '') => {
      if (!Array.isArray(nodes)) return;
      for (const node of nodes) {
        const path = node.path || node.url;
        const title = node.meta?.title || node.name || path;
        if (path && typeof path === 'string' && path.startsWith('/') && !seen.has(path)) {
          seen.add(path);
          options.push({
            value: path,
            title: String(title),
            category: parentTitle || '?쇰컲',
            label: parentTitle ? `[${parentTitle}] ${title} (${path})` : `${title} (${path})`,
          });
        }
        if (Array.isArray(node.children) && node.children.length > 0) {
          traverse(node.children, String(title));
        }
      }
    };
    traverse(menuList);
    return options;
  } catch {
    return [];
  }
};
