package com.empasy.sync.kkumz.system;

import com.empasy.sync.core.i18n.SyncLocaleConstants;
import com.empasy.sync.core.i18n.SyncMessageResolver;
import com.empasy.sync.kkumz.common.Result;
import com.empasy.sync.kkumz.system.KkumzSystemDTO.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Kkumz 시스템 인증, 동적 권한 메뉴 인가(/api/v1/kkumz/sys/permission/getUserPermissionByToken),
 * 사용자·역할·메뉴·공통코드 관리 컨트롤러 (단일 Canonical URL 표준)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/kkumz/sys")
@RequiredArgsConstructor
public class KkumzSystemController {

    private final KkumzSystemService systemService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody(required = false) LoginRequest request) {
        log.info("Kkumz Admin/Web 로그인 요청: username={}", request != null ? request.getUsername() : "admin");
        return Result.OK(systemService.login(request));
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        log.info("Kkumz Web 셀프 회원가입 요청: username={}, roleCode={}",
                request != null ? request.getUsername() : null,
                request != null ? request.getRoleCode() : null);
        return Result.OK(SyncMessageResolver.localized("회원가입이 완료되었습니다.", "Registration completed successfully."),
                systemService.register(request));
    }

    @GetMapping("/logout")
    public Result<String> logout() {
        return Result.OK("");
    }

    @GetMapping("/user/getUserInfo")
    public Result<Map<String, Object>> getUserInfo(
            @RequestParam(required = false) String username,
            @RequestHeader(value = "X-Access-Token", required = false) String xAccessToken,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return Result.OK(systemService.getUserInfo(resolveUsername(username, xAccessToken, authorization)));
    }

    @PutMapping("/user/locale")
    public Result<Map<String, Object>> updateUserLocale(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) String locale,
            @RequestParam(required = false) String username,
            @RequestHeader(value = "X-Access-Token", required = false) String xAccessToken,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String resolvedUsername = resolveUsername(username, xAccessToken, authorization);
        String rawLocale = locale;
        if ((rawLocale == null || rawLocale.isBlank()) && body != null) {
            Object locObj = body.get("locale") != null ? body.get("locale") : body.get("preferredLocale");
            if (locObj != null) {
                rawLocale = String.valueOf(locObj);
            }
        }
        Map<String, Object> updated = systemService.updateUserLocale(resolvedUsername, rawLocale);
        String normalized = String.valueOf(updated.get("preferredLocale"));
        Locale targetLocale = SyncLocaleConstants.toLocale(normalized);
        return Result.OK(SyncMessageResolver.resolve("user.locale.updated", targetLocale), updated);
    }

    @GetMapping("/permission/getUserPermissionByToken")
    public Result<Map<String, Object>> getUserPermissionByToken(
            @RequestParam(required = false) String username,
            @RequestHeader(value = "X-Access-Token", required = false) String xAccessToken,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return Result.OK(systemService.getUserPermissionByToken(resolveUsername(username, xAccessToken, authorization)));
    }

    private String resolveUsername(String usernameParam, String xAccessToken, String authorization) {
        if (usernameParam != null && !usernameParam.isBlank()) {
            return usernameParam.trim();
        }
        String extracted = KkumzSystemService.extractUsernameFromToken(xAccessToken, authorization);
        if (extracted != null && !extracted.isBlank()) {
            return extracted;
        }
        throw new IllegalArgumentException(SyncMessageResolver.resolve("auth.login.required"));
    }

    @GetMapping("/user/list")
    public Result<Map<String, Object>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return Result.OK(systemService.listUsers(keyword, status, pageNo, pageSize));
    }

    @PostMapping("/user")
    public Result<Map<String, Object>> createUser(@RequestBody SaveUserRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.save.success"), systemService.saveUser(request));
    }

    @PutMapping("/user")
    public Result<Map<String, Object>> updateUser(@RequestBody SaveUserRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.update.success"), systemService.saveUser(request));
    }

    @DeleteMapping("/user/{id}")
    public Result<String> deleteUser(@PathVariable String id) {
        systemService.deleteUser(id);
        return Result.OK(SyncMessageResolver.resolve("common.delete.success"), null);
    }

    @GetMapping("/role/list")
    public Result<Map<String, Object>> listRoles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return Result.OK(systemService.listRoles(keyword, pageNo, pageSize));
    }

    @PostMapping("/role")
    public Result<Map<String, Object>> createRole(@RequestBody SaveRoleRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.save.success"), systemService.saveRole(request));
    }

    @PutMapping("/role")
    public Result<Map<String, Object>> updateRole(@RequestBody SaveRoleRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.update.success"), systemService.saveRole(request));
    }

    @DeleteMapping("/role/{id}")
    public Result<String> deleteRole(@PathVariable String id) {
        systemService.deleteRole(id);
        return Result.OK(SyncMessageResolver.resolve("common.delete.success"), null);
    }

    @GetMapping("/permission/list")
    public Result<List<Map<String, Object>>> listPermissions() {
        return Result.OK(systemService.listPermissions());
    }

    @PostMapping("/permission")
    public Result<Map<String, Object>> createPermission(@RequestBody SavePermissionRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.save.success"), systemService.savePermission(request));
    }

    @PutMapping("/permission")
    public Result<Map<String, Object>> updatePermission(@RequestBody SavePermissionRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.update.success"), systemService.savePermission(request));
    }

    @DeleteMapping("/permission/{id}")
    public Result<String> deletePermission(@PathVariable String id) {
        systemService.deletePermission(id);
        return Result.OK(SyncMessageResolver.resolve("common.delete.success"), null);
    }
}
