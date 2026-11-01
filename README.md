# Kkumz (꿈즈) — AI 브랜드·웹사이트 빌더 및 창업 팀빌딩 플랫폼

> **SyncSeries Vertical Application Service (`services/kkumz`)**
> 1인 메이커와 초기 창업팀이 아이디어 한 줄만 입력하면 브랜드 네이밍, 벡터 SVG 로고, 기능명세서, 화면 블루프린트를 생성하고, **SyncShop**(커머스 스토어) 및 **SyncCMS**(브랜드·커뮤니티 사이트) 멀티테넌트 인스턴스를 프로비저닝하여 라이브 웹사이트 오픈부터 동료 팀원 모집까지 수행하는 AI 창업 스튜디오 플랫폼입니다.

---

## 1. 아키텍처 및 프로젝트 디렉터리 구조

```text
services/kkumz/
├── pom.xml                           # 최상위 Maven Parent POM (com.empasy.sync.kkumz:kkumz-parent)
├── README.md                         # 프로젝트 개요 및 빠른 시작 가이드
├── AGENTS.md                         # AI 코딩 에이전트 개발 표준 지침서
├── PROJECT_ONBOARDING_GUIDE.md       # 신규 참여자용 온보딩 및 아키텍처 가이드
├── docker-compose-local.yml          # IDE 로컬 개발용 인프라 (PostgreSQL 18.6, Redis, MinIO)
├── docker-compose-dev.yml            # 통합 컨테이너 실행 클러스터 (Spring Profile: dev)
├── db/                               # 전사 표준 6단계 PostgreSQL 18.6 초기화 스크립트
│   ├── 00_extensions.sql             # 필수 확장 모듈 (vector, uuid-ossp, pg_trgm)
│   ├── 01_schema_system.sql          # 시스템 공통 관리 테이블 (sys_user, sys_role, sys_permission 등 8종)
│   ├── 02_schema_domain.sql          # Kkumz 도메인 비즈니스 테이블 15종 (kkumz_idea, kkumz_site 등)
│   ├── 03_seed_system.sql            # 시스템 역할(role_admin, role_moderator, role_creator, role_developer) 및 공통 메뉴
│   ├── 04_seed_domain.sql            # 어드민 백오피스 도메인 메뉴(sys_permission) 및 AI 프롬프트 템플릿 4종
│   └── 05_sample_dev.sql             # [DEV/TEST ONLY] 4대 대표 멀티사이트 및 스튜디오·커뮤니티 시드 데이터
├── doc/                              # Kkumz 엔터프라이즈 기획·설계·운영 산출물
│   ├── README.md                     # 전체 문서 네비게이션 인덱스
│   ├── 01 ~ 11_*.md                  # 기획서, 권한체계, 디자인시스템, 화면목록, 상세명세, DB/API, 운영 SOP 등
│   ├── ai/00_AI_Agentization_Plan.md # AgentScope Java 기반 AI 에이전트 아키텍처 명세
│   └── screens/                      # 화면별 개별 상세 명세서 (WEB-01~05, SCR-KZ-01~07)
├── Server/                           # Spring Boot 3.5 + Java 25 백엔드 (kkumz-server-parent)
│   ├── pom.xml
│   └── kkumz-server-admin/           # 통합 백엔드 애플리케이션 (포트: 8096, API Prefix: /api/v1/kkumz)
├── admin/                            # 운영자 백오피스 (Vue 3 + Vben Admin, @empasy/kkumz-admin, 포트: 3108)
└── web/                              # 창업가 포털 & 6-Step AI 스튜디오 & 멀티사이트 렌더러 (@empasy/kkumz-web, 포트: 3008)
```

---

## 2. 핵심 비즈니스 워크플로우

### 2.1 집중형 6-Step AI 브랜드·사이트 스튜디오 (`/studio/:ideaId`)
1. **Step 1. 아이디어 스케치**: 창업 아이디어 한 줄 요약, 문제 정의, 핵심 타깃 고객, 사이트 유형(`COMMERCE` 커머스 스토어 / `CMS_SITE` 브랜드·매거진 사이트 / `HYBRID` 복합형) 선택
2. **Step 2. AI 브랜드 네이밍 (8종 후보)**: 한/영/혼합 브랜드명 8종 생성, 네이밍 스토리 해설, 사이트 슬러그(`/s/{slug}`) 및 서브도메인(`{slug}.kkumz.io`) 실시간 중복 검증
3. **Step 3. AI 벡터 SVG 로고 & 디자인 토큰 (6종 후보)**: 심볼형·워드마크형·엠블럼형 순수 벡터 SVG 로고 6종 생성 및 브랜드 컬러 팔레트(`primary`, `secondary`, `accent`, `surface`, `radius`) 자동 추출
4. **Step 4. AI 기능명세서 및 버전 히스토리**: 타깃 페르소나, MVP 핵심 기능, 수익화 모델, SyncShop/SyncCMS 연동 명세가 포함된 마크다운 기획서 자동 작성 및 버전 스냅샷(`v1`, `v2`...) 비교·롤백
5. **Step 5. 화면 설계 블루프린트**: 라우트 경로, 레이아웃 구성 블록, 데이터 바인딩 컴포넌트 구조 설계 및 실시간 인터랙티브 프리뷰
6. **Step 6. 2-Track 하이브리드 배포 & WebIDE**:
   - **Track 1 (`DYNAMIC_SDUI`)**: 코드 빌드 대기 시간 없이(0초) JSON 레이아웃·디자인 토큰 스키마를 즉시 반영하여 오픈
   - **Track 2 (`CUSTOM_BUNDLE`)**: 브라우저 내장 WebIDE에서 생성된 Vue 3 SFC(`HeroSection.vue`, `CustomStyles.css`, `site.config.json`)를 편집하고, 샌드박스 빌드 오류 발생 시 **AI 에러 자동 패치**로 복구하여 MinIO 오브젝트 스토리지에 배포

