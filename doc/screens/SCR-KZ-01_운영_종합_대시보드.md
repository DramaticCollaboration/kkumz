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
1. **상단 핵심 KPI 요약 카드 (6종)**:
   - **누적 아이디어 프로젝트 (`totalIdeas` / `publishedIdeas`)**: 생성된 `kkumz_idea` 총 건수, `DEPLOYED` 전환 건수 및 화면 설계 블루프린트 누적 건수(`screenBlueprintCount`)
   - **가동 중 멀티사이트 (`publishedSites` / `totalSites`)**: 현재 정상 운영 중인 사이트 수, 정지(`SUSPENDED`) 사이트 수, 누적 방문수(`totalViews`) 및 좋아요 수(`totalLikes`)
   - **WebIDE 빌드 성공률 (`buildSuccessRate` / `totalBuilds`)**: WebIDE 커스텀 번들 및 초기 배포 빌드 성공률(%), 성공/실패 건수(`successfulBuilds` / `failedBuilds`), AI 자동 패치 복구 건수(`aiPatchedBuilds`)
   - **오픈 팀빌딩 포지션 (`openRecruits` / `activeTeamMembers`)**: 현재 모집 중인 팀원 공고 수, 활성 합류 팀원 수(`activeTeamMembers`), 승인 대기 지원자 수(`pendingApplicants`)
   - **AI 프롬프트 누적 호출 (`totalAiPromptCalls`)**: 브랜드 네이밍(`namingCount`), 벡터 SVG 로고(`logoCount`), 기능명세서 버전(`specVersionCount`), 블루프린트 및 AI 빌드 패치 누적 호출 집계
   - **미처리 모더레이션 신고 (`pendingModerationCount` / `totalReports`)**: 즉각 심사가 필요한 `PENDING` 상태 신고 건수 및 누적 접수 건수
2. **중단 관제 현황 카드 (3종)**:
   - **멀티사이트 유형별 분포 및 배포 아키텍처**: `siteTypeDistribution`(`COMMERCE`, `CMS_SITE`, `HYBRID`) 비율 프로그레스 바·트래픽 지표 및 2-Track 하이브리드 배포 모드(`DYNAMIC_SDUI` vs `CUSTOM_BUNDLE`) 현황, 주요 관제 화면 바로가기 버튼
   - **최근 WebIDE 빌드 & 배포 파이프라인 (`recentBuildJobs`)**: 최근 실행된 샌드박스 빌드 잡의 상태(`SUCCESS` / `FAILED`), 브랜드명, 릴리즈 버전(`versionNo`), 트리거 유형, 소요 시간(`durationMs`), AI 자동 패치 및 에러 요약 표시
   - **최근 모더레이션 신고 접수 큐 (`recentReports`)**: 접수된 신고의 사유 코드(`COPYRIGHT`, `PHISHING`, `SPAM`, `INAPPROPRIATE`), 심사 상태(`PENDING` / `RESOLVED` / `DISMISSED`), 대상 사이트 브랜드명, 상세 사유 및 처리 결과 표시
3. **하단 실데이터 상세 패널 (2종)**:
   - **라이브 멀티사이트 운영 및 트래픽 현황 (`topSites`)**: 배포된 멀티사이트의 벡터 SVG 로고, 브랜드명, 슬러그(`/s/{siteSlug}`), 사이트 유형, 배포 모드, 방문·좋아요·댓글 지표, 운영 상태 배지 및 새창 열기 링크
   - **최근 AI 스튜디오 아이디어 & 기능명세서 현황 (`recentIdeas`)**: 창업 아이디어별 진행 단계(`Step 1~6`), 확정 브랜드명, 사이트 유형, 상태 배지(`DEPLOYED`, `SPEC_READY`, `DRAFT`) 및 작성자 정보

## 3. 연동 REST API 명세
| 액션 | HTTP Method & URI | 설명 |
| :--- | :--- | :--- |
| 어드민 운영 종합 대시보드 조회 | `GET /api/v1/kkumz/admin/dashboard/summary` | 6대 KPI 지표, 멀티사이트 유형·배포모드 분포, 최근 빌드 잡, 최근 신고 큐, 라이브 멀티사이트 및 최근 아이디어 일괄 조회 |
| 종합 통계 조회 | `GET /api/v1/kkumz/stats` | 아이디어·사이트·빌드·팀빌딩·신고 건수 실시간 집계 |
| 최근 아이디어 조회 | `GET /api/v1/kkumz/ideas` | 최근 생성된 창업 아이디어 목록 조회 |
| 멀티사이트 현황 조회 | `GET /api/v1/kkumz/sites` | 배포 사이트 상태 및 트래픽 지표 조회 |
| 미처리 신고 조회 | `GET /api/v1/kkumz/moderation/reports` | `PENDING` 상태 신고 목록 조회 |
