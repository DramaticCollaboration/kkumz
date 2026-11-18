# DOC-KZ-04. Kkumz 구현 화면 목록 (04_Kkumz_구현화면목록.md)

- **문서 번호**: DOC-KZ-04 (동일 참조본: [`08_Screen_List_화면목록.md`](./08_Screen_List_화면목록.md))
- **대상 애플리케이션**:
  1. 크리에이터·창업가 웹 포털 & 11-Step 모듈형 AI 스튜디오 & 멀티사이트 렌더러 (`services/kkumz/web/`, 포트 `3008`)
  2. 운영자 어드민 백오피스 (`services/kkumz/admin/`, 포트 `3098` / `3108`)
- **원천 정합성 기준**: `web/src/router/index.ts`, `db/03_seed_system.sql`, `db/04_seed_domain.sql`

---

## 1. 전체 화면 구성 요약

| 애플리케이션 구분 | 화면 분류 | 화면 수 | 하위 모듈/모달 수 | 라우팅 방식 |
| :--- | :--- | :---: | :---: | :--- |
| **크리에이터 웹 (`web/`)** | 창업가·크리에이터 포털 & 11-모듈 AI 스튜디오 & 멀티사이트 렌더러 | **5개 엔트리 뷰** (`WEB-01` ~ `WEB-05`) | **11개 스튜디오 모듈** (`StudioStep1`~`StudioStep11` / `views/studio/Studio*.vue`) + 4개 포털 모달 | Vue Router (`web/src/router/index.ts`) |
| **운영자 어드민 (`admin/`)** | 도메인 비즈니스 관제 화면 | **8개 화면** (`SCR-KZ-01` ~ `SCR-KZ-07`, `SCR-KZ-05-KEY`) | 4개 (`*Modal.vue`) | DB 동적 라우팅 (`PermissionModeEnum.BACK`) |
| **운영자 어드민 (`admin/`)** | 시스템 공통 RBAC · 동적 설정 · 프롬프트 · 도움말 화면 | **10개 화면** (`SCR-KZ-SYS-01` ~ `SCR-KZ-SYS-10`) | 6개 (`*Modal.vue`) | DB 동적 라우팅 (`PermissionModeEnum.BACK`) |
| **합계** | **전체 구현 화면** | **23개 메인 화면** | **25개 모듈/모달** | - |

---

## 2. 크리에이터·창업가 웹 포털 & AI 스튜디오 (`web/`) 화면 목록

| 화면 ID | 화면명 (커뮤니티 도메인 매핑) | 라우트 경로 (`path`) | 레이아웃 모드 | 소스 파일 경로 (`services/kkumz/web/src/`) | 연동 REST API (`/api/v1/kkumz/...`) | 개별 명세서 |
| :---: | :--- | :--- | :---: | :--- | :--- | :--- |
| **WEB-01** | **쇼케이스 피드** (`광장게시글` / `/plaza`) | `/feed` (`/`) | `community` (1단 GNB) | `views/FeedView.vue` | `GET /feed`, `POST /feed/{siteId}/like`, `GET, POST /feed/{siteId}/comments`, `POST /moderation/reports` | [`WEB-01`](./screens/WEB-01_쇼케이스_피드.md) |
| **WEB-02** | **팀빌딩 라운지** (`채팅방` / `/chat`) | `/lounge/:siteSlug?` | `community` (1단 GNB) | `views/ProjectLoungeView.vue` | `GET /recruits`, `POST /recruits`, `POST /recruits/{id}/apply` | [`WEB-02`](./screens/WEB-02_팀빌딩_라운지.md) |
| **WEB-03** | **내 워크스페이스** (`퀘스트` / `배지관리` / `/quest` / `/mypage`) | `/workspace` | `community` (1단 GNB) | `views/WorkspaceView.vue` | `GET /ideas`, `GET /sites`, `GET /team-members`, `POST /team-members/{id}/approve`, `GET, POST /notifications` | [`WEB-03`](./screens/WEB-03_내_워크스페이스.md) |
| **WEB-04** | **11-Step 모듈형 AI 스튜디오 & WebIDE** (`/studio`) | `/studio/:ideaId?` | `studio` (GNB 숨김, 3-Pane 워크벤치) | `views/StudioView.vue` 및 `views/studio/Studio*.vue` (11개 모듈) | `GET, POST, PUT /ideas`, `generate-names`, `select-name`, `generate-logos`, `select-logo`, `generate-spec`, `versions`, `rollback`, `generate-screens`, `deploy`, `unpublish`, `webide-save`, `webide-ai-fix` | [`WEB-04`](./screens/WEB-04_6단계_AI_스튜디오_및_WebIDE.md) |
| **WEB-05** | **멀티사이트 라이브 렌더러** (`/sandbox`) | `/s/:siteSlug` | `independent` (독립 브랜드 사이트) | `views/LiveSiteRendererView.vue` | `GET /sites/{siteSlug}/runtime`, `POST /sites/{siteSlug}/orders`, `POST /sites/{siteSlug}/forms`, `POST /sites/{siteSlug}/concierge` | [`WEB-05`](./screens/WEB-05_멀티사이트_라이브_렌더러.md) |

