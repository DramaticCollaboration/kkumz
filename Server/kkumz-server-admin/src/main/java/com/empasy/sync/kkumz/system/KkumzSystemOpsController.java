package com.empasy.sync.kkumz.system;

import com.empasy.sync.core.i18n.SyncMessageResolver;
import com.empasy.sync.kkumz.common.Result;
import com.empasy.sync.kkumz.system.KkumzSystemOpsDTO.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Kkumz 시스템 운영 확장 컨트롤러
 * (기본 홈 라우트, 부서 트리·권한, 알림 발송 템플릿, 작업 로그 조회)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/kkumz/sys")
@RequiredArgsConstructor
public class KkumzSystemOpsController {

    private final KkumzSystemOpsService opsService;

    @GetMapping("/sysRoleIndex/queryDefIndex")
    public Result<Map<String, Object>> queryDefIndex() {
        return Result.OK(opsService.queryDefIndex());
    }

    @PutMapping("/sysRoleIndex/updateDefIndex")
    public Result<Map<String, Object>> updateDefIndex(
            @RequestParam String url,
            @RequestParam String component,
            @RequestParam(required = false, defaultValue = "true") Boolean isRoute
    ) {
        return Result.OK(SyncMessageResolver.resolve("common.save.success"), opsService.updateDefIndex(url, component, isRoute));
    }

    @GetMapping("/sysDepart/queryDepartTreeSync")
    public Result<List<Map<String, Object>>> queryDepartTreeSync(
            @RequestParam(required = false) String pid,
            @RequestParam(required = false) String ids
    ) {
        return Result.OK(opsService.queryDepartTreeSync(pid, ids));
    }

    @GetMapping("/sysDepart/searchBy")
    public Result<List<Map<String, Object>>> searchDepartsByKeywords(
            @RequestParam(required = false) String keyWord
    ) {
        return Result.OK(opsService.searchDepartsByKeywords(keyWord));
    }

    @PostMapping("/sysDepart/add")
    public Result<Map<String, Object>> addDepart(@RequestBody SaveDepartRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.save.success"), opsService.saveDepart(request, false));
    }

    @PutMapping("/sysDepart/edit")
    public Result<Map<String, Object>> editDepart(@RequestBody SaveDepartRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.update.success"), opsService.saveDepart(request, true));
    }

    @DeleteMapping("/sysDepart/delete")
    public Result<String> deleteDepart(@RequestParam String id) {
        opsService.deleteDepart(id);
        return Result.OK(SyncMessageResolver.resolve("common.delete.success"), null);
    }

    @DeleteMapping("/sysDepart/deleteBatch")
    public Result<String> deleteDepartBatch(@RequestParam String ids) {
        opsService.deleteDepartBatch(ids);
        return Result.OK(SyncMessageResolver.resolve("common.delete.batch.success"), null);
    }

    @GetMapping("/permission/queryTreeList")
    public Result<Map<String, Object>> queryPermissionTreeList() {
        return Result.OK(opsService.queryPermissionTreeList());
    }

    @GetMapping("/permission/queryDepartPermission")
    public Result<List<String>> queryDepartPermission(@RequestParam String departId) {
        return Result.OK(opsService.queryDepartPermission(departId));
    }

    @PostMapping("/permission/saveDepartPermission")
    public Result<String> saveDepartPermission(@RequestBody SaveDepartPermissionRequest request) {
        opsService.saveDepartPermission(request);
        return Result.OK(SyncMessageResolver.resolve("common.save.success"), null);
    }

    @GetMapping("/message/sysMessageTemplate/list")
    public Result<Map<String, Object>> listMessageTemplates(
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) String templateCode,
            @RequestParam(required = false) String templateType,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return Result.OK(opsService.listMessageTemplates(templateName, templateCode, templateType, pageNo, pageSize));
    }

    @PostMapping("/message/sysMessageTemplate/add")
    public Result<Map<String, Object>> addMessageTemplate(@RequestBody SaveMessageTemplateRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.save.success"), opsService.saveMessageTemplate(request, false));
    }

    @PutMapping("/message/sysMessageTemplate/edit")
    public Result<Map<String, Object>> editMessageTemplate(@RequestBody SaveMessageTemplateRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.update.success"), opsService.saveMessageTemplate(request, true));
    }

    @DeleteMapping("/message/sysMessageTemplate/delete")
    public Result<String> deleteMessageTemplate(@RequestParam String id) {
        opsService.deleteMessageTemplate(id);
        return Result.OK(SyncMessageResolver.resolve("common.delete.success"), null);
    }

    @DeleteMapping("/message/sysMessageTemplate/deleteBatch")
    public Result<String> deleteMessageTemplateBatch(@RequestParam String ids) {
        opsService.deleteMessageTemplateBatch(ids);
        return Result.OK(SyncMessageResolver.resolve("common.delete.batch.success"), null);
    }

    @PostMapping("/message/sysMessageTemplate/sendMsg")
    public Result<Map<String, Object>> sendMessageTest(@RequestBody SendMessageTestRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.success"), opsService.sendTestMessage(request));
    }

    @GetMapping("/log/list")
    public Result<Map<String, Object>> listLogs(
            @RequestParam(required = false) Integer logType,
            @RequestParam(required = false) String keyWord,
            @RequestParam(required = false) Integer operateType,
            @RequestParam(name = "createTime_begin", required = false) String createTimeBegin,
            @RequestParam(name = "createTime_end", required = false) String createTimeEnd,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return Result.OK(opsService.listLogs(logType, keyWord, operateType, createTimeBegin, createTimeEnd, pageNo, pageSize));
    }
}
