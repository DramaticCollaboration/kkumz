# DOC-KZ-05. Kkumz 화면별 상세 명세서

- **문서 번호**: DOC-KZ-05
- **대상 범위**: 사용자 웹 5개 뷰(`WEB-01` ~ `WEB-05`) 및 어드민 도메인 7개 화면(`SCR-KZ-01` ~ `SCR-KZ-07`)
- **참고**: 각 화면의 독립 파일 명세서는 [`doc/screens/`](./screens/) 하위에서도 개별 조회할 수 있습니다.

---

## Part 1. 창업가 포털 & AI 스튜디오 (`services/kkumz/web/`)

### 1.1 `WEB-01`: 쇼케이스 피드 (`/feed` — `FeedView.vue`)
- **화면 목적**: Kkumz 6-Step AI 스튜디오를 통해 배포된 라이브 멀티사이트들을 탐색하고, 사이트 유형별로 필터링하며, 좋아요·피드백 댓글·신고를 수행하는 창업 쇼케이스 허브입니다.
- **레이아웃 구성**:
  1. **히어로 배너**: 플랫폼 소개 카피, 전체 배포 사이트 수·누적 방문수·오픈 모집 공고 수 요약, `+ 5분 만에 내 브랜드 사이트 런칭하기 (/studio)` 버튼
  2. **유형 필터 탭**: `전체`, `COMMERCE (SyncShop 커머스)`, `CMS_SITE (SyncCMS 브랜드·매거진)`, `HYBRID (커머스+CMS 복합)`
  3. **쇼케이스 사이트 카드 그리드 (2열 반응형)**:
     - 브랜드 벡터 SVG 로고(`selected_logo_svg`) 및 브랜드명(`selected_brand_name`)
     - 배포 모드 배지(`DYNAMIC_SDUI` 노코드 즉시배포 / `CUSTOM_BUNDLE` WebIDE 커스텀 번들) 및 활성 버전(`v1`, `v2`)
     - 한 줄 요약(`summary`), 타깃 고객, 커스텀 서브도메인(`{slug}.kkumz.io`)
     - 오픈된 팀원 모집 포지션 태그(`FRONTEND`, `DESIGNER`, `MARKETER`, `MD`)
     - 하단 액션 바: `좋아요 토글 (likes_count)`, `댓글 열기 (comments_count)`, `팀빌딩 라운지 이동 (/lounge/:siteSlug)`, `신고하기`, **`라이브 사이트 방문 (/s/:siteSlug)`**
  4. **서브 모달**:
     - **피드백 댓글 드로어/모달**: 작성자명, 댓글 내용 입력 및 실시간 등록
     - **부적절 사이트 신고 모달**: 신고 사유(`SPAM`, `COPYRIGHT`, `PHISHING`, `INAPPROPRIATE`), 상세 설명 입력 후 접수

| UI 이벤트 | 호출 REST API | 요청/응답 핵심 데이터 |
| :--- | :--- | :--- |
| 화면 초기 진입 | `GET /api/v1/kkumz/feed` | 배포된 `kkumz_site` + `kkumz_idea` 조인 목록 |
| 좋아요 버튼 클릭 | `POST /api/v1/kkumz/feed/{siteId}/like` | `{ userId }` → 토글 후 갱신된 `likesCount` 반환 |
| 댓글 목록 조회 / 작성 | `GET, POST /api/v1/kkumz/feed/{siteId}/comments` | `{ authorName, content }` → `kkumz_feed_comment` INSERT |
| 신고 접수 제출 | `POST /api/v1/kkumz/moderation/reports` | `{ targetType: 'SITE', targetId, reasonCode, description }` |

---

