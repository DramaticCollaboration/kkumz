# WEB-04. 6단계 AI 스튜디오 및 WebIDE 화면 상세 명세서

## 1. 화면 기본 메타데이터
| 항목 | 내용 |
| :--- | :--- |
| **화면 ID** | `WEB-04` |
| **화면명** | 집중형 6-Step AI 브랜드·웹사이트 스튜디오 및 내장 WebIDE |
| **라우트 경로** | `/studio/:ideaId?` |
| **뷰 컴포넌트** | `services/kkumz/web/src/views/StudioView.vue` |
| **레이아웃 모드** | `studio` (상단 GNB 숨김, 좌측 6-Step 수직 스테퍼 + 중앙 캔버스 + 우측 프리뷰/WebIDE) |
| **접근 권한** | 프로젝트 `OWNER`, `EDITOR`, `DEVELOPER` |

## 2. 6-Step 워크벤치 단계별 상세 기능
1. **Step 1. 아이디어 스케치**:
   - 입력 필드: 프로젝트 제목(`title`), 한 줄 핵심 요약(`summary`), 해결 문제(`problemStatement`), 타깃 고객(`targetAudience`), 사이트 유형(`COMMERCE`, `CMS_SITE`, `HYBRID`)
2. **Step 2. AI 브랜드 네이밍 (8종 후보 & 슬러그 중복 검증)**:
   - `AI 브랜드 네이밍 8종 생성` 버튼 클릭 시 한/영/혼합 브랜드명 8종, 태그라인, 네이밍 스토리, 추천 슬러그(`/s/{slug}`), 실시간 슬러그 가용성(`slugAvailable`) 카드 렌더링
   - 카드 선택 시 `selectedBrandName` 및 `selectedSiteSlug` 확정
3. **Step 3. AI 벡터 SVG 로고 (6종 후보 & 5대 디자인 토큰)**:
   - `AI 벡터 SVG 로고 6종 생성` 실행 시 순수 인라인 `<svg>` 로고 6종 및 컬러 팔레트(`primary`, `secondary`, `accent`, `surface`, `radius`) 표시
   - 선택 즉시 우측 실시간 미니 사이트 프리뷰에 로고 및 테마 컬러 반영
4. **Step 4. AI 마크다운 기능명세서 및 버전 히스토리 롤백**:
   - 마크다운 기능명세서 자동 생성 및 직접 편집기 제공
   - 버전 히스토리 타임라인(`kkumz_idea_version`: `v1`, `v2`...) 조회 및 **`이 버전으로 롤백`** 원클릭 복원
5. **Step 5. 화면 설계 블루프린트**:
   - 사이트 유형별 필수 화면(`kkumz_screen_spec`), 라우트 경로, 구성 섹션 블록(`sectionsJson`), SyncShop/SyncCMS 데이터 바인딩 명세 확인
6. **Step 6. 2-Track 하이브리드 배포 & 내장 WebIDE**:
   - 배포 모드 선택: `DYNAMIC_SDUI` (0초 즉시 배포) vs `CUSTOM_BUNDLE` (WebIDE 커스텀 번들)
   - **내장 WebIDE**: `HeroSection.vue`, `CustomStyles.css`, `site.config.json` 파일 편집 및 샌드박스 빌드 실행
   - **AI 에러 자동 패치**: 빌드 상태가 `FAILED`일 때 에러 로그 확인 및 **`AI 에러 자동 패치 실행`** 버튼으로 코드 자동 복구·재배포

## 3. 연동 REST API 명세
| 단계 | HTTP Method & URI | 설명 |
| :--- | :--- | :--- |
| Step 1 | `GET, POST, PUT /api/v1/kkumz/ideas/{id?}` | 아이디어 조회, 신규 생성, 기본 정보 업데이트 |
| Step 2 | `POST /api/v1/kkumz/ideas/{id}/generate-names`<br>`POST /api/v1/kkumz/ideas/{id}/select-name` | AI 네이밍 8종 생성 및 브랜드명·슬러그 확정 |
| Step 3 | `POST /api/v1/kkumz/ideas/{id}/generate-logos`<br>`POST /api/v1/kkumz/ideas/{id}/select-logo` | AI 벡터 SVG 로고 6종 생성 및 로고·컬러 토큰 확정 |
| Step 4 | `POST /api/v1/kkumz/ideas/{id}/generate-spec`<br>`GET /api/v1/kkumz/ideas/{id}/versions`<br>`POST /api/v1/kkumz/ideas/{id}/rollback` | AI 기능명세서 생성, 버전 목록 조회, 특정 버전으로 롤백 |
| Step 5 | `POST /api/v1/kkumz/ideas/{id}/generate-screens` | 화면 설계 블루프린트 생성 |
| Step 6 | `POST /api/v1/kkumz/ideas/{id}/deploy`<br>`POST /api/v1/kkumz/sites/{siteSlug}/webide-save`<br>`POST /api/v1/kkumz/sites/{siteSlug}/webide-ai-fix` | 멀티사이트 라이브 배포, WebIDE 코드 저장·빌드, AI 빌드 에러 자동 패치 |
