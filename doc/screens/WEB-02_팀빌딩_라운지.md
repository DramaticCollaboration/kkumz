# WEB-02. 팀빌딩 라운지 화면 상세 명세서

## 1. 화면 기본 메타데이터
| 항목 | 내용 |
| :--- | :--- |
| **화면 ID** | `WEB-02` |
| **화면명** | 팀빌딩 라운지 |
| **라우트 경로** | `/lounge/:siteSlug?` |
| **뷰 컴포넌트** | `services/kkumz/web/src/views/ProjectLoungeView.vue` |
| **레이아웃 모드** | `community` (상단 1단 글로벌 내비게이션 바) |
| **접근 권한** | 창업가 메이커(`role_creator`), 협업 개발자(`role_developer`) 및 예비 팀원 |

## 2. 화면 구성 요소 및 데이터 바인딩
1. **상단 포지션 필터 및 공고 등록 영역**:
   - 포지션 필터 탭: `ALL`, `FRONTEND (프론트엔드 개발자)`, `DESIGNER (브랜드/프로덕트 디자이너)`, `MARKETER (그로스 마케터)`, `MD (커머스 MD)`
   - 사이트 슬러그 파라미터(`:siteSlug`) 전달 시 해당 프로젝트 공고만 자동 필터링
   - 우측 상단 액션: `+ 새 팀원 모집 공고 등록` 모달 오픈
2. **모집 공고 카드 리스트 (`kkumz_team_recruit`)**:
   - 소속 브랜드 SVG 로고, 브랜드명(`brandName`), 사이트 슬러그(`/s/:siteSlug` 링크)
   - 모집 포지션 태그(`roleNeeded`), 보상 및 지분 조건(`rewardType`), 공고 상태(`OPEN` / `CLOSED`)
   - 공고 제목(`title`), 상세 업무 및 요구 역량(`description`), 현재 지원자 수(`applicantsCount`)
   - **`합류 지원하기`** 버튼 (클릭 시 지원 모달 팝업)
3. **서브 모달**:
   - **팀 합류 지원 모달**: 지원자 이름(`userName`), 지원 동기 및 포트폴리오 링크(`motivation`) 입력
   - **신규 공고 등록 모달**: 대상 사이트(`siteId`), 모집 포지션(`roleNeeded`), 공고 제목, 상세 설명, 보상 조건 입력

## 3. 연동 REST API 명세
| 액션 | HTTP Method & URI | 설명 |
| :--- | :--- | :--- |
| 모집 공고 목록 조회 | `GET /api/v1/kkumz/recruits` | 전체 팀원 모집 공고 및 브랜드 메타데이터 조회 |
| 신규 모집 공고 등록 | `POST /api/v1/kkumz/recruits` | `kkumz_team_recruit` 신규 레코드 생성 |
| 포지션 합류 지원 | `POST /api/v1/kkumz/recruits/{id}/apply` | `kkumz_team_member` (`status = 'PENDING'`) 생성, 지원자 수 증가, 프로젝트 리더에게 `TEAM_APPLY` 알림 발송 |