### 1.2 `WEB-02`: 팀빌딩 라운지 (`/lounge/:siteSlug?` — `ProjectLoungeView.vue`)
- **화면 목적**: 배포된 멀티사이트 창업 프로젝트들이 필요한 핵심 동료(`FRONTEND`, `DESIGNER`, `MARKETER`, `MD`, `CO_FOUNDER`)를 모집하고, 예비 팀원이 포트폴리오와 지원 동기를 작성해 합류를 신청하는 팀빌딩 마켓플레이스입니다.
- **레이아웃 구성**:
  1. **상단 필터 및 공고 등록 바**: 포지션 역할 필터(`ALL`, `FRONTEND`, `DESIGNER`, `MARKETER`, `MD`), 사이트 슬러그 필터, `+ 새 팀원 모집 공고 등록` 버튼
  2. **모집 공고 카드 리스트**:
     - 소속 브랜드 로고(SVG), 브랜드명, 사이트 슬러그(`/s/:siteSlug` 바로가기)
     - 모집 포지션 배지(`role_needed`), 보상/지분 조건(`reward_type`: 지분 쉐어 / 수익 쉐어 / 스톡옵션 협의 등)
     - 공고 제목(`title`), 상세 업무 및 기술 스택(`description`), 현재 지원자 수(`applicants_count`)
     - **`이 포지션에 합류 지원하기`** 버튼
  3. **서브 모달**:
     - **팀원 합류 지원 모달**: 지원자 성명(`userName`), 이메일, 지원 포지션에 따른 프로젝트 권한 매핑(`DEVELOPER`, `EDITOR`, `MD`), 지원 동기 및 포트폴리오(`motivation`) 입력
     - **신규 모집 공고 등록 모달**: 내 프로젝트(`siteId`) 선택, 모집 포지션(`roleNeeded`), 공고 제목, 상세 설명, 보상 조건 입력

| UI 이벤트 | 호출 REST API | 요청/응답 핵심 데이터 |
| :--- | :--- | :--- |
| 모집 공고 목록 조회 | `GET /api/v1/kkumz/recruits` | `kkumz_team_recruit` + 브랜드 메타데이터 목록 |
| 신규 모집 공고 등록 | `POST /api/v1/kkumz/recruits` | `{ ideaId, siteId, title, roleNeeded, description, rewardType }` |
| 포지션 합류 지원 제출 | `POST /api/v1/kkumz/recruits/{id}/apply` | `{ userId, userName, motivation }` → `kkumz_team_member` (`PENDING`) 생성 및 리더 알림(`TEAM_APPLY`) 발송 |

---

### 1.3 `WEB-03`: 내 워크스페이스 (`/workspace` — `WorkspaceView.vue`)
- **화면 목적**: 창업가가 본인이 기획 중인 아이디어(`kkumz_idea`), 배포 완료한 멀티사이트(`kkumz_site`), 프로젝트에 지원하거나 합류한 팀원(`kkumz_team_member`)을 한곳에서 관리하고 지원자를 승인/반려하는 개인 컨트롤 타워입니다.
- **레이아웃 구성**:
  1. **상단 요약 KPI 카드 (4종)**: 내 창업 아이디어 수, 운영 중인 라이브 사이트 수, 누적 방문·좋아요 수, 승인 대기 중인 팀원 지원자 수(`PENDING`)
  2. **탭 1 — 내 창업 아이디어 & 스튜디오 진행 현황**:
     - 아이디어별 현재 진행 단계(`Step 1` ~ `Step 6`), 상태 배지(`DRAFT`, `SPEC_READY`, `DEPLOYED`), 선택된 브랜드명·SVG 로고 미리보기
     - `6-Step 스튜디오에서 이어서 편집 (/studio/:ideaId)` 버튼 및 `라이브 사이트 열기 (/s/:siteSlug)` 버튼
  3. **탭 2 — 배포된 멀티사이트 & 릴리즈 상태**:
     - 사이트별 배포 모드(`DYNAMIC_SDUI` / `CUSTOM_BUNDLE`), 활성 버전(`v1`, `v2`), 매핑된 `shop_site_id` / `cms_site_id`, 도메인 정보
  4. **탭 3 — 프로젝트 팀원 & 합류 지원자 관리**:
     - 지원자 성명, 요청 역할(`DEVELOPER`, `EDITOR`, `MD`), 지원 동기 및 포트폴리오 메시지, 상태(`PENDING` / `ACTIVE` / `REJECTED`)
     - `PENDING` 상태 행에 **`합류 승인 (ACTIVE 전환)`** 및 **`반려`** 액션 버튼 제공

