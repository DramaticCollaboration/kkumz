# Kkumz (꿈즈) 데이터베이스 스키마 설계서 (04_Database_스키마설계서.md)

- **문서 번호**: DOC-KZ-07 / 04_Database_스키마설계서
- **대상 데이터베이스**: PostgreSQL 18.6 (`pgvector`, 데이터베이스명: `sync_kkumz` / `kkumz`, 포트 `5432`)
- **원천 스크립트**: `services/kkumz/db/00_extensions.sql` ~ `05_sample_dev.sql` (6-File DB SSOT)
- **원천 컨트롤러**: `KkumzController.java` (`/api/v1/kkumz`), `KkumzSystemController.java` (`/api/v1/kkumz/sys`)

---

## 1. 데이터베이스 6단계 표준 파일 체계 (`services/kkumz/db/`)

| 실행 순서 | 파일명 | 역할 및 포함 객체 |
| :---: | :--- | :--- |
| **00** | `00_extensions.sql` | PostgreSQL 18.6 확장 모듈 활성화 (`uuid-ossp`, `pg_trgm`, `vector`) |
| **01** | `01_schema_system.sql` | 플랫폼 공통 시스템 테이블 **12종** (`sys_user`, `sys_role`, `sys_permission`, `sys_user_role`, `sys_role_permission`, `sys_dict`, `sys_dict_item`, `sys_log`, `sys_config`, `sys_page_help`, `sys_prompt_template`, `sys_prompt_history`) |
| **02** | `02_schema_domain.sql` | Kkumz 도메인 전용 테이블 **19종** (`kkumz_idea`, `kkumz_idea_version`, `kkumz_name_suggestion`, `kkumz_logo_suggestion`, `kkumz_screen_spec`, `kkumz_site`, `kkumz_site_release`, `kkumz_build_job`, `kkumz_team_recruit`, `kkumz_team_member`, `kkumz_feed_comment`, `kkumz_feed_like`, `kkumz_moderation_report`, `kkumz_ai_prompt_template`, `kkumz_notification`, `kkumz_idea_asset`, `kkumz_api_access_key`, `kkumz_site_git_repo`, `kkumz_feature_catalog`) |
| **03** | `03_seed_system.sql` | 4대 시스템 역할(`role_admin`, `role_moderator`, `role_creator`, `role_developer`), 6대 기본 사용자 계정, 공통 시스템 메뉴(`/system/config`, `/system/prompt` 포함), 공통 코드 사전 22종, 동적 환경설정 `sys_config` 15종 |
| **04** | `04_seed_domain.sql` | 어드민 도메인 메뉴(`perm-kkumz-dom-001` ~ `022`), 역할별 메뉴 권한 매핑, AI 프롬프트 템플릿 8종(`tpl-kkumz-001` ~ `008` 및 `sys_prompt_template`, `sys_prompt_history`), API 액세스키 4종, 52종 기능 카탈로그(`kkumz_feature_catalog`), 화면별 인앱 가이드(`sys_page_help`) |
| **05** | `05_sample_dev.sql` | [DEV/TEST] 대표 창업·크리에이터 프로젝트(`matcha-atelier`, `desk-orbit`, `seoul-run-club`, `bean-chronicle` 등)의 아이디어·버전·네이밍·로고·화면·사이트·릴리즈·빌드·팀빌딩·피드·신고·알림 시드 데이터 |

---

## 2. 도메인 엔티티 관계도 (ERD)

