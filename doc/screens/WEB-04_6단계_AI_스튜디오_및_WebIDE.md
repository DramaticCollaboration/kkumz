# WEB-04. 11-Step 모듈형 AI 스튜디오 및 WebIDE 화면 상세 명세서

## 1. 화면 기본 메타데이터
| 항목 | 내용 |
| :--- | :--- |
| **화면 ID** | `WEB-04` |
| **화면명** | 집중형 11-Step 모듈형 AI 브랜드·웹사이트·세계관 스튜디오 및 내장 WebIDE |
| **라우트 경로** | `/studio/:ideaId?` |
| **엔트리 뷰 컴포넌트** | `services/kkumz/web/src/views/StudioView.vue` |
| **모듈형 하위 컴포넌트 (11종)** | `services/kkumz/web/src/views/studio/` 하위 11개 Vue 컴포넌트 (`StudioStep1AssetUploadPanel.vue`, `StudioStep1FeatureCatalogPanel.vue`, `StudioStep2BrandPanel.vue`, `StudioStep3SpecPanel.vue`, `StudioStep4LivePreviewStage.vue`, `StudioStep5WebIdePanel.vue`, `StudioStep6DeployPanel.vue`, `StudioSidebar.vue`, `StudioTopbar.vue`, `StudioStatusBanners.vue`, `StudioAccessDeniedSection.vue`) |
| **레이아웃 모드** | `studio` (상단 GNB 숨김, 좌측 수직 스테퍼 `StudioSidebar.vue` + 상단 컨트롤바 `StudioTopbar.vue` + 중앙 모듈 워크벤치 + 우측 라이브 프리뷰/WebIDE) |
| **접근 권한** | 프로젝트 `OWNER`, `EDITOR`, `DEVELOPER` (비인가 접근 시 `StudioAccessDeniedSection.vue` 렌더링) |

---

## 2. 11-Step 모듈형 AI 스튜디오 컴포넌트 구성 (`views/studio/`)

