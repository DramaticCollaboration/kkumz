# Kkumz (꿈즈) AI Coding Agent Guidelines

> **문서 목적**: 본 문서는 `services/kkumz` 하위 프로젝트에서 코드를 작성·수정하거나 문서를 갱신하는 AI 코딩 에이전트(Antigravity 등) 및 개발자가 반드시 준수해야 할 도메인 특화 아키텍처, 기술 스택, 코딩 컨벤션을 정의합니다.

---

## 1. 프로젝트 정체성 및 도메인 경계

- **서비스명**: **Kkumz (꿈즈)** — AI 브랜드·웹사이트 빌더 및 창업 팀빌딩 플랫폼
- **포지셔닝**: SyncSeries의 Vertical Application Service (`services/kkumz`)로서, 1인 창업가와 메이커가 아이디어 한 줄로 브랜드 아이덴티티(네이밍 8종, 벡터 SVG 로고 6종), 기능명세서(마크다운 및 버전 관리), 화면 설계 블루프린트를 생성하고, **SyncShop**(커머스) 및 **SyncCMS**(콘텐츠/폼) 멀티테넌트 인스턴스를 프로비저닝하여 실제 작동하는 웹사이트(`/s/:siteSlug`)를 배포하고 동료 팀원을 모집하는 올인원 창업 플랫폼입니다.
- **절대 원칙**:
  1. Kkumz는 단순 정적 랜딩페이지 생성기가 아니라 **SyncShop(`pms_product`, `oms_order`) 및 SyncCMS(`cms_content`, `cms_form_data`)의 멀티사이트 원장과 실시간 연동되는 실운영 플랫폼**입니다.
  2. 가짜 데이터 하드코딩(Mock Array 반환, `Math.random()` 난수 응답, 빈 `TODO` 스텁)을 전면 금지하며, 모든 조회·생성·상태 변경은 PostgreSQL 18.6 데이터베이스(`kkumz_*`, `sys_*`) 트랜잭션으로 영속화되어야 합니다.

---

## 2. Maven 좌표계, 패키지 및 디렉터리 표준

1. **Maven GroupId & ArtifactId**:
   - GroupId: `com.empasy.sync.kkumz`
   - 최상위 Parent POM (`services/kkumz/pom.xml`): `kkumz-parent`
   - 백엔드 Server Parent POM (`services/kkumz/Server/pom.xml`): `kkumz-server-parent`
   - 백엔드 실행 모듈 (`services/kkumz/Server/kkumz-server-admin/pom.xml`): `kkumz-server-admin`
   - 물리 디렉터리명은 반드시 `artifactId`와 1:1로 일치해야 합니다.
2. **Java 기본 패키지**:
   - `com.empasy.sync.kkumz` (`domain`, `system`, `config` 하위 패키지 구성)
3. **프론트엔드 디렉터리 및 npm Scoped Name**:
   - 운영자 백오피스 (`services/kkumz/admin/package.json`): `@empasy/kkumz-admin`
   - 사용자 웹 포털 & 스튜디오 (`services/kkumz/web/package.json`): `@empasy/kkumz-web`

---

## 3. API URL 단일 매핑 및 `/api/v1/kkumz` Prefix 규칙

1. **Canonical API Prefix 강제**:
   - 도메인 비즈니스 API (`KkumzController`): `/api/v1/kkumz/...`
   - 시스템/인증/RBAC API (`KkumzSystemController`): `/api/v1/kkumz/sys/...`
   - 동적 메뉴 및 권한 인가 API: `GET /api/v1/kkumz/sys/permission/getUserPermissionByToken`
2. **다중 URL 매핑 금지**:
   - `@RequestMapping({"/url1", "/url2"})` 형태의 복수 경로 매핑이나 레거시 `/sys/...`, `/api/v1/ideas` 등의 비표준 경로 노출을 전면 금지합니다.
