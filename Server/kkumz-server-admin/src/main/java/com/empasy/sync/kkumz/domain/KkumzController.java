package com.empasy.sync.kkumz.domain;

import com.empasy.sync.kkumz.common.Result;
import com.empasy.sync.kkumz.domain.KkumzDTO.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Kkumz(꿈즈) AI 브랜드·멀티사이트 생성 스튜디오, 창업 팀빌딩 커뮤니티 및 전용 Admin 백오피스 REST 컨트롤러
 * 단일 Canonical URL Prefix: /api/v1/kkumz
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/kkumz")
@RequiredArgsConstructor
public class KkumzController {

    private final KkumzService kkumzService;

    // =========================================================================
    // 1. 스튜디오 공통 사전 메타데이터 및 아이디어/화면설계 관리
    // =========================================================================

    @GetMapping("/studio/metadata")
    public Result<Map<String, Object>> getStudioMetadata() {
        return Result.OK(kkumzService.getStudioMetadata());
    }

    @GetMapping("/ideas")
    public Result<List<Map<String, Object>>> listIdeas(
            @RequestParam(required = false) String authorUsername,
            @RequestParam(required = false) String siteType,
            @RequestParam(required = false) Boolean mineOnly
    ) {
        return Result.OK(kkumzService.listIdeas(authorUsername, siteType, mineOnly));
    }

    @PostMapping("/ideas")
    public Result<Map<String, Object>> createIdea(@RequestBody CreateIdeaRequest request) {
        log.info("신규 꿈즈 아이디어 생성 요청: title={}, siteType={}", request.getTitle(), request.getSiteType());
        return Result.OK(kkumzService.createIdea(request));
    }

    @GetMapping("/ideas/{ideaId}")
    public Result<Map<String, Object>> getIdeaDetail(@PathVariable String ideaId) {
        return Result.OK(kkumzService.getIdeaDetail(ideaId));
    }

    @PostMapping("/ideas/{ideaId}/versions")
    public Result<Map<String, Object>> saveOrRollbackIdeaVersion(
            @PathVariable String ideaId,
            @RequestBody SaveIdeaVersionRequest request
    ) {
        return Result.OK(kkumzService.saveOrRollbackIdeaVersion(ideaId, request));
    }

    @GetMapping("/ideas/{ideaId}/screens")
    public Result<List<Map<String, Object>>> listIdeaScreens(@PathVariable String ideaId) {
        return Result.OK(kkumzService.listIdeaScreens(ideaId));
    }

    @PostMapping("/ideas/{ideaId}/screens")
    public Result<List<Map<String, Object>>> saveIdeaScreen(
            @PathVariable String ideaId,
            @RequestBody SaveScreenSpecRequest request
    ) {
        return Result.OK(kkumzService.saveIdeaScreen(ideaId, request));
    }

    @DeleteMapping("/ideas/{ideaId}/screens/{screenId}")
    public Result<List<Map<String, Object>>> deleteIdeaScreen(
            @PathVariable String ideaId,
            @PathVariable String screenId
    ) {
        return Result.OK(kkumzService.deleteIdeaScreen(ideaId, screenId));
    }

    // =========================================================================
    // 1-2. 아이디어·사이트 업로드 자산 (현장/상품/배너 사진, 로고, 안내문·요금표 문서) 관리
    // =========================================================================

    @GetMapping("/ideas/{ideaId}/assets")
    public Result<List<Map<String, Object>>> listIdeaAssets(@PathVariable String ideaId) {
        return Result.OK(kkumzService.listIdeaAssets(ideaId));
    }

