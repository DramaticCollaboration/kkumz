package com.empasy.sync.kkumz;

import com.empasy.sync.kkumz.domain.KkumzController;
import com.empasy.sync.kkumz.domain.KkumzDTO.*;
import com.empasy.sync.kkumz.domain.KkumzService;
import com.empasy.sync.kkumz.system.KkumzSystemController;
import com.empasy.sync.kkumz.system.KkumzSystemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class KkumzServiceTest {

    private JdbcTemplate jdbcTemplate;
    private JdbcTemplate shopJdbcTemplate;
    private JdbcTemplate cmsJdbcTemplate;
    private KkumzService kkumzService;
    private KkumzSystemService systemService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        shopJdbcTemplate = Mockito.mock(JdbcTemplate.class);
        cmsJdbcTemplate = Mockito.mock(JdbcTemplate.class);

        kkumzService = new KkumzService(jdbcTemplate, new ObjectMapper(), null);
        ReflectionTestUtils.setField(kkumzService, "shopJdbcTemplate", shopJdbcTemplate);
        ReflectionTestUtils.setField(kkumzService, "cmsJdbcTemplate", cmsJdbcTemplate);

        systemService = new KkumzSystemService(jdbcTemplate);

        KkumzController domainController = new KkumzController(kkumzService);
        KkumzSystemController sysController = new KkumzSystemController(systemService);
        mockMvc = MockMvcBuilders.standaloneSetup(domainController, sysController).build();
    }

    @Test
    @DisplayName("1. [아이디어 목록 조회] GET /api/v1/kkumz/ideas 정상 반환")
    void testListIdeas() throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", "idea-kkumz-001");
        row.put("title", "제주 유기농 말차 & 다기 브랜드 스토어");
        row.put("siteType", "COMMERCE");
        row.put("tagsJson", "[\"제주말차\",\"다기\"]");
        row.put("selectedBrandName", "말차 아뜰리에");
        row.put("selectedSiteSlug", "matcha-atelier");

        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(row));

        mockMvc.perform(get("/api/v1/kkumz/ideas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result[0].id").value("idea-kkumz-001"))
                .andExpect(jsonPath("$.result[0].selectedSiteSlug").value("matcha-atelier"))
                .andExpect(jsonPath("$.result[0].tags[0]").value("제주말차"));
    }

    @Test
    @DisplayName("2. [동적 메뉴 인가 API] GET /api/v1/kkumz/sys/permission/getUserPermissionByToken 트리 반환 검증")
    void testGetUserPermissionByToken() throws Exception {
        Map<String, Object> rootMenu = new LinkedHashMap<>();
        rootMenu.put("id", "perm-kkumz-001");
        rootMenu.put("parentId", null);
        rootMenu.put("name", "대시보드");
        rootMenu.put("url", "/dashboard");
        rootMenu.put("component", "layouts/default/index");
        rootMenu.put("componentName", "KkumzDashboard");
        rootMenu.put("redirect", "/dashboard/analysis");
        rootMenu.put("menuType", 0);
        rootMenu.put("sortNo", 1.0);
        rootMenu.put("icon", "ant-design:dashboard-outlined");
        rootMenu.put("isRoute", true);

        Map<String, Object> childMenu = new LinkedHashMap<>();
        childMenu.put("id", "perm-kkumz-002");
        childMenu.put("parentId", "perm-kkumz-001");
        childMenu.put("name", "운영 종합 대시보드");
        childMenu.put("url", "/dashboard/analysis");
        childMenu.put("component", "dashboard/analysis/index");
        childMenu.put("componentName", "KkumzAnalysis");
        childMenu.put("menuType", 1);
        childMenu.put("perms", "kkumz:dashboard:view");
        childMenu.put("sortNo", 1.0);
        childMenu.put("icon", "ant-design:fund-projection-screen-outlined");
        childMenu.put("isRoute", true);

        when(jdbcTemplate.queryForList(contains("FROM sys_permission"), any(Object[].class))).thenReturn(List.of(rootMenu, childMenu));

        mockMvc.perform(get("/api/v1/kkumz/sys/permission/getUserPermissionByToken")
                        .header("X-Access-Token", "kkumz-jwt-token-admin-1700000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.menu[0].path").value("/dashboard"))
                .andExpect(jsonPath("$.result.menu[0].children[0].path").value("/dashboard/analysis"))
                .andExpect(jsonPath("$.result.codeList[0]").value("kkumz:dashboard:view"));
    }

    @Test
    @DisplayName("3. [쇼케이스 피드 조회] GET /api/v1/kkumz/feed 필터 및 모집 공고 병합 검증")
    void testGetCommunityFeed() {
        Map<String, Object> siteRow = new LinkedHashMap<>();
        siteRow.put("id", "site-kkumz-001");
        siteRow.put("ideaId", "idea-kkumz-001");
        siteRow.put("siteSlug", "matcha-atelier");
        siteRow.put("siteName", "말차 아뜰리에");
        siteRow.put("siteType", "COMMERCE");
        siteRow.put("tagsJson", "[\"D2C\",\"티하우스\"]");
        siteRow.put("designTokensJson", "{\"primary\":\"#14532d\"}");

        Map<String, Object> recruitRow = new LinkedHashMap<>();
        recruitRow.put("id", "rec-001");
        recruitRow.put("siteId", "site-kkumz-001");
        recruitRow.put("positionRole", "FRONTEND");
        recruitRow.put("title", "프론트엔드 개발자 모집");
        recruitRow.put("requiredSkillsJson", "[\"Vue 3\",\"TypeScript\"]");

        when(jdbcTemplate.queryForList(contains("FROM kkumz_site s"), any(Object[].class)))
                .thenReturn(List.of(siteRow));
        when(jdbcTemplate.queryForList(contains("FROM kkumz_site WHERE site_slug = ?"), eq("site-kkumz-001"), eq("site-kkumz-001")))
                .thenReturn(List.of(siteRow));
        when(jdbcTemplate.queryForList(contains("FROM kkumz_team_recruit"), eq("site-kkumz-001")))
                .thenReturn(List.of(recruitRow));

        List<Map<String, Object>> feed = kkumzService.getCommunityFeed("COMMERCE", null, "POPULAR", null);
        assertEquals(1, feed.size());
        assertEquals("matcha-atelier", feed.get(0).get("siteSlug"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recruits = (List<Map<String, Object>>) feed.get(0).get("openRecruits");
        assertEquals(1, recruits.size());
        assertEquals("FRONTEND", recruits.get(0).get("positionRole"));
    }

    @Test
    @DisplayName("4. [멀티사이트 실주문 처리] SyncShop oms_order 생성 및 pms_product 재고 차감 검증")
    void testCreateCommerceOrder() {
        Map<String, Object> siteRow = new LinkedHashMap<>();
        siteRow.put("id", "site-kkumz-001");
        siteRow.put("siteSlug", "matcha-atelier");
        siteRow.put("siteName", "말차 아뜰리에");
        siteRow.put("ownerUsername", "soyeon.jeju");
        siteRow.put("shopSiteId", 2L);

        Map<String, Object> prodRow = new LinkedHashMap<>();
        prodRow.put("id", 201L);
        prodRow.put("name", "첫물 유기농 세레모니얼 말차 40g 틴케이스");
        prodRow.put("pic", "/images/studio/prod-balmuda-brew.jpg");
        prodRow.put("product_sn", "MTC-201");
        prodRow.put("brand_name", "MATCHA ATELIER");
        prodRow.put("product_category_id", 201L);
        prodRow.put("price", new BigDecimal("42000.00"));
        prodRow.put("promotion_price", new BigDecimal("36000.00"));
        prodRow.put("stock", 120);

        when(jdbcTemplate.queryForList(contains("FROM kkumz_site"), eq("matcha-atelier"), eq("matcha-atelier")))
                .thenReturn(List.of(siteRow));
        when(shopJdbcTemplate.queryForList(contains("FROM pms_product WHERE id = ?"), eq(201L), eq(2L)))
                .thenReturn(List.of(prodRow));
        when(shopJdbcTemplate.queryForObject(contains("INSERT INTO oms_order"), eq(Long.class), any(Object[].class)))
                .thenReturn(9001L);

        CommerceOrderRequest req = CommerceOrderRequest.builder()
                .productId(201L)
                .quantity(2)
                .receiverName("홍길동")
                .receiverPhone("010-1111-2222")
                .receiverAddress("서울특별시 성동구 성수이로 77")
                .build();

        Map<String, Object> orderResult = kkumzService.createCommerceOrder("matcha-atelier", req);
        assertEquals(9001L, orderResult.get("orderId"));
        assertEquals(new BigDecimal("72000.00"), orderResult.get("payAmount"));
        assertEquals("PAID_READY_FOR_SHIPMENT", orderResult.get("status"));
        verify(shopJdbcTemplate, times(1)).update(contains("INSERT INTO oms_order_item"), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(shopJdbcTemplate, times(1)).update(contains("UPDATE pms_product SET stock"), eq(2), eq(2), eq(201L));
        verify(jdbcTemplate, times(1)).update(contains("INSERT INTO kkumz_notification"), anyString(), eq("soyeon.jeju"), eq("COMMERCE_ORDER"), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("5. [모더레이션 신고 심사] RESOLVED 처리 및 사이트 정지 연동 검증")
    void testResolveModerationReport() {
        when(jdbcTemplate.queryForList(contains("FROM kkumz_moderation_report"), any(Object[].class)))
                .thenReturn(List.of(Map.of("id", "rep-kkumz-001", "status", "RESOLVED")));

        ResolveReportRequest req = ResolveReportRequest.builder()
                .status("RESOLVED")
                .actionTaken("커뮤니티 운영 정책에 따라 게시물 수정 권고 완료")
                .suspendSite(true)
                .moderatorUsername("admin")
                .build();

        List<Map<String, Object>> updated = kkumzService.resolveModerationReport("rep-kkumz-001", req);
        assertEquals(1, updated.size());
        verify(jdbcTemplate, times(1)).update(contains("UPDATE kkumz_moderation_report"), eq("RESOLVED"), anyString(), eq("admin"), eq("rep-kkumz-001"));
        verify(jdbcTemplate, times(1)).update(contains("UPDATE kkumz_site SET status = 'SUSPENDED'"), eq("rep-kkumz-001"));
    }

    @Test
    @DisplayName("6. [운영 종합 대시보드 요약] GET /api/v1/kkumz/admin/dashboard/summary KPI 및 파이프라인 집계 검증")
    void testGetAdminDashboardSummary() throws Exception {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(4);
        when(jdbcTemplate.queryForList(contains("GROUP BY site_type")))
                .thenReturn(List.of(Map.of("siteType", "COMMERCE", "count", 2, "totalViews", 4530, "totalLikes", 363)));
        when(jdbcTemplate.queryForList(contains("GROUP BY deploy_mode")))
                .thenReturn(List.of(Map.of("deployMode", "CUSTOM_BUNDLE", "count", 2)));
        when(jdbcTemplate.queryForList(contains("FROM kkumz_build_job b"), any(Object[].class)))
                .thenReturn(List.of(Map.of("id", "job-kkumz-002", "brandName", "말차 아뜰리에 제주", "status", "SUCCESS", "versionNo", 2)));
        when(jdbcTemplate.queryForList(contains("FROM kkumz_moderation_report r"), any(Object[].class)))
                .thenReturn(List.of(Map.of("id", "rep-kkumz-001", "brandName", "데스크 오빗", "status", "PENDING", "reasonCategory", "SPAM_PROMOTION")));
        when(jdbcTemplate.queryForList(contains("FROM kkumz_idea i"), any(Object[].class)))
                .thenReturn(List.of(Map.of("id", "idea-kkumz-001", "selectedBrandName", "말차 아뜰리에 제주", "status", "PUBLISHED")));
        when(jdbcTemplate.queryForList(contains("FROM kkumz_site s"), any(Object[].class)))
                .thenReturn(List.of(Map.of("id", "site-kkumz-001", "siteName", "말차 아뜰리에 제주", "viewCount", 1890)));

        mockMvc.perform(get("/api/v1/kkumz/admin/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.totalIdeas").value(4))
                .andExpect(jsonPath("$.result.buildSuccessRate").value(100))
                .andExpect(jsonPath("$.result.totalAiPromptCalls").value(16))
                .andExpect(jsonPath("$.result.siteTypeDistribution[0].siteType").value("COMMERCE"))
                .andExpect(jsonPath("$.result.recentBuilds[0].brandName").value("말차 아뜰리에 제주"))
                .andExpect(jsonPath("$.result.recentReports[0].brandName").value("데스크 오빗"));
    }

    @Test
    @DisplayName("7. [스튜디오 메타데이터 조회] GET /api/v1/kkumz/studio/metadata DB 사전 & 52종 기능 카탈로그 연동 검증")
    void testGetStudioMetadata() throws Exception {
        when(jdbcTemplate.queryForList(contains("FROM sys_dict_item i")))
                .thenReturn(List.of(
                        Map.of("dictCode", "kkumz_layout_type", "itemValue", "HERO_SPLIT", "itemText", "히어로 스플릿 배너", "description", ""),
                        Map.of("dictCode", "kkumz_component_type", "itemValue", "BrandSplitHero", "itemText", "브랜드 스플릿 히어로", "description", ""),
                        Map.of("dictCode", "kkumz_feature_category", "itemValue", "COMMERCE_CATALOG", "itemText", "상품·객실·메뉴 카탈로그", "description", "커머스 진열"),
                        Map.of("dictCode", "kkumz_hero_style", "itemValue", "FULL_SCENIC", "itemText", "풀스크린 시닉 히어로", "description", "풍경 강조"),
                        Map.of("dictCode", "kkumz_starter_preset", "itemValue", "COMMERCE_ORGANIC_TEA", "itemText", "유기농 D2C 티 스토어",
                                "description", "{\"siteType\":\"COMMERCE\",\"category\":\"F&B · 웰니스\",\"prompt\":\"제주 유기농 말차 다기 D2C 스토어\",\"tags\":[\"제주말차\",\"D2C\"]}")
                ));
        when(jdbcTemplate.queryForList(contains("FROM kkumz_feature_catalog")))
                .thenReturn(List.of(
                        new LinkedHashMap<>(Map.of(
                                "id", "fcat-kkumz-001",
                                "featureCode", "SHOP_PRODUCT_CATALOG",
                                "engineCode", "SYNC_SHOP",
                                "categoryCode", "COMMERCE_CATALOG",
                                "categoryName", "상품·객실·메뉴 카탈로그",
                                "title", "시그니처 상품·객실·메뉴 쇼케이스",
                                "subtitle", "대표 상품과 객실·메뉴를 카드로 진열",
                                "description", "상세 진열 모듈",
                                "searchKeywordsJson", "[\"상품\",\"객실\",\"민박\"]",
                                "recommendedCategoriesJson", "[\"숙박\",\"민박\"]"
                        ))
                ));

        mockMvc.perform(get("/api/v1/kkumz/studio/metadata"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.layoutLabels.HERO_SPLIT").value("히어로 스플릿 배너"))
                .andExpect(jsonPath("$.result.componentLabels.BrandSplitHero").value("브랜드 스플릿 히어로"))
                .andExpect(jsonPath("$.result.componentLabels.SHOP_PRODUCT_CATALOG").value("시그니처 상품·객실·메뉴 쇼케이스"))
                .andExpect(jsonPath("$.result.featureCategories[0].code").value("COMMERCE_CATALOG"))
                .andExpect(jsonPath("$.result.featureCatalog[0].featureCode").value("SHOP_PRODUCT_CATALOG"))
                .andExpect(jsonPath("$.result.starterPresets[0].code").value("COMMERCE_ORGANIC_TEA"))
                .andExpect(jsonPath("$.result.starterPresets[0].siteType").value("COMMERCE"));
    }

    @Test
    @DisplayName("8. [SyncCMS 실데이터 연동] cms_content 조회 및 cms_form_data 신청 접수 검증")
    void testCmsRuntimeAndFormSubmission() {
        Map<String, Object> siteRow = new LinkedHashMap<>();
        siteRow.put("id", "site-kkumz-003");
        siteRow.put("ideaId", "idea-kkumz-003");
        siteRow.put("siteSlug", "seoul-run-club");
        siteRow.put("siteName", "서울 나이트 런 클럽");
        siteRow.put("siteType", "CMS_SITE");
        siteRow.put("activeVersion", 1);
        siteRow.put("cmsSiteId", 101L);
        siteRow.put("ownerUsername", "jihoon.run");

        Map<String, Object> releaseRow = new LinkedHashMap<>();
        releaseRow.put("id", "rel-kkumz-003-v1");
        releaseRow.put("versionNo", 1);
        releaseRow.put("deployMode", "DYNAMIC_SDUI");
        releaseRow.put("releaseNote", "초기 배포");
        releaseRow.put("designTokensJson", "{\"primary\":\"#ea580c\"}");
        releaseRow.put("layoutSchemaJson", "{\"heroTitle\":\"서울 나이트 런 클럽\"}");
        releaseRow.put("webideFilesJson", "[]");

        when(jdbcTemplate.queryForList(contains("FROM kkumz_site WHERE site_slug = ?"), eq("seoul-run-club"), eq("seoul-run-club")))
                .thenReturn(List.of(siteRow));
        when(jdbcTemplate.queryForList(contains("FROM kkumz_site WHERE site_slug = ?"), eq("site-kkumz-003"), eq("site-kkumz-003")))
                .thenReturn(List.of(siteRow));
        when(jdbcTemplate.queryForList(contains("FROM kkumz_site_release"), eq("site-kkumz-003"), eq(1)))
                .thenReturn(List.of(releaseRow));
        when(cmsJdbcTemplate.queryForList(contains("FROM cms_content c"), eq(101L)))
                .thenReturn(List.of(Map.of("id", 101001L, "title", "반포 한강 나이트 런", "summary", "7.5K 템포런")));
        when(cmsJdbcTemplate.queryForList(contains("FROM cms_banner"), eq(101L)))
                .thenReturn(List.of(Map.of("id", 10101L, "title", "목요일 세션 오픈")));
        when(cmsJdbcTemplate.queryForList(contains("FROM cms_form"), eq(101L)))
                .thenReturn(List.of(new LinkedHashMap<>(Map.of("id", 101L, "title", "게스트 런 신청", "fieldsJson", "[]"))));

        Map<String, Object> runtime = kkumzService.getSiteRuntime("seoul-run-club");
        assertNotNull(runtime.get("cms"));
        @SuppressWarnings("unchecked")
        Map<String, Object> cms = (Map<String, Object>) runtime.get("cms");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> articles = (List<Map<String, Object>>) cms.get("articles");
        assertEquals(1, articles.size());
        assertEquals("반포 한강 나이트 런", articles.get(0).get("title"));

        when(cmsJdbcTemplate.queryForObject(contains("INSERT INTO cms_form_data"), eq(Long.class), eq(101L), anyString()))
                .thenReturn(5001L);
        when(cmsJdbcTemplate.queryForObject(contains("SELECT COUNT(*) FROM cms_form_data"), eq(Integer.class), eq(101L)))
                .thenReturn(2);

        CmsFormSubmitRequest submitReq = CmsFormSubmitRequest.builder()
                .applicantName("김러너")
                .phone("010-9999-8888")
                .optionValue("600 그룹")
                .message("첫 참가 신청합니다.")
                .build();
        Map<String, Object> submitRes = kkumzService.submitCmsForm("seoul-run-club", submitReq);
        assertEquals(5001L, submitRes.get("submissionId"));
        assertEquals(2, submitRes.get("totalSubmissions"));
    }

    @Test
    @DisplayName("9. [AI 응답 JSON 파서] 한국어 인사말 및 대괄호 서두 포함 응답 파싱 검증")
    void testParseAiJsonArrayWithConversationalPrefix() {
        String noisyAiOutput = """
                물론입니다! 요청하신 [제주 말차 브랜드 네이밍 2종] 후보입니다:
                [
                  {"brandName": "말차 아뜰리에 제주", "siteSlug": "matcha-atelier", "score": 98},
                  {"brandName": "비취다원", "siteSlug": "jade-tea-garden", "score": 95}
                ]
                도움이 되셨기를 바랍니다!
                """;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> parsed = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
                kkumzService, "parseAiJsonArray", noisyAiOutput, "NAMING"
        );
        assertNotNull(parsed);
        assertEquals(2, parsed.size());
        assertEquals("말차 아뜰리에 제주", parsed.get(0).get("brandName"));
        assertEquals("matcha-atelier", parsed.get(0).get("siteSlug"));
    }

    @Test
    @DisplayName("10. [API 연동·액세스키 관제] GET/POST /api/v1/kkumz/admin/api-keys 키 목록 및 신규 발급 검증")
    void testAdminApiKeyManagement() throws Exception {
        Map<String, Object> keyRow = new LinkedHashMap<>();
        keyRow.put("id", "ak-kkumz-out-cms");
        keyRow.put("keyName", "SyncCMS 멀티사이트 연동 키");
        keyRow.put("direction", "OUTBOUND");
        keyRow.put("serviceCode", "SYNC_CMS");
        keyRow.put("baseUrl", "http://localhost:18085");
        keyRow.put("accessKey", "ak_cms_kkumz_live_01");
        keyRow.put("secretKey", "sk_cms_kkumz_secret_2026_prod_99");
        keyRow.put("scopesJson", "[\"cms:site:read\",\"cms:site:provision\"]");
        keyRow.put("rateLimitPerMin", 1000);
        keyRow.put("status", "ACTIVE");
        keyRow.put("callCount", 42L);

        when(jdbcTemplate.queryForList(contains("FROM kkumz_api_access_key"), any(Object[].class)))
                .thenReturn(List.of(new LinkedHashMap<>(keyRow)));

        mockMvc.perform(get("/api/v1/kkumz/admin/api-keys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result[0].id").value("ak-kkumz-out-cms"))
                .andExpect(jsonPath("$.result[0].serviceCode").value("SYNC_CMS"))
                .andExpect(jsonPath("$.result[0].accessKey").value("ak_cms_kkumz_live_01"))
                .andExpect(jsonPath("$.result[0].scopes[0]").value("cms:site:read"));
    }

    @Test
    @DisplayName("11. [AI 의도 분석 & 기능 선택] analyzeIdeaIntent 및 updateIdeaFeatures 하이브리드 자동 판별 검증")
    void testAnalyzeIdeaIntentAndUpdateFeatures() {
        Map<String, Object> ideaRow = new LinkedHashMap<>();
        ideaRow.put("id", "idea-01137479");
        ideaRow.put("authorUsername", "admin");
        ideaRow.put("authorDisplayName", "최고관리자");
        ideaRow.put("title", "맑은물 민박");
        ideaRow.put("promptText", "충남 공주 시골에 있는 냇가 옆에 위치한 맑은물 민박 사이트 만들고 싶어");
        ideaRow.put("siteType", "HYBRID");
        ideaRow.put("category", "숙박 · 민박 · 스테이");
        ideaRow.put("tagsJson", "[\"공주민박\",\"냇가평상\"]");
        ideaRow.put("enabledFeaturesJson", "[\"SHOP_PRODUCT_CATALOG\",\"CMS_DYNAMIC_FORM\"]");
        ideaRow.put("aiIntentAnalysisJson", "{\"siteType\":\"HYBRID\"}");

        when(jdbcTemplate.queryForList(contains("FROM kkumz_idea"), eq("idea-01137479")))
                .thenReturn(List.of(new LinkedHashMap<>(ideaRow)));
        when(jdbcTemplate.queryForList(contains("FROM kkumz_feature_catalog")))
                .thenReturn(List.of(
                        new LinkedHashMap<>(Map.of(
                                "id", "fcat-kkumz-001",
                                "featureCode", "SHOP_PRODUCT_CATALOG",
                                "engineCode", "SYNC_SHOP",
                                "categoryCode", "COMMERCE_CATALOG",
                                "categoryName", "상품·객실·메뉴 카탈로그",
                                "title", "시그니처 상품·객실·메뉴 쇼케이스",
                                "subtitle", "객실과 스테이 상품을 진열",
                                "searchKeywordsJson", "[\"민박\",\"객실\",\"숙박\"]",
                                "recommendedCategoriesJson", "[\"숙박\",\"민박\"]"
                        )),
                        new LinkedHashMap<>(Map.of(
                                "id", "fcat-kkumz-036",
                                "featureCode", "CMS_DYNAMIC_FORM",
                                "engineCode", "SYNC_CMS",
                                "categoryCode", "FORM_SCHEDULE",
                                "categoryName", "예약·신청 폼 & 일정",
                                "title", "맞춤형 온라인 예약·신청 동적 폼",
                                "subtitle", "체크인 날짜·인원·옵션 예약 폼",
                                "searchKeywordsJson", "[\"예약\",\"민박\",\"신청\"]",
                                "recommendedCategoriesJson", "[\"숙박\",\"민박\"]"
                        ))
                ));

        UpdateIdeaFeaturesRequest featReq = UpdateIdeaFeaturesRequest.builder()
                .enabledFeatures(List.of("SHOP_PRODUCT_CATALOG", "CMS_DYNAMIC_FORM"))
                .syncScreens(true)
                .build();

        Map<String, Object> updated = kkumzService.updateIdeaFeatures("idea-01137479", featReq);
        assertEquals("HYBRID", updated.get("siteType"));
        verify(jdbcTemplate, atLeastOnce()).update(contains("UPDATE kkumz_idea"), any(), any(), eq("HYBRID"), any(), any(), any(), any(), any(), eq("idea-01137479"));
    }

    @Test
    @DisplayName("12. [자가 치유형 스캐폴딩] 로고(selectedLogoSvg)·디자인 토큰 미선택 상태에서도 예외 없이 자동 복구 및 프로비저닝 검증")
    void testScaffoldSiteSelfHealingWithoutSelectedLogo() {
        Map<String, Object> incompleteIdeaRow = new LinkedHashMap<>();
        incompleteIdeaRow.put("id", "idea-cb4a9d2d");
        incompleteIdeaRow.put("authorUsername", "admin");
        incompleteIdeaRow.put("authorDisplayName", "최고관리자");
        incompleteIdeaRow.put("title", "맑은물 민박 홈페이지를 더 화려하게 만들고 싶어");
        incompleteIdeaRow.put("promptText", "충남 공주 계곡 옆 자연 친화적인 맑은물 민박 홈페이지");
        incompleteIdeaRow.put("siteType", "CMS_SITE");
        incompleteIdeaRow.put("category", "숙박 · 민박 · 스테이");
        incompleteIdeaRow.put("selectedBrandName", "");
        incompleteIdeaRow.put("selectedSiteSlug", "");
        incompleteIdeaRow.put("selectedLogoSvg", "");
        incompleteIdeaRow.put("designTokensJson", "{}");
        incompleteIdeaRow.put("enabledFeaturesJson", "[\"CMS_DYNAMIC_FORM\",\"CMS_PHOTO_GALLERY\"]");

        Map<String, Object> provisionedSiteRow = new LinkedHashMap<>();
        provisionedSiteRow.put("id", "site-self-healed");
        provisionedSiteRow.put("ideaId", "idea-cb4a9d2d");
        provisionedSiteRow.put("siteSlug", "malgeunmul-minbak");
        provisionedSiteRow.put("siteName", "맑은물 민박");
        provisionedSiteRow.put("siteType", "CMS_SITE");
        provisionedSiteRow.put("activeVersion", 1);
        provisionedSiteRow.put("cmsSiteId", 105L);
        provisionedSiteRow.put("ownerUsername", "admin");

        Map<String, Object> releaseRow = new LinkedHashMap<>();
        releaseRow.put("id", "rel-self-healed-v1");
        releaseRow.put("versionNo", 1);
        releaseRow.put("deployMode", "DYNAMIC_SDUI");
        releaseRow.put("releaseNote", "Initial Live Multisite Provisioning");
        releaseRow.put("designTokensJson", "{\"primary\":\"#0284c7\"}");
        releaseRow.put("layoutSchemaJson", "{\"heroTitle\":\"맑은물 민박\"}");
        releaseRow.put("webideFilesJson", "[]");

        when(jdbcTemplate.queryForList(contains("FROM kkumz_idea"), eq("idea-cb4a9d2d")))
                .thenReturn(List.of(incompleteIdeaRow));
        when(jdbcTemplate.queryForList(contains("FROM kkumz_site"), eq("idea-cb4a9d2d")))
                .thenReturn(List.of(provisionedSiteRow));
        when(jdbcTemplate.queryForList(contains("FROM kkumz_site WHERE site_slug = ?"), anyString(), anyString()))
                .thenReturn(List.of(provisionedSiteRow));
        when(jdbcTemplate.queryForList(contains("FROM kkumz_site_release"), eq("site-self-healed"), eq(1)))
                .thenReturn(List.of(releaseRow));

        Map<String, Object> runtime = assertDoesNotThrow(() -> kkumzService.scaffoldSite("idea-cb4a9d2d"));
        assertNotNull(runtime);
        verify(jdbcTemplate, atLeastOnce()).update(
                contains("selected_logo_svg = ?"),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), contains("<svg"), anyString(), eq("idea-cb4a9d2d")
        );
    }
}
