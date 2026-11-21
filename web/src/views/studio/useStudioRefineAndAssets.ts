import { kkumzApi } from '@/api/kkumzApi';
import type { StudioEntity, StudioState } from './useStudioState';

export function useStudioRefineAndAssets(state: StudioState) {
  const {
    activeSiteSlug,
    assetUploading,
    busy,
    busyMessage,
    catalogUploadTarget,
    chatInstruction,
    currentIdea,
    editableArticles,
    editableCmsForm,
    editableProducts,
    errorBanner,
    formStep1,
    ideas,
    imageAssets,
    previewBrandName,
    previewLogoSvg,
    renderLogoWithBrand,
    reportError,
    selectedIdeaId,
    selectedLogoId,
    showToast,
    siteRuntime,
    step4CatalogImageInputRef,
    step4ExtraAssetInputRef,
    step4HeroInputRef,
    step4Tab,
    syncCustomBrandFromSelection,
    syncVisualFormFromRuntime,
    visualForm,
  } = state;

  function focusStep4Editor(tab: 'AI_CHAT' | 'VISUAL_THEME' | 'CATALOG_EDIT' | 'FORM_EDIT') {
    step4Tab.value = tab;
  }

  function moveEditableProduct(idx: number, delta: number) {
    const target = idx + delta;
    if (target < 0 || target >= editableProducts.value.length) return;
    const tmp = editableProducts.value[idx];
    editableProducts.value[idx] = editableProducts.value[target];
    editableProducts.value[target] = tmp;
  }

  function moveEditableArticle(idx: number, delta: number) {
    const target = idx + delta;
    if (target < 0 || target >= editableArticles.value.length) return;
    const tmp = editableArticles.value[idx];
    editableArticles.value[idx] = editableArticles.value[target];
    editableArticles.value[target] = tmp;
  }

  async function applyChatRefine() {
    if (!siteRuntime.value || !chatInstruction.value.trim()) {
      if (!chatInstruction.value.trim()) {
        showToast('수정할 디자인·문구 지시 내용을 입력해 주세요.', 'error');
      }
      return;
    }
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = 'AI 대화형 수정 사항을 프리뷰에 실시간 반영하고 있습니다...';
    try {
      siteRuntime.value = (await kkumzApi.chatRefineSite(siteRuntime.value.siteSlug || '', {
        instruction: chatInstruction.value.trim(),
      })) as StudioEntity;
      syncVisualFormFromRuntime(siteRuntime.value);
      chatInstruction.value = '';
      showToast('대화형 디자인 수정 사항이 프리뷰에 즉시 반영되었습니다.', 'success');
    } catch (err) {
      reportError(
        err,
        '대화형 디자인 수정에 실패했습니다',
        'AI 수정 지시를 반영하는 중 오류가 발생했습니다.',
        () => applyChatRefine(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function applyQuickTheme(instruction: string) {
    chatInstruction.value = instruction;
    await applyChatRefine();
  }

  async function ensureIdeaForUpload(): Promise<string> {
    if (selectedIdeaId.value) {
      return selectedIdeaId.value;
    }
    const prompt = formStep1.value.promptText.trim() || formStep1.value.title.trim();
    if (!prompt) {
      throw new Error('먼저 프로젝트 제목 또는 아이디어 설명을 한 줄 이상 입력해 주세요.');
    }
    const created = (await kkumzApi.createIdea({
      title: formStep1.value.title || prompt,
      promptText: prompt,
      siteType: formStep1.value.siteType,
      category: formStep1.value.category,
      tags: formStep1.value.tags,
      visibility: 'PRIVATE',
    })) as StudioEntity;
    ideas.value = ((await kkumzApi.listIdeas({ mineOnly: true })) || []) as StudioEntity[];
    const createdId = String(created.id || '');
    selectedIdeaId.value = createdId;
    currentIdea.value = created;
    return createdId;
  }

  async function handleUploadIdeaFiles(files: File[], targetAssetType = 'AUTO') {
    if (!files.length) return;
    errorBanner.value = null;
    assetUploading.value = true;
    busy.value = true;
    busyMessage.value = `${files.length}개의 보유 사진·문서 파일을 업로드하고 텍스트/메타데이터를 분석하고 있습니다...`;
    try {
      const ideaId = await ensureIdeaForUpload();
      await kkumzApi.uploadIdeaAssets(ideaId, files, targetAssetType, '', true);
      const detail = (await kkumzApi.getIdeaDetail(ideaId)) as StudioEntity;
      currentIdea.value = detail;
      if (detail.promptText) {
        formStep1.value.promptText = detail.promptText;
      }
      const selLogo =
        (detail.logoSuggestions || []).find((l) => l.isSelected) ||
        detail.logoSuggestions?.[0];
      if (selLogo) {
        selectedLogoId.value = String(selLogo.id || '');
      }
      syncCustomBrandFromSelection(detail);
      if (activeSiteSlug.value) {
        try {
          siteRuntime.value = (await kkumzApi.getSiteRuntime(activeSiteSlug.value)) as StudioEntity;
          syncVisualFormFromRuntime(siteRuntime.value);
        } catch {
          // 프로비저닝 전 상태 무시
        }
      }
      showToast(
        `파일 ${files.length}건이 업로드되어 프로젝트 자산 보관함 및 AI 프롬프트에 자동 반영되었습니다.`,
        'success',
      );
    } catch (err: unknown) {
      const errMsg = err instanceof Error ? err.message : undefined;
      reportError(
        err,
        '파일 업로드에 실패했습니다',
        errMsg || '선택한 사진·문서 파일을 업로드하는 중 오류가 발생했습니다.',
      );
    } finally {
      assetUploading.value = false;
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function onAssetMetaChange(asset: StudioEntity) {
    if (!selectedIdeaId.value || !asset?.id) return;
    try {
      await kkumzApi.updateIdeaAsset(selectedIdeaId.value, String(asset.id), {
        assetType: asset.assetType,
        caption: asset.caption || '',
        sortOrder: asset.sortOrder,
      });
      const detail = (await kkumzApi.getIdeaDetail(selectedIdeaId.value)) as StudioEntity;
      currentIdea.value = detail;
      if (activeSiteSlug.value) {
        siteRuntime.value = (await kkumzApi.getSiteRuntime(activeSiteSlug.value)) as StudioEntity;
        syncVisualFormFromRuntime(siteRuntime.value);
      }
      showToast('자산 용도 및 설명이 저장되었습니다.', 'success');
    } catch (err) {
      reportError(err, '자산 정보 수정에 실패했습니다', '파일 용도 및 설명을 변경하는 중 오류가 발생했습니다.');
    }
  }

  async function applyAssetTextToPrompt(asset: StudioEntity) {
    if (!selectedIdeaId.value || !asset?.id) return;
    busy.value = true;
    busyMessage.value = '업로드 자료 내용을 AI 프롬프트에 반영하고 있습니다...';
    try {
      const detail = (await kkumzApi.applyAssetToPrompt(selectedIdeaId.value, String(asset.id))) as StudioEntity;
      currentIdea.value = detail;
      formStep1.value.promptText = detail.promptText || formStep1.value.promptText;
      showToast('자료 설명 및 추출 텍스트가 AI 프롬프트에 추가되었습니다.', 'success');
    } catch (err) {
      reportError(err, '프롬프트 반영에 실패했습니다', '자산 내용을 프롬프트에 반영하는 중 오류가 발생했습니다.');
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function removeIdeaAsset(asset: StudioEntity) {
    if (!selectedIdeaId.value || !asset?.id) return;
    busy.value = true;
    busyMessage.value = '선택한 업로드 파일을 삭제하고 있습니다...';
    try {
      await kkumzApi.deleteIdeaAsset(selectedIdeaId.value, String(asset.id));
      const detail = (await kkumzApi.getIdeaDetail(selectedIdeaId.value)) as StudioEntity;
      currentIdea.value = detail;
      if (activeSiteSlug.value) {
        siteRuntime.value = (await kkumzApi.getSiteRuntime(activeSiteSlug.value)) as StudioEntity;
        syncVisualFormFromRuntime(siteRuntime.value);
      }
      showToast('업로드 파일이 삭제되었습니다.', 'info');
    } catch (err) {
      reportError(err, '파일 삭제에 실패했습니다', '업로드 자산을 삭제하는 중 오류가 발생했습니다.');
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function handleStep2LogoUpload(file: File) {
    if (!file || !selectedIdeaId.value) return;
    errorBanner.value = null;
    assetUploading.value = true;
    busy.value = true;
    busyMessage.value = '보유 로고 파일을 업로드하고 브랜드 키트에 등록하고 있습니다...';
    try {
      const detail = (await kkumzApi.uploadCustomBrandLogo(
        selectedIdeaId.value,
        file,
        previewBrandName.value,
      )) as StudioEntity;
      currentIdea.value = detail;
      const selLogo =
        (detail.logoSuggestions || []).find((l) => l.isSelected) ||
        detail.logoSuggestions?.[0];
      if (selLogo) {
        selectedLogoId.value = String(selLogo.id || '');
      }
      syncCustomBrandFromSelection(detail);
      showToast('보유 로고가 업로드되어 현재 브랜드 로고로 즉시 선택되었습니다.', 'success');
    } catch (err) {
      reportError(err, '보유 로고 업로드에 실패했습니다', '로고 파일을 등록하는 중 오류가 발생했습니다.');
    } finally {
      assetUploading.value = false;
      busy.value = false;
      busyMessage.value = '';
    }
  }

  function triggerStep4HeroUpload() {
    step4HeroInputRef.value?.click();
  }

  async function saveVisualThemeOverrides() {
    if (!siteRuntime.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = '배너 문구·브랜드·사진·컬러·섹션 설정을 프리뷰에 적용하고 있습니다...';
    try {
      const updatedLogo = renderLogoWithBrand(
        siteRuntime.value.logoSvg || previewLogoSvg.value,
        visualForm.value.siteName || siteRuntime.value.siteName,
      );
      siteRuntime.value = (await kkumzApi.chatRefineSite(siteRuntime.value.siteSlug || '', {
        instruction: '',
        siteName: visualForm.value.siteName,
        brandSlogan: visualForm.value.brandSlogan,
        logoSvg: updatedLogo,
        tokenOverrides: {
          primary: visualForm.value.primaryColor,
          primaryColor: visualForm.value.primaryColor,
          secondary: visualForm.value.secondaryColor,
          secondaryColor: visualForm.value.secondaryColor,
          accent: visualForm.value.accentColor,
          accentColor: visualForm.value.accentColor,
          surface: visualForm.value.surfaceColor,
          surfaceColor: visualForm.value.surfaceColor,
          radius: visualForm.value.radius,
          surfaceMode: visualForm.value.surfaceMode,
          fontPreset: visualForm.value.fontPreset,
        },
        layoutOverrides: {
          themeArchetype: visualForm.value.themeArchetype,
          heroStyle: visualForm.value.heroStyle,
          customBadge: visualForm.value.customBadge,
          heroTitle: visualForm.value.heroTitle,
          heroSubtitle: visualForm.value.heroSubtitle,
          ctaText: visualForm.value.ctaText,
          primaryCta: visualForm.value.ctaText,
          heroBannerImage: visualForm.value.heroBannerImage,
          heroBannerCaption: visualForm.value.heroBannerCaption,
          showFlashDeal: visualForm.value.showFlashDeal,
          showPhotoGallery: visualForm.value.showPhotoGallery,
          showCmsArticles: visualForm.value.showCmsArticles,
          showCmsForm: visualForm.value.showCmsForm,
          showAiConcierge: visualForm.value.showAiConcierge,
        },
      })) as StudioEntity;
      syncVisualFormFromRuntime(siteRuntime.value);
      showToast('배너·브랜드·컬러·섹션 노출 설정이 라이브 프리뷰에 즉시 반영되었습니다.', 'success');
    } catch (err) {
      reportError(
        err,
        '배너·컬러 설정 적용에 실패했습니다',
        '비주얼 테마 설정을 저장하는 중 오류가 발생했습니다.',
        () => saveVisualThemeOverrides(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function onStep4HeroFileChange(e: Event) {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file || !selectedIdeaId.value) return;
    assetUploading.value = true;
    busy.value = true;
    busyMessage.value = '메인 히어로 배너 사진을 업로드하고 프리뷰에 반영하고 있습니다...';
    try {
      const uploaded = (await kkumzApi.uploadIdeaAssets(
        selectedIdeaId.value,
        [file],
        'HERO',
        visualForm.value.heroTitle || '',
        false,
      )) as unknown as StudioEntity[];
      const first = uploaded?.[0];
      if (first?.fileUrl) {
        visualForm.value.heroBannerImage = first.fileUrl;
        visualForm.value.heroBannerCaption = first.caption || first.originalName || '';
      }
      currentIdea.value = (await kkumzApi.getIdeaDetail(selectedIdeaId.value)) as StudioEntity;
      await saveVisualThemeOverrides();
    } catch (err) {
      reportError(err, '배너 사진 업로드에 실패했습니다', '히어로 배너 이미지를 업로드하는 중 오류가 발생했습니다.');
    } finally {
      assetUploading.value = false;
      busy.value = false;
      busyMessage.value = '';
    }
  }

  function onPickHeroAsset(fileUrl: string) {
    visualForm.value.heroBannerImage = fileUrl || '';
    const matched = imageAssets.value.find((a) => a.fileUrl === fileUrl);
    if (matched?.caption) {
      visualForm.value.heroBannerCaption = matched.caption;
    }
  }

  function triggerCatalogItemImageUpload(kind: 'PRODUCT' | 'ARTICLE', index: number) {
    catalogUploadTarget.value = { kind, index };
    step4CatalogImageInputRef.value?.click();
  }

  async function onStep4CatalogImageFileChange(e: Event) {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    const target = catalogUploadTarget.value;
    catalogUploadTarget.value = null;
    if (!file || !target || !selectedIdeaId.value) return;

    assetUploading.value = true;
    busy.value = true;
    busyMessage.value = '상품·매거진 사진을 업로드하고 있습니다...';
    try {
      const itemCaption =
        target.kind === 'PRODUCT'
          ? editableProducts.value[target.index]?.name || ''
          : editableArticles.value[target.index]?.title || '';
      const uploaded = (await kkumzApi.uploadIdeaAssets(
        selectedIdeaId.value,
        [file],
        target.kind,
        itemCaption,
        false,
      )) as unknown as StudioEntity[];
      const first = uploaded?.[0];
      if (first?.fileUrl) {
        if (target.kind === 'PRODUCT' && editableProducts.value[target.index]) {
          editableProducts.value[target.index].pic = first.fileUrl;
        } else if (target.kind === 'ARTICLE' && editableArticles.value[target.index]) {
          editableArticles.value[target.index].cover = first.fileUrl;
        }
      }
      currentIdea.value = (await kkumzApi.getIdeaDetail(selectedIdeaId.value)) as StudioEntity;
      showToast('사진이 업로드되어 항목에 적용되었습니다. 하단 상품·매거진 상세 변경사항 즉시 반영을 눌러 저장하세요.', 'success');
    } catch (err) {
      reportError(err, '항목 사진 업로드에 실패했습니다', '사진을 업로드하는 중 오류가 발생했습니다.');
    } finally {
      assetUploading.value = false;
      busy.value = false;
      busyMessage.value = '';
    }
  }

  function triggerStep4ExtraAssetUpload() {
    step4ExtraAssetInputRef.value?.click();
  }

  async function onStep4ExtraAssetFileChange(e: Event) {
    const input = e.target as HTMLInputElement;
    const files = input.files ? Array.from(input.files) : [];
    input.value = '';
    if (!files.length) return;
    await handleUploadIdeaFiles(files, 'AUTO');
  }

  async function saveFormOverrides() {
    if (!siteRuntime.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = '온라인 예약·신청 폼 및 AI 컨시어지 설정을 저장하고 있습니다...';
    try {
      const existingForm = siteRuntime.value.cmsForm || {};
      const customForm = {
        ...existingForm,
        title: editableCmsForm.value.title,
        formName: editableCmsForm.value.title,
        description: editableCmsForm.value.description,
        submitText: editableCmsForm.value.submitText,
      };
      siteRuntime.value = (await kkumzApi.chatRefineSite(siteRuntime.value.siteSlug || '', {
        instruction: '',
        layoutOverrides: {
          customCmsForm: customForm,
          conciergeGreeting: editableCmsForm.value.conciergeGreeting,
        },
      })) as StudioEntity;
      syncVisualFormFromRuntime(siteRuntime.value);
      showToast('온라인 예약·신청 폼 및 컨시어지 안내 문구가 즉시 반영되었습니다.', 'success');
    } catch (err) {
      reportError(
        err,
        '예약·신청 폼 저장에 실패했습니다',
        '온라인 신청 폼 정보를 저장하는 중 오류가 발생했습니다.',
        () => saveFormOverrides(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  function addEditableProduct() {
    editableProducts.value.push({
      id: Date.now(),
      name: '',
      pic: '',
      price: 0,
      promotionPrice: 0,
      subTitle: '',
      stock: 10,
      unit: '개',
      brandName: siteRuntime.value?.siteName || '',
      productCategoryName: '',
    });
  }

  function addEditableArticle() {
    editableArticles.value.push({
      id: Date.now(),
      title: '',
      author: siteRuntime.value?.siteName ? `${siteRuntime.value.siteName} 에디터` : '호스트',
      description: '',
      summary: '',
      bodyHtml: '',
      categoryName: '브랜드 저널',
      cover: '',
      clicks: 0,
      viewCount: 0,
    });
  }

  async function saveCatalogAndArticlesOverrides() {
    if (!siteRuntime.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = '진열 상품 및 매거진 아티클 구성을 업데이트하고 있습니다...';
    try {
      const normalizedProducts = editableProducts.value.map((p) => ({
        ...p,
        price: Number(p.price || p.promotionPrice || 0),
        promotionPrice: Number(p.promotionPrice || p.price || 0),
      }));
      const normalizedArticles = editableArticles.value.map((a) => ({
        ...a,
        summary: a.description || a.summary || '',
        bodyHtml: a.bodyHtml || `<p>${a.description || a.summary || ''}</p>`,
      }));
      siteRuntime.value = (await kkumzApi.chatRefineSite(siteRuntime.value.siteSlug || '', {
        instruction: '',
        layoutOverrides: {
          customProducts: normalizedProducts,
          customArticles: normalizedArticles,
        },
      })) as StudioEntity;
      syncVisualFormFromRuntime(siteRuntime.value);
      showToast('진열 상품 및 매거진 아티클 구성이 즉시 업데이트되었습니다.', 'success');
    } catch (err) {
      reportError(
        err,
        '상품·매거진 업데이트에 실패했습니다',
        '변경한 상품 및 아티클 정보를 반영하는 중 오류가 발생했습니다.',
        () => saveCatalogAndArticlesOverrides(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  return {
    focusStep4Editor,
    moveEditableProduct,
    moveEditableArticle,
    applyChatRefine,
    applyQuickTheme,
    ensureIdeaForUpload,
    handleUploadIdeaFiles,
    onAssetMetaChange,
    applyAssetTextToPrompt,
    removeIdeaAsset,
    handleStep2LogoUpload,
    triggerStep4HeroUpload,
    onStep4HeroFileChange,
    onPickHeroAsset,
    triggerCatalogItemImageUpload,
    onStep4CatalogImageFileChange,
    triggerStep4ExtraAssetUpload,
    onStep4ExtraAssetFileChange,
    saveVisualThemeOverrides,
    saveFormOverrides,
    addEditableProduct,
    addEditableArticle,
    saveCatalogAndArticlesOverrides,
  };
}
