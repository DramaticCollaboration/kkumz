package com.empasy.sync.kkumz.domain;

import com.empasy.sync.kkumz.domain.KkumzDTO.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static com.empasy.sync.kkumz.domain.KkumzJsonAndTextUtils.*;

/**
 * Kkumz AI 브랜드 네이밍, 병렬 벡터 SVG 로고 생성, 브랜드 확정 및 자가치유 기본 토큰 생성을 담당하는 협력 컴포넌트입니다.
 */
@Slf4j
@RequiredArgsConstructor
class KkumzBrandKitSupport {

    private final KkumzService root;

    Map<String, Object> generateNameSuggestions(String ideaId, GenerateNamesRequest req) {
        root.accessMeta().assertIdeaEditable(ideaId);
        Map<String, Object> idea = root.ideaSpec().getIdeaDetail(ideaId);
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
        String rawJson = root.ensureAiGateway().executeStagePrompt("NAMING", Map.of(
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

        root.jdbcTemplate().update("DELETE FROM kkumz_name_suggestion WHERE idea_id = ?", ideaId);

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

            Integer taken = root.jdbcTemplate().queryForObject(
                    "SELECT COUNT(*) FROM kkumz_site WHERE site_slug = ? AND idea_id <> ?",
                    Integer.class, slug, ideaId
            );
            boolean available = (taken == null || taken == 0);
            boolean isSelected = (idx == 0);

            root.jdbcTemplate().update("""
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
                root.jdbcTemplate().update("""
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

        return root.ideaSpec().getIdeaDetail(ideaId);
    }

    Map<String, Object> generateLogoSuggestions(String ideaId, GenerateLogosRequest req) {
        root.accessMeta().assertIdeaEditable(ideaId);
        Map<String, Object> idea = root.ideaSpec().getIdeaDetail(ideaId);
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
                return root.ideaSpec().getIdeaDetail(ideaId);
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
        List<Map<String, Object>> latestIdeaRows = root.jdbcTemplate().queryForList(
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

        root.jdbcTemplate().update("DELETE FROM kkumz_logo_suggestion WHERE idea_id = ?", ideaId);

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

            root.jdbcTemplate().update("""
                    INSERT INTO kkumz_logo_suggestion (
                        id, idea_id, style_name, svg_code, color_palette_json, typography_json, tags_json, is_selected, created_at
                    ) VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, CURRENT_TIMESTAMP)
                    """,
                    "logo-" + ideaId + "-" + (idx + 1),
                    ideaId, styleName, svgCode, toJson(paletteObj), toJson(typoObj), toJson(tagsObj), isSelected
            );

            if (isSelected) {
                root.jdbcTemplate().update("""
                        UPDATE kkumz_idea
                        SET selected_logo_svg = ?, design_tokens_json = ?::jsonb, current_step = GREATEST(current_step, 3), updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                        svgCode, toJson(paletteObj), ideaId
                );
            }
            idx++;
        }

        return root.ideaSpec().getIdeaDetail(ideaId);
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

        KkumzAiGatewayService gateway = root.ensureAiGateway();

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
        List<Map<String, Object>> existingLogos = root.jdbcTemplate().queryForList(
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
            root.jdbcTemplate().update("UPDATE kkumz_logo_suggestion SET svg_code = ? WHERE id = ?", synced, lid);
            if (Boolean.TRUE.equals(logoRow.get("isSelected"))) {
                root.jdbcTemplate().update(
                        "UPDATE kkumz_idea SET selected_logo_svg = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                        synced, ideaId
                );
            }
        }
    }

    Map<String, Object> selectBrand(String ideaId, SelectBrandRequest req) {
        root.accessMeta().assertIdeaEditable(ideaId);
        Map<String, Object> idea = root.ideaSpec().getIdeaDetail(ideaId);
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
            root.jdbcTemplate().update("UPDATE kkumz_name_suggestion SET is_selected = (id = ?) WHERE idea_id = ?",
                    req.getNameSuggestionId(), ideaId);
            List<Map<String, Object>> rows = root.jdbcTemplate().queryForList(
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
                    root.jdbcTemplate().update("""
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
            root.jdbcTemplate().update("UPDATE kkumz_name_suggestion SET is_selected = FALSE WHERE idea_id = ?", ideaId);
            String slugVal = normalizeSlug(customSiteSlug != null ? customSiteSlug : customBrandName);
            String customId = "name-custom-" + ideaId;
            root.jdbcTemplate().update("DELETE FROM kkumz_name_suggestion WHERE id = ?", customId);
            root.jdbcTemplate().update("""
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
            root.jdbcTemplate().update("UPDATE kkumz_logo_suggestion SET is_selected = (id = ?) WHERE idea_id = ?",
                    req.getLogoSuggestionId(), ideaId);
            List<Map<String, Object>> rows = root.jdbcTemplate().queryForList(
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
                    root.jdbcTemplate().update(
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
        List<Map<String, Object>> allLogos = root.jdbcTemplate().queryForList(
                "SELECT id, svg_code AS \"svgCode\" FROM kkumz_logo_suggestion WHERE idea_id = ?",
                ideaId
        );
        for (Map<String, Object> logoRow : allLogos) {
            String lid = String.valueOf(logoRow.get("id"));
            String lsvg = String.valueOf(logoRow.getOrDefault("svgCode", ""));
            String syncedSvg = (req.getLogoSuggestionId() != null && req.getLogoSuggestionId().equals(lid) && req.getLogoSvg() != null && !req.getLogoSvg().isBlank())
                    ? finalSvg
                    : replaceBrandTextInSvg(lsvg, finalBrand, knownBrandNames);
            root.jdbcTemplate().update("UPDATE kkumz_logo_suggestion SET svg_code = ? WHERE id = ?", syncedSvg, lid);
        }

        root.jdbcTemplate().update("""
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

        Integer slugConflict = root.jdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM kkumz_site WHERE site_slug = ? AND idea_id <> ?",
                Integer.class, finalSlug, ideaId
        );
        if (slugConflict == null || slugConflict == 0) {
            root.jdbcTemplate().update("""
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

        return root.ideaSpec().getIdeaDetail(ideaId);
    }

    @SuppressWarnings("unchecked")
    String resolveScaffoldBrandName(Map<String, Object> idea) {
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
    String resolveScaffoldSiteSlug(Map<String, Object> idea, String brandName) {
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
            List<Map<String, Object>> existingByBrand = root.jdbcTemplate().queryForList(
                    "SELECT site_slug AS \"siteSlug\" FROM kkumz_site WHERE site_name = ? ORDER BY updated_at DESC LIMIT 1",
                    brandName
            );
            if (!existingByBrand.isEmpty() && existingByBrand.get(0).get("siteSlug") != null) {
                String foundSlug = String.valueOf(existingByBrand.get(0).get("siteSlug")).trim();
                if (!foundSlug.isBlank()) {
                    return foundSlug;
                }
            }
            List<Map<String, Object>> existingIdeaByBrand = root.jdbcTemplate().queryForList(
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
    Map<String, Object> resolveOrProvisionDesignTokens(
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
            List<Map<String, Object>> siblingIdeas = root.jdbcTemplate().queryForList(
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
    String resolveOrProvisionLogoSvg(
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
            List<Map<String, Object>> siblingSites = root.jdbcTemplate().queryForList(
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
            root.jdbcTemplate().update("""
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
}
