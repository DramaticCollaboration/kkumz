package com.empasy.sync.kkumz.domain;

import com.empasy.sync.kkumz.domain.KkumzDTO.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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

    @Value("${kkumz.cross-db.shop-url:}")
    private String shopDbUrl;

    @Value("${kkumz.cross-db.cms-url:}")
    private String cmsDbUrl;

    @Value("${kkumz.cross-db.username:}")
    private String crossDbUsername;

    @Value("${kkumz.cross-db.password:}")
    private String crossDbPassword;

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
    }

    // =========================================================================
    // 0. 스튜디오 공통 사전 메타데이터 조회 (DB sys_dict / sys_dict_item 단일 원천)
    // =========================================================================

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
        List<Map<String, Object>> starterPresets = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            String dictCode = String.valueOf(row.get("dictCode"));
            String itemValue = String.valueOf(row.get("itemValue"));
            String itemText = String.valueOf(row.get("itemText"));
            String description = row.get("description") != null ? String.valueOf(row.get("description")) : "";

            switch (dictCode) {
                case "kkumz_layout_type" -> layoutLabels.put(itemValue, itemText);
                case "kkumz_component_type" -> componentLabels.put(itemValue, itemText);
                case "kkumz_recruit_role" -> roleLabels.put(itemValue, itemText);
                case "kkumz_site_type" -> siteTypeLabels.put(itemValue, itemText);
                case "kkumz_starter_preset" -> {
                    Map<String, Object> preset = new LinkedHashMap<>(parseJsonObject(description));
                    preset.put("code", itemValue);
                    preset.put("title", itemText);
                    starterPresets.add(preset);
                }
                default -> {
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("layoutLabels", layoutLabels);
        result.put("componentLabels", componentLabels);
        result.put("roleLabels", roleLabels);
        result.put("siteTypeLabels", siteTypeLabels);
        result.put("starterPresets", starterPresets);
        return result;
    }

    // =========================================================================
    // 1. 아이디어 & 버전 히스토리 & 화면설계 (Step 1 & Step 3)
    // =========================================================================

    public List<Map<String, Object>> listIdeas(String authorUsername, String siteType) {
        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       author_username AS "authorUsername",
                       author_display_name AS "authorDisplayName",
                       title,
                       prompt_text AS "promptText",
                       site_type AS "siteType",
                       category,
                       tags_json::text AS "tagsJson",
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
        if (authorUsername != null && !authorUsername.isBlank()) {
            sql.append(" AND author_username = ?");
            params.add(authorUsername.trim());
        }
        if (siteType != null && !siteType.isBlank() && !"ALL".equalsIgnoreCase(siteType)) {
            sql.append(" AND site_type = ?");
            params.add(siteType.trim().toUpperCase(Locale.ROOT));
        }
        sql.append(" ORDER BY updated_at DESC");

        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        list.forEach(this::enrichIdeaRow);
        return list;
    }

    @Transactional
    public Map<String, Object> createIdea(CreateIdeaRequest req) {
        if (req == null || req.getPromptText() == null || req.getPromptText().isBlank()) {
            throw new IllegalArgumentException("창업 아이디어 프롬프트(promptText)는 필수 입력 항목입니다.");
        }
        String promptText = req.getPromptText().trim();
        String id = "idea-" + UUID.randomUUID().toString().substring(0, 8);
        String siteType = (req.getSiteType() != null && !req.getSiteType().isBlank())
                ? req.getSiteType().trim().toUpperCase(Locale.ROOT)
                : "COMMERCE";
        String category = (req.getCategory() != null && !req.getCategory().isBlank())
                ? req.getCategory().trim().toUpperCase(Locale.ROOT)
                : "FOOD_BEVERAGE";
        String title = (req.getTitle() != null && !req.getTitle().isBlank())
                ? req.getTitle().trim()
                : summarizeTitle(promptText);

        Map<String, String> author = resolveUserIdentity(req.getAuthorUsername(), req.getAuthorDisplayName());
        String authorUsername = author.get("username");
        String authorDisplay = author.get("realname");
        String visibility = (req.getVisibility() != null && !req.getVisibility().isBlank())
                ? req.getVisibility().trim().toUpperCase(Locale.ROOT)
                : "PUBLIC";

        List<String> tags = (req.getTags() != null && !req.getTags().isEmpty())
                ? req.getTags()
                : List.of(category);

        jdbcTemplate.update("""
                INSERT INTO kkumz_idea (
                    id, author_username, author_display_name, title, prompt_text, site_type, category,
                    tags_json, visibility, current_step, spec_markdown, screens_json, status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, 2, ?, '[]'::jsonb, 'DRAFT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                id, authorUsername, authorDisplay, title, promptText, siteType, category,
                toJson(tags), visibility, promptText
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
        idea.put("screenSpecs", listIdeaScreens(ideaId));

        List<Map<String, Object>> sites = jdbcTemplate.queryForList("""
                SELECT id,
                       site_slug AS "siteSlug",
                       site_name AS "siteName",
                       site_type AS "siteType",
                       deploy_mode AS "deployMode",
                       active_version AS "activeVersion",
                       deployed_url AS "deployedUrl",
                       shop_site_id AS "shopSiteId",
                       cms_site_id AS "cmsSiteId",
                       status
                FROM kkumz_site
                WHERE idea_id = ?
                ORDER BY created_at DESC
                """, ideaId);
        idea.put("deployedSites", sites);

        return idea;
    }

    @Transactional
    public Map<String, Object> saveOrRollbackIdeaVersion(String ideaId, SaveIdeaVersionRequest req) {
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
            jdbcTemplate.update("""
                    UPDATE kkumz_idea
                    SET prompt_text = ?, spec_markdown = ?, screens_json = ?::jsonb, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                    target.get("promptText"),
                    target.get("specMarkdown"),
                    target.get("screensJson"),
                    ideaId
            );
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

        jdbcTemplate.update("""
                INSERT INTO kkumz_idea_version (
                    id, idea_id, version_no, title, prompt_text, spec_markdown, screens_json, created_by, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?, CURRENT_TIMESTAMP)
                """,
                "iver-" + ideaId + "-v" + nextVer, ideaId, nextVer, title, promptText, specMarkdown, screensJson, createdBy
        );

        jdbcTemplate.update("""
                UPDATE kkumz_idea
                SET prompt_text = ?, spec_markdown = ?, screens_json = ?::jsonb, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                promptText, specMarkdown, screensJson, ideaId
        );

        return getIdeaDetail(ideaId);
    }

    public List<Map<String, Object>> listIdeaScreens(String ideaId) {
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
        int sortNo = req.getSortNo() != null ? req.getSortNo() : (listIdeaScreens(ideaId).size() + 1);
        List<String> components = req.getComponents() != null ? req.getComponents() : List.of("BrandStoryBanner", "FeaturedProductGrid");

        jdbcTemplate.update("""
                INSERT INTO kkumz_screen_spec (
                    id, idea_id, screen_key, screen_title, route_path, layout_type, description, components_json, sort_no, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, CURRENT_TIMESTAMP)
                """,
                "scr-" + UUID.randomUUID().toString().substring(0, 8),
                ideaId,
                screenKey,
                req.getScreenTitle().trim(),
                routePath,
                layoutType,
                req.getDescription() != null ? req.getDescription().trim() : "",
                toJson(components),
                sortNo
        );

        return listIdeaScreens(ideaId);
    }

    // =========================================================================
    // 2. Step 2 — AI 브랜드 네이밍 & 벡터 SVG 로고 생성 (AgentScope Java 연동)
    // =========================================================================

    @Transactional
    public Map<String, Object> generateNameSuggestions(String ideaId, GenerateNamesRequest req) {
        Map<String, Object> idea = getIdeaDetail(ideaId);
        String promptText = String.valueOf(idea.get("promptText"));
        String category = String.valueOf(idea.get("category"));
        String keywordHint = (req != null && req.getKeywordHint() != null) ? req.getKeywordHint().trim() : "";

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> existingNames = (List<Map<String, Object>>) idea.get("nameSuggestions");
        if (existingNames != null && !existingNames.isEmpty() && keywordHint.isBlank()) {
            return idea;
        }

        String rawJson = ensureAiGateway().executeStagePrompt("NAMING", Map.of(
                "promptText", promptText,
                "category", category,
                "keywordHint", keywordHint
        ));

        List<Map<String, Object>> candidates = parseAiJsonArray(rawJson, "NAMING");
        if (candidates.isEmpty()) {
            throw new IllegalStateException("AI 브랜드 네이밍 생성 결과가 비어 있습니다.");
        }

        jdbcTemplate.update("DELETE FROM kkumz_name_suggestion WHERE idea_id = ?", ideaId);

        int idx = 0;
        for (Map<String, Object> item : candidates) {
            String brandName = String.valueOf(item.getOrDefault("brandName", item.getOrDefault("brand_name", ""))).trim();
            String rawSlug = String.valueOf(item.getOrDefault("siteSlug", item.getOrDefault("site_slug", ""))).trim();
            if (brandName.isBlank() || rawSlug.isBlank()) {
                continue;
            }
            String slug = normalizeSlug(rawSlug);
            String pronunciation = String.valueOf(item.getOrDefault("pronunciation", brandName)).trim();
            String story = String.valueOf(item.getOrDefault("brandStory", item.getOrDefault("brand_story", ""))).trim();
            int score = (item.get("score") instanceof Number num) ? num.intValue() : (98 - idx * 2);

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
                jdbcTemplate.update("""
                        UPDATE kkumz_idea
                        SET selected_brand_name = ?, selected_site_slug = ?, current_step = GREATEST(current_step, 2), updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                        brandName, slug, ideaId
                );
            }
            idx++;
        }

        return getIdeaDetail(ideaId);
    }

    @Transactional
    public Map<String, Object> generateLogoSuggestions(String ideaId, GenerateLogosRequest req) {
        Map<String, Object> idea = getIdeaDetail(ideaId);
        String brandName = (req != null && req.getBrandName() != null && !req.getBrandName().isBlank())
                ? req.getBrandName().trim()
                : String.valueOf(idea.getOrDefault("selectedBrandName", idea.get("title")));
        String stylePreference = (req != null && req.getStylePreference() != null)
                ? req.getStylePreference().trim()
                : "";

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> existingLogos = (List<Map<String, Object>>) idea.get("logoSuggestions");
        if (existingLogos != null && !existingLogos.isEmpty() && stylePreference.isBlank()) {
            return idea;
        }

        String rawJson = ensureAiGateway().executeStagePrompt("LOGO_SVG", Map.of(
                "brandName", brandName,
                "stylePreference", stylePreference
        ));

        List<Map<String, Object>> candidates = parseAiJsonArray(rawJson, "LOGO_SVG");
        if (candidates.isEmpty()) {
            throw new IllegalStateException("AI 벡터 SVG 로고 생성 결과가 비어 있습니다.");
        }

        jdbcTemplate.update("DELETE FROM kkumz_logo_suggestion WHERE idea_id = ?", ideaId);

        int idx = 0;
        for (Map<String, Object> item : candidates) {
            String styleName = String.valueOf(item.getOrDefault("styleName", item.getOrDefault("style_name", ""))).trim();
            String svgCode = String.valueOf(item.getOrDefault("svgCode", item.getOrDefault("svg_code", ""))).trim();
            if (styleName.isBlank() || !svgCode.startsWith("<svg")) {
                continue;
            }
            Object paletteObj = item.getOrDefault("colorPalette", item.getOrDefault("color_palette", Map.of()));
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

    @Transactional
    public Map<String, Object> selectBrand(String ideaId, SelectBrandRequest req) {
        Map<String, Object> idea = getIdeaDetail(ideaId);
        String brandName = req.getBrandName();
        String siteSlug = req.getSiteSlug();
        String logoSvg = req.getLogoSvg();
        Map<String, Object> designTokens = req.getDesignTokens();

        if (req.getNameSuggestionId() != null && !req.getNameSuggestionId().isBlank()) {
            jdbcTemplate.update("UPDATE kkumz_name_suggestion SET is_selected = (id = ?) WHERE idea_id = ?",
                    req.getNameSuggestionId(), ideaId);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT brand_name AS \"brandName\", site_slug AS \"siteSlug\" FROM kkumz_name_suggestion WHERE id = ?",
                    req.getNameSuggestionId()
            );
            if (!rows.isEmpty()) {
                brandName = String.valueOf(rows.get(0).get("brandName"));
                if (siteSlug == null || siteSlug.isBlank()) {
                    siteSlug = String.valueOf(rows.get(0).get("siteSlug"));
                }
            }
        }

        if (req.getLogoSuggestionId() != null && !req.getLogoSuggestionId().isBlank()) {
            jdbcTemplate.update("UPDATE kkumz_logo_suggestion SET is_selected = (id = ?) WHERE idea_id = ?",
                    req.getLogoSuggestionId(), ideaId);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT svg_code AS \"svgCode\", color_palette_json::text AS \"palette\" FROM kkumz_logo_suggestion WHERE id = ?",
                    req.getLogoSuggestionId()
            );
            if (!rows.isEmpty()) {
                logoSvg = String.valueOf(rows.get(0).get("svgCode"));
                if (designTokens == null || designTokens.isEmpty()) {
                    designTokens = parseJsonObject(String.valueOf(rows.get(0).get("palette")));
                }
            }
        }

        String finalBrand = (brandName != null && !brandName.isBlank())
                ? brandName.trim()
                : String.valueOf(idea.getOrDefault("selectedBrandName", idea.get("title")));
        String finalSlug = (siteSlug != null && !siteSlug.isBlank())
                ? normalizeSlug(siteSlug)
                : String.valueOf(idea.getOrDefault("selectedSiteSlug", "site-" + ideaId));
        String finalSvg = (logoSvg != null && !logoSvg.isBlank())
                ? logoSvg
                : String.valueOf(idea.getOrDefault("selectedLogoSvg", ""));
        String finalTokensJson = (designTokens != null && !designTokens.isEmpty())
                ? toJson(designTokens)
                : toJson(idea.get("designTokens"));

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

        return getIdeaDetail(ideaId);
    }

    // =========================================================================
    // 3. Step 3 — AI 기능명세서(FRS) & 화면 블루프린트 생성/편집
    // =========================================================================

    @Transactional
    public Map<String, Object> generateSpec(String ideaId) {
        Map<String, Object> idea = getIdeaDetail(ideaId);
        String existingMarkdown = idea.get("specMarkdown") != null ? String.valueOf(idea.get("specMarkdown")) : "";
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> existingScreens = (List<Map<String, Object>>) idea.get("screenSpecs");
        if (!existingMarkdown.isBlank() && existingScreens != null && !existingScreens.isEmpty()) {
            return idea;
        }

        String brandName = idea.get("selectedBrandName") != null
                ? String.valueOf(idea.get("selectedBrandName"))
                : String.valueOf(idea.get("title"));
        String siteType = String.valueOf(idea.get("siteType"));
        String promptText = String.valueOf(idea.get("promptText"));

        String rawOutput = ensureAiGateway().executeStagePrompt("SPEC_FRS", Map.of(
                "brandName", brandName,
                "siteType", siteType,
                "promptText", promptText
        ));

        String markdown = rawOutput;
        List<Map<String, Object>> screens = new ArrayList<>();
        try {
            JsonNode node = objectMapper.readTree(rawOutput);
            if (node.isObject()) {
                if (node.has("specMarkdown")) {
                    markdown = node.get("specMarkdown").asText();
                }
                if (node.has("screens") && node.get("screens").isArray()) {
                    screens = objectMapper.convertValue(node.get("screens"), new TypeReference<List<Map<String, Object>>>() {});
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
            jdbcTemplate.update("DELETE FROM kkumz_screen_spec WHERE idea_id = ?", ideaId);
            int idx = 1;
            for (Map<String, Object> scr : screens) {
                SaveScreenSpecRequest sReq = SaveScreenSpecRequest.builder()
                        .screenKey(String.valueOf(scr.getOrDefault("screenKey", "SCR_" + idx)))
                        .screenTitle(String.valueOf(scr.getOrDefault("title", scr.getOrDefault("screenTitle", "화면 " + idx))))
                        .routePath(String.valueOf(scr.getOrDefault("routePath", idx == 1 ? "/" : "/section-" + idx)))
                        .layoutType(String.valueOf(scr.getOrDefault("layout", scr.getOrDefault("layoutType", "STANDARD_GRID"))))
                        .description(String.valueOf(scr.getOrDefault("description", "")))
                        .sortNo(idx++)
                        .build();
                saveIdeaScreen(ideaId, sReq);
            }
        }

        return getIdeaDetail(ideaId);
    }

    @Transactional
    public Map<String, Object> updateSpec(String ideaId, UpdateSpecRequest req) {
        Map<String, Object> idea = getIdeaDetail(ideaId);
        String markdown = (req.getSpecMarkdown() != null && !req.getSpecMarkdown().isBlank())
                ? req.getSpecMarkdown()
                : String.valueOf(idea.get("specMarkdown"));
        String screensJson = req.getScreens() != null
                ? toJson(req.getScreens())
                : toJson(idea.get("screens"));

        jdbcTemplate.update("""
                UPDATE kkumz_idea
                SET spec_markdown = ?, screens_json = ?::jsonb, current_step = GREATEST(current_step, 4), updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                markdown, screensJson, ideaId
        );

        SaveIdeaVersionRequest verReq = SaveIdeaVersionRequest.builder()
                .title(idea.get("title") + " (기획서 편집본)")
                .promptText(String.valueOf(idea.get("promptText")))
                .specMarkdown(markdown)
                .screens(req.getScreens())
                .createdBy(req.getUpdatedBy())
                .build();
        return saveOrRollbackIdeaVersion(ideaId, verReq);
    }

    // =========================================================================
    // 4. Step 4~6 — 멀티사이트 프로비저닝, WebIDE 편집, AI 빌드 복구 & 배포
    // =========================================================================

    @Transactional
    public Map<String, Object> scaffoldSite(String ideaId) {
        long startNs = System.nanoTime();
        Map<String, Object> idea = getIdeaDetail(ideaId);
        String siteType = String.valueOf(idea.get("siteType"));
        String category = String.valueOf(idea.get("category"));
        String brandName = idea.get("selectedBrandName") != null
                ? String.valueOf(idea.get("selectedBrandName"))
                : String.valueOf(idea.get("title"));
        String siteSlug = idea.get("selectedSiteSlug") != null
                ? String.valueOf(idea.get("selectedSiteSlug"))
                : normalizeSlug(brandName);
        String logoSvg = idea.get("selectedLogoSvg") != null ? String.valueOf(idea.get("selectedLogoSvg")) : "";
        if (logoSvg.isBlank()) {
            throw new IllegalStateException("사이트 스캐폴딩 전 브랜드 로고(selectedLogoSvg) 선택이 완료되어야 합니다: ideaId=" + ideaId);
        }
        String brandSlogan = String.valueOf(idea.get("title"));
        String deployMode = "DYNAMIC_SDUI";

        @SuppressWarnings("unchecked")
        Map<String, Object> tokens = (Map<String, Object>) idea.get("designTokens");
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalStateException("디자인 토큰(designTokens) 정보가 존재하지 않습니다: ideaId=" + ideaId);
        }

        Map<String, Object> layoutSchema = new LinkedHashMap<>();
        layoutSchema.put("heroTitle", brandName);
        layoutSchema.put("heroSubtitle", brandSlogan);
        layoutSchema.put("ctaText", "자세히 보기");
        layoutSchema.put("showFlashDeal", !"CMS_SITE".equalsIgnoreCase(siteType));
        layoutSchema.put("showAiConcierge", true);
        layoutSchema.put("showCmsArticles", !"COMMERCE".equalsIgnoreCase(siteType));
        layoutSchema.put("showCmsForm", !"COMMERCE".equalsIgnoreCase(siteType));
        layoutSchema.put("customBadge", category);

        Long shopSiteId = null;
        Long cmsSiteId = null;
        if ("COMMERCE".equalsIgnoreCase(siteType) || "HYBRID".equalsIgnoreCase(siteType)) {
            shopSiteId = provisionSyncShopSite(siteSlug, brandName, brandSlogan);
        }
        if ("CMS_SITE".equalsIgnoreCase(siteType) || "HYBRID".equalsIgnoreCase(siteType)) {
            cmsSiteId = provisionSyncCmsSite(siteSlug, brandName, brandSlogan);
        }

        List<Map<String, Object>> existingSite = jdbcTemplate.queryForList(
                "SELECT id, active_version AS \"activeVersion\" FROM kkumz_site WHERE site_slug = ?",
                siteSlug
        );

        String siteId;
        int nextVer;
        if (!existingSite.isEmpty()) {
            siteId = String.valueOf(existingSite.get(0).get("id"));
            nextVer = ((Number) existingSite.get(0).get("activeVersion")).intValue() + 1;
            jdbcTemplate.update("""
                    UPDATE kkumz_site
                    SET site_name = ?, site_type = ?, category = ?, brand_slogan = ?, logo_svg = ?,
                        shop_site_id = COALESCE(?, shop_site_id), cms_site_id = COALESCE(?, cms_site_id),
                        deploy_mode = ?, active_version = ?, status = 'ACTIVE', updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                    brandName, siteType, category, brandSlogan, logoSvg,
                    shopSiteId, cmsSiteId, deployMode, nextVer, siteId
            );
        } else {
            siteId = "site-" + UUID.randomUUID().toString().substring(0, 8);
            nextVer = 1;
            jdbcTemplate.update("""
                    INSERT INTO kkumz_site (
                        id, idea_id, site_slug, site_name, site_type, category, brand_slogan, logo_svg,
                        shop_site_id, cms_site_id, deploy_mode, active_version, deployed_url, custom_domain,
                        is_featured, visibility, like_count, comment_count, view_count,
                        owner_username, owner_display_name, status, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, FALSE, 'PUBLIC', 0, 0, 0, ?, ?, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """,
                    siteId, ideaId, siteSlug, brandName, siteType, category, brandSlogan, logoSvg,
                    shopSiteId, cmsSiteId, deployMode,
                    "/s/" + siteSlug, siteSlug + ".kkumz.io",
                    idea.get("authorUsername"), idea.get("authorDisplayName")
            );
        }

        List<Map<String, Object>> screenSpecs = listIdeaScreens(ideaId);
        List<Map<String, Object>> webideFiles = buildWebIdeFilesFromScreens(brandName, tokens, layoutSchema, screenSpecs);
        String releaseId = "rel-" + siteId + "-v" + nextVer;
        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        long durationMs = Math.max(1L, (System.nanoTime() - startNs) / 1_000_000L);
        String buildLog = "[" + nowTime + "] [Provision] Site slug /s/" + siteSlug + " initialized (v" + nextVer + ", " + durationMs + "ms).";
        String bundlePath = "/s/" + siteSlug + "/releases/v" + nextVer;

        jdbcTemplate.update("""
                INSERT INTO kkumz_site_release (
                    id, site_id, version_no, release_note, deploy_mode, design_tokens_json,
                    layout_schema_json, webide_files_json, minio_bundle_path, build_status, build_log, created_by, created_at
                ) VALUES (?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, 'SUCCESS', ?, ?, CURRENT_TIMESTAMP)
                """,
                releaseId, siteId, nextVer,
                "멀티사이트 프로비저닝 및 배포 (v" + nextVer + ")",
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
                SET current_step = 6, status = 'PUBLISHED', selected_site_slug = ?, selected_brand_name = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                siteSlug, brandName, ideaId
        );

        return getSiteRuntime(siteSlug);
    }

    @Transactional
    public Map<String, Object> chatRefineSite(String siteIdOrSlug, ChatRefineRequest req) {
        long startNs = System.nanoTime();
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
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
                ls.forEach((k, v) -> layout.put(String.valueOf(k), v));
            }
            for (String tokenKey : List.of("primary", "secondary", "accent", "surface", "radius")) {
                if (parsed.containsKey(tokenKey)) {
                    tokens.put(tokenKey, parsed.get(tokenKey));
                }
            }
            for (String layoutKey : List.of("heroTitle", "heroSubtitle", "ctaText", "customBadge", "showFlashDeal", "showAiConcierge")) {
                if (parsed.containsKey(layoutKey)) {
                    layout.put(layoutKey, parsed.get(layoutKey));
                }
            }
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

        jdbcTemplate.update("UPDATE kkumz_site SET active_version = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                nextVer, siteId);

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

        Map<String, Object> workspace = new LinkedHashMap<>();
        workspace.put("site", site);
        workspace.put("versionNo", rel.get("versionNo"));
        workspace.put("deployMode", rel.get("deployMode"));
        workspace.put("buildStatus", rel.get("buildStatus"));
        workspace.put("buildLog", rel.get("buildLog"));
        workspace.put("designTokens", parseJsonObject(String.valueOf(rel.get("designTokensJson"))));
        workspace.put("layoutSchema", parseJsonObject(String.valueOf(rel.get("layoutSchemaJson"))));
        workspace.put("files", parseJsonList(String.valueOf(rel.get("webideFilesJson"))));
        workspace.put("buildJobs", jobs);
        return workspace;
    }

    @Transactional
    public Map<String, Object> updateWebIdeFiles(String siteIdOrSlug, UpdateWebIdeFilesRequest req) {
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
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));
        int currentVer = ((Number) site.get("activeVersion")).intValue();

        String deployMode = (req != null && req.getDeployMode() != null && !req.getDeployMode().isBlank())
                ? req.getDeployMode().trim().toUpperCase(Locale.ROOT)
                : String.valueOf(site.get("deployMode"));
        String visibility = (req != null && req.getVisibility() != null && !req.getVisibility().isBlank())
                ? req.getVisibility().trim().toUpperCase(Locale.ROOT)
                : String.valueOf(site.get("visibility"));

        jdbcTemplate.update("""
                UPDATE kkumz_site
                SET deploy_mode = ?, visibility = ?, status = 'ACTIVE', updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                deployMode, visibility, siteId
        );

        createNotification(
                String.valueOf(site.get("ownerUsername")),
                "BUILD_DONE",
                site.get("siteName") + " v" + currentVer + " 라이브 배포 완료",
                "멀티사이트 런타임(/s/" + siteSlug + ")에 최신 릴리즈가 반영되었습니다.",
                "/s/" + siteSlug
        );

        return getSiteRuntime(siteSlug);
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
        int activeVer = ((Number) site.get("activeVersion")).intValue();

        jdbcTemplate.update("UPDATE kkumz_site SET view_count = view_count + 1 WHERE id = ?", siteId);

        Map<String, Object> activeRel = getLatestRelease(siteId, activeVer);
        List<Map<String, Object>> allReleases = jdbcTemplate.queryForList("""
                SELECT id, version_no AS "versionNo", release_note AS "releaseNote", deploy_mode AS "deployMode",
                       build_status AS "buildStatus", created_by AS "createdBy", created_at AS "createdAt"
                FROM kkumz_site_release
                WHERE site_id = ?
                ORDER BY version_no DESC
                """, siteId);

        Map<String, Object> runtime = new LinkedHashMap<>();
        runtime.put("site", site);
        runtime.put("activeVersion", activeVer);
        runtime.put("deployMode", activeRel.get("deployMode"));
        runtime.put("releaseNote", activeRel.get("releaseNote"));
        runtime.put("designTokens", parseJsonObject(String.valueOf(activeRel.get("designTokensJson"))));
        runtime.put("layoutSchema", parseJsonObject(String.valueOf(activeRel.get("layoutSchemaJson"))));
        runtime.put("webideFiles", parseJsonList(String.valueOf(activeRel.get("webideFilesJson"))));
        runtime.put("releases", allReleases);
        runtime.put("screenSpecs", listIdeaScreens(ideaId));

        if ("COMMERCE".equalsIgnoreCase(siteType) || "HYBRID".equalsIgnoreCase(siteType)) {
            Long shopSiteId = site.get("shopSiteId") != null ? ((Number) site.get("shopSiteId")).longValue() : null;
            runtime.put("commerce", loadCommerceData(shopSiteId));
        }

        if ("CMS_SITE".equalsIgnoreCase(siteType) || "HYBRID".equalsIgnoreCase(siteType)) {
            Long cmsSiteId = site.get("cmsSiteId") != null ? ((Number) site.get("cmsSiteId")).longValue() : null;
            runtime.put("cms", loadCmsData(cmsSiteId));
        }

        runtime.put("recruits", listSiteRecruits(siteId));
        runtime.put("teamMembers", listSiteTeamMembers(siteId));
        runtime.put("comments", listSiteComments(siteId));

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
        if (shopJdbcTemplate == null) {
            throw new IllegalStateException("SyncShop 데이터베이스 연결(shopJdbcTemplate)이 초기화되지 않았습니다.");
        }

        Map<String, Object> site = findSiteByIdOrSlug(siteSlug);
        Long shopSiteId = site.get("shopSiteId") != null ? ((Number) site.get("shopSiteId")).longValue() : null;
        if (shopSiteId == null) {
            throw new IllegalStateException("해당 사이트에 매핑된 SyncShop 스토어 ID(shopSiteId)가 없습니다: " + siteSlug);
        }

        int quantity = (req.getQuantity() != null && req.getQuantity() > 0) ? req.getQuantity() : 1;
        String receiverName = req.getReceiverName().trim();
        String receiverPhone = req.getReceiverPhone().trim();
        String receiverAddress = req.getReceiverAddress().trim();
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
        if (cmsJdbcTemplate == null) {
            throw new IllegalStateException("SyncCMS 데이터베이스 연결(cmsJdbcTemplate)이 초기화되지 않았습니다.");
        }

        Map<String, Object> site = findSiteByIdOrSlug(siteSlug);
        Long cmsSiteId = site.get("cmsSiteId") != null ? ((Number) site.get("cmsSiteId")).longValue() : null;
        if (cmsSiteId == null) {
            throw new IllegalStateException("해당 사이트에 매핑된 SyncCMS 사이트 ID(cmsSiteId)가 없습니다: " + siteSlug);
        }
        Long formId = req.getFormId();
        if (formId == null) {
            List<Map<String, Object>> forms = cmsJdbcTemplate.queryForList(
                    "SELECT id FROM cms_form WHERE site_id = ? AND status = 'ACTIVE' ORDER BY id ASC LIMIT 1",
                    cmsSiteId
            );
            if (forms.isEmpty()) {
                throw new IllegalStateException("해당 사이트에 활성화된 CMS 신청 폼이 존재하지 않습니다: cmsSiteId=" + cmsSiteId);
            }
            formId = ((Number) forms.get(0).get("id")).longValue();
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        if (req.getExtraFields() != null) {
            payload.putAll(req.getExtraFields());
        }
        payload.put("applicant_name", applicant);
        payload.put("phone", phone);
        if (req.getOptionValue() != null && !req.getOptionValue().isBlank()) {
            payload.put("option_value", req.getOptionValue().trim());
        }
        if (req.getMessage() != null && !req.getMessage().isBlank()) {
            payload.put("message", req.getMessage().trim());
        }

        Long submissionId = cmsJdbcTemplate.queryForObject("""
                INSERT INTO cms_form_submission (
                    form_id, site_id, submitted_data, submitter_ip, status, created_time
                ) VALUES (?, ?, ?::jsonb, '0.0.0.0', 'RECEIVED', CURRENT_TIMESTAMP)
                RETURNING id
                """,
                Long.class,
                formId, cmsSiteId, toJson(payload)
        );

        Integer cnt = cmsJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cms_form_submission WHERE form_id = ?",
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
        String question = req.getQuestion().trim();

        String answer = ensureAiGateway().executeStagePrompt("CONCIERGE", Map.of(
                "siteName", siteName,
                "brandSlogan", slogan,
                "question", question
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
                WHERE s.status = 'ACTIVE' AND s.visibility = 'PUBLIC'
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

        List<Map<String, Object>> sites = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        for (Map<String, Object> item : sites) {
            String sId = String.valueOf(item.get("id"));
            item.put("tags", parseJsonList(String.valueOf(item.remove("tagsJson"))));
            item.put("designTokens", parseJsonObject(String.valueOf(item.remove("designTokensJson"))));
            item.put("openRecruits", listSiteRecruits(sId));
            item.put("teamMembers", listSiteTeamMembers(sId));
            item.put("recentComments", listSiteComments(sId));
        }
        return sites;
    }

    @Transactional
    public Map<String, Object> toggleSiteLike(String siteIdOrSlug, String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("좋아요 처리를 위한 사용자 계정(username)은 필수입니다.");
        }
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String user = username.trim();

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
        String siteId = String.valueOf(site.get("id"));
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
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        Map<String, String> author = resolveUserIdentity(req.getUsername(), req.getDisplayName());
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
        return listSiteComments(siteId);
    }

    public List<Map<String, Object>> listSiteRecruits(String siteIdOrSlug) {
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
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

        return listSiteRecruits(siteId);
    }

    public List<Map<String, Object>> listSiteTeamMembers(String siteIdOrSlug) {
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
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
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        Map<String, String> applicant = resolveUserIdentity(req.getUsername(), req.getDisplayName());
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

        return listSiteTeamMembers(siteId);
    }

    @Transactional
    public List<Map<String, Object>> inviteTeamMember(String siteIdOrSlug, TeamInviteRequest req) {
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

        return listSiteTeamMembers(siteId);
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

        return listSiteTeamMembers(siteId);
    }

    // =========================================================================
    // 7. 사용자 인앱 알림 센터
    // =========================================================================

    public List<Map<String, Object>> listNotifications(String username) {
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
        if (username != null && !username.isBlank()) {
            sql.append(" WHERE recipient_username = ?");
            params.add(username.trim());
        }
        sql.append(" ORDER BY created_at DESC LIMIT 30");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Transactional
    public void markAllNotificationsRead(String username) {
        if (username != null && !username.isBlank()) {
            jdbcTemplate.update("UPDATE kkumz_notification SET is_read = TRUE WHERE recipient_username = ?", username.trim());
        } else {
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
        List<Map<String, Object>> recentIdeas = listIdeas(null, null);
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
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Transactional
    public Map<String, Object> updateAdminSiteStatus(String siteId, UpdateSiteStatusRequest req) {
        Map<String, Object> site = findSiteByIdOrSlug(siteId);
        String resolvedId = String.valueOf(site.get("id"));

        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            jdbcTemplate.update("UPDATE kkumz_site SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    req.getStatus().trim().toUpperCase(Locale.ROOT), resolvedId);
        }
        if (req.getVisibility() != null && !req.getVisibility().isBlank()) {
            jdbcTemplate.update("UPDATE kkumz_site SET visibility = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    req.getVisibility().trim().toUpperCase(Locale.ROOT), resolvedId);
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
        if (req.getReporterUsername() == null || req.getReporterUsername().isBlank()) {
            throw new IllegalArgumentException("신고자 계정(reporterUsername)은 필수입니다.");
        }
        Map<String, Object> site = findSiteByIdOrSlug(siteIdOrSlug);
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
                req.getReporterUsername().trim(),
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
    // 9. Cross-DB (SyncShop / SyncCMS) 실데이터 조회 및 프로비저닝 헬퍼
    // =========================================================================

    private Map<String, Object> loadCommerceData(Long shopSiteId) {
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
                       product_category_name AS "categoryName",
                       price,
                       promotion_price AS "promotionPrice",
                       original_price AS "originalPrice",
                       stock,
                       sale,
                       unit,
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
        if (cmsSiteId == null || cmsJdbcTemplate == null) {
            return Map.of(
                    "cmsSiteId", 0L,
                    "articles", List.of(),
                    "banners", List.of(),
                    "forms", List.of()
            );
        }
        List<Map<String, Object>> articles = cmsJdbcTemplate.queryForList("""
                SELECT id,
                       site_id AS "siteId",
                       title,
                       summary,
                       content_html AS "contentHtml",
                       thumbnail_url AS "thumbnailUrl",
                       author_name AS "authorName",
                       view_count AS "viewCount",
                       published_time AS "publishedTime"
                FROM cms_article
                WHERE site_id = ? AND status = 'PUBLISHED' AND del_flag = 0
                ORDER BY is_top DESC, published_time DESC, id ASC
                """, cmsSiteId);

        List<Map<String, Object>> banners = cmsJdbcTemplate.queryForList("""
                SELECT id, title, image_url AS "imageUrl", link_url AS "linkUrl", position
                FROM cms_banner
                WHERE site_id = ? AND status = 1 AND del_flag = 0
                ORDER BY sort_order ASC, id ASC
                """, cmsSiteId);

        List<Map<String, Object>> forms = cmsJdbcTemplate.queryForList("""
                SELECT id, form_name AS "formName", form_code AS "formCode", description, fields_json::text AS "fieldsJson"
                FROM cms_form
                WHERE site_id = ? AND status = 'ACTIVE' AND del_flag = 0
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
        if (shopJdbcTemplate == null) {
            return null;
        }
        List<Map<String, Object>> existing = shopJdbcTemplate.queryForList(
                "SELECT id FROM ums_site WHERE site_code = ?", siteSlug
        );
        if (!existing.isEmpty()) {
            return ((Number) existing.get(0).get("id")).longValue();
        }
        return shopJdbcTemplate.queryForObject("""
                INSERT INTO ums_site (site_code, site_name, domain_url, theme_code, description, status)
                VALUES (?, ?, ?, 'modern-studio', ?, 1)
                RETURNING id
                """,
                Long.class,
                siteSlug, brandName, siteSlug + ".kkumz.io", slogan
        );
    }

    private Long provisionSyncCmsSite(String siteSlug, String brandName, String slogan) {
        if (cmsJdbcTemplate == null) {
            return null;
        }
        List<Map<String, Object>> existing = cmsJdbcTemplate.queryForList(
                "SELECT id FROM cms_site WHERE site_code = ?", siteSlug
        );
        if (!existing.isEmpty()) {
            return ((Number) existing.get(0).get("id")).longValue();
        }
        return cmsJdbcTemplate.queryForObject("""
                INSERT INTO cms_site (site_code, site_name, domain, description, theme_name, status, create_by)
                VALUES (?, ?, ?, ?, 'kkumz-editorial', 1, 'admin')
                RETURNING id
                """,
                Long.class,
                siteSlug, brandName, siteSlug + ".kkumz.io", slogan
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

    private List<Map<String, Object>> parseAiJsonArray(String rawJson, String stageName) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            if (root.isArray()) {
                return objectMapper.convertValue(root, new TypeReference<List<Map<String, Object>>>() {});
            }
            if (root.isObject()) {
                for (String key : List.of("items", "candidates", "suggestions", "files", "data", "result")) {
                    if (root.has(key) && root.get(key).isArray()) {
                        return objectMapper.convertValue(root.get(key), new TypeReference<List<Map<String, Object>>>() {});
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
        String appVue = "<template>\n  <div class=\"kkumz-live-shell\">\n    <header class=\"brand-hero\">\n      <h1>{{ layout.heroTitle }}</h1>\n      <p>{{ layout.heroSubtitle }}</p>\n    </header>\n"
                + sectionMarkup
                + "  </div>\n</template>\n<script setup lang=\"ts\">\ndefineProps<{ tokens: Record<string, any>; layout: Record<string, any> }>();\n</script>";

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
        row.put("tags", parseJsonList(String.valueOf(row.remove("tagsJson"))));
        row.put("designTokens", parseJsonObject(String.valueOf(row.remove("designTokensJson"))));
        row.put("screens", parseJsonList(String.valueOf(row.remove("screensJson"))));
    }

    private String summarizeTitle(String promptText) {
        String cleaned = promptText.replaceAll("\\s+", " ").trim();
        return cleaned.length() > 42 ? cleaned.substring(0, 42) + "..." : cleaned;
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
}
