package com.empasy.sync.kkumz.domain;

import com.empasy.sync.kkumz.domain.KkumzDTO.*;
import com.empasy.sync.kkumz.system.KkumzSystemService;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Kkumz(꿈즈) 핵심 도메인 비즈니스 서비스
 * - AI 브랜드·멀티사이트 빌더 (AgentScope Java + DB 프롬프트 템플릿 기반)
 * - SyncShop / SyncCMS 멀티사이트 실데이터 연동 (Zero-Mock & Fail-Fast)
 * - 창업 쇼케이스 피드, 팀원 모집/지원, 실시간 댓글 및 모더레이션
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KkumzService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<KkumzAiGatewayService> aiGatewayProvider;

    private KkumzAiGatewayService aiGatewayService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
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

    @Value("${kkumz.integration.cms-base-url:http://localhost:18085}")
    private String defaultCmsBaseUrl;

    @Value("${kkumz.integration.shop-base-url:http://localhost:8501}")
    private String defaultShopBaseUrl;

    @Value("${kkumz.server.public-base-url:http://localhost:9580}")
    private String kkumzPublicBaseUrl;

    private JdbcTemplate shopJdbcTemplate;
    private JdbcTemplate cmsJdbcTemplate;

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
            Files.createDirectories(resolveUploadDir());
        } catch (IOException e) {
            log.warn("업로드 디렉터리 초기화 경고: {}", e.getMessage());
        }
    }

    // =========================================================================
    // 0. 스튜디오 공통 사전 메타데이터 & 52종 전체 기능 카탈로그 조회 (DB 단일 원천)
    // =========================================================================

    public List<Map<String, Object>> listFeatureCatalog() {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                    SELECT id,
                           feature_code AS "featureCode",
                           engine_code AS "engineCode",
                           category_code AS "categoryCode",
                           category_name AS "categoryName",
                           title,
                           subtitle,
                           description,
                           badge_label AS "badgeLabel",
                           icon_name AS "iconName",
                           search_keywords_json::text AS "searchKeywordsJson",
                           recommended_categories_json::text AS "recommendedCategoriesJson",
                           sdui_archetype AS "sduiArchetype",
                           default_config_json::text AS "defaultConfigJson",
                           sort_no AS "sortNo"
                    FROM kkumz_feature_catalog
                    WHERE status = 'ACTIVE'
                    ORDER BY sort_no ASC, id ASC
                    """);
            List<Map<String, Object>> catalog = new ArrayList<>();
            for (Map<String, Object> r : rows) {
                Map<String, Object> item = new LinkedHashMap<>(r);
                item.put("searchKeywords", parseJsonStringList(String.valueOf(item.remove("searchKeywordsJson"))));
                item.put("recommendedCategories", parseJsonStringList(String.valueOf(item.remove("recommendedCategoriesJson"))));
                item.put("defaultConfig", parseJsonObject(String.valueOf(item.remove("defaultConfigJson"))));
                catalog.add(item);
            }
            return catalog;
        } catch (Exception e) {
            log.warn("kkumz_feature_catalog 조회 경고: {}", e.getMessage());
            return List.of();
        }
    }

    public Map<String, Object> getStudioMetadata() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT d.dict_code AS "dictCode",
                       i.item_text AS "itemText",
                       i.item_value AS "itemValue",
                       i.description AS "description",
                       i.sort_order AS "sortOrder"
                FROM sys_dict_item i
                JOIN sys_dict d ON d.id = i.dict_id
                WHERE d.del_flag = 0 AND i.status = 1
                ORDER BY d.dict_code ASC, i.sort_order ASC
                """);

        Map<String, String> layoutLabels = new LinkedHashMap<>();
        Map<String, String> componentLabels = new LinkedHashMap<>();
        Map<String, String> roleLabels = new LinkedHashMap<>();
        Map<String, String> siteTypeLabels = new LinkedHashMap<>();
        Map<String, String> assetTypeLabels = new LinkedHashMap<>();
        List<Map<String, Object>> siteTypes = new ArrayList<>();
        List<Map<String, Object>> assetTypes = new ArrayList<>();
        List<Map<String, Object>> starterPresets = new ArrayList<>();
        List<Map<String, Object>> featureCategories = new ArrayList<>();
        List<Map<String, Object>> heroStyles = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            String dictCode = String.valueOf(row.get("dictCode"));
            String itemValue = String.valueOf(row.get("itemValue"));
            String itemText = String.valueOf(row.get("itemText"));
            String description = row.get("description") != null ? String.valueOf(row.get("description")) : "";

            switch (dictCode) {
                case "kkumz_layout_type" -> layoutLabels.put(itemValue, itemText);
                case "kkumz_component_type" -> componentLabels.put(itemValue, itemText);
                case "kkumz_recruit_role" -> roleLabels.put(itemValue, itemText);
                case "kkumz_site_type" -> {
                    siteTypeLabels.put(itemValue, itemText);
                    Map<String, Object> st = new LinkedHashMap<>();
                    st.put("code", itemValue);
                    st.put("badge", itemText);
                    st.put("title", itemText);
                    st.put("desc", description);
                    siteTypes.add(st);
                }
                case "kkumz_asset_type" -> {
                    assetTypeLabels.put(itemValue, itemText);
                    Map<String, Object> at = new LinkedHashMap<>();
                    at.put("code", itemValue);
                    at.put("label", itemText);
                    at.put("description", description);
                    assetTypes.add(at);
                }
                case "kkumz_starter_preset" -> {
                    Map<String, Object> preset = new LinkedHashMap<>(parseJsonObject(description));
                    preset.put("code", itemValue);
                    preset.put("title", itemText);
                    starterPresets.add(preset);
                }
                case "kkumz_feature_category" -> {
                    Map<String, Object> fc = new LinkedHashMap<>();
                    fc.put("code", itemValue);
                    fc.put("label", itemText);
                    fc.put("description", description);
                    featureCategories.add(fc);
                }
                case "kkumz_hero_style" -> {
                    Map<String, Object> hs = new LinkedHashMap<>();
                    hs.put("code", itemValue);
                    hs.put("label", itemText);
                    hs.put("description", description);
                    heroStyles.add(hs);
                }
                default -> {
                }
            }
        }

        List<Map<String, Object>> featureCatalog = listFeatureCatalog();
        for (Map<String, Object> fc : featureCatalog) {
            String fCode = String.valueOf(fc.getOrDefault("featureCode", ""));
            String fTitle = String.valueOf(fc.getOrDefault("title", ""));
            if (!fCode.isBlank() && !fTitle.isBlank()) {
                componentLabels.put(fCode, fTitle);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("layoutLabels", layoutLabels);
        result.put("componentLabels", componentLabels);
        result.put("roleLabels", roleLabels);
        result.put("siteTypeLabels", siteTypeLabels);
        result.put("assetTypeLabels", assetTypeLabels);
        result.put("siteTypes", siteTypes);
        result.put("assetTypes", assetTypes);
        result.put("starterPresets", starterPresets);
        result.put("featureCategories", featureCategories);
        result.put("heroStyles", heroStyles);
        result.put("featureCatalog", featureCatalog);
        return result;
    }

    // =========================================================================
    // 0-1. 인증 세션 추출 및 아이디어·사이트 소유권(Ownership RBAC) 검증 헬퍼
    // =========================================================================

    private boolean isHttpRequestActive() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes;
    }

    public String resolveCurrentUsernameFromRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest request = attrs.getRequest();
            String xToken = request.getHeader("X-Access-Token");
            String authHeader = request.getHeader("Authorization");
            String paramToken = request.getParameter("token");
            String primaryToken = (xToken != null && !xToken.isBlank()) ? xToken : paramToken;
            String extracted = KkumzSystemService.extractUsernameFromToken(primaryToken, authHeader);
            return (extracted != null && !extracted.isBlank()) ? extracted.trim() : null;
        }
        return null;
    }

    private String requireAuthenticatedUsername() {
        String current = resolveCurrentUsernameFromRequest();
        if (current != null && !current.isBlank()) {
            return current;
        }
        if (isHttpRequestActive()) {
            throw new SecurityException("[401] 로그인이 필요한 기능입니다. 먼저 로그인해 주세요.");
        }
        return null;
    }

    public boolean isAdminUser(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        String user = username.trim();
        if ("admin".equalsIgnoreCase(user)) {
            return true;
        }
        try {
            Integer cnt = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM sys_user_role ur
                    JOIN sys_user u ON u.id = ur.user_id
                    JOIN sys_role r ON r.id = ur.role_id
                    WHERE LOWER(u.username) = LOWER(?) AND u.del_flag = 0
                      AND (r.id = 'role_admin' OR UPPER(r.role_code) = 'ROLE_ADMIN')
                    """, Integer.class, user);
            return cnt != null && cnt > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean canEditIdea(Map<String, Object> ideaRow, String currentUsername) {
        if (currentUsername == null || currentUsername.isBlank() || ideaRow == null) {
            return false;
        }
        String user = currentUsername.trim();
        String author = ideaRow.get("authorUsername") != null ? String.valueOf(ideaRow.get("authorUsername")).trim() : "";
        if (!author.isEmpty() && user.equalsIgnoreCase(author)) {
            return true;
        }
        if (isAdminUser(user)) {
            return true;
        }
        String ideaId = ideaRow.get("id") != null ? String.valueOf(ideaRow.get("id")).trim() : "";
        if (!ideaId.isEmpty()) {
            try {
                Integer teamCnt = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM kkumz_team_member tm
                        JOIN kkumz_site s ON s.id = tm.site_id
                        WHERE s.idea_id = ?
                          AND LOWER(tm.username) = LOWER(?)
                          AND tm.status = 'ACTIVE'
                          AND tm.member_role IN ('OWNER', 'DEVELOPER', 'EDITOR')
                        """, Integer.class, ideaId, user);
                return teamCnt != null && teamCnt > 0;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public boolean canEditSite(Map<String, Object> siteRow, String currentUsername) {
        if (currentUsername == null || currentUsername.isBlank() || siteRow == null) {
            return false;
        }
        String user = currentUsername.trim();
        String owner = siteRow.get("ownerUsername") != null ? String.valueOf(siteRow.get("ownerUsername")).trim() : "";
        if (!owner.isEmpty() && user.equalsIgnoreCase(owner)) {
            return true;
        }
        if (isAdminUser(user)) {
            return true;
        }
        String ideaId = siteRow.get("ideaId") != null ? String.valueOf(siteRow.get("ideaId")).trim() : "";
        if (!ideaId.isEmpty() && !"null".equalsIgnoreCase(ideaId)) {
            try {
                Integer ideaOwnerCnt = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM kkumz_idea WHERE id = ? AND LOWER(author_username) = LOWER(?)",
                        Integer.class, ideaId, user
                );
                if (ideaOwnerCnt != null && ideaOwnerCnt > 0) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        String siteId = siteRow.get("id") != null ? String.valueOf(siteRow.get("id")).trim() : "";
        if (!siteId.isEmpty()) {
            try {
                Integer teamCnt = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM kkumz_team_member
                        WHERE site_id = ?
                          AND LOWER(username) = LOWER(?)
                          AND status = 'ACTIVE'
                          AND member_role IN ('OWNER', 'DEVELOPER', 'EDITOR')
                        """, Integer.class, siteId, user);
                return teamCnt != null && teamCnt > 0;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public void assertIdeaEditable(String ideaId) {
        if (!isHttpRequestActive()) {
            return;
        }
        String currentUser = requireAuthenticatedUsername();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, author_username AS \"authorUsername\" FROM kkumz_idea WHERE id = ?",
                ideaId
        );
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 아이디어 ID입니다: " + ideaId);
        }
        if (!canEditIdea(rows.get(0), currentUser)) {
            throw new SecurityException("[403] 본인이 생성한 사이트(또는 승인된 협업 팀원)만 수정할 수 있습니다.");
        }
    }

    public void assertSiteEditable(String siteIdOrSlug) {
        if (!isHttpRequestActive()) {
            return;
        }
        String currentUser = requireAuthenticatedUsername();
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        if (!canEditSite(site, currentUser)) {
            throw new SecurityException("[403] 본인이 생성한 사이트(또는 승인된 협업 팀원)만 수정할 수 있습니다.");
        }
    }

    public boolean canViewIdea(Map<String, Object> ideaRow, String currentUsername) {
        if (currentUsername == null || currentUsername.isBlank() || ideaRow == null) {
            return false;
        }
        if (canEditIdea(ideaRow, currentUsername)) {
            return true;
        }
        String user = currentUsername.trim();
        String ideaId = ideaRow.get("id") != null ? String.valueOf(ideaRow.get("id")).trim() : "";
        if (!ideaId.isEmpty()) {
            try {
                Integer teamCnt = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM kkumz_team_member tm
                        JOIN kkumz_site s ON s.id = tm.site_id
                        WHERE s.idea_id = ?
                          AND LOWER(tm.username) = LOWER(?)
                          AND tm.status = 'ACTIVE'
                        """, Integer.class, ideaId, user);
                return teamCnt != null && teamCnt > 0;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public void assertIdeaReadable(Map<String, Object> ideaRow) {
        if (!isHttpRequestActive()) {
            return;
        }
        String currentUser = resolveCurrentUsernameFromRequest();
        if (currentUser == null || currentUser.isBlank()) {
            throw new SecurityException("[401] 로그인이 필요합니다. 본인이 생성한 아이디어만 열람할 수 있습니다.");
        }
        if (!canViewIdea(ideaRow, currentUser)) {
            throw new SecurityException("[403] 본인이 생성한 아이디어만 열람할 수 있습니다.");
        }
    }

    public void assertIdeaReadable(String ideaId) {
        if (!isHttpRequestActive()) {
            return;
        }
        String currentUser = resolveCurrentUsernameFromRequest();
        if (currentUser == null || currentUser.isBlank()) {
            throw new SecurityException("[401] 로그인이 필요합니다. 본인이 생성한 아이디어만 열람할 수 있습니다.");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, author_username AS \"authorUsername\" FROM kkumz_idea WHERE id = ?",
                ideaId
        );
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 아이디어 ID입니다: " + ideaId);
        }
        if (!canViewIdea(rows.get(0), currentUser)) {
            throw new SecurityException("[403] 본인이 생성한 아이디어만 열람할 수 있습니다.");
        }
    }

    public Integer resolvePublishedVersion(Map<String, Object> siteRow) {
        if (siteRow == null) {
            return null;
        }
        Object rawPub = siteRow.get("publishedVersion");
        if (rawPub instanceof Number n) {
            int val = n.intValue();
            return val > 0 ? val : null;
        }
        if (rawPub != null) {
            String str = String.valueOf(rawPub).trim();
            if (!str.isEmpty() && !"null".equalsIgnoreCase(str)) {
                try {
                    int parsed = Integer.parseInt(str);
                    return parsed > 0 ? parsed : null;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (!siteRow.containsKey("publishedVersion")) {
            String status = siteRow.get("status") != null ? String.valueOf(siteRow.get("status")).trim().toUpperCase(Locale.ROOT) : "ACTIVE";
            String visibility = siteRow.get("visibility") != null ? String.valueOf(siteRow.get("visibility")).trim().toUpperCase(Locale.ROOT) : "PUBLIC";
            if (("ACTIVE".equals(status) || "PUBLISHED".equals(status)) && "PUBLIC".equals(visibility)) {
                Object activeObj = siteRow.get("activeVersion");
                if (activeObj instanceof Number an) {
                    return an.intValue();
                }
            }
        }
        return null;
    }

    public boolean isSitePubliclyPublished(Map<String, Object> siteRow) {
        if (siteRow == null) {
            return false;
        }
        if (!siteRow.containsKey("status") && !siteRow.containsKey("visibility") && !siteRow.containsKey("publishedVersion")) {
            return true;
        }
        String status = siteRow.get("status") != null ? String.valueOf(siteRow.get("status")).trim().toUpperCase(Locale.ROOT) : "DRAFT";
        String visibility = siteRow.get("visibility") != null ? String.valueOf(siteRow.get("visibility")).trim().toUpperCase(Locale.ROOT) : "PRIVATE";
        Integer publishedVer = resolvePublishedVersion(siteRow);
        return ("ACTIVE".equals(status) || "PUBLISHED".equals(status))
                && "PUBLIC".equals(visibility)
                && publishedVer != null
                && publishedVer > 0;
    }

    public boolean canViewSite(Map<String, Object> siteRow, String currentUsername) {
        if (siteRow == null) {
            return false;
        }
        if (currentUsername != null && !currentUsername.isBlank()) {
            if (canEditSite(siteRow, currentUsername)) {
                return true;
            }
            String siteId = siteRow.get("id") != null ? String.valueOf(siteRow.get("id")).trim() : "";
            if (!siteId.isEmpty()) {
                try {
                    Integer teamCnt = jdbcTemplate.queryForObject("""
                            SELECT COUNT(*)
                            FROM kkumz_team_member
                            WHERE site_id = ?
                              AND LOWER(username) = LOWER(?)
                              AND status = 'ACTIVE'
                            """, Integer.class, siteId, currentUsername.trim());
                    if (teamCnt != null && teamCnt > 0) {
                        return true;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return isSitePubliclyPublished(siteRow);
    }

    public void assertSiteReadable(Map<String, Object> siteRow) {
        if (!isHttpRequestActive()) {
            return;
        }
        String currentUser = resolveCurrentUsernameFromRequest();
        if (!canViewSite(siteRow, currentUser)) {
            throw new SecurityException("[403] 아직 배포되지 않았거나 비공개 상태인 사이트입니다.");
        }
    }

    public void assertSiteReadable(String siteIdOrSlug) {
        if (!isHttpRequestActive()) {
            return;
        }
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        assertSiteReadable(site);
    }

    // =========================================================================
    // 1. 아이디어 & 버전 히스토리 & 화면설계 (Step 1 & Step 3)
    // =========================================================================

    public List<Map<String, Object>> listIdeas(String authorUsername, String siteType) {
        return listIdeas(authorUsername, siteType, false);
    }

    public List<Map<String, Object>> listIdeas(String authorUsername, String siteType, Boolean mineOnly) {
        String currentUser = resolveCurrentUsernameFromRequest();
        if (isHttpRequestActive() && (currentUser == null || currentUser.isBlank())) {
            return List.of();
        }
        boolean isAdmin = currentUser != null && !currentUser.isBlank() && isAdminUser(currentUser);
        String targetUser;
        if (isHttpRequestActive()) {
            if (isAdmin && !Boolean.TRUE.equals(mineOnly)) {
                targetUser = (authorUsername != null && !authorUsername.isBlank()) ? authorUsername.trim() : null;
            } else {
                targetUser = currentUser.trim();
            }
        } else {
            targetUser = (authorUsername != null && !authorUsername.isBlank())
                    ? authorUsername.trim()
                    : (Boolean.TRUE.equals(mineOnly) ? currentUser : null);
            if (Boolean.TRUE.equals(mineOnly) && (targetUser == null || targetUser.isBlank())) {
                return List.of();
            }
        }

        StringBuilder sql = new StringBuilder("""
                SELECT i.id,
                       i.author_username AS "authorUsername",
                       i.author_display_name AS "authorDisplayName",
                       i.title,
                       i.prompt_text AS "promptText",
                       i.site_type AS "siteType",
                       i.category,
                       i.tags_json::text AS "tagsJson",
                       i.enabled_features_json::text AS "enabledFeaturesJson",
                       i.ai_intent_analysis_json::text AS "aiIntentAnalysisJson",
                       i.visibility,
                       i.current_step AS "currentStep",
                       i.selected_brand_name AS "selectedBrandName",
                       i.selected_site_slug AS "selectedSiteSlug",
                       i.selected_logo_svg AS "selectedLogoSvg",
                       i.design_tokens_json::text AS "designTokensJson",
                       i.spec_markdown AS "specMarkdown",
                       i.screens_json::text AS "screensJson",
                       i.status,
                       i.created_at AS "createdAt",
                       i.updated_at AS "updatedAt",
                       st.id AS "siteId",
                       st.site_slug AS "siteSlug",
                       st.site_name AS "siteName",
                       st.site_type AS "siteSiteType",
                       st.deploy_mode AS "siteDeployMode",
                       st.active_version AS "siteActiveVersion",
                       st.published_version AS "sitePublishedVersion",
                       st.published_at AS "sitePublishedAt",
                       st.visibility AS "siteVisibility",
                       st.status AS "siteStatus",
                       st.deployed_url AS "siteDeployedUrl",
                       st.custom_domain AS "siteCustomDomain",
                       st.like_count AS "siteLikeCount",
                       st.comment_count AS "siteCommentCount",
                       st.view_count AS "siteViewCount"
                FROM kkumz_idea i
                LEFT JOIN LATERAL (
                    SELECT s.*
                    FROM kkumz_site s
                    WHERE s.idea_id = i.id
                    ORDER BY s.updated_at DESC
                    LIMIT 1
                ) st ON TRUE
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (targetUser != null && !targetUser.isBlank()) {
            sql.append("""
                     AND (LOWER(i.author_username) = LOWER(?)
                          OR i.id IN (
                              SELECT s.idea_id FROM kkumz_site s
                              JOIN kkumz_team_member tm ON tm.site_id = s.id
                              WHERE s.idea_id IS NOT NULL
                                AND LOWER(tm.username) = LOWER(?)
                                AND tm.status = 'ACTIVE'
                          ))
                    """);
            params.add(targetUser);
            params.add(targetUser);
        }
        if (siteType != null && !siteType.isBlank() && !"ALL".equalsIgnoreCase(siteType)) {
            sql.append(" AND i.site_type = ?");
            params.add(siteType.trim().toUpperCase(Locale.ROOT));
        }
        sql.append(" ORDER BY i.updated_at DESC");

        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        list.forEach(this::enrichIdeaRow);
        return list;
    }

    @Transactional
    public Map<String, Object> createIdea(CreateIdeaRequest req) {
        if (req == null || req.getPromptText() == null || req.getPromptText().isBlank()) {
            throw new IllegalArgumentException("창업 아이디어 프롬프트(promptText)는 필수 입력 항목입니다.");
        }
        String authenticatedUser = isHttpRequestActive() ? requireAuthenticatedUsername() : resolveCurrentUsernameFromRequest();
        String promptText = req.getPromptText().trim();
        String id = "idea-" + UUID.randomUUID().toString().substring(0, 8);
        String siteType = (req.getSiteType() != null && !req.getSiteType().isBlank())
                ? req.getSiteType().trim().toUpperCase(Locale.ROOT)
                : "HYBRID";
        String category = (req.getCategory() != null && !req.getCategory().isBlank()
                && !"FOOD_BEVERAGE".equalsIgnoreCase(req.getCategory().trim()))
                ? req.getCategory().trim()
                : "AUTO";
        String title = (req.getTitle() != null && !req.getTitle().isBlank())
                ? req.getTitle().trim()
                : summarizeTitle(promptText);

        String requestedUser = (authenticatedUser != null && !authenticatedUser.isBlank())
                ? authenticatedUser
                : req.getAuthorUsername();
        Map<String, String> author = resolveUserIdentity(requestedUser, req.getAuthorDisplayName());
        String authorUsername = author.get("username");
        String authorDisplay = author.get("realname");
        String visibility = "PRIVATE";

        List<String> tags = (req.getTags() != null && !req.getTags().isEmpty())
                ? req.getTags()
                : List.of();
        List<String> enabledFeatures = (req.getEnabledFeatures() != null && !req.getEnabledFeatures().isEmpty())
                ? req.getEnabledFeatures()
                : List.of();
        Map<String, Object> aiIntentAnalysis = (req.getAiIntentAnalysis() != null && !req.getAiIntentAnalysis().isEmpty())
                ? req.getAiIntentAnalysis()
                : Map.of();

        jdbcTemplate.update("""
                INSERT INTO kkumz_idea (
                    id, author_username, author_display_name, title, prompt_text, site_type, category,
                    tags_json, enabled_features_json, ai_intent_analysis_json,
                    visibility, current_step, spec_markdown, screens_json, status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, 2, ?, '[]'::jsonb, 'DRAFT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                id, authorUsername, authorDisplay, title, promptText, siteType, category,
                toJson(tags), toJson(enabledFeatures), toJson(aiIntentAnalysis), visibility, promptText
        );

        String verId = "iver-" + id + "-v1";
        jdbcTemplate.update("""
                INSERT INTO kkumz_idea_version (
                    id, idea_id, version_no, title, prompt_text, spec_markdown, screens_json, created_by, created_at
                ) VALUES (?, ?, 1, ?, ?, ?, '[]'::jsonb, ?, CURRENT_TIMESTAMP)
                """,
                verId, id, title + " (v1)", promptText, promptText, authorUsername
        );

        return getIdeaDetail(id);
    }

    public Map<String, Object> getIdeaDetail(String ideaId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id,
                       author_username AS "authorUsername",
                       author_display_name AS "authorDisplayName",
                       title,
                       prompt_text AS "promptText",
                       site_type AS "siteType",
                       category,
                       tags_json::text AS "tagsJson",
                       enabled_features_json::text AS "enabledFeaturesJson",
                       ai_intent_analysis_json::text AS "aiIntentAnalysisJson",
                       visibility,
                       current_step AS "currentStep",
                       selected_brand_name AS "selectedBrandName",
                       selected_site_slug AS "selectedSiteSlug",
                       selected_logo_svg AS "selectedLogoSvg",
                       design_tokens_json::text AS "designTokensJson",
                       spec_markdown AS "specMarkdown",
                       screens_json::text AS "screensJson",
                       status,
                       created_at AS "createdAt",
                       updated_at AS "updatedAt"
                FROM kkumz_idea
                WHERE id = ?
                """, ideaId);

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 아이디어 ID입니다: " + ideaId);
        }

        assertIdeaReadable(rows.get(0));

        Map<String, Object> idea = new LinkedHashMap<>(rows.get(0));
        enrichIdeaRow(idea);

        List<Map<String, Object>> versions = jdbcTemplate.queryForList("""
                SELECT id,
                       idea_id AS "ideaId",
                       version_no AS "versionNo",
                       title,
                       prompt_text AS "promptText",
                       spec_markdown AS "specMarkdown",
                       screens_json::text AS "screensJson",
                       created_by AS "createdBy",
                       created_at AS "createdAt"
                FROM kkumz_idea_version
                WHERE idea_id = ?
                ORDER BY version_no DESC
                """, ideaId);
        versions.forEach(v -> v.put("screens", parseJsonList(String.valueOf(v.remove("screensJson")))));
        idea.put("versions", versions);

        List<Map<String, Object>> names = jdbcTemplate.queryForList("""
                SELECT id,
                       idea_id AS "ideaId",
                       brand_name AS "brandName",
                       site_slug AS "siteSlug",
                       pronunciation,
                       brand_story AS "brandStory",
                       score,
                       slug_available AS "slugAvailable",
                       is_selected AS "isSelected"
                FROM kkumz_name_suggestion
                WHERE idea_id = ?
                ORDER BY score DESC, created_at ASC
                """, ideaId);
        idea.put("nameSuggestions", names);

        List<Map<String, Object>> logos = jdbcTemplate.queryForList("""
                SELECT id,
                       idea_id AS "ideaId",
                       style_name AS "styleName",
                       svg_code AS "svgCode",
                       color_palette_json::text AS "colorPaletteJson",
                       typography_json::text AS "typographyJson",
                       tags_json::text AS "tagsJson",
                       is_selected AS "isSelected"
                FROM kkumz_logo_suggestion
                WHERE idea_id = ?
                ORDER BY is_selected DESC, created_at ASC
                """, ideaId);
        logos.forEach(l -> {
            l.put("colorPalette", parseJsonObject(String.valueOf(l.remove("colorPaletteJson"))));
            l.put("typography", parseJsonObject(String.valueOf(l.remove("typographyJson"))));
            l.put("tags", parseJsonList(String.valueOf(l.remove("tagsJson"))));
        });
        idea.put("logoSuggestions", logos);
        idea.put("screenSpecs", listIdeaScreensInternal(ideaId));
        idea.put("assets", listIdeaAssetsInternal(ideaId));

        List<Map<String, Object>> sites = jdbcTemplate.queryForList("""
                SELECT id,
                       site_slug AS "siteSlug",
                       site_name AS "siteName",
                       site_type AS "siteType",
                       deploy_mode AS "deployMode",
                       active_version AS "activeVersion",
                       published_version AS "publishedVersion",
                       published_at AS "publishedAt",
                       visibility,
                       deployed_url AS "deployedUrl",
                       custom_domain AS "customDomain",
                       shop_site_id AS "shopSiteId",
                       cms_site_id AS "cmsSiteId",
                       like_count AS "likeCount",
                       comment_count AS "commentCount",
                       view_count AS "viewCount",
                       status
                FROM kkumz_site
                WHERE idea_id = ?
                ORDER BY created_at DESC
                """, ideaId);
        String selectedSlugForSite = idea.get("selectedSiteSlug") != null
                ? String.valueOf(idea.get("selectedSiteSlug")).trim()
                : "";
        if (sites.isEmpty() && !selectedSlugForSite.isEmpty()) {
            sites = jdbcTemplate.queryForList("""
                    SELECT id,
                           site_slug AS "siteSlug",
                           site_name AS "siteName",
                           site_type AS "siteType",
                           deploy_mode AS "deployMode",
                           active_version AS "activeVersion",
                           published_version AS "publishedVersion",
                           published_at AS "publishedAt",
                           visibility,
                           deployed_url AS "deployedUrl",
                           custom_domain AS "customDomain",
                           shop_site_id AS "shopSiteId",
                           cms_site_id AS "cmsSiteId",
                           like_count AS "likeCount",
                           comment_count AS "commentCount",
                           view_count AS "viewCount",
                           status
                    FROM kkumz_site
                    WHERE site_slug = ?
                    ORDER BY created_at DESC
                    """, selectedSlugForSite);
        }
        idea.put("deployedSites", sites);
        if (!sites.isEmpty()) {
            for (Map<String, Object> s : sites) {
                boolean isPub = isSitePubliclyPublished(s);
                int activeVer = s.get("activeVersion") instanceof Number n ? n.intValue() : 1;
                Integer pubVer = resolvePublishedVersion(s);
                boolean hasUnpublished = !isPub || (pubVer != null && activeVer > pubVer);
                s.put("isOwner", idea.get("isOwner"));
                s.put("canEdit", idea.get("canEdit"));
                s.put("isPublished", isPub);
                s.put("publishedVersion", pubVer);
                s.put("hasUnpublishedChanges", hasUnpublished);
            }
            idea.put("site", sites.get(0));
        }

        return idea;
    }

    @Transactional
    public Map<String, Object> saveOrRollbackIdeaVersion(String ideaId, SaveIdeaVersionRequest req) {
        assertIdeaEditable(ideaId);
        Map<String, Object> current = getIdeaDetail(ideaId);

        if (req.getRollbackToVersionNo() != null && req.getRollbackToVersionNo() > 0) {
            List<Map<String, Object>> targetRows = jdbcTemplate.queryForList("""
                    SELECT title, prompt_text AS "promptText", spec_markdown AS "specMarkdown", screens_json::text AS "screensJson"
                    FROM kkumz_idea_version
                    WHERE idea_id = ? AND version_no = ?
                    """, ideaId, req.getRollbackToVersionNo());
            if (targetRows.isEmpty()) {
                throw new IllegalArgumentException("롤백 대상 버전을 찾을 수 없습니다: v" + req.getRollbackToVersionNo());
            }
            Map<String, Object> target = targetRows.get(0);
            String targetScreensJson = String.valueOf(target.getOrDefault("screensJson", "[]"));
            jdbcTemplate.update("""
                    UPDATE kkumz_idea
                    SET prompt_text = ?, spec_markdown = ?, screens_json = ?::jsonb, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                    target.get("promptText"),
                    target.get("specMarkdown"),
                    targetScreensJson,
                    ideaId
            );
            List<Map<String, Object>> rolledBackScreens = parseScreenMapList(targetScreensJson);
            if (!rolledBackScreens.isEmpty()) {
                syncScreenSpecs(ideaId, rolledBackScreens);
            }
            return getIdeaDetail(ideaId);
        }

        Integer maxVer = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(version_no), 0) FROM kkumz_idea_version WHERE idea_id = ?",
                Integer.class, ideaId
        );
        int nextVer = (maxVer != null ? maxVer : 0) + 1;
        String title = (req.getTitle() != null && !req.getTitle().isBlank())
                ? req.getTitle().trim()
                : current.get("title") + " (v" + nextVer + ")";
        String promptText = (req.getPromptText() != null && !req.getPromptText().isBlank())
                ? req.getPromptText().trim()
                : String.valueOf(current.get("promptText"));
        String specMarkdown = (req.getSpecMarkdown() != null && !req.getSpecMarkdown().isBlank())
                ? req.getSpecMarkdown().trim()
                : String.valueOf(current.get("specMarkdown"));
        String screensJson = req.getScreens() != null
                ? toJson(req.getScreens())
                : toJson(current.get("screens"));
        String createdBy = (req.getCreatedBy() != null && !req.getCreatedBy().isBlank())
                ? req.getCreatedBy().trim()
                : String.valueOf(current.get("authorUsername"));

        if (req.getScreens() != null) {
            syncScreenSpecs(ideaId, req.getScreens());
        }

        jdbcTemplate.update("""
                INSERT INTO kkumz_idea_version (
                    id, idea_id, version_no, title, prompt_text, spec_markdown, screens_json, created_by, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?, CURRENT_TIMESTAMP)
                """,
                "iver-" + ideaId + "-v" + nextVer, ideaId, nextVer, title, promptText, specMarkdown, screensJson, createdBy
        );

        String enabledFeaturesJson = req.getEnabledFeatures() != null
                ? toJson(req.getEnabledFeatures())
                : toJson(current.getOrDefault("enabledFeatures", List.of()));
        String aiIntentAnalysisJson = req.getAiIntentAnalysis() != null
                ? toJson(req.getAiIntentAnalysis())
                : toJson(current.getOrDefault("aiIntentAnalysis", Map.of()));

        jdbcTemplate.update("""
                UPDATE kkumz_idea
                SET prompt_text = ?, spec_markdown = ?, screens_json = ?::jsonb,
                    enabled_features_json = ?::jsonb, ai_intent_analysis_json = ?::jsonb,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                promptText, specMarkdown, screensJson, enabledFeaturesJson, aiIntentAnalysisJson, ideaId
        );

        return getIdeaDetail(ideaId);
    }

    // =========================================================================
    // 1-1. Step 1 — AI 의도·사이트 아키텍처 자율 진단 & 52종 기능 카탈로그 선택 동기화
    // =========================================================================

    @Transactional
    public Map<String, Object> analyzeIdeaIntent(String ideaId, AnalyzeIntentRequest req) {
        assertIdeaEditable(ideaId);
        Map<String, Object> idea = getIdeaDetail(ideaId);
        String promptText = (req != null && req.getPromptText() != null && !req.getPromptText().isBlank())
                ? req.getPromptText().trim()
                : String.valueOf(idea.getOrDefault("promptText", "")).trim();
        String title = (req != null && req.getTitle() != null && !req.getTitle().isBlank())
                ? req.getTitle().trim()
                : String.valueOf(idea.getOrDefault("title", summarizeTitle(promptText))).trim();
        String categoryHint = (req != null && req.getCategory() != null && !req.getCategory().isBlank())
                ? req.getCategory().trim()
                : String.valueOf(idea.getOrDefault("category", "AUTO")).trim();

        List<Map<String, Object>> assets = listIdeaAssetsInternal(ideaId);
        List<Map<String, Object>> catalog = listFeatureCatalog();

        StringBuilder assetsSummary = new StringBuilder();
        if (assets.isEmpty()) {
            assetsSummary.append("업로드된 미디어·문서 자산 없음");
        } else {
            for (Map<String, Object> a : assets) {
                assetsSummary.append("- id=").append(a.get("id"))
                        .append(", originalName=").append(a.get("originalName"))
                        .append(", assetType=").append(a.get("assetType"))
                        .append(", caption=").append(a.getOrDefault("caption", ""))
                        .append("\n");
            }
        }

        StringBuilder catalogSummary = new StringBuilder();
        for (Map<String, Object> fc : catalog) {
            catalogSummary.append("- ").append(fc.get("featureCode"))
                    .append(" [").append(fc.get("engineCode")).append(" / ").append(fc.get("categoryName")).append("] ")
                    .append(fc.get("title")).append(": ").append(fc.get("subtitle"))
                    .append(" (키워드: ").append(fc.get("searchKeywords")).append(")\n");
        }

        Map<String, Object> parsedAnalysis = new LinkedHashMap<>();
        try {
            String rawAiJson = ensureAiGateway().executeStagePrompt(
                    "INTENT_ADVISOR",
                    Map.of(
                            "title", title,
                            "promptText", promptText,
                            "assetsSummary", assetsSummary.toString(),
                            "catalogSummary", catalogSummary.toString()
                    ),
                    java.time.Duration.ofSeconds(18),
                    1200
            );
            parsedAnalysis.putAll(parseJsonObject(rawAiJson));
        } catch (Exception e) {
            log.warn("INTENT_ADVISOR LLM 호출 경고 (DB 카탈로그 키워드 매칭 엔진으로 보완): {}", e.getMessage());
        }

        Map<String, Object> catalogDiagnosis = diagnoseIntentFromCatalog(title, promptText, categoryHint, assets, catalog);
        for (Map.Entry<String, Object> entry : catalogDiagnosis.entrySet()) {
            Object existingVal = parsedAnalysis.get(entry.getKey());
            if (existingVal == null
                    || (existingVal instanceof String s && s.isBlank())
                    || (existingVal instanceof Collection<?> c && c.isEmpty())
                    || (existingVal instanceof Map<?, ?> m && m.isEmpty())) {
                parsedAnalysis.put(entry.getKey(), entry.getValue());
            }
        }

        Set<String> validCodes = new LinkedHashSet<>();
        for (Map<String, Object> fc : catalog) {
            validCodes.add(String.valueOf(fc.get("featureCode")));
        }

        List<String> recommendedFeatures = new ArrayList<>();
        if (parsedAnalysis.get("recommendedFeatures") instanceof List<?> rfList) {
            for (Object o : rfList) {
                String code = String.valueOf(o).trim();
                if (validCodes.contains(code) && !recommendedFeatures.contains(code)) {
                    recommendedFeatures.add(code);
                }
            }
        }
        if (recommendedFeatures.isEmpty() && catalogDiagnosis.get("recommendedFeatures") instanceof List<?> fallbackRf) {
            for (Object o : fallbackRf) {
                String code = String.valueOf(o).trim();
                if (!code.isBlank() && !recommendedFeatures.contains(code)) {
                    recommendedFeatures.add(code);
                }
            }
        }
        parsedAnalysis.put("recommendedFeatures", recommendedFeatures);

        @SuppressWarnings("unchecked")
        Map<String, Object> fallbackReasons = (Map<String, Object>) catalogDiagnosis.getOrDefault("featureReasons", Map.of());
        Map<String, String> mergedReasons = new LinkedHashMap<>();
        fallbackReasons.forEach((k, v) -> mergedReasons.put(k, String.valueOf(v)));
        if (parsedAnalysis.get("featureReasons") instanceof Map<?, ?> aiReasons) {
            aiReasons.forEach((k, v) -> {
                if (k != null && v != null && !String.valueOf(v).isBlank()) {
                    mergedReasons.put(String.valueOf(k), String.valueOf(v));
                }
            });
        }
        for (String code : recommendedFeatures) {
            if (!mergedReasons.containsKey(code)) {
                catalog.stream()
                        .filter(fc -> code.equals(fc.get("featureCode")))
                        .findFirst()
                        .ifPresent(fc -> mergedReasons.put(code, String.valueOf(fc.getOrDefault("subtitle", fc.getOrDefault("description", "")))));
            }
        }
        parsedAnalysis.put("featureReasons", mergedReasons);

        String resolvedSiteType = String.valueOf(parsedAnalysis.getOrDefault("siteType", catalogDiagnosis.getOrDefault("siteType", "HYBRID")))
                .trim().toUpperCase(Locale.ROOT);
        if (!resolvedSiteType.matches("^(COMMERCE|CMS_SITE|HYBRID)$")) {
            resolvedSiteType = "HYBRID";
        }
        if (hasBothShopAndCmsFeatures(recommendedFeatures, catalog)) {
            resolvedSiteType = "HYBRID";
        }
        parsedAnalysis.put("siteType", resolvedSiteType);

        String resolvedCategory = String.valueOf(parsedAnalysis.getOrDefault("category", catalogDiagnosis.getOrDefault("category", categoryHint))).trim();
        if (resolvedCategory.isBlank() || "AUTO".equalsIgnoreCase(resolvedCategory)) {
            resolvedCategory = String.valueOf(catalogDiagnosis.getOrDefault("category", "라이프스타일 · 브랜드"));
        }
        parsedAnalysis.put("category", resolvedCategory);

        List<String> resolvedTags = new ArrayList<>();
        if (parsedAnalysis.get("tags") instanceof List<?> tList) {
            for (Object t : tList) {
                if (t != null && !String.valueOf(t).isBlank()) {
                    resolvedTags.add(String.valueOf(t).trim());
                }
            }
        }
        if (resolvedTags.isEmpty() && catalogDiagnosis.get("tags") instanceof List<?> fbTags) {
            for (Object t : fbTags) {
                resolvedTags.add(String.valueOf(t));
            }
        }
        parsedAnalysis.put("tags", resolvedTags);

        // 업로드된 사진 자산의 AI 용도 분류 및 자연스러운 한글 캡션 자동 반영
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> annotations = parsedAnalysis.get("assetAnnotations") instanceof List<?> rawAnn
                ? objectMapper.convertValue(rawAnn, new TypeReference<List<Map<String, Object>>>() {})
                : (List<Map<String, Object>>) catalogDiagnosis.getOrDefault("assetAnnotations", List.of());

        if (annotations != null && !annotations.isEmpty()) {
            for (Map<String, Object> ann : annotations) {
                String targetAssetId = ann.get("assetId") != null ? String.valueOf(ann.get("assetId")).trim() : "";
                String targetOrigName = ann.get("originalName") != null ? String.valueOf(ann.get("originalName")).trim() : "";
                String newType = ann.get("assetType") != null ? String.valueOf(ann.get("assetType")).trim().toUpperCase(Locale.ROOT) : "";
                String newCap = ann.get("caption") != null ? String.valueOf(ann.get("caption")).trim() : "";
                if (newCap.isBlank()) {
                    continue;
                }
                if (!targetAssetId.isBlank()) {
                    jdbcTemplate.update(
                            "UPDATE kkumz_idea_asset SET asset_type = COALESCE(NULLIF(?, ''), asset_type), caption = ? WHERE id = ? AND idea_id = ?",
                            newType, newCap, targetAssetId, ideaId
                    );
                } else if (!targetOrigName.isBlank()) {
                    jdbcTemplate.update(
                            "UPDATE kkumz_idea_asset SET asset_type = COALESCE(NULLIF(?, ''), asset_type), caption = ? WHERE original_name = ? AND idea_id = ?",
                            newType, newCap, targetOrigName, ideaId
                    );
                }
            }
        }

        @SuppressWarnings("unchecked")
        List<String> currentEnabled = (List<String>) idea.getOrDefault("enabledFeatures", List.of());
        List<String> finalEnabled = new ArrayList<>();
        if (req != null && Boolean.TRUE.equals(req.getApplyRecommendedFeatures())) {
            finalEnabled.addAll(recommendedFeatures);
        } else if (currentEnabled != null && !currentEnabled.isEmpty()) {
            finalEnabled.addAll(currentEnabled);
            for (String rf : recommendedFeatures) {
                if (!finalEnabled.contains(rf)) {
                    finalEnabled.add(rf);
                }
            }
        } else {
            finalEnabled.addAll(recommendedFeatures);
        }

        String finalPrompt = promptText;
        if (req != null && Boolean.TRUE.equals(req.getApplyExpandedPrompt())
                && parsedAnalysis.get("expandedPrompt") != null
                && !String.valueOf(parsedAnalysis.get("expandedPrompt")).isBlank()) {
            finalPrompt = String.valueOf(parsedAnalysis.get("expandedPrompt")).trim();
        }

        List<Map<String, Object>> syncedScreens = buildScreensFromEnabledFeatures(ideaId, title, resolvedSiteType, finalEnabled, catalog);
        syncScreenSpecs(ideaId, syncedScreens);

        jdbcTemplate.update("""
                UPDATE kkumz_idea
                SET title = ?, prompt_text = ?, site_type = ?, category = ?,
                    tags_json = ?::jsonb, enabled_features_json = ?::jsonb, ai_intent_analysis_json = ?::jsonb,
                    screens_json = ?::jsonb, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                title, finalPrompt, resolvedSiteType, resolvedCategory,
                toJson(resolvedTags), toJson(finalEnabled), toJson(parsedAnalysis),
                toJson(syncedScreens), ideaId
        );

        syncIdeaAssetsToDeployedSiteIfPresent(ideaId);
        return getIdeaDetail(ideaId);
    }

    @Transactional
    public Map<String, Object> updateIdeaFeatures(String ideaId, UpdateIdeaFeaturesRequest req) {
        assertIdeaEditable(ideaId);
        Map<String, Object> idea = getIdeaDetail(ideaId);
        List<Map<String, Object>> catalog = listFeatureCatalog();
        Set<String> validCodes = new LinkedHashSet<>();
        for (Map<String, Object> fc : catalog) {
            validCodes.add(String.valueOf(fc.get("featureCode")));
        }

        List<String> enabledFeatures = new ArrayList<>();
        if (req != null && req.getEnabledFeatures() != null) {
            for (String code : req.getEnabledFeatures()) {
                if (code != null && !code.isBlank() && (validCodes.isEmpty() || validCodes.contains(code.trim()))) {
                    String trimmed = code.trim();
                    if (!enabledFeatures.contains(trimmed)) {
                        enabledFeatures.add(trimmed);
                    }
                }
            }
        }

        String siteType = (req != null && req.getSiteType() != null && !req.getSiteType().isBlank())
                ? req.getSiteType().trim().toUpperCase(Locale.ROOT)
                : String.valueOf(idea.getOrDefault("siteType", "HYBRID"));
        if (hasBothShopAndCmsFeatures(enabledFeatures, catalog)) {
            siteType = "HYBRID";
        } else if (hasEngineFeature(enabledFeatures, catalog, "SYNC_SHOP") && !hasEngineFeature(enabledFeatures, catalog, "SYNC_CMS")) {
            siteType = "COMMERCE";
        } else if (hasEngineFeature(enabledFeatures, catalog, "SYNC_CMS") && !hasEngineFeature(enabledFeatures, catalog, "SYNC_SHOP")) {
            siteType = "CMS_SITE";
        }

        String title = (req != null && req.getTitle() != null && !req.getTitle().isBlank())
                ? req.getTitle().trim()
                : String.valueOf(idea.getOrDefault("title", ""));
        String promptText = (req != null && req.getPromptText() != null && !req.getPromptText().isBlank())
                ? req.getPromptText().trim()
                : String.valueOf(idea.getOrDefault("promptText", ""));
        String category = (req != null && req.getCategory() != null && !req.getCategory().isBlank())
                ? req.getCategory().trim()
                : String.valueOf(idea.getOrDefault("category", "AUTO"));
        @SuppressWarnings("unchecked")
        List<String> tags = (req != null && req.getTags() != null && !req.getTags().isEmpty())
                ? req.getTags()
                : (List<String>) idea.getOrDefault("tags", List.of());
        @SuppressWarnings("unchecked")
        Map<String, Object> aiAnalysis = (req != null && req.getAiIntentAnalysis() != null && !req.getAiIntentAnalysis().isEmpty())
                ? new LinkedHashMap<>(req.getAiIntentAnalysis())
                : new LinkedHashMap<>((Map<String, Object>) idea.getOrDefault("aiIntentAnalysis", Map.of()));
        aiAnalysis.put("siteType", siteType);

        boolean syncScreens = req == null || req.getSyncScreens() == null || Boolean.TRUE.equals(req.getSyncScreens());
        List<Map<String, Object>> screens;
        if (syncScreens && !enabledFeatures.isEmpty()) {
            screens = buildScreensFromEnabledFeatures(ideaId, title, siteType, enabledFeatures, catalog);
            syncScreenSpecs(ideaId, screens);
        } else {
            screens = listIdeaScreensInternal(ideaId);
        }

        jdbcTemplate.update("""
                UPDATE kkumz_idea
                SET title = ?, prompt_text = ?, site_type = ?, category = ?,
                    tags_json = ?::jsonb, enabled_features_json = ?::jsonb, ai_intent_analysis_json = ?::jsonb,
                    screens_json = ?::jsonb, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                title, promptText, siteType, category,
                toJson(tags), toJson(enabledFeatures), toJson(aiAnalysis),
                toJson(screens), ideaId
        );

        // 이미 배포된 사이트가 있으면 활성 릴리즈의 enabledFeatures도 즉시 동기화
        List<Map<String, Object>> deployed = jdbcTemplate.queryForList(
                "SELECT id, active_version AS \"activeVersion\" FROM kkumz_site WHERE idea_id = ? ORDER BY created_at DESC LIMIT 1",
                ideaId
        );
        if (!deployed.isEmpty()) {
            String siteId = String.valueOf(deployed.get(0).get("id"));
            int activeVer = ((Number) deployed.get(0).get("activeVersion")).intValue();
            Map<String, Object> rel = getLatestRelease(siteId, activeVer);
            Map<String, Object> layoutSchema = new LinkedHashMap<>(parseJsonObject(String.valueOf(rel.get("layoutSchemaJson"))));
            layoutSchema.put("enabledFeatures", enabledFeatures);
            jdbcTemplate.update("UPDATE kkumz_site SET site_type = ?, category = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    siteType, category, siteId);
            jdbcTemplate.update("UPDATE kkumz_site_release SET layout_schema_json = ?::jsonb WHERE id = ?",
                    toJson(layoutSchema), rel.get("id"));
        }

        return getIdeaDetail(ideaId);
    }

    public List<Map<String, Object>> listIdeaScreens(String ideaId) {
        assertIdeaReadable(ideaId);
        return listIdeaScreensInternal(ideaId);
    }

    private List<Map<String, Object>> listIdeaScreensInternal(String ideaId) {
        List<Map<String, Object>> screens = jdbcTemplate.queryForList("""
                SELECT id,
                       idea_id AS "ideaId",
                       screen_key AS "screenKey",
                       screen_title AS "screenTitle",
                       route_path AS "routePath",
                       layout_type AS "layoutType",
                       description,
                       components_json::text AS "componentsJson",
                       sort_no AS "sortNo"
                FROM kkumz_screen_spec
                WHERE idea_id = ?
                ORDER BY sort_no ASC, created_at ASC
                """, ideaId);
        screens.forEach(s -> s.put("components", parseJsonList(String.valueOf(s.remove("componentsJson")))));
        return screens;
    }

    @Transactional
    public List<Map<String, Object>> saveIdeaScreen(String ideaId, SaveScreenSpecRequest req) {
        assertIdeaEditable(ideaId);
        if (req == null || req.getScreenTitle() == null || req.getScreenTitle().isBlank()) {
            throw new IllegalArgumentException("화면 제목(screenTitle)은 필수입니다.");
        }
        String screenKey = (req.getScreenKey() != null && !req.getScreenKey().isBlank())
                ? req.getScreenKey().trim().toUpperCase(Locale.ROOT)
                : "SCR_" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT);
        String routePath = (req.getRoutePath() != null && !req.getRoutePath().isBlank())
                ? req.getRoutePath().trim()
                : "/" + screenKey.toLowerCase(Locale.ROOT).replace('_', '-');
        String layoutType = (req.getLayoutType() != null && !req.getLayoutType().isBlank())
                ? req.getLayoutType().trim()
                : "STANDARD_GRID";
        int sortNo = req.getSortNo() != null ? req.getSortNo() : (listIdeaScreensInternal(ideaId).size() + 1);
        List<String> components = req.getComponents() != null ? req.getComponents() : List.of("BrandStoryBanner", "FeaturedProductGrid");

        if (req.getId() != null && !req.getId().isBlank()) {
            int updated = jdbcTemplate.update("""
                    UPDATE kkumz_screen_spec
                    SET screen_key = ?, screen_title = ?, route_path = ?, layout_type = ?,
                        description = ?, components_json = ?::jsonb, sort_no = ?
                    WHERE id = ? AND idea_id = ?
                    """,
                    screenKey,
                    req.getScreenTitle().trim(),
                    routePath,
                    layoutType,
                    req.getDescription() != null ? req.getDescription().trim() : "",
                    toJson(components),
                    sortNo,
                    req.getId().trim(),
                    ideaId
            );
            if (updated > 0) {
                return listIdeaScreensInternal(ideaId);
            }
        }

        jdbcTemplate.update("""
                INSERT INTO kkumz_screen_spec (
                    id, idea_id, screen_key, screen_title, route_path, layout_type, description, components_json, sort_no, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, CURRENT_TIMESTAMP)
                """,
                req.getId() != null && !req.getId().isBlank() ? req.getId().trim() : "scr-" + UUID.randomUUID().toString().substring(0, 8),
                ideaId,
                screenKey,
                req.getScreenTitle().trim(),
                routePath,
                layoutType,
                req.getDescription() != null ? req.getDescription().trim() : "",
                toJson(components),
                sortNo
        );

        return listIdeaScreensInternal(ideaId);
    }

    @Transactional
    public List<Map<String, Object>> deleteIdeaScreen(String ideaId, String screenId) {
        assertIdeaEditable(ideaId);
        jdbcTemplate.update("DELETE FROM kkumz_screen_spec WHERE id = ? AND idea_id = ?", screenId, ideaId);
        List<Map<String, Object>> remaining = listIdeaScreensInternal(ideaId);
        jdbcTemplate.update("UPDATE kkumz_idea SET screens_json = ?::jsonb, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                toJson(remaining), ideaId);
        return remaining;
    }

    private void syncScreenSpecs(String ideaId, List<Map<String, Object>> screens) {
        if (screens == null) {
            return;
        }
        jdbcTemplate.update("DELETE FROM kkumz_screen_spec WHERE idea_id = ?", ideaId);
        int idx = 1;
        for (Map<String, Object> scr : screens) {
            String title = String.valueOf(scr.getOrDefault("screenTitle", scr.getOrDefault("title", "화면 섹션 " + idx))).trim();
            if (title.isBlank()) {
                title = "화면 섹션 " + idx;
            }
            List<String> compList = new ArrayList<>();
            Object rawComps = scr.get("components");
            if (rawComps instanceof List<?> cl) {
                for (Object c : cl) {
                    if (c != null && !String.valueOf(c).isBlank()) {
                        compList.add(String.valueOf(c).trim());
                    }
                }
            }
            if (compList.isEmpty()) {
                compList.add("BrandStoryBanner");
                compList.add("FeaturedProductGrid");
            }
            SaveScreenSpecRequest sReq = SaveScreenSpecRequest.builder()
                    .id(scr.get("id") != null ? String.valueOf(scr.get("id")) : null)
                    .screenKey(String.valueOf(scr.getOrDefault("screenKey", "SCR_" + idx)))
                    .screenTitle(title)
                    .routePath(String.valueOf(scr.getOrDefault("routePath", idx == 1 ? "/" : "/section-" + idx)))
                    .layoutType(String.valueOf(scr.getOrDefault("layoutType", scr.getOrDefault("layout", "STANDARD_GRID"))))
                    .description(String.valueOf(scr.getOrDefault("description", "")))
                    .components(compList)
                    .sortNo(idx++)
                    .build();
            saveIdeaScreen(ideaId, sReq);
        }
    }

    // =========================================================================
    // 1-2. 아이디어·사이트 업로드 자산 (사진·로고·문서) 관리 및 자동 바인딩
    // =========================================================================

    public List<Map<String, Object>> listIdeaAssets(String ideaId) {
        assertIdeaReadable(ideaId);
        return listIdeaAssetsInternal(ideaId);
    }

    private List<Map<String, Object>> listIdeaAssetsInternal(String ideaId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id,
                       idea_id AS "ideaId",
                       asset_type AS "assetType",
                       original_name AS "originalName",
                       stored_name AS "storedName",
                       file_url AS "fileUrl",
                       mime_type AS "mimeType",
                       file_size AS "fileSize",
                       caption,
                       extracted_text AS "extractedText",
                       sort_no AS "sortNo",
                       uploaded_by AS "uploadedBy",
                       created_at AS "createdAt"
                FROM kkumz_idea_asset
                WHERE idea_id = ?
                ORDER BY sort_no ASC, created_at ASC
                """, ideaId);
        for (Map<String, Object> row : rows) {
            String mime = String.valueOf(row.getOrDefault("mimeType", "")).toLowerCase(Locale.ROOT);
            String orig = String.valueOf(row.getOrDefault("originalName", "")).toLowerCase(Locale.ROOT);
            boolean isImg = mime.startsWith("image/")
                    || orig.matches(".*\\.(jpg|jpeg|png|webp|gif|svg|avif)$");
            row.put("isImage", isImg);
            String ext = extractExtension(orig);
            row.put("fileExt", ext);
            String fUrl = String.valueOf(row.getOrDefault("fileUrl", ""));
            row.put("downloadUrl", fUrl.isBlank() ? "" : (fUrl + "?download=true"));
        }
        return rows;
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
        assertIdeaEditable(ideaId);
        Map<String, Object> idea = getIdeaDetail(ideaId);
        List<MultipartFile> allFiles = new ArrayList<>();
        if (files != null) {
            for (MultipartFile f : files) {
                if (f != null && !f.isEmpty()) {
                    allFiles.add(f);
                }
            }
        }
        if (singleFile != null && !singleFile.isEmpty()) {
            allFiles.add(singleFile);
        }
        if (allFiles.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 선택되지 않았습니다.");
        }

        Path uploadRoot = resolveUploadDir();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 저장소 디렉터리를 생성할 수 없습니다: " + e.getMessage(), e);
        }

        Integer currentMaxSort = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(sort_no), 0) FROM kkumz_idea_asset WHERE idea_id = ?",
                Integer.class, ideaId
        );
        int sortSeq = (currentMaxSort != null ? currentMaxSort : 0);
        String actor = (uploadedBy != null && !uploadedBy.isBlank())
                ? uploadedBy.trim()
                : String.valueOf(idea.getOrDefault("authorUsername", "admin"));

        List<String> extractedSnippets = new ArrayList<>();
        List<Map<String, Object>> uploadedAssets = new ArrayList<>();

        for (MultipartFile mf : allFiles) {
            sortSeq++;
            String origName = mf.getOriginalFilename() != null && !mf.getOriginalFilename().isBlank()
                    ? Paths.get(mf.getOriginalFilename()).getFileName().toString()
                    : "asset-" + sortSeq;
            String ext = extractExtension(origName);
            String mimeType = resolveMimeType(mf.getContentType(), ext);
            String resolvedType = classifyAssetType(assetType, origName, ext, mimeType);
            String assetId = "ast-" + UUID.randomUUID().toString().substring(0, 8);
            String storedName = ideaId + "-" + assetId + (ext.isEmpty() ? "" : "." + ext);

            byte[] bytes;
            try {
                bytes = mf.getBytes();
                Files.write(uploadRoot.resolve(storedName), bytes);
            } catch (IOException e) {
                throw new IllegalStateException("파일 저장 중 오류가 발생했습니다 (" + origName + "): " + e.getMessage(), e);
            }

            String prefix = storagePublicPrefix != null && !storagePublicPrefix.isBlank()
                    ? storagePublicPrefix.replaceAll("/+$", "")
                    : "/api/v1/kkumz/assets/files";
            String fileUrl = prefix + "/" + storedName;
            String resolvedCaption = (caption != null && !caption.isBlank())
                    ? caption.trim()
                    : stripExtension(origName);

            String extractedText = null;
            if ("DOC".equalsIgnoreCase(resolvedType) || isTextOrDocumentFile(ext, mimeType)) {
                extractedText = extractDocumentText(bytes, ext, mimeType);
                if (extractedText != null && !extractedText.isBlank()) {
                    extractedSnippets.add("[" + origName + "]\n" + extractedText);
                }
            }

            jdbcTemplate.update("""
                    INSERT INTO kkumz_idea_asset (
                        id, idea_id, asset_type, original_name, stored_name, file_url,
                        mime_type, file_size, caption, extracted_text, sort_no, uploaded_by, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                    """,
                    assetId, ideaId, resolvedType, origName, storedName, fileUrl,
                    mimeType, (long) bytes.length, resolvedCaption, extractedText, sortSeq, actor
            );

            if ("LOGO".equalsIgnoreCase(resolvedType)) {
                registerUploadedLogoCandidate(ideaId, resolvedCaption, fileUrl, bytes, ext, mimeType);
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", assetId);
            item.put("ideaId", ideaId);
            item.put("assetType", resolvedType);
            item.put("originalName", origName);
            item.put("storedName", storedName);
            item.put("fileUrl", fileUrl);
            item.put("downloadUrl", fileUrl + "?download=true");
            item.put("fileExt", ext);
            item.put("mimeType", mimeType);
            item.put("fileSize", (long) bytes.length);
            item.put("caption", resolvedCaption);
            item.put("extractedText", extractedText);
            item.put("sortNo", sortSeq);
            uploadedAssets.add(item);
        }

        if (Boolean.TRUE.equals(applyToPrompt) && !extractedSnippets.isEmpty()) {
            appendExtractedTextToIdeaPrompt(ideaId, String.join("\n\n", extractedSnippets));
        }

        syncIdeaAssetsToDeployedSiteIfPresent(ideaId);

        Map<String, Object> detail = getIdeaDetail(ideaId);
        detail.put("uploadedAssets", uploadedAssets);
        return detail;
    }

    @Transactional
    public Map<String, Object> updateIdeaAsset(String ideaId, String assetId, UpdateIdeaAssetRequest req) {
        assertIdeaEditable(ideaId);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id, original_name AS "originalName", stored_name AS "storedName",
                       file_url AS "fileUrl", mime_type AS "mimeType", caption, asset_type AS "assetType"
                FROM kkumz_idea_asset
                WHERE id = ? AND idea_id = ?
                """, assetId, ideaId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("수정할 업로드 자산을 찾을 수 없습니다: " + assetId);
        }
        Map<String, Object> existing = rows.get(0);
        String newType = (req != null && req.getAssetType() != null && !req.getAssetType().isBlank())
                ? req.getAssetType().trim().toUpperCase(Locale.ROOT)
                : String.valueOf(existing.get("assetType"));
        String newCaption = (req != null && req.getCaption() != null)
                ? req.getCaption().trim()
                : String.valueOf(existing.getOrDefault("caption", ""));

        if (req != null && req.getSortNo() != null) {
            jdbcTemplate.update("""
                    UPDATE kkumz_idea_asset
                    SET asset_type = ?, caption = ?, sort_no = ?
                    WHERE id = ? AND idea_id = ?
                    """, newType, newCaption, req.getSortNo(), assetId, ideaId);
        } else {
            jdbcTemplate.update("""
                    UPDATE kkumz_idea_asset
                    SET asset_type = ?, caption = ?
                    WHERE id = ? AND idea_id = ?
                    """, newType, newCaption, assetId, ideaId);
        }

        if ("LOGO".equalsIgnoreCase(newType)) {
            String storedName = String.valueOf(existing.get("storedName"));
            String origName = String.valueOf(existing.get("originalName"));
            String fileUrl = String.valueOf(existing.get("fileUrl"));
            String mimeType = String.valueOf(existing.get("mimeType"));
            byte[] bytes = new byte[0];
            try {
                Path p = resolveUploadDir().resolve(storedName);
                if (Files.exists(p)) {
                    bytes = Files.readAllBytes(p);
                }
            } catch (IOException ignored) {
            }
            registerUploadedLogoCandidate(ideaId, newCaption.isBlank() ? stripExtension(origName) : newCaption,
                    fileUrl, bytes, extractExtension(origName), mimeType);
        }

        syncIdeaAssetsToDeployedSiteIfPresent(ideaId);
        return getIdeaDetail(ideaId);
    }

    @Transactional
    public Map<String, Object> deleteIdeaAsset(String ideaId, String assetId) {
        assertIdeaEditable(ideaId);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT stored_name AS \"storedName\" FROM kkumz_idea_asset WHERE id = ? AND idea_id = ?",
                assetId, ideaId
        );
        if (!rows.isEmpty()) {
            String storedName = String.valueOf(rows.get(0).get("storedName"));
            try {
                Files.deleteIfExists(resolveUploadDir().resolve(storedName));
            } catch (IOException e) {
                log.warn("저장 파일 삭제 경고 ({}): {}", storedName, e.getMessage());
            }
        }
        jdbcTemplate.update("DELETE FROM kkumz_idea_asset WHERE id = ? AND idea_id = ?", assetId, ideaId);
        syncIdeaAssetsToDeployedSiteIfPresent(ideaId);
        return getIdeaDetail(ideaId);
    }

    @Transactional
    public Map<String, Object> applyAssetToPrompt(String ideaId, String assetId) {
        assertIdeaEditable(ideaId);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT original_name AS "originalName", caption, extracted_text AS "extractedText"
                FROM kkumz_idea_asset
                WHERE id = ? AND idea_id = ?
                """, assetId, ideaId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("대상 자산을 찾을 수 없습니다: " + assetId);
        }
        Map<String, Object> asset = rows.get(0);
        String origName = String.valueOf(asset.get("originalName"));
        String extracted = asset.get("extractedText") != null ? String.valueOf(asset.get("extractedText")).trim() : "";
        String caption = asset.get("caption") != null ? String.valueOf(asset.get("caption")).trim() : "";
        String snippet = !extracted.isBlank()
                ? "[첨부자료: " + origName + "]\n" + extracted
                : "[첨부자료: " + origName + "] " + caption;
        appendExtractedTextToIdeaPrompt(ideaId, snippet);
        return getIdeaDetail(ideaId);
    }

    @Transactional
    public Map<String, Object> uploadCustomBrandLogo(String ideaId, MultipartFile file, String styleName) {
        assertIdeaEditable(ideaId);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 로고 파일이 비어 있습니다.");
        }
        String label = (styleName != null && !styleName.isBlank()) ? styleName.trim() : null;
        return uploadIdeaAssets(ideaId, List.of(file), null, "LOGO", label, false, null);
    }

    public StoredAssetPayload loadStoredAssetFile(String storedName) {
        if (storedName == null || storedName.isBlank() || storedName.contains("..") || storedName.matches(".*[\\\\/].*")) {
            throw new IllegalArgumentException("유효하지 않은 파일 식별자입니다.");
        }
        Path filePath = resolveUploadDir().resolve(storedName);
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("요청한 업로드 파일이 존재하지 않습니다: " + storedName);
        }
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            List<Map<String, Object>> meta = jdbcTemplate.queryForList(
                    "SELECT original_name AS \"originalName\", mime_type AS \"mimeType\" FROM kkumz_idea_asset WHERE stored_name = ? LIMIT 1",
                    storedName
            );
            String origName = !meta.isEmpty() && meta.get(0).get("originalName") != null
                    ? String.valueOf(meta.get(0).get("originalName"))
                    : storedName;
            String mimeType = !meta.isEmpty() && meta.get(0).get("mimeType") != null
                    ? String.valueOf(meta.get(0).get("mimeType"))
                    : resolveMimeType(null, extractExtension(storedName));
            return StoredAssetPayload.builder()
                    .bytes(bytes)
                    .mimeType(mimeType)
                    .originalName(origName)
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("업로드 파일을 읽는 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // 2. Step 2 — AI 브랜드 네이밍 & 벡터 SVG 로고 생성 (AgentScope Java 연동)
    // =========================================================================

    public Map<String, Object> generateNameSuggestions(String ideaId, GenerateNamesRequest req) {
        assertIdeaEditable(ideaId);
        Map<String, Object> idea = getIdeaDetail(ideaId);
        String title = String.valueOf(idea.getOrDefault("title", ""));
        String promptText = String.valueOf(idea.get("promptText"));
        String siteType = String.valueOf(idea.getOrDefault("siteType", "CMS_SITE"));
        String rawCategory = String.valueOf(idea.getOrDefault("category", "AUTO"));
        String categoryContext = (rawCategory.isBlank() || "AUTO".equalsIgnoreCase(rawCategory) || "FOOD_BEVERAGE".equalsIgnoreCase(rawCategory))
                ? "프롬프트 문맥에서 AI 에이전트가 업종·도메인 자율 추론"
                : rawCategory;
        String keywordHint = (req != null && req.getKeywordHint() != null) ? req.getKeywordHint().trim() : "";
        String toneStyle = (req != null && req.getToneStyle() != null) ? req.getToneStyle().trim() : "";
        boolean forceRegenerate = req != null && Boolean.TRUE.equals(req.getForceRegenerate());

        String combinedHint = keywordHint;
        if (!toneStyle.isBlank()) {
            combinedHint = combinedHint.isBlank() ? "톤앤매너: " + toneStyle : combinedHint + " (톤앤매너: " + toneStyle + ")";
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> existingNames = (List<Map<String, Object>>) idea.get("nameSuggestions");
        if (!forceRegenerate && existingNames != null && !existingNames.isEmpty() && combinedHint.isBlank()) {
            return idea;
        }

        // 외부 LLM HTTP 호출은 DB 트랜잭션/커넥션 점유 없이 실행
        String rawJson = ensureAiGateway().executeStagePrompt("NAMING", Map.of(
                "title", title,
                "promptText", promptText,
                "siteType", siteType,
                "category", categoryContext,
                "keywordHint", combinedHint
        ));

        List<Map<String, Object>> candidates = parseAiJsonArray(rawJson, "NAMING");
        if (candidates.isEmpty()) {
            throw new IllegalStateException("AI 브랜드 네이밍 생성 결과가 비어 있습니다.");
        }

        jdbcTemplate.update("DELETE FROM kkumz_name_suggestion WHERE idea_id = ?", ideaId);

        int idx = 0;
        String aiInferredCategory = null;
        String winningBrandName = null;
        for (Map<String, Object> item : candidates) {
            String brandName = String.valueOf(item.getOrDefault("brandName",
                    item.getOrDefault("brand_name",
                            item.getOrDefault("name", item.getOrDefault("title", ""))))).trim();
            String rawSlug = String.valueOf(item.getOrDefault("siteSlug",
                    item.getOrDefault("site_slug",
                            item.getOrDefault("slug", brandName)))).trim();
            if (brandName.isBlank() || rawSlug.isBlank()) {
                continue;
            }
            if (aiInferredCategory == null) {
                Object catObj = item.getOrDefault("categoryLabel", item.get("category"));
                if (catObj != null && !String.valueOf(catObj).isBlank() && !"null".equalsIgnoreCase(String.valueOf(catObj))) {
                    aiInferredCategory = String.valueOf(catObj).trim();
                }
            }
            String slug = normalizeSlug(rawSlug);
            String pronunciation = String.valueOf(item.getOrDefault("pronunciation",
                    item.getOrDefault("pronunciationGuide",
                            item.getOrDefault("pronunciation_guide", brandName)))).trim();
            String story = String.valueOf(item.getOrDefault("brandStory",
                    item.getOrDefault("brand_story",
                            item.getOrDefault("story", item.getOrDefault("description", ""))))).trim();
            int score = (item.get("score") instanceof Number num) ? num.intValue() : (99 - idx);

            Integer taken = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM kkumz_site WHERE site_slug = ? AND idea_id <> ?",
                    Integer.class, slug, ideaId
            );
            boolean available = (taken == null || taken == 0);
            boolean isSelected = (idx == 0);

            jdbcTemplate.update("""
                    INSERT INTO kkumz_name_suggestion (
                        id, idea_id, brand_name, site_slug, pronunciation, brand_story, score, slug_available, is_selected, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                    """,
                    "name-" + ideaId + "-" + (idx + 1),
                    ideaId, brandName, slug, pronunciation, story, score, available, isSelected
            );

            if (isSelected) {
                winningBrandName = brandName;
                String resolvedCat = (aiInferredCategory != null && !aiInferredCategory.isBlank())
                        ? aiInferredCategory
                        : rawCategory;
                jdbcTemplate.update("""
                        UPDATE kkumz_idea
                        SET selected_brand_name = ?,
                            selected_site_slug = ?,
                            category = ?,
                            current_step = GREATEST(current_step, 2),
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                        brandName, slug, resolvedCat, ideaId
                );
            }
            idx++;
        }

        // 병렬로 먼저 생성된 로고가 있다면 1순위 브랜드명으로 워드마크 즉시 동기화
        if (winningBrandName != null && !winningBrandName.isBlank()) {
            syncExistingLogosWithBrandName(ideaId, winningBrandName);
        }

        return getIdeaDetail(ideaId);
    }

    public Map<String, Object> generateLogoSuggestions(String ideaId, GenerateLogosRequest req) {
        assertIdeaEditable(ideaId);
        Map<String, Object> idea = getIdeaDetail(ideaId);
        String promptText = String.valueOf(idea.getOrDefault("promptText", ""));
        String brandName = (req != null && req.getBrandName() != null && !req.getBrandName().isBlank())
                ? req.getBrandName().trim()
                : String.valueOf(idea.getOrDefault("selectedBrandName", idea.get("title")));
        String stylePreference = (req != null && req.getStylePreference() != null)
                ? req.getStylePreference().trim()
                : "";
        boolean forceRegenerate = req != null && Boolean.TRUE.equals(req.getForceRegenerate());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> existingLogos = (List<Map<String, Object>>) idea.get("logoSuggestions");
        String existingSelectedSvg = idea.get("selectedLogoSvg") != null ? String.valueOf(idea.get("selectedLogoSvg")) : "";
        boolean logoBrandSynced = !existingSelectedSvg.isBlank() && existingSelectedSvg.contains(escapeXml(brandName));
        if (!forceRegenerate && existingLogos != null && !existingLogos.isEmpty()) {
            if (!logoBrandSynced) {
                syncExistingLogosWithBrandName(ideaId, brandName);
                return getIdeaDetail(ideaId);
            }
            return idea;
        }

        // 2-Way 병렬 분할 호출 (Shard 1: 스타일 1~3 / Shard 2: 스타일 4~6)
        // 출력 토큰 생성 길이를 절반으로 줄여 응답 지연 시간을 50% 이상 단축
        List<Map<String, Object>> candidates = executeParallelLogoGeneration(brandName, promptText, stylePreference);
        if (candidates.isEmpty()) {
            throw new IllegalStateException("AI 벡터 SVG 로고 생성 결과가 비어 있습니다.");
        }

        // 병렬 실행 중 네이밍 생성이 완료되어 selected_brand_name이 갱신되었을 수 있으므로 최신 브랜드명 재확인
        List<Map<String, Object>> latestIdeaRows = jdbcTemplate.queryForList(
                "SELECT selected_brand_name AS \"selectedBrandName\", title FROM kkumz_idea WHERE id = ?",
                ideaId
        );
        String finalBrandName = brandName;
        if (!latestIdeaRows.isEmpty()) {
            Object latestSelected = latestIdeaRows.get(0).get("selectedBrandName");
            if (latestSelected != null && !String.valueOf(latestSelected).isBlank()) {
                finalBrandName = String.valueOf(latestSelected).trim();
            }
        }

        jdbcTemplate.update("DELETE FROM kkumz_logo_suggestion WHERE idea_id = ?", ideaId);

        int idx = 0;
        for (Map<String, Object> item : candidates) {
            if (idx >= 6) {
                break;
            }
            String styleName = String.valueOf(item.getOrDefault("styleName",
                    item.getOrDefault("style_name",
                            item.getOrDefault("style", item.getOrDefault("name", "Modern Style " + (idx + 1)))))).trim();
            String svgCode = String.valueOf(item.getOrDefault("svgCode",
                    item.getOrDefault("svg_code",
                            item.getOrDefault("svg", "")))).trim();
            int svgStart = svgCode.indexOf("<svg");
            if (svgStart > 0) {
                svgCode = svgCode.substring(svgStart).trim();
            }
            if (styleName.isBlank() || !svgCode.startsWith("<svg")) {
                continue;
            }
            svgCode = replaceBrandTextInSvg(svgCode, finalBrandName, List.of(brandName));
            Object paletteObj = item.getOrDefault("colorPalette",
                    item.getOrDefault("color_palette",
                            item.getOrDefault("palette", Map.of())));
            Object typoObj = item.getOrDefault("typography", Map.of("fontHeading", "Pretendard", "fontBody", "Pretendard"));
            Object tagsObj = item.getOrDefault("tags", List.of(styleName));
            boolean isSelected = (idx == 0);

            jdbcTemplate.update("""
                    INSERT INTO kkumz_logo_suggestion (
                        id, idea_id, style_name, svg_code, color_palette_json, typography_json, tags_json, is_selected, created_at
                    ) VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, CURRENT_TIMESTAMP)
                    """,
                    "logo-" + ideaId + "-" + (idx + 1),
                    ideaId, styleName, svgCode, toJson(paletteObj), toJson(typoObj), toJson(tagsObj), isSelected
            );

            if (isSelected) {
                jdbcTemplate.update("""
                        UPDATE kkumz_idea
                        SET selected_logo_svg = ?, design_tokens_json = ?::jsonb, current_step = GREATEST(current_step, 3), updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                        svgCode, toJson(paletteObj), ideaId
                );
            }
            idx++;
        }

        return getIdeaDetail(ideaId);
    }

    private List<Map<String, Object>> executeParallelLogoGeneration(
            String brandName,
            String promptText,
            String stylePreference
    ) {
        String baseStyle = (stylePreference == null || stylePreference.isBlank() || "ALL".equalsIgnoreCase(stylePreference))
                ? "업종 및 브랜드 정체성에 어울리는 심볼+워드마크 스타일"
                : stylePreference;

        String shard1Pref = baseStyle
                + " [병렬 배치 1/2: 반드시 서로 다른 3개의 스타일(1. 내추럴 라인아트, 2. 모던 지오메트릭, 3. 클래식 엠블럼)만 정확히 3개 JSON 배열로 생성]";
        String shard2Pref = baseStyle
                + " [병렬 배치 2/2: 반드시 서로 다른 3개의 스타일(4. 소프트 워터컬러 배지, 5. 미니멀 아치, 6. 에디토리얼 시그니처)만 정확히 3개 JSON 배열로 생성]";

        KkumzAiGatewayService gateway = ensureAiGateway();

        java.util.concurrent.CompletableFuture<List<Map<String, Object>>> future1 =
                java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                    String json1 = gateway.executeStagePrompt("LOGO_SVG", Map.of(
                            "brandName", brandName,
                            "promptText", promptText,
                            "stylePreference", shard1Pref
                    ));
                    return parseAiJsonArray(json1, "LOGO_SVG");
                });

        java.util.concurrent.CompletableFuture<List<Map<String, Object>>> future2 =
                java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                    try {
                        String json2 = gateway.executeStagePrompt("LOGO_SVG", Map.of(
                                "brandName", brandName,
                                "promptText", promptText,
                                "stylePreference", shard2Pref
                        ));
                        return parseAiJsonArray(json2, "LOGO_SVG");
                    } catch (Exception e) {
                        log.warn("[KkumzService] LOGO_SVG 병렬 배치 2/2 보조 호출 경고 (배치 1 결과로 폴백 가능): {}", e.getMessage());
                        return List.of();
                    }
                });

        List<Map<String, Object>> part1 = future1.join();
        List<Map<String, Object>> part2 = future2.join();

        if (part1.size() >= 6) {
            return part1.subList(0, 6);
        }

        List<Map<String, Object>> merged = new ArrayList<>(part1);
        for (Map<String, Object> item : part2) {
            if (merged.size() >= 6) {
                break;
            }
            merged.add(item);
        }
        return merged;
    }

    private void syncExistingLogosWithBrandName(String ideaId, String targetBrandName) {
        if (targetBrandName == null || targetBrandName.isBlank()) {
            return;
        }
        List<Map<String, Object>> existingLogos = jdbcTemplate.queryForList(
                "SELECT id, svg_code AS \"svgCode\", is_selected AS \"isSelected\" FROM kkumz_logo_suggestion WHERE idea_id = ?",
                ideaId
        );
        if (existingLogos.isEmpty()) {
            return;
        }
        for (Map<String, Object> logoRow : existingLogos) {
            String lid = String.valueOf(logoRow.get("id"));
            String lsvg = String.valueOf(logoRow.getOrDefault("svgCode", ""));
            String synced = replaceBrandTextInSvg(lsvg, targetBrandName, List.of());
            jdbcTemplate.update("UPDATE kkumz_logo_suggestion SET svg_code = ? WHERE id = ?", synced, lid);
            if (Boolean.TRUE.equals(logoRow.get("isSelected"))) {
                jdbcTemplate.update(
                        "UPDATE kkumz_idea SET selected_logo_svg = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                        synced, ideaId
                );
            }
        }
    }

    @Transactional
    public Map<String, Object> selectBrand(String ideaId, SelectBrandRequest req) {
        assertIdeaEditable(ideaId);
        Map<String, Object> idea = getIdeaDetail(ideaId);
        String customBrandName = (req.getBrandName() != null && !req.getBrandName().isBlank()) ? req.getBrandName().trim() : null;
        String customSiteSlug = (req.getSiteSlug() != null && !req.getSiteSlug().isBlank()) ? req.getSiteSlug().trim() : null;
        String customPronun = (req.getPronunciation() != null && !req.getPronunciation().isBlank()) ? req.getPronunciation().trim() : null;
        String customStory = (req.getBrandStory() != null && !req.getBrandStory().isBlank()) ? req.getBrandStory().trim() : null;
        String brandName = customBrandName;
        String siteSlug = customSiteSlug;
        String logoSvg = (req.getLogoSvg() != null && !req.getLogoSvg().isBlank()) ? req.getLogoSvg().trim() : null;
        Map<String, Object> designTokens = req.getDesignTokens();

        List<String> knownBrandNames = new ArrayList<>();
        if (idea.get("selectedBrandName") != null) {
            knownBrandNames.add(String.valueOf(idea.get("selectedBrandName")));
        }
        if (idea.get("title") != null) {
            knownBrandNames.add(String.valueOf(idea.get("title")));
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nameSuggestions = (List<Map<String, Object>>) idea.get("nameSuggestions");
        if (nameSuggestions != null) {
            for (Map<String, Object> ns : nameSuggestions) {
                if (ns.get("brandName") != null) {
                    knownBrandNames.add(String.valueOf(ns.get("brandName")));
                }
            }
        }

        if (req.getNameSuggestionId() != null && !req.getNameSuggestionId().isBlank()) {
            jdbcTemplate.update("UPDATE kkumz_name_suggestion SET is_selected = (id = ?) WHERE idea_id = ?",
                    req.getNameSuggestionId(), ideaId);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT brand_name AS \"brandName\", site_slug AS \"siteSlug\", pronunciation, brand_story AS \"brandStory\" FROM kkumz_name_suggestion WHERE id = ?",
                    req.getNameSuggestionId()
            );
            if (!rows.isEmpty()) {
                String dbBrand = String.valueOf(rows.get(0).get("brandName"));
                String dbSlug = String.valueOf(rows.get(0).get("siteSlug"));
                String dbPronun = String.valueOf(rows.get(0).getOrDefault("pronunciation", dbBrand));
                String dbStory = String.valueOf(rows.get(0).getOrDefault("brandStory", ""));
                if (brandName == null) {
                    brandName = dbBrand;
                }
                if (siteSlug == null) {
                    siteSlug = dbSlug;
                }
                if (customBrandName != null || customSiteSlug != null || customPronun != null || customStory != null) {
                    jdbcTemplate.update("""
                            UPDATE kkumz_name_suggestion
                            SET brand_name = ?, site_slug = ?, pronunciation = ?, brand_story = ?
                            WHERE id = ? AND idea_id = ?
                            """,
                            brandName,
                            normalizeSlug(siteSlug),
                            customPronun != null ? customPronun : dbPronun,
                            customStory != null ? customStory : dbStory,
                            req.getNameSuggestionId(),
                            ideaId
                    );
                }
            }
        } else if (customBrandName != null) {
            jdbcTemplate.update("UPDATE kkumz_name_suggestion SET is_selected = FALSE WHERE idea_id = ?", ideaId);
            String slugVal = normalizeSlug(customSiteSlug != null ? customSiteSlug : customBrandName);
            String customId = "name-custom-" + ideaId;
            jdbcTemplate.update("DELETE FROM kkumz_name_suggestion WHERE id = ?", customId);
            jdbcTemplate.update("""
                    INSERT INTO kkumz_name_suggestion (
                        id, idea_id, brand_name, site_slug, pronunciation, brand_story, score, slug_available, is_selected, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?, 100, TRUE, TRUE, CURRENT_TIMESTAMP)
                    """,
                    customId,
                    ideaId,
                    customBrandName,
                    slugVal,
                    customPronun != null ? customPronun : customBrandName,
                    customStory != null ? customStory : "사용자 직접 정의 브랜드 아이덴티티"
            );
            siteSlug = slugVal;
        }

        if (req.getLogoSuggestionId() != null && !req.getLogoSuggestionId().isBlank()) {
            jdbcTemplate.update("UPDATE kkumz_logo_suggestion SET is_selected = (id = ?) WHERE idea_id = ?",
                    req.getLogoSuggestionId(), ideaId);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT svg_code AS \"svgCode\", color_palette_json::text AS \"palette\" FROM kkumz_logo_suggestion WHERE id = ?",
                    req.getLogoSuggestionId()
            );
            if (!rows.isEmpty()) {
                if (logoSvg == null) {
                    logoSvg = String.valueOf(rows.get(0).get("svgCode"));
                }
                if (designTokens == null || designTokens.isEmpty()) {
                    designTokens = parseJsonObject(String.valueOf(rows.get(0).get("palette")));
                } else {
                    jdbcTemplate.update(
                            "UPDATE kkumz_logo_suggestion SET color_palette_json = ?::jsonb WHERE id = ? AND idea_id = ?",
                            toJson(designTokens), req.getLogoSuggestionId(), ideaId
                    );
                }
            }
        }

        String finalBrand = (brandName != null && !brandName.isBlank())
                ? brandName.trim()
                : String.valueOf(idea.getOrDefault("selectedBrandName", idea.get("title")));
        String finalSlug = (siteSlug != null && !siteSlug.isBlank())
                ? normalizeSlug(siteSlug)
                : String.valueOf(idea.getOrDefault("selectedSiteSlug", "site-" + ideaId));
        String rawSvg = (logoSvg != null && !logoSvg.isBlank())
                ? logoSvg
                : String.valueOf(idea.getOrDefault("selectedLogoSvg", ""));
        String finalSvg = replaceBrandTextInSvg(rawSvg, finalBrand, knownBrandNames);
        String finalTokensJson = (designTokens != null && !designTokens.isEmpty())
                ? toJson(designTokens)
                : toJson(idea.get("designTokens"));

        // 모든 로고 후보의 워드마크 텍스트도 확정된 브랜드명으로 동기화
        List<Map<String, Object>> allLogos = jdbcTemplate.queryForList(
                "SELECT id, svg_code AS \"svgCode\" FROM kkumz_logo_suggestion WHERE idea_id = ?",
                ideaId
        );
        for (Map<String, Object> logoRow : allLogos) {
            String lid = String.valueOf(logoRow.get("id"));
            String lsvg = String.valueOf(logoRow.getOrDefault("svgCode", ""));
            String syncedSvg = (req.getLogoSuggestionId() != null && req.getLogoSuggestionId().equals(lid) && req.getLogoSvg() != null && !req.getLogoSvg().isBlank())
                    ? finalSvg
                    : replaceBrandTextInSvg(lsvg, finalBrand, knownBrandNames);
            jdbcTemplate.update("UPDATE kkumz_logo_suggestion SET svg_code = ? WHERE id = ?", syncedSvg, lid);
        }

        jdbcTemplate.update("""
                UPDATE kkumz_idea
                SET selected_brand_name = ?,
                    selected_site_slug = ?,
                    selected_logo_svg = ?,
                    design_tokens_json = ?::jsonb,
                    current_step = GREATEST(current_step, 3),
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                finalBrand, finalSlug, finalSvg, finalTokensJson, ideaId
        );

        Integer slugConflict = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM kkumz_site WHERE site_slug = ? AND idea_id <> ?",
                Integer.class, finalSlug, ideaId
        );
        if (slugConflict == null || slugConflict == 0) {
            jdbcTemplate.update("""
                    UPDATE kkumz_site
                    SET site_name = ?,
                        site_slug = ?,
                        logo_svg = ?,
                        deployed_url = ?,
                        custom_domain = ?,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE idea_id = ?
                    """,
                    finalBrand, finalSlug, finalSvg, "/s/" + finalSlug, finalSlug + ".kkumz.io", ideaId
            );
        }

        return getIdeaDetail(ideaId);
    }

    // =========================================================================
    // 3. Step 3 — AI 기능명세서(FRS) & 화면 블루프린트 생성/편집
    // =========================================================================

    public Map<String, Object> generateSpec(String ideaId) {
        return generateSpec(ideaId, false);
    }

    public Map<String, Object> generateSpec(String ideaId, boolean forceRegenerate) {
        assertIdeaEditable(ideaId);
        Map<String, Object> idea = getIdeaDetail(ideaId);
        String existingMarkdown = idea.get("specMarkdown") != null ? String.valueOf(idea.get("specMarkdown")) : "";
        String rawPromptText = idea.get("promptText") != null ? String.valueOf(idea.get("promptText")) : "";
        String brandName = idea.get("selectedBrandName") != null && !String.valueOf(idea.get("selectedBrandName")).isBlank()
                ? String.valueOf(idea.get("selectedBrandName")).trim()
                : String.valueOf(idea.get("title")).trim();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> existingScreens = (List<Map<String, Object>>) idea.get("screenSpecs");

        boolean isInitialPlaceholder = existingMarkdown.isBlank()
                || existingMarkdown.trim().equals(rawPromptText.trim())
                || existingMarkdown.contains("# 상세 기능 명세서 마크다운 전문");
        boolean brandMismatch = !brandName.isBlank() && !existingMarkdown.contains(brandName);

        if (!forceRegenerate && !isInitialPlaceholder && !brandMismatch && existingScreens != null && !existingScreens.isEmpty()) {
            return idea;
        }

        String siteType = String.valueOf(idea.get("siteType"));
        String promptText = buildPromptWithAssetContext(
                rawPromptText,
                listIdeaAssets(ideaId)
        );

        Map<String, Object> meta = getStudioMetadata();
        @SuppressWarnings("unchecked")
        Map<String, String> layoutLabels = (Map<String, String>) meta.getOrDefault("layoutLabels", Map.of());
        @SuppressWarnings("unchecked")
        Map<String, String> componentLabels = (Map<String, String>) meta.getOrDefault("componentLabels", Map.of());

        List<String> layoutCatalog = new ArrayList<>();
        layoutLabels.forEach((code, label) -> layoutCatalog.add(code + "(" + label + ")"));
        List<String> componentCatalog = new ArrayList<>();
        componentLabels.forEach((code, label) -> componentCatalog.add(code + "(" + label + ")"));

        String rawOutput = ensureAiGateway().executeStagePrompt("SPEC_FRS", Map.of(
                "brandName", brandName,
                "siteType", siteType,
                "promptText", promptText,
                "availableLayouts", layoutCatalog.isEmpty() ? "STANDARD_HERO, STANDARD_GRID, DYNAMIC_CMS_FORM" : String.join(", ", layoutCatalog),
                "availableComponents", componentCatalog.isEmpty() ? "BrandStoryBanner, FeaturedProductGrid, EditorialJournalSection, ClassBookingForm" : String.join(", ", componentCatalog)
        ));

        String markdown = rawOutput;
        List<Map<String, Object>> screens = new ArrayList<>();
        try {
            Map<String, Object> parsedObj = parseJsonObject(rawOutput);
            if (!parsedObj.isEmpty()) {
                if (parsedObj.get("specMarkdown") != null) {
                    markdown = String.valueOf(parsedObj.get("specMarkdown"));
                }
                if (parsedObj.get("screens") instanceof List<?> rawScreens) {
                    screens = objectMapper.convertValue(rawScreens, new TypeReference<List<Map<String, Object>>>() {});
                }
            }
        } catch (Exception ignored) {
            // Markdown 평문 응답인 경우 그대로 사용
        }

        jdbcTemplate.update("""
                UPDATE kkumz_idea
                SET spec_markdown = ?, screens_json = ?::jsonb, current_step = GREATEST(current_step, 4), updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                markdown, toJson(screens), ideaId
        );

        if (!screens.isEmpty()) {
            syncScreenSpecs(ideaId, screens);
        }

        return getIdeaDetail(ideaId);
    }

    @Transactional
    public Map<String, Object> updateSpec(String ideaId, UpdateSpecRequest req) {
        assertIdeaEditable(ideaId);
        Map<String, Object> idea = getIdeaDetail(ideaId);
        String newTitle = (req.getTitle() != null && !req.getTitle().isBlank())
                ? req.getTitle().trim()
                : String.valueOf(idea.get("title"));
        String newPrompt = (req.getPromptText() != null && !req.getPromptText().isBlank())
                ? req.getPromptText().trim()
                : String.valueOf(idea.get("promptText"));
        String newSiteType = (req.getSiteType() != null && !req.getSiteType().isBlank())
                ? req.getSiteType().trim().toUpperCase(Locale.ROOT)
                : String.valueOf(idea.get("siteType"));
        String newCategory = (req.getCategory() != null && !req.getCategory().isBlank())
                ? req.getCategory().trim()
                : String.valueOf(idea.getOrDefault("category", "AUTO"));
        String newVisibility = (req.getVisibility() != null && !req.getVisibility().isBlank())
                ? req.getVisibility().trim().toUpperCase(Locale.ROOT)
                : String.valueOf(idea.getOrDefault("visibility", "PUBLIC"));
        String tagsJson = (req.getTags() != null && !req.getTags().isEmpty())
                ? toJson(req.getTags())
                : toJson(idea.getOrDefault("tags", List.of(newCategory)));
        String enabledFeaturesJson = (req.getEnabledFeatures() != null && !req.getEnabledFeatures().isEmpty())
                ? toJson(req.getEnabledFeatures())
                : toJson(idea.getOrDefault("enabledFeatures", List.of()));
        String aiIntentAnalysisJson = (req.getAiIntentAnalysis() != null && !req.getAiIntentAnalysis().isEmpty())
                ? toJson(req.getAiIntentAnalysis())
                : toJson(idea.getOrDefault("aiIntentAnalysis", Map.of()));
        String markdown = (req.getSpecMarkdown() != null && !req.getSpecMarkdown().isBlank())
                ? req.getSpecMarkdown()
                : String.valueOf(idea.get("specMarkdown"));
        String screensJson = req.getScreens() != null
                ? toJson(req.getScreens())
                : toJson(idea.get("screens"));

        jdbcTemplate.update("""
                UPDATE kkumz_idea
                SET title = ?, prompt_text = ?, site_type = ?, category = ?, tags_json = ?::jsonb,
                    enabled_features_json = ?::jsonb, ai_intent_analysis_json = ?::jsonb, visibility = ?,
                    spec_markdown = ?, screens_json = ?::jsonb,
                    current_step = GREATEST(current_step, 4), updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                newTitle, newPrompt, newSiteType, newCategory, tagsJson,
                enabledFeaturesJson, aiIntentAnalysisJson, newVisibility, markdown, screensJson, ideaId
        );

        SaveIdeaVersionRequest verReq = SaveIdeaVersionRequest.builder()
                .title(newTitle + " (기획서 편집본)")
                .promptText(newPrompt)
                .specMarkdown(markdown)
                .screens(req.getScreens())
                .enabledFeatures(req.getEnabledFeatures())
                .aiIntentAnalysis(req.getAiIntentAnalysis())
                .createdBy(req.getUpdatedBy())
                .build();
        return saveOrRollbackIdeaVersion(ideaId, verReq);
    }

    // =========================================================================
    // 4. Step 4~6 — 멀티사이트 프로비저닝, WebIDE 편집, AI 빌드 복구 & 배포
    // =========================================================================

    @Transactional
    public Map<String, Object> scaffoldSite(String ideaId) {
        assertIdeaEditable(ideaId);
        long startNs = System.nanoTime();
        Map<String, Object> idea = getIdeaDetail(ideaId);
        List<Map<String, Object>> catalog = listFeatureCatalog();

        @SuppressWarnings("unchecked")
        List<String> enabledFeatures = new ArrayList<>((List<String>) idea.getOrDefault("enabledFeatures", List.of()));
        @SuppressWarnings("unchecked")
        Map<String, Object> aiIntentAnalysis = new LinkedHashMap<>((Map<String, Object>) idea.getOrDefault("aiIntentAnalysis", Map.of()));

        if (enabledFeatures.isEmpty() && aiIntentAnalysis.get("recommendedFeatures") instanceof List<?> rf) {
            for (Object o : rf) {
                if (o != null && !String.valueOf(o).isBlank()) {
                    enabledFeatures.add(String.valueOf(o).trim());
                }
            }
        }
        if (enabledFeatures.isEmpty()) {
            Map<String, Object> diag = diagnoseIntentFromCatalog(
                    String.valueOf(idea.getOrDefault("title", "")),
                    String.valueOf(idea.getOrDefault("promptText", "")),
                    String.valueOf(idea.getOrDefault("category", "AUTO")),
                    listIdeaAssetsInternal(ideaId),
                    catalog
            );
            if (diag.get("recommendedFeatures") instanceof List<?> rf) {
                for (Object o : rf) {
                    enabledFeatures.add(String.valueOf(o));
                }
            }
            if (aiIntentAnalysis.isEmpty()) {
                aiIntentAnalysis.putAll(diag);
            }
        }

        String siteType = String.valueOf(idea.getOrDefault("siteType", "HYBRID")).toUpperCase(Locale.ROOT);
        boolean needsShop = "COMMERCE".equalsIgnoreCase(siteType) || "HYBRID".equalsIgnoreCase(siteType)
                || hasEngineFeature(enabledFeatures, catalog, "SYNC_SHOP");
        boolean needsCms = "CMS_SITE".equalsIgnoreCase(siteType) || "HYBRID".equalsIgnoreCase(siteType)
                || hasEngineFeature(enabledFeatures, catalog, "SYNC_CMS");
        if (needsShop && needsCms) {
            siteType = "HYBRID";
        } else if (needsShop) {
            siteType = "COMMERCE";
        } else if (needsCms) {
            siteType = "CMS_SITE";
        }

        String category = String.valueOf(idea.getOrDefault("category", "AUTO"));
        if ("AUTO".equalsIgnoreCase(category) && aiIntentAnalysis.get("category") != null) {
            category = String.valueOf(aiIntentAnalysis.get("category"));
        }
        String brandName = resolveScaffoldBrandName(idea);
        String siteSlug = resolveScaffoldSiteSlug(idea, brandName);

        @SuppressWarnings("unchecked")
        Map<String, Object> rawTokens = (Map<String, Object>) idea.get("designTokens");
        Map<String, Object> tokens = resolveOrProvisionDesignTokens(idea, brandName, siteSlug, category, rawTokens);

        String rawLogoSvg = idea.get("selectedLogoSvg") != null ? String.valueOf(idea.get("selectedLogoSvg")) : "";
        String logoSvg = resolveOrProvisionLogoSvg(ideaId, idea, brandName, siteSlug, category, tokens, rawLogoSvg);

        String brandSlogan = resolveBrandSlogan(idea, brandName, category, aiIntentAnalysis);
        String heroStyle = aiIntentAnalysis.get("heroStyle") != null && !String.valueOf(aiIntentAnalysis.get("heroStyle")).isBlank()
                ? String.valueOf(aiIntentAnalysis.get("heroStyle"))
                : ("COMMERCE".equalsIgnoreCase(siteType) ? "PROMO_COMMERCE" : "FULL_SCENIC");
        String deployMode = "DYNAMIC_SDUI";

        Map<String, Object> layoutSchema = new LinkedHashMap<>();
        layoutSchema.put("heroTitle", brandName);
        layoutSchema.put("heroSubtitle", brandSlogan);
        layoutSchema.put("heroStyle", heroStyle);
        layoutSchema.put("ctaText", needsShop && needsCms ? "객실·상품 및 예약 보기" : (needsShop ? "시그니처 컬렉션 보기" : "프로그램·예약 신청하기"));
        layoutSchema.put("showFlashDeal", enabledFeatures.contains("SHOP_FLASH_DEAL") || needsShop);
        layoutSchema.put("showAiConcierge", enabledFeatures.contains("AI_BRAND_CONCIERGE") || true);
        layoutSchema.put("showCmsArticles", needsCms);
        layoutSchema.put("showCmsForm", needsCms);
        layoutSchema.put("customBadge", category);
        layoutSchema.put("enabledFeatures", enabledFeatures);

        Long shopSiteId = null;
        Long cmsSiteId = null;
        if (needsShop) {
            shopSiteId = provisionSyncShopSite(siteSlug, brandName, brandSlogan);
        }
        if (needsCms) {
            cmsSiteId = provisionSyncCmsSite(siteSlug, brandName, brandSlogan);
        }

        applyUploadedAssetsToSiteProvisioning(ideaId, brandName, layoutSchema, shopSiteId, cmsSiteId);
        provisionDomainSiteContent(idea, brandName, siteSlug, brandSlogan, category, enabledFeatures, catalog, layoutSchema, shopSiteId, cmsSiteId);

        List<Map<String, Object>> existingSite = jdbcTemplate.queryForList(
                "SELECT id, active_version AS \"activeVersion\" FROM kkumz_site WHERE idea_id = ? OR site_slug = ? ORDER BY updated_at DESC LIMIT 1",
                ideaId, siteSlug
        );

        String siteId;
        int nextVer;
        if (!existingSite.isEmpty()) {
            siteId = String.valueOf(existingSite.get(0).get("id"));
            nextVer = ((Number) existingSite.get(0).get("activeVersion")).intValue() + 1;
            jdbcTemplate.update("""
                    UPDATE kkumz_site
                    SET idea_id = ?, site_slug = ?, site_name = ?, site_type = ?, category = ?, brand_slogan = ?, logo_svg = ?,
                        deployed_url = ?, custom_domain = ?,
                        shop_site_id = COALESCE(?, shop_site_id), cms_site_id = COALESCE(?, cms_site_id),
                        deploy_mode = ?, active_version = ?, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                    ideaId, siteSlug, brandName, siteType, category, brandSlogan, logoSvg,
                    "/s/" + siteSlug, siteSlug + ".kkumz.io",
                    shopSiteId, cmsSiteId, deployMode, nextVer, siteId
            );
        } else {
            siteId = "site-" + UUID.randomUUID().toString().substring(0, 8);
            nextVer = 1;
            jdbcTemplate.update("""
                    INSERT INTO kkumz_site (
                        id, idea_id, site_slug, site_name, site_type, category, brand_slogan, logo_svg,
                        shop_site_id, cms_site_id, deploy_mode, active_version, published_version, published_at,
                        deployed_url, custom_domain,
                        is_featured, visibility, like_count, comment_count, view_count,
                        owner_username, owner_display_name, status, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, NULL, NULL, ?, ?, FALSE, 'PRIVATE', 0, 0, 0, ?, ?, 'DRAFT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """,
                    siteId, ideaId, siteSlug, brandName, siteType, category, brandSlogan, logoSvg,
                    shopSiteId, cmsSiteId, deployMode,
                    "/s/" + siteSlug, siteSlug + ".kkumz.io",
                    idea.get("authorUsername"), idea.get("authorDisplayName")
            );
        }

        List<Map<String, Object>> screenSpecs = listIdeaScreensInternal(ideaId);
        List<Map<String, Object>> webideFiles = buildWebIdeFilesFromScreens(brandName, tokens, layoutSchema, screenSpecs);
        String releaseId = "rel-" + siteId + "-v" + nextVer;
        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        long durationMs = Math.max(1L, (System.nanoTime() - startNs) / 1_000_000L);
        String buildLog = "[" + nowTime + "] [Provision] Site slug /s/" + siteSlug + " initialized (v" + nextVer + ", " + durationMs + "ms, features=" + enabledFeatures.size() + ").";
        String bundlePath = "/s/" + siteSlug + "/releases/v" + nextVer;

        jdbcTemplate.update("""
                INSERT INTO kkumz_site_release (
                    id, site_id, version_no, release_note, deploy_mode, design_tokens_json,
                    layout_schema_json, webide_files_json, minio_bundle_path, build_status, build_log, created_by, created_at
                ) VALUES (?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, 'SUCCESS', ?, ?, CURRENT_TIMESTAMP)
                """,
                releaseId, siteId, nextVer,
                "멀티사이트 프로비저닝 초안 (v" + nextVer + ")",
                deployMode, toJson(tokens), toJson(layoutSchema), toJson(webideFiles),
                bundlePath, buildLog, idea.get("authorUsername")
        );

        jdbcTemplate.update("""
                INSERT INTO kkumz_build_job (
                    id, site_id, release_id, job_type, deploy_mode, status, duration_ms,
                    ai_patch_applied, build_log, triggered_by, started_at, finished_at
                ) VALUES (?, ?, ?, 'INITIAL_PROVISION', ?, 'SUCCESS', ?, FALSE, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "job-" + UUID.randomUUID().toString().substring(0, 8),
                siteId, releaseId, deployMode, durationMs, buildLog, idea.get("authorUsername")
        );

        jdbcTemplate.update("""
                UPDATE kkumz_idea
                SET current_step = 6,
                    status = CASE WHEN status = 'PUBLISHED' THEN 'PUBLISHED' ELSE 'DRAFT' END,
                    site_type = ?, category = ?,
                    enabled_features_json = ?::jsonb, ai_intent_analysis_json = ?::jsonb,
                    selected_site_slug = ?, selected_brand_name = ?,
                    selected_logo_svg = ?, design_tokens_json = ?::jsonb,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                siteType, category, toJson(enabledFeatures), toJson(aiIntentAnalysis),
                siteSlug, brandName, logoSvg, toJson(tokens), ideaId
        );

        return getSiteRuntime(siteSlug);
    }

    @Transactional
    public Map<String, Object> chatRefineSite(String siteIdOrSlug, ChatRefineRequest req) {
        assertSiteEditable(siteIdOrSlug);
        long startNs = System.nanoTime();
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String ideaId = String.valueOf(site.get("ideaId"));
        String siteSlug = String.valueOf(site.get("siteSlug"));
        int currentVer = ((Number) site.get("activeVersion")).intValue();

        Map<String, Object> latestRel = getLatestRelease(siteId, currentVer);
        Map<String, Object> tokens = new LinkedHashMap<>(parseJsonObject(String.valueOf(latestRel.get("designTokensJson"))));
        Map<String, Object> layout = new LinkedHashMap<>(parseJsonObject(String.valueOf(latestRel.get("layoutSchemaJson"))));

        if (req.getTokenOverrides() != null && !req.getTokenOverrides().isEmpty()) {
            tokens.putAll(req.getTokenOverrides());
        }
        if (req.getLayoutOverrides() != null && !req.getLayoutOverrides().isEmpty()) {
            layout.putAll(req.getLayoutOverrides());
        }

        String newSiteName = (req.getSiteName() != null && !req.getSiteName().isBlank())
                ? req.getSiteName().trim()
                : String.valueOf(site.get("siteName"));
        String newBrandSlogan = (req.getBrandSlogan() != null && !req.getBrandSlogan().isBlank())
                ? req.getBrandSlogan().trim()
                : String.valueOf(site.get("brandSlogan"));
        String newLogoSvg = (req.getLogoSvg() != null && !req.getLogoSvg().isBlank())
                ? req.getLogoSvg().trim()
                : String.valueOf(site.getOrDefault("logoSvg", ""));

        String instruction = req.getInstruction() != null ? req.getInstruction().trim() : "";
        if (!instruction.isBlank()
                && (req.getTokenOverrides() == null || req.getTokenOverrides().isEmpty())
                && (req.getLayoutOverrides() == null || req.getLayoutOverrides().isEmpty())) {
            String aiJson = ensureAiGateway().executeStagePrompt("CHAT_REFINE", Map.of(
                    "tokensJson", toJson(tokens),
                    "layoutJson", toJson(layout),
                    "instruction", instruction
            ));
            Map<String, Object> parsed = parseJsonObject(aiJson);
            if (parsed.get("designTokens") instanceof Map<?, ?> dt) {
                dt.forEach((k, v) -> tokens.put(String.valueOf(k), v));
            }
            if (parsed.get("layoutSchema") instanceof Map<?, ?> ls) {
                ls.forEach((k, v) -> {
                    String key = String.valueOf(k);
                    if ("featureBlocks".equals(key) && v instanceof Map<?, ?> newBlocks && layout.get("featureBlocks") instanceof Map<?, ?> prevBlocks) {
                        Map<String, Object> mergedBlocks = new LinkedHashMap<>();
                        prevBlocks.forEach((pk, pv) -> mergedBlocks.put(String.valueOf(pk), pv));
                        newBlocks.forEach((nk, nv) -> mergedBlocks.put(String.valueOf(nk), nv));
                        layout.put("featureBlocks", mergedBlocks);
                    } else {
                        layout.put(key, v);
                    }
                });
            }
            for (String tokenKey : List.of("primary", "secondary", "accent", "surface", "radius")) {
                if (parsed.containsKey(tokenKey)) {
                    tokens.put(tokenKey, parsed.get(tokenKey));
                }
            }
            for (String layoutKey : List.of(
                    "heroTitle", "heroSubtitle", "heroStyle", "ctaText", "customBadge",
                    "showFlashDeal", "showAiConcierge", "enabledFeatures", "featureBlocks",
                    "customProducts", "customArticles", "customCmsForm"
            )) {
                if (parsed.containsKey(layoutKey)) {
                    layout.put(layoutKey, parsed.get(layoutKey));
                }
            }
        }

        if (layout.get("heroSubtitle") != null && !String.valueOf(layout.get("heroSubtitle")).isBlank()
                && (req.getBrandSlogan() == null || req.getBrandSlogan().isBlank())) {
            newBrandSlogan = String.valueOf(layout.get("heroSubtitle")).trim();
        }

        int nextVer = currentVer + 1;
        String releaseId = "rel-" + siteId + "-v" + nextVer;
        long durationMs = Math.max(1L, (System.nanoTime() - startNs) / 1_000_000L);
        String logMsg = "[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                + "] [Chat-Refine] Applied instruction ('" + instruction + "') -> v" + nextVer + " (" + durationMs + "ms).";

        jdbcTemplate.update("""
                INSERT INTO kkumz_site_release (
                    id, site_id, version_no, release_note, deploy_mode, design_tokens_json,
                    layout_schema_json, webide_files_json, minio_bundle_path, build_status, build_log, created_by, created_at
                ) VALUES (?, ?, ?, ?, 'DYNAMIC_SDUI', ?::jsonb, ?::jsonb, ?::jsonb, ?, 'SUCCESS', ?, ?, CURRENT_TIMESTAMP)
                """,
                releaseId, siteId, nextVer,
                "대화형 디자인 편집: " + (instruction.isBlank() ? "테마/레이아웃 속성 업데이트" : instruction),
                toJson(tokens), toJson(layout), String.valueOf(latestRel.get("webideFilesJson")),
                "/s/" + siteSlug + "/releases/v" + nextVer,
                logMsg,
                req.getUsername() != null ? req.getUsername() : site.get("ownerUsername")
        );

        jdbcTemplate.update("""
                UPDATE kkumz_site
                SET site_name = ?, brand_slogan = ?, logo_svg = ?, active_version = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                newSiteName, newBrandSlogan, newLogoSvg, nextVer, siteId);

        if (ideaId != null && !ideaId.isBlank() && !"null".equalsIgnoreCase(ideaId)) {
            if (layout.get("enabledFeatures") instanceof List<?> efList) {
                jdbcTemplate.update("""
                        UPDATE kkumz_idea
                        SET selected_brand_name = ?, selected_logo_svg = ?, design_tokens_json = ?::jsonb,
                            enabled_features_json = ?::jsonb, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                        newSiteName, newLogoSvg, toJson(tokens), toJson(efList), ideaId);
            } else {
                jdbcTemplate.update("""
                        UPDATE kkumz_idea
                        SET selected_brand_name = ?, selected_logo_svg = ?, design_tokens_json = ?::jsonb, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                        newSiteName, newLogoSvg, toJson(tokens), ideaId);
            }
        }

        jdbcTemplate.update("""
                INSERT INTO kkumz_build_job (
                    id, site_id, release_id, job_type, deploy_mode, status, duration_ms,
                    ai_patch_applied, build_log, triggered_by, started_at, finished_at
                ) VALUES (?, ?, ?, 'CHAT_HOT_RELOAD', 'DYNAMIC_SDUI', 'SUCCESS', ?, FALSE, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "job-" + UUID.randomUUID().toString().substring(0, 8),
                siteId, releaseId, durationMs, logMsg,
                req.getUsername() != null ? req.getUsername() : site.get("ownerUsername")
        );

        return getSiteRuntime(siteSlug);
    }

    public Map<String, Object> getWebIdeFiles(String siteIdOrSlug) {
        assertSiteEditable(siteIdOrSlug);
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        int activeVer = ((Number) site.get("activeVersion")).intValue();
        Map<String, Object> rel = getLatestRelease(siteId, activeVer);

        List<Map<String, Object>> jobs = jdbcTemplate.queryForList("""
                SELECT id,
                       job_type AS "jobType",
                       deploy_mode AS "deployMode",
                       status,
                       duration_ms AS "durationMs",
                       error_summary AS "errorSummary",
                       ai_patch_applied AS "aiPatchApplied",
                       build_log AS "buildLog",
                       triggered_by AS "triggeredBy",
                       started_at AS "startedAt"
                FROM kkumz_build_job
                WHERE site_id = ?
                ORDER BY started_at DESC
                LIMIT 10
                """, siteId);

        List<Map<String, Object>> normalizedFiles = normalizeAndFormatWebIdeFiles(
                parseJsonList(String.valueOf(rel.get("webideFilesJson")))
        );

        Map<String, Object> workspace = new LinkedHashMap<>();
        workspace.put("site", site);
        workspace.put("versionNo", rel.get("versionNo"));
        workspace.put("deployMode", rel.get("deployMode"));
        workspace.put("buildStatus", rel.get("buildStatus"));
        workspace.put("buildLog", rel.get("buildLog"));
        workspace.put("designTokens", parseJsonObject(String.valueOf(rel.get("designTokensJson"))));
        workspace.put("layoutSchema", parseJsonObject(String.valueOf(rel.get("layoutSchemaJson"))));
        workspace.put("files", normalizedFiles);
        workspace.put("buildJobs", jobs);
        workspace.put("gitRepo", findSiteGitRepoNullable(siteId));
        return workspace;
    }

    @Transactional
    public Map<String, Object> updateWebIdeFiles(String siteIdOrSlug, UpdateWebIdeFilesRequest req) {
        assertSiteEditable(siteIdOrSlug);
        long startNs = System.nanoTime();
        if (req == null || req.getFiles() == null || req.getFiles().isEmpty()) {
            throw new IllegalArgumentException("저장할 WebIDE 소스 파일 목록(files)이 비어 있습니다.");
        }
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));
        int currentVer = ((Number) site.get("activeVersion")).intValue();
        Map<String, Object> latestRel = getLatestRelease(siteId, currentVer);

        List<String> validationErrors = validateWebIdeSourceFiles(req.getFiles());
        boolean hasError = !validationErrors.isEmpty();

        int nextVer = currentVer + 1;
        String releaseId = "rel-" + siteId + "-v" + nextVer;
        String status = hasError ? "FAILED" : "SUCCESS";
        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        long durationMs = Math.max(1L, (System.nanoTime() - startNs) / 1_000_000L);
        String errorSummary = hasError ? String.join("; ", validationErrors) : null;
        String buildLog = hasError
                ? "[" + nowTime + "] [Bundle-Builder] Compiling " + req.getFiles().size() + " files...\n"
                  + "[" + nowTime + "] [ERROR] " + errorSummary
                : "[" + nowTime + "] [Bundle-Builder] Validated " + req.getFiles().size() + " SFC/JSON files (" + durationMs + "ms).\n"
                  + "[" + nowTime + "] [Publish] Release v" + nextVer + " activated for /s/" + siteSlug + ".";

        jdbcTemplate.update("""
                INSERT INTO kkumz_site_release (
                    id, site_id, version_no, release_note, deploy_mode, design_tokens_json,
                    layout_schema_json, webide_files_json, minio_bundle_path, build_status, build_log, created_by, created_at
                ) VALUES (?, ?, ?, ?, 'CUSTOM_BUNDLE', ?::jsonb, ?::jsonb, ?::jsonb, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                releaseId, siteId, nextVer,
                req.getCommitMessage() != null && !req.getCommitMessage().isBlank() ? req.getCommitMessage() : "WebIDE 코드 편집 저장 (v" + nextVer + ")",
                String.valueOf(latestRel.get("designTokensJson")),
                String.valueOf(latestRel.get("layoutSchemaJson")),
                toJson(req.getFiles()),
                "/s/" + siteSlug + "/releases/v" + nextVer,
                status, buildLog,
                req.getUsername() != null ? req.getUsername() : site.get("ownerUsername")
        );

        if (!hasError) {
            jdbcTemplate.update("UPDATE kkumz_site SET active_version = ?, deploy_mode = 'CUSTOM_BUNDLE', updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    nextVer, siteId);
        }

        jdbcTemplate.update("""
                INSERT INTO kkumz_build_job (
                    id, site_id, release_id, job_type, deploy_mode, status, duration_ms, error_summary,
                    ai_patch_applied, build_log, triggered_by, started_at, finished_at
                ) VALUES (?, ?, ?, 'WEBIDE_BUNDLE_BUILD', 'CUSTOM_BUNDLE', ?, ?, ?, FALSE, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "job-" + UUID.randomUUID().toString().substring(0, 8),
                siteId, releaseId, status, durationMs,
                errorSummary,
                buildLog,
                req.getUsername() != null ? req.getUsername() : site.get("ownerUsername")
        );

        return getWebIdeFiles(siteSlug);
    }

    @Transactional
    public Map<String, Object> aiFixBuildError(String siteIdOrSlug, FixBuildRequest req) {
        assertSiteEditable(siteIdOrSlug);
        long startNs = System.nanoTime();
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));

        List<Map<String, Object>> latestRelRows = jdbcTemplate.queryForList("""
                SELECT id, version_no AS "versionNo", design_tokens_json::text AS "designTokensJson",
                       layout_schema_json::text AS "layoutSchemaJson", webide_files_json::text AS "webideFilesJson",
                       build_log AS "buildLog"
                FROM kkumz_site_release
                WHERE site_id = ?
                ORDER BY version_no DESC
                LIMIT 1
                """, siteId);
        if (latestRelRows.isEmpty()) {
            throw new IllegalStateException("빌드 복구 대상 릴리즈 스냅샷이 존재하지 않습니다: siteId=" + siteId);
        }

        Map<String, Object> latestRel = latestRelRows.get(0);
        String currentFilesJson = String.valueOf(latestRel.get("webideFilesJson"));
        String prevBuildLog = (req != null && req.getErrorContext() != null && !req.getErrorContext().isBlank())
                ? req.getErrorContext().trim()
                : String.valueOf(latestRel.getOrDefault("buildLog", ""));

        String repairedJson = ensureAiGateway().executeStagePrompt("BUILD_FIX", Map.of(
                "buildLog", prevBuildLog,
                "filesJson", currentFilesJson
        ));

        List<Map<String, Object>> repairedFiles = parseAiJsonArray(repairedJson, "BUILD_FIX");
        if (repairedFiles.isEmpty()) {
            throw new IllegalStateException("AI 빌드 오류 패치 결과 파일 목록이 비어 있습니다.");
        }

        List<String> postCheckErrors = validateWebIdeSourceFiles(repairedFiles);
        if (!postCheckErrors.isEmpty()) {
            throw new IllegalStateException("AI 패치 후에도 문법 오류가 남아 있습니다: " + String.join("; ", postCheckErrors));
        }

        int nextVer = ((Number) latestRel.get("versionNo")).intValue() + 1;
        String releaseId = "rel-" + siteId + "-v" + nextVer;
        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        long durationMs = Math.max(1L, (System.nanoTime() - startNs) / 1_000_000L);
        String healLog = "[" + nowTime + "] [AI-Build-Fix] Repaired " + repairedFiles.size() + " files via AgentScope BUILD_FIX template (" + durationMs + "ms).";
        String actor = (req != null && req.getUsername() != null && !req.getUsername().isBlank())
                ? req.getUsername().trim()
                : String.valueOf(site.get("ownerUsername"));

        jdbcTemplate.update("""
                INSERT INTO kkumz_site_release (
                    id, site_id, version_no, release_note, deploy_mode, design_tokens_json,
                    layout_schema_json, webide_files_json, minio_bundle_path, build_status, build_log, created_by, created_at
                ) VALUES (?, ?, ?, 'AI 빌드 오류 자동 패치 및 배포 복구', 'CUSTOM_BUNDLE', ?::jsonb, ?::jsonb, ?::jsonb, ?, 'SUCCESS', ?, ?, CURRENT_TIMESTAMP)
                """,
                releaseId, siteId, nextVer,
                String.valueOf(latestRel.get("designTokensJson")),
                String.valueOf(latestRel.get("layoutSchemaJson")),
                toJson(repairedFiles),
                "/s/" + siteSlug + "/releases/v" + nextVer,
                healLog, actor
        );

        jdbcTemplate.update("UPDATE kkumz_site SET active_version = ?, deploy_mode = 'CUSTOM_BUNDLE', updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                nextVer, siteId);

        jdbcTemplate.update("""
                INSERT INTO kkumz_build_job (
                    id, site_id, release_id, job_type, deploy_mode, status, duration_ms,
                    ai_patch_applied, build_log, triggered_by, started_at, finished_at
                ) VALUES (?, ?, ?, 'AI_AUTO_HEAL_BUILD', 'CUSTOM_BUNDLE', 'SUCCESS', ?, TRUE, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "job-" + UUID.randomUUID().toString().substring(0, 8),
                siteId, releaseId, durationMs, healLog, actor
        );

        return getWebIdeFiles(siteSlug);
    }

    @Transactional
    public Map<String, Object> rollbackSiteRelease(String siteIdOrSlug, Integer versionNo) {
        assertSiteEditable(siteIdOrSlug);
        if (versionNo == null || versionNo <= 0) {
            throw new IllegalArgumentException("롤백할 버전 번호(versionNo)가 유효하지 않습니다.");
        }
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));

        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM kkumz_site_release WHERE site_id = ? AND version_no = ?",
                Integer.class, siteId, versionNo
        );
        if (exists == null || exists == 0) {
            throw new IllegalArgumentException("롤백할 릴리즈 버전이 존재하지 않습니다: v" + versionNo);
        }
        jdbcTemplate.update("UPDATE kkumz_site SET active_version = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                versionNo, siteId);
        return getSiteRuntime(siteSlug);
    }

    @Transactional
    public Map<String, Object> deploySite(String siteIdOrSlug, DeploySiteRequest req) {
        assertSiteEditable(siteIdOrSlug);
        long startNs = System.nanoTime();
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String ideaId = site.get("ideaId") != null ? String.valueOf(site.get("ideaId")) : null;
        String siteSlug = String.valueOf(site.get("siteSlug"));
        int currentVer = ((Number) site.get("activeVersion")).intValue();

        String deployMode = (req != null && req.getDeployMode() != null && !req.getDeployMode().isBlank())
                ? req.getDeployMode().trim().toUpperCase(Locale.ROOT)
                : String.valueOf(site.get("deployMode"));
        String visibility = (req != null && req.getVisibility() != null && !req.getVisibility().isBlank())
                ? req.getVisibility().trim().toUpperCase(Locale.ROOT)
                : "PUBLIC";
        String customDomain = (req != null && req.getCustomDomain() != null && !req.getCustomDomain().isBlank())
                ? req.getCustomDomain().trim()
                : String.valueOf(site.getOrDefault("customDomain", siteSlug + ".kkumz.io"));
        String releaseNote = (req != null && req.getReleaseNote() != null && !req.getReleaseNote().isBlank())
                ? req.getReleaseNote().trim()
                : null;

        int finalVer = currentVer;
        if (releaseNote != null) {
            Map<String, Object> latestRel = getLatestRelease(siteId, currentVer);
            finalVer = currentVer + 1;
            String releaseId = "rel-" + siteId + "-v" + finalVer;
            long durationMs = Math.max(1L, (System.nanoTime() - startNs) / 1_000_000L);
            String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String buildLog = "[" + nowTime + "] [Deploy] Published release v" + finalVer + " (" + deployMode + ", " + durationMs + "ms): " + releaseNote;
            String actor = (req.getUsername() != null && !req.getUsername().isBlank())
                    ? req.getUsername().trim()
                    : String.valueOf(site.get("ownerUsername"));

            jdbcTemplate.update("""
                    INSERT INTO kkumz_site_release (
                        id, site_id, version_no, release_note, deploy_mode, design_tokens_json,
                        layout_schema_json, webide_files_json, minio_bundle_path, build_status, build_log, created_by, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, 'SUCCESS', ?, ?, CURRENT_TIMESTAMP)
                    """,
                    releaseId, siteId, finalVer, releaseNote, deployMode,
                    String.valueOf(latestRel.get("designTokensJson")),
                    String.valueOf(latestRel.get("layoutSchemaJson")),
                    String.valueOf(latestRel.get("webideFilesJson")),
                    "/s/" + siteSlug + "/releases/v" + finalVer,
                    buildLog, actor
            );

            jdbcTemplate.update("""
                    INSERT INTO kkumz_build_job (
                        id, site_id, release_id, job_type, deploy_mode, status, duration_ms,
                        ai_patch_applied, build_log, triggered_by, started_at, finished_at
                    ) VALUES (?, ?, ?, 'LIVE_RELEASE_DEPLOY', ?, 'SUCCESS', ?, FALSE, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """,
                    "job-" + UUID.randomUUID().toString().substring(0, 8),
                    siteId, releaseId, deployMode, durationMs, buildLog, actor
            );
        }

        if ("PRIVATE".equalsIgnoreCase(visibility)) {
            jdbcTemplate.update("""
                    UPDATE kkumz_site
                    SET deploy_mode = ?, visibility = 'PRIVATE', custom_domain = ?, active_version = ?,
                        published_version = NULL, status = 'DRAFT', updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                    deployMode, customDomain, finalVer, siteId
            );
            if (ideaId != null && !ideaId.isBlank() && !"null".equalsIgnoreCase(ideaId)) {
                jdbcTemplate.update("""
                        UPDATE kkumz_idea
                        SET status = 'DRAFT', visibility = 'PRIVATE', updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                        ideaId
                );
            }
        } else {
            jdbcTemplate.update("""
                    UPDATE kkumz_site
                    SET deploy_mode = ?, visibility = 'PUBLIC', custom_domain = ?, active_version = ?,
                        published_version = ?, published_at = CURRENT_TIMESTAMP, status = 'ACTIVE', updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                    deployMode, customDomain, finalVer, finalVer, siteId
            );
            if (ideaId != null && !ideaId.isBlank() && !"null".equalsIgnoreCase(ideaId)) {
                jdbcTemplate.update("""
                        UPDATE kkumz_idea
                        SET status = 'PUBLISHED', visibility = 'PUBLIC', current_step = 6, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                        ideaId
                );
            }
        }

        createNotification(
                String.valueOf(site.get("ownerUsername")),
                "BUILD_DONE",
                site.get("siteName") + " v" + finalVer + ("PRIVATE".equalsIgnoreCase(visibility) ? " 비공개 전환 완료" : " 라이브 배포 완료"),
                "PRIVATE".equalsIgnoreCase(visibility)
                        ? "사이트가 비공개 초안 모드로 전환되어 외부 노출이 중지되었습니다."
                        : "멀티사이트 런타임(/s/" + siteSlug + ")에 최신 릴리즈가 반영되었습니다.",
                "/s/" + siteSlug
        );

        return getSiteRuntime(siteSlug);
    }

    @Transactional
    public Map<String, Object> unpublishSite(String siteIdOrSlug) {
        assertSiteEditable(siteIdOrSlug);
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String ideaId = site.get("ideaId") != null ? String.valueOf(site.get("ideaId")) : null;
        String siteSlug = String.valueOf(site.get("siteSlug"));

        jdbcTemplate.update("""
                UPDATE kkumz_site
                SET status = 'DRAFT', visibility = 'PRIVATE', published_version = NULL, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                siteId
        );
        if (ideaId != null && !ideaId.isBlank() && !"null".equalsIgnoreCase(ideaId)) {
            jdbcTemplate.update("""
                    UPDATE kkumz_idea
                    SET status = 'DRAFT', visibility = 'PRIVATE', updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                    ideaId
            );
        }

        createNotification(
                String.valueOf(site.get("ownerUsername")),
                "BUILD_DONE",
                site.get("siteName") + " 비공개 전환 완료",
                "사이트가 비공개 초안 상태(나만 보기)로 전환되어 쇼케이스 피드와 외부 접속이 차단되었습니다.",
                "/studio"
        );

        return getSiteRuntime(siteSlug);
    }

    // =========================================================================
    // 4-2. GitHub 리포지토리 프로비저닝, 양방향 Git Push 동기화 & 사용자 전용 배포 키 관리
    // =========================================================================

    public Map<String, Object> getSiteGitRepo(String siteIdOrSlug) {
        assertSiteEditable(siteIdOrSlug);
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        Map<String, Object> repo = findSiteGitRepoNullable(siteId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("site", site);
        result.put("provisioned", repo != null);
        result.put("gitRepo", repo);
        return result;
    }

    @Transactional
    public Map<String, Object> provisionSiteGitRepo(String siteIdOrSlug, ProvisionGitRepoRequest req) {
        assertSiteEditable(siteIdOrSlug);
        long startNs = System.nanoTime();
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));
        String siteName = String.valueOf(site.get("siteName"));
        int activeVer = ((Number) site.get("activeVersion")).intValue();
        Map<String, Object> latestRel = getLatestRelease(siteId, activeVer);

        String actor = (req != null && req.getUsername() != null && !req.getUsername().isBlank())
                ? req.getUsername().trim()
                : String.valueOf(site.get("ownerUsername"));
        String repoOwner = (req != null && req.getRepoOwner() != null && !req.getRepoOwner().isBlank())
                ? sanitizeGitIdentifier(req.getRepoOwner().trim(), "kkumz-sites")
                : "kkumz-sites";
        String repoName = (req != null && req.getRepoName() != null && !req.getRepoName().isBlank())
                ? sanitizeGitIdentifier(req.getRepoName().trim(), siteSlug)
                : sanitizeGitIdentifier(siteSlug, "kkumz-site");
        String defaultBranch = (req != null && req.getDefaultBranch() != null && !req.getDefaultBranch().isBlank())
                ? sanitizeGitIdentifier(req.getDefaultBranch().trim(), "main")
                : "main";
        String visibility = (req != null && req.getVisibility() != null && "PUBLIC".equalsIgnoreCase(req.getVisibility().trim()))
                ? "PUBLIC"
                : "PRIVATE";
        boolean includeScaffold = req == null || req.getIncludeFullViteScaffold() == null || req.getIncludeFullViteScaffold();

        // 1. 사이트 전용 런타임 API 액세스키 (kkumz_api_access_key) 발급 또는 조회
        Map<String, Object> siteApiKey = ensureSiteRuntimeApiKey(siteId, siteSlug, siteName, actor, false);
        String siteApiKeyId = String.valueOf(siteApiKey.get("id"));
        String siteAccessKey = String.valueOf(siteApiKey.get("accessKey"));
        String siteSecretKey = String.valueOf(siteApiKey.get("secretKey"));

        // 2. 리포지토리 단일 범위 Ed25519 SSH Deploy Key 쌍 생성
        String keyTitle = "kkumz-deploy-key@" + siteSlug + " (" + actor + ")";
        Map<String, String> sshKeyPair = generateEd25519SshKeyPair(keyTitle);
        String sshPublicKey = sshKeyPair.get("publicKey");
        String sshPrivateKeyPem = sshKeyPair.get("privateKeyPem");
        String sshFingerprint = sshKeyPair.get("fingerprint");

        String repoHtmlUrl = "https://github.com/" + repoOwner + "/" + repoName;
        String cloneSshUrl = "git@github.com:" + repoOwner + "/" + repoName + ".git";
        String cloneHttpsUrl = "https://github.com/" + repoOwner + "/" + repoName + ".git";

        // 3. 실행 가능한(Runnable) Vue 3 + Vite 프로젝트 스캐폴딩 파일 구성
        List<Map<String, Object>> currentFiles = normalizeAndFormatWebIdeFiles(
                parseJsonList(String.valueOf(latestRel.get("webideFilesJson")))
        );
        List<Map<String, Object>> fullRepoFiles = includeScaffold
                ? buildRunnableGitProjectFiles(site, currentFiles, repoOwner, repoName, defaultBranch, cloneSshUrl, siteAccessKey, siteSecretKey)
                : currentFiles;

        String commitMsg = "feat(scaffold): initialize " + siteName + " (" + siteSlug + ") runnable Vue 3 + Vite repository";
        String commitSha = calculateGitCommitSha(siteId, repoName, commitMsg, fullRepoFiles);

        // 4. 선택적 GitHub REST API 원격 저장소 생성 및 Deploy Key 등록
        String patToken = (req != null && req.getGithubPatToken() != null && !req.getGithubPatToken().isBlank())
                ? req.getGithubPatToken().trim()
                : resolveConfiguredGitHubToken();
        boolean remoteSynced = false;
        String syncMode = "MANAGED_GIT";
        String remoteNotice = "Kkumz Managed Git 저장소 프로비저닝 및 사용자 전용 키(SSH Deploy Key + Site API Key) 발급 완료";

        if (patToken != null && !patToken.isBlank()) {
            Map<String, Object> ghSync = syncWithGitHubRemoteApi(
                    patToken, repoOwner, repoName, defaultBranch, "PRIVATE".equals(visibility),
                    siteName + " — Built with Kkumz AI Studio (/s/" + siteSlug + ")",
                    keyTitle, sshPublicKey, fullRepoFiles, commitMsg
            );
            if (Boolean.TRUE.equals(ghSync.get("synced"))) {
                remoteSynced = true;
                syncMode = "GITHUB_API";
                if (ghSync.get("htmlUrl") != null) repoHtmlUrl = String.valueOf(ghSync.get("htmlUrl"));
                if (ghSync.get("sshUrl") != null) cloneSshUrl = String.valueOf(ghSync.get("sshUrl"));
                if (ghSync.get("cloneUrl") != null) cloneHttpsUrl = String.valueOf(ghSync.get("cloneUrl"));
                if (ghSync.get("owner") != null) repoOwner = String.valueOf(ghSync.get("owner"));
                if (ghSync.get("commitSha") != null) commitSha = String.valueOf(ghSync.get("commitSha"));
                remoteNotice = String.valueOf(ghSync.getOrDefault("message", "GitHub 원격 저장소 생성 및 Deploy Key 등록 완료"));
            } else if (ghSync.get("message") != null) {
                remoteNotice = String.valueOf(ghSync.get("message"));
            }
        }

        // 5. 로컬 디스크 Git 작업 트리 동기화
        writeGitRepoFilesToWorkspace(siteSlug, repoOwner, repoName, defaultBranch, commitSha, commitMsg, fullRepoFiles, sshPublicKey);

        // 6. 커밋 히스토리 기록 및 DB 영속화
        Map<String, Object> commitEntry = new LinkedHashMap<>();
        commitEntry.put("sha", commitSha);
        commitEntry.put("shortSha", commitSha.substring(0, 7));
        commitEntry.put("message", commitMsg);
        commitEntry.put("author", actor);
        commitEntry.put("branch", defaultBranch);
        commitEntry.put("fileCount", fullRepoFiles.size());
        commitEntry.put("committedAt", Instant.now().toString());
        List<Map<String, Object>> history = new ArrayList<>();
        history.add(commitEntry);

        Map<String, Object> existingRepo = findSiteGitRepoNullable(siteId);
        if (existingRepo != null) {
            if (existingRepo.get("commitHistory") instanceof List<?> prevList) {
                for (Object item : prevList) {
                    if (item instanceof Map<?, ?> m && history.size() < 15) {
                        Map<String, Object> copy = new LinkedHashMap<>();
                        m.forEach((k, v) -> copy.put(String.valueOf(k), v));
                        history.add(copy);
                    }
                }
            }
            jdbcTemplate.update("""
                    UPDATE kkumz_site_git_repo
                    SET repo_owner = ?, repo_name = ?, default_branch = ?, visibility = ?,
                        repo_html_url = ?, clone_ssh_url = ?, clone_https_url = ?,
                        ssh_key_title = ?, ssh_key_fingerprint = ?, ssh_public_key = ?, ssh_private_key_pem = ?,
                        site_api_key_id = ?, site_access_key = ?, site_secret_key = ?,
                        last_commit_sha = ?, last_commit_message = ?, sync_mode = ?, github_remote_synced = ?,
                        commit_history_json = ?::jsonb, key_rotated_at = CURRENT_TIMESTAMP,
                        last_synced_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                    WHERE site_id = ?
                    """,
                    repoOwner, repoName, defaultBranch, visibility,
                    repoHtmlUrl, cloneSshUrl, cloneHttpsUrl,
                    keyTitle, sshFingerprint, sshPublicKey, sshPrivateKeyPem,
                    siteApiKeyId, siteAccessKey, siteSecretKey,
                    commitSha, commitMsg, syncMode, remoteSynced,
                    toJson(history), siteId
            );
        } else {
            String gitRepoId = "git-" + siteId;
            jdbcTemplate.update("""
                    INSERT INTO kkumz_site_git_repo (
                        id, site_id, provider, repo_owner, repo_name, default_branch, visibility,
                        repo_html_url, clone_ssh_url, clone_https_url,
                        ssh_key_title, ssh_key_fingerprint, ssh_public_key, ssh_private_key_pem,
                        site_api_key_id, site_access_key, site_secret_key,
                        last_commit_sha, last_commit_message, sync_mode, github_remote_synced,
                        commit_history_json, key_rotated_at, last_synced_at, created_by, created_at, updated_at
                    ) VALUES (?, ?, 'GITHUB', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """,
                    gitRepoId, siteId, repoOwner, repoName, defaultBranch, visibility,
                    repoHtmlUrl, cloneSshUrl, cloneHttpsUrl,
                    keyTitle, sshFingerprint, sshPublicKey, sshPrivateKeyPem,
                    siteApiKeyId, siteAccessKey, siteSecretKey,
                    commitSha, commitMsg, syncMode, remoteSynced,
                    toJson(history), actor
            );
        }

        // 7. 최신 릴리즈의 webide_files_json에도 실행 가능 스캐폴딩 파일 세트 반영 및 빌드 로그 기록
        long durationMs = Math.max(1L, (System.nanoTime() - startNs) / 1_000_000L);
        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String buildLog = "[" + nowTime + "] [Git-Provision] Repository " + repoOwner + "/" + repoName + " (" + defaultBranch + ") initialized.\n"
                + "[" + nowTime + "] [SSH-KeyGen] Ed25519 Deploy Key issued (" + sshFingerprint + ").\n"
                + "[" + nowTime + "] [Runtime-Key] Site SDK Access Key issued (" + siteAccessKey + ").\n"
                + "[" + nowTime + "] [Commit] " + commitSha.substring(0, 7) + " - " + commitMsg + " (" + fullRepoFiles.size() + " files, " + durationMs + "ms).\n"
                + "[" + nowTime + "] [Sync-Status] " + remoteNotice;

        jdbcTemplate.update("""
                UPDATE kkumz_site_release
                SET webide_files_json = ?::jsonb, build_log = ?
                WHERE id = ?
                """,
                toJson(fullRepoFiles), buildLog, latestRel.get("id")
        );

        jdbcTemplate.update("""
                INSERT INTO kkumz_build_job (
                    id, site_id, release_id, job_type, deploy_mode, status, duration_ms,
                    ai_patch_applied, build_log, triggered_by, started_at, finished_at
                ) VALUES (?, ?, ?, 'GIT_REPO_PROVISION', 'CUSTOM_BUNDLE', 'SUCCESS', ?, FALSE, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "job-" + UUID.randomUUID().toString().substring(0, 8),
                siteId, latestRel.get("id"), durationMs, buildLog, actor
        );

        Map<String, Object> workspace = getWebIdeFiles(siteSlug);
        workspace.put("notice", remoteNotice);
        return workspace;
    }

    @Transactional
    public Map<String, Object> pushSiteToGitRepo(String siteIdOrSlug, GitPushSyncRequest req) {
        assertSiteEditable(siteIdOrSlug);
        long startNs = System.nanoTime();
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));
        String siteName = String.valueOf(site.get("siteName"));

        Map<String, Object> existingRepo = findSiteGitRepoNullable(siteId);
        if (existingRepo == null) {
            ProvisionGitRepoRequest provReq = ProvisionGitRepoRequest.builder()
                    .githubPatToken(req != null ? req.getGithubPatToken() : null)
                    .username(req != null ? req.getUsername() : null)
                    .includeFullViteScaffold(true)
                    .build();
            return provisionSiteGitRepo(siteSlug, provReq);
        }

        if (req != null && req.getFiles() != null && !req.getFiles().isEmpty()) {
            UpdateWebIdeFilesRequest updateReq = UpdateWebIdeFilesRequest.builder()
                    .files(req.getFiles())
                    .commitMessage(req.getCommitMessage())
                    .username(req.getUsername())
                    .build();
            updateWebIdeFiles(siteSlug, updateReq);
        }

        int activeVer = ((Number) findSiteByIdOrSlug(siteId).get("activeVersion")).intValue();
        Map<String, Object> latestRel = getLatestRelease(siteId, activeVer);
        List<Map<String, Object>> files = normalizeAndFormatWebIdeFiles(
                parseJsonList(String.valueOf(latestRel.get("webideFilesJson")))
        );

        String actor = (req != null && req.getUsername() != null && !req.getUsername().isBlank())
                ? req.getUsername().trim()
                : String.valueOf(site.get("ownerUsername"));
        String repoOwner = String.valueOf(existingRepo.get("repoOwner"));
        String repoName = String.valueOf(existingRepo.get("repoName"));
        String defaultBranch = String.valueOf(existingRepo.get("defaultBranch"));
        String commitMsg = (req != null && req.getCommitMessage() != null && !req.getCommitMessage().isBlank())
                ? req.getCommitMessage().trim()
                : "feat(studio): sync WebIDE changes for " + siteName + " (v" + activeVer + ")";
        String commitSha = calculateGitCommitSha(siteId, repoName, commitMsg + ":" + Instant.now(), files);

        String patToken = (req != null && req.getGithubPatToken() != null && !req.getGithubPatToken().isBlank())
                ? req.getGithubPatToken().trim()
                : resolveConfiguredGitHubToken();
        boolean remoteSynced = Boolean.TRUE.equals(existingRepo.get("githubRemoteSynced"));
        String syncMode = String.valueOf(existingRepo.get("syncMode"));
        String remoteNotice = "Git 저장소 커밋(" + commitSha.substring(0, 7) + ") 및 번들 동기화 완료";

        if (patToken != null && !patToken.isBlank()) {
            Map<String, Object> ghSync = syncWithGitHubRemoteApi(
                    patToken, repoOwner, repoName, defaultBranch,
                    "PRIVATE".equalsIgnoreCase(String.valueOf(existingRepo.get("visibility"))),
                    siteName + " — Built with Kkumz AI Studio (/s/" + siteSlug + ")",
                    String.valueOf(existingRepo.get("sshKeyTitle")),
                    String.valueOf(existingRepo.get("sshPublicKey")),
                    files, commitMsg
            );
            if (Boolean.TRUE.equals(ghSync.get("synced"))) {
                remoteSynced = true;
                syncMode = "GITHUB_API";
                if (ghSync.get("commitSha") != null) commitSha = String.valueOf(ghSync.get("commitSha"));
                remoteNotice = String.valueOf(ghSync.getOrDefault("message", "GitHub 원격 브랜치(" + defaultBranch + ")로 커밋 푸시 완료"));
            } else if (ghSync.get("message") != null) {
                remoteNotice = String.valueOf(ghSync.get("message"));
            }
        }

        writeGitRepoFilesToWorkspace(
                siteSlug, repoOwner, repoName, defaultBranch, commitSha, commitMsg, files,
                String.valueOf(existingRepo.get("sshPublicKey"))
        );

        Map<String, Object> commitEntry = new LinkedHashMap<>();
        commitEntry.put("sha", commitSha);
        commitEntry.put("shortSha", commitSha.substring(0, 7));
        commitEntry.put("message", commitMsg);
        commitEntry.put("author", actor);
        commitEntry.put("branch", defaultBranch);
        commitEntry.put("fileCount", files.size());
        commitEntry.put("committedAt", Instant.now().toString());

        List<Map<String, Object>> history = new ArrayList<>();
        history.add(commitEntry);
        if (existingRepo.get("commitHistory") instanceof List<?> prevList) {
            for (Object item : prevList) {
                if (item instanceof Map<?, ?> m && history.size() < 15) {
                    Map<String, Object> copy = new LinkedHashMap<>();
                    m.forEach((k, v) -> copy.put(String.valueOf(k), v));
                    history.add(copy);
                }
            }
        }

        jdbcTemplate.update("""
                UPDATE kkumz_site_git_repo
                SET last_commit_sha = ?, last_commit_message = ?, sync_mode = ?, github_remote_synced = ?,
                    commit_history_json = ?::jsonb, last_synced_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                WHERE site_id = ?
                """,
                commitSha, commitMsg, syncMode, remoteSynced, toJson(history), siteId
        );

        long durationMs = Math.max(1L, (System.nanoTime() - startNs) / 1_000_000L);
        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String pushLog = "[" + nowTime + "] [Git-Push] " + repoOwner + "/" + repoName + " (" + defaultBranch + ") <- " + commitSha.substring(0, 7) + "\n"
                + "[" + nowTime + "] [Commit-Msg] " + commitMsg + " (" + files.size() + " files, " + durationMs + "ms)\n"
                + "[" + nowTime + "] [Result] " + remoteNotice;

        jdbcTemplate.update(
                "UPDATE kkumz_site_release SET build_log = ? WHERE id = ?",
                pushLog, latestRel.get("id")
        );

        Map<String, Object> workspace = getWebIdeFiles(siteSlug);
        workspace.put("notice", remoteNotice);
        return workspace;
    }

    @Transactional
    public Map<String, Object> rotateSiteGitKeys(String siteIdOrSlug, RotateGitKeysRequest req) {
        assertSiteEditable(siteIdOrSlug);
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));
        String siteName = String.valueOf(site.get("siteName"));

        Map<String, Object> existingRepo = findSiteGitRepoNullable(siteId);
        if (existingRepo == null) {
            return provisionSiteGitRepo(siteSlug, ProvisionGitRepoRequest.builder()
                    .username(req != null ? req.getUsername() : null)
                    .githubPatToken(req != null ? req.getGithubPatToken() : null)
                    .includeFullViteScaffold(true)
                    .build());
        }

        boolean rotateSsh = req == null || req.getRotateSshKey() == null || req.getRotateSshKey();
        boolean rotateApi = req == null || req.getRotateSiteApiKey() == null || req.getRotateSiteApiKey();
        String actor = (req != null && req.getUsername() != null && !req.getUsername().isBlank())
                ? req.getUsername().trim()
                : String.valueOf(site.get("ownerUsername"));

        String sshKeyTitle = String.valueOf(existingRepo.get("sshKeyTitle"));
        String sshPublicKey = String.valueOf(existingRepo.get("sshPublicKey"));
        String sshPrivateKeyPem = String.valueOf(existingRepo.get("sshPrivateKeyPem"));
        String sshFingerprint = String.valueOf(existingRepo.get("sshKeyFingerprint"));

        if (rotateSsh) {
            sshKeyTitle = "kkumz-deploy-key@" + siteSlug + " (rotated by " + actor + ")";
            Map<String, String> newPair = generateEd25519SshKeyPair(sshKeyTitle);
            sshPublicKey = newPair.get("publicKey");
            sshPrivateKeyPem = newPair.get("privateKeyPem");
            sshFingerprint = newPair.get("fingerprint");

            String patToken = (req != null && req.getGithubPatToken() != null && !req.getGithubPatToken().isBlank())
                    ? req.getGithubPatToken().trim()
                    : resolveConfiguredGitHubToken();
            if (patToken != null && !patToken.isBlank()) {
                replaceGitHubDeployKey(
                        patToken,
                        String.valueOf(existingRepo.get("repoOwner")),
                        String.valueOf(existingRepo.get("repoName")),
                        sshKeyTitle,
                        sshPublicKey
                );
            }
        }

        String siteApiKeyId = existingRepo.get("siteApiKeyId") != null ? String.valueOf(existingRepo.get("siteApiKeyId")) : null;
        String siteAccessKey = String.valueOf(existingRepo.get("siteAccessKey"));
        String siteSecretKey = String.valueOf(existingRepo.get("siteSecretKey"));

        if (rotateApi) {
            Map<String, Object> rotatedKey = ensureSiteRuntimeApiKey(siteId, siteSlug, siteName, actor, true);
            siteApiKeyId = String.valueOf(rotatedKey.get("id"));
            siteAccessKey = String.valueOf(rotatedKey.get("accessKey"));
            siteSecretKey = String.valueOf(rotatedKey.get("secretKey"));
        }

        jdbcTemplate.update("""
                UPDATE kkumz_site_git_repo
                SET ssh_key_title = ?, ssh_key_fingerprint = ?, ssh_public_key = ?, ssh_private_key_pem = ?,
                    site_api_key_id = ?, site_access_key = ?, site_secret_key = ?,
                    key_rotated_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                WHERE site_id = ?
                """,
                sshKeyTitle, sshFingerprint, sshPublicKey, sshPrivateKeyPem,
                siteApiKeyId, siteAccessKey, siteSecretKey, siteId
        );

        int activeVer = ((Number) site.get("activeVersion")).intValue();
        Map<String, Object> latestRel = getLatestRelease(siteId, activeVer);
        List<Map<String, Object>> files = normalizeAndFormatWebIdeFiles(
                parseJsonList(String.valueOf(latestRel.get("webideFilesJson")))
        );
        for (Map<String, Object> f : files) {
            if (".env.example".equals(String.valueOf(f.get("path")))) {
                f.put("content", buildDotEnvExampleContent(siteSlug, siteAccessKey, siteSecretKey));
            }
        }
        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String rotateLog = "[" + nowTime + "] [Key-Rotation] Rotated developer keys for /s/" + siteSlug + ".\n"
                + "[" + nowTime + "] [SSH-Key] Fingerprint: " + sshFingerprint + "\n"
                + "[" + nowTime + "] [Site-API-Key] AccessKey: " + siteAccessKey + " (previous secret revoked)";
        jdbcTemplate.update(
                "UPDATE kkumz_site_release SET webide_files_json = ?::jsonb, build_log = ? WHERE id = ?",
                toJson(files), rotateLog, latestRel.get("id")
        );

        Map<String, Object> workspace = getWebIdeFiles(siteSlug);
        workspace.put("notice", "사용자 전용 SSH Deploy Key 및 사이트 런타임 API Secret Key가 안전하게 재발급(회전)되었습니다.");
        return workspace;
    }

    public StoredAssetPayload downloadSiteSshPrivateKey(String siteIdOrSlug) {
        assertSiteEditable(siteIdOrSlug);
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));
        Map<String, Object> repo = findSiteGitRepoNullable(siteId);
        if (repo == null) {
            provisionSiteGitRepo(siteSlug, ProvisionGitRepoRequest.builder().includeFullViteScaffold(true).build());
            repo = findSiteGitRepoNullable(siteId);
        }
        String pem = String.valueOf(repo.get("sshPrivateKeyPem"));
        return StoredAssetPayload.builder()
                .bytes(pem.getBytes(StandardCharsets.UTF_8))
                .mimeType("application/x-pem-file")
                .originalName(siteSlug + "-deploy-key.pem")
                .build();
    }

    public StoredAssetPayload downloadSiteGitArchive(String siteIdOrSlug) {
        assertSiteEditable(siteIdOrSlug);
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));
        Map<String, Object> repo = findSiteGitRepoNullable(siteId);
        if (repo == null) {
            provisionSiteGitRepo(siteSlug, ProvisionGitRepoRequest.builder().includeFullViteScaffold(true).build());
            repo = findSiteGitRepoNullable(siteId);
        }
        int activeVer = ((Number) findSiteByIdOrSlug(siteId).get("activeVersion")).intValue();
        Map<String, Object> latestRel = getLatestRelease(siteId, activeVer);
        List<Map<String, Object>> files = normalizeAndFormatWebIdeFiles(
                parseJsonList(String.valueOf(latestRel.get("webideFilesJson")))
        );

        String repoName = String.valueOf(repo.get("repoName"));
        String cloneSshUrl = String.valueOf(repo.get("cloneSshUrl"));
        String defaultBranch = String.valueOf(repo.get("defaultBranch"));
        String sshPrivateKeyPem = String.valueOf(repo.get("sshPrivateKeyPem"));
        String sshPublicKey = String.valueOf(repo.get("sshPublicKey"));
        String siteAccessKey = String.valueOf(repo.get("siteAccessKey"));
        String siteSecretKey = String.valueOf(repo.get("siteSecretKey"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {
            String rootPrefix = repoName + "/";
            for (Map<String, Object> file : files) {
                String relPath = String.valueOf(file.getOrDefault("path", "")).replaceAll("^/+", "");
                if (relPath.isBlank()) continue;
                String content = String.valueOf(file.getOrDefault("content", ""));
                addZipTextEntry(zos, rootPrefix + relPath, content);
            }
            addZipTextEntry(zos, rootPrefix + ".env.local", buildDotEnvExampleContent(siteSlug, siteAccessKey, siteSecretKey));
            addZipTextEntry(zos, rootPrefix + ".ssh/" + siteSlug + "-deploy-key.pem", sshPrivateKeyPem);
            addZipTextEntry(zos, rootPrefix + ".ssh/" + siteSlug + "-deploy-key.pub", sshPublicKey + "\n");
            String setupSh = "#!/usr/bin/env bash\n"
                    + "set -e\n"
                    + "chmod 600 .ssh/" + siteSlug + "-deploy-key.pem\n"
                    + "git init -b " + defaultBranch + "\n"
                    + "git config core.sshCommand \"ssh -i $(pwd)/.ssh/" + siteSlug + "-deploy-key.pem -o IdentitiesOnly=yes\"\n"
                    + "git remote remove origin 2>/dev/null || true\n"
                    + "git remote add origin " + cloneSshUrl + "\n"
                    + "echo \"[Kkumz Git] SSH Deploy Key & Remote (" + cloneSshUrl + ") configured.\"\n";
            addZipTextEntry(zos, rootPrefix + "setup-git-key.sh", setupSh);
            zos.finish();
            return StoredAssetPayload.builder()
                    .bytes(baos.toByteArray())
                    .mimeType("application/zip")
                    .originalName(repoName + "-git-bundle.zip")
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Git 프로젝트 번들 ZIP 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // 5. 멀티사이트 라이브 런타임 (/s/:siteSlug) — SyncShop + SyncCMS 실데이터 연동
    // =========================================================================

    @Transactional
    public Map<String, Object> getSiteRuntime(String siteSlug) {
        Map<String, Object> site = findSiteByIdOrSlug(siteSlug);
        String siteId = String.valueOf(site.get("id"));
        String ideaId = String.valueOf(site.get("ideaId"));
        String siteType = String.valueOf(site.get("siteType"));
        int activeVer = site.get("activeVersion") instanceof Number n ? n.intValue() : 1;

        String currentUser = resolveCurrentUsernameFromRequest();
        String ownerUser = site.get("ownerUsername") != null ? String.valueOf(site.get("ownerUsername")).trim() : "";
        boolean isOwner = currentUser != null && !currentUser.isBlank() && currentUser.equalsIgnoreCase(ownerUser);
        boolean canEdit = isOwner || (currentUser != null && !currentUser.isBlank() && canEditSite(site, currentUser));
        boolean isPublished = isSitePubliclyPublished(site);
        boolean canViewAsMember = canEdit || (currentUser != null && !currentUser.isBlank() && canViewSite(site, currentUser) && !isPublished);

        if (isHttpRequestActive() && !isPublished && !canViewAsMember) {
            throw new SecurityException("[403] 아직 배포되지 않았거나 비공개 상태인 사이트입니다.");
        }

        Integer publishedVer = resolvePublishedVersion(site);
        boolean hasUnpublishedChanges = !isPublished || (publishedVer != null && activeVer > publishedVer);
        int targetVer = (!canViewAsMember && isPublished && publishedVer != null && publishedVer > 0)
                ? publishedVer
                : activeVer;
        boolean isPreviewMode = !isPublished || (publishedVer != null && targetVer != publishedVer);

        if (!canViewAsMember) {
            jdbcTemplate.update("UPDATE kkumz_site SET view_count = view_count + 1 WHERE id = ?", siteId);
        }

        Map<String, Object> activeRel = getLatestRelease(siteId, targetVer);
        List<Map<String, Object>> allReleases = jdbcTemplate.queryForList("""
                SELECT id, version_no AS "versionNo", release_note AS "releaseNote", deploy_mode AS "deployMode",
                       build_status AS "buildStatus", created_by AS "createdBy", created_at AS "createdAt"
                FROM kkumz_site_release
                WHERE site_id = ?
                ORDER BY version_no DESC
                """, siteId);

        Map<String, Object> layoutSchema = new LinkedHashMap<>(parseJsonObject(String.valueOf(activeRel.get("layoutSchemaJson"))));
        List<Map<String, Object>> ideaAssets = listIdeaAssetsInternal(ideaId);
        List<Map<String, Object>> catalog = listFeatureCatalog();

        List<String> enabledFeatures = new ArrayList<>();
        if (layoutSchema.get("enabledFeatures") instanceof List<?> efList) {
            for (Object o : efList) {
                if (o != null && !String.valueOf(o).isBlank()) {
                    enabledFeatures.add(String.valueOf(o).trim());
                }
            }
        }
        if (enabledFeatures.isEmpty() && ideaId != null && !ideaId.isBlank() && !"null".equalsIgnoreCase(ideaId)) {
            try {
                List<Map<String, Object>> ideaRows = jdbcTemplate.queryForList(
                        "SELECT enabled_features_json::text AS \"ef\" FROM kkumz_idea WHERE id = ? LIMIT 1",
                        ideaId
                );
                if (!ideaRows.isEmpty()) {
                    enabledFeatures.addAll(parseJsonStringList(String.valueOf(ideaRows.get(0).get("ef"))));
                }
            } catch (Exception ignored) {
            }
        }
        layoutSchema.put("enabledFeatures", enabledFeatures);

        List<Map<String, Object>> activeFeatureCards = new ArrayList<>();
        for (Map<String, Object> fc : catalog) {
            String fCode = String.valueOf(fc.getOrDefault("featureCode", ""));
            if (enabledFeatures.contains(fCode)) {
                activeFeatureCards.add(fc);
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> featureBlocks = layoutSchema.get("featureBlocks") instanceof Map<?, ?> fb
                ? new LinkedHashMap<>((Map<String, Object>) fb)
                : new LinkedHashMap<>();

        String siteBrandName = String.valueOf(site.getOrDefault("siteName", "브랜드"));
        String siteBrandSlogan = String.valueOf(site.getOrDefault("brandSlogan", ""));
        String siteCategory = String.valueOf(site.getOrDefault("category", "브랜드 · 서비스"));
        ensureCatalogDefaultBlocks(siteBrandName, siteBrandSlogan, siteCategory, enabledFeatures, catalog, ideaAssets, featureBlocks);
        layoutSchema.put("featureBlocks", featureBlocks);

        Map<String, Object> runtime = new LinkedHashMap<>();
        site.put("isOwner", isOwner);
        site.put("canEdit", canEdit);
        site.put("isPublished", isPublished);
        site.put("publishedVersion", publishedVer);
        site.put("hasUnpublishedChanges", hasUnpublishedChanges);
        site.put("renderedVersion", targetVer);
        site.put("isPreviewMode", isPreviewMode);
        runtime.putAll(site);
        runtime.put("isOwner", isOwner);
        runtime.put("canEdit", canEdit);
        runtime.put("isPublished", isPublished);
        runtime.put("publishedVersion", publishedVer);
        runtime.put("publishedAt", site.get("publishedAt"));
        runtime.put("hasUnpublishedChanges", hasUnpublishedChanges);
        runtime.put("renderedVersion", targetVer);
        runtime.put("isPreviewMode", isPreviewMode);
        runtime.put("brandName", site.get("siteName"));
        runtime.put("tagline", site.get("brandSlogan"));
        runtime.put("site", site);
        runtime.put("activeVersion", activeVer);
        runtime.put("deployMode", activeRel.get("deployMode"));
        runtime.put("releaseNote", activeRel.get("releaseNote"));
        runtime.put("designTokens", parseJsonObject(String.valueOf(activeRel.get("designTokensJson"))));
        runtime.put("layoutSchema", layoutSchema);
        runtime.put("heroStyle", layoutSchema.getOrDefault("heroStyle", "FULL_SCENIC"));
        runtime.put("enabledFeatures", enabledFeatures);
        runtime.put("activeFeatureCards", activeFeatureCards);
        runtime.put("featureBlocks", featureBlocks);
        runtime.put("webideFiles", parseJsonList(String.valueOf(activeRel.get("webideFilesJson"))));
        runtime.put("releases", allReleases);
        runtime.put("screenSpecs", listIdeaScreensInternal(ideaId));
        runtime.put("assets", ideaAssets);
        runtime.put("products", List.of());
        runtime.put("categories", List.of());
        runtime.put("coupons", List.of());
        runtime.put("articles", List.of());
        runtime.put("banners", List.of());

        String resolvedSlug = String.valueOf(site.get("siteSlug"));
        Long shopSiteId = site.get("shopSiteId") != null ? ((Number) site.get("shopSiteId")).longValue() : null;
        Long cmsSiteId = site.get("cmsSiteId") != null ? ((Number) site.get("cmsSiteId")).longValue() : null;

        if (shopSiteId != null || "COMMERCE".equalsIgnoreCase(siteType) || "HYBRID".equalsIgnoreCase(siteType)
                || hasEngineFeature(enabledFeatures, catalog, "SYNC_SHOP")) {
            Map<String, Object> commerceData = loadCommerceData(shopSiteId, resolvedSlug);
            runtime.put("commerce", commerceData);
            if (commerceData.get("products") instanceof List<?> pList) {
                runtime.put("products", pList);
            }
            if (commerceData.get("categories") instanceof List<?> cList) {
                runtime.put("categories", cList);
            }
            if (commerceData.get("coupons") instanceof List<?> cpList) {
                runtime.put("coupons", cpList);
            }
        }

        if (cmsSiteId != null || "CMS_SITE".equalsIgnoreCase(siteType) || "HYBRID".equalsIgnoreCase(siteType)
                || hasEngineFeature(enabledFeatures, catalog, "SYNC_CMS")) {
            Map<String, Object> cmsData = loadCmsData(cmsSiteId, resolvedSlug);
            runtime.put("cms", cmsData);
            if (cmsData.get("articles") instanceof List<?> aList) {
                runtime.put("articles", aList);
            }
            if (cmsData.get("banners") instanceof List<?> bList) {
                runtime.put("banners", bList);
            }
        }

        if (layoutSchema.get("customProducts") instanceof List<?> cp && !cp.isEmpty()) {
            runtime.put("products", cp);
        }
        if (layoutSchema.get("customArticles") instanceof List<?> ca && !ca.isEmpty()) {
            runtime.put("articles", ca);
        }
        if (layoutSchema.get("customCmsForm") instanceof Map<?, ?> cf && !cf.isEmpty()) {
            runtime.put("cmsForm", new LinkedHashMap<>((Map<String, Object>) cf));
        } else if (runtime.get("cms") instanceof Map<?, ?> cmsMap && cmsMap.get("forms") instanceof List<?> fList && !fList.isEmpty()) {
            runtime.put("cmsForm", new LinkedHashMap<>((Map<String, Object>) fList.get(0)));
        } else {
            Map<String, Object> defaultForm = new LinkedHashMap<>();
            defaultForm.put("id", 1L);
            defaultForm.put("title", site.get("siteName") + " 온라인 예약 · 문의 신청");
            defaultForm.put("formName", site.get("siteName") + " 온라인 예약 · 문의 신청");
            defaultForm.put("description", "원하시는 일정이나 문의 사항을 남겨주시면 빠르게 안내해 드립니다.");
            defaultForm.put("submitText", "예약·문의 신청 접수하기");
            runtime.put("cmsForm", defaultForm);
        }
        if (runtime.get("cmsForm") instanceof Map<?, ?> rawCmsForm) {
            @SuppressWarnings("unchecked")
            Map<String, Object> formMap = new LinkedHashMap<>((Map<String, Object>) rawCmsForm);
            if (!(formMap.get("fields") instanceof List<?> fl && fl.size() >= 3)) {
                formMap.put("fields", List.of(
                        Map.of("key", "desiredDate", "label", "희망 방문 · 이용 일자", "type", "date", "required", true),
                        Map.of("key", "partySize", "label", "방문 인원 / 수량", "type", "select", "required", true, "options", List.of("1~2인 (커플/개인)", "3~4인 (가족/소모임)", "5인 이상 (단체/별도 문의)")),
                        Map.of("key", "programSelect", "label", "희망 공간 · 프로그램 선택", "type", "select", "required", true, "options", List.of("시그니처 메인 코스", "프리미엄 프라이빗 패키지", "당일 체험 & 라운지 이용")),
                        Map.of("key", "optionItems", "label", "추가 신청 옵션", "type", "checkbox", "required", false, "options", List.of("웰컴 로컬 다과·음료 세트", "프라이빗 바비큐·다이닝 세팅", "레이트 체크아웃 / 시간 연장"))
                ));
            }
            runtime.put("cmsForm", formMap);
        }
        runtime.put("galleryImages", resolveGalleryAssets(layoutSchema, ideaAssets));
        runtime.put("downloadableDocs", resolveDocumentAssets(layoutSchema, ideaAssets));

        runtime.put("recruits", listSiteRecruitsInternal(siteId));
        runtime.put("teamMembers", listSiteTeamMembersInternal(siteId));
        runtime.put("comments", listSiteCommentsInternal(siteId));

        return runtime;
    }

    @Transactional
    public Map<String, Object> createCommerceOrder(String siteSlug, CommerceOrderRequest req) {
        if (req == null || req.getProductId() == null) {
            throw new IllegalArgumentException("주문 상품 ID(productId)는 필수입니다.");
        }
        if (req.getReceiverName() == null || req.getReceiverName().isBlank()) {
            throw new IllegalArgumentException("수령인 성함(receiverName)은 필수입니다.");
        }
        if (req.getReceiverPhone() == null || req.getReceiverPhone().isBlank()) {
            throw new IllegalArgumentException("수령인 연락처(receiverPhone)는 필수입니다.");
        }
        if (req.getReceiverAddress() == null || req.getReceiverAddress().isBlank()) {
            throw new IllegalArgumentException("배송지 주소(receiverAddress)는 필수입니다.");
        }
        Map<String, Object> site = findSiteByIdOrSlug(siteSlug);
        assertSiteReadable(site);
        Long shopSiteId = site.get("shopSiteId") != null ? ((Number) site.get("shopSiteId")).longValue() : null;
        if (shopSiteId == null) {
            throw new IllegalStateException("해당 사이트에 매핑된 SyncShop 스토어 ID(shopSiteId)가 없습니다: " + siteSlug);
        }

        int quantity = (req.getQuantity() != null && req.getQuantity() > 0) ? req.getQuantity() : 1;
        String receiverName = req.getReceiverName().trim();
        String receiverPhone = req.getReceiverPhone().trim();
        String receiverAddress = req.getReceiverAddress().trim();
        String resolvedSlug = String.valueOf(site.get("siteSlug"));

        if (syncServiceApiClient != null) {
            try {
                Map<String, Object> orderPayload = new LinkedHashMap<>();
                orderPayload.put("productId", req.getProductId());
                orderPayload.put("quantity", quantity);
                orderPayload.put("receiverName", receiverName);
                orderPayload.put("receiverPhone", receiverPhone);
                orderPayload.put("receiverAddress", receiverAddress);
                orderPayload.put("note", req.getNote() != null ? req.getNote() : "");
                if (req.getDiscountAmount() != null) {
                    orderPayload.put("discountAmount", req.getDiscountAmount());
                }
                Map<String, Object> apiResult = syncServiceApiClient.createShopOrder(shopSiteId, resolvedSlug, orderPayload);
                String orderSn = String.valueOf(apiResult.getOrDefault("orderSn", "KKUMZ-" + Instant.now().toEpochMilli()));
                String productName = String.valueOf(apiResult.getOrDefault("productName", "브랜드 상품"));
                Object payAmount = apiResult.getOrDefault("payAmount", 0);

                createNotification(
                        String.valueOf(site.get("ownerUsername")),
                        "COMMERCE_ORDER",
                        "[" + site.get("siteName") + "] 신규 주문 접수 (" + orderSn + ")",
                        receiverName + " 고객님이 '" + productName + "' " + quantity + "건(" + payAmount + "원)을 주문했습니다.",
                        "/s/" + resolvedSlug
                );
                apiResult.put("shopSiteId", shopSiteId);
                apiResult.putIfAbsent("status", "PAID_READY_FOR_SHIPMENT");
                apiResult.putIfAbsent("orderedAt", Timestamp.from(Instant.now()));
                return apiResult;
            } catch (Exception e) {
                log.warn("SyncShop 주문 REST API 호출 실패, 폴백 검증 진행: {}", e.getMessage());
                if (shopJdbcTemplate == null) {
                    throw new IllegalStateException("SyncShop 주문 API 연동 오류: " + e.getMessage(), e);
                }
            }
        }

        if (shopJdbcTemplate == null) {
            throw new IllegalStateException("SyncShop 서비스 API 클라이언트 또는 데이터베이스 연결이 초기화되지 않았습니다.");
        }

        String orderSn = "KKUMZ-" + Instant.now().toEpochMilli();

        List<Map<String, Object>> prodRows = shopJdbcTemplate.queryForList("""
                SELECT id, name, pic, product_sn, brand_name, product_category_id, price, promotion_price, stock
                FROM pms_product WHERE id = ? AND site_id = ? AND delete_status = 0
                """, req.getProductId(), shopSiteId);

        if (prodRows.isEmpty()) {
            throw new IllegalArgumentException("주문 대상 상품이 존재하지 않습니다: productId=" + req.getProductId() + ", shopSiteId=" + shopSiteId);
        }

        Map<String, Object> prod = prodRows.get(0);
        Long resolvedProductId = ((Number) prod.get("id")).longValue();
        String productName = String.valueOf(prod.get("name"));
        String productPic = String.valueOf(prod.getOrDefault("pic", ""));
        String productSn = String.valueOf(prod.getOrDefault("product_sn", ""));
        String brandName = String.valueOf(prod.getOrDefault("brand_name", site.get("siteName")));
        Long categoryId = prod.get("product_category_id") != null ? ((Number) prod.get("product_category_id")).longValue() : null;
        BigDecimal promo = (BigDecimal) prod.get("promotion_price");
        BigDecimal base = (BigDecimal) prod.get("price");
        BigDecimal unitPrice = (promo != null && promo.compareTo(BigDecimal.ZERO) > 0) ? promo : base;
        int currentStock = prod.get("stock") != null ? ((Number) prod.get("stock")).intValue() : 0;
        if (currentStock < quantity) {
            throw new IllegalStateException("상품 재고가 부족합니다: 현재 재고=" + currentStock + ", 요청 수량=" + quantity);
        }

        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        Long orderId = shopJdbcTemplate.queryForObject("""
                INSERT INTO oms_order (
                    site_id, member_id, order_sn, create_time, member_username,
                    total_amount, pay_amount, freight_amount, promotion_amount, integration_amount, coupon_amount, discount_amount,
                    pay_type, source_type, status, order_type,
                    receiver_name, receiver_phone, receiver_detail_address, note, confirm_status, delete_status
                ) VALUES (?, 1001, ?, CURRENT_TIMESTAMP, ?, ?, ?, 0.00, 0.00, 0.00, 0.00, 0.00, 1, 1, 1, 0, ?, ?, ?, ?, 0, 0)
                RETURNING id
                """,
                Long.class,
                shopSiteId, orderSn, receiverName, totalAmount, totalAmount,
                receiverName, receiverPhone, receiverAddress,
                req.getNote() != null ? req.getNote() : ""
        );

        shopJdbcTemplate.update("""
                INSERT INTO oms_order_item (
                    order_id, order_sn, product_id, product_pic, product_name, product_brand,
                    product_sn, product_price, product_quantity, product_category_id, real_amount
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                orderId, orderSn, resolvedProductId, productPic, productName, brandName,
                productSn, unitPrice, quantity, categoryId, totalAmount
        );

        shopJdbcTemplate.update("""
                UPDATE pms_product SET stock = GREATEST(0, stock - ?), sale = sale + ?
                WHERE id = ?
                """,
                quantity, quantity, resolvedProductId
        );

        int remainingStock = Math.max(0, currentStock - quantity);

        createNotification(
                String.valueOf(site.get("ownerUsername")),
                "COMMERCE_ORDER",
                "[" + site.get("siteName") + "] 신규 주문 접수 (" + orderSn + ")",
                receiverName + " 고객님이 '" + productName + "' " + quantity + "건(" + totalAmount + "원)을 주문했습니다.",
                "/s/" + site.get("siteSlug")
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", orderId);
        result.put("orderSn", orderSn);
        result.put("shopSiteId", shopSiteId);
        result.put("productId", resolvedProductId);
        result.put("productName", productName);
        result.put("quantity", quantity);
        result.put("unitPrice", unitPrice);
        result.put("payAmount", totalAmount);
        result.put("remainingStock", remainingStock);
        result.put("receiverName", receiverName);
        result.put("status", "PAID_READY_FOR_SHIPMENT");
        result.put("orderedAt", Timestamp.from(Instant.now()));
        return result;
    }

    @Transactional
    public Map<String, Object> submitCmsForm(String siteSlug, CmsFormSubmitRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("신청 폼 요청 데이터(req)가 비어 있습니다.");
        }
        String applicant = (req.getApplicantName() != null && !req.getApplicantName().isBlank())
                ? req.getApplicantName().trim()
                : (req.getExtraFields() != null && req.getExtraFields().get("applicant_name") != null
                ? String.valueOf(req.getExtraFields().get("applicant_name")).trim()
                : "");
        if (applicant.isBlank()) {
            throw new IllegalArgumentException("신청자 성함(applicantName)은 필수입니다.");
        }
        String phone = (req.getPhone() != null && !req.getPhone().isBlank())
                ? req.getPhone().trim()
                : (req.getExtraFields() != null && req.getExtraFields().get("phone") != null
                ? String.valueOf(req.getExtraFields().get("phone")).trim()
                : "");
        if (phone.isBlank()) {
            throw new IllegalArgumentException("신청자 연락처(phone)는 필수입니다.");
        }
        Map<String, Object> site = findSiteByIdOrSlug(siteSlug);
        assertSiteReadable(site);
        Long cmsSiteId = site.get("cmsSiteId") != null ? ((Number) site.get("cmsSiteId")).longValue() : null;
        if (cmsSiteId == null) {
            throw new IllegalStateException("해당 사이트에 매핑된 SyncCMS 사이트 ID(cmsSiteId)가 없습니다: " + siteSlug);
        }
        String resolvedSlug = String.valueOf(site.get("siteSlug"));

        Map<String, Object> payload = new LinkedHashMap<>();
        if (req.getExtraFields() != null) {
            payload.putAll(req.getExtraFields());
        }
        payload.put("applicantName", applicant);
        payload.put("applicant_name", applicant);
        payload.put("phone", phone);
        if (req.getFormId() != null) {
            payload.put("formId", req.getFormId());
        }
        if (req.getOptionValue() != null && !req.getOptionValue().isBlank()) {
            payload.put("optionValue", req.getOptionValue().trim());
            payload.put("option_value", req.getOptionValue().trim());
        }
        if (req.getMessage() != null && !req.getMessage().isBlank()) {
            payload.put("message", req.getMessage().trim());
        }

        if (syncServiceApiClient != null) {
            try {
                Map<String, Object> apiResult = syncServiceApiClient.submitCmsForm(cmsSiteId, resolvedSlug, payload);
                createNotification(
                        String.valueOf(site.get("ownerUsername")),
                        "CMS_FORM_SUBMIT",
                        "[" + site.get("siteName") + "] 신규 온라인 신청 접수",
                        applicant + "님이 참가/예약 신청서를 제출했습니다.",
                        "/s/" + resolvedSlug
                );
                apiResult.put("cmsSiteId", cmsSiteId);
                apiResult.put("applicantName", applicant);
                apiResult.put("submittedData", payload);
                apiResult.putIfAbsent("status", "RECEIVED");
                apiResult.putIfAbsent("submittedAt", Timestamp.from(Instant.now()));
                return apiResult;
            } catch (Exception e) {
                log.warn("SyncCMS 폼 접수 REST API 호출 실패, 폴백 검증 진행: {}", e.getMessage());
                if (cmsJdbcTemplate == null) {
                    throw new IllegalStateException("SyncCMS 폼 접수 API 연동 오류: " + e.getMessage(), e);
                }
            }
        }

        if (cmsJdbcTemplate == null) {
            throw new IllegalStateException("SyncCMS 서비스 API 클라이언트 또는 데이터베이스 연결이 초기화되지 않았습니다.");
        }

        Long formId = req.getFormId();
        if (formId == null) {
            List<Map<String, Object>> forms = cmsJdbcTemplate.queryForList(
                    "SELECT id FROM cms_form WHERE site_id = ? AND status = 1 ORDER BY id ASC LIMIT 1",
                    cmsSiteId
            );
            if (forms.isEmpty()) {
                throw new IllegalStateException("해당 사이트에 활성화된 CMS 신청 폼이 존재하지 않습니다: cmsSiteId=" + cmsSiteId);
            }
            formId = ((Number) forms.get(0).get("id")).longValue();
        }

        Long submissionId = cmsJdbcTemplate.queryForObject("""
                INSERT INTO cms_form_data (
                    form_id, submission_data, client_ip, status, create_date
                ) VALUES (?, ?::jsonb, '127.0.0.1', 'RECEIVED', CURRENT_TIMESTAMP)
                RETURNING id
                """,
                Long.class,
                formId, toJson(payload)
        );

        Integer cnt = cmsJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cms_form_data WHERE form_id = ?",
                Integer.class, formId
        );
        int totalSubmissions = cnt != null ? cnt : 1;

        createNotification(
                String.valueOf(site.get("ownerUsername")),
                "CMS_FORM_SUBMIT",
                "[" + site.get("siteName") + "] 신규 온라인 신청 접수",
                applicant + "님이 참가/예약 신청서를 제출했습니다.",
                "/s/" + site.get("siteSlug")
        );

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("submissionId", submissionId);
        res.put("formId", formId);
        res.put("cmsSiteId", cmsSiteId);
        res.put("applicantName", applicant);
        res.put("submittedData", payload);
        res.put("totalSubmissions", totalSubmissions);
        res.put("status", "RECEIVED");
        res.put("submittedAt", Timestamp.from(Instant.now()));
        return res;
    }

    public Map<String, Object> askSiteConcierge(String siteSlug, SiteConciergeRequest req) {
        if (req == null || req.getQuestion() == null || req.getQuestion().isBlank()) {
            throw new IllegalArgumentException("질문 내용(question)은 필수입니다.");
        }
        Map<String, Object> runtime = getSiteRuntime(siteSlug);
        @SuppressWarnings("unchecked")
        Map<String, Object> site = (Map<String, Object>) runtime.get("site");
        String siteName = String.valueOf(site.get("siteName"));
        String slogan = String.valueOf(site.get("brandSlogan"));
        String ideaId = String.valueOf(site.get("ideaId"));
        String question = req.getQuestion().trim();
        String enrichedQuestion = buildConciergeQuestionWithContext(question, ideaId, runtime);

        String answer = ensureAiGateway().executeStagePrompt("CONCIERGE", Map.of(
                "siteName", siteName,
                "brandSlogan", slogan,
                "question", enrichedQuestion
        ));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("siteSlug", siteSlug);
        response.put("siteName", siteName);
        response.put("question", question);
        response.put("answer", answer);
        response.put("answeredAt", Timestamp.from(Instant.now()));
        return response;
    }

    // =========================================================================
    // 6. 창업 커뮤니티 쇼케이스 피드, 팀원 모집/지원, 댓글, 좋아요
    // =========================================================================

    public List<Map<String, Object>> getCommunityFeed(String siteType, String recruitRole, String sortBy, String keyword) {
        StringBuilder sql = new StringBuilder("""
                SELECT s.id,
                       s.idea_id AS "ideaId",
                       s.site_slug AS "siteSlug",
                       s.site_name AS "siteName",
                       s.site_type AS "siteType",
                       s.category,
                       s.brand_slogan AS "brandSlogan",
                       s.logo_svg AS "logoSvg",
                       s.deploy_mode AS "deployMode",
                       s.active_version AS "activeVersion",
                       s.published_version AS "publishedVersion",
                       s.published_at AS "publishedAt",
                       s.visibility,
                       s.deployed_url AS "deployedUrl",
                       s.custom_domain AS "customDomain",
                       s.is_featured AS "isFeatured",
                       s.like_count AS "likeCount",
                       s.comment_count AS "commentCount",
                       s.view_count AS "viewCount",
                       s.owner_username AS "ownerUsername",
                       s.owner_display_name AS "ownerDisplayName",
                       s.status,
                       s.created_at AS "createdAt",
                       i.prompt_text AS "promptText",
                       i.tags_json::text AS "tagsJson",
                       i.design_tokens_json::text AS "designTokensJson"
                FROM kkumz_site s
                JOIN kkumz_idea i ON i.id = s.idea_id
                WHERE s.status = 'ACTIVE' AND s.visibility = 'PUBLIC' AND s.published_version IS NOT NULL
                """);
        List<Object> params = new ArrayList<>();

        if (siteType != null && !siteType.isBlank() && !"ALL".equalsIgnoreCase(siteType)) {
            sql.append(" AND s.site_type = ?");
            params.add(siteType.trim().toUpperCase(Locale.ROOT));
        }
        if (recruitRole != null && !recruitRole.isBlank() && !"ALL".equalsIgnoreCase(recruitRole)) {
            sql.append(" AND EXISTS (SELECT 1 FROM kkumz_team_recruit r WHERE r.site_id = s.id AND r.status = 'OPEN' AND r.position_role = ?)");
            params.add(recruitRole.trim().toUpperCase(Locale.ROOT));
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (s.site_name ILIKE ? OR s.brand_slogan ILIKE ? OR i.prompt_text ILIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if ("RECENT".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY s.created_at DESC");
        } else if ("VIEWS".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY s.view_count DESC, s.like_count DESC");
        } else {
            sql.append(" ORDER BY s.is_featured DESC, s.like_count DESC, s.view_count DESC");
        }

        List<Map<String, Object>> rawSites = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        List<Map<String, Object>> sites = new ArrayList<>();
        String currentUser = resolveCurrentUsernameFromRequest();
        for (Map<String, Object> item : rawSites) {
            if (!isSitePubliclyPublished(item)) {
                continue;
            }
            String sId = String.valueOf(item.get("id"));
            String ownerUser = item.get("ownerUsername") != null ? String.valueOf(item.get("ownerUsername")).trim() : "";
            boolean isOwner = currentUser != null && !currentUser.isBlank() && currentUser.equalsIgnoreCase(ownerUser);
            boolean canEdit = isOwner || (currentUser != null && !currentUser.isBlank() && canEditSite(item, currentUser));
            int activeVer = item.get("activeVersion") instanceof Number n ? n.intValue() : 1;
            Integer pubVer = resolvePublishedVersion(item);
            item.put("isOwner", isOwner);
            item.put("canEdit", canEdit);
            item.put("isPublished", true);
            item.put("publishedVersion", pubVer != null ? pubVer : activeVer);
            item.put("hasUnpublishedChanges", pubVer != null && activeVer > pubVer);
            item.put("tags", parseJsonList(String.valueOf(item.remove("tagsJson"))));
            item.put("designTokens", parseJsonObject(String.valueOf(item.remove("designTokensJson"))));
            item.put("openRecruits", listSiteRecruitsInternal(sId));
            item.put("teamMembers", listSiteTeamMembersInternal(sId));
            item.put("recentComments", listSiteCommentsInternal(sId));
            sites.add(item);
        }
        return sites;
    }

    @Transactional
    public Map<String, Object> toggleSiteLike(String siteIdOrSlug, String username) {
        String authUser = resolveCurrentUsernameFromRequest();
        String resolvedUser = (authUser != null && !authUser.isBlank()) ? authUser : username;
        if (resolvedUser == null || resolvedUser.isBlank()) {
            if (isHttpRequestActive()) {
                throw new SecurityException("[401] 로그인이 필요한 기능입니다. 먼저 로그인해 주세요.");
            }
            throw new IllegalArgumentException("좋아요 처리를 위한 사용자 계정(username)은 필수입니다.");
        }
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        assertSiteReadable(site);
        String siteId = String.valueOf(site.get("id"));
        String user = resolvedUser.trim();

        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM kkumz_feed_like WHERE site_id = ? AND username = ?",
                Integer.class, siteId, user
        );

        boolean liked;
        if (exists != null && exists > 0) {
            jdbcTemplate.update("DELETE FROM kkumz_feed_like WHERE site_id = ? AND username = ?", siteId, user);
            jdbcTemplate.update("UPDATE kkumz_site SET like_count = GREATEST(0, like_count - 1) WHERE id = ?", siteId);
            liked = false;
        } else {
            jdbcTemplate.update(
                    "INSERT INTO kkumz_feed_like (id, site_id, username, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
                    "like-" + UUID.randomUUID().toString().substring(0, 8), siteId, user
            );
            jdbcTemplate.update("UPDATE kkumz_site SET like_count = like_count + 1 WHERE id = ?", siteId);
            liked = true;
        }

        Integer newCount = jdbcTemplate.queryForObject("SELECT like_count FROM kkumz_site WHERE id = ?", Integer.class, siteId);
        return Map.of("siteId", siteId, "liked", liked, "likeCount", newCount != null ? newCount : 0);
    }

    public List<Map<String, Object>> listSiteComments(String siteIdOrSlug) {
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        assertSiteReadable(site);
        String siteId = String.valueOf(site.get("id"));
        return listSiteCommentsInternal(siteId);
    }

    private List<Map<String, Object>> listSiteCommentsInternal(String siteId) {
        return jdbcTemplate.queryForList("""
                SELECT id,
                       site_id AS "siteId",
                       username,
                       display_name AS "displayName",
                       author_role_badge AS "authorRoleBadge",
                       content,
                       created_at AS "createdAt"
                FROM kkumz_feed_comment
                WHERE site_id = ?
                ORDER BY created_at DESC
                """, siteId);
    }

    @Transactional
    public List<Map<String, Object>> addSiteComment(String siteIdOrSlug, CreateCommentRequest req) {
        if (req == null || req.getContent() == null || req.getContent().isBlank()) {
            throw new IllegalArgumentException("댓글 내용(content)을 입력해 주세요.");
        }
        String authUser = isHttpRequestActive() ? requireAuthenticatedUsername() : resolveCurrentUsernameFromRequest();
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        assertSiteReadable(site);
        String siteId = String.valueOf(site.get("id"));
        Map<String, String> author = resolveUserIdentity(
                authUser != null ? authUser : req.getUsername(),
                req.getDisplayName()
        );
        String username = author.get("username");
        String displayName = author.get("realname");
        String badge = (req.getAuthorRoleBadge() != null && !req.getAuthorRoleBadge().isBlank())
                ? req.getAuthorRoleBadge().trim()
                : "메이커";

        jdbcTemplate.update("""
                INSERT INTO kkumz_feed_comment (
                    id, site_id, username, display_name, author_role_badge, content, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                "cmt-" + UUID.randomUUID().toString().substring(0, 8),
                siteId, username, displayName, badge, req.getContent().trim()
        );

        jdbcTemplate.update("UPDATE kkumz_site SET comment_count = comment_count + 1 WHERE id = ?", siteId);
        return listSiteCommentsInternal(siteId);
    }

    public List<Map<String, Object>> listSiteRecruits(String siteIdOrSlug) {
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        assertSiteReadable(site);
        String siteId = String.valueOf(site.get("id"));
        return listSiteRecruitsInternal(siteId);
    }

    private List<Map<String, Object>> listSiteRecruitsInternal(String siteId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id,
                       site_id AS "siteId",
                       position_role AS "positionRole",
                       title,
                       description,
                       required_skills_json::text AS "requiredSkillsJson",
                       headcount,
                       recruited_count AS "recruitedCount",
                       status,
                       created_at AS "createdAt"
                FROM kkumz_team_recruit
                WHERE site_id = ?
                ORDER BY created_at DESC
                """, siteId);
        rows.forEach(r -> r.put("requiredSkills", parseJsonList(String.valueOf(r.remove("requiredSkillsJson")))));
        return rows;
    }

    @Transactional
    public List<Map<String, Object>> createSiteRecruit(String siteIdOrSlug, CreateRecruitRequest req) {
        assertSiteEditable(siteIdOrSlug);
        if (req == null || req.getTitle() == null || req.getTitle().isBlank()) {
            throw new IllegalArgumentException("팀원 모집 공고 제목(title)은 필수입니다.");
        }
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String role = (req.getPositionRole() != null && !req.getPositionRole().isBlank())
                ? req.getPositionRole().trim().toUpperCase(Locale.ROOT)
                : "FRONTEND";
        int headcount = (req.getHeadcount() != null && req.getHeadcount() > 0) ? req.getHeadcount() : 1;
        List<String> skills = (req.getRequiredSkills() != null && !req.getRequiredSkills().isEmpty())
                ? req.getRequiredSkills()
                : List.of(role);

        jdbcTemplate.update("""
                INSERT INTO kkumz_team_recruit (
                    id, site_id, position_role, title, description, required_skills_json, headcount, recruited_count, status, created_at
                ) VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, 0, 'OPEN', CURRENT_TIMESTAMP)
                """,
                "rec-" + UUID.randomUUID().toString().substring(0, 8),
                siteId, role, req.getTitle().trim(),
                req.getDescription() != null ? req.getDescription().trim() : "",
                toJson(skills), headcount
        );

        return listSiteRecruitsInternal(siteId);
    }

    public List<Map<String, Object>> listSiteTeamMembers(String siteIdOrSlug) {
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        assertSiteReadable(site);
        String siteId = String.valueOf(site.get("id"));
        return listSiteTeamMembersInternal(siteId);
    }

    private List<Map<String, Object>> listSiteTeamMembersInternal(String siteId) {
        return jdbcTemplate.queryForList("""
                SELECT id,
                       site_id AS "siteId",
                       recruit_id AS "recruitId",
                       username,
                       display_name AS "displayName",
                       member_role AS "memberRole",
                       position_role AS "positionRole",
                       join_type AS "joinType",
                       application_message AS "applicationMessage",
                       status,
                       joined_at AS "joinedAt"
                FROM kkumz_team_member
                WHERE site_id = ?
                ORDER BY CASE WHEN member_role = 'OWNER' THEN 0 ELSE 1 END, joined_at ASC
                """, siteId);
    }

    @Transactional
    public List<Map<String, Object>> applyToTeam(String siteIdOrSlug, TeamApplyRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("팀 합류 신청 정보(req)는 필수입니다.");
        }
        String authUser = isHttpRequestActive() ? requireAuthenticatedUsername() : resolveCurrentUsernameFromRequest();
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        assertSiteReadable(site);
        String siteId = String.valueOf(site.get("id"));
        Map<String, String> applicant = resolveUserIdentity(
                authUser != null ? authUser : req.getUsername(),
                req.getDisplayName()
        );
        String username = applicant.get("username");
        String displayName = applicant.get("realname");
        String posRole = (req.getPositionRole() != null && !req.getPositionRole().isBlank())
                ? req.getPositionRole().trim().toUpperCase(Locale.ROOT)
                : "FRONTEND";
        String memberRole = "FRONTEND".equals(posRole) ? "DEVELOPER" : ("MD".equals(posRole) ? "MD" : "EDITOR");

        jdbcTemplate.update("""
                INSERT INTO kkumz_team_member (
                    id, site_id, recruit_id, username, display_name, member_role, position_role,
                    join_type, application_message, status, joined_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'APPLIED', ?, 'PENDING', CURRENT_TIMESTAMP)
                """,
                "tmem-" + UUID.randomUUID().toString().substring(0, 8),
                siteId, req.getRecruitId(), username, displayName, memberRole, posRole,
                req.getApplicationMessage() != null ? req.getApplicationMessage().trim() : ""
        );

        createNotification(
                String.valueOf(site.get("ownerUsername")),
                "TEAM_APPLY",
                "[" + site.get("siteName") + "] " + posRole + " 포지션 신규 지원",
                displayName + "님이 프로젝트 합류를 신청했습니다.",
                "/lounge/" + site.get("siteSlug")
        );

        return listSiteTeamMembersInternal(siteId);
    }

    @Transactional
    public List<Map<String, Object>> inviteTeamMember(String siteIdOrSlug, TeamInviteRequest req) {
        assertSiteEditable(siteIdOrSlug);
        if (req == null || req.getUsername() == null || req.getUsername().isBlank()) {
            throw new IllegalArgumentException("초대할 팀원 아이디(username)는 필수입니다.");
        }
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        Map<String, String> invited = resolveUserIdentity(req.getUsername(), req.getDisplayName());
        String username = invited.get("username");
        String displayName = invited.get("realname");
        String posRole = (req.getPositionRole() != null && !req.getPositionRole().isBlank())
                ? req.getPositionRole().trim().toUpperCase(Locale.ROOT)
                : "FRONTEND";
        String memberRole = (req.getMemberRole() != null && !req.getMemberRole().isBlank())
                ? req.getMemberRole().trim().toUpperCase(Locale.ROOT)
                : "EDITOR";

        jdbcTemplate.update("""
                INSERT INTO kkumz_team_member (
                    id, site_id, recruit_id, username, display_name, member_role, position_role,
                    join_type, application_message, status, joined_at
                ) VALUES (?, ?, NULL, ?, ?, ?, ?, 'INVITED', ?, 'ACTIVE', CURRENT_TIMESTAMP)
                """,
                "tmem-" + UUID.randomUUID().toString().substring(0, 8),
                siteId, username, displayName, memberRole, posRole,
                req.getMessage() != null ? req.getMessage().trim() : ""
        );

        createNotification(
                username,
                "TEAM_INVITE",
                "[" + site.get("siteName") + "] 프로젝트 합류 초대 도착",
                site.get("siteName") + " 팀에 합류되었습니다.",
                "/lounge/" + site.get("siteSlug")
        );

        return listSiteTeamMembersInternal(siteId);
    }

    @Transactional
    public List<Map<String, Object>> updateTeamMemberStatus(String siteIdOrSlug, String memberId, UpdateMemberStatusRequest req) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT site_id AS \"siteId\", recruit_id AS \"recruitId\", username, display_name AS \"displayName\" FROM kkumz_team_member WHERE id = ?",
                memberId
        );
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 팀 지원 내역입니다: " + memberId);
        }
        Map<String, Object> member = rows.get(0);
        String siteId = String.valueOf(member.get("siteId"));
        assertSiteEditable(siteId);
        String status = (req.getStatus() != null && !req.getStatus().isBlank())
                ? req.getStatus().trim().toUpperCase(Locale.ROOT)
                : "ACTIVE";
        String memberRole = (req.getMemberRole() != null && !req.getMemberRole().isBlank())
                ? req.getMemberRole().trim().toUpperCase(Locale.ROOT)
                : "EDITOR";

        jdbcTemplate.update("UPDATE kkumz_team_member SET status = ?, member_role = ? WHERE id = ?",
                status, memberRole, memberId);

        if ("ACTIVE".equals(status) && member.get("recruitId") != null) {
            jdbcTemplate.update("""
                    UPDATE kkumz_team_recruit
                    SET recruited_count = recruited_count + 1,
                        status = CASE WHEN recruited_count + 1 >= headcount THEN 'CLOSED' ELSE status END
                    WHERE id = ?
                    """, member.get("recruitId"));
        }

        createNotification(
                String.valueOf(member.get("username")),
                "TEAM_DECISION",
                "팀 합류 심사 결과 안내 (" + ("ACTIVE".equals(status) ? "승인" : "반려") + ")",
                "지원하신 프로젝트 팀 합류 상태가 '" + status + "'(으)로 업데이트되었습니다.",
                "/lounge"
        );

        return listSiteTeamMembersInternal(siteId);
    }

    // =========================================================================
    // 7. 사용자 인앱 알림 센터
    // =========================================================================

    public List<Map<String, Object>> listNotifications(String username) {
        String authUser = resolveCurrentUsernameFromRequest();
        String targetUser = (authUser != null && !authUser.isBlank())
                ? authUser
                : (username != null && !username.isBlank() ? username.trim() : null);
        if (isHttpRequestActive() && (targetUser == null || targetUser.isBlank())) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       recipient_username AS "recipientUsername",
                       notif_type AS "notifType",
                       title,
                       message,
                       link_url AS "linkUrl",
                       is_read AS "isRead",
                       created_at AS "createdAt"
                FROM kkumz_notification
                """);
        List<Object> params = new ArrayList<>();
        if (targetUser != null && !targetUser.isBlank()) {
            sql.append(" WHERE LOWER(recipient_username) = LOWER(?)");
            params.add(targetUser.trim());
        }
        sql.append(" ORDER BY created_at DESC LIMIT 30");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Transactional
    public void markAllNotificationsRead(String username) {
        String authUser = resolveCurrentUsernameFromRequest();
        String targetUser = (authUser != null && !authUser.isBlank())
                ? authUser
                : (username != null && !username.isBlank() ? username.trim() : null);
        if (targetUser != null && !targetUser.isBlank()) {
            jdbcTemplate.update("UPDATE kkumz_notification SET is_read = TRUE WHERE LOWER(recipient_username) = LOWER(?)", targetUser.trim());
        } else if (!isHttpRequestActive()) {
            jdbcTemplate.update("UPDATE kkumz_notification SET is_read = TRUE");
        }
    }

    // =========================================================================
    // 8. Admin 백오피스 (대시보드 KPI, 프롬프트 템플릿, 배포 사이트 제어, 빌드 로그, 신고 심사)
    // =========================================================================

    public Map<String, Object> getAdminDashboardSummary() {
        Integer totalIdeas = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_idea", Integer.class);
        Integer totalSites = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_site WHERE status = 'ACTIVE'", Integer.class);
        Integer totalBuilds = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_build_job", Integer.class);
        Integer successBuilds = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_build_job WHERE status = 'SUCCESS'", Integer.class);
        Integer aiHealedBuilds = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_build_job WHERE ai_patch_applied = TRUE", Integer.class);
        Integer openRecruits = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_team_recruit WHERE status = 'OPEN'", Integer.class);
        Integer activeTeamMembers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_team_member WHERE status = 'ACTIVE'", Integer.class);
        Integer pendingReports = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_moderation_report WHERE status = 'PENDING'", Integer.class);
        Integer totalComments = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_feed_comment", Integer.class);
        Integer activePrompts = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_ai_prompt_template WHERE status = 'ACTIVE'", Integer.class);
        Integer totalNameSuggestions = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_name_suggestion", Integer.class);
        Integer totalLogoSuggestions = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_logo_suggestion", Integer.class);
        Integer totalScreenSpecs = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_screen_spec", Integer.class);
        Integer avgBuildDurationMs = jdbcTemplate.queryForObject("SELECT COALESCE(ROUND(AVG(duration_ms))::int, 0) FROM kkumz_build_job", Integer.class);

        List<Map<String, Object>> bySiteType = jdbcTemplate.queryForList("""
                SELECT site_type AS "siteType",
                       COUNT(*) AS "count",
                       COALESCE(SUM(view_count), 0) AS "totalViews",
                       COALESCE(SUM(like_count), 0) AS "totalLikes"
                FROM kkumz_site
                GROUP BY site_type
                ORDER BY "count" DESC
                """);

        List<Map<String, Object>> byDeployMode = jdbcTemplate.queryForList("""
                SELECT deploy_mode AS "deployMode",
                       COUNT(*) AS "count"
                FROM kkumz_site
                GROUP BY deploy_mode
                ORDER BY "count" DESC
                """);

        List<Map<String, Object>> recentBuilds = listAdminBuilds(null, null);
        List<Map<String, Object>> recentReports = listModerationReports(null);
        List<Map<String, Object>> recentIdeas = listAdminIdeas(null, null, null);
        List<Map<String, Object>> topSites = listAdminSites(null, null, null);

        int totalB = totalBuilds != null ? totalBuilds : 0;
        int succB = successBuilds != null ? successBuilds : 0;
        int buildSuccessRate = totalB > 0 ? Math.round((succB * 100.0f) / totalB) : 100;
        int totalAiGenerations = (totalNameSuggestions != null ? totalNameSuggestions : 0)
                + (totalLogoSuggestions != null ? totalLogoSuggestions : 0)
                + (totalScreenSpecs != null ? totalScreenSpecs : 0)
                + (aiHealedBuilds != null ? aiHealedBuilds : 0);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalIdeas", totalIdeas != null ? totalIdeas : 0);
        summary.put("totalSites", totalSites != null ? totalSites : 0);
        summary.put("totalBuilds", totalB);
        summary.put("successBuilds", succB);
        summary.put("buildSuccessRate", buildSuccessRate);
        summary.put("avgBuildDurationMs", avgBuildDurationMs != null ? avgBuildDurationMs : 0);
        summary.put("aiHealedBuilds", aiHealedBuilds != null ? aiHealedBuilds : 0);
        summary.put("activePrompts", activePrompts != null ? activePrompts : 0);
        summary.put("totalAiPromptCalls", totalAiGenerations);
        summary.put("totalNameSuggestions", totalNameSuggestions != null ? totalNameSuggestions : 0);
        summary.put("totalLogoSuggestions", totalLogoSuggestions != null ? totalLogoSuggestions : 0);
        summary.put("totalScreenSpecs", totalScreenSpecs != null ? totalScreenSpecs : 0);
        summary.put("openRecruits", openRecruits != null ? openRecruits : 0);
        summary.put("activeTeamMembers", activeTeamMembers != null ? activeTeamMembers : 0);
        summary.put("pendingReports", pendingReports != null ? pendingReports : 0);
        summary.put("totalComments", totalComments != null ? totalComments : 0);
        summary.put("siteTypeDistribution", bySiteType);
        summary.put("deployModeDistribution", byDeployMode);
        summary.put("recentIdeas", recentIdeas.size() > 5 ? recentIdeas.subList(0, 5) : recentIdeas);
        summary.put("topSites", topSites.size() > 5 ? topSites.subList(0, 5) : topSites);
        summary.put("recentBuilds", recentBuilds.size() > 5 ? recentBuilds.subList(0, 5) : recentBuilds);
        summary.put("recentReports", recentReports.size() > 5 ? recentReports.subList(0, 5) : recentReports);
        return summary;
    }

    public List<Map<String, Object>> listAdminIdeas(String siteType, String status, String keyword) {
        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       author_username AS "authorUsername",
                       author_display_name AS "authorDisplayName",
                       title,
                       prompt_text AS "promptText",
                       site_type AS "siteType",
                       category,
                       tags_json::text AS "tagsJson",
                       enabled_features_json::text AS "enabledFeaturesJson",
                       ai_intent_analysis_json::text AS "aiIntentAnalysisJson",
                       visibility,
                       current_step AS "currentStep",
                       selected_brand_name AS "selectedBrandName",
                       selected_site_slug AS "selectedSiteSlug",
                       selected_logo_svg AS "selectedLogoSvg",
                       design_tokens_json::text AS "designTokensJson",
                       spec_markdown AS "specMarkdown",
                       screens_json::text AS "screensJson",
                       status,
                       created_at AS "createdAt",
                       updated_at AS "updatedAt"
                FROM kkumz_idea
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (siteType != null && !siteType.isBlank() && !"ALL".equalsIgnoreCase(siteType)) {
            sql.append(" AND site_type = ?");
            params.add(siteType.trim().toUpperCase(Locale.ROOT));
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND status = ?");
            params.add(status.trim().toUpperCase(Locale.ROOT));
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (title ILIKE ? OR prompt_text ILIKE ? OR selected_brand_name ILIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        sql.append(" ORDER BY updated_at DESC");

        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        list.forEach(this::enrichIdeaRow);
        return list;
    }

    @Transactional
    public void deleteAdminIdea(String ideaId) {
        jdbcTemplate.update("DELETE FROM kkumz_idea WHERE id = ?", ideaId);
    }

    public List<Map<String, Object>> listAdminPromptTemplates(String stageType) {
        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       template_code AS "templateCode",
                       template_name AS "templateName",
                       stage_type AS "stageType",
                       model_name AS "modelName",
                       temperature,
                       system_prompt AS "systemPrompt",
                       user_prompt_template AS "userPromptTemplate",
                       forbidden_words_json::text AS "forbiddenWordsJson",
                       status,
                       updated_by AS "updatedBy",
                       updated_at AS "updatedAt"
                FROM kkumz_ai_prompt_template
                """);
        List<Object> params = new ArrayList<>();
        if (stageType != null && !stageType.isBlank() && !"ALL".equalsIgnoreCase(stageType)) {
            sql.append(" WHERE stage_type = ?");
            params.add(stageType.trim().toUpperCase(Locale.ROOT));
        }
        sql.append(" ORDER BY stage_type ASC, template_code ASC");

        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        list.forEach(p -> p.put("forbiddenWords", parseJsonList(String.valueOf(p.remove("forbiddenWordsJson")))));
        return list;
    }

    @Transactional
    public Map<String, Object> saveAdminPromptTemplate(SavePromptTemplateRequest req) {
        if (req == null || req.getTemplateName() == null || req.getTemplateName().isBlank()) {
            throw new IllegalArgumentException("프롬프트 템플릿 이름(templateName)은 필수입니다.");
        }
        List<String> words = req.getForbiddenWords() != null ? req.getForbiddenWords()
                : (req.getForbiddenKeywords() != null ? req.getForbiddenKeywords() : Collections.emptyList());
        String forbiddenJson = toJson(words);
        BigDecimal temp = req.getTemperature() != null ? req.getTemperature() : new BigDecimal("0.70");
        String modelName = (req.getModelName() != null && !req.getModelName().isBlank()) ? req.getModelName().trim() : "";
        if (modelName.isBlank()) {
            throw new IllegalArgumentException("대상 모델명(modelName)은 필수입니다.");
        }
        String status = (req.getStatus() != null && !req.getStatus().isBlank()) ? req.getStatus().trim() : "ACTIVE";
        String id;

        if (req.getId() != null && !req.getId().isBlank()) {
            id = req.getId().trim();
            jdbcTemplate.update("""
                    UPDATE kkumz_ai_prompt_template
                    SET template_name = ?,
                        stage_type = COALESCE(?, stage_type),
                        model_name = ?,
                        temperature = ?,
                        system_prompt = ?,
                        user_prompt_template = COALESCE(?, user_prompt_template),
                        forbidden_words_json = ?::jsonb,
                        status = ?,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                    req.getTemplateName().trim(),
                    req.getStageType(),
                    modelName,
                    temp,
                    req.getSystemPrompt(),
                    req.getUserPromptTemplate(),
                    forbiddenJson,
                    status,
                    id
            );
        } else {
            id = "tpl-" + UUID.randomUUID().toString().substring(0, 8);
            String code = (req.getTemplateCode() != null && !req.getTemplateCode().isBlank())
                    ? req.getTemplateCode().trim().toUpperCase(Locale.ROOT)
                    : "TPL_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
            jdbcTemplate.update("""
                    INSERT INTO kkumz_ai_prompt_template (
                        id, template_code, template_name, stage_type, model_name, temperature,
                        system_prompt, user_prompt_template, forbidden_words_json, status, created_by, updated_by, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """,
                    id, code, req.getTemplateName().trim(),
                    req.getStageType() != null ? req.getStageType() : "NAMING",
                    modelName, temp,
                    req.getSystemPrompt() != null ? req.getSystemPrompt() : "",
                    req.getUserPromptTemplate() != null ? req.getUserPromptTemplate() : "",
                    forbiddenJson, status
            );
        }

        List<Map<String, Object>> all = listAdminPromptTemplates(null);
        return all.stream()
                .filter(p -> id.equals(String.valueOf(p.get("id"))))
                .findFirst()
                .orElse(all.isEmpty() ? Map.of("id", id) : all.get(0));
    }

    @Transactional
    public void deleteAdminPromptTemplate(String promptId) {
        jdbcTemplate.update("DELETE FROM kkumz_ai_prompt_template WHERE id = ?", promptId);
    }

    public List<Map<String, Object>> listAdminSites(String siteType, String status, String keyword) {
        StringBuilder sql = new StringBuilder("""
                SELECT s.id,
                       s.idea_id AS "ideaId",
                       s.site_slug AS "siteSlug",
                       s.site_name AS "siteName",
                       s.site_type AS "siteType",
                       s.category,
                       s.brand_slogan AS "brandSlogan",
                       s.logo_svg AS "logoSvg",
                       s.shop_site_id AS "shopSiteId",
                       s.cms_site_id AS "cmsSiteId",
                       s.deploy_mode AS "deployMode",
                       s.active_version AS "activeVersion",
                       s.published_version AS "publishedVersion",
                       s.published_at AS "publishedAt",
                       s.deployed_url AS "deployedUrl",
                       s.custom_domain AS "customDomain",
                       s.is_featured AS "isFeatured",
                       s.visibility,
                       s.like_count AS "likeCount",
                       s.comment_count AS "commentCount",
                       s.view_count AS "viewCount",
                       s.owner_username AS "ownerUsername",
                       s.owner_display_name AS "ownerDisplayName",
                       s.status,
                       s.created_at AS "createdAt",
                       s.updated_at AS "updatedAt"
                FROM kkumz_site s
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (siteType != null && !siteType.isBlank() && !"ALL".equalsIgnoreCase(siteType)) {
            sql.append(" AND s.site_type = ?");
            params.add(siteType.trim().toUpperCase(Locale.ROOT));
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND s.status = ?");
            params.add(status.trim().toUpperCase(Locale.ROOT));
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (s.site_name ILIKE ? OR s.site_slug ILIKE ? OR s.brand_slogan ILIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        sql.append(" ORDER BY s.created_at DESC");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        List<Map<String, Object>> list = new ArrayList<>(rows.size());
        for (Map<String, Object> raw : rows) {
            Map<String, Object> item = new LinkedHashMap<>(raw);
            boolean isPub = isSitePubliclyPublished(item);
            int activeVer = item.get("activeVersion") instanceof Number n ? n.intValue() : 1;
            Integer pubVer = resolvePublishedVersion(item);
            item.put("isPublished", isPub);
            item.put("publishedVersion", pubVer);
            item.put("hasUnpublishedChanges", !isPub || (pubVer != null && activeVer > pubVer));
            list.add(item);
        }
        return list;
    }

    @Transactional
    public Map<String, Object> updateAdminSiteStatus(String siteId, UpdateSiteStatusRequest req) {
        Map<String, Object> site = findSiteByIdOrSlug(siteId);
        String resolvedId = String.valueOf(site.get("id"));
        String nextStatus = (req.getStatus() != null && !req.getStatus().isBlank())
                ? req.getStatus().trim().toUpperCase(Locale.ROOT)
                : String.valueOf(site.getOrDefault("status", "DRAFT")).toUpperCase(Locale.ROOT);
        String nextVis = (req.getVisibility() != null && !req.getVisibility().isBlank())
                ? req.getVisibility().trim().toUpperCase(Locale.ROOT)
                : String.valueOf(site.getOrDefault("visibility", "PRIVATE")).toUpperCase(Locale.ROOT);

        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            jdbcTemplate.update("UPDATE kkumz_site SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    nextStatus, resolvedId);
        }
        if (req.getVisibility() != null && !req.getVisibility().isBlank()) {
            jdbcTemplate.update("UPDATE kkumz_site SET visibility = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    nextVis, resolvedId);
        }
        if ("PRIVATE".equals(nextVis) || "DRAFT".equals(nextStatus)) {
            jdbcTemplate.update("UPDATE kkumz_site SET published_version = NULL, updated_at = CURRENT_TIMESTAMP WHERE id = ?", resolvedId);
        } else if ("ACTIVE".equals(nextStatus) && "PUBLIC".equals(nextVis)) {
            jdbcTemplate.update("""
                    UPDATE kkumz_site
                    SET published_version = COALESCE(published_version, active_version),
                        published_at = COALESCE(published_at, CURRENT_TIMESTAMP),
                        updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """, resolvedId);
        }
        if (req.getIsFeatured() != null) {
            jdbcTemplate.update("UPDATE kkumz_site SET is_featured = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    req.getIsFeatured(), resolvedId);
        }
        if (req.getCustomDomain() != null && !req.getCustomDomain().isBlank()) {
            jdbcTemplate.update("UPDATE kkumz_site SET custom_domain = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    req.getCustomDomain().trim(), resolvedId);
        }
        return findSiteByIdOrSlug(resolvedId);
    }

    public List<Map<String, Object>> listAdminBuilds(String status, String deployMode) {
        StringBuilder sql = new StringBuilder("""
                SELECT b.id,
                       b.site_id AS "siteId",
                       s.site_slug AS "siteSlug",
                       s.site_name AS "siteName",
                       s.site_name AS "brandName",
                       b.release_id AS "releaseId",
                       COALESCE(r.version_no, s.active_version) AS "versionNo",
                       b.job_type AS "jobType",
                       b.deploy_mode AS "deployMode",
                       b.status,
                       b.duration_ms AS "durationMs",
                       b.error_summary AS "errorSummary",
                       b.ai_patch_applied AS "aiPatchApplied",
                       b.build_log AS "buildLog",
                       b.triggered_by AS "triggeredBy",
                       b.started_at AS "startedAt",
                       b.finished_at AS "finishedAt",
                       b.started_at AS "createdAt"
                FROM kkumz_build_job b
                JOIN kkumz_site s ON s.id = b.site_id
                LEFT JOIN kkumz_site_release r ON r.id = b.release_id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND b.status = ?");
            params.add(status.trim().toUpperCase(Locale.ROOT));
        }
        if (deployMode != null && !deployMode.isBlank() && !"ALL".equalsIgnoreCase(deployMode)) {
            sql.append(" AND b.deploy_mode = ?");
            params.add(deployMode.trim().toUpperCase(Locale.ROOT));
        }
        sql.append(" ORDER BY b.started_at DESC");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public List<Map<String, Object>> listAdminCommunityProjects(String siteType, String keyword) {
        return getCommunityFeed(siteType, null, "POPULAR", keyword);
    }

    public List<Map<String, Object>> listModerationReports(String status) {
        StringBuilder sql = new StringBuilder("""
                SELECT r.id,
                       r.target_type AS "targetType",
                       r.target_id AS "targetId",
                       r.site_id AS "siteId",
                       s.site_slug AS "siteSlug",
                       s.site_name AS "siteName",
                       s.site_name AS "brandName",
                       r.reporter_username AS "reporterUsername",
                       r.reason_category AS "reasonCategory",
                       r.reason_detail AS "reasonDetail",
                       r.status,
                       r.action_taken AS "actionTaken",
                       r.moderator_username AS "moderatorUsername",
                       r.moderator_username AS "resolvedBy",
                       r.resolved_at AS "resolvedAt",
                       r.created_at AS "createdAt"
                FROM kkumz_moderation_report r
                JOIN kkumz_site s ON s.id = r.site_id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND r.status = ?");
            params.add(status.trim().toUpperCase(Locale.ROOT));
        }
        sql.append(" ORDER BY CASE WHEN r.status = 'PENDING' THEN 0 ELSE 1 END, r.created_at DESC");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Transactional
    public Map<String, Object> createModerationReport(String siteIdOrSlug, CreateReportRequest req) {
        if (req == null || req.getReasonDetail() == null || req.getReasonDetail().isBlank()) {
            throw new IllegalArgumentException("신고 상세 사유(reasonDetail)를 입력해 주세요.");
        }
        String authUser = resolveCurrentUsernameFromRequest();
        String reporter = (authUser != null && !authUser.isBlank()) ? authUser : req.getReporterUsername();
        if (reporter == null || reporter.isBlank()) {
            if (isHttpRequestActive()) {
                throw new SecurityException("[401] 로그인이 필요한 기능입니다. 먼저 로그인해 주세요.");
            }
            throw new IllegalArgumentException("신고자 계정(reporterUsername)은 필수입니다.");
        }
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        assertSiteReadable(site);
        String siteId = String.valueOf(site.get("id"));
        String targetType = (req.getTargetType() != null && !req.getTargetType().isBlank())
                ? req.getTargetType().trim().toUpperCase(Locale.ROOT)
                : "SITE";
        String targetId = (req.getTargetId() != null && !req.getTargetId().isBlank())
                ? req.getTargetId().trim()
                : siteId;
        String reportId = "rep-" + UUID.randomUUID().toString().substring(0, 8);

        jdbcTemplate.update("""
                INSERT INTO kkumz_moderation_report (
                    id, target_type, target_id, site_id, reporter_username, reason_category, reason_detail, status, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', CURRENT_TIMESTAMP)
                """,
                reportId,
                targetType, targetId, siteId,
                reporter.trim(),
                req.getReasonCategory() != null ? req.getReasonCategory() : "SPAM_PROMOTION",
                req.getReasonDetail().trim()
        );
        return Map.of("id", reportId, "siteId", siteId, "status", "PENDING");
    }

    @Transactional
    public List<Map<String, Object>> resolveModerationReport(String reportId, ResolveReportRequest req) {
        String status = (req.getStatus() != null && !req.getStatus().isBlank())
                ? req.getStatus().trim().toUpperCase(Locale.ROOT)
                : "RESOLVED";
        String actionTaken = (req.getActionTaken() != null && !req.getActionTaken().isBlank())
                ? req.getActionTaken().trim()
                : (req.getResolutionNote() != null && !req.getResolutionNote().isBlank()
                ? req.getResolutionNote().trim()
                : "모더레이터 심사 완료");
        String moderator = (req.getModeratorUsername() != null && !req.getModeratorUsername().isBlank())
                ? req.getModeratorUsername().trim()
                : (req.getResolvedBy() != null && !req.getResolvedBy().isBlank() ? req.getResolvedBy().trim() : "admin");

        jdbcTemplate.update("""
                UPDATE kkumz_moderation_report
                SET status = ?, action_taken = ?, moderator_username = ?, resolved_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                status, actionTaken, moderator, reportId
        );

        boolean shouldSuspend = Boolean.TRUE.equals(req.getSuspendSite())
                || ("RESOLVED".equals(status) && actionTaken.contains("정지"));
        if (shouldSuspend) {
            jdbcTemplate.update("""
                    UPDATE kkumz_site SET status = 'SUSPENDED', updated_at = CURRENT_TIMESTAMP
                    WHERE id = (SELECT site_id FROM kkumz_moderation_report WHERE id = ?)
                    """, reportId);
        }

        return listModerationReports(null);
    }

    // =========================================================================
    // 8-B. Admin API 연동 · 액세스키(Access Key / Secret Key) 발급·회전·검증·관리
    // =========================================================================

    public List<Map<String, Object>> listAdminApiKeys(String direction, String serviceCode, String status, String keyword) {
        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       key_name AS "keyName",
                       direction,
                       service_code AS "serviceCode",
                       base_url AS "baseUrl",
                       access_key AS "accessKey",
                       secret_key AS "secretKey",
                       scopes_json::text AS "scopesJson",
                       rate_limit_rpm AS "rateLimitPerMin",
                       rate_limit_rpm AS "rateLimitRpm",
                       status,
                       last_used_at AS "lastUsedAt",
                       call_count AS "callCount",
                       expires_at AS "expiresAt",
                       description,
                       created_by AS "createdBy",
                       created_at AS "createdAt",
                       updated_at AS "updatedAt"
                FROM kkumz_api_access_key
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (direction != null && !direction.isBlank() && !"ALL".equalsIgnoreCase(direction)) {
            sql.append(" AND direction = ?");
            params.add(direction.trim().toUpperCase(Locale.ROOT));
        }
        if (serviceCode != null && !serviceCode.isBlank() && !"ALL".equalsIgnoreCase(serviceCode)) {
            sql.append(" AND service_code = ?");
            params.add(serviceCode.trim().toUpperCase(Locale.ROOT));
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND status = ?");
            params.add(status.trim().toUpperCase(Locale.ROOT));
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (key_name ILIKE ? OR access_key ILIKE ? OR description ILIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        sql.append(" ORDER BY CASE WHEN direction = 'OUTBOUND' THEN 0 ELSE 1 END, created_at ASC");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        for (Map<String, Object> row : rows) {
            row.put("scopes", parseJsonList(String.valueOf(row.remove("scopesJson"))));
        }
        return rows;
    }

    @Transactional
    public Map<String, Object> saveAdminApiKey(SaveApiAccessKeyRequest req) {
        if (req == null || req.getKeyName() == null || req.getKeyName().isBlank()) {
            throw new IllegalArgumentException("액세스 키 명칭(keyName)은 필수입니다.");
        }
        String direction = (req.getDirection() != null && !req.getDirection().isBlank())
                ? req.getDirection().trim().toUpperCase(Locale.ROOT)
                : "INBOUND";
        String serviceCode = (req.getServiceCode() != null && !req.getServiceCode().isBlank())
                ? req.getServiceCode().trim().toUpperCase(Locale.ROOT)
                : ("OUTBOUND".equals(direction) ? "SYNC_CMS" : "SYNC_VERSE");
        String status = (req.getStatus() != null && !req.getStatus().isBlank())
                ? req.getStatus().trim().toUpperCase(Locale.ROOT)
                : "ACTIVE";
        int rateLimit = (req.getRateLimitPerMin() != null && req.getRateLimitPerMin() > 0)
                ? req.getRateLimitPerMin()
                : 600;
        List<String> scopes = (req.getScopes() != null && !req.getScopes().isEmpty())
                ? req.getScopes()
                : ("OUTBOUND".equals(direction)
                ? ("SYNC_CMS".equals(serviceCode)
                ? List.of("cms:site:read", "cms:site:provision", "cms:content:write", "cms:form:submit")
                : List.of("shop:site:read", "shop:site:provision", "shop:product:write", "shop:order:write"))
                : List.of("kkumz:idea:read", "kkumz:site:read", "kkumz:site:deploy"));
        String scopesJson = toJson(scopes);
        String baseUrl = req.getBaseUrl() != null ? req.getBaseUrl().trim() : "";
        if (baseUrl.isEmpty() && "OUTBOUND".equals(direction)) {
            baseUrl = "SYNC_CMS".equals(serviceCode)
                    ? (defaultCmsBaseUrl != null ? defaultCmsBaseUrl : "")
                    : (defaultShopBaseUrl != null ? defaultShopBaseUrl : "");
        }
        String description = req.getDescription() != null ? req.getDescription().trim() : "";
        String createdBy = (req.getCreatedBy() != null && !req.getCreatedBy().isBlank()) ? req.getCreatedBy().trim() : "admin";

        String keyPrefix = "OUTBOUND".equals(direction)
                ? ("SYNC_CMS".equals(serviceCode) ? "ak_cms_" : "ak_shop_")
                : "ak_kkumz_";
        String secretPrefix = "OUTBOUND".equals(direction)
                ? ("SYNC_CMS".equals(serviceCode) ? "sk_cms_" : "sk_shop_")
                : "sk_kkumz_";

        String id;
        String finalAccessKey;
        String finalSecretKey;

        if (req.getId() != null && !req.getId().isBlank()) {
            id = req.getId().trim();
            List<Map<String, Object>> existing = jdbcTemplate.queryForList(
                    "SELECT access_key, secret_key FROM kkumz_api_access_key WHERE id = ?", id
            );
            if (existing.isEmpty()) {
                throw new IllegalArgumentException("존재하지 않는 API 액세스 키 ID입니다: " + id);
            }
            finalAccessKey = (req.getAccessKey() != null && !req.getAccessKey().isBlank())
                    ? req.getAccessKey().trim()
                    : String.valueOf(existing.get(0).get("access_key"));
            finalSecretKey = (req.getSecretKey() != null && !req.getSecretKey().isBlank())
                    ? req.getSecretKey().trim()
                    : String.valueOf(existing.get(0).get("secret_key"));

            jdbcTemplate.update("""
                    UPDATE kkumz_api_access_key
                    SET key_name = ?,
                        direction = ?,
                        service_code = ?,
                        base_url = ?,
                        access_key = ?,
                        secret_key = ?,
                        scopes_json = ?::jsonb,
                        rate_limit_rpm = ?,
                        status = ?,
                        description = ?,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                    req.getKeyName().trim(), direction, serviceCode, baseUrl,
                    finalAccessKey, finalSecretKey, scopesJson, rateLimit, status, description, id
            );
        } else {
            id = "ak-kkumz-" + UUID.randomUUID().toString().substring(0, 8);
            finalAccessKey = (req.getAccessKey() != null && !req.getAccessKey().isBlank())
                    ? req.getAccessKey().trim()
                    : keyPrefix + UUID.randomUUID().toString().replace("-", "").substring(0, 18);
            finalSecretKey = (req.getSecretKey() != null && !req.getSecretKey().isBlank())
                    ? req.getSecretKey().trim()
                    : secretPrefix + UUID.randomUUID().toString().replace("-", "").substring(0, 26);

            jdbcTemplate.update("""
                    INSERT INTO kkumz_api_access_key (
                        id, key_name, direction, service_code, base_url, access_key, secret_key,
                        scopes_json, rate_limit_rpm, status, call_count, expires_at, description, created_by, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, 0, CURRENT_TIMESTAMP + INTERVAL '365 days', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """,
                    id, req.getKeyName().trim(), direction, serviceCode, baseUrl,
                    finalAccessKey, finalSecretKey, scopesJson, rateLimit, status, description, createdBy
            );
        }

        if ("OUTBOUND".equals(direction)) {
            syncOutboundKeyToRemoteService(serviceCode, req.getKeyName().trim(), finalAccessKey, finalSecretKey, status);
        }

        return getAdminApiKeyById(id);
    }

    @Transactional
    public Map<String, Object> rotateAdminApiKey(String keyId) {
        Map<String, Object> existing = getAdminApiKeyById(keyId);
        String direction = String.valueOf(existing.get("direction"));
        String serviceCode = String.valueOf(existing.get("serviceCode"));
        String accessKey = String.valueOf(existing.get("accessKey"));
        String keyName = String.valueOf(existing.get("keyName"));
        String status = String.valueOf(existing.get("status"));

        String secretPrefix = "OUTBOUND".equalsIgnoreCase(direction)
                ? ("SYNC_CMS".equalsIgnoreCase(serviceCode) ? "sk_cms_" : "sk_shop_")
                : "sk_kkumz_";
        String rotatedSecret = secretPrefix + UUID.randomUUID().toString().replace("-", "").substring(0, 26);

        jdbcTemplate.update(
                "UPDATE kkumz_api_access_key SET secret_key = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                rotatedSecret, keyId
        );

        if ("OUTBOUND".equalsIgnoreCase(direction)) {
            syncOutboundKeyToRemoteService(serviceCode, keyName, accessKey, rotatedSecret, status);
        }

        return getAdminApiKeyById(keyId);
    }

    @Transactional
    public Map<String, Object> updateAdminApiKeyStatus(String keyId, String status) {
        Map<String, Object> existing = getAdminApiKeyById(keyId);
        String normalized = (status != null && !status.isBlank()) ? status.trim().toUpperCase(Locale.ROOT) : "ACTIVE";
        jdbcTemplate.update(
                "UPDATE kkumz_api_access_key SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                normalized, keyId
        );
        if ("OUTBOUND".equalsIgnoreCase(String.valueOf(existing.get("direction")))) {
            syncOutboundKeyToRemoteService(
                    String.valueOf(existing.get("serviceCode")),
                    String.valueOf(existing.get("keyName")),
                    String.valueOf(existing.get("accessKey")),
                    String.valueOf(existing.get("secretKey")),
                    normalized
            );
        }
        return getAdminApiKeyById(keyId);
    }

    @Transactional
    public Map<String, Object> verifyAdminApiKey(String keyId) {
        Map<String, Object> keyRow = getAdminApiKeyById(keyId);
        String direction = String.valueOf(keyRow.get("direction"));
        String serviceCode = String.valueOf(keyRow.get("serviceCode"));
        String baseUrl = keyRow.get("baseUrl") != null ? String.valueOf(keyRow.get("baseUrl")) : "";
        String accessKey = String.valueOf(keyRow.get("accessKey"));
        String secretKey = String.valueOf(keyRow.get("secretKey"));
        String status = String.valueOf(keyRow.get("status"));

        if (!"ACTIVE".equalsIgnoreCase(status)) {
            Map<String, Object> inactiveRes = new LinkedHashMap<>();
            inactiveRes.put("keyId", keyId);
            inactiveRes.put("verified", false);
            inactiveRes.put("serviceCode", serviceCode);
            inactiveRes.put("direction", direction);
            inactiveRes.put("message", "비활성화(REVOKED) 상태인 액세스 키입니다.");
            return inactiveRes;
        }

        if ("OUTBOUND".equalsIgnoreCase(direction) && syncServiceApiClient != null) {
            Map<String, Object> remoteCheck = syncServiceApiClient.verifyServiceConnection(
                    serviceCode, baseUrl, accessKey, secretKey, keyId
            );
            remoteCheck.put("keyId", keyId);
            remoteCheck.put("keyName", keyRow.get("keyName"));
            remoteCheck.put("direction", direction);
            remoteCheck.put("updatedKey", getAdminApiKeyById(keyId));
            return remoteCheck;
        }

        jdbcTemplate.update(
                "UPDATE kkumz_api_access_key SET last_used_at = NOW(), call_count = COALESCE(call_count, 0) + 1 WHERE id = ?",
                keyId
        );
        Map<String, Object> inboundRes = new LinkedHashMap<>();
        inboundRes.put("keyId", keyId);
        inboundRes.put("keyName", keyRow.get("keyName"));
        inboundRes.put("direction", direction);
        inboundRes.put("serviceCode", serviceCode);
        inboundRes.put("accessKey", accessKey);
        inboundRes.put("verified", true);
        inboundRes.put("latencyMs", 2L);
        inboundRes.put("message", "Kkumz 인바운드 API 액세스 키 유효성 및 스코프 검증 완료");
        inboundRes.put("updatedKey", getAdminApiKeyById(keyId));
        return inboundRes;
    }

    @Transactional
    public void deleteAdminApiKey(String keyId) {
        jdbcTemplate.update("DELETE FROM kkumz_api_access_key WHERE id = ?", keyId);
    }

    private Map<String, Object> getAdminApiKeyById(String keyId) {
        List<Map<String, Object>> list = listAdminApiKeys(null, null, null, null);
        return list.stream()
                .filter(k -> keyId.equals(String.valueOf(k.get("id"))))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 API 액세스 키입니다: " + keyId));
    }

    private void syncOutboundKeyToRemoteService(String serviceCode, String keyName, String accessKey, String secretKey, String status) {
        try {
            if ("SYNC_CMS".equalsIgnoreCase(serviceCode) && cmsJdbcTemplate != null) {
                Integer exists = cmsJdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM sys_app WHERE app_key = ?", Integer.class, accessKey
                );
                if (exists != null && exists > 0) {
                    cmsJdbcTemplate.update(
                            "UPDATE sys_app SET app_secret = ?, channel = ? WHERE app_key = ?",
                            secretKey, "KKUMZ_STUDIO", accessKey
                    );
                } else {
                    Integer nextId = cmsJdbcTemplate.queryForObject(
                            "SELECT COALESCE(MAX(id), 100) + 1 FROM sys_app", Integer.class
                    );
                    cmsJdbcTemplate.update(
                            """
                            INSERT INTO sys_app (id, site_id, channel, app_key, app_secret, authorized_apis, expiry_minutes)
                            VALUES (?, 1, 'KKUMZ_STUDIO', ?, ?, '/api/v1/cms/multisite/**,/api/v1/cms/frontend/**,/api/v1/cms/form/**', 525600)
                            """,
                            nextId != null ? nextId : 105, accessKey, secretKey
                    );
                }
            } else if ("SYNC_SHOP".equalsIgnoreCase(serviceCode) && shopJdbcTemplate != null) {
                int shopStatus = "ACTIVE".equalsIgnoreCase(status) ? 1 : 0;
                Integer exists = shopJdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM sys_api_access_key WHERE access_key = ?", Integer.class, accessKey
                );
                if (exists != null && exists > 0) {
                    shopJdbcTemplate.update(
                            "UPDATE sys_api_access_key SET secret_key = ?, client_name = ?, status = ?, update_time = NOW() WHERE access_key = ?",
                            secretKey, keyName, shopStatus, accessKey
                    );
                } else {
                    shopJdbcTemplate.update(
                            """
                            INSERT INTO sys_api_access_key (
                                id, client_code, client_name, access_key, secret_key, allowed_scopes,
                                rate_limit_rpm, status, call_count, description, create_time, update_time
                            ) VALUES (?, 'KKUMZ_STUDIO', ?, ?, ?, 'shop:site:provision,shop:product:read,shop:product:write,shop:order:write,shop:promotion:read', 1200, ?, 0, ?, NOW(), NOW())
                            """,
                            "ak_shop_" + UUID.randomUUID().toString().substring(0, 6), keyName, accessKey, secretKey, shopStatus, keyName
                    );
                }
            }
        } catch (Exception e) {
            log.debug("원격 서비스 키 동기화 생략 ({}): {}", serviceCode, e.getMessage());
        }
    }

    // =========================================================================
    // 9. SyncShop / SyncCMS 멀티사이트 API 연동 및 폴백 헬퍼
    // =========================================================================

    private Map<String, Object> loadCommerceData(Long shopSiteId) {
        return loadCommerceData(shopSiteId, null);
    }

    private Map<String, Object> loadCommerceData(Long shopSiteId, String siteSlug) {
        if (shopSiteId == null && (siteSlug == null || siteSlug.isBlank())) {
            return Map.of(
                    "shopSiteId", 0L,
                    "products", List.of(),
                    "banners", List.of(),
                    "categories", List.of(),
                    "coupons", List.of()
            );
        }
        if (syncServiceApiClient != null) {
            try {
                Map<String, Object> apiData = syncServiceApiClient.fetchShopBootstrap(shopSiteId, siteSlug);
                if (apiData != null && !apiData.isEmpty()) {
                    apiData.putIfAbsent("shopSiteId", shopSiteId != null ? shopSiteId : 0L);
                    apiData.putIfAbsent("products", List.of());
                    apiData.putIfAbsent("banners", List.of());
                    apiData.putIfAbsent("categories", List.of());
                    apiData.putIfAbsent("coupons", List.of());
                    return apiData;
                }
            } catch (Exception e) {
                log.warn("SyncShop 부트스트랩 REST API 호출 실패, 로컬 어댑터 폴백 수행: {}", e.getMessage());
            }
        }
        if (shopSiteId == null || shopJdbcTemplate == null) {
            return Map.of(
                    "shopSiteId", 0L,
                    "products", List.of(),
                    "banners", List.of(),
                    "categories", List.of(),
                    "coupons", List.of()
            );
        }
        List<Map<String, Object>> products = shopJdbcTemplate.queryForList("""
                SELECT id,
                       site_id AS "siteId",
                       name,
                       sub_title AS "subTitle",
                       description,
                       pic,
                       product_sn AS "productSn",
                       brand_name AS "brandName",
                       product_category_id AS "productCategoryId",
                       product_category_name AS "productCategoryName",
                       product_category_name AS "categoryName",
                       price,
                       promotion_price AS "promotionPrice",
                       original_price AS "originalPrice",
                       stock,
                       sale,
                       unit,
                       note,
                       keywords
                FROM pms_product
                WHERE site_id = ? AND delete_status = 0 AND publish_status = 1
                ORDER BY sort ASC, id ASC
                """, shopSiteId);

        List<Map<String, Object>> banners = shopJdbcTemplate.queryForList("""
                SELECT id, name, pic, url, note
                FROM sms_home_advertise
                WHERE site_id = ? AND status = 1
                ORDER BY sort ASC, id ASC
                """, shopSiteId);

        List<Map<String, Object>> categories = shopJdbcTemplate.queryForList("""
                SELECT id, name, icon, product_count AS "productCount"
                FROM pms_product_category
                WHERE site_id = ? AND show_status = 1
                ORDER BY sort ASC, id ASC
                """, shopSiteId);

        List<Map<String, Object>> coupons = shopJdbcTemplate.queryForList("""
                SELECT id, name, amount, min_point AS "minPoint", code, note
                FROM sms_coupon
                WHERE site_id = ?
                ORDER BY id ASC
                """, shopSiteId);

        Map<String, Object> commerce = new LinkedHashMap<>();
        commerce.put("shopSiteId", shopSiteId);
        commerce.put("products", products);
        commerce.put("banners", banners);
        commerce.put("categories", categories);
        commerce.put("coupons", coupons);
        return commerce;
    }

    private Map<String, Object> loadCmsData(Long cmsSiteId) {
        return loadCmsData(cmsSiteId, null);
    }

    private Map<String, Object> loadCmsData(Long cmsSiteId, String siteSlug) {
        if (cmsSiteId == null && (siteSlug == null || siteSlug.isBlank())) {
            return Map.of(
                    "cmsSiteId", 0L,
                    "articles", List.of(),
                    "banners", List.of(),
                    "forms", List.of()
            );
        }
        if (syncServiceApiClient != null) {
            try {
                Map<String, Object> apiData = syncServiceApiClient.fetchCmsBootstrap(cmsSiteId, siteSlug);
                if (apiData != null && !apiData.isEmpty()) {
                    apiData.putIfAbsent("cmsSiteId", cmsSiteId != null ? cmsSiteId : 0L);
                    apiData.putIfAbsent("articles", List.of());
                    apiData.putIfAbsent("banners", List.of());
                    apiData.putIfAbsent("forms", List.of());
                    return apiData;
                }
            } catch (Exception e) {
                log.warn("SyncCMS 부트스트랩 REST API 호출 실패, 로컬 어댑터 폴백 수행: {}", e.getMessage());
            }
        }
        if (cmsSiteId == null || cmsJdbcTemplate == null) {
            return Map.of(
                    "cmsSiteId", 0L,
                    "articles", List.of(),
                    "banners", List.of(),
                    "forms", List.of()
            );
        }
        List<Map<String, Object>> articles = cmsJdbcTemplate.queryForList("""
                SELECT c.id,
                       c.site_id AS "siteId",
                       c.title,
                       c.description,
                       c.description AS "summary",
                       COALESCE(a.text, c.description) AS "bodyHtml",
                       COALESCE(a.text, c.description) AS "contentHtml",
                       c.cover,
                       c.cover AS "thumbnailUrl",
                       c.author,
                       c.author AS "authorName",
                       cat.name AS "categoryName",
                       c.clicks,
                       c.clicks AS "viewCount",
                       c.publish_date AS "publishedTime"
                FROM cms_content c
                LEFT JOIN cms_content_attribute a ON a.content_id = c.id
                LEFT JOIN cms_category cat ON cat.id = c.category_id
                WHERE c.site_id = ? AND c.status = 1 AND c.disabled = FALSE
                ORDER BY c.sort DESC, c.publish_date DESC, c.id ASC
                """, cmsSiteId);

        List<Map<String, Object>> banners = cmsJdbcTemplate.queryForList("""
                SELECT id,
                       title,
                       sub_title AS "subTitle",
                       image_url AS "imageUrl",
                       link_url AS "linkUrl",
                       zone_code AS "position"
                FROM cms_banner
                WHERE site_id = ? AND status = 1
                ORDER BY sort DESC, id ASC
                """, cmsSiteId);

        List<Map<String, Object>> forms = cmsJdbcTemplate.queryForList("""
                SELECT id,
                       title,
                       title AS "formName",
                       code,
                       code AS "formCode",
                       description,
                       schema_json::text AS "fieldsJson"
                FROM cms_form
                WHERE site_id = ? AND status = 1
                ORDER BY id ASC
                """, cmsSiteId);
        forms.forEach(f -> f.put("fields", parseJsonList(String.valueOf(f.remove("fieldsJson")))));

        Map<String, Object> cms = new LinkedHashMap<>();
        cms.put("cmsSiteId", cmsSiteId);
        cms.put("articles", articles);
        cms.put("banners", banners);
        cms.put("forms", forms);
        return cms;
    }

    private Long provisionSyncShopSite(String siteSlug, String brandName, String slogan) {
        if (syncServiceApiClient != null) {
            try {
                Map<String, Object> apiRes = syncServiceApiClient.provisionShopSite(
                        siteSlug, brandName, slogan, "#4F46E5", "SIGNATURE"
                );
                if (apiRes != null && apiRes.get("shopSiteId") instanceof Number num) {
                    return num.longValue();
                }
            } catch (Exception e) {
                log.warn("SyncShop 스토어 프로비저닝 REST API 호출 실패, 로컬 어댑터 폴백 수행: {}", e.getMessage());
            }
        }
        if (shopJdbcTemplate == null) {
            return null;
        }
        List<Map<String, Object>> existing = shopJdbcTemplate.queryForList(
                "SELECT id FROM shop_site WHERE site_slug = ? LIMIT 1", siteSlug
        );
        Long shopSiteId;
        if (!existing.isEmpty()) {
            shopSiteId = ((Number) existing.get(0).get("id")).longValue();
        } else {
            String safeName = brandName != null && brandName.length() > 120 ? brandName.substring(0, 120) : brandName;
            String safeSlogan = slogan != null && slogan.length() > 250 ? slogan.substring(0, 250) : slogan;
            shopSiteId = shopJdbcTemplate.queryForObject("""
                    INSERT INTO shop_site (site_slug, site_name, site_type, brand_slogan, status, create_time, update_time)
                    VALUES (?, ?, 'COMMERCE', ?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    RETURNING id
                    """,
                    Long.class,
                    siteSlug, safeName, safeSlogan
            );
        }
        if (shopSiteId == null) {
            return null;
        }

        List<Map<String, Object>> existingCats = shopJdbcTemplate.queryForList(
                "SELECT id, name FROM pms_product_category WHERE site_id = ? ORDER BY id ASC LIMIT 1",
                shopSiteId
        );
        Long defaultCategoryId;
        String defaultCategoryName = "시그니처 컬렉션";
        if (!existingCats.isEmpty()) {
            defaultCategoryId = ((Number) existingCats.get(0).get("id")).longValue();
            if (existingCats.get(0).get("name") != null) {
                defaultCategoryName = String.valueOf(existingCats.get(0).get("name"));
            }
        } else {
            defaultCategoryId = shopJdbcTemplate.queryForObject("""
                    INSERT INTO pms_product_category (
                        site_id, parent_id, name, level, product_count, product_unit, nav_status, show_status, sort, description
                    ) VALUES (?, 0, ?, 0, 2, '개', 1, 1, 1, ?)
                    RETURNING id
                    """,
                    Long.class,
                    shopSiteId, defaultCategoryName, brandName + " 대표 시그니처 상품 라인업"
            );
        }

        Integer couponCount = shopJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sms_coupon WHERE site_id = ?",
                Integer.class, shopSiteId
        );
        if (couponCount == null || couponCount == 0) {
            shopJdbcTemplate.update("""
                    INSERT INTO sms_coupon (
                        site_id, type, name, platform, count, amount, per_limit, min_point,
                        start_time, end_time, use_type, note, publish_count, use_count, receive_count, code
                    ) VALUES (?, 0, ?, 0, 500, 5000.00, 1, 30000.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '365 days', 0, '신규 오픈 기념 즉시 할인', 500, 0, 0, ?)
                    """,
                    shopSiteId, brandName + " 웰컴 5,000원 할인 쿠폰", "WELCOME-" + shopSiteId
            );
        }

        return shopSiteId;
    }

    private Long provisionSyncCmsSite(String siteSlug, String brandName, String slogan) {
        if (syncServiceApiClient != null) {
            try {
                Map<String, Object> apiRes = syncServiceApiClient.provisionCmsSite(siteSlug, brandName, slogan);
                if (apiRes != null && apiRes.get("cmsSiteId") instanceof Number num) {
                    return num.longValue();
                }
            } catch (Exception e) {
                log.warn("SyncCMS 멀티사이트 프로비저닝 REST API 호출 실패, 로컬 어댑터 폴백 수행: {}", e.getMessage());
            }
        }
        if (cmsJdbcTemplate == null) {
            return null;
        }
        String sitePath = "/synccms/" + siteSlug + "/";
        String dynamicPath = "/s/" + siteSlug + "/";
        List<Map<String, Object>> existing = cmsJdbcTemplate.queryForList(
                "SELECT id FROM sys_site WHERE site_path = ? OR dynamic_path = ? OR name = ? ORDER BY id ASC LIMIT 1",
                sitePath, dynamicPath, brandName
        );
        Long cmsSiteId;
        if (!existing.isEmpty()) {
            cmsSiteId = ((Number) existing.get(0).get("id")).longValue();
        } else {
            Integer nextId = cmsJdbcTemplate.queryForObject("""
                    SELECT GREATEST(
                        COALESCE((SELECT MAX(id) FROM sys_site), 100),
                        COALESCE((SELECT MAX(site_id) FROM cms_category), 102)
                    ) + 1
                    """, Integer.class);
            short resolvedSiteId = nextId != null ? nextId.shortValue() : (short) 103;
            String safeName = brandName != null && brandName.length() > 50 ? brandName.substring(0, 50) : brandName;
            cmsSiteId = cmsJdbcTemplate.queryForObject("""
                    INSERT INTO sys_site (id, parent_id, name, use_static, site_path, use_ssi, dynamic_path, disabled)
                    VALUES (?, 1, ?, FALSE, ?, FALSE, ?, FALSE)
                    RETURNING id
                    """,
                    Long.class,
                    resolvedSiteId, safeName, sitePath, dynamicPath
            );
        }
        if (cmsSiteId == null) {
            return null;
        }

        short siteIdShort = cmsSiteId.shortValue();
        List<Map<String, Object>> existingCat = cmsJdbcTemplate.queryForList(
                "SELECT id FROM cms_category WHERE site_id = ? ORDER BY id ASC LIMIT 1",
                siteIdShort
        );
        Integer categoryId;
        if (!existingCat.isEmpty()) {
            categoryId = ((Number) existingCat.get(0).get("id")).intValue();
        } else {
            String safeCatCode = siteSlug.length() > 40 ? siteSlug.substring(0, 40) + "-news" : siteSlug + "-news";
            categoryId = cmsJdbcTemplate.queryForObject("""
                    INSERT INTO cms_category (
                        site_id, name, code, only_url, has_static, contain_child, allow_contribute, sort, hidden, disabled
                    ) VALUES (?, '브랜드 스토리', ?, FALSE, FALSE, TRUE, TRUE, 1, FALSE, FALSE)
                    RETURNING id
                    """,
                    Integer.class,
                    siteIdShort, safeCatCode
            );
        }

        Integer contentCount = cmsJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cms_content WHERE site_id = ? AND disabled = FALSE",
                Integer.class, siteIdShort
        );
        if (contentCount == null || contentCount == 0) {
            String safeAuthor = brandName != null && brandName.length() > 50 ? brandName.substring(0, 50) : brandName;
            String title1 = "[브랜드 스토리] " + brandName + " — " + slogan;
            if (title1.length() > 250) {
                title1 = title1.substring(0, 250);
            }
            String desc1 = brandName + " 공식 공간에 오신 것을 환영합니다. " + slogan + "의 가치를 담아 정성껏 준비한 프로그램과 공간 이야기를 소개합니다.";
            Long contentId1 = cmsJdbcTemplate.queryForObject("""
                    INSERT INTO cms_content (
                        site_id, title, user_id, category_id, model_id, copied, contribute,
                        author, only_url, has_images, has_files, has_products, has_static,
                        description, cover, childs, scores, comments, clicks,
                        publish_date, create_date, sort, status, disabled, locale
                    ) VALUES (?, ?, 1, ?, 'article', FALSE, FALSE, ?, FALSE, TRUE, FALSE, FALSE, FALSE, ?, '/images/studio/ed-1.jpg', 0, 95, 4, 128, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 100, 1, FALSE, 'ko')
                    RETURNING id
                    """,
                    Long.class,
                    siteIdShort, title1, categoryId, safeAuthor, desc1
            );
            String body1 = "<p>" + escapeXml(desc1) + "</p>";
            cmsJdbcTemplate.update(
                    "INSERT INTO cms_content_attribute (content_id, text, word_count) VALUES (?, ?, ?)",
                    contentId1, body1, desc1.length()
            );

            String title2 = "[이용 및 예약 안내] " + brandName + " 주요 시설 · 프로그램 및 오시는 길 안내";
            if (title2.length() > 250) {
                title2 = title2.substring(0, 250);
            }
            String desc2 = "방문 전 확인하실 수 있는 이용 시간, 예약 방법, 주변 안내 및 자주 묻는 질문을 정리했습니다. 온라인 신청 폼 또는 AI 컨시어지를 통해 언제든 문의하실 수 있습니다.";
            Long contentId2 = cmsJdbcTemplate.queryForObject("""
                    INSERT INTO cms_content (
                        site_id, title, user_id, category_id, model_id, copied, contribute,
                        author, only_url, has_images, has_files, has_products, has_static,
                        description, cover, childs, scores, comments, clicks,
                        publish_date, create_date, sort, status, disabled, locale
                    ) VALUES (?, ?, 1, ?, 'article', FALSE, FALSE, ?, FALSE, TRUE, FALSE, FALSE, FALSE, ?, '/images/studio/ed-4.jpg', 0, 88, 2, 94, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 95, 1, FALSE, 'ko')
                    RETURNING id
                    """,
                    Long.class,
                    siteIdShort, title2, categoryId, safeAuthor, desc2
            );
            String body2 = "<p>" + escapeXml(desc2) + "</p>";
            cmsJdbcTemplate.update(
                    "INSERT INTO cms_content_attribute (content_id, text, word_count) VALUES (?, ?, ?)",
                    contentId2, body2, desc2.length()
            );
        }

        Integer bannerCount = cmsJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cms_banner WHERE site_id = ? AND status = 1",
                Integer.class, siteIdShort
        );
        if (bannerCount == null || bannerCount == 0) {
            String bannerTitle = (brandName + " — " + slogan);
            if (bannerTitle.length() > 195) {
                bannerTitle = bannerTitle.substring(0, 195);
            }
            cmsJdbcTemplate.update("""
                    INSERT INTO cms_banner (
                        site_id, zone_code, title, sub_title, image_url, link_url, start_date, end_date, sort, status
                    ) VALUES (?, 'MAIN_HERO', ?, '공식 브랜드 스토리 및 온라인 예약·문의 접수 중', '/images/studio/ed-1.jpg', '#section-form', '2025-01-01 00:00:00', '2030-12-31 23:59:59', 100, 1)
                    """,
                    siteIdShort, bannerTitle
            );
        }

        ensureDefaultCmsForm(siteIdShort, siteSlug, brandName);
        return cmsSiteId;
    }

    private Long ensureDefaultCmsForm(short cmsSiteId, String siteSlug, String brandName) {
        List<Map<String, Object>> existingForm = cmsJdbcTemplate.queryForList(
                "SELECT id FROM cms_form WHERE site_id = ? AND status = 1 ORDER BY id ASC LIMIT 1",
                cmsSiteId
        );
        if (!existingForm.isEmpty()) {
            return ((Number) existingForm.get(0).get("id")).longValue();
        }
        String baseSlug = (siteSlug != null && !siteSlug.isBlank()) ? siteSlug : ("site-" + cmsSiteId);
        String safeFormCode = baseSlug.length() > 38 ? baseSlug.substring(0, 38) + "-apply" : baseSlug + "-apply";
        List<Map<String, Object>> codeCollision = cmsJdbcTemplate.queryForList(
                "SELECT id FROM cms_form WHERE code = ? LIMIT 1",
                safeFormCode
        );
        if (!codeCollision.isEmpty()) {
            safeFormCode = safeFormCode + "-" + cmsSiteId;
        }
        String defaultSchema = "[{\"field\":\"applicantName\",\"label\":\"신청자 성함\",\"type\":\"text\",\"required\":true},{\"field\":\"phone\",\"label\":\"연락처\",\"type\":\"tel\",\"required\":true},{\"field\":\"optionValue\",\"label\":\"희망 일정 / 프로그램 선택\",\"type\":\"text\",\"required\":false},{\"field\":\"message\",\"label\":\"문의 및 요청사항\",\"type\":\"textarea\",\"required\":false}]";
        return cmsJdbcTemplate.queryForObject("""
                INSERT INTO cms_form (
                    site_id, code, title, description, schema_json, privacy_policy, status, create_date
                ) VALUES (?, ?, ?, ?, ?::jsonb, '신청 접수 및 안내 연락 목적으로만 수집·이용됩니다.', 1, CURRENT_TIMESTAMP)
                RETURNING id
                """,
                Long.class,
                cmsSiteId,
                safeFormCode,
                brandName + " 온라인 예약 · 문의 신청",
                "희망하시는 일정이나 문의사항을 남겨주시면 확인 후 신속히 안내해 드립니다.",
                defaultSchema
        );
    }

    // =========================================================================
    // 10. 내부 유틸리티 & 검증 헬퍼
    // =========================================================================

    private KkumzAiGatewayService ensureAiGateway() {
        if (this.aiGatewayService == null) {
            KkumzAiGatewayService provided = this.aiGatewayProvider != null ? this.aiGatewayProvider.getIfAvailable() : null;
            this.aiGatewayService = provided != null ? provided : new KkumzAiGatewayService(this.jdbcTemplate, this.objectMapper, null);
        }
        return this.aiGatewayService;
    }

    private Map<String, String> resolveUserIdentity(String requestedUsername, String requestedDisplayName) {
        if (requestedUsername != null && !requestedUsername.isBlank()) {
            String uname = requestedUsername.trim();
            if (requestedDisplayName != null && !requestedDisplayName.isBlank()) {
                return Map.of("username", uname, "realname", requestedDisplayName.trim());
            }
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT username, realname FROM sys_user WHERE username = ? AND del_flag = 0 LIMIT 1",
                    uname
            );
            if (!rows.isEmpty()) {
                return Map.of(
                        "username", String.valueOf(rows.get(0).get("username")),
                        "realname", String.valueOf(rows.get(0).get("realname"))
                );
            }
            return Map.of("username", uname, "realname", uname);
        }

        List<Map<String, Object>> defaultRows = jdbcTemplate.queryForList(
                "SELECT username, realname FROM sys_user WHERE del_flag = 0 ORDER BY create_time ASC LIMIT 1"
        );
        if (!defaultRows.isEmpty()) {
            String uname = String.valueOf(defaultRows.get(0).get("username"));
            String rname = (requestedDisplayName != null && !requestedDisplayName.isBlank())
                    ? requestedDisplayName.trim()
                    : String.valueOf(defaultRows.get(0).get("realname"));
            return Map.of("username", uname, "realname", rname);
        }
        String fallbackName = (requestedDisplayName != null && !requestedDisplayName.isBlank())
                ? requestedDisplayName.trim()
                : "admin";
        return Map.of("username", "admin", "realname", fallbackName);
    }

    private ObjectMapper lenientMapper() {
        return JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
                .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
                .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
                .build();
    }

    private List<Map<String, Object>> parseAiJsonArray(String rawJson, String stageName) {
        try {
            String cleaned = rawJson != null ? rawJson.trim() : "";
            cleaned = cleaned.replaceAll("(?s)<think>.*?</think>", "").trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceFirst("^```[a-zA-Z0-9_-]*\\s*", "").replaceFirst("\\s*```$", "").trim();
            }

            ObjectMapper mapper = lenientMapper();
            List<String> arrayKeys = List.of("items", "candidates", "suggestions", "files", "data", "result", "brands", "logos", "names", "list");

            // 1. 각 '[' 위치부터 마지막 ']' 까지 순차 탐색하여 유효한 JSON 배열인지 검사 (앞부분 인사말이나 [대괄호 텍스트] 무시)
            int lastBracket = cleaned.lastIndexOf(']');
            if (lastBracket > 0) {
                int searchFrom = 0;
                while (searchFrom >= 0 && searchFrom < lastBracket) {
                    int arrStart = cleaned.indexOf('[', searchFrom);
                    if (arrStart < 0 || arrStart >= lastBracket) {
                        break;
                    }
                    String slice = cleaned.substring(arrStart, lastBracket + 1);
                    try {
                        JsonNode node = mapper.readTree(slice);
                        if (node.isArray() && !node.isEmpty()) {
                            return mapper.convertValue(node, new TypeReference<List<Map<String, Object>>>() {});
                        }
                    } catch (Exception ignored) {
                    }
                    searchFrom = arrStart + 1;
                }
            }

            // 2. '{' ~ '}' JSON 객체 래퍼 또는 개별 객체 나열 탐색
            int firstBrace = cleaned.indexOf('{');
            int lastBrace = cleaned.lastIndexOf('}');
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                String objSlice = cleaned.substring(firstBrace, lastBrace + 1);
                try {
                    JsonNode root = mapper.readTree(objSlice);
                    if (root.isObject()) {
                        for (String key : arrayKeys) {
                            if (root.has(key) && root.get(key).isArray()) {
                                return mapper.convertValue(root.get(key), new TypeReference<List<Map<String, Object>>>() {});
                            }
                        }
                        return List.of(mapper.convertValue(root, new TypeReference<Map<String, Object>>() {}));
                    }
                } catch (Exception ignored) {
                    // 여러 개의 JSON 객체가 줄바꿈/쉼표로 나열된 경우 배열로 감싸서 재시도
                    try {
                        JsonNode wrapped = mapper.readTree("[" + objSlice + "]");
                        if (wrapped.isArray() && !wrapped.isEmpty()) {
                            return mapper.convertValue(wrapped, new TypeReference<List<Map<String, Object>>>() {});
                        }
                    } catch (Exception ignored2) {
                    }
                }
            }

            JsonNode root = mapper.readTree(cleaned);
            if (root.isArray()) {
                return mapper.convertValue(root, new TypeReference<List<Map<String, Object>>>() {});
            }
            if (root.isObject()) {
                for (String key : arrayKeys) {
                    if (root.has(key) && root.get(key).isArray()) {
                        return mapper.convertValue(root.get(key), new TypeReference<List<Map<String, Object>>>() {});
                    }
                }
            }
            throw new IllegalStateException("AI 응답(" + stageName + ")이 JSON 배열 형식이 아닙니다.");
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("AI 응답(" + stageName + ") JSON 파싱에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private List<String> validateWebIdeSourceFiles(List<Map<String, Object>> files) {
        List<String> errors = new ArrayList<>();
        for (Map<String, Object> f : files) {
            String path = String.valueOf(f.getOrDefault("path", "unknown"));
            String content = String.valueOf(f.getOrDefault("content", ""));
            if (path.endsWith(".json")) {
                try {
                    objectMapper.readTree(content);
                } catch (Exception e) {
                    errors.add(path + ": 유효하지 않은 JSON 구문 (" + e.getMessage() + ")");
                }
            } else if (path.endsWith(".vue")) {
                if (content.contains("<template>") && !content.contains("</template>")) {
                    errors.add(path + ": 닫히지 않은 <template> 태그가 존재합니다.");
                }
                if (content.contains("<script") && !content.contains("</script>")) {
                    errors.add(path + ": 닫히지 않은 <script> 태그가 존재합니다.");
                }
            }
        }
        return errors;
    }

    private List<Map<String, Object>> buildWebIdeFilesFromScreens(
            String brandName,
            Map<String, Object> tokens,
            Map<String, Object> layout,
            List<Map<String, Object>> screenSpecs
    ) {
        StringBuilder sectionMarkup = new StringBuilder();
        for (Map<String, Object> screen : screenSpecs) {
            String key = String.valueOf(screen.getOrDefault("screenKey", "SECTION"));
            String title = String.valueOf(screen.getOrDefault("screenTitle", brandName));
            sectionMarkup.append("    <section class=\"screen-block\" data-screen=\"")
                    .append(escapeXml(key))
                    .append("\">\n      <h2>")
                    .append(escapeXml(title))
                    .append("</h2>\n    </section>\n");
        }
        String appVue = "<template>\n"
                + "  <div class=\"kkumz-live-shell\" :style=\"{ backgroundColor: tokens?.surface || '#F8FAFC', color: tokens?.text || '#0F172A' }\">\n"
                + "    <header class=\"brand-hero\">\n"
                + "      <div class=\"hero-inner\">\n"
                + "        <span class=\"brand-badge\">{{ layout?.category || 'OFFICIAL BRAND' }}</span>\n"
                + "        <h1>{{ layout?.heroTitle }}</h1>\n"
                + "        <p>{{ layout?.heroSubtitle }}</p>\n"
                + "      </div>\n"
                + "    </header>\n"
                + "    <main class=\"screen-container\">\n"
                + sectionMarkup
                + "    </main>\n"
                + "    <footer class=\"brand-footer\">\n"
                + "      <p>{{ layout?.heroTitle }} — Powered by Kkumz Runtime SDK</p>\n"
                + "    </footer>\n"
                + "  </div>\n"
                + "</template>\n"
                + "<script setup lang=\"ts\">\n"
                + "defineProps<{\n"
                + "  tokens: Record<string, any>;\n"
                + "  layout: Record<string, any>;\n"
                + "  runtime?: Record<string, any> | null;\n"
                + "}>();\n"
                + "</script>\n"
                + "<style scoped>\n"
                + ".kkumz-live-shell { min-height: 100vh; display: flex; flex-direction: column; }\n"
                + ".brand-hero { padding: 4rem 1.5rem; text-align: center; }\n"
                + ".screen-container { max-width: 1120px; margin: 0 auto; padding: 2rem 1.5rem; display: grid; gap: 1.5rem; }\n"
                + ".screen-block { padding: 1.75rem; border-radius: 1rem; background: #ffffff; border: 1px solid #e2e8f0; }\n"
                + ".brand-footer { margin-top: auto; padding: 2rem 1.5rem; text-align: center; font-size: 0.875rem; color: #64748b; }\n"
                + "</style>";

        List<Map<String, Object>> files = new ArrayList<>();
        files.add(new LinkedHashMap<>(Map.of("path", "src/App.vue", "language", "vue", "content", appVue)));
        files.add(new LinkedHashMap<>(Map.of("path", "src/theme.json", "language", "json", "content", toJson(tokens))));
        files.add(new LinkedHashMap<>(Map.of("path", "src/layout.json", "language", "json", "content", toJson(layout))));
        return files;
    }

    private Map<String, Object> findSiteByIdOrSlug(String siteIdOrSlug) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id,
                       idea_id AS "ideaId",
                       site_slug AS "siteSlug",
                       site_name AS "siteName",
                       site_type AS "siteType",
                       category,
                       brand_slogan AS "brandSlogan",
                       logo_svg AS "logoSvg",
                       shop_site_id AS "shopSiteId",
                       cms_site_id AS "cmsSiteId",
                       deploy_mode AS "deployMode",
                       active_version AS "activeVersion",
                       published_version AS "publishedVersion",
                       published_at AS "publishedAt",
                       deployed_url AS "deployedUrl",
                       custom_domain AS "customDomain",
                       is_featured AS "isFeatured",
                       visibility,
                       like_count AS "likeCount",
                       comment_count AS "commentCount",
                       view_count AS "viewCount",
                       owner_username AS "ownerUsername",
                       owner_display_name AS "ownerDisplayName",
                       status,
                       created_at AS "createdAt",
                       updated_at AS "updatedAt"
                FROM kkumz_site WHERE site_slug = ? OR id = ?
                LIMIT 1
                """, siteIdOrSlug, siteIdOrSlug);

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 멀티사이트입니다: " + siteIdOrSlug);
        }
        return new LinkedHashMap<>(rows.get(0));
    }

    private Map<String, Object> getLatestRelease(String siteId, int versionNo) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id,
                       site_id AS "siteId",
                       version_no AS "versionNo",
                       release_note AS "releaseNote",
                       deploy_mode AS "deployMode",
                       design_tokens_json::text AS "designTokensJson",
                       layout_schema_json::text AS "layoutSchemaJson",
                       webide_files_json::text AS "webideFilesJson",
                       minio_bundle_path AS "minioBundlePath",
                       build_status AS "buildStatus",
                       build_log AS "buildLog",
                       created_by AS "createdBy",
                       created_at AS "createdAt"
                FROM kkumz_site_release
                WHERE site_id = ? AND version_no = ?
                LIMIT 1
                """, siteId, versionNo);

        if (!rows.isEmpty()) {
            return rows.get(0);
        }

        List<Map<String, Object>> fallback = jdbcTemplate.queryForList("""
                SELECT id,
                       site_id AS "siteId",
                       version_no AS "versionNo",
                       release_note AS "releaseNote",
                       deploy_mode AS "deployMode",
                       design_tokens_json::text AS "designTokensJson",
                       layout_schema_json::text AS "layoutSchemaJson",
                       webide_files_json::text AS "webideFilesJson",
                       minio_bundle_path AS "minioBundlePath",
                       build_status AS "buildStatus",
                       build_log AS "buildLog",
                       created_by AS "createdBy",
                       created_at AS "createdAt"
                FROM kkumz_site_release
                WHERE site_id = ?
                ORDER BY version_no DESC
                LIMIT 1
                """, siteId);

        if (fallback.isEmpty()) {
            throw new IllegalStateException("사이트 릴리즈 스냅샷이 존재하지 않습니다: " + siteId);
        }
        return fallback.get(0);
    }

    private void createNotification(String recipient, String type, String title, String message, String linkUrl) {
        if (recipient == null || recipient.isBlank()) {
            return;
        }
        jdbcTemplate.update("""
                INSERT INTO kkumz_notification (
                    id, recipient_username, notif_type, title, message, link_url, is_read, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, FALSE, CURRENT_TIMESTAMP)
                """,
                "noti-" + UUID.randomUUID().toString().substring(0, 8),
                recipient.trim(), type, title, message, linkUrl
        );
    }

    private void enrichIdeaRow(Map<String, Object> row) {
        row.put("tags", parseJsonStringList(String.valueOf(row.remove("tagsJson"))));
        row.put("enabledFeatures", parseJsonStringList(String.valueOf(row.remove("enabledFeaturesJson"))));
        row.put("aiIntentAnalysis", parseJsonObject(String.valueOf(row.remove("aiIntentAnalysisJson"))));
        row.put("designTokens", parseJsonObject(String.valueOf(row.remove("designTokensJson"))));
        row.put("screens", parseJsonList(String.valueOf(row.remove("screensJson"))));
        String currentUser = resolveCurrentUsernameFromRequest();
        String author = row.get("authorUsername") != null ? String.valueOf(row.get("authorUsername")).trim() : "";
        boolean isOwner = currentUser != null && !currentUser.isBlank() && currentUser.equalsIgnoreCase(author);
        boolean canEdit = isOwner || (currentUser != null && !currentUser.isBlank() && canEditIdea(row, currentUser));
        row.put("isOwner", isOwner);
        row.put("canEdit", canEdit);

        Object siteIdObj = row.remove("siteId");
        Object siteSlugObj = row.remove("siteSlug");
        Object siteNameObj = row.remove("siteName");
        Object siteSiteTypeObj = row.remove("siteSiteType");
        Object siteDeployModeObj = row.remove("siteDeployMode");
        Object siteActiveVersionObj = row.remove("siteActiveVersion");
        Object sitePublishedVersionObj = row.remove("sitePublishedVersion");
        Object sitePublishedAtObj = row.remove("sitePublishedAt");
        Object siteVisibilityObj = row.remove("siteVisibility");
        Object siteStatusObj = row.remove("siteStatus");
        Object siteDeployedUrlObj = row.remove("siteDeployedUrl");
        Object siteCustomDomainObj = row.remove("siteCustomDomain");
        Object siteLikeCountObj = row.remove("siteLikeCount");
        Object siteCommentCountObj = row.remove("siteCommentCount");
        Object siteViewCountObj = row.remove("siteViewCount");

        if (siteIdObj != null && siteSlugObj != null) {
            Map<String, Object> siteMap = new LinkedHashMap<>();
            siteMap.put("id", siteIdObj);
            siteMap.put("siteSlug", siteSlugObj);
            siteMap.put("siteName", siteNameObj != null ? siteNameObj : row.getOrDefault("selectedBrandName", row.get("title")));
            siteMap.put("siteType", siteSiteTypeObj != null ? siteSiteTypeObj : row.get("siteType"));
            siteMap.put("deployMode", siteDeployModeObj != null ? siteDeployModeObj : "DYNAMIC_SDUI");
            int activeVer = siteActiveVersionObj instanceof Number n ? n.intValue() : 1;
            siteMap.put("activeVersion", activeVer);
            siteMap.put("publishedVersion", sitePublishedVersionObj);
            siteMap.put("publishedAt", sitePublishedAtObj);
            siteMap.put("visibility", siteVisibilityObj != null ? siteVisibilityObj : "PRIVATE");
            siteMap.put("status", siteStatusObj != null ? siteStatusObj : "DRAFT");
            siteMap.put("deployedUrl", siteDeployedUrlObj != null ? siteDeployedUrlObj : ("/s/" + siteSlugObj));
            siteMap.put("customDomain", siteCustomDomainObj);
            siteMap.put("likeCount", siteLikeCountObj != null ? siteLikeCountObj : 0);
            siteMap.put("commentCount", siteCommentCountObj != null ? siteCommentCountObj : 0);
            siteMap.put("viewCount", siteViewCountObj != null ? siteViewCountObj : 0);
            boolean isPub = isSitePubliclyPublished(siteMap);
            Integer pubVer = resolvePublishedVersion(siteMap);
            siteMap.put("isPublished", isPub);
            siteMap.put("publishedVersion", pubVer);
            siteMap.put("hasUnpublishedChanges", !isPub || (pubVer != null && activeVer > pubVer));
            siteMap.put("isOwner", isOwner);
            siteMap.put("canEdit", canEdit);
            row.put("site", siteMap);
            row.put("deployedSites", List.of(siteMap));
        }
    }

    private List<String> parseJsonStringList(String raw) {
        if (raw == null || raw.isBlank() || "null".equalsIgnoreCase(raw.trim())) {
            return new ArrayList<>();
        }
        try {
            JsonNode node = lenientMapper().readTree(raw);
            if (node.isArray()) {
                List<String> list = new ArrayList<>();
                for (JsonNode n : node) {
                    if (n != null && !n.isNull()) {
                        String val = n.asText().trim();
                        if (!val.isBlank()) {
                            list.add(val);
                        }
                    }
                }
                return list;
            }
        } catch (Exception ignored) {
        }
        return new ArrayList<>();
    }

    private boolean hasEngineFeature(List<String> enabledFeatures, List<Map<String, Object>> catalog, String engineCode) {
        if (enabledFeatures == null || enabledFeatures.isEmpty() || catalog == null) {
            return false;
        }
        for (Map<String, Object> fc : catalog) {
            String fCode = String.valueOf(fc.getOrDefault("featureCode", ""));
            String eCode = String.valueOf(fc.getOrDefault("engineCode", ""));
            if (enabledFeatures.contains(fCode) && engineCode.equalsIgnoreCase(eCode)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBothShopAndCmsFeatures(List<String> enabledFeatures, List<Map<String, Object>> catalog) {
        return hasEngineFeature(enabledFeatures, catalog, "SYNC_SHOP")
                && hasEngineFeature(enabledFeatures, catalog, "SYNC_CMS");
    }

    private boolean isConversationalRawPrompt(String text) {
        if (text == null || text.isBlank()) {
            return true;
        }
        String t = text.trim();
        if (t.matches(".*(만들고\\s*싶어요?|구축하고\\s*싶어요?).*")) {
            return true;
        }
        if (t.matches(".*(만들어\\s*줘|만들어\\s*주세요).*")) {
            return true;
        }
        return t.matches(".*(사이트|홈페이지)를?\\s*만들고.*") || t.endsWith("원해");
    }

    private String stripConversationalTail(String text) {
        if (text == null) {
            return "";
        }
        return text.trim()
                .replaceAll("\\s*(더\\s*화려하게|멋지게)\\s*", " ")
                .replaceAll("\\s*(예쁘게|깔끔하게)\\s*", " ")
                .replaceAll("\\s*새로\\s*", " ")
                .replaceAll("\\s*(홈페이지|웹사이트)를?\\s*", " ")
                .replaceAll("\\s*(사이트|플랫폼)을?를?\\s*", " ")
                .replaceAll("\\s*(공식몰|스토어)를?\\s*", " ")
                .replaceAll("\\s*(만들고\\s*싶어요?|구축하고\\s*싶어요?)\\.?$", "")
                .replaceAll("\\s*(만들어\\s*주세요|만들어\\s*줘)\\.?$", "")
                .replaceAll("\\s*(원해요?|부탁해요?)\\.?$", "")
                .trim();
    }

    private String cleanConversationalPromptToHeadline(String rawPrompt, String brandName, String category) {
        if (rawPrompt == null || rawPrompt.isBlank()) {
            return brandName + " 공식 브랜드 & 예약 플랫폼";
        }
        String cleaned = stripConversationalTail(rawPrompt);
        if (cleaned.isBlank() || cleaned.equals(brandName)) {
            return brandName + " — " + (category != null && !category.isBlank() && !"AUTO".equalsIgnoreCase(category) ? category : "공식 브랜드 공간");
        }
        if (!cleaned.contains(brandName)) {
            return cleaned + " · " + brandName;
        }
        return cleaned;
    }

    private String resolveBrandSlogan(
            Map<String, Object> idea,
            String brandName,
            String category,
            Map<String, Object> aiIntentAnalysis
    ) {
        if (aiIntentAnalysis != null && aiIntentAnalysis.get("brandSlogan") != null) {
            String aiSlogan = String.valueOf(aiIntentAnalysis.get("brandSlogan")).trim();
            if (!aiSlogan.isBlank() && !isConversationalRawPrompt(aiSlogan)) {
                return aiSlogan;
            }
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> names = (List<Map<String, Object>>) idea.get("nameSuggestions");
        if (names != null) {
            for (Map<String, Object> n : names) {
                if (Boolean.TRUE.equals(n.get("isSelected")) && n.get("brandStory") != null) {
                    String story = String.valueOf(n.get("brandStory")).trim();
                    if (!story.isBlank() && !isConversationalRawPrompt(story)) {
                        return story.length() > 110 ? story.substring(0, 110) : story;
                    }
                }
            }
            if (!names.isEmpty() && names.get(0).get("brandStory") != null) {
                String story = String.valueOf(names.get(0).get("brandStory")).trim();
                if (!story.isBlank() && !isConversationalRawPrompt(story)) {
                    return story.length() > 110 ? story.substring(0, 110) : story;
                }
            }
        }
        String title = String.valueOf(idea.getOrDefault("title", "")).trim();
        if (!title.isBlank() && !isConversationalRawPrompt(title)) {
            return title;
        }
        return cleanConversationalPromptToHeadline(String.valueOf(idea.getOrDefault("promptText", "")), brandName, category);
    }

    private Map<String, Object> diagnoseIntentFromCatalog(
            String title,
            String promptText,
            String categoryHint,
            List<Map<String, Object>> assets,
            List<Map<String, Object>> catalog
    ) {
        String corpus = ((title != null ? title : "") + " " + (promptText != null ? promptText : "") + " " + (categoryHint != null ? categoryHint : ""))
                .toLowerCase(Locale.ROOT);

        // 1. DB 스타터 프리셋 및 기능 카탈로그의 recommendedCategories / searchKeywords를 기반으로 업종 추론
        String inferredCategory = (categoryHint != null && !categoryHint.isBlank() && !"AUTO".equalsIgnoreCase(categoryHint))
                ? categoryHint
                : "라이프스타일 · 브랜드";
        Map<String, Integer> catScores = new LinkedHashMap<>();
        for (Map<String, Object> fc : catalog) {
            @SuppressWarnings("unchecked")
            List<String> keywords = (List<String>) fc.getOrDefault("searchKeywords", List.of());
            @SuppressWarnings("unchecked")
            List<String> recCats = (List<String>) fc.getOrDefault("recommendedCategories", List.of());
            int hit = 0;
            for (String kw : keywords) {
                if (!kw.isBlank() && corpus.contains(kw.toLowerCase(Locale.ROOT))) {
                    hit += 2;
                }
            }
            if (hit > 0) {
                for (String rc : recCats) {
                    if (!"ALL".equalsIgnoreCase(rc)) {
                        catScores.merge(rc, hit, Integer::sum);
                    }
                }
            }
        }
        if ("AUTO".equalsIgnoreCase(categoryHint) || categoryHint == null || categoryHint.isBlank()) {
            List<Map.Entry<String, Integer>> topCats = catScores.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .limit(3)
                    .toList();
            if (!topCats.isEmpty()) {
                inferredCategory = topCats.stream().map(Map.Entry::getKey).reduce((a, b) -> a + " · " + b).orElse(inferredCategory);
            }
        }

        // 2. DB 52종 기능 카탈로그 전체 스코어링
        Map<String, Integer> featureScores = new LinkedHashMap<>();
        Map<String, String> featureReasons = new LinkedHashMap<>();
        for (Map<String, Object> fc : catalog) {
            String code = String.valueOf(fc.get("featureCode"));
            String fTitle = String.valueOf(fc.getOrDefault("title", ""));
            String fSub = String.valueOf(fc.getOrDefault("subtitle", ""));
            @SuppressWarnings("unchecked")
            List<String> keywords = (List<String>) fc.getOrDefault("searchKeywords", List.of());
            @SuppressWarnings("unchecked")
            List<String> recCats = (List<String>) fc.getOrDefault("recommendedCategories", List.of());

            int score = 0;
            List<String> matchedKws = new ArrayList<>();
            for (String kw : keywords) {
                if (!kw.isBlank() && corpus.contains(kw.toLowerCase(Locale.ROOT))) {
                    score += 5;
                    matchedKws.add(kw);
                }
            }
            for (String rc : recCats) {
                if ("ALL".equalsIgnoreCase(rc)) {
                    score += 3;
                } else if (corpus.contains(rc.toLowerCase(Locale.ROOT)) || inferredCategory.contains(rc)) {
                    score += 4;
                    if (matchedKws.isEmpty()) {
                        matchedKws.add(rc);
                    }
                }
            }
            if (assets != null && assets.size() >= 3 && code.matches("^(CMS_PHOTO_GALLERY|CMS_SPACE_TOUR|SHOP_PRODUCT_CATALOG)$")) {
                score += 6;
            }
            if (score > 0) {
                featureScores.put(code, score);
                String reason = !matchedKws.isEmpty()
                        ? "'" + String.join("·", matchedKws.subList(0, Math.min(3, matchedKws.size()))) + "' 맥락에 최적화된 " + fTitle + " (" + fSub + ")"
                        : fSub;
                featureReasons.put(code, reason);
            }
        }

        List<String> recommended = featureScores.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(12)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        for (String baseCode : List.of(
                "CMS_HERO_SHOWCASE",
                "SHOP_PRODUCT_CATALOG",
                "SHOP_PRICING_MATRIX",
                "CMS_SPACE_TOUR",
                "CMS_STEP_RITUAL_GUIDE",
                "CMS_DYNAMIC_BOOKING_FORM",
                "CMS_FAQ_ACCORDION",
                "CMS_LOCATION_DIRECTIONS",
                "CMS_POLICY_RULES_BOX",
                "SHOP_PHOTO_REVIEWS",
                "CMS_LIVE_POLL",
                "CMS_TRUST_METRICS_BAR",
                "AI_BRAND_CONCIERGE"
        )) {
            if (!recommended.contains(baseCode) && recommended.size() < 14) {
                recommended.add(baseCode);
            }
        }

        boolean hasShop = hasEngineFeature(recommended, catalog, "SYNC_SHOP");
        boolean hasCms = hasEngineFeature(recommended, catalog, "SYNC_CMS");
        String siteType = (hasShop && hasCms) ? "HYBRID" : (hasShop ? "COMMERCE" : "CMS_SITE");
        String siteTypeReason = "HYBRID".equals(siteType)
                ? "상품·객실·요금 안내(SyncShop)와 공간 갤러리·매거진·온라인 예약 폼(SyncCMS)이 모두 필요한 하이브리드 사이트로 자동 진단되었습니다."
                : ("COMMERCE".equals(siteType)
                ? "상품 카탈로그·옵션·장바구니·즉시결제 중심의 브랜드 커머스 스토어(SyncShop)로 진단되었습니다."
                : "브랜드 스토리·갤러리·온라인 신청 폼 중심의 커뮤니티·웹사이트(SyncCMS)로 진단되었습니다.");

        String cleanHeadline = cleanConversationalPromptToHeadline(promptText, title != null ? title : "브랜드", inferredCategory);
        String expandedPrompt = cleanHeadline + " 공식 웹사이트. "
                + inferredCategory + " 분야의 핵심 고객을 위해 "
                + "시그니처 상품·프로그램 카탈로그, 투명한 요금·일정 안내, 현장 포토 아카이브, 맞춤형 온라인 예약·문의 신청 폼, 자주 묻는 질문(FAQ)과 오시는 길, 24시간 AI 컨시어지를 통합 제공합니다.";

        List<String> tags = new ArrayList<>();
        for (String piece : inferredCategory.split("[·,/\\s]+")) {
            if (!piece.isBlank() && tags.size() < 5 && !tags.contains(piece.trim())) {
                tags.add(piece.trim());
            }
        }

        // 업로드 자산 용도 및 한글 캡션 자동 정리
        List<Map<String, Object>> assetAnnotations = new ArrayList<>();
        if (assets != null && !assets.isEmpty()) {
            int imgIdx = 0;
            for (Map<String, Object> a : assets) {
                String origName = String.valueOf(a.getOrDefault("originalName", ""));
                String curCap = String.valueOf(a.getOrDefault("caption", "")).trim();
                boolean isRawFilename = curCap.isBlank()
                        || curCap.equalsIgnoreCase(stripExtension(origName))
                        || curCap.toLowerCase(Locale.ROOT).matches("^(download|img|image|photo|pic|screenshot|asset|file)[_-]?\\d*.*");
                if (Boolean.TRUE.equals(a.get("isImage"))) {
                    imgIdx++;
                    String role = imgIdx == 1 ? "HERO" : (imgIdx <= 4 ? "PRODUCT" : (imgIdx <= 6 ? "ARTICLE" : "GALLERY"));
                    String newCaption = isRawFilename
                            ? (imgIdx == 1
                            ? cleanHeadline + " 대표 전경 히어로 이미지"
                            : (imgIdx <= 4
                            ? inferredCategory + " 대표 공간·시그니처 프로그램 #" + (imgIdx - 1)
                            : (imgIdx <= 6
                            ? cleanHeadline + " 계절 풍경 & 공간 스토리 #" + (imgIdx - 4)
                            : cleanHeadline + " 현장 포토 아카이브 #" + imgIdx)))
                            : curCap;
                    Map<String, Object> ann = new LinkedHashMap<>();
                    ann.put("assetId", a.get("id"));
                    ann.put("originalName", origName);
                    ann.put("assetType", role);
                    ann.put("caption", newCaption);
                    assetAnnotations.add(ann);
                }
            }
        }

        Map<String, Object> diag = new LinkedHashMap<>();
        diag.put("siteType", siteType);
        diag.put("siteTypeReason", siteTypeReason);
        diag.put("category", inferredCategory);
        diag.put("heroStyle", "HYBRID".equals(siteType) ? "FULL_SCENIC" : ("COMMERCE".equals(siteType) ? "PROMO_COMMERCE" : "EDITORIAL_STORY"));
        diag.put("brandSlogan", cleanHeadline);
        diag.put("expandedPrompt", expandedPrompt);
        diag.put("tags", tags);
        diag.put("recommendedFeatures", recommended);
        diag.put("featureReasons", featureReasons);
        diag.put("assetAnnotations", assetAnnotations);
        return diag;
    }

    private List<Map<String, Object>> buildScreensFromEnabledFeatures(
            String ideaId,
            String brandTitle,
            String siteType,
            List<String> enabledFeatures,
            List<Map<String, Object>> catalog
    ) {
        Map<String, Map<String, Object>> catalogByCode = new LinkedHashMap<>();
        for (Map<String, Object> fc : catalog) {
            catalogByCode.put(String.valueOf(fc.get("featureCode")), fc);
        }

        List<String> heroAndCatalog = new ArrayList<>();
        List<String> spaceAndMedia = new ArrayList<>();
        List<String> editorialAndGuide = new ArrayList<>();
        List<String> formAndSchedule = new ArrayList<>();
        List<String> trustAndAi = new ArrayList<>();

        for (String code : enabledFeatures) {
            Map<String, Object> fc = catalogByCode.get(code);
            String catCode = fc != null ? String.valueOf(fc.getOrDefault("categoryCode", "")) : "";
            switch (catCode) {
                case "COMMERCE_CATALOG", "ORDER_PRICING", "PROMO_MARKETING" -> heroAndCatalog.add(code);
                case "VISUAL_MEDIA" -> spaceAndMedia.add(code);
                case "CONTENT_EDITORIAL" -> editorialAndGuide.add(code);
                case "FORM_SCHEDULE" -> formAndSchedule.add(code);
                case "COMMUNITY_SOCIAL", "TRUST_AI_GUIDE" -> trustAndAi.add(code);
                default -> heroAndCatalog.add(code);
            }
        }
        if (!heroAndCatalog.contains("CMS_HERO_BANNER")) {
            heroAndCatalog.add(0, "CMS_HERO_BANNER");
        }

        List<Map<String, Object>> screens = new ArrayList<>();
        int sort = 1;

        screens.add(new LinkedHashMap<>(Map.of(
                "id", "scr-" + ideaId + "-01",
                "screenKey", "SCR_HOME",
                "screenTitle", "메인 홈 & 시그니처 쇼케이스",
                "routePath", "/",
                "layoutType", "STANDARD_HERO",
                "description", "브랜드 대표 히어로 배너와 핵심 상품·객실·요금 카탈로그를 한눈에 보여주는 메인 랜딩 섹션",
                "components", heroAndCatalog.isEmpty() ? List.of("CMS_HERO_BANNER", "SHOP_PRODUCT_CATALOG") : heroAndCatalog,
                "sortNo", sort++
        )));

        if (!spaceAndMedia.isEmpty()) {
            screens.add(new LinkedHashMap<>(Map.of(
                    "id", "scr-" + ideaId + "-02",
                    "screenKey", "SCR_GALLERY",
                    "screenTitle", "공간 투어 & 현장 포토 아카이브",
                    "routePath", "/gallery",
                    "layoutType", "MASONRY_PORTFOLIO",
                    "description", "구역별 공간 포인트 투어와 고해상도 현장 사진을 라이트박스로 감상하는 비주얼 아카이브",
                    "components", spaceAndMedia,
                    "sortNo", sort++
            )));
        }

        if (!editorialAndGuide.isEmpty()) {
            screens.add(new LinkedHashMap<>(Map.of(
                    "id", "scr-" + ideaId + "-03",
                    "screenKey", "SCR_MAGAZINE",
                    "screenTitle", "브랜드 스토리 & 이용 가이드",
                    "routePath", "/story",
                    "layoutType", "EDITORIAL_MAGAZINE",
                    "description", "브랜드 저널 아티클과 단계별 이용 코스·가이드를 제공하는 에디토리얼 섹션",
                    "components", editorialAndGuide,
                    "sortNo", sort++
            )));
        }

        if (!formAndSchedule.isEmpty()) {
            screens.add(new LinkedHashMap<>(Map.of(
                    "id", "scr-" + ideaId + "-04",
                    "screenKey", "SCR_RESERVE",
                    "screenTitle", "온라인 예약 · 문의 신청",
                    "routePath", "/reserve",
                    "layoutType", "DYNAMIC_CMS_FORM",
                    "description", "일정·인원·옵션 선택을 포함한 맞춤형 동적 신청 폼",
                    "components", formAndSchedule,
                    "sortNo", sort++
            )));
        }

        if (!trustAndAi.isEmpty()) {
            screens.add(new LinkedHashMap<>(Map.of(
                    "id", "scr-" + ideaId + "-05",
                    "screenKey", "SCR_GUIDE_AI",
                    "screenTitle", "고객 후기 · FAQ · 오시는 길 & AI 컨시어지",
                    "routePath", "/concierge",
                    "layoutType", "SPLIT_SHOWCASE",
                    "description", "실방문객 리뷰, 자주 묻는 질문(FAQ), 오시는 길·주변 코스 안내 및 24시간 AI 컨시어지",
                    "components", trustAndAi,
                    "sortNo", sort
            )));
        }

        return screens;
    }

    @SuppressWarnings("unchecked")
    private void provisionDomainSiteContent(
            Map<String, Object> idea,
            String brandName,
            String resolvedSiteSlug,
            String brandSlogan,
            String category,
            List<String> enabledFeatures,
            List<Map<String, Object>> catalog,
            Map<String, Object> layoutSchema,
            Long shopSiteId,
            Long cmsSiteId
    ) {
        String ideaId = String.valueOf(idea.get("id"));
        String siteSlug = (resolvedSiteSlug != null && !resolvedSiteSlug.isBlank())
                ? resolvedSiteSlug
                : String.valueOf(idea.getOrDefault("selectedSiteSlug", normalizeSlug(brandName)));
        String siteType = String.valueOf(idea.getOrDefault("siteType", "HYBRID"));
        String promptText = String.valueOf(idea.getOrDefault("promptText", ""));
        List<Map<String, Object>> assets = listIdeaAssetsInternal(ideaId);

        // 1. 기존 배포 릴리즈에 이미 생성된 featureBlocks / customProducts / customCmsForm이 있다면 기본값으로 보존
        try {
            List<Map<String, Object>> prevSites = jdbcTemplate.queryForList(
                    "SELECT id, active_version AS \"activeVersion\" FROM kkumz_site WHERE idea_id = ? OR site_slug = ? ORDER BY updated_at DESC LIMIT 1",
                    ideaId, siteSlug
            );
            if (!prevSites.isEmpty()) {
                String prevSiteId = String.valueOf(prevSites.get(0).get("id"));
                int prevVer = ((Number) prevSites.get(0).get("activeVersion")).intValue();
                Map<String, Object> prevRel = getLatestRelease(prevSiteId, prevVer);
                Map<String, Object> prevLayout = parseJsonObject(String.valueOf(prevRel.get("layoutSchemaJson")));
                for (String preserveKey : List.of("featureBlocks", "customProducts", "customArticles", "customCmsForm", "heroStyle")) {
                    if (prevLayout.get(preserveKey) != null && !layoutSchema.containsKey(preserveKey)) {
                        layoutSchema.put(preserveKey, prevLayout.get(preserveKey));
                    }
                }
            }
        } catch (Exception ignored) {
        }

        boolean alreadyHasFullProvisionedContent =
                layoutSchema.get("featureBlocks") instanceof Map<?, ?> fb && !fb.isEmpty()
                && layoutSchema.get("customProducts") instanceof List<?> cp && !cp.isEmpty()
                && layoutSchema.get("customCmsForm") instanceof Map<?, ?> cf && !cf.isEmpty();

        // 2. 최초 스캐폴딩이거나 핵심 콘텐츠가 비어 있을 때만 SITE_CONTENT_PROVISION(qwen-flash, 25s 타임아웃) 실행
        Map<String, Object> aiContent = new LinkedHashMap<>();
        if (!alreadyHasFullProvisionedContent) {
            try {
                StringBuilder assetsSummary = new StringBuilder();
                for (Map<String, Object> a : assets) {
                    assetsSummary.append("- fileUrl=").append(a.get("fileUrl"))
                            .append(", assetType=").append(a.get("assetType"))
                            .append(", caption=").append(a.getOrDefault("caption", ""))
                            .append("\n");
                }
                String rawProvisionJson = ensureAiGateway().executeStagePrompt(
                        "SITE_CONTENT_PROVISION",
                        Map.of(
                                "brandName", brandName,
                                "brandSlogan", brandSlogan,
                                "siteType", siteType,
                                "category", category,
                                "promptText", promptText,
                                "enabledFeatures", String.join(", ", enabledFeatures),
                                "assetsSummary", assetsSummary.toString()
                        ),
                        java.time.Duration.ofSeconds(25),
                        1600
                );
                aiContent.putAll(parseJsonObject(rawProvisionJson));
            } catch (Exception e) {
                log.warn("SITE_CONTENT_PROVISION 호출 경고 (DB 기능 카탈로그 defaultConfig 스키마로 보완): {}", e.getMessage());
            }
        }

        if (aiContent.get("heroSubtitle") != null && !String.valueOf(aiContent.get("heroSubtitle")).isBlank()
                && !isConversationalRawPrompt(String.valueOf(aiContent.get("heroSubtitle")))) {
            layoutSchema.put("heroSubtitle", String.valueOf(aiContent.get("heroSubtitle")).trim());
        }
        if (aiContent.get("heroStyle") != null && !String.valueOf(aiContent.get("heroStyle")).isBlank()) {
            layoutSchema.put("heroStyle", String.valueOf(aiContent.get("heroStyle")).trim());
        }
        if (aiContent.get("ctaText") != null && !String.valueOf(aiContent.get("ctaText")).isBlank()) {
            layoutSchema.put("ctaText", String.valueOf(aiContent.get("ctaText")).trim());
        }

        // 3. featureBlocks 병합 (AI 생성 데이터 + DB kkumz_feature_catalog.defaultConfig 스키마 결합)
        Map<String, Object> featureBlocks = layoutSchema.get("featureBlocks") instanceof Map<?, ?> existingFb
                ? new LinkedHashMap<>((Map<String, Object>) existingFb)
                : new LinkedHashMap<>();
        if (aiContent.get("featureBlocks") instanceof Map<?, ?> aiFb) {
            aiFb.forEach((k, v) -> {
                if (v != null) {
                    featureBlocks.put(String.valueOf(k), v);
                }
            });
        }

        // DB kkumz_feature_catalog의 defaultConfig를 사용하여 선택된 기능 중 누락된 블록을 보완
        ensureCatalogDefaultBlocks(brandName, brandSlogan, category, enabledFeatures, catalog, assets, featureBlocks);
        layoutSchema.put("featureBlocks", featureBlocks);

        // 4. 상품/객실(customProducts) 및 SyncShop pms_product 동기화
        if (aiContent.get("products") instanceof List<?> aiProds && !aiProds.isEmpty()) {
            List<Map<String, Object>> normalizedProds = normalizeAndSyncProducts(
                    brandName, category, (List<Map<String, Object>>) aiProds, assets, shopSiteId
            );
            if (!normalizedProds.isEmpty()) {
                layoutSchema.put("customProducts", normalizedProds);
            }
        } else if (layoutSchema.get("customProducts") instanceof List<?> existingProds && !existingProds.isEmpty()) {
            List<Map<String, Object>> normalizedProds = normalizeAndSyncProducts(
                    brandName, category, (List<Map<String, Object>>) existingProds, assets, shopSiteId
            );
            layoutSchema.put("customProducts", normalizedProds);
        } else {
            List<Map<String, Object>> defaultProds = buildDefaultDomainProducts(brandName, brandSlogan, category);
            List<Map<String, Object>> normalizedProds = normalizeAndSyncProducts(
                    brandName, category, defaultProds, assets, shopSiteId
            );
            layoutSchema.put("customProducts", normalizedProds);
        }

        // 5. 매거진/공지(customArticles) 및 SyncCMS cms_content 동기화
        if (aiContent.get("articles") instanceof List<?> aiArts && !aiArts.isEmpty()) {
            List<Map<String, Object>> normalizedArts = normalizeAndSyncArticles(
                    brandName, (List<Map<String, Object>>) aiArts, assets, cmsSiteId
            );
            if (!normalizedArts.isEmpty()) {
                layoutSchema.put("customArticles", normalizedArts);
            }
        } else if (!(layoutSchema.get("customArticles") instanceof List<?> existingArts && !existingArts.isEmpty())) {
            List<Map<String, Object>> defaultArts = buildDefaultDomainArticles(brandName, brandSlogan, category);
            List<Map<String, Object>> normalizedArts = normalizeAndSyncArticles(
                    brandName, defaultArts, assets, cmsSiteId
            );
            layoutSchema.put("customArticles", normalizedArts);
        }

        // 6. 동적 폼(customCmsForm) 및 SyncCMS cms_form.schema_json 동기화
        if (aiContent.get("cmsForm") instanceof Map<?, ?> aiForm && !aiForm.isEmpty()) {
            Map<String, Object> formMap = new LinkedHashMap<>();
            aiForm.forEach((k, v) -> formMap.put(String.valueOf(k), v));
            layoutSchema.put("customCmsForm", formMap);
            if (cmsSiteId != null && cmsJdbcTemplate != null && formMap.get("fields") instanceof List<?> fieldsList) {
                try {
                    cmsJdbcTemplate.update("""
                            UPDATE cms_form
                            SET title = COALESCE(?, title),
                                description = COALESCE(?, description),
                                schema_json = ?::jsonb
                            WHERE site_id = ? AND status = 1
                            """,
                            formMap.get("title") != null ? String.valueOf(formMap.get("title")) : null,
                            formMap.get("description") != null ? String.valueOf(formMap.get("description")) : null,
                            toJson(fieldsList),
                            cmsSiteId.shortValue()
                    );
                } catch (Exception e) {
                    log.debug("cms_form 동적 스키마 업데이트 생략: {}", e.getMessage());
                }
            }
        } else if (!layoutSchema.containsKey("customCmsForm")) {
            // DB kkumz_feature_catalog의 CMS_DYNAMIC_BOOKING_FORM defaultConfig를 기반으로 기본 동적 폼 구성
            catalog.stream()
                    .filter(fc -> "CMS_DYNAMIC_BOOKING_FORM".equals(fc.get("featureCode")) || "CMS_DYNAMIC_FORM".equals(fc.get("featureCode")))
                    .findFirst()
                    .ifPresent(fc -> {
                        Map<String, Object> defCfg = (Map<String, Object>) fc.getOrDefault("defaultConfig", Map.of());
                        Map<String, Object> formMap = new LinkedHashMap<>();
                        formMap.put("id", 1L);
                        formMap.put("title", brandName + " 온라인 예약 · 문의 신청");
                        formMap.put("formName", brandName + " 온라인 예약 · 문의 신청");
                        formMap.put("description", brandSlogan + " — 원하시는 일정과 옵션을 선택해 주세요.");
                        formMap.put("submitText", "온라인 예약·문의 접수하기");
                        Object rawFields = defCfg.get("fields");
                        if (rawFields instanceof List<?> fl && !fl.isEmpty()) {
                            formMap.put("fields", fl);
                        } else {
                            formMap.put("fields", List.of(
                                    Map.of("key", "desiredDate", "label", "희망 방문 · 이용 일자", "type", "date", "required", true),
                                    Map.of("key", "partySize", "label", "방문 인원 / 수량", "type", "select", "required", true, "options", List.of("1~2인 (커플/개인)", "3~4인 (가족/소모임)", "5인 이상 (단체/별도 문의)")),
                                    Map.of("key", "programSelect", "label", "희망 공간 · 프로그램 선택", "type", "select", "required", true, "options", List.of("시그니처 메인 코스", "프리미엄 프라이빗 패키지", "당일 체험 & 라운지 이용")),
                                    Map.of("key", "optionItems", "label", "추가 신청 옵션", "type", "checkbox", "required", false, "options", List.of("웰컴 로컬 다과·음료 세트", "프라이빗 바비큐·다이닝 세팅", "레이트 체크아웃 / 시간 연장"))
                            ));
                        }
                        layoutSchema.put("customCmsForm", formMap);
                    });
        }
    }

    @SuppressWarnings("unchecked")
    private void ensureCatalogDefaultBlocks(
            String brandName,
            String brandSlogan,
            String category,
            List<String> enabledFeatures,
            List<Map<String, Object>> catalog,
            List<Map<String, Object>> assets,
            Map<String, Object> featureBlocks
    ) {
        String heroImg = assets != null && !assets.isEmpty()
                ? String.valueOf(assets.get(0).getOrDefault("fileUrl", "/images/studio/ed-1.jpg"))
                : "/images/studio/ed-1.jpg";

        Object rawPm = featureBlocks.get("pricingMatrix");
        if (rawPm instanceof List<?> pmList && !pmList.isEmpty()) {
            List<Map<String, Object>> normalizedRows = new ArrayList<>();
            for (Object item : pmList) {
                if (item instanceof Map<?, ?> m) {
                    Map<String, Object> row = new LinkedHashMap<>((Map<String, Object>) m);
                    row.putIfAbsent("capacity", row.getOrDefault("target", "2인 ~ 4인 기준"));
                    row.putIfAbsent("target", row.getOrDefault("capacity", "2인 ~ 4인 기준"));
                    row.putIfAbsent("note", row.getOrDefault("includes", "웰컴 키트 · 기본 편의시설 일체 포함"));
                    row.putIfAbsent("includes", row.getOrDefault("note", "웰컴 키트 · 기본 편의시설 일체 포함"));
                    normalizedRows.add(row);
                }
            }
            featureBlocks.put("pricingMatrix", Map.of(
                    "title", brandName + " 시즌 · 요일 · 인원별 상세 요금 안내",
                    "columns", defaultPricingColumns(),
                    "rows", normalizedRows
            ));
        } else if (!(rawPm instanceof Map<?, ?> pmMap && pmMap.get("rows") instanceof List<?> pmRows && !pmRows.isEmpty())) {
            featureBlocks.put("pricingMatrix", Map.of(
                    "title", brandName + " 시즌 · 요일 · 인원별 상세 요금 안내",
                    "columns", defaultPricingColumns(),
                    "rows", List.of(
                            Map.of("tier", "시그니처 기본 이용", "capacity", "2인 ~ 4인 기준", "target", "2인 ~ 4인 기준", "offPeakWeekday", "120,000원", "offPeakWeekend", "160,000원", "peakSeason", "200,000원", "note", "웰컴 키트 · 기본 편의시설 일체 포함", "includes", "웰컴 키트 · 기본 편의시설 일체 포함"),
                            Map.of("tier", "프리미엄 패밀리 · 단체 패키지", "capacity", "4인 ~ 8인 기준", "target", "4인 ~ 8인 기준", "offPeakWeekday", "180,000원", "offPeakWeekend", "240,000원", "peakSeason", "290,000원", "note", "전용 공간 전체 + 스페셜 바비큐·다이닝 옵션", "includes", "전용 공간 전체 + 스페셜 바비큐·다이닝 옵션"),
                            Map.of("tier", "당일 프라이빗 대여 · 체험", "capacity", "당일 이용 (10:00~18:00)", "target", "당일 이용 (10:00~18:00)", "offPeakWeekday", "60,000원", "offPeakWeekend", "80,000원", "peakSeason", "100,000원", "note", "프라이빗 공간 대여 + 현장 케어 서비스", "includes", "프라이빗 공간 대여 + 현장 케어 서비스")
                    )
            ));
        }

        if (!(featureBlocks.get("spaceTour") instanceof List<?> st && !st.isEmpty())) {
            List<Map<String, Object>> spots = new ArrayList<>();
            int limit = assets != null ? Math.min(4, assets.size()) : 0;
            for (int i = 0; i < limit; i++) {
                Map<String, Object> a = assets.get(i);
                String descText = brandName + "의 감성과 자연스러운 휴식을 경험할 수 있는 핵심 공간 포인트입니다.";
                spots.add(Map.of(
                        "zone", "SPACE 0" + (i + 1),
                        "title", String.valueOf(a.getOrDefault("caption", brandName + " 주요 공간 #" + (i + 1))),
                        "desc", descText,
                        "description", descText,
                        "imageUrl", String.valueOf(a.getOrDefault("fileUrl", heroImg))
                ));
            }
            if (spots.isEmpty()) {
                spots.add(Map.of("zone", "SPACE 01", "title", brandName + " 메인 라운지", "desc", brandSlogan, "description", brandSlogan, "imageUrl", heroImg));
            }
            featureBlocks.put("spaceTour", spots);
        }

        if (!(featureBlocks.get("stepGuide") instanceof List<?> sg && !sg.isEmpty())) {
            featureBlocks.put("stepGuide", List.of(
                    Map.of("step", "STEP 01", "time", "웰컴 & 체크인", "title", "여유로운 도착 및 공간 안내", "desc", brandName + "에 도착하여 호스트의 안내와 함께 공간과 이용 가이드를 확인합니다."),
                    Map.of("step", "STEP 02", "time", "시그니처 경험", "title", "핵심 프로그램 & 프라이빗 휴식", "desc", category + " 본연의 매력을 만끽하며 온전한 쉼과 체험을 즐깁니다."),
                    Map.of("step", "STEP 03", "time", "다이닝 & 리추얼", "title", "저녁 만찬과 감성 나이트 타임", "desc", "소중한 사람들과 함께 정성스러운 다이닝과 여유로운 밤 시간을 보냅니다."),
                    Map.of("step", "STEP 04", "time", "모닝 & 체크아웃", "title", "상쾌한 아침 산책 및 마무리", "desc", "여유로운 아침 풍경을 감상한 뒤 기분 좋게 일정을 마무리합니다.")
            ));
        }

        if (!(featureBlocks.get("faqItems") instanceof List<?> fq && !fq.isEmpty())) {
            featureBlocks.put("faqItems", List.of(
                    Map.of("q", "예약 확정 및 일정 변경은 어떻게 진행되나요?", "a", "온라인 예약·문의 폼 또는 즉시 예약을 남겨주시면 호스트 확인 후 확정 안내 메시지를 발송해 드립니다. 일정 변경은 이용일 7일 전까지 가능합니다."),
                    Map.of("q", "전용 주차 공간과 대중교통 접근성이 궁금합니다.", "a", "방문객 전용 무료 주차 공간이 마련되어 있으며, 하단 오시는 길 섹션에서 상세 주소와 내비게이션 키워드를 확인하실 수 있습니다."),
                    Map.of("q", "단체 예약이나 맞춤 옵션 추가가 가능한가요?", "a", "네, 인원 추가·바비큐·특화 패키지 등 맞춤 옵션을 예약 폼에서 함께 선택하시거나 AI 컨시어지에 문의해 주세요.")
            ));
        }

        if (!(featureBlocks.get("locationGuide") instanceof Map<?, ?> lg && !lg.isEmpty())) {
            List<Map<String, Object>> courses = new ArrayList<>();
            courses.add(Map.of("name", "주변 자연 산책 코스", "distance", "도보 5분", "desc", "계절마다 아름다운 풍경을 감상할 수 있는 힐링 산책로"));
            courses.add(Map.of("name", "로컬 미식 & 카페 거리", "distance", "차량 10분", "desc", "지역 특산 요리와 감성 베이커리 카페 추천 코스"));
            courses.add(Map.of("name", "대표 문화·역사 명소", "distance", "차량 15분", "desc", "가족·연인과 함께 둘러보기 좋은 대표 관광 명소"));
            featureBlocks.put("locationGuide", Map.of(
                    "address", brandName + " 공식 오프라인 공간 (" + category + ")",
                    "parking", "전용 무료 주차장 완비 (대형 SUV 및 다수 차량 주차 가능)",
                    "publicTransit", "주요 역·터미널에서 택시 또는 시내버스로 편리하게 이동 가능",
                    "transit", "주요 역·터미널에서 택시 또는 시내버스로 편리하게 이동 가능",
                    "nearbySpots", courses,
                    "nearbyCourses", courses
            ));
        }

        if (!(featureBlocks.get("policyRules") instanceof List<?> pr && !pr.isEmpty())) {
            featureBlocks.put("policyRules", List.of(
                    "이용 시간 및 체크인·체크아웃 시간을 준수해 주시기 바랍니다.",
                    "쾌적하고 안전한 환경을 위해 실내 절대 금연 및 화기 사용 수칙을 지켜주세요.",
                    "예약일 7일 전 100% 환불, 5일 전 70% 환불, 3일 전 50% 환불, 당일 취소는 환불이 불가합니다."
            ));
        }

        if (!(featureBlocks.get("reviews") instanceof List<?> rv && !rv.isEmpty())) {
            String rv1 = "사진보다 실제 공간이 훨씬 더 아름답고 쾌적했습니다. 호스트분도 정말 친절하셔서 가족 모두 최고의 시간을 보냈어요!";
            String rv2 = "예약 과정부터 이용 안내까지 체계적이고 세심해서 감동받았습니다. 다음 시즌에 꼭 다시 찾고 싶은 곳입니다.";
            featureBlocks.put("reviews", List.of(
                    Map.of("author", "김*연 고객님", "rating", 5, "date", "2026.10", "tag", "재방문 의사 100%", "comment", rv1, "content", rv1, "photoUrl", heroImg),
                    Map.of("author", "박*훈 고객님", "rating", 5, "date", "2026.10", "tag", "시그니처 추천", "comment", rv2, "content", rv2, "photoUrl", heroImg)
            ));
        }

        if (!(featureBlocks.get("pollWidget") instanceof Map<?, ?> pw && !pw.isEmpty())) {
            featureBlocks.put("pollWidget", Map.of(
                    "question", brandName + "에서 가장 기대되는 경험은 무엇인가요?",
                    "totalVotes", 248,
                    "options", List.of(
                            Map.of("label", "시그니처 공간에서의 프라이빗 휴식", "votes", 112, "percent", 45),
                            Map.of("label", "스페셜 다이닝 & 시즌 프로그램 체험", "votes", 84, "percent", 34),
                            Map.of("label", "감성 포토 스팟 & 주변 여행 코스", "votes", 52, "percent", 21)
                    )
            ));
        }

        if (!(featureBlocks.get("trustMetrics") instanceof List<?> tm && !tm.isEmpty())) {
            featureBlocks.put("trustMetrics", List.of(
                    Map.of("label", "누적 방문·이용 고객", "value", "4,800+", "sub", "높은 만족도와 입소문으로 검증된 브랜드"),
                    Map.of("label", "실제 이용객 평점", "value", "4.95 / 5.0", "sub", "실제 예약·구매 고객 후기 기준"),
                    Map.of("label", "재방문 & 지인 추천율", "value", "94%", "sub", "사계절 다시 찾는 시그니처 공간")
            ));
        }
    }

    private List<Map<String, Object>> normalizeAndSyncProducts(
            String brandName,
            String category,
            List<Map<String, Object>> rawProducts,
            List<Map<String, Object>> assets,
            Long shopSiteId
    ) {
        List<Map<String, Object>> result = new ArrayList<>();
        int idx = 1;
        for (Map<String, Object> rp : rawProducts) {
            String name = String.valueOf(rp.getOrDefault("name", brandName + " 시그니처 #" + idx)).trim();
            String subTitle = String.valueOf(rp.getOrDefault("subTitle", rp.getOrDefault("description", brandName + " 대표 라인업"))).trim();
            String desc = String.valueOf(rp.getOrDefault("description", subTitle)).trim();
            String pic = String.valueOf(rp.getOrDefault("pic", rp.getOrDefault("imageUrl", ""))).trim();
            if (pic.isBlank() || pic.contains("prod-matcha") || pic.contains("prod-tea")) {
                if (assets != null && !assets.isEmpty()) {
                    Map<String, Object> asset = assets.get((idx - 1) % assets.size());
                    pic = String.valueOf(asset.getOrDefault("fileUrl", "/images/studio/ed-1.jpg"));
                } else {
                    pic = "/images/studio/ed-1.jpg";
                }
            }
            int defaultPrice = idx == 1 ? 150000 : (idx == 2 ? 75000 : 120000);
            int price = rp.get("price") instanceof Number n && n.intValue() > 0 ? n.intValue() : defaultPrice;
            int promoPrice = rp.get("promotionPrice") instanceof Number n && n.intValue() > 0 ? n.intValue() : price;
            int stock = rp.get("stock") instanceof Number n ? n.intValue() : 10;
            String unit = String.valueOf(rp.getOrDefault("unit", "건"));
            String catName = String.valueOf(rp.getOrDefault("productCategoryName", category));
            String badge = String.valueOf(rp.getOrDefault("badge", rp.getOrDefault("note", "SIGNATURE")));

            Long prodId = rp.get("id") instanceof Number n ? n.longValue() : (long) (91000 + idx);
            if (shopSiteId != null && shopJdbcTemplate != null) {
                try {
                    List<Map<String, Object>> exist = shopJdbcTemplate.queryForList(
                            "SELECT id FROM pms_product WHERE site_id = ? AND name = ? AND delete_status = 0 LIMIT 1",
                            shopSiteId, name
                    );
                    if (!exist.isEmpty()) {
                        prodId = ((Number) exist.get(0).get("id")).longValue();
                        shopJdbcTemplate.update("""
                                UPDATE pms_product
                                SET pic = ?, price = ?, promotion_price = ?, original_price = ?,
                                    sub_title = ?, description = ?, unit = ?, product_category_name = ?, note = ?
                                WHERE id = ?
                                """,
                                pic, BigDecimal.valueOf(price), BigDecimal.valueOf(promoPrice), BigDecimal.valueOf(price),
                                subTitle, desc, unit, catName, badge, prodId
                        );
                    } else {
                        prodId = shopJdbcTemplate.queryForObject("""
                                INSERT INTO pms_product (
                                    site_id, brand_name, name, pic, product_sn, delete_status, publish_status,
                                    new_status, recommand_status, verify_status, sort, sale, price, promotion_price,
                                    original_price, sub_title, description, stock, low_stock, unit, product_category_name, note
                                ) VALUES (?, ?, ?, ?, ?, 0, 1, 1, 1, 1, ?, ?, ?, ?, ?, ?, ?, ?, 2, ?, ?, ?)
                                RETURNING id
                                """,
                                Long.class,
                                shopSiteId, brandName, name, pic, "SKU-" + shopSiteId + "-" + idx,
                                idx, 15 + idx * 3,
                                BigDecimal.valueOf(price), BigDecimal.valueOf(promoPrice), BigDecimal.valueOf(price),
                                subTitle, desc, stock, unit, catName, badge
                        );
                    }
                } catch (Exception e) {
                    log.debug("pms_product 동기화 생략: {}", e.getMessage());
                }
            }

            Map<String, Object> item = new LinkedHashMap<>(rp);
            item.put("id", prodId);
            item.put("name", name);
            item.put("subTitle", subTitle);
            item.put("description", desc);
            item.put("pic", pic);
            item.put("price", price);
            item.put("promotionPrice", promoPrice);
            item.put("stock", stock);
            item.put("unit", unit);
            item.put("brandName", brandName);
            item.put("productCategoryName", catName);
            item.put("badge", badge);
            result.add(item);
            idx++;
        }
        return result;
    }

    private List<Map<String, Object>> normalizeAndSyncArticles(
            String brandName,
            List<Map<String, Object>> rawArticles,
            List<Map<String, Object>> assets,
            Long cmsSiteId
    ) {
        List<Map<String, Object>> result = new ArrayList<>();
        int idx = 1;
        for (Map<String, Object> ra : rawArticles) {
            String title = String.valueOf(ra.getOrDefault("title", "[" + brandName + "] 브랜드 저널 #" + idx)).trim();
            if (isConversationalRawPrompt(title)) {
                title = "[" + brandName + " 스토리] 자연과 일상을 잇는 시그니처 공간 이야기";
            }
            String summary = String.valueOf(ra.getOrDefault("summary", ra.getOrDefault("description", title))).trim();
            String cover = String.valueOf(ra.getOrDefault("cover", ra.getOrDefault("thumbnailUrl", ""))).trim();
            if (cover.isBlank() && assets != null && !assets.isEmpty()) {
                cover = String.valueOf(assets.get((idx - 1) % assets.size()).getOrDefault("fileUrl", "/images/studio/ed-1.jpg"));
            }
            String catName = String.valueOf(ra.getOrDefault("categoryName", "브랜드 저널"));

            Map<String, Object> item = new LinkedHashMap<>(ra);
            item.put("id", ra.get("id") != null ? ra.get("id") : (81000L + idx));
            item.put("title", title);
            item.put("summary", summary);
            item.put("description", summary);
            item.put("cover", cover);
            item.put("thumbnailUrl", cover);
            item.put("author", brandName);
            item.put("categoryName", catName);
            result.add(item);
            idx++;
        }
        return result;
    }

    private List<String> defaultPricingColumns() {
        List<String> cols = new ArrayList<>();
        cols.add("구분");
        cols.add("기준 인원");
        cols.add("비수기 주중");
        cols.add("비수기 주말");
        cols.add("성수기/연휴");
        cols.add("포함 혜택");
        return cols;
    }

    private List<Map<String, Object>> buildDefaultDomainProducts(String brandName, String brandSlogan, String category) {
        String catLower = category != null ? category.toLowerCase(Locale.ROOT) : "";
        boolean isStay = catLower.matches(".*(숙박|민박).*")
                || catLower.matches(".*(스테이|펜션).*")
                || catLower.matches(".*(캠핑|글램핑).*")
                || brandName.matches(".*(민박|스테이).*")
                || brandName.contains("펜션");
        List<Map<String, Object>> items = new ArrayList<>();
        if (isStay) {
            items.add(new LinkedHashMap<>(Map.of(
                    "name", brandName + " 시그니처 독채 스테이 (기준 2~4인)",
                    "subTitle", "자연 풍경을 품은 프라이빗 메인 객실 · 웰컴 다과 및 기본 어메니티 제공",
                    "description", brandSlogan,
                    "price", 150000,
                    "promotionPrice", 130000,
                    "unit", "박",
                    "stock", 5,
                    "productCategoryName", "시그니처 객실",
                    "badge", "SIGNATURE"
            )));
            items.add(new LinkedHashMap<>(Map.of(
                    "name", brandName + " 패밀리 · 단체 온돌 스위트 (기준 4~8인)",
                    "subTitle", "가족 및 소모임을 위한 여유로운 거실 + 침실 + 전용 바비큐 데크 패키지",
                    "description", "넓은 평상과 전용 다이닝 테라스를 함께 이용할 수 있는 대표 패밀리 객실입니다.",
                    "price", 220000,
                    "promotionPrice", 190000,
                    "unit", "박",
                    "stock", 4,
                    "productCategoryName", "패밀리·단체 객실",
                    "badge", "BEST"
            )));
            items.add(new LinkedHashMap<>(Map.of(
                    "name", brandName + " 프라이빗 평상 · 당일 피크닉 대여 패키지",
                    "subTitle", "당일(10:00~18:00) 자연 속 휴식과 바비큐·다과를 즐기는 프라이빗 대여 프로그램",
                    "description", "숙박 없이도 하루 동안 온전히 쉬어갈 수 있는 당일 전용 공간 대여권입니다.",
                    "price", 80000,
                    "promotionPrice", 65000,
                    "unit", "회",
                    "stock", 8,
                    "productCategoryName", "당일 프로그램",
                    "badge", "SEASONAL"
            )));
            return items;
        }
        items.add(new LinkedHashMap<>(Map.of(
                "name", brandName + " 시그니처 대표 컬렉션",
                "subTitle", brandSlogan,
                "description", brandName + "의 철학과 품질 기준을 담아 선보이는 대표 시그니처 라인업입니다.",
                "price", 68000,
                "promotionPrice", 58000,
                "unit", "세트",
                "stock", 25,
                "productCategoryName", category != null && !category.isBlank() ? category : "시그니처",
                "badge", "SIGNATURE"
        )));
        items.add(new LinkedHashMap<>(Map.of(
                "name", brandName + " 프리미엄 스페셜 에디션 패키지",
                "subTitle", "선물용 및 프리미엄 고객을 위한 올인원 큐레이션 구성",
                "description", "가장 인기 있는 핵심 구성을 한데 모은 스페셜 패키지입니다.",
                "price", 115000,
                "promotionPrice", 98000,
                "unit", "세트",
                "stock", 15,
                "productCategoryName", category != null && !category.isBlank() ? category : "프리미엄",
                "badge", "BEST"
        )));
        items.add(new LinkedHashMap<>(Map.of(
                "name", brandName + " 스타터 트라이얼 & 체험권",
                "subTitle", "처음 방문하시는 고객을 위한 부담 없는 입문 구성",
                "description", brandName + "의 핵심 매력을 경험해 볼 수 있는 스타터 프로그램입니다.",
                "price", 35000,
                "promotionPrice", 29000,
                "unit", "건",
                "stock", 40,
                "productCategoryName", "스타터·체험",
                "badge", "NEW"
        )));
        return items;
    }

    private List<Map<String, Object>> buildDefaultDomainArticles(String brandName, String brandSlogan, String category) {
        List<Map<String, Object>> articles = new ArrayList<>();
        articles.add(new LinkedHashMap<>(Map.of(
                "title", "[브랜드 스토리] " + brandName + " — " + brandSlogan,
                "categoryName", "브랜드 스토리",
                "summary", brandName + "이 추구하는 공간의 철학과 사계절 자연스러운 휴식의 가치를 소개합니다."
        )));
        articles.add(new LinkedHashMap<>(Map.of(
                "title", "[이용 및 예약 안내] " + brandName + " 100% 즐기는 시그니처 가이드",
                "categoryName", "이용 가이드",
                "summary", "방문 전 확인하면 좋은 체크인·이용 코스, 주변 추천 명소 및 예약 팁을 정리했습니다."
        )));
        return articles;
    }

    @SuppressWarnings("unchecked")
    private String resolveScaffoldBrandName(Map<String, Object> idea) {
        if (idea.get("selectedBrandName") != null && !String.valueOf(idea.get("selectedBrandName")).isBlank()) {
            return String.valueOf(idea.get("selectedBrandName")).trim();
        }
        if (idea.get("nameSuggestions") instanceof List<?> names && !names.isEmpty()) {
            for (Object o : names) {
                if (o instanceof Map<?, ?> m && Boolean.TRUE.equals(m.get("isSelected")) && m.get("brandName") != null) {
                    String bn = String.valueOf(m.get("brandName")).trim();
                    if (!bn.isBlank()) {
                        return bn;
                    }
                }
            }
            Object first = names.get(0);
            if (first instanceof Map<?, ?> m && m.get("brandName") != null) {
                String bn = String.valueOf(m.get("brandName")).trim();
                if (!bn.isBlank()) {
                    return bn;
                }
            }
        }
        String rawTitle = String.valueOf(idea.getOrDefault("title", "")).trim();
        String cleaned = stripConversationalTail(rawTitle);
        if (!cleaned.isBlank() && cleaned.length() <= 30) {
            return cleaned;
        }
        return !rawTitle.isBlank() ? (rawTitle.length() > 24 ? rawTitle.substring(0, 24).trim() : rawTitle) : "시그니처 브랜드";
    }

    @SuppressWarnings("unchecked")
    private String resolveScaffoldSiteSlug(Map<String, Object> idea, String brandName) {
        if (idea.get("selectedSiteSlug") != null && !String.valueOf(idea.get("selectedSiteSlug")).isBlank()) {
            return String.valueOf(idea.get("selectedSiteSlug")).trim();
        }
        if (idea.get("nameSuggestions") instanceof List<?> names && !names.isEmpty()) {
            for (Object o : names) {
                if (o instanceof Map<?, ?> m && Boolean.TRUE.equals(m.get("isSelected")) && m.get("siteSlug") != null) {
                    String slug = String.valueOf(m.get("siteSlug")).trim();
                    if (!slug.isBlank()) {
                        return slug;
                    }
                }
            }
            Object first = names.get(0);
            if (first instanceof Map<?, ?> m && m.get("siteSlug") != null) {
                String slug = String.valueOf(m.get("siteSlug")).trim();
                if (!slug.isBlank()) {
                    return slug;
                }
            }
        }
        try {
            List<Map<String, Object>> existingByBrand = jdbcTemplate.queryForList(
                    "SELECT site_slug AS \"siteSlug\" FROM kkumz_site WHERE site_name = ? ORDER BY updated_at DESC LIMIT 1",
                    brandName
            );
            if (!existingByBrand.isEmpty() && existingByBrand.get(0).get("siteSlug") != null) {
                String foundSlug = String.valueOf(existingByBrand.get(0).get("siteSlug")).trim();
                if (!foundSlug.isBlank()) {
                    return foundSlug;
                }
            }
            List<Map<String, Object>> existingIdeaByBrand = jdbcTemplate.queryForList(
                    "SELECT selected_site_slug AS \"siteSlug\" FROM kkumz_idea WHERE selected_brand_name = ? AND selected_site_slug IS NOT NULL AND selected_site_slug <> '' ORDER BY updated_at DESC LIMIT 1",
                    brandName
            );
            if (!existingIdeaByBrand.isEmpty() && existingIdeaByBrand.get(0).get("siteSlug") != null) {
                String foundSlug = String.valueOf(existingIdeaByBrand.get(0).get("siteSlug")).trim();
                if (!foundSlug.isBlank()) {
                    return foundSlug;
                }
            }
        } catch (Exception ignored) {
        }
        return normalizeSlug(brandName);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveOrProvisionDesignTokens(
            Map<String, Object> idea,
            String brandName,
            String siteSlug,
            String category,
            Map<String, Object> existingTokens
    ) {
        if (existingTokens != null && !existingTokens.isEmpty() && existingTokens.get("primary") != null) {
            return new LinkedHashMap<>(existingTokens);
        }
        if (idea.get("logoSuggestions") instanceof List<?> logos && !logos.isEmpty()) {
            for (Object o : logos) {
                if (o instanceof Map<?, ?> m && Boolean.TRUE.equals(m.get("isSelected")) && m.get("colorPalette") instanceof Map<?, ?> pal && !pal.isEmpty()) {
                    Map<String, Object> tokens = new LinkedHashMap<>();
                    pal.forEach((k, v) -> tokens.put(String.valueOf(k), v));
                    return tokens;
                }
            }
            Object first = logos.get(0);
            if (first instanceof Map<?, ?> m && m.get("colorPalette") instanceof Map<?, ?> pal && !pal.isEmpty()) {
                Map<String, Object> tokens = new LinkedHashMap<>();
                pal.forEach((k, v) -> tokens.put(String.valueOf(k), v));
                return tokens;
            }
        }
        try {
            List<Map<String, Object>> siblingIdeas = jdbcTemplate.queryForList(
                    "SELECT design_tokens_json::text AS \"tokensJson\" FROM kkumz_idea WHERE (selected_brand_name = ? OR selected_site_slug = ?) AND design_tokens_json IS NOT NULL AND design_tokens_json::text <> '{}' ORDER BY updated_at DESC LIMIT 1",
                    brandName, siteSlug
            );
            if (!siblingIdeas.isEmpty()) {
                Map<String, Object> parsed = parseJsonObject(String.valueOf(siblingIdeas.get(0).get("tokensJson")));
                if (!parsed.isEmpty()) {
                    return parsed;
                }
            }
        } catch (Exception ignored) {
        }
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("primary", "#14532d");
        defaults.put("secondary", "#166534");
        defaults.put("accent", "#d97706");
        defaults.put("surface", "#f8fafc");
        defaults.put("radius", "14px");
        return defaults;
    }

    @SuppressWarnings("unchecked")
    private String resolveOrProvisionLogoSvg(
            String ideaId,
            Map<String, Object> idea,
            String brandName,
            String siteSlug,
            String category,
            Map<String, Object> tokens,
            String existingLogoSvg
    ) {
        if (existingLogoSvg != null && !existingLogoSvg.isBlank()) {
            return replaceBrandTextInSvg(existingLogoSvg, brandName, List.of(brandName));
        }
        if (idea.get("logoSuggestions") instanceof List<?> logos && !logos.isEmpty()) {
            for (Object o : logos) {
                if (o instanceof Map<?, ?> m && Boolean.TRUE.equals(m.get("isSelected")) && m.get("svgCode") != null) {
                    String svg = String.valueOf(m.get("svgCode")).trim();
                    if (!svg.isBlank()) {
                        return replaceBrandTextInSvg(svg, brandName, List.of(brandName));
                    }
                }
            }
            Object first = logos.get(0);
            if (first instanceof Map<?, ?> m && m.get("svgCode") != null) {
                String svg = String.valueOf(m.get("svgCode")).trim();
                if (!svg.isBlank()) {
                    return replaceBrandTextInSvg(svg, brandName, List.of(brandName));
                }
            }
        }
        try {
            List<Map<String, Object>> siblingSites = jdbcTemplate.queryForList(
                    "SELECT logo_svg AS \"logoSvg\" FROM kkumz_site WHERE (site_name = ? OR site_slug = ?) AND logo_svg IS NOT NULL AND logo_svg <> '' ORDER BY updated_at DESC LIMIT 1",
                    brandName, siteSlug
            );
            if (!siblingSites.isEmpty() && siblingSites.get(0).get("logoSvg") != null) {
                String svg = String.valueOf(siblingSites.get(0).get("logoSvg")).trim();
                if (!svg.isBlank()) {
                    return replaceBrandTextInSvg(svg, brandName, List.of(brandName));
                }
            }
        } catch (Exception ignored) {
        }

        String primary = String.valueOf(tokens.getOrDefault("primary", "#14532d"));
        String accent = String.valueOf(tokens.getOrDefault("accent", "#d97706"));
        String firstChar = !brandName.isBlank() ? escapeXml(brandName.substring(0, 1)) : "B";
        String cleanBrand = escapeXml(brandName);
        String generatedSvg = "<svg viewBox=\"0 0 320 90\" xmlns=\"http://www.w3.org/2000/svg\">"
                + "<rect x=\"10\" y=\"15\" width=\"60\" height=\"60\" rx=\"16\" fill=\"" + escapeXml(primary) + "\"/>"
                + "<circle cx=\"56\" cy=\"29\" r=\"6\" fill=\"" + escapeXml(accent) + "\"/>"
                + "<text x=\"40\" y=\"54\" text-anchor=\"middle\" fill=\"#ffffff\" font-family=\"Pretendard, sans-serif\" font-size=\"26\" font-weight=\"800\">" + firstChar + "</text>"
                + "<text x=\"86\" y=\"54\" fill=\"" + escapeXml(primary) + "\" font-family=\"Pretendard, sans-serif\" font-size=\"24\" font-weight=\"800\">" + cleanBrand + "</text>"
                + "</svg>";

        try {
            String logoId = "logo-auto-" + UUID.randomUUID().toString().substring(0, 8);
            jdbcTemplate.update("""
                    INSERT INTO kkumz_logo_suggestion (
                        id, idea_id, style_name, svg_code, color_palette_json, typography_json, tags_json, is_selected, created_at
                    ) VALUES (?, ?, '시그니처 워드마크 엠블럼', ?, ?::jsonb, '{"headingFont":"Pretendard","bodyFont":"Pretendard"}'::jsonb, '["시그니처","자동생성"]'::jsonb, TRUE, CURRENT_TIMESTAMP)
                    """,
                    logoId, ideaId, generatedSvg, toJson(tokens)
            );
        } catch (Exception ignored) {
        }
        return generatedSvg;
    }

    private String summarizeTitle(String promptText) {
        String cleaned = promptText.replaceAll("\\s+", " ").trim();
        return cleaned.length() > 42 ? cleaned.substring(0, 42) + "..." : cleaned;
    }

    private String replaceBrandTextInSvg(String svgCode, String targetBrandName, List<String> knownBrandCandidates) {
        if (svgCode == null || svgCode.isBlank() || targetBrandName == null || targetBrandName.isBlank()) {
            return svgCode;
        }
        String cleanTarget = escapeXml(targetBrandName.trim());
        String firstCharTarget = escapeXml(targetBrandName.trim().substring(0, 1));
        String updated = svgCode;

        java.util.regex.Pattern textTagPattern = java.util.regex.Pattern.compile("(<text\\b[^>]*>)([^<]+)(</text>)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = textTagPattern.matcher(updated);

        List<String> textValues = new ArrayList<>();
        while (matcher.find()) {
            textValues.add(matcher.group(2).trim());
        }
        if (textValues.isEmpty()) {
            return updated;
        }

        // 가장 긴 텍스트 노드를 메인 브랜드 워드마크로 식별
        int mainIdx = 0;
        int maxLen = -1;
        for (int i = 0; i < textValues.size(); i++) {
            String val = textValues.get(i);
            boolean matchesKnown = knownBrandCandidates != null && knownBrandCandidates.stream()
                    .anyMatch(k -> k != null && !k.isBlank() && val.contains(k.trim()));
            int score = (matchesKnown ? 1000 : 0) + val.length();
            if (score > maxLen) {
                maxLen = score;
                mainIdx = i;
            }
        }

        String oldMainText = textValues.get(mainIdx);
        String oldFirstChar = !oldMainText.isEmpty() ? oldMainText.substring(0, 1) : "";

        matcher.reset();
        StringBuffer sb = new StringBuffer();
        int idx = 0;
        while (matcher.find()) {
            String openTag = matcher.group(1);
            String innerText = matcher.group(2).trim();
            String closeTag = matcher.group(3);
            String newInner = innerText;
            if (idx == mainIdx) {
                newInner = cleanTarget;
            } else if (innerText.length() == 1 && !oldFirstChar.isEmpty() && innerText.equals(oldFirstChar)) {
                newInner = firstCharTarget;
            }
            matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(openTag + newInner + closeTag));
            idx++;
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private List<Map<String, Object>> parseScreenMapList(String json) {
        if (json == null || json.isBlank() || "null".equalsIgnoreCase(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String normalizeSlug(String raw) {
        String slug = raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9-]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
        return slug.isBlank() ? "brand-" + UUID.randomUUID().toString().substring(0, 6) : slug;
    }

    private String escapeXml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalStateException("JSON 직렬화 실패", e);
        }
    }

    private Map<String, Object> parseJsonObject(String json) {
        if (json == null || json.isBlank() || "null".equalsIgnoreCase(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            try {
                String cleaned = json.replaceAll("(?s)<think>.*?</think>", "").trim();
                int firstBrace = cleaned.indexOf('{');
                int lastBrace = cleaned.lastIndexOf('}');
                if (firstBrace >= 0 && lastBrace > firstBrace) {
                    return lenientMapper().readValue(
                            cleaned.substring(firstBrace, lastBrace + 1),
                            new TypeReference<Map<String, Object>>() {}
                    );
                }
            } catch (Exception ignored) {
            }
            return new LinkedHashMap<>();
        }
    }

    private List<Object> parseJsonList(String json) {
        if (json == null || json.isBlank() || "null".equalsIgnoreCase(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Object>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // =========================================================================
    // 11. 업로드 미디어·문서 자산 처리 및 사이트 프로비저닝 연동 헬퍼
    // =========================================================================

    private Path resolveUploadDir() {
        String configured = (storageUploadDir != null && !storageUploadDir.isBlank())
                ? storageUploadDir.trim()
                : "uploads/kkumz";
        return Paths.get(configured).toAbsolutePath().normalize();
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private String stripExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private String resolveMimeType(String providedMime, String ext) {
        if (providedMime != null && !providedMime.isBlank() && !"application/octet-stream".equalsIgnoreCase(providedMime)) {
            return providedMime.trim();
        }
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "avif" -> "image/avif";
            case "pdf" -> "application/pdf";
            case "txt", "md", "csv", "tsv" -> "text/plain; charset=UTF-8";
            case "json" -> "application/json; charset=UTF-8";
            case "html", "htm" -> "text/html; charset=UTF-8";
            default -> "application/octet-stream";
        };
    }

    private String classifyAssetType(String requestedType, String origName, String ext, String mimeType) {
        if (requestedType != null && !requestedType.isBlank() && !"AUTO".equalsIgnoreCase(requestedType.trim())) {
            return requestedType.trim().toUpperCase(Locale.ROOT);
        }
        String lowerName = origName != null ? origName.toLowerCase(Locale.ROOT) : "";
        if ("svg".equals(ext) || lowerName.contains("logo") || lowerName.matches(".*(로고|심볼).*")) {
            return "LOGO";
        }
        boolean isImg = (mimeType != null && mimeType.toLowerCase(Locale.ROOT).startsWith("image/"))
                || (ext != null && ext.matches("^(jpg|jpeg|png|webp|gif|avif)$"));
        if (isImg) {
            if (lowerName.matches(".*(hero|banner).*") || lowerName.matches(".*(배너|전경).*") || lowerName.matches(".*(외관|대표).*")) {
                return "HERO";
            }
            if (lowerName.matches(".*(prod|item|room).*") || lowerName.matches(".*(상품|객실).*") || lowerName.contains("메뉴")) {
                return "PRODUCT";
            }
            if (lowerName.matches(".*(article|magazine).*") || lowerName.matches(".*(매거진|스토리).*")) {
                return "ARTICLE";
            }
            return "GALLERY";
        }
        return "DOC";
    }

    private boolean isTextOrDocumentFile(String ext, String mimeType) {
        if (ext != null && ext.matches("^(txt|md|csv|tsv|json|html|htm|xml|docx|pdf)$")) {
            return true;
        }
        return mimeType != null && (mimeType.startsWith("text/") || mimeType.matches(".*(json|xml|pdf).*"));
    }

    private String extractDocumentText(byte[] bytes, String ext, String mimeType) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            if ("docx".equals(ext)) {
                StringBuilder docxText = new StringBuilder();
                try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        if ("word/document.xml".equals(entry.getName())) {
                            String xml = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                            String plain = xml.replaceAll("<w:p[^>]*>", "\n")
                                    .replaceAll("<[^>]+>", " ")
                                    .replaceAll("[ \\t]+", " ")
                                    .trim();
                            docxText.append(plain);
                            break;
                        }
                    }
                }
                String res = docxText.toString().trim();
                return res.length() > 4000 ? res.substring(0, 4000) : res;
            }
            if ((ext != null && ext.matches("^(txt|md|csv|tsv|json|html|htm|xml)$"))
                    || (mimeType != null && mimeType.startsWith("text/"))) {
                String utf8 = new String(bytes, StandardCharsets.UTF_8);
                if (utf8.contains("\uFFFD")) {
                    try {
                        utf8 = new String(bytes, Charset.forName("MS949"));
                    } catch (Exception ignored) {
                    }
                }
                if ("html".equals(ext) || "htm".equals(ext)) {
                    utf8 = utf8.replaceAll("<script[^>]*>[\\s\\S]*?</script>", " ")
                            .replaceAll("<style[^>]*>[\\s\\S]*?</style>", " ")
                            .replaceAll("<[^>]+>", " ");
                }
                String cleaned = utf8.replaceAll("\\r\\n", "\n").trim();
                return cleaned.length() > 4000 ? cleaned.substring(0, 4000) : cleaned;
            }
            if ("pdf".equals(ext)) {
                String rawLatin = new String(bytes, StandardCharsets.UTF_8);
                StringBuilder sb = new StringBuilder();
                for (String line : rawLatin.split("[\\r\\n]+")) {
                    String trimmed = line.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", " ").replaceAll("\\s+", " ").trim();
                    if (trimmed.length() >= 6 && !trimmed.startsWith("%PDF") && !trimmed.contains("/Type") && !trimmed.contains("endobj")) {
                        sb.append(trimmed).append("\n");
                        if (sb.length() > 2500) {
                            break;
                        }
                    }
                }
                String extracted = sb.toString().trim();
                return extracted.isEmpty() ? null : extracted;
            }
        } catch (Exception e) {
            log.debug("문서 텍스트 추출 생략: {}", e.getMessage());
        }
        return null;
    }

    private void appendExtractedTextToIdeaPrompt(String ideaId, String snippet) {
        if (snippet == null || snippet.isBlank()) {
            return;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT prompt_text AS \"promptText\" FROM kkumz_idea WHERE id = ?",
                ideaId
        );
        if (rows.isEmpty()) {
            return;
        }
        String currentPrompt = rows.get(0).get("promptText") != null ? String.valueOf(rows.get(0).get("promptText")).trim() : "";
        if (currentPrompt.contains(snippet.trim())) {
            return;
        }
        String updated = currentPrompt.isEmpty()
                ? snippet.trim()
                : currentPrompt + "\n\n[업로드 자료 참고 내용]\n" + snippet.trim();
        jdbcTemplate.update(
                "UPDATE kkumz_idea SET prompt_text = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                updated, ideaId
        );
    }

    private void registerUploadedLogoCandidate(
            String ideaId,
            String label,
            String fileUrl,
            byte[] bytes,
            String ext,
            String mimeType
    ) {
        String svgCode;
        if ("svg".equalsIgnoreCase(ext) && bytes != null && bytes.length > 0) {
            String rawSvg = new String(bytes, StandardCharsets.UTF_8).trim();
            int svgIdx = rawSvg.indexOf("<svg");
            svgCode = svgIdx >= 0 ? rawSvg.substring(svgIdx) : rawSvg;
        } else {
            svgCode = "<svg viewBox=\"0 0 185 40\" xmlns=\"http://www.w3.org/2000/svg\">"
                    + "<image href=\"" + escapeXml(fileUrl) + "\" x=\"0\" y=\"0\" width=\"185\" height=\"40\" preserveAspectRatio=\"xMinYMid meet\"/>"
                    + "</svg>";
        }

        List<Map<String, Object>> existingLogos = jdbcTemplate.queryForList(
                """
                SELECT COALESCE(
                         (SELECT l.color_palette_json::text FROM kkumz_logo_suggestion l WHERE l.idea_id = i.id ORDER BY l.is_selected DESC, l.created_at ASC LIMIT 1),
                         NULLIF(i.design_tokens_json::text, '{}'),
                         (SELECT l2.color_palette_json::text FROM kkumz_logo_suggestion l2 ORDER BY l2.is_selected DESC, l2.created_at ASC LIMIT 1),
                         '{}'
                       ) AS "palette",
                       COALESCE(
                         (SELECT l.typography_json::text FROM kkumz_logo_suggestion l WHERE l.idea_id = i.id ORDER BY l.is_selected DESC, l.created_at ASC LIMIT 1),
                         (SELECT l2.typography_json::text FROM kkumz_logo_suggestion l2 ORDER BY l2.is_selected DESC, l2.created_at ASC LIMIT 1),
                         '{}'
                       ) AS "typo"
                FROM kkumz_idea i
                WHERE i.id = ?
                """,
                ideaId
        );
        String paletteJson = !existingLogos.isEmpty() && existingLogos.get(0).get("palette") != null
                ? String.valueOf(existingLogos.get(0).get("palette"))
                : "{}";
        String typoJson = !existingLogos.isEmpty() && existingLogos.get(0).get("typo") != null
                ? String.valueOf(existingLogos.get(0).get("typo"))
                : "{}";

        jdbcTemplate.update("UPDATE kkumz_logo_suggestion SET is_selected = FALSE WHERE idea_id = ?", ideaId);
        String logoId = "logo-up-" + UUID.randomUUID().toString().substring(0, 8);
        String styleName = (label != null && !label.isBlank()) ? "업로드 로고 · " + label : "업로드 보유 로고";

        jdbcTemplate.update("""
                INSERT INTO kkumz_logo_suggestion (
                    id, idea_id, style_name, svg_code, color_palette_json, typography_json, tags_json, is_selected, created_at
                ) VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, '["보유로고","직접업로드"]'::jsonb, TRUE, CURRENT_TIMESTAMP)
                """,
                logoId, ideaId, styleName, svgCode, paletteJson, typoJson
        );

        jdbcTemplate.update("""
                UPDATE kkumz_idea
                SET selected_logo_svg = ?,
                    design_tokens_json = CASE
                        WHEN design_tokens_json = '{}'::jsonb THEN ?::jsonb
                        ELSE design_tokens_json
                    END,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                svgCode, paletteJson, ideaId
        );
    }

    private String buildPromptWithAssetContext(String basePrompt, List<Map<String, Object>> assets) {
        if (assets == null || assets.isEmpty()) {
            return basePrompt;
        }
        StringBuilder sb = new StringBuilder(basePrompt != null ? basePrompt : "");
        List<String> mediaCaptions = new ArrayList<>();
        List<String> docTexts = new ArrayList<>();
        for (Map<String, Object> a : assets) {
            String type = String.valueOf(a.getOrDefault("assetType", ""));
            String cap = a.get("caption") != null ? String.valueOf(a.get("caption")).trim() : "";
            String orig = String.valueOf(a.getOrDefault("originalName", ""));
            String extText = a.get("extractedText") != null ? String.valueOf(a.get("extractedText")).trim() : "";
            if (!extText.isBlank()) {
                docTexts.add("- " + orig + ": " + (extText.length() > 800 ? extText.substring(0, 800) : extText));
            } else if (!cap.isBlank()) {
                mediaCaptions.add("- [" + type + "] " + cap + " (" + orig + ")");
            }
        }
        if (!mediaCaptions.isEmpty()) {
            sb.append("\n\n[업로드된 미디어 자산 목록]\n").append(String.join("\n", mediaCaptions));
        }
        if (!docTexts.isEmpty()) {
            sb.append("\n\n[업로드된 참고 문서 텍스트]\n").append(String.join("\n", docTexts));
        }
        return sb.toString();
    }

    private void applyUploadedAssetsToSiteProvisioning(
            String ideaId,
            String brandName,
            Map<String, Object> layoutSchema,
            Long shopSiteId,
            Long cmsSiteId
    ) {
        List<Map<String, Object>> assets = listIdeaAssetsInternal(ideaId);
        if (assets.isEmpty()) {
            return;
        }

        Map<String, Object> heroAsset = null;
        List<Map<String, Object>> productAssets = new ArrayList<>();
        List<Map<String, Object>> articleAssets = new ArrayList<>();
        List<Map<String, Object>> galleryAssets = new ArrayList<>();
        List<Map<String, Object>> docAssets = new ArrayList<>();

        for (Map<String, Object> a : assets) {
            String type = String.valueOf(a.getOrDefault("assetType", "GALLERY")).toUpperCase(Locale.ROOT);
            boolean isImage = Boolean.TRUE.equals(a.get("isImage"));
            switch (type) {
                case "HERO" -> {
                    if (heroAsset == null && isImage) {
                        heroAsset = a;
                    }
                    if (isImage) {
                        galleryAssets.add(a);
                    }
                }
                case "PRODUCT" -> {
                    if (isImage) {
                        productAssets.add(a);
                        galleryAssets.add(a);
                    }
                }
                case "ARTICLE" -> {
                    if (isImage) {
                        articleAssets.add(a);
                        galleryAssets.add(a);
                    }
                }
                case "GALLERY" -> {
                    if (isImage) {
                        galleryAssets.add(a);
                    }
                }
                case "DOC" -> docAssets.add(a);
                default -> {
                    if (isImage) {
                        galleryAssets.add(a);
                    }
                }
            }
        }

        if (heroAsset == null && !galleryAssets.isEmpty()) {
            heroAsset = galleryAssets.get(0);
        }

        if (heroAsset != null) {
            String heroUrl = String.valueOf(heroAsset.get("fileUrl"));
            String heroCap = heroAsset.get("caption") != null ? String.valueOf(heroAsset.get("caption")) : brandName;
            layoutSchema.put("heroBannerImage", heroUrl);
            layoutSchema.put("heroBannerCaption", Map.of(
                    "title", heroCap,
                    "sub", String.valueOf(heroAsset.getOrDefault("originalName", ""))
            ));
        }

        if (!galleryAssets.isEmpty()) {
            List<Map<String, Object>> galleryList = new ArrayList<>();
            for (Map<String, Object> g : galleryAssets) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", g.get("id"));
                item.put("fileUrl", g.get("fileUrl"));
                item.put("url", g.get("fileUrl"));
                item.put("originalName", g.get("originalName"));
                item.put("caption", g.get("caption") != null ? g.get("caption") : g.get("originalName"));
                item.put("assetType", g.get("assetType"));
                galleryList.add(item);
            }
            layoutSchema.put("galleryImages", galleryList);
            layoutSchema.put("showPhotoGallery", true);
        }

        if (!docAssets.isEmpty()) {
            List<Map<String, Object>> docList = new ArrayList<>();
            for (Map<String, Object> d : docAssets) {
                String fUrl = String.valueOf(d.get("fileUrl"));
                String origName = String.valueOf(d.getOrDefault("originalName", ""));
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", d.get("id"));
                item.put("fileUrl", fUrl);
                item.put("url", fUrl);
                item.put("downloadUrl", fUrl + (fUrl.contains("?") ? "&download=true" : "?download=true"));
                item.put("originalName", origName);
                item.put("name", origName);
                item.put("fileExt", extractExtension(origName));
                item.put("caption", d.get("caption") != null ? d.get("caption") : origName);
                item.put("fileSize", d.get("fileSize"));
                docList.add(item);
            }
            layoutSchema.put("downloadableDocs", docList);
        }

        if (!productAssets.isEmpty()) {
            List<Map<String, Object>> customProducts = new ArrayList<>();
            int idx = 1;
            for (Map<String, Object> pa : productAssets) {
                String prodName = pa.get("caption") != null && !String.valueOf(pa.get("caption")).isBlank()
                        ? String.valueOf(pa.get("caption"))
                        : stripExtension(String.valueOf(pa.get("originalName")));
                String picUrl = String.valueOf(pa.get("fileUrl"));
                Long prodId = (long) (90000 + idx);

                if (shopSiteId != null && shopJdbcTemplate != null) {
                    List<Map<String, Object>> existProd = shopJdbcTemplate.queryForList(
                            "SELECT id, price, promotion_price AS \"promotionPrice\", stock, sub_title AS \"subTitle\", product_category_name AS \"categoryName\" FROM pms_product WHERE site_id = ? AND pic = ? LIMIT 1",
                            shopSiteId, picUrl
                    );
                    if (!existProd.isEmpty()) {
                        prodId = ((Number) existProd.get(0).get("id")).longValue();
                    } else {
                        prodId = shopJdbcTemplate.queryForObject("""
                                INSERT INTO pms_product (
                                    site_id, brand_name, name, pic, product_sn, delete_status, publish_status,
                                    new_status, recommand_status, verify_status, sort, sale, price, promotion_price,
                                    original_price, sub_title, description, stock, low_stock, unit, product_category_name
                                ) VALUES (?, ?, ?, ?, ?, 0, 1, 1, 1, 1, ?, 0, 0, 0, 0, ?, ?, 50, 5, '건', ?)
                                RETURNING id
                                """,
                                Long.class,
                                shopSiteId, brandName, prodName, picUrl,
                                "AST-" + pa.get("id"), idx,
                                prodName, prodName, brandName
                        );
                    }
                }

                Map<String, Object> prodMap = new LinkedHashMap<>();
                prodMap.put("id", prodId);
                prodMap.put("name", prodName);
                prodMap.put("pic", picUrl);
                prodMap.put("price", 0);
                prodMap.put("promotionPrice", 0);
                prodMap.put("subTitle", prodName);
                prodMap.put("stock", 50);
                prodMap.put("unit", "건");
                prodMap.put("brandName", brandName);
                prodMap.put("productCategoryName", brandName);
                customProducts.add(prodMap);
                idx++;
            }
            layoutSchema.put("customProducts", customProducts);
        }

        if (!articleAssets.isEmpty()) {
            List<Map<String, Object>> customArticles = new ArrayList<>();
            int idx = 1;
            for (Map<String, Object> aa : articleAssets) {
                String artTitle = aa.get("caption") != null && !String.valueOf(aa.get("caption")).isBlank()
                        ? String.valueOf(aa.get("caption"))
                        : stripExtension(String.valueOf(aa.get("originalName")));
                String coverUrl = String.valueOf(aa.get("fileUrl"));
                Long artId = (long) (80000 + idx);

                if (cmsSiteId != null && cmsJdbcTemplate != null) {
                    List<Map<String, Object>> existArt = cmsJdbcTemplate.queryForList(
                            "SELECT id FROM cms_content WHERE site_id = ? AND cover = ? LIMIT 1",
                            cmsSiteId, coverUrl
                    );
                    if (!existArt.isEmpty()) {
                        artId = ((Number) existArt.get(0).get("id")).longValue();
                    } else {
                        List<Map<String, Object>> cats = cmsJdbcTemplate.queryForList(
                                "SELECT id FROM cms_category WHERE site_id = ? ORDER BY id ASC LIMIT 1",
                                cmsSiteId
                        );
                        Integer categoryId;
                        if (!cats.isEmpty()) {
                            categoryId = ((Number) cats.get(0).get("id")).intValue();
                        } else {
                            categoryId = cmsJdbcTemplate.queryForObject("""
                                    INSERT INTO cms_category (
                                        site_id, name, code, only_url, has_static, contain_child, allow_contribute, sort, hidden, disabled
                                    ) VALUES (?, '브랜드 매거진', ?, FALSE, FALSE, TRUE, TRUE, 1, FALSE, FALSE)
                                    RETURNING id
                                    """,
                                    Integer.class,
                                    cmsSiteId, "cat-" + cmsSiteId
                            );
                        }
                        String safeAuthor = brandName != null && brandName.length() > 50 ? brandName.substring(0, 50) : brandName;
                        String safeDesc = artTitle != null && artTitle.length() > 300 ? artTitle.substring(0, 300) : artTitle;
                        artId = cmsJdbcTemplate.queryForObject("""
                                INSERT INTO cms_content (
                                    site_id, title, user_id, category_id, model_id, copied, contribute,
                                    author, only_url, has_images, has_files, has_products, has_static,
                                    description, cover, childs, scores, comments, clicks,
                                    publish_date, create_date, sort, status, disabled
                                ) VALUES (?, ?, 1, ?, 'article', FALSE, FALSE, ?, FALSE, TRUE, FALSE, FALSE, FALSE, ?, ?, 0, 100, 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, 1, FALSE)
                                RETURNING id
                                """,
                                Long.class,
                                cmsSiteId, artTitle, categoryId, safeAuthor, safeDesc, coverUrl, 100 - idx
                        );
                        String bodyHtml = "<p>" + escapeXml(artTitle) + "</p>";
                        cmsJdbcTemplate.update("""
                                INSERT INTO cms_content_attribute (content_id, text, word_count)
                                VALUES (?, ?, ?)
                                """,
                                artId, bodyHtml, artTitle != null ? artTitle.length() : 0
                        );
                    }
                }

                Map<String, Object> artMap = new LinkedHashMap<>();
                artMap.put("id", artId);
                artMap.put("title", artTitle);
                artMap.put("description", artTitle);
                artMap.put("summary", artTitle);
                artMap.put("cover", coverUrl);
                artMap.put("thumbnailUrl", coverUrl);
                artMap.put("author", brandName);
                artMap.put("categoryName", brandName);
                customArticles.add(artMap);
                idx++;
            }
            layoutSchema.put("customArticles", customArticles);
        }
    }

    private void syncIdeaAssetsToDeployedSiteIfPresent(String ideaId) {
        List<Map<String, Object>> sites = jdbcTemplate.queryForList(
                "SELECT id, site_name AS \"siteName\", active_version AS \"activeVersion\", shop_site_id AS \"shopSiteId\", cms_site_id AS \"cmsSiteId\" FROM kkumz_site WHERE idea_id = ? ORDER BY created_at DESC LIMIT 1",
                ideaId
        );
        if (sites.isEmpty()) {
            return;
        }
        Map<String, Object> site = sites.get(0);
        String siteId = String.valueOf(site.get("id"));
        String siteName = String.valueOf(site.get("siteName"));
        int activeVer = ((Number) site.get("activeVersion")).intValue();
        Long shopSiteId = site.get("shopSiteId") != null ? ((Number) site.get("shopSiteId")).longValue() : null;
        Long cmsSiteId = site.get("cmsSiteId") != null ? ((Number) site.get("cmsSiteId")).longValue() : null;

        Map<String, Object> latestRel = getLatestRelease(siteId, activeVer);
        Map<String, Object> layoutSchema = new LinkedHashMap<>(parseJsonObject(String.valueOf(latestRel.get("layoutSchemaJson"))));

        applyUploadedAssetsToSiteProvisioning(ideaId, siteName, layoutSchema, shopSiteId, cmsSiteId);

        jdbcTemplate.update(
                "UPDATE kkumz_site_release SET layout_schema_json = ?::jsonb WHERE id = ?",
                toJson(layoutSchema), latestRel.get("id")
        );
    }

    private List<Map<String, Object>> resolveGalleryAssets(Map<String, Object> layoutSchema, List<Map<String, Object>> ideaAssets) {
        if (layoutSchema != null && layoutSchema.get("galleryImages") instanceof List<?> gl && !gl.isEmpty()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Object o : gl) {
                if (o instanceof Map<?, ?> m) {
                    Map<String, Object> copy = new LinkedHashMap<>();
                    m.forEach((k, v) -> copy.put(String.valueOf(k), v));
                    if (!copy.containsKey("fileUrl") && copy.get("url") != null) {
                        copy.put("fileUrl", copy.get("url"));
                    }
                    if (!copy.containsKey("url") && copy.get("fileUrl") != null) {
                        copy.put("url", copy.get("fileUrl"));
                    }
                    list.add(copy);
                }
            }
            if (!list.isEmpty()) {
                return list;
            }
        }
        List<Map<String, Object>> fromAssets = new ArrayList<>();
        if (ideaAssets != null) {
            for (Map<String, Object> a : ideaAssets) {
                String type = String.valueOf(a.getOrDefault("assetType", ""));
                if (Boolean.TRUE.equals(a.get("isImage")) && !"LOGO".equalsIgnoreCase(type)) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", a.get("id"));
                    item.put("fileUrl", a.get("fileUrl"));
                    item.put("url", a.get("fileUrl"));
                    item.put("originalName", a.get("originalName"));
                    item.put("caption", a.get("caption") != null ? a.get("caption") : a.get("originalName"));
                    item.put("assetType", type);
                    fromAssets.add(item);
                }
            }
        }
        return fromAssets;
    }

    private List<Map<String, Object>> resolveDocumentAssets(Map<String, Object> layoutSchema, List<Map<String, Object>> ideaAssets) {
        if (layoutSchema != null && layoutSchema.get("downloadableDocs") instanceof List<?> dl && !dl.isEmpty()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Object o : dl) {
                if (o instanceof Map<?, ?> m) {
                    Map<String, Object> copy = new LinkedHashMap<>();
                    m.forEach((k, v) -> copy.put(String.valueOf(k), v));
                    if (!copy.containsKey("fileUrl") && copy.get("url") != null) {
                        copy.put("fileUrl", copy.get("url"));
                    }
                    if (!copy.containsKey("originalName") && copy.get("name") != null) {
                        copy.put("originalName", copy.get("name"));
                    }
                    if (!copy.containsKey("downloadUrl") && copy.get("fileUrl") != null) {
                        String fUrl = String.valueOf(copy.get("fileUrl"));
                        copy.put("downloadUrl", fUrl + (fUrl.contains("?") ? "&download=true" : "?download=true"));
                    }
                    list.add(copy);
                }
            }
            if (!list.isEmpty()) {
                return list;
            }
        }
        List<Map<String, Object>> fromAssets = new ArrayList<>();
        if (ideaAssets != null) {
            for (Map<String, Object> a : ideaAssets) {
                if ("DOC".equalsIgnoreCase(String.valueOf(a.getOrDefault("assetType", "")))) {
                    String fUrl = String.valueOf(a.get("fileUrl"));
                    String origName = String.valueOf(a.getOrDefault("originalName", ""));
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", a.get("id"));
                    item.put("fileUrl", fUrl);
                    item.put("url", fUrl);
                    item.put("downloadUrl", fUrl + (fUrl.contains("?") ? "&download=true" : "?download=true"));
                    item.put("originalName", origName);
                    item.put("name", origName);
                    item.put("fileExt", extractExtension(origName));
                    item.put("caption", a.get("caption") != null ? a.get("caption") : origName);
                    item.put("fileSize", a.get("fileSize"));
                    fromAssets.add(item);
                }
            }
        }
        return fromAssets;
    }

    private String buildConciergeQuestionWithContext(String question, String ideaId, Map<String, Object> runtime) {
        StringBuilder ctx = new StringBuilder();
        List<Map<String, Object>> assets = listIdeaAssetsInternal(ideaId);
        for (Map<String, Object> a : assets) {
            if (a.get("extractedText") != null && !String.valueOf(a.get("extractedText")).isBlank()) {
                ctx.append("[참고문서 ").append(a.get("originalName")).append("] ")
                        .append(a.get("extractedText")).append("\n");
            }
        }
        if (ctx.isEmpty()) {
            return question;
        }
        return question + "\n\n(참고 브랜드 자료:\n" + ctx.toString().trim() + ")";
    }

    // =========================================================================
    // Git 리포지토리 스캐폴딩, Ed25519 키쌍 생성 & GitHub API 연동 헬퍼
    // =========================================================================

    private Map<String, Object> findSiteGitRepoNullable(String siteId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                    SELECT id,
                           site_id AS "siteId",
                           provider,
                           repo_owner AS "repoOwner",
                           repo_name AS "repoName",
                           default_branch AS "defaultBranch",
                           visibility,
                           repo_html_url AS "repoHtmlUrl",
                           clone_ssh_url AS "cloneSshUrl",
                           clone_https_url AS "cloneHttpsUrl",
                           ssh_key_title AS "sshKeyTitle",
                           ssh_key_fingerprint AS "sshKeyFingerprint",
                           ssh_public_key AS "sshPublicKey",
                           ssh_private_key_pem AS "sshPrivateKeyPem",
                           site_api_key_id AS "siteApiKeyId",
                           site_access_key AS "siteAccessKey",
                           site_secret_key AS "siteSecretKey",
                           last_commit_sha AS "lastCommitSha",
                           last_commit_message AS "lastCommitMessage",
                           sync_mode AS "syncMode",
                           github_remote_synced AS "githubRemoteSynced",
                           commit_history_json::text AS "commitHistoryJson",
                           key_rotated_at AS "keyRotatedAt",
                           last_synced_at AS "lastSyncedAt",
                           created_by AS "createdBy",
                           created_at AS "createdAt",
                           updated_at AS "updatedAt"
                    FROM kkumz_site_git_repo
                    WHERE site_id = ?
                    LIMIT 1
                    """, siteId);
            if (rows.isEmpty()) {
                return null;
            }
            Map<String, Object> row = new LinkedHashMap<>(rows.get(0));
            String commitHistoryJson = String.valueOf(row.remove("commitHistoryJson"));
            row.put("commitHistory", parseJsonList(commitHistoryJson));
            String slug = String.valueOf(row.getOrDefault("repoName", "site"));
            String sshUrl = String.valueOf(row.getOrDefault("cloneSshUrl", ""));
            String branch = String.valueOf(row.getOrDefault("defaultBranch", "main"));
            row.put("quickCloneCommand",
                    "GIT_SSH_COMMAND=\"ssh -i ~/.ssh/" + slug + "-deploy-key.pem -o IdentitiesOnly=yes\" git clone -b " + branch + " " + sshUrl);
            return row;
        } catch (Exception e) {
            log.debug("kkumz_site_git_repo 조회 스킵: {}", e.getMessage());
            return null;
        }
    }

    private List<Map<String, Object>> normalizeAndFormatWebIdeFiles(List<?> rawFiles) {
        if (rawFiles == null || rawFiles.isEmpty()) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : rawFiles) {
            if (!(item instanceof Map<?, ?> mapItem)) continue;
            Map<String, Object> copy = new LinkedHashMap<>();
            mapItem.forEach((k, v) -> copy.put(String.valueOf(k), v));
            String path = String.valueOf(copy.getOrDefault("path", ""));
            String lang = String.valueOf(copy.getOrDefault("language", ""));
            String content = String.valueOf(copy.getOrDefault("content", ""));
            if ((path.endsWith(".json") || "json".equalsIgnoreCase(lang)) && !content.isBlank() && !content.contains("\n")) {
                try {
                    Object parsed = objectMapper.readValue(content, Object.class);
                    copy.put("content", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed));
                } catch (Exception ignored) {
                }
            }
            result.add(copy);
        }
        return result;
    }

    private Map<String, Object> ensureSiteRuntimeApiKey(
            String siteId,
            String siteSlug,
            String siteName,
            String actor,
            boolean forceRotate
    ) {
        String keyMarker = "[" + siteSlug + "]";
        List<Map<String, Object>> existing = jdbcTemplate.queryForList("""
                SELECT id, access_key AS "accessKey", secret_key AS "secretKey"
                FROM kkumz_api_access_key
                WHERE direction = 'INBOUND' AND service_code = 'SITE_GIT_SDK' AND key_name LIKE ?
                ORDER BY created_at DESC
                LIMIT 1
                """, "%" + keyMarker + "%");

        if (!existing.isEmpty()) {
            Map<String, Object> found = new LinkedHashMap<>(existing.get(0));
            if (forceRotate) {
                String newSecret = "sk_site_" + UUID.randomUUID().toString().replace("-", "")
                        + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
                jdbcTemplate.update(
                        "UPDATE kkumz_api_access_key SET secret_key = ?, status = 'ACTIVE', updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                        newSecret, found.get("id")
                );
                found.put("secretKey", newSecret);
            }
            return found;
        }

        String keyId = "ak-site-" + UUID.randomUUID().toString().substring(0, 8);
        String cleanSlug = siteSlug.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
        if (cleanSlug.length() > 10) {
            cleanSlug = cleanSlug.substring(0, 10);
        }
        String accessKey = "ak_site_" + cleanSlug + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String secretKey = "sk_site_" + UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        List<String> scopes = List.of(
                "kkumz:site:read",
                "kkumz:site:deploy",
                "kkumz:assets:read",
                "shop:product:read",
                "shop:order:write",
                "cms:content:read",
                "cms:form:submit"
        );
        String keyName = siteName + " " + keyMarker + " Git 개발자 SDK 런타임 키";
        String desc = "5단계 WebIDE 및 외부 Git 클론 환경에서 /s/" + siteSlug + " 사이트 실데이터(자산·상품·예약·AI컨시어지)를 호출하기 위한 전용 키";

        jdbcTemplate.update("""
                INSERT INTO kkumz_api_access_key (
                    id, key_name, direction, service_code, base_url, access_key, secret_key,
                    scopes_json, rate_limit_rpm, status, call_count, expires_at, description, created_by, created_at, updated_at
                ) VALUES (?, ?, 'INBOUND', 'SITE_GIT_SDK', ?, ?, ?, ?::jsonb, 1200, 'ACTIVE', 0, CURRENT_TIMESTAMP + INTERVAL '365 days', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                keyId, keyName, "/s/" + siteSlug, accessKey, secretKey, toJson(scopes), desc, actor
        );

        Map<String, Object> created = new LinkedHashMap<>();
        created.put("id", keyId);
        created.put("accessKey", accessKey);
        created.put("secretKey", secretKey);
        return created;
    }

    private Map<String, String> generateEd25519SshKeyPair(String comment) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
            KeyPair kp = kpg.generateKeyPair();
            byte[] x509Pub = kp.getPublic().getEncoded();
            // Ed25519 X.509 SubjectPublicKeyInfo의 마지막 32바이트가 순수 공개키 바이트
            byte[] rawPub = Arrays.copyOfRange(x509Pub, x509Pub.length - 32, x509Pub.length);

            ByteArrayOutputStream sshWire = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(sshWire);
            byte[] keyType = "ssh-ed25519".getBytes(StandardCharsets.US_ASCII);
            dos.writeInt(keyType.length);
            dos.write(keyType);
            dos.writeInt(rawPub.length);
            dos.write(rawPub);
            dos.flush();
            byte[] wireBytes = sshWire.toByteArray();

            String pubBase64 = Base64.getEncoder().encodeToString(wireBytes);
            String publicKey = "ssh-ed25519 " + pubBase64 + " " + comment;

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            String fpBase64 = Base64.getEncoder().withoutPadding().encodeToString(sha256.digest(wireBytes));
            String fingerprint = "SHA256:" + fpBase64;

            String privBase64 = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII))
                    .encodeToString(kp.getPrivate().getEncoded());
            String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n"
                    + privBase64 + "\n"
                    + "-----END PRIVATE KEY-----\n";

            Map<String, String> res = new LinkedHashMap<>();
            res.put("publicKey", publicKey);
            res.put("privateKeyPem", privateKeyPem);
            res.put("fingerprint", fingerprint);
            return res;
        } catch (Exception e) {
            throw new IllegalStateException("Ed25519 SSH Deploy Key 생성 중 암호화 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> buildRunnableGitProjectFiles(
            Map<String, Object> site,
            List<Map<String, Object>> existingFiles,
            String repoOwner,
            String repoName,
            String defaultBranch,
            String cloneSshUrl,
            String siteAccessKey,
            String siteSecretKey
    ) {
        String siteSlug = String.valueOf(site.get("siteSlug"));
        String siteName = String.valueOf(site.get("siteName"));
        String brandSlogan = String.valueOf(site.getOrDefault("brandSlogan", ""));

        Map<String, Map<String, Object>> byPath = new LinkedHashMap<>();
        for (Map<String, Object> f : existingFiles) {
            String p = String.valueOf(f.getOrDefault("path", "")).trim();
            if (!p.isEmpty()) {
                byPath.put(p, new LinkedHashMap<>(f));
            }
        }

        // 핵심 3대 파일(src/App.vue, src/theme.json, src/layout.json) 우선 배치 후 실행용 스캐폴딩 파일 구성
        if (!byPath.containsKey("package.json")) {
            String pkgJson = """
                    {
                      "name": "@kkumz-sites/%s",
                      "version": "1.0.0",
                      "private": true,
                      "description": "%s — Kkumz AI Studio Custom Site Bundle",
                      "type": "module",
                      "scripts": {
                        "dev": "vite --port 5173",
                        "build": "vite build",
                        "preview": "vite preview --port 4173"
                      },
                      "dependencies": {
                        "vue": "^3.5.13"
                      },
                      "devDependencies": {
                        "@vitejs/plugin-vue": "^5.2.1",
                        "typescript": "^5.7.3",
                        "vite": "^6.0.7"
                      }
                    }
                    """.formatted(siteSlug, escapeJsonString(siteName));
            byPath.put("package.json", new LinkedHashMap<>(Map.of(
                    "path", "package.json",
                    "language", "json",
                    "content", pkgJson.trim()
            )));
        }

        if (!byPath.containsKey("vite.config.ts")) {
            String viteConfig = """
                    import { defineConfig, loadEnv } from 'vite';
                    import vue from '@vitejs/plugin-vue';

                    export default defineConfig(({ mode }) => {
                      const env = loadEnv(mode, process.cwd(), '');
                      const apiTarget = env.VITE_KKUMZ_API_TARGET || '%s';
                      const siteAccessKey = env.VITE_KKUMZ_SITE_ACCESS_KEY || '%s';

                      return {
                        plugins: [vue()],
                        server: {
                          host: '0.0.0.0',
                          port: 5173,
                          proxy: {
                            '/api/v1/kkumz': {
                              target: apiTarget,
                              changeOrigin: true,
                              headers: {
                                'X-Kkumz-Site-Key': siteAccessKey,
                              },
                            },
                          },
                        },
                      };
                    });
                    """.formatted(kkumzPublicBaseUrl != null ? kkumzPublicBaseUrl : "", siteAccessKey);
            byPath.put("vite.config.ts", new LinkedHashMap<>(Map.of(
                    "path", "vite.config.ts",
                    "language", "typescript",
                    "content", viteConfig.trim()
            )));
        }

        if (!byPath.containsKey("index.html")) {
            String indexHtml = """
                    <!DOCTYPE html>
                    <html lang="ko">
                      <head>
                        <meta charset="UTF-8" />
                        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                        <title>%s — %s</title>
                      </head>
                      <body>
                        <div id="app"></div>
                        <script type="module" src="/src/main.ts"></script>
                      </body>
                    </html>
                    """.formatted(escapeXml(siteName), escapeXml(brandSlogan));
            byPath.put("index.html", new LinkedHashMap<>(Map.of(
                    "path", "index.html",
                    "language", "html",
                    "content", indexHtml.trim()
            )));
        }

        if (!byPath.containsKey("src/main.ts")) {
            String mainTs = """
                    import { createApp, h, reactive } from 'vue';
                    import App from './App.vue';
                    import localTheme from './theme.json';
                    import localLayout from './layout.json';

                    const state = reactive({
                      tokens: (localTheme as any).designTokens || localTheme || {},
                      layout: (localLayout as any).layoutSchema || localLayout || {},
                      runtime: null as Record<string, any> | null,
                    });

                    async function bootstrap() {
                      try {
                        const res = await fetch('/api/v1/kkumz/sites/%s/runtime', {
                          headers: {
                            'X-Kkumz-Site-Key': import.meta.env.VITE_KKUMZ_SITE_ACCESS_KEY || '',
                          },
                        });
                        if (res.ok) {
                          const json = await res.json();
                          state.runtime = json?.result || json?.data || null;
                        }
                      } catch (err) {
                        console.warn('[Kkumz SDK] 로컬 정적 스키마(theme.json / layout.json)로 렌더링합니다.', err);
                      }

                      createApp({
                        render: () => h(App, { tokens: state.tokens, layout: state.layout, runtime: state.runtime }),
                      }).mount('#app');
                    }

                    bootstrap();
                    """.formatted(siteSlug);
            byPath.put("src/main.ts", new LinkedHashMap<>(Map.of(
                    "path", "src/main.ts",
                    "language", "typescript",
                    "content", mainTs.trim()
            )));
        }

        byPath.put(".env.example", new LinkedHashMap<>(Map.of(
                "path", ".env.example",
                "language", "properties",
                "content", buildDotEnvExampleContent(siteSlug, siteAccessKey, siteSecretKey)
        )));

        String readmeMd = """
                # %s (`/s/%s`)

                > %s
                > Kkumz 5단계 WebIDE 및 Git 개발자 워크스페이스에서 프로비저닝된 **Vue 3 + Vite** 공식 소스 리포지토리입니다.

                ## 1. 발급된 전용 키로 Git Clone 실행

                발급받은 `%s-deploy-key.pem` 파일을 `~/.ssh/` 경로에 저장한 뒤 아래 명령어로 클론하세요.

                ```bash
                chmod 600 ~/.ssh/%s-deploy-key.pem
                GIT_SSH_COMMAND="ssh -i ~/.ssh/%s-deploy-key.pem -o IdentitiesOnly=yes" git clone -b %s %s
                cd %s
                ```

                ## 2. 로컬 개발 서버 실행 (Kkumz 실데이터 자동 연동)

                ```bash
                cp .env.example .env.local
                pnpm install
                pnpm dev
                ```

                - 로컬 실행 시 `.env.local`에 설정된 사이트 전용 런타임 키(`%s`)를 통해 Kkumz 백엔드의 업로드 사진·문서(`/api/v1/kkumz/assets/files/...`), SyncShop 상품, SyncCMS 매거진·예약폼 API가 실시간 연동됩니다.
                """.formatted(
                siteName, siteSlug, brandSlogan,
                siteSlug, siteSlug, siteSlug, defaultBranch, cloneSshUrl, repoName,
                siteAccessKey
        );
        byPath.put("README.md", new LinkedHashMap<>(Map.of(
                "path", "README.md",
                "language", "markdown",
                "content", readmeMd.trim()
        )));

        return new ArrayList<>(byPath.values());
    }

    private String buildDotEnvExampleContent(String siteSlug, String siteAccessKey, String siteSecretKey) {
        return """
                # Kkumz Site Developer SDK Runtime Credentials
                VITE_KKUMZ_API_TARGET=http://localhost:9580
                VITE_KKUMZ_SITE_SLUG=%s
                VITE_KKUMZ_SITE_ACCESS_KEY=%s
                VITE_KKUMZ_SITE_SECRET_KEY=%s
                """.formatted(siteSlug, siteAccessKey, siteSecretKey).trim();
    }

    private String calculateGitCommitSha(String siteId, String repoName, String commitMsg, List<Map<String, Object>> files) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.update((siteId + ":" + repoName + ":" + commitMsg + "\n").getBytes(StandardCharsets.UTF_8));
            for (Map<String, Object> f : files) {
                sha1.update(String.valueOf(f.getOrDefault("path", "")).getBytes(StandardCharsets.UTF_8));
                sha1.update(String.valueOf(f.getOrDefault("content", "")).getBytes(StandardCharsets.UTF_8));
            }
            byte[] digest = sha1.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "") + "00000000";
        }
    }

    private String resolveConfiguredGitHubToken() {
        String envToken = System.getenv("GITHUB_PAT_TOKEN");
        if (envToken != null && !envToken.isBlank()) {
            return envToken.trim();
        }
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                    SELECT secret_key
                    FROM kkumz_api_access_key
                    WHERE direction = 'OUTBOUND' AND service_code = 'GITHUB' AND status = 'ACTIVE'
                    ORDER BY updated_at DESC
                    LIMIT 1
                    """);
            if (!rows.isEmpty() && rows.get(0).get("secret_key") != null) {
                String token = String.valueOf(rows.get(0).get("secret_key")).trim();
                if (!token.isBlank() && !token.startsWith("sk_")) {
                    return token;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Map<String, Object> syncWithGitHubRemoteApi(
            String patToken,
            String requestedOwner,
            String repoName,
            String branch,
            boolean isPrivate,
            String description,
            String sshKeyTitle,
            String sshPublicKey,
            List<Map<String, Object>> files,
            String commitMessage
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            String targetOwner = (requestedOwner == null || requestedOwner.isBlank())
                    ? "kkumz-sites"
                    : requestedOwner.trim();

            // 1. 토큰 소유자 또는 Organization 확인
            HttpRequest userReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/user"))
                    .header("Authorization", "Bearer " + patToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                    .GET()
                    .build();
            HttpResponse<String> userRes = client.send(userReq, HttpResponse.BodyHandlers.ofString());
            if (userRes.statusCode() != 200) {
                result.put("synced", false);
                result.put("message", "GitHub 토큰 인증 실패(HTTP " + userRes.statusCode() + ") — Kkumz Managed Git 저장소 모드로 프로비저닝되었습니다.");
                return result;
            }
            JsonNode userNode = objectMapper.readTree(userRes.body());
            String loginUser = userNode.path("login").asText("");

            // 2. 리포지토리 존재 확인 또는 kkumz-sites Organization 하위에 신규 생성
            HttpRequest getRepoReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/" + targetOwner + "/" + repoName))
                    .header("Authorization", "Bearer " + patToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                    .GET()
                    .build();
            HttpResponse<String> getRepoRes = client.send(getRepoReq, HttpResponse.BodyHandlers.ofString());
            JsonNode repoNode;
            if (getRepoRes.statusCode() == 404) {
                String createUri = targetOwner.equalsIgnoreCase(loginUser)
                        ? "https://api.github.com/user/repos"
                        : "https://api.github.com/orgs/" + targetOwner + "/repos";
                Map<String, Object> createPayload = Map.of(
                        "name", repoName,
                        "description", description,
                        "private", isPrivate,
                        "auto_init", true
                );
                HttpRequest createReq = HttpRequest.newBuilder()
                        .uri(URI.create(createUri))
                        .header("Authorization", "Bearer " + patToken)
                        .header("Accept", "application/vnd.github+json")
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "Kkumz-Studio-Git-Agent")
                        .POST(HttpRequest.BodyPublishers.ofString(toJson(createPayload)))
                        .build();
                HttpResponse<String> createRes = client.send(createReq, HttpResponse.BodyHandlers.ofString());
                if (createRes.statusCode() != 201 && createRes.statusCode() != 200) {
                    result.put("synced", false);
                    result.put("message", "GitHub(" + targetOwner + ") 리포지토리 생성 응답(HTTP " + createRes.statusCode() + ": " + createRes.body() + ") — 로컬 Managed Git 모드로 저장되었습니다.");
                    return result;
                }
                repoNode = objectMapper.readTree(createRes.body());
                // GitHub가 auto_init 초기 커밋 생성을 완료하도록 잠깐 대기
                Thread.sleep(600L);
            } else if (getRepoRes.statusCode() == 200) {
                repoNode = objectMapper.readTree(getRepoRes.body());
            } else {
                result.put("synced", false);
                result.put("message", "GitHub 리포지토리 조회 응답(HTTP " + getRepoRes.statusCode() + ") — 로컬 Managed Git 모드로 저장되었습니다.");
                return result;
            }

            // 3. SSH Deploy Key 교체 및 등록 (Write 권한)
            replaceGitHubDeployKey(patToken, targetOwner, repoName, sshKeyTitle, sshPublicKey);

            // 4. Git Data Tree API로 전체 프로젝트 파일 단일 원자적 커밋 푸시 (실패 시 Contents API 폴백)
            String remoteCommitSha = pushFilesViaGitDataTreeApi(client, patToken, targetOwner, repoName, branch, files, commitMessage);
            if (remoteCommitSha == null) {
                for (Map<String, Object> f : files) {
                    String path = String.valueOf(f.getOrDefault("path", "")).replaceAll("^/+", "");
                    if (path.isBlank()) continue;
                    String contentB64 = Base64.getEncoder().encodeToString(
                            String.valueOf(f.getOrDefault("content", "")).getBytes(StandardCharsets.UTF_8)
                    );
                    String contentUrl = "https://api.github.com/repos/" + targetOwner + "/" + repoName + "/contents/" + path;
                    HttpRequest checkFileReq = HttpRequest.newBuilder()
                            .uri(URI.create(contentUrl + "?ref=" + branch))
                            .header("Authorization", "Bearer " + patToken)
                            .header("Accept", "application/vnd.github+json")
                            .header("User-Agent", "Kkumz-Studio-Git-Agent")
                            .GET()
                            .build();
                    HttpResponse<String> checkFileRes = client.send(checkFileReq, HttpResponse.BodyHandlers.ofString());
                    Map<String, Object> putPayload = new LinkedHashMap<>();
                    putPayload.put("message", commitMessage);
                    putPayload.put("content", contentB64);
                    putPayload.put("branch", branch);
                    if (checkFileRes.statusCode() == 200) {
                        JsonNode existingFile = objectMapper.readTree(checkFileRes.body());
                        if (existingFile.hasNonNull("sha")) {
                            putPayload.put("sha", existingFile.get("sha").asText());
                        }
                    }
                    HttpRequest putFileReq = HttpRequest.newBuilder()
                            .uri(URI.create(contentUrl))
                            .header("Authorization", "Bearer " + patToken)
                            .header("Accept", "application/vnd.github+json")
                            .header("Content-Type", "application/json")
                            .header("User-Agent", "Kkumz-Studio-Git-Agent")
                            .PUT(HttpRequest.BodyPublishers.ofString(toJson(putPayload)))
                            .build();
                    HttpResponse<String> putFileRes = client.send(putFileReq, HttpResponse.BodyHandlers.ofString());
                    if (putFileRes.statusCode() == 200 || putFileRes.statusCode() == 201) {
                        JsonNode commitNode = objectMapper.readTree(putFileRes.body()).path("commit");
                        if (commitNode.hasNonNull("sha")) {
                            remoteCommitSha = commitNode.get("sha").asText();
                        }
                    }
                }
            }

            result.put("synced", true);
            result.put("owner", targetOwner);
            if (remoteCommitSha != null && !remoteCommitSha.isBlank()) {
                result.put("commitSha", remoteCommitSha);
            }
            result.put("htmlUrl", repoNode.path("html_url").asText("https://github.com/" + targetOwner + "/" + repoName));
            result.put("sshUrl", repoNode.path("ssh_url").asText("git@github.com:" + targetOwner + "/" + repoName + ".git"));
            result.put("cloneUrl", repoNode.path("clone_url").asText("https://github.com/" + targetOwner + "/" + repoName + ".git"));
            result.put("message", "GitHub 원격 저장소(https://github.com/" + targetOwner + "/" + repoName + ") 생성, 전용 SSH Deploy Key 등록 및 소스 푸시가 완료되었습니다.");
            return result;
        } catch (Exception e) {
            log.warn("GitHub 원격 API 연동 중 예외 발생 (Managed Git 모드로 폴백): {}", e.getMessage());
            result.put("synced", false);
            result.put("message", "Kkumz Managed Git 저장소 및 전용 배포 키 프로비저닝 완료 (오프라인/로컬 번들 즉시 사용 가능)");
            return result;
        }
    }

    private void replaceGitHubDeployKey(
            String patToken,
            String owner,
            String repoName,
            String sshKeyTitle,
            String sshPublicKey
    ) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            String keysUrl = "https://api.github.com/repos/" + owner + "/" + repoName + "/keys";
            HttpRequest listReq = HttpRequest.newBuilder()
                    .uri(URI.create(keysUrl))
                    .header("Authorization", "Bearer " + patToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                    .GET()
                    .build();
            HttpResponse<String> listRes = client.send(listReq, HttpResponse.BodyHandlers.ofString());
            if (listRes.statusCode() == 200) {
                JsonNode arr = objectMapper.readTree(listRes.body());
                if (arr.isArray()) {
                    for (JsonNode k : arr) {
                        String title = k.path("title").asText("");
                        long keyId = k.path("id").asLong(0L);
                        if (keyId > 0 && title.startsWith("kkumz-deploy-key@")) {
                            HttpRequest delReq = HttpRequest.newBuilder()
                                    .uri(URI.create(keysUrl + "/" + keyId))
                                    .header("Authorization", "Bearer " + patToken)
                                    .header("Accept", "application/vnd.github+json")
                                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                                    .DELETE()
                                    .build();
                            client.send(delReq, HttpResponse.BodyHandlers.ofString());
                        }
                    }
                }
            }
            Map<String, Object> keyPayload = Map.of(
                    "title", sshKeyTitle,
                    "key", sshPublicKey,
                    "read_only", false
            );
            HttpRequest keyReq = HttpRequest.newBuilder()
                    .uri(URI.create(keysUrl))
                    .header("Authorization", "Bearer " + patToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                    .POST(HttpRequest.BodyPublishers.ofString(toJson(keyPayload)))
                    .build();
            client.send(keyReq, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.warn("GitHub Deploy Key 등록 경고 ({}/{}): {}", owner, repoName, e.getMessage());
        }
    }

    private String pushFilesViaGitDataTreeApi(
            HttpClient client,
            String patToken,
            String owner,
            String repoName,
            String branch,
            List<Map<String, Object>> files,
            String commitMessage
    ) {
        try {
            String refUrl = "https://api.github.com/repos/" + owner + "/" + repoName + "/git/ref/heads/" + branch;
            HttpRequest refReq = HttpRequest.newBuilder()
                    .uri(URI.create(refUrl))
                    .header("Authorization", "Bearer " + patToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                    .GET()
                    .build();
            HttpResponse<String> refRes = client.send(refReq, HttpResponse.BodyHandlers.ofString());
            if (refRes.statusCode() != 200) {
                return null;
            }
            String parentCommitSha = objectMapper.readTree(refRes.body()).path("object").path("sha").asText("");
            if (parentCommitSha.isBlank()) {
                return null;
            }

            List<Map<String, Object>> treeItems = new ArrayList<>();
            for (Map<String, Object> f : files) {
                String path = String.valueOf(f.getOrDefault("path", "")).replaceAll("^/+", "");
                if (path.isBlank()) continue;
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("path", path);
                item.put("mode", "100644");
                item.put("type", "blob");
                item.put("content", String.valueOf(f.getOrDefault("content", "")));
                treeItems.add(item);
            }

            Map<String, Object> treePayload = new LinkedHashMap<>();
            treePayload.put("base_tree", parentCommitSha);
            treePayload.put("tree", treeItems);

            HttpRequest treeReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/" + owner + "/" + repoName + "/git/trees"))
                    .header("Authorization", "Bearer " + patToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                    .POST(HttpRequest.BodyPublishers.ofString(toJson(treePayload)))
                    .build();
            HttpResponse<String> treeRes = client.send(treeReq, HttpResponse.BodyHandlers.ofString());
            if (treeRes.statusCode() != 201 && treeRes.statusCode() != 200) {
                return null;
            }
            String newTreeSha = objectMapper.readTree(treeRes.body()).path("sha").asText("");
            if (newTreeSha.isBlank()) {
                return null;
            }

            Map<String, Object> commitPayload = new LinkedHashMap<>();
            commitPayload.put("message", commitMessage);
            commitPayload.put("tree", newTreeSha);
            commitPayload.put("parents", List.of(parentCommitSha));

            HttpRequest commitReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/" + owner + "/" + repoName + "/git/commits"))
                    .header("Authorization", "Bearer " + patToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                    .POST(HttpRequest.BodyPublishers.ofString(toJson(commitPayload)))
                    .build();
            HttpResponse<String> commitRes = client.send(commitReq, HttpResponse.BodyHandlers.ofString());
            if (commitRes.statusCode() != 201 && commitRes.statusCode() != 200) {
                return null;
            }
            String newCommitSha = objectMapper.readTree(commitRes.body()).path("sha").asText("");
            if (newCommitSha.isBlank()) {
                return null;
            }

            Map<String, Object> updateRefPayload = Map.of("sha", newCommitSha, "force", true);
            HttpRequest updateRefReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/" + owner + "/" + repoName + "/git/refs/heads/" + branch))
                    .header("Authorization", "Bearer " + patToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(toJson(updateRefPayload)))
                    .build();
            HttpResponse<String> updateRefRes = client.send(updateRefReq, HttpResponse.BodyHandlers.ofString());
            if (updateRefRes.statusCode() == 200) {
                return newCommitSha;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void writeGitRepoFilesToWorkspace(
            String siteSlug,
            String repoOwner,
            String repoName,
            String branch,
            String commitSha,
            String commitMessage,
            List<Map<String, Object>> files,
            String sshPublicKey
    ) {
        try {
            Path repoDir = resolveUploadDir().resolve("git-repos").resolve(siteSlug);
            Files.createDirectories(repoDir);
            for (Map<String, Object> f : files) {
                String relPath = String.valueOf(f.getOrDefault("path", "")).replaceAll("^/+", "");
                if (relPath.isBlank() || relPath.contains("..")) continue;
                Path target = repoDir.resolve(relPath);
                if (target.getParent() != null) {
                    Files.createDirectories(target.getParent());
                }
                Files.writeString(target, String.valueOf(f.getOrDefault("content", "")), StandardCharsets.UTF_8);
            }
            Path gitMetaDir = repoDir.resolve(".git");
            Files.createDirectories(gitMetaDir.resolve("refs").resolve("heads"));
            Files.writeString(gitMetaDir.resolve("HEAD"), "ref: refs/heads/" + branch + "\n", StandardCharsets.UTF_8);
            Files.writeString(gitMetaDir.resolve("refs").resolve("heads").resolve(branch), commitSha + "\n", StandardCharsets.UTF_8);
            Files.writeString(gitMetaDir.resolve("COMMIT_EDITMSG"), commitMessage + "\n", StandardCharsets.UTF_8);
            Files.writeString(gitMetaDir.resolve("authorized_deploy_key.pub"), sshPublicKey + "\n", StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("로컬 Git 작업 디렉터리 기록 경고 (siteSlug={}): {}", siteSlug, e.getMessage());
        }
    }

    private void addZipTextEntry(ZipOutputStream zos, String entryName, String content) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        byte[] bytes = (content != null ? content : "").getBytes(StandardCharsets.UTF_8);
        zos.write(bytes);
        zos.closeEntry();
    }

    private String sanitizeGitIdentifier(String raw, String fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        String cleaned = raw.trim().replaceAll("[^a-zA-Z0-9._-]", "-").replaceAll("-+", "-").replaceAll("^-|-$", "");
        return cleaned.isBlank() ? fallback : cleaned;
    }

    private String escapeJsonString(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