| UI 이벤트 | 호출 REST API | 요청/응답 핵심 데이터 |
| :--- | :--- | :--- |
| 워크스페이스 데이터 로드 | `GET /api/v1/kkumz/ideas`, `/sites`, `/team-members` | 아이디어, 멀티사이트, 팀원/지원자 전체 목록 |
| 지원자 합류 승인 / 반려 | `POST /api/v1/kkumz/team-members/{id}/approve` | `{ status: 'ACTIVE' \| 'REJECTED' }` → 팀원 권한 활성화 |

---

### 1.4 `WEB-04`: 집중형 6-Step AI 스튜디오 & WebIDE (`/studio/:ideaId?` — `StudioView.vue`)
- **화면 목적**: 아이디어 입력부터 네이밍, 벡터 로고, 기능명세서(버전 롤백 포함), 화면 블루프린트, 2-Track 하이브리드 배포 및 WebIDE 코드 편집·AI 빌드 에러 자동 패치까지 수행하는 핵심 제작 스튜디오입니다.
- **6단계 상세 구성**:
  - **Step 1. 아이디어 스케치**: 프로젝트 제목, 한 줄 요약, 해결하려는 문제, 타깃 고객, 사이트 유형(`COMMERCE` / `CMS_SITE` / `HYBRID`) 입력 및 저장 (`POST /api/v1/kkumz/ideas`, `PUT /api/v1/kkumz/ideas/{id}`)
  - **Step 2. AI 브랜드 네이밍 (8종 후보)**:
    - `AI 브랜드 네이밍 8종 생성` 클릭 시 한/영/혼합 브랜드명 8종, 태그라인, 네이밍 스토리, 추천 슬러그(`/s/{slug}`), 슬러그 사용 가능 여부(`slug_available`) 카드 표시
    - 원하는 후보 클릭 시 `selected_brand_name` 및 `selected_site_slug` 즉시 확정 (`POST /api/v1/kkumz/ideas/{id}/select-name`)
  - **Step 3. AI 벡터 SVG 로고 & 디자인 토큰 (6종 후보)**:
    - `AI 벡터 SVG 로고 6종 생성` 클릭 시 심볼형·워드마크형·엠블럼형 `<svg>` 로고 6종과 5대 컬러 토큰(`primary`, `secondary`, `accent`, `surface`, `radius`) 팔레트 칩 표시
    - 선택 즉시 우측 라이브 프리뷰 패널의 헤더 로고와 전체 테마 색상이 실시간 변경 (`POST /api/v1/kkumz/ideas/{id}/select-logo`)
  - **Step 4. AI 마크다운 기능명세서 & 버전 롤백**:
    - `AI 기능명세서 자동 작성` 실행 시 서비스 개요, 핵심 타깃, MVP 기능 명세, SyncShop/SyncCMS 연동 구조, 수익 모델이 포함된 마크다운 생성
    - 우측/하단 **버전 히스토리 타임라인(`v1`, `v2`...)** 에서 변경 요약(`change_summary`) 확인 및 **`이 버전으로 롤백`** (`POST /api/v1/kkumz/ideas/{id}/rollback`) 지원
  - **Step 5. 화면 설계 블루프린트**:
    - 사이트 유형에 맞춘 핵심 화면 목록(`kkumz_screen_spec`: 홈 랜딩, 상품 카탈로그, 정기구독/프리오더, 브랜드 스토리 등) 및 구성 섹션 블록(`sections_json`) 생성·확인
  - **Step 6. 2-Track 하이브리드 배포 & 내장 WebIDE**:
    - **배포 트랙 선택**: `DYNAMIC_SDUI (노코드 0초 즉시 배포)` 또는 `CUSTOM_BUNDLE (WebIDE 커스텀 번들 빌드)`
    - **내장 WebIDE 코드 에디터**: `HeroSection.vue`, `CustomStyles.css`, `site.config.json` 탭 전환 및 소스코드 직접 수정, `샌드박스 빌드 & 배포` (`POST /api/v1/kkumz/sites/{siteSlug}/webide-save`)
    - **AI 빌드 에러 자동 패치 배너**: 빌드 상태가 `FAILED`인 경우 컴파일 에러 로그를 표시하고 **`AI 에러 자동 패치 실행`** 버튼(`POST /api/v1/kkumz/sites/{siteSlug}/webide-ai-fix`)으로 문법 오류를 자동 복구하여 재배포

