-- ============================================================================
-- Kkumz (꿈즈) — 05_sample_dev.sql
-- [DEV/TEST ONLY] 개발 및 시연 전용 샘플 픽스처 데이터
-- 무중복 결정론적 순수 INSERT INTO 구문만 사용
-- ============================================================================

INSERT INTO kkumz_idea (
    id, author_username, author_display_name, title, prompt_text, site_type, category,
    tags_json, visibility, current_step, selected_brand_name, selected_site_slug, selected_logo_svg,
    design_tokens_json, spec_markdown, screens_json, enabled_features_json, ai_intent_analysis_json, status, created_at, updated_at
) VALUES
(
    'idea-kkumz-001',
    'soyeon.jeju',
    '한소연 대표',
    '제주 서귀포 첫물 유기농 차광 말차 & 도예가 수제 다기 브랜드 스토어',
    '제주 서귀포 유기농 다원에서 재배한 첫물 세레모니얼 말차와 이천 도예가의 수제 백자 다완, 100본립 차선을 큐레이션하여 판매하는 프리미엄 다도 D2C 쇼핑몰을 만들고 싶어요.',
    'COMMERCE',
    'FOOD_BEVERAGE',
    '["제주유기농말차", "홈다도", "수제다완", "프리미엄기프트", "D2C브랜드"]'::jsonb,
    'PUBLIC',
    6,
    '말차 아뜰리에 제주',
    'matcha-atelier',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#14532d"/><circle cx="20" cy="20" r="11" fill="#86efac" fill-opacity="0.25"/><path d="M13 24C13 17 20 12 27 15C26 22 20 27 13 24Z" fill="#4ade80"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#14532d">MATCHA ATELIER</text></svg>',
    '{"primary":"#14532d","secondary":"#16a34a","accent":"#ca8a04","surface":"#f7fee7","radius":"14px","fontHeading":"Pretendard","fontBody":"Pretendard"}'::jsonb,
    '## 1. 브랜드 비전 및 핵심 가치
- **브랜드명**: 말차 아뜰리에 제주
- **핵심 슬로건**: 제주 화산암반수와 해풍이 길러낸 첫물 유기농 차광 말차 & 수제 다기 아뜰리에
- **타겟 고객**: 바쁜 일상 속 5분의 고요한 다도 리추얼을 추구하는 25~45세 프리미엄 라이프스타일 소비자

## 2. 핵심 스토어 기능 구성
1. **다도 큐레이션 히어로**: 첫물 세레모니얼 말차 틴캔 및 백자 다완 스타터 세트 스토리텔링 배너
2. **등급별 말차 카탈로그**: 세레모니얼 말차, 수제 다기 & 차선, 말차 디저트 기프트 카테고리 필터
3. **AI 티 소믈리에 컨시어지**: 취향(고소한 감칠맛 vs 산뜻한 청량감, 라떼용 vs 격불용) 기반 맞춤형 말차 추천
4. **원클릭 장바구니 및 선물 포장 결제**: 보자기 기프트 패키징 옵션 및 실시간 주문·재고 연동',
    '[
      {"screenKey":"HOME_HERO","title":"보태니컬 다도 히어로 섹션","layout":"EDITORIAL_BOTANICAL","components":["BrandStoryBanner","MatchaRitualSteps","FeaturedProductGrid"]},
      {"screenKey":"CATALOG","title":"말차 등급 및 다구 카탈로그","layout":"FILTERABLE_3COL_GRID","components":["CategoryTabs","TasteNoteBadge","QuickCartDrawer"]},
      {"screenKey":"CHECKOUT","title":"기프트 패키징 및 즉시 결제","layout":"ONE_PAGE_CHECKOUT","components":["GiftWrapSelector","OrderForm","AiTeaSommelierWidget"]}
    ]'::jsonb,
    '["CMS_HERO_SHOWCASE","SHOP_PRODUCT_GRID","SHOP_CURATED_BUNDLE","CMS_STEP_RITUAL_GUIDE","SHOP_COUPON_PACK","SHOP_PHOTO_REVIEWS","CMS_BRAND_MAGAZINE","CMS_FAQ_ACCORDION","AI_TASTE_CURATOR","AI_BRAND_CONCIERGE"]'::jsonb,
    '{"siteType":"COMMERCE","siteTypeReason":"유기농 말차 파우더와 수제 다기 판매 및 선물 포장 결제가 중심인 D2C 브랜드 스토어입니다.","category":"프리미엄 F&B · 다도 D2C 스토어","heroStyle":"EDITORIAL_STORY","brandSlogan":"제주 화산암반수와 해풍이 길러낸 첫물 유기농 차광 말차 & 수제 다기 아뜰리에"}'::jsonb,
    'PUBLISHED',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
),
(
    'idea-kkumz-002',
    'minjun.maker',
    '박민준 메이커',
    '몰입의 궤도를 설계하는 CNC 알루미늄 데스크테리어 & 매그세이프 기어 샵',
    '개발자와 디자이너의 책상 위 케이블을 깔끔하게 정리해 주는 CNC 통절삭 알루미늄 Qi2 충전 스테이션, 북미산 월넛 마그네틱 데스크 쉘프, 비대칭 모니터 조명을 판매하는 테크 브랜드 스토어를 만들고 싶습니다.',
    'COMMERCE',
    'TECH_HARDWARE',
    '["데스크테리어", "매그세이프충전독", "모듈형데스크", "개발자셋업", "CNC알루미늄"]'::jsonb,
    'PUBLIC',
    6,
    '데스크 오빗',
    'desk-orbit',
    '<svg viewBox="0 0 150 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#1e1b4b"/><ellipse cx="20" cy="20" rx="13" ry="6" transform="rotate(-25 20 20)" stroke="#818cf8" stroke-width="2.5" fill="none"/><circle cx="20" cy="20" r="4" fill="#38bdf8"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#1e1b4b">DESK ORBIT</text></svg>',
    '{"primary":"#1e1b4b","secondary":"#4f46e5","accent":"#06b6d4","surface":"#f8fafc","radius":"10px","fontHeading":"Pretendard","fontBody":"Pretendard"}'::jsonb,
    '## 1. 브랜드 비전 및 핵심 가치
- **브랜드명**: 데스크 오빗
- **핵심 슬로건**: 몰입의 궤도를 설계하는 모듈형 알루미늄 데스크테리어 & 매그세이프 테크 기어
- **타겟 고객**: 재택근무 및 하이엔드 워크스테이션 셋업에 진심인 개발자, 프로덕트 디자이너, 크리에이터

## 2. 핵심 스토어 기능 구성
1. **다크 테크 셋업 쇼케이스**: 실제 듀얼 모니터 & 맥북 워크스테이션 구성별 모듈 호환 가이드
2. **모듈형 카탈로그 & 번들 할인**: 충전 독, 월넛 데스크 쉘프, 비대칭 스크린바, QMK 매크로 패드 라인업
3. **AI 데스크 셋업 어드바이저**: 책상 가로 폭(1200/1400/1600mm)과 보유 기기에 맞는 최적 배치 추천',
    '[
      {"screenKey":"HOME_HERO","title":"다크 테크 워크스테이션 히어로","layout":"DARK_TECH_GRID","components":["OrbitSetupShowcase","SpecHighlightStrip","BestGearGrid"]},
      {"screenKey":"CATALOG","title":"모듈형 기어 카탈로그","layout":"SPEC_COMPARISON_GRID","components":["CompatibilityFilter","ProductCard","CartSidebar"]},
      {"screenKey":"CHECKOUT","title":"익스프레스 주문서","layout":"ONE_PAGE_CHECKOUT","components":["ShippingAddressForm","CouponApplier","InstantOrderButton"]}
    ]'::jsonb,
    '["CMS_HERO_SHOWCASE","SHOP_PRODUCT_GRID","SHOP_SPEC_COMPARISON","SHOP_CURATED_BUNDLE","SHOP_FLASH_DEAL","SHOP_PHOTO_REVIEWS","CMS_FAQ_ACCORDION","AI_TASTE_CURATOR","AI_BRAND_CONCIERGE"]'::jsonb,
    '{"siteType":"COMMERCE","siteTypeReason":"하드웨어 상세 스펙 비교와 묶음 세트 구매가 핵심인 테크 기어 스토어입니다.","category":"테크 하드웨어 · 데스크테리어 기어","heroStyle":"PROMO_COMMERCE","brandSlogan":"몰입의 궤도를 설계하는 모듈형 알루미늄 데스크테리어 & 매그세이프 테크 기어"}'::jsonb,
    'PUBLISHED',
    CURRENT_TIMESTAMP - INTERVAL '4 days',
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
),
(
    'idea-kkumz-003',
    'jihoon.run',
    '정지훈 크루장',
    '한강 나이트 러닝 크루 커뮤니티 & 마라톤 트레이닝 매거진 웹사이트',
    '매주 화요일·목요일 저녁 반포·성수·여의도 한강공원에서 함께 달리는 2030 러닝 크루 공식 브랜드 사이트이자, 러닝 코스 아카이브와 정규 세션 참가 신청을 받는 커뮤니티 웹사이트를 만들고 싶어요.',
    'CMS_SITE',
    'COMMUNITY_MEDIA',
    '["러닝크루", "한강나이트런", "마라톤트레이닝", "커뮤니티웹", "시티러너"]'::jsonb,
    'PUBLIC',
    6,
    '서울 나이트 런 클럽',
    'seoul-run-club',
    '<svg viewBox="0 0 170 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#0f172a"/><path d="M9 26L18 14L23 21L31 12" stroke="#f97316" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/><circle cx="31" cy="12" r="3" fill="#fb923c"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#0f172a">SEOUL RUN CLUB</text></svg>',
    '{"primary":"#ea580c","secondary":"#0f172a","accent":"#f59e0b","surface":"#fff7ed","radius":"12px","fontHeading":"Pretendard","fontBody":"Pretendard"}'::jsonb,
    '## 1. 커뮤니티 비전 및 핵심 가치
- **사이트명**: 서울 나이트 런 클럽
- **핵심 슬로건**: 서울의 밤을 가르는 페이스, 함께 달릴 때 더 멀리 가는 시티 러닝 커뮤니티
- **사이트 유형**: 브랜드·커뮤니티 웹 (매거진 아티클·배너·온라인 참가 신청 통합)

## 2. 핵심 콘텐츠 및 커뮤니티 기능
1. **이번 주 나이트 런 세션 공지**: 반포 잠수교 7K 템포런, 성수 서울숲 5K 리커버리런 일정 안내
2. **서울 시티 러닝 코스 매거진**: 급수대, 짐보관소, 노면 상태가 정리된 코스 가이드 아티클
3. **신규 크루원 & 게스트 런 온라인 신청 폼**: 희망 페이스(530/600/630) 및 세션 참가 신청 실시간 접수',
    '[
      {"screenKey":"CMS_HERO","title":"시티 나이트 런 비주얼 배너","layout":"MAGAZINE_HERO","components":["WeeklySessionBanner","CrewMetricsBar"]},
      {"screenKey":"ARTICLE_FEED","title":"러닝 코스 가이드 & 훈련 칼럼","layout":"EDITORIAL_CARDS","components":["CourseArticleList","PaceCalculatorCard"]},
      {"screenKey":"APPLY_FORM","title":"게스트 런 & 크루 합류 신청 폼","layout":"DYNAMIC_CMS_FORM","components":["SessionSelectField","PaceRadioGroup","SubmitApplicationButton"]}
    ]'::jsonb,
    '["CMS_HERO_SHOWCASE","CMS_SESSION_TIMETABLE","CMS_SPACE_TOUR","CMS_BRAND_MAGAZINE","CMS_PHOTO_GALLERY","CMS_DYNAMIC_BOOKING_FORM","CMS_LIVE_POLL","CMS_LOCATION_DIRECTIONS","AI_TEAM_RECRUIT","AI_BRAND_CONCIERGE"]'::jsonb,
    '{"siteType":"CMS_SITE","siteTypeReason":"주간 러닝 세션 일정표, 코스 매거진, 게스트 참가 신청 폼이 중심인 커뮤니티 웹사이트입니다.","category":"스포츠 크루 · 러닝 커뮤니티 미디어","heroStyle":"FULL_SCENIC","brandSlogan":"서울의 밤을 가르는 페이스, 함께 달릴 때 더 멀리 가는 시티 러닝 커뮤니티"}'::jsonb,
    'PUBLISHED',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '3 hours'
),
(
    'idea-kkumz-004',
    'hajun.roast',
    '최하준 헤드로스터',
    '스페셜티 커피 로스터리 아카이브 매거진 & 싱글오리진 원두 스토어 복합 사이트',
    '콜롬비아·에티오피아 산지 농장 직교역(Direct Trade) 인터뷰 매거진과 로스팅 프로파일 아티클을 발행하면서, 매월 로스터가 선별한 싱글오리진 원두와 드립백을 바로 주문하고 시음 클래스도 예약할 수 있는 매거진+스토어 복합 사이트를 만들고 싶습니다.',
    'HYBRID',
    'LIFESTYLE_HYBRID',
    '["스페셜티커피", "로스터리매거진", "원두스토어", "시음클래스예약", "복합사이트"]'::jsonb,
    'PUBLIC',
    6,
    '빈 크로니클 서울',
    'bean-chronicle',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#451a03"/><ellipse cx="20" cy="20" rx="10" ry="13" fill="#d97706"/><path d="M20 8C17 14 23 26 20 32" stroke="#451a03" stroke-width="2.2" stroke-linecap="round"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#451a03">BEAN CHRONICLE</text></svg>',
    '{"primary":"#78350f","secondary":"#b45309","accent":"#d97706","surface":"#fffbeb","radius":"12px","fontHeading":"Pretendard","fontBody":"Pretendard"}'::jsonb,
    '## 1. 브랜드 비전 및 복합 아키텍처
- **사이트명**: 빈 크로니클 서울
- **핵심 슬로건**: 한 잔의 커피에 담긴 산지의 연대기 — 로스터리 저널 & 스페셜티 셀렉트 샵
- **사이트 유형**: 매거진 + 스토어 복합 사이트 (아티클·클래스 신청 + 상품 카탈로그·주문 통합)

## 2. 핵심 복합 기능 구성
1. **산지 다이렉트 트레이드 매거진**: 농장 인터뷰, 커핑 노트, 브루잉 레시피 저널
2. **이달의 로스터리 셀렉션 & 브루잉 기어 스토어**: 원두·드립 커피머신·그라인더 즉시 구매
3. **프라이빗 커핑 세션 & 원두 납품 문의**: 주말 커핑 클래스 예약 및 카페 B2B 원두 샘플 신청',
    '[
      {"screenKey":"HYBRID_HERO","title":"로스터리 저널 & 이달의 원두 스플릿 히어로","layout":"HYBRID_EDITORIAL_COMMERCE","components":["FeaturedStoryHero","RoastProfileBadge"]},
      {"screenKey":"SHOP_AND_JOURNAL","title":"브루잉 스토어 & 커핑 아카이브 통합 탭","layout":"DUAL_STREAM_GRID","components":["CommerceCatalogSection","EditorialJournalSection"]},
      {"screenKey":"CUPPING_RESERVE","title":"커핑 클래스 예약 및 B2B 샘플 신청","layout":"DYNAMIC_CMS_FORM","components":["ClassBookingForm","AiBrewingConcierge"]}
    ]'::jsonb,
    '["CMS_HERO_SHOWCASE","SHOP_PRODUCT_GRID","SHOP_SUBSCRIPTION_TIERS","CMS_BRAND_MAGAZINE","CMS_STEP_RITUAL_GUIDE","CMS_DYNAMIC_BOOKING_FORM","SHOP_PHOTO_REVIEWS","CMS_FAQ_ACCORDION","AI_TASTE_CURATOR","AI_BRAND_CONCIERGE"]'::jsonb,
    '{"siteType":"HYBRID","siteTypeReason":"산지 농장 인터뷰 매거진과 시음 클래스 예약(SyncCMS) + 싱글오리진 원두·브루잉 기어 스토어(SyncShop)가 모두 필요한 복합 브랜드입니다.","category":"스페셜티 커피 로스터리 · 라이프스타일 복합","heroStyle":"SPLIT_VISUAL","brandSlogan":"한 잔의 커피에 담긴 산지의 연대기 — 로스터리 저널 & 스페셜티 셀렉트 샵"}'::jsonb,
    'PUBLISHED',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes'
),
(
    'idea-01137479',
    'admin',
    '김도현 총괄관리자',
    '충남 공주 시골 맑은 냇가 옆 독채 민박 & 평상 스테이 (맑은물 민박)',
    '충남 공주 시골 맑은 냇가 바로 옆에 위치한 정겨운 맑은물 민박 공식 사이트입니다. 황토 온돌 독채 객실과 냇가 평상 당일 대여, 능이 토종닭 백숙·숯불 바비큐 세트를 예약받고, 성수기/비수기 상세 요금표와 사계절 현장 사진 갤러리(12장), 공간 구역별 투어, 체크인 날짜·인원·바비큐 옵션을 선택하는 맞춤 예약 신청 폼, 숙박 FAQ, 공주 오시는 길 및 주변 추천 관광코스를 한눈에 제공합니다.',
    'HYBRID',
    '숙박 · 민박 · 프라이빗 스테이',
    '["공주민박", "냇가평상", "황토독채", "능이백숙", "사계절촌캉스", "가족스테이"]'::jsonb,
    'PUBLIC',
    6,
    '맑은물 민박',
    'malgeunmul-minbak',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#0f766e"/><path d="M8 25 C13 19, 18 19, 23 25 C26 28, 30 28, 33 23" stroke="#99f6e4" stroke-width="2.5" fill="none" stroke-linecap="round"/><path d="M12 18 L20 11 L28 18" stroke="#ffffff" stroke-width="2.2" fill="none" stroke-linecap="round" stroke-linejoin="round"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-size="15" font-weight="800" fill="#0f766e">맑은물 민박</text></svg>',
    '{"primary":"#0f766e","secondary":"#115e59","accent":"#d97706","surface":"#f0fdf4","radius":"14px","fontHeading":"Pretendard","fontBody":"Pretendard"}'::jsonb,
    '# 맑은물 민박 서비스 기능명세서\n\n## 핵심 제공 기능 및 공간 경험\n- 충남 공주 청정 냇가 바로 앞 프라이빗 황토 독채 & 별채 온돌방 실시간 예약\n- 여름철 시원한 냇가 평상·정자 당일 대여 및 능이 토종닭 백숙·숯불 바비큐 패키지 주문\n- 비수기·성수기·주중·주말 및 인원 추가 기준이 정리된 투명한 시즌 요금표 매트릭스\n- 사계절 현장 사진 갤러리(12장) 및 실내·평상존·냇가 물놀이장 구역별 스페이스 투어\n- 체크인 날짜, 희망 객실/평상, 방문 인원수, 식사·바비큐 옵션을 선택하는 동적 예약 신청 폼\n- 자주 묻는 질문(FAQ), 공주 오시는 길·주변 관광코스 및 24시간 AI 민박 컨시어지 상담\n',
    '[
      {"screenKey":"SCR_HOME","screenTitle":"메인 히어로 & 객실·평상 카탈로그","routePath":"/","layoutType":"HYBRID_EDITORIAL_COMMERCE","description":"공주 냇가 대표 전경 배너와 독채 객실·평상 대여·백숙 패키지 쇼케이스","components":["CMS_HERO_SHOWCASE","CMS_TRUST_METRICS_BAR","SHOP_ROOM_SHOWCASE","SHOP_SEASONAL_MENU","SHOP_PRICING_MATRIX"]},
      {"screenKey":"SCR_GALLERY","screenTitle":"사계절 현장 포토 갤러리 & 공간 투어","routePath":"/gallery","layoutType":"GALLERY_SHOWCASE","description":"여름 계곡 물놀이, 가을 단풍 정자, 겨울 눈썰매 등 12장의 현장 사진과 구역별 안내","components":["CMS_SPACE_TOUR","CMS_PHOTO_GALLERY","CMS_STEP_RITUAL_GUIDE"]},
      {"screenKey":"SCR_MAGAZINE","screenTitle":"맑은물 계절 소식 & 방문객 후기","routePath":"/magazine","layoutType":"MAGAZINE_LIST","description":"공주 계절 소식 저널, 실제 가족 방문객 포토 후기 및 선호도 투표","components":["CMS_BRAND_MAGAZINE","SHOP_PHOTO_REVIEWS","CMS_LIVE_POLL"]},
      {"screenKey":"SCR_RESERVATION","screenTitle":"맞춤 일정·객실·바비큐 온라인 예약","routePath":"/reserve","layoutType":"RESERVATION_FORM","description":"체크인 날짜, 객실/평상 타입, 인원수, 능이백숙·숯불 옵션을 선택하는 동적 예약 폼","components":["CMS_DYNAMIC_BOOKING_FORM","SHOP_COUPON_PACK","CMS_POLICY_RULES_BOX"]},
      {"screenKey":"SCR_FAQ_CONCIERGE","screenTitle":"숙박 FAQ · 오시는 길 & AI 컨시어지","routePath":"/concierge","layoutType":"FAQ_CONCIERGE","description":"자주 묻는 질문 아코디언, 공주 네비게이션 주소·주변 코스 및 24시간 AI 상담","components":["AI_TASTE_CURATOR","CMS_FAQ_ACCORDION","CMS_LOCATION_DIRECTIONS","AI_BRAND_CONCIERGE"]}
    ]'::jsonb,
    '["CMS_HERO_SHOWCASE","CMS_TRUST_METRICS_BAR","SHOP_ROOM_SHOWCASE","SHOP_SEASONAL_MENU","SHOP_PRICING_MATRIX","SHOP_COUPON_PACK","CMS_SPACE_TOUR","CMS_PHOTO_GALLERY","CMS_STEP_RITUAL_GUIDE","CMS_BRAND_MAGAZINE","SHOP_PHOTO_REVIEWS","CMS_LIVE_POLL","CMS_DYNAMIC_BOOKING_FORM","AI_TASTE_CURATOR","CMS_FAQ_ACCORDION","CMS_LOCATION_DIRECTIONS","CMS_POLICY_RULES_BOX","AI_BRAND_CONCIERGE"]'::jsonb,
    '{"siteType":"HYBRID","siteTypeReason":"독채 객실·냇가 평상·능이백숙 예약 및 요금표(SyncShop)와 사계절 공간 갤러리·맞춤 예약폼·오시는길 가이드(SyncCMS)가 결합된 숙박·스테이 복합 사이트입니다.","category":"숙박 · 민박 · 프라이빗 스테이","heroStyle":"FULL_SCENIC","brandSlogan":"공주 청정 냇가 물소리와 따뜻한 황토 온돌이 맞이하는 사계절 시골 쉼터","expandedPrompt":"충남 공주 시골 맑은 냇가 바로 옆에 자리한 맑은물 민박은 가족과 친구들이 도심을 벗어나 온전한 쉼을 누리는 프라이빗 스테이입니다. 황토 온돌 독채와 별채 객실, 냇가 바로 옆 시원한 평상·정자 대여, 능이 토종닭 백숙과 숯불 바비큐 세트를 투명한 시즌별 요금표와 함께 예약할 수 있으며, 12장의 사계절 현장 사진과 구역별 공간 투어, 동적 예약 신청 폼, 공주 주변 관광코스를 제공합니다.","recommendedFeatures":["CMS_HERO_SHOWCASE","CMS_TRUST_METRICS_BAR","SHOP_ROOM_SHOWCASE","SHOP_SEASONAL_MENU","SHOP_PRICING_MATRIX","SHOP_COUPON_PACK","CMS_SPACE_TOUR","CMS_PHOTO_GALLERY","CMS_STEP_RITUAL_GUIDE","CMS_BRAND_MAGAZINE","SHOP_PHOTO_REVIEWS","CMS_LIVE_POLL","CMS_DYNAMIC_BOOKING_FORM","AI_TASTE_CURATOR","CMS_FAQ_ACCORDION","CMS_LOCATION_DIRECTIONS","CMS_POLICY_RULES_BOX","AI_BRAND_CONCIERGE"],"featureReasons":{"SHOP_ROOM_SHOWCASE":"독채·황토방·냇가 평상 등 공간 타입별 기준 인원과 1박/대여 요금을 한눈에 비교하고 바로 예약할 수 있습니다.","SHOP_PRICING_MATRIX":"비수기·성수기·주중·주말 요금과 인원 추가, 바비큐 숯불 비용을 표로 명확히 안내해 전화 문의를 줄여줍니다.","CMS_SPACE_TOUR":"실내 온돌방, 마당 정자, 냇가 평상존, 물놀이/썰매 체험 구역을 동선별로 생생하게 소개합니다.","CMS_DYNAMIC_BOOKING_FORM":"체크인 날짜, 희망 객실/평상, 방문 인원수, 능이백숙·바비큐 옵션을 선택해 간편하게 예약 신청을 접수합니다.","CMS_LOCATION_DIRECTIONS":"공주 시골길 네비게이션 주소 복사, 주차 공간 안내 및 공산성·마곡사 등 주변 추천 코스를 안내합니다."}}'::jsonb,
    'PUBLISHED',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '10 minutes'
);

