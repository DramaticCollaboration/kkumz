import { $t } from '/@/locales';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';
import {
  DataLineageModel,
  DEFAULT_APP_CODE,
  HelpGuideType,
  LineageDestinationItem,
  LineageSourceItem,
  MetricFormulaItem,
  PageFaqItem,
  PageFeatureItem,
  PageStepItem,
  RelatedScreenItem,
  SysPageHelpModel,
} from './help.api';

export interface GuideCategoryGroup {
  category: string;
  items: SysPageHelpModel[];
}

export function resolveHelpType(record?: Partial<SysPageHelpModel> | null): HelpGuideType {
  return String(record?.helpType || '').toUpperCase() === 'SYSTEM' ? 'SYSTEM' : 'SCREEN';
}

export function isSystemGuide(record?: Partial<SysPageHelpModel> | null): boolean {
  return resolveHelpType(record) === 'SYSTEM';
}

export function supportsCalloutLegend(record?: Partial<SysPageHelpModel> | null): boolean {
  if (!record || isSystemGuide(record)) return false;
  const pk = String(record.pageKey || '').trim().toLowerCase();
  if (/dashboard|^\/help$|^\/home$|^\/overview$/.test(pk)) {
    return false;
  }
  return true;
}

export function groupScreenGuidesByCategory(guides: SysPageHelpModel[]): GuideCategoryGroup[] {
  const map = new Map<string, SysPageHelpModel[]>();
  for (const item of guides) {
    if (isSystemGuide(item)) continue;
    const cat = item.category?.trim() || $t('appUi.kaef1a1e7');
    if (!map.has(cat)) {
      map.set(cat, []);
    }
    map.get(cat)!.push(item);
  }
  return Array.from(map.entries()).map(([category, items]) => ({ category, items }));
}

export function splitGuidesByType(guides: SysPageHelpModel[]) {
  const systemGuides = guides.filter((item) => isSystemGuide(item));
  const screenGuides = guides.filter((item) => !isSystemGuide(item));
  const screenGroups = groupScreenGuidesByCategory(screenGuides);
  return { systemGuides, screenGuides, screenGroups };
}

