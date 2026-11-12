package com.empasy.sync.kkumz.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kkumz AI 스튜디오 및 멀티사이트 에이전트 실행 게이트웨이 (AgentScope Java 기반)
 * - DB(kkumz_ai_prompt_template)에 등록된 단계별 프롬프트 템플릿 및 금지어 정책을 단일 원천으로 조회하여 실행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KkumzAiGatewayService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<Model> agentScopeModelProvider;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Value("${kkumz.ai.llm.base-url:}")
    private String llmBaseUrl;

    @Value("${kkumz.ai.llm.api-key:}")
    private String llmApiKey;

    @Value("${kkumz.ai.llm.model:morph-glm53flash}")
    private String defaultModelName;

    @PostConstruct
    public void synchronizeOptimalStageTemplates() {
        try {
            jdbcTemplate.update("""
                    UPDATE kkumz_ai_prompt_template
                    SET model_name = 'morph-glm53flash', updated_at = CURRENT_TIMESTAMP
                    WHERE model_name IN ('qwen-plus', 'qwen-flash') AND status = 'ACTIVE'
                    """);
            jdbcTemplate.update("""
                    UPDATE kkumz_ai_prompt_template
                    SET model_name = 'morph-glm53flash',
                        system_prompt = '당신은 브랜드 웹사이트 콘텐츠 디렉터이자 커머스 MD입니다. 브랜드명, 상세 프롬프트, 업종 카테고리, 활성화된 기능 코드 목록, 업로드된 사진 캡션 목록을 바탕으로 해당 업종에 부합하는 실제 사이트 초기 데이터(대표 상품/객실/메뉴 3종, 성수기·비수기 요금표 3행, 맞춤 예약 폼 필드 4종, 매거진 아티클 2건, FAQ 3건, 오시는 길 및 주변 코스, 방문객 리뷰 2건)를 간결하고 완성도 높은 순수 JSON 객체로 생성하십시오. 인사말이나 코드펜스 없이 첫 글자가 반드시 "{" 로 시작하고 "}" 로 끝나야 하며, 프롬프트 원문("~만들고 싶어")을 제목/서브타이틀에 그대로 복붙하지 마십시오.',
                        user_prompt_template = '브랜드명: {{brandName}}\n업종 카테고리: {{category}}\n사이트 유형: {{siteType}}\n아이디어 프롬프트: {{promptText}}\n선택된 활성 기능 목록: {{enabledFeatures}}\n업로드 자산 목록: {{assetsSummary}}\n\n[필수 JSON 출력 스키마 — 첫 글자 "{" 부터 마지막 "}" 까지 순수 JSON만 간결하게 출력]\n{\n  "brandSlogan": "감성 브랜드 슬로건 1줄",\n  "customBadge": "상단 공식 배지 문구",\n  "heroTitle": "메인 히어로 헤드라인",\n  "heroSubtitle": "히어로 서브 카피 (원문 복붙 금지)",\n  "ctaText": "히어로 메인 버튼 문구",\n  "heroStyle": "FULL_SCENIC|SPLIT_VISUAL|EDITORIAL_STORY|PROMO_COMMERCE",\n  "conciergeGreeting": "AI 컨시어지 첫 인사말",\n  "products": [\n    {"name": "상품/객실/메뉴명", "subTitle": "핵심 구성 및 매력 1줄 설명", "price": 120000, "promotionPrice": 99000, "unit": "박/세트/건", "stock": 15, "productCategoryName": "분류명", "badge": "BEST"}\n  ],\n  "articles": [\n    {"title": "매거진 제목", "categoryName": "카테고리", "summary": "핵심 요약 1~2문장"}\n  ],\n  "cmsForm": {\n    "title": "맞춤 예약·신청 폼 제목",\n    "description": "예약 안내 설명",\n    "submitText": "예약·신청 접수하기",\n    "fields": [\n      {"name": "applicantName", "label": "예약자 성함", "type": "text", "required": true},\n      {"name": "phone", "label": "연락처", "type": "tel", "required": true},\n      {"name": "visitDate", "label": "희망 방문/이용 일자", "type": "date", "required": true},\n      {"name": "optionValue", "label": "희망 객실·프로그램 선택", "type": "select", "required": true, "options": ["옵션1", "옵션2", "옵션3"]}\n    ]\n  },\n  "featureBlocks": {\n    "pricingMatrix": [\n      {"tier": "구분명", "capacity": "기준 인원", "offPeakWeekday": "100,000원", "offPeakWeekend": "130,000원", "peakSeason": "160,000원", "note": "포함 혜택"}\n    ],\n    "faqItems": [{"q": "자주 묻는 질문", "a": "친절한 답변"}],\n    "locationGuide": {"address": "주소 안내", "parking": "주차 안내", "publicTransit": "대중교통 안내", "nearbyCourses": [{"name": "주변 명소", "distance": "소요시간", "desc": "설명"}]},\n    "reviews": [{"author": "김*연 고객님", "rating": 5, "tag": "재방문 추천", "date": "2026.10", "comment": "실제 방문 후기 1~2문장"}]\n  }\n}',
                        updated_at = CURRENT_TIMESTAMP
                    WHERE id = 'tpl-kkumz-008' OR stage_type = 'SITE_CONTENT_PROVISION'
                    """);
        } catch (Exception e) {
            log.debug("AI 프롬프트 템플릿 최적화 동기화 생략: {}", e.getMessage());
        }
    }

    public Map<String, Object> loadActiveTemplateByStage(String stageType) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id,
                       template_code AS "templateCode",
                       template_name AS "templateName",
                       stage_type AS "stageType",
                       model_name AS "modelName",
                       temperature,
                       system_prompt AS "systemPrompt",
                       user_prompt_template AS "userPromptTemplate",
                       forbidden_words_json::text AS "forbiddenWordsJson"
                FROM kkumz_ai_prompt_template
                WHERE stage_type = ? AND status = 'ACTIVE'
                ORDER BY updated_at DESC
                LIMIT 1
                """, stageType);
        if (rows.isEmpty()) {
            throw new IllegalStateException("활성화된 AI 프롬프트 템플릿이 DB(kkumz_ai_prompt_template)에 존재하지 않습니다: stageType=" + stageType);
        }
        return rows.get(0);
    }

    public List<String> extractForbiddenWords(Map<String, Object> templateRow) {
        if (templateRow == null || templateRow.get("forbiddenWordsJson") == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(String.valueOf(templateRow.get("forbiddenWordsJson")), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("금지어 JSON 파싱에 실패했습니다.", e);
        }
    }

    public void validateAgainstForbiddenWords(String text, List<String> forbiddenWords) {
        if (text == null || text.isBlank() || forbiddenWords == null || forbiddenWords.isEmpty()) {
            return;
        }
        String lower = text.toLowerCase();
        for (String forbidden : forbiddenWords) {
            if (forbidden != null && !forbidden.isBlank() && lower.contains(forbidden.trim().toLowerCase())) {
                throw new IllegalArgumentException("금지어 정책에 위배되는 키워드가 포함되어 있습니다: " + forbidden);
            }
        }
    }

    public String executeStagePrompt(String stageType, Map<String, String> variables) {
        return executeStagePrompt(stageType, variables, null, null);
    }

    public String executeStagePrompt(
            String stageType,
            Map<String, String> variables,
            Duration timeoutOverride,
            Integer maxTokensOverride
    ) {
        Map<String, Object> template = loadActiveTemplateByStage(stageType);
        List<String> forbiddenWords = extractForbiddenWords(template);
        for (String val : variables.values()) {
            validateAgainstForbiddenWords(val, forbiddenWords);
        }

        String systemPrompt = String.valueOf(template.getOrDefault("systemPrompt", ""));
        String userPrompt = String.valueOf(template.getOrDefault("userPromptTemplate", ""));
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            userPrompt = userPrompt.replace(placeholder, value);
        }

        String modelName = String.valueOf(template.getOrDefault("modelName", ""));
        if (modelName.isBlank()) {
            modelName = defaultModelName;
        }
        double temperature = 0.7;
        if (template.get("temperature") instanceof Number num) {
            temperature = num.doubleValue();
        }

        List<Msg> messages = List.of(
                createMsg(MsgRole.SYSTEM, systemPrompt),
                createMsg(MsgRole.USER, userPrompt)
        );
        if (timeoutOverride == null && maxTokensOverride == null) {
            return dispatchMessages(messages, modelName, temperature);
        }
        return dispatchMessages(messages, modelName, temperature, timeoutOverride, maxTokensOverride);
    }

    public String dispatchMessages(List<Msg> messages, String modelName, double temperature) {
        return dispatchMessages(messages, modelName, temperature, Duration.ofSeconds(60), null);
    }

    public String dispatchMessages(
            List<Msg> messages,
            String modelName,
            double temperature,
            Duration timeoutOverride,
            Integer maxTokensOverride
    ) {
        Duration effectiveTimeout = (timeoutOverride != null && !timeoutOverride.isNegative() && !timeoutOverride.isZero())
                ? timeoutOverride
                : Duration.ofSeconds(60);

        Model registeredModel = agentScopeModelProvider != null ? agentScopeModelProvider.getIfAvailable() : null;
        if (registeredModel != null) {
            ChatResponse response = registeredModel.stream(messages, List.of(), GenerateOptions.builder().build())
                    .blockLast(effectiveTimeout);
            if (response != null && response.getContent() != null) {
                StringBuilder sb = new StringBuilder();
                for (ContentBlock block : response.getContent()) {
                    if (block instanceof TextBlock tb) {
                        sb.append(tb.getText());
                    }
                }
                String content = stripCodeFence(sb.toString());
                if (!content.isBlank()) {
                    return content;
                }
            }
        }

        if (llmBaseUrl == null || llmBaseUrl.isBlank()) {
            throw new IllegalStateException("LLM 게이트웨이 URL(kkumz.ai.llm.base-url)이 설정되지 않았습니다.");
        }
        if (llmApiKey == null || llmApiKey.isBlank()) {
            throw new IllegalStateException("LLM API Key(kkumz.ai.llm.api-key)가 설정되지 않았습니다.");
        }
        String resolvedModel = (modelName != null && !modelName.isBlank()) ? modelName : defaultModelName;
        if (resolvedModel == null || resolvedModel.isBlank()) {
            throw new IllegalStateException("LLM 모델명이 설정되지 않았습니다.");
        }

        String endpoint = llmBaseUrl.endsWith("/chat/completions")
                ? llmBaseUrl
                : (llmBaseUrl.endsWith("/") ? llmBaseUrl + "chat/completions" : llmBaseUrl + "/chat/completions");

        try {
            List<Map<String, String>> reqMessages = new ArrayList<>();
            for (Msg msg : messages) {
                String role = "user";
                if (msg.getRole() == MsgRole.SYSTEM) {
                    role = "system";
                } else if (msg.getRole() == MsgRole.ASSISTANT) {
                    role = "assistant";
                }
                reqMessages.add(Map.of("role", role, "content", extractText(msg)));
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", resolvedModel);
            payload.put("messages", reqMessages);
            payload.put("temperature", temperature);
            payload.put("enable_thinking", false);
            if (maxTokensOverride != null && maxTokensOverride > 0) {
                payload.put("max_tokens", maxTokensOverride);
            }

            String requestBody = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + llmApiKey.trim())
                    .header("X-Agent-Id", "KKUMZ_STUDIO")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(effectiveTimeout)
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && !choices.isEmpty()) {
                    String content = choices.get(0).path("message").path("content").asText("");
                    if (!content.isBlank()) {
                        return stripCodeFence(content);
                    }
                }
                throw new IllegalStateException("LLM 게이트웨이 응답에 유효한 choices 콘텐츠가 없습니다.");
            }
            throw new IllegalStateException("LLM 게이트웨이 호출 실패 (HTTP " + response.statusCode() + "): " + response.body());
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("LLM 게이트웨이 통신 중 오류가 발생했습니다 (" + endpoint + "): " + e.getMessage(), e);
        }
    }

    private Msg createMsg(MsgRole role, String text) {
        return Msg.builder()
                .role(role)
                .content(List.of(TextBlock.builder().text(text != null ? text : "").build()))
                .build();
    }

    private String extractText(Msg msg) {
        if (msg == null || msg.getContent() == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ContentBlock block : msg.getContent()) {
            if (block instanceof TextBlock tb) {
                sb.append(tb.getText());
            }
        }
        return sb.toString();
    }

    private String stripCodeFence(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        java.util.regex.Matcher fenceMatcher = java.util.regex.Pattern
                .compile("```(?:json|markdown|html|xml|svg)?\\s*\\r?\\n?([\\s\\S]*?)\\r?\\n?```", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(trimmed);
        if (fenceMatcher.find()) {
            return fenceMatcher.group(1).trim();
        }
        trimmed = trimmed.replaceFirst("^```(?:json|markdown|html|xml|svg)?\\s*\\r?\\n?", "");
        trimmed = trimmed.replaceFirst("\\r?\\n?```$", "");
        return trimmed.trim();
    }
}
