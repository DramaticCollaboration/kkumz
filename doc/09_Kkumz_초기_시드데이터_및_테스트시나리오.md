# DOC-KZ-09. Kkumz 초기 시드 데이터 및 테스트 시나리오

- **문서 번호**: DOC-KZ-09
- **대상 서비스**: `services/kkumz`
- **데이터베이스 원천**: `db/03_seed_system.sql`, `db/04_seed_domain.sql`, `db/05_sample_dev.sql`

---

## 1. 초기 시드 데이터 전체 현황

Kkumz는 클린 DB 초기화 직후 빈 화면 없이 모든 기능(6-Step 스튜디오, 버전 롤백, 2-Track 멀티사이트 렌더링, WebIDE 에러 복구, SyncShop 주문, SyncCMS 폼, 팀빌딩, 모더레이션)을 즉시 시연·검증할 수 있도록 풍성한 한국형 시드 데이터를 내장하고 있습니다.

### 1.1 4대 대표 창업 프로젝트 및 멀티사이트 시드 요약 (`db/05_sample_dev.sql`)

| 아이디어 ID / 사이트 ID | 브랜드명 (`brand_name`) | 슬러그 (`site_slug`) | 사이트 유형 (`site_type`) | 배포 모드 (`deploy_mode`) | 활성 버전 | 외부 엔진 매핑 (`shop_site_id` / `cms_site_id`) | 소유 창업가 (`owner_id`) |
| :--- | :--- | :--- | :---: | :---: | :---: | :--- | :--- |
| `idea-kkumz-001`<br>`site-kkumz-001` | **말차 아뜰리에 제주** | `/s/matcha-atelier`<br>`matcha-atelier.kkumz.io` | `COMMERCE` | `CUSTOM_BUNDLE` | `v2` (`v1`, `v2` 보유) | `shop_site_id = 'matcha-atelier'`<br>`cms_site_id = NULL` | `user_kkumz_soyeon`<br>(한소연 대표) |
| `idea-kkumz-002`<br>`site-kkumz-002` | **데스크 오빗** | `/s/desk-orbit`<br>`desk-orbit.kkumz.io` | `COMMERCE` | `DYNAMIC_SDUI` | `v2` (`v1`, `v2` + `v3 FAILED` 보유) | `shop_site_id = 'desk-orbit'`<br>`cms_site_id = NULL` | `user_kkumz_minjun`<br>(박민준 메이커) |
| `idea-kkumz-003`<br>`site-kkumz-003` | **서울 나이트 런 클럽** | `/s/seoul-run-club`<br>`seoul-run-club.kkumz.io` | `CMS_SITE` | `DYNAMIC_SDUI` | `v1` | `shop_site_id = NULL`<br>`cms_site_id = 1` | `user_kkumz_jihoon`<br>(정지훈 크루장) |
| `idea-kkumz-004`<br>`site-kkumz-004` | **빈 크로니클 서울** | `/s/bean-chronicle`<br>`bean-chronicle.kkumz.io` | `HYBRID` | `CUSTOM_BUNDLE` | `v1` | `shop_site_id = 'bean-chronicle'`<br>`cms_site_id = 2` | `user_kkumz_hajun`<br>(최하준 헤드로스터) |

### 1.2 테이블별 시드 레코드 구성 내역
- `kkumz_idea_version`: `idea-kkumz-001`의 `v1`(초안), `v2`(정기구독 고도화본) 등 버전 비교·롤백 시연용 스냅샷 5건
- `kkumz_name_suggestion`: 말차 아뜰리에 제주 8종 후보(`name-kkumz-001` ~ `008`) 및 데스크 오빗 후보
- `kkumz_logo_suggestion`: 순수 벡터 SVG 로고 6종 후보(`logo-kkumz-001` ~ `006`) 및 사이트별 대표 SVG 로고
- `kkumz_screen_spec`: 사이트별 홈 랜딩, 정기구독/상품 카탈로그, 스토리 화면 설계 블루프린트 7건
- `kkumz_site_release` & `kkumz_build_job`:
  - 정상 빌드 완료 릴리즈 5건 (`SUCCESS`)
  - **AI 에러 자동 패치 시연용 실패 빌드 1건 (`rel-kkumz-002-v3`, `build-kkumz-004`, 상태 `FAILED` — `HeroSection.vue` 닫는 `</template>` 태그 누락 오류 내장)**
- `kkumz_team_recruit` & `kkumz_team_member`:
  - 포지션별 팀원 모집 공고 5건 (`FRONTEND`, `DESIGNER`, `MARKETER`, `MD`)
  - 프로젝트 팀원 및 승인 대기(`PENDING`) 지원자 7건
- `kkumz_moderation_report`: 심사 완료(`RESOLVED`) 1건 및 **모더레이터 즉시 심사 시연용 대기(`PENDING`) 2건 (`rep-kkumz-001`, `rep-kkumz-002`)**

---

## 2. 엔드투엔드(E2E) 핵심 인수 테스트 시나리오

