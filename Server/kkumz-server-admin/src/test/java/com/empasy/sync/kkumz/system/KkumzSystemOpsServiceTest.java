package com.empasy.sync.kkumz.system;

import com.empasy.sync.kkumz.system.KkumzSystemOpsDTO.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KkumzSystemOpsServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private KkumzSystemOpsService service;

    @BeforeEach
    void setUp() {
        service = new KkumzSystemOpsService(jdbcTemplate, new ObjectMapper());
    }

    @Test
    void queryDefIndexReturnsConfiguredRoleIndexFromDatabase() {
        Map<String, Object> dbRow = new LinkedHashMap<>();
        dbRow.put("id", "ridx-kkumz-001");
        dbRow.put("role_code", "DEF_INDEX_ALL");
        dbRow.put("url", "/dashboard/analysis");
        dbRow.put("component", "dashboard/analysis/index");
        dbRow.put("is_route", Boolean.TRUE);
        dbRow.put("priority", 1);
        dbRow.put("status", "1");

        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(dbRow));

        Map<String, Object> result = service.queryDefIndex();

        assertThat(result)
                .containsEntry("id", "ridx-kkumz-001")
                .containsEntry("url", "/dashboard/analysis")
                .containsEntry("component", "dashboard/analysis/index")
                .containsEntry("isRoute", true);
    }

    @Test
    void queryDepartTreeSyncMapsTreeFieldsAndLeafFlag() {
        Map<String, Object> rootRow = new LinkedHashMap<>();
        rootRow.put("id", "dept-kkumz-001");
        rootRow.put("parent_id", null);
        rootRow.put("depart_name", "꿈즈 플랫폼 본부");
        rootRow.put("org_code", "KKUMZ-HQ");
        rootRow.put("org_category", "1");
        rootRow.put("depart_order", 1);
        rootRow.put("status", "1");
        rootRow.put("child_count", 2L);

        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(rootRow));

        List<Map<String, Object>> tree = service.queryDepartTreeSync(null, null);

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0))
                .containsEntry("id", "dept-kkumz-001")
                .containsEntry("key", "dept-kkumz-001")
                .containsEntry("title", "꿈즈 플랫폼 본부")
                .containsEntry("orgCode", "KKUMZ-HQ")
                .containsEntry("isLeaf", false);
    }

    @Test
    void listLogsAppliesFiltersAndMapsOperateTypeDictionaryLabels() {
        Map<String, Object> logRow = new LinkedHashMap<>();
        logRow.put("id", "log-kkumz-001");
        logRow.put("log_type", 2);
        logRow.put("log_content", "관리자 설정 조회");
        logRow.put("operate_type", 1);
        logRow.put("userid", "usr-kkumz-001");
        logRow.put("username", "admin");
        logRow.put("ip", "127.0.0.1");
        logRow.put("method", "GET");
        logRow.put("request_url", "/api/v1/kkumz/sys/config/list");
        logRow.put("request_param", "{}");
        logRow.put("client_type", "WEB");
        logRow.put("cost_time", 15L);

        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(logRow));

        Map<String, Object> page = service.listLogs(2, "설정", 1, "2026-11-19", "2026-11-19", 1, 20);

        verify(jdbcTemplate).queryForList(
                anyString(),
                eq(new Object[]{2, 1, "%설정%", "%설정%", "%설정%", "2026-11-19 00:00:00", "2026-11-19 23:59:59.999"})
        );
        assertThat(page.get("total")).isEqualTo(1);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> records = (List<Map<String, Object>>) page.get("records");
        assertThat(records).hasSize(1);
        assertThat(records.get(0))
                .containsEntry("id", "log-kkumz-001")
                .containsEntry("operateType_dictText", "조회")
                .containsEntry("clientType_dictText", "PC 웹");
    }

    @Test
    void listPageHelpDeserializesJsonbFieldsAlongsideRawJsonStrings() {
        Map<String, Object> helpRow = new LinkedHashMap<>();
        helpRow.put("id", "help_kkumz_sys_prompt");
        helpRow.put("app_code", "KKUMZ");
        helpRow.put("page_key", "/system/prompt");
        helpRow.put("title", "AI 프롬프트 관리");
        helpRow.put("category", "시스템 설정");
        helpRow.put("summary", "요약");
        helpRow.put("overview", "개요");
        helpRow.put("features_json", "[{\"name\":\"버전 관리\",\"description\":\"롤백 지원\"}]");
        helpRow.put("lineage_json", "{\"sources\":[]}");
        helpRow.put("formulas_json", "[]");
        helpRow.put("steps_json", "[]");
        helpRow.put("related_screens_json", "[]");
        helpRow.put("tips_json", "[\"변경 전 백업\"]");
        helpRow.put("warnings_json", "[]");
        helpRow.put("faq_json", "[]");
        helpRow.put("status", "ACTIVE");
        helpRow.put("sort_order", 1);

        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(helpRow));

        List<Map<String, Object>> list = service.listPageHelp("KKUMZ", null, null, null);

        assertThat(list).hasSize(1);
        assertThat(list.get(0))
                .containsEntry("id", "help_kkumz_sys_prompt")
                .containsEntry("pageKey", "/system/prompt");
        assertThat(list.get(0).get("tips")).isEqualTo(List.of("변경 전 백업"));
    }

    @Test
    void sendTestMessageRejectsUnknownTemplateCode() {
        when(jdbcTemplate.queryForList(anyString(), eq("UNKNOWN_CODE"))).thenReturn(Collections.emptyList());

        SendMessageTestRequest req = SendMessageTestRequest.builder()
                .templateCode("UNKNOWN_CODE")
                .receiver("admin")
                .build();

        assertThatThrownBy(() -> service.sendTestMessage(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UNKNOWN_CODE");
    }

    @Test
    void updateDefIndexUpdatesExistingDefaultRow() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(1);
        Map<String, Object> updated = new LinkedHashMap<>();
        updated.put("id", "ridx-kkumz-001");
        updated.put("role_code", "DEF_INDEX_ALL");
        updated.put("url", "/kkumz/sites");
        updated.put("component", "kkumz/sites/index");
        updated.put("is_route", Boolean.TRUE);
        updated.put("priority", 1);
        updated.put("status", "1");
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(updated));

        Map<String, Object> result = service.updateDefIndex("/kkumz/sites", "kkumz/sites/index", true);

        verify(jdbcTemplate).update(anyString(), eq("/kkumz/sites"), eq("kkumz/sites/index"), eq(true));
        assertThat(result).containsEntry("url", "/kkumz/sites");
    }
}
