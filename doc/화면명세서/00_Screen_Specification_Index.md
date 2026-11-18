# Kkumz (꿈즈) AI 스튜디오 및 크리에이터 커뮤니티 화면명세서 마스터 인덱스

본 문서는 **Empasy SyncSeries - Kkumz (`services/kkumz`)**의 크리에이터 웹 포털 & 11-모듈 AI 스튜디오(`web/src/views/`), 운영자 어드민 백오피스(`admin/src/views/`), 그리고 DB 단일 원천 동적 라우팅(`db/03_seed_system.sql`, `db/04_seed_domain.sql`)에 구현된 전체 화면의 상세 명세서 마스터 인덱스입니다.

---

## 1. 크리에이터 웹 포털 & 11-Step 모듈형 AI 스튜디오 (`web/`) 화면 인덱스

| 화면 ID | 화면명 (커뮤니티 도메인 매핑) | Router Path | Physical File Path (`services/kkumz/web/src/`) | 상세 명세서 파일 링크 |
| :---: | :--- | :--- | :--- | :--- |
| `WEB-01` | 쇼케이스 피드 (`광장게시글` / `/plaza`) | `/feed` (`/`) | `views/FeedView.vue` | [WEB-01_쇼케이스_피드.md](../screens/WEB-01_쇼케이스_피드.md) |
| `WEB-02` | 팀빌딩 라운지 (`채팅방` / `/chat`) | `/lounge/:siteSlug?` | `views/ProjectLoungeView.vue` | [WEB-02_팀빌딩_라운지.md](../screens/WEB-02_팀빌딩_라운지.md) |
| `WEB-03` | 내 워크스페이스 (`퀘스트` / `배지관리` / `/quest` / `/mypage`) | `/workspace` | `views/WorkspaceView.vue` | [WEB-03_내_워크스페이스.md](../screens/WEB-03_내_워크스페이스.md) |
| `WEB-04` | 11-Step 모듈형 AI 스튜디오 & WebIDE (`/studio`) | `/studio/:ideaId?` | `views/StudioView.vue` 및 `views/studio/Studio*.vue` (11개 모듈 컴포넌트) | [WEB-04_6단계_AI_스튜디오_및_WebIDE.md](../screens/WEB-04_6단계_AI_스튜디오_및_WebIDE.md) |
| `WEB-05` | 멀티사이트 라이브 렌더러 (`/sandbox`) | `/s/:siteSlug` | `views/LiveSiteRendererView.vue` | [WEB-05_멀티사이트_라이브_렌더러.md](../screens/WEB-05_멀티사이트_라이브_렌더러.md) |

### 1.1 `WEB-04` AI 스튜디오 11개 모듈 컴포넌트 (`views/studio/`) 상세 매핑

