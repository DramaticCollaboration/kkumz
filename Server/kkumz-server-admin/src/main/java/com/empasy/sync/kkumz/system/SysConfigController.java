package com.empasy.sync.kkumz.system;

import com.empasy.sync.core.common.config.SysConfig;
import com.empasy.sync.core.common.config.SystemConfigService;
import com.empasy.sync.kkumz.common.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kkumz 시스템 동적 환경설정 관리 컨트롤러 (/api/v1/kkumz/sys/config)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/kkumz/sys/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SystemConfigService systemConfigService;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigUpdateRequest {
        @NotBlank(message = "설정 키(configKey)는 필수입니다.")
        private String configKey;
        private String configValue;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigConnectionTestRequest {
        private String testType;
        private String endpoint;
        private String credential;
        private Map<String, String> extraParams;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigConnectionTestResponse {
        private boolean success;
        private long latencyMs;
        private String message;
        private Map<String, Object> details;
    }

    @GetMapping("/list")
    public Result<List<SysConfig>> list(@RequestParam(name = "category", required = false) String category) {
        return Result.OK(systemConfigService.listByCategory(category));
    }

    @GetMapping("/categories")
    public Result<List<String>> categories() {
        return Result.OK(systemConfigService.listCategories());
    }

    @GetMapping("/public")
    public Result<Map<String, String>> listPublic(@RequestParam(name = "category", required = false) String category) {
        return Result.OK(systemConfigService.listPublicConfigs(category));
    }

    @PostMapping("/save")
    public Result<SysConfig> save(@RequestBody SysConfig dto) {
        return Result.OK(systemConfigService.saveOrUpdateConfig(dto, "admin"));
    }

    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam(name = "configKey") String configKey) {
        systemConfigService.deleteConfig(configKey, "admin");
        return Result.OK("설정 항목이 삭제되었습니다.");
    }

    @PutMapping("/update")
    public Result<String> update(@Valid @RequestBody ConfigUpdateRequest request) {
        systemConfigService.updateConfig(request.getConfigKey(), request.getConfigValue(), "admin");
        return Result.OK("설정이 성공적으로 저장되었습니다.");
    }

    @PutMapping("/updateBatch")
    public Result<String> updateBatch(@Valid @RequestBody List<ConfigUpdateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Result.error("수정할 설정 목록이 비어있습니다.");
        }
        for (ConfigUpdateRequest req : requests) {
            systemConfigService.updateConfig(req.getConfigKey(), req.getConfigValue(), "admin");
        }
        return Result.OK("설정이 일괄 저장되었습니다.");
    }

    @PostMapping("/reset")
    public Result<String> reset(@RequestParam(name = "configKey") String configKey) {
        systemConfigService.resetToDefault(configKey, "admin");
        return Result.OK("기본값으로 복원되었습니다.");
    }

    @PostMapping("/refreshCache")
    public Result<String> refreshCache() {
        systemConfigService.refreshCache();
        return Result.OK("환경설정 캐시가 갱신되었습니다.");
    }

    @PostMapping("/test-connection")
    public Result<ConfigConnectionTestResponse> testConnection(@Valid @RequestBody ConfigConnectionTestRequest request) {
        long startTime = System.currentTimeMillis();
        String testType = request.getTestType() != null ? request.getTestType().toUpperCase() : "HTTP";
        String endpoint = request.getEndpoint();

        if (endpoint == null || endpoint.isBlank()) {
            return Result.error("진단할 엔드포인트 URL이 입력되지 않았습니다.");
        }
        if (!isSafeExternalEndpoint(endpoint)) {
            return Result.error("보안 정책 위반: 내부망·루프백·메타데이터 주소 또는 허용되지 않은 프로토콜은 진단할 수 없습니다.");
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(7))
                    .GET();

            if (request.getCredential() != null && !request.getCredential().isBlank()) {
                reqBuilder.header("Authorization", "Bearer " + request.getCredential());
            }

            HttpResponse<String> response = client.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());
            long latencyMs = System.currentTimeMillis() - startTime;

            Map<String, Object> details = new HashMap<>();
            details.put("statusCode", response.statusCode());
            details.put("endpoint", endpoint);
            details.put("testType", testType);

            boolean isSuccess = (response.statusCode() >= 200 && response.statusCode() < 400)
                    || response.statusCode() == 401 || response.statusCode() == 403;

            String message = isSuccess
                    ? "원격 서비스 연결 성공 (응답 코드: " + response.statusCode() + ", " + latencyMs + "ms)"
                    : "원격 서비스 응답 이상 (응답 코드: " + response.statusCode() + ")";

            ConfigConnectionTestResponse result = ConfigConnectionTestResponse.builder()
                    .success(isSuccess)
                    .latencyMs(latencyMs)
                    .message(message)
                    .details(details)
                    .build();

            return Result.OK(result);
        } catch (Exception e) {
            long latencyMs = System.currentTimeMillis() - startTime;
            log.warn("Connection test failed for endpoint {}: {}", endpoint, e.getMessage());

            Map<String, Object> details = new HashMap<>();
            details.put("endpoint", endpoint);
            details.put("error", e.getClass().getSimpleName());

            ConfigConnectionTestResponse result = ConfigConnectionTestResponse.builder()
                    .success(false)
                    .latencyMs(latencyMs)
                    .message("연결 실패: " + e.getMessage())
                    .details(details)
                    .build();

            return Result.OK(result);
        }
    }

    private boolean isSafeExternalEndpoint(String endpoint) {
        try {
            URI uri = URI.create(endpoint.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                return false;
            }
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return false;
            }
            String lowerHost = host.toLowerCase();
            if ("localhost".equals(lowerHost)
                    || "127.0.0.1".equals(lowerHost)
                    || "0.0.0.0".equals(lowerHost)
                    || "::1".equals(lowerHost)
                    || "[::1]".equals(lowerHost)
                    || "169.254.169.254".equals(lowerHost)
                    || "metadata.google.internal".equals(lowerHost)
                    || lowerHost.endsWith(".internal")
                    || lowerHost.endsWith(".local")) {
                return false;
            }
            InetAddress addr = InetAddress.getByName(host);
            return !(addr.isLoopbackAddress()
                    || addr.isLinkLocalAddress()
                    || addr.isSiteLocalAddress()
                    || addr.isAnyLocalAddress()
                    || addr.isMulticastAddress());
        } catch (Exception e) {
            return false;
        }
    }
}