```mermaid
erDiagram
    sys_user ||--o{ sys_user_role : "user_id"
    sys_role ||--o{ sys_user_role : "role_id"
    sys_role ||--o{ sys_role_permission : "role_id"
    sys_permission ||--o{ sys_role_permission : "permission_id"
    sys_dict ||--o{ sys_dict_item : "dict_id"
    sys_prompt_template ||--o{ sys_prompt_history : "prompt_id"

    kkumz_idea ||--o{ kkumz_idea_version : "idea_id"
    kkumz_idea ||--o{ kkumz_idea_asset : "idea_id"
    kkumz_idea ||--o{ kkumz_name_suggestion : "idea_id"
    kkumz_idea ||--o{ kkumz_logo_suggestion : "idea_id"
    kkumz_idea ||--o{ kkumz_screen_spec : "idea_id"
    kkumz_idea ||--o| kkumz_site : "idea_id"
    kkumz_site ||--o{ kkumz_site_release : "site_id"
    kkumz_site ||--o{ kkumz_build_job : "site_id"
    kkumz_site ||--o| kkumz_site_git_repo : "site_id"
    kkumz_site ||--o{ kkumz_team_recruit : "site_id"
    kkumz_site ||--o{ kkumz_team_member : "site_id"
    kkumz_site ||--o{ kkumz_feed_comment : "site_id"
    kkumz_site ||--o{ kkumz_feed_like : "site_id"
    kkumz_site ||--o{ kkumz_moderation_report : "site_id"
```

---

## 3. 시스템 공통 테이블 명세 (`01_schema_system.sql` — 12개 테이블)

| 번호 | 테이블명 | PK | 주요 컬럼 구성 | 용도 |
| :---: | :--- | :--- | :--- | :--- |
| 1 | `sys_user` | `id` (`VARCHAR(64)`) | `username` (UK), `realname`, `password`, `salt`, `avatar`, `email`, `phone`, `org_code`, `status`, `del_flag` | 운영자, 모더레이터, 크리에이터 계정 원장 (`/system/user`) |
| 2 | `sys_role` | `id` (`VARCHAR(64)`) | `role_name`, `role_code` (UK), `description` | 시스템 역할 (`role_admin`, `role_moderator`, `role_creator`, `role_developer`) (`/system/role`) |
| 3 | `sys_permission` | `id` (`VARCHAR(64)`) | `parent_id`, `name`, `url`, `component`, `component_name`, `menu_type`, `perms`, `sort_no`, `icon`, `is_route`, `is_leaf` | 어드민 백오피스 DB 단일 원천 동적 메뉴 및 라우트 원장 (`/system/menu`) |
| 4 | `sys_user_role` | `id` (`VARCHAR(64)`) | `user_id`, `role_id` (UK: `user_id, role_id`) | 사용자-시스템 역할 매핑 |
| 5 | `sys_role_permission` | `id` (`VARCHAR(64)`) | `role_id`, `permission_id` (UK: `role_id, permission_id`) | 역할-메뉴 권한 매핑 |
| 6 | `sys_dict` | `id` (`VARCHAR(64)`) | `dict_name`, `dict_code` (UK), `description`, `del_flag` | 공통 코드 그룹 마스터 (`/system/dict`) |
| 7 | `sys_dict_item` | `id` (`VARCHAR(64)`) | `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status` | 공통 코드 상세 항목 |
| 8 | `sys_log` | `id` (`VARCHAR(64)`) | `log_type`, `log_content`, `operate_type`, `userid`, `username`, `ip`, `method`, `request_url`, `cost_time`, `create_time` | 시스템 감사 및 작업 이력 로그 (`/monitor/log`) |
| 9 | `sys_config` | `id` (`VARCHAR(32)`) | `config_category`, `config_key` (UK), `config_name`, `config_value`, `default_value`, `value_type`, `is_secret`, `is_dynamic`, `status` | `SystemConfigHolder` 및 `ClusterEventBroadcaster` 연동 무중단 동적 환경설정 (`/system/config`) |
| 10 | `sys_page_help` | `id` (`VARCHAR(64)`) | `app_code`, `page_key` (UK), `route_name`, `title`, `category`, `summary`, `overview`, `features_json`, `lineage_json`, `steps_json`, `related_screens_json`, `tips_json`, `warnings_json`, `faq_json` | 화면별 인앱 사용자 가이드 및 데이터 리니지 마스터 (`/system/help-guide`) |
| 11 | `sys_prompt_template` | `id` (`VARCHAR(64)`) | `app_code`, `prompt_code` (UK), `prompt_name`, `category`, `caller_class`, `system_prompt`, `user_prompt_template`, `variables_schema` (`JSONB`), `output_schema`, `model_override`, `temperature`, `version`, `status` | `SystemPromptHolder` 연동 전사 AI 프롬프트 템플릿 원장 (`/system/prompt`) |
| 12 | `sys_prompt_history` | `id` (`VARCHAR(64)`) | `prompt_id`, `app_code`, `prompt_code`, `version`, `system_prompt`, `user_prompt_template`, `variables_schema`, `output_schema`, `change_reason`, `created_by`, `created_at` | AI 프롬프트 변경 이력 및 버전 롤백 원장 (`/system/prompt`) |

