package com.empasy.sync.kkumz.system;

import com.empasy.sync.kkumz.system.KkumzSystemOpsDTO.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Kkumz 시스템 운영 확장 서비스
 * (기본 홈 라우트, 조직 부서 트리 및 부서 권한, 알림 템플릿, 작업 감사 로그, 화면 도움말 마스터)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KkumzSystemOpsService {

    private static final Map<Integer, String> OPERATE_TYPE_LABELS = Map.of(
            1, "조회",
            2, "등록",
            3, "수정",
            4, "삭제",
            5, "가져오기",
            6, "내보내기"
    );

    private static final Map<String, String> CLIENT_TYPE_LABELS = Map.of(
            "WEB", "PC 웹",
            "APP", "모바일 앱",
            "API", "외부 API"
    );

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void ensureSystemTables() {
        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS sys_role_index (" +
                    "id VARCHAR(64) PRIMARY KEY, " +
                    "role_code VARCHAR(64) NOT NULL UNIQUE, " +
                    "url VARCHAR(255) NOT NULL, " +
                    "component VARCHAR(255) NOT NULL, " +
                    "is_route BOOLEAN NOT NULL DEFAULT TRUE, " +
                    "priority INTEGER NOT NULL DEFAULT 1, " +
                    "status VARCHAR(16) NOT NULL DEFAULT '1', " +
                    "create_by VARCHAR(64) DEFAULT 'admin', " +
                    "create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "update_by VARCHAR(64) DEFAULT 'admin', " +
                    "update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP)"
            );
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS sys_depart (" +
                    "id VARCHAR(64) PRIMARY KEY, " +
                    "parent_id VARCHAR(64), " +
                    "depart_name VARCHAR(100) NOT NULL, " +
                    "depart_name_en VARCHAR(100), " +
                    "depart_order INTEGER NOT NULL DEFAULT 0, " +
                    "description VARCHAR(500), " +
                    "org_category VARCHAR(16) NOT NULL DEFAULT '1', " +
                    "org_type VARCHAR(16) NOT NULL DEFAULT '1', " +
                    "org_code VARCHAR(64) NOT NULL UNIQUE, " +
                    "mobile VARCHAR(32), " +
                    "fax VARCHAR(32), " +
                    "address VARCHAR(255), " +
                    "memo VARCHAR(500), " +
                    "status VARCHAR(16) NOT NULL DEFAULT '1', " +
                    "del_flag INTEGER NOT NULL DEFAULT 0, " +
                    "create_by VARCHAR(64) DEFAULT 'admin', " +
                    "create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "update_by VARCHAR(64) DEFAULT 'admin', " +
                    "update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP)"
            );
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS sys_depart_permission (" +
                    "id VARCHAR(64) PRIMARY KEY, " +
                    "depart_id VARCHAR(64) NOT NULL REFERENCES sys_depart(id) ON DELETE CASCADE, " +
                    "permission_id VARCHAR(64) NOT NULL REFERENCES sys_permission(id) ON DELETE CASCADE, " +
                    "CONSTRAINT uk_kkumz_sys_depart_perm UNIQUE (depart_id, permission_id))"
            );
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS sys_message_template (" +
                    "id VARCHAR(64) PRIMARY KEY, " +
                    "template_name VARCHAR(120) NOT NULL, " +
                    "template_code VARCHAR(64) NOT NULL UNIQUE, " +
                    "template_type VARCHAR(16) NOT NULL DEFAULT '4', " +
                    "template_content TEXT NOT NULL, " +
                    "template_test_json TEXT, " +
                    "use_status VARCHAR(16) NOT NULL DEFAULT '1', " +
                    "create_by VARCHAR(64) DEFAULT 'admin', " +
                    "create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "update_by VARCHAR(64) DEFAULT 'admin', " +
                    "update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP)"
            );
            jdbcTemplate.execute("ALTER TABLE sys_log ADD COLUMN IF NOT EXISTS request_param TEXT");
            jdbcTemplate.execute("ALTER TABLE sys_log ADD COLUMN IF NOT EXISTS client_type VARCHAR(32) DEFAULT 'WEB'");
        } catch (Exception e) {
            log.warn("Kkumz 시스템 확장 테이블 점검 중 경고: {}", e.getMessage());
        }
    }

    public Map<String, Object> queryDefIndex() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, role_code, url, component, is_route, priority, status " +
                "FROM sys_role_index WHERE status = '1' ORDER BY priority ASC, create_time ASC LIMIT 1"
        );
        if (!rows.isEmpty()) {
            Map<String, Object> row = rows.get(0);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", row.get("id"));
            result.put("roleCode", row.get("role_code"));
            result.put("url", row.get("url"));
            result.put("component", row.get("component"));
            result.put("isRoute", Boolean.TRUE.equals(row.get("is_route")));
            result.put("priority", row.get("priority"));
            result.put("status", row.get("status"));
            return result;
        }
        List<Map<String, Object>> permRows = jdbcTemplate.queryForList(
                "SELECT url, component, is_route FROM sys_permission " +
                "WHERE del_flag = 0 AND menu_type = 1 ORDER BY sort_no ASC LIMIT 1"
        );
        if (permRows.isEmpty()) {
            throw new IllegalStateException("기본 홈 라우트(sys_role_index 또는 sys_permission)가 DB에 등록되어 있지 않습니다.");
        }
        Map<String, Object> fallback = permRows.get(0);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("roleCode", "DEF_INDEX_ALL");
        result.put("url", fallback.get("url"));
        result.put("component", fallback.get("component"));
        result.put("isRoute", Boolean.TRUE.equals(fallback.get("is_route")));
        result.put("status", "1");
        return result;
    }

    @Transactional
    public Map<String, Object> updateDefIndex(String url, String component, Boolean isRoute) {
        if (url == null || url.isBlank() || component == null || component.isBlank()) {
            throw new IllegalArgumentException("기본 홈 URL과 컴포넌트 경로는 필수입니다.");
        }
        boolean routeFlag = isRoute == null || isRoute;
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_role_index WHERE role_code = 'DEF_INDEX_ALL'",
                Integer.class
        );
        if (exists != null && exists > 0) {
            jdbcTemplate.update(
                    "UPDATE sys_role_index SET url = ?, component = ?, is_route = ?, update_by = 'admin', update_time = CURRENT_TIMESTAMP " +
                    "WHERE role_code = 'DEF_INDEX_ALL'",
                    url.trim(), component.trim(), routeFlag
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO sys_role_index (id, role_code, url, component, is_route, priority, status, create_by) " +
                    "VALUES (?, 'DEF_INDEX_ALL', ?, ?, ?, 1, '1', 'admin')",
                    "ridx-" + UUID.randomUUID().toString().substring(0, 8),
                    url.trim(), component.trim(), routeFlag
            );
        }
        return queryDefIndex();
    }

    public List<Map<String, Object>> queryDepartTreeSync(String pid, String ids) {
        List<Map<String, Object>> rows;
        if (ids != null && !ids.isBlank()) {
            List<String> idList = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            if (idList.isEmpty()) {
                return Collections.emptyList();
            }
            String placeholders = String.join(",", Collections.nCopies(idList.size(), "?"));
            rows = jdbcTemplate.queryForList(
                    "SELECT d.*, (SELECT COUNT(*) FROM sys_depart c WHERE c.parent_id = d.id AND c.del_flag = 0) AS child_count " +
                    "FROM sys_depart d WHERE d.del_flag = 0 AND d.id IN (" + placeholders + ") ORDER BY d.depart_order ASC, d.create_time ASC",
                    idList.toArray()
            );
        } else if (pid != null && !pid.isBlank()) {
            rows = jdbcTemplate.queryForList(
                    "SELECT d.*, (SELECT COUNT(*) FROM sys_depart c WHERE c.parent_id = d.id AND c.del_flag = 0) AS child_count " +
                    "FROM sys_depart d WHERE d.del_flag = 0 AND d.parent_id = ? ORDER BY d.depart_order ASC, d.create_time ASC",
                    pid.trim()
            );
        } else {
            rows = jdbcTemplate.queryForList(
                    "SELECT d.*, (SELECT COUNT(*) FROM sys_depart c WHERE c.parent_id = d.id AND c.del_flag = 0) AS child_count " +
                    "FROM sys_depart d WHERE d.del_flag = 0 AND (d.parent_id IS NULL OR d.parent_id = '') ORDER BY d.depart_order ASC, d.create_time ASC"
            );
        }
        return rows.stream().map(this::mapDepartNode).toList();
    }

    public List<Map<String, Object>> searchDepartsByKeywords(String keyWord) {
        String kw = keyWord == null ? "" : keyWord.trim();
        if (kw.isEmpty()) {
            return queryDepartTreeSync(null, null);
        }
        String pattern = "%" + kw.toLowerCase() + "%";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT d.*, (SELECT COUNT(*) FROM sys_depart c WHERE c.parent_id = d.id AND c.del_flag = 0) AS child_count " +
                "FROM sys_depart d WHERE d.del_flag = 0 AND (LOWER(d.depart_name) LIKE ? OR LOWER(d.org_code) LIKE ?) " +
                "ORDER BY d.depart_order ASC, d.create_time ASC",
                pattern, pattern
        );
        return rows.stream().map(this::mapDepartNode).toList();
    }

    @Transactional
    public Map<String, Object> saveDepart(SaveDepartRequest req, boolean isUpdate) {
        if (req == null || req.getDepartName() == null || req.getDepartName().isBlank()) {
            throw new IllegalArgumentException("부서명은 필수입니다.");
        }
        String departId = (req.getId() != null && !req.getId().isBlank())
                ? req.getId().trim()
                : "dept-" + UUID.randomUUID().toString().substring(0, 8);
        String parentId = (req.getParentId() != null && !req.getParentId().isBlank()) ? req.getParentId().trim() : null;
        String orgCategory = (req.getOrgCategory() != null && !req.getOrgCategory().isBlank()) ? req.getOrgCategory().trim() : (parentId == null ? "1" : "2");
        String orgType = (req.getOrgType() != null && !req.getOrgType().isBlank()) ? req.getOrgType().trim() : orgCategory;
        String orgCode = (req.getOrgCode() != null && !req.getOrgCode().isBlank())
                ? req.getOrgCode().trim()
                : "ORG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        int order = req.getDepartOrder() != null ? req.getDepartOrder() : 0;
        String status = (req.getStatus() != null && !req.getStatus().isBlank()) ? req.getStatus().trim() : "1";

        if (isUpdate) {
            jdbcTemplate.update(
                    "UPDATE sys_depart SET parent_id = ?, depart_name = ?, depart_name_en = ?, depart_order = ?, " +
                    "description = ?, org_category = ?, org_type = ?, org_code = ?, mobile = ?, fax = ?, address = ?, memo = ?, " +
                    "status = ?, update_by = 'admin', update_time = CURRENT_TIMESTAMP WHERE id = ?",
                    parentId, req.getDepartName().trim(), req.getDepartNameEn(), order,
                    req.getDescription(), orgCategory, orgType, orgCode, req.getMobile(), req.getFax(),
                    req.getAddress(), req.getMemo(), status, departId
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO sys_depart (id, parent_id, depart_name, depart_name_en, depart_order, description, " +
                    "org_category, org_type, org_code, mobile, fax, address, memo, status, del_flag, create_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin')",
                    departId, parentId, req.getDepartName().trim(), req.getDepartNameEn(), order,
                    req.getDescription(), orgCategory, orgType, orgCode, req.getMobile(), req.getFax(),
                    req.getAddress(), req.getMemo(), status
            );
        }
        return Map.of("id", departId, "departName", req.getDepartName().trim(), "orgCode", orgCode);
    }

    @Transactional
    public void deleteDepart(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        jdbcTemplate.update("UPDATE sys_depart SET del_flag = 1, update_time = CURRENT_TIMESTAMP WHERE id = ? OR parent_id = ?", id.trim(), id.trim());
    }

    @Transactional
    public void deleteDepartBatch(String ids) {
        if (ids == null || ids.isBlank()) {
            return;
        }
        for (String id : ids.split(",")) {
            if (!id.trim().isEmpty()) {
                deleteDepart(id.trim());
            }
        }
    }

    public Map<String, Object> queryPermissionTreeList() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, parent_id, name, icon, menu_type, rule_flag, sort_no " +
                "FROM sys_permission WHERE del_flag = 0 ORDER BY sort_no ASC, id ASC"
        );
        Map<String, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        List<String> allIds = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String id = String.valueOf(row.get("id"));
            allIds.add(id);
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", id);
            node.put("key", id);
            node.put("value", id);
            node.put("title", row.get("name"));
            node.put("slotTitle", row.get("name"));
            node.put("parentId", row.get("parent_id"));
            node.put("icon", row.get("icon"));
            node.put("ruleFlag", row.get("rule_flag"));
            node.put("isLeaf", true);
            node.put("children", new ArrayList<Map<String, Object>>());
            nodeMap.put(id, node);
        }
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Map<String, Object> node : nodeMap.values()) {
            Object parentObj = node.get("parentId");
            String parentId = parentObj != null ? String.valueOf(parentObj) : "";
            if (!parentId.isBlank() && nodeMap.containsKey(parentId)) {
                Map<String, Object> parent = nodeMap.get(parentId);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                children.add(node);
                parent.put("isLeaf", false);
            } else {
                roots.add(node);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("treeList", roots);
        result.put("ids", allIds);
        return result;
    }

    public List<String> queryDepartPermission(String departId) {
        if (departId == null || departId.isBlank()) {
            return Collections.emptyList();
        }
        return jdbcTemplate.queryForList(
                "SELECT permission_id FROM sys_depart_permission WHERE depart_id = ?",
                String.class,
                departId.trim()
        );
    }

    @Transactional
    public void saveDepartPermission(SaveDepartPermissionRequest req) {
        if (req == null || req.getDepartId() == null || req.getDepartId().isBlank()) {
            throw new IllegalArgumentException("부서 식별자(departId)는 필수입니다.");
        }
        String departId = req.getDepartId().trim();
        jdbcTemplate.update("DELETE FROM sys_depart_permission WHERE depart_id = ?", departId);
        if (req.getPermissionIds() != null && !req.getPermissionIds().isBlank()) {
            for (String permId : req.getPermissionIds().split(",")) {
                String trimmed = permId.trim();
                if (!trimmed.isEmpty()) {
                    jdbcTemplate.update(
                            "INSERT INTO sys_depart_permission (id, depart_id, permission_id) VALUES (?, ?, ?)",
                            "sdp-" + UUID.randomUUID().toString().substring(0, 12),
                            departId,
                            trimmed
                    );
                }
            }
        }
    }

    public Map<String, Object> listMessageTemplates(String templateName, String templateCode, String templateType, int pageNo, int pageSize) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, template_name, template_code, template_type, template_content, template_test_json, use_status, create_by, create_time, update_time " +
                "FROM sys_message_template WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (templateName != null && !templateName.isBlank()) {
            sql.append(" AND LOWER(template_name) LIKE ?");
            params.add("%" + stripWildcards(templateName).toLowerCase() + "%");
        }
        if (templateCode != null && !templateCode.isBlank()) {
            sql.append(" AND LOWER(template_code) LIKE ?");
            params.add("%" + stripWildcards(templateCode).toLowerCase() + "%");
        }
        if (templateType != null && !templateType.isBlank()) {
            sql.append(" AND template_type = ?");
            params.add(templateType.trim());
        }
        sql.append(" ORDER BY create_time DESC, id ASC");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        List<Map<String, Object>> mapped = rows.stream().map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.get("id"));
            item.put("templateName", row.get("template_name"));
            item.put("templateCode", row.get("template_code"));
            item.put("templateType", row.get("template_type"));
            item.put("templateContent", row.get("template_content"));
            item.put("templateTestJson", row.get("template_test_json"));
            item.put("useStatus", row.get("use_status"));
            item.put("createBy", row.get("create_by"));
            item.put("createTime", formatTimestamp(row.get("create_time")));
            item.put("updateTime", formatTimestamp(row.get("update_time")));
            return item;
        }).toList();
        return toPageResult(mapped, pageNo, pageSize);
    }

    @Transactional
    public Map<String, Object> saveMessageTemplate(SaveMessageTemplateRequest req, boolean isUpdate) {
        if (req == null || req.getTemplateCode() == null || req.getTemplateCode().isBlank()) {
            throw new IllegalArgumentException("템플릿 코드는 필수입니다.");
        }
        if (req.getTemplateName() == null || req.getTemplateName().isBlank()) {
            throw new IllegalArgumentException("템플릿 제목은 필수입니다.");
        }
        String id = (req.getId() != null && !req.getId().isBlank())
                ? req.getId().trim()
                : "msgtpl-" + UUID.randomUUID().toString().substring(0, 8);
        String type = (req.getTemplateType() != null && !req.getTemplateType().isBlank()) ? req.getTemplateType().trim() : "4";
        String content = req.getTemplateContent() != null ? req.getTemplateContent() : "";
        String useStatus = (req.getUseStatus() != null && !req.getUseStatus().isBlank()) ? req.getUseStatus().trim() : "1";

        if (isUpdate) {
            jdbcTemplate.update(
                    "UPDATE sys_message_template SET template_name = ?, template_code = ?, template_type = ?, " +
                    "template_content = ?, template_test_json = ?, use_status = ?, update_by = 'admin', update_time = CURRENT_TIMESTAMP WHERE id = ?",
                    req.getTemplateName().trim(), req.getTemplateCode().trim(), type,
                    content, req.getTemplateTestJson(), useStatus, id
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO sys_message_template (id, template_name, template_code, template_type, template_content, template_test_json, use_status, create_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, 'admin')",
                    id, req.getTemplateName().trim(), req.getTemplateCode().trim(), type,
                    content, req.getTemplateTestJson(), useStatus
            );
        }
        return Map.of("id", id, "templateCode", req.getTemplateCode().trim());
    }

    @Transactional
    public void deleteMessageTemplate(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        jdbcTemplate.update("DELETE FROM sys_message_template WHERE id = ?", id.trim());
    }

    @Transactional
    public void deleteMessageTemplateBatch(String ids) {
        if (ids == null || ids.isBlank()) {
            return;
        }
        for (String id : ids.split(",")) {
            if (!id.trim().isEmpty()) {
                deleteMessageTemplate(id.trim());
            }
        }
    }

    @Transactional
    public Map<String, Object> sendTestMessage(SendMessageTestRequest req) {
        if (req == null || req.getTemplateCode() == null || req.getTemplateCode().isBlank()) {
            throw new IllegalArgumentException("발송할 템플릿 코드가 필요합니다.");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, template_name, template_content FROM sys_message_template WHERE template_code = ?",
                req.getTemplateCode().trim()
        );
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("등록되지 않은 메시지 템플릿 코드입니다: " + req.getTemplateCode());
        }
        String receiver = (req.getReceiver() != null && !req.getReceiver().isBlank()) ? req.getReceiver().trim() : "admin";
        String logId = "log-" + UUID.randomUUID().toString().substring(0, 10);
        jdbcTemplate.update(
                "INSERT INTO sys_log (id, log_type, log_content, operate_type, userid, username, ip, method, request_url, request_param, client_type, cost_time) " +
                "VALUES (?, 2, ?, 2, 'usr-kkumz-001', 'admin', '127.0.0.1', 'POST', '/api/v1/kkumz/sys/message/sysMessageTemplate/sendMsg', ?, 'WEB', 12)",
                logId,
                "알림 템플릿 테스트 발송: " + req.getTemplateCode().trim() + " -> " + receiver,
                req.getTestData() != null ? req.getTestData() : "{}"
        );
        return Map.of("logId", logId, "templateCode", req.getTemplateCode().trim(), "receiver", receiver);
    }

    public Map<String, Object> listLogs(
            Integer logType,
            String keyWord,
            Integer operateType,
            String createTimeBegin,
            String createTimeEnd,
            int pageNo,
            int pageSize
    ) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, log_type, log_content, operate_type, userid, username, ip, method, request_url, request_param, client_type, cost_time, create_time " +
                "FROM sys_log WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (logType != null) {
            sql.append(" AND log_type = ?");
            params.add(logType);
        }
        if (operateType != null) {
            sql.append(" AND operate_type = ?");
            params.add(operateType);
        }
        if (keyWord != null && !keyWord.isBlank()) {
            String pattern = "%" + stripWildcards(keyWord).toLowerCase() + "%";
            sql.append(" AND (LOWER(log_content) LIKE ? OR LOWER(COALESCE(username, '')) LIKE ? OR LOWER(COALESCE(request_url, '')) LIKE ?)");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (createTimeBegin != null && !createTimeBegin.isBlank()) {
            String trimmedBegin = createTimeBegin.trim();
            String boundBegin = trimmedBegin.length() == 10 ? trimmedBegin + " 00:00:00" : trimmedBegin;
            sql.append(" AND create_time >= CAST(? AS TIMESTAMP WITH TIME ZONE)");
            params.add(boundBegin);
        }
        if (createTimeEnd != null && !createTimeEnd.isBlank()) {
            String trimmedEnd = createTimeEnd.trim();
            String boundEnd = trimmedEnd.length() == 10 ? trimmedEnd + " 23:59:59.999" : trimmedEnd;
            sql.append(" AND create_time <= CAST(? AS TIMESTAMP WITH TIME ZONE)");
            params.add(boundEnd);
        }
        sql.append(" ORDER BY create_time DESC, id DESC");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        List<Map<String, Object>> mapped = rows.stream().map(row -> {
            Integer opType = row.get("operate_type") instanceof Number n ? n.intValue() : null;
            String clientType = row.get("client_type") != null ? String.valueOf(row.get("client_type")) : "WEB";
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.get("id"));
            item.put("logType", row.get("log_type"));
            item.put("logContent", row.get("log_content"));
            item.put("operateType", opType);
            item.put("operateType_dictText", opType != null ? OPERATE_TYPE_LABELS.getOrDefault(opType, String.valueOf(opType)) : "");
            item.put("userid", row.get("userid"));
            item.put("username", row.get("username"));
            item.put("ip", row.get("ip"));
            item.put("method", row.get("method"));
            item.put("requestUrl", row.get("request_url"));
            item.put("requestParam", row.get("request_param"));
            item.put("clientType", clientType);
            item.put("clientType_dictText", CLIENT_TYPE_LABELS.getOrDefault(clientType, clientType));
            item.put("costTime", row.get("cost_time"));
            item.put("createTime", formatTimestamp(row.get("create_time")));
            return item;
        }).toList();
        return toPageResult(mapped, pageNo, pageSize);
    }

    public Map<String, Object> getHelpGuide(String appCode, String path, String routeName) {
        String resolvedApp = (appCode != null && !appCode.isBlank()) ? appCode.trim() : "KKUMZ";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM sys_page_help WHERE del_flag = 0 AND app_code = ? AND (page_key = ? OR route_name = ?) ORDER BY sort_order ASC LIMIT 1",
                resolvedApp, path != null ? path.trim() : "", routeName != null ? routeName.trim() : ""
        );
        if (rows.isEmpty()) {
            return null;
        }
        return mapPageHelp(rows.get(0));
    }

    public List<Map<String, Object>> listPageHelp(String appCode, String category, String keyword, String status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM sys_page_help WHERE del_flag = 0");
        List<Object> params = new ArrayList<>();
        if (appCode != null && !appCode.isBlank()) {
            sql.append(" AND app_code = ?");
            params.add(appCode.trim());
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND LOWER(category) LIKE ?");
            params.add("%" + category.trim().toLowerCase() + "%");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
            params.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(title) LIKE ? OR LOWER(page_key) LIKE ? OR LOWER(summary) LIKE ?)");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        sql.append(" ORDER BY sort_order ASC, update_time DESC");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray())
                .stream()
                .map(this::mapPageHelp)
                .toList();
    }

    public Map<String, Object> getHelpById(String id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM sys_page_help WHERE id = ? AND del_flag = 0",
                id
        );
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("도움말 항목을 찾을 수 없습니다: " + id);
        }
        return mapPageHelp(rows.get(0));
    }

    @Transactional
    public Map<String, Object> savePageHelp(SavePageHelpRequest req) {
        if (req == null || req.getPageKey() == null || req.getPageKey().isBlank()) {
            throw new IllegalArgumentException("화면 경로(pageKey)는 필수입니다.");
        }
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new IllegalArgumentException("도움말 제목(title)은 필수입니다.");
        }
        String id = (req.getId() != null && !req.getId().isBlank())
                ? req.getId().trim()
                : "help_kkumz_" + UUID.randomUUID().toString().substring(0, 8);
        String appCode = (req.getAppCode() != null && !req.getAppCode().isBlank()) ? req.getAppCode().trim() : "KKUMZ";
        String category = (req.getCategory() != null && !req.getCategory().isBlank()) ? req.getCategory().trim() : "시스템 관리";
        String summary = req.getSummary() != null ? req.getSummary() : "";
        String overview = req.getOverview() != null ? req.getOverview() : "";
        String status = (req.getStatus() != null && !req.getStatus().isBlank()) ? req.getStatus().trim() : "ACTIVE";
        int sortOrder = req.getSortOrder() != null ? req.getSortOrder() : 0;

        String featuresJson = toJsonText(req.getFeaturesJson(), req.getFeatures(), "[]");
        String lineageJson = toJsonText(req.getLineageJson(), req.getLineage(), "{}");
        String formulasJson = toJsonText(req.getFormulasJson(), req.getFormulas(), "[]");
        String stepsJson = toJsonText(req.getStepsJson(), req.getSteps(), "[]");
        String relatedJson = toJsonText(req.getRelatedScreensJson(), req.getRelatedScreens(), "[]");
        String tipsJson = toJsonText(req.getTipsJson(), req.getTips(), "[]");
        String warningsJson = toJsonText(req.getWarningsJson(), req.getWarnings(), "[]");
        String faqJson = toJsonText(req.getFaqJson(), req.getFaq(), "[]");

        Integer exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_page_help WHERE id = ?", Integer.class, id);
        if (exists != null && exists > 0) {
            jdbcTemplate.update(
                    "UPDATE sys_page_help SET app_code = ?, page_key = ?, route_name = ?, title = ?, category = ?, icon = ?, " +
                    "summary = ?, overview = ?, features_json = CAST(? AS jsonb), lineage_json = CAST(? AS jsonb), " +
                    "formulas_json = CAST(? AS jsonb), steps_json = CAST(? AS jsonb), related_screens_json = CAST(? AS jsonb), " +
                    "tips_json = CAST(? AS jsonb), warnings_json = CAST(? AS jsonb), faq_json = CAST(? AS jsonb), " +
                    "ai_prompt_hint = ?, status = ?, sort_order = ?, del_flag = 0, update_by = 'admin', update_time = CURRENT_TIMESTAMP WHERE id = ?",
                    appCode, req.getPageKey().trim(), req.getRouteName(), req.getTitle().trim(), category, req.getIcon(),
                    summary, overview, featuresJson, lineageJson, formulasJson, stepsJson, relatedJson,
                    tipsJson, warningsJson, faqJson, req.getAiPromptHint(), status, sortOrder, id
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO sys_page_help (id, app_code, page_key, route_name, title, category, icon, summary, overview, " +
                    "features_json, lineage_json, formulas_json, steps_json, related_screens_json, tips_json, warnings_json, faq_json, " +
                    "ai_prompt_hint, status, sort_order, del_flag, create_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS jsonb), CAST(? AS jsonb), CAST(? AS jsonb), CAST(? AS jsonb), " +
                    "CAST(? AS jsonb), CAST(? AS jsonb), CAST(? AS jsonb), CAST(? AS jsonb), ?, ?, ?, 0, 'admin')",
                    id, appCode, req.getPageKey().trim(), req.getRouteName(), req.getTitle().trim(), category, req.getIcon(),
                    summary, overview, featuresJson, lineageJson, formulasJson, stepsJson, relatedJson,
                    tipsJson, warningsJson, faqJson, req.getAiPromptHint(), status, sortOrder
            );
        }
        return getHelpById(id);
    }

    @Transactional
    public void deletePageHelp(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        jdbcTemplate.update("UPDATE sys_page_help SET del_flag = 1, update_time = CURRENT_TIMESTAMP WHERE id = ?", id.trim());
    }

    @Transactional
    public void deletePageHelpBatch(String ids) {
        if (ids == null || ids.isBlank()) {
            return;
        }
        for (String id : ids.split(",")) {
            if (!id.trim().isEmpty()) {
                deletePageHelp(id.trim());
            }
        }
    }

    private Map<String, Object> mapDepartNode(Map<String, Object> row) {
        String id = String.valueOf(row.get("id"));
        String name = String.valueOf(row.get("depart_name"));
        long childCount = row.get("child_count") instanceof Number n ? n.longValue() : 0L;
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", id);
        node.put("key", id);
        node.put("value", id);
        node.put("title", name);
        node.put("departName", name);
        node.put("departNameEn", row.get("depart_name_en"));
        node.put("parentId", row.get("parent_id"));
        node.put("orgCode", row.get("org_code"));
        node.put("orgCategory", row.get("org_category"));
        node.put("orgType", row.get("org_type"));
        node.put("departOrder", row.get("depart_order"));
        node.put("description", row.get("description"));
        node.put("mobile", row.get("mobile"));
        node.put("fax", row.get("fax"));
        node.put("address", row.get("address"));
        node.put("memo", row.get("memo"));
        node.put("status", row.get("status"));
        node.put("isLeaf", childCount == 0L);
        return node;
    }

    private Map<String, Object> mapPageHelp(Map<String, Object> row) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", row.get("id"));
        item.put("appCode", row.get("app_code"));
        item.put("pageKey", row.get("page_key"));
        item.put("routeName", row.get("route_name"));
        item.put("title", row.get("title"));
        item.put("category", row.get("category"));
        item.put("icon", row.get("icon"));
        item.put("summary", row.get("summary"));
        item.put("overview", row.get("overview"));

        String featuresStr = asJsonString(row.get("features_json"), "[]");
        String lineageStr = asJsonString(row.get("lineage_json"), "{}");
        String formulasStr = asJsonString(row.get("formulas_json"), "[]");
        String stepsStr = asJsonString(row.get("steps_json"), "[]");
        String relatedStr = asJsonString(row.get("related_screens_json"), "[]");
        String tipsStr = asJsonString(row.get("tips_json"), "[]");
        String warningsStr = asJsonString(row.get("warnings_json"), "[]");
        String faqStr = asJsonString(row.get("faq_json"), "[]");

        item.put("featuresJson", featuresStr);
        item.put("lineageJson", lineageStr);
        item.put("formulasJson", formulasStr);
        item.put("stepsJson", stepsStr);
        item.put("relatedScreensJson", relatedStr);
        item.put("tipsJson", tipsStr);
        item.put("warningsJson", warningsStr);
        item.put("faqJson", faqStr);

        item.put("features", parseJsonObject(featuresStr, Collections.emptyList()));
        item.put("lineage", parseJsonObject(lineageStr, Collections.emptyMap()));
        item.put("formulas", parseJsonObject(formulasStr, Collections.emptyList()));
        item.put("steps", parseJsonObject(stepsStr, Collections.emptyList()));
        item.put("relatedScreens", parseJsonObject(relatedStr, Collections.emptyList()));
        item.put("tips", parseJsonObject(tipsStr, Collections.emptyList()));
        item.put("warnings", parseJsonObject(warningsStr, Collections.emptyList()));
        item.put("faq", parseJsonObject(faqStr, Collections.emptyList()));

        item.put("aiPromptHint", row.get("ai_prompt_hint"));
        item.put("status", row.get("status"));
        item.put("sortOrder", row.get("sort_order"));
        item.put("updateTime", formatTimestamp(row.get("update_time")));
        return item;
    }

    private String toJsonText(String rawJson, Object structured, String defaultVal) {
        if (rawJson != null && !rawJson.isBlank()) {
            return rawJson.trim();
        }
        if (structured != null) {
            try {
                return objectMapper.writeValueAsString(structured);
            } catch (Exception e) {
                throw new IllegalArgumentException("JSON 직렬화에 실패했습니다: " + e.getMessage(), e);
            }
        }
        return defaultVal;
    }

    private String asJsonString(Object val, String defaultVal) {
        if (val == null) {
            return defaultVal;
        }
        String text = val.toString();
        return text.isBlank() ? defaultVal : text;
    }

    private Object parseJsonObject(String json, Object defaultVal) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            log.warn("sys_page_help JSON 파싱 경고: {}", e.getMessage());
            return defaultVal;
        }
    }

    private String stripWildcards(String input) {
        return input.replace("*", "").trim();
    }

    private String formatTimestamp(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Timestamp ts) {
            return ts.toLocalDateTime().toString().replace('T', ' ');
        }
        if (val instanceof OffsetDateTime odt) {
            return odt.toLocalDateTime().toString().replace('T', ' ');
        }
        return val.toString();
    }

    private Map<String, Object> toPageResult(List<Map<String, Object>> allRecords, int pageNo, int pageSize) {
        int safePageNo = Math.max(1, pageNo);
        int safePageSize = Math.max(1, pageSize);
        int total = allRecords != null ? allRecords.size() : 0;
        int fromIndex = (safePageNo - 1) * safePageSize;
        List<Map<String, Object>> pagedRecords = (allRecords == null || fromIndex >= total)
                ? Collections.emptyList()
                : allRecords.subList(fromIndex, Math.min(fromIndex + safePageSize, total));
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("records", pagedRecords);
        page.put("items", pagedRecords);
        page.put("total", total);
        page.put("current", safePageNo);
        page.put("size", safePageSize);
        return page;
    }
}
