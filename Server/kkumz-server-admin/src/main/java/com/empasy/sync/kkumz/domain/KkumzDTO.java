package com.empasy.sync.kkumz.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Kkumz(꿈즈) AI 브랜드·멀티사이트 빌더, 창업 팀빌딩 커뮤니티 및 전용 Admin 백오피스 DTO 모음
 */
public class KkumzDTO {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateIdeaRequest {
        private String title;
        private String promptText;
        private String siteType;
        private String category;
        private List<String> tags;
        private String visibility;
        private String authorUsername;
        private String authorDisplayName;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveIdeaVersionRequest {
        private String title;
        private String promptText;
        private String specMarkdown;
        private List<Map<String, Object>> screens;
        private Integer rollbackToVersionNo;
        private String createdBy;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateNamesRequest {
        private String keywordHint;
        private String toneStyle;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateLogosRequest {
        private String brandName;
        private String siteSlug;
        private String stylePreference;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectBrandRequest {
        private String nameSuggestionId;
        private String logoSuggestionId;
        private String brandName;
        private String siteSlug;
        private String logoSvg;
        private Map<String, Object> designTokens;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateSpecRequest {
        private String title;
        private String promptText;
        private String specMarkdown;
        private List<Map<String, Object>> screens;
        private String updatedBy;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveScreenSpecRequest {
        private String id;
        private String screenKey;
        private String screenTitle;
        private String routePath;
        private String layoutType;
        private String description;
        private List<String> components;
        private Integer sortNo;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRefineRequest {
        private String instruction;
        private String viewport;
        private Map<String, Object> tokenOverrides;
        private Map<String, Object> layoutOverrides;
        private String username;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateWebIdeFilesRequest {
        private List<Map<String, Object>> files;
        private String commitMessage;
        private Boolean simulateSyntaxError;
        private String username;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeploySiteRequest {
        private String deployMode;
        private String releaseNote;
        private String visibility;
        private String username;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FixBuildRequest {
        private String errorContext;
        private String username;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommerceOrderRequest {
        private Long productId;
        private Integer quantity;
        private String receiverName;
        private String receiverPhone;
        private String receiverAddress;
        private String note;
        private BigDecimal discountAmount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CmsFormSubmitRequest {
        private Long formId;
        private String applicantName;
        private String phone;
        private String optionValue;
        private String message;
        private Map<String, Object> extraFields;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateCommentRequest {
        private String username;
        private String displayName;
        private String authorRoleBadge;
        private String content;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRecruitRequest {
        private String positionRole;
        private String title;
        private String description;
        private List<String> requiredSkills;
        private Integer headcount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamApplyRequest {
        private String recruitId;
        private String username;
        private String displayName;
        private String positionRole;
        private String applicationMessage;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamInviteRequest {
        private String username;
        private String displayName;
        private String memberRole;
        private String positionRole;
        private String message;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateMemberStatusRequest {
        private String status;
        private String memberRole;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateSiteStatusRequest {
        private String status;
        private String visibility;
        private String customDomain;
        private Boolean isFeatured;
        private String reason;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReportRequest {
        private String targetType;
        private String targetId;
        private String reporterUsername;
        private String reasonCategory;
        private String reasonDetail;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResolveReportRequest {
        private String status;
        private String actionTaken;
        private Boolean suspendSite;
        private String moderatorUsername;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SiteConciergeRequest {
        private String question;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavePromptTemplateRequest {
        private String id;
        private String templateCode;
        private String templateName;
        private String stageType;
        private String modelName;
        private BigDecimal temperature;
        private String systemPrompt;
        private String userPromptTemplate;
        private List<String> forbiddenWords;
        private String status;
    }
}