### 시나리오 TC-01: 6-Step AI 스튜디오 신규 아이디어 생성 및 기능명세서 버전 롤백 검증
1. **사전 조건**: `web/` (`http://localhost:3008/studio/idea-kkumz-001`) 접속
2. **테스트 절차**:
   - Step 2 탭에서 생성된 8종의 브랜드 네이밍 후보와 `/s/matcha-atelier` 슬러그 가용성(`사용 가능`) 배지를 확인한다.
   - Step 3 탭에서 6종의 벡터 SVG 로고 카드를 클릭할 때 우측 미니 프리뷰의 로고와 테마 컬러(`primary`, `secondary`, `accent`)가 즉시 변경되는지 확인한다.
   - Step 4 탭에서 버전 히스토리 목록(`v1`, `v2`)을 확인하고, `v1 (초기 아이디어 기반 기본 커머스 기능명세서 초안 생성)`의 **`이 버전으로 롤백`** 버튼을 클릭한다.
3. **기대 결과**:
   - `POST /api/v1/kkumz/ideas/idea-kkumz-001/rollback` 응답이 `200 OK`를 반환하며, 마크다운 에디터 본문이 `v1` 스냅샷 내용으로 즉시 복원된다.

### 시나리오 TC-02: WebIDE 빌드 실패(`FAILED`) 감지 및 AI 에러 자동 패치(`AI_AUTO_FIX`) 검증
1. **사전 조건**: 데스크 오빗(`site-kkumz-002`, `/s/desk-orbit`)의 `v3` 릴리즈(`rel-kkumz-002-v3`)는 `HeroSection.vue`의 닫는 `</template>` 태그 누락으로 인해 `build_status = 'FAILED'` 상태로 시드되어 있음
2. **테스트 절차**:
   - `web/` 스튜디오 Step 6 (`/studio/idea-kkumz-002`) 또는 `admin/` 빌드 파이프라인 화면(`/deployment/builds`)에서 `FAILED` 상태의 빌드 로그(`Unclosed element <section> before end of template`)를 확인한다.
   - **`AI 에러 자동 패치 실행`** 버튼을 클릭하여 `POST /api/v1/kkumz/sites/desk-orbit/webide-ai-fix`를 호출한다.
3. **기대 결과**:
   - 백엔드가 깨진 `<template>` 태그를 자동 복구하여 신규 버전(`v4`) 릴리즈(`build_status = 'SUCCESS'`)와 `trigger_type = 'AI_AUTO_FIX'` 빌드 잡을 생성하고, `desk-orbit` 사이트의 `active_version`이 `4`로 갱신된다.

### 시나리오 TC-03: 라이브 멀티사이트(`/s/:siteSlug`) SyncShop 실주문 및 SyncCMS 리드 폼 연동 검증
1. **테스트 절차 (커머스 주문)**:
   - `http://localhost:3008/s/matcha-atelier` 접속 후 노출된 제주 유기농 첫물 세레모니얼 말차 상품을 선택하고 수령인명·연락처·주소를 입력해 **주문 결제하기**를 실행한다.
2. **기대 결과 (커머스 주문)**:
   - `POST /api/v1/kkumz/sites/matcha-atelier/orders`가 호출되어 `sync_shop.oms_order`에 신규 주문번호(`KKUMZ-...`)가 생성되고, 상단 GNB 알림 센터에 `COMMERCE_ORDER` 실시간 알림이 추가된다.
3. **테스트 절차 (CMS 리드 폼 & AI 컨시어지)**:
   - `http://localhost:3008/s/seoul-run-club` 접속 후 야간 러닝 코스 매거진 아티클을 열람하고, 하단 크루 참가 신청 폼을 제출한다.
   - 우측 하단 **AI 브랜드 컨시어지** 창을 열어 `"이번 주 러닝 코스와 신청 방법이 궁금해요"`를 입력한다.
4. **기대 결과**:
   - `sync_cms.cms_form_data`에 신청자 JSONB 데이터가 저장되며, AI 컨시어지가 서울 나이트 런 클럽의 아티클 및 크루 정보를 바탕으로 맞춤형 답변을 반환한다.

### 시나리오 TC-04: 팀빌딩 라운지 지원 → 리더 승인 및 어드민 신고 모더레이션 정지 처분 검증
1. **테스트 절차 (팀빌딩)**:
   - `/lounge`에서 모집 공고에 **합류 지원하기**(`POST /api/v1/kkumz/recruits/{id}/apply`) 실행 후, `/workspace`의 팀원 관리 탭에서 해당 지원자(`PENDING`)를 **합류 승인**(`POST /api/v1/kkumz/team-members/{id}/approve`)한다.
2. **테스트 절차 (어드민 모더레이션)**:
   - 어드민 백오피스(`http://localhost:3108/community/moderation`)에서 대기 중인 신고 건(`rep-kkumz-001`)의 **심사 처리** 모달을 열고 `사이트 즉시 정지 (suspendSite: true)`로 처분한다.
3. **기대 결과**:
   - 신고 상태가 `RESOLVED`로 변경됨과 동시에 대상 사이트의 `status`가 `SUSPENDED`로 전환되며, `/deployment/sites` 화면에서 즉시 정지 상태를 확인하고 다시 `PUBLISHED`로 복구할 수 있다.
