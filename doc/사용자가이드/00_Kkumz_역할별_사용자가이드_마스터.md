# Kkumz (꿈즈) AI 스튜디오 및 크리에이터 커뮤니티 역할별 사용자 가이드 마스터

- **문서 버전**: v2.6.5 (전사 6-File DB SSOT 및 11-모듈 AI 스튜디오 통합판)
- **대상 시스템**: `services/kkumz` (어드민 백오피스 `admin/` 포트 `3098`/`3108`, 크리에이터 웹 포털 `web/` 포트 `3008`, 백엔드 서버 `Server/` 포트 `8098`)
- **표준 API 프리픽스**: `/api/v1/kkumz/...` (동적 메뉴 인가 API: `GET /api/v1/kkumz/sys/permission/getUserPermissionByToken`)
- **기술 스택 표준**: `PostgreSQL 18.6 (pgvector)`, `AgentScope Java 2.0.2 (io.agentscope:agentscope-harness)`, `JDK 25`, `Spring Boot 3.4.3`, Vben Admin (`PermissionModeEnum.BACK`, `BasicTable`, `BasicModal`, `BasicForm`, `PageWrapper`)

---

## 1. 가이드 개요 및 역할별 매뉴얼 구성 체계

`Kkumz (꿈즈)`는 크리에이터와 창업가가 아이디어·세계관 한 줄로 브랜드 네이밍 8종, 벡터 SVG 로고 6종, 마크다운 기능명세서(FRS), 화면 설계 블루프린트, 52종 기능 카탈로그(`kkumz_feature_catalog`) 기반 라이브 사이트 및 에피소드를 제작·배포하고, 커뮤니티 광장(`/feed`, `/plaza`)과 팀빌딩 채팅 라운지(`/lounge`, `/chat`)에서 협업 및 수익화를 수행하는 AI 스튜디오 & 크리에이터 커뮤니티 버티컬 서비스입니다.

### 역할별 상세 실무 매뉴얼 바로가기

| 번호 | 직무 역할 명칭 | 역할 코드 (`sys_role`) | 대상 사용자 및 조직 | 매뉴얼 파일 링크 | 핵심 수행 업무 |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **01** | **플랫폼 운영 총괄 관리자** | `role_admin` (`ROLE_ADMIN`) | 플랫폼 운영본부, 시스템 아키텍트 | [01_플랫폼운영총괄관리자_실무_매뉴얼.md](./01_플랫폼운영총괄관리자_실무_매뉴얼.md) | 운영 종합 대시보드(`대시보드`), 작품·아이디어·에피소드·캐릭터·세계관 카탈로그(`창업 기획서`, `사이트 목록`), 퀘스트·배지·리워드 경제 및 크리에이터 정산·구독플랜 연동, 통합 동적 환경설정(`/system/config`), AI 프롬프트 거버넌스(`/system/prompt`, `/studio/prompts`) 총괄 |
| **02** | **AI 스튜디오 크리에이터** | `role_creator` / `role_developer` | 창업가 메이커, 웹툰·스토리·브랜드 크리에이터, 협업 개발자 | [02_AI스튜디오크리에이터_실무_매뉴얼.md](./02_AI스튜디오크리에이터_실무_매뉴얼.md) | 11-Step AI 스튜디오 워크플로우(`StudioStep1UniversePanel` ~ `StudioStep11PublishPanel` 및 11개 모듈 컴포넌트 `StudioStep1AssetUploadPanel.vue` ~ `StudioStep6DeployPanel.vue`), AI 샌드박스 WebIDE 프로토타이핑, 라이브 멀티사이트·에피소드 배포 및 SyncShop 수익·주문 추적 |
| **03** | **커뮤니티 모더레이터** | `role_moderator` (`ROLE_MODERATOR`) | 커뮤니티 신뢰안전팀(Trust & Safety) | [03_커뮤니티모더레이터_실무_매뉴얼.md](./03_커뮤니티모더레이터_실무_매뉴얼.md) | 광장 쇼케이스 피드(`광장게시글`), 댓글·실시간 채팅방(`댓글모더`, `채팅방`) 모니터링, AI 금지어 안전 정책(`forbidden_words_json`) 집행, 신고 접수 심사(`/community/moderation`) 및 위반 사이트 즉시 정지(`SUSPEND_SITE`) |

---

## 2. 역할별 기본 계정 및 접속 정보 (`db/03_seed_system.sql`)