| 단계 | 논리 11-Step 패널 식별자 | 물리 Vue 컴포넌트 파일명 | 상세 기능 및 데이터 바인딩 |
| :---: | :--- | :--- | :--- |
| **Step 1** | `StudioStep1UniversePanel` | `StudioStep1AssetUploadPanel.vue` | 프로젝트 제목(`title`), 아이디어·세계관 프롬프트(`promptText`), 5대 스타터 프리셋 선택 및 이미지·로고·안내문 문서(`kkumz_idea_asset`: `HERO`, `PRODUCT`, `ARTICLE`, `GALLERY`, `LOGO`, `DOC`) 업로드 및 텍스트 자동 추출 |
| **Step 2** | `StudioStep2CharacterCatalogPanel` | `StudioStep1FeatureCatalogPanel.vue` | `KKUMZ_INTENT_ADVISOR_V1` 기반 아키텍처·업종 자동 진단 및 `kkumz_feature_catalog` (SyncShop x SyncCMS x AI 52종 전체 기능 카탈로그) 선택·검색 |
| **Step 3** | `StudioStep3BrandNamingPanel` | `StudioStep2BrandPanel.vue` | AI 브랜드 네이밍 8종 후보(`kkumz_name_suggestion`, 사용자 명시 상호명 1순위 보존) 및 인라인 벡터 `<svg>` 로고 6종(`kkumz_logo_suggestion`) + 5대 디자인 토큰 생성·선택 |
| **Step 4** | `StudioStep4EpisodeSpecPanel` | `StudioStep3SpecPanel.vue` | AI 마크다운 상세 기능명세서(`spec_markdown`) 및 화면 블루프린트(`kkumz_screen_spec`) 생성·편집, 버전 히스토리(`kkumz_idea_version`) 비교 및 원클릭 버전 롤백 |
| **Step 5** | `StudioStep5LivePreviewPanel` | `StudioStep4LivePreviewStage.vue` | 업종 맞춤 초기 콘텐츠 프로비저닝(`KKUMZ_SITE_PROVISION_V1`: 상품/객실 3종, 요금표 매트릭스, 예약폼, 매거진, FAQ, 오시는 길) 및 자연어 대화형 테마·레이아웃 리파인(`STUDIO_CHAT_REFINE`) |
| **Step 6** | `StudioStep6WebIdeSandboxPanel` | `StudioStep5WebIdePanel.vue` | 가상 Vue 3 SFC(`HeroSection.vue`, `CustomStyles.css`, `site.config.json`) 코드 에디터, 샌드박스 빌드(`kkumz_build_job`) 실행 및 빌드 실패 시 `WEBIDE_AI_BUILD_FIXER` 자동 코드 패치 |
| **Step 7** | `StudioStep7GitProvisionPanel` | `StudioStep6DeployPanel.vue` (Git 리포지토리 섹션) | 멀티사이트 전용 Git 리포지토리(`kkumz_site_git_repo`), SSH 배포키 핑거프린트, 커밋 히스토리 및 사이트 전용 API 액세스키(`kkumz_api_access_key`) 프로비저닝 |
| **Step 8** | `StudioStep8StepNavigatorPanel` | `StudioSidebar.vue` | 스튜디오 단계별 진행 상태 표시, 단계 간 빠른 전환 및 버전 히스토리 타임라인 탐색 |
| **Step 9** | `StudioStep9TopbarControlPanel` | `StudioTopbar.vue` | 현재 편집 중인 브랜드명·슬러그 배지, 데스크톱/태블릿/모바일 프리뷰 뷰포트 전환, 즉시 저장 및 라이브 사이트 열기 |
| **Step 10** | `StudioStep10StatusDiagnosticPanel` | `StudioStatusBanners.vue` | AI 추론 진행 상태, 외부 연동 서킷브레이커 상태(`kkumz.integration.circuit-cooldown-ms`) 및 빌드 오류 진단 알림 배너 |
| **Step 11** | `StudioStep11PublishPanel` | `StudioAccessDeniedSection.vue` + `StudioStep6DeployPanel.vue` | 프로젝트 권한 검증 및 2-Track(`DYNAMIC_SDUI` / `CUSTOM_BUNDLE`) 라이브 공개 배포(`published_version = active_version`), 공개 범위(`PUBLIC`/`PRIVATE`) 설정 및 비공개 전환(`unpublish`) |

---

## 3. 연동 REST API 명세 (`/api/v1/kkumz/...`)
| 단계 | HTTP Method & URI | 설명 |
| :--- | :--- | :--- |
| Step 1~2 | `GET, POST, PUT /api/v1/kkumz/ideas/{id?}` | 아이디어·세계관 조회, 신규 생성, 업로드 자산 및 52종 기능 카탈로그 선택 저장 |
| Step 3 | `POST /api/v1/kkumz/ideas/{id}/generate-names`<br>`POST /api/v1/kkumz/ideas/{id}/select-name`<br>`POST /api/v1/kkumz/ideas/{id}/generate-logos`<br>`POST /api/v1/kkumz/ideas/{id}/select-logo` | AI 네이밍 8종 생성·확정 및 AI 벡터 SVG 로고 6종·디자인 토큰 생성·확정 |
| Step 4~5 | `POST /api/v1/kkumz/ideas/{id}/generate-spec`<br>`GET /api/v1/kkumz/ideas/{id}/versions`<br>`POST /api/v1/kkumz/ideas/{id}/rollback`<br>`POST /api/v1/kkumz/ideas/{id}/generate-screens` | AI 마크다운 기능명세서 생성, 버전 이력 조회 및 롤백, 화면 블루프린트 및 초기 SDUI 콘텐츠 생성 |
| Step 6 | `POST /api/v1/kkumz/sites/{siteSlug}/webide-save`<br>`POST /api/v1/kkumz/sites/{siteSlug}/webide-ai-fix` | WebIDE 가상 SFC 파일 저장·샌드박스 빌드 및 빌드 오류 AI 자동 코드 패치 |
| Step 7~11 | `POST /api/v1/kkumz/sites/{siteSlug}/deploy`<br>`POST /api/v1/kkumz/sites/{siteSlug}/unpublish` | 2-Track 멀티사이트 라이브 공개 배포, Git 리포지토리 동기화 및 비공개 초안 전환 |
