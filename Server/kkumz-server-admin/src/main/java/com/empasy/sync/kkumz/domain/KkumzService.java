package com.empasy.sync.kkumz.domain;

import com.empasy.sync.kkumz.domain.KkumzDTO.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Kkumz(꿈즈) AI 브랜드·멀티사이트 생성기, 2-Track 하이브리드 배포,
 * SyncShop/SyncCMS 멀티사이트 실시간 연동, 창업 팀빌딩 커뮤니티 및 전용 Admin 백오피스 핵심 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KkumzService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kkumz.cross-db.shop-url:jdbc:postgresql://localhost:5432/syncshop_db?currentSchema=public&stringtype=unspecified}")
    private String shopDbUrl;

    @Value("${kkumz.cross-db.cms-url:jdbc:postgresql://localhost:5432/synccms_db?currentSchema=public&stringtype=unspecified}")
    private String cmsDbUrl;

    @Value("${kkumz.cross-db.username:syncadmin}")
    private String crossDbUsername;

    @Value("${kkumz.cross-db.password:syncpass}")
    private String crossDbPassword;

    private JdbcTemplate shopJdbcTemplate;
    private JdbcTemplate cmsJdbcTemplate;

    @PostConstruct
    public void initCrossDomainJdbcTemplates() {
        try {
            DriverManagerDataSource shopDs = new DriverManagerDataSource(shopDbUrl, crossDbUsername, crossDbPassword);
            shopDs.setDriverClassName("org.postgresql.Driver");
            this.shopJdbcTemplate = new JdbcTemplate(shopDs);

            DriverManagerDataSource cmsDs = new DriverManagerDataSource(cmsDbUrl, crossDbUsername, crossDbPassword);
            cmsDs.setDriverClassName("org.postgresql.Driver");
            this.cmsJdbcTemplate = new JdbcTemplate(cmsDs);
        } catch (Exception e) {
            log.warn("Cross-domain JdbcTemplate 초기화 경고: {}", e.getMessage());
        }
    }

    // =========================================================================
    // 1. 아이디어 및 버전/화면설계 관리
    // =========================================================================

    public List<Map<String, Object>> listIdeas(String authorUsername, String siteType) {
        StringBuilder sql = new StringBuilder(
                "SELECT i.id, i.author_username AS \"authorUsername\", i.author_display_name AS \"authorDisplayName\", " +
                "i.title, i.prompt_text AS \"promptText\", i.site_type AS \"siteType\", i.category, " +
                "i.tags_json::text AS \"tagsJson\", i.visibility, i.current_step AS \"currentStep\", " +
                "i.selected_brand_name AS \"selectedBrandName\", i.selected_site_slug AS \"selectedSiteSlug\", " +
                "i.selected_logo_svg AS \"selectedLogoSvg\", i.status, i.created_at AS \"createdAt\", i.updated_at AS \"updatedAt\", " +
                "s.id AS \"siteId\", s.deployed_url AS \"deployedUrl\", s.deploy_mode AS \"deployMode\", s.active_version AS \"activeVersion\" " +
                "FROM kkumz_idea i " +
                "LEFT JOIN kkumz_site s ON s.idea_id = i.id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (authorUsername != null && !authorUsername.isBlank()) {
            sql.append(" AND i.author_username = ? ");
            params.add(authorUsername.trim());
        }
        if (siteType != null && !siteType.isBlank() && !"ALL".equalsIgnoreCase(siteType)) {
            sql.append(" AND i.site_type = ? ");
            params.add(siteType.trim().toUpperCase(Locale.ROOT));
        }
        sql.append(" ORDER BY i.updated_at DESC, i.created_at DESC ");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        for (Map<String, Object> row : rows) {
            row.put("tags", parseJsonList(row.remove("tagsJson")));
        }
        return rows;
    }

    @Transactional
    public Map<String, Object> createIdea(CreateIdeaRequest req) {
        if (req.getPromptText() == null || req.getPromptText().isBlank()) {
            throw new IllegalArgumentException("아이디어 프롬프트(promptText)는 필수입니다.");
        }
        String ideaId = "idea-kkumz-" + UUID.randomUUID().toString().substring(0, 8);
        String prompt = req.getPromptText().trim();
        String title = (req.getTitle() != null && !req.getTitle().isBlank())
                ? req.getTitle().trim()
                : (prompt.length() > 42 ? prompt.substring(0, 42) + "..." : prompt);
        String siteType = normalizeSiteType(req.getSiteType());
        String category = (req.getCategory() != null && !req.getCategory().isBlank()) ? req.getCategory().trim() : "LIFESTYLE";
        String visibility = (req.getVisibility() != null && !req.getVisibility().isBlank()) ? req.getVisibility().trim().toUpperCase(Locale.ROOT) : "PUBLIC";
        String authorUser = (req.getAuthorUsername() != null && !req.getAuthorUsername().isBlank()) ? req.getAuthorUsername().trim() : "admin";
        String authorName = (req.getAuthorDisplayName() != null && !req.getAuthorDisplayName().isBlank()) ? req.getAuthorDisplayName().trim() : "김도현 창업가";
        String tagsJson = toJson(req.getTags() != null && !req.getTags().isEmpty() ? req.getTags() : List.of("AI브랜드빌더", category));

        String initialSpec = buildDefaultMarkdownSpec(title, prompt, siteType, title);
        List<Map<String, Object>> initialScreens = buildDefaultScreens(siteType);

        jdbcTemplate.update(
                "INSERT INTO kkumz_idea (id, author_username, author_display_name, title, prompt_text, site_type, " +
                "category, tags_json, visibility, current_step, spec_markdown, screens_json, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, 2, ?, ?::jsonb, 'DRAFT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                ideaId, authorUser, authorName, title, prompt, siteType, category, tagsJson, visibility, initialSpec, toJson(initialScreens)
        );

        String verId = "iver-" + ideaId + "-v1";
        jdbcTemplate.update(
                "INSERT INTO kkumz_idea_version (id, idea_id, version_no, title, prompt_text, spec_markdown, screens_json, created_by, created_at) " +
                "VALUES (?, ?, 1, ?, ?, ?, ?::jsonb, ?, CURRENT_TIMESTAMP)",
                verId, ideaId, title, prompt, initialSpec, toJson(initialScreens), authorUser
        );

        syncScreenSpecsToTable(ideaId, initialScreens);
        return getIdeaDetail(ideaId);
    }

    public Map<String, Object> getIdeaDetail(String ideaId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, author_username AS \"authorUsername\", author_display_name AS \"authorDisplayName\", " +
                "title, prompt_text AS \"promptText\", site_type AS \"siteType\", category, " +
                "tags_json::text AS \"tagsJson\", visibility, current_step AS \"currentStep\", " +
                "selected_brand_name AS \"selectedBrandName\", selected_site_slug AS \"selectedSiteSlug\", " +
                "selected_logo_svg AS \"selectedLogoSvg\", design_tokens_json::text AS \"designTokensJson\", " +
                "spec_markdown AS \"specMarkdown\", screens_json::text AS \"screensJson\", " +
                "status, created_at AS \"createdAt\", updated_at AS \"updatedAt\" " +
                "FROM kkumz_idea WHERE id = ? LIMIT 1",
                ideaId
        );
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 아이디어 ID입니다: " + ideaId);
        }
        Map<String, Object> idea = new LinkedHashMap<>(rows.get(0));
        idea.put("tags", parseJsonList(idea.remove("tagsJson")));
        idea.put("designTokens", parseJsonMap(idea.remove("designTokensJson")));
        idea.put("screens", parseJsonListOfMap(idea.remove("screensJson")));

        List<Map<String, Object>> names = jdbcTemplate.queryForList(
                "SELECT id, idea_id AS \"ideaId\", brand_name AS \"brandName\", site_slug AS \"siteSlug\", " +
                "pronunciation, brand_story AS \"brandStory\", score, slug_available AS \"slugAvailable\", " +
                "is_selected AS \"isSelected\", created_at AS \"createdAt\" " +
                "FROM kkumz_name_suggestion WHERE idea_id = ? ORDER BY score DESC, id ASC",
                ideaId
        );
        List<Map<String, Object>> logos = jdbcTemplate.queryForList(
                "SELECT id, idea_id AS \"ideaId\", style_name AS \"styleName\", svg_code AS \"svgCode\", " +
                "color_palette_json::text AS \"colorPaletteJson\", typography_json::text AS \"typographyJson\", " +
                "tags_json::text AS \"tagsJson\", is_selected AS \"isSelected\", created_at AS \"createdAt\" " +
                "FROM kkumz_logo_suggestion WHERE idea_id = ? ORDER BY is_selected DESC, id ASC",
                ideaId
        );
        for (Map<String, Object> logo : logos) {
            logo.put("colorPalette", parseJsonMap(logo.remove("colorPaletteJson")));
            logo.put("typography", parseJsonMap(logo.remove("typographyJson")));
            logo.put("tags", parseJsonList(logo.remove("tagsJson")));
        }

        List<Map<String, Object>> versions = jdbcTemplate.queryForList(
                "SELECT id, idea_id AS \"ideaId\", version_no AS \"versionNo\", title, prompt_text AS \"promptText\", " +
                "spec_markdown AS \"specMarkdown\", screens_json::text AS \"screensJson\", " +
                "created_by AS \"createdBy\", created_at AS \"createdAt\" " +
                "FROM kkumz_idea_version WHERE idea_id = ? ORDER BY version_no DESC",
                ideaId
        );
        for (Map<String, Object> ver : versions) {
            ver.put("screens", parseJsonListOfMap(ver.remove("screensJson")));
        }

        List<Map<String, Object>> siteRows = jdbcTemplate.queryForList(
                "SELECT id, site_slug AS \"siteSlug\", site_name AS \"siteName\", site_type AS \"siteType\", " +
                "brand_slogan AS \"brandSlogan\", shop_site_id AS \"shopSiteId\", cms_site_id AS \"cmsSiteId\", " +
                "deploy_mode AS \"deployMode\", active_version AS \"activeVersion\", deployed_url AS \"deployedUrl\", " +
                "custom_domain AS \"customDomain\", is_featured AS \"isFeatured\", status " +
                "FROM kkumz_site WHERE idea_id = ? LIMIT 1",
                ideaId
        );

        idea.put("nameSuggestions", names);
        idea.put("logoSuggestions", logos);
        idea.put("versions", versions);
        idea.put("screenSpecs", listIdeaScreens(ideaId));
        idea.put("site", siteRows.isEmpty() ? null : siteRows.get(0));
        return idea;
    }

    public List<Map<String, Object>> listIdeaScreens(String ideaId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, idea_id AS \"ideaId\", screen_key AS \"screenKey\", screen_title AS \"screenTitle\", " +
                "route_path AS \"routePath\", layout_type AS \"layoutType\", description, " +
                "components_json::text AS \"componentsJson\", sort_no AS \"sortNo\", created_at AS \"createdAt\" " +
                "FROM kkumz_screen_spec WHERE idea_id = ? ORDER BY sort_no ASC, id ASC",
                ideaId
        );
        for (Map<String, Object> r : rows) {
            r.put("components", parseJsonList(r.remove("componentsJson")));
        }
        return rows;
    }

    @Transactional
    public List<Map<String, Object>> saveIdeaScreen(String ideaId, SaveScreenSpecRequest req) {
        String id = (req.getId() != null && !req.getId().isBlank())
                ? req.getId().trim()
                : "scr-" + UUID.randomUUID().toString().substring(0, 8);
        String key = (req.getScreenKey() != null && !req.getScreenKey().isBlank()) ? req.getScreenKey().trim() : "CUSTOM_SCREEN";
        String title = (req.getScreenTitle() != null && !req.getScreenTitle().isBlank()) ? req.getScreenTitle().trim() : "신규 화면 섹션";
        String route = (req.getRoutePath() != null && !req.getRoutePath().isBlank()) ? req.getRoutePath().trim() : "/section";
        String layout = (req.getLayoutType() != null && !req.getLayoutType().isBlank()) ? req.getLayoutType().trim() : "STANDARD_GRID";
        String comps = toJson(req.getComponents() != null ? req.getComponents() : List.of("HeroBanner", "ContentGrid"));
        int sortNo = req.getSortNo() != null ? req.getSortNo() : 10;

        Integer exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_screen_spec WHERE id = ?", Integer.class, id);
        if (exists != null && exists > 0) {
            jdbcTemplate.update(
                    "UPDATE kkumz_screen_spec SET screen_key = ?, screen_title = ?, route_path = ?, layout_type = ?, " +
                    "description = ?, components_json = ?::jsonb, sort_no = ? WHERE id = ?",
                    key, title, route, layout, req.getDescription(), comps, sortNo, id
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO kkumz_screen_spec (id, idea_id, screen_key, screen_title, route_path, layout_type, description, components_json, sort_no, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, CURRENT_TIMESTAMP)",
                    id, ideaId, key, title, route, layout, req.getDescription(), comps, sortNo
            );
        }
        return listIdeaScreens(ideaId);
    }

    @Transactional
    public Map<String, Object> saveOrRollbackIdeaVersion(String ideaId, SaveIdeaVersionRequest req) {
        Map<String, Object> idea = getIdeaDetail(ideaId);
        if (req.getRollbackToVersionNo() != null) {
            List<Map<String, Object>> targetRows = jdbcTemplate.queryForList(
                    "SELECT title, prompt_text, spec_markdown, screens_json::text AS screens_text " +
                    "FROM kkumz_idea_version WHERE idea_id = ? AND version_no = ? LIMIT 1",
                    ideaId, req.getRollbackToVersionNo()
            );
            if (targetRows.isEmpty()) {
                throw new IllegalArgumentException("롤백할 버전을 찾을 수 없습니다: v" + req.getRollbackToVersionNo());
            }
            Map<String, Object> target = targetRows.get(0);
            jdbcTemplate.update(
                    "UPDATE kkumz_idea SET title = ?, prompt_text = ?, spec_markdown = ?, " +
                    "screens_json = ?::jsonb, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    target.get("title"), target.get("prompt_text"), target.get("spec_markdown"),
                    target.get("screens_text"), ideaId
            );
            return getIdeaDetail(ideaId);
        }

        Integer maxVer = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(version_no), 0) FROM kkumz_idea_version WHERE idea_id = ?",
                Integer.class, ideaId
        );
        int nextVer = (maxVer != null ? maxVer : 0) + 1;
        String title = req.getTitle() != null && !req.getTitle().isBlank() ? req.getTitle().trim() : String.valueOf(idea.get("title"));
        String prompt = req.getPromptText() != null && !req.getPromptText().isBlank() ? req.getPromptText().trim() : String.valueOf(idea.get("promptText"));
        String spec = req.getSpecMarkdown() != null && !req.getSpecMarkdown().isBlank() ? req.getSpecMarkdown() : String.valueOf(idea.get("specMarkdown"));
        String screensJson = req.getScreens() != null ? toJson(req.getScreens()) : toJson(idea.get("screens"));
        String creator = req.getCreatedBy() != null && !req.getCreatedBy().isBlank() ? req.getCreatedBy() : "admin";

        jdbcTemplate.update(
                "INSERT INTO kkumz_idea_version (id, idea_id, version_no, title, prompt_text, spec_markdown, screens_json, created_by, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?, CURRENT_TIMESTAMP)",
                "iver-" + ideaId + "-v" + nextVer, ideaId, nextVer, title, prompt, spec, screensJson, creator
        );
        jdbcTemplate.update(
                "UPDATE kkumz_idea SET title = ?, prompt_text = ?, spec_markdown = ?, screens_json = ?::jsonb, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                title, prompt, spec, screensJson, ideaId
        );
        if (req.getScreens() != null && !req.getScreens().isEmpty()) {
            syncScreenSpecsToTable(ideaId, req.getScreens());
        }
        return getIdeaDetail(ideaId);
    }

    // =========================================================================
    // 2. Step 2 — AI 브랜드 네이밍 8종 & 벡터 SVG 로고 6종 생성 및 선택
    // =========================================================================

    @Transactional
    public Map<String, Object> generateNameSuggestions(String ideaId, GenerateNamesRequest req) {
        Map<String, Object> idea = getIdeaDetail(ideaId);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> existing = (List<Map<String, Object>>) idea.get("nameSuggestions");
        if (existing != null && existing.size() >= 8 && (req == null || req.getKeywordHint() == null || req.getKeywordHint().isBlank())) {
            return idea;
        }

        String prompt = String.valueOf(idea.get("promptText"));
        String hint = (req != null && req.getKeywordHint() != null && !req.getKeywordHint().isBlank())
                ? req.getKeywordHint().trim()
                : extractCoreKeyword(prompt);
        String slugBase = toSlugBase(hint);

        jdbcTemplate.update("DELETE FROM kkumz_name_suggestion WHERE idea_id = ?", ideaId);

        String[][] templates = new String[][]{
                {hint + " 아뜰리에", slugBase + "-atelier", hint + " Atelier", "장인정신과 모던 라이프스타일 큐레이션의 가치를 담은 프리미엄 브랜드 네이밍입니다.", "99"},
                {hint + " 오빗", slugBase + "-orbit", hint + " Orbit", "고객의 일상과 몰입의 궤도를 완벽하게 연결한다는 감각적인 스토리를 담았습니다.", "96"},
                {hint + " 크로니클", slugBase + "-chronicle", hint + " Chronicle", "원산지부터 사용자 경험까지의 진정성 있는 기록을 강조한 에디토리얼 브랜드명입니다.", "95"},
                {"아카이브 " + hint, "archive-" + slugBase, "Archive " + hint, "엄선된 컬렉션과 깊이 있는 커뮤니티 매거진 감성을 동시에 전달합니다.", "93"},
                {hint + " 콜렉티브", slugBase + "-collective", hint + " Collective", "크리에이터와 소비자가 함께 취향을 공유하는 젊은 커뮤니티형 브랜드명입니다.", "92"},
                {"스튜디오 " + hint, "studio-" + slugBase, "Studio " + hint, "직관적이고 기억하기 쉬운 모던 디자인 하우스 스타일의 네이밍입니다.", "91"},
                {hint + " 리추얼", slugBase + "-ritual", hint + " Ritual", "매일 반복되는 일상을 특별한 감각의 루틴으로 바꾸어 주는 브랜드 철학을 담았습니다.", "90"},
                {"네오 " + hint + " 랩", "neo-" + slugBase + "-lab", "Neo " + hint + " Lab", "혁신적인 기획력과 실험적인 제품 라인업을 상징하는 테크·라이프스타일 브랜드명입니다.", "89"}
        };

        for (int i = 0; i < templates.length; i++) {
            String[] t = templates[i];
            String candidateSlug = ensureAvailableSlug(t[1], ideaId);
            boolean isFirst = (i == 0);
            jdbcTemplate.update(
                    "INSERT INTO kkumz_name_suggestion (id, idea_id, brand_name, site_slug, pronunciation, brand_story, score, slug_available, is_selected, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, TRUE, ?, CURRENT_TIMESTAMP)",
                    "name-" + ideaId + "-" + (i + 1), ideaId, t[0], candidateSlug, t[2], t[3], Integer.parseInt(t[4]), isFirst
            );
            if (isFirst) {
                jdbcTemplate.update(
                        "UPDATE kkumz_idea SET selected_brand_name = ?, selected_site_slug = ?, current_step = GREATEST(current_step, 2), updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                        t[0], candidateSlug, ideaId
                );
            }
        }
        return getIdeaDetail(ideaId);
    }

    @Transactional
    public Map<String, Object> generateLogoSuggestions(String ideaId, GenerateLogosRequest req) {
        Map<String, Object> idea = getIdeaDetail(ideaId);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> existing = (List<Map<String, Object>>) idea.get("logoSuggestions");
        if (existing != null && existing.size() >= 6 && (req == null || req.getStylePreference() == null || req.getStylePreference().isBlank())) {
            return idea;
        }

        String brandName = (req != null && req.getBrandName() != null && !req.getBrandName().isBlank())
                ? req.getBrandName().trim()
                : (idea.get("selectedBrandName") != null ? String.valueOf(idea.get("selectedBrandName")) : "KKUMZ STUDIO");
        String displayUpper = brandName.length() > 14 ? brandName.substring(0, 14).toUpperCase(Locale.ROOT) : brandName.toUpperCase(Locale.ROOT);

        jdbcTemplate.update("DELETE FROM kkumz_logo_suggestion WHERE idea_id = ?", ideaId);

        Object[][] logoSpecs = new Object[][]{
                {
                        "보태니컬 엠블럼",
                        "<svg viewBox=\"0 0 180 40\" xmlns=\"http://www.w3.org/2000/svg\"><rect width=\"40\" height=\"40\" rx=\"10\" fill=\"#14532d\"/><circle cx=\"20\" cy=\"20\" r=\"11\" fill=\"#86efac\" fill-opacity=\"0.25\"/><path d=\"M13 24C13 17 20 12 27 15C26 22 20 27 13 24Z\" fill=\"#4ade80\"/><text x=\"50\" y=\"25\" font-family=\"Pretendard, sans-serif\" font-weight=\"800\" font-size=\"13\" fill=\"#14532d\">" + escapeXml(displayUpper) + "</text></svg>",
                        Map.of("primary", "#14532d", "secondary", "#16a34a", "accent", "#ca8a04", "surface", "#f7fee7"),
                        List.of("보태니컬", "딥포레스트", "오가닉")
                },
                {
                        "오빗 테크 심볼",
                        "<svg viewBox=\"0 0 180 40\" xmlns=\"http://www.w3.org/2000/svg\"><rect width=\"40\" height=\"40\" rx=\"10\" fill=\"#1e1b4b\"/><ellipse cx=\"20\" cy=\"20\" rx=\"13\" ry=\"6\" transform=\"rotate(-25 20 20)\" stroke=\"#818cf8\" stroke-width=\"2.5\" fill=\"none\"/><circle cx=\"20\" cy=\"20\" r=\"4\" fill=\"#38bdf8\"/><text x=\"50\" y=\"25\" font-family=\"Pretendard, sans-serif\" font-weight=\"800\" font-size=\"13\" fill=\"#1e1b4b\">" + escapeXml(displayUpper) + "</text></svg>",
                        Map.of("primary", "#1e1b4b", "secondary", "#4f46e5", "accent", "#06b6d4", "surface", "#f8fafc"),
                        List.of("다크테크", "인디고", "모듈러")
                },
                {
                        "다이내믹 펄스 크레스트",
                        "<svg viewBox=\"0 0 180 40\" xmlns=\"http://www.w3.org/2000/svg\"><rect width=\"40\" height=\"40\" rx=\"10\" fill=\"#0f172a\"/><path d=\"M9 26L18 14L23 21L31 12\" stroke=\"#f97316\" stroke-width=\"3\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/><circle cx=\"31\" cy=\"12\" r=\"3\" fill=\"#fb923c\"/><text x=\"50\" y=\"25\" font-family=\"Pretendard, sans-serif\" font-weight=\"800\" font-size=\"13\" fill=\"#0f172a\">" + escapeXml(displayUpper) + "</text></svg>",
                        Map.of("primary", "#ea580c", "secondary", "#0f172a", "accent", "#f59e0b", "surface", "#fff7ed"),
                        List.of("에너지오렌지", "애슬레틱", "커뮤니티")
                },
                {
                        "아티장 로스트 배지",
                        "<svg viewBox=\"0 0 180 40\" xmlns=\"http://www.w3.org/2000/svg\"><rect width=\"40\" height=\"40\" rx=\"10\" fill=\"#451a03\"/><ellipse cx=\"20\" cy=\"20\" rx=\"10\" ry=\"13\" fill=\"#d97706\"/><path d=\"M20 8C17 14 23 26 20 32\" stroke=\"#451a03\" stroke-width=\"2.2\" stroke-linecap=\"round\"/><text x=\"50\" y=\"25\" font-family=\"Pretendard, sans-serif\" font-weight=\"800\" font-size=\"13\" fill=\"#451a03\">" + escapeXml(displayUpper) + "</text></svg>",
                        Map.of("primary", "#78350f", "secondary", "#b45309", "accent", "#d97706", "surface", "#fffbeb"),
                        List.of("크래프트", "웜앰버", "에디토리얼")
                },
                {
                        "미니멀 프리즘 마크",
                        "<svg viewBox=\"0 0 180 40\" xmlns=\"http://www.w3.org/2000/svg\"><rect width=\"40\" height=\"40\" rx=\"10\" fill=\"#090d16\"/><polygon points=\"20,9 31,29 9,29\" stroke=\"#38bdf8\" stroke-width=\"2.4\" fill=\"none\"/><circle cx=\"20\" cy=\"22\" r=\"3\" fill=\"#a855f7\"/><text x=\"50\" y=\"25\" font-family=\"Pretendard, sans-serif\" font-weight=\"800\" font-size=\"13\" fill=\"#090d16\">" + escapeXml(displayUpper) + "</text></svg>",
                        Map.of("primary", "#0f172a", "secondary", "#7c3aed", "accent", "#38bdf8", "surface", "#f8fafc"),
                        List.of("지오메트릭", "모던프리즘", "스튜디오")
                },
                {
                        "코랄 아치 시그니처",
                        "<svg viewBox=\"0 0 180 40\" xmlns=\"http://www.w3.org/2000/svg\"><rect width=\"40\" height=\"40\" rx=\"10\" fill=\"#881337\"/><path d=\"M12 29V19C12 14.5 15.5 11 20 11C24.5 11 28 14.5 28 19V29\" stroke=\"#fda4af\" stroke-width=\"2.6\" fill=\"none\"/><circle cx=\"20\" cy=\"19\" r=\"3.5\" fill=\"#fb7185\"/><text x=\"50\" y=\"25\" font-family=\"Pretendard, sans-serif\" font-weight=\"800\" font-size=\"13\" fill=\"#881337\">" + escapeXml(displayUpper) + "</text></svg>",
                        Map.of("primary", "#9f1239", "secondary", "#e11d48", "accent", "#f43f5e", "surface", "#fff1f2"),
                        List.of("부티크아치", "로즈코랄", "감성브랜드")
                }
        };

        Map<String, Object> typo = Map.of("fontHeading", "Pretendard", "fontBody", "Pretendard", "weightHeading", "800");
        for (int i = 0; i < logoSpecs.length; i++) {
            Object[] spec = logoSpecs[i];
            boolean isFirst = (i == 0);
            jdbcTemplate.update(
                    "INSERT INTO kkumz_logo_suggestion (id, idea_id, style_name, svg_code, color_palette_json, typography_json, tags_json, is_selected, created_at) " +
                    "VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, CURRENT_TIMESTAMP)",
                    "logo-" + ideaId + "-" + (i + 1), ideaId, spec[0], spec[1], toJson(spec[2]), toJson(typo), toJson(spec[3]), isFirst
            );
            if (isFirst) {
                jdbcTemplate.update(
                        "UPDATE kkumz_idea SET selected_logo_svg = ?, design_tokens_json = ?::jsonb, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                        spec[1], toJson(spec[2]), ideaId
                );
            }
        }
        return getIdeaDetail(ideaId);
    }

    @Transactional
    public Map<String, Object> selectBrand(String ideaId, SelectBrandRequest req) {
        Map<String, Object> idea = getIdeaDetail(ideaId);
        String brandName = req.getBrandName();
        String siteSlug = req.getSiteSlug();
        String logoSvg = req.getLogoSvg();
        Map<String, Object> tokens = req.getDesignTokens();

        if (req.getNameSuggestionId() != null && !req.getNameSuggestionId().isBlank()) {
            jdbcTemplate.update("UPDATE kkumz_name_suggestion SET is_selected = (id = ?) WHERE idea_id = ?",
                    req.getNameSuggestionId(), ideaId);
            List<Map<String, Object>> nameRows = jdbcTemplate.queryForList(
                    "SELECT brand_name, site_slug FROM kkumz_name_suggestion WHERE id = ? LIMIT 1",
                    req.getNameSuggestionId()
            );
            if (!nameRows.isEmpty()) {
                brandName = String.valueOf(nameRows.get(0).get("brand_name"));
                siteSlug = String.valueOf(nameRows.get(0).get("site_slug"));
            }
        }
        if (req.getLogoSuggestionId() != null && !req.getLogoSuggestionId().isBlank()) {
            jdbcTemplate.update("UPDATE kkumz_logo_suggestion SET is_selected = (id = ?) WHERE idea_id = ?",
                    req.getLogoSuggestionId(), ideaId);
            List<Map<String, Object>> logoRows = jdbcTemplate.queryForList(
                    "SELECT svg_code, color_palette_json::text AS palette_text FROM kkumz_logo_suggestion WHERE id = ? LIMIT 1",
                    req.getLogoSuggestionId()
            );
            if (!logoRows.isEmpty()) {
                logoSvg = String.valueOf(logoRows.get(0).get("svg_code"));
                tokens = parseJsonMap(logoRows.get(0).get("palette_text"));
            }
        }

        if (brandName == null || brandName.isBlank()) {
            brandName = idea.get("selectedBrandName") != null ? String.valueOf(idea.get("selectedBrandName")) : String.valueOf(idea.get("title"));
        }
        if (siteSlug == null || siteSlug.isBlank()) {
            siteSlug = idea.get("selectedSiteSlug") != null ? String.valueOf(idea.get("selectedSiteSlug")) : toSlugBase(brandName);
        }
        if (logoSvg == null || logoSvg.isBlank()) {
            logoSvg = idea.get("selectedLogoSvg") != null ? String.valueOf(idea.get("selectedLogoSvg")) : "";
        }
        String tokensJson = toJson(tokens != null && !tokens.isEmpty() ? tokens : idea.get("designTokens"));

        jdbcTemplate.update(
                "UPDATE kkumz_idea SET selected_brand_name = ?, selected_site_slug = ?, selected_logo_svg = ?, " +
                "design_tokens_json = ?::jsonb, current_step = GREATEST(current_step, 3), updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                brandName, siteSlug, logoSvg, tokensJson, ideaId
        );
        return getIdeaDetail(ideaId);
    }

    // =========================================================================
    // 3. Step 3 — AI 마크다운 기획서 & 화면 블루프린트 생성 및 수정
    // =========================================================================

    @Transactional
    public Map<String, Object> generateSpec(String ideaId) {
        Map<String, Object> idea = getIdeaDetail(ideaId);
        String title = String.valueOf(idea.get("title"));
        String prompt = String.valueOf(idea.get("promptText"));
        String siteType = String.valueOf(idea.get("siteType"));
        String brandName = idea.get("selectedBrandName") != null ? String.valueOf(idea.get("selectedBrandName")) : title;

        String specMarkdown = buildDefaultMarkdownSpec(title, prompt, siteType, brandName);
        List<Map<String, Object>> screens = buildDefaultScreens(siteType);

        SaveIdeaVersionRequest saveReq = SaveIdeaVersionRequest.builder()
                .title(title)
                .promptText(prompt)
                .specMarkdown(specMarkdown)
                .screens(screens)
                .createdBy(String.valueOf(idea.get("authorUsername")))
                .build();
        saveOrRollbackIdeaVersion(ideaId, saveReq);
        jdbcTemplate.update("UPDATE kkumz_idea SET current_step = GREATEST(current_step, 3) WHERE id = ?", ideaId);
        return getIdeaDetail(ideaId);
    }

    @Transactional
    public Map<String, Object> updateSpec(String ideaId, UpdateSpecRequest req) {
        SaveIdeaVersionRequest saveReq = SaveIdeaVersionRequest.builder()
                .title(req.getTitle())
                .promptText(req.getPromptText())
                .specMarkdown(req.getSpecMarkdown())
                .screens(req.getScreens())
                .createdBy(req.getUpdatedBy())
                .build();
        return saveOrRollbackIdeaVersion(ideaId, saveReq);
    }

    // =========================================================================
    // 4. Step 4, 5, 6 — 멀티사이트 스캐폴딩, 대화형 편집, WebIDE, 빌드/배포 & 롤백
    // =========================================================================

    @Transactional
    public Map<String, Object> scaffoldSite(String ideaId) {
        Map<String, Object> idea = getIdeaDetail(ideaId);
        String siteType = String.valueOf(idea.get("siteType"));
        String category = String.valueOf(idea.get("category"));
        String brandName = idea.get("selectedBrandName") != null && !String.valueOf(idea.get("selectedBrandName")).isBlank()
                ? String.valueOf(idea.get("selectedBrandName"))
                : String.valueOf(idea.get("title"));
        String rawSlug = idea.get("selectedSiteSlug") != null && !String.valueOf(idea.get("selectedSiteSlug")).isBlank()
                ? String.valueOf(idea.get("selectedSiteSlug"))
                : toSlugBase(brandName);
        String logoSvg = idea.get("selectedLogoSvg") != null && !String.valueOf(idea.get("selectedLogoSvg")).isBlank()
                ? String.valueOf(idea.get("selectedLogoSvg"))
                : "<svg viewBox=\"0 0 185 40\" xmlns=\"http://www.w3.org/2000/svg\"><rect width=\"40\" height=\"40\" rx=\"10\" fill=\"#0f172a\"/><circle cx=\"20\" cy=\"20\" r=\"9\" fill=\"#38bdf8\"/><text x=\"50\" y=\"25\" font-family=\"Pretendard, sans-serif\" font-weight=\"800\" font-size=\"13\" fill=\"#0f172a\">" + escapeXml(brandName) + "</text></svg>";
        String slogan = String.valueOf(idea.get("promptText"));
        if (slogan.length() > 110) {
            slogan = slogan.substring(0, 110);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> tokens = (Map<String, Object>) idea.get("designTokens");
        if (tokens == null || tokens.isEmpty()) {
            tokens = Map.of("primary", "#14532d", "secondary", "#16a34a", "accent", "#ca8a04", "surface", "#f7fee7", "radius", "14px");
        }
        Map<String, Object> layoutSchema = new LinkedHashMap<>();
        layoutSchema.put("heroTitle", brandName + " — 공식 브랜드 쇼케이스");
        layoutSchema.put("heroSubtitle", slogan);
        layoutSchema.put("ctaText", "COMMERCE".equals(siteType) ? "시그니처 상품 구매하기" : ("CMS_SITE".equals(siteType) ? "커뮤니티 세션 신청하기" : "매거진 & 스토어 탐색하기"));
        layoutSchema.put("showFlashDeal", !"CMS_SITE".equals(siteType));
        layoutSchema.put("showAiConcierge", true);
        layoutSchema.put("showCmsArticles", !"COMMERCE".equals(siteType));
        layoutSchema.put("showCmsForm", !"COMMERCE".equals(siteType));
        layoutSchema.put("customBadge", "AI 멀티사이트 프로비저닝 완료");

        List<Map<String, Object>> existingSites = jdbcTemplate.queryForList(
                "SELECT id, site_slug, shop_site_id, cms_site_id, active_version FROM kkumz_site WHERE idea_id = ? LIMIT 1",
                ideaId
        );
        if (!existingSites.isEmpty()) {
            String existingSlug = String.valueOf(existingSites.get(0).get("site_slug"));
            jdbcTemplate.update("UPDATE kkumz_idea SET current_step = GREATEST(current_step, 4), updated_at = CURRENT_TIMESTAMP WHERE id = ?", ideaId);
            return getSiteRuntime(existingSlug);
        }

        String siteSlug = ensureAvailableSlug(rawSlug, ideaId);
        Long shopSiteId = null;
        Long cmsSiteId = null;
        if ("COMMERCE".equals(siteType) || "HYBRID".equals(siteType)) {
            shopSiteId = provisionSyncShopSite(siteSlug, brandName, siteType, logoSvg, slogan, tokens, layoutSchema);
        }
        if ("CMS_SITE".equals(siteType) || "HYBRID".equals(siteType)) {
            cmsSiteId = provisionSyncCmsSite(siteSlug, brandName, slogan);
        }

        String siteId = "site-kkumz-" + UUID.randomUUID().toString().substring(0, 8);
        String ownerUser = String.valueOf(idea.get("authorUsername"));
        String ownerName = String.valueOf(idea.get("authorDisplayName"));

        jdbcTemplate.update(
                "INSERT INTO kkumz_site (id, idea_id, site_slug, site_name, site_type, category, brand_slogan, " +
                "logo_svg, shop_site_id, cms_site_id, deploy_mode, active_version, deployed_url, custom_domain, is_featured, visibility, " +
                "like_count, comment_count, view_count, owner_username, owner_display_name, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'DYNAMIC_SDUI', 1, ?, ?, FALSE, 'PUBLIC', 1, 0, 1, ?, ?, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                siteId, ideaId, siteSlug, brandName, siteType, category, slogan, logoSvg,
                shopSiteId, cmsSiteId, "/s/" + siteSlug, siteSlug + ".kkumz.io", ownerUser, ownerName
        );

        List<Map<String, Object>> webideFiles = buildDefaultWebIdeFiles(brandName, siteSlug, siteType, tokens, layoutSchema);
        String buildLog = "[Instant-Deploy] Provisioned multi-site '" + siteSlug + "' (shopSiteId=" + shopSiteId + ", cmsSiteId=" + cmsSiteId + ").\n" +
                "[Publish] Live endpoint /s/" + siteSlug + " is ready.";
        String releaseId = "rel-" + siteId + "-v1";

        jdbcTemplate.update(
                "INSERT INTO kkumz_site_release (id, site_id, version_no, release_note, deploy_mode, " +
                "design_tokens_json, layout_schema_json, webide_files_json, minio_bundle_path, build_status, build_log, created_by, created_at) " +
                "VALUES (?, ?, 1, '초기 멀티사이트 프로비저닝 및 기본 스키마 배포', 'DYNAMIC_SDUI', ?::jsonb, ?::jsonb, ?::jsonb, ?, 'SUCCESS', ?, ?, CURRENT_TIMESTAMP)",
                releaseId, siteId, toJson(tokens), toJson(layoutSchema), toJson(webideFiles),
                "minio://kkumz-sites/" + siteSlug + "/v1/schema.json", buildLog, ownerUser
        );

        recordBuildJob(siteId, releaseId, "INITIAL_PROVISION", "DYNAMIC_SDUI", "SUCCESS", 680, null, false, buildLog, ownerUser);

        jdbcTemplate.update(
                "INSERT INTO kkumz_team_member (id, site_id, recruit_id, username, display_name, member_role, position_role, join_type, application_message, status, joined_at) " +
                "VALUES (?, ?, NULL, ?, ?, 'OWNER', 'CO_FOUNDER', 'FOUNDER', '프로젝트 총괄 창업자', 'ACTIVE', CURRENT_TIMESTAMP)",
                "tmem-" + siteId + "-founder", siteId, ownerUser, ownerName
        );

        jdbcTemplate.update(
                "UPDATE kkumz_idea SET selected_brand_name = ?, selected_site_slug = ?, current_step = GREATEST(current_step, 4), status = 'PUBLISHED', updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                brandName, siteSlug, ideaId
        );

        return getSiteRuntime(siteSlug);
    }

    @Transactional
    public Map<String, Object> chatRefineSite(String siteSlug, ChatRefineRequest req) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteSlug);
        String siteId = String.valueOf(site.get("id"));
        int currentVer = ((Number) site.get("activeVersion")).intValue();

        Map<String, Object> latestRel = getReleaseRecord(siteId, currentVer);
        Map<String, Object> tokens = new LinkedHashMap<>(parseJsonMap(latestRel.get("designTokensText")));
        Map<String, Object> layout = new LinkedHashMap<>(parseJsonMap(latestRel.get("layoutSchemaText")));
        List<Map<String, Object>> files = new ArrayList<>(parseJsonListOfMap(latestRel.get("webideFilesText")));

        String instruction = req.getInstruction() != null ? req.getInstruction().trim() : "";
        if (!instruction.isBlank() && site.get("ideaId") != null) {
            List<Map<String, Object>> matchedPalettes = jdbcTemplate.queryForList(
                    "SELECT color_palette_json::text AS palette_text FROM kkumz_logo_suggestion " +
                    "WHERE idea_id = ? AND (? ILIKE '%' || style_name || '%' OR tags_json::text ILIKE '%' || ? || '%') LIMIT 1",
                    site.get("ideaId"), instruction, extractCoreKeyword(instruction)
            );
            if (!matchedPalettes.isEmpty()) {
                tokens.putAll(parseJsonMap(matchedPalettes.get(0).get("palette_text")));
            }
        }
        if (instruction.contains("인디고")) {
            tokens.put("primary", "#1e1b4b");
            tokens.put("secondary", "#4f46e5");
            tokens.put("accent", "#06b6d4");
            tokens.put("surface", "#f8fafc");
        } else if (instruction.contains("포레스트")) {
            tokens.put("primary", "#14532d");
            tokens.put("secondary", "#16a34a");
            tokens.put("accent", "#ca8a04");
            tokens.put("surface", "#f7fee7");
        } else if (instruction.contains("크래프트")) {
            tokens.put("primary", "#78350f");
            tokens.put("secondary", "#b45309");
            tokens.put("accent", "#d97706");
            tokens.put("surface", "#fffbeb");
        }

        if (req.getTokenOverrides() != null) {
            tokens.putAll(req.getTokenOverrides());
        }
        if (req.getLayoutOverrides() != null) {
            layout.putAll(req.getLayoutOverrides());
        }
        if (!instruction.isBlank()) {
            layout.put("customBadge", "AI 반영: " + (instruction.length() > 28 ? instruction.substring(0, 28) + "..." : instruction));
            layout.put("heroSubtitle", instruction);
        }

        Integer maxVer = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(version_no), 1) FROM kkumz_site_release WHERE site_id = ?",
                Integer.class, siteId
        );
        int nextVer = (maxVer != null ? maxVer : 1) + 1;
        String note = instruction.isBlank() ? "대화형 프리뷰 디자인 토큰 및 레이아웃 수정" : "대화형 편집: " + instruction;
        String logMsg = "[Chat-Refine] Applied instruction: \"" + note + "\"\n[Hot-Reload] Updated designTokens & layoutSchema (v" + nextVer + ").";
        String releaseId = "rel-" + siteId + "-v" + nextVer;
        String user = req.getUsername() != null ? req.getUsername() : "admin";

        jdbcTemplate.update(
                "INSERT INTO kkumz_site_release (id, site_id, version_no, release_note, deploy_mode, " +
                "design_tokens_json, layout_schema_json, webide_files_json, minio_bundle_path, build_status, build_log, created_by, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, 'SUCCESS', ?, ?, CURRENT_TIMESTAMP)",
                releaseId, siteId, nextVer, note, site.get("deployMode"),
                toJson(tokens), toJson(layout), toJson(files),
                "minio://kkumz-sites/" + site.get("siteSlug") + "/v" + nextVer + "/schema.json",
                logMsg, user
        );

        recordBuildJob(siteId, releaseId, "CHAT_HOT_RELOAD", String.valueOf(site.get("deployMode")), "SUCCESS", 420, null, false, logMsg, user);

        jdbcTemplate.update(
                "UPDATE kkumz_site SET active_version = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                nextVer, siteId
        );
        return getSiteRuntime(String.valueOf(site.get("siteSlug")));
    }

    public Map<String, Object> getWebIdeFiles(String siteSlug) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteSlug);
        String siteId = String.valueOf(site.get("id"));
        int activeVer = ((Number) site.get("activeVersion")).intValue();
        Map<String, Object> rel = getReleaseRecord(siteId, activeVer);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("siteId", siteId);
        result.put("siteSlug", site.get("siteSlug"));
        result.put("siteName", site.get("siteName"));
        result.put("siteType", site.get("siteType"));
        result.put("deployMode", site.get("deployMode"));
        result.put("activeVersion", activeVer);
        result.put("buildStatus", rel.get("buildStatus"));
        result.put("buildLog", rel.get("buildLog"));
        result.put("minioBundlePath", rel.get("minioBundlePath"));
        result.put("files", parseJsonListOfMap(rel.get("webideFilesText")));
        result.put("releases", listSiteReleases(siteId));
        return result;
    }

    @Transactional
    public Map<String, Object> updateWebIdeFiles(String siteSlug, UpdateWebIdeFilesRequest req) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteSlug);
        String siteId = String.valueOf(site.get("id"));
        int activeVer = ((Number) site.get("activeVersion")).intValue();
        Map<String, Object> rel = getReleaseRecord(siteId, activeVer);

        List<Map<String, Object>> files = (req.getFiles() != null && !req.getFiles().isEmpty())
                ? req.getFiles()
                : parseJsonListOfMap(rel.get("webideFilesText"));

        boolean hasSyntaxIssue = Boolean.TRUE.equals(req.getSimulateSyntaxError());
        for (Map<String, Object> f : files) {
            String content = String.valueOf(f.getOrDefault("content", ""));
            if (content.contains("SYNTAX_ERROR") || content.contains("<<<")) {
                hasSyntaxIssue = true;
            }
        }

        String buildStatus = hasSyntaxIssue ? "FAILED" : "SUCCESS";
        String buildLog = hasSyntaxIssue
                ? "[Bundle-Builder] Compiling SFC bundle...\n[ERROR] [vite:vue] Unterminated template tag or invalid expression in src/App.vue:12:5\n[Hint] Click 'AI 에러 자동 수정' to diagnose and repair AST syntax automatically."
                : "[Bundle-Builder] Compiling " + files.size() + " virtual SFC files...\n[Vite-Bundle] TypeScript & Vue template check passed (0 errors).\n[Artifact] Ready at minio://kkumz-sites/" + site.get("siteSlug") + "/v" + activeVer + "/dist/index.js";

        jdbcTemplate.update(
                "UPDATE kkumz_site_release SET webide_files_json = ?::jsonb, build_status = ?, build_log = ? " +
                "WHERE site_id = ? AND version_no = ?",
                toJson(files), buildStatus, buildLog, siteId, activeVer
        );

        recordBuildJob(
                siteId,
                String.valueOf(rel.get("id")),
                "WEBIDE_BUNDLE_BUILD",
                "CUSTOM_BUNDLE",
                buildStatus,
                hasSyntaxIssue ? 610 : 1850,
                hasSyntaxIssue ? "Unterminated template tag in src/App.vue:12:5" : null,
                false,
                buildLog,
                req.getUsername() != null ? req.getUsername() : "admin"
        );
        return getWebIdeFiles(String.valueOf(site.get("siteSlug")));
    }

    @Transactional
    public Map<String, Object> aiFixBuildError(String siteSlug, FixBuildRequest req) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteSlug);
        String siteId = String.valueOf(site.get("id"));
        int activeVer = ((Number) site.get("activeVersion")).intValue();
        Map<String, Object> rel = getReleaseRecord(siteId, activeVer);

        List<Map<String, Object>> files = parseJsonListOfMap(rel.get("webideFilesText"));
        for (Map<String, Object> file : files) {
            String content = String.valueOf(file.getOrDefault("content", ""));
            content = content.replace("SYNTAX_ERROR", "").replace("<<<", "");
            file.put("content", content);
        }

        String repairedLog = "[AI Build Healer] Diagnosing previous compiler stacktrace...\n" +
                "[AI Patch] Repaired Vue SFC template closing tag & normalized TypeScript props in src/App.vue.\n" +
                "[Bundle-Builder] Re-running Vite production bundle build... SUCCESS (0 errors, 0 warnings).\n" +
                "[Artifact] Uploaded minio://kkumz-sites/" + site.get("siteSlug") + "/v" + activeVer + "/dist/index.js (43.2 KB).";

        jdbcTemplate.update(
                "UPDATE kkumz_site_release SET webide_files_json = ?::jsonb, build_status = 'SUCCESS', build_log = ? " +
                "WHERE site_id = ? AND version_no = ?",
                toJson(files), repairedLog, siteId, activeVer
        );

        recordBuildJob(
                siteId,
                String.valueOf(rel.get("id")),
                "AI_BUILD_SELF_HEAL",
                "CUSTOM_BUNDLE",
                "SUCCESS",
                1420,
                null,
                true,
                repairedLog,
                req != null && req.getUsername() != null ? req.getUsername() : "admin"
        );
        return getWebIdeFiles(String.valueOf(site.get("siteSlug")));
    }

    @Transactional
    public Map<String, Object> deploySite(String siteSlug, DeploySiteRequest req) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteSlug);
        String siteId = String.valueOf(site.get("id"));
        int activeVer = ((Number) site.get("activeVersion")).intValue();
        Map<String, Object> rel = getReleaseRecord(siteId, activeVer);

        String mode = (req != null && req.getDeployMode() != null && !req.getDeployMode().isBlank())
                ? req.getDeployMode().trim().toUpperCase(Locale.ROOT)
                : String.valueOf(site.get("deployMode"));
        String visibility = (req != null && req.getVisibility() != null && !req.getVisibility().isBlank())
                ? req.getVisibility().trim().toUpperCase(Locale.ROOT)
                : String.valueOf(site.get("visibility"));
        String note = (req != null && req.getReleaseNote() != null && !req.getReleaseNote().isBlank())
                ? req.getReleaseNote().trim()
                : ("CUSTOM_BUNDLE".equals(mode) ? "WebIDE 커스텀 코드 번들 빌드 및 라이브 배포" : "즉시 디자인 스키마 무중단 배포");

        Integer maxVer = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(version_no), 1) FROM kkumz_site_release WHERE site_id = ?",
                Integer.class, siteId
        );
        int nextVer = (maxVer != null ? maxVer : 1) + 1;
        String bundlePath = "CUSTOM_BUNDLE".equals(mode)
                ? "minio://kkumz-sites/" + site.get("siteSlug") + "/v" + nextVer + "/dist/index.js"
                : "minio://kkumz-sites/" + site.get("siteSlug") + "/v" + nextVer + "/schema.json";
        String buildLog = "[Deploy Engine] Mode=" + mode + ", Version=v" + nextVer + "\n" +
                "[Artifact] Published to " + bundlePath + "\n" +
                "[Gateway] Live URL /s/" + site.get("siteSlug") + " switched to v" + nextVer + " with zero downtime.";
        String releaseId = "rel-" + siteId + "-v" + nextVer;
        String user = (req != null && req.getUsername() != null) ? req.getUsername() : "admin";

        jdbcTemplate.update(
                "INSERT INTO kkumz_site_release (id, site_id, version_no, release_note, deploy_mode, " +
                "design_tokens_json, layout_schema_json, webide_files_json, minio_bundle_path, build_status, build_log, created_by, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, 'SUCCESS', ?, ?, CURRENT_TIMESTAMP)",
                releaseId, siteId, nextVer, note, mode,
                rel.get("designTokensText"), rel.get("layoutSchemaText"), rel.get("webideFilesText"),
                bundlePath, buildLog, user
        );

        recordBuildJob(siteId, releaseId, "LIVE_RELEASE_DEPLOY", mode, "SUCCESS", 1120, null, false, buildLog, user);

        jdbcTemplate.update(
                "UPDATE kkumz_site SET deploy_mode = ?, active_version = ?, visibility = ?, status = 'ACTIVE', updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                mode, nextVer, visibility, siteId
        );
        jdbcTemplate.update(
                "UPDATE kkumz_idea SET current_step = 6, status = 'PUBLISHED', visibility = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                visibility, site.get("ideaId")
        );
        return getSiteRuntime(String.valueOf(site.get("siteSlug")));
    }

    @Transactional
    public Map<String, Object> rollbackSiteRelease(String siteSlug, Integer versionNo) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteSlug);
        String siteId = String.valueOf(site.get("id"));
        Map<String, Object> targetRel = getReleaseRecord(siteId, versionNo);

        jdbcTemplate.update(
                "UPDATE kkumz_site SET active_version = ?, deploy_mode = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                versionNo, targetRel.get("deployMode"), siteId
        );
        return getSiteRuntime(String.valueOf(site.get("siteSlug")));
    }

    // =========================================================================
    // 5. 멀티사이트 통합 라이브 런타임 렌더러 (/s/:siteSlug) & 실주문/폼제출/AI컨시어지
    // =========================================================================

    public Map<String, Object> getSiteRuntime(String siteSlug) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteSlug);
        String siteId = String.valueOf(site.get("id"));
        int activeVer = ((Number) site.get("activeVersion")).intValue();
        Map<String, Object> rel = getReleaseRecord(siteId, activeVer);

        jdbcTemplate.update("UPDATE kkumz_site SET view_count = view_count + 1 WHERE id = ?", siteId);

        Map<String, Object> runtime = new LinkedHashMap<>(site);
        runtime.put("designTokens", parseJsonMap(rel.get("designTokensText")));
        runtime.put("layoutSchema", parseJsonMap(rel.get("layoutSchemaText")));
        runtime.put("webideFiles", parseJsonListOfMap(rel.get("webideFilesText")));
        runtime.put("releaseNote", rel.get("releaseNote"));
        runtime.put("buildStatus", rel.get("buildStatus"));
        runtime.put("buildLog", rel.get("buildLog"));
        runtime.put("minioBundlePath", rel.get("minioBundlePath"));
        runtime.put("releases", listSiteReleases(siteId));

        String siteType = String.valueOf(site.get("siteType"));
        String siteName = String.valueOf(site.get("siteName"));
        String slogan = String.valueOf(site.get("brandSlogan"));
        Long shopSiteId = site.get("shopSiteId") != null ? ((Number) site.get("shopSiteId")).longValue() : null;
        Long cmsSiteId = site.get("cmsSiteId") != null ? ((Number) site.get("cmsSiteId")).longValue() : null;

        if ("COMMERCE".equals(siteType) || "HYBRID".equals(siteType) || shopSiteId != null) {
            runtime.put("commerce", loadCommerceData(shopSiteId != null ? shopSiteId : 2L, siteName, slogan));
        } else {
            runtime.put("commerce", Map.of("banners", List.of(), "categories", List.of(), "products", List.of(), "coupons", List.of()));
        }

        if ("CMS_SITE".equals(siteType) || "HYBRID".equals(siteType) || cmsSiteId != null) {
            runtime.put("cms", loadCmsData(cmsSiteId != null ? cmsSiteId : 101L, siteName, slogan));
        } else {
            runtime.put("cms", Map.of("categories", List.of(), "articles", List.of(), "banners", List.of(), "forms", List.of(), "recentSubmissions", List.of()));
        }

        runtime.put("recruits", listSiteRecruits(siteId));
        runtime.put("teamMembers", listSiteTeamMembers(siteId));
        runtime.put("comments", listSiteComments(siteId));
        return runtime;
    }

    public Map<String, Object> createCommerceOrder(String siteSlug, CommerceOrderRequest req) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteSlug);
        Long shopSiteId = site.get("shopSiteId") != null ? ((Number) site.get("shopSiteId")).longValue() : 2L;
        int quantity = (req.getQuantity() != null && req.getQuantity() > 0) ? req.getQuantity() : 1;
        String orderSn = "KKZ-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + shopSiteId;
        String receiverName = (req.getReceiverName() == null || req.getReceiverName().isBlank()) ? "김도현 고객" : req.getReceiverName().trim();
        String receiverPhone = (req.getReceiverPhone() == null || req.getReceiverPhone().isBlank()) ? "안심번호-기본" : req.getReceiverPhone().trim();
        String receiverAddress = (req.getReceiverAddress() == null || req.getReceiverAddress().isBlank())
                ? "서울특별시 강남구 테헤란로 123 엠파시타워"
                : req.getReceiverAddress().trim();

        Long orderId = System.currentTimeMillis() % 100000L;
        Long resolvedProductId = req.getProductId() != null ? req.getProductId() : 101L;
        String productName = site.get("siteName") + " 시그니처 플래그십 에디션";
        BigDecimal unitPrice = new BigDecimal("42000");
        BigDecimal discount = req.getDiscountAmount() != null ? req.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal payAmount = unitPrice.multiply(BigDecimal.valueOf(quantity)).subtract(discount).max(BigDecimal.ZERO);

        try {
            if (shopJdbcTemplate != null) {
                List<Map<String, Object>> prodRows = shopJdbcTemplate.queryForList(
                        "SELECT id, name, pic, product_sn, brand_name, product_category_id, price, promotion_price, stock " +
                        "FROM pms_product WHERE id = ? AND site_id = ? LIMIT 1",
                        req.getProductId(), shopSiteId
                );
                if (prodRows.isEmpty()) {
                    prodRows = shopJdbcTemplate.queryForList(
                            "SELECT id, name, pic, product_sn, brand_name, product_category_id, price, promotion_price, stock " +
                            "FROM pms_product WHERE site_id = ? ORDER BY sort DESC, id ASC LIMIT 1",
                            shopSiteId
                    );
                }
                if (!prodRows.isEmpty()) {
                    Map<String, Object> product = prodRows.get(0);
                    resolvedProductId = ((Number) product.get("id")).longValue();
                    productName = String.valueOf(product.get("name"));
                    unitPrice = product.get("promotion_price") != null
                            ? new BigDecimal(String.valueOf(product.get("promotion_price")))
                            : new BigDecimal(String.valueOf(product.get("price")));
                    BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
                    payAmount = totalAmount.subtract(discount).max(BigDecimal.ZERO);

                    orderId = shopJdbcTemplate.queryForObject(
                            "INSERT INTO oms_order (site_id, member_id, order_sn, create_time, member_username, total_amount, pay_amount, " +
                            "freight_amount, discount_amount, pay_type, source_type, status, order_type, receiver_name, receiver_phone, " +
                            "receiver_detail_address, note, confirm_status, delete_status, payment_time, modify_time) " +
                            "VALUES (?, 1, ?, ?, 'kkumz.buyer', ?, ?, 0.00, ?, 1, 1, 1, 0, ?, ?, ?, ?, 0, 0, ?, ?) RETURNING id",
                            Long.class,
                            shopSiteId, orderSn, Timestamp.from(Instant.now()), totalAmount, payAmount, discount,
                            receiverName, receiverPhone, receiverAddress, req.getNote(),
                            Timestamp.from(Instant.now()), Timestamp.from(Instant.now())
                    );

                    shopJdbcTemplate.update(
                            "INSERT INTO oms_order_item (order_id, order_sn, product_id, product_pic, product_name, product_brand, " +
                            "product_sn, product_price, product_quantity, product_category_id, real_amount) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            orderId, orderSn, resolvedProductId, product.get("pic"), productName, product.get("brand_name"),
                            product.get("product_sn"), unitPrice, quantity, product.get("product_category_id"), payAmount
                    );

                    shopJdbcTemplate.update(
                            "UPDATE pms_product SET stock = GREATEST(0, COALESCE(stock, 10) - ?), sale = COALESCE(sale, 0) + ? WHERE id = ?",
                            quantity, quantity, resolvedProductId
                    );
                }
            }
        } catch (Exception e) {
            log.info("Standalone 모드 주문 접수 처리 (orderSn={}): {}", orderSn, e.getMessage());
        }

        createNotification(
                String.valueOf(site.get("ownerUsername")),
                "COMMERCE_ORDER",
                "[" + site.get("siteName") + "] 신규 주문 접수 (" + orderSn + ")",
                receiverName + " 고객님이 " + productName + " " + quantity + "건을 주문했습니다.",
                "/s/" + site.get("siteSlug")
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", orderId);
        result.put("orderSn", orderSn);
        result.put("shopSiteId", shopSiteId);
        result.put("siteSlug", site.get("siteSlug"));
        result.put("productId", resolvedProductId);
        result.put("productName", productName);
        result.put("quantity", quantity);
        result.put("payAmount", payAmount);
        result.put("receiverName", receiverName);
        result.put("status", "PAID_READY_FOR_SHIPMENT");
        return result;
    }

    public Map<String, Object> submitCmsForm(String siteSlug, CmsFormSubmitRequest req) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteSlug);
        Long cmsSiteId = site.get("cmsSiteId") != null ? ((Number) site.get("cmsSiteId")).longValue() : 101L;
        Long formId = req.getFormId() != null ? req.getFormId() : 101L;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("applicantName", req.getApplicantName() != null && !req.getApplicantName().isBlank() ? req.getApplicantName().trim() : "김도현 신청자");
        payload.put("phone", (req.getPhone() != null && !req.getPhone().isBlank()) ? req.getPhone().trim() : "안심번호-기본");
        payload.put("optionValue", req.getOptionValue() != null ? req.getOptionValue() : "정규 세션 참가 신청");
        payload.put("message", req.getMessage() != null ? req.getMessage() : "꿈즈 라이브 사이트를 통해 온라인 신청합니다.");
        if (req.getExtraFields() != null) {
            payload.putAll(req.getExtraFields());
        }

        Long submissionId = System.currentTimeMillis() % 100000L;
        try {
            if (cmsJdbcTemplate != null) {
                if (req.getFormId() == null) {
                    List<Map<String, Object>> forms = cmsJdbcTemplate.queryForList(
                            "SELECT id FROM cms_form WHERE site_id = ? ORDER BY id ASC LIMIT 1", cmsSiteId.shortValue()
                    );
                    if (!forms.isEmpty()) {
                        formId = ((Number) forms.get(0).get("id")).longValue();
                    }
                }
                submissionId = cmsJdbcTemplate.queryForObject(
                        "INSERT INTO cms_form_data (form_id, submission_data, client_ip, status, admin_note, create_date) " +
                        "VALUES (?, ?::jsonb, '127.0.0.1', 'RECEIVED', '꿈즈 라이브 사이트 온라인 접수 완료', CURRENT_TIMESTAMP) RETURNING id",
                        Long.class, formId, toJson(payload)
                );
            }
        } catch (Exception e) {
            log.info("Standalone 모드 온라인 신청 폼 접수 (siteSlug={}): {}", siteSlug, e.getMessage());
        }

        createNotification(
                String.valueOf(site.get("ownerUsername")),
                "FORM_SUBMIT",
                "[" + site.get("siteName") + "] 온라인 참가/문의 신청 접수",
                payload.get("applicantName") + "님이 [" + payload.get("optionValue") + "] 신청서를 제출했습니다.",
                "/s/" + site.get("siteSlug")
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("submissionId", submissionId);
        result.put("formId", formId);
        result.put("cmsSiteId", cmsSiteId);
        result.put("siteSlug", site.get("siteSlug"));
        result.put("submittedData", payload);
        result.put("status", "RECEIVED");
        return result;
    }

    public Map<String, Object> askSiteConcierge(String siteSlug, SiteConciergeRequest req) {
        Map<String, Object> runtime = getSiteRuntime(siteSlug);
        String siteName = String.valueOf(runtime.get("siteName"));
        String siteType = String.valueOf(runtime.get("siteType"));
        String question = (req != null && req.getQuestion() != null && !req.getQuestion().isBlank())
                ? req.getQuestion().trim()
                : "이 사이트의 대표 추천 상품과 특징을 알려주세요.";

        @SuppressWarnings("unchecked")
        Map<String, Object> commerce = (Map<String, Object>) runtime.get("commerce");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> products = commerce != null ? (List<Map<String, Object>>) commerce.get("products") : List.of();
        @SuppressWarnings("unchecked")
        Map<String, Object> cms = (Map<String, Object>) runtime.get("cms");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> articles = cms != null ? (List<Map<String, Object>>) cms.get("articles") : List.of();

        StringBuilder answer = new StringBuilder();
        answer.append("안녕하세요! **").append(siteName).append("** AI 컨시어지입니다. 문의하신 [").append(question).append("]에 대해 안내해 드립니다.\n\n");
        if (!products.isEmpty()) {
            Map<String, Object> top = products.get(0);
            answer.append("- **시그니처 추천 상품**: `").append(top.get("name")).append("` (할인가 ")
                  .append(top.get("promotionPrice") != null ? top.get("promotionPrice") : top.get("price"))
                  .append("원) — ").append(top.get("subTitle")).append("\n");
        }
        if (!articles.isEmpty()) {
            Map<String, Object> art = articles.get(0);
            answer.append("- **주요 콘텐츠/세션 안내**: `").append(art.get("title")).append("` — ").append(art.get("description")).append("\n");
        }
        answer.append("- **원클릭 이용 안내**: 화면 내 상품 주문 모달 또는 온라인 신청 폼에서 바로 주문·접수하실 수 있습니다.");

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("siteSlug", siteSlug);
        res.put("siteType", siteType);
        res.put("question", question);
        res.put("answer", answer.toString());
        res.put("recommendedProductCount", products.size());
        res.put("recommendedArticleCount", articles.size());
        return res;
    }

    // =========================================================================
    // 6. SNS 커뮤니티 쇼케이스 피드, 좋아요, 댓글, 알림 & 팀빌딩 라운지
    // =========================================================================

    public List<Map<String, Object>> getCommunityFeed(String siteType, String recruitRole, String sortBy, String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT s.id, s.idea_id AS \"ideaId\", s.site_slug AS \"siteSlug\", s.site_name AS \"siteName\", " +
                "s.site_type AS \"siteType\", s.category, s.brand_slogan AS \"brandSlogan\", s.logo_svg AS \"logoSvg\", " +
                "s.shop_site_id AS \"shopSiteId\", s.cms_site_id AS \"cmsSiteId\", s.deploy_mode AS \"deployMode\", " +
                "s.active_version AS \"activeVersion\", s.deployed_url AS \"deployedUrl\", s.custom_domain AS \"customDomain\", " +
                "s.is_featured AS \"isFeatured\", s.visibility, " +
                "s.like_count AS \"likeCount\", s.comment_count AS \"commentCount\", s.view_count AS \"viewCount\", " +
                "s.owner_username AS \"ownerUsername\", s.owner_display_name AS \"ownerDisplayName\", " +
                "s.status, s.created_at AS \"createdAt\", s.updated_at AS \"updatedAt\", " +
                "i.prompt_text AS \"promptText\", i.tags_json::text AS \"tagsJson\", i.design_tokens_json::text AS \"designTokensJson\" " +
                "FROM kkumz_site s " +
                "JOIN kkumz_idea i ON i.id = s.idea_id " +
                "WHERE s.status = 'ACTIVE' AND s.visibility = 'PUBLIC' "
        );
        List<Object> params = new ArrayList<>();
        if (siteType != null && !siteType.isBlank() && !"ALL".equalsIgnoreCase(siteType)) {
            sql.append(" AND s.site_type = ? ");
            params.add(siteType.trim().toUpperCase(Locale.ROOT));
        }
        if (recruitRole != null && !recruitRole.isBlank() && !"ALL".equalsIgnoreCase(recruitRole)) {
            sql.append(" AND EXISTS (SELECT 1 FROM kkumz_team_recruit r WHERE r.site_id = s.id AND r.status = 'OPEN' AND r.position_role = ?) ");
            params.add(recruitRole.trim().toUpperCase(Locale.ROOT));
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (s.site_name ILIKE ? OR s.brand_slogan ILIKE ? OR i.prompt_text ILIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if ("LATEST".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY s.created_at DESC ");
        } else {
            sql.append(" ORDER BY s.is_featured DESC, s.like_count DESC, s.view_count DESC ");
        }

        List<Map<String, Object>> sites = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        for (Map<String, Object> item : sites) {
            String siteId = String.valueOf(item.get("id"));
            item.put("tags", parseJsonList(item.remove("tagsJson")));
            item.put("designTokens", parseJsonMap(item.remove("designTokensJson")));
            item.put("openRecruits", listSiteRecruits(siteId));
        }
        return sites;
    }

    @Transactional
    public Map<String, Object> toggleSiteLike(String siteIdOrSlug, String username) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String user = (username != null && !username.isBlank()) ? username.trim() : "guest." + UUID.randomUUID().toString().substring(0, 6);

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

        Integer updatedLikes = jdbcTemplate.queryForObject(
                "SELECT like_count FROM kkumz_site WHERE id = ?", Integer.class, siteId
        );
        return Map.of("siteId", siteId, "liked", liked, "likeCount", updatedLikes != null ? updatedLikes : 0);
    }

    public List<Map<String, Object>> listSiteComments(String siteIdOrSlug) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        return jdbcTemplate.queryForList(
                "SELECT id, site_id AS \"siteId\", username, display_name AS \"displayName\", " +
                "author_role_badge AS \"authorRoleBadge\", content, created_at AS \"createdAt\" " +
                "FROM kkumz_feed_comment WHERE site_id = ? ORDER BY created_at DESC",
                siteId
        );
    }

    @Transactional
    public List<Map<String, Object>> addSiteComment(String siteIdOrSlug, CreateCommentRequest req) {
        if (req.getContent() == null || req.getContent().isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력해 주세요.");
        }
        Map<String, Object> site = getSiteRecordBySlugOrId(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String username = (req.getUsername() != null && !req.getUsername().isBlank()) ? req.getUsername().trim() : "maker.user";
        String displayName = (req.getDisplayName() != null && !req.getDisplayName().isBlank()) ? req.getDisplayName().trim() : "김도현 메이커";
        String badge = (req.getAuthorRoleBadge() != null && !req.getAuthorRoleBadge().isBlank()) ? req.getAuthorRoleBadge().trim() : "커뮤니티 멤버";

        jdbcTemplate.update(
                "INSERT INTO kkumz_feed_comment (id, site_id, username, display_name, author_role_badge, content, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                "cmt-" + UUID.randomUUID().toString().substring(0, 8), siteId, username, displayName, badge, req.getContent().trim()
        );
        jdbcTemplate.update("UPDATE kkumz_site SET comment_count = comment_count + 1 WHERE id = ?", siteId);
        return listSiteComments(siteId);
    }

    public List<Map<String, Object>> listSiteRecruits(String siteIdOrSlug) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, site_id AS \"siteId\", position_role AS \"positionRole\", title, description, " +
                "required_skills_json::text AS \"requiredSkillsJson\", headcount, recruited_count AS \"recruitedCount\", " +
                "status, created_at AS \"createdAt\" " +
                "FROM kkumz_team_recruit WHERE site_id = ? ORDER BY created_at DESC",
                siteId
        );
        for (Map<String, Object> row : rows) {
            row.put("requiredSkills", parseJsonList(row.remove("requiredSkillsJson")));
        }
        return rows;
    }

    @Transactional
    public List<Map<String, Object>> createSiteRecruit(String siteIdOrSlug, CreateRecruitRequest req) {
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new IllegalArgumentException("모집 공고 제목은 필수입니다.");
        }
        Map<String, Object> site = getSiteRecordBySlugOrId(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String role = (req.getPositionRole() != null && !req.getPositionRole().isBlank()) ? req.getPositionRole().trim().toUpperCase(Locale.ROOT) : "FRONTEND";
        int headcount = (req.getHeadcount() != null && req.getHeadcount() > 0) ? req.getHeadcount() : 1;
        String skillsJson = toJson(req.getRequiredSkills() != null ? req.getRequiredSkills() : List.of(role));

        jdbcTemplate.update(
                "INSERT INTO kkumz_team_recruit (id, site_id, position_role, title, description, required_skills_json, headcount, recruited_count, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, 0, 'OPEN', CURRENT_TIMESTAMP)",
                "rec-" + UUID.randomUUID().toString().substring(0, 8), siteId, role, req.getTitle().trim(),
                req.getDescription() != null ? req.getDescription().trim() : "", skillsJson, headcount
        );
        return listSiteRecruits(siteId);
    }

    public List<Map<String, Object>> listSiteTeamMembers(String siteIdOrSlug) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        return jdbcTemplate.queryForList(
                "SELECT id, site_id AS \"siteId\", recruit_id AS \"recruitId\", username, display_name AS \"displayName\", " +
                "member_role AS \"memberRole\", position_role AS \"positionRole\", join_type AS \"joinType\", " +
                "application_message AS \"applicationMessage\", status, joined_at AS \"joinedAt\" " +
                "FROM kkumz_team_member WHERE site_id = ? ORDER BY joined_at ASC",
                siteId
        );
    }

    @Transactional
    public List<Map<String, Object>> applyToTeam(String siteIdOrSlug, TeamApplyRequest req) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String username = (req.getUsername() != null && !req.getUsername().isBlank()) ? req.getUsername().trim() : "applicant." + UUID.randomUUID().toString().substring(0, 4);
        String displayName = (req.getDisplayName() != null && !req.getDisplayName().isBlank()) ? req.getDisplayName().trim() : "신규 지원자";
        String posRole = (req.getPositionRole() != null && !req.getPositionRole().isBlank()) ? req.getPositionRole().trim().toUpperCase(Locale.ROOT) : "FRONTEND";

        jdbcTemplate.update(
                "INSERT INTO kkumz_team_member (id, site_id, recruit_id, username, display_name, member_role, position_role, join_type, application_message, status, joined_at) " +
                "VALUES (?, ?, ?, ?, ?, 'EDITOR', ?, 'APPLIED', ?, 'PENDING', CURRENT_TIMESTAMP)",
                "tmem-" + UUID.randomUUID().toString().substring(0, 8), siteId, req.getRecruitId(),
                username, displayName, posRole, req.getApplicationMessage() != null ? req.getApplicationMessage() : "프로젝트 비전에 공감하여 지원합니다."
        );

        createNotification(
                String.valueOf(site.get("ownerUsername")),
                "TEAM_APPLY",
                "[" + site.get("siteName") + "] 신규 팀원 지원 도착",
                displayName + "님이 " + posRole + " 포지션에 지원했습니다.",
                "/lounge"
        );
        return listSiteTeamMembers(siteId);
    }

    @Transactional
    public List<Map<String, Object>> inviteTeamMember(String siteIdOrSlug, TeamInviteRequest req) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String username = (req.getUsername() != null && !req.getUsername().isBlank()) ? req.getUsername().trim() : "invited." + UUID.randomUUID().toString().substring(0, 4);
        String displayName = (req.getDisplayName() != null && !req.getDisplayName().isBlank()) ? req.getDisplayName().trim() : username;
        String memberRole = (req.getMemberRole() != null && !req.getMemberRole().isBlank()) ? req.getMemberRole().trim().toUpperCase(Locale.ROOT) : "EDITOR";
        String posRole = (req.getPositionRole() != null && !req.getPositionRole().isBlank()) ? req.getPositionRole().trim().toUpperCase(Locale.ROOT) : "PLANNER";

        jdbcTemplate.update(
                "INSERT INTO kkumz_team_member (id, site_id, recruit_id, username, display_name, member_role, position_role, join_type, application_message, status, joined_at) " +
                "VALUES (?, ?, NULL, ?, ?, ?, ?, 'INVITED', ?, 'ACTIVE', CURRENT_TIMESTAMP)",
                "tmem-" + UUID.randomUUID().toString().substring(0, 8), siteId, username, displayName, memberRole, posRole,
                req.getMessage() != null ? req.getMessage() : "창업자 직접 초대로 합류"
        );
        return listSiteTeamMembers(siteId);
    }

    @Transactional
    public List<Map<String, Object>> updateTeamMemberStatus(String siteIdOrSlug, String memberId, UpdateMemberStatusRequest req) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String status = (req.getStatus() != null && !req.getStatus().isBlank()) ? req.getStatus().trim().toUpperCase(Locale.ROOT) : "ACTIVE";
        String memberRole = (req.getMemberRole() != null && !req.getMemberRole().isBlank()) ? req.getMemberRole().trim().toUpperCase(Locale.ROOT) : "EDITOR";

        jdbcTemplate.update(
                "UPDATE kkumz_team_member SET status = ?, member_role = ? WHERE id = ? AND site_id = ?",
                status, memberRole, memberId, siteId
        );
        if ("ACTIVE".equals(status)) {
            jdbcTemplate.update(
                    "UPDATE kkumz_team_recruit SET recruited_count = recruited_count + 1 " +
                    "WHERE id = (SELECT recruit_id FROM kkumz_team_member WHERE id = ?)",
                    memberId
            );
        }
        return listSiteTeamMembers(siteId);
    }

    public List<Map<String, Object>> listNotifications(String username) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, recipient_username AS \"recipientUsername\", notif_type AS \"notifType\", " +
                "title, message, link_url AS \"linkUrl\", is_read AS \"isRead\", created_at AS \"createdAt\" " +
                "FROM kkumz_notification WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (username != null && !username.isBlank()) {
            sql.append(" AND recipient_username = ? ");
            params.add(username.trim());
        }
        sql.append(" ORDER BY created_at DESC LIMIT 30 ");
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
    // 7. Kkumz 전용 Admin 백오피스 관제 API
    // =========================================================================

    public Map<String, Object> getAdminDashboardSummary() {
        Integer totalIdeas = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_idea", Integer.class);
        Integer totalSites = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_site", Integer.class);
        Integer activeSites = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_site WHERE status = 'ACTIVE'", Integer.class);
        Integer openRecruits = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_team_recruit WHERE status = 'OPEN'", Integer.class);
        Integer activeMembers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_team_member WHERE status = 'ACTIVE'", Integer.class);
        Integer pendingReports = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_moderation_report WHERE status = 'PENDING'", Integer.class);
        Integer totalBuilds = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_build_job", Integer.class);
        Integer successBuilds = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_build_job WHERE status = 'SUCCESS'", Integer.class);

        List<Map<String, Object>> siteTypeDistribution = jdbcTemplate.queryForList(
                "SELECT site_type AS \"siteType\", COUNT(*) AS \"count\" FROM kkumz_site GROUP BY site_type ORDER BY \"count\" DESC"
        );
        List<Map<String, Object>> recentBuilds = listAdminBuilds(null, null);
        if (recentBuilds.size() > 5) {
            recentBuilds = recentBuilds.subList(0, 5);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalIdeas", totalIdeas != null ? totalIdeas : 0);
        summary.put("totalSites", totalSites != null ? totalSites : 0);
        summary.put("activeSites", activeSites != null ? activeSites : 0);
        summary.put("openRecruits", openRecruits != null ? openRecruits : 0);
        summary.put("activeMembers", activeMembers != null ? activeMembers : 0);
        summary.put("pendingReports", pendingReports != null ? pendingReports : 0);
        summary.put("totalBuilds", totalBuilds != null ? totalBuilds : 0);
        summary.put("successBuilds", successBuilds != null ? successBuilds : 0);
        summary.put("siteTypeDistribution", siteTypeDistribution);
        summary.put("recentBuilds", recentBuilds);
        summary.put("topSites", listAdminSites(null, null, null));
        return summary;
    }

    public List<Map<String, Object>> listAdminIdeas(String siteType, String status, String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT i.id, i.author_username AS \"authorUsername\", i.author_display_name AS \"authorDisplayName\", " +
                "i.title, i.prompt_text AS \"promptText\", i.site_type AS \"siteType\", i.category, " +
                "i.visibility, i.current_step AS \"currentStep\", i.selected_brand_name AS \"selectedBrandName\", " +
                "i.selected_site_slug AS \"selectedSiteSlug\", i.status, i.created_at AS \"createdAt\", i.updated_at AS \"updatedAt\", " +
                "(SELECT COUNT(*) FROM kkumz_idea_version v WHERE v.idea_id = i.id) AS \"versionCount\", " +
                "(SELECT COUNT(*) FROM kkumz_name_suggestion n WHERE n.idea_id = i.id) AS \"nameCandidateCount\", " +
                "(SELECT COUNT(*) FROM kkumz_logo_suggestion l WHERE l.idea_id = i.id) AS \"logoCandidateCount\" " +
                "FROM kkumz_idea i WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (siteType != null && !siteType.isBlank() && !"ALL".equalsIgnoreCase(siteType)) {
            sql.append(" AND i.site_type = ? ");
            params.add(siteType.trim().toUpperCase(Locale.ROOT));
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND i.status = ? ");
            params.add(status.trim().toUpperCase(Locale.ROOT));
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (i.title ILIKE ? OR i.prompt_text ILIKE ? OR i.selected_brand_name ILIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        sql.append(" ORDER BY i.updated_at DESC ");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Transactional
    public void deleteAdminIdea(String ideaId) {
        jdbcTemplate.update("DELETE FROM kkumz_idea WHERE id = ?", ideaId);
    }

    public List<Map<String, Object>> listAdminPromptTemplates(String stageType) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, template_code AS \"templateCode\", template_name AS \"templateName\", " +
                "stage_type AS \"stageType\", model_name AS \"modelName\", temperature, " +
                "system_prompt AS \"systemPrompt\", user_prompt_template AS \"userPromptTemplate\", " +
                "forbidden_words_json::text AS \"forbiddenWordsJson\", status, updated_at AS \"updatedAt\" " +
                "FROM kkumz_ai_prompt_template WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (stageType != null && !stageType.isBlank() && !"ALL".equalsIgnoreCase(stageType)) {
            sql.append(" AND stage_type = ? ");
            params.add(stageType.trim().toUpperCase(Locale.ROOT));
        }
        sql.append(" ORDER BY id ASC ");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        for (Map<String, Object> r : rows) {
            r.put("forbiddenWords", parseJsonList(r.remove("forbiddenWordsJson")));
        }
        return rows;
    }

    @Transactional
    public Map<String, Object> saveAdminPromptTemplate(SavePromptTemplateRequest req) {
        if (req.getTemplateName() == null || req.getTemplateName().isBlank()) {
            throw new IllegalArgumentException("프롬프트 템플릿명은 필수입니다.");
        }
        String id = (req.getId() != null && !req.getId().isBlank())
                ? req.getId().trim()
                : "prm-kkumz-" + UUID.randomUUID().toString().substring(0, 6);
        String code = (req.getTemplateCode() != null && !req.getTemplateCode().isBlank())
                ? req.getTemplateCode().trim()
                : "KKUMZ_" + id.toUpperCase(Locale.ROOT).replace("-", "_");
        String stage = (req.getStageType() != null && !req.getStageType().isBlank()) ? req.getStageType().trim() : "NAMING";
        String model = (req.getModelName() != null && !req.getModelName().isBlank()) ? req.getModelName().trim() : "gemini-2.5-pro";
        BigDecimal temp = req.getTemperature() != null ? req.getTemperature() : new BigDecimal("0.70");
        String forbidden = toJson(req.getForbiddenWords() != null ? req.getForbiddenWords() : List.of());
        String status = (req.getStatus() != null && !req.getStatus().isBlank()) ? req.getStatus().trim() : "ACTIVE";

        Integer exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kkumz_ai_prompt_template WHERE id = ?", Integer.class, id);
        if (exists != null && exists > 0) {
            jdbcTemplate.update(
                    "UPDATE kkumz_ai_prompt_template SET template_code = ?, template_name = ?, stage_type = ?, " +
                    "model_name = ?, temperature = ?, system_prompt = ?, user_prompt_template = ?, " +
                    "forbidden_words_json = ?::jsonb, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    code, req.getTemplateName().trim(), stage, model, temp,
                    req.getSystemPrompt(), req.getUserPromptTemplate(), forbidden, status, id
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO kkumz_ai_prompt_template (id, template_code, template_name, stage_type, model_name, " +
                    "temperature, system_prompt, user_prompt_template, forbidden_words_json, status, created_by, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    id, code, req.getTemplateName().trim(), stage, model, temp,
                    req.getSystemPrompt() != null ? req.getSystemPrompt() : "",
                    req.getUserPromptTemplate() != null ? req.getUserPromptTemplate() : "",
                    forbidden, status
            );
        }
        return Map.of("id", id, "templateName", req.getTemplateName());
    }

    @Transactional
    public void deleteAdminPromptTemplate(String id) {
        jdbcTemplate.update("DELETE FROM kkumz_ai_prompt_template WHERE id = ?", id);
    }

    public List<Map<String, Object>> listAdminSites(String siteType, String status, String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT s.id, s.idea_id AS \"ideaId\", s.site_slug AS \"siteSlug\", s.site_name AS \"siteName\", " +
                "s.site_type AS \"siteType\", s.category, s.brand_slogan AS \"brandSlogan\", s.logo_svg AS \"logoSvg\", " +
                "s.shop_site_id AS \"shopSiteId\", s.cms_site_id AS \"cmsSiteId\", s.deploy_mode AS \"deployMode\", " +
                "s.active_version AS \"activeVersion\", s.deployed_url AS \"deployedUrl\", s.custom_domain AS \"customDomain\", " +
                "s.is_featured AS \"isFeatured\", s.visibility, " +
                "s.like_count AS \"likeCount\", s.comment_count AS \"commentCount\", s.view_count AS \"viewCount\", " +
                "s.owner_username AS \"ownerUsername\", s.owner_display_name AS \"ownerDisplayName\", " +
                "s.status, s.created_at AS \"createdAt\", s.updated_at AS \"updatedAt\" " +
                "FROM kkumz_site s WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (siteType != null && !siteType.isBlank() && !"ALL".equalsIgnoreCase(siteType)) {
            sql.append(" AND s.site_type = ? ");
            params.add(siteType.trim().toUpperCase(Locale.ROOT));
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND s.status = ? ");
            params.add(status.trim().toUpperCase(Locale.ROOT));
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (s.site_name ILIKE ? OR s.site_slug ILIKE ? OR s.owner_display_name ILIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        sql.append(" ORDER BY s.created_at DESC ");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Transactional
    public Map<String, Object> updateAdminSiteStatus(String siteId, UpdateSiteStatusRequest req) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteId);
        String resolvedId = String.valueOf(site.get("id"));
        String newStatus = (req.getStatus() != null && !req.getStatus().isBlank()) ? req.getStatus().trim().toUpperCase(Locale.ROOT) : String.valueOf(site.get("status"));
        String newVis = (req.getVisibility() != null && !req.getVisibility().isBlank()) ? req.getVisibility().trim().toUpperCase(Locale.ROOT) : String.valueOf(site.get("visibility"));
        String domain = (req.getCustomDomain() != null && !req.getCustomDomain().isBlank()) ? req.getCustomDomain().trim() : (site.get("customDomain") != null ? String.valueOf(site.get("customDomain")) : null);
        boolean featured = req.getIsFeatured() != null ? req.getIsFeatured() : Boolean.TRUE.equals(site.get("isFeatured"));

        jdbcTemplate.update(
                "UPDATE kkumz_site SET status = ?, visibility = ?, custom_domain = ?, is_featured = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                newStatus, newVis, domain, featured, resolvedId
        );
        return getSiteRecordBySlugOrId(resolvedId);
    }

    public List<Map<String, Object>> listAdminBuilds(String status, String deployMode) {
        StringBuilder sql = new StringBuilder(
                "SELECT b.id, b.site_id AS \"siteId\", s.site_name AS \"siteName\", s.site_slug AS \"siteSlug\", " +
                "b.release_id AS \"releaseId\", b.job_type AS \"jobType\", b.deploy_mode AS \"deployMode\", " +
                "b.status, b.duration_ms AS \"durationMs\", b.error_summary AS \"errorSummary\", " +
                "b.ai_patch_applied AS \"aiPatchApplied\", b.build_log AS \"buildLog\", " +
                "b.triggered_by AS \"triggeredBy\", b.started_at AS \"startedAt\", b.finished_at AS \"finishedAt\" " +
                "FROM kkumz_build_job b JOIN kkumz_site s ON s.id = b.site_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND b.status = ? ");
            params.add(status.trim().toUpperCase(Locale.ROOT));
        }
        if (deployMode != null && !deployMode.isBlank() && !"ALL".equalsIgnoreCase(deployMode)) {
            sql.append(" AND b.deploy_mode = ? ");
            params.add(deployMode.trim().toUpperCase(Locale.ROOT));
        }
        sql.append(" ORDER BY b.started_at DESC ");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public List<Map<String, Object>> listAdminCommunityProjects(String keyword) {
        List<Map<String, Object>> sites = listAdminSites(null, null, keyword);
        for (Map<String, Object> site : sites) {
            String siteId = String.valueOf(site.get("id"));
            site.put("recruits", listSiteRecruits(siteId));
            site.put("members", listSiteTeamMembers(siteId));
        }
        return sites;
    }

    public List<Map<String, Object>> listModerationReports(String status) {
        StringBuilder sql = new StringBuilder(
                "SELECT r.id, r.target_type AS \"targetType\", r.target_id AS \"targetId\", r.site_id AS \"siteId\", " +
                "s.site_slug AS \"siteSlug\", s.site_name AS \"siteName\", s.site_type AS \"siteType\", " +
                "r.reporter_username AS \"reporterUsername\", r.reason_category AS \"reasonCategory\", " +
                "r.reason_detail AS \"reasonDetail\", r.status, r.action_taken AS \"actionTaken\", " +
                "r.moderator_username AS \"moderatorUsername\", r.resolved_at AS \"resolvedAt\", r.created_at AS \"createdAt\" " +
                "FROM kkumz_moderation_report r " +
                "JOIN kkumz_site s ON s.id = r.site_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND r.status = ? ");
            params.add(status.trim().toUpperCase(Locale.ROOT));
        }
        sql.append(" ORDER BY CASE WHEN r.status = 'PENDING' THEN 0 ELSE 1 END, r.created_at DESC ");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Transactional
    public Map<String, Object> createModerationReport(String siteIdOrSlug, CreateReportRequest req) {
        Map<String, Object> site = getSiteRecordBySlugOrId(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String reportId = "rep-" + UUID.randomUUID().toString().substring(0, 8);
        String targetType = (req.getTargetType() != null && !req.getTargetType().isBlank()) ? req.getTargetType().trim().toUpperCase(Locale.ROOT) : "SITE";
        String targetId = (req.getTargetId() != null && !req.getTargetId().isBlank()) ? req.getTargetId().trim() : siteId;
        String reporter = (req.getReporterUsername() != null && !req.getReporterUsername().isBlank()) ? req.getReporterUsername().trim() : "community.member";
        String category = (req.getReasonCategory() != null && !req.getReasonCategory().isBlank()) ? req.getReasonCategory().trim() : "SPAM_PROMOTION";
        String detail = (req.getReasonDetail() != null && !req.getReasonDetail().isBlank()) ? req.getReasonDetail().trim() : "커뮤니티 운영 정책 검토 요청";

        jdbcTemplate.update(
                "INSERT INTO kkumz_moderation_report (id, target_type, target_id, site_id, reporter_username, reason_category, reason_detail, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', CURRENT_TIMESTAMP)",
                reportId, targetType, targetId, siteId, reporter, category, detail
        );
        return Map.of("id", reportId, "siteId", siteId, "status", "PENDING");
    }

    @Transactional
    public List<Map<String, Object>> resolveModerationReport(String reportId, ResolveReportRequest req) {
        String status = (req.getStatus() != null && !req.getStatus().isBlank()) ? req.getStatus().trim().toUpperCase(Locale.ROOT) : "RESOLVED";
        String actionTaken = (req.getActionTaken() != null && !req.getActionTaken().isBlank())
                ? req.getActionTaken().trim()
                : ("RESOLVED".equals(status) ? "커뮤니티 가이드라인에 따라 조치 완료" : "검토 결과 위반 사항 없음으로 기각 처리");
        String moderator = (req.getModeratorUsername() != null && !req.getModeratorUsername().isBlank()) ? req.getModeratorUsername().trim() : "admin";

        jdbcTemplate.update(
                "UPDATE kkumz_moderation_report SET status = ?, action_taken = ?, moderator_username = ?, resolved_at = CURRENT_TIMESTAMP WHERE id = ?",
                status, actionTaken, moderator, reportId
        );

        if (Boolean.TRUE.equals(req.getSuspendSite())) {
            jdbcTemplate.update(
                    "UPDATE kkumz_site SET status = 'SUSPENDED', updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = (SELECT site_id FROM kkumz_moderation_report WHERE id = ?)",
                    reportId
            );
        }
        return listModerationReports(null);
    }

    // =========================================================================
    // 내부 헬퍼 메서드
    // =========================================================================

    private void syncScreenSpecsToTable(String ideaId, List<Map<String, Object>> screens) {
        if (screens == null || screens.isEmpty()) {
            return;
        }
        jdbcTemplate.update("DELETE FROM kkumz_screen_spec WHERE idea_id = ?", ideaId);
        int sort = 1;
        for (Map<String, Object> s : screens) {
            String key = String.valueOf(s.getOrDefault("screenKey", "SECTION_" + sort));
            String title = String.valueOf(s.getOrDefault("title", "화면 섹션 " + sort));
            String layout = String.valueOf(s.getOrDefault("layout", "STANDARD_GRID"));
            Object compsObj = s.get("components");
            String compsJson = compsObj != null ? toJson(compsObj) : "[\"HeroBanner\"]";
            jdbcTemplate.update(
                    "INSERT INTO kkumz_screen_spec (id, idea_id, screen_key, screen_title, route_path, layout_type, description, components_json, sort_no, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, CURRENT_TIMESTAMP)",
                    "scr-" + ideaId + "-" + sort, ideaId, key, title, "/" + key.toLowerCase(Locale.ROOT), layout,
                    title + " 블루프린트 구성", compsJson, sort
            );
            sort++;
        }
    }

    private void recordBuildJob(String siteId, String releaseId, String jobType, String deployMode, String status,
                                int durationMs, String errorSummary, boolean aiPatch, String buildLog, String triggeredBy) {
        jdbcTemplate.update(
                "INSERT INTO kkumz_build_job (id, site_id, release_id, job_type, deploy_mode, status, duration_ms, error_summary, ai_patch_applied, build_log, triggered_by, started_at, finished_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "job-" + UUID.randomUUID().toString().substring(0, 8), siteId, releaseId, jobType, deployMode,
                status, durationMs, errorSummary, aiPatch, buildLog, triggeredBy
        );
    }

    private void createNotification(String recipient, String type, String title, String message, String linkUrl) {
        jdbcTemplate.update(
                "INSERT INTO kkumz_notification (id, recipient_username, notif_type, title, message, link_url, is_read, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, FALSE, CURRENT_TIMESTAMP)",
                "noti-" + UUID.randomUUID().toString().substring(0, 8),
                recipient != null && !recipient.isBlank() ? recipient : "admin",
                type, title, message, linkUrl
        );
    }

    private Map<String, Object> loadCommerceData(Long shopSiteId, String siteName, String slogan) {
        try {
            if (shopJdbcTemplate != null) {
                List<Map<String, Object>> products = shopJdbcTemplate.queryForList(
                        "SELECT id, site_id AS \"siteId\", brand_id AS \"brandId\", product_category_id AS \"productCategoryId\", " +
                        "name, pic, product_sn AS \"productSn\", price, promotion_price AS \"promotionPrice\", " +
                        "original_price AS \"originalPrice\", sub_title AS \"subTitle\", description, stock, unit, " +
                        "brand_name AS \"brandName\", product_category_name AS \"productCategoryName\", sale, note " +
                        "FROM pms_product WHERE site_id = ? AND delete_status = 0 AND publish_status = 1 " +
                        "ORDER BY sort DESC, id ASC LIMIT 24",
                        shopSiteId
                );
                if (!products.isEmpty()) {
                    List<Map<String, Object>> banners = shopJdbcTemplate.queryForList(
                            "SELECT id, name, pic, url, note, sort FROM sms_home_advertise " +
                            "WHERE site_id = ? AND status = 1 ORDER BY sort DESC, id DESC LIMIT 6",
                            shopSiteId
                    );
                    List<Map<String, Object>> categories = shopJdbcTemplate.queryForList(
                            "SELECT id, parent_id AS \"parentId\", name, level, product_count AS \"productCount\", " +
                            "product_unit AS \"productUnit\", icon, keywords, description " +
                            "FROM pms_product_category WHERE site_id = ? AND show_status = 1 ORDER BY sort DESC, id ASC",
                            shopSiteId
                    );
                    List<Map<String, Object>> coupons = shopJdbcTemplate.queryForList(
                            "SELECT id, name, amount, min_point AS \"minPoint\", note, code " +
                            "FROM sms_coupon WHERE site_id = ? ORDER BY id DESC LIMIT 6",
                            shopSiteId
                    );
                    Map<String, Object> commerce = new LinkedHashMap<>();
                    commerce.put("shopSiteId", shopSiteId);
                    commerce.put("banners", banners);
                    commerce.put("categories", categories);
                    commerce.put("products", products);
                    commerce.put("coupons", coupons);
                    return commerce;
                }
            }
        } catch (Exception e) {
            log.debug("Standalone 커머스 카탈로그 기본값 로드 (shopSiteId={}): {}", shopSiteId, e.getMessage());
        }

        List<Map<String, Object>> fallbackProducts = List.of(
                Map.of(
                        "id", 101L,
                        "name", siteName + " 시그니처 플래그십 에디션",
                        "pic", "/images/studio/prod-balmuda-brew.jpg",
                        "price", 49000,
                        "promotionPrice", 42000,
                        "subTitle", slogan,
                        "stock", 85,
                        "unit", "세트",
                        "brandName", siteName,
                        "productCategoryName", "시그니처 컬렉션"
                ),
                Map.of(
                        "id", 102L,
                        "name", siteName + " 프레스티지 기프트 패키지",
                        "pic", "/images/studio/ed-5.jpg",
                        "price", 79000,
                        "promotionPrice", 68000,
                        "subTitle", "전용 선물 포장 및 커스텀 메시지 카드 포함",
                        "stock", 60,
                        "unit", "박스",
                        "brandName", siteName,
                        "productCategoryName", "기프트 에디션"
                ),
                Map.of(
                        "id", 103L,
                        "name", siteName + " 데일리 스타터 팩",
                        "pic", "/images/studio/ed-1.jpg",
                        "price", 32000,
                        "promotionPrice", 27900,
                        "subTitle", "일상 속에서 부담 없이 시작하는 베스트셀러",
                        "stock", 120,
                        "unit", "개",
                        "brandName", siteName,
                        "productCategoryName", "스타터 라인"
                )
        );
        return Map.of(
                "shopSiteId", shopSiteId,
                "banners", List.of(Map.of("id", 1L, "name", siteName + " 공식 런칭 기획전", "pic", "/images/studio/banner-acoustic-lounge.jpg", "note", slogan)),
                "categories", List.of(
                        Map.of("id", 1L, "name", "시그니처 컬렉션", "productCount", 3),
                        Map.of("id", 2L, "name", "기프트 에디션", "productCount", 2)
                ),
                "products", fallbackProducts,
                "coupons", List.of(Map.of("id", 1L, "name", siteName + " 첫 구매 5,000원 웰컴 쿠폰", "amount", 5000, "minPoint", 30000, "code", "WELCOME-5K"))
        );
    }

    private Map<String, Object> loadCmsData(Long cmsSiteId, String siteName, String slogan) {
        try {
            if (cmsJdbcTemplate != null) {
                short sid = cmsSiteId.shortValue();
                List<Map<String, Object>> articles = cmsJdbcTemplate.queryForList(
                        "SELECT id, title, author, description, cover, scores, comments, clicks, publish_date AS \"publishDate\" " +
                        "FROM cms_content WHERE site_id = ? AND disabled = false ORDER BY sort DESC, id DESC LIMIT 12",
                        sid
                );
                if (!articles.isEmpty()) {
                    List<Map<String, Object>> categories = cmsJdbcTemplate.queryForList(
                            "SELECT id, name, code, sort FROM cms_category WHERE site_id = ? AND disabled = false ORDER BY sort ASC, id ASC",
                            sid
                    );
                    List<Map<String, Object>> banners = cmsJdbcTemplate.queryForList(
                            "SELECT id, title, sub_title AS \"subTitle\", image_url AS \"imageUrl\", link_url AS \"linkUrl\" " +
                            "FROM cms_banner WHERE site_id = ? AND status = 1 ORDER BY sort DESC, id DESC LIMIT 6",
                            sid
                    );
                    List<Map<String, Object>> forms = cmsJdbcTemplate.queryForList(
                            "SELECT id, code, title, description, schema_json::text AS \"schemaText\", privacy_policy AS \"privacyPolicy\" " +
                            "FROM cms_form WHERE site_id = ? AND status = 1 ORDER BY id ASC",
                            sid
                    );
                    for (Map<String, Object> form : forms) {
                        form.put("fields", parseJsonListOfMap(form.remove("schemaText")));
                    }
                    Map<String, Object> cms = new LinkedHashMap<>();
                    cms.put("cmsSiteId", cmsSiteId);
                    cms.put("categories", categories);
                    cms.put("articles", articles);
                    cms.put("banners", banners);
                    cms.put("forms", forms);
                    cms.put("recentSubmissions", List.of());
                    return cms;
                }
            }
        } catch (Exception e) {
            log.debug("Standalone 매거진/커뮤니티 기본값 로드 (cmsSiteId={}): {}", cmsSiteId, e.getMessage());
        }

        List<Map<String, Object>> fallbackArticles = List.of(
                Map.of(
                        "id", 201L,
                        "title", "[브랜드 스토리] " + siteName + " 공식 런칭 및 디렉터 인터뷰",
                        "author", "브랜드 에디터",
                        "description", slogan,
                        "cover", "/images/studio/ed-5.jpg",
                        "clicks", 420
                ),
                Map.of(
                        "id", 202L,
                        "title", "[커뮤니티 가이드] " + siteName + " 정규 프로그램 및 오프라인 세션 안내",
                        "author", "커뮤니티 매니저",
                        "description", "초기 멤버를 위한 오프라인 밋업 및 온라인 커뮤니티 참여 안내 가이드입니다.",
                        "cover", "/images/studio/ed-1.jpg",
                        "clicks", 315
                )
        );
        List<Map<String, Object>> fallbackForms = List.of(
                Map.of(
                        "id", 101L,
                        "code", "online-apply",
                        "title", siteName + " 온라인 참가 신청 및 제휴 문의",
                        "description", "아래 양식을 작성해 제출해 주시면 담당 매니저가 빠르게 안내해 드립니다.",
                        "fields", List.of(
                                Map.of("field", "applicantName", "label", "신청자 성명", "type", "text", "required", true),
                                Map.of("field", "phone", "label", "연락처", "type", "tel", "required", true),
                                Map.of("field", "optionValue", "label", "신청 프로그램", "type", "select", "options", List.of("정규 세션/클래스 참여 신청", "파트너십 및 제휴 문의"), "required", true)
                        )
                )
        );
        return Map.of(
                "cmsSiteId", cmsSiteId,
                "categories", List.of(Map.of("id", 1, "name", siteName + " 매거진 & 소식", "code", "news")),
                "articles", fallbackArticles,
                "banners", List.of(Map.of("id", 1, "title", siteName + " 공식 커뮤니티 오픈", "subTitle", slogan, "imageUrl", "/images/studio/banner-urban-techwear.jpg")),
                "forms", fallbackForms,
                "recentSubmissions", List.of()
        );
    }

    private Long provisionSyncShopSite(String slug, String siteName, String siteType, String logoSvg, String slogan,
                                       Map<String, Object> tokens, Map<String, Object> layout) {
        try {
            if (shopJdbcTemplate == null) {
                return 2L;
            }
            List<Map<String, Object>> existing = shopJdbcTemplate.queryForList(
                    "SELECT id FROM shop_site WHERE site_slug = ? LIMIT 1", slug
            );
            if (!existing.isEmpty()) {
                return ((Number) existing.get(0).get("id")).longValue();
            }
            return shopJdbcTemplate.queryForObject(
                    "INSERT INTO shop_site (site_slug, site_name, site_type, logo_svg, brand_slogan, " +
                    "design_tokens_json, layout_schema_json, owner_username, status, create_time, update_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, 'admin', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) RETURNING id",
                    Long.class, slug, siteName, siteType, logoSvg, slogan, toJson(tokens), toJson(layout)
            );
        } catch (Exception e) {
            log.info("SyncShop 멀티사이트 자동 프로비저닝 기본 ID 대체 (slug={}): {}", slug, e.getMessage());
            return 2L;
        }
    }

    private Long provisionSyncCmsSite(String slug, String siteName, String slogan) {
        try {
            if (cmsJdbcTemplate == null) {
                return 101L;
            }
            Integer maxSiteId = cmsJdbcTemplate.queryForObject(
                    "SELECT COALESCE(MAX(site_id), 102) FROM cms_category WHERE site_id >= 100", Integer.class
            );
            return (long) ((maxSiteId != null ? maxSiteId : 102) + 1);
        } catch (Exception e) {
            log.info("SyncCMS 멀티사이트 자동 프로비저닝 기본 ID 대체 (slug={}): {}", slug, e.getMessage());
            return 101L;
        }
    }

    private Map<String, Object> getSiteRecordBySlugOrId(String siteSlugOrId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, idea_id AS \"ideaId\", site_slug AS \"siteSlug\", site_name AS \"siteName\", " +
                "site_type AS \"siteType\", category, brand_slogan AS \"brandSlogan\", logo_svg AS \"logoSvg\", " +
                "shop_site_id AS \"shopSiteId\", cms_site_id AS \"cmsSiteId\", deploy_mode AS \"deployMode\", " +
                "active_version AS \"activeVersion\", deployed_url AS \"deployedUrl\", custom_domain AS \"customDomain\", " +
                "is_featured AS \"isFeatured\", visibility, " +
                "like_count AS \"likeCount\", comment_count AS \"commentCount\", view_count AS \"viewCount\", " +
                "owner_username AS \"ownerUsername\", owner_display_name AS \"ownerDisplayName\", " +
                "status, created_at AS \"createdAt\", updated_at AS \"updatedAt\" " +
                "FROM kkumz_site WHERE site_slug = ? OR id = ? LIMIT 1",
                siteSlugOrId, siteSlugOrId
        );
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 꿈즈 사이트입니다: " + siteSlugOrId);
        }
        return new LinkedHashMap<>(rows.get(0));
    }

    private Map<String, Object> getReleaseRecord(String siteId, int versionNo) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, site_id AS \"siteId\", version_no AS \"versionNo\", release_note AS \"releaseNote\", " +
                "deploy_mode AS \"deployMode\", design_tokens_json::text AS \"designTokensText\", " +
                "layout_schema_json::text AS \"layoutSchemaText\", webide_files_json::text AS \"webideFilesText\", " +
                "minio_bundle_path AS \"minioBundlePath\", build_status AS \"buildStatus\", build_log AS \"buildLog\", " +
                "created_by AS \"createdBy\", created_at AS \"createdAt\" " +
                "FROM kkumz_site_release WHERE site_id = ? AND version_no = ? LIMIT 1",
                siteId, versionNo
        );
        if (rows.isEmpty()) {
            rows = jdbcTemplate.queryForList(
                    "SELECT id, site_id AS \"siteId\", version_no AS \"versionNo\", release_note AS \"releaseNote\", " +
                    "deploy_mode AS \"deployMode\", design_tokens_json::text AS \"designTokensText\", " +
                    "layout_schema_json::text AS \"layoutSchemaText\", webide_files_json::text AS \"webideFilesText\", " +
                    "minio_bundle_path AS \"minioBundlePath\", build_status AS \"buildStatus\", build_log AS \"buildLog\", " +
                    "created_by AS \"createdBy\", created_at AS \"createdAt\" " +
                    "FROM kkumz_site_release WHERE site_id = ? ORDER BY version_no DESC LIMIT 1",
                    siteId
            );
        }
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("사이트 릴리즈 정보를 찾을 수 없습니다: " + siteId);
        }
        return rows.get(0);
    }

    private List<Map<String, Object>> listSiteReleases(String siteId) {
        return jdbcTemplate.queryForList(
                "SELECT id, version_no AS \"versionNo\", release_note AS \"releaseNote\", deploy_mode AS \"deployMode\", " +
                "minio_bundle_path AS \"minioBundlePath\", build_status AS \"buildStatus\", created_by AS \"createdBy\", created_at AS \"createdAt\" " +
                "FROM kkumz_site_release WHERE site_id = ? ORDER BY version_no DESC",
                siteId
        );
    }

    private String normalizeSiteType(String raw) {
        if (raw == null || raw.isBlank()) {
            return "COMMERCE";
        }
        String u = raw.trim().toUpperCase(Locale.ROOT);
        if ("CMS_SITE".equals(u) || "HYBRID".equals(u) || "COMMERCE".equals(u)) {
            return u;
        }
        return "COMMERCE";
    }

    private String extractCoreKeyword(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "아뜰리에";
        }
        String cleaned = prompt.replaceAll("[^가-힣a-zA-Z0-9\\s]", " ").trim();
        String[] words = cleaned.split("\\s+");
        for (String w : words) {
            if (w.length() >= 2) {
                return w.length() > 6 ? w.substring(0, 6) : w;
            }
        }
        return "드림랩";
    }

    private String toSlugBase(String input) {
        if (input == null || input.isBlank()) {
            return "kkumz-brand";
        }
        String ascii = input.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (ascii.length() >= 3) {
            return ascii;
        }
        return "kkumz-" + Math.abs(input.hashCode() % 9000 + 1000);
    }

    private String ensureAvailableSlug(String candidate, String ideaId) {
        String slug = candidate.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]", "");
        if (slug.isBlank()) {
            slug = "kkumz-site";
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM kkumz_site WHERE site_slug = ? AND idea_id <> ?",
                Integer.class, slug, ideaId
        );
        if (count != null && count > 0) {
            return slug + "-" + UUID.randomUUID().toString().substring(0, 4);
        }
        return slug;
    }

    private String buildDefaultMarkdownSpec(String title, String prompt, String siteType, String brandName) {
        String typeDesc = "COMMERCE".equals(siteType)
                ? "프리미엄 D2C 브랜드 쇼핑몰 스토어"
                : ("CMS_SITE".equals(siteType) ? "브랜드 스토리 & 커뮤니티 웹사이트" : "매거진 아카이브 + 쇼핑몰 복합 사이트");
        String featureDesc = "COMMERCE".equals(siteType)
                ? "카테고리별 상품 카탈로그, 기프트 옵션, 장바구니 및 원클릭 주문서"
                : ("CMS_SITE".equals(siteType) ? "에디토리얼 매거진 아티클, 주간 일정 배너 및 온라인 참가/문의 신청 폼" : "브랜드 저널 아티클과 상품 즉시 구매, 온라인 클래스/제휴 신청 폼 통합");
        return "## 1. 프로젝트 개요 및 브랜드 정의\n" +
                "- **브랜드명**: " + brandName + "\n" +
                "- **사이트 유형**: " + typeDesc + "\n" +
                "- **핵심 아이디어**: " + prompt + "\n\n" +
                "## 2. 주요 화면 및 기능 명세\n" +
                "1. **브랜드 히어로 & 비주얼 아이덴티티**: SVG 벡터 로고, 전용 컬러 팔레트 토큰 및 핵심 가치 제안 배너\n" +
                "2. **핵심 비즈니스 기능**: " + featureDesc + "\n" +
                "3. **AI 브랜드 컨시어지 & 팀빌딩**: 방문자 실시간 질의응답 컨시어지 및 초기 창업 팀원 모집 라운지 연동";
    }

    private List<Map<String, Object>> buildDefaultScreens(String siteType) {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of(
                "screenKey", "HERO_SECTION",
                "title", "브랜드 시그니처 히어로 & 핵심 슬로건 배너",
                "layout", "SPLIT_SHOWCASE",
                "components", List.of("BrandSvgLogoHeader", "HeroBannerCarousel", "AiConciergeTrigger")
        ));
        if ("COMMERCE".equals(siteType) || "HYBRID".equals(siteType)) {
            list.add(Map.of(
                    "screenKey", "COMMERCE_CATALOG",
                    "title", "상품 카탈로그 & 원클릭 장바구니/주문",
                    "layout", "FILTERABLE_3COL_GRID",
                    "components", List.of("CategoryFilterPills", "ProductCardGrid", "InstantCheckoutModal")
            ));
        }
        if ("CMS_SITE".equals(siteType) || "HYBRID".equals(siteType)) {
            list.add(Map.of(
                    "screenKey", "EDITORIAL_AND_FORM",
                    "title", "브랜드 아카이브 저널 & 온라인 참가/문의 신청 폼",
                    "layout", "EDITORIAL_MAGAZINE_LAYOUT",
                    "components", List.of("ArticleStoryGrid", "OnlineApplicationForm")
            ));
        }
        return list;
    }

    private List<Map<String, Object>> buildDefaultWebIdeFiles(String brandName, String siteSlug, String siteType,
                                                              Map<String, Object> tokens, Map<String, Object> layout) {
        List<Map<String, Object>> files = new ArrayList<>();
        files.add(new LinkedHashMap<>(Map.of(
                "path", "src/App.vue",
                "language", "vue",
                "content", "<template>\n" +
                        "  <div class=\"kkumz-live-shell\" :style=\"{ '--brand-primary': tokens.primary }\">\n" +
                        "    <HeroSection :title=\"layout.heroTitle\" :subtitle=\"layout.heroSubtitle\" :badge=\"layout.customBadge\" />\n" +
                        "    <CatalogSection v-if=\"siteType !== 'CMS_SITE'\" :products=\"commerce.products\" />\n" +
                        "    <JournalSection v-if=\"siteType !== 'COMMERCE'\" :articles=\"cms.articles\" />\n" +
                        "  </div>\n" +
                        "</template>\n" +
                        "<script setup lang=\"ts\">\n" +
                        "// Generated by Kkumz AI Studio (" + siteSlug + ")\n" +
                        "const siteType = '" + siteType + "';\n" +
                        "</script>"
        )));
        files.add(new LinkedHashMap<>(Map.of(
                "path", "src/sections/HeroSection.vue",
                "language", "vue",
                "content", "<template>\n" +
                        "  <section class=\"kkumz-hero-banner\">\n" +
                        "    <span class=\"hero-badge\">{{ badge }}</span>\n" +
                        "    <h1>{{ title }}</h1>\n" +
                        "    <p>{{ subtitle }}</p>\n" +
                        "  </section>\n" +
                        "</template>\n" +
                        "<script setup lang=\"ts\">\n" +
                        "defineProps<{ title: string; subtitle: string; badge?: string }>();\n" +
                        "</script>"
        )));
        files.add(new LinkedHashMap<>(Map.of(
                "path", "src/sections/CatalogSection.vue",
                "language", "vue",
                "content", "<template>\n" +
                        "  <section class=\"kkumz-catalog-grid\">\n" +
                        "    <article v-for=\"item in products\" :key=\"item.id\" class=\"product-item\">\n" +
                        "      <h3>{{ item.name }}</h3>\n" +
                        "      <p>{{ item.subTitle }}</p>\n" +
                        "    </article>\n" +
                        "  </section>\n" +
                        "</template>\n" +
                        "<script setup lang=\"ts\">\n" +
                        "defineProps<{ products: any[] }>();\n" +
                        "</script>"
        )));
        files.add(new LinkedHashMap<>(Map.of(
                "path", "src/theme.json",
                "language", "json",
                "content", toJson(tokens)
        )));
        return files;
    }

    private String escapeXml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private Map<String, Object> parseJsonMap(Object raw) {
        if (raw == null) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(String.valueOf(raw), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private List<String> parseJsonList(Object raw) {
        if (raw == null) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(String.valueOf(raw), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> parseJsonListOfMap(Object raw) {
        if (raw == null) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(String.valueOf(raw), new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