---

### 1.5 `WEB-05`: 멀티사이트 라이브 렌더러 (`/s/:siteSlug` — `LiveSiteRendererView.vue`)
- **화면 목적**: 배포된 각 창업 브랜드의 고유 URL(`/s/matcha-atelier`, `/s/desk-orbit`, `/s/seoul-run-club`, `/s/bean-chronicle` 등)로 접속한 방문자에게 실제 작동하는 커머스 쇼핑몰(`SyncShop`), 브랜드 매거진·신청 폼(`SyncCMS`), AI 브랜드 컨시어지 챗봇을 제공하는 라이브 런타임 뷰입니다.
- **레이아웃 및 실데이터 연동**:
  1. **브랜드 전용 헤더**: 해당 사이트의 SVG 로고, 브랜드명, 커스텀 도메인 배지, 장바구니 담기 수량 배지
  2. **히어로 섹션**: `layout_schema_json`의 히어로 타이틀·서브타이틀 또는 `CUSTOM_BUNDLE`의 `HeroSection.vue` 커스텀 공지 배너 렌더링
  3. **SyncShop 실시간 상품 카탈로그 & 장바구니/주문 (`COMMERCE`, `HYBRID`)**:
     - `sync_shop.pms_product`에서 조회된 실제 상품 카드(상품 이미지, 상품명, 부제, 판매가, 재고, `장바구니 담기 / 즉시 구매`)
     - 주문자명(`receiverName`), 연락처(`receiverPhone`), 배송지(`receiverAddress`) 입력 후 **`실제 주문 결제하기`** 클릭 시 `POST /api/v1/kkumz/sites/{siteSlug}/orders` 호출 → `sync_shop.oms_order` 및 `oms_order_item`에 주문 생성 및 주문번호 반환
  4. **SyncCMS 브랜드 매거진 아티클 & 고객 리드 폼 (`CMS_SITE`, `HYBRID`)**:
     - `sync_cms.cms_content`에서 조회된 매거진 아티클 카드 및 본문 모달 열람
     - 뉴스레터 구독 / 크루 참가 신청 / B2B 샘플 문의 폼 제출 시 `POST /api/v1/kkumz/sites/{siteSlug}/forms` 호출 → `sync_cms.cms_form_data`에 JSONB 페이로드 영속화
  5. **AI 브랜드 컨시어지 플로팅 챗봇**:
     - 우측 하단 챗봇 창에서 배송·정기구독·상품 추천·크루 일정 질문 입력 시 `POST /api/v1/kkumz/sites/{siteSlug}/concierge`가 해당 브랜드의 명세서·상품·아티클 데이터를 기반으로 즉시 답변

---

## Part 2. 운영자 어드민 백오피스 (`services/kkumz/admin/`)

### 2.1 `SCR-KZ-01`: 운영 종합 대시보드 (`/dashboard/analysis`)
- **권한 코드**: `perm-kkumz-sys-002` (`role_admin`, `role_moderator`)
- **주요 구성**:
  1. **상단 핵심 KPI 카드 4종**: 누적 창업 아이디어 수(`totalIdeas`), 라이브 배포 사이트 수(`publishedSites` / `totalSites`), 샌드박스 빌드 성공률(`successfulBuilds` / `totalBuilds`), 미처리 모더레이션 신고 건수(`pendingReports`)
  2. **최근 생성된 창업 아이디어 요약 테이블**: 브랜드명, 사이트 유형(`COMMERCE`/`CMS_SITE`/`HYBRID`), 진행 단계(`Step 1~6`), 생성일시
  3. **멀티사이트 배포 및 빌드 상태 요약 패널**: 배포 모드별 비율(`DYNAMIC_SDUI` vs `CUSTOM_BUNDLE`), 최근 빌드 잡 상태 및 미처리 신고 바로가기