    @PostMapping(value = "/ideas/{ideaId}/assets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, Object>> uploadIdeaAssets(
            @PathVariable String ideaId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "file", required = false) MultipartFile singleFile,
            @RequestParam(value = "assetType", required = false) String assetType,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "applyToPrompt", required = false) Boolean applyToPrompt,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy
    ) {
        return Result.OK(kkumzService.uploadIdeaAssets(
                ideaId, files, singleFile, assetType, caption, applyToPrompt, uploadedBy
        ));
    }

    @PutMapping("/ideas/{ideaId}/assets/{assetId}")
    public Result<Map<String, Object>> updateIdeaAsset(
            @PathVariable String ideaId,
            @PathVariable String assetId,
            @RequestBody UpdateIdeaAssetRequest request
    ) {
        return Result.OK(kkumzService.updateIdeaAsset(ideaId, assetId, request));
    }

    @DeleteMapping("/ideas/{ideaId}/assets/{assetId}")
    public Result<Map<String, Object>> deleteIdeaAsset(
            @PathVariable String ideaId,
            @PathVariable String assetId
    ) {
        return Result.OK(kkumzService.deleteIdeaAsset(ideaId, assetId));
    }

    @PostMapping("/ideas/{ideaId}/assets/{assetId}/apply-prompt")
    public Result<Map<String, Object>> applyAssetToPrompt(
            @PathVariable String ideaId,
            @PathVariable String assetId
    ) {
        return Result.OK(kkumzService.applyAssetToPrompt(ideaId, assetId));
    }

    @PostMapping("/ideas/{ideaId}/analyze-intent")
    public Result<Map<String, Object>> analyzeIdeaIntent(
            @PathVariable String ideaId,
            @RequestBody(required = false) AnalyzeIntentRequest request
    ) {
        return Result.OK(kkumzService.analyzeIdeaIntent(ideaId, request));
    }

    @PutMapping("/ideas/{ideaId}/features")
    public Result<Map<String, Object>> updateIdeaFeatures(
            @PathVariable String ideaId,
            @RequestBody UpdateIdeaFeaturesRequest request
    ) {
        return Result.OK(kkumzService.updateIdeaFeatures(ideaId, request));
    }

    @PostMapping(value = "/ideas/{ideaId}/upload-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, Object>> uploadCustomBrandLogo(
            @PathVariable String ideaId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "styleName", required = false) String styleName
    ) {
        return Result.OK(kkumzService.uploadCustomBrandLogo(ideaId, file, styleName));
    }

    @GetMapping("/assets/files/{storedName:.+}")
    public ResponseEntity<byte[]> serveStoredAssetFile(
            @PathVariable String storedName,
            @RequestParam(value = "download", required = false, defaultValue = "false") boolean download
    ) {
        StoredAssetPayload payload = kkumzService.loadStoredAssetFile(storedName);
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(payload.getMimeType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        ContentDisposition disposition = (download
                ? ContentDisposition.attachment()
                : ContentDisposition.inline())
                .filename(payload.getOriginalName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .body(payload.getBytes());
    }

    // =========================================================================
    // 2. Step 2 — AI 브랜드 네이밍 8종 & 벡터 SVG 로고 6종 생성 및 선택
    // =========================================================================

    @PostMapping("/ideas/{ideaId}/generate/names")
    public Result<Map<String, Object>> generateNameSuggestions(
            @PathVariable String ideaId,
            @RequestBody(required = false) GenerateNamesRequest request
    ) {
        return Result.OK(kkumzService.generateNameSuggestions(ideaId, request));
    }

    @PostMapping("/ideas/{ideaId}/generate/logos")
    public Result<Map<String, Object>> generateLogoSuggestions(
            @PathVariable String ideaId,
            @RequestBody(required = false) GenerateLogosRequest request
    ) {
        return Result.OK(kkumzService.generateLogoSuggestions(ideaId, request));
    }

    @PutMapping("/ideas/{ideaId}/select-brand")
    public Result<Map<String, Object>> selectBrand(
            @PathVariable String ideaId,
            @RequestBody SelectBrandRequest request
    ) {
        return Result.OK(kkumzService.selectBrand(ideaId, request));
    }

    // =========================================================================
    // 3. Step 3 — AI 마크다운 기획서 & 화면 블루프린트 생성 및 편집
    // =========================================================================

    @PostMapping("/ideas/{ideaId}/generate/spec")
    public Result<Map<String, Object>> generateSpec(
            @PathVariable String ideaId,
            @RequestParam(required = false, defaultValue = "false") boolean forceRegenerate
    ) {
        return Result.OK(kkumzService.generateSpec(ideaId, forceRegenerate));
    }

    @PutMapping("/ideas/{ideaId}/spec")
    public Result<Map<String, Object>> updateSpec(
            @PathVariable String ideaId,
            @RequestBody UpdateSpecRequest request
    ) {
        return Result.OK(kkumzService.updateSpec(ideaId, request));
    }

    // =========================================================================
    // 4. Step 4, 5, 6 — 멀티사이트 스캐폴딩, 대화형 편집, WebIDE, 빌드/배포 & 롤백
    // =========================================================================

    @PostMapping("/ideas/{ideaId}/scaffold-site")
    public Result<Map<String, Object>> scaffoldSite(@PathVariable String ideaId) {
        log.info("꿈즈 멀티사이트 스캐폴딩 실행: ideaId={}", ideaId);
        return Result.OK(kkumzService.scaffoldSite(ideaId));
    }

    @PostMapping("/sites/{siteSlug}/chat-refine")
    public Result<Map<String, Object>> chatRefineSite(
            @PathVariable String siteSlug,
            @RequestBody ChatRefineRequest request
    ) {
        return Result.OK(kkumzService.chatRefineSite(siteSlug, request));
    }

    @GetMapping("/sites/{siteSlug}/webide/files")
    public Result<Map<String, Object>> getWebIdeFiles(@PathVariable String siteSlug) {
        return Result.OK(kkumzService.getWebIdeFiles(siteSlug));
    }

    @PutMapping("/sites/{siteSlug}/webide/files")
    public Result<Map<String, Object>> updateWebIdeFiles(
            @PathVariable String siteSlug,
            @RequestBody UpdateWebIdeFilesRequest request
    ) {
        return Result.OK(kkumzService.updateWebIdeFiles(siteSlug, request));
    }

    @PostMapping("/sites/{siteSlug}/deploy")
    public Result<Map<String, Object>> deploySite(
            @PathVariable String siteSlug,
            @RequestBody(required = false) DeploySiteRequest request
    ) {
        return Result.OK(kkumzService.deploySite(siteSlug, request));
    }

    @PostMapping("/sites/{siteSlug}/unpublish")
    public Result<Map<String, Object>> unpublishSite(@PathVariable String siteSlug) {
        return Result.OK(kkumzService.unpublishSite(siteSlug));
    }

    @PostMapping("/sites/{siteSlug}/ai-fix-build")
    public Result<Map<String, Object>> aiFixBuildError(
            @PathVariable String siteSlug,
            @RequestBody(required = false) FixBuildRequest request
    ) {
        return Result.OK(kkumzService.aiFixBuildError(siteSlug, request));
    }

    @PostMapping("/sites/{siteSlug}/rollback/{versionNo}")
    public Result<Map<String, Object>> rollbackSiteRelease(
            @PathVariable String siteSlug,
            @PathVariable Integer versionNo
    ) {
        return Result.OK(kkumzService.rollbackSiteRelease(siteSlug, versionNo));
    }

    // =========================================================================
    // 4-2. GitHub 리포지토리 프로비저닝, 양방향 Git Push 동기화 & 사용자 전용 배포 키 관리
    // =========================================================================

    @GetMapping("/sites/{siteSlug}/git")
    public Result<Map<String, Object>> getSiteGitRepo(@PathVariable String siteSlug) {
        return Result.OK(kkumzService.getSiteGitRepo(siteSlug));
    }

    @PostMapping("/sites/{siteSlug}/git/provision")
    public Result<Map<String, Object>> provisionSiteGitRepo(
            @PathVariable String siteSlug,
            @RequestBody(required = false) ProvisionGitRepoRequest request
    ) {
        log.info("꿈즈 멀티사이트 Git 리포지토리 및 전용 키 프로비저닝 요청: siteSlug={}", siteSlug);
        return Result.OK(kkumzService.provisionSiteGitRepo(siteSlug, request));
    }

    @PostMapping("/sites/{siteSlug}/git/push")
    public Result<Map<String, Object>> pushSiteToGitRepo(
            @PathVariable String siteSlug,
            @RequestBody(required = false) GitPushSyncRequest request
    ) {
        log.info("꿈즈 멀티사이트 WebIDE -> Git 리포지토리 커밋/푸시 동기화: siteSlug={}", siteSlug);
        return Result.OK(kkumzService.pushSiteToGitRepo(siteSlug, request));
    }

    @PostMapping("/sites/{siteSlug}/git/rotate-keys")
    public Result<Map<String, Object>> rotateSiteGitKeys(
            @PathVariable String siteSlug,
            @RequestBody(required = false) RotateGitKeysRequest request
    ) {
        log.info("꿈즈 멀티사이트 Git 배포 키 및 런타임 API 키 재발급(회전): siteSlug={}", siteSlug);
        return Result.OK(kkumzService.rotateSiteGitKeys(siteSlug, request));
    }

    @GetMapping("/sites/{siteSlug}/git/ssh-key/download")
    public ResponseEntity<byte[]> downloadSiteSshPrivateKey(@PathVariable String siteSlug) {
        StoredAssetPayload payload = kkumzService.downloadSiteSshPrivateKey(siteSlug);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(payload.getOriginalName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate")
                .body(payload.getBytes());
    }

    @GetMapping("/sites/{siteSlug}/git/archive")
    public ResponseEntity<byte[]> downloadSiteGitArchive(@PathVariable String siteSlug) {
        StoredAssetPayload payload = kkumzService.downloadSiteGitArchive(siteSlug);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(payload.getOriginalName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate")
                .body(payload.getBytes());
    }

    // =========================================================================
    // 5. 멀티사이트 통합 라이브 런타임 (/s/:siteSlug) & 실주문/폼제출/AI컨시어지
    // =========================================================================

    @GetMapping("/sites/{siteSlug}/runtime")
    public Result<Map<String, Object>> getSiteRuntime(@PathVariable String siteSlug) {
        return Result.OK(kkumzService.getSiteRuntime(siteSlug));
    }

    @PostMapping("/sites/{siteSlug}/commerce/order")
    public Result<Map<String, Object>> createCommerceOrder(
            @PathVariable String siteSlug,
            @RequestBody CommerceOrderRequest request
    ) {
        log.info("꿈즈 멀티사이트 실주문 접수: siteSlug={}, productId={}", siteSlug, request.getProductId());
        return Result.OK(kkumzService.createCommerceOrder(siteSlug, request));
    }

    @PostMapping("/sites/{siteSlug}/cms/form-submit")
    public Result<Map<String, Object>> submitCmsForm(
            @PathVariable String siteSlug,
            @RequestBody CmsFormSubmitRequest request
    ) {
        log.info("꿈즈 멀티사이트 온라인 폼 접수: siteSlug={}, applicant={}", siteSlug, request.getApplicantName());
        return Result.OK(kkumzService.submitCmsForm(siteSlug, request));
    }

    @PostMapping("/sites/{siteSlug}/concierge")
    public Result<Map<String, Object>> askSiteConcierge(
            @PathVariable String siteSlug,
            @RequestBody(required = false) SiteConciergeRequest request
    ) {
        return Result.OK(kkumzService.askSiteConcierge(siteSlug, request));
    }

    // =========================================================================
    // 6. SNS 커뮤니티 쇼케이스 피드, 좋아요, 댓글, 알림 & 팀빌딩 라운지
    // =========================================================================

    @GetMapping("/feed")
    public Result<List<Map<String, Object>>> getCommunityFeed(
            @RequestParam(required = false) String siteType,
            @RequestParam(required = false) String recruitRole,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String keyword
    ) {
        return Result.OK(kkumzService.getCommunityFeed(siteType, recruitRole, sortBy, keyword));
    }

    @PostMapping("/sites/{siteId}/like")
    public Result<Map<String, Object>> toggleSiteLike(
            @PathVariable String siteId,
            @RequestBody(required = false) Map<String, String> payload
    ) {
        String username = payload != null ? payload.get("username") : null;
        return Result.OK(kkumzService.toggleSiteLike(siteId, username));
    }

    @GetMapping("/sites/{siteId}/comments")
    public Result<List<Map<String, Object>>> listSiteComments(@PathVariable String siteId) {
        return Result.OK(kkumzService.listSiteComments(siteId));
    }

    @PostMapping("/sites/{siteId}/comments")
    public Result<List<Map<String, Object>>> addSiteComment(
            @PathVariable String siteId,
            @RequestBody CreateCommentRequest request
    ) {
        return Result.OK(kkumzService.addSiteComment(siteId, request));
    }

    @GetMapping("/sites/{siteId}/recruits")
    public Result<List<Map<String, Object>>> listSiteRecruits(@PathVariable String siteId) {
        return Result.OK(kkumzService.listSiteRecruits(siteId));
    }

    @PostMapping("/sites/{siteId}/recruits")
    public Result<List<Map<String, Object>>> createSiteRecruit(
            @PathVariable String siteId,
            @RequestBody CreateRecruitRequest request
    ) {
        return Result.OK(kkumzService.createSiteRecruit(siteId, request));
    }

    @GetMapping("/sites/{siteId}/members")
    public Result<List<Map<String, Object>>> listSiteTeamMembers(@PathVariable String siteId) {
        return Result.OK(kkumzService.listSiteTeamMembers(siteId));
    }

    @PostMapping("/sites/{siteId}/team/apply")
    public Result<List<Map<String, Object>>> applyToTeam(
            @PathVariable String siteId,
            @RequestBody TeamApplyRequest request
    ) {
        return Result.OK(kkumzService.applyToTeam(siteId, request));
    }

    @PostMapping("/sites/{siteId}/team/invite")
    public Result<List<Map<String, Object>>> inviteTeamMember(
            @PathVariable String siteId,
            @RequestBody TeamInviteRequest request
    ) {
        return Result.OK(kkumzService.inviteTeamMember(siteId, request));
    }

    @PutMapping("/sites/{siteId}/team/members/{memberId}/status")
    public Result<List<Map<String, Object>>> updateTeamMemberStatus(
            @PathVariable String siteId,
            @PathVariable String memberId,
            @RequestBody UpdateMemberStatusRequest request
    ) {
        return Result.OK(kkumzService.updateTeamMemberStatus(siteId, memberId, request));
    }

    @PostMapping("/sites/{siteId}/report")
    public Result<Map<String, Object>> createModerationReport(
            @PathVariable String siteId,
            @RequestBody CreateReportRequest request
    ) {
        return Result.OK(kkumzService.createModerationReport(siteId, request));
    }

    @GetMapping("/notifications")
    public Result<List<Map<String, Object>>> listNotifications(
            @RequestParam(required = false) String username
    ) {
        return Result.OK(kkumzService.listNotifications(username));
    }

    @PutMapping("/notifications/read-all")
    public Result<String> markAllNotificationsRead(
            @RequestParam(required = false) String username
    ) {
        kkumzService.markAllNotificationsRead(username);
        return Result.OK("모든 알림을 읽음 처리했습니다.");
    }

    // =========================================================================
    // 7. Kkumz 전용 Admin 백오피스 관제 API
    // =========================================================================

    @GetMapping("/admin/dashboard/summary")
    public Result<Map<String, Object>> getAdminDashboardSummary() {
        return Result.OK(kkumzService.getAdminDashboardSummary());
    }

    @GetMapping("/admin/ideas")
    public Result<List<Map<String, Object>>> listAdminIdeas(
            @RequestParam(required = false) String siteType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword
    ) {
        return Result.OK(kkumzService.listAdminIdeas(siteType, status, keyword));
    }

    @DeleteMapping("/admin/ideas/{ideaId}")
    public Result<String> deleteAdminIdea(@PathVariable String ideaId) {
        kkumzService.deleteAdminIdea(ideaId);
        return Result.OK("아이디어가 삭제되었습니다.");
    }

    @GetMapping("/admin/prompts")
    public Result<List<Map<String, Object>>> listAdminPromptTemplates(
            @RequestParam(required = false) String stageType
    ) {
        return Result.OK(kkumzService.listAdminPromptTemplates(stageType));
    }

    @PostMapping("/admin/prompts")
    public Result<Map<String, Object>> createAdminPromptTemplate(@RequestBody SavePromptTemplateRequest request) {
        return Result.OK("등록되었습니다.", kkumzService.saveAdminPromptTemplate(request));
    }

    @PutMapping("/admin/prompts/{promptId}")
    public Result<Map<String, Object>> updateAdminPromptTemplate(
            @PathVariable String promptId,
            @RequestBody SavePromptTemplateRequest request
    ) {
        request.setId(promptId);
        return Result.OK("수정되었습니다.", kkumzService.saveAdminPromptTemplate(request));
    }

    @DeleteMapping("/admin/prompts/{promptId}")
    public Result<String> deleteAdminPromptTemplate(@PathVariable String promptId) {
        kkumzService.deleteAdminPromptTemplate(promptId);
        return Result.OK("삭제되었습니다.", null);
    }

    @GetMapping("/admin/sites")
    public Result<List<Map<String, Object>>> listAdminSites(
            @RequestParam(required = false) String siteType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword
    ) {
        return Result.OK(kkumzService.listAdminSites(siteType, status, keyword));
    }

    @PutMapping("/admin/sites/{siteId}/status")
    public Result<Map<String, Object>> updateAdminSiteStatus(
            @PathVariable String siteId,
            @RequestBody UpdateSiteStatusRequest request
    ) {
        return Result.OK(kkumzService.updateAdminSiteStatus(siteId, request));
    }

    @GetMapping("/admin/builds")
    public Result<List<Map<String, Object>>> listAdminBuilds(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String deployMode
    ) {
        return Result.OK(kkumzService.listAdminBuilds(status, deployMode));
    }

    @GetMapping("/admin/projects")
    public Result<List<Map<String, Object>>> listAdminCommunityProjects(
            @RequestParam(required = false) String siteType,
            @RequestParam(required = false) String keyword
    ) {
        return Result.OK(kkumzService.listAdminCommunityProjects(siteType, keyword));
    }

    @GetMapping("/admin/moderation/reports")
    public Result<List<Map<String, Object>>> listModerationReports(
            @RequestParam(required = false) String status
    ) {
        return Result.OK(kkumzService.listModerationReports(status));
    }

    @PutMapping("/admin/moderation/reports/{reportId}/resolve")
    public Result<List<Map<String, Object>>> resolveModerationReport(
            @PathVariable String reportId,
            @RequestBody ResolveReportRequest request
    ) {
        return Result.OK("심사 처리가 저장되었습니다.", kkumzService.resolveModerationReport(reportId, request));
    }

    // =========================================================================
    // 8. 서비스 간 연동 API 액세스키 (Access Key / Secret Key) 발급·회전·검증·관리
    // =========================================================================

    @GetMapping("/admin/api-keys")
    public Result<List<Map<String, Object>>> listAdminApiKeys(
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String serviceCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword
    ) {
        return Result.OK(kkumzService.listAdminApiKeys(direction, serviceCode, status, keyword));
    }

    @PostMapping("/admin/api-keys")
    public Result<Map<String, Object>> createAdminApiKey(@RequestBody SaveApiAccessKeyRequest request) {
        return Result.OK("등록되었습니다.", kkumzService.saveAdminApiKey(request));
    }

    @PutMapping("/admin/api-keys/{keyId}")
    public Result<Map<String, Object>> updateAdminApiKey(
            @PathVariable String keyId,
            @RequestBody SaveApiAccessKeyRequest request
    ) {
        request.setId(keyId);
        return Result.OK("수정되었습니다.", kkumzService.saveAdminApiKey(request));
    }

    @PostMapping("/admin/api-keys/{keyId}/rotate")
    public Result<Map<String, Object>> rotateAdminApiKey(@PathVariable String keyId) {
        return Result.OK(kkumzService.rotateAdminApiKey(keyId));
    }

    @PutMapping("/admin/api-keys/{keyId}/status")
    public Result<Map<String, Object>> updateAdminApiKeyStatus(
            @PathVariable String keyId,
            @RequestBody Map<String, Object> body
    ) {
        String status = body != null && body.get("status") != null ? String.valueOf(body.get("status")) : "ACTIVE";
        return Result.OK(kkumzService.updateAdminApiKeyStatus(keyId, status));
    }

    @PostMapping("/admin/api-keys/{keyId}/verify")
    public Result<Map<String, Object>> verifyAdminApiKey(@PathVariable String keyId) {
        return Result.OK(kkumzService.verifyAdminApiKey(keyId));
    }

    @DeleteMapping("/admin/api-keys/{keyId}")
    public Result<String> deleteAdminApiKey(@PathVariable String keyId) {
        kkumzService.deleteAdminApiKey(keyId);
        return Result.OK("API 액세스 키가 삭제되었습니다.");
    }
}