### 2.2 SyncShop & SyncCMS 멀티테넌트 실데이터 브릿지 (`/s/:siteSlug`)
- **SyncShop 커머스 연동**: `kkumz_site.shop_site_id`를 통해 `sync_shop` DB의 상품(`pms_product`) 목록 조회 및 실제 주문(`oms_order`, `oms_order_item`) 생성, 창업가 실시간 주문 알림 발송
- **SyncCMS 콘텐츠·폼 연동**: `kkumz_site.cms_site_id`를 통해 `sync_cms` DB의 아티클(`cms_content`) 렌더링 및 고객 리드·사전예약 폼(`cms_form_data`) 접수
- **AI 브랜드 컨시어지 챗봇**: 각 배포 사이트 하단에 내장되어 해당 브랜드의 기능명세서와 등록 상품·아티클 컨텍스트를 기반으로 방문자 질문에 실시간 답변

### 2.3 창업 커뮤니티 쇼케이스 및 팀빌딩 라운지 (`/feed`, `/lounge`)
- **쇼케이스 피드**: 배포된 라이브 사이트 카드 탐색, 산업/유형 필터링, 좋아요 및 응원·피드백 댓글 소통, 원클릭 라이브 사이트 방문
- **팀빌딩 라운지**: 프론트엔드 개발자(`FRONTEND`), 브랜드 디자이너(`DESIGNER`), 그로스 마케터(`MARKETER`), 커머스 MD(`MD`), 공동창업자(`CO_FOUNDER`) 모집 공고 등록, 역할 신청 및 프로젝트 리더의 승인/반려(`OWNER`, `EDITOR`, `DEVELOPER`, `MD` 권한 부여)

---

## 3. 애플리케이션 화면 구성

### 3.1 사용자 웹 포털 & 스튜디오 (`web/` — `@empasy/kkumz-web`, 포트 `3008`)
| 화면 ID | 라우트 경로 | 뷰 컴포넌트 | 주요 역할 |
| :--- | :--- | :--- | :--- |
| **WEB-01** | `/feed` | `src/views/FeedView.vue` | 라이브 사이트 쇼케이스 피드, 유형 필터링, 좋아요·댓글 피드백 |
| **WEB-02** | `/lounge/:siteSlug?` | `src/views/ProjectLoungeView.vue` | 창업 프로젝트 팀원 모집 공고 탐색 및 합류 지원, 모집 공고 등록 |
| **WEB-03** | `/workspace` | `src/views/WorkspaceView.vue` | 내 창업 아이디어 목록, 배포 멀티사이트 상태, 지원자 승인/반려 관리 |
| **WEB-04** | `/studio/:ideaId?` | `src/views/StudioView.vue` | 6-Step AI 브랜드·웹사이트 빌더, 내장 WebIDE, AI 빌드 에러 패치 |
| **WEB-05** | `/s/:siteSlug` | `src/views/LiveSiteRendererView.vue` | 멀티사이트 라이브 렌더러 (SyncShop 주문, SyncCMS 폼, AI 컨시어지) |

### 3.2 운영자 어드민 백오피스 (`admin/` — `@empasy/kkumz-admin`, 포트 `3108`)
- 모든 메뉴와 라우트는 DB(`sys_permission`, `sys_role_permission`) 단일 원천에서 공급되며 `PermissionModeEnum.BACK` 모드로 동작합니다.
- 백엔드 인가 API: `GET /api/v1/kkumz/sys/permission/getUserPermissionByToken`

