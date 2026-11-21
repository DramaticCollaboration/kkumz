package com.empasy.sync.kkumz.domain;

import com.empasy.sync.kkumz.domain.KkumzDTO.*;
import com.empasy.sync.kkumz.system.KkumzSystemService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static com.empasy.sync.kkumz.domain.KkumzJsonAndTextUtils.*;

/**
 * Kkumz 스튜디오 메타데이터, 기능 카탈로그, 인증 세션 추출 및 아이디어·사이트 소유권(RBAC) 검증 협력 컴포넌트입니다.
 */
@Slf4j
@RequiredArgsConstructor
class KkumzAccessAndMetadataSupport {

    private final KkumzService root;

    List<Map<String, Object>> listFeatureCatalog() {
        try {
            List<Map<String, Object>> rows = root.jdbcTemplate().queryForList("""
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

    Map<String, Object> getStudioMetadata() {
        List<Map<String, Object>> rows = root.jdbcTemplate().queryForList("""
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
        List<Map<String, Object>> themeArchetypes = new ArrayList<>();
        List<Map<String, Object>> fontPresets = new ArrayList<>();
        Map<String, String> categoryLabels = new LinkedHashMap<>();

        for (Map<String, Object> row : rows) {
            String dictCode = String.valueOf(row.get("dictCode"));
            String itemValue = String.valueOf(row.get("itemValue"));
            String itemText = String.valueOf(row.get("itemText"));
            String description = row.get("description") != null ? String.valueOf(row.get("description")) : "";

            switch (dictCode) {
                case "kkumz_layout_type" -> layoutLabels.put(itemValue, itemText);
                case "kkumz_component_type" -> componentLabels.put(itemValue, itemText);
                case "kkumz_recruit_role" -> roleLabels.put(itemValue, itemText);
                case "kkumz_content_category" -> categoryLabels.put(itemValue, itemText);
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
                case "kkumz_theme_archetype" -> {
                    Map<String, Object> ta = new LinkedHashMap<>(parseJsonObject(description));
                    ta.put("code", itemValue);
                    ta.put("label", itemText);
                    themeArchetypes.add(ta);
                }
                case "kkumz_font_preset" -> {
                    Map<String, Object> fp = new LinkedHashMap<>();
                    fp.put("code", itemValue);
                    fp.put("label", itemText);
                    fp.put("fontFamily", description);
                    fontPresets.add(fp);
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
        result.put("categoryLabels", categoryLabels);
        result.put("siteTypes", siteTypes);
        result.put("assetTypes", assetTypes);
        result.put("starterPresets", starterPresets);
        result.put("featureCategories", featureCategories);
        result.put("heroStyles", heroStyles);
        result.put("themeArchetypes", themeArchetypes);
        result.put("fontPresets", fontPresets);
        result.put("featureCatalog", featureCatalog);
        return result;
    }

    boolean isHttpRequestActive() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes;
    }

    String resolveCurrentUsernameFromRequest() {
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

    String requireAuthenticatedUsername() {
        String current = resolveCurrentUsernameFromRequest();
        if (current != null && !current.isBlank()) {
            return current;
        }
        if (isHttpRequestActive()) {
            throw new SecurityException("[401] 로그인이 필요한 기능입니다. 먼저 로그인해 주세요.");
        }
        return null;
    }

    boolean isAdminUser(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        String user = username.trim();
        if ("admin".equalsIgnoreCase(user)) {
            return true;
        }
        try {
            Integer cnt = root.jdbcTemplate().queryForObject("""
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

    boolean canEditIdea(Map<String, Object> ideaRow, String currentUsername) {
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
                Integer teamCnt = root.jdbcTemplate().queryForObject("""
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

    boolean canEditSite(Map<String, Object> siteRow, String currentUsername) {
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
                Integer ideaOwnerCnt = root.jdbcTemplate().queryForObject(
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
                Integer teamCnt = root.jdbcTemplate().queryForObject("""
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

    void assertIdeaEditable(String ideaId) {
        if (!isHttpRequestActive()) {
            return;
        }
        String currentUser = requireAuthenticatedUsername();
        List<Map<String, Object>> rows = root.jdbcTemplate().queryForList(
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

    void assertSiteEditable(String siteIdOrSlug) {
        if (!isHttpRequestActive()) {
            return;
        }
        String currentUser = requireAuthenticatedUsername();
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        if (!canEditSite(site, currentUser)) {
            throw new SecurityException("[403] 본인이 생성한 사이트(또는 승인된 협업 팀원)만 수정할 수 있습니다.");
        }
    }

    boolean canViewIdea(Map<String, Object> ideaRow, String currentUsername) {
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
                Integer teamCnt = root.jdbcTemplate().queryForObject("""
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

    void assertIdeaReadable(Map<String, Object> ideaRow) {
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

    void assertIdeaReadable(String ideaId) {
        if (!isHttpRequestActive()) {
            return;
        }
        String currentUser = resolveCurrentUsernameFromRequest();
        if (currentUser == null || currentUser.isBlank()) {
            throw new SecurityException("[401] 로그인이 필요합니다. 본인이 생성한 아이디어만 열람할 수 있습니다.");
        }
        List<Map<String, Object>> rows = root.jdbcTemplate().queryForList(
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

    Integer resolvePublishedVersion(Map<String, Object> siteRow) {
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

    boolean isSitePubliclyPublished(Map<String, Object> siteRow) {
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

    boolean canViewSite(Map<String, Object> siteRow, String currentUsername) {
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
                    Integer teamCnt = root.jdbcTemplate().queryForObject("""
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

    void assertSiteReadable(Map<String, Object> siteRow) {
        if (!isHttpRequestActive()) {
            return;
        }
        String currentUser = resolveCurrentUsernameFromRequest();
        if (!canViewSite(siteRow, currentUser)) {
            throw new SecurityException("[403] 아직 배포되지 않았거나 비공개 상태인 사이트입니다.");
        }
    }

    void assertSiteReadable(String siteIdOrSlug) {
        if (!isHttpRequestActive()) {
            return;
        }
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        assertSiteReadable(site);
    }

    Map<String, String> resolveUserIdentity(String requestedUsername, String requestedDisplayName) {
        if (requestedUsername != null && !requestedUsername.isBlank()) {
            String uname = requestedUsername.trim();
            if (requestedDisplayName != null && !requestedDisplayName.isBlank()) {
                return Map.of("username", uname, "realname", requestedDisplayName.trim());
            }
            List<Map<String, Object>> rows = root.jdbcTemplate().queryForList(
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

        List<Map<String, Object>> defaultRows = root.jdbcTemplate().queryForList(
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

    Map<String, Object> findSiteByIdOrSlug(String siteIdOrSlug) {
        List<Map<String, Object>> rows = root.jdbcTemplate().queryForList("""
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

    Map<String, Object> getLatestRelease(String siteId, int versionNo) {
        List<Map<String, Object>> rows = root.jdbcTemplate().queryForList("""
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

        List<Map<String, Object>> fallback = root.jdbcTemplate().queryForList("""
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

    void createNotification(String recipient, String type, String title, String message, String linkUrl) {
        if (recipient == null || recipient.isBlank()) {
            return;
        }
        root.jdbcTemplate().update("""
                INSERT INTO kkumz_notification (
                    id, recipient_username, notif_type, title, message, link_url, is_read, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, FALSE, CURRENT_TIMESTAMP)
                """,
                "noti-" + UUID.randomUUID().toString().substring(0, 8),
                recipient.trim(), type, title, message, linkUrl
        );
    }

    void enrichIdeaRow(Map<String, Object> row) {
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

    String resolveHumanCategoryLabel(String rawCategory) {
        if (rawCategory == null || rawCategory.isBlank() || "AUTO".equalsIgnoreCase(rawCategory.trim())) {
            return "브랜드 · 서비스";
        }
        String trimmed = rawCategory.trim();
        if (!trimmed.matches("^[A-Z0-9_]+$")) {
            return trimmed;
        }
        try {
            List<Map<String, Object>> rows = root.jdbcTemplate().queryForList("""
                    SELECT i.item_text AS "label"
                    FROM sys_dict_item i
                    JOIN sys_dict d ON d.id = i.dict_id
                    WHERE d.dict_code = 'kkumz_content_category'
                      AND i.item_value = ?
                      AND i.status = 1
                    LIMIT 1
                    """, trimmed);
            if (!rows.isEmpty() && rows.get(0).get("label") != null) {
                return String.valueOf(rows.get(0).get("label"));
            }
        } catch (Exception e) {
            log.debug("카테고리 라벨 사전 조회 생략: {}", e.getMessage());
        }
        return trimmed.replace('_', ' ');
    }

    Map<String, Object> resolveCuratedMediaForCategory(String rawCategory) {
        String key = (rawCategory != null && !rawCategory.isBlank()) ? rawCategory.trim().toUpperCase(Locale.ROOT) : "LIFESTYLE_HYBRID";
        if (!key.matches("^[A-Z0-9_]+$")) {
            key = "HOSPITALITY_STAY";
        }
        try {
            List<Map<String, Object>> rows = root.jdbcTemplate().queryForList("""
                    SELECT i.description AS "mediaJson"
                    FROM sys_dict_item i
                    JOIN sys_dict d ON d.id = i.dict_id
                    WHERE d.dict_code = 'kkumz_curated_media'
                      AND i.item_value = ?
                      AND i.status = 1
                    LIMIT 1
                    """, key);
            if (!rows.isEmpty() && rows.get(0).get("mediaJson") != null) {
                return parseJsonObject(String.valueOf(rows.get(0).get("mediaJson")));
            }
        } catch (Exception e) {
            log.debug("큐레이션 미디어 사전 조회 생략: {}", e.getMessage());
        }
        return Map.of();
    }
}
