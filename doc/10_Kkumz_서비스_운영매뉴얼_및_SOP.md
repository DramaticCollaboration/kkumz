# DOC-KZ-10. Kkumz 서비스 운영매뉴얼 및 표준운영절차 (SOP)

- **문서 번호**: DOC-KZ-10
- **대상 독자**: 플랫폼 총괄관리자(`role_admin`), 커뮤니티 모더레이터(`role_moderator`), DevOps 엔지니어
- **기준 시스템**: Kkumz Admin Backoffice (`http://localhost:3108`)

---

## 1. 일일 운영 관제 루틴 (Daily Operations Checklist)

운영자 및 모더레이터는 매일 오전 업무 시작 시 **운영 종합 대시보드 (`SCR-KZ-01`, `/dashboard/analysis`)** 에 접속하여 아래 4대 지표를 점검합니다.

| 점검 순서 | 점검 항목 | 확인 화면 경로 | 정상 기준 | 이상 징후 발생 시 조치 SOP |
| :---: | :--- | :--- | :--- | :--- |
| **1** | **미처리 신고 건수 (`pendingReports`)** | `/community/moderation` | 24시간 이상 경과한 `PENDING` 건 0건 | **SOP-03 (신고 심사 및 악성 사이트 정지 SOP)** 즉시 수행 |
| **2** | **WebIDE 샌드박스 빌드 실패율** | `/deployment/builds` | 최근 24시간 빌드 성공률 95% 이상 | 반복 실패 사이트의 에러 로그(`BuildLogModal`) 확인 후 **AI 자동 패치 트리거** 또는 `WEBIDE_AI_BUILD_FIXER` 프롬프트 보강 |
| **3** | **멀티사이트 런타임 상태** | `/deployment/sites` | 정상 사이트 `PUBLISHED` 유지 | 외부 엔진(`shop_site_id`, `cms_site_id`) 연결 상태 및 도메인 라우팅 점검 |
| **4** | **AI 생성 품질 및 금지어 위반** | `/studio/ideas` & `/studio/prompts` | 상표권 침해·부적절 네이밍 0건 | **SOP-01 (AI 프롬프트 튜닝 및 금지어 필터 갱신 SOP)** 수행 |

---

## 2. 표준운영절차 (SOP) 상세 가이드

### 2.1 SOP-01: AI 프롬프트 튜닝 및 상표권·금지어 필터 긴급 차단 절차
1. **발생 상황**: 특정 유명 상표명을 도용한 브랜드 네이밍이 생성되거나, 기능명세서/SVG 로고의 출력 포맷을 개선해야 하는 경우
2. **조치 절차**:
   - 어드민 백오피스 좌측 메뉴에서 **AI 브랜드 스튜디오 > AI 프롬프트 & 필터 관리 (`/studio/prompts`)** 로 이동합니다.
   - 대상 단계 템플릿(`BRAND_NAMING_8X`, `VECTOR_SVG_LOGO_6X`, `FRS_SPEC_MARKDOWN`, `WEBIDE_AI_BUILD_FIXER`) 행의 **프롬프트 편집** 버튼을 클릭합니다.
   - `PromptModal` 팝업에서 `금지어 필터 (forbidden_words_json)` 배열에 차단할 키워드(예: `"샤넬짝퉁"`, `"공식몰사칭"`)를 추가하거나 `system_prompt` 지침 및 `temperature` 값을 조정합니다.
   - **저장** 버튼을 클릭하면 서버 재기동 없이 즉시 다음 스튜디오 생성 요청부터 변경된 프롬프트와 금지어 필터가 적용됩니다.

### 2.2 SOP-02: WebIDE 빌드 장애 복구 및 멀티사이트 운영 상태 제어 절차
1. **발생 상황**: 창업가 또는 팀 개발자가 WebIDE에서 커스텀 SFC 코드를 수정한 뒤 빌드가 `FAILED`되어 도움을 요청하거나, 특정 사이트를 점검/정지 상태로 전환해야 하는 경우
2. **빌드 장애 복구 절차 (`/deployment/builds`)**:
   - **멀티사이트 & 빌드 관제 > 빌드 파이프라인 & 로그 (`/deployment/builds`)** 에 접속하여 상태가 `FAILED`인 빌드 잡을 조회합니다.
   - **로그 상세** 버튼을 클릭하여 `BuildLogModal`에서 컴파일러 에러 원인(`error_summary`, `build_log`)과 `AI 패치 제안 (ai_fix_suggestion)`을 확인합니다.
   - 행 액션의 **`AI 자동 패치 & 재빌드`** 버튼을 클릭하면 백엔드가 자동으로 문법 오류를 교정한 차기 버전 릴리즈를 빌드·배포합니다.
3. **멀티사이트 강제 정지 및 복구 절차 (`/deployment/sites`)**:
   - **멀티사이트 & 도메인 관제 (`/deployment/sites`)** 에서 대상 사이트 행의 **`사이트 강제 정지`** 버튼을 클릭하면 상태가 즉시 `SUSPENDED`로 전환됩니다.
   - 소명 완료 후 재오픈 시 동일 행의 **`정상 운영 복구`** 버튼을 클릭하여 `PUBLISHED` 상태로 복원합니다.

### 2.3 SOP-03: 커뮤니티 신고 심사 및 원클릭 사이트 정지 절차
1. **발생 상황**: 쇼케이스 피드나 라이브 사이트에서 상표권 침해(`COPYRIGHT`), 결제 피싱(`PHISHING`), 스팸(`SPAM`), 허위 팀원 모집(`INAPPROPRIATE`) 신고가 접수되어 `status = 'PENDING'`으로 등록된 경우
2. **조치 절차**:
   - **커뮤니티 & 팀빌딩 > 신고 심사 & 모더레이션 (`/community/moderation`)** 화면으로 이동합니다.
   - 신고 대상(`target_type`, `target_id`)과 신고 상세 사유(`description`)를 확인하고, 필요한 경우 대상 사이트(`/s/{siteSlug}`)를 열람하여 사실관계를 검증합니다.
   - 해당 행의 **심사 처리** 버튼을 클릭하여 `ModerationResolveModal`을 엽니다:
     - **중대 위반 (피싱/상표권 침해/불법 사이트)**: 처리 상태를 `RESOLVED (조치 완료)`로 선택하고 **`대상 사이트 즉시 정지 (suspendSite = true)`** 옵션을 활성화한 뒤 조치 사유(`action_taken`)를 입력합니다. 저장 즉시 해당 멀티사이트는 `SUSPENDED` 상태로 차단됩니다.
     - **경미 위반 (단순 경고/댓글 정리)**: `RESOLVED` 선택 및 `suspendSite = false`로 경고 기록을 남깁니다.
     - **허위/오신고**: 처리 상태를 `DISMISSED (혐의 없음 기각)`로 선택하고 기각 사유를 기록합니다.