| 화면 ID | 권한 코드 (`sys_permission.id`) | 라우트 경로 | 뷰 컴포넌트 (`src/views/`) | 주요 기능 |
| :--- | :--- | :--- | :--- | :--- |
| **SCR-KZ-01** | `perm-kkumz-sys-002` | `/dashboard/analysis` | `dashboard/analysis/index.vue` | 아이디어·멀티사이트·빌드 성공률·신고 현황 종합 관제 |
| **SCR-KZ-02** | `perm-kkumz-dom-002` | `/studio/ideas` | `studio/ideas/index.vue` | 창업 아이디어, 네이밍·로고 후보, 마크다운 기능명세서 조회 |
| **SCR-KZ-03** | `perm-kkumz-dom-003` | `/studio/prompts` | `studio/prompts/index.vue` | 단계별 AI 시스템 프롬프트 템플릿, 모델 파라미터, 금지어 관리 |
| **SCR-KZ-04** | `perm-kkumz-dom-011` | `/deployment/sites` | `deployment/sites/index.vue` | 배포 멀티사이트 상태(`PUBLISHED`/`SUSPENDED`), 릴리즈 버전 관제 |
| **SCR-KZ-05** | `perm-kkumz-dom-012` | `/deployment/builds` | `deployment/builds/index.vue` | WebIDE 샌드박스 빌드 파이프라인 이력 및 AI 자동 패치 로그 |
| **SCR-KZ-06** | `perm-kkumz-dom-021` | `/community/projects` | `community/projects/index.vue` | 팀원 모집 공고 상태 제어 및 프로젝트 합류 팀원 현황 조회 |
| **SCR-KZ-07** | `perm-kkumz-dom-022` | `/community/moderation` | `community/moderation/index.vue` | 피드·사이트·모집공고 신고 심사 및 사이트 즉시 정지 처분 |
| **SCR-KZ-SYS-01~04** | `perm-kkumz-sys-011~014` | `/system/*` | `system/{user,role,menu,dict}/index.vue` | 사용자 계정, 역할·권한 매핑, 동적 메뉴 트리, 공통 사전 관리 |

---

## 4. 기술 스택 및 표준 규격

| 영역 | 적용 기술 | 비고 |
| :--- | :--- | :--- |
| **Backend** | Java 25, Spring Boot 3.5, Spring JDBC (`JdbcTemplate`), Lombok | `com.empasy.sync.kkumz:kkumz-server-admin` (`8096`) |
| **AI Framework** | AgentScope Java (`io.agentscope:agentscope-harness`) | 전사 표준 AI 프레임워크 및 프롬프트 템플릿 엔진 |
| **Database** | PostgreSQL 18.6 (`pgvector`, `uuid-ossp`, `pg_trgm`) | `db/00_extensions.sql` ~ `05_sample_dev.sql` |
| **Cache & Storage** | Redis 7.4, MinIO Object Storage | 세션/캐시 및 `CUSTOM_BUNDLE` 정적 번들 아티팩트 저장 |
| **Admin Frontend** | Vue 3, TypeScript, Vite, Ant Design Vue, Vben Admin | `@empasy/kkumz-admin` (`3108`), `PermissionModeEnum.BACK` |
| **User Web / Studio** | Vue 3, TypeScript, Vite, Vue Router 4 | `@empasy/kkumz-web` (`3008`), 2-Tier Dual Layout & SDUI Renderer |

---

## 5. 로컬 개발 환경 빠른 시작

### 5.1 로컬 인프라 기동 (`docker-compose-local.yml`)
```bash
cd services/kkumz
docker compose -f docker-compose-local.yml up -d
```
- PostgreSQL 18.6 컨테이너 기동 시 `db/00_extensions.sql`부터 `db/05_sample_dev.sql`까지 6단계 스크립트가 순서대로 자동 초기화됩니다.

### 5.2 백엔드 기동 (`kkumz-server-admin`)
```bash
cd services/kkumz/Server
mvn spring-boot:run -pl kkumz-server-admin -Dspring-boot.run.profiles=local
```
- 기본 포트: `http://localhost:8096`
- 헬스체크 및 통계 확인: `GET http://localhost:8096/api/v1/kkumz/stats`

### 5.3 프론트엔드 기동 (`web/` 및 `admin/`)
```bash
# 창업가 포털 & 6-Step AI 스튜디오 (http://localhost:3008)
cd services/kkumz/web
pnpm install && pnpm dev

# 운영자 어드민 백오피스 (http://localhost:3108)
cd services/kkumz/admin
pnpm install && pnpm dev
```

### 5.4 기본 시드 계정 (`db/03_seed_system.sql`)
- **총괄관리자 (`role_admin`)**: `admin` / `123456`
- **커뮤니티 모더레이터 (`role_moderator`)**: `moderator.kim` / `123456`
- **창업가 메이커 (`role_creator`)**: `soyeon.jeju` (말차 아뜰리에 제주), `minjun.maker` (데스크 오빗), `jihoon.run` (서울 나이트 런 클럽), `hajun.roast` (빈 크로니클 서울) / `123456`

---

## 6. 상세 문서 안내
전체 기획·설계·데이터베이스·API·운영 문서는 [`doc/README.md`](./doc/README.md) 및 [`PROJECT_ONBOARDING_GUIDE.md`](./PROJECT_ONBOARDING_GUIDE.md)에서 확인할 수 있습니다.
