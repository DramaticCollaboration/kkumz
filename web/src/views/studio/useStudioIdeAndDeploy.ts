import { kkumzApi } from '@/api/kkumzApi';
import type { StudioEntity, StudioState } from './useStudioState';

export function useStudioIdeAndDeploy(state: StudioState) {
  const {
    activeSiteSlug,
    activeWebIdeFile,
    activeFileIndex,
    busy,
    busyMessage,
    currentIdea,
    deployForm,
    errorBanner,
    gitForm,
    ideas,
    liveOpenCelebration,
    reportError,
    selectedIdeaId,
    showGitConfigForm,
    showToast,
    siteRuntime,
    syncVisualFormFromRuntime,
    webIdeCommitMsg,
    webIdeData,
    webIdeFiles,
  } = state;

  async function loadWebIde(slug: string) {
    const data = (await kkumzApi.getWebIdeFiles(slug)) as StudioEntity;
    webIdeData.value = data;
    webIdeFiles.value = (data.files || []).map((f) => ({ ...f }));
    activeFileIndex.value = 0;
    if (data.gitRepo) {
      gitForm.value.repoOwner = data.gitRepo.repoOwner || 'kkumz-sites';
      gitForm.value.repoName = data.gitRepo.repoName || slug;
      gitForm.value.defaultBranch = data.gitRepo.defaultBranch || 'main';
      gitForm.value.visibility = data.gitRepo.visibility || 'PRIVATE';
    } else if (!gitForm.value.repoName) {
      gitForm.value.repoName = slug;
    }
  }

  async function provisionGitRepository() {
    if (!activeSiteSlug.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = 'Git 리포지토리 스캐폴딩 및 사용자 전용 배포 키를 생성하고 있습니다...';
    try {
      const res = (await kkumzApi.provisionSiteGitRepo(activeSiteSlug.value, {
        repoOwner: gitForm.value.repoOwner.trim() || 'kkumz-sites',
        repoName: gitForm.value.repoName.trim() || activeSiteSlug.value,
        defaultBranch: gitForm.value.defaultBranch.trim() || 'main',
        visibility: gitForm.value.visibility,
        githubPatToken: gitForm.value.githubPatToken.trim() || undefined,
        includeFullViteScaffold: true,
      })) as StudioEntity;
      webIdeData.value = res;
      webIdeFiles.value = (res.files || []).map((f) => ({ ...f }));
      showGitConfigForm.value = false;
      showToast(
        res.notice || 'Git 리포지토리 생성 및 전용 개발 키 발급이 완료되었습니다.',
        'success',
      );
    } catch (err) {
      reportError(
        err,
        'Git 저장소 및 전용 키 구성에 실패했습니다',
        '저장소 설정을 확인한 뒤 다시 시도해 주세요.',
        () => provisionGitRepository(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function pushWebIdeToGit() {
    if (!activeSiteSlug.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = '현재 WebIDE 코드를 검증하고 Git 리포지토리에 커밋·푸시하고 있습니다...';
    try {
      const res = (await kkumzApi.pushSiteToGitRepo(activeSiteSlug.value, {
        files: webIdeFiles.value,
        commitMessage:
          webIdeCommitMsg.value.trim() ||
          `feat(studio): update ${activeSiteSlug.value} components & layout from WebIDE`,
        githubPatToken: gitForm.value.githubPatToken.trim() || undefined,
      })) as StudioEntity;
      webIdeData.value = res;
      webIdeFiles.value = (res.files || []).map((f) => ({ ...f }));
      webIdeCommitMsg.value = '';
      showToast(res.notice || 'WebIDE 변경 사항이 Git 리포지토리 최신 커밋으로 푸시되었습니다.', 'success');
    } catch (err) {
      reportError(
        err,
        'Git 커밋 및 푸시 동기화에 실패했습니다',
        'WebIDE 코드 구문 또는 저장소 상태를 확인해 주세요.',
        () => pushWebIdeToGit(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function rotateGitDeveloperKeys() {
    if (!activeSiteSlug.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = '기존 키를 폐기하고 새로운 SSH Deploy Key 및 Site API Key를 재발급하고 있습니다...';
    try {
      const res = (await kkumzApi.rotateSiteGitKeys(activeSiteSlug.value, {
        rotateSshKey: true,
        rotateSiteApiKey: true,
        githubPatToken: gitForm.value.githubPatToken.trim() || undefined,
      })) as StudioEntity;
      webIdeData.value = res;
      webIdeFiles.value = (res.files || []).map((f) => ({ ...f }));
      showToast(
        res.notice || '사용자 전용 SSH Deploy Key 및 사이트 API Secret Key가 새로 발급되었습니다.',
        'success',
      );
    } catch (err) {
      reportError(
        err,
        '전용 개발 키 재발급에 실패했습니다',
        '잠시 후 다시 시도해 주세요.',
        () => rotateGitDeveloperKeys(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function copyTextToClipboard(text: string | undefined, label: string) {
    if (!text) return;
    try {
      await navigator.clipboard.writeText(text);
      showToast(`${label}가 클립보드에 복사되었습니다.`, 'success');
    } catch {
      showToast(`${label} 복사에 실패했습니다. 텍스트를 직접 선택해 복사해 주세요.`, 'error');
    }
  }

  function addNewWebIdeFile() {
    const nextIdx = webIdeFiles.value.length + 1;
    const openScript = '<' + 'script setup lang="ts">';
    const closeScript = '<' + '/script>';
    webIdeFiles.value.push({
      path: `src/components/CustomSection${nextIdx}.vue`,
      language: 'vue',
      content: `<template>\n  <section class="custom-section">\n    <h3>커스텀 섹션 ${nextIdx}</h3>\n  </section>\n</template>\n\n${openScript}\n${closeScript}\n`,
    });
    activeFileIndex.value = webIdeFiles.value.length - 1;
  }

  function removeWebIdeFile(idx: number) {
    if (webIdeFiles.value.length <= 1) return;
    webIdeFiles.value.splice(idx, 1);
    if (activeFileIndex.value >= webIdeFiles.value.length) {
      activeFileIndex.value = Math.max(0, webIdeFiles.value.length - 1);
    }
  }

  function formatActiveJsonFile() {
    const file = activeWebIdeFile.value;
    if (!file) return;
    try {
      const parsed = JSON.parse(file.content);
      file.content = JSON.stringify(parsed, null, 2);
      showToast('JSON 포맷이 깔끔하게 정렬되었습니다.', 'success');
    } catch {
      showToast('JSON 구문 오류가 있어 자동 정렬할 수 없습니다. 구문을 확인해 주세요.', 'error');
    }
  }

  async function syncStep4ToWebIde() {
    if (!activeSiteSlug.value) return;
    busy.value = true;
    busyMessage.value = '4단계 비주얼·상품 설정을 WebIDE 소스 파일에 동기화하고 있습니다...';
    try {
      await loadWebIde(activeSiteSlug.value);
      const themeIdx = webIdeFiles.value.findIndex((f) => f.path.endsWith('theme.json'));
      if (themeIdx >= 0 && siteRuntime.value) {
        webIdeFiles.value[themeIdx].content = JSON.stringify(
          {
            siteName: siteRuntime.value.siteName,
            siteSlug: siteRuntime.value.siteSlug,
            designTokens: siteRuntime.value.designTokens,
            layoutSchema: siteRuntime.value.layoutSchema,
          },
          null,
          2,
        );
        activeFileIndex.value = themeIdx;
      }
      showToast('4단계 최신 비주얼·테마 설정이 WebIDE 코드로 동기화되었습니다.', 'success');
    } catch (err) {
      reportError(err, 'WebIDE 동기화에 실패했습니다', '파일을 다시 불러오는 중 오류가 발생했습니다.');
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function saveAndBuildWebIde() {
    if (!activeSiteSlug.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = '코드를 저장하고 번들 빌드를 검증하고 있습니다...';
    try {
      const res = (await kkumzApi.updateWebIdeFiles(activeSiteSlug.value, {
        files: webIdeFiles.value,
        commitMessage: webIdeCommitMsg.value.trim() || undefined,
      })) as StudioEntity;
      webIdeData.value = res;
      webIdeFiles.value = (res.files || []).map((f) => ({ ...f }));
      webIdeCommitMsg.value = '';
      showToast(
        res.buildStatus === 'FAILED'
          ? '빌드 검증 중 구문 오류가 감지되었습니다. 우측 상단 AI 빌드 오류 자동 패치 실행 버튼으로 복구할 수 있습니다.'
          : '번들 빌드가 정상 완료되었습니다.',
        res.buildStatus === 'FAILED' ? 'error' : 'success',
      );
    } catch (err) {
      reportError(
        err,
        '코드 저장 및 번들 빌드에 실패했습니다',
        'WebIDE 파일 저장 및 빌드 요청 중 오류가 발생했습니다.',
        () => saveAndBuildWebIde(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function runAiFixBuild() {
    if (!activeSiteSlug.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = 'AI가 빌드 오류를 분석하고 자동 패치하고 있습니다...';
    try {
      const res = (await kkumzApi.aiFixBuildError(activeSiteSlug.value)) as StudioEntity;
      webIdeData.value = res;
      webIdeFiles.value = (res.files || []).map((f) => ({ ...f }));
      showToast('AI가 템플릿 구문 오류를 수정하고 빌드를 완료했습니다.', 'success');
    } catch (err) {
      reportError(
        err,
        'AI 빌드 오류 자동 패치에 실패했습니다',
        '구문 오류 자동 복구 요청 중 오류가 발생했습니다.',
        () => runAiFixBuild(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function executeDeploy() {
    if (!siteRuntime.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = '새 릴리즈 버전을 빌드하고 라이브 사이트에 오픈 반영하고 있습니다...';
    try {
      siteRuntime.value = (await kkumzApi.deploySite(siteRuntime.value.siteSlug || '', {
        deployMode: deployForm.value.deployMode,
        releaseNote: deployForm.value.releaseNote,
        visibility: deployForm.value.visibility || 'PUBLIC',
        customDomain: deployForm.value.customDomain.trim() || undefined,
      })) as StudioEntity;
      syncVisualFormFromRuntime(siteRuntime.value);
      if (selectedIdeaId.value) {
        currentIdea.value = (await kkumzApi.getIdeaDetail(selectedIdeaId.value)) as StudioEntity;
        ideas.value = ((await kkumzApi.listIdeas({ mineOnly: true })) || []) as StudioEntity[];
      }
      deployForm.value.releaseNote = '';
      showToast(
        siteRuntime.value.isPublished
          ? `v${siteRuntime.value.publishedVersion || siteRuntime.value.activeVersion} 라이브 사이트 배포가 완료되어 전체 공개되었습니다.`
          : `v${siteRuntime.value.activeVersion} 버전이 저장되었으며 비공개 상태로 설정되었습니다.`,
        'success',
      );
    } catch (err) {
      reportError(
        err,
        '라이브 배포 실행에 실패했습니다',
        '새 릴리즈 버전을 배포하는 중 오류가 발생했습니다.',
        () => executeDeploy(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function executeUnpublish() {
    if (!siteRuntime.value || busy.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = '사이트를 비공개 초안 상태로 전환하고 있습니다...';
    try {
      siteRuntime.value = (await kkumzApi.unpublishSite(siteRuntime.value.siteSlug || '')) as StudioEntity;
      syncVisualFormFromRuntime(siteRuntime.value);
      liveOpenCelebration.value = false;
      if (selectedIdeaId.value) {
        currentIdea.value = (await kkumzApi.getIdeaDetail(selectedIdeaId.value)) as StudioEntity;
        ideas.value = ((await kkumzApi.listIdeas({ mineOnly: true })) || []) as StudioEntity[];
      }
      showToast('사이트가 비공개 초안 상태로 전환되어 외부 노출이 즉시 중단되었습니다.', 'info');
    } catch (err) {
      reportError(
        err,
        '비공개 전환에 실패했습니다',
        '사이트 공개 상태를 변경하는 중 오류가 발생했습니다.',
        () => executeUnpublish(),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  async function executeQuickLiveOpen() {
    if (!siteRuntime.value || busy.value) return;
    deployForm.value.deployMode = 'DYNAMIC_SDUI';
    if (!deployForm.value.releaseNote.trim()) {
      deployForm.value.releaseNote = '1초 즉시 오픈 · 최신 문구·사진·상품 반영';
    }
    await executeDeploy();
    if (!errorBanner.value) {
      liveOpenCelebration.value = true;
    }
  }

  async function copyLiveSiteUrl() {
    if (!siteRuntime.value?.siteSlug) return;
    const origin = typeof window !== 'undefined' ? window.location.origin : '';
    const fullUrl = `${origin}/s/${siteRuntime.value.siteSlug}`;
    await copyTextToClipboard(fullUrl, '내 사이트 인터넷 주소');
  }

  async function rollbackRelease(versionNo: number) {
    if (!siteRuntime.value) return;
    errorBanner.value = null;
    busy.value = true;
    busyMessage.value = `라이브 사이트를 v${versionNo} 릴리즈로 롤백하고 있습니다...`;
    try {
      siteRuntime.value = (await kkumzApi.rollbackSiteRelease(
        siteRuntime.value.siteSlug || '',
        versionNo,
      )) as StudioEntity;
      syncVisualFormFromRuntime(siteRuntime.value);
      showToast(`라이브 사이트가 v${versionNo} 릴리즈로 즉시 롤백되었습니다.`, 'success');
    } catch (err) {
      reportError(
        err,
        '릴리즈 롤백에 실패했습니다',
        `v${versionNo} 버전으로 롤백하는 중 오류가 발생했습니다.`,
        () => rollbackRelease(versionNo),
      );
    } finally {
      busy.value = false;
      busyMessage.value = '';
    }
  }

  return {
    loadWebIde,
    provisionGitRepository,
    pushWebIdeToGit,
    rotateGitDeveloperKeys,
    copyTextToClipboard,
    addNewWebIdeFile,
    removeWebIdeFile,
    formatActiveJsonFile,
    syncStep4ToWebIde,
    saveAndBuildWebIde,
    runAiFixBuild,
    executeDeploy,
    executeUnpublish,
    executeQuickLiveOpen,
    copyLiveSiteUrl,
    rollbackRelease,
  };
}