export const columns: BasicColumn[] = [
  {
    title: $t('appUi.k7745cc5a'),
    dataIndex: 'sortOrder',
    width: 68,
    align: 'center',
    sorter: true,
    helpMessage: $t('appUi.k23fbbc98'),
    customRender: ({ record }) => record.sortOrder ?? 0,
  },
  {
    title: $t('appUi.k80577c5f'),
    dataIndex: 'helpType',
    width: 100,
    align: 'center',
    sorter: true,
    helpMessage: $t('appUi.ke3deff2a'),
    customRender: ({ record }) => {
      const sys = isSystemGuide(record);
      return h(Tag, { color: sys ? 'cyan' : 'default' }, () => (sys ? $t('appUi.k368c6bad') : $t('appUi.ka3a0501d')));
    },
  },
  {
    title: $t('appUi.k8b1d74f0'),
    dataIndex: 'category',
    width: 120,
    align: 'center',
    sorter: true,
    helpMessage: $t('appUi.k2b8dc39d'),
    customRender: ({ text }) => h(Tag, { color: 'blue' }, () => text || $t('appUi.kaef1a1e7')),
  },
  {
    title: $t('appUi.k8cfe4107'),
    dataIndex: 'title',
    width: 210,
    align: 'left',
    sorter: true,
    helpMessage: $t('appUi.k10e9acf5'),
  },
  {
    title: $t('appUi.kfa5e96d1'),
    dataIndex: 'pageKey',
    width: 200,
    align: 'left',
    sorter: true,
    helpMessage: $t('appUi.kfc1e4986'),
    customRender: ({ text }) =>
      h('code', { class: 'px-1.5 py-0.5 bg-gray-100 rounded text-xs font-mono text-blue-600' }, text),
  },
  {
    title: $t('appUi.k3d9cd24e'),
    dataIndex: 'summary',
    align: 'left',
    sorter: true,
    helpMessage: $t('appUi.k6af93fe0'),
  },
  {
    title: $t('appUi.k2926977b'),
    dataIndex: 'status',
    width: 84,
    align: 'center',
    sorter: true,
    helpMessage: $t('appUi.kb931bf7a'),
    customRender: ({ record }) => {
      const isActive = record.status === 'ACTIVE' || record.status === 1;
      return h(Tag, { color: isActive ? 'success' : 'default' }, () => (isActive ? $t('appUi.k6a2123a3') : $t('appUi.kfae2014b')));
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'title',
    label: $t('appUi.k8cfe4107'),
    component: 'Input',
    alwaysShow: true,
    helpMessage: $t('appUi.ke3ec0281'),
    componentProps: {
      placeholder: $t('appUi.ke0b74425'),
      allowClear: true,
    },
    colProps: { span: 6 },
  },
  {
    field: 'pageKey',
    label: $t('appUi.kfa5e96d1'),
    component: 'Input',
    alwaysShow: true,
    helpMessage: $t('appUi.k32939ffe'),
    componentProps: {
      placeholder: $t('appUi.k03bd87da'),
      allowClear: true,
    },
    colProps: { span: 6 },
  },
  {
    field: 'category',
    label: $t('appUi.k8b1d74f0'),
    component: 'Input',
    helpMessage: $t('appUi.k1be0895c'),
    componentProps: {
      placeholder: $t('appUi.k0af75525'),
      allowClear: true,
    },
    colProps: { span: 6 },
  },
  {
    field: 'status',
    label: $t('appUi.k2926977b'),
    component: 'Select',
    alwaysShow: true,
    helpMessage: $t('appUi.kb4724106'),
    componentProps: {
      placeholder: $t('appUi.k21caa442'),
      allowClear: true,
      options: [
        { label: $t('appUi.k6a2123a3'), value: 'ACTIVE' },
        { label: $t('appUi.kfae2014b'), value: 'INACTIVE' },
      ],
    },
    colProps: { span: 6 },
  },
];

export function parseJsonArray<T = any>(raw: any): T[] {
  if (!raw) return [];
  if (Array.isArray(raw)) return raw;
  if (typeof raw === 'string') {
    try {
      const parsed = JSON.parse(raw);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }
  return [];
}

export function parseLineage(record?: Partial<SysPageHelpModel> | null): Required<DataLineageModel> {
  if (!record) return { sources: [], destinations: [] };
  let obj: any = record.lineage;
  if (!obj && record.lineageJson) {
    try {
      obj = typeof record.lineageJson === 'string' ? JSON.parse(record.lineageJson) : record.lineageJson;
    } catch {
      obj = {};
    }
  }
  const rawSources = Array.isArray(obj?.sources) ? obj.sources : [];
  const rawDestinations = Array.isArray(obj?.destinations) ? obj.destinations : [];

  const sources: LineageSourceItem[] = rawSources.map((s: any) => ({
    origin: s?.origin || s?.name || '',
    cycle: s?.cycle || s?.type || '',
    description: s?.description || s?.desc || '',
  }));

  const destinations: LineageDestinationItem[] = rawDestinations.map((d: any) => ({
    target: d?.target || d?.name || '',
    timing: d?.timing || d?.type || '',
    description: d?.description || d?.desc || '',
  }));

  return { sources, destinations };
}

export function parseHelpRecord(record?: Partial<SysPageHelpModel> | null) {
  const r = record || {};
  const rawFeatures = parseJsonArray<any>(r.features ?? r.featuresJson);
  const rawSteps = parseJsonArray<any>(r.steps ?? r.stepsJson);
  const rawFormulas = parseJsonArray<any>(r.formulas ?? r.formulasJson);
  const rawRelated = parseJsonArray<any>(r.relatedScreens ?? r.relatedScreensJson);

  const features: PageFeatureItem[] = rawFeatures.map((f: any) => ({
    name: f?.name || f?.title || '',
    description: f?.description || f?.detail || f?.desc || '',
    badge: f?.badge || '',
  }));

  const steps: PageStepItem[] = rawSteps.map((s: any, idx: number) => {
    const item: PageStepItem = {
      title:
        s?.step && s?.title && !String(s.title).startsWith(String(s.step))
          ? $t('appUi.kfa3c60ef', { p0: s.step, p1: s.title })
          : s?.title || $t('appUi.k377dee54', { p0: idx + 1 }),
      description: s?.description || s?.detail || '',
    };
    if (s?.path) {
      item.path = String(s.path);
    }
    return item;
  });

  const formulas: MetricFormulaItem[] = rawFormulas.map((fm: any) => ({
    name: fm?.name || fm?.metric || '',
    formula: fm?.formula || fm?.expression || '',
    description: fm?.description || fm?.explanation || '',
  }));

  const relatedScreens: RelatedScreenItem[] = rawRelated.map((rel: any) => {
    const item: RelatedScreenItem = {
      type: rel?.type || rel?.relation || $t('appUi.k6c837656'),
      title: rel?.title || '',
      description: rel?.description || rel?.relation || '',
    };
    if (rel?.path) {
      item.path = String(rel.path);
    }
    return item;
  });

  return {
    features,
    steps,
    formulas,
    relatedScreens,
    tips: parseJsonArray<string>(r.tips ?? r.tipsJson),
    warnings: parseJsonArray<string>(r.warnings ?? r.warningsJson),
    faq: parseJsonArray<PageFaqItem>(r.faq ?? r.faqJson),
    lineage: parseLineage(r),
  };
}

const APP_CODE_FOLDER_MAP: Record<string, string> = {
  SYNCWATCH: 'sync-watch',
  SYNCAPIM: 'sync-apim',
  SYNCVERSE: 'sync-verse',
  SYNCVERSE_USER: 'sync-verse',
  SYNCBOOT: 'sync-boot',
  SYNCCMS: 'sync-cms',
  SYNCSHOP: 'sync-shop',
  SYNCINSIGHT: 'sync-insight',
  SYNCSDK: 'sync-sdk',
  SYNCLLM: 'sync-llm',
  SYNCOPS: 'sync-ops',
  SYNCCRAWL: 'sync-crawl',
  SYNC_ETA: 'sync-eta',
  SYNCETA: 'sync-eta',
  TODAYCARE: 'today-care',
  AWESOMEFIT: 'awesome-fit',
  KKUMZ: 'kkumz',
};

const ROUTE_SCREEN_INDEX_MAP: Record<string, number> = {
  '/dashboard/analysis': 1,
  '/dashboard/overview': 1,
  '/executive/dashboard': 1,
  '/ecommerce/dashboard': 1,
  '/eta/dashboard': 1,
  '/today-care/macro-dashboard': 1,
  '/watch/advisories': 2,
  '/watch/feeds': 2,
  '/watch/verse-rules': 3,
  '/watch/apim-rules': 3,
  '/watch/dispatches': 4,
};

export function resolveGuideScreenshotUrl(
  appCode?: string,
  pageKey?: string,
  helpType?: string,
): string {
  if (String(helpType || '').toUpperCase() === 'SYSTEM') {
    return '';
  }
  const code = String(appCode || DEFAULT_APP_CODE).toUpperCase();
  const folder = APP_CODE_FOLDER_MAP[code] || 'kkumz';
  const pk = String(pageKey || '').trim();
  const idx =
    ROUTE_SCREEN_INDEX_MAP[pk] ||
    ((Math.abs(Array.from(pk).reduce((acc, ch) => acc + ch.charCodeAt(0), 0)) % 4) + 1);
  return `/images/screenshots/${folder}/screen_0${idx}_annotated.png`;
}

export function extractRelatedScreenPath(item?: RelatedScreenItem | null): string {
  if (!item) return '';
  if (item.path && item.path.startsWith('/')) return item.path.trim();
  const match = String(item.title || '').match(/\(([^)]+)\)/);
  if (match && match[1] && match[1].startsWith('/')) {
    return match[1].trim();
  }
  return '';
}

export function formatFullGuideClipboardText(guide?: Partial<SysPageHelpModel> | null): string {
  if (!guide) return '';
  const parsed = parseHelpRecord(guide);
  const sys = isSystemGuide(guide);
  const lines: string[] = [];
  lines.push(`${sys ? $t('appUi.k28584907') : $t('appUi.k4eb8b538')} ${guide.title || ''}`);
  if (sys) {
    lines.push($t('appUi.kb73604d7', { p0: guide.appCode || DEFAULT_APP_CODE, p1: guide.category || '시스템 안내' }));
  } else {
    lines.push($t('appUi.kd3178b08', { p0: guide.pageKey || '', p1: guide.category || '일반' }));
  }
  if (guide.summary) lines.push($t('appUi.kdcd0353a', { p0: guide.summary }));
  if (guide.overview) {
    lines.push('');
    lines.push(sys ? $t('appUi.ka7a0316f') : $t('appUi.kf6bb9889'));
    lines.push(guide.overview);
  }
  if (parsed.steps.length > 0) {
    lines.push('');
    lines.push(sys ? $t('appUi.kcb5a0967') : $t('appUi.kc4faa776'));
    parsed.steps.forEach((s) => {
      lines.push(`- ${s.title}: ${s.description}`);
    });
  }
  if (parsed.features.length > 0) {
    lines.push('');
    lines.push(sys ? $t('appUi.k5676af4c') : $t('appUi.k3559bad1'));
    parsed.features.forEach((f, idx) => {
      lines.push(`${idx + 1}. ${f.name}${f.badge ? ` [${f.badge}]` : ''}: ${f.description}`);
    });
  }
  if (parsed.lineage.sources.length > 0 || parsed.lineage.destinations.length > 0) {
    lines.push('');
    lines.push(sys ? $t('appUi.k55e4c8be') : $t('appUi.k1ff7cdb9'));
    parsed.lineage.sources.forEach((src) => {
      lines.push($t('appUi.kfcc51213', { p0: src.origin, p1: src.cycle ? ` [${src.cycle}]` : '', p2: src.description || '' }));
    });
    parsed.lineage.destinations.forEach((dst) => {
      lines.push($t('appUi.ka4ddb2b0', { p0: dst.target, p1: dst.timing ? ` [${dst.timing}]` : '', p2: dst.description || '' }));
    });
  }
  if (!sys && parsed.formulas.length > 0) {
    lines.push('');
    lines.push($t('appUi.k1b864aff'));
    parsed.formulas.forEach((fm) => {
      lines.push(`- ${fm.name}: ${fm.formula} (${fm.description || ''})`);
    });
  }
  if (parsed.tips.length > 0) {
    lines.push('');
    lines.push(sys ? $t('appUi.k746cfcd5') : $t('appUi.kd359682a'));
    parsed.tips.forEach((t) => lines.push(`- ${t}`));
  }
  if (parsed.warnings.length > 0) {
    lines.push('');
    lines.push(sys ? $t('appUi.k1ee16f4b') : $t('appUi.kae18c778'));
    parsed.warnings.forEach((w) => lines.push(`- ${w}`));
  }
  if (parsed.faq.length > 0) {
    lines.push('');
    lines.push($t('appUi.k61a46119'));
    parsed.faq.forEach((item) => {
      lines.push(`Q. ${item.q}`);
      lines.push(`A. ${item.a}`);
    });
  }
  if (guide.aiPromptHint) {
    lines.push('');
    lines.push($t('appUi.k8acf43d4'));
    lines.push(guide.aiPromptHint);
  }
  return lines.join('\n');
}
