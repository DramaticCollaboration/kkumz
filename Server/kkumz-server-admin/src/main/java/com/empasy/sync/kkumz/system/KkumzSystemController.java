package com.empasy.sync.kkumz.system;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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

    @GetMapping("/logout")
    public Result<String> logout() {
        return Result.OK("로그아웃되었습니다.");
    }

    @GetMapping("/user/getUserInfo")
    public Result<Map<String, Object>> getUserInfo(@RequestParam(required = false) String username) {
        return Result.OK(systemService.getUserInfo(username));
    }

    @GetMapping("/permission/getUserPermissionByToken")
    public Result<Map<String, Object>> getUserPermissionByToken(@RequestParam(required = false) String username) {
        return Result.OK(systemService.getUserPermissionByToken(username));
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
        return Result.OK(systemService.saveUser(request));
    }

    @PutMapping("/user")
    public Result<Map<String, Object>> updateUser(@RequestBody SaveUserRequest request) {
        return Result.OK(systemService.saveUser(request));
    }

    @DeleteMapping("/user/{id}")
    public Result<String> deleteUser(@PathVariable String id) {
        systemService.deleteUser(id);
        return Result.OK("사용자가 삭제 처리되었습니다.");
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
        return Result.OK(systemService.saveRole(request));
    }

    @PutMapping("/role")
    public Result<Map<String, Object>> updateRole(@RequestBody SaveRoleRequest request) {
        return Result.OK(systemService.saveRole(request));
    }

    @DeleteMapping("/role/{id}")
    public Result<String> deleteRole(@PathVariable String id) {
        systemService.deleteRole(id);
        return Result.OK("역할이 삭제되었습니다.");
    }

    @GetMapping("/permission/list")
    public Result<List<Map<String, Object>>> listPermissions() {
        return Result.OK(systemService.listPermissions());
    }

    @PostMapping("/permission")
    public Result<Map<String, Object>> createPermission(@RequestBody SavePermissionRequest request) {
        return Result.OK(systemService.savePermission(request));
    }

    @PutMapping("/permission")
    public Result<Map<String, Object>> updatePermission(@RequestBody SavePermissionRequest request) {
        return Result.OK(systemService.savePermission(request));
    }

    @DeleteMapping("/permission/{id}")
    public Result<String> deletePermission(@PathVariable String id) {
        systemService.deletePermission(id);
        return Result.OK("메뉴 권한이 삭제되었습니다.");
    }

    @GetMapping("/dict/list")
    public Result<Map<String, Object>> listDicts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return Result.OK(systemService.listDicts(keyword, pageNo, pageSize));
    }

    @GetMapping("/dict/items")
    public Result<List<Map<String, Object>>> listDictItems(
            @RequestParam(required = false) String dictId,
            @RequestParam(required = false) String dictCode
    ) {
        return Result.OK(systemService.listDictItems(dictId, dictCode));
    }

    @PostMapping("/dict")
    public Result<Map<String, Object>> createDict(@RequestBody SaveDictRequest request) {
        return Result.OK(systemService.saveDict(request));
    }

    @PutMapping("/dict")
    public Result<Map<String, Object>> updateDict(@RequestBody SaveDictRequest request) {
        return Result.OK(systemService.saveDict(request));
    }

    @PostMapping("/dict/item")
    public Result<Map<String, Object>> saveDictItem(@RequestBody SaveDictItemRequest request) {
        return Result.OK(systemService.saveDictItem(request));
    }

    @DeleteMapping("/dict/{id}")
    public Result<String> deleteDict(@PathVariable String id) {
        systemService.deleteDict(id);
        return Result.OK("공통 사전 코드가 삭제되었습니다.");
    }
}
