# WEB-01. 쇼케이스 피드 화면 상세 명세서

## 1. 화면 기본 메타데이터
| 항목 | 내용 |
| :--- | :--- |
| **화면 ID** | `WEB-01` |
| **화면명** | 쇼케이스 피드 |
| **라우트 경로** | `/feed` (루트 `/` 접속 시 자동 리다이렉트) |
| **뷰 컴포넌트** | `services/kkumz/web/src/views/FeedView.vue` |
| **레이아웃 모드** | `community` (상단 1단 글로벌 내비게이션 바 + 본문 그리드) |
| **접근 권한** | 전체 사용자 및 창업가 (`role_creator`, `role_developer`, 방문자) |

## 2. 화면 구성 요소 및 데이터 바인딩
1. **상단 히어로 섹션**:
   - 플랫폼 슬로건 및 실시간 배포 멀티사이트 수, 누적 방문수 집계 표시
   - 주요 CTA: `+ 5분 만에 내 브랜드 사이트 런칭하기` (`/studio` 이동)
2. **사이트 유형 필터 바**:
   - `ALL (전체)`, `COMMERCE (SyncShop 커머스)`, `CMS_SITE (SyncCMS 브랜드·매거진)`, `HYBRID (커머스+CMS 복합)`
3. **쇼케이스 멀티사이트 카드 그리드**:
   - **브랜드 헤더**: 벡터 SVG 로고(`selectedLogoSvg`), 브랜드명(`brandName`), 소유자(`ownerName`), 배포 모드 배지(`DYNAMIC_SDUI` / `CUSTOM_BUNDLE`), 활성 버전(`activeVersion`)
   - **본문 요약**: 한 줄 요약(`summary`), 타깃 고객(`targetAudience`), 커스텀 도메인(`customDomain`)
   - **팀원 모집 배지**: 현재 오픈된 팀원 모집 포지션(`openRecruits`: `FRONTEND`, `DESIGNER`, `MARKETER`, `MD`)
   - **하단 인터랙션 바**:
     - `좋아요 (likesCount)` 토글 버튼
     - `피드백 댓글 (commentsCount)` 모달/드로어 열기 버튼
     - `팀 합류 지원` (`/lounge/:siteSlug` 이동)
     - `신고하기` (모더레이션 신고 모달 오픈)
     - **`라이브 사이트 방문`** (`/s/:siteSlug` 이동)

## 3. 연동 REST API 명세
| 액션 | HTTP Method & URI | 설명 |
| :--- | :--- | :--- |
| 피드 목록 조회 | `GET /api/v1/kkumz/feed` | 배포된 멀티사이트(`kkumz_site`) + 아이디어 요약 + 오픈 모집 공고 조회 |
| 좋아요 토글 | `POST /api/v1/kkumz/feed/{siteId}/like` | `kkumz_feed_like` 등록/해제 및 `kkumz_site.likes_count` 증감 |
| 댓글 목록 조회 | `GET /api/v1/kkumz/feed/{siteId}/comments` | 해당 사이트의 `kkumz_feed_comment` 시간순 조회 |
| 댓글 작성 | `POST /api/v1/kkumz/feed/{siteId}/comments` | 신규 피드백 댓글 저장 |
| 부적절 사이트 신고 | `POST /api/v1/kkumz/moderation/reports` | `kkumz_moderation_report`에 `PENDING` 상태로 신고 접수 |
