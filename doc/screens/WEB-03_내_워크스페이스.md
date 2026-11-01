# WEB-03. 내 워크스페이스 화면 상세 명세서

## 1. 화면 기본 메타데이터
| 항목 | 내용 |
| :--- | :--- |
| **화면 ID** | `WEB-03` |
| **화면명** | 내 워크스페이스 |
| **라우트 경로** | `/workspace` |
| **뷰 컴포넌트** | `services/kkumz/web/src/views/WorkspaceView.vue` |
| **레이아웃 모드** | `community` (상단 1단 글로벌 내비게이션 바) |
| **접근 권한** | 창업가 메이커(`role_creator`) 및 프로젝트 팀원(`OWNER`, `DEVELOPER`, `EDITOR`, `MD`) |

## 2. 화면 구성 요소 및 데이터 바인딩
1. **상단 요약 KPI 카드 (4종)**:
   - 내 창업 아이디어 수, 운영 중인 라이브 멀티사이트 수, 누적 방문·좋아요 합계, 승인 대기 중인 팀원 지원자 수(`PENDING`)
2. **내 창업 아이디어 & 스튜디오 진행 목록 (`kkumz_idea`)**:
   - 아이디어 제목, 확정 브랜드명(`selectedBrandName`), 벡터 SVG 로고 미리보기, 사이트 유형(`COMMERCE`, `CMS_SITE`, `HYBRID`), 현재 단계(`Step 1` ~ `Step 6`), 상태(`DRAFT`, `SPEC_READY`, `DEPLOYED`)
   - 액션 버튼: `6-Step 스튜디오 편집 (/studio/:ideaId)`, 배포 완료 시 `라이브 사이트 열기 (/s/:siteSlug)`
3. **배포된 멀티사이트 운영 현황 (`kkumz_site`)**:
   - 사이트 슬러그, 커스텀 도메인(`*.kkumz.io`), 배포 모드(`DYNAMIC_SDUI` / `CUSTOM_BUNDLE`), 활성 버전(`v1`, `v2`), 매핑된 외부 테넌트(`shopSiteId`, `cmsSiteId`), 운영 상태(`PUBLISHED` / `SUSPENDED`)
4. **프로젝트 팀원 및 지원자 승인 관리 (`kkumz_team_member`)**:
   - 소속 브랜드명, 팀원/지원자 성명(`userName`), 프로젝트 역할(`OWNER`, `DEVELOPER`, `EDITOR`, `MD`), 지원 동기(`motivation`), 상태(`ACTIVE` / `PENDING` / `REJECTED`)
   - `PENDING` 상태 행 액션: **`합류 승인 (ACTIVE 전환)`**, **`반려 (REJECTED 전환)`**

## 3. 연동 REST API 명세
| 액션 | HTTP Method & URI | 설명 |
| :--- | :--- | :--- |
| 아이디어 목록 조회 | `GET /api/v1/kkumz/ideas` | 생성된 창업 아이디어 목록 조회 |
| 멀티사이트 목록 조회 | `GET /api/v1/kkumz/sites` | 배포된 멀티사이트 및 릴리즈 정보 조회 |
| 팀원 및 지원자 조회 | `GET /api/v1/kkumz/team-members` | 프로젝트별 팀원 및 합류 대기 지원자 목록 조회 |
| 지원자 승인 / 반려 | `POST /api/v1/kkumz/team-members/{id}/approve` | `{ status: 'ACTIVE' \| 'REJECTED' }` 상태 업데이트 |
