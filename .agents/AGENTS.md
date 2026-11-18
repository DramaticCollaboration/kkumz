# Kkumz (꿈즈) Antigravity Agent Configuration (.agents/AGENTS.md)

## 1. 역할 및 프로젝트 컨텍스트
본 작업 공간은 **Kkumz (꿈즈: AI Character/Webtoon/Story Universe 11-Step Studio & Creator Community Vertical Service 및 AI 브랜드·웹사이트 멀티사이트 빌더 플랫폼, `services/kkumz`)** 프론트엔드(`admin/`, `web/`), 백엔드(`Server/`) 및 데이터베이스(`db/`) 개발 전용 에이전트 설정입니다.
전사 공통 규칙은 루트 [AGENTS.md](../../../.agents/AGENTS.md) 및 [05_coding_convention_agent_guide.md](../../../docs/02_dev/05_coding_convention_agent_guide.md)를 상위 표준으로 준수하며, 본 문서는 Kkumz 버티컬 서비스 특화 규격을 정의합니다.

## 2. 디렉터리, 포트 및 Maven 좌표 표준
- **최상위 디렉터리**: `services/kkumz/`
- **서비스 역할**: AI Character/Webtoon/Story Universe 11-Step Studio & Creator Community Vertical Service (SyncShop·SyncCMS 연동 멀티사이트 빌더 및 크리에이터 커뮤니티)
- **Maven GroupId**: `com.empasy.sync.kkumz`
- **최상위 POM / 백엔드 부모 POM**: `kkumz-parent` / `kkumz-server-parent`
- **백엔드 모듈 (`Server/`, 폴더명 = `artifactId` 1:1 일치)**: `kkumz-server-admin`
- **기본 Java 패키지**: `com.empasy.sync.kkumz` (`domain`, `system`, `config`)
- **프론트엔드 모듈**:
  - 운영자 어드민 백오피스: `admin/` (`@empasy/kkumz-admin`)
  - 크리에이터·창업가 웹 포털 & AI 스튜디오: `web/` (`@empasy/kkumz-web`)
- **표준 서비스 포트**: Server `8098` (로컬 `8096`/`9580`), Admin `3098` (`3108`), Web `3008`
- **단일 Canonical API Prefix**: `/api/v1/kkumz/...` (시스템 API: `/api/v1/kkumz/sys/...`, 동적 메뉴 인가 API: `GET /api/v1/kkumz/sys/permission/getUserPermissionByToken`)
- **데이터베이스**: PostgreSQL 18.6 (`pgvector`, 데이터베이스명: `sync_kkumz` / `kkumz`, 스크립트 위치: `services/kkumz/db/`)

## 3. 중요 참조 문서
- **온보딩 가이드**: [PROJECT_ONBOARDING_GUIDE.md](../PROJECT_ONBOARDING_GUIDE.md)
- **문서 마스터 인덱스**: [doc/README.md](../doc/README.md)
- **AI 에이전트화 기획서**: [doc/ai/00_AI_Agentization_Plan.md](../doc/ai/00_AI_Agentization_Plan.md)
- **데이터베이스 스키마 설계서**: [doc/04_Database_스키마설계서.md](../doc/04_Database_스키마설계서.md), [doc/07_Kkumz_데이터모델링_및_인터페이스정의서.md](../doc/07_Kkumz_데이터모델링_및_인터페이스정의서.md)
- **전체 화면 목록**: [doc/08_Screen_List_화면목록.md](../doc/08_Screen_List_화면목록.md), [doc/04_Kkumz_구현화면목록.md](../doc/04_Kkumz_구현화면목록.md)
- **화면명세서 인덱스**: [doc/화면명세서/00_Screen_Specification_Index.md](../doc/화면명세서/00_Screen_Specification_Index.md)
- **역할별 사용자 가이드**: [doc/사용자가이드/00_Kkumz_역할별_사용자가이드_마스터.md](../doc/사용자가이드/00_Kkumz_역할별_사용자가이드_마스터.md)

