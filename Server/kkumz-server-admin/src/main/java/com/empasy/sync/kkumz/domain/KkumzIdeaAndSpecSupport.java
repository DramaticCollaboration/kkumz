package com.empasy.sync.kkumz.domain;

import com.empasy.sync.kkumz.domain.KkumzDTO.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static com.empasy.sync.kkumz.domain.KkumzJsonAndTextUtils.*;
import tools.jackson.core.type.TypeReference;

/**
 * Kkumz 아이디어 CRUD, 버전 히스토리, AI 의도 분석, 기능·화면설계 및 기능명세서(FRS) 생성을 담당하는 협력 컴포넌트입니다.
 */
@Slf4j
@RequiredArgsConstructor
class KkumzIdeaAndSpecSupport {

    private final KkumzService root;

    List<Map<String, Object>> listIdeas(String authorUsername, String siteType) {
        return listIdeas(authorUsername, siteType, false);
    }

    List<Map<String, Object>> listIdeas(String authorUsername, String siteType, Boolean mineOnly) {
        String currentUser = root.accessMeta().resolveCurrentUsernameFromRequest();
        if (root.accessMeta().isHttpRequestActive() && (currentUser == null || currentUser.isBlank())) {
            return List.of();
        }
        boolean isAdmin = currentUser != null && !currentUser.isBlank() && root.accessMeta().isAdminUser(currentUser);
        String targetUser;
        if (root.accessMeta().isHttpRequestActive()) {
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

        List<Map<String, Object>> list = root.jdbcTemplate().queryForList(sql.toString(), params.toArray());
        list.forEach(root.accessMeta()::enrichIdeaRow);
        return list;
    }

    Map<String, Object> createIdea(CreateIdeaRequest req) {
        if (req == null || req.getPromptText() == null || req.getPromptText().isBlank()) {
            throw new IllegalArgumentException("창업 아이디어 프롬프트(promptText)는 필수 입력 항목입니다.");
        }
        String authenticatedUser = root.accessMeta().isHttpRequestActive() ? root.accessMeta().requireAuthenticatedUsername() : root.accessMeta().resolveCurrentUsernameFromRequest();
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
        Map<String, String> author = root.accessMeta().resolveUserIdentity(requestedUser, req.getAuthorDisplayName());
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

        root.jdbcTemplate().update("""
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
        root.jdbcTemplate().update("""
                INSERT INTO kkumz_idea_version (
                    id, idea_id, version_no, title, prompt_text, spec_markdown, screens_json, created_by, created_at
                ) VALUES (?, ?, 1, ?, ?, ?, '[]'::jsonb, ?, CURRENT_TIMESTAMP)
                """,
                verId, id, title + " (v1)", promptText, promptText, authorUsername
        );

        return getIdeaDetail(id);
    }

    Map<String, Object> getIdeaDetail(String ideaId) {
        List<Map<String, Object>> rows = root.jdbcTemplate().queryForList("""
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

        root.accessMeta().assertIdeaReadable(rows.get(0));

        Map<String, Object> idea = new LinkedHashMap<>(rows.get(0));
        root.accessMeta().enrichIdeaRow(idea);

        List<Map<String, Object>> versions = root.jdbcTemplate().queryForList("""
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

        List<Map<String, Object>> names = root.jdbcTemplate().queryForList("""
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

        List<Map<String, Object>> logos = root.jdbcTemplate().queryForList("""
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
        idea.put("assets", root.assets().listIdeaAssetsInternal(ideaId));

        List<Map<String, Object>> sites = root.jdbcTemplate().queryForList("""
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
            sites = root.jdbcTemplate().queryForList("""
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
                boolean isPub = root.accessMeta().isSitePubliclyPublished(s);
                int activeVer = s.get("activeVersion") instanceof Number n ? n.intValue() : 1;
                Integer pubVer = root.accessMeta().resolvePublishedVersion(s);
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

    Map<String, Object> saveOrRollbackIdeaVersion(String ideaId, SaveIdeaVersionRequest req) {
        root.accessMeta().assertIdeaEditable(ideaId);
        Map<String, Object> current = getIdeaDetail(ideaId);

        if (req.getRollbackToVersionNo() != null && req.getRollbackToVersionNo() > 0) {
            List<Map<String, Object>> targetRows = root.jdbcTemplate().queryForList("""
                    SELECT title, prompt_text AS "promptText", spec_markdown AS "specMarkdown", screens_json::text AS "screensJson"
                    FROM kkumz_idea_version
                    WHERE idea_id = ? AND version_no = ?
                    """, ideaId, req.getRollbackToVersionNo());
            if (targetRows.isEmpty()) {
                throw new IllegalArgumentException("롤백 대상 버전을 찾을 수 없습니다: v" + req.getRollbackToVersionNo());
            }
            Map<String, Object> target = targetRows.get(0);
            String targetScreensJson = String.valueOf(target.getOrDefault("screensJson", "[]"));
            root.jdbcTemplate().update("""
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

        Integer maxVer = root.jdbcTemplate().queryForObject(
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

        root.jdbcTemplate().update("""
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

        root.jdbcTemplate().update("""
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

    Map<String, Object> analyzeIdeaIntent(String ideaId, AnalyzeIntentRequest req) {
        root.accessMeta().assertIdeaEditable(ideaId);
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

        List<Map<String, Object>> assets = root.assets().listIdeaAssetsInternal(ideaId);
        List<Map<String, Object>> catalog = root.accessMeta().listFeatureCatalog();

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
            String rawAiJson = root.ensureAiGateway().executeStagePrompt(
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
                ? root.objectMapper().convertValue(rawAnn, new TypeReference<List<Map<String, Object>>>() {})
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
                    root.jdbcTemplate().update(
                            "UPDATE kkumz_idea_asset SET asset_type = COALESCE(NULLIF(?, ''), asset_type), caption = ? WHERE id = ? AND idea_id = ?",
                            newType, newCap, targetAssetId, ideaId
                    );
                } else if (!targetOrigName.isBlank()) {
                    root.jdbcTemplate().update(
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

        root.jdbcTemplate().update("""
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

        root.assets().syncIdeaAssetsToDeployedSiteIfPresent(ideaId);
        return getIdeaDetail(ideaId);
    }

    Map<String, Object> updateIdeaFeatures(String ideaId, UpdateIdeaFeaturesRequest req) {
        root.accessMeta().assertIdeaEditable(ideaId);
        Map<String, Object> idea = getIdeaDetail(ideaId);
        List<Map<String, Object>> catalog = root.accessMeta().listFeatureCatalog();
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

        root.jdbcTemplate().update("""
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
        List<Map<String, Object>> deployed = root.jdbcTemplate().queryForList(
                "SELECT id, active_version AS \"activeVersion\" FROM kkumz_site WHERE idea_id = ? ORDER BY created_at DESC LIMIT 1",
                ideaId
        );
        if (!deployed.isEmpty()) {
            String siteId = String.valueOf(deployed.get(0).get("id"));
            int activeVer = ((Number) deployed.get(0).get("activeVersion")).intValue();
            Map<String, Object> rel = root.accessMeta().getLatestRelease(siteId, activeVer);
            Map<String, Object> layoutSchema = new LinkedHashMap<>(parseJsonObject(String.valueOf(rel.get("layoutSchemaJson"))));
            layoutSchema.put("enabledFeatures", enabledFeatures);
            root.jdbcTemplate().update("UPDATE kkumz_site SET site_type = ?, category = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    siteType, category, siteId);
            root.jdbcTemplate().update("UPDATE kkumz_site_release SET layout_schema_json = ?::jsonb WHERE id = ?",
                    toJson(layoutSchema), rel.get("id"));
        }

        return getIdeaDetail(ideaId);
    }

    List<Map<String, Object>> listIdeaScreens(String ideaId) {
        root.accessMeta().assertIdeaReadable(ideaId);
        return listIdeaScreensInternal(ideaId);
    }

    List<Map<String, Object>> listIdeaScreensInternal(String ideaId) {
        List<Map<String, Object>> screens = root.jdbcTemplate().queryForList("""
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

    List<Map<String, Object>> saveIdeaScreen(String ideaId, SaveScreenSpecRequest req) {
        root.accessMeta().assertIdeaEditable(ideaId);
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
            int updated = root.jdbcTemplate().update("""
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

        root.jdbcTemplate().update("""
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

    List<Map<String, Object>> deleteIdeaScreen(String ideaId, String screenId) {
        root.accessMeta().assertIdeaEditable(ideaId);
        root.jdbcTemplate().update("DELETE FROM kkumz_screen_spec WHERE id = ? AND idea_id = ?", screenId, ideaId);
        List<Map<String, Object>> remaining = listIdeaScreensInternal(ideaId);
        root.jdbcTemplate().update("UPDATE kkumz_idea SET screens_json = ?::jsonb, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                toJson(remaining), ideaId);
        return remaining;
    }

    private void syncScreenSpecs(String ideaId, List<Map<String, Object>> screens) {
        if (screens == null) {
            return;
        }
        root.jdbcTemplate().update("DELETE FROM kkumz_screen_spec WHERE idea_id = ?", ideaId);
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

    Map<String, Object> generateSpec(String ideaId) {
        return generateSpec(ideaId, false);
    }

    Map<String, Object> generateSpec(String ideaId, boolean forceRegenerate) {
        root.accessMeta().assertIdeaEditable(ideaId);
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
        String promptText = root.assets().buildPromptWithAssetContext(
                rawPromptText,
                root.assets().listIdeaAssets(ideaId)
        );

        Map<String, Object> meta = root.accessMeta().getStudioMetadata();
        @SuppressWarnings("unchecked")
        Map<String, String> layoutLabels = (Map<String, String>) meta.getOrDefault("layoutLabels", Map.of());
        @SuppressWarnings("unchecked")
        Map<String, String> componentLabels = (Map<String, String>) meta.getOrDefault("componentLabels", Map.of());

        List<String> layoutCatalog = new ArrayList<>();
        layoutLabels.forEach((code, label) -> layoutCatalog.add(code + "(" + label + ")"));
        List<String> componentCatalog = new ArrayList<>();
        componentLabels.forEach((code, label) -> componentCatalog.add(code + "(" + label + ")"));

        String rawOutput = root.ensureAiGateway().executeStagePrompt("SPEC_FRS", Map.of(
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
                    screens = root.objectMapper().convertValue(rawScreens, new TypeReference<List<Map<String, Object>>>() {});
                }
            }
        } catch (Exception ignored) {
            // Markdown 평문 응답인 경우 그대로 사용
        }

        root.jdbcTemplate().update("""
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

    Map<String, Object> updateSpec(String ideaId, UpdateSpecRequest req) {
        root.accessMeta().assertIdeaEditable(ideaId);
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

        root.jdbcTemplate().update("""
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

    Map<String, Object> diagnoseIntentFromCatalog(
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
}
