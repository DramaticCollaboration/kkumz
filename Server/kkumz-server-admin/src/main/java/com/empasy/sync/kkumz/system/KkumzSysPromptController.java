package com.empasy.sync.kkumz.system;

import com.empasy.sync.core.prompt.controller.AbstractSysPromptController;
import com.empasy.sync.core.prompt.service.SystemPromptService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [Kkumz / 시스템 관리] AI 프롬프트 템플릿 관리·버전 롤백·실행 테스트 REST Controller
 */
@RestController
@RequestMapping("/api/v1/kkumz/sys/prompt")
@Tag(name = "Kkumz System Prompt API", description = "Kkumz 표준 AI 프롬프트 템플릿 조회·편집·이력·롤백·실행 테스트 API")
public class KkumzSysPromptController extends AbstractSysPromptController {

    public KkumzSysPromptController(SystemPromptService promptService) {
        super(promptService);
    }

    @Override
    protected String getDefaultAppCode() {
        return "KKUMZ";
    }
}