---

## 4. Kkumz 도메인 테이블 상세 명세 (`02_schema_domain.sql` — 19개 테이블)

| 번호 | 테이블명 | PK | 주요 컬럼 및 제약조건 | 설명 및 연동 스튜디오 컴포넌트 |
| :---: | :--- | :--- | :--- | :--- |
| 1 | `kkumz_idea` | `id` (`VARCHAR(64)`) | `author_username`, `title`, `prompt_text`, `site_type`, `category`, `tags_json`, `visibility`, `current_step`, `selected_brand_name`, `selected_site_slug`, `selected_logo_svg`, `design_tokens_json`, `spec_markdown`, `screens_json`, `enabled_features_json`, `ai_intent_analysis_json`, `status` | 아이디어·세계관 및 11-Step 스튜디오 마스터 (`StudioView.vue`, `StudioStep1AssetUploadPanel.vue`) |
| 2 | `kkumz_idea_version` | `id` (`VARCHAR(64)`) | `idea_id`, `version_no` (UK: `idea_id, version_no`), `title`, `prompt_text`, `spec_markdown`, `screens_json` | 기능명세서 및 화면 설계 버전 히스토리 (`StudioStep3SpecPanel.vue`) |
| 3 | `kkumz_name_suggestion` | `id` (`VARCHAR(64)`) | `idea_id`, `brand_name`, `site_slug`, `pronunciation`, `brand_story`, `score`, `slug_available`, `is_selected` | AI 브랜드 네이밍 8종 후보 (`StudioStep2BrandPanel.vue`) |
| 4 | `kkumz_logo_suggestion` | `id` (`VARCHAR(64)`) | `idea_id`, `style_name`, `svg_code`, `color_palette_json`, `typography_json`, `tags_json`, `is_selected` | AI 벡터 SVG 로고 6종 및 컬러 팔레트 후보 (`StudioStep2BrandPanel.vue`) |
| 5 | `kkumz_screen_spec` | `id` (`VARCHAR(64)`) | `idea_id`, `screen_key`, `screen_title`, `route_path`, `layout_type`, `components_json`, `sort_no` | 화면 설계 블루프린트 목록 (`StudioStep3SpecPanel.vue`) |
| 6 | `kkumz_site` | `id` (`VARCHAR(64)`) | `idea_id`, `site_slug` (UK), `site_name`, `site_type`, `category`, `brand_slogan`, `logo_svg`, `shop_site_id`, `cms_site_id`, `deploy_mode`, `active_version`, `published_version`, `deployed_url`, `custom_domain`, `is_featured`, `visibility`, `like_count`, `comment_count`, `view_count`, `status` | 배포된 멀티사이트 마스터 및 SyncShop/SyncCMS 테넌트 매핑 (`StudioStep6DeployPanel.vue`, `LiveSiteRendererView.vue`) |
| 7 | `kkumz_site_release` | `id` (`VARCHAR(64)`) | `site_id`, `version_no` (UK: `site_id, version_no`), `release_note`, `deploy_mode`, `design_tokens_json`, `layout_schema_json`, `webide_files_json`, `minio_bundle_path`, `build_status`, `build_log` | 멀티사이트 버전별 릴리즈 스냅샷 및 WebIDE 소스 원장 (`StudioStep4LivePreviewStage.vue`, `StudioStep5WebIdePanel.vue`) |
| 8 | `kkumz_build_job` | `id` (`VARCHAR(64)`) | `site_id`, `release_id`, `job_type`, `deploy_mode`, `status`, `duration_ms`, `error_summary`, `ai_patch_applied`, `build_log` | 샌드박스 빌드 파이프라인 잡 및 AI 에러 패치 이력 (`StudioStep5WebIdePanel.vue`, `/deployment/builds`) |
| 9 | `kkumz_team_recruit` | `id` (`VARCHAR(64)`) | `site_id`, `position_role`, `title`, `description`, `required_skills_json`, `headcount`, `recruited_count`, `status` | 팀원 모집 공고 (`ProjectLoungeView.vue`, `/community/projects`) |
| 10 | `kkumz_team_member` | `id` (`VARCHAR(64)`) | `site_id`, `recruit_id`, `username`, `display_name`, `member_role`, `position_role`, `join_type`, `application_message`, `status` | 프로젝트 합류 팀원 및 지원/초대 내역 (`WorkspaceView.vue`) |
| 11 | `kkumz_feed_comment` | `id` (`VARCHAR(64)`) | `site_id`, `username`, `display_name`, `author_role_badge`, `content`, `created_at` | 커뮤니티 쇼케이스 광장 피드 댓글 (`FeedView.vue`) |
| 12 | `kkumz_feed_like` | `id` (`VARCHAR(64)`) | `site_id`, `username` (UK: `site_id, username`), `created_at` | 커뮤니티 쇼케이스 피드 좋아요 원장 (`FeedView.vue`) |
| 13 | `kkumz_moderation_report` | `id` (`VARCHAR(64)`) | `target_type`, `target_id`, `site_id`, `reporter_username`, `reason_category`, `reason_detail`, `status`, `action_taken`, `moderator_username`, `resolved_at` | 콘텐츠 신고 접수 및 모더레이션 심사 처분 원장 (`/community/moderation`) |
| 14 | `kkumz_ai_prompt_template` | `id` (`VARCHAR(64)`) | `template_code` (UK), `template_name`, `stage_type`, `model_name`, `temperature`, `system_prompt`, `user_prompt_template`, `forbidden_words_json`, `status` | 스튜디오 단계별 AI 프롬프트 및 금지어 필터 규칙 (`/studio/prompts`) |
| 15 | `kkumz_notification` | `id` (`VARCHAR(64)`) | `recipient_username`, `notif_type`, `title`, `message`, `link_url`, `is_read`, `created_at` | 사용자 실시간 인앱 알림 센터 원장 (`WorkspaceView.vue`) |
| 16 | `kkumz_idea_asset` | `id` (`VARCHAR(64)`) | `idea_id`, `asset_type` (`HERO`, `PRODUCT`, `ARTICLE`, `GALLERY`, `LOGO`, `DOC`), `original_name`, `stored_name`, `file_url`, `mime_type`, `file_size`, `caption`, `extracted_text` | 아이디어·사이트 업로드 미디어 및 참고 문서 자산 (`StudioStep1AssetUploadPanel.vue`) |
| 17 | `kkumz_api_access_key` | `id` (`VARCHAR(64)`) | `direction` (`OUTBOUND`, `INBOUND`), `service_code`, `key_name`, `base_url`, `access_key` (UK), `secret_key`, `scopes_json`, `rate_limit_rpm`, `call_count`, `status` | 서비스 간 API 연동 액세스키 및 클라이언트 인증키 관리 (`/deployment/api-keys`) |
| 18 | `kkumz_site_git_repo` | `id` (`VARCHAR(64)`) | `site_id` (UK), `provider`, `repo_owner`, `repo_name`, `repo_html_url`, `clone_ssh_url`, `ssh_key_fingerprint`, `site_access_key`, `last_commit_sha`, `commit_history_json` | 멀티사이트 Git 리포지토리 프로비저닝 및 개발자 배포 키 원장 (`StudioStep6DeployPanel.vue`) |
| 19 | `kkumz_feature_catalog` | `id` (`VARCHAR(64)`) | `feature_code` (UK), `engine_code` (`SYNC_SHOP`, `SYNC_CMS`, `AI`), `category_code`, `category_name`, `title`, `subtitle`, `description`, `sdui_archetype`, `default_config_json` | SyncShop x SyncCMS x AI 52종 전체 기능 카탈로그 마스터 (`StudioStep1FeatureCatalogPanel.vue`) |
