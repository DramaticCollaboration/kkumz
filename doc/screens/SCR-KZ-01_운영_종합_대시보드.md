# SCR-KZ-01. 운영 종합 대시보드 화면 상세 명세서

## 1. 화면 기본 메타데이터
| 항목 | 내용 |
| :--- | :--- |
| **화면 ID** | `SCR-KZ-01` |
| **권한 ID (`sys_permission.id`)** | `perm-kkumz-sys-002` (상위 폴더: `perm-kkumz-sys-001` 대시보드) |
| **라우트 경로 (`url`)** | `/dashboard/analysis` |
| **뷰 컴포넌트 (`component`)** | `services/kkumz/admin/src/views/dashboard/analysis/index.vue` |
| **인가 역할** | `role_admin` (총괄관리자), `role_moderator` (커뮤니티 모더레이터) |

## 2. 화면 구성 요소 및 데이터 바인딩
1. **상단 핵심 KPI 요약 카드 (4종)**:
   - **누적 창업 아이디어 (`totalIdeas`)**: 생성된 `kkumz_idea` 총 건수 및 `DEPLOYED` 전환 건수
   - **라이브 멀티사이트 (`publishedSites` / `totalSites`)**: 현재 정상 운영 중인 사이트 수 및 정지(`SUSPENDED`) 사이트 수
   - **샌드박스 빌드 성공률 (`successfulBuilds` / `totalBuilds`)**: WebIDE 커스텀 번들 및 초기 배포 빌드 성공률(%) 및 실패 건수(`failedBuilds`)
   - **미처리 모더레이션 신고 (`pendingReports`)**: 즉각 심사가 필요한 `PENDING` 상태 신고 건수 및 오픈 팀빌딩 공고 수(`openRecruits`)
2. **최근 창업 아이디어 & 멀티사이트 현황 패널**:
   - 최근 등록된 아이디어의 브랜드명, 사이트 유형(`COMMERCE`/`CMS_SITE`/`HYBRID`), 현재 진행 단계(`Step 1~6`), 상태 배지 표시
   - 배포된 멀티사이트의 배포 모드(`DYNAMIC_SDUI` vs `CUSTOM_BUNDLE`), 방문수, 좋아요 수 요약
3. **신속 관제 바로가기**:
   - 미처리 신고 심사(`/community/moderation`), 빌드 파이프라인 로그(`/deployment/builds`), 멀티사이트 관제(`/deployment/sites`) 이동 링크

## 3. 연동 REST API 명세
| 액션 | HTTP Method & URI | 설명 |
| :--- | :--- | :--- |
| 종합 통계 조회 | `GET /api/v1/kkumz/stats` | 아이디어·사이트·빌드·팀빌딩·신고 건수 실시간 집계 |
| 최근 아이디어 조회 | `GET /api/v1/kkumz/ideas` | 최근 생성된 창업 아이디어 목록 조회 |
| 멀티사이트 현황 조회 | `GET /api/v1/kkumz/sites` | 배포 사이트 상태 및 트래픽 지표 조회 |
| 미처리 신고 조회 | `GET /api/v1/kkumz/moderation/reports` | `PENDING` 상태 신고 목록 조회 |
