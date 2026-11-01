# SCR-KZ-06. 프로젝트 및 팀빌딩 관리 화면 상세 명세서

## 1. 화면 기본 메타데이터
| 항목 | 내용 |
| :--- | :--- |
| **화면 ID** | `SCR-KZ-06` |
| **권한 ID (`sys_permission.id`)** | `perm-kkumz-dom-021` (상위 폴더: `perm-kkumz-dom-020` 커뮤니티 & 팀빌딩) |
| **라우트 경로 (`url`)** | `/community/projects` |
| **뷰 컴포넌트 (`component`)** | `services/kkumz/admin/src/views/community/projects/index.vue` |
| **분리 모듈 파일** | `projects.data.ts` |
| **인가 역할** | `role_admin`, `role_moderator` |

## 2. Vben 표준 컴포넌트 구성
1. **검색 폼 (`searchFormSchema` in `projects.data.ts`)**:
   - 브랜드명/공고 제목 검색, 모집 포지션(`FRONTEND`, `DESIGNER`, `MARKETER`, `MD`), 공고 상태(`OPEN`, `CLOSED`)
2. **데이터 그리드 (`BasicTable` & `columns`)**:
   - 표시 컬럼: 모집 공고 ID(`id`), 소속 브랜드명(`brandName`), 사이트 슬러그(`siteSlug`), 공고 제목(`title`), 모집 포지션 배지(`roleNeeded`), 보상/지분 조건(`rewardType`), 지원자 수(`applicantsCount`), 상태(`status`), 등록일시(`createdAt`)
   - 행 액션(`TableAction`):
     - 공고 상태가 `OPEN`일 때: **`모집 강제 마감 (CLOSED)`**
     - 공고 상태가 `CLOSED`일 때: **`모집 재오픈 (OPEN)`**
     - 프로젝트 소속 팀원 및 지원자 현황(`kkumz_team_member`) 확인

## 3. 연동 REST API 명세
| 액션 | HTTP Method & URI | 설명 |
| :--- | :--- | :--- |
| 팀원 모집 공고 목록 조회 | `GET /api/v1/kkumz/recruits` | 전체 프로젝트 팀원 모집 공고 조회 |
| 모집 공고 상태 변경 | `PUT /api/v1/kkumz/recruits/{id}/status` | `{ status: 'OPEN' \| 'CLOSED' }` 전환 |
| 팀원 및 지원자 목록 조회 | `GET /api/v1/kkumz/team-members` | 프로젝트별 팀원(`OWNER`, `DEVELOPER`, `EDITOR`, `MD`) 및 지원자 조회 |
