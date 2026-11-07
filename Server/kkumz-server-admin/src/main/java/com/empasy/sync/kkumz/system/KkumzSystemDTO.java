package com.empasy.sync.kkumz.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Kkumz 시스템 관리(사용자, 역할, 메뉴/권한, 공통코드, 로그인) DTO 모음
 */
public class KkumzSystemDTO {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
        private String captcha;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        private String username;
        private String password;
        private String realname;
        private String email;
        private String phone;
        private String roleCode;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveUserRequest {
        private String id;
        private String username;
        private String realname;
        private String password;
        private String email;
        private String phone;
        private String orgCode;
        private Integer status;
        private List<String> roleIds;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveRoleRequest {
        private String id;
        private String roleName;
        private String roleCode;
        private String description;
        private List<String> permissionIds;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavePermissionRequest {
        private String id;
        private String parentId;
        private String name;
        private String url;
        private String component;
        private String componentName;
        private String redirect;
        private Integer menuType;
        private String perms;
        private Double sortNo;
        private String icon;
        private Boolean isRoute;
        private Boolean isLeaf;
        private Boolean hidden;
        private Integer status;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveDictRequest {
        private String id;
        private String dictName;
        private String dictCode;
        private String description;
        private Integer type;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveDictItemRequest {
        private String id;
        private String dictId;
        private String itemText;
        private String itemValue;
        private String description;
        private Integer sortOrder;
        private Integer status;
    }
}