## 4. DB 스크립트 6단계 단일 원천 규칙 (`services/kkumz/db/`)
모든 SQL 파일은 `services/kkumz/db/` 루트에 위치하며 아래 6단계 구성을 단일 원천(SSOT)으로 유지합니다 (`migrations/` 분리 생성 및 `ON CONFLICT` 구문 사용 전면 금지):
1. `00_extensions.sql`: PostgreSQL 18.6 확장 모듈 (`vector`, `uuid-ossp`, `pg_trgm`).
2. `01_schema_system.sql`: 플랫폼 공통 시스템 테이블 12종 (`sys_user`, `sys_role`, `sys_permission`, `sys_user_role`, `sys_role_permission`, `sys_dict`, `sys_dict_item`, `sys_log`, `sys_config`, `sys_page_help`, `sys_prompt_template`, `sys_prompt_history`).
3. `02_schema_domain.sql`: Kkumz 도메인 전용 테이블 19종 (`kkumz_idea`, `kkumz_idea_version`, `kkumz_name_suggestion`, `kkumz_logo_suggestion`, `kkumz_screen_spec`, `kkumz_site`, `kkumz_site_release`, `kkumz_build_job`, `kkumz_team_recruit`, `kkumz_team_member`, `kkumz_feed_comment`, `kkumz_feed_like`, `kkumz_moderation_report`, `kkumz_ai_prompt_template`, `kkumz_notification`, `kkumz_idea_asset`, `kkumz_api_access_key`, `kkumz_site_git_repo`, `kkumz_feature_catalog`).
4. `03_seed_system.sql`: 4대 시스템 역할(`role_admin`, `role_moderator`, `role_creator`, `role_developer`), 기본 계정 6종, 시스템 메뉴(`/system/config`, `/system/prompt` 포함), 공통 코드 사전 및 동적 환경설정(`sys_config`).
5. `04_seed_domain.sql`: 도메인 업무 메뉴(`perm-kkumz-dom-*`), 역할 권한 매핑(`srp_kkumz_*`), AI 프롬프트 템플릿(`kkumz_ai_prompt_template`, `sys_prompt_template`, `sys_prompt_history`), 52종 기능 카탈로그(`kkumz_feature_catalog`), 화면별 인앱 도움말(`sys_page_help`).
6. `05_sample_dev.sql`: 개발·시연용 대표 프로젝트 및 멀티사이트 시드 데이터.

## 5. 프론트엔드 및 백엔드 아키텍처 표준
1. **어드민 백오피스 (`admin/`)**:
   - `PermissionModeEnum.BACK` 동적 라우팅을 강제하며 정적 라우트 하드코딩을 금지합니다.
   - Vben 표준 고수준 컴포넌트(`BasicTable`, `TableAction`, `BasicModal`, `BasicForm`, `PageWrapper`)를 사용하고 모달은 `*Modal.vue`로 독립 분리합니다.
2. **웹 포털 & 11-모듈 AI 스튜디오 (`web/`)**:
   - `StudioView.vue`는 11개 모듈형 컴포넌트(`StudioStep1AssetUploadPanel.vue`, `StudioStep1FeatureCatalogPanel.vue`, `StudioStep2BrandPanel.vue`, `StudioStep3SpecPanel.vue`, `StudioStep4LivePreviewStage.vue`, `StudioStep5WebIdePanel.vue`, `StudioStep6DeployPanel.vue`, `StudioSidebar.vue`, `StudioTopbar.vue`, `StudioStatusBanners.vue`, `StudioAccessDeniedSection.vue`) 및 11단계 확장 크리에이터 워크플로우(`StudioStep1UniversePanel` ~ `StudioStep11PublishPanel` 아키텍처 매핑)로 구성됩니다.
3. **백엔드 및 AI 프레임워크 (`Server/`)**:
   - JDK 25, Spring Boot 3.4.3, `AgentScope Java 2.0.2` (`io.agentscope:agentscope-harness`), Project Lombok을 필수 적용합니다.
   - 런타임 설정은 `SystemConfigHolder`(`sys_config`), AI 프롬프트는 `SystemPromptHolder`(`sys_prompt_template`, `sys_prompt_history`) 및 `ClusterEventBroadcaster`로 무중단 동적 관리합니다.
   - 모든 코드, UI, 마크다운 문서에서 장식용 유니코드 이모지 사용과 불필요한 영문 괄호 병기를 전면 금지합니다.