INSERT INTO kkumz_idea_version (
    id, idea_id, version_no, title, prompt_text, spec_markdown, screens_json, created_by, created_at
) VALUES
(
    'iver-kkumz-001-v1',
    'idea-kkumz-001',
    1,
    '제주 유기농 말차 쇼핑몰 초기 기획안 (v1)',
    '제주도 유기농 말차 가루와 차선을 파는 쇼핑몰을 만들고 싶어요.',
    '## 1. 초기 기획 요약\n- 제주산 유기농 말차 파우더 단품 판매 중심의 기본 쇼핑몰 구성\n- 기본 상품 리스트 및 장바구니 기능 포함',
    '[{"screenKey":"HOME_HERO","title":"기본 메인 배너","layout":"STANDARD_HERO","components":["HeroBanner","ProductGrid"]}]'::jsonb,
    'soyeon.jeju',
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'iver-kkumz-001-v2',
    'idea-kkumz-001',
    2,
    '제주 서귀포 첫물 유기농 차광 말차 & 도예가 수제 다기 브랜드 스토어 (v2 확정)',
    '제주 서귀포 유기농 다원에서 재배한 첫물 세레모니얼 말차와 이천 도예가의 수제 백자 다완, 100본립 차선을 큐레이션하여 판매하는 프리미엄 다도 D2C 쇼핑몰을 만들고 싶어요.',
    '## 1. 브랜드 비전 및 핵심 가치\n- **브랜드명**: 말차 아뜰리에 제주\n- **핵심 슬로건**: 제주 화산암반수와 해풍이 길러낸 첫물 유기농 차광 말차 & 수제 다기 아뜰리에\n- **차별화 포인트**: 이천 도예 장인 협업 백자 다완 스타터 세트 및 AI 티 소믈리에 컨시어지 탑재',
    '[{"screenKey":"HOME_HERO","title":"보태니컬 다도 히어로 섹션","layout":"EDITORIAL_BOTANICAL","components":["BrandStoryBanner","MatchaRitualSteps","FeaturedProductGrid"]},{"screenKey":"CATALOG","title":"말차 등급 및 다구 카탈로그","layout":"FILTERABLE_3COL_GRID","components":["CategoryTabs","TasteNoteBadge","QuickCartDrawer"]}]'::jsonb,
    'soyeon.jeju',
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'iver-kkumz-002-v1',
    'idea-kkumz-002',
    1,
    '데스크 오빗 모듈형 데스크테리어 스토어 기획서 (v1)',
    '개발자와 디자이너의 책상 위 케이블을 깔끔하게 정리해 주는 CNC 통절삭 알루미늄 Qi2 충전 스테이션, 북미산 월넛 마그네틱 데스크 쉘프, 비대칭 모니터 조명을 판매하는 테크 브랜드 스토어를 만들고 싶습니다.',
    '## 1. 브랜드 비전\n- **브랜드명**: 데스크 오빗\n- **핵심 기능**: 다크 테크 감성의 3-in-1 Qi2 충전 독 및 마그네틱 데스크 쉘프 전문 D2C 커머스',
    '[{"screenKey":"HOME_HERO","title":"다크 테크 워크스테이션 히어로","layout":"DARK_TECH_GRID","components":["OrbitSetupShowcase","BestGearGrid"]}]'::jsonb,
    'minjun.maker',
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'iver-kkumz-003-v1',
    'idea-kkumz-003',
    1,
    '서울 나이트 런 클럽 커뮤니티 사이트 기획서 (v1)',
    '매주 화요일·목요일 저녁 반포·성수·여의도 한강공원에서 함께 달리는 2030 러닝 크루 공식 브랜드 사이트이자, 러닝 코스 아카이브와 정규 세션 참가 신청을 받는 커뮤니티 웹사이트를 만들고 싶어요.',
    '## 1. 커뮤니티 비전\n- **사이트명**: 서울 나이트 런 클럽\n- **핵심 기능**: 주간 세션 일정 공지, 한강 러닝 코스 매거진, 게스트 런 참가 신청 폼',
    '[{"screenKey":"CMS_HERO","title":"시티 나이트 런 비주얼 배너","layout":"MAGAZINE_HERO","components":["WeeklySessionBanner","CourseArticleList","SessionApplyForm"]}]'::jsonb,
    'jihoon.run',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'iver-kkumz-004-v1',
    'idea-kkumz-004',
    1,
    '빈 크로니클 서울 로스터리 매거진 & 스토어 복합 기획서 (v1)',
    '콜롬비아·에티오피아 산지 농장 직교역 인터뷰 매거진과 로스팅 프로파일 아티클을 발행하면서, 싱글오리진 원두를 바로 주문하고 시음 클래스도 예약할 수 있는 복합 사이트를 만들고 싶습니다.',
    '## 1. 복합 사이트 비전\n- **사이트명**: 빈 크로니클 서울\n- **핵심 기능**: 산지 아카이브 저널 + 브루잉 스토어 + 커핑 클래스 예약 폼 통합',
    '[{"screenKey":"HYBRID_HERO","title":"로스터리 저널 & 원두 스토어 복합 히어로","layout":"HYBRID_EDITORIAL_COMMERCE","components":["FeaturedStoryHero","CommerceCatalogSection","EditorialJournalSection"]}]'::jsonb,
    'hajun.roast',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
);

