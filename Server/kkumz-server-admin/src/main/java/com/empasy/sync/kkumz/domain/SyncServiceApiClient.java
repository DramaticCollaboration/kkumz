package com.empasy.sync.kkumz.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kkumz -> SyncCMS / SyncShop 서비스 간 표준 REST API 클라이언트
 * X-Sync-Access-Key / X-Sync-Secret-Key 인증 헤더를 동적 주입하며,
 * kkumz_api_access_key 테이블의 OUTBOUND 키 설정 및 호출 통계를 실시간 연동한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SyncServiceApiClient {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kkumz.integration.cms-base-url:http://localhost:18085}")
    private String defaultCmsBaseUrl;

    @Value("${kkumz.integration.cms-access-key:ak_cms_kkumz_live_01}")
    private String defaultCmsAccessKey;

    @Value("${kkumz.integration.cms-secret-key:sk_cms_kkumz_secret_2026_prod_99}")
    private String defaultCmsSecretKey;

    @Value("${kkumz.integration.shop-base-url:http://localhost:8501}")
    private String defaultShopBaseUrl;

    @Value("${kkumz.integration.shop-access-key:ak_shop_kkumz_live_01}")
    private String defaultShopAccessKey;

    @Value("${kkumz.integration.shop-secret-key:sk_shop_kkumz_secret_2026_prod_88}")
    private String defaultShopSecretKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Getter
    @Builder
    public static class ServiceEndpointCredentials {
        private final String keyId;
        private final String serviceCode;
        private final String baseUrl;
        private final String accessKey;
        private final String secretKey;
    }

    public ServiceEndpointCredentials resolveCredentials(String serviceCode) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    """
                    SELECT id, service_code, base_url, access_key, secret_key
                    FROM kkumz_api_access_key
                    WHERE direction = 'OUTBOUND'
                      AND service_code = ?
                      AND status = 'ACTIVE'
                    ORDER BY updated_at DESC
                    LIMIT 1
                    """,
                    serviceCode
            );
            if (!rows.isEmpty()) {
                Map<String, Object> row = rows.get(0);
                String dbBaseUrl = row.get("base_url") != null ? String.valueOf(row.get("base_url")).trim() : "";
                String fallbackBaseUrl = "SYNC_CMS".equalsIgnoreCase(serviceCode) ? defaultCmsBaseUrl : defaultShopBaseUrl;
                return ServiceEndpointCredentials.builder()
                        .keyId(String.valueOf(row.get("id")))
                        .serviceCode(serviceCode)
                        .baseUrl(StringUtils.hasText(dbBaseUrl) ? trimTrailingSlash(dbBaseUrl) : trimTrailingSlash(fallbackBaseUrl))
                        .accessKey(String.valueOf(row.get("access_key")))
                        .secretKey(String.valueOf(row.get("secret_key")))
                        .build();
            }
        } catch (Exception e) {
            log.debug("kkumz_api_access_key 조회 생략 또는 폴백 사용 ({}): {}", serviceCode, e.getMessage());
        }

        if ("SYNC_CMS".equalsIgnoreCase(serviceCode)) {
            return ServiceEndpointCredentials.builder()
                    .keyId(null)
                    .serviceCode("SYNC_CMS")
                    .baseUrl(trimTrailingSlash(defaultCmsBaseUrl != null ? defaultCmsBaseUrl : "http://localhost:18085"))
                    .accessKey(defaultCmsAccessKey != null ? defaultCmsAccessKey : "ak_cms_kkumz_live_01")
                    .secretKey(defaultCmsSecretKey != null ? defaultCmsSecretKey : "sk_cms_kkumz_secret_2026_prod_99")
                    .build();
        }
        return ServiceEndpointCredentials.builder()
                .keyId(null)
                .serviceCode("SYNC_SHOP")
                .baseUrl(trimTrailingSlash(defaultShopBaseUrl != null ? defaultShopBaseUrl : "http://localhost:8501"))
                .accessKey(defaultShopAccessKey != null ? defaultShopAccessKey : "ak_shop_kkumz_live_01")
                .secretKey(defaultShopSecretKey != null ? defaultShopSecretKey : "sk_shop_kkumz_secret_2026_prod_88")
                .build();
    }

    private void recordKeyUsage(String keyId) {
        if (!StringUtils.hasText(keyId)) {
            return;
        }
        try {
            jdbcTemplate.update(
                    "UPDATE kkumz_api_access_key SET last_used_at = NOW(), call_count = COALESCE(call_count, 0) + 1 WHERE id = ?",
                    keyId
            );
        } catch (Exception e) {
            log.debug("API Key 사용 이력 업데이트 실패 ({}): {}", keyId, e.getMessage());
        }
    }

    /**
     * 1. 서비스 연동 상태 및 Access Key 실시간 검증
     */
    public Map<String, Object> verifyServiceConnection(
            String serviceCode,
            String baseUrlOverride,
            String accessKeyOverride,
            String secretKeyOverride,
            String keyIdToRecord) {
        ServiceEndpointCredentials creds = resolveCredentials(serviceCode);
        String baseUrl = StringUtils.hasText(baseUrlOverride) ? trimTrailingSlash(baseUrlOverride) : creds.getBaseUrl();
        String accessKey = StringUtils.hasText(accessKeyOverride) ? accessKeyOverride.trim() : creds.getAccessKey();
        String secretKey = StringUtils.hasText(secretKeyOverride) ? secretKeyOverride.trim() : creds.getSecretKey();

        String verifyPath = "SYNC_CMS".equalsIgnoreCase(serviceCode)
                ? "/api/v1/cms/multisite/auth-verify"
                : "/api/v1/shop/portal/site/auth-verify";

        long startMs = System.currentTimeMillis();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("serviceCode", serviceCode);
        result.put("endpoint", baseUrl + verifyPath);
        result.put("accessKey", accessKey);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + verifyPath))
                    .timeout(Duration.ofSeconds(6))
                    .header("Accept", "application/json")
                    .header("X-Sync-Access-Key", accessKey)
                    .header("X-Sync-Secret-Key", secretKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            long latencyMs = System.currentTimeMillis() - startMs;
            result.put("latencyMs", latencyMs);
            result.put("httpStatus", response.statusCode());

            Map<String, Object> parsed = parseJsonMap(response.body());
            Object codeObj = parsed.get("code");
            int apiCode = codeObj instanceof Number num ? num.intValue() : response.statusCode();
            boolean ok = response.statusCode() == 200 && apiCode == 200;

            result.put("verified", ok);
            result.put("message", ok ? "연동 인증 및 헬스체크 성공" : String.valueOf(parsed.getOrDefault("message", "인증 실패")));
            if (parsed.get("data") instanceof Map<?, ?> dataMap) {
                result.put("remoteData", dataMap);
            }
            if (ok) {
                recordKeyUsage(StringUtils.hasText(keyIdToRecord) ? keyIdToRecord : creds.getKeyId());
            }
            return result;
        } catch (Exception e) {
            result.put("verified", false);
            result.put("latencyMs", System.currentTimeMillis() - startMs);
            result.put("message", "원격 서비스 연결 실패: " + e.getMessage());
            return result;
        }
    }

    /**
     * 2. SyncShop 멀티사이트 부트스트랩 조회 (상품 + 최근 주문)
     */
    public Map<String, Object> fetchShopBootstrap(Long shopSiteId, String siteSlug) {
        String identifier = StringUtils.hasText(siteSlug)
                ? encodeSegment(siteSlug)
                : (shopSiteId != null ? String.valueOf(shopSiteId) : "default");
        return callGet("SYNC_SHOP", "/api/v1/shop/portal/site/" + identifier + "/bootstrap", shopSiteId, siteSlug);
    }

    /**
     * 3. SyncShop 신규 브랜드 스토어 자동 프로비저닝
     */
    public Map<String, Object> provisionShopSite(
            String siteSlug,
            String siteName,
            String brandSlogan,
            String themeColor,
            String category) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("siteSlug", siteSlug);
        payload.put("siteName", siteName);
        payload.put("brandSlogan", brandSlogan);
        payload.put("themeColor", themeColor);
        payload.put("category", category);
        return callPost("SYNC_SHOP", "/api/v1/shop/portal/site/provision", null, siteSlug, payload);
    }

    /**
     * 4. SyncShop 업로드 이미지·가격표 기반 상품 일괄 동기화
     */
    public Map<String, Object> syncShopProducts(
            Long shopSiteId,
            String siteSlug,
            String siteName,
            List<Map<String, Object>> products) {
        String identifier = StringUtils.hasText(siteSlug)
                ? encodeSegment(siteSlug)
                : (shopSiteId != null ? String.valueOf(shopSiteId) : "default");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("siteName", siteName);
        payload.put("products", products);
        return callPost("SYNC_SHOP", "/api/v1/shop/portal/site/" + identifier + "/products/sync", shopSiteId, siteSlug, payload);
    }

    /**
     * 5. SyncShop 멀티사이트 즉시 주문 생성
     */
    public Map<String, Object> createShopOrder(
            Long shopSiteId,
            String siteSlug,
            Map<String, Object> orderPayload) {
        String identifier = StringUtils.hasText(siteSlug)
                ? encodeSegment(siteSlug)
                : (shopSiteId != null ? String.valueOf(shopSiteId) : "default");
        return callPost("SYNC_SHOP", "/api/v1/shop/portal/site/" + identifier + "/order", shopSiteId, siteSlug, orderPayload);
    }

    /**
     * 6. SyncCMS 멀티사이트 부트스트랩 조회 (아티클 + 배너 + 동적 폼)
     */
    public Map<String, Object> fetchCmsBootstrap(Long cmsSiteId, String siteSlug) {
        String identifier = cmsSiteId != null
                ? String.valueOf(cmsSiteId)
                : (StringUtils.hasText(siteSlug) ? encodeSegment(siteSlug) : "default");
        return callGet("SYNC_CMS", "/api/v1/cms/multisite/" + identifier + "/bootstrap", cmsSiteId, siteSlug);
    }

    /**
     * 7. SyncCMS 신규 멀티사이트 자동 프로비저닝
     */
    public Map<String, Object> provisionCmsSite(
            String siteSlug,
            String siteName,
            String brandSlogan) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("siteSlug", siteSlug);
        payload.put("siteName", siteName);
        payload.put("brandSlogan", brandSlogan);
        return callPost("SYNC_CMS", "/api/v1/cms/multisite/provision", null, siteSlug, payload);
    }

    /**
     * 8. SyncCMS 업로드 미디어·문서 자산 기반 배너/아티클 동기화
     */
    public Map<String, Object> syncCmsContents(
            Long cmsSiteId,
            String siteSlug,
            String siteName,
            String heroImageUrl,
            List<Map<String, Object>> articles) {
        String identifier = cmsSiteId != null
                ? String.valueOf(cmsSiteId)
                : (StringUtils.hasText(siteSlug) ? encodeSegment(siteSlug) : "default");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("siteName", siteName);
        if (StringUtils.hasText(heroImageUrl)) {
            payload.put("heroImageUrl", heroImageUrl);
        }
        payload.put("articles", articles != null ? articles : List.of());
        return callPost("SYNC_CMS", "/api/v1/cms/multisite/" + identifier + "/contents/sync", cmsSiteId, siteSlug, payload);
    }

    /**
     * 9. SyncCMS 멀티사이트 온라인 폼 신청 제출
     */
    public Map<String, Object> submitCmsForm(
            Long cmsSiteId,
            String siteSlug,
            Map<String, Object> formPayload) {
        String identifier = cmsSiteId != null
                ? String.valueOf(cmsSiteId)
                : (StringUtils.hasText(siteSlug) ? encodeSegment(siteSlug) : "default");
        return callPost("SYNC_CMS", "/api/v1/cms/multisite/" + identifier + "/form-submit", cmsSiteId, siteSlug, formPayload);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callGet(String serviceCode, String path, Long siteId, String siteSlug) {
        ServiceEndpointCredentials creds = resolveCredentials(serviceCode);
        String url = creds.getBaseUrl() + path;
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/json")
                    .header("X-Sync-Access-Key", creds.getAccessKey())
                    .header("X-Sync-Secret-Key", creds.getSecretKey());
            if (siteId != null) {
                builder.header("X-Site-Id", String.valueOf(siteId));
            }
            if (StringUtils.hasText(siteSlug)) {
                builder.header("X-Site-Slug", siteSlug);
            }
            HttpResponse<String> response = httpClient.send(builder.GET().build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            recordKeyUsage(creds.getKeyId());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                Map<String, Object> envelope = parseJsonMap(response.body());
                Object codeObj = envelope.get("code");
                if (codeObj instanceof Number num && num.intValue() != 200) {
                    throw new IllegalStateException(serviceCode + " API returned error code " + num + ": " + envelope.get("message"));
                }
                if (envelope.get("data") instanceof Map<?, ?> dataMap) {
                    return new LinkedHashMap<>((Map<String, Object>) dataMap);
                }
                return envelope;
            }
            throw new IllegalStateException(serviceCode + " HTTP " + response.statusCode() + " from " + url);
        } catch (Exception e) {
            log.warn("[SyncServiceApiClient] GET {} 호출 실패: {}", url, e.getMessage());
            throw new IllegalStateException(serviceCode + " API 연동 오류: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callPost(String serviceCode, String path, Long siteId, String siteSlug, Map<String, Object> payload) {
        ServiceEndpointCredentials creds = resolveCredentials(serviceCode);
        String url = creds.getBaseUrl() + path;
        try {
            String jsonBody = objectMapper.writeValueAsString(payload != null ? payload : Map.of());
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .header("X-Sync-Access-Key", creds.getAccessKey())
                    .header("X-Sync-Secret-Key", creds.getSecretKey());
            if (siteId != null) {
                builder.header("X-Site-Id", String.valueOf(siteId));
            }
            if (StringUtils.hasText(siteSlug)) {
                builder.header("X-Site-Slug", siteSlug);
            }
            HttpResponse<String> response = httpClient.send(
                    builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8)).build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            recordKeyUsage(creds.getKeyId());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                Map<String, Object> envelope = parseJsonMap(response.body());
                Object codeObj = envelope.get("code");
                if (codeObj instanceof Number num && num.intValue() != 200) {
                    throw new IllegalStateException(serviceCode + " API returned error code " + num + ": " + envelope.get("message"));
                }
                if (envelope.get("data") instanceof Map<?, ?> dataMap) {
                    return new LinkedHashMap<>((Map<String, Object>) dataMap);
                }
                return envelope;
            }
            throw new IllegalStateException(serviceCode + " HTTP " + response.statusCode() + " from " + url);
        } catch (Exception e) {
            log.warn("[SyncServiceApiClient] POST {} 호출 실패: {}", url, e.getMessage());
            throw new IllegalStateException(serviceCode + " API 연동 오류: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> parseJsonMap(String body) {
        if (!StringUtils.hasText(body)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private String trimTrailingSlash(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String encodeSegment(String value) {
        return URLEncoder.encode(value.trim(), StandardCharsets.UTF_8);
    }
}