| 역할 구분 | 로그인 아이디 | 사용자 성명 | 소속 코드 (`org_code`) | 역할 코드 | 기본 접속 애플리케이션 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **플랫폼 운영 총괄 관리자** | `admin` | 김도현 총괄관리자 | `KKUMZ-HQ` | `role_admin` | 어드민 백오피스 (`admin/`, `/dashboard/analysis`) |
| **커뮤니티 모더레이터** | `moderator.kim` | 김서연 모더레이터 | `KKUMZ-TRUST` | `role_moderator` | 어드민 백오피스 (`admin/`, `/community/moderation`) |
| **AI 스튜디오 크리에이터** | `soyeon.jeju` | 한소연 대표 | `CREATOR` | `role_creator` | 크리에이터 웹 포털 (`web/`, `/studio`, `/workspace`) |
| **AI 스튜디오 크리에이터** | `minjun.maker` | 박민준 메이커 | `CREATOR` | `role_creator` | 크리에이터 웹 포털 (`web/`, `/studio`, `/feed`) |
| **AI 스튜디오 크리에이터** | `jihoon.run` | 정지훈 크루장 | `CREATOR` | `role_creator` | 크리에이터 웹 포털 (`web/`, `/lounge`, `/workspace`) |
| **AI 스튜디오 크리에이터** | `hajun.roast` | 최하준 헤드로스터 | `CREATOR` | `role_creator` | 크리에이터 웹 포털 (`web/`, `/studio`, `/s/bean-chronicle`) |

---

## 3. 어드민 백오피스 (`admin/`) DB 동적 메뉴 및 도메인 매핑 매트릭스

어드민 백오피스는 `permissionMode: PermissionModeEnum.BACK` 표준에 따라 DB(`sys_permission`, `sys_role_permission`)에서 인가된 메뉴만 동적 공급합니다.

| 상위 그룹 | DB 표준 메뉴명 (`sys_permission.name`) | 크리에이터·커뮤니티 도메인 매핑 호칭 | 라우트 URL (`url`) | 뷰 컴포넌트 (`admin/src/views/`) | `role_admin` | `role_moderator` |
| :--- | :--- | :--- | :--- | :--- | :---: | :---: |
| **대시보드** | `대시보드` | `대시보드` | `/dashboard/analysis` | `dashboard/analysis/index.vue` | O | O |
| **AI 스튜디오** | `창업 기획서` | `작품관리` / `세계관` / `캐릭터` | `/studio/ideas` | `studio/ideas/index.vue` (`IdeaDetailModal.vue`) | O | - |
| **AI 스튜디오** | `AI 생성 규칙` | `프롬프트` (스튜디오 필터) | `/studio/prompts` | `studio/prompts/index.vue` (`PromptModal.vue`) | O | - |
| **사이트·배포** | `사이트 목록` | `에피소드` / `크리에이터정산` / `구독플랜` | `/deployment/sites` | `deployment/sites/index.vue` | O | - |
| **사이트·배포** | `빌드 로그` | `에피소드` (샌드박스 빌드) | `/deployment/builds` | `deployment/builds/index.vue` (`BuildLogModal.vue`) | O | - |
| **사이트·배포** | `API 키` | `크리에이터정산` (SyncShop/CMS 브릿지) | `/deployment/api-keys` | `deployment/api-keys/index.vue` | O | - |
| **커뮤니티** | `프로젝트·팀원` | `광장게시글` / `채팅방` / `퀘스트` / `배지관리` | `/community/projects` | `community/projects/index.vue` | O | O |
| **커뮤니티** | `신고 관리` | `댓글모더` / `광장게시글` 모더레이션 | `/community/moderation` | `community/moderation/index.vue` (`ModerationResolveModal.vue`) | O | O |
| **환경 설정** | `사용자` | `사용자` / `배지관리` | `/system/user` | `system/user/index.vue` | O | - |
| **환경 설정** | `권한` | `권한` | `/system/role` | `system/role/index.vue` | O | - |
| **환경 설정** | `메뉴` | `메뉴` | `/system/menu` | `system/menu/index.vue` | O | - |
| **환경 설정** | `공통 코드` | `퀘스트` / `배지관리` / `구독플랜` 사전 | `/system/dict` | `system/dict/index.vue` | O | - |
| **환경 설정** | `프롬프트` | `프롬프트` (`SystemPromptHolder`) | `/system/prompt` | `system/prompt/index.vue` | O | - |
| **환경 설정** | `시스템 환경설정` | `환경설정` (`SystemConfigHolder`) | `/system/config` | `system/config/index.vue` | O | - |
| **환경 설정** | `부서` | `부서` | `/system/depart` | `system/depart/index.vue` | O | - |
| **환경 설정** | `알림 발송` | `알림 발송` | `/system/message` | `system/message/index.vue` | O | - |
| **환경 설정** | `작업 로그` | `작업 로그` | `/monitor/log` | `monitor/log/index.vue` | O | - |
| **환경 설정** | `도움말 설정` | `도움말 설정` (`sys_page_help`) | `/system/help-guide` | `system/help/index.vue` | O | - |

---

## 4. 크리에이터 웹 포털 (`web/`) 라우트 및 11-모듈 AI 스튜디오 아키텍처