| 모듈 번호 | 논리 11-Step 스튜디오 패널명 | 물리 Vue 파일 (`services/kkumz/web/src/views/studio/`) | 핵심 역할 및 연동 테이블 |
| :---: | :--- | :--- | :--- |
| **01** | `StudioStep1UniversePanel` | `StudioStep1AssetUploadPanel.vue` | 아이디어·세계관 정의, 스타터 프리셋 선택, 이미지·로고·문서(`DOC`) 업로드 (`kkumz_idea`, `kkumz_idea_asset`) |
| **02** | `StudioStep2CharacterCatalogPanel` | `StudioStep1FeatureCatalogPanel.vue` | AI 아키텍처·의도 자동 진단(`KKUMZ_INTENT_ADVISOR_V1`) 및 8대 분류 52종 기능 카탈로그 선택 (`kkumz_feature_catalog`) |
| **03** | `StudioStep3BrandNamingPanel` | `StudioStep2BrandPanel.vue` | AI 브랜드 네이밍 8종(`kkumz_name_suggestion`) 및 인라인 벡터 SVG 로고 6종(`kkumz_logo_suggestion`) 생성·확정 |
| **04** | `StudioStep4EpisodeSpecPanel` | `StudioStep3SpecPanel.vue` | 마크다운 상세 기능명세서(FRS) 생성·편집, 버전 히스토리(`kkumz_idea_version`) 롤백 및 화면 블루프린트(`kkumz_screen_spec`) 설계 |
| **05** | `StudioStep5LivePreviewPanel` | `StudioStep4LivePreviewStage.vue` | 초기 사이트 콘텐츠 프로비저닝(`KKUMZ_SITE_PROVISION_V1`) 및 대화형 테마·레이아웃 리파인(`STUDIO_CHAT_REFINE`) 라이브 프리뷰 |
| **06** | `StudioStep6WebIdeSandboxPanel` | `StudioStep5WebIdePanel.vue` | 가상 Vue 3 SFC 파일 편집, 샌드박스 번들 빌드(`kkumz_build_job`) 및 AI 빌드 오류 자동 코드 패치(`WEBIDE_AI_BUILD_FIXER`) |
| **07** | `StudioStep7GitProvisionPanel` | `StudioStep6DeployPanel.vue` (Git 섹션) | 멀티사이트 전용 Git 리포지토리(`kkumz_site_git_repo`), SSH 배포키 핑거프린트 및 사이트 API 키 프로비저닝 |
| **08** | `StudioStep8StepNavigatorPanel` | `StudioSidebar.vue` | 좌측 수직 스테퍼 내비게이션, 단계별 진행 상태 표시 및 버전 스냅샷 컨트롤 |
| **09** | `StudioStep9TopbarControlPanel` | `StudioTopbar.vue` | 상단 브랜드 타이틀, 데스크톱/모바일 뷰포트 전환, 임시 저장 및 라이브 사이트 바로가기 |
| **10** | `StudioStep10StatusDiagnosticPanel` | `StudioStatusBanners.vue` | AI 추론 진행 상태, 외부 연동 서킷브레이커 폴백 상태 및 샌드박스 빌드 진단 배너 |
| **11** | `StudioStep11PublishPanel` | `StudioAccessDeniedSection.vue` + `StudioStep6DeployPanel.vue` | 프로젝트 멤버십 권한 게이트 검증 및 2-Track(`DYNAMIC_SDUI` / `CUSTOM_BUNDLE`) 라이브 배포·비공개 전환 (`kkumz_site`, `kkumz_site_release`) |

---

## 2. 운영자 어드민 백오피스 (`admin/`) 도메인 화면 명세서 인덱스

| 화면 ID | 권한 ID (`sys_permission.id`) | DB 메뉴명 | 크리에이터 도메인 매핑 | Router Path (`url`) | 뷰 컴포넌트 (`admin/src/views/`) | 분리된 모달 컴포넌트 | 상세 명세서 파일 링크 |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `SCR-KZ-01` | `perm-kkumz-sys-001` | `대시보드` | `대시보드` | `/dashboard/analysis` | `dashboard/analysis/index.vue` | - | [SCR-KZ-01_운영_종합_대시보드.md](../screens/SCR-KZ-01_운영_종합_대시보드.md) |
| `SCR-KZ-02` | `perm-kkumz-dom-002` | `창업 기획서` | `작품관리` / `세계관` / `캐릭터` | `/studio/ideas` | `studio/ideas/index.vue` | `IdeaDetailModal.vue` | [SCR-KZ-02_아이디어_및_명세서_관리.md](../screens/SCR-KZ-02_아이디어_및_명세서_관리.md) |
| `SCR-KZ-03` | `perm-kkumz-dom-003` | `AI 생성 규칙` | `프롬프트` (스튜디오 규칙) | `/studio/prompts` | `studio/prompts/index.vue` | `PromptModal.vue` | [SCR-KZ-03_AI_프롬프트_및_필터_관리.md](../screens/SCR-KZ-03_AI_프롬프트_및_필터_관리.md) |
| `SCR-KZ-04` | `perm-kkumz-dom-011` | `사이트 목록` | `에피소드` / `크리에이터정산` / `구독플랜` | `/deployment/sites` | `deployment/sites/index.vue` | - | [SCR-KZ-04_멀티사이트_및_도메인_관제.md](../screens/SCR-KZ-04_멀티사이트_및_도메인_관제.md) |
| `SCR-KZ-05` | `perm-kkumz-dom-012` | `빌드 로그` | `에피소드` (샌드박스 빌드) | `/deployment/builds` | `deployment/builds/index.vue` | `BuildLogModal.vue` | [SCR-KZ-05_빌드_파이프라인_및_로그.md](../screens/SCR-KZ-05_빌드_파이프라인_및_로그.md) |
| `SCR-KZ-05-KEY` | `perm-kkumz-dom-013` | `API 키` | `크리에이터정산` (SyncShop/CMS 연동키) | `/deployment/api-keys` | `deployment/api-keys/index.vue` | - | [SCR-KZ-04_멀티사이트_및_도메인_관제.md](../screens/SCR-KZ-04_멀티사이트_및_도메인_관제.md) |
| `SCR-KZ-06` | `perm-kkumz-dom-021` | `프로젝트·팀원` | `광장게시글` / `채팅방` / `퀘스트` / `배지관리` | `/community/projects` | `community/projects/index.vue` | - | [SCR-KZ-06_프로젝트_및_팀빌딩_관리.md](../screens/SCR-KZ-06_프로젝트_및_팀빌딩_관리.md) |
| `SCR-KZ-07` | `perm-kkumz-dom-022` | `신고 관리` | `댓글모더` | `/community/moderation` | `community/moderation/index.vue` | `ModerationResolveModal.vue` | [SCR-KZ-07_신고_심사_및_모더레이션.md](../screens/SCR-KZ-07_신고_심사_및_모더레이션.md) |

