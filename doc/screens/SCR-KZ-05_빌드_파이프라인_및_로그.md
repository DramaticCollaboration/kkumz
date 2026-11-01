# SCR-KZ-05. 빌드 파이프라인 및 로그 화면 상세 명세서

## 1. 화면 기본 메타데이터
| 항목 | 내용 |
| :--- | :--- |
| **화면 ID** | `SCR-KZ-05` |
| **권한 ID (`sys_permission.id`)** | `perm-kkumz-dom-012` (상위 폴더: `perm-kkumz-dom-010` 멀티사이트 & 빌드 관제) |
| **라우트 경로 (`url`)** | `/deployment/builds` |
| **뷰 컴포넌트 (`component`)** | `services/kkumz/admin/src/views/deployment/builds/index.vue` |
| **분리 모듈 파일** | `builds.data.ts`, `BuildLogModal.vue` |
| **인가 역할** | `role_admin` |

## 2. Vben 표준 컴포넌트 구성
1. **검색 폼 (`searchFormSchema` in `builds.data.ts`)**:
   - 사이트 슬러그/브랜드명 검색, 트리거 유형(`INITIAL_DEPLOY`, `WEBIDE_SAVE`, `AI_AUTO_FIX`), 빌드 상태(`SUCCESS`, `FAILED`, `BUILDING`)
2. **데이터 그리드 (`BasicTable` & `columns`)**:
   - 표시 컬럼: 빌드 잡 ID(`id`), 브랜드명(`brandName`), 사이트 슬러그(`siteSlug`), 릴리즈 버전(`versionNo`), 트리거 유형 태그(`triggerType`), 빌드 상태 배지(`status`), 소요 시간(`durationMs`), 에러 요약(`errorSummary`), AI 패치 제안(`aiFixSuggestion`), 실행일시(`createdAt`)
   - 행 액션(`TableAction`):
     - **`빌드 콘솔 로그 상세`** (`BuildLogModal` 오픈)
     - 빌드 상태가 `FAILED`인 행: **`AI 자동 패치 & 재빌드`** 실행 버튼
3. **빌드 로그 모달 (`BuildLogModal.vue` — `BasicModal`)**:
   - 다크 터미널 스타일 콘솔 뷰어에 전체 `buildLog`, `errorSummary`, `aiFixSuggestion` 출력 및 원클릭 AI 복구 트리거 제공

## 3. 연동 REST API 명세
| 액션 | HTTP Method & URI | 설명 |
| :--- | :--- | :--- |
| 빌드 파이프라인 이력 조회 | `GET /api/v1/kkumz/builds` | `kkumz_build_job` + `kkumz_site` 조인 이력 조회 |
| AI 빌드 에러 자동 패치 | `POST /api/v1/kkumz/sites/{siteSlug}/webide-ai-fix` | 실패한 WebIDE SFC 코드를 자동 패치하여 차기 버전(`SUCCESS`)으로 재배포 |