- **연동 API**: `GET /api/v1/kkumz/stats`, `GET /api/v1/kkumz/ideas`, `GET /api/v1/kkumz/sites`, `GET /api/v1/kkumz/moderation/reports`

### 2.2 `SCR-KZ-02`: 아이디어 & 명세서 관리 (`/studio/ideas`)
- **권한 코드**: `perm-kkumz-dom-002` (`role_admin`)
- **주요 구성**:
  - **검색 필터 (`searchFormSchema`)**: 프로젝트/브랜드명 키워드, 사이트 유형(`COMMERCE`, `CMS_SITE`, `HYBRID`), 진행 상태(`DRAFT`, `SPEC_READY`, `DEPLOYED`)
  - **그리드 컬럼 (`columns`)**: 아이디어 ID, 프로젝트 제목, 선택된 브랜드명, 사이트 슬러그(`/s/{slug}`), 사이트 유형 태그, 현재 단계(`Step 1~6`), 상태 배지, 작성자, 생성일시
  - **행 액션 (`TableAction`)**: `명세서 & 로고 상세 (`IdeaDetailModal.vue` 팝업 — SVG 로고, 컬러 토큰, 마크다운 기능명세서 전문 및 버전 히스토리 열람)`, `삭제`
- **연동 API**: `GET /api/v1/kkumz/ideas`, `GET /api/v1/kkumz/ideas/{id}`, `DELETE /api/v1/kkumz/ideas/{id}`

### 2.3 `SCR-KZ-03`: AI 프롬프트 & 필터 관리 (`/studio/prompts`)
- **권한 코드**: `perm-kkumz-dom-003` (`role_admin`)
- **주요 구성**:
  - **그리드 컬럼**: 템플릿 ID, 단계 코드(`BRAND_NAMING_8X`, `VECTOR_SVG_LOGO_6X`, `FRS_SPEC_MARKDOWN`, `WEBIDE_AI_BUILD_FIXER`), 템플릿명, 타깃 모델(`model_name`), Temperature, 금지어 필터(`forbidden_words_json`), 활성 상태, 수정일시
  - **편집 모달 (`PromptModal.vue`)**: 시스템 프롬프트(`system_prompt`), 유저 프롬프트 템플릿(`user_prompt_template`), Temperature 슬라이더/입력, 상표권·금칙어 JSON 배열 편집 및 즉시 반영
- **연동 API**: `GET /api/v1/kkumz/prompts`, `POST /api/v1/kkumz/prompts`

### 2.4 `SCR-KZ-04`: 멀티사이트 & 도메인 관제 (`/deployment/sites`)
- **권한 코드**: `perm-kkumz-dom-011` (`role_admin`)
- **주요 구성**:
  - **검색 필터**: 브랜드명/슬러그 검색, 사이트 유형, 배포 모드(`DYNAMIC_SDUI`, `CUSTOM_BUNDLE`), 운영 상태(`PUBLISHED`, `SUSPENDED`)
  - **그리드 컬럼**: 사이트 ID, 브랜드명, 슬러그(`/s/{slug}`), 커스텀 서브도메인(`*.kkumz.io`), 사이트 유형, 배포 모드, 활성 릴리즈 버전(`v1`, `v2`), 매핑된 `shop_site_id` / `cms_site_id`, 방문수, 좋아요 수, 상태 배지
  - **행 액션 (`TableAction`)**:
    - `라이브 사이트 새창 열기 (/s/{siteSlug})`
    - 상태가 `PUBLISHED`일 때: **`사이트 강제 정지 (SUSPENDED 전환)`**
    - 상태가 `SUSPENDED`일 때: **`정상 운영 복구 (PUBLISHED 전환)`**
