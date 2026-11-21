package com.empasy.sync.kkumz.domain;

import com.empasy.sync.core.common.config.SystemConfigHolder;
import com.empasy.sync.kkumz.domain.KkumzDTO.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Kkumz(꿈즈) 핵심 도메인 비즈니스 퍼사드 서비스
 * - AI 브랜드·멀티사이트 빌더 (AgentScope Java + DB 프롬프트 템플릿 기반)
 * - SyncShop / SyncCMS 멀티사이트 실데이터 연동 (실데이터 연동 및 명시적 예외 전파)
 * - 창업 쇼케이스 피드, 팀원 모집/지원, 실시간 댓글, Git 연동 및 Admin 백오피스
 * 각 도메인 책임은 동일 패키지의 전담 협력 컴포넌트에 위임합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KkumzService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<KkumzAiGatewayService> aiGatewayProvider;

    private KkumzAiGatewayService aiGatewayService;

    @Setter(onMethod_ = @org.springframework.beans.factory.annotation.Autowired(required = false))
    private SyncServiceApiClient syncServiceApiClient;

    @Value("${kkumz.cross-db.shop-url:}")
    private String shopDbUrl;

    @Value("${kkumz.cross-db.cms-url:}")
    private String cmsDbUrl;

    @Value("${kkumz.cross-db.username:}")
    private String crossDbUsername;

    @Value("${kkumz.cross-db.password:}")
    private String crossDbPassword;

    @Value("${kkumz.storage.upload-dir:uploads/kkumz}")
    private String storageUploadDir;

    @Value("${kkumz.storage.public-prefix:/api/v1/kkumz/assets/files}")
    private String storagePublicPrefix;

    @Value("${kkumz.integration.cms-base-url:}")
    private String defaultCmsBaseUrl;

    @Value("${kkumz.integration.shop-base-url:}")
    private String defaultShopBaseUrl;

    @Value("${kkumz.server.public-base-url:}")
    private String kkumzPublicBaseUrl;

    private JdbcTemplate shopJdbcTemplate;
    private JdbcTemplate cmsJdbcTemplate;

    private final ReentrantLock collaboratorLock = new ReentrantLock();
    private volatile KkumzAccessAndMetadataSupport accessMetaSupport;
    private volatile KkumzIdeaAndSpecSupport ideaSpecSupport;
    private volatile KkumzAssetSupport assetSupport;
    private volatile KkumzBrandKitSupport brandKitSupport;
    private volatile KkumzSiteBuildAndDeploySupport buildDeploySupport;
    private volatile KkumzGitIntegrationSupport gitSupport;
    private volatile KkumzStorefrontRuntimeSupport runtimeSupport;
    private volatile KkumzCommunityAndAdminSupport communityAdminSupport;

    @PostConstruct
    public void initCrossDomainJdbcTemplates() {
        if (shopDbUrl != null && !shopDbUrl.isBlank() && crossDbUsername != null && !crossDbUsername.isBlank()) {
            DriverManagerDataSource shopDs = new DriverManagerDataSource(shopDbUrl, crossDbUsername, crossDbPassword);
            shopDs.setDriverClassName("org.postgresql.Driver");
            this.shopJdbcTemplate = new JdbcTemplate(shopDs);
        }
        if (cmsDbUrl != null && !cmsDbUrl.isBlank() && crossDbUsername != null && !crossDbUsername.isBlank()) {
            DriverManagerDataSource cmsDs = new DriverManagerDataSource(cmsDbUrl, crossDbUsername, crossDbPassword);
            cmsDs.setDriverClassName("org.postgresql.Driver");
            this.cmsJdbcTemplate = new JdbcTemplate(cmsDs);
        }
        if (this.aiGatewayService == null) {
            KkumzAiGatewayService provided = this.aiGatewayProvider != null ? this.aiGatewayProvider.getIfAvailable() : null;
            this.aiGatewayService = provided != null ? provided : new KkumzAiGatewayService(this.jdbcTemplate, this.objectMapper, null);
        }
        try {
            Files.createDirectories(assets().resolveUploadDir());
        } catch (IOException e) {
            log.warn("업로드 디렉터리 초기화 경고: {}", e.getMessage());
        }
    }

    JdbcTemplate jdbcTemplate() {
        return this.jdbcTemplate;
    }

    ObjectMapper objectMapper() {
        return this.objectMapper;
    }

    JdbcTemplate shopJdbcTemplate() {
        return this.shopJdbcTemplate;
    }

    JdbcTemplate cmsJdbcTemplate() {
        return this.cmsJdbcTemplate;
    }

    SyncServiceApiClient syncServiceApiClient() {
        return this.syncServiceApiClient;
    }

    String storageUploadDir() {
        return this.storageUploadDir;
    }

    String storagePublicPrefix() {
        return this.storagePublicPrefix;
    }

    String resolveCmsBaseUrl() {
        return SystemConfigHolder.getValue("kkumz.integration.cms.base-url", defaultCmsBaseUrl != null ? defaultCmsBaseUrl : "");
    }

    String resolveShopBaseUrl() {
        return SystemConfigHolder.getValue("kkumz.integration.shop.base-url", defaultShopBaseUrl != null ? defaultShopBaseUrl : "");
    }

    String resolveKkumzPublicBaseUrl() {
        return SystemConfigHolder.getValue("kkumz.server.public-base-url", kkumzPublicBaseUrl != null ? kkumzPublicBaseUrl : "");
    }

    KkumzAiGatewayService ensureAiGateway() {
        if (this.aiGatewayService == null) {
            KkumzAiGatewayService provided = this.aiGatewayProvider != null ? this.aiGatewayProvider.getIfAvailable() : null;
            this.aiGatewayService = provided != null ? provided : new KkumzAiGatewayService(this.jdbcTemplate, this.objectMapper, null);
        }
        return this.aiGatewayService;
    }

    KkumzAccessAndMetadataSupport accessMeta() {
        KkumzAccessAndMetadataSupport local = this.accessMetaSupport;
        if (local == null) {
            collaboratorLock.lock();
            try {
                local = this.accessMetaSupport;
                if (local == null) {
                    local = new KkumzAccessAndMetadataSupport(this);
                    this.accessMetaSupport = local;
                }
            } finally {
                collaboratorLock.unlock();
            }
        }
        return local;
    }

    KkumzIdeaAndSpecSupport ideaSpec() {
        KkumzIdeaAndSpecSupport local = this.ideaSpecSupport;
        if (local == null) {
            collaboratorLock.lock();
            try {
                local = this.ideaSpecSupport;
                if (local == null) {
                    local = new KkumzIdeaAndSpecSupport(this);
                    this.ideaSpecSupport = local;
                }
            } finally {
                collaboratorLock.unlock();
            }
        }
        return local;
    }

    KkumzAssetSupport assets() {
        KkumzAssetSupport local = this.assetSupport;
        if (local == null) {
            collaboratorLock.lock();
            try {
                local = this.assetSupport;
                if (local == null) {
                    local = new KkumzAssetSupport(this);
                    this.assetSupport = local;
                }
            } finally {
                collaboratorLock.unlock();
            }
        }
        return local;
    }

    KkumzBrandKitSupport brandKit() {
        KkumzBrandKitSupport local = this.brandKitSupport;
        if (local == null) {
            collaboratorLock.lock();
            try {
                local = this.brandKitSupport;
                if (local == null) {
                    local = new KkumzBrandKitSupport(this);
                    this.brandKitSupport = local;
                }
            } finally {
                collaboratorLock.unlock();
            }
        }
        return local;
    }

    KkumzSiteBuildAndDeploySupport buildDeploy() {
        KkumzSiteBuildAndDeploySupport local = this.buildDeploySupport;
        if (local == null) {
            collaboratorLock.lock();
            try {
                local = this.buildDeploySupport;
                if (local == null) {
                    local = new KkumzSiteBuildAndDeploySupport(this);
                    this.buildDeploySupport = local;
                }
            } finally {
                collaboratorLock.unlock();
            }
        }
        return local;
    }

    KkumzGitIntegrationSupport git() {
        KkumzGitIntegrationSupport local = this.gitSupport;
        if (local == null) {
            collaboratorLock.lock();
            try {
                local = this.gitSupport;
                if (local == null) {
                    local = new KkumzGitIntegrationSupport(this);
                    this.gitSupport = local;
                }
            } finally {
                collaboratorLock.unlock();
            }
        }
        return local;
    }

    KkumzStorefrontRuntimeSupport runtime() {
        KkumzStorefrontRuntimeSupport local = this.runtimeSupport;
        if (local == null) {
            collaboratorLock.lock();
            try {
                local = this.runtimeSupport;
                if (local == null) {
                    local = new KkumzStorefrontRuntimeSupport(this);
                    this.runtimeSupport = local;
                }
            } finally {
                collaboratorLock.unlock();
            }
        }
        return local;
    }

    KkumzCommunityAndAdminSupport communityAdmin() {
        KkumzCommunityAndAdminSupport local = this.communityAdminSupport;
        if (local == null) {
            collaboratorLock.lock();
            try {
                local = this.communityAdminSupport;
                if (local == null) {
                    local = new KkumzCommunityAndAdminSupport(this);
                    this.communityAdminSupport = local;
                }
            } finally {
                collaboratorLock.unlock();
            }
        }
        return local;
    }

    private List<Map<String, Object>> parseAiJsonArray(String rawJson, String stageName) {
        return KkumzJsonAndTextUtils.parseAiJsonArray(rawJson, stageName);
    }

    public List<Map<String, Object>> listFeatureCatalog() {
        return accessMeta().listFeatureCatalog();
    }

    public Map<String, Object> getStudioMetadata() {
        return accessMeta().getStudioMetadata();
    }

    public String resolveCurrentUsernameFromRequest() {
        return accessMeta().resolveCurrentUsernameFromRequest();
    }

    public boolean isAdminUser(String username) {
        return accessMeta().isAdminUser(username);
    }

    public boolean canEditIdea(Map<String, Object> ideaRow, String currentUsername) {
        return accessMeta().canEditIdea(ideaRow, currentUsername);
    }

    public boolean canEditSite(Map<String, Object> siteRow, String currentUsername) {
        return accessMeta().canEditSite(siteRow, currentUsername);
    }

    public void assertIdeaEditable(String ideaId) {
        accessMeta().assertIdeaEditable(ideaId);
    }

    public void assertSiteEditable(String siteIdOrSlug) {
        accessMeta().assertSiteEditable(siteIdOrSlug);
    }

    public boolean canViewIdea(Map<String, Object> ideaRow, String currentUsername) {
        return accessMeta().canViewIdea(ideaRow, currentUsername);
    }

    public void assertIdeaReadable(Map<String, Object> ideaRow) {
        accessMeta().assertIdeaReadable(ideaRow);
    }

    public void assertIdeaReadable(String ideaId) {
        accessMeta().assertIdeaReadable(ideaId);
    }

    public Integer resolvePublishedVersion(Map<String, Object> siteRow) {
        return accessMeta().resolvePublishedVersion(siteRow);
    }

    public boolean isSitePubliclyPublished(Map<String, Object> siteRow) {
        return accessMeta().isSitePubliclyPublished(siteRow);
    }

    public boolean canViewSite(Map<String, Object> siteRow, String currentUsername) {
        return accessMeta().canViewSite(siteRow, currentUsername);
    }

    public void assertSiteReadable(Map<String, Object> siteRow) {
        accessMeta().assertSiteReadable(siteRow);
    }

    public void assertSiteReadable(String siteIdOrSlug) {
        accessMeta().assertSiteReadable(siteIdOrSlug);
    }

    public List<Map<String, Object>> listIdeas(String authorUsername, String siteType) {
        return ideaSpec().listIdeas(authorUsername, siteType);
    }

    public List<Map<String, Object>> listIdeas(String authorUsername, String siteType, Boolean mineOnly) {
        return ideaSpec().listIdeas(authorUsername, siteType, mineOnly);
    }

    @Transactional
    public Map<String, Object> createIdea(CreateIdeaRequest req) {
        return ideaSpec().createIdea(req);
    }

    public Map<String, Object> getIdeaDetail(String ideaId) {
        return ideaSpec().getIdeaDetail(ideaId);
    }

    @Transactional
    public Map<String, Object> saveOrRollbackIdeaVersion(String ideaId, SaveIdeaVersionRequest req) {
        return ideaSpec().saveOrRollbackIdeaVersion(ideaId, req);
    }

    public Map<String, Object> analyzeIdeaIntent(String ideaId, AnalyzeIntentRequest req) {
        return ideaSpec().analyzeIdeaIntent(ideaId, req);
    }

    @Transactional
    public Map<String, Object> updateIdeaFeatures(String ideaId, UpdateIdeaFeaturesRequest req) {
        return ideaSpec().updateIdeaFeatures(ideaId, req);
    }

    public List<Map<String, Object>> listIdeaScreens(String ideaId) {
        return ideaSpec().listIdeaScreens(ideaId);
    }

    @Transactional
    public List<Map<String, Object>> saveIdeaScreen(String ideaId, SaveScreenSpecRequest req) {
        return ideaSpec().saveIdeaScreen(ideaId, req);
    }

    @Transactional
    public List<Map<String, Object>> deleteIdeaScreen(String ideaId, String screenId) {
        return ideaSpec().deleteIdeaScreen(ideaId, screenId);
    }

    public List<Map<String, Object>> listIdeaAssets(String ideaId) {
        return assets().listIdeaAssets(ideaId);
    }

    @Transactional
    public Map<String, Object> uploadIdeaAssets(
            String ideaId,
            List<MultipartFile> files,
            MultipartFile singleFile,
            String assetType,
            String caption,
            Boolean applyToPrompt,
            String uploadedBy
    ) {
        return assets().uploadIdeaAssets(ideaId, files, singleFile, assetType, caption, applyToPrompt, uploadedBy);
    }

    @Transactional
    public Map<String, Object> updateIdeaAsset(String ideaId, String assetId, UpdateIdeaAssetRequest req) {
        return assets().updateIdeaAsset(ideaId, assetId, req);
    }

    @Transactional
    public Map<String, Object> deleteIdeaAsset(String ideaId, String assetId) {
        return assets().deleteIdeaAsset(ideaId, assetId);
    }

    @Transactional
    public Map<String, Object> applyAssetToPrompt(String ideaId, String assetId) {
        return assets().applyAssetToPrompt(ideaId, assetId);
    }

    @Transactional
    public Map<String, Object> uploadCustomBrandLogo(String ideaId, MultipartFile file, String styleName) {
        return assets().uploadCustomBrandLogo(ideaId, file, styleName);
    }

    public StoredAssetPayload loadStoredAssetFile(String storedName) {
        return assets().loadStoredAssetFile(storedName);
    }

    public Map<String, Object> generateNameSuggestions(String ideaId, GenerateNamesRequest req) {
        return brandKit().generateNameSuggestions(ideaId, req);
    }

    public Map<String, Object> generateLogoSuggestions(String ideaId, GenerateLogosRequest req) {
        return brandKit().generateLogoSuggestions(ideaId, req);
    }

    @Transactional
    public Map<String, Object> selectBrand(String ideaId, SelectBrandRequest req) {
        return brandKit().selectBrand(ideaId, req);
    }

    public Map<String, Object> generateSpec(String ideaId) {
        return ideaSpec().generateSpec(ideaId);
    }

    public Map<String, Object> generateSpec(String ideaId, boolean forceRegenerate) {
        return ideaSpec().generateSpec(ideaId, forceRegenerate);
    }

    @Transactional
    public Map<String, Object> updateSpec(String ideaId, UpdateSpecRequest req) {
        return ideaSpec().updateSpec(ideaId, req);
    }

    public Map<String, Object> scaffoldSite(String ideaId) {
        return buildDeploy().scaffoldSite(ideaId);
    }

    public Map<String, Object> chatRefineSite(String siteIdOrSlug, ChatRefineRequest req) {
        return buildDeploy().chatRefineSite(siteIdOrSlug, req);
    }

    public Map<String, Object> getWebIdeFiles(String siteIdOrSlug) {
        return buildDeploy().getWebIdeFiles(siteIdOrSlug);
    }

    @Transactional
    public Map<String, Object> updateWebIdeFiles(String siteIdOrSlug, UpdateWebIdeFilesRequest req) {
        return buildDeploy().updateWebIdeFiles(siteIdOrSlug, req);
    }

    public Map<String, Object> aiFixBuildError(String siteIdOrSlug, FixBuildRequest req) {
        return buildDeploy().aiFixBuildError(siteIdOrSlug, req);
    }

    @Transactional
    public Map<String, Object> rollbackSiteRelease(String siteIdOrSlug, Integer versionNo) {
        return buildDeploy().rollbackSiteRelease(siteIdOrSlug, versionNo);
    }

    @Transactional
    public Map<String, Object> deploySite(String siteIdOrSlug, DeploySiteRequest req) {
        return buildDeploy().deploySite(siteIdOrSlug, req);
    }

    @Transactional
    public Map<String, Object> unpublishSite(String siteIdOrSlug) {
        return buildDeploy().unpublishSite(siteIdOrSlug);
    }

    public Map<String, Object> getSiteGitRepo(String siteIdOrSlug) {
        return git().getSiteGitRepo(siteIdOrSlug);
    }

    @Transactional
    public Map<String, Object> provisionSiteGitRepo(String siteIdOrSlug, ProvisionGitRepoRequest req) {
        return git().provisionSiteGitRepo(siteIdOrSlug, req);
    }

    @Transactional
    public Map<String, Object> pushSiteToGitRepo(String siteIdOrSlug, GitPushSyncRequest req) {
        return git().pushSiteToGitRepo(siteIdOrSlug, req);
    }

    @Transactional
    public Map<String, Object> rotateSiteGitKeys(String siteIdOrSlug, RotateGitKeysRequest req) {
        return git().rotateSiteGitKeys(siteIdOrSlug, req);
    }

    public StoredAssetPayload downloadSiteSshPrivateKey(String siteIdOrSlug) {
        return git().downloadSiteSshPrivateKey(siteIdOrSlug);
    }

    public StoredAssetPayload downloadSiteGitArchive(String siteIdOrSlug) {
        return git().downloadSiteGitArchive(siteIdOrSlug);
    }

    @Transactional
    public Map<String, Object> getSiteRuntime(String siteSlug) {
        return runtime().getSiteRuntime(siteSlug);
    }

    @Transactional
    public Map<String, Object> createCommerceOrder(String siteSlug, CommerceOrderRequest req) {
        return runtime().createCommerceOrder(siteSlug, req);
    }

    @Transactional
    public Map<String, Object> submitCmsForm(String siteSlug, CmsFormSubmitRequest req) {
        return runtime().submitCmsForm(siteSlug, req);
    }

    public Map<String, Object> askSiteConcierge(String siteSlug, SiteConciergeRequest req) {
        return runtime().askSiteConcierge(siteSlug, req);
    }

    public List<Map<String, Object>> getCommunityFeed(String siteType, String recruitRole, String sortBy, String keyword) {
        return communityAdmin().getCommunityFeed(siteType, recruitRole, sortBy, keyword);
    }

    @Transactional
    public Map<String, Object> toggleSiteLike(String siteIdOrSlug, String username) {
        return communityAdmin().toggleSiteLike(siteIdOrSlug, username);
    }

    public List<Map<String, Object>> listSiteComments(String siteIdOrSlug) {
        return communityAdmin().listSiteComments(siteIdOrSlug);
    }

    @Transactional
    public List<Map<String, Object>> addSiteComment(String siteIdOrSlug, CreateCommentRequest req) {
        return communityAdmin().addSiteComment(siteIdOrSlug, req);
    }

    public List<Map<String, Object>> listSiteRecruits(String siteIdOrSlug) {
        return communityAdmin().listSiteRecruits(siteIdOrSlug);
    }

    @Transactional
    public List<Map<String, Object>> createSiteRecruit(String siteIdOrSlug, CreateRecruitRequest req) {
        return communityAdmin().createSiteRecruit(siteIdOrSlug, req);
    }

    public List<Map<String, Object>> listSiteTeamMembers(String siteIdOrSlug) {
        return communityAdmin().listSiteTeamMembers(siteIdOrSlug);
    }

    @Transactional
    public List<Map<String, Object>> applyToTeam(String siteIdOrSlug, TeamApplyRequest req) {
        return communityAdmin().applyToTeam(siteIdOrSlug, req);
    }

    @Transactional
    public List<Map<String, Object>> inviteTeamMember(String siteIdOrSlug, TeamInviteRequest req) {
        return communityAdmin().inviteTeamMember(siteIdOrSlug, req);
    }

    @Transactional
    public List<Map<String, Object>> updateTeamMemberStatus(String siteIdOrSlug, String memberId, UpdateMemberStatusRequest req) {
        return communityAdmin().updateTeamMemberStatus(siteIdOrSlug, memberId, req);
    }

    public List<Map<String, Object>> listNotifications(String username) {
        return communityAdmin().listNotifications(username);
    }

    @Transactional
    public void markAllNotificationsRead(String username) {
        communityAdmin().markAllNotificationsRead(username);
    }

    public Map<String, Object> getAdminDashboardSummary() {
        return communityAdmin().getAdminDashboardSummary();
    }

    public List<Map<String, Object>> listAdminIdeas(String siteType, String status, String keyword) {
        return communityAdmin().listAdminIdeas(siteType, status, keyword);
    }

    @Transactional
    public void deleteAdminIdea(String ideaId) {
        communityAdmin().deleteAdminIdea(ideaId);
    }

    public List<Map<String, Object>> listAdminPromptTemplates(String stageType) {
        return communityAdmin().listAdminPromptTemplates(stageType);
    }

    @Transactional
    public Map<String, Object> saveAdminPromptTemplate(SavePromptTemplateRequest req) {
        return communityAdmin().saveAdminPromptTemplate(req);
    }

    @Transactional
    public void deleteAdminPromptTemplate(String promptId) {
        communityAdmin().deleteAdminPromptTemplate(promptId);
    }

    public List<Map<String, Object>> listAdminSites(String siteType, String status, String keyword) {
        return communityAdmin().listAdminSites(siteType, status, keyword);
    }

    @Transactional
    public Map<String, Object> updateAdminSiteStatus(String siteId, UpdateSiteStatusRequest req) {
        return communityAdmin().updateAdminSiteStatus(siteId, req);
    }

    public List<Map<String, Object>> listAdminBuilds(String status, String deployMode) {
        return communityAdmin().listAdminBuilds(status, deployMode);
    }

    public List<Map<String, Object>> listAdminCommunityProjects(String siteType, String keyword) {
        return communityAdmin().listAdminCommunityProjects(siteType, keyword);
    }

    public List<Map<String, Object>> listModerationReports(String status) {
        return communityAdmin().listModerationReports(status);
    }

    @Transactional
    public Map<String, Object> createModerationReport(String siteIdOrSlug, CreateReportRequest req) {
        return communityAdmin().createModerationReport(siteIdOrSlug, req);
    }

    @Transactional
    public List<Map<String, Object>> resolveModerationReport(String reportId, ResolveReportRequest req) {
        return communityAdmin().resolveModerationReport(reportId, req);
    }

    public List<Map<String, Object>> listAdminApiKeys(String direction, String serviceCode, String status, String keyword) {
        return communityAdmin().listAdminApiKeys(direction, serviceCode, status, keyword);
    }

    @Transactional
    public Map<String, Object> saveAdminApiKey(SaveApiAccessKeyRequest req) {
        return communityAdmin().saveAdminApiKey(req);
    }

    @Transactional
    public Map<String, Object> rotateAdminApiKey(String keyId) {
        return communityAdmin().rotateAdminApiKey(keyId);
    }

    @Transactional
    public Map<String, Object> updateAdminApiKeyStatus(String keyId, String status) {
        return communityAdmin().updateAdminApiKeyStatus(keyId, status);
    }

    @Transactional
    public Map<String, Object> verifyAdminApiKey(String keyId) {
        return communityAdmin().verifyAdminApiKey(keyId);
    }

    @Transactional
    public void deleteAdminApiKey(String keyId) {
        communityAdmin().deleteAdminApiKey(keyId);
    }
}