---

## 3. 운영자 어드민 백오피스 (`admin/`) 시스템 및 동적 거버넌스 화면 인덱스

| 화면 ID | 권한 ID (`sys_permission.id`) | DB 메뉴명 (도메인 호칭) | Router Path (`url`) | 뷰 컴포넌트 (`admin/src/views/`) | 연동 테이블 및 핵심 기능 |
| :---: | :--- | :--- | :--- | :--- | :--- |
| `SCR-KZ-SYS-01` | `perm-kkumz-sys-011` | `사용자` (`사용자`) | `/system/user` | `system/user/index.vue` | `sys_user`, `sys_user_role` 크리에이터·모더레이터·관리자 계정 관리 |
| `SCR-KZ-SYS-02` | `perm-kkumz-sys-012` | `권한` (`권한`) | `/system/role` | `system/role/index.vue` | `sys_role`, `sys_role_permission` 역할별 메뉴 접근 권한 설정 |
| `SCR-KZ-SYS-03` | `perm-kkumz-sys-013` | `메뉴` (`메뉴`) | `/system/menu` | `system/menu/index.vue` | `sys_permission` DB 단일 원천 동적 메뉴 트리 관리 |
| `SCR-KZ-SYS-04` | `perm-kkumz-sys-014` | `공통 코드` (`퀘스트`/`배지관리`/`구독플랜` 사전) | `/system/dict` | `system/dict/index.vue` | `sys_dict`, `sys_dict_item` 22개 공통 코드 사전 관리 |
| `SCR-KZ-SYS-05` | `perm-kkumz-sys-015` | `프롬프트` (`프롬프트`) | `/system/prompt` | `system/prompt/index.vue` | `sys_prompt_template`, `sys_prompt_history` (`SystemPromptHolder`) 전사 AI 프롬프트 버전 관리 |
| `SCR-KZ-SYS-06` | `perm-kkumz-sys-config` | `시스템 환경설정` (`환경설정`) | `/system/config` | `system/config/index.vue` | `sys_config` (`SystemConfigHolder`, `ClusterEventBroadcaster`) 무중단 동적 환경설정 |
| `SCR-KZ-SYS-07` | `perm-kkumz-sys-016` | `부서` (`부서`) | `/system/depart` | `system/depart/index.vue` | 조직 및 크리에이터 운영 부서 트리 관리 |
| `SCR-KZ-SYS-08` | `perm-kkumz-sys-017` | `알림 발송` (`알림 발송`) | `/system/message` | `system/message/index.vue` | 시스템 알림 메시지 및 발송 템플릿 관리 |
| `SCR-KZ-SYS-09` | `perm-kkumz-sys-018` | `작업 로그` (`작업 로그`) | `/monitor/log` | `monitor/log/index.vue` | `sys_log` 관리자 및 시스템 작업 감사 로그 조회 |
| `SCR-KZ-SYS-10` | `perm-kkumz-sys-019` | `도움말 설정` (`도움말 설정`) | `/system/help-guide` | `system/help/index.vue` | `sys_page_help` 화면별 인앱 운영 가이드 및 데이터 리니지 관리 |