### 2.1 `WEB-04` AI 스튜디오 11개 모듈형 컴포넌트 (`views/studio/`) 목록

| 순번 | 논리 11-Step 패널명 | 물리 Vue 파일 (`services/kkumz/web/src/views/studio/`) | 주요 기능 및 연동 테이블 |
| :---: | :--- | :--- | :--- |
| 1 | `StudioStep1UniversePanel` | `StudioStep1AssetUploadPanel.vue` | 아이디어·세계관 정의 및 사진·로고·문서 자산 업로드 (`kkumz_idea`, `kkumz_idea_asset`) |
| 2 | `StudioStep2CharacterCatalogPanel` | `StudioStep1FeatureCatalogPanel.vue` | AI 아키텍처·업종 진단(`KKUMZ_INTENT_ADVISOR_V1`) 및 52종 기능 카탈로그 선택 (`kkumz_feature_catalog`) |
| 3 | `StudioStep3BrandNamingPanel` | `StudioStep2BrandPanel.vue` | AI 브랜드 네이밍 8종(`kkumz_name_suggestion`) 및 인라인 벡터 SVG 로고 6종(`kkumz_logo_suggestion`) 생성 |
| 4 | `StudioStep4EpisodeSpecPanel` | `StudioStep3SpecPanel.vue` | 마크다운 기능명세서(FRS) 편집, 버전 히스토리(`kkumz_idea_version`) 롤백 및 화면 블루프린트(`kkumz_screen_spec`) |
| 5 | `StudioStep5LivePreviewPanel` | `StudioStep4LivePreviewStage.vue` | 업종 맞춤 초기 콘텐츠 프로비저닝(`KKUMZ_SITE_PROVISION_V1`) 및 대화형 테마·레이아웃 리파인(`STUDIO_CHAT_REFINE`) |
| 6 | `StudioStep6WebIdeSandboxPanel` | `StudioStep5WebIdePanel.vue` | 가상 Vue 3 SFC 편집, 샌드박스 빌드(`kkumz_build_job`) 및 AI 빌드 오류 자동 패치(`WEBIDE_AI_BUILD_FIXER`) |
| 7 | `StudioStep7GitProvisionPanel` | `StudioStep6DeployPanel.vue` (Git 섹션) | 사이트 전용 Git 리포지토리(`kkumz_site_git_repo`), SSH 배포키 및 API 액세스키(`kkumz_api_access_key`) 프로비저닝 |
| 8 | `StudioStep8StepNavigatorPanel` | `StudioSidebar.vue` | 좌측 수직 스테퍼 내비게이션 및 진행 단계 컨트롤 |
| 9 | `StudioStep9TopbarControlPanel` | `StudioTopbar.vue` | 상단 브랜드 배지, 뷰포트(데스크톱/모바일) 전환 및 즉시 저장 바 |
| 10 | `StudioStep10StatusDiagnosticPanel` | `StudioStatusBanners.vue` | AI 추론 상태, 외부 연동 서킷브레이커 및 샌드박스 빌드 진단 배너 |
| 11 | `StudioStep11PublishPanel` | `StudioAccessDeniedSection.vue` + `StudioStep6DeployPanel.vue` | 프로젝트 멤버십 권한 게이트 검증 및 2-Track(`DYNAMIC_SDUI` / `CUSTOM_BUNDLE`) 라이브 배포·비공개 전환 |

---

## 3. 운영자 어드민 백오피스 (`admin/`) 도메인 화면 목록

