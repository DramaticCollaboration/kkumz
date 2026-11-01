# SCR-KZ-07. 신고 심사 및 모더레이션 화면 상세 명세서

## 1. 화면 기본 메타데이터
| 항목 | 내용 |
| :--- | :--- |
| **화면 ID** | `SCR-KZ-07` |
| **권한 ID (`sys_permission.id`)** | `perm-kkumz-dom-022` (상위 폴더: `perm-kkumz-dom-020` 커뮤니티 & 팀빌딩) |
| **라우트 경로 (`url`)** | `/community/moderation` |
| **뷰 컴포넌트 (`component`)** | `services/kkumz/admin/src/views/community/moderation/index.vue` |
| **분리 모듈 파일** | `moderation.data.ts`, `ModerationResolveModal.vue` |
| **인가 역할** | `role_admin`, `role_moderator` |

## 2. Vben 표준 컴포넌트 구성
1. **검색 폼 (`searchFormSchema` in `moderation.data.ts`)**:
   - 심사 상태(`PENDING` 심사대기, `RESOLVED` 조치완료, `DISMISSED` 기각), 대상 유형(`SITE`, `COMMENT`, `RECRUIT`), 신고 사유 코드(`COPYRIGHT`, `PHISHING`, `SPAM`, `INAPPROPRIATE`)
2. **데이터 그리드 (`BasicTable` & `columns`)**:
   - 표시 컬럼: 신고 ID(`id`), 대상 유형(`targetType`), 대상 ID(`targetId`), 신고 사유 태그(`reasonCode`), 신고 상세 내용(`description`), 신고자(`reporterName`), 심사 상태 배지(`status`), 조치 내역(`actionTaken`), 처리자(`resolvedBy`), 접수일시(`createdAt`), 처리일시(`resolvedAt`)
   - 행 액션(`TableAction`): **`심사 처분 실행`** (`ModerationResolveModal` 오픈)
3. **심사 처분 모달 (`ModerationResolveModal.vue` — `BasicModal` + `BasicForm`)**:
   - 심사 결과 선택(`RESOLVED` 조치 완료 / `DISMISSED` 혐의 없음 기각)
   - **`대상 멀티사이트 즉시 정지 (suspendSite)`** 스위치 (활성화 시 대상 `kkumz_site.status`를 `SUSPENDED`로 즉시 변경)
   - 조치 상세 코멘트(`actionTaken`) 입력 및 저장

## 3. 연동 REST API 명세
| 액션 | HTTP Method & URI | 설명 |
| :--- | :--- | :--- |
| 신고 접수 목록 조회 | `GET /api/v1/kkumz/moderation/reports` | `kkumz_moderation_report` 전체 목록(미처리 `PENDING` 우선 정렬) 조회 |
| 신고 심사 처분 확정 | `POST /api/v1/kkumz/moderation/reports/{id}/resolve` | 심사 상태 업데이트 및 `suspendSite = true` 시 대상 멀티사이트 즉시 정지(`SUSPENDED`) |
