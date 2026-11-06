import axios from 'axios';

const AI_TIMEOUT = 120000;

const http = axios.create({
  baseURL: '/api/v1/kkumz',
  timeout: 90000,
});

function unwrap<T = any>(res: any): T {
  if (res?.data && typeof res.data === 'object') {
    if ('result' in res.data && res.data.result !== undefined) {
      return res.data.result as T;
    }
    if ('data' in res.data && res.data.data !== undefined) {
      return res.data.data as T;
    }
  }
  return res?.data as T;
}

/**
 * 사이트 유형 친근한 한국어 비즈니스 라벨 변환 (내부 코드 노출 차단)
 */
export function formatSiteTypeLabel(siteType?: string): string {
  switch ((siteType || '').toUpperCase()) {
    case 'COMMERCE':
      return '쇼핑몰 스토어';
    case 'CMS_SITE':
      return '브랜드·커뮤니티 웹';
    case 'HYBRID':
      return '매거진+스토어 복합';
    default:
      return '브랜드 웹사이트';
  }
}

/**
 * 배포 방식 친근한 한국어 라벨 변환
 */
export function formatDeployModeLabel(deployMode?: string): string {
  if ((deployMode || '').toUpperCase() === 'CUSTOM_BUNDLE') {
    return '코드 번들 배포';
  }
  return '즉시 디자인 배포';
}

/**
 * 팀빌딩 포지션 한국어 라벨 변환
 */
export function formatPositionRoleLabel(role?: string): string {
  const map: Record<string, string> = {
    FRONTEND: '프론트엔드 개발',
    BACKEND: '백엔드 개발',
    DESIGNER: '브랜드·프로덕트 디자이너',
    MARKETER: '그로스 마케터',
    MD: '커머스 상품 MD',
    PLANNER: '서비스 기획·PM',
    CO_FOUNDER: '공동창업자',
  };
  return map[(role || '').toUpperCase()] || role || '팀원';
}