| 화면 ID | 권한 ID (`sys_permission.id`) | 상위 그룹 | DB 메뉴명 (`name`) | 크리에이터 도메인 매핑 | 라우트 URL (`url`) | 뷰 컴포넌트 (`admin/src/views/`) | 분리된 스키마 & 모달 파일 | 접근 역할 |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **SCR-KZ-01** | `perm-kkumz-sys-001` | 대시보드 | `대시보드` | `대시보드` | `/dashboard/analysis` | `dashboard/analysis/index.vue` | - | `role_admin`, `role_moderator` |
| **SCR-KZ-02** | `perm-kkumz-dom-002` | `AI 스튜디오` | `창업 기획서` | `작품관리` / `세계관` / `캐릭터` | `/studio/ideas` | `studio/ideas/index.vue` | `ideas.data.ts`, `IdeaDetailModal.vue` | `role_admin` |
| **SCR-KZ-03** | `perm-kkumz-dom-003` | `AI 스튜디오` | `AI 생성 규칙` | `프롬프트` (스튜디오 필터) | `/studio/prompts` | `studio/prompts/index.vue` | `prompts.data.ts`, `PromptModal.vue` | `role_admin` |
| **SCR-KZ-04** | `perm-kkumz-dom-011` | `사이트·배포` | `사이트 목록` | `에피소드` / `크리에이터정산` / `구독플랜` | `/deployment/sites` | `deployment/sites/index.vue` | `sites.data.ts` | `role_admin` |
| **SCR-KZ-05** | `perm-kkumz-dom-012` | `사이트·배포` | `빌드 로그` | `에피소드` (샌드박스 빌드) | `/deployment/builds` | `deployment/builds/index.vue` | `builds.data.ts`, `BuildLogModal.vue` | `role_admin` |
| **SCR-KZ-05-KEY** | `perm-kkumz-dom-013` | `사이트·배포` | `API 키` | `크리에이터정산` (SyncShop/CMS 연동) | `/deployment/api-keys` | `deployment/api-keys/index.vue` | `apiKeys.data.ts` | `role_admin` |
| **SCR-KZ-06** | `perm-kkumz-dom-021` | `커뮤니티` | `프로젝트·팀원` | `광장게시글` / `채팅방` / `퀘스트` / `배지관리` | `/community/projects` | `community/projects/index.vue` | `projects.data.ts` | `role_admin`, `role_moderator` |
| **SCR-KZ-07** | `perm-kkumz-dom-022` | `커뮤니티` | `신고 관리` | `댓글모더` | `/community/moderation` | `community/moderation/index.vue` | `moderation.data.ts`, `ModerationResolveModal.vue` | `role_admin`, `role_moderator` |

---

## 4. 운영자 어드민 백오피스 (`admin/`) 시스템 공통 및 동적 거버넌스 화면 목록

| 화면 ID | 권한 ID (`sys_permission.id`) | DB 메뉴명 (`name`) | 도메인 매핑 호칭 | 라우트 URL (`url`) | 뷰 컴포넌트 (`admin/src/views/`) | 연동 DB 테이블 및 REST API (`/api/v1/kkumz/sys/...`) |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- |
| **SCR-KZ-SYS-01** | `perm-kkumz-sys-011` | `사용자` | `사용자` | `/system/user` | `system/user/index.vue` | `sys_user`, `sys_user_role` (`/sys/user/list`, `/save`, `/delete`) |
| **SCR-KZ-SYS-02** | `perm-kkumz-sys-012` | `권한` | `권한` | `/system/role` | `system/role/index.vue` | `sys_role`, `sys_role_permission` (`/sys/role/list`, `/queryRolePermission`, `/saveRolePermission`) |
| **SCR-KZ-SYS-03** | `perm-kkumz-sys-013` | `메뉴` | `메뉴` | `/system/menu` | `system/menu/index.vue` | `sys_permission` (`/sys/permission/list`, `/getUserPermissionByToken`) |
| **SCR-KZ-SYS-04** | `perm-kkumz-sys-014` | `공통 코드` | `퀘스트` / `배지관리` / `구독플랜` 사전 | `/system/dict` | `system/dict/index.vue` | `sys_dict`, `sys_dict_item` (`/sys/dict/list`, `/sys/dictItem/list`) |
| **SCR-KZ-SYS-05** | `perm-kkumz-sys-015` | `프롬프트` | `프롬프트` | `/system/prompt` | `system/prompt/index.vue` | `sys_prompt_template`, `sys_prompt_history` (`SystemPromptHolder` — `/sys/prompt/list`, `/update`, `/history`, `/rollback`) |
| **SCR-KZ-SYS-06** | `perm-kkumz-sys-config` | `시스템 환경설정` | `환경설정` | `/system/config` | `system/config/index.vue` | `sys_config` (`SystemConfigHolder`, `ClusterEventBroadcaster` — `/sys/config/list`, `/saveBatch`, `/reset`) |
| **SCR-KZ-SYS-07** | `perm-kkumz-sys-016` | `부서` | `부서` | `/system/depart` | `system/depart/index.vue` | 조직 및 크리에이터 운영 부서 트리 관리 (`/sys/sysDepart/queryTreeList`) |
| **SCR-KZ-SYS-08** | `perm-kkumz-sys-017` | `알림 발송` | `알림 발송` | `/system/message` | `system/message/index.vue` | 시스템 알림 메시지 및 발송 템플릿 관리 |
| **SCR-KZ-SYS-09** | `perm-kkumz-sys-018` | `작업 로그` | `작업 로그` | `/monitor/log` | `monitor/log/index.vue` | `sys_log` (`/sys/log/list`) |
| **SCR-KZ-SYS-10** | `perm-kkumz-sys-019` | `도움말 설정` | `도움말 설정` | `/system/help-guide` | `system/help/index.vue` | `sys_page_help` (`/sys/help/list`, `/guide`) |