| 웹 화면 ID | 포털 라우트 (`web/src/router`) | 크리에이터 커뮤니티 라우트 매핑 | 물리 뷰 파일 (`services/kkumz/web/src/views/`) | 핵심 기능 |
| :---: | :--- | :--- | :--- | :--- |
| `WEB-01` | `/feed` (`/`) | `/plaza` (광장게시글 쇼케이스) | `FeedView.vue` | 라이브 배포된 작품·멀티사이트 쇼케이스 피드, 좋아요, 응원 댓글, 신고 접수 |
| `WEB-02` | `/lounge/:siteSlug?` | `/chat` (팀빌딩·크루 채팅 라운지) | `ProjectLoungeView.vue` | 직군별(`FRONTEND`, `DESIGNER`, `MARKETER`, `MD`, `PLANNER`, `CO_FOUNDER`) 모집 및 지원 |
| `WEB-03` | `/workspace` | `/quest`, `/mypage` (내 워크스페이스·퀘스트·배지) | `WorkspaceView.vue` | 내 기획서·멀티사이트 관리, 팀원 승인, 리워드 포인트(`CREATOR_POINT`) 및 인앱 알림 센터 |
| `WEB-04` | `/studio/:ideaId?` | `/studio` (11-Step 모듈형 AI 스튜디오) | `StudioView.vue` 및 `views/studio/Studio*.vue` (11개 모듈 컴포넌트) | 세계관·아이디어 정의, 자산 업로드, 52종 기능 카탈로그, 네이밍 8종, SVG 로고 6종, FRS 명세서, 라이브 프리뷰, WebIDE 샌드박스, Git/배포 |
| `WEB-05` | `/s/:siteSlug` | `/sandbox` (라이브 렌더러 & AI 컨시어지) | `LiveSiteRendererView.vue` | 배포된 멀티사이트·에피소드 독립 렌더링, SyncShop 주문/예약 결제, SyncCMS 폼 접수, AI 컨시어지 |

### 4.1 `views/studio/` 11개 물리 모듈 컴포넌트와 11-Step 크리에이터 워크플로우 1:1 매핑

| 단계 번호 | 논리 11-Step 스튜디오 단계명 | 물리 Vue 컴포넌트 (`services/kkumz/web/src/views/studio/`) | 연동 백엔드 API 및 DB 테이블 |
| :---: | :--- | :--- | :--- |
| **Step 1** | `StudioStep1UniversePanel` (세계관·아이디어 및 미디어 자산 업로드) | `StudioStep1AssetUploadPanel.vue` | `POST /api/v1/kkumz/ideas`, `kkumz_idea`, `kkumz_idea_asset` |
| **Step 2** | `StudioStep2CharacterCatalogPanel` (AI 의도 진단 및 52종 기능·캐릭터 카탈로그) | `StudioStep1FeatureCatalogPanel.vue` | `KKUMZ_INTENT_ADVISOR_V1`, `kkumz_feature_catalog` (52종) |
| **Step 3** | `StudioStep3BrandNamingPanel` (AI 브랜드·작품 네이밍 8종 및 로고 6종 생성) | `StudioStep2BrandPanel.vue` | `POST /ideas/{id}/generate-names`, `generate-logos`, `kkumz_name_suggestion`, `kkumz_logo_suggestion` |
| **Step 4** | `StudioStep4EpisodeSpecPanel` (마크다운 기능·스토리 명세서 FRS 및 버전 롤백) | `StudioStep3SpecPanel.vue` | `POST /ideas/{id}/generate-spec`, `rollback`, `kkumz_idea_version`, `kkumz_screen_spec` |
| **Step 5** | `StudioStep5LivePreviewPanel` (대화형 테마·SDUI 레이아웃 라이브 프리뷰 스테이지) | `StudioStep4LivePreviewStage.vue` | `STUDIO_CHAT_REFINE`, `KKUMZ_SITE_PROVISION_V1` |
| **Step 6** | `StudioStep6WebIdeSandboxPanel` (WebIDE Vue SFC 편집 및 AI 빌드 오류 자동 패치) | `StudioStep5WebIdePanel.vue` | `POST /sites/{siteSlug}/webide-save`, `webide-ai-fix`, `kkumz_build_job` |
| **Step 7** | `StudioStep7GitProvisionPanel` (멀티사이트 전용 Git 리포지토리 및 SSH 배포키 프로비저닝) | `StudioStep6DeployPanel.vue` (Git 섹션) | `kkumz_site_git_repo`, `kkumz_api_access_key` |
| **Step 8** | `StudioStep8StepNavigatorPanel` (스튜디오 단계 내비게이션 및 진행 상태 제어) | `StudioSidebar.vue` | `kkumz_idea.current_step` 단계 전환 및 버전 스냅샷 컨트롤 |
| **Step 9** | `StudioStep9TopbarControlPanel` (스튜디오 상단 액션 바 및 프리뷰 뷰포트 전환) | `StudioTopbar.vue` | 데스크톱/모바일 뷰포트 전환, 즉시 저장, 라이브 사이트 새 창 열기 |
| **Step 10** | `StudioStep10StatusDiagnosticPanel` (AI 추론·빌드·배포 상태 배너 및 실시간 진단) | `StudioStatusBanners.vue` | 서킷브레이커 상태, 빌드 진행 상태 및 AI 패치 알림 배너 |
| **Step 11** | `StudioStep11PublishPanel` (권한 게이트 검증 및 2-Track 라이브 멀티사이트 퍼블리싱) | `StudioAccessDeniedSection.vue` + `StudioStep6DeployPanel.vue` | `POST /sites/{siteSlug}/deploy`, `unpublish`, `kkumz_site`, `kkumz_site_release` |
