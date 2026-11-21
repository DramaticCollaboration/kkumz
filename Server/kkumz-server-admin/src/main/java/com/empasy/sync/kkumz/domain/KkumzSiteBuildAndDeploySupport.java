package com.empasy.sync.kkumz.domain;

import com.empasy.sync.kkumz.domain.KkumzDTO.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static com.empasy.sync.kkumz.domain.KkumzJsonAndTextUtils.*;

/**
 * Kkumz 멀티사이트 스캐폴딩, 대화형 디자인 수정, WebIDE 편집, AI 빌드 복구 및 배포·비공개 전환을 담당하는 협력 컴포넌트입니다.
 */
@Slf4j
@RequiredArgsConstructor
class KkumzSiteBuildAndDeploySupport {

    private final KkumzService root;

    Map<String, Object> scaffoldSite(String ideaId) {
        root.accessMeta().assertIdeaEditable(ideaId);
        long startNs = System.nanoTime();
        Map<String, Object> idea = root.ideaSpec().getIdeaDetail(ideaId);
        List<Map<String, Object>> catalog = root.accessMeta().listFeatureCatalog();

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
            Map<String, Object> diag = root.ideaSpec().diagnoseIntentFromCatalog(
                    String.valueOf(idea.getOrDefault("title", "")),
                    String.valueOf(idea.getOrDefault("promptText", "")),
                    String.valueOf(idea.getOrDefault("category", "AUTO")),
                    root.assets().listIdeaAssetsInternal(ideaId),
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
        String brandName = root.brandKit().resolveScaffoldBrandName(idea);
        String siteSlug = root.brandKit().resolveScaffoldSiteSlug(idea, brandName);

        @SuppressWarnings("unchecked")
        Map<String, Object> rawTokens = (Map<String, Object>) idea.get("designTokens");
        Map<String, Object> tokens = root.brandKit().resolveOrProvisionDesignTokens(idea, brandName, siteSlug, category, rawTokens);

        String rawLogoSvg = idea.get("selectedLogoSvg") != null ? String.valueOf(idea.get("selectedLogoSvg")) : "";
        String logoSvg = root.brandKit().resolveOrProvisionLogoSvg(ideaId, idea, brandName, siteSlug, category, tokens, rawLogoSvg);

        String brandSlogan = resolveBrandSlogan(idea, brandName, category, aiIntentAnalysis);
        String resolvedCategoryLabel = root.accessMeta().resolveHumanCategoryLabel(category);
        String themeArchetype = resolveThemeArchetype(
                aiIntentAnalysis.get("themeArchetype") != null ? String.valueOf(aiIntentAnalysis.get("themeArchetype")) : null,
                siteType,
                category,
                enabledFeatures
        );
        String heroStyle = aiIntentAnalysis.get("heroStyle") != null && !String.valueOf(aiIntentAnalysis.get("heroStyle")).isBlank()
                ? String.valueOf(aiIntentAnalysis.get("heroStyle"))
                : resolveDefaultHeroStyleForArchetype(themeArchetype, siteType);
        if (aiIntentAnalysis.get("surfaceMode") != null && !String.valueOf(aiIntentAnalysis.get("surfaceMode")).isBlank()) {
            tokens.putIfAbsent("surfaceMode", String.valueOf(aiIntentAnalysis.get("surfaceMode")));
        }
        if (aiIntentAnalysis.get("fontPreset") != null && !String.valueOf(aiIntentAnalysis.get("fontPreset")).isBlank()) {
            tokens.putIfAbsent("fontPreset", String.valueOf(aiIntentAnalysis.get("fontPreset")));
        }
        String deployMode = "DYNAMIC_SDUI";

        boolean hasFormFeature = hasAnyFormFeature(enabledFeatures);
        Map<String, Object> layoutSchema = new LinkedHashMap<>();
        layoutSchema.put("themeArchetype", themeArchetype);
        layoutSchema.put("heroTitle", brandName);
        layoutSchema.put("heroSubtitle", brandSlogan);
        layoutSchema.put("heroStyle", heroStyle);
        layoutSchema.put("ctaText", needsShop && needsCms ? "시그니처 컬렉션 & 안내 보기" : (needsShop ? "시그니처 컬렉션 보기" : "프로그램·참가 신청하기"));
        layoutSchema.put("showFlashDeal", enabledFeatures.contains("SHOP_FLASH_DEAL"));
        layoutSchema.put("showAiConcierge", enabledFeatures.isEmpty() || enabledFeatures.contains("AI_BRAND_CONCIERGE"));
        layoutSchema.put("showCmsArticles", needsCms || enabledFeatures.contains("CMS_BRAND_MAGAZINE"));
        layoutSchema.put("showCmsForm", hasFormFeature || "CMS_SITE".equalsIgnoreCase(siteType));
        layoutSchema.put("customBadge", resolvedCategoryLabel);
        layoutSchema.put("enabledFeatures", enabledFeatures);

        Long shopSiteId = null;
        Long cmsSiteId = null;
        if (needsShop) {
            shopSiteId = root.runtime().provisionSyncShopSite(siteSlug, brandName, brandSlogan);
        }
        if (needsCms) {
            cmsSiteId = root.runtime().provisionSyncCmsSite(siteSlug, brandName, brandSlogan);
        }

        root.assets().applyUploadedAssetsToSiteProvisioning(ideaId, brandName, layoutSchema, shopSiteId, cmsSiteId);
        provisionDomainSiteContent(idea, brandName, siteSlug, brandSlogan, category, enabledFeatures, catalog, layoutSchema, shopSiteId, cmsSiteId);

        List<Map<String, Object>> existingSite = root.jdbcTemplate().queryForList(
                "SELECT id, active_version AS \"activeVersion\" FROM kkumz_site WHERE idea_id = ? OR site_slug = ? ORDER BY updated_at DESC LIMIT 1",
                ideaId, siteSlug
        );

        String siteId;
        int nextVer;
        if (!existingSite.isEmpty()) {
            siteId = String.valueOf(existingSite.get(0).get("id"));
            nextVer = ((Number) existingSite.get(0).get("activeVersion")).intValue() + 1;
            root.jdbcTemplate().update("""
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
            root.jdbcTemplate().update("""
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

        List<Map<String, Object>> screenSpecs = root.ideaSpec().listIdeaScreensInternal(ideaId);
        List<Map<String, Object>> webideFiles = buildWebIdeFilesFromScreens(brandName, tokens, layoutSchema, screenSpecs);
        String releaseId = "rel-" + siteId + "-v" + nextVer;
        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        long durationMs = Math.max(1L, (System.nanoTime() - startNs) / 1_000_000L);
        String buildLog = "[" + nowTime + "] [Provision] Site slug /s/" + siteSlug + " initialized (v" + nextVer + ", " + durationMs + "ms, features=" + enabledFeatures.size() + ").";
        String bundlePath = "/s/" + siteSlug + "/releases/v" + nextVer;

        root.jdbcTemplate().update("""
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

        root.jdbcTemplate().update("""
                INSERT INTO kkumz_build_job (
                    id, site_id, release_id, job_type, deploy_mode, status, duration_ms,
                    ai_patch_applied, build_log, triggered_by, started_at, finished_at
                ) VALUES (?, ?, ?, 'INITIAL_PROVISION', ?, 'SUCCESS', ?, FALSE, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "job-" + UUID.randomUUID().toString().substring(0, 8),
                siteId, releaseId, deployMode, durationMs, buildLog, idea.get("authorUsername")
        );

        root.jdbcTemplate().update("""
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

        return root.runtime().getSiteRuntime(siteSlug);
    }

    Map<String, Object> chatRefineSite(String siteIdOrSlug, ChatRefineRequest req) {
        root.accessMeta().assertSiteEditable(siteIdOrSlug);
        long startNs = System.nanoTime();
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String ideaId = String.valueOf(site.get("ideaId"));
        String siteSlug = String.valueOf(site.get("siteSlug"));
        int currentVer = ((Number) site.get("activeVersion")).intValue();

        Map<String, Object> latestRel = root.accessMeta().getLatestRelease(siteId, currentVer);
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
            String aiJson = root.ensureAiGateway().executeStagePrompt("CHAT_REFINE", Map.of(
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
            for (String tokenKey : List.of("primary", "secondary", "accent", "surface", "radius", "fontPreset", "surfaceMode")) {
                if (parsed.containsKey(tokenKey)) {
                    tokens.put(tokenKey, parsed.get(tokenKey));
                }
            }
            for (String layoutKey : List.of(
                    "themeArchetype", "heroTitle", "heroSubtitle", "heroStyle", "ctaText", "customBadge",
                    "sectionOrder", "copyOverrides", "showFlashDeal", "showAiConcierge", "enabledFeatures",
                    "featureBlocks", "customProducts", "customArticles", "customCmsForm"
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

        root.jdbcTemplate().update("""
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

        root.jdbcTemplate().update("""
                UPDATE kkumz_site
                SET site_name = ?, brand_slogan = ?, logo_svg = ?, active_version = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                newSiteName, newBrandSlogan, newLogoSvg, nextVer, siteId);

        if (ideaId != null && !ideaId.isBlank() && !"null".equalsIgnoreCase(ideaId)) {
            if (layout.get("enabledFeatures") instanceof List<?> efList) {
                root.jdbcTemplate().update("""
                        UPDATE kkumz_idea
                        SET selected_brand_name = ?, selected_logo_svg = ?, design_tokens_json = ?::jsonb,
                            enabled_features_json = ?::jsonb, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                        newSiteName, newLogoSvg, toJson(tokens), toJson(efList), ideaId);
            } else {
                root.jdbcTemplate().update("""
                        UPDATE kkumz_idea
                        SET selected_brand_name = ?, selected_logo_svg = ?, design_tokens_json = ?::jsonb, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                        newSiteName, newLogoSvg, toJson(tokens), ideaId);
            }
        }

        root.jdbcTemplate().update("""
                INSERT INTO kkumz_build_job (
                    id, site_id, release_id, job_type, deploy_mode, status, duration_ms,
                    ai_patch_applied, build_log, triggered_by, started_at, finished_at
                ) VALUES (?, ?, ?, 'CHAT_HOT_RELOAD', 'DYNAMIC_SDUI', 'SUCCESS', ?, FALSE, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "job-" + UUID.randomUUID().toString().substring(0, 8),
                siteId, releaseId, durationMs, logMsg,
                req.getUsername() != null ? req.getUsername() : site.get("ownerUsername")
        );

        return root.runtime().getSiteRuntime(siteSlug);
    }

    Map<String, Object> getWebIdeFiles(String siteIdOrSlug) {
        root.accessMeta().assertSiteEditable(siteIdOrSlug);
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        int activeVer = ((Number) site.get("activeVersion")).intValue();
        Map<String, Object> rel = root.accessMeta().getLatestRelease(siteId, activeVer);

        List<Map<String, Object>> jobs = root.jdbcTemplate().queryForList("""
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
        workspace.put("gitRepo", root.git().findSiteGitRepoNullable(siteId));
        return workspace;
    }

    Map<String, Object> updateWebIdeFiles(String siteIdOrSlug, UpdateWebIdeFilesRequest req) {
        root.accessMeta().assertSiteEditable(siteIdOrSlug);
        long startNs = System.nanoTime();
        if (req == null || req.getFiles() == null || req.getFiles().isEmpty()) {
            throw new IllegalArgumentException("저장할 WebIDE 소스 파일 목록(files)이 비어 있습니다.");
        }
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));
        int currentVer = ((Number) site.get("activeVersion")).intValue();
        Map<String, Object> latestRel = root.accessMeta().getLatestRelease(siteId, currentVer);

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

        root.jdbcTemplate().update("""
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
            root.jdbcTemplate().update("UPDATE kkumz_site SET active_version = ?, deploy_mode = 'CUSTOM_BUNDLE', updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    nextVer, siteId);
        }

        root.jdbcTemplate().update("""
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

    Map<String, Object> aiFixBuildError(String siteIdOrSlug, FixBuildRequest req) {
        root.accessMeta().assertSiteEditable(siteIdOrSlug);
        long startNs = System.nanoTime();
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));

        List<Map<String, Object>> latestRelRows = root.jdbcTemplate().queryForList("""
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

        String repairedJson = root.ensureAiGateway().executeStagePrompt("BUILD_FIX", Map.of(
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

        root.jdbcTemplate().update("""
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

        root.jdbcTemplate().update("UPDATE kkumz_site SET active_version = ?, deploy_mode = 'CUSTOM_BUNDLE', updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                nextVer, siteId);

        root.jdbcTemplate().update("""
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

    Map<String, Object> rollbackSiteRelease(String siteIdOrSlug, Integer versionNo) {
        root.accessMeta().assertSiteEditable(siteIdOrSlug);
        if (versionNo == null || versionNo <= 0) {
            throw new IllegalArgumentException("롤백할 버전 번호(versionNo)가 유효하지 않습니다.");
        }
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));

        Integer exists = root.jdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM kkumz_site_release WHERE site_id = ? AND version_no = ?",
                Integer.class, siteId, versionNo
        );
        if (exists == null || exists == 0) {
            throw new IllegalArgumentException("롤백할 릴리즈 버전이 존재하지 않습니다: v" + versionNo);
        }
        root.jdbcTemplate().update("UPDATE kkumz_site SET active_version = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                versionNo, siteId);
        return root.runtime().getSiteRuntime(siteSlug);
    }

    Map<String, Object> deploySite(String siteIdOrSlug, DeploySiteRequest req) {
        root.accessMeta().assertSiteEditable(siteIdOrSlug);
        long startNs = System.nanoTime();
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
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
            Map<String, Object> latestRel = root.accessMeta().getLatestRelease(siteId, currentVer);
            finalVer = currentVer + 1;
            String releaseId = "rel-" + siteId + "-v" + finalVer;
            long durationMs = Math.max(1L, (System.nanoTime() - startNs) / 1_000_000L);
            String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String buildLog = "[" + nowTime + "] [Deploy] Published release v" + finalVer + " (" + deployMode + ", " + durationMs + "ms): " + releaseNote;
            String actor = (req.getUsername() != null && !req.getUsername().isBlank())
                    ? req.getUsername().trim()
                    : String.valueOf(site.get("ownerUsername"));

            root.jdbcTemplate().update("""
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

            root.jdbcTemplate().update("""
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
            root.jdbcTemplate().update("""
                    UPDATE kkumz_site
                    SET deploy_mode = ?, visibility = 'PRIVATE', custom_domain = ?, active_version = ?,
                        published_version = NULL, status = 'DRAFT', updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                    deployMode, customDomain, finalVer, siteId
            );
            if (ideaId != null && !ideaId.isBlank() && !"null".equalsIgnoreCase(ideaId)) {
                root.jdbcTemplate().update("""
                        UPDATE kkumz_idea
                        SET status = 'DRAFT', visibility = 'PRIVATE', updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                        ideaId
                );
            }
        } else {
            root.jdbcTemplate().update("""
                    UPDATE kkumz_site
                    SET deploy_mode = ?, visibility = 'PUBLIC', custom_domain = ?, active_version = ?,
                        published_version = ?, published_at = CURRENT_TIMESTAMP, status = 'ACTIVE', updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                    deployMode, customDomain, finalVer, finalVer, siteId
            );
            if (ideaId != null && !ideaId.isBlank() && !"null".equalsIgnoreCase(ideaId)) {
                root.jdbcTemplate().update("""
                        UPDATE kkumz_idea
                        SET status = 'PUBLISHED', visibility = 'PUBLIC', current_step = 6, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                        ideaId
                );
            }
        }

        root.accessMeta().createNotification(
                String.valueOf(site.get("ownerUsername")),
                "BUILD_DONE",
                site.get("siteName") + " v" + finalVer + ("PRIVATE".equalsIgnoreCase(visibility) ? " 비공개 전환 완료" : " 라이브 배포 완료"),
                "PRIVATE".equalsIgnoreCase(visibility)
                        ? "사이트가 비공개 초안 모드로 전환되어 외부 노출이 중지되었습니다."
                        : "멀티사이트 런타임(/s/" + siteSlug + ")에 최신 릴리즈가 반영되었습니다.",
                "/s/" + siteSlug
        );

        return root.runtime().getSiteRuntime(siteSlug);
    }

    Map<String, Object> unpublishSite(String siteIdOrSlug) {
        root.accessMeta().assertSiteEditable(siteIdOrSlug);
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String ideaId = site.get("ideaId") != null ? String.valueOf(site.get("ideaId")) : null;
        String siteSlug = String.valueOf(site.get("siteSlug"));

        root.jdbcTemplate().update("""
                UPDATE kkumz_site
                SET status = 'DRAFT', visibility = 'PRIVATE', published_version = NULL, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                siteId
        );
        if (ideaId != null && !ideaId.isBlank() && !"null".equalsIgnoreCase(ideaId)) {
            root.jdbcTemplate().update("""
                    UPDATE kkumz_idea
                    SET status = 'DRAFT', visibility = 'PRIVATE', updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                    ideaId
            );
        }

        root.accessMeta().createNotification(
                String.valueOf(site.get("ownerUsername")),
                "BUILD_DONE",
                site.get("siteName") + " 비공개 전환 완료",
                "사이트가 비공개 초안 상태(나만 보기)로 전환되어 쇼케이스 피드와 외부 접속이 차단되었습니다.",
                "/studio"
        );

        return root.runtime().getSiteRuntime(siteSlug);
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
        List<Map<String, Object>> assets = root.assets().listIdeaAssetsInternal(ideaId);

        // 1. 기존 배포 릴리즈에 이미 생성된 featureBlocks / customProducts / customCmsForm이 있다면 기본값으로 보존
        try {
            List<Map<String, Object>> prevSites = root.jdbcTemplate().queryForList(
                    "SELECT id, active_version AS \"activeVersion\" FROM kkumz_site WHERE idea_id = ? OR site_slug = ? ORDER BY updated_at DESC LIMIT 1",
                    ideaId, siteSlug
            );
            if (!prevSites.isEmpty()) {
                String prevSiteId = String.valueOf(prevSites.get(0).get("id"));
                int prevVer = ((Number) prevSites.get(0).get("activeVersion")).intValue();
                Map<String, Object> prevRel = root.accessMeta().getLatestRelease(prevSiteId, prevVer);
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
                String rawProvisionJson = root.ensureAiGateway().executeStagePrompt(
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
        if (aiContent.get("themeArchetype") != null && !String.valueOf(aiContent.get("themeArchetype")).isBlank()) {
            layoutSchema.put("themeArchetype", String.valueOf(aiContent.get("themeArchetype")).trim());
        }
        if (aiContent.get("heroStyle") != null && !String.valueOf(aiContent.get("heroStyle")).isBlank()) {
            layoutSchema.put("heroStyle", String.valueOf(aiContent.get("heroStyle")).trim());
        }
        if (aiContent.get("ctaText") != null && !String.valueOf(aiContent.get("ctaText")).isBlank()) {
            layoutSchema.put("ctaText", String.valueOf(aiContent.get("ctaText")).trim());
        }
        if (aiContent.get("sectionOrder") instanceof List<?> so && !so.isEmpty()) {
            layoutSchema.put("sectionOrder", so);
        }
        if (aiContent.get("copyOverrides") instanceof Map<?, ?> co && !co.isEmpty()) {
            layoutSchema.put("copyOverrides", co);
        }

        // 3. featureBlocks 병합 (AI 생성 데이터 + 엄격한 선택 기능 게이팅)
        Map<String, Object> featureBlocks = layoutSchema.get("featureBlocks") instanceof Map<?, ?> existingFb
                ? new LinkedHashMap<>((Map<String, Object>) existingFb)
                : new LinkedHashMap<>();
        if (aiContent.get("featureBlocks") instanceof Map<?, ?> aiFb) {
            aiFb.forEach((k, v) -> {
                if (v != null && !(v instanceof String s && s.isBlank())) {
                    featureBlocks.put(String.valueOf(k), v);
                }
            });
        }

        root.runtime().hydrateFeatureBlocksStrict(brandName, brandSlogan, category, enabledFeatures, assets, Map.of(), featureBlocks);
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
            if (cmsSiteId != null && root.cmsJdbcTemplate() != null && formMap.get("fields") instanceof List<?> fieldsList) {
                try {
                    root.cmsJdbcTemplate().update("""
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
        } else if (!layoutSchema.containsKey("customCmsForm") && hasAnyFormFeature(enabledFeatures)) {
            // 선택된 폼 기능이 활성화된 경우에만 DB kkumz_feature_catalog defaultConfig 기반 폼 구성
            catalog.stream()
                    .filter(fc -> "CMS_DYNAMIC_BOOKING_FORM".equals(fc.get("featureCode")) || "CMS_DYNAMIC_FORM".equals(fc.get("featureCode")))
                    .findFirst()
                    .ifPresent(fc -> {
                        Map<String, Object> defCfg = (Map<String, Object>) fc.getOrDefault("defaultConfig", Map.of());
                        Map<String, Object> formMap = new LinkedHashMap<>();
                        formMap.put("id", 1L);
                        formMap.put("title", brandName + " 온라인 신청 · 문의");
                        formMap.put("formName", brandName + " 온라인 신청 · 문의");
                        formMap.put("description", brandSlogan);
                        formMap.put("submitText", "신청서 접수하기");
                        Object rawFields = defCfg.get("fields");
                        if (rawFields instanceof List<?> fl && !fl.isEmpty()) {
                            formMap.put("fields", fl);
                        }
                        layoutSchema.put("customCmsForm", formMap);
                    });
        }
    }

    private List<Map<String, Object>> normalizeAndSyncProducts(
            String brandName,
            String category,
            List<Map<String, Object>> rawProducts,
            List<Map<String, Object>> assets,
            Long shopSiteId
    ) {
        Map<String, Object> curatedMedia = root.accessMeta().resolveCuratedMediaForCategory(category);
        List<String> curatedItems = curatedMedia.get("items") instanceof List<?> ci
                ? ci.stream().map(String::valueOf).toList()
                : List.of();
        String curatedHero = curatedMedia.get("hero") != null ? String.valueOf(curatedMedia.get("hero")) : "";

        List<Map<String, Object>> result = new ArrayList<>();
        int idx = 1;
        for (Map<String, Object> rp : rawProducts) {
            String name = String.valueOf(rp.getOrDefault("name", brandName + " 시그니처 #" + idx)).trim();
            String subTitle = String.valueOf(rp.getOrDefault("subTitle", rp.getOrDefault("description", brandName + " 대표 라인업"))).trim();
            String desc = String.valueOf(rp.getOrDefault("description", subTitle)).trim();
            String pic = String.valueOf(rp.getOrDefault("pic", rp.getOrDefault("imageUrl", ""))).trim();
            if (pic.isBlank()) {
                if (assets != null && !assets.isEmpty()) {
                    Map<String, Object> asset = assets.get((idx - 1) % assets.size());
                    pic = String.valueOf(asset.getOrDefault("fileUrl", curatedHero));
                } else if (!curatedItems.isEmpty()) {
                    pic = curatedItems.get((idx - 1) % curatedItems.size());
                } else {
                    pic = curatedHero;
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
            if (shopSiteId != null && root.shopJdbcTemplate() != null) {
                try {
                    List<Map<String, Object>> exist = root.shopJdbcTemplate().queryForList(
                            "SELECT id FROM pms_product WHERE site_id = ? AND name = ? AND delete_status = 0 LIMIT 1",
                            shopSiteId, name
                    );
                    if (!exist.isEmpty()) {
                        prodId = ((Number) exist.get(0).get("id")).longValue();
                        root.shopJdbcTemplate().update("""
                                UPDATE pms_product
                                SET pic = ?, price = ?, promotion_price = ?, original_price = ?,
                                    sub_title = ?, description = ?, unit = ?, product_category_name = ?, note = ?
                                WHERE id = ?
                                """,
                                pic, BigDecimal.valueOf(price), BigDecimal.valueOf(promoPrice), BigDecimal.valueOf(price),
                                subTitle, desc, unit, catName, badge, prodId
                        );
                    } else {
                        prodId = root.shopJdbcTemplate().queryForObject("""
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
                cover = String.valueOf(assets.get((idx - 1) % assets.size()).getOrDefault("fileUrl", ""));
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
}