export const kkumzApi = {
  // 0. 스튜디오 메타데이터 (레이아웃/컴포넌트 사전, 업로드 용도 사전 및 스타터 프리셋)
  async getStudioMetadata() {
    const res = await http.get('/studio/metadata');
    return unwrap<{
      layoutLabels: Record<string, string>;
      componentLabels: Record<string, string>;
      siteTypeLabels?: Record<string, string>;
      assetTypeLabels?: Record<string, string>;
      siteTypes?: Array<{
        code: string;
        badge: string;
        title: string;
        desc: string;
      }>;
      assetTypes?: Array<{
        code: string;
        label: string;
        description: string;
      }>;
      starterPresets: Array<{
        code: string;
        label: string;
        siteType: string;
        category: string;
        prompt: string;
        tags: string[];
      }>;
    }>(res);
  },

  // 1. 아이디어 & 버전/화면설계
  async listIdeas(params?: { authorUsername?: string; siteType?: string }) {
    const res = await http.get('/ideas', { params });
    return unwrap<any[]>(res);
  },
  async createIdea(payload: {
    title?: string;
    promptText: string;
    siteType: string;
    category?: string;
    tags?: string[];
    visibility?: string;
    authorUsername?: string;
    authorDisplayName?: string;
  }) {
    const res = await http.post('/ideas', payload);
    return unwrap<any>(res);
  },
  async getIdeaDetail(ideaId: string) {
    const res = await http.get(`/ideas/${encodeURIComponent(ideaId)}`);
    return unwrap<any>(res);
  },
  async analyzeIdeaIntent(
    ideaId: string,
    payload?: {
      title?: string;
      promptText?: string;
      category?: string;
      tags?: string[];
      applyExpandedPrompt?: boolean;
      applyAssetCaptions?: boolean;
      applyRecommendedFeatures?: boolean;
    },
  ) {
    const res = await http.post(
      `/ideas/${encodeURIComponent(ideaId)}/analyze-intent`,
      payload || {},
      { timeout: AI_TIMEOUT },
    );
    return unwrap<any>(res);
  },
  async updateIdeaFeatures(
    ideaId: string,
    payload: {
      title?: string;
      promptText?: string;
      enabledFeatures: string[];
      siteType?: string;
      category?: string;
      tags?: string[];
      aiIntentAnalysis?: Record<string, any>;
      syncScreens?: boolean;
    },
  ) {
    const res = await http.put(`/ideas/${encodeURIComponent(ideaId)}/features`, payload);
    return unwrap<any>(res);
  },
  async saveOrRollbackIdeaVersion(
    ideaId: string,
    payload: {
      title?: string;
      promptText?: string;
      specMarkdown?: string;
      screens?: any[];
      rollbackToVersionNo?: number;
      createdBy?: string;
    },
  ) {
    const res = await http.post(`/ideas/${encodeURIComponent(ideaId)}/versions`, payload);
    return unwrap<any>(res);
  },
  async listIdeaScreens(ideaId: string) {
    const res = await http.get(`/ideas/${encodeURIComponent(ideaId)}/screens`);
    return unwrap<any[]>(res);
  },
  async saveIdeaScreen(ideaId: string, payload: any) {
    const res = await http.post(`/ideas/${encodeURIComponent(ideaId)}/screens`, payload);
    return unwrap<any[]>(res);
  },
  async deleteIdeaScreen(ideaId: string, screenId: string) {
    const res = await http.delete(
      `/ideas/${encodeURIComponent(ideaId)}/screens/${encodeURIComponent(screenId)}`,
    );
    return unwrap<any[]>(res);
  },

  // 1-2. 아이디어·사이트 업로드 자산 (현장/상품 사진, 로고, 안내문·요금표 문서)
  async listIdeaAssets(ideaId: string) {
    const res = await http.get(`/ideas/${encodeURIComponent(ideaId)}/assets`);
    return unwrap<any[]>(res);
  },
  async uploadIdeaAssets(
    ideaId: string,
    filesOrPayload:
      | File[]
      | {
          files: File[];
          assetType?: string;
          caption?: string;
          applyToPrompt?: boolean;
          uploadedBy?: string;
        },
    assetType?: string,
    caption?: string,
    applyToPrompt?: boolean,
    uploadedBy?: string,
  ) {
    const payload = Array.isArray(filesOrPayload)
      ? {
          files: filesOrPayload,
          assetType,
          caption,
          applyToPrompt,
          uploadedBy,
        }
      : filesOrPayload;
    const formData = new FormData();
    for (const f of payload.files) {
      formData.append('files', f);
    }
    if (payload.assetType) {
      formData.append('assetType', payload.assetType);
    }
    if (payload.caption) {
      formData.append('caption', payload.caption);
    }
    if (payload.applyToPrompt !== undefined) {
      formData.append('applyToPrompt', String(payload.applyToPrompt));
    }
    if (payload.uploadedBy) {
      formData.append('uploadedBy', payload.uploadedBy);
    }
    const res = await http.post(`/ideas/${encodeURIComponent(ideaId)}/assets`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return unwrap<any>(res);
  },
  async updateIdeaAsset(
    ideaId: string,
    assetId: string,
    payload: { assetType?: string; caption?: string; sortNo?: number; sortOrder?: number },
  ) {
    const res = await http.put(
      `/ideas/${encodeURIComponent(ideaId)}/assets/${encodeURIComponent(assetId)}`,
      {
        ...payload,
        sortNo: payload.sortNo ?? payload.sortOrder,
      },
    );
    return unwrap<any>(res);
  },
  async deleteIdeaAsset(ideaId: string, assetId: string) {
    const res = await http.delete(
      `/ideas/${encodeURIComponent(ideaId)}/assets/${encodeURIComponent(assetId)}`,
    );
    return unwrap<any>(res);
  },
  async applyAssetToPrompt(ideaId: string, assetId: string) {
    const res = await http.post(
      `/ideas/${encodeURIComponent(ideaId)}/assets/${encodeURIComponent(assetId)}/apply-prompt`,
    );
    return unwrap<any>(res);
  },
  async uploadCustomBrandLogo(ideaId: string, file: File, styleName?: string) {
    const formData = new FormData();
    formData.append('file', file);
    if (styleName) {
      formData.append('styleName', styleName);
    }
    const res = await http.post(`/ideas/${encodeURIComponent(ideaId)}/upload-logo`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return unwrap<any>(res);
  },

  // 2. AI 브랜드 네이밍 8종 & 벡터 SVG 로고 6종
  async generateNames(
    ideaId: string,
    payload?: { keywordHint?: string; toneStyle?: string; forceRegenerate?: boolean },
  ) {
    const res = await http.post(`/ideas/${encodeURIComponent(ideaId)}/generate/names`, payload || {}, {
      timeout: AI_TIMEOUT,
    });
    return unwrap<any>(res);
  },
  async generateLogos(
    ideaId: string,
    payload?: {
      brandName?: string;
      siteSlug?: string;
      stylePreference?: string;
      forceRegenerate?: boolean;
    },
  ) {
    const res = await http.post(`/ideas/${encodeURIComponent(ideaId)}/generate/logos`, payload || {}, {
      timeout: AI_TIMEOUT,
    });
    return unwrap<any>(res);
  },
  async selectBrand(
    ideaId: string,
    payload: {
      nameSuggestionId?: string;
      logoSuggestionId?: string;
      brandName?: string;
      customBrandName?: string;
      siteSlug?: string;
      customSiteSlug?: string;
      pronunciation?: string;
      brandStory?: string;
      logoSvg?: string;
      customLogoSvg?: string;
      designTokens?: Record<string, any>;
      colorPalette?: Record<string, any>;
    },
  ) {
    const normalized = {
      nameSuggestionId: payload.nameSuggestionId,
      logoSuggestionId: payload.logoSuggestionId,
      brandName: payload.brandName || payload.customBrandName,
      siteSlug: payload.siteSlug || payload.customSiteSlug,
      pronunciation: payload.pronunciation,
      brandStory: payload.brandStory,
      logoSvg: payload.logoSvg || payload.customLogoSvg,
      designTokens: payload.designTokens || payload.colorPalette,
    };
    const res = await http.put(`/ideas/${encodeURIComponent(ideaId)}/select-brand`, normalized);
    return unwrap<any>(res);
  },

  // 3. AI 마크다운 서비스 기획서
  async generateSpec(ideaId: string, forceRegenerate = false) {
    const res = await http.post(`/ideas/${encodeURIComponent(ideaId)}/generate/spec`, null, {
      params: { forceRegenerate },
      timeout: AI_TIMEOUT,
    });
    return unwrap<any>(res);
  },
  async updateSpec(
    ideaId: string,
    payload: {
      title?: string;
      promptText?: string;
      siteType?: string;
      category?: string;
      tags?: string[];
      visibility?: string;
      specMarkdown: string;
      screens?: any[];
      updatedBy?: string;
    },
  ) {
    const res = await http.put(`/ideas/${encodeURIComponent(ideaId)}/spec`, payload);
    return unwrap<any>(res);
  },

  // 4. 멀티사이트 스캐폴딩, 실시간 프리뷰 편집, WebIDE, 빌드 & 배포
  async scaffoldSite(ideaId: string) {
    const res = await http.post(`/ideas/${encodeURIComponent(ideaId)}/scaffold-site`);
    return unwrap<any>(res);
  },
  async chatRefineSite(
    siteSlug: string,
    payload: {
      instruction?: string;
      viewport?: string;
      siteName?: string;
      brandSlogan?: string;
      logoSvg?: string;
      tokenOverrides?: Record<string, any>;
      layoutOverrides?: Record<string, any>;
      username?: string;
    },
  ) {
    const res = await http.post(`/sites/${encodeURIComponent(siteSlug)}/chat-refine`, payload, {
      timeout: AI_TIMEOUT,
    });
    return unwrap<any>(res);
  },
  async getWebIdeFiles(siteSlug: string) {
    const res = await http.get(`/sites/${encodeURIComponent(siteSlug)}/webide/files`);
    return unwrap<any>(res);
  },
  async updateWebIdeFiles(
    siteSlug: string,
    payload: {
      files: Array<{ path: string; language: string; content: string }>;
      commitMessage?: string;
      simulateSyntaxError?: boolean;
      username?: string;
    },
  ) {
    const res = await http.put(`/sites/${encodeURIComponent(siteSlug)}/webide/files`, payload);
    return unwrap<any>(res);
  },
  async aiFixBuildError(siteSlug: string, errorContext?: string, username?: string) {
    const res = await http.post(
      `/sites/${encodeURIComponent(siteSlug)}/ai-fix-build`,
      {
        errorContext: errorContext || 'Auto-detect compiler error',
        username,
      },
      { timeout: AI_TIMEOUT },
    );
    return unwrap<any>(res);
  },
  async deploySite(
    siteSlug: string,
    payload?: {
      deployMode?: string;
      releaseNote?: string;
      visibility?: string;
      customDomain?: string;
      username?: string;
    },
  ) {
    const res = await http.post(`/sites/${encodeURIComponent(siteSlug)}/deploy`, payload || {});
    return unwrap<any>(res);
  },
  async rollbackSiteRelease(siteSlug: string, versionNo: number) {
    const res = await http.post(`/sites/${encodeURIComponent(siteSlug)}/rollback/${versionNo}`);
    return unwrap<any>(res);
  },

  // 4-2. GitHub 리포지토리 프로비저닝, Git 커밋/푸시 & 사용자 전용 배포 키 관리
  async getSiteGitRepo(siteSlug: string) {
    const res = await http.get(`/sites/${encodeURIComponent(siteSlug)}/git`);
    return unwrap<any>(res);
  },
  async provisionSiteGitRepo(
    siteSlug: string,
    payload?: {
      repoOwner?: string;
      repoName?: string;
      defaultBranch?: string;
      visibility?: string;
      githubPatToken?: string;
      includeFullViteScaffold?: boolean;
      username?: string;
    },
  ) {
    const res = await http.post(
      `/sites/${encodeURIComponent(siteSlug)}/git/provision`,
      payload || {},
    );
    return unwrap<any>(res);
  },
  async pushSiteToGitRepo(
    siteSlug: string,
    payload?: {
      commitMessage?: string;
      files?: Array<{ path: string; language: string; content: string }>;
      githubPatToken?: string;
      username?: string;
    },
  ) {
    const res = await http.post(`/sites/${encodeURIComponent(siteSlug)}/git/push`, payload || {});
    return unwrap<any>(res);
  },
  async rotateSiteGitKeys(
    siteSlug: string,
    payload?: {
      rotateSshKey?: boolean;
      rotateSiteApiKey?: boolean;
      githubPatToken?: string;
      username?: string;
    },
  ) {
    const res = await http.post(
      `/sites/${encodeURIComponent(siteSlug)}/git/rotate-keys`,
      payload || {},
    );
    return unwrap<any>(res);
  },
  getSshKeyDownloadUrl(siteSlug: string) {
    return `/api/v1/kkumz/sites/${encodeURIComponent(siteSlug)}/git/ssh-key/download`;
  },
  getGitArchiveDownloadUrl(siteSlug: string) {
    return `/api/v1/kkumz/sites/${encodeURIComponent(siteSlug)}/git/archive`;
  },

  // 5. 라이브 멀티사이트 런타임 & 실주문/폼제출/AI컨시어지
  async getSiteRuntime(siteSlug: string) {
    const res = await http.get(`/sites/${encodeURIComponent(siteSlug)}/runtime`);
    return unwrap<any>(res);
  },
  async createCommerceOrder(
    siteSlug: string,
    payload: {
      productId: number;
      quantity: number;
      receiverName: string;
      receiverPhone: string;
      receiverAddress: string;
      note?: string;
      discountAmount?: number;
    },
  ) {
    const res = await http.post(`/sites/${encodeURIComponent(siteSlug)}/commerce/order`, payload);
    return unwrap<any>(res);
  },
  async submitCmsForm(
    siteSlug: string,
    payload: {
      formId?: number;
      applicantName: string;
      phone: string;
      optionValue?: string;
      message?: string;
      extraFields?: Record<string, any>;
    },
  ) {
    const res = await http.post(`/sites/${encodeURIComponent(siteSlug)}/cms/form-submit`, payload);
    return unwrap<any>(res);
  },
  async askSiteConcierge(siteSlug: string, question: string) {
    const res = await http.post(
      `/sites/${encodeURIComponent(siteSlug)}/concierge`,
      { question },
      { timeout: AI_TIMEOUT },
    );
    return unwrap<any>(res);
  },

  // 6. 쇼케이스 피드, 좋아요, 댓글, 팀빌딩, 알림
  async getCommunityFeed(params?: {
    siteType?: string;
    recruitRole?: string;
    sortBy?: string;
    keyword?: string;
  }) {
    const res = await http.get('/feed', { params });
    return unwrap<any[]>(res);
  },
  async toggleSiteLike(siteId: string, username: string) {
    const res = await http.post(`/sites/${encodeURIComponent(siteId)}/like`, { username });
    return unwrap<any>(res);
  },
  async listSiteComments(siteId: string) {
    const res = await http.get(`/sites/${encodeURIComponent(siteId)}/comments`);
    return unwrap<any[]>(res);
  },
  async addSiteComment(
    siteId: string,
    payload: { username?: string; displayName?: string; authorRoleBadge?: string; content: string },
  ) {
    const res = await http.post(`/sites/${encodeURIComponent(siteId)}/comments`, payload);
    return unwrap<any[]>(res);
  },
  async listSiteRecruits(siteId: string) {
    const res = await http.get(`/sites/${encodeURIComponent(siteId)}/recruits`);
    return unwrap<any[]>(res);
  },
  async createSiteRecruit(
    siteId: string,
    payload: {
      positionRole: string;
      title: string;
      description: string;
      requiredSkills: string[];
      headcount: number;
    },
  ) {
    const res = await http.post(`/sites/${encodeURIComponent(siteId)}/recruits`, payload);
    return unwrap<any[]>(res);
  },
  async listSiteTeamMembers(siteId: string) {
    const res = await http.get(`/sites/${encodeURIComponent(siteId)}/members`);
    return unwrap<any[]>(res);
  },
  async applyToTeam(
    siteId: string,
    payload: {
      recruitId?: string;
      username?: string;
      displayName: string;
      positionRole: string;
      applicationMessage: string;
    },
  ) {
    const res = await http.post(`/sites/${encodeURIComponent(siteId)}/team/apply`, payload);
    return unwrap<any[]>(res);
  },
  async inviteTeamMember(
    siteId: string,
    payload: {
      username: string;
      displayName: string;
      memberRole: string;
      positionRole: string;
      message?: string;
    },
  ) {
    const res = await http.post(`/sites/${encodeURIComponent(siteId)}/team/invite`, payload);
    return unwrap<any[]>(res);
  },
  async updateTeamMemberStatus(
    siteId: string,
    memberId: string,
    payload: { status: string; memberRole?: string },
  ) {
    const res = await http.put(
      `/sites/${encodeURIComponent(siteId)}/team/members/${encodeURIComponent(memberId)}/status`,
      payload,
    );
    return unwrap<any[]>(res);
  },
  async reportContent(
    siteId: string,
    payload: {
      targetType?: string;
      targetId?: string;
      reporterUsername?: string;
      reasonCategory: string;
      reasonDetail: string;
    },
  ) {
    const res = await http.post(`/sites/${encodeURIComponent(siteId)}/report`, payload);
    return unwrap<any>(res);
  },
  async listNotifications(username?: string) {
    const res = await http.get('/notifications', { params: { username } });
    return unwrap<any[]>(res);
  },
  async markAllNotificationsRead(username?: string) {
    const res = await http.put('/notifications/read-all', null, { params: { username } });
    return unwrap<any>(res);
  },
};
