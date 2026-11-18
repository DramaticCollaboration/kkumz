# Kkumz (꿈즈) Antigravity Agent Configuration (AGENTS.md)

> **문서 목적**: 본 문서는 `services/kkumz` 하위 프로젝트에서 코드를 작성·수정하거나 문서를 갱신하는 AI 코딩 에이전트 및 개발자가 반드시 준수해야 할 도메인 특화 아키텍처, 기술 스택, 코딩 컨벤션을 정의합니다. 전사 공통 규칙은 루트 [AGENTS.md](../../.agents/AGENTS.md) 및 [.agents/AGENTS.md](./.agents/AGENTS.md)를 상위 표준으로 준수합니다.

---

## 1. 역할 및 프로젝트 컨텍스트
- **서비스명**: **Kkumz (꿈즈)** — AI Character/Webtoon/Story Universe 11-Step Studio & Creator Community Vertical Service 및 AI 브랜드·웹사이트 멀티사이트 빌더 플랫폼 (`services/kkumz`)
- **역할 및 아키텍처**:
  1. 11-모듈 AI 스튜디오(`StudioStep1` ~ `StudioStep11` 아키텍처 및 `StudioStep1AssetUploadPanel.vue` ~ `StudioStep6DeployPanel.vue`, `StudioSidebar.vue`, `StudioTopbar.vue`, `StudioStatusBanners.vue`, `StudioAccessDeniedSection.vue`)를 통해 크리에이터·창업가가 아이디어, 세계관, 브랜드 네이밍 8종, 벡터 SVG 로고 6종, 마크다운 기능명세서(FRS), 라이브 프리뷰, WebIDE 샌드박스 빌드, 멀티사이트 배포를 원스톱으로 수행합니다.
  2. **SyncShop**(`pms_product`, `oms_order`) 및 **SyncCMS**(`cms_content`, `cms_form_data`) 멀티테넌트 원장과 실시간 연동되며, 모든 조회·생성·상태 변경은 PostgreSQL 18.6 데이터베이스(`kkumz_*`, `sys_*`) 트랜잭션으로 영속화됩니다.

## 2. 디렉터리, 포트 및 Maven 좌표 표준
- **최상위 디렉터리**: `services/kkumz/`
- **Maven GroupId**: `com.empasy.sync.kkumz`
- **최상위 POM / 백엔드 부모 POM**: `kkumz-parent` / `kkumz-server-parent`
- **백엔드 모듈 (`Server/`, 폴더명 = `artifactId` 1:1 일치)**: `kkumz-server-admin`
- **Java 기본 패키지**: `com.empasy.sync.kkumz` (`domain`, `system`, `config`)
- **프론트엔드 디렉터리 및 npm Scope**:
  - 운영자 백오피스 (`services/kkumz/admin/package.json`): `@empasy/kkumz-admin`
  - 크리에이터·창업가 웹 포털 & 스튜디오 (`services/kkumz/web/package.json`): `@empasy/kkumz-web`
- **표준 포트**: Server `8098` (로컬 `8096`/`9580`), Admin `3098` (`3108`), Web `3008`
- **단일 Canonical API Prefix**: `/api/v1/kkumz/...` (시스템 API: `/api/v1/kkumz/sys/...`, 동적 메뉴 인가 API: `GET /api/v1/kkumz/sys/permission/getUserPermissionByToken`)
- **데이터베이스**: PostgreSQL 18.6 (`pgvector`, 데이터베이스명: `sync_kkumz` / `kkumz`, 스크립트 위치: `services/kkumz/db/`)

## 3. DB 스크립트 6단계 단일 원천 규칙 (`services/kkumz/db/`)
1. `00_extensions.sql`: `vector`, `uuid-ossp`, `pg_trgm` 확장 모듈
2. `01_schema_system.sql`: `sys_user`, `sys_role`, `sys_permission`, `sys_user_role`, `sys_role_permission`, `sys_dict`, `sys_dict_item`, `sys_log`, `sys_config`, `sys_page_help`, `sys_prompt_template`, `sys_prompt_history` (12개 시스템 테이블)
3. `02_schema_domain.sql`: `kkumz_idea`, `kkumz_idea_version`, `kkumz_name_suggestion`, `kkumz_logo_suggestion`, `kkumz_screen_spec`, `kkumz_site`, `kkumz_site_release`, `kkumz_build_job`, `kkumz_team_recruit`, `kkumz_team_member`, `kkumz_feed_comment`, `kkumz_feed_like`, `kkumz_moderation_report`, `kkumz_ai_prompt_template`, `kkumz_notification`, `kkumz_idea_asset`, `kkumz_api_access_key`, `kkumz_site_git_repo`, `kkumz_feature_catalog` (19개 도메인 테이블)
4. `03_seed_system.sql`: 시스템 역할 4종(`role_admin`, `role_moderator`, `role_creator`, `role_developer`), 기본 계정 6종, 공통 시스템 메뉴(`/system/config`, `/system/prompt` 포함), 공통 사전 및 동적 환경설정(`sys_config`)
5. `04_seed_domain.sql`: 도메인 업무 메뉴(`perm-kkumz-dom-*`), 역할별 메뉴 권한 매핑(`srp_kkumz_*`), AI 프롬프트 템플릿(`kkumz_ai_prompt_template`, `sys_prompt_template`, `sys_prompt_history`), 52종 기능 카탈로그(`kkumz_feature_catalog`), 화면별 인앱 도움말(`sys_page_help`)
6. `05_sample_dev.sql`: 개발·시연용 대표 창업 프로젝트 및 라이브 멀티사이트 시드 데이터
- 제품 개발 단계에서 `migrations/` 분리 생성 및 `ON CONFLICT` 구문 사용을 전면 금지합니다.

## 4. 기술 스택 및 코드 위생 표준
- **백엔드**: JDK 25, Spring Boot 3.4.3, `AgentScope Java 2.0.2` (`io.agentscope:agentscope-harness`), Project Lombok(`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`), 동적 설정(`SystemConfigHolder` / `sys_config`), 동적 프롬프트(`SystemPromptHolder` / `sys_prompt_template`, `sys_prompt_history`), 클러스터 상태 동기화(`ClusterEventBroadcaster`).
- **프론트엔드**: Vben Admin (`PermissionModeEnum.BACK`, `BasicTable`, `BasicModal`, `BasicForm`, `PageWrapper`), 모달 분리(`*Modal.vue`).
- **표현 원칙**: 모든 코드, UI, 마크다운 문서에서 장식용 유니코드 이모지 사용과 불필요한 영문 괄호 병기를 전면 금지합니다.
