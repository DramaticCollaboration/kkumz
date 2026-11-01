# SCR-KZ-04. 멀티사이트 및 도메인 관제 화면 상세 명세서

## 1. 화면 기본 메타데이터
| 항목 | 내용 |
| :--- | :--- |
| **화면 ID** | `SCR-KZ-04` |
| **권한 ID (`sys_permission.id`)** | `perm-kkumz-dom-011` (상위 폴더: `perm-kkumz-dom-010` 멀티사이트 & 빌드 관제) |
| **라우트 경로 (`url`)** | `/deployment/sites` |
| **뷰 컴포넌트 (`component`)** | `services/kkumz/admin/src/views/deployment/sites/index.vue` |
| **분리 모듈 파일** | `sites.data.ts` |
| **인가 역할** | `role_admin` |

## 2. Vben 표준 컴포넌트 구성
1. **검색 폼 (`searchFormSchema` in `sites.data.ts`)**:
   - 브랜드명/슬러그 키워드, 사이트 유형(`COMMERCE`, `CMS_SITE`, `HYBRID`), 배포 모드(`DYNAMIC_SDUI`, `CUSTOM_BUNDLE`), 운영 상태(`PUBLISHED`, `SUSPENDED`)
2. **데이터 그리드 (`BasicTable` & `columns`)**:
   - 표시 컬럼: 사이트 ID, 브랜드명(`brandName`), 슬러그(`siteSlug`), 내부 배포 경로(`deployedUrl`), 와일드카드 도메인(`customDomain`), 사이트 유형, 배포 모드 배지, 활성 버전(`activeVersion`), 외부 엔진 매핑(`shopSiteId` / `cmsSiteId`), 누적 방문수(`visitsCount`), 좋아요 수(`likesCount`), 운영 상태(`status`)
   - 행 액션(`TableAction`):
     - **`라이브 사이트 열기`** (`/s/{siteSlug}` 새 탭 오픈)
     - 상태가 `PUBLISHED`인 경우: **`강제 정지 (SUSPENDED)`**
     - 상태가 `SUSPENDED`인 경우: **`정상 복구 (PUBLISHED)`**

## 3. 연동 REST API 명세
| 액션 | HTTP Method & URI | 설명 |
| :--- | :--- | :--- |
| 멀티사이트 목록 조회 | `GET /api/v1/kkumz/sites` | `kkumz_site` 및 최신 릴리즈 빌드 상태 통합 조회 |
| 사이트 운영 상태 변경 | `PUT /api/v1/kkumz/sites/{id}/status` | `{ status: 'PUBLISHED' \| 'SUSPENDED' }` 즉시 전환 |
