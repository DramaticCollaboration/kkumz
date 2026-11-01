# Kkumz (꿈즈) — 통합 산출물 네비게이션 맵 및 문서 인덱스

> **프로젝트에 처음 합류하셨나요?**
> 상세 설계 문서를 읽기 전에 루트 폴더의 **[`PROJECT_ONBOARDING_GUIDE.md`](../PROJECT_ONBOARDING_GUIDE.md)** 및 **[`AGENTS.md`](../AGENTS.md)** 를 먼저 읽으시면 15분 만에 전체 도메인 구조와 개발 표준을 파악하실 수 있습니다.

- **프로젝트명**: Kkumz (꿈즈) — AI 브랜드·웹사이트 빌더 및 창업 팀빌딩 플랫폼
- **문서 체계 버전**: v2.6.5 (SyncSeries 엔터프라이즈 표준 정렬판)
- **대상 범위**: 창업가 포털 & 6-Step AI 스튜디오 (`web/`), 운영자 어드민 백오피스 (`admin/`), 통합 백엔드 서버 (`Server/kkumz-server-admin`), 6단계 DB 스크립트 (`db/00_extensions.sql` ~ `05_sample_dev.sql`)

---

## 1. 전체 산출물 구성도 (Master Document Index)

| 문서 번호 | 파일명 | 대상 독자 | 핵심 수록 내용 |
| :---: | :--- | :--- | :--- |
| **DOC-KZ-00** | **[`../PROJECT_ONBOARDING_GUIDE.md`](../PROJECT_ONBOARDING_GUIDE.md)** | **전 직군 필독** | **15분 요약 온보딩 가이드**, 전체 화면 지하철 노선도, 3대 핵심 메커니즘, 직군별 패스트트랙 |
| **DOC-KZ-01** | **[`01_Kkumz_서비스_개발기획서.md`](./01_Kkumz_서비스_개발기획서.md)** | PM, 전 직군 | 서비스 비전, 4대 창업가 페르소나, 6-Step AI 스튜디오 여정, 2-Track 배포, SyncShop/SyncCMS 연동 모델 |
| **DOC-KZ-02** | **[`02_Kkumz_사용자역할정의_및_권한체계.md`](./02_Kkumz_사용자역할정의_및_권한체계.md)** | 기획, FE, BE | 시스템 4대 역할(`role_admin` 등) + 프로젝트 팀 4대 역할(`OWNER`, `DEVELOPER` 등) 이중 RBAC 및 동적 라우팅 |
| **DOC-KZ-03** | **[`03_Kkumz_UIUX_디자인시스템_및_스타일가이드.md`](./03_Kkumz_UIUX_디자인시스템_및_스타일가이드.md)** | 디자이너, FE | 사용자 웹 듀얼 레이아웃(포털 GNB vs 집중형 스튜디오), 멀티사이트 동적 테마 토큰, 어드민 Vben 표준 규격 |
| **DOC-KZ-04** | **[`04_Kkumz_구현화면목록.md`](./04_Kkumz_구현화면목록.md)** | 기획, FE, QA | 사용자 웹 5개 뷰(`WEB-01~05`) + 어드민 도메인 7개 화면(`SCR-KZ-01~07`) + 시스템 4개 화면 마스터 매트릭스 |
| **DOC-KZ-05** | **[`05_Kkumz_화면별상세명세서.md`](./05_Kkumz_화면별상세명세서.md)** | FE, BE, QA | 전 화면 UI 레이아웃, 컬럼·폼 구성, 상태 전이, 버튼 액션 및 `/api/v1/kkumz/...` 바인딩 명세 |
| **DOC-KZ-06** | **[`06_Kkumz_기능명세서_및_멀티사이트아키텍처.md`](./06_Kkumz_기능명세서_및_멀티사이트아키텍처.md)** | BE, FE, 아키텍트 | 아이디어·버전 롤백, 네이밍·로고 생성, 2-Track 배포(`DYNAMIC_SDUI`/`CUSTOM_BUNDLE`), WebIDE & AI 패치, 팀빌딩 |
| **DOC-KZ-07** | **[`07_Kkumz_데이터모델링_및_인터페이스정의서.md`](./07_Kkumz_데이터모델링_및_인터페이스정의서.md)** | BE, DBA, FE | 6단계 DB 표준, 시스템 8개 + 도메인 15개 테이블 전 컬럼 명세, `/api/v1/kkumz/**` REST API 전체 명세 |
| **DOC-KZ-08** | **[`08_Kkumz_기술연동_및_AI프롬프트_명세서.md`](./08_Kkumz_기술연동_및_AI프롬프트_명세서.md)** | AI, BE | AgentScope Java 연동, 4대 AI 프롬프트 템플릿(`tpl-kkumz-001~004`), 금지어 필터, SyncShop/SyncCMS 크로스 DB 브릿지 |
| **DOC-KZ-09** | **[`09_Kkumz_초기_시드데이터_및_테스트시나리오.md`](./09_Kkumz_초기_시드데이터_및_테스트시나리오.md)** | QA, 개발자 | 6단계 SQL 초기화 검증, 4대 샘플 멀티사이트(`matcha-atelier` 등) 시드 명세, E2E 인수 테스트 시나리오 |
| **DOC-KZ-10** | **[`10_Kkumz_서비스_운영매뉴얼_및_SOP.md`](./10_Kkumz_서비스_운영매뉴얼_및_SOP.md)** | 운영자, 모더레이터 | 일일 관제 루틴, AI 프롬프트 튜닝 및 금지어 차단 SOP, 멀티사이트 정지·복구 및 신고 모더레이션 SOP |
| **DOC-KZ-11** | **[`11_Kkumz_상용오픈_및_환경설정_가이드.md`](./11_Kkumz_상용오픈_및_환경설정_가이드.md)** | DevOps, 보안 | 4-Tier Spring 프로파일(`local`/`dev`/`prod`), 와일드카드 도메인(`*.kkumz.io`), MinIO 번들 스토리지, 라이선스·보안 정책 |
| **DOC-KZ-AI** | **[`ai/00_AI_Agentization_Plan.md`](./ai/00_AI_Agentization_Plan.md)** | AI 아키텍트, BE | AgentScope Java 기반 5대 전문 에이전트(`BrandNamingAgent` 등) 설계 및 SyncVerse/SyncShop/SyncCMS A2A 연동 계획 |

