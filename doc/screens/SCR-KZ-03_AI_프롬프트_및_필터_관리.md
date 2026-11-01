# SCR-KZ-03. AI 프롬프트 및 필터 관리 화면 상세 명세서

## 1. 화면 기본 메타데이터
| 항목 | 내용 |
| :--- | :--- |
| **화면 ID** | `SCR-KZ-03` |
| **권한 ID (`sys_permission.id`)** | `perm-kkumz-dom-003` (상위 폴더: `perm-kkumz-dom-001` AI 브랜드 스튜디오) |
| **라우트 경로 (`url`)** | `/studio/prompts` |
| **뷰 컴포넌트 (`component`)** | `services/kkumz/admin/src/views/studio/prompts/index.vue` |
| **분리 모듈 파일** | `prompts.data.ts`, `PromptModal.vue` |
| **인가 역할** | `role_admin` |

## 2. Vben 표준 컴포넌트 구성
1. **데이터 그리드 (`BasicTable` & `columns` in `prompts.data.ts`)**:
   - 관리 대상 4대 템플릿(`kkumz_ai_prompt_template`):
     1. `BRAND_NAMING_8X` (`tpl-kkumz-001`): Step 2 AI 브랜드 네이밍 8종 및 슬러그 생성기
     2. `VECTOR_SVG_LOGO_6X` (`tpl-kkumz-002`): Step 3 AI 순수 벡터 SVG 로고 6종 및 컬러 토큰 생성기
     3. `FRS_SPEC_MARKDOWN` (`tpl-kkumz-003`): Step 4 AI 서비스 기능명세서(Markdown) 생성기
     4. `WEBIDE_AI_BUILD_FIXER` (`tpl-kkumz-004`): Step 6 WebIDE 샌드박스 빌드 에러 AI 자동 패치 에이전트
   - 표시 컬럼: 템플릿 ID, 단계 코드(`stepCode`), 템플릿명(`templateName`), 모델명(`modelName`), `temperature`, 금지어 필터(`forbiddenWordsJson`), 상태(`status`), 최종 수정일시(`updatedAt`)
   - 행 액션(`TableAction`): **`프롬프트 및 금지어 편집`** (`PromptModal` 오픈)
2. **편집 모달 (`PromptModal.vue` — `BasicModal` + `BasicForm`)**:
   - 템플릿명, 타깃 LLM 모델명(`claude-sonnet-4-6` 등), `temperature` (`0.00` ~ `1.00`), 시스템 프롬프트(`systemPrompt`), 유저 프롬프트 템플릿(`userPromptTemplate`), 금지어 JSON 배열(`forbiddenWordsJson`), 활성 상태(`ACTIVE`/`INACTIVE`) 수정 및 저장

## 3. 연동 REST API 명세
| 액션 | HTTP Method & URI | 설명 |
| :--- | :--- | :--- |
| 프롬프트 템플릿 목록 조회 | `GET /api/v1/kkumz/prompts` | `kkumz_ai_prompt_template` 전체 목록 조회 |
| 프롬프트 및 금지어 수정 | `POST /api/v1/kkumz/prompts` | 시스템/유저 프롬프트, Temperature, 금지어 목록 즉시 업데이트 |
