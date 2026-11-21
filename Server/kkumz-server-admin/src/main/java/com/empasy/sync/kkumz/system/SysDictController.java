package com.empasy.sync.kkumz.system;

import com.empasy.sync.core.i18n.SyncMessageResolver;
import com.empasy.sync.kkumz.common.Result;
import com.empasy.sync.kkumz.system.KkumzSystemDTO.SaveDictItemRequest;
import com.empasy.sync.kkumz.system.KkumzSystemDTO.SaveDictRequest;
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
 * Kkumz 공통 사전(sys_dict, sys_dict_item) 관리 컨트롤러 (/api/v1/kkumz/sys/dict)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/kkumz/sys/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final KkumzSystemService systemService;

    @GetMapping("/list")
    public Result<Map<String, Object>> listDicts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String dictName,
            @RequestParam(required = false) String dictCode,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        String resolvedKw = (keyword != null && !keyword.isBlank())
                ? keyword
                : ((dictName != null && !dictName.isBlank()) ? dictName : dictCode);
        return Result.OK(systemService.listDicts(resolvedKw, pageNo, pageSize));
    }

    @PostMapping
    public Result<Map<String, Object>> createDict(@RequestBody SaveDictRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.save.success"), systemService.saveDict(request));
    }

    @PostMapping("/add")
    public Result<Map<String, Object>> addDict(@RequestBody SaveDictRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.save.success"), systemService.saveDict(request));
    }

    @PutMapping
    public Result<Map<String, Object>> updateDict(@RequestBody SaveDictRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.update.success"), systemService.saveDict(request));
    }

    @PutMapping("/edit")
    public Result<Map<String, Object>> editDict(@RequestBody SaveDictRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.update.success"), systemService.saveDict(request));
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteDictByPath(@PathVariable String id) {
        systemService.deleteDict(id);
        return Result.OK(SyncMessageResolver.resolve("common.delete.success"), null);
    }

    @DeleteMapping("/delete")
    public Result<String> deleteDictByParam(@RequestParam String id) {
        systemService.deleteDict(id);
        return Result.OK(SyncMessageResolver.resolve("common.delete.success"), null);
    }

    @GetMapping("/items")
    public Result<List<Map<String, Object>>> listDictItems(
            @RequestParam(required = false) String dictId,
            @RequestParam(required = false) String dictCode
    ) {
        return Result.OK(systemService.listDictItems(dictId, dictCode));
    }

    @GetMapping("/itemList")
    public Result<List<Map<String, Object>>> itemList(
            @RequestParam(required = false) String dictId,
            @RequestParam(required = false) String dictCode
    ) {
        return Result.OK(systemService.listDictItems(dictId, dictCode));
    }

    @PostMapping("/item")
    public Result<Map<String, Object>> saveDictItem(@RequestBody SaveDictItemRequest request) {
        boolean isUpdate = request != null && request.getId() != null && !request.getId().isBlank();
        return Result.OK(SyncMessageResolver.resolve(isUpdate ? "common.update.success" : "common.save.success"),
                systemService.saveDictItem(request));
    }

    @PostMapping("/addItem")
    public Result<Map<String, Object>> addDictItem(@RequestBody SaveDictItemRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.save.success"), systemService.saveDictItem(request));
    }

    @PutMapping("/editItem")
    public Result<Map<String, Object>> editDictItem(@RequestBody SaveDictItemRequest request) {
        return Result.OK(SyncMessageResolver.resolve("common.update.success"), systemService.saveDictItem(request));
    }

    @DeleteMapping("/item/{id}")
    public Result<String> deleteDictItemByPath(@PathVariable String id) {
        systemService.deleteDictItem(id);
        return Result.OK(SyncMessageResolver.resolve("common.delete.success"), null);
    }

    @DeleteMapping("/deleteItem")
    public Result<String> deleteDictItemByParam(@RequestParam String id) {
        systemService.deleteDictItem(id);
        return Result.OK(SyncMessageResolver.resolve("common.delete.success"), null);
    }

    @GetMapping("/getDictItems/{dictCode}")
    public Result<List<Map<String, Object>>> getDictItems(@PathVariable String dictCode) {
        return Result.OK(systemService.getDictItemsByCode(dictCode));
    }

    @GetMapping("/queryAllDictItems")
    public Result<Map<String, List<Map<String, Object>>>> queryAllDictItems() {
        return Result.OK(systemService.queryAllDictItems());
    }

    @GetMapping("/refreshCache")
    public Result<String> refreshCache() {
        return Result.OK(SyncMessageResolver.resolve("dict.cache.refresh.success"), null);
    }
}
