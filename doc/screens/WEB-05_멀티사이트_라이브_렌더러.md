# WEB-05. 멀티사이트 라이브 렌더러 화면 상세 명세서

## 1. 화면 기본 메타데이터
| 항목 | 내용 |
| :--- | :--- |
| **화면 ID** | `WEB-05` |
| **화면명** | 멀티사이트 라이브 렌더러 |
| **라우트 경로** | `/s/:siteSlug` (와일드카드 서브도메인 `{siteSlug}.kkumz.io` 매핑) |
| **뷰 컴포넌트** | `services/kkumz/web/src/views/LiveSiteRendererView.vue` |
| **레이아웃 모드** | `independent` (독립 브랜드 헤더 + 동적 CSS 테마 토큰 + AI 브랜드 컨시어지) |
| **접근 권한** | 전체 일반 방문자 및 고객 (단, `status = 'SUSPENDED'` 사이트는 접근 차단 안내 표시) |

## 2. 화면 구성 요소 및 외부 엔진 실데이터 연동
1. **독립 브랜드 헤더 & 히어로 배너**:
   - 해당 브랜드의 벡터 SVG 로고(`logoSvg`), 브랜드명, 커스텀 도메인, 배포 릴리즈 버전 표시
   - `designTokens` (`primary`, `secondary`, `accent`, `surface`, `radius`)를 기반으로 전체 버튼·배지·배경 테마 동적 적용
   - `CUSTOM_BUNDLE` 모드인 경우 WebIDE의 `HeroSection.vue` 커스텀 공지 배너 및 `CustomStyles.css` 반영
2. **SyncShop 실시간 커머스 섹션 (`COMMERCE`, `HYBRID` 사이트)**:
   - `sync_shop.pms_product`에서 `site_id = shop_site_id`로 조회된 실제 판매 상품 그리드 렌더링
   - 상품 선택 후 수령인명(`receiverName`), 연락처(`receiverPhone`), 배송지(`receiverAddress`), 수량(`quantity`) 입력 → **`실제 주문 결제하기`** 실행 시 `sync_shop.oms_order` 및 `oms_order_item`에 주문 생성
3. **SyncCMS 브랜드 매거진 및 고객 리드 폼 섹션 (`CMS_SITE`, `HYBRID` 사이트)**:
   - `sync_cms.cms_content`에서 `site_id = cms_site_id`로 조회된 매거진 아티클 카드 및 상세 모달 열람
   - 사전예약 / 뉴스레터 / 크루 참가 신청 폼 제출 시 `sync_cms.cms_form_data`에 JSONB 데이터 실시간 저장
4. **AI 브랜드 컨시어지 플로팅 챗봇**:
   - 우측 하단 플로팅 버튼을 통해 열리며, 방문자가 상품 추천·가격·배송·크루 일정을 질문하면 해당 브랜드의 명세서·상품·아티클 컨텍스트 기반으로 즉시 응답

## 3. 연동 REST API 명세
| 액션 | HTTP Method & URI | 설명 |
| :--- | :--- | :--- |
| 라이브 사이트 데이터 로드 | `GET /api/v1/kkumz/sites/{siteSlug}/live` | 사이트 메타데이터 + 활성 릴리즈 + SyncShop 상품 + SyncCMS 아티클 + 모집 공고 통합 조회 |
| 커머스 실주문 생성 | `POST /api/v1/kkumz/sites/{siteSlug}/orders` | `sync_shop.oms_order` 실제 주문 생성 및 창업가 `COMMERCE_ORDER` 알림 발송 |
| CMS 리드 폼 제출 | `POST /api/v1/kkumz/sites/{siteSlug}/forms` | `sync_cms.cms_form_data` 폼 데이터 저장 및 창업가 `FORM_SUBMIT` 알림 발송 |
| AI 브랜드 컨시어지 상담 | `POST /api/v1/kkumz/sites/{siteSlug}/concierge` | 사이트 컨텍스트 기반 방문자 질의응답 |
