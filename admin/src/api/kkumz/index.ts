import { defHttp } from '/@/utils/http/axios';

export enum KkumzAdminApi {
  DashboardSummary = '/api/v1/kkumz/admin/dashboard/summary',
  Ideas = '/api/v1/kkumz/admin/ideas',
  Prompts = '/api/v1/kkumz/admin/prompts',
  Sites = '/api/v1/kkumz/admin/sites',
  Builds = '/api/v1/kkumz/admin/builds',
  Projects = '/api/v1/kkumz/admin/projects',
  ModerationReports = '/api/v1/kkumz/admin/moderation/reports',
}

// 1. 운영 종합 대시보드 KPI
export const getDashboardSummary = () =>
  defHttp.get({ url: KkumzAdminApi.DashboardSummary });

// 2. AI 스튜디오 관제 — 아이디어 & 명세서 관리
export const getAdminIdeaList = (params?: Recordable) =>
  defHttp.get({ url: KkumzAdminApi.Ideas, params });

export const getAdminIdeaDetail = (ideaId: string) =>
  defHttp.get({ url: `/api/v1/kkumz/ideas/${encodeURIComponent(ideaId)}` });

export const deleteAdminIdea = (ideaId: string) =>
  defHttp.delete({ url: `${KkumzAdminApi.Ideas}/${encodeURIComponent(ideaId)}` });

// 3. AI 프롬프트 & 브랜드 필터 관리
export const getAdminPromptList = (params?: Recordable) =>
  defHttp.get({ url: KkumzAdminApi.Prompts, params });

export const saveAdminPrompt = (params: Recordable, isUpdate = false) =>
  isUpdate
    ? defHttp.put({ url: `${KkumzAdminApi.Prompts}/${encodeURIComponent(params.id)}`, params })
    : defHttp.post({ url: KkumzAdminApi.Prompts, params });

export const deleteAdminPrompt = (promptId: string) =>
  defHttp.delete({ url: `${KkumzAdminApi.Prompts}/${encodeURIComponent(promptId)}` });

// 4. 멀티사이트 & 도메인 관제
export const getAdminSiteList = (params?: Recordable) =>
  defHttp.get({ url: KkumzAdminApi.Sites, params });

export const updateAdminSiteStatus = (siteId: string, params: { status?: string; visibility?: string }) =>
  defHttp.put({ url: `${KkumzAdminApi.Sites}/${encodeURIComponent(siteId)}/status`, params });

export const rollbackAdminSiteRelease = (siteSlug: string, versionNo: number) =>
  defHttp.post({ url: `/api/v1/kkumz/sites/${encodeURIComponent(siteSlug)}/rollback/${versionNo}` });

// 5. 빌드 파이프라인 & 에러 로그
export const getAdminBuildList = (params?: Recordable) =>
  defHttp.get({ url: KkumzAdminApi.Builds, params });

export const triggerAdminAiFixBuild = (siteSlug: string, errorContext?: string) =>
  defHttp.post({
    url: `/api/v1/kkumz/sites/${encodeURIComponent(siteSlug)}/ai-fix-build`,
    params: { errorContext: errorContext || 'Admin triggered AI build patch', username: 'admin' },
  });

// 6. 커뮤니티 프로젝트 & 팀빌딩 관리
export const getAdminProjectList = (params?: Recordable) =>
  defHttp.get({ url: KkumzAdminApi.Projects, params });

// 7. 신고 심사 & 모더레이션
export const getAdminModerationList = (params?: Recordable) =>
  defHttp.get({ url: KkumzAdminApi.ModerationReports, params });

export const resolveModerationReport = (
  reportId: string,
  params: { status: string; resolutionNote: string; suspendSite?: boolean; resolvedBy?: string },
) =>
  defHttp.put({
    url: `${KkumzAdminApi.ModerationReports}/${encodeURIComponent(reportId)}/resolve`,
    params,
  });
