package com.empasy.sync.kkumz.domain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Kkumz 도메인 서비스 전반에서 공유하는 순수 JSON 파싱, 텍스트 정규화, SVG·CSS 새니타이징,
 * 문서 텍스트 추출 및 Git 키/아카이브 보조 정적 유틸리티 모음입니다.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class KkumzJsonAndTextUtils {

    private static final ObjectMapper SHARED_MAPPER = new ObjectMapper();

    private static final Set<String> FORM_FEATURE_CODES = Set.of(
            "CMS_DYNAMIC_BOOKING_FORM",
            "CMS_DYNAMIC_FORM",
            "CMS_B2B_INQUIRY_FORM",
            "CMS_VIP_CONSULT_BOOKING"
    );

    static ObjectMapper lenientMapper() {
        return JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
                .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
                .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
                .build();
    }

    static List<Map<String, Object>> parseAiJsonArray(String rawJson, String stageName) {
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

    static List<String> validateWebIdeSourceFiles(List<Map<String, Object>> files) {
        List<String> errors = new ArrayList<>();
        for (Map<String, Object> f : files) {
            String path = String.valueOf(f.getOrDefault("path", "unknown"));
            String content = String.valueOf(f.getOrDefault("content", ""));
            if (path.endsWith(".json")) {
                try {
                    SHARED_MAPPER.readTree(content);
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

    static List<Map<String, Object>> buildWebIdeFilesFromScreens(
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

    static List<String> parseJsonStringList(String raw) {
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

    static boolean hasEngineFeature(List<String> enabledFeatures, List<Map<String, Object>> catalog, String engineCode) {
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

    static boolean hasBothShopAndCmsFeatures(List<String> enabledFeatures, List<Map<String, Object>> catalog) {
        return hasEngineFeature(enabledFeatures, catalog, "SYNC_SHOP")
                && hasEngineFeature(enabledFeatures, catalog, "SYNC_CMS");
    }

    static boolean isConversationalRawPrompt(String text) {
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

    static String stripConversationalTail(String text) {
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

    static String cleanConversationalPromptToHeadline(String rawPrompt, String brandName, String category) {
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

    static String resolveBrandSlogan(
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

    static boolean hasAnyFormFeature(List<String> enabledFeatures) {
        if (enabledFeatures == null || enabledFeatures.isEmpty()) {
            return false;
        }
        for (String code : enabledFeatures) {
            if (FORM_FEATURE_CODES.contains(code)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> castMapList(List<?> rawList) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object o : rawList) {
            if (o instanceof Map<?, ?> m) {
                result.add(new LinkedHashMap<>((Map<String, Object>) m));
            }
        }
        return result;
    }

    static String resolveThemeArchetype(String explicit, String siteType, String rawCategory, List<String> enabledFeatures) {
        if (explicit != null && !explicit.isBlank()) {
            return explicit.trim().toUpperCase(Locale.ROOT);
        }
        String cat = rawCategory != null ? rawCategory.trim().toUpperCase(Locale.ROOT) : "";
        if ("TECH_HARDWARE".equals(cat) || (enabledFeatures != null && enabledFeatures.contains("SHOP_SPEC_COMPARISON"))) {
            return "DARK_TECH_SHOWCASE";
        }
        if ("COMMUNITY_MEDIA".equals(cat) || "CMS_SITE".equalsIgnoreCase(siteType)) {
            return "COMMUNITY_KINETIC";
        }
        if (enabledFeatures != null && enabledFeatures.contains("SHOP_PRICING_MATRIX")) {
            return "SCENIC_HOSPITALITY";
        }
        if ("FOOD_BEVERAGE".equals(cat) || "COMMERCE".equalsIgnoreCase(siteType)) {
            return "EDITORIAL_BOTANICAL";
        }
        return "MAGAZINE_HYBRID";
    }

    static String resolveDefaultHeroStyleForArchetype(String archetype, String siteType) {
        if ("DARK_TECH_SHOWCASE".equals(archetype)) {
            return "PROMO_COMMERCE";
        }
        if ("EDITORIAL_BOTANICAL".equals(archetype)) {
            return "EDITORIAL_STORY";
        }
        if ("MAGAZINE_HYBRID".equals(archetype)) {
            return "SPLIT_VISUAL";
        }
        return "FULL_SCENIC";
    }

    static String resolveSurfaceMode(Map<String, Object> designTokens, String archetype) {
        if (designTokens != null && designTokens.get("surfaceMode") != null
                && !String.valueOf(designTokens.get("surfaceMode")).isBlank()) {
            return String.valueOf(designTokens.get("surfaceMode")).trim().toUpperCase(Locale.ROOT);
        }
        if ("DARK_TECH_SHOWCASE".equals(archetype)) {
            return "DARK";
        }
        if ("COMMUNITY_KINETIC".equals(archetype)) {
            return "LIGHT";
        }
        return "WARM";
    }

    static String resolveFontPreset(Map<String, Object> designTokens, String archetype) {
        if (designTokens != null && designTokens.get("fontPreset") != null
                && !String.valueOf(designTokens.get("fontPreset")).isBlank()) {
            return String.valueOf(designTokens.get("fontPreset")).trim().toUpperCase(Locale.ROOT);
        }
        if ("DARK_TECH_SHOWCASE".equals(archetype)) {
            return "TECH_MONO";
        }
        if ("COMMUNITY_KINETIC".equals(archetype)) {
            return "BOLD_SANS";
        }
        return "SERIF_EDITORIAL";
    }

    static String extractSanitizedCustomCss(List<Map<String, Object>> webideFiles) {
        if (webideFiles == null || webideFiles.isEmpty()) {
            return "";
        }
        StringBuilder css = new StringBuilder();
        for (Map<String, Object> file : webideFiles) {
            String path = file.get("path") != null ? String.valueOf(file.get("path")).toLowerCase(Locale.ROOT) : "";
            if (path.endsWith(".css")) {
                String content = file.get("content") != null ? String.valueOf(file.get("content")) : "";
                if (!content.isBlank()) {
                    String safe = content
                            .replaceAll("(?i)<\\s*/?\\s*script[^>]*>", "")
                            .replaceAll("(?i)<\\s*/?\\s*style[^>]*>", "")
                            .replaceAll("(?i)@import[^;]+;", "")
                            .replaceAll("(?i)javascript\\s*:", "")
                            .replaceAll("(?i)expression\\s*\\(", "");
                    css.append(safe).append('\n');
                }
            }
        }
        return css.toString().trim();
    }

    static List<String> defaultPricingColumns() {
        List<String> cols = new ArrayList<>();
        cols.add("구분");
        cols.add("기준 인원");
        cols.add("비수기 주중");
        cols.add("비수기 주말");
        cols.add("성수기/연휴");
        cols.add("포함 혜택");
        return cols;
    }

    static String summarizeTitle(String promptText) {
        String cleaned = promptText.replaceAll("\\s+", " ").trim();
        return cleaned.length() > 42 ? cleaned.substring(0, 42) + "..." : cleaned;
    }

    static String replaceBrandTextInSvg(String svgCode, String targetBrandName, List<String> knownBrandCandidates) {
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

    static List<Map<String, Object>> parseScreenMapList(String json) {
        if (json == null || json.isBlank() || "null".equalsIgnoreCase(json)) {
            return new ArrayList<>();
        }
        try {
            return SHARED_MAPPER.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    static String normalizeSlug(String raw) {
        String slug = raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9-]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
        return slug.isBlank() ? "brand-" + UUID.randomUUID().toString().substring(0, 6) : slug;
    }

    static String escapeXml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    static String toJson(Object obj) {
        if (obj == null) {
            return "{}";
        }
        try {
            return SHARED_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalStateException("JSON 직렬화 실패", e);
        }
    }

    static Map<String, Object> parseJsonObject(String json) {
        if (json == null || json.isBlank() || "null".equalsIgnoreCase(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return SHARED_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
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

    static List<Object> parseJsonList(String json) {
        if (json == null || json.isBlank() || "null".equalsIgnoreCase(json)) {
            return new ArrayList<>();
        }
        try {
            return SHARED_MAPPER.readValue(json, new TypeReference<List<Object>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    static String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    static String stripExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    static String resolveMimeType(String providedMime, String ext) {
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

    static String classifyAssetType(String requestedType, String origName, String ext, String mimeType) {
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

    static boolean isTextOrDocumentFile(String ext, String mimeType) {
        if (ext != null && ext.matches("^(txt|md|csv|tsv|json|html|htm|xml|docx|pdf)$")) {
            return true;
        }
        return mimeType != null && (mimeType.startsWith("text/") || mimeType.matches(".*(json|xml|pdf).*"));
    }

    static String extractDocumentText(byte[] bytes, String ext, String mimeType) {
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

    static List<Map<String, Object>> normalizeAndFormatWebIdeFiles(List<?> rawFiles) {
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
                    Object parsed = SHARED_MAPPER.readValue(content, Object.class);
                    copy.put("content", SHARED_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(parsed));
                } catch (Exception ignored) {
                }
            }
            result.add(copy);
        }
        return result;
    }

    static Map<String, String> generateEd25519SshKeyPair(String comment) {
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

    static String buildDotEnvExampleContent(String siteSlug, String siteAccessKey, String siteSecretKey) {
        return """
                # Kkumz Site Developer SDK Runtime Credentials
                VITE_KKUMZ_API_TARGET=http://localhost:9580
                VITE_KKUMZ_SITE_SLUG=%s
                VITE_KKUMZ_SITE_ACCESS_KEY=%s
                VITE_KKUMZ_SITE_SECRET_KEY=%s
                """.formatted(siteSlug, siteAccessKey, siteSecretKey).trim();
    }

    static String calculateGitCommitSha(String siteId, String repoName, String commitMsg, List<Map<String, Object>> files) {
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

    static void addZipTextEntry(ZipOutputStream zos, String entryName, String content) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        byte[] bytes = (content != null ? content : "").getBytes(StandardCharsets.UTF_8);
        zos.write(bytes);
        zos.closeEntry();
    }

    static String sanitizeGitIdentifier(String raw, String fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        String cleaned = raw.trim().replaceAll("[^a-zA-Z0-9._-]", "-").replaceAll("-+", "-").replaceAll("^-|-$", "");
        return cleaned.isBlank() ? fallback : cleaned;
    }

    static String escapeJsonString(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