3. **투명 프록시 (Transparent Proxy)**:
   - `admin/` 및 `web/`의 Vite Dev Server 프록시는 `/api/v1/kkumz` 요청을 경로 변형(rewrite) 없이 백엔드(`http://localhost:8096/api/v1/kkumz`)로 1:1 전달해야 합니다.

---

## 4. 데이터베이스 6단계 스크립트 표준 (`services/kkumz/db/`)

1. **물리 위치 및 실행 순서**:
   - 모든 SQL 파일은 `services/kkumz/db/` 루트에 위치하며 아래 6단계 구성을 엄격히 유지합니다:
     - `00_extensions.sql`: `vector`, `uuid-ossp`, `pg_trgm` 확장 모듈
     - `01_schema_system.sql`: `sys_user`, `sys_role`, `sys_permission`, `sys_user_role`, `sys_role_permission`, `sys_dict`, `sys_dict_item`, `sys_log` (8개 테이블)
     - `02_schema_domain.sql`: `kkumz_idea`, `kkumz_idea_version`, `kkumz_name_suggestion`, `kkumz_logo_suggestion`, `kkumz_screen_spec`, `kkumz_site`, `kkumz_site_release`, `kkumz_build_job`, `kkumz_team_recruit`, `kkumz_team_member`, `kkumz_feed_comment`, `kkumz_feed_like`, `kkumz_moderation_report`, `kkumz_ai_prompt_template`, `kkumz_notification` (15개 도메인 테이블)
     - `03_seed_system.sql`: 시스템 역할 4종(`role_admin`, `role_moderator`, `role_creator`, `role_developer`), 기본 계정 6종, 공통 시스템 메뉴(`perm-kkumz-sys-*`), 공통 사전(`kkumz_site_type`, `kkumz_deploy_mode`, `kkumz_team_role`, `kkumz_site_status`)
     - `04_seed_domain.sql`: 어드민 도메인 메뉴(`perm-kkumz-dom-001` ~ `022`), 역할별 메뉴 권한 매핑(`srp_kkumz_*`), AI 프롬프트 템플릿 4종(`tpl-kkumz-001` ~ `004`)
     - `05_sample_dev.sql`: 개발/시연용 4대 대표 창업 아이디어 및 라이브 멀티사이트(`matcha-atelier`, `desk-orbit`, `seoul-run-club`, `bean-chronicle`) 풀세트 시드 데이터
2. **마이그레이션 파일 분리 및 `ON CONFLICT` 금지**:
   - 제품 개발 단계에서 `db/migrations/` 디렉터리나 개별 `ALTER TABLE` 패치 파일을 생성하지 않고, `01_schema_*` ~ `05_sample_dev.sql` 원본 스크립트에 직접 통합합니다.
   - 모든 SQL 스크립트에서 `ON CONFLICT DO NOTHING`, `ON CONFLICT DO UPDATE` 등 임시 충돌 회피 구문 사용을 금지하며, 결정론적 고유 ID(`perm-kkumz-*`, `srp_kkumz_*`, `idea-kkumz-*`, `site-kkumz-*`)를 사용한 순수 `INSERT INTO` 구문만 허용합니다.

---

## 5. 프론트엔드 아키텍처 및 컴포넌트 규칙

### 5.1 운영자 백오피스 (`services/kkumz/admin/`)
1. **DB 단일 원천 동적 라우팅 (`PermissionModeEnum.BACK`)**:
   - `src/settings/projectSetting.ts`의 권한 모드는 반드시 `PermissionModeEnum.BACK`으로 고정합니다.
   - `src/router/routes/modules/` 하위에 정적 라우트 파일을 생성하거나 하드코딩하는 행위를 금지합니다.
   - 신규 화면 추가 시 반드시 `db/04_seed_domain.sql`의 `sys_permission` 및 `sys_role_permission`에 등록해야 합니다.