---

## 2. 화면별 개별 상세 명세서 (`screens/`)

전체 화면 통합 명세(`05_Kkumz_화면별상세명세서.md`)와 함께, 각 화면 단위의 독립 명세서를 `doc/screens/` 하위에서 관리합니다.

### 2.1 창업가 포털 & AI 스튜디오 (`web/` — `@empasy/kkumz-web`)
- [`screens/WEB-01_쇼케이스_피드.md`](./screens/WEB-01_쇼케이스_피드.md) (`/feed` — `FeedView.vue`)
- [`screens/WEB-02_팀빌딩_라운지.md`](./screens/WEB-02_팀빌딩_라운지.md) (`/lounge/:siteSlug?` — `ProjectLoungeView.vue`)
- [`screens/WEB-03_내_워크스페이스.md`](./screens/WEB-03_내_워크스페이스.md) (`/workspace` — `WorkspaceView.vue`)
- [`screens/WEB-04_6단계_AI_스튜디오_및_WebIDE.md`](./screens/WEB-04_6단계_AI_스튜디오_및_WebIDE.md) (`/studio/:ideaId?` — `StudioView.vue`)
- [`screens/WEB-05_멀티사이트_라이브_렌더러.md`](./screens/WEB-05_멀티사이트_라이브_렌더러.md) (`/s/:siteSlug` — `LiveSiteRendererView.vue`)

### 2.2 운영자 어드민 백오피스 (`admin/` — `@empasy/kkumz-admin`)
- [`screens/SCR-KZ-01_운영_종합_대시보드.md`](./screens/SCR-KZ-01_운영_종합_대시보드.md) (`/dashboard/analysis` — `perm-kkumz-sys-002`)
- [`screens/SCR-KZ-02_아이디어_및_명세서_관리.md`](./screens/SCR-KZ-02_아이디어_및_명세서_관리.md) (`/studio/ideas` — `perm-kkumz-dom-002`)
- [`screens/SCR-KZ-03_AI_프롬프트_및_필터_관리.md`](./screens/SCR-KZ-03_AI_프롬프트_및_필터_관리.md) (`/studio/prompts` — `perm-kkumz-dom-003`)
- [`screens/SCR-KZ-04_멀티사이트_및_도메인_관제.md`](./screens/SCR-KZ-04_멀티사이트_및_도메인_관제.md) (`/deployment/sites` — `perm-kkumz-dom-011`)
- [`screens/SCR-KZ-05_빌드_파이프라인_및_로그.md`](./screens/SCR-KZ-05_빌드_파이프라인_및_로그.md) (`/deployment/builds` — `perm-kkumz-dom-012`)
- [`screens/SCR-KZ-06_프로젝트_및_팀빌딩_관리.md`](./screens/SCR-KZ-06_프로젝트_및_팀빌딩_관리.md) (`/community/projects` — `perm-kkumz-dom-021`)
- [`screens/SCR-KZ-07_신고_심사_및_모더레이션.md`](./screens/SCR-KZ-07_신고_심사_및_모더레이션.md) (`/community/moderation` — `perm-kkumz-dom-022`)

---

## 3. 문서-코드-DB 1:1 정합성 기준

1. **데이터베이스 스키마 원천**: [`../db/01_schema_system.sql`](../db/01_schema_system.sql), [`../db/02_schema_domain.sql`](../db/02_schema_domain.sql)
2. **시드 데이터 및 메뉴 원천**: [`../db/03_seed_system.sql`](../db/03_seed_system.sql), [`../db/04_seed_domain.sql`](../db/04_seed_domain.sql), [`../db/05_sample_dev.sql`](../db/05_sample_dev.sql)
3. **백엔드 API 원천**: [`KkumzController.java`](../Server/kkumz-server-admin/src/main/java/com/empasy/sync/kkumz/domain/KkumzController.java), [`KkumzSystemController.java`](../Server/kkumz-server-admin/src/main/java/com/empasy/sync/kkumz/system/KkumzSystemController.java)
4. **프론트엔드 화면 원천**: [`../web/src/views/`](../web/src/views/), [`../admin/src/views/`](../admin/src/views/)
