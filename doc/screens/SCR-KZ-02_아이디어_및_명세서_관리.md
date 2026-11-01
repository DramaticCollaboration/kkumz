# SCR-KZ-02. 아이디어 및 명세서 관리 화면 상세 명세서

## 1. 화면 기본 메타데이터
| 항목 | 내용 |
| :--- | :--- |
| **화면 ID** | `SCR-KZ-02` |
| **권한 ID (`sys_permission.id`)** | `perm-kkumz-dom-002` (상위 폴더: `perm-kkumz-dom-001` AI 브랜드 스튜디오) |
| **라우트 경로 (`url`)** | `/studio/ideas` |
| **뷰 컴포넌트 (`component`)** | `services/kkumz/admin/src/views/studio/ideas/index.vue` |
| **분리 모듈 파일** | `ideas.data.ts`, `IdeaDetailModal.vue` |
| **인가 역할** | `role_admin` |

## 2. Vben 표준 컴포넌트 구성
1. **검색 폼 (`searchFormSchema` in `ideas.data.ts`)**:
   - 브랜드명/제목 검색어 입력, 사이트 유형(`COMMERCE`, `CMS_SITE`, `HYBRID`), 진행 상태(`DRAFT`, `SPEC_READY`, `DEPLOYED`) 필터
2. **데이터 그리드 (`BasicTable` & `columns`)**:
   - 표시 컬럼: 아이디어 ID, 프로젝트 제목(`title`), 확정 브랜드명(`selectedBrandName`), 슬러그(`selectedSiteSlug`), 사이트 유형 태그, 스튜디오 진행 단계(`currentStep`), 상태 배지(`status`), 소유자(`ownerName`), 생성일시(`createdAt`)
   - 행 액션(`TableAction`): **`상세 & 명세서 열람`** (`IdeaDetailModal` 오픈), **`삭제`** (확인 팝업 후 삭제)
3. **상세 모달 (`IdeaDetailModal.vue` — `BasicModal`)**:
   - 확정된 벡터 SVG 로고(`selectedLogoSvg`) 렌더링 및 5대 디자인 토큰(`designTokensJson`) 컬러 칩 표시
   - 마크다운 기능명세서(`specMarkdown`) 전문 및 버전 히스토리(`versions`), 화면 설계 블루프린트(`screenSpecs`) 탭 조회

## 3. 연동 REST API 명세
| 액션 | HTTP Method & URI | 설명 |
| :--- | :--- | :--- |
| 아이디어 목록 조회 | `GET /api/v1/kkumz/ideas` | 전체 창업 아이디어 원장 조회 |
| 아이디어 상세 조회 | `GET /api/v1/kkumz/ideas/{id}` | 단건 상세 + 네이밍/로고/화면/버전 스냅샷 통합 조회 |
| 아이디어 삭제 | `DELETE /api/v1/kkumz/ideas/{id}` | 아이디어 및 하위 생성 후보 레코드 삭제 |
