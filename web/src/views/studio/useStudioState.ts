import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
  easySteps,
  steps,
  sanitizeBrandStoryText,
  renderLogoWithBrand as renderLogoWithBrandHelper,
  parseHighlightsFromMarkdown,
  isImageAsset,
  formatApiError,
  normalizeRuntime,
} from './studioFormatters';

import type { KkumzEntity } from '@/types/kkumz';

export type StudioEntity = KkumzEntity;

export function useStudioState() {
  const router = useRouter();

  const easyMode = ref(true);
  const showFeatureCatalogPanel = ref(false);
  const showAssetUploadPanel = ref(false);
  const showStep1MetaPanel = ref(false);
  const liveOpenCelebration = ref(false);

  const displayedSteps = computed(() => (easyMode.value ? easySteps : steps));

  const siteTypes = ref<Array<{ code: string; badge: string; title: string; desc: string }>>([]);
  const assetTypes = ref<Array<{ code: string; label: string; desc?: string }>>([]);
  const assetTypeLabels = ref<Record<string, string>>({});
  const studioPresets = ref<StudioEntity[]>([]);
  const layoutLabels = ref<Record<string, string>>({});
  const componentLabels = ref<Record<string, string>>({});
  const featureCatalog = ref<StudioEntity[]>([]);
  const featureCategories = ref<Array<{ code: string; label: string; desc?: string }>>([]);
  const heroStyles = ref<Array<{ code: string; label: string; desc?: string }>>([]);
  const themeArchetypes = ref<Array<{ code: string; label: string; desc?: string }>>([]);
  const fontPresets = ref<Array<{ code: string; label: string; desc?: string }>>([]);

  const enabledFeatures = ref<string[]>([]);
  const aiIntentAnalysis = ref<StudioEntity>({});
  const featureSearchQuery = ref('');
  const activeFeatureCategoryTab = ref('ALL');
  const activeEngineFilter = ref('ALL');
  const analyzingIntent = ref(false);

  const step4HeroInputRef = ref<HTMLInputElement | null>(null);
  const step4CatalogImageInputRef = ref<HTMLInputElement | null>(null);
  const step4ExtraAssetInputRef = ref<HTMLInputElement | null>(null);

  const uploadAssetType = ref('AUTO');
  const assetUploading = ref(false);
  const catalogUploadTarget = ref<{ kind: 'PRODUCT' | 'ARTICLE'; index: number } | null>(null);

  const ideas = ref<StudioEntity[]>([]);
  const selectedIdeaId = ref('');
  const currentIdea = ref<StudioEntity | null>(null);
  const accessDenied = ref(false);
  const accessDeniedOwner = ref('');
  const accessDeniedBrand = ref('');
  const activeStep = ref(1);
  const busy = ref(false);
  const busyMessage = ref('');
  const busySteps = ref<Array<{ label: string; status: 'done' | 'active' | 'pending' }>>([]);
  const generatingNames = ref(false);
  const generatingLogos = ref(false);
  const toastMessage = ref('');
  const toastType = ref<'info' | 'success' | 'error'>('info');

  watch(busy, (isBusy) => {
    if (isBusy && typeof document !== 'undefined') {
      (document.activeElement as HTMLElement | null)?.blur?.();
    }
    if (!isBusy) {
      busySteps.value = [];
      generatingNames.value = false;
      generatingLogos.value = false;
    }
  });

  const errorBanner = ref<{
    title: string;
    detail: string;
    endpoint?: string;
    retryAction?: (() => void) | null;
    canMoveToStep2?: boolean;
  } | null>(null);
  const specEditMode = ref(false);
  const step4Tab = ref<'AI_CHAT' | 'VISUAL_THEME' | 'CATALOG_EDIT' | 'FORM_EDIT'>('AI_CHAT');

  const formStep1 = ref({
    siteType: 'COMMERCE',
    title: '',
    promptText: '',
    category: 'AUTO',
    tags: [] as string[],
  });
  const newTagInput = ref('');

  const namingTonePreference = ref('ALL');
  const keywordHint = ref('');
  const selectedNameId = ref('');
  const selectedLogoId = ref('');

  const customBrandForm = ref({
    brandName: '',
    siteSlug: '',
    pronunciation: '',
    brandStory: '',
    svgCode: '',
    colorPalette: {
      primary: '#14532d',
      secondary: '#166534',
      accent: '#d97706',
      surface: '#f8fafc',
      radius: '12px',
    } as Record<string, string>,
  });

  const editableSpecMarkdown = ref('');
  const editableHighlights = ref<string[]>([]);
  const editableScreens = ref<StudioEntity[]>([]);

  const siteRuntime = ref<StudioEntity | null>(null);
  const viewportMode = ref<'DESKTOP' | 'TABLET' | 'MOBILE'>('DESKTOP');
  const chatInstruction = ref('');

  const visualForm = ref({
    siteName: '',
    brandSlogan: '',
    themeArchetype: 'EDITORIAL_BOTANICAL',
    heroStyle: 'EDITORIAL_STORY',
    surfaceMode: 'WARM',
    fontPreset: 'SERIF_EDITORIAL',
    customBadge: '',
    heroTitle: '',
    heroSubtitle: '',
    ctaText: '',
    heroBannerImage: '',
    heroBannerCaption: '',
    primaryColor: '#14532d',
    secondaryColor: '#166534',
    accentColor: '#d97706',
    surfaceColor: '#f8fafc',
    radius: '12px',
    showFlashDeal: true,
    showPhotoGallery: true,
    showCmsArticles: true,
    showCmsForm: true,
    showAiConcierge: true,
  });

  const editableProducts = ref<StudioEntity[]>([]);
  const editableArticles = ref<StudioEntity[]>([]);
  const editableCmsForm = ref({
    title: '',
    description: '',
    submitText: '온라인 신청 접수',
    conciergeGreeting: '',
  });

  const webIdeData = ref<StudioEntity | null>(null);
  const webIdeFiles = ref<Array<{ path: string; language: string; content: string }>>([]);
  const activeFileIndex = ref(0);
  const webIdeCommitMsg = ref('');

  const showGitConfigForm = ref(false);
  const gitForm = ref({
    repoOwner: 'kkumz-sites',
    repoName: '',
    defaultBranch: 'main',
    visibility: 'PRIVATE',
    githubPatToken: '',
  });
  const gitRepoInfo = computed(() => webIdeData.value?.gitRepo || null);

  const deployForm = ref({
    deployMode: 'DYNAMIC_SDUI',
    releaseNote: '',
    visibility: 'PUBLIC',
    customDomain: '',
  });

  const nameSuggestions = computed(() => currentIdea.value?.nameSuggestions || []);
  const logoSuggestions = computed(() => currentIdea.value?.logoSuggestions || []);
  const ideaVersions = computed(() => currentIdea.value?.versions || []);
  const ideaAssets = computed<StudioEntity[]>(() => currentIdea.value?.assets || []);

  const selectableAssetTypes = computed(() =>
    assetTypes.value.filter((at) => at.code !== 'AUTO'),
  );

  const imageAssets = computed(() =>
    ideaAssets.value.filter((a) => isImageAsset(a)),
  );

  const previewGalleryImages = computed<StudioEntity[]>(() => {
    const layout = (siteRuntime.value?.layoutSchema || {}) as Record<string, unknown>;
    const fromRuntime =
      siteRuntime.value?.galleryImages ||
      (layout.galleryImages as StudioEntity[] | undefined) ||
      [];
    if (fromRuntime.length > 0) return fromRuntime;
    return imageAssets.value.filter((a) => a.assetType !== 'LOGO');
  });

  const previewDownloadableDocs = computed<StudioEntity[]>(() => {
    const layout = (siteRuntime.value?.layoutSchema || {}) as Record<string, unknown>;
    const fromRuntime =
      siteRuntime.value?.downloadableDocs ||
      (layout.downloadableDocs as StudioEntity[] | undefined) ||
      [];
    if (fromRuntime.length > 0) return fromRuntime;
    return ideaAssets.value
      .filter((a) => a.assetType === 'DOC' || !isImageAsset(a))
      .map((a) => ({
        ...a,
        downloadUrl: `${a.fileUrl}?download=true`,
      }));
  });

  const previewHeroStyle = computed<Record<string, string>>(() => {
    const layout = (siteRuntime.value?.layoutSchema || {}) as Record<string, unknown>;
    const primary =
      visualForm.value.primaryColor ||
      siteRuntime.value?.designTokens?.primary ||
      '#14532d';
    const secondary =
      visualForm.value.secondaryColor ||
      siteRuntime.value?.designTokens?.secondary ||
      primary;
    const bannerImg =
      visualForm.value.heroBannerImage ||
      String(layout.heroBannerImage || '');
    if (bannerImg) {
      return {
        backgroundImage: `linear-gradient(135deg, rgba(15, 23, 42, 0.72) 0%, rgba(15, 23, 42, 0.48) 100%), url("${bannerImg}")`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        color: '#ffffff',
      };
    }
    return {
      background: `linear-gradient(135deg, ${primary} 0%, ${secondary} 100%)`,
      color: '#ffffff',
    };
  });

  const selectedNameObj = computed(
    () =>
      nameSuggestions.value.find((n) => String(n.id || '') === selectedNameId.value) ||
      nameSuggestions.value[0] ||
      null,
  );
  const selectedLogoObj = computed(
    () =>
      logoSuggestions.value.find((l) => String(l.id || '') === selectedLogoId.value) ||
      logoSuggestions.value[0] ||
      null,
  );

  const previewBrandName = computed(() => {
    const custom = customBrandForm.value.brandName?.trim();
    if (custom) return custom;
    return (
      selectedNameObj.value?.brandName ||
      currentIdea.value?.selectedBrandName ||
      currentIdea.value?.title ||
      '신규 브랜드'
    );
  });

  function renderLogoWithBrand(svgCode: unknown, overrideBrandName?: string): string {
    return renderLogoWithBrandHelper(svgCode, overrideBrandName, previewBrandName.value);
  }

  const previewLogoSvg = computed(() => {
    const raw =
      customBrandForm.value.svgCode ||
      selectedLogoObj.value?.svgCode ||
      currentIdea.value?.selectedLogoSvg ||
      '';
    return renderLogoWithBrand(raw, previewBrandName.value);
  });

  const activeSiteSlug = computed(
    () =>
      customBrandForm.value.siteSlug?.trim() ||
      siteRuntime.value?.siteSlug ||
      currentIdea.value?.site?.siteSlug ||
      currentIdea.value?.selectedSiteSlug ||
      '',
  );
  const activeWebIdeFile = computed(() => webIdeFiles.value[activeFileIndex.value] || null);

  const previewProducts = computed(() => {
    if (editableProducts.value.length > 0) return editableProducts.value;
    return siteRuntime.value?.products || siteRuntime.value?.commerce?.products || [];
  });
  const previewCategories = computed(
    () => siteRuntime.value?.categories || siteRuntime.value?.commerce?.categories || [],
  );
  const previewArticles = computed(() => {
    if (editableArticles.value.length > 0) return editableArticles.value;
    return siteRuntime.value?.articles || siteRuntime.value?.cms?.articles || [];
  });
  const previewFeatureBlocks = computed<Record<string, unknown>>(() => {
    const layout = (siteRuntime.value?.layoutSchema || {}) as Record<string, unknown>;
    return (
      siteRuntime.value?.featureBlocks ||
      (layout.featureBlocks as Record<string, unknown> | undefined) ||
      {}
    );
  });

  const recommendedFeatureCodes = computed<string[]>(() => {
    const fromAnalysis = aiIntentAnalysis.value?.recommendedFeatures;
    if (Array.isArray(fromAnalysis) && fromAnalysis.length > 0) {
      return fromAnalysis.map((c) => String(c));
    }
    return enabledFeatures.value;
  });

  const enabledFeatureSummaries = computed<Array<{ code: string; title: string }>>(() => {
    const codes =
      enabledFeatures.value.length > 0
        ? enabledFeatures.value
        : recommendedFeatureCodes.value;
    return codes.map((code) => {
      const found = featureCatalog.value.find((f) => String(f.featureCode) === String(code));
      return {
        code: String(code),
        title: String(found?.title || code),
      };
    });
  });

  const aiFeatureReasons = computed<Record<string, string>>(() => {
    const raw = aiIntentAnalysis.value?.featureReasons;
    if (raw && typeof raw === 'object') {
      return raw as Record<string, string>;
    }
    return {};
  });

  const filteredFeatureCatalog = computed<StudioEntity[]>(() => {
    const q = featureSearchQuery.value.trim().toLowerCase();
    const catTab = activeFeatureCategoryTab.value;
    const eng = activeEngineFilter.value;

    return featureCatalog.value.filter((feat) => {
      const code = String(feat.featureCode || '');
      if (eng !== 'ALL' && String(feat.engineCode || '') !== eng) {
        return false;
      }
      if (catTab === 'RECOMMENDED' && !recommendedFeatureCodes.value.includes(code)) {
        return false;
      } else if (catTab === 'SELECTED' && !enabledFeatures.value.includes(code)) {
        return false;
      } else if (
        catTab !== 'ALL' &&
        catTab !== 'RECOMMENDED' &&
        catTab !== 'SELECTED' &&
        String(feat.categoryCode || '') !== catTab
      ) {
        return false;
      }
      if (!q) return true;
      const keywords = Array.isArray(feat.searchKeywords) ? feat.searchKeywords.join(' ') : '';
      const haystack = [
        feat.featureCode,
        feat.title,
        feat.subtitle,
        feat.description,
        feat.categoryName,
        feat.badgeLabel,
        feat.engineCode,
        keywords,
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();
      return haystack.includes(q);
    });
  });

  function setStudioMode(isEasy: boolean) {
    easyMode.value = isEasy;
    if (!isEasy) {
      showFeatureCatalogPanel.value = true;
      showStep1MetaPanel.value = true;
    } else if (activeStep.value === 3 || activeStep.value === 5 || activeStep.value === 6) {
      activeStep.value = siteRuntime.value ? 4 : 2;
    }
  }

  function openLiveSiteBySlug(siteSlug: string) {
    if (!siteSlug) return;
    router.push({ name: 'LiveSiteRendererView', params: { siteSlug } });
  }

  function onStep1PromptInput() {
    if (!formStep1.value.title.trim()) {
      const firstLine = formStep1.value.promptText
        .split(/\r?\n/)[0]
        .trim()
        .slice(0, 48);
      formStep1.value.title = firstLine;
    }
  }

  function showToast(msg: string, type: 'info' | 'success' | 'error' = 'info') {
    toastMessage.value = msg;
    toastType.value = type;
    const duration = type === 'error' ? 6800 : 3800;
    setTimeout(() => {
      if (toastMessage.value === msg) toastMessage.value = '';
    }, duration);
  }

  function reportError(
    err: unknown,
    title: string,
    fallbackDetail: string,
    retryAction?: () => void,
    canMoveToStep2 = false,
  ) {
    const { detail, endpoint } = formatApiError(err, fallbackDetail);
    errorBanner.value = {
      title,
      detail,
      endpoint,
      retryAction: retryAction || null,
      canMoveToStep2,
    };
    showToast(`${title}: ${detail}`, 'error');
  }

  function runErrorRetry() {
    const fn = errorBanner.value?.retryAction;
    if (fn) {
      fn();
    }
  }

  function forceMoveToStep2() {
    errorBanner.value = null;
    activeStep.value = 2;
  }

  function formatAssetTypeLabel(code: string) {
    if (!code) return '';
    return assetTypeLabels.value[code] || code;
  }

  function toggleFeatureCard(code: string) {
    if (!code) return;
    const idx = enabledFeatures.value.indexOf(code);
    if (idx >= 0) {
      enabledFeatures.value.splice(idx, 1);
    } else {
      enabledFeatures.value.push(code);
    }
  }

  function selectRecommendedFeatures() {
    const recs = recommendedFeatureCodes.value;
    if (recs.length === 0) {
      showToast('먼저 AI로 내 설명에 맞는 기능 미리 추천받기를 실행해 주세요.', 'info');
      return;
    }
    const merged = new Set<string>([...recs]);
    enabledFeatures.value = Array.from(merged);
    showToast(`AI가 추천한 ${enabledFeatures.value.length}종 핵심 기능이 선택되었습니다.`, 'success');
  }

  function applyExpandedPrompt() {
    const exp = String(aiIntentAnalysis.value?.expandedPrompt || '').trim();
    if (!exp) return;
    formStep1.value.promptText = exp;
    showToast('AI가 구체화한 상세 기획 프롬프트가 적용되었습니다.', 'success');
  }

  function applyStarterPreset(preset: StudioEntity) {
    formStep1.value.siteType = preset.siteType || 'COMMERCE';
    formStep1.value.title = preset.title || preset.label || '';
    formStep1.value.promptText = preset.prompt || preset.promptText || '';
    if (preset.category) {
      formStep1.value.category = preset.category;
    }
  }

  function addStep1Tag() {
    const val = newTagInput.value.trim().replace(/^#/, '');
    if (!val) return;
    if (!formStep1.value.tags.includes(val)) {
      formStep1.value.tags.push(val);
    }
    newTagInput.value = '';
  }

  function syncCustomBrandFromSelection(detail?: StudioEntity | null) {
    const idea = detail || currentIdea.value;
    const selName =
      nameSuggestions.value.find((n) => String(n.id || '') === selectedNameId.value) ||
      nameSuggestions.value[0] ||
      null;
    const selLogo =
      logoSuggestions.value.find((l) => String(l.id || '') === selectedLogoId.value) ||
      logoSuggestions.value[0] ||
      null;

    const brandName = selName?.brandName || idea?.selectedBrandName || idea?.title || '';
    const siteSlug = selName?.siteSlug || idea?.selectedSiteSlug || idea?.site?.siteSlug || '';
    const pronunciation = selName?.pronunciation || '';
    const brandStory = sanitizeBrandStoryText(selName?.brandStory || '');
    const palette = selLogo?.colorPalette || {};
    const rawSvg = selLogo?.svgCode || idea?.selectedLogoSvg || '';

    customBrandForm.value = {
      brandName,
      siteSlug,
      pronunciation,
      brandStory,
      svgCode: renderLogoWithBrand(rawSvg, brandName),
      colorPalette: {
        primary: String(palette.primary || '#14532d'),
        secondary: String(palette.secondary || '#166534'),
        accent: String(palette.accent || '#d97706'),
        surface: String(palette.surface || '#f8fafc'),
        radius: String(palette.radius || '12px'),
      },
    };
  }

  function onCustomBrandNameInput() {
    const name = customBrandForm.value.brandName.trim();
    if (name && customBrandForm.value.svgCode) {
      customBrandForm.value.svgCode = renderLogoWithBrand(customBrandForm.value.svgCode, name);
    }
  }

  function onCustomSlugInput() {
    customBrandForm.value.siteSlug = customBrandForm.value.siteSlug
      .toLowerCase()
      .replace(/[^a-z0-9-]/g, '-')
      .replace(/-+/g, '-');
  }

  function onCustomPaletteChange() {
    const baseLogo = selectedLogoObj.value;
    if (!baseLogo?.svgCode) return;
    let updatedSvg = renderLogoWithBrand(baseLogo.svgCode, previewBrandName.value);
    const oldPal = baseLogo.colorPalette || {};
    const newPal = customBrandForm.value.colorPalette;
    for (const key of ['primary', 'secondary', 'accent', 'surface']) {
      const oldHex = String(oldPal[key] || '').trim();
      const newHex = String(newPal[key] || '').trim();
      if (oldHex && newHex && oldHex.toLowerCase() !== newHex.toLowerCase()) {
        const escaped = oldHex.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        updatedSvg = updatedSvg.replace(new RegExp(escaped, 'gi'), newHex);
      }
    }
    customBrandForm.value.svgCode = updatedSvg;
  }

  function syncStep3FromIdea(detail: StudioEntity | null) {
    if (!detail) return;
    editableSpecMarkdown.value = detail.specMarkdown || '';
    editableHighlights.value = parseHighlightsFromMarkdown(editableSpecMarkdown.value);
    const rawScreens = detail.screenSpecs || detail.screens || [];
    editableScreens.value = rawScreens.map((s, idx) => ({
      id: s.id || '',
      screenKey: s.screenKey || `scr_${idx + 1}`,
      screenTitle: s.screenTitle || s.title || `화면 섹션 ${idx + 1}`,
      routePath: s.routePath || `/${s.screenKey || `section-${idx + 1}`}`,
      layoutType: s.layoutType || s.layout || 'HERO_BANNER_GRID',
      description: s.description || '',
      components: Array.isArray(s.components) ? [...s.components] : [],
      sortOrder: s.sortOrder ?? idx + 1,
    }));
  }

  function addSpecHighlightItem() {
    editableHighlights.value.push('새로운 핵심 서비스 기능 또는 공간 특징을 입력하세요');
    syncHighlightsToMarkdown();
  }

  function removeSpecHighlightItem(idx: number) {
    editableHighlights.value.splice(idx, 1);
    syncHighlightsToMarkdown();
  }

  function syncHighlightsToMarkdown() {
    const cleanLines = editableHighlights.value
      .map((l) => l.trim())
      .filter((l) => l.length > 0);
    const bulletBlock = cleanLines.map((l) => `- ${l}`).join('\n');
    const titleHeader = `# ${previewBrandName.value} 서비스 기능명세서\n\n## 핵심 제공 기능 및 공간 경험\n`;
    editableSpecMarkdown.value = `${titleHeader}${bulletBlock}\n`;
  }

  function syncMarkdownToHighlights() {
    editableHighlights.value = parseHighlightsFromMarkdown(editableSpecMarkdown.value);
  }

  function addEditableScreen() {
    const nextNo = editableScreens.value.length + 1;
    editableScreens.value.push({
      id: '',
      screenKey: `custom_sec_${Date.now()}`,
      screenTitle: `신규 커스텀 섹션 0${nextNo}`,
      routePath: `/section-${nextNo}`,
      layoutType: 'PRODUCT_CATALOG_GRID',
      description: '이 화면 섹션에서 제공할 콘텐츠와 핵심 기능을 설명해 주세요.',
      components: ['ProductGrid', 'AiConciergeWidget'],
      sortOrder: nextNo,
    });
  }

  function moveEditableScreen(idx: number, delta: number) {
    const targetIdx = idx + delta;
    if (targetIdx < 0 || targetIdx >= editableScreens.value.length) return;
    const temp = editableScreens.value[idx];
    editableScreens.value[idx] = editableScreens.value[targetIdx];
    editableScreens.value[targetIdx] = temp;
    editableScreens.value.forEach((s, i) => {
      s.sortOrder = i + 1;
    });
  }

  function syncVisualFormFromRuntime(rt: StudioEntity | null) {
    if (!rt) return;
    const normalized = normalizeRuntime(rt) as StudioEntity;
    siteRuntime.value = normalized;
    const layout = (normalized.layoutSchema || {}) as Record<string, unknown>;
    const tokens = (normalized.designTokens || {}) as Record<string, string>;
    const rawCap = layout.heroBannerCaption as Record<string, unknown> | string | undefined;
    const capStr =
      typeof rawCap === 'object' && rawCap !== null
        ? String(rawCap.title || '')
        : String(rawCap || '');

    visualForm.value = {
      siteName: normalized.siteName || '',
      brandSlogan: normalized.brandSlogan || '',
      themeArchetype: normalized.themeArchetype || String(layout.themeArchetype || 'EDITORIAL_BOTANICAL'),
      heroStyle: normalized.heroStyle || String(layout.heroStyle || 'EDITORIAL_STORY'),
      surfaceMode: normalized.surfaceMode || tokens.surfaceMode || 'WARM',
      fontPreset: normalized.fontPreset || tokens.fontPreset || 'SERIF_EDITORIAL',
      customBadge: String(layout.customBadge || '공식 브랜드 쇼케이스'),
      heroTitle: String(layout.heroTitle || normalized.siteName || ''),
      heroSubtitle: String(layout.heroSubtitle || normalized.brandSlogan || ''),
      ctaText: String(layout.ctaText || layout.primaryCta || '시그니처 컬렉션 탐색'),
      heroBannerImage: String(layout.heroBannerImage || ''),
      heroBannerCaption: capStr,
      primaryColor: tokens.primary || tokens.primaryColor || '#14532d',
      secondaryColor: tokens.secondary || tokens.secondaryColor || tokens.primary || '#166534',
      accentColor: tokens.accent || tokens.accentColor || '#d97706',
      surfaceColor: tokens.surface || tokens.surfaceColor || '#f8fafc',
      radius: tokens.radius || '12px',
      showFlashDeal: layout.showFlashDeal !== false,
      showPhotoGallery: layout.showPhotoGallery !== false,
      showCmsArticles: layout.showCmsArticles !== false,
      showCmsForm: layout.showCmsForm !== false,
      showAiConcierge: layout.showAiConcierge !== false,
    };
    const prods = normalized.products || normalized.commerce?.products || [];
    editableProducts.value = prods.map((p) => ({
      id: p.id,
      name: p.name,
      pic: p.pic || '',
      price: Number(p.price || p.promotionPrice || 0),
      promotionPrice: Number(p.promotionPrice || p.price || 0),
      subTitle: p.subTitle || '',
      stock: Number(p.stock ?? 0),
      unit: p.unit || '개',
      brandName: p.brandName || normalized.siteName,
      productCategoryName: p.productCategoryName || p.categoryName || '',
    }));
    const arts = normalized.articles || normalized.cms?.articles || [];
    editableArticles.value = arts.map((a) => ({
      id: a.id,
      title: a.title,
      author: a.author || a.authorName || '',
      description: a.description || a.summary || '',
      summary: a.summary || a.description || '',
      bodyHtml: a.bodyHtml || a.contentHtml || `<p>${a.description || a.summary || ''}</p>`,
      categoryName: a.categoryName || '',
      cover: a.cover || a.thumbnailUrl || '',
      clicks: a.clicks || a.viewCount || 0,
      viewCount: a.viewCount || a.clicks || 0,
    }));

    const formObj = ((layout.customCmsForm as StudioEntity) || normalized.cmsForm || normalized.cms?.forms?.[0] || {}) as StudioEntity;
    const copyOverrides = (layout.copyOverrides || {}) as Record<string, string>;
    editableCmsForm.value = {
      title: formObj.title || formObj.formName || `${normalized.siteName || '브랜드'} 온라인 신청 · 문의`,
      description:
        formObj.description ||
        '원하시는 문의 내용이나 신청 정보를 남겨주시면 확인 후 신속히 안내해 드립니다.',
      submitText: formObj.submitText || '온라인 신청 접수',
      conciergeGreeting:
        String(layout.conciergeGreeting || copyOverrides.conciergeSubtitle || ''),
    };

    deployForm.value.deployMode = normalized.deployMode || 'DYNAMIC_SDUI';
    deployForm.value.visibility = (normalized.isPublished ? normalized.visibility : 'PUBLIC') || 'PUBLIC';
    deployForm.value.customDomain = normalized.customDomain || '';
  }

  return {
    easyMode,
    showFeatureCatalogPanel,
    showAssetUploadPanel,
    showStep1MetaPanel,
    liveOpenCelebration,
    displayedSteps,
    siteTypes,
    assetTypes,
    assetTypeLabels,
    studioPresets,
    layoutLabels,
    componentLabels,
    featureCatalog,
    featureCategories,
    heroStyles,
    themeArchetypes,
    fontPresets,
    enabledFeatures,
    aiIntentAnalysis,
    featureSearchQuery,
    activeFeatureCategoryTab,
    activeEngineFilter,
    analyzingIntent,
    step4HeroInputRef,
    step4CatalogImageInputRef,
    step4ExtraAssetInputRef,
    uploadAssetType,
    assetUploading,
    catalogUploadTarget,
    ideas,
    selectedIdeaId,
    currentIdea,
    accessDenied,
    accessDeniedOwner,
    accessDeniedBrand,
    activeStep,
    busy,
    busyMessage,
    busySteps,
    generatingNames,
    generatingLogos,
    toastMessage,
    toastType,
    errorBanner,
    specEditMode,
    step4Tab,
    formStep1,
    newTagInput,
    namingTonePreference,
    keywordHint,
    selectedNameId,
    selectedLogoId,
    customBrandForm,
    editableSpecMarkdown,
    editableHighlights,
    editableScreens,
    siteRuntime,
    viewportMode,
    chatInstruction,
    visualForm,
    editableProducts,
    editableArticles,
    editableCmsForm,
    webIdeData,
    webIdeFiles,
    activeFileIndex,
    webIdeCommitMsg,
    showGitConfigForm,
    gitForm,
    gitRepoInfo,
    deployForm,
    nameSuggestions,
    logoSuggestions,
    ideaVersions,
    ideaAssets,
    selectableAssetTypes,
    imageAssets,
    previewGalleryImages,
    previewDownloadableDocs,
    previewHeroStyle,
    selectedNameObj,
    selectedLogoObj,
    previewBrandName,
    previewLogoSvg,
    activeSiteSlug,
    activeWebIdeFile,
    previewProducts,
    previewCategories,
    previewArticles,
    previewFeatureBlocks,
    recommendedFeatureCodes,
    enabledFeatureSummaries,
    aiFeatureReasons,
    filteredFeatureCatalog,
    setStudioMode,
    openLiveSiteBySlug,
    onStep1PromptInput,
    showToast,
    reportError,
    runErrorRetry,
    forceMoveToStep2,
    formatAssetTypeLabel,
    toggleFeatureCard,
    selectRecommendedFeatures,
    applyExpandedPrompt,
    applyStarterPreset,
    addStep1Tag,
    renderLogoWithBrand,
    syncCustomBrandFromSelection,
    onCustomBrandNameInput,
    onCustomSlugInput,
    onCustomPaletteChange,
    syncStep3FromIdea,
    addSpecHighlightItem,
    removeSpecHighlightItem,
    syncHighlightsToMarkdown,
    syncMarkdownToHighlights,
    addEditableScreen,
    moveEditableScreen,
    syncVisualFormFromRuntime,
  };
}

export type StudioState = ReturnType<typeof useStudioState>;