2. **Vben 표준 고수준 컴포넌트 최우선 사용**:
   - 원시 `<a-table>`, `<a-modal>`, `<a-form>` 직접 작성 대신 Vben 프레임워크 표준 컴포넌트인 `BasicTable` + `useTable` + `TableAction`, `BasicModal` + `useModalInner`, `BasicForm` + `useForm`, `PageWrapper`를 사용합니다.
   - 테이블/모달/폼 구성 스키마는 `*.data.ts` 파일로 분리하고, 팝업 모달은 `*Modal.vue`로 분리하여 단일 책임 원칙을 준수합니다.

### 5.2 창업가 웹 포털 & 스튜디오 (`services/kkumz/web/`)
1. **듀얼 레이아웃 체계 (Dual Layout Architecture)**:
   - **커뮤니티/포털 레이아웃 (`/feed`, `/lounge`, `/workspace`)**: 상단 1단 글로벌 내비게이션 바(GNB) + 실시간 인앱 알림 드롭다운 + 본문 컨테이너로 구성합니다.
   - **집중형 스튜디오 레이아웃 (`/studio/:ideaId`)**: 상단 GNB를 숨기고, 좌측 고정 6-Step 수직 스테퍼 + 중앙 작업 캔버스 + 우측 실시간 프리뷰/WebIDE 패널 구조로 몰입도를 극대화합니다.
   - **멀티사이트 라이브 렌더러 (`/s/:siteSlug`)**: Kkumz 플랫폼 헤더 대신 해당 사이트의 고유 브랜드 로고(SVG), 브랜드명, 동적 컬러 토큰(`design_tokens_json`), 장바구니/콘텐츠 탭, 하단 고정 `Built with Kkumz AI Studio` 플로팅 배지 및 AI 컨시어지 위젯을 렌더링합니다.

---

## 6. Java 백엔드 개발 규칙

1. **Lombok 및 생성자 주입 필수**:
   - 모든 DTO/VO/Entity에 `@Data`, `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`를 적용하고 수동 보일러플레이트 작성을 금지합니다.
   - 로깅은 `@Slf4j`, 의존성 주입은 `private final` 필드 + `@RequiredArgsConstructor`를 사용합니다.
2. **표준 응답 래퍼**:
   - 모든 REST API는 `{ code: 200, success: true, message: "OK", result: ... }` 규격(`KkumzController.ok(...)` / `KkumzSystemController` 표준 포맷)으로 응답하여 Vben Admin 및 Web 클라이언트 인터셉터와 정합성을 유지합니다.
3. **AI 프레임워크**:
   - LLM 연동 및 자율 에이전트 기능 확장 시 전사 표준인 **AgentScope Java (`io.agentscope:agentscope-harness`)** 및 `kkumz_ai_prompt_template` 테이블에 등록된 프롬프트 설정을 기반으로 동작해야 합니다.

---

## 7. 문서화 및 커뮤니케이션 품질 수칙

1. **장식용 이모지 사용 전면 금지**:
   - 모든 마크다운 문서(`README.md`, `doc/**/*.md`), 주석, UI 텍스트에서 유니코드 그림 이모지 사용을 금지합니다. 강조가 필요한 경우 마크다운 볼드체, 인용구(`> [!IMPORTANT]`), 표를 활용합니다.
2. **불필요한 한영 병기 지양**:
   - 한글만으로 의미가 명확한 제목 및 레이블에 불필요한 괄호 영문 표기를 반복하지 않습니다.
3. **참고 핵심 문서**:
   - [`PROJECT_ONBOARDING_GUIDE.md`](./PROJECT_ONBOARDING_GUIDE.md)
   - [`doc/README.md`](./doc/README.md)
   - [`doc/01_Kkumz_서비스_개발기획서.md`](./doc/01_Kkumz_서비스_개발기획서.md)
   - [`doc/07_Kkumz_데이터모델링_및_인터페이스정의서.md`](./doc/07_Kkumz_데이터모델링_및_인터페이스정의서.md)
   - [`doc/ai/00_AI_Agentization_Plan.md`](./doc/ai/00_AI_Agentization_Plan.md)