- **연동 API**: `GET /api/v1/kkumz/sites`, `PUT /api/v1/kkumz/sites/{id}/status`

### 2.5 `SCR-KZ-05`: 빌드 파이프라인 & 로그 (`/deployment/builds`)
- **권한 코드**: `perm-kkumz-dom-012` (`role_admin`)
- **주요 구성**:
  - **그리드 컬럼**: 빌드 잡 ID, 대상 사이트 브랜드명(`brand_name`), 슬러그(`site_slug`), 릴리즈 버전, 트리거 유형(`INITIAL_DEPLOY`, `WEBIDE_SAVE`, `AI_AUTO_FIX`), 빌드 상태(`SUCCESS`, `FAILED`, `BUILDING`), 소요 시간(`duration_ms`), 에러 요약, AI 패치 제안, 실행일시
  - **행 액션 (`TableAction`)**:
    - `빌드 콘솔 로그 상세 (`BuildLogModal.vue` — 전체 터미널 출력 및 에러 스택트레이스 조회)`
    - 상태가 `FAILED`인 빌드 행: **`AI 자동 패치 & 재빌드 트리거`** (`POST /api/v1/kkumz/sites/{siteSlug}/webide-ai-fix`)
- **연동 API**: `GET /api/v1/kkumz/builds`, `POST /api/v1/kkumz/sites/{siteSlug}/webide-ai-fix`

### 2.6 `SCR-KZ-06`: 프로젝트 & 팀빌딩 관리 (`/community/projects`)
- **권한 코드**: `perm-kkumz-dom-021` (`role_admin`, `role_moderator`)
- **주요 구성**:
  - **상단/하단 구성**: 창업 팀원 모집 공고(`kkumz_team_recruit`) 관리 테이블 및 프로젝트별 합류 팀원(`kkumz_team_member`) 현황 조회
  - **그리드 컬럼**: 공고 ID, 브랜드명(`brand_name`), 공고 제목, 모집 포지션(`FRONTEND`, `DESIGNER`, `MARKETER`, `MD`), 보상 조건(`reward_type`), 지원자 수, 공고 상태(`OPEN` / `CLOSED`), 등록일시
  - **행 액션**: `모집 마감 (CLOSED 전환)` / `모집 재오픈 (OPEN 전환)` 토글
- **연동 API**: `GET /api/v1/kkumz/recruits`, `PUT /api/v1/kkumz/recruits/{id}/status`, `GET /api/v1/kkumz/team-members`

### 2.7 `SCR-KZ-07`: 신고 심사 & 모더레이션 (`/community/moderation`)
- **권한 코드**: `perm-kkumz-dom-022` (`role_admin`, `role_moderator`)
- **주요 구성**:
  - **검색 필터**: 심사 상태(`PENDING` 심사대기, `RESOLVED` 조치완료, `DISMISSED` 기각), 신고 대상 유형(`SITE`, `COMMENT`, `RECRUIT`), 사유 코드(`COPYRIGHT`, `PHISHING`, `SPAM`, `INAPPROPRIATE`)
  - **그리드 컬럼**: 신고 ID, 대상 유형, 대상 ID(`target_id`), 사유 코드 태그, 신고 상세 내용, 신고자, 심사 상태 배지, 심사 결과 코멘트(`action_taken`), 처리자, 접수/처리일시
  - **행 액션 (`ModerationResolveModal.vue`)**:
    - 심사 처분 선택: `사이트 즉시 정지 및 조치 완료 (SUSPEND_SITE + RESOLVED)`, `경고 조치 완료 (RESOLVED)`, `혐의 없음 기각 (DISMISSED)`
    - `SUSPEND_SITE` 선택 시 백엔드 트랜잭션으로 해당 `kkumz_site.status`가 즉시 `SUSPENDED`로 변경됩니다.
- **연동 API**: `GET /api/v1/kkumz/moderation/reports`, `POST /api/v1/kkumz/moderation/reports/{id}/resolve`
