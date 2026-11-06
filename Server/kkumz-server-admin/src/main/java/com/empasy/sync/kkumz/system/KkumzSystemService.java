package com.empasy.sync.kkumz.system;

import cn.hutool.crypto.digest.BCrypt;
import com.empasy.sync.kkumz.system.KkumzSystemDTO.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Kkumz 시스템 인가(RBAC 동적 라우팅 메뉴 트리), 인증 및 사용자·역할·메뉴·공통코드 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KkumzSystemService {

    private final JdbcTemplate jdbcTemplate;

    private String normalizeUsername(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("사용자 계정 식별자(username)가 누락되었습니다.");
        }
        return raw.trim();
    }

    public Map<String, Object> login(LoginRequest req) {
        if (req == null || req.getUsername() == null || req.getUsername().isBlank()) {
            throw new IllegalArgumentException("로그인 아이디(username)는 필수입니다.");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new IllegalArgumentException("로그인 비밀번호(password)는 필수입니다.");
        }
        String username = normalizeUsername(req.getUsername());
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "SELECT id, username, realname, password, avatar, email, phone, org_code AS \"orgCode\", status " +
                "FROM sys_user WHERE username = ? AND del_flag = 0 LIMIT 1",
                username
        );
        if (users.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자 계정입니다: " + username);
        }
        Map<String, Object> user = new LinkedHashMap<>(users.get(0));
        String storedHash = user.remove("password") != null ? String.valueOf(users.get(0).get("password")) : "";
        if (storedHash.startsWith("$2")) {
            if (!BCrypt.checkpw(req.getPassword(), storedHash)) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        } else if (!storedHash.isBlank() && !storedHash.equals(req.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        List<Map<String, Object>> firstMenu = jdbcTemplate.queryForList("""
                SELECT p.url
                FROM sys_permission p
                JOIN sys_role_permission rp ON rp.permission_id = p.id
                JOIN sys_user_role ur ON ur.role_id = rp.role_id
                WHERE ur.user_id = ? AND p.del_flag = 0 AND p.status = 1 AND p.menu_type = 1
                ORDER BY p.sort_no ASC
                LIMIT 1
                """, user.get("id"));
        String homePath = !firstMenu.isEmpty() ? String.valueOf(firstMenu.get(0).get("url")) : "/dashboard/analysis";
        user.put("homePath", homePath);

        String token = "kkumz-jwt-token-" + user.get("username") + "-" + System.currentTimeMillis();
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("token", token);
        res.put("userInfo", user);
        res.put("roles", getUserRoles(String.valueOf(user.get("id"))));
        return res;
    }

    public Map<String, Object> getUserInfo(String usernameHint) {
        String username = normalizeUsername(usernameHint);
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "SELECT id, username, realname, avatar, email, phone, org_code AS \"orgCode\", status " +
                "FROM sys_user WHERE username = ? AND del_flag = 0 LIMIT 1",
                username
        );
        if (users.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자 계정입니다: " + username);
        }
        Map<String, Object> user = new LinkedHashMap<>(users.get(0));
        user.put("userId", user.get("id"));
        user.put("roles", getUserRoles(String.valueOf(user.get("id"))));

        Map<String, Object> res = new LinkedHashMap<>(user);
        res.put("userInfo", user);
        res.put("sysAllDictItems", Map.of());
        return res;
    }

    public List<Map<String, Object>> getUserRoles(String userId) {
        return jdbcTemplate.queryForList(
                "SELECT r.id, r.role_name AS \"roleName\", r.role_code AS \"roleCode\", r.role_code AS \"value\" " +
                "FROM sys_role r JOIN sys_user_role ur ON ur.role_id = r.id WHERE ur.user_id = ?",
                userId
        );
    }

    /**
     * 전사 표준 동적 메뉴/권한 조회 (/api/v1/kkumz/sys/permission/getUserPermissionByToken)
     */
    public Map<String, Object> getUserPermissionByToken(String usernameHint) {
        String username = normalizeUsername(usernameHint);
        List<Map<String, Object>> allPerms = jdbcTemplate.queryForList(
                "SELECT DISTINCT p.id, p.parent_id AS \"parentId\", p.name, p.url, p.component, p.component_name AS \"componentName\", " +
                "p.redirect, p.menu_type AS \"menuType\", p.perms, p.sort_no AS \"sortNo\", p.icon, " +
                "p.is_route AS \"isRoute\", p.is_leaf AS \"isLeaf\", p.keep_alive AS \"keepAlive\", " +
                "p.hidden, p.status " +
                "FROM sys_permission p " +
                "JOIN sys_role_permission rp ON rp.permission_id = p.id " +
                "JOIN sys_user_role ur ON ur.role_id = rp.role_id " +
                "JOIN sys_user u ON u.id = ur.user_id " +
                "WHERE u.username = ? AND u.del_flag = 0 AND p.del_flag = 0 AND p.status = 1 " +
                "ORDER BY p.sort_no ASC, p.id ASC",
                username
        );

        List<Map<String, Object>> menuItems = new ArrayList<>();
        List<Map<String, Object>> authItems = new ArrayList<>();
        List<String> codeList = new ArrayList<>();

        for (Map<String, Object> row : allPerms) {
            int menuType = row.get("menuType") != null ? ((Number) row.get("menuType")).intValue() : 1;
            String perms = row.get("perms") != null ? String.valueOf(row.get("perms")) : null;
            if (perms != null && !perms.isBlank()) {
                codeList.add(perms);
                Map<String, Object> auth = new LinkedHashMap<>();
                auth.put("action", perms);
                auth.put("describe", row.get("name"));
                auth.put("type", "1");
                auth.put("status", "1");
                auth.put("isAuth", true);
                authItems.add(auth);
            }
            if (menuType == 0 || menuType == 1) {
                menuItems.add(row);
            }
        }

        List<Map<String, Object>> routeTree = buildRouteTree(menuItems, null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("menu", routeTree);
        result.put("auth", authItems);
        result.put("allAuth", authItems);
        result.put("codeList", codeList);
        result.put("sysSafeMode", false);
        return result;
    }

    private List<Map<String, Object>> buildRouteTree(List<Map<String, Object>> rows, String parentId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String rowParent = row.get("parentId") != null ? String.valueOf(row.get("parentId")).trim() : "";
            boolean isRoot = (parentId == null || parentId.isBlank()) && rowParent.isEmpty();
            boolean isChild = parentId != null && !parentId.isBlank() && parentId.equals(rowParent);
            if (!isRoot && !isChild) {
                continue;
            }

            String id = String.valueOf(row.get("id"));
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", id);
            node.put("path", row.get("url"));
            node.put("name", row.get("componentName") != null ? row.get("componentName") : id);
            node.put("component", row.get("component"));
            if (row.get("redirect") != null && !String.valueOf(row.get("redirect")).isBlank()) {
                node.put("redirect", row.get("redirect"));
            }
            node.put("route", row.get("isRoute") != null ? row.get("isRoute") : true);

            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("title", row.get("name"));
            meta.put("icon", row.get("icon"));
            meta.put("keepAlive", Boolean.TRUE.equals(row.get("keepAlive")));
            meta.put("hideMenu", Boolean.TRUE.equals(row.get("hidden")));
            meta.put("orderNo", row.get("sortNo"));
            node.put("meta", meta);

            List<Map<String, Object>> children = buildRouteTree(rows, id);
            if (!children.isEmpty()) {
                node.put("children", children);
            }
            list.add(node);
        }
        return list;
    }

    // =========================================================================
    // 사용자(sys_user) CRUD
    // =========================================================================

    public Map<String, Object> listUsers(String keyword, Integer status, int pageNo, int pageSize) {
        StringBuilder sql = new StringBuilder(
                "SELECT u.id, u.username, u.realname, u.avatar, u.email, u.phone, u.org_code AS \"orgCode\", " +
                "u.status, u.create_time AS \"createTime\" FROM sys_user u WHERE u.del_flag = 0 "
        );
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (u.username ILIKE ? OR u.realname ILIKE ? OR u.email ILIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (status != null) {
            sql.append(" AND u.status = ? ");
            params.add(status);
        }
        sql.append(" ORDER BY u.create_time DESC ");
        List<Map<String, Object>> records = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        for (Map<String, Object> rec : records) {
            rec.put("roles", getUserRoles(String.valueOf(rec.get("id"))));
        }
        return toPageResult(records, pageNo, pageSize);
    }

    @Transactional
    public Map<String, Object> saveUser(SaveUserRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new IllegalArgumentException("사용자 아이디(username)는 필수입니다.");
        }
        String userId = (req.getId() != null && !req.getId().isBlank())
                ? req.getId().trim()
                : "user_" + UUID.randomUUID().toString().substring(0, 8);
        Integer exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user WHERE id = ?", Integer.class, userId);

        if (exists != null && exists > 0) {
            jdbcTemplate.update(
                    "UPDATE sys_user SET realname = ?, email = ?, phone = ?, org_code = ?, status = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?",
                    req.getRealname(), req.getEmail(), req.getPhone(),
                    req.getOrgCode() != null ? req.getOrgCode() : "KKUMZ",
                    req.getStatus() != null ? req.getStatus() : 1,
                    userId
            );
        } else {
            String rawPw = (req.getPassword() != null && !req.getPassword().isBlank()) ? req.getPassword().trim() : UUID.randomUUID().toString();
            String hashedPw = BCrypt.hashpw(rawPw, BCrypt.gensalt(10));
            String salt = UUID.randomUUID().toString().substring(0, 8);
            jdbcTemplate.update(
                    "INSERT INTO sys_user (id, username, realname, password, salt, email, phone, org_code, status, del_flag, create_by, create_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'admin', CURRENT_TIMESTAMP)",
                    userId, req.getUsername().trim(),
                    req.getRealname() != null ? req.getRealname().trim() : req.getUsername().trim(),
                    hashedPw, salt,
                    req.getEmail(), req.getPhone(),
                    req.getOrgCode() != null ? req.getOrgCode() : "KKUMZ",
                    req.getStatus() != null ? req.getStatus() : 1
            );
        }

        if (req.getRoleIds() != null && !req.getRoleIds().isEmpty()) {
            jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", userId);
            for (String roleId : req.getRoleIds()) {
                jdbcTemplate.update(
                        "INSERT INTO sys_user_role (id, user_id, role_id) VALUES (?, ?, ?)",
                        "sur_" + UUID.randomUUID().toString().substring(0, 8), userId, roleId
                );
            }
        }
        return getUserInfo(req.getUsername());
    }

    @Transactional
    public void deleteUser(String id) {
        jdbcTemplate.update("UPDATE sys_user SET del_flag = 1, update_time = CURRENT_TIMESTAMP WHERE id = ?", id);
    }

    // =========================================================================
    // 역할(sys_role) CRUD
    // =========================================================================

    public Map<String, Object> listRoles(String keyword, int pageNo, int pageSize) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, role_name AS \"roleName\", role_code AS \"roleCode\", description, create_time AS \"createTime\" " +
                "FROM sys_role WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (role_name ILIKE ? OR role_code ILIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }
        sql.append(" ORDER BY create_time ASC ");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        for (Map<String, Object> r : rows) {
            List<String> permIds = jdbcTemplate.queryForList(
                    "SELECT permission_id FROM sys_role_permission WHERE role_id = ?",
                    String.class, r.get("id")
            );
            r.put("permissionIds", permIds);
        }
        return toPageResult(rows, pageNo, pageSize);
    }

    @Transactional
    public Map<String, Object> saveRole(SaveRoleRequest req) {
        if (req.getRoleName() == null || req.getRoleName().isBlank()) {
            throw new IllegalArgumentException("역할명(roleName)은 필수입니다.");
        }
        String roleId = (req.getId() != null && !req.getId().isBlank())
                ? req.getId().trim()
                : "role_" + UUID.randomUUID().toString().substring(0, 8);
        Integer exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_role WHERE id = ?", Integer.class, roleId);
        if (exists != null && exists > 0) {
            jdbcTemplate.update(
                    "UPDATE sys_role SET role_name = ?, role_code = ?, description = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?",
                    req.getRoleName().trim(), req.getRoleCode(), req.getDescription(), roleId
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO sys_role (id, role_name, role_code, description, create_by, create_time) " +
                    "VALUES (?, ?, ?, ?, 'admin', CURRENT_TIMESTAMP)",
                    roleId, req.getRoleName().trim(),
                    req.getRoleCode() != null ? req.getRoleCode().trim() : roleId.toUpperCase(),
                    req.getDescription()
            );
        }

        if (req.getPermissionIds() != null) {
            jdbcTemplate.update("DELETE FROM sys_role_permission WHERE role_id = ?", roleId);
            for (String permId : req.getPermissionIds()) {
                jdbcTemplate.update(
                        "INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES (?, ?, ?)",
                        "srp_" + UUID.randomUUID().toString().substring(0, 8), roleId, permId
                );
            }
        }
        return Map.of("id", roleId, "roleName", req.getRoleName());
    }

    @Transactional
    public void deleteRole(String id) {
        jdbcTemplate.update("DELETE FROM sys_role_permission WHERE role_id = ?", id);
        jdbcTemplate.update("DELETE FROM sys_role WHERE id = ?", id);
    }

    // =========================================================================
    // 메뉴/권한(sys_permission) CRUD
    // =========================================================================

    public List<Map<String, Object>> listPermissions() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, parent_id AS \"parentId\", name, url, component, component_name AS \"componentName\", " +
                "redirect, menu_type AS \"menuType\", perms, sort_no AS \"sortNo\", icon, " +
                "is_route AS \"isRoute\", is_leaf AS \"isLeaf\", hidden, status, create_time AS \"createTime\" " +
                "FROM sys_permission WHERE del_flag = 0 ORDER BY sort_no ASC, id ASC"
        );
        return buildPermissionManageTree(rows, null);
    }

    private List<Map<String, Object>> buildPermissionManageTree(List<Map<String, Object>> rows, String parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String rowParent = row.get("parentId") != null ? String.valueOf(row.get("parentId")).trim() : "";
            boolean isRoot = (parentId == null || parentId.isBlank()) && rowParent.isEmpty();
            boolean isChild = parentId != null && !parentId.isBlank() && parentId.equals(rowParent);
            if (!isRoot && !isChild) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>(row);
            String id = String.valueOf(row.get("id"));
            List<Map<String, Object>> children = buildPermissionManageTree(rows, id);
            if (!children.isEmpty()) {
                item.put("children", children);
            }
            tree.add(item);
        }
        return tree;
    }

    @Transactional
    public Map<String, Object> savePermission(SavePermissionRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("메뉴명(name)은 필수입니다.");
        }
        String permId = (req.getId() != null && !req.getId().isBlank())
                ? req.getId().trim()
                : "perm-kkumz-" + UUID.randomUUID().toString().substring(0, 6);
        Integer exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_permission WHERE id = ?", Integer.class, permId);
        if (exists != null && exists > 0) {
            jdbcTemplate.update(
                    "UPDATE sys_permission SET parent_id = ?, name = ?, url = ?, component = ?, component_name = ?, " +
                    "redirect = ?, menu_type = ?, perms = ?, sort_no = ?, icon = ?, is_route = ?, is_leaf = ?, " +
                    "hidden = ?, status = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?",
                    req.getParentId(), req.getName().trim(), req.getUrl(), req.getComponent(), req.getComponentName(),
                    req.getRedirect(), req.getMenuType() != null ? req.getMenuType() : 1, req.getPerms(),
                    req.getSortNo() != null ? req.getSortNo() : 10.0, req.getIcon(),
                    req.getIsRoute() != null ? req.getIsRoute() : true,
                    req.getIsLeaf() != null ? req.getIsLeaf() : true,
                    req.getHidden() != null ? req.getHidden() : false,
                    req.getStatus() != null ? req.getStatus() : 1,
                    permId
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, " +
                    "menu_type, perms, sort_no, icon, is_route, is_leaf, keep_alive, hidden, status, del_flag, create_by, create_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, false, ?, ?, 0, 'admin', CURRENT_TIMESTAMP)",
                    permId, req.getParentId(), req.getName().trim(), req.getUrl(),
                    req.getComponent() != null ? req.getComponent() : "layouts/default/index",
                    req.getComponentName() != null ? req.getComponentName() : permId,
                    req.getRedirect(),
                    req.getMenuType() != null ? req.getMenuType() : 1,
                    req.getPerms(),
                    req.getSortNo() != null ? req.getSortNo() : 10.0,
                    req.getIcon() != null ? req.getIcon() : "ant-design:appstore-outlined",
                    req.getIsRoute() != null ? req.getIsRoute() : true,
                    req.getIsLeaf() != null ? req.getIsLeaf() : true,
                    req.getHidden() != null ? req.getHidden() : false,
                    req.getStatus() != null ? req.getStatus() : 1
            );
            jdbcTemplate.update(
                    "INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES (?, 'role_admin', ?)",
                    "srp_" + UUID.randomUUID().toString().substring(0, 8), permId
            );
        }
        return Map.of("id", permId, "name", req.getName());
    }

    @Transactional
    public void deletePermission(String id) {
        jdbcTemplate.update("UPDATE sys_permission SET del_flag = 1, update_time = CURRENT_TIMESTAMP WHERE id = ?", id);
    }

    // =========================================================================
    // 공통 사전 코드(sys_dict, sys_dict_item) CRUD
    // =========================================================================

    public Map<String, Object> listDicts(String keyword, int pageNo, int pageSize) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, dict_name AS \"dictName\", dict_code AS \"dictCode\", description, type, create_time AS \"createTime\" " +
                "FROM sys_dict WHERE del_flag = 0 "
        );
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (dict_name ILIKE ? OR dict_code ILIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }
        sql.append(" ORDER BY create_time ASC ");
        List<Map<String, Object>> dicts = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        for (Map<String, Object> d : dicts) {
            d.put("items", listDictItems(String.valueOf(d.get("id")), null));
        }
        return toPageResult(dicts, pageNo, pageSize);
    }

    public List<Map<String, Object>> listDictItems(String dictId, String dictCode) {
        if (dictId != null && !dictId.isBlank()) {
            return jdbcTemplate.queryForList(
                    "SELECT id, dict_id AS \"dictId\", item_text AS \"itemText\", item_value AS \"itemValue\", " +
                    "description, sort_order AS \"sortOrder\", status FROM sys_dict_item WHERE dict_id = ? ORDER BY sort_order ASC",
                    dictId
            );
        }
        if (dictCode != null && !dictCode.isBlank()) {
            return jdbcTemplate.queryForList(
                    "SELECT i.id, i.dict_id AS \"dictId\", i.item_text AS \"itemText\", i.item_value AS \"itemValue\", " +
                    "i.description, i.sort_order AS \"sortOrder\", i.status " +
                    "FROM sys_dict_item i JOIN sys_dict d ON d.id = i.dict_id WHERE d.dict_code = ? ORDER BY i.sort_order ASC",
                    dictCode
            );
        }
        return List.of();
    }

    @Transactional
    public Map<String, Object> saveDict(SaveDictRequest req) {
        if (req.getDictCode() == null || req.getDictCode().isBlank()) {
            throw new IllegalArgumentException("사전 코드(dictCode)는 필수입니다.");
        }
        String dictId = (req.getId() != null && !req.getId().isBlank())
                ? req.getId().trim()
                : "dict_" + UUID.randomUUID().toString().substring(0, 8);
        Integer exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_dict WHERE id = ?", Integer.class, dictId);
        if (exists != null && exists > 0) {
            jdbcTemplate.update(
                    "UPDATE sys_dict SET dict_name = ?, dict_code = ?, description = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?",
                    req.getDictName(), req.getDictCode().trim(), req.getDescription(), dictId
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, type, create_by, create_time) " +
                    "VALUES (?, ?, ?, ?, 0, ?, 'admin', CURRENT_TIMESTAMP)",
                    dictId, req.getDictName(), req.getDictCode().trim(), req.getDescription(),
                    req.getType() != null ? req.getType() : 0
            );
        }
        return Map.of("id", dictId, "dictCode", req.getDictCode());
    }

    @Transactional
    public Map<String, Object> saveDictItem(SaveDictItemRequest req) {
        String itemId = (req.getId() != null && !req.getId().isBlank())
                ? req.getId().trim()
                : "ditem_" + UUID.randomUUID().toString().substring(0, 8);
        Integer exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_dict_item WHERE id = ?", Integer.class, itemId);
        if (exists != null && exists > 0) {
            jdbcTemplate.update(
                    "UPDATE sys_dict_item SET item_text = ?, item_value = ?, description = ?, sort_order = ?, status = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?",
                    req.getItemText(), req.getItemValue(), req.getDescription(),
                    req.getSortOrder() != null ? req.getSortOrder() : 1,
                    req.getStatus() != null ? req.getStatus() : 1,
                    itemId
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, 'admin', CURRENT_TIMESTAMP)",
                    itemId, req.getDictId(), req.getItemText(), req.getItemValue(), req.getDescription(),
                    req.getSortOrder() != null ? req.getSortOrder() : 1,
                    req.getStatus() != null ? req.getStatus() : 1
            );
        }
        return Map.of("id", itemId, "itemText", req.getItemText());
    }

    @Transactional
    public void deleteDict(String id) {
        jdbcTemplate.update("UPDATE sys_dict SET del_flag = 1, update_time = CURRENT_TIMESTAMP WHERE id = ?", id);
    }

    @Transactional
    public void deleteDictItem(String id) {
        jdbcTemplate.update("DELETE FROM sys_dict_item WHERE id = ?", id);
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
