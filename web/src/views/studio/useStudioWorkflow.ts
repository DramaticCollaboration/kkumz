import { useRoute, useRouter } from 'vue-router';
import {
  kkumzApi,
  isAuthenticated,
  openAuthModal,
  formatSiteTypeLabel,
} from '@/api/kkumzApi';
import { sanitizeBrandStoryText } from './studioFormatters';
import type { StudioEntity, StudioState } from './useStudioState';

export function useStudioWorkflow(state: StudioState, loadWebIde: (slug: string) => Promise<void>) {
  const route = useRoute();
  const router = useRouter();

  const {
    accessDenied,
    accessDeniedBrand,
    accessDeniedOwner,
    activeSiteSlug,
    activeStep,
    aiIntentAnalysis,
    analyzingIntent,
    assetTypeLabels,
    assetTypes,
    busy,
    busyMessage,
    busySteps,
    componentLabels,
    currentIdea,
    customBrandForm,
    easyMode,
    editableHighlights,
    editableScreens,
    editableSpecMarkdown,
    enabledFeatures,
    errorBanner,
    featureCatalog,
    featureCategories,
    fontPresets,
    formStep1,
    generatingLogos,
    generatingNames,
    heroStyles,
    ideas,
    keywordHint,
    layoutLabels,
    logoSuggestions,
    nameSuggestions,
    namingTonePreference,
    previewBrandName,
    renderLogoWithBrand,
    reportError,
    selectedIdeaId,
    selectedLogoId,
    selectedLogoObj,
    selectedNameId,
    selectedNameObj,
    showToast,
    siteRuntime,
    siteTypes,
    specEditMode,
    studioPresets,
    syncCustomBrandFromSelection,
    syncHighlightsToMarkdown,
    syncStep3FromIdea,
    syncVisualFormFromRuntime,
    themeArchetypes,
  } = state;

  async function runIntentAnalysis(applyExpandedAndAssets = false) {
    if (busy.value || analyzingIntent.value) return;
    if (!formStep1.value.promptText.trim()) {
      showToast('먼저 아이디어 상세 설명을 입력해 주세요.', 'error');
      return;
    }
    analyzingIntent.value = true;
    errorBanner.value = null;
    try {
      let targetId = selectedIdeaId.value;
      if (!targetId) {
        const created = (await kkumzApi.createIdea({
          title: formStep1.value.title || formStep1.value.promptText.slice(0, 40),
          promptText: formStep1.value.promptText,
          siteType: formStep1.value.siteType,
          category: formStep1.value.category,
          tags: formStep1.value.tags,
          enabledFeatures: enabledFeatures.value,
          visibility: 'PRIVATE',
        })) as StudioEntity;
        ideas.value = ((await kkumzApi.listIdeas({ mineOnly: true })) || []) as StudioEntity[];
        const createdId = String(created.id || '');
        selectedIdeaId.value = createdId;
        targetId = createdId;
      }

      const updated = (await kkumzApi.analyzeIdeaIntent(targetId, {
        title: formStep1.value.title,
        promptText: formStep1.value.promptText,
        category: formStep1.value.category,
        tags: formStep1.value.tags,
        applyExpandedPrompt: applyExpandedAndAssets,
        applyRecommendedFeatures: true,
      })) as StudioEntity;

      currentIdea.value = updated;
      formStep1.value.siteType = updated.siteType || formStep1.value.siteType;
      formStep1.value.category = updated.category || formStep1.value.category;
      if (Array.isArray(updated.tags) && updated.tags.length > 0) {
        formStep1.value.tags = [...updated.tags];
      }
      if (applyExpandedAndAssets && updated.promptText) {
        formStep1.value.promptText = updated.promptText;
      }
      if (Array.isArray(updated.enabledFeatures)) {
        enabledFeatures.value = [...updated.enabledFeatures];
      }
      if (updated.aiIntentAnalysis && typeof updated.aiIntentAnalysis === 'object') {
        aiIntentAnalysis.value = { ...updated.aiIntentAnalysis };
      }
      syncStep3FromIdea(updated);
      ideas.value = ((await kkumzApi.listIdeas({ mineOnly: true })) || []) as StudioEntity[];
      showToast(
        `AI 분석 완료! 추천 아키텍처 ${formatSiteTypeLabel(formStep1.value.siteType)}와 맞춤 기능 ${enabledFeatures.value.length}종이 자동 구성되었습니다.`,
        'success',
      );
    } catch (err) {
      reportError(
        err,
        'AI 의도·기능 카탈로그 분석 중 오류가 발생했습니다',
        '프롬프트 분석 요청을 처리하는 중 오류가 발생했습니다.',
        () => runIntentAnalysis(applyExpandedAndAssets),
      );
    } finally {
      analyzingIntent.value = false;
    }
  }

  async function saveEnabledFeaturesToServer(showNotification = false) {
    if (!selectedIdeaId.value) return;
    try {
      const updated = (await kkumzApi.updateIdeaFeatures(selectedIdeaId.value, {
        siteType: formStep1.value.siteType,
        category: formStep1.value.category,
        tags: formStep1.value.tags,
        enabledFeatures: enabledFeatures.value,
        aiIntentAnalysis: aiIntentAnalysis.value,
      })) as StudioEntity;
      currentIdea.value = updated;
      syncStep3FromIdea(updated);
      if (showNotification) {
        showToast(`선택한 기능 카드 ${enabledFeatures.value.length}종이 프로젝트 및 블루프린트에 반영되었습니다.`, 'success');
      }
    } catch (err) {
      if (showNotification) {
        reportError(
          err,
          '선택 기능 저장에 실패했습니다',
          '기능 카탈로그 선택 목록을 저장하는 중 오류가 발생했습니다.',
          () => saveEnabledFeaturesToServer(true),
        );
      }
    }
  }

  async function removeEditableScreen(idx: number) {
    const target = editableScreens.value[idx];
    editableScreens.value.splice(idx, 1);
    if (selectedIdeaId.value && target?.id) {
      try {
        await kkumzApi.deleteIdeaScreen(selectedIdeaId.value, String(target.id));
      } catch {
        // saveSpecVersion 시 동기화
      }
    }
  }

  async function performGenerateNames(forceRegenerate = false) {
    if (!selectedIdeaId.value) return;
    generatingNames.value = true;
    try {
      let updated: StudioEntity | null = null;
      try {
        updated = (await kkumzApi.generateNames(selectedIdeaId.value, {
          keywordHint: keywordHint.value.trim(),
          toneStyle: namingTonePreference.value !== 'ALL' ? namingTonePreference.value : undefined,
          forceRegenerate,
        })) as StudioEntity;
      } catch (err) {
        try {
          const recovered = (await kkumzApi.getIdeaDetail(selectedIdeaId.value)) as StudioEntity;
          if (!forceRegenerate && (recovered?.nameSuggestions?.length || 0) >= 8) {
            updated = recovered;
          } else {
            throw err;
          }
        } catch {
          throw err;
        }
      }

      if (currentIdea.value && updated) {
        currentIdea.value = {
          ...currentIdea.value,
          category: updated.category || currentIdea.value.category,
          selectedBrandName: updated.selectedBrandName || currentIdea.value.selectedBrandName,
          selectedSiteSlug: updated.selectedSiteSlug || currentIdea.value.selectedSiteSlug,
          nameSuggestions: updated.nameSuggestions || [],
          logoSuggestions:
            (updated.logoSuggestions?.length || 0) >= (currentIdea.value.logoSuggestions?.length || 0)
              ? updated.logoSuggestions
              : currentIdea.value.logoSuggestions,
        };
      } else {
        currentIdea.value = updated;
      }

      if (currentIdea.value?.category) {
        formStep1.value.category = currentIdea.value.category;
      }
      const sel =
        (currentIdea.value?.nameSuggestions || []).find((n) => n.isSelected) ||
        currentIdea.value?.nameSuggestions?.[0];
      selectedNameId.value = String(sel?.id || '');
      syncCustomBrandFromSelection(currentIdea.value);
    } finally {
      generatingNames.value = false;
    }
  }

  async function performGenerateLogos(forceRegenerate = false) {
    if (!selectedIdeaId.value) return;
    generatingLogos.value = true;
    try {
      let updated: StudioEntity | null = null;
      try {
        updated = (await kkumzApi.generateLogos(selectedIdeaId.value, {
          brandName: previewBrandName.value,
          stylePreference: namingTonePreference.value !== 'ALL' ? namingTonePreference.value : undefined,
          forceRegenerate,
        })) as StudioEntity;
      } catch (err) {
        try {
          const recovered = (await kkumzApi.getIdeaDetail(selectedIdeaId.value)) as StudioEntity;
          if (!forceRegenerate && (recovered?.logoSuggestions?.length || 0) >= 6) {
            updated = recovered;
          } else {
            throw err;
          }
        } catch {
          throw err;
        }
      }

      if (currentIdea.value && updated) {
        currentIdea.value = {
          ...currentIdea.value,
          selectedLogoSvg: updated.selectedLogoSvg || currentIdea.value.selectedLogoSvg,
          designTokens: updated.designTokens || currentIdea.value.designTokens,
          logoSuggestions: updated.logoSuggestions || [],
          nameSuggestions:
            (updated.nameSuggestions?.length || 0) >= (currentIdea.value.nameSuggestions?.length || 0)
              ? updated.nameSuggestions
              : currentIdea.value.nameSuggestions,
        };
      } else {
        currentIdea.value = updated;
      }

      const sel =
        (currentIdea.value?.logoSuggestions || []).find((l) => l.isSelected) ||
        currentIdea.value?.logoSuggestions?.[0];
      selectedLogoId.value = String(sel?.id || '');
      syncCustomBrandFromSelection(currentIdea.value);
    } finally {
      generatingLogos.value = false;
    }
  }

  async function runStep2GenerationsParallel(
    needNames: boolean,
    needLogos: boolean,
    forceRegenerate: boolean,
    nameStepIndex: number,
    logoStepIndex: number,
  ): Promise<{ nameErr: unknown; logoErr: unknown }> {
    let nameErr: unknown = null;
    let logoErr: unknown = null;

    const tasks: Promise<void>[] = [];

    if (needNames && needLogos) {
      if (busySteps.value[nameStepIndex]) busySteps.value[nameStepIndex].status = 'active';
      if (busySteps.value[logoStepIndex]) busySteps.value[logoStepIndex].status = 'active';
      busyMessage.value = 'AI가 브랜드 네이밍 8종과 벡터 SVG 로고 6종을 동시에 병렬 생성하고 있습니다...';
    } else if (needNames) {
      if (busySteps.value[nameStepIndex]) busySteps.value[nameStepIndex].status = 'active';
      busyMessage.value = 'AI가 브랜드 네이밍 후보 8종을 생성하고 있습니다...';
    } else if (needLogos) {
      if (busySteps.value[logoStepIndex]) busySteps.value[logoStepIndex].status = 'active';
      busyMessage.value = 'AI가 벡터 SVG 로고 후보 6종을 생성하고 있습니다...';
    }

    if (needNames) {
      tasks.push(
        performGenerateNames(forceRegenerate)
          .then(() => {
            if (busySteps.value[nameStepIndex]) busySteps.value[nameStepIndex].status = 'done';
            if (needLogos && generatingLogos.value) {
              busyMessage.value = '브랜드 네이밍 8종 완료! 벡터 SVG 로고 6종을 마무리하고 있습니다...';
            }
          })
          .catch((err) => {
            nameErr = err;
          }),
      );
    }

    if (needLogos) {
      tasks.push(
        performGenerateLogos(forceRegenerate)
          .then(() => {
            if (busySteps.value[logoStepIndex]) busySteps.value[logoStepIndex].status = 'done';
            if (needNames && generatingNames.value) {
              busyMessage.value = '벡터 SVG 로고 6종 완료! 브랜드 네이밍 8종을 마무리하고 있습니다...';
            }
          })
          .catch((err) => {
            logoErr = err;
          }),
      );
    }

    await Promise.allSettled(tasks);

    if (currentIdea.value) {
      syncCustomBrandFromSelection(currentIdea.value);
    }

    return { nameErr, logoErr };
  }

  async function regenerateNames() {
    if (!selectedIdeaId.value || busy.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = 'AI가 브랜드 네이밍 후보 8종을 새로 생성하고 있습니다...';
    try {
      await performGenerateNames(true);
      showToast('AI 브랜드 네이밍 8종이 새로 생성되었습니다.', 'success');
    } catch (err) {
      reportError(
        err,
        'AI 브랜드 네이밍 생성에 실패했습니다',
        '브랜드 네이밍 8종을 생성하는 중 오류가 발생했습니다.',
        () => regenerateNames(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function regenerateLogos() {
    if (!selectedIdeaId.value || busy.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = 'AI가 벡터 SVG 로고 후보 6종을 새로 생성하고 있습니다...';
    try {
      await performGenerateLogos(true);
      showToast('벡터 SVG 로고 6종이 새로 생성되었습니다.', 'success');
    } catch (err) {
      reportError(
        err,
        'AI 벡터 SVG 로고 생성에 실패했습니다',
        '벡터 SVG 로고 6종을 생성하는 중 오류가 발생했습니다.',
        () => regenerateLogos(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function ensureStep2Candidates() {
    if (!selectedIdeaId.value) return;
    const needNames = nameSuggestions.value.length < 8;
    const needLogos = logoSuggestions.value.length < 6;
    if (!needNames && !needLogos) return;

    busy.value = true;
    busySteps.value = [
      { label: '1. AI 브랜드 네이밍 8종 생성', status: needNames ? 'active' : 'done' },
      { label: '2. 벡터 SVG 로고 6종 병렬 생성', status: needLogos ? 'active' : 'done' },
    ];
    try {
      const { nameErr, logoErr } = await runStep2GenerationsParallel(
        needNames,
        needLogos,
        false,
        0,
        1,
      );
      if (nameErr) {
        reportError(
          nameErr,
          'AI 브랜드 네이밍 자동 생성에 실패했습니다',
          '네이밍 후보를 불러오지 못했습니다. 상단 다른 이름 8가지 추천받기 버튼으로 다시 시도해 주세요.',
          () => regenerateNames(),
        );
        return;
      }
      if (logoErr) {
        reportError(
          logoErr,
          'AI 벡터 SVG 로고 자동 생성에 실패했습니다',
          'SVG 로고 후보를 불러오지 못했습니다. 상단 다른 로고 6가지 추천받기 버튼으로 다시 시도해 주세요.',
          () => regenerateLogos(),
        );
      }
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function onSelectIdea(ideaId: string, allowWhenBusy = false) {
    if (busy.value && !allowWhenBusy) return;
    if (!ideaId) {
      selectedIdeaId.value = '';
      currentIdea.value = null;
      siteRuntime.value = null;
      accessDenied.value = false;
      activeStep.value = 1;
      formStep1.value = {
        siteType: 'COMMERCE',
        title: '',
        promptText: '',
        category: 'AUTO',
        tags: [],
      };
      enabledFeatures.value = [];
      if (route.params.ideaId) {
        router.replace({ name: 'StudioView' }).catch(() => {});
      }
      return;
    }
    selectedIdeaId.value = ideaId;
    errorBanner.value = null;
    accessDenied.value = false;
    if (String(route.params.ideaId || '') !== ideaId) {
      router.replace({ name: 'StudioView', params: { ideaId }, query: route.query }).catch(() => {});
    }
    try {
      const detail = (await kkumzApi.getIdeaDetail(ideaId)) as StudioEntity;
      if (selectedIdeaId.value !== ideaId) return;
      if (!detail.canEdit) {
        accessDenied.value = true;
        accessDeniedOwner.value = detail.authorDisplayName || detail.authorUsername || '원작자';
        accessDeniedBrand.value = detail.selectedBrandName || detail.title || '해당 브랜드';
        currentIdea.value = null;
        siteRuntime.value = null;
        return;
      }
      currentIdea.value = detail;

      formStep1.value = {
        siteType: detail.siteType || 'COMMERCE',
        title: detail.title || '',
        promptText: detail.promptText || '',
        category: detail.category || 'AUTO',
        tags: Array.isArray(detail.tags) ? [...detail.tags] : [],
      };
      enabledFeatures.value = Array.isArray(detail.enabledFeatures) ? [...detail.enabledFeatures] : [];
      aiIntentAnalysis.value =
        detail.aiIntentAnalysis && typeof detail.aiIntentAnalysis === 'object'
          ? { ...detail.aiIntentAnalysis }
          : {};
      syncStep3FromIdea(detail);

      const selName = (detail.nameSuggestions || []).find((n) => n.isSelected) || detail.nameSuggestions?.[0];
      selectedNameId.value = String(selName?.id || '');

      const selLogo = (detail.logoSuggestions || []).find((l) => l.isSelected) || detail.logoSuggestions?.[0];
      selectedLogoId.value = String(selLogo?.id || '');

      syncCustomBrandFromSelection(detail);

      const slug = detail.site?.siteSlug || detail.deployedSites?.[0]?.siteSlug || detail.selectedSiteSlug;
      if (slug) {
        try {
          siteRuntime.value = (await kkumzApi.getSiteRuntime(slug)) as StudioEntity;
          syncVisualFormFromRuntime(siteRuntime.value);
        } catch {
          siteRuntime.value = null;
        }
      } else {
        siteRuntime.value = null;
      }

      if (activeStep.value === 2 && !allowWhenBusy) {
        await ensureStep2Candidates();
      }
    } catch (err: unknown) {
      if (selectedIdeaId.value !== ideaId) return;
      const errObj = err as { response?: { status?: number } };
      const status = errObj?.response?.status;
      if (status === 403 || status === 401) {
        accessDenied.value = true;
        accessDeniedOwner.value = '소유자';
        accessDeniedBrand.value = '비공개 프로젝트';
        currentIdea.value = null;
        siteRuntime.value = null;
        return;
      }
      reportError(
        err,
        '프로젝트 상세 정보를 불러오지 못했습니다',
        '선택한 아이디어 정보를 조회하는 중 오류가 발생했습니다.',
        () => onSelectIdea(ideaId),
      );
    }
  }

  async function executeScaffoldWithRecovery(targetIdeaId: string): Promise<StudioEntity> {
    const needsBrandSync =
      !currentIdea.value?.selectedLogoSvg ||
      (customBrandForm.value.brandName.trim() &&
        customBrandForm.value.brandName.trim() !== currentIdea.value?.selectedBrandName);
    const candidateSvg =
      customBrandForm.value.svgCode ||
      selectedLogoObj.value?.svgCode ||
      currentIdea.value?.selectedLogoSvg ||
      '';
    const candidateBrand =
      customBrandForm.value.brandName.trim() ||
      selectedNameObj.value?.brandName ||
      currentIdea.value?.selectedBrandName ||
      '';

    if (needsBrandSync && (candidateSvg || candidateBrand)) {
      try {
        const syncedSvg = candidateSvg && candidateBrand
          ? renderLogoWithBrand(candidateSvg, candidateBrand)
          : candidateSvg;
        currentIdea.value = (await kkumzApi.selectBrand(targetIdeaId, {
          nameSuggestionId: selectedNameId.value || undefined,
          logoSuggestionId: selectedLogoId.value || undefined,
          brandName: candidateBrand || undefined,
          siteSlug: customBrandForm.value.siteSlug.trim() || selectedNameObj.value?.siteSlug || undefined,
          pronunciation: customBrandForm.value.pronunciation.trim() || selectedNameObj.value?.pronunciation || undefined,
          brandStory:
            customBrandForm.value.brandStory.trim() ||
            sanitizeBrandStoryText(selectedNameObj.value?.brandStory || '') ||
            undefined,
          logoSvg: syncedSvg || undefined,
          designTokens: { ...customBrandForm.value.colorPalette },
        })) as StudioEntity;
      } catch {
        // scaffoldSite 자체의 브랜드·로고 보완 로직으로 계속 진행
      }
    }

    try {
      return (await kkumzApi.scaffoldSite(targetIdeaId)) as StudioEntity;
    } catch (err) {
      try {
        const recovered = (await kkumzApi.getIdeaDetail(targetIdeaId)) as StudioEntity;
        const slug =
          recovered?.site?.siteSlug ||
          recovered?.deployedSites?.[0]?.siteSlug ||
          recovered?.selectedSiteSlug;
        if (slug) {
          const rt = (await kkumzApi.getSiteRuntime(slug)) as StudioEntity;
          if (rt && rt.siteSlug) {
            currentIdea.value = recovered;
            return rt;
          }
        }
      } catch {
        // 복구 조회 실패 시 원본 에러 재전파
      }
      throw err;
    }
  }

  async function goToStep(stepNo: number) {
    if (busy.value) return;
    errorBanner.value = null;
    activeStep.value = stepNo;
    const targetIdeaId = selectedIdeaId.value;
    if (!targetIdeaId) return;

    if (stepNo === 2) {
      await ensureStep2Candidates();
    } else if (stepNo === 3) {
      const targetBrand = previewBrandName.value;
      const specMd = currentIdea.value?.specMarkdown || '';
      const needSpecSync =
        !currentIdea.value?.screenSpecs?.length ||
        (targetBrand && !specMd.includes(targetBrand));
      if (needSpecSync) {
        busy.value = true;
        busyMessage.value = `'${targetBrand}' 브랜드 기준으로 AI 3단계 기능명세서 및 화면 설계를 준비하고 있습니다...`;
        try {
          currentIdea.value = (await kkumzApi.generateSpec(targetIdeaId, false)) as StudioEntity;
        } catch {
          // 기존 상태 유지
        } finally {
          busy.value = false;
          busyMessage.value = '';
        }
      }
      if (currentIdea.value) {
        syncStep3FromIdea(currentIdea.value);
      }
    } else if (stepNo === 4 || stepNo === 6) {
      if (!siteRuntime.value) {
        busy.value = true;
        busyMessage.value = '라이브 멀티사이트 환경을 구성하고 있습니다...';
        try {
          const rt = await executeScaffoldWithRecovery(targetIdeaId);
          if (selectedIdeaId.value !== targetIdeaId) return;
          siteRuntime.value = rt;
          syncVisualFormFromRuntime(siteRuntime.value);
        } catch (err) {
          if (selectedIdeaId.value !== targetIdeaId) return;
          reportError(
            err,
            '사이트 환경 구성에 실패했습니다',
            `${stepNo}단계 사이트 런타임을 구성하는 중 오류가 발생했습니다.`,
            () => goToStep(stepNo),
          );
        } finally {
          busy.value = false;
          busyMessage.value = '';
        }
      } else {
        syncVisualFormFromRuntime(siteRuntime.value);
      }
    } else if (stepNo === 5) {
      busy.value = true;
      try {
        if (!siteRuntime.value) {
          busyMessage.value = '라이브 멀티사이트 환경을 구성하고 있습니다...';
          const rt = await executeScaffoldWithRecovery(targetIdeaId);
          if (selectedIdeaId.value !== targetIdeaId) return;
          siteRuntime.value = rt;
          syncVisualFormFromRuntime(siteRuntime.value);
        }
        if (activeSiteSlug.value) {
          busyMessage.value = 'WebIDE 프로젝트 소스 파일을 불러오고 있습니다...';
          await loadWebIde(activeSiteSlug.value);
        }
      } catch (err) {
        if (selectedIdeaId.value !== targetIdeaId) return;
        reportError(
          err,
          'WebIDE 코드 편집기 준비에 실패했습니다',
          '프로젝트 소스 파일을 불러오는 중 오류가 발생했습니다.',
          () => goToStep(5),
        );
      } finally {
        busy.value = false;
        busyMessage.value = '';
      }
    }
  }

  async function createNewIdeaFromStep1() {
    if (busy.value) return;
    if (!formStep1.value.promptText.trim()) {
      showToast('만들고 싶은 사이트에 대한 설명을 입력해 주세요.', 'error');
      return;
    }
    if (!formStep1.value.title.trim()) {
      formStep1.value.title = formStep1.value.promptText.split(/\r?\n/)[0].trim().slice(0, 48);
    }
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = '새 브랜드 프로젝트를 생성하고 있습니다...';
    busySteps.value = [
      { label: '1. 신규 프로젝트 생성 & 맞춤 기능 분석', status: 'active' },
      { label: '2. AI 추천 브랜드 이름 8가지 병렬 생성', status: 'pending' },
      { label: '3. 고화질 브랜드 로고 6가지 병렬 생성', status: 'pending' },
    ];
    try {
      const created = (await kkumzApi.createIdea({
        title: formStep1.value.title,
        promptText: formStep1.value.promptText,
        siteType: formStep1.value.siteType,
        category: formStep1.value.category,
        tags: formStep1.value.tags,
        enabledFeatures: enabledFeatures.value,
        visibility: 'PRIVATE',
      })) as StudioEntity;
      ideas.value = ((await kkumzApi.listIdeas({ mineOnly: true })) || []) as StudioEntity[];
      await onSelectIdea(String(created.id || ''), true);
      if (busySteps.value[0]) busySteps.value[0].status = 'done';

      activeStep.value = 2;

      const { nameErr, logoErr } = await runStep2GenerationsParallel(true, true, true, 1, 2);
      if (nameErr) {
        reportError(
          nameErr,
          '새 프로젝트는 생성되었으나 AI 이름 생성에 실패했습니다',
          'AI 브랜드 이름 생성 중 서버 오류가 발생했습니다. 다시 시도를 눌러 이름을 생성해 주세요.',
          () => regenerateNames(),
        );
        return;
      }
      if (logoErr) {
        reportError(
          logoErr,
          'AI 고화질 브랜드 로고 생성 중 오류가 발생했습니다',
          '프로젝트 및 이름 8가지는 준비되었으나 로고 생성 중 서버 오류가 발생했습니다. 다시 시도를 눌러 로고를 생성해 주세요.',
          () => regenerateLogos(),
        );
        return;
      }
    } catch (err) {
      reportError(
        err,
        '새 브랜드 프로젝트 생성에 실패했습니다',
        '프로젝트를 생성하는 중 서버 오류가 발생했습니다.',
        () => createNewIdeaFromStep1(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function saveCurrentIdeaAndGoStep2() {
    if (busy.value) return;
    if (!formStep1.value.promptText.trim()) {
      showToast('만들고 싶은 사이트에 대한 설명을 한 줄 이상 입력해 주세요.', 'error');
      return;
    }
    if (!formStep1.value.title.trim()) {
      formStep1.value.title = formStep1.value.promptText.split(/\r?\n/)[0].trim().slice(0, 48);
    }
    if (!selectedIdeaId.value) {
      await createNewIdeaFromStep1();
      return;
    }
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = '1단계 아이디어 설정을 저장하고 있습니다...';
    const prevPrompt = currentIdea.value?.promptText || '';
    const promptChanged = prevPrompt.trim() !== formStep1.value.promptText.trim();
    const prevTags = JSON.stringify(currentIdea.value?.tags || []);
    const nextTags = JSON.stringify(formStep1.value.tags || []);
    const prevFeatures = JSON.stringify(currentIdea.value?.enabledFeatures || []);
    const nextFeatures = JSON.stringify(enabledFeatures.value || []);
    const step1Changed =
      promptChanged ||
      (currentIdea.value?.title || '').trim() !== formStep1.value.title.trim() ||
      (currentIdea.value?.siteType || '') !== formStep1.value.siteType ||
      (currentIdea.value?.category || 'AUTO') !== (formStep1.value.category || 'AUTO') ||
      prevTags !== nextTags ||
      prevFeatures !== nextFeatures;

    let willGenNames = promptChanged || nameSuggestions.value.length < 8;
    let willGenLogos = promptChanged || logoSuggestions.value.length < 6;

    busySteps.value = [
      { label: '1. 아이디어 설정 확인 · 맞춤 기능 자동 반영', status: 'active' },
      { label: '2. AI 추천 브랜드 이름 8가지 생성', status: willGenNames ? 'pending' : 'done' },
      { label: '3. 고화질 브랜드 로고 6가지 생성', status: willGenLogos ? 'pending' : 'done' },
    ];

    try {
      try {
        if (enabledFeatures.value.length === 0) {
          currentIdea.value = (await kkumzApi.analyzeIdeaIntent(selectedIdeaId.value, {
            title: formStep1.value.title,
            promptText: formStep1.value.promptText,
            category: formStep1.value.category,
            tags: formStep1.value.tags,
            applyExpandedPrompt: false,
            applyRecommendedFeatures: true,
          })) as StudioEntity;
          if (Array.isArray(currentIdea.value?.enabledFeatures)) {
            enabledFeatures.value = [...currentIdea.value.enabledFeatures];
          }
          if (currentIdea.value?.siteType) {
            formStep1.value.siteType = currentIdea.value.siteType;
          }
          if (currentIdea.value?.aiIntentAnalysis) {
            aiIntentAnalysis.value = { ...currentIdea.value.aiIntentAnalysis };
          }
          ideas.value = ((await kkumzApi.listIdeas({ mineOnly: true })) || []) as StudioEntity[];
        } else if (step1Changed) {
          currentIdea.value = (await kkumzApi.updateSpec(selectedIdeaId.value, {
            title: formStep1.value.title,
            promptText: formStep1.value.promptText,
            siteType: formStep1.value.siteType,
            category: formStep1.value.category,
            tags: formStep1.value.tags,
            enabledFeatures: enabledFeatures.value,
            aiIntentAnalysis: aiIntentAnalysis.value,
            specMarkdown: editableSpecMarkdown.value,
          })) as StudioEntity;
          ideas.value = ((await kkumzApi.listIdeas({ mineOnly: true })) || []) as StudioEntity[];
        } else {
          currentIdea.value = (await kkumzApi.getIdeaDetail(selectedIdeaId.value)) as StudioEntity;
        }
        willGenNames = promptChanged || nameSuggestions.value.length < 8;
        willGenLogos = promptChanged || logoSuggestions.value.length < 6;
        if (busySteps.value[0]) busySteps.value[0].status = 'done';
        if (busySteps.value[1]) busySteps.value[1].status = willGenNames ? 'pending' : 'done';
        if (busySteps.value[2]) busySteps.value[2].status = willGenLogos ? 'pending' : 'done';
        syncCustomBrandFromSelection(currentIdea.value);
      } catch (err) {
        reportError(
          err,
          '아이디어 설정 저장에 실패했습니다',
          '1단계 프로젝트 정보를 저장하는 중 서버 오류가 발생했습니다.',
          () => saveCurrentIdeaAndGoStep2(),
        );
        return;
      }

      activeStep.value = 2;

      if (willGenNames || willGenLogos) {
        const { nameErr, logoErr } = await runStep2GenerationsParallel(
          willGenNames,
          willGenLogos,
          promptChanged,
          1,
          2,
        );
        if (nameErr) {
          reportError(
            nameErr,
            'AI 브랜드 이름 생성 중 오류가 발생했습니다',
            '1단계 아이디어는 정상 저장되어 2단계로 이동했으나, AI 브랜드 이름 생성 요청이 실패했습니다. 다시 시도 또는 상단 다른 이름 8가지 추천받기를 눌러주세요.',
            () => regenerateNames(),
          );
          return;
        }
        if (logoErr) {
          reportError(
            logoErr,
            'AI 고화질 브랜드 로고 생성 중 오류가 발생했습니다',
            '아이디어 저장 및 브랜드 이름 8가지는 완료되었으나, 로고 생성 요청이 실패했습니다. 다시 시도 또는 상단 다른 로고 6가지 추천받기를 눌러주세요.',
            () => regenerateLogos(),
          );
          return;
        }
      }
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function runOneClickAutoComplete() {
    if (busy.value) return;
    if (!formStep1.value.promptText.trim()) {
      showToast('만들고 싶은 사이트에 대한 설명을 한 줄 이상 입력해 주세요.', 'error');
      return;
    }
    if (!formStep1.value.title.trim()) {
      formStep1.value.title = formStep1.value.promptText.split(/\r?\n/)[0].trim().slice(0, 48);
    }
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = 'AI가 아이디어를 분석해 이름·로고·기능을 자동 구성하고 완성된 사이트 화면을 만들고 있습니다...';
    busySteps.value = [
      { label: '1. 아이디어 분석 & 필수 기능 자동 구성', status: 'active' },
      { label: '2. AI 대표 브랜드 이름 & 고화질 로고 자동 생성', status: 'pending' },
      { label: '3. 내 사이트 화면 자동 제작 및 미리보기 오픈', status: 'pending' },
    ];
    try {
      let targetId = selectedIdeaId.value;
      const prevPrompt = currentIdea.value?.promptText || '';
      const promptChanged = !targetId || prevPrompt.trim() !== formStep1.value.promptText.trim();

      if (!targetId) {
        const created = (await kkumzApi.createIdea({
          title: formStep1.value.title,
          promptText: formStep1.value.promptText,
          siteType: formStep1.value.siteType,
          category: formStep1.value.category,
          tags: formStep1.value.tags,
          enabledFeatures: enabledFeatures.value,
          visibility: 'PRIVATE',
        })) as StudioEntity;
        ideas.value = ((await kkumzApi.listIdeas({ mineOnly: true })) || []) as StudioEntity[];
        const createdId = String(created.id || '');
        await onSelectIdea(createdId, true);
        targetId = createdId;
      }

      const analyzed = (await kkumzApi.analyzeIdeaIntent(targetId, {
        title: formStep1.value.title,
        promptText: formStep1.value.promptText,
        category: formStep1.value.category,
        tags: formStep1.value.tags,
        applyExpandedPrompt: false,
        applyRecommendedFeatures: enabledFeatures.value.length === 0,
      })) as StudioEntity;
      currentIdea.value = analyzed;
      if (Array.isArray(analyzed?.enabledFeatures)) {
        enabledFeatures.value = [...analyzed.enabledFeatures];
      }
      if (analyzed?.siteType) {
        formStep1.value.siteType = analyzed.siteType;
      }
      if (analyzed?.aiIntentAnalysis) {
        aiIntentAnalysis.value = { ...analyzed.aiIntentAnalysis };
      }
      if (busySteps.value[0]) busySteps.value[0].status = 'done';

      const needNames = promptChanged || nameSuggestions.value.length === 0;
      const needLogos = promptChanged || logoSuggestions.value.length === 0;
      if (needNames || needLogos) {
        await runStep2GenerationsParallel(needNames, needLogos, promptChanged, 1, 1);
      } else if (busySteps.value[1]) {
        busySteps.value[1].status = 'done';
      }
      if (busySteps.value[1]) busySteps.value[1].status = 'done';
      if (busySteps.value[2]) busySteps.value[2].status = 'active';
      busyMessage.value = '선택된 브랜드 이름과 고화질 로고로 내 사이트 화면을 자동 제작하고 있습니다...';

      const rt = await executeScaffoldWithRecovery(targetId);
      if (busySteps.value[2]) busySteps.value[2].status = 'done';

      siteRuntime.value = rt;
      syncVisualFormFromRuntime(siteRuntime.value);
      currentIdea.value = (await kkumzApi.getIdeaDetail(targetId)) as StudioEntity;
      syncStep3FromIdea(currentIdea.value);
      ideas.value = ((await kkumzApi.listIdeas({ mineOnly: true })) || []) as StudioEntity[];
      activeStep.value = 4;
      showToast(
        '나만 볼 수 있는 사이트 초안이 생성되었습니다. 배포 단계에서 라이브 오픈을 실행하면 다른 사용자에게 공개됩니다.',
        'success',
      );
    } catch (err) {
      reportError(
        err,
        '1분 자동 완성 처리 중 오류가 발생했습니다',
        '사이트를 자동 구성하는 중 오류가 발생했습니다. 다시 시도를 눌러주세요.',
        () => runOneClickAutoComplete(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  function selectNameCandidate(name: StudioEntity) {
    if (busy.value) return;
    selectedNameId.value = String(name.id || '');
    customBrandForm.value.brandName = name.brandName || '';
    customBrandForm.value.siteSlug = name.siteSlug || '';
    customBrandForm.value.pronunciation = name.pronunciation || '';
    customBrandForm.value.brandStory = sanitizeBrandStoryText(name.brandStory || '');
    const baseSvg = selectedLogoObj.value?.svgCode || customBrandForm.value.svgCode || '';
    if (baseSvg) {
      customBrandForm.value.svgCode = renderLogoWithBrand(baseSvg, name.brandName);
    }
  }

  function selectLogoCandidate(logo: StudioEntity) {
    if (busy.value) return;
    selectedLogoId.value = String(logo.id || '');
    const pal = logo.colorPalette || {};
    customBrandForm.value.colorPalette = {
      primary: String(pal.primary || '#14532d'),
      secondary: String(pal.secondary || '#166534'),
      accent: String(pal.accent || '#d97706'),
      surface: String(pal.surface || '#f8fafc'),
      radius: String(pal.radius || '12px'),
    };
    customBrandForm.value.svgCode = renderLogoWithBrand(logo.svgCode || '', previewBrandName.value);
  }

  async function confirmBrandAndNext() {
    if (!selectedIdeaId.value || busy.value) return;
    const targetIdeaId = selectedIdeaId.value;
    const finalBrandName = customBrandForm.value.brandName.trim() || selectedNameObj.value?.brandName || '';
    if (!finalBrandName) {
      showToast('브랜드 이름을 선택하거나 직접 입력해 주세요.', 'error');
      return;
    }
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = easyMode.value
      ? `'${finalBrandName}' 이름과 로고로 내 사이트 화면을 자동 제작하고 있습니다...`
      : '선택 및 커스텀한 브랜드 아이덴티티를 확정하고 3단계 기획서를 준비하고 있습니다...';
    busySteps.value = easyMode.value
      ? [
          { label: '1. 브랜드 이름 & 고화질 로고 확정', status: 'active' },
          { label: '2. AI 맞춤 화면 구성 자동 설계', status: 'pending' },
          { label: '3. 내 사이트 화면 자동 제작 및 미리보기 오픈', status: 'pending' },
        ]
      : [
          { label: '1. 브랜드 아이덴티티 확정', status: 'active' },
          { label: '2. 3단계 AI 기능명세서 · 화면설계 생성', status: 'pending' },
        ];
    try {
      const finalSvg = renderLogoWithBrand(
        customBrandForm.value.svgCode || selectedLogoObj.value?.svgCode || '',
        finalBrandName,
      );
      currentIdea.value = (await kkumzApi.selectBrand(targetIdeaId, {
        nameSuggestionId: selectedNameId.value,
        logoSuggestionId: selectedLogoId.value,
        brandName: finalBrandName,
        siteSlug: customBrandForm.value.siteSlug.trim() || selectedNameObj.value?.siteSlug || '',
        pronunciation: customBrandForm.value.pronunciation.trim() || selectedNameObj.value?.pronunciation || '',
        brandStory: customBrandForm.value.brandStory.trim() || sanitizeBrandStoryText(selectedNameObj.value?.brandStory || ''),
        logoSvg: finalSvg,
        designTokens: { ...customBrandForm.value.colorPalette },
      })) as StudioEntity;
      if (busySteps.value[0]) busySteps.value[0].status = 'done';
      if (busySteps.value[1]) busySteps.value[1].status = 'active';
      busyMessage.value = `'${finalBrandName}' 브랜드에 맞춰 화면 구성을 자동 설계하고 있습니다...`;
      currentIdea.value = (await kkumzApi.generateSpec(targetIdeaId, false)) as StudioEntity;
      if (busySteps.value[1]) busySteps.value[1].status = 'done';
      syncStep3FromIdea(currentIdea.value);

      if (easyMode.value) {
        if (busySteps.value[2]) busySteps.value[2].status = 'active';
        busyMessage.value = `'${finalBrandName}' 사이트의 상품·배너·예약창 화면을 자동 제작하고 있습니다...`;
        const rt = await executeScaffoldWithRecovery(targetIdeaId);
        if (busySteps.value[2]) busySteps.value[2].status = 'done';
        siteRuntime.value = rt;
        syncVisualFormFromRuntime(siteRuntime.value);
        currentIdea.value = (await kkumzApi.getIdeaDetail(targetIdeaId)) as StudioEntity;
        ideas.value = ((await kkumzApi.listIdeas({ mineOnly: true })) || []) as StudioEntity[];
        activeStep.value = 4;
        showToast(
          '나만 볼 수 있는 사이트 초안이 생성되었습니다. 배포 단계에서 라이브 오픈을 실행하면 다른 사용자에게 공개됩니다.',
          'success',
        );
      } else {
        activeStep.value = 3;
        ideas.value = ((await kkumzApi.listIdeas({ mineOnly: true })) || []) as StudioEntity[];
      }
    } catch (err) {
      reportError(
        err,
        '브랜드 확정 및 사이트 화면 제작에 실패했습니다',
        '선택한 브랜드 이름·로고를 반영하는 중 오류가 발생했습니다.',
        () => confirmBrandAndNext(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function regenerateSpec() {
    if (!selectedIdeaId.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = 'AI가 서비스 기능명세서 및 화면 설계 블루프린트를 새로 생성하고 있습니다...';
    try {
      currentIdea.value = (await kkumzApi.generateSpec(selectedIdeaId.value, true)) as StudioEntity;
      syncStep3FromIdea(currentIdea.value);
      showToast('AI 기능명세서 및 화면 설계 블루프린트가 새로 생성되었습니다.', 'success');
    } catch (err) {
      reportError(
        err,
        'AI 기획서 생성에 실패했습니다',
        '서비스 기능명세서 및 화면 설계를 생성하는 중 오류가 발생했습니다.',
        () => regenerateSpec(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function saveSpecVersion() {
    if (!selectedIdeaId.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = '기획서 및 화면 설계 블루프린트 스냅샷을 저장하고 있습니다...';
    try {
      if (!specEditMode.value && editableHighlights.value.length > 0) {
        syncHighlightsToMarkdown();
      }
      currentIdea.value = (await kkumzApi.updateSpec(selectedIdeaId.value, {
        title: formStep1.value.title,
        promptText: formStep1.value.promptText,
        siteType: formStep1.value.siteType,
        category: formStep1.value.category,
        tags: formStep1.value.tags,
        specMarkdown: editableSpecMarkdown.value,
        screens: editableScreens.value,
      })) as StudioEntity;
      syncStep3FromIdea(currentIdea.value);
      showToast('기획서 및 화면 설계 블루프린트 스냅샷이 저장되었습니다.', 'success');
    } catch (err) {
      reportError(
        err,
        '기획서 스냅샷 저장에 실패했습니다',
        '기획서 변경사항을 저장하는 중 오류가 발생했습니다.',
        () => saveSpecVersion(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function rollbackSpecVersion(versionNo: number) {
    if (!selectedIdeaId.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = `v${versionNo} 기획서 버전으로 복원하고 있습니다...`;
    try {
      currentIdea.value = (await kkumzApi.saveOrRollbackIdeaVersion(selectedIdeaId.value, {
        rollbackToVersionNo: versionNo,
      })) as StudioEntity;
      syncStep3FromIdea(currentIdea.value);
      showToast(`기획서 및 화면 블루프린트가 v${versionNo} 버전으로 복원되었습니다.`, 'success');
    } catch (err) {
      reportError(
        err,
        '기획서 버전 복원에 실패했습니다',
        `v${versionNo} 버전으로 되돌리는 중 오류가 발생했습니다.`,
        () => rollbackSpecVersion(versionNo),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function provisionAndGoStep4() {
    const targetIdeaId = selectedIdeaId.value;
    if (!targetIdeaId || busy.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = '기획서·화면설계를 반영하여 라이브 멀티사이트 환경을 구성하고 있습니다...';
    busySteps.value = [
      { label: '1. 브랜드 아이덴티티 · 기획서 블루프린트 저장', status: 'active' },
      { label: '2. 쇼핑몰·홈페이지 자동 생성 및 기본 콘텐츠 채우기', status: 'pending' },
    ];
    try {
      if (editableScreens.value.length > 0 || editableSpecMarkdown.value) {
        currentIdea.value = (await kkumzApi.updateSpec(targetIdeaId, {
          title: formStep1.value.title,
          promptText: formStep1.value.promptText,
          siteType: formStep1.value.siteType,
          category: formStep1.value.category,
          tags: formStep1.value.tags,
          specMarkdown: editableSpecMarkdown.value,
          screens: editableScreens.value,
        })) as StudioEntity;
      }
      if (busySteps.value[0]) busySteps.value[0].status = 'done';
      if (busySteps.value[1]) busySteps.value[1].status = 'active';
      busyMessage.value = '업종 맞춤 초기 콘텐츠와 SDUI 레이아웃 런타임을 생성하고 있습니다...';

      const rt = await executeScaffoldWithRecovery(targetIdeaId);
      if (selectedIdeaId.value !== targetIdeaId) return;
      if (busySteps.value[1]) busySteps.value[1].status = 'done';

      siteRuntime.value = rt;
      syncVisualFormFromRuntime(siteRuntime.value);
      currentIdea.value = (await kkumzApi.getIdeaDetail(targetIdeaId)) as StudioEntity;
      ideas.value = ((await kkumzApi.listIdeas({ mineOnly: true })) || []) as StudioEntity[];
      activeStep.value = 4;
      showToast(
        '나만 볼 수 있는 사이트 초안이 생성되었습니다. 배포 단계에서 라이브 오픈을 실행하면 다른 사용자에게 공개됩니다.',
        'success',
      );
    } catch (err) {
      if (selectedIdeaId.value !== targetIdeaId) return;
      reportError(
        err,
        '라이브 멀티사이트 환경 구성에 실패했습니다',
        '사이트를 생성하고 초기 데이터를 구성하는 중 오류가 발생했습니다.',
        () => provisionAndGoStep4(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function initStudio() {
    try {
      const meta = await kkumzApi.getStudioMetadata();
      layoutLabels.value = meta?.layoutLabels || {};
      componentLabels.value = meta?.componentLabels || {};
      assetTypeLabels.value = meta?.assetTypeLabels || {};
      assetTypes.value = meta?.assetTypes || [];
      siteTypes.value = meta?.siteTypes || [];
      studioPresets.value = (meta?.starterPresets || []) as StudioEntity[];
      featureCatalog.value = (meta?.featureCatalog || []) as StudioEntity[];
      featureCategories.value = meta?.featureCategories || [];
      heroStyles.value = meta?.heroStyles || [];
      themeArchetypes.value = meta?.themeArchetypes || [];
      fontPresets.value = meta?.fontPresets || [];
    } catch {
      layoutLabels.value = {};
      componentLabels.value = {};
      assetTypeLabels.value = {};
      assetTypes.value = [];
      siteTypes.value = [];
      studioPresets.value = [];
      featureCatalog.value = [];
      featureCategories.value = [];
      heroStyles.value = [];
      themeArchetypes.value = [];
      fontPresets.value = [];
    }
    const routeIdeaId = route.params.ideaId ? String(route.params.ideaId) : '';
    if (!isAuthenticated.value && !routeIdeaId) {
      ideas.value = [];
      selectedIdeaId.value = '';
      currentIdea.value = null;
      openAuthModal(
        'login',
        () => initStudio(),
        'AI 브랜드 스튜디오에서 나만의 사이트를 생성·편집하려면 먼저 로그인 또는 회원가입해 주세요.',
      );
      return;
    }
    try {
      ideas.value = isAuthenticated.value ? (((await kkumzApi.listIdeas({ mineOnly: true })) || []) as StudioEntity[]) : [];
      const targetId = routeIdeaId || String(ideas.value[0]?.id ?? '');
      if (targetId) {
        await onSelectIdea(targetId);
      } else {
        selectedIdeaId.value = '';
        currentIdea.value = null;
        siteRuntime.value = null;
        accessDenied.value = false;
        activeStep.value = 1;
      }
      if (String(route.query.fastTrack || '') === '1' && formStep1.value.promptText.trim()) {
        router
          .replace({
            name: 'StudioView',
            params: selectedIdeaId.value ? { ideaId: selectedIdeaId.value } : undefined,
          })
          .catch(() => {});
        await runOneClickAutoComplete();
      }
    } catch (err) {
      reportError(
        err,
        '프로젝트 목록을 불러오지 못했습니다',
        '스튜디오 초기 데이터를 조회하는 중 오류가 발생했습니다.',
        () => initStudio(),
      );
    }
  }

  return {
    runIntentAnalysis,
    saveEnabledFeaturesToServer,
    removeEditableScreen,
    initStudio,
    onSelectIdea,
    performGenerateNames,
    performGenerateLogos,
    runStep2GenerationsParallel,
    ensureStep2Candidates,
    executeScaffoldWithRecovery,
    goToStep,
    saveCurrentIdeaAndGoStep2,
    createNewIdeaFromStep1,
    runOneClickAutoComplete,
    regenerateNames,
    regenerateLogos,
    selectNameCandidate,
    selectLogoCandidate,
    confirmBrandAndNext,
    regenerateSpec,
    saveSpecVersion,
    rollbackSpecVersion,
    provisionAndGoStep4,
  };
}
