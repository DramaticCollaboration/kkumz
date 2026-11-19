package com.empasy.sync.kkumz.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Kkumz 시스템 운영 확장(기본 홈 라우트, 부서 트리, 알림 템플릿, 도움말 가이드) DTO 모음
 */
public class KkumzSystemOpsDTO {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveDepartRequest {
        private String id;
        private String parentId;
        private String departName;
        private String departNameEn;
        private Integer departOrder;
        private String description;
        private String orgCategory;
        private String orgType;
        private String orgCode;
        private String mobile;
        private String fax;
        private String address;
        private String memo;
        private String status;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveDepartPermissionRequest {
        private String departId;
        private String permissionIds;
        private String lastpermissionIds;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveMessageTemplateRequest {
        private String id;
        private String templateName;
        private String templateCode;
        private String templateType;
        private String templateContent;
        private String templateTestJson;
        private String useStatus;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendMessageTestRequest {
        private String templateCode;
        private String receiver;
        private String msgType;
        private String testData;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavePageHelpRequest {
        private String id;
        private String appCode;
        private String pageKey;
        private String routeName;
        private String title;
        private String category;
        private String icon;
        private String summary;
        private String overview;
        private Object features;
        private Object lineage;
        private Object formulas;
        private Object steps;
        private Object relatedScreens;
        private Object tips;
        private Object warnings;
        private Object faq;
        private String featuresJson;
        private String lineageJson;
        private String formulasJson;
        private String stepsJson;
        private String relatedScreensJson;
        private String tipsJson;
        private String warningsJson;
        private String faqJson;
        private String aiPromptHint;
        private String status;
        private Integer sortOrder;
    }
}
