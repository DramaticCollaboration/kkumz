package com.empasy.sync.kkumz.system;

import com.empasy.sync.kkumz.common.Result;
import com.empasy.sync.kkumz.system.KkumzSystemOpsDTO.SavePageHelpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Kkumz 화면별 사용자 가이드 및 도움말 마스터 컨트롤러 (/api/v1/kkumz/sys/help)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/kkumz/sys/help")
@RequiredArgsConstructor
public class KkumzSysHelpController {

    private final KkumzSystemOpsService opsService;

    @GetMapping("/guide")
    public Result<Map<String, Object>> getGuideByPath(
            @RequestParam(required = false, defaultValue = "KKUMZ") String appCode,
            @RequestParam String path,
            @RequestParam(required = false) String routeName
    ) {
        return Result.OK(opsService.getHelpGuide(appCode, path, routeName));
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> listHelp(
            @RequestParam(required = false, defaultValue = "KKUMZ") String appCode,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        return Result.OK(opsService.listPageHelp(appCode, category, keyword, status));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable String id) {
        return Result.OK(opsService.getHelpById(id));
    }

    @PostMapping("/save")
    public Result<Map<String, Object>> saveHelp(@RequestBody SavePageHelpRequest request) {
        return Result.OK("도움말 가이드가 저장되었습니다.", opsService.savePageHelp(request));
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteById(@PathVariable String id) {
        opsService.deletePageHelp(id);
        return Result.OK("도움말 가이드가 삭제되었습니다.", null);
    }

    @DeleteMapping("/batch")
    public Result<String> deleteBatch(@RequestParam String ids) {
        opsService.deletePageHelpBatch(ids);
        return Result.OK("선택한 도움말 가이드가 삭제되었습니다.", null);
    }
}
