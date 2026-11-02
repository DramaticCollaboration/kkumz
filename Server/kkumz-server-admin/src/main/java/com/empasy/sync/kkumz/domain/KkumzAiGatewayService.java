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

    @Value("${kkumz.ai.llm.model:}")
    private String defaultModelName;

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
        return dispatchMessages(messages, modelName, temperature);
    }

    public String dispatchMessages(List<Msg> messages, String modelName, double temperature) {
        Model registeredModel = agentScopeModelProvider != null ? agentScopeModelProvider.getIfAvailable() : null;
        if (registeredModel != null) {
            ChatResponse response = registeredModel.stream(messages, List.of(), GenerateOptions.builder().build())
                    .blockLast(Duration.ofSeconds(30));
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

            String requestBody = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + llmApiKey.trim())
                    .header("X-Agent-Id", "KKUMZ_STUDIO")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(45))
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
        trimmed = trimmed.replaceFirst("^```(?:json|markdown|html|xml|svg)?\\s*\\r?\\n?", "");
        trimmed = trimmed.replaceFirst("\\r?\\n?```$", "");
        return trimmed.trim();
    }
}