-- AI Brand Naming Suggestions
INSERT INTO kkumz_name_suggestion (
    id, idea_id, brand_name, site_slug, pronunciation, brand_story, score, slug_available, is_selected, created_at
) VALUES
('name-kkumz-101', 'idea-kkumz-001', '말차 아뜰리에 제주', 'matcha-atelier', 'Matcha Atelier Jeju / 말차 아뜰리에 제주', '제주 차밭의 장인정신과 현대적인 다도 공방의 감성을 결합한 프리미엄 다이렉트 브랜드명입니다.', 99, TRUE, TRUE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('name-kkumz-102', 'idea-kkumz-001', '설록차실 서귀포', 'seogwipo-chasil', 'Seogwipo Chasil / 서귀포 차실', '서귀포 화산암반수 다원의 지역성과 고요한 프라이빗 다실의 공간감을 강조한 네이밍입니다.', 95, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('name-kkumz-103', 'idea-kkumz-001', '비취다원', 'jade-tea-garden', 'Bichwi Dawon / 비취다원', '21일 차광 재배로 깊어진 첫물 말차의 에메랄드(비취)빛 수색을 직관적으로 표현합니다.', 93, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('name-kkumz-104', 'idea-kkumz-001', '첫물 리추얼', 'first-harvest-ritual', 'Cheotmul Ritual / 첫물 리추얼', '매년 봄 가장 먼저 수확한 어린 찻잎으로 시작하는 5분 명상 다도 루틴을 뜻합니다.', 92, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('name-kkumz-105', 'idea-kkumz-001', '다완과 차선', 'chawan-chasen', 'Chawan & Chasen / 다완과 차선', '이천 수제 백자 다완과 대나무 차선으로 완성하는 정통 홈 다구 큐레이션을 상징합니다.', 91, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('name-kkumz-106', 'idea-kkumz-001', '우마미 그린', 'umami-green-jeju', 'Umami Green / 우마미 그린', '떫은맛 없이 깊고 풍부한 테아닌 감칠맛(우마미)을 전면에 내세운 모던 감각 네이밍입니다.', 90, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('name-kkumz-107', 'idea-kkumz-001', '하영다도', 'hayoung-tea', 'Hayoung Dado / 하영다도', '제주의 너른 자연과 풍요로움을 뜻하는 제주어 어감을 살린 로컬 헤리티지 브랜드명입니다.', 89, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('name-kkumz-108', 'idea-kkumz-001', '크림말차 랩', 'cream-matcha-lab', 'Cream Matcha Lab / 크림말차 랩', '마이크로 벨벳 거품과 무가당 라떼 베이스 연구에 특화된 젊은 감각의 브랜드명입니다.', 88, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('name-kkumz-201', 'idea-kkumz-002', '데스크 오빗', 'desk-orbit', 'Desk Orbit / 데스크 오빗', '책상 위 모든 디바이스와 도구가 안정적인 몰입의 궤도로 정렬된다는 의미를 담은 테크 기어 브랜드입니다.', 98, TRUE, TRUE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('name-kkumz-202', 'idea-kkumz-002', '알루미나 독 랩', 'alumina-dock-lab', 'Alumina Dock Lab / 알루미나 독 랩', '항공 등급 6061 통절삭 알루미늄 가공 기술과 마그네틱 충전 스테이션의 정밀함을 강조합니다.', 95, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('name-kkumz-203', 'idea-kkumz-002', '모듈러 그리드 서울', 'modular-grid-seoul', 'Modular Grid Seoul / 모듈러 그리드', '개발자와 디자이너의 작업 동선에 맞춰 자유롭게 확장되는 모듈형 데스크 셋업 시스템입니다.', 94, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('name-kkumz-204', 'idea-kkumz-002', '월넛 앤 아노다이즈', 'walnut-anodize', 'Walnut & Anodize / 월넛 앤 아노다이즈', '따뜻한 북미산 호두나무 원목과 차가운 애노다이징 메탈의 조화로운 대비를 직관적으로 담았습니다.', 92, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('name-kkumz-205', 'idea-kkumz-002', '포커스 아키텍처', 'focus-architecture', 'Focus Architecture / 포커스 아키텍처', '케이블 노이즈를 제거하고 순수한 몰입의 공간만을 설계한다는 프로페셔널 워크스페이스 브랜드입니다.', 91, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('name-kkumz-206', 'idea-kkumz-002', '제로케이블 스튜디오', 'zerocable-studio', 'ZeroCable Studio / 제로케이블 스튜디오', 'Qi2 무선 충전과 매립형 마그네틱 레일로 선 없는 미니멀 데스크를 구현하는 브랜드 정체성을 표현합니다.', 90, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('name-kkumz-207', 'idea-kkumz-002', '스페이스 기어 콜렉티브', 'space-gear-collective', 'Space Gear Collective / 스페이스 기어', '하이엔드 키보드, 모니터 조명, 충전 독 메이커들이 모인 크리에이터 장비 큐레이션 샵입니다.', 89, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('name-kkumz-208', 'idea-kkumz-002', '노바 데스크웨어', 'nova-deskware', 'Nova Deskware / 노바 데스크웨어', '야간 코딩과 크리에이티브 작업 환경을 밝혀주는 차세대 데스크테리어 하드웨어 브랜드명입니다.', 88, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('name-kkumz-301', 'idea-kkumz-003', '서울 나이트 런 클럽', 'seoul-run-club', 'Seoul Night Run Club / 서울 나이트 런 클럽', '서울 한강의 야경을 가르며 함께 달리는 역동적인 시티 러닝 크루 아이덴티티를 담았습니다.', 97, TRUE, TRUE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('name-kkumz-302', 'idea-kkumz-003', '한강 페이서스 530', 'hangang-pacers', 'Hangang Pacers / 한강 페이서스', '반포·성수·여의도 한강공원에서 서로의 페이스를 끌어주는 2030 러너 커뮤니티를 상징합니다.', 95, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('name-kkumz-303', 'idea-kkumz-003', '미드나잇 스트라이드', 'midnight-stride', 'Midnight Stride / 미드나잇 스트라이드', '퇴근 후 도심의 밤공기를 가르며 달리는 시티 러너들의 경쾌한 보폭과 에너지를 표현합니다.', 94, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('name-kkumz-304', 'idea-kkumz-003', '브릿지 러너스 아카이브', 'bridge-runners', 'Bridge Runners Archive / 브릿지 러너스', '잠수교부터 성수대교까지 서울의 다리와 강변 코스를 기록하는 러닝 매거진 & 크루 브랜드입니다.', 92, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('name-kkumz-305', 'idea-kkumz-003', '펄스 트랙 서울', 'pulse-track-seoul', 'Pulse Track Seoul / 펄스 트랙 서울', '심박수가 뛰는 템포런 세션과 체계적인 마라톤 풀코스 훈련 커뮤니티의 전문성을 담았습니다.', 91, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('name-kkumz-306', 'idea-kkumz-003', '어반 마일 콜렉티브', 'urban-mile-collective', 'Urban Mile Collective / 어반 마일', '도심 속 1마일씩 함께 누적하며 성장하는 오픈 게스트 런 & 라이프스타일 크루 네이밍입니다.', 90, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('name-kkumz-307', 'idea-kkumz-003', '런 앤 리커버리 클럽', 'run-recovery-club', 'Run & Recovery Club / 런 앤 리커버리', '부상 없는 러닝과 세션 후 스트레칭·교류 문화를 지향하는 친근한 커뮤니티 브랜드명입니다.', 89, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('name-kkumz-308', 'idea-kkumz-003', '시티 템포 저널', 'city-tempo-journal', 'City Tempo Journal / 시티 템포 저널', '러닝 코스 리뷰, 러닝화 큐레이션, 주간 세션 신청을 아우르는 미디어형 러닝 클럽입니다.', 88, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('name-kkumz-401', 'idea-kkumz-004', '빈 크로니클 서울', 'bean-chronicle', 'Bean Chronicle Seoul / 빈 크로니클 서울', '산지 농부부터 로스터, 바리스타까지 이어지는 스페셜티 원두의 연대기를 기록하는 복합 문화 브랜드입니다.', 98, TRUE, TRUE, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('name-kkumz-402', 'idea-kkumz-004', '테루아 로스터리 저널', 'terroir-roastery', 'Terroir Roastery Journal / 테루아 로스터리', '콜롬비아·에티오피아 고산지대의 토양과 기후(떼루아)를 한 잔의 커피와 아티클로 전달합니다.', 96, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('name-kkumz-403', 'idea-kkumz-004', '마이크로랏 아뜰리에', 'microlot-atelier', 'Microlot Atelier / 마이크로랏 아뜰리에', '소량 직교역(Direct Trade)으로만 공수한 희귀 게이샤·무산소 발효 원두 전문 큐레이션 스토어입니다.', 94, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('name-kkumz-404', 'idea-kkumz-004', '성수 커핑 노트', 'seongsu-cupping-note', 'Seongsu Cupping Note / 성수 커핑 노트', '매월 헤드로스터의 테이스팅 노트 매거진과 주말 프라이빗 시음 클래스가 열리는 복합 공간 브랜드입니다.', 93, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('name-kkumz-405', 'idea-kkumz-004', '다이렉트 빈 아카이브', 'direct-bean-archive', 'Direct Bean Archive / 다이렉트 빈 아카이브', '산지 농장 인터뷰 아카이브와 신선한 로스팅 원두 즉시 주문을 하나로 결합한 하이브리드 브랜드명입니다.', 91, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('name-kkumz-406', 'idea-kkumz-004', '나인티투 브루잉 랩', 'ninetytwo-brewing', '92 Brewing Lab / 나인티투 브루잉 랩', '스페셜티 추출의 황금 온도인 92℃에서 영감을 얻은 정밀 로스팅 & 브루잉 기어 셀렉트샵입니다.', 90, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('name-kkumz-407', 'idea-kkumz-004', '앰버 크래프트 로스터스', 'amber-craft-roasters', 'Amber Craft Roasters / 앰버 크래프트', '호박빛 클린컵 추출액과 장인 로스터의 섬세한 불조절 프로파일을 상징하는 감성 브랜드명입니다.', 89, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('name-kkumz-408', 'idea-kkumz-004', '오리진 앤 푸어', 'origin-and-pour', 'Origin & Pour / 오리진 앤 푸어', '싱글오리진 원두의 본질과 푸어오버 핸드드립의 여유로운 일상을 잇는 모던 커피 브랜드입니다.', 88, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 days');

-- AI SVG Logo & Palette Suggestions
INSERT INTO kkumz_logo_suggestion (
    id, idea_id, style_name, svg_code, color_palette_json, typography_json, tags_json, is_selected, created_at
) VALUES
(
    'logo-kkumz-101',
    'idea-kkumz-001',
    '보태니컬 리프 엠블럼',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#14532d"/><circle cx="20" cy="20" r="11" fill="#86efac" fill-opacity="0.25"/><path d="M13 24C13 17 20 12 27 15C26 22 20 27 13 24Z" fill="#4ade80"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#14532d">MATCHA ATELIER</text></svg>',
    '{"primary":"#14532d","secondary":"#16a34a","accent":"#ca8a04","surface":"#f7fee7"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["보태니컬", "딥포레스트", "세레모니얼"]'::jsonb,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'logo-kkumz-102',
    'idea-kkumz-001',
    '미니멀 백자 다완 라인아트',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="20" fill="#ecfccb"/><path d="M10 17H30C30 25 25 29 20 29C15 29 10 25 10 17Z" stroke="#15803d" stroke-width="2.5" fill="none"/><path d="M15 31H25" stroke="#15803d" stroke-width="2.5" stroke-linecap="round"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="700" font-size="14" fill="#14532d">MATCHA ATELIER</text></svg>',
    '{"primary":"#15803d","secondary":"#3f6212","accent":"#a16207","surface":"#f7fee7"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"700"}'::jsonb,
    '["다완실루엣", "미니멀", "젠스타일"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'logo-kkumz-103',
    'idea-kkumz-001',
    '맷돌 스톤 모노그램',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="8" fill="#064e3b"/><circle cx="20" cy="20" r="12" stroke="#6ee7b7" stroke-width="2" fill="none"/><circle cx="20" cy="20" r="4" fill="#34d399"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#064e3b">MATCHA ATELIER</text></svg>',
    '{"primary":"#064e3b","secondary":"#059669","accent":"#d97706","surface":"#ecfdf5"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["맷돌모티브", "에메랄드", "크래프트"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'logo-kkumz-104',
    'idea-kkumz-001',
    '제주 오름 & 차밭 지오메트릭',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#166534"/><path d="M8 28L17 15L24 23L28 18L33 28H8Z" fill="#bbf7d0"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#166534">MATCHA ATELIER</text></svg>',
    '{"primary":"#166534","secondary":"#22c55e","accent":"#eab308","surface":"#f0fdf4"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["제주오름", "내추럴", "가든"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'logo-kkumz-105',
    'idea-kkumz-001',
    '차선 위스크 시그니처',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#365314"/><path d="M14 12C14 20 17 24 20 28C23 24 26 20 26 12" stroke="#bef264" stroke-width="2.2" fill="none"/><path d="M20 11V29" stroke="#bef264" stroke-width="2.2"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#365314">MATCHA ATELIER</text></svg>',
    '{"primary":"#365314","secondary":"#65a30d","accent":"#ca8a04","surface":"#f7fee7"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["100본립차선", "아티장", "모던다도"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'logo-kkumz-106',
    'idea-kkumz-001',
    '모던 씰 스탬프 배지',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect x="2" y="2" width="36" height="36" rx="8" stroke="#14532d" stroke-width="2.5" fill="#dcfce7"/><text x="12" y="26" font-family="Pretendard, sans-serif" font-weight="900" font-size="16" fill="#14532d">茶</text><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#14532d">MATCHA ATELIER</text></svg>',
    '{"primary":"#14532d","secondary":"#15803d","accent":"#b45309","surface":"#f0fdf4"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["낙관스탬프", "헤리티지", "티하우스"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'logo-kkumz-201',
    'idea-kkumz-002',
    '오빗 일립스 테크 마크',
    '<svg viewBox="0 0 165 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#1e1b4b"/><ellipse cx="20" cy="20" rx="13" ry="6" transform="rotate(-25 20 20)" stroke="#818cf8" stroke-width="2.5" fill="none"/><circle cx="20" cy="20" r="4" fill="#38bdf8"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#1e1b4b">DESK ORBIT</text></svg>',
    '{"primary":"#1e1b4b","secondary":"#4f46e5","accent":"#06b6d4","surface":"#f8fafc"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["데스크테리어", "인디고테크", "모듈형"]'::jsonb,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'logo-kkumz-202',
    'idea-kkumz-002',
    'CNC 헥사곤 정밀 배지',
    '<svg viewBox="0 0 165 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#0f172a"/><polygon points="20,8 30,14 30,26 20,32 10,26 10,14" stroke="#38bdf8" stroke-width="2.2" fill="none"/><circle cx="20" cy="20" r="3.5" fill="#818cf8"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#0f172a">DESK ORBIT</text></svg>',
    '{"primary":"#0f172a","secondary":"#334155","accent":"#38bdf8","surface":"#f1f5f9"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["CNC알루미늄", "다크슬레이트", "하드웨어"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'logo-kkumz-203',
    'idea-kkumz-002',
    '월넛 & 티타늄 레이어 심볼',
    '<svg viewBox="0 0 165 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#27272a"/><rect x="9" y="13" width="22" height="5" rx="2.5" fill="#d97706"/><rect x="9" y="22" width="22" height="5" rx="2.5" fill="#a1a1aa"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#27272a">DESK ORBIT</text></svg>',
    '{"primary":"#27272a","secondary":"#78350f","accent":"#d97706","surface":"#fafafa"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["월넛원목", "티타늄그레이", "데스크쉘프"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'logo-kkumz-204',
    'idea-kkumz-002',
    '매그세이프 링 마크',
    '<svg viewBox="0 0 165 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#172554"/><circle cx="20" cy="18" r="9" stroke="#60a5fa" stroke-width="2.5" fill="none"/><line x1="20" y1="29" x2="20" y2="33" stroke="#38bdf8" stroke-width="2.5" stroke-linecap="round"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#172554">DESK ORBIT</text></svg>',
    '{"primary":"#172554","secondary":"#2563eb","accent":"#38bdf8","surface":"#eff6ff"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["Qi2무선충전", "딥네이비", "스마트독"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'logo-kkumz-205',
    'idea-kkumz-002',
    '비대칭 라이트바 프리즘',
    '<svg viewBox="0 0 165 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#18181b"/><line x1="10" y1="12" x2="30" y2="12" stroke="#facc15" stroke-width="3" stroke-linecap="round"/><polygon points="13,15 27,15 32,30 8,30" fill="#fde047" fill-opacity="0.22"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#18181b">DESK ORBIT</text></svg>',
    '{"primary":"#18181b","secondary":"#3f3f46","accent":"#eab308","surface":"#f4f4f5"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["모니터조명", "앰비언트", "블랙골드"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'logo-kkumz-206',
    'idea-kkumz-002',
    '사이버 매트릭스 모노그램',
    '<svg viewBox="0 0 165 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#311042"/><rect x="11" y="11" width="7" height="7" rx="2" fill="#e879f9"/><rect x="22" y="11" width="7" height="7" rx="2" fill="#38bdf8"/><rect x="11" y="22" width="18" height="7" rx="2" fill="#c084fc"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#311042">DESK ORBIT</text></svg>',
    '{"primary":"#3b0764","secondary":"#7e22ce","accent":"#06b6d4","surface":"#faf5ff"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["매크로패드", "네온바이올렛", "크리에이터"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'logo-kkumz-301',
    'idea-kkumz-003',
    '나이트 펄스 트랙 마크',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#0f172a"/><path d="M9 26L18 14L23 21L31 12" stroke="#f97316" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/><circle cx="31" cy="12" r="3" fill="#fb923c"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#0f172a">SEOUL RUN CLUB</text></svg>',
    '{"primary":"#ea580c","secondary":"#0f172a","accent":"#f59e0b","surface":"#fff7ed"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["러닝크루", "에너지오렌지", "나이트런"]'::jsonb,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'logo-kkumz-302',
    'idea-kkumz-003',
    '한강 브릿지 웨이브 엠블럼',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#1e293b"/><path d="M8 24C14 14 26 14 32 24" stroke="#38bdf8" stroke-width="2.5" fill="none"/><path d="M8 28H32" stroke="#f97316" stroke-width="2.5" stroke-linecap="round"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#1e293b">SEOUL RUN CLUB</text></svg>',
    '{"primary":"#0284c7","secondary":"#0f172a","accent":"#f97316","surface":"#f0f9ff"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["한강 잠수교", "리버블루", "시티러너"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'logo-kkumz-303',
    'idea-kkumz-003',
    '네온 볼트 스트라이드',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#111827"/><polygon points="22,8 11,22 20,22 18,32 29,18 20,18" fill="#84cc16"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#111827">SEOUL RUN CLUB</text></svg>',
    '{"primary":"#3f6212","secondary":"#111827","accent":"#84cc16","surface":"#f7fee7"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["네온라임", "스피드세션", "마라톤"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'logo-kkumz-304',
    'idea-kkumz-003',
    '트랙 오벌 크루 실드',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#7c2d12"/><rect x="8" y="13" width="24" height="14" rx="7" stroke="#fdba74" stroke-width="2.5" fill="none"/><circle cx="26" cy="20" r="3" fill="#fb923c"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#7c2d12">SEOUL RUN CLUB</text></svg>',
    '{"primary":"#c2410c","secondary":"#431407","accent":"#fb923c","surface":"#fff7ed"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["트랙오벌", "테라코타", "인터벌훈련"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'logo-kkumz-305',
    'idea-kkumz-003',
    '미드나잇 문라이트 러너',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#1e1b4b"/><path d="M24 11A9 9 0 1 0 29 25A7 7 0 1 1 24 11Z" fill="#fde047"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#1e1b4b">SEOUL RUN CLUB</text></svg>',
    '{"primary":"#1e1b4b","secondary":"#312e81","accent":"#eab308","surface":"#eef2ff"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["미드나잇", "문라이트", "야간러닝"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'logo-kkumz-306',
    'idea-kkumz-003',
    '하트비트 페이서 크레스트',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#881337"/><path d="M7 20H14L17 12L22 28L25 20H33" stroke="#fda4af" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" fill="none"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#881337">SEOUL RUN CLUB</text></svg>',
    '{"primary":"#be123c","secondary":"#4c0519","accent":"#fb7185","surface":"#fff1f2"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["심박수페이스", "크림슨레드", "열정크루"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'logo-kkumz-401',
    'idea-kkumz-004',
    '로스팅 빈 엠블럼',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#451a03"/><ellipse cx="20" cy="20" rx="10" ry="13" fill="#d97706"/><path d="M20 8C17 14 23 26 20 32" stroke="#451a03" stroke-width="2.2" stroke-linecap="round"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#451a03">BEAN CHRONICLE</text></svg>',
    '{"primary":"#78350f","secondary":"#b45309","accent":"#d97706","surface":"#fffbeb"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["스페셜티로스터리", "에스프레소브라운", "매거진스토어"]'::jsonb,
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'logo-kkumz-402',
    'idea-kkumz-004',
    '푸어오버 드리퍼 라인 마크',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#292524"/><polygon points="10,11 30,11 23,25 17,25" stroke="#f59e0b" stroke-width="2.2" fill="none"/><line x1="14" y1="29" x2="26" y2="29" stroke="#fbbf24" stroke-width="2.5" stroke-linecap="round"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#292524">BEAN CHRONICLE</text></svg>',
    '{"primary":"#292524","secondary":"#57534e","accent":"#d97706","surface":"#fafaf9"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["푸어오버", "모던스톤", "핸드드립"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'logo-kkumz-403',
    'idea-kkumz-004',
    '테루아 마운틴 & 선 배지',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#14532d"/><circle cx="26" cy="14" r="4" fill="#fbbf24"/><path d="M8 29L17 16L24 24L29 19L33 29H8Z" fill="#86efac"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#14532d">BEAN CHRONICLE</text></svg>',
    '{"primary":"#14532d","secondary":"#166534","accent":"#d97706","surface":"#f7fee7"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["고산지농장", "다이렉트트레이드", "보태니컬"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'logo-kkumz-404',
    'idea-kkumz-004',
    '에디토리얼 아카이브 북 심볼',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#1e293b"/><path d="M11 13H19V28H11V13ZM21 13H29V28H21V13Z" stroke="#fdba74" stroke-width="2" fill="none"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#1e293b">BEAN CHRONICLE</text></svg>',
    '{"primary":"#1e293b","secondary":"#475569","accent":"#ea580c","surface":"#f8fafc"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["매거진아카이브", "슬레이트네이비", "저널리즘"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'logo-kkumz-405',
    'idea-kkumz-004',
    '스팀 아로마 웨이브 실드',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#7c2d12"/><path d="M15 27C13 22 17 18 15 13M20 27C18 22 22 18 20 13M25 27C23 22 27 18 25 13" stroke="#fed7aa" stroke-width="2.2" stroke-linecap="round" fill="none"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#7c2d12">BEAN CHRONICLE</text></svg>',
    '{"primary":"#7c2d12","secondary":"#9a3412","accent":"#f97316","surface":"#fff7ed"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["로스팅아로마", "웜테라코타", "크래프트"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'logo-kkumz-406',
    'idea-kkumz-004',
    '게이샤 블라썸 크레스트',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#4c0519"/><circle cx="20" cy="20" r="9" stroke="#fda4af" stroke-width="2.2" fill="none"/><circle cx="20" cy="20" r="3.5" fill="#fb7185"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#4c0519">BEAN CHRONICLE</text></svg>',
    '{"primary":"#881337","secondary":"#be123c","accent":"#f43f5e","surface":"#fff1f2"}'::jsonb,
    '{"fontHeading":"Pretendard","fontBody":"Pretendard","weightHeading":"800"}'::jsonb,
    '["게이샤플로럴", "버건디로즈", "부티크로스터리"]'::jsonb,
    FALSE,
    CURRENT_TIMESTAMP - INTERVAL '2 days'
);

-- Screen Blueprint Specifications (kkumz_screen_spec)
INSERT INTO kkumz_screen_spec (
    id, idea_id, screen_key, screen_title, route_path, layout_type, description, components_json, sort_no, created_at
) VALUES
('scr-kkumz-101', 'idea-kkumz-001', 'HOME_HERO', '보태니컬 다도 히어로 섹션', '/', 'EDITORIAL_BOTANICAL', '제주 첫물 세레모니얼 말차와 수제 백자 다완 스토리텔링 메인 배너 및 추천 큐레이션', '["BrandStoryBanner","MatchaRitualSteps","FeaturedProductGrid"]'::jsonb, 1, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('scr-kkumz-102', 'idea-kkumz-001', 'CATALOG', '말차 등급 및 다구 카탈로그', '/catalog', 'FILTERABLE_3COL_GRID', '세레모니얼 등급별 말차, 이천 도예 다완, 100본립 차선 필터 및 장바구니 담기', '["CategoryTabs","TasteNoteBadge","QuickCartDrawer"]'::jsonb, 2, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('scr-kkumz-103', 'idea-kkumz-001', 'CHECKOUT', '기프트 패키징 및 즉시 결제', '/checkout', 'ONE_PAGE_CHECKOUT', '보자기 선물 포장 선택 및 AI 티 소믈리에 추천 연동 원페이지 주문서', '["GiftWrapSelector","OrderForm","AiTeaSommelierWidget"]'::jsonb, 3, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('scr-kkumz-201', 'idea-kkumz-002', 'HOME_HERO', '다크 테크 워크스테이션 히어로', '/', 'DARK_TECH_GRID', '듀얼 모니터 & 맥북 프로 셋업 쇼케이스 및 알루미늄 기어 하이라이트', '["OrbitSetupShowcase","SpecHighlightStrip","BestGearGrid"]'::jsonb, 1, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('scr-kkumz-202', 'idea-kkumz-002', 'CATALOG', '모듈형 기어 카탈로그', '/catalog', 'SPEC_COMPARISON_GRID', '책상 규격 호환성 필터 및 Qi2 충전 독·데스크 쉘프 비교 카탈로그', '["CompatibilityFilter","ProductCard","CartSidebar"]'::jsonb, 2, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('scr-kkumz-203', 'idea-kkumz-002', 'CHECKOUT', '데스크 풀셋업 번들 주문서', '/checkout', 'ONE_PAGE_CHECKOUT', '케이블 정리 기어 번들 할인 적용 및 당일 출고 익스프레스 결제', '["ShippingAddressForm","CouponApplier","InstantOrderButton"]'::jsonb, 3, CURRENT_TIMESTAMP - INTERVAL '4 days'),
('scr-kkumz-301', 'idea-kkumz-003', 'CMS_HERO', '시티 나이트 런 비주얼 배너', '/', 'MAGAZINE_HERO', '주간 나이트 런 일정 안내 및 한강 코스 하이라이트 배너', '["WeeklySessionBanner","CrewMetricsBar"]'::jsonb, 1, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('scr-kkumz-302', 'idea-kkumz-003', 'ARTICLE_FEED', '한강 러닝 코스 아카이브 매거진', '/articles', 'EDITORIAL_CARDS', '반포·성수·여의도 급수대 및 노면 가이드 아티클 피드', '["CourseArticleList","PaceCalculatorCard"]'::jsonb, 2, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('scr-kkumz-303', 'idea-kkumz-003', 'APPLY_FORM', '게스트 런 & 크루 합류 신청 폼', '/apply', 'DYNAMIC_CMS_FORM', '희망 러닝 페이스 및 세션 참가 온라인 신청 폼', '["SessionSelectField","PaceRadioGroup","SubmitApplicationButton"]'::jsonb, 3, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('scr-kkumz-401', 'idea-kkumz-004', 'HYBRID_HERO', '로스터리 저널 & 이달의 원두 히어로', '/', 'HYBRID_EDITORIAL_COMMERCE', '산지 농장 인터뷰 아티클과 이달의 싱글오리진 원두 셀렉션 통합 메인', '["FeaturedStoryHero","RoastProfileBadge","AiBrewingConcierge"]'::jsonb, 1, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('scr-kkumz-402', 'idea-kkumz-004', 'SHOP_CATALOG', '싱글오리진 원두 & 브루잉 기어 스토어', '/store', 'FILTERABLE_3COL_GRID', '게이샤 홀빈, 질소충전 드립백, 온도조절 드립포트 카탈로그 및 즉시 주문', '["CommerceCatalogSection","RoastLevelFilter","InstantCheckoutModal"]'::jsonb, 2, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('scr-kkumz-403', 'idea-kkumz-004', 'JOURNAL_AND_CLASS', '산지 직교역 매거진 & 커핑 클래스 예약', '/journal', 'EDITORIAL_MAGAZINE_LAYOUT', '농장 직교역 인터뷰 저널 열람 및 주말 성수동 커핑 세션 온라인 예약 폼', '["EditorialJournalSection","ClassBookingForm"]'::jsonb, 3, CURRENT_TIMESTAMP - INTERVAL '2 days');

-- Deployed Kkumz Multi-Sites (kkumz_site)
INSERT INTO kkumz_site (
    id, idea_id, site_slug, site_name, site_type, category, brand_slogan, logo_svg,
    shop_site_id, cms_site_id, deploy_mode, active_version, published_version, published_at,
    deployed_url, custom_domain, is_featured, visibility,
    like_count, comment_count, view_count, owner_username, owner_display_name, status, created_at, updated_at
) VALUES
(
    'site-kkumz-001',
    'idea-kkumz-001',
    'matcha-atelier',
    '말차 아뜰리에 제주',
    'COMMERCE',
    'FOOD_BEVERAGE',
    '제주 화산암반수와 해풍이 길러낸 첫물 유기농 차광 말차 & 수제 다기 아뜰리에',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#14532d"/><circle cx="20" cy="20" r="11" fill="#86efac" fill-opacity="0.25"/><path d="M13 24C13 17 20 12 27 15C26 22 20 27 13 24Z" fill="#4ade80"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#14532d">MATCHA ATELIER</text></svg>',
    2,
    NULL,
    'CUSTOM_BUNDLE',
    2,
    2,
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    '/s/matcha-atelier',
    'matcha-atelier.kkumz.io',
    TRUE,
    'PUBLIC',
    148,
    12,
    1890,
    'soyeon.jeju',
    '한소연 대표',
    'ACTIVE',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
),
(
    'site-kkumz-002',
    'idea-kkumz-002',
    'desk-orbit',
    '데스크 오빗',
    'COMMERCE',
    'TECH_HARDWARE',
    '몰입의 궤도를 설계하는 모듈형 알루미늄 데스크테리어 & 매그세이프 테크 기어',
    '<svg viewBox="0 0 150 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#1e1b4b"/><ellipse cx="20" cy="20" rx="13" ry="6" transform="rotate(-25 20 20)" stroke="#818cf8" stroke-width="2.5" fill="none"/><circle cx="20" cy="20" r="4" fill="#38bdf8"/><text x="50" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="14" fill="#1e1b4b">DESK ORBIT</text></svg>',
    3,
    NULL,
    'DYNAMIC_SDUI',
    2,
    2,
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    '/s/desk-orbit',
    'desk-orbit.kkumz.io',
    TRUE,
    'PUBLIC',
    215,
    18,
    2640,
    'minjun.maker',
    '박민준 메이커',
    'ACTIVE',
    CURRENT_TIMESTAMP - INTERVAL '4 days',
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
),
(
    'site-kkumz-003',
    'idea-kkumz-003',
    'seoul-run-club',
    '서울 나이트 런 클럽',
    'CMS_SITE',
    'COMMUNITY_MEDIA',
    '서울의 밤을 가르는 페이스, 함께 달릴 때 더 멀리 가는 시티 러닝 커뮤니티',
    '<svg viewBox="0 0 170 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#0f172a"/><path d="M9 26L18 14L23 21L31 12" stroke="#f97316" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/><circle cx="31" cy="12" r="3" fill="#fb923c"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#0f172a">SEOUL RUN CLUB</text></svg>',
    NULL,
    101,
    'DYNAMIC_SDUI',
    1,
    1,
    CURRENT_TIMESTAMP - INTERVAL '3 hours',
    '/s/seoul-run-club',
    'seoul-run-club.kkumz.io',
    FALSE,
    'PUBLIC',
    176,
    15,
    2110,
    'jihoon.run',
    '정지훈 크루장',
    'ACTIVE',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '3 hours'
),
(
    'site-kkumz-004',
    'idea-kkumz-004',
    'bean-chronicle',
    '빈 크로니클 서울',
    'HYBRID',
    'LIFESTYLE_HYBRID',
    '한 잔의 커피에 담긴 산지의 연대기 — 로스터리 저널 & 스페셜티 셀렉트 샵',
    '<svg viewBox="0 0 175 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#451a03"/><ellipse cx="20" cy="20" rx="10" ry="13" fill="#d97706"/><path d="M20 8C17 14 23 26 20 32" stroke="#451a03" stroke-width="2.2" stroke-linecap="round"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-weight="800" font-size="13" fill="#451a03">BEAN CHRONICLE</text></svg>',
    4,
    102,
    'CUSTOM_BUNDLE',
    1,
    1,
    CURRENT_TIMESTAMP - INTERVAL '30 minutes',
    '/s/bean-chronicle',
    'bean-chronicle.kkumz.io',
    TRUE,
    'PUBLIC',
    192,
    14,
    2380,
    'hajun.roast',
    '최하준 헤드로스터',
    'ACTIVE',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes'
),
(
    'site-kkumz-005',
    'idea-01137479',
    'malgeunmul-minbak',
    '맑은물 민박',
    'HYBRID',
    '숙박 · 민박 · 프라이빗 스테이',
    '공주 청정 냇가 물소리와 따뜻한 황토 온돌이 맞이하는 사계절 시골 쉼터',
    '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#0f766e"/><path d="M8 25 C13 19, 18 19, 23 25 C26 28, 30 28, 33 23" stroke="#99f6e4" stroke-width="2.5" fill="none" stroke-linecap="round"/><path d="M12 18 L20 11 L28 18" stroke="#ffffff" stroke-width="2.2" fill="none" stroke-linecap="round" stroke-linejoin="round"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-size="15" font-weight="800" fill="#0f766e">맑은물 민박</text></svg>',
    NULL,
    NULL,
    'DYNAMIC_SDUI',
    1,
    1,
    CURRENT_TIMESTAMP - INTERVAL '15 minutes',
    '/s/malgeunmul-minbak',
    'malgeunmul-minbak.kkumz.io',
    TRUE,
    'PUBLIC',
    164,
    9,
    1720,
    'admin',
    '김도현 총괄관리자',
    'ACTIVE',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '15 minutes'
);

-- 2-Track Releases & WebIDE Snapshots (kkumz_site_release)
INSERT INTO kkumz_site_release (
    id, site_id, version_no, release_note, deploy_mode, design_tokens_json, layout_schema_json,
    webide_files_json, minio_bundle_path, build_status, build_log, created_by, created_at
) VALUES
(
    'rel-kkumz-001-v1',
    'site-kkumz-001',
    1,
    '초기 멀티사이트 프로비저닝 및 즉시 디자인 스키마 배포',
    'DYNAMIC_SDUI',
    '{"primary":"#14532d","secondary":"#16a34a","accent":"#ca8a04","surface":"#f7fee7","radius":"14px"}'::jsonb,
    '{"heroTitle":"제주 첫물 유기농 차광 말차의 깊은 여운","heroSubtitle":"서귀포 단일 다원에서 수확한 1번 차광 세레모니얼 등급 말차와 수제 백자 다완 컬렉션","ctaText":"햇차 컬렉션 살펴보기","showFlashDeal":true,"showAiConcierge":true}'::jsonb,
    '[
      {"path":"src/App.vue","language":"vue","content":"<template>\n  <div class=\"matcha-store\">\n    <HeroSection :tokens=\"tokens\" />\n    <CatalogSection :products=\"products\" />\n  </div>\n</template>"},
      {"path":"src/theme.json","language":"json","content":"{\n  \"primary\": \"#14532d\",\n  \"secondary\": \"#16a34a\",\n  \"accent\": \"#ca8a04\",\n  \"surface\": \"#f7fee7\"\n}"}
    ]'::jsonb,
    'minio://kkumz-sites/matcha-atelier/v1/schema.json',
    'SUCCESS',
    '[10:12:01] [Instant-Deploy] Validating JSON layout schema and design tokens... OK\n[10:12:02] [Commerce-Link] Bound store catalog (4 products, 3 categories, 2 banners)... OK\n[10:12:02] [Publish] Live route /s/matcha-atelier activated (v1).',
    'soyeon.jeju',
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'rel-kkumz-001-v2',
    'site-kkumz-001',
    2,
    '보태니컬 에디토리얼 아키타입 및 제주 다도 리추얼 스토어 개편 (커스텀 번들)',
    'CUSTOM_BUNDLE',
    '{"primary":"#14532d","secondary":"#15803d","accent":"#b45309","surface":"#f7f5ee","radius":"6px","surfaceMode":"WARM","fontPreset":"SERIF_EDITORIAL"}'::jsonb,
    '{
      "themeArchetype":"EDITORIAL_BOTANICAL",
      "heroStyle":"EDITORIAL_STORY",
      "heroTitle":"제주 화산토가 길러낸 첫물 차광 말차와 백자 다완의 고요한 의식",
      "heroSubtitle":"서귀포 남원읍 단일 유기농 다원에서 21일간 빛을 가려 수확한 첫물 찻잎을 화강암 맷돌로 시간당 30g씩 저온 분쇄했습니다.",
      "heroBannerImage":"/images/studio/banner-matcha-jeju.jpg",
      "heroBannerCaption":{"title":"2026 서귀포 1번 차광 세레모니얼 햇차","sub":"유기농 단일 다원 · 화강암 석구 저온 분쇄"},
      "ctaText":"첫물 말차 & 다구 컬렉션",
      "showFlashDeal":true,
      "showAiConcierge":true,
      "showCmsArticles":true,
      "showCmsForm":false,
      "customBadge":"2026 제주 첫물 세레모니얼 햇차 오픈",
      "sectionOrder":["CATALOG_GRID","STEP_GUIDE","EDITORIAL_FEED","REVIEWS_POLL","FAQ_LOCATION"],
      "copyOverrides":{
        "navCatalogLabel":"말차 · 다구",
        "navEditorialLabel":"다원 스토리",
        "navFaqLabel":"보관 · 배송 FAQ",
        "catalogKicker":"JEJU CEREMONIAL ARCHIVE",
        "catalogTitle":"세레모니얼 말차 & 수제 다기 컬렉션",
        "catalogSubtitle":"격불용 최상급 햇차부터 이천 도예가 물레 백자 다완, 보자기 기프트 세트까지 큐레이션했습니다.",
        "productPriceLabel":"공식 스토어가",
        "productCtaLabel":"상세 보기 · 구매",
        "stepKicker":"5-MINUTE TEA RITUAL",
        "stepTitle":"바쁜 아침 5분, 완벽한 말차 격불 리추얼",
        "stepSubtitle":"물 온도 75℃와 100본립 대나무 차선으로 에메랄드빛 벨벳 거품을 완성하는 정통 가이드",
        "editorialKicker":"SEOGWIPO TEA JOURNAL",
        "editorialTitle":"제주 다원 아카이브 & 다도 에세이",
        "reviewKicker":"VERIFIED TEA LOVER REVIEWS",
        "reviewTitle":"실구매자 & 티 소믈리에 시음 후기",
        "faqKicker":"SHIPPING & STORAGE GUIDE",
        "faqTitle":"신선 배송 · 보관 · 기프트 포장 안내",
        "conciergeTitle":"AI 티 소믈리에 컨시어지",
        "conciergeSubtitle":"라떼용과 격불용의 차이, 다완 입문 구성, 보자기 선물 포장까지 취향에 맞춰 추천해 드립니다.",
        "conciergePresets":["격불 입문자에게 어울리는 첫물 말차와 다완 세트를 추천해 주세요","아이스 말차 라떼용 농축 베이스와 틴캔 파우더의 차이는 무엇인가요?","기업 답례품 보자기 패키징 주문 조건이 궁금합니다"]
      },
      "featureBlocks":{
        "trustMetrics":[
          {"label":"단일 유기농 다원","value":"제주 서귀포","sub":"화산암반수 · 21일 전통 차광 재배"},
          {"label":"화강암 맷돌 분쇄","value":"30g / 시간","sub":"산화 없는 저온 저속 미세 분쇄"},
          {"label":"실구매자 만족도","value":"4.97 / 5.0","sub":"티 소믈리에 및 홈다도 고객 실후기"},
          {"label":"산지 직송 포장","value":"보자기 기프트","sub":"전 상품 냉장 신선 포장 및 선물 지원"}
        ],
        "stepGuide":[
          {"step":"RITUAL 01","time":"75℃ 온수 예열","title":"백자 다완 데우기 & 차선 적시기","desc":"이천 수제 백자 다완에 따뜻한 물을 부어 온기를 채우고, 100본립 대나무 차선의 끝을 부드럽게 풀어줍니다."},
          {"step":"RITUAL 02","time":"2g 고운 체치기","title":"첫물 세레모니얼 말차 2스쿱","desc":"산화를 막은 틴캔에서 에메랄드빛 말차 2g(차시 2스쿱)을 고운 체에 내려 뭉침 없이 다완에 담습니다."},
          {"step":"RITUAL 03","time":"W자 20초 격불","title":"미세 크림 벨벳 거품 완성","desc":"75℃ 정수 60ml를 붓고 손목 스냅으로 20초간 빠르게 W자로 격불해 쫀쫀하고 고운 거품을 올립니다."}
        ],
        "policyRules":[
          "서귀포 유기농 다원 출고센터에서 평일 오후 2시 이전 주문 시 당일 항공택배 출고됩니다.",
          "빛과 습기에 민감한 첫물 말차 특성상 개봉 후 밀봉 냉장 보관을 권장하며 60일 이내 음용 시 최상의 향미를 느낄 수 있습니다.",
          "수제 백자 다완은 배송 중 파손 시 사진 확인만으로 100% 무상 맞교환 발송해 드립니다."
        ]
      }
    }'::jsonb,
    '[
      {"path":"src/App.vue","language":"vue","content":"<template>\n  <div class=\"matcha-atelier-shell\" :style=\"{ ''--brand-primary'': tokens.primary }\">\n    <HeroSection :title=\"layout.heroTitle\" :subtitle=\"layout.heroSubtitle\" :badge=\"layout.customBadge\" />\n    <CatalogSection :products=\"products\" />\n  </div>\n</template>"},
      {"path":"src/CustomStyles.css","language":"css","content":".sf-root { letter-spacing: -0.015em; }\n.sf-editorial-rule { border-color: rgba(20, 83, 45, 0.16); }"},
      {"path":"src/theme.json","language":"json","content":"{\n  \"primary\": \"#14532d\",\n  \"secondary\": \"#15803d\",\n  \"accent\": \"#b45309\",\n  \"surface\": \"#f7f5ee\",\n  \"radius\": \"6px\",\n  \"surfaceMode\": \"WARM\",\n  \"fontPreset\": \"SERIF_EDITORIAL\"\n}"}
    ]'::jsonb,
    'minio://kkumz-sites/matcha-atelier/v2/dist/index.js',
    'SUCCESS',
    '[14:20:10] [Bundle-Builder] Compiling Matcha Atelier Editorial Botanical bundle (App.vue, CustomStyles.css, theme.json)...\n[14:20:12] [Publish] Promoted v2 to active release for /s/matcha-atelier.',
    'soyeon.jeju',
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
),
(
    'rel-kkumz-002-v1',
    'site-kkumz-002',
    1,
    '데스크 오빗 초기 프로비저닝 및 다크 테크 테마 스키마 배포',
    'DYNAMIC_SDUI',
    '{"primary":"#6366f1","secondary":"#38bdf8","accent":"#22d3ee","surface":"#09090b","radius":"10px","surfaceMode":"DARK","fontPreset":"TECH_MONO"}'::jsonb,
    '{"themeArchetype":"DARK_TECH_SHOWCASE","heroStyle":"PROMO_COMMERCE","heroTitle":"Design Your Focus Orbit","heroSubtitle":"CNC 풀알루미늄 Qi2 충전 독과 북미산 호두나무 마그네틱 데스크 쉘프로 완성하는 무결점 워크스페이스","heroBannerImage":"/images/studio/prod-walnut-shelf.jpg","ctaText":"데스크 기어 컬렉션 보기","showFlashDeal":true,"showAiConcierge":true,"showCmsForm":false}'::jsonb,
    '[
      {"path":"src/App.vue","language":"vue","content":"<template>\n  <div class=\"desk-orbit-app\">\n    <HeroSection />\n    <CatalogSection />\n  </div>\n</template>"},
      {"path":"src/theme.json","language":"json","content":"{\n  \"primary\": \"#6366f1\",\n  \"secondary\": \"#38bdf8\",\n  \"accent\": \"#22d3ee\",\n  \"surface\": \"#09090b\",\n  \"surfaceMode\": \"DARK\"\n}"}
    ]'::jsonb,
    'minio://kkumz-sites/desk-orbit/v1/schema.json',
    'SUCCESS',
    '[11:05:00] [Instant-Deploy] Bound store catalog (4 products, 3 categories)... OK',
    'minjun.maker',
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'rel-kkumz-002-v2',
    'site-kkumz-002',
    2,
    '다크 테크 쇼케이스 아키타입 & 하드웨어 스펙 비교 매트릭스 적용',
    'DYNAMIC_SDUI',
    '{"primary":"#6366f1","secondary":"#38bdf8","accent":"#22d3ee","surface":"#09090b","radius":"10px","surfaceMode":"DARK","fontPreset":"TECH_MONO"}'::jsonb,
    '{
      "themeArchetype":"DARK_TECH_SHOWCASE",
      "heroStyle":"PROMO_COMMERCE",
      "heroTitle":"ZERO CABLE. PURE FOCUS ORBIT.",
      "heroSubtitle":"항공 등급 6061 CNC 통절삭 알루미늄 Qi2 충전 스테이션과 마그네틱 월넛 데스크 쉘프. 개발자와 디자이너의 워크스테이션을 단 하나의 궤도로 정렬합니다.",
      "heroBannerImage":"/images/studio/prod-walnut-shelf.jpg",
      "heroBannerCaption":{"title":"DESK ORBIT 2026 MODULAR WORKSTATION","sub":"6061 CNC ALUMINUM · QI2 15W CERTIFIED · FAS WALNUT"},
      "ctaText":"모듈러 기어 라인업 탐색",
      "showFlashDeal":true,
      "showAiConcierge":true,
      "showCmsArticles":true,
      "showCmsForm":false,
      "customBadge":"QI2 OFFICIAL CERTIFIED · 성수 랩 당일 출고",
      "sectionOrder":["CATALOG_GRID","SPEC_COMPARISON","EDITORIAL_FEED","REVIEWS_POLL","FAQ_LOCATION"],
      "copyOverrides":{
        "navCatalogLabel":"하드웨어 라인업",
        "navSpecLabel":"스펙 · 호환성",
        "navEditorialLabel":"셋업 가이드",
        "navFaqLabel":"기술 지원 FAQ",
        "catalogKicker":"PRECISION CNC HARDWARE LINEUP",
        "catalogTitle":"모듈형 데스크테리어 & 워크스테이션 기어",
        "catalogSubtitle":"네오디뮴 마그네틱 레일 규격으로 상호 호환되는 4대 핵심 하드웨어 라인업",
        "productPriceLabel":"공식 리테일가",
        "productCtaLabel":"스펙 확인 · 주문",
        "specKicker":"HARDWARE ENGINEERING MATRIX",
        "specTitle":"제품 라인업 핵심 사양 & 호환성 비교표",
        "specSubtitle":"책상 가로 폭(1200~1600mm)과 보유 디바이스 규격에 맞춘 정밀 엔지니어링 제원",
        "editorialKicker":"WORKSTATION SETUP LAB",
        "editorialTitle":"개발자 · 크리에이터 데스크 셋업 리포트",
        "reviewKicker":"VERIFIED MAKER & ENGINEER REVIEWS",
        "reviewTitle":"현업 개발자 · 디자이너 실사용 리뷰",
        "faqKicker":"COMPATIBILITY & FIRMWARE FAQ",
        "faqTitle":"호환 규격 · QMK/VIA · 보증 안내",
        "conciergeTitle":"AI 데스크 셋업 아키텍트",
        "conciergeSubtitle":"책상 크기, 모니터 대수, 보유 기기(MacBook/iPhone/Galaxy)를 알려주시면 최적의 모듈 조합을 설계해 드립니다.",
        "conciergePresets":["1400mm 책상에 듀얼 27인치 모니터 셋업 추천 조합 알려주세요","OrbitCube Qi2가 갤럭시 S25와 아이폰 16 프로 모두 15W 지원하나요?","OrbitPad QMK/VIA 매크로 패드 단축키 설정 방법이 궁금해요"]
      },
      "featureBlocks":{
        "trustMetrics":[
          {"label":"바디 가공 정밀도","value":"6061 CNC","sub":"항공 등급 통절삭 아노다이징 알루미늄"},
          {"label":"무선 충전 인증","value":"Qi2 15W","sub":"스마트폰·워치·이어버드 3-in-1 동시 고속"},
          {"label":"광학 색재현 지수","value":"Ra 97+","sub":"화면 반사 Zero 비대칭 듀얼 광원"},
          {"label":"펌웨어 커스텀","value":"QMK / VIA","sub":"웹 브라우저 실시간 레이어 키매핑"}
        ],
        "specComparison":{
          "title":"데스크 오빗 4대 코어 기어 제원 비교",
          "columns":["모델명","핵심 소재","출력 / 규격","호환성 & 특징","권장 셋업"],
          "rows":[
            {"tier":"OrbitCube Qi2 3-in-1","spec1":"CNC 6061 알루미늄","spec2":"Qi2 15W + 45W GaN","spec3":"iPhone 12~16 / Galaxy Qi2","note":"무선 충전 케이블 3선 통합"},
            {"tier":"Walnut Shelf 1200mm","spec1":"북미산 FAS 월넛 + 4T 스틸","spec2":"1200 × 240 × 110mm","spec3":"하단 마그네틱 모듈 레일","note":"32인치 듀얼 모니터 거치"},
            {"tier":"HaloBar Pro 스크린바","spec1":"애노다이징 알루미늄","spec2":"2700K~6500K / Ra97","spec3":"1000R 커브드·울트라와이드","note":"무선 알루미늄 다이얼 포함"},
            {"tier":"OrbitPad 9Key Macro","spec1":"CNC 블랙 알루미늄","spec2":"9키 + 듀얼 노브 + OLED","spec3":"QMK / VIA 핫스왑 스위치","note":"IDE · Figma · 편집 단축키"}
          ]
        }
      }
    }'::jsonb,
    '[
      {"path":"src/App.vue","language":"vue","content":"<template>\n  <div class=\"desk-orbit-app\">\n    <HeroSection :badge=\"layout.customBadge\" />\n    <CatalogSection />\n  </div>\n</template>"},
      {"path":"src/theme.json","language":"json","content":"{\n  \"primary\": \"#6366f1\",\n  \"secondary\": \"#38bdf8\",\n  \"accent\": \"#22d3ee\",\n  \"surface\": \"#09090b\",\n  \"surfaceMode\": \"DARK\",\n  \"fontPreset\": \"TECH_MONO\"\n}"}
    ]'::jsonb,
    'minio://kkumz-sites/desk-orbit/v2/schema.json',
    'SUCCESS',
    '[16:40:12] [Chat-Refine] Applied Dark Tech Showcase archetype & hardware spec comparison matrix. Live in 45ms.',
    'minjun.maker',
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
),
(
    'rel-kkumz-003-v1',
    'site-kkumz-003',
    1,
    '커뮤니티 키네틱 아키타입 · 주간 나이트 런 일정표 및 한강 코스 매거진 배포',
    'DYNAMIC_SDUI',
    '{"primary":"#ea580c","secondary":"#0f172a","accent":"#f59e0b","surface":"#ffffff","radius":"12px","surfaceMode":"LIGHT","fontPreset":"BOLD_SANS"}'::jsonb,
    '{
      "themeArchetype":"COMMUNITY_KINETIC",
      "heroStyle":"FULL_SCENIC",
      "heroTitle":"SEOUL NIGHT RUNNERS — 매주 화·목 서울의 밤을 달립니다",
      "heroSubtitle":"반포 잠수교 · 성수 서울숲 · 여의도 한강공원. 530부터 630까지 전담 페이서와 함께 달리는 2030 시티 러닝 커뮤니티.",
      "heroBannerImage":"/images/studio/banner-seoul-night-run.jpg",
      "heroBannerCaption":{"title":"THIS WEEK SESSION · 반포 잠수교 7.5K 템포런","sub":"매주 화·목 20:00 · 530 / 600 / 630 페이스 그룹 운영"},
      "ctaText":"이번 주 게스트 런 신청하기",
      "showCmsArticles":true,
      "showCmsForm":true,
      "showPhotoGallery":true,
      "customBadge":"매주 화·목 20:00 정규 세션 · 게스트 오픈 런",
      "sectionOrder":["SESSION_TIMETABLE","EDITORIAL_FEED","SPACE_TOUR_GALLERY","REVIEWS_POLL","DYNAMIC_FORM","FAQ_LOCATION"],
      "copyOverrides":{
        "navTimetableLabel":"정규 세션 일정",
        "navEditorialLabel":"한강 코스 가이드",
        "navGalleryLabel":"코스 스폿 투어",
        "navFormLabel":"게스트 런 신청",
        "navFaqLabel":"참가 FAQ · 집결지",
        "timetableKicker":"WEEKLY RUNNING SCHEDULE",
        "timetableTitle":"이번 주 정규 나이트 런 & 주말 LSD 세션",
        "timetableSubtitle":"원하는 요일과 페이스 그룹을 클릭하면 하단 게스트 런 신청 폼에 자동으로 선택됩니다.",
        "editorialKicker":"SEOUL COURSE ARCHIVE & TRAINING",
        "editorialTitle":"한강 러닝 코스 아카이브 & 트레이닝 칼럼",
        "formKicker":"GUEST RUN & CREW APPLICATION",
        "conciergeTitle":"AI 러닝 크루 페이스 코치",
        "conciergeSubtitle":"초보 러너 페이스 배정, 짐보관 위치, 반포·성수 코스 노면 정보까지 바로 안내해 드립니다.",
        "conciergePresets":["5K 완주가 처음인데 어떤 페이스 그룹으로 신청하면 좋을까요?","화요일 반포 잠수교 세션 집결지와 짐보관 방법을 알려주세요","정규 크루원 합류 조건과 운영진 모집 포지션이 궁금해요"]
      },
      "featureBlocks":{
        "trustMetrics":[
          {"label":"활성 크루 멤버","value":"1,240+","sub":"서울 2030 시티 러너 커뮤니티"},
          {"label":"주간 정규 세션","value":"화·목·토","sub":"전담 페이서 3개 그룹 동시 운영"},
          {"label":"누적 러닝 거리","value":"48,500 km","sub":"2026 시즌 크루 합산 마일리지"},
          {"label":"게스트 재참여율","value":"92%","sub":"초보 러너 환영 리커버리 그룹 운영"}
        ],
        "sessionTimetable":[
          {"day":"매주 화요일","time":"20:00 ~ 21:30","title":"반포 잠수교 7.5K 나이트 템포런","location":"반포한강공원 달빛광장 세빛섬 앞","pace":"530 / 600 / 630 그룹","capacity":"선착순 40명","badge":"정규 메인"},
          {"day":"매주 목요일","time":"20:00 ~ 21:15","title":"성수 서울숲 ~ 뚝섬 5K 리커버리런","location":"서울숲역 3번 출구 보행데크 광장","pace":"600 / 630 / 700 입문","capacity":"선착순 35명","badge":"초보 추천"},
          {"day":"매주 토요일","time":"07:00 ~ 09:00","title":"여의도 ~ 양화대교 12K 마라톤 LSD","location":"여의나루역 2번 출구 물빛무대","pace":"515 / 545 / 615 그룹","capacity":"선착순 30명","badge":"풀코스 대비"}
        ],
        "spaceTour":[
          {"zone":"COURSE 01","title":"반포 잠수교 · 세빛섬 나이트 직선 주로","desc":"신호등 없이 한강 야경과 달빛무지개분수를 가르며 페이스 리듬을 맞추는 화요일 7.5K 대표 템포 코스입니다.","imageUrl":"/images/studio/run-course-banpo.jpg"},
          {"zone":"COURSE 02","title":"성수 서울숲 · 뚝섬 리커버리 그린 루트","desc":"도심 숲길과 한강 자전거 보행 분리 도로가 이어져 러닝 입문자도 편안하게 완주할 수 있는 목요일 5K 코스입니다.","imageUrl":"/images/studio/run-course-seongsu.jpg"},
          {"zone":"COURSE 03","title":"잠실 우레탄 트랙 스피드 인터벌 존","desc":"마라톤 기록 단축을 위한 400m·800m 인터벌과 러닝 드릴을 전문 코치와 함께 훈련하는 스피드 트레이닝 코스입니다.","imageUrl":"/images/studio/run-training-track.jpg"}
        ],
        "pollWidget":{
          "question":"다음 달 스페셜 선라이즈 런 희망 코스를 투표해 주세요",
          "totalVotes":318,
          "options":[
            {"label":"남산 북측순환로 업힐 8K + 브런치","votes":142,"percent":45},
            {"label":"잠실대교 ~ 광나루 한강 일출 10K","votes":108,"percent":34},
            {"label":"북악스카이웨이 야경 트레일 시티런","votes":68,"percent":21}
          ]
        },
        "faqItems":[
          {"q":"러닝 입문자도 게스트 런에 참여할 수 있나요?","a":"물론입니다. 목요일 성수 서울숲 5K 세션과 화요일 630 그룹에는 전담 후미 페이서가 함께 뛰어 초보자도 낙오 없이 완주할 수 있습니다."},
          {"q":"세션 중 짐보관과 탈의는 어떻게 하나요?","a":"정규 세션 집결지에 크루 운영진 차량 및 유인 락커 보관 구역을 운영합니다. 귀중품은 최소화해 와주시면 편리합니다."},
          {"q":"우천 시 세션 진행 여부는 언제 공지되나요?","a":"세션 시작 3시간 전(오후 5시) 시간당 강수량 3mm 이상 예보 시 안전을 위해 실내 보강 스트레칭 번개로 전환 또는 순연 공지합니다."}
        ],
        "locationGuide":{
          "address":"서울특별시 서초구 신반포로11길 40 반포한강공원 달빛광장 (화요일 메인 집결지)",
          "parking":"반포한강공원 제1·제2 공영주차장 이용 가능",
          "publicTransit":"지하철 3·7·9호선 고속터미널역 8-1번 출구 도보 12분 / 신반포역 1번 출구 도보 10분",
          "nearbySpots":[
            {"name":"반포 달빛무지개분수","distance":"집결지 바로 앞","desc":"스트레칭 및 페이스 그룹 집결·단체 촬영 포인트"},
            {"name":"잠수교 보행전용 구간","distance":"코스 2.5km 지점","desc":"신호등 없이 한강 야경을 가로지르는 핵심 직선 주로"},
            {"name":"세빛섬 러너스 급수대","distance":"코스 반환점","desc":"전담 스태프 이온음료 보급 및 쿨다운 스트레칭 존"}
          ]
        }
      }
    }'::jsonb,
    '[
      {"path":"src/App.vue","language":"vue","content":"<template>\n  <div class=\"seoul-run-cms\">\n    <RunClubHero />\n    <SessionTimetable />\n    <CourseMagazineSection />\n    <GuestRunApplyForm />\n  </div>\n</template>"},
      {"path":"src/theme.json","language":"json","content":"{\n  \"primary\": \"#ea580c\",\n  \"secondary\": \"#0f172a\",\n  \"surface\": \"#ffffff\",\n  \"surfaceMode\": \"LIGHT\",\n  \"fontPreset\": \"BOLD_SANS\"\n}"}
    ]'::jsonb,
    'minio://kkumz-sites/seoul-run-club/v1/schema.json',
    'SUCCESS',
    '[09:30:00] [Content-Link] Provisioned Community Kinetic schedule, 3 course articles and Guest Run registration form... OK',
    'jihoon.run',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'rel-kkumz-004-v1',
    'site-kkumz-004',
    1,
    '매거진 하이브리드 아키타입 · 산지 저널 + 스페셜티 원두 스토어 + 월간 구독 배포',
    'CUSTOM_BUNDLE',
    '{"primary":"#78350f","secondary":"#b45309","accent":"#d97706","surface":"#fbf8f3","radius":"8px","surfaceMode":"WARM","fontPreset":"SERIF_EDITORIAL"}'::jsonb,
    '{
      "themeArchetype":"MAGAZINE_HYBRID",
      "heroStyle":"SPLIT_VISUAL",
      "heroTitle":"산지의 떼루아부터 한 잔의 브루잉까지, 빈 크로니클 서울",
      "heroSubtitle":"콜롬비아·에티오피아 직교역 농장의 수확 기록을 담은 로스터리 저널과 성수동에서 당일 로스팅해 발송하는 이달의 마이크로랏 스토어.",
      "heroBannerImage":"/images/studio/banner-geisha-coffee.jpg",
      "heroBannerCaption":{"title":"ISSUE NO.14 · 콜롬비아 엘 파라이소 96h 게이샤","sub":"DIRECT TRADE ARCHIVE & SEONGSU ROASTERY SELECTION"},
      "ctaText":"이달의 싱글오리진 & 저널 탐색",
      "showFlashDeal":true,
      "showCmsArticles":true,
      "showCmsForm":true,
      "customBadge":"성수동 산지 직교역 로스터리 · 주문 당일 로스팅 발송",
      "sectionOrder":["EDITORIAL_FEED","CATALOG_GRID","SUBSCRIPTION_TIERS","STEP_GUIDE","REVIEWS_POLL","FAQ_LOCATION","DYNAMIC_FORM"],
      "copyOverrides":{
        "navEditorialLabel":"산지 저널",
        "navCatalogLabel":"원두 · 기어 샵",
        "navSubscriptionLabel":"월간 원두 구독",
        "navFaqLabel":"로스터리 FAQ · 쇼룸",
        "navFormLabel":"커핑 클래스 예약",
        "editorialKicker":"DIRECT TRADE ROASTERY JOURNAL · ISSUE 14",
        "editorialTitle":"산지 농장 아카이브 & 헤드로스터 추출 노트",
        "editorialSubtitle":"농부의 발효 프로토콜부터 92℃ 클린컵 브루잉 레시피까지 매월 기록합니다.",
        "catalogKicker":"SEONGSU ROASTERY SELECT SHOP",
        "catalogTitle":"이달의 마이크로랏 원두 & 정밀 브루잉 기어",
        "catalogSubtitle":"주문 당일 성수 로스터리에서 소량 배치 로스팅해 아로마 밸브 팩으로 발송합니다.",
        "productPriceLabel":"로스터리 직판가",
        "productCtaLabel":"테이스팅 노트 · 주문",
        "subscriptionKicker":"MONTHLY ROASTER SUBSCRIPTION",
        "subscriptionTitle":"매월 로스터가 선별하는 제철 싱글오리진 정기구독",
        "subscriptionSubtitle":"분쇄도와 로스팅 성향에 맞춰 매월 첫째 주 목요일 갓 볶은 마이크로랏 원두를 보내드립니다.",
        "stepKicker":"92℃ GOLDEN BREWING RECIPE",
        "stepTitle":"성수동 헤드로스터의 V60 푸어오버 3단계 가이드",
        "stepSubtitle":"원두 18g · 92℃ 정수 270ml · 총 추출 시간 2분 30초 기준 클린컵 표준 레시피",
        "reviewKicker":"VERIFIED HOME BARISTA REVIEWS",
        "reviewTitle":"홈 바리스타 & 바이어 실구매 테이스팅 후기",
        "faqKicker":"ROASTING · DEGASSING · SHOWROOM",
        "faqTitle":"로스팅 · 숙성 가이드 & 성수 쇼룸 오시는 길",
        "formKicker":"WEEKEND CUPPING & B2B SAMPLE",
        "conciergeTitle":"AI 수석 로스터 컨시어지",
        "conciergeSubtitle":"선호하는 산미·바디감·추출 도구(V60/에스프레소/드립백)에 맞는 원두와 분쇄도를 추천해 드립니다.",
        "conciergePresets":["산미가 화사하고 복숭아 향이 나는 원두를 추천해 주세요","게이샤 원두 디개싱 권장 기간과 92도 브루잉 레시피가 궁금해요","토요일 성수 로스터리 프라이빗 커핑 클래스 예약 방법을 알려주세요"]
      },
      "featureBlocks":{
        "trustMetrics":[
          {"label":"산지 직교역 농장","value":"14개 마이크로랏","sub":"콜롬비아·에티오피아·과테말라 직접 계약"},
          {"label":"로스팅 신선도","value":"주문 당일 배전","sub":"성수 로스터리 소량 프로파일 관리"},
          {"label":"누적 커핑 클래스","value":"340+ 세션","sub":"Q-Grader 헤드로스터 직접 진행"},
          {"label":"정기구독 유지율","value":"96%","sub":"매월 제철 스페셜티 큐레이션 발송"}
        ],
        "subscriptionTiers":[
          {"tier":"SINGLE ORIGIN EXPLORER","badge":"입문 추천","price":"28,000원","period":"/ 월 1회 (200g × 2종)","desc":"매월 서로 다른 산지·가공법의 제철 싱글오리진 2종과 헤드로스터 커핑 노트 카드를 받아보세요.","features":["200g × 2종 (매월 신규 마이크로랏)","홀빈 / 핸드드립 / 에스프레소 분쇄 선택","무료 배송 및 웰컴 브루잉 레시피북"]},
          {"tier":"MICROLOT COLLECTOR","badge":"BEST 인기","price":"49,000원","period":"/ 월 1회 (게이샤 포함 3종)","desc":"96시간 무산소 발효 게이샤 등 희귀 옥션 랏 1종과 시그니처 싱글오리진 2종으로 구성된 마니아 플랜입니다.","features":["희귀 게이샤·옥션 랏 우선 배정","성수 쇼룸 주말 커핑 클래스 월 1회 무료","원두 10% 상시 추가 할인 혜택"],"highlighted":true},
          {"tier":"OFFICE & CAFE B2B","badge":"사업자 전용","price":"145,000원","period":"/ 월 4kg 맞춤 로스팅","desc":"소규모 오피스 라운지나 스페셜티 카페를 위한 맞춤형 에스프레소 블렌드 및 싱글오리진 정기 납품 플랜입니다.","features":["주간/격주 분할 로스팅 당일 발송","추출 세팅 및 머신 캘리브레이션 상담","세금계산서 발행 및 샘플 3종 무상 제공"]}
        ],
        "stepGuide":[
          {"step":"BREW 01","time":"0:00 ~ 0:40","title":"18g 중배전 분쇄 & 40ml 뜸들이기","desc":"92℃ 정수 40ml를 원두 전체에 고르게 적셔 40초간 CO2 가스를 충분히 배출시킵니다."},
          {"step":"BREW 02","time":"0:40 ~ 1:45","title":"중심부 2차 푸어링 (누적 180ml)","desc":"구스넥 포트로 500원 동전 크기의 원을 그리며 140ml를 일정한 유속으로 부드럽게 추출합니다."},
          {"step":"BREW 03","time":"1:45 ~ 2:30","title":"바이패스 마무리 & 테이스팅","desc":"남은 90ml를 가볍게 부어 총 270ml를 완성한 뒤, 온도가 살짝 내려갈 때 살아나는 백도·재스민 향미를 즐깁니다."}
        ],
        "locationGuide":{
          "address":"서울특별시 성동구 서울숲2길 44 빈 크로니클 성수 로스터리 쇼룸 1층",
          "parking":"인근 성동구민종합체육센터 공영주차장 도보 3분 (유료)",
          "publicTransit":"수인분당선 서울숲역 5번 출구 도보 6분 / 2호선 뚝섬역 8번 출구 도보 8분",
          "nearbySpots":[
            {"name":"서울숲 공원 산책로","distance":"도보 3분","desc":"테이크아웃 푸어오버 한 잔과 함께 걷기 좋은 숲길"},
            {"name":"성수동 아뜰리에 거리","distance":"도보 5분","desc":"독립 서점, 세라믹 공방, 편집샵이 모인 로컬 골목"},
            {"name":"성수 로스터리 커핑 랩","distance":"쇼룸 2층","desc":"매주 토요일 14:00 프라이빗 커핑 클래스 전용 공간"}
          ]
        }
      }
    }'::jsonb,
    '[
      {"path":"src/App.vue","language":"vue","content":"<template>\n  <div class=\"bean-chronicle-hybrid\" :style=\"{ ''--brand-primary'': tokens.primary }\">\n    <EditorialSplitHero :title=\"layout.heroTitle\" :subtitle=\"layout.heroSubtitle\" :badge=\"layout.customBadge\" />\n    <DirectTradeJournalSection :articles=\"cms.articles\" />\n    <RoasteryShopSection :products=\"commerce.products\" />\n    <CuppingClassForm :form=\"cms.forms?.[0]\" />\n  </div>\n</template>"},
      {"path":"src/CustomStyles.css","language":"css","content":".sf-root { font-feature-settings: \"kern\" 1; }\n.sf-magazine-lead { border-bottom: 1px solid rgba(120, 53, 15, 0.18); }"},
      {"path":"src/theme.json","language":"json","content":"{\n  \"primary\": \"#78350f\",\n  \"secondary\": \"#b45309\",\n  \"accent\": \"#d97706\",\n  \"surface\": \"#fbf8f3\",\n  \"radius\": \"8px\",\n  \"surfaceMode\": \"WARM\",\n  \"fontPreset\": \"SERIF_EDITORIAL\"\n}"}
    ]'::jsonb,
    'minio://kkumz-sites/bean-chronicle/v1/dist/index.js',
    'SUCCESS',
    '[15:10:00] [Hybrid-Deploy] Linked specialty coffee catalog (4 products) + editorial journal (2 articles) + monthly subscription tiers + cupping form.\n[15:10:01] [Publish] Custom bundle built and published to /s/bean-chronicle.',
    'hajun.roast',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'rel-kkumz-005-v1',
    'site-kkumz-005',
    1,
    '시닉 호스피탈리티 아키타입 · 공주 냇가 독채 민박 & 시즌 요금표·스페이스 투어 배포',
    'DYNAMIC_SDUI',
    '{"primary":"#0f766e","secondary":"#115e59","accent":"#d97706","surface":"#f5f7f2","radius":"16px","surfaceMode":"WARM","fontPreset":"SERIF_EDITORIAL"}'::jsonb,
    '{
      "themeArchetype":"SCENIC_HOSPITALITY",
      "heroStyle":"FULL_SCENIC",
      "heroTitle":"공주 청정 냇가 물소리와 황토 온돌의 쉼, 맑은물 민박",
      "heroSubtitle":"충남 공주 맑은 냇가 바로 앞에 자리한 프라이빗 황토 독채와 나무 그늘 평상. 능이 토종닭 백숙과 사계절 촌캉스를 온전히 누려보세요.",
      "heroBannerImage":"/images/studio/banner-minbak-creek.jpg",
      "heroBannerCaption":{"title":"충남 공주 반포면 청정 냇가 독채 & 평상 스테이","sub":"황토 온돌 독채 · 냇가 전용 평상 · 사계절 프라이빗 휴식"},
      "ctaText":"객실 · 평상 & 패키지 보기",
      "showFlashDeal":true,
      "showPhotoGallery":true,
      "showCmsArticles":false,
      "showCmsForm":true,
      "showAiConcierge":true,
      "customBadge":"공주 청정 냇가 직결 · 공식 직예약 웰컴 혜택",
      "sectionOrder":["CATALOG_GRID","PRICING_MATRIX","SPACE_TOUR_GALLERY","STEP_GUIDE","REVIEWS_POLL","FAQ_LOCATION","DYNAMIC_FORM"],
      "copyOverrides":{
        "navCatalogLabel":"독채 · 평상 안내",
        "navPricingLabel":"시즌 요금표",
        "navGalleryLabel":"공간 투어 · 갤러리",
        "navFaqLabel":"숙박 FAQ · 오시는 길",
        "navFormLabel":"맞춤 예약 신청",
        "catalogKicker":"PRIVATE HANOK & CREEKSIDE STAY",
        "catalogTitle":"황토 온돌 독채 · 냇가 평상 · 다이닝 패키지",
        "catalogSubtitle":"가족 단위 1박 2일 독채 숙박부터 여름 냇가 평상 당일 대여와 능이 백숙 한상까지 바로 예약하세요.",
        "productPriceLabel":"공식 직예약가",
        "productCtaLabel":"상세 보기 · 예약",
        "stepKicker":"1 NIGHT 2 DAYS HEALING COURSE",
        "stepTitle":"맑은물 민박 1박 2일 추천 힐링 동선",
        "stepSubtitle":"오후 3시 입실부터 냇가 평상 휴식, 저녁 숯불 만찬, 다음 날 아침 계곡 산책까지",
        "conciergeTitle":"AI 맑은물 민박 호스트 컨시어지",
        "conciergeSubtitle":"성인·아동 인원별 요금, 냇가 물놀이 수심, 바비큐·능이백숙 사전 주문까지 24시간 안내해 드립니다.",
        "conciergePresets":["성인 4명 아이 2명 주말 황토 독채 1박 요금이 얼마인가요?","여름철 냇가 평상 당일 대여와 능이 토종닭 백숙 사전 주문이 가능한가요?","공주 KTX역에서 오시는 길과 주변 추천 관광 코스를 알려주세요"]
      },
      "customProducts":[
        {"id":501,"name":"황토 온돌 독채 스테이 (기준 4인 / 최대 6인)","subTitle":"천연 황토벽·편백 마루 독채 + 냇가 전용 데크 + 웰컴 공주 알밤 다과 제공","description":"아이 동반 가족과 부모님 효도 여행에 최적화된 프라이빗 황토 온돌 독채 객실입니다.","pic":"/images/studio/minbak-room-ondol.jpg","price":190000,"promotionPrice":165000,"originalPrice":190000,"stock":3,"sale":214,"unit":"박","productCategoryName":"황토 독채 객실","badge":"SIGNATURE","detailHtml":"<p>천연 황토와 편백 원목으로 지어진 독립 채로, 거실 통창 너머로 공주 맑은 냇가가 한눈에 펼쳐집니다. 전용 야외 바비큐 데크와 개별 화장실, 풀옵션 주방이 구비되어 있습니다.</p>"},
        {"id":502,"name":"냇가 나무 그늘 프라이빗 평상 당일 대여 (10:00~18:00)","subTitle":"계곡물 바로 옆 원목 평상 1동 + 아이스박스·취사 버너·샤워실 이용 포함","description":"숙박 없이도 여름철 시원한 냇가 물놀이와 그늘 휴식을 즐기는 당일 전용 평상 대여권입니다.","pic":"/images/studio/minbak-pyeongsang.jpg","price":80000,"promotionPrice":65000,"originalPrice":80000,"stock":6,"sale":348,"unit":"회","productCategoryName":"냇가 평상 대여","badge":"여름 인기"},
        {"id":503,"name":"공주 토종닭 능이 백숙 & 참숯 바비큐 한상 패키지 (4인 기준)","subTitle":"국내산 1등급 능이버섯 토종닭 백숙 + 텃밭 유기농 쌈채소 + 참숯 그릴 세팅","description":"도착 시간에 맞춰 가마솥에 푹 고아낸 능이 백숙과 참숯 바비큐 세트를 야외 테라스에 차려드립니다.","pic":"/images/studio/minbak-dining-bbq.jpg","price":110000,"promotionPrice":95000,"originalPrice":110000,"stock":8,"sale":186,"unit":"세트","productCategoryName":"계절 다이닝","badge":"호스트 추천"}
      ],
      "customCmsForm":{
        "id":501,
        "title":"맑은물 민박 맞춤 일정 · 객실 · 다이닝 온라인 예약",
        "formName":"맑은물 민박 온라인 예약",
        "description":"희망 체크인 날짜와 객실/평상, 인원수, 식사 옵션을 남겨주시면 호스트 확인 후 예약 확정 문자를 발송해 드립니다.",
        "submitText":"예약 신청 접수하기",
        "fields":[
          {"key":"checkInDate","label":"희망 체크인 / 방문 일자","type":"date","required":true},
          {"key":"stayType","label":"희망 객실 / 평상 타입","type":"select","required":true,"options":["황토 온돌 독채 (1박 2일)","냇가 프라이빗 평상 (당일 대여)","독채 + 냇가 평상 올인원 패키지"]},
          {"key":"guestCount","label":"방문 인원 (성인+아동)","type":"number","min":1,"max":12,"required":true},
          {"key":"diningOptions","label":"사전 식사 · 바비큐 옵션","type":"checkbox","required":false,"options":["능이 토종닭 백숙 한상 (사전 예약)","참숯 바비큐 그릴 세팅","공주 알밤 군밤 체험 키트"]}
        ]
      },
      "featureBlocks":{
        "trustMetrics":[
          {"label":"누적 가족 방문객","value":"3,400+","sub":"사계절 단골 가족이 찾는 공주 쉼터"},
          {"label":"실제 투숙객 평점","value":"4.96 / 5.0","sub":"청결도·호스트 환대 부문 최상위"},
          {"label":"냇가 접근 거리","value":"도보 10초","sub":"마당 바로 앞 청정 1급수 계곡"},
          {"label":"재방문 추천율","value":"95%","sub":"여름 물놀이 · 가을 단풍 · 겨울 설경"}
        ],
        "pricingMatrix":{
          "title":"맑은물 민박 시즌 · 요일 · 인원별 공식 요금표",
          "columns":["구분","기준 / 최대 인원","비수기 주중","비수기 주말","성수기 (7~8월)","포함 혜택"],
          "rows":[
            {"tier":"황토 온돌 독채","capacity":"기준 4인 / 최대 6인","offPeakWeekday":"150,000원","offPeakWeekend":"185,000원","peakSeason":"240,000원","note":"전용 바비큐 데크 · 공주 알밤 다과"},
            {"tier":"별채 대가족 스위트","capacity":"기준 6인 / 최대 10인","offPeakWeekday":"210,000원","offPeakWeekend":"260,000원","peakSeason":"320,000원","note":"독립 거실 2실 + 냇가 대형 평상 무료"},
            {"tier":"냇가 평상 당일 대여","capacity":"기준 4인 (10:00~18:00)","offPeakWeekday":"60,000원","offPeakWeekend":"75,000원","peakSeason":"95,000원","note":"평상 1동 · 온수 샤워실 · 주차 무료"}
          ]
        },
        "spaceTour":[
          {"zone":"ZONE 01","title":"천연 황토 온돌 독채 실내","desc":"은은한 편백 향과 뜨끈한 온돌 바닥, 냇가가 한눈에 들어오는 넓은 원목 창이 마련된 프라이빗 공간입니다.","imageUrl":"/images/studio/minbak-room-ondol.jpg"},
          {"zone":"ZONE 02","title":"청정 냇가 나무 그늘 평상존","desc":"발목~무릎 깊이의 맑은 1급수 계곡 바로 옆, 시원한 나무 그늘 아래서 아이들과 안심하고 물놀이를 즐길 수 있습니다.","imageUrl":"/images/studio/minbak-pyeongsang.jpg"},
          {"zone":"ZONE 03","title":"가을 단풍 마당 & 야외 바비큐 테라스","desc":"황금빛 은행나무와 붉은 단풍에 둘러싸인 돌마당과 저녁 참숯 바비큐·능이백숙을 즐기는 조명 테라스입니다.","imageUrl":"/images/studio/minbak-autumn-yard.jpg"},
          {"zone":"ZONE 04","title":"겨울 설경 냇가 정자 & 군밤 체험","desc":"눈이 소복이 쌓인 고요한 겨울 냇가 풍경을 감상하며 따뜻한 황토방과 공주 알밤 군밤 체험을 즐깁니다.","imageUrl":"/images/studio/minbak-winter-snow.jpg"}
        ],
        "stepGuide":[
          {"step":"STEP 01","time":"15:00 체크인","title":"도착 & 공주 알밤 웰컴 티","desc":"호스트의 안내와 함께 황토 독채에 짐을 풀고 시원한 냇가 소리를 들으며 웰컴 다과로 여정을 시작합니다."},
          {"step":"STEP 02","time":"16:00 냇가 휴식","title":"청정 계곡 물놀이 & 평상 다과","desc":"도보 10초 거리의 맑은 냇가에서 다슬기를 찾거나 나무 그늘 평상에 누워 여유로운 오후를 보냅니다."},
          {"step":"STEP 03","time":"18:30 저녁 만찬","title":"가마솥 능이백숙 & 참숯 바비큐","desc":"노을이 지는 정원 테라스에서 정성스레 준비한 능이 토종닭 백숙과 참숯 바비큐 만찬을 즐깁니다."},
          {"step":"STEP 04","time":"11:00 체크아웃","title":"아침 물안개 산책 & 공주 투어","desc":"아침 물안개가 피어오르는 계곡길을 산책한 뒤 공산성·마곡사 등 주변 명소로 이동합니다."}
        ],
        "reviews":[
          {"author":"이*진 가족 고객님","rating":5,"date":"2026.10","tag":"황토 독채 1박","comment":"아이들이 마당 바로 앞 얕은 냇가에서 다슬기를 잡으며 시간 가는 줄 몰랐습니다. 황토방이 정말 따뜻하고 침구도 호텔처럼 뽀송해서 부모님도 대만족하셨어요."},
          {"author":"박*호 가족 고객님","rating":5,"date":"2026.09","tag":"평상 + 능이백숙","comment":"나무 그늘 평상 바로 옆으로 맑은 계곡물이 흘러 에어컨이 필요 없었습니다. 미리 주문한 능이 토종닭 백숙 국물이 진국이라 가을 단풍 때 독채로 재방문 예약했습니다."},
          {"author":"정*서 고객님","rating":5,"date":"2026.10","tag":"가을 촌캉스","comment":"사진보다 단풍 든 마당과 야외 바비큐 테라스 분위기가 훨씬 아늑했습니다. 호스트 부부분이 공주 알밤도 챙겨주셔서 따뜻한 시골 정을 듬뿍 느끼고 돌아왔습니다."}
        ],
        "faqItems":[
          {"q":"입실·퇴실 시간과 얼리 체크인 가능 여부가 궁금합니다.","a":"정규 입실은 오후 3시, 퇴실은 오전 11시입니다. 당일 객실 정비 상황에 따라 사전 문의 시 1시간 내외 조율 가능합니다."},
          {"q":"아이들과 냇가 물놀이할 때 수심이 안전한가요?","a":"민박 바로 앞 여울은 성인 발목에서 무릎 정도(30~60cm)의 완만하고 맑은 수심이며, 구명조끼와 다슬기 채집망을 무료로 대여해 드립니다."},
          {"q":"능이 토종닭 백숙과 바비큐 숯불은 언제까지 예약해야 하나요?","a":"토종닭 백숙은 신선한 재료 준비를 위해 방문 최소 1일 전까지 예약 폼 또는 AI 컨시어지로 신청해 주셔야 합니다."}
        ],
        "locationGuide":{
          "address":"충청남도 공주시 반포면 계룡산청정냇가길 82 (맑은물 민박)",
          "parking":"민박 전용 무료 주차장 완비 (객실당 2대, 대형 SUV 및 카니발 주차 가능)",
          "publicTransit":"KTX 공주역에서 차량 25분 / 대전 유성시외버스터미널에서 차량 20분",
          "nearbySpots":[
            {"name":"계룡산 동학사 계곡","distance":"차량 10분","desc":"사계절 단풍과 숲길이 아름다운 명산 사찰 산책 코스"},
            {"name":"공주 공산성 & 무령왕릉","distance":"차량 18분","desc":"백제 유네스코 세계유산 야경과 전통 한옥마을 투어"},
            {"name":"공주 알밤 체험 농원","distance":"차량 12분","desc":"가을철 가족과 함께 즐기는 공주 특산 알밤 줍기 체험"}
          ]
        },
        "policyRules":[
          "쾌적한 시골 밤의 휴식을 위해 오후 10시 이후 야외 매너타임을 준수해 주시기 바랍니다.",
          "황토 목조 독채 실내 절대 금연이며, 숯불 화기는 지정된 야외 바비큐 테라스에서만 이용해 주세요.",
          "예약일 7일 전 100% 환불, 5일 전 70% 환불, 3일 전 50% 환불, 당일 취소는 환불이 불가합니다."
        ]
      }
    }'::jsonb,
    '[]'::jsonb,
    'minio://kkumz-sites/malgeunmul-minbak/v1/schema.json',
    'SUCCESS',
    '[12:00:00] [Provision] Published Malgeunmul Minbak Scenic Hospitality release v1 with pricing matrix, space tour, and seasonal gallery.',
    'admin',
    CURRENT_TIMESTAMP - INTERVAL '15 minutes'
);

-- Build Jobs History (kkumz_build_job)
INSERT INTO kkumz_build_job (
    id, site_id, release_id, job_type, deploy_mode, status, duration_ms, error_summary, ai_patch_applied, build_log, triggered_by, started_at, finished_at
) VALUES
(
    'job-kkumz-001',
    'site-kkumz-001',
    'rel-kkumz-001-v1',
    'INITIAL_PROVISION',
    'DYNAMIC_SDUI',
    'SUCCESS',
    820,
    NULL,
    FALSE,
    '[10:12:01] [Instant-Deploy] Validating JSON layout schema and design tokens... OK\n[10:12:02] [Publish] Live route /s/matcha-atelier activated (v1).',
    'soyeon.jeju',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '1 second'
),
(
    'job-kkumz-002',
    'site-kkumz-001',
    'rel-kkumz-001-v2',
    'WEBIDE_BUNDLE_BUILD',
    'CUSTOM_BUNDLE',
    'SUCCESS',
    2140,
    NULL,
    TRUE,
    '[14:20:10] [Bundle-Builder] Compiling 4 virtual SFC files...\n[14:20:11] [AI-Self-Heal] Auto-resolved missing closing tag in HeroSection.vue\n[14:20:12] [Publish] Promoted v2 to active release for /s/matcha-atelier.',
    'soyeon.jeju',
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    CURRENT_TIMESTAMP - INTERVAL '1 hour' + INTERVAL '2 seconds'
),
(
    'job-kkumz-003',
    'site-kkumz-002',
    'rel-kkumz-002-v2',
    'CHAT_HOT_RELOAD',
    'DYNAMIC_SDUI',
    'SUCCESS',
    450,
    NULL,
    FALSE,
    '[16:40:12] [Chat-Refine] Updated hero copy and activated flash deal highlight. Live in 450ms.',
    'minjun.maker',
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CURRENT_TIMESTAMP - INTERVAL '2 hours' + INTERVAL '1 second'
),
(
    'job-kkumz-004',
    'site-kkumz-004',
    'rel-kkumz-004-v1',
    'WEBIDE_BUNDLE_BUILD',
    'CUSTOM_BUNDLE',
    'SUCCESS',
    1980,
    NULL,
    FALSE,
    '[15:10:00] [Hybrid-Deploy] Linked store catalog + editorial journal. Custom bundle built and published.',
    'hajun.roast',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '2 seconds'
);

-- Team Recruitment Posts (kkumz_team_recruit)
INSERT INTO kkumz_team_recruit (
    id, site_id, position_role, title, description, required_skills_json, headcount, recruited_count, status, created_at
) VALUES
(
    'rec-kkumz-001',
    'site-kkumz-001',
    'MARKETER',
    '제주 유기농 말차 브랜드 숏폼 콘텐츠 & 글로벌 D2C 그로스 마케터 모십니다',
    '서귀포 다원 현지 스토리텔링 릴스/쇼츠 기획과 북미·일본 마켓 초기 진출 캠페인을 함께 리드할 코파운더급 브랜드 마케터를 찾습니다. 초기 지분 스톡옵션 및 매출 인센티브를 함께 나눕니다.',
    '["인스타그램 릴스 기획", "브랜드 카피라이팅", "Meta/Google 퍼포먼스 광고", "글로벌 D2C"]'::jsonb,
    2,
    1,
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '4 days'
),
(
    'rec-kkumz-002',
    'site-kkumz-001',
    'DESIGNER',
    '수제 백자 다완 패키지 & 보자기 기프트 언박싱 경험 디자이너 모집',
    '친환경 지류 패키지와 보자기 선물 포장, 다도 가이드북 편집 디자인을 담당할 브랜드/패키지 디자이너를 모십니다.',
    '["패키지 디자인", "브랜드 아이덴티티", "Figma", "인쇄 감리"]'::jsonb,
    1,
    0,
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'rec-kkumz-003',
    'site-kkumz-002',
    'FRONTEND',
    '3D 책상 배치 컨피규레이터(Three.js / Vue 3) 함께 개발할 프론트엔드 엔지니어',
    '데스크 오빗 스토어 내에서 고객이 자신의 책상 크기(1200~1800mm)에 맞춰 월넛 데스크 쉘프와 모니터, 충전 독을 3D로 배치해 볼 수 있는 인터랙티브 컨피규레이터를 개발할 프론트엔드 팀원을 찾습니다.',
    '["Vue 3", "TypeScript", "Three.js / WebGL", "인터랙티브 UI"]'::jsonb,
    1,
    0,
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    'rec-kkumz-004',
    'site-kkumz-002',
    'MD',
    '커스텀 키보드·데스크 기어 글로벌 소싱 및 프리오더 운영 MD 모집',
    '알루미늄 CNC 가공 파트너사 커뮤니케이션 및 신규 데스크테리어 기어 라인업 기획을 함께할 하드웨어/커머스 MD를 찾습니다.',
    '["하드웨어 소싱", "와디즈/킥스타터 펀딩", "커머스 상품기획"]'::jsonb,
    1,
    1,
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'rec-kkumz-005',
    'site-kkumz-003',
    'PLANNER',
    '한강 나이트 런 시즌 마라톤 대회 제휴 & 브랜드 콜라보 커뮤니티 매니저',
    '스포츠 브랜드 팝업 세션 제휴 및 200명 규모 한강 나이트 런 행사 운영을 함께 기획할 커뮤니티 플래너를 모십니다.',
    '["오프라인 이벤트 기획", "브랜드 파트너십", "러닝 크루 운영"]'::jsonb,
    2,
    1,
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'rec-kkumz-006',
    'site-kkumz-004',
    'CO_FOUNDER',
    '스페셜티 원두 월간 구독 모델 & 오프라인 쇼룸 비즈니스 리드 코파운더',
    '성수동 로스터리 쇼룸 오프라인 커핑 클래스와 온라인 정기구독 리텐션 모델을 함께 설계할 비즈니스 공동창업자를 찾습니다.',
    '["구독 커머스 전략", "F&B 브랜딩", "B2B 원두 세일즈"]'::jsonb,
    1,
    0,
    'OPEN',
    CURRENT_TIMESTAMP - INTERVAL '1 day'
);

-- Team Members & Applications (kkumz_team_member)
INSERT INTO kkumz_team_member (
    id, site_id, recruit_id, username, display_name, member_role, position_role, join_type, application_message, status, joined_at
) VALUES
('tmem-kkumz-001', 'site-kkumz-001', NULL, 'soyeon.jeju', '한소연 대표', 'OWNER', 'CO_FOUNDER', 'FOUNDER', '제주 유기농 다원 총괄 및 브랜드 디렉팅', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '5 days'),
('tmem-kkumz-002', 'site-kkumz-001', 'rec-kkumz-001', 'yuna.growth', '김유나 마케터', 'EDITOR', 'MARKETER', 'APPLIED', '티 브랜드 인스타그램 릴스 100만 뷰 캠페인 경험이 있습니다. 제주 말차의 매력을 글로벌 MZ세대에게 알리고 싶습니다!', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '3 days'),
('tmem-kkumz-003', 'site-kkumz-001', 'rec-kkumz-002', 'daeun.pack', '이다은 패키지디자이너', 'EDITOR', 'DESIGNER', 'APPLIED', '전통 한지와 친환경 펄프 몰드를 활용한 프리미엄 패키지 디자인 포트폴리오를 보유하고 있습니다. 함께하고 싶어요.', 'PENDING', CURRENT_TIMESTAMP - INTERVAL '6 hours'),
('tmem-kkumz-004', 'site-kkumz-002', NULL, 'minjun.maker', '박민준 메이커', 'OWNER', 'CO_FOUNDER', 'FOUNDER', '제품 기구 설계 및 데스크 오빗 총괄 디렉터', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '4 days'),
('tmem-kkumz-005', 'site-kkumz-002', 'rec-kkumz-004', 'hyunwoo.md', '장현우 테크MD', 'MD', 'MD', 'INVITED', '커스텀 키보드 및 데스크 기어 전문 MD 합류', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('tmem-kkumz-006', 'site-kkumz-002', 'rec-kkumz-003', 'seojun.fe', '윤서준 프론트엔드', 'DEVELOPER', 'FRONTEND', 'APPLIED', 'Vue 3와 Three.js로 가구 배치 시뮬레이터를 구현해 본 경험이 있습니다. 데스크 오빗 3D 컨피규레이터를 완성하고 싶습니다.', 'PENDING', CURRENT_TIMESTAMP - INTERVAL '4 hours'),
('tmem-kkumz-007', 'site-kkumz-003', NULL, 'jihoon.run', '정지훈 크루장', 'OWNER', 'PLANNER', 'FOUNDER', '서울 나이트 런 클럽 헤드 코치 및 크루장', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '3 days'),
('tmem-kkumz-008', 'site-kkumz-004', NULL, 'hajun.roast', '최하준 헤드로스터', 'OWNER', 'CO_FOUNDER', 'FOUNDER', 'Q-Grader 및 빈 크로니클 수석 로스터', 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '2 days');

-- Community Feed Comments (kkumz_feed_comment)
INSERT INTO kkumz_feed_comment (
    id, site_id, username, display_name, author_role_badge, content, created_at
) VALUES
('cmt-kkumz-001', 'site-kkumz-001', 'minjun.maker', '박민준 메이커', '프로덕트 메이커', '보태니컬 딥그린 컬러 팔레트와 백자 다완 스타터 세트 구성이 정말 감각적이네요! 라이브 스토어(/s/matcha-atelier)에서 말차 라떼 베이스 바로 주문했습니다.', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('cmt-kkumz-002', 'site-kkumz-001', 'hajun.roast', '최하준 헤드로스터', '로스터리 창업가', '성수동 저희 로스터리 쇼룸에서도 제주 첫물 말차 콜라보 시즌 메뉴를 기획해 보고 싶습니다. 응원합니다!', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('cmt-kkumz-003', 'site-kkumz-002', 'seojun.fe', '윤서준 프론트엔드', '프론트엔드 개발자', 'Qi2 3-in-1 알루미늄 충전 독과 월넛 데스크 쉘프 조합이 개발자 취향 저격입니다. 프론트엔드 포지션에 지원서 남겼습니다!', CURRENT_TIMESTAMP - INTERVAL '18 hours'),
('cmt-kkumz-004', 'site-kkumz-003', 'soyeon.jeju', '한소연 대표', '브랜드 창업가', '반포 잠수교 나이트 런 코스 가이드 아티클이 정말 자세히 정리되어 있네요. 이번 주 목요일 600 페이스 게스트 런 신청 완료했습니다!', CURRENT_TIMESTAMP - INTERVAL '12 hours'),
('cmt-kkumz-005', 'site-kkumz-004', 'jihoon.run', '정지훈 크루장', '커뮤니티 리더', '산지 농장 직교역 매거진 읽다가 브루잉 머신이랑 원두까지 한 화면에서 바로 담을 수 있는 복합 사이트 구성이 무척 매끄럽습니다.', CURRENT_TIMESTAMP - INTERVAL '5 hours');

-- Community Feed Likes (kkumz_feed_like)
INSERT INTO kkumz_feed_like (id, site_id, username, created_at) VALUES
('like-kkumz-001', 'site-kkumz-001', 'admin', CURRENT_TIMESTAMP - INTERVAL '3 days'),
('like-kkumz-002', 'site-kkumz-001', 'minjun.maker', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('like-kkumz-003', 'site-kkumz-002', 'admin', CURRENT_TIMESTAMP - INTERVAL '3 days'),
('like-kkumz-004', 'site-kkumz-002', 'soyeon.jeju', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('like-kkumz-005', 'site-kkumz-003', 'admin', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('like-kkumz-006', 'site-kkumz-004', 'admin', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- Moderation Reports (kkumz_moderation_report)
INSERT INTO kkumz_moderation_report (
    id, target_type, target_id, site_id, reporter_username, reason_category, reason_detail,
    status, action_taken, moderator_username, resolved_at, created_at
) VALUES
(
    'rep-kkumz-001',
    'COMMENT',
    'cmt-spam-901',
    'site-kkumz-002',
    'minjun.maker',
    'SPAM_PROMOTION',
    '프로젝트 피드 댓글란에 무관한 외부 불법 홍보 링크를 반복 게시하는 스팸 댓글이 감지되어 신고합니다.',
    'PENDING',
    NULL,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '3 hours'
),
(
    'rep-kkumz-002',
    'RECRUIT',
    'rec-kkumz-005',
    'site-kkumz-003',
    'guest.runner',
    'INFO_VERIFICATION',
    '오프라인 나이트 런 세션 참가비 및 보험 가입 여부에 대한 안내 문구 보완이 필요합니다.',
    'PENDING',
    NULL,
    NULL,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '5 hours'
),
(
    'rep-kkumz-003',
    'SITE',
    'site-kkumz-001',
    'site-kkumz-001',
    'competitor.check',
    'COPYRIGHT_TRADEMARK',
    '유기농 인증 번호 표기 누락 여부 확인 요청 건',
    'DISMISSED',
    '서귀포 유기농 친환경 인증서 원본 및 상세페이지 하단 법적 필수 표기 사항이 정상 확인되어 신고 기각 처리함',
    'admin',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
);

-- Notifications (kkumz_notification)
INSERT INTO kkumz_notification (
    id, recipient_username, notif_type, title, message, link_url, is_read, created_at
) VALUES
('noti-kkumz-001', 'soyeon.jeju', 'TEAM_APPLY', '패키지 디자이너 포지션 신규 지원 도착', '이다은 패키지디자이너님이 [말차 아뜰리에 제주] 팀 합류를 신청했습니다.', '/lounge', FALSE, CURRENT_TIMESTAMP - INTERVAL '6 hours'),
('noti-kkumz-002', 'minjun.maker', 'TEAM_APPLY', '프론트엔드 엔지니어 포지션 신규 지원 도착', '윤서준 프론트엔드님이 [데스크 오빗] 팀 합류를 신청했습니다.', '/lounge', FALSE, CURRENT_TIMESTAMP - INTERVAL '4 hours'),
('noti-kkumz-003', 'soyeon.jeju', 'BUILD_DONE', '말차 아뜰리에 제주 v2 배포 완료', '커스텀 번들 빌드가 성공적으로 완료되어 /s/matcha-atelier 에 즉시 반영되었습니다.', '/s/matcha-atelier', TRUE, CURRENT_TIMESTAMP - INTERVAL '1 hour');

-- ============================================================================
-- 맑은물 민박 (idea-01137479) 버전·네이밍·로고·화면설계·업로드 사진 자산 시드
-- ============================================================================
INSERT INTO kkumz_idea_version (
    id, idea_id, version_no, title, prompt_text, spec_markdown, screens_json, created_by, created_at
) VALUES
(
    'iver-idea-01137479-v1',
    'idea-01137479',
    1,
    '맑은물 민박 — 공주 냇가 독채 스테이 & 평상 예약 복합 기획안 (v1)',
    '충남 공주 시골 맑은 냇가 바로 옆에 위치한 정겨운 맑은물 민박 공식 사이트입니다. 황토 온돌 독채 객실과 냇가 평상 당일 대여, 능이 토종닭 백숙·숯불 바비큐 세트를 예약받고, 성수기/비수기 상세 요금표와 사계절 현장 사진 갤러리(12장), 공간 구역별 투어, 체크인 날짜·인원·바비큐 옵션을 선택하는 맞춤 예약 신청 폼, 숙박 FAQ, 공주 오시는 길 및 주변 추천 관광코스를 한눈에 제공합니다.',
    '# 맑은물 민박 서비스 기능명세서\n\n## 핵심 제공 기능 및 공간 경험\n- 충남 공주 청정 냇가 바로 앞 프라이빗 황토 독채 & 별채 온돌방 실시간 예약\n- 여름철 시원한 냇가 평상·정자 당일 대여 및 능이 토종닭 백숙·숯불 바비큐 패키지 주문\n- 비수기·성수기·주중·주말 및 인원 추가 기준이 정리된 투명한 시즌 요금표 매트릭스\n- 사계절 현장 사진 갤러리(12장) 및 실내·평상존·냇가 물놀이장 구역별 스페이스 투어\n- 체크인 날짜, 희망 객실/평상, 방문 인원수, 식사·바비큐 옵션을 선택하는 동적 예약 신청 폼\n- 자주 묻는 질문(FAQ), 공주 오시는 길·주변 관광코스 및 24시간 AI 민박 컨시어지 상담\n',
    '[
      {"screenKey":"SCR_HOME","screenTitle":"메인 히어로 & 객실·평상 카탈로그","routePath":"/","layoutType":"HYBRID_EDITORIAL_COMMERCE","description":"공주 냇가 대표 전경 배너와 독채 객실·평상 대여·백숙 패키지 쇼케이스","components":["CMS_HERO_SHOWCASE","CMS_TRUST_METRICS_BAR","SHOP_ROOM_SHOWCASE","SHOP_SEASONAL_MENU","SHOP_PRICING_MATRIX"]},
      {"screenKey":"SCR_GALLERY","screenTitle":"사계절 현장 포토 갤러리 & 공간 투어","routePath":"/gallery","layoutType":"GALLERY_SHOWCASE","description":"여름 계곡 물놀이, 가을 단풍 정자, 겨울 눈썰매 등 12장의 현장 사진과 구역별 안내","components":["CMS_SPACE_TOUR","CMS_PHOTO_GALLERY","CMS_STEP_RITUAL_GUIDE"]},
      {"screenKey":"SCR_MAGAZINE","screenTitle":"맑은물 계절 소식 & 방문객 후기","routePath":"/magazine","layoutType":"MAGAZINE_LIST","description":"공주 계절 소식 저널, 실제 가족 방문객 포토 후기 및 선호도 투표","components":["CMS_BRAND_MAGAZINE","SHOP_PHOTO_REVIEWS","CMS_LIVE_POLL"]},
      {"screenKey":"SCR_RESERVATION","screenTitle":"맞춤 일정·객실·바비큐 온라인 예약","routePath":"/reserve","layoutType":"RESERVATION_FORM","description":"체크인 날짜, 객실/평상 타입, 인원수, 능이백숙·숯불 옵션을 선택하는 동적 예약 폼","components":["CMS_DYNAMIC_BOOKING_FORM","SHOP_COUPON_PACK","CMS_POLICY_RULES_BOX"]},
      {"screenKey":"SCR_FAQ_CONCIERGE","screenTitle":"숙박 FAQ · 오시는 길 & AI 컨시어지","routePath":"/concierge","layoutType":"FAQ_CONCIERGE","description":"자주 묻는 질문 아코디언, 공주 네비게이션 주소·주변 코스 및 24시간 AI 상담","components":["AI_TASTE_CURATOR","CMS_FAQ_ACCORDION","CMS_LOCATION_DIRECTIONS","AI_BRAND_CONCIERGE"]}
    ]'::jsonb,
    'admin',
    CURRENT_TIMESTAMP - INTERVAL '1 day'
);

INSERT INTO kkumz_name_suggestion (
    id, idea_id, brand_name, site_slug, pronunciation, brand_story, score, slug_available, is_selected, created_at
) VALUES
('name-idea-01137479-1', 'idea-01137479', '맑은물 민박', 'malgeunmul-minbak', '맑은물 민박 / Malgeunmul Minbak', '공주 시골 냇가 옆, 맑고 차가운 물소리와 따뜻한 황토 온돌이 어우러진 사계절 정겨운 시골 스테이입니다.', 99, TRUE, TRUE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('name-idea-01137479-2', 'idea-01137479', '공주냇가 민박', 'gongju-netga-minbak', '공주냇가 민박 / Gongju Netga Minbak', '충남 공주의 정겨운 냇가 풍경을 담은 민박으로, 도시를 벗어나 자연 속에서 여유를 되찾는 하루를 선사합니다.', 97, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('name-idea-01137479-3', 'idea-01137479', '맑은냇가 스테이', 'malgeun-netga-stay', '맑은냇가 스테이 / Malgeun Netga Stay', '맑은 물길과 새소리가 어우러진 냇가 옆 프라이빗 독채 스테이 공간입니다.', 95, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('name-idea-01137479-4', 'idea-01137479', '물안개 민박', 'mulangae-minbak', '물안개 민박 / Mulangae Minbak', '아침 냇가 위로 피어오르는 물안개처럼 잔잔하고 포근한 공주 시골의 숨은 휴식 명소입니다.', 93, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('name-idea-01137479-5', 'idea-01137479', '두물머리 민가', 'dumulmeori-minga', '두물머리 민가 / Dumulmeori Minga', '두 물줄기가 만나는 평화로운 냇가 끝자락, 마음이 편안해지는 시골 고택형 민박입니다.', 91, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('name-idea-01137479-6', 'idea-01137479', '청류민가 공주', 'cheongryu-minga', '청류민가 공주 / Cheongryu Minga', '맑은 물줄기(청류)를 품은 시골 집으로 공주 냇가의 사계절 정서를 담아낸 공간입니다.', 89, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('name-idea-01137479-7', 'idea-01137479', '수향민박', 'suhyang-minbak', '수향민박 / Suhyang Minbak', '물의 향기와 솔바람을 머금은 공주 냇가 옆 가족 휴양 민박입니다.', 87, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('name-idea-01137479-8', 'idea-01137479', '네이처브릿지 공주', 'nature-bridge-gongju', '네이처브릿지 공주 / Nature Bridge Gongju', '자연과 도시를 잇는 다리처럼 공주 냇가에서 진정한 회복을 경험하게 해주는 감성 민박입니다.', 85, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day');

INSERT INTO kkumz_logo_suggestion (
    id, idea_id, style_name, svg_code, color_palette_json, typography_json, tags_json, is_selected, created_at
) VALUES
('logo-idea-01137479-1', 'idea-01137479', '내추럴 라인아트', '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect width="40" height="40" rx="10" fill="#0f766e"/><path d="M8 25 C13 19, 18 19, 23 25 C26 28, 30 28, 33 23" stroke="#99f6e4" stroke-width="2.5" fill="none" stroke-linecap="round"/><path d="M12 18 L20 11 L28 18" stroke="#ffffff" stroke-width="2.2" fill="none" stroke-linecap="round" stroke-linejoin="round"/><text x="48" y="25" font-family="Pretendard, sans-serif" font-size="15" font-weight="800" fill="#0f766e">맑은물 민박</text></svg>', '{"primary":"#0f766e","secondary":"#115e59","accent":"#d97706","surface":"#f0fdf4","radius":"14px"}'::jsonb, '{"fontHeading":"Pretendard","fontBody":"Pretendard"}'::jsonb, '["자연", "수목", "물결", "시골", "휴식"]'::jsonb, TRUE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('logo-idea-01137479-2', 'idea-01137479', '모던 지오메트릭', '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><rect x="6" y="6" width="28" height="28" rx="6" fill="#0f766e"/><polygon points="10,28 19,18 28,28" fill="#ffffff"/><text x="44" y="25" font-family="Pretendard, sans-serif" font-size="15" font-weight="800" fill="#0f172a">맑은물 민박</text></svg>', '{"primary":"#0f766e","secondary":"#0284c7","accent":"#f97316","surface":"#f8fafc","radius":"14px"}'::jsonb, '{"fontHeading":"Pretendard","fontBody":"Pretendard"}'::jsonb, '["기하학", "현대적", "단정함"]'::jsonb, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('logo-idea-01137479-3', 'idea-01137479', '클래식 엠블럼', '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><circle cx="20" cy="20" r="16" fill="#0f172a"/><circle cx="20" cy="20" r="10" fill="#0f766e"/><text x="44" y="25" font-family="Pretendard, sans-serif" font-size="15" font-weight="800" fill="#0f172a">맑은물 민박</text></svg>', '{"primary":"#0f766e","secondary":"#0f172a","accent":"#d97706","surface":"#f1f5f9","radius":"14px"}'::jsonb, '{"fontHeading":"Pretendard","fontBody":"Pretendard"}'::jsonb, '["전통", "엠블럼", "신뢰"]'::jsonb, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('logo-idea-01137479-4', 'idea-01137479', '소프트 워터컬러 배지', '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><circle cx="20" cy="20" r="14" fill="#a7f3d0"/><path d="M12 18 Q18 13 24 18 T30 23" stroke="#0f766e" stroke-width="2.2" fill="none"/><text x="44" y="25" font-family="Pretendard, sans-serif" font-size="15" font-weight="800" fill="#0f172a">맑은물 민박</text></svg>', '{"primary":"#0f766e","secondary":"#059669","accent":"#d97706","surface":"#f0fdf4","radius":"14px"}'::jsonb, '{"fontHeading":"Pretendard","fontBody":"Pretendard"}'::jsonb, '["물결", "자연", "감성"]'::jsonb, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('logo-idea-01137479-5', 'idea-01137479', '미니멀 아치', '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><path d="M10 28 Q14 14 22 28 T34 28" stroke="#0f172a" stroke-width="2.5" fill="none"/><text x="44" y="25" font-family="Pretendard, sans-serif" font-size="15" font-weight="800" fill="#0f172a">맑은물 민박</text></svg>', '{"primary":"#0f172a","secondary":"#0f766e","accent":"#10b981","surface":"#f0fdf4","radius":"10px"}'::jsonb, '{"fontHeading":"Pretendard","fontBody":"Pretendard"}'::jsonb, '["아치", "평온", "스테이"]'::jsonb, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('logo-idea-01137479-6', 'idea-01137479', '에디토리얼 시그니처', '<svg viewBox="0 0 185 40" xmlns="http://www.w3.org/2000/svg"><path d="M10 25 L19 15 L28 25" stroke="#0f766e" stroke-width="2.5" fill="none"/><path d="M14 22 Q20 16 26 22" stroke="#d97706" stroke-width="2" fill="none"/><text x="44" y="25" font-family="Pretendard, sans-serif" font-size="15" font-weight="800" fill="#0f172a">맑은물 민박</text></svg>', '{"primary":"#0f766e","secondary":"#1e293b","accent":"#d97706","surface":"#f8fafc","radius":"12px"}'::jsonb, '{"fontHeading":"Pretendard","fontBody":"Pretendard"}'::jsonb, '["시그니처", "정제된감성"]'::jsonb, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day');

INSERT INTO kkumz_screen_spec (
    id, idea_id, screen_key, screen_title, route_path, layout_type, description, components_json, sort_no, created_at
) VALUES
('scr-01137479-01', 'idea-01137479', 'SCR_HOME', '메인 히어로 & 객실·평상 카탈로그', '/', 'HYBRID_EDITORIAL_COMMERCE', '공주 냇가 대표 전경 배너와 독채 객실·평상 대여·백숙 패키지 쇼케이스', '["CMS_HERO_SHOWCASE","CMS_TRUST_METRICS_BAR","SHOP_ROOM_SHOWCASE","SHOP_SEASONAL_MENU","SHOP_PRICING_MATRIX"]'::jsonb, 1, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('scr-01137479-02', 'idea-01137479', 'SCR_GALLERY', '사계절 현장 포토 갤러리 & 공간 투어', '/gallery', 'GALLERY_SHOWCASE', '여름 계곡 물놀이, 가을 단풍 정자, 겨울 눈썰매 등 12장의 현장 사진과 구역별 안내', '["CMS_SPACE_TOUR","CMS_PHOTO_GALLERY","CMS_STEP_RITUAL_GUIDE"]'::jsonb, 2, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('scr-01137479-03', 'idea-01137479', 'SCR_MAGAZINE', '맑은물 계절 소식 & 방문객 후기', '/magazine', 'MAGAZINE_LIST', '공주 계절 소식 저널, 실제 가족 방문객 포토 후기 및 선호도 투표', '["CMS_BRAND_MAGAZINE","SHOP_PHOTO_REVIEWS","CMS_LIVE_POLL"]'::jsonb, 3, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('scr-01137479-04', 'idea-01137479', 'SCR_RESERVATION', '맞춤 일정·객실·바비큐 온라인 예약', '/reserve', 'RESERVATION_FORM', '체크인 날짜, 객실/평상 타입, 인원수, 능이백숙·숯불 옵션을 선택하는 동적 예약 폼', '["CMS_DYNAMIC_BOOKING_FORM","SHOP_COUPON_PACK","CMS_POLICY_RULES_BOX"]'::jsonb, 4, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('scr-01137479-05', 'idea-01137479', 'SCR_FAQ_CONCIERGE', '숙박 FAQ · 오시는 길 & AI 컨시어지', '/concierge', 'FAQ_CONCIERGE', '자주 묻는 질문 아코디언, 공주 네비게이션 주소·주변 코스 및 24시간 AI 상담', '["AI_TASTE_CURATOR","CMS_FAQ_ACCORDION","CMS_LOCATION_DIRECTIONS","AI_BRAND_CONCIERGE"]'::jsonb, 5, CURRENT_TIMESTAMP - INTERVAL '1 day');

INSERT INTO kkumz_idea_asset (
    id, idea_id, asset_type, original_name, stored_name, file_url, mime_type, file_size, caption, extracted_text, sort_no, uploaded_by, created_at
) VALUES
('ast-4c14d423', 'idea-01137479', 'HERO', 'banner-minbak-creek.jpg', 'banner-minbak-creek.jpg', '/images/studio/banner-minbak-creek.jpg', 'image/jpeg', 682400, '공주 청정 냇가 바로 옆 황토 독채와 나무 그늘 평상 전경', NULL, 1, 'admin', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('ast-ab06955d', 'idea-01137479', 'PRODUCT', 'minbak-room-ondol.jpg', 'minbak-room-ondol.jpg', '/images/studio/minbak-room-ondol.jpg', 'image/jpeg', 598120, '천연 황토벽과 편백 원목으로 완성한 따뜻한 온돌 독채 실내', NULL, 2, 'admin', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('ast-722c5dd5', 'idea-01137479', 'PRODUCT', 'minbak-pyeongsang.jpg', 'minbak-pyeongsang.jpg', '/images/studio/minbak-pyeongsang.jpg', 'image/jpeg', 612300, '시원한 계곡물 바로 옆 나무 그늘 프라이빗 원목 평상', NULL, 3, 'admin', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('ast-2b5a8627', 'idea-01137479', 'PRODUCT', 'minbak-dining-bbq.jpg', 'minbak-dining-bbq.jpg', '/images/studio/minbak-dining-bbq.jpg', 'image/jpeg', 645900, '노을 지는 야외 테라스 참숯 바비큐 & 능이 토종닭 백숙 한상', NULL, 4, 'admin', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('ast-03809f76', 'idea-01137479', 'GALLERY', 'minbak-autumn-yard.jpg', 'minbak-autumn-yard.jpg', '/images/studio/minbak-autumn-yard.jpg', 'image/jpeg', 631400, '황금빛 은행나무와 붉은 단풍에 둘러싸인 아늑한 민박 돌마당', NULL, 5, 'admin', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('ast-04ed63a9', 'idea-01137479', 'GALLERY', 'minbak-winter-snow.jpg', 'minbak-winter-snow.jpg', '/images/studio/minbak-winter-snow.jpg', 'image/jpeg', 589200, '눈이 소복이 쌓인 겨울 냇가 정자와 고요한 시골 설경', NULL, 6, 'admin', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- Managed Git Repositories for Provisioned Sites (kkumz_site_git_repo)
INSERT INTO kkumz_site_git_repo (
    id, site_id, provider, repo_owner, repo_name, default_branch, visibility,
    repo_html_url, clone_ssh_url, clone_https_url,
    ssh_key_title, ssh_key_fingerprint, ssh_public_key, ssh_private_key_pem,
    site_api_key_id, site_access_key, site_secret_key,
    last_commit_sha, last_commit_message, sync_mode, github_remote_synced,
    commit_history_json, key_rotated_at, last_synced_at, created_by, created_at, updated_at
) VALUES
(
    'git-kkumz-001',
    'site-kkumz-001',
    'GITHUB',
    'kkumz-sites',
    'matcha-atelier-storefront',
    'main',
    'PRIVATE',
    'https://github.com/kkumz-sites/matcha-atelier-storefront',
    'git@github.com:kkumz-sites/matcha-atelier-storefront.git',
    'https://github.com/kkumz-sites/matcha-atelier-storefront.git',
    'kkumz-deploy-key-matcha-atelier',
    'SHA256:9xK2mP7vL4qR8wN1tY5bC3dF6gH0jK9lM2nP4qR6sT8',
    'ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIKkumzMatchaAtelierDeployKeyPublic001 deploy@matcha-atelier.kkumz.io',
    '-----BEGIN OPENSSH PRIVATE KEY-----\nb3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW\nQyNTUxOQAAACCpKkumzMatchaAtelierDeployKeyPrivate001==\n-----END OPENSSH PRIVATE KEY-----',
    'ak-kkumz-out-shop',
    'KKUMZ_SITE_AK_MATCHA_ATELIER_001',
    'sk_site_matcha_atelier_live_98a7b6c5d4e3f2g1h0',
    'a4f82c91e7b03d56f1298c45e67a89b012c34d56',
    'feat(storefront): initialize ceremonial matcha catalog and AI tea sommelier widget',
    'MANAGED_GIT',
    TRUE,
    '[{"sha":"a4f82c91e7b03d56f1298c45e67a89b012c34d56","author":"kkumz-bot","message":"feat(storefront): initialize ceremonial matcha catalog and AI tea sommelier widget","timestamp":"2026-11-12T10:15:00Z"}]'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    'soyeon.jeju',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
),
(
    'git-kkumz-002',
    'site-kkumz-002',
    'GITHUB',
    'kkumz-sites',
    'desk-orbit-storefront',
    'main',
    'PRIVATE',
    'https://github.com/kkumz-sites/desk-orbit-storefront',
    'git@github.com:kkumz-sites/desk-orbit-storefront.git',
    'https://github.com/kkumz-sites/desk-orbit-storefront.git',
    'kkumz-deploy-key-desk-orbit',
    'SHA256:3bN8vQ1zX5cR9mP2wL7kJ4hG6fD0sA8eW1yU3iO5pL7',
    'ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIKkumzDeskOrbitDeployKeyPublic002 deploy@desk-orbit.kkumz.io',
    '-----BEGIN OPENSSH PRIVATE KEY-----\nb3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW\nQyNTUxOQAAACCpKkumzDeskOrbitDeployKeyPrivate002==\n-----END OPENSSH PRIVATE KEY-----',
    'ak-kkumz-out-shop',
    'KKUMZ_SITE_AK_DESK_ORBIT_002',
    'sk_site_desk_orbit_live_12f3e4d5c6b7a8n9m0',
    'c91d4e72b8a35f60d2341e89a76b54c321d09e87',
    'feat(workspace): configure Qi2 charging station 3D showcase and bundle configurator',
    'MANAGED_GIT',
    TRUE,
    '[{"sha":"c91d4e72b8a35f60d2341e89a76b54c321d09e87","author":"kkumz-bot","message":"feat(workspace): configure Qi2 charging station 3D showcase and bundle configurator","timestamp":"2026-11-12T14:30:00Z"}]'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '4 days',
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    'minjun.maker',
    CURRENT_TIMESTAMP - INTERVAL '4 days',
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
);

-- Initial System Prompt History Snapshot (sys_prompt_history)
INSERT INTO sys_prompt_history (
    id, prompt_id, app_code, prompt_code, version,
    system_prompt, user_prompt_template, variables_schema, output_schema,
    model_override, temperature, change_reason, created_by, created_at
)
SELECT
    REPLACE(id, 'prm-', 'prmh-') || '-v1',
    id,
    app_code,
    prompt_code,
    version,
    system_prompt,
    user_prompt_template,
    variables_schema,
    output_schema,
    model_override,
    temperature,
    '초기 표준 프롬프트 템플릿 시드 등록 (v1)',
    create_by,
    create_time
FROM sys_prompt_template
WHERE app_code = 'KKUMZ';

